/**
 * @author :- Samar Aswal
 * @desc :- This a body structer of close order V2 API
 */


package com.paytm.dto.CloseOrderV2;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "mid",
        "orderId"
})
public class Body {

    @JsonProperty("mid")
    private String mid;
    @JsonProperty("orderId")
    private String orderId;
    @JsonProperty("forceClose")
    private String forceClose;

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

    @JsonProperty("forceClose")
    public String getForceClose() {
        return forceClose;
    }

    @JsonProperty("forceClose")
    public Body setForceClose(String forceClose) {
        this.forceClose = forceClose;
        return this;
    }
}