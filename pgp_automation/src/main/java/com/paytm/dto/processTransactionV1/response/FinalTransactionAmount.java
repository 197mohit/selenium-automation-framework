package com.paytm.dto.processTransactionV1.response;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "value",
        "currency"
})
public class FinalTransactionAmount {

    @JsonProperty("value")
    public String value = null;
    @JsonProperty("currency")
    public String currency = "INR";


    @JsonProperty("value")
    public String getvalue() { return value; }

    @JsonProperty("value")
    public void setvalue(String value) { this.value = value; }

    @JsonProperty("currency")
    public String getcurrency() { return currency; }

    @JsonProperty("value")
    public void setcurrency(String currency) { this.currency = currency; }
}
