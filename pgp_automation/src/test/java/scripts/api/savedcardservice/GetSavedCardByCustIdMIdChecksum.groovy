package scripts.api.savedcardservice

import com.paytm.LocalConfig
import com.paytm.api.RedisAPI
import com.paytm.appconstants.Constants
import com.paytm.appconstants.FF4JFeatures
import com.paytm.apphelpers.PGPHelpers
import com.paytm.base.test.TestSetUp
import com.paytm.framework.utils.DatabaseUtil
import com.paytm.utils.merchant.merchant.util.alipay.AlipayMerchant
import com.paytm.utils.merchant.merchant.util.annotations.Merchant
import com.paytm.utils.merchant.util.PGPUtil
import io.qameta.allure.Owner
import io.restassured.builder.RequestSpecBuilder
import io.restassured.http.ContentType
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test

import static com.paytm.LocalConfig.PGP_HOST
import static io.restassured.RestAssured.given
import static org.hamcrest.Matchers.*

@Owner('Deepak')
class GetSavedCardByCustIdMIdChecksum extends TestSetUp {

    final def req = { custId, mId ->
        given(
                new RequestSpecBuilder()
                        .addRequestSpecification(reqSpec())
                        .setContentType(ContentType.JSON)
                        .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                        .setBaseUri(PGP_HOST)
                        .setBasePath("/savedcardservice/savedcardOpenAPIService/get/savedcard/custId/mId/checkSum/${custId}/${mId}/${PGPUtil.getChecksum(m().key, [custId: custId, mId: mId] as TreeMap)}")
                        .build()
        )
    }

    @Merchant(edit = true, value = { it.payModes.containsAll(['cc', 'dc']) })
    @Test
    void testSuccess() {
        (m() as AlipayMerchant).users[0].savedCards.clear()
        def card = cards.find()
        assert m().users[0].savedCards.add(card)
        assert (m() as AlipayMerchant).users[0].savedCards.add(card)
        req(m().users[0].id, m().id).get().then()
                .statusCode(200)
                .body('responseStatus', equalTo('SUCCESS'),
                'httpCode', equalTo('200'),
                'httpSubCode', equalTo('200'),
                'codeDetail', equalTo('Success'),
                'response', isA(List.class))
                .root('response[0]')
                .body(
                'cardId', anyOf(isA(Integer.class), isA(String.class)),
                'cardNumber', isA(String.class),
                'cardScheme', isIn('CC', 'DC'))
    }
}
