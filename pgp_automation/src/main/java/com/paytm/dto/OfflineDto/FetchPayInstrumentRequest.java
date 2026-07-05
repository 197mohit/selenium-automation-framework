package com.paytm.dto.OfflineDto;
import com.fasterxml.jackson.annotation.JsonProperty;
/**
 * Created by anjukumari on 04/12/18
 */
public class FetchPayInstrumentRequest {
    @JsonProperty("head")
    private Head head;
    @JsonProperty("body")
    private Body body;

    @JsonProperty("head")
    public Head getHead() {
        return head;
    }

    @JsonProperty("head")
    public void setHead(Head head) {
        this.head = head;
    }

    @JsonProperty("body")
    public Body getBody() {
        return body;
    }

    @JsonProperty("body")
    public void setBody(Body body) {
        this.body = body;
    }

    public void changeMidInRequest(String mid){
        this.head.setMid(mid);
    }

    public void changeTokenInRequest(String token){
        this.head.setToken(token);
    }

    public void changeTxnAmount(String amt){
        this.body.getOrderAmount().setValue(amt);
    }

    public void changePostPaidOnboarding(String postpaidOnbooarding) {this.body.setPostpaidOnboardingSupported(postpaidOnbooarding);}

    public FetchPayInstrumentRequest() {
        this.head = new Head();
        this.body = new Body();
    }

}
