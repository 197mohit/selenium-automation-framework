package com.paytm.utils.merchant.merchant.util.alipay

import com.paytm.framework.api.curlloggingutil.CurlLoggingRestAssuredConfigBuilder
import groovy.transform.Memoized
import groovy.transform.PackageScope

import static com.paytm.utils.merchant.Constants.PGP_HOST
import static io.restassured.RestAssured.given

class AlipayMerchant {
    @PackageScope
    final com.paytm.utils.merchant.merchant.util.Merchant paytmMerchant

    AlipayMerchant(com.paytm.utils.merchant.merchant.util.Merchant paytmMerchant) {
        this.paytmMerchant = paytmMerchant
    }

    String getId() {
        given().config(new CurlLoggingRestAssuredConfigBuilder().build())
                .baseUri(PGP_HOST)
                .basePath("/mapping-service/merchant/get/extended/info/${this.paytmMerchant.id}")
                .get().jsonPath()
                .get('extendedInfo.alipayMid')
    }

    @Memoized
    MerchantUsers getUsers() {
        new MerchantUsers(this)
    }
}
