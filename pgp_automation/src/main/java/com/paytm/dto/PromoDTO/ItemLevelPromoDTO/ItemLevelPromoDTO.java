package com.paytm.dto.PromoDTO.ItemLevelPromoDTO;

import com.fasterxml.jackson.annotation.*;
import com.paytm.dto.PromoDTO.ItemLevelPromoDTO.Body;
import com.paytm.dto.PromoDTO.ItemLevelPromoDTO.Head;
import com.paytm.dto.PromoDTO.ItemLevelPromoDTO.PaymentOptions;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "head",
        "body"
})

public class ItemLevelPromoDTO implements Serializable
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


    public ItemLevelPromoDTO(Builder builder)
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
                .setPaymentOptions((builder.PaymentOptions))
                .setpromoContext(builder.promoContext);

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
        private String promoContext;

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

        public Builder setPromocode(String promocode)
        {
            this.promocode=promocode;
            return this;
        }

        public Builder setTotalTransactionAmount(String totalTransactionAmount)
        {
            this.totalTransactionAmount=totalTransactionAmount;
            return this;
        }


        public Builder setPaymentOptions(PaymentOptions paymentOptions[])
        {
            this.PaymentOptions = paymentOptions;
            return this;
        }
        public Builder setPromoContext(String promoContext)
        {
            this.promoContext = promoContext;
            return this;
        }


        public ItemLevelPromoDTO build() {
            return new ItemLevelPromoDTO(this);
        }

    }


}
