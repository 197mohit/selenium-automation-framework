package scripts.api.instantrefund

import com.paytm.apphelpers.PGPHelpers
import com.paytm.base.test.Group
import com.paytm.base.test.TestSetUp
import com.paytm.dto.PaymentDTO
import com.paytm.framework.reporting.Owners
import com.paytm.utils.merchant.merchant.util.annotations.Merchant
import com.paytm.utils.merchant.merchant.util.annotations.Merchants
import io.qameta.allure.Issue
import io.qameta.allure.Link
import io.qameta.allure.Owner
import io.restassured.builder.RequestSpecBuilder
import io.restassured.http.ContentType
import io.restassured.parsing.Parser
import org.testng.annotations.*

import static com.paytm.LocalConfig.PGP_HOST
import static com.paytm.appconstants.Constants.Refund.REFUND_ACCOUNT_VALIDATE
import static com.paytm.utils.merchant.util.PGPUtil.getChecksum
import static groovy.json.JsonOutput.toJson
import static io.restassured.RestAssured.given
import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.notNullValue

@Owner("Deepak")
@Owners(author = "Deepak", qa = "Pulkit")
@Link('https://jira.mypaytm.com/browse/PGP-12375')
@Test(groups = ['refund', Group.PUBLIC_API])
class RefundAccountValidate extends TestSetUp {

    final def req = {
        given(
                new RequestSpecBuilder()
                        .addRequestSpecification(reqSpec())
                        .setContentType(ContentType.JSON)
                        .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                        .setBaseUri(PGP_HOST)
                        .setBasePath(REFUND_ACCOUNT_VALIDATE)
                        .addQueryParam('mid', m().id)
                        .build()
        )
    }

    final def root = {
        [
                head: [
                        requestTimestamp: System.currentTimeMillis() as String,
                        version         : 'v1',
                        channelId       : 'WEB',
                        signature       : null,
                ],
                body: [
                        requestId    : new Random().nextLong().abs() as String,
                        mid          : m().id,
                        vpa          : null,
                        accountNumber: accNo,
                        ifscCode     : ifsc,
                        bankName     : bankName,
                ]
        ]
    }

    private final static String accNo = '003100100030871'
    private final static String ifsc = 'BACB0000003'
    private final static String bankName = 'HDFC'
    private final static String vpa = new PaymentDTO().getVpa()

    @Merchant({ it.preferences.vpaAccountValidation.enabled == true })
    @Test(dataProvider = 'ValidDetails', description = '$I(accountsDetails = valid); $O(token != null)')
    void assertSuccessWhenValidDetailsPassed(accountNumber, ifscCode, bankName, vpa) {
        def root = root()
        root.body << [accountNumber: accountNumber, ifscCode: ifscCode, bankName: bankName, vpa: vpa]
        root.head << [signature: getChecksum(m().key, toJson(root.body))]
        req().queryParam('requestId', root.body.requestId).body(root).post()
                .then().defaultParser(Parser.JSON)
                .root('body.resultInfo')
                .body('resultStatus', equalTo('S'),
                'resultCode', equalTo('1'),
                'resultMsg', equalTo('Txn Success'))
                .root('body')
                .body('token', notNullValue())
    }

    @DataProvider
    static Object[][] ValidDetails() {
        [[accNo, ifsc, bankName, null],
         [null, null, null, vpa]]
    }

    @Merchant({ it.preferences.vpaAccountValidation.enabled == true })
    @Parameters('mismatchingChecksum')
    @Test
    void assertChecksumMismatch(@Optional('') String mismatchingChecksum) {
        def root = root()
        root.head << [signature: mismatchingChecksum]
        root.body << [accountNumber: accNo, ifscCode: ifsc, bankName: bankName]
        req().queryParams([requestId: root.body.requestId]).body(root).post()
                .then().defaultParser(Parser.JSON)
                .root('body.resultInfo')
                .body('resultStatus', equalTo('F'),
                'resultCode', equalTo('330'),
                'resultMsg', equalTo('Paytm checksum mismatch.'))
    }

    @Merchant({ it.preferences.vpaAccountValidation.enabled == true })
    @Parameters('invalidMid')
    @Test
    void assertInvalidMid(@Optional('ahdhakh') String invalidMid) {
        def root = root()
        root.body.mid = invalidMid
        root.head << [signature: getChecksum(m().key, toJson(root.body))]
        req().queryParams([mid: invalidMid, requestId: root.body.requestId]).body(root).post()
                .then().defaultParser(Parser.JSON)
                .root('body.resultInfo')
                .body('resultStatus', equalTo('F'),
                'resultCode', equalTo('335'),
                'resultMsg', equalTo('Invalid merchant Id.'))
    }

//    @Merchant({ it.preferences.vpaAccountValidation.enabled == true })
//    @Parameters('invalidIFSCCode')
//    @Test(enabled = false)
//TODO as of now mock always returns success
    void testWhenInvalidIFSCCodeSupplied(@Optional('@') String invalidIFSCCode) {
        def root = root()
        root.body.ifscCode = invalidIFSCCode
        root.head << [signature: getChecksum(m().key, toJson(root.body))]
        req().queryParams([requestId: root.body.requestId]).body(root).post()
                .then().defaultParser(Parser.JSON)
                .root('body.resultInfo')
                .body('resultStatus', equalTo('F'),
                'resultCode', equalTo('601'),
                'resultMsg', equalTo('Invalid A/c Number or IFSC Code.'))
    }

    @Merchant({ it.preferences.vpaAccountValidation.enabled == true })
    @Parameters('invalidAcctNo')
    @Test
    void testWhenInvalidAcctNoSupplied(@Optional('5010005580649215') String invalidAcctNo) {
        def root = root()
        root.body.accountNumber = invalidAcctNo
        root.head << [signature: getChecksum(m().key, toJson(root.body))]
        req().queryParams([requestId: root.body.requestId]).body(root).post()
                .then().defaultParser(Parser.JSON)
                .root('body.resultInfo')
                .body('resultStatus', equalTo('F'),
                'resultCode', equalTo('601'),
                'resultMsg', equalTo('Invalid A/c Number or IFSC Code.'))
    }

    @Issue('PGP-23132')
    @Merchant({ it.preferences.vpaAccountValidation.enabled == true })
    @Parameters('invalidVpa')
    @Test
    void assertInvalidVPADetails(@Optional('erwerr@paytm') String invalidVpa) {
        def root = root()
        root.body << [vpa: invalidVpa, accountNumber: null, ifscCode: null, bankName: null]
        root.head << [signature: getChecksum(m().key, toJson(root.body))]
        req().queryParams([requestId: root.body.requestId]).body(root).post()
                .then().defaultParser(Parser.JSON)
                .root('body.resultInfo')
                .body('resultStatus', equalTo('F'),
                'resultCode', equalTo('602'),
                'resultMsg', equalTo('Invalid VPA.'))
    }

    @Merchant({ it.preferences.vpaAccountValidation.enabled == true })
    @Test
    void testWhenChannelIdIsNull() {
        def root = root()
        root.head.channelId = null
        root.head << [signature: getChecksum(m().key, toJson(root.body))]
        req().queryParams([requestId: root.body.requestId]).body(root).post()
                .then().defaultParser(Parser.JSON)
                .root('body.resultInfo')
                .body('resultStatus', equalTo('F'),
                'resultCode', equalTo('501'),
                'resultMsg', equalTo('System Error.'))
    }

    @Merchant({ it.preferences.vpaAccountValidation.disabled == true })
    @Test
    void assertMIDWithoutValidationPreference() {
        def root = root()
        root.head << [signature: getChecksum(m().key, toJson(root.body))]
        def response = req().queryParams([requestId: root.body.requestId]).body(root).when().post()
                .then().defaultParser(Parser.JSON)
                .root('body.resultInfo')
                .body('resultStatus', equalTo('F'),
                'resultMsg', equalTo('Merchant is not configured to use this API'))
        def responseBody = response.extract().body().as(Map)
        assert responseBody.body.resultInfo.resultCode in ['600', '402']
    }

    @Merchant({ it.preferences.vpaAccountValidation.enabled == true })
    @Test
    void assertFailureWhenAccountDetailsAreNotSupplied() {
        def root = root()
        root.body << [accountNumber: null, ifscCode: null, bankName: null, vpa: null]
        root.head << [signature: getChecksum(m().key, toJson(root.body))]
        req().queryParams([requestId: root.body.requestId]).body(root).post()
                .then().defaultParser(Parser.JSON)
                .root('body.resultInfo')
                .body('resultStatus', equalTo('F'),
                'resultCode', equalTo('600'),
                'resultMsg', equalTo('Validation Failed. Vpa and account number both can not be null'))
    }

    @Merchants([
            @Merchant(edit = true, value = { it.preferences.vpaAccountValidation.enabled == true }),
            @Merchant({ it.preferences.vpaAccountValidation.enabled == true })
    ])
    @Test
    void assertDifferentMIDsInBodyAndQueryParams() {
        def root = root()
        root.body.mid = m(1).id
        root.head << [signature: getChecksum(m(1).key, toJson(root.body))]
        req().queryParams([requestId: root.body.requestId]).body(root).post()
                .then().defaultParser(Parser.JSON)
                .root('body.resultInfo')
                .body('resultStatus', equalTo('F'),
                'resultCode', equalTo('600'),
                'resultMsg', equalTo('Validation Failed. mid in request is different with mid in query parameter'))
    }
}
