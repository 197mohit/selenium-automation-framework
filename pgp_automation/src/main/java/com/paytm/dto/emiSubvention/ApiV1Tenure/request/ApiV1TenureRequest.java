package com.paytm.dto.emiSubvention.ApiV1Tenure.request;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.dto.emiSubvention.ApiV1Banks.request.Head;
import com.paytm.dto.emiSubvention.ApiV1Banks.request.Item;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "head",
        "body"
})
public class ApiV1TenureRequest implements Serializable {

    private ApiV1TenureRequest(Builder builder) {
        this.head = new Head()
                .setToken(builder.token)
                .setTokenType(builder.tokenType);

        this.body = new Body()
                .setMid(builder.mid)
                .setItems(builder.items)
                .setFilters(builder.filters)
                .setPrice(builder.price)
                .setOriginalPrice(builder.originalPrice)
                .setSubventionAmount(builder.subventionAmount);
    }

    public static class Builder {
        private String token;
        private String tokenType;
        private String mid;
        String subventionAmount;
        private String id, productId, brandId, merchantId, model, ean, discoverability, verticalId, merchantKey;
        String price, quantity,originalPrice;
        private boolean isPhysical, isEmiEnabled;
        private List<String> categoryList;
        private List<Item> items = null;
        private Filters filters;

        public Builder() {
            this.items = Arrays.asList(new Item());
        }

        public Builder(String token, String tokenType, String mid) {
            this.token = token;
            this.tokenType = tokenType;
            this.mid = mid;
            this.items = Arrays.asList(new Item());
        }

        public Builder(String price, String subventionAmount)
        {
            this.price = price;
            this.subventionAmount = subventionAmount;
            this.items = null;

        }

        public Builder setFilters(Filters filters) {
            this.filters = filters;
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

        public ApiV1TenureRequest build() {
            ApiV1TenureRequest a = new ApiV1TenureRequest(this);
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
    public ApiV1TenureRequest setHead(Head head) {
        this.head = head;
        return this;
    }

    @JsonProperty("body")
    public Body getBody() {
        return body;
    }

    @JsonProperty("body")
    public ApiV1TenureRequest setBody(Body body) {
        this.body = body;
        return this;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public ApiV1TenureRequest setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
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
