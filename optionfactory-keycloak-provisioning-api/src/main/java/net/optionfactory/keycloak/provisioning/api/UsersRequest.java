package net.optionfactory.keycloak.provisioning.api;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class UsersRequest {

    @Valid
    public ValueFilter id;
    @Valid
    public ValueFilter username;
    @Valid
    public ValueFilter email;
    @Valid
    public ValueFilter firstName;
    @Valid
    public ValueFilter lastName;
    @Valid
    public ValueFilter enabled;
    @Valid
    public ValueFilter emailVerified;
    @Valid
    public ValueFilter createdTimestamp;
    @NotNull
    public List<@Valid ValueFilter> groups;
    @NotNull
    public List<@Valid KeyValueFilter> attributes;

    public enum FilterOp {
        EQ, NEQ, IN, NIN
    }

    public static class ValueFilter {

        @NotNull
        public FilterOp op;
        @NotNull
        public String value;
    }

    public static class KeyValueFilter {

        @NotNull
        public FilterOp op;
        @NotNull
        public String key;
        @NotNull
        public String value;
    }

}
