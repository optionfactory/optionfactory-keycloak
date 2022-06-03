package net.optionfactory.keycloak.provisioning.api;

import java.util.List;

public class PageResponse<T> {

    public List<T> data;
    public int size;

    public static <T> PageResponse<T> of(List<T> data, int size) {
        final net.optionfactory.keycloak.provisioning.api.PageResponse<T> p = new PageResponse<T>();
        p.data = data;
        p.size = size;
        return p;
    }

}
