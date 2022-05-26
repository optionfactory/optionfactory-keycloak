package net.optionfactory.keycloak.email;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.jboss.logging.Logger;
import org.keycloak.theme.Theme;

public class CidsProvider {

    private static final Map<String, Map<String, CidSource>> THEME_ID_TO_ALLOWED_CIDS_CACHE = new ConcurrentHashMap<>();
    static final Pattern CID_PATTERN = Pattern.compile("([\"'])CID:([A-Z0-9-_.]+)\\1", Pattern.CASE_INSENSITIVE);
    private static final Logger logger = Logger.getLogger(CidsProvider.class);
    private final Map<String, CidSource> allowedCids;

    public CidsProvider(Theme theme) {
        this.allowedCids = THEME_ID_TO_ALLOWED_CIDS_CACHE.computeIfAbsent(theme.getName(), (name) -> {
            final ObjectMapper mapper = new ObjectMapper();
            try (final var is = theme.getResourceAsStream("allowed_cids.json")) {
                return is == null ? Map.of() : mapper.readValue(is, CID_SOURCE_LIST)
                        .stream()
                        .collect(Collectors.toMap(c -> c.id, c -> c));
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        });
    }

    private static final TypeReference<List<CidSource>> CID_SOURCE_LIST = new TypeReference<List<CidSource>>() {
    };

    public List<CidSource> cids(String htmlBody) {
        final var matcher = CID_PATTERN.matcher(htmlBody);
        final var matches = new ArrayList<CidSource>();
        while (matcher.find()) {
            final var cid = matcher.group(2);
            final var found = allowedCids.get(cid);
            if (found != null) {
                logger.infof("found a cid referenced in the email but not in allow_cids.json: %s", cid);
                matches.add(found);
            }
        }
        return matches;
    }

}
