package scripts.api.van.updateVan

import com.paytm.base.test.TestSetUp
import com.paytm.utils.merchant.merchant.util.annotations.Merchant
import com.paytm.utils.merchant.util.PGPUtil
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import io.qameta.allure.Feature
import io.qameta.allure.Link
import io.qameta.allure.Owner
import io.restassured.builder.RequestSpecBuilder
import io.restassured.builder.ResponseSpecBuilder
import io.restassured.filter.Filter
import io.restassured.filter.FilterContext
import io.restassured.http.ContentType
import io.restassured.path.json.JsonPath
import io.restassured.response.Response
import io.restassured.specification.FilterableRequestSpecification
import io.restassured.specification.FilterableResponseSpecification
import io.restassured.specification.RequestSpecification
import org.hamcrest.Matchers
import org.testng.annotations.Test
import scripts.api.van.createVan.ChecksumTokenVanCreateTest

import static com.paytm.LocalConfig.PGP_HOST
import static com.paytm.appconstants.Constants.VANProxy.VAN_PROXY_UPDATE
import static io.restassured.RestAssured.given
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath
import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.startsWith

@Owner('Karmvir')
@Link(url = 'https://wiki.mypaytm.com/pages/viewpage.action?pageId=215356761')
@Feature("PGP-28150")

class ChecksumTokenVanUpdateTest extends TestSetUp implements VanUpdateTest{
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
                            .expectBody(matchesJsonSchemaInClasspath('json-schemas/van-proxy/van-update-schema.json'))
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
                .setBasePath(VAN_PROXY_UPDATE)
    }

    private final RequestSpecification req() {
        given(reqBldr().build())
    }


    private final Map root() {
        [
                head: [
                        version         : 'v1',
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
                        vanInfo: [
                                [
                                        van     : 'PY11113484082225',
                                        active  : true,
                                ]
                        ]
                ]
        ]
    }
    private final ChecksumTokenVanCreateTest checksumTokenVanCreateAPI = new ChecksumTokenVanCreateTest()
    @Override
    @Merchant(edit = false, value = { it.preferences.bankTransfer.enabled == true && it.acquirings.any { it.payMode == 'bank-transfer' && it.enabled == true } })
    @Test
    void "Validate the response of Van update api for disable a VAN"() {
        String Van
        VAN_PROXY_CREATE:
        {
            def root = checksumTokenVanCreateAPI.root()
            Van = checksumTokenVanCreateAPI.req().body(root).post().path('body.vanDetails[0].van')
        }
        def root = root()
        root.body.vanInfo[0].van = Van
        root.body.vanInfo[0].active = false
        req().body(root).post().then().
                body("body.resultInfo.resultStatus", Matchers.equalTo("S")).
                body("body.resultInfo.resultCode", Matchers.equalTo("0000")).
                body("body.resultInfo.resultMsg", Matchers.equalTo("SUCCESS")).
                body("body.vanInfo[0].vanStatus", Matchers.equalTo("INACTIVE")).
                body("body.vanInfo[0].responseStatus", Matchers.equalTo("SUCCESS"));
    }

    @Override
    @Merchant(edit = false, value = { it.preferences.bankTransfer.enabled == true && it.acquirings.any { it.payMode == 'bank-transfer' && it.enabled == true } })
    @Test
    void "Validate the response of van update api for active a van which is already active"() {
        String Van
        VAN_PROXY_CREATE:
        {
            def root = checksumTokenVanCreateAPI.root()
            Van = checksumTokenVanCreateAPI.req().body(root).post().path('body.vanDetails[0].van')
        }
        def root = root()
        root.body.vanInfo[0].van = Van
        root.body.vanInfo[0].active = true
        req().body(root).post().then().
                body("body.resultInfo.resultStatus", Matchers.equalTo("S")).
                body("body.resultInfo.resultCode", Matchers.equalTo("0000")).
                body("body.resultInfo.resultMsg", Matchers.equalTo("SUCCESS")).
                body("body.vanInfo[0].errorMessage", Matchers.equalTo("Van status is already the same as requested")).
                body("body.vanInfo[0].responseStatus", Matchers.equalTo("FAILURE"));
    }

    @Override
    @Merchant(edit = false, value = { it.preferences.bankTransfer.enabled == true && it.acquirings.any { it.payMode == 'bank-transfer' && it.enabled == true } })
    @Test
    void "Validate the response of van update api when 4 vans are provided from which 1 is correct and 3 are incorrect"() {
        String Van1
        String Van2
        VAN_PROXY_CREATE:
        {
            def root = checksumTokenVanCreateAPI.root()
            root.body.vanDetails = (0..1).collect { root.body.vanDetails[0].clone() }
            root.body.vanDetails[0].identificationNo = (0..9).collect { new Random().nextInt(10) }.join('');
            root.body.vanDetails[1].identificationNo = (0..9).collect { new Random().nextInt(10) }.join('');
            JsonPath Vancreate = checksumTokenVanCreateAPI.req().body(root).post().jsonPath()
            Van1=Vancreate.getString('body.vanDetails[0].van')
            Van2=Vancreate.getString('body.vanDetails[1].van')
        }
        def root=root()
        root.body.vanInfo = (0..3).collect { root.body.vanInfo[0].clone() }
        root.body.vanInfo[0].van=Van1
        root.body.vanInfo[0].active=false
        root.body.vanInfo[1].van=Van2
        root.body.vanInfo[1].active=true
        root.body.vanInfo[2].van= (0..15).collect { new Random().nextInt(10) }.join('')
        root.body.vanInfo[3].van=(0..10).collect { new Random().nextInt(10) }.join('')
        req().body(root).post().then().
                body("body.resultInfo.resultStatus", Matchers.equalTo("S")).
                body("body.resultInfo.resultCode", Matchers.equalTo("0000")).
                body("body.resultInfo.resultMsg", Matchers.equalTo("SUCCESS")).
                body("body.vanInfo[0].responseStatus", Matchers.equalTo("SUCCESS")).
                body("body.vanInfo[0].vanStatus", Matchers.equalTo("INACTIVE")).
                body("body.vanInfo[1].errorMessage", Matchers.equalTo("Van status is already the same as requested")).
                body("body.vanInfo[2].errorMessage", Matchers.equalTo("Van not Found")).
                body("body.vanInfo[3].errorMessage", Matchers.equalTo("Invalid Van format"))
    }

    @Override
    @Merchant(edit = false, value = { it.preferences.bankTransfer.enabled == false })
    @Test
    void "Validate the response when merchant is not bank transfer pref enabled"() {
        def root = root()
        root.body.mid = m().id
        req().body(root).post().then().
                body("body.resultInfo.resultStatus", Matchers.equalTo("F")).
                body("body.resultInfo.resultCode", Matchers.equalTo("2001")).
                body("body.resultInfo.resultMsg", Matchers.equalTo("Bank Transfer not supported"))

    }

    @Override
    @Merchant(edit = false, value = { it.preferences.bankTransfer.enabled == true && it.acquirings.any { it.payMode == 'bank-transfer' && it.enabled == true } })
    @Test
    void "Validate the response of update van api for token validation failed"() {
        def root = root()
        root.head.token = UUID.randomUUID().toString()
        req().body(root).post().then().
                body("body.resultInfo.resultStatus", Matchers.equalTo("F")).
                body("body.resultInfo.resultCode", Matchers.equalTo("2002")).
                body("body.resultInfo.resultMsg", Matchers.equalTo("CheckSum Validation Failure"))

    }

    @Override
    @Merchant(edit = false, value = { it.preferences.bankTransfer.enabled == true && it.acquirings.any { it.payMode == 'bank-transfer' && it.enabled == true } })
    @Test
    void "Validate the response of update van api when request id is having more the 64 digit"() {
        def root = root();
        root.body.requestId = (0..64).collect { new Random().nextInt(10) }.join('');
        req().body(root).post().then().
                body("body.resultInfo.resultStatus", equalTo("F")).
                body("body.resultInfo.resultCode", equalTo("0001")).
                body("body.resultInfo.resultMsg", startsWith("body.requestId : requestId length should be [1,64]"));
    }

    @Override
    @Merchant(edit = false, value = { it.preferences.bankTransfer.enabled == true && it.acquirings.any { it.payMode == 'bank-transfer' && it.enabled == true } })
    @Test
    void "Validate the response of update van api when vanInfo is null"() {
        def root = root();
        root.body.vanInfo=null
        req().body(root).post().then().
                body("body.resultInfo.resultStatus", Matchers.equalTo("F")).
                body("body.resultInfo.resultCode", Matchers.equalTo("0001")).
                body("body.resultInfo.resultMsg", Matchers.equalTo("body.vanInfo : vanInfo should not be empty : rejected value [null]"))

    }

    @Override
    @Merchant(edit = false, value = { it.preferences.bankTransfer.enabled == true && it.acquirings.any { it.payMode == 'bank-transfer' && it.enabled == true } })
    @Test
    void "Validate the response when invalid VAN is provided in request"() {
        def root = root();
        root.body.vanInfo[0].van=(0..15).collect { new Random().nextInt(10) }.join('');
        req().body(root).post().then().
                body("body.resultInfo.resultStatus", Matchers.equalTo("S")).
                body("body.resultInfo.resultCode", Matchers.equalTo("0000")).
                body("body.resultInfo.resultMsg", Matchers.equalTo("SUCCESS")).
                body("body.vanInfo[0].errorMessage", Matchers.equalTo("Van not Found")).
                body("body.vanInfo[0].responseStatus", Matchers.equalTo("FAILURE"));

    }

    @Override
    @Merchant(edit = false, value = { it.preferences.bankTransfer.enabled == true && it.acquirings.any { it.payMode == 'bank-transfer' && it.enabled == true } })
    @Test
    void "Validate the response of van update api when active is null"() {
        String Van
        VAN_PROXY_CREATE:
        {
            def root = checksumTokenVanCreateAPI.root()
            Van = checksumTokenVanCreateAPI.req().body(root).post().path('body.vanDetails[0].van')
        }
        def root = root();
        root.body.vanInfo[0].van=Van
        root.body.vanInfo[0].active=null
        req().body(root).post().then().
                body("body.resultInfo.resultStatus", Matchers.equalTo("S")).
                body("body.resultInfo.resultCode", Matchers.equalTo("0000")).
                body("body.resultInfo.resultMsg", Matchers.equalTo("SUCCESS")).
                body("body.vanInfo[0].errorMessage", Matchers.equalTo("active field can not be empty")).
                body("body.vanInfo[0].responseStatus", Matchers.equalTo("FAILURE"));
    }

    @Override
    @Merchant(edit = false, value = { it.preferences.bankTransfer.enabled == true && it.acquirings.any { it.payMode == 'bank-transfer' && it.enabled == true } })
    @Test
    void "Validate the response of update van api when van is null"() {
        def root = root();
        root.body.vanInfo[0].van=null
        req().body(root).post().then().
                body("body.resultInfo.resultStatus", Matchers.equalTo("S")).
                body("body.resultInfo.resultCode", Matchers.equalTo("0000")).
                body("body.resultInfo.resultMsg", Matchers.equalTo("SUCCESS")).
                body("body.vanInfo[0].errorMessage", Matchers.equalTo("Invalid Van format")).
                body("body.vanInfo[0].responseStatus", Matchers.equalTo("FAILURE"));

    }

    @Override
    @Merchant(edit = false, value = { it.preferences.bankTransfer.enabled == true && it.acquirings.any { it.payMode == 'bank-transfer' && it.enabled == true } })
    @Test
    void "validate the response of update van api when more then 10 van details are provided"() {
        def root = root();
        root.body.vanInfo = (0..10).collect { root.body.vanInfo[0].clone() }
        root.body.vanInfo[0].van = (0..15).collect { new Random().nextInt(10) }.join('');
        root.body.vanInfo[1].van = (0..15).collect { new Random().nextInt(10) }.join('');
        root.body.vanInfo[2].van = (0..15).collect { new Random().nextInt(10) }.join('');
        root.body.vanInfo[3].van = (0..15).collect { new Random().nextInt(10) }.join('');
        root.body.vanInfo[4].van = (0..15).collect { new Random().nextInt(10) }.join('');
        root.body.vanInfo[5].van = (0..15).collect { new Random().nextInt(10) }.join('');
        root.body.vanInfo[6].van = (0..15).collect { new Random().nextInt(10) }.join('');
        root.body.vanInfo[7].van = (0..15).collect { new Random().nextInt(10) }.join('');
        root.body.vanInfo[8].van = (0..15).collect { new Random().nextInt(10) }.join('');
        root.body.vanInfo[9].van = (0..15).collect { new Random().nextInt(10) }.join('');
        root.body.vanInfo[10].van = (0..15).collect { new Random().nextInt(10) }.join('');
        req().body(root).post().then().
                body("body.resultInfo.resultStatus", Matchers.equalTo("F")).
                body("body.resultInfo.resultCode", Matchers.equalTo("0001")).
        body("body.resultInfo.resultMsg", startsWith("body.vanInfo : allowed size - min=1, max=10 : rejected value"));

    }

    @Override
    @Merchant(edit = false, value = { it.preferences.bankTransfer.enabled == true && it.acquirings.any { it.payMode == 'bank-transfer' && it.enabled == true } })
    @Test
    void "Validate the response of update van api when activate a deactivated van"() {
        String Van
        VAN_PROXY_CREATE:
        {
            def root = checksumTokenVanCreateAPI.root()
            Van = checksumTokenVanCreateAPI.req().body(root).post().path('body.vanDetails[0].van')
        }
        JWTTokenVanUpdateTest JWTTokenVanUpdateApi= new JWTTokenVanUpdateTest();
        def root = JWTTokenVanUpdateApi.root()
        root.body.vanInfo[0].van = Van
        root.body.vanInfo[0].active = false
        JWTTokenVanUpdateApi.req().body(root).post()
        def root1 = JWTTokenVanUpdateApi.root()
        root1.body.vanInfo[0].van = Van
        root1.body.vanInfo[0].active = true
        JWTTokenVanUpdateApi.req().body(root1).post().then().
                body("body.resultInfo.resultStatus", Matchers.equalTo("S")).
                body("body.resultInfo.resultCode", Matchers.equalTo("0000")).
                body("body.resultInfo.resultMsg", Matchers.equalTo("SUCCESS")).
                body("body.vanInfo[0].vanStatus", Matchers.equalTo("ACTIVE")).
                body("body.vanInfo[0].responseStatus", Matchers.equalTo("SUCCESS"));

    }

    @Override
    @Merchant(edit = false, value = { it.preferences.bankTransfer.enabled == true && it.acquirings.any { it.payMode == 'bank-transfer' && it.enabled == true } })
    @Test
    void "Validate the response when mid is blank"() {
        def root= root()
        root.body.mid=""
        root.body.vanInfo[0].van= (0..15).collect { new Random().nextInt(10) }.join('')
        req().body(root).post().then().
                body("body.resultInfo.resultStatus", Matchers.equalTo("F")).
                body("body.resultInfo.resultCode", Matchers.equalTo("2004")).
                body("body.resultInfo.resultMsg", Matchers.equalTo("Incorrect mid"))
    }
}
