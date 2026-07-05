package scripts.api.mappingService.merchantInfo

import com.paytm.base.test.TestSetUp
import com.paytm.framework.utils.RedisUtil
import com.paytm.utils.merchant.merchant.util.annotations.Merchant
import com.paytm.utils.merchant.util.DbQueriesUtil
import com.paytm.utils.merchant.util.PgpRedisUtil
import io.restassured.builder.RequestSpecBuilder
import io.restassured.builder.ResponseSpecBuilder
import io.restassured.http.ContentType
import io.restassured.specification.ResponseSpecification
import org.testng.annotations.Test

import static com.paytm.LocalConfig.PGP_HOST
import static com.paytm.api.MappingService.MappingAlipayApi.getMerchantProfile
import static com.paytm.appconstants.Constants.MappingService.MERCHANT_PROFILE
import static io.restassured.RestAssured.given
import static io.restassured.RestAssured.responseSpecification
import static org.hamcrest.Matchers.*

class MerchantProfile extends TestSetUp {

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
                        .setBasePath(MERCHANT_PROFILE)
                        .build()
        )
    }

    protected ResponseSpecification schema() {
               new ResponseSpecBuilder()
                        .expectStatusCode(200)
                        .expectContentType(ContentType.JSON)
                        .expectBody('merchantId',allOf(isA(String.class), notNullValue()))
                       .expectBody('paytmId',allOf(isA(String.class), notNullValue()))
                       .expectBody('isAggregatorMerchant',allOf(isA(Boolean.class), notNullValue()))
                       .expectBody('aesKey',allOf(isA(String.class), notNullValue()))
                       .expectBody('category',allOf(isA(String.class), notNullValue()))
                       .expectBody('certifyStatus',allOf(isA(String.class), notNullValue()))
                       .expectBody('offlinePostConvenience',allOf(isA(Boolean.class), notNullValue()))
                       .expectBody('mcc',allOf(isA(String.class)))
                        .build()
            }




    protected ResponseSpecification errorResp(String Id) {
        new ResponseSpecBuilder()
                .expectStatusCode(417)
                .expectContentType(ContentType.JSON)
                .expectBody('code',allOf(isA(Integer.class), equalTo(417)))
                .expectBody('message',allOf(isA(String.class), equalTo('result not found when find Merchant by '+Id)))
                .expectBody('offendingPath',allOf(isA(String.class), equalTo('MerchantIdentityAdapterImpl.javacom.paytm.pgplus.mapping.service.adapter.impl.MerchantIdentityAdapterImpl')))
                .expectBody('offendingMethod',allOf(isA(String.class), equalTo('fetchFromPGPlusDataSource')))
                .expectBody('offendingLine',allOf(isA(Integer.class)))
                .build()
    }


    @Merchant
    @Test
    void 'test api merchant profile for paytm id with Database'() {

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
            query = getMerchantProfile(m().getAlipayId())
        }
        API:
        {
            resp = req().pathParam('mid', m().id)
                    .pathParam('id', id.PAYTM)
                    .get().then()
        }
        VALIDATION:
        {
            resp.body('merchantId', equalTo(query.get('merchantId')),
                    'officialName', equalTo(query.get('officialName')),
                    'officeAddress.cityName', equalTo(query.get('officeAddress.cityName')),
                    'merchantType', equalTo(query.get('merchantType')),
                    'contactEmail.email', equalTo(query.get('contactEmail.email')))

        }
    }


    @Merchant
    @Test
    void 'test api merchant profile for paytm id with Redis'() {

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
            query = getMerchantProfile(m().getAlipayId())
        }
        API:
        {
            resp = req().pathParam('mid', m().id)
                    .pathParam('id', id.PAYTM)
                    .get().then()
        }
        VALIDATION:
        {
            resp.body('merchantId', equalTo(query.get('merchantId')),
                    'officialName', equalTo(query.get('officialName')),
                    'officeAddress.cityName', equalTo(query.get('officeAddress.cityName')),
                    'merchantType', equalTo(query.get('merchantType')),
                    'contactEmail.email', equalTo(query.get('contactEmail.email')))

        }
    }

    @Merchant
    @Test
    void 'test api merchant profile for alipay id with database'() {

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
            query = getMerchantProfile(m().getAlipayId())
        }
        API:
        {
            resp = req().pathParam('mid', m().alipayId)
                    .pathParam('id', id.ALIPAY)
                    .get().then()
        }
        VALIDATION:
        {
            resp.body('merchantId', equalTo(query.get('merchantId')),
                    'officialName', equalTo(query.get('officialName')),
                    'officeAddress.cityName', equalTo(query.get('officeAddress.cityName')),
                    'merchantType', equalTo(query.get('merchantType')),
                    'contactEmail.email', equalTo(query.get('contactEmail.email')))

        }
    }


    @Merchant
    @Test
    void 'test api merchant profile for alipay id with redis'() {

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
            query = getMerchantProfile(m().getAlipayId())
        }
        API:
        {
            resp = req().pathParam('mid', m().alipayId)
                    .pathParam('id', id.ALIPAY)
                    .get().then()
        }
        VALIDATION:
        {
            resp.body('merchantId', equalTo(query.get('merchantId')),
                    'officialName', equalTo(query.get('officialName')),
                    'officeAddress.cityName', equalTo(query.get('officeAddress.cityName')),
                    'merchantType', equalTo(query.get('merchantType')),
                    'contactEmail.email', equalTo(query.get('contactEmail.email')))

        }
    }


    @Merchant
    @Test
    void 'test New Offline Fields Added to Mapping API when id is alipay'() {
        req().pathParam('mid', m().alipayId)
                .pathParam('id', id.ALIPAY)
                .get().then().spec(schema())
    }

    @Merchant
    @Test
    void 'test New Offline Fields Added to Mapping API when id is paytm'() {
        req().pathParam('mid', m().alipayId)
                .pathParam('id', id.ALIPAY)
                .get().then().spec(schema())
    }

    @Merchant
    @Test
    void 'test Incorrect MID is Passed when id is paytm'() {
        req().pathParam('mid', "HydRid12ddaa4332n")
                .pathParam('id', id.PAYTM)
                .get().then().spec(errorResp("paytm"))
    }


    @Merchant
    @Test
    void 'test Incorrect MID is Passed when id is alipay'() {
        req().pathParam('mid', "100013233211234")
                .pathParam('id', id.ALIPAY)
                .get().then().spec(errorResp('alipayId'))
    }


    @Merchant
    @Test
    void 'test correct alipay MID is Passed when id is paytm'() {
        req().pathParam('mid', m().alipayId)
                .pathParam('id', id.PAYTM)
                .get().then().spec(errorResp("paytm"))
    }


    @Merchant
    @Test
    void 'test correct paytm MID is Passed when id is alipay'() {
        req().pathParam('mid', m().id)
                .pathParam('id', id.ALIPAY)
                .get().then().spec(errorResp('alipayId'))
    }




    @Merchant
    @Test
    void 'test QR components in Merchant Profile from Database when Id is paytm'() {

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
            query = dbQuery(m().entityId)
        }
        API:
        {
            resp = req().pathParam('mid', m().alipayId)
                    .pathParam('id', id.ALIPAY)
                    .get().then()
        }
        VALIDATION:
        {
            resp.body('aesKey',equalTo(query.findAll{it.PARAMS}.flatten {it.ENTITY_KEY}[0]))
        }
    }









}
