package net.optionfactory.keycloak.providers.filtering;

import org.junit.jupiter.api.Assertions;
import jakarta.ws.rs.BadRequestException;
import org.junit.jupiter.api.Test;

public class AttributeFilterTest {

    private static final AttributeFilter ATTRIBUTES = new AttributeFilter("attributes");

    @Test
    public void equalityWithNullCompilesToCorrelatedSubselect() {
        final var cf = ATTRIBUTES.configure(new String[]{"EQ", "CASE_SENSITIVE", "fiscal-code", null});
        Assertions.assertEquals("id in (select user_id from user_attribute where user_id = u.id and name = ? and coalesce(long_value, value) is null )", cf.expression());
        Assertions.assertArrayEquals(new Object[]{"fiscal-code"}, cf.parameters());
    }

    @Test
    public void inequalityWithNullCompilesToCorrelatedSubselect() {
        final var cf = ATTRIBUTES.configure(new String[]{"NEQ", "CASE_SENSITIVE", "fiscal-code", null});
        Assertions.assertEquals("id in (select user_id from user_attribute where user_id = u.id and name = ? and coalesce(long_value, value) is not null )", cf.expression());
        Assertions.assertArrayEquals(new Object[]{"fiscal-code"}, cf.parameters());
    }

    @Test
    public void caseSensitiveEqualityBindsFieldAndValue() {
        final var cf = ATTRIBUTES.configure(new String[]{"EQ", "CASE_SENSITIVE", "fiscal-code", "ABC123"});
        Assertions.assertEquals("id in (select user_id from user_attribute where user_id = u.id and name = ? and coalesce(long_value, value) = ? )", cf.expression());
        Assertions.assertArrayEquals(new Object[]{"fiscal-code", "ABC123"}, cf.parameters());
    }

    @Test
    public void caseInsensitiveEqualityLowersValue() {
        final var cf = ATTRIBUTES.configure(new String[]{"EQ", "IGNORE_CASE", "fiscal-code", "AbC123"});
        Assertions.assertEquals("id in (select user_id from user_attribute where user_id = u.id and name = ? and lower(coalesce(long_value, value)) = ? )", cf.expression());
        Assertions.assertArrayEquals(new Object[]{"fiscal-code", "abc123"}, cf.parameters());
    }

    @Test
    public void containsCompilesToLikeSubselectWithEscapedPattern() {
        final var cf = ATTRIBUTES.configure(new String[]{"CONTAINS", "CASE_SENSITIVE", "fiscal-code", "ABC%"});
        Assertions.assertEquals("id in (select user_id from user_attribute where name = ? and coalesce(long_value, value) like ? )", cf.expression());
        Assertions.assertArrayEquals(new Object[]{"fiscal-code", "%ABC\\%%"}, cf.parameters());
    }

    @Test
    public void attributeNamesNeverReachTheGeneratedSql() {
        final var cf = ATTRIBUTES.configure(new String[]{"EQ", "CASE_SENSITIVE", "name = 'admin' or 1=1 --", "x"});
        Assertions.assertEquals("id in (select user_id from user_attribute where user_id = u.id and name = ? and coalesce(long_value, value) = ? )", cf.expression());
    }

    @Test
    public void malformedRequestsAreRejected() {
        Assertions.assertThrows(BadRequestException.class, () -> ATTRIBUTES.configure(new String[]{"EQ", "CASE_SENSITIVE", "fiscal-code"}));
        Assertions.assertThrows(BadRequestException.class, () -> ATTRIBUTES.configure(new String[]{"STARTS_WITH", "CASE_SENSITIVE", "fiscal-code", null}));
    }
}
