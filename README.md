# optionfactory-keycloak

Keycloak extensions maintained by OptionFactory: resource authentication helpers, provisioning/inspection apis, a cid-embedding email sender, an apple identity provider, sms otp plumbing, an online-access token flow and remoting utilities. All modules are deployed as keycloak providers (`provided` scope dependencies for embedders).

See [CHANGELOG.md](CHANGELOG.md) for release notes.

## optionfactory-keycloak-providers

Shared utilities: `ResourceAuthenticator` (bearer/scope enforcement for exposed resources), the filtering/pagination stack used by the apis, and `Groups` helpers.

Usage: include the artifact as a dependency with `provided` scope
```xml
<dependency>
    <groupId>net.optionfactory.keycloak</groupId>
    <artifactId>optionfactory-keycloak-providers</artifactId>
    <version>${optionfactory.keycloak.version}</version>
    <scope>provided</scope>
</dependency>        
```
get the authenticator from session and use it to control access to the resource:

```java
    @Override
    public RealmResourceProvider create(KeycloakSession session) {
        session.getProvider(ResourceAuthenticator.class).enforceScope("myscope");

        return new RealmResourceProvider() {
            @Override
            public Object getResource() {
                return new MyEndpoints(session);
            }

            @Override
            public void close() {

            }
        };
    }
```


## optionfactory-keycloak-api-provisioning

A search and provisioning api: `/admin/realms/{realm}/inspection/...` (filterable user/group queries) and `/admin/realms/{realm}/provisioning/...` (user provide/patch/wipe, group by-path management). Access is gated by the admin permission evaluator (`users().requireView()` / `requireManage()`).


## optionfactory-keycloak-email-sender

A replacement email sender allowing CID attachments.

### Usage:

1. enable by replacing the default email sender provider in `keycloak.conf`:

```properties
spi-email-sender-provider=opfa-cid-embedding
```

2. create a allowed_cids.json in your email theme resources folder, e.g:
filename: `theme/mytheme/email/resources/allowed_cids.json`
```json
[{
    "id": "logo",
    "source": "logo.png",
    "mimeType": "image/png"
},{
    "id": "header",
    "source": "header.png",
    "mimeType": "image/png"
}]
```
3. add cid attachments to your theme resources folder, e.g: 

filename: `theme/mytheme/email/resources/logo.png`
filename: `theme/mytheme/email/resources/header.png`

4. `cid`s referenced in your email templated and whitelisted in your `allowed_cids.json` are automatically added to the email, e.g:
filename: `theme/mytheme/email/html/executeActions.ftl`
```html
...
    <img src="cid:logo" alt="My Logo">
...

```

## optionfactory-keycloak-login-stats

An event listener recording users login stats into an attribute

### Usage:

1. Customize the used attribute name in `keycloak.conf` (defaults to `loginStats`):
```properties
spi-events-listener-opfa-login-stats-attribute=login-stats
```

2. Add the listener to `Manage` -> `Events` -> `Config` -> `Event Listeners` -> Add `opfa-login-stats`

The attribute value is `count:firstLoginTimestamp:lastLoginTimestamp` (epoch millis); malformed values are tolerated and counting restarts from defaults.

## optionfactory-keycloak-remoting

Http client utilities for provider-to-remote integration, including a multipart/form-data entity and header parameter encoding.

### HttpClients

Built through a fluent builder with independent trust and hostname verification:

```java
CloseableHttpClient client = HttpClients.builder("my-integration")
        .keyMaterial(HttpClients.KeyMaterial.fromPkcs12File(path, Optional.of(pass), Optional.of(keyPass))) // optional mutual tls
        .trustCertificatesIn(truststore)       // or trustSystem() (default) / trustAny() (no chain validation)
        .verifyHostnames()                     // default; or verifyHostnamesWith(verifier) / verifyNoHostname()
        .timeouts(Duration.ofSeconds(3), Duration.ofSeconds(30))
        .followRedirects()                     // opt-in: redirects are returned as-is by default
        .cookieStore(new BasicCookieStore())   // opt-in: no cookie retention by default
        .build();
```

By default the client does exactly what each request says: full tls verification, no redirect following, no cookie retention, no auth caching. Retries keep the httpclient default (idempotent requests only, never entity-enclosing ones).

## optionfactory-keycloak-online-access

Mints single-use login links from offline-access tokens: a client holding the `online-access` role posts an access token to `/realms/{realm}/online-access/action-token` and receives a short-lived link that logs the subject in and redirects. Enable in `keycloak.conf`:

```properties
online-access--enabled=true
```

(one key enables both the endpoints and the action-token handler)

## optionfactory-keycloak-authenticators

Authentication flow building blocks: `clear-user` (drops the user from the flow context), conditional authenticators (`conditional-auth-note`, `conditional-client-id`, `conditional-client-role-attribute`, `conditional-user-group`, `conditional-user-has-federation-link`), the `opfa-verify-email-otp` required action (code-based email verification) and the `opfa-impersonation` authenticator (cookie-backed identity swap, see `impersonation-spec.md`), plus the `opfa-sms-client` spi with an aws sns implementation and a placebo client for testing:

```properties
spi-opfa-sms-client--opfa-sms-client--type=sns
spi-opfa-sms-client--opfa-sms-client--clientId=...
spi-opfa-sms-client--opfa-sms-client--clientSecret=...
spi-opfa-sms-client--opfa-sms-client--region=eu-west-1
# optional sender id
spi-opfa-sms-client--opfa-sms-client--senderId=...
```

`type=placebo` (the default) logs the message instead of sending it.

The `opfa-verify-email-otp` required action replaces the built-in `VERIFY_EMAIL`: the email carries a numeric code instead of a link, and the user types it into the login tab that is still open, so mobile flows using custom tabs never lose the authentication session to the mail app's browser. Enable it as a default required action, disable the built-in `Verify Email`, turn the realm's "Verify email" login toggle off (the toggle makes the registration form defer the password until the email is verified, a mode that expects the built-in link flow and would leave users verified here without a password), and strip any `VERIFY_EMAIL` left on existing users (a required action without a provider fails the login); users that close the tab mid-verification simply log in again and are re-prompted (a still-valid code is accepted, no duplicate email). Settings, in the required action config: `codeLength` (6), `codeLifespanSeconds` (900), `maxAttempts` (5), `resendCooldownSeconds` (30). Codes are stored hashed in keycloak's single-use object store, are single-use, and are invalidated after `maxAttempts` failed submissions. The store protocol they rely on (hashed single-use codes, atomic attempt cap, resend cooldown) lives in `otp.OtpCodes` and is reusable for other one-time code flows, e.g. an sms-based required action on top of `opfa-sms-client`.

The `opfa-impersonation` authenticator swaps the browser identity when the authorization request carries `impersonate=<userId>` and restores the operator on `deimpersonate`: configure it as the first `ALTERNATIVE` of a cloned browser flow, bound as the browser-flow override of the target client only (full design, including the cross-app handoff contract, in `impersonation-spec.md`). Integration contract: the initiating app links only to the target app's canonical `/impersonate?target=<userId>` route, the banner's exit goes through `/exit-impersonation`, and the return destination is environment configuration of the target SPA, never a url parameter (no open redirect); the target SPA owns the whole OIDC round trip, appending the parameter to `keycloak.createLoginUrl(...)` output and stripping the address bar immediately. The operator is proven by the identity cookie (validated in-code, no flow-ordering dependency); parameters without a valid session, or held by a user without the configured `role` (default `opfa-impersonate`, distinct from keycloak's built-in `realm-management.impersonation` and `admin-impersonating` roles), fall through to a normal login with an `IMPERSONATE_ERROR` audit event, so a lingering parameter after a refresh degrades gracefully. Targets must exist, be enabled, not be service accounts, never hold the operator role itself (operators are not impersonable), optionally not hold `forbiddenRole`, and have no pending required actions nor an unverified email (else a warning page with a back link to `backUrl`/the client base URL; operators must never be challenged with the target's obligations). The swap sets the native `IMPERSONATOR_ID`/`IMPERSONATOR_USERNAME` session notes (built-in "Impersonator" mappers and introspection `act.sub` light up), fires `IMPERSONATE`, and removes the current session server-side so nothing is orphaned (a full backchannel logout is impossible mid-flow: the root auth session the flow executes in shares its id with the cookie user session and would be deleted underneath it, so the old session's access tokens live out their expiry while its refresh tokens die with the session); `deimpersonate` re-validates the operator (enabled + role) before restoring. Known trade-off: the parameter is ambient authority (any site can attempt it in an operator's browser; impact is a session swap, not token theft), accepted for internal tooling.

## optionfactory-keycloak-idp-apple

Sign in with Apple identity provider (`opfa-apple`). The p8 private key goes in the `clientSecret` config field (bare base64 or pem), with `keyId` and `teamId` as additional config keys. Id-token signatures are validated via apple's jwks; the server must reach `https://appleid.apple.com/auth/keys`.

## optionfactory-keycloak-ldap

Ldap federation helpers: `caching-group-ldap-mapper` (an `LDAP_ONLY` group mapper that also stores memberships in the keycloak database for fast querying) and `reset-lockout-time-on-update-credentials` (resets the Active Directory `lockoutTime` attribute when a federated user updates their password).

## optionfactory-keycloak-themes

`opfa-freemarker-configurable`, a login forms provider that expands `${conf.*}` placeholders from server configuration in **message bundles** (`messages_*.properties`, via `msg()`/`advancedMsg()`) and in **`theme.properties` values** (`${properties.*}`, keyed `styles.*`/`scripts.*`/`favicons.*`, `styles=`, `themeHeaders.*`/`baseHeaders.*`, `darkMode`). The `conf` map is also exposed to templates directly.

Values are sourced from `keycloak.conf` via MicroProfile Config: the quarkus distro exposes each entry with a `kc.` prefix, and the provider collects every property starting with `kc.login-theme-conf--` **once at factory startup**, stripping that prefix. Consequences: changes to `keycloak.conf` require a restart to take effect, and the provider works only on the quarkus distribution (no `kc.*` config exists on the legacy wildfly adapter). Also ships the `opfablank` welcome theme.

### Configuration

```properties
# keycloak.conf
login-theme-conf--support-mail=support@example.com
login-theme-conf--footer-url=https://example.com/area-riservata
login-theme-conf--analytics-id=GTM-ABC123
login-theme-conf--assistance-path=assistenza
```

Keys accept dashes (`analytics-id` → `${conf.analytics-id}`); the map is flat, so dotted keys read naturally in templates (`login-theme-conf--support.mail` → `${conf.support.mail}`).

### Examples

**Message bundles** — the typical i18n + per-environment case (support mail differs between staging and production, translations stay in the bundle):

```properties
# theme/mytheme/login/messages/messages_it.properties
needHelpMessage=Hai bisogno di aiuto? Scrivi a ${conf.support-mail}
privacyMessage=Leggi la <a href="${conf.footer-url}/privacy">privacy policy</a>
```

```ftl
${msg("needHelpMessage")}
```

**Templates, direct access** — no round-trip through properties or messages; read the map from the data model:

```ftl
<a id="footer-link" href="${conf.footer-url}">${msg("backToHome")}</a>
```

**theme.properties** — per-environment styling and head tags without rebuilding the theme jar; here the analytics script and the forgot-password/assistance entry points come from `keycloak.conf`:

```properties
# theme/mytheme/login/theme.properties
parent=bootstrap
styles=css/mytheme.css
themeHeaders.0=<script src="https://www.googletagmanager.com/gtm.js?id=${conf.analytics-id}" type="text/javascript"></script>
```

```ftl
# in a forked login.ftl: reuse the base url and append the conf-controlled path
<a href="${url.loginResetCredentialsUrl?replace("login-actions/reset-credentials", conf.assistancePath)}">Hai bisogno di assistenza?</a>
```

`${properties.*}` inherits the expansion too, so keycloak's own plumbing picks conf values up (e.g. `kcLogoLink=${conf.footer-url}`).

### Composition and fallback semantics

`StringPropertyReplacer` (the expansion engine) supports defaults (`${key:fallback}`), composite keys (`${key1,key2}`, both tried as keys — **not** literal fallbacks) and **recursive** substitution (a resolved value is re-scanned for further `${...}` refs, with an infinite-recursion guard). Practical patterns:

```properties
# messages: degrade explicitly when the conf key is absent
supportMail=${conf.support-mail:support@example.com}
footerMessage=Vai alla <a href="${conf.footer-url}">${conf.footer-label:Area Riservata}</a>
# theme.properties: key chain — use the env-specific id, fall back to a shared one
themeHeaders.0=<script src="https://www.googletagmanager.com/gtm.js?id=${conf.analytics-id,conf.shared-analytics-id}"></script>
```

Ordering and interaction of the layers:

1. **`${sys.*}`/`${env.*}`** — keycloak's own substitution, applied at **theme load** (before the provider runs). A resolved `${env.X}` becomes a literal before `${conf.*}` ever sees it, so the two never fight over the same placeholder. Unresolved refs stay for the next layer.
2. **`${conf.*}`** — expanded by this provider per request, in messages and theme.properties values. Unknown conf keys resolve to `null` and the **whole `${...}` ref is left verbatim** — use `${key:fallback}` for literal defaults, `${key1,key2}` to chain keys.
3. Plain `${someKey}` refs with no `conf.`/`sys.`/`env.` prefix also resolve to `null` here and stay verbatim — safe inside values that contain dollar-brace syntax meant for other layers.
4. Recursion: substituted values are re-scanned, so conf values may themselves reference `${conf.*}` keys; cycles throw `IllegalStateException` ("Infinite recursion") instead of hanging.

These semantics are pinned by unit tests in `optionfactory-keycloak-themes` (`ConfigurableFreemarkerLoginFormsProviderTest`).

### Bootstrapping

The provider registers via `META-INF/services` and wins default-provider selection over the stock freemarker factory (`order() = 1`), so deploying the jar activates it for **all** login themes of the server, including `keycloak` and `keycloak.v2` parents: stock themes are unaffected functionally (they carry no `${conf.*}` refs), but keep it in mind when reasoning about provider selection.

## optionfactory-keycloak-themes-preview

A harness that renders **every stock base login page** (discovered from the keycloak-themes jar, so new upstream pages are picked up automatically; transient relays like `saml-post-form` are excluded) through a login theme and writes an html gallery to `target/<name>/` — page-per-file plus an ftl-rendered `index.html` with lazy iframes. Rendering failures fail the build, so it doubles as a smoke test for template/data-model compatibility. All stock-page fixtures are preconfigured (including an automatic `login-invalid` variant); the only knobs are the theme inheritance chain, locale and naming. Provider-supplied `theme-resources` on the classpath (templates, resources and message bundles) are picked up with keycloak's precedence, so a derived theme's custom step pages preview with their own css, js and messages — render them with `extraRender`.

Theme modules reuse it by depending on this module (test scope) and configuring the generator — no subclassing:

```java
LoginThemePreviewGenerator.preview()
        .templateResourceBases( // child-first, mirrors theme inheritance
                "theme/mytheme/login",
                "theme/bootstrap/login",
                "theme/base/login")
        .propertiesResources( // parent-first, mirrors per-key merge
                "theme/base/login/theme.properties",
                "theme/bootstrap/login/theme.properties",
                "theme/mytheme/login/theme.properties")
        .messagesResources(
                "theme/base/login/messages/messages_it.properties",
                "theme/mytheme/login/messages/messages_it.properties")
        .locale(Locale.ITALIAN)
        .title("mytheme preview")
        .outputDirectory("theme-preview-mytheme")
        .extraRender("my-step-page", "my-step-page", m -> { /* custom step page fixtures */ })
        .generate();
```

The gallery must be served over http (module scripts are blocked from `file://` origins): `python3 -m http.server -d target/theme-preview-mytheme` (the hint is embedded in the generated index). Theme resources referenced by `resourcesPath` are copied next to the output (jar classpath entries included) so css/js/images load; the `rfc4648` importmap module is satisfied by a stub. Caveat: page-specific beans are fixtures, not keycloak internals — templates reading model keys the fixtures don't provide fail loudly (which is the point).

In use: `optionfactory-keycloak-themes-bootstrap` previews itself this way (`BootstrapThemePreviewTest`), as do the downstream themes in the creditagricole/homeinsurance (both login themes + custom step pages) and innovabay/sygmund (custom consents/survey pages) projects.

## optionfactory-keycloak-themes-bootstrap

A `bootstrap` login theme: upstream `base` templates with all `kc*Class` properties mapped to bootstrap 5 classes, CDN-loaded bootstrap (SRI-pinned), and a head-injection contract (`googleFonts`/`baseHeaders`/`themeHeaders`) for derived themes. The template is upstream plus purely additive marked hunks (byte-identical macro signature), so keycloak upgrades are a re-diff. Theming happens via a small set of `--opfa-*` tokens plus the `--bs-*` variables bootstrap itself reads (no `@layer`: consumer sheets load last and win at equal specificity); an optional `cards=true` two-column layout ships with an overridable `left-card.ftl` and a per-page `footerCard` section. See the [module readme](optionfactory-keycloak-themes-bootstrap/README.md) for the full contract.