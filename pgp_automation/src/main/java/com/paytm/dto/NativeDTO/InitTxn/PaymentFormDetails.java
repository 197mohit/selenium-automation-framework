package com.paytm.dto.NativeDTO.InitTxn;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.ToStringBuilder;


@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "paymentForm",
        "customerName",
        "mobileNo",
        "emailId",
        "txnAmount",
        "skipLoginEnabled"
})
public class PaymentFormDetails implements Serializable {

    @JsonProperty("paymentForm")
    private List<PaymentForm> paymentForm = null;
    @JsonProperty("customerName")
    private String customerName;
    @JsonProperty("mobileNo")
    private String mobileNo;
    @JsonProperty("emailId")
    private String emailId;
    @JsonProperty("txnAmount")
    private Object txnAmount;
    @JsonProperty("skipLoginEnabled")
    private Boolean skipLoginEnabled;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();
    private final static long serialVersionUID = 3554800944723383984L;

    @JsonProperty("paymentForm")
    public List<PaymentForm> getPaymentForm() {
        return paymentForm;
    }

    @JsonProperty("paymentForm")
    public void setPaymentForm(List<PaymentForm> paymentForm) {
        this.paymentForm = paymentForm;
    }

    @JsonProperty("customerName")
    public String getCustomerName() {
        return customerName;
    }

    @JsonProperty("customerName")
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    @JsonProperty("mobileNo")
    public String getMobileNo() {
        return mobileNo;
    }

    @JsonProperty("mobileNo")
    public void setMobileNo(String mobileNo) {
        this.mobileNo = mobileNo;
    }

    @JsonProperty("emailId")
    public String getEmailId() {
        return emailId;
    }

    @JsonProperty("emailId")
    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    @JsonProperty("txnAmount")
    public Object getTxnAmount() {
        return txnAmount;
    }

    @JsonProperty("txnAmount")
    public void setTxnAmount(Object txnAmount) {
        this.txnAmount = txnAmount;
    }

    @JsonProperty("skipLoginEnabled")
    public Boolean getSkipLoginEnabled() {
        return skipLoginEnabled;
    }

    @JsonProperty("skipLoginEnabled")
    public void setSkipLoginEnabled(Boolean skipLoginEnabled) {
        this.skipLoginEnabled = skipLoginEnabled;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("paymentForm", paymentForm).append("customerName", customerName).append("mobileNo", mobileNo).append("emailId", emailId).append("txnAmount", txnAmount).append("skipLoginEnabled", skipLoginEnabled).append("additionalProperties", additionalProperties).toString();
    }

}