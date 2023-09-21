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
import net.optionfactory.keycloak.validation.validators.NonEmptyInputPart.Validator;
import org.apache.http.message.BasicHeader;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;

@Target({FIELD, METHOD, PARAMETER, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = Validator.class)
@Documented
public @interface NonEmptyInputPart {

    String message() default "{net.optionfactory.keycloak.validation.validators.NonEmptyInputPart.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    public static class Validator implements ConstraintValidator<NonEmptyInputPart, InputPart> {

        @Override
        public void initialize(NonEmptyInputPart annotation) {
        }

        @Override
        public boolean isValid(InputPart value, ConstraintValidatorContext constraintContext) {
            if (value == null) {
                return false;
            }
            final String cd = value.getHeaders().getFirst("Content-Disposition");
            if (cd == null) {
                return false;
            }
            final var bh = new BasicHeader("Content-Disposition", cd);
            final var bhes = bh.getElements();
            if (bhes.length == 0) {
                return false;
            }
            final var bhe = bhes[0];
            final var filenameParam = bhe.getParameterByName("filename");
            if (filenameParam == null) {
                return false;
            }
            final var filename = filenameParam.getValue();
            return filename != null && !filename.isBlank();
        }

    }

}
