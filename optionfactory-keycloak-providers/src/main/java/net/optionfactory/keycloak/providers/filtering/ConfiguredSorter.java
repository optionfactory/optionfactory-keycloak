package net.optionfactory.keycloak.providers.filtering;

public record ConfiguredSorter(String alias, Direction dir) {

    public enum Direction {
        ASC, DESC
    }

}
