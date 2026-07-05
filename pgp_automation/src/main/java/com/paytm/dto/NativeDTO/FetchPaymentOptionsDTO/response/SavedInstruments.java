package com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Created by ankuragarwal on 16/10/18
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SavedInstruments {
    private IsDisabled isDisabled;
    private HasLowSuccess hasLowSuccess;
    private String iconUrl;
    private String bankLogoUrl;
    private String oneClickSupported;
    private String corporateCard;
    private CardDetails cardDetails;
    private String issuingBank;
    private String emiDetails;
    private List<String> authModes;
    private String channelCode;
    private String channelName;
    private String isEligibleForCoft;
    private String isCardCoft;
    private String isCoftPaymentSupported;

    public IsDisabled getIsDisabled() {
        return isDisabled;
    }

    public HasLowSuccess getHasLowSuccess() {
        return hasLowSuccess;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public CardDetails getCardDetails() {
        return cardDetails;
    }

    public String getIssuingBank() {
        return issuingBank;
    }

    public String getEmiDetails() {
        return emiDetails;
    }

    public List<String> getAuthModes() {
        return authModes;
    }

    public String getChannelCode() {
        return channelCode;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setIsDisabled(IsDisabled isDisabled) {
        this.isDisabled = isDisabled;
    }

    public void setHasLowSuccess(HasLowSuccess hasLowSuccess) {
        this.hasLowSuccess = hasLowSuccess;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public void setCardDetails(CardDetails cardDetails) {
        this.cardDetails = cardDetails;
    }

    public void setIssuingBank(String issuingBank) {
        this.issuingBank = issuingBank;
    }

    public void setEmiDetails(String emiDetails) {
        this.emiDetails = emiDetails;
    }

    public void setAuthModes(List<String> authModes) {
        this.authModes = authModes;
    }

    public void setChannelCode(String channelCode) {
        this.channelCode = channelCode;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getBankLogoUrl() {
        return bankLogoUrl;
    }

    public void setBankLogoUrl(String bankLogoUrl) {
        this.bankLogoUrl = bankLogoUrl;
    }

    public String getOneClickSupported() {
        return oneClickSupported;
    }

    public void setOneClickSupported(String oneClickSupported) {
        this.oneClickSupported = oneClickSupported;
    }

    public String getCorporateCard() {
        return corporateCard;
    }

    public void setCorporateCard(String corporateCard) {
        this.corporateCard = corporateCard;
    }

    public String getIsEligibleForCoft() {
        return isEligibleForCoft;
    }

    public void setIsEligibleForCoft(String isEligibleForCoft) {
        this.isEligibleForCoft = isEligibleForCoft;
    }

    public String getIsCardCoft() {
        return isCardCoft;
    }

    public void setIsCardCoft(String isCardCoft) {
        this.isCardCoft = isCardCoft;
    }

    public String getIsCoftPaymentSupported() {
        return isCoftPaymentSupported;
    }

    public void setIsCoftPaymentSupported(String isCoftPaymentSupported) {
        this.isCoftPaymentSupported = isCoftPaymentSupported;
    }
}
