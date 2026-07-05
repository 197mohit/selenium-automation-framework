package com.paytm.dto.NativeDTO.OfferApply;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class PaymentDetails {
    @JsonProperty("orderAmount")
    private double orderAmount;
    
    @JsonProperty("paymentOptions")
    private List<PaymentOption> paymentOptions;

    public PaymentDetails() {}

    public PaymentDetails(double orderAmount, List<PaymentOption> paymentOptions) {
        this.orderAmount = orderAmount;
        this.paymentOptions = paymentOptions;
    }

    public double getOrderAmount() {
        return orderAmount;
    }

    public void setOrderAmount(double orderAmount) {
        this.orderAmount = orderAmount;
    }

    public List<PaymentOption> getPaymentOptions() {
        return paymentOptions;
    }

    public void setPaymentOptions(List<PaymentOption> paymentOptions) {
        this.paymentOptions = paymentOptions;
    }
}