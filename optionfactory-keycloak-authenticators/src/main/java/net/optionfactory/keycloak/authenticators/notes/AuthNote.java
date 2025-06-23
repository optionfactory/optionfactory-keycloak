package net.optionfactory.keycloak.authenticators.notes;

import java.util.Optional;
import java.util.function.Supplier;
import org.keycloak.sessions.AuthenticationSessionModel;

public class AuthNote<T> {

    private final Note<T> note;
    private final String key;

    public AuthNote(String key, Class<T> type) {
        this.note = new Note<>(type);
        this.key = key;
    }

    public static <T> AuthNote<T> of(String key, Class<T> type) {
        return new AuthNote(key, type);
    }

    public void store(AuthenticationSessionModel as, T value) {
        as.setAuthNote(key, note.marshal(value));
    }

    public Optional<T> load(AuthenticationSessionModel as) {
        return Optional.ofNullable(note.unmarshal(as.getAuthNote(key)));
    }

    public Optional<T> load(AuthenticationSessionModel as, T defaultValue) {
        T v = note.unmarshal(as.getAuthNote(key));
        if (v != null) {
            return Optional.of(v);
        }
        store(as, defaultValue);
        return Optional.of(defaultValue);
    }

    public Optional<T> load(AuthenticationSessionModel as, Supplier<T> supplier) {
        T v = note.unmarshal(as.getAuthNote(key));
        if (v != null) {
            return Optional.of(v);
        }
        final T defaultValue = supplier.get();
        store(as, defaultValue);
        return Optional.of(defaultValue);
    }

    public boolean exists(AuthenticationSessionModel as) {
        return as.getAuthNote(key) != null;
    }

    public void clear(AuthenticationSessionModel as) {
        as.removeAuthNote(key);
    }

}
