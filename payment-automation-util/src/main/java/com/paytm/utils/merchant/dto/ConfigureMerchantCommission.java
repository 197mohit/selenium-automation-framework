package com.paytm.utils.merchant.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class ConfigureMerchantCommission {

    @JsonProperty("MID")
    private String mid;
    @JsonProperty("ACTION")
    private String action;
    @JsonProperty("INDUSTRY")
    private String industry;
    @JsonProperty("PAY_OPTION")
    private String payMode;
    @JsonProperty("DC_ONUS_OFFUS")
    private String dcOnusOffus;
    @JsonProperty("CC_ONUS_OFFUS")
    private String ccOnusOffus;
    @JsonProperty("CARD_CATEGORY")
    private String cardCategory;
    @JsonProperty("COMMISSION")
    private CommissionConfig commission;

    public String getMid() {
        return mid;
    }

    public ConfigureMerchantCommission setMid(String mid) {
        this.mid = mid;
        return this;
    }

    public String getAction() {
        return action;
    }

    public ConfigureMerchantCommission setAction(String action) {
        this.action = action;
        return this;
    }

    public String getIndustry() {
        return this.industry;
    }

    public ConfigureMerchantCommission setIndustry(String industry) {
        this.industry = industry;
        return this;
    }

    public String getPayMode() {
        return this.payMode;
    }

    public ConfigureMerchantCommission setPayMode(String payMode) {
        this.payMode = payMode;
        return this;
    }

    public String getDcOnusOffus() {
        return this.dcOnusOffus;
    }

    public ConfigureMerchantCommission setDcOnusOffus(String dcOnusOffus) {
        this.dcOnusOffus = dcOnusOffus;
        return this;
    }

    public String getCcOnusOffus() {
        return this.ccOnusOffus;
    }

    public ConfigureMerchantCommission setCcOnusOffus(String ccOnusOffus) {
        this.ccOnusOffus = ccOnusOffus;
        return this;
    }

    public String getCardCategory() {
        return this.cardCategory;
    }

    public ConfigureMerchantCommission setCardCategory(String cardCategory) {
        this.cardCategory = cardCategory;
        return this;
    }

    public CommissionConfig getCommission() {
        return commission;
    }

    public ConfigureMerchantCommission setCommission(CommissionConfig commission) {
        this.commission = commission;
        return this;
    }


}
