package net.optionfactory.keycloak.remoting;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BasicHeaderParameterEncoderTest {

    @Test
    public void plainTokensAreLeftUnquoted() {
        Assertions.assertEquals("file-report", BasicHeaderParameterEncoder.encode("file-report"));
        Assertions.assertEquals("report_v1.2", BasicHeaderParameterEncoder.encode("report_v1.2"));
    }

    @Test
    public void tokensContainingSeparatorsAreQuoted() {
        Assertions.assertEquals("\"a b\"", BasicHeaderParameterEncoder.encode("a b"));
        Assertions.assertEquals("\"a=b\"", BasicHeaderParameterEncoder.encode("a=b"));
    }

    @Test
    public void doubleQuotesAreEscaped() {
        Assertions.assertEquals("\"a\\\"b\"", BasicHeaderParameterEncoder.encode("a\"b"));
    }

    @Test
    public void backslashesAreEscapedBeforeOtherCharacters() {
        Assertions.assertEquals("\"a\\\\b\"", BasicHeaderParameterEncoder.encode("a\\b"));
    }

    @Test
    public void controlCharactersAreEscaped() {
        Assertions.assertEquals("\"a\\nb\"", BasicHeaderParameterEncoder.encode("a\nb"));
        Assertions.assertEquals("\"a\\rb\"", BasicHeaderParameterEncoder.encode("a\rb"));
        Assertions.assertEquals("\"a\\tb\"", BasicHeaderParameterEncoder.encode("a\tb"));
    }

    @Test
    public void escapingCannotEscapeTheClosingQuote() {
        // a trailing backslash must be doubled, so the closing quote stays a quote
        Assertions.assertEquals("\"ab\\\\\"", BasicHeaderParameterEncoder.encode("ab\\"));
    }

    @Test
    public void attackerCannotBreakOutOfTheQuotedString() {
        final var encoded = BasicHeaderParameterEncoder.encode("a\"\r\nX-Injected: 1");
        Assertions.assertEquals("\"a\\\"\\r\\nX-Injected: 1\"", encoded);
    }
}
