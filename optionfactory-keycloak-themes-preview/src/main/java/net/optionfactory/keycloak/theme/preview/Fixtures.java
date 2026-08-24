package net.optionfactory.keycloak.theme.preview;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class Fixtures {

    public static class Auth {

        public String attemptedUsername = "jdoe";
        public String selectedCredential = "";
        public boolean usernameShown = false;
        public boolean resetCredentialsShown = false;
        public boolean tryAnotherWay = false;
        public List<AuthenticationSelection> authenticationSelections = List.of();

        public boolean showUsername() {
            return usernameShown;
        }

        public boolean showResetCredentials() {
            return resetCredentialsShown;
        }

        public boolean showTryAnotherWayLink() {
            return tryAnotherWay;
        }
    }

    public static class AuthenticationSelection {

        public String authExecId;
        public String displayName;
        public String helpText;
        public String iconCssClass;
    }

    public static class Login {

        public String username = "jdoe@example.com";
        public boolean rememberMe = true;
    }

    public static class LocaleBean {

        public String current = "English";
        public String currentLanguageTag = "en";
        public boolean rtl = false;
        public List<Supported> supported = List.of();
    }

    public static class Supported {

        public String languageTag = "en";
        public String label = "English";
        public String url = "#";
    }

    public static class Message {

        public String type = "info";
        public String summary = "Informational message from the preview fixtures.";
    }

    public static class MessagesPerField {

        public Set<String> errors = Set.of();

        public boolean exists(String... fields) {
            return existsError(fields);
        }

        public boolean existsError(String... fields) {
            for (String f : fields) {
                if (errors.contains(f)) {
                    return true;
                }
            }
            return false;
        }

        public String getFirstError(String... fields) {
            for (String f : fields) {
                if (errors.contains(f)) {
                    return "invalid value for " + f;
                }
            }
            return "";
        }

        public String get(String field) {
            return errors.contains(field) ? "invalid value for " + field : "";
        }
    }

    public static class SocialProviders {

        public List<SocialProvider> providers = List.of();
    }

    public static class SocialProvider {

        public String alias = "google";
        public String loginUrl = "#";
        public String displayName = "Google";
        public String iconClasses = "bi bi-google";
    }

    public static class Group {

        public String name = "";
        public String displayHeader = "";
        public String displayDescription = "";
        public Map<String, String> html5DataAnnotations = Map.of();
    }

    public static class Attribute {

        public String name;
        public String displayName;
        public String value = "";
        public List<String> values = List.of();
        public boolean required = true;
        public boolean readOnly = false;
        public boolean multivalued = false;
        public String autocomplete = "";
        public Map<String, Object> annotations = Map.of();
        public Map<String, Object> validators = Map.of();
        public Map<String, String> html5DataAnnotations = Map.of();
        public Group group;
    }

    public static class Options {

        public List<String> options = List.of();
    }

    public static class Profile {

        public List<Attribute> attributes = List.of();
        public Map<String, String> html5DataAnnotations = Map.of();
    }

    public static class TotpPolicy {

        public String type = "totp";
        public String algorithm = "HmacSHA1";
        public int digits = 6;
        public int period = 30;
        public int initialCounter = 0;

        public String getAlgorithmKey() {
            return "SHA1";
        }
    }

    public static class Totp {

        public TotpPolicy policy = new TotpPolicy();
        public String totpSecret = "GBSWG4ZAN5HS2NCJ";
        public String totpSecretEncoded = "GBSW G4ZA N5HS 2NCJ";
        public String totpSecretQrCode = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==";
        public String manualUrl = "#";
        public String qrUrl = "#";
        public List<String> supportedApplications = List.of("FreeOTP", "Google Authenticator", "Microsoft Authenticator");
        public List<OtpCredential> otpCredentials = List.of(new OtpCredential());
    }

    public static class OtpCredential {

        public String id = "cred-1";
        public String userLabel = "Phone (default)";
    }

    public static class OtpLogin {

        public List<OtpCredential> userOtpCredentials = List.of(new OtpCredential());
        public String selectedCredentialId = "cred-1";
    }

    public static class ClientScope {

        public String consentScreenText;
        public boolean dynamicScope = false;
        public String parameterizedScopeParameter = "";
    }

    public static class OAuth {

        public String code = "os9Ke6AhWIJ2VQnf";
        public List<ClientScope> clientScopesRequested = List.of();
    }

    public static class Client {

        public String clientId = "securemail-web";
        public String name = "SecureMail Web";
        public String baseUrl = "https://portal.example.com/";
        public String frontChannelLogoutUrl = "#";
        public Map<String, String> attributes = Map.of();
    }

    public static class Transports {

        public String iconClass = "kcWebAuthnUSB";
    }

    public static class AuthenticatorInfo {

        public String credentialId = "webauthn-1";
        public String label = "YubiKey 5";
        public String createdAt = "2026-01-15";
        public Transports transports = new Transports();
    }

    public static class Authenticators {

        public List<AuthenticatorInfo> authenticators = List.of(new AuthenticatorInfo());
    }

    public static class RecoveryAuthnCodesConfig {

        public String generatedAt = "23 August 2026, 14:00";
        public List<String> generatedRecoveryAuthnCodes = List.of(
                "He2fB9YxZNAQqet", "Uf6dS3nFvhcmu41", "Kp2sQ8wTzrjxae5", "Gv7cM1hLdybtn96",
                "Rt4nJ0pXkqszwg8", "Wb9eV5rHfmjcyl2", "Zx3tC6uNpadokg7", "Qm8kD2ySvbewfz4",
                "Ln5hA7jGrtxcpi1", "Cy1wE9mKuzsdnq6", "Fj6bT4xNhgoqavr3", "Oa2rP8cWlkmyez9"
        );
        public String generatedRecoveryAuthnCodesAsString = String.join(" ", generatedRecoveryAuthnCodes);
        public List<String> generatedRecoveryAuthnCodesList = generatedRecoveryAuthnCodes;
    }

    public static class Organization {

        public String alias = "acme";
        public String name = "Acme Corp";
    }

    public static class User {

        public String email = "jdoe@example.com";
        public List<Organization> organizations = List.of();
    }
}
