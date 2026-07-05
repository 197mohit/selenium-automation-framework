package com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AddMoneyMerchantDetails {

    private String mcc;
    private String merchantVpa;
    private String merchantName;

    public String getMcc() {
        return mcc;
    }

    public AddMoneyMerchantDetails setMcc(String mcc) {
        this.mcc = mcc;
        return this;
    }

    public String getMerchantVpa() {
        return merchantVpa;
    }

    public AddMoneyMerchantDetails setMerchantVpa(String merchantVpa) {
        this.merchantVpa = merchantVpa;
        return this;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public AddMoneyMerchantDetails setMerchantName(String merchantName) {
        this.merchantName = merchantName;
        return this;
    }
}
