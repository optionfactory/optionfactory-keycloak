package net.optionfactory.keycloak.email;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import jakarta.activation.DataSource;
import org.keycloak.theme.Theme;

public class CidFromThemeDataSource implements DataSource {

    private final Theme theme;
    private final CidSource cid;

    public CidFromThemeDataSource(Theme theme, CidSource cid) {
        this.theme = theme;
        this.cid = cid;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return theme.getResourceAsStream(cid.source);
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getContentType() {
        return cid.mimeType;
    }

    @Override
    public String getName() {
        return cid.source;
    }

}
