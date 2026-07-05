package com.paytm.dto.NativeDTO.UpdateTransaction;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "clientId",
        "version",
        "requestTimestamp",
        "channelId",
        "signature",
        "txnToken"
})
public class Head {

    @JsonProperty("clientId")
    private String clientId;
    @JsonProperty("version")
    private String version;
    @JsonProperty("requestTimestamp")
    private String requestTimestamp;
    @JsonProperty("channelId")
    private String channelId;
    @JsonProperty("signature")
    private String signature;
    @JsonProperty("txnToken")
    private String txnToken;

    public Head(String txnToken){
    this.txnToken = txnToken;
    this.clientId = "C11";
    this.version = "v1";
    this.requestTimestamp = "Time";
    this.channelId = "WEB";
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

    @JsonProperty("signature")
    public String getSignature() {
        return signature;
    }

    @JsonProperty("signature")
    public Head setSignature(String signature) {
        this.signature = signature;
        return this;
    }

    @JsonProperty("txnToken")
    public String getTxnToken() {
        return txnToken;
    }

    @JsonProperty("txnToken")
    public Head setTxnToken(String txnToken) {
        this.txnToken = txnToken;
        return this;
    }

}