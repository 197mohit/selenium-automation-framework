package com.paytm.dto.PromoDTO.ApplyPromoDTO;


import com.fasterxml.jackson.annotation.*;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "mid",
        "promocode",
        "totalTransactionAmount",
        "paymentOptions"
})
public class Body implements Serializable
{
    @JsonProperty("mid")
    private String mid;
    @JsonProperty("promocode")
    private String promocode;
    @JsonProperty("totalTransactionAmount")
    private String totalTransactionAmount;
    @JsonProperty("paymentOptions")
    private PaymentOptions[] paymentOptions;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();
    private final static long serialVersionUID = 7416053253385020526L;

    @JsonProperty("mid")
    public String getMid() {
        return mid;
    }

    @JsonProperty("mid")
    public com.paytm.dto.PromoDTO.ApplyPromoDTO.Body setMid(String mid) {
        this.mid = mid;
        return this;
    }

    @JsonProperty("promocode")
    public String getpromocode() {
        return promocode;
    }

    @JsonProperty("promocode")
    public com.paytm.dto.PromoDTO.ApplyPromoDTO.Body setPromocode(String promocode) {
        this.promocode = promocode;
        return this;
    }

    @JsonProperty("totalTransactionAmount")
    public String getTotalTransactionAmount() {
        return totalTransactionAmount;
    }

    @JsonProperty("totalTransactionAmount")
    public com.paytm.dto.PromoDTO.ApplyPromoDTO.Body setTotalTransactionAmount(String totalTransactionAmount) {
        this.totalTransactionAmount = totalTransactionAmount;
        return this;
    }

    @JsonProperty("paymentOptions")
    public PaymentOptions[] getPaymentOptions() {
        return paymentOptions;
    }

    @JsonProperty("paymentOptions")
    public com.paytm.dto.PromoDTO.ApplyPromoDTO.Body setPaymentOptions(PaymentOptions[] paymentOptions) {
        this.paymentOptions = paymentOptions;
        return this;
    }

    /*@JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }*/
}
