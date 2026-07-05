package com.paytm.utils.merchant.user

import com.paytm.utils.merchant.merchant.util.annotations.Weight
import com.paytm.utils.merchant.merchant.util.mappings.Mapper

class MSubWallets implements Mapper {
    @Weight(1)
    Boolean main
    @Weight(2)
    Boolean food
    @Weight(2)
    Boolean gift
}
