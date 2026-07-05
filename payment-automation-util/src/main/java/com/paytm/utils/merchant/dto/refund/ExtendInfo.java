package com.paytm.utils.merchant.dto.refund;

import com.fasterxml.jackson.annotation.*;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "uniqueTxnId"
})
public class ExtendInfo implements Serializable
{
    @JsonProperty("uniqueTxnId")
    private String uniqueTxnId;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();
    private final static long serialVersionUID = -2484580176719415435L;

    @JsonProperty("uniqueTxnId")
    public String getUniqueTxnId() {
        return uniqueTxnId;
    }


    @JsonProperty("uniqueTxnId")
    public ExtendInfo setUniqueTxnId(String uniqueTxnId) {
        this.uniqueTxnId = uniqueTxnId;
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

}