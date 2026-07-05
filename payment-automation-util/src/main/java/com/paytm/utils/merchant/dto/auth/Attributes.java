package com.paytm.utils.merchant.dto.auth;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "POSTPAID_STATUS"
})
public class Attributes {

    @JsonProperty("POSTPAID_STATUS")
    private String pOSTPAIDSTATUS;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    @JsonProperty("POSTPAID_STATUS")
    public String getPOSTPAIDSTATUS() {
        return pOSTPAIDSTATUS;
    }

    @JsonProperty("POSTPAID_STATUS")
    public void setPOSTPAIDSTATUS(String pOSTPAIDSTATUS) {
        this.pOSTPAIDSTATUS = pOSTPAIDSTATUS;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}