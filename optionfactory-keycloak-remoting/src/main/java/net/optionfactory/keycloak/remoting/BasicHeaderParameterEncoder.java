package net.optionfactory.keycloak.remoting;

import java.util.List;
import java.util.Set;

public class BasicHeaderParameterEncoder {

    //see: https://tools.ietf.org/html/rfc2616#section-2.2
    public static final Set<Character> SEPARATORS = Set.of('(', ')', '<', '>', '@', ',', ';', ':', '\\', '\"', '/', '[', ']', '?', '=', '{', '}', ' ', '\t', '\n', '\r');
    public static final List<String[]> TRANSLATIONS = List.of(
            new String[]{"\\", "\\\\"},
            new String[]{"\n", "\\n"},
            new String[]{"\r", "\\r"},
            new String[]{"\t", "\\t"},
            new String[]{"\"", "\\\""}
    );

    public static String encode(String source) {
        if (SEPARATORS.stream().noneMatch(k -> source.contains("" + k))) {
            return source;
        }
        String translated = source;
        for (final var translation : TRANSLATIONS) {
            translated = translated.replace(translation[0], translation[1]);
        }
        return "\"%s\"".formatted(translated);
    }

}
