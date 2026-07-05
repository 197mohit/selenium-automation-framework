package com.paytm.utils.merchant.merchant.util

import com.paytm.utils.merchant.GList

class MerchantUsers implements GList<MerchantUser> {

    private final Merchant m

    MerchantUsers(Merchant m) {
        this.m = m
    }

    @Override
    Iterator<MerchantUser> iterator() {
        new Iterator<MerchantUser>() {
            int index = 0

            @Override
            boolean hasNext() {
                index < 100
            }

            @Override
            MerchantUser next() {
                new MerchantUser(m, m.id + 'f' * 5 + index++)
            }
        }
    }

    @Override
    boolean addAll(Collection<? extends MerchantUser> c) {
        return false
    }

    @Override
    boolean removeAll(Collection<?> c) {
        return false
    }
}
