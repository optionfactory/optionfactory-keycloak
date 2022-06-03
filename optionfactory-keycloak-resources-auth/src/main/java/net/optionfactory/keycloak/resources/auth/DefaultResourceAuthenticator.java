package net.optionfactory.keycloak.resources.auth;

import java.util.stream.Stream;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotAuthorizedException;
import org.keycloak.models.KeycloakSession;
import org.keycloak.services.managers.AppAuthManager.BearerTokenAuthenticator;
import org.keycloak.services.managers.AuthenticationManager.AuthResult;

public class DefaultResourceAuthenticator implements ResourceAuthenticator {

    private final KeycloakSession session;

    public DefaultResourceAuthenticator(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public void enforceScope(String scope) {
        final AuthResult auth = new BearerTokenAuthenticator(session).authenticate();
        if (auth == null) {
            throw new NotAuthorizedException("Bearer");
        }
        if (Stream.of(auth.getToken().getScope().split(" ")).noneMatch(scope::equals)) {
            throw new ForbiddenException(String.format("Client does not have required scope '%s'", scope));
        }
    }

}
