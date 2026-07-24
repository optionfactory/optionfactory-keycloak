package net.optionfactory.keycloak.ldap;

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

    @Override
    public void onImportUserFromLDAP(LDAPObject ldapUser, UserModel user, RealmModel realm, boolean isCreate) {
        //mode is always (LDAP_ONLY);

        List<LDAPObject> ldapGroups = getLDAPGroupMappings(ldapUser);

        final GroupModel ldapGroupsRoot = getKcGroupsPathGroup(realm);
        try (final var ugs = user.getGroupsStream()) {
            ugs
                    .filter(gm -> ldapGroupsRoot == null || ldapGroupsRoot.equals(gm.getParent()))
                    .forEach(gm -> user.leaveGroup(gm));
        }
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
        }

    }
}
