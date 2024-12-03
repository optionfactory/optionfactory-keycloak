package net.optionfactory.keycloak.providers.filtering;

public interface AllowedFilter {

    String name();

    String alias();

    ConfiguredFilter configure(String[] values);

    public record ConfiguredFilter(String expression, Object... parameters) {

    }

}
