package com.paytm.utils.merchant.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by deepakkumar on 17/10/17.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DocsDetails {

    @JsonProperty("DETAILED_LIST")
    private String[] detailedList;

    public String[] getDetailedList() {
        return detailedList;
    }

    public DocsDetails setDetailedList(String... docList) {
        this.detailedList = docList;
        return this;
    }
}
