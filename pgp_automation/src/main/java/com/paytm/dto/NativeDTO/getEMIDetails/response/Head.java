package com.paytm.dto.NativeDTO.getEMIDetails.response;


import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "requestId",
        "responseTimestamp",
        "version"
})
public class Head {

    @JsonProperty("requestId")
    private Object requestId;
    @JsonProperty("responseTimestamp")
    private String responseTimestamp;
    @JsonProperty("version")
    private String version;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("requestId")
    public Object getRequestId() {
        return requestId;
    }

    @JsonProperty("requestId")
    public void setRequestId(Object requestId) {
        this.requestId = requestId;
    }

    public Head withRequestId(Object requestId) {
        this.requestId = requestId;
        return this;
    }

    @JsonProperty("responseTimestamp")
    public String getResponseTimestamp() {
        return responseTimestamp;
    }

    @JsonProperty("responseTimestamp")
    public void setResponseTimestamp(String responseTimestamp) {
        this.responseTimestamp = responseTimestamp;
    }

    public Head withResponseTimestamp(String responseTimestamp) {
        this.responseTimestamp = responseTimestamp;
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
