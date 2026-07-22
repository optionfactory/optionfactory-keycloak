package net.optionfactory.keycloak.onlineaccess;

import org.keycloak.events.Errors;
import org.keycloak.events.EventType;
import org.keycloak.services.messages.Messages;
import org.keycloak.sessions.AuthenticationSessionModel;

import jakarta.ws.rs.core.Response;
import java.net.URI;
import net.optionfactory.keycloak.providers.Conf;
import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.authentication.AuthenticationProcessor;
import org.keycloak.authentication.actiontoken.ActionTokenContext;
import org.keycloak.authentication.actiontoken.ActionTokenHandler;
import org.keycloak.authentication.actiontoken.ActionTokenHandlerFactory;
import org.keycloak.common.util.Time;
import org.keycloak.events.Details;
import org.keycloak.events.EventBuilder;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.services.managers.AuthenticationManager;

public class OnlineAccessActionTokenHandler implements ActionTokenHandler<OnlineAccessActionToken> {

    @Override
    public AuthenticationSessionModel startFreshAuthenticationSession(OnlineAccessActionToken token, ActionTokenContext<OnlineAccessActionToken> context) {
        return context.createAuthenticationSessionForClient(token.getIssuedFor());
    }

    @Override
    public String getAuthenticationSessionIdFromToken(OnlineAccessActionToken token, ActionTokenContext<OnlineAccessActionToken> tokenContext, AuthenticationSessionModel currentAuthSession) {
        return token.getCompoundAuthenticationSessionId();
    }

    @Override
    public boolean canUseTokenRepeatedly(OnlineAccessActionToken token, ActionTokenContext<OnlineAccessActionToken> context) {
        return false;
    }

    @Override
    public Response handleToken(OnlineAccessActionToken token, ActionTokenContext<OnlineAccessActionToken> context) {
        final var uriInfo = context.getUriInfo();
        final var realm = context.getRealm();
        final var session = context.getSession();
        final var eventBuilder = new EventBuilder(realm, session);
        final var connection = session.getContext().getConnection();
        final var userSession = session.getContext().getUserSession();
        final var authenticationSession = context.getAuthenticationSession();
        authenticationSession.setRedirectUri(token.getRedirectUri());

        final var clientSessionContext = AuthenticationProcessor.attachSession(authenticationSession, userSession, context.getSession(), context.getRealm(), connection, eventBuilder);
        final var maybeNewUserSession = clientSessionContext.getClientSession().getUserSession();

        maybeNewUserSession.setNote(AuthenticationManager.AUTH_TIME, String.valueOf(Time.currentTimeSeconds()));

        eventBuilder.event(EventType.LOGIN)
                .user(maybeNewUserSession.getUser())
                .session(maybeNewUserSession)
                .detail(Details.AUTH_METHOD, "online_access_token")
                .success();

        AuthenticationManager.createLoginCookie(session, realm, maybeNewUserSession.getUser(), maybeNewUserSession, uriInfo, connection);

        session.singleUseObjects().put(token.serializeKey(), token.getExp() - Time.currentTimeSeconds(), null);

        if (authenticationSession.getParentSession() != null) {
            session.authenticationSessions().removeRootAuthenticationSession(realm, authenticationSession.getParentSession());
        }
        return Response.status(Response.Status.FOUND)
                .location(URI.create(token.getRedirectUri()))
                .build();
    }

    @Override
    public Class<OnlineAccessActionToken> getTokenClass() {
        return OnlineAccessActionToken.class;
    }

    @Override
    public EventType eventType() {
        return EventType.EXECUTE_ACTION_TOKEN;
    }

    @Override
    public String getDefaultEventError() {
        return Errors.NOT_ALLOWED;
    }

    @Override
    public String getDefaultErrorMessage() {
        return Messages.INVALID_CODE;
    }

    @Override
    public void close() {

    }

    public static class Factory implements ActionTokenHandlerFactory<OnlineAccessActionToken> {

        private boolean enabled;

        private static final Logger logger = Logger.getLogger(Factory.class);

        @Override
        public ActionTokenHandler<OnlineAccessActionToken> create(KeycloakSession session) {
            return enabled ? new OnlineAccessActionTokenHandler() : null;

        }

        @Override
        public void init(Config.Scope ignored) {
            final var config = Conf.fromPrefix("online-access-action-token-handler", "online-access");
            this.enabled = config.bool("enabled", false);
            logger.infof("online-access(action-token-handler) initialized: %s", this.enabled ? "enabled" : "disabled");
        }

        @Override
        public void postInit(KeycloakSessionFactory factory) {

        }

        @Override
        public void close() {

        }

        @Override
        public String getId() {
            return OnlineAccessActionToken.TOKEN_TYPE;
        }

    }
}
