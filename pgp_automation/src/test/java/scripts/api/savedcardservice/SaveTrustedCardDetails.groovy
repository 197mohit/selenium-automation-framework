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

@Deprecated
@Owner('Deepak')
class SaveTrustedCardDetails extends TestSetUp {

    def primary

    final def req = {
        given(
                new RequestSpecBuilder()
                        .addRequestSpecification(reqSpec())
                        .setContentType(ContentType.JSON)
                        .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                        .setBaseUri(PGP_HOST)
                        .setBasePath("/savedcardservice/savedCardService/saveTrustedCardDetails")
                        .build()
        )
    }

    final def root = {
        cards.find().with {
            [
                    cardNumber: it.no,
                    userId    : user().id,
                    expiryDate: it.expMo + it.expYr,
            ]
        }
    }

//    @AUser(edit = true)
//    @Test(enabled = false)
//exception occurred while encrypting or decrypting card details
    void testSuccess() {
        def root = root()
        user().savedCards.clear()
        req().body(root).post().path('')
    }
}
