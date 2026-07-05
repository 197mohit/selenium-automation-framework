package com.paytm.dto.processTransactionV1.response;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "ADDITIONAL_PARAM",
        "BANKNAME",
        "BANKTXNID",
        "CHECKSUMHASH",
        "CURRENCY",
        "GATEWAYNAME",
        "MID",
        "ORDERID",
        "PAYMENTMODE",
        "RESPCODE",
        "RESPMSG",
        "STATUS",
        "TXNAMOUNT",
        "TXNDATE",
        "TXNID",
        "PRN"
})
public class TxnInfo {

    @JsonProperty("ADDITIONAL_PARAM")
    private String aDDITIONALPARAM;
    @JsonProperty("BANKNAME")
    private String bANKNAME;
    @JsonProperty("BANKTXNID")
    private String bANKTXNID;
    @JsonProperty("CHECKSUMHASH")
    private String cHECKSUMHASH;
    @JsonProperty("CURRENCY")
    private String cURRENCY;
    @JsonProperty("GATEWAYNAME")
    private String gATEWAYNAME;
    @JsonProperty("MID")
    private String mID;
    @JsonProperty("ORDERID")
    private String oRDERID;
    @JsonProperty("PAYMENTMODE")
    private String pAYMENTMODE;
    @JsonProperty("RESPCODE")
    private String rESPCODE;
    @JsonProperty("RESPMSG")
    private String rESPMSG;
    @JsonProperty("STATUS")
    private String sTATUS;
    @JsonProperty("TXNAMOUNT")
    private String tXNAMOUNT;
    @JsonProperty("TXNDATE")
    private String tXNDATE;
    @JsonProperty("TXNID")
    private String tXNID;
    @JsonProperty("PRN")
    private String pRN;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    @JsonProperty("PRN")
    public String getpRN() {
        return pRN;
    }

    @JsonProperty("PRN")
    public void setpRN(String pRN) {
        this.pRN = pRN;
    }

    @JsonProperty("ADDITIONAL_PARAM")
    public String getADDITIONALPARAM() {
        return aDDITIONALPARAM;
    }

    @JsonProperty("ADDITIONAL_PARAM")
    public void setADDITIONALPARAM(String aDDITIONALPARAM) {
        this.aDDITIONALPARAM = aDDITIONALPARAM;
    }

    @JsonProperty("BANKNAME")
    public String getBANKNAME() {
        return bANKNAME;
    }

    @JsonProperty("BANKNAME")
    public void setBANKNAME(String bANKNAME) {
        this.bANKNAME = bANKNAME;
    }

    @JsonProperty("BANKTXNID")
    public String getBANKTXNID() {
        return bANKTXNID;
    }

    @JsonProperty("BANKTXNID")
    public void setBANKTXNID(String bANKTXNID) {
        this.bANKTXNID = bANKTXNID;
    }

    @JsonProperty("CHECKSUMHASH")
    public String getCHECKSUMHASH() {
        return cHECKSUMHASH;
    }

    @JsonProperty("CHECKSUMHASH")
    public void setCHECKSUMHASH(String cHECKSUMHASH) {
        this.cHECKSUMHASH = cHECKSUMHASH;
    }

    @JsonProperty("CURRENCY")
    public String getCURRENCY() {
        return cURRENCY;
    }

    @JsonProperty("CURRENCY")
    public void setCURRENCY(String cURRENCY) {
        this.cURRENCY = cURRENCY;
    }

    @JsonProperty("GATEWAYNAME")
    public String getGATEWAYNAME() {
        return gATEWAYNAME;
    }

    @JsonProperty("GATEWAYNAME")
    public void setGATEWAYNAME(String gATEWAYNAME) {
        this.gATEWAYNAME = gATEWAYNAME;
    }

    @JsonProperty("MID")
    public String getMID() {
        return mID;
    }

    @JsonProperty("MID")
    public void setMID(String mID) {
        this.mID = mID;
    }

    @JsonProperty("ORDERID")
    public String getORDERID() {
        return oRDERID;
    }

    @JsonProperty("ORDERID")
    public void setORDERID(String oRDERID) {
        this.oRDERID = oRDERID;
    }

    @JsonProperty("PAYMENTMODE")
    public String getPAYMENTMODE() {
        return pAYMENTMODE;
    }

    @JsonProperty("PAYMENTMODE")
    public void setPAYMENTMODE(String pAYMENTMODE) {
        this.pAYMENTMODE = pAYMENTMODE;
    }

    @JsonProperty("RESPCODE")
    public String getRESPCODE() {
        return rESPCODE;
    }

    @JsonProperty("RESPCODE")
    public void setRESPCODE(String rESPCODE) {
        this.rESPCODE = rESPCODE;
    }

    @JsonProperty("RESPMSG")
    public String getRESPMSG() {
        return rESPMSG;
    }

    @JsonProperty("RESPMSG")
    public void setRESPMSG(String rESPMSG) {
        this.rESPMSG = rESPMSG;
    }

    @JsonProperty("STATUS")
    public String getSTATUS() {
        return sTATUS;
    }

    @JsonProperty("STATUS")
    public void setSTATUS(String sTATUS) {
        this.sTATUS = sTATUS;
    }

    @JsonProperty("TXNAMOUNT")
    public String getTXNAMOUNT() {
        return tXNAMOUNT;
    }

    @JsonProperty("TXNAMOUNT")
    public void setTXNAMOUNT(String tXNAMOUNT) {
        this.tXNAMOUNT = tXNAMOUNT;
    }

    @JsonProperty("TXNDATE")
    public String getTXNDATE() {
        return tXNDATE;
    }

    @JsonProperty("TXNDATE")
    public void setTXNDATE(String tXNDATE) {
        this.tXNDATE = tXNDATE;
    }

    @JsonProperty("TXNID")
    public String getTXNID() {
        return tXNID;
    }

    @JsonProperty("TXNID")
    public void setTXNID(String tXNID) {
        this.tXNID = tXNID;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}