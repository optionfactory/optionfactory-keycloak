package net.optionfactory.keycloak.validation.hibernate;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.ws.rs.BadRequestException;
import net.optionfactory.keycloak.validation.RequestValidator;
import org.hibernate.validator.HibernateValidator;

public class HibernateRequestValidator implements RequestValidator {

    private final Validator validator;

    public HibernateRequestValidator() {
        this.validator = Validation
                .byProvider(HibernateValidator.class)
                .configure()
                .buildValidatorFactory()
                .getValidator();
    }

    @Override
    public <T> void enforce(T request, Function<String, RuntimeException> exFactory, Class<?>... groups) {
        final Set<ConstraintViolation<T>> violations = validator.validate(request, groups);
        enforceNoViolations(violations, exFactory);
    }

    @Override
    public <T> void enforce(T o, Method m, Object[] parameterValues, Function<String, RuntimeException> exCtor, Class<?>... groups) {
        final var violations = validator.forExecutables().validateParameters(o, m, parameterValues, groups);
        enforceNoViolations(violations, exCtor);
    }

    private <T> void enforceNoViolations(Set<ConstraintViolation<T>> violations, Function<String, RuntimeException> exFactory) throws BadRequestException {
        if (violations.isEmpty()) {
            return;
        }
        final var m = violations
                .stream()
                .map(v -> "field '%s' %s".formatted(v.getPropertyPath(), v.getMessage()))
                .collect(Collectors.joining(", "));
        throw exFactory.apply(m);
    }

}
