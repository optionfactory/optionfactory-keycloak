package net.optionfactory.keycloak.validation.validators;

import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import net.optionfactory.keycloak.validation.validators.FileExtensionsInputPart.Validator;
import org.apache.http.message.BasicHeader;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;

@Target({FIELD, METHOD, PARAMETER, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = Validator.class)
@Documented
public @interface FileExtensionsInputPart {

    String message() default "{net.optionfactory.keycloak.validation.validators.FileExtensionsInputPart.message}";

    Class<?>[] groups() default {};

    String[] value() default {"pdf", "png", "bmp", "jpg", "jpeg"};

    Class<? extends Payload>[] payload() default {};

    public static class Validator implements ConstraintValidator<FileExtensionsInputPart, InputPart> {

        private Set<String> supported;

        @Override
        public void initialize(FileExtensionsInputPart annotation) {
            this.supported = Stream.of(annotation.value()).collect(Collectors.toSet());
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
            if (filename == null) {
                return false;
            }
            String[] parts = filename.toLowerCase().split("\\.");
            String extension = parts[parts.length - 1];
            return supported.contains(extension);
        }

    }

}
