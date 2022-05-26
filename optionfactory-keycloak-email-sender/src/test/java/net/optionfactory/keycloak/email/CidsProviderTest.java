package net.optionfactory.keycloak.email;

import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class CidsProviderTest {

    @Test
    public void canMatchSingleQuoteAttribute() {
        final var matcher = CidsProvider.CID_PATTERN.matcher(" <a href='cid:test'>link</a> ");
        final var matches = new ArrayList<String>();
        while (matcher.find()) {
            matches.add(matcher.group(2));
        }
        Assert.assertEquals(List.of("test"), matches);
    }
    @Test
    public void canMatchDoubleQuoteAttribute() {
        final var matcher = CidsProvider.CID_PATTERN.matcher(" <a href=\"cid:test\">link</a> ");
        final var matches = new ArrayList<String>();
        while (matcher.find()) {
            matches.add(matcher.group(2));
        }
        Assert.assertEquals(List.of("test"), matches);
    }
    @Test
    public void canMatchMultiplAttributes() {
        final var matcher = CidsProvider.CID_PATTERN.matcher(" <a href='cid:test' data-ref='cid:test2'>link</a> ");
        final var matches = new ArrayList<String>();
        while (matcher.find()) {
            matches.add(matcher.group(2));
        }
        Assert.assertEquals(List.of("test", "test2"), matches);
    }
}
