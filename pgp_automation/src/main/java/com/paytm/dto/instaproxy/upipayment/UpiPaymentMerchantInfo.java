package com.paytm.dto.instaproxy.upipayment;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

// DTO: Instaproxy UPI payment request — merchantInfo
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpiPaymentMerchantInfo {

    @JsonProperty("merchantId")
    private String merchantId;
    @JsonProperty("merchantName")
    private String merchantName;
    @JsonProperty("merchantMcc")
    private String merchantMcc;
    @JsonProperty("merchantTransId")
    private String merchantTransId;
    @JsonProperty("merchantVPA")
    private String merchantVPA;
    @JsonProperty("acquiringMCC")
    private String acquiringMCC;

    public UpiPaymentMerchantInfo() {
    }

    private UpiPaymentMerchantInfo(Builder b) {
        this.merchantId = b.merchantId;
        this.merchantName = b.merchantName;
        this.merchantMcc = b.merchantMcc;
        this.merchantTransId = b.merchantTransId;
        this.merchantVPA = b.merchantVPA;
        this.acquiringMCC = b.acquiringMCC;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public String getMerchantMcc() {
        return merchantMcc;
    }

    public void setMerchantMcc(String merchantMcc) {
        this.merchantMcc = merchantMcc;
    }

    public String getMerchantTransId() {
        return merchantTransId;
    }

    public void setMerchantTransId(String merchantTransId) {
        this.merchantTransId = merchantTransId;
    }

    public String getMerchantVPA() {
        return merchantVPA;
    }

    public void setMerchantVPA(String merchantVPA) {
        this.merchantVPA = merchantVPA;
    }

    public String getAcquiringMCC() {
        return acquiringMCC;
    }

    public void setAcquiringMCC(String acquiringMCC) {
        this.acquiringMCC = acquiringMCC;
    }

    public static class Builder {
        private String merchantId = "qa12id40010813237542";
        private String merchantName = "AERONUTRIX SPORTS PRODUCTS";
        private String merchantMcc = "Retail";
        private String merchantTransId;
        private String merchantVPA = "paytm.d956934823@ptyesb";
        private String acquiringMCC = "7322";

        public Builder setMerchantId(String merchantId) {
            this.merchantId = merchantId;
            return this;
        }

        public Builder setMerchantName(String merchantName) {
            this.merchantName = merchantName;
            return this;
        }

        public Builder setMerchantMcc(String merchantMcc) {
            this.merchantMcc = merchantMcc;
            return this;
        }

        public Builder setMerchantTransId(String merchantTransId) {
            this.merchantTransId = merchantTransId;
            return this;
        }

        public Builder setMerchantVPA(String merchantVPA) {
            this.merchantVPA = merchantVPA;
            return this;
        }

        public Builder setAcquiringMCC(String acquiringMCC) {
            this.acquiringMCC = acquiringMCC;
            return this;
        }

        public UpiPaymentMerchantInfo build() {
            return new UpiPaymentMerchantInfo(this);
        }
    }
}
