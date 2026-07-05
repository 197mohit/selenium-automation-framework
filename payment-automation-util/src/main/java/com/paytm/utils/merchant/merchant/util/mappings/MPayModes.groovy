package com.paytm.utils.merchant.merchant.util.mappings

import com.paytm.utils.merchant.merchant.util.annotations.Weight;

class MPayModes implements Mapper {
    @Weight(2)
    Boolean cc
    @Weight(2)
    Boolean dc
    @Weight(2)
    Boolean ppbl
    @Weight(2)
    Boolean nb
    @Weight(4)
    Boolean upi
    @Weight(2)
    Boolean cod
    @Weight(10)
    Boolean emi
}
