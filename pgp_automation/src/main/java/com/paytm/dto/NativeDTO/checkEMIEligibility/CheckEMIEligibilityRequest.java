package com.paytm.dto.NativeDTO.checkEMIEligibility;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.paytm.dto.NativeDTO.InitTxn.UserInfo;

import java.util.ArrayList;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "head",
        "body"
})
public class CheckEMIEligibilityRequest {

    @JsonProperty("head")
    private Head head;
    @JsonProperty("body")
    private Body body;


    public CheckEMIEligibilityRequest(){}
    public CheckEMIEligibilityRequest(String token, String mid, String channelCode, ArrayList emiTypes) {
        this.head=new Head().setToken(token).setChannelId("WEB");
        this.body=new Body().setEmiTypes(emiTypes).setMid(mid).setChannelCode(channelCode);
  }

    public CheckEMIEligibilityRequest(String version, String requestTimestamp, String channelId, String clientId, String tokenType, String token, String ssoToken, String mobileNumber, String mid, String txnAmount, String channelCode, ArrayList emiTypes) {
        this.head = new Head().setVersion(version).setRequestTimestamp(requestTimestamp).setChannelId(channelId).setClientId(clientId).setTokenType(tokenType).setToken(token);
        this.body = new Body().setUserInfo(ssoToken,mobileNumber).setMid(mid).setTxnAmount(txnAmount).setChannelCode(channelCode).setEmiTypes(emiTypes);
    }

    @JsonProperty("head")
    public Head getHead() {
        return head;
    }

    @JsonProperty("head")
    public CheckEMIEligibilityRequest setHead(Head head) {
        this.head = head;
        return this;
    }
    @JsonProperty("body")
    public Body getBody() {
        return body;
    }

    @JsonProperty("body")
    public CheckEMIEligibilityRequest setBody(Body body) {
        this.body = body;
        return this;
    }


}