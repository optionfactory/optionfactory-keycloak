package net.optionfactory.keycloak.validation.hibernate.phhonenumbers;

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
import javax.validation.Constraint;
import javax.validation.Payload;

@Target({FIELD, METHOD, PARAMETER, ANNOTATION_TYPE, TYPE_USE})
@Retention(RUNTIME)
@Constraint(validatedBy = PhoneNumberValidator.class)
@Documented
public @interface PhoneNumber {

    
    PhoneNumberType[] types() default {
        PhoneNumberType.FIXED_LINE_OR_MOBILE, 
        PhoneNumberType.MOBILE
    };

    
    String defaultRegion() default "IT";

    String message() default "Numero di telefono non valido";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
