package com.paytm.dto.coft;

public class CardData {
    private String cardNumber;
    private String expiryMonth;
    private String expiryYear;
    private String securityCode;

    public CardData setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
        return this;
    }

    public CardData setCardData(String cardNumber, String expiryMonth, String expiryYear, String cvv) {
        this.cardNumber = cardNumber;
        this.expiryMonth = expiryMonth;
        this.expiryYear = expiryYear;
        this.securityCode = cvv;
        return this;
    }
}
