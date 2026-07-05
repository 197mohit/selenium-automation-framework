package com.paytm.dto.upiIntent.staticQR;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreditCardInfo {

    @JsonProperty("binNumber")
    private String binNumber;
    @JsonProperty("creditAccountReferenceNumber")
    private String creditAccountReferenceNumber;
    @JsonProperty("cardType")
    private String cardType;

    public CreditCardInfo(String binNumber, String creditAccountReferenceNumber, String cardType) {
        this.binNumber = binNumber;
        this.creditAccountReferenceNumber = creditAccountReferenceNumber;
        this.cardType = cardType;
    }

    @JsonProperty("binNumber")
    public String getBinNumber() {
        return binNumber;
    }

    @JsonProperty("binNumber")
    public CreditCardInfo setBinNumber(String binNumber) {
        this.binNumber = binNumber;
        return this;
    }

    @JsonProperty("creditAccountReferenceNumber")
    public String getCreditAccountReferenceNumber() {
        return creditAccountReferenceNumber;
    }

    @JsonProperty("creditAccountReferenceNumber")
    public CreditCardInfo setCreditAccountReferenceNumber(String creditAccountReferenceNumber) {
        this.creditAccountReferenceNumber = creditAccountReferenceNumber;
        return this;
    }

    @JsonProperty("cardType")
    public String getCardType() {
        return cardType;
    }

    @JsonProperty("cardType")
    public CreditCardInfo setCardType(String cardType) {
        this.cardType = cardType;
        return this;
    }
}
