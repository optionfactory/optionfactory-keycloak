package net.optionfactory.keycloak.providers.groups;

import org.keycloak.models.GroupModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.utils.KeycloakModelUtils;

public class Groups {

    public static GroupModel provide(KeycloakSession session, RealmModel realm, String groupPath) {
        final var groupNames = KeycloakModelUtils.splitPath(groupPath, true);
        GroupModel parentGroup = null;
        for (String groupName : groupNames) {
            final var currentParentGroup = parentGroup;
            final var groups = parentGroup == null ? session.groups().getTopLevelGroupsStream(realm) : parentGroup.getSubGroupsStream();
            parentGroup = groups.filter(group -> group.getName().equals(groupName)).findFirst().orElseGet(() -> {
                final var g = realm.createGroup(groupName);
                g.setParent(currentParentGroup);
                return g;
            });
        }
        return parentGroup;
    }

}
