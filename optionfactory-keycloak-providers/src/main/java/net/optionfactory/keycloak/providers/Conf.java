package net.optionfactory.keycloak.providers;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;
import org.jboss.logging.Logger;
import org.keycloak.Config;

public class Conf {

    private final Logger logger = Logger.getLogger(Conf.class);

    private final Config.Scope scope;
    private final String name;

    public Conf(String name, Config.Scope scope) {
        this.name = name;
        this.scope = scope;
    }

    public String anyOf(String key, String... values) {
        final var value = string(key);
        final var domain = Set.of(values);
        ensure(domain.contains(key), "'%s' must be one of %s", key, domain);
        return value;
    }

    public URI uri(String key) {
        final var value = scope.get(key);
        ensure(value != null, "'%s' must be configured", key);
        try {
            return URI.create(value);
        } catch (IllegalArgumentException ex) {
            throw failure("'%s' must be a valid uri", key);
        }
    }

    public URI uri(String key, String defaultValue) {
        final var value = scope.get(key, defaultValue);
        if(value == null){
            return null;
        }
        try {
            return URI.create(value);
        } catch (IllegalArgumentException ex) {
            throw failure("'%s' must be a valid uri", key);
        }
    }

    public String string(String key) {
        final var value = scope.get(key);
        ensure(value != null, "'%s' must be configured", key);
        return value;
    }

    public String string(String key, String defaultValue) {
        return scope.get(key, defaultValue);
    }

    public boolean bool(String key) {
        final var value = scope.getBoolean(key);
        ensure(value != null, "'%s' must be configured", key);
        return value;
    }

    public boolean bool(String key, boolean defaultValue) {
        return scope.getBoolean(key, defaultValue);
    }

    public long number(String key) {
        final var value = scope.getLong(key);
        ensure(value != null, "'%s' must be configured", key);
        return value;
    }

    public long number(String key, long defaultValue) {
        return scope.getLong(key, defaultValue);
    }

    public String[] array(String key) {
        final var value = scope.getArray(key);
        ensure(value != null, "'%s' must be configured", key);
        return value;
    }

    public String[] array(String key, String[] defaultValue) {
        final var v = scope.getArray(key);
        return v == null ? defaultValue : v;
    }

    public void ensure(boolean test, String format, Object... args) {
        if (test) {
            return;
        }
        throw failure(format, args);
    }

    public ConfigurationException failure(String format, Object... args) {
        final var prefix = String.format("misconfigured %s", name);
        final var suffix = String.format(format, args);
        final var message = String.format("%s: %s", prefix, suffix);
        logger.warn(message);
        return new ConfigurationException(message);
    }

    public static class ConfigurationException extends IllegalArgumentException {

        public ConfigurationException(String message) {
            super(message);
        }

    }
}
