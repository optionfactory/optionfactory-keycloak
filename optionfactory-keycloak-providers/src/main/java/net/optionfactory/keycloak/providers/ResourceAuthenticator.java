package net.optionfactory.keycloak.providers;

import java.util.stream.Stream;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotAuthorizedException;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.utils.RoleUtils;
import org.keycloak.services.managers.AppAuthManager.BearerTokenAuthenticator;

public interface ResourceAuthenticator {

    public static void enforceScope(KeycloakSession session, String scope) {
        final var auth = new BearerTokenAuthenticator(session).authenticate();
        authenticated(auth != null, "Bearer");
        final var hasScope = Stream.of(auth.token().getScope().split(" ")).anyMatch(scope::equals);
        authorized(hasScope, "Client does not have required scope '%s'", scope);
    }

    public static void enforceServiceAccountHasClientRole(KeycloakSession session, String clientName, String roleName) {
        final var auth = new BearerTokenAuthenticator(session).authenticate();
        authenticated(auth != null, "Bearer");
        final var client = auth.client();
        authorized(client != null, "Session context has no associated client");
        final var sa = session.users().getServiceAccount(client);
        authorized(sa != null, "Client does not have an associated service account");
        final var roleClient = session.clients().getClientByClientId(session.getContext().getRealm(), clientName);
        authorized(roleClient != null, "Required client '%s' does not exist", clientName);
        final var role = session.roles().getClientRole(roleClient, roleName);
        authorized(role != null, "Required realm role '%s' does not exist", roleName);
        final var hasRole = RoleUtils.hasRole(sa.getClientRoleMappingsStream(roleClient), role);
        authorized(hasRole, "Service account does not have required realm role '%s'", roleName);
    }

    public static void authenticated(boolean test, String message, Object... args) {
        if (test) {
            return;
        }
        throw new NotAuthorizedException(String.format(message, args));
    }

    public static void authorized(boolean test, String message, Object... args) {
        if (test) {
            return;
        }
        throw new ForbiddenException(String.format(message, args));
    }
}
