package com.paytm.utils.merchant.merchant.util

import com.paytm.framework.api.curlloggingutil.CurlLoggingRestAssuredConfigBuilder
import com.paytm.utils.merchant.GList
import io.restassured.http.ContentType

import static com.paytm.utils.merchant.Constants.MOCK_HOST
import static io.restassured.RestAssured.given

class Promos implements GList<Promo> {

    private final Merchant m

    Promos(Merchant m) {
        this.m = m
    }

    @Override
    Iterator<Promo> iterator() {
        return new Iterator<Promo>() {
            private def list = given().config(new CurlLoggingRestAssuredConfigBuilder().build()).baseUri(MOCK_HOST).basePath('/mockbank/v1/promosearch/payment/offers').queryParam('merchant-id', m.id).contentType(ContentType.JSON).get().then().statusCode(200).extract().path('data').collect {
                new Promo(it.promocode)
            }
            private int index = 0

            @Override
            boolean hasNext() {
                list.size() > index
            }

            @Override
            Promo next() {
                list[index++]
            }
        }
    }

    @Override
    boolean addAll(Collection<? extends Promo> c) {
        if (!m.editable) throw new UnsupportedOperationException()
        def root = [
                mid : m.id,
                data: c.collect { [name: it.name, expired: it.expired, used: it.used, is8DigitBin: it.is8DigitBin] }
        ]
        given().config(new CurlLoggingRestAssuredConfigBuilder().build()).baseUri(MOCK_HOST).basePath('/mockbank/promos/add').contentType(ContentType.JSON).body(root).post().statusCode() == 200
    }

    @Override
    boolean removeAll(Collection<?> c) {
        if (!m.editable) throw new UnsupportedOperationException()
        def root = [
                mid   : m.id,
                promos: c.collect { it.name }
        ]
        given().config(new CurlLoggingRestAssuredConfigBuilder().build()).baseUri(MOCK_HOST).basePath('/mockbank/promos/remove').contentType(ContentType.JSON).body(root).post().statusCode() == 200
    }
}
