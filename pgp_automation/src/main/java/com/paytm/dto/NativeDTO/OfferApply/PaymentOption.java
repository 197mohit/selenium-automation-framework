package com.paytm.dto.NativeDTO.OfferApply;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.dto.processTransactionV1.CardTokenInfo;
import java.util.List;
import com.paytm.dto.NativeDTO.OfferApply.Tenure;

public class PaymentOption {
    @JsonProperty("applyBankOffer")
    private boolean applyBankOffer;
    
    @JsonProperty("applySubvention")
    private boolean applySubvention;
    
    @JsonProperty("bankCode")
    private String bankCode;
    
    @JsonProperty("convFee")
    private double convFee;
    
    @JsonProperty("issuingBank")
    private String issuingBank;
    
    @JsonProperty("payMethod")
    private String payMethod;
    
    @JsonProperty("platformFee")
    private double platformFee;
    
    @JsonProperty("transactionAmount")
    private String transactionAmount;

    @JsonProperty("cardTokenInfo")
    private CardTokenInfo cardTokenInfo;

    @JsonProperty("subventionAmount")
    private String subventionAmount;
    
    @JsonProperty("boEligibleAmount")
    private String boEligibleAmount;
    
    @JsonProperty("savedCardId")
    private String savedCardId;
    
    @JsonProperty("vpa")
    private String vpa;
    
    @JsonProperty("cardNo")
    private String cardNo;
    
    @JsonProperty("tenure")
    private List<Tenure> tenure;

    public PaymentOption() {}

    public PaymentOption(boolean applyBankOffer, boolean applySubvention, 
                        String bankCode, double convFee, String issuingBank,
                        String payMethod, double platformFee, String transactionAmount,
                        String subventionAmount, String boEligibleAmount, 
                        String savedCardId, String vpa, String cardNo,
                        List<Tenure> tenure) {
        this.applyBankOffer = applyBankOffer;
        this.applySubvention = applySubvention;
        this.bankCode = bankCode;
        this.convFee = convFee;
        this.issuingBank = issuingBank;
        this.payMethod = payMethod;
        this.platformFee = platformFee;
        this.transactionAmount = transactionAmount;
        this.subventionAmount = subventionAmount;
        this.boEligibleAmount = boEligibleAmount;
        this.savedCardId = savedCardId;
        this.vpa = vpa;
        this.cardNo = cardNo;
        this.tenure = tenure;
    }

    public boolean isApplyBankOffer() {
        return applyBankOffer;
    }

    public void setApplyBankOffer(boolean applyBankOffer) {
        this.applyBankOffer = applyBankOffer;
    }

    public boolean isApplySubvention() {
        return applySubvention;
    }

    public void setApplySubvention(boolean applySubvention) {
        this.applySubvention = applySubvention;
    }

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public double getConvFee() {
        return convFee;
    }

    public void setConvFee(double convFee) {
        this.convFee = convFee;
    }

    public String getIssuingBank() {
        return issuingBank;
    }

    public void setIssuingBank(String issuingBank) {
        this.issuingBank = issuingBank;
    }

    public String getPayMethod() {
        return payMethod;
    }

    public void setPayMethod(String payMethod) {
        this.payMethod = payMethod;
    }

    public double getPlatformFee() {
        return platformFee;
    }

    public void setPlatformFee(double platformFee) {
        this.platformFee = platformFee;
    }

    public String getTransactionAmount() {
        return transactionAmount;
    }

    public void setTransactionAmount(String transactionAmount) {
        this.transactionAmount = transactionAmount;
    }

    public CardTokenInfo getCardTokenInfo() {
        return cardTokenInfo;
    }

    public PaymentOption setCardTokenInfo(CardTokenInfo cardTokenInfo) {
        this.cardTokenInfo = cardTokenInfo;
        return this;
    }

    public String getSubventionAmount() {
        return subventionAmount;
    }

    public void setSubventionAmount(String subventionAmount) {
        this.subventionAmount = subventionAmount;
    }

    public String getBoEligibleAmount() {
        return boEligibleAmount;
    }

    public void setBoEligibleAmount(String boEligibleAmount) {
        this.boEligibleAmount = boEligibleAmount;
    }

    public String getSavedCardId() {
        return savedCardId;
    }

    public void setSavedCardId(String savedCardId) {
        this.savedCardId = savedCardId;
    }

    public String getVpa() {
        return vpa;
    }

    public void setVpa(String vpa) {
        this.vpa = vpa;
    }

    public String getCardNo() {
        return cardNo;
    }

    public void setCardNo(String cardNo) {
        this.cardNo = cardNo;
    }

    public List<Tenure> getTenure() {
        return tenure;
    }

    public void setTenure(List<Tenure> tenure) {
        this.tenure = tenure;
    }
}