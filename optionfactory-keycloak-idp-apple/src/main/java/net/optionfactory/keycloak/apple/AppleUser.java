package net.optionfactory.keycloak.apple;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.keycloak.broker.provider.BrokeredIdentityContext;

@JsonIgnoreProperties(ignoreUnknown = true)
class AppleUser {

    public String email;
    public Name name;

    /// Apple posts this payload once, on the first authorization, and it is the only place a name is
    /// ever available: the id_token carries no name claims. It travels through the user agent unsigned,
    /// so it contributes the name and nothing else. The email is ignored on purpose: the id_token's is
    /// signature-verified, while an address arriving this way would inherit that token's verified status
    /// (`setEmail` does not touch the `EMAIL_VERIFIED` context data) or, where the token carries no such
    /// claim, be marked verified outright by `trustEmail`.
    public void applyTo(BrokeredIdentityContext context) {
        if (name == null) {
            return;
        }
        if (name.firstName != null) {
            context.setFirstName(name.firstName);
        }
        if (name.lastName != null) {
            context.setLastName(name.lastName);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Name {

        public String firstName;
        public String lastName;
    }

}
