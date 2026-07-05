package com.paytm.dto.refundtroubleshooting;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "clientId",
        "version",
        "requestTimestamp",
        "signature"
})
public class Head {

    @JsonProperty("clientId")
    private String clientId="";
    @JsonProperty("version")
    private String version="";
    @JsonProperty("requestTimestamp")
    private String requestTimestamp="Time";
    @JsonProperty("signature")
    private String signature="";

    /**
     * No args constructor for use in serialization
     *
     */
    public Head() {
    }

    /**
     *
     * @param requestTimestamp
     * @param signature
     * @param clientId
     * @param version
     */
    public Head(String clientId, String version, String requestTimestamp, String signature) {
        super();
        this.clientId = clientId;
        this.version = version;
        this.requestTimestamp = requestTimestamp;
        this.signature = signature;
    }

    @JsonProperty("clientId")
    public String getClientId() {
        return clientId;
    }

    @JsonProperty("clientId")
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public Head withClientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    @JsonProperty("version")
    public String getVersion() {
        return version;
    }

    @JsonProperty("version")
    public void setVersion(String version) {
        this.version = version;
    }

    public Head withVersion(String version) {
        this.version = version;
        return this;
    }

    @JsonProperty("requestTimestamp")
    public String getRequestTimestamp() {
        return requestTimestamp;
    }

    @JsonProperty("requestTimestamp")
    public void setRequestTimestamp(String requestTimestamp) {
        this.requestTimestamp = requestTimestamp;
    }

    public Head withRequestTimestamp(String requestTimestamp) {
        this.requestTimestamp = requestTimestamp;
        return this;
    }

    @JsonProperty("signature")
    public String getSignature() {
        return signature;
    }

    @JsonProperty("signature")
    public void setSignature(String signature) {
        this.signature = signature;
    }

    public Head withSignature(String signature) {
        this.signature = signature;
        return this;
    }

}