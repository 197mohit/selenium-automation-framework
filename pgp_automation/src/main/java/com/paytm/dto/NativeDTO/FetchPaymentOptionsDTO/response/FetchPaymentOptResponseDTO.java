package com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.exceptions.NoSuchKeyException;

import java.util.List;

/**
 * Created by ankuragarwal on 16/10/18
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class FetchPaymentOptResponseDTO {

    private  Head head;
    private Body body;

    public Head getHead() {
        return head;
    }

    public void setHead(Head head) {
        this.head = head;
    }

    public Body getBody() {
        return body;
    }

    public void setBody(Body body) {
        this.body = body;
    }

    public PaymentModes getMerchantPaymentMode(String paymentMode) throws NoSuchKeyException {
        List<PaymentModes> paymentModes = this.body.getMerchantPayOption().getPaymentModes();
        for (PaymentModes payMode : paymentModes) {
            if(payMode.getPaymentMode().equalsIgnoreCase(paymentMode))
                return payMode;
        }
        throw new NoSuchKeyException(paymentMode + " not found in Fetch payment option response");
    }

    public SavedInstruments getMerchantSavedInstrument(String cardId) throws NoSuchKeyException {
        List<SavedInstruments> savedInstrumentsList = this.body.getMerchantPayOption().getSavedInstruments();
        for (SavedInstruments instrument : savedInstrumentsList) {
            if(instrument.getCardDetails().getCardId().equalsIgnoreCase(cardId))
                return instrument;
        }
        throw new NoSuchKeyException(cardId + " not found in Fetch payment option response");
    }

    public PaymentModes getAddMoneyPaymentMode(String paymentMode) throws NoSuchKeyException {
        List<PaymentModes> paymentModes = this.body.getAddMoneyPayOption().getPaymentModes();
        for (PaymentModes payMode : paymentModes) {
            if(payMode.getPaymentMode().equalsIgnoreCase(paymentMode))
                return payMode;
        }
        throw new NoSuchKeyException(paymentMode + " not found in Fetch payment option response");
    }

    @Override
    public String toString() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return "";
    }
}
