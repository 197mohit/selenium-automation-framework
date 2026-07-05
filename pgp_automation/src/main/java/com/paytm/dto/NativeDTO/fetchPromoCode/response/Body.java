
package com.paytm.dto.NativeDTO.fetchPromoCode.response;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "resultInfo",
        "promoCodeDetail",
        "checkPromoValidityURL",
        "paymentModes",
        "nbBanks"
})
public class Body {

    @JsonProperty("resultInfo")
    private ResultInfo resultInfo;
    @JsonProperty("promoCodeDetail")
    private PromoCodeDetail promoCodeDetail;
    @JsonProperty("checkPromoValidityURL")
    private Object checkPromoValidityURL;
    @JsonProperty("paymentModes")
    private Object paymentModes;
    @JsonProperty("nbBanks")
    private Object nbBanks;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("resultInfo")
    public ResultInfo getResultInfo() {
        return resultInfo;
    }

    @JsonProperty("resultInfo")
    public void setResultInfo(ResultInfo resultInfo) {
        this.resultInfo = resultInfo;
    }

    @JsonProperty("promoCodeDetail")
    public PromoCodeDetail getPromoCodeDetail() {
        return promoCodeDetail;
    }

    @JsonProperty("promoCodeDetail")
    public void setPromoCodeDetail(PromoCodeDetail promoCodeDetail) {
        this.promoCodeDetail = promoCodeDetail;
    }

    @JsonProperty("checkPromoValidityURL")
    public Object getCheckPromoValidityURL() {
        return checkPromoValidityURL;
    }

    @JsonProperty("checkPromoValidityURL")
    public void setCheckPromoValidityURL(Object checkPromoValidityURL) {
        this.checkPromoValidityURL = checkPromoValidityURL;
    }

    @JsonProperty("paymentModes")
    public Object getPaymentModes() {
        return paymentModes;
    }

    @JsonProperty("paymentModes")
    public void setPaymentModes(Object paymentModes) {
        this.paymentModes = paymentModes;
    }

    @JsonProperty("nbBanks")
    public Object getNbBanks() {
        return nbBanks;
    }

    @JsonProperty("nbBanks")
    public void setNbBanks(Object nbBanks) {
        this.nbBanks = nbBanks;
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
