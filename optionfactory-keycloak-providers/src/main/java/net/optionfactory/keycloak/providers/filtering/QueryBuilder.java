package net.optionfactory.keycloak.providers.filtering;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.optionfactory.keycloak.providers.filtering.AllowedFilter.ConfiguredFilter;

public class QueryBuilder {

    private final Map<String, AllowedFilter> allowedFilters = new HashMap<>();
    private final Map<String, String> allowedSorters = new HashMap<>();
    private final String template;
    private String defaultOrder;

    public QueryBuilder(String template) {
        this.template = template;
    }

    /// The order to impose when a caller asks for a window without saying how to sort it. Offset and limit
    /// over an unordered scan are free to return a row on two pages, or on none: postgres makes no promise
    /// about the order of rows it has not been asked to sort.
    public QueryBuilder orderedBy(String alias) {
        this.defaultOrder = alias;
        return this;
    }

    public QueryBuilder filter(AllowedFilter allowedFilter) {
        this.allowedFilters.put(allowedFilter.name(), allowedFilter);
        return this;
    }

    public QueryBuilder sorter(String name, String alias) {
        this.allowedSorters.put(name, alias);
        return this;
    }

    public Query create(
            EntityManager em,
            Map<String, String[]> requestedFilters,
            List<String> requestedSorters,
            boolean countOverTotal,
            int offset,
            int limit,
            Object... params) {

        // an absent body and a null value for a known filter are both malformed requests, not failures:
        // every filter reads values[0] straight away, so they would surface as a 500
        final var filters = (requestedFilters == null ? Map.<String, String[]>of() : requestedFilters).entrySet()
                .stream()
                .filter(e -> allowedFilters.containsKey(e.getKey()))
                .map(e -> {
                    Parsers.ensure(e.getValue() != null, e.getKey(), "must not be null");
                    return allowedFilters.get(e.getKey()).configure(e.getValue());
                })
                .toList();

        final var conditions = (filters.isEmpty() ? "" : "and ") + filters.stream()
                .map(ConfiguredFilter::expression)
                .collect(Collectors.joining(" and "));

        final var sorters = Parsers.sorters(allowedSorters, requestedSorters);

        final var paginated = offset != 0 || limit != 0;
        final var orderClause = sorters.isEmpty()
                ? (paginated && defaultOrder != null ? String.format("order by %s", defaultOrder) : "")
                : String.format("order by %s", sorters.stream().map(s -> String.format("%s %s", s.alias(), s.dir())).collect(Collectors.joining(",")));

        final var qs = template
                .replace("{COUNT_OVER_TOTAL}", countOverTotal ? ", count(*) over() as total" : "")
                .replace("{CONDITIONS}", conditions)
                .replace("{ORDER_CLAUSE}", orderClause);

        final var query = em.createNativeQuery(qs);

        int pi = 0;
        for (final var param : params) {
            query.setParameter(++pi, param);
        }
        for (final var filter : filters) {
            for (final var parameter : filter.parameters()) {
                query.setParameter(++pi, parameter);
            }
        }
        if (offset != 0) {
            query.setFirstResult(offset);
        }
        if (limit != 0) {
            query.setMaxResults(limit);
        }
        return query;
    }

}
