package net.optionfactory.keycloak.api.provisioning;

import java.util.List;
import java.util.Map;

public record UserProvisioningRequest(
        /*@NotEmpty*/ String id,
        /*@NotEmpty*/ String username,
        /*@NotEmpty*/ String email,
        /*@NotEmpty*/ String firstName,
        /*@NotEmpty*/ String lastName,
        /*@NotNull*/ Map<String, List<String>> attributes,
        /*@NotNull*/ List<String> groups,
        /*@NotNull*/ List<String> requiredActions,
        boolean enabled,
        boolean emailVerified) {

}
