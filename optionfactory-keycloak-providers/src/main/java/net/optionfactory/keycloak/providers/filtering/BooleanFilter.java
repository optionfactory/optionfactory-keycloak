package net.optionfactory.keycloak.providers.filtering;

import java.util.Arrays;

public record BooleanFilter(String name, String alias) implements AllowedFilter {

    public enum Operator {
        EQ("="), NEQ("<>");
        public final String op;

        private Operator(String op) {
            this.op = op;
        }
    }

    @Override
    public ConfiguredFilter configure(String[] values) {
        Parsers.ensure(values.length == 2, name(), "Expected 2 values: [OP,VALUE], got %s", Arrays.toString(values));
        final var operator = Parsers.enumeration(Operator.class, values[0], name());
        final var value = values[1];
        if (value == null) {
            return new ConfiguredFilter(String.format("%s %s null", alias, operator == Operator.EQ ? "is" : "is not"));
        }
        Parsers.ensure("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value), name(), "Expected a boolean value (true/false), got: '%s'", value);
        return new ConfiguredFilter(String.format("%s %s ?", alias, operator.op), Boolean.parseBoolean(value));
    }

}
