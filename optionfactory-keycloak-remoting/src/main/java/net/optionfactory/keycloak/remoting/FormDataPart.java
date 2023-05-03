package net.optionfactory.keycloak.remoting;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.apache.http.NameValuePair;
import org.apache.http.entity.ContentType;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHeaderElement;
import org.apache.http.message.BasicNameValuePair;

public class FormDataPart {

    private final List<BasicHeader> headers;
    private final Supplier<InputStream> iss;

    public FormDataPart(List<BasicHeader> headers, Supplier<InputStream> iss) {
        this.headers = headers;
        this.iss = iss;
    }

    public InputStream content() {
        return iss.get();
    }

    public InputStream marshalled() {
        final String joinedHeaders = headers.stream().map(BasicHeader::toString).map(h -> h + "\r\n").collect(Collectors.joining()) + "\r\n";
        final InputStream headersInputStream = new ByteArrayInputStream(joinedHeaders.getBytes(StandardCharsets.UTF_8));
        final InputStream trailer = new ByteArrayInputStream("\r\n".getBytes(StandardCharsets.UTF_8));
        return new SequenceInputStream(Collections.enumeration(List.of(
                headersInputStream,
                iss.get(),
                trailer
        )));
    }

    public static FormDataPart json(String field, String value) {
        final var cdv = new BasicHeaderElement("form-data", null, new NameValuePair[]{
            new BasicNameValuePair("name", BasicHeaderParameterEncoder.encode(field))
        });
        final var headers = List.of(
                new BasicHeader("Content-Disposition", cdv.toString()),
                new BasicHeader("Content-Transfer-Encoding", "8bit"),
                new BasicHeader("Content-Type", ContentType.APPLICATION_JSON.toString())
        );
        return new FormDataPart(headers, () -> new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8)));
    }

    public static FormDataPart field(String field, String value) {
        final var cdv = new BasicHeaderElement("form-data", null, new NameValuePair[]{
            new BasicNameValuePair("name", BasicHeaderParameterEncoder.encode(field))
        });
        final var headers = List.of(
                new BasicHeader("Content-Disposition", cdv.toString()),
                new BasicHeader("Content-Transfer-Encoding", "8bit")
        );
        return new FormDataPart(headers, () -> new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8)));
    }

    public static FormDataPart file(String field, String filename, ContentType contentType, InputStream value) {
        final var cdv = new BasicHeaderElement("form-data", null, new NameValuePair[]{
            new BasicNameValuePair("name", BasicHeaderParameterEncoder.encode(field)),
            new BasicNameValuePair("filename", BasicHeaderParameterEncoder.encode(filename))
        });
        final var headers = List.of(
                new BasicHeader("Content-Disposition", cdv.toString()),
                new BasicHeader("Content-Transfer-Encoding", "8bit"),
                new BasicHeader("Content-Type", contentType.toString())
        );
        return new FormDataPart(headers, () -> value);
    }

}
