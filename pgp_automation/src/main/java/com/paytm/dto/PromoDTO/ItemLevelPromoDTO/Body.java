package com.paytm.dto.PromoDTO.ItemLevelPromoDTO;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "mid",
        "promocode",
        "paymentOptions" ,
        "totalTransactionAmount",
        "promoContext"
})
public class Body implements Serializable
{
    @JsonProperty("mid")
    private String mid;
    @JsonProperty("promocode")
    private String promocode;
    @JsonProperty("paymentOptions")
    private PaymentOptions[] paymentOptions;
    @JsonProperty("totalTransactionAmount")
    private String totalTransactionAmount;
    @JsonProperty("promoContext")
    private String promoContext;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();
    private final static long serialVersionUID = 7416053253385020526L;

    @JsonProperty("mid")
    public String getMid() {
        return mid;
    }

    @JsonProperty("mid")
    public Body setMid(String mid) {
        this.mid = mid;
        return this;
    }

    @JsonProperty("promocode")
    public String getpromocode() {
        return promocode;
    }

    @JsonProperty("promocode")
    public Body setPromocode(String promocode) {
        this.promocode = promocode;
        return this;
    }

    @JsonProperty("totalTransactionAmount")
    public String getTotalTransactionAmount() {
        return totalTransactionAmount;
    }

    @JsonProperty("totalTransactionAmount")
    public Body setTotalTransactionAmount(String totalTransactionAmount) {
        this.totalTransactionAmount = totalTransactionAmount;
        return this;
    }

    @JsonProperty("paymentOptions")
    public PaymentOptions[] getPaymentOptions() {
        return paymentOptions;
    }

    @JsonProperty("paymentOptions")
    public Body setPaymentOptions(PaymentOptions[] paymentOptions) {
        this.paymentOptions = paymentOptions;
        return this;
    }
    @JsonProperty("promoContext")
    public String getpromoContext() {
        return promoContext;
    }

    @JsonProperty("promoContext")
    public Body setpromoContext(String promoContext) {
        this.promoContext = promoContext;
        return this;
    }

}
