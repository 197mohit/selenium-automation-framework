package com.paytm.dto.NativeDTO.getEMIDetails.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "currency",
        "value"
})
public class MaxAmount {

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

    public MaxAmount withCurrency(String currency) {
        this.currency = currency;
        return this;
    }

    @JsonProperty("value")
    public String getValue() {
        return value;
    }

    @JsonProperty("value")
    public void setValue(String value) {
        this.value = value;
    }

    public MaxAmount withValue(String value) {
        this.value = value;
        return this;
    }

}