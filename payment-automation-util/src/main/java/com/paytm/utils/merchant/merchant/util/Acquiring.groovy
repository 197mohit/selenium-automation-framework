package com.paytm.utils.merchant.merchant.util

import groovy.transform.ToString

@ToString(includePackage = false, includeNames = true)
class Acquiring {

    protected final String id
    final String payMode
    final String bank
    final boolean enabled

    protected Acquiring(String id, String payMode, String bank, boolean enabled) {
        this.id = id
        this.payMode = payMode
        this.bank = bank
        this.enabled = enabled
    }

    Acquiring(String payMode, String bank) {
        this.payMode = payMode
        this.bank = bank
    }

    boolean equals(Object o) {
        o instanceof Acquiring ? this.payMode == o.payMode && this.bank == o.bank : false
    }
}
