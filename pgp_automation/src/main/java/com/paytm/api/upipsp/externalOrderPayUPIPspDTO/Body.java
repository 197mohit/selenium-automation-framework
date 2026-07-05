package com.paytm.api.upipsp.externalOrderPayUPIPspDTO;

import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "bankCode",
        "posId",
        "txnStatus",
        "txnDate",
        "orderId",
        "payeeVpa",
        "mid",
        "bankTxnId",
        "refId",
        "payerVpa",
        "txnAmount"
})
@Generated("jsonschema2pojo")
public class Body {

    @JsonProperty("bankCode")
    private String bankCode;
    @JsonProperty("posId")
    private String posId;
    @JsonProperty("txnStatus")
    private String txnStatus;
    @JsonProperty("txnDate")
    private String txnDate;
    @JsonProperty("orderId")
    private String orderId;
    @JsonProperty("payeeVpa")
    private String payeeVpa;
    @JsonProperty("mid")
    private String mid;
    @JsonProperty("bankTxnId")
    private String bankTxnId;
    @JsonProperty("refId")
    private String refId;
    @JsonProperty("payerVpa")
    private String payerVpa;
    @JsonProperty("txnAmount")
    private String txnAmount;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

    @JsonProperty("bankCode")
    public String getBankCode() {
        return bankCode;
    }

    @JsonProperty("bankCode")
    public Body setBankCode(String bankCode) {
        this.bankCode = bankCode;
        return this;
    }

    @JsonProperty("posId")
    public String getPosId() {
        return posId;
    }

    @JsonProperty("posId")
    public Body setPosId(String posId) {
        this.posId = posId;
        return this;
    }

    @JsonProperty("txnStatus")
    public String getTxnStatus() {
        return txnStatus;
    }

    @JsonProperty("txnStatus")
    public Body setTxnStatus(String txnStatus) {
        this.txnStatus = txnStatus;
        return this;
    }

    @JsonProperty("txnDate")
    public String getTxnDate() {
        return txnDate;
    }

    @JsonProperty("txnDate")
    public Body setTxnDate(String txnDate) {
        this.txnDate = txnDate;
        return this;
    }

    @JsonProperty("orderId")
    public String getOrderId() {
        return orderId;
    }

    @JsonProperty("orderId")
    public Body setOrderId(String orderId) {
        this.orderId = orderId;
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

    @JsonProperty("mid")
    public String getMid() {
        return mid;
    }

    @JsonProperty("mid")
    public Body setMid(String mid) {
        this.mid = mid;
        return this;
    }

    @JsonProperty("bankTxnId")
    public String getBankTxnId() {
        return bankTxnId;
    }

    @JsonProperty("bankTxnId")
    public Body setBankTxnId(String bankTxnId) {
        this.bankTxnId = bankTxnId;
        return this;
    }

    @JsonProperty("refId")
    public String getRefId() {
        return refId;
    }

    @JsonProperty("refId")
    public Body setRefId(String refId) {
        this.refId = refId;
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

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
