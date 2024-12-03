package net.optionfactory.keycloak.providers.filtering;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.core.Response;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.optionfactory.keycloak.providers.filtering.ConfiguredSorter.Direction;
import net.optionfactory.keycloak.providers.validation.Problem;

public class Parsers {

    private static final ConcurrentMap<Class<? extends Enum>, Set<String>> CACHE = new ConcurrentHashMap<>();

    public static <T extends Enum<T>> T enumeration(Class<T> k, String value, String path) {
        final var names = CACHE.computeIfAbsent(k, ek -> Stream.of(ek.getEnumConstants()).map(e -> e.name()).collect(Collectors.toSet()));
        if (!names.contains(value)) {
            final var message = String.format("expected one of %s, got: '%s'", names, value);
            throw new BadRequestException(badRequest(path, message));
        }
        return Enum.valueOf(k, value);
    }

    public static Long instant(String value, String path) {
        if (value == null) {
            return null;
        }
        try {
            return Instant.parse(value).toEpochMilli();
        } catch (DateTimeParseException ex) {
            final var message = String.format("expected an iso instant, got: '%s'", value);
            throw new BadRequestException(badRequest(path, message));
        }

    }

    public static List<ConfiguredSorter> sorters(Map<String, String> allowed, List<String> requested) {
        if (requested == null || requested.isEmpty()) {
            return List.of();
        }
        return requested.stream()
                .map(c -> c.split(","))
                .map(nad -> new String[]{nad[0].trim(), nad.length == 2 ? nad[1] : null})
                .filter(nad -> allowed.containsKey(nad[0]))
                .map(nad -> new ConfiguredSorter(allowed.get(nad[0]), "DESC".equals(nad[1]) ? Direction.DESC : Direction.ASC))
                .toList();
    }

    public static void ensure(boolean test, String path, String format, Object... args) {
        if (test) {
            return;
        }
        throw new BadRequestException(badRequest(path, String.format(format, args)));
    }

    private static Response badRequest(String path, String message) {
        return Response.status(Response.Status.BAD_REQUEST)
                .type("application/failures+json")
                .entity(List.of(new Problem("FIELD_ERROR", path, message)))
                .build();
    }
}
