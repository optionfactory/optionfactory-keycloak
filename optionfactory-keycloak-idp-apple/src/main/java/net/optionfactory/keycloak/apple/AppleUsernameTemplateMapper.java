package net.optionfactory.keycloak.apple;

import org.keycloak.broker.oidc.mappers.UsernameTemplateMapper;

public class AppleUsernameTemplateMapper extends UsernameTemplateMapper {

    @Override
    public String[] getCompatibleProviders() {
        return new String[]{
            "opfa-apple"
        };
    }

    @Override
    public String getId() {
        return "opfa-apple-username-template-mapper";
    }
}
