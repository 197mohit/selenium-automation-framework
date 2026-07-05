package com.paytm.dto.saveCard;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Date;

/**
 * Created by anjukumari on 30/08/18
 */

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SaveCardRequestGeneric {
    private String paymentTypeId;
    private String cardNumber;
    private String userId;
    private Object status;
    private String cardType;
    private String expiryDate;
    private String firstSixDigit;
    private String lastFourDigit;
    private String transactionId;
    private Date created_on;
    private Date updated_on;
    private String custId;
    private String mId;

    public String getPaymentTypeId() {
        return paymentTypeId;
    }

    public SaveCardRequestGeneric setPaymentTypeId(String paymentTypeId) {
        this.paymentTypeId = paymentTypeId;
        return this;
    }


    public SaveCardRequestGeneric setStatus(Object status) {
        this.status = status;
        return this;
    }

    public Date getCreated_on() {
        return created_on;
    }

    public SaveCardRequestGeneric setCreated_on(Date created_on) {
        this.created_on = created_on;
        return this;
    }

    public Date getUpdated_on() {
        return updated_on;
    }

    public SaveCardRequestGeneric setUpdated_on(Date updated_on) {
        this.updated_on = updated_on;
        return this;
    }

    public String getCustId() {
        return custId;
    }

    public SaveCardRequestGeneric setCustId(String custId) {
        this.custId = custId;
        return this;
    }

    public String getmId() {
        return mId;
    }

    public SaveCardRequestGeneric setmId(String mId) {
        this.mId = mId;
        return this;
    }

    public Long getCardId() {
        return cardId;
    }

    public SaveCardRequestGeneric setCardId(Long cardId) {
        this.cardId = cardId;
        return this;
    }

    private Long cardId;


    public SaveCardRequestGeneric() {

    }

    public SaveCardRequestGeneric(String cardNumber, String userId, Integer status, String cardType, String expiryDate, String firstSixDigit, String lastFourDigit, String transactionId) {
        this.cardNumber = cardNumber;
        this.userId = userId;
        this.status = status;
        this.cardType = cardType;
        this.expiryDate = expiryDate;
        this.firstSixDigit = firstSixDigit;
        this.lastFourDigit = lastFourDigit;
        this.transactionId = transactionId;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public SaveCardRequestGeneric setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
        return this;
    }

    public String getUserId() {
        return userId;
    }

    public SaveCardRequestGeneric setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public Object getStatus() {
        return status;
    }



    public String getCardType() {
        return cardType;
    }

    public SaveCardRequestGeneric setCardType(String cardType) {
        this.cardType = cardType;
        return this;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public SaveCardRequestGeneric setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
        return this;
    }

    public String getFirstSixDigit() {
        return firstSixDigit;
    }

    public SaveCardRequestGeneric setFirstSixDigit(String firstSixDigit) {
        this.firstSixDigit = firstSixDigit;
        return this;
    }

    public String getLastFourDigit() {
        return lastFourDigit;
    }

    public SaveCardRequestGeneric setLastFourDigit(String lastFourDigit) {
        this.lastFourDigit = lastFourDigit;
        return this;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public SaveCardRequestGeneric setTransactionId(String transactionId) {
        this.transactionId = transactionId;
        return this;
    }
}
