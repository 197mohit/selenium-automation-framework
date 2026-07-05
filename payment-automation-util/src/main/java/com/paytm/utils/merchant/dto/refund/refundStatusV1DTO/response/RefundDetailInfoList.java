package com.paytm.utils.merchant.dto.refund.refundStatusV1DTO.response;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder ({
        "refundType",
        "maskedBankAccountNumber",
        "retryInfo",
        "retryFlag",
        "payMethod",
        "userMobileNo",
        "refundAmount",
        "issuingBankName",
        "userCreditExpectedDate",
        "cardScheme",
        "maskedCardNumber"



})
public class RefundDetailInfoList {

    @JsonProperty ("refundType")
    private String refundType;
    @JsonProperty("maskedBankAccountNumber")
    private String maskedBankAccountNumber;
    @JsonProperty ("userCreditExpectedDate")
    private String userCreditExpectedDate;
    @JsonProperty("retryInfo")
    private RetryInfo retryInfo;
    @JsonProperty("retryFlag")
    private String retryFlag;
    @JsonProperty("payMethod")
    private String payMethod;
    @JsonProperty("userMobileNo")
    private String userMobileNo;
    @JsonProperty("refundAmount")
    private String refundAmount;
    @JsonProperty("issuingBankName")
    private String issuingBankName;
    @JsonProperty("cardScheme")
    private String cardScheme;
    @JsonProperty("maskedCardNumber")
    private String maskedCardNumber;
    @JsonProperty("rrn")
    private String rrn;
    @JsonProperty("maskedVpa")
    private String maskedVpa;


    @JsonProperty("cardScheme")
    public String getCardScheme() {
        return cardScheme;
    }
    @JsonProperty("cardScheme")
    public void setCardScheme(String cardScheme) {
        this.cardScheme = cardScheme;
    }

    @JsonProperty("maskedCardNumber")
    public String getMaskedCardNumber() {
        return maskedCardNumber;
    }
    @JsonProperty("maskedCardNumber")
    public void setMaskedCardNumber(String maskedCardNumber) {
        this.maskedCardNumber = maskedCardNumber;
    }

    @JsonProperty("refundType")
    public String getRefundType() {
        return refundType;
    }


    @JsonProperty("refundType")
    public RefundDetailInfoList setRefundType(String refundType) {
        this.refundType = refundType;
        return this;
    }

    @JsonProperty("maskedBankAccountNumber")
    public String getMaskedBankAccountNumber() {
        return maskedBankAccountNumber;
    }

    @JsonProperty("maskedBankAccountNumber")
    public RefundDetailInfoList setMaskedBankAccountNumber(String maskedBankAccountNumber) {
        this.maskedBankAccountNumber = maskedBankAccountNumber;
        return this;
    }


    @JsonProperty("issuingBankName")
    public String getIssuingBankName() {
        return issuingBankName;
    }

    @JsonProperty("issuingBankName")
    public RefundDetailInfoList setIssuingBankName(String issuingBankName) {
        this.issuingBankName = issuingBankName;
        return this;
    }


    @JsonProperty("userCreditExpectedDate")
    public String getUserCreditExpectedDate() {
        return userCreditExpectedDate;
    }

    @JsonProperty("userCreditExpectedDate")
    public  RefundDetailInfoList setUserCreditExpectedDate(String userCreditExpectedDate) {
        this.userCreditExpectedDate = userCreditExpectedDate;
        return this;
    }


    @JsonProperty("retryInfo")
    public RetryInfo getRetryInfo() {
        return retryInfo;
    }

    @JsonProperty("retryInfo")
    public RefundDetailInfoList setRetryInfo(RetryInfo retryInfo) {
        this.retryInfo = retryInfo;
        return this;
    }

    @JsonProperty("retryFlag")
    public String getRetryFlag() {
        return retryFlag;
    }

    @JsonProperty("retryFlag")
    public RefundDetailInfoList setRetryFlag(String retryFlag) {
        this.retryFlag = retryFlag;
        return this;
    }


    @JsonProperty("payMethod")
    public String getPayMethod() {
        return payMethod;
    }

    @JsonProperty("payMethod")
    public RefundDetailInfoList setPayMethod(String payMethod) {
        this.payMethod = payMethod;
        return this;
    }



    @JsonProperty("userMobileNo")
    public String getUserMobileNo() {
        return userMobileNo;
    }

    @JsonProperty("userMobileNo")
    public RefundDetailInfoList setUserMobileNo(String userMobileNo) {
        this.userMobileNo = userMobileNo;
        return this;
    }


    @JsonProperty("refundAmount")
    public String getRefundAmount() {
        return refundAmount;
    }

    @JsonProperty("refundAmount")
    public RefundDetailInfoList setRefundAmount(String refundAmount) {
        this.refundAmount = refundAmount;
        return this;
    }

    @JsonProperty("rrn")
    public String getRrn() {
        return rrn;
    }

    @JsonProperty("rrn")
    public RefundDetailInfoList setRrn(String rrn) {
        this.rrn = rrn;
        return this;
    }

    @JsonProperty("maskedVpa")
    public String getMaskedVpa() {
        return maskedVpa;
    }
    @JsonProperty("maskedVpa")
    public void setMaskedVpa(String maskedVpa) {
        this.maskedVpa = maskedVpa;
    }


}