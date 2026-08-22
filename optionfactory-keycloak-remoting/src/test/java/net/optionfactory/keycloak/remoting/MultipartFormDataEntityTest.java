package net.optionfactory.keycloak.remoting;

import org.junit.jupiter.api.Assertions;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.http.entity.ContentType;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Test;

public class MultipartFormDataEntityTest {

    @Test
    public void contentTypeDeclaresTheBoundary() {
        final var entity = new MultipartFormDataEntity("sep", java.util.List.of());
        Assertions.assertEquals("multipart/form-data; boundary=sep", entity.getContentType().getValue());
    }

    @Test
    public void emptyEntitiesStillEmitTheClosingBoundary() throws IOException {
        final var entity = new MultipartFormDataEntity("sep", java.util.List.of());
        final var body = new String(EntityUtils.toByteArray(entity), StandardCharsets.UTF_8);
        Assertions.assertEquals("--sep--\r\n", body);
    }

    @Test
    public void partsAreDelimitedAndTerminated() throws IOException {
        final var entity = new MultipartFormDataEntity("sep", java.util.List.of(
                FormDataPart.field("username", "wyatt"),
                FormDataPart.json("profile", "{\"k\":1}")
        ));
        final var body = new String(EntityUtils.toByteArray(entity), StandardCharsets.UTF_8);
        final var crlf = "\r\n";
        Assertions.assertTrue(body.startsWith("--sep" + crlf));
        Assertions.assertTrue(body.endsWith("--sep--" + crlf));
        Assertions.assertEquals(1, count(body, crlf + "--sep" + crlf));
        Assertions.assertEquals("--sep" + crlf
                + "Content-Disposition: form-data; name=username" + crlf
                + "Content-Transfer-Encoding: 8bit" + crlf
                + crlf
                + "wyatt" + crlf
                + "--sep" + crlf
                + "Content-Disposition: form-data; name=profile" + crlf
                + "Content-Transfer-Encoding: 8bit" + crlf
                + "Content-Type: application/json; charset=UTF-8" + crlf
                + crlf
                + "{\"k\":1}" + crlf
                + "--sep--" + crlf, body);
    }

    @Test
    public void filenamesAndFieldNamesAreHeaderEncoded() throws IOException {
        final var entity = new MultipartFormDataEntity("sep", java.util.List.of(
                FormDataPart.file("upload file", "report \"final\".pdf", ContentType.create("application/pdf"), new java.io.ByteArrayInputStream("bytes".getBytes(StandardCharsets.UTF_8)))
        ));
        final var body = new String(EntityUtils.toByteArray(entity), StandardCharsets.UTF_8);
        Assertions.assertTrue(body.contains("Content-Disposition: form-data; name=\"upload file\"; filename=\"report \\\"final\\\".pdf\""));
        Assertions.assertTrue(body.contains("Content-Type: application/pdf"));
        Assertions.assertTrue(body.endsWith("bytes\r\n--sep--\r\n"));
    }

    @Test
    public void plainFieldNamesStayUnquoted() throws IOException {
        final var entity = new MultipartFormDataEntity("sep", java.util.List.of(
                FormDataPart.field("username", "wyatt")
        ));
        final var body = new String(EntityUtils.toByteArray(entity), StandardCharsets.UTF_8);
        Assertions.assertTrue(body.contains("Content-Disposition: form-data; name=username"));
        Assertions.assertFalse(body.contains("name=\"username\""));
    }

    @Test
    public void entityIsStreamingAndUnchunked() {
        final var entity = new MultipartFormDataEntity("sep", java.util.List.of());
        Assertions.assertTrue(entity.isStreaming());
        Assertions.assertFalse(entity.isChunked());
    }

    private static int count(String haystack, String needle) {
        int count = 0;
        for (int idx = haystack.indexOf(needle); idx != -1; idx = haystack.indexOf(needle, idx + 1)) {
            count++;
        }
        return count;
    }
}
