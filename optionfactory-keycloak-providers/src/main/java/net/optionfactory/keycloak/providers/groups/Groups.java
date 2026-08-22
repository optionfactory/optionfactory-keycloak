package net.optionfactory.keycloak.providers.groups;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.core.Response;
import java.util.List;
import net.optionfactory.keycloak.providers.validation.Problem;
import org.keycloak.models.GroupModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.utils.KeycloakModelUtils;

public class Groups {

    public static GroupModel provide(KeycloakSession session, RealmModel realm, String groupPath) {
        if (groupPath == null || groupPath.isBlank() || groupPath.equals("/")) {
            throw badPath("must not be blank");
        }
        final var groupNames = KeycloakModelUtils.splitPath(groupPath, true);
        if (groupNames.length == 0) {
            // e.g. '//': both slashes are stripped and nothing remains
            throw badPath("must not be blank");
        }
        // 'a//b' splits to an empty segment that would create an empty-named group
        for (final var segment : groupNames) {
            if (segment.isBlank()) {
                throw badPath("segments must not be blank");
            }
        }
        GroupModel parentGroup = null;
        for (String groupName : groupNames) {
            final var currentParentGroup = parentGroup;
            try(final var groups = parentGroup == null ? session.groups().getTopLevelGroupsStream(realm) : parentGroup.getSubGroupsStream()){
                parentGroup = groups.filter(group -> group.getName().equals(groupName)).findFirst().orElseGet(() -> {
                    final var g = realm.createGroup(groupName);
                    g.setParent(currentParentGroup);
                    if(currentParentGroup != null){
                        //this is needed to invalidate the infinispan cache.
                        currentParentGroup.addChild(g);
                    }
                    return g;
                });
            }
        }
        return parentGroup;
    }

    private static BadRequestException badPath(String reason) {
        throw new BadRequestException(Response.status(Response.Status.BAD_REQUEST)
                .type("application/failures+json")
                .entity(List.of(new Problem("FIELD_ERROR", "path", reason)))
                .build());
    }

    public static GroupModel search(KeycloakSession session, RealmModel realm, String groupPath) {
        return KeycloakModelUtils.findGroupByPath(session, realm, groupPath);
    }
}
