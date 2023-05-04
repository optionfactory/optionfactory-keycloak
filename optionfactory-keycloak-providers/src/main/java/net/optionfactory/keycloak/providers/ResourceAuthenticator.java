package net.optionfactory.keycloak.providers;

import java.util.stream.Stream;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotAuthorizedException;
import org.keycloak.models.KeycloakSession;
import org.keycloak.services.managers.AppAuthManager.BearerTokenAuthenticator;
import org.keycloak.services.managers.AuthenticationManager.AuthResult;

public interface ResourceAuthenticator {

    public static void enforceScope(KeycloakSession session, String scope) {
        final AuthResult auth = new BearerTokenAuthenticator(session).authenticate();
        if (auth == null) {
            throw new NotAuthorizedException("Bearer");
        }
        if (Stream.of(auth.getToken().getScope().split(" ")).noneMatch(scope::equals)) {
            throw new ForbiddenException(String.format("Client does not have required scope '%s'", scope));
        }
    }

}
