package net.optionfactory.keycloak.theme.bootstrap;

import net.optionfactory.keycloak.theme.preview.LoginThemePreviewGenerator;
import org.junit.jupiter.api.Test;

public class BootstrapThemePreviewTest {

    
    @Test
    public void rendersEveryBaseLoginPage() throws Exception {
        LoginThemePreviewGenerator.preview()
                .templateResourceBases("theme/bootstrap/login", "theme/base/login")
                .propertiesResources("theme/base/login/theme.properties", "theme/bootstrap/login/theme.properties")
                .title("bootstrap theme preview")
                .generate();
    }
}
