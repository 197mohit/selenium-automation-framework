package scripts.api.instantrefund

import com.paytm.apphelpers.PGPHelpers
import com.paytm.base.test.Group
import com.paytm.base.test.TestSetUp
import com.paytm.dto.PaymentDTO
import com.paytm.framework.reporting.Owners
import com.paytm.utils.merchant.user.Account
import com.paytm.utils.merchant.user.annotations.AUser
import io.qameta.allure.Link
import io.qameta.allure.Owner
import io.restassured.builder.RequestSpecBuilder
import io.restassured.builder.ResponseSpecBuilder
import io.restassured.http.ContentType
import io.restassured.specification.ResponseSpecification
import org.testng.annotations.Optional
import org.testng.annotations.Parameters
import org.testng.annotations.Test

import static com.paytm.LocalConfig.PGP_HOST
import static com.paytm.appconstants.Constants.savedCard.REFUND_ACCOUNT_REMOVE
import static io.restassured.RestAssured.given
import static org.hamcrest.Matchers.*

@Owner("Deepak")
@Owners(author = "Deepak", qa = "Pulkit")
@Test(groups = ['refund', Group.PUBLIC_API])
@Link(name = 'doc', value = 'https://wiki.mypaytm.com/display/PGP/IMPS+Refund+Integration+Solution')
class RefundAccountRemove extends TestSetUp {

    private static final String ASSET_ADD_REDIS_KEY_PREFIX = 'ASSET_ADD_'

    final def req = {
        given(
                new RequestSpecBuilder()
                        .addRequestSpecification(reqSpec())
                        .setContentType(ContentType.JSON)
                        .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                        .setBaseUri(PGP_HOST)
                        .setBasePath(REFUND_ACCOUNT_REMOVE)
                        .build()
        )
    }

    final def root = {
        [
                head: [
                        requestId       : new Random().nextLong().abs() as String,
                        requestTimestamp: System.currentTimeMillis() as String,
                        tokenType       : 'SSO',
                        token           : user()?.tokens?.getAt('sso')?.id,
                ],
                body: [
                        channelId: 'WEB',
                        assetId  : null
                ]
        ]
    }

    private final Account bankAcct = new Account('003100100030871', 'BACB0000003', 'HDFC', 'pulkit', 'agarwal')
    private final Account vpaAcct = new Account(new PaymentDTO().getVpa())

    private final static ResponseSpecification rootSchema = new ResponseSpecBuilder()
            .expectStatusCode(isIn(200, 400))
            .expectBody('', hasKey('head'))
            .expectBody('', hasKey('body'))
            .rootPath('head')
            .expectBody('responseTimestamp', notNullValue())
            .expectBody('requestId', notNullValue())
            .rootPath('body')
            .expectBody('', hasKey('resultInfo'))
            .rootPath('body.resultInfo')
            .expectBody('resultStatus', isIn('S', 'F', 'U'))
            .expectBody('resultCodeId', isIn('0000', '1001', '1002', '9999', '2011', '2007'))
            .expectBody('resultCode', isIn('SUCCESS', 'REQUEST_PARAMS_VALIDATION_EXCEPTION', 'TOKEN_VALIDATION_EXCEPTION', 'SYSTEM_ERROR', 'ASSET_NOT_EXIST', 'TARGET_NOT_FOUND'))
            .expectBody('resultMsg', isIn('Success', 'Request prameters are not valid', 'Token validation failed', 'system error', 'Target not found', 'ASSET not exist', 'Invalid ssoToken'))
            .build()

    private final static ResponseSpecification successSpec = new ResponseSpecBuilder()
            .expectStatusCode(200)
            .rootPath("body.resultInfo")
            .expectBody("resultStatus", equalTo("S"))
            .expectBody("resultCodeId", equalTo("0000"))
            .expectBody("resultCode", equalTo("SUCCESS"))
            .expectBody("resultMsg", equalTo("Success"))
            .build()

    @AUser(edit = true)
    @Test
    void checkAbleToDeleteBankAccount() {
        user().accounts.clear()
        SESSION_REDIS_CLUSTER().del(ASSET_ADD_REDIS_KEY_PREFIX + user().tokens['sso'].id)
        assert user().accounts.add(bankAcct)
        SESSION_REDIS_CLUSTER().del(ASSET_ADD_REDIS_KEY_PREFIX + user().tokens['sso'].id)
        def root = root()
        root.body.assetId = user().accounts.last().id
        req().body(root).post()
                .then().spec(rootSchema).spec(successSpec)
    }

    //TODO invalid per Pulkit
//    @AUser(edit = true)
//    @Test(enabled = false)
    void checkAbleToDeleteUpiAccount() {
        user().accounts.clear()
        SESSION_REDIS_CLUSTER().del(ASSET_ADD_REDIS_KEY_PREFIX + user().tokens['sso'].id)
        assert user().accounts.add(vpaAcct)
        SESSION_REDIS_CLUSTER().del(ASSET_ADD_REDIS_KEY_PREFIX + user().tokens['sso'].id)
        def root = root()
        root.body.assetId = user().accounts.last().id
        req().body(root).post()
                .then().spec(rootSchema).spec(successSpec)
    }

    @AUser(edit = true)
    @Parameters(['invalidToken'])
    @Test
    void checkAbleToDeleteAssetForInvalidToken(@Optional('313131') String invalidToken) {
        user().accounts.clear()
        SESSION_REDIS_CLUSTER().del(ASSET_ADD_REDIS_KEY_PREFIX + user().tokens['sso'].id)
        assert user().accounts.add(bankAcct)
        SESSION_REDIS_CLUSTER().del(ASSET_ADD_REDIS_KEY_PREFIX + user().tokens['sso'].id)
        def root = root()
        root.head.token = invalidToken
        root.body.assetId = user().accounts.last().id
        req().body(root).post()
                .then().spec(rootSchema)
                .root('body.resultInfo')
                .body("resultStatus", equalTo("F"))
                .body("resultCodeId", equalTo("1001"))
                .body("resultCode", equalTo("REQUEST_PARAMS_VALIDATION_EXCEPTION"))
                .body("resultMsg", equalTo("Invalid ssoToken"))
        assert !user().accounts.empty
    }

    @AUser(edit = true)
    @Parameters(['invalidAssetId'])
    @Test
    void testWithWrongAssetId(@Optional('1234567890') String invalidAssetId) {
        user().accounts.clear()
        assert user().accounts.add(bankAcct)
        def root = root()
        root.body.assetId = invalidAssetId
        req().body(root).post()
                .then().spec(rootSchema)
                .root('body.resultInfo')
                .body("resultStatus", equalTo("F"))
                .body("resultCodeId", equalTo("2011"))
                .body("resultCode", equalTo("ASSET_NOT_EXIST"))
                .body("resultMsg", equalTo("ASSET not exist"))
    }
}
