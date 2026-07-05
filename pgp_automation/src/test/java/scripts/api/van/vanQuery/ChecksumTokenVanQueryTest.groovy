package scripts.api.van.vanQuery

import com.paytm.base.test.TestSetUp
import com.paytm.utils.merchant.merchant.util.annotations.Merchant
import com.paytm.utils.merchant.util.PGPUtil
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
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
import org.testng.annotations.Test
import scripts.api.van.createVan.ChecksumTokenVanCreateTest

import static com.paytm.LocalConfig.PGP_HOST
import static com.paytm.appconstants.Constants.VANProxy.VAN_PROXY_QUERY
import static io.restassured.RestAssured.given
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath
import static org.hamcrest.Matchers.*

@Owner('Karmvir | Deepak')
@Link(url = 'https://wiki.mypaytm.com/display/PGP/%5BSA%5DVAN+service')
class ChecksumTokenVanQueryTest extends TestSetUp implements VanQueryTest {

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
                            .expectBody(matchesJsonSchemaInClasspath('json-schemas/van-proxy/van-query-schema.json'))
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
                .setBasePath(VAN_PROXY_QUERY)
    }

    private final RequestSpecification req() {
        given(reqBldr().build())
    }

    private final Map root() {
        [
                head: [
                        version         : '',
                        clientId        : 'c11',
                        channelId       : 'WEB',
                        requestTimestamp: System.currentTimeMillis().toString(),
                        tokenType       : 'CHECKSUM',
                        token           : '?',
                ],
                body: [
                        mid          : m().id,
                        requestId    : UUID.randomUUID().toString(),
                        searchKey    : "IDN",
                        searchDetails: [
                                [
                                        merchantPrefix: '1111',
                                        searchId      : 'K735789163',
                                ]
                        ]
                ]
        ]
    }

    private final ChecksumTokenVanCreateTest checksumTokenVanCreateAPI = new ChecksumTokenVanCreateTest()

    @Override
    @Merchant(edit = false, value = { it.preferences.bankTransfer.enabled == true && it.acquirings.any { it.payMode == 'bank-transfer' && it.enabled == true } })
    @Test
    void "Validate the query van api response when all mandatory parameters are provided and search by VAN"() {
        String Van
        VAN_PROXY_CREATE:
        {
            def root = checksumTokenVanCreateAPI.root()
            Van = checksumTokenVanCreateAPI.req().body(root).post().path('body.vanDetails[0].van')
        }
        def root = root();
        root.body.mid = m().id;
        root.body.searchKey = "VAN";
        root.body.searchDetails[0].searchId = Van;
        req().body(root).post().then().
                body("body.resultInfo.resultStatus", equalTo("S")).
                body("body.resultInfo.resultMsg", equalTo("SUCCESS")).
                body("body.vanDetails[0].responseStatus", equalTo("SUCCESS"));

    }

    @Override
    @Merchant(edit = false, value = { it.preferences.bankTransfer.enabled == true && it.acquirings.any { it.payMode == 'bank-transfer' && it.enabled == true } })
    @Test
    void "Validate the query van api response when all mandatory parameters are provided and search by IDN"() {
        String identificationNo
        String merchantPrefix='1111'
        VAN_PROXY_CREATE:
        {
            def root = checksumTokenVanCreateAPI.root()
            identificationNo = checksumTokenVanCreateAPI.req().body(root).post().path('body.vanDetails[0].identificationNo')

        }
        def root = root();
        root.body.mid = m().id;
        root.body.searchKey = "IDN";
        root.body.searchDetails[0].merchantPrefix = merchantPrefix;
        root.body.searchDetails[0].searchId = identificationNo;
        req().body(root).post().then().
                body("body.resultInfo.resultStatus", equalTo("S")).
                body("body.resultInfo.resultMsg", equalTo("SUCCESS" )).
                body("body.vanDetails[0].responseStatus", equalTo("SUCCESS"));

    }

    @Override
    @Merchant(edit = false, value = { it.preferences.bankTransfer.enabled == true && it.acquirings.any { it.payMode == 'bank-transfer' && it.enabled == true } })
    @Test
    void "validate the response of query van api when token validation failed"() {
        def root = root();
        root.head.token = "bvwbubajnoi824ufewjk";
        root.body.mid = m().id;
        req().body(root).post().then().
                body("body.resultInfo.resultStatus", equalTo("F")).
                body("body.resultInfo.resultMsg", equalTo("CheckSum Validation Failure"));
    }

    @Override
    @Merchant(edit = false, value = { it.preferences.bankTransfer.enabled == false })
    @Test
    void "validate the response of query van api when mid is provided which is not having bank transfer as paymode"() {
        def root = root();
        root.body.mid = m().id;
        req().body(root).post().then().
                body("body.resultInfo.resultStatus", equalTo("F")).
                body("body.resultInfo.resultMsg", equalTo("Bank Transfer not supported"));
    }

    @Override
    @Merchant(edit = false, value = { it.preferences.bankTransfer.enabled == true && it.acquirings.any { it.payMode == 'bank-transfer' && it.enabled == true } })
    @Test
    void "Validate the response when searchkey is VAN and IDN is provided in searchid"() {
        String identificationNo
        String merchantPrefix='1111'
        VAN_PROXY_CREATE:
        {
            def root = checksumTokenVanCreateAPI.root()
            identificationNo = checksumTokenVanCreateAPI.req().body(root).post().path('body.vanDetails[0].identificationNo')

        }
        def root = root();
        root.body.mid = m().id;
        root.body.searchKey = "VAN";
        root.body.searchDetails[0].searchId = identificationNo;
        root.body.searchDetails[0].merchantPrefix = merchantPrefix;
        req().body(root).post().then().
                body("body.resultInfo.resultStatus", equalTo("S")).
                body("body.vanDetails[0].responseStatus", equalTo("FAILURE")).
                body("body.vanDetails[0].errorMessage", equalTo("invalid searchId"));
    }

    @Override
    @Merchant(edit = false, value = { it.preferences.bankTransfer.enabled == true && it.acquirings.any { it.payMode == 'bank-transfer' && it.enabled == true } })
    @Test
    void "Validate the response when searchKey is IDN and VAN is provided in searchid"() {
        String Van
        VAN_PROXY_CREATE:
        {
            def root = checksumTokenVanCreateAPI.root()
            Van = checksumTokenVanCreateAPI.req().body(root).post().path('body.vanDetails[0].van')
        }
        def root = root();
        root.body.mid = m().id;
        root.body.searchKey = "IDN";
        root.body.searchDetails[0].searchId = Van;
        req().body(root).post().then().
                body("body.resultInfo.resultStatus", equalTo("S")).
                body("body.vanDetails[0].responseStatus", equalTo("FAILURE")).
                body("body.vanDetails[0].errorMessage", equalTo("invalid searchId"));
    }

    @Override
    @Merchant(edit = false, value = { it.preferences.bankTransfer.enabled == true && it.acquirings.any { it.payMode == 'bank-transfer' && it.enabled == true } })
    @Test
    void "Validate the response when searchKey is IDN and merchant prefix is null"() {
        def root = root();
        root.body.mid = m().id;
        root.body.searchKey = "IDN";
        root.body.searchDetails[0].merchantPrefix = null
        req().body(root).post().then().
                body("body.resultInfo.resultStatus", equalTo("S")).
                body("body.vanDetails[0].responseStatus", equalTo("FAILURE")).
                body("body.vanDetails[0].errorMessage", equalTo("Invalid merchant prefix"));
    }

    @Override
    @Merchant(edit = false, value = { it.preferences.bankTransfer.enabled == true && it.acquirings.any { it.payMode == 'bank-transfer' && it.enabled == true } })
    @Test
    void "Validate the response when searchKey is IDN and merchant prefix is invalid"() {
        String identificationNo;
        VAN_PROXY_CREATE:
        {
            def root = checksumTokenVanCreateAPI.root()
            identificationNo = checksumTokenVanCreateAPI.req().body(root).post().path('body.vanDetails[0].identificationNo')
        }
        def root = root();
        root.body.mid = m().id;
        root.body.searchKey = "IDN";
        root.body.searchDetails[0].searchId = identificationNo;
        root.body.searchDetails[0].merchantPrefix=(0..4).collect { new Random().nextInt(10) }.join('');
        req().body(root).post().then().
                body("body.resultInfo.resultStatus", equalTo("S")).
                body("body.vanDetails[0].responseStatus", equalTo("FAILURE")).
                body("body.vanDetails[0].errorMessage", equalTo("Invalid merchant prefix"));
    }

    @Override
    @Merchant(edit = false, value = { it.preferences.bankTransfer.enabled == true && it.acquirings.any { it.payMode == 'bank-transfer' && it.enabled == true } })
    @Test
    void "Validate the response when searchKey is IDN and merchant prefix with invalid length"() {
        def root = root();
        root.body.mid = m().id;
        root.body.searchKey = "IDN";
        root.body.searchDetails[0].merchantPrefix = (0..2).collect { new Random().nextInt(10) }.join('');
        req().body(root).post().then().
                body("body.resultInfo.resultStatus", equalTo("S")).
                body("body.vanDetails[0].responseStatus", equalTo("FAILURE")).
                body("body.vanDetails[0].errorMessage", equalTo("Invalid merchant prefix"));
    }

    @Override
    @Merchant(edit = false, value = { it.preferences.bankTransfer.enabled == true && it.acquirings.any { it.payMode == 'bank-transfer' && it.enabled == true } })
    @Test
    void "Validate the response when searchKey is IDN and invalid IDN is provided"() {
        String merchantPrefix;
        VAN_PROXY_CREATE:
        {
            def root = checksumTokenVanCreateAPI.root()
            merchantPrefix = checksumTokenVanCreateAPI.req().body(root).post().path('body.vanDetails[0].merchantPrefix')

        }
        def root = root();
        root.body.mid = m().id;
        root.body.searchKey = "IDN";
        root.body.searchDetails[0].merchantPrefix = merchantPrefix;
        root.body.searchDetails[0].searchId = (0..9).collect { new Random().nextInt(10) }.join('');
        req().body(root).post().then().
                body("body.resultInfo.resultStatus", equalTo("S")).
                body("body.vanDetails[0].responseStatus", equalTo("FAILURE")).
                body("body.vanDetails[0].errorMessage", equalTo("Van not Found"));
    }

    @Override
    @Merchant(edit = false, value = { it.preferences.bankTransfer.enabled == true && it.acquirings.any { it.payMode == 'bank-transfer' && it.enabled == true } })
    @Test
    void "Validate the response when searchKey is IDN and IDN provided with invalid length"() {
        def root = root();
        root.body.mid = m().id;
        root.body.searchKey = "IDN";
        root.body.searchDetails[0].searchId = (0..8).collect { new Random().nextInt(10) }.join('');
        req().body(root).post().then().
                body("body.resultInfo.resultStatus", equalTo("S")).
                body("body.vanDetails[0].responseStatus", equalTo("FAILURE")).
                body("body.vanDetails[0].errorMessage", equalTo("invalid searchId"));
    }

    @Override
    @Merchant(edit = false, value = { it.preferences.bankTransfer.enabled == true && it.acquirings.any { it.payMode == 'bank-transfer' && it.enabled == true } })
    @Test
    void "Validate the response when searchKey is VAN and invalid van id provided"() {
        def root = root();
        root.body.mid = m().id;
        root.body.searchKey = "VAN";
        root.body.searchDetails[0].searchId = (0..15).collect { new Random().nextInt(10) }.join('');
        req().body(root).post().then().
                body("body.resultInfo.resultStatus", equalTo("S")).
                body("body.vanDetails[0].responseStatus", equalTo("FAILURE")).
                body("body.vanDetails[0].errorMessage", equalTo("Van not Found"));
    }

    @Override
    @Merchant(edit = false, value = { it.preferences.bankTransfer.enabled == true && it.acquirings.any { it.payMode == 'bank-transfer' && it.enabled == true } })
    @Test
    void "Validate the response when searchKey is VAN and van id provided with invalid length"() {
        def root = root();
        root.body.mid = m().id;
        root.body.searchKey = "VAN";
        root.body.searchDetails[0].searchId = (0..8).collect { new Random().nextInt(10) }.join('');
        req().body(root).post().then().
                body("body.resultInfo.resultStatus", equalTo("S")).
                body("body.vanDetails[0].responseStatus", equalTo("FAILURE")).
                body("body.vanDetails[0].errorMessage", equalTo("invalid searchId"));

    }

    @Override
    @Merchant(edit = false, value = { it.preferences.bankTransfer.enabled == true && it.acquirings.any { it.payMode == 'bank-transfer' && it.enabled == true } })
    @Test
    void "Validate the response of van query api when 2 Vans has been provided in Search details 1 is correct and another is incorrect"() {
        String Van
        VAN_PROXY_CREATE:
        {
            def root = checksumTokenVanCreateAPI.root()
            Van = checksumTokenVanCreateAPI.req().body(root).post().path('body.vanDetails[0].van')
        }
        def root = root()
        root.body.mid = m().id
        root.body.searchKey = "VAN"
        root.body.searchDetails = (0..1).collect { root.body.searchDetails[0].clone() }
        root.body.searchDetails[0].searchId = Van
        root.body.searchDetails[1].searchId = (0..15).collect { new Random().nextInt(10) }.join('')
        req().body(root).post().then().
                body("body.resultInfo.resultStatus", equalTo("S")).
                body("body.vanDetails[0].responseStatus", equalTo("SUCCESS")).
                body("body.vanDetails[1].responseStatus", equalTo("FAILURE")).
                body("body.vanDetails[1].errorMessage", equalTo("Van not Found"));


    }

    @Override
    @Merchant(edit = false, value = { it.preferences.bankTransfer.enabled == true && it.acquirings.any { it.payMode == 'bank-transfer' && it.enabled == true } })
    @Test
    void "Validate the response when try to query more then 10 van info"() {
        def root = root();
        root.body.mid = m().id;
        root.body.searchKey = "VAN";
        root.body.searchDetails = (0..10).collect { root.body.searchDetails[0].clone() }
        root.body.searchDetails[0].searchId = (0..15).collect { new Random().nextInt(10) }.join('')
        root.body.searchDetails[1].searchId = (0..15).collect { new Random().nextInt(10) }.join('')
        root.body.searchDetails[2].searchId = (0..15).collect { new Random().nextInt(10) }.join('')
        root.body.searchDetails[3].searchId = (0..15).collect { new Random().nextInt(10) }.join('')
        root.body.searchDetails[4].searchId = (0..15).collect { new Random().nextInt(10) }.join('')
        root.body.searchDetails[5].searchId = (0..15).collect { new Random().nextInt(10) }.join('')
        root.body.searchDetails[6].searchId = (0..15).collect { new Random().nextInt(10) }.join('')
        root.body.searchDetails[7].searchId = (0..15).collect { new Random().nextInt(10) }.join('')
        root.body.searchDetails[8].searchId = (0..15).collect { new Random().nextInt(10) }.join('')
        root.body.searchDetails[9].searchId = (0..15).collect { new Random().nextInt(10) }.join('')
        root.body.searchDetails[10].searchId = (0..15).collect { new Random().nextInt(10) }.join('')

        req().body(root).post().then().
                body("body.resultInfo.resultStatus", equalTo("F")).
                body("body.resultInfo.resultCode", equalTo("0001"));


    }

    @Override
    @Merchant(edit = false, value = { it.preferences.bankTransfer.enabled == true && it.acquirings.any { it.payMode == 'bank-transfer' && it.enabled == true } })
    @Test
    void "Validate the response of van query api when requestid length is more then 64 digit"() {
        def root = root();
        root.body.mid = m().id;
        root.body.requestId = (0..64).collect { new Random().nextInt(10) }.join('');
        root.body.searchKey = "VAN";
        root.body.searchDetails[0].searchId = (0..16).collect { new Random().nextInt(10) }.join('');
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
}
