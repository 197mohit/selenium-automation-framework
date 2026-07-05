package com.paytm.dto.OfflineDto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by anjukumari on 04/12/18
 */
public class OrderAmount {
    @JsonProperty("currency")
    private String currency;
    @JsonProperty("value")
    private String value;
    @JsonProperty("currency")
    public String getCurrency() {
        return currency;
    }

    @JsonProperty("currency")
    public void setCurrency(String currency) {
        this.currency = currency;
    }

    @JsonProperty("value")
    public String getValue() {
        return value;
    }

    @JsonProperty("value")
    public void setValue(String value) {
        this.value = value;
    }

    public OrderAmount() {
        this.currency = "INR";
        this.value = "2.0";
    }
}
