package com.paytm.utils.merchant.merchant.util

import com.paytm.framework.api.curlloggingutil.CurlLoggingRestAssuredConfigBuilder
import com.paytm.utils.merchant.GList
import com.paytm.utils.merchant.user.Card
import com.paytm.utils.merchant.util.PGPUtil
import io.restassured.http.ContentType

import static com.paytm.utils.merchant.Constants.PGP_HOST
import static io.restassured.RestAssured.given

class SavedCards implements GList<Card> {

    private final MerchantUser u

    SavedCards(MerchantUser user) {
        this.u = user
    }

    @Override
    Iterator<Card> iterator() {
        new Iterator<Card>() {
            List<Card> list = {
                def root = [
                        CUSTID      : u.id,
                        MID         : u.m.id,
                        REQUEST_TYPE: 'DEFAULT',
                        CHECKSUM    : null,
                ]
                root.CHECKSUM = PGPUtil.getChecksum(u.m.key, root.findAll { it.value != null } as TreeMap)
                given().config(new CurlLoggingRestAssuredConfigBuilder().build()).contentType(ContentType.JSON).baseUri(PGP_HOST).basePath('/savedcardservice/merchant/v1/get/card').body(root).post().path('response').collect {
                    new Card(it.savedCardId as String, u)
                }
            }()
            int index = 0

            @Override
            boolean hasNext() {
                list.size() > index
            }

            @Override
            Card next() {
                list[index++]
            }
        }
    }

    @Override
    boolean addAll(Collection<? extends Card> c) {
        if (!u.m.editable) throw new UnsupportedOperationException()
        c.every {
            def root = [
                    paymentTypeId: '0',
                    cardNumber   : it.no,
                    userId       : null,
                    status       : null,
                    cardType     : null,
                    expiryDate   : it.expMo + it.expYr,
                    firstSixDigit: it.no[0..5],
                    lastFourDigit: it.no[-4..-1],
                    transactionId: null,
                    created_on   : null,
                    updated_on   : null,
                    custId       : u.id,
                    mId          : u.m.id,
            ]
            given().config(new CurlLoggingRestAssuredConfigBuilder().build()).contentType(ContentType.JSON).baseUri(PGP_HOST + '/savedcardservice/savedCardService/v1').basePath('/add/savecard/mId/custId').body(root).post().path('responseStatus') == 'SUCCESS'
        }
    }

    @Override
    boolean removeAll(Collection<?> c) {
        if (!u.m.editable) throw new UnsupportedOperationException()
        c.every {
            given().config(new CurlLoggingRestAssuredConfigBuilder().build()).baseUri(PGP_HOST + '/savedcardservice/savedCardService/v1').basePath("/delete/savedcard/cardId/userId/mId/custId/${it.id}/${null}/${u.m.id}/${u.id}").delete().path('responseStatus') == 'SUCCESS'
        }
    }
}
