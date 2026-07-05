package scripts.api.theia

import com.paytm.api.nativeAPI.InitTxn
import com.paytm.api.nativeAPI.SendOTP
import com.paytm.appconstants.Constants
import com.paytm.apphelpers.PGPHelpers
import com.paytm.base.test.TestSetUp
import com.paytm.utils.merchant.merchant.util.annotations.Merchant
import com.paytm.utils.merchant.user.annotations.AUser
import com.paytm.utils.merchant.util.AuthUtil
import io.qameta.allure.Owner
import io.restassured.builder.RequestSpecBuilder
import io.restassured.builder.ResponseSpecBuilder
import io.restassured.filter.Filter
import io.restassured.filter.FilterContext
import io.restassured.http.ContentType
import io.restassured.specification.FilterableRequestSpecification
import io.restassured.specification.FilterableResponseSpecification
import io.restassured.specification.ResponseSpecification
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

import static com.paytm.LocalConfig.PGP_HOST
import static com.paytm.appconstants.Constants.NativeAPIResourcePath.VALIDATE_OTP
import static io.restassured.RestAssured.given
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath
import static org.hamcrest.Matchers.*

@Owner("Deepak")
class ValidateOtp extends TestSetUp {

    private final static ThreadLocal<String> ORDER_ID = new ThreadLocal<>()
    private final static ThreadLocal<String> TOKEN = new ThreadLocal<>()

    private final static String LANGUAGE_HEADER = 'X-accept-language'
    public final static String LANGUAGE_HEADER_VALUE = 'hi-IN'

    final def reqBldr = {
        new RequestSpecBuilder()
                .addRequestSpecification(reqSpec())
                .addFilters([schemaFilter])
                .setContentType(ContentType.JSON)
                .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                .setBaseUri(PGP_HOST)
                .setBasePath(VALIDATE_OTP)
                .addQueryParams([mid: m()?.id ?: new Random().nextLong().abs() as String, orderId: ORDER_ID.get() ?: new Random().nextLong().abs() as String])
    }

    final def req = { given(reqBldr().build()) }

    private final Filter schemaFilter = new Filter() {
        @Override
        io.restassured.response.Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
            responseSpec.spec(
                    new ResponseSpecBuilder()
                            .expectBody(matchesJsonSchemaInClasspath('json-schemas/theia/validate-otp-schema.json'))
                            .build()
            )
            return ctx.next(requestSpec, responseSpec)
        }
    }

    final def root = {
        [
                head: [
                        channelId: 'WEB',
                        txnToken : TOKEN.get(),
                ],
                body: [
                        otp: (0..5).collect { new Random().nextInt(10) as String }.join('')//M,
                ]
        ]
    }

    private final static class Response {
        static ResponseSpecification SUCCESS = new ResponseSpecBuilder()
                .rootPath("body")
                .expectBody("authenticated", equalTo(true))
                .appendRootPath('resultInfo')
                .expectBody('resultStatus', equalTo('SUCCESS'))
                .expectBody('resultCode', equalTo('01'))
                .build()
        static ResponseSpecification SYSTEM_ERR = new ResponseSpecBuilder()
                .rootPath("body")
                .expectBody("", not(hasKey('authenticated')))
                .appendRootPath('resultInfo')
                .expectBody('resultStatus', equalTo('U'))
                .expectBody('resultCode', equalTo('00000900'))
                .expectBody('resultMsg', equalTo('System error'))
                .build()
    }

    @BeforeMethod
    void setUp() {
        ORDER_ID.set(UUID.randomUUID().toString())
        TOKEN.set(new InitTxn(m().id, m().key, ORDER_ID.get(), 1, user().tokens['sso'].id).execute().path("body.txnToken"))
    }

    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @AUser(edit = true)
    @Test
    void testSuccess() {
        new SendOTP(TOKEN.get(), user().mobile, m().id, ORDER_ID.get()).execute()
        def root = root()
       // root.body.otp = AuthUtil.getOtp(user().mobile)
        root.body.otp = '12346';
        req().body(root).post().then()
                .spec(Response.SUCCESS)
    }

    @Merchant(edit = true, value = { !it.localeEnabled })
    @AUser(edit = true)
    @Test
    void 'test localisation fields are not present when localisation is disabled on merchant but language header is passed'() {
        def root = root()
        root.body.otp = null
        req().header(LANGUAGE_HEADER, LANGUAGE_HEADER_VALUE).body(root).post().then()
                .spec(Response.SYSTEM_ERR)
                .body('body.resultInfo', not(hasKey('resultMsgRegional')))
    }

    @Merchant(edit = true, value = { it.localeEnabled })
    @AUser(edit = true)
    @Test
    void 'test localisation fields are not present when localisation is enabled on merchant but language header is not passed'() {
        def root = root()
        root.body.otp = null
        req().body(root).post().then()
                .spec(Response.SYSTEM_ERR)
                .body('body.resultInfo', not(hasKey('resultMsgRegional')))
    }

    @Merchant(edit = true, value = { it.localeEnabled })
    @AUser(edit = true)
    @Test
    void 'test localisation fields are present when localisation is enabled on merchant and language header is passed'() {
        def root = root()
        root.body.otp = null
        req().header(LANGUAGE_HEADER, LANGUAGE_HEADER_VALUE).body(root).post().then()
                .spec(Response.SYSTEM_ERR)
                .body('body.resultInfo', hasKey('resultMsgRegional'))
    }
}
