package com.paytm.utils.merchant.DatabaseDTO.paytmpgdb;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by ankuragarwal on 9/10/18
 */
public class BankMasterDTO {

    @JsonProperty("BANK_ID")
    private Object bankId;
    @JsonProperty("BANK_NAME")
    private Object bankName;
    @JsonProperty("BANK_CODE")
    private Object bankCode;
    @JsonProperty("STATUS")
    private Object status;
    @JsonProperty("CREATED_BY")
    private Object createdby;
    @JsonProperty("CREATED_DATE")
    private Object createdDate;
    @JsonProperty("MODIFIED_BY")
    private Object modifiedBy;
    @JsonProperty("MODIFIED_DATE")
    private Object modifiedDate;
    @JsonProperty("BANK_TYPE")
    private Object bankType;
    @JsonProperty("BANK_WAP_LOGO")
    private Object bankWapLogo;
    @JsonProperty("BANK_WEB_LOGO")
    private Object bankWebLogo;
    @JsonProperty("BANK_DISPLAY_NAME")
    private Object bankDisplayName;
    @JsonProperty("DISPLAY_ORDER")
    private Object displayOrder;
    @JsonProperty("BANK_KEY")
    private Object bankKey;
    @JsonProperty("CARD_TYPE_PREF")
    private Object cardTypePref;
    @JsonProperty("IS_BANK")
    private Object isBank;
    @JsonProperty("IS_CORPORATE")
    private Object isCorporate;
    @JsonProperty("IS_INTERNATIONAL")
    private Object isInternational;
    @JsonProperty("IS_TRADE_SUPPORT")
    private Object isTradeSupport;


    public Object getBankId() {
        return bankId;
    }

    public Object getBankName() {
        return bankName;
    }

    public Object getBankCode() {
        return bankCode;
    }

    public Object getStatus() {
        return status;
    }

    public Object getCreatedby() {
        return createdby;
    }

    public Object getCreatedDate() {
        return createdDate;
    }

    public Object getModifiedBy() {
        return modifiedBy;
    }

    public Object getModifiedDate() {
        return modifiedDate;
    }

    public Object getBankType() {
        return bankType;
    }

    public Object getBankWapLogo() {
        return bankWapLogo;
    }

    public Object getBankWebLogo() {
        return bankWebLogo;
    }

    public Object getBankDisplayName() {
        return bankDisplayName;
    }

    public Object getDisplayOrder() {
        return displayOrder;
    }

    public Object getBankKey() {
        return bankKey;
    }

    public Object getCardTypePref() {
        return cardTypePref;
    }

    public Object getIsBank() {
        return isBank;
    }

    public Object getIsCorporate() {
        return isCorporate;
    }

    public Object getIsInternational() {
        return isInternational;
    }

    public Object getIsTradeSupport() {
        return isTradeSupport;
    }

    public void setBankId(Object bankId) {
        this.bankId = bankId;
    }

    public void setBankName(Object bankName) {
        this.bankName = bankName;
    }

    public void setBankCode(Object bankCode) {
        this.bankCode = bankCode;
    }

    public void setStatus(Object status) {
        this.status = status;
    }

    public void setCreatedby(Object createdby) {
        this.createdby = createdby;
    }

    public void setCreatedDate(Object createdDate) {
        this.createdDate = createdDate;
    }

    public void setModifiedBy(Object modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public void setModifiedDate(Object modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public void setBankType(Object bankType) {
        this.bankType = bankType;
    }

    public void setBankWapLogo(Object bankWapLogo) {
        this.bankWapLogo = bankWapLogo;
    }

    public void setBankWebLogo(Object bankWebLogo) {
        this.bankWebLogo = bankWebLogo;
    }

    public void setBankDisplayName(Object bankDisplayName) {
        this.bankDisplayName = bankDisplayName;
    }

    public void setDisplayOrder(Object displayOrder) {
        this.displayOrder = displayOrder;
    }

    public void setBankKey(Object bankKey) {
        this.bankKey = bankKey;
    }

    public void setCardTypePref(Object cardTypePref) {
        this.cardTypePref = cardTypePref;
    }

    public void setIsBank(Object isBank) {
        this.isBank = isBank;
    }

    public void setIsCorporate(Object isCorporate) {
        this.isCorporate = isCorporate;
    }

    public void setIsInternational(Object isInternational) {
        this.isInternational = isInternational;
    }

    public void setIsTradeSupport(Object isTradeSupport) {
        this.isTradeSupport = isTradeSupport;
    }
}
