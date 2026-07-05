package com.paytm.dto.NativeDTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Created by anjukumari on 22/10/18
 */



@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "version",
        "requestTimestamp",
        "channelId",
        "tokenType",
        "txnToken",
        "token"
})
public class Head {

    @JsonProperty("version")
    private String version;
    @JsonProperty("requestTimestamp")
    private String requestTimestamp;
    @JsonProperty("channelId")
    private String channelId;
    @JsonProperty("txnToken")
    private String txnToken;
    @JsonProperty("tokenType")
    private String tokenType;
    @JsonProperty("token")
    private String token;

    @JsonProperty("version")
    public String getVersion() {
        return version;
    }

    @JsonProperty("version")
    public void setVersion(String version) {
        this.version = version;
    }

    @JsonProperty("requestTimestamp")
    public String getRequestTimestamp() {
        return requestTimestamp;
    }

    @JsonProperty("requestTimestamp")
    public void setRequestTimestamp(String requestTimestamp) {
        this.requestTimestamp = requestTimestamp;
    }

    @JsonProperty("channelId")
    public String getChannelId() {
        return channelId;
    }

    @JsonProperty("channelId")
    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }


    @JsonProperty("tokenType")
    public String getTokenType() {
        return tokenType;
    }

    @JsonProperty("tokenType")
    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    @JsonProperty("txnToken")
    public String getTxnToken() {
        return txnToken;
    }

    @JsonProperty("txnToken")
    public void setTxnToken(String txnToken) {
        this.txnToken = txnToken;
    }

    @JsonProperty("token")
    public String getToken() {
        return token;
    }

    @JsonProperty("token")
    public void setToken(String token) {
        this.token = token;
    }

    public Head(String txnToken) {
        this.version = "v1";
        this.requestTimestamp = "Time";
        this.channelId = "WEB";
        this.txnToken = txnToken;
    }

    public Head(String tokenType,String ssoToken) {
        this.version = "v1";
        this.requestTimestamp = "Time";
        this.channelId = "WEB";
        this.tokenType = tokenType;
        this.txnToken = "";
        this.token =ssoToken;
    }

}
