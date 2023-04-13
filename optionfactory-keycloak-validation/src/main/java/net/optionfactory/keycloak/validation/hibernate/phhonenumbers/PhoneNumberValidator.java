package net.optionfactory.keycloak.validation.hibernate.phhonenumbers;

import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberType;
import java.util.EnumSet;
import java.util.List;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class PhoneNumberValidator implements ConstraintValidator<PhoneNumber, String> {

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
