package scripts.api.theia

import com.paytm.apphelpers.PGPHelpers
import com.paytm.base.test.TestSetUp
import io.qameta.allure.Owner
import io.restassured.builder.RequestSpecBuilder
import io.restassured.http.ContentType
import org.testng.annotations.Test

import static com.paytm.LocalConfig.PGP_HOST
import static io.restassured.RestAssured.given
import static org.hamcrest.Matchers.stringContainsInOrder

@Owner("Deepak")
class ErrorJsp extends TestSetUp {

    final def reqBldr = {
        new RequestSpecBuilder()
                .addRequestSpecification(reqSpec())
                .setContentType(ContentType.JSON)
                .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                .setBaseUri(PGP_HOST)
                .setBasePath('/theia/jsp/error.jsp')
    }

    final def req = { given(reqBldr().build()) }

    @Test
    void testSuccess() {
        req().get().then()
                .statusCode(200)
                .body('html.body.toString()', stringContainsInOrder(['Something went wrong', 'It may be due to any of these reasons', 'Session expired due to inactivity', 'Our system encountered an obstacle', 'You can fix it yourself!', "Here's how", 'Check payment status with your bank to avoid double payment', 'Clear cookies & temporary internet files of the browser & retry', 'Launch a new browser & start from the beginning', 'till unable to transact? visit us at paytm.com/care']))
    }
}
