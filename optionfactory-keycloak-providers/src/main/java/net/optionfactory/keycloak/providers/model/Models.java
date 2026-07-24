package net.optionfactory.keycloak.providers.model;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.keycloak.models.GroupModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserModel;

public class Models {

    public static Set<GroupModel> userGroups(UserModel user) {
        try (final var ugs = user.getGroupsStream()) {
            return ugs.collect(Collectors.toSet());
        }
    }

    public static Set<GroupModel> realmGroups(RealmModel realm) {
        try (final var ugs = realm.getGroupsStream()) {
            return ugs.collect(Collectors.toSet());
        }
    }

    public static Optional<String> attributeFirst(UserModel u, String attr) {
        try (final var s = u.getAttributeStream(attr)) {
            return s.findFirst();
        }
    }

    public static List<String> attribute(UserModel u, String attr) {
        try (final var s = u.getAttributeStream(attr)) {
            return s.toList();
        }
    }
    public static List<String> attribute(RoleModel u, String attr) {
        try (final var s = u.getAttributeStream(attr)) {
            return s.toList();
        }
    }

    public static List<String> requiredActions(UserModel u) {
        try (final var s = u.getRequiredActionsStream()) {
            return s.toList();
        }
    }

}
