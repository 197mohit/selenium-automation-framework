package scripts.api.savedcardservice

import com.paytm.apphelpers.PGPHelpers
import com.paytm.base.test.TestSetUp
import com.paytm.utils.merchant.merchant.util.annotations.Merchant
import io.qameta.allure.Owner
import io.restassured.builder.RequestSpecBuilder
import io.restassured.http.ContentType
import org.testng.annotations.Test

import static com.paytm.LocalConfig.PGP_HOST
import static io.restassured.RestAssured.given
import static org.hamcrest.Matchers.*

@Owner('Deepak')
class GetSavedCardByCustIdMId extends TestSetUp {

    def primary

    final def req = { custId, mId ->
        given(
                new RequestSpecBuilder()
                        .addRequestSpecification(reqSpec())
                        .setContentType(ContentType.JSON)
                        .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                        .setBaseUri(PGP_HOST)
                        .setBasePath("/savedcardservice/savedCardService/v1/get/savedcard/custId/mId/${custId}/${mId}")
                        .build()
        )
    }

    @Merchant(edit = true)
    @Test
    void testSuccess() {
        m().users[0].savedCards.clear()
        def card = cards.find()
        assert m().users[0].savedCards.add(card)
        req(m().users[0].id, m().id).get().then()
                .statusCode(200)
                .body('responseStatus', equalTo('SUCCESS'),
                'httpCode', equalTo('200'),
                'httpSubCode', equalTo('200'),
                'codeDetail', equalTo('Success'),
                'response', isA(List.class))
                .root('response[0]')
                .body('cardId', isA(Integer.class),
                'cardNumber', isA(String.class),
                'cardType', isA(Integer.class),
                'expiryDate', isA(String.class),
                'firstSixDigit', equalTo(card.no[0..5]),
                'lastFourDigit', equalTo(card.no[-4..-1]),
                'status', isIn(0, 1),
                'updated_on', isA(Long.class),
                'created_on', isA(Long.class),
                'mId', equalTo(m().id),
                'custId', equalTo(m().users[0].id),
                'cardScheme', isIn('DEBIT_CARD', 'CREDIT_CARD'),
                'bankName', isA(String.class))
    }
}
