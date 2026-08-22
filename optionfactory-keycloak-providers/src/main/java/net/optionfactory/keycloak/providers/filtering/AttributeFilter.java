package net.optionfactory.keycloak.providers.filtering;

import java.util.Arrays;
import java.util.Locale;
import net.optionfactory.keycloak.providers.filtering.TextFilter.CaseSensitivity;
import net.optionfactory.keycloak.providers.filtering.TextFilter.Operator;

public record AttributeFilter(String name) implements AllowedFilter {

    @Override
    public ConfiguredFilter configure(String[] values) {
        Parsers.ensure(values.length == 4, name(), "Expected 4 values: [OP,CASE,FIELD,VALUE], got %s", Arrays.toString(values));
        final var operator = Parsers.enumeration(Operator.class, values[0], name());
        final var sensitivity = Parsers.enumeration(CaseSensitivity.class, values[1], name());
        final var field = values[2];
        final var value = values[3];

        return switch (operator) {
            case EQ, NEQ, LT, GT, LTE, GTE -> {
                if (value == null) {
                    Parsers.ensure(operator == Operator.EQ || operator == Operator.NEQ, name(), "Operator %s expects a non-null value", operator);
                    yield new ConfiguredFilter(
                    subselect(String.format("user_id = u.id and name = ? and coalesce(long_value, value) %s null", operator == Operator.EQ ? "is" : "is not")),
                    field
                    );
                }
                yield new ConfiguredFilter(
                subselect(String.format(sensitivity == CaseSensitivity.CASE_SENSITIVE
                ? "user_id = u.id and name = ? and coalesce(long_value, value) %s ?"
                : "user_id = u.id and name = ? and lower(coalesce(long_value, value)) %s ?", operator.op)
                ),
                field,
                sensitivity == CaseSensitivity.CASE_SENSITIVE ? value : value.toLowerCase(Locale.ROOT)
                );
            }
            case CONTAINS, STARTS_WITH, ENDS_WITH -> {
                Parsers.ensure(value != null, name(), "Operator %s expects a non-null value", operator);
                yield new ConfiguredFilter(
                subselect(String.format("name = ? and coalesce(long_value, value) %s ?", sensitivity == CaseSensitivity.CASE_SENSITIVE ? "like" : "ilike")),
                field,
                TextFilter.likePattern(operator, value)
                );
            }
        };
    }

    private static String subselect(String conditions) {
        return String.format("id in (select user_id from user_attribute where %s )", conditions);
    }
}
