package net.optionfactory.keycloak.api.inspection;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.NoResultException;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import net.optionfactory.keycloak.providers.filtering.AttributeFilter;
import net.optionfactory.keycloak.providers.filtering.BooleanFilter;
import net.optionfactory.keycloak.providers.filtering.GroupFilter;
import net.optionfactory.keycloak.providers.filtering.QueryBuilder;
import net.optionfactory.keycloak.providers.filtering.TextFilter;
import net.optionfactory.keycloak.providers.filtering.TimestampFilter;
import net.optionfactory.keycloak.providers.groups.Groups;
import net.optionfactory.keycloak.providers.pagination.PageResponse;
import net.optionfactory.keycloak.providers.pagination.SliceResponse;
import org.keycloak.Config;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.services.resources.admin.AdminEventBuilder;
import org.keycloak.services.resources.admin.ext.AdminRealmResourceProvider;
import org.keycloak.services.resources.admin.ext.AdminRealmResourceProviderFactory;
import org.keycloak.services.resources.admin.fgap.AdminPermissionEvaluator;

/**
 * @see org.keycloak.services.resources.admin.UserResource }
 * @author rferranti
 */
public class InspectionEndpoints {

    private final ObjectMapper om;
    private final KeycloakSession session;

    private static final QueryBuilder USERS_QUERY_TEMPLATE = new QueryBuilder(
            """
        with recursive group_path as (
              select id, name, '/' || name as path from keycloak_group where parent_group = ' '
              union all
              select g.id, g.name, gp.path || '/' || g.name as path from keycloak_group g inner join group_path gp on g.parent_group = gp.id
        )            
        select 
            id, username, email, first_name, last_name, 
            enabled, email_verified, created_timestamp, 
            groups, attributes, federated_identities {COUNT_OVER_TOTAL}
        from 
            user_entity u 
            left join lateral (
                select coalesce(jsonb_object_agg(gp.path, g.id) filter (where g.id is not null), '{}'::jsonb) as groups from user_group_membership ug 
                left join keycloak_group g on ug.group_id = g.id
                left join group_path gp on gp.id = g.id            
                where ug.user_id = u.id
            ) gs on true
            left join lateral (
                with ua as (select name, coalesce(long_value, value) as value from user_attribute where user_id = u.id) select jsonb_agg(ua) as attributes from ua
            ) at on true
            left join lateral (
                select coalesce(jsonb_object_agg(fi.identity_provider, fi.federated_user_id), '{}'::jsonb) as federated_identities 
                from federated_identity fi 
                where fi.user_id = u.id
            ) ft on true            
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

    @GET
    @Path("/users/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    // mapped to be http://localhost:8080/admin/realms/{realm}/inspection/users/{id}
    public Optional<UserResponse> user(@PathParam("id") String id) {
        final var realmId = session.getContext().getRealm().getId();
        final var em = session.getProvider(JpaConnectionProvider.class).getEntityManager();
        final var query = USERS_QUERY_TEMPLATE.create(em, Map.of("id", new String[]{"EQ", "CASE_SENSITIVE", id}), List.of(), false, 0, 1, realmId);
        try {
            final var row = (Object[]) query.getSingleResult();
            return Optional.of(userFromRow(om, row));
        } catch (NoResultException ex) {
            return Optional.empty();
        }
    }

    @GET
    @Path("/users")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    // mapped to be http://localhost:8080/admin/realms/{realm}/inspection/users
    public Optional<UserResponse> userByUsername(@QueryParam("username") String username) {
        final var realmId = session.getContext().getRealm().getId();
        final var em = session.getProvider(JpaConnectionProvider.class).getEntityManager();
        final var query = USERS_QUERY_TEMPLATE.create(em, Map.of("username", new String[]{"EQ", "CASE_SENSITIVE", username}), List.of(), false, 0, 1, realmId);
        try {
            final var row = (Object[]) query.getSingleResult();
            return Optional.of(userFromRow(om, row));
        } catch (NoResultException ex) {
            return Optional.empty();
        }
    }

    @POST
    @Path("/users")
    @Consumes(MediaType.APPLICATION_JSON)
    // mapped to be http://localhost:8080/admin/realms/{realm}/inspection/users
    public Response users(
            @HeaderParam("Accept") MediaType accept,
            Map<String, String[]> filters,
            @QueryParam("sort") List<String> sort,
            @DefaultValue("0") @QueryParam("offset") int offset,
            @DefaultValue("0") @QueryParam("limit") int limit
    ) {
        final var slice = MediaType.valueOf("application/slice+json").equals(accept);

        final var realmId = session.getContext().getRealm().getId();
        final var em = session.getProvider(JpaConnectionProvider.class).getEntityManager();

        final var query = USERS_QUERY_TEMPLATE.create(em, filters, sort, !slice, offset, limit == 0 ? 0 : limit + 1, realmId);
        final AtomicInteger totalAcc = new AtomicInteger();
        final var chunk = ((Stream<Object[]>) query.getResultStream()).map(row -> {
            if (!slice) {
                totalAcc.set(((Number) row[11]).intValue());
            }
            return userFromRow(om, row);
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

    private static UserResponse userFromRow(ObjectMapper om, Object[] row) {
        final UserResponse ur = new UserResponse();
        ur.id = (String) row[0];
        ur.username = (String) row[1];
        ur.email = (String) row[2];
        ur.firstName = (String) row[3];
        ur.lastName = (String) row[4];
        ur.enabled = (Boolean) row[5];
        ur.emailVerified = (Boolean) row[6];
        ur.createdAt = row[7] == null ? null : Instant.ofEpochMilli(((Number) row[7]).longValue());
        try {
            ur.groups = row[8] == null ? Map.of() : om.readValue((String) row[8], MAP_TYPE);
            ur.attributes = row[9] == null ? Map.of() : om.readValue((String) row[9], ATTRIBUTES_TYPE).stream().collect(
                    Collectors.toMap(attr -> attr.name(), attr -> List.of(attr.value()), (lhs, rhs) -> Stream.concat(lhs.stream(), rhs.stream()).toList())
            );
            ur.federatedIdentities = row[10] == null ? Map.of() : om.readValue((String) row[10], MAP_TYPE);
            return ur;
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static final QueryBuilder GROUPS_QUERY_TEMPLATE = new QueryBuilder(
            """
            with recursive group_path as (
                select id, name, '/' || name as path from keycloak_group where parent_group = ' ' and realm_id = ?
                union all
                select g.id, g.name, gp.path || '/' || g.name as path from keycloak_group g inner join group_path gp on g.parent_group = gp.id
            )            
            select gp.id, gp.name, gp.path, coalesce(jsonb_object_agg(ue.username, ue.id) filter (where ue.id is not null), '{}'::jsonb) as members
            from group_path gp
                left join user_group_membership ugm on ugm.group_id = gp.id
                left join user_entity ue on ue.id = ugm.user_id
            where 
                1 = 1 {CONDITIONS}
            {ORDER_CLAUSE}        
            group by gp.id, gp,name, gp.path;
            """)
            .filter(new TextFilter("id", "id"))
            .filter(new TextFilter("name", "name"))
            .filter(new TextFilter("path", "path"))
            .sorter("id", "id")
            .sorter("name", "name")
            .sorter("path", "path");

    @POST
    @Path("/groups/membership")
    public List<GroupMemberhipResponse> groupsMemberhip(Map<String, String[]> filters, @QueryParam("sort") List<String> sort) {
        final var realmId = session.getContext().getRealm().getId();
        final var em = session.getProvider(JpaConnectionProvider.class).getEntityManager();
        final var query = GROUPS_QUERY_TEMPLATE.create(em, filters, sort, false, 0, 0, realmId);
        return ((Stream<Object[]>) query.getResultStream()).map(row -> {
            final var id = (String) row[0];
            final var name = (String) row[1];
            final var path = (String) row[2];
            try {
                final var members = om.readValue((String) row[3], MAP_TYPE);
                return new GroupMemberhipResponse(id, name, path, members);
            } catch (JsonProcessingException ex) {
                throw new IllegalStateException(ex);
            }

        }).toList();

    }

    @POST
    @Path("/groups/membership/@by-path")
    public Response groupMemberhip(
            @HeaderParam("Accept") MediaType accept,
            @QueryParam("path") String path,
            @DefaultValue("0") @QueryParam("offset") int offset,
            @DefaultValue("0") @QueryParam("limit") int limit) {
        final var slice = MediaType.valueOf("application/slice+json").equals(accept);
        final var rb = Response.ok()
                .type(slice ? "application/slice+json" : "application/page+json");

        final var realm = session.getContext().getRealm();
        final var group = Groups.search(session, realm, path);
        if (group == null) {
            if (slice) {
                rb.entity(new SliceResponse(List.of(), false));
            }
            return rb.entity(new PageResponse(List.of(), 0)).build();
        }
        final var chunk = session.users().getGroupMembersStream(realm, group, offset, limit == 0 ? null : limit + 1)
                .map(u -> new GroupMember(u.getId(), u.getUsername(), u.getEmail(), u.getFirstName(), u.getLastName()))
                .toList();

        if (!slice) {
            final var fakeSize = limit == 0
                    ? chunk.size()
                    : offset + limit + (chunk.size() > limit ? 1 : 0);
            return rb.entity(new PageResponse(chunk, fakeSize)).build();
        }
        if (limit == 0) {
            return rb.entity(new SliceResponse(chunk, false)).build();
        }
        final var sliceData = chunk.subList(0, Math.min(chunk.size(), limit));
        return rb.entity(new SliceResponse(sliceData, sliceData.size() < limit)).build();
    }

    @POST
    @Path("/groups")
    public List<GroupResponse> groups() {
        final var realm = session.getContext().getRealm();
        return session.groups()
                .getGroupsStream(realm).map(g -> new GroupResponse(g.getId(), g.getName(), KeycloakModelUtils.buildGroupPath(g)))
                .toList();
    }

    public record GroupMember(String id, String username, String email, String firstName, String lastName) {

    }

    public record GroupResponse(String id, String name, String path) {

    }

    public record GroupMemberhipResponse(String id, String name, String path, Map<String, String> members) {

    }

    public static class Factory implements AdminRealmResourceProviderFactory {

        @Override
        public AdminRealmResourceProvider create(KeycloakSession session) {
            return new AdminRealmResourceProvider() {
                @Override
                public Object getResource(KeycloakSession ks, RealmModel rm, AdminPermissionEvaluator auth, AdminEventBuilder aeb) {
                    auth.users().requireView();
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
