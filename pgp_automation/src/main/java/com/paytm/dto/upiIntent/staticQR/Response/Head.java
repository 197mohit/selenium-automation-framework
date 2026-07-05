package com.paytm.dto.upiIntent.staticQR.Response;

import com.fasterxml.jackson.annotation.*;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "responseTimestamp",
        "version"
})
public class Head {

    @JsonProperty("responseTimestamp")
    private BigInteger responseTimestamp;
    @JsonProperty("version")
    private String version;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    @JsonProperty("responseTimestamp")
    public BigInteger getResponseTimestamp() {
        return responseTimestamp;
    }

    @JsonProperty("responseTimestamp")
    public void setResponseTimestamp(BigInteger responseTimestamp) {
        this.responseTimestamp = responseTimestamp;
    }

    @JsonProperty("version")
    public String getVersion() {
        return version;
    }

    @JsonProperty("version")
    public void setVersion(String version) {
        this.version = version;
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
