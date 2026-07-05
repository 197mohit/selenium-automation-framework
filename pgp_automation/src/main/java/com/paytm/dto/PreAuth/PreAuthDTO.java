package com.paytm.dto.PreAuth;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.paytm.appconstants.Constants;
import com.paytm.dto.OrderDTO;
import com.paytm.utils.merchant.util.PGPUtil;

import java.util.TreeMap;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "MID",
        "ORDER_ID",
        "TXN_AMOUNT",
        "TOKEN",
        "DURATIONHRS",
        "CHECKSUM"
})
public class PreAuthDTO {

    @JsonProperty("MID")
    private String mID;
    @JsonProperty("ORDER_ID")
    private String oRDER_ID;
    @JsonProperty("TXN_AMOUNT")
    private String tXN_AMOUNT;
    @JsonProperty("TOKEN")
    private String tOKEN;
    @JsonProperty("DURATIONHRS")
    private Integer dURATIONHRS;
    @JsonProperty("CHECKSUM")
    private String cHECKSUM;

    public PreAuthDTO(String oRDER_ID, Constants.MerchantType merchant, String tXN_AMOUNT, String sSOToken){
        TreeMap<String, String> treeMap = new TreeMap<>();
        this.oRDER_ID = oRDER_ID;
        treeMap.put("ORDER_ID",this.oRDER_ID);
        this.mID = merchant.getId();
        treeMap.put("MID", this.mID);
        this.tXN_AMOUNT = tXN_AMOUNT;
        treeMap.put("TXN_AMOUNT", this.tXN_AMOUNT);
        this.tOKEN = sSOToken;
        treeMap.put("TOKEN", this.tOKEN);
        this.dURATIONHRS = 1;
        treeMap.put("DURATIONHRS", this.dURATIONHRS.toString());
        this.cHECKSUM = PGPUtil.getChecksum(merchant.getKey(), treeMap);
    }

    public PreAuthDTO(OrderDTO orderDTO){
        TreeMap<String, String> treeMap = new TreeMap<>();
        this.oRDER_ID = orderDTO.getORDER_ID();
        treeMap.put("ORDER_ID",this.oRDER_ID);
        this.mID = orderDTO.getMID();
        treeMap.put("MID", this.mID);
        this.tXN_AMOUNT = orderDTO.getTXN_AMOUNT();
        treeMap.put("TXN_AMOUNT", this.tXN_AMOUNT);
        this.tOKEN = orderDTO.getSSO_TOKEN();
        treeMap.put("TOKEN", this.tOKEN);
        this.dURATIONHRS = 1;
        treeMap.put("DURATIONHRS", this.dURATIONHRS.toString());
        this.cHECKSUM = PGPUtil.getChecksum(orderDTO.getMerchantKey(), treeMap);
    }

    @JsonProperty("MID")
    public String getMID() {
        return mID;
    }

    @JsonProperty("MID")
    public PreAuthDTO setMID(String mID) {
        this.mID = mID;
        return this;
    }

    @JsonProperty("ORDER_ID")
    public String getORDER_ID() {
        return oRDER_ID;
    }

    @JsonProperty("ORDER_ID")
    public PreAuthDTO setORDER_ID(String oRDER_ID) {
        this.oRDER_ID = oRDER_ID;
        return this;
    }

    @JsonProperty("TXN_AMOUNT")
    public String getTXN_AMOUNT() {
        return tXN_AMOUNT;
    }

    @JsonProperty("TXN_AMOUNT")
    public PreAuthDTO setTXN_AMOUNT(String tXN_AMOUNT) {
        this.tXN_AMOUNT = tXN_AMOUNT;
        return this;
    }

    @JsonProperty("TOKEN")
    public String getTOKEN() {
        return tOKEN;
    }

    @JsonProperty("TOKEN")
    public PreAuthDTO setTOKEN(String tOKEN) {
        this.tOKEN = tOKEN;
        return this;
    }

    @JsonProperty("DURATIONHRS")
    public Integer getDURATIONHRS() {
        return dURATIONHRS;
    }

    @JsonProperty("DURATIONHRS")
    public PreAuthDTO setDURATIONHRS(Integer dURATIONHRS) {
        this.dURATIONHRS = dURATIONHRS;
        return this;
    }

    @JsonProperty("CHECKSUM")
    public String getCHECKSUM() {
        return cHECKSUM;
    }

    @JsonProperty("CHECKSUM")
    public PreAuthDTO setCHECKSUM(String cHECKSUM) {
        this.cHECKSUM = cHECKSUM;
        return this;
    }

}