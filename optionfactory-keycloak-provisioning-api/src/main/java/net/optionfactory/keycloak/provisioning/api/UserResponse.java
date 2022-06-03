package net.optionfactory.keycloak.provisioning.api;

import java.util.List;
import java.util.Map;

public class UserResponse {

    public String id;
    public String username;
    public String email;
    public String firstName;
    public String lastName;
    public boolean enabled;
    public boolean emailVerified;
    public long createdTimestamp;
    public List<String> groups;
    public Map<String, String> attributes;

}
