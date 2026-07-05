package com.paytm.utils.merchant.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommissionConfig {

    @JsonProperty("PERCENT_COMMISSION")
    private Double percentCommission;
    @JsonProperty("FLAT_COMMISSION")
    private Double flatCommission;
    @JsonProperty("COMMISSION_TYPE_BOTH")
    private Boolean commissionTypeBoth;
    @JsonProperty("SLAB_1_START_RANGE")
    private Double slab1StartRange;
    @JsonProperty("SLAB_1_END_RANGE")
    private Double slab1EndRange;
    @JsonProperty("SLAB_2_START_RANGE")
    private Double slab2StartRange;
    @JsonProperty("SLAB_2_END_RANGE")
    private Double slab2EndRange;
    @JsonProperty("SLAB_3_START_RANGE")
    private Double slab3StartRange;
    @JsonProperty("SLAB_3_END_RANGE")
    private Double slab3EndRange;
    @JsonProperty("SLAB_1_COMMISSION_TYPE_BOTH")
    private Boolean slab1CommissionTypeBoth;
    @JsonProperty("SLAB_2_COMMISSION_TYPE_BOTH")
    private Boolean slab2CommissionTypeBoth;
    @JsonProperty("SLAB_3_COMMISSION_TYPE_BOTH")
    private Boolean slab3CommissionTypeBoth;
    @JsonProperty("SLAB_1_PERCENT_COMMISSION")
    private Double slab1PercentCommission;
    @JsonProperty("SLAB_1_FLAT_COMMISSION")
    private Double slab1FlatCommission;
    @JsonProperty("SLAB_2_PERCENT_COMMISSION")
    private Double slab2PercentCommission;
    @JsonProperty("SLAB_2_FLAT_COMMISSION")
    private Double slab2FlatCommission;
    @JsonProperty("SLAB_3_PERCENT_COMMISSION")
    private Double slab3PercentCommission;
    @JsonProperty("SLAB_3_FLAT_COMMISSION")
    private Double slab3FlatCommission;
    @JsonProperty("FEE_TYPE")
    private String feeType;

    public Double getPercentCommission() {
        return percentCommission;
    }

    public Double getFlatCommission() {
        return flatCommission;
    }


    public Boolean isCommissionTypeBoth() {
        return commissionTypeBoth;
    }

    public Double getSlab1StartRange() {
        return slab1StartRange;
    }


    public Double getSlab1EndRange() {
        return slab1EndRange;
    }


    public Double getSlab2StartRange() {
        return slab2StartRange;
    }


    public Double getSlab2EndRange() {
        return slab2EndRange;
    }


    public Double getSlab3StartRange() {
        return slab3StartRange;
    }


    public Double getSlab3EndRange() {
        return slab3EndRange;
    }


    public Boolean isSlab1CommissionTypeBoth() {
        return slab1CommissionTypeBoth;
    }


    public Boolean isSlab2CommissionTypeBoth() {
        return slab2CommissionTypeBoth;
    }


    public Boolean isSlab3CommissionTypeBoth() {
        return slab3CommissionTypeBoth;
    }


    public Double getSlab1PercentCommission() {
        return slab1PercentCommission;
    }


    public Double getSlab1FlatCommission() {
        return slab1FlatCommission;
    }


    public Double getSlab2PercentCommission() {
        return slab2PercentCommission;
    }

    public Double getSlab2FlatCommission() {
        return slab2FlatCommission;
    }

    public Double getSlab3PercentCommission() {
        return slab3PercentCommission;
    }

    public Double getSlab3FlatCommission() {
        return slab3FlatCommission;
    }

    public String getFeeType() {
        return feeType;
    }

    public CommissionConfig setPercentCommission(Double percentCommission) {
        this.percentCommission = percentCommission;
        return this;
    }

    public CommissionConfig setFlatCommission(Double flatCommission) {
        this.flatCommission = flatCommission;
        return this;
    }

    public CommissionConfig setCommissionTypeBoth(Boolean commissionTypeBoth) {
        this.commissionTypeBoth = commissionTypeBoth;
        return this;
    }

    public CommissionConfig setSlab1StartRange(Double slab1StartRange) {
        this.slab1StartRange = slab1StartRange;
        return this;
    }

    public CommissionConfig setSlab1EndRange(Double slab1EndRange) {
        this.slab1EndRange = slab1EndRange;
        return this;
    }

    public CommissionConfig setSlab2StartRange(Double slab2StartRange) {
        this.slab2StartRange = slab2StartRange;
        return this;
    }

    public CommissionConfig setSlab2EndRange(Double slab2EndRange) {
        this.slab2EndRange = slab2EndRange;
        return this;
    }

    public CommissionConfig setSlab3StartRange(Double slab3StartRange) {
        this.slab3StartRange = slab3StartRange;
        return this;
    }

    public CommissionConfig setSlab3EndRange(Double slab3EndRange) {
        this.slab3EndRange = slab3EndRange;
        return this;
    }

    public CommissionConfig setSlab1CommissionTypeBoth(Boolean slab1CommissionTypeBoth) {
        this.slab1CommissionTypeBoth = slab1CommissionTypeBoth;
        return this;
    }

    public CommissionConfig setSlab2CommissionTypeBoth(Boolean slab2CommissionTypeBoth) {
        this.slab2CommissionTypeBoth = slab2CommissionTypeBoth;
        return this;
    }

    public CommissionConfig setSlab3CommissionTypeBoth(Boolean slab3CommissionTypeBoth) {
        this.slab3CommissionTypeBoth = slab3CommissionTypeBoth;
        return this;
    }

    public CommissionConfig setSlab1PercentCommission(Double slab1PercentCommission) {
        this.slab1PercentCommission = slab1PercentCommission;
        return this;
    }

    public CommissionConfig setSlab1FlatCommission(Double slab1FlatCommission) {
        this.slab1FlatCommission = slab1FlatCommission;
        return this;
    }

    public CommissionConfig setSlab2PercentCommission(Double slab2PercentCommission) {
        this.slab2PercentCommission = slab2PercentCommission;
        return this;
    }

    public CommissionConfig setSlab2FlatCommission(Double slab2FlatCommission) {
        this.slab2FlatCommission = slab2FlatCommission;
        return this;
    }

    public CommissionConfig setSlab3PercentCommission(Double slab3PercentCommission) {
        this.slab3PercentCommission = slab3PercentCommission;
        return this;
    }

    public CommissionConfig setSlab3FlatCommission(Double slab3FlatCommission) {
        this.slab3FlatCommission = slab3FlatCommission;
        return this;
    }

    public CommissionConfig setFeeType(String feeType) {
        this.feeType = feeType;
        return this;
    }
}