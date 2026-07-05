
package com.paytm.dto.NativeDTO.fetchPromoCode.response;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "promocodeId",
        "promoCode",
        "paymentModes",
        "cardBins",
        "nbBanks",
        "merchants",
        "startDate",
        "endDate",
        "promocodeType",
        "promocodeTypeName",
        "promoMsg",
        "promoErrorMsg",
        "promoCardType",
        "savedCardDate",
        "txnLimitMap",
        "maxCount",
        "maxAmount",
        "minAmount",
        "promoOnSavedCard",
        "validatePromoWithWallet",
        "cardLimit",
        "custLimit"
})
public class PromoCodeDetail {

    @JsonProperty("promocodeId")
    private Integer promocodeId;
    @JsonProperty("promoCode")
    private String promoCode;
    @JsonProperty("paymentModes")
    private List<String> paymentModes = null;
    @JsonProperty("nbBanks")
    private List<Object> nbBanks = null;
    @JsonProperty("merchants")
    private List<Integer> merchants = null;
    @JsonProperty("startDate")
    private String startDate;
    @JsonProperty("endDate")
    private String endDate;
    @JsonProperty("promocodeType")
    private Integer promocodeType;
    @JsonProperty("promocodeTypeName")
    private String promocodeTypeName;
    @JsonProperty("promoMsg")
    private String promoMsg;
    @JsonProperty("promoErrorMsg")
    private String promoErrorMsg;
    @JsonProperty("promoCardType")
    private List<Object> promoCardType = null;
    @JsonProperty("savedCardDate")
    private String savedCardDate;
    @JsonProperty("maxCount")
    private Integer maxCount;
    @JsonProperty("maxAmount")
    private Integer maxAmount;
    @JsonProperty("minAmount")
    private Integer minAmount;
    @JsonProperty("promoOnSavedCard")
    private Boolean promoOnSavedCard;
    @JsonProperty("validatePromoWithWallet")
    private Boolean validatePromoWithWallet;
    @JsonProperty("cardLimit")
    private Boolean cardLimit;
    @JsonProperty("custLimit")
    private Boolean custLimit;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("promocodeId")
    public Integer getPromocodeId() {
        return promocodeId;
    }

    @JsonProperty("promocodeId")
    public void setPromocodeId(Integer promocodeId) {
        this.promocodeId = promocodeId;
    }

    @JsonProperty("promoCode")
    public String getPromoCode() {
        return promoCode;
    }

    @JsonProperty("promoCode")
    public void setPromoCode(String promoCode) {
        this.promoCode = promoCode;
    }

    @JsonProperty("paymentModes")
    public List<String> getPaymentModes() {
        return paymentModes;
    }

    @JsonProperty("paymentModes")
    public void setPaymentModes(List<String> paymentModes) {
        this.paymentModes = paymentModes;
    }

    @JsonProperty("nbBanks")
    public List<Object> getNbBanks() {
        return nbBanks;
    }

    @JsonProperty("nbBanks")
    public void setNbBanks(List<Object> nbBanks) {
        this.nbBanks = nbBanks;
    }

    @JsonProperty("merchants")
    public List<Integer> getMerchants() {
        return merchants;
    }

    @JsonProperty("merchants")
    public void setMerchants(List<Integer> merchants) {
        this.merchants = merchants;
    }

    @JsonProperty("startDate")
    public String getStartDate() {
        return startDate;
    }

    @JsonProperty("startDate")
    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    @JsonProperty("endDate")
    public String getEndDate() {
        return endDate;
    }

    @JsonProperty("endDate")
    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    @JsonProperty("promocodeType")
    public Integer getPromocodeType() {
        return promocodeType;
    }

    @JsonProperty("promocodeType")
    public void setPromocodeType(Integer promocodeType) {
        this.promocodeType = promocodeType;
    }

    @JsonProperty("promocodeTypeName")
    public String getPromocodeTypeName() {
        return promocodeTypeName;
    }

    @JsonProperty("promocodeTypeName")
    public void setPromocodeTypeName(String promocodeTypeName) {
        this.promocodeTypeName = promocodeTypeName;
    }

    @JsonProperty("promoMsg")
    public String getPromoMsg() {
        return promoMsg;
    }

    @JsonProperty("promoMsg")
    public void setPromoMsg(String promoMsg) {
        this.promoMsg = promoMsg;
    }

    @JsonProperty("promoErrorMsg")
    public String getPromoErrorMsg() {
        return promoErrorMsg;
    }

    @JsonProperty("promoErrorMsg")
    public void setPromoErrorMsg(String promoErrorMsg) {
        this.promoErrorMsg = promoErrorMsg;
    }

    @JsonProperty("promoCardType")
    public List<Object> getPromoCardType() {
        return promoCardType;
    }

    @JsonProperty("promoCardType")
    public void setPromoCardType(List<Object> promoCardType) {
        this.promoCardType = promoCardType;
    }

    @JsonProperty("savedCardDate")
    public String getSavedCardDate() {
        return savedCardDate;
    }

    @JsonProperty("savedCardDate")
    public void setSavedCardDate(String savedCardDate) {
        this.savedCardDate = savedCardDate;
    }

    @JsonProperty("maxCount")
    public Integer getMaxCount() {
        return maxCount;
    }

    @JsonProperty("maxCount")
    public void setMaxCount(Integer maxCount) {
        this.maxCount = maxCount;
    }

    @JsonProperty("maxAmount")
    public Integer getMaxAmount() {
        return maxAmount;
    }

    @JsonProperty("maxAmount")
    public void setMaxAmount(Integer maxAmount) {
        this.maxAmount = maxAmount;
    }

    @JsonProperty("minAmount")
    public Integer getMinAmount() {
        return minAmount;
    }

    @JsonProperty("minAmount")
    public void setMinAmount(Integer minAmount) {
        this.minAmount = minAmount;
    }

    @JsonProperty("promoOnSavedCard")
    public Boolean getPromoOnSavedCard() {
        return promoOnSavedCard;
    }

    @JsonProperty("promoOnSavedCard")
    public void setPromoOnSavedCard(Boolean promoOnSavedCard) {
        this.promoOnSavedCard = promoOnSavedCard;
    }

    @JsonProperty("validatePromoWithWallet")
    public Boolean getValidatePromoWithWallet() {
        return validatePromoWithWallet;
    }

    @JsonProperty("validatePromoWithWallet")
    public void setValidatePromoWithWallet(Boolean validatePromoWithWallet) {
        this.validatePromoWithWallet = validatePromoWithWallet;
    }

    @JsonProperty("cardLimit")
    public Boolean getCardLimit() {
        return cardLimit;
    }

    @JsonProperty("cardLimit")
    public void setCardLimit(Boolean cardLimit) {
        this.cardLimit = cardLimit;
    }

    @JsonProperty("custLimit")
    public Boolean getCustLimit() {
        return custLimit;
    }

    @JsonProperty("custLimit")
    public void setCustLimit(Boolean custLimit) {
        this.custLimit = custLimit;
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
