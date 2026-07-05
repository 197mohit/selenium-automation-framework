package com.paytm.dto.emiSubvention.ApiV1Tenure.request;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.dto.emiSubvention.ApiV1Banks.request.Item;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "items",
        "filters",
        "mid",
        "price",
        "subventionAmount",
        "originalPrice"
})
public class Body implements Serializable {

    @JsonProperty("items")
    private List<Item> items = null;
    @JsonProperty("filters")
    private Filters filters;
    @JsonProperty("mid")
    private String mid;
    @JsonProperty("price")
    private String price;
    @JsonProperty("originalPrice")
    private String originalPrice;
    @JsonProperty("subventionAmount")
    private String subventionAmount;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();
    private final static long serialVersionUID = -2430554182743537485L;

    @JsonProperty("items")
    public List<Item> getItems() {
        return items;
    }

    @JsonProperty("items")
    public Body setItems(List<Item> items) {
        this.items = items;
        return this;
    }

    @JsonProperty("filters")
    public Filters getFilters() {
        return filters;
    }

    @JsonProperty("filters")
    public Body setFilters(Filters filters) {
        this.filters = filters;
        return this;
    }

    @JsonProperty("mid")
    public String getMid() {
        return mid;
    }

    @JsonProperty("mid")
    public Body setMid(String mid) {
        this.mid = mid;
        return this;
    }

    @JsonProperty("price")
    public String getPrice() {
        return price;
    }

    @JsonProperty("price")
    public Body setPrice(String price) {
        this.price = price;
        return this;
    }
    @JsonProperty("originalPrice")
    public String getOriginalPrice() {
        return originalPrice;
    }

    @JsonProperty("originalPrice")
    public Body setOriginalPrice(String originalPrice) {
        this.originalPrice = originalPrice;
        return this;
    }

    @JsonProperty("subventionAmount")
    public String getSubventionAmount() {
        return subventionAmount;
    }

    @JsonProperty("subventionAmount")
    public Body setSubventionAmount(String subventionAmount) {
        this.subventionAmount = subventionAmount;
        return this;
    }


    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public Body setAdditionalProperty(String name, Object value) {
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