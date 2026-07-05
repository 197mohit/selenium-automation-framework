package com.paytm.utils.merchant.api.pgp.theia.validateAndFethMerchantInfo.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "requestId",
        "requestTimeStamp",
        "clientId",
        "version",
        "tokenType",
        "token"
})
public class Head {

    @JsonProperty("requestId")
    private String requestId = "WEB";
    @JsonProperty("requestTimeStamp")
    private String requestTimeStamp;
    @JsonProperty("clientId")
    private String clientId = "C11";
    @JsonProperty("version")
    private String version = "v1";
    @JsonProperty("tokenType")
    private String tokenType = "JWT";
    @JsonProperty("token")
    private String token;

    @JsonProperty("requestId")
    public String getRequestId() {
        return requestId;
    }

    @JsonProperty("requestId")
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    @JsonProperty("requestTimeStamp")
    public String getRequestTimeStamp() {
        return requestTimeStamp;
    }

    @JsonProperty("requestTimeStamp")
    public void setRequestTimeStamp(String requestTimeStamp) {
        this.requestTimeStamp = requestTimeStamp;
    }

    @JsonProperty("clientId")
    public String getClientId() {
        return clientId;
    }

    @JsonProperty("clientId")
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    @JsonProperty("version")
    public String getVersion() {
        return version;
    }

    @JsonProperty("version")
    public void setVersion(String version) {
        this.version = version;
    }

    @JsonProperty("tokenType")
    public String getTokenType() {
        return tokenType;
    }

    @JsonProperty("tokenType")
    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    @JsonProperty("token")
    public String getToken() {
        return token;
    }

    @JsonProperty("token")
    public void setToken(String token) {
        this.token = token;
    }

}
