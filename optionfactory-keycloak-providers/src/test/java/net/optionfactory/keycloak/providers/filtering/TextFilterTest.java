package net.optionfactory.keycloak.providers.filtering;

import org.junit.jupiter.api.Assertions;
import jakarta.ws.rs.BadRequestException;
import java.util.List;
import net.optionfactory.keycloak.providers.filtering.TextFilter.CaseSensitivity;
import net.optionfactory.keycloak.providers.filtering.TextFilter.Operator;
import org.junit.jupiter.api.Test;

public class TextFilterTest {

    private static final TextFilter USERNAME = new TextFilter("username", "u.username");

    @Test
    public void equalityWithNullCompilesToIsNull() {
        final var cf = USERNAME.configure(new String[]{"EQ", "CASE_SENSITIVE", null});
        Assertions.assertEquals("u.username is null", cf.expression());
        Assertions.assertArrayEquals(new Object[]{}, cf.parameters());
    }

    @Test
    public void inequalityWithNullCompilesToIsNotNull() {
        final var cf = USERNAME.configure(new String[]{"NEQ", "CASE_SENSITIVE", null});
        Assertions.assertEquals("u.username is not null", cf.expression());
        Assertions.assertArrayEquals(new Object[]{}, cf.parameters());
    }

    @Test
    public void orderingOperatorsRejectNullValues() {
        Assertions.assertThrows(BadRequestException.class, () -> USERNAME.configure(new String[]{"LT", "CASE_SENSITIVE", null}));
        Assertions.assertThrows(BadRequestException.class, () -> USERNAME.configure(new String[]{"GTE", "CASE_SENSITIVE", null}));
    }

    @Test
    public void likeOperatorsRejectNullValues() {
        Assertions.assertThrows(BadRequestException.class, () -> USERNAME.configure(new String[]{"CONTAINS", "CASE_SENSITIVE", null}));
    }

    @Test
    public void caseSensitiveEqualityBindsValueAsIs() {
        final var cf = USERNAME.configure(new String[]{"EQ", "CASE_SENSITIVE", "wyatt"});
        Assertions.assertEquals("u.username = ?", cf.expression());
        Assertions.assertArrayEquals(new Object[]{"wyatt"}, cf.parameters());
    }

    @Test
    public void caseInsensitiveEqualityLowersColumnAndValue() {
        final var cf = USERNAME.configure(new String[]{"EQ", "IGNORE_CASE", "WyAtt"});
        Assertions.assertEquals("lower(u.username) = ?", cf.expression());
        Assertions.assertArrayEquals(new Object[]{"wyatt"}, cf.parameters());
    }

    @Test
    public void caseInsensitiveLoweringIsLocaleIndependent() {
        final var cf = USERNAME.configure(new String[]{"EQ", "IGNORE_CASE", "BÜRO"});
        Assertions.assertArrayEquals(new Object[]{"büro"}, cf.parameters());
    }

    @Test
    public void orderingOperatorsAreSupported() {
        Assertions.assertEquals("u.username > ?", USERNAME.configure(new String[]{"GT", "CASE_SENSITIVE", "m"}).expression());
        Assertions.assertEquals("u.username <= ?", USERNAME.configure(new String[]{"LTE", "CASE_SENSITIVE", "m"}).expression());
        Assertions.assertEquals("u.username <> ?", USERNAME.configure(new String[]{"NEQ", "CASE_SENSITIVE", "m"}).expression());
    }

    @Test
    public void containsUsesLikeAndWrapsValueInWildcards() {
        final var cf = USERNAME.configure(new String[]{"CONTAINS", "CASE_SENSITIVE", "att"});
        Assertions.assertEquals("u.username like ?", cf.expression());
        Assertions.assertArrayEquals(new Object[]{"%att%"}, cf.parameters());
    }

    @Test
    public void containsUsesIlikeWhenIgnoringCase() {
        final var cf = USERNAME.configure(new String[]{"CONTAINS", "IGNORE_CASE", "att"});
        Assertions.assertEquals("u.username ilike ?", cf.expression());
        Assertions.assertArrayEquals(new Object[]{"%att%"}, cf.parameters());
    }

    @Test
    public void startsAndEndsWithAnchorWildcards() {
        Assertions.assertArrayEquals(new Object[]{"wy%"}, USERNAME.configure(new String[]{"STARTS_WITH", "CASE_SENSITIVE", "wy"}).parameters());
        Assertions.assertArrayEquals(new Object[]{"%att"}, USERNAME.configure(new String[]{"ENDS_WITH", "CASE_SENSITIVE", "att"}).parameters());
    }

    @Test
    public void likePatternsEscapeWildcards() {
        Assertions.assertEquals("%100\\%%", TextFilter.likePattern(Operator.CONTAINS, "100%"));
        Assertions.assertEquals("%a\\_b%", TextFilter.likePattern(Operator.CONTAINS, "a_b"));
    }

    @Test
    public void likePatternsEscapeBackslashesSoUserInputCannotEscapeAppendedWildcards() {
        // a value ending in '\' must not neutralize the appended '%'
        Assertions.assertEquals("abc\\\\%", TextFilter.likePattern(Operator.STARTS_WITH, "abc\\"));
        Assertions.assertEquals("%a\\\\b\\%c\\\\%", TextFilter.likePattern(Operator.CONTAINS, "a\\b%c\\"));
    }

    @Test
    public void malformedRequestsAreRejected() {
        Assertions.assertThrows(BadRequestException.class, () -> USERNAME.configure(new String[]{"EQ", "CASE_SENSITIVE"}));
        Assertions.assertThrows(BadRequestException.class, () -> USERNAME.configure(new String[]{"MATCH", "CASE_SENSITIVE", "x"}));
        Assertions.assertThrows(BadRequestException.class, () -> USERNAME.configure(new String[]{"EQ", "WHATEVER", "x"}));
    }

    @Test
    public void userValuesNeverReachTheGeneratedSql() {
        final var injectionAttempts = List.of("x' or '1'='1", "x; drop table users; --", "'; union select password from user_entity --");
        for (final var attempt : injectionAttempts) {
            final var cf = USERNAME.configure(new String[]{"EQ", "CASE_SENSITIVE", attempt});
            Assertions.assertEquals("u.username = ?", cf.expression());
            Assertions.assertArrayEquals(new Object[]{attempt}, cf.parameters());
        }
    }
}
