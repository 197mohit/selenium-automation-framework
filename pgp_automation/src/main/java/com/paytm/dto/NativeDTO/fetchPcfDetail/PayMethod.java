

package com.paytm.dto.NativeDTO.fetchPcfDetail;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by anjukumari on 14/05/19
 */

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "payMethod",
        "instId",
        "feeRateFactors"
})
public class PayMethod {

    @JsonProperty("payMethod")
    private String payMethod;
    @JsonProperty("instId")
    private String instId;
    @JsonProperty("feeRateFactors")
    private feeRateFactors feeRateFactors;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("payMethod")
    public String getPayMethod() {
        return payMethod;
    }

    @JsonProperty("payMethod")
    public PayMethod setPayMethod(String payMethod) {
        this.payMethod = payMethod;
        return this;
    }

    @JsonProperty("instId")
    public String getInstId() {
        return instId;
    }

    @JsonProperty("instId")
    public PayMethod setInstId(String instId) {
        this.instId = instId;
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

    @JsonProperty("feeRateFactors")
    public feeRateFactors getFeeRateFactors() {
        return feeRateFactors;
    }
    @JsonProperty("feeRateFactors")
    public PayMethod setFeeRateFactors(feeRateFactors feeRateFactors) {
        this.feeRateFactors = feeRateFactors;
        return this;
    }

}