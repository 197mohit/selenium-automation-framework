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
import static com.paytm.appconstants.Constants.MappingService.GET_MERCHANT_IDMAP
import static io.restassured.RestAssured.given
import static org.hamcrest.Matchers.equalTo

class MerchantIdMap extends TestSetUp {
    private enum id {
        PAYTM("paytm"),
        ALIPAY("alipay")
        final String id

        id(String id) { this.id = id }

        @Override
        String toString() { return id }
    }

    def dbQuery(String merchantId) {


        HashMap idmap = DbQueriesUtil.
                selectFromPaytmPGDB('select ID, SSO_ID from ENTITY_INFO where MID =' + '"' + merchantId + '"')
        HashMap merchantInfo = DbQueriesUtil.
                selectFromPGPDB('select alipay_merchant_id,paytm_merchant_id,official_name, ' +
                        'industry_type_id,merchant_type from alipay_paytm_merchant where paytm_merchant_id =' + '"' + merchantId + '"')
        merchantInfo.putAll(idmap)

        return merchantInfo

    }

    protected def req() {
        given(
                new RequestSpecBuilder()
                        .addRequestSpecification(reqSpec())
                        .setContentType(ContentType.JSON)
                        .setBaseUri(PGP_HOST)
                        .setBasePath(GET_MERCHANT_IDMAP)
                        .build()
        )
    }


    @Merchant
    @Test
    void 'test MerchantIdMap Api when user Paytm with Database'() {

        def query
        def resp
        REDIS:
        {
            String redisKey = 'MID_PAYTM_ID_' + m().id
            if (STATIC_REDIS_CLUSTER().get(redisKey) != null)
                STATIC_REDIS_CLUSTER().del(redisKey)
        }
        QUERY:
        {
            query = dbQuery(m().id)
        }
        API:
        {
            resp = req().pathParam('mid', m().id)
                    .pathParam('id', id.PAYTM)
                    .get().then()
        }
        VALIDATION:
        {
            resp.body('paytmId', equalTo(query.get('paytm_merchant_id')),
                    'alipayId', equalTo(query.get('alipay_merchant_id')),
                    'ssoId', equalTo(query.get('SSO_ID') as String),
                    'officialName', equalTo(query.get('official_name')),
                    'merchantType', equalTo(query.get('merchant_type')),
                    'industryTypeId', equalTo(query.get('industry_type_id') as Integer),
                    'enityId', equalTo(query.get('ID') as Integer))

        }
    }


    @Merchant
    @Test
    void 'test MerchantIdMap Api when user Paytm with Redis'() {

        def query
        def resp

        REDIS:
        {
            String redisKey = 'MID_PAYTM_ID_' + m().id
            if (STATIC_REDIS_CLUSTER().get(redisKey) == null) {
                req().pathParam('mid', m().id)
                        .pathParam('id', id.PAYTM).get()

                assert STATIC_REDIS_CLUSTER().get(redisKey) != null

            }
        }
        QUERY:
        {
            query = dbQuery(m().id)
        }
        API:
        {
            resp = req().pathParam('mid', m().id)
                    .pathParam('id', id.PAYTM)
                    .get().then()
        }
        VALIDATION:
        {
            resp.body('paytmId', equalTo(query.get('paytm_merchant_id')),
                    'alipayId', equalTo(query.get('alipay_merchant_id')),
                    'ssoId', equalTo(query.get('SSO_ID') as String),
                    'officialName', equalTo(query.get('official_name')),
                    'merchantType', equalTo(query.get('merchant_type')),
                    'industryTypeId', equalTo(query.get('industry_type_id') as Integer),
                    'enityId', equalTo(query.get('ID') as Integer))

        }
    }


    @Merchant
    @Test
    void 'test MerchantIdMap Api when user Alipay with Database'() {

        def query
        def resp
        REDIS:
        {
            String redisKey = 'MID_ALIPAY_ID_' + m().getAlipayId()
            if (STATIC_REDIS_CLUSTER().get(redisKey) != null)
                STATIC_REDIS_CLUSTER().del(redisKey)
        }
        QUERY:
        {
            query = dbQuery(m().id)
        }
        API:
        {
            resp = req().pathParam('mid', m().alipayId)
                    .pathParam('id', id.ALIPAY)
                    .get().then()
        }
        VALIDATION:
        {
            resp.body('paytmId', equalTo(query.get('paytm_merchant_id')),
                    'alipayId', equalTo(query.get('alipay_merchant_id')),
                    'ssoId', equalTo(query.get('SSO_ID') as String),
                    'officialName', equalTo(query.get('official_name')),
                    'merchantType', equalTo(query.get('merchant_type')),
                    'industryTypeId', equalTo(query.get('industry_type_id') as Integer),
                    'enityId', equalTo(query.get('ID') as Integer))

        }
    }


    @Merchant
    @Test
    void 'test MerchantIdMap Api when user Alipay with Redis'() {

        def query
        def resp

        REDIS:
        {
            String redisKey = 'MID_ALIPAY_ID_' + m().getAlipayId()
            if (STATIC_REDIS_CLUSTER().get(redisKey) == null) {
                req().pathParam('mid', m().alipayId)
                        .pathParam('id', id.ALIPAY).get()

                assert STATIC_REDIS_CLUSTER().get(redisKey) != null

            }
        }
        QUERY:
        {
            query = dbQuery(m().id)
        }
        API:
        {
            resp = req().pathParam('mid', m().alipayId)
                    .pathParam('id', id.ALIPAY)
                    .get().then()
        }
        VALIDATION:
        {
            resp.body('paytmId', equalTo(query.get('paytm_merchant_id')),
                    'alipayId', equalTo(query.get('alipay_merchant_id')),
                    'ssoId', equalTo(query.get('SSO_ID') as String),
                    'officialName', equalTo(query.get('official_name')),
                    'merchantType', equalTo(query.get('merchant_type')),
                    'industryTypeId', equalTo(query.get('industry_type_id') as Integer),
                    'enityId', equalTo(query.get('ID') as Integer))

        }
    }

}
