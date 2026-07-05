package com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by ankuragarwal on 16/10/18
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CardDetails {
    private String cardId;
    private String cardType;
    private String expiryDate;
    private String firstSixDigit;
    private String lastFourDigit;
    private String status;

    public String getCardId() {
        return cardId;
    }

    public String getCardType() {
        return cardType;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public String getFirstSixDigit() {
        return firstSixDigit;
    }

    public String getLastFourDigit() {
        return lastFourDigit;
    }

    public String getStatus() {
        return status;
    }

    public void setCardId(String cardId) {
        this.cardId = cardId;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public void setFirstSixDigit(String firstSixDigit) {
        this.firstSixDigit = firstSixDigit;
    }

    public void setLastFourDigit(String lastFourDigit) {
        this.lastFourDigit = lastFourDigit;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
