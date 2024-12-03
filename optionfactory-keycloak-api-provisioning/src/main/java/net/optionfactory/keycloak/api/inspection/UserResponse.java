package net.optionfactory.keycloak.api.inspection;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.Instant;
import java.util.Map;

public class UserResponse {

    public String id;
    public String username;
    public String email;
    public String firstName;
    public String lastName;
    public boolean enabled;
    public boolean emailVerified;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    public Instant createdAt;
    public Map<String, String> groups;
    public Map<String, String> attributes;

}
