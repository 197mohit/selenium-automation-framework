package scripts.api.savedcardservice

import com.paytm.apphelpers.PGPHelpers
import com.paytm.base.test.TestSetUp
import com.paytm.utils.merchant.user.annotations.AUser
import io.qameta.allure.Owner
import io.restassured.builder.RequestSpecBuilder
import io.restassured.http.ContentType
import org.hamcrest.Matchers
import org.testng.annotations.Test

import static com.paytm.LocalConfig.PGP_HOST
import static io.restassured.RestAssured.given
import static org.hamcrest.Matchers.*

@Owner('Deepak')
class AddSavedCardCache extends TestSetUp {

    def primary

    final def req = {
        given(
                new RequestSpecBuilder()
                        .addRequestSpecification(reqSpec())
                        .setContentType(ContentType.JSON)
                        .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                        .setBaseUri(PGP_HOST)
                        .setBasePath('/savedcardservice/savedCardService/v1/add/savedcard/cache')
                        .build()
        )
    }

    final def root = {
        cards.find().with {
            [
                    "cardNumber"   : "EEjsWPA5Aea5KYnsCsLN8fB9jSdlxbQqulS5gQ9v6Aw=",
                    "userId"       : user().id,
                    "status"       : "1",
                    "cardType"     : "0",
                    "expiryDate"   : "s2yh9ZiOmw1DYam/clseXg==",
                    "firstSixDigit": it.no[0..5],
                    "lastFourDigit": it.no[-4..-1],
                    "transactionId": new Random().nextLong().abs() as String
            ]
        }
    }
//
//    @AUser(edit = true)
//    @Test(enabled = false)
    void testSuccess() {
        def root = root()
        user().savedCards.clear()
        req().body(root).post().then()
                .body('responseStatus', equalTo('SUCCESS'),
                'httpCode', equalTo('200'),
                'httpSubCode', equalTo('200'),
                'codeDetail', equalTo('Success'),
                'response', equalTo(true))
        assert !user().savedCards.empty
    }
}
