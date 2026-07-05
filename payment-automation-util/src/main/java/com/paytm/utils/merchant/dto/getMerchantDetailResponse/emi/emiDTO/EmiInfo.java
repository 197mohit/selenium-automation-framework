package com.paytm.utils.merchant.dto.getMerchantDetailResponse.emi.emiDTO;

public class EmiInfo {
    private String cardAcquiringMode;
    private double emiInterestRate;
    private int emiMonths;
    private String emiPlanId;
    private String emiTenureId;
    private EmiMinAmount emiMinAmount;
    private EmiMaxAmount emiMaxAmount;

    public String getCardAcquiringMode() {
        return cardAcquiringMode;
    }

    public double getEmiInterestRate() {
        return emiInterestRate;
    }

    public int getEmiMonths() {
        return emiMonths;
    }

    public String getEmiPlanId() {
        return emiPlanId;
    }

    public String getEmiTenureId() {
        return emiTenureId;
    }

    public EmiMinAmount getEmiMinAmount() {
        return emiMinAmount;
    }

    public EmiMaxAmount getEmiMaxAmount() {
        return emiMaxAmount;
    }

    public void setCardAcquiringMode(String cardAcquiringMode) {
        this.cardAcquiringMode = cardAcquiringMode;
    }

    public void setEmiInterestRate(double emiInterestRate) {
        this.emiInterestRate = emiInterestRate;
    }

    public void setEmiMonths(int emiMonths) {
        this.emiMonths = emiMonths;
    }

    public void setEmiPlanId(String emiPlanId) {
        this.emiPlanId = emiPlanId;
    }

    public void setEmiTenureId(String emiTenureId) {
        this.emiTenureId = emiTenureId;
    }

    public void setEmiMinAmount(EmiMinAmount emiMinAmount) {
        this.emiMinAmount = emiMinAmount;
    }

    public void setEmiMaxAmount(EmiMaxAmount emiMaxAmount) {
        this.emiMaxAmount = emiMaxAmount;
    }
}
