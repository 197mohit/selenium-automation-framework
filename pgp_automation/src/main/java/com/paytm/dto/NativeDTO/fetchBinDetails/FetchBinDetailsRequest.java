package com.paytm.dto.NativeDTO.fetchBinDetails;

import com.fasterxml.jackson.annotation.*;
import com.paytm.appconstants.Constants;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by anjukumari on 15/10/18
 */

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "head",
        "body"
})
public class FetchBinDetailsRequest {

    @JsonProperty("head")
    private Head head;
    @JsonProperty("body")
    private Body body;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

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

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    private FetchBinDetailsRequest(Builder builder) {
        this.body = new Body(builder.bin, builder.paymentMode).setIsEMIDetail(builder.isEMIDetail).setMid(builder.mid).setEmiType(builder.emiType)
                .setRequestType(builder.requestType);
        this.head = new Head(builder.version, builder.requestTimestamp, builder.channelId, builder.txnToken)
                .setTokenType(builder.tokenType)
                .setToken(builder.token);
    }


    public static class Builder {
        private String bin;
        private String txnToken;
        private String version;
        private String requestTimestamp;
        private String channelId;
        private String paymentMode;
        private String tokenType;
        private String token;
        private String isEMIDetail;
        private String mid;

        private String emiType;

        private String requestType;


        public Builder(String TToken, String binNum) {
            this.bin = binNum;
            this.txnToken = TToken;
            this.version = "v1";
            this.requestTimestamp = "Time";
            this.channelId = "WEB";
        }

        public Builder(String TToken, String binNum,String emiType) {
            this.bin = binNum;
            this.txnToken = TToken;
            this.version = "v1";
            this.requestTimestamp = "Time";
            this.channelId = "WEB";
            this.emiType=emiType;
        }

        public Builder(String binNum, Constants.MerchantType mid, String token, String isEMIDetail){
            this.tokenType = "GUEST";
            this.bin = binNum;
            this.version = "v1";
            this.requestTimestamp = "Time";
            this.channelId = "WEB";
            this.mid = mid.getId();
            this.token = token;
            this.isEMIDetail = isEMIDetail;
        }

        public String getBin() {
            return bin;
        }

        public Builder setBin(String bin) {
            this.bin = bin;
            return this;
        }

        public String getTxnToken() {
            return txnToken;
        }

        public Builder setTxnToken(String txnToken) {
            this.txnToken = txnToken;
            return this;
        }

        public String getVersion() {
            return version;
        }

        public Builder setVersion(String version) {
            this.version = version;
            return this;
        }

        public String getPaymentMode() {
            return paymentMode;
        }

        public Builder setPaymentMode(String paymentMode) {
            this.paymentMode = paymentMode;
            return this;
        }

        public String getRequestTimestamp() {
            return requestTimestamp;
        }

        public Builder setRequestTimestamp(String requestTimestamp) {
            this.requestTimestamp = requestTimestamp;
            return this;
        }

        public String getChannelId() {
            return channelId;
        }

        public Builder setChannelId(String channelId) {
            this.channelId = channelId;
            return this;
        }

        public String getTokenType(){
            return tokenType;
        }

        public Builder setTokenType(String tokenType){
            this.tokenType = tokenType;
            return this;
        }

        public String getToken(){
            return token;
        }

        public Builder setToken(String token){
            this.token = token;
            return this;
        }

        public String getIsEMIDetail(){
            return isEMIDetail;
        }

        public Builder setIsEMIDetail(String isEMIDetail){
            this.isEMIDetail = isEMIDetail;
            return this;
        }

        public String getMid(){
            return mid;
        }

        public Builder setMid(String mid){
            this.mid = mid;
            return this;
        }

        public FetchBinDetailsRequest build() {
            return new FetchBinDetailsRequest(this);
        }

        public String getEmiType() {
            return emiType;
        }

        public Builder setEmiType(String emiType) {
            this.emiType = emiType;
            return this;
        }

        public String getRequestType() {
            return requestType;
        }

        public Builder setRequestType(String requestType) {
            this.requestType = requestType;
            return this;
        }
    }
}





