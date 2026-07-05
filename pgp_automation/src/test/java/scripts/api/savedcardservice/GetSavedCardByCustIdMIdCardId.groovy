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
import io.qameta.allure.Owner
import io.restassured.builder.RequestSpecBuilder
import io.restassured.http.ContentType
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test

import static com.paytm.LocalConfig.PGP_HOST
import static io.restassured.RestAssured.given
import static org.hamcrest.Matchers.*

@Owner('Deepak')
class GetSavedCardByCustIdMIdCardId extends TestSetUp {

    final def req = { custId, mId, cardId ->
        given(
                new RequestSpecBuilder()
                        .addRequestSpecification(reqSpec())
                        .setContentType(ContentType.JSON)
                        .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                        .setBaseUri(PGP_HOST)
                        .setBasePath("/savedcardservice/savedCardService/v1/get/savedcard/custId/mId/cardId/${custId}/${mId}/${cardId}")
                        .build()
        )
    }

    @BeforeClass
    void disableSCFetchFromPlatFormForMidFlag() {
        String queryForExp = "UPDATE FF4J_FEATURES SET ENABLE = 0 WHERE FEAT_UID = '${FF4JFeatures.SC_FETCH_FROM_PLATFORM_FOR_MID}';";
        DatabaseUtil.getInstance().executeUpdateQuery(LocalConfig.PGP_DB_CONNECTION_URL, queryForExp);
        RedisAPI.deleteKey("FF4J_FEATURE_${FF4JFeatures.SC_FETCH_FROM_PLATFORM_FOR_MID}")
        Thread.sleep(Constants.FF4J_INTERNAL_CACHE_TIMEOUT)
    }

    @Merchant(edit = true, value = { it.payModes.containsAll(['cc', 'dc']) })
    @Test
    //TODO - https://jira.mypaytm.com/browse/PGP-38345
    void testSuccess() {
        (m() as AlipayMerchant).users[0].savedCards.clear()
        def card = cards.find()
        assert m().users[0].savedCards.add(card)
        assert (m() as AlipayMerchant).users[0].savedCards.add(card)
        req(m().users[0].id, m().id, m().users[0].savedCards[0].id).get().then()
                .statusCode(200)
                .body('responseStatus', equalTo('SUCCESS'),
                'httpCode', equalTo('200'),
                'httpSubCode', equalTo('200'),
                'codeDetail', equalTo('Success'),
                'response', isA(Object.class))
                .root('response')
                .body('cardId', isA(Integer.class),
                'cardNumber', isA(String.class),
                'cardType', isA(Integer.class),
                'expiryDate', isA(String.class),
                'firstSixDigit', equalTo(card.no[0..5]),
                'lastFourDigit', equalTo(card.no[-4..-1]),
                'status', isIn(0, 1),
                'updated_on', isA(Long.class),
                'created_on', isA(Long.class),
                'mId', equalTo(m().id),
                'custId', equalTo(m().users[0].id),
                'cardScheme', isIn('DEBIT_CARD', 'CREDIT_CARD'),
                'bankName', isA(String.class))
    }
}
