package net.optionfactory.keycloak.providers.pagination;

import java.util.List;

public record SliceResponse<T>(List<T> data, boolean last) {

}
