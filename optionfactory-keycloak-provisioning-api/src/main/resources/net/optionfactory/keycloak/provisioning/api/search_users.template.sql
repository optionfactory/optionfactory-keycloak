select 
    id, username, email, first_name, last_name,
    enabled, email_verified, created_timestamp, 
    cast(groups as text), cast(attributes as text), total 
from(
    select 
        id, username, email, first_name, last_name, 
        enabled, email_verified, created_timestamp, 
        groups, attributes, count(*) over() as total
    from user_entity u 
    left join lateral (
        select jsonb_agg(g.name) as groups from user_group_membership ug 
        inner join keycloak_group g on ug.group_id = g.id
        where ug.user_id = u.id
    ) gs on true
    left join lateral (
        select jsonb_object_agg(ua.name, ua.value) as attributes from user_attribute ua
        where ua.user_id = u.id
    ) at on true
    where 
        service_account_client_link is null
        and %s
) rs

