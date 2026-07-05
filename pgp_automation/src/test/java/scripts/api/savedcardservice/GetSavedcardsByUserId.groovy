package scripts.api.savedcardservice

import com.paytm.LocalConfig
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
class GetSavedcardsByUserId extends TestSetUp {

    def primary

    final def req = {
        given(
                new RequestSpecBuilder()
                        .addRequestSpecification(reqSpec())
                        .setContentType(ContentType.JSON)
                        .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                        .setBaseUri(PGP_HOST)
                        .setBasePath("/savedcardservice/savedcardOpenAPIService/v1/savedcardsByUserId")
                        .build()
        )
    }

    final def root = {
        [
                head: [
                        clientId        : 'C11',
                        version         : 'v1',
                        requestTimestamp: 'Time',
                        channelId       : 'WEB',
                        tokenType       : 'JWT',
                        token           : {
                            def map = [
                                    tokenType: 'JWT',
                                    userId   : user().id,
                            ]
                            PGPHelpers.createJsonWebToken(map, PGPHelpers.ISSUER.ts, LocalConfig.PG_JWT_KEY)
                        }(),
                ],
                body: [
                        userId                   : user().id,
                        isCardIndexNumberRequired: 'true'
                ],
        ]
    }

    @AUser(edit = true)
    @Test
    void testSuccess() {
        def root = root()
        user().savedCards.clear()
        def card = cards.find()
        assert user().savedCards.add(card)
        req().body(root).post().then()
                .statusCode(200)
                .body('', hasKey('head'),
                '', hasKey('body'))
                .root('head')
                .body('responseTimestamp', isA(String.class),
                'version', equalTo('v1'),
                'clientId', equalTo(root.head.clientId),
                'signature', nullValue())
                .root('body')
                .body('resultInfo', isA(Object.class),
                'savedCardDetails', isA(List.class))
                .root('body.resultInfo')
                .body('resultStatus', equalTo('S'),
                'resultCodeId', nullValue(),
                'resultCode', equalTo('00000000'),
                'resultMsg', equalTo('Success'))
                .root('body.savedCardDetails[0]')
                .body('cardId', equalTo(user().savedCards.find().id as Integer),
                'maskedCardNumber', isA(String.class),
                'cardType', isIn('DC', 'CC'),
                'cardExpiry', equalTo(card.with { it.expMo + it.expYr }),
                'cardScheme', equalTo(card.scheme.toUpperCase()),
                'bankName', isA(String.class),
                'cardIndexNumber', isA(String.class))
    }
}
