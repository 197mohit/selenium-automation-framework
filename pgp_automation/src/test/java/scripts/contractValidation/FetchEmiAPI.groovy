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
class FetchEmiAPI extends TestSetUp implements ContractTest {

    final def req = { mId, orderId ->
        given(
                new RequestSpecBuilder()
                        .addRequestSpecification(reqSpec())
                        .setContentType(ContentType.JSON)
                        .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                        .setBaseUri(PGP_HOST)
                        .setBasePath('/fetchEMIDetail')
                        .addQueryParams([mid: mId, orderId: orderId])
                        .build()
        )
    }

    final def root = {
        [
                head: [
                        txnToken: null,
                ],
                body: [
                        channelCode: null,
                ]
        ]
    }

    List<String> headAttr
    List<String> bodyAttr
    List<String> emiChannelAttr
    List<String> emiChannelInfoAttr

    @BeforeClass
    void setUp() {
        DriverManager.getDriver().get('https://developer.paytm.com/docs/fetch-bin-details-api/')
        headAttr = DriverManager.getDriver().findElements(By.xpath("//h2[text() = 'Response Attributes']/following-sibling::h3[text()='Head']/following-sibling::table[1]//tr/td[1]/strong")).collect {
            it.text.trim()
        }
        bodyAttr = DriverManager.getDriver().findElements(By.xpath("//h2[text() = 'Response Attributes']/following-sibling::h3[text()='Body']/following-sibling::table[1]//tr/td[1]/strong")).collect {
            it.text.trim()
        }
        emiChannelAttr = DriverManager.getDriver().findElements(By.xpath("//h3[text()='EmiChannel']/following-sibling::table//strong")).collect {
            it.text.trim()
        }
        emiChannelInfoAttr = DriverManager.getDriver().findElements(By.xpath("//h3[text()='EMIChannelInfo']/following-sibling::table//strong")).collect {
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
        root.body.channelCode = cards.find().scheme
        req(m().id, m().orders.last().id).body(root).post().then().statusCode(200).extract().path('').with {
            assert this.headAttr.containsAll(it.head.keySet())
            assert this.bodyAttr.containsAll(it.body.keySet())
            assert this.emiChannelAttr.containsAll(it.body.emiDetail.keySet())
            assert this.emiChannelInfoAttr.containsAll(it.body.emiDetail.emiChannelInfos.keySet())
            if (it.body.emiDetail.emiHybridChannelInfos) assert this.emiChannelInfoAttr.containsAll(it.body.emiDetail.emiHybridChannelInfos.keySet())
        }
    }

}
