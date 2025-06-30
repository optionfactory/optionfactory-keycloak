package net.optionfactory.keycloak.login;

import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.eclipse.microprofile.config.ConfigProvider;
import org.keycloak.Config;
import org.keycloak.common.util.StringPropertyReplacer;
import org.keycloak.forms.login.LoginFormsProviderFactory;
import org.keycloak.forms.login.freemarker.FreeMarkerLoginFormsProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.services.Urls;
import org.keycloak.theme.Theme;

public class ConfigurableFreemarkerLoginFormsProvider extends FreeMarkerLoginFormsProvider {

    private final Map<String, String> conf;

    public ConfigurableFreemarkerLoginFormsProvider(KeycloakSession session, Map<String, String> conf) {
        super(session);
        this.conf = conf;
        this.attributes.put("conf", conf);
        this.attributes.put("baseUri", session.getContext().getUri().getBaseUri().getPath());
        this.attributes.put("realmUri", Urls.realmBase(session.getContext().getUri().getBaseUri()).path("{realm}").build(session.getContext().getRealm().getName()).getPath());
        System.out.println(this.attributes);
    }

    @Override
    protected Properties handleThemeResources(Theme theme, Locale locale) {
        final var prefix = "conf.";
        final var ps = super.handleThemeResources(theme, locale);
        for (final var key : ps.stringPropertyNames()) {
            final var value = ps.getProperty(key);
            ps.setProperty(key, StringPropertyReplacer.replaceProperties(value, (k) -> {
                return k.startsWith(prefix) ? this.conf.get(k.substring(prefix.length())) : null;
            }));
        }
        return ps;
    }

    public static class Factory implements LoginFormsProviderFactory {

        private Map<String, String> conf;

        @Override
        public ConfigurableFreemarkerLoginFormsProvider create(KeycloakSession session) {
            return new ConfigurableFreemarkerLoginFormsProvider(session, conf);
        }

        @Override
        public void init(Config.Scope ignored) {
            final var prefix = "kc.conf-login-";
            //fuck keycloak and their fetish for dashes
            final var config = ConfigProvider.getConfig();
            conf = StreamSupport.stream(config.getPropertyNames().spliterator(), false)
                    .filter(key -> key.startsWith(prefix))
                    .map(k -> Map.entry(k.substring(prefix.length()), config.getValue(k, String.class)))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

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
