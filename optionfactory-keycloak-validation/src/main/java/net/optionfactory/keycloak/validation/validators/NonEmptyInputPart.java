package net.optionfactory.keycloak.validation.validators;

import java.io.StringReader;
import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import net.optionfactory.keycloak.validation.validators.NonEmptyInputPart.Validator;
import org.apache.james.mime4j.field.contentdisposition.parser.ContentDispositionParser;
import org.apache.james.mime4j.field.contentdisposition.parser.ParseException;
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
            Map<String, String> params = contentDisposition(cd);
            final var filename = params.get("filename");
            return filename != null && !filename.isBlank();
        }

        public static Map<String, String> contentDisposition(String headerValue) {
            try {
                final var cdp = new ContentDispositionParser(new StringReader(headerValue));
                cdp.parse();
                List<String> names = cdp.getParamNames();
                List<String> values = cdp.getParamValues();
                Map<String, String> params = new HashMap<>();
                for (int i = 0; i != names.size(); i++) {
                    params.put(names.get(i), values.get(i));
                }
                return params;
            } catch (ParseException ex) {
                return Map.of();
            }
        }
    }

}
