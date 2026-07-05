package com.paytm.dto.emiSubvention.ApiV1Validate.request;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONObject;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "totalTransactionAmount",
        "cardNumber",
        "cardBin"
})
public class PaymentDetails implements Serializable {

    @JsonProperty("totalTransactionAmount")
    private String totalTransactionAmount;
    @JsonProperty("cardNumber")
    private String cardNumber;
    @JsonProperty("cardBin")
    private String cardBin;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();
    private final static long serialVersionUID = -3213606331572130393L;

    @JsonProperty("cardTokenInfo")
    private JSONObject cardTokenInfo;

    @JsonProperty("totalTransactionAmount")
    public String getTotalTransactionAmount() {
        return totalTransactionAmount;
    }

    @JsonProperty("totalTransactionAmount")
    public PaymentDetails setTotalTransactionAmount(String totalTransactionAmount) {
        this.totalTransactionAmount = totalTransactionAmount;
        return this;
    }

    @JsonProperty("cardNumber")
    public String getCardNumber() {
        return cardNumber;
    }

    @JsonProperty("cardNumber")
    public PaymentDetails setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
        return this;
    }

    @JsonProperty("cardBin")
    public String getCardBin() {
        return cardBin;
    }

    @JsonProperty("cardBin")
    public PaymentDetails setCardBin(String cardBin) {
        this.cardBin = cardBin;
        return this;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public PaymentDetails setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

    @JsonProperty("cardTokenInfo")
    public JSONObject getCardTokenInfo() {
        return cardTokenInfo;
    }

    @JsonProperty("cardTokenInfo")
    public PaymentDetails setCardTokenInfo(JSONObject cardTokenInfo) {
        this.cardTokenInfo = cardTokenInfo;
        return this;
    }

    @Override
    public String toString() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return "";
    }

}