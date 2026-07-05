package com.paytm.dto.saveCard.SavedcardOpenAPIServiceCardTypeSsoToken;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "cardType",
        "paytmSsoToken"
})
public class Body {

    @JsonProperty("cardType")
    private String cardType;
    @JsonProperty("paytmSsoToken")
    private String paytmSsoToken;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     *
     */
    public Body() {
    }

    /**
     *
     * @param paytmSsoToken
     * @param cardType
     */
    public Body(String cardType, String paytmSsoToken) {
        super();
        this.cardType = cardType;
        this.paytmSsoToken = paytmSsoToken;
    }

    @JsonProperty("cardType")
    public String getCardType() {
        return cardType;
    }

    @JsonProperty("cardType")
    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public Body withCardType(String cardType) {
        this.cardType = cardType;
        return this;
    }

    @JsonProperty("paytmSsoToken")
    public String getPaytmSsoToken() {
        return paytmSsoToken;
    }

    @JsonProperty("paytmSsoToken")
    public void setPaytmSsoToken(String paytmSsoToken) {
        this.paytmSsoToken = paytmSsoToken;
    }

    public Body withPaytmSsoToken(String paytmSsoToken) {
        this.paytmSsoToken = paytmSsoToken;
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

    public Body withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

}
