package com.paytm.dto.HANDLERINTERNALTXNSTATUS;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "extendInfo",
        "signature",
        "ORDERID"
})
public class Body {

    public Body(String oRDERID){
        this.extendInfo = new ExtendInfo();
        this.oRDERID = oRDERID;
        this.signature = "";
    }

    @JsonProperty("extendInfo")
    private ExtendInfo extendInfo;
    @JsonProperty("signature")
    private String signature;
    @JsonProperty("ORDERID")
    private String oRDERID;

    @JsonProperty("extendInfo")
    public ExtendInfo getExtendInfo() {
        return extendInfo;
    }

    @JsonProperty("extendInfo")
    public void setExtendInfo(ExtendInfo extendInfo) {
        this.extendInfo = extendInfo;
    }

    @JsonProperty("signature")
    public String getSignature() {
        return signature;
    }

    @JsonProperty("signature")
    public void setSignature(String signature) {
        this.signature = signature;
    }

    @JsonProperty("ORDERID")
    public String getORDERID() {
        return oRDERID;
    }

    @JsonProperty("ORDERID")
    public void setORDERID(String oRDERID) {
        this.oRDERID = oRDERID;
    }

}