
package com.paytm.dto.NativeDTO.fetchPromoCode;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "cardNumber",
        "txnType"
})
public class Body {

    @JsonProperty("cardNumber")
    private String cardNumber;
    @JsonProperty("txnType")
    private String txnType;
    @JsonProperty("bankCode")
    private String bankCode;
    @JsonProperty("isEnhancedFlow")
    private String isEnhancedFlow;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    Body(String txnType, String cardNumber, String bankCode, String isEnhancedFlow) {
        this.txnType = txnType;
        this.cardNumber = cardNumber;
        this.bankCode = bankCode;
        this.isEnhancedFlow = isEnhancedFlow;
    }

    @JsonProperty("cardNumber")
    public String getCardNumber() {
        return cardNumber;
    }

    @JsonProperty("cardNumber")
    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    @JsonProperty("txnType")
    public String getTxnType() {
        return txnType;
    }

    @JsonProperty("txnType")
    public void setTxnType(String txnType) {
        this.txnType = txnType;
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
