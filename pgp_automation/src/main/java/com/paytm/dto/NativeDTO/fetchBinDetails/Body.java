package com.paytm.dto.NativeDTO.fetchBinDetails;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
/**
 * Created by anjukumari on 16/10/18
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "bin", "paymentMode" , "mid", "isEMIDetail", "emiType", "requestType"
})
public class Body {

    @JsonProperty("bin")
    private String bin;
    private String paymentMode;
    @JsonProperty("mid")
    private String mid;
    @JsonProperty("isEMIDetail")
    private String isEMIDetail;

    @JsonProperty("emiType")
    private String emiType;

    @JsonProperty("requestType")
    private String requestType;

    @JsonProperty("bin")
    public String getBin() {
        return bin;
    }

    @JsonProperty("bin")
    public void setBin(String bin) {
        this.bin = bin;
    }

    public Body(String bin, String paymentMode) {
        this.bin = bin;
        this.paymentMode = paymentMode;
    }

    @JsonProperty("paymentMode")
    public String getPaymentMode() {
        return paymentMode;
    }

    @JsonProperty("paymentMode")
    public Body setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
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

    @JsonProperty("isEMIDetail")
    public String getIsEMIDetail(){ return isEMIDetail;}

    @JsonProperty("isEMIDetail")
    public Body setIsEMIDetail(String isEMIDetail) {
        this.isEMIDetail = isEMIDetail;
        return this;
    }

    public Body(String bin) {
        this.bin = bin;
    }

    @JsonProperty("emiType")
    public String getEmiType(){ return emiType;}

    @JsonProperty("emiType")
    public Body setEmiType(String emiType) {
        this.emiType = emiType;
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

}