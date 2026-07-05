package com.paytm.utils.merchant.util;

public enum MerchantPrefType {
    ADD_MONEY_ENABLED("ADD_MONEY_ENABLED"),
    HYBRID_ALLOWED("HYBRID_ALLOWED"),
    CHECKSUM_ENABLED("CHECKSUM_ENABLED"),
    OCP_ENABLED("OCP_ENABLED"),
    AUTO_REFUND_TO_BANK("AUTO_REFUND_TO_BANK"),
    OFFLINE_SNP_DES_TXT("OFFLINE_SNP_DESC_FLAG"),
    OFFLINE_SNP_DESC_FLAG("OFFLINE_SNP_DESC_FLAG"),
    WalletOnlyMerchant("WalletOnlyMerchant"),

    ;
    private String prefType;
    MerchantPrefType(String prefType) {
        this.prefType = prefType;
    }

    public String toString() { return prefType; }



}
