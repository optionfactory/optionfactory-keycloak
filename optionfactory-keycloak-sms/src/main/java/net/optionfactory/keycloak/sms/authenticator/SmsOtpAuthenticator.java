package net.optionfactory.keycloak.sms.authenticator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.ws.rs.core.Response;
import net.optionfactory.keycloak.sms.otp.SmsOtp;
import net.optionfactory.keycloak.sms.otp.SmsOtpService;
import net.optionfactory.keycloak.sms.otp.SmsOtpValidationException;
import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.provider.ProviderConfigProperty;

public class SmsOtpAuthenticator implements Authenticator {

    private static final String OPFA_OTP_FORM_FTL_TEMPLATE = "opfa-sms-otp.ftl";
    private static final Logger logger = Logger.getLogger(SmsOtpAuthenticator.class);
    private final KeycloakSession session;
    private final SmsOtpService smsOtps;
    private final String userMobileAttribute;
    private final String authNoteMobileAttribute;

    public SmsOtpAuthenticator(KeycloakSession session, SmsOtpService smsOtps, String userMobileAttribute, String authNoteMobileAttribute) {
        this.session = session;
        this.smsOtps = smsOtps;
        this.userMobileAttribute = userMobileAttribute;
        this.authNoteMobileAttribute = authNoteMobileAttribute;
    }

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        final String mobileNumber = Optional.ofNullable(context.getUser())
                .flatMap(u -> u.getAttributeStream(userMobileAttribute).findFirst())
                .orElseGet(() -> context.getAuthenticationSession().getAuthNote(authNoteMobileAttribute));

        final SmsOtp otp = smsOtps.send(String.format("auth:%s", mobileNumber), mobileNumber, "todo: %s");
        context.getAuthenticationSession().setAuthNote("opfa-sms-otp-id", otp.id);
        final Response form = context.form().createForm(OPFA_OTP_FORM_FTL_TEMPLATE);
        context.challenge(form);
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        final var formParams = context.getHttpRequest().getDecodedFormParameters();
        final var otpId = context.getAuthenticationSession().getAuthNote("opfa-sms-otp-id");
        try {
            smsOtps.validate(otpId, formParams.getFirst("otp"));
            logger.infof("success");
            context.success();

        } catch (SmsOtpValidationException ex) {
            final List<FormMessage> errors = new ArrayList<>();
            final var challenge = context.form().setErrors(errors)
                    .createForm(OPFA_OTP_FORM_FTL_TEMPLATE);
            context.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS, challenge);
        }
    }

    @Override
    public boolean requiresUser() {
        return false;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {

    }

    @Override
    public void close() {

    }

    public static class Factory implements AuthenticatorFactory {

        private String userMobileAttribute;
        private String authNoteMobileAttribute;
        private static final Logger logger = Logger.getLogger(Factory.class);

        @Override
        public SmsOtpAuthenticator create(KeycloakSession ks) {
            return new SmsOtpAuthenticator(ks, ks.getProvider(SmsOtpService.class), userMobileAttribute, authNoteMobileAttribute);
        }

        @Override
        public void init(Config.Scope config) {
            this.userMobileAttribute = config.get("userMobileAttribute", "mobile");
            this.authNoteMobileAttribute = config.get("authNoteMobileAttribute", "mobile");
            logger.infof("SmsOtpAuthenticator configured: user-mobile-attribute:%s, auth-note-mobile-attribute: %s", this.userMobileAttribute, this.authNoteMobileAttribute);
        }

        @Override
        public void postInit(KeycloakSessionFactory ksf) {
        }

        @Override
        public void close() {
        }

        @Override
        public String getId() {
            return "opfa-sms-otp-authenticator";
        }

        @Override
        public String getDisplayType() {
            return "opfa-sms-otp-authenticator";
        }

        @Override
        public String getReferenceCategory() {
            return null;
        }

        @Override
        public boolean isConfigurable() {
            return false;
        }

        @Override
        public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
            return new AuthenticationExecutionModel.Requirement[]{
                AuthenticationExecutionModel.Requirement.REQUIRED,
                AuthenticationExecutionModel.Requirement.DISABLED
            };
        }

        @Override
        public boolean isUserSetupAllowed() {
            return false;
        }

        @Override
        public String getHelpText() {
            return "opfa-sms-otp-authenticator";
        }

        @Override
        public List<ProviderConfigProperty> getConfigProperties() {
            return List.of();
        }

    }

}
