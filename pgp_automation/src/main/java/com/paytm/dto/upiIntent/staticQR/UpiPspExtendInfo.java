package com.paytm.dto.upiIntent.staticQR;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpiPspExtendInfo {

    @JsonProperty("additionalInfo")
    private String additionalInfo;

    public UpiPspExtendInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    @JsonProperty("additionalInfo")
    public String getAdditionalInfo() {
        return additionalInfo;
    }

    @JsonProperty("additionalInfo")
    public UpiPspExtendInfo setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
        return this;
    }
}
