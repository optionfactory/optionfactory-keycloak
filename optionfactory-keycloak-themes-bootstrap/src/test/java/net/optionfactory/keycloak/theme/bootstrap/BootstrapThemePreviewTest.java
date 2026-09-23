package net.optionfactory.keycloak.theme.bootstrap;

import net.optionfactory.keycloak.theme.preview.LoginThemePreviewGenerator;
import org.junit.jupiter.api.Test;

public class BootstrapThemePreviewTest {

    @Test
    public void rendersEveryBaseLoginPage() throws Exception {
        LoginThemePreviewGenerator.preview()
                .theme("bootstrap")
                .generate();
    }

    @Test
    public void rendersEveryBaseLoginPageWithFloatingLabels() throws Exception {
        LoginThemePreviewGenerator.preview()
                .theme("bootstrap")
                .property("floatingLabels", "true")
                .title("bootstrap theme preview (floating labels)")
                .outputDirectory("theme-preview-floating-labels")
                .generate();
    }
}
