package com.paytm.dto.checkoutjs;
import java.io.Serializable;
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
        "bodyBackgroundColor",
        "bodyColor",
        "themeBackgroundColor",
        "themeColor",
        "headerBackgroundColor",
        "headerColor",
        "errorColor",
        "successColor",
        "card"
})
public class Style implements Serializable
{

    @JsonProperty("bodyBackgroundColor")
    public String bodyBackgroundColor;
    @JsonProperty("bodyColor")
    public String bodyColor;
    @JsonProperty("themeBackgroundColor")
    public String themeBackgroundColor;
    @JsonProperty("themeColor")
    public String themeColor;
    @JsonProperty("headerBackgroundColor")
    public String headerBackgroundColor;
    @JsonProperty("headerColor")
    public String headerColor;
    @JsonProperty("errorColor")
    public String errorColor;
    @JsonProperty("successColor")
    public String successColor;
    @JsonProperty("card")
    public Card card;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();
    private final static long serialVersionUID = -1711313455493626908L;


    public Style setBodyBackgroundColor(String bodyBackgroundColor) {
        this.bodyBackgroundColor = bodyBackgroundColor;
        return this;
    }

    public Style setBodyColor(String bodyColor) {
        this.bodyColor = bodyColor;
        return this;
    }

    public Style setThemeBackgroundColor(String themeBackgroundColor) {
        this.themeBackgroundColor = themeBackgroundColor;
        return this;
    }

    public Style setThemeColor(String themeColor) {
        this.themeColor = themeColor;
        return this;
    }

    public Style setHeaderBackgroundColor(String headerBackgroundColor) {
        this.headerBackgroundColor = headerBackgroundColor;
        return this;
    }

    public Style setHeaderColor(String headerColor) {
        this.headerColor = headerColor;
        return this;
    }

    public Style setErrorColor(String errorColor) {
        this.errorColor = errorColor;
        return this;
    }

    public Style setSuccessColor(String successColor) {
        this.successColor = successColor;
        return this;
    }

    public Style setCard(Card card) {
        this.card = card;
        return this;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public Style setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }
}
