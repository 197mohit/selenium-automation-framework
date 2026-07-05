package com.paytm.utils.merchant.dto.refund.refundStatusV1DTO.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;



@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder ({
        "maskedBankAccountNumber",
        "refundType",
        "retryUserCreditInitiateTimestamp",
        "issuingBankName",
        "payMethod",
        "userCreditExpectedDate",
        "userMobileNo",
        "cardScheme"
})
public class RetryInfo {

    @JsonProperty ("maskedBankAccountNumber")
    private String maskedBankAccountNumber;
    @JsonProperty("refundType")
    private String refundType;
    @JsonProperty("retryUserCreditInitiateTimestamp")
    private String retryUserCreditInitiateTimestamp;
    @JsonProperty("issuingBankName")
    private String issuingBankName;
    @JsonProperty("payMethod")
    private String payMethod;
    @JsonProperty("userCreditExpectedDate")
    private String userCreditExpectedDate;
    @JsonProperty("userMobileNo")
    private String userMobileNo;
    @JsonProperty("cardScheme")
    private String cardScheme;
    @JsonProperty("maskedCardNumber")
    private String maskedCardNumber;



    @JsonProperty("maskedBankAccountNumber")
    public String getMaskedBankAccountNumber() {
        return maskedBankAccountNumber;
    }

    @JsonProperty("maskedBankAccountNumber")
    public RetryInfo setMaskedBankAccountNumber(String maskedBankAccountNumber) {
        this.maskedBankAccountNumber = maskedBankAccountNumber;
        return this;
    }


    @JsonProperty("refundType")
    public String getRefundType() {
        return refundType;
    }

    @JsonProperty("refundType")
    public RetryInfo setRefundType(String refundType) {
        this.refundType = refundType;
        return this;
    }

    @JsonProperty("cardScheme")
    public String getCardScheme() {
        return cardScheme;
    }

    @JsonProperty("cardScheme")
    public RetryInfo setCardScheme(String cardScheme) {
        this.cardScheme = cardScheme;
        return this;
    }

    @JsonProperty("retryUserCreditInitiateTimestamp")
    public String getRetryUserCreditInitiateTimestamp() {
        return retryUserCreditInitiateTimestamp;
    }

    @JsonProperty("retryUserCreditInitiateTimestamp")
    public RetryInfo setRetryUserCreditInitiateTimestamp(String retryUserCreditInitiateTimestamp) {
        this.retryUserCreditInitiateTimestamp = retryUserCreditInitiateTimestamp;
        return this;
    }


    @JsonProperty("issuingBankName")
    public String getIssuingBankName() {
        return issuingBankName;
    }

    @JsonProperty("issuingBankName")
    public RetryInfo setIssuingBankName(String issuingBankName) {
        this.issuingBankName = issuingBankName;
        return this;
    }



    @JsonProperty("payMethod")
    public String getPayMethod() {
        return payMethod;
    }

    @JsonProperty("payMethod")
    public RetryInfo setPayMethod(String payMethod) {
        this.payMethod = payMethod;
        return this;
    }


    @JsonProperty("userCreditExpectedDate")
    public String getUserCreditExpectedDate() {
        return userCreditExpectedDate;
    }

    @JsonProperty("userCreditExpectedDate")
    public RetryInfo setUserCreditExpectedDate(String userCreditExpectedDate) {
        this.userCreditExpectedDate = userCreditExpectedDate;
        return this;
    }


    @JsonProperty("userMobileNo")
    public String getUserMobileNo() {
        return userMobileNo;
    }

    @JsonProperty("userMobileNo")
    public RetryInfo setUserMobileNo(String userMobileNo) {
        this.userMobileNo = userMobileNo;
        return this;
    }

    @JsonProperty("maskedCardNumber")
    public String getMaskedCardNumber() {
        return maskedCardNumber;
    }

    @JsonProperty("maskedCardNumber")
    public RetryInfo setMaskedCardNumber(String maskedCardNumber) {
        this.maskedCardNumber = maskedCardNumber;
        return this;
    }


}