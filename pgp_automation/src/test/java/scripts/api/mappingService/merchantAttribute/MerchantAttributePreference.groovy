package scripts.api.mappingService.merchantAttribute

import com.paytm.base.test.TestSetUp
import com.paytm.framework.utils.RedisUtil
import com.paytm.utils.merchant.merchant.util.annotations.Merchant
import com.paytm.utils.merchant.util.PgpRedisUtil
import io.restassured.builder.RequestSpecBuilder
import io.restassured.http.ContentType
import org.testng.annotations.Test

import static com.paytm.LocalConfig.PGP_HOST
import static com.paytm.api.MappingService.MappingAlipayApi.getMerchantAttribute
import static com.paytm.appconstants.Constants.MappingService.MERCHANT_ATTRIBUTE_PREFERENCE
import static io.restassured.RestAssured.given
import static org.hamcrest.Matchers.equalTo

class MerchantAttributePreference extends TestSetUp {

    private enum id {
        PAYTM("paytm"),
        ALIPAY("alipay")
        final String id

        id(String id) { this.id = id }

        @Override
        String toString() { return id }
    }


    protected def req() {
        given(
                new RequestSpecBuilder()
                        .addRequestSpecification(reqSpec())
                        .setContentType(ContentType.JSON)
                        .setBaseUri(PGP_HOST)
                        .setBasePath(MERCHANT_ATTRIBUTE_PREFERENCE)
                        .build()
        )
    }


    @Merchant
    @Test
    void 'test Merchant Attribute Preference Api when for Paytm id with Database'() {

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
            query = getMerchantAttribute(m().alipayId)
        }
        API:
        {
            resp = req().pathParam('mid', m().id)
                    .pathParam('id', id.PAYTM)
                    .get().then()
        }
        VALIDATION:
        {
            resp.body('offlineMerchant', equalTo(query['offlineMerchant']),
                    'enableQrTag', equalTo(query['offlineMerchant']),
                    'walletOnlyMerchant', equalTo(query['walletOnlyMerchant']),
                    'postConvenience', equalTo(query['postConvenience']),
                    'autoAddMoney', equalTo(query['autoAddMoney']),
                    'exemptServiceTax', equalTo(query['exemptServiceTax']),
                    'pccEnabled', equalTo(query['pccEnabled']),
                    'withdrawOTPEnable', equalTo(query['withdrawOTPEnable']),
                    'withdrawPushEnable', equalTo(query['withdrawPushEnable']))
        }
    }

    @Merchant
    @Test
    void 'test Merchant Attribute Preference Api when for Paytm id with Redis'() {

        def query
        def resp
        REDIS:
        {
            String redisKey = 'MID_ALIPAY_ID_' + m().getAlipayId()
            if (STATIC_REDIS_CLUSTER().get(redisKey) == null) {
                req().pathParam('mid', m().id)
                        .pathParam('id', id.PAYTM).get()

                assert STATIC_REDIS_CLUSTER().get(redisKey) != null

            }
        }
        QUERY:
        {
            query = getMerchantAttribute(m().alipayId)
        }
        API:
        {
            resp = req().pathParam('mid', m().id)
                    .pathParam('id', id.PAYTM)
                    .get().then()
        }
        VALIDATION:
        {
            resp.body('offlineMerchant', equalTo(query['offlineMerchant']),
                    'enableQrTag', equalTo(query['offlineMerchant']),
                    'walletOnlyMerchant', equalTo(query['walletOnlyMerchant']),
                    'postConvenience', equalTo(query['postConvenience']),
                    'autoAddMoney', equalTo(query['autoAddMoney']),
                    'exemptServiceTax', equalTo(query['exemptServiceTax']),
                    'pccEnabled', equalTo(query['pccEnabled']),
                    'withdrawOTPEnable', equalTo(query['withdrawOTPEnable']),
                    'withdrawPushEnable', equalTo(query['withdrawPushEnable']))

        }
    }

    @Merchant
    @Test
    void 'test Merchant Attribute Preference Api when for Alipay id with Database'() {

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
            query = getMerchantAttribute(m().alipayId)
        }
        API:
        {
            resp = req().pathParam('mid', m().alipayId)
                    .pathParam('id', id.ALIPAY)
                    .get().then()
        }
        VALIDATION:
        {
            resp.body('offlineMerchant', equalTo(query['offlineMerchant']),
                    'enableQrTag', equalTo(query['offlineMerchant']),
                    'walletOnlyMerchant', equalTo(query['walletOnlyMerchant']),
                    'postConvenience', equalTo(query['postConvenience']),
                    'autoAddMoney', equalTo(query['autoAddMoney']),
                    'exemptServiceTax', equalTo(query['exemptServiceTax']),
                    'pccEnabled', equalTo(query['pccEnabled']),
                    'withdrawOTPEnable', equalTo(query['withdrawOTPEnable']),
                    'withdrawPushEnable', equalTo(query['withdrawPushEnable']))
        }
    }


    @Merchant
    @Test
    void 'test Merchant Attribute Preference Api when for Alipay id with Redis'() {

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
            query = getMerchantAttribute(m().alipayId)
        }
        API:
        {
            resp = req().pathParam('mid', m().alipayId)
                    .pathParam('id', id.ALIPAY)
                    .get().then()
        }
        VALIDATION:
        {
            resp.body('offlineMerchant', equalTo(query['offlineMerchant']),
                    'enableQrTag', equalTo(query['offlineMerchant']),
                    'walletOnlyMerchant', equalTo(query['walletOnlyMerchant']),
                    'postConvenience', equalTo(query['postConvenience']),
                    'autoAddMoney', equalTo(query['autoAddMoney']),
                    'exemptServiceTax', equalTo(query['exemptServiceTax']),
                    'pccEnabled', equalTo(query['pccEnabled']),
                    'withdrawOTPEnable', equalTo(query['withdrawOTPEnable']),
                    'withdrawPushEnable', equalTo(query['withdrawPushEnable']))

        }
    }
}
