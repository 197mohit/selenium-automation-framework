package com.paytm.dto.processTransactionV1;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "version",
        "requestTimestamp",
        "channelId",
        "tokenType",
        "token",
        "txnToken"

})
public class Head {

    @JsonProperty("version")
    private String version = "v1";
    @JsonProperty("requestTimestamp")
    private Long requestTimestamp = 1551766556536l;
    @JsonProperty("channelId")
    private String channelId = "WEB";
    @JsonProperty("tokenType")
    private String tokenType;
    @JsonProperty("token")
    private String token;
    @JsonProperty("txnToken")
    private String txnToken;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("txnToken")
    public String getTxnToken() {
        return txnToken;
    }

    @JsonProperty("txnToken")
    public Head setTxnToken(String txnToken) {
        this.txnToken = txnToken;
        return this;
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
    public Long getRequestTimestamp() {
        return requestTimestamp;
    }

    @JsonProperty("requestTimestamp")
    public Head setRequestTimestamp(Long requestTimestamp) {
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

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public Head setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

}