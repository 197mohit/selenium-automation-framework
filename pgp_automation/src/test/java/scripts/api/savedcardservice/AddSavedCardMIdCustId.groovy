package scripts.api.savedcardservice

import com.paytm.LocalConfig
import com.paytm.api.RedisAPI
import com.paytm.appconstants.Constants
import com.paytm.appconstants.FF4JFeatures
import com.paytm.apphelpers.PGPHelpers
import com.paytm.base.test.TestSetUp
import com.paytm.framework.utils.DatabaseUtil
import com.paytm.utils.merchant.merchant.util.annotations.Merchant
import io.qameta.allure.Owner
import io.restassured.builder.RequestSpecBuilder
import io.restassured.http.ContentType
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test

import static com.paytm.LocalConfig.PGP_HOST
import static io.restassured.RestAssured.given
import static org.hamcrest.Matchers.*

@Deprecated
@Owner('Deepak')
class AddSavedCardMIdCustId extends TestSetUp {

    def primary

    final def req = {
        given(
                new RequestSpecBuilder()
                        .addRequestSpecification(reqSpec())
                        .setContentType(ContentType.JSON)
                        .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                        .setBaseUri(PGP_HOST)
                        .setBasePath('/savedcardservice/savedCardService/v1/add/savecard/mId/custId')
                        .build()
        )
    }

    final def root = {
        cards.find().with {
            [
                    "paymentTypeId": "0",
                    "cardNumber"   : it.no,
                    "expiryDate"   : it.expMo + it.expYr,
                    "mId"          : m().id,
                    "firstSixDigit": it.no[0..5],
                    "lastFourDigit": it.no[-4..-1],
                    "custId"       : m().users[0].id
            ]
        }
    }

    @BeforeClass
    void disableSCFetchFromPlatFormForMidFlag() {
        String queryForExp = "UPDATE FF4J_FEATURES SET ENABLE = 0 WHERE FEAT_UID = '${FF4JFeatures.SC_FETCH_FROM_PLATFORM_FOR_MID}';";
        DatabaseUtil.getInstance().executeUpdateQuery(LocalConfig.PGP_DB_CONNECTION_URL, queryForExp);
        RedisAPI.deleteKey("FF4J_FEATURE_${FF4JFeatures.SC_FETCH_FROM_PLATFORM_FOR_MID}")
        Thread.sleep(Constants.FF4J_INTERNAL_CACHE_TIMEOUT)
    }

//    @Merchant(edit = true, value = { it.payModes.containsAll(['cc', 'dc']) })
//    @Test(enabled = false)
    void testSuccess() {
        def root = root()
        m().users[0].savedCards.clear()
        req().body(root).post().then()
                .body('responseStatus', equalTo('SUCCESS'),
                'httpCode', equalTo('200'),
                'httpSubCode', equalTo('200'),
                'codeDetail', equalTo('Success'),
                'response', isA(Integer.class))
        assert !m().users[0].savedCards.empty
    }

}
