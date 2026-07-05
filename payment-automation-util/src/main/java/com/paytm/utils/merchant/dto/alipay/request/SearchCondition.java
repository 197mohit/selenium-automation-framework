package com.paytm.utils.merchant.dto.alipay.request;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "searchKey",
        "searchValue"
})
public class SearchCondition {

    @JsonProperty("searchKey")
    private String searchKey;
    @JsonProperty("searchValue")
    private String searchValue;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    @JsonProperty("searchKey")
    public String getSearchKey() {
        return searchKey;
    }

    @JsonProperty("searchKey")
    public void setSearchKey(String searchKey) {
        this.searchKey = searchKey;
    }

    @JsonProperty("searchValue")
    public String getSearchValue() {
        return searchValue;
    }

    @JsonProperty("searchValue")
    public void setSearchValue(String searchValue) {
        this.searchValue = searchValue;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}

