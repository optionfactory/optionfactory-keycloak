package net.optionfactory.keycloak.providers.filtering;

import java.util.Arrays;

public record TextFilter(String name, String alias) implements AllowedFilter {

    public enum Operator {
        EQ("="), NEQ("<>"), LT("<"), GT(">"), LTE("<="), GTE(">="), CONTAINS("%%"), STARTS_WITH("%"), ENDS_WITH("%");
        public final String op;

        Operator(String op) {
            this.op = op;
        }
    }

    public enum CaseSensitivity {
        CASE_SENSITIVE, IGNORE_CASE
    }

    @Override
    public ConfiguredFilter configure(String[] values) {
        Parsers.ensure(values.length == 3, name(), "Expected 3 values: [OP,CASE,VALUE], got %s", Arrays.toString(values));
        final var operator = Parsers.enumeration(Operator.class, values[0], name());
        final var sensitivity = Parsers.enumeration(CaseSensitivity.class, values[1], name());
        final var value = values[2];
        return switch (operator) {
            case EQ, NEQ, LT, GT, LTE, GTE -> {
                if (value == null) {
                    Parsers.ensure(operator == Operator.EQ || operator == Operator.NEQ, name(), "Operator %s expects a non-null value", operator);
                    yield new ConfiguredFilter(String.format("%s %s null", alias, operator == Operator.EQ ? "is" : "is not"));
                }
                yield new ConfiguredFilter(String.format(sensitivity == CaseSensitivity.CASE_SENSITIVE ? "%s %s ?" : "lower(%s) %s ?", alias, operator.op), sensitivity == CaseSensitivity.CASE_SENSITIVE ? value : value.toLowerCase());
            }
            case CONTAINS, STARTS_WITH, ENDS_WITH -> {
                Parsers.ensure(value != null, name(), "Operator %s expects a non-null value", operator);
                yield new ConfiguredFilter(String.format("%s %s ?", alias, sensitivity == CaseSensitivity.CASE_SENSITIVE ? "like" : "ilike"), likePattern(operator, value));
            }
        };
    }

    public static String likePattern(Operator op, String value) {
        final java.lang.String esc = value.replace("%", "\\%").replace("_", "\\_");
        if (op == Operator.STARTS_WITH) {
            return esc + "%";
        }
        if (op == Operator.ENDS_WITH) {
            return "%" + esc;
        }
        return "%" + esc + "%";
    }

}
