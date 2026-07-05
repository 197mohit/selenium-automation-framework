package com.paytm.dto.PromoDTO.ApplyPromoDTO;

import com.fasterxml.jackson.annotation.*;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "head",
        "body"
})

public class ApplyPromoDTO implements Serializable
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


    public ApplyPromoDTO(ApplyPromoDTO.Builder builder)
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
                .setPromocode(builder.promocode)
                .setTotalTransactionAmount(builder.totalTransactionAmount)
                .setPaymentOptions((builder.PaymentOptions));

    }

    public static class Builder {
        private String version = "v1";
        private String requestTimestamp = "TIME";
        private String requestId ="test123";
        private String channelId ="WAP";
        private String tokenType;
        private String token;
        private String mid;
        private String promocode;
        private String totalTransactionAmount;
        private PaymentOptions[] PaymentOptions;

        public ApplyPromoDTO.Builder setVersion(String version)
        {
            this.version=version;
            return this;
        }

        public ApplyPromoDTO.Builder setRequestTimeStamp(String requestTimeStamp)
        {
            this.requestTimestamp=requestTimeStamp;
            return this;
        }

        public ApplyPromoDTO.Builder setRequestId(String requestId)
        {
            this.requestId=requestId;
            return this;
        }

        public ApplyPromoDTO.Builder setChannelId(String channelId)
        {
            this.channelId=channelId;
            return this;
        }

        public ApplyPromoDTO.Builder setTokenType(String tokenType)
        {
            this.tokenType=tokenType;
            return this;
        }

        public ApplyPromoDTO.Builder setToken(String token)
        {
            this.token=token;
            return this;
        }

        public ApplyPromoDTO.Builder setMID(String mid)
        {
            this.mid=mid;
            return this;
        }

        public ApplyPromoDTO.Builder setPromocode(String promocode)
        {
            this.promocode=promocode;
            return this;
        }

        public ApplyPromoDTO.Builder setTotalTransactionAmount(String totalTransactionAmount)
        {
            this.totalTransactionAmount=totalTransactionAmount;
            return this;
        }


        public ApplyPromoDTO.Builder setPaymentOptions(PaymentOptions paymentOptions[])
        {
            this.PaymentOptions = paymentOptions;
            return this;
        }


        public ApplyPromoDTO build() {
            return new ApplyPromoDTO(this);
        }

    }


}
