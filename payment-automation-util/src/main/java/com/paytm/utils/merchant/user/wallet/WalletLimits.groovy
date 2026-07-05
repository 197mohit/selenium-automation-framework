package com.paytm.utils.merchant.user.wallet

interface WalletLimits {

    WalletLimit getAdd()

    WalletLimit getRemove()

    WalletLimit getQuery()

}