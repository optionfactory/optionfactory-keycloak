--
-- PostgreSQL database dump
--

-- Dumped from database version 17.2 (Debian 17.2-1.pgdg120+1)
-- Dumped by pg_dump version 17.2 (Debian 17.2-1.pgdg120+1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: keycloak; Type: DATABASE; Schema: -; Owner: postgres
--

CREATE DATABASE keycloak WITH TEMPLATE = template0 ENCODING = 'UTF8' LOCALE_PROVIDER = libc LOCALE = 'en_US.UTF-8';


ALTER DATABASE keycloak OWNER TO postgres;

\connect keycloak

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: admin_event_entity; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.admin_event_entity (
    id character varying(36) NOT NULL,
    admin_event_time bigint,
    realm_id character varying(255),
    operation_type character varying(255),
    auth_realm_id character varying(255),
    auth_client_id character varying(255),
    auth_user_id character varying(255),
    ip_address character varying(255),
    resource_path character varying(2550),
    representation text,
    error character varying(255),
    resource_type character varying(64),
    details_json text
);


ALTER TABLE public.admin_event_entity OWNER TO postgres;

--
-- Name: associated_policy; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.associated_policy (
    policy_id character varying(36) NOT NULL,
    associated_policy_id character varying(36) NOT NULL
);


ALTER TABLE public.associated_policy OWNER TO postgres;

--
-- Name: authentication_execution; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.authentication_execution (
    id character varying(36) NOT NULL,
    alias character varying(255),
    authenticator character varying(36),
    realm_id character varying(36),
    flow_id character varying(36),
    requirement integer,
    priority integer,
    authenticator_flow boolean DEFAULT false NOT NULL,
    auth_flow_id character varying(36),
    auth_config character varying(36)
);


ALTER TABLE public.authentication_execution OWNER TO postgres;

--
-- Name: authentication_flow; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.authentication_flow (
    id character varying(36) NOT NULL,
    alias character varying(255),
    description character varying(255),
    realm_id character varying(36),
    provider_id character varying(36) DEFAULT 'basic-flow'::character varying NOT NULL,
    top_level boolean DEFAULT false NOT NULL,
    built_in boolean DEFAULT false NOT NULL
);


ALTER TABLE public.authentication_flow OWNER TO postgres;

--
-- Name: authenticator_config; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.authenticator_config (
    id character varying(36) NOT NULL,
    alias character varying(255),
    realm_id character varying(36)
);


ALTER TABLE public.authenticator_config OWNER TO postgres;

--
-- Name: authenticator_config_entry; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.authenticator_config_entry (
    authenticator_id character varying(36) NOT NULL,
    value text,
    name character varying(255) NOT NULL
);


ALTER TABLE public.authenticator_config_entry OWNER TO postgres;

--
-- Name: broker_link; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.broker_link (
    identity_provider character varying(255) NOT NULL,
    storage_provider_id character varying(255),
    realm_id character varying(36) NOT NULL,
    broker_user_id character varying(255),
    broker_username character varying(255),
    token text,
    user_id character varying(255) NOT NULL
);


ALTER TABLE public.broker_link OWNER TO postgres;

--
-- Name: client; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.client (
    id character varying(36) NOT NULL,
    enabled boolean DEFAULT false NOT NULL,
    full_scope_allowed boolean DEFAULT false NOT NULL,
    client_id character varying(255),
    not_before integer,
    public_client boolean DEFAULT false NOT NULL,
    secret character varying(255),
    base_url character varying(255),
    bearer_only boolean DEFAULT false NOT NULL,
    management_url character varying(255),
    surrogate_auth_required boolean DEFAULT false NOT NULL,
    realm_id character varying(36),
    protocol character varying(255),
    node_rereg_timeout integer DEFAULT 0,
    frontchannel_logout boolean DEFAULT false NOT NULL,
    consent_required boolean DEFAULT false NOT NULL,
    name character varying(255),
    service_accounts_enabled boolean DEFAULT false NOT NULL,
    client_authenticator_type character varying(255),
    root_url character varying(255),
    description character varying(255),
    registration_token character varying(255),
    standard_flow_enabled boolean DEFAULT true NOT NULL,
    implicit_flow_enabled boolean DEFAULT false NOT NULL,
    direct_access_grants_enabled boolean DEFAULT false NOT NULL,
    always_display_in_console boolean DEFAULT false NOT NULL
);


ALTER TABLE public.client OWNER TO postgres;

--
-- Name: client_attributes; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.client_attributes (
    client_id character varying(36) NOT NULL,
    name character varying(255) NOT NULL,
    value text
);


ALTER TABLE public.client_attributes OWNER TO postgres;

--
-- Name: client_auth_flow_bindings; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.client_auth_flow_bindings (
    client_id character varying(36) NOT NULL,
    flow_id character varying(36),
    binding_name character varying(255) NOT NULL
);


ALTER TABLE public.client_auth_flow_bindings OWNER TO postgres;

--
-- Name: client_initial_access; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.client_initial_access (
    id character varying(36) NOT NULL,
    realm_id character varying(36) NOT NULL,
    "timestamp" integer,
    expiration integer,
    count integer,
    remaining_count integer
);


ALTER TABLE public.client_initial_access OWNER TO postgres;

--
-- Name: client_node_registrations; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.client_node_registrations (
    client_id character varying(36) NOT NULL,
    value integer,
    name character varying(255) NOT NULL
);


ALTER TABLE public.client_node_registrations OWNER TO postgres;

--
-- Name: client_scope; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.client_scope (
    id character varying(36) NOT NULL,
    name character varying(255),
    realm_id character varying(36),
    description character varying(255),
    protocol character varying(255)
);


ALTER TABLE public.client_scope OWNER TO postgres;

--
-- Name: client_scope_attributes; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.client_scope_attributes (
    scope_id character varying(36) NOT NULL,
    value character varying(2048),
    name character varying(255) NOT NULL
);


ALTER TABLE public.client_scope_attributes OWNER TO postgres;

--
-- Name: client_scope_client; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.client_scope_client (
    client_id character varying(255) NOT NULL,
    scope_id character varying(255) NOT NULL,
    default_scope boolean DEFAULT false NOT NULL
);


ALTER TABLE public.client_scope_client OWNER TO postgres;

--
-- Name: client_scope_role_mapping; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.client_scope_role_mapping (
    scope_id character varying(36) NOT NULL,
    role_id character varying(36) NOT NULL
);


ALTER TABLE public.client_scope_role_mapping OWNER TO postgres;

--
-- Name: component; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.component (
    id character varying(36) NOT NULL,
    name character varying(255),
    parent_id character varying(36),
    provider_id character varying(36),
    provider_type character varying(255),
    realm_id character varying(36),
    sub_type character varying(255)
);


ALTER TABLE public.component OWNER TO postgres;

--
-- Name: component_config; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.component_config (
    id character varying(36) NOT NULL,
    component_id character varying(36) NOT NULL,
    name character varying(255) NOT NULL,
    value text
);


ALTER TABLE public.component_config OWNER TO postgres;

--
-- Name: composite_role; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.composite_role (
    composite character varying(36) NOT NULL,
    child_role character varying(36) NOT NULL
);


ALTER TABLE public.composite_role OWNER TO postgres;

--
-- Name: credential; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.credential (
    id character varying(36) NOT NULL,
    salt bytea,
    type character varying(255),
    user_id character varying(36),
    created_date bigint,
    user_label character varying(255),
    secret_data text,
    credential_data text,
    priority integer
);


ALTER TABLE public.credential OWNER TO postgres;

--
-- Name: databasechangelog; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.databasechangelog (
    id character varying(255) NOT NULL,
    author character varying(255) NOT NULL,
    filename character varying(255) NOT NULL,
    dateexecuted timestamp without time zone NOT NULL,
    orderexecuted integer NOT NULL,
    exectype character varying(10) NOT NULL,
    md5sum character varying(35),
    description character varying(255),
    comments character varying(255),
    tag character varying(255),
    liquibase character varying(20),
    contexts character varying(255),
    labels character varying(255),
    deployment_id character varying(10)
);


ALTER TABLE public.databasechangelog OWNER TO postgres;

--
-- Name: databasechangeloglock; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.databasechangeloglock (
    id integer NOT NULL,
    locked boolean NOT NULL,
    lockgranted timestamp without time zone,
    lockedby character varying(255)
);


ALTER TABLE public.databasechangeloglock OWNER TO postgres;

--
-- Name: default_client_scope; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.default_client_scope (
    realm_id character varying(36) NOT NULL,
    scope_id character varying(36) NOT NULL,
    default_scope boolean DEFAULT false NOT NULL
);


ALTER TABLE public.default_client_scope OWNER TO postgres;

--
-- Name: event_entity; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.event_entity (
    id character varying(36) NOT NULL,
    client_id character varying(255),
    details_json character varying(2550),
    error character varying(255),
    ip_address character varying(255),
    realm_id character varying(255),
    session_id character varying(255),
    event_time bigint,
    type character varying(255),
    user_id character varying(255),
    details_json_long_value text
);


ALTER TABLE public.event_entity OWNER TO postgres;

--
-- Name: fed_user_attribute; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.fed_user_attribute (
    id character varying(36) NOT NULL,
    name character varying(255) NOT NULL,
    user_id character varying(255) NOT NULL,
    realm_id character varying(36) NOT NULL,
    storage_provider_id character varying(36),
    value character varying(2024),
    long_value_hash bytea,
    long_value_hash_lower_case bytea,
    long_value text
);


ALTER TABLE public.fed_user_attribute OWNER TO postgres;

--
-- Name: fed_user_consent; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.fed_user_consent (
    id character varying(36) NOT NULL,
    client_id character varying(255),
    user_id character varying(255) NOT NULL,
    realm_id character varying(36) NOT NULL,
    storage_provider_id character varying(36),
    created_date bigint,
    last_updated_date bigint,
    client_storage_provider character varying(36),
    external_client_id character varying(255)
);


ALTER TABLE public.fed_user_consent OWNER TO postgres;

--
-- Name: fed_user_consent_cl_scope; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.fed_user_consent_cl_scope (
    user_consent_id character varying(36) NOT NULL,
    scope_id character varying(36) NOT NULL
);


ALTER TABLE public.fed_user_consent_cl_scope OWNER TO postgres;

--
-- Name: fed_user_credential; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.fed_user_credential (
    id character varying(36) NOT NULL,
    salt bytea,
    type character varying(255),
    created_date bigint,
    user_id character varying(255) NOT NULL,
    realm_id character varying(36) NOT NULL,
    storage_provider_id character varying(36),
    user_label character varying(255),
    secret_data text,
    credential_data text,
    priority integer
);


ALTER TABLE public.fed_user_credential OWNER TO postgres;

--
-- Name: fed_user_group_membership; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.fed_user_group_membership (
    group_id character varying(36) NOT NULL,
    user_id character varying(255) NOT NULL,
    realm_id character varying(36) NOT NULL,
    storage_provider_id character varying(36)
);


ALTER TABLE public.fed_user_group_membership OWNER TO postgres;

--
-- Name: fed_user_required_action; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.fed_user_required_action (
    required_action character varying(255) DEFAULT ' '::character varying NOT NULL,
    user_id character varying(255) NOT NULL,
    realm_id character varying(36) NOT NULL,
    storage_provider_id character varying(36)
);


ALTER TABLE public.fed_user_required_action OWNER TO postgres;

--
-- Name: fed_user_role_mapping; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.fed_user_role_mapping (
    role_id character varying(36) NOT NULL,
    user_id character varying(255) NOT NULL,
    realm_id character varying(36) NOT NULL,
    storage_provider_id character varying(36)
);


ALTER TABLE public.fed_user_role_mapping OWNER TO postgres;

--
-- Name: federated_identity; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.federated_identity (
    identity_provider character varying(255) NOT NULL,
    realm_id character varying(36),
    federated_user_id character varying(255),
    federated_username character varying(255),
    token text,
    user_id character varying(36) NOT NULL
);


ALTER TABLE public.federated_identity OWNER TO postgres;

--
-- Name: federated_user; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.federated_user (
    id character varying(255) NOT NULL,
    storage_provider_id character varying(255),
    realm_id character varying(36) NOT NULL
);


ALTER TABLE public.federated_user OWNER TO postgres;

--
-- Name: group_attribute; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.group_attribute (
    id character varying(36) DEFAULT 'sybase-needs-something-here'::character varying NOT NULL,
    name character varying(255) NOT NULL,
    value character varying(255),
    group_id character varying(36) NOT NULL
);


ALTER TABLE public.group_attribute OWNER TO postgres;

--
-- Name: group_role_mapping; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.group_role_mapping (
    role_id character varying(36) NOT NULL,
    group_id character varying(36) NOT NULL
);


ALTER TABLE public.group_role_mapping OWNER TO postgres;

--
-- Name: identity_provider; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.identity_provider (
    internal_id character varying(36) NOT NULL,
    enabled boolean DEFAULT false NOT NULL,
    provider_alias character varying(255),
    provider_id character varying(255),
    store_token boolean DEFAULT false NOT NULL,
    authenticate_by_default boolean DEFAULT false NOT NULL,
    realm_id character varying(36),
    add_token_role boolean DEFAULT true NOT NULL,
    trust_email boolean DEFAULT false NOT NULL,
    first_broker_login_flow_id character varying(36),
    post_broker_login_flow_id character varying(36),
    provider_display_name character varying(255),
    link_only boolean DEFAULT false NOT NULL,
    organization_id character varying(255),
    hide_on_login boolean DEFAULT false
);


ALTER TABLE public.identity_provider OWNER TO postgres;

--
-- Name: identity_provider_config; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.identity_provider_config (
    identity_provider_id character varying(36) NOT NULL,
    value text,
    name character varying(255) NOT NULL
);


ALTER TABLE public.identity_provider_config OWNER TO postgres;

--
-- Name: identity_provider_mapper; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.identity_provider_mapper (
    id character varying(36) NOT NULL,
    name character varying(255) NOT NULL,
    idp_alias character varying(255) NOT NULL,
    idp_mapper_name character varying(255) NOT NULL,
    realm_id character varying(36) NOT NULL
);


ALTER TABLE public.identity_provider_mapper OWNER TO postgres;

--
-- Name: idp_mapper_config; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.idp_mapper_config (
    idp_mapper_id character varying(36) NOT NULL,
    value text,
    name character varying(255) NOT NULL
);


ALTER TABLE public.idp_mapper_config OWNER TO postgres;

--
-- Name: keycloak_group; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.keycloak_group (
    id character varying(36) NOT NULL,
    name character varying(255),
    parent_group character varying(36) NOT NULL,
    realm_id character varying(36),
    type integer DEFAULT 0 NOT NULL
);


ALTER TABLE public.keycloak_group OWNER TO postgres;

--
-- Name: keycloak_role; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.keycloak_role (
    id character varying(36) NOT NULL,
    client_realm_constraint character varying(255),
    client_role boolean DEFAULT false NOT NULL,
    description character varying(255),
    name character varying(255),
    realm_id character varying(255),
    client character varying(36),
    realm character varying(36)
);


ALTER TABLE public.keycloak_role OWNER TO postgres;

--
-- Name: migration_model; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.migration_model (
    id character varying(36) NOT NULL,
    version character varying(36),
    update_time bigint DEFAULT 0 NOT NULL
);


ALTER TABLE public.migration_model OWNER TO postgres;

--
-- Name: offline_client_session; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.offline_client_session (
    user_session_id character varying(36) NOT NULL,
    client_id character varying(255) NOT NULL,
    offline_flag character varying(4) NOT NULL,
    "timestamp" integer,
    data text,
    client_storage_provider character varying(36) DEFAULT 'local'::character varying NOT NULL,
    external_client_id character varying(255) DEFAULT 'local'::character varying NOT NULL,
    version integer DEFAULT 0
);


ALTER TABLE public.offline_client_session OWNER TO postgres;

--
-- Name: offline_user_session; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.offline_user_session (
    user_session_id character varying(36) NOT NULL,
    user_id character varying(255) NOT NULL,
    realm_id character varying(36) NOT NULL,
    created_on integer NOT NULL,
    offline_flag character varying(4) NOT NULL,
    data text,
    last_session_refresh integer DEFAULT 0 NOT NULL,
    broker_session_id character varying(1024),
    version integer DEFAULT 0
);


ALTER TABLE public.offline_user_session OWNER TO postgres;

--
-- Name: org; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.org (
    id character varying(255) NOT NULL,
    enabled boolean NOT NULL,
    realm_id character varying(255) NOT NULL,
    group_id character varying(255) NOT NULL,
    name character varying(255) NOT NULL,
    description character varying(4000),
    alias character varying(255) NOT NULL,
    redirect_url character varying(2048)
);


ALTER TABLE public.org OWNER TO postgres;

--
-- Name: org_domain; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.org_domain (
    id character varying(36) NOT NULL,
    name character varying(255) NOT NULL,
    verified boolean NOT NULL,
    org_id character varying(255) NOT NULL
);


ALTER TABLE public.org_domain OWNER TO postgres;

--
-- Name: policy_config; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.policy_config (
    policy_id character varying(36) NOT NULL,
    name character varying(255) NOT NULL,
    value text
);


ALTER TABLE public.policy_config OWNER TO postgres;

--
-- Name: protocol_mapper; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.protocol_mapper (
    id character varying(36) NOT NULL,
    name character varying(255) NOT NULL,
    protocol character varying(255) NOT NULL,
    protocol_mapper_name character varying(255) NOT NULL,
    client_id character varying(36),
    client_scope_id character varying(36)
);


ALTER TABLE public.protocol_mapper OWNER TO postgres;

--
-- Name: protocol_mapper_config; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.protocol_mapper_config (
    protocol_mapper_id character varying(36) NOT NULL,
    value text,
    name character varying(255) NOT NULL
);


ALTER TABLE public.protocol_mapper_config OWNER TO postgres;

--
-- Name: realm; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.realm (
    id character varying(36) NOT NULL,
    access_code_lifespan integer,
    user_action_lifespan integer,
    access_token_lifespan integer,
    account_theme character varying(255),
    admin_theme character varying(255),
    email_theme character varying(255),
    enabled boolean DEFAULT false NOT NULL,
    events_enabled boolean DEFAULT false NOT NULL,
    events_expiration bigint,
    login_theme character varying(255),
    name character varying(255),
    not_before integer,
    password_policy character varying(2550),
    registration_allowed boolean DEFAULT false NOT NULL,
    remember_me boolean DEFAULT false NOT NULL,
    reset_password_allowed boolean DEFAULT false NOT NULL,
    social boolean DEFAULT false NOT NULL,
    ssl_required character varying(255),
    sso_idle_timeout integer,
    sso_max_lifespan integer,
    update_profile_on_soc_login boolean DEFAULT false NOT NULL,
    verify_email boolean DEFAULT false NOT NULL,
    master_admin_client character varying(36),
    login_lifespan integer,
    internationalization_enabled boolean DEFAULT false NOT NULL,
    default_locale character varying(255),
    reg_email_as_username boolean DEFAULT false NOT NULL,
    admin_events_enabled boolean DEFAULT false NOT NULL,
    admin_events_details_enabled boolean DEFAULT false NOT NULL,
    edit_username_allowed boolean DEFAULT false NOT NULL,
    otp_policy_counter integer DEFAULT 0,
    otp_policy_window integer DEFAULT 1,
    otp_policy_period integer DEFAULT 30,
    otp_policy_digits integer DEFAULT 6,
    otp_policy_alg character varying(36) DEFAULT 'HmacSHA1'::character varying,
    otp_policy_type character varying(36) DEFAULT 'totp'::character varying,
    browser_flow character varying(36),
    registration_flow character varying(36),
    direct_grant_flow character varying(36),
    reset_credentials_flow character varying(36),
    client_auth_flow character varying(36),
    offline_session_idle_timeout integer DEFAULT 0,
    revoke_refresh_token boolean DEFAULT false NOT NULL,
    access_token_life_implicit integer DEFAULT 0,
    login_with_email_allowed boolean DEFAULT true NOT NULL,
    duplicate_emails_allowed boolean DEFAULT false NOT NULL,
    docker_auth_flow character varying(36),
    refresh_token_max_reuse integer DEFAULT 0,
    allow_user_managed_access boolean DEFAULT false NOT NULL,
    sso_max_lifespan_remember_me integer DEFAULT 0 NOT NULL,
    sso_idle_timeout_remember_me integer DEFAULT 0 NOT NULL,
    default_role character varying(255)
);


ALTER TABLE public.realm OWNER TO postgres;

--
-- Name: realm_attribute; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.realm_attribute (
    name character varying(255) NOT NULL,
    realm_id character varying(36) NOT NULL,
    value text
);


ALTER TABLE public.realm_attribute OWNER TO postgres;

--
-- Name: realm_default_groups; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.realm_default_groups (
    realm_id character varying(36) NOT NULL,
    group_id character varying(36) NOT NULL
);


ALTER TABLE public.realm_default_groups OWNER TO postgres;

--
-- Name: realm_enabled_event_types; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.realm_enabled_event_types (
    realm_id character varying(36) NOT NULL,
    value character varying(255) NOT NULL
);


ALTER TABLE public.realm_enabled_event_types OWNER TO postgres;

--
-- Name: realm_events_listeners; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.realm_events_listeners (
    realm_id character varying(36) NOT NULL,
    value character varying(255) NOT NULL
);


ALTER TABLE public.realm_events_listeners OWNER TO postgres;

--
-- Name: realm_localizations; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.realm_localizations (
    realm_id character varying(255) NOT NULL,
    locale character varying(255) NOT NULL,
    texts text NOT NULL
);


ALTER TABLE public.realm_localizations OWNER TO postgres;

--
-- Name: realm_required_credential; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.realm_required_credential (
    type character varying(255) NOT NULL,
    form_label character varying(255),
    input boolean DEFAULT false NOT NULL,
    secret boolean DEFAULT false NOT NULL,
    realm_id character varying(36) NOT NULL
);


ALTER TABLE public.realm_required_credential OWNER TO postgres;

--
-- Name: realm_smtp_config; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.realm_smtp_config (
    realm_id character varying(36) NOT NULL,
    value character varying(255),
    name character varying(255) NOT NULL
);


ALTER TABLE public.realm_smtp_config OWNER TO postgres;

--
-- Name: realm_supported_locales; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.realm_supported_locales (
    realm_id character varying(36) NOT NULL,
    value character varying(255) NOT NULL
);


ALTER TABLE public.realm_supported_locales OWNER TO postgres;

--
-- Name: redirect_uris; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.redirect_uris (
    client_id character varying(36) NOT NULL,
    value character varying(255) NOT NULL
);


ALTER TABLE public.redirect_uris OWNER TO postgres;

--
-- Name: required_action_config; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.required_action_config (
    required_action_id character varying(36) NOT NULL,
    value text,
    name character varying(255) NOT NULL
);


ALTER TABLE public.required_action_config OWNER TO postgres;

--
-- Name: required_action_provider; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.required_action_provider (
    id character varying(36) NOT NULL,
    alias character varying(255),
    name character varying(255),
    realm_id character varying(36),
    enabled boolean DEFAULT false NOT NULL,
    default_action boolean DEFAULT false NOT NULL,
    provider_id character varying(255),
    priority integer
);


ALTER TABLE public.required_action_provider OWNER TO postgres;

--
-- Name: resource_attribute; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.resource_attribute (
    id character varying(36) DEFAULT 'sybase-needs-something-here'::character varying NOT NULL,
    name character varying(255) NOT NULL,
    value character varying(255),
    resource_id character varying(36) NOT NULL
);


ALTER TABLE public.resource_attribute OWNER TO postgres;

--
-- Name: resource_policy; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.resource_policy (
    resource_id character varying(36) NOT NULL,
    policy_id character varying(36) NOT NULL
);


ALTER TABLE public.resource_policy OWNER TO postgres;

--
-- Name: resource_scope; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.resource_scope (
    resource_id character varying(36) NOT NULL,
    scope_id character varying(36) NOT NULL
);


ALTER TABLE public.resource_scope OWNER TO postgres;

--
-- Name: resource_server; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.resource_server (
    id character varying(36) NOT NULL,
    allow_rs_remote_mgmt boolean DEFAULT false NOT NULL,
    policy_enforce_mode smallint NOT NULL,
    decision_strategy smallint DEFAULT 1 NOT NULL
);


ALTER TABLE public.resource_server OWNER TO postgres;

--
-- Name: resource_server_perm_ticket; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.resource_server_perm_ticket (
    id character varying(36) NOT NULL,
    owner character varying(255) NOT NULL,
    requester character varying(255) NOT NULL,
    created_timestamp bigint NOT NULL,
    granted_timestamp bigint,
    resource_id character varying(36) NOT NULL,
    scope_id character varying(36),
    resource_server_id character varying(36) NOT NULL,
    policy_id character varying(36)
);


ALTER TABLE public.resource_server_perm_ticket OWNER TO postgres;

--
-- Name: resource_server_policy; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.resource_server_policy (
    id character varying(36) NOT NULL,
    name character varying(255) NOT NULL,
    description character varying(255),
    type character varying(255) NOT NULL,
    decision_strategy smallint,
    logic smallint,
    resource_server_id character varying(36) NOT NULL,
    owner character varying(255)
);


ALTER TABLE public.resource_server_policy OWNER TO postgres;

--
-- Name: resource_server_resource; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.resource_server_resource (
    id character varying(36) NOT NULL,
    name character varying(255) NOT NULL,
    type character varying(255),
    icon_uri character varying(255),
    owner character varying(255) NOT NULL,
    resource_server_id character varying(36) NOT NULL,
    owner_managed_access boolean DEFAULT false NOT NULL,
    display_name character varying(255)
);


ALTER TABLE public.resource_server_resource OWNER TO postgres;

--
-- Name: resource_server_scope; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.resource_server_scope (
    id character varying(36) NOT NULL,
    name character varying(255) NOT NULL,
    icon_uri character varying(255),
    resource_server_id character varying(36) NOT NULL,
    display_name character varying(255)
);


ALTER TABLE public.resource_server_scope OWNER TO postgres;

--
-- Name: resource_uris; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.resource_uris (
    resource_id character varying(36) NOT NULL,
    value character varying(255) NOT NULL
);


ALTER TABLE public.resource_uris OWNER TO postgres;

--
-- Name: revoked_token; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.revoked_token (
    id character varying(255) NOT NULL,
    expire bigint NOT NULL
);


ALTER TABLE public.revoked_token OWNER TO postgres;

--
-- Name: role_attribute; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.role_attribute (
    id character varying(36) NOT NULL,
    role_id character varying(36) NOT NULL,
    name character varying(255) NOT NULL,
    value character varying(255)
);


ALTER TABLE public.role_attribute OWNER TO postgres;

--
-- Name: scope_mapping; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.scope_mapping (
    client_id character varying(36) NOT NULL,
    role_id character varying(36) NOT NULL
);


ALTER TABLE public.scope_mapping OWNER TO postgres;

--
-- Name: scope_policy; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.scope_policy (
    scope_id character varying(36) NOT NULL,
    policy_id character varying(36) NOT NULL
);


ALTER TABLE public.scope_policy OWNER TO postgres;

--
-- Name: user_attribute; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.user_attribute (
    name character varying(255) NOT NULL,
    value character varying(255),
    user_id character varying(36) NOT NULL,
    id character varying(36) DEFAULT 'sybase-needs-something-here'::character varying NOT NULL,
    long_value_hash bytea,
    long_value_hash_lower_case bytea,
    long_value text
);


ALTER TABLE public.user_attribute OWNER TO postgres;

--
-- Name: user_consent; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.user_consent (
    id character varying(36) NOT NULL,
    client_id character varying(255),
    user_id character varying(36) NOT NULL,
    created_date bigint,
    last_updated_date bigint,
    client_storage_provider character varying(36),
    external_client_id character varying(255)
);


ALTER TABLE public.user_consent OWNER TO postgres;

--
-- Name: user_consent_client_scope; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.user_consent_client_scope (
    user_consent_id character varying(36) NOT NULL,
    scope_id character varying(36) NOT NULL
);


ALTER TABLE public.user_consent_client_scope OWNER TO postgres;

--
-- Name: user_entity; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.user_entity (
    id character varying(36) NOT NULL,
    email character varying(255),
    email_constraint character varying(255),
    email_verified boolean DEFAULT false NOT NULL,
    enabled boolean DEFAULT false NOT NULL,
    federation_link character varying(255),
    first_name character varying(255),
    last_name character varying(255),
    realm_id character varying(255),
    username character varying(255),
    created_timestamp bigint,
    service_account_client_link character varying(255),
    not_before integer DEFAULT 0 NOT NULL
);


ALTER TABLE public.user_entity OWNER TO postgres;

--
-- Name: user_federation_config; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.user_federation_config (
    user_federation_provider_id character varying(36) NOT NULL,
    value character varying(255),
    name character varying(255) NOT NULL
);


ALTER TABLE public.user_federation_config OWNER TO postgres;

--
-- Name: user_federation_mapper; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.user_federation_mapper (
    id character varying(36) NOT NULL,
    name character varying(255) NOT NULL,
    federation_provider_id character varying(36) NOT NULL,
    federation_mapper_type character varying(255) NOT NULL,
    realm_id character varying(36) NOT NULL
);


ALTER TABLE public.user_federation_mapper OWNER TO postgres;

--
-- Name: user_federation_mapper_config; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.user_federation_mapper_config (
    user_federation_mapper_id character varying(36) NOT NULL,
    value character varying(255),
    name character varying(255) NOT NULL
);


ALTER TABLE public.user_federation_mapper_config OWNER TO postgres;

--
-- Name: user_federation_provider; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.user_federation_provider (
    id character varying(36) NOT NULL,
    changed_sync_period integer,
    display_name character varying(255),
    full_sync_period integer,
    last_sync integer,
    priority integer,
    provider_name character varying(255),
    realm_id character varying(36)
);


ALTER TABLE public.user_federation_provider OWNER TO postgres;

--
-- Name: user_group_membership; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.user_group_membership (
    group_id character varying(36) NOT NULL,
    user_id character varying(36) NOT NULL,
    membership_type character varying(255) NOT NULL
);


ALTER TABLE public.user_group_membership OWNER TO postgres;

--
-- Name: user_required_action; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.user_required_action (
    user_id character varying(36) NOT NULL,
    required_action character varying(255) DEFAULT ' '::character varying NOT NULL
);


ALTER TABLE public.user_required_action OWNER TO postgres;

--
-- Name: user_role_mapping; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.user_role_mapping (
    role_id character varying(255) NOT NULL,
    user_id character varying(36) NOT NULL
);


ALTER TABLE public.user_role_mapping OWNER TO postgres;

--
-- Name: username_login_failure; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.username_login_failure (
    realm_id character varying(36) NOT NULL,
    username character varying(255) NOT NULL,
    failed_login_not_before integer,
    last_failure bigint,
    last_ip_failure character varying(255),
    num_failures integer
);


ALTER TABLE public.username_login_failure OWNER TO postgres;

--
-- Name: web_origins; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.web_origins (
    client_id character varying(36) NOT NULL,
    value character varying(255) NOT NULL
);


ALTER TABLE public.web_origins OWNER TO postgres;

--
-- Data for Name: admin_event_entity; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.admin_event_entity (id, admin_event_time, realm_id, operation_type, auth_realm_id, auth_client_id, auth_user_id, ip_address, resource_path, representation, error, resource_type, details_json) FROM stdin;
\.


--
-- Data for Name: associated_policy; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.associated_policy (policy_id, associated_policy_id) FROM stdin;
\.


--
-- Data for Name: authentication_execution; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.authentication_execution (id, alias, authenticator, realm_id, flow_id, requirement, priority, authenticator_flow, auth_flow_id, auth_config) FROM stdin;
21f9431a-b5d7-4c5a-bb7f-6096939495c5	\N	auth-cookie	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	9a7daaef-09a5-433e-b0e2-e1e8836a02f3	2	10	f	\N	\N
e78ce154-6a77-42ca-b8df-d3524e42255d	\N	auth-spnego	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	9a7daaef-09a5-433e-b0e2-e1e8836a02f3	3	20	f	\N	\N
863c366a-03e0-49a0-9644-426829919a9d	\N	identity-provider-redirector	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	9a7daaef-09a5-433e-b0e2-e1e8836a02f3	2	25	f	\N	\N
25abbbc3-b6f6-4254-917f-1aaaab877c0b	\N	\N	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	9a7daaef-09a5-433e-b0e2-e1e8836a02f3	2	30	t	c82912ba-7afb-4140-a52a-8fecc54337d4	\N
007f1f17-6174-4cc4-91d9-8a352e6ad834	\N	auth-username-password-form	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	c82912ba-7afb-4140-a52a-8fecc54337d4	0	10	f	\N	\N
4261d29e-06cb-4ca2-9be5-a06dc174f8a2	\N	\N	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	c82912ba-7afb-4140-a52a-8fecc54337d4	1	20	t	d394cec4-1b71-46f3-a442-7327753e344f	\N
8a215e7f-6eb4-4e0b-9db0-f6a7a6781f43	\N	conditional-user-configured	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	d394cec4-1b71-46f3-a442-7327753e344f	0	10	f	\N	\N
9b533ac0-83ca-4d39-af3e-3df2ffd9dfcc	\N	auth-otp-form	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	d394cec4-1b71-46f3-a442-7327753e344f	0	20	f	\N	\N
e2cff9cb-9850-47e1-8ee0-9dfd1665c788	\N	direct-grant-validate-username	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	61839121-d7d7-4e32-997c-73c9702e9e4a	0	10	f	\N	\N
63767d8c-7296-47e6-bb53-3d6bc5e37334	\N	direct-grant-validate-password	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	61839121-d7d7-4e32-997c-73c9702e9e4a	0	20	f	\N	\N
0bbf238a-a57c-4576-8c72-f023cd2a3542	\N	\N	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	61839121-d7d7-4e32-997c-73c9702e9e4a	1	30	t	e923f6eb-6bd2-4116-be15-2de7808cdfec	\N
5ee98258-99f1-45a5-bcc9-790bd6a00d3a	\N	conditional-user-configured	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	e923f6eb-6bd2-4116-be15-2de7808cdfec	0	10	f	\N	\N
92c1e112-5c40-4bcf-a6f8-69b750cfd0c7	\N	direct-grant-validate-otp	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	e923f6eb-6bd2-4116-be15-2de7808cdfec	0	20	f	\N	\N
a89c5465-2377-4b2f-a7aa-d83d2da9581b	\N	registration-page-form	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	3d04805b-7311-40e9-bab0-b47d5498efca	0	10	t	5fe7dca9-ea5c-4abb-9a9b-a8c9ff29cd9f	\N
eb5e2ab8-a3d0-4eae-b992-5dbe5c8539db	\N	registration-user-creation	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	5fe7dca9-ea5c-4abb-9a9b-a8c9ff29cd9f	0	20	f	\N	\N
432ece96-dff4-4ae2-a5df-897091547102	\N	registration-password-action	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	5fe7dca9-ea5c-4abb-9a9b-a8c9ff29cd9f	0	50	f	\N	\N
4675030a-4215-4a55-b8a9-4cf0049682a8	\N	registration-recaptcha-action	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	5fe7dca9-ea5c-4abb-9a9b-a8c9ff29cd9f	3	60	f	\N	\N
74f41cb2-d4bd-4368-8c07-8d8744735c22	\N	reset-credentials-choose-user	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	7a551ae1-4e86-42f4-b2c7-e2eff67c6e56	0	10	f	\N	\N
2e89c587-6ea4-4e45-aa5c-c47b88d3eea2	\N	reset-credential-email	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	7a551ae1-4e86-42f4-b2c7-e2eff67c6e56	0	20	f	\N	\N
7b96347e-4339-470c-b49f-9e0dca198a50	\N	reset-password	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	7a551ae1-4e86-42f4-b2c7-e2eff67c6e56	0	30	f	\N	\N
8bbbb426-10db-4559-b0e6-26af92289ef5	\N	\N	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	7a551ae1-4e86-42f4-b2c7-e2eff67c6e56	1	40	t	21913b3c-43a2-4ba3-bfe7-f6e52765208b	\N
4a28004d-680c-42f8-b45a-f83b0cf16f51	\N	conditional-user-configured	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	21913b3c-43a2-4ba3-bfe7-f6e52765208b	0	10	f	\N	\N
8a6be78e-0485-457c-ab58-ba43a0d8d2cd	\N	reset-otp	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	21913b3c-43a2-4ba3-bfe7-f6e52765208b	0	20	f	\N	\N
b9909bb7-ddfd-40d8-b82a-37602048889e	\N	client-secret	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	cf1122d5-566a-4245-90e3-730fc1bcc82e	2	10	f	\N	\N
cd9e2408-6cda-42d9-9024-a336dd5b319d	\N	client-jwt	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	cf1122d5-566a-4245-90e3-730fc1bcc82e	2	20	f	\N	\N
db25fc7a-ef5b-4e3b-b952-726bc033594b	\N	client-secret-jwt	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	cf1122d5-566a-4245-90e3-730fc1bcc82e	2	30	f	\N	\N
8db5e0ad-278c-4bee-b4fc-2f2e61a58e92	\N	client-x509	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	cf1122d5-566a-4245-90e3-730fc1bcc82e	2	40	f	\N	\N
fd402565-cd1d-4522-89a6-cb13341191c5	\N	idp-review-profile	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	7f718844-dfa8-4d65-8a02-aca6ed4c42cf	0	10	f	\N	851fc344-0b6f-4889-b9db-d1a57dff53f5
ecf98572-b476-4637-8e32-d691daf30cf5	\N	\N	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	7f718844-dfa8-4d65-8a02-aca6ed4c42cf	0	20	t	75ecf19e-42ab-4a42-9c4a-99095ebed28f	\N
5a069d0b-658d-4fba-b1a5-ea9f5f343a7c	\N	idp-create-user-if-unique	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	75ecf19e-42ab-4a42-9c4a-99095ebed28f	2	10	f	\N	ebe5b70c-0a93-4bdf-8e47-d023a759bbb4
5f667b53-926e-4e8c-934c-c5b0fe662b90	\N	\N	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	75ecf19e-42ab-4a42-9c4a-99095ebed28f	2	20	t	cb0c8675-103f-4369-b71e-4d305c5abd22	\N
c65dc89e-b199-4c63-83f3-b027e059c5f8	\N	idp-confirm-link	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	cb0c8675-103f-4369-b71e-4d305c5abd22	0	10	f	\N	\N
792f01e1-088a-4242-ad19-b11a9e3adb0a	\N	\N	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	cb0c8675-103f-4369-b71e-4d305c5abd22	0	20	t	f49a79a0-beee-4338-8790-2ff85163a949	\N
172ad314-f640-4307-8428-c480582c3a02	\N	idp-email-verification	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	f49a79a0-beee-4338-8790-2ff85163a949	2	10	f	\N	\N
b54a2a0b-8241-4044-9f1d-d1476da039e2	\N	\N	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	f49a79a0-beee-4338-8790-2ff85163a949	2	20	t	0c95ee16-98ef-4cd1-9677-7c25eb0f5374	\N
326f14f1-6f7e-42d1-8ac8-84b09345ddb5	\N	idp-username-password-form	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	0c95ee16-98ef-4cd1-9677-7c25eb0f5374	0	10	f	\N	\N
94fdb474-0678-4652-90ef-1bc15b05322c	\N	\N	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	0c95ee16-98ef-4cd1-9677-7c25eb0f5374	1	20	t	139164a7-8148-4345-93f5-08409ef53051	\N
01ccef09-6963-473c-b113-f3011a5a13f4	\N	conditional-user-configured	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	139164a7-8148-4345-93f5-08409ef53051	0	10	f	\N	\N
61a85348-608f-4b7d-8aa3-22c60e83c02c	\N	auth-otp-form	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	139164a7-8148-4345-93f5-08409ef53051	0	20	f	\N	\N
7ce28bef-b373-4822-a780-51ec1ec34277	\N	http-basic-authenticator	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	fc54e664-07e7-4b28-a084-5228db72954b	0	10	f	\N	\N
e5939744-4907-416a-a61e-a599ba964a29	\N	docker-http-basic-authenticator	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	3f7c84bd-487e-4099-bc35-e8a1b565613a	0	10	f	\N	\N
f6b66ded-4509-4010-adef-6c84710ac591	\N	auth-cookie	3a9fd404-d708-4128-83e7-ef00fa943752	b6fce5a0-c2dc-49f2-b23a-082ad7a9c31c	2	10	f	\N	\N
23b8b513-ed04-4c0e-9368-38d6d0136fbc	\N	auth-spnego	3a9fd404-d708-4128-83e7-ef00fa943752	b6fce5a0-c2dc-49f2-b23a-082ad7a9c31c	3	20	f	\N	\N
5aa29d2a-26dc-47db-8979-1bf60b425ce9	\N	identity-provider-redirector	3a9fd404-d708-4128-83e7-ef00fa943752	b6fce5a0-c2dc-49f2-b23a-082ad7a9c31c	2	25	f	\N	\N
18bbea81-bf9c-4daa-af8e-6b75cf21a65e	\N	\N	3a9fd404-d708-4128-83e7-ef00fa943752	b6fce5a0-c2dc-49f2-b23a-082ad7a9c31c	2	30	t	bffe8a68-7d55-4a26-8065-f5d101a88b6f	\N
76833561-2c7c-4d24-9895-84c3c8e06a1f	\N	auth-username-password-form	3a9fd404-d708-4128-83e7-ef00fa943752	bffe8a68-7d55-4a26-8065-f5d101a88b6f	0	10	f	\N	\N
7fac4fbd-9eb2-4803-9f7c-7769ac004b86	\N	\N	3a9fd404-d708-4128-83e7-ef00fa943752	bffe8a68-7d55-4a26-8065-f5d101a88b6f	1	20	t	c36886ad-33e6-47ef-899e-fbdf1ec6e665	\N
cb394869-c21b-4440-af45-a3e74ac91b4e	\N	conditional-user-configured	3a9fd404-d708-4128-83e7-ef00fa943752	c36886ad-33e6-47ef-899e-fbdf1ec6e665	0	10	f	\N	\N
064a97dd-3c60-4925-a88a-1dbfdadf2ee9	\N	auth-otp-form	3a9fd404-d708-4128-83e7-ef00fa943752	c36886ad-33e6-47ef-899e-fbdf1ec6e665	0	20	f	\N	\N
b25f9d19-19b6-4be3-9ef9-4e83e9720ee2	\N	\N	3a9fd404-d708-4128-83e7-ef00fa943752	b6fce5a0-c2dc-49f2-b23a-082ad7a9c31c	2	26	t	7d663b79-7eb9-439e-a654-1efc7da6338d	\N
bc269e62-8323-4b58-ab11-9299767361c5	\N	\N	3a9fd404-d708-4128-83e7-ef00fa943752	7d663b79-7eb9-439e-a654-1efc7da6338d	1	10	t	71c8a977-8b23-48b0-a1d0-e484fa4fd2ec	\N
d6ba26ef-0336-4f61-89a1-61435b600995	\N	conditional-user-configured	3a9fd404-d708-4128-83e7-ef00fa943752	71c8a977-8b23-48b0-a1d0-e484fa4fd2ec	0	10	f	\N	\N
1d8dda21-20d1-4f9e-95cb-f6e226a1e11b	\N	organization	3a9fd404-d708-4128-83e7-ef00fa943752	71c8a977-8b23-48b0-a1d0-e484fa4fd2ec	2	20	f	\N	\N
ff04b674-4360-4665-929c-197bfe650d1b	\N	direct-grant-validate-username	3a9fd404-d708-4128-83e7-ef00fa943752	45630bd2-1b7a-490d-b422-cb9e20aedadd	0	10	f	\N	\N
7558e9a7-3c0a-48b7-8f92-d57a3d1ed776	\N	direct-grant-validate-password	3a9fd404-d708-4128-83e7-ef00fa943752	45630bd2-1b7a-490d-b422-cb9e20aedadd	0	20	f	\N	\N
7b49a02f-97ab-4a07-854f-eb6974d65c1e	\N	\N	3a9fd404-d708-4128-83e7-ef00fa943752	45630bd2-1b7a-490d-b422-cb9e20aedadd	1	30	t	2f530fde-ce4c-43b9-bec8-8cd76cf6617b	\N
bcf6b97e-6d60-4d9a-8552-9e134a939288	\N	conditional-user-configured	3a9fd404-d708-4128-83e7-ef00fa943752	2f530fde-ce4c-43b9-bec8-8cd76cf6617b	0	10	f	\N	\N
d51fbe98-946f-4abd-a93f-fa75ff721418	\N	direct-grant-validate-otp	3a9fd404-d708-4128-83e7-ef00fa943752	2f530fde-ce4c-43b9-bec8-8cd76cf6617b	0	20	f	\N	\N
46ea0f1a-27dd-450a-8919-24271a014f1c	\N	registration-page-form	3a9fd404-d708-4128-83e7-ef00fa943752	c7cde766-1c06-4f88-9ec2-51c6d758607b	0	10	t	ed03e7c1-c6f5-451d-bccf-ea37349f190e	\N
92b9fcc8-aa01-43aa-89b0-c4feb8a01e91	\N	registration-user-creation	3a9fd404-d708-4128-83e7-ef00fa943752	ed03e7c1-c6f5-451d-bccf-ea37349f190e	0	20	f	\N	\N
6fe4a0bf-054d-4c93-b1cc-0e3ec9df8d06	\N	registration-password-action	3a9fd404-d708-4128-83e7-ef00fa943752	ed03e7c1-c6f5-451d-bccf-ea37349f190e	0	50	f	\N	\N
417f6a2c-dab8-46ec-ab44-5d5dcdd36e06	\N	registration-recaptcha-action	3a9fd404-d708-4128-83e7-ef00fa943752	ed03e7c1-c6f5-451d-bccf-ea37349f190e	3	60	f	\N	\N
5fd01649-6628-4676-99d5-6e919617f139	\N	registration-terms-and-conditions	3a9fd404-d708-4128-83e7-ef00fa943752	ed03e7c1-c6f5-451d-bccf-ea37349f190e	3	70	f	\N	\N
236ebc7f-5f7f-49a2-8ebc-b24fe1387694	\N	reset-credentials-choose-user	3a9fd404-d708-4128-83e7-ef00fa943752	34a61605-dcec-4a72-8b30-8e05a8183588	0	10	f	\N	\N
e1f10f98-9830-468e-93ec-5fe262d8649d	\N	reset-credential-email	3a9fd404-d708-4128-83e7-ef00fa943752	34a61605-dcec-4a72-8b30-8e05a8183588	0	20	f	\N	\N
1704244f-bad1-486f-b0f1-bf42edc842c3	\N	reset-password	3a9fd404-d708-4128-83e7-ef00fa943752	34a61605-dcec-4a72-8b30-8e05a8183588	0	30	f	\N	\N
327e9f78-f39a-43b9-9bbf-5617c637e67b	\N	\N	3a9fd404-d708-4128-83e7-ef00fa943752	34a61605-dcec-4a72-8b30-8e05a8183588	1	40	t	d882a2e3-59d5-452c-82f5-1d41a4dbd35e	\N
f11995a7-e646-4027-ad20-e77660c27488	\N	conditional-user-configured	3a9fd404-d708-4128-83e7-ef00fa943752	d882a2e3-59d5-452c-82f5-1d41a4dbd35e	0	10	f	\N	\N
49fd422d-869b-429a-a999-6a59cb1ddf5f	\N	reset-otp	3a9fd404-d708-4128-83e7-ef00fa943752	d882a2e3-59d5-452c-82f5-1d41a4dbd35e	0	20	f	\N	\N
cdba3b71-0d65-4065-92f5-5dbdc926ad5c	\N	client-secret	3a9fd404-d708-4128-83e7-ef00fa943752	2a156b38-7495-4c6e-b19d-1d18f71b7b16	2	10	f	\N	\N
54456578-cfbd-4f98-8261-4850b2403849	\N	client-jwt	3a9fd404-d708-4128-83e7-ef00fa943752	2a156b38-7495-4c6e-b19d-1d18f71b7b16	2	20	f	\N	\N
02b33ba6-09c0-440b-88d7-ec716e613afc	\N	client-secret-jwt	3a9fd404-d708-4128-83e7-ef00fa943752	2a156b38-7495-4c6e-b19d-1d18f71b7b16	2	30	f	\N	\N
eac1c336-5019-4079-b5b5-a8aa954d083d	\N	client-x509	3a9fd404-d708-4128-83e7-ef00fa943752	2a156b38-7495-4c6e-b19d-1d18f71b7b16	2	40	f	\N	\N
c399cf5e-ebdf-4d0d-8c87-c767fa1201d1	\N	idp-review-profile	3a9fd404-d708-4128-83e7-ef00fa943752	5df8553d-b6e0-4e80-b28e-01509fe3f789	0	10	f	\N	7f3206e3-4c12-41ed-bfa9-6a3b9f168be8
1e0c570b-3091-4982-954b-39bcd1a4c91b	\N	\N	3a9fd404-d708-4128-83e7-ef00fa943752	5df8553d-b6e0-4e80-b28e-01509fe3f789	0	20	t	55e15184-028d-4205-9341-8dde48845ea6	\N
0f10d948-24f8-48a5-83d2-3a133fa98c4f	\N	idp-create-user-if-unique	3a9fd404-d708-4128-83e7-ef00fa943752	55e15184-028d-4205-9341-8dde48845ea6	2	10	f	\N	9cb65e22-d46b-424f-8885-4ef2351eed88
54a14293-9be4-4b85-b0d1-c683ac57d4fa	\N	\N	3a9fd404-d708-4128-83e7-ef00fa943752	55e15184-028d-4205-9341-8dde48845ea6	2	20	t	77a9f15f-c711-44a6-881e-ea9a76e65ab0	\N
89ff924e-9e80-444f-9b86-d6cd78b571af	\N	idp-confirm-link	3a9fd404-d708-4128-83e7-ef00fa943752	77a9f15f-c711-44a6-881e-ea9a76e65ab0	0	10	f	\N	\N
109cf193-ee84-4295-95e3-a6875eb136c0	\N	\N	3a9fd404-d708-4128-83e7-ef00fa943752	77a9f15f-c711-44a6-881e-ea9a76e65ab0	0	20	t	e5a57f99-46f3-4083-8b4e-69a934c3fa03	\N
bdc19189-143c-425a-9e48-6f683de2315b	\N	idp-email-verification	3a9fd404-d708-4128-83e7-ef00fa943752	e5a57f99-46f3-4083-8b4e-69a934c3fa03	2	10	f	\N	\N
8e0e8a7d-bb1a-4da8-abc2-f3e4a7a8024d	\N	\N	3a9fd404-d708-4128-83e7-ef00fa943752	e5a57f99-46f3-4083-8b4e-69a934c3fa03	2	20	t	6f111a66-a6e2-4fe5-8261-1dbcbd1332a4	\N
ffc5ca93-6d38-4dee-866e-a2838499a140	\N	idp-username-password-form	3a9fd404-d708-4128-83e7-ef00fa943752	6f111a66-a6e2-4fe5-8261-1dbcbd1332a4	0	10	f	\N	\N
325955c2-0e6b-456a-9df7-050d15ce4546	\N	\N	3a9fd404-d708-4128-83e7-ef00fa943752	6f111a66-a6e2-4fe5-8261-1dbcbd1332a4	1	20	t	05974ee9-9d60-44bc-bd40-3594de1c5028	\N
852e97b6-f5d5-40d0-b942-645adcce8158	\N	conditional-user-configured	3a9fd404-d708-4128-83e7-ef00fa943752	05974ee9-9d60-44bc-bd40-3594de1c5028	0	10	f	\N	\N
603f5165-1a42-4feb-b9b2-c1a273c5caeb	\N	auth-otp-form	3a9fd404-d708-4128-83e7-ef00fa943752	05974ee9-9d60-44bc-bd40-3594de1c5028	0	20	f	\N	\N
302d7277-7f92-450b-9531-8dc99589667b	\N	\N	3a9fd404-d708-4128-83e7-ef00fa943752	5df8553d-b6e0-4e80-b28e-01509fe3f789	1	50	t	7bc0f6ba-9c88-42ed-b5f9-4d1317308073	\N
f89fcc47-042a-4fd4-a6ce-617db48b5c23	\N	conditional-user-configured	3a9fd404-d708-4128-83e7-ef00fa943752	7bc0f6ba-9c88-42ed-b5f9-4d1317308073	0	10	f	\N	\N
f915b42f-844c-4aec-8ec3-02c44bff29e5	\N	idp-add-organization-member	3a9fd404-d708-4128-83e7-ef00fa943752	7bc0f6ba-9c88-42ed-b5f9-4d1317308073	0	20	f	\N	\N
528216f4-59ff-4093-9717-391d61d88b12	\N	http-basic-authenticator	3a9fd404-d708-4128-83e7-ef00fa943752	81782db0-86e5-4622-8c00-82e8c92e8293	0	10	f	\N	\N
4c361b79-373b-4476-baf1-6a1d16e05bc1	\N	docker-http-basic-authenticator	3a9fd404-d708-4128-83e7-ef00fa943752	0e6f0ff1-abf1-41e6-9cc8-af6689204782	0	10	f	\N	\N
\.


--
-- Data for Name: authentication_flow; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.authentication_flow (id, alias, description, realm_id, provider_id, top_level, built_in) FROM stdin;
9a7daaef-09a5-433e-b0e2-e1e8836a02f3	browser	browser based authentication	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	basic-flow	t	t
c82912ba-7afb-4140-a52a-8fecc54337d4	forms	Username, password, otp and other auth forms.	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	basic-flow	f	t
d394cec4-1b71-46f3-a442-7327753e344f	Browser - Conditional OTP	Flow to determine if the OTP is required for the authentication	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	basic-flow	f	t
61839121-d7d7-4e32-997c-73c9702e9e4a	direct grant	OpenID Connect Resource Owner Grant	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	basic-flow	t	t
e923f6eb-6bd2-4116-be15-2de7808cdfec	Direct Grant - Conditional OTP	Flow to determine if the OTP is required for the authentication	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	basic-flow	f	t
3d04805b-7311-40e9-bab0-b47d5498efca	registration	registration flow	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	basic-flow	t	t
5fe7dca9-ea5c-4abb-9a9b-a8c9ff29cd9f	registration form	registration form	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	form-flow	f	t
7a551ae1-4e86-42f4-b2c7-e2eff67c6e56	reset credentials	Reset credentials for a user if they forgot their password or something	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	basic-flow	t	t
21913b3c-43a2-4ba3-bfe7-f6e52765208b	Reset - Conditional OTP	Flow to determine if the OTP should be reset or not. Set to REQUIRED to force.	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	basic-flow	f	t
cf1122d5-566a-4245-90e3-730fc1bcc82e	clients	Base authentication for clients	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	client-flow	t	t
7f718844-dfa8-4d65-8a02-aca6ed4c42cf	first broker login	Actions taken after first broker login with identity provider account, which is not yet linked to any Keycloak account	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	basic-flow	t	t
75ecf19e-42ab-4a42-9c4a-99095ebed28f	User creation or linking	Flow for the existing/non-existing user alternatives	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	basic-flow	f	t
cb0c8675-103f-4369-b71e-4d305c5abd22	Handle Existing Account	Handle what to do if there is existing account with same email/username like authenticated identity provider	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	basic-flow	f	t
f49a79a0-beee-4338-8790-2ff85163a949	Account verification options	Method with which to verity the existing account	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	basic-flow	f	t
0c95ee16-98ef-4cd1-9677-7c25eb0f5374	Verify Existing Account by Re-authentication	Reauthentication of existing account	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	basic-flow	f	t
139164a7-8148-4345-93f5-08409ef53051	First broker login - Conditional OTP	Flow to determine if the OTP is required for the authentication	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	basic-flow	f	t
fc54e664-07e7-4b28-a084-5228db72954b	saml ecp	SAML ECP Profile Authentication Flow	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	basic-flow	t	t
3f7c84bd-487e-4099-bc35-e8a1b565613a	docker auth	Used by Docker clients to authenticate against the IDP	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	basic-flow	t	t
b6fce5a0-c2dc-49f2-b23a-082ad7a9c31c	browser	Browser based authentication	3a9fd404-d708-4128-83e7-ef00fa943752	basic-flow	t	t
bffe8a68-7d55-4a26-8065-f5d101a88b6f	forms	Username, password, otp and other auth forms.	3a9fd404-d708-4128-83e7-ef00fa943752	basic-flow	f	t
c36886ad-33e6-47ef-899e-fbdf1ec6e665	Browser - Conditional OTP	Flow to determine if the OTP is required for the authentication	3a9fd404-d708-4128-83e7-ef00fa943752	basic-flow	f	t
7d663b79-7eb9-439e-a654-1efc7da6338d	Organization	\N	3a9fd404-d708-4128-83e7-ef00fa943752	basic-flow	f	t
71c8a977-8b23-48b0-a1d0-e484fa4fd2ec	Browser - Conditional Organization	Flow to determine if the organization identity-first login is to be used	3a9fd404-d708-4128-83e7-ef00fa943752	basic-flow	f	t
45630bd2-1b7a-490d-b422-cb9e20aedadd	direct grant	OpenID Connect Resource Owner Grant	3a9fd404-d708-4128-83e7-ef00fa943752	basic-flow	t	t
2f530fde-ce4c-43b9-bec8-8cd76cf6617b	Direct Grant - Conditional OTP	Flow to determine if the OTP is required for the authentication	3a9fd404-d708-4128-83e7-ef00fa943752	basic-flow	f	t
c7cde766-1c06-4f88-9ec2-51c6d758607b	registration	Registration flow	3a9fd404-d708-4128-83e7-ef00fa943752	basic-flow	t	t
ed03e7c1-c6f5-451d-bccf-ea37349f190e	registration form	Registration form	3a9fd404-d708-4128-83e7-ef00fa943752	form-flow	f	t
34a61605-dcec-4a72-8b30-8e05a8183588	reset credentials	Reset credentials for a user if they forgot their password or something	3a9fd404-d708-4128-83e7-ef00fa943752	basic-flow	t	t
d882a2e3-59d5-452c-82f5-1d41a4dbd35e	Reset - Conditional OTP	Flow to determine if the OTP should be reset or not. Set to REQUIRED to force.	3a9fd404-d708-4128-83e7-ef00fa943752	basic-flow	f	t
2a156b38-7495-4c6e-b19d-1d18f71b7b16	clients	Base authentication for clients	3a9fd404-d708-4128-83e7-ef00fa943752	client-flow	t	t
5df8553d-b6e0-4e80-b28e-01509fe3f789	first broker login	Actions taken after first broker login with identity provider account, which is not yet linked to any Keycloak account	3a9fd404-d708-4128-83e7-ef00fa943752	basic-flow	t	t
55e15184-028d-4205-9341-8dde48845ea6	User creation or linking	Flow for the existing/non-existing user alternatives	3a9fd404-d708-4128-83e7-ef00fa943752	basic-flow	f	t
77a9f15f-c711-44a6-881e-ea9a76e65ab0	Handle Existing Account	Handle what to do if there is existing account with same email/username like authenticated identity provider	3a9fd404-d708-4128-83e7-ef00fa943752	basic-flow	f	t
e5a57f99-46f3-4083-8b4e-69a934c3fa03	Account verification options	Method with which to verity the existing account	3a9fd404-d708-4128-83e7-ef00fa943752	basic-flow	f	t
6f111a66-a6e2-4fe5-8261-1dbcbd1332a4	Verify Existing Account by Re-authentication	Reauthentication of existing account	3a9fd404-d708-4128-83e7-ef00fa943752	basic-flow	f	t
05974ee9-9d60-44bc-bd40-3594de1c5028	First broker login - Conditional OTP	Flow to determine if the OTP is required for the authentication	3a9fd404-d708-4128-83e7-ef00fa943752	basic-flow	f	t
7bc0f6ba-9c88-42ed-b5f9-4d1317308073	First Broker Login - Conditional Organization	Flow to determine if the authenticator that adds organization members is to be used	3a9fd404-d708-4128-83e7-ef00fa943752	basic-flow	f	t
81782db0-86e5-4622-8c00-82e8c92e8293	saml ecp	SAML ECP Profile Authentication Flow	3a9fd404-d708-4128-83e7-ef00fa943752	basic-flow	t	t
0e6f0ff1-abf1-41e6-9cc8-af6689204782	docker auth	Used by Docker clients to authenticate against the IDP	3a9fd404-d708-4128-83e7-ef00fa943752	basic-flow	t	t
\.


--
-- Data for Name: authenticator_config; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.authenticator_config (id, alias, realm_id) FROM stdin;
851fc344-0b6f-4889-b9db-d1a57dff53f5	review profile config	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e
ebe5b70c-0a93-4bdf-8e47-d023a759bbb4	create unique user config	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e
7f3206e3-4c12-41ed-bfa9-6a3b9f168be8	review profile config	3a9fd404-d708-4128-83e7-ef00fa943752
9cb65e22-d46b-424f-8885-4ef2351eed88	create unique user config	3a9fd404-d708-4128-83e7-ef00fa943752
\.


--
-- Data for Name: authenticator_config_entry; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.authenticator_config_entry (authenticator_id, value, name) FROM stdin;
851fc344-0b6f-4889-b9db-d1a57dff53f5	missing	update.profile.on.first.login
ebe5b70c-0a93-4bdf-8e47-d023a759bbb4	false	require.password.update.after.registration
7f3206e3-4c12-41ed-bfa9-6a3b9f168be8	missing	update.profile.on.first.login
9cb65e22-d46b-424f-8885-4ef2351eed88	false	require.password.update.after.registration
\.


--
-- Data for Name: broker_link; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.broker_link (identity_provider, storage_provider_id, realm_id, broker_user_id, broker_username, token, user_id) FROM stdin;
\.


--
-- Data for Name: client; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.client (id, enabled, full_scope_allowed, client_id, not_before, public_client, secret, base_url, bearer_only, management_url, surrogate_auth_required, realm_id, protocol, node_rereg_timeout, frontchannel_logout, consent_required, name, service_accounts_enabled, client_authenticator_type, root_url, description, registration_token, standard_flow_enabled, implicit_flow_enabled, direct_access_grants_enabled, always_display_in_console) FROM stdin;
6b42d9f1-5b82-481b-aab2-4286edf4e8e8	t	f	master-realm	0	f	\N	\N	t	\N	f	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	\N	0	f	f	master Realm	f	client-secret	\N	\N	\N	t	f	f	f
5faa1af4-99ee-4664-83c0-ada476a835be	t	f	account	0	t	\N	/realms/master/account/	f	\N	f	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	openid-connect	0	f	f	${client_account}	f	client-secret	${authBaseUrl}	\N	\N	t	f	f	f
3cbf8aa7-c144-492c-aaf9-859a636cce37	t	f	account-console	0	t	\N	/realms/master/account/	f	\N	f	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	openid-connect	0	f	f	${client_account-console}	f	client-secret	${authBaseUrl}	\N	\N	t	f	f	f
0603646a-8907-459a-91bf-facd78dc0140	t	f	broker	0	f	\N	\N	t	\N	f	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	openid-connect	0	f	f	${client_broker}	f	client-secret	\N	\N	\N	t	f	f	f
193c8e25-0d00-4c45-ad5c-fa2a6e50d8f6	t	t	security-admin-console	0	t	\N	/admin/master/console/	f	\N	f	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	openid-connect	0	f	f	${client_security-admin-console}	f	client-secret	${authAdminUrl}	\N	\N	t	f	f	f
481624e6-0bf9-4e05-9af4-aa150c5763a9	t	t	admin-cli	0	t	\N	\N	f	\N	f	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	openid-connect	0	f	f	${client_admin-cli}	f	client-secret	\N	\N	\N	f	f	t	f
fcaa2ba0-efe8-4ad8-b7e8-6fd188f409bd	t	f	test-realm	0	f	\N	\N	t	\N	f	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	\N	0	f	f	test Realm	f	client-secret	\N	\N	\N	t	f	f	f
2c594784-be48-4508-a8e0-1135e4cb37ae	t	f	realm-management	0	f	\N	\N	t	\N	f	3a9fd404-d708-4128-83e7-ef00fa943752	openid-connect	0	f	f	${client_realm-management}	f	client-secret	\N	\N	\N	t	f	f	f
9d44bf2d-8bad-41b0-a76d-9ef30b0ce5bf	t	f	account	0	t	\N	/realms/test/account/	f	\N	f	3a9fd404-d708-4128-83e7-ef00fa943752	openid-connect	0	f	f	${client_account}	f	client-secret	${authBaseUrl}	\N	\N	t	f	f	f
7b5da870-582a-4bd2-9320-84172f977b88	t	f	account-console	0	t	\N	/realms/test/account/	f	\N	f	3a9fd404-d708-4128-83e7-ef00fa943752	openid-connect	0	f	f	${client_account-console}	f	client-secret	${authBaseUrl}	\N	\N	t	f	f	f
fee15b0e-3064-42a7-867a-d9ba6488cf9f	t	f	broker	0	f	\N	\N	t	\N	f	3a9fd404-d708-4128-83e7-ef00fa943752	openid-connect	0	f	f	${client_broker}	f	client-secret	\N	\N	\N	t	f	f	f
ca3d0ca8-abd5-4605-80df-b32fa0e45a83	t	t	security-admin-console	0	t	\N	/admin/test/console/	f	\N	f	3a9fd404-d708-4128-83e7-ef00fa943752	openid-connect	0	f	f	${client_security-admin-console}	f	client-secret	${authAdminUrl}	\N	\N	t	f	f	f
86099425-6565-442a-ad45-312468b4f1f0	t	t	admin-cli	0	t	\N	\N	f	\N	f	3a9fd404-d708-4128-83e7-ef00fa943752	openid-connect	0	f	f	${client_admin-cli}	f	client-secret	\N	\N	\N	f	f	t	f
27b6abbc-155f-4768-9200-8721b6d0831f	t	t	test-sa-view-client	0	f	LokDtT7ZAwhEvekQcQLLW7c89YQIbkce	http://localhost:8000/	f	http://localhost:8000/	f	3a9fd404-d708-4128-83e7-ef00fa943752	openid-connect	-1	t	f	test-sa-view-client	t	client-secret	http://localhost:8000/		\N	t	f	f	f
\.


--
-- Data for Name: client_attributes; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.client_attributes (client_id, name, value) FROM stdin;
3cbf8aa7-c144-492c-aaf9-859a636cce37	pkce.code.challenge.method	S256
193c8e25-0d00-4c45-ad5c-fa2a6e50d8f6	pkce.code.challenge.method	S256
3cbf8aa7-c144-492c-aaf9-859a636cce37	post.logout.redirect.uris	+
193c8e25-0d00-4c45-ad5c-fa2a6e50d8f6	post.logout.redirect.uris	+
5faa1af4-99ee-4664-83c0-ada476a835be	post.logout.redirect.uris	+
193c8e25-0d00-4c45-ad5c-fa2a6e50d8f6	client.use.lightweight.access.token.enabled	true
481624e6-0bf9-4e05-9af4-aa150c5763a9	client.use.lightweight.access.token.enabled	true
9d44bf2d-8bad-41b0-a76d-9ef30b0ce5bf	post.logout.redirect.uris	+
7b5da870-582a-4bd2-9320-84172f977b88	post.logout.redirect.uris	+
7b5da870-582a-4bd2-9320-84172f977b88	pkce.code.challenge.method	S256
ca3d0ca8-abd5-4605-80df-b32fa0e45a83	post.logout.redirect.uris	+
ca3d0ca8-abd5-4605-80df-b32fa0e45a83	pkce.code.challenge.method	S256
ca3d0ca8-abd5-4605-80df-b32fa0e45a83	client.use.lightweight.access.token.enabled	true
86099425-6565-442a-ad45-312468b4f1f0	client.use.lightweight.access.token.enabled	true
27b6abbc-155f-4768-9200-8721b6d0831f	client.secret.creation.time	1732813418
27b6abbc-155f-4768-9200-8721b6d0831f	oauth2.device.authorization.grant.enabled	false
27b6abbc-155f-4768-9200-8721b6d0831f	oidc.ciba.grant.enabled	false
27b6abbc-155f-4768-9200-8721b6d0831f	backchannel.logout.session.required	true
27b6abbc-155f-4768-9200-8721b6d0831f	backchannel.logout.revoke.offline.tokens	false
27b6abbc-155f-4768-9200-8721b6d0831f	realm_client	false
27b6abbc-155f-4768-9200-8721b6d0831f	display.on.consent.screen	false
27b6abbc-155f-4768-9200-8721b6d0831f	use.refresh.tokens	true
27b6abbc-155f-4768-9200-8721b6d0831f	client_credentials.use_refresh_token	false
27b6abbc-155f-4768-9200-8721b6d0831f	token.response.type.bearer.lower-case	false
27b6abbc-155f-4768-9200-8721b6d0831f	tls.client.certificate.bound.access.tokens	false
27b6abbc-155f-4768-9200-8721b6d0831f	require.pushed.authorization.requests	false
27b6abbc-155f-4768-9200-8721b6d0831f	client.use.lightweight.access.token.enabled	false
27b6abbc-155f-4768-9200-8721b6d0831f	client.introspection.response.allow.jwt.claim.enabled	false
27b6abbc-155f-4768-9200-8721b6d0831f	acr.loa.map	{}
\.


--
-- Data for Name: client_auth_flow_bindings; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.client_auth_flow_bindings (client_id, flow_id, binding_name) FROM stdin;
\.


--
-- Data for Name: client_initial_access; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.client_initial_access (id, realm_id, "timestamp", expiration, count, remaining_count) FROM stdin;
\.


--
-- Data for Name: client_node_registrations; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.client_node_registrations (client_id, value, name) FROM stdin;
\.


--
-- Data for Name: client_scope; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.client_scope (id, name, realm_id, description, protocol) FROM stdin;
f26177d9-ab49-449c-bbc7-050c6ea05305	offline_access	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	OpenID Connect built-in scope: offline_access	openid-connect
c5728f88-7d82-4a3f-95b0-e8394aa20f74	role_list	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	SAML role list	saml
b7fa698d-506e-4edd-ba59-92d1330019f7	profile	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	OpenID Connect built-in scope: profile	openid-connect
57b95975-6d57-4d7a-86bd-3912d154d522	email	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	OpenID Connect built-in scope: email	openid-connect
de577a7b-5939-44fa-aa0f-d0b50991435b	address	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	OpenID Connect built-in scope: address	openid-connect
23331e70-04a9-4ee0-a188-49a8636c01b8	phone	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	OpenID Connect built-in scope: phone	openid-connect
d4ea9c2f-7ae0-4297-8743-e5314bb8e811	roles	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	OpenID Connect scope for add user roles to the access token	openid-connect
bf7a8b48-9bfb-4612-a643-8d7c01c5ae91	web-origins	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	OpenID Connect scope for add allowed web origins to the access token	openid-connect
180ede63-bd73-4832-b8aa-f930d57263d5	microprofile-jwt	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	Microprofile - JWT built-in scope	openid-connect
b5a5d94a-d9d0-4dde-ad70-7b629561ad61	acr	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	OpenID Connect scope for add acr (authentication context class reference) to the token	openid-connect
9d11eb69-e2dd-416b-8c04-d35444356a47	provisioning	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	provisioning api access scope	openid-connect
6f3da124-ce69-4e6c-b933-35dfc3e58221	basic	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	OpenID Connect scope for add all basic claims to the token	openid-connect
c9b2c9be-e9f9-4eda-847a-b8f20265ed48	offline_access	3a9fd404-d708-4128-83e7-ef00fa943752	OpenID Connect built-in scope: offline_access	openid-connect
59352ece-9aad-4086-ac75-eb145c579d2d	role_list	3a9fd404-d708-4128-83e7-ef00fa943752	SAML role list	saml
50169b72-4790-45a2-9a55-1f1797bc287b	saml_organization	3a9fd404-d708-4128-83e7-ef00fa943752	Organization Membership	saml
3809229d-762a-41ad-b8da-9175829b87e4	profile	3a9fd404-d708-4128-83e7-ef00fa943752	OpenID Connect built-in scope: profile	openid-connect
748acf76-9899-4a7c-b222-7a4bd53d21a1	email	3a9fd404-d708-4128-83e7-ef00fa943752	OpenID Connect built-in scope: email	openid-connect
e54f533d-3819-4345-9502-8c3b57ebb6a6	address	3a9fd404-d708-4128-83e7-ef00fa943752	OpenID Connect built-in scope: address	openid-connect
5b4a9626-9522-4fdf-89fc-6abe5538a593	phone	3a9fd404-d708-4128-83e7-ef00fa943752	OpenID Connect built-in scope: phone	openid-connect
5c60430f-7841-4c66-acb4-0d372753c861	roles	3a9fd404-d708-4128-83e7-ef00fa943752	OpenID Connect scope for add user roles to the access token	openid-connect
d7a399be-d14a-43bf-9a5a-cde8802b9168	web-origins	3a9fd404-d708-4128-83e7-ef00fa943752	OpenID Connect scope for add allowed web origins to the access token	openid-connect
dc3fd5f4-803e-4df7-ab4b-56b6a0df379c	microprofile-jwt	3a9fd404-d708-4128-83e7-ef00fa943752	Microprofile - JWT built-in scope	openid-connect
9058dc48-9336-4dac-95ec-81095b2bf8b9	acr	3a9fd404-d708-4128-83e7-ef00fa943752	OpenID Connect scope for add acr (authentication context class reference) to the token	openid-connect
3583a32f-b84b-4d85-a79d-0ee871e335dc	basic	3a9fd404-d708-4128-83e7-ef00fa943752	OpenID Connect scope for add all basic claims to the token	openid-connect
880f5c51-7209-4262-9a6a-543895c0075f	organization	3a9fd404-d708-4128-83e7-ef00fa943752	Additional claims about the organization a subject belongs to	openid-connect
\.


--
-- Data for Name: client_scope_attributes; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.client_scope_attributes (scope_id, value, name) FROM stdin;
f26177d9-ab49-449c-bbc7-050c6ea05305	true	display.on.consent.screen
f26177d9-ab49-449c-bbc7-050c6ea05305	${offlineAccessScopeConsentText}	consent.screen.text
c5728f88-7d82-4a3f-95b0-e8394aa20f74	true	display.on.consent.screen
c5728f88-7d82-4a3f-95b0-e8394aa20f74	${samlRoleListScopeConsentText}	consent.screen.text
b7fa698d-506e-4edd-ba59-92d1330019f7	true	display.on.consent.screen
b7fa698d-506e-4edd-ba59-92d1330019f7	${profileScopeConsentText}	consent.screen.text
b7fa698d-506e-4edd-ba59-92d1330019f7	true	include.in.token.scope
57b95975-6d57-4d7a-86bd-3912d154d522	true	display.on.consent.screen
57b95975-6d57-4d7a-86bd-3912d154d522	${emailScopeConsentText}	consent.screen.text
57b95975-6d57-4d7a-86bd-3912d154d522	true	include.in.token.scope
de577a7b-5939-44fa-aa0f-d0b50991435b	true	display.on.consent.screen
de577a7b-5939-44fa-aa0f-d0b50991435b	${addressScopeConsentText}	consent.screen.text
de577a7b-5939-44fa-aa0f-d0b50991435b	true	include.in.token.scope
23331e70-04a9-4ee0-a188-49a8636c01b8	true	display.on.consent.screen
23331e70-04a9-4ee0-a188-49a8636c01b8	${phoneScopeConsentText}	consent.screen.text
23331e70-04a9-4ee0-a188-49a8636c01b8	true	include.in.token.scope
d4ea9c2f-7ae0-4297-8743-e5314bb8e811	true	display.on.consent.screen
d4ea9c2f-7ae0-4297-8743-e5314bb8e811	${rolesScopeConsentText}	consent.screen.text
d4ea9c2f-7ae0-4297-8743-e5314bb8e811	false	include.in.token.scope
bf7a8b48-9bfb-4612-a643-8d7c01c5ae91	false	display.on.consent.screen
bf7a8b48-9bfb-4612-a643-8d7c01c5ae91		consent.screen.text
bf7a8b48-9bfb-4612-a643-8d7c01c5ae91	false	include.in.token.scope
180ede63-bd73-4832-b8aa-f930d57263d5	false	display.on.consent.screen
180ede63-bd73-4832-b8aa-f930d57263d5	true	include.in.token.scope
b5a5d94a-d9d0-4dde-ad70-7b629561ad61	false	display.on.consent.screen
b5a5d94a-d9d0-4dde-ad70-7b629561ad61	false	include.in.token.scope
9d11eb69-e2dd-416b-8c04-d35444356a47	false	display.on.consent.screen
9d11eb69-e2dd-416b-8c04-d35444356a47	true	include.in.token.scope
6f3da124-ce69-4e6c-b933-35dfc3e58221	false	display.on.consent.screen
6f3da124-ce69-4e6c-b933-35dfc3e58221	false	include.in.token.scope
c9b2c9be-e9f9-4eda-847a-b8f20265ed48	true	display.on.consent.screen
c9b2c9be-e9f9-4eda-847a-b8f20265ed48	${offlineAccessScopeConsentText}	consent.screen.text
59352ece-9aad-4086-ac75-eb145c579d2d	true	display.on.consent.screen
59352ece-9aad-4086-ac75-eb145c579d2d	${samlRoleListScopeConsentText}	consent.screen.text
50169b72-4790-45a2-9a55-1f1797bc287b	false	display.on.consent.screen
3809229d-762a-41ad-b8da-9175829b87e4	true	display.on.consent.screen
3809229d-762a-41ad-b8da-9175829b87e4	${profileScopeConsentText}	consent.screen.text
3809229d-762a-41ad-b8da-9175829b87e4	true	include.in.token.scope
748acf76-9899-4a7c-b222-7a4bd53d21a1	true	display.on.consent.screen
748acf76-9899-4a7c-b222-7a4bd53d21a1	${emailScopeConsentText}	consent.screen.text
748acf76-9899-4a7c-b222-7a4bd53d21a1	true	include.in.token.scope
e54f533d-3819-4345-9502-8c3b57ebb6a6	true	display.on.consent.screen
e54f533d-3819-4345-9502-8c3b57ebb6a6	${addressScopeConsentText}	consent.screen.text
e54f533d-3819-4345-9502-8c3b57ebb6a6	true	include.in.token.scope
5b4a9626-9522-4fdf-89fc-6abe5538a593	true	display.on.consent.screen
5b4a9626-9522-4fdf-89fc-6abe5538a593	${phoneScopeConsentText}	consent.screen.text
5b4a9626-9522-4fdf-89fc-6abe5538a593	true	include.in.token.scope
5c60430f-7841-4c66-acb4-0d372753c861	true	display.on.consent.screen
5c60430f-7841-4c66-acb4-0d372753c861	${rolesScopeConsentText}	consent.screen.text
5c60430f-7841-4c66-acb4-0d372753c861	false	include.in.token.scope
d7a399be-d14a-43bf-9a5a-cde8802b9168	false	display.on.consent.screen
d7a399be-d14a-43bf-9a5a-cde8802b9168		consent.screen.text
d7a399be-d14a-43bf-9a5a-cde8802b9168	false	include.in.token.scope
dc3fd5f4-803e-4df7-ab4b-56b6a0df379c	false	display.on.consent.screen
dc3fd5f4-803e-4df7-ab4b-56b6a0df379c	true	include.in.token.scope
9058dc48-9336-4dac-95ec-81095b2bf8b9	false	display.on.consent.screen
9058dc48-9336-4dac-95ec-81095b2bf8b9	false	include.in.token.scope
3583a32f-b84b-4d85-a79d-0ee871e335dc	false	display.on.consent.screen
3583a32f-b84b-4d85-a79d-0ee871e335dc	false	include.in.token.scope
880f5c51-7209-4262-9a6a-543895c0075f	true	display.on.consent.screen
880f5c51-7209-4262-9a6a-543895c0075f	${organizationScopeConsentText}	consent.screen.text
880f5c51-7209-4262-9a6a-543895c0075f	true	include.in.token.scope
\.


--
-- Data for Name: client_scope_client; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.client_scope_client (client_id, scope_id, default_scope) FROM stdin;
5faa1af4-99ee-4664-83c0-ada476a835be	b7fa698d-506e-4edd-ba59-92d1330019f7	t
5faa1af4-99ee-4664-83c0-ada476a835be	57b95975-6d57-4d7a-86bd-3912d154d522	t
5faa1af4-99ee-4664-83c0-ada476a835be	bf7a8b48-9bfb-4612-a643-8d7c01c5ae91	t
5faa1af4-99ee-4664-83c0-ada476a835be	b5a5d94a-d9d0-4dde-ad70-7b629561ad61	t
5faa1af4-99ee-4664-83c0-ada476a835be	d4ea9c2f-7ae0-4297-8743-e5314bb8e811	t
5faa1af4-99ee-4664-83c0-ada476a835be	de577a7b-5939-44fa-aa0f-d0b50991435b	f
5faa1af4-99ee-4664-83c0-ada476a835be	180ede63-bd73-4832-b8aa-f930d57263d5	f
5faa1af4-99ee-4664-83c0-ada476a835be	23331e70-04a9-4ee0-a188-49a8636c01b8	f
5faa1af4-99ee-4664-83c0-ada476a835be	f26177d9-ab49-449c-bbc7-050c6ea05305	f
3cbf8aa7-c144-492c-aaf9-859a636cce37	b7fa698d-506e-4edd-ba59-92d1330019f7	t
3cbf8aa7-c144-492c-aaf9-859a636cce37	57b95975-6d57-4d7a-86bd-3912d154d522	t
3cbf8aa7-c144-492c-aaf9-859a636cce37	bf7a8b48-9bfb-4612-a643-8d7c01c5ae91	t
3cbf8aa7-c144-492c-aaf9-859a636cce37	b5a5d94a-d9d0-4dde-ad70-7b629561ad61	t
3cbf8aa7-c144-492c-aaf9-859a636cce37	d4ea9c2f-7ae0-4297-8743-e5314bb8e811	t
3cbf8aa7-c144-492c-aaf9-859a636cce37	de577a7b-5939-44fa-aa0f-d0b50991435b	f
3cbf8aa7-c144-492c-aaf9-859a636cce37	180ede63-bd73-4832-b8aa-f930d57263d5	f
3cbf8aa7-c144-492c-aaf9-859a636cce37	23331e70-04a9-4ee0-a188-49a8636c01b8	f
3cbf8aa7-c144-492c-aaf9-859a636cce37	f26177d9-ab49-449c-bbc7-050c6ea05305	f
481624e6-0bf9-4e05-9af4-aa150c5763a9	b7fa698d-506e-4edd-ba59-92d1330019f7	t
481624e6-0bf9-4e05-9af4-aa150c5763a9	57b95975-6d57-4d7a-86bd-3912d154d522	t
481624e6-0bf9-4e05-9af4-aa150c5763a9	bf7a8b48-9bfb-4612-a643-8d7c01c5ae91	t
481624e6-0bf9-4e05-9af4-aa150c5763a9	b5a5d94a-d9d0-4dde-ad70-7b629561ad61	t
481624e6-0bf9-4e05-9af4-aa150c5763a9	d4ea9c2f-7ae0-4297-8743-e5314bb8e811	t
481624e6-0bf9-4e05-9af4-aa150c5763a9	de577a7b-5939-44fa-aa0f-d0b50991435b	f
481624e6-0bf9-4e05-9af4-aa150c5763a9	180ede63-bd73-4832-b8aa-f930d57263d5	f
481624e6-0bf9-4e05-9af4-aa150c5763a9	23331e70-04a9-4ee0-a188-49a8636c01b8	f
481624e6-0bf9-4e05-9af4-aa150c5763a9	f26177d9-ab49-449c-bbc7-050c6ea05305	f
0603646a-8907-459a-91bf-facd78dc0140	b7fa698d-506e-4edd-ba59-92d1330019f7	t
0603646a-8907-459a-91bf-facd78dc0140	57b95975-6d57-4d7a-86bd-3912d154d522	t
0603646a-8907-459a-91bf-facd78dc0140	bf7a8b48-9bfb-4612-a643-8d7c01c5ae91	t
0603646a-8907-459a-91bf-facd78dc0140	b5a5d94a-d9d0-4dde-ad70-7b629561ad61	t
0603646a-8907-459a-91bf-facd78dc0140	d4ea9c2f-7ae0-4297-8743-e5314bb8e811	t
0603646a-8907-459a-91bf-facd78dc0140	de577a7b-5939-44fa-aa0f-d0b50991435b	f
0603646a-8907-459a-91bf-facd78dc0140	180ede63-bd73-4832-b8aa-f930d57263d5	f
0603646a-8907-459a-91bf-facd78dc0140	23331e70-04a9-4ee0-a188-49a8636c01b8	f
0603646a-8907-459a-91bf-facd78dc0140	f26177d9-ab49-449c-bbc7-050c6ea05305	f
6b42d9f1-5b82-481b-aab2-4286edf4e8e8	b7fa698d-506e-4edd-ba59-92d1330019f7	t
6b42d9f1-5b82-481b-aab2-4286edf4e8e8	57b95975-6d57-4d7a-86bd-3912d154d522	t
6b42d9f1-5b82-481b-aab2-4286edf4e8e8	bf7a8b48-9bfb-4612-a643-8d7c01c5ae91	t
6b42d9f1-5b82-481b-aab2-4286edf4e8e8	b5a5d94a-d9d0-4dde-ad70-7b629561ad61	t
6b42d9f1-5b82-481b-aab2-4286edf4e8e8	d4ea9c2f-7ae0-4297-8743-e5314bb8e811	t
6b42d9f1-5b82-481b-aab2-4286edf4e8e8	de577a7b-5939-44fa-aa0f-d0b50991435b	f
6b42d9f1-5b82-481b-aab2-4286edf4e8e8	180ede63-bd73-4832-b8aa-f930d57263d5	f
6b42d9f1-5b82-481b-aab2-4286edf4e8e8	23331e70-04a9-4ee0-a188-49a8636c01b8	f
6b42d9f1-5b82-481b-aab2-4286edf4e8e8	f26177d9-ab49-449c-bbc7-050c6ea05305	f
193c8e25-0d00-4c45-ad5c-fa2a6e50d8f6	b7fa698d-506e-4edd-ba59-92d1330019f7	t
193c8e25-0d00-4c45-ad5c-fa2a6e50d8f6	57b95975-6d57-4d7a-86bd-3912d154d522	t
193c8e25-0d00-4c45-ad5c-fa2a6e50d8f6	bf7a8b48-9bfb-4612-a643-8d7c01c5ae91	t
193c8e25-0d00-4c45-ad5c-fa2a6e50d8f6	b5a5d94a-d9d0-4dde-ad70-7b629561ad61	t
193c8e25-0d00-4c45-ad5c-fa2a6e50d8f6	d4ea9c2f-7ae0-4297-8743-e5314bb8e811	t
193c8e25-0d00-4c45-ad5c-fa2a6e50d8f6	de577a7b-5939-44fa-aa0f-d0b50991435b	f
193c8e25-0d00-4c45-ad5c-fa2a6e50d8f6	180ede63-bd73-4832-b8aa-f930d57263d5	f
193c8e25-0d00-4c45-ad5c-fa2a6e50d8f6	23331e70-04a9-4ee0-a188-49a8636c01b8	f
193c8e25-0d00-4c45-ad5c-fa2a6e50d8f6	f26177d9-ab49-449c-bbc7-050c6ea05305	f
481624e6-0bf9-4e05-9af4-aa150c5763a9	9d11eb69-e2dd-416b-8c04-d35444356a47	t
5faa1af4-99ee-4664-83c0-ada476a835be	6f3da124-ce69-4e6c-b933-35dfc3e58221	t
3cbf8aa7-c144-492c-aaf9-859a636cce37	6f3da124-ce69-4e6c-b933-35dfc3e58221	t
481624e6-0bf9-4e05-9af4-aa150c5763a9	6f3da124-ce69-4e6c-b933-35dfc3e58221	t
193c8e25-0d00-4c45-ad5c-fa2a6e50d8f6	6f3da124-ce69-4e6c-b933-35dfc3e58221	t
9d44bf2d-8bad-41b0-a76d-9ef30b0ce5bf	5c60430f-7841-4c66-acb4-0d372753c861	t
9d44bf2d-8bad-41b0-a76d-9ef30b0ce5bf	3583a32f-b84b-4d85-a79d-0ee871e335dc	t
9d44bf2d-8bad-41b0-a76d-9ef30b0ce5bf	d7a399be-d14a-43bf-9a5a-cde8802b9168	t
9d44bf2d-8bad-41b0-a76d-9ef30b0ce5bf	3809229d-762a-41ad-b8da-9175829b87e4	t
9d44bf2d-8bad-41b0-a76d-9ef30b0ce5bf	9058dc48-9336-4dac-95ec-81095b2bf8b9	t
9d44bf2d-8bad-41b0-a76d-9ef30b0ce5bf	748acf76-9899-4a7c-b222-7a4bd53d21a1	t
9d44bf2d-8bad-41b0-a76d-9ef30b0ce5bf	dc3fd5f4-803e-4df7-ab4b-56b6a0df379c	f
9d44bf2d-8bad-41b0-a76d-9ef30b0ce5bf	5b4a9626-9522-4fdf-89fc-6abe5538a593	f
9d44bf2d-8bad-41b0-a76d-9ef30b0ce5bf	c9b2c9be-e9f9-4eda-847a-b8f20265ed48	f
9d44bf2d-8bad-41b0-a76d-9ef30b0ce5bf	e54f533d-3819-4345-9502-8c3b57ebb6a6	f
9d44bf2d-8bad-41b0-a76d-9ef30b0ce5bf	880f5c51-7209-4262-9a6a-543895c0075f	f
7b5da870-582a-4bd2-9320-84172f977b88	5c60430f-7841-4c66-acb4-0d372753c861	t
7b5da870-582a-4bd2-9320-84172f977b88	3583a32f-b84b-4d85-a79d-0ee871e335dc	t
7b5da870-582a-4bd2-9320-84172f977b88	d7a399be-d14a-43bf-9a5a-cde8802b9168	t
7b5da870-582a-4bd2-9320-84172f977b88	3809229d-762a-41ad-b8da-9175829b87e4	t
7b5da870-582a-4bd2-9320-84172f977b88	9058dc48-9336-4dac-95ec-81095b2bf8b9	t
7b5da870-582a-4bd2-9320-84172f977b88	748acf76-9899-4a7c-b222-7a4bd53d21a1	t
7b5da870-582a-4bd2-9320-84172f977b88	dc3fd5f4-803e-4df7-ab4b-56b6a0df379c	f
7b5da870-582a-4bd2-9320-84172f977b88	5b4a9626-9522-4fdf-89fc-6abe5538a593	f
7b5da870-582a-4bd2-9320-84172f977b88	c9b2c9be-e9f9-4eda-847a-b8f20265ed48	f
7b5da870-582a-4bd2-9320-84172f977b88	e54f533d-3819-4345-9502-8c3b57ebb6a6	f
7b5da870-582a-4bd2-9320-84172f977b88	880f5c51-7209-4262-9a6a-543895c0075f	f
86099425-6565-442a-ad45-312468b4f1f0	5c60430f-7841-4c66-acb4-0d372753c861	t
86099425-6565-442a-ad45-312468b4f1f0	3583a32f-b84b-4d85-a79d-0ee871e335dc	t
86099425-6565-442a-ad45-312468b4f1f0	d7a399be-d14a-43bf-9a5a-cde8802b9168	t
86099425-6565-442a-ad45-312468b4f1f0	3809229d-762a-41ad-b8da-9175829b87e4	t
86099425-6565-442a-ad45-312468b4f1f0	9058dc48-9336-4dac-95ec-81095b2bf8b9	t
86099425-6565-442a-ad45-312468b4f1f0	748acf76-9899-4a7c-b222-7a4bd53d21a1	t
86099425-6565-442a-ad45-312468b4f1f0	dc3fd5f4-803e-4df7-ab4b-56b6a0df379c	f
86099425-6565-442a-ad45-312468b4f1f0	5b4a9626-9522-4fdf-89fc-6abe5538a593	f
86099425-6565-442a-ad45-312468b4f1f0	c9b2c9be-e9f9-4eda-847a-b8f20265ed48	f
86099425-6565-442a-ad45-312468b4f1f0	e54f533d-3819-4345-9502-8c3b57ebb6a6	f
86099425-6565-442a-ad45-312468b4f1f0	880f5c51-7209-4262-9a6a-543895c0075f	f
fee15b0e-3064-42a7-867a-d9ba6488cf9f	5c60430f-7841-4c66-acb4-0d372753c861	t
fee15b0e-3064-42a7-867a-d9ba6488cf9f	3583a32f-b84b-4d85-a79d-0ee871e335dc	t
fee15b0e-3064-42a7-867a-d9ba6488cf9f	d7a399be-d14a-43bf-9a5a-cde8802b9168	t
fee15b0e-3064-42a7-867a-d9ba6488cf9f	3809229d-762a-41ad-b8da-9175829b87e4	t
fee15b0e-3064-42a7-867a-d9ba6488cf9f	9058dc48-9336-4dac-95ec-81095b2bf8b9	t
fee15b0e-3064-42a7-867a-d9ba6488cf9f	748acf76-9899-4a7c-b222-7a4bd53d21a1	t
fee15b0e-3064-42a7-867a-d9ba6488cf9f	dc3fd5f4-803e-4df7-ab4b-56b6a0df379c	f
fee15b0e-3064-42a7-867a-d9ba6488cf9f	5b4a9626-9522-4fdf-89fc-6abe5538a593	f
fee15b0e-3064-42a7-867a-d9ba6488cf9f	c9b2c9be-e9f9-4eda-847a-b8f20265ed48	f
fee15b0e-3064-42a7-867a-d9ba6488cf9f	e54f533d-3819-4345-9502-8c3b57ebb6a6	f
fee15b0e-3064-42a7-867a-d9ba6488cf9f	880f5c51-7209-4262-9a6a-543895c0075f	f
2c594784-be48-4508-a8e0-1135e4cb37ae	5c60430f-7841-4c66-acb4-0d372753c861	t
2c594784-be48-4508-a8e0-1135e4cb37ae	3583a32f-b84b-4d85-a79d-0ee871e335dc	t
2c594784-be48-4508-a8e0-1135e4cb37ae	d7a399be-d14a-43bf-9a5a-cde8802b9168	t
2c594784-be48-4508-a8e0-1135e4cb37ae	3809229d-762a-41ad-b8da-9175829b87e4	t
2c594784-be48-4508-a8e0-1135e4cb37ae	9058dc48-9336-4dac-95ec-81095b2bf8b9	t
2c594784-be48-4508-a8e0-1135e4cb37ae	748acf76-9899-4a7c-b222-7a4bd53d21a1	t
2c594784-be48-4508-a8e0-1135e4cb37ae	dc3fd5f4-803e-4df7-ab4b-56b6a0df379c	f
2c594784-be48-4508-a8e0-1135e4cb37ae	5b4a9626-9522-4fdf-89fc-6abe5538a593	f
2c594784-be48-4508-a8e0-1135e4cb37ae	c9b2c9be-e9f9-4eda-847a-b8f20265ed48	f
2c594784-be48-4508-a8e0-1135e4cb37ae	e54f533d-3819-4345-9502-8c3b57ebb6a6	f
2c594784-be48-4508-a8e0-1135e4cb37ae	880f5c51-7209-4262-9a6a-543895c0075f	f
ca3d0ca8-abd5-4605-80df-b32fa0e45a83	5c60430f-7841-4c66-acb4-0d372753c861	t
ca3d0ca8-abd5-4605-80df-b32fa0e45a83	3583a32f-b84b-4d85-a79d-0ee871e335dc	t
ca3d0ca8-abd5-4605-80df-b32fa0e45a83	d7a399be-d14a-43bf-9a5a-cde8802b9168	t
ca3d0ca8-abd5-4605-80df-b32fa0e45a83	3809229d-762a-41ad-b8da-9175829b87e4	t
ca3d0ca8-abd5-4605-80df-b32fa0e45a83	9058dc48-9336-4dac-95ec-81095b2bf8b9	t
ca3d0ca8-abd5-4605-80df-b32fa0e45a83	748acf76-9899-4a7c-b222-7a4bd53d21a1	t
ca3d0ca8-abd5-4605-80df-b32fa0e45a83	dc3fd5f4-803e-4df7-ab4b-56b6a0df379c	f
ca3d0ca8-abd5-4605-80df-b32fa0e45a83	5b4a9626-9522-4fdf-89fc-6abe5538a593	f
ca3d0ca8-abd5-4605-80df-b32fa0e45a83	c9b2c9be-e9f9-4eda-847a-b8f20265ed48	f
ca3d0ca8-abd5-4605-80df-b32fa0e45a83	e54f533d-3819-4345-9502-8c3b57ebb6a6	f
ca3d0ca8-abd5-4605-80df-b32fa0e45a83	880f5c51-7209-4262-9a6a-543895c0075f	f
27b6abbc-155f-4768-9200-8721b6d0831f	5c60430f-7841-4c66-acb4-0d372753c861	t
27b6abbc-155f-4768-9200-8721b6d0831f	3583a32f-b84b-4d85-a79d-0ee871e335dc	t
27b6abbc-155f-4768-9200-8721b6d0831f	d7a399be-d14a-43bf-9a5a-cde8802b9168	t
27b6abbc-155f-4768-9200-8721b6d0831f	3809229d-762a-41ad-b8da-9175829b87e4	t
27b6abbc-155f-4768-9200-8721b6d0831f	9058dc48-9336-4dac-95ec-81095b2bf8b9	t
27b6abbc-155f-4768-9200-8721b6d0831f	748acf76-9899-4a7c-b222-7a4bd53d21a1	t
27b6abbc-155f-4768-9200-8721b6d0831f	dc3fd5f4-803e-4df7-ab4b-56b6a0df379c	f
27b6abbc-155f-4768-9200-8721b6d0831f	5b4a9626-9522-4fdf-89fc-6abe5538a593	f
27b6abbc-155f-4768-9200-8721b6d0831f	c9b2c9be-e9f9-4eda-847a-b8f20265ed48	f
27b6abbc-155f-4768-9200-8721b6d0831f	e54f533d-3819-4345-9502-8c3b57ebb6a6	f
27b6abbc-155f-4768-9200-8721b6d0831f	880f5c51-7209-4262-9a6a-543895c0075f	f
\.


--
-- Data for Name: client_scope_role_mapping; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.client_scope_role_mapping (scope_id, role_id) FROM stdin;
f26177d9-ab49-449c-bbc7-050c6ea05305	b697644d-9a79-47cc-be19-b41bc079258a
c9b2c9be-e9f9-4eda-847a-b8f20265ed48	8fb6b6ef-3b9e-4cdc-81ec-b1ed952dc628
\.


--
-- Data for Name: component; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.component (id, name, parent_id, provider_id, provider_type, realm_id, sub_type) FROM stdin;
1feb8b95-ffab-496a-b55e-4d35ab563625	Trusted Hosts	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	trusted-hosts	org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	anonymous
7b7aca39-ebbb-47e8-bea0-83a00c5c68a6	Consent Required	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	consent-required	org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	anonymous
90a3e6f6-745c-4779-bc31-c27386533a1d	Full Scope Disabled	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	scope	org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	anonymous
92bd2d6d-4ca8-42c8-b551-d546134e91ae	Max Clients Limit	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	max-clients	org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	anonymous
ae2135c3-538e-433c-9dc5-a8949b5fc5cd	Allowed Protocol Mapper Types	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	allowed-protocol-mappers	org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	anonymous
ce450286-0887-457a-ab0f-bfa97cb2f98f	Allowed Client Scopes	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	allowed-client-templates	org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	anonymous
85671aaf-41aa-48e7-9675-5b668846ae25	Allowed Protocol Mapper Types	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	allowed-protocol-mappers	org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	authenticated
988de064-cd40-4a8c-b476-c19e402d6fed	Allowed Client Scopes	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	allowed-client-templates	org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	authenticated
077aa053-9480-4dd8-bc25-38a2ffc8b6b0	rsa-generated	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	rsa-generated	org.keycloak.keys.KeyProvider	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	\N
f755fa5b-528c-49bc-b748-b246a089d548	rsa-enc-generated	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	rsa-enc-generated	org.keycloak.keys.KeyProvider	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	\N
fe0ce970-a2b6-4e97-b5a3-a028a0d72c94	hmac-generated	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	hmac-generated	org.keycloak.keys.KeyProvider	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	\N
3c2b033d-0620-497e-8712-9b506f177498	aes-generated	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	aes-generated	org.keycloak.keys.KeyProvider	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	\N
c6b9437d-f085-4c22-baf9-d895f6be3ffd	\N	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	declarative-user-profile	org.keycloak.userprofile.UserProfileProvider	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	\N
32fa92d5-9b83-4cad-9663-fbe0ed536837	hmac-generated-hs512	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	hmac-generated	org.keycloak.keys.KeyProvider	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	\N
ece060d3-b278-4600-b3a8-e57ffd03c7c3	rsa-generated	3a9fd404-d708-4128-83e7-ef00fa943752	rsa-generated	org.keycloak.keys.KeyProvider	3a9fd404-d708-4128-83e7-ef00fa943752	\N
f1b3aea4-83c0-4927-af80-de991733efb1	rsa-enc-generated	3a9fd404-d708-4128-83e7-ef00fa943752	rsa-enc-generated	org.keycloak.keys.KeyProvider	3a9fd404-d708-4128-83e7-ef00fa943752	\N
a408832a-469c-4e7a-966d-617aa20c3156	hmac-generated-hs512	3a9fd404-d708-4128-83e7-ef00fa943752	hmac-generated	org.keycloak.keys.KeyProvider	3a9fd404-d708-4128-83e7-ef00fa943752	\N
2614fca7-d17e-41ce-83b9-8d4dbdfc86ac	aes-generated	3a9fd404-d708-4128-83e7-ef00fa943752	aes-generated	org.keycloak.keys.KeyProvider	3a9fd404-d708-4128-83e7-ef00fa943752	\N
e18c8d00-d31f-47ee-93f1-ecd14e7fe86c	Trusted Hosts	3a9fd404-d708-4128-83e7-ef00fa943752	trusted-hosts	org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy	3a9fd404-d708-4128-83e7-ef00fa943752	anonymous
c0d843dc-fc1c-4d6e-a308-39621e4c02c6	Consent Required	3a9fd404-d708-4128-83e7-ef00fa943752	consent-required	org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy	3a9fd404-d708-4128-83e7-ef00fa943752	anonymous
d3852def-f2a3-4d5c-b79f-88a753b5839b	Full Scope Disabled	3a9fd404-d708-4128-83e7-ef00fa943752	scope	org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy	3a9fd404-d708-4128-83e7-ef00fa943752	anonymous
cb3fc936-b565-44cd-842c-944c385b7992	Max Clients Limit	3a9fd404-d708-4128-83e7-ef00fa943752	max-clients	org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy	3a9fd404-d708-4128-83e7-ef00fa943752	anonymous
c1b9084c-7d85-4963-bfdd-590d7f0ee606	Allowed Protocol Mapper Types	3a9fd404-d708-4128-83e7-ef00fa943752	allowed-protocol-mappers	org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy	3a9fd404-d708-4128-83e7-ef00fa943752	anonymous
c30d38cf-afe9-4204-a805-0299b748d6e0	Allowed Client Scopes	3a9fd404-d708-4128-83e7-ef00fa943752	allowed-client-templates	org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy	3a9fd404-d708-4128-83e7-ef00fa943752	anonymous
750c7b29-a1ee-453b-9e26-4d357f4a1dea	Allowed Protocol Mapper Types	3a9fd404-d708-4128-83e7-ef00fa943752	allowed-protocol-mappers	org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy	3a9fd404-d708-4128-83e7-ef00fa943752	authenticated
96e87b99-2fcd-4278-bbcd-907c39745892	Allowed Client Scopes	3a9fd404-d708-4128-83e7-ef00fa943752	allowed-client-templates	org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy	3a9fd404-d708-4128-83e7-ef00fa943752	authenticated
87df47b7-488e-4d11-89eb-1a142669c9e7	\N	3a9fd404-d708-4128-83e7-ef00fa943752	declarative-user-profile	org.keycloak.userprofile.UserProfileProvider	3a9fd404-d708-4128-83e7-ef00fa943752	\N
\.


--
-- Data for Name: component_config; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.component_config (id, component_id, name, value) FROM stdin;
fac89154-0660-4af7-8b44-3a2952b44ba8	ae2135c3-538e-433c-9dc5-a8949b5fc5cd	allowed-protocol-mapper-types	oidc-address-mapper
ad05f099-0e5d-4b5a-bad2-3185d65fc745	ae2135c3-538e-433c-9dc5-a8949b5fc5cd	allowed-protocol-mapper-types	oidc-full-name-mapper
2c69f0e7-31ae-47f3-b027-021c8c94bfba	ae2135c3-538e-433c-9dc5-a8949b5fc5cd	allowed-protocol-mapper-types	oidc-usermodel-property-mapper
068ab853-16f9-46ab-8595-8d34c0b701a2	ae2135c3-538e-433c-9dc5-a8949b5fc5cd	allowed-protocol-mapper-types	saml-role-list-mapper
37e8761a-0bf1-4e44-84bd-b0f53dbc6410	ae2135c3-538e-433c-9dc5-a8949b5fc5cd	allowed-protocol-mapper-types	oidc-sha256-pairwise-sub-mapper
adec83d6-4fcb-4925-94bc-95c7a53204ad	ae2135c3-538e-433c-9dc5-a8949b5fc5cd	allowed-protocol-mapper-types	saml-user-property-mapper
960df987-1153-480a-bbde-424e48f7b039	ae2135c3-538e-433c-9dc5-a8949b5fc5cd	allowed-protocol-mapper-types	saml-user-attribute-mapper
aa0c7230-66cf-4735-a048-2d81b5d18ee4	ae2135c3-538e-433c-9dc5-a8949b5fc5cd	allowed-protocol-mapper-types	oidc-usermodel-attribute-mapper
56d7183a-90ed-480d-85cc-0ec6e7e14c53	85671aaf-41aa-48e7-9675-5b668846ae25	allowed-protocol-mapper-types	oidc-usermodel-attribute-mapper
0c811200-a9ca-46dc-8029-ac8550cb120b	85671aaf-41aa-48e7-9675-5b668846ae25	allowed-protocol-mapper-types	saml-user-property-mapper
0cbdb5bd-610e-49e5-88c9-ba2638d93c74	85671aaf-41aa-48e7-9675-5b668846ae25	allowed-protocol-mapper-types	oidc-usermodel-property-mapper
d6209165-b364-4407-bff3-b3d6774ad22d	85671aaf-41aa-48e7-9675-5b668846ae25	allowed-protocol-mapper-types	oidc-address-mapper
dbd06978-2eb4-48b2-abf6-ce2bb0af3e93	85671aaf-41aa-48e7-9675-5b668846ae25	allowed-protocol-mapper-types	saml-role-list-mapper
c1f07470-ca63-46f5-a5aa-a7c24e6e563d	85671aaf-41aa-48e7-9675-5b668846ae25	allowed-protocol-mapper-types	saml-user-attribute-mapper
d4d25e94-ed20-4024-a828-e3073c62bc3f	85671aaf-41aa-48e7-9675-5b668846ae25	allowed-protocol-mapper-types	oidc-sha256-pairwise-sub-mapper
e51f1a2b-cf4d-4819-a41f-1329edd401ca	85671aaf-41aa-48e7-9675-5b668846ae25	allowed-protocol-mapper-types	oidc-full-name-mapper
ed502644-4a7b-4ae1-a033-8e2e8905ddac	1feb8b95-ffab-496a-b55e-4d35ab563625	client-uris-must-match	true
88fa8094-3955-404e-9d43-936aef541f69	1feb8b95-ffab-496a-b55e-4d35ab563625	host-sending-registration-request-must-match	true
99f893bf-35f1-43f1-9ea2-a80235acc393	988de064-cd40-4a8c-b476-c19e402d6fed	allow-default-scopes	true
d0a9af0d-b41e-4172-984d-0e329843b06a	92bd2d6d-4ca8-42c8-b551-d546134e91ae	max-clients	200
be25685c-de35-4f84-85a8-e0c32e0a7ed2	ce450286-0887-457a-ab0f-bfa97cb2f98f	allow-default-scopes	true
6999c487-8fac-4d8c-8163-d5e3ab84a668	f755fa5b-528c-49bc-b748-b246a089d548	privateKey	MIIEogIBAAKCAQEAzSAffmIFWJbaBfdJ0Dcf9w18UaLj7Pa6vvdmrVF86KWFSIpQ1oWFe5erjThhtIjc8jv9h8sKtiqVMN6UrXvESEXlj44knRLKb3IwkcaIgKLAZ7yhMfReUr/Gqr/rJ7BkvPf3oRqKMN9hgOJk+ZvOxR/y69o6w1dyPKXPrtJ7WVR5uAW4Dz+cbzSA2J/SyklkPXao3wXsMGG2W0a9ky0vqSyquLKWPi5my/xWXgjLEQE4s8KGMpMW8e7SdOUS5S5baQZAn5+qw5DkxLZ+yjvcU6dTpCWF2k6O19Nzk320kl1Pn+dcZXys0YBHGGO2otH5+UBpzccQnHcps0gEMO+NpQIDAQABAoIBAB9yptFp9rbbvcY/YNYmtWRbcW1Uo0v/kabYaCn3PcavJa124yjYZJwLpzXp6HTUrMcIcnigpN5DzgalgTbNrEyGRskI+k0IhferEo4u2VubWBMvZKkzdUiLdUpVTRiTHwann5PX7UrL3Hd1fhSUkhE/si79kJfFLtLkeuArDdUma3EF2aDKAT0pumaGUchL6LDtWkaRqg6XODMUHbsh+Ro71fybZhW3PSzcWDRniOkFdjO32XLdpNErb4/96dY+fs9bhXGzUhguxAjzwF19XXG9r27FahdnyCODovCqhU048F3STVBUwCBjC26fDuzfs607YTzPCK+g0G9isx/51qECgYEA1xwl1IRPOAYbjZeOBYJp17IB6nCI8+rBXoPg75hy3M3HRlfyASt7nNXmHxZMVpf92d0E55GNFx+12nf7NHANwGzgG4sE5dVBuKzIGjSCIEbQ83ICEzLw0iIhDTD57FD6LAVEcbI/zPoT4axdaf1h7kU38iiHWBC2gWryMCZIsnUCgYEA9B4aWLRJu8lR1JVzRtAApYFtzvTqsRpK1As2v/JnW64iXTG/GYKEcgfiIp+H1TcGoEbpG6YaqHilDq3Gxqs8emAohPpKcHSMfp1EpNYxt3yY/hPSh7d8HqS1R70X8XyocIs+KCBmnCp0fYiZej4avaZtBVrRYmJ2wQR+es9MqHECgYAH5oHBL2bVcOv8JE/UBfkrMd9+I8nz43Bcr01In3nAgFJMVBiQUG+G0BhWlfQRzh1Fridq9d6Ptdtw/a0dPOxUX+tPN6jjZBS42qeyxrDszI0KE+rdl6ZeoWBDYsQbGNJHYZ4k6t4LAjhOtozrwqGhJaurWi+2XU7GrA3fFNjgrQKBgHgdw8r30vwCgDBA5TrnqBnHyLc3ja9dB4q0hdkGtlLHKQOAMc6kiPTu6hctNhH0hF8i5RqErcNNsbG6Uf95TEXOwMFFwpD42BSI37jX6Binx0PljwbWgs01MDm/UmOxedP+v7PQ1Yxr22vVZedfXZsJo8UpvbNSTaqFwpI9JDQRAoGAIyWvK/ksXJz7HV5nxOxXf+zjRrozQ6X8vTgBQsK1zUdCw+kkhgRm74ZEGjSasEjmbykOLSi+KTfn0qIEUFeWixc5ZEo6xsI29QOMYdymq2SDz8V+d5lUr84sDQKxDstra+x452BrfdGePSaRsKOx7Se9XHzMZftarWM4U667uRk=
7a010a88-8ed4-48e5-847f-50bc9d5e725b	f755fa5b-528c-49bc-b748-b246a089d548	keyUse	ENC
661ba2d9-5ada-4eca-97df-fa146eb27f40	077aa053-9480-4dd8-bc25-38a2ffc8b6b0	priority	100
7b654bab-f6bf-463f-8a7b-f25e75216c59	fe0ce970-a2b6-4e97-b5a3-a028a0d72c94	priority	100
53e0ffc7-0274-4aee-991e-743f6c0da3e9	fe0ce970-a2b6-4e97-b5a3-a028a0d72c94	kid	8dc4045a-5591-4687-bb44-79c2faf24d08
4d3716ea-2129-4c68-8146-fa810c64a52f	f755fa5b-528c-49bc-b748-b246a089d548	certificate	MIICmzCCAYMCBgGA+pVZaTANBgkqhkiG9w0BAQsFADARMQ8wDQYDVQQDDAZtYXN0ZXIwHhcNMjIwNTI1MDkzNzEzWhcNMzIwNTI1MDkzODUzWjARMQ8wDQYDVQQDDAZtYXN0ZXIwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQDNIB9+YgVYltoF90nQNx/3DXxRouPs9rq+92atUXzopYVIilDWhYV7l6uNOGG0iNzyO/2Hywq2KpUw3pSte8RIReWPjiSdEspvcjCRxoiAosBnvKEx9F5Sv8aqv+snsGS89/ehGoow32GA4mT5m87FH/Lr2jrDV3I8pc+u0ntZVHm4BbgPP5xvNIDYn9LKSWQ9dqjfBewwYbZbRr2TLS+pLKq4spY+LmbL/FZeCMsRATizwoYykxbx7tJ05RLlLltpBkCfn6rDkOTEtn7KO9xTp1OkJYXaTo7X03OTfbSSXU+f51xlfKzRgEcYY7ai0fn5QGnNxxCcdymzSAQw742lAgMBAAEwDQYJKoZIhvcNAQELBQADggEBALpN03pKmuNb66q9RVA89+Z9r2eNqVXPlWyCZfiRET1sdxAETLXkVv3xGcaS1WxbQAzeqrO59kiuEotPS8+y5tFbknRrvqFuqPI9ZQ+d9SUF9J1EuZRB0xVfMBS82aIBOcTXOsFJ0kqrRRM9kNAsdkXgnKd1Hk1yNXzOmM0TKOSF184cCe2OKtZtL/RWKHJbVJnUvYFMx5esR+iHu4HHCE16HT8ko/xPVGs+mUQ1OVmW51aLg+/+JRLKsyp/ipHJqPvz0qgdh65p5vzIaMFpoBQmPS4btgNJG0Uvr4AzlW6YRh1MfeqTkcZCmV0BMtlgj+lAgfM+lm5kM2NgL8GCgz8=
c8577c40-63fb-415d-9068-75aa1332eb52	f755fa5b-528c-49bc-b748-b246a089d548	priority	100
c24385d2-75e5-4a38-b6f3-cb5798258945	f755fa5b-528c-49bc-b748-b246a089d548	algorithm	RSA-OAEP
e962e86c-e4fa-452f-add3-3e572d1c583d	077aa053-9480-4dd8-bc25-38a2ffc8b6b0	keyUse	SIG
38c12822-3865-42c6-9274-27c995c0ba30	c1b9084c-7d85-4963-bfdd-590d7f0ee606	allowed-protocol-mapper-types	oidc-address-mapper
3e8e2a4c-b232-4337-a4a6-495fa645e76c	c1b9084c-7d85-4963-bfdd-590d7f0ee606	allowed-protocol-mapper-types	oidc-usermodel-property-mapper
81e6abf0-e67e-449c-a6ee-144b32076c2e	c1b9084c-7d85-4963-bfdd-590d7f0ee606	allowed-protocol-mapper-types	oidc-usermodel-attribute-mapper
ef9f6d11-4c6a-4eb9-aacc-d3c6890e4073	c1b9084c-7d85-4963-bfdd-590d7f0ee606	allowed-protocol-mapper-types	saml-role-list-mapper
54642a41-c4d9-421a-b6f9-c88e26055d4a	c1b9084c-7d85-4963-bfdd-590d7f0ee606	allowed-protocol-mapper-types	oidc-sha256-pairwise-sub-mapper
5730bd46-cd2f-47d2-b82c-49e6ad0fb675	c1b9084c-7d85-4963-bfdd-590d7f0ee606	allowed-protocol-mapper-types	saml-user-attribute-mapper
79d76d7e-96e4-445b-a41c-6759212d2f02	c1b9084c-7d85-4963-bfdd-590d7f0ee606	allowed-protocol-mapper-types	saml-user-property-mapper
3232bbc3-bf58-44d3-a9d0-f35cebb6a660	077aa053-9480-4dd8-bc25-38a2ffc8b6b0	privateKey	MIIEogIBAAKCAQEA75qtE4byBptnUkc1bxzaqcJKjYbgqJiNHjyMXVa2ViQeCScOmWc8Lfl29EC1RdyywvA3bxQSaF85irC7TUIuMY4OUR2p8cn1BOa0EMq9ggh6PSbNEgzqsDlnEmyKSNYGBhg/VLUMVfUJvdH673NOzAZBveR4BHGhlpT78iU/1ncuWq//JxQr9th9z0ihq+Y/Bl+ywSCNEd6GttjM9j6YVI9PVEMznjH260dRjUVok9NlVMfYj5MilNo3pr6doGPMlpzum+TkKEXqhqrPSEb6KyiGvbvNCMTylKJWRwQxVh+4GOr2Miutv8iM4FLHFcn57k6IAv4IRNxu3KpFg/Rm4QIDAQABAoH/A457aZuXL06403/UsZ5VoJXbUj57FzjGEqzTaI9HWMzKC78iRSSpjNHz8hKdawWobm+IyYO19XBBNzQ1LSObJYeP4grB4W6EZ8FrUTrkYgm+LSozFES2ak2jBtYvN0v1/iEqz316F8yLWHkr2fuo/JGBm4MvEa6eHdFLhbMH2ScXlSFk2F3LhbH0WVyhcP5SzyywngG1wJqDRYeT/ryG/e+FRxwJTXZdegT7aCtv3OHtvOtVff8FCXickXomUkOc/Ma8owANQlGTFFhPjDWqCvRLe47k44jGL35p1TUmjUHS0cAij1K8D9QJPBAM5GLzsTGe0IKeRZK1eDmrD/HRAoGBAPW4087RCCuVSA5j7qZg7Bl7ijNjXvNaQrgpDw9DOGAWRM1vCbNP2Ng/GtMN85COTpeoM/+SS0ncUtSxiM9forJhlqnA0kl5H0/L/FikBEw0NE4xaKZDD8mk/kwUPR1EfgXHSREL6BN0rq4o3ze59BAMgaOaoCiF1qM+m27iezWFAoGBAPmgVwmOnYElmL1IkT5xxp6fLSGanhW9bv9igwYkki+YXd9O++tHst9p17KdhMhbyL7RxOWxJV05JcwXpx8yWbhrn5TR1r8c7nJvQ1VzuyOyVrzMHtLDy91/RcvFkkyyy8gdfIze+d6r6tp65xkjdKE2TAxEnwlhSnfl3SGTIwytAoGBAJbll/zr1S6ekA92Qvodjx/fA/Gm1opvJYXegQcIarhnNH1L8RMSt8Gkjx6I9MgS/CSaAMSz3FRRXvSKyc8/lbQhxdodNYY1QYtJY4AzyQfbDzvtj6x6hll4X0ntK9KrWxDFXxN+0lgQUa1WIC4incS1SW9k53G7LyI0slMX9j3ZAoGAIvdOucv8TAbUKk9r6O9O/xEU5UwfMLRkQ+msqMOepoQVQh5yMzbL23eb+y0NWHNi3zfK+NGjhjdnD/uQMVKANtD8fYAXMJYA4I6tQ97+K7XjaHa/Ko66k8AsKqr2H0A8Qz++Ho1tOr+iGhB1JpRM1fghFubV0B6Mq9bK7tUJZ9ECgYEA1kwojXQzGTwea2xjWX3eP70fr80iaI9HUQaWk77VoBWE7ztQ+1iv5aFDlej7r67Rc/skkpBg8NTSlVyxhnDOWZFlv5LOvg2pTDaZAmc5liBZfmETVVnsTl1kqHAbZR+hE3GQJDLnj6eHR1J/g5/4o3lSIUKkwWY/80hjosoPeDY=
52bec883-08be-4f9b-b139-783210d0182b	077aa053-9480-4dd8-bc25-38a2ffc8b6b0	certificate	MIICmzCCAYMCBgGA+pVZDDANBgkqhkiG9w0BAQsFADARMQ8wDQYDVQQDDAZtYXN0ZXIwHhcNMjIwNTI1MDkzNzEzWhcNMzIwNTI1MDkzODUzWjARMQ8wDQYDVQQDDAZtYXN0ZXIwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQDvmq0ThvIGm2dSRzVvHNqpwkqNhuComI0ePIxdVrZWJB4JJw6ZZzwt+Xb0QLVF3LLC8DdvFBJoXzmKsLtNQi4xjg5RHanxyfUE5rQQyr2CCHo9Js0SDOqwOWcSbIpI1gYGGD9UtQxV9Qm90frvc07MBkG95HgEcaGWlPvyJT/Wdy5ar/8nFCv22H3PSKGr5j8GX7LBII0R3oa22Mz2PphUj09UQzOeMfbrR1GNRWiT02VUx9iPkyKU2jemvp2gY8yWnO6b5OQoReqGqs9IRvorKIa9u80IxPKUolZHBDFWH7gY6vYyK62/yIzgUscVyfnuTogC/ghE3G7cqkWD9GbhAgMBAAEwDQYJKoZIhvcNAQELBQADggEBAOdHRzPf22QlPWvOo9bObvPB3BeortQiKamshETM60Eay3ny9hEI6qWv6Ef2J0xN/A3ggHR1xbXuSVTLSwtz68QQh8ol7sKRV/Oy7fu6fiZMwG/9SKE3a+iEQDCVvKJYnvJyfDDB8YuIonvryVbo+GnSdBdMXttPKstj20b3G8AJK8mzV8OjWMkyOWhnSe9in3ZdjJ73TBGCHgjpGia5riv6SV4a2r60/kmacxbAZ0CplOFTCVf1/4BqlNiGumZGEVInOodWHhY9wsttIX9iIvZ/iNOCJoEWG7yqHwGcPfcfZSorb3r7wEJd4Gj2HRgforZwMoOccaJNyjOVl+zknuU=
24fc401f-1037-4a0b-93ca-864a10f967df	fe0ce970-a2b6-4e97-b5a3-a028a0d72c94	secret	gIluH1mFdrBbMm2U1qfmcEepLb4Jt5tO22aihJVTD0fKYORjBUrW1DIJrbKN9D_dLXpYu8SVpfksz6jC20tV3A
3ffb8e74-542b-4f6c-93ad-797e9dc7020d	fe0ce970-a2b6-4e97-b5a3-a028a0d72c94	algorithm	HS256
1f070a72-522b-46d7-b04b-445e35c52e3b	3c2b033d-0620-497e-8712-9b506f177498	kid	0a2d3b70-41dd-43a7-8960-553c4af58cb4
ae5c9fba-fdec-422a-a6e0-b3875dfeeaa2	3c2b033d-0620-497e-8712-9b506f177498	secret	YJ2KYYxkLTBD_Wp4alAuVw
43516dee-e2fd-4d58-9682-a4d2664a47fe	3c2b033d-0620-497e-8712-9b506f177498	priority	100
969e8291-9de7-4684-a271-ea93343cab3e	32fa92d5-9b83-4cad-9663-fbe0ed536837	algorithm	HS512
225bbd06-990d-47fd-bb90-9e279cd7c831	32fa92d5-9b83-4cad-9663-fbe0ed536837	kid	00dcb89a-9190-4061-b347-c4c2f1f74e28
8d6850b7-13a7-481f-b564-3880b6bd2dfe	32fa92d5-9b83-4cad-9663-fbe0ed536837	secret	u_12Rgj-uBgmJC1Wyt_SavkoDDrhfbCgbLfRotpdICYfW9hxmggHxE6TlHXx5U44ByZY6ux235gSxpCvuYGdaep27VSeUu5ubyFzgANOGtgIA3yHch7rQO_TkiwkxCllm7zW-WSMfjNJARHdoAds289IvPyqMO9aTYybKUv41Yk
edfa7b5c-c61e-4758-94c2-4b7685242161	32fa92d5-9b83-4cad-9663-fbe0ed536837	priority	100
97f2ccd5-6dd7-41a5-b26e-98327c2791bc	c6b9437d-f085-4c22-baf9-d895f6be3ffd	kc.user.profile.config	{"attributes":[{"name":"username","displayName":"${username}","validations":{"length":{"min":3,"max":255},"username-prohibited-characters":{},"up-username-not-idn-homograph":{}},"permissions":{"view":["admin","user"],"edit":["admin","user"]},"multivalued":false},{"name":"email","displayName":"${email}","validations":{"email":{},"length":{"max":255}},"required":{"roles":["user"]},"permissions":{"view":["admin","user"],"edit":["admin","user"]},"multivalued":false},{"name":"firstName","displayName":"${firstName}","validations":{"length":{"max":255},"person-name-prohibited-characters":{}},"required":{"roles":["user"]},"permissions":{"view":["admin","user"],"edit":["admin","user"]},"multivalued":false},{"name":"lastName","displayName":"${lastName}","validations":{"length":{"max":255},"person-name-prohibited-characters":{}},"required":{"roles":["user"]},"permissions":{"view":["admin","user"],"edit":["admin","user"]},"multivalued":false}],"groups":[{"name":"user-metadata","displayHeader":"User metadata","displayDescription":"Attributes, which refer to user metadata"}],"unmanagedAttributePolicy":"ENABLED"}
df4468b5-0ebb-4af8-8b9b-97187a1a0693	a408832a-469c-4e7a-966d-617aa20c3156	priority	100
bab91d43-2edd-480c-9cf6-b74cba41bcc8	a408832a-469c-4e7a-966d-617aa20c3156	algorithm	HS512
6492dc8b-79e0-438b-bf4b-d6f71a22bbd8	a408832a-469c-4e7a-966d-617aa20c3156	kid	da5b96f2-51ac-42b3-bc8b-889e5eeabb40
46055c83-f57d-4eac-87bd-5b5882e41062	a408832a-469c-4e7a-966d-617aa20c3156	secret	uWY3-siLDCSqBIawoYA1IOjGaSg_BoaVIRZ-3kFPGxnazkls8neaO5eU_b5Rl5eChCQSEk2MmLfH3pm_iZNTh5y_KA0oIsRXxH6l09hVajluaKXEkdAF2qLhuszw_6ZAzcT9P-WYt-hSsz7da3bQT3MIM8DGklG8w29UU5I-zhM
dbf48be3-aa01-489e-99a9-a8ad736a889c	2614fca7-d17e-41ce-83b9-8d4dbdfc86ac	secret	_l5kRxPzLf0qexnFwJgBeQ
e8231687-482e-49a0-ae21-447f576b6908	2614fca7-d17e-41ce-83b9-8d4dbdfc86ac	kid	1a26d83d-9484-4e40-8e73-c10a6e5c30ea
2dca6cf4-934c-4610-8773-92335498546a	2614fca7-d17e-41ce-83b9-8d4dbdfc86ac	priority	100
4ad5c0c1-db6b-4cfc-a395-a215315c6ef0	f1b3aea4-83c0-4927-af80-de991733efb1	algorithm	RSA-OAEP
4ea4cc1e-0d23-458c-9eec-45f59c90cf5c	87df47b7-488e-4d11-89eb-1a142669c9e7	kc.user.profile.config	{"attributes":[{"name":"username","displayName":"${username}","validations":{"length":{"min":3,"max":255},"username-prohibited-characters":{},"up-username-not-idn-homograph":{}},"permissions":{"view":["admin","user"],"edit":["admin","user"]},"multivalued":false},{"name":"email","displayName":"${email}","validations":{"email":{},"length":{"max":255}},"required":{"roles":["user"]},"permissions":{"view":["admin","user"],"edit":["admin","user"]},"multivalued":false},{"name":"firstName","displayName":"${firstName}","validations":{"length":{"max":255},"person-name-prohibited-characters":{}},"required":{"roles":["user"]},"permissions":{"view":["admin","user"],"edit":["admin","user"]},"multivalued":false},{"name":"lastName","displayName":"${lastName}","validations":{"length":{"max":255},"person-name-prohibited-characters":{}},"required":{"roles":["user"]},"permissions":{"view":["admin","user"],"edit":["admin","user"]},"multivalued":false}],"groups":[{"name":"user-metadata","displayHeader":"User metadata","displayDescription":"Attributes, which refer to user metadata"}],"unmanagedAttributePolicy":"ENABLED"}
44cbb6de-3a7d-402e-ba33-6473cb2a2eb4	f1b3aea4-83c0-4927-af80-de991733efb1	privateKey	MIIEpAIBAAKCAQEAvzH5FxkRTAKh81FtJeCdNQ2/CvquNO+UWeU47h7AbGPzGz1rpmGduWyWl+l2aHOGxlhjYMSeYldgn4m1ntk9wK0yX4Wg49/9305rfiJG74kIYMHAWU8Dnw7pbD66sGbP30I0Eq3Jlhgx7/TkkZRuXlL1x2wDQJAYpADNKzBuhI5cPfzuNN2EgKsqOOzwiDomQuvH7PW8fh9/X+WoZ++8vMLnk3kyl7fscKLMqlW5GfGRtbcsHf/6Le04FT4u1WlzYTAnot+HKK0sjlHYU6+D/kFKBY2IlKHIzGFz+TDw0x0xML2PkyZT+m3a8JmuDlJNiNjCk95G6Qowm0EFFlrp3QIDAQABAoIBAEfn2/XfZB1xmgXf+Ew4AvfKgTB4Yd8fwWvjpPsNDrJVDr+OZHeRG9f6KlLcpz8WP7hF0q44lBaJJRorGOaybiKSILFDGOTKv3hlzlCERKheoYt+IsCT0llWKtyeZOjl6cTd6aaivH9Z/yHvOK01pSKNUckb6sczykiX0pGtJ8tq9c74UnWmet0FSY/2Ma+VugX7lVn/tgnTIC8GgjBEiitRNzxGoO3tODKfe/4r/MjpGdAx0giyi+iM8EwyQdpqtCkrcgUq/x5o5dRLBbgo5QvU2b8CFMcHZtaGLEp4aWII+58nUa0bGppOZm9RK+vNmYfzrnzi1eKxm/q7Be5V2KMCgYEA8rYWqCnsd1tqvl+lEtMFu1ASSAkbnDEijZ1Dm3c/nPYF8gWPapNoZkdTc3ezlysW1SsN9bfB7vPn+ITYIpeCRetFBgiu0ACxJ2Y+sOL0igPqWf0qXXmk3lSga4yZ+T8cVU81+Q63QlpTJbYtwOZDvUT623U746UW9LvIvJAyYtMCgYEAyanR/qrXdCyD8VaHO3P5PBKJnSnUgbhV434wg5jo5FS6LiZzM8k7IQUPMgC+Ap5y7ULrnv71a7M+CawtNnCtNcK9oaaE0mqZB5RthBs6Wo8jDnduQhWUd0hoRFzmHQWXSMK9WERUu1nu9n/lO4HFBiVEIHpmgyLNRQUVmEMlso8CgYEA4Np2tQZZ7t2/T202dfEPz0/ID/p4HMSceUvntWDNizGVonkqO5ci0hZkJoYP2A1wUr5XaXv669AE6QTRY4Kviu0CfiuPIF5zwMeFbsRjvnUQKJxVIlIuvlbyY5DXmMSyEAqZOwo6FvutKjPTwXDR8se0C5pZiHWKheJuftqsA0cCgYEAvr4lph+pknTYh9k0KAPV5gASsdjy64+TjY26DX8AKZgKUcSEiqL3jo/eg4k4L/n28nutqcG2GJx9PWDhkvCYpJUCEHgUgYHaJNU7kQy/8RAaNIC6hT8sIncWSDMmZKT5JxUrSuW2z06NPBJUnChuVxXAjoe3lqnARlw8xLweV/kCgYAzXaWCztcjpdRPAN7H6aT9C1nBjhb3N3hrLH3tl4P7YbkgI7kMGArPApBOeg0iZtEhFzGdV+vVzp1ZChOX2kLumH9jcQnttd/PzNEwqI+JwIi+ngp9ONL1KKPDeDPF0Afjtx0DT1/SWUZV5uGOqB2U6V2NLcLVSbSzbaPuphfFww==
cae13dc3-c121-47b0-af37-b8345f4bea42	f1b3aea4-83c0-4927-af80-de991733efb1	keyUse	ENC
96ee3736-798a-4d58-9897-49b50b7f4199	f1b3aea4-83c0-4927-af80-de991733efb1	priority	100
1fd7afbf-4d36-4a74-b25e-ce263da30dd0	f1b3aea4-83c0-4927-af80-de991733efb1	certificate	MIIClzCCAX8CBgGTc7bMFDANBgkqhkiG9w0BAQsFADAPMQ0wCwYDVQQDDAR0ZXN0MB4XDTI0MTEyODE2NTc1OVoXDTM0MTEyODE2NTkzOVowDzENMAsGA1UEAwwEdGVzdDCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAL8x+RcZEUwCofNRbSXgnTUNvwr6rjTvlFnlOO4ewGxj8xs9a6ZhnblslpfpdmhzhsZYY2DEnmJXYJ+JtZ7ZPcCtMl+FoOPf/d9Oa34iRu+JCGDBwFlPA58O6Ww+urBmz99CNBKtyZYYMe/05JGUbl5S9cdsA0CQGKQAzSswboSOXD387jTdhICrKjjs8Ig6JkLrx+z1vH4ff1/lqGfvvLzC55N5Mpe37HCizKpVuRnxkbW3LB3/+i3tOBU+LtVpc2EwJ6LfhyitLI5R2FOvg/5BSgWNiJShyMxhc/kw8NMdMTC9j5MmU/pt2vCZrg5STYjYwpPeRukKMJtBBRZa6d0CAwEAATANBgkqhkiG9w0BAQsFAAOCAQEAZx2VN/rH7MtUC7SO76Vc+3tfZXhOAhrh9yOUECDEvGaAt+N98PHHZhrQZssDeOX4c0dev63t6LkACoT1OA5ENc2kcAMwc5ZCM++LnjNtwDmGKczT4D0pQozEDxe4n95BFMPdUwoZ14MgFsIwP397UHoZkH84NxT/JdEBj/b4A6OjlBTRu54goOiluyf7rmDKnvFuCpw8Yov06vV9JVPcMqb1KePUu3KUQSd1IUc7MgXxFtgiyy+xLIKlqzSGcSNE1ysGPqeXx71APsJIRckWHg4XS4NadTj0r2bqp34Lw3Bf6YY/mq8iDT+jrg2QZPkpFeP8UymOJ2wciT9VVJSNtg==
262aac7d-7328-4037-a623-3b87bb34f2c9	ece060d3-b278-4600-b3a8-e57ffd03c7c3	privateKey	MIIEpAIBAAKCAQEAr6SIwwqriJLZ1zJtK48SioxEKkpLITvjiccT3nbCPV6e8PD/9486MwSijg+A67x+eklq/ErbX56of/l6c//Wgoc+d3Ymp/hrDemQ05BrGRAJPDWLEZ97EuPzGIEVuw23itWm8Nb5N/uNsBNg0/RrLLrWpMiN/IrqMZJLjDbRMhcDQNZ40uTbdn/so9vTLNFXQlyAAMwwSWVCWFWDlR3Ea2L6krrHmWQcAhhWA+ZW3kYl6yqHBQhnpKm+EXBkjb9m8hV6izrAl9B0F26mhL6gOjCF93QHvYCDib3B26PWWB0v1PpYp2+O7y91H9TIpsw4g1n+khXP7YRBd9Jur8fcMQIDAQABAoIBAAT67xbKrWjyswMPTrmitaWgiwlA8Ibpdg7wyl8vMlw/m9Wcm8Fm+gE60yoghekJP/PTM5miPLymGMSCLCbwXOJ4F70W6Eswd8ad9gmNqX+9LOncYcxW34w+UuUBuf5DCg6BdtmS2t+rvg19SyxPXJ+Yz5++AQqlXLUZnxNDdaSMJvM1tpRSJyqKQ601dMIoOF9KAFWck6k1unGtd7KlrRB/H/5+k887/BRsZagI1RT4r9WaVYta8alZ26VQQmX78sSbJlYKOx21cgQkLnCxIxQOrfYQj9FPf2LP6orfx1dci0d9T/aQ2wHFVHR4uSYUajQBPpvhEanRJzWmhgovfIECgYEA4TKkQbrStf7rWxPpoZQiC43P9qb+mfBdmHcdDDOmC1zLaPnCZ4l/r776qa3w1tPsGCT4SeM/tOP3Rogpd3r7Y/+XP7v6Bo5rHjQg3THmSBdbAX1QATmIU3J+CnhsV6jiQhfSL14gnDufEvuBrNa5W06pTeiZYz7TtvafgXTbZiECgYEAx6q1QgHTDV3CWGTXk0o7KBeMwrbFhmWDSR0OiMrFbVT2oJAb6TKaTAm9iY9OCC2mtjg93A0WJ10gIKcDQTasoDKHvlBLR1Jvzxu5tprrYyo0DjUeJvn9ZVQQhIwN03V9CTfbiVvueAFR4FOfPbrZaXXtvQJjYUxRIsLPtdrLlBECgYEAuOClXtzsXcNRHu2DEUaWiJu8zoacYG6nuCKZs0m3mH5u89iPlAaa0AJesOx+2y8SFWRMfgRVacD5YWspd9B8D8VmO0msYrOLFzm+fTmZDzg1W0ywYMEFvVQyS5A/idu5HH+H66E/7mM4HhJijC2f8Gjsr2w0ePi8QOnJWzM/Z2ECgYB6PWXpOmq25S8FpP9iOnELM5fG4dT+WgnNak+qqdHDBOBVHsUnuQZEBTaYEDcqxKArMTWWEiD9CoOP2hAhDdMT5Lu8ZTYhPTQcrRG+3Qu9U2o5RIXoQObE8XmHXKNeUbCyQxNR/gl6SPyShT+ZsFpIuZ1KOTsUp3+I85Hs+T3nsQKBgQClA9Qysu1zkPT5K1bko9GR3MduwiDSxm/ACgJdL6On/sFTxekyoluO3oAnO6tFadRvmZdnSSRVfXDEachrZszlvh7A7YrGj2hrOOOlIryToEFkoaixZbODtQWKiQVoc1zl2MTKsN8EInyVq34o6KQH6Rt/aTOwbbxFlpcOgBGA4A==
9b71196b-c93a-4d07-9df6-7f19a31f290d	ece060d3-b278-4600-b3a8-e57ffd03c7c3	certificate	MIIClzCCAX8CBgGTc7bLBTANBgkqhkiG9w0BAQsFADAPMQ0wCwYDVQQDDAR0ZXN0MB4XDTI0MTEyODE2NTc1OVoXDTM0MTEyODE2NTkzOVowDzENMAsGA1UEAwwEdGVzdDCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAK+kiMMKq4iS2dcybSuPEoqMRCpKSyE744nHE952wj1envDw//ePOjMEoo4PgOu8fnpJavxK21+eqH/5enP/1oKHPnd2Jqf4aw3pkNOQaxkQCTw1ixGfexLj8xiBFbsNt4rVpvDW+Tf7jbATYNP0ayy61qTIjfyK6jGSS4w20TIXA0DWeNLk23Z/7KPb0yzRV0JcgADMMEllQlhVg5UdxGti+pK6x5lkHAIYVgPmVt5GJesqhwUIZ6SpvhFwZI2/ZvIVeos6wJfQdBdupoS+oDowhfd0B72Ag4m9wduj1lgdL9T6WKdvju8vdR/UyKbMOINZ/pIVz+2EQXfSbq/H3DECAwEAATANBgkqhkiG9w0BAQsFAAOCAQEAQYYNpk9b5L+mwB5gsrqV65xSXNsKrfjAwREzV/2TkfMHtUtWc3qgoSCzRokJtkDh20fwbHvQg2HCX8Q4zTh6syzBNP9pcQukoIcz6OTFgYkGR4t9XrPuRxz1jc+3DgrN+zEMfR8NUEWRr/Com2m735p40qNRUD9SsTkotXy0e5aA7AxCaDr7c/t6HcmfJFRyow92vvjuduGwZ1AEotJX7ir1FYXdXkVgh5yODHLagXLOHpLfXcJkL2XpN7vGZ0khD/KKzIsHIkRmAcftQWy8YhOGbRmqudoVasQaY5D+a+V4b9AsL6F6SgJSXOZALviBiVNgT21akUHoFJKiBZrRLg==
f78635f5-cfa7-4a3d-9206-95b573f7c0e3	ece060d3-b278-4600-b3a8-e57ffd03c7c3	priority	100
3097a853-3d31-4f89-a6a3-7f0766bcd80a	ece060d3-b278-4600-b3a8-e57ffd03c7c3	keyUse	SIG
9e8069ce-54b6-490b-9896-be9cc12f0223	cb3fc936-b565-44cd-842c-944c385b7992	max-clients	200
389c55d6-fae4-4a2f-9c7f-b8727dda08cb	750c7b29-a1ee-453b-9e26-4d357f4a1dea	allowed-protocol-mapper-types	oidc-usermodel-property-mapper
6ccd26c5-967f-4b8c-ae84-f58d35890119	750c7b29-a1ee-453b-9e26-4d357f4a1dea	allowed-protocol-mapper-types	oidc-full-name-mapper
60629b0b-da2d-44c7-ab4b-0f750a153fa1	750c7b29-a1ee-453b-9e26-4d357f4a1dea	allowed-protocol-mapper-types	saml-user-property-mapper
9e0d874b-6dfa-4a70-b625-4da6cb29daaf	750c7b29-a1ee-453b-9e26-4d357f4a1dea	allowed-protocol-mapper-types	saml-user-attribute-mapper
194564e1-cb10-46c0-8804-ce92d6998cd8	750c7b29-a1ee-453b-9e26-4d357f4a1dea	allowed-protocol-mapper-types	oidc-sha256-pairwise-sub-mapper
933bfcae-6462-486f-84e8-e593c38c5637	750c7b29-a1ee-453b-9e26-4d357f4a1dea	allowed-protocol-mapper-types	saml-role-list-mapper
6cf7c341-ea93-4c8e-bf57-e09c3828fd81	750c7b29-a1ee-453b-9e26-4d357f4a1dea	allowed-protocol-mapper-types	oidc-address-mapper
54ef3bb0-6a05-477e-a6a9-282de0af9118	750c7b29-a1ee-453b-9e26-4d357f4a1dea	allowed-protocol-mapper-types	oidc-usermodel-attribute-mapper
9eba1619-db60-4d57-9ee8-d56d890f8754	c30d38cf-afe9-4204-a805-0299b748d6e0	allow-default-scopes	true
11ae63b0-dd45-48b5-86d2-2944f54cce35	96e87b99-2fcd-4278-bbcd-907c39745892	allow-default-scopes	true
d23ef221-fd39-45b5-9d20-bcd939c1cba7	e18c8d00-d31f-47ee-93f1-ecd14e7fe86c	client-uris-must-match	true
b91d31b1-c1cc-4a5a-a3b1-bd5a01a0de31	e18c8d00-d31f-47ee-93f1-ecd14e7fe86c	host-sending-registration-request-must-match	true
035416c0-e9b1-485d-ae83-ff7993858146	c1b9084c-7d85-4963-bfdd-590d7f0ee606	allowed-protocol-mapper-types	oidc-full-name-mapper
\.


--
-- Data for Name: composite_role; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.composite_role (composite, child_role) FROM stdin;
7f6e982b-20ec-4574-a4d5-539f845a770e	b5df16b0-c491-40db-be0f-f5fd9813698d
7f6e982b-20ec-4574-a4d5-539f845a770e	5a9c8959-cb2a-4f93-ad5f-0ab1c2c590e5
7f6e982b-20ec-4574-a4d5-539f845a770e	bb250240-a9bc-454e-9f37-f85ce33b9bf4
7f6e982b-20ec-4574-a4d5-539f845a770e	2192eebb-f131-4215-890e-7bde94b336dc
7f6e982b-20ec-4574-a4d5-539f845a770e	b0b45185-cf1c-45f9-8a04-4b7b5c4d53f2
7f6e982b-20ec-4574-a4d5-539f845a770e	66864d04-e7b0-4795-afc2-0ce71754c66e
7f6e982b-20ec-4574-a4d5-539f845a770e	5e114d37-0688-49f3-832b-661f52ee733c
7f6e982b-20ec-4574-a4d5-539f845a770e	e07473fd-de7f-4999-81b3-0d8804d65d59
7f6e982b-20ec-4574-a4d5-539f845a770e	cf56997d-564b-4d48-9481-eaa898bdbf23
7f6e982b-20ec-4574-a4d5-539f845a770e	72857986-26f8-41a5-af57-8421631a61b7
7f6e982b-20ec-4574-a4d5-539f845a770e	ee2febf6-dbfc-416b-8a86-acab9c495a47
7f6e982b-20ec-4574-a4d5-539f845a770e	c04f1567-f719-44b9-81d4-b8bcd77e99f9
7f6e982b-20ec-4574-a4d5-539f845a770e	4111d529-7641-4a9f-bf73-783d73b34db9
7f6e982b-20ec-4574-a4d5-539f845a770e	c30a9928-59ba-4a10-a45e-501c9beadd4e
7f6e982b-20ec-4574-a4d5-539f845a770e	d13615a7-3791-426c-ab16-0a522f9c473f
7f6e982b-20ec-4574-a4d5-539f845a770e	84b7ef06-3c34-4ec0-be67-92ce10b153b9
7f6e982b-20ec-4574-a4d5-539f845a770e	13d83f1a-5947-41a5-ae33-050b0160b37c
7f6e982b-20ec-4574-a4d5-539f845a770e	dd8833fa-6644-4e78-8919-122c79dcde32
2192eebb-f131-4215-890e-7bde94b336dc	d13615a7-3791-426c-ab16-0a522f9c473f
2192eebb-f131-4215-890e-7bde94b336dc	dd8833fa-6644-4e78-8919-122c79dcde32
b0b45185-cf1c-45f9-8a04-4b7b5c4d53f2	84b7ef06-3c34-4ec0-be67-92ce10b153b9
e52fb623-86f0-4833-9c9b-dbbafbfc4d93	ec9b1d11-9140-4192-a4a9-68dce0acba25
e52fb623-86f0-4833-9c9b-dbbafbfc4d93	8d61b16e-aeeb-4cf1-b37e-3c56c3b0c2f3
8d61b16e-aeeb-4cf1-b37e-3c56c3b0c2f3	8b1d0e2e-4ca9-4e86-9b72-a9a36ea458b5
ffe5f76f-2478-4246-a573-73e1f5dd6de4	1674ac8f-675f-430a-8d86-d7fdd019372c
7f6e982b-20ec-4574-a4d5-539f845a770e	4f71ac80-f5c6-483b-a57b-95d3a62dd300
e52fb623-86f0-4833-9c9b-dbbafbfc4d93	b697644d-9a79-47cc-be19-b41bc079258a
e52fb623-86f0-4833-9c9b-dbbafbfc4d93	6906700a-deb4-4b3d-8856-8346c5996bf2
7f6e982b-20ec-4574-a4d5-539f845a770e	2313bd52-977b-48b4-a653-2189511987e5
7f6e982b-20ec-4574-a4d5-539f845a770e	f6214b20-dcc3-4285-bf58-c93b6cd8f5be
7f6e982b-20ec-4574-a4d5-539f845a770e	f0b39f30-bd4f-4a2e-966b-65672a5d7ed3
7f6e982b-20ec-4574-a4d5-539f845a770e	983259a2-7830-4cc7-8f65-6d0fa404580d
7f6e982b-20ec-4574-a4d5-539f845a770e	3df2d791-bc78-4639-bc75-cba165eff8ba
7f6e982b-20ec-4574-a4d5-539f845a770e	7b17674e-cd9d-446d-bd4d-f215f1666c03
7f6e982b-20ec-4574-a4d5-539f845a770e	5b73b9fc-021d-4c58-92b3-30088d7e741e
7f6e982b-20ec-4574-a4d5-539f845a770e	a8492758-84eb-4ba8-92ab-69fa678e0964
7f6e982b-20ec-4574-a4d5-539f845a770e	84cf1880-6550-4096-b10f-6f97e75ee2ef
7f6e982b-20ec-4574-a4d5-539f845a770e	62bbd9fc-8327-462e-982b-74873de472ca
7f6e982b-20ec-4574-a4d5-539f845a770e	62ad2008-6115-4bc9-930e-e42062b42520
7f6e982b-20ec-4574-a4d5-539f845a770e	9917e1a2-e7ec-43f1-b2a9-23fe7f543cd9
7f6e982b-20ec-4574-a4d5-539f845a770e	64652ce2-1738-45e4-8da7-1c16b36c835b
7f6e982b-20ec-4574-a4d5-539f845a770e	6a631fff-f677-4247-a2a1-ad991336c584
7f6e982b-20ec-4574-a4d5-539f845a770e	95f13184-edd0-4693-b91e-db224e78207d
7f6e982b-20ec-4574-a4d5-539f845a770e	645a69bf-1ac7-4c8b-badf-c1088bf27194
7f6e982b-20ec-4574-a4d5-539f845a770e	4bbb3bb1-6f7c-4660-9bda-fa3e8ff3ba67
983259a2-7830-4cc7-8f65-6d0fa404580d	95f13184-edd0-4693-b91e-db224e78207d
f0b39f30-bd4f-4a2e-966b-65672a5d7ed3	6a631fff-f677-4247-a2a1-ad991336c584
f0b39f30-bd4f-4a2e-966b-65672a5d7ed3	4bbb3bb1-6f7c-4660-9bda-fa3e8ff3ba67
34912a74-3713-4cee-b482-f7fcd604afff	f5647caa-e381-4f52-8839-313608754e28
34912a74-3713-4cee-b482-f7fcd604afff	25da286f-37ba-480b-82ba-b43024724ee8
34912a74-3713-4cee-b482-f7fcd604afff	305213e3-bf40-419d-ae7a-09e53a8c8681
34912a74-3713-4cee-b482-f7fcd604afff	6762e630-0b80-4453-823e-478d0fec76aa
34912a74-3713-4cee-b482-f7fcd604afff	793f9964-dc14-489f-accc-d281bf089b45
34912a74-3713-4cee-b482-f7fcd604afff	e821981e-c5e2-41ca-ab58-f54c6755f279
34912a74-3713-4cee-b482-f7fcd604afff	d5898665-00fc-4c33-a806-4d97b4bb8838
34912a74-3713-4cee-b482-f7fcd604afff	b6733d58-3a43-41e9-a2eb-ae360657d053
34912a74-3713-4cee-b482-f7fcd604afff	a0271e9a-a863-4fb3-9ee8-4172f1405742
34912a74-3713-4cee-b482-f7fcd604afff	0768ea7d-1a96-42af-b9cb-d0e3edbe8d28
34912a74-3713-4cee-b482-f7fcd604afff	94b009f8-3759-4af8-a01c-6bde819f1ea4
34912a74-3713-4cee-b482-f7fcd604afff	6ef84316-fba1-4885-b900-edfac3ac87a4
34912a74-3713-4cee-b482-f7fcd604afff	04459e5c-8386-45fc-82e2-f2021b7be253
34912a74-3713-4cee-b482-f7fcd604afff	8c76f207-a9f9-4e48-88bb-5f1beabea99b
34912a74-3713-4cee-b482-f7fcd604afff	819731d3-be54-4ce3-b55a-c583572cc682
34912a74-3713-4cee-b482-f7fcd604afff	4870d54d-2ca9-4a24-bcac-f109859c9725
34912a74-3713-4cee-b482-f7fcd604afff	51f207e1-0df3-466b-b890-8ffb2368b702
305213e3-bf40-419d-ae7a-09e53a8c8681	51f207e1-0df3-466b-b890-8ffb2368b702
305213e3-bf40-419d-ae7a-09e53a8c8681	8c76f207-a9f9-4e48-88bb-5f1beabea99b
42f64d4f-4059-4dca-aab6-44b0725e5469	9341e953-34c5-4700-9ef0-782171cbe232
6762e630-0b80-4453-823e-478d0fec76aa	819731d3-be54-4ce3-b55a-c583572cc682
42f64d4f-4059-4dca-aab6-44b0725e5469	898d7633-0975-41f6-989a-d2e661311e9d
898d7633-0975-41f6-989a-d2e661311e9d	6b7e944a-1f34-4c9f-9e4e-082b9174d6c3
343b54e3-55ad-491d-b513-87906876c16a	d4704af7-34cb-4f97-8907-db18dd0e3394
7f6e982b-20ec-4574-a4d5-539f845a770e	015876ed-deb6-4202-a3f1-455caa1df6fb
34912a74-3713-4cee-b482-f7fcd604afff	36f6b327-4e7e-48ec-8c3b-065fce32a555
42f64d4f-4059-4dca-aab6-44b0725e5469	8fb6b6ef-3b9e-4cdc-81ec-b1ed952dc628
42f64d4f-4059-4dca-aab6-44b0725e5469	f37266fd-f534-416d-b26d-43ade62a9679
\.


--
-- Data for Name: credential; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.credential (id, salt, type, user_id, created_date, user_label, secret_data, credential_data, priority) FROM stdin;
f53d2c5d-f0ab-4290-9340-3c6638926ec6	\N	password	dfef0e06-05d1-4103-a8a4-f3a19a7e2802	1653471802987	\N	{"value":"tYaZoNwVVr8BXIc8j/nsQhnNIMsnXVpBumzZalcVT2o=","salt":"0jJd6CKaUAXvqEqxBTHMpw==","additionalParameters":{}}	{"hashIterations":5,"algorithm":"argon2","additionalParameters":{"hashLength":["32"],"memory":["7168"],"type":["id"],"version":["1.3"],"parallelism":["1"]}}	10
5e7aafa8-e217-47e7-b127-258d8fb2007a	\N	password	3d0bd8f9-ad4a-4d08-babb-14a48f210450	1732813234860	My password	{"value":"DiOcrxoiNZeAAXrms1/NdsompBgL2DT+i2zSSrGYaaE=","salt":"bbIxLk9fmds2dyedrWXQXA==","additionalParameters":{}}	{"hashIterations":5,"algorithm":"argon2","additionalParameters":{"hashLength":["32"],"memory":["7168"],"type":["id"],"version":["1.3"],"parallelism":["1"]}}	10
\.


--
-- Data for Name: databasechangelog; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.databasechangelog (id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, comments, tag, liquibase, contexts, labels, deployment_id) FROM stdin;
authn-3.4.0.CR1-refresh-token-max-reuse	glavoie@gmail.com	META-INF/jpa-changelog-authz-3.4.0.CR1.xml	2022-05-25 09:38:52.262328	49	EXECUTED	9:d198654156881c46bfba39abd7769e69	addColumn tableName=REALM		\N	4.8.0	\N	\N	3471531609
19.0.0-10135	keycloak	META-INF/jpa-changelog-19.0.0.xml	2024-11-27 17:11:49.109419	108	EXECUTED	9:9518e495fdd22f78ad6425cc30630221	customChange		\N	4.29.1	\N	\N	2727508830
1.0.0.Final-KEYCLOAK-5461	sthorger@redhat.com	META-INF/jpa-changelog-1.0.0.Final.xml	2022-05-25 09:38:51.811497	1	EXECUTED	9:6f1016664e21e16d26517a4418f5e3df	createTable tableName=APPLICATION_DEFAULT_ROLES; createTable tableName=CLIENT; createTable tableName=CLIENT_SESSION; createTable tableName=CLIENT_SESSION_ROLE; createTable tableName=COMPOSITE_ROLE; createTable tableName=CREDENTIAL; createTable tab...		\N	4.8.0	\N	\N	3471531609
1.0.0.Final-KEYCLOAK-5461	sthorger@redhat.com	META-INF/db2-jpa-changelog-1.0.0.Final.xml	2022-05-25 09:38:51.815766	2	MARK_RAN	9:828775b1596a07d1200ba1d49e5e3941	createTable tableName=APPLICATION_DEFAULT_ROLES; createTable tableName=CLIENT; createTable tableName=CLIENT_SESSION; createTable tableName=CLIENT_SESSION_ROLE; createTable tableName=COMPOSITE_ROLE; createTable tableName=CREDENTIAL; createTable tab...		\N	4.8.0	\N	\N	3471531609
1.1.0.Beta1	sthorger@redhat.com	META-INF/jpa-changelog-1.1.0.Beta1.xml	2022-05-25 09:38:51.835824	3	EXECUTED	9:5f090e44a7d595883c1fb61f4b41fd38	delete tableName=CLIENT_SESSION_ROLE; delete tableName=CLIENT_SESSION; delete tableName=USER_SESSION; createTable tableName=CLIENT_ATTRIBUTES; createTable tableName=CLIENT_SESSION_NOTE; createTable tableName=APP_NODE_REGISTRATIONS; addColumn table...		\N	4.8.0	\N	\N	3471531609
1.1.0.Final	sthorger@redhat.com	META-INF/jpa-changelog-1.1.0.Final.xml	2022-05-25 09:38:51.837979	4	EXECUTED	9:c07e577387a3d2c04d1adc9aaad8730e	renameColumn newColumnName=EVENT_TIME, oldColumnName=TIME, tableName=EVENT_ENTITY		\N	4.8.0	\N	\N	3471531609
1.2.0.Beta1	psilva@redhat.com	META-INF/jpa-changelog-1.2.0.Beta1.xml	2022-05-25 09:38:51.87923	5	EXECUTED	9:b68ce996c655922dbcd2fe6b6ae72686	delete tableName=CLIENT_SESSION_ROLE; delete tableName=CLIENT_SESSION_NOTE; delete tableName=CLIENT_SESSION; delete tableName=USER_SESSION; createTable tableName=PROTOCOL_MAPPER; createTable tableName=PROTOCOL_MAPPER_CONFIG; createTable tableName=...		\N	4.8.0	\N	\N	3471531609
1.2.0.Beta1	psilva@redhat.com	META-INF/db2-jpa-changelog-1.2.0.Beta1.xml	2022-05-25 09:38:51.880651	6	MARK_RAN	9:543b5c9989f024fe35c6f6c5a97de88e	delete tableName=CLIENT_SESSION_ROLE; delete tableName=CLIENT_SESSION_NOTE; delete tableName=CLIENT_SESSION; delete tableName=USER_SESSION; createTable tableName=PROTOCOL_MAPPER; createTable tableName=PROTOCOL_MAPPER_CONFIG; createTable tableName=...		\N	4.8.0	\N	\N	3471531609
1.2.0.RC1	bburke@redhat.com	META-INF/jpa-changelog-1.2.0.CR1.xml	2022-05-25 09:38:51.918454	7	EXECUTED	9:765afebbe21cf5bbca048e632df38336	delete tableName=CLIENT_SESSION_ROLE; delete tableName=CLIENT_SESSION_NOTE; delete tableName=CLIENT_SESSION; delete tableName=USER_SESSION_NOTE; delete tableName=USER_SESSION; createTable tableName=MIGRATION_MODEL; createTable tableName=IDENTITY_P...		\N	4.8.0	\N	\N	3471531609
1.2.0.RC1	bburke@redhat.com	META-INF/db2-jpa-changelog-1.2.0.CR1.xml	2022-05-25 09:38:51.921207	8	MARK_RAN	9:db4a145ba11a6fdaefb397f6dbf829a1	delete tableName=CLIENT_SESSION_ROLE; delete tableName=CLIENT_SESSION_NOTE; delete tableName=CLIENT_SESSION; delete tableName=USER_SESSION_NOTE; delete tableName=USER_SESSION; createTable tableName=MIGRATION_MODEL; createTable tableName=IDENTITY_P...		\N	4.8.0	\N	\N	3471531609
1.2.0.Final	keycloak	META-INF/jpa-changelog-1.2.0.Final.xml	2022-05-25 09:38:51.925091	9	EXECUTED	9:9d05c7be10cdb873f8bcb41bc3a8ab23	update tableName=CLIENT; update tableName=CLIENT; update tableName=CLIENT		\N	4.8.0	\N	\N	3471531609
13.0.0-increase-column-size-federated	keycloak	META-INF/jpa-changelog-13.0.0.xml	2022-05-25 09:38:52.480452	94	EXECUTED	9:43c0c1055b6761b4b3e89de76d612ccf	modifyDataType columnName=CLIENT_ID, tableName=CLIENT_SCOPE_CLIENT; modifyDataType columnName=SCOPE_ID, tableName=CLIENT_SCOPE_CLIENT		\N	4.8.0	\N	\N	3471531609
1.3.0	bburke@redhat.com	META-INF/jpa-changelog-1.3.0.xml	2022-05-25 09:38:51.968445	10	EXECUTED	9:18593702353128d53111f9b1ff0b82b8	delete tableName=CLIENT_SESSION_ROLE; delete tableName=CLIENT_SESSION_PROT_MAPPER; delete tableName=CLIENT_SESSION_NOTE; delete tableName=CLIENT_SESSION; delete tableName=USER_SESSION_NOTE; delete tableName=USER_SESSION; createTable tableName=ADMI...		\N	4.8.0	\N	\N	3471531609
1.4.0	bburke@redhat.com	META-INF/jpa-changelog-1.4.0.xml	2022-05-25 09:38:51.995361	11	EXECUTED	9:6122efe5f090e41a85c0f1c9e52cbb62	delete tableName=CLIENT_SESSION_AUTH_STATUS; delete tableName=CLIENT_SESSION_ROLE; delete tableName=CLIENT_SESSION_PROT_MAPPER; delete tableName=CLIENT_SESSION_NOTE; delete tableName=CLIENT_SESSION; delete tableName=USER_SESSION_NOTE; delete table...		\N	4.8.0	\N	\N	3471531609
1.4.0	bburke@redhat.com	META-INF/db2-jpa-changelog-1.4.0.xml	2022-05-25 09:38:51.996641	12	MARK_RAN	9:e1ff28bf7568451453f844c5d54bb0b5	delete tableName=CLIENT_SESSION_AUTH_STATUS; delete tableName=CLIENT_SESSION_ROLE; delete tableName=CLIENT_SESSION_PROT_MAPPER; delete tableName=CLIENT_SESSION_NOTE; delete tableName=CLIENT_SESSION; delete tableName=USER_SESSION_NOTE; delete table...		\N	4.8.0	\N	\N	3471531609
1.5.0	bburke@redhat.com	META-INF/jpa-changelog-1.5.0.xml	2022-05-25 09:38:52.004732	13	EXECUTED	9:7af32cd8957fbc069f796b61217483fd	delete tableName=CLIENT_SESSION_AUTH_STATUS; delete tableName=CLIENT_SESSION_ROLE; delete tableName=CLIENT_SESSION_PROT_MAPPER; delete tableName=CLIENT_SESSION_NOTE; delete tableName=CLIENT_SESSION; delete tableName=USER_SESSION_NOTE; delete table...		\N	4.8.0	\N	\N	3471531609
1.6.1_from15	mposolda@redhat.com	META-INF/jpa-changelog-1.6.1.xml	2022-05-25 09:38:52.013819	14	EXECUTED	9:6005e15e84714cd83226bf7879f54190	addColumn tableName=REALM; addColumn tableName=KEYCLOAK_ROLE; addColumn tableName=CLIENT; createTable tableName=OFFLINE_USER_SESSION; createTable tableName=OFFLINE_CLIENT_SESSION; addPrimaryKey constraintName=CONSTRAINT_OFFL_US_SES_PK2, tableName=...		\N	4.8.0	\N	\N	3471531609
1.6.1_from16-pre	mposolda@redhat.com	META-INF/jpa-changelog-1.6.1.xml	2022-05-25 09:38:52.014861	15	MARK_RAN	9:bf656f5a2b055d07f314431cae76f06c	delete tableName=OFFLINE_CLIENT_SESSION; delete tableName=OFFLINE_USER_SESSION		\N	4.8.0	\N	\N	3471531609
1.6.1_from16	mposolda@redhat.com	META-INF/jpa-changelog-1.6.1.xml	2022-05-25 09:38:52.01586	16	MARK_RAN	9:f8dadc9284440469dcf71e25ca6ab99b	dropPrimaryKey constraintName=CONSTRAINT_OFFLINE_US_SES_PK, tableName=OFFLINE_USER_SESSION; dropPrimaryKey constraintName=CONSTRAINT_OFFLINE_CL_SES_PK, tableName=OFFLINE_CLIENT_SESSION; addColumn tableName=OFFLINE_USER_SESSION; update tableName=OF...		\N	4.8.0	\N	\N	3471531609
1.6.1	mposolda@redhat.com	META-INF/jpa-changelog-1.6.1.xml	2022-05-25 09:38:52.016756	17	EXECUTED	9:d41d8cd98f00b204e9800998ecf8427e	empty		\N	4.8.0	\N	\N	3471531609
1.7.0	bburke@redhat.com	META-INF/jpa-changelog-1.7.0.xml	2022-05-25 09:38:52.034663	18	EXECUTED	9:3368ff0be4c2855ee2dd9ca813b38d8e	createTable tableName=KEYCLOAK_GROUP; createTable tableName=GROUP_ROLE_MAPPING; createTable tableName=GROUP_ATTRIBUTE; createTable tableName=USER_GROUP_MEMBERSHIP; createTable tableName=REALM_DEFAULT_GROUPS; addColumn tableName=IDENTITY_PROVIDER; ...		\N	4.8.0	\N	\N	3471531609
1.8.0	mposolda@redhat.com	META-INF/jpa-changelog-1.8.0.xml	2022-05-25 09:38:52.051861	19	EXECUTED	9:8ac2fb5dd030b24c0570a763ed75ed20	addColumn tableName=IDENTITY_PROVIDER; createTable tableName=CLIENT_TEMPLATE; createTable tableName=CLIENT_TEMPLATE_ATTRIBUTES; createTable tableName=TEMPLATE_SCOPE_MAPPING; dropNotNullConstraint columnName=CLIENT_ID, tableName=PROTOCOL_MAPPER; ad...		\N	4.8.0	\N	\N	3471531609
1.8.0-2	keycloak	META-INF/jpa-changelog-1.8.0.xml	2022-05-25 09:38:52.05404	20	EXECUTED	9:f91ddca9b19743db60e3057679810e6c	dropDefaultValue columnName=ALGORITHM, tableName=CREDENTIAL; update tableName=CREDENTIAL		\N	4.8.0	\N	\N	3471531609
1.8.0	mposolda@redhat.com	META-INF/db2-jpa-changelog-1.8.0.xml	2022-05-25 09:38:52.055058	21	MARK_RAN	9:831e82914316dc8a57dc09d755f23c51	addColumn tableName=IDENTITY_PROVIDER; createTable tableName=CLIENT_TEMPLATE; createTable tableName=CLIENT_TEMPLATE_ATTRIBUTES; createTable tableName=TEMPLATE_SCOPE_MAPPING; dropNotNullConstraint columnName=CLIENT_ID, tableName=PROTOCOL_MAPPER; ad...		\N	4.8.0	\N	\N	3471531609
1.8.0-2	keycloak	META-INF/db2-jpa-changelog-1.8.0.xml	2022-05-25 09:38:52.056133	22	MARK_RAN	9:f91ddca9b19743db60e3057679810e6c	dropDefaultValue columnName=ALGORITHM, tableName=CREDENTIAL; update tableName=CREDENTIAL		\N	4.8.0	\N	\N	3471531609
1.9.0	mposolda@redhat.com	META-INF/jpa-changelog-1.9.0.xml	2022-05-25 09:38:52.065683	23	EXECUTED	9:bc3d0f9e823a69dc21e23e94c7a94bb1	update tableName=REALM; update tableName=REALM; update tableName=REALM; update tableName=REALM; update tableName=CREDENTIAL; update tableName=CREDENTIAL; update tableName=CREDENTIAL; update tableName=REALM; update tableName=REALM; customChange; dr...		\N	4.8.0	\N	\N	3471531609
1.9.1	keycloak	META-INF/jpa-changelog-1.9.1.xml	2022-05-25 09:38:52.068022	24	EXECUTED	9:c9999da42f543575ab790e76439a2679	modifyDataType columnName=PRIVATE_KEY, tableName=REALM; modifyDataType columnName=PUBLIC_KEY, tableName=REALM; modifyDataType columnName=CERTIFICATE, tableName=REALM		\N	4.8.0	\N	\N	3471531609
1.9.1	keycloak	META-INF/db2-jpa-changelog-1.9.1.xml	2022-05-25 09:38:52.069	25	MARK_RAN	9:0d6c65c6f58732d81569e77b10ba301d	modifyDataType columnName=PRIVATE_KEY, tableName=REALM; modifyDataType columnName=CERTIFICATE, tableName=REALM		\N	4.8.0	\N	\N	3471531609
1.9.2	keycloak	META-INF/jpa-changelog-1.9.2.xml	2022-05-25 09:38:52.082215	26	EXECUTED	9:fc576660fc016ae53d2d4778d84d86d0	createIndex indexName=IDX_USER_EMAIL, tableName=USER_ENTITY; createIndex indexName=IDX_USER_ROLE_MAPPING, tableName=USER_ROLE_MAPPING; createIndex indexName=IDX_USER_GROUP_MAPPING, tableName=USER_GROUP_MEMBERSHIP; createIndex indexName=IDX_USER_CO...		\N	4.8.0	\N	\N	3471531609
authz-2.0.0	psilva@redhat.com	META-INF/jpa-changelog-authz-2.0.0.xml	2022-05-25 09:38:52.113113	27	EXECUTED	9:43ed6b0da89ff77206289e87eaa9c024	createTable tableName=RESOURCE_SERVER; addPrimaryKey constraintName=CONSTRAINT_FARS, tableName=RESOURCE_SERVER; addUniqueConstraint constraintName=UK_AU8TT6T700S9V50BU18WS5HA6, tableName=RESOURCE_SERVER; createTable tableName=RESOURCE_SERVER_RESOU...		\N	4.8.0	\N	\N	3471531609
authz-2.5.1	psilva@redhat.com	META-INF/jpa-changelog-authz-2.5.1.xml	2022-05-25 09:38:52.114654	28	EXECUTED	9:44bae577f551b3738740281eceb4ea70	update tableName=RESOURCE_SERVER_POLICY		\N	4.8.0	\N	\N	3471531609
2.1.0-KEYCLOAK-5461	bburke@redhat.com	META-INF/jpa-changelog-2.1.0.xml	2022-05-25 09:38:52.140769	29	EXECUTED	9:bd88e1f833df0420b01e114533aee5e8	createTable tableName=BROKER_LINK; createTable tableName=FED_USER_ATTRIBUTE; createTable tableName=FED_USER_CONSENT; createTable tableName=FED_USER_CONSENT_ROLE; createTable tableName=FED_USER_CONSENT_PROT_MAPPER; createTable tableName=FED_USER_CR...		\N	4.8.0	\N	\N	3471531609
2.2.0	bburke@redhat.com	META-INF/jpa-changelog-2.2.0.xml	2022-05-25 09:38:52.14661	30	EXECUTED	9:a7022af5267f019d020edfe316ef4371	addColumn tableName=ADMIN_EVENT_ENTITY; createTable tableName=CREDENTIAL_ATTRIBUTE; createTable tableName=FED_CREDENTIAL_ATTRIBUTE; modifyDataType columnName=VALUE, tableName=CREDENTIAL; addForeignKeyConstraint baseTableName=FED_CREDENTIAL_ATTRIBU...		\N	4.8.0	\N	\N	3471531609
18.0.0-10625-IDX_ADMIN_EVENT_TIME	keycloak	META-INF/jpa-changelog-18.0.0.xml	2022-05-25 09:38:52.515634	106	EXECUTED	9:015479dbd691d9cc8669282f4828c41d	createIndex indexName=IDX_ADMIN_EVENT_TIME, tableName=ADMIN_EVENT_ENTITY		\N	4.8.0	\N	\N	3471531609
2.3.0	bburke@redhat.com	META-INF/jpa-changelog-2.3.0.xml	2022-05-25 09:38:52.154362	31	EXECUTED	9:fc155c394040654d6a79227e56f5e25a	createTable tableName=FEDERATED_USER; addPrimaryKey constraintName=CONSTR_FEDERATED_USER, tableName=FEDERATED_USER; dropDefaultValue columnName=TOTP, tableName=USER_ENTITY; dropColumn columnName=TOTP, tableName=USER_ENTITY; addColumn tableName=IDE...		\N	4.8.0	\N	\N	3471531609
2.4.0	bburke@redhat.com	META-INF/jpa-changelog-2.4.0.xml	2022-05-25 09:38:52.156734	32	EXECUTED	9:eac4ffb2a14795e5dc7b426063e54d88	customChange		\N	4.8.0	\N	\N	3471531609
2.5.0	bburke@redhat.com	META-INF/jpa-changelog-2.5.0.xml	2022-05-25 09:38:52.159202	33	EXECUTED	9:54937c05672568c4c64fc9524c1e9462	customChange; modifyDataType columnName=USER_ID, tableName=OFFLINE_USER_SESSION		\N	4.8.0	\N	\N	3471531609
2.5.0-unicode-oracle	hmlnarik@redhat.com	META-INF/jpa-changelog-2.5.0.xml	2022-05-25 09:38:52.160107	34	MARK_RAN	9:3a32bace77c84d7678d035a7f5a8084e	modifyDataType columnName=DESCRIPTION, tableName=AUTHENTICATION_FLOW; modifyDataType columnName=DESCRIPTION, tableName=CLIENT_TEMPLATE; modifyDataType columnName=DESCRIPTION, tableName=RESOURCE_SERVER_POLICY; modifyDataType columnName=DESCRIPTION,...		\N	4.8.0	\N	\N	3471531609
2.5.0-unicode-other-dbs	hmlnarik@redhat.com	META-INF/jpa-changelog-2.5.0.xml	2022-05-25 09:38:52.171003	35	EXECUTED	9:33d72168746f81f98ae3a1e8e0ca3554	modifyDataType columnName=DESCRIPTION, tableName=AUTHENTICATION_FLOW; modifyDataType columnName=DESCRIPTION, tableName=CLIENT_TEMPLATE; modifyDataType columnName=DESCRIPTION, tableName=RESOURCE_SERVER_POLICY; modifyDataType columnName=DESCRIPTION,...		\N	4.8.0	\N	\N	3471531609
2.5.0-duplicate-email-support	slawomir@dabek.name	META-INF/jpa-changelog-2.5.0.xml	2022-05-25 09:38:52.173094	36	EXECUTED	9:61b6d3d7a4c0e0024b0c839da283da0c	addColumn tableName=REALM		\N	4.8.0	\N	\N	3471531609
2.5.0-unique-group-names	hmlnarik@redhat.com	META-INF/jpa-changelog-2.5.0.xml	2022-05-25 09:38:52.175607	37	EXECUTED	9:8dcac7bdf7378e7d823cdfddebf72fda	addUniqueConstraint constraintName=SIBLING_NAMES, tableName=KEYCLOAK_GROUP		\N	4.8.0	\N	\N	3471531609
2.5.1	bburke@redhat.com	META-INF/jpa-changelog-2.5.1.xml	2022-05-25 09:38:52.177219	38	EXECUTED	9:a2b870802540cb3faa72098db5388af3	addColumn tableName=FED_USER_CONSENT		\N	4.8.0	\N	\N	3471531609
3.0.0	bburke@redhat.com	META-INF/jpa-changelog-3.0.0.xml	2022-05-25 09:38:52.179376	39	EXECUTED	9:132a67499ba24bcc54fb5cbdcfe7e4c0	addColumn tableName=IDENTITY_PROVIDER		\N	4.8.0	\N	\N	3471531609
3.2.0-fix	keycloak	META-INF/jpa-changelog-3.2.0.xml	2022-05-25 09:38:52.180822	40	MARK_RAN	9:938f894c032f5430f2b0fafb1a243462	addNotNullConstraint columnName=REALM_ID, tableName=CLIENT_INITIAL_ACCESS		\N	4.8.0	\N	\N	3471531609
3.2.0-fix-with-keycloak-5416	keycloak	META-INF/jpa-changelog-3.2.0.xml	2022-05-25 09:38:52.182068	41	MARK_RAN	9:845c332ff1874dc5d35974b0babf3006	dropIndex indexName=IDX_CLIENT_INIT_ACC_REALM, tableName=CLIENT_INITIAL_ACCESS; addNotNullConstraint columnName=REALM_ID, tableName=CLIENT_INITIAL_ACCESS; createIndex indexName=IDX_CLIENT_INIT_ACC_REALM, tableName=CLIENT_INITIAL_ACCESS		\N	4.8.0	\N	\N	3471531609
3.2.0-fix-offline-sessions	hmlnarik	META-INF/jpa-changelog-3.2.0.xml	2022-05-25 09:38:52.185195	42	EXECUTED	9:fc86359c079781adc577c5a217e4d04c	customChange		\N	4.8.0	\N	\N	3471531609
3.2.0-fixed	keycloak	META-INF/jpa-changelog-3.2.0.xml	2022-05-25 09:38:52.238259	43	EXECUTED	9:59a64800e3c0d09b825f8a3b444fa8f4	addColumn tableName=REALM; dropPrimaryKey constraintName=CONSTRAINT_OFFL_CL_SES_PK2, tableName=OFFLINE_CLIENT_SESSION; dropColumn columnName=CLIENT_SESSION_ID, tableName=OFFLINE_CLIENT_SESSION; addPrimaryKey constraintName=CONSTRAINT_OFFL_CL_SES_P...		\N	4.8.0	\N	\N	3471531609
3.3.0	keycloak	META-INF/jpa-changelog-3.3.0.xml	2022-05-25 09:38:52.240166	44	EXECUTED	9:d48d6da5c6ccf667807f633fe489ce88	addColumn tableName=USER_ENTITY		\N	4.8.0	\N	\N	3471531609
authz-3.4.0.CR1-resource-server-pk-change-part1	glavoie@gmail.com	META-INF/jpa-changelog-authz-3.4.0.CR1.xml	2022-05-25 09:38:52.242124	45	EXECUTED	9:dde36f7973e80d71fceee683bc5d2951	addColumn tableName=RESOURCE_SERVER_POLICY; addColumn tableName=RESOURCE_SERVER_RESOURCE; addColumn tableName=RESOURCE_SERVER_SCOPE		\N	4.8.0	\N	\N	3471531609
authz-3.4.0.CR1-resource-server-pk-change-part2-KEYCLOAK-6095	hmlnarik@redhat.com	META-INF/jpa-changelog-authz-3.4.0.CR1.xml	2022-05-25 09:38:52.244459	46	EXECUTED	9:b855e9b0a406b34fa323235a0cf4f640	customChange		\N	4.8.0	\N	\N	3471531609
authz-3.4.0.CR1-resource-server-pk-change-part3-fixed	glavoie@gmail.com	META-INF/jpa-changelog-authz-3.4.0.CR1.xml	2022-05-25 09:38:52.245321	47	MARK_RAN	9:51abbacd7b416c50c4421a8cabf7927e	dropIndex indexName=IDX_RES_SERV_POL_RES_SERV, tableName=RESOURCE_SERVER_POLICY; dropIndex indexName=IDX_RES_SRV_RES_RES_SRV, tableName=RESOURCE_SERVER_RESOURCE; dropIndex indexName=IDX_RES_SRV_SCOPE_RES_SRV, tableName=RESOURCE_SERVER_SCOPE		\N	4.8.0	\N	\N	3471531609
authz-3.4.0.CR1-resource-server-pk-change-part3-fixed-nodropindex	glavoie@gmail.com	META-INF/jpa-changelog-authz-3.4.0.CR1.xml	2022-05-25 09:38:52.260466	48	EXECUTED	9:bdc99e567b3398bac83263d375aad143	addNotNullConstraint columnName=RESOURCE_SERVER_CLIENT_ID, tableName=RESOURCE_SERVER_POLICY; addNotNullConstraint columnName=RESOURCE_SERVER_CLIENT_ID, tableName=RESOURCE_SERVER_RESOURCE; addNotNullConstraint columnName=RESOURCE_SERVER_CLIENT_ID, ...		\N	4.8.0	\N	\N	3471531609
3.4.0	keycloak	META-INF/jpa-changelog-3.4.0.xml	2022-05-25 09:38:52.28042	50	EXECUTED	9:cfdd8736332ccdd72c5256ccb42335db	addPrimaryKey constraintName=CONSTRAINT_REALM_DEFAULT_ROLES, tableName=REALM_DEFAULT_ROLES; addPrimaryKey constraintName=CONSTRAINT_COMPOSITE_ROLE, tableName=COMPOSITE_ROLE; addPrimaryKey constraintName=CONSTR_REALM_DEFAULT_GROUPS, tableName=REALM...		\N	4.8.0	\N	\N	3471531609
3.4.0-KEYCLOAK-5230	hmlnarik@redhat.com	META-INF/jpa-changelog-3.4.0.xml	2022-05-25 09:38:52.293801	51	EXECUTED	9:7c84de3d9bd84d7f077607c1a4dcb714	createIndex indexName=IDX_FU_ATTRIBUTE, tableName=FED_USER_ATTRIBUTE; createIndex indexName=IDX_FU_CONSENT, tableName=FED_USER_CONSENT; createIndex indexName=IDX_FU_CONSENT_RU, tableName=FED_USER_CONSENT; createIndex indexName=IDX_FU_CREDENTIAL, t...		\N	4.8.0	\N	\N	3471531609
3.4.1	psilva@redhat.com	META-INF/jpa-changelog-3.4.1.xml	2022-05-25 09:38:52.295348	52	EXECUTED	9:5a6bb36cbefb6a9d6928452c0852af2d	modifyDataType columnName=VALUE, tableName=CLIENT_ATTRIBUTES		\N	4.8.0	\N	\N	3471531609
3.4.2	keycloak	META-INF/jpa-changelog-3.4.2.xml	2022-05-25 09:38:52.296511	53	EXECUTED	9:8f23e334dbc59f82e0a328373ca6ced0	update tableName=REALM		\N	4.8.0	\N	\N	3471531609
3.4.2-KEYCLOAK-5172	mkanis@redhat.com	META-INF/jpa-changelog-3.4.2.xml	2022-05-25 09:38:52.297622	54	EXECUTED	9:9156214268f09d970cdf0e1564d866af	update tableName=CLIENT		\N	4.8.0	\N	\N	3471531609
4.0.0-KEYCLOAK-6335	bburke@redhat.com	META-INF/jpa-changelog-4.0.0.xml	2022-05-25 09:38:52.300343	55	EXECUTED	9:db806613b1ed154826c02610b7dbdf74	createTable tableName=CLIENT_AUTH_FLOW_BINDINGS; addPrimaryKey constraintName=C_CLI_FLOW_BIND, tableName=CLIENT_AUTH_FLOW_BINDINGS		\N	4.8.0	\N	\N	3471531609
4.0.0-CLEANUP-UNUSED-TABLE	bburke@redhat.com	META-INF/jpa-changelog-4.0.0.xml	2022-05-25 09:38:52.302362	56	EXECUTED	9:229a041fb72d5beac76bb94a5fa709de	dropTable tableName=CLIENT_IDENTITY_PROV_MAPPING		\N	4.8.0	\N	\N	3471531609
18.0.15-30992-index-consent	keycloak	META-INF/jpa-changelog-18.0.15.xml	2024-11-27 17:11:49.087394	107	EXECUTED	9:80071ede7a05604b1f4906f3bf3b00f0	createIndex indexName=IDX_USCONSENT_SCOPE_ID, tableName=USER_CONSENT_CLIENT_SCOPE		\N	4.29.1	\N	\N	2727508830
4.0.0-KEYCLOAK-6228	bburke@redhat.com	META-INF/jpa-changelog-4.0.0.xml	2022-05-25 09:38:52.324676	57	EXECUTED	9:079899dade9c1e683f26b2aa9ca6ff04	dropUniqueConstraint constraintName=UK_JKUWUVD56ONTGSUHOGM8UEWRT, tableName=USER_CONSENT; dropNotNullConstraint columnName=CLIENT_ID, tableName=USER_CONSENT; addColumn tableName=USER_CONSENT; addUniqueConstraint constraintName=UK_JKUWUVD56ONTGSUHO...		\N	4.8.0	\N	\N	3471531609
4.0.0-KEYCLOAK-5579-fixed	mposolda@redhat.com	META-INF/jpa-changelog-4.0.0.xml	2022-05-25 09:38:52.361517	58	EXECUTED	9:139b79bcbbfe903bb1c2d2a4dbf001d9	dropForeignKeyConstraint baseTableName=CLIENT_TEMPLATE_ATTRIBUTES, constraintName=FK_CL_TEMPL_ATTR_TEMPL; renameTable newTableName=CLIENT_SCOPE_ATTRIBUTES, oldTableName=CLIENT_TEMPLATE_ATTRIBUTES; renameColumn newColumnName=SCOPE_ID, oldColumnName...		\N	4.8.0	\N	\N	3471531609
authz-4.0.0.CR1	psilva@redhat.com	META-INF/jpa-changelog-authz-4.0.0.CR1.xml	2022-05-25 09:38:52.373489	59	EXECUTED	9:b55738ad889860c625ba2bf483495a04	createTable tableName=RESOURCE_SERVER_PERM_TICKET; addPrimaryKey constraintName=CONSTRAINT_FAPMT, tableName=RESOURCE_SERVER_PERM_TICKET; addForeignKeyConstraint baseTableName=RESOURCE_SERVER_PERM_TICKET, constraintName=FK_FRSRHO213XCX4WNKOG82SSPMT...		\N	4.8.0	\N	\N	3471531609
authz-4.0.0.Beta3	psilva@redhat.com	META-INF/jpa-changelog-authz-4.0.0.Beta3.xml	2022-05-25 09:38:52.376476	60	EXECUTED	9:e0057eac39aa8fc8e09ac6cfa4ae15fe	addColumn tableName=RESOURCE_SERVER_POLICY; addColumn tableName=RESOURCE_SERVER_PERM_TICKET; addForeignKeyConstraint baseTableName=RESOURCE_SERVER_PERM_TICKET, constraintName=FK_FRSRPO2128CX4WNKOG82SSRFY, referencedTableName=RESOURCE_SERVER_POLICY		\N	4.8.0	\N	\N	3471531609
authz-4.2.0.Final	mhajas@redhat.com	META-INF/jpa-changelog-authz-4.2.0.Final.xml	2022-05-25 09:38:52.380686	61	EXECUTED	9:42a33806f3a0443fe0e7feeec821326c	createTable tableName=RESOURCE_URIS; addForeignKeyConstraint baseTableName=RESOURCE_URIS, constraintName=FK_RESOURCE_SERVER_URIS, referencedTableName=RESOURCE_SERVER_RESOURCE; customChange; dropColumn columnName=URI, tableName=RESOURCE_SERVER_RESO...		\N	4.8.0	\N	\N	3471531609
authz-4.2.0.Final-KEYCLOAK-9944	hmlnarik@redhat.com	META-INF/jpa-changelog-authz-4.2.0.Final.xml	2022-05-25 09:38:52.383514	62	EXECUTED	9:9968206fca46eecc1f51db9c024bfe56	addPrimaryKey constraintName=CONSTRAINT_RESOUR_URIS_PK, tableName=RESOURCE_URIS		\N	4.8.0	\N	\N	3471531609
4.2.0-KEYCLOAK-6313	wadahiro@gmail.com	META-INF/jpa-changelog-4.2.0.xml	2022-05-25 09:38:52.385309	63	EXECUTED	9:92143a6daea0a3f3b8f598c97ce55c3d	addColumn tableName=REQUIRED_ACTION_PROVIDER		\N	4.8.0	\N	\N	3471531609
4.3.0-KEYCLOAK-7984	wadahiro@gmail.com	META-INF/jpa-changelog-4.3.0.xml	2022-05-25 09:38:52.386794	64	EXECUTED	9:82bab26a27195d889fb0429003b18f40	update tableName=REQUIRED_ACTION_PROVIDER		\N	4.8.0	\N	\N	3471531609
4.6.0-KEYCLOAK-7950	psilva@redhat.com	META-INF/jpa-changelog-4.6.0.xml	2022-05-25 09:38:52.388281	65	EXECUTED	9:e590c88ddc0b38b0ae4249bbfcb5abc3	update tableName=RESOURCE_SERVER_RESOURCE		\N	4.8.0	\N	\N	3471531609
4.6.0-KEYCLOAK-8377	keycloak	META-INF/jpa-changelog-4.6.0.xml	2022-05-25 09:38:52.394209	66	EXECUTED	9:5c1f475536118dbdc38d5d7977950cc0	createTable tableName=ROLE_ATTRIBUTE; addPrimaryKey constraintName=CONSTRAINT_ROLE_ATTRIBUTE_PK, tableName=ROLE_ATTRIBUTE; addForeignKeyConstraint baseTableName=ROLE_ATTRIBUTE, constraintName=FK_ROLE_ATTRIBUTE_ID, referencedTableName=KEYCLOAK_ROLE...		\N	4.8.0	\N	\N	3471531609
4.6.0-KEYCLOAK-8555	gideonray@gmail.com	META-INF/jpa-changelog-4.6.0.xml	2022-05-25 09:38:52.396845	67	EXECUTED	9:e7c9f5f9c4d67ccbbcc215440c718a17	createIndex indexName=IDX_COMPONENT_PROVIDER_TYPE, tableName=COMPONENT		\N	4.8.0	\N	\N	3471531609
4.7.0-KEYCLOAK-1267	sguilhen@redhat.com	META-INF/jpa-changelog-4.7.0.xml	2022-05-25 09:38:52.399242	68	EXECUTED	9:88e0bfdda924690d6f4e430c53447dd5	addColumn tableName=REALM		\N	4.8.0	\N	\N	3471531609
4.7.0-KEYCLOAK-7275	keycloak	META-INF/jpa-changelog-4.7.0.xml	2022-05-25 09:38:52.404755	69	EXECUTED	9:f53177f137e1c46b6a88c59ec1cb5218	renameColumn newColumnName=CREATED_ON, oldColumnName=LAST_SESSION_REFRESH, tableName=OFFLINE_USER_SESSION; addNotNullConstraint columnName=CREATED_ON, tableName=OFFLINE_USER_SESSION; addColumn tableName=OFFLINE_USER_SESSION; customChange; createIn...		\N	4.8.0	\N	\N	3471531609
4.8.0-KEYCLOAK-8835	sguilhen@redhat.com	META-INF/jpa-changelog-4.8.0.xml	2022-05-25 09:38:52.407706	70	EXECUTED	9:a74d33da4dc42a37ec27121580d1459f	addNotNullConstraint columnName=SSO_MAX_LIFESPAN_REMEMBER_ME, tableName=REALM; addNotNullConstraint columnName=SSO_IDLE_TIMEOUT_REMEMBER_ME, tableName=REALM		\N	4.8.0	\N	\N	3471531609
authz-7.0.0-KEYCLOAK-10443	psilva@redhat.com	META-INF/jpa-changelog-authz-7.0.0.xml	2022-05-25 09:38:52.409667	71	EXECUTED	9:fd4ade7b90c3b67fae0bfcfcb42dfb5f	addColumn tableName=RESOURCE_SERVER		\N	4.8.0	\N	\N	3471531609
8.0.0-adding-credential-columns	keycloak	META-INF/jpa-changelog-8.0.0.xml	2022-05-25 09:38:52.412947	72	EXECUTED	9:aa072ad090bbba210d8f18781b8cebf4	addColumn tableName=CREDENTIAL; addColumn tableName=FED_USER_CREDENTIAL		\N	4.8.0	\N	\N	3471531609
8.0.0-updating-credential-data-not-oracle-fixed	keycloak	META-INF/jpa-changelog-8.0.0.xml	2022-05-25 09:38:52.416357	73	EXECUTED	9:1ae6be29bab7c2aa376f6983b932be37	update tableName=CREDENTIAL; update tableName=CREDENTIAL; update tableName=CREDENTIAL; update tableName=FED_USER_CREDENTIAL; update tableName=FED_USER_CREDENTIAL; update tableName=FED_USER_CREDENTIAL		\N	4.8.0	\N	\N	3471531609
8.0.0-updating-credential-data-oracle-fixed	keycloak	META-INF/jpa-changelog-8.0.0.xml	2022-05-25 09:38:52.417482	74	MARK_RAN	9:14706f286953fc9a25286dbd8fb30d97	update tableName=CREDENTIAL; update tableName=CREDENTIAL; update tableName=CREDENTIAL; update tableName=FED_USER_CREDENTIAL; update tableName=FED_USER_CREDENTIAL; update tableName=FED_USER_CREDENTIAL		\N	4.8.0	\N	\N	3471531609
8.0.0-credential-cleanup-fixed	keycloak	META-INF/jpa-changelog-8.0.0.xml	2022-05-25 09:38:52.427516	75	EXECUTED	9:2b9cc12779be32c5b40e2e67711a218b	dropDefaultValue columnName=COUNTER, tableName=CREDENTIAL; dropDefaultValue columnName=DIGITS, tableName=CREDENTIAL; dropDefaultValue columnName=PERIOD, tableName=CREDENTIAL; dropDefaultValue columnName=ALGORITHM, tableName=CREDENTIAL; dropColumn ...		\N	4.8.0	\N	\N	3471531609
8.0.0-resource-tag-support	keycloak	META-INF/jpa-changelog-8.0.0.xml	2022-05-25 09:38:52.430746	76	EXECUTED	9:91fa186ce7a5af127a2d7a91ee083cc5	addColumn tableName=MIGRATION_MODEL; createIndex indexName=IDX_UPDATE_TIME, tableName=MIGRATION_MODEL		\N	4.8.0	\N	\N	3471531609
9.0.0-always-display-client	keycloak	META-INF/jpa-changelog-9.0.0.xml	2022-05-25 09:38:52.432689	77	EXECUTED	9:6335e5c94e83a2639ccd68dd24e2e5ad	addColumn tableName=CLIENT		\N	4.8.0	\N	\N	3471531609
9.0.0-drop-constraints-for-column-increase	keycloak	META-INF/jpa-changelog-9.0.0.xml	2022-05-25 09:38:52.433757	78	MARK_RAN	9:6bdb5658951e028bfe16fa0a8228b530	dropUniqueConstraint constraintName=UK_FRSR6T700S9V50BU18WS5PMT, tableName=RESOURCE_SERVER_PERM_TICKET; dropUniqueConstraint constraintName=UK_FRSR6T700S9V50BU18WS5HA6, tableName=RESOURCE_SERVER_RESOURCE; dropPrimaryKey constraintName=CONSTRAINT_O...		\N	4.8.0	\N	\N	3471531609
9.0.0-increase-column-size-federated-fk	keycloak	META-INF/jpa-changelog-9.0.0.xml	2022-05-25 09:38:52.442005	79	EXECUTED	9:d5bc15a64117ccad481ce8792d4c608f	modifyDataType columnName=CLIENT_ID, tableName=FED_USER_CONSENT; modifyDataType columnName=CLIENT_REALM_CONSTRAINT, tableName=KEYCLOAK_ROLE; modifyDataType columnName=OWNER, tableName=RESOURCE_SERVER_POLICY; modifyDataType columnName=CLIENT_ID, ta...		\N	4.8.0	\N	\N	3471531609
9.0.0-recreate-constraints-after-column-increase	keycloak	META-INF/jpa-changelog-9.0.0.xml	2022-05-25 09:38:52.443159	80	MARK_RAN	9:077cba51999515f4d3e7ad5619ab592c	addNotNullConstraint columnName=CLIENT_ID, tableName=OFFLINE_CLIENT_SESSION; addNotNullConstraint columnName=OWNER, tableName=RESOURCE_SERVER_PERM_TICKET; addNotNullConstraint columnName=REQUESTER, tableName=RESOURCE_SERVER_PERM_TICKET; addNotNull...		\N	4.8.0	\N	\N	3471531609
9.0.1-add-index-to-client.client_id	keycloak	META-INF/jpa-changelog-9.0.1.xml	2022-05-25 09:38:52.445843	81	EXECUTED	9:be969f08a163bf47c6b9e9ead8ac2afb	createIndex indexName=IDX_CLIENT_ID, tableName=CLIENT		\N	4.8.0	\N	\N	3471531609
9.0.1-KEYCLOAK-12579-drop-constraints	keycloak	META-INF/jpa-changelog-9.0.1.xml	2022-05-25 09:38:52.446938	82	MARK_RAN	9:6d3bb4408ba5a72f39bd8a0b301ec6e3	dropUniqueConstraint constraintName=SIBLING_NAMES, tableName=KEYCLOAK_GROUP		\N	4.8.0	\N	\N	3471531609
9.0.1-KEYCLOAK-12579-add-not-null-constraint	keycloak	META-INF/jpa-changelog-9.0.1.xml	2022-05-25 09:38:52.449019	83	EXECUTED	9:966bda61e46bebf3cc39518fbed52fa7	addNotNullConstraint columnName=PARENT_GROUP, tableName=KEYCLOAK_GROUP		\N	4.8.0	\N	\N	3471531609
9.0.1-KEYCLOAK-12579-recreate-constraints	keycloak	META-INF/jpa-changelog-9.0.1.xml	2022-05-25 09:38:52.450055	84	MARK_RAN	9:8dcac7bdf7378e7d823cdfddebf72fda	addUniqueConstraint constraintName=SIBLING_NAMES, tableName=KEYCLOAK_GROUP		\N	4.8.0	\N	\N	3471531609
9.0.1-add-index-to-events	keycloak	META-INF/jpa-changelog-9.0.1.xml	2022-05-25 09:38:52.452596	85	EXECUTED	9:7d93d602352a30c0c317e6a609b56599	createIndex indexName=IDX_EVENT_TIME, tableName=EVENT_ENTITY		\N	4.8.0	\N	\N	3471531609
map-remove-ri	keycloak	META-INF/jpa-changelog-11.0.0.xml	2022-05-25 09:38:52.454956	86	EXECUTED	9:71c5969e6cdd8d7b6f47cebc86d37627	dropForeignKeyConstraint baseTableName=REALM, constraintName=FK_TRAF444KK6QRKMS7N56AIWQ5Y; dropForeignKeyConstraint baseTableName=KEYCLOAK_ROLE, constraintName=FK_KJHO5LE2C0RAL09FL8CM9WFW9		\N	4.8.0	\N	\N	3471531609
map-remove-ri	keycloak	META-INF/jpa-changelog-12.0.0.xml	2022-05-25 09:38:52.458708	87	EXECUTED	9:a9ba7d47f065f041b7da856a81762021	dropForeignKeyConstraint baseTableName=REALM_DEFAULT_GROUPS, constraintName=FK_DEF_GROUPS_GROUP; dropForeignKeyConstraint baseTableName=REALM_DEFAULT_ROLES, constraintName=FK_H4WPD7W4HSOOLNI3H0SW7BTJE; dropForeignKeyConstraint baseTableName=CLIENT...		\N	4.8.0	\N	\N	3471531609
12.1.0-add-realm-localization-table	keycloak	META-INF/jpa-changelog-12.0.0.xml	2022-05-25 09:38:52.462843	88	EXECUTED	9:fffabce2bc01e1a8f5110d5278500065	createTable tableName=REALM_LOCALIZATIONS; addPrimaryKey tableName=REALM_LOCALIZATIONS		\N	4.8.0	\N	\N	3471531609
default-roles	keycloak	META-INF/jpa-changelog-13.0.0.xml	2022-05-25 09:38:52.466146	89	EXECUTED	9:fa8a5b5445e3857f4b010bafb5009957	addColumn tableName=REALM; customChange		\N	4.8.0	\N	\N	3471531609
default-roles-cleanup	keycloak	META-INF/jpa-changelog-13.0.0.xml	2022-05-25 09:38:52.468916	90	EXECUTED	9:67ac3241df9a8582d591c5ed87125f39	dropTable tableName=REALM_DEFAULT_ROLES; dropTable tableName=CLIENT_DEFAULT_ROLES		\N	4.8.0	\N	\N	3471531609
13.0.0-KEYCLOAK-16844	keycloak	META-INF/jpa-changelog-13.0.0.xml	2022-05-25 09:38:52.471588	91	EXECUTED	9:ad1194d66c937e3ffc82386c050ba089	createIndex indexName=IDX_OFFLINE_USS_PRELOAD, tableName=OFFLINE_USER_SESSION		\N	4.8.0	\N	\N	3471531609
map-remove-ri-13.0.0	keycloak	META-INF/jpa-changelog-13.0.0.xml	2022-05-25 09:38:52.475471	92	EXECUTED	9:d9be619d94af5a2f5d07b9f003543b91	dropForeignKeyConstraint baseTableName=DEFAULT_CLIENT_SCOPE, constraintName=FK_R_DEF_CLI_SCOPE_SCOPE; dropForeignKeyConstraint baseTableName=CLIENT_SCOPE_CLIENT, constraintName=FK_C_CLI_SCOPE_SCOPE; dropForeignKeyConstraint baseTableName=CLIENT_SC...		\N	4.8.0	\N	\N	3471531609
13.0.0-KEYCLOAK-17992-drop-constraints	keycloak	META-INF/jpa-changelog-13.0.0.xml	2022-05-25 09:38:52.476572	93	MARK_RAN	9:544d201116a0fcc5a5da0925fbbc3bde	dropPrimaryKey constraintName=C_CLI_SCOPE_BIND, tableName=CLIENT_SCOPE_CLIENT; dropIndex indexName=IDX_CLSCOPE_CL, tableName=CLIENT_SCOPE_CLIENT; dropIndex indexName=IDX_CL_CLSCOPE, tableName=CLIENT_SCOPE_CLIENT		\N	4.8.0	\N	\N	3471531609
13.0.0-KEYCLOAK-17992-recreate-constraints	keycloak	META-INF/jpa-changelog-13.0.0.xml	2022-05-25 09:38:52.481556	95	MARK_RAN	9:8bd711fd0330f4fe980494ca43ab1139	addNotNullConstraint columnName=CLIENT_ID, tableName=CLIENT_SCOPE_CLIENT; addNotNullConstraint columnName=SCOPE_ID, tableName=CLIENT_SCOPE_CLIENT; addPrimaryKey constraintName=C_CLI_SCOPE_BIND, tableName=CLIENT_SCOPE_CLIENT; createIndex indexName=...		\N	4.8.0	\N	\N	3471531609
json-string-accomodation-fixed	keycloak	META-INF/jpa-changelog-13.0.0.xml	2022-05-25 09:38:52.484296	96	EXECUTED	9:e07d2bc0970c348bb06fb63b1f82ddbf	addColumn tableName=REALM_ATTRIBUTE; update tableName=REALM_ATTRIBUTE; dropColumn columnName=VALUE, tableName=REALM_ATTRIBUTE; renameColumn newColumnName=VALUE, oldColumnName=VALUE_NEW, tableName=REALM_ATTRIBUTE		\N	4.8.0	\N	\N	3471531609
14.0.0-KEYCLOAK-11019	keycloak	META-INF/jpa-changelog-14.0.0.xml	2022-05-25 09:38:52.489103	97	EXECUTED	9:24fb8611e97f29989bea412aa38d12b7	createIndex indexName=IDX_OFFLINE_CSS_PRELOAD, tableName=OFFLINE_CLIENT_SESSION; createIndex indexName=IDX_OFFLINE_USS_BY_USER, tableName=OFFLINE_USER_SESSION; createIndex indexName=IDX_OFFLINE_USS_BY_USERSESS, tableName=OFFLINE_USER_SESSION		\N	4.8.0	\N	\N	3471531609
14.0.0-KEYCLOAK-18286	keycloak	META-INF/jpa-changelog-14.0.0.xml	2022-05-25 09:38:52.490345	98	MARK_RAN	9:259f89014ce2506ee84740cbf7163aa7	createIndex indexName=IDX_CLIENT_ATT_BY_NAME_VALUE, tableName=CLIENT_ATTRIBUTES		\N	4.8.0	\N	\N	3471531609
14.0.0-KEYCLOAK-18286-revert	keycloak	META-INF/jpa-changelog-14.0.0.xml	2022-05-25 09:38:52.496942	99	MARK_RAN	9:04baaf56c116ed19951cbc2cca584022	dropIndex indexName=IDX_CLIENT_ATT_BY_NAME_VALUE, tableName=CLIENT_ATTRIBUTES		\N	4.8.0	\N	\N	3471531609
14.0.0-KEYCLOAK-18286-supported-dbs	keycloak	META-INF/jpa-changelog-14.0.0.xml	2022-05-25 09:38:52.499989	100	EXECUTED	9:60ca84a0f8c94ec8c3504a5a3bc88ee8	createIndex indexName=IDX_CLIENT_ATT_BY_NAME_VALUE, tableName=CLIENT_ATTRIBUTES		\N	4.8.0	\N	\N	3471531609
14.0.0-KEYCLOAK-18286-unsupported-dbs	keycloak	META-INF/jpa-changelog-14.0.0.xml	2022-05-25 09:38:52.501255	101	MARK_RAN	9:d3d977031d431db16e2c181ce49d73e9	createIndex indexName=IDX_CLIENT_ATT_BY_NAME_VALUE, tableName=CLIENT_ATTRIBUTES		\N	4.8.0	\N	\N	3471531609
KEYCLOAK-17267-add-index-to-user-attributes	keycloak	META-INF/jpa-changelog-14.0.0.xml	2022-05-25 09:38:52.504055	102	EXECUTED	9:0b305d8d1277f3a89a0a53a659ad274c	createIndex indexName=IDX_USER_ATTRIBUTE_NAME, tableName=USER_ATTRIBUTE		\N	4.8.0	\N	\N	3471531609
KEYCLOAK-18146-add-saml-art-binding-identifier	keycloak	META-INF/jpa-changelog-14.0.0.xml	2022-05-25 09:38:52.507198	103	EXECUTED	9:2c374ad2cdfe20e2905a84c8fac48460	customChange		\N	4.8.0	\N	\N	3471531609
15.0.0-KEYCLOAK-18467	keycloak	META-INF/jpa-changelog-15.0.0.xml	2022-05-25 09:38:52.510347	104	EXECUTED	9:47a760639ac597360a8219f5b768b4de	addColumn tableName=REALM_LOCALIZATIONS; update tableName=REALM_LOCALIZATIONS; dropColumn columnName=TEXTS, tableName=REALM_LOCALIZATIONS; renameColumn newColumnName=TEXTS, oldColumnName=TEXTS_NEW, tableName=REALM_LOCALIZATIONS; addNotNullConstrai...		\N	4.8.0	\N	\N	3471531609
17.0.0-9562	keycloak	META-INF/jpa-changelog-17.0.0.xml	2022-05-25 09:38:52.513025	105	EXECUTED	9:a6272f0576727dd8cad2522335f5d99e	createIndex indexName=IDX_USER_SERVICE_ACCOUNT, tableName=USER_ENTITY		\N	4.8.0	\N	\N	3471531609
20.0.0-12964-supported-dbs	keycloak	META-INF/jpa-changelog-20.0.0.xml	2024-11-27 17:11:49.156168	109	EXECUTED	9:e5f243877199fd96bcc842f27a1656ac	createIndex indexName=IDX_GROUP_ATT_BY_NAME_VALUE, tableName=GROUP_ATTRIBUTE		\N	4.29.1	\N	\N	2727508830
20.0.0-12964-unsupported-dbs	keycloak	META-INF/jpa-changelog-20.0.0.xml	2024-11-27 17:11:49.158432	110	MARK_RAN	9:1a6fcaa85e20bdeae0a9ce49b41946a5	createIndex indexName=IDX_GROUP_ATT_BY_NAME_VALUE, tableName=GROUP_ATTRIBUTE		\N	4.29.1	\N	\N	2727508830
client-attributes-string-accomodation-fixed	keycloak	META-INF/jpa-changelog-20.0.0.xml	2024-11-27 17:11:49.167542	111	EXECUTED	9:3f332e13e90739ed0c35b0b25b7822ca	addColumn tableName=CLIENT_ATTRIBUTES; update tableName=CLIENT_ATTRIBUTES; dropColumn columnName=VALUE, tableName=CLIENT_ATTRIBUTES; renameColumn newColumnName=VALUE, oldColumnName=VALUE_NEW, tableName=CLIENT_ATTRIBUTES		\N	4.29.1	\N	\N	2727508830
21.0.2-17277	keycloak	META-INF/jpa-changelog-21.0.2.xml	2024-11-27 17:11:49.174244	112	EXECUTED	9:7ee1f7a3fb8f5588f171fb9a6ab623c0	customChange		\N	4.29.1	\N	\N	2727508830
21.1.0-19404	keycloak	META-INF/jpa-changelog-21.1.0.xml	2024-11-27 17:11:49.196158	113	EXECUTED	9:3d7e830b52f33676b9d64f7f2b2ea634	modifyDataType columnName=DECISION_STRATEGY, tableName=RESOURCE_SERVER_POLICY; modifyDataType columnName=LOGIC, tableName=RESOURCE_SERVER_POLICY; modifyDataType columnName=POLICY_ENFORCE_MODE, tableName=RESOURCE_SERVER		\N	4.29.1	\N	\N	2727508830
21.1.0-19404-2	keycloak	META-INF/jpa-changelog-21.1.0.xml	2024-11-27 17:11:49.197732	114	MARK_RAN	9:627d032e3ef2c06c0e1f73d2ae25c26c	addColumn tableName=RESOURCE_SERVER_POLICY; update tableName=RESOURCE_SERVER_POLICY; dropColumn columnName=DECISION_STRATEGY, tableName=RESOURCE_SERVER_POLICY; renameColumn newColumnName=DECISION_STRATEGY, oldColumnName=DECISION_STRATEGY_NEW, tabl...		\N	4.29.1	\N	\N	2727508830
22.0.0-17484-updated	keycloak	META-INF/jpa-changelog-22.0.0.xml	2024-11-27 17:11:49.202597	115	EXECUTED	9:90af0bfd30cafc17b9f4d6eccd92b8b3	customChange		\N	4.29.1	\N	\N	2727508830
22.0.5-24031	keycloak	META-INF/jpa-changelog-22.0.0.xml	2024-11-27 17:11:49.203483	116	MARK_RAN	9:a60d2d7b315ec2d3eba9e2f145f9df28	customChange		\N	4.29.1	\N	\N	2727508830
23.0.0-12062	keycloak	META-INF/jpa-changelog-23.0.0.xml	2024-11-27 17:11:49.210206	117	EXECUTED	9:2168fbe728fec46ae9baf15bf80927b8	addColumn tableName=COMPONENT_CONFIG; update tableName=COMPONENT_CONFIG; dropColumn columnName=VALUE, tableName=COMPONENT_CONFIG; renameColumn newColumnName=VALUE, oldColumnName=VALUE_NEW, tableName=COMPONENT_CONFIG		\N	4.29.1	\N	\N	2727508830
23.0.0-17258	keycloak	META-INF/jpa-changelog-23.0.0.xml	2024-11-27 17:11:49.212432	118	EXECUTED	9:36506d679a83bbfda85a27ea1864dca8	addColumn tableName=EVENT_ENTITY		\N	4.29.1	\N	\N	2727508830
24.0.0-9758	keycloak	META-INF/jpa-changelog-24.0.0.xml	2024-11-27 17:11:49.367873	119	EXECUTED	9:502c557a5189f600f0f445a9b49ebbce	addColumn tableName=USER_ATTRIBUTE; addColumn tableName=FED_USER_ATTRIBUTE; createIndex indexName=USER_ATTR_LONG_VALUES, tableName=USER_ATTRIBUTE; createIndex indexName=FED_USER_ATTR_LONG_VALUES, tableName=FED_USER_ATTRIBUTE; createIndex indexName...		\N	4.29.1	\N	\N	2727508830
24.0.0-9758-2	keycloak	META-INF/jpa-changelog-24.0.0.xml	2024-11-27 17:11:49.371256	120	EXECUTED	9:bf0fdee10afdf597a987adbf291db7b2	customChange		\N	4.29.1	\N	\N	2727508830
24.0.0-26618-drop-index-if-present	keycloak	META-INF/jpa-changelog-24.0.0.xml	2024-11-27 17:11:49.374589	121	MARK_RAN	9:04baaf56c116ed19951cbc2cca584022	dropIndex indexName=IDX_CLIENT_ATT_BY_NAME_VALUE, tableName=CLIENT_ATTRIBUTES		\N	4.29.1	\N	\N	2727508830
24.0.0-26618-reindex	keycloak	META-INF/jpa-changelog-24.0.0.xml	2024-11-27 17:11:49.405324	122	EXECUTED	9:08707c0f0db1cef6b352db03a60edc7f	createIndex indexName=IDX_CLIENT_ATT_BY_NAME_VALUE, tableName=CLIENT_ATTRIBUTES		\N	4.29.1	\N	\N	2727508830
24.0.2-27228	keycloak	META-INF/jpa-changelog-24.0.2.xml	2024-11-27 17:11:49.409562	123	EXECUTED	9:eaee11f6b8aa25d2cc6a84fb86fc6238	customChange		\N	4.29.1	\N	\N	2727508830
24.0.2-27967-drop-index-if-present	keycloak	META-INF/jpa-changelog-24.0.2.xml	2024-11-27 17:11:49.410465	124	MARK_RAN	9:04baaf56c116ed19951cbc2cca584022	dropIndex indexName=IDX_CLIENT_ATT_BY_NAME_VALUE, tableName=CLIENT_ATTRIBUTES		\N	4.29.1	\N	\N	2727508830
24.0.2-27967-reindex	keycloak	META-INF/jpa-changelog-24.0.2.xml	2024-11-27 17:11:49.411695	125	MARK_RAN	9:d3d977031d431db16e2c181ce49d73e9	createIndex indexName=IDX_CLIENT_ATT_BY_NAME_VALUE, tableName=CLIENT_ATTRIBUTES		\N	4.29.1	\N	\N	2727508830
25.0.0-28265-tables	keycloak	META-INF/jpa-changelog-25.0.0.xml	2024-11-27 17:11:49.418932	126	EXECUTED	9:deda2df035df23388af95bbd36c17cef	addColumn tableName=OFFLINE_USER_SESSION; addColumn tableName=OFFLINE_CLIENT_SESSION		\N	4.29.1	\N	\N	2727508830
25.0.0-28265-index-creation	keycloak	META-INF/jpa-changelog-25.0.0.xml	2024-11-27 17:11:49.446687	127	EXECUTED	9:3e96709818458ae49f3c679ae58d263a	createIndex indexName=IDX_OFFLINE_USS_BY_LAST_SESSION_REFRESH, tableName=OFFLINE_USER_SESSION		\N	4.29.1	\N	\N	2727508830
25.0.0-28265-index-cleanup	keycloak	META-INF/jpa-changelog-25.0.0.xml	2024-11-27 17:11:49.450806	128	EXECUTED	9:8c0cfa341a0474385b324f5c4b2dfcc1	dropIndex indexName=IDX_OFFLINE_USS_CREATEDON, tableName=OFFLINE_USER_SESSION; dropIndex indexName=IDX_OFFLINE_USS_PRELOAD, tableName=OFFLINE_USER_SESSION; dropIndex indexName=IDX_OFFLINE_USS_BY_USERSESS, tableName=OFFLINE_USER_SESSION; dropIndex ...		\N	4.29.1	\N	\N	2727508830
25.0.0-28265-index-2-mysql	keycloak	META-INF/jpa-changelog-25.0.0.xml	2024-11-27 17:11:49.451983	129	MARK_RAN	9:b7ef76036d3126bb83c2423bf4d449d6	createIndex indexName=IDX_OFFLINE_USS_BY_BROKER_SESSION_ID, tableName=OFFLINE_USER_SESSION		\N	4.29.1	\N	\N	2727508830
25.0.0-28265-index-2-not-mysql	keycloak	META-INF/jpa-changelog-25.0.0.xml	2024-11-27 17:11:49.471354	130	EXECUTED	9:23396cf51ab8bc1ae6f0cac7f9f6fcf7	createIndex indexName=IDX_OFFLINE_USS_BY_BROKER_SESSION_ID, tableName=OFFLINE_USER_SESSION		\N	4.29.1	\N	\N	2727508830
25.0.0-org	keycloak	META-INF/jpa-changelog-25.0.0.xml	2024-11-27 17:11:49.487171	131	EXECUTED	9:5c859965c2c9b9c72136c360649af157	createTable tableName=ORG; addUniqueConstraint constraintName=UK_ORG_NAME, tableName=ORG; addUniqueConstraint constraintName=UK_ORG_GROUP, tableName=ORG; createTable tableName=ORG_DOMAIN		\N	4.29.1	\N	\N	2727508830
unique-consentuser	keycloak	META-INF/jpa-changelog-25.0.0.xml	2024-11-27 17:11:49.497686	132	EXECUTED	9:5857626a2ea8767e9a6c66bf3a2cb32f	customChange; dropUniqueConstraint constraintName=UK_JKUWUVD56ONTGSUHOGM8UEWRT, tableName=USER_CONSENT; addUniqueConstraint constraintName=UK_LOCAL_CONSENT, tableName=USER_CONSENT; addUniqueConstraint constraintName=UK_EXTERNAL_CONSENT, tableName=...		\N	4.29.1	\N	\N	2727508830
unique-consentuser-mysql	keycloak	META-INF/jpa-changelog-25.0.0.xml	2024-11-27 17:11:49.498596	133	MARK_RAN	9:b79478aad5adaa1bc428e31563f55e8e	customChange; dropUniqueConstraint constraintName=UK_JKUWUVD56ONTGSUHOGM8UEWRT, tableName=USER_CONSENT; addUniqueConstraint constraintName=UK_LOCAL_CONSENT, tableName=USER_CONSENT; addUniqueConstraint constraintName=UK_EXTERNAL_CONSENT, tableName=...		\N	4.29.1	\N	\N	2727508830
25.0.0-28861-index-creation	keycloak	META-INF/jpa-changelog-25.0.0.xml	2024-11-27 17:11:49.547745	134	EXECUTED	9:b9acb58ac958d9ada0fe12a5d4794ab1	createIndex indexName=IDX_PERM_TICKET_REQUESTER, tableName=RESOURCE_SERVER_PERM_TICKET; createIndex indexName=IDX_PERM_TICKET_OWNER, tableName=RESOURCE_SERVER_PERM_TICKET		\N	4.29.1	\N	\N	2727508830
26.0.0-org-alias	keycloak	META-INF/jpa-changelog-26.0.0.xml	2024-11-27 17:11:49.554766	135	EXECUTED	9:6ef7d63e4412b3c2d66ed179159886a4	addColumn tableName=ORG; update tableName=ORG; addNotNullConstraint columnName=ALIAS, tableName=ORG; addUniqueConstraint constraintName=UK_ORG_ALIAS, tableName=ORG		\N	4.29.1	\N	\N	2727508830
26.0.0-org-group	keycloak	META-INF/jpa-changelog-26.0.0.xml	2024-11-27 17:11:49.562345	136	EXECUTED	9:da8e8087d80ef2ace4f89d8c5b9ca223	addColumn tableName=KEYCLOAK_GROUP; update tableName=KEYCLOAK_GROUP; addNotNullConstraint columnName=TYPE, tableName=KEYCLOAK_GROUP; customChange		\N	4.29.1	\N	\N	2727508830
26.0.0-org-indexes	keycloak	META-INF/jpa-changelog-26.0.0.xml	2024-11-27 17:11:49.595461	137	EXECUTED	9:79b05dcd610a8c7f25ec05135eec0857	createIndex indexName=IDX_ORG_DOMAIN_ORG_ID, tableName=ORG_DOMAIN		\N	4.29.1	\N	\N	2727508830
26.0.0-org-group-membership	keycloak	META-INF/jpa-changelog-26.0.0.xml	2024-11-27 17:11:49.599307	138	EXECUTED	9:a6ace2ce583a421d89b01ba2a28dc2d4	addColumn tableName=USER_GROUP_MEMBERSHIP; update tableName=USER_GROUP_MEMBERSHIP; addNotNullConstraint columnName=MEMBERSHIP_TYPE, tableName=USER_GROUP_MEMBERSHIP		\N	4.29.1	\N	\N	2727508830
31296-persist-revoked-access-tokens	keycloak	META-INF/jpa-changelog-26.0.0.xml	2024-11-27 17:11:49.604844	139	EXECUTED	9:64ef94489d42a358e8304b0e245f0ed4	createTable tableName=REVOKED_TOKEN; addPrimaryKey constraintName=CONSTRAINT_RT, tableName=REVOKED_TOKEN		\N	4.29.1	\N	\N	2727508830
31725-index-persist-revoked-access-tokens	keycloak	META-INF/jpa-changelog-26.0.0.xml	2024-11-27 17:11:49.632071	140	EXECUTED	9:b994246ec2bf7c94da881e1d28782c7b	createIndex indexName=IDX_REV_TOKEN_ON_EXPIRE, tableName=REVOKED_TOKEN		\N	4.29.1	\N	\N	2727508830
26.0.0-idps-for-login	keycloak	META-INF/jpa-changelog-26.0.0.xml	2024-11-27 17:11:49.700846	141	EXECUTED	9:51f5fffadf986983d4bd59582c6c1604	addColumn tableName=IDENTITY_PROVIDER; createIndex indexName=IDX_IDP_REALM_ORG, tableName=IDENTITY_PROVIDER; createIndex indexName=IDX_IDP_FOR_LOGIN, tableName=IDENTITY_PROVIDER; customChange		\N	4.29.1	\N	\N	2727508830
26.0.0-32583-drop-redundant-index-on-client-session	keycloak	META-INF/jpa-changelog-26.0.0.xml	2024-11-27 17:11:49.743551	142	EXECUTED	9:24972d83bf27317a055d234187bb4af9	dropIndex indexName=IDX_US_SESS_ID_ON_CL_SESS, tableName=OFFLINE_CLIENT_SESSION		\N	4.29.1	\N	\N	2727508830
26.0.0.32582-remove-tables-user-session-user-session-note-and-client-session	keycloak	META-INF/jpa-changelog-26.0.0.xml	2024-11-27 17:11:49.764761	143	EXECUTED	9:febdc0f47f2ed241c59e60f58c3ceea5	dropTable tableName=CLIENT_SESSION_ROLE; dropTable tableName=CLIENT_SESSION_NOTE; dropTable tableName=CLIENT_SESSION_PROT_MAPPER; dropTable tableName=CLIENT_SESSION_AUTH_STATUS; dropTable tableName=CLIENT_USER_SESSION_NOTE; dropTable tableName=CLI...		\N	4.29.1	\N	\N	2727508830
26.0.0-33201-org-redirect-url	keycloak	META-INF/jpa-changelog-26.0.0.xml	2024-11-27 17:11:49.767194	144	EXECUTED	9:4d0e22b0ac68ebe9794fa9cb752ea660	addColumn tableName=ORG		\N	4.29.1	\N	\N	2727508830
25.0.0-28265-index-cleanup-uss-createdon	keycloak	META-INF/jpa-changelog-25.0.0.xml	2024-12-03 10:49:17.668201	145	MARK_RAN	9:78ab4fc129ed5e8265dbcc3485fba92f	dropIndex indexName=IDX_OFFLINE_USS_CREATEDON, tableName=OFFLINE_USER_SESSION		\N	4.29.1	\N	\N	3222957648
25.0.0-28265-index-cleanup-uss-preload	keycloak	META-INF/jpa-changelog-25.0.0.xml	2024-12-03 10:49:17.684573	146	MARK_RAN	9:de5f7c1f7e10994ed8b62e621d20eaab	dropIndex indexName=IDX_OFFLINE_USS_PRELOAD, tableName=OFFLINE_USER_SESSION		\N	4.29.1	\N	\N	3222957648
25.0.0-28265-index-cleanup-uss-by-usersess	keycloak	META-INF/jpa-changelog-25.0.0.xml	2024-12-03 10:49:17.69018	147	MARK_RAN	9:6eee220d024e38e89c799417ec33667f	dropIndex indexName=IDX_OFFLINE_USS_BY_USERSESS, tableName=OFFLINE_USER_SESSION		\N	4.29.1	\N	\N	3222957648
25.0.0-28265-index-cleanup-css-preload	keycloak	META-INF/jpa-changelog-25.0.0.xml	2024-12-03 10:49:17.695231	148	MARK_RAN	9:5411d2fb2891d3e8d63ddb55dfa3c0c9	dropIndex indexName=IDX_OFFLINE_CSS_PRELOAD, tableName=OFFLINE_CLIENT_SESSION		\N	4.29.1	\N	\N	3222957648
26.0.6-34013	keycloak	META-INF/jpa-changelog-26.0.6.xml	2024-12-03 10:49:17.701268	149	EXECUTED	9:e6b686a15759aef99a6d758a5c4c6a26	addColumn tableName=ADMIN_EVENT_ENTITY		\N	4.29.1	\N	\N	3222957648
\.


--
-- Data for Name: databasechangeloglock; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.databasechangeloglock (id, locked, lockgranted, lockedby) FROM stdin;
1	f	\N	\N
1000	f	\N	\N
1001	f	\N	\N
\.


--
-- Data for Name: default_client_scope; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.default_client_scope (realm_id, scope_id, default_scope) FROM stdin;
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	f26177d9-ab49-449c-bbc7-050c6ea05305	f
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	c5728f88-7d82-4a3f-95b0-e8394aa20f74	t
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	b7fa698d-506e-4edd-ba59-92d1330019f7	t
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	57b95975-6d57-4d7a-86bd-3912d154d522	t
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	de577a7b-5939-44fa-aa0f-d0b50991435b	f
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	23331e70-04a9-4ee0-a188-49a8636c01b8	f
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	d4ea9c2f-7ae0-4297-8743-e5314bb8e811	t
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	bf7a8b48-9bfb-4612-a643-8d7c01c5ae91	t
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	180ede63-bd73-4832-b8aa-f930d57263d5	f
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	b5a5d94a-d9d0-4dde-ad70-7b629561ad61	t
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	6f3da124-ce69-4e6c-b933-35dfc3e58221	t
3a9fd404-d708-4128-83e7-ef00fa943752	c9b2c9be-e9f9-4eda-847a-b8f20265ed48	f
3a9fd404-d708-4128-83e7-ef00fa943752	59352ece-9aad-4086-ac75-eb145c579d2d	t
3a9fd404-d708-4128-83e7-ef00fa943752	50169b72-4790-45a2-9a55-1f1797bc287b	t
3a9fd404-d708-4128-83e7-ef00fa943752	3809229d-762a-41ad-b8da-9175829b87e4	t
3a9fd404-d708-4128-83e7-ef00fa943752	748acf76-9899-4a7c-b222-7a4bd53d21a1	t
3a9fd404-d708-4128-83e7-ef00fa943752	e54f533d-3819-4345-9502-8c3b57ebb6a6	f
3a9fd404-d708-4128-83e7-ef00fa943752	5b4a9626-9522-4fdf-89fc-6abe5538a593	f
3a9fd404-d708-4128-83e7-ef00fa943752	5c60430f-7841-4c66-acb4-0d372753c861	t
3a9fd404-d708-4128-83e7-ef00fa943752	d7a399be-d14a-43bf-9a5a-cde8802b9168	t
3a9fd404-d708-4128-83e7-ef00fa943752	dc3fd5f4-803e-4df7-ab4b-56b6a0df379c	f
3a9fd404-d708-4128-83e7-ef00fa943752	9058dc48-9336-4dac-95ec-81095b2bf8b9	t
3a9fd404-d708-4128-83e7-ef00fa943752	3583a32f-b84b-4d85-a79d-0ee871e335dc	t
3a9fd404-d708-4128-83e7-ef00fa943752	880f5c51-7209-4262-9a6a-543895c0075f	f
\.


--
-- Data for Name: event_entity; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.event_entity (id, client_id, details_json, error, ip_address, realm_id, session_id, event_time, type, user_id, details_json_long_value) FROM stdin;
\.


--
-- Data for Name: fed_user_attribute; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.fed_user_attribute (id, name, user_id, realm_id, storage_provider_id, value, long_value_hash, long_value_hash_lower_case, long_value) FROM stdin;
\.


--
-- Data for Name: fed_user_consent; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.fed_user_consent (id, client_id, user_id, realm_id, storage_provider_id, created_date, last_updated_date, client_storage_provider, external_client_id) FROM stdin;
\.


--
-- Data for Name: fed_user_consent_cl_scope; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.fed_user_consent_cl_scope (user_consent_id, scope_id) FROM stdin;
\.


--
-- Data for Name: fed_user_credential; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.fed_user_credential (id, salt, type, created_date, user_id, realm_id, storage_provider_id, user_label, secret_data, credential_data, priority) FROM stdin;
\.


--
-- Data for Name: fed_user_group_membership; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.fed_user_group_membership (group_id, user_id, realm_id, storage_provider_id) FROM stdin;
\.


--
-- Data for Name: fed_user_required_action; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.fed_user_required_action (required_action, user_id, realm_id, storage_provider_id) FROM stdin;
\.


--
-- Data for Name: fed_user_role_mapping; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.fed_user_role_mapping (role_id, user_id, realm_id, storage_provider_id) FROM stdin;
\.


--
-- Data for Name: federated_identity; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.federated_identity (identity_provider, realm_id, federated_user_id, federated_username, token, user_id) FROM stdin;
\.


--
-- Data for Name: federated_user; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.federated_user (id, storage_provider_id, realm_id) FROM stdin;
\.


--
-- Data for Name: group_attribute; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.group_attribute (id, name, value, group_id) FROM stdin;
\.


--
-- Data for Name: group_role_mapping; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.group_role_mapping (role_id, group_id) FROM stdin;
\.


--
-- Data for Name: identity_provider; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.identity_provider (internal_id, enabled, provider_alias, provider_id, store_token, authenticate_by_default, realm_id, add_token_role, trust_email, first_broker_login_flow_id, post_broker_login_flow_id, provider_display_name, link_only, organization_id, hide_on_login) FROM stdin;
\.


--
-- Data for Name: identity_provider_config; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.identity_provider_config (identity_provider_id, value, name) FROM stdin;
\.


--
-- Data for Name: identity_provider_mapper; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.identity_provider_mapper (id, name, idp_alias, idp_mapper_name, realm_id) FROM stdin;
\.


--
-- Data for Name: idp_mapper_config; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.idp_mapper_config (idp_mapper_id, value, name) FROM stdin;
\.


--
-- Data for Name: keycloak_group; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.keycloak_group (id, name, parent_group, realm_id, type) FROM stdin;
55488dd2-11e5-474e-a2d3-63fa4f98b19a	test-group	 	3a9fd404-d708-4128-83e7-ef00fa943752	0
\.


--
-- Data for Name: keycloak_role; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.keycloak_role (id, client_realm_constraint, client_role, description, name, realm_id, client, realm) FROM stdin;
e52fb623-86f0-4833-9c9b-dbbafbfc4d93	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	f	${role_default-roles}	default-roles-master	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	\N	\N
7f6e982b-20ec-4574-a4d5-539f845a770e	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	f	${role_admin}	admin	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	\N	\N
b5df16b0-c491-40db-be0f-f5fd9813698d	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	f	${role_create-realm}	create-realm	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	\N	\N
5a9c8959-cb2a-4f93-ad5f-0ab1c2c590e5	6b42d9f1-5b82-481b-aab2-4286edf4e8e8	t	${role_create-client}	create-client	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	6b42d9f1-5b82-481b-aab2-4286edf4e8e8	\N
bb250240-a9bc-454e-9f37-f85ce33b9bf4	6b42d9f1-5b82-481b-aab2-4286edf4e8e8	t	${role_view-realm}	view-realm	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	6b42d9f1-5b82-481b-aab2-4286edf4e8e8	\N
2192eebb-f131-4215-890e-7bde94b336dc	6b42d9f1-5b82-481b-aab2-4286edf4e8e8	t	${role_view-users}	view-users	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	6b42d9f1-5b82-481b-aab2-4286edf4e8e8	\N
b0b45185-cf1c-45f9-8a04-4b7b5c4d53f2	6b42d9f1-5b82-481b-aab2-4286edf4e8e8	t	${role_view-clients}	view-clients	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	6b42d9f1-5b82-481b-aab2-4286edf4e8e8	\N
66864d04-e7b0-4795-afc2-0ce71754c66e	6b42d9f1-5b82-481b-aab2-4286edf4e8e8	t	${role_view-events}	view-events	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	6b42d9f1-5b82-481b-aab2-4286edf4e8e8	\N
5e114d37-0688-49f3-832b-661f52ee733c	6b42d9f1-5b82-481b-aab2-4286edf4e8e8	t	${role_view-identity-providers}	view-identity-providers	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	6b42d9f1-5b82-481b-aab2-4286edf4e8e8	\N
e07473fd-de7f-4999-81b3-0d8804d65d59	6b42d9f1-5b82-481b-aab2-4286edf4e8e8	t	${role_view-authorization}	view-authorization	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	6b42d9f1-5b82-481b-aab2-4286edf4e8e8	\N
cf56997d-564b-4d48-9481-eaa898bdbf23	6b42d9f1-5b82-481b-aab2-4286edf4e8e8	t	${role_manage-realm}	manage-realm	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	6b42d9f1-5b82-481b-aab2-4286edf4e8e8	\N
72857986-26f8-41a5-af57-8421631a61b7	6b42d9f1-5b82-481b-aab2-4286edf4e8e8	t	${role_manage-users}	manage-users	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	6b42d9f1-5b82-481b-aab2-4286edf4e8e8	\N
ee2febf6-dbfc-416b-8a86-acab9c495a47	6b42d9f1-5b82-481b-aab2-4286edf4e8e8	t	${role_manage-clients}	manage-clients	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	6b42d9f1-5b82-481b-aab2-4286edf4e8e8	\N
c04f1567-f719-44b9-81d4-b8bcd77e99f9	6b42d9f1-5b82-481b-aab2-4286edf4e8e8	t	${role_manage-events}	manage-events	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	6b42d9f1-5b82-481b-aab2-4286edf4e8e8	\N
4111d529-7641-4a9f-bf73-783d73b34db9	6b42d9f1-5b82-481b-aab2-4286edf4e8e8	t	${role_manage-identity-providers}	manage-identity-providers	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	6b42d9f1-5b82-481b-aab2-4286edf4e8e8	\N
c30a9928-59ba-4a10-a45e-501c9beadd4e	6b42d9f1-5b82-481b-aab2-4286edf4e8e8	t	${role_manage-authorization}	manage-authorization	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	6b42d9f1-5b82-481b-aab2-4286edf4e8e8	\N
d13615a7-3791-426c-ab16-0a522f9c473f	6b42d9f1-5b82-481b-aab2-4286edf4e8e8	t	${role_query-users}	query-users	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	6b42d9f1-5b82-481b-aab2-4286edf4e8e8	\N
84b7ef06-3c34-4ec0-be67-92ce10b153b9	6b42d9f1-5b82-481b-aab2-4286edf4e8e8	t	${role_query-clients}	query-clients	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	6b42d9f1-5b82-481b-aab2-4286edf4e8e8	\N
13d83f1a-5947-41a5-ae33-050b0160b37c	6b42d9f1-5b82-481b-aab2-4286edf4e8e8	t	${role_query-realms}	query-realms	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	6b42d9f1-5b82-481b-aab2-4286edf4e8e8	\N
dd8833fa-6644-4e78-8919-122c79dcde32	6b42d9f1-5b82-481b-aab2-4286edf4e8e8	t	${role_query-groups}	query-groups	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	6b42d9f1-5b82-481b-aab2-4286edf4e8e8	\N
ec9b1d11-9140-4192-a4a9-68dce0acba25	5faa1af4-99ee-4664-83c0-ada476a835be	t	${role_view-profile}	view-profile	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	5faa1af4-99ee-4664-83c0-ada476a835be	\N
8d61b16e-aeeb-4cf1-b37e-3c56c3b0c2f3	5faa1af4-99ee-4664-83c0-ada476a835be	t	${role_manage-account}	manage-account	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	5faa1af4-99ee-4664-83c0-ada476a835be	\N
8b1d0e2e-4ca9-4e86-9b72-a9a36ea458b5	5faa1af4-99ee-4664-83c0-ada476a835be	t	${role_manage-account-links}	manage-account-links	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	5faa1af4-99ee-4664-83c0-ada476a835be	\N
57b285a0-52c9-46af-95ee-86acbaf203e0	5faa1af4-99ee-4664-83c0-ada476a835be	t	${role_view-applications}	view-applications	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	5faa1af4-99ee-4664-83c0-ada476a835be	\N
1674ac8f-675f-430a-8d86-d7fdd019372c	5faa1af4-99ee-4664-83c0-ada476a835be	t	${role_view-consent}	view-consent	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	5faa1af4-99ee-4664-83c0-ada476a835be	\N
ffe5f76f-2478-4246-a573-73e1f5dd6de4	5faa1af4-99ee-4664-83c0-ada476a835be	t	${role_manage-consent}	manage-consent	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	5faa1af4-99ee-4664-83c0-ada476a835be	\N
8620479a-fd7f-4dbe-9bc7-48bb54178a80	5faa1af4-99ee-4664-83c0-ada476a835be	t	${role_delete-account}	delete-account	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	5faa1af4-99ee-4664-83c0-ada476a835be	\N
ef552923-2977-4bac-9450-7df72b1d7c20	0603646a-8907-459a-91bf-facd78dc0140	t	${role_read-token}	read-token	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	0603646a-8907-459a-91bf-facd78dc0140	\N
4f71ac80-f5c6-483b-a57b-95d3a62dd300	6b42d9f1-5b82-481b-aab2-4286edf4e8e8	t	${role_impersonation}	impersonation	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	6b42d9f1-5b82-481b-aab2-4286edf4e8e8	\N
b697644d-9a79-47cc-be19-b41bc079258a	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	f	${role_offline-access}	offline_access	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	\N	\N
6906700a-deb4-4b3d-8856-8346c5996bf2	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	f	${role_uma_authorization}	uma_authorization	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	\N	\N
cc41a34b-4e7b-4396-8274-808c8c8b4ff0	5faa1af4-99ee-4664-83c0-ada476a835be	t	${role_view-groups}	view-groups	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	5faa1af4-99ee-4664-83c0-ada476a835be	\N
42f64d4f-4059-4dca-aab6-44b0725e5469	3a9fd404-d708-4128-83e7-ef00fa943752	f	${role_default-roles}	default-roles-test	3a9fd404-d708-4128-83e7-ef00fa943752	\N	\N
2313bd52-977b-48b4-a653-2189511987e5	fcaa2ba0-efe8-4ad8-b7e8-6fd188f409bd	t	${role_create-client}	create-client	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	fcaa2ba0-efe8-4ad8-b7e8-6fd188f409bd	\N
f6214b20-dcc3-4285-bf58-c93b6cd8f5be	fcaa2ba0-efe8-4ad8-b7e8-6fd188f409bd	t	${role_view-realm}	view-realm	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	fcaa2ba0-efe8-4ad8-b7e8-6fd188f409bd	\N
f0b39f30-bd4f-4a2e-966b-65672a5d7ed3	fcaa2ba0-efe8-4ad8-b7e8-6fd188f409bd	t	${role_view-users}	view-users	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	fcaa2ba0-efe8-4ad8-b7e8-6fd188f409bd	\N
983259a2-7830-4cc7-8f65-6d0fa404580d	fcaa2ba0-efe8-4ad8-b7e8-6fd188f409bd	t	${role_view-clients}	view-clients	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	fcaa2ba0-efe8-4ad8-b7e8-6fd188f409bd	\N
3df2d791-bc78-4639-bc75-cba165eff8ba	fcaa2ba0-efe8-4ad8-b7e8-6fd188f409bd	t	${role_view-events}	view-events	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	fcaa2ba0-efe8-4ad8-b7e8-6fd188f409bd	\N
7b17674e-cd9d-446d-bd4d-f215f1666c03	fcaa2ba0-efe8-4ad8-b7e8-6fd188f409bd	t	${role_view-identity-providers}	view-identity-providers	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	fcaa2ba0-efe8-4ad8-b7e8-6fd188f409bd	\N
5b73b9fc-021d-4c58-92b3-30088d7e741e	fcaa2ba0-efe8-4ad8-b7e8-6fd188f409bd	t	${role_view-authorization}	view-authorization	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	fcaa2ba0-efe8-4ad8-b7e8-6fd188f409bd	\N
a8492758-84eb-4ba8-92ab-69fa678e0964	fcaa2ba0-efe8-4ad8-b7e8-6fd188f409bd	t	${role_manage-realm}	manage-realm	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	fcaa2ba0-efe8-4ad8-b7e8-6fd188f409bd	\N
84cf1880-6550-4096-b10f-6f97e75ee2ef	fcaa2ba0-efe8-4ad8-b7e8-6fd188f409bd	t	${role_manage-users}	manage-users	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	fcaa2ba0-efe8-4ad8-b7e8-6fd188f409bd	\N
62bbd9fc-8327-462e-982b-74873de472ca	fcaa2ba0-efe8-4ad8-b7e8-6fd188f409bd	t	${role_manage-clients}	manage-clients	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	fcaa2ba0-efe8-4ad8-b7e8-6fd188f409bd	\N
62ad2008-6115-4bc9-930e-e42062b42520	fcaa2ba0-efe8-4ad8-b7e8-6fd188f409bd	t	${role_manage-events}	manage-events	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	fcaa2ba0-efe8-4ad8-b7e8-6fd188f409bd	\N
9917e1a2-e7ec-43f1-b2a9-23fe7f543cd9	fcaa2ba0-efe8-4ad8-b7e8-6fd188f409bd	t	${role_manage-identity-providers}	manage-identity-providers	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	fcaa2ba0-efe8-4ad8-b7e8-6fd188f409bd	\N
64652ce2-1738-45e4-8da7-1c16b36c835b	fcaa2ba0-efe8-4ad8-b7e8-6fd188f409bd	t	${role_manage-authorization}	manage-authorization	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	fcaa2ba0-efe8-4ad8-b7e8-6fd188f409bd	\N
6a631fff-f677-4247-a2a1-ad991336c584	fcaa2ba0-efe8-4ad8-b7e8-6fd188f409bd	t	${role_query-users}	query-users	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	fcaa2ba0-efe8-4ad8-b7e8-6fd188f409bd	\N
95f13184-edd0-4693-b91e-db224e78207d	fcaa2ba0-efe8-4ad8-b7e8-6fd188f409bd	t	${role_query-clients}	query-clients	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	fcaa2ba0-efe8-4ad8-b7e8-6fd188f409bd	\N
645a69bf-1ac7-4c8b-badf-c1088bf27194	fcaa2ba0-efe8-4ad8-b7e8-6fd188f409bd	t	${role_query-realms}	query-realms	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	fcaa2ba0-efe8-4ad8-b7e8-6fd188f409bd	\N
4bbb3bb1-6f7c-4660-9bda-fa3e8ff3ba67	fcaa2ba0-efe8-4ad8-b7e8-6fd188f409bd	t	${role_query-groups}	query-groups	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	fcaa2ba0-efe8-4ad8-b7e8-6fd188f409bd	\N
34912a74-3713-4cee-b482-f7fcd604afff	2c594784-be48-4508-a8e0-1135e4cb37ae	t	${role_realm-admin}	realm-admin	3a9fd404-d708-4128-83e7-ef00fa943752	2c594784-be48-4508-a8e0-1135e4cb37ae	\N
f5647caa-e381-4f52-8839-313608754e28	2c594784-be48-4508-a8e0-1135e4cb37ae	t	${role_create-client}	create-client	3a9fd404-d708-4128-83e7-ef00fa943752	2c594784-be48-4508-a8e0-1135e4cb37ae	\N
25da286f-37ba-480b-82ba-b43024724ee8	2c594784-be48-4508-a8e0-1135e4cb37ae	t	${role_view-realm}	view-realm	3a9fd404-d708-4128-83e7-ef00fa943752	2c594784-be48-4508-a8e0-1135e4cb37ae	\N
305213e3-bf40-419d-ae7a-09e53a8c8681	2c594784-be48-4508-a8e0-1135e4cb37ae	t	${role_view-users}	view-users	3a9fd404-d708-4128-83e7-ef00fa943752	2c594784-be48-4508-a8e0-1135e4cb37ae	\N
6762e630-0b80-4453-823e-478d0fec76aa	2c594784-be48-4508-a8e0-1135e4cb37ae	t	${role_view-clients}	view-clients	3a9fd404-d708-4128-83e7-ef00fa943752	2c594784-be48-4508-a8e0-1135e4cb37ae	\N
793f9964-dc14-489f-accc-d281bf089b45	2c594784-be48-4508-a8e0-1135e4cb37ae	t	${role_view-events}	view-events	3a9fd404-d708-4128-83e7-ef00fa943752	2c594784-be48-4508-a8e0-1135e4cb37ae	\N
e821981e-c5e2-41ca-ab58-f54c6755f279	2c594784-be48-4508-a8e0-1135e4cb37ae	t	${role_view-identity-providers}	view-identity-providers	3a9fd404-d708-4128-83e7-ef00fa943752	2c594784-be48-4508-a8e0-1135e4cb37ae	\N
d5898665-00fc-4c33-a806-4d97b4bb8838	2c594784-be48-4508-a8e0-1135e4cb37ae	t	${role_view-authorization}	view-authorization	3a9fd404-d708-4128-83e7-ef00fa943752	2c594784-be48-4508-a8e0-1135e4cb37ae	\N
b6733d58-3a43-41e9-a2eb-ae360657d053	2c594784-be48-4508-a8e0-1135e4cb37ae	t	${role_manage-realm}	manage-realm	3a9fd404-d708-4128-83e7-ef00fa943752	2c594784-be48-4508-a8e0-1135e4cb37ae	\N
a0271e9a-a863-4fb3-9ee8-4172f1405742	2c594784-be48-4508-a8e0-1135e4cb37ae	t	${role_manage-users}	manage-users	3a9fd404-d708-4128-83e7-ef00fa943752	2c594784-be48-4508-a8e0-1135e4cb37ae	\N
0768ea7d-1a96-42af-b9cb-d0e3edbe8d28	2c594784-be48-4508-a8e0-1135e4cb37ae	t	${role_manage-clients}	manage-clients	3a9fd404-d708-4128-83e7-ef00fa943752	2c594784-be48-4508-a8e0-1135e4cb37ae	\N
94b009f8-3759-4af8-a01c-6bde819f1ea4	2c594784-be48-4508-a8e0-1135e4cb37ae	t	${role_manage-events}	manage-events	3a9fd404-d708-4128-83e7-ef00fa943752	2c594784-be48-4508-a8e0-1135e4cb37ae	\N
6ef84316-fba1-4885-b900-edfac3ac87a4	2c594784-be48-4508-a8e0-1135e4cb37ae	t	${role_manage-identity-providers}	manage-identity-providers	3a9fd404-d708-4128-83e7-ef00fa943752	2c594784-be48-4508-a8e0-1135e4cb37ae	\N
04459e5c-8386-45fc-82e2-f2021b7be253	2c594784-be48-4508-a8e0-1135e4cb37ae	t	${role_manage-authorization}	manage-authorization	3a9fd404-d708-4128-83e7-ef00fa943752	2c594784-be48-4508-a8e0-1135e4cb37ae	\N
8c76f207-a9f9-4e48-88bb-5f1beabea99b	2c594784-be48-4508-a8e0-1135e4cb37ae	t	${role_query-users}	query-users	3a9fd404-d708-4128-83e7-ef00fa943752	2c594784-be48-4508-a8e0-1135e4cb37ae	\N
819731d3-be54-4ce3-b55a-c583572cc682	2c594784-be48-4508-a8e0-1135e4cb37ae	t	${role_query-clients}	query-clients	3a9fd404-d708-4128-83e7-ef00fa943752	2c594784-be48-4508-a8e0-1135e4cb37ae	\N
4870d54d-2ca9-4a24-bcac-f109859c9725	2c594784-be48-4508-a8e0-1135e4cb37ae	t	${role_query-realms}	query-realms	3a9fd404-d708-4128-83e7-ef00fa943752	2c594784-be48-4508-a8e0-1135e4cb37ae	\N
51f207e1-0df3-466b-b890-8ffb2368b702	2c594784-be48-4508-a8e0-1135e4cb37ae	t	${role_query-groups}	query-groups	3a9fd404-d708-4128-83e7-ef00fa943752	2c594784-be48-4508-a8e0-1135e4cb37ae	\N
9341e953-34c5-4700-9ef0-782171cbe232	9d44bf2d-8bad-41b0-a76d-9ef30b0ce5bf	t	${role_view-profile}	view-profile	3a9fd404-d708-4128-83e7-ef00fa943752	9d44bf2d-8bad-41b0-a76d-9ef30b0ce5bf	\N
898d7633-0975-41f6-989a-d2e661311e9d	9d44bf2d-8bad-41b0-a76d-9ef30b0ce5bf	t	${role_manage-account}	manage-account	3a9fd404-d708-4128-83e7-ef00fa943752	9d44bf2d-8bad-41b0-a76d-9ef30b0ce5bf	\N
6b7e944a-1f34-4c9f-9e4e-082b9174d6c3	9d44bf2d-8bad-41b0-a76d-9ef30b0ce5bf	t	${role_manage-account-links}	manage-account-links	3a9fd404-d708-4128-83e7-ef00fa943752	9d44bf2d-8bad-41b0-a76d-9ef30b0ce5bf	\N
53f56658-cb1d-4f4d-99bf-27071c4a8b49	9d44bf2d-8bad-41b0-a76d-9ef30b0ce5bf	t	${role_view-applications}	view-applications	3a9fd404-d708-4128-83e7-ef00fa943752	9d44bf2d-8bad-41b0-a76d-9ef30b0ce5bf	\N
d4704af7-34cb-4f97-8907-db18dd0e3394	9d44bf2d-8bad-41b0-a76d-9ef30b0ce5bf	t	${role_view-consent}	view-consent	3a9fd404-d708-4128-83e7-ef00fa943752	9d44bf2d-8bad-41b0-a76d-9ef30b0ce5bf	\N
343b54e3-55ad-491d-b513-87906876c16a	9d44bf2d-8bad-41b0-a76d-9ef30b0ce5bf	t	${role_manage-consent}	manage-consent	3a9fd404-d708-4128-83e7-ef00fa943752	9d44bf2d-8bad-41b0-a76d-9ef30b0ce5bf	\N
8c1d2735-c84c-498b-978b-90b94a39243e	9d44bf2d-8bad-41b0-a76d-9ef30b0ce5bf	t	${role_view-groups}	view-groups	3a9fd404-d708-4128-83e7-ef00fa943752	9d44bf2d-8bad-41b0-a76d-9ef30b0ce5bf	\N
c20021ce-7817-4e5d-ae80-55cf2b59d2aa	9d44bf2d-8bad-41b0-a76d-9ef30b0ce5bf	t	${role_delete-account}	delete-account	3a9fd404-d708-4128-83e7-ef00fa943752	9d44bf2d-8bad-41b0-a76d-9ef30b0ce5bf	\N
015876ed-deb6-4202-a3f1-455caa1df6fb	fcaa2ba0-efe8-4ad8-b7e8-6fd188f409bd	t	${role_impersonation}	impersonation	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	fcaa2ba0-efe8-4ad8-b7e8-6fd188f409bd	\N
36f6b327-4e7e-48ec-8c3b-065fce32a555	2c594784-be48-4508-a8e0-1135e4cb37ae	t	${role_impersonation}	impersonation	3a9fd404-d708-4128-83e7-ef00fa943752	2c594784-be48-4508-a8e0-1135e4cb37ae	\N
25a62c17-eb02-4cb0-9f49-9077e11242ac	fee15b0e-3064-42a7-867a-d9ba6488cf9f	t	${role_read-token}	read-token	3a9fd404-d708-4128-83e7-ef00fa943752	fee15b0e-3064-42a7-867a-d9ba6488cf9f	\N
8fb6b6ef-3b9e-4cdc-81ec-b1ed952dc628	3a9fd404-d708-4128-83e7-ef00fa943752	f	${role_offline-access}	offline_access	3a9fd404-d708-4128-83e7-ef00fa943752	\N	\N
f37266fd-f534-416d-b26d-43ade62a9679	3a9fd404-d708-4128-83e7-ef00fa943752	f	${role_uma_authorization}	uma_authorization	3a9fd404-d708-4128-83e7-ef00fa943752	\N	\N
\.


--
-- Data for Name: migration_model; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.migration_model (id, version, update_time) FROM stdin;
hcfyq	18.0.0	1653471532
wgh99	26.0.5	1732727510
dwpwm	26.0.7	1733222957
\.


--
-- Data for Name: offline_client_session; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.offline_client_session (user_session_id, client_id, offline_flag, "timestamp", data, client_storage_provider, external_client_id, version) FROM stdin;
\.


--
-- Data for Name: offline_user_session; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.offline_user_session (user_session_id, user_id, realm_id, created_on, offline_flag, data, last_session_refresh, broker_session_id, version) FROM stdin;
\.


--
-- Data for Name: org; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.org (id, enabled, realm_id, group_id, name, description, alias, redirect_url) FROM stdin;
\.


--
-- Data for Name: org_domain; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.org_domain (id, name, verified, org_id) FROM stdin;
\.


--
-- Data for Name: policy_config; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.policy_config (policy_id, name, value) FROM stdin;
\.


--
-- Data for Name: protocol_mapper; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.protocol_mapper (id, name, protocol, protocol_mapper_name, client_id, client_scope_id) FROM stdin;
36dd221c-3909-46ef-aeb3-5a5b2a6664bb	audience resolve	openid-connect	oidc-audience-resolve-mapper	3cbf8aa7-c144-492c-aaf9-859a636cce37	\N
cc6c522b-8c8b-4b5d-b029-19f4e4540e53	locale	openid-connect	oidc-usermodel-attribute-mapper	193c8e25-0d00-4c45-ad5c-fa2a6e50d8f6	\N
79c6c311-238f-4804-a3f0-4f5a98e42a34	role list	saml	saml-role-list-mapper	\N	c5728f88-7d82-4a3f-95b0-e8394aa20f74
0cc2a68c-7a1e-443d-be34-270d86610958	full name	openid-connect	oidc-full-name-mapper	\N	b7fa698d-506e-4edd-ba59-92d1330019f7
a3600141-09d2-4474-831e-7e54dfadc51a	family name	openid-connect	oidc-usermodel-property-mapper	\N	b7fa698d-506e-4edd-ba59-92d1330019f7
efb2a06c-01bc-4f22-9f47-3bdd96d6bf4b	given name	openid-connect	oidc-usermodel-property-mapper	\N	b7fa698d-506e-4edd-ba59-92d1330019f7
6d18235d-51ac-4e1e-b580-63d654abff5f	middle name	openid-connect	oidc-usermodel-attribute-mapper	\N	b7fa698d-506e-4edd-ba59-92d1330019f7
25c9ee9b-913c-4a51-96e2-e5264bb3ec5b	nickname	openid-connect	oidc-usermodel-attribute-mapper	\N	b7fa698d-506e-4edd-ba59-92d1330019f7
f407eb75-a34b-482d-b8b6-700c64732c2d	username	openid-connect	oidc-usermodel-property-mapper	\N	b7fa698d-506e-4edd-ba59-92d1330019f7
a206a391-08e1-410b-9dc5-4e7f303b652c	profile	openid-connect	oidc-usermodel-attribute-mapper	\N	b7fa698d-506e-4edd-ba59-92d1330019f7
ab9db35f-a212-48a9-85c0-0e4a9b3ca4c9	picture	openid-connect	oidc-usermodel-attribute-mapper	\N	b7fa698d-506e-4edd-ba59-92d1330019f7
aadc8fcc-9c5c-465c-966a-a10d55ac4a59	website	openid-connect	oidc-usermodel-attribute-mapper	\N	b7fa698d-506e-4edd-ba59-92d1330019f7
41bdc006-40c5-4947-9aa4-c5667b7fc1f6	gender	openid-connect	oidc-usermodel-attribute-mapper	\N	b7fa698d-506e-4edd-ba59-92d1330019f7
ea63386a-61c0-49ed-b8b7-f017a1f74bfb	birthdate	openid-connect	oidc-usermodel-attribute-mapper	\N	b7fa698d-506e-4edd-ba59-92d1330019f7
ace36a52-d7bc-4b44-9223-86ed9ccd89dc	zoneinfo	openid-connect	oidc-usermodel-attribute-mapper	\N	b7fa698d-506e-4edd-ba59-92d1330019f7
5c3842ee-26c8-409e-a4fe-b48456ecfc91	locale	openid-connect	oidc-usermodel-attribute-mapper	\N	b7fa698d-506e-4edd-ba59-92d1330019f7
5122c472-1f8f-43e9-9851-1e915adab44f	updated at	openid-connect	oidc-usermodel-attribute-mapper	\N	b7fa698d-506e-4edd-ba59-92d1330019f7
65c6c9cb-93cb-4478-a17a-04ba2f1dcc1e	email	openid-connect	oidc-usermodel-property-mapper	\N	57b95975-6d57-4d7a-86bd-3912d154d522
593456d1-8510-4cd7-80eb-3abfd5c16c13	email verified	openid-connect	oidc-usermodel-property-mapper	\N	57b95975-6d57-4d7a-86bd-3912d154d522
c1510ea2-1b42-489f-9b18-bf090ca8ea7c	address	openid-connect	oidc-address-mapper	\N	de577a7b-5939-44fa-aa0f-d0b50991435b
e5b6c33b-ada5-4cfc-87e1-08777b049455	phone number	openid-connect	oidc-usermodel-attribute-mapper	\N	23331e70-04a9-4ee0-a188-49a8636c01b8
23bea587-d09a-4817-a2b5-f442375a60e1	phone number verified	openid-connect	oidc-usermodel-attribute-mapper	\N	23331e70-04a9-4ee0-a188-49a8636c01b8
903d1fdd-8c69-422b-8a7b-ad6e46a2e338	realm roles	openid-connect	oidc-usermodel-realm-role-mapper	\N	d4ea9c2f-7ae0-4297-8743-e5314bb8e811
870eb91b-49d7-4d2c-a094-8fa9f51ffe71	client roles	openid-connect	oidc-usermodel-client-role-mapper	\N	d4ea9c2f-7ae0-4297-8743-e5314bb8e811
9f96d14d-2ca6-4c4c-ae8e-dab21f1941c9	audience resolve	openid-connect	oidc-audience-resolve-mapper	\N	d4ea9c2f-7ae0-4297-8743-e5314bb8e811
5de48feb-2642-4def-988e-739742c9cee9	allowed web origins	openid-connect	oidc-allowed-origins-mapper	\N	bf7a8b48-9bfb-4612-a643-8d7c01c5ae91
a82fc42d-ee06-45bc-9245-a6b20920149d	upn	openid-connect	oidc-usermodel-property-mapper	\N	180ede63-bd73-4832-b8aa-f930d57263d5
17281910-15d9-4409-a856-ef62262c5846	groups	openid-connect	oidc-usermodel-realm-role-mapper	\N	180ede63-bd73-4832-b8aa-f930d57263d5
bba9420a-f0a4-4a36-b8ec-4876f4b60af1	acr loa level	openid-connect	oidc-acr-mapper	\N	b5a5d94a-d9d0-4dde-ad70-7b629561ad61
e4e601a9-8f42-4e77-a7bf-cf00b26ed191	auth_time	openid-connect	oidc-usersessionmodel-note-mapper	\N	6f3da124-ce69-4e6c-b933-35dfc3e58221
e1f6aa24-dd73-4004-bb3c-fe5bda3d7a50	sub	openid-connect	oidc-sub-mapper	\N	6f3da124-ce69-4e6c-b933-35dfc3e58221
aae53041-853c-4454-a705-a28898551863	audience resolve	openid-connect	oidc-audience-resolve-mapper	7b5da870-582a-4bd2-9320-84172f977b88	\N
a6d74478-b943-49b5-b86b-b92a1aab6a2e	role list	saml	saml-role-list-mapper	\N	59352ece-9aad-4086-ac75-eb145c579d2d
c3bca9a6-81df-458d-9c1d-5b2487e07008	organization	saml	saml-organization-membership-mapper	\N	50169b72-4790-45a2-9a55-1f1797bc287b
74f03892-3ff6-4b18-a9cb-31ff52db4434	full name	openid-connect	oidc-full-name-mapper	\N	3809229d-762a-41ad-b8da-9175829b87e4
a6585b89-f4d5-40af-9f3c-8dad5f15745a	family name	openid-connect	oidc-usermodel-attribute-mapper	\N	3809229d-762a-41ad-b8da-9175829b87e4
6ff0b1d9-cea2-48bc-b3d8-b76bda9a9643	given name	openid-connect	oidc-usermodel-attribute-mapper	\N	3809229d-762a-41ad-b8da-9175829b87e4
b78a1e40-b7b3-4d71-89b6-2f7755780f27	middle name	openid-connect	oidc-usermodel-attribute-mapper	\N	3809229d-762a-41ad-b8da-9175829b87e4
408f216e-c594-4bd1-b299-c0f8072f6b4d	nickname	openid-connect	oidc-usermodel-attribute-mapper	\N	3809229d-762a-41ad-b8da-9175829b87e4
0d5c2ba1-ec39-4a99-8f95-58452c73051c	username	openid-connect	oidc-usermodel-attribute-mapper	\N	3809229d-762a-41ad-b8da-9175829b87e4
bbcf3685-cf64-4932-b0fa-4f7c8849e4ad	profile	openid-connect	oidc-usermodel-attribute-mapper	\N	3809229d-762a-41ad-b8da-9175829b87e4
96b30c31-bc14-46e8-9ae1-bb836d5a79c6	picture	openid-connect	oidc-usermodel-attribute-mapper	\N	3809229d-762a-41ad-b8da-9175829b87e4
9789cbae-a343-43d7-a161-e79162ff3b75	website	openid-connect	oidc-usermodel-attribute-mapper	\N	3809229d-762a-41ad-b8da-9175829b87e4
01a3be18-a127-491b-8ad4-dd313728b23c	gender	openid-connect	oidc-usermodel-attribute-mapper	\N	3809229d-762a-41ad-b8da-9175829b87e4
c186469c-ec75-49d6-93d3-431be8f2fd75	birthdate	openid-connect	oidc-usermodel-attribute-mapper	\N	3809229d-762a-41ad-b8da-9175829b87e4
f0fccd6f-dc17-4a84-a939-65477a93b9fd	zoneinfo	openid-connect	oidc-usermodel-attribute-mapper	\N	3809229d-762a-41ad-b8da-9175829b87e4
2dc15d60-e942-4713-a248-6d62f08b6d56	locale	openid-connect	oidc-usermodel-attribute-mapper	\N	3809229d-762a-41ad-b8da-9175829b87e4
77b5ebe2-52e6-4284-ade4-e9197a1028af	updated at	openid-connect	oidc-usermodel-attribute-mapper	\N	3809229d-762a-41ad-b8da-9175829b87e4
bb090263-d375-4457-9255-27be4b34d8c7	email	openid-connect	oidc-usermodel-attribute-mapper	\N	748acf76-9899-4a7c-b222-7a4bd53d21a1
758135d8-c4ff-42a5-be68-3d02ea800f0c	email verified	openid-connect	oidc-usermodel-property-mapper	\N	748acf76-9899-4a7c-b222-7a4bd53d21a1
241667b9-f39f-4154-b843-c6e4e125ccad	address	openid-connect	oidc-address-mapper	\N	e54f533d-3819-4345-9502-8c3b57ebb6a6
4d46932c-309f-486f-8318-2c66f94d4a36	phone number	openid-connect	oidc-usermodel-attribute-mapper	\N	5b4a9626-9522-4fdf-89fc-6abe5538a593
1e722767-7cac-4b25-927b-75f6c9f1c4f9	phone number verified	openid-connect	oidc-usermodel-attribute-mapper	\N	5b4a9626-9522-4fdf-89fc-6abe5538a593
245c24cf-ee5e-4c30-b68b-e2d18a703bee	realm roles	openid-connect	oidc-usermodel-realm-role-mapper	\N	5c60430f-7841-4c66-acb4-0d372753c861
120eb7c4-3af0-4f9e-93f6-4335a7f4a43e	client roles	openid-connect	oidc-usermodel-client-role-mapper	\N	5c60430f-7841-4c66-acb4-0d372753c861
7931d561-24ed-4f67-beb4-41b153f17b79	audience resolve	openid-connect	oidc-audience-resolve-mapper	\N	5c60430f-7841-4c66-acb4-0d372753c861
ffbb1f10-1c04-4306-8d1e-c5184ceb7539	allowed web origins	openid-connect	oidc-allowed-origins-mapper	\N	d7a399be-d14a-43bf-9a5a-cde8802b9168
bebbcedd-0e3d-4705-b4cd-f4684676422c	upn	openid-connect	oidc-usermodel-attribute-mapper	\N	dc3fd5f4-803e-4df7-ab4b-56b6a0df379c
0cea6507-abb7-41cd-9b81-3faeb5b29741	groups	openid-connect	oidc-usermodel-realm-role-mapper	\N	dc3fd5f4-803e-4df7-ab4b-56b6a0df379c
f7a19634-6cf1-42f0-8486-71096245f0fb	acr loa level	openid-connect	oidc-acr-mapper	\N	9058dc48-9336-4dac-95ec-81095b2bf8b9
54df5b64-7355-43f3-bc89-019459e14caa	auth_time	openid-connect	oidc-usersessionmodel-note-mapper	\N	3583a32f-b84b-4d85-a79d-0ee871e335dc
ebfe2aa3-52f8-49ab-88a2-a8784e8e0496	sub	openid-connect	oidc-sub-mapper	\N	3583a32f-b84b-4d85-a79d-0ee871e335dc
90e9b12f-241b-4415-a214-a1c027f862f4	organization	openid-connect	oidc-organization-membership-mapper	\N	880f5c51-7209-4262-9a6a-543895c0075f
22bcbde9-37c6-4c35-9c96-1cfa39757b81	locale	openid-connect	oidc-usermodel-attribute-mapper	ca3d0ca8-abd5-4605-80df-b32fa0e45a83	\N
b1338b66-b82c-446d-8525-152254c98986	Client ID	openid-connect	oidc-usersessionmodel-note-mapper	27b6abbc-155f-4768-9200-8721b6d0831f	\N
a6fd6bfb-3c9c-4e0a-8344-e8a41f5d6f59	Client Host	openid-connect	oidc-usersessionmodel-note-mapper	27b6abbc-155f-4768-9200-8721b6d0831f	\N
05917ef0-bd5d-4599-8f6b-3d6084c332e1	Client IP Address	openid-connect	oidc-usersessionmodel-note-mapper	27b6abbc-155f-4768-9200-8721b6d0831f	\N
\.


--
-- Data for Name: protocol_mapper_config; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.protocol_mapper_config (protocol_mapper_id, value, name) FROM stdin;
cc6c522b-8c8b-4b5d-b029-19f4e4540e53	true	userinfo.token.claim
cc6c522b-8c8b-4b5d-b029-19f4e4540e53	locale	user.attribute
cc6c522b-8c8b-4b5d-b029-19f4e4540e53	true	id.token.claim
cc6c522b-8c8b-4b5d-b029-19f4e4540e53	true	access.token.claim
cc6c522b-8c8b-4b5d-b029-19f4e4540e53	locale	claim.name
cc6c522b-8c8b-4b5d-b029-19f4e4540e53	String	jsonType.label
79c6c311-238f-4804-a3f0-4f5a98e42a34	false	single
79c6c311-238f-4804-a3f0-4f5a98e42a34	Basic	attribute.nameformat
79c6c311-238f-4804-a3f0-4f5a98e42a34	Role	attribute.name
0cc2a68c-7a1e-443d-be34-270d86610958	true	userinfo.token.claim
0cc2a68c-7a1e-443d-be34-270d86610958	true	id.token.claim
0cc2a68c-7a1e-443d-be34-270d86610958	true	access.token.claim
25c9ee9b-913c-4a51-96e2-e5264bb3ec5b	true	userinfo.token.claim
25c9ee9b-913c-4a51-96e2-e5264bb3ec5b	nickname	user.attribute
25c9ee9b-913c-4a51-96e2-e5264bb3ec5b	true	id.token.claim
25c9ee9b-913c-4a51-96e2-e5264bb3ec5b	true	access.token.claim
25c9ee9b-913c-4a51-96e2-e5264bb3ec5b	nickname	claim.name
25c9ee9b-913c-4a51-96e2-e5264bb3ec5b	String	jsonType.label
41bdc006-40c5-4947-9aa4-c5667b7fc1f6	true	userinfo.token.claim
41bdc006-40c5-4947-9aa4-c5667b7fc1f6	gender	user.attribute
41bdc006-40c5-4947-9aa4-c5667b7fc1f6	true	id.token.claim
41bdc006-40c5-4947-9aa4-c5667b7fc1f6	true	access.token.claim
41bdc006-40c5-4947-9aa4-c5667b7fc1f6	gender	claim.name
41bdc006-40c5-4947-9aa4-c5667b7fc1f6	String	jsonType.label
5122c472-1f8f-43e9-9851-1e915adab44f	true	userinfo.token.claim
5122c472-1f8f-43e9-9851-1e915adab44f	updatedAt	user.attribute
5122c472-1f8f-43e9-9851-1e915adab44f	true	id.token.claim
5122c472-1f8f-43e9-9851-1e915adab44f	true	access.token.claim
5122c472-1f8f-43e9-9851-1e915adab44f	updated_at	claim.name
5122c472-1f8f-43e9-9851-1e915adab44f	long	jsonType.label
5c3842ee-26c8-409e-a4fe-b48456ecfc91	true	userinfo.token.claim
5c3842ee-26c8-409e-a4fe-b48456ecfc91	locale	user.attribute
5c3842ee-26c8-409e-a4fe-b48456ecfc91	true	id.token.claim
5c3842ee-26c8-409e-a4fe-b48456ecfc91	true	access.token.claim
5c3842ee-26c8-409e-a4fe-b48456ecfc91	locale	claim.name
5c3842ee-26c8-409e-a4fe-b48456ecfc91	String	jsonType.label
6d18235d-51ac-4e1e-b580-63d654abff5f	true	userinfo.token.claim
6d18235d-51ac-4e1e-b580-63d654abff5f	middleName	user.attribute
6d18235d-51ac-4e1e-b580-63d654abff5f	true	id.token.claim
6d18235d-51ac-4e1e-b580-63d654abff5f	true	access.token.claim
6d18235d-51ac-4e1e-b580-63d654abff5f	middle_name	claim.name
6d18235d-51ac-4e1e-b580-63d654abff5f	String	jsonType.label
a206a391-08e1-410b-9dc5-4e7f303b652c	true	userinfo.token.claim
a206a391-08e1-410b-9dc5-4e7f303b652c	profile	user.attribute
a206a391-08e1-410b-9dc5-4e7f303b652c	true	id.token.claim
a206a391-08e1-410b-9dc5-4e7f303b652c	true	access.token.claim
a206a391-08e1-410b-9dc5-4e7f303b652c	profile	claim.name
a206a391-08e1-410b-9dc5-4e7f303b652c	String	jsonType.label
a3600141-09d2-4474-831e-7e54dfadc51a	true	userinfo.token.claim
a3600141-09d2-4474-831e-7e54dfadc51a	lastName	user.attribute
a3600141-09d2-4474-831e-7e54dfadc51a	true	id.token.claim
a3600141-09d2-4474-831e-7e54dfadc51a	true	access.token.claim
a3600141-09d2-4474-831e-7e54dfadc51a	family_name	claim.name
a3600141-09d2-4474-831e-7e54dfadc51a	String	jsonType.label
aadc8fcc-9c5c-465c-966a-a10d55ac4a59	true	userinfo.token.claim
aadc8fcc-9c5c-465c-966a-a10d55ac4a59	website	user.attribute
aadc8fcc-9c5c-465c-966a-a10d55ac4a59	true	id.token.claim
aadc8fcc-9c5c-465c-966a-a10d55ac4a59	true	access.token.claim
aadc8fcc-9c5c-465c-966a-a10d55ac4a59	website	claim.name
aadc8fcc-9c5c-465c-966a-a10d55ac4a59	String	jsonType.label
ab9db35f-a212-48a9-85c0-0e4a9b3ca4c9	true	userinfo.token.claim
ab9db35f-a212-48a9-85c0-0e4a9b3ca4c9	picture	user.attribute
ab9db35f-a212-48a9-85c0-0e4a9b3ca4c9	true	id.token.claim
ab9db35f-a212-48a9-85c0-0e4a9b3ca4c9	true	access.token.claim
ab9db35f-a212-48a9-85c0-0e4a9b3ca4c9	picture	claim.name
ab9db35f-a212-48a9-85c0-0e4a9b3ca4c9	String	jsonType.label
ace36a52-d7bc-4b44-9223-86ed9ccd89dc	true	userinfo.token.claim
ace36a52-d7bc-4b44-9223-86ed9ccd89dc	zoneinfo	user.attribute
ace36a52-d7bc-4b44-9223-86ed9ccd89dc	true	id.token.claim
ace36a52-d7bc-4b44-9223-86ed9ccd89dc	true	access.token.claim
ace36a52-d7bc-4b44-9223-86ed9ccd89dc	zoneinfo	claim.name
ace36a52-d7bc-4b44-9223-86ed9ccd89dc	String	jsonType.label
ea63386a-61c0-49ed-b8b7-f017a1f74bfb	true	userinfo.token.claim
ea63386a-61c0-49ed-b8b7-f017a1f74bfb	birthdate	user.attribute
ea63386a-61c0-49ed-b8b7-f017a1f74bfb	true	id.token.claim
ea63386a-61c0-49ed-b8b7-f017a1f74bfb	true	access.token.claim
ea63386a-61c0-49ed-b8b7-f017a1f74bfb	birthdate	claim.name
ea63386a-61c0-49ed-b8b7-f017a1f74bfb	String	jsonType.label
efb2a06c-01bc-4f22-9f47-3bdd96d6bf4b	true	userinfo.token.claim
efb2a06c-01bc-4f22-9f47-3bdd96d6bf4b	firstName	user.attribute
efb2a06c-01bc-4f22-9f47-3bdd96d6bf4b	true	id.token.claim
efb2a06c-01bc-4f22-9f47-3bdd96d6bf4b	true	access.token.claim
efb2a06c-01bc-4f22-9f47-3bdd96d6bf4b	given_name	claim.name
efb2a06c-01bc-4f22-9f47-3bdd96d6bf4b	String	jsonType.label
f407eb75-a34b-482d-b8b6-700c64732c2d	true	userinfo.token.claim
f407eb75-a34b-482d-b8b6-700c64732c2d	username	user.attribute
f407eb75-a34b-482d-b8b6-700c64732c2d	true	id.token.claim
f407eb75-a34b-482d-b8b6-700c64732c2d	true	access.token.claim
f407eb75-a34b-482d-b8b6-700c64732c2d	preferred_username	claim.name
f407eb75-a34b-482d-b8b6-700c64732c2d	String	jsonType.label
593456d1-8510-4cd7-80eb-3abfd5c16c13	true	userinfo.token.claim
593456d1-8510-4cd7-80eb-3abfd5c16c13	emailVerified	user.attribute
593456d1-8510-4cd7-80eb-3abfd5c16c13	true	id.token.claim
593456d1-8510-4cd7-80eb-3abfd5c16c13	true	access.token.claim
593456d1-8510-4cd7-80eb-3abfd5c16c13	email_verified	claim.name
593456d1-8510-4cd7-80eb-3abfd5c16c13	boolean	jsonType.label
65c6c9cb-93cb-4478-a17a-04ba2f1dcc1e	true	userinfo.token.claim
65c6c9cb-93cb-4478-a17a-04ba2f1dcc1e	email	user.attribute
65c6c9cb-93cb-4478-a17a-04ba2f1dcc1e	true	id.token.claim
65c6c9cb-93cb-4478-a17a-04ba2f1dcc1e	true	access.token.claim
65c6c9cb-93cb-4478-a17a-04ba2f1dcc1e	email	claim.name
65c6c9cb-93cb-4478-a17a-04ba2f1dcc1e	String	jsonType.label
c1510ea2-1b42-489f-9b18-bf090ca8ea7c	formatted	user.attribute.formatted
c1510ea2-1b42-489f-9b18-bf090ca8ea7c	country	user.attribute.country
c1510ea2-1b42-489f-9b18-bf090ca8ea7c	postal_code	user.attribute.postal_code
c1510ea2-1b42-489f-9b18-bf090ca8ea7c	true	userinfo.token.claim
c1510ea2-1b42-489f-9b18-bf090ca8ea7c	street	user.attribute.street
c1510ea2-1b42-489f-9b18-bf090ca8ea7c	true	id.token.claim
c1510ea2-1b42-489f-9b18-bf090ca8ea7c	region	user.attribute.region
c1510ea2-1b42-489f-9b18-bf090ca8ea7c	true	access.token.claim
c1510ea2-1b42-489f-9b18-bf090ca8ea7c	locality	user.attribute.locality
23bea587-d09a-4817-a2b5-f442375a60e1	true	userinfo.token.claim
23bea587-d09a-4817-a2b5-f442375a60e1	phoneNumberVerified	user.attribute
23bea587-d09a-4817-a2b5-f442375a60e1	true	id.token.claim
23bea587-d09a-4817-a2b5-f442375a60e1	true	access.token.claim
23bea587-d09a-4817-a2b5-f442375a60e1	phone_number_verified	claim.name
23bea587-d09a-4817-a2b5-f442375a60e1	boolean	jsonType.label
e5b6c33b-ada5-4cfc-87e1-08777b049455	true	userinfo.token.claim
e5b6c33b-ada5-4cfc-87e1-08777b049455	phoneNumber	user.attribute
e5b6c33b-ada5-4cfc-87e1-08777b049455	true	id.token.claim
e5b6c33b-ada5-4cfc-87e1-08777b049455	true	access.token.claim
e5b6c33b-ada5-4cfc-87e1-08777b049455	phone_number	claim.name
e5b6c33b-ada5-4cfc-87e1-08777b049455	String	jsonType.label
870eb91b-49d7-4d2c-a094-8fa9f51ffe71	true	multivalued
870eb91b-49d7-4d2c-a094-8fa9f51ffe71	foo	user.attribute
870eb91b-49d7-4d2c-a094-8fa9f51ffe71	true	access.token.claim
870eb91b-49d7-4d2c-a094-8fa9f51ffe71	resource_access.${client_id}.roles	claim.name
870eb91b-49d7-4d2c-a094-8fa9f51ffe71	String	jsonType.label
903d1fdd-8c69-422b-8a7b-ad6e46a2e338	true	multivalued
903d1fdd-8c69-422b-8a7b-ad6e46a2e338	foo	user.attribute
903d1fdd-8c69-422b-8a7b-ad6e46a2e338	true	access.token.claim
903d1fdd-8c69-422b-8a7b-ad6e46a2e338	realm_access.roles	claim.name
903d1fdd-8c69-422b-8a7b-ad6e46a2e338	String	jsonType.label
17281910-15d9-4409-a856-ef62262c5846	true	multivalued
17281910-15d9-4409-a856-ef62262c5846	foo	user.attribute
17281910-15d9-4409-a856-ef62262c5846	true	id.token.claim
17281910-15d9-4409-a856-ef62262c5846	true	access.token.claim
17281910-15d9-4409-a856-ef62262c5846	groups	claim.name
17281910-15d9-4409-a856-ef62262c5846	String	jsonType.label
a82fc42d-ee06-45bc-9245-a6b20920149d	true	userinfo.token.claim
a82fc42d-ee06-45bc-9245-a6b20920149d	username	user.attribute
a82fc42d-ee06-45bc-9245-a6b20920149d	true	id.token.claim
a82fc42d-ee06-45bc-9245-a6b20920149d	true	access.token.claim
a82fc42d-ee06-45bc-9245-a6b20920149d	upn	claim.name
a82fc42d-ee06-45bc-9245-a6b20920149d	String	jsonType.label
bba9420a-f0a4-4a36-b8ec-4876f4b60af1	true	id.token.claim
bba9420a-f0a4-4a36-b8ec-4876f4b60af1	true	access.token.claim
e1f6aa24-dd73-4004-bb3c-fe5bda3d7a50	true	introspection.token.claim
e1f6aa24-dd73-4004-bb3c-fe5bda3d7a50	true	access.token.claim
e4e601a9-8f42-4e77-a7bf-cf00b26ed191	AUTH_TIME	user.session.note
e4e601a9-8f42-4e77-a7bf-cf00b26ed191	true	introspection.token.claim
e4e601a9-8f42-4e77-a7bf-cf00b26ed191	true	id.token.claim
e4e601a9-8f42-4e77-a7bf-cf00b26ed191	true	access.token.claim
e4e601a9-8f42-4e77-a7bf-cf00b26ed191	auth_time	claim.name
e4e601a9-8f42-4e77-a7bf-cf00b26ed191	long	jsonType.label
a6d74478-b943-49b5-b86b-b92a1aab6a2e	false	single
a6d74478-b943-49b5-b86b-b92a1aab6a2e	Basic	attribute.nameformat
a6d74478-b943-49b5-b86b-b92a1aab6a2e	Role	attribute.name
01a3be18-a127-491b-8ad4-dd313728b23c	true	introspection.token.claim
01a3be18-a127-491b-8ad4-dd313728b23c	true	userinfo.token.claim
01a3be18-a127-491b-8ad4-dd313728b23c	gender	user.attribute
01a3be18-a127-491b-8ad4-dd313728b23c	true	id.token.claim
01a3be18-a127-491b-8ad4-dd313728b23c	true	access.token.claim
01a3be18-a127-491b-8ad4-dd313728b23c	gender	claim.name
01a3be18-a127-491b-8ad4-dd313728b23c	String	jsonType.label
0d5c2ba1-ec39-4a99-8f95-58452c73051c	true	introspection.token.claim
0d5c2ba1-ec39-4a99-8f95-58452c73051c	true	userinfo.token.claim
0d5c2ba1-ec39-4a99-8f95-58452c73051c	username	user.attribute
0d5c2ba1-ec39-4a99-8f95-58452c73051c	true	id.token.claim
0d5c2ba1-ec39-4a99-8f95-58452c73051c	true	access.token.claim
0d5c2ba1-ec39-4a99-8f95-58452c73051c	preferred_username	claim.name
0d5c2ba1-ec39-4a99-8f95-58452c73051c	String	jsonType.label
2dc15d60-e942-4713-a248-6d62f08b6d56	true	introspection.token.claim
2dc15d60-e942-4713-a248-6d62f08b6d56	true	userinfo.token.claim
2dc15d60-e942-4713-a248-6d62f08b6d56	locale	user.attribute
2dc15d60-e942-4713-a248-6d62f08b6d56	true	id.token.claim
2dc15d60-e942-4713-a248-6d62f08b6d56	true	access.token.claim
2dc15d60-e942-4713-a248-6d62f08b6d56	locale	claim.name
2dc15d60-e942-4713-a248-6d62f08b6d56	String	jsonType.label
408f216e-c594-4bd1-b299-c0f8072f6b4d	true	introspection.token.claim
408f216e-c594-4bd1-b299-c0f8072f6b4d	true	userinfo.token.claim
408f216e-c594-4bd1-b299-c0f8072f6b4d	nickname	user.attribute
408f216e-c594-4bd1-b299-c0f8072f6b4d	true	id.token.claim
408f216e-c594-4bd1-b299-c0f8072f6b4d	true	access.token.claim
408f216e-c594-4bd1-b299-c0f8072f6b4d	nickname	claim.name
408f216e-c594-4bd1-b299-c0f8072f6b4d	String	jsonType.label
6ff0b1d9-cea2-48bc-b3d8-b76bda9a9643	true	introspection.token.claim
6ff0b1d9-cea2-48bc-b3d8-b76bda9a9643	true	userinfo.token.claim
6ff0b1d9-cea2-48bc-b3d8-b76bda9a9643	firstName	user.attribute
6ff0b1d9-cea2-48bc-b3d8-b76bda9a9643	true	id.token.claim
6ff0b1d9-cea2-48bc-b3d8-b76bda9a9643	true	access.token.claim
6ff0b1d9-cea2-48bc-b3d8-b76bda9a9643	given_name	claim.name
6ff0b1d9-cea2-48bc-b3d8-b76bda9a9643	String	jsonType.label
74f03892-3ff6-4b18-a9cb-31ff52db4434	true	introspection.token.claim
74f03892-3ff6-4b18-a9cb-31ff52db4434	true	userinfo.token.claim
74f03892-3ff6-4b18-a9cb-31ff52db4434	true	id.token.claim
74f03892-3ff6-4b18-a9cb-31ff52db4434	true	access.token.claim
77b5ebe2-52e6-4284-ade4-e9197a1028af	true	introspection.token.claim
77b5ebe2-52e6-4284-ade4-e9197a1028af	true	userinfo.token.claim
77b5ebe2-52e6-4284-ade4-e9197a1028af	updatedAt	user.attribute
77b5ebe2-52e6-4284-ade4-e9197a1028af	true	id.token.claim
77b5ebe2-52e6-4284-ade4-e9197a1028af	true	access.token.claim
77b5ebe2-52e6-4284-ade4-e9197a1028af	updated_at	claim.name
77b5ebe2-52e6-4284-ade4-e9197a1028af	long	jsonType.label
96b30c31-bc14-46e8-9ae1-bb836d5a79c6	true	introspection.token.claim
96b30c31-bc14-46e8-9ae1-bb836d5a79c6	true	userinfo.token.claim
96b30c31-bc14-46e8-9ae1-bb836d5a79c6	picture	user.attribute
96b30c31-bc14-46e8-9ae1-bb836d5a79c6	true	id.token.claim
96b30c31-bc14-46e8-9ae1-bb836d5a79c6	true	access.token.claim
96b30c31-bc14-46e8-9ae1-bb836d5a79c6	picture	claim.name
96b30c31-bc14-46e8-9ae1-bb836d5a79c6	String	jsonType.label
9789cbae-a343-43d7-a161-e79162ff3b75	true	introspection.token.claim
9789cbae-a343-43d7-a161-e79162ff3b75	true	userinfo.token.claim
9789cbae-a343-43d7-a161-e79162ff3b75	website	user.attribute
9789cbae-a343-43d7-a161-e79162ff3b75	true	id.token.claim
9789cbae-a343-43d7-a161-e79162ff3b75	true	access.token.claim
9789cbae-a343-43d7-a161-e79162ff3b75	website	claim.name
9789cbae-a343-43d7-a161-e79162ff3b75	String	jsonType.label
a6585b89-f4d5-40af-9f3c-8dad5f15745a	true	introspection.token.claim
a6585b89-f4d5-40af-9f3c-8dad5f15745a	true	userinfo.token.claim
a6585b89-f4d5-40af-9f3c-8dad5f15745a	lastName	user.attribute
a6585b89-f4d5-40af-9f3c-8dad5f15745a	true	id.token.claim
a6585b89-f4d5-40af-9f3c-8dad5f15745a	true	access.token.claim
a6585b89-f4d5-40af-9f3c-8dad5f15745a	family_name	claim.name
a6585b89-f4d5-40af-9f3c-8dad5f15745a	String	jsonType.label
b78a1e40-b7b3-4d71-89b6-2f7755780f27	true	introspection.token.claim
b78a1e40-b7b3-4d71-89b6-2f7755780f27	true	userinfo.token.claim
b78a1e40-b7b3-4d71-89b6-2f7755780f27	middleName	user.attribute
b78a1e40-b7b3-4d71-89b6-2f7755780f27	true	id.token.claim
b78a1e40-b7b3-4d71-89b6-2f7755780f27	true	access.token.claim
b78a1e40-b7b3-4d71-89b6-2f7755780f27	middle_name	claim.name
b78a1e40-b7b3-4d71-89b6-2f7755780f27	String	jsonType.label
bbcf3685-cf64-4932-b0fa-4f7c8849e4ad	true	introspection.token.claim
bbcf3685-cf64-4932-b0fa-4f7c8849e4ad	true	userinfo.token.claim
bbcf3685-cf64-4932-b0fa-4f7c8849e4ad	profile	user.attribute
bbcf3685-cf64-4932-b0fa-4f7c8849e4ad	true	id.token.claim
bbcf3685-cf64-4932-b0fa-4f7c8849e4ad	true	access.token.claim
bbcf3685-cf64-4932-b0fa-4f7c8849e4ad	profile	claim.name
bbcf3685-cf64-4932-b0fa-4f7c8849e4ad	String	jsonType.label
c186469c-ec75-49d6-93d3-431be8f2fd75	true	introspection.token.claim
c186469c-ec75-49d6-93d3-431be8f2fd75	true	userinfo.token.claim
c186469c-ec75-49d6-93d3-431be8f2fd75	birthdate	user.attribute
c186469c-ec75-49d6-93d3-431be8f2fd75	true	id.token.claim
c186469c-ec75-49d6-93d3-431be8f2fd75	true	access.token.claim
c186469c-ec75-49d6-93d3-431be8f2fd75	birthdate	claim.name
c186469c-ec75-49d6-93d3-431be8f2fd75	String	jsonType.label
f0fccd6f-dc17-4a84-a939-65477a93b9fd	true	introspection.token.claim
f0fccd6f-dc17-4a84-a939-65477a93b9fd	true	userinfo.token.claim
f0fccd6f-dc17-4a84-a939-65477a93b9fd	zoneinfo	user.attribute
f0fccd6f-dc17-4a84-a939-65477a93b9fd	true	id.token.claim
f0fccd6f-dc17-4a84-a939-65477a93b9fd	true	access.token.claim
f0fccd6f-dc17-4a84-a939-65477a93b9fd	zoneinfo	claim.name
f0fccd6f-dc17-4a84-a939-65477a93b9fd	String	jsonType.label
758135d8-c4ff-42a5-be68-3d02ea800f0c	true	introspection.token.claim
758135d8-c4ff-42a5-be68-3d02ea800f0c	true	userinfo.token.claim
758135d8-c4ff-42a5-be68-3d02ea800f0c	emailVerified	user.attribute
758135d8-c4ff-42a5-be68-3d02ea800f0c	true	id.token.claim
758135d8-c4ff-42a5-be68-3d02ea800f0c	true	access.token.claim
758135d8-c4ff-42a5-be68-3d02ea800f0c	email_verified	claim.name
758135d8-c4ff-42a5-be68-3d02ea800f0c	boolean	jsonType.label
bb090263-d375-4457-9255-27be4b34d8c7	true	introspection.token.claim
bb090263-d375-4457-9255-27be4b34d8c7	true	userinfo.token.claim
bb090263-d375-4457-9255-27be4b34d8c7	email	user.attribute
bb090263-d375-4457-9255-27be4b34d8c7	true	id.token.claim
bb090263-d375-4457-9255-27be4b34d8c7	true	access.token.claim
bb090263-d375-4457-9255-27be4b34d8c7	email	claim.name
bb090263-d375-4457-9255-27be4b34d8c7	String	jsonType.label
241667b9-f39f-4154-b843-c6e4e125ccad	formatted	user.attribute.formatted
241667b9-f39f-4154-b843-c6e4e125ccad	country	user.attribute.country
241667b9-f39f-4154-b843-c6e4e125ccad	true	introspection.token.claim
241667b9-f39f-4154-b843-c6e4e125ccad	postal_code	user.attribute.postal_code
241667b9-f39f-4154-b843-c6e4e125ccad	true	userinfo.token.claim
241667b9-f39f-4154-b843-c6e4e125ccad	street	user.attribute.street
241667b9-f39f-4154-b843-c6e4e125ccad	true	id.token.claim
241667b9-f39f-4154-b843-c6e4e125ccad	region	user.attribute.region
241667b9-f39f-4154-b843-c6e4e125ccad	true	access.token.claim
241667b9-f39f-4154-b843-c6e4e125ccad	locality	user.attribute.locality
1e722767-7cac-4b25-927b-75f6c9f1c4f9	true	introspection.token.claim
1e722767-7cac-4b25-927b-75f6c9f1c4f9	true	userinfo.token.claim
1e722767-7cac-4b25-927b-75f6c9f1c4f9	phoneNumberVerified	user.attribute
1e722767-7cac-4b25-927b-75f6c9f1c4f9	true	id.token.claim
1e722767-7cac-4b25-927b-75f6c9f1c4f9	true	access.token.claim
1e722767-7cac-4b25-927b-75f6c9f1c4f9	phone_number_verified	claim.name
1e722767-7cac-4b25-927b-75f6c9f1c4f9	boolean	jsonType.label
4d46932c-309f-486f-8318-2c66f94d4a36	true	introspection.token.claim
4d46932c-309f-486f-8318-2c66f94d4a36	true	userinfo.token.claim
4d46932c-309f-486f-8318-2c66f94d4a36	phoneNumber	user.attribute
4d46932c-309f-486f-8318-2c66f94d4a36	true	id.token.claim
4d46932c-309f-486f-8318-2c66f94d4a36	true	access.token.claim
4d46932c-309f-486f-8318-2c66f94d4a36	phone_number	claim.name
4d46932c-309f-486f-8318-2c66f94d4a36	String	jsonType.label
120eb7c4-3af0-4f9e-93f6-4335a7f4a43e	true	introspection.token.claim
120eb7c4-3af0-4f9e-93f6-4335a7f4a43e	true	multivalued
120eb7c4-3af0-4f9e-93f6-4335a7f4a43e	foo	user.attribute
120eb7c4-3af0-4f9e-93f6-4335a7f4a43e	true	access.token.claim
120eb7c4-3af0-4f9e-93f6-4335a7f4a43e	resource_access.${client_id}.roles	claim.name
120eb7c4-3af0-4f9e-93f6-4335a7f4a43e	String	jsonType.label
245c24cf-ee5e-4c30-b68b-e2d18a703bee	true	introspection.token.claim
245c24cf-ee5e-4c30-b68b-e2d18a703bee	true	multivalued
245c24cf-ee5e-4c30-b68b-e2d18a703bee	foo	user.attribute
245c24cf-ee5e-4c30-b68b-e2d18a703bee	true	access.token.claim
245c24cf-ee5e-4c30-b68b-e2d18a703bee	realm_access.roles	claim.name
245c24cf-ee5e-4c30-b68b-e2d18a703bee	String	jsonType.label
7931d561-24ed-4f67-beb4-41b153f17b79	true	introspection.token.claim
7931d561-24ed-4f67-beb4-41b153f17b79	true	access.token.claim
ffbb1f10-1c04-4306-8d1e-c5184ceb7539	true	introspection.token.claim
ffbb1f10-1c04-4306-8d1e-c5184ceb7539	true	access.token.claim
0cea6507-abb7-41cd-9b81-3faeb5b29741	true	introspection.token.claim
0cea6507-abb7-41cd-9b81-3faeb5b29741	true	multivalued
0cea6507-abb7-41cd-9b81-3faeb5b29741	foo	user.attribute
0cea6507-abb7-41cd-9b81-3faeb5b29741	true	id.token.claim
0cea6507-abb7-41cd-9b81-3faeb5b29741	true	access.token.claim
0cea6507-abb7-41cd-9b81-3faeb5b29741	groups	claim.name
0cea6507-abb7-41cd-9b81-3faeb5b29741	String	jsonType.label
bebbcedd-0e3d-4705-b4cd-f4684676422c	true	introspection.token.claim
bebbcedd-0e3d-4705-b4cd-f4684676422c	true	userinfo.token.claim
bebbcedd-0e3d-4705-b4cd-f4684676422c	username	user.attribute
bebbcedd-0e3d-4705-b4cd-f4684676422c	true	id.token.claim
bebbcedd-0e3d-4705-b4cd-f4684676422c	true	access.token.claim
bebbcedd-0e3d-4705-b4cd-f4684676422c	upn	claim.name
bebbcedd-0e3d-4705-b4cd-f4684676422c	String	jsonType.label
f7a19634-6cf1-42f0-8486-71096245f0fb	true	introspection.token.claim
f7a19634-6cf1-42f0-8486-71096245f0fb	true	id.token.claim
f7a19634-6cf1-42f0-8486-71096245f0fb	true	access.token.claim
54df5b64-7355-43f3-bc89-019459e14caa	AUTH_TIME	user.session.note
54df5b64-7355-43f3-bc89-019459e14caa	true	introspection.token.claim
54df5b64-7355-43f3-bc89-019459e14caa	true	id.token.claim
54df5b64-7355-43f3-bc89-019459e14caa	true	access.token.claim
54df5b64-7355-43f3-bc89-019459e14caa	auth_time	claim.name
54df5b64-7355-43f3-bc89-019459e14caa	long	jsonType.label
ebfe2aa3-52f8-49ab-88a2-a8784e8e0496	true	introspection.token.claim
ebfe2aa3-52f8-49ab-88a2-a8784e8e0496	true	access.token.claim
90e9b12f-241b-4415-a214-a1c027f862f4	true	introspection.token.claim
90e9b12f-241b-4415-a214-a1c027f862f4	true	multivalued
90e9b12f-241b-4415-a214-a1c027f862f4	true	id.token.claim
90e9b12f-241b-4415-a214-a1c027f862f4	true	access.token.claim
90e9b12f-241b-4415-a214-a1c027f862f4	organization	claim.name
90e9b12f-241b-4415-a214-a1c027f862f4	String	jsonType.label
22bcbde9-37c6-4c35-9c96-1cfa39757b81	true	introspection.token.claim
22bcbde9-37c6-4c35-9c96-1cfa39757b81	true	userinfo.token.claim
22bcbde9-37c6-4c35-9c96-1cfa39757b81	locale	user.attribute
22bcbde9-37c6-4c35-9c96-1cfa39757b81	true	id.token.claim
22bcbde9-37c6-4c35-9c96-1cfa39757b81	true	access.token.claim
22bcbde9-37c6-4c35-9c96-1cfa39757b81	locale	claim.name
22bcbde9-37c6-4c35-9c96-1cfa39757b81	String	jsonType.label
05917ef0-bd5d-4599-8f6b-3d6084c332e1	clientAddress	user.session.note
05917ef0-bd5d-4599-8f6b-3d6084c332e1	true	introspection.token.claim
05917ef0-bd5d-4599-8f6b-3d6084c332e1	true	id.token.claim
05917ef0-bd5d-4599-8f6b-3d6084c332e1	true	access.token.claim
05917ef0-bd5d-4599-8f6b-3d6084c332e1	clientAddress	claim.name
05917ef0-bd5d-4599-8f6b-3d6084c332e1	String	jsonType.label
a6fd6bfb-3c9c-4e0a-8344-e8a41f5d6f59	clientHost	user.session.note
a6fd6bfb-3c9c-4e0a-8344-e8a41f5d6f59	true	introspection.token.claim
a6fd6bfb-3c9c-4e0a-8344-e8a41f5d6f59	true	id.token.claim
a6fd6bfb-3c9c-4e0a-8344-e8a41f5d6f59	true	access.token.claim
a6fd6bfb-3c9c-4e0a-8344-e8a41f5d6f59	clientHost	claim.name
a6fd6bfb-3c9c-4e0a-8344-e8a41f5d6f59	String	jsonType.label
b1338b66-b82c-446d-8525-152254c98986	client_id	user.session.note
b1338b66-b82c-446d-8525-152254c98986	true	introspection.token.claim
b1338b66-b82c-446d-8525-152254c98986	true	id.token.claim
b1338b66-b82c-446d-8525-152254c98986	true	access.token.claim
b1338b66-b82c-446d-8525-152254c98986	client_id	claim.name
b1338b66-b82c-446d-8525-152254c98986	String	jsonType.label
\.


--
-- Data for Name: realm; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.realm (id, access_code_lifespan, user_action_lifespan, access_token_lifespan, account_theme, admin_theme, email_theme, enabled, events_enabled, events_expiration, login_theme, name, not_before, password_policy, registration_allowed, remember_me, reset_password_allowed, social, ssl_required, sso_idle_timeout, sso_max_lifespan, update_profile_on_soc_login, verify_email, master_admin_client, login_lifespan, internationalization_enabled, default_locale, reg_email_as_username, admin_events_enabled, admin_events_details_enabled, edit_username_allowed, otp_policy_counter, otp_policy_window, otp_policy_period, otp_policy_digits, otp_policy_alg, otp_policy_type, browser_flow, registration_flow, direct_grant_flow, reset_credentials_flow, client_auth_flow, offline_session_idle_timeout, revoke_refresh_token, access_token_life_implicit, login_with_email_allowed, duplicate_emails_allowed, docker_auth_flow, refresh_token_max_reuse, allow_user_managed_access, sso_max_lifespan_remember_me, sso_idle_timeout_remember_me, default_role) FROM stdin;
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	60	300	60	\N	\N	\N	t	f	0	\N	master	0	\N	f	f	f	f	EXTERNAL	1800	36000	f	f	6b42d9f1-5b82-481b-aab2-4286edf4e8e8	1800	f	\N	f	f	f	f	0	1	30	6	HmacSHA1	totp	9a7daaef-09a5-433e-b0e2-e1e8836a02f3	3d04805b-7311-40e9-bab0-b47d5498efca	61839121-d7d7-4e32-997c-73c9702e9e4a	7a551ae1-4e86-42f4-b2c7-e2eff67c6e56	cf1122d5-566a-4245-90e3-730fc1bcc82e	2592000	f	900	t	f	3f7c84bd-487e-4099-bc35-e8a1b565613a	0	f	0	0	e52fb623-86f0-4833-9c9b-dbbafbfc4d93
3a9fd404-d708-4128-83e7-ef00fa943752	60	300	300	\N	\N	\N	t	f	0	\N	test	0	\N	f	f	f	f	EXTERNAL	1800	36000	f	f	fcaa2ba0-efe8-4ad8-b7e8-6fd188f409bd	1800	f	\N	f	f	f	f	0	1	30	6	HmacSHA1	totp	b6fce5a0-c2dc-49f2-b23a-082ad7a9c31c	c7cde766-1c06-4f88-9ec2-51c6d758607b	45630bd2-1b7a-490d-b422-cb9e20aedadd	34a61605-dcec-4a72-8b30-8e05a8183588	2a156b38-7495-4c6e-b19d-1d18f71b7b16	2592000	f	900	t	f	0e6f0ff1-abf1-41e6-9cc8-af6689204782	0	f	0	0	42f64d4f-4059-4dca-aab6-44b0725e5469
\.


--
-- Data for Name: realm_attribute; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.realm_attribute (name, realm_id, value) FROM stdin;
cibaBackchannelTokenDeliveryMode	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	poll
cibaExpiresIn	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	120
cibaAuthRequestedUserHint	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	login_hint
parRequestUriLifespan	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	60
cibaInterval	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	5
displayName	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	Keycloak
displayNameHtml	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	<div class="kc-logo-text"><span>Keycloak</span></div>
bruteForceProtected	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	false
permanentLockout	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	false
maxFailureWaitSeconds	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	900
minimumQuickLoginWaitSeconds	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	60
waitIncrementSeconds	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	60
quickLoginCheckMilliSeconds	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	1000
maxDeltaTimeSeconds	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	43200
failureFactor	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	30
actionTokenGeneratedByAdminLifespan	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	43200
actionTokenGeneratedByUserLifespan	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	300
oauth2DeviceCodeLifespan	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	600
oauth2DevicePollingInterval	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	600
defaultSignatureAlgorithm	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	RS256
offlineSessionMaxLifespanEnabled	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	false
offlineSessionMaxLifespan	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	5184000
clientSessionIdleTimeout	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	0
clientSessionMaxLifespan	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	0
clientOfflineSessionIdleTimeout	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	0
clientOfflineSessionMaxLifespan	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	0
webAuthnPolicyRpEntityName	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	keycloak
webAuthnPolicySignatureAlgorithms	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	ES256
webAuthnPolicyRpId	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	
webAuthnPolicyAttestationConveyancePreference	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	not specified
webAuthnPolicyAuthenticatorAttachment	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	not specified
webAuthnPolicyRequireResidentKey	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	not specified
webAuthnPolicyUserVerificationRequirement	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	not specified
webAuthnPolicyCreateTimeout	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	0
webAuthnPolicyAvoidSameAuthenticatorRegister	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	false
webAuthnPolicyRpEntityNamePasswordless	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	keycloak
webAuthnPolicySignatureAlgorithmsPasswordless	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	ES256
webAuthnPolicyRpIdPasswordless	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	
webAuthnPolicyAttestationConveyancePreferencePasswordless	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	not specified
webAuthnPolicyAuthenticatorAttachmentPasswordless	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	not specified
webAuthnPolicyRequireResidentKeyPasswordless	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	not specified
webAuthnPolicyUserVerificationRequirementPasswordless	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	not specified
webAuthnPolicyCreateTimeoutPasswordless	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	0
webAuthnPolicyAvoidSameAuthenticatorRegisterPasswordless	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	false
client-policies.profiles	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	{"profiles":[]}
client-policies.policies	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	{"policies":[]}
firstBrokerLoginFlowId	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	7f718844-dfa8-4d65-8a02-aca6ed4c42cf
maxTemporaryLockouts	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	0
bruteForceStrategy	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	MULTIPLE
organizationsEnabled	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	false
realmReusableOtpCode	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	false
_browser_header.contentSecurityPolicyReportOnly	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	
_browser_header.xContentTypeOptions	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	nosniff
_browser_header.xRobotsTag	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	none
_browser_header.xFrameOptions	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	SAMEORIGIN
_browser_header.contentSecurityPolicy	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	frame-src 'self'; frame-ancestors 'self'; object-src 'none';
_browser_header.xXSSProtection	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	1; mode=block
_browser_header.strictTransportSecurity	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	max-age=31536000; includeSubDomains
bruteForceProtected	3a9fd404-d708-4128-83e7-ef00fa943752	false
permanentLockout	3a9fd404-d708-4128-83e7-ef00fa943752	false
maxTemporaryLockouts	3a9fd404-d708-4128-83e7-ef00fa943752	0
bruteForceStrategy	3a9fd404-d708-4128-83e7-ef00fa943752	MULTIPLE
maxFailureWaitSeconds	3a9fd404-d708-4128-83e7-ef00fa943752	900
minimumQuickLoginWaitSeconds	3a9fd404-d708-4128-83e7-ef00fa943752	60
waitIncrementSeconds	3a9fd404-d708-4128-83e7-ef00fa943752	60
quickLoginCheckMilliSeconds	3a9fd404-d708-4128-83e7-ef00fa943752	1000
maxDeltaTimeSeconds	3a9fd404-d708-4128-83e7-ef00fa943752	43200
failureFactor	3a9fd404-d708-4128-83e7-ef00fa943752	30
realmReusableOtpCode	3a9fd404-d708-4128-83e7-ef00fa943752	false
defaultSignatureAlgorithm	3a9fd404-d708-4128-83e7-ef00fa943752	RS256
offlineSessionMaxLifespanEnabled	3a9fd404-d708-4128-83e7-ef00fa943752	false
offlineSessionMaxLifespan	3a9fd404-d708-4128-83e7-ef00fa943752	5184000
actionTokenGeneratedByAdminLifespan	3a9fd404-d708-4128-83e7-ef00fa943752	43200
actionTokenGeneratedByUserLifespan	3a9fd404-d708-4128-83e7-ef00fa943752	300
oauth2DeviceCodeLifespan	3a9fd404-d708-4128-83e7-ef00fa943752	600
oauth2DevicePollingInterval	3a9fd404-d708-4128-83e7-ef00fa943752	5
webAuthnPolicyRpEntityName	3a9fd404-d708-4128-83e7-ef00fa943752	keycloak
webAuthnPolicySignatureAlgorithms	3a9fd404-d708-4128-83e7-ef00fa943752	ES256,RS256
webAuthnPolicyRpId	3a9fd404-d708-4128-83e7-ef00fa943752	
webAuthnPolicyAttestationConveyancePreference	3a9fd404-d708-4128-83e7-ef00fa943752	not specified
webAuthnPolicyAuthenticatorAttachment	3a9fd404-d708-4128-83e7-ef00fa943752	not specified
webAuthnPolicyRequireResidentKey	3a9fd404-d708-4128-83e7-ef00fa943752	not specified
webAuthnPolicyUserVerificationRequirement	3a9fd404-d708-4128-83e7-ef00fa943752	not specified
webAuthnPolicyCreateTimeout	3a9fd404-d708-4128-83e7-ef00fa943752	0
webAuthnPolicyAvoidSameAuthenticatorRegister	3a9fd404-d708-4128-83e7-ef00fa943752	false
webAuthnPolicyRpEntityNamePasswordless	3a9fd404-d708-4128-83e7-ef00fa943752	keycloak
webAuthnPolicySignatureAlgorithmsPasswordless	3a9fd404-d708-4128-83e7-ef00fa943752	ES256,RS256
webAuthnPolicyRpIdPasswordless	3a9fd404-d708-4128-83e7-ef00fa943752	
webAuthnPolicyAttestationConveyancePreferencePasswordless	3a9fd404-d708-4128-83e7-ef00fa943752	not specified
webAuthnPolicyAuthenticatorAttachmentPasswordless	3a9fd404-d708-4128-83e7-ef00fa943752	not specified
webAuthnPolicyRequireResidentKeyPasswordless	3a9fd404-d708-4128-83e7-ef00fa943752	not specified
webAuthnPolicyUserVerificationRequirementPasswordless	3a9fd404-d708-4128-83e7-ef00fa943752	not specified
webAuthnPolicyCreateTimeoutPasswordless	3a9fd404-d708-4128-83e7-ef00fa943752	0
webAuthnPolicyAvoidSameAuthenticatorRegisterPasswordless	3a9fd404-d708-4128-83e7-ef00fa943752	false
cibaBackchannelTokenDeliveryMode	3a9fd404-d708-4128-83e7-ef00fa943752	poll
cibaExpiresIn	3a9fd404-d708-4128-83e7-ef00fa943752	120
cibaInterval	3a9fd404-d708-4128-83e7-ef00fa943752	5
cibaAuthRequestedUserHint	3a9fd404-d708-4128-83e7-ef00fa943752	login_hint
parRequestUriLifespan	3a9fd404-d708-4128-83e7-ef00fa943752	60
firstBrokerLoginFlowId	3a9fd404-d708-4128-83e7-ef00fa943752	5df8553d-b6e0-4e80-b28e-01509fe3f789
frontendUrl	3a9fd404-d708-4128-83e7-ef00fa943752	
acr.loa.map	3a9fd404-d708-4128-83e7-ef00fa943752	{}
displayName	3a9fd404-d708-4128-83e7-ef00fa943752	
displayNameHtml	3a9fd404-d708-4128-83e7-ef00fa943752	
organizationsEnabled	3a9fd404-d708-4128-83e7-ef00fa943752	false
clientSessionIdleTimeout	3a9fd404-d708-4128-83e7-ef00fa943752	0
clientSessionMaxLifespan	3a9fd404-d708-4128-83e7-ef00fa943752	0
clientOfflineSessionIdleTimeout	3a9fd404-d708-4128-83e7-ef00fa943752	0
clientOfflineSessionMaxLifespan	3a9fd404-d708-4128-83e7-ef00fa943752	0
client-policies.profiles	3a9fd404-d708-4128-83e7-ef00fa943752	{"profiles":[]}
client-policies.policies	3a9fd404-d708-4128-83e7-ef00fa943752	{"policies":[]}
_browser_header.contentSecurityPolicyReportOnly	3a9fd404-d708-4128-83e7-ef00fa943752	
_browser_header.xContentTypeOptions	3a9fd404-d708-4128-83e7-ef00fa943752	nosniff
_browser_header.referrerPolicy	3a9fd404-d708-4128-83e7-ef00fa943752	no-referrer
_browser_header.xRobotsTag	3a9fd404-d708-4128-83e7-ef00fa943752	none
_browser_header.xFrameOptions	3a9fd404-d708-4128-83e7-ef00fa943752	SAMEORIGIN
_browser_header.contentSecurityPolicy	3a9fd404-d708-4128-83e7-ef00fa943752	frame-src 'self'; frame-ancestors 'self'; object-src 'none';
_browser_header.xXSSProtection	3a9fd404-d708-4128-83e7-ef00fa943752	1; mode=block
_browser_header.strictTransportSecurity	3a9fd404-d708-4128-83e7-ef00fa943752	max-age=31536000; includeSubDomains
\.


--
-- Data for Name: realm_default_groups; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.realm_default_groups (realm_id, group_id) FROM stdin;
\.


--
-- Data for Name: realm_enabled_event_types; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.realm_enabled_event_types (realm_id, value) FROM stdin;
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	UPDATE_CONSENT_ERROR
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	SEND_RESET_PASSWORD
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	GRANT_CONSENT
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	VERIFY_PROFILE_ERROR
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	UPDATE_TOTP
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	REMOVE_TOTP
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	REVOKE_GRANT
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	LOGIN_ERROR
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	CLIENT_LOGIN
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	RESET_PASSWORD_ERROR
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	IMPERSONATE_ERROR
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	CODE_TO_TOKEN_ERROR
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	CUSTOM_REQUIRED_ACTION
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	OAUTH2_DEVICE_CODE_TO_TOKEN_ERROR
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	RESTART_AUTHENTICATION
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	UPDATE_PROFILE_ERROR
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	IMPERSONATE
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	LOGIN
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	UPDATE_PASSWORD_ERROR
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	OAUTH2_DEVICE_VERIFY_USER_CODE
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	CLIENT_INITIATED_ACCOUNT_LINKING
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	TOKEN_EXCHANGE
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	REGISTER
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	LOGOUT
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	AUTHREQID_TO_TOKEN
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	DELETE_ACCOUNT_ERROR
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	CLIENT_REGISTER
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	IDENTITY_PROVIDER_LINK_ACCOUNT
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	UPDATE_PASSWORD
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	DELETE_ACCOUNT
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	FEDERATED_IDENTITY_LINK_ERROR
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	CLIENT_DELETE
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	IDENTITY_PROVIDER_FIRST_LOGIN
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	VERIFY_EMAIL
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	CLIENT_DELETE_ERROR
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	CLIENT_LOGIN_ERROR
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	RESTART_AUTHENTICATION_ERROR
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	REMOVE_FEDERATED_IDENTITY_ERROR
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	EXECUTE_ACTIONS
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	TOKEN_EXCHANGE_ERROR
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	PERMISSION_TOKEN
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	SEND_IDENTITY_PROVIDER_LINK_ERROR
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	EXECUTE_ACTION_TOKEN_ERROR
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	SEND_VERIFY_EMAIL
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	OAUTH2_DEVICE_AUTH
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	EXECUTE_ACTIONS_ERROR
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	REMOVE_FEDERATED_IDENTITY
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	OAUTH2_DEVICE_CODE_TO_TOKEN
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	IDENTITY_PROVIDER_POST_LOGIN
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	IDENTITY_PROVIDER_LINK_ACCOUNT_ERROR
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	UPDATE_EMAIL
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	OAUTH2_DEVICE_VERIFY_USER_CODE_ERROR
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	REGISTER_ERROR
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	REVOKE_GRANT_ERROR
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	LOGOUT_ERROR
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	UPDATE_EMAIL_ERROR
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	EXECUTE_ACTION_TOKEN
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	CLIENT_UPDATE_ERROR
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	UPDATE_PROFILE
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	AUTHREQID_TO_TOKEN_ERROR
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	FEDERATED_IDENTITY_LINK
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	CLIENT_REGISTER_ERROR
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	SEND_VERIFY_EMAIL_ERROR
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	SEND_IDENTITY_PROVIDER_LINK
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	RESET_PASSWORD
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	CLIENT_INITIATED_ACCOUNT_LINKING_ERROR
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	OAUTH2_DEVICE_AUTH_ERROR
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	UPDATE_CONSENT
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	REMOVE_TOTP_ERROR
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	VERIFY_EMAIL_ERROR
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	SEND_RESET_PASSWORD_ERROR
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	CLIENT_UPDATE
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	IDENTITY_PROVIDER_POST_LOGIN_ERROR
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	CUSTOM_REQUIRED_ACTION_ERROR
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	UPDATE_TOTP_ERROR
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	CODE_TO_TOKEN
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	VERIFY_PROFILE
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	GRANT_CONSENT_ERROR
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	IDENTITY_PROVIDER_FIRST_LOGIN_ERROR
\.


--
-- Data for Name: realm_events_listeners; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.realm_events_listeners (realm_id, value) FROM stdin;
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	jboss-logging
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	opfa-login-stats
3a9fd404-d708-4128-83e7-ef00fa943752	jboss-logging
\.


--
-- Data for Name: realm_localizations; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.realm_localizations (realm_id, locale, texts) FROM stdin;
\.


--
-- Data for Name: realm_required_credential; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.realm_required_credential (type, form_label, input, secret, realm_id) FROM stdin;
password	password	t	t	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e
password	password	t	t	3a9fd404-d708-4128-83e7-ef00fa943752
\.


--
-- Data for Name: realm_smtp_config; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.realm_smtp_config (realm_id, value, name) FROM stdin;
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e		replyToDisplayName
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e		starttls
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e		auth
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	2525	port
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	172.18.26.4	host
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e		replyTo
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	test@example.com	from
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e		fromDisplayName
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e		ssl
86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e		envelopeFrom
\.


--
-- Data for Name: realm_supported_locales; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.realm_supported_locales (realm_id, value) FROM stdin;
\.


--
-- Data for Name: redirect_uris; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.redirect_uris (client_id, value) FROM stdin;
5faa1af4-99ee-4664-83c0-ada476a835be	/realms/master/account/*
3cbf8aa7-c144-492c-aaf9-859a636cce37	/realms/master/account/*
193c8e25-0d00-4c45-ad5c-fa2a6e50d8f6	/admin/master/console/*
9d44bf2d-8bad-41b0-a76d-9ef30b0ce5bf	/realms/test/account/*
7b5da870-582a-4bd2-9320-84172f977b88	/realms/test/account/*
ca3d0ca8-abd5-4605-80df-b32fa0e45a83	/admin/test/console/*
27b6abbc-155f-4768-9200-8721b6d0831f	http://localhost:8000/*
\.


--
-- Data for Name: required_action_config; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.required_action_config (required_action_id, value, name) FROM stdin;
\.


--
-- Data for Name: required_action_provider; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.required_action_provider (id, alias, name, realm_id, enabled, default_action, provider_id, priority) FROM stdin;
79841834-806d-4d91-a771-4ac3ced85a34	VERIFY_EMAIL	Verify Email	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	t	f	VERIFY_EMAIL	50
6492f0ae-0afe-4e6c-b306-91f2c16b05cf	UPDATE_PROFILE	Update Profile	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	t	f	UPDATE_PROFILE	40
698a3bca-8bc0-4d80-a696-490fa4bf2b66	CONFIGURE_TOTP	Configure OTP	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	t	f	CONFIGURE_TOTP	10
bee90d3c-c6a7-4490-bcd3-8cf438277e43	UPDATE_PASSWORD	Update Password	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	t	f	UPDATE_PASSWORD	30
03ea97c9-cbc8-4536-a795-569225dbbe49	update_user_locale	Update User Locale	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	t	f	update_user_locale	1000
17b95cc1-f508-458e-a46e-21c2b1f4c6bb	delete_account	Delete Account	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	f	f	delete_account	60
f9a01b8e-3b32-42ea-b922-2f20dad2ec86	TERMS_AND_CONDITIONS	Terms and Conditions	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	f	f	TERMS_AND_CONDITIONS	20
c326c23c-e4ff-44c3-80d7-7514d17a3d4f	delete_credential	Delete Credential	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	t	f	delete_credential	100
2a0aaec7-92e9-4721-9afb-867fdc6909d9	VERIFY_EMAIL	Verify Email	3a9fd404-d708-4128-83e7-ef00fa943752	t	f	VERIFY_EMAIL	50
cc6da090-933a-4d5e-a2d6-e45bb87a5c7c	UPDATE_PROFILE	Update Profile	3a9fd404-d708-4128-83e7-ef00fa943752	t	f	UPDATE_PROFILE	40
e031022a-3f77-490d-baf2-6ce8e9becc9d	CONFIGURE_TOTP	Configure OTP	3a9fd404-d708-4128-83e7-ef00fa943752	t	f	CONFIGURE_TOTP	10
f4139ad0-ef81-44f9-8cb8-3bff98111a4c	UPDATE_PASSWORD	Update Password	3a9fd404-d708-4128-83e7-ef00fa943752	t	f	UPDATE_PASSWORD	30
fec56dd9-3ae2-4f52-8d6e-9fb70ccecd31	TERMS_AND_CONDITIONS	Terms and Conditions	3a9fd404-d708-4128-83e7-ef00fa943752	f	f	TERMS_AND_CONDITIONS	20
eeb34b73-0ced-429f-bf8c-e4d77dc23ddb	delete_account	Delete Account	3a9fd404-d708-4128-83e7-ef00fa943752	f	f	delete_account	60
c7764322-a3d8-45e0-aeb6-5a4fcaedf8f9	delete_credential	Delete Credential	3a9fd404-d708-4128-83e7-ef00fa943752	t	f	delete_credential	100
5976fe73-0d8d-4fea-8a83-86b9e9228ddb	update_user_locale	Update User Locale	3a9fd404-d708-4128-83e7-ef00fa943752	t	f	update_user_locale	1000
febc1a1e-489e-4ae6-bad5-af9e49c0a211	UPDATE_EMAIL	Update Email	3a9fd404-d708-4128-83e7-ef00fa943752	t	f	UPDATE_EMAIL	70
c4550ebb-e853-4d37-8c63-2dd4d98ab9d2	webauthn-register	Webauthn Register	3a9fd404-d708-4128-83e7-ef00fa943752	t	f	webauthn-register	70
80c97590-7bad-4999-bb7e-3f70f9558bdc	webauthn-register-passwordless	Webauthn Register Passwordless	3a9fd404-d708-4128-83e7-ef00fa943752	t	f	webauthn-register-passwordless	80
59546164-4839-4c05-b750-a9380a1e19d7	VERIFY_PROFILE	Verify Profile	3a9fd404-d708-4128-83e7-ef00fa943752	t	f	VERIFY_PROFILE	90
\.


--
-- Data for Name: resource_attribute; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.resource_attribute (id, name, value, resource_id) FROM stdin;
\.


--
-- Data for Name: resource_policy; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.resource_policy (resource_id, policy_id) FROM stdin;
\.


--
-- Data for Name: resource_scope; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.resource_scope (resource_id, scope_id) FROM stdin;
\.


--
-- Data for Name: resource_server; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.resource_server (id, allow_rs_remote_mgmt, policy_enforce_mode, decision_strategy) FROM stdin;
\.


--
-- Data for Name: resource_server_perm_ticket; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.resource_server_perm_ticket (id, owner, requester, created_timestamp, granted_timestamp, resource_id, scope_id, resource_server_id, policy_id) FROM stdin;
\.


--
-- Data for Name: resource_server_policy; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.resource_server_policy (id, name, description, type, decision_strategy, logic, resource_server_id, owner) FROM stdin;
\.


--
-- Data for Name: resource_server_resource; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.resource_server_resource (id, name, type, icon_uri, owner, resource_server_id, owner_managed_access, display_name) FROM stdin;
\.


--
-- Data for Name: resource_server_scope; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.resource_server_scope (id, name, icon_uri, resource_server_id, display_name) FROM stdin;
\.


--
-- Data for Name: resource_uris; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.resource_uris (resource_id, value) FROM stdin;
\.


--
-- Data for Name: revoked_token; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.revoked_token (id, expire) FROM stdin;
\.


--
-- Data for Name: role_attribute; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.role_attribute (id, role_id, name, value) FROM stdin;
\.


--
-- Data for Name: scope_mapping; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.scope_mapping (client_id, role_id) FROM stdin;
3cbf8aa7-c144-492c-aaf9-859a636cce37	8d61b16e-aeeb-4cf1-b37e-3c56c3b0c2f3
3cbf8aa7-c144-492c-aaf9-859a636cce37	cc41a34b-4e7b-4396-8274-808c8c8b4ff0
7b5da870-582a-4bd2-9320-84172f977b88	8c1d2735-c84c-498b-978b-90b94a39243e
7b5da870-582a-4bd2-9320-84172f977b88	898d7633-0975-41f6-989a-d2e661311e9d
\.


--
-- Data for Name: scope_policy; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.scope_policy (scope_id, policy_id) FROM stdin;
\.


--
-- Data for Name: user_attribute; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.user_attribute (name, value, user_id, id, long_value_hash, long_value_hash_lower_case, long_value) FROM stdin;
TEST_ATTRIBUTE	TEST_VALUE	3d0bd8f9-ad4a-4d08-babb-14a48f210450	61557f45-b1e4-4952-a247-24523cb49a89	\N	\N	\N
loginStats	5:1653654922871:1732886498097	dfef0e06-05d1-4103-a8a4-f3a19a7e2802	631821c3-f3ca-42b8-8876-99fcda4149c8	\N	\N	\N
\.


--
-- Data for Name: user_consent; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.user_consent (id, client_id, user_id, created_date, last_updated_date, client_storage_provider, external_client_id) FROM stdin;
\.


--
-- Data for Name: user_consent_client_scope; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.user_consent_client_scope (user_consent_id, scope_id) FROM stdin;
\.


--
-- Data for Name: user_entity; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.user_entity (id, email, email_constraint, email_verified, enabled, federation_link, first_name, last_name, realm_id, username, created_timestamp, service_account_client_link, not_before) FROM stdin;
dfef0e06-05d1-4103-a8a4-f3a19a7e2802	admin@example.com	admin@example.com	t	t	\N	admin	admin	86f52c97-a5c0-4a5c-8da8-3ff79cf7fa3e	admin@example.com	1653471749135	\N	0
3d0bd8f9-ad4a-4d08-babb-14a48f210450	user@example.com	user@example.com	t	t	\N	John	Doe	3a9fd404-d708-4128-83e7-ef00fa943752	user@example.com	1732813204458	\N	0
23a66fda-0ffd-4e2c-8540-d70c37a287ce	\N	21fb32dd-c747-4b8e-bba6-50b3fe38cc87	f	t	\N	\N	\N	3a9fd404-d708-4128-83e7-ef00fa943752	service-account-test-sa-view-client	1732813418138	27b6abbc-155f-4768-9200-8721b6d0831f	0
\.


--
-- Data for Name: user_federation_config; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.user_federation_config (user_federation_provider_id, value, name) FROM stdin;
\.


--
-- Data for Name: user_federation_mapper; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.user_federation_mapper (id, name, federation_provider_id, federation_mapper_type, realm_id) FROM stdin;
\.


--
-- Data for Name: user_federation_mapper_config; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.user_federation_mapper_config (user_federation_mapper_id, value, name) FROM stdin;
\.


--
-- Data for Name: user_federation_provider; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.user_federation_provider (id, changed_sync_period, display_name, full_sync_period, last_sync, priority, provider_name, realm_id) FROM stdin;
\.


--
-- Data for Name: user_group_membership; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.user_group_membership (group_id, user_id, membership_type) FROM stdin;
55488dd2-11e5-474e-a2d3-63fa4f98b19a	3d0bd8f9-ad4a-4d08-babb-14a48f210450	UNMANAGED
\.


--
-- Data for Name: user_required_action; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.user_required_action (user_id, required_action) FROM stdin;
\.


--
-- Data for Name: user_role_mapping; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.user_role_mapping (role_id, user_id) FROM stdin;
e52fb623-86f0-4833-9c9b-dbbafbfc4d93	dfef0e06-05d1-4103-a8a4-f3a19a7e2802
7f6e982b-20ec-4574-a4d5-539f845a770e	dfef0e06-05d1-4103-a8a4-f3a19a7e2802
42f64d4f-4059-4dca-aab6-44b0725e5469	3d0bd8f9-ad4a-4d08-babb-14a48f210450
42f64d4f-4059-4dca-aab6-44b0725e5469	23a66fda-0ffd-4e2c-8540-d70c37a287ce
305213e3-bf40-419d-ae7a-09e53a8c8681	23a66fda-0ffd-4e2c-8540-d70c37a287ce
\.


--
-- Data for Name: username_login_failure; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.username_login_failure (realm_id, username, failed_login_not_before, last_failure, last_ip_failure, num_failures) FROM stdin;
\.


--
-- Data for Name: web_origins; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.web_origins (client_id, value) FROM stdin;
193c8e25-0d00-4c45-ad5c-fa2a6e50d8f6	+
ca3d0ca8-abd5-4605-80df-b32fa0e45a83	+
27b6abbc-155f-4768-9200-8721b6d0831f	http://localhost:8000
\.


--
-- Name: username_login_failure CONSTRAINT_17-2; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.username_login_failure
    ADD CONSTRAINT "CONSTRAINT_17-2" PRIMARY KEY (realm_id, username);


--
-- Name: org_domain ORG_DOMAIN_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.org_domain
    ADD CONSTRAINT "ORG_DOMAIN_pkey" PRIMARY KEY (id, name);


--
-- Name: org ORG_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.org
    ADD CONSTRAINT "ORG_pkey" PRIMARY KEY (id);


--
-- Name: keycloak_role UK_J3RWUVD56ONTGSUHOGM184WW2-2; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.keycloak_role
    ADD CONSTRAINT "UK_J3RWUVD56ONTGSUHOGM184WW2-2" UNIQUE (name, client_realm_constraint);


--
-- Name: client_auth_flow_bindings c_cli_flow_bind; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.client_auth_flow_bindings
    ADD CONSTRAINT c_cli_flow_bind PRIMARY KEY (client_id, binding_name);


--
-- Name: client_scope_client c_cli_scope_bind; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.client_scope_client
    ADD CONSTRAINT c_cli_scope_bind PRIMARY KEY (client_id, scope_id);


--
-- Name: client_initial_access cnstr_client_init_acc_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.client_initial_access
    ADD CONSTRAINT cnstr_client_init_acc_pk PRIMARY KEY (id);


--
-- Name: realm_default_groups con_group_id_def_groups; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.realm_default_groups
    ADD CONSTRAINT con_group_id_def_groups UNIQUE (group_id);


--
-- Name: broker_link constr_broker_link_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.broker_link
    ADD CONSTRAINT constr_broker_link_pk PRIMARY KEY (identity_provider, user_id);


--
-- Name: component_config constr_component_config_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.component_config
    ADD CONSTRAINT constr_component_config_pk PRIMARY KEY (id);


--
-- Name: component constr_component_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.component
    ADD CONSTRAINT constr_component_pk PRIMARY KEY (id);


--
-- Name: fed_user_required_action constr_fed_required_action; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fed_user_required_action
    ADD CONSTRAINT constr_fed_required_action PRIMARY KEY (required_action, user_id);


--
-- Name: fed_user_attribute constr_fed_user_attr_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fed_user_attribute
    ADD CONSTRAINT constr_fed_user_attr_pk PRIMARY KEY (id);


--
-- Name: fed_user_consent constr_fed_user_consent_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fed_user_consent
    ADD CONSTRAINT constr_fed_user_consent_pk PRIMARY KEY (id);


--
-- Name: fed_user_credential constr_fed_user_cred_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fed_user_credential
    ADD CONSTRAINT constr_fed_user_cred_pk PRIMARY KEY (id);


--
-- Name: fed_user_group_membership constr_fed_user_group; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fed_user_group_membership
    ADD CONSTRAINT constr_fed_user_group PRIMARY KEY (group_id, user_id);


--
-- Name: fed_user_role_mapping constr_fed_user_role; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fed_user_role_mapping
    ADD CONSTRAINT constr_fed_user_role PRIMARY KEY (role_id, user_id);


--
-- Name: federated_user constr_federated_user; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.federated_user
    ADD CONSTRAINT constr_federated_user PRIMARY KEY (id);


--
-- Name: realm_default_groups constr_realm_default_groups; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.realm_default_groups
    ADD CONSTRAINT constr_realm_default_groups PRIMARY KEY (realm_id, group_id);


--
-- Name: realm_enabled_event_types constr_realm_enabl_event_types; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.realm_enabled_event_types
    ADD CONSTRAINT constr_realm_enabl_event_types PRIMARY KEY (realm_id, value);


--
-- Name: realm_events_listeners constr_realm_events_listeners; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.realm_events_listeners
    ADD CONSTRAINT constr_realm_events_listeners PRIMARY KEY (realm_id, value);


--
-- Name: realm_supported_locales constr_realm_supported_locales; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.realm_supported_locales
    ADD CONSTRAINT constr_realm_supported_locales PRIMARY KEY (realm_id, value);


--
-- Name: identity_provider constraint_2b; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.identity_provider
    ADD CONSTRAINT constraint_2b PRIMARY KEY (internal_id);


--
-- Name: client_attributes constraint_3c; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.client_attributes
    ADD CONSTRAINT constraint_3c PRIMARY KEY (client_id, name);


--
-- Name: event_entity constraint_4; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.event_entity
    ADD CONSTRAINT constraint_4 PRIMARY KEY (id);


--
-- Name: federated_identity constraint_40; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.federated_identity
    ADD CONSTRAINT constraint_40 PRIMARY KEY (identity_provider, user_id);


--
-- Name: realm constraint_4a; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.realm
    ADD CONSTRAINT constraint_4a PRIMARY KEY (id);


--
-- Name: user_federation_provider constraint_5c; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_federation_provider
    ADD CONSTRAINT constraint_5c PRIMARY KEY (id);


--
-- Name: client constraint_7; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.client
    ADD CONSTRAINT constraint_7 PRIMARY KEY (id);


--
-- Name: scope_mapping constraint_81; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.scope_mapping
    ADD CONSTRAINT constraint_81 PRIMARY KEY (client_id, role_id);


--
-- Name: client_node_registrations constraint_84; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.client_node_registrations
    ADD CONSTRAINT constraint_84 PRIMARY KEY (client_id, name);


--
-- Name: realm_attribute constraint_9; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.realm_attribute
    ADD CONSTRAINT constraint_9 PRIMARY KEY (name, realm_id);


--
-- Name: realm_required_credential constraint_92; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.realm_required_credential
    ADD CONSTRAINT constraint_92 PRIMARY KEY (realm_id, type);


--
-- Name: keycloak_role constraint_a; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.keycloak_role
    ADD CONSTRAINT constraint_a PRIMARY KEY (id);


--
-- Name: admin_event_entity constraint_admin_event_entity; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.admin_event_entity
    ADD CONSTRAINT constraint_admin_event_entity PRIMARY KEY (id);


--
-- Name: authenticator_config_entry constraint_auth_cfg_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.authenticator_config_entry
    ADD CONSTRAINT constraint_auth_cfg_pk PRIMARY KEY (authenticator_id, name);


--
-- Name: authentication_execution constraint_auth_exec_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.authentication_execution
    ADD CONSTRAINT constraint_auth_exec_pk PRIMARY KEY (id);


--
-- Name: authentication_flow constraint_auth_flow_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.authentication_flow
    ADD CONSTRAINT constraint_auth_flow_pk PRIMARY KEY (id);


--
-- Name: authenticator_config constraint_auth_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.authenticator_config
    ADD CONSTRAINT constraint_auth_pk PRIMARY KEY (id);


--
-- Name: user_role_mapping constraint_c; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_role_mapping
    ADD CONSTRAINT constraint_c PRIMARY KEY (role_id, user_id);


--
-- Name: composite_role constraint_composite_role; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.composite_role
    ADD CONSTRAINT constraint_composite_role PRIMARY KEY (composite, child_role);


--
-- Name: identity_provider_config constraint_d; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.identity_provider_config
    ADD CONSTRAINT constraint_d PRIMARY KEY (identity_provider_id, name);


--
-- Name: policy_config constraint_dpc; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.policy_config
    ADD CONSTRAINT constraint_dpc PRIMARY KEY (policy_id, name);


--
-- Name: realm_smtp_config constraint_e; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.realm_smtp_config
    ADD CONSTRAINT constraint_e PRIMARY KEY (realm_id, name);


--
-- Name: credential constraint_f; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.credential
    ADD CONSTRAINT constraint_f PRIMARY KEY (id);


--
-- Name: user_federation_config constraint_f9; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_federation_config
    ADD CONSTRAINT constraint_f9 PRIMARY KEY (user_federation_provider_id, name);


--
-- Name: resource_server_perm_ticket constraint_fapmt; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.resource_server_perm_ticket
    ADD CONSTRAINT constraint_fapmt PRIMARY KEY (id);


--
-- Name: resource_server_resource constraint_farsr; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.resource_server_resource
    ADD CONSTRAINT constraint_farsr PRIMARY KEY (id);


--
-- Name: resource_server_policy constraint_farsrp; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.resource_server_policy
    ADD CONSTRAINT constraint_farsrp PRIMARY KEY (id);


--
-- Name: associated_policy constraint_farsrpap; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.associated_policy
    ADD CONSTRAINT constraint_farsrpap PRIMARY KEY (policy_id, associated_policy_id);


--
-- Name: resource_policy constraint_farsrpp; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.resource_policy
    ADD CONSTRAINT constraint_farsrpp PRIMARY KEY (resource_id, policy_id);


--
-- Name: resource_server_scope constraint_farsrs; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.resource_server_scope
    ADD CONSTRAINT constraint_farsrs PRIMARY KEY (id);


--
-- Name: resource_scope constraint_farsrsp; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.resource_scope
    ADD CONSTRAINT constraint_farsrsp PRIMARY KEY (resource_id, scope_id);


--
-- Name: scope_policy constraint_farsrsps; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.scope_policy
    ADD CONSTRAINT constraint_farsrsps PRIMARY KEY (scope_id, policy_id);


--
-- Name: user_entity constraint_fb; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_entity
    ADD CONSTRAINT constraint_fb PRIMARY KEY (id);


--
-- Name: user_federation_mapper_config constraint_fedmapper_cfg_pm; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_federation_mapper_config
    ADD CONSTRAINT constraint_fedmapper_cfg_pm PRIMARY KEY (user_federation_mapper_id, name);


--
-- Name: user_federation_mapper constraint_fedmapperpm; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_federation_mapper
    ADD CONSTRAINT constraint_fedmapperpm PRIMARY KEY (id);


--
-- Name: fed_user_consent_cl_scope constraint_fgrntcsnt_clsc_pm; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fed_user_consent_cl_scope
    ADD CONSTRAINT constraint_fgrntcsnt_clsc_pm PRIMARY KEY (user_consent_id, scope_id);


--
-- Name: user_consent_client_scope constraint_grntcsnt_clsc_pm; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_consent_client_scope
    ADD CONSTRAINT constraint_grntcsnt_clsc_pm PRIMARY KEY (user_consent_id, scope_id);


--
-- Name: user_consent constraint_grntcsnt_pm; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_consent
    ADD CONSTRAINT constraint_grntcsnt_pm PRIMARY KEY (id);


--
-- Name: keycloak_group constraint_group; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.keycloak_group
    ADD CONSTRAINT constraint_group PRIMARY KEY (id);


--
-- Name: group_attribute constraint_group_attribute_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.group_attribute
    ADD CONSTRAINT constraint_group_attribute_pk PRIMARY KEY (id);


--
-- Name: group_role_mapping constraint_group_role; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.group_role_mapping
    ADD CONSTRAINT constraint_group_role PRIMARY KEY (role_id, group_id);


--
-- Name: identity_provider_mapper constraint_idpm; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.identity_provider_mapper
    ADD CONSTRAINT constraint_idpm PRIMARY KEY (id);


--
-- Name: idp_mapper_config constraint_idpmconfig; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.idp_mapper_config
    ADD CONSTRAINT constraint_idpmconfig PRIMARY KEY (idp_mapper_id, name);


--
-- Name: migration_model constraint_migmod; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.migration_model
    ADD CONSTRAINT constraint_migmod PRIMARY KEY (id);


--
-- Name: offline_client_session constraint_offl_cl_ses_pk3; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.offline_client_session
    ADD CONSTRAINT constraint_offl_cl_ses_pk3 PRIMARY KEY (user_session_id, client_id, client_storage_provider, external_client_id, offline_flag);


--
-- Name: offline_user_session constraint_offl_us_ses_pk2; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.offline_user_session
    ADD CONSTRAINT constraint_offl_us_ses_pk2 PRIMARY KEY (user_session_id, offline_flag);


--
-- Name: protocol_mapper constraint_pcm; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.protocol_mapper
    ADD CONSTRAINT constraint_pcm PRIMARY KEY (id);


--
-- Name: protocol_mapper_config constraint_pmconfig; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.protocol_mapper_config
    ADD CONSTRAINT constraint_pmconfig PRIMARY KEY (protocol_mapper_id, name);


--
-- Name: redirect_uris constraint_redirect_uris; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.redirect_uris
    ADD CONSTRAINT constraint_redirect_uris PRIMARY KEY (client_id, value);


--
-- Name: required_action_config constraint_req_act_cfg_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.required_action_config
    ADD CONSTRAINT constraint_req_act_cfg_pk PRIMARY KEY (required_action_id, name);


--
-- Name: required_action_provider constraint_req_act_prv_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.required_action_provider
    ADD CONSTRAINT constraint_req_act_prv_pk PRIMARY KEY (id);


--
-- Name: user_required_action constraint_required_action; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_required_action
    ADD CONSTRAINT constraint_required_action PRIMARY KEY (required_action, user_id);


--
-- Name: resource_uris constraint_resour_uris_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.resource_uris
    ADD CONSTRAINT constraint_resour_uris_pk PRIMARY KEY (resource_id, value);


--
-- Name: role_attribute constraint_role_attribute_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.role_attribute
    ADD CONSTRAINT constraint_role_attribute_pk PRIMARY KEY (id);


--
-- Name: revoked_token constraint_rt; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.revoked_token
    ADD CONSTRAINT constraint_rt PRIMARY KEY (id);


--
-- Name: user_attribute constraint_user_attribute_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_attribute
    ADD CONSTRAINT constraint_user_attribute_pk PRIMARY KEY (id);


--
-- Name: user_group_membership constraint_user_group; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_group_membership
    ADD CONSTRAINT constraint_user_group PRIMARY KEY (group_id, user_id);


--
-- Name: web_origins constraint_web_origins; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.web_origins
    ADD CONSTRAINT constraint_web_origins PRIMARY KEY (client_id, value);


--
-- Name: databasechangeloglock databasechangeloglock_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.databasechangeloglock
    ADD CONSTRAINT databasechangeloglock_pkey PRIMARY KEY (id);


--
-- Name: client_scope_attributes pk_cl_tmpl_attr; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.client_scope_attributes
    ADD CONSTRAINT pk_cl_tmpl_attr PRIMARY KEY (scope_id, name);


--
-- Name: client_scope pk_cli_template; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.client_scope
    ADD CONSTRAINT pk_cli_template PRIMARY KEY (id);


--
-- Name: resource_server pk_resource_server; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.resource_server
    ADD CONSTRAINT pk_resource_server PRIMARY KEY (id);


--
-- Name: client_scope_role_mapping pk_template_scope; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.client_scope_role_mapping
    ADD CONSTRAINT pk_template_scope PRIMARY KEY (scope_id, role_id);


--
-- Name: default_client_scope r_def_cli_scope_bind; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.default_client_scope
    ADD CONSTRAINT r_def_cli_scope_bind PRIMARY KEY (realm_id, scope_id);


--
-- Name: realm_localizations realm_localizations_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.realm_localizations
    ADD CONSTRAINT realm_localizations_pkey PRIMARY KEY (realm_id, locale);


--
-- Name: resource_attribute res_attr_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.resource_attribute
    ADD CONSTRAINT res_attr_pk PRIMARY KEY (id);


--
-- Name: keycloak_group sibling_names; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.keycloak_group
    ADD CONSTRAINT sibling_names UNIQUE (realm_id, parent_group, name);


--
-- Name: identity_provider uk_2daelwnibji49avxsrtuf6xj33; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.identity_provider
    ADD CONSTRAINT uk_2daelwnibji49avxsrtuf6xj33 UNIQUE (provider_alias, realm_id);


--
-- Name: client uk_b71cjlbenv945rb6gcon438at; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.client
    ADD CONSTRAINT uk_b71cjlbenv945rb6gcon438at UNIQUE (realm_id, client_id);


--
-- Name: client_scope uk_cli_scope; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.client_scope
    ADD CONSTRAINT uk_cli_scope UNIQUE (realm_id, name);


--
-- Name: user_entity uk_dykn684sl8up1crfei6eckhd7; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_entity
    ADD CONSTRAINT uk_dykn684sl8up1crfei6eckhd7 UNIQUE (realm_id, email_constraint);


--
-- Name: user_consent uk_external_consent; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_consent
    ADD CONSTRAINT uk_external_consent UNIQUE (client_storage_provider, external_client_id, user_id);


--
-- Name: resource_server_resource uk_frsr6t700s9v50bu18ws5ha6; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.resource_server_resource
    ADD CONSTRAINT uk_frsr6t700s9v50bu18ws5ha6 UNIQUE (name, owner, resource_server_id);


--
-- Name: resource_server_perm_ticket uk_frsr6t700s9v50bu18ws5pmt; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.resource_server_perm_ticket
    ADD CONSTRAINT uk_frsr6t700s9v50bu18ws5pmt UNIQUE (owner, requester, resource_server_id, resource_id, scope_id);


--
-- Name: resource_server_policy uk_frsrpt700s9v50bu18ws5ha6; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.resource_server_policy
    ADD CONSTRAINT uk_frsrpt700s9v50bu18ws5ha6 UNIQUE (name, resource_server_id);


--
-- Name: resource_server_scope uk_frsrst700s9v50bu18ws5ha6; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.resource_server_scope
    ADD CONSTRAINT uk_frsrst700s9v50bu18ws5ha6 UNIQUE (name, resource_server_id);


--
-- Name: user_consent uk_local_consent; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_consent
    ADD CONSTRAINT uk_local_consent UNIQUE (client_id, user_id);


--
-- Name: org uk_org_alias; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.org
    ADD CONSTRAINT uk_org_alias UNIQUE (realm_id, alias);


--
-- Name: org uk_org_group; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.org
    ADD CONSTRAINT uk_org_group UNIQUE (group_id);


--
-- Name: org uk_org_name; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.org
    ADD CONSTRAINT uk_org_name UNIQUE (realm_id, name);


--
-- Name: realm uk_orvsdmla56612eaefiq6wl5oi; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.realm
    ADD CONSTRAINT uk_orvsdmla56612eaefiq6wl5oi UNIQUE (name);


--
-- Name: user_entity uk_ru8tt6t700s9v50bu18ws5ha6; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_entity
    ADD CONSTRAINT uk_ru8tt6t700s9v50bu18ws5ha6 UNIQUE (realm_id, username);


--
-- Name: fed_user_attr_long_values; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX fed_user_attr_long_values ON public.fed_user_attribute USING btree (long_value_hash, name);


--
-- Name: fed_user_attr_long_values_lower_case; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX fed_user_attr_long_values_lower_case ON public.fed_user_attribute USING btree (long_value_hash_lower_case, name);


--
-- Name: idx_admin_event_time; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_admin_event_time ON public.admin_event_entity USING btree (realm_id, admin_event_time);


--
-- Name: idx_assoc_pol_assoc_pol_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_assoc_pol_assoc_pol_id ON public.associated_policy USING btree (associated_policy_id);


--
-- Name: idx_auth_config_realm; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_auth_config_realm ON public.authenticator_config USING btree (realm_id);


--
-- Name: idx_auth_exec_flow; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_auth_exec_flow ON public.authentication_execution USING btree (flow_id);


--
-- Name: idx_auth_exec_realm_flow; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_auth_exec_realm_flow ON public.authentication_execution USING btree (realm_id, flow_id);


--
-- Name: idx_auth_flow_realm; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_auth_flow_realm ON public.authentication_flow USING btree (realm_id);


--
-- Name: idx_cl_clscope; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_cl_clscope ON public.client_scope_client USING btree (scope_id);


--
-- Name: idx_client_att_by_name_value; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_client_att_by_name_value ON public.client_attributes USING btree (name, substr(value, 1, 255));


--
-- Name: idx_client_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_client_id ON public.client USING btree (client_id);


--
-- Name: idx_client_init_acc_realm; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_client_init_acc_realm ON public.client_initial_access USING btree (realm_id);


--
-- Name: idx_clscope_attrs; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_clscope_attrs ON public.client_scope_attributes USING btree (scope_id);


--
-- Name: idx_clscope_cl; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_clscope_cl ON public.client_scope_client USING btree (client_id);


--
-- Name: idx_clscope_protmap; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_clscope_protmap ON public.protocol_mapper USING btree (client_scope_id);


--
-- Name: idx_clscope_role; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_clscope_role ON public.client_scope_role_mapping USING btree (scope_id);


--
-- Name: idx_compo_config_compo; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_compo_config_compo ON public.component_config USING btree (component_id);


--
-- Name: idx_component_provider_type; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_component_provider_type ON public.component USING btree (provider_type);


--
-- Name: idx_component_realm; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_component_realm ON public.component USING btree (realm_id);


--
-- Name: idx_composite; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_composite ON public.composite_role USING btree (composite);


--
-- Name: idx_composite_child; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_composite_child ON public.composite_role USING btree (child_role);


--
-- Name: idx_defcls_realm; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_defcls_realm ON public.default_client_scope USING btree (realm_id);


--
-- Name: idx_defcls_scope; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_defcls_scope ON public.default_client_scope USING btree (scope_id);


--
-- Name: idx_event_time; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_event_time ON public.event_entity USING btree (realm_id, event_time);


--
-- Name: idx_fedidentity_feduser; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_fedidentity_feduser ON public.federated_identity USING btree (federated_user_id);


--
-- Name: idx_fedidentity_user; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_fedidentity_user ON public.federated_identity USING btree (user_id);


--
-- Name: idx_fu_attribute; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_fu_attribute ON public.fed_user_attribute USING btree (user_id, realm_id, name);


--
-- Name: idx_fu_cnsnt_ext; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_fu_cnsnt_ext ON public.fed_user_consent USING btree (user_id, client_storage_provider, external_client_id);


--
-- Name: idx_fu_consent; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_fu_consent ON public.fed_user_consent USING btree (user_id, client_id);


--
-- Name: idx_fu_consent_ru; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_fu_consent_ru ON public.fed_user_consent USING btree (realm_id, user_id);


--
-- Name: idx_fu_credential; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_fu_credential ON public.fed_user_credential USING btree (user_id, type);


--
-- Name: idx_fu_credential_ru; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_fu_credential_ru ON public.fed_user_credential USING btree (realm_id, user_id);


--
-- Name: idx_fu_group_membership; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_fu_group_membership ON public.fed_user_group_membership USING btree (user_id, group_id);


--
-- Name: idx_fu_group_membership_ru; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_fu_group_membership_ru ON public.fed_user_group_membership USING btree (realm_id, user_id);


--
-- Name: idx_fu_required_action; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_fu_required_action ON public.fed_user_required_action USING btree (user_id, required_action);


--
-- Name: idx_fu_required_action_ru; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_fu_required_action_ru ON public.fed_user_required_action USING btree (realm_id, user_id);


--
-- Name: idx_fu_role_mapping; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_fu_role_mapping ON public.fed_user_role_mapping USING btree (user_id, role_id);


--
-- Name: idx_fu_role_mapping_ru; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_fu_role_mapping_ru ON public.fed_user_role_mapping USING btree (realm_id, user_id);


--
-- Name: idx_group_att_by_name_value; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_group_att_by_name_value ON public.group_attribute USING btree (name, ((value)::character varying(250)));


--
-- Name: idx_group_attr_group; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_group_attr_group ON public.group_attribute USING btree (group_id);


--
-- Name: idx_group_role_mapp_group; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_group_role_mapp_group ON public.group_role_mapping USING btree (group_id);


--
-- Name: idx_id_prov_mapp_realm; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_id_prov_mapp_realm ON public.identity_provider_mapper USING btree (realm_id);


--
-- Name: idx_ident_prov_realm; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_ident_prov_realm ON public.identity_provider USING btree (realm_id);


--
-- Name: idx_idp_for_login; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_idp_for_login ON public.identity_provider USING btree (realm_id, enabled, link_only, hide_on_login, organization_id);


--
-- Name: idx_idp_realm_org; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_idp_realm_org ON public.identity_provider USING btree (realm_id, organization_id);


--
-- Name: idx_keycloak_role_client; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_keycloak_role_client ON public.keycloak_role USING btree (client);


--
-- Name: idx_keycloak_role_realm; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_keycloak_role_realm ON public.keycloak_role USING btree (realm);


--
-- Name: idx_offline_uss_by_broker_session_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_offline_uss_by_broker_session_id ON public.offline_user_session USING btree (broker_session_id, realm_id);


--
-- Name: idx_offline_uss_by_last_session_refresh; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_offline_uss_by_last_session_refresh ON public.offline_user_session USING btree (realm_id, offline_flag, last_session_refresh);


--
-- Name: idx_offline_uss_by_user; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_offline_uss_by_user ON public.offline_user_session USING btree (user_id, realm_id, offline_flag);


--
-- Name: idx_org_domain_org_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_org_domain_org_id ON public.org_domain USING btree (org_id);


--
-- Name: idx_perm_ticket_owner; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_perm_ticket_owner ON public.resource_server_perm_ticket USING btree (owner);


--
-- Name: idx_perm_ticket_requester; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_perm_ticket_requester ON public.resource_server_perm_ticket USING btree (requester);


--
-- Name: idx_protocol_mapper_client; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_protocol_mapper_client ON public.protocol_mapper USING btree (client_id);


--
-- Name: idx_realm_attr_realm; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_realm_attr_realm ON public.realm_attribute USING btree (realm_id);


--
-- Name: idx_realm_clscope; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_realm_clscope ON public.client_scope USING btree (realm_id);


--
-- Name: idx_realm_def_grp_realm; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_realm_def_grp_realm ON public.realm_default_groups USING btree (realm_id);


--
-- Name: idx_realm_evt_list_realm; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_realm_evt_list_realm ON public.realm_events_listeners USING btree (realm_id);


--
-- Name: idx_realm_evt_types_realm; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_realm_evt_types_realm ON public.realm_enabled_event_types USING btree (realm_id);


--
-- Name: idx_realm_master_adm_cli; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_realm_master_adm_cli ON public.realm USING btree (master_admin_client);


--
-- Name: idx_realm_supp_local_realm; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_realm_supp_local_realm ON public.realm_supported_locales USING btree (realm_id);


--
-- Name: idx_redir_uri_client; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_redir_uri_client ON public.redirect_uris USING btree (client_id);


--
-- Name: idx_req_act_prov_realm; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_req_act_prov_realm ON public.required_action_provider USING btree (realm_id);


--
-- Name: idx_res_policy_policy; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_res_policy_policy ON public.resource_policy USING btree (policy_id);


--
-- Name: idx_res_scope_scope; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_res_scope_scope ON public.resource_scope USING btree (scope_id);


--
-- Name: idx_res_serv_pol_res_serv; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_res_serv_pol_res_serv ON public.resource_server_policy USING btree (resource_server_id);


--
-- Name: idx_res_srv_res_res_srv; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_res_srv_res_res_srv ON public.resource_server_resource USING btree (resource_server_id);


--
-- Name: idx_res_srv_scope_res_srv; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_res_srv_scope_res_srv ON public.resource_server_scope USING btree (resource_server_id);


--
-- Name: idx_rev_token_on_expire; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_rev_token_on_expire ON public.revoked_token USING btree (expire);


--
-- Name: idx_role_attribute; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_role_attribute ON public.role_attribute USING btree (role_id);


--
-- Name: idx_role_clscope; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_role_clscope ON public.client_scope_role_mapping USING btree (role_id);


--
-- Name: idx_scope_mapping_role; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_scope_mapping_role ON public.scope_mapping USING btree (role_id);


--
-- Name: idx_scope_policy_policy; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_scope_policy_policy ON public.scope_policy USING btree (policy_id);


--
-- Name: idx_update_time; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_update_time ON public.migration_model USING btree (update_time);


--
-- Name: idx_usconsent_clscope; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_usconsent_clscope ON public.user_consent_client_scope USING btree (user_consent_id);


--
-- Name: idx_usconsent_scope_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_usconsent_scope_id ON public.user_consent_client_scope USING btree (scope_id);


--
-- Name: idx_user_attribute; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_user_attribute ON public.user_attribute USING btree (user_id);


--
-- Name: idx_user_attribute_name; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_user_attribute_name ON public.user_attribute USING btree (name, value);


--
-- Name: idx_user_consent; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_user_consent ON public.user_consent USING btree (user_id);


--
-- Name: idx_user_credential; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_user_credential ON public.credential USING btree (user_id);


--
-- Name: idx_user_email; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_user_email ON public.user_entity USING btree (email);


--
-- Name: idx_user_group_mapping; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_user_group_mapping ON public.user_group_membership USING btree (user_id);


--
-- Name: idx_user_reqactions; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_user_reqactions ON public.user_required_action USING btree (user_id);


--
-- Name: idx_user_role_mapping; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_user_role_mapping ON public.user_role_mapping USING btree (user_id);


--
-- Name: idx_user_service_account; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_user_service_account ON public.user_entity USING btree (realm_id, service_account_client_link);


--
-- Name: idx_usr_fed_map_fed_prv; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_usr_fed_map_fed_prv ON public.user_federation_mapper USING btree (federation_provider_id);


--
-- Name: idx_usr_fed_map_realm; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_usr_fed_map_realm ON public.user_federation_mapper USING btree (realm_id);


--
-- Name: idx_usr_fed_prv_realm; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_usr_fed_prv_realm ON public.user_federation_provider USING btree (realm_id);


--
-- Name: idx_web_orig_client; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_web_orig_client ON public.web_origins USING btree (client_id);


--
-- Name: user_attr_long_values; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX user_attr_long_values ON public.user_attribute USING btree (long_value_hash, name);


--
-- Name: user_attr_long_values_lower_case; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX user_attr_long_values_lower_case ON public.user_attribute USING btree (long_value_hash_lower_case, name);


--
-- Name: identity_provider fk2b4ebc52ae5c3b34; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.identity_provider
    ADD CONSTRAINT fk2b4ebc52ae5c3b34 FOREIGN KEY (realm_id) REFERENCES public.realm(id);


--
-- Name: client_attributes fk3c47c64beacca966; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.client_attributes
    ADD CONSTRAINT fk3c47c64beacca966 FOREIGN KEY (client_id) REFERENCES public.client(id);


--
-- Name: federated_identity fk404288b92ef007a6; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.federated_identity
    ADD CONSTRAINT fk404288b92ef007a6 FOREIGN KEY (user_id) REFERENCES public.user_entity(id);


--
-- Name: client_node_registrations fk4129723ba992f594; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.client_node_registrations
    ADD CONSTRAINT fk4129723ba992f594 FOREIGN KEY (client_id) REFERENCES public.client(id);


--
-- Name: redirect_uris fk_1burs8pb4ouj97h5wuppahv9f; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.redirect_uris
    ADD CONSTRAINT fk_1burs8pb4ouj97h5wuppahv9f FOREIGN KEY (client_id) REFERENCES public.client(id);


--
-- Name: user_federation_provider fk_1fj32f6ptolw2qy60cd8n01e8; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_federation_provider
    ADD CONSTRAINT fk_1fj32f6ptolw2qy60cd8n01e8 FOREIGN KEY (realm_id) REFERENCES public.realm(id);


--
-- Name: realm_required_credential fk_5hg65lybevavkqfki3kponh9v; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.realm_required_credential
    ADD CONSTRAINT fk_5hg65lybevavkqfki3kponh9v FOREIGN KEY (realm_id) REFERENCES public.realm(id);


--
-- Name: resource_attribute fk_5hrm2vlf9ql5fu022kqepovbr; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.resource_attribute
    ADD CONSTRAINT fk_5hrm2vlf9ql5fu022kqepovbr FOREIGN KEY (resource_id) REFERENCES public.resource_server_resource(id);


--
-- Name: user_attribute fk_5hrm2vlf9ql5fu043kqepovbr; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_attribute
    ADD CONSTRAINT fk_5hrm2vlf9ql5fu043kqepovbr FOREIGN KEY (user_id) REFERENCES public.user_entity(id);


--
-- Name: user_required_action fk_6qj3w1jw9cvafhe19bwsiuvmd; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_required_action
    ADD CONSTRAINT fk_6qj3w1jw9cvafhe19bwsiuvmd FOREIGN KEY (user_id) REFERENCES public.user_entity(id);


--
-- Name: keycloak_role fk_6vyqfe4cn4wlq8r6kt5vdsj5c; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.keycloak_role
    ADD CONSTRAINT fk_6vyqfe4cn4wlq8r6kt5vdsj5c FOREIGN KEY (realm) REFERENCES public.realm(id);


--
-- Name: realm_smtp_config fk_70ej8xdxgxd0b9hh6180irr0o; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.realm_smtp_config
    ADD CONSTRAINT fk_70ej8xdxgxd0b9hh6180irr0o FOREIGN KEY (realm_id) REFERENCES public.realm(id);


--
-- Name: realm_attribute fk_8shxd6l3e9atqukacxgpffptw; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.realm_attribute
    ADD CONSTRAINT fk_8shxd6l3e9atqukacxgpffptw FOREIGN KEY (realm_id) REFERENCES public.realm(id);


--
-- Name: composite_role fk_a63wvekftu8jo1pnj81e7mce2; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.composite_role
    ADD CONSTRAINT fk_a63wvekftu8jo1pnj81e7mce2 FOREIGN KEY (composite) REFERENCES public.keycloak_role(id);


--
-- Name: authentication_execution fk_auth_exec_flow; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.authentication_execution
    ADD CONSTRAINT fk_auth_exec_flow FOREIGN KEY (flow_id) REFERENCES public.authentication_flow(id);


--
-- Name: authentication_execution fk_auth_exec_realm; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.authentication_execution
    ADD CONSTRAINT fk_auth_exec_realm FOREIGN KEY (realm_id) REFERENCES public.realm(id);


--
-- Name: authentication_flow fk_auth_flow_realm; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.authentication_flow
    ADD CONSTRAINT fk_auth_flow_realm FOREIGN KEY (realm_id) REFERENCES public.realm(id);


--
-- Name: authenticator_config fk_auth_realm; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.authenticator_config
    ADD CONSTRAINT fk_auth_realm FOREIGN KEY (realm_id) REFERENCES public.realm(id);


--
-- Name: user_role_mapping fk_c4fqv34p1mbylloxang7b1q3l; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_role_mapping
    ADD CONSTRAINT fk_c4fqv34p1mbylloxang7b1q3l FOREIGN KEY (user_id) REFERENCES public.user_entity(id);


--
-- Name: client_scope_attributes fk_cl_scope_attr_scope; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.client_scope_attributes
    ADD CONSTRAINT fk_cl_scope_attr_scope FOREIGN KEY (scope_id) REFERENCES public.client_scope(id);


--
-- Name: client_scope_role_mapping fk_cl_scope_rm_scope; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.client_scope_role_mapping
    ADD CONSTRAINT fk_cl_scope_rm_scope FOREIGN KEY (scope_id) REFERENCES public.client_scope(id);


--
-- Name: protocol_mapper fk_cli_scope_mapper; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.protocol_mapper
    ADD CONSTRAINT fk_cli_scope_mapper FOREIGN KEY (client_scope_id) REFERENCES public.client_scope(id);


--
-- Name: client_initial_access fk_client_init_acc_realm; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.client_initial_access
    ADD CONSTRAINT fk_client_init_acc_realm FOREIGN KEY (realm_id) REFERENCES public.realm(id);


--
-- Name: component_config fk_component_config; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.component_config
    ADD CONSTRAINT fk_component_config FOREIGN KEY (component_id) REFERENCES public.component(id);


--
-- Name: component fk_component_realm; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.component
    ADD CONSTRAINT fk_component_realm FOREIGN KEY (realm_id) REFERENCES public.realm(id);


--
-- Name: realm_default_groups fk_def_groups_realm; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.realm_default_groups
    ADD CONSTRAINT fk_def_groups_realm FOREIGN KEY (realm_id) REFERENCES public.realm(id);


--
-- Name: user_federation_mapper_config fk_fedmapper_cfg; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_federation_mapper_config
    ADD CONSTRAINT fk_fedmapper_cfg FOREIGN KEY (user_federation_mapper_id) REFERENCES public.user_federation_mapper(id);


--
-- Name: user_federation_mapper fk_fedmapperpm_fedprv; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_federation_mapper
    ADD CONSTRAINT fk_fedmapperpm_fedprv FOREIGN KEY (federation_provider_id) REFERENCES public.user_federation_provider(id);


--
-- Name: user_federation_mapper fk_fedmapperpm_realm; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_federation_mapper
    ADD CONSTRAINT fk_fedmapperpm_realm FOREIGN KEY (realm_id) REFERENCES public.realm(id);


--
-- Name: associated_policy fk_frsr5s213xcx4wnkog82ssrfy; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.associated_policy
    ADD CONSTRAINT fk_frsr5s213xcx4wnkog82ssrfy FOREIGN KEY (associated_policy_id) REFERENCES public.resource_server_policy(id);


--
-- Name: scope_policy fk_frsrasp13xcx4wnkog82ssrfy; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.scope_policy
    ADD CONSTRAINT fk_frsrasp13xcx4wnkog82ssrfy FOREIGN KEY (policy_id) REFERENCES public.resource_server_policy(id);


--
-- Name: resource_server_perm_ticket fk_frsrho213xcx4wnkog82sspmt; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.resource_server_perm_ticket
    ADD CONSTRAINT fk_frsrho213xcx4wnkog82sspmt FOREIGN KEY (resource_server_id) REFERENCES public.resource_server(id);


--
-- Name: resource_server_resource fk_frsrho213xcx4wnkog82ssrfy; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.resource_server_resource
    ADD CONSTRAINT fk_frsrho213xcx4wnkog82ssrfy FOREIGN KEY (resource_server_id) REFERENCES public.resource_server(id);


--
-- Name: resource_server_perm_ticket fk_frsrho213xcx4wnkog83sspmt; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.resource_server_perm_ticket
    ADD CONSTRAINT fk_frsrho213xcx4wnkog83sspmt FOREIGN KEY (resource_id) REFERENCES public.resource_server_resource(id);


--
-- Name: resource_server_perm_ticket fk_frsrho213xcx4wnkog84sspmt; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.resource_server_perm_ticket
    ADD CONSTRAINT fk_frsrho213xcx4wnkog84sspmt FOREIGN KEY (scope_id) REFERENCES public.resource_server_scope(id);


--
-- Name: associated_policy fk_frsrpas14xcx4wnkog82ssrfy; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.associated_policy
    ADD CONSTRAINT fk_frsrpas14xcx4wnkog82ssrfy FOREIGN KEY (policy_id) REFERENCES public.resource_server_policy(id);


--
-- Name: scope_policy fk_frsrpass3xcx4wnkog82ssrfy; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.scope_policy
    ADD CONSTRAINT fk_frsrpass3xcx4wnkog82ssrfy FOREIGN KEY (scope_id) REFERENCES public.resource_server_scope(id);


--
-- Name: resource_server_perm_ticket fk_frsrpo2128cx4wnkog82ssrfy; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.resource_server_perm_ticket
    ADD CONSTRAINT fk_frsrpo2128cx4wnkog82ssrfy FOREIGN KEY (policy_id) REFERENCES public.resource_server_policy(id);


--
-- Name: resource_server_policy fk_frsrpo213xcx4wnkog82ssrfy; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.resource_server_policy
    ADD CONSTRAINT fk_frsrpo213xcx4wnkog82ssrfy FOREIGN KEY (resource_server_id) REFERENCES public.resource_server(id);


--
-- Name: resource_scope fk_frsrpos13xcx4wnkog82ssrfy; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.resource_scope
    ADD CONSTRAINT fk_frsrpos13xcx4wnkog82ssrfy FOREIGN KEY (resource_id) REFERENCES public.resource_server_resource(id);


--
-- Name: resource_policy fk_frsrpos53xcx4wnkog82ssrfy; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.resource_policy
    ADD CONSTRAINT fk_frsrpos53xcx4wnkog82ssrfy FOREIGN KEY (resource_id) REFERENCES public.resource_server_resource(id);


--
-- Name: resource_policy fk_frsrpp213xcx4wnkog82ssrfy; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.resource_policy
    ADD CONSTRAINT fk_frsrpp213xcx4wnkog82ssrfy FOREIGN KEY (policy_id) REFERENCES public.resource_server_policy(id);


--
-- Name: resource_scope fk_frsrps213xcx4wnkog82ssrfy; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.resource_scope
    ADD CONSTRAINT fk_frsrps213xcx4wnkog82ssrfy FOREIGN KEY (scope_id) REFERENCES public.resource_server_scope(id);


--
-- Name: resource_server_scope fk_frsrso213xcx4wnkog82ssrfy; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.resource_server_scope
    ADD CONSTRAINT fk_frsrso213xcx4wnkog82ssrfy FOREIGN KEY (resource_server_id) REFERENCES public.resource_server(id);


--
-- Name: composite_role fk_gr7thllb9lu8q4vqa4524jjy8; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.composite_role
    ADD CONSTRAINT fk_gr7thllb9lu8q4vqa4524jjy8 FOREIGN KEY (child_role) REFERENCES public.keycloak_role(id);


--
-- Name: user_consent_client_scope fk_grntcsnt_clsc_usc; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_consent_client_scope
    ADD CONSTRAINT fk_grntcsnt_clsc_usc FOREIGN KEY (user_consent_id) REFERENCES public.user_consent(id);


--
-- Name: user_consent fk_grntcsnt_user; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_consent
    ADD CONSTRAINT fk_grntcsnt_user FOREIGN KEY (user_id) REFERENCES public.user_entity(id);


--
-- Name: group_attribute fk_group_attribute_group; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.group_attribute
    ADD CONSTRAINT fk_group_attribute_group FOREIGN KEY (group_id) REFERENCES public.keycloak_group(id);


--
-- Name: group_role_mapping fk_group_role_group; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.group_role_mapping
    ADD CONSTRAINT fk_group_role_group FOREIGN KEY (group_id) REFERENCES public.keycloak_group(id);


--
-- Name: realm_enabled_event_types fk_h846o4h0w8epx5nwedrf5y69j; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.realm_enabled_event_types
    ADD CONSTRAINT fk_h846o4h0w8epx5nwedrf5y69j FOREIGN KEY (realm_id) REFERENCES public.realm(id);


--
-- Name: realm_events_listeners fk_h846o4h0w8epx5nxev9f5y69j; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.realm_events_listeners
    ADD CONSTRAINT fk_h846o4h0w8epx5nxev9f5y69j FOREIGN KEY (realm_id) REFERENCES public.realm(id);


--
-- Name: identity_provider_mapper fk_idpm_realm; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.identity_provider_mapper
    ADD CONSTRAINT fk_idpm_realm FOREIGN KEY (realm_id) REFERENCES public.realm(id);


--
-- Name: idp_mapper_config fk_idpmconfig; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.idp_mapper_config
    ADD CONSTRAINT fk_idpmconfig FOREIGN KEY (idp_mapper_id) REFERENCES public.identity_provider_mapper(id);


--
-- Name: web_origins fk_lojpho213xcx4wnkog82ssrfy; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.web_origins
    ADD CONSTRAINT fk_lojpho213xcx4wnkog82ssrfy FOREIGN KEY (client_id) REFERENCES public.client(id);


--
-- Name: scope_mapping fk_ouse064plmlr732lxjcn1q5f1; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.scope_mapping
    ADD CONSTRAINT fk_ouse064plmlr732lxjcn1q5f1 FOREIGN KEY (client_id) REFERENCES public.client(id);


--
-- Name: protocol_mapper fk_pcm_realm; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.protocol_mapper
    ADD CONSTRAINT fk_pcm_realm FOREIGN KEY (client_id) REFERENCES public.client(id);


--
-- Name: credential fk_pfyr0glasqyl0dei3kl69r6v0; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.credential
    ADD CONSTRAINT fk_pfyr0glasqyl0dei3kl69r6v0 FOREIGN KEY (user_id) REFERENCES public.user_entity(id);


--
-- Name: protocol_mapper_config fk_pmconfig; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.protocol_mapper_config
    ADD CONSTRAINT fk_pmconfig FOREIGN KEY (protocol_mapper_id) REFERENCES public.protocol_mapper(id);


--
-- Name: default_client_scope fk_r_def_cli_scope_realm; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.default_client_scope
    ADD CONSTRAINT fk_r_def_cli_scope_realm FOREIGN KEY (realm_id) REFERENCES public.realm(id);


--
-- Name: required_action_provider fk_req_act_realm; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.required_action_provider
    ADD CONSTRAINT fk_req_act_realm FOREIGN KEY (realm_id) REFERENCES public.realm(id);


--
-- Name: resource_uris fk_resource_server_uris; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.resource_uris
    ADD CONSTRAINT fk_resource_server_uris FOREIGN KEY (resource_id) REFERENCES public.resource_server_resource(id);


--
-- Name: role_attribute fk_role_attribute_id; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.role_attribute
    ADD CONSTRAINT fk_role_attribute_id FOREIGN KEY (role_id) REFERENCES public.keycloak_role(id);


--
-- Name: realm_supported_locales fk_supported_locales_realm; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.realm_supported_locales
    ADD CONSTRAINT fk_supported_locales_realm FOREIGN KEY (realm_id) REFERENCES public.realm(id);


--
-- Name: user_federation_config fk_t13hpu1j94r2ebpekr39x5eu5; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_federation_config
    ADD CONSTRAINT fk_t13hpu1j94r2ebpekr39x5eu5 FOREIGN KEY (user_federation_provider_id) REFERENCES public.user_federation_provider(id);


--
-- Name: user_group_membership fk_user_group_user; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_group_membership
    ADD CONSTRAINT fk_user_group_user FOREIGN KEY (user_id) REFERENCES public.user_entity(id);


--
-- Name: policy_config fkdc34197cf864c4e43; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.policy_config
    ADD CONSTRAINT fkdc34197cf864c4e43 FOREIGN KEY (policy_id) REFERENCES public.resource_server_policy(id);


--
-- Name: identity_provider_config fkdc4897cf864c4e43; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.identity_provider_config
    ADD CONSTRAINT fkdc4897cf864c4e43 FOREIGN KEY (identity_provider_id) REFERENCES public.identity_provider(internal_id);


--
-- PostgreSQL database dump complete
--

