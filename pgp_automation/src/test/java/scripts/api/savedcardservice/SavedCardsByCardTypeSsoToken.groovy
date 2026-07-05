package scripts.api.savedcardservice

import com.paytm.apphelpers.PGPHelpers
import com.paytm.base.test.TestSetUp
import com.paytm.utils.merchant.user.annotations.AUser
import io.qameta.allure.Owner
import io.restassured.builder.RequestSpecBuilder
import io.restassured.http.ContentType
import org.testng.annotations.Test

import static com.paytm.LocalConfig.PGP_HOST
import static io.restassured.RestAssured.given
import static org.hamcrest.Matchers.*

@Owner('Deepak')
class SavedCardsByCardTypeSsoToken extends TestSetUp {

    def primary

    final def req = {
        given(
                new RequestSpecBuilder()
                        .addRequestSpecification(reqSpec())
                        .setContentType(ContentType.JSON)
                        .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                        .setBaseUri(PGP_HOST)
                        .setBasePath("/savedcardservice/savedcardOpenAPIService/savedcardsBycardTypeSsoToken")
                        .build()
        )
    }

    final def root = {
        [
                head: [
                        clientId : 'C11',
                        signature: ''
                ],
                body: [
                        cardType     : 'CC',
                        paytmSsoToken: user().tokens['sso'].id,
                ],
        ]
    }

    @AUser(edit = true)
    @Test
    void testSuccess() {
        def root = root()
        user().savedCards.clear()
        def card = cards.find { it.type == 'credit' }
        assert user().savedCards.add(card)
        root.body.cardType = 'CC'
        req().body(root).post().then()
                .statusCode(200)
                .body('head', isA(Object.class),
                'body', isA(Object.class))
                .root('head')
                .body('responseTimestamp', isA(String.class),
                'version', equalTo(root.head.version),
                'clientId', equalTo(root.head.clientId),
                'signature', nullValue())
                .root('body')
                .body('resultInfo', isA(Object.class),
                'savedCards', isA(List.class))
                .root('body.resultInfo')
                .body('resultStatus', equalTo('S'),
                'resultCodeId', nullValue(),
                'resultCode', equalTo('00000000'),
                'resultMsg', equalTo('Success'))
                .root('body.savedCards[0]')
                .body('cardId', equalTo(user().savedCards.find().id as Integer),
                'cardNumber', startsWith(card.no[0..5]),
                'cardNumber', endsWith(card.no[-4..-1]),
                'cardType', isIn('DC', 'CC'),
                'cardScheme', equalTo(card.scheme.toUpperCase()),
                'bank', isA(String.class),
                'displayName', isA(String.class))
    }
}
