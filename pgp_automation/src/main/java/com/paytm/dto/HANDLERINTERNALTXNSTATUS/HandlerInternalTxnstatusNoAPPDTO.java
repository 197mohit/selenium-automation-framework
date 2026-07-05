package com.paytm.dto.HANDLERINTERNALTXNSTATUS;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "MID",
        "ORDERID"
})
public class HandlerInternalTxnstatusNoAPPDTO {

    @JsonProperty("MID")
    private String mID;
    @JsonProperty("ORDERID")
    private String oRDERID;

    @JsonProperty("MID")
    public String getMID() {
        return mID;
    }

    @JsonProperty("MID")
    public HandlerInternalTxnstatusNoAPPDTO setMID(String mID) {
        this.mID = mID;
        return this;
    }

    @JsonProperty("ORDERID")
    public String getORDERID() {
        return oRDERID;
    }

    @JsonProperty("ORDERID")
    public HandlerInternalTxnstatusNoAPPDTO setORDERID(String oRDERID) {
        this.oRDERID = oRDERID;
        return this;
    }

}