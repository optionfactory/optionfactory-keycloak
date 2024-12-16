package net.optionfactory.keycloak.ldap;

import java.util.List;
import org.jboss.logging.Logger;
import org.keycloak.component.ComponentModel;
import org.keycloak.component.ComponentValidationException;
import org.keycloak.models.GroupModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.UserModelDelegate;
import org.keycloak.storage.ldap.LDAPStorageProvider;
import org.keycloak.storage.ldap.idm.model.LDAPObject;
import org.keycloak.storage.ldap.mappers.membership.group.GroupLDAPStorageMapper;
import org.keycloak.storage.ldap.mappers.membership.group.GroupLDAPStorageMapperFactory;
import org.keycloak.storage.ldap.mappers.membership.group.GroupMapperConfig;

public class CachingGroupLDAPStorageMapper extends GroupLDAPStorageMapper {

    private static final Logger logger = Logger.getLogger(CachingGroupLDAPStorageMapper.class);

    public CachingGroupLDAPStorageMapper(ComponentModel mapperModel, LDAPStorageProvider ldapProvider, GroupLDAPStorageMapperFactory factory) {
        super(mapperModel, ldapProvider, factory);
    }

    @Override
    public UserModel proxy(LDAPObject ldapUser, UserModel delegate, RealmModel realm) {
        return new LDAPGroupMappingsUserDelegate(realm, delegate, ldapUser);
    }

    @Override
    public void onImportUserFromLDAP(LDAPObject ldapUser, UserModel proxy, RealmModel realm, boolean isCreate) {
        //mode is always (LDAP_ONLY);
        final UserModel delegate = ((UserModelDelegate) proxy).getDelegate();

        List<LDAPObject> ldapGroups = getLDAPGroupMappings(ldapUser);
        //TODO: diff and add/remove
        if (!ldapGroups.isEmpty()) {
            GroupModel parent = getKcGroupsPathGroup(realm);
            // Import role mappings from LDAP into Keycloak DB
            for (LDAPObject ldapGroup : ldapGroups) {

                GroupModel kcGroup = findKcGroupOrSyncFromLDAP(realm, parent, ldapGroup, proxy);
                if (kcGroup != null) {
                    logger.debugf("User '%s' joins group '%s' during import from LDAP", proxy.getUsername(), kcGroup.getName());
                    delegate.joinGroup(kcGroup);
                }
            }
        }
    }

    public static class Factory extends GroupLDAPStorageMapperFactory {

        @Override
        public String getId() {
            return "caching-group-ldap-mapper";
        }

        @Override
        public void validateConfiguration(KeycloakSession session, RealmModel realm, ComponentModel config) throws ComponentValidationException {
            super.validateConfiguration(session, realm, config);
            final var mode = config.getConfig().getFirst(GroupMapperConfig.MODE);
            if (!"LDAP_ONLY".equals(mode)) {
                throw new ComponentValidationException("Mode MUST be LDAP_ONLY when using  'caching-group-ldap-mapper'");
            }
        }

    }
}
