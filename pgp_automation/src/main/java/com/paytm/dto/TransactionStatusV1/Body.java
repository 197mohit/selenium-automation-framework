package com.paytm.dto.TransactionStatusV1;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "mid",
        "orderId",
        "isCallbackUrlRequired",
        "isFinalTxnStatusRequired"
})
public class Body {

    @JsonProperty("mid")
    private String mid;
    @JsonProperty("orderId")
    private String orderId;
    @JsonProperty("isCallbackUrlRequired")
    private Boolean isCallbackUrlRequired;
    @JsonProperty("isFinalTxnStatusRequired")
    private Boolean isFinalTxnStatusRequired;

    @JsonProperty("mid")
    public String getMid() {
        return mid;
    }

    @JsonProperty("mid")
    public Body setMid(String mid) {
        this.mid = mid;
        return this;
    }

    @JsonProperty("orderId")
    public String getOrderId() {
        return orderId;
    }

    @JsonProperty("orderId")
    public Body setOrderId(String orderId) {
        this.orderId = orderId;
        return this;
    }

    @JsonProperty("isCallbackUrlRequired")
    public Boolean getIsCallbackUrlRequired() {
        return isCallbackUrlRequired;
    }

    @JsonProperty("isCallbackUrlRequired")
    public Body setIsCallbackUrlRequired(Boolean isCallbackUrlRequired) {
        this.isCallbackUrlRequired = isCallbackUrlRequired;
        return this;
    }

    @JsonProperty("isFinalTxnStatusRequired")
    public Boolean getIsFinalTxnStatusRequired() {
        return isFinalTxnStatusRequired;
    }

    @JsonProperty("isFinalTxnStatusRequired")
    public Body setIsFinalTxnStatusRequired(Boolean isFinalTxnStatusRequired) {
        this.isFinalTxnStatusRequired = isFinalTxnStatusRequired;
        return this;
    }

}