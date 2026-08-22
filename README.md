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

Authentication flow building blocks: `clear-user` (drops the user from the flow context), conditional authenticators (`conditional-auth-note`, `conditional-client-id`, `conditional-client-role-attribute`, `conditional-user-group`, `conditional-user-has-federation-link`) and the `opfa-sms-client` spi with an aws sns implementation and a placebo client for testing:

```properties
spi-opfa-sms-client--opfa-sms-client--type=sns
spi-opfa-sms-client--opfa-sms-client--clientId=...
spi-opfa-sms-client--opfa-sms-client--clientSecret=...
spi-opfa-sms-client--opfa-sms-client--region=eu-west-1
# optional sender id
spi-opfa-sms-client--opfa-sms-client--senderId=...
```

`type=placebo` (the default) logs the message instead of sending it.

## optionfactory-keycloak-idp-apple

Sign in with Apple identity provider (`opfa-apple`). The p8 private key goes in the `clientSecret` config field (bare base64 or pem), with `keyId` and `teamId` as additional config keys. Id-token signatures are validated via apple's jwks; the server must reach `https://appleid.apple.com/auth/keys`.

## optionfactory-keycloak-ldap

Ldap federation helpers: `caching-group-ldap-mapper` (an `LDAP_ONLY` group mapper that also stores memberships in the keycloak database for fast querying) and `reset-lockout-time-on-update-credentials` (resets the Active Directory `lockoutTime` attribute when a federated user updates their password).

## optionfactory-keycloak-themes

`opfa-freemarker-configurable`, a login forms provider that expands `${conf.*}` placeholders in theme properties (e.g. `theme.properties`, message bundles) from server configuration:

```properties
# keycloak.conf
login-theme-conf--support-mail=support@example.com
login-theme-conf--footer-url=https://example.com
```

```properties
# theme/mytheme/login/theme.properties
supportMail=${conf.support-mail}
footerUrl=${conf.footer-url}
```

Templates then use the theme properties as usual (`${properties.footerUrl}`). Also ships the `opfablank` welcome theme.

## optionfactory-keycloak-themes-bootstrap

A `bootstrap` login theme with a blank, fully overridable template for building custom login pages.