package net.optionfactory.keycloak.providers.filtering;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record GroupFilter(String name, String alias) implements AllowedFilter {

    public enum Operator {
        ANY("jsonb_exists_any"),
        ALL("jsonb_exists_all"),
        NONE("not jsonb_exists_any");
        public final String fn;

        private Operator(String fn) {
            this.fn = fn;
        }

    }

    @Override
    public ConfiguredFilter configure(String[] values) {
        Parsers.ensure(values.length >= 2, name(), "Expected at least 2 values: [OP,...VALUES], got %s", Arrays.toString(values));
        final var operator = Parsers.enumeration(Operator.class, values[0], name());
        final var params = Arrays.copyOfRange(values, 1, values.length);
        // ?| array['b', 'd'] (any) jsonb_exists_any
        // ?& array['a', 'b'] (all) jsonb_exists_all
        return new ConfiguredFilter(
                String.format("%s(%s, array[%s])", operator.fn, alias, arrayPlaceholders(params)),
                (Object[]) params);
    }

    private static String arrayPlaceholders(Object[] params) {
        return Stream.of(params).map(p -> "?").collect(Collectors.joining(","));
    }

}
