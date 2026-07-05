package com.paytm.utils.merchant.peon

import com.paytm.framework.api.curlloggingutil.CurlLoggingRestAssuredConfigBuilder
import com.paytm.framework.conditions.Wait
import com.paytm.framework.reporting.filters.RequestResponseLoggingFilter
import io.restassured.response.Response

import static com.paytm.utils.merchant.Constants.PGP_HOST
import static io.restassured.RestAssured.given

class TxnPeon implements Peon {

    String orderId;
    String name = 'txnpeon'
    private Response response;
    private Map map;

    TxnPeon(String orderId) {
        this.orderId = orderId
    }

    @Override
    TxnPeon get() {
        new Wait({ 2 }, 10, 10**3).apply({
            given().config(new CurlLoggingRestAssuredConfigBuilder().build())
                    .filter([new RequestResponseLoggingFilter()])
                    .baseUri(PGP_HOST)
                    .basePath('/mockbank/peon')
                    .queryParam('orderId', orderId)
                    .get()
                    .with {
                statusCode != 204
            }
        })
        this.response = given().config(new CurlLoggingRestAssuredConfigBuilder().build())
                .baseUri(PGP_HOST)
                .basePath('/mockbank/peon')
                .queryParam('orderId', orderId)
                .get()
        this.map = this.response
                .path('') as Map
        return this
    }

    @Override
    Response response() {
        return this.response
    }

    @Override
    Map map() {
        this.map
    }
}
