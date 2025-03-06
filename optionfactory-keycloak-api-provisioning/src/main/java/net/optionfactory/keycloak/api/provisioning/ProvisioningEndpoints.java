package net.optionfactory.keycloak.api.provisioning;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import net.optionfactory.keycloak.providers.validation.RequestValidator;
import org.keycloak.Config;
import org.keycloak.models.GroupModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserProvider;
import org.keycloak.services.ServicesLogger;
import org.keycloak.services.resources.admin.AdminEventBuilder;
import org.keycloak.services.resources.admin.ext.AdminRealmResourceProvider;
import org.keycloak.services.resources.admin.ext.AdminRealmResourceProviderFactory;
import org.keycloak.services.resources.admin.permissions.AdminPermissionEvaluator;

/**
 * @see org.keycloak.services.resources.admin.UserResource }
 * @author rferranti
 */
public class ProvisioningEndpoints {

    private final ServicesLogger logger = ServicesLogger.LOGGER;
    private final RequestValidator validator;
    private final KeycloakSession session;

    public ProvisioningEndpoints(RequestValidator validator, KeycloakSession session) {
        this.validator = validator;
        this.session = session;
    }

    @DELETE
    @Path("/users")
    @Consumes(MediaType.APPLICATION_JSON)
    // mapped to be http://localhost:8080/admin/realms/{realm}/provisioning/users
    public void wipe(List<String> ids) {
        validator.enforce(ids);
        final RealmModel realm = session.getContext().getRealm();
        final UserProvider users = session.users();
        for (String id : ids) {
            final UserModel user = users.getUserById(realm, id);
            if (user != null) {
                users.removeUser(realm, user);
            }
        }
    }

    @PUT
    @Path("/users")
    @Consumes(MediaType.APPLICATION_JSON)
    // mapped to be http://localhost:8080/admin/realms/{realm}/provisioning/users
    public void provide(UserProvisioningRequest req) {
        validator.enforce(req);

        final RealmModel realm = session.getContext().getRealm();
        final UserProvider users = session.users();

        final UserModel user = Optional.ofNullable(users.getUserById(realm, req.id))
                .orElseGet(() -> users.addUser(realm, req.id, req.username, true, true));

        user.setFirstName(req.firstName);
        user.setLastName(req.lastName);
        user.setEnabled(req.enabled);
        user.setEmail(req.username);
        user.setEmailVerified(req.emailVerified);
        for (Map.Entry<String, List<String>> entry : req.attributes.entrySet()) {
            user.setAttribute(entry.getKey(), entry.getValue());
        }
        for (String requiredAction : req.requiredActions) {
            user.addRequiredAction(requiredAction);
        }
        for (String groupName : req.groups) {
            final GroupModel group = session.groups()
                    .getGroupsStream(realm)
                    .filter(g -> g.getName().equals(groupName))
                    .findFirst()
                    .orElseGet(() -> session.groups().createGroup(realm, groupName));
            user.joinGroup(group);
        }
    }

    public static class Factory implements AdminRealmResourceProviderFactory {

        @Override
        public AdminRealmResourceProvider create(KeycloakSession session) {
            final var validator = session.getProvider(RequestValidator.class);
            return new AdminRealmResourceProvider() {

                @Override
                public Object getResource(KeycloakSession ks, RealmModel rm, AdminPermissionEvaluator ape, AdminEventBuilder aeb) {
                    ape.users().requireManage();
                    return new ProvisioningEndpoints(validator, ks);
                }

                @Override
                public void close() {

                }
            };
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
            return "provisioning";
        }

    }

}
