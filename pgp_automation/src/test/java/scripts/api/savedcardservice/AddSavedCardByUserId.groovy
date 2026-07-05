package scripts.api.savedcardservice

import com.paytm.apphelpers.PGPHelpers
import com.paytm.base.test.TestSetUp
import com.paytm.utils.merchant.user.annotations.AUser
import io.qameta.allure.Owner
import io.restassured.builder.RequestSpecBuilder
import io.restassured.http.ContentType
import org.testng.annotations.Test
import scripts.api.savecardService.SaveCard

import static com.paytm.LocalConfig.PGP_HOST
import static io.restassured.RestAssured.given
import static org.hamcrest.Matchers.*

@Deprecated
@Owner('Deepak')
class AddSavedCardByUserId extends TestSetUp {

    def primary

    final def req = {
        given(
                new RequestSpecBuilder()
                        .addRequestSpecification(reqSpec())
                        .setContentType(ContentType.JSON)
                        .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                        .setBaseUri(PGP_HOST)
                        .setBasePath('/savedcardservice/savedCardService/v1/add/savedcard/userId')
                        .build()
        )
    }

    final def root = {
        cards.find().with {
            [
                    "cardNumber"   : new SaveCard().AesEncCardNumDebit,
                    "expiryDate"   : new SaveCard().AesEncExpDebit,
                    "userId"       : user().id,
                    "status"       : "1",
                    "cardType"     : "0",
                    "firstSixDigit": it.no[0..5],
                    "lastFourDigit": it.no[-4..-1]
            ]
        }
    }

    @AUser(edit = true)
    @Test(enabled=false)
    void testSuccess() {
        def root = root()
        user().savedCards.clear()
        req().body(root).post().then()
                .statusCode(200)
                .body('responseStatus', equalTo('SUCCESS'),
                'httpCode', equalTo('200'),
                'httpSubCode', equalTo('200'),
                'codeDetail', equalTo('Success'),
                'response', isA(Integer.class))
        assert !user().savedCards.empty
    }
}
