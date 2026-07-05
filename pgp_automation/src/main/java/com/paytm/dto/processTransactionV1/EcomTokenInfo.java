package com.paytm.dto.processTransactionV1;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "ecomToken",
        "expiryMonth",
        "expiryYear",
        "authenticationValue",
        "firstSixDigits",
        "lastFourDigits",
        "issuingBank",
        "networkTokenRequestorId"
})
public class EcomTokenInfo {

    @JsonProperty("ecomToken")
    private String ecomToken;
    @JsonProperty("expiryMonth")
    private String expiryMonth;
    @JsonProperty("expiryYear")
    private String expiryYear;
    @JsonProperty("authenticationValue")
    private String authenticationValue;
    @JsonProperty("firstSixDigits")
    private String firstSixDigits;
    @JsonProperty("lastFourDigits")
    private String lastFourDigits;
    @JsonProperty("issuingBank")
    private String issuingBank;
    @JsonProperty("networkTokenRequestorId")
    private String networkTokenRequestorId;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("ecomToken")
    public String getEcomToken() {
        return ecomToken;
    }

    @JsonProperty("ecomToken")
    public EcomTokenInfo setEcomToken(String ecomToken) {
        this.ecomToken = ecomToken;
        return this;
    }

    @JsonProperty("expiryMonth")
    public String getExpiryMonth() {
        return expiryMonth;
    }

    @JsonProperty("expiryMonth")
    public EcomTokenInfo setExpiryMonth(String expiryMonth) {
        this.expiryMonth = expiryMonth;
        return this;
    }

    @JsonProperty("expiryYear")
    public String getExpiryYear() {
        return expiryYear;
    }

    @JsonProperty("expiryYear")
    public EcomTokenInfo setExpiryYear(String expiryYear) {
        this.expiryYear = expiryYear;
        return this;
    }

    @JsonProperty("authenticationValue")
    public String getAuthenticationValue() {
        return authenticationValue;
    }

    @JsonProperty("authenticationValue")
    public EcomTokenInfo setAuthenticationValue(String authenticationValue) {
        this.authenticationValue = authenticationValue;
        return this;
    }

    @JsonProperty("firstSixDigits")
    public String getFirstSixDigits() {
        return firstSixDigits;
    }

    @JsonProperty("firstSixDigits")
    public EcomTokenInfo setFirstSixDigits(String firstSixDigits) {
        this.firstSixDigits = firstSixDigits;
        return this;
    }

    @JsonProperty("lastFourDigits")
    public String getLastFourDigits() {
        return lastFourDigits;
    }

    @JsonProperty("lastFourDigits")
    public EcomTokenInfo setLastFourDigits(String lastFourDigits) {
        this.lastFourDigits = lastFourDigits;
        return this;
    }

    @JsonProperty("issuingBank")
    public String getIssuingBank() {
        return issuingBank;
    }

    @JsonProperty("issuingBank")
    public EcomTokenInfo setIssuingBank(String issuingBank) {
        this.issuingBank = issuingBank;
        return this;
    }

    @JsonProperty("networkTokenRequestorId")
    public String getNetworkTokenRequestorId() {
        return networkTokenRequestorId;
    }

    @JsonProperty("networkTokenRequestorId")
    public EcomTokenInfo setNetworkTokenRequestorId(String networkTokenRequestorId) {
        this.networkTokenRequestorId = networkTokenRequestorId;
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