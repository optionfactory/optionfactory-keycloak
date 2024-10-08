package net.optionfactory.keycloak.email;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import jakarta.activation.DataHandler;
import jakarta.mail.Address;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMultipart;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import org.eclipse.angus.mail.smtp.SMTPMessage;
import org.jboss.logging.Logger;
import org.keycloak.common.enums.HostnameVerificationPolicy;
import org.keycloak.email.EmailException;
import org.keycloak.email.EmailSenderProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.services.ServicesLogger;
import org.keycloak.theme.Theme;
import org.keycloak.truststore.JSSETruststoreConfigurator;
import org.keycloak.vault.VaultStringSecret;

public class CidEmbeddingEmailSenderProvider implements EmailSenderProvider {

    private static final String SUPPORTED_SSL_PROTOCOLS = getSupportedSslProtocols();

    private static final Logger logger = Logger.getLogger(CidEmbeddingEmailSenderProvider.class);

    private final KeycloakSession session;

    public CidEmbeddingEmailSenderProvider(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public void send(Map<String, String> config, String address, String subject, String textBody, String htmlBody) throws EmailException {
        try {
            Properties props = new Properties();

            if (config.containsKey("host")) {
                props.setProperty("mail.smtp.host", config.get("host"));
            }

            boolean auth = "true".equals(config.get("auth"));
            boolean ssl = "true".equals(config.get("ssl"));
            boolean starttls = "true".equals(config.get("starttls"));

            if (config.containsKey("port") && config.get("port") != null) {
                props.setProperty("mail.smtp.port", config.get("port"));
            }

            if (auth) {
                props.setProperty("mail.smtp.auth", "true");
            }

            if (ssl) {
                props.setProperty("mail.smtp.ssl.enable", "true");
            }

            if (starttls) {
                props.setProperty("mail.smtp.starttls.enable", "true");
            }

            if (ssl || starttls || auth){
                props.put("mail.smtp.ssl.protocols", SUPPORTED_SSL_PROTOCOLS);

                setupTruststore(props);
            }

            props.setProperty("mail.smtp.timeout", "10000");
            props.setProperty("mail.smtp.connectiontimeout", "10000");

            String from = config.get("from");
            if (from == null) {
                throw new EmailException("No sender address configured in the realm settings for emails");
            }
            String fromDisplayName = config.get("fromDisplayName");
            String replyTo = config.get("replyTo");
            String replyToDisplayName = config.get("replyToDisplayName");
            String envelopeFrom = config.get("envelopeFrom");

            final Session emailSession = Session.getInstance(props);
            final var alternatives = new MimeMultipart("alternative");

            if (textBody != null) {
                final var textPart = new MimeBodyPart();
                textPart.setText(textBody, "UTF-8");
                alternatives.addBodyPart(textPart);
            }

            if (htmlBody != null) {
                final var related = new MimeMultipart("related");

                final var htmlPart = new MimeBodyPart();
                htmlPart.setContent(htmlBody, "text/html; charset=utf-8");

                related.addBodyPart(htmlPart);
                final var emailTheme = session.theme().getTheme(Theme.Type.EMAIL);
                final var cidsProvider = new CidsProvider(emailTheme);
                for (CidSource allowedCid : cidsProvider.cids(htmlBody)) {
                    final var source = new CidFromThemeDataSource(emailTheme, allowedCid);
                    final var mbp = new MimeBodyPart();
                    mbp.setDataHandler(new DataHandler(source));
                    mbp.setContentID(String.format("<%s>", allowedCid.id));
                    related.addBodyPart(mbp);
                }

                alternatives.addBodyPart(multipartAsBodyPart(related));
            }

            final var mixed = new MimeMultipart("mixed");
            mixed.addBodyPart(multipartAsBodyPart(alternatives));

            SMTPMessage msg = new SMTPMessage(emailSession);
            msg.setFrom(toInternetAddress(from, fromDisplayName));

            msg.setReplyTo(new Address[]{toInternetAddress(from, fromDisplayName)});
            if (replyTo != null && !replyTo.isEmpty()) {
                msg.setReplyTo(new Address[]{toInternetAddress(replyTo, replyToDisplayName)});
            }
            if (envelopeFrom != null && !envelopeFrom.isEmpty()) {
                msg.setEnvelopeFrom(envelopeFrom);
            }

            msg.setHeader("To", address);
            msg.setSubject(subject, "utf-8");
            msg.setContent(mixed);
            msg.saveChanges();
            msg.setSentDate(new Date());

            try (Transport transport = emailSession.getTransport("smtp")) {
                if (auth) {
                    try (VaultStringSecret vaultStringSecret = this.session.vault().getStringSecret(config.get("password"))) {
                        transport.connect(config.get("user"), vaultStringSecret.get().orElse(config.get("password")));
                    }
                } else {
                    transport.connect();
                }
                transport.sendMessage(msg, new InternetAddress[]{new InternetAddress(address)});
            }
        } catch (EmailException e) {
            throw e;
        } catch (Exception e) {
            ServicesLogger.LOGGER.failedToSendEmail(e);
            throw new EmailException("Error when attempting to send the email to the server. More information is available in the server log.", e);
        }
    }

    private static MimeBodyPart multipartAsBodyPart(MimeMultipart part) throws MessagingException {
        final var bp = new MimeBodyPart();
        bp.setContent(part);
        return bp;
    }

    protected InternetAddress toInternetAddress(String email, String displayName) throws UnsupportedEncodingException, AddressException, EmailException {
        if (email == null || "".equals(email.trim())) {
            throw new EmailException("Please provide a valid address", null);
        }
        if (displayName == null || "".equals(displayName.trim())) {
            return new InternetAddress(email);
        }
        return new InternetAddress(email, displayName, "utf-8");
    }

    private void setupTruststore(Properties props) {
        JSSETruststoreConfigurator configurator = new JSSETruststoreConfigurator(session);

        SSLSocketFactory factory = configurator.getSSLSocketFactory();
        if (factory != null) {
            props.put("mail.smtp.ssl.socketFactory", factory);
            if (configurator.getProvider().getPolicy() == HostnameVerificationPolicy.ANY) {
                props.setProperty("mail.smtp.ssl.trust", "*");
                props.put("mail.smtp.ssl.checkserveridentity", Boolean.FALSE.toString()); // this should be the default but seems to be impl specific, so set it explicitly just to be sure
            }
            else {
                props.put("mail.smtp.ssl.checkserveridentity", Boolean.TRUE.toString());
            }
        }
    }

    @Override
    public void close() {

    }

    private static String getSupportedSslProtocols() {
        try {
            String[] protocols = SSLContext.getDefault().getSupportedSSLParameters().getProtocols();
            if (protocols != null) {
                return String.join(" ", protocols);
            }
        } catch (Exception e) {
            logger.warn("Failed to get list of supported SSL protocols", e);
        }
        return null;
    }

}
