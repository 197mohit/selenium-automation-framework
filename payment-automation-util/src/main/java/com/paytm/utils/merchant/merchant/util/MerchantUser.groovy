package com.paytm.utils.merchant.merchant.util

import groovy.transform.Memoized

class MerchantUser {

    final Merchant m
    final String id
    final SavedCards savedCards = new SavedCards(this)

    MerchantUser(Merchant m, String id) {
        this.m = m
        this.id = id
    }

}
