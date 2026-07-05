package com.paytm.utils.merchant.dto.getMerchantDetailResponse.contract.contractDTO;

public class FixedFeeAmount {

    private String amount;
    private String currency;
    private String value;
    private String amountInRs;

    public String getAmountInRs() {
        return amountInRs;
    }

    public void setAmountInRs(String amountInRs) {
        this.amountInRs = amountInRs;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
