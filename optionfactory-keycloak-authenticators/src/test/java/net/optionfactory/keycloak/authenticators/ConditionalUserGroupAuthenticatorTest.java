package net.optionfactory.keycloak.authenticators;

import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ConditionalUserGroupAuthenticatorTest {

    @Test
    public void whatTheConsolePickerStoresIsUsedAsItIs() {
        // the group picker writes the group's path, leading slash and all
        Assertions.assertEquals(List.of("/customers/premium"),
                ConditionalUserGroupAuthenticator.candidatePaths("/customers/premium"));
    }

    @Test
    public void aValueWrittenByHandWithoutTheSlashIsAlsoTriedAsATopLevelPath() {
        Assertions.assertEquals(List.of("premium", "/premium"),
                ConditionalUserGroupAuthenticator.candidatePaths("premium"));
    }

    @Test
    public void surroundingSpaceIsIgnored() {
        Assertions.assertEquals(List.of("/premium"),
                ConditionalUserGroupAuthenticator.candidatePaths("  /premium  "));
    }

    @Test
    public void nothingConfiguredMeansNothingToLookUp() {
        Assertions.assertEquals(List.of(), ConditionalUserGroupAuthenticator.candidatePaths(null));
        Assertions.assertEquals(List.of(), ConditionalUserGroupAuthenticator.candidatePaths("   "));
    }
}
