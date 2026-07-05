package com.paytm.dto.upiIntent.staticQR.Response;

import com.fasterxml.jackson.annotation.*;
import com.paytm.dto.UPIIntentRequestDTO;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "resultCode",
        "resultCodeId",
        "resultMsg",
        "externalSerialNo",
        "orderId",
        "requestMsgId",
        "txnAmount",
        "mid",
        "txnAllowed",
        "subResultCodeId",
        "payeeMobileNumber",
        "partnerReferenceNumber"
})
public class Body {
    @JsonProperty("resultCode")
    private String resultCode;
    @JsonProperty("resultCodeId")
    private String resultCodeId;
    @JsonProperty("resultMsg")
    private String resultMsg;
    @JsonProperty("externalSerialNo")
    private String externalSerialNo;
    @JsonProperty("orderId")
    private String orderId;
    @JsonProperty("requestMsgId")
    private String requestMsgId;
    @JsonProperty("txnAmount")
    private String txnAmount;
    @JsonProperty("mid")
    private String mid;
    @JsonProperty("callbackUrl")
    private String callbackUrl;
    @JsonProperty("txnAllowed")
    private Boolean txnAllowed;
    @JsonProperty("subResultCodeId")
    private String subResultCodeId;
    @JsonProperty("payeeMobileNumber")
    private String payeeMobileNumber;
    @JsonProperty("partnerReferenceNumber")
    private String partnerReferenceNumber;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    @JsonProperty("resultCode")
    public String getResultCode() {
        return resultCode;
    }

    @JsonProperty("resultCode")
    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    @JsonProperty("resultCodeId")
    public String getResultCodeId() {
        return resultCodeId;
    }

    @JsonProperty("resultCodeId")
    public void setResultCodeId(String resultCodeId) {
        this.resultCodeId = resultCodeId;
    }

    @JsonProperty("resultMsg")
    public String getResultMsg() {
        return resultMsg;
    }

    @JsonProperty("resultMsg")
    public void setResultMsg(String resultMsg) {
        this.resultMsg = resultMsg;
    }

    @JsonProperty("externalSerialNo")
    public String getExternalSerialNo() {
        return externalSerialNo;
    }

    @JsonProperty("externalSerialNo")
    public void setExternalSerialNo(String externalSerialNo) {
        this.externalSerialNo = externalSerialNo;
    }

    @JsonProperty("orderId")
    public String getOrderId() {
        return orderId;
    }

    @JsonProperty("orderId")
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    @JsonProperty("requestMsgId")
    public String getRequestMsgId() {
        return requestMsgId;
    }

    @JsonProperty("requestMsgId")
    public void setRequestMsgId(String requestMsgId) {
        this.requestMsgId = requestMsgId;
    }

    @JsonProperty("txnAmount")
    public String getTxnAmount() {
        return txnAmount;
    }

    @JsonProperty("txnAmount")
    public void setTxnAmount(String txnAmount) {
        this.txnAmount = txnAmount;
    }

    @JsonProperty("mid")
    public String getMid() {
        return mid;
    }

    @JsonProperty("mid")
    public void setMid(String mid) {
        this.mid = mid;
    }

    @JsonProperty("callbackUrl")
    public String getCallbackUrl() {
        return callbackUrl;
    }

    @JsonProperty("callbackUrl")
    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    @JsonProperty("txnAllowed")
    public Boolean getTxnAllowed() {
        return txnAllowed;
    }

    @JsonProperty("mid")
    public void setTxnAllowed(Boolean txnAllowed) {
        this.txnAllowed = txnAllowed;
    }

    @JsonProperty("subResultCodeId")
    public String getSubResultCodeId() {
        return subResultCodeId;
    }

    @JsonProperty("subResultCodeId")
    public void setSubResultCodeId(String subResultCodeId) {
        this.subResultCodeId = subResultCodeId;
    }

    @JsonProperty("payeeMobileNumber")
    public String getPayeeMobileNumber() {
        return payeeMobileNumber;
    }

    @JsonProperty("payeeMobileNumber")
    public void setPayeeMobileNumber(String payeeMobileNumber) {
        this.payeeMobileNumber = payeeMobileNumber;
    }

    @JsonProperty("partnerReferenceNumber")
    public String getPartnerReferenceNumber() {
        return partnerReferenceNumber;
    }

    @JsonProperty("partnerReferenceNumber")
    public void setPartnerReferenceNumber(String partnerReferenceNumber) {
        this.partnerReferenceNumber = partnerReferenceNumber;
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
