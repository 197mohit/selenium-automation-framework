package scripts.contractValidation

import com.paytm.apphelpers.PGPHelpers
import com.paytm.base.test.TestSetUp
import com.paytm.framework.core.DriverManager
import com.paytm.utils.merchant.merchant.util.OrderV2
import com.paytm.utils.merchant.merchant.util.annotations.Merchant
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
class UpdateTransactionAPI extends TestSetUp implements ContractTest {

    final def req = { mId, orderId ->
        given(
                new RequestSpecBuilder()
                        .addRequestSpecification(reqSpec())
                        .setContentType(ContentType.JSON)
                        .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                        .setBaseUri(PGP_HOST)
                        .setBasePath('/theia/api/v1/updateTransactionDetail')
                        .addQueryParams([mid: mId, orderId: orderId])
                        .build()
        )
    }

    final def root = {
        [
                head: [
                        txnToken : null,
                        channelId: 'WEB',
                        signature: null,
                ],
                body: [
                        txnAmount: [
                                currency: 'INR',
                                value   : null,
                        ]
                ]
        ]
    }

    List<String> headAttr
    List<String> bodyAttr

    @BeforeClass
    void setUp() {
        DriverManager.getDriver().get('https://developer.paytm.com/docs/update-transaction-detail-api')
        headAttr = DriverManager.getDriver().findElements(By.xpath("//h2[text() = 'Response Attributes']/following-sibling::h3[text()='Head']/following-sibling::table[1]//tr/td/strong")).collect {
            it.text.trim()
        }
        bodyAttr = DriverManager.getDriver().findElements(By.xpath("//h2[text() = 'Response Attributes']/following-sibling::h3[text()='Body']/following-sibling::table[1]//tr/td/strong")).collect {
            it.text.trim()
        }
    }

    @AfterClass(alwaysRun = true)
    void tearDown() {
        DriverManager.closeCurrentDriver()
    }

    @Merchant
    @Test
    void testResponseAttributesAreAsExpected() {
        def root = root()
        assert m().orders.add(new OrderV2(1))
        root.body.txnAmount.value = m().orders.last().amt + 1
        root.head.txnToken = m().orders.last().transaction.token
        root.head.signature = getChecksum(m().key, toJson(root.body))
        req(m().id, m().orders.last().id).body(root).post().then().statusCode(200).extract().path('').with {
            assert this.headAttr.containsAll(it.head.keySet())
            assert this.bodyAttr.containsAll(it.body.keySet())
        }
    }
}
