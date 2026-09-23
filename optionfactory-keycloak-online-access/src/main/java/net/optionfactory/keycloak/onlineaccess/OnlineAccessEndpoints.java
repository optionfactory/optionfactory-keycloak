package net.optionfactory.keycloak.onlineaccess;

import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.optionfactory.keycloak.providers.Conf;
import net.optionfactory.keycloak.providers.model.Models;
import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.TokenVerifier;
import org.keycloak.TokenVerifier.Predicate;
import org.keycloak.common.VerificationException;
import org.keycloak.common.util.Time;
import org.keycloak.crypto.SignatureProvider;
import org.keycloak.models.ClientModel;
import org.keycloak.models.Constants;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.protocol.oidc.utils.RedirectUtils;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.JsonWebToken;
import org.keycloak.services.Urls;
import org.keycloak.services.managers.AppAuthManager;
import org.keycloak.services.resource.RealmResourceProvider;
import org.keycloak.services.resource.RealmResourceProviderFactory;
import org.keycloak.services.resources.LoginActionsService;
import org.keycloak.services.resources.RealmsResource;

/**
 * @author rferranti
 */
public class OnlineAccessEndpoints {

    public static final String REQUIRED_ROLE = "online-access";
    private final KeycloakSession session;
    private final int tokenDurationInSeconds;

    public OnlineAccessEndpoints(KeycloakSession session, int tokenDurationInSeconds) {
        this.session = session;
        this.tokenDurationInSeconds = tokenDurationInSeconds;
    }

    /// Where a link may send the browser: the issuing client's own redirect uris, plus whatever the calling
    /// client's `online-access` role lists in its `redirect_uri` attribute. The calling client's id does not
    /// belong here - it names a client, it is not a uri - and only ever sat in the set because the block that
    /// collects *authorized clients* is built the same way, where the id does belong.
    static Set<String> validRedirects(ClientModel caller, ClientModel issuer) {
        return Stream.concat(
                issuer.getRedirectUris().stream(),
                Optional.ofNullable(caller.getRole(REQUIRED_ROLE))
                        .map(r -> Models.attribute(r, "redirect_uri").stream())
                        .orElse(Stream.of()))
                .collect(Collectors.toSet());
    }

    public static class IssuedForContainsAuthorizedClient implements Predicate<JsonWebToken> {

        private final Set<String> authorizedClients;

        public IssuedForContainsAuthorizedClient(ClientModel client) {
            this.authorizedClients = Stream.concat(Stream.of(client.getClientId()), Optional.ofNullable(client.getRole(REQUIRED_ROLE))
                    .map(r -> Models.attribute(r, "clients").stream())
                    .orElse(Stream.of()))
                    .collect(Collectors.toSet());
        }

        @Override
        public boolean test(JsonWebToken t) throws VerificationException {
            if (!authorizedClients.contains(t.getIssuedFor())) {
                throw new VerificationException(String.format("%s is not an authorized client", t.getIssuedFor()));
            }
            return true;
        }

    }

    public static class HasOfflineAccessScope implements Predicate<AccessToken> {

        @Override
        public boolean test(AccessToken t) throws VerificationException {
            final var scopes = Stream.of(t.getScope() == null ? "" : t.getScope())
                    .flatMap(s -> Stream.of(s.split(" ")))
                    .filter(s -> !s.isBlank())
                    .collect(Collectors.toSet());

            if (!scopes.contains("offline_access")) {
                throw new VerificationException("must have offline_access scope");
            }
            return true;
        }

    }

    public record OnlineSessionResponse(String link) {

    }

    /**
     * Requesting client must have online-access role. If requesting client and
     * the access_token client differ, online-access role, online-access role
     * must have a possibly multi-values 'clients' attribute containing the
     * access_token client id. Allowed redirect_uri are inherited from the at
     * client, allowlist can be expanded by adding redirect_uri attributes to
     * the online-access role.
     *
     * @param at the access token
     * @param redirectUri
     * @return a link to
     * @throws Exception
     */
    @POST
    @Path("/action-token")
    @Produces(MediaType.APPLICATION_JSON)
    public OnlineSessionResponse create(@HeaderParam("at") String at, @QueryParam("redirect_uri") String redirectUri) throws Exception {
        final var uriInfo = session.getContext().getUri();
        final var realm = session.getContext().getRealm();
        final var client = session.getContext().getClient();
        final var verifier = TokenVerifier.create(at, AccessToken.class);

        try {
            // the header is attacker-controlled: an unknown/null alg or kid must fail
            // verification cleanly instead of NPE-ing on the provider lookup
            final var algorithm = verifier.getHeader().getAlgorithm();
            final var signatureProvider = algorithm == null ? null : session.getProvider(SignatureProvider.class, algorithm.name());
            if (signatureProvider == null) {
                throw new VerificationException(String.format("unknown token algorithm: %s", algorithm));
            }
            final var token = verifier.withChecks(
                    new TokenVerifier.RealmUrlCheck(Urls.realmIssuer(uriInfo.getBaseUri(), realm.getName())),
                    new TokenVerifier.TokenTypeCheck(List.of("Bearer")),
                    new IssuedForContainsAuthorizedClient(client),
                    new HasOfflineAccessScope(),
                    TokenVerifier.IS_ACTIVE,
                    TokenVerifier.SUBJECT_EXISTS_CHECK
            )
                    .verifierContext(signatureProvider.verifier(verifier.getHeader().getKeyId()))
                    .verify()
                    .getToken();

            final var subject = token.getSubject();

            final var user = session.users().getUserById(realm, subject);
            if (user == null) {
                throw new NotFoundException("user not found");
            }
            if (!user.isEnabled()) {
                throw new NotFoundException("user is disabled");
            }

            final var otherClient = session.clients().getClientByClientId(realm, token.getIssuedFor());
            if (otherClient == null) {
                throw new ForbiddenException("invalid at client");
            }
            final var validRedirects = validRedirects(client, otherClient);

            final var verifiedRedirectUri = RedirectUtils.verifyRedirectUri(session, otherClient.getRootUrl(), redirectUri, validRedirects, true);
            if (verifiedRedirectUri == null) {
                throw new ForbiddenException("invalid redirect_uri");
            }
            final var actionToken = new OnlineAccessActionToken(user.getId(), Time.currentTime() + tokenDurationInSeconds, token.getIssuedFor(), verifiedRedirectUri);

            final var link = Urls.realmBase(uriInfo.getBaseUri())
                    .path(RealmsResource.class, "getLoginActionsService")
                    .path(LoginActionsService.class, "executeActionToken")
                    .queryParam(Constants.KEY, actionToken.serialize(session, realm, uriInfo))
                    .queryParam(Constants.CLIENT_ID, actionToken.getIssuedFor())
                    .build(realm.getName())
                    .toString();

            return new OnlineSessionResponse(link);
        } catch (VerificationException ex) {
            throw new ForbiddenException(ex.getMessage());
        }
    }

    public static class Factory implements RealmResourceProviderFactory {

        private boolean enabled;
        private int tokenDurationInSeconds;

        private static final Logger logger = Logger.getLogger(Factory.class);

        @Override
        public RealmResourceProvider create(KeycloakSession session) {
            if (!enabled) {
                // how keycloak's realm extension dispatcher is told to answer 404: RealmsResource
                // .resolveRealmExtension throws NotFoundException when the provider is null. The
                // action token handler answers the same flag, so minting and redeeming go together
                return null;
            }
            return new RealmResourceProvider() {

                @Override
                public Object getResource() {
                    final var auth = new AppAuthManager.BearerTokenAuthenticator(session).authenticate();
                    if (auth == null) {
                        throw new ForbiddenException("Invalid client or Invalid client credentials");
                    }
                    if (auth.client().getRole(REQUIRED_ROLE) == null) {
                        throw new ForbiddenException("client is missing a required role");
                    }
                    return new OnlineAccessEndpoints(session, tokenDurationInSeconds);
                }

                @Override
                public void close() {

                }

            };
        }

        @Override
        public void init(Config.Scope scope) {
            final var config = Conf.fromPrefix("online-access-endpoints", "online-access");
            this.enabled = config.bool("enabled", false);
            this.tokenDurationInSeconds = (int) config.number("token-duration", 60);
            logger.infof("online-access(endpoints) initialized: %s", this.enabled ? "enabled" : "disabled");
        }

        @Override
        public void postInit(KeycloakSessionFactory ksf) {

        }

        @Override
        public void close() {

        }

        @Override
        public String getId() {
            return "online-access";
        }

    }
}
