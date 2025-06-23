package net.optionfactory.keycloak.authenticators.notes;

import org.keycloak.sessions.AuthenticationSessionModel;

import java.util.Optional;
import org.keycloak.models.UserSessionModel;

public class UserSessionNote<T> {

    private final Note<T> note;
    private final String key;

    public UserSessionNote(String key, Class<T> type) {
        this.key = key;
        this.note = new Note<>(type);
    }

    public static <T> UserSessionNote<T> of(String key, Class<T> type) {
        return new UserSessionNote<>(key, type);
    }

    public void store(AuthenticationSessionModel as, T value) {
        as.setUserSessionNote(key, note.marshal(value));
    }

    public Optional<T> load(AuthenticationSessionModel asm) {
        return Optional.ofNullable(note.unmarshal(asm.getUserSessionNotes().get(key)));
    }

    public Optional<T> load(UserSessionModel usm) {
        return Optional.ofNullable(note.unmarshal(usm.getNote(key)));
    }

}
