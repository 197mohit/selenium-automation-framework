package com.paytm.dto.saveCard;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Date;

/**
 * Created by anjukumari on 04/07/18
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SaveCardBean {
    private Long cardId;
    private String cardNumber;
    private Integer cardType;
    private String expiryDate;
    private String firstSixDigit;
    private String lastFourDigit;
    private Integer status;
    private String userId;
    private Date updated_on;
    private Date created_on;
    private transient String decryptedExpiryDate;
    private transient String decryptedCardNumber;
    private String mId;
    private String custId;
    private String cardScheme;

    public SaveCardBean(){

    }

    public Long getCardId() {
        return cardId;
    }

    public SaveCardBean setCardId(Long cardId) {
        this.cardId = cardId;
        return this;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public SaveCardBean setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
        return this;
    }

    public Integer getCardType() {
        return cardType;
    }

    public SaveCardBean setCardType(Integer cardType) {
        this.cardType = cardType;
        return this;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public SaveCardBean setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
        return this;
    }

    public String getFirstSixDigit() {
        return firstSixDigit;
    }

    public SaveCardBean setFirstSixDigit(String firstSixDigit) {
        this.firstSixDigit = firstSixDigit;
        return this;
    }

    public String getLastFourDigit() {
        return lastFourDigit;
    }

    public SaveCardBean setLastFourDigit(String lastFourDigit) {
        this.lastFourDigit = lastFourDigit;
        return this;
    }

    public Integer getStatus() {
        return status;
    }

    public SaveCardBean setStatus(Integer status) {
        this.status = status;
        return this;
    }

    public String getUserId() {
        return userId;
    }

    public SaveCardBean setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public Date getUpdated_on() {
        return updated_on;
    }

    public SaveCardBean setUpdated_on(Date updated_on) {
        this.updated_on = updated_on;
        return this;
    }

    public Date getCreated_on() {
        return created_on;
    }

    public SaveCardBean setCreated_on(Date created_on) {
        this.created_on = created_on;
        return this;
    }

    public String getDecryptedExpiryDate() {
        return decryptedExpiryDate;
    }

    public SaveCardBean setDecryptedExpiryDate(String decryptedExpiryDate) {
        this.decryptedExpiryDate = decryptedExpiryDate;
        return this;
    }

    public String getDecryptedCardNumber() {
        return decryptedCardNumber;
    }

    public SaveCardBean setDecryptedCardNumber(String decryptedCardNumber) {
        this.decryptedCardNumber = decryptedCardNumber;
        return this;
    }

    public String getmId() {
        return mId;
    }

    public SaveCardBean setmId(String mId) {
        this.mId = mId;
        return this;
    }

    public String getCustId() {
        return custId;
    }

    public SaveCardBean setCustId(String custId) {
        this.custId = custId;
        return this;
    }

    public String getCardScheme() {
        return cardScheme;
    }

    public SaveCardBean setCardScheme(String cardScheme) {
        this.cardScheme = cardScheme;
        return this;
    }
}
