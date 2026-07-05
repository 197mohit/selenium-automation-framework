package com.paytm.utils.merchant.merchant.util

import com.paytm.framework.api.curlloggingutil.CurlLoggingRestAssuredConfigBuilder
import com.paytm.framework.utils.CommonUtils
import com.paytm.utils.merchant.Constants
import com.paytm.utils.merchant.GList
import com.paytm.utils.merchant.util.PGPUtil
import groovy.json.JsonOutput
import io.restassured.RestAssured
import io.restassured.filter.log.RequestLoggingFilter
import io.restassured.filter.log.ResponseLoggingFilter
import io.restassured.http.ContentType
import org.hamcrest.Matchers
import org.joda.time.DateTime

import static com.paytm.utils.merchant.util.PGPUtil.getChecksum
import static groovy.json.JsonOutput.toJson
import static io.restassured.RestAssured.given
import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.isEmptyString
import static org.hamcrest.Matchers.not

class OrdersV2 implements GList<OrderV2> {

    List<OrderV2> list = []
    private final Merchant m

    OrdersV2(Merchant m) {
        this.m = m
    }

    @Override
    Iterator<OrderV2> iterator() {
        return new Iterator<OrderV2>() {
//            List<OrderV2> list
            int index = 0

            @Override
            boolean hasNext() {
                list.size() > index
            }

            @Override
            OrderV2 next() {
                list[index++]
            }
        }
    }

    @Override
    boolean addAll(Collection<? extends OrderV2> c) {
        if (!m.editable) throw new UnsupportedOperationException()
        c.every {
            def root = [
                    head: [
                            signature: null,
                    ],
                    body: [
                            requestType  : 'NATIVE',
                            mid          : m.id,
                            orderId      : it.id,
                            websiteName  : 'retail',
                            txnAmount    : [
                                    currency: 'INR',
                                    value   : it.amt as String,
                            ],
                            userInfo     : [
                                    custId: System.currentTimeMillis() as String
                            ],
                            paytmSsoToken: it.ssoToken
                    ]
            ]
            root.head.signature = getChecksum(m.key, toJson(root.body))
            String txnToken = given().config(new CurlLoggingRestAssuredConfigBuilder().build()).contentType(ContentType.JSON).filters([new RequestLoggingFilter(), new ResponseLoggingFilter()]).baseUri(Constants.PGP_HOST).basePath('/theia/api/v1/initiateTransaction').queryParams([mid: m.id, orderId: root.body.orderId]).body(root).post().then().body('body.resultInfo.resultStatus', equalTo('S'), 'body.txnToken', not(isEmptyString())).extract().path('body.txnToken')
            list.add(new OrderV2(it.id, it.amt, txnToken, it.ssoToken, m, root.body.userInfo.custId))
        }
    }

    @Override
    boolean removeAll(Collection<?> c) {
        return false
    }
}
