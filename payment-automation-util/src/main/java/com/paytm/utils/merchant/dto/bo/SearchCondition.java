package com.paytm.utils.merchant.dto.bo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize (include = JsonSerialize.Inclusion.NON_NULL)
@JsonPropertyOrder({
        "searchBy",
        "searchValue"
})
public class SearchCondition {

    @JsonProperty ("searchBy")
    private String searchBy;
    @JsonProperty("searchValue")
    private String searchValue;

    @JsonProperty("searchBy")
    public String getSearchBy() {
        return searchBy;
    }

    public SearchCondition setSearchBy(String searchBy) {
        this.searchBy = searchBy;
        return this;
    }

    @JsonProperty("searchValue")
    public String getSearchValue() {
        return searchValue;
    }

    public SearchCondition setSearchValue(String searchValue) {
        this.searchValue = searchValue;
        return this;
    }



}