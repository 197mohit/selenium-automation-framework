package com.paytm.utils.merchant.peon

import com.paytm.framework.api.curlloggingutil.CurlLoggingRestAssuredConfigBuilder
import com.paytm.framework.conditions.Wait
import com.paytm.framework.reporting.filters.RequestResponseLoggingFilter
import io.restassured.response.Response

import static com.paytm.utils.merchant.Constants.PGP_HOST
import static io.restassured.RestAssured.given

class EncPeon implements Peon {
    String orderId;
    String name = 'encpeon'
    private Response response;
    private Map map;

    EncPeon(String orderId) {
        this.orderId = orderId;
    }

    @Override
    EncPeon get() {
        new Wait({ 2 }, 10, 10**3).apply({
            given().config(new CurlLoggingRestAssuredConfigBuilder().build()).config(new CurlLoggingRestAssuredConfigBuilder().build())
                    .filter([new RequestResponseLoggingFilter()])
                    .baseUri(PGP_HOST)
                    .basePath('/mockbank/encpeon')
                    .queryParam('orderId', orderId)
                    .get()
                    .with {
                statusCode != 204
            }
        })
        this.response = given().config(new CurlLoggingRestAssuredConfigBuilder().build())
                .baseUri(PGP_HOST)
                .basePath('/mockbank/encpeon')
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
        return this.map;
    }
}
