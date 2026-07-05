package com.paytm.dto.NativeDTO.OfferApply;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class OfferApplyDTO {
    @JsonProperty("head")
    private Head head;
    @JsonProperty("body")
    private Body body;

    public OfferApplyDTO(Builder builder) {
        this.head = new Head(builder.channelId, builder.requestId, builder.requestTimestamp,
                builder.token, builder.tokenType, builder.version);
        this.body = new Body(builder.amountBasedBankOffer, builder.amountBasedSubvention, builder.custId, builder.items,
                builder.mid, builder.paymentDetails, builder.paytmUserId, builder.promoContext);
    }

    public Head getHead() { return head; }
    public void setHead(Head head) { this.head = head; }
    public Body getBody() { return body; }
    public void setBody(Body body) { this.body = body; }

    public static class Builder {
        private String channelId = "WAP";
        private String requestId;
        private String requestTimestamp;
        private String token;
        private String tokenType = "SSO";
        private String version = "1.0";
        private Boolean amountBasedBankOffer = false;
        private Boolean amountBasedSubvention = false;
        private String custId;
        private List<Item> items;
        private String mid;
        private PaymentDetails paymentDetails;
        private String paytmUserId;
        private PromoContext promoContext;

        public Builder(String token, String mid) {
            this.token = token;
            this.mid = mid;
            this.requestId = "Native_" + generateRequestId();
            this.requestTimestamp = String.valueOf(System.currentTimeMillis());
        }

        private String generateRequestId() {
            return java.util.UUID.randomUUID().toString().replace("-", "");
        }

        public Builder setChannelId(String channelId) {
            this.channelId = channelId;
            return this;
        }

        public Builder setAmountBasedBankOffer(Boolean amountBasedBankOffer) {
            this.amountBasedBankOffer = amountBasedBankOffer;
            return this;
        }

        public Builder setAmountBasedSubvention(Boolean amountBasedSubvention) {
            this.amountBasedSubvention = amountBasedSubvention;
            return this;
        }

        public Builder setCustId(String custId) {
            this.custId = custId;
            this.paytmUserId = custId;
            return this;
        }

        public Builder setItems(List<Item> items) {
            this.items = items;
            return this;
        }

        public Builder setPaymentDetails(PaymentDetails paymentDetails) {
            this.paymentDetails = paymentDetails;
            return this;
        }

        public Builder setPromoContext(PromoContext promoContext) {
            this.promoContext = promoContext;
            return this;
        }

        public OfferApplyDTO build() {
            return new OfferApplyDTO(this);
        }
    }
}