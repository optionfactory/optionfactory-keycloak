package net.optionfactory.keycloak.theme.preview;

import java.nio.file.Files;
import java.nio.file.Path;
import net.optionfactory.keycloak.theme.preview.LoginThemePreviewGenerator.PreviewPlugin;
import net.optionfactory.keycloak.theme.preview.LoginThemePreviewGenerator.StepPage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LoginThemePreviewGeneratorTest {

    /**
     * What a module owning step pages ships, so its previews say the pages and their fixtures
     * once instead of per preview.
     */
    public static class TestPreviewPlugin implements PreviewPlugin {

        @Override
        public void contribute(LoginThemePreviewGenerator preview) {
            preview.stepPages(new StepPage("login", "login-from-plugin", m -> m.put("title", "from the plugin")));
        }
    }

    @Test
    public void namingAThemeIsTheWholeConfiguration() throws Exception {
        LoginThemePreviewGenerator.preview()
                .theme("base")
                .outputDirectory("theme-preview-base")
                .generate();

        var out = Path.of("target", "theme-preview-base");
        Assertions.assertTrue(Files.exists(out.resolve("login.html")), "stock page");
        Assertions.assertTrue(Files.exists(out.resolve("index.html")), "gallery index");
        // base is abstract: its locales list every bundle keycloak ships, so english is kept
        Assertions.assertTrue(Files.readString(out.resolve("login.html")).contains("lang=\"en\""), "deduced locale");
    }

    @Test
    public void rendersStepPagesContributedByAPlugin() throws Exception {
        LoginThemePreviewGenerator.preview()
                .theme("base")
                .plugin(new TestPreviewPlugin())
                .outputDirectory("theme-preview-plugins")
                .generate();

        var page = Path.of("target", "theme-preview-plugins", "login-from-plugin.html");
        Assertions.assertTrue(Files.exists(page), "step page contributed by the plugin");
        Assertions.assertTrue(Files.readString(page).contains("from the plugin"), "plugin model tweak applied");
    }
}
