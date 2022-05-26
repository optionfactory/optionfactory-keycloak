package net.optionfactory.keycloak.email;

public class CidSource {

        public String source;
        public String id;
        public String mimeType;

        public static CidSource of(String source, String id, String mimeType) {
            final var cs = new CidSource();
            cs.source = source;
            cs.id = id;
            cs.mimeType = mimeType;
            return cs;
        }
}
