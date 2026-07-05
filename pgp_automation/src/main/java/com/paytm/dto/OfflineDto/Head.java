package com.paytm.dto.OfflineDto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by anjukumari on 04/12/18
 */
public class Head {

    @JsonProperty("mid")
    private String mid;
    @JsonProperty("version")
    private String version;
    @JsonProperty("requestId")
    private String requestId;
    @JsonProperty("clientId")
    private String clientId;
    @JsonProperty("token")
    private String token;
    @JsonProperty("tokenType")
    private String tokenType;
    @JsonProperty("mid")
    public String getMid() {
        return mid;
    }

    @JsonProperty("mid")
    public void setMid(String mid) {
        this.mid = mid;
    }

    @JsonProperty("version")
    public String getVersion() {
        return version;
    }

    @JsonProperty("version")
    public void setVersion(String version) {
        this.version = version;
    }

    @JsonProperty("requestId")
    public String getRequestId() {
        return requestId;
    }

    @JsonProperty("requestId")
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    @JsonProperty("clientId")
    public String getClientId() {
        return clientId;
    }

    @JsonProperty("clientId")
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    @JsonProperty("token")
    public String getToken() {
        return token;
    }

    @JsonProperty("token")
    public void setToken(String token) {
        this.token = token;
    }

    @JsonProperty("tokenType")
    public String getTokenType() {
        return tokenType;
    }

    @JsonProperty("tokenType")
    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public Head() {
        this.mid = "";
        this.version = "v2";
        this.requestId = "sdas";
        this.clientId = "paytm-pg-client-staging";
        this.token = "";
        this.tokenType = "SSO";
    }
}
