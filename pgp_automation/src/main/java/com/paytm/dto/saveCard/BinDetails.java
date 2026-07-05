package com.paytm.dto.saveCard;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.minidev.json.JSONObject;

/**
 * Created by anjukumari on 28/08/18
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BinDetails {

    private String id;
    private String bin;
    private String isIndian;
    private String bank;
    private String cardType;
    private String cardName;
    private String bankCode;
    private String instId;
    private Boolean iDebitEnabled;
    private Boolean cardEnabled;
    private Boolean ccDirectEnabled;
    private String displayBankName;
    @JsonProperty("isEmiPlanActive")
    private Boolean isEmiPlanActive;
    private Boolean active;
    private Boolean zeroSuccessRate;
    @JsonProperty("eligibleForAltId")
    private Boolean eligibleForAltId;
    private Boolean oneClickSupported;
    private String binInfoSource;
    private String prepaidCard;
    private String corporateCard;
    private String currency;
    private String country;
    private String symbol;
    private String countryCodeISO;
    private String binTokenization;
    private String category;
    private String issuingBankCardVariant;
    private String token;
    private JSONObject binAttributes;
    private String countryCode;

    public String getCountryCode() {
        return countryCode;
    }

    public BinDetails setCountryCode(String countryCode) {
        this.countryCode = countryCode;
        return this;
    }

    public JSONObject getBinAttributes() {
        return binAttributes;
    }

    public void setBinAttributes(JSONObject binAttributes) {
        this.binAttributes = binAttributes;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getToken() {
        return token;
    }

    public BinDetails setToken(String token) {
        this.token = token;
        return this;
    }

    private String isNativeOtpEligible;
    public String getIsNativeOtpEligible() {
        return isNativeOtpEligible;
    }

    public void setIsNativeOtpEligible(String isNativeOtpEligible) {
        this.isNativeOtpEligible = isNativeOtpEligible;
    }

    private String nativeOtpEligible;
    public String getNativeOtpEligible() {
        return nativeOtpEligible;
    }

    public void setNativeOtpEligible(String nativeOtpEligible) {
        this.nativeOtpEligible = nativeOtpEligible;
    }




    public String getIssuingBankCardVariant() {
        return issuingBankCardVariant;
    }

    public void setIssuingBankCardVariant(String issuingBankCardVariant) {
        this.issuingBankCardVariant = issuingBankCardVariant;
    }

    public String getBinTokenization() {
        return binTokenization;
    }

    public void setBinTokenization(String binTokenization) {
        this.binTokenization = binTokenization;
    }



    public String getCountryCodeISO() {
        return countryCodeISO;
    }

    public BinDetails setCountryCodeISO(String countryCodeISO) {
        this.countryCodeISO = countryCodeISO;
        return this;
    }

    public String getCurrencyPrecision() {
        return currencyPrecision;
    }

    public BinDetails setCurrencyPrecision(String currencyPrecision) {
        this.currencyPrecision = currencyPrecision;
        return this;
    }

    public String getCurrencyCodeISO() {
        return currencyCodeISO;
    }

    public BinDetails setCurrencyCodeISO(String currencyCodeISO) {
        this.currencyCodeISO = currencyCodeISO;
        return this;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public BinDetails setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
        return this;
    }

    private String currencyPrecision;
    private String currencyCodeISO;
    private String currencyCode;

    public String getSymbol() {
        return symbol;
    }

    public BinDetails setSymbol(String symbol) {
        this.symbol = symbol;
        return this;
    }

    public String getCountry() {
        return country;
    }

    public BinDetails setCountry(String country) {
        this.country = country;
        return this;
    }

    public String getCurrency() {
        return currency;
    }

    public BinDetails setCurrency(String currency) {
        this.currency = currency;
        return this;
    }

    public String getBinInfoSource() {
        return binInfoSource;
    }

    public void setBinInfoSource(String binInfoSource) {
        this.binInfoSource = binInfoSource;
    }

    public Boolean getZeroSuccessRate() {
        return zeroSuccessRate;
    }

    public BinDetails setZeroSuccessRate(Boolean zeroSuccessRate) {
        this.zeroSuccessRate = zeroSuccessRate;
        return this;
    }

    public Boolean getEligibleForAltId() {
        return eligibleForAltId;
    }

    public BinDetails setEligibleForAltId(Boolean eligibleForAltId) {
        this.eligibleForAltId = eligibleForAltId;
        return this;
    }


    public Boolean getEmiPlanActive() {
        return isEmiPlanActive;
    }

    public BinDetails setEmiPlanActive(Boolean emiPlanActive) {
        this.isEmiPlanActive = emiPlanActive;
        return this;
    }

    public String getId() {
        return id;
    }

    public BinDetails setId(String id) {
        this.id = id;
        return this;
    }

    public String getBin() {
        return bin;
    }

    public BinDetails setBin(String bin) {
        this.bin = bin;
        return this;
    }

    public String getIsIndian() {
        return isIndian;
    }

    public BinDetails setIsIndian(String isIndian) {
        this.isIndian = isIndian;
        return this;
    }

    public String getBank() {
        return bank;
    }

    public BinDetails setBank(String bank) {
        this.bank = bank;
        return this;
    }

    public String getCardType() {
        return cardType;
    }

    public BinDetails setCardType(String cardType) {
        this.cardType = cardType;
        return this;
    }

    public String getCardName() {
        return cardName;
    }

    public BinDetails setCardName(String cardName) {
        this.cardName = cardName;
        return this;
    }

    public String getBankCode() {
        return bankCode;
    }

    public BinDetails setBankCode(String bankCode) {
        this.bankCode = bankCode;
        return this;
    }

    public String getInstId() {
        return instId;
    }

    public BinDetails setInstId(String instId) {
        this.instId = instId;
        return this;
    }

    public Boolean getiDebitEnabled() {
        return iDebitEnabled;
    }

    public BinDetails setiDebitEnabled(Boolean iDebitEnabled) {
        this.iDebitEnabled = iDebitEnabled;
        return this;
    }

    public Boolean getCardEnabled() {
        return cardEnabled;
    }

    public BinDetails setCardEnabled(Boolean cardEnabled) {
        this.cardEnabled = cardEnabled;
        return this;
    }

    public Boolean getCcDirectEnabled() {
        return ccDirectEnabled;
    }

    public BinDetails setCcDirectEnabled(Boolean ccDirectEnabled) {
        this.ccDirectEnabled = ccDirectEnabled;
        return this;
    }

    public String getDisplayBankName() {
        return displayBankName;
    }

    public BinDetails setDisplayBankName(String displayBankName) {
        this.displayBankName = displayBankName;
        return this;
    }


    public Boolean getActive() {
        return active;
    }

    public BinDetails setActive(Boolean active) {
        this.active = active;
        return this;
    }

    public Boolean getOneClickSupported() {
        return oneClickSupported;
    }

    public BinDetails setOneClickSupported(Boolean oneClickSupported) {
        this.oneClickSupported = oneClickSupported;
        return this;
    }

    public String getPrepaidCard() {
        return prepaidCard;
    }

    public void setPrepaidCard(String prepaidCard) {
        this.prepaidCard = prepaidCard;
    }

    public String getCorporateCard() {
        return corporateCard;
    }

    public void setCorporateCard(String corporateCard) {
        this.corporateCard = corporateCard;
    }

}
