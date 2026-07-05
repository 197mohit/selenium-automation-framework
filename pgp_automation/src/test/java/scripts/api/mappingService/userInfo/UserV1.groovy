package scripts.api.mappingService.userInfo

import com.paytm.base.test.TestSetUp
import com.paytm.framework.utils.RedisUtil
import com.paytm.utils.merchant.user.User
import com.paytm.utils.merchant.user.annotations.AUser
import com.paytm.utils.merchant.util.*
import io.restassured.builder.RequestSpecBuilder
import io.restassured.http.ContentType
import org.testng.annotations.Test

import static com.paytm.LocalConfig.*
import static com.paytm.appconstants.Constants.MappingService.*
import static io.restassured.RestAssured.given
import static org.hamcrest.Matchers.equalTo


class UserV1 extends TestSetUp {

    private enum id {
        PAYTM("paytm"),
        ALIPAY("alipay")
        final String id

        id(String id) { this.id = id }

        @Override
        String toString() { return id }
    }


    def dbQuery(User user) {

        DbQueriesUtil.
                selectFromPGPDB('SELECT alipay_id,paytm_id,paytm_account_id,alipay_account_id' +
                        ' FROM alipay_paytm_user WHERE alipay_Id =' + user.alipayId+' limit 1')
    }

    protected def req() {
        given(
                new RequestSpecBuilder()
                        .addRequestSpecification(reqSpec())
                        .setContentType(ContentType.JSON)
                        .setBaseUri(PGP_HOST)
                        .setBasePath(GET_USER_V1)
                        .build()
        )
    }

    @AUser
    @Test
    void 'test api UserV1 for paytm id with Database'() {

        def resp
        def dbResp


        QUERY:
        {
            dbResp = dbQuery(user()).get(0)
        }

        REDIS:
        {
            String redisKey = 'UID_P_A_' + user().id
            if (TRANSACTIONAL_REDIS_CLUSTER().get(redisKey) != null)
                TRANSACTIONAL_REDIS_CLUSTER().del(redisKey);
        }

        API:
        {      resp = req().pathParam('id', id.PAYTM)
                .pathParam('user', user().id)
                .get().then().root('response')
        }

        VALIDATION:
        {
            resp.body('paytmId',equalTo(dbResp.get('paytm_id')),
                    'alipayId', equalTo(dbResp.get('alipay_id')),
                    'paytmAccountId',equalTo(dbResp.get('paytm_account_id')),
                    'alipayAccountId' ,equalTo(dbResp.get('alipay_account_id')))

        }
    }


    @AUser
    @Test
    void 'test api UserV1 for paytm id with Redis'() {

        def resp

        def dbResp


        QUERY:
        {
            dbResp = dbQuery(user()).get(0)
        }

        REDIS:
        {
            String redisKey = 'UID_P_A_' + user().id
            if (TRANSACTIONAL_REDIS_CLUSTER().get(redisKey) == null) {
                req().pathParam('id', id.PAYTM)
                        .pathParam('user', user().id).get()

                assert TRANSACTIONAL_REDIS_CLUSTER().del(redisKey) != null
            }
        }
        API:
        {
            resp = req().pathParam('id', id.PAYTM)
                    .pathParam('user', user().id)
                    .get().then().root('response')
        }
        VALIDATION:
        {
            resp.body('paytmId',equalTo(dbResp.get('paytm_id')),
                    'alipayId', equalTo(dbResp.get('alipay_id')),
                    'paytmAccountId',equalTo(dbResp.get('paytm_account_id')),
                    'alipayAccountId' ,equalTo(dbResp.get('alipay_account_id')))

        }


    }

    @AUser
    @Test
    void 'test api UserV1 for Alipay id with Database'() {

        def resp

        def dbResp


        QUERY:
        {
            dbResp = dbQuery(user()).get(0)
        }


        REDIS:
        {
            String redisKey = 'UID_P_A_' + user().id
            if (TRANSACTIONAL_REDIS_CLUSTER().get(redisKey) != null)
                TRANSACTIONAL_REDIS_CLUSTER().del(redisKey);
        }

        API:
        {
            resp = req().pathParam('id', id.ALIPAY)
                    .pathParam('user', user().alipayId)
                    .get().then().root('response')
        }

        VALIDATION:
        {
            resp.body('paytmId',equalTo(dbResp.get('paytm_id')),
                    'alipayId', equalTo(dbResp.get('alipay_id')),
                    'paytmAccountId',equalTo(dbResp.get('paytm_account_id')),
                    'alipayAccountId' ,equalTo(dbResp.get('alipay_account_id')))

        }

    }


    @AUser
    @Test
    void 'test api UserV1 for Alipay id with Redis'() {

        def resp

        def dbResp


        QUERY:
        {
            dbResp = dbQuery(user()).get(0)
        }


        REDIS:
        {
            String redisKey = 'UID_P_A_' + user().id
            if (TRANSACTIONAL_REDIS_CLUSTER().get(redisKey) == null) {
                req().pathParam('id', id.ALIPAY)
                        .pathParam('user', user().alipayId).get()

                assert TRANSACTIONAL_REDIS_CLUSTER().get(redisKey) != null
            }
        }
        API:
        {
            resp = req().pathParam('id', id.ALIPAY)
                    .pathParam('user', user().alipayId)
                    .get().then().root('response')
        }
        VALIDATION:
        {
            resp.body('paytmId',equalTo(dbResp.get('paytm_id')),
                    'alipayId', equalTo(dbResp.get('alipay_id')),
                    'paytmAccountId',equalTo(dbResp.get('paytm_account_id')),
                    'alipayAccountId' ,equalTo(dbResp.get('alipay_account_id')))

        }

    }


}