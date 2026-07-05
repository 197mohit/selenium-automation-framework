package com.paytm.dto.orderPayUnipsp;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.paytm.dto.OrderDTO;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "requestType",
        "iss",
        "custID",
        "mid",
        "payerVpa",
        "txnAmount",
        "payeeVpa"
})
public class Body {

    @JsonProperty("requestType")
    private String requestType;
    @JsonProperty("iss")
    private String iss;
    @JsonProperty("custID")
    private String custID;
    @JsonProperty("mid")
    private String mid;
    @JsonProperty("payerVpa")
    private String payerVpa;
    @JsonProperty("txnAmount")
    private String txnAmount;
    @JsonProperty("payeeVpa")
    private String payeeVpa;

    public Body(String mid, OrderDTO orderDTO){
        this.requestType = "SEAMLESS_3D_FORM";
        this.iss = "ts";
        this.custID = "1107236247";
        this.mid = mid;
        this.txnAmount = orderDTO.getTXN_AMOUNT();
        this.payerVpa = orderDTO.getPayerAccount();
        this.payeeVpa = orderDTO.getTxnId() + "@paytm";
    }

    @JsonProperty("requestType")
    public String getRequestType() {
        return requestType;
    }

    @JsonProperty("requestType")
    public Body setRequestType(String requestType) {
        this.requestType = requestType;
        return this;
    }

    @JsonProperty("iss")
    public String getIss() {
        return iss;
    }

    @JsonProperty("iss")
    public Body setIss(String iss) {
        this.iss = iss;
        return this;
    }

    @JsonProperty("custID")
    public String getCustID() {
        return custID;
    }

    @JsonProperty("custID")
    public Body setCustID(String custID) {
        this.custID = custID;
        return this;
    }

    @JsonProperty("mid")
    public String getMid() {
        return mid;
    }

    @JsonProperty("mid")
    public Body setMid(String mid) {
        this.mid = mid;
        return this;
    }

    @JsonProperty("payerVpa")
    public String getPayerVpa() {
        return payerVpa;
    }

    @JsonProperty("payerVpa")
    public Body setPayerVpa(String payerVpa) {
        this.payerVpa = payerVpa;
        return this;
    }

    @JsonProperty("txnAmount")
    public String getTxnAmount() {
        return txnAmount;
    }

    @JsonProperty("txnAmount")
    public Body setTxnAmount(String txnAmount) {
        this.txnAmount = txnAmount;
        return this;
    }

    @JsonProperty("payeeVpa")
    public String getPayeeVpa() {
        return payeeVpa;
    }

    @JsonProperty("payeeVpa")
    public Body setPayeeVpa(String payeeVpa) {
        this.payeeVpa = payeeVpa;
        return this;
    }

}