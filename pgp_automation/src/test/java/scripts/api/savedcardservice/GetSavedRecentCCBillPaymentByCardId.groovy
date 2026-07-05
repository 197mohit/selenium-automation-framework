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

@Owner('Deepak')
class GetSavedRecentCCBillPaymentByCardId extends TestSetUp {

    def primary

    final def req = { cardId ->
        given(
                new RequestSpecBuilder()
                        .addRequestSpecification(reqSpec())
                        .setContentType(ContentType.JSON)
                        .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                        .setBaseUri(PGP_HOST)
                        .setBasePath("/savedcardservice/savedCardService/v1/get/savedRecentCCBillPayment/cardId/${cardId}")
                        .build()
        )
    }

//    @AUser(edit = true)
//    @Test(enabled = false)
//TODO need req
    void testName() {
        user().savedCards.clear()
        assert user().savedCards.add(cards.find())
        req(user().savedCards.find().id).get().path('')
    }
}
