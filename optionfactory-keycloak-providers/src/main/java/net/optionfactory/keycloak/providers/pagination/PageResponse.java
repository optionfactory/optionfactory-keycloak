package net.optionfactory.keycloak.providers.pagination;

import java.util.List;

public record PageResponse<T>(List<T> data, int size) {

}
