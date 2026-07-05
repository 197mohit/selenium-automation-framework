package com.paytm.utils.merchant.merchant.util.alipay

import com.paytm.utils.merchant.GList

class MerchantUsers implements GList<MerchantUser> {

    private final AlipayMerchant m

    MerchantUsers(AlipayMerchant m) {
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
                new MerchantUser(m, m.paytmMerchant.id + 'f' * 5 + index++)
            }
        }

    }

    @Override
    boolean addAll(Collection<? extends MerchantUser> c) {
        throw new UnsupportedOperationException()
    }

    @Override
    boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException()
    }
}
