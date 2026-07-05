package com.paytm.utils.merchant.merchant.util.names

import com.paytm.framework.api.curlloggingutil.CurlLoggingRestAssuredConfigBuilder
import com.paytm.utils.merchant.merchant.util.Merchant
import com.paytm.utils.merchant.util.exception.pgpException.PGPException
import groovy.transform.Memoized

import static com.paytm.utils.merchant.Constants.PGP_HOST
import static io.restassured.RestAssured.given

class DisplayName implements MerchantName {

    final Merchant m

    DisplayName(Merchant m) {
        this.m = m
    }

    @Memoized
    @Override
    String getName() {
        given().config(new CurlLoggingRestAssuredConfigBuilder().build()).baseUri(PGP_HOST).basePath("/mapping-service/get/merchantlogoinfo/v2/$m.id").get().thenReturn().tap {
            if (it.statusCode != 200) throw new PGPException("unable to check $type name of $m")
        }.path('merchantDisplayName') as String
    }

    @Override
    String getType() {
        'display'
    }
}
