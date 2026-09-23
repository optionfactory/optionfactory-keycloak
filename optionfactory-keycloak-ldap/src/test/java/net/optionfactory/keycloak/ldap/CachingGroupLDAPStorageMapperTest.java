package net.optionfactory.keycloak.ldap;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.keycloak.models.GroupModel;

public class CachingGroupLDAPStorageMapperTest {

    private static GroupModel group(String id) {
        return (GroupModel) Proxy.newProxyInstance(CachingGroupLDAPStorageMapperTest.class.getClassLoader(),
                new Class<?>[]{GroupModel.class},
                (proxy, method, args) -> "getId".equals(method.getName()) ? id : null);
    }

    private static List<String> ids(Stream<GroupModel> groups) {
        return groups.map(GroupModel::getId).toList();
    }

    @Test
    public void aMembershipHeldInLdapAndInTheDatabaseIsReportedOnce() {
        // what the delegate hands over: the ldap mappings, then the database rows this mapper wrote
        final var reported = Stream.of(group("eng"), group("sales"), group("eng"), group("sales"));

        Assertions.assertEquals(List.of("eng", "sales"), ids(CachingGroupLDAPStorageMapper.distinctById(reported)));
    }

    @Test
    public void groupsThatOnlyExistInTheDatabaseSurvive() {
        // local groups, and those another mapper assigned, are not in ldap and must still be reported
        final var reported = Stream.of(group("eng"), group("eng"), group("local-support"));

        Assertions.assertEquals(List.of("eng", "local-support"), ids(CachingGroupLDAPStorageMapper.distinctById(reported)));
    }

    @Test
    public void theOrderTheDelegateChoseIsKept() {
        final var reported = Stream.of(group("b"), group("a"), group("c"), group("a"));

        Assertions.assertEquals(List.of("b", "a", "c"), ids(CachingGroupLDAPStorageMapper.distinctById(reported)));
    }

    @Test
    public void noGroupsIsNotAProblem() {
        Assertions.assertEquals(List.of(), ids(CachingGroupLDAPStorageMapper.distinctById(Stream.of())));
    }
}
