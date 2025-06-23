package net.optionfactory.keycloak.authenticators;

import jakarta.ws.rs.core.Response;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.events.Errors;

public class Authenticators {

    public static void accessDenied(AuthenticationFlowContext context, String messageKey) {
        context.getEvent().error(Errors.ACCESS_DENIED);
        final var challenge = context.form()
                .setError(messageKey)
                .createErrorPage(Response.Status.UNAUTHORIZED);
        context.failure(AuthenticationFlowError.ACCESS_DENIED, challenge);
    }

}
