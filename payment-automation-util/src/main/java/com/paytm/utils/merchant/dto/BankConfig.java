package com.paytm.utils.merchant.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class BankConfig {

    @JsonProperty("BANK")
    private String bank;
    @JsonProperty("INDUSTRY")
    private String industry;
    @JsonProperty("PAY_MODE")
    private String[] payMode;
    @JsonProperty("CHANNEL")
    private String[] channel;
    @JsonProperty("MBID")
    private String mbid;
    @JsonProperty("AUTH")
    private String authMode;
    @JsonProperty("BANK_KEY")
    private String bankKey;
    @JsonProperty("PARAMS")
    private String params;
    @JsonProperty("ESCROW_FLAG")
    private Boolean escrow;
    @JsonProperty("ACTION")
    private String action;

    public String getBank() {
        return bank;
    }

    public BankConfig setBank(String bank) {
        this.bank = bank;
        return this;
    }

    public String getIndustry() {
        return industry;
    }

    public BankConfig setIndustry(String industry) {
        this.industry = industry;
        return this;
    }

    public String[] getPayMode() {
        return payMode;
    }

    public BankConfig setPayMode(String... payMode) {
        this.payMode = payMode;
        return this;
    }

    public String[] getChannel() {
        return channel;
    }

    public BankConfig setChannel(String... channel) {
        this.channel = channel;
        return this;
    }

    public String getMbid() {
        return mbid;
    }

    public BankConfig setMbid(String mbid) {
        this.mbid = mbid;
        return this;
    }

    public String getAuthMode() {
        return authMode;
    }

    public BankConfig setAuthMode(String authMode) {
        this.authMode = authMode;
        return this;
    }

    public String getBankKey() {
        return bankKey;
    }

    public BankConfig setBankKey(String bankKey) {
        this.bankKey = bankKey;
        return this;
    }

    public String getParams() {
        return params;
    }

    public BankConfig setParams(String params) {
        this.params = params;
        return this;
    }

    public Boolean getEscrow() {
        return escrow;
    }

    public BankConfig setEscrow(Boolean escrow) {
        this.escrow = escrow;
        return this;
    }

    public String getAction() {
        return action;
    }

    public BankConfig setAction(String action) {
        this.action = action;
        return this;
    }

}
