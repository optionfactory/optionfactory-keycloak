package net.optionfactory.keycloak.email;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.keycloak.Config;
import org.keycloak.email.DefaultEmailAuthenticator;
import org.keycloak.email.EmailAuthenticator;
import org.keycloak.email.EmailSenderProvider;
import org.keycloak.email.EmailSenderProviderFactory;
import org.keycloak.email.PasswordAuthEmailAuthenticator;
import org.keycloak.email.TokenAuthEmailAuthenticator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

public class CidEmbeddingEmailSenderProviderFactory implements EmailSenderProviderFactory {


    private final Map<EmailAuthenticator.AuthenticatorType, EmailAuthenticator> emailAuthenticators = new ConcurrentHashMap<>();
    
    
    @Override
    public EmailSenderProvider create(KeycloakSession session) {
        return new CidEmbeddingEmailSenderProvider(session, emailAuthenticators);
    }

    @Override
    public void init(Config.Scope config) {
        emailAuthenticators.put(EmailAuthenticator.AuthenticatorType.NONE, new DefaultEmailAuthenticator());
        emailAuthenticators.put(EmailAuthenticator.AuthenticatorType.BASIC, new PasswordAuthEmailAuthenticator());
        emailAuthenticators.put(EmailAuthenticator.AuthenticatorType.TOKEN, new TokenAuthEmailAuthenticator());        
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    @Override
    public void close() {
        emailAuthenticators.clear();
    }

    @Override
    public String getId() {
        return "opfa-cid-embedding";
    }

}
