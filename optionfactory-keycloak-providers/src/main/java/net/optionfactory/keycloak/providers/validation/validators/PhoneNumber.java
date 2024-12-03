package net.optionfactory.keycloak.providers.validation.validators;

import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberType;
import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;
import java.util.EnumSet;
import java.util.List;
import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import net.optionfactory.keycloak.providers.validation.validators.PhoneNumber.Validator;

@Target({FIELD, METHOD, PARAMETER, ANNOTATION_TYPE, TYPE_USE})
@Retention(RUNTIME)
@Constraint(validatedBy = Validator.class)
@Documented
public @interface PhoneNumber {

    PhoneNumberType[] types() default {
        PhoneNumberType.FIXED_LINE_OR_MOBILE,
        PhoneNumberType.MOBILE
    };

    String defaultRegion() default "IT";

    String message() default "{net.optionfactory.keycloak.providers.validation.validators.PhoneNumber.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    public static class Validator implements ConstraintValidator<PhoneNumber, String> {

        private PhoneNumbers phones = new PhoneNumbers();
        private EnumSet<PhoneNumberType> types;
        private String defaultRegion;

        @Override
        public void initialize(PhoneNumber annotation) {
            this.types = EnumSet.copyOf(List.of(annotation.types()));
            this.defaultRegion = annotation.defaultRegion();
        }

        @Override
        public boolean isValid(String value, ConstraintValidatorContext ctx) {
            return phones.validate("phoneNumber", value, this.defaultRegion.isBlank() ? null : this.defaultRegion, types).isEmpty();
        }

    }

}
