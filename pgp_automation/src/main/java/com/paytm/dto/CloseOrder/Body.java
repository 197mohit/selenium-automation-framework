package com.paytm.dto.CloseOrder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "mid",
        "orderId",
        "isForceClose"
})
public class Body {

    @JsonProperty("mid")
    private String mid;
    @JsonProperty("orderId")
    private String orderId;
    @JsonProperty("isForceClose")
    private Boolean isForceClose;

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

    @JsonProperty("isForceClose")
    public Boolean getIsForceClose() {
        return isForceClose;
    }

    @JsonProperty("isForceClose")
    public Body setIsForceClose(Boolean isForceClose) {
        this.isForceClose = isForceClose;
        return this;
    }

}