package net.optionfactory.keycloak.providers.filtering;

import java.util.Arrays;

public record TimestampFilter(String name, String alias) implements AllowedFilter {

    public enum Operator {
        EQ("="), NEQ("<>"), LT("<"), GT(">"), LTE("<="), GTE(">=");
        public final String op;

        private Operator(String op) {
            this.op = op;
        }
    }

    @Override
    public ConfiguredFilter configure(String[] values) {
        Parsers.ensure(values.length == 2, name(), "Expected 3 values: [OP,VALUE], got %s", Arrays.toString(values));
        final var operator = Parsers.enumeration(Operator.class, values[0], name());
        final var value = Parsers.instant(values[1], name());
        if (value == null) {
            Parsers.ensure(operator == Operator.EQ || operator == Operator.NEQ, name(), "Operator %s expects a non-null value", operator);
            return new ConfiguredFilter(String.format("%s %s null", alias, operator == Operator.EQ ? "is" : "is not"));
        }
        return new ConfiguredFilter(String.format("%s %s ?", alias, operator.op), value);
    }

}
