package net.optionfactory.keycloak.email;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
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
    private final String themeName;
    private final Map<String, CidSource> allowedCids;

    public CidsProvider(Theme theme) {
        this.themeName = theme.getName();
        this.allowedCids = THEME_ID_TO_ALLOWED_CIDS_CACHE.computeIfAbsent(this.themeName, (name) -> {
            final ObjectMapper mapper = new ObjectMapper();
            try (final var is = theme.getResourceAsStream("allowed_cids.json")) {
                // keyed case-insensitively, because the pattern that finds a reference is: an id written
                // 'Logo' in the template and 'logo' in the allowlist used to match nothing and drop the image
                return is == null ? Map.of() : mapper.readValue(is, CID_SOURCE_LIST)
                        .stream()
                        .collect(Collectors.toMap(c -> c.id.toLowerCase(Locale.ROOT), c -> c, (lhs, rhs) -> lhs));
            } catch (IOException ex) {
                // an unreadable allowlist is not a reason to fail the mail. The sender spi declares
                // EmailException and an unchecked one would sail past its catch, turning a theme typo into
                // a 500 on password reset; nothing is embedded until it is fixed, and the empty result is
                // cached so the parse is not retried on every send
                logger.errorf(ex, "email theme '%s' has an unreadable allowed_cids.json: no cid is embedded", name);
                return Map.of();
            }
        });
    }

    private static final TypeReference<List<CidSource>> CID_SOURCE_LIST = new TypeReference<List<CidSource>>() {
    };

    public List<CidSource> cids(String htmlBody) {
        final var matcher = CID_PATTERN.matcher(htmlBody);
        final var matches = new LinkedHashSet<CidSource>();
        while (matcher.find()) {
            final var cid = matcher.group(2);
            final var found = allowedCids.get(cid.toLowerCase(Locale.ROOT));
            if (found == null) {
                logger.infof("in email theme '%s' cid '%s' is referenced in the email but not allowed in allowed_cids.json (%s entries)", themeName, cid, allowedCids.size());
                continue;
            }
            matches.add(found);
        }
        return List.copyOf(matches);
    }

}
