package com.paytm.utils.merchant.dto.cachecardtoken.request;

public class Head {

    private String requestTimestamp;
    private String clientId;
    private String channelId;
    private String version;
    private String signature;

    public String getClientId(){ return clientId; }

    public Head setClientId(String clientId) {
        this.clientId = clientId;
        return  this;
    }

    public String getChannelId(){ return channelId; }

    public Head setChannelId(String channelId) {
        this.channelId = channelId;
        return  this;
    }

    public String getRequestTimestamp(){ return requestTimestamp; }

    public Head setRequestTimestamp(String requestTimestamp) {
        this.requestTimestamp = requestTimestamp;
        return this;
    }

    public String getVersion(){ return clientId; }

    public Head setVersion(String version) {
        this.version = version;
        return this;
    }

    public String getSignature(){ return signature; }

    public Head setSignature(String signature) {
        this.signature = signature;
        return  this;
    }


}
