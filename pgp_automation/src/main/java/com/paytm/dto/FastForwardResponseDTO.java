package com.paytm.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class FastForwardResponseDTO {

    private String PaymentMode;
    private String Status;
    private String MerchantId;
    private String OrderId;
    private String ResponseCode;
    private String BankName;
    private String TxnAmount;
    private String TxnId;
    private String CustId;
    private String ResponseMessage;
    private String MercUnqRef;
    private String BankTxnId;
    private String CheckSum;
    private String LoyaltyPoints;


    @JsonProperty("LoyaltyPoints")
    public String getLoyaltyPoints() {
        return LoyaltyPoints;
    }

    public FastForwardResponseDTO setLoyaltyPoints(String loyaltyPoints) {
        this.LoyaltyPoints = loyaltyPoints;
        return this;
    }

    @JsonProperty("CheckSum")
    public String getCheckSum() {
        return CheckSum;
    }

    public FastForwardResponseDTO setCheckSum(String checkSum) {
        CheckSum = checkSum;
        return this;
    }

    @JsonProperty("BankTxnId")
    public String getBankTxnId() {
        return BankTxnId;
    }

    public FastForwardResponseDTO setBankTxnId(String bankTxnId) {
        BankTxnId = bankTxnId;
        return this;
    }


    public String getMercUnqRef() {
        return MercUnqRef;
    }

    @JsonProperty("MercUnqRef")
    public FastForwardResponseDTO setMercUnqRef(String mercUnqRef) {
        MercUnqRef = mercUnqRef;
        return this;
    }


    @JsonProperty("PaymentMode")
    public String getPaymentMode() {
        return PaymentMode;
    }

    @JsonProperty("PaymentMode")
    public void setPaymentMode(String PaymentMode) {
        this.PaymentMode = PaymentMode;
    }

    @JsonProperty("Status")
    public String getStatus() {
        return Status;
    }

    @JsonProperty("Status")
    public void setStatus(String Status) {
        this.Status = Status;
    }

    @JsonProperty("MerchantId")
    public String getMerchantId() {
        return MerchantId;
    }

    @JsonProperty("MerchantId")
    public void setMerchantId(String MerchantId) {
        this.MerchantId = MerchantId;
    }

    @JsonProperty("OrderId")
    public String getOrderId() {
        return OrderId;
    }

    @JsonProperty("OrderId")
    public void setOrderId(String OrderId) {
        this.OrderId = OrderId;
    }

    @JsonProperty("ResponseCode")
    public String getResponseCode() {
        return ResponseCode;
    }

    @JsonProperty("ResponseCode")
    public void setResponseCode(String ResponseCode) {
        this.ResponseCode = ResponseCode;
    }

    @JsonProperty("BankName")
    public String getBankName() {
        return BankName;
    }

    @JsonProperty("BankName")
    public void setBankName(String BankName) {
        this.BankName = BankName;
    }

    @JsonProperty("TxnAmount")
    public String getTxnAmount() {
        return TxnAmount;
    }

    @JsonProperty("TxnAmount")
    public void setTxnAmount(String TxnAmount) {
        this.TxnAmount = TxnAmount;
    }

    @JsonProperty("TxnId")
    public String getTxnId() {
        return TxnId;
    }

    @JsonProperty("TxnId")
    public void setTxnId(String TxnId) {
        this.TxnId = TxnId;
    }

    @JsonProperty("CustId")
    public String getCustId() {
        return CustId;
    }

    @JsonProperty("CustId")
    public void setCustId(String CustId) {
        this.CustId = CustId;
    }

    @JsonProperty("ResponseMessage")
    public String getResponseMessage() {
        return ResponseMessage;
    }

    @JsonProperty("ResponseMessage")
    public void setResponseMessage(String ResponseMessage) {
        this.ResponseMessage = ResponseMessage;
    }

    @Override
    public String toString() {
        return "ClassPojo [PaymentMode = " + PaymentMode + ", Status = " + Status + ", MerchantId = " + MerchantId + ", OrderId = " + OrderId + ", ResponseCode = " + ResponseCode + ", BankName = " + BankName + ", TxnAmount = " + TxnAmount + ", TxnId = " + TxnId + ", CustId = " + CustId + ", ResponseMessage = " + ResponseMessage + "]";
    }
}



