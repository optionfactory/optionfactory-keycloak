package net.optionfactory.keycloak.providers.validation.validators;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberType;
import com.google.i18n.phonenumbers.Phonenumber;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.ConfiguredProvider;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.validate.AbstractStringValidator;
import org.keycloak.validate.ValidationContext;
import org.keycloak.validate.ValidationError;
import org.keycloak.validate.ValidationResult;
import org.keycloak.validate.ValidatorConfig;

public class PhoneNumberValidator extends AbstractStringValidator implements ConfiguredProvider {

    private static final PhoneNumberUtil PHONE_NUMBERS = PhoneNumberUtil.getInstance();
    private static final String DEFAULT_REGION = "default-region";
    private static final String ALLOWED_TYPES = "allowed-types";

    @Override
    public String getId() {
        return "phonenumber";
    }

    @Override
    protected void doValidate(String value, String inputHint, ValidationContext context, ValidatorConfig config) {
        try {
            final Phonenumber.PhoneNumber parsed = PHONE_NUMBERS.parse(value.trim(), defaultRegion(config));
            if (!PHONE_NUMBERS.isValidNumber(parsed)) {
                context.addError(new ValidationError("phonenumber", inputHint, "error-invalid-phone-number"));
                return;
            }
            final PhoneNumberType type = PHONE_NUMBERS.getNumberType(parsed);
            if (!allowedTypes(config).contains(type)) {
                context.addError(new ValidationError("phonenumber", inputHint, "error-invalid-phone-number-type", type.name()));
            }
        } catch (NumberParseException ex) {
            context.addError(new ValidationError("phonenumber", inputHint, "error-invalid-phone-number"));
        }
    }

    @Override
    protected boolean skipValidation(Object value, ValidatorConfig config) {
        return value == null || value.toString().isBlank() || super.skipValidation(value, config);
    }

    @Override
    public String getHelpText() {
        return "Validates a phone number using libphonenumber: numbers in international +CC format are always accepted, national formats require a default region";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return List.of(
                new ProviderConfigProperty(DEFAULT_REGION, "Default region", "ISO 3166-1 alpha-2 region (e.g. IT) used to parse national phone numbers; when unset, only numbers in international +CC format are accepted", ProviderConfigProperty.STRING_TYPE, null),
                new ProviderConfigProperty(ALLOWED_TYPES, "Allowed types", "Accepted phone number types, e.g. MOBILE, FIXED_LINE_OR_MOBILE, FIXED_LINE; defaults to MOBILE and FIXED_LINE_OR_MOBILE", ProviderConfigProperty.MULTIVALUED_STRING_TYPE, null)
        );
    }

    @Override
    public ValidationResult validateConfig(KeycloakSession session, ValidatorConfig config) {
        final Set<ValidationError> errors = new HashSet<>();
        final String region = config.getString(DEFAULT_REGION);
        if (region != null && !region.isBlank() && !PHONE_NUMBERS.getSupportedRegions().contains(region.trim().toUpperCase(Locale.ROOT))) {
            errors.add(new ValidationError("phonenumber", DEFAULT_REGION, "error-unknown-default-region", region));
        }
        for (String name : config.getStringListOrDefault(ALLOWED_TYPES, List.<String>of())) {
            try {
                PhoneNumberType.valueOf(name.trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ex) {
                errors.add(new ValidationError("phonenumber", ALLOWED_TYPES, "error-unknown-phone-number-type", name));
            }
        }
        return errors.isEmpty() ? super.validateConfig(session, config) : new ValidationResult(errors);
    }

    private String defaultRegion(ValidatorConfig config) {
        final String region = config.getString(DEFAULT_REGION);
        return region == null || region.isBlank() ? null : region.trim().toUpperCase(Locale.ROOT);
    }

    private EnumSet<PhoneNumberType> allowedTypes(ValidatorConfig config) {
        final List<String> names = config.getStringListOrDefault(ALLOWED_TYPES, List.of());
        if (names.isEmpty()) {
            return EnumSet.of(PhoneNumberType.MOBILE, PhoneNumberType.FIXED_LINE_OR_MOBILE);
        }
        final EnumSet<PhoneNumberType> types = EnumSet.noneOf(PhoneNumberType.class);
        for (String name : names) {
            types.add(PhoneNumberType.valueOf(name.trim().toUpperCase(Locale.ROOT)));
        }
        return types;
    }
}
