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
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
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
 * All stock-page fixtures are preconfigured: the only knobs are where the
 * theme lives (inheritance chain, child-first for templates, parent-first for
 * property/message merging) and locale/title/output naming. Reusable by login
 * theme modules: depend on this module (test scope), configure, generate - no
 * subclassing:
 *
 * <pre>
 * LoginThemePreviewGenerator.preview()
 *         .templateResourceBases("theme/mytheme/login", "theme/base/login")
 *         .propertiesResources("theme/base/login/theme.properties", "theme/mytheme/login/theme.properties")
 *         .messagesResources("theme/base/login/messages/messages_it.properties", "theme/mytheme/login/messages/messages_it.properties")
 *         .locale(Locale.ITALIAN)
 *         .title("mytheme preview")
 *         .generate();
 * </pre>
 *
 * The gallery must be served over http (module scripts are blocked from
 * file:// origins): python3 -m http.server -d target/theme-preview
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

    private final List<String> templateResourceBases = new ArrayList<>(List.of("theme/base/login"));
    private final List<String> propertiesResources = new ArrayList<>(List.of("theme/base/login/theme.properties"));
    private final List<String> messagesResources = new ArrayList<>(List.of("theme/base/login/messages/messages_en.properties"));
    private final List<Render> extraRenders = new ArrayList<>();
    private Locale locale = Locale.ENGLISH;
    private String title = "login theme preview";
    private String outputDirectory = "theme-preview";

    private record Render(String page, String outputName, Consumer<Map<String, Object>> customizer) {
    }

    public static LoginThemePreviewGenerator preview() {
        return new LoginThemePreviewGenerator();
    }

    /**
     * Resource bases of the loader chain, child theme first: the first
     * template found wins, mirroring keycloak theme inheritance. Replaces the
     * default (base-only).
     */
    public LoginThemePreviewGenerator templateResourceBases(String... bases) {
        this.templateResourceBases.clear();
        Collections.addAll(this.templateResourceBases, bases);
        return this;
    }

    /**
     * theme.properties locations to merge, parent first: later loads override
     * earlier ones, mirroring keycloak's per-key merge. Replaces the default
     * (base-only).
     */
    public LoginThemePreviewGenerator propertiesResources(String... resources) {
        this.propertiesResources.clear();
        Collections.addAll(this.propertiesResources, resources);
        return this;
    }

    /**
     * Message bundle locations to merge, parent first. Replaces the default
     * (base-only).
     */
    public LoginThemePreviewGenerator messagesResources(String... resources) {
        this.messagesResources.clear();
        Collections.addAll(this.messagesResources, resources);
        return this;
    }

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

    /**
     * Additional render (e.g. a custom step page of the theme, or a variant
     * of a stock page), emitted after the discovered pages.
     */
    public LoginThemePreviewGenerator extraRender(String page, String outputName, Consumer<Map<String, Object>> customizer) {
        this.extraRenders.add(new Render(page, outputName, customizer));
        return this;
    }

    public void generate() throws Exception {
        var cl = getClass().getClassLoader();
        var wrapperBuilder = new DefaultObjectWrapperBuilder(Configuration.VERSION_2_3_32);
        wrapperBuilder.setExposeFields(true);
        var cfg = new Configuration(Configuration.VERSION_2_3_32);
        cfg.setObjectWrapper(wrapperBuilder.build());
        cfg.setOutputFormat(freemarker.core.HTMLOutputFormat.INSTANCE);
        var loaders = new ArrayList<TemplateLoader>();
        for (String base : templateResourceBases) {
            loaders.add(new ClassTemplateLoader(cl, base));
        }
        cfg.setTemplateLoader(new MultiTemplateLoader(loaders.toArray(TemplateLoader[]::new)));

        var properties = new Properties();
        for (String resource : propertiesResources) {
            try (InputStream is = cl.getResourceAsStream(resource)) {
                Assertions.assertNotNull(is, resource + " not on test classpath");
                properties.load(new InputStreamReader(is, StandardCharsets.UTF_8));
            }
        }
        var messages = new Properties();
        for (String resource : messagesResources) {
            try (InputStream is = cl.getResourceAsStream(resource)) {
                Assertions.assertNotNull(is, resource + " not on test classpath");
                messages.load(new InputStreamReader(is, StandardCharsets.UTF_8));
            }
        }

        var outDir = Path.of("target", outputDirectory);
        Files.createDirectories(outDir);
        // copy parent resources first so child theme files win; then stub the
        // importmap module so the webauthn scripts resolve
        var copyBases = new ArrayList<>(templateResourceBases);
        Collections.reverse(copyBases);
        for (String base : copyBases) {
            copyResources(cl, base + "/resources", outDir.resolve("resources"));
        }
        var rfc4648 = outDir.resolve(Path.of("resources", "vendor", "rfc4648", "rfc4648.js"));
        Files.createDirectories(rfc4648.getParent());
        try (InputStream is = cl.getResourceAsStream(RFC4648_STUB_RESOURCE)) {
            Assertions.assertNotNull(is, RFC4648_STUB_RESOURCE + " not on classpath");
            Files.copy(is, rfc4648, StandardCopyOption.REPLACE_EXISTING);
        }

        for (String page : discoverPages(cl)) {
            render(cfg, properties, messages, outDir, page, page, stockFixture(page));
        }
        // automatic variant: login with validation errors (aria-invalid styling)
        render(cfg, properties, messages, outDir, "login", "login-invalid", invalidLoginFixture());
        for (Render extra : extraRenders) {
            render(cfg, properties, messages, outDir, extra.page(), extra.outputName(), extra.customizer());
        }
        writeIndex(cfg, outDir);
    }

    private List<String> discoverPages(ClassLoader cl) throws IOException {
        var url = cl.getResource("theme/base/login/login.ftl");
        Assertions.assertNotNull(url, "base login templates not on test classpath");
        var external = url.toString();
        if (external.startsWith("jar:")) {
            try (var jar = jarOf(external)) {
                return jar.stream()
                        .map(ZipEntry::getName)
                        .filter(n -> n.startsWith("theme/base/login/") && n.endsWith(".ftl"))
                        .map(n -> n.substring("theme/base/login/".length(), n.length() - 4))
                        .filter(n -> !NON_PAGES.contains(n))
                        .sorted()
                        .toList();
            }
        }
        var dir = Path.of(URI.create(external)).getParent();
        try (var walk = Files.list(dir)) {
            return walk
                    .map(p -> p.getFileName().toString())
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
        url.put("resourcesPath", "resources");
        url.put("resourcesCommonPath", "resources");
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
                "clients", List.of(Map.of("name", "SecureMail Web", "frontChannelLogoutUrl", "about:blank"))
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
            case "login-update-password", "login-verify-email", "delete-account-confirm", "delete-credential", "login-recovery-authn-code-config" ->
                m -> {
                    m.put("isAppInitiatedAction", Boolean.TRUE);
                    var auth = (Fixtures.Auth) m.get("auth");
                    auth.usernameShown = true;
                    if (page.equals("delete-credential")) {
                        m.put("credentialLabel", "YubiKey 5");
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
                m -> m.put("idpDisplayName", "Google");
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
                    m.put("isSetRetry", Boolean.FALSE);
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
                m -> m.put("mode", "manual");
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
        model.put("title", title);
        model.put("renderer", getClass().getSimpleName());
        model.put("outputDirectory", outputDirectory);
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
