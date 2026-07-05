package com.paytm.dto.NativeDTO.checkEMIEligibility;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.assertj.core.internal.bytebuddy.implementation.bind.annotation.Default;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "version",
        "requestTimestamp",
        "channelId",
        "clientId",
        "requestId",
        "tokenType",
        "token"
})
public class Head {

    @JsonProperty(value = "version",defaultValue = "v1")
    private String version;
    @JsonProperty(value = "requestTimestamp",defaultValue = "Time")
    private String requestTimestamp;
    @JsonProperty(value = "requestId",defaultValue = "")
    private String requestId;
    @JsonProperty(value="tokenType",defaultValue = "SSO")
    private String tokenType;
    @JsonProperty("token")
    private String token;
    @JsonProperty(value = "channelId",defaultValue = "")
    private String channelId;
    @JsonProperty(value = "clientId",defaultValue = "")
    private String clientId;
    public Head(){
        this.version = "v1";
        this.requestTimestamp = "Time";
        this.requestId = "";
        this.tokenType="SSO";
        this.channelId = "";
        this.clientId = "";
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



    @JsonProperty("requestId")
    public String getRequestId() {
        return requestId;
    }

    @JsonProperty("requestId")
    public Head setRequestId(String requestId) {
        this.requestId = requestId;
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
    @JsonProperty("channelId")
    public String getChannelId() {
        return channelId;
    }

    @JsonProperty("ChannelId")
    public Head setChannelId(String channelId) {
        this.channelId = channelId;
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