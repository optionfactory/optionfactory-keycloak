package net.optionfactory.keycloak.api.provisioning;

import jakarta.ws.rs.BadRequestException;
import net.optionfactory.keycloak.providers.groups.Groups;
import java.util.List;
import java.util.Optional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import java.util.ArrayList;
import net.optionfactory.keycloak.api.provisioning.UserPatchRequest.PatchMode;
import net.optionfactory.keycloak.providers.model.Models;
import net.optionfactory.keycloak.providers.validation.Problem;
import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserProvider;
import org.keycloak.models.utils.ModelToRepresentation;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.services.ErrorResponse;
import org.keycloak.services.resources.admin.AdminEventBuilder;
import org.keycloak.services.resources.admin.ext.AdminRealmResourceProvider;
import org.keycloak.services.resources.admin.ext.AdminRealmResourceProviderFactory;
import org.keycloak.services.resources.admin.fgap.AdminPermissionEvaluator;

/**
 * @see org.keycloak.services.resources.admin.UserResource }
 * @author rferranti
 */
public class ProvisioningEndpoints {

    private final KeycloakSession session;

    public ProvisioningEndpoints(KeycloakSession session) {
        this.session = session;
    }

    @DELETE
    @Path("/users")
    @Consumes(MediaType.APPLICATION_JSON)
    // mapped to be http://localhost:8080/admin/realms/{realm}/provisioning/users
    public void wipe(List<String> ids) {
        if (ids == null) {
            final var response = Response.status(Response.Status.BAD_REQUEST)
                    .type("application/failures+json")
                    .entity(List.of(new Problem("FIELD_ERROR", "ids", "must not be null")))
                    .build();
            throw new BadRequestException(response);
        }

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
        final var problems = new ArrayList<>();
        if (req.id() == null || req.id().isBlank()) {
            problems.add(new Problem("FIELD_ERROR", "id", "must not be blank"));
        }
        if (req.email() == null || req.email().isBlank()) {
            problems.add(new Problem("FIELD_ERROR", "email", "must not be blank"));
        }
        if (req.firstName() == null || req.firstName().isBlank()) {
            problems.add(new Problem("FIELD_ERROR", "firstName", "must not be blank"));
        }
        if (req.lastName() == null || req.lastName().isBlank()) {
            problems.add(new Problem("FIELD_ERROR", "lastName", "must not be blank"));
        }
        if (req.attributes() == null) {
            problems.add(new Problem("FIELD_ERROR", "attributes", "must not be null"));
        }
        if (req.groups() == null) {
            problems.add(new Problem("FIELD_ERROR", "groups", "Campo obbligatorio"));
        }
        if (req.requiredActions() == null) {
            problems.add(new Problem("FIELD_ERROR", "requiredActions", "must not be null"));
        }
        if (!problems.isEmpty()) {
            final var response = Response.status(Response.Status.BAD_REQUEST)
                    .type("application/failures+json")
                    .entity(problems)
                    .build();
            throw new BadRequestException(response);

        }
        final RealmModel realm = session.getContext().getRealm();
        final UserProvider users = session.users();

        final UserModel user = Optional.ofNullable(users.getUserById(realm, req.id()))
                .orElseGet(() -> users.addUser(realm, req.id(), req.username(), true, true));

        user.setFirstName(req.firstName());
        user.setLastName(req.lastName());
        user.setEnabled(req.enabled());
        user.setEmail(req.email());
        user.setEmailVerified(req.emailVerified());
        for (final var entry : req.attributes().entrySet()) {
            user.setAttribute(entry.getKey(), entry.getValue());
        }
        for (final var requiredAction : req.requiredActions()) {
            user.addRequiredAction(requiredAction);
        }
        for (final var groupName : req.groups()) {
            user.joinGroup(Groups.provide(session, realm, groupName));
        }
    }

    @PATCH
    @Path("/users")
    @Consumes(MediaType.APPLICATION_JSON)
    // mapped to be http://localhost:8080/admin/realms/{realm}/provisioning/users
    public void patch(UserPatchRequest req) {
        if (req.id() == null || req.id().isBlank()) {
            final var response = Response.status(Response.Status.BAD_REQUEST)
                    .type("application/failures+json")
                    .entity(List.of(new Problem("FIELD_ERROR", "id", "must not be blank")))
                    .build();
            throw new BadRequestException(response);
        }
        final RealmModel realm = session.getContext().getRealm();
        final UserProvider users = session.users();

        final UserModel user = Optional.ofNullable(users.getUserById(realm, req.id()))
                .orElseThrow(() -> ErrorResponse.error("Unknown user", Status.BAD_REQUEST));

        if (req.firstName() != null) {
            user.setFirstName(req.firstName());
        }
        if (req.lastName() != null) {
            user.setLastName(req.lastName());
        }
        if (req.enabled() != null) {
            user.setEnabled(req.enabled());
        }
        if (req.username() != null) {
            user.setUsername(req.username());
        }
        if (req.email() != null) {
            user.setEmail(req.email());
        }
        if (req.emailVerified() != null) {
            user.setEmailVerified(req.emailVerified());
        }
        if (req.attributes() != null) {
            final var mode = req.attributesPatchMode() == null ? PatchMode.REPLACE : req.attributesPatchMode();
            final var actual = user.getAttributes();
            final var desired = req.attributes();
            final var attributes = Patch.ofMap(mode, actual, desired);

            for (final var entry : attributes.toBeRemoved()) {
                user.removeAttribute(entry.getKey());
            }
            for (final var entry : attributes.toBeAdded()) {
                user.setAttribute(entry.getKey(), entry.getValue());
            }
        }
        if (req.requiredActions() != null) {
            final var mode = req.requiredActionsPatchMode() == null ? PatchMode.REPLACE : req.requiredActionsPatchMode();
            final var actual = Models.requiredActions(user);
            final var desired = req.requiredActions();
            final var actions = Patch.of(mode, actual, desired);
            for (final var item : actions.toBeRemoved()) {
                user.removeRequiredAction(item);
            }
            for (String item : actions.toBeAdded()) {
                user.addRequiredAction(item);
            }
        }
        if (req.groups() != null) {
            final var mode = req.groupsPatchMode() == null ? PatchMode.REPLACE : req.groupsPatchMode();
            final var actual = Models.userGroups(user).stream().toList();
            final var desired = req.groups().stream().map(gp -> Groups.provide(session, realm, gp)).toList();
            final var groups = Patch.of(mode, actual, desired);
            for (final var item : groups.toBeRemoved()) {
                user.leaveGroup(item);
            }
            for (final var item : groups.toBeAdded()) {
                user.joinGroup(item);
            }
        }
    }

    @POST
    @Path("/groups/@by-path")
    @Consumes(MediaType.APPLICATION_JSON)
    // mapped to be http://localhost:8080/admin/realms/{realm}/provisioning/groups
    public GroupRepresentation putGroup(@QueryParam("path") String path) {
        final RealmModel realm = session.getContext().getRealm();
        final var gm = Groups.provide(session, realm, path);
        return ModelToRepresentation.toRepresentation(gm, true);
    }

    @DELETE
    @Path("/groups/@by-path")
    @Consumes(MediaType.APPLICATION_JSON)
    // mapped to be http://localhost:8080/admin/realms/{realm}/provisioning/groups
    public void deleteGroup(@QueryParam("path") String path) {
        final var realm = session.getContext().getRealm();
        final var g = Groups.search(session, realm, path);
        if (g == null) {
            return;
        }
        realm.removeGroup(g);
    }

    public static class Factory implements AdminRealmResourceProviderFactory {

        @Override
        public AdminRealmResourceProvider create(KeycloakSession session) {
            return new AdminRealmResourceProvider() {

                @Override
                public Object getResource(KeycloakSession ks, RealmModel rm, AdminPermissionEvaluator auth, AdminEventBuilder events) {
                    auth.users().requireManage();
                    return new ProvisioningEndpoints(ks);
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
