package net.optionfactory.keycloak.email;

import org.junit.jupiter.api.Assertions;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.keycloak.theme.Theme;
import org.junit.jupiter.api.Test;

public class CidsProviderTest {

    private static final AtomicLong THEME_SEQ = new AtomicLong();

    private static Theme themeWithAllowlist(String json) {
        final var name = "test-theme-" + THEME_SEQ.incrementAndGet();
        return (Theme) Proxy.newProxyInstance(Theme.class.getClassLoader(), new Class<?>[]{Theme.class}, (proxy, method, args) -> {
            return switch (method.getName()) {
                case "getName" ->
                    name;
                case "getResourceAsStream" -> {
                    if ("allowed_cids.json".equals(args[0]) && json != null) {
                        yield new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
                    }
                    yield null;
                }
                default ->
                    null;
            };
        });
    }

    private static final String TWO_CIDS = """
    [
      {"id": "logo", "source": "img/logo.png", "mimeType": "image/png"},
      {"id": "banner", "source": "img/banner.png", "mimeType": "image/png"}
    ]
    """;

    private static List<String> ids(List<CidSource> sources) {
        return sources.stream().map(c -> c.id).toList();
    }

    @Test
    public void canMatchSingleQuotedAttribute() {
        final var provider = new CidsProvider(themeWithAllowlist(TWO_CIDS));
        Assertions.assertEquals(List.of("logo"), ids(provider.cids(" <a href='cid:logo'>link</a> ")));
    }

    @Test
    public void canMatchDoubleQuotedAttribute() {
        final var provider = new CidsProvider(themeWithAllowlist(TWO_CIDS));
        Assertions.assertEquals(List.of("logo"), ids(provider.cids(" <a href=\"cid:logo\">link</a> ")));
    }

    @Test
    public void canMatchMultipleAttributes() {
        final var provider = new CidsProvider(themeWithAllowlist(TWO_CIDS));
        Assertions.assertEquals(List.of("logo", "banner"), ids(provider.cids(" <a href='cid:logo' data-ref='cid:banner'>link</a> ")));
    }

    @Test
    public void mismatchedQuotesDoNotMatch() {
        final var provider = new CidsProvider(themeWithAllowlist(TWO_CIDS));
        Assertions.assertEquals(List.of(), provider.cids(" <a href='cid:logo\">link</a> "));
    }

    @Test
    public void unknownCidsAreSkipped() {
        final var provider = new CidsProvider(themeWithAllowlist(TWO_CIDS));
        Assertions.assertEquals(List.of(), provider.cids(" <a href='cid:evil'>link</a> "));
    }

    @Test
    public void repeatedCidsYieldASingleAttachment() {
        final var provider = new CidsProvider(themeWithAllowlist(TWO_CIDS));
        final var cids = provider.cids(" <img src='cid:logo'> <img src=\"cid:logo\"> <img src='cid:logo'> ");
        Assertions.assertEquals(List.of("logo"), ids(cids));
    }

    @Test
    public void mixedKnownAndUnknownCidsKeepTheKnownOnes() {
        final var provider = new CidsProvider(themeWithAllowlist(TWO_CIDS));
        Assertions.assertEquals(List.of("logo", "banner"), ids(provider.cids(" <img src='cid:evil'> <img src='cid:logo'> <img src='cid:banner'> ")));
    }

    @Test
    public void themesWithoutAnAllowlistEmbedNothing() {
        final var provider = new CidsProvider(themeWithAllowlist(null));
        Assertions.assertEquals(List.of(), provider.cids(" <img src='cid:logo'> "));
    }

    @Test
    public void duplicateIdsInTheAllowlistAreTolerated() {
        final var provider = new CidsProvider(themeWithAllowlist("""
        [
          {"id": "logo", "source": "img/one.png", "mimeType": "image/png"},
          {"id": "logo", "source": "img/two.png", "mimeType": "image/png"}
        ]
        """));
        final var cids = provider.cids(" <img src='cid:logo'> ");
        Assertions.assertEquals(1, cids.size());
        Assertions.assertEquals("img/one.png", cids.get(0).source);
    }

    @Test
    public void resolvedSourcesCarryNameAndMimeType() {
        final var provider = new CidsProvider(themeWithAllowlist(TWO_CIDS));
        final var cid = provider.cids(" <img src='cid:logo'> ").get(0);
        Assertions.assertEquals("img/logo.png", cid.source);
        Assertions.assertEquals("image/png", cid.mimeType);
    }

    @Test
    public void caseInsensitiveCidPrefixIsAccepted() {
        final var provider = new CidsProvider(themeWithAllowlist(TWO_CIDS));
        Assertions.assertTrue(ids(provider.cids(" <img src='CID:logo'> ")).contains("logo"));
    }
    @Test
    public void aReferenceIsMatchedWhateverTheCasing() {
        // the pattern that finds references is case insensitive, so the lookup must be too
        final var cids = new CidsProvider(themeWithAllowlist(TWO_CIDS));

        Assertions.assertEquals(List.of("logo"), ids(cids.cids("<img src=\"CID:Logo\">")));
        Assertions.assertEquals(List.of("logo"), ids(cids.cids("<img src=\"cid:LOGO\">")));
    }

    @Test
    public void anAllowlistEntryIsFoundWhateverItsOwnCasing() {
        final var cids = new CidsProvider(themeWithAllowlist("""
        [{"id": "Logo", "source": "img/logo.png", "mimeType": "image/png"}]
        """));

        Assertions.assertEquals(List.of("Logo"), ids(cids.cids("<img src=\"cid:logo\">")));
    }

    @Test
    public void anUnreadableAllowlistEmbedsNothingInsteadOfThrowing() {
        // the sender spi declares EmailException; an unchecked one would escape it and 500 a password reset
        final var cids = Assertions.assertDoesNotThrow(
                () -> new CidsProvider(themeWithAllowlist("{ this is not the list it should be }")));

        Assertions.assertEquals(List.of(), cids.cids("<img src=\"cid:logo\">"));
    }

    @Test
    public void anAllowlistThatIsNotAListEmbedsNothing() {
        final var cids = Assertions.assertDoesNotThrow(
                () -> new CidsProvider(themeWithAllowlist("{\"id\": \"logo\"}")));

        Assertions.assertEquals(List.of(), cids.cids("<img src=\"cid:logo\">"));
    }
}
