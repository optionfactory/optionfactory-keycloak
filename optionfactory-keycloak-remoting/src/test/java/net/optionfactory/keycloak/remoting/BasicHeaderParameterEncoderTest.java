package net.optionfactory.keycloak.remoting;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicHeaderElement;
import org.apache.http.message.BasicNameValuePair;
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
    @Test
    public void nothingIsEncodedAsAnEmptyQuotedString() {
        // bare 'filename=' is not a legal parameter: a token cannot be empty
        Assertions.assertEquals("\"\"", BasicHeaderParameterEncoder.encode(""));
    }

    @Test
    public void anEmptyValueSurvivesIntoTheHeader() {
        final var element = new BasicHeaderElement("form-data", null, new NameValuePair[]{
            new BasicNameValuePair("name", BasicHeaderParameterEncoder.encode("field")),
            new BasicNameValuePair("filename", BasicHeaderParameterEncoder.encode(""))
        });

        Assertions.assertEquals("form-data; name=field; filename=\"\"", element.toString());
    }
}
