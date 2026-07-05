package com.paytm.utils.merchant.user

import com.paytm.framework.api.curlloggingutil.CurlLoggingRestAssuredConfigBuilder
import com.paytm.utils.merchant.Constants
import com.paytm.utils.merchant.intersections.MerchantUserIntersection
import com.paytm.utils.merchant.merchant.util.Merchant
import com.paytm.utils.merchant.merchant.util.mappings.Mapper
import com.paytm.utils.merchant.user.alipay.AlipayUser
import com.paytm.utils.merchant.user.wallet.Wallets
import com.paytm.utils.merchant.util.AuthUtil
import groovy.transform.Memoized
import groovy.transform.ToString

import static io.restassured.RestAssured.given

@ToString(includes = ['mobile'], includePackage = false)
class User implements Mapper {

    final String mobile
    final String password
    final boolean editable
    final Tokens tokens = new Tokens((this))
    final Wallets wallets = new Wallets(this)
    final Accounts accounts = new Accounts(this)
    final SavedCards savedCards = new SavedCards(this)
    final UserQRCodes qrCodes = new UserQRCodes(this)

    User(String mobile, String password, boolean editable) {
        this.mobile = mobile
        this.password = password
        this.editable = editable
    }

    User(String mobile, String password) {
        this(mobile, password, false)
    }

    @Memoized
    String getId() {
        AuthUtil.getCustomerID(Constants.AUTH_HOST, this.mobile)
    }

    String getAlipayId() {
        given().config(new CurlLoggingRestAssuredConfigBuilder().build()).baseUri(Constants.AUTH_HOST).basePath('/user/mapping').body([paytmUserId: id])
                .header('Authorization', 'Basic cGF5dG0tcGctY2xpZW50LXN0YWdpbmc6YTc0MjZiZTAtYTJkZC00N2NmLWExODEtYjM3YzgwMWYzNGM2')
                .post().jsonPath().get('mapping.platformPlusUserId')
    }

    String getAlipayAccountId() {
        given().config(new CurlLoggingRestAssuredConfigBuilder().build()).baseUri(Constants.AUTH_HOST).basePath('/user/mapping').body([paytmUserId: id])
                .header('Authorization', 'Basic cGF5dG0tcGctY2xpZW50LXN0YWdpbmc6YTc0MjZiZTAtYTJkZC00N2NmLWExODEtYjM3YzgwMWYzNGM2')
                .post().jsonPath().get('mapping.platformPlusAccountId')
    }

    @Memoized
    MerchantUserIntersection intersect(Merchant m) {
        m.intersect(this)
    }

    @Memoized
    Object asType(Class<?> clazz) {
        if (clazz == AlipayUser) return new AlipayUser(this)
        else return super.asType(clazz)
    }

    boolean equals(User user) {
        this.mobile == user.mobile
    }

}
