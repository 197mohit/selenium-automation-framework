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
        "mid",
        "name",
        "logo",
        "redirect",
        "callbackUrl",
        "hidePaytmBranding",
        "multipleWindowWebview"
})
public class Merchant implements Serializable
{

    @JsonProperty("mid")
    public String mid;
    @JsonProperty("name")
    public String name;
    @JsonProperty("logo")
    public String logo;
    @JsonProperty("redirect")
    public Boolean redirect;
    @JsonProperty("isTimerRequired")
    public Boolean isTimerRequired;
    @JsonProperty("cancelPendingOrder")
    public Boolean cancelPendingOrder;
    @JsonProperty("callbackUrl")
    public String callbackUrl;
    @JsonProperty("hidePaytmBranding")
    public Boolean hidePaytmBranding;
    @JsonProperty("multipleWindowWebview")
    public Boolean multipleWindowWebview;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();
    private final static long serialVersionUID = -74354510819496108L;

    public Merchant setMid(String mid) {
        this.mid = mid;
        return this;
    }

    public Merchant setName(String name) {
        this.name = name;
        return this;
    }

    public String getName() {
        return this.name;
    }


    public Merchant setLogo(String logo) {
        this.logo = logo;
        return this;
    }

    public Merchant setRedirect(Boolean redirect) {
        this.redirect = redirect;
        return this;
    }

    public Merchant setIsTimerRequired(Boolean isTimerRequired) {
        this.isTimerRequired = isTimerRequired;
        return this;
    }

    public Merchant setCancelPendingOrder(Boolean cancelPendingOrder) {
        this.cancelPendingOrder = cancelPendingOrder;
        return this;
    }

    public Merchant setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
        return this;
    }

    public Merchant setHidePaytmBranding(Boolean hidePaytmBranding) {
        this.hidePaytmBranding = hidePaytmBranding;
        return this;
    }

    public Merchant setmultipleWindowWebview(Boolean multipleWindowWebview) {
        this.multipleWindowWebview = multipleWindowWebview;
        return this;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public Merchant setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }
    
}
