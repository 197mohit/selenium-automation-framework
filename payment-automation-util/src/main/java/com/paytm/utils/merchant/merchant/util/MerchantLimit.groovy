package com.paytm.utils.merchant.merchant.util

import groovy.transform.ToString

@ToString(includePackage = false, includeNames = true)
class MerchantLimit {

    final String payMode
    final double max

    MerchantLimit(String payMode, double max) {
        this.payMode = payMode
        this.max = max
    }
}