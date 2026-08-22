package net.optionfactory.keycloak.api.provisioning;

import org.junit.jupiter.api.Assertions;
import java.util.List;
import java.util.Map;
import net.optionfactory.keycloak.api.provisioning.UserPatchRequest.PatchMode;
import org.junit.jupiter.api.Test;

public class PatchTest {

    @Test
    public void appendOnlyAddsMissingItems() {
        final var p = Patch.of(PatchMode.APPEND, List.of("a", "b"), List.of("b", "c"));
        Assertions.assertEquals(List.of(), p.toBeRemoved());
        Assertions.assertEquals(List.of("c"), p.toBeAdded());
    }

    @Test
    public void removeOnlyRemovesExistingItems() {
        final var p = Patch.of(PatchMode.REMOVE, List.of("a", "b"), List.of("b", "c"));
        Assertions.assertEquals(List.of("b"), p.toBeRemoved());
        Assertions.assertEquals(List.of(), p.toBeAdded());
    }

    @Test
    public void replaceRemovesTheMissingAndAddsTheNew() {
        final var p = Patch.of(PatchMode.REPLACE, List.of("a", "b"), List.of("b", "c"));
        Assertions.assertEquals(List.of("a"), p.toBeRemoved());
        Assertions.assertEquals(List.of("c"), p.toBeAdded());
    }

    @Test
    public void replaceToIdenticalDesiredStateIsANoOp() {
        final var p = Patch.of(PatchMode.REPLACE, List.of("a", "b"), List.of("b", "a"));
        Assertions.assertEquals(List.of(), p.toBeRemoved());
        Assertions.assertEquals(List.of(), p.toBeAdded());
    }

    @Test
    public void mapRemoveDropsOnlyKeysPresentInActual() {
        final var p = Patch.ofMap(PatchMode.REMOVE, Map.of("a", "1", "b", "2"), Map.of("b", "ignored", "c", "ignored"));
        Assertions.assertEquals(List.of(Map.entry("b", "ignored")), p.toBeRemoved());
        Assertions.assertEquals(List.of(), p.toBeAdded());
    }

    @Test
    public void mapReplaceRemovesMissingKeysAndReaddsEveryDesiredEntry() {
        final var p = Patch.ofMap(PatchMode.REPLACE, Map.of("a", "1", "b", "2"), Map.of("b", "3"));
        Assertions.assertEquals(List.of(Map.entry("a", "1")), p.toBeRemoved());
        // every desired entry is re-set, even when the key already exists, as the value may differ
        Assertions.assertEquals(List.of(Map.entry("b", "3")), p.toBeAdded());
    }

    @Test
    public void mapAppendReaddsEveryDesiredEntry() {
        final var p = Patch.ofMap(PatchMode.APPEND, Map.of("a", "1"), Map.of("a", "2"));
        Assertions.assertEquals(List.of(), p.toBeRemoved());
        Assertions.assertEquals(List.of(Map.entry("a", "2")), p.toBeAdded());
    }
}
