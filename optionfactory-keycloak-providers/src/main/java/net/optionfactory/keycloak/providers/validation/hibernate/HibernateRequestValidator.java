package net.optionfactory.keycloak.providers.validation.hibernate;

import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Locale.LanguageRange;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import java.util.stream.StreamSupport;
import net.optionfactory.keycloak.providers.validation.Problem;
import net.optionfactory.keycloak.providers.validation.RequestValidator;
import net.optionfactory.keycloak.providers.validation.RequestValidatorFactory;
import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.hibernate.validator.spi.messageinterpolation.LocaleResolverContext;
import org.keycloak.Config;
import org.keycloak.common.util.Resteasy;
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

        final var headers = Resteasy.getContextData(HttpHeaders.class);
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
    public <T> void enforce(T request, Class<?>... groups) {
        final Set<ConstraintViolation<T>> violations = validator.validate(request, groups);
        if (violations.isEmpty()) {
            return;
        }
        final var response = Response.status(Response.Status.BAD_REQUEST)
                .type("application/failures+json")
                .entity(violations.stream().map(v -> {
                    final var path = StreamSupport.stream(v.getPropertyPath().spliterator(), false)
                            .skip(1)
                            .map(node -> node.getIndex() != null ? String.valueOf(node.getIndex()) : node.getName())
                            .collect(Collectors.joining("."));
                    return new Problem("FIELD_ERROR", path, v.getMessage());
                }))
                .build();
        throw new BadRequestException(response);
    }

    @Override
    public <T> void enforce(T request, Function<Set<ConstraintViolation<T>>, RuntimeException> exFactory, Class<?>... groups) {
        final Set<ConstraintViolation<T>> violations = validator.validate(request, groups);
        if (violations.isEmpty()) {
            return;
        }
        throw exFactory.apply(violations);
    }
    
    @Override
    public <T> void enforce(T o, Method m, Object[] parameterValues, Function<Set<ConstraintViolation<T>>, RuntimeException> exFactory, Class<?>... groups) {
        final var violations = validator.forExecutables().validateParameters(o, m, parameterValues, groups);
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
