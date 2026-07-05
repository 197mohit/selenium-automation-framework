package com.paytm.utils.merchant.intersections

import com.paytm.framework.api.curlloggingutil.CurlLoggingRestAssuredConfigBuilder
import com.paytm.utils.merchant.GiftVouchers
import com.paytm.utils.merchant.merchant.util.Merchant
import com.paytm.utils.merchant.user.User
import groovy.transform.Memoized
import io.restassured.http.ContentType

import static com.paytm.utils.merchant.Constants.PGP_HOST
import static io.restassured.RestAssured.given

class MerchantUserIntersection {

    private final Merchant m
    private final User u

    MerchantUserIntersection(Merchant m, User u) {
        this.m = m
        this.u = u
    }

    @Memoized
    GiftVouchers getGiftVouchers() {
        new GiftVouchers(m, u)
    }

    @Memoized
    List<String> getPayModes() {
        def map = [NET_BANKING: 'nb', UPI: 'upi', 'UPI_PUSH': 'upip', 'UPI_PUSH_EXPRESS': 'upipe', CREDIT_CARD: 'cc', DEBIT_CARD: 'dc', EMI: 'emi', PAYTM_DIGITAL_CREDIT: 'pdc', EMI_DC: 'emidc', COD: 'cod', MP_COD: 'cod', BALANCE: 'ppi', PPBL: 'ppbl', 'ADVANCE_DEPOSIT_ACCOUNT': 'ada']
        def root = [
                head: [
                        "channelId": 'WEB',
                        "tokenType": 'SSO',
                        "token"    : u.tokens['sso'].id,
                ],
                body: [
                        "mid": m.id,
                ]
        ]
        given().config(new CurlLoggingRestAssuredConfigBuilder().build()).contentType(ContentType.JSON).baseUri(PGP_HOST).basePath('/theia/api/v1/fetchPaymentOptions').queryParams([mid: m.id]).body(root).post().jsonPath().get('body.merchantPayOption.paymentModes.paymentMode').collect {
            map[it]
        }
    }
}
