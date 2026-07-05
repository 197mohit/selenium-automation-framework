package scripts.api.savedcardservice

import com.paytm.apphelpers.PGPHelpers
import com.paytm.base.test.TestSetUp
import com.paytm.utils.merchant.merchant.util.alipay.AlipayMerchant
import com.paytm.utils.merchant.merchant.util.annotations.Merchant
import com.paytm.utils.merchant.user.annotations.AUser
import io.qameta.allure.Owner
import io.restassured.builder.RequestSpecBuilder
import io.restassured.http.ContentType
import org.testng.annotations.Test

import static com.paytm.LocalConfig.PGP_HOST
import static com.paytm.utils.merchant.util.PGPUtil.getChecksum
import static io.restassured.RestAssured.given
import static org.hamcrest.Matchers.*

@Owner('Deepak')
class GetCard extends TestSetUp {

    def primary

    final def req = {
        given(
                new RequestSpecBuilder()
                        .addRequestSpecification(reqSpec())
                        .setContentType(ContentType.JSON)
                        .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                        .setBaseUri(PGP_HOST)
                        .setBasePath("/savedcardservice/merchant/v1/get/card")
                        .build()
        )
    }

    final def root = {
        [
                CUSTID      : new Random().nextLong().abs() as String,
                SSO_TOKEN   : user().tokens['sso'].id,
                MID         : m().id,
                REQUEST_TYPE: 'DEFAULT',
                CHECKSUM    : null,
        ]
    }

    @Merchant(edit = true)
    @AUser(edit = true)
    @Test
    void testSuccess() {
        def root = root()
        user().savedCards.clear()
        (m() as AlipayMerchant).users[0].savedCards.clear()
        def card = cards.find()
        assert user().savedCards.add(card)
        assert (m() as AlipayMerchant).users[0].savedCards.add(card)
        root.CHECKSUM = getChecksum(m().key, root.findAll { it.value != null } as TreeMap)
        req().body(root).post().then()
                .statusCode(200)
                .body('responseStatus', equalTo('SUCCESS'),
                        'httpCode', equalTo('200'),
                        'httpSubCode', equalTo('200'),
                        'codeDetail', equalTo('Success'),
                        'response', isA(List.class))
                .root('response[0]')
                .body('savedCardId', equalTo(m().users[0].savedCards[0].id),
                        'cardFirstSixDigits', equalTo(card.no[0..5]),
                        'cardLastFourDigits', equalTo(card.no[12..15]),
                        'cardType', isIn('DEBIT_CARD', 'CREDIT_CARD'),
                        'issuerDisplayName', isA(String.class),
                        'issuerCode', isA(String.class),
                        'cardScheme', equalTo(card.scheme.toUpperCase()))
    }
}
