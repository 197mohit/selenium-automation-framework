package com.paytm.dto.OrderStatusV2;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "requestTimestamp",
        "tokenType",
        "token",
        "clientId"
})
public class Head {

    @JsonProperty("requestTimestamp")
    private Integer requestTimestamp;
    @JsonProperty("tokenType")
    private String tokenType;
    @JsonProperty("token")
    private String token;
    @JsonProperty("clientId")
    private String clientId;

    @JsonProperty("requestTimestamp")
    public Integer getRequestTimestamp() {
        return requestTimestamp;
    }

    @JsonProperty("requestTimestamp")
    public Head setRequestTimestamp(Integer requestTimestamp) {
        this.requestTimestamp = requestTimestamp;
        return this;
    }

    @JsonProperty("tokenType")
    public String getTokenType() {
        return tokenType;
    }

    @JsonProperty("tokenType")
    public Head setTokenType(String tokenType) {
        this.tokenType = tokenType;
        return this;
    }

    @JsonProperty("token")
    public String getToken() {
        return token;
    }

    @JsonProperty("token")
    public Head setToken(String token) {
        this.token = token;
        return this;
    }

    @JsonProperty("clientId")
    public String getClientId() {
        return clientId;
    }

    @JsonProperty("clientId")
    public Head setClientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

}