package scripts.api.mappingService.merchantAttribute

import com.paytm.base.test.TestSetUp
import com.paytm.framework.utils.RedisUtil
import com.paytm.utils.merchant.merchant.util.annotations.Merchant
import com.paytm.utils.merchant.util.DbQueriesUtil
import com.paytm.utils.merchant.util.PgpRedisUtil
import io.restassured.builder.RequestSpecBuilder
import io.restassured.http.ContentType
import org.testng.annotations.Test

import static com.paytm.LocalConfig.PGP_HOST
import static com.paytm.appconstants.Constants.MappingService.MERCHANT_ATTRIBUTE_UPIKEYS
import static com.paytm.appconstants.Constants.MerchantType.MGV_AGGREGATOR_CHILD
import static io.restassured.RestAssured.given
import static org.hamcrest.Matchers.equalTo

class MerchantAttributeUPIKeys extends TestSetUp {


    def DbQuery(String entityId) {

        def resultParams = DbQueriesUtil.
                selectFromPaytmPGDB('select PARAMS from ENTITY_CHANNEL_INFO where ENTITY_ID=' + entityId + ' and ' +
                        'PARAMS like \'%MERCHANT_VPA%\' ORDER BY ID DESC limit 1')
        HashMap<String, String> keysData = new HashMap()
        String[] splitParam = ((String) resultParams.get(0).get('PARAMS')).split(';')
        for (String splitParamData : splitParam) {
            String[] splitData = splitParamData.trim().split("=")
            if (splitData.length == 2) {
                keysData.put(splitData[0], splitData[1])
            }
        }
        return keysData
    }

    protected def req() {
        given(
                new RequestSpecBuilder()
                        .addRequestSpecification(reqSpec())
                        .setContentType(ContentType.JSON)
                        .setBaseUri(PGP_HOST)
                        .setBasePath(MERCHANT_ATTRIBUTE_UPIKEYS)
                        .build()
        )
    }

    /*  Used Specified Merchant  So that only one specific key is created in Redis
    as Data can be fetched from other keys corresponds to that merchanti n other txn
      * */


    @Merchant(edit = true, value = { it.id == MGV_AGGREGATOR_CHILD.id })
    @Test
    void 'test merchant UPI keys paytm with Database'() {

        def query
        def resp
        REDIS:
        {
            String redisKey = 'MID_A_P_' + m().alipayId
            if (STATIC_REDIS_CLUSTER().get(redisKey) != null)
                STATIC_REDIS_CLUSTER().del(redisKey)
        }
        QUERY:
        {
            query = DbQuery(m().getEntityId())
        }
        API:
        {
            resp = req().pathParam('mid', m().id)
                    .get().then()
        }
        VALIDATION:
        {
            resp.body('merchantVpa', equalTo(query.get('MERCHANT_VPA')),
                    'mcc', equalTo(query.get('MCC')))
        }
    }


    @Merchant(edit = true, value = { it.id == MGV_AGGREGATOR_CHILD.id })
    @Test
    void 'test merchant UPI keys paytm with Redis'() {

        def query
        def resp
        REDIS:
        {
            String redisKey = 'MID_P_A_' + m().id
            if (STATIC_REDIS_CLUSTER().get(redisKey) == null) {
                req().pathParam('mid', m().id).get()

                assert STATIC_REDIS_CLUSTER().get(redisKey) != null

            }
        }
        QUERY:
        {
            query = DbQuery(m().getEntityId())
        }
        API:
        {
            resp = req().pathParam('mid', m().id)
                    .get().then()
        }
        VALIDATION:
        {
            resp.body('merchantVpa', equalTo(query.get('MERCHANT_VPA')),
                    'mcc', equalTo(query.get('MCC')))
        }
    }
}
