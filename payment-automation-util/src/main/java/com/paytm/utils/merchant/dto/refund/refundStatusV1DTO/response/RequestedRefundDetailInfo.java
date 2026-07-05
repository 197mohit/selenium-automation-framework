package com.paytm.utils.merchant.dto.refund.refundStatusV1DTO.response;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "maskedBankAccountNumber",
        "refundType",
        "ifscCode"
})
public class RequestedRefundDetailInfo {

    @JsonProperty("maskedBankAccountNumber")
    private String maskedBankAccountNumber;
    @JsonProperty("refundType")
    private String refundType;
    @JsonProperty("ifscCode")
    private String ifscCode;


       @JsonProperty("maskedBankAccountNumber")
    public String getMaskedBankAccountNumber() {
        return maskedBankAccountNumber;
    }

    @JsonProperty("maskedBankAccountNumber")
    public RequestedRefundDetailInfo setMaskedBankAccountNumber(String maskedBankAccountNumber) {
        this.maskedBankAccountNumber = maskedBankAccountNumber;
        return this;
    }

    @JsonProperty("refundType")
    public String getRefundType() {
        return refundType;
    }

    @JsonProperty("refundType")
    public RequestedRefundDetailInfo setRefundType(String refundType) {
        this.refundType = refundType;
        return  this;
    }


    @JsonProperty("ifscCode")
    public String getIfscCode() {
        return ifscCode;
    }

    @JsonProperty("ifscCode")
    public RequestedRefundDetailInfo setIfscCode(String ifscCode) {
        this.ifscCode = ifscCode;
        return this;
    }



}