package com.paytm.dto.NativeDTO.OfferApply;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class OfferDetails {
    @JsonProperty("bankOfferDetails")
    private List<Object> bankOfferDetails;

    @JsonProperty("emiOfferDetails")
    private EmiOfferDetails emiOfferDetails;

    public OfferDetails() {}

    public OfferDetails(List<Object> bankOfferDetails) {
        this.bankOfferDetails = bankOfferDetails;
    }

    public List<Object> getBankOfferDetails() {
        return bankOfferDetails;
    }

    public void setBankOfferDetails(List<Object> bankOfferDetails) {
        this.bankOfferDetails = bankOfferDetails;
    }

    public EmiOfferDetails getEmiOfferDetails() {
        return emiOfferDetails;
    }

    public void setEmiOfferDetails(EmiOfferDetails emiOfferDetails) {
        this.emiOfferDetails = emiOfferDetails;
    }
}