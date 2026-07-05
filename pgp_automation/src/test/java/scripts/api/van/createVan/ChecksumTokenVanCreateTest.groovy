package scripts.api.van.createVan

import com.paytm.apphelpers.PGPHelpers
import com.paytm.base.test.TestSetUp
import com.paytm.utils.merchant.merchant.util.annotations.Merchant
import com.paytm.utils.merchant.util.PGPUtil
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import io.qameta.allure.Link
import io.qameta.allure.Links
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
import org.testng.annotations.Test

import static com.paytm.LocalConfig.PGP_HOST
import static com.paytm.appconstants.Constants.VANProxy.VAN_PROXY_CREATE
import static io.restassured.RestAssured.given
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath
import static org.hamcrest.Matchers.*

@Owner('Karmvir | Deepak')
@Links([
        @Link(url = 'https://wiki.mypaytm.com/display/PGP/Create+VAN+-+Virtual+Account+Number'),
        @Link(url = 'https://wiki.mypaytm.com/display/PGP/%5BSA%5DVAN+service')
])
class ChecksumTokenVanCreateTest extends TestSetUp implements VanCreateTest {

    private final Filter setTokenFilter = new Filter() {
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

    private final Filter schemaFilter = new Filter() {
        @Override
        Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
            responseSpec.spec(
                    new ResponseSpecBuilder()
                            .expectBody(matchesJsonSchemaInClasspath('json-schemas/van-proxy/van-create-schema.json'))
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
                .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                .setBaseUri(PGP_HOST)
                .setBasePath(VAN_PROXY_CREATE)
    }

    final RequestSpecification req() {
        given(reqBldr().build())
    }

    final Map root() {
        [
                head: [
                        version         : 'v1',
                        clientId        : 'c11',
                        channelId       : 'SYSTEM',
                        requestTimestamp: System.currentTimeMillis().toString(),
                        tokenType       : 'CHECKSUM',
                        token           : '?',
                ],
                body: [
                        mid       : m().id,
                        requestId : UUID.randomUUID().toString(),
                        vanDetails: [
                                [
                                        identificationNo : (0..9).collect { new Random().nextInt(10) }.join(''),
                                        purpose          : 'Dont',
                                        merchantPrefix   : '1111',
                                        customerDetails  : [
                                                [
                                                        customerEmail : 'kp@gmail.com',
                                                        customerMobile: 'hjbcajce',
                                                        customerName  : 'test',
                                                ]
                                        ],
                                        userDefinedFields: [
                                                udf1: UUID.randomUUID().toString(),
                                                udf2: UUID.randomUUID().toString(),
                                                udf3: UUID.randomUUID().toString(),
                                                udf4: UUID.randomUUID().toString(),
                                                udf5: UUID.randomUUID().toString(),
                                        ],
                                ]
                        ]
                ]
        ]
    }

    @Override
    @Merchant(edit = false, value = { it.preferences.bankTransfer.enabled == true && it.acquirings.any { it.payMode == 'bank-transfer' && it.enabled == true } })
    @Test
    void "Validate the create van api response when all mandatory parameters are provided"() {
        def root = root();
        root.body.mid = m().id;
        req().body(root).post().then().
                body("body.resultInfo.resultStatus", equalTo("S")).
                body("body.resultInfo.resultMsg", equalTo("SUCCESS")).
                body("body.vanDetails[0].responseStatus", equalTo("SUCCESS"));
    }

    @Override
    @Merchant(edit = false, value = { it.preferences.bankTransfer.enabled == true && it.acquirings.any { it.payMode == 'bank-transfer' && it.enabled == true } })
    @Test
    void "validate the response of create van api when token validation failed"() {
        def root = root();
        root.head.token = "bvwbubajnoi824ufewjk";
        root.body.mid = m().id;
        req().body(root).post().then().
                body("body.resultInfo.resultStatus", equalTo("F")).
                body("body.resultInfo.resultMsg", equalTo("CheckSum Validation Failure"));

    }

    @Override
    @Test
    @Merchant(edit = false, value = { it.preferences.bankTransfer.enabled == false })
    void "validate the response of create van api when mid is provided which is not having bank transfer as paymode"() {
        def root = root();
        root.body.mid = m().id;
        req().body(root).post().then().
                body("body.resultInfo.resultStatus", equalTo("F")).
                body("body.resultInfo.resultMsg", equalTo("Bank Transfer not supported"));


    }

//    @Override
//    @Merchant(edit = false, value = { it.preferences.bankTransfer.enabled == true && it.acquirings.any { it.payMode == 'bank-transfer' && it.enabled == true } })
//    @Test(enabled = false)
    void "validate the response of create van api when same identification number for which VAN is exist"() {
        String identificationNo
        String merchantPrefix
        VAN_PROXY_CREATE:
        {
            final ChecksumTokenVanCreateTest checksumTokenVanCreateAPI = new ChecksumTokenVanCreateTest()
            def root = checksumTokenVanCreateAPI.root()
            identificationNo = checksumTokenVanCreateAPI.req().body(root).post().path('body.vanDetails[0].identificationNo')
            merchantPrefix = checksumTokenVanCreateAPI.req().body(root).post().path('body.vanDetails[0].merchantPrefix')
        }
        def root = root();
        root.body.mid = m().id;
        root.body.vanDetails[0].identificationNo = identificationNo;
        root.body.vanDetails[0].merchantPrefix = merchantPrefix;
        req().body(root).post().then().
                body("body.resultInfo.resultStatus", equalTo("S")).
                body("body.vanDetails[0].responseStatus", equalTo("FAILURE")).
                body("body.vanDetails[0].errorMessage", equalTo("Already exists. Please try with new identifier"));
    }

    @Override
    @Merchant(edit = false, value = { it.preferences.bankTransfer.enabled == true && it.acquirings.any { it.payMode == 'bank-transfer' && it.enabled == true } })
    @Test
    void "Validate the response of create van api when length of identification number is more then 10"() {
        def root = root();
        root.body.mid = m().id;
        root.body.vanDetails[0].identificationNo = (0..11).collect { new Random().nextInt(10) }.join('');
        req().body(root).post().then().
                body("body.resultInfo.resultStatus", equalTo("S")).
                body("body.vanDetails[0].errorMessage", equalTo("Invalid identificationNo")).
                body("body.vanDetails[0].responseStatus", equalTo("FAILURE"));

    }

    @Override
    @Merchant(edit = false, value = { it.preferences.bankTransfer.enabled == true && it.acquirings.any { it.payMode == 'bank-transfer' && it.enabled == true } })
    @Test
    void "Validate the response of create van api when length of identification number is less then 10"() {
        def root = root();
        root.body.mid = m().id;
        root.body.vanDetails[0].identificationNo = (0..5).collect { new Random().nextInt(10) }.join('');
        req().body(root).post().then().
                body("body.resultInfo.resultStatus", equalTo("S")).
                body("body.vanDetails[0].errorMessage", equalTo("Invalid identificationNo")).
                body("body.vanDetails[0].responseStatus", equalTo("FAILURE"));

    }

    @Override
    @Merchant(edit = false, value = { it.preferences.bankTransfer.enabled == true && it.acquirings.any { it.payMode == 'bank-transfer' && it.enabled == true } })
    @Test
    void "Validate the response of create van api when merchant prefix length is provided more then 4"() {
        def root = root();
        root.body.mid = m().id;
        root.body.vanDetails[0].merchantPrefix = (0..9).collect { new Random().nextInt(10) }.join('');
        req().body(root).post().then().
                body("body.resultInfo.resultStatus", equalTo("S")).
                body("body.vanDetails[0].errorMessage", equalTo("Invalid merchant prefix")).
                body("body.vanDetails[0].responseStatus", equalTo("FAILURE"));
    }

    @Override
    @Merchant(edit = false, value = { it.preferences.bankTransfer.enabled == true && it.acquirings.any { it.payMode == 'bank-transfer' && it.enabled == true } })
    @Test
    void "Validate the response of create van api when merchant prefix length is provided less then 4"() {
        def root = root();
        root.body.mid = m().id;
        root.body.vanDetails[0].merchantPrefix = (0..2).collect { new Random().nextInt(10) }.join('');
        req().body(root).post().then().
                body("body.resultInfo.resultStatus", equalTo("S")).
                body("body.vanDetails[0].errorMessage", equalTo("Invalid merchant prefix")).
                body("body.vanDetails[0].responseStatus", equalTo("FAILURE"));
    }

    @Override
    @Merchant(edit = false, value = { it.preferences.bankTransfer.enabled == true && it.acquirings.any { it.payMode == 'bank-transfer' && it.enabled == true } })
    @Test
    void "validate the response of create van api when more then 1 van details are provided"() {
        def root = root();
        root.body.mid = m().id;
        root.body.vanDetails = (0..1).collect { root.body.vanDetails[0].clone() }
        root.body.vanDetails[0].identificationNo = (0..9).collect { new Random().nextInt(10) }.join('');
        root.body.vanDetails[1].identificationNo = (0..9).collect { new Random().nextInt(10) }.join(''); ;
        req().body(root).post().then()
                .body("body.resultInfo.resultStatus", equalTo("S")).
                body("body.vanDetails[0].responseStatus", equalTo("SUCCESS")).
                body("body.vanDetails[1].responseStatus", equalTo("SUCCESS"));

    }

    @Override
    @Merchant(edit = false, value = { it.preferences.bankTransfer.enabled == true && it.acquirings.any { it.payMode == 'bank-transfer' && it.enabled == true } })
    @Test
    void "Validate the response of create van api when requestid provided which is having more then 64 digit"() {
        def root = root();
        root.body.mid = m().id;
        root.body.requestId = (0..64).collect { new Random().nextInt(10) }.join('');
        req().body(root).post().then().
                body("body.resultInfo.resultStatus", equalTo("F")).
                body("body.resultInfo.resultCode", equalTo("0001"));
    }

    @Override
    @Merchant(edit = false, value = { it.preferences.bankTransfer.enabled == true && it.acquirings.any { it.payMode == 'bank-transfer' && it.enabled == true } })
    @Test
    void "Validate the create van api response when mid is empty"() {
        def root= root()
        root.body.mid=''
        req().body(root).post().then().
                body("body.resultInfo.resultStatus", equalTo("F")).
                body("body.resultInfo.resultMsg", equalTo("Incorrect mid"));

    }

    @Override
    @Merchant(edit = false, value = { it.preferences.bankTransfer.enabled == true && it.acquirings.any { it.payMode == 'bank-transfer' && it.enabled == true } })
    @Test
    void "Validate the response of create van api when 2 VAN details are provided in which 1 is correct and 1 is incorrect"() {
        def root = root();
        root.body.mid = m().id;
        root.body.vanDetails = (0..1).collect { root.body.vanDetails[0].clone() }
        root.body.vanDetails[0].identificationNo = (0..9).collect { new Random().nextInt(10) }.join('');
        root.body.vanDetails[1].identificationNo = (0..11).collect { new Random().nextInt(10) }.join(''); ;
        req().body(root).post().then()
                .body("body.resultInfo.resultStatus", equalTo("S")).
                body("body.vanDetails[0].responseStatus", equalTo("SUCCESS")).
                body("body.vanDetails[1].errorMessage", equalTo("Invalid identificationNo")).
                body("body.vanDetails[1].responseStatus", equalTo("FAILURE"));
    }

    @Override
    @Merchant(edit = false, value = { it.preferences.bankTransfer.enabled == true && it.acquirings.any { it.payMode == 'bank-transfer' && it.enabled == true } })
    @Test
    void "validate the response of create van api when more then 10 van details are provided"() {
        def root = root();
        root.body.mid = m().id;
        root.body.vanDetails = (0..10).collect { root.body.vanDetails[0].clone() }
        root.body.vanDetails[0].identificationNo = (0..9).collect { new Random().nextInt(10) }.join('');
        root.body.vanDetails[1].identificationNo = (0..9).collect { new Random().nextInt(10) }.join('');
        root.body.vanDetails[2].identificationNo = (0..9).collect { new Random().nextInt(10) }.join('');
        root.body.vanDetails[3].identificationNo = (0..9).collect { new Random().nextInt(10) }.join('');
        root.body.vanDetails[4].identificationNo = (0..9).collect { new Random().nextInt(10) }.join('');
        root.body.vanDetails[5].identificationNo = (0..9).collect { new Random().nextInt(10) }.join('');
        root.body.vanDetails[6].identificationNo = (0..9).collect { new Random().nextInt(10) }.join('');
        root.body.vanDetails[7].identificationNo = (0..9).collect { new Random().nextInt(10) }.join('');
        root.body.vanDetails[8].identificationNo = (0..9).collect { new Random().nextInt(10) }.join('');
        root.body.vanDetails[9].identificationNo = (0..9).collect { new Random().nextInt(10) }.join('');
        root.body.vanDetails[10].identificationNo = (0..9).collect { new Random().nextInt(10) }.join('');
        req().body(root).post().then().
                body("body.resultInfo.resultStatus", equalTo("F")).
                body("body.resultInfo.resultCode", equalTo("0001"));
    }

}
