package net.optionfactory.keycloak.providers.filtering;

import java.util.Arrays;
import net.optionfactory.keycloak.providers.filtering.TextFilter.CaseSensitivity;
import net.optionfactory.keycloak.providers.filtering.TextFilter.Operator;

public record AttributeFilter(String name, String alias) implements AllowedFilter {

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
                        String.format("jsonb_object_field_text(%s, ?) %s null", alias, operator == Operator.EQ ? "is" : "is not"),
                        field
                    );
                }
                yield new ConfiguredFilter(
                    String.format(sensitivity == CaseSensitivity.CASE_SENSITIVE ? "jsonb_object_field_text(%s, ?) %s ?" : "lower(jsonb_object_field_text(%s, ?)) %s ?", alias, operator.op),
                    field,
                    sensitivity == CaseSensitivity.CASE_SENSITIVE ? value : value.toLowerCase()
                );
            }
            case CONTAINS, STARTS_WITH, ENDS_WITH -> {
                Parsers.ensure(value != null, name(), "Operator %s expects a non-null value", operator);
                yield new ConfiguredFilter(
                    String.format("jsonb_object_field_text(%s, ?) %s ?", alias, sensitivity == CaseSensitivity.CASE_SENSITIVE ? "like" : "ilike"),
                    field,
                    TextFilter.likePattern(operator, value)
                );
            }
        };
    }

}
