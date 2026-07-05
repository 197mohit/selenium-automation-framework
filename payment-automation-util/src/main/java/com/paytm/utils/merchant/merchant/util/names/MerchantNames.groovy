package com.paytm.utils.merchant.merchant.util.names

import com.paytm.utils.merchant.GList
import com.paytm.utils.merchant.merchant.util.Merchant

class MerchantNames implements GList<MerchantName> {

    final Merchant m

    MerchantNames(Merchant m) {
        this.m = m
    }

    MerchantName getBusiness() {
        this['business']
    }

    MerchantName getDisplay() {
        this['display']
    }

    MerchantName getAt(String type) {
        this.find { it.type == type }
    }

    @Override
    Iterator<MerchantName> iterator() {
        new Iterator<MerchantName>() {
            List<MerchantName> list = [new BusinessName(m), new DisplayName(m)]
            int index = 0

            @Override
            boolean hasNext() {
                list.size() > index
            }

            @Override
            MerchantName next() {
                list[index++]
            }
        }
    }

    @Override
    boolean addAll(Collection<? extends MerchantName> c) {
        throw new UnsupportedOperationException()
    }

    @Override
    boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException()
    }
}
