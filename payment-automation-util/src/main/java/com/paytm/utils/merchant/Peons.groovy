package com.paytm.utils.merchant

import com.paytm.framework.api.curlloggingutil.CurlLoggingRestAssuredConfigBuilder
import com.paytm.framework.reporting.filters.RequestResponseLoggingFilter

import static com.paytm.utils.merchant.Constants.MOCK_HOST
import static io.restassured.RestAssured.given


class Peons implements GList<Peon> {

    Peon getAt(String orderId) {
        given().config(new CurlLoggingRestAssuredConfigBuilder().build()).filters([new RequestResponseLoggingFilter()]).baseUri(MOCK_HOST).basePath('/mockbank/peon').queryParam('orderId', orderId).get().with {
            statusCode == 200 ? it : null
        }?.path('') as Peon
    }

    @Override
    Iterator<Peon> iterator() {
        throw new UnsupportedOperationException()
    }

    @Override
    boolean addAll(Collection<? extends Peon> c) {
        throw new UnsupportedOperationException()
    }

    @Override
    boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException()
    }
}
