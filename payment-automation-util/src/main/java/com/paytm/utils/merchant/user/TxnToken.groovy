package com.paytm.utils.merchant.user

import com.paytm.utils.merchant.util.AuthUtil

import static com.paytm.utils.merchant.Constants.AUTH_HOST

class TxnToken implements Token {

    private final User user
    final String name = 'txn'
    String id

    TxnToken(User user) {
        this.user = user
    }

    @Override
    String getId() {
        id ?: (id = AuthUtil.getTxnToken(AUTH_HOST, user.mobile, user.password))
    }

    @Override
    String toString() {
        name
    }
}
