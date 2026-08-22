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
            throw new BadRequestException(Response.status(Response.Status.BAD_REQUEST)
                    .type("application/failures+json")
                    .entity(List.of(new Problem("FIELD_ERROR", "path", "must not be blank")))
                    .build());
        }
        final var groupNames = KeycloakModelUtils.splitPath(groupPath, true);
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

    public static GroupModel search(KeycloakSession session, RealmModel realm, String groupPath) {
        return KeycloakModelUtils.findGroupByPath(session, realm, groupPath);
    }
}
