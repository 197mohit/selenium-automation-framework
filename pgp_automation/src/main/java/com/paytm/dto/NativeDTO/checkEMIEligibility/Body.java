package com.paytm.dto.NativeDTO.checkEMIEligibility;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.paytm.dto.NativeDTO.InitTxn.UserInfo;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "userInfo",
        "mid",
        "txnAmount",
        "channelCode",
        "emiTypes"
})
public class Body {

    @JsonProperty("userInfo")
    private UserInfo userInfo;
    @JsonProperty("mid")
    private String mid;
    @JsonProperty("channelCode")
    private String channelCode;
    @JsonProperty("emiTypes")
    private List<String> emiTypes = null;
    @JsonProperty("txnAmount")
    private String txnAmount;
    @JsonProperty("mid")
    public String getMid() {
        return mid;
    }

    @JsonProperty("userInfo")
    public UserInfo getUserInfo() {
        return userInfo;
    }

    @JsonProperty("mid")
    public Body setMid(String mid) {
        this.mid = mid;
        return this;
    }

    public Body withMid(String mid) {
        this.mid = mid;
        return this;
    }

    @JsonProperty("channelCode")
    public String getChannelCode() {
        return channelCode;
    }

    @JsonProperty("channelCode")
    public Body setChannelCode(String channelCode) {
        this.channelCode = channelCode;
        return this;
    }
  @JsonProperty("emiTypes")
    public List<String> getEmiTypes() {
        return emiTypes;
    }

    @JsonProperty("emiTypes")
    public  Body setEmiTypes(List<String> emiTypes) {
        this.emiTypes = emiTypes;
        return this;
    }

    @JsonProperty("txnAmount")
    public String getTxnAmount() {
        return txnAmount;
    }

    @JsonProperty("txnAmount")
    public Body setTxnAmount(String txnAmount) {
        this.txnAmount = txnAmount;
        return this;
    }

    public Body setUserInfo(String ssoToken, String mobileNumber) {
        this.userInfo = new UserInfo(ssoToken, mobileNumber);
        return this;
    }
}
