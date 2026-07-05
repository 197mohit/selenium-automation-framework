package com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Created by ankuragarwal on 16/10/18
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MerchantPayOption {
    private List<PaymentModes> paymentModes;
    private List<SavedInstruments> savedInstruments;

    public List<PaymentModes> getPaymentModes() {
        return paymentModes;
    }

    public void setPaymentModes(List<PaymentModes> paymentModes) {
        this.paymentModes = paymentModes;
    }

    public List<SavedInstruments> getSavedInstruments() {
        return savedInstruments;
    }

    public void setSavedInstruments(List<SavedInstruments> savedInstruments) {
        this.savedInstruments = savedInstruments;
    }
}
