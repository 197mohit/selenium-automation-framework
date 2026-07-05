package com.paytm.dto.NativeDTO.getEMIDetails.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "emiId",
        "planId",
        "interestRate",
        "ofMonths",
        "minAmount",
        "maxAmount"
})
public class EmiChannelInfo {

    @JsonProperty("emiId")
    private String emiId;
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
    @JsonProperty("bankId")
    private String bankId;


    @JsonProperty("emiId")
    public String getEmiId() {
        return emiId;
    }

    @JsonProperty("emiId")
    public void setEmiId(String emiId) {
        this.emiId = emiId;
    }

    public EmiChannelInfo withEmiId(String emiId) {
        this.emiId = emiId;
        return this;
    }

    @JsonProperty("planId")
    public String getPlanId() {
        return planId;
    }

    @JsonProperty("planId")
    public void setPlanId(String planId) {
        this.planId = planId;
    }

    public EmiChannelInfo withPlanId(String planId) {
        this.planId = planId;
        return this;
    }

    @JsonProperty("interestRate")
    public String getInterestRate() {
        return interestRate;
    }

    @JsonProperty("interestRate")
    public void setInterestRate(String interestRate) {
        this.interestRate = interestRate;
    }

    public EmiChannelInfo withInterestRate(String interestRate) {
        this.interestRate = interestRate;
        return this;
    }

    @JsonProperty("ofMonths")
    public String getOfMonths() {
        return ofMonths;
    }

    @JsonProperty("ofMonths")
    public void setOfMonths(String ofMonths) {
        this.ofMonths = ofMonths;
    }

    public EmiChannelInfo withOfMonths(String ofMonths) {
        this.ofMonths = ofMonths;
        return this;
    }

    @JsonProperty("minAmount")
    public MinAmount getMinAmount() {
        return minAmount;
    }

    @JsonProperty("minAmount")
    public void setMinAmount(MinAmount minAmount) {
        this.minAmount = minAmount;
    }

    public EmiChannelInfo withMinAmount(MinAmount minAmount) {
        this.minAmount = minAmount;
        return this;
    }

    @JsonProperty("maxAmount")
    public MaxAmount getMaxAmount() {
        return maxAmount;
    }

    @JsonProperty("maxAmount")
    public void setMaxAmount(MaxAmount maxAmount) {
        this.maxAmount = maxAmount;
    }

    public EmiChannelInfo withMaxAmount(MaxAmount maxAmount) {
        this.maxAmount = maxAmount;
        return this;
    }

    @JsonProperty("bankId")
    public String getBankId() {
        return bankId;
    }

    @JsonProperty("bankId")
    public void setBankId(String bankId) {
        this.bankId = bankId;
    }

    public EmiChannelInfo withBankId(String bankId) {
        this.bankId = bankId;
        return this;
    }

}