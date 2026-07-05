package com.paytm.utils.merchant.merchant.util.alipay

import groovy.transform.Memoized

class MerchantUser {

    final AlipayMerchant m
    final String id

    MerchantUser(AlipayMerchant m, String id) {
        this.m = m
        this.id = id
    }

    @Memoized
    SavedCards getSavedCards() {
        new SavedCards(this)
    }
}
