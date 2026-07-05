package scripts.contractValidation

import com.paytm.apphelpers.PGPHelpers
import com.paytm.base.test.TestSetUp
import com.paytm.framework.core.DriverManager
import com.paytm.utils.merchant.merchant.util.annotations.Merchant
import com.paytm.utils.merchant.user.annotations.AUser
import io.qameta.allure.Owner
import io.restassured.builder.RequestSpecBuilder
import io.restassured.http.ContentType
import org.openqa.selenium.By
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test

import static com.paytm.LocalConfig.PGP_HOST
import static com.paytm.utils.merchant.util.PGPUtil.getChecksum
import static groovy.json.JsonOutput.toJson
import static io.restassured.RestAssured.given

@Owner("Deepak")
class PreAuthAPI extends TestSetUp implements ContractTest {

    final def req = {
        given(
                new RequestSpecBuilder()
                        .addRequestSpecification(reqSpec())
                        .setContentType(ContentType.JSON)
                        .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                        .setBaseUri(PGP_HOST)
                        .setBasePath('/order/preAuth')
                        .build()
        )
    }

    final def root = {
        [
                MID       : null,
                ORDER_ID  : new Random().nextLong() as String,
                TXN_AMOUNT: null,
                TOKEN     : null,
                CHECKSUM  : null,
        ]
    }

    List<String> resAttr

    @BeforeClass
    void setUp() {
        DriverManager.getDriver().get('https://developer.paytm.com/docs/pre-auth-api/')
        resAttr = DriverManager.getDriver().findElements(By.xpath("//h2[text() = 'Response Attributes']/following-sibling::div[1]/table[1]//tr/td[1]/strong")).collect {
            it.getText()
        }
    }

    @AfterClass(alwaysRun = true)
    void tearDown() {
        DriverManager.closeCurrentDriver()
    }

    @Merchant
    @AUser
    @Test
    void testResponseAttributesAreAsExpected() {
        def root = root()
        root << [MID: m().id, TXN_AMOUNT: 1 as String, TOKEN: user().tokens['paytm'].id]
        root.CHECKSUM = getChecksum(m().key, toJson(root))
        req().body(root).post().then().statusCode(200).extract().path('').with {
            this.resAttr.containsAll(it.keySet())
        }
    }
}
