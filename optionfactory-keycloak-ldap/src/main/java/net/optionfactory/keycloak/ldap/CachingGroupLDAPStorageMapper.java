package net.optionfactory.keycloak.ldap;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Stream;
import org.jboss.logging.Logger;
import org.keycloak.component.ComponentModel;
import org.keycloak.component.ComponentValidationException;
import org.keycloak.models.GroupModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.storage.ldap.LDAPStorageProvider;
import org.keycloak.storage.ldap.idm.model.LDAPObject;
import org.keycloak.storage.ldap.mappers.AbstractLDAPStorageMapper;
import org.keycloak.storage.ldap.mappers.membership.group.GroupLDAPStorageMapper;
import org.keycloak.storage.ldap.mappers.membership.group.GroupLDAPStorageMapperFactory;
import org.keycloak.storage.ldap.mappers.membership.group.GroupMapperConfig;

public class CachingGroupLDAPStorageMapper extends GroupLDAPStorageMapper {

    private static final Logger logger = Logger.getLogger(CachingGroupLDAPStorageMapper.class);

    public CachingGroupLDAPStorageMapper(ComponentModel mapperModel, LDAPStorageProvider ldapProvider, GroupLDAPStorageMapperFactory factory) {
        super(mapperModel, ldapProvider, factory);
    }

    @Override
    public UserModel proxy(LDAPObject ldapUser, UserModel user, RealmModel realm) {
        return new DbCachingLdapUserDelegate(realm, user, ldapUser);
    }

    public class DbCachingLdapUserDelegate extends LDAPGroupMappingsUserDelegate {

        private final UserModel inner;

        public DbCachingLdapUserDelegate(RealmModel realm, UserModel user, LDAPObject ldapUser) {
            super(realm, user, ldapUser);
            this.inner = user;
        }

        /// The delegate reports the ldap mappings and then, whenever the groups path is not the realm root,
        /// the database rows as well. This mapper writes those same memberships to the database, so every
        /// ldap group would be reported twice - visibly so in the admin console. Keeping the first sighting
        /// of each id preserves the delegate's order and still surfaces groups that exist only in the
        /// database: local ones, and those assigned by another mapper.
        @Override
        public Stream<GroupModel> getGroupsStream() {
            return distinctById(super.getGroupsStream());
        }

        @Override
        public void joinGroup(GroupModel group) {
            super.joinGroup(group);
            inner.joinGroup(group);
        }

        @Override
        public void leaveGroup(GroupModel group) {
            super.leaveGroup(group);
            inner.leaveGroup(group);
        }

    }

    /// Keeps the first sighting of each group id, preserving the order it arrives in.
    static Stream<GroupModel> distinctById(Stream<GroupModel> groups) {
        final var seen = new HashSet<String>();
        return groups.filter(group -> seen.add(group.getId()));
    }

    @Override
    public void onImportUserFromLDAP(LDAPObject ldapUser, UserModel user, RealmModel realm, boolean isCreate) {
        //mode is always (LDAP_ONLY);

        List<LDAPObject> ldapGroups = getLDAPGroupMappings(ldapUser);

        final GroupModel ldapGroupsRoot = getKcGroupsPathGroup(realm);
        // everything under the configured path belongs to this mapper and is about to be rebuilt from ldap.
        // isGroupInGroupPath walks the whole ancestor chain, so a membership nested below the path - the
        // shape 'preserve group inheritance' produces - is revoked too, and organization groups are spared.
        // Collected first: leaving a group while streaming the same collection is asking for trouble.
        final List<GroupModel> owned;
        try (final var ugs = user.getGroupsStream()) {
            owned = ugs.filter(gm -> isGroupInGroupPath(realm, gm)).toList();
        }
        owned.forEach(user::leaveGroup);
        // Import role mappings from LDAP into Keycloak DB
        for (LDAPObject ldapGroup : ldapGroups) {

            GroupModel kcGroup = findKcGroupOrSyncFromLDAP(realm, ldapGroupsRoot, ldapGroup, user);
            if (kcGroup != null) {
                logger.debugf("User '%s' joins group '%s' during import from LDAP", user.getUsername(), kcGroup.getName());
                user.joinGroup(kcGroup);
            }
        }
    }

    public static class Factory extends GroupLDAPStorageMapperFactory {

        @Override
        public String getId() {
            return "caching-group-ldap-mapper";
        }

        @Override
        public String getHelpText() {
            return "Used to map group mappings of groups from some LDAP DN to Keycloak group mappings. This mapper always work in LDAP_ONLY mode but stores group membership in the database.";
        }

        @Override
        protected AbstractLDAPStorageMapper createMapper(ComponentModel mapperModel, LDAPStorageProvider federationProvider) {
            return new CachingGroupLDAPStorageMapper(mapperModel, federationProvider, this);
        }

        @Override
        public void validateConfiguration(KeycloakSession session, RealmModel realm, ComponentModel config) throws ComponentValidationException {
            super.validateConfiguration(session, realm, config);
            final var mode = config.getConfig().getFirst(GroupMapperConfig.MODE);
            if (!"LDAP_ONLY".equals(mode)) {
                throw new ComponentValidationException(String.format("Mode MUST be LDAP_ONLY when using '%s'", getId()));
            }
            // at the realm root every group a user has counts as this mapper's, so a sync would drop the
            // purely local ones it is about to not find in ldap
            final var groupsPath = config.getConfig().getFirst(GroupMapperConfig.LDAP_GROUPS_PATH);
            if (groupsPath == null || groupsPath.isBlank() || GroupMapperConfig.DEFAULT_LDAP_GROUPS_PATH.equals(groupsPath.trim())) {
                throw new ComponentValidationException(String.format("Groups Path MUST NOT be the realm root when using '%s', or a sync would drop every group membership the user holds outside ldap", getId()));
            }
        }

    }
}
