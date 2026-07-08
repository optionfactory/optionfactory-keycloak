package net.optionfactory.keycloak.ldap;

import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.ldap.LDAPStorageProvider;

public class ResetLockoutTimeOnUpdateCredentials implements EventListenerProvider {

    private static final Logger log = Logger.getLogger(ResetLockoutTimeOnUpdateCredentials.class.getName());
    private final KeycloakSession session;

    public ResetLockoutTimeOnUpdateCredentials(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public void onEvent(Event event) {
        if (EventType.UPDATE_CREDENTIAL != event.getType()) {
            return;
        }
        final var details = event.getDetails();
        final var credentialType = details != null ? details.get("credential_type") : null;
        if (!PasswordCredentialModel.TYPE.equals(credentialType)) {
            return;
        }
        resetLockoutTime(session, event.getRealmId(), event.getUserId());
    }

    @Override
    public void onEvent(AdminEvent adminEvent, boolean includeRepresentation) {

    }

    public static void resetLockoutTime(KeycloakSession session, String realmId, String userId) {
        final var realm = session.realms().getRealm(realmId);
        final var user = session.users().getUserById(realm, userId);
        if (user == null || user.getFederationLink() == null) {
            return;
        }
        final var component = realm.getComponent(user.getFederationLink());
        if (component == null) {
            return;
        }
        final var userStorage = session.getProvider(UserStorageProvider.class, component);
        if (!(userStorage instanceof LDAPStorageProvider ldap)) {
            return;
        }
        log.infof("Password update detected for user with an ldap user storage: %s", user.getUsername());
        try {
            final var ldapUser = ldap.loadLDAPUserByUsername(realm, user.getUsername());
            if (ldapUser == null) {
                log.warnf("User not found on ldap: %s", user.getUsername());
                return;
            }
            ldapUser.setSingleAttribute("lockoutTime", "0");
            ldap.getLdapIdentityStore().update(ldapUser);
            log.infof("Successfully reset lockoutTime for AD user: %s", user.getUsername());
        } catch (Exception e) {
            log.errorf(e, "Failed to unlock AD account for user %s", user.getUsername());
        }
    }

    @Override
    public void close() {
    }

    public static class Factory implements EventListenerProviderFactory {

        private static final String ID = "reset-lockout-time-on-update-credentials";

        @Override
        public EventListenerProvider create(KeycloakSession session) {
            return new ResetLockoutTimeOnUpdateCredentials(session);
        }

        @Override
        public void init(Config.Scope config) {

        }

        @Override
        public void postInit(KeycloakSessionFactory factory) {

        }

        @Override
        public void close() {

        }

        @Override
        public String getId() {
            return ID;
        }
    }
}
