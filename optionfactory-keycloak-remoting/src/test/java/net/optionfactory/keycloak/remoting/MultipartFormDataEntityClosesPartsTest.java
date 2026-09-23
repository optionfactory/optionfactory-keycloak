package net.optionfactory.keycloak.remoting;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MultipartFormDataEntityClosesPartsTest {

    /// A part stream that records being closed, standing in for the FileInputStream a caller hands to
    /// FormDataPart.file and can never get back.
    private static InputStream tracked(String name, List<String> closed) {
        return new ByteArrayInputStream("payload".getBytes(StandardCharsets.UTF_8)) {
            @Override
            public void close() throws IOException {
                closed.add(name);
                super.close();
            }
        };
    }

    private static OutputStream failingAfter(int bytes) {
        return new OutputStream() {
            private int written;

            @Override
            public void write(int b) throws IOException {
                if (++written > bytes) {
                    throw new IOException("connection reset");
                }
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                for (int i = 0; i < len; i++) {
                    write(b[off + i]);
                }
            }
        };
    }

    private static MultipartFormDataEntity entityWith(List<String> closed) {
        return new MultipartFormDataEntity("boundary", List.of(
                FormDataPart.file("first", "a.bin", ContentType.APPLICATION_OCTET_STREAM, tracked("first", closed)),
                FormDataPart.file("second", "b.bin", ContentType.APPLICATION_OCTET_STREAM, tracked("second", closed))));
    }

    @Test
    public void everyPartIsClosedWhenTheWriteFailsHalfWay() {
        final var closed = new ArrayList<String>();

        Assertions.assertThrows(IOException.class, () -> entityWith(closed).writeTo(failingAfter(8)));

        Assertions.assertEquals(List.of("first", "second"), closed,
                "a failed upload must not strand the streams the caller handed over");
    }

    @Test
    public void everyPartIsClosedOnASuccessfulWrite() {
        final var closed = new ArrayList<String>();

        Assertions.assertDoesNotThrow(() -> entityWith(closed).writeTo(OutputStream.nullOutputStream()));

        Assertions.assertEquals(List.of("first", "second"), closed);
    }
}
