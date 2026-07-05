package scripts.api.theia

import com.paytm.appconstants.Constants
import com.paytm.apphelpers.PGPHelpers
import com.paytm.apphelpers.WalletHelpers
import com.paytm.base.test.TestSetUp
import com.paytm.base.test.User
import com.paytm.utils.merchant.merchant.util.annotations.Merchant
import com.paytm.utils.merchant.user.annotations.AUser
import io.restassured.builder.RequestSpecBuilder
import io.restassured.builder.ResponseSpecBuilder
import io.restassured.filter.Filter
import io.restassured.filter.FilterContext
import io.restassured.http.ContentType
import io.restassured.response.Response
import io.restassured.specification.FilterableRequestSpecification
import io.restassured.specification.FilterableResponseSpecification
import org.testng.annotations.Test

import static com.paytm.LocalConfig.PGP_HOST
import static com.paytm.appconstants.Constants.PGPAPIResourcePath.FAST_FWD
import static io.restassured.RestAssured.given
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath
import static org.hamcrest.Matchers.*

class FastForwardTest extends TestSetUp {

    private final Filter schemaFilter = new Filter() {
        @Override
        Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
            responseSpec.spec(
                    new ResponseSpecBuilder()
                            .expectBody(matchesJsonSchemaInClasspath('json-schemas/clw-app-pay-app-schema.json'))
                            .build()
            )
            return ctx.next(requestSpec, responseSpec)
        }
    }

    final def reqBldr = {
        new RequestSpecBuilder()
                .addRequestSpecification(reqSpec())
                .addFilters([schemaFilter])
                .setContentType(ContentType.JSON)
                .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                .setBaseUri(PGP_HOST)
                .setBasePath(FAST_FWD)
                .setAccept(ContentType.JSON)
    }

    final def req = { given(reqBldr().build()) }

    final def root = {
        [
                "head": [
                        requestId       : UUID.randomUUID() as String,
                        requestTimestamp: System.currentTimeMillis() as String,
                        clientId        : UUID.randomUUID() as String,
                        version         : 'v1',
                        tokenType       : 'SSO',
                        token           : user().tokens['sso'].id,
                        mid             : m().id,
                ],
                "body": [
                        extendInfo  : [
                                merchantUniqueReference: UUID.randomUUID() as String,
                                udf1                   : UUID.randomUUID() as String,
                                udf2                   : UUID.randomUUID() as String,
                                udf3                   : UUID.randomUUID() as String,
                                additionalInfo         : UUID.randomUUID() as String,
                        ],
                        signature   : UUID.randomUUID() as String,
                        reqType     : 'CLW_APP_PAY',
                        paymentMode : 'PPI',
                        txnAmount   : '1',
                        customerId  : UUID.randomUUID() as String,
                        industryType: 'retail',
                        currency    : 'INR',
                        deviceId    : UUID.randomUUID() as String,
                        appIP       : UUID.randomUUID() as String,
                        authMode    : 'USRPWD',
                        channel     : 'WAP',
                        orderId     : UUID.randomUUID() as String,
                ]
        ]
    }

    @Merchant({ it.id== Constants.MerchantType.AddMoney.getId() })
    @AUser(edit = true)
    @Test
    void testSuccess() {
        def root = root()
    //    user().wallets['main'].balance = 1
        User user = new User(user().getMobile(),user().getPassword());
        WalletHelpers.modifyBalance(user, 1);
        req().body(root).post().then()
                .root('body.resultInfo')
                .body(
                        'resultStatus', equalTo('TXN_SUCCESS'),
                        'resultCode', equalTo('01'),
                        'resultMsg', equalTo('Txn Successful.'))
    }
}
