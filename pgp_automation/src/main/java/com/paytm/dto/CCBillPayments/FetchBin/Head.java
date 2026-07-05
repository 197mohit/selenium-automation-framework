package com.paytm.dto.CCBillPayments.FetchBin;
import com.fasterxml.jackson.annotation.JsonInclude;
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Head {
    private String clientId="IN";
    private String version="v1";
    private String requestTimeStamp= Long.toString(System.currentTimeMillis());
    private String channelId="APP";
    private String signature="";
    // Getter Methods
    public String getClientId() {
        return clientId;
    }
    public String getVersion() {
        return version;
    }
    public String getRequestTimeStamp() {
        return requestTimeStamp;
    }
    public String getChannelId() {
        return channelId;
    }
    public String getSignature() {
        return signature;
    }
    // Setter Methods
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
    public void setVersion(String version) {
        this.version = version;
    }
    public void setRequestTimeStamp(String requestTimeStamp) {
        this.requestTimeStamp = requestTimeStamp;
    }
    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }
    public void setSignature(String signature) {
        this.signature = signature;
    }
}
