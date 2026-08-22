package net.optionfactory.keycloak.authenticators.notes;

import org.junit.jupiter.api.Assertions;
import java.io.UncheckedIOException;
import java.util.List;
import org.junit.jupiter.api.Test;

public class NoteTest {

    public static record Attempt(String phoneNumber, String code, int retries) {

    }

    @Test
    public void stringsArePassedThroughAsIs() {
        final Note<String> note = new Note<>(String.class);
        Assertions.assertEquals("hello", note.marshal("hello"));
        Assertions.assertEquals("hello", note.unmarshal("hello"));
    }

    @Test
    public void nullsArePreserved() {
        final Note<String> note = new Note<>(String.class);
        Assertions.assertNull(note.marshal(null));
        Assertions.assertNull(note.unmarshal(null));
    }

    @Test
    public void structuredValuesRoundTripThroughJson() {
        final Note<Attempt> note = new Note<>(Attempt.class);
        final var marshalled = note.marshal(new Attempt("+39333...", "123456", 2));
        Assertions.assertEquals(new Attempt("+39333...", "123456", 2), note.unmarshal(marshalled));
    }

    @Test
    public void unmarshallingGarbageFailsLoudly() {
        final Note<Attempt> note = new Note<>(Attempt.class);
        Assertions.assertThrows(UncheckedIOException.class, () -> note.unmarshal("not json"));
    }

    @Test
    public void listsRoundTripThroughJson() {
        final Note<List> note = new Note<>(List.class);
        final var marshalled = note.marshal(List.of("+39...", "+44..."));
        Assertions.assertEquals(List.of("+39...", "+44..."), note.unmarshal(marshalled));
    }
}
