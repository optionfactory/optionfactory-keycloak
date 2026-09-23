package net.optionfactory.keycloak.providers.validation.validators;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.keycloak.validate.ValidationContext;
import org.keycloak.validate.ValidatorConfig;

public class PhoneNumberValidatorTest {

    private final PhoneNumberValidator validator = new PhoneNumberValidator();
    private final ValidatorConfig unconfigured = ValidatorConfig.configFromMap(Map.of());

    @Test
    public void acceptsTheCanonicalForm() {
        Assertions.assertTrue(validator.validate("+393351234567", "phoneNumber", new ValidationContext(), unconfigured).isValid());
        Assertions.assertTrue(validator.validate("+12025550100", "phoneNumber", new ValidationContext(), unconfigured).isValid());
    }

    @Test
    public void rejectsSeparatorsAndNamesTheCanonicalForm() {
        for (String written : List.of("+39 335 1234567", "+39-335-1234567", "+39 (335) 1234567", " +393351234567", "+393351234567 ")) {
            final var context = validator.validate(written, "phoneNumber", new ValidationContext(), unconfigured);

            Assertions.assertFalse(context.isValid(), written);
            final var error = context.getErrors().iterator().next();
            Assertions.assertEquals("error-phone-number-not-canonical", error.getMessage(), written);
            Assertions.assertEquals("+393351234567", error.getMessageParameters()[0], written);
        }
    }

    @Test
    public void rejectsANumberWithoutItsInternationalPrefix() {
        // no default region to fall back on: this is what makes the stored value unambiguous
        Assertions.assertFalse(validator.validate("3351234567", "phoneNumber", new ValidationContext(), unconfigured).isValid());
        Assertions.assertFalse(validator.validate("00393351234567", "phoneNumber", new ValidationContext(), unconfigured).isValid());
    }

    @Test
    public void rejectsMalformedNumbers() {
        final var context = validator.validate("not-a-phone", "phoneNumber", new ValidationContext(), unconfigured);

        Assertions.assertFalse(context.isValid());
        final var error = context.getErrors().iterator().next();
        Assertions.assertEquals("error-invalid-phone-number", error.getMessage());
        Assertions.assertEquals("phoneNumber", error.getInputHint());
    }

    @Test
    public void rejectsWellFormedNumbersThatDoNotExist() {
        Assertions.assertFalse(validator.validate("+390123456", "phoneNumber", new ValidationContext(), unconfigured).isValid());
    }

    @Test
    public void rejectsNumbersOfDisallowedTypes() {
        final var config = ValidatorConfig.configFromMap(Map.of("allowed-types", List.of("MOBILE")));
        final var context = validator.validate("+390212345678", "phoneNumber", new ValidationContext(), config);

        Assertions.assertFalse(context.isValid());
        final var error = context.getErrors().iterator().next();
        Assertions.assertEquals("error-invalid-phone-number-type", error.getMessage());
        Assertions.assertEquals("FIXED_LINE", error.getMessageParameters()[0]);
    }

    @Test
    public void acceptsNumbersOfExplicitlyAllowedTypes() {
        final var config = ValidatorConfig.configFromMap(Map.of("allowed-types", List.of("MOBILE", "FIXED_LINE")));

        Assertions.assertTrue(validator.validate("+393351234567", "phoneNumber", new ValidationContext(), config).isValid());
        Assertions.assertTrue(validator.validate("+390212345678", "phoneNumber", new ValidationContext(), config).isValid());
    }

    @Test
    public void defaultsToMobileNumbers() {
        Assertions.assertTrue(validator.validate("+393351234567", "phoneNumber", new ValidationContext(), unconfigured).isValid());
        Assertions.assertFalse(validator.validate("+390212345678", "phoneNumber", new ValidationContext(), unconfigured).isValid());
    }

    @Test
    public void skipsNullAndBlankValues() {
        Assertions.assertTrue(validator.validate(null, "phoneNumber", new ValidationContext(), unconfigured).isValid());
        Assertions.assertTrue(validator.validate("", "phoneNumber", new ValidationContext(), unconfigured).isValid());
        Assertions.assertTrue(validator.validate("   ", "phoneNumber", new ValidationContext(), unconfigured).isValid());
    }

    @Test
    public void configAcceptsValidTypes() {
        Assertions.assertTrue(validator.validateConfig(null, ValidatorConfig.configFromMap(Map.of(
                "allowed-types", List.of("mobile", "FIXED_LINE_OR_MOBILE")
        ))).isValid());
        Assertions.assertTrue(validator.validateConfig(null, unconfigured).isValid());
    }

    @Test
    public void configRejectsUnknownTypes() {
        final var result = validator.validateConfig(null, ValidatorConfig.configFromMap(Map.of(
                "allowed-types", List.of("CARRIER_PIGEON")
        )));

        Assertions.assertFalse(result.isValid());
        Assertions.assertEquals("error-unknown-phone-number-type", result.getErrors().iterator().next().getMessage());
    }

    @Test
    public void anUnknownTypeNameIsIgnoredRatherThanThrown() {
        // config that never passed through validateConfig, a partial import say, must not blow up
        final var config = ValidatorConfig.configFromMap(Map.of("allowed-types", List.of("CARRIER_PIGEON", "MOBILE")));

        Assertions.assertTrue(validator.validate("+393351234567", "phoneNumber", new ValidationContext(), config).isValid());
        Assertions.assertFalse(validator.validate("+390212345678", "phoneNumber", new ValidationContext(), config).isValid());
    }

    @Test
    public void allowingOnlyUnknownTypeNamesAllowsNothing() {
        final var config = ValidatorConfig.configFromMap(Map.of("allowed-types", List.of("CARRIER_PIGEON")));
        final var context = validator.validate("+393351234567", "phoneNumber", new ValidationContext(), config);

        Assertions.assertFalse(context.isValid());
        Assertions.assertEquals("error-invalid-phone-number-type", context.getErrors().iterator().next().getMessage());
    }
}
