package com.paytm.dto.processTransactionV1;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "value",
        "currency"
})

public class TipAmount {
    @JsonProperty("value")
    private String value;
    @JsonProperty("currency")
    private String currency = "INR";

    @JsonProperty("value")
    public String getValue() {
        return value;
    }

    @JsonProperty("value")
    public TipAmount setValue(String value) {
        this.value = value;
        return this;
    }

    @JsonProperty("currency")
    public String getCurrency() {
        return currency;
    }

    @JsonProperty("currency")
    public TipAmount setCurrency(String currency) {
        this.currency = currency;
        return this;
    }
}
