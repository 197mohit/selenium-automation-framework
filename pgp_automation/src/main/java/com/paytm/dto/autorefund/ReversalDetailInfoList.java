
package com.paytm.dto.autorefund;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "reversalAmount",
    "issuingBankName",
    "cardScheme",
    "payMethod",
    "userCreditExpectedDate",
    "userMobileNo",
    "maskedCardNumber",
    "rrn",
    "ifscCode"
})
public class ReversalDetailInfoList implements Serializable
{

    @JsonProperty("reversalAmount")
    private String reversalAmount;
    @JsonProperty("issuingBankName")
    private String issuingBankName;
    @JsonProperty("cardScheme")
    private String cardScheme;
    @JsonProperty("payMethod")
    private String payMethod;
    @JsonProperty("userCreditExpectedDate")
    private String userCreditExpectedDate;
    @JsonProperty("maskedVpa")
    private String maskedVpa;
    @JsonProperty("userMobileNo")
    private String userMobileNo;
    @JsonProperty("maskedCardNumber")
    private String maskedCardNumber;
    @JsonProperty("rrn")
    private String rrn;
    @JsonProperty("ifscCode")
    private String ifscCode;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();
    private final static long serialVersionUID = -7064437671841845444L;

    @JsonProperty("reversalAmount")
    public String getReversalAmount() {
        return reversalAmount;
    }

    @JsonProperty("reversalAmount")
    public void setReversalAmount(String reversalAmount) {
        this.reversalAmount = reversalAmount;
    }

    @JsonProperty("issuingBankName")
    public String getIssuingBankName() {
        return issuingBankName;
    }

    @JsonProperty("issuingBankName")
    public void setIssuingBankName(String issuingBankName) {
        this.issuingBankName = issuingBankName;
    }

    @JsonProperty("cardScheme")
    public String getCardScheme() {
        return cardScheme;
    }

    @JsonProperty("cardScheme")
    public void setCardScheme(String cardScheme) {
        this.cardScheme = cardScheme;
    }

    @JsonProperty("payMethod")
    public String getPayMethod() {
        return payMethod;
    }

    @JsonProperty("payMethod")
    public void setPayMethod(String payMethod) {
        this.payMethod = payMethod;
    }

    @JsonProperty("userCreditExpectedDate")
    public String getUserCreditExpectedDate() {
        return userCreditExpectedDate;
    }

    @JsonProperty("userCreditExpectedDate")
    public void setUserCreditExpectedDate(String userCreditExpectedDate) {
        this.userCreditExpectedDate = userCreditExpectedDate;
    }

    @JsonProperty("maskedVpa")
    public String getMaskedVpa() {
        return maskedVpa;
    }

    @JsonProperty("maskedVpa")
    public void setMaskedVpa(String maskedVpa) {
        this.maskedVpa = maskedVpa;
    }

    @JsonProperty("ifscCode")
    public String getIfscCode() {
        return ifscCode;
    }

    @JsonProperty("ifscCode")
    public void setIfscCode(String ifscCode) {
        this.ifscCode = ifscCode;
    }

    @JsonProperty("userMobileNo")
    public String getUserMobileNo() {
        return userMobileNo;
    }

    @JsonProperty("userMobileNo")
    public void setUserMobileNo(String userMobileNo) {
        this.userMobileNo = userMobileNo;
    }

    @JsonProperty("maskedCardNumber")
    public String getMaskedCardNumber() {
        return maskedCardNumber;
    }

    @JsonProperty("maskedCardNumber")
    public void setMaskedCardNumber(String maskedCardNumber) {
        this.maskedCardNumber = maskedCardNumber;
    }

    @JsonProperty("rrn")
    public String getRrn() {
        return rrn;
    }

    @JsonProperty("rrn")
    public void setRrn(String rrn) {
        this.rrn = rrn;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
