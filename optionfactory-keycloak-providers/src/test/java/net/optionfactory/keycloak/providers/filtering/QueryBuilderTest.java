package net.optionfactory.keycloak.providers.filtering;

import org.junit.jupiter.api.Assertions;
import jakarta.ws.rs.BadRequestException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class QueryBuilderTest {

    private static class RecordedQuery implements InvocationHandler {

        public String sql;
        public final Map<Integer, Object> parameters = new LinkedHashMap<>();
        public Integer firstResult;
        public Integer maxResults;

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            return switch (method.getName()) {
                case "setParameter" -> {
                    parameters.put((Integer) args[0], args[1]);
                    yield proxy;
                }
                case "setFirstResult" -> {
                    firstResult = (Integer) args[0];
                    yield proxy;
                }
                case "setMaxResults" -> {
                    maxResults = (Integer) args[0];
                    yield proxy;
                }
                default ->
                    null;
            };
        }

        public Query proxy() {
            return (Query) Proxy.newProxyInstance(Query.class.getClassLoader(), new Class<?>[]{Query.class}, this);
        }

        public List<Object> boundValues() {
            return new ArrayList<>(parameters.values());
        }
    }

    private static class RecordedEm implements InvocationHandler {

        public RecordedQuery query;

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if ("createNativeQuery".equals(method.getName())) {
                query = new RecordedQuery();
                query.sql = (String) args[0];
                return query.proxy();
            }
            throw new UnsupportedOperationException(method.getName());
        }

        public EntityManager proxy() {
            return (EntityManager) Proxy.newProxyInstance(EntityManager.class.getClassLoader(), new Class<?>[]{EntityManager.class}, this);
        }
    }

    private static final QueryBuilder ORDERED = new QueryBuilder("select id from user_entity u where realm_id = ? {CONDITIONS} {ORDER_CLAUSE} {COUNT_OVER_TOTAL}")
            .orderedBy("id")
            .sorter("username", "u.username");

    private static final QueryBuilder USERS = new QueryBuilder("select id from user_entity u where realm_id = ? {CONDITIONS} {ORDER_CLAUSE} {COUNT_OVER_TOTAL}")
            .filter(new TextFilter("username", "u.username"))
            .filter(new BooleanFilter("enabled", "u.enabled"))
            .sorter("username", "u.username")
            .sorter("createdAt", "u.created_timestamp");

    @Test
    public void queryWithoutFiltersOrSortersKeepsTemplateIntact() {
        final var em = new RecordedEm();
        USERS.create(em.proxy(), Map.of(), List.of(), false, 0, 0, "realm-x");
        Assertions.assertEquals("select id from user_entity u where realm_id = ?   ", em.query.sql);
        Assertions.assertEquals(Map.of(1, "realm-x"), em.query.parameters);
    }

    @Test
    public void unknownFiltersAndSortersAreDropped() {
        final var em = new RecordedEm();
        USERS.create(em.proxy(), Map.of("hacker", new String[]{"EQ", "CASE_SENSITIVE", "nope"}), List.of("hacker; drop table user_entity"), false, 0, 0, "realm-x");
        Assertions.assertFalse(em.query.sql.contains("hacker"));
        Assertions.assertEquals(Map.of(1, "realm-x"), em.query.parameters);
    }

    @Test
    public void filtersAreAndComposedAndBoundAfterTemplateParameters() {
        final var em = new RecordedEm();
        final var requested = new LinkedHashMap<String, String[]>();
        requested.put("username", new String[]{"EQ", "IGNORE_CASE", "Wyatt"});
        requested.put("enabled", new String[]{"EQ", "true"});
        USERS.create(em.proxy(), requested, List.of(), false, 0, 0, "realm-x");
        Assertions.assertTrue(em.query.sql.contains("and lower(u.username) = ? and u.enabled = ?"));
        Assertions.assertEquals(List.of("realm-x", "wyatt", Boolean.TRUE), em.query.boundValues());
    }

    @Test
    public void sortersCompileToOrderByClause() {
        final var em = new RecordedEm();
        USERS.create(em.proxy(), Map.of(), List.of("username,DESC", "createdAt"), false, 0, 0, "realm-x");
        Assertions.assertTrue(em.query.sql.contains("order by u.username DESC,u.created_timestamp ASC"));
    }

    @Test
    public void countOverTotalIsInjectable() {
        final var em = new RecordedEm();
        USERS.create(em.proxy(), Map.of(), List.of(), true, 0, 0, "realm-x");
        Assertions.assertTrue(em.query.sql.contains(", count(*) over() as total"));
    }

    @Test
    public void offsetAndLimitAreAppliedWhenNonZero() {
        final var em = new RecordedEm();
        USERS.create(em.proxy(), Map.of(), List.of(), false, 40, 20, "realm-x");
        Assertions.assertEquals(40, em.query.firstResult);
        Assertions.assertEquals(20, em.query.maxResults);
    }

    @Test
    public void zeroOffsetAndLimitMeanUnset() {
        final var em = new RecordedEm();
        USERS.create(em.proxy(), Map.of(), List.of(), false, 0, 0, "realm-x");
        Assertions.assertEquals(null, em.query.firstResult);
        Assertions.assertEquals(null, em.query.maxResults);
    }

    @Test
    public void likeFiltersBindEscapedPatterns() {
        final var em = new RecordedEm();
        USERS.create(em.proxy(), Map.of("username", new String[]{"CONTAINS", "IGNORE_CASE", "50%_off"}), List.of(), false, 0, 0, "realm-x");
        Assertions.assertTrue(em.query.sql.contains("u.username ilike ?"));
        Assertions.assertEquals(List.of("realm-x", "%50\\%\\_off%"), em.query.boundValues());
    }
    @Test
    public void anAbsentFilterMapIsTreatedAsNoFilters() {
        // an empty request body deserializes to null, and every filter reads values[0] straight away
        final var em = new RecordedEm();

        USERS.create(em.proxy(), null, List.of(), false, 0, 0, "realm-x");

        Assertions.assertEquals("select id from user_entity u where realm_id = ?   ", em.query.sql);
        Assertions.assertEquals(Map.of(1, "realm-x"), em.query.parameters);
    }

    @Test
    public void aNullValueForAKnownFilterIsARejection() {
        final var em = new RecordedEm();
        final var requested = new LinkedHashMap<String, String[]>();
        requested.put("username", null);

        final var ex = Assertions.assertThrows(BadRequestException.class,
                () -> USERS.create(em.proxy(), requested, List.of(), false, 0, 0, "realm-x"));

        Assertions.assertEquals(400, ex.getResponse().getStatus());
    }

    @Test
    public void aNullValueForAnUnknownFilterIsJustDropped() {
        final var em = new RecordedEm();
        final var requested = new LinkedHashMap<String, String[]>();
        requested.put("hacker", null);

        USERS.create(em.proxy(), requested, List.of(), false, 0, 0, "realm-x");

        Assertions.assertFalse(em.query.sql.contains("hacker"));
    }
    @Test
    public void aWindowWithoutASortGetsTheDefaultOrder() {
        // offset and limit over an unordered scan can repeat a row on two pages, or skip it
        final var em = new RecordedEm();

        ORDERED.create(em.proxy(), Map.of(), List.of(), false, 0, 25, "realm-x");

        Assertions.assertTrue(em.query.sql.contains("order by id"), em.query.sql);
    }

    @Test
    public void anExplicitSortWins() {
        final var em = new RecordedEm();

        ORDERED.create(em.proxy(), Map.of(), List.of("username,DESC"), false, 0, 25, "realm-x");

        Assertions.assertTrue(em.query.sql.contains("order by u.username DESC"), em.query.sql);
        Assertions.assertFalse(em.query.sql.contains("order by id"), em.query.sql);
    }

    @Test
    public void anUnwindowedQueryIsLeftUnordered() {
        // nothing is paged over, so imposing a sort would only cost time and change what callers see
        final var em = new RecordedEm();

        ORDERED.create(em.proxy(), Map.of(), List.of(), false, 0, 0, "realm-x");

        Assertions.assertFalse(em.query.sql.contains("order by"), em.query.sql);
    }
}
