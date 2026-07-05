package com.paytm.dto.GetPaymentStatusRequest;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "mid",
        "aggMid",
        "orderId"
})
public class Body {
    @JsonProperty("mid")
    private String mid;
    @JsonProperty("aggMid")
    private String aggMid;
    @JsonProperty("orderId")
    private String orderId;

    public Body(String orderId, String aggMid, String mid) {
        this.orderId = orderId;
        this.mid = mid;
        this.aggMid = aggMid;
    }

    @JsonProperty("mid")
    public String getMid() {
        return mid;
    }

    @JsonProperty("mid")
    public void setMid(String mid) {
        this.mid = mid;
    }

    @JsonProperty("aggMid")
    public String getAggMid() {
        return aggMid;
    }

    @JsonProperty("aggMid")
    public void setAggMid(String aggMid) {
        this.aggMid = aggMid;
    }

    @JsonProperty("orderId")
    public String getOrderId() {
        return orderId;
    }

    @JsonProperty("orderId")
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

}
