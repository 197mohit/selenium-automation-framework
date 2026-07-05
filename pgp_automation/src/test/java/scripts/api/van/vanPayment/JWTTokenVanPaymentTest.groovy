package scripts.api.van.vanPayment


import com.paytm.base.test.TestSetUp
import com.paytm.utils.merchant.merchant.util.annotations.Merchant
import groovy.json.JsonSlurper
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.qameta.allure.Link
import io.qameta.allure.Owner
import io.restassured.builder.RequestSpecBuilder
import io.restassured.builder.ResponseSpecBuilder
import io.restassured.filter.Filter
import io.restassured.filter.FilterContext
import io.restassured.http.ContentType
import io.restassured.response.Response
import io.restassured.specification.FilterableRequestSpecification
import io.restassured.specification.FilterableResponseSpecification
import io.restassured.specification.RequestSpecification
import org.hamcrest.Matchers
import org.testng.annotations.Test

import static com.paytm.LocalConfig.PGP_HOST
import static com.paytm.appconstants.Constants.VANProxy.VAN_PROXY_PAYMENT
import static io.restassured.RestAssured.given
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath
import static org.hamcrest.Matchers.*

@Owner('Karmvir | Deepak')
@Link(url = 'https://wiki.mypaytm.com/display/PGP/%5BSA%5DVAN+service')
class JWTTokenVanPaymentTest extends TestSetUp implements VanPaymentTest {

    private final Filter setTokenFilter = new Filter() {
        @Override
        Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
            def root = new JsonSlurper().parseText(requestSpec.getBody())
            if (requestSpec.getHeaders().get('Authorization') == null) {
                requestSpec.replaceHeader(
                        'Authorization',
                        Jwts.builder()
                                .setIssuer('PAYTMBANK')
                                .addClaims(new JsonSlurper().parseText(requestSpec.getBody()))
                                .signWith(SignatureAlgorithm.HS256, Base64.getEncoder().encodeToString('7nsFaObLRz-gQ7VH1anTD-qY8iiWeUc4lvVTHdfObTc='.getBytes()))
                                .compact()
                )
            }
            ctx.next(requestSpec, responseSpec)
        }
    }

    private final Filter schemaFilter = new Filter() {
        @Override
        Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
            responseSpec.spec(
                    new ResponseSpecBuilder()
                            .expectBody(matchesJsonSchemaInClasspath('json-schemas/van-proxy/van-payment-schema.json'))
                            .build()
            )
            return ctx.next(requestSpec, responseSpec)
        }
    }

    private final RequestSpecBuilder reqBldr() {
        new RequestSpecBuilder()
                .addRequestSpecification(reqSpec())
                .addFilters([setTokenFilter, schemaFilter])
                .setContentType(ContentType.JSON)
                .setBaseUri(PGP_HOST)
                .setBasePath(VAN_PROXY_PAYMENT)
    }

    private final RequestSpecification req() {
        given(reqBldr().build())
    }

    private final Map root() {
        [
                event_tracking_id: UUID.randomUUID().toString(),
                ca_id            : '12345',
                data             : [
                        status                  : 'SUCCESS',
                        amount                  : '1',
                        vanNumber               : 'PY7777K735789163',
                        beneficiaryAccountNumber: '919245673456',
                        beneficiaryIfsc         : 'PYTM0123456',
                        remitterAccountNumber   : '919234567913',
                        remitterIfsc            : 'PYTM12345',
                        remitterNbin            : '1234',
                        remitterName            : 'XXXXXXXXXXXX',
                        bankTxnIdentifier       : '4194200672312649',
                        transactionRequestId    : '6241897169224789',
                        transferMode            : 'NEFT',
                        responseCode            : '0',
                        transactionDate         : '2020-10-12 10:34:12',
                        transactionType         : 'VAN_INWARD',
                        parentUtr               : '67867676SFS6235',
                        meta                    : [
                                'Mobile Number'  : '5267478725',
                                mid              : m().id,
                                identificationNo : '123412345',
                                vanExtendInfo    : '{\"purpose\":\"Dont\",\"merchantPrefix\":\"7777\"}',
                                userDefinedFields: '{\"udf1\":\"blah1234567890kjhgfdszxcvbnjr456789ihvbgfdszxcv\",\"udf2\":\"blah1234567890kjhgfdszxcvbnjr456789ihvbgfdszxcv\",\"udf3\":\"blah1234567890kjhgfdszxcvbnjr456789ihvbgfdszxcv\",\"udf4\":\"blah1234567890kjhgfdszxcvbnjr456789ihvbgfdszxcv\",\"udf5\":\"blah1234567890kjhgfdszxcvbnjr456789ihvbgfdszxcv\"}',
                                customerDetails  : '[{\"customerEmail\":\"vaibhav41094@gmail.com\",\"customerMobile\":\"9599749261\",\"customerName\":\"test\"}]',
                        ]
                ]
        ]
    }

    @Override
    @Merchant(edit = false, value = { it.preferences.bankTransfer.enabled == true && it.acquirings.any { it.payMode == 'bank-transfer' && it.enabled == true } })
    @Test
    void "Validate the successful txn using callback PPBL api"() {
        def root = root()
        root.data.transactionRequestId = (0..5).collect { new Random().nextInt(10) }.join('');
        root.event_tracking_id=UUID.randomUUID().toString();
        req().body(root).post().then().
                body("response_code", equalTo("CL_2000")).
                body("data.status", equalTo("SUCCESS")).
                body("event_tracking_id", Matchers.equalTo(root.event_tracking_id)).
                body("data.errorCode", equalTo("7000"));

    }

    @Override
    @Merchant(edit = false, value = { it.preferences.bankTransfer.enabled == true && it.acquirings.any { it.payMode == 'bank-transfer' && it.enabled == true } })
    @Test(groups = "Security")
    void "Validate the response of callback PPBL api when invalid JWT token is provided"() {
        def root = root()
        root.data.transactionRequestId = (0..5).collect { new Random().nextInt(10) }.join('');
        req().header('Authorization', 'gubhejacdliuhnkjeraiscnefac').body(root).post().then().
                body("response_code", equalTo("CL_2000")).
                body("data.status", equalTo("FAILURE")).
                body("data.transactionType", Matchers.equalTo("REVERSAL")).
                body("data.errorMessage", equalTo("Jwt Validation Failure"));
    }

    @Override
    @Merchant(edit = false, value = { it.preferences.bankTransfer.enabled == true && it.acquirings.any { it.payMode == 'bank-transfer' && it.enabled == true } })
    @Test
    void "Validate the response of callback PPBL api when event_tracking_id is null "() {
        def root = root()
        root.data.transactionRequestId = (0..5).collect { new Random().nextInt(10) }.join('');
        root.event_tracking_id = null;
        req().body(root).post().then().
                body("response_code", equalTo("CL_2000")).
                body("data.status", equalTo("FAILURE")).
                body("data.transactionType", Matchers.equalTo("REVERSAL")).
                body("data.errorMessage", equalTo("event_tracking_id : event_tracking_id should not be empty : rejected value [null]"));

    }


    @Override
    @Merchant(edit = false, value = { it.preferences.bankTransfer.enabled == true && it.acquirings.any { it.payMode == 'bank-transfer' && it.enabled == true } })
    @Test
    void "Validate the response of callback PPBL api when status is null"() {
        def root = root()
        root.data.transactionRequestId = (0..5).collect { new Random().nextInt(10) }.join('');
        root.data.status = null
        req().body(root).post().then().
                body("response_code", equalTo("CL_2000")).
                body("data.status", equalTo("FAILURE")).
                body("data.transactionType", Matchers.equalTo("REVERSAL")).
                body("data.errorMessage", equalTo("data.status : status should not be empty : rejected value [null]"));
    }

    @Override
    @Merchant(edit = false, value = { it.preferences.bankTransfer.enabled == true && it.acquirings.any { it.payMode == 'bank-transfer' && it.enabled == true } })
    @Test
    void "Validate the response of call back PPBL api when Status is failure"() {
        def root = root()
        root.data.transactionRequestId = (0..5).collect { new Random().nextInt(10) }.join('');
        root.data.status = "failure";
        root.event_tracking_id=UUID.randomUUID().toString();
        req().body(root).post().then().
                body("response_code", equalTo("CL_2000")).
                body("data.status", equalTo("SUCCESS")).
                body("event_tracking_id", Matchers.equalTo(root.event_tracking_id)).
                body("data.errorCode", equalTo("7000"));
    }

    @Override
    @Merchant(edit = false, value = { it.preferences.bankTransfer.enabled == true && it.acquirings.any { it.payMode == 'bank-transfer' && it.enabled == true } })
    @Test
    void "Validate the response of callback PPBL api when vanNumber is null"() {
        def root = root()
        root.data.transactionRequestId = (0..5).collect { new Random().nextInt(10) }.join('');
        root.data.vanNumber = null
        req().body(root).post().then().
                body("response_code", equalTo("CL_2000")).
                body("data.status", equalTo("FAILURE")).
                body("data.transactionType", Matchers.equalTo("REVERSAL")).
                body("data.errorMessage", equalTo("data.vanNumber : vanNumber should not be empty : rejected value [null]"));
    }

    @Override
    @Merchant(edit = false, value = { it.preferences.bankTransfer.enabled == true && it.acquirings.any { it.payMode == 'bank-transfer' && it.enabled == true } })
    @Test
    void "Validate the response of callback PPBL api when beneficiaryAccountNumber is null"() {
        def root = root()
        root.data.transactionRequestId = (0..5).collect { new Random().nextInt(10) }.join('');
        root.data.beneficiaryAccountNumber = null
        req().body(root).post().then().
                body("response_code", equalTo("CL_2000")).
                body("data.status", equalTo("FAILURE")).
                body("data.transactionType", Matchers.equalTo("REVERSAL")).
                body("data.errorMessage", equalTo("data.beneficiaryAccountNumber : beneficiaryAccountNumber should not be empty : rejected value [null]"));
    }

    @Override
    @Merchant(edit = false, value = { it.preferences.bankTransfer.enabled == true && it.acquirings.any { it.payMode == 'bank-transfer' && it.enabled == true } })
    @Test
    void "Validate the response of callback PPBL api when beneficiaryIfsc is null"() {
        def root = root()
        root.data.transactionRequestId = (0..5).collect { new Random().nextInt(10) }.join('');
        root.data.beneficiaryIfsc = null
        req().body(root).post().then().
                body("response_code", equalTo("CL_2000")).
                body("data.status", equalTo("FAILURE")).
                body("data.transactionType", Matchers.equalTo("REVERSAL")).
                body("data.errorMessage", equalTo("data.beneficiaryIfsc : beneficiaryIfsc should not be empty : rejected value [null]"));
    }

    @Override
    @Merchant(edit = false, value = { it.preferences.bankTransfer.enabled == true && it.acquirings.any { it.payMode == 'bank-transfer' && it.enabled == true } })
    @Test
    void "Validate the response of callback PPBL api when remitterAccountNumber is null"() {
        def root = root()
        root.data.transactionRequestId = (0..5).collect { new Random().nextInt(10) }.join('');
        root.data.remitterAccountNumber = null
        req().body(root).post().then().
                body("response_code", equalTo("CL_2000")).
                body("data.status", equalTo("FAILURE")).
                body("data.transactionType", Matchers.equalTo("REVERSAL")).
                body("data.errorMessage", equalTo("data.remitterAccountNumber : remitterAccountNumber should not be empty : rejected value [null]"));
    }

    @Override
    @Merchant(edit = false, value = { it.preferences.bankTransfer.enabled == true && it.acquirings.any { it.payMode == 'bank-transfer' && it.enabled == true } })
    @Test
    void "Validate the response of callback PPBL api when remitterName is null"() {
        def root = root()
        root.data.transactionRequestId = (0..5).collect { new Random().nextInt(10) }.join('');
        root.data.remitterName = null
        req().body(root).post().then().
                body("response_code", equalTo("CL_2000")).
                body("data.status", equalTo("FAILURE")).
                body("data.transactionType", Matchers.equalTo("REVERSAL")).
                body("data.errorMessage", equalTo("data.remitterName : remitterName should not be empty : rejected value [null]"));
    }

    @Override
    @Merchant(edit = false, value = { it.preferences.bankTransfer.enabled == true && it.acquirings.any { it.payMode == 'bank-transfer' && it.enabled == true } })
    @Test
    void "Validate the response of callback PPBL api when amount is 0"() {
        def root = root()
        root.data.transactionRequestId = (0..5).collect { new Random().nextInt(10) }.join('');
        root.data.amount = "0";
        req().body(root).post().then().
                body("response_code", equalTo("CL_2000")).
                body("data.status", equalTo("FAILURE")).
                body("data.transactionType", Matchers.equalTo("REVERSAL")).
                body("data.errorMessage", equalTo("Transaction Failure"));
    }

    @Override
    @Merchant(edit = false, value = { it.preferences.bankTransfer.enabled == true && it.acquirings.any { it.payMode == 'bank-transfer' && it.enabled == true } })
    @Test
    void "Validate the response of callback PPBL api when banktxnidentifier is null"() {
        def root = root()
        root.data.transactionRequestId = (0..5).collect { new Random().nextInt(10) }.join('');
        root.data.bankTxnIdentifier = null
        req().body(root).post().then().
                body("response_code", equalTo("CL_2000")).
                body("data.status", equalTo("FAILURE")).
                body("data.transactionType", Matchers.equalTo("REVERSAL")).
                body("data.errorMessage", equalTo("data.bankTxnIdentifier : bankTxnIdentifier should not be empty : rejected value [null]"));
    }

    @Override
    @Merchant(edit = false, value = { it.preferences.bankTransfer.enabled == true && it.acquirings.any { it.payMode == 'bank-transfer' && it.enabled == true } })
    @Test
    void "Validate the response of callback PPBL api when transactionRequestId is null"() {
        def root = root()
        root.data.transactionRequestId = (0..5).collect { new Random().nextInt(10) }.join('');
        root.data.transactionRequestId = null
        req().body(root).post().then().
                body("response_code", equalTo("CL_2000")).
                body("data.status", equalTo("FAILURE")).
                body("data.transactionType", Matchers.equalTo("REVERSAL")).
                body("data.errorMessage", equalTo("data.transactionRequestId : transactionRequestId should not be empty : rejected value [null]"));
    }

    @Override
    @Merchant(edit = false, value = { it.preferences.bankTransfer.enabled == true && it.acquirings.any { it.payMode == 'bank-transfer' && it.enabled == true } })
    @Test
    void "Validate the response of callback PPBL api when transferMode is null"() {
        def root = root()
        root.data.transactionRequestId = (0..5).collect { new Random().nextInt(10) }.join('');
        root.data.transferMode = null
        req().body(root).post().then().
                body("response_code", equalTo("CL_2000")).
                body("data.status", equalTo("FAILURE")).
                body("data.transactionType", Matchers.equalTo("REVERSAL")).
                body("data.errorMessage", equalTo("data.transferMode : transferMode should not be empty : rejected value [null]"));
    }

    @Override
    @Merchant(edit = false, value = { it.preferences.bankTransfer.enabled == true && it.acquirings.any { it.payMode == 'bank-transfer' && it.enabled == true } })
    @Test
    void "Validate the response of callback PPBL api when transactionDate is null"() {
        def root = root()
        root.data.transactionRequestId = (0..5).collect { new Random().nextInt(10) }.join('');
        root.data.transactionDate = null
        req().body(root).post().then().
                body("response_code", equalTo("CL_2000")).
                body("data.status", equalTo("FAILURE")).
                body("data.transactionType", Matchers.equalTo("REVERSAL")).
                body("data.errorMessage", equalTo("data.transactionDate : transactionDate should not be empty : rejected value [null]"));
    }

    @Override
    @Merchant(edit = false, value = { it.preferences.bankTransfer.enabled == true && it.acquirings.any { it.payMode == 'bank-transfer' && it.enabled == true } })
    @Test
    void "Validate the response of callback PPBL api when transactionType is null"() {
        def root = root()
        root.data.transactionRequestId = (0..5).collect { new Random().nextInt(10) }.join('');
        root.data.transactionType = null
        req().body(root).post().then().
                body("response_code", equalTo("CL_2000")).
                body("data.status", equalTo("FAILURE")).
                body("data.transactionType", Matchers.equalTo("REVERSAL")).
                body("data.errorMessage", equalTo("data.transactionType : transactionType should not be empty : rejected value [null]"));
    }

    @Override
    @Merchant(edit = false, value = { it.preferences.bankTransfer.enabled == true && it.acquirings.any { it.payMode == 'bank-transfer' && it.enabled == true } })
    @Test
    void "Validate the response of callback PPBL api when responseCode is null"() {
        def root = root()
        root.data.transactionRequestId = (0..5).collect { new Random().nextInt(10) }.join('');
        root.data.responseCode = null
        req().body(root).post().then().
                body("response_code", equalTo("CL_2000")).
                body("data.status", equalTo("FAILURE")).
                body("data.transactionType", Matchers.equalTo("REVERSAL")).
                body("data.errorMessage", equalTo("data.responseCode : responseCode should not be empty : rejected value [null]"));
    }

    @Override
    @Merchant(edit = false, value = { it.preferences.bankTransfer.enabled == true && it.acquirings.any { it.payMode == 'bank-transfer' && it.enabled == true } })
    @Test
    void "Validate the response of callback PPBL api when amount is greater then 2 decimal"() {
        def root = root()
        root.data.transactionRequestId = (0..5).collect { new Random().nextInt(10) }.join('');
        root.data.amount="10.123";
        req().body(root).post().then().
                body("response_code", equalTo("CL_2000")).
                body("data.status", equalTo("FAILURE")).
                body("data.errorMessage", equalTo("Transaction Failure"));
    }

    @Override
    @Merchant(edit = false, value = { it.preferences.bankTransfer.enabled == true && it.acquirings.any { it.payMode == 'bank-transfer' && it.enabled == true } })
    @Test
    void "Validate the response of callback PPBL api when transferMode is IMPS"() {
        def root = root()
        root.data.transactionRequestId = (0..5).collect { new Random().nextInt(10) }.join('');
        root.data.transferMode="IMPS";
        root.event_tracking_id=UUID.randomUUID().toString();
        req().body(root).post().then().
                body("response_code", equalTo("CL_2000")).
                body("data.status", equalTo("SUCCESS")).
                body("event_tracking_id", Matchers.equalTo(root.event_tracking_id)).
                body("data.errorCode", equalTo("7000"));
    }
}
