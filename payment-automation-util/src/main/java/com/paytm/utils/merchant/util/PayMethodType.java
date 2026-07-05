package com.paytm.utils.merchant.util;

public enum PayMethodType {
    DEBIT_CARD("DEBIT_CARD"),
    COD("MP_COD"),
    NET_BANKING("NET_BANKING"),
    UPI("UPI"),
    CREDIT_CARD("CREDIT_CARD"),
    EMI("EMI"),
    PAYTM_DIGITAL_CREDIT("PAYTM_DIGITAL_CREDIT"),
    ATM("ATM"),
    PPBL("PPBL"),
    IMPS("IMPS"),
    BALANCE("BALANCE"),
    HYBRID_PAYMENT("HYBRID_PAYMENT"),
    ADDANDPAY("ADDANDPAY"),
    EMI_DC("EMI_DC");

    private final String paymethodType;

    PayMethodType(String paymethodType) { this.paymethodType = paymethodType; }

    @Override
    public String toString() { return paymethodType;}
}
