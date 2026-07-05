package com.paytm.dto.emiSubvention.ApiV1Validate.request;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.dto.emiSubvention.ApiV1Banks.request.Head;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "head",
        "body"
})
public class ApiV1ValidateRequest implements Serializable {


    private ApiV1ValidateRequest(Builder builder) {
        this.head = new Head()
                .setToken(builder.token)
                .setTokenType(builder.tokenType);

        this.body = new Body()
                .setMid(builder.mid)
                .setItems(builder.items)
                .setCustomerId(builder.customerId)
                .setGenerateTokenForIntent(builder.generateTokenForIntent)
                .setPaymentDetails(builder.paymentDetails)
                .setPlanId(builder.planId)
                .setCacheCardToken(builder.cacheCardToken)
                .setPrice(builder.price)
                .setOriginalPrice(builder.originalPrice)
                .setSubventionAmount(builder.subventionAmount)
                .setOfferDetails(builder.offerDetails);
    }

    public static class Builder {
        private String token;
        private String tokenType;
        private String mid;
        String subventionAmount;
        private OfferDetails offerDetails;
        private String id, productId, brandId, merchantId, model, ean, discoverability, verticalId, merchantKey, customerId;
        String price, quantity, originalPrice;
        private boolean isPhysical, isEmiEnabled, generateTokenForIntent;
        private List<String> categoryList;
        private List<Item> items = null;
        private PaymentDetails paymentDetails;
        private BigInteger planId;
        private String cacheCardToken;

        public Builder() {
            this.items = Arrays.asList(new Item());
        }

        public Builder(String token, String tokenType, String mid) {
            this.token = token;
            this.tokenType = tokenType;
            this.mid = mid;
            this.customerId = "9191";
            this.items = Arrays.asList(new Item());
        }

        public Builder(String price,String subventionAmount)
        {
            this.price = price;
            this.subventionAmount = subventionAmount;
            this.items = null;
        }

        public Builder setCacheCardToken(String cacheCardToken) {
            this.cacheCardToken = cacheCardToken;
            return this;
        }

        public Builder setGenerateTokenForIntent(boolean generateTokenForIntent) {
            this.generateTokenForIntent = generateTokenForIntent;
            return this;
        }

        public Builder setPlanId(BigInteger planId) {
            this.planId = planId;
            return this;
        }

        public Builder setCustomerId(String customerId) {
            this.customerId = customerId;
            return this;
        }

        public Builder setPaymentDetails(PaymentDetails paymentDetails) {
            this.paymentDetails = paymentDetails;
            return this;
        }

        public Builder setOfferDetails(OfferDetails offerDetails) {
            this.offerDetails = offerDetails;
            return this;
        }

        public Builder setMerchantKey(String merchantKey) {
            this.merchantKey = merchantKey;
            return this;
        }

        public Builder setCategoryList(List<String> categoryList) {
            this.categoryList = categoryList;
            return this;
        }

        public Builder setItems(List<Item> items) {
            this.items = items;
            return this;
        }

        public Builder setToken(String token) {
            this.token = token;
            return this;
        }

        public Builder setTokenType(String tokenType) {
            this.tokenType = tokenType;
            return this;
        }

        public Builder setMid(String mid) {
            this.mid = mid;
            return this;
        }

        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        public Builder setProductId(String productId) {
            this.productId = productId;
            return this;
        }

        public Builder setBrandId(String brandId) {
            this.brandId = brandId;
            return this;
        }

        public Builder setMerchantId(String merchantId) {
            this.merchantId = merchantId;
            return this;
        }

        public Builder setModel(String model) {
            this.model = model;
            return this;
        }

        public Builder setEan(String ean) {
            this.ean = ean;
            return this;
        }

        public Builder setPrice(String price) {
            this.price = price;
            return this;
        }
        public Builder setOriginalPrice(String originalPrice) {
            this.originalPrice = originalPrice;
            return this;
        }

        public Builder setSubventionAmount(String subventionAmount) {
            this.subventionAmount = subventionAmount;
            return this;
        }


        public Builder setQuantity(String quantity) {
            this.quantity = quantity;
            return this;
        }

        public Builder setDiscoverability(String discoverability) {
            this.discoverability = discoverability;
            return this;
        }

        public Builder setVerticalId(String verticalId) {
            this.verticalId = verticalId;
            return this;
        }

        public Builder setPhysical(boolean physical) {
            isPhysical = physical;
            return this;
        }

        public Builder setEmiEnabled(boolean emiEnabled) {
            isEmiEnabled = emiEnabled;
            return this;
        }

        public ApiV1ValidateRequest build() {
            ApiV1ValidateRequest a = new ApiV1ValidateRequest(this);
            if (a.head.getTokenType() == "CHECKSUM") {
                String checkSum = PGPHelpers.getNativeChecksum(this.merchantKey, a.getBody());
                a.getHead().setToken(checkSum);
            }
            return a;
        }
    }

    @JsonProperty("head")
    private Head head;
    @JsonProperty("body")
    private Body body;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();
    private final static long serialVersionUID = -4405432493028224894L;

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

    @Override
    public String toString() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return "";
    }

}