package net.optionfactory.keycloak.validation;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.function.Function;
import jakarta.validation.ConstraintViolation;
import org.keycloak.provider.Provider;

public interface RequestValidator extends Provider {

    public <T> Set<ConstraintViolation<T>> validate(T o, Class<?>... groups);

    public <T> Set<ConstraintViolation<T>> validate(T o, Method m, Object[] parameterValues, Class<?>... groups);

    public <T> void enforce(T o, Function<Set<ConstraintViolation<T>>, RuntimeException> ex, Class<?>... groups);

    public <T> void enforce(T o, Method m, Object[] parameterValues, Function<Set<ConstraintViolation<T>>, RuntimeException> ex, Class<?>... groups);

    public <T> T unwrap(Class<T> k);

    @Override
    default void close() {

    }

}
