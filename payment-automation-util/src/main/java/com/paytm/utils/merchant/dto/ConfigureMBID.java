package com.paytm.utils.merchant.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.Arrays;
import java.util.List;

/**
 * Created by deepakkumar on 24/10/17.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"ACTION", "MBID_DETAILS"})
public class ConfigureMBID {

    @JsonProperty("BANKS")
    private List<BankConfig> bank;

    public List<BankConfig> getBank() {
        return bank;
    }

    public ConfigureMBID setBank(BankConfig[] bank) {
        this.bank = Arrays.asList(bank);
        return this;
    }

    public ConfigureMBID setBank(List<BankConfig> bank) {
        this.bank = bank;
        return this;
    }
}
