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
class DeleteSavedCardBySSOTokenUserIdCardId extends TestSetUp {

    def primary

    final def req = { ssoToken, userId, cardId ->
        given(
                new RequestSpecBuilder()
                        .addRequestSpecification(reqSpec())
                        .setContentType(ContentType.JSON)
                        .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                        .setBaseUri(PGP_HOST)
                        .setBasePath("/savedcardservice/savedcardOpenAPIService/delete/savedcard/ssoToken/userId/cardId/${ssoToken}/${userId}/${cardId}")
                        .build()
        )
    }

//    @AUser(edit = true)
//    @Test(enabled = false)
//TODO getting token scope is invalidated in response
    void testName() {
        user().savedCards.clear()
        assert user().savedCards.add(cards.find())
        req(user().tokens['sso'].id, user().id, user().savedCards.find().id).delete().path('')
    }
}
