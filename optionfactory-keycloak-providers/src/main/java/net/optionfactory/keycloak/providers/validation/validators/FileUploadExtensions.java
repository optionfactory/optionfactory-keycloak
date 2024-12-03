package net.optionfactory.keycloak.providers.validation.validators;

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
import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import net.optionfactory.keycloak.providers.validation.validators.FileUploadExtensions.Validator;
import org.jboss.resteasy.reactive.multipart.FileUpload;

@Target({FIELD, METHOD, PARAMETER, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = Validator.class)
@Documented
public @interface FileUploadExtensions {

    String message() default "{net.optionfactory.keycloak.validation.providers.validators.FileUploadExtensions.message}";

    Class<?>[] groups() default {};

    String[] value() default {"pdf", "png", "bmp", "jpg", "jpeg"};

    Class<? extends Payload>[] payload() default {};

    public static class Validator implements ConstraintValidator<FileUploadExtensions, FileUpload> {

        private Set<String> supported;

        @Override
        public void initialize(FileUploadExtensions annotation) {
            this.supported = Stream.of(annotation.value()).collect(Collectors.toSet());
        }

        @Override
        public boolean isValid(FileUpload value, ConstraintValidatorContext constraintContext) {
            if (value == null) {
                return false;
            }
            final String filename = value.fileName();
            if (filename == null) {
                return false;
            }
            String[] parts = filename.toLowerCase().split("\\.");
            String extension = parts[parts.length - 1];
            return supported.contains(extension);
        }

    }

}
