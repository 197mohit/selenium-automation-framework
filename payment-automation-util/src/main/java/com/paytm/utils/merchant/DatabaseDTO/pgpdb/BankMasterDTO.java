package com.paytm.utils.merchant.DatabaseDTO.pgpdb;

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
    private Object createdBy;
    @JsonProperty("CREATED_DATE")
    private Object createdDate;
    @JsonProperty("MODIFIED_BY")
    private Object modifiedBy;
    @JsonProperty("MODIFIED_DATE")
    private Object modifiedDate;
    @JsonProperty("BANK_DISPLAY_NAME")
    private Object bankDisplayName;
    @JsonProperty("BANK_KEY")
    private Object bankKey;
    @JsonProperty("ALIPAY_CODE")
    private Object alipayCode;
    @JsonProperty("BANK_WAP_LOGO")
    private Object bankWapLogo;
    @JsonProperty("BANK_WEB_LOGO")
    private Object bankWebLogo;


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

    public Object getCreatedBy() {
        return createdBy;
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

    public Object getBankDisplayName() {
        return bankDisplayName;
    }

    public Object getBankKey() {
        return bankKey;
    }

    public Object getAlipayCode() {
        return alipayCode;
    }

    public Object getBankWapLogo() {
        return bankWapLogo;
    }

    public Object getBankWebLogo() {
        return bankWebLogo;
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

    public void setCreatedBy(Object createdBy) {
        this.createdBy = createdBy;
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

    public void setBankDisplayName(Object bankDisplayName) {
        this.bankDisplayName = bankDisplayName;
    }

    public void setBankKey(Object bankKey) {
        this.bankKey = bankKey;
    }

    public void setAlipayCode(Object alipayCode) {
        this.alipayCode = alipayCode;
    }

    public void setBankWapLogo(Object bankWapLogo) {
        this.bankWapLogo = bankWapLogo;
    }

    public void setBankWebLogo(Object bankWebLogo) {
        this.bankWebLogo = bankWebLogo;
    }
}
