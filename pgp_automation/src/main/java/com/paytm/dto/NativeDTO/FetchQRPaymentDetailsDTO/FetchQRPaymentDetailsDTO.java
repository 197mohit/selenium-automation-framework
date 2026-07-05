package com.paytm.dto.NativeDTO.FetchQRPaymentDetailsDTO;

import com.fasterxml.jackson.annotation.*;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "head",
        "body"
})
public class FetchQRPaymentDetailsDTO implements Serializable
{

    @JsonProperty("head")
    private Head head;
    @JsonProperty("body")
    private Body body;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();
    private final static long serialVersionUID = -8799005937213284111L;

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


    public FetchQRPaymentDetailsDTO(Builder builder)
    {
        this.head = new Head()
                .setVersion(builder.version)
                .setRequestTimestamp(builder.requestTimestamp)
                .setRequestId(builder.requestId)
                .setChannelId(builder.channelId)
                .setTokenType(builder.tokenType)
                .setToken(builder.token);

        this.body = new Body()
                .setMid(builder.mid)
                .setQrCodeId(builder.qrCodeId)
                .setMlvSupported(builder.mlvSupported)
                .setOrderId(builder.orderId)
                .setGenerateOrderId(builder.generateorderId)
                .setsupportedPayModesForAddNPay(builder.supportedPayModesForAddNPay)
                .setIsLiteEligible(builder.isLiteEligible)
                .setmerchantVpa(builder.merchantVpa)
                .setTpap(builder.tpap);

    }

    public static class Builder {
        private String version = "v1";
        private String requestTimestamp = "1544614590000";
        private String requestId ="test123";
        private String channelId ="APP";
        private String tokenType;
        private String token;
        private String mid;
        private String qrCodeId;
        private boolean mlvSupported=false;
        private String  orderId;
        private String generateorderId;
        private String supportedPayModesForAddNPay;
        private Boolean isLiteEligible;
        private String merchantVpa;
        private Boolean tpap;
        private Boolean isOffline;

        public Builder setMerchantVpa(String merchantVpa) {
            this.merchantVpa = merchantVpa;
            return this;
        }

        public Builder setTpap(Boolean tpap) {
            this.tpap = tpap;
            return this;

        }

        public Builder setOffline(Boolean offline) {
            this.isOffline=isOffline;
           return this;
        }


        public Builder setVersion(String version)
        {
            this.version=version;
            return this;
        }

        public Builder setRequestTimeStamp(String requestTimeStamp)
        {
            this.requestTimestamp=requestTimeStamp;
            return this;
        }

        public Builder setRequestId(String requestId)
        {
            this.requestId=requestId;
            return this;
        }

        public Builder setChannelId(String channelId)
        {
            this.channelId=channelId;
            return this;
        }

        public Builder setTokenType(String tokenType)
        {
            this.tokenType=tokenType;
            return this;
        }

        public Builder setToken(String token)
        {
            this.token=token;
            return this;
        }

        public Builder setMID(String mid)
        {
            this.mid=mid;
            return this;
        }

        public Builder setQRCodeId(String qrCodeId)
        {
            this.qrCodeId=qrCodeId;
            return this;
        }
        public Builder setorderId(String orderId)
        {
            this.orderId=orderId;
            return this;
        }
        public Builder setgenerateorderId(String generateorderId)
        {
            this.generateorderId=generateorderId;
            return this;
        }
        public Builder setMLVSupported(boolean mlvSupported)
        {
            this.mlvSupported=mlvSupported;
            return this;
        }
        public Builder setsupportedPayModesForAddNPay(String supportedPayModesForAddNPay){
            this.supportedPayModesForAddNPay=supportedPayModesForAddNPay;
            return this;
        }
        public Builder setIsLiteEligible(Boolean isLiteEligible)
        {
            this.isLiteEligible=isLiteEligible;
            return this;
        }



        public FetchQRPaymentDetailsDTO build() {
            return new FetchQRPaymentDetailsDTO(this);
        }

    }
}
