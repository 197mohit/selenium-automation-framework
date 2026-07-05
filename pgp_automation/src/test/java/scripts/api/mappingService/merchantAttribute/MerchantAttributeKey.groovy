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
import static com.paytm.appconstants.Constants.MappingService.MERCHANT_ATTRIBUTE_KEY
import static io.restassured.RestAssured.given

class MerchantAttributeKey extends TestSetUp {
    private enum id {
        PAYTM("paytm"),
        ALIPAY("alipay")
        final String id

        id(String id) { this.id = id }

        @Override
        String toString() { return id }
    }


    def dbQuery(String entityId) {

        DbQueriesUtil.
                selectFromPaytmPGDB('select EKI.ENTITY_KEY, ECI.PARAMS from \n' +
                        'ENTITY_CHANNEL_INFO ECI join ENTITY_KEY_INFO EKI where ' +
                        '(ECI.ENTITY_ID = EKI.ENTITY_ID) and ECI.ENTITY_ID = ' + entityId)

    }


    protected def req() {
        given(
                new RequestSpecBuilder()
                        .addRequestSpecification(reqSpec())
                        .setContentType(ContentType.JSON)
                        .setBaseUri(PGP_HOST)
                        .setBasePath(MERCHANT_ATTRIBUTE_KEY)
                        .build()
        )
    }


    @Merchant
    @Test
    void 'test Merchant Attribute Key for paytm id with Database'() {

        def resp
        REDIS:
        {
            String redisKey = 'MID_PAYTM_ID_' + m().id
            if (STATIC_REDIS_CLUSTER().get(redisKey) != null)
                STATIC_REDIS_CLUSTER().del(redisKey)

        }
        API:
        {
            resp = req().pathParam('mid', m().id)
                    .pathParam('id', id.PAYTM)
                    .get().then().extract().jsonPath()
        }
        VALIDATION:
        {
            assert dbQuery(m().getEntityId()).find {
                it.containsValue(resp.get('aesKey')
                )
            }

        }


    }


    @Merchant
    @Test
    void 'test Merchant Attribute Key for paytm id with Redis'() {

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
        API:
        {
            resp = req().pathParam('mid', m().id)
                    .pathParam('id', id.PAYTM)
                    .get().then().extract().jsonPath()
        }
        VALIDATION:
        {
            assert dbQuery(m().getEntityId()).find {
                it.containsValue(resp.get('aesKey')
                )
            }

        }


    }


    @Merchant
    @Test
    void 'test Merchant Attribute Key for Alipay id with Database'() {

        def resp
        REDIS:
        {
            String redisKey = 'MID_ALIPAY_ID_' + m().getAlipayId()
            if (STATIC_REDIS_CLUSTER().get(redisKey) != null)
                STATIC_REDIS_CLUSTER().del(redisKey)


        }
        API:
        {
            resp = req().pathParam('mid', m().alipayId)
                    .pathParam('id', id.ALIPAY)
                    .get().then().extract().jsonPath()
        }
        VALIDATION:
        {
            assert dbQuery(m().getEntityId()).find {
                it.containsValue(resp.get('aesKey')
                )
            }

        }


    }


    @Merchant
    @Test
    void 'test Merchant Attribute Key for Alipay id with Redis'() {

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
        API:
        {
            resp = req().pathParam('mid', m().alipayId)
                    .pathParam('id', id.ALIPAY)
                    .get().then().extract().jsonPath()
        }
        VALIDATION:
        {
            assert dbQuery(m().getEntityId()).find {
                it.containsValue(resp.get('aesKey')
                )
            }

        }


    }
}
