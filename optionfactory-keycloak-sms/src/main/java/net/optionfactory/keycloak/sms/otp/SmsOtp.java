package net.optionfactory.keycloak.sms.otp;

import java.io.Serializable;

public class SmsOtp implements Serializable {

    public String id;
    public String value;
    public long durationMs;
    public int maxTentatives;
    public long creationTimestamp;
    public long usedTentatives;

}
