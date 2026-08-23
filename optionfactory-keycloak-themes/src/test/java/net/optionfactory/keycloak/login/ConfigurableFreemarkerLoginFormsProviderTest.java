package net.optionfactory.keycloak.login;

import java.util.Map;
import java.util.Properties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ConfigurableFreemarkerLoginFormsProviderTest {

    private static final Map<String, String> CONF = Map.of(
            "support-mail", "support@example.com",
            "analytics-id", "GTM-ABC123",
            "footer-label", "Area Riservata",
            "chained", "prefix-${conf.footer-label}",
            "sys-mixed", "${sys.something}"
    );

    @Test
    public void plainValuesAreUnchanged() {
        Assertions.assertEquals("no placeholders here", ConfigurableFreemarkerLoginFormsProvider.expandConf("no placeholders here", CONF));
        Assertions.assertEquals("", ConfigurableFreemarkerLoginFormsProvider.expandConf("", CONF));
        Assertions.assertNull(ConfigurableFreemarkerLoginFormsProvider.expandConf(null, CONF));
    }

    @Test
    public void confPlaceholdersExpand() {
        Assertions.assertEquals("support@example.com", ConfigurableFreemarkerLoginFormsProvider.expandConf("${conf.support-mail}", CONF));
        Assertions.assertEquals("Scrivi a support@example.com per aiuto", ConfigurableFreemarkerLoginFormsProvider.expandConf("Scrivi a ${conf.support-mail} per aiuto", CONF));
        Assertions.assertEquals("GTM-ABC123 and support@example.com", ConfigurableFreemarkerLoginFormsProvider.expandConf("${conf.analytics-id} and ${conf.support-mail}", CONF));
    }

    @Test
    public void dashedAndDottedKeysResolve() {
        Assertions.assertEquals("GTM-ABC123", ConfigurableFreemarkerLoginFormsProvider.expandConf("${conf.analytics-id}", CONF));
    }

    @Test
    public void unknownConfKeysStayVerbatim() {
        Assertions.assertEquals("${conf.not-set}", ConfigurableFreemarkerLoginFormsProvider.expandConf("${conf.not-set}", CONF));
    }

    @Test
    public void nonConfRefsStayVerbatim() {
        // plain refs and other layers' refs are not this provider's business
        Assertions.assertEquals("${someKey}", ConfigurableFreemarkerLoginFormsProvider.expandConf("${someKey}", CONF));
        Assertions.assertEquals("${sys.java.home}", ConfigurableFreemarkerLoginFormsProvider.expandConf("${sys.java.home}", CONF));
        Assertions.assertEquals("${env.HOME}", ConfigurableFreemarkerLoginFormsProvider.expandConf("${env.HOME}", CONF));
    }

    @Test
    public void colonDefaultUsedWhenKeyMissing() {
        Assertions.assertEquals("fallback@example.com", ConfigurableFreemarkerLoginFormsProvider.expandConf("${conf.not-set:fallback@example.com}", CONF));
    }

    @Test
    public void colonDefaultIgnoredWhenKeyPresent() {
        Assertions.assertEquals("support@example.com", ConfigurableFreemarkerLoginFormsProvider.expandConf("${conf.support-mail:fallback@example.com}", CONF));
    }

    @Test
    public void compositeKeysFallBackToOtherKeysNotLiterals() {
        // ${key1,key2} tries key2 as a KEY when key1 is absent: key chains work...
        Assertions.assertEquals("support@example.com", ConfigurableFreemarkerLoginFormsProvider.expandConf("${conf.not-set,conf.support-mail}", CONF));
        // ...but a literal second part resolves to nothing and the whole ref stays verbatim
        Assertions.assertEquals("${conf.not-set,support@example.com}", ConfigurableFreemarkerLoginFormsProvider.expandConf("${conf.not-set,support@example.com}", CONF));
    }

    @Test
    public void substitutedValuesAreRescannedRecursively() {
        Assertions.assertEquals("prefix-Area Riservata", ConfigurableFreemarkerLoginFormsProvider.expandConf("${conf.chained}", CONF));
        // refs for other layers inside conf values stay verbatim through rescanning
        Assertions.assertEquals("${sys.something}", ConfigurableFreemarkerLoginFormsProvider.expandConf("${conf.sys-mixed}", CONF));
    }

    @Test
    public void cyclesFailFast() {
        final Map<String, String> cyclic = Map.of(
                "a", "${conf.b}",
                "b", "${conf.a}"
        );
        Assertions.assertThrows(IllegalStateException.class, () -> ConfigurableFreemarkerLoginFormsProvider.expandConf("${conf.a}", cyclic));
    }

    @Test
    public void propertiesExpansionReturnsExpandedCopyWithoutMutatingOriginal() {
        final Properties raw = new Properties();
        raw.setProperty("themeHeaders.0", "<script src=\"https://www.googletagmanager.com/gtm.js?id=${conf.analytics-id}\"></script>");
        raw.setProperty("styles", "css/mytheme.css");
        raw.setProperty("kcLogoLink", "${conf.footer-url}");

        final Properties expanded = ConfigurableFreemarkerLoginFormsProvider.expandProperties(raw, CONF);

        Assertions.assertEquals("<script src=\"https://www.googletagmanager.com/gtm.js?id=GTM-ABC123\"></script>", expanded.getProperty("themeHeaders.0"));
        Assertions.assertEquals("css/mytheme.css", expanded.getProperty("styles"));
        Assertions.assertEquals("${conf.footer-url}", expanded.getProperty("kcLogoLink"));
        // original untouched (it may be keycloak's shared cached instance)
        Assertions.assertEquals("<script src=\"https://www.googletagmanager.com/gtm.js?id=${conf.analytics-id}\"></script>", raw.getProperty("themeHeaders.0"));
        Assertions.assertNotSame(raw, expanded);
    }

    @Test
    public void propertiesExpansionWithEmptyConfIsIdentityCopy() {
        final Properties raw = new Properties();
        raw.setProperty("a", "${conf.x}");
        final Properties expanded = ConfigurableFreemarkerLoginFormsProvider.expandProperties(raw, Map.of());
        Assertions.assertEquals("${conf.x}", expanded.getProperty("a"));
        Assertions.assertNotSame(raw, expanded);
    }
}
