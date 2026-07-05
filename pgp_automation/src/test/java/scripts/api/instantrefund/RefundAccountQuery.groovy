package scripts.api.instantrefund

import com.paytm.apphelpers.PGPHelpers
import com.paytm.base.test.Group
import com.paytm.base.test.TestSetUp
import com.paytm.dto.PaymentDTO
import com.paytm.framework.reporting.Owners
import com.paytm.utils.merchant.user.Account
import com.paytm.utils.merchant.user.User
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
import static com.paytm.appconstants.Constants.savedCard.REFUND_ACCOUNT_QUERY
import static io.restassured.RestAssured.given
import static org.hamcrest.Matchers.*

@Owner("Deepak")
@Owners(author = "Deepak", qa = "Pulkit")
@Test(groups = ['refund', Group.PUBLIC_API])
@Link(name = 'doc', value = 'https://wiki.mypaytm.com/display/PGP/IMPS+Refund+Integration+Solution')
class RefundAccountQuery extends TestSetUp {

    private static final String ASSET_ADD_REDIS_KEY_PREFIX = 'ASSET_ADD_'

    final def req = {
        given(
                new RequestSpecBuilder()
                        .addRequestSpecification(reqSpec())
                        .setContentType(ContentType.JSON)
                        .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                        .setBaseUri(PGP_HOST)
                        .setBasePath(REFUND_ACCOUNT_QUERY)
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
                        channelId: 'WEB'
                ]
        ]
    }

    private final Account bankAcct = new Account('003100100030871', 'BACB0000003', 'HDFC', 'pulkit', 'agarwal')
    private final Account vpaAcct = new Account(new PaymentDTO().getVpa())

    private final static ResponseSpecification rootSchema = new ResponseSpecBuilder()
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
            .expectBody('resultMsg', isIn('Success', 'Request prameters are not valid', 'Token validation failed', 'system error', 'Asset not exist', 'Target not found', 'Invalid ssoToken'))
            .build()


    private final static ResponseSpecification successSpec = new ResponseSpecBuilder()
            .rootPath("body")
            .expectBody("refundAccounts", everyItem(notNullValue()))
            .rootPath("body.refundAccounts.holderName")
            .expectBody("firstName", everyItem(notNullValue()))
            .expectBody("", everyItem(hasKey('lastName')))
            .rootPath("body.resultInfo")
            .expectBody("resultStatus", equalTo("S"))
            .expectBody("resultCodeId", equalTo("0000"))
            .expectBody("resultCode", equalTo("SUCCESS"))
            .expectBody("resultMsg", equalTo("Success"))
            .build()

    @AUser(edit = true)
    @Test
    void checkAccountDetailsReturnedWhenAvailable() {
        user().accounts.clear()
        SESSION_REDIS_CLUSTER().del(ASSET_ADD_REDIS_KEY_PREFIX + user().tokens['sso'].id)
        assert user().accounts.add(bankAcct)
        SESSION_REDIS_CLUSTER().del(ASSET_ADD_REDIS_KEY_PREFIX + user().tokens['sso'].id)
        def root = root()
        req().body(root).post()
                .then().spec(rootSchema).spec(successSpec)
                .root('body.refundAccounts[0]')
                .body('assetId', notNullValue(),
                'bankName', equalTo(bankAcct.bank))
                .root('body.refundAccounts[0].holderName')
                .body('firstName', notNullValue(),
                'lastName', notNullValue())
    }

    @Test
//TODO need a way to provide user to test case which has upi account linked and set upiAccountId; as of now hardcoding it using Pulkit's no
    void checkUPIAccountDetailsReturnedWhenAvailable() {
        def root = root()
        com.paytm.base.test.User userDetails = userManager.getForRead(Label.SAVEDVPA);
        def user = new User(userDetails.mobNo(),userDetails.password(),true)
     // def user = new User('8684922965', 'paytm@197', true)
        SESSION_REDIS_CLUSTER().del(ASSET_ADD_REDIS_KEY_PREFIX + user.tokens['sso'].id)
        root.head.token = user.tokens['sso'].id
        req().body(root).post()
                .then().spec(rootSchema).spec(successSpec)
                .root('body.refundAccounts[0]')
                .body('assetId', notNullValue(),
                        'bankName', not(isEmptyOrNullString()))
                .root('body.refundAccounts[0].holderName')
                .body('firstName', notNullValue(),
                        '', hasKey('lastName'))
    }

    @AUser(edit = true)
    @Test
    void checkNoAccountDetailsReturnedWhenNotAvailable() {
        user().accounts.clear()
        SESSION_REDIS_CLUSTER().del(ASSET_ADD_REDIS_KEY_PREFIX + user().tokens['sso'].id)
        def root = root()
        req().body(root).post()
                .then().spec(rootSchema)
                .root('body.resultInfo')
                .body("resultStatus", equalTo("F"))
                .body("resultCodeId", equalTo("2007"))
                .body("resultCode", equalTo("TARGET_NOT_FOUND"))
                .body("resultMsg", equalTo("Target not found"))
    }

    @Parameters(['invalidToken'])
    @Test
    void testWhenTokenIsInvalid(@Optional('hdhd2188') String invalidToken) {
        def root = root()
        root.head.token = invalidToken
        req().body(root).post()
                .then().spec(rootSchema)
                .root('body.resultInfo')
                .body("resultStatus", equalTo("F"))
                .body("resultCodeId", equalTo("1001"))
                .body("resultCode", equalTo("REQUEST_PARAMS_VALIDATION_EXCEPTION"))
                .body("resultMsg", equalTo("Invalid ssoToken"))
    }
}
