package com.paytm.utils.merchant.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by sureshgupta on 15/11/17.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VelocityDetails {
    private Integer maxAmtPerTxn;
    private Integer maxAmtPerDay;
    private Integer maxAmtPerWeek;
    private Integer maxAmtPerMonth;
    private Integer maxTxnPerDay;
    private Integer maxTxnPerWeek;
    private Integer maxTxnPerMonth;

    @JsonProperty("MAX_AMT_PER_TXN")
    public Integer getMaxAmtPerTxn() {
        return maxAmtPerTxn;
    }

    @JsonProperty("MAX_AMT_PER_DAY")
    public Integer getMaxAmtPerDay() {
        return maxAmtPerDay;
    }

    @JsonProperty("MAX_AMT_PER_WEEK")
    public Integer getMaxAmtPerWeek() {
        return maxAmtPerWeek;
    }

    @JsonProperty("MAX_AMT_PER_MONTH")
    public Integer getMaxAmtPerMonth() {
        return maxAmtPerMonth;
    }

    @JsonProperty("MAX_TXN_PER_DAY")
    public Integer getMaxTxnPerDay() {
        return maxTxnPerDay;
    }

    @JsonProperty("MAX_TXN_PER_WEEK")
    public Integer getMaxTxnPerWeek() {
        return maxTxnPerWeek;
    }

    @JsonProperty("MAX_TXN_PER_MONTH")
    public Integer getMaxTxnPerMonth() {
        return maxTxnPerMonth;
    }

    public VelocityDetails setMaxAmtPerTxn(Integer maxAmtPerTxn) {
        this.maxAmtPerTxn = maxAmtPerTxn;
        return this;
    }

    public VelocityDetails setMaxAmtPerDay(Integer maxAmtPerDay) {
        this.maxAmtPerDay = maxAmtPerDay;
        return this;
    }

    public VelocityDetails setMaxAmtPerWeek(Integer maxAmtPerWeek) {
        this.maxAmtPerWeek = maxAmtPerWeek;
        return this;
    }

    public VelocityDetails setMaxAmtPerMonth(Integer maxAmtPerMonth) {
        this.maxAmtPerMonth = maxAmtPerMonth;
        return this;
    }

    public VelocityDetails setMaxTxnPerDay(Integer maxTxnPerDay) {
        this.maxTxnPerDay = maxTxnPerDay;
        return this;
    }

    public VelocityDetails setMaxTxnPerWeek(Integer maxTxnPerWeek) {
        this.maxTxnPerWeek = maxTxnPerWeek;
        return this;
    }

    public VelocityDetails setMaxTxnPerMonth(Integer maxTxnPerMonth) {
        this.maxTxnPerMonth = maxTxnPerMonth;
        return this;
    }
}
