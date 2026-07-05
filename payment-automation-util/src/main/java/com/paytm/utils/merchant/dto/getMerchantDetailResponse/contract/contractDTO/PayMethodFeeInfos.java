package com.paytm.utils.merchant.dto.getMerchantDetailResponse.contract.contractDTO;

import java.util.List;

public class PayMethodFeeInfos {

    private String payMethod;
    private String feeRateFactors;
    private List<FeeRanges> feeRanges;
    private String invoiceType;

    public String getInvoiceType() { return invoiceType; }

    public void setInvoiceType(String invoiceType) { this.invoiceType = invoiceType; }

    public String getPayMethod() {
        return payMethod;
    }

    public void setPayMethod(String payMethod) {
        this.payMethod = payMethod;
    }

    public String getFeeRateFactors() {
        return feeRateFactors;
    }

    public void setFeeRateFactors(String feeRateFactors) {
        this.feeRateFactors = feeRateFactors;
    }

    public List<FeeRanges> getFeeRanges() {
        return feeRanges;
    }

    public void setFeeRanges(List<FeeRanges> feeRanges) {
        this.feeRanges = feeRanges;
    }
}
