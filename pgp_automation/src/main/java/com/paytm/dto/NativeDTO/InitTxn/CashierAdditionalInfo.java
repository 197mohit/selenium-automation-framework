package com.paytm.dto.NativeDTO.InitTxn;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "priorityUpiAccount",
        "itineraryDisplayURL"
})

public class CashierAdditionalInfo {

    @JsonProperty("priorityUpiAccount")
    private String priorityUpiAccount;
    @JsonProperty("itineraryDisplayURL")
    private String itineraryDisplayURL;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("priorityUpiAccount")
    public String getPriorityUpiAccount() {
        return priorityUpiAccount;
    }

    @JsonProperty("priorityUpiAccount")
    public void setPriorityUpiAccount(String priorityUpiAccount) {
        this.priorityUpiAccount = priorityUpiAccount;
    }

    @JsonProperty("itineraryDisplayURL")
    public String getItineraryDisplayURL() {
        return itineraryDisplayURL;
    }

    @JsonProperty("itineraryDisplayURL")
    public void setItineraryDisplayURL(String itineraryDisplayURL) {
        this.itineraryDisplayURL = itineraryDisplayURL;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
