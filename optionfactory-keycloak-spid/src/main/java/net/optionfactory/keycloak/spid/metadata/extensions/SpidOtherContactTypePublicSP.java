package net.optionfactory.keycloak.spid.metadata.extensions;

import net.optionfactory.keycloak.spid.SpidIdentityProviderConfig;
import org.keycloak.saml.common.exceptions.ConfigurationException;

class SpidOtherContactTypePublicSP extends SpidOtherContactType {

    public SpidOtherContactTypePublicSP(final SpidIdentityProviderConfig config) throws ConfigurationException {
        super(config);

        // IPA Code
        addExtensionElement("spid:IPACode", config.getIpaCode());

        addQualifier("spid:Public");
    }
}
