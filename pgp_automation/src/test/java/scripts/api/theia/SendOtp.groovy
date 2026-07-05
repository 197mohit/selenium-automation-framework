package scripts.api.theia

import com.paytm.LocalConfig
import com.paytm.ServerConfigProvider
import com.paytm.appconstants.Constants
import com.paytm.apphelpers.LogsValidationHelper
import com.paytm.apphelpers.PGPHelpers
import com.paytm.base.test.TestSetUp
import com.paytm.utils.merchant.merchant.util.OrderV2
import com.paytm.utils.merchant.merchant.util.annotations.Merchant
import com.paytm.utils.merchant.user.annotations.AUser
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
import org.assertj.core.api.Assertions
import org.assertj.core.api.SoftAssertions
import org.awaitility.Awaitility
import org.testng.annotations.Test

import java.util.regex.Pattern

import static com.paytm.LocalConfig.PGP_HOST
import static com.paytm.appconstants.Constants.NativeAPIResourcePath.SEND_OTP
import static io.restassured.RestAssured.given
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath
import static org.hamcrest.Matchers.*

@Owner(Constants.Owner.DEEPAK)
class SendOtp extends TestSetUp implements SendOtpTest {

    private static final String OAUTH_SMS_SENDER_ID = "iPaytm"
    private static final String OAUTH_SMS_ENTITY_ID = "1501601290000011395"
    private static final String LOGIN_OTP_MESSAGE_WITH_HASH_TEMPLATE_ID = "1507160119648012918"
    private static final String LOGIN_OTP_MESSAGE_WITHOUT_HASH_TEMPLATE_ID = "1507160119652870156"
    private static final String OAUTH_V3_TEMPLATE_ID = "1507160096820878504"
    private static final String LOGIN_OTP_MESSAGE_WITH_HASH = "<#>Paytm never calls you asking for OTP. Sharing OTP gives access to Paytm Balance. OTP to link your Paytm account with .+ is .+. ID:.+"
    private static final String LOGIN_OTP_MESSAGE_WITHOUT_HASH = "Paytm never calls you asking for OTP. Sharing OTP gives anyone full access to Paytm balance. OTP for linking your Paytm account to .+ is .+."
    private static final String LOGIN_OTP = "Beware of fraudulent calls. DO NOT share OTP with anyone for any offers, prizes or contests. To pay .+.+.+, OTP is .+."
    private static final String V3_OTP_MESSAGE = "Use Paytm Code .+to proceed."
    private static final Constants.MerchantType MERCHANT_HAVING_OAUTH_SEND_OTP_V5_ENABLED_IN_FF4J = Constants.MerchantType.PGOnly
    private static final String LANGUAGE_HEADER = 'X-accept-language'
    public static final String LANGUAGE_HEADER_VALUE = 'hi-IN'

    final def reqBldr = {
        new RequestSpecBuilder()
                .addRequestSpecification(reqSpec())
                .addFilters([schemaFilter])
                .setContentType(ContentType.JSON)
                .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                .setBaseUri(PGP_HOST)
                .setBasePath(SEND_OTP)
                .addQueryParams([mid: m()?.id ?: new Random().nextLong().abs() as String, orderId: m()?.orders?.last()?.id ?: new Random().nextLong().abs() as String])
    }

    final def req = { given(reqBldr().build()) }

    final def root = {
        [
                head: [
                        txnToken: m()?.orders?.with {
                            assert it.add(new OrderV2(1, user()?.tokens?.getAt('sso')?.id, null))
                            it.last()?.transaction?.token
                        },
                ],
                body: [
                        mobileNumber: user().mobile,//M
                        autoReadHash: null,//O
                ]
        ]
    }

    private final Filter schemaFilter = new Filter() {
        @Override
        Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
            responseSpec.spec(
                    new ResponseSpecBuilder()
                            .expectBody(matchesJsonSchemaInClasspath('json-schemas/theia/send-otp-v1-schema.json'))
                            .build()
            )
            return ctx.next(requestSpec, responseSpec)
        }
    }

    private final static class ResultInfo {
        static ResponseSpecification SUCCESS = new ResponseSpecBuilder()
                .rootPath('body.resultInfo')
                .expectBody('resultStatus', equalTo('SUCCESS'))
                .expectBody('resultCode', equalTo('01'))
                .expectBody('resultMsg', equalTo("Otp sent to phone"))
                .build()
    }

    @Override
    @Merchant(edit = true)
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
        String cmdToFetchSendOTPRequest = "grep '${m()?.orders?.last()?.id}' /paytm/logs/theia_facade.log | " +
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
        String cmdToFetchSendOTPRequest = "grep '${m()?.orders?.last()?.id}' /paytm/logs/theia_facade.log | " +
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

   // @Override
   // @Merchant(edit = true, value = { it.id != MERCHANT_HAVING_OAUTH_SEND_OTP_V5_ENABLED_IN_FF4J.getId() })
   // @AUser(edit = true)
   // @Test(enabled = false) // theia.enable.send.otp.v5 flag is enabled on Prod for all MIDs
    void "test when merchant has send otp v5 flag disabled and autoReadHash is passed"() {
        def root = root()
        root.body.autoReadHash = UUID.randomUUID().toString()
        req().body(root).post().then()
                .spec(ResultInfo.SUCCESS)
        String cmdToFetchSendOTPRequest = "grep '${m()?.orders?.last()?.id}' /paytm/logs/theia_facade.log | " +
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

  /*  @Override
    @Merchant(edit = true, value = { it.id != MERCHANT_HAVING_OAUTH_SEND_OTP_V5_ENABLED_IN_FF4J.getId() })
    @AUser(edit = true)
    @Test(enabled = false) */ // theia.enable.send.otp.v5 flag is enabled on Prod for all MIDs
    void "test when merchant has send otp v5 flag disabled and autoReadHash is not passed"() {
        def root = root()
        root.body.remove('autoReadHash')
        req().body(root).post().then()
                .spec(ResultInfo.SUCCESS)
        String cmdToFetchSendOTPRequest = "grep '${m()?.orders?.last()?.id}' /paytm/logs/theia_facade.log | " +
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

    @Merchant(edit = true, value = { !it.localeEnabled })
    @AUser
    @Test
    void 'test localisation fields are not present when localisation is disabled on merchant but language header is passed'() {
        def root = root()
        req().header(LANGUAGE_HEADER, LANGUAGE_HEADER_VALUE).body(root).post().then()
                .spec(ResultInfo.SUCCESS)
                .body('body.resultInfo', not(hasKey('resultMsgRegional')))
    }

    @Merchant(edit = true, value = { it.localeEnabled })
    @AUser
    @Test
    void 'test localisation fields are not present when localisation is enabled on merchant but language header is not passed'() {
        def root = root()
        req().body(root).post().then()
                .spec(ResultInfo.SUCCESS)
                .body('body.resultInfo', not(hasKey('resultMsgRegional')))
    }

    @Merchant(edit = true, value = { it.localeEnabled })
    @AUser
    @Test
    void 'test localisation fields are present when localisation is enabled on merchant and language header is passed'() {
        def root = root()
        req().header(LANGUAGE_HEADER, LANGUAGE_HEADER_VALUE).body(root).post().then()
                .spec(ResultInfo.SUCCESS)
                .body('body.resultInfo', hasKey('resultMsgRegional'))
    }
}
