package net.optionfactory.keycloak.validation;

import java.lang.reflect.Method;
import java.util.function.Function;
import org.keycloak.provider.Provider;

public interface RequestValidator extends Provider {

    public <T> void enforce(T o, Function<String, RuntimeException> ex, Class<?>... groups);

    public <T> void enforce(T o, Method m, Object[] parameterValues, Function<String, RuntimeException> ex, Class<?>... groups);

    @Override
    default void close() {

    }

}
