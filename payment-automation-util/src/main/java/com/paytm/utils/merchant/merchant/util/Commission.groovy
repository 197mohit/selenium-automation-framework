package com.paytm.utils.merchant.merchant.util

import groovy.transform.ToString

@ToString(includePackage = false, includeNames = true)
class Commission {
    final double feeRate
    final double fixedFee
    final double maxFee
    final double minAmt
    final double maxAmt
    final String payMode

    Commission(double feeRate, double fixedFee, double maxFee, double minAmt, double maxAmt, String payMode) {
        this.feeRate = feeRate
        this.fixedFee = fixedFee
        this.maxFee = maxFee
        this.minAmt = minAmt
        this.maxAmt = maxAmt
        this.payMode = payMode
    }
}