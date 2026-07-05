package com.paytm.dto.NativeDTO.OfferApply;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Item {
    
    @JsonProperty("brandId")
    private String brandId;
    
    @JsonProperty("categoryId")
    private String categoryId;
    
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("offerDetails")
    private OfferDetails offerDetails;
    
    @JsonProperty("price")
    private Double price;
    
    @JsonProperty("productId")
    private String productId;
    
    @JsonProperty("verticalId")
    private Double verticalId;

    public Item() {}


    public Item(String id,String brandId, String categoryId,String productId, Double price) {
        this.brandId = brandId;
        this.categoryId = categoryId;
        this.id = id;
        this.price = price;
        this.productId = productId;
    }
    public Item(String id,String brandId, String categoryId,String productId, Double price, Double verticalId) {
        this.brandId = brandId;
        this.categoryId = categoryId;
        this.id = id;
        this.price = price;
        this.productId = productId;
        this.verticalId = verticalId;
    }
    public Item(String id, String brandId, String categoryId, String productId, Double price, Double verticalId, OfferDetails offerDetails) {
        this.brandId = brandId;
        this.categoryId = categoryId;
        this.id = id;
        this.price = price;
        this.productId = productId;
        this.verticalId = verticalId;
        this.offerDetails = offerDetails;
    }


    public String getBrandId() {
        return brandId;
    }

    public void setBrandId(String brandId) {
        this.brandId = brandId;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public OfferDetails getOfferDetails() {
        return offerDetails;
    }

    public void setOfferDetails(OfferDetails offerDetails) {
        this.offerDetails = offerDetails;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public Double getVerticalId() {
        return verticalId;
    }

    public void setVerticalId(Double verticalId) {
        this.verticalId = verticalId;
    }
}