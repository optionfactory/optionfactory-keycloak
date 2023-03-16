package net.optionfactory.keycloak.sms.otp;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import net.optionfactory.keycloak.sms.client.SmsClient;
import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

public class ExpiringSmsOtpService implements SmsOtpService {

    private final ConcurrentMap<String, SmsOtp> state;
    private final SmsClient sms;
    private final Random random;
    private final Clock clock;
    private final Duration duration;
    private final int maxTentatives;
    private final GenMode genMode;
    private final String preset;

    public ExpiringSmsOtpService(SmsClient sms, ConcurrentMap<String, SmsOtp> state, Random random, Clock clock, Duration duration, int maxTentatives, GenMode genMode, String preset) {
        this.sms = sms;
        this.state = state;
        this.random = random;
        this.clock = clock;
        this.duration = duration;
        this.maxTentatives = maxTentatives;
        this.genMode = genMode;
        this.preset = preset;
    }

    public enum GenMode {
        RANDOM, PRESET;
    }

    @Override
    public SmsOtp send(String opKey, String mobileNumber, String template) {
        state.values().removeIf(this::otpExpired);
        final SmsOtp otp = new SmsOtp();
        otp.id = opKey;
        otp.value = genMode == GenMode.PRESET ? preset : random.ints(5, 0, 10).mapToObj(String::valueOf).collect(Collectors.joining());
        otp.usedTentatives = 0;
        otp.durationMs = duration.toMillis();
        otp.maxTentatives = maxTentatives;
        otp.creationTimestamp = clock.millis();
        sms.send(mobileNumber, String.format(template, otp.value));
        state.put(otp.id, otp);
        return otp;
    }

    @Override
    public void validate(String opKey, String userOtp) {
        state.values().removeIf(this::otpExpired);
        if (!state.containsKey(opKey)) {
            throw new SmsOtpValidationException("Il codice di verifica non corrisponde");
        }
        final SmsOtp otp = state.get(opKey);
        if (otpExpired(otp)) {
            state.remove(opKey);
            throw new SmsOtpValidationException("Il codice di verifica non corrisponde");
        }
        otp.usedTentatives++;
        if (!otp.value.equals(userOtp.trim())) {
            if (otpExpired(otp)) {
                state.remove(opKey);
            }
            throw new SmsOtpValidationException("Il codice di verifica non corrisponde");
        }
        state.remove(opKey);
    }

    private boolean otpExpired(SmsOtp otp) {
        return otp.usedTentatives >= otp.maxTentatives || clock.millis() - otp.creationTimestamp > otp.durationMs;
    }

    @Override
    public void close() {

    }

    public static class Factory implements SmsOtpServiceFactory {

        private final Logger logger = Logger.getLogger(Factory.class);
        private final AtomicReference<SmsOtpConf> conf = new AtomicReference<>();

        @Override
        public void init(Config.Scope config) {
            final var c = new SmsOtpConf();
            c.random = new SecureRandom();
            c.clock = Clock.systemUTC();
            c.duration = Optional.ofNullable(config.get("duration")).map(ds -> Duration.parse(ds)).orElse(Duration.ofHours(5));
            c.maxTentatives = config.getInt("maxTentatives", 5);
            c.genMode = GenMode.valueOf(config.get("genMode", "RANDOM"));
            c.preset = c.genMode == GenMode.PRESET ? config.get("preset", "40428") : null;
            c.state = new ConcurrentHashMap<>();
            logger.infof("ExpiringSmsOtpServiceFactory configured: duration:%s, maxTentatives:%s, genMode:%s, preset:%s",
                    c.duration,
                    c.maxTentatives,
                    c.genMode,
                    c.preset);
            conf.set(c);
        }

        public static class SmsOtpConf {

            public Random random;
            public Clock clock;
            public Duration duration;
            public int maxTentatives;
            public GenMode genMode;
            public String preset;
            public ConcurrentMap<String, SmsOtp> state;
        }

        @Override
        public void postInit(KeycloakSessionFactory factory) {
        }

        @Override
        public ExpiringSmsOtpService create(KeycloakSession session) {
            final var smsClient = session.getProvider(SmsClient.class);
            final var c = conf.get();
            return new ExpiringSmsOtpService(smsClient, c.state, c.random, c.clock, c.duration, c.maxTentatives, c.genMode, c.preset);
        }

        @Override
        public void close() {
        }

        @Override
        public String getId() {
            return "opfa-sms-otp-expiring";
        }

    }

}
