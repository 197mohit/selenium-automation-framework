package com.paytm.dto.NativeDTO.OfferApply;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EmiOfferDetails {
    @JsonProperty("offerId")
    private String offerId;

    public EmiOfferDetails() {}

    public EmiOfferDetails(String offerId) {
        this.offerId = offerId;
    }

    public String getOfferId() {
        return offerId;
    }

    public void setOfferId(String offerId) {
        this.offerId = offerId;
    }
}