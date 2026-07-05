
package com.paytm.dto.NativeDTO.FetchCardDetailsDTO;

import com.fasterxml.jackson.annotation.*;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "mid",
        "cardNumber",
        "savedCardId",
        "isEightDigitBinRequired"
})
public class Body implements Serializable
{

    @JsonProperty("mid")
    private String mid;
    @JsonProperty("cardNumber")
    private String cardNumber;
    @JsonProperty("savedCardId")
    private String savedCardId;
    @JsonProperty("isEightDigitBinRequired")
    private Boolean isEightDigitBinRequired;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();
    private final static long serialVersionUID = 7416053253385020526L;

    @JsonProperty("mid")
    public String getMid() {
        return mid;
    }

    @JsonProperty("mid")
    public Body setMid(String mid) {
        this.mid = mid;
        return this;
    }

    @JsonProperty("cardNumber")
    public String getCardNumber() {
        return cardNumber;
    }

    @JsonProperty("cardNumber")
    public Body setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
        return this;
    }

    @JsonProperty("savedCardId")
    public String getSavedCardId() {
        return savedCardId;
    }

    @JsonProperty("savedCardId")
    public Body setSavedCardId(String savedCardId) {
        this.savedCardId = savedCardId;
        return this;
    }

    @JsonProperty("isEightDigitBinRequired")
    public Boolean getIsEightDigitBinRequired() {
        return isEightDigitBinRequired;
    }

    @JsonProperty("isEightDigitBinRequired")
    public Body setIsEightDigitBinRequired(Boolean isEightDigitBinRequired) {
        this.isEightDigitBinRequired = isEightDigitBinRequired;
        return this;
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
