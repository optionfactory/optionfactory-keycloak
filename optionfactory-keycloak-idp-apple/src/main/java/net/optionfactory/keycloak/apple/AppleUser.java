package net.optionfactory.keycloak.apple;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
class AppleUser {

    public String email;
    public Name name;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Name {

        public String firstName;
        public String lastName;
    }

}
