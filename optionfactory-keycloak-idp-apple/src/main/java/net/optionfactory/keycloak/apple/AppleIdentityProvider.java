package net.optionfactory.keycloak.apple;

import org.keycloak.broker.oidc.OIDCIdentityProvider;
import org.keycloak.broker.oidc.OIDCIdentityProviderConfig;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.broker.provider.IdentityBrokerException;
import org.keycloak.broker.social.SocialIdentityProvider;
import org.keycloak.common.util.Time;
import org.keycloak.crypto.Algorithm;
import org.keycloak.crypto.KeyWrapper;
import org.keycloak.crypto.ServerECDSASignatureSignerContext;
import org.keycloak.crypto.SignatureSignerContext;
import org.keycloak.events.EventBuilder;
import org.keycloak.jose.jws.JWSBuilder;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.representations.JsonWebToken;
import org.keycloak.util.JsonSerialization;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicReference;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.core.Response;
import org.keycloak.OAuth2Constants;
import org.keycloak.broker.oidc.AbstractOAuth2IdentityProvider;
import org.keycloak.http.simple.SimpleHttpRequest;

public class AppleIdentityProvider extends OIDCIdentityProvider implements SocialIdentityProvider<OIDCIdentityProviderConfig> {

    private final AtomicReference<String> userJsonRef = new AtomicReference<>();

    public AppleIdentityProvider(KeycloakSession session, AppleIdentityProviderConfig config) {
        super(session, config);
        config.setAuthorizationUrl("https://appleid.apple.com/auth/authorize?response_mode=form_post");
        config.setTokenUrl("https://appleid.apple.com/auth/token");
        config.setJwksUrl("https://appleid.apple.com/auth/keys");
        config.setUseJwksUrl(true);
        config.setValidateSignature(true);
        config.setIssuer("https://appleid.apple.com");
    }

    @Override
    public Object callback(RealmModel realm, AuthenticationCallback callback, EventBuilder event) {
        return new AppleOidcEndpoint(callback, realm, event, userJsonRef, this);
    }

    @Override
    public BrokeredIdentityContext getFederatedIdentity(String response) {
        BrokeredIdentityContext context = super.getFederatedIdentity(response);
        final String uj = userJsonRef.get();
        if (uj != null) {
            userJsonRef.compareAndSet(uj, null);
            try {
                JsonSerialization.readValue(uj, AppleUser.class).applyTo(context);
            } catch (IOException e) {
                logger.errorf("Failed to parse userJson: %s", e.getMessage());
            }
        }

        return context;
    }

    @Override
        public SimpleHttpRequest authenticateTokenRequest(SimpleHttpRequest tokenRequest) {
        AppleIdentityProviderConfig config = (AppleIdentityProviderConfig) getConfig();
        tokenRequest.param(OAUTH2_PARAMETER_CLIENT_ID, config.getClientId());

        try {
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            byte[] pkc8ePrivateKey = Base64.getDecoder().decode(stripPemArmor(config.getClientSecret()));
            PKCS8EncodedKeySpec keySpecPKCS8 = new PKCS8EncodedKeySpec(pkc8ePrivateKey);
            PrivateKey privateKey = keyFactory.generatePrivate(keySpecPKCS8);

            KeyWrapper keyWrapper = new KeyWrapper();
            keyWrapper.setAlgorithm(Algorithm.ES256);
            keyWrapper.setKid(config.getKeyId());
            keyWrapper.setPrivateKey(privateKey);
            SignatureSignerContext signer = new ServerECDSASignatureSignerContext(keyWrapper);

            long currentTime = Time.currentTime();
            JsonWebToken token = new JsonWebToken();
            token.issuer(config.getTeamId());
            token.iat(currentTime);
            token.exp(currentTime + 15 * 60);
            token.audience("https://appleid.apple.com");
            token.subject(config.getClientId());
            String clientSecret = new JWSBuilder().jsonContent(token).sign(signer);

            tokenRequest.param(OAUTH2_PARAMETER_CLIENT_SECRET, clientSecret);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | IllegalArgumentException e) {
            logger.errorf("Failed to generate apple client secret from the configured private key: %s", e.getMessage());
            // proceeding without a client_secret would only yield a confusing invalid_client from apple
            throw new IdentityBrokerException("Invalid apple client private key", e);
        }

        return tokenRequest;
    }

    private static String stripPemArmor(String key) {
        return key
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
    }

    @Override
    protected String getDefaultScopes() {
        return "email name";
    }

    public static class AppleOidcEndpoint extends OIDCIdentityProvider.OIDCEndpoint {

        private final AtomicReference<String> userJson;

        public AppleOidcEndpoint(AuthenticationCallback callback, RealmModel realm, EventBuilder event, AtomicReference<String> userJson, final AppleIdentityProvider outer) {
            super(callback, realm, event, outer);
            this.userJson = userJson;
        }

        
        @POST
        public Response authResponse(
                @FormParam(value = AbstractOAuth2IdentityProvider.OAUTH2_PARAMETER_STATE) String state, 
                @FormParam(value = AbstractOAuth2IdentityProvider.OAUTH2_PARAMETER_CODE) String authorizationCode, 
                @FormParam(value = "user") String userJson, 
                @FormParam(value = OAuth2Constants.ERROR) String error,
                @FormParam(value = OAuth2Constants.ERROR_DESCRIPTION) String errorDescription) {
            this.userJson.set(userJson);
            return super.authResponse(state, authorizationCode, error, errorDescription);
        }

    }

}
