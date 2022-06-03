## optionfactory-keycloak-resources-auth
A resource authentication provider to enforce valid auth and scope in exposed resources

Usage: 
include the artifact as a dependency with `provided` scope
```xml
<dependency>
    <groupId>net.optionfactory.keycloak</groupId>
    <artifactId>optionfactory-keycloak-resources-auth</artifactId>
    <version>${optionfactory.keycloak.version}</version>
    <scope>provided</scope>
</dependency>        
```
get the provider from session and use it to control access to the resource:

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


## optionfactory-keycloak-validation
javax validation provider 
Usage:
include the artifact as a dependency with `provided` scope
```xml
<dependency>
    <groupId>net.optionfactory.keycloak</groupId>
    <artifactId>optionfactory-keycloak-validation</artifactId>
    <version>${optionfactory.keycloak.version}</version>
    <scope>provided</scope>
</dependency>        
```
get the provider from session and use it to validate beans or method calls:

```java
        final var validator = session.getProvider(RequestValidator.class);
        validator.enforce(request, BadRequestException::new);
```


## optionfactory-keycloak-provisioning-api
A search and provisioning api


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