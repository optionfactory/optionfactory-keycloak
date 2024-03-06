package net.optionfactory.keycloak.remoting;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.SequenceInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.message.BasicHeader;

public class MultipartFormDataEntity implements HttpEntity {

    private final String boundary;
    private final List<FormDataPart> parts;

    public MultipartFormDataEntity(String boundary, List<FormDataPart> parts) {
        this.boundary = boundary;
        this.parts = parts;
    }

    @Override
    public boolean isRepeatable() {
        return false;
    }

    @Override
    public boolean isChunked() {
        return false;
    }

    @Override
    public boolean isStreaming() {
        return true;
    }

    @Override
    public long getContentLength() {
        return -1;
    }

    @Override
    public Header getContentType() {
        return new BasicHeader("Content-Type", "multipart/form-data; boundary=%s".formatted(boundary));
    }

    @Override
    public Header getContentEncoding() {
        return null;
    }

    @Override
    public void consumeContent() throws IOException {

    }

    @Override
    public InputStream getContent() throws IOException {
        List<InputStream> streams = parts.stream().map(FormDataPart::marshalled).collect(Collectors.toList());
        List<InputStream> withBoundaries = new ArrayList<>();

        for (InputStream stream : streams) {
            withBoundaries.add(new ByteArrayInputStream("--%s\r\n".formatted(boundary).getBytes(StandardCharsets.US_ASCII)));
            withBoundaries.add(stream);
        }
        withBoundaries.add(new ByteArrayInputStream("--%s--\r\n".formatted(boundary).getBytes(StandardCharsets.US_ASCII)));
        return new SequenceInputStream(Collections.enumeration(withBoundaries));
    }

    @Override
    public void writeTo(OutputStream os) throws IOException {
        getContent().transferTo(os);
    }

}
