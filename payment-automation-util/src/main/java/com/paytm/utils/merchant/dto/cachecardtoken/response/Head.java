package com.paytm.utils.merchant.dto.cachecardtoken.response;

public class Head {

    private String responseTimestamp;
    private String clientId;
    private String version;
    private String signature;

    public String getClientId(){ return clientId; }

    public Head setClientId(String clientId) {
        this.clientId = clientId;
        return  this;
    }

    public String getResponseTimestamp(){ return responseTimestamp; }

    public Head setResponseTimestamp(String responseTimestamp) {
        this.responseTimestamp = responseTimestamp;
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
