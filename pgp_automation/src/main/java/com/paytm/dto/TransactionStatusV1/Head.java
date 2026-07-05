package com.paytm.dto.TransactionStatusV1;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "version",
        "requestTimestamp",
        "channelId",
        "tokenType",
        "token"
})
public class Head {

    @JsonProperty("version")
    private String version;
    @JsonProperty("requestTimestamp")
    private String requestTimestamp;
    @JsonProperty("channelId")
    private String channelId;
    @JsonProperty("tokenType")
    private String tokenType;
    @JsonProperty("token")
    private String token;

    public Head(){
        this.version = "v1";
        this.channelId = "WEB";
        this.requestTimestamp = "Time";
    }

    @JsonProperty("version")
    public String getVersion() {
        return version;
    }

    @JsonProperty("version")
    public Head setVersion(String version) {
        this.version = version;
        return this;
    }

    @JsonProperty("requestTimestamp")
    public String getRequestTimestamp() {
        return requestTimestamp;
    }

    @JsonProperty("requestTimestamp")
    public Head setRequestTimestamp(String requestTimestamp) {
        this.requestTimestamp = requestTimestamp;
        return this;
    }

    @JsonProperty("channelId")
    public String getChannelId() {
        return channelId;
    }

    @JsonProperty("channelId")
    public Head setChannelId(String channelId) {
        this.channelId = channelId;
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

}