package com.paytm.utils.merchant.user

import com.paytm.utils.merchant.merchant.util.annotations.Weight
import com.paytm.utils.merchant.merchant.util.mappings.Mapper

class MUser implements Mapper {

    String mobile
    String password
    @Weight(3)
    Boolean premium
    @Weight(2)
    Boolean ppbl
    @Weight(2)
    Boolean paytmcc
    @Weight(-4)
    Boolean kyc
    @Weight(10)
    Boolean emidc
    @Weight(0)
    MSubWallets subWallets

    boolean equals(MUser user) {
        this.mobile == user.mobile
    }
}
