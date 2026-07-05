package com.paytm.dto.NativeDTO.InitTxn;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "ultimateBeneficiaryName"
})
public class UltimateBeneficiaryDetails {

    @JsonProperty("ultimateBeneficiaryName")
    private String ultimateBeneficiaryName;

    @JsonProperty("ultimateBeneficiaryName")
    public String getUltimateBeneficiaryName() {
        return ultimateBeneficiaryName;
    }

    @JsonProperty("ultimateBeneficiaryName")
    public void setUltimateBeneficiaryName(String ultimateBeneficiaryName) {
        this.ultimateBeneficiaryName = ultimateBeneficiaryName;
    }

    public UltimateBeneficiaryDetails(String ultimateBeneficiaryName) {
        this.ultimateBeneficiaryName = ultimateBeneficiaryName;
    }

    public UltimateBeneficiaryDetails() {
        this.ultimateBeneficiaryName = "ultimateBeneficiaryName";
    }



}