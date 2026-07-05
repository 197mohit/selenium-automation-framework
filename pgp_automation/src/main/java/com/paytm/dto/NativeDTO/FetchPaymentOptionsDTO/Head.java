package com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Head {
    private String requestTimestamp;
    private String txnToken;
    private String version;
    private String channelId;
    private String tokenType;
    private String token;
    private String workFlow;
    private String requestId;

    public String getRequestId() {
        return requestId;
    }

    public Head setRequestId(String requestId) {
        this.requestId = requestId;
        return this;
    }



    public Head(String channelId) {
        this.channelId = channelId;
        this.requestTimestamp = "Time";
        this.version = "v1";

    }

    public Head(String txnToken, String channelId) {
        this.requestTimestamp = "Time";
        this.txnToken = txnToken;
        this.version = "v1";
        this.channelId =channelId;
        this.tokenType = "";
    }

    public String getToken() {
        return token;
    }

    public Head setToken(String token) {
        this.token = token;
        return this;
    }

    public String getTokenType() {
        return tokenType;
    }

    public Head setTokenType(String tokenType) {
        this.tokenType = tokenType;
        return this;
    }

    public String getRequestTimestamp() {
        return requestTimestamp;
    }

    public Head setRequestTimestamp(String requestTimestamp) {
        this.requestTimestamp = requestTimestamp;
        return this;
    }

    public String getTxnToken() {
        return txnToken;
    }

    public Head setTxnToken(String txnToken) {
        this.txnToken = txnToken;
        return this;
    }

    public String getVersion() {
        return version;
    }

    public Head setVersion(String version) {
        this.version = version;
        return this;
    }

    public String getChannelId() {
        return channelId;
    }

    public Head setChannelId(String channelId) {
        this.channelId = channelId;
        return this;
    }

    public String getWorkFlow() {
        return workFlow;
    }

    public Head setWorkFlow(String workFlow) {
        this.workFlow = workFlow;
        return this;
    }

    @Override
    public String toString() {
        return "Head{" +
                "requestTimestamp='" + requestTimestamp + '\'' +
                ", txnToken='" + txnToken + '\'' +
                ", version='" + version + '\'' +
                ", channelId='" + channelId + '\'' +
                '}';
    }
}
