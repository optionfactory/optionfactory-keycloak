package net.optionfactory.keycloak.providers.validation.validators;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberType;
import com.google.i18n.phonenumbers.Phonenumber;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.ConfiguredProvider;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.validate.AbstractStringValidator;
import org.keycloak.validate.ValidationContext;
import org.keycloak.validate.ValidationError;
import org.keycloak.validate.ValidationResult;
import org.keycloak.validate.ValidatorConfig;

/// Accepts a phone number only in its canonical E.164 form: `+`, country code, digits, nothing
/// else.
///
/// A validator cannot rewrite what it validates - keycloak hands it an immutable `Map.Entry`, so
/// `setValue` throws - which leaves two options for an attribute that must end up canonical:
/// accept `+39 333 1234567` and have every reader normalize defensively, or refuse it. Refusing
/// makes "this attribute is E.164" an invariant of the realm rather than a convention each caller
/// has to remember, and it holds for the admin api and provisioning too, where no login form runs.
/// The error carries the canonical form so the form can tell the user exactly what to write.
public class PhoneNumberValidator extends AbstractStringValidator implements ConfiguredProvider {

    private static final PhoneNumberUtil PHONE_NUMBERS = PhoneNumberUtil.getInstance();
    private static final String ALLOWED_TYPES = "allowed-types";

    @Override
    public String getId() {
        return "phonenumber";
    }

    @Override
    protected void doValidate(String value, String inputHint, ValidationContext context, ValidatorConfig config) {
        final Phonenumber.PhoneNumber parsed;
        try {
            // no default region: a number without its `+` prefix does not parse, which is the point
            parsed = PHONE_NUMBERS.parse(value, null);
        } catch (NumberParseException ex) {
            context.addError(new ValidationError("phonenumber", inputHint, "error-invalid-phone-number"));
            return;
        }
        if (!PHONE_NUMBERS.isValidNumber(parsed)) {
            context.addError(new ValidationError("phonenumber", inputHint, "error-invalid-phone-number"));
            return;
        }
        // compared against the raw value, never a trimmed copy: what gets stored is what was
        // submitted, so surrounding whitespace has to be refused as well
        final String canonical = PHONE_NUMBERS.format(parsed, PhoneNumberFormat.E164);
        if (!canonical.equals(value)) {
            context.addError(new ValidationError("phonenumber", inputHint, "error-phone-number-not-canonical", canonical));
            return;
        }
        final PhoneNumberType type = PHONE_NUMBERS.getNumberType(parsed);
        if (!allowedTypes(config).contains(type)) {
            context.addError(new ValidationError("phonenumber", inputHint, "error-invalid-phone-number-type", type.name()));
        }
    }

    @Override
    protected boolean skipValidation(Object value, ValidatorConfig config) {
        return value == null || value.toString().isBlank() || super.skipValidation(value, config);
    }

    @Override
    public String getHelpText() {
        return "Validates a phone number using libphonenumber, accepting only its canonical E.164 form: + country code and digits, with no spaces or separators";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return List.of(
                new ProviderConfigProperty(ALLOWED_TYPES, "Allowed types", "Accepted phone number types, e.g. MOBILE, FIXED_LINE_OR_MOBILE, FIXED_LINE; defaults to MOBILE and FIXED_LINE_OR_MOBILE", ProviderConfigProperty.MULTIVALUED_STRING_TYPE, null)
        );
    }

    @Override
    public ValidationResult validateConfig(KeycloakSession session, ValidatorConfig config) {
        final Set<ValidationError> errors = new HashSet<>();
        for (String name : config.getStringListOrDefault(ALLOWED_TYPES, List.<String>of())) {
            if (type(name).isEmpty()) {
                errors.add(new ValidationError("phonenumber", ALLOWED_TYPES, "error-unknown-phone-number-type", name));
            }
        }
        return errors.isEmpty() ? super.validateConfig(session, config) : new ValidationResult(errors);
    }

    private EnumSet<PhoneNumberType> allowedTypes(ValidatorConfig config) {
        final List<String> names = config.getStringListOrDefault(ALLOWED_TYPES, List.of());
        if (names.isEmpty()) {
            return EnumSet.of(PhoneNumberType.MOBILE, PhoneNumberType.FIXED_LINE_OR_MOBILE);
        }
        // a name that is not a type matches nothing, just as a valid but unlisted one would:
        // a typo is reported by validateConfig, and a login form is no place to throw
        final EnumSet<PhoneNumberType> types = EnumSet.noneOf(PhoneNumberType.class);
        for (String name : names) {
            type(name).ifPresent(types::add);
        }
        return types;
    }

    private static Optional<PhoneNumberType> type(String name) {
        try {
            return Optional.of(PhoneNumberType.valueOf(name.trim().toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }
}
