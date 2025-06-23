package net.optionfactory.keycloak.authenticators.notes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.UncheckedIOException;

public record Note<T>(Class<T> type) {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public String marshal(T value) {
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

    public T unmarshal(String value) {
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
