package com.paytm.dto.NativeDTO.OfferApply;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Head {
    @JsonProperty("channelId")
    private String channelId;
    
    @JsonProperty("requestId")
    private String requestId;
    
    @JsonProperty("requestTimestamp")
    private String requestTimestamp;
    
    @JsonProperty("token")
    private String token;
    
    @JsonProperty("tokenType")
    private String tokenType;
    
    @JsonProperty("version")
    private String version;

    public Head() {}

    public Head(String channelId, String requestId, String requestTimestamp,
               String token, String tokenType, String version) {
        this.channelId = channelId;
        this.requestId = requestId;
        this.requestTimestamp = requestTimestamp;
        this.token = token;
        this.tokenType = tokenType;
        this.version = version;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getRequestTimestamp() {
        return requestTimestamp;
    }

    public void setRequestTimestamp(String requestTimestamp) {
        this.requestTimestamp = requestTimestamp;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}