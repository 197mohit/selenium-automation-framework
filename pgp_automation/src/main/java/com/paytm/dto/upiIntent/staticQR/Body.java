package com.paytm.dto.upiIntent.staticQR;

import com.fasterxml.jackson.annotation.*;
import com.paytm.dto.NativeDTO.InitTxn.RiskExtendInfo;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "requestType",
    "iss",
    "custID",
    "mid",
    "payerVpa",
    "txnAmount",
    "payeeVpa",
    "orderId",
    "payerName",
    "payerPSP",
    "creditCardInfo",
    "extendInfo",
    "riskExtendInfo"
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
  @JsonProperty("orderId")
  private String orderId;
  @JsonProperty("payerName")
  private String payerName;
  @JsonProperty("payerPSP")
  private String payerPSP;
  @JsonProperty("type")
  private String type;
  @JsonProperty("payerPaymentInstrument")
  private String payerPaymentInstrument;
  @JsonProperty("payerPaymentInstrumentFee")
  private String payerPaymentInstrumentFee;
  @JsonProperty("creditCardInfo")
  private CreditCardInfo creditCardInfo;
  @JsonProperty("extendInfo")
  private UpiPspExtendInfo extendInfo;
  @JsonProperty("riskExtendInfo")
  private RiskExtendInfo riskExtendInfo;
  @JsonIgnore
  private Map<String, Object> additionalProperties = new HashMap<>();

  @JsonProperty("orderId")
  public String getOrderId() {
    return orderId;
  }

  @JsonProperty("orderId")
  public Body setOrderId(String orderId) {
    this.orderId = orderId;
    return this;
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

  @JsonProperty("payerName")
  public String getPayerName() {
    return payerName;
  }

  @JsonProperty("payerName")
  public Body setPayerName(String payerName) {
    this.payerName = payerName;
    return this;
  }

  @JsonProperty("payerPSP")
  public String getPayerPSP() {
    return payerPSP;
  }

  @JsonProperty("payerPSP")
  public Body setPayerPSP(String payerPSP) {
    this.payerPSP = payerPSP;
    return this;
  }

  @JsonProperty("type")
  public String getTypeField() {
    return type;
  }

  @JsonProperty("type")
  public Body setType(String typeField) {
    this.type = typeField;
    return this;
  }

  @JsonProperty("payerPaymentInstrument")
  public String getPayerPaymentInstrument() {
    return payerPaymentInstrument;
  }

  @JsonProperty("payerPaymentInstrument")
  public Body setPayerPaymentInstrument(String payerPaymentInstrument) {
    this.payerPaymentInstrument = payerPaymentInstrument;
    return this;
  }

  @JsonProperty("payerPaymentInstrumentFee")
  public String getPayerPaymentInstrumentFee() {
    return payerPaymentInstrumentFee;
  }

  @JsonProperty("payerPaymentInstrumentFee")
  public Body setPayerPaymentInstrumentFee(String payerPaymentInstrumentFee) {
    this.payerPaymentInstrumentFee = payerPaymentInstrumentFee;
    return this;
  }

  @JsonProperty("creditCardInfo")
  public CreditCardInfo getCreditCardInfo() {
    return creditCardInfo;
  }

  @JsonProperty("creditCardInfo")
  public Body setCreditCardInfo(CreditCardInfo creditCardInfo) {
    this.creditCardInfo = creditCardInfo;
    return this;
  }

  @JsonProperty("extendInfo")
  public UpiPspExtendInfo getExtendInfo() {
    return extendInfo;
  }

  @JsonProperty("extendInfo")
  public Body setExtendInfo(UpiPspExtendInfo extendInfo) {
    this.extendInfo = extendInfo;
    return this;
  }

  @JsonProperty("riskExtendInfo")
  public RiskExtendInfo getRiskExtendInfo() {
    return riskExtendInfo;
  }

  @JsonProperty("riskExtendInfo")
  public Body setRiskExtendInfo(RiskExtendInfo riskExtendInfo) {
    this.riskExtendInfo = riskExtendInfo;
    return this;
  }

  @JsonAnyGetter
  public Map<String, Object> getAdditionalProperties() {
    return this.additionalProperties;
  }

  @JsonAnySetter
  public Body setAdditionalProperty(String name, Object value) {
    this.additionalProperties.put(name, value);
    return this;
  }

}