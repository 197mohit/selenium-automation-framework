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

@Deprecated
@Owner('Deepak')
class DeleteSavedCard extends TestSetUp {

    def primary

    final def req = {
        given(
                new RequestSpecBuilder()
                        .addRequestSpecification(reqSpec())
                        .setContentType(ContentType.JSON)
                        .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                        .setBaseUri(PGP_HOST)
                        .setBasePath('/savedcardservice/savedCardService/deleteSavedCard')
                        .build()
        )
    }

    final def root = {
        [
                cardNumber: null,
                expiryDate: null,
                userId    : user().id,
        ]
    }

//    @AUser(edit = true)
//    @Test(enabled = false)
    void testSuccess() {
        def root = root()
        def card = cards.find()
        user().savedCards.clear()
        assert user().savedCards.add(card)
        root.cardNumber = card.no
        root.expiryDate = card.expMo + card.expYr
        root.userId = user().id
        req().body(root).post().then()
                .statusCode(200)
                .body('responseStatus', equalTo('FAILURE'),
                'httpCode', equalTo('400'),
                'httpSubCode', equalTo('410'),
                'codeDetail', equalTo('Card is already deactivated'),
                'response', equalTo(user().id))
    }
}
