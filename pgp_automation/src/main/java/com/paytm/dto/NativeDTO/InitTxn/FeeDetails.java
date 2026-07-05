package com.paytm.dto.NativeDTO.InitTxn;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FeeDetails {
    @JsonProperty("convenienceFees")
    private String convenienceFees;
    @JsonProperty("paymodeBasedConvFees")
    private String paymodeBasedConvFees;
    @JsonProperty("convenienceFeeTax")
    private String convenienceFeeTax;
    @JsonProperty("platformFees")
    private String platformFees;

    @JsonProperty("convenienceFees")
    public String getConvenienceFees() {
        return convenienceFees;
    }

    @JsonProperty("convenienceFees")
    public void setConvenienceFees(String convenienceFees) {

        this.convenienceFees = convenienceFees;
    }

    @JsonProperty("paymodeBasedConvFees")
    public String getPaymodeBasedConvFees() {
        return paymodeBasedConvFees;
    }

    @JsonProperty("paymodeBasedConvFees")
    public void setPaymodeBasedConvFees(String paymodeBasedConvFees) {

        this.paymodeBasedConvFees = paymodeBasedConvFees;
    }

    @JsonProperty("convenienceFeeTax")
    public String getConvenienceFeeTax() {
        return convenienceFeeTax;
    }

    @JsonProperty("convenienceFeeTax")
    public void setConvenienceFeeTax(String convenienceFeeTax) {

        this.convenienceFeeTax = convenienceFeeTax;
    }

    @JsonProperty("platformFees")
    public String getPlatformFees() {
        return platformFees;
    }

    @JsonProperty("platformFees")
    public void setPlatformFees(String platformFees) {

        this.platformFees = platformFees;
    }
}
