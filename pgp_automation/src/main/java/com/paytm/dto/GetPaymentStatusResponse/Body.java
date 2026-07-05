package com.paytm.dto.GetPaymentStatusResponse;
import java.util.List;

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
        "resultInfo",
        "txnId",
        "bankTxnId",
        "orderId",
        "txnAmount",
        "txnType",
        "gatewayName",
        "bankName",
        "mid",
        "paymentMode",
        "refundAmt",
        "txnDate",
        "childTransaction",
        "maskedCardNo",
        "cardScheme",
        "bin",
        "lastFourDigit",
        "chargeAmount",
        "feeRateFactors",
        "maskedEcomToken",
        "cardScheme",
        "rrnCode",
        "authCode"

})
public class Body {

    @JsonProperty("resultInfo")
    private ResultInfo resultInfo;
    @JsonProperty("txnId")
    private String txnId;
    @JsonProperty("bankTxnId")
    private String bankTxnId;
    @JsonProperty("orderId")
    private String orderId;
    @JsonProperty("txnAmount")
    private String txnAmount;
    @JsonProperty("txnType")
    private String txnType;
    @JsonProperty("gatewayName")
    private String gatewayName;
    @JsonProperty("bankName")
    private String bankName;
    @JsonProperty("mid")
    private String mid;
    @JsonProperty("paymentMode")
    private String paymentMode;
    @JsonProperty("refundAmt")
    private String refundAmt;
    @JsonProperty("txnDate")
    private String txnDate;
    @JsonProperty("childTransaction")
    private List<ChildTransaction> childTransaction = null;
    @JsonProperty("maskedCardNo")
    private String maskedCardNo;
    @JsonProperty("cardScheme")
    private String cardScheme;
    @JsonProperty("bin")
    private String bin;
    @JsonProperty("lastFourDigit")
    private String lastFourDigit;
    @JsonProperty("chargeAmount")
    private String chargeAmount;
    @JsonProperty("feeRateFactors")
    private FeeRateFactors feeRateFactors;
    @JsonProperty("maskedEcomToken")
    private String maskedEcomToken;
    @JsonProperty("rrnCode")
    private String rrnCode;
    @JsonProperty("authCode")
    private String authCode;

    public String getBin() {
        return bin;
    }

    public void setBin(String bin) {
        this.bin = bin;
    }

    public String getLastFourDigit() {
        return lastFourDigit;
    }

    public void setLastFourDigit(String lastFourDigit) {
        this.lastFourDigit = lastFourDigit;
    }

    public FeeRateFactors getFeeRateFactors() {
        return feeRateFactors;
    }

    public void setFeeRateFactors(FeeRateFactors feeRateFactors) {
        this.feeRateFactors = feeRateFactors;
    }

    public String getMaskedEcomToken() {
        return maskedEcomToken;
    }

    public void setMaskedEcomToken(String maskedEcomToken) {
        this.maskedEcomToken = maskedEcomToken;
    }

    public String getMaskedCardNo() {
        return maskedCardNo;
    }

    public void setMaskedCardNo(String maskedCardNo) {
        this.maskedCardNo = maskedCardNo;
    }

    public String getRrnCode(){return rrnCode;}

    @JsonProperty("rrnCode")
    public void setRrnCode(String rrnCode){this.rrnCode = rrnCode;}

    public String getAuthCode(){return authCode;}

    @JsonProperty("authCode")
    public void setAuthCode(String authCode){this.authCode = authCode;}

    @JsonProperty("cardScheme")
    public String getCardScheme() {
        return cardScheme;
    }

    @JsonProperty("cardScheme")
    public void setCardScheme(String cardScheme) {
        this.cardScheme = cardScheme;
    }

    @JsonProperty("resultInfo")
    public ResultInfo getResultInfo() {
        return resultInfo;
    }

    @JsonProperty("resultInfo")
    public void setResultInfo(ResultInfo resultInfo) {
        this.resultInfo = resultInfo;
    }

    @JsonProperty("txnId")
    public String getTxnId() {
        return txnId;
    }

    @JsonProperty("txnId")
    public void setTxnId(String txnId) {
        this.txnId = txnId;
    }

    @JsonProperty("bankTxnId")
    public String getBankTxnId() {
        return bankTxnId;
    }

    @JsonProperty("bankTxnId")
    public void setBankTxnId(String bankTxnId) {
        this.bankTxnId = bankTxnId;
    }

    @JsonProperty("orderId")
    public String getOrderId() {
        return orderId;
    }

    @JsonProperty("orderId")
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    @JsonProperty("txnAmount")
    public String getTxnAmount() {
        return txnAmount;
    }

    @JsonProperty("txnAmount")
    public void setTxnAmount(String txnAmount) {
        this.txnAmount = txnAmount;
    }

    @JsonProperty("txnType")
    public String getTxnType() {
        return txnType;
    }

    @JsonProperty("txnType")
    public void setTxnType(String txnType) {
        this.txnType = txnType;
    }

    @JsonProperty("gatewayName")
    public String getGatewayName() {
        return gatewayName;
    }

    @JsonProperty("gatewayName")
    public void setGatewayName(String gatewayName) {
        this.gatewayName = gatewayName;
    }

    @JsonProperty("bankName")
    public String getBankName() {
        return bankName;
    }

    @JsonProperty("bankName")
    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    @JsonProperty("mid")
    public String getMid() {
        return mid;
    }

    @JsonProperty("mid")
    public void setMid(String mid) {
        this.mid = mid;
    }

    @JsonProperty("paymentMode")
    public String getPaymentMode() {
        return paymentMode;
    }

    @JsonProperty("paymentMode")
    public void setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
    }

    @JsonProperty("refundAmt")
    public String getRefundAmt() {
        return refundAmt;
    }

    @JsonProperty("refundAmt")
    public void setRefundAmt(String refundAmt) {
        this.refundAmt = refundAmt;
    }

    @JsonProperty("txnDate")
    public String getTxnDate() {
        return txnDate;
    }

    @JsonProperty("txnDate")
    public void setTxnDate(String txnDate) {
        this.txnDate = txnDate;
    }

    @JsonProperty("childTransaction")
    public List<ChildTransaction> getChildTransaction() {
        return childTransaction;
    }

    @JsonProperty("childTransaction")
    public void setChildTransaction(List<ChildTransaction> childTransaction) {
        this.childTransaction = childTransaction;
    }

    @JsonProperty("chargeAmount")
    public String getChargeAmount(){return chargeAmount;}

    @JsonProperty("chargeAmount")
    public void setChargeAmount(String chargeAmount){this.chargeAmount = chargeAmount;}
}