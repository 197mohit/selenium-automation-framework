package com.paytm.dto.ApplyPromoV2DTO;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "head",
        "body"
})
public class ApplyPromoV2DTO {

    @JsonProperty("head")
    private Head head;
    @JsonProperty("body")
    private Body body;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

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

    public ApplyPromoV2DTO(Builder builder){
        this.head=new Head()
                .setChannelId(builder.channelId)
                .setRequestId(builder.requestId)
                .setRequestTimeStamp(builder.requestTimeStamp)
                .setToken(builder.token)
                .setTokenType(builder.tokenType);
        this.body=new Body()
                 .setMid(builder.mid)
                 .setOrderId(builder.orderId)
                 .setCustId(builder.custId)
                 .setPaymentOptions(builder.paymentOptions)
                 .setTotalTransactionAmount(builder.totalTransactionAmount)
                 .setPromocode(builder.promocode)
                 .setCartDetails(builder.cartDetails);
    }

    public static class Builder{
        private String requestId="9dd70277-8737-4fbf-a8f1-08532301e00e";
        private String requestTimeStamp="232";
        private String channelId="WEB";
        private String tokenType;
        private String token;
        private String mid;

        private String orderId;
        private String promocode;
        private List<PaymentOption> paymentOptions;
        private String custId;
        private String totalTransactionAmount;
        private CartDetails cartDetails;



        public Builder setRequestId(String requestId) {
            this.requestId = requestId;
            return this;
        }

        public Builder setOrderId(String orderId) {
            this.orderId = orderId;
            return this;
        }

        public Builder setRequestTimeStamp(String requestTimeStamp) {
            this.requestTimeStamp = requestTimeStamp;
            return this;
        }

        public Builder setChannelId(String channelId) {
            this.channelId = channelId;
            return this;
        }

        public Builder setTokenType(String tokenType) {
            this.tokenType = tokenType;
            return this;
        }

        public Builder setToken(String token) {
            this.token = token;
            return this;
        }

        public Builder setMid(String mid) {
            this.mid = mid;
            return this;
        }

        public Builder setPromocode(String promocode) {
            this.promocode = promocode;
            return this;
        }

        public Builder setPaymentOptions(List<PaymentOption> paymentOptions) {
            this.paymentOptions = paymentOptions;
            return this;
        }

        public Builder setCustId(String custId) {
            this.custId = custId;
            return this;
        }

        public Builder setTotalTransactionAmount(String totalTransactionAmount) {
            this.totalTransactionAmount = totalTransactionAmount;
            return this;
        }

        public Builder setCartDetails(CartDetails cartDetails) {
            this.cartDetails = cartDetails;
            return this;
        }

        public ApplyPromoV2DTO build() {
            return new ApplyPromoV2DTO(this);
        }
    }

}
