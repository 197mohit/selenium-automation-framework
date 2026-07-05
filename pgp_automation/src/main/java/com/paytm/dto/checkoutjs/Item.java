package com.paytm.dto.checkoutjs;

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
        "itemId",
        "productId",
        "brandId",
        "categoryList",
        "model",
        "amount",
        "quantity",
        "isEmiEnabled"
})
public class Item implements Serializable {
    @JsonProperty("itemId")
    private String itemId;
    @JsonProperty("productId")
    private String productId;
    @JsonProperty("brandId")
    private String brandId;
    @JsonProperty("categoryList")
    private List<String> categoryList = null;
    @JsonProperty("model")
    private String model;
    @JsonProperty("amount")
    private Double amount;
    @JsonProperty("quantity")
    private Double quantity;
    @JsonProperty("isEmiEnabled")
    private Boolean isEmiEnabled;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();
    private final static long serialVersionUID = -7305723053274505045L;

    public Item() {
        this.itemId = "1";
        this.productId = "321067334";
        this.brandId = "124197";
        categoryList = Arrays.asList("66781");
        this.model = "G531BT-BQ002T";
        this.amount = Double.valueOf(100);
        this.quantity = Double.valueOf(1);
        this.isEmiEnabled = true;
    }


    @JsonProperty("itemId")
    public String getItemId() {
        return itemId;
    }

    @JsonProperty("itemId")
    public Item setItemId(String itemId) {
        this.itemId = itemId;
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

    @JsonProperty("brandId")
    public String getBrandId() {
        return brandId;
    }

    @JsonProperty("brandId")
    public Item setBrandId(String brandId) {
        this.brandId = brandId;
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


    @JsonProperty("model")
    public String getModel() {
        return model;
    }

    @JsonProperty("model")
    public Item setModel(String model) {
        this.model = model;
        return this;
    }

    @JsonProperty("amount")
    public Double getamount() {
        return amount;
    }

    @JsonProperty("amount")
    public Item setamount(Double amount) {
        this.amount = amount;
        return this;
    }

    @JsonProperty("quantity")
    public Double getQuantity() {
        return quantity;
    }

    @JsonProperty("quantity")
    public Item setQuantity(Double quantity) {
        this.quantity = quantity;
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