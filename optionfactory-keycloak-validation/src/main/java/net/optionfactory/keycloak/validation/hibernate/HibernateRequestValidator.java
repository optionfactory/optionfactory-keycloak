package net.optionfactory.keycloak.validation.hibernate;

import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Locale.LanguageRange;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.HttpHeaders;
import net.optionfactory.keycloak.validation.RequestValidator;
import net.optionfactory.keycloak.validation.RequestValidatorFactory;
import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.hibernate.validator.spi.messageinterpolation.LocaleResolverContext;
import org.jboss.resteasy.core.ResteasyContext;
import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

public class HibernateRequestValidator implements RequestValidator {

    private final Validator validator;

    public HibernateRequestValidator(Locale defaultLocale, Set<Locale> supportedLocales) {
        this.validator = Validation
                .byProvider(HibernateValidator.class)
                .configure()
                .locales(supportedLocales)
                .defaultLocale(defaultLocale)
                .localeResolver(HibernateRequestValidator::resolveLocaleUsingAcceptLanguageHeader)
                .messageInterpolator(new ParameterMessageInterpolator(supportedLocales, defaultLocale, HibernateRequestValidator::resolveLocaleUsingAcceptLanguageHeader, true))
                .buildValidatorFactory()
                .getValidator();
    }

    public static Locale resolveLocaleUsingAcceptLanguageHeader(LocaleResolverContext lrc) {
        final var headers = ResteasyContext.getContextData(HttpHeaders.class);
        if (headers == null) {
            return lrc.getDefaultLocale();
        }
        final var header = headers.getRequestHeaders().getFirst("Accept-Language");
        if (header == null) {
            return lrc.getDefaultLocale();
        }
        final var requested = LanguageRange.parse(header);
        final var supported = lrc.getSupportedLocales();
        final var filtered = Locale.filter(requested, supported);
        return filtered.isEmpty() ? lrc.getDefaultLocale() : filtered.get(0);
    }

    @Override
    public <T> T unwrap(Class<T> k) {
        return (T) validator;
    }

    @Override
    public <T> Set<ConstraintViolation<T>> validate(T request, Class<?>... groups) {
        return validator.validate(request, groups);
    }

    @Override
    public <T> Set<ConstraintViolation<T>> validate(T request, Method m, Object[] parameterValues, Class<?>... groups) {
        return validator.forExecutables().validateParameters(request, m, parameterValues, groups);
    }

    @Override
    public <T> void enforce(T request, Function<Set<ConstraintViolation<T>>, RuntimeException> exFactory, Class<?>... groups) {
        final Set<ConstraintViolation<T>> violations = validator.validate(request, groups);
        enforceNoViolations(violations, exFactory);
    }

    @Override
    public <T> void enforce(T o, Method m, Object[] parameterValues, Function<Set<ConstraintViolation<T>>, RuntimeException> exCtor, Class<?>... groups) {
        final var violations = validator.forExecutables().validateParameters(o, m, parameterValues, groups);
        enforceNoViolations(violations, exCtor);
    }

    private <T> void enforceNoViolations(Set<ConstraintViolation<T>> violations, Function<Set<ConstraintViolation<T>>, RuntimeException> exFactory) throws BadRequestException {
        if (violations.isEmpty()) {
            return;
        }
        throw exFactory.apply(violations);
    }

    public static class Factory implements RequestValidatorFactory {

        private HibernateRequestValidator validator;

        @Override
        public HibernateRequestValidator create(KeycloakSession session) {
            return validator;
        }

        @Override
        public void init(Config.Scope config) {
            final var defaultLocale = Locale.forLanguageTag(config.get("defaultLocale", "it"));
            final var supportedLocales = Stream.of(config.get("supportedLocales", "ar,ca,cs,da,de,en,es,fr,fi,hu,it,ja,lt,nl,no,pl,pt-BR,ru,sk,sv,tr,zh-CN").split(","))
                    .map(String::trim)
                    .filter(ls -> !ls.isEmpty())
                    .map(Locale::forLanguageTag)
                    .collect(Collectors.toSet());
            this.validator = new HibernateRequestValidator(defaultLocale, supportedLocales);
        }

        @Override
        public void postInit(KeycloakSessionFactory factory) {
        }

        @Override
        public void close() {
            this.validator.close();
        }

        @Override
        public String getId() {
            return "opfa-hibernate-request-validator";
        }

    }

}
