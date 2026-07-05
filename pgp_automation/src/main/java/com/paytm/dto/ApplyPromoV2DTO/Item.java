package com.paytm.dto.ApplyPromoV2DTO;
import java.util.LinkedHashMap;
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
        "id",
        "amount",
        "productDetails"
})
public class Item {
    @JsonProperty("id")
    private String id;
    @JsonProperty("amount")
    private Integer amount;
    @JsonProperty("productDetail")
    private ProductDetail productDetail;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("id")
    public Item setId(String id) {
        this.id = id;
        return this;
    }

    @JsonProperty("amount")
    public Integer getAmount() {
        return amount;
    }

    @JsonProperty("amount")
    public Item setAmount(Integer amount) {
        this.amount = amount;
        return this;
    }

    @JsonProperty("productDetail")
    public ProductDetail getProductDetail() {
        return productDetail;
    }

    @JsonProperty("productDetail")
    public Item setProductDetail(ProductDetail productDetail) {
        this.productDetail = productDetail;
        return this;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }
    public Item(String id, Integer amount, ProductDetail productDetail) {
        this.id = id;
        this.amount = amount;
        this.productDetail = productDetail;
    }
}
