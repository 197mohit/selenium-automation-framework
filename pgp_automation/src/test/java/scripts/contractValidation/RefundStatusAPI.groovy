package scripts.contractValidation

import com.paytm.apphelpers.PGPHelpers
import com.paytm.base.test.TestSetUp
import com.paytm.framework.core.DriverManager
import com.paytm.utils.merchant.merchant.util.OrderV2
import com.paytm.utils.merchant.merchant.util.RefundV2
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
class RefundStatusAPI extends TestSetUp implements ContractTest {

    final def req = {
        given(
                new RequestSpecBuilder()
                        .addRequestSpecification(reqSpec())
                        .setContentType(ContentType.JSON)
                        .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                        .setBaseUri(PGP_HOST)
                        .setBasePath('/v2/refund/status')
                        .build()
        )
    }

    final def root = {
        [
                head: [
                        signature: null,
                ],
                body: [
                        mid    : null,
                        orderId: null,
                        refId  : null,
                ]
        ]
    }

    List<String> headAttr
    List<String> bodyAttr
    List<String> refundDetailInfoListAttr

    @BeforeClass
    void setUp() {
        DriverManager.getDriver().get('https://developer.paytm.com/docs/refund-status-api/')
        headAttr = DriverManager.getDriver().findElements(By.xpath("//h2[text() = 'Response Attributes']/following-sibling::h3[text()='Head']/following-sibling::table[1]//strong")).collect {
            it.getText().trim()
        }
        bodyAttr = DriverManager.getDriver().findElements(By.xpath("//h2[text() = 'Response Attributes']/following-sibling::h3[text()='Body']/following-sibling::table[1]//strong")).collect {
            it.getText().trim()
        }
        refundDetailInfoListAttr = DriverManager.getDriver().findElements(By.xpath("//h3[text()='RefundDetailInfoList']/following-sibling::table[1]//strong")).collect {
            it.getText().trim()
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
        m().orders.last().payUsingNB()
        assertion.apply(txnStatusWait.apply({ m().orders.last().transaction.status == 'TXN_SUCCESS' }))
        assert m().orders.last().refunds.add(new RefundV2(m().orders.last().amt))
        root.body = [mid: m().id, orderId: m().orders.last().id, refId: m().orders.last().refunds.last().id]
        root.head.signature = getChecksum(m().key, toJson(root.body))
        req().body(root).post().then().statusCode(200).extract().path('').with {
            assert this.headAttr.containsAll(it.head.keySet())
            assert this.bodyAttr.containsAll(it.body.keySet())
            assert this.refundDetailInfoListAttr.containsAll(it.body.refundDetailInfoList.keySet())
        }
    }
}
