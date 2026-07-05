package com.paytm.dto.saveCard.SavedcardOpenAPIServiceCardTypeSsoToken;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "clientId",
        "signature"
})
public class Head {

    @JsonProperty("clientId")
    private String clientId="C11";
    @JsonProperty("signature")
    private String signature="";
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     *
     */
    public Head() {
    }

    /**
     *
     * @param signature
     * @param clientId
     */
    public Head(String clientId, String signature) {
        super();
        this.clientId = clientId;
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

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public Head withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

}
