package com.paytm.dto.NativeDTO.FetchQRPaymentDetailsDTO;

import com.fasterxml.jackson.annotation.*;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "mid",
        "qrCodeId",
        "mlvSupported"
})
public class Body implements Serializable
{

    @JsonProperty("mid")
    private String mid;
    @JsonProperty("qrCodeId")
    private String qrCodeId;
    @JsonProperty("orderId")
    private String orderId;
    @JsonProperty("generateOrderId")
    private String generateOrderId;
    @JsonProperty("mlvSupported")
    private Boolean mlvSupported;
    @JsonProperty("supportedPayModesForAddNPay")
    private String supportedPayModesForAddNPay;
    @JsonProperty("isLiteEligible")
    private Boolean isLiteEligible;
    @JsonProperty("merchantVpa")
    private String merchantVpa;
    @JsonProperty("tpap")
    private Boolean tpap;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();
    private final static long serialVersionUID = -4335448592039216470L;

    @JsonProperty("mid")
    public String getMid() {
        return mid;
    }

    @JsonProperty("mid")
    public Body setMid(String mid) {
        this.mid = mid;
        return this;
    }

    @JsonProperty("qrCodeId")
    public String getQrCodeId() {
        return qrCodeId;
    }

    @JsonProperty("qrCodeId")
    public Body setQrCodeId(String qrCodeId) {
        this.qrCodeId = qrCodeId;
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
    @JsonProperty("generateOrderId")
    public String getGenerateOrderId() {
        return generateOrderId;
    }

    @JsonProperty("generateOrderId")
    public Body setGenerateOrderId(String generateorderId) {
        this.generateOrderId = generateorderId;
        return this;
    }
    @JsonProperty("mlvSupported")
    public Boolean getMlvSupported() {
        return mlvSupported;
    }

    @JsonProperty("mlvSupported")
    public Body setMlvSupported(Boolean mlvSupported) {
        this.mlvSupported = mlvSupported;
        return this;
    }


    @JsonProperty("supportedPayModesForAddNPay")
    public  String setsupportedPayModesForAddNPay(){
        return supportedPayModesForAddNPay;
    }

    @JsonProperty("supportedPayModesForAddNPay")
    public Body setsupportedPayModesForAddNPay(String supportedPayModesForAddNPay){
        this.supportedPayModesForAddNPay=supportedPayModesForAddNPay;
        return this;
    }

    @JsonProperty("isLiteEligible")
    public Boolean getIsLiteEligible() {
        return isLiteEligible;
    }

    @JsonProperty("isLiteEligible")
    public Body setIsLiteEligible(Boolean isLiteEligible) {
        this.isLiteEligible = isLiteEligible;
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

    @JsonProperty("merchantVpa")
    public  Body setmerchantVpa(String merchantVpa)
    {
        this.merchantVpa= merchantVpa;
        return this;
    }
    @JsonProperty("tpap")
    public  Body setTpap(Boolean tpap)
    {
        this.tpap= tpap;
        return this;
    }


}