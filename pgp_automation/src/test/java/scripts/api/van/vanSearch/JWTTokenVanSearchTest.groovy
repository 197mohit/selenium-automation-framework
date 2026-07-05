package scripts.api.van.vanSearch

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.paytm.base.test.TestSetUp
import com.paytm.utils.merchant.merchant.util.annotations.Merchant
import groovy.json.JsonSlurper
import io.qameta.allure.Feature
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
import static com.paytm.appconstants.Constants.VANProxy.VAN_PROXY_SEARCH
import static io.restassured.RestAssured.given
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath

@Owner('Karmvir')
@Link(url = 'https://wiki.mypaytm.com/pages/viewpage.action?pageId=219098055')
@Feature("PGP-27691")
class JWTTokenVanSearchTest  extends TestSetUp implements VanSearchTest {


    private final Filter setTokenFilter = new Filter() {
        @Override
        Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
            def root = new JsonSlurper().parseText(requestSpec.getBody())
            root?.head?.with {
                if (it?.token == '?') it.token = JWT.create()
                        .withIssuer('c11')
                        .withClaim('mid', root.body.mid)
                        .withClaim('requestId', root.body.requestId)
                        .sign(Algorithm.HMAC256("123456"))
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
                            .expectBody(matchesJsonSchemaInClasspath('json-schemas/van-proxy/van-search-schema.json'))
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
                .setBasePath(VAN_PROXY_SEARCH)
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
                        tokenType       : 'JWT',
                        token           : '?',
                ],
                body: [
                        mid          : m().id,
                        requestId    : UUID.randomUUID().toString(),
                        searchParams : [
                                mid  : m().id,
                                active : true,
                                startDate: null,
                                endDate:  null

                        ],
                        pageInfo:[
                                limit: 1,
                                offset: 0
                        ]
                ]
        ]
    }

    private static final long DAY_IN_MIILLIS = 1 * 24 * 60 * 60 * 1000
    @Override
    @Merchant(edit = false, value = { it.preferences.bankTransfer.enabled == true && it.acquirings.any { it.payMode == 'bank-transfer' && it.enabled == true } })
    @Test
    void "Validate the search van api response when all mandatory paramters are provided and search by MID"() {
        def root = root();
        root.body.searchParams.mid=m().id
        req().body(root).post().then().
                body("body.resultInfo.resultStatus", Matchers.equalTo("S")).
                body("body.resultInfo.resultCode", Matchers.equalTo("0000")).
                body("body.resultInfo.resultMsg", Matchers.equalTo("SUCCESS")).
                body("body.vanDetails", Matchers.notNullValue());

    }

    @Override
    @Merchant(edit = false, value = { it.preferences.bankTransfer.enabled == true && it.acquirings.any { it.payMode == 'bank-transfer' && it.enabled == true } })
    @Test
    void "Validate the search van api response when token validation failed"() {
        def root = root();
        root.head.token = UUID.randomUUID().toString()
        req().body(root).post().then().
                body("body.resultInfo.resultStatus", Matchers.equalTo("F")).
                body("body.resultInfo.resultCode", Matchers.equalTo("2003")).
                body("body.resultInfo.resultMsg", Matchers.equalTo("Jwt Validation Failure")).
                body("body.vanDetails", Matchers.nullValue())
    }

    @Override
    @Merchant(edit = false, value = { it.preferences.bankTransfer.enabled == false })
    @Test
    void "Validate the search api response when MID is used which is not having bank transfer supported"() {
        def root = root();
        req().body(root).post().then().
                body("body.resultInfo.resultStatus", Matchers.equalTo("F")).
                body("body.resultInfo.resultCode", Matchers.equalTo("2001")).
                body("body.resultInfo.resultMsg", Matchers.equalTo("Bank Transfer not supported")).
                body("body.vanDetails", Matchers.nullValue())
    }

    @Override
    @Merchant(edit = false, value = { it.preferences.bankTransfer.enabled == true && it.acquirings.any { it.payMode == 'bank-transfer' && it.enabled == true } })
    @Test
    void "Validate the search api response when requestid is passed which is having more then 64 digit"() {
        def root = root();
        root.body.requestId=(0..64).collect { new Random().nextInt(10) }.join('');
        req().body(root).post().then().
                body("body.resultInfo.resultStatus", Matchers.equalTo("F")).
                body("body.resultInfo.resultCode", Matchers.equalTo("0001")).
                body("body.vanDetails", Matchers.nullValue())

    }

    @Override
    @Merchant(edit = false, value = { it.preferences.bankTransfer.enabled == true && it.acquirings.any { it.payMode == 'bank-transfer' && it.enabled == true } })
    @Test
    void "Validate the search van api response when active status true"() {
        def root = root();
        root.body.searchParams.active=true
        req().body(root).post().then().
                body("body.resultInfo.resultStatus", Matchers.equalTo("S")).
                body("body.resultInfo.resultCode", Matchers.equalTo("0000")).
                body("body.resultInfo.resultMsg", Matchers.equalTo("SUCCESS")).
                body("body.vanDetails[0].active", Matchers.equalTo(true)).
                body("body.vanDetails", Matchers.notNullValue());
    }

    @Override
    @Merchant(edit = false, value = { it.preferences.bankTransfer.enabled == true && it.acquirings.any { it.payMode == 'bank-transfer' && it.enabled == true } })
    @Test
    void "Validate the search van api response when active status false"() {
        def root = root();
        root.body.searchParams.active=false
        req().body(root).post().then().
                body("body.resultInfo.resultStatus", Matchers.equalTo("S")).
                body("body.resultInfo.resultCode", Matchers.equalTo("0000")).
                body("body.resultInfo.resultMsg", Matchers.equalTo("SUCCESS")).
                body("body.vanDetails[0].active", Matchers.equalTo(false)).
                body("body.vanDetails", Matchers.notNullValue());
    }

    @Override
    @Merchant(edit = false, value = { it.preferences.bankTransfer.enabled == true && it.acquirings.any { it.payMode == 'bank-transfer' && it.enabled == true } })
    @Test
    void "Validate the search van api response when date range is provided in search param"() {

        def root = root();
        root.body.searchParams.startDate= (System.currentTimeMillis()-DAY_IN_MIILLIS).toString()
        root.body.searchParams.endDate=System.currentTimeMillis().toString();
        req().body(root).post().then().
                body("body.resultInfo.resultStatus", Matchers.equalTo("S")).
                body("body.resultInfo.resultCode", Matchers.equalTo("0000")).
                body("body.resultInfo.resultMsg", Matchers.equalTo("SUCCESS")).
                body("body.vanDetails", Matchers.notNullValue());
    }

    @Override
    @Merchant(edit = false, value = { it.preferences.bankTransfer.enabled == true && it.acquirings.any { it.payMode == 'bank-transfer' && it.enabled == true } })
    @Test
    void "Validate the search van api response when date range is provided in search param and active status false"() {
        def root = root();

        root.body.mid = m().id;
        root.body.searchParams.active=false
        root.body.searchParams.startDate= (System.currentTimeMillis()-DAY_IN_MIILLIS).toString()
        root.body.searchParams.endDate=System.currentTimeMillis().toString();
        req().body(root).post().then().
                body("body.resultInfo.resultStatus", Matchers.equalTo("S")).
                body("body.resultInfo.resultCode", Matchers.equalTo("0000")).
                body("body.resultInfo.resultMsg", Matchers.equalTo("SUCCESS")).
                body("body.vanDetails", Matchers.notNullValue());
    }

    @Override
    @Merchant(edit = false, value = { it.preferences.bankTransfer.enabled == true && it.acquirings.any { it.payMode == 'bank-transfer' && it.enabled == true } })
    @Test
    void "Validate the search api response when start date is greater then end date "() {
        def root = root();
        root.body.searchParams.startDate=System.currentTimeMillis().toString();
        root.body.searchParams.endDate=(System.currentTimeMillis()-DAY_IN_MIILLIS).toString()
        req().body(root).post().then().
                body("body.resultInfo.resultStatus", Matchers.equalTo("F")).
                body("body.resultInfo.resultCode", Matchers.equalTo("3016")).
                body("body.resultInfo.resultMsg", Matchers.equalTo("start date should be less than end date")).
                body("body.vanDetails", Matchers.emptyIterable());

    }

    @Override
    @Merchant(edit = false, value = { it.preferences.bankTransfer.enabled == true && it.acquirings.any { it.payMode == 'bank-transfer' && it.enabled == true } })
    @Test
    void "Validate the search api response when start date is provided and end date is not provided"() {
        def root = root();
        root.body.searchParams.startDate=(System.currentTimeMillis()-DAY_IN_MIILLIS).toString()
        root.body.searchParams.endDate=null
        req().body(root).post().then().
                body("body.resultInfo.resultStatus", Matchers.equalTo("S")).
                body("body.resultInfo.resultCode", Matchers.equalTo("0000")).
                body("body.resultInfo.resultMsg", Matchers.equalTo("SUCCESS")).
                body("body.vanDetails", Matchers.notNullValue());

    }

    @Override
    @Merchant(edit = false, value = { it.preferences.bankTransfer.enabled == true && it.acquirings.any { it.payMode == 'bank-transfer' && it.enabled == true } })
    @Test
    void "Validate the search api response when searchParams is null"() {
        def root = root();
        root.body.searchParams=null
        req().body(root).post().then().
                body("body.resultInfo.resultStatus", Matchers.equalTo("F")).
                body("body.resultInfo.resultCode", Matchers.equalTo("0001")).
                body("body.resultInfo.resultMsg", Matchers.equalTo("body.searchParams : searchParams should not be empty : rejected value [null]")).
                body("body.vanDetails", Matchers.nullValue())

    }

    @Override
    @Merchant(edit = false, value = { it.preferences.bankTransfer.enabled == true && it.acquirings.any { it.payMode == 'bank-transfer' && it.enabled == true } })
    @Test
    void "Validate the search api response when pageInfo is null"() {
        def root = root();
        root.body.pageInfo=null
        req().body(root).post().then().
                body("body.resultInfo.resultStatus", Matchers.equalTo("F")).
                body("body.resultInfo.resultCode", Matchers.equalTo("0001")).
                body("body.resultInfo.resultMsg", Matchers.equalTo("body.pageInfo : pageInfo should not be empty : rejected value [null]")).
                body("body.vanDetails", Matchers.nullValue())

    }

    @Override
    @Merchant(edit = false, value = { it.preferences.bankTransfer.enabled == true && it.acquirings.any { it.payMode == 'bank-transfer' && it.enabled == true } })
    @Test
    void "Validate the response of van search api when limit is 50"() {
        def root = root();
        root.body.pageInfo.limit=50;
        req().body(root).post().then().
                body("body.resultInfo.resultStatus", Matchers.equalTo("S")).
                body("body.resultInfo.resultCode", Matchers.equalTo("0000")).
                body("body.resultInfo.resultMsg", Matchers.equalTo("SUCCESS")).
                body("body.vanDetails", Matchers.notNullValue());
    }

    @Override
    @Merchant(edit = false, value = { it.preferences.bankTransfer.enabled == true && it.acquirings.any { it.payMode == 'bank-transfer' && it.enabled == true } })
    @Test
    void "Validate the response of van search api when limit is 101"() {
        def root = root();
        root.body.mid=m().id;
        root.body.searchParams.startDate=null
        root.body.searchParams.endDate=null
        root.body.pageInfo.limit=101;
        req().body(root).post().then().
                body("body.resultInfo.resultStatus", Matchers.equalTo("F")).
                body("body.resultInfo.resultCode", Matchers.equalTo("3008")).
                body("body.resultInfo.resultMsg", Matchers.equalTo("Invalid limit [1,100]")).
                body("body.vanDetails", Matchers.emptyIterable());
    }

    @Override
    @Merchant(edit = false, value = { it.preferences.bankTransfer.enabled == true && it.acquirings.any { it.payMode == 'bank-transfer' && it.enabled == true } })
    @Test
    void "Validate the response of van search when page limit is in decimal"() {
        def root = root();
        root.body.pageInfo.limit=8.5;
        req().body(root).post().then().
                body("body.resultInfo.resultStatus", Matchers.equalTo("S")).
                body("body.resultInfo.resultCode", Matchers.equalTo("0000")).
                body("body.resultInfo.resultMsg", Matchers.equalTo("SUCCESS")).
                body("body.vanDetails", Matchers.hasSize(8)).
                body("body.vanDetails", Matchers.notNullValue());
    }

    @Override
    @Merchant(edit = false, value = { it.preferences.bankTransfer.enabled == true && it.acquirings.any { it.payMode == 'bank-transfer' && it.enabled == true } })
    @Test
    void "Validate the response of van search when page limit is in negative"() {
        def root = root();
        root.body.pageInfo.limit=-8;
        req().body(root).post().then().
                body("body.resultInfo.resultStatus", Matchers.equalTo("F")).
                body("body.resultInfo.resultCode", Matchers.equalTo("3008")).
                body("body.resultInfo.resultMsg", Matchers.equalTo("Invalid limit [1,100]")).
                body("body.vanDetails", Matchers.emptyIterable());
    }

    @Override
    @Merchant(edit = false, value = { it.preferences.bankTransfer.enabled == true && it.acquirings.any { it.payMode == 'bank-transfer' && it.enabled == true } })
    @Test
    void "Validate the response of van search api when page limit is 100"() {
        def root = root();
        root.body.pageInfo.limit=100;
        req().body(root).post().then().
                body("body.resultInfo.resultStatus", Matchers.equalTo("S")).
                body("body.resultInfo.resultCode", Matchers.equalTo("0000")).
                body("body.resultInfo.resultMsg", Matchers.equalTo("SUCCESS")).
                body("body.vanDetails", Matchers.notNullValue());

    }

    @Override
    @Merchant(edit = false, value = { it.preferences.bankTransfer.enabled == true && it.acquirings.any { it.payMode == 'bank-transfer' && it.enabled == true } })
    @Test
    void "Validate the response of van search api when page limit is 1"() {
        def root = root();
        root.body.pageInfo.limit=1;
        req().body(root).post().then().
                body("body.resultInfo.resultStatus", Matchers.equalTo("S")).
                body("body.resultInfo.resultCode", Matchers.equalTo("0000")).
                body("body.resultInfo.resultMsg", Matchers.equalTo("SUCCESS")).
                body("body.vanDetails", Matchers.hasSize(1)).
                body("body.vanDetails", Matchers.notNullValue());

    }

    @Override
    @Merchant(edit = false, value = { it.preferences.bankTransfer.enabled == true && it.acquirings.any { it.payMode == 'bank-transfer' && it.enabled == true } })
    @Test
    void "Validate the response when page limit is 0"() {
        def root = root();
        root.body.pageInfo.limit=-0;
        req().body(root).post().then().
                body("body.resultInfo.resultStatus", Matchers.equalTo("F")).
                body("body.resultInfo.resultCode", Matchers.equalTo("3008")).
                body("body.resultInfo.resultMsg", Matchers.equalTo("Invalid limit [1,100]")).
                body("body.vanDetails", Matchers.emptyIterable());
    }
}
