package com.paytm.utils.merchant.dto.bo;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;



@JsonSerialize (include = JsonSerialize.Inclusion.NON_NULL)
@JsonPropertyOrder ({
        "searchConditions",
        "orderStatus"
})
public class PGPlusBODTO {
    @JsonProperty ("searchConditions")
    private List<SearchCondition> searchConditions = null;
    @JsonProperty("orderStatus")
    private String orderStatus;

    @JsonProperty("searchConditions")
    public List<SearchCondition> getSearchConditions() {
        return searchConditions;
    }

    public PGPlusBODTO setSearchConditions(List<SearchCondition> searchConditions) {
        this.searchConditions = searchConditions;
        return this;
    }

    @JsonProperty("orderStatus")
    public String getOrderStatus() {
        return orderStatus;
    }

    public PGPlusBODTO setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
        return this;
    }
}