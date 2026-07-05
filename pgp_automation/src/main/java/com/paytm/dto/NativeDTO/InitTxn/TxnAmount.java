
package com.paytm.dto.NativeDTO.InitTxn;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "value",
        "currency"
})
public class TxnAmount {

    @JsonProperty("value")
    private String value;
    @JsonProperty("currency")
    private String currency;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("value")
    public String getValue() {
        return value;
    }

    @JsonProperty("value")
    public TxnAmount setValue(String value) {
        this.value = value;
        return this;
    }

    @JsonProperty("currency")
    public String getCurrency() {
        return currency;
    }

    @JsonProperty("currency")
    public TxnAmount setCurrency(String currency) {
        this.currency = currency;
        return this;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public TxnAmount setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }
    public TxnAmount(String value) {
        this.value = value;
        this.currency = "INR";
    }

    public TxnAmount(String value,String currency) {
        this.value = value;
        this.currency = (currency != null && !currency.isEmpty()) ? currency : "INR";
    }

}
