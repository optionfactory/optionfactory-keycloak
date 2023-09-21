package net.optionfactory.keycloak.provisioning.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import net.optionfactory.keycloak.provisioning.api.UsersRequest.FilterOp;
import net.optionfactory.keycloak.provisioning.api.UsersRequest.ValueFilter;
import net.optionfactory.keycloak.validation.RequestValidator;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.models.GroupModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserProvider;
import org.keycloak.services.ServicesLogger;

/**
 * @see org.keycloak.services.resources.admin.UserResource }
 * @author rferranti
 */
public class ProvisioningEndpoints {

    private final ServicesLogger logger = ServicesLogger.LOGGER;
    private final ObjectMapper om;
    private final RequestValidator validator;
    private final KeycloakSession session;

    public ProvisioningEndpoints(ObjectMapper om, RequestValidator validator, KeycloakSession session) {
        this.om = om;
        this.validator = validator;
        this.session = session;
    }
    
    private static <T> RuntimeException badRequest(Set<ConstraintViolation<T>> violations) {
        return new BadRequestException(String.format("violations: %s", violations));
    }

    @DELETE
    @Path("/users")
    @Consumes(MediaType.APPLICATION_JSON)
    // mapped to be http://localhost:8080/realms/{realm}/provisioning/users
    public void wipe(List<String> ids) {
        validator.enforce(ids, ProvisioningEndpoints::badRequest);
        final RealmModel realm = session.getContext().getRealm();
        final UserProvider users = session.users();
        for (String id : ids) {
            final UserModel user = users.getUserById(realm, id);
            if (user != null) {
                users.removeUser(realm, user);
            }
        }
    }

    @PUT
    @Path("/users")
    @Consumes(MediaType.APPLICATION_JSON)
    // mapped to be http://localhost:8080/realms/{realm}/provisioning/users
    public void provide(UserProvisioningRequest req) {
        validator.enforce(req, ProvisioningEndpoints::badRequest);

        final RealmModel realm = session.getContext().getRealm();
        final UserProvider users = session.users();

        final UserModel user = Optional.ofNullable(users.getUserById(realm, req.id))
                .orElseGet(() -> users.addUser(realm, req.id, req.username, true, true));

        user.setFirstName(req.firstName);
        user.setLastName(req.lastName);
        user.setEnabled(req.enabled);
        user.setEmail(req.username);
        user.setEmailVerified(req.emailVerified);
        for (Map.Entry<String, List<String>> entry : req.attributes.entrySet()) {
            user.setAttribute(entry.getKey(), entry.getValue());
        }
        for (String reduiredAction : req.reduiredActions) {
            user.addRequiredAction(reduiredAction);
        }
        for (String groupName : req.groups) {
            final GroupModel group = session.groups()
                    .getGroupsStream(realm)
                    .filter(g -> g.getName().equals(groupName))
                    .findFirst()
                    .orElseGet(() -> session.groups().createGroup(realm, groupName));
            user.joinGroup(group);
        }
    }

    public static class UserProvisioningRequest {

        @NotEmpty
        public String id;
        @NotEmpty
        public String username;
        @NotEmpty
        public String firstName;
        @NotEmpty
        public String lastName;
        @NotNull
        public Map<String, List<String>> attributes;
        @NotNull
        public List<String> groups;
        @NotNull
        public List<String> reduiredActions;
        public boolean enabled;
        public boolean emailVerified;
    }

    @POST
    @Path("/users")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    // mapped to be http://localhost:8080/realms/{realm}/provisioning/users
    // \doS+ ->>
    public PageResponse<UserResponse> users(
            @DefaultValue("0") @QueryParam("offset") int offset,
            @DefaultValue("0") @QueryParam("limit") int limit,
            UsersRequest request
    ) {
        validator.enforce(request, ProvisioningEndpoints::badRequest);

        final var em = session.getProvider(JpaConnectionProvider.class).getEntityManager();

        final var conditions = new ArrayList<String>(List.of("1=1"));
        final var parameters = new ArrayList<Object>();

        Stream.of(ValueFilterSpec.of("id", request.id, String.class),
                ValueFilterSpec.of("username", request.username, String.class),
                ValueFilterSpec.of("email", request.email, String.class),
                ValueFilterSpec.of("first_name", request.firstName, String.class),
                ValueFilterSpec.of("last_name", request.lastName, String.class),
                ValueFilterSpec.of("enabled", request.enabled, boolean.class),
                ValueFilterSpec.of("email_verified", request.emailVerified, boolean.class),
                ValueFilterSpec.of("created_timestamp", request.createdTimestamp, long.class)
        )
                .filter(fs -> fs.filter != null)
                .forEach(fs -> {
                    final var rop = fs.filter.op == FilterOp.NEQ || fs.filter.op == FilterOp.NIN ? "not" : "";
                    final var rendered = fs.filter.op == FilterOp.IN || fs.filter.op == FilterOp.NIN
                            ? String.format("%s position(%s in %s) > 0", rop, placeholder(parameters), fs.field)
                            : String.format("%s %s = %s", rop, placeholder(parameters), fs.field);
                    conditions.add(rendered);
                    if (fs.type == boolean.class) {
                        parameters.add(Boolean.parseBoolean(fs.filter.value));
                    } else if (fs.type == long.class) {
                        parameters.add(Long.parseLong(fs.filter.value));
                    } else {
                        parameters.add(fs.filter.value);
                    }
                });

        for (final net.optionfactory.keycloak.provisioning.api.UsersRequest.ValueFilter gf : request.groups) {
            conditions.add(String.format("%s jsonb_exists(groups,%s)",
                    gf.op == FilterOp.NIN || gf.op == FilterOp.NEQ ? "not" : "",
                    placeholder(parameters)
            ));
            parameters.add(gf.value);
        }
        for (final net.optionfactory.keycloak.provisioning.api.UsersRequest.KeyValueFilter af : request.attributes) {
            if (af.op == FilterOp.EQ || af.op == FilterOp.NEQ) {
                conditions.add(String.format("%s %s = jsonb_object_field_text(attributes, %s)",
                        af.op == FilterOp.NIN || af.op == FilterOp.NEQ ? "not" : "",
                        placeholder(parameters),
                        placeholder(parameters)
                ));
                parameters.add(af.value);
                parameters.add(af.key);
            } else {
                conditions.add(String.format("%s position(%s in jsonb_object_field_text(attributes, %s)) > 0",
                        af.op == FilterOp.NIN || af.op == FilterOp.NEQ ? "not" : "",
                        placeholder(parameters),
                        placeholder(parameters)
                ));
                parameters.add(af.value);
                parameters.add(af.key);
            }
        }

        final var queryTemplate = "select \n"
                + "    id, username, email, first_name, last_name,\n"
                + "    enabled, email_verified, created_timestamp, \n"
                + "    cast(groups as text), cast(attributes as text), total \n"
                + "from(\n"
                + "    select \n"
                + "        id, username, email, first_name, last_name, \n"
                + "        enabled, email_verified, created_timestamp, \n"
                + "        groups, attributes, count(*) over() as total\n"
                + "    from user_entity u \n"
                + "    left join lateral (\n"
                + "        select jsonb_agg(g.name) as groups from user_group_membership ug \n"
                + "        inner join keycloak_group g on ug.group_id = g.id\n"
                + "        where ug.user_id = u.id\n"
                + "    ) gs on true\n"
                + "    left join lateral (\n"
                + "        select jsonb_object_agg(ua.name, ua.value) as attributes from user_attribute ua\n"
                + "        where ua.user_id = u.id\n"
                + "    ) at on true\n"
                + "    where \n"
                + "        service_account_client_link is null\n"
                + "        and %s\n"
                + ") rs";
        final String query = String.format(queryTemplate, conditions.stream().collect(Collectors.joining(" and ")));

        final var q = em.createNativeQuery(query);

        for (int i = 0; i < parameters.size(); i++) {
            q.setParameter(i + 1, parameters.get(i));
        }
        if (offset != 0) {
            q.setFirstResult(offset);
        }
        if (limit != 0) {
            q.setMaxResults(limit);
        }
        final AtomicInteger totalAcc = new AtomicInteger();
        final var slice = ((Stream<Object[]>) q.getResultStream()).map(row -> {
            try {
                totalAcc.set(((Number) row[10]).intValue());
                final UserResponse ur = new UserResponse();
                ur.id = (String) row[0];
                ur.username = (String) row[1];
                ur.email = (String) row[2];
                ur.firstName = (String) row[3];
                ur.lastName = (String) row[4];
                ur.enabled = (Boolean) row[5];
                ur.emailVerified = (Boolean) row[6];
                ur.createdTimestamp = row[7] == null ? 0 : ((Number) row[7]).longValue();
                ur.groups = row[8] == null ? List.of() : om.readValue((String) row[8], new TypeReference<List<String>>() {
                });
                ur.attributes = row[9] == null ? Map.of() : om.readValue((String) row[9], new TypeReference<Map<String, String>>() {
                });
                return ur;
            } catch (JsonProcessingException ex) {
                throw new IllegalStateException(ex);
            }
        }).collect(Collectors.toList());

        return PageResponse.of(slice, totalAcc.get());
    }

    private static String placeholder(List<Object> params) {
        return String.format("?%d", params.size() + 1);
    }

    private static class ValueFilterSpec {

        public String field;
        public ValueFilter filter;
        public Class<?> type;

        public static ValueFilterSpec of(String fieldName, ValueFilter filter, Class<?> type) {
            final var f = new ValueFilterSpec();
            f.field = fieldName;
            f.filter = filter;
            f.type = type;
            return f;
        }

    }

}
