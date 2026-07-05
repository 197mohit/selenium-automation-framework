package com.paytm.utils.merchant.dto.getMerchantDetailResponse.contract.contractDTO;

import java.util.List;

public class FeeItems {

    private String feeCalcBasis;
    private String chargeTarget;
    private String feeCalcMethod;
    private String feeSettleMode;
    private String feeRefundRule;
    private List<PayMethodFeeInfos> payMethodFeeInfos;
    private String chargeMode;
    private double taxRate;
    private String taxRateCode;
    private String chargeCurrency;
    private String roundingRule;
    private String chargeItemCode;
    private String accountRemark;
    private String taxCategory;
    private String taxPriceRelationship;
    private String taxRateInfos;
    private String pricingMethod;
    private String chargeTargetRules;


    public String getFeeCalcBasis() {
        return feeCalcBasis;
    }

    public void setFeeCalcBasis(String feeCalcBasis) {
        this.feeCalcBasis = feeCalcBasis;
    }

    public String getChargeTarget() {
        return chargeTarget;
    }

    public void setChargeTarget(String chargeTarget) {
        this.chargeTarget = chargeTarget;
    }

    public String getFeeCalcMethod() {
        return feeCalcMethod;
    }

    public void setFeeCalcMethod(String feeCalcMethod) {
        this.feeCalcMethod = feeCalcMethod;
    }

    public String getFeeSettleMode() {
        return feeSettleMode;
    }

    public void setFeeSettleMode(String feeSettleMode) {
        this.feeSettleMode = feeSettleMode;
    }

    public String getFeeRefundRule() {
        return feeRefundRule;
    }

    public void setFeeRefundRule(String feeRefundRule) {
        this.feeRefundRule = feeRefundRule;
    }

    public List<PayMethodFeeInfos> getPayMethodFeeInfos() {
        return payMethodFeeInfos;
    }

    public void setPayMethodFeeInfos(List<PayMethodFeeInfos> payMethodFeeInfos) {
        this.payMethodFeeInfos = payMethodFeeInfos;
    }

    public String getChargeMode() {
        return chargeMode;
    }

    public void setChargeMode(String chargeMode) {
        this.chargeMode = chargeMode;
    }

    public double getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(double taxRate) {
        this.taxRate = taxRate;
    }

    public String getTaxRateCode() {
        return taxRateCode;
    }

    public void setTaxRateCode(String taxRateCode) {
        this.taxRateCode = taxRateCode;
    }

    public String getChargeCurrency() {
        return chargeCurrency;
    }

    public void setChargeCurrency(String chargeCurrency) {
        this.chargeCurrency = chargeCurrency;
    }

    public String getRoundingRule() {
        return roundingRule;
    }

    public void setRoundingRule(String roundingRule) {
        this.roundingRule = roundingRule;
    }

    public String getChargeItemCode() {
        return chargeItemCode;
    }

    public void setChargeItemCode(String chargeItemCode) {
        this.chargeItemCode = chargeItemCode;
    }

    public String getAccountRemark() {
        return accountRemark;
    }

    public void setAccountRemark(String accountRemark) {
        this.accountRemark = accountRemark;
    }

    public String getTaxCategory() {
        return taxCategory;
    }

    public void setTaxCategory(String taxCategory) {
        this.taxCategory = taxCategory;
    }

    public String getTaxPriceRelationship() {
        return taxPriceRelationship;
    }

    public void setTaxPriceRelationship(String taxPriceRelationship) {
        this.taxPriceRelationship = taxPriceRelationship;
    }

    public String getTaxRateInfos() {
        return taxRateInfos;
    }

    public void setTaxRateInfos(String taxRateInfos) {
        this.taxRateInfos = taxRateInfos;
    }

    public String getPricingMethod() {
        return pricingMethod;
    }

    public void setPricingMethod(String pricingMethod) {
        this.pricingMethod = pricingMethod;
    }

    public String getChargeTargetRules() {
        return chargeTargetRules;
    }

    public void setChargeTargetRules(String chargeTargetRules) {
        this.chargeTargetRules = chargeTargetRules;
    }
}
