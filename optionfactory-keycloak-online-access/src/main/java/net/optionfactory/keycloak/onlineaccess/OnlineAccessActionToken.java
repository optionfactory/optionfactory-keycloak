package net.optionfactory.keycloak.onlineaccess;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.keycloak.authentication.actiontoken.DefaultActionToken;

public class OnlineAccessActionToken extends DefaultActionToken {

    public static final String TOKEN_TYPE = "online-access-action-token";

    @JsonProperty("rdu")
    private String redirectUri;

    public OnlineAccessActionToken(String userId, int absoluteExpirationInSecs, String clientId, String redirectUri) {
        super(userId, TOKEN_TYPE, absoluteExpirationInSecs, null);
        this.redirectUri = redirectUri;
        this.issuedFor = clientId;
    }

    private OnlineAccessActionToken() {
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

}
