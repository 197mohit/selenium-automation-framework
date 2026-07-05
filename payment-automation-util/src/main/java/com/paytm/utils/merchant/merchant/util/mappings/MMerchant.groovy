package com.paytm.utils.merchant.merchant.util.mappings

import com.paytm.utils.merchant.merchant.util.annotations.Weight

class MMerchant implements Mapper {
    String id
    String key
    @Weight(1)
    Boolean hybrid
    @Weight(1)
    Boolean addnpay
    @Weight(1)
    Boolean saveCardDetails
    @Weight(1)
    Boolean peon
    @Weight(5)
    Boolean onus
    @Weight(-50)
    Boolean migrated
    @Weight(-40)
    Boolean expired
    @Weight(1)
    Boolean checksum
    @Weight(10)
    Boolean pcf
    @Weight(0)
    Integer retry
    @Weight(0)
    MPayModes paymodes

    boolean equals(MMerchant mMerchant) {
        this.id == mMerchant.id
    }
}
