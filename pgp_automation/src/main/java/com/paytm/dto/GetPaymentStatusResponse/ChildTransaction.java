package com.paytm.dto.GetPaymentStatusResponse;
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
        "txnId",
        "paymentMode",
        "txnAmount",
        "gateway",
        "bankTxnId",
        "bankName",
        "status",
        "cardScheme"
})
public class ChildTransaction {

    @JsonProperty("txnId")
    private String txnId;
    @JsonProperty("paymentMode")
    private String paymentMode;
    @JsonProperty("txnAmount")
    private String txnAmount;
    @JsonProperty("gateway")
    private String gateway;
    @JsonProperty("bankTxnId")
    private String bankTxnId;
    @JsonProperty("bankName")
    private String bankName;
    @JsonProperty("status")
    private String status;
    @JsonProperty("cardScheme")
    private String cardScheme;
    @JsonProperty("cardScheme")
    public String getCardScheme() {
        return cardScheme;
    }

    @JsonProperty("cardScheme")
    public void setCardScheme(String cardScheme) {
        this.cardScheme = cardScheme;
    }

    @JsonProperty("txnId")
    public String getTxnId() {
        return txnId;
    }

    @JsonProperty("txnId")
    public void setTxnId(String txnId) {
        this.txnId = txnId;
    }

    @JsonProperty("paymentMode")
    public String getPaymentMode() {
        return paymentMode;
    }

    @JsonProperty("paymentMode")
    public void setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
    }

    @JsonProperty("txnAmount")
    public String getTxnAmount() {
        return txnAmount;
    }

    @JsonProperty("txnAmount")
    public void setTxnAmount(String txnAmount) {
        this.txnAmount = txnAmount;
    }

    @JsonProperty("gateway")
    public String getGateway() {
        return gateway;
    }

    @JsonProperty("gateway")
    public void setGateway(String gateway) {
        this.gateway = gateway;
    }

    @JsonProperty("bankTxnId")
    public String getBankTxnId() {
        return bankTxnId;
    }

    @JsonProperty("bankTxnId")
    public void setBankTxnId(String bankTxnId) {
        this.bankTxnId = bankTxnId;
    }

    @JsonProperty("bankName")
    public String getBankName() {
        return bankName;
    }

    @JsonProperty("bankName")
    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    @JsonProperty("status")
    public String getStatus() {
        return status;
    }

    @JsonProperty("status")
    public void setStatus(String status) {
        this.status = status;
    }
}