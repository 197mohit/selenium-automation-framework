package scripts.api.theia.checkUPIAccountExist

import com.paytm.api.UpiPredicate
import com.paytm.apphelpers.PGPHelpers
import com.paytm.base.test.TestSetUp
import com.paytm.utils.merchant.merchant.util.annotations.Merchant
import com.paytm.utils.merchant.merchant.util.annotations.Merchants
import com.paytm.utils.merchant.user.annotations.AUser
import com.paytm.utils.merchant.util.PGPUtil
import com.paytm.appconstants.Constants
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import io.qameta.allure.Feature
import io.qameta.allure.Owner
import io.restassured.builder.RequestSpecBuilder
import io.restassured.builder.ResponseSpecBuilder
import io.restassured.filter.Filter
import io.restassured.filter.FilterContext
import io.restassured.http.ContentType
import io.restassured.response.Response
import io.restassured.specification.FilterableRequestSpecification
import io.restassured.specification.FilterableResponseSpecification
import io.restassured.specification.ResponseSpecification
import org.testng.annotations.Test
import static com.paytm.appconstants.Constants.Owner.GAGANDEEP
import static com.paytm.LocalConfig.PGP_HOST
import static com.paytm.appconstants.Constants.NativeAPIResourcePath.CHECKUPI_ACCOUNT
import static io.restassured.RestAssured.given
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath
import static org.hamcrest.Matchers.equalTo


@Owner(GAGANDEEP)
@Feature("PGP-27174")
@Deprecated
class ChecksumUPIAccountExistTest extends TestSetUp implements CheckUPIAccountExistTest {

    private static final String IncorrectMid = 'Fzss4233sdveaaw332a'
    private static final String MOBILE_NO_WITH_UPI = '9560653535' //Need to replace with user having savedvpa
    private String condition = "\"{request -> request.getParameter('cust-id')=='{CUST_ID}'}\"";
    private final String upiProfileData = "{\"status\":\"FAILURE\",\"seqNo\":\"873e94c827024330957f393c58b8d64etheiaprimaryite\",\"respMessage\":\"Your request was unsuccessful. Please try again later.\",\"respCode\":\"514\"}"

    final def reqBldr = {
        new RequestSpecBuilder()
                .addRequestSpecification(reqSpec())
                .addFilters([schemaFilter, setChecksumFilter])
                .setContentType(ContentType.JSON)
                .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                .setBaseUri(PGP_HOST)
                .setBasePath(CHECKUPI_ACCOUNT)
    }

    final def req = { given(reqBldr().build()) }

    def root = {
        [
                head: [
                        version  : 'test',
                        tokenType: 'CHECKSUM',
                        token    : '?',
                        requestId: UUID.randomUUID().toString()[0..10],
                        channelId: 'WAP',
                ],
                body: [
                        mid         : m().id,
                        mobileNumber: MOBILE_NO_WITH_UPI,
                        deviceId    : UUID.randomUUID().toString()[0..3]
                ],
        ]
    }

    private final Filter schemaFilter = new Filter() {
        @Override
        Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
            responseSpec.spec(
                    new ResponseSpecBuilder()
                            .expectBody(matchesJsonSchemaInClasspath('json-schemas/theia/check-upi-account-schema.json'))
                            .build()
            )
            return ctx.next(requestSpec, responseSpec)
        }
    }

    private final Filter setChecksumFilter = new Filter() {
        @Override
        Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
            def root = new JsonSlurper().parseText(requestSpec.getBody())
            root?.head?.with {
                if (it?.token == '?') it.token = PGPUtil.getChecksum(m().key, JsonOutput.toJson(root.body))
            }
            requestSpec.body(root)
            ctx.next(requestSpec, responseSpec)
        }
    }

    private final static class ResultInfo {
        static ResponseSpecification INVALID_REQUEST_PARAMS = new ResponseSpecBuilder()
                .rootPath('body.resultInfo')
                .expectBody('resultStatus', equalTo('F'))
                .expectBody('resultCode', equalTo('1001'))
                .expectBody('resultMsg', equalTo("Request parameters are not valid"))
                .build()
    }

    @Test (enabled = false)
    @Merchant({ it.id == Constants.MerchantType.PPBLC_ONLY.getId() })
    @Override
    void "test CHECK_UPI_ACCOUNT_EXISTS=N on merchant error will invoke"() {
        def root = root()
        req().body(root).post().then().root('body.resultInfo')
                .body(
                        "resultMsg", equalTo('check upi account preference not enabled'),
                        "resultCode", equalTo("9999"),
                        "resultStatus", equalTo("F"))
    }

    @Test(description = 'test requestId stamp is optional value api does not invoke any error in response',enabled = false)
    @Merchant({  it.id == Constants.MerchantType.UPI_CONSENT_ADD.getId()})
    @Override
    void "test when head requestId is not provided"() {
        def root = root()
        root.head.remove('requestId')
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
    }

    @Test(description = 'test requestId stamp is null value api does not invoke any error in response',enabled = false)
    @Merchant({ it.id == Constants.MerchantType.UPI_CONSENT_ADD.getId() })
    @Override
    void "test when head requestId = null"() {
        def root = root()
        root.head.requestId = null
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
    }

    @Test(description = 'test requestId stamp is blank value api does not invoke any error in response',enabled = false)
    @Merchant({it.id == Constants.MerchantType.UPI_CONSENT_ADD.getId()})
    @Override
    void "test when head requestId = ''"() {
        def root = root()
        root.head.requestId = ''
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
    }

    @Test(description = 'test requestId stamp is random value api does not invoke any error in response',enabled = false)
    @Merchant({ it.id == Constants.MerchantType.UPI_CONSENT_ADD.getId() })
    @Override
    void "test when head requestId equals random value"() {
        def root = root()
        root.head.requestId = UUID.randomUUID().toString()[0..3]
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
    }

    @Test(description = 'test requestTimestamp stamp is not provided api does not invoke any error in response',enabled = false)
    @Merchant({ it.id == Constants.MerchantType.UPI_CONSENT_ADD.getId() })
    @Override
    void "test when head requestTimestamp is not provided"() {
        def root = root()
        root.head.remove('requestTimestamp')
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
    }

    @Test(description = 'test requestTimestamp stamp is null api does not invoke any error in response',enabled = false)
    @Merchant({ it.id == Constants.MerchantType.UPI_CONSENT_ADD.getId()})
    @Override
    void "test when head requestTimestamp = null"() {
        def root = root()
        root.head.requestTimestamp = null
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
    }

    @Test(description = 'test requestTimestamp stamp is blank api does not invoke any error in response',enabled = false)
    @Merchant({ it.id == Constants.MerchantType.UPI_CONSENT_ADD.getId() })
    @Override
    void "test when head requestTimestamp = ''"() {
        def root = root()
        root.head.requestTimestamp = ''
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
    }

    @Test(description = 'test requestTimestamp stamp is random value api does not invoke any error in response',enabled = false)
    @Merchant({ it.id == Constants.MerchantType.UPI_CONSENT_ADD.getId() })
    @Override
    void "test when head requestTimestamp equals random value"() {
        def root = root()
        root.head.requestTimestamp = UUID.randomUUID().toString()[0..7]
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
    }

    @Test(description = 'test channelId stamp is not provided not invoke any error in response',enabled = false)
    @Merchant({ it.id == Constants.MerchantType.UPI_CONSENT_ADD.getId()})
    @Override
    void "test when head channelId is not provided"() {
        def root = root()
        root.head.remove('channelId')
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
    }

    @Test(description = 'test channelId stamp is null api does not invoke any error in response',enabled = false)
    @Merchant({ it.id == Constants.MerchantType.UPI_CONSENT_ADD.getId()})
    @Override
    void "test when head channelId = null"() {
        def root = root()
        root.head.channelId = null
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
    }

    @Test(description = 'test channelId is blank api does not invoke any error in response',enabled = false)
    @Merchant({ it.id == Constants.MerchantType.UPI_CONSENT_ADD.getId() })
    @Override
    void "test when head channelId = ''"() {
        def root = root()
        root.head.channelId = ''
        req().body(root).post().then()
                .spec(results.sysErr as ResponseSpecification)
    }

    @Test(description = 'test channelId is random value api does not invoke any error in response',enabled = false)
    @Merchant({ it.id == Constants.MerchantType.UPI_CONSENT_ADD.getId()})
    @Override
    void "test when head channelId equals random value"() {
        //As discussed with dev channelId is either web/wap or blank otherwise it invoke error
        def root = root()
        root.head.channelId = UUID.randomUUID().toString()[0..10]
        req().body(root).post().then()
                .spec(results.sysErr as ResponseSpecification)
    }

    @Test(description = 'test tokenType is not provided api invoke any error in response',enabled = false)
    @Merchant({ it.id == Constants.MerchantType.UPI_CONSENT_ADD.getId()})
    @Override
    void "test when head tokenType is not provided"() {
        def root = root()
        root.head.remove('tokenType')
        req().body(root).post().then()
                .spec(ResultInfo.INVALID_REQUEST_PARAMS)
    }

    @Test(description = 'test tokenType is null api invoke any error in response',enabled = false)
    @Merchant({ it.id == Constants.MerchantType.UPI_CONSENT_ADD.getId() })
    @Override
    void "test when head tokenType = null"() {
        def root = root()
        root.head.tokenType = null
        req().body(root).post().then()
                .spec(ResultInfo.INVALID_REQUEST_PARAMS)
    }

    @Test(description = 'test tokenType is blank api invoke any error in response',enabled = false)
    @Merchant({ it.id == Constants.MerchantType.UPI_CONSENT_ADD.getId()})
    @Override
    void "test when head tokenType = ''"() {
        def root = root()
        root.head.tokenType = ''
        req().body(root).post().then()
                .spec(results.sysErr as ResponseSpecification)
    }

    @Test(description = 'test tokenType is random value api invoke any error in response',enabled = false)
    @Merchant({ it.id == Constants.MerchantType.UPI_CONSENT_ADD.getId() })
    @Override
    void "test when head tokenType equals random value"() {
        def root = root()
        root.head.tokenType = UUID.randomUUID().toString()[0..4]
        req().body(root).post().then()
                .spec(results.sysErr as ResponseSpecification)
    }

    @Test(enabled = false)
    @Merchant({ it.id == Constants.MerchantType.UPI_CONSENT_ADD.getId() })
    @AUser
    @Override
    void "test SSO token support is removed from the api"() {
        def root = root()
        root.head.tokenType = 'SSO'
        root.head.token = user().tokens.getAt('sso').id
        req().body(root).post().then()
                .spec(ResultInfo.INVALID_REQUEST_PARAMS)
    }

    @Test(enabled = false)
    @Merchant({ it.id == Constants.MerchantType.UPI_CONSENT_ADD.getId()})
    @Override
    void "test with invalid checksum it should invoke error"() {
        def root = root()
        root.head.token = UUID.randomUUID().toString()[0..5]
        req().body(root).post().then()
                .spec(results.invalidChecksum as ResponseSpecification)
    }

    @Test(enabled = false)
    @Merchant({it.id == Constants.MerchantType.UPI_CONSENT_ADD.getId()})
    @Override
    void "test mid is mandatory parameter and error message is returned in case mid is not send in request body"() {
        def root = root()
        root.body.remove('mid')
        req().body(root).post().then()
                .root('body.resultInfo')
                .body(
                        'resultStatus', equalTo('F'),
                        'resultCode', equalTo('9999'),
                        'resultMsg', equalTo('invalid request params'))
    }

    @Test(enabled = false)
    @Merchant({ it.id == Constants.MerchantType.UPI_CONSENT_ADD.getId() })
    @Override
    void "test error message is retuned in response is incorrect mid is sent in request body"() {
        def root = root()
        root.body.mid = IncorrectMid
        req().body(root).post().then()
                .root('body.resultInfo')
                .body(
                        'resultStatus', equalTo('F'),
                        'resultCode', equalTo('9999'),
                        'resultMsg', equalTo('check upi account preference not enabled'))
    }

    @Test(enabled = false)
    @Merchant({ it.id == Constants.MerchantType.UPI_CONSENT_ADD.getId()})
    @Override
    void "test mobileNumber is mandatory parameter and error message is returned in case mobileNumber is not send in request body"() {
        def root = root()
        root.body.remove('mobileNumber')
        req().body(root).post().then()
                .root('body.resultInfo')
                .body(
                        'resultStatus', equalTo('F'),
                        'resultCode', equalTo('9999'),
                        'resultMsg', equalTo('invalid request params'))
    }

    @Test(enabled = false)
    @Merchant({ it.id == Constants.MerchantType.UPI_CONSENT_ADD.getId() })
    @Override
    void "test error message is retuned in response is incorrect mobileNumber is sent in request body"() {
        def root = root()
        root.body.mobileNumber = '87719111111'
        req().body(root).post().then()
                .spec(results.sysErr as ResponseSpecification)
    }

    @Test(enabled = false)
    @Merchant({ it.id == Constants.MerchantType.UPI_CONSENT_ADD.getId()})
    @Override
    void "test success response if all mandatory parameters are sent and correct"() {
        def root = root()
        root.head.tokenType = 'CHECKSUM'
        root.body.mid = m().id
        root.body.mobileNumber = MOBILE_NO_WITH_UPI
        root.body.deviceId = UUID.randomUUID().toString()[0..3]
        req().body(root).post().then()
                .statusCode(200)
                .spec(results.success as ResponseSpecification)
    }

    @Test(enabled = false)
    @AUser
    @Merchant({ it.id == Constants.MerchantType.UPI_CONSENT_ADD.getId() })
    @Override
    void "test upiAccountWithTokenExists=false upi account doesnot exist for a number"() {
        def root = root()
        root.body.mobileNumber = user().mobile
        req().body(root).post().then()
                .body(
                        'body.resultInfo.resultStatus', equalTo('F'),
                        'body.resultInfo.resultCode', equalTo('0001'),
                        'body.resultInfo.resultMsg', equalTo('Failure'),
                        'body.upiAccountExist', equalTo(false))


    }

    @Test(enabled = false)
    @Merchant({ it.id == Constants.MerchantType.UPI_CONSENT_ADD.getId() })
    @AUser(edit = true)
    @Override
    void "test upi account exist for a number but mpin is not set then api should provide account not exist response with api status success"() {
        def root = root()
        condition = condition.replace("{CUST_ID}", user().id);
        UpiPredicate upiPredicate = new UpiPredicate(condition, upiProfileData);
        upiPredicate.execute();
        root.body.mobileNumber = user().mobile
        req().body(root).post().then()
                .body(
                        'body.resultInfo.resultStatus', equalTo('F'),
                        'body.resultInfo.resultCode', equalTo('0001'),
                        'body.resultInfo.resultMsg', equalTo('Failure'),
                        'body.upiAccountExist', equalTo(false))
    }

    @Override
    @Merchants([
            @Merchant({ it.preferences.checkUPIAccountExists.enabled == true }),
            @Merchant({ it.preferences.checkUPIAccountExists.enabled == false })
    ])
    @Test(enabled = false)
    void 'test when head token equals checksum generated using different merchant\'s key'() {
        def root = root()
        root.head.token = PGPUtil.getChecksum(m(1).key, JsonOutput.toJson(root.body))
        req().body(root).post().then()
                .spec(results.invalidChecksum as ResponseSpecification)
    }
}
