package com.paytm.utils.merchant.dto.getMerchantDetailResponse.contract.contractDTO;

public class FeeRanges {

    private double feeRate;
    private FixedFeeAmount fixedFeeAmount;
    private boolean includeLowerValue;
    private boolean includeUpperValue;
    private LowerFeeValue lowerFeeValue;
    private MaxFeeAmount maxFeeAmount;
    private MinFeeAmount minFeeAmount;
    private UpperFeeValue upperFeeValue;

    public double getFeeRate() {
        return feeRate;
    }

    public void setFeeRate(double feeRate) {
        this.feeRate = feeRate;
    }

    public FixedFeeAmount getFixedFeeAmount() {
        return fixedFeeAmount;
    }

    public void setFixedFeeAmount(FixedFeeAmount fixedFeeAmount) {
        this.fixedFeeAmount = fixedFeeAmount;
    }

    public boolean isIncludeLowerValue() {
        return includeLowerValue;
    }

    public void setIncludeLowerValue(boolean includeLowerValue) {
        this.includeLowerValue = includeLowerValue;
    }

    public boolean isIncludeUpperValue() {
        return includeUpperValue;
    }

    public void setIncludeUpperValue(boolean includeUpperValue) {
        this.includeUpperValue = includeUpperValue;
    }

    public LowerFeeValue getLowerFeeValue() {
        return lowerFeeValue;
    }

    public void setLowerFeeValue(LowerFeeValue lowerFeeValue) {
        this.lowerFeeValue = lowerFeeValue;
    }

    public MaxFeeAmount getMaxFeeAmount() {
        return maxFeeAmount;
    }

    public void setMaxFeeAmount(MaxFeeAmount maxFeeAmount) {
        this.maxFeeAmount = maxFeeAmount;
    }

    public MinFeeAmount getMinFeeAmount() {
        return minFeeAmount;
    }

    public void setMinFeeAmount(MinFeeAmount minFeeAmount) {
        this.minFeeAmount = minFeeAmount;
    }

    public UpperFeeValue getUpperFeeValue() {
        return upperFeeValue;
    }

    public void setUpperFeeValue(UpperFeeValue upperFeeValue) {
        this.upperFeeValue = upperFeeValue;
    }
}
