package com.paytm.utils.merchant.user

import com.paytm.utils.merchant.util.AuthUtil

import static com.paytm.utils.merchant.Constants.AUTH_HOST

class PaytmToken implements Token {

    private final User user
    final String name = 'paytm'
    String id

    PaytmToken(User user) {
        this.user = user
    }

    @Override
    String getId() {
        id ?: (id = AuthUtil.getPaytmToken(AUTH_HOST, user.mobile, user.password))
    }

    @Override
    String toString() {
        name
    }
}
