package net.optionfactory.keycloak.login;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.eclipse.microprofile.config.ConfigProvider;
import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.common.util.StringPropertyReplacer;
import org.keycloak.forms.login.LoginFormsProviderFactory;
import org.keycloak.forms.login.freemarker.FreeMarkerLoginFormsProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.services.Urls;
import org.keycloak.theme.Theme;
import org.keycloak.theme.ThemeResourcesParser;
import org.keycloak.theme.beans.AdvancedMessageFormatterMethod;
import org.keycloak.theme.beans.MessageFormatterMethod;

public class ConfigurableFreemarkerLoginFormsProvider extends FreeMarkerLoginFormsProvider {

    private final Map<String, String> conf;

    public ConfigurableFreemarkerLoginFormsProvider(KeycloakSession session, Map<String, String> conf) {
        super(session);
        this.conf = conf;
        this.attributes.put("conf", conf);
        this.attributes.put("baseUri", session.getContext().getUri().getBaseUri().getPath());
        this.attributes.put("realmUri", Urls.realmBase(session.getContext().getUri().getBaseUri()).path("{realm}").build(session.getContext().getRealm().getName()).getPath());
    }

    @Override
    protected Properties handleThemeResources(Theme theme, Locale locale) {
        final var ps = super.handleThemeResources(theme, locale);
        // expand ${conf.*} in message bundles; super built the msg()/advancedMsg() formatters
        // over pre-expansion copies, so rebuild them over the expanded bundle
        for (final var key : ps.stringPropertyNames()) {
            ps.setProperty(key, expandConf(ps.getProperty(key), conf));
        }
        final Map<Object, Object> msgParams = new HashMap<>(attributes);
        msgParams.putAll(ps);
        attributes.put("msg", new MessageFormatterMethod(locale, msgParams));
        attributes.put("advancedMsg", new AdvancedMessageFormatterMethod(locale, ps));
        // expand ${conf.*} in theme.properties values (properties, themeResources, darkMode).
        // theme.getProperties() may be the shared cached instance: expand a copy, never mutate
        try {
            final var expanded = expandProperties(theme.getProperties(), conf);
            attributes.put("properties", expanded);
            attributes.put("themeResources", ThemeResourcesParser.parse(expanded));
            attributes.put("darkMode", "true".equals(expanded.getProperty("darkMode"))
                    && realm.getAttribute("darkMode", true));
        } catch (IOException e) {
            // super already logged and installed fallbacks
        }
        return ps;
    }

    // package-private statics: pure functions of (input, conf), unit-tested without a keycloak runtime

    static String expandConf(String value, Map<String, String> conf) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        return StringPropertyReplacer.replaceProperties(value, (k) -> {
            return k.startsWith("conf.") ? conf.get(k.substring("conf.".length())) : null;
        });
    }

    static Properties expandProperties(Properties raw, Map<String, String> conf) {
        final var expanded = new Properties();
        for (final var key : raw.stringPropertyNames()) {
            expanded.setProperty(key, expandConf(raw.getProperty(key), conf));
        }
        return expanded;
    }

    public static class Factory implements LoginFormsProviderFactory {

        private Map<String, String> conf;
        private static final Logger logger = Logger.getLogger(Factory.class);

        @Override
        public ConfigurableFreemarkerLoginFormsProvider create(KeycloakSession session) {
            return new ConfigurableFreemarkerLoginFormsProvider(session, conf);
        }

        @Override
        public void init(Config.Scope ignored) {
            final var prefix = "kc.login-theme-conf--";
            //fuck keycloak and their fetish for dashes
            final var config = ConfigProvider.getConfig();
            conf = StreamSupport.stream(config.getPropertyNames().spliterator(), false)
                    .filter(key -> key.startsWith(prefix))
                    .map(k -> Map.entry(k.substring(prefix.length()), config.getValue(k, String.class)))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            logger.infof("login:opfa-freemarker-configurable initialized: with %s custom properties", conf.size());
        }

        @Override
        public void postInit(KeycloakSessionFactory factory) {
        }

        @Override
        public void close() {
        }

        @Override
        public String getId() {
            return "opfa-freemarker-configurable";
        }

        @Override
        public int order() {
            return 1;
        }

    }

}
