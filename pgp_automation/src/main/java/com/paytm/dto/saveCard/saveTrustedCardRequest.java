package com.paytm.dto.saveCard;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Created by anjukumari on 24/08/18
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class saveTrustedCardRequest {
    private String cardNumber;
    private String userId;
    private String expiryDate;
    private String maskedCardNumber;
    private String tokenType;
    private String token;

    public String getUserId() {
        return userId;
    }

    public saveTrustedCardRequest setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public saveTrustedCardRequest setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
        return this;
    }

    public String getMaskedCardNumber() {
        return maskedCardNumber;
    }

    public saveTrustedCardRequest setMaskedCardNumber(String maskedCardNumber) {
        this.maskedCardNumber = maskedCardNumber;
        return this;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public saveTrustedCardRequest setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
        return this;
    }

    public String getTokenType() {
        return tokenType;
    }

    public saveTrustedCardRequest setTokenType(String tokenType) {
        this.tokenType = tokenType;
        return this;
    }

    public String getToken() {
        return token;
    }

    public saveTrustedCardRequest setToken(String token) {
        this.token = token;
        return this;
    }
}
