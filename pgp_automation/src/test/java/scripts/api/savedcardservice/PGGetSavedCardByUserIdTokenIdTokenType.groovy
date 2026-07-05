package scripts.api.savedcardservice

import com.paytm.apphelpers.PGPHelpers
import com.paytm.base.test.TestSetUp
import com.paytm.utils.merchant.user.alipay.AlipayUser
import com.paytm.utils.merchant.user.annotations.AUser
import io.qameta.allure.Owner
import io.restassured.builder.RequestSpecBuilder
import io.restassured.http.ContentType
import org.testng.annotations.Test

import static com.paytm.LocalConfig.PGP_HOST
import static io.restassured.RestAssured.given
import static org.hamcrest.Matchers.*

@Owner('Deepak')
class PGGetSavedCardByUserIdTokenIdTokenType extends TestSetUp {

    def primary

    final def req = { userId, tokenId, tokenType = 'oauth' ->
        given(
                new RequestSpecBuilder()
                        .addRequestSpecification(reqSpec())
                        .setContentType(ContentType.JSON)
                        .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                        .setBaseUri(PGP_HOST)
                        .setBasePath("/savedcardservice/savedCardService/pg/get/savedcard/userId/${userId}/tokenId/${tokenId}/tokenType/${tokenType}")
                        .build()
        )
    }

    @AUser(edit = true)
    @Test
    void testSuccess() {
        user().savedCards.clear()
        (user() as AlipayUser).savedCards.clear()
        def card = cards.find()
        assert user().savedCards.add(card)
        req(user().id, user().tokens['sso'].id, 'oauth').get().then()
                .statusCode(200)
                .body('responseStatus', equalTo('SUCCESS'),
                'httpCode', equalTo('200'),
                'httpSubCode', equalTo('200'),
                'codeDetail', equalTo('Success'),
                'response', isA(List.class))
                .root('response[0]')
                .body('cardId', isIn(user().savedCards.find().id as Integer, card.idxNo),
                'cardNumber', isA(String.class),
                'cardType', isA(Integer.class),
                'expiryDate', isA(String.class),
                'firstSixDigit', equalTo(card.no[0..5]),
                'lastFourDigit', equalTo(card.no[-4..-1]),
                'status', isIn(0, 1),
                'updated_on', isA(Long.class),
                'created_on', isA(Long.class),
                'mId', isEmptyOrNullString(),
                'custId', isEmptyOrNullString(),
                'cardScheme', isIn('DEBIT_CARD', 'CREDIT_CARD'),
                'bankName', isA(String.class))
    }
}
