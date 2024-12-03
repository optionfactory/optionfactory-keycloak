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

    public QueryBuilder(String template) {
        this.template = template;
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
            int offset,
            int limit,
            Object... params) {

        final var filters = requestedFilters.entrySet()
                .stream()
                .filter(e -> allowedFilters.containsKey(e.getKey()))
                .map(e -> allowedFilters.get(e.getKey()).configure(e.getValue()))
                .toList();

        final var conditions = (filters.isEmpty() ? "" : "and ") + filters.stream()
                .map(ConfiguredFilter::expression)
                .collect(Collectors.joining(" and "));

        final var sorters = Parsers.sorters(allowedSorters, requestedSorters);

        final var orderClause = sorters.isEmpty() ? "" : String.format("order by %s", sorters.stream().map(s -> String.format("%s %s", s.alias(), s.dir())).collect(Collectors.joining(",")));

        final var qs = template
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
