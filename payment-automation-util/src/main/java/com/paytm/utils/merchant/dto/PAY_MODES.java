package com.paytm.utils.merchant.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by rahulkumar on Apr,2018
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PAY_MODES {

   @JsonProperty("COMMISSION")
    private CommissionConfig commission;
   @JsonProperty("PAY_MODE")
    private String payMode;
   @JsonProperty("BANKS")
    private List<BankConfig> banks= new ArrayList<>();


    public CommissionConfig getCommission()
    {
        return commission;
    }

    public PAY_MODES setCommission(CommissionConfig commission)
    {
        this.commission = commission;
        return this;
    }

    public String getPayMode()
    {
        return payMode;
    }

    public PAY_MODES setPayMode(String payMode)
    {
        this.payMode = payMode;
        return this;
    }

    public List<BankConfig> getBanks()
    {
        return banks;
    }

    public PAY_MODES setBanks(BankConfig... banks)
    {
        this.banks = Arrays.asList(banks);
        return this;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [commission = "+ commission +", payMode = "+ payMode +", banks = "+ banks +"]";
    }
}

