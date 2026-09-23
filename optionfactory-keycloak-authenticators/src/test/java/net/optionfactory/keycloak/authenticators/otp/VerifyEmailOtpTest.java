package net.optionfactory.keycloak.authenticators.otp;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.keycloak.authentication.RequiredActionContext;
import org.keycloak.models.RequiredActionConfigModel;
import org.keycloak.models.UserModel;

public class VerifyEmailOtpTest {

    @Test
    public void settingsFallBackToDefaultsOnMissingConfig() {
        Assertions.assertEquals(VerifyEmailOtp.Settings.DEFAULTS, VerifyEmailOtp.Settings.of(null));
        Assertions.assertEquals(VerifyEmailOtp.Settings.DEFAULTS, VerifyEmailOtp.Settings.of(new RequiredActionConfigModel()));
    }

    @Test
    public void settingsIgnoreGarbageAndReadValues() {
        final var config = new RequiredActionConfigModel();
        config.setConfig(Map.of(
                "codeLength", "not-a-number",
                "maxAttempts", "3"));
        Assertions.assertEquals(new VerifyEmailOtp.Settings(6, 900, 3, 30), VerifyEmailOtp.Settings.of(config));
    }

    @Test
    public void settingsAreBounded() {
        final var config = new RequiredActionConfigModel();
        config.setConfig(Map.of(
                "codeLength", "99",
                "codeLifespanSeconds", "1",
                "resendCooldownSeconds", "0"));
        Assertions.assertEquals(new VerifyEmailOtp.Settings(12, 60, 5, 0), VerifyEmailOtp.Settings.of(config));
    }
    @Test
    public void theCodeKeyIsBoundToTheAddressItWasSentTo() {
        final var forOld = VerifyEmailOtp.codeKey("u1", "old@corp.example");
        final var forNew = VerifyEmailOtp.codeKey("u1", "victim@corp.example");

        // a code issued for one address must not be usable to verify another
        Assertions.assertNotEquals(forOld, forNew);
    }

    @Test
    public void theCodeKeySurvivesCasingAndPadding() {
        // an admin retyping the same address must not silently invalidate an outstanding code
        Assertions.assertEquals(
                VerifyEmailOtp.codeKey("u1", "john@corp.example"),
                VerifyEmailOtp.codeKey("u1", "  John@Corp.Example  "));
    }

    @Test
    public void theCodeKeySeparatesUsersSharingAnAddress() {
        Assertions.assertNotEquals(
                VerifyEmailOtp.codeKey("u1", "shared@corp.example"),
                VerifyEmailOtp.codeKey("u2", "shared@corp.example"));
    }

    @Test
    public void theCodeKeyDoesNotCarryTheAddressInClear() {
        // the single-use store is not a place to leave addresses lying around
        Assertions.assertFalse(VerifyEmailOtp.codeKey("u1", "john@corp.example").contains("john@corp.example"));
    }

    @Test
    public void aMissingAddressDoesNotBlowUp() {
        Assertions.assertDoesNotThrow(() -> VerifyEmailOtp.codeKey("u1", null));
    }
    /// A user model backed by a mutable action list, enough for evaluateTriggers: it reads the address and
    /// the verified flag, and writes through addRequiredAction/removeRequiredAction.
    private static UserModel user(String email, boolean emailVerified, List<String> actions) {
        return (UserModel) Proxy.newProxyInstance(VerifyEmailOtpTest.class.getClassLoader(),
                new Class<?>[]{UserModel.class}, (proxy, method, args) -> switch (method.getName()) {
            case "getEmail" ->
                email;
            case "isEmailVerified" ->
                emailVerified;
            case "getRequiredActionsStream" ->
                List.copyOf(actions).stream();
            case "addRequiredAction" -> {
                actions.add((String) args[0]);
                yield null;
            }
            case "removeRequiredAction" -> {
                actions.remove((String) args[0]);
                yield null;
            }
            default ->
                boolean.class.equals(method.getReturnType()) ? false : null;
        });
    }

    private static RequiredActionContext contextFor(UserModel user) {
        return (RequiredActionContext) Proxy.newProxyInstance(VerifyEmailOtpTest.class.getClassLoader(),
                new Class<?>[]{RequiredActionContext.class},
                (proxy, method, args) -> "getUser".equals(method.getName()) ? user : null);
    }

    @Test
    public void anUnverifiedAddressIsChallenged() {
        final var actions = new ArrayList<String>();

        new VerifyEmailOtp().evaluateTriggers(contextFor(user("john@corp.example", false, actions)));

        Assertions.assertEquals(List.of(VerifyEmailOtp.PROVIDER_ID), actions);
    }

    @Test
    public void aUserWithNoAddressIsNeverChallenged() {
        // the action would attach and never clear: only SUCCESS removes one, and such users are ignored
        final var actions = new ArrayList<String>();

        new VerifyEmailOtp().evaluateTriggers(contextFor(user(null, false, actions)));
        new VerifyEmailOtp().evaluateTriggers(contextFor(user("   ", false, actions)));

        Assertions.assertEquals(List.of(), actions);
    }

    @Test
    public void aUserPinnedByTheOldBehaviourIsHealed() {
        final var actions = new ArrayList<>(List.of(VerifyEmailOtp.PROVIDER_ID));

        new VerifyEmailOtp().evaluateTriggers(contextFor(user(null, false, actions)));

        Assertions.assertEquals(List.of(), actions);
    }

    @Test
    public void aPendingUpdateEmailTakesPrecedence() {
        final var actions = new ArrayList<>(List.of(UserModel.RequiredAction.UPDATE_EMAIL.name()));

        new VerifyEmailOtp().evaluateTriggers(contextFor(user("john@corp.example", false, actions)));

        Assertions.assertEquals(List.of(UserModel.RequiredAction.UPDATE_EMAIL.name()), actions);
    }

    @Test
    public void aVerifiedAddressIsLeftAlone() {
        final var actions = new ArrayList<String>();

        new VerifyEmailOtp().evaluateTriggers(contextFor(user("john@corp.example", true, actions)));

        Assertions.assertEquals(List.of(), actions);
    }
}
