package scripts.api.theia

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.paytm.LocalConfig
import com.paytm.ServerConfigProvider
import com.paytm.appconstants.Constants
import com.paytm.apphelpers.LogsValidationHelper
import com.paytm.apphelpers.PGPHelpers
import com.paytm.base.test.TestSetUp
import com.paytm.utils.merchant.merchant.util.annotations.Merchant
import com.paytm.utils.merchant.user.annotations.AUser
import groovy.json.JsonSlurper
import io.qameta.allure.Link
import io.restassured.builder.RequestSpecBuilder
import io.restassured.builder.ResponseSpecBuilder
import io.restassured.filter.Filter
import io.restassured.filter.FilterContext
import io.restassured.http.ContentType
import io.restassured.response.Response
import io.restassured.specification.FilterableRequestSpecification
import io.restassured.specification.FilterableResponseSpecification
import io.restassured.specification.RequestSpecification
import io.restassured.specification.ResponseSpecification
import org.assertj.core.api.Assertions
import org.assertj.core.api.SoftAssertions
import org.awaitility.Awaitility
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

import java.util.regex.Pattern

import static com.paytm.LocalConfig.PGP_HOST
import static com.paytm.appconstants.Constants.NativeAPIResourcePath.SEND_OTP_V4
import static io.restassured.RestAssured.given
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath
import static org.hamcrest.Matchers.equalTo

@Link('https://wiki.mypaytm.com/display/PGP/SendOTP+%28V4%29+-+Theia')
class SendOtpV4Test extends TestSetUp implements SendOtpTest {

    private static final ThreadLocal<String> ORDER_ID = new ThreadLocal<>()

    private static final String OAUTH_SMS_SENDER_ID = "iPaytm"
    private static final String OAUTH_SMS_ENTITY_ID = "1501601290000011395"
    private static final String LOGIN_OTP_MESSAGE_WITH_HASH_TEMPLATE_ID = "1507160119648012918"
    private static final String LOGIN_OTP_MESSAGE_WITHOUT_HASH_TEMPLATE_ID = "1507160119652870156"
    private static final String SUBSCRIPTION_CREATE_OTP_MESSAGE_TEMPLATE_ID = "1507160119657745832"
    private static final String OAUTH_V3_TEMPLATE_ID = "1507160096820878504"
    private static final String SUBSCRIPTION_CREATE_OTP_MESSAGE = "Please DO NOT SHARE OTP with anyone. To pay Rs.+ & allow .+ to deduct upto Rs.+ every .+, OTP is .+"
    private static final String LOGIN_OTP_MESSAGE_WITH_HASH = "<#>Paytm never calls you asking for OTP. Sharing OTP gives access to Paytm Balance. OTP to link your Paytm account with .+ is .+. ID:.+"
    private static final String LOGIN_OTP_MESSAGE_WITHOUT_HASH = "Paytm never calls you asking for OTP. Sharing OTP gives anyone full access to Paytm balance. OTP for linking your Paytm account to .+ is .+."
    private static final String LOGIN_OTP = "Beware of fraudulent calls. DO NOT share OTP with anyone for any offers, prizes or contests. To pay .+.+.+, OTP is .+."
    private static final String V3_OTP_MESSAGE = "Use Paytm Code .+to proceed."
    private static final Constants.MerchantType MERCHANT_HAVING_OAUTH_SEND_OTP_V5_ENABLED_IN_FF4J = Constants.MerchantType.PGOnly

    private RequestSpecBuilder reqBldr() {
        new RequestSpecBuilder()
                .addRequestSpecification(reqSpec())
                .addFilters([setQueryParamMidFilter, setTokenFilter, schemaFilter])
                .setContentType(ContentType.JSON)
                .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                .setBaseUri(PGP_HOST)
                .setBasePath(SEND_OTP_V4)
                .addQueryParam('mid', '?')
                .addQueryParam('referenceId', UUID.randomUUID().toString()[0..9])
                .addQueryParam('orderId', ORDER_ID.get())
                .addHeader('JWT_TOKEN', '?')
    }

    private RequestSpecification req() { given(reqBldr().build()) }

    private final Filter schemaFilter = new Filter() {
        @Override
        Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
            responseSpec.spec(
                    new ResponseSpecBuilder()
                            .expectBody(matchesJsonSchemaInClasspath('json-schemas/theia/send-otp-v4-schema.json'))
                            .build()
            )
            return ctx.next(requestSpec, responseSpec)
        }
    }

    Filter setQueryParamMidFilter = new Filter() {
        @Override
        Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
            if (requestSpec.getQueryParams()['mid'] == '?') {
                requestSpec.removeQueryParam('mid').queryParam('mid', new JsonSlurper().parseText(requestSpec.getBody())?.body?.mid ?: m()?.id ?: UUID.randomUUID().toString())
            }
            ctx.next(requestSpec, responseSpec)
        }
    }

    private final Filter setTokenFilter = new Filter() {
        @Override
        Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
            def root = new JsonSlurper().parseText(requestSpec.getBody())
            if (requestSpec.getHeaders().get('JWT_TOKEN').getValue() == '?') {
                requestSpec.replaceHeader('JWT_TOKEN', JWT.create()
                        .withIssuer(root.head.clientId)
                        .withClaim('mid', root.body.mid)
                        .withClaim('mobileNumber', root.body.mobileNumber)
                        .sign(Algorithm.HMAC256("87rtvasudve24ufc77wfb38fy84yf73"))) //value of vault property 'supergw'
            }
            requestSpec.body(root)
            ctx.next(requestSpec, responseSpec)
        }
    }

    private final Map root() {
        [
                head: [
                        requestTimestamp: System.currentTimeMillis().toString(),//M
                        version         : 'v4',//M
                        channelId       : 'WEB',//M
                        clientId        : 'supergw',//M //present in vault
                ],
                body: [
                        mobileNumber       : user().mobile,//M
                        autoReadHash       : null,//O
                        mid                : m().id,//M
                        subscriptionDetails: null,//O
                        requestTypes       : ["NATIVE"],//M
                ]
        ]
    }

    private final static class ResultInfo {
        static ResponseSpecification SUCCESS = new ResponseSpecBuilder()
                .rootPath('body.resultInfo')
                .expectBody('resultStatus', equalTo('S'))
                .expectBody('resultCode', equalTo('01'))
                .expectBody('resultMsg', equalTo("Otp sent to phone"))
                .build()
    }

    @BeforeMethod
    void setUp() {
        ORDER_ID.set(UUID.randomUUID().toString())
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @AUser(edit = true)
    @Test
    void testSuccess() {
        def root = root()
        req().body(root).post().then()
                .spec(ResultInfo.SUCCESS)
    }

    @Override
    @Merchant(edit = true, value = { it.id == MERCHANT_HAVING_OAUTH_SEND_OTP_V5_ENABLED_IN_FF4J.getId() })
    @AUser(edit = true)
    @Test
    void "test when merchant has send otp v5 flag enabled and autoReadHash is passed"() {
        def root = root()
        root.body.autoReadHash = UUID.randomUUID().toString()
        req().body(root).post().then()
                .spec(ResultInfo.SUCCESS)
        String cmdToFetchSendOTPRequest = "grep '${ORDER_ID.get()}' /paytm/logs/theia_facade.log | " +
                "grep ${m().id} | grep '/api/sendOtp' | grep '${'*' * 6 + user().mobile[-4..-1]}'"
        String sendOtpRequestDetails = Awaitility.await().until({ LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, cmdToFetchSendOTPRequest) }, { it as Boolean })
        Assertions.assertThat(sendOtpRequestDetails).contains('v5')
        String cmdToFetchSMSDetails = "grep '${user().mobile}' /paytm/logs/communicationGateway.log | grep 'com.paytm.pgplus.sms.gateway.core.services.MessageService.process' | tail -1"
        String smsDetails = Awaitility.await().until({ LogsValidationHelper.getLogsOnServer(LocalConfig.STAGING_COMMUNICATION_GATEWAY_IP, cmdToFetchSMSDetails) }, { it as Boolean })
        SoftAssertions softly = new SoftAssertions()
        softly.assertThat(smsDetails).contains("sender=" + OAUTH_SMS_SENDER_ID)
        softly.assertThat(smsDetails).contains("entityId=" + OAUTH_SMS_ENTITY_ID)
        softly.assertThat(smsDetails).contains("dltTemplateId=" + LOGIN_OTP_MESSAGE_WITH_HASH_TEMPLATE_ID)
        softly.assertThat(smsDetails).containsPattern(Pattern.compile(LOGIN_OTP_MESSAGE_WITH_HASH))
        softly.assertAll()
    }

    @Override
    @Merchant(edit = true, value = { it.id == MERCHANT_HAVING_OAUTH_SEND_OTP_V5_ENABLED_IN_FF4J.getId() })
    @AUser(edit = true)
    @Test
    void "test when merchant has send otp v5 flag enabled and autoReadHash is not passed"() {
        def root = root()
        root.body.remove('autoReadHash')
        req().body(root).post().then()
                .spec(ResultInfo.SUCCESS)
        String cmdToFetchSendOTPRequest = "grep '${ORDER_ID.get()}' /paytm/logs/theia_facade.log | " +
                "grep ${m().id} | grep '/api/sendOtp' | grep '${'*' * 6 + user().mobile[-4..-1]}'"
        String sendOtpRequestDetails = Awaitility.await().until({ LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, cmdToFetchSendOTPRequest) }, { it as Boolean })
        Assertions.assertThat(sendOtpRequestDetails).contains('v5')
        String cmdToFetchSMSDetails = "grep '${user().mobile}' /paytm/logs/communicationGateway.log | grep 'com.paytm.pgplus.sms.gateway.core.services.MessageService.process' | tail -1"
        String smsDetails = Awaitility.await().until({ LogsValidationHelper.getLogsOnServer(LocalConfig.STAGING_COMMUNICATION_GATEWAY_IP, cmdToFetchSMSDetails) }, { it as Boolean })
        SoftAssertions softly = new SoftAssertions()
        softly.assertThat(smsDetails).contains("sender=" + OAUTH_SMS_SENDER_ID)
        softly.assertThat(smsDetails).contains("entityId=" + OAUTH_SMS_ENTITY_ID)
        softly.assertThat(smsDetails).contains("dltTemplateId=" + LOGIN_OTP_MESSAGE_WITHOUT_HASH_TEMPLATE_ID)
        softly.assertThat(smsDetails).containsPattern(Pattern.compile(LOGIN_OTP_MESSAGE_WITHOUT_HASH))
        softly.assertAll()
    }

 /*   @Override
    @Merchant(edit = true, value = { it.id != MERCHANT_HAVING_OAUTH_SEND_OTP_V5_ENABLED_IN_FF4J.getId() })
    @AUser(edit = true)
    @Test(enabled = false)
    // theia.enable.send.otp.v5 flag is enabled on Prod for all MIDs
  */
    void "test when merchant has send otp v5 flag disabled and autoReadHash is passed"() {
        def root = root()
        root.body.autoReadHash = UUID.randomUUID().toString()
        req().body(root).post().then()
                .spec(ResultInfo.SUCCESS)
        String cmdToFetchSendOTPRequest = "grep '${ORDER_ID.get()}' /paytm/logs/theia_facade.log | " +
                "grep ${m().id} | grep '/api/sendOtp' | grep '${'*' * 6 + user().mobile[-4..-1]}'"
        String sendOtpRequestDetails = Awaitility.await().until({ LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, cmdToFetchSendOTPRequest) }, { it as Boolean })
        Assertions.assertThat(sendOtpRequestDetails).contains('v3')
        String cmdToFetchSMSDetails = "grep '${user().mobile}' /paytm/logs/communicationGateway.log | grep 'com.paytm.pgplus.sms.gateway.core.services.MessageService.process' | tail -1"
        String smsDetails = Awaitility.await().until({ LogsValidationHelper.getLogsOnServer(LocalConfig.STAGING_COMMUNICATION_GATEWAY_IP, cmdToFetchSMSDetails) }, { it as Boolean })
        SoftAssertions softly = new SoftAssertions()
        softly.assertThat(smsDetails).contains("sender=" + OAUTH_SMS_SENDER_ID)
        softly.assertThat(smsDetails).contains("entityId=" + OAUTH_SMS_ENTITY_ID)
        softly.assertThat(smsDetails).contains("dltTemplateId=" + OAUTH_V3_TEMPLATE_ID)
        softly.assertThat(smsDetails).containsPattern(Pattern.compile(V3_OTP_MESSAGE))
        softly.assertAll()
    }

/*    @Override
    @Merchant(edit = true, value = { it.id != MERCHANT_HAVING_OAUTH_SEND_OTP_V5_ENABLED_IN_FF4J.getId() })
    @AUser(edit = true)
    @Test(enabled = false)
    // theia.enable.send.otp.v5 flag is enabled on Prod for all MIDs
 */
    void "test when merchant has send otp v5 flag disabled and autoReadHash is not passed"() {
        def root = root()
        root.body.remove('autoReadHash')
        req().body(root).post().then()
                .spec(ResultInfo.SUCCESS)
        String cmdToFetchSendOTPRequest = "grep '${ORDER_ID.get()}' /paytm/logs/theia_facade.log | " +
                "grep ${m().id} | grep '/api/sendOtp' | grep '${'*' * 6 + user().mobile[-4..-1]}'"
        String sendOtpRequestDetails = Awaitility.await().until({ LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, cmdToFetchSendOTPRequest) }, { it as Boolean })
        Assertions.assertThat(sendOtpRequestDetails).contains('v3')
        String cmdToFetchSMSDetails = "grep '${user().mobile}' /paytm/logs/communicationGateway.log | grep 'com.paytm.pgplus.sms.gateway.core.services.MessageService.process' | tail -1"
        String smsDetails = Awaitility.await().until({ LogsValidationHelper.getLogsOnServer(LocalConfig.STAGING_COMMUNICATION_GATEWAY_IP, cmdToFetchSMSDetails) }, { it as Boolean })
        SoftAssertions softly = new SoftAssertions()
        softly.assertThat(smsDetails).contains("sender=" + OAUTH_SMS_SENDER_ID)
        softly.assertThat(smsDetails).contains("entityId=" + OAUTH_SMS_ENTITY_ID)
        softly.assertThat(smsDetails).contains("dltTemplateId=" + OAUTH_V3_TEMPLATE_ID)
        softly.assertThat(smsDetails).containsPattern(Pattern.compile(V3_OTP_MESSAGE))
        softly.assertAll()
    }

    @Merchant(edit = true, value = { it.id == MERCHANT_HAVING_OAUTH_SEND_OTP_V5_ENABLED_IN_FF4J.getId() })
    @AUser(edit = true)
    @Test
    void "test when merchant has send otp v5 flag enabled and requestType is NATIVE_SUBSCRIPTION"() {
        def root = root()
        root.body.requestTypes = ['NATIVE_SUBSCRIPTION']
        root.body.subscriptionDetails = [
                maxAmount    : [currency: 'INR', value: '100'],
                txnAmount    : [currency: 'INR', value: '2'],
                frequency    : '1',
                frequencyUnit: 'MONTH'
        ]
        req().body(root).post().then()
                .spec(ResultInfo.SUCCESS)
        String cmdToFetchSendOTPRequest = "grep '${ORDER_ID.get()}' /paytm/logs/theia_facade.log | " +
                "grep ${m().id} | grep '/api/sendOtp' | grep '${'*' * 6 + user().mobile[-4..-1]}'"
        String sendOtpRequestDetails = Awaitility.await().until({ LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, cmdToFetchSendOTPRequest) }, { it as Boolean })
        Assertions.assertThat(sendOtpRequestDetails).contains('v5')
        String cmdToFetchSMSDetails = "grep '${user().mobile}' /paytm/logs/communicationGateway.log | grep 'com.paytm.pgplus.sms.gateway.core.services.MessageService.process' | tail -1"
        String smsDetails = Awaitility.await().until({ LogsValidationHelper.getLogsOnServer(LocalConfig.STAGING_COMMUNICATION_GATEWAY_IP, cmdToFetchSMSDetails) }, { it as Boolean })
        SoftAssertions softly = new SoftAssertions()
        softly.assertThat(smsDetails).contains("sender=" + OAUTH_SMS_SENDER_ID)
        softly.assertThat(smsDetails).contains("entityId=" + OAUTH_SMS_ENTITY_ID)
        softly.assertThat(smsDetails).contains("dltTemplateId=" + SUBSCRIPTION_CREATE_OTP_MESSAGE_TEMPLATE_ID)
        softly.assertThat(smsDetails).containsPattern(Pattern.compile(SUBSCRIPTION_CREATE_OTP_MESSAGE))
        softly.assertAll()
    }

/*    @Merchant(edit = true, value = { it.id != MERCHANT_HAVING_OAUTH_SEND_OTP_V5_ENABLED_IN_FF4J.getId() })
    @AUser(edit = true)
    @Test(enabled = false)
    // theia.enable.send.otp.v5 flag is enabled on Prod for all MIDs
 */
    void "test when merchant has send otp v5 flag disabled and requestType is NATIVE_SUBSCRIPTION"() {
        def root = root()
        root.body.requestTypes = ['NATIVE', 'NATIVE_SUBSCRIPTION']
        root.body.subscriptionDetails = [
                maxAmount    : [currency: 'INR', value: '100'],
                txnAmount    : [currency: 'INR', value: '2'],
                frequency    : '1',
                frequencyUnit: 'MONTH'
        ]
        req().body(root).post().then()
                .spec(ResultInfo.SUCCESS)
        String cmdToFetchSendOTPRequest = "grep '${ORDER_ID.get()}' /paytm/logs/theia_facade.log | " +
                "grep ${m().id} | grep '/api/sendOtp' | grep '${'*' * 6 + user().mobile[-4..-1]}'"
        String sendOtpRequestDetails = Awaitility.await().until({ LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, cmdToFetchSendOTPRequest) }, { it as Boolean })
        Assertions.assertThat(sendOtpRequestDetails).contains('v3')
        String cmdToFetchSMSDetails = "grep '${user().mobile}' /paytm/logs/communicationGateway.log | grep 'com.paytm.pgplus.sms.gateway.core.services.MessageService.process' | tail -1"
        String smsDetails = Awaitility.await().until({ LogsValidationHelper.getLogsOnServer(LocalConfig.STAGING_COMMUNICATION_GATEWAY_IP, cmdToFetchSMSDetails) }, { it as Boolean })
        SoftAssertions softly = new SoftAssertions()
        softly.assertThat(smsDetails).contains("sender=" + OAUTH_SMS_SENDER_ID)
        softly.assertThat(smsDetails).contains("entityId=" + OAUTH_SMS_ENTITY_ID)
        softly.assertThat(smsDetails).contains("dltTemplateId=" + OAUTH_V3_TEMPLATE_ID)
        softly.assertThat(smsDetails).containsPattern(Pattern.compile(V3_OTP_MESSAGE))
        softly.assertAll()
    }
}
