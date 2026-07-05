package com.paytm.dto.NativeDTO.getEMIDetails.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "version",
        "requestTimestamp",
        "requestId",
        "clientId",
        "signature"
})
public class Head {

    @JsonProperty(value = "version",defaultValue = "v1")
    private String version;
    @JsonProperty("requestTimestamp")
    private String requestTimestamp;
    @JsonProperty("requestId")
    private String requestId;
    @JsonProperty("clientId")
    private String clientId;
    @JsonProperty("signature")
    private String signature;

    public Head(){
        this.version = "v1";
        this.requestTimestamp = "Time";
        this.requestId = "";
        this.clientId = "subvention";
    }
    public Head(String signature) {
        super();
        this.version = "v1";
        this.requestTimestamp = "Time";
        this.requestId = "";
        this.clientId = "subvention";
        this.signature=signature;
    }
    @JsonProperty("version")
    public String getVersion() {
        return version;
    }

    public Head setVersion(String version) {
        this.version = version;
        return this;
    }

    @JsonProperty("requestTimestamp")
    public String getRequestTimestamp() {
        return requestTimestamp;
    }

    @JsonProperty(value = "requestTimestamp")
    public Head setRequestTimestamp(String requestTimestamp) {
        this.requestTimestamp = requestTimestamp;
        return this;
    }

    @JsonProperty(value = "requestId")
    public String getRequestId() {
        return requestId;
    }

    @JsonProperty(value = "requestId",defaultValue = "")
    public Head  setRequestId(String requestId) {
        this.requestId = requestId;
        return this;
    }

    @JsonProperty("clientId")
    public String getClientId() {
        return clientId;
    }

    @JsonProperty(value = "clientId",defaultValue = "subvention")
    public Head setClientId(String clientId) {
        this.clientId = clientId;
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

}