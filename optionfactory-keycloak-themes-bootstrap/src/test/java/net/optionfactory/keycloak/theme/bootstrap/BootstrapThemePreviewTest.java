package net.optionfactory.keycloak.theme.bootstrap;

import net.optionfactory.keycloak.theme.preview.LoginThemePreviewGenerator;
import org.junit.jupiter.api.Disabled;
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

    @Test
    @Disabled("enable to browse the gallery: serves it until the run is stopped")
    public void servesEveryBaseLoginPage() throws Exception {
        LoginThemePreviewGenerator.preview()
                .theme("bootstrap")
                .serve();
    }

    /* a port of its own, so both galleries can be up at once */
    @Test
    @Disabled("enable to browse the floating-labels gallery: serves it until the run is stopped")
    public void servesEveryBaseLoginPageWithFloatingLabels() throws Exception {
        LoginThemePreviewGenerator.preview()
                .theme("bootstrap")
                .property("floatingLabels", "true")
                .title("bootstrap theme preview (floating labels)")
                .outputDirectory("theme-preview-floating-labels")
                .serve(8001);
    }
}
