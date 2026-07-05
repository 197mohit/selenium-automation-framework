package com.paytm.utils.merchant.user.wallet

interface WalletLimit {

    void breach()

    void reset()
}