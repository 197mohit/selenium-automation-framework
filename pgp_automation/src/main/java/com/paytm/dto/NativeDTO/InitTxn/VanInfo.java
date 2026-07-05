package com.paytm.dto.NativeDTO.InitTxn;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "identificationNo",
        "merchantPrefix",
        "purpose"
})
public class VanInfo {

    @JsonProperty("identificationNo")
    private String identificationNo;
    @JsonProperty("merchantPrefix")
    private String merchantPrefix;
    @JsonProperty("purpose")
    private String purpose;

    @JsonProperty("identificationNo")
    public String getIdentificationNo() {
        return identificationNo;
    }

    @JsonProperty("identificationNo")
    public VanInfo setIdentificationNo(String identificationNo) {
        this.identificationNo = identificationNo;
        return this;
    }

    @JsonProperty("merchantPrefix")
    public String getMerchantPrefix() {
        return merchantPrefix;
    }

    @JsonProperty("merchantPrefix")
    public VanInfo setMerchantPrefix(String merchantPrefix) {
        this.merchantPrefix = merchantPrefix;
        return this;
    }

    @JsonProperty("purpose")
    public String getPurpose() {
        return purpose;
    }

    @JsonProperty("purpose")
    public VanInfo setPurpose(String purpose) {
        this.purpose = purpose;
        return this;
    }

    public VanInfo(String identificationNo, String merchantPrefix, String purpose){
        this.identificationNo = identificationNo;
        this.merchantPrefix = merchantPrefix;
        this.purpose = purpose;
    }

    public VanInfo(){
        this.identificationNo = null;
        this.merchantPrefix = null;
        this.purpose = null;
    }
}