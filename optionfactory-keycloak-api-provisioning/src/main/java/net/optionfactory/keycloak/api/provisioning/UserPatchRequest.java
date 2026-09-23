package net.optionfactory.keycloak.api.provisioning;

import jakarta.annotation.Nullable;
import java.util.List;
import java.util.Map;

public record UserPatchRequest(
        /*@NotEmpty*/ String id,
        @Nullable String username,
        @Nullable String email,
        @Nullable String firstName,
        @Nullable String lastName,
        @Nullable PatchMode attributesPatchMode,
        @Nullable Map<String, List<String>> attributes,
        @Nullable PatchMode groupsPatchMode,
        @Nullable List<String> groups,
        @Nullable PatchMode requiredActionsPatchMode,
        @Nullable List<String> requiredActions,
        @Nullable PasswordRequest password,
        @Nullable Boolean enabled,
        @Nullable Boolean emailVerified) {

    public enum PatchMode {
        APPEND, REPLACE, REMOVE;
    }
}
