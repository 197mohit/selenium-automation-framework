package scripts.api.merchantStatus

import com.paytm.apphelpers.PGPHelpers
import com.paytm.base.test.TestSetUp
import io.qameta.allure.Owner
import io.restassured.builder.RequestSpecBuilder
import io.restassured.http.ContentType

import static com.paytm.LocalConfig.PGP_HOST
import static com.paytm.appconstants.Constants.PGPAPIResourcePath.TXNSTATUS
import static io.restassured.RestAssured.given

@Owner("Deepak")
class TxnStatus extends TestSetUp {

    final def req = {
        given(
                new RequestSpecBuilder()
                        .addRequestSpecification(reqSpec())
                        .setContentType(ContentType.JSON)
                        .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                        .setBaseUri(PGP_HOST)
                        .setBasePath(TXNSTATUS)
                        .build()
        )
    }

    final def root = {
        [
                MID    : m().id,
                ORDERID: new Random().nextLong().abs() as String
        ]
    }
}
