package scripts.api.instantrefund

import com.paytm.apphelpers.PGPHelpers
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
import static com.paytm.appconstants.Constants.savedCard.REFUND_ACCOUNT_ADD
import static com.paytm.base.test.Group.*
import static io.restassured.RestAssured.given
import static org.hamcrest.Matchers.*

@Owner("Deepak")
@Owners(author = "Deepak", qa = "Pulkit")
@Link('https://wiki.mypaytm.com/display/PGP/IMPS+Refund+Integration+Solution')
@Test(groups = ['refund', PUBLIC_API])
class RefundAccountAdd extends TestSetUp {

    private static final String ASSET_ADD_REDIS_KEY_PREFIX = 'ASSET_ADD_'

    final def req = {
        given(
                new RequestSpecBuilder()
                        .addRequestSpecification(reqSpec())
                        .setContentType(ContentType.JSON)
                        .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                        .setBaseUri(PGP_HOST)
                        .setBasePath(REFUND_ACCOUNT_ADD)
                        .build()
        )
    }

    final def root = {
        [
                head: [
                        requestTimestamp: System.currentTimeMillis() as String,
                        requestId       : new Random().nextLong().abs() as String,
                        tokenType       : 'SSO',
                        token           : user()?.tokens?.getAt('sso')?.id,
                ],
                body: [
                        accountNumber: null,
                        ifsc         : null,
                        bankName     : null,
                        upiAccountId : null,
                        channelId    : 'WEB',
                        holderName   : [
                                firstName: 'pulkit',
                                lastName : 'agarwal'
                        ]
                ]
        ]
    }

    private final Account bankAcct = new Account('003100100030871', 'BACB0000003', 'HDFC', 'pulkit', 'agarwal')
    private final Account vpaAcct = new Account(new PaymentDTO().getVpa())

    private final static ResponseSpecification schema = new ResponseSpecBuilder()
            .expectStatusCode(isIn(200, 400, 500))
            .expectContentType(ContentType.JSON)
            .expectBody('', hasKey('head'))
            .expectBody('', hasKey('body'))
            .rootPath('head')
            .expectBody('responseTimestamp', notNullValue())
            .expectBody('requestId', notNullValue())
            .rootPath('body')
            .expectBody('', hasKey('resultInfo'))
            .rootPath('body.resultInfo')
            .expectBody('resultStatus', isIn('S', 'F', 'U'))
            .expectBody('resultCodeId', isIn('0000', '1001', '1002', '9999', '1003', '2001', '2002', '2005', '2003', '2004', '2006', '2019', '00000900'))
            .expectBody('resultCode', isIn('SUCCESS', 'REQUEST_PARAMS_VALIDATION_EXCEPTION', 'TOKEN_VALIDATION_EXCEPTION', 'SYSTEM_ERROR', 'ACCOUNT_VERIFICATION_FAIL', 'RISK_REJECT', 'ADD_ASSET_OVERLIMIT', 'CARD_INDEX_NO_NOT_EXIST', 'ASSET_HAS_ADDED', 'USER_IS_FROZEN', 'USER_NOT_EXIST', 'DUPLICATE_REQUEST_FOR_ADD_ASSET', 'INVALID_ACCOUNTID_CUST_ID'))
            .expectBody('resultMsg', isIn('Success', 'Request prameters are not valid', 'Token validation failed', 'Something went wrong', 'Account Verification Failed', 'Risk Reject', 'Add asset overlimit', 'Card Index number not exist', 'Asset has added', 'User is frozen', 'User not exist', 'Invalid ssoToken', 'Bank account addition is taking longer than usual. Please try after sometime','Invalid user and upiAccountId combination'))
            .build()
    private final static ResponseSpecification respSpecSuccess = new ResponseSpecBuilder()
            .expectStatusCode(200)
            .rootPath("body")
            .expectBody("assetId", notNullValue())
            .rootPath("body.resultInfo")
            .expectBody("resultStatus", equalTo("S"))
            .expectBody("resultCodeId", equalTo("0000"))
            .expectBody("resultCode", equalTo("SUCCESS"))
            .expectBody("resultMsg", equalTo("Success"))
            .build()

    @AUser(edit = true)
    @Test(description = 'test for real bank account')
    void testForRealBankAccount() {
        user().accounts.clear()
        SESSION_REDIS_CLUSTER().del(ASSET_ADD_REDIS_KEY_PREFIX + user().tokens['sso'].id)
        def root = root()
        root.body << [accountNumber: new Random().nextLong().abs() as String, ifsc: bankAcct.ifsc, bankName: bankAcct.bank, upiAccountId: null]
        req().body(root).post()
                .then().spec(schema).spec(respSpecSuccess)
    }

    @Test(description = 'test for real upi account')
//TODO need a way to provide user to test case which has upi account linked and set upiAccountId; as of now hardcoding it using Pulkit's no
    void testForRealUpiAccount() {
        com.paytm.base.test.User userDetails = userManager.getForRead(Label.SAVEDVPA);
        def user = new User(userDetails.mobNo(),userDetails.password(),true)
       // def user = new User('8684922965', 'paytm@197', true)
        SESSION_REDIS_CLUSTER().del(ASSET_ADD_REDIS_KEY_PREFIX + user.tokens['sso'].id)
        def root = root()
        root.head.token = user.tokens['sso'].id
        root.body << [upiAccountId: '10645']
        req().body(root).post()
                .then().spec(schema).spec(respSpecSuccess)
    }

    @AUser(edit = true)
    @Test(description = 'test when account details not passed')
    void testWhenAccountDetailsNotPassed() {
        user().accounts.clear()
        SESSION_REDIS_CLUSTER().del(ASSET_ADD_REDIS_KEY_PREFIX + user().tokens['sso'].id)
        def root = root()
        root.body << [accountNumber: null, ifsc: null, bankName: null, upiAccountId: null]
        req().body(root).post()
                .then().spec(schema)
                .statusCode(400)
                .root('body.resultInfo')
                .body("resultStatus", equalTo("F"))
                .body("resultCodeId", equalTo("1001"))
                .body("resultCode", equalTo("REQUEST_PARAMS_VALIDATION_EXCEPTION"))
                .body("resultMsg", equalTo("Request prameters are not valid"))
    }

    @AUser(edit = true)
    @Test(description = 'test when account details are already added')
    void testWhenAccountDetailsAreAlreadyAdded() {
        user().accounts.clear()
        SESSION_REDIS_CLUSTER().del(ASSET_ADD_REDIS_KEY_PREFIX + user().tokens['sso'].id)
        assert user().accounts.add(bankAcct)
        SESSION_REDIS_CLUSTER().del(ASSET_ADD_REDIS_KEY_PREFIX + user().tokens['sso'].id)
//        Thread.sleep(5 * 60 * 10**3)//added 5 mins wait because there is a check that doesn't allow same request to be made within 5 mins interval
        def root = root()
        root.body << [accountNumber: new Random().nextLong().abs() as String, ifsc: bankAcct.ifsc, bankName: bankAcct.bank]
        req().body(root).post()
                .then().spec(schema)
                .statusCode(200)
                .root('body.resultInfo')
                .body("resultStatus", equalTo("F"))
                .body("resultCodeId", equalTo("2002"))
                .body("resultCode", equalTo("ADD_ASSET_OVERLIMIT"))
                .body("resultMsg", equalTo("Add asset overlimit"))
    }

    @Parameters('invalidToken')
    @Test(description = 'test when token is invalid',enabled = false)
    void testWhenTokenIsInvalid(@Optional('hdhd2188') String invalidToken) {
        def root = root()
        root.head.token = invalidToken
        root.body << [accountNumber: new Random().nextLong().abs() as String, ifsc: bankAcct.ifsc, bankName: bankAcct.bank]
        req().body(root).post()
                .then().spec(schema)
                .statusCode(400)
                .root('body.resultInfo')
                .body("resultStatus", equalTo("F"))
                .body("resultCodeId", equalTo("1001"))
                .body("resultCode", equalTo("REQUEST_PARAMS_VALIDATION_EXCEPTION"))
                .body("resultMsg", equalTo("Invalid ssoToken"))
    }


}
