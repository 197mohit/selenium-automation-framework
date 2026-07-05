package com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "id",
        "productId",
        "categoryList",
        "merchantId",
        "model",
        "ean",
        "price",
        "quantity",
        "discoverability",
        "verticalId",
        "isPhysical",
        "isEmiEnabled",
        "offerDetails"
})
public class Item implements Serializable {

    @JsonProperty("id")
    private String id;
    @JsonProperty("productId")
    private String productId;
    @JsonProperty("categoryList")
    private List<String> categoryList = null;
    @JsonProperty("merchantId")
    private String merchantId;
    @JsonProperty("model")
    private String model;
    @JsonProperty("ean")
    private String ean;
    @JsonProperty("price")
    private Integer price;
    @JsonProperty("quantity")
    private Integer quantity;
    @JsonProperty("discoverability")
    private String discoverability;
    @JsonProperty("verticalId")
    private String verticalId;
    @JsonProperty("isPhysical")
    private String isPhysical;
    @JsonProperty("isEmiEnabled")
    private Boolean isEmiEnabled;
    @JsonProperty("offerDetails")
    private OfferDetails offerDetails;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();
    private final static long serialVersionUID = -5475753441394747901L;


    public Item() {
        this.id = "1234";
        this.productId = "27902";
        this.categoryList = Arrays.asList(new String[]{"categoryList"});
        this.merchantId = "995183";
        this.model = "awasdfasd";
        this.ean = "P30";
        this.price = 200;
        this.quantity = 1;
        this.discoverability = "n";
        this.verticalId = "VID2";
        this.isPhysical = "false";
        this.isEmiEnabled = true;
        this.offerDetails = new OfferDetails().setOfferId(32168);
    }


    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("id")
    public Item setId(String id) {
        this.id = id;
        return this;
    }

    @JsonProperty("productId")
    public String getProductId() {
        return productId;
    }

    @JsonProperty("productId")
    public Item setProductId(String productId) {
        this.productId = productId;
        return this;
    }

    @JsonProperty("categoryList")
    public List<String> getCategoryList() {
        return categoryList;
    }

    @JsonProperty("categoryList")
    public Item setCategoryList(List<String> categoryList) {
        this.categoryList = categoryList;
        return this;
    }

    @JsonProperty("merchantId")
    public String getMerchantId() {
        return merchantId;
    }

    @JsonProperty("merchantId")
    public Item setMerchantId(String merchantId) {
        this.merchantId = merchantId;
        return this;
    }

    @JsonProperty("model")
    public String getModel() {
        return model;
    }

    @JsonProperty("model")
    public Item setModel(String model) {
        this.model = model;
        return this;
    }

    @JsonProperty("ean")
    public String getEan() {
        return ean;
    }

    @JsonProperty("ean")
    public Item setEan(String ean) {
        this.ean = ean;
        return this;
    }

    @JsonProperty("price")
    public Integer getPrice() {
        return price;
    }

    @JsonProperty("price")
    public Item setPrice(Integer price) {
        this.price = price;
        return this;
    }

    @JsonProperty("quantity")
    public Integer getQuantity() {
        return quantity;
    }

    @JsonProperty("quantity")
    public Item setQuantity(Integer quantity) {
        this.quantity = quantity;
        return this;
    }

    @JsonProperty("discoverability")
    public String getDiscoverability() {
        return discoverability;
    }

    @JsonProperty("discoverability")
    public Item setDiscoverability(String discoverability) {
        this.discoverability = discoverability;
        return this;
    }

    @JsonProperty("verticalId")
    public String getVerticalId() {
        return verticalId;
    }

    @JsonProperty("verticalId")
    public Item setVerticalId(String verticalId) {
        this.verticalId = verticalId;
        return this;
    }

    @JsonProperty("isPhysical")
    public String getIsPhysical() {
        return isPhysical;
    }

    @JsonProperty("isPhysical")
    public Item setIsPhysical(String isPhysical) {
        this.isPhysical = isPhysical;
        return this;
    }

    @JsonProperty("isEmiEnabled")
    public Boolean getIsEmiEnabled() {
        return isEmiEnabled;
    }

    @JsonProperty("isEmiEnabled")
    public Item setIsEmiEnabled(Boolean isEmiEnabled) {
        this.isEmiEnabled = isEmiEnabled;
        return this;
    }

    @JsonProperty("offerDetails")
    public OfferDetails getOfferDetails() {
        return offerDetails;
    }

    @JsonProperty("offerDetails")
    public Item setOfferDetails(OfferDetails offerDetails) {
        this.offerDetails = offerDetails;
        return this;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public Item setAdditionalProperty(String name, Object value) {
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
