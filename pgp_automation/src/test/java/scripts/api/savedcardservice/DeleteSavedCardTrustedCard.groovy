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
class DeleteSavedCardTrustedCard extends TestSetUp {

    def primary

    final def req = {
        given(
                new RequestSpecBuilder()
                        .addRequestSpecification(reqSpec())
                        .setContentType(ContentType.JSON)
                        .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                        .setBaseUri(PGP_HOST)
                        .setBasePath('/savedcardservice/savedCardService/v1/delete/savedcard/trustedCard')
                        .build()
        )
    }

    final def root = {
        [
                cardNumber: null,
                userId    : user().id,
                expiryDate: null,
        ]
    }

    @AUser(edit = true)
    @Test
    void testSuccess() {
        def root = root()
        def card = cards.find()
        user().savedCards.clear()
        assert user().savedCards.add(card)
        root.cardNumber = card.no
        root.expiryDate = card.expMo + card.expYr
        req().body(root).post().then()
                .statusCode(200)
                .body('responseStatus', equalTo('SUCCESS'),
                'httpCode', equalTo('200'),
                'httpSubCode', equalTo('200'),
                'codeDetail', equalTo('Success'),
                'response', isA(String.class))
    }
}
