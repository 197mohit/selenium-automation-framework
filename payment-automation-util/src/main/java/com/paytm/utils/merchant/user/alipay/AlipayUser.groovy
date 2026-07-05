package com.paytm.utils.merchant.user.alipay

import com.paytm.framework.api.curlloggingutil.CurlLoggingRestAssuredConfigBuilder
import groovy.transform.Memoized

import static com.paytm.utils.merchant.Constants.AUTH_HOST
import static io.restassured.RestAssured.given

class AlipayUser {
    final com.paytm.utils.merchant.user.User paytmUser

    AlipayUser(com.paytm.utils.merchant.user.User paytmUser) {
        this.paytmUser = paytmUser
    }

    String getId() {
        given().config(new CurlLoggingRestAssuredConfigBuilder().build())
                .baseUri(AUTH_HOST)
                .basePath('/user/mapping')
                .header('Authorization', 'Basic cGF5dG0tcGctY2xpZW50LXN0YWdpbmc6YTc0MjZiZTAtYTJkZC00N2NmLWExODEtYjM3YzgwMWYzNGM2')
                .body([paytmUserId: this.paytmUser.id])
                .post().jsonPath()
                .get('mapping.platformPlusUserId')
    }

    String getAccountId() {
        given().config(new CurlLoggingRestAssuredConfigBuilder().build())
                .baseUri(AUTH_HOST)
                .basePath('/user/mapping')
                .header('Authorization', 'Basic cGF5dG0tcGctY2xpZW50LXN0YWdpbmc6YTc0MjZiZTAtYTJkZC00N2NmLWExODEtYjM3YzgwMWYzNGM2')
                .body([paytmUserId: this.paytmUser.id])
                .post().jsonPath()
                .get('mapping.platformPlusAccountId')
    }

    @Memoized
    SavedCards getSavedCards() {
        new SavedCards(this)
    }
}
