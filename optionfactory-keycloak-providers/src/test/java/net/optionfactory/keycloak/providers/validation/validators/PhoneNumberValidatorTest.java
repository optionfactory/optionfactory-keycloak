package net.optionfactory.keycloak.providers.validation.validators;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.keycloak.validate.ValidationContext;
import org.keycloak.validate.ValidatorConfig;

public class PhoneNumberValidatorTest {

    private final PhoneNumberValidator validator = new PhoneNumberValidator();

    @Test
    public void acceptsNationalNumbersWhenDefaultRegionIsConfigured() {
        final var config = ValidatorConfig.configFromMap(Map.of("default-region", "IT"));
        Assertions.assertTrue(validator.validate("335 1234567", "phoneNumber", new ValidationContext(), config).isValid());
    }

    @Test
    public void acceptsInternationalNumbersWithoutDefaultRegion() {
        final var config = ValidatorConfig.configFromMap(Map.of());
        Assertions.assertTrue(validator.validate("+39 335 1234567", "phoneNumber", new ValidationContext(), config).isValid());
        Assertions.assertTrue(validator.validate("+1 (202) 555-0100", "phoneNumber", new ValidationContext(), config).isValid());
    }

    @Test
    public void rejectsMalformedNumbers() {
        final var config = ValidatorConfig.configFromMap(Map.of("default-region", "IT"));
        final var context = validator.validate("not-a-phone", "phoneNumber", new ValidationContext(), config);
        Assertions.assertFalse(context.isValid());
        final var error = context.getErrors().iterator().next();
        Assertions.assertEquals("error-invalid-phone-number", error.getMessage());
        Assertions.assertEquals("phoneNumber", error.getInputHint());
    }

    @Test
    public void rejectsNationalNumbersWhenNoDefaultRegionIsConfigured() {
        final var config = ValidatorConfig.configFromMap(Map.of());
        Assertions.assertFalse(validator.validate("335 1234567", "phoneNumber", new ValidationContext(), config).isValid());
    }

    @Test
    public void rejectsInvalidNumbers() {
        final var config = ValidatorConfig.configFromMap(Map.of("default-region", "IT"));
        Assertions.assertFalse(validator.validate("+39 0123456", "phoneNumber", new ValidationContext(), config).isValid());
    }

    @Test
    public void rejectsNumbersOfDisallowedTypes() {
        final var config = ValidatorConfig.configFromMap(Map.of(
                "default-region", "IT",
                "allowed-types", List.of("MOBILE")
        ));
        final var context = validator.validate("02 12345678", "phoneNumber", new ValidationContext(), config);
        Assertions.assertFalse(context.isValid());
        final var error = context.getErrors().iterator().next();
        Assertions.assertEquals("error-invalid-phone-number-type", error.getMessage());
        Assertions.assertEquals("FIXED_LINE", error.getMessageParameters()[0]);
    }

    @Test
    public void acceptsNumbersOfExplicitlyAllowedTypes() {
        final var config = ValidatorConfig.configFromMap(Map.of(
                "default-region", "IT",
                "allowed-types", List.of("MOBILE", "FIXED_LINE")
        ));
        Assertions.assertTrue(validator.validate("335 1234567", "phoneNumber", new ValidationContext(), config).isValid());
        Assertions.assertTrue(validator.validate("02 12345678", "phoneNumber", new ValidationContext(), config).isValid());
    }

    @Test
    public void skipsNullAndBlankValues() {
        final var config = ValidatorConfig.configFromMap(Map.of("default-region", "IT"));
        Assertions.assertTrue(validator.validate(null, "phoneNumber", new ValidationContext(), config).isValid());
        Assertions.assertTrue(validator.validate("", "phoneNumber", new ValidationContext(), config).isValid());
        Assertions.assertTrue(validator.validate("   ", "phoneNumber", new ValidationContext(), config).isValid());
    }

    @Test
    public void configAcceptsValidRegionsAndTypes() {
        Assertions.assertTrue(validator.validateConfig(null, ValidatorConfig.configFromMap(Map.of(
                "default-region", "it",
                "allowed-types", List.of("mobile", "FIXED_LINE_OR_MOBILE")
        ))).isValid());
        Assertions.assertTrue(validator.validateConfig(null, ValidatorConfig.configFromMap(Map.of())).isValid());
    }

    @Test
    public void configRejectsUnknownRegions() {
        final var result = validator.validateConfig(null, ValidatorConfig.configFromMap(Map.of("default-region", "XX")));
        Assertions.assertFalse(result.isValid());
        Assertions.assertEquals("error-unknown-default-region", result.getErrors().iterator().next().getMessage());
    }

    @Test
    public void configRejectsUnknownTypes() {
        final var result = validator.validateConfig(null, ValidatorConfig.configFromMap(Map.of(
                "default-region", "IT",
                "allowed-types", List.of("CARRIER_PIGEON")
        )));
        Assertions.assertFalse(result.isValid());
        Assertions.assertEquals("error-unknown-phone-number-type", result.getErrors().iterator().next().getMessage());
    }
    @Test
    public void anUnknownTypeNameIsIgnoredRatherThanThrown() {
        // config that never passed through validateConfig, a partial import say, must not blow up
        final var config = ValidatorConfig.configFromMap(Map.of(
                "default-region", "IT",
                "allowed-types", List.of("CARRIER_PIGEON", "MOBILE")
        ));
        Assertions.assertTrue(validator.validate("335 1234567", "phoneNumber", new ValidationContext(), config).isValid());
        Assertions.assertFalse(validator.validate("02 12345678", "phoneNumber", new ValidationContext(), config).isValid());
    }

    @Test
    public void allowingOnlyUnknownTypeNamesAllowsNothing() {
        final var config = ValidatorConfig.configFromMap(Map.of(
                "default-region", "IT",
                "allowed-types", List.of("CARRIER_PIGEON")
        ));
        final var context = validator.validate("335 1234567", "phoneNumber", new ValidationContext(), config);
        Assertions.assertFalse(context.isValid());
        Assertions.assertEquals("error-invalid-phone-number-type", context.getErrors().iterator().next().getMessage());
    }
}
