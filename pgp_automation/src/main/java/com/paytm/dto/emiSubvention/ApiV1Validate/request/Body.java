package com.paytm.dto.emiSubvention.ApiV1Validate.request;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "mid",
        "customerId",
        "cacheCardToken",
        "planId",
        "items",
        "paymentDetails",
        "generateTokenForIntent",
        "price",
        "originalPrice",
        "subventionAmount",
        "offerDetails"

})
public class Body implements Serializable {

    @JsonProperty("mid")
    private String mid;
    @JsonProperty("customerId")
    private String customerId;
    @JsonProperty("cacheCardToken")
    private String cacheCardToken;
    @JsonProperty("planId")
    private BigInteger planId;
    @JsonProperty("items")
    private List<Item> items = null;
    @JsonProperty("paymentDetails")
    private PaymentDetails paymentDetails;
    @JsonProperty("generateTokenForIntent")
    private Boolean generateTokenForIntent;
    @JsonProperty("price")
    private String price;
    @JsonProperty("originalPrice")
    private String originalPrice;
    @JsonProperty("subventionAmount")
    private String subventionAmount;
    @JsonProperty("offerDetails")
    private OfferDetails offerDetails;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();
    private final static long serialVersionUID = -5249560350551345849L;

    @JsonProperty("cacheCardToken")
    public String getCacheCardToken() {
        return cacheCardToken;
    }

    @JsonProperty("cacheCardToken")
    public Body setCacheCardToken(String cacheCardToken) {
        this.cacheCardToken = cacheCardToken;
        return this;
    }

    @JsonProperty("mid")
    public String getMid() {
        return mid;
    }

    @JsonProperty("mid")
    public Body setMid(String mid) {
        this.mid = mid;
        return this;
    }

    @JsonProperty("customerId")
    public String getCustomerId() {
        return customerId;
    }

    @JsonProperty("customerId")
    public Body setCustomerId(String customerId) {
        this.customerId = customerId;
        return this;
    }

    @JsonProperty("planId")
    public BigInteger getPlanId() {
        return planId;
    }

    @JsonProperty("planId")
    public Body setPlanId(BigInteger planId) {
        this.planId = planId;
        return this;
    }

    @JsonProperty("price")
    public String getPrice() {
        return price;
    }

    @JsonProperty("price")
    public Body setPrice(String price) {
        this.price = price;
        return this;
    }
    @JsonProperty("originalPrice")
    public String getOriginalPrice() {
        return originalPrice;
    }

    @JsonProperty("originalPrice")
    public Body setOriginalPrice(String originalPrice) {
        this.originalPrice = originalPrice;
        return this;
    }

    @JsonProperty("subventionAmount")
    public String getSubventionAmount() {
        return subventionAmount;
    }

    @JsonProperty("subventionAmount")
    public Body setSubventionAmount(String subventionAmount) {
        this.subventionAmount = subventionAmount;
        return this;
    }

    @JsonProperty("offerDetails")
    public OfferDetails getOfferDetails() {
        return offerDetails;
    }

    @JsonProperty("offerDetails")
    public Body setOfferDetails(OfferDetails offerDetails) {
        this.offerDetails = offerDetails;
        return this;
    }

    @JsonProperty("items")
    public List<Item> getItems() {
        return items;
    }

    @JsonProperty("items")
    public Body setItems(List<Item> items) {
        this.items = items;
        return this;
    }

    @JsonProperty("paymentDetails")
    public PaymentDetails getPaymentDetails() {
        return paymentDetails;
    }

    @JsonProperty("paymentDetails")
    public Body setPaymentDetails(PaymentDetails paymentDetails) {
        this.paymentDetails = paymentDetails;
        return this;
    }

    @JsonProperty("generateTokenForIntent")
    public Boolean getGenerateTokenForIntent() {
        return generateTokenForIntent;
    }

    @JsonProperty("generateTokenForIntent")
    public Body setGenerateTokenForIntent(Boolean generateTokenForIntent) {
        this.generateTokenForIntent = generateTokenForIntent;
        return this;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public Body setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

    @Override
    public String toString() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return "";
    }

}
