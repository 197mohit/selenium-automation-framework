package com.paytm.dto.ApplyPromoV2DTO;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "mid",
        "promocode",
        "paymentOptions",
        "custId",
        "totalTransactionAmount",
        "cartDetails"
})
public class Body {
    @JsonProperty("mid")
    private String mid;

    @JsonProperty("orderId")
    private String orderId;
    @JsonProperty("promocode")
    private String promocode;
    @JsonProperty("paymentOptions")
    private List<PaymentOption> paymentOptions;
    @JsonProperty("custId")
    private String custId;
    @JsonProperty("totalTransactionAmount")
    private String totalTransactionAmount;
    @JsonProperty("cartDetails")
    private CartDetails cartDetails;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

    @JsonProperty("mid")
    public String getMid() {
        return mid;
    }

    @JsonProperty("mid")
    public Body setMid(String mid) {
        this.mid = mid;
        return this;
    }

    public String getOrderId() {
        return orderId;
    }

    public Body setOrderId(String orderId) {
        this.orderId = orderId;
        return this;
    }

    @JsonProperty("promocode")
    public String getPromocode() {
        return promocode;
    }

    @JsonProperty("promocode")
    public Body setPromocode(String promocode) {
        this.promocode = promocode;
        return this;
    }

    @JsonProperty("paymentOptions")
    public List<PaymentOption> getPaymentOptions() {
        return paymentOptions;
    }

    @JsonProperty("paymentOptions")
    public Body setPaymentOptions(List<PaymentOption> paymentOptions) {
        this.paymentOptions = paymentOptions;
        return this;
    }

    @JsonProperty("custId")
    public String getCustId() {
        return custId;
    }

    @JsonProperty("custId")
    public Body setCustId(String custId) {
        this.custId = custId;
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

    @JsonProperty("cartDetails")
    public CartDetails getCartDetails() {
        return cartDetails;
    }

    @JsonProperty("cartDetails")
    public Body setCartDetails(CartDetails cartDetails) {
        this.cartDetails = cartDetails;
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
