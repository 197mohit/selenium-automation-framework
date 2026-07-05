package com.paytm.dto.NativeDTO.InitTxn;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AffordabilityInfo {
    @JsonProperty("paymentFlow")
    private String paymentFlow;

    public AffordabilityInfo(String paymentFlow) {
        this.paymentFlow = paymentFlow;
    }

    public String getPaymentFlow() {
        return paymentFlow;
    }

    public void setPaymentFlow(String paymentFlow) {
        this.paymentFlow = paymentFlow;
    }
}