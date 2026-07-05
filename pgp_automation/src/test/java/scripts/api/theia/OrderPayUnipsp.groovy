package scripts.api.theia

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.paytm.ServerConfigProvider
import com.paytm.api.HandlerTxnStatusNoAppApi
import com.paytm.api.MockbankUPIIntentPayAPI
import com.paytm.api.qr.GenerateQR
import com.paytm.appconstants.Constants
import com.paytm.apphelpers.LogsValidationHelper
import com.paytm.apphelpers.PGPHelpers
import com.paytm.base.test.TestSetUp
import com.paytm.dto.HANDLERINTERNALTXNSTATUS.HandlerInternalTxnstatusNoAPPDTO
import com.paytm.dto.MockbankUPIIntentPayDTO
import com.paytm.utils.merchant.merchant.util.annotations.Merchant
import com.paytm.utils.merchant.user.annotations.AUser
import groovy.json.JsonSlurper
import io.qameta.allure.Owner
import io.restassured.builder.RequestSpecBuilder
import io.restassured.builder.ResponseSpecBuilder
import io.restassured.filter.Filter
import io.restassured.filter.FilterContext
import io.restassured.http.ContentType
import io.restassured.path.json.JsonPath
import io.restassured.specification.FilterableRequestSpecification
import io.restassured.specification.FilterableResponseSpecification
import io.restassured.specification.ResponseSpecification
import org.assertj.core.api.Assertions
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

import static com.paytm.LocalConfig.JWT_KEY
import static com.paytm.LocalConfig.PGP_HOST
import static com.paytm.appconstants.Constants.Owner.DEEPAK
import static com.paytm.appconstants.Constants.Owner.JAI
import static io.restassured.RestAssured.given
import static org.hamcrest.Matchers.equalTo

@Owner(JAI)
class OrderPayUnipsp extends TestSetUp{

    private static final int POS_ID = 11225;
    private static final String DEFAULT_TIMEOUT_IN_SECONDS = '259200'
    //above property is present in theia on location : /etc/appconf/project/project-theia-biz.properties and property name is : upi.intent.order.timeout.seconds

    private def req() {
        given(
                new RequestSpecBuilder()
                        .addRequestSpecification(reqSpec())
                        .addFilter(setSignatureFilter)
                        .setContentType(ContentType.JSON)
                        .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                        .setBaseUri(PGP_HOST)
                        .setBasePath("theia/v1/order/pay/upipsp")
                        .build()
        )
    }

    final def root = {
        [
            header:
            [
                    clientId        : null,
                    requestTimestamp: System.currentTimeMillis() as String,
                    version         : 'v1',
                    requestMsgId    : 'asdasdasd',
                    signature       : '?'
            ] ,
            body:
                    [
                        requestType: 'SEAMLESS_3D_FORM' ,
                        iss: 'ts' ,
                        custID: '1000036031' ,
                        mid: m().id ,
                        payerVpa: 'arsh.test2@paytm' ,
                        txnAmount: '1.00' ,
                        payeeVpa: 'pqr' + generateQRViaWallet(m().id, POS_ID) + '@paytm',
                        upiOrderTimeOutInSeconds: null
                    ]
        ]
    }

    Filter setSignatureFilter = new Filter() {
        @Override
        io.restassured.response.Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
            def root = new JsonSlurper().parseText(requestSpec.getBody())
            root?.header?.with {
                if (it?.signature == '?') it.signature = JWT.create().withIssuer('ts')
                        .withClaim('requestType',root.body.requestType)
                        .withClaim('iss',root.body.iss)
                        .withClaim('custID',root.body.custID)
                        .withClaim('mid',root.body.mid)
                        .withClaim('payerVpa',root.body.payerVpa)
                        .withClaim('txnAmount',root.body.txnAmount)
                        .withClaim('payeeVpa',root.body.payeeVpa)
                        .sign(Algorithm.HMAC256(JWT_KEY))
            }
            requestSpec.body(root)
            ctx.next(requestSpec, responseSpec)
        }
    }

    private final static class Response {
        static ResponseSpecification SUCCESS = new ResponseSpecBuilder()
                .rootPath('body')
                .expectBody('resultCodeId', equalTo('001'))
                .expectBody('resultCode', equalTo('SUCCESS'))
                .expectBody('resultMsg', equalTo('success'))
                .build()
        static ResponseSpecification FAILURE = new ResponseSpecBuilder()
                .rootPath('body')
                .expectBody('resultCodeId', equalTo('009'))
                .expectBody('resultCode', equalTo('FAIL'))
                .expectBody('resultMsg', equalTo('payment Failure'))
                .build()
    }

    static String generateQRViaWallet(String mid, int POS_ID)
    {
        GenerateQR generateQR = new GenerateQR(mid,"",POS_ID);
        String resp = generateQR.execute().getBody().asString().replace("\\", "").replace("\"{","{").replace("}\"","}");
        JsonPath generateJson = JsonPath.given(resp);
//        JsonPath generateJson = generateQR.execute().jsonPath();
        Assertions.assertThat(generateJson.getString("statusCode")).isEqualTo("200");
        String qrCodeId = generateJson.getString("response.qrCodeId").replaceAll("\\p{P}", "");
        return qrCodeId;
    }

    @Owner(JAI)
    @Merchant({it.id == Constants.MerchantType.UPI_INTENT.id})
    @AUser
    @Test(description = 'Verify that udf1 value is passed in COP request')
    void 'Verify udf1 and posId passed in Create Order & Pay Request'() {
        def root = root()
        def res = req().body(root).post().then()
                .spec(Response.SUCCESS)
                .extract()

        String grepcmd = "grep \"" + res.jsonPath().get('body.orderId') + "\" /paytm/logs/theia_facade.log | " +
                "grep \"" + root.body.mid +"\" | grep \"ACQUIRING_CREATE_ORDER_AND_PAY\" | grep \"REQUEST\""
        String theiafacadelogs = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY,grepcmd);
        Assertions.assertThat(theiafacadelogs).contains('\\"posId\\":\\"' + POS_ID + '\\"');
        Assertions.assertThat(theiafacadelogs).contains('\\"udf1\\":\\"' + POS_ID +'\\"');
    }

    @Owner(JAI)
    @Merchant({it.id == Constants.MerchantType.UPI_INTENT.id})
    @AUser
    @Test(description = 'Verify that udf1 and posId value in txnStatus after COP request')
    void 'Verify payment status after Create Order & Pay'() {
        def root = root()
        def res = req().body(root).post().then()
                .spec(Response.SUCCESS)
                .extract()
        MockbankUPIIntentPayDTO mockbankUPIIntentPayDTO = new MockbankUPIIntentPayDTO(root.body.txnAmount, res.jsonPath().get('body.orderId')
                , res.jsonPath().get('body.externalSerialNo'), root.body.mid)
        MockbankUPIIntentPayAPI mockbankUPIIntentPayAPI = new MockbankUPIIntentPayAPI(mockbankUPIIntentPayDTO)
        io.restassured.response.Response response = mockbankUPIIntentPayAPI.execute()
        Assertions.assertThat(response.jsonPath().get("body.resultCode")).isEqualTo("SUCCESS")
        Assertions.assertThat(response.jsonPath().get("body.resultCodeId")).isEqualTo("001")
        HandlerInternalTxnstatusNoAPPDTO handlerInternalTxnstatusNoAPPDTO = new HandlerInternalTxnstatusNoAPPDTO()
        handlerInternalTxnstatusNoAPPDTO.setMID(root.body.mid).setORDERID(res.jsonPath().get('body.orderId'))
        HandlerTxnStatusNoAppApi handlerTxnStatusApi = new HandlerTxnStatusNoAppApi(handlerInternalTxnstatusNoAPPDTO)
        io.restassured.response.Response txnStatusResp = handlerTxnStatusApi.executeUntilNotPending()
        Assertions.assertThat(txnStatusResp.jsonPath().get("POS_ID")).isEqualTo(String.valueOf(POS_ID))
        Assertions.assertThat(txnStatusResp.jsonPath().get("UDF_1")).isEqualTo(String.valueOf(POS_ID))
    }

    @DataProvider
    static Object[][] timeOuts() {
        String negative = '-1'
        String zero = '0'
        String positive = '100'
        String extremelyHigh = (0..9).collect {'9'}.join('')
        String nonNumeric = 'fafasasf#@#@#@asa'
        [
                [negative, negative, Response.FAILURE],
                [zero, zero, Response.FAILURE],
                [positive, positive, Response.SUCCESS],
                [extremelyHigh, extremelyHigh, Response.FAILURE],
                [nonNumeric, DEFAULT_TIMEOUT_IN_SECONDS, Response.SUCCESS],
        ]
    }

    @Owner(DEEPAK)
    @Merchant({it.id == Constants.MerchantType.UPI_INTENT.id})
    @Test(dataProvider = 'timeOuts')
    void "test if 'upiOrderTimeOutInSeconds' parameter's value is forwarded in 'timeoutInSeconds' parameter of COP request"(String upiOrderTimeOutInSeconds, String timeoutInSeconds, ResponseSpecification response) {
        def root = root()
        root.body.upiOrderTimeOutInSeconds = upiOrderTimeOutInSeconds
        def res = req().body(root).post().then()
                .spec(response)
                .extract()
        String cmdToFetchCOPRequest = "grep '${res.jsonPath().get('body.orderId')}' /paytm/logs/theia_facade.log | " +
                "grep ${root.body.mid} | grep 'ACQUIRING_CREATE_ORDER_AND_PAY' | grep 'REQUEST'"
        String theiaFacadelogs = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, cmdToFetchCOPRequest);
        Assertions.assertThat(theiaFacadelogs).contains('"timeoutInSeconds":"' + timeoutInSeconds + '"')
    }

    @Owner(DEEPAK)
    @Merchant({it.id == Constants.MerchantType.UPI_INTENT.id})
    @Test
    void "test upiOrderTimeOutInSeconds is optional"() {
        def root = root()
        root.body.remove('upiOrderTimeOutInSeconds')
        req().body(root).post().then()
        .spec(Response.SUCCESS)
    }

    @Owner(DEEPAK)
    @Merchant({it.id == Constants.MerchantType.UPI_INTENT.id})
    @Test
    void "test 'timeoutInSeconds' parameter of COP request should be 1800 when 'upiOrderTimeOutInSeconds' parameter is not passed"() {
        def root = root()
        root.body.remove('upiOrderTimeOutInSeconds')
        def res = req().body(root).post().then().extract()
        String cmdToFetchCOPRequest = "grep '${res.jsonPath().get('body.orderId')}' /paytm/logs/theia_facade.log | " +
                "grep ${root.body.mid} | grep 'ACQUIRING_CREATE_ORDER_AND_PAY' | grep 'REQUEST'"
        String theiaFacadelogs = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, cmdToFetchCOPRequest);
        Assertions.assertThat(theiaFacadelogs).contains('"timeoutInSeconds":"' + DEFAULT_TIMEOUT_IN_SECONDS + '"')
    }
}
