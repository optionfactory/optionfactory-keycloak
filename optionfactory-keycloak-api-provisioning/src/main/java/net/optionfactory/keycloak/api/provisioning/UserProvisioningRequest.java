package net.optionfactory.keycloak.api.provisioning;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

public class UserProvisioningRequest {

    @NotEmpty
    public String id;
    @NotEmpty
    public String username;
    @NotEmpty
    public String firstName;
    @NotEmpty
    public String lastName;
    @NotNull
    public Map<String, List<String>> attributes;
    @NotNull
    public List<String> groups;
    @NotNull
    public List<String> requiredActions;
    public boolean enabled;
    public boolean emailVerified;

}
