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
import static io.restassured.RestAssured.given

@Owner("Deepak")
class FetchNBPaymentChannelAPI extends TestSetUp implements ContractTest {

    final def req = { mId, orderId ->
        given(
                new RequestSpecBuilder()
                        .addRequestSpecification(reqSpec())
                        .setContentType(ContentType.JSON)
                        .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                        .setBaseUri(PGP_HOST)
                        .setBasePath('/theia/api/v1/fetchNBPaymentChannels')
                        .addQueryParams([mid: mId, orderId: orderId])
                        .build()
        )
    }

    final def root = {
        [
                head: [
                        channelId: 'WEB',
                        txnToken : null,
                ],
                body: [
                        type: 'MERCHANT',
                ]
        ]
    }

    List<String> headAttr
    List<String> bodyAttr
    List<String> payMethodAttr
    List<String> payChannelBaseAttr
    List<String> statusInfoAttr
    List<String> resultInfoAttr

    @BeforeClass
    void setUp() {
        DriverManager.getDriver().get('https://developer.paytm.com/docs/fetch-nb-payment-channels-api/')
        headAttr = DriverManager.getDriver().findElements(By.xpath("//h2[text() = 'Response Attributes']/following-sibling::h3[text()='Head']/following-sibling::table[1]//strong")).collect {
            it.text.trim()
        }
        bodyAttr = DriverManager.getDriver().findElements(By.xpath("//h2[text() = 'Response Attributes']/following-sibling::h3[text()='Body']/following-sibling::table[1]//strong")).collect {
            it.text.trim()
        }
        payMethodAttr = DriverManager.getDriver().findElements(By.xpath("//h3[text()='PayMethod']/following-sibling::table[1]//strong")).collect {
            it.text.trim()
        }
        payChannelBaseAttr = DriverManager.getDriver().findElements(By.xpath("//h3[text()='PayChannelBase']/following-sibling::table[1]//strong")).collect {
            it.text.trim()
        }
        statusInfoAttr = DriverManager.getDriver().findElements(By.xpath("//h3[text()='StatusInfo']/following-sibling::table[1]//strong")).collect {
            it.text.trim()
        }
        resultInfoAttr = DriverManager.getDriver().findElements(By.xpath("//h3[text()='ResultInfo']/following-sibling::table[1]//strong")).collect {
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
        root.head.txnToken = m().orders.last().transaction.token
        req(m().id, m().orders.last().id).body(root).post().then().statusCode(200).extract().path('').with {
            assert this.headAttr.containsAll(it.head.keySet())
            assert this.bodyAttr.containsAll(it.body.keySet())
            assert this.payMethodAttr.containsAll(it.body.nbPayOption.keySet())
            assert this.statusInfoAttr.containsAll(it.body.nbPayOption.isDisabled.keySet())
            it.body.nbPayOption.payChannelOptions.each {
                assert this.payChannelBaseAttr.containsAll(it.keySet())
                assert this.statusInfoAttr.containsAll(it.isDisabled.keySet())
                assert this.statusInfoAttr.containsAll(it.hasLowSuccess.keySet())
            }
        }
    }
}
