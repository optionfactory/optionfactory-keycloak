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
import net.optionfactory.keycloak.validation.RequestValidatorFactory;
import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

public class HibernateRequestValidator implements RequestValidator {

    private final Validator validator;

    public HibernateRequestValidator() {
        this.validator = Validation
                .byProvider(HibernateValidator.class)
                .configure()
                .messageInterpolator(new ParameterMessageInterpolator())
                .buildValidatorFactory()
                .getValidator();
    }

    @Override
    public <T> Set<ConstraintViolation<T>> validate(T request, Class<?>... groups) {
        return validator.validate(request, groups);
    }

    @Override
    public <T> Set<ConstraintViolation<T>> validate(T request, Method m, Object[] parameterValues, Class<?>... groups) {
        return validator.forExecutables().validateParameters(request, m, parameterValues, groups);
    }

    @Override
    public <T> void enforce(T request, Function<Set<ConstraintViolation<T>>, RuntimeException> exFactory, Class<?>... groups) {
        final Set<ConstraintViolation<T>> violations = validator.validate(request, groups);
        enforceNoViolations(violations, exFactory);
    }

    @Override
    public <T> void enforce(T o, Method m, Object[] parameterValues, Function<Set<ConstraintViolation<T>>, RuntimeException> exCtor, Class<?>... groups) {
        final var violations = validator.forExecutables().validateParameters(o, m, parameterValues, groups);
        enforceNoViolations(violations, exCtor);
    }

    private <T> void enforceNoViolations(Set<ConstraintViolation<T>> violations, Function<Set<ConstraintViolation<T>>, RuntimeException> exFactory) throws BadRequestException {
        if (violations.isEmpty()) {
            return;
        }
        throw exFactory.apply(violations);
    }

    public static class Factory implements RequestValidatorFactory {

        private HibernateRequestValidator validator;

        @Override
        public HibernateRequestValidator create(KeycloakSession session) {
            return validator;
        }

        @Override
        public void init(Config.Scope config) {
            this.validator = new HibernateRequestValidator();
        }

        @Override
        public void postInit(KeycloakSessionFactory factory) {
        }

        @Override
        public void close() {
            this.validator.close();
        }

        @Override
        public String getId() {
            return "opfa-hibernate-request-validator";
        }

    }

}
