package net.optionfactory.keycloak.theme.preview;

import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapperBuilder;
import freemarker.template.Template;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.Set;
import java.util.function.Consumer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import org.junit.jupiter.api.Assertions;

/**
 * Renders every stock base login page (discovered from the keycloak-themes
 * jar) through a login theme and writes an html gallery (index included,
 * rendered via preview-index.ftl) to the output directory for visual
 * inspection. Rendering failures throw, so running it as a test doubles as a
 * template/data-model compatibility check.
 *
 * All stock-page fixtures are preconfigured, and the theme's own inheritance chain answers
 * every resource path, so naming the theme is the whole configuration. Reusable by login theme
 * modules: depend on this module (test scope), name the theme, generate - no subclassing:
 *
 * <pre>
 * LoginThemePreviewGenerator.preview()
 *         .theme("mytheme")
 *         .locale(Locale.ITALIAN)
 *         .generate();
 * </pre>
 *
 * Everything else is deduced from the theme itself, so a preview normally configures nothing:
 *
 * <ul>
 * <li>the inheritance chain, by walking {@code parent=} exactly as keycloak does, which gives
 * templates child-first and property/message merging parent-first;
 * <li>the message bundles for the locale, skipping a theme in the chain that ships none;
 * <li>the locale, from the first {@code locales=} the chain declares as its own (an abstract
 * theme is skipped: base lists every bundle keycloak ships, which is availability rather than
 * preference), english when no theme declares one;
 * <li>the theme's own step pages, being the templates it adds that call the layout macro -
 * a partial like {@code template.ftl} or an include renders nothing on its own and is left out;
 * <li>the gallery title, from the theme name.
 * </ul>
 *
 * What is left to say is what the theme cannot tell you: a variant to preview, a fixture for a
 * step page, or an output directory of its own.
 *
 * <pre>
 * LoginThemePreviewGenerator.preview()
 *         .theme("mytheme")
 *         .property("floatingLabels", "true")
 *         .stepPages(new StepPage("my-step", m -&gt; m.put("total", 3)))
 *         .outputDirectory("theme-preview-floating-labels")
 *         .generate();
 * </pre>
 *
 * Step pages shared by several themes of a module - and their fixtures - go in a
 * {@link PreviewPlugin} that each preview registers with {@link #plugin}, rather than being
 * repeated per preview. The {@code *Resources} setters remain for a theme that does not follow
 * the {@code theme/<name>/login} layout.
 *
 * Provider-supplied {@code theme-resources} on the classpath (templates, resources and
 * message bundles, as contributed by any jar) are picked up automatically, with keycloak's
 * precedence: the theme chain wins for templates and resources, and its bundles overlay
 * the provider ones.
 *
 * The gallery's own page is an editor: an aside lists every custom property the previewed
 * stylesheets declare in a {@code :root} block and overrides it live in each frame, takes
 * arbitrary css for a selector the tokens do not reach, and switches every frame to a phone,
 * tablet or desktop viewport (which is what drives the theme's own media queries). Nothing is
 * written to a file - the panel hands back the css to paste into a stylesheet.
 *
 * The gallery must be served over http (module scripts are blocked from file:// origins):
 * {@link #serve()}, or python3 -m http.server -d target/theme-preview
 */
public class LoginThemePreviewGenerator {

    // saml-post-form is excluded: it is a transient relay (auto-submits its form on
    // load) with nothing to inspect visually
    private static final Set<String> NON_PAGES = Set.of(
            "template", "footer", "theme-resources", "cli_splash",
            "user-profile-commons", "password-commons", "register-commons",
            "passkeys", "saml-post-form"
    );

    // the stock template's importmap points at keycloak's common theme for rfc4648;
    // the webauthn scripts only use base64url.parse/stringify, satisfied by the
    // rfc4648.js stub resource shipped in this module
    private static final String RFC4648_STUB_RESOURCE = "rfc4648.js";

    // provider-supplied theme resources (ClasspathThemeResourceProviderFactory). Keycloak
    // consults these *after* the whole theme chain for templates and resources, and merges
    // their message bundles *underneath* it, so they act as a fallback layer in all three.
    private static final String THEME_RESOURCES_TEMPLATES = "theme-resources/templates";
    private static final String THEME_RESOURCES_RESOURCES = "theme-resources/resources";
    private static final String THEME_RESOURCES_MESSAGES = "theme-resources/messages/messages_%s.properties";

    // child-first, e.g. [appcompagnie, bootstrap, base]. Everything else derives from it;
    // the *Resources setters below override a derived list when a theme does not follow the
    // theme/<name>/login layout.
    private final List<String> themeChain = new ArrayList<>(List.of("base"));
    private List<String> templateResourceBases;
    private List<String> propertiesResources;
    private List<String> messagesResources;
    private final Map<String, String> propertyOverrides = new LinkedHashMap<>();
    private final List<StepPage> stepPages = new ArrayList<>();
    private final List<PreviewPlugin> plugins = new ArrayList<>();
    private Locale locale;
    private String title;
    private String outputDirectory;

    /**
     * A page outside the stock set: a theme's own step template. {@code template} names the
     * ftl (without extension), {@code outputName} the html file, and {@code model} tweaks the
     * preconfigured model the way the stock fixtures do.
     */
    public record StepPage(String template, String outputName, Consumer<Map<String, Object>> model) {

        public StepPage(String template) {
            this(template, template, m -> {
            });
        }

        public StepPage(String template, Consumer<Map<String, Object>> model) {
            this(template, template, model);
        }
    }

    /**
     * Contributes to a preview - step pages, fixtures, property overrides. A module owning step
     * pages implements this once and every preview of its themes registers the one instance with
     * {@link #plugin}, instead of repeating the pages and their fixtures per preview.
     */
    public interface PreviewPlugin {

        void contribute(LoginThemePreviewGenerator preview);
    }

    public static LoginThemePreviewGenerator preview() {
        return new LoginThemePreviewGenerator();
    }

    /**
     * The theme to preview, by name. Its inheritance chain is walked through each
     * {@code theme.properties}' {@code parent=} exactly as keycloak walks it, and templates
     * (child first), property merging and message bundles (both parent first) all derive from
     * it - which is every resource path the preview needs. Bundles missing from a theme in the
     * chain are skipped; a locale is picked with {@link #locale}.
     */
    public LoginThemePreviewGenerator theme(String name) {
        this.themeChain.clear();
        for (var current = name; current != null;) {
            Assertions.assertFalse(themeChain.contains(current), "theme inheritance cycle at " + current);
            themeChain.add(current);
            current = parentOf(current);
        }
        return this;
    }

    /**
     * Resource bases of the loader chain, child theme first: the first
     * template found wins, mirroring keycloak theme inheritance. Overrides what
     * {@link #theme} derives.
     */
    public LoginThemePreviewGenerator templateResourceBases(String... bases) {
        this.templateResourceBases = List.of(bases);
        return this;
    }

    /**
     * theme.properties locations to merge, parent first: later loads override
     * earlier ones, mirroring keycloak's per-key merge. Overrides what
     * {@link #theme} derives.
     */
    public LoginThemePreviewGenerator propertiesResources(String... resources) {
        this.propertiesResources = List.of(resources);
        return this;
    }

    /**
     * Message bundle locations to merge, parent first. Overrides what {@link #theme} derives.
     */
    public LoginThemePreviewGenerator messagesResources(String... resources) {
        this.messagesResources = List.of(resources);
        return this;
    }

    /**
     * A theme.properties value applied over the merged ones: previews a variant (an opt-in
     * flag, say) without a properties file of its own.
     */
    public LoginThemePreviewGenerator property(String key, String value) {
        this.propertyOverrides.put(key, value);
        return this;
    }

    /**
     * Step pages of the theme, rendered after the stock ones.
     */
    public LoginThemePreviewGenerator stepPages(StepPage... pages) {
        Collections.addAll(this.stepPages, pages);
        return this;
    }

    /**
     * Contributions shared by the previews of this module's themes.
     */
    public LoginThemePreviewGenerator plugin(PreviewPlugin... plugins) {
        Collections.addAll(this.plugins, plugins);
        return this;
    }

    /**
     * The locale to render in. Deduced from the theme's own {@code locales=} (its first entry)
     * when left unset, english if it declares none.
     */
    public LoginThemePreviewGenerator locale(Locale locale) {
        this.locale = locale;
        return this;
    }

    public LoginThemePreviewGenerator title(String title) {
        this.title = title;
        return this;
    }

    public LoginThemePreviewGenerator outputDirectory(String name) {
        this.outputDirectory = name;
        return this;
    }


    private Properties ownProperties(String theme) {
        var properties = new Properties();
        var resource = "theme/" + theme + "/login/theme.properties";
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resource)) {
            Assertions.assertNotNull(is, resource + " not on test classpath");
            properties.load(new InputStreamReader(is, StandardCharsets.UTF_8));
        } catch (IOException ex) {
            throw new IllegalStateException("cannot read " + resource, ex);
        }
        return properties;
    }

    private String parentOf(String theme) {
        return ownProperties(theme).getProperty("parent");
    }

    /**
     * The first locale the chain declares as its own, child first. An abstract theme is skipped:
     * base lists every bundle keycloak ships, which is availability rather than preference, and
     * inheriting that list would preview a theme in whatever language happens to sort first.
     */
    private Locale deducedLocale() {
        for (String theme : themeChain) {
            var own = ownProperties(theme);
            if (Boolean.parseBoolean(own.getProperty("abstract", "false"))) {
                continue;
            }
            var first = own.getProperty("locales", "").split(",")[0].trim();
            if (!first.isEmpty()) {
                return Locale.forLanguageTag(first);
            }
        }
        return Locale.ENGLISH;
    }

    private List<String> templateBases() {
        if (templateResourceBases != null) {
            return templateResourceBases;
        }
        return themeChain.stream().map(t -> "theme/" + t + "/login").toList();
    }

    private List<String> parentFirstChain() {
        var reversed = new ArrayList<>(themeChain);
        Collections.reverse(reversed);
        return reversed;
    }

    private List<String> propertyResources() {
        if (propertiesResources != null) {
            return propertiesResources;
        }
        return parentFirstChain().stream().map(t -> "theme/" + t + "/login/theme.properties").toList();
    }

    // a theme in the chain need not ship a bundle for the locale: derived paths are optional,
    // an explicitly named one is a typo until proven otherwise
    private record Bundle(String resource, boolean optional) {
    }

    private List<Bundle> messageBundles() {
        if (messagesResources != null) {
            return messagesResources.stream().map(r -> new Bundle(r, false)).toList();
        }
        return parentFirstChain().stream()
                .map(t -> new Bundle("theme/" + t + "/login/messages/messages_%s.properties".formatted(locale.getLanguage()), true))
                .toList();
    }

    /**
     * Generates the gallery, then serves it on port 8000 until the run is stopped. The frames are
     * pages carrying module scripts, which no browser loads from a {@code file://} origin, so the
     * gallery needs a server; this is that server, so nothing outside the build is needed to look
     * at a theme. It blocks, which is what a {@code @Disabled} test is for:
     *
     * <pre>
     * &#64;Test
     * &#64;Disabled("enable to browse the gallery")
     * public void servesEveryLoginPage() throws Exception {
     *     LoginThemePreviewGenerator.preview().theme("mytheme").serve();
     * }
     * </pre>
     */
    public void serve() throws Exception {
        serve(8000);
    }

    public void serve(int port) throws Exception {
        generate();
        var root = Path.of("target", outputDirectory != null ? outputDirectory : "theme-preview").toRealPath();
        var server = com.sun.net.httpserver.HttpServer.create(new java.net.InetSocketAddress(port), 0);
        server.createContext("/", exchange -> {
            var requested = exchange.getRequestURI().getPath().substring(1);
            var file = root.resolve(requested.isEmpty() ? "index.html" : requested).normalize();
            if (!file.startsWith(root) || !Files.isRegularFile(file)) {
                exchange.sendResponseHeaders(404, -1);
                exchange.close();
                return;
            }
            var body = Files.readAllBytes(file);
            exchange.getResponseHeaders().add("Content-Type", contentType(file));
            exchange.sendResponseHeaders(200, body.length);
            try (var out = exchange.getResponseBody()) {
                out.write(body);
            }
        });
        server.start();
        System.out.printf("Serving %s at http://localhost:%d/ - stop the run to stop it%n", root, port);
        new java.util.concurrent.CountDownLatch(1).await();
    }

    private static String contentType(Path file) {
        var name = file.getFileName().toString();
        var extension = name.substring(name.lastIndexOf('.') + 1);
        return switch (extension) {
            case "html" ->
                "text/html; charset=utf-8";
            case "css" ->
                "text/css; charset=utf-8";
            case "js", "mjs" ->
                "text/javascript; charset=utf-8";
            case "json" ->
                "application/json; charset=utf-8";
            case "svg" ->
                "image/svg+xml";
            case "png" ->
                "image/png";
            case "jpg", "jpeg" ->
                "image/jpeg";
            case "gif" ->
                "image/gif";
            case "ico" ->
                "image/x-icon";
            case "woff2" ->
                "font/woff2";
            case "woff" ->
                "font/woff";
            case "ttf" ->
                "font/ttf";
            default ->
                "application/octet-stream";
        };
    }

    public void generate() throws Exception {
        for (PreviewPlugin plugin : plugins) {
            plugin.contribute(this);
        }
        var cl = getClass().getClassLoader();
        var wrapperBuilder = new DefaultObjectWrapperBuilder(Configuration.VERSION_2_3_32);
        wrapperBuilder.setExposeFields(true);
        var cfg = new Configuration(Configuration.VERSION_2_3_32);
        cfg.setObjectWrapper(wrapperBuilder.build());
        cfg.setOutputFormat(freemarker.core.HTMLOutputFormat.INSTANCE);
        var loaders = new ArrayList<TemplateLoader>();
        for (String base : templateBases()) {
            loaders.add(new ClassTemplateLoader(cl, base));
        }
        // last: the theme chain wins, provider templates are the fallback
        loaders.add(new ClassTemplateLoader(cl, THEME_RESOURCES_TEMPLATES));
        cfg.setTemplateLoader(new MultiTemplateLoader(loaders.toArray(TemplateLoader[]::new)));

        var properties = new Properties();
        for (String resource : propertyResources()) {
            try (InputStream is = cl.getResourceAsStream(resource)) {
                Assertions.assertNotNull(is, resource + " not on test classpath");
                properties.load(new InputStreamReader(is, StandardCharsets.UTF_8));
            }
        }
        properties.putAll(propertyOverrides);
        if (locale == null) {
            locale = deducedLocale();
        }
        var messages = new Properties();
        // provider bundles first so the theme chain overlays them
        for (Enumeration<java.net.URL> e = cl.getResources(String.format(THEME_RESOURCES_MESSAGES, locale.getLanguage())); e.hasMoreElements();) {
            try (InputStream is = e.nextElement().openStream()) {
                messages.load(new InputStreamReader(is, StandardCharsets.UTF_8));
            }
        }
        for (Bundle bundle : messageBundles()) {
            try (InputStream is = cl.getResourceAsStream(bundle.resource())) {
                if (is == null && bundle.optional()) {
                    continue;
                }
                Assertions.assertNotNull(is, bundle.resource() + " not on test classpath");
                messages.load(new InputStreamReader(is, StandardCharsets.UTF_8));
            }
        }

        var outDir = Path.of("target", outputDirectory != null ? outputDirectory : "theme-preview");
        Files.createDirectories(outDir);
        // copy parent resources first so child theme files win; then stub the
        // importmap module so the webauthn scripts resolve
        var copyBases = new ArrayList<>(templateBases());
        Collections.reverse(copyBases);
        // provider resources first so theme files of the same name win
        copyResources(cl, THEME_RESOURCES_RESOURCES, outDir.resolve("resources"));
        for (String base : copyBases) {
            copyResources(cl, base + "/resources", outDir.resolve("resources"));
        }
        var rfc4648 = outDir.resolve(Path.of("resources", "vendor", "rfc4648", "rfc4648.js"));
        Files.createDirectories(rfc4648.getParent());
        try (InputStream is = cl.getResourceAsStream(RFC4648_STUB_RESOURCE)) {
            Assertions.assertNotNull(is, RFC4648_STUB_RESOURCE + " not on classpath");
            Files.copy(is, rfc4648, StandardCopyOption.REPLACE_EXISTING);
        }

        var pages = discoverPages(cl);
        for (String page : pages) {
            render(cfg, properties, messages, outDir, page, page, stockFixture(page));
        }
        // automatic variant: login with validation errors (aria-invalid styling)
        render(cfg, properties, messages, outDir, "login", "login-invalid", invalidLoginFixture());
        var steps = new ArrayList<>(stepPages);
        steps.addAll(discoverStepPages(cl, pages));
        for (StepPage step : steps) {
            render(cfg, properties, messages, outDir, step.template(), step.outputName(), step.model());
        }
        writeIndex(cfg, outDir);
    }

    private List<String> discoverPages(ClassLoader cl) throws IOException {
        var pages = listTemplates(cl, "theme/base/login");
        Assertions.assertFalse(pages.isEmpty(), "base login templates not on test classpath");
        return pages;
    }

    /**
     * The theme chain's own pages, minus the stock ones it overrides: a template that calls the
     * layout macro is a page, anything else in the directory is a partial (template.ftl and the
     * head/card includes) and renders nothing on its own.
     */
    private List<StepPage> discoverStepPages(ClassLoader cl, List<String> stockPages) throws IOException {
        var registered = stepPages.stream().map(StepPage::template).toList();
        var discovered = new ArrayList<StepPage>();
        for (String base : templateBases()) {
            if (base.equals("theme/base/login")) {
                continue;
            }
            for (String name : listTemplates(cl, base)) {
                if (stockPages.contains(name) || registered.contains(name) || discovered.stream().anyMatch(s -> s.template().equals(name))) {
                    continue;
                }
                try (InputStream is = cl.getResourceAsStream(base + "/" + name + ".ftl")) {
                    if (is == null || !new String(is.readAllBytes(), StandardCharsets.UTF_8).contains("registrationLayout")) {
                        continue;
                    }
                }
                discovered.add(new StepPage(name));
            }
        }
        return discovered;
    }

    private List<String> listTemplates(ClassLoader cl, String base) throws IOException {
        var url = cl.getResource(base);
        if (url == null) {
            return List.of();
        }
        var external = url.toString();
        if (external.startsWith("jar:")) {
            var prefix = base + "/";
            try (var jar = jarOf(external)) {
                return jar.stream()
                        .map(ZipEntry::getName)
                        .filter(n -> n.startsWith(prefix) && n.endsWith(".ftl") && n.indexOf('/', prefix.length()) < 0)
                        .map(n -> n.substring(prefix.length(), n.length() - 4))
                        .filter(n -> !NON_PAGES.contains(n))
                        .sorted()
                        .toList();
            }
        }
        try (var walk = Files.list(Path.of(URI.create(external)))) {
            return walk
                    .map(f -> f.getFileName().toString())
                    .filter(n -> n.endsWith(".ftl"))
                    .map(n -> n.substring(0, n.length() - 4))
                    .filter(n -> !NON_PAGES.contains(n))
                    .sorted()
                    .toList();
        }
    }

    private Map<String, Object> baseModel(Properties properties, Properties messages, String pageId) {
        var model = new HashMap<String, Object>();
        model.put("properties", properties);
        model.put("pageId", pageId);
        model.put("lang", locale.toLanguageTag());
        model.put("title", "Sign in");
        model.put("scripts", new LinkedList<String>());

        var realm = new HashMap<String, Object>();
        realm.put("displayName", "OptionFactory");
        realm.put("displayNameHtml", "OptionFactory");
        realm.put("internationalizationEnabled", true);
        realm.put("loginWithEmailAllowed", true);
        realm.put("registrationEmailAsUsername", false);
        realm.put("password", true);
        realm.put("registrationAllowed", true);
        realm.put("rememberMe", true);
        realm.put("resetPasswordAllowed", true);
        realm.put("duplicateEmailsAllowed", false);
        model.put("realm", realm);

        var url = new HashMap<String, Object>();
        url.put("loginAction", "#");
        url.put("loginUrl", "#");
        url.put("loginResetCredentialsUrl", "#");
        url.put("loginRestartFlowUrl", "#");
        url.put("registrationAction", "#");
        url.put("registrationUrl", "#");
        url.put("oauth", "#");
        url.put("oauthAction", "#");
        url.put("oauth2DeviceVerificationAction", "#");
        url.put("logoutConfirmAction", "#");
        // "./" so the module imports the stock templates build from resourcesPath resolve:
        // a bare "resources/js/x.js" is not a relative specifier and every page threw
        url.put("resourcesPath", "./resources");
        url.put("resourcesCommonPath", "./resources");
        url.put("ssoLoginInOtherTabsUrl", "about:blank");
        model.put("url", url);

        var localeBean = new Fixtures.LocaleBean();
        var en = new Fixtures.Supported();
        var it = new Fixtures.Supported();
        it.languageTag = "it";
        it.label = "Italiano";
        localeBean.supported = List.of(en, it);
        model.put("locale", localeBean);

        var msgParams = new HashMap<Object, Object>(model);
        msgParams.putAll(messages);
        model.put("msg", new org.keycloak.theme.beans.MessageFormatterMethod(locale, msgParams));
        model.put("advancedMsg", new org.keycloak.theme.beans.AdvancedMessageFormatterMethod(locale, messages));
        model.put("kcSanitize", new org.keycloak.theme.KeycloakSanitizerMethod());

        model.put("auth", authFixture());
        model.put("login", new Fixtures.Login());
        model.put("message", new Fixtures.Message());
        model.put("messagesPerField", new Fixtures.MessagesPerField());
        model.put("client", new Fixtures.Client());
        model.put("social", socialProvidersFixture());
        model.put("profile", profileFixture());
        model.put("otpLogin", otpLoginFixture());
        model.put("configuredOtpCredentials", otpLoginFixture());
        model.put("totp", new Fixtures.Totp());
        model.put("oauth", oauthFixture());
        model.put("recoveryAuthnCodesConfigBean", new Fixtures.RecoveryAuthnCodesConfig());
        model.put("recoveryAuthnCodesInputBean", Map.of("codeNumber", 4));
        model.put("authenticators", authenticatorsFixture());
        model.put("user", userFixture());
        model.put("code", "-preview-code-");
        model.put("logoutConfirm", new HashMap<>(Map.of(
                "code", "abc123",
                "skipLink", Boolean.FALSE
        )));
        // empty logoutRedirectUri keeps frontchannel-logout static (its redirect
        // script is emitted only when the uri is non-empty)
        model.put("logout", new HashMap<>(Map.of(
                "logoutRedirectUri", "",
                "clients", List.of(Map.of("name", "MyClient Web", "frontChannelLogoutUrl", "about:blank"))
        )));
        model.put("samlPost", Map.of("url", "#", "SAMLRequest", "PHNBTUw+", "relayState", "relay"));
        model.put("challenge", "\"preview-challenge\"");
        model.put("userVerification", "\"required\"");
        model.put("rpId", "\"example.com\"");
        model.put("createTimeout", "60");
        model.put("isUserIdentified", "false");
        model.put("execution", "exec-1");
        model.put("idpDisplayName", "Google");
        model.put("brokerContext", Map.of("username", "jdoe@example.com"));
        model.put("triggered_from_aia", Boolean.FALSE);
        return model;
    }

    // --- preconfigured stock-page fixtures ---

    private Consumer<Map<String, Object>> stockFixture(String page) {
        return switch (page) {
            case "login-password" ->
                m -> {
                    var auth = (Fixtures.Auth) m.get("auth");
                    auth.usernameShown = true;
                    auth.resetCredentialsShown = true;
                };
            case "login-update-password", "login-verify-email", "delete-account-confirm", "delete-credential", "login-recovery-authn-code-config",
                 "login-update-profile", "update-email", "webauthn-error" ->
                m -> {
                    m.put("isAppInitiatedAction", Boolean.TRUE);
                    var auth = (Fixtures.Auth) m.get("auth");
                    auth.usernameShown = true;
                    if (page.equals("delete-credential")) {
                        m.put("credentialLabel", "YubiKey 5");
                    }
                    if (page.equals("delete-account-confirm")) {
                        // renders the aia cancel button (this page guards on triggered_from_aia)
                        m.put("triggered_from_aia", Boolean.TRUE);
                    }
                };
            case "login-reset-password" ->
                m -> m.put("message", invalidMessage());
            case "login-x509-info" ->
                m -> m.put("x509", Map.of("formData", Map.of(
                "subjectDN", "CN=John Doe, O=Acme, C=IT",
                "username", "jdoe",
                "isUserEnabled", true
        )));
            case "login-oauth2-device-verify-user-code", "code" ->
                m -> m.put("code", deviceCode());
            case "login-idp-link-confirm", "login-idp-link-confirm-override", "login-idp-link-email", "link-idp-action" ->
                m -> {
                    m.put("idpDisplayName", "Google");
                    if (page.equals("link-idp-action")) {
                        // renders the aia cancel button
                        m.put("isAppInitiatedAction", Boolean.TRUE);
                    }
                };
            case "select-authenticator" ->
                m -> m.put("auth", authenticationSelectionsFixture());
            case "select-organization" ->
                m -> m.put("user", userFixture());
            case "register" ->
                m -> {
                    m.put("termsAcceptanceRequired", Boolean.TRUE);
                    var errors = new Fixtures.MessagesPerField();
                    errors.errors = Set.of("password", "password-confirm");
                    m.put("messagesPerField", errors);
                };
            case "info" ->
                m -> {
                    var message = new Fixtures.Message();
                    message.type = "success";
                    message.summary = "Your account has been updated.";
                    m.put("message", message);
                    m.put("requiredActions", List.of("UPDATE_PASSWORD"));
                };
            case "error" ->
                m -> m.put("message", invalidMessage());
            case "webauthn-register" ->
                m -> {
                    // isSetRetry stays unset: the aia cancel form renders only when it is missing
                    // render the AIA branch (cancel below the register button)
                    m.put("isAppInitiatedAction", Boolean.TRUE);
                    m.put("username", "jdoe");
                    m.put("challenge", "\"reg-challenge\"");
                    m.put("userid", "\"dXNlci1pZA==\"");
                    m.put("rpEntityName", "\"example.com\"");
                    m.put("sigAlg", "-7");
                    m.put("signatureAlgorithms", List.of("-7", "-257"));
                    m.put("createTimeout", "0");
                    m.put("excludeCredentialIds", "\"\"");
                    m.put("authenticatorAttachment", "null");
                    m.put("requireResidentKey", "false");
                    m.put("residentKey", "null");
                    m.put("userVerificationRequirement", "\"preferred\"");
                    m.put("attestationConveyancePreference", "\"none\"");
                };
            case "passkeys", "login-passkeys-conditional-authenticate" ->
                m -> m.put("username", "jdoe");
            case "login-otp", "login-reset-otp" ->
                m -> {
                    var otpLogin = otpLoginFixture();
                    m.put("otpLogin", otpLogin);
                    m.put("configuredOtpCredentials", otpLogin);
                    if (page.equals("login-reset-otp")) {
                        var credentials = new ArrayList<>(otpLogin.userOtpCredentials);
                        m.put("otpCredentials", credentials);
                        m.put("otpCredential", credentials.get(0));
                    }
                };
            case "login-config-totp" ->
                m -> {
                    m.put("mode", "manual");
                    // renders the aia cancel button
                    m.put("isAppInitiatedAction", Boolean.TRUE);
                    ((Fixtures.Auth) m.get("auth")).usernameShown = true;
                };
            default ->
                m -> {
                };
        };
    }

    private Consumer<Map<String, Object>> invalidLoginFixture() {
        return m -> {
            m.put("message", invalidMessage());
            var errors = new Fixtures.MessagesPerField();
            errors.errors = Set.of("username", "password");
            m.put("messagesPerField", errors);
            var login = new Fixtures.Login();
            login.rememberMe = false;
            m.put("login", login);
        };
    }

    private static Fixtures.Message invalidMessage() {
        var message = new Fixtures.Message();
        message.type = "error";
        message.summary = "Invalid username or password.";
        return message;
    }

    private static Map<String, Object> deviceCode() {
        var code = new HashMap<String, Object>();
        code.put("code", "WDJB-MJHT");
        code.put("success", Boolean.TRUE);
        code.put("error", null);
        return code;
    }

    private static Fixtures.Auth authFixture() {
        var auth = new Fixtures.Auth();
        auth.authenticationSelections = List.of();
        return auth;
    }

    private static Fixtures.Auth authenticationSelectionsFixture() {
        var auth = new Fixtures.Auth();
        var password = new Fixtures.AuthenticationSelection();
        password.authExecId = "exec-password";
        password.displayName = "password-display-name";
        password.helpText = "password-help-text";
        password.iconCssClass = "kcWebAuthnKeyIcon";
        var otp = new Fixtures.AuthenticationSelection();
        otp.authExecId = "exec-otp";
        otp.displayName = "otp-display-name";
        otp.helpText = "otp-help-text";
        otp.iconCssClass = "kcWebAuthnDefaultIcon";
        auth.authenticationSelections = List.of(password, otp);
        return auth;
    }

    private static Fixtures.SocialProviders socialProvidersFixture() {
        var social = new Fixtures.SocialProviders();
        var google = new Fixtures.SocialProvider();
        var github = new Fixtures.SocialProvider();
        github.alias = "github";
        github.displayName = "GitHub";
        github.iconClasses = "bi bi-github";
        social.providers = List.of(google, github);
        return social;
    }

    private static Fixtures.Profile profileFixture() {
        var profile = new Fixtures.Profile();
        var username = new Fixtures.Attribute();
        username.name = "username";
        username.displayName = "${username}";
        username.value = "jdoe";
        var email = new Fixtures.Attribute();
        email.name = "email";
        email.displayName = "${email}";
        email.value = "jdoe@example.com";
        var firstName = new Fixtures.Attribute();
        firstName.name = "firstName";
        firstName.displayName = "${firstName}";
        firstName.value = "John";
        var group = new Fixtures.Group();
        group.name = "contact";
        group.displayHeader = "Contact information";
        var phone = new Fixtures.Attribute();
        phone.name = "phoneNumber";
        phone.displayName = "Phone number";
        phone.group = group;
        profile.attributes = List.of(username, email, firstName, phone);
        return profile;
    }

    private static Fixtures.OtpLogin otpLoginFixture() {
        var otpLogin = new Fixtures.OtpLogin();
        var second = new Fixtures.OtpCredential();
        second.id = "cred-2";
        second.userLabel = "Backup key";
        otpLogin.userOtpCredentials = List.of(new Fixtures.OtpCredential(), second);
        otpLogin.selectedCredentialId = "cred-1";
        return otpLogin;
    }

    private static Fixtures.OAuth oauthFixture() {
        var oauth = new Fixtures.OAuth();
        var email = new Fixtures.ClientScope();
        email.consentScreenText = "consent.client-scope.email";
        var org = new Fixtures.ClientScope();
        org.consentScreenText = "consent.client-scope.organization";
        org.dynamicScope = true;
        org.parameterizedScopeParameter = "acme-corp";
        oauth.clientScopesRequested = List.of(email, org);
        return oauth;
    }

    private static Fixtures.Authenticators authenticatorsFixture() {
        var authenticators = new Fixtures.Authenticators();
        var second = new Fixtures.AuthenticatorInfo();
        second.credentialId = "webauthn-2";
        second.label = "iPhone";
        second.transports = new Fixtures.Transports();
        second.transports.iconClass = "kcWebAuthnInternal";
        authenticators.authenticators = List.of(new Fixtures.AuthenticatorInfo(), second);
        return authenticators;
    }

    private static Fixtures.User userFixture() {
        var user = new Fixtures.User();
        var acme = new Fixtures.Organization();
        var globex = new Fixtures.Organization();
        globex.alias = "globex";
        globex.name = "Globex";
        user.organizations = List.of(acme, globex);
        return user;
    }

    // --- rendering ---

    private void render(Configuration cfg, Properties properties, Properties messages, Path outDir, String page, String outputName, Consumer<Map<String, Object>> customizer) throws Exception {
        var model = baseModel(properties, messages, page);
        customizer.accept(model);
        var template = cfg.getTemplate(page + ".ftl");
        var sw = new StringWriter();
        template.process(model, sw);
        var html = sw.toString();
        Assertions.assertTrue(!html.isEmpty(), page + " rendered empty");
        Assertions.assertTrue(html.contains("data-page-id"), page + " missing layout wrapper");
        Files.writeString(outDir.resolve(outputName + ".html"), html, StandardCharsets.UTF_8);
    }

    /**
     * The custom properties the previewed stylesheets declare in a {@code :root} block, grouped
     * by stylesheet, the one that wins the cascade first: a theme's own sheet is linked after the
     * library's, so it is what a reader wants at the top, and a token it redeclares is listed
     * under it alone rather than twice. The stylesheets and their order are taken from the
     * {@code <link>} elements of a page that was just rendered - the only account of what the
     * preview actually loads, so a sheet the theme does not link contributes nothing. Only
     * {@code :root} declarations: a {@code --bs-btn-*} set on a component is that component's
     * plumbing, not a knob of the theme. Feeds the gallery's editor, which overrides them live in
     * every frame.
     */
    private List<Map<String, Object>> cssVariableGroups(Path outDir, String page) throws IOException {
        var rendered = outDir.resolve(page + ".html");
        if (!Files.exists(rendered)) {
            return List.of();
        }
        var links = Pattern.compile("<link[^>]+href=\"\\.?/?(resources/[^\"]+\\.css)\"").matcher(Files.readString(rendered, StandardCharsets.UTF_8));
        var sheets = new ArrayList<String>();
        while (links.find()) {
            if (!sheets.contains(links.group(1))) {
                sheets.add(links.group(1));
            }
        }
        var roots = Pattern.compile(":root\\s*\\{([^}]*)\\}");
        var declarations = Pattern.compile("(--[\\w-]+)\\s*:\\s*([^;]+);");
        // by name, so the last declaration wins exactly as the cascade has it
        var effective = new LinkedHashMap<String, Map<String, Object>>();
        for (String sheet : sheets) {
            var file = outDir.resolve(sheet);
            if (!Files.exists(file)) {
                continue;
            }
            // comments first: this stylesheet documents tokens by naming them in prose
            var text = Files.readString(file, StandardCharsets.UTF_8).replaceAll("(?s)/\\*.*?\\*/", "");
            var blocks = roots.matcher(text);
            while (blocks.find()) {
                var block = declarations.matcher(blocks.group(1));
                while (block.find()) {
                    var value = block.group(2).trim();
                    effective.put(block.group(1), new HashMap<>(Map.of(
                            "name", block.group(1),
                            "value", value,
                            "file", sheet.substring("resources/".length()),
                            "color", value.matches("#[0-9a-fA-F]{3,8}")
                    )));
                }
            }
        }
        var byFile = new LinkedHashMap<String, List<Map<String, Object>>>();
        for (Map<String, Object> variable : effective.values()) {
            byFile.computeIfAbsent((String) variable.get("file"), f -> new ArrayList<>()).add(variable);
        }
        // the link order, reversed: whichever sheet had the last word comes first
        var winnerFirst = new ArrayList<>(sheets.stream().map(sheet -> sheet.substring("resources/".length())).toList());
        Collections.reverse(winnerFirst);
        return winnerFirst.stream()
                .filter(byFile::containsKey)
                .map(file -> (Map<String, Object>) new HashMap<String, Object>(Map.of("file", file, "variables", byFile.get(file))))
                .toList();
    }

    private void writeIndex(Configuration cfg, Path outDir) throws Exception {
        var pages = new ArrayList<String>();
        try (var list = Files.list(outDir)) {
            list.filter(p -> p.getFileName().toString().endsWith(".html"))
                    .filter(p -> !p.getFileName().toString().equals("index.html"))
                    .sorted(Comparator.comparing(p -> p.getFileName().toString()))
                    .forEach(p -> pages.add(p.getFileName().toString().replace(".html", "")));
        }
        var cl = getClass().getClassLoader();
        String tplSrc;
        try (InputStream is = cl.getResourceAsStream("preview-index.ftl")) {
            Assertions.assertNotNull(is, "preview-index.ftl not on classpath");
            tplSrc = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
        var tpl = new Template("preview-index", new StringReader(tplSrc), cfg);
        var sw = new StringWriter();
        var model = new HashMap<String, Object>();
        model.put("pages", pages);
        model.put("title", title != null ? title : themeChain.getFirst() + " theme preview");
        model.put("renderer", getClass().getSimpleName());
        model.put("outputDirectory", outputDirectory != null ? outputDirectory : "theme-preview");
        model.put("variableGroups", pages.isEmpty() ? List.of() : cssVariableGroups(outDir, pages.getFirst()));
        tpl.process(model, sw);
        Files.writeString(outDir.resolve("index.html"), sw.toString(), StandardCharsets.UTF_8);
    }

    // --- resource copying (directory and jar classpath entries) ---

    private static JarFile jarOf(String jarUrl) throws IOException {
        return new JarFile(Path.of(jarUrl.substring("jar:file:".length(), jarUrl.indexOf('!'))).toFile());
    }

    private static void copyResources(ClassLoader cl, String base, Path target) throws IOException {
        var urls = new ArrayList<java.net.URL>();
        for (Enumeration<java.net.URL> e = cl.getResources(base); e.hasMoreElements();) {
            urls.add(e.nextElement());
        }
        if (urls.isEmpty()) {
            return;
        }
        var prefix = base.endsWith("/") ? base : base + "/";
        for (java.net.URL url : urls) {
            var external = url.toString();
            if (external.startsWith("jar:")) {
                try (var jar = jarOf(external)) {
                    var entries = jar.entries();
                    while (entries.hasMoreElements()) {
                        JarEntry entry = entries.nextElement();
                        var name = entry.getName();
                        if (entry.isDirectory() || !name.startsWith(prefix)) {
                            continue;
                        }
                        var relative = name.substring(prefix.length());
                        var destination = target.resolve(relative);
                        Files.createDirectories(destination.getParent());
                        try (InputStream is = jar.getInputStream(entry)) {
                            Files.copy(is, destination, StandardCopyOption.REPLACE_EXISTING);
                        }
                    }
                }
            } else {
                var dir = Path.of(URI.create(external));
                try (var walk = Files.walk(dir)) {
                    walk.forEach(source -> {
                        try {
                            if (Files.isDirectory(source)) {
                                return;
                            }
                            var destination = target.resolve(dir.relativize(source).toString());
                            Files.createDirectories(destination.getParent());
                            Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }
            }
        }
    }
}
