package com.paytm.dto.mappingService.GetBrandEmiDetail.response;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

public class Plan {

    @JsonProperty("id")
    private Integer id;
    @JsonProperty("planId")
    private String planId;
    @JsonProperty("bank")
    private String bank;
    @JsonProperty("month")
    private Integer month;
    @JsonProperty("interest")
    private Double interest;
    @JsonProperty("minAmount")
    private Double minAmount;
    @JsonProperty("maxAmount")
    private Double maxAmount;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("id")
    public Integer getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(Integer id) {
        this.id = id;
    }

    public Plan withId(Integer id) {
        this.id = id;
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

    public Plan withPlanId(String planId) {
        this.planId = planId;
        return this;
    }

    @JsonProperty("bank")
    public String getBank() {
        return bank;
    }

    @JsonProperty("bank")
    public void setBank(String bank) {
        this.bank = bank;
    }

    public Plan withBank(String bank) {
        this.bank = bank;
        return this;
    }

    @JsonProperty("month")
    public Integer getMonth() {
        return month;
    }

    @JsonProperty("month")
    public void setMonth(Integer month) {
        this.month = month;
    }

    public Plan withMonth(Integer month) {
        this.month = month;
        return this;
    }

    @JsonProperty("interest")
    public Double getInterest() {
        return interest;
    }

    @JsonProperty("interest")
    public void setInterest(Double interest) {
        this.interest = interest;
    }

    public Plan withInterest(Double interest) {
        this.interest = interest;
        return this;
    }

    @JsonProperty("minAmount")
    public Double getMinAmount() {
        return minAmount;
    }

    @JsonProperty("minAmount")
    public void setMinAmount(Double minAmount) {
        this.minAmount = minAmount;
    }

    public Plan withMinAmount(Double minAmount) {
        this.minAmount = minAmount;
        return this;
    }

    @JsonProperty("maxAmount")
    public Double getMaxAmount() {
        return maxAmount;
    }

    @JsonProperty("maxAmount")
    public void setMaxAmount(Double maxAmount) {
        this.maxAmount = maxAmount;
    }

    public Plan withMaxAmount(Double maxAmount) {
        this.maxAmount = maxAmount;
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

    public Plan withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }
}
