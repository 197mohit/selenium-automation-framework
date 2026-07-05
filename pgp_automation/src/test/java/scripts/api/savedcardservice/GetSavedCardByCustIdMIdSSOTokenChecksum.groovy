package scripts.api.savedcardservice

import com.paytm.apphelpers.PGPHelpers
import com.paytm.base.test.TestSetUp
import com.paytm.utils.merchant.merchant.util.annotations.Merchant
import com.paytm.utils.merchant.user.annotations.AUser
import com.paytm.utils.merchant.util.PGPUtil
import io.qameta.allure.Owner
import io.restassured.builder.RequestSpecBuilder
import io.restassured.http.ContentType
import org.testng.annotations.Test

import static com.paytm.LocalConfig.PGP_HOST
import static io.restassured.RestAssured.given
import static org.hamcrest.Matchers.*

@Owner('Deepak')
class GetSavedCardByCustIdMIdSSOTokenChecksum extends TestSetUp {

    def primary

    final def req = { custId, mId, ssoToken ->
        given(
                new RequestSpecBuilder()
                        .addRequestSpecification(reqSpec())
                        .setContentType(ContentType.JSON)
                        .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                        .setBaseUri(PGP_HOST)
                        .setBasePath("/savedcardservice/savedcardOpenAPIService/get/savecard/custId/mId/ssoToken/checkSum/${custId}/${mId}/${ssoToken}/${PGPUtil.getChecksum(m().key, [custId: custId, mId: mId, ssoToken: ssoToken] as TreeMap)}")
                        .build()
        )
    }

    @Merchant
    @AUser(edit = true)
    @Test
    void testSuccess() {
        user().savedCards.clear()
        assert user().savedCards.add(cards.find())
        req('test', m().id, user().tokens['sso'].id).get().then()
                .statusCode(200)
                .body('responseStatus', equalTo('SUCCESS'),
                'httpCode', equalTo('200'),
                'httpSubCode', equalTo('200'),
                'codeDetail', equalTo('Success'),
                'response', isA(List.class))
                .root('response[0]')
                .body(
                'cardId', isA(Integer.class),
                'cardNumber', isA(String.class),
                'cardScheme', isIn('CC', 'DC'))
    }
}
