package net.optionfactory.keycloak.validation.validators;

import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;
import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import net.optionfactory.keycloak.validation.validators.Email.Validator;
import org.keycloak.validate.validators.EmailValidator;

@Target({FIELD, METHOD, PARAMETER, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = Validator.class)
@Documented
public @interface Email {

    String message() default "{net.optionfactory.keycloak.validation.validators.Email.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    public static class Validator implements ConstraintValidator<Email, String> {

        @Override
        public void initialize(Email annotation) {
        }

        @Override
        public boolean isValid(String value, ConstraintValidatorContext constraintContext) {
            return EmailValidator.INSTANCE.validate(value).isValid();
        }

    }

}
