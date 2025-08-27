package net.optionfactory.keycloak.api.provisioning;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.optionfactory.keycloak.api.provisioning.UserPatchRequest.PatchMode;

public record Patch<T>(List<T> toBeRemoved, List<T> toBeAdded) {

    public static <T> Patch<T> of(PatchMode mode, List<T> actual, List<T> desired) {
        final var d = new Patch<T>(new ArrayList<>(), new ArrayList<>());
        if (mode == PatchMode.REMOVE) {
            for (final var item : desired) {
                if (actual.contains(item)) {
                    d.toBeRemoved().add(item);
                }
            }
            return d;
        }
        if (mode == PatchMode.REPLACE) {
            for (final var group : actual) {
                if (!desired.contains(group)) {
                    d.toBeRemoved().add(group);
                }
            }
        }
        for (final var group : desired) {
            if (!actual.contains(group)) {
                d.toBeAdded().add(group);
            }
        }
        return d;
    }

    public static <K, V> Patch<Map.Entry<K, V>> ofMap(PatchMode mode, Map<K, V> actual, Map<K, V> desired) {
        final var d = new Patch<Map.Entry<K, V>>(new ArrayList<>(), new ArrayList<>());
        if (mode == PatchMode.REMOVE) {
            for (final var item : desired.entrySet()) {
                if (actual.containsKey(item.getKey())) {
                    d.toBeRemoved().add(item);
                }
            }
            return d;
        }
        if (mode == PatchMode.REPLACE) {
            for (final var item : actual.entrySet()) {
                if (!desired.containsKey(item.getKey())) {
                    d.toBeRemoved().add(item);
                }
            }
        }
        for (final var item : desired.entrySet()) {
            //unconditionally as value may be different
            d.toBeAdded().add(item);
        }
        return d;
    }
}
