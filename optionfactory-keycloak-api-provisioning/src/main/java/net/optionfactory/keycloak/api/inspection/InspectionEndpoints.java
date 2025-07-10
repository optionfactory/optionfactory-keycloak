package net.optionfactory.keycloak.api.inspection;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import net.optionfactory.keycloak.providers.filtering.AttributeFilter;
import net.optionfactory.keycloak.providers.filtering.BooleanFilter;
import net.optionfactory.keycloak.providers.filtering.GroupFilter;
import net.optionfactory.keycloak.providers.filtering.QueryBuilder;
import net.optionfactory.keycloak.providers.filtering.TextFilter;
import net.optionfactory.keycloak.providers.filtering.TimestampFilter;
import net.optionfactory.keycloak.providers.pagination.PageResponse;
import net.optionfactory.keycloak.providers.pagination.SliceResponse;
import org.keycloak.Config;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;
import org.keycloak.services.ServicesLogger;
import org.keycloak.services.resources.admin.AdminEventBuilder;
import org.keycloak.services.resources.admin.ext.AdminRealmResourceProvider;
import org.keycloak.services.resources.admin.ext.AdminRealmResourceProviderFactory;
import org.keycloak.services.resources.admin.fgap.AdminPermissionEvaluator;

/**
 * @see org.keycloak.services.resources.admin.UserResource }
 * @author rferranti
 */
public class InspectionEndpoints {

    private final ServicesLogger logger = ServicesLogger.LOGGER;
    private final ObjectMapper om;
    private final KeycloakSession session;

    private static final QueryBuilder QUERY_TEMPLATE = new QueryBuilder(
            """
        select 
            id, username, email, first_name, last_name, 
            enabled, email_verified, created_timestamp, 
            groups, attributes {COUNT_OVER_TOTAL}
        from 
            user_entity u 
            left join lateral (
                select jsonb_object_agg(g.name, g.id) as groups from user_group_membership ug 
                inner join keycloak_group g on ug.group_id = g.id
                where ug.user_id = u.id
            ) gs on true
            left join lateral (
                with ua as (select name, coalesce(long_value, value) as value from user_attribute where user_id = u.id) select jsonb_agg(ua) as attributes from ua
            ) at on true
        where 
            service_account_client_link is null
            and realm_id = ?
            {CONDITIONS}
        {ORDER_CLAUSE}
        """)
            .filter(new TextFilter("id", "id"))
            .filter(new TextFilter("username", "username"))
            .filter(new TextFilter("email", "email"))
            .filter(new TextFilter("firstName", "first_name"))
            .filter(new TextFilter("lastName", "last_name"))
            .filter(new BooleanFilter("enabled", "enabled"))
            .filter(new BooleanFilter("emailVerified", "email_verified"))
            .filter(new TimestampFilter("createdAt", "created_timestamp"))
            .filter(new GroupFilter("groups", "groups"))
            .filter(new AttributeFilter("attributes"))
            .sorter("id", "id")
            .sorter("username", "username")
            .sorter("email", "email")
            .sorter("firstName", "first_name")
            .sorter("lastName", "last_name")
            .sorter("enabled", "enabled")
            .sorter("emailVerified", "email_verified")
            .sorter("createdAt", "created_timestamp");

    private static final TypeReference<Map<String, String>> MAP_TYPE = new TypeReference<Map<String, String>>() {
    };

    public record Attribute(String name, String value) {

    }
    private static final TypeReference<List<Attribute>> ATTRIBUTES_TYPE = new TypeReference<List<Attribute>>() {
    };

    public InspectionEndpoints(KeycloakSession session) {
        this.om = new ObjectMapper();
        this.session = session;
    }

    @POST
    @Path("/users")
    @Consumes(MediaType.APPLICATION_JSON)
    // mapped to be http://localhost:8080/admin/realms/{realm}/inspection/users
    // \doS+ ->>
    public Response users(
            @HeaderParam("Accept") MediaType accept,
            Map<String, String[]> filters,
            @QueryParam("sort") List<String> sort,
            @DefaultValue("0") @QueryParam("offset") int offset,
            @DefaultValue("0") @QueryParam("limit") int limit
    ) {

        final var slice = MediaType.valueOf("application/slice+json").equals(accept);

        final var em = session.getProvider(JpaConnectionProvider.class).getEntityManager();

        final var query = QUERY_TEMPLATE.create(em, filters, sort, !slice, offset, limit == 0 ? 0 : limit + 1, session.getContext().getRealm().getId());
        final AtomicInteger totalAcc = new AtomicInteger();
        final var chunk = ((Stream<Object[]>) query.getResultStream()).map(row -> {
            try {
                if (!slice) {
                    totalAcc.set(((Number) row[10]).intValue());
                }
                final UserResponse ur = new UserResponse();
                ur.id = (String) row[0];
                ur.username = (String) row[1];
                ur.email = (String) row[2];
                ur.firstName = (String) row[3];
                ur.lastName = (String) row[4];
                ur.enabled = (Boolean) row[5];
                ur.emailVerified = (Boolean) row[6];
                ur.createdAt = row[7] == null ? null : Instant.ofEpochMilli(((Number) row[7]).longValue());
                ur.groups = row[8] == null ? Map.of() : om.readValue((String) row[8], MAP_TYPE);
                ur.attributes = row[9] == null ? Map.of() : om.readValue((String) row[9], ATTRIBUTES_TYPE).stream().collect(
                        Collectors.toMap(attr -> attr.name(), attr -> List.of(attr.value()), (lhs, rhs) -> Stream.concat(lhs.stream(), rhs.stream()).toList())
                );
                return ur;
            } catch (JsonProcessingException ex) {
                throw new IllegalStateException(ex);
            }
        }).toList();

        final var rb = Response.ok()
                .type(slice ? "application/slice+json" : "application/page+json");

        if (!slice) {
            return rb.entity(new PageResponse(chunk, totalAcc.get())).build();
        }
        if (limit == 0) {
            return rb.entity(new SliceResponse(chunk, false)).build();
        }
        final var sliceData = chunk.subList(0, Math.min(chunk.size(), limit));
        return rb.entity(new SliceResponse(sliceData, sliceData.size() < limit)).build();
    }

    public static class Factory implements AdminRealmResourceProviderFactory {

        @Override
        public AdminRealmResourceProvider create(KeycloakSession session) {
            return new AdminRealmResourceProvider() {
                @Override
                public Object getResource(KeycloakSession ks, RealmModel rm, AdminPermissionEvaluator ape, AdminEventBuilder aeb) {
                    ape.users().requireView();
                    return new InspectionEndpoints(session);
                }

                @Override
                public void close() {

                }

            };
        }

        @Override
        public void init(Config.Scope config) {
        }

        @Override
        public void postInit(KeycloakSessionFactory factory) {

        }

        @Override
        public void close() {

        }

        @Override
        public String getId() {
            return "inspection";
        }

    }

}
