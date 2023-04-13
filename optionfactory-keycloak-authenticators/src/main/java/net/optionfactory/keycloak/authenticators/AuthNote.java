package net.optionfactory.keycloak.authenticators;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.UncheckedIOException;
import java.util.Optional;
import org.keycloak.sessions.AuthenticationSessionModel;

public class AuthNote<T> {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final Class<T> type;
    private final String key;

    public AuthNote(String key, Class<T> type) {
        this.key = key;
        this.type = type;
    }

    public static <T> AuthNote<T> of(String key, Class<T> type) {
        return new AuthNote(key, type);
    }

    public void store(AuthenticationSessionModel as, T value) {
        as.setAuthNote(key, marshal(value));
    }

    public Optional<T> load(AuthenticationSessionModel as) {
        return Optional.ofNullable(unmarshal(as.getAuthNote(key)));
    }

    public boolean exists(AuthenticationSessionModel as) {
        return as.getAuthNote(key) != null;
    }

    public void clear(AuthenticationSessionModel as) {
        as.removeAuthNote(key);
    }

    private String marshal(T value) {
        if (value == null) {
            return null;
        }
        if (type == String.class) {
            return (String) value;
        }
        try {
            return MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private T unmarshal(String value) {
        if (value == null) {
            return null;
        }
        if (type == String.class) {
            return (T) value;
        }
        try {
            return MAPPER.readValue(value, type);
        } catch (JsonProcessingException ex) {
            throw new UncheckedIOException(ex);
        }
    }

}
