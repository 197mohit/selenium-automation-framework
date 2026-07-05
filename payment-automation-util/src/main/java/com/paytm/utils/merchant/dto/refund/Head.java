package com.paytm.utils.merchant.dto.refund;

/**
 * Created by ankuragarwal on 13/12/18
 */
public class Head {

    private String clientId;
    private String version;
    private String requestTimestamp;
    private String signature;
    private String tokenType;
    private String token;

    public String getClientId() {
        return clientId;
    }

    public Head setClientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    public String getVersion() {
        return version;
    }

    public Head setVersion(String version) {
        this.version = version;
        return this;
    }

    public String getRequestTimestamp() {
        return requestTimestamp;
    }

    public Head setRequestTimestamp(String requestTimestamp) {
        this.requestTimestamp = requestTimestamp;
        return this;
    }

    public String getSignature() {
        return signature;
    }

    public Head setSignature(String signature) {
        this.signature = signature;
        return this;
    }

    public String getTokenType() {
        return tokenType;
    }

    public Head setTokenType(String tokenType) {
        this.tokenType = tokenType;
        return this;
    }

    public String getToken() {
        return token;
    }

    public Head setToken(String token) {
        this.token = token;
        return this;
    }
}
