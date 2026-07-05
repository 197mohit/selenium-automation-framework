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
        "id",
        "brandId",
        "categoryIds"
})
public class ProductDetail {
    @JsonProperty("id")
    private String id;
    @JsonProperty("brandId")
    private String brandId;
    @JsonProperty("categoryIds")
    private List<String> categoryIds;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("id")
    public ProductDetail setId(String id) {
        this.id = id;
        return this;
    }

    @JsonProperty("brandId")
    public String getBrandId() {
        return brandId;
    }

    @JsonProperty("brandId")
    public ProductDetail setBrandId(String brandId) {
        this.brandId = brandId;
        return this;
    }

    @JsonProperty("categoryIds")
    public List<String> getCategoryIds() {
        return categoryIds;
    }

    @JsonProperty("categoryIds")
    public ProductDetail setCategoryIds(List<String> categoryIds) {
        this.categoryIds = categoryIds;
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
    public ProductDetail(String id,String brandId,List<String> categoryIds){
        this.id=id;
        this.brandId=brandId;
        this.categoryIds=categoryIds;
    }
}
