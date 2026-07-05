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
class CheckBalanceAPI extends TestSetUp implements ContractTest {

    final def req = {
        given(
                new RequestSpecBuilder()
                        .addRequestSpecification(reqSpec())
                        .setContentType(ContentType.JSON)
                        .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                        .setBaseUri(PGP_HOST)
                        .setBasePath('/paymentservices/pay/consult')
                        .build()
        )
    }

    final def root = {
        [
                head: [
                        requestTimestamp: System.currentTimeMillis(),
                        channelId       : 'WEB',
                        signature       : null,
                ],
                body: [
                        userToken  : null,
                        totalAmount: null,
                        mid        : null,
                ]
        ]
    }

    List<String> headAttr
    List<String> bodyAttr

    @BeforeClass
    void setUp() {
        DriverManager.getDriver().get('https://developer.paytm.com/docs/check-balance-api/')
        headAttr = DriverManager.getDriver().findElements(By.xpath("//h2[text() = 'Response Attributes']/following-sibling::h3[text()='Head:']/following-sibling::div[1]/table[1]//tr/td[1]/strong")).collect {
            it.text.trim()
        }
        bodyAttr = DriverManager.getDriver().findElements(By.xpath("//h2[text() = 'Response Attributes']/following-sibling::h3[text()='Body:']/following-sibling::div[1]/table[1]//tr/td[1]/strong")).collect {
            it.text.trim()
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
        root.body << [userToken: user().tokens['paytm'].id, totalAmount: 1 as String, mid: m().id]
        root.head.signature = getChecksum(m().key, toJson(root.body))
        req().body(root).post().then().statusCode(200).extract().path('').with {
            this.headAttr.containsAll(it.head.keySet())
            this.bodyAttr.containsAll(it.body.keySet())
        }
    }
}
