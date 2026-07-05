package scripts.api.savedcardservice

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.paytm.LocalConfig
import com.paytm.apphelpers.PGPHelpers
import com.paytm.base.test.TestSetUp
import com.paytm.utils.merchant.user.alipay.AlipayUser
import com.paytm.utils.merchant.user.annotations.AUser
import io.qameta.allure.Owner
import io.restassured.builder.RequestSpecBuilder
import io.restassured.http.ContentType
import org.testng.annotations.Test

import static com.paytm.LocalConfig.PGP_HOST
import static com.paytm.LocalConfig.PG_JWT_KEY
import static io.restassured.RestAssured.given
import static org.hamcrest.Matchers.*

@Owner('Deepak')
class AddSavedCardTrustedCard extends TestSetUp {

    def primary

    final def req = {
        given(
                new RequestSpecBuilder()
                        .addRequestSpecification(reqSpec())
                        .setContentType(ContentType.JSON)
                        .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                        .setBaseUri(PGP_HOST)
                        .setBasePath("/savedcardservice/savedCardService/v1/add/savedcard/trustedCard")
                        .build()
        )
    }

    final def root = {
        cards.find().with {
            [
                    cardNumber: it.no,
                    userId    : user().id,
                    expiryDate: it.expMo + it.expYr,
                    tokenType : 'JWT'
            ]
        }
    }

    @AUser(edit = true)
    @Test
    void testSuccess() {
        def root = root()
        def card = cards.find()
        root.cardNumber = card.no
        root.token = JWT.create()
                .withIssuer('ts')
                .tap { token ->
                    root.each {
                        token.withClaim(it.key, it.value)
                    }
                }
                .sign(Algorithm.HMAC256(PG_JWT_KEY))
        user().savedCards.clear()
        (user() as AlipayUser).savedCards.clear()
        req().body(root).post().then()
                .statusCode(200)
                .body('responseStatus', equalTo('SUCCESS'),
                'httpCode', equalTo('200'),
                'httpSubCode', equalTo('200'),
                'codeDetail', equalTo('Success'),
                'response', isA(Object.class))
                .root('response')
                .body('responseCode', equalTo(200),
                'responseMessage', equalTo('SUCCESS'),
                'cardId', equalTo(card.idxNo),
                'userId', equalTo(user().id),
                'cardType', isIn('DEBIT_CARD', 'CREDIT_CARD'),
                'cardScheme', isA(String.class))
        assert !user().savedCards.empty
    }
}
