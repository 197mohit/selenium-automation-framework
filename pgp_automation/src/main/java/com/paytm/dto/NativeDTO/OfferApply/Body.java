package com.paytm.dto.NativeDTO.OfferApply;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class Body {
    @JsonProperty("amountBasedBankOffer")
    private Boolean amountBasedBankOffer;

    @JsonProperty("amountBasedSubvention")
    private Boolean amountBasedSubvention;
    
    @JsonProperty("custId")
    private String custId;
    
    @JsonProperty("items")
    private List<Item> items;
    
    @JsonProperty("mid")
    private String mid;
    
    @JsonProperty("paymentDetails")
    private PaymentDetails paymentDetails;
    
    @JsonProperty("paytmUserId")
    private String paytmUserId;
    
    @JsonProperty("promoContext")
    private PromoContext promoContext;

    public Body() {}

    public Body(Boolean amountBasedBankOffer, Boolean amountBasedSubvention, String custId, List<Item> items,
               String mid, PaymentDetails paymentDetails, String paytmUserId,
               PromoContext promoContext) {
        this.amountBasedBankOffer = amountBasedBankOffer;
        this.amountBasedSubvention = amountBasedSubvention;
        this.custId = custId;
        this.items = items;
        this.mid = mid;
        this.paymentDetails = paymentDetails;
        this.paytmUserId = paytmUserId;
        this.promoContext = promoContext;
    }

    public Boolean isAmountBasedBankOffer() {
        return amountBasedBankOffer;
    }

    public void setAmountBasedBankOffer(Boolean amountBasedBankOffer) {
        this.amountBasedBankOffer = amountBasedBankOffer;
    }

    public Boolean AmountBasedSubvention() {
        return amountBasedSubvention;
    }

    public void setAmountBasedSubvention(Boolean amountBasedSubvention) {
        this.amountBasedSubvention = amountBasedSubvention;
    }

    public String getCustId() {
        return custId;
    }

    public void setCustId(String custId) {
        this.custId = custId;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public PaymentDetails getPaymentDetails() {
        return paymentDetails;
    }

    public void setPaymentDetails(PaymentDetails paymentDetails) {
        this.paymentDetails = paymentDetails;
    }

    public String getPaytmUserId() {
        return paytmUserId;
    }

    public void setPaytmUserId(String paytmUserId) {
        this.paytmUserId = paytmUserId;
    }

    public PromoContext getPromoContext() {
        return promoContext;
    }

    public void setPromoContext(PromoContext promoContext) {
        this.promoContext = promoContext;
    }
}