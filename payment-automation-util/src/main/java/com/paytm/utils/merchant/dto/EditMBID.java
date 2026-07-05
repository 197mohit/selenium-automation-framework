package com.paytm.utils.merchant.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by sureshgupta on 15/11/17.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EditMBID {

    @JsonProperty("ID")
    private Long id;
    @JsonProperty("BANK")
    private String bank;
    @JsonProperty("INDUSTRY")
    private String industry;
    @JsonProperty("BANK_KEY")
    private String bankKey;
    @JsonProperty("PARAMS")
    private String params;

    public Long getId() {
        return id;
    }

    public EditMBID setId(Long id) {
        this.id = id;
        return this;
    }

    public String getBank() {
        return bank;
    }

    public EditMBID setBank(String bank) {
        this.bank = bank;
        return this;
    }

    public String getIndustry() {
        return industry;
    }

    public EditMBID setIndustry(String industry) {
        this.industry = industry;
        return this;
    }

    public String getBankKey() {
        return bankKey;
    }

    public EditMBID setBankKey(String bankKey) {
        this.bankKey = bankKey;
        return this;
    }

    public String getParams() {
        return params;
    }

    public EditMBID setParams(String params) {
        this.params = params;
        return this;
    }
}