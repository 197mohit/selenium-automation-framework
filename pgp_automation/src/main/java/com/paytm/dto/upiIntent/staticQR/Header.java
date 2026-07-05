package com.paytm.dto.upiIntent.staticQR;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "clientId",
        "requestTimestamp",
        "version",
        "requestMsgId",
        "signature"
})
public class Header {

    @JsonProperty("clientId")
    private Object clientId;
    @JsonProperty("requestTimestamp")
    private String requestTimestamp;
    @JsonProperty("version")
    private String version;
    @JsonProperty("requestMsgId")
    private String requestMsgId;
    @JsonProperty("signature")
    private String signature;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    @JsonProperty("clientId")
    public Object getClientId() {
        return clientId;
    }

    @JsonProperty("clientId")
    public Header setClientId(Object clientId) {
        this.clientId = clientId;
        return this;
    }

    @JsonProperty("requestTimestamp")
    public String getRequestTimestamp() {
        return requestTimestamp;
    }

    @JsonProperty("requestTimestamp")
    public Header setRequestTimestamp(String requestTimestamp) {
        this.requestTimestamp = requestTimestamp;
        return this;
    }

    @JsonProperty("version")
    public String getVersion() {
        return version;
    }

    @JsonProperty("version")
    public Header setVersion(String version) {
        this.version = version;
        return this;
    }

    @JsonProperty("requestMsgId")
    public String getRequestMsgId() {
        return requestMsgId;
    }

    @JsonProperty("requestMsgId")
    public Header setRequestMsgId(String requestMsgId) {
        this.requestMsgId = requestMsgId;
        return this;
    }

    @JsonProperty("signature")
    public String getSignature() {
        return signature;
    }

    @JsonProperty("signature")
    public Header setSignature(String signature) {
        this.signature = signature;
        return this;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public Header setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

}