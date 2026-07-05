package com.paytm.utils.merchant.dto.refund;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by ankuragarwal on 13/12/18
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SubWalletAmount {
    @JsonProperty("FOOD")
    private String food;

    public String getFood() {
        return food;
    }

    public void setFood(String food) {
        this.food = food;
    }
}
