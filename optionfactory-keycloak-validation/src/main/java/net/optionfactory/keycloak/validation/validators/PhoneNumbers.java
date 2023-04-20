package net.optionfactory.keycloak.validation.validators;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberType;
import com.google.i18n.phonenumbers.Phonenumber;
import java.util.EnumSet;
import java.util.Optional;
import org.keycloak.models.utils.FormMessage;

public class PhoneNumbers {

    private final PhoneNumberUtil phoneNumbers;

    public PhoneNumbers() {
        this.phoneNumbers = PhoneNumberUtil.getInstance();
    }

    public Optional<FormMessage> validate(String fieldName, String phoneNumber, String defaultRegion, EnumSet<PhoneNumberType> whitelistedTypes) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            return Optional.of(new FormMessage( fieldName, "mandatoryField"));
        }
        try {
            final Phonenumber.PhoneNumber parsed = phoneNumbers.parse(phoneNumber, defaultRegion);
            if (!phoneNumbers.isValidNumber(parsed)) {
                return Optional.of(new FormMessage(fieldName, "invalidField"));
            }
            final PhoneNumberUtil.PhoneNumberType type = phoneNumbers.getNumberType(parsed);
            if (!whitelistedTypes.contains(type)) {
                return Optional.of(new FormMessage(fieldName, "invalidPhoneNumberType", type.name()));
            }
            return Optional.empty();
        } catch (NumberParseException ex) {
            return Optional.of(new FormMessage(fieldName, "invalidField"));
        }
    }

    public String e164Format(String phoneNumber, String defaultRegion) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            return "";
        }
        try {
            final Phonenumber.PhoneNumber parsed = phoneNumbers.parse(phoneNumber, defaultRegion);
            return phoneNumbers.format(parsed, PhoneNumberUtil.PhoneNumberFormat.E164);
        } catch (NumberParseException ex) {
            throw new IllegalArgumentException(ex);
        }
    }
}
