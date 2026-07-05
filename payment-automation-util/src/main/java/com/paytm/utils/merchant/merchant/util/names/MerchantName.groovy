package com.paytm.utils.merchant.merchant.util.names

trait MerchantName {

    abstract String getName()

    abstract String getType()

    @Override
    String toString() {
        "$type($name)"
    }
}
