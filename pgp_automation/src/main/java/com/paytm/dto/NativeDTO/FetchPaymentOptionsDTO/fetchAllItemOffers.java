package com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.paytm.dto.NativeDTO.InitTxn.SimplifiedSubvention;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "ultimateBeneficiaryName"
})
public class fetchAllItemOffers {


    public List<SimplifiedSubvention.Item> getItems() {
        return items;
    }

    public void setItems(List<SimplifiedSubvention.Item> items) {
        this.items = items;
    }

    @JsonProperty("items")
    private List<SimplifiedSubvention.Item> items;


}
