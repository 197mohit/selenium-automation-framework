package scripts.api.mappingService.merchantInfo

import com.paytm.base.test.TestSetUp
import com.paytm.framework.utils.RedisUtil
import com.paytm.utils.merchant.merchant.util.annotations.Merchant
import com.paytm.utils.merchant.util.DbQueriesUtil
import com.paytm.utils.merchant.util.PgpRedisUtil
import io.restassured.builder.RequestSpecBuilder
import io.restassured.http.ContentType
import org.testng.annotations.Test

import static com.paytm.LocalConfig.PGP_HOST
import static com.paytm.appconstants.Constants.MappingService.GET_MERCHANT_V1
import static io.restassured.RestAssured.given
import static org.hamcrest.Matchers.equalTo

class MerchantV1 extends TestSetUp {

    private enum id {
        PAYTM("paytm"),
        ALIPAY("alipay")
        final String id

        id(String id) { this.id = id }

        @Override
        String toString() { return id }
    }

    def dbQuery(String merchantId) {

        DbQueriesUtil.
                selectFromPGPDB('SELECT alipay_merchant_id,paytm_merchant_id,' +
                        'alipay_merchant_wallet_id,paytm_merchant_wallet_id,official_name,industry_type_id' +
                        ' FROM alipay_paytm_merchant WHERE alipay_merchant_id = ' + merchantId)
    }

    protected def req() {
        given(
                new RequestSpecBuilder()
                        .addRequestSpecification(reqSpec())
                        .setContentType(ContentType.JSON)
                        .setBaseUri(PGP_HOST)
                        .setBasePath(GET_MERCHANT_V1)
                        .build()
        )
    }


    @Merchant
    @Test
    void 'test Merchant V1 api for Paytm user with Database'() {

        def query
        def resp
        REDIS:
        {
            String redisKey = 'MID_P_A_' + m().id
            if (STATIC_REDIS_CLUSTER().get(redisKey) != null)
                STATIC_REDIS_CLUSTER().del(redisKey)
        }
        QUERY:
        {
            query = dbQuery(m().alipayId).get(0) as HashMap
        }

        API:
        {
            resp = req().pathParam('id', id.PAYTM)
                    .pathParam('mid', m().id)
                    .get().then()
        }
        VALIDATION:
        {
            resp.body('response.paytmId', equalTo(query.get('paytm_merchant_id')),
                    'response.alipayId', equalTo(query.get('alipay_merchant_id')),
                    'response.industryTypeId', equalTo(query.get('industry_type_id') as Integer),
                    'response.officialName', equalTo(query.get('official_name')))

        }
    }


    @Merchant
    @Test
    void 'test Merchant V1 api for Paytm user with Redis'() {

        def query
        def resp
        REDIS:
        {
            String redisKey = 'MID_P_A_' + m().id
            if (STATIC_REDIS_CLUSTER().get(redisKey) == null) {
                req().pathParam('id', id.PAYTM)
                        .pathParam('mid', m().id).get()

                assert STATIC_REDIS_CLUSTER().get(redisKey) != null

            }
        }
        QUERY:
        {
            query = dbQuery(m().alipayId).get(0) as HashMap
        }

        API:
        {
            resp = req().pathParam('id', id.PAYTM)
                    .pathParam('mid', m().id)
                    .get().then()
        }
        VALIDATION:
        {
            resp.body('response.paytmId', equalTo(query.get('paytm_merchant_id')),
                    'response.alipayId', equalTo(query.get('alipay_merchant_id')),
                    'response.industryTypeId', equalTo(query.get('industry_type_id') as Integer),
                    'response.officialName', equalTo(query.get('official_name')))

        }
    }


    @Merchant
    @Test
    void 'test Merchant V1 api for Alipay user with Database'() {

        def query
        def resp
        REDIS:
        {
            String redisKey = 'MID_A_P_' + m().getAlipayId()
            if (STATIC_REDIS_CLUSTER().get(redisKey) != null)
                STATIC_REDIS_CLUSTER().del(redisKey)
        }
        QUERY:
        {
            query = dbQuery(m().alipayId).get(0) as HashMap
        }

        API:
        {
            resp = req().pathParam('id', id.ALIPAY)
                    .pathParam('mid', m().alipayId)
                    .get().then()
        }
        VALIDATION:
        {
            resp.body('response.paytmId', equalTo(query.get('paytm_merchant_id')),
                    'response.alipayId', equalTo(query.get('alipay_merchant_id')),
                    'response.industryTypeId', equalTo(query.get('industry_type_id') as Integer),
                    'response.officialName', equalTo(query.get('official_name')))

        }
    }


    @Merchant
    @Test
    void 'test Merchant V1 api for Alipay user with Redis'() {

        def query
        def resp

        REDIS:
        {
            String redisKey = 'MID_A_P_' + m().getAlipayId()
            if (STATIC_REDIS_CLUSTER().get(redisKey) == null) {
                req().pathParam('mid', m().alipayId)
                        .pathParam('id', id.ALIPAY).get()

                assert STATIC_REDIS_CLUSTER().get(redisKey) != null

            }

        }
        QUERY:
        {
            query = dbQuery(m().alipayId).get(0) as HashMap
        }

        API:
        {
            resp = req().pathParam('id', id.ALIPAY)
                    .pathParam('mid', m().alipayId)
                    .get().then()
        }
        VALIDATION:
        {
            resp.body('response.paytmId', equalTo(query.get('paytm_merchant_id')),
                    'response.alipayId', equalTo(query.get('alipay_merchant_id')),
                    'response.industryTypeId', equalTo(query.get('industry_type_id') as Integer),
                    'response.officialName', equalTo(query.get('official_name')))

        }
    }
}