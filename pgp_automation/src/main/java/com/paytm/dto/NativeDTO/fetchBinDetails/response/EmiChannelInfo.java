package com.paytm.dto.NativeDTO.fetchBinDetails.response;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "planId",
        "interestRate",
        "ofMonths",
        "minAmount",
        "maxAmount",
        "emiAmount",
        "totalAmount"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmiChannelInfo {

    @JsonProperty("planId")
    private String planId;
    @JsonProperty("interestRate")
    private String interestRate;
    @JsonProperty("ofMonths")
    private String ofMonths;
    @JsonProperty("minAmount")
    private MinAmount minAmount;
    @JsonProperty("maxAmount")
    private MaxAmount maxAmount;
    @JsonProperty("emiAmount")
    private EmiAmount emiAmount;
    @JsonProperty("totalAmount")
    private TotalAmount totalAmount;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    @JsonProperty("planId")
    public String getPlanId() {
        return planId;
    }

    @JsonProperty("planId")
    public void setPlanId(String planId) {
        this.planId = planId;
    }

    @JsonProperty("interestRate")
    public String getInterestRate() {
        return interestRate;
    }

    @JsonProperty("interestRate")
    public void setInterestRate(String interestRate) {
        this.interestRate = interestRate;
    }

    @JsonProperty("ofMonths")
    public String getOfMonths() {
        return ofMonths;
    }

    @JsonProperty("ofMonths")
    public void setOfMonths(String ofMonths) {
        this.ofMonths = ofMonths;
    }

    @JsonProperty("minAmount")
    public MinAmount getMinAmount() {
        return minAmount;
    }

    @JsonProperty("minAmount")
    public void setMinAmount(MinAmount minAmount) {
        this.minAmount = minAmount;
    }

    @JsonProperty("maxAmount")
    public MaxAmount getMaxAmount() {
        return maxAmount;
    }

    @JsonProperty("maxAmount")
    public void setMaxAmount(MaxAmount maxAmount) {
        this.maxAmount = maxAmount;
    }

    @JsonProperty("emiAmount")
    public EmiAmount getEmiAmount() {
        return emiAmount;
    }

    @JsonProperty("emiAmount")
    public void setEmiAmount(EmiAmount emiAmount) {
        this.emiAmount = emiAmount;
    }

    @JsonProperty("totalAmount")
    public TotalAmount getTotalAmount() {
        return totalAmount;
    }

    @JsonProperty("totalAmount")
    public void setTotalAmount(TotalAmount totalAmount) {
        this.totalAmount = totalAmount;
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