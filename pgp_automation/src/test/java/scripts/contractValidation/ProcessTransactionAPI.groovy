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
class ProcessTransactionAPI extends TestSetUp implements ContractTest {

    final def req = {
        given(
                new RequestSpecBuilder()
                        .addRequestSpecification(reqSpec())
                        .setContentType(ContentType.JSON)
                        .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                        .setBaseUri(PGP_HOST)
                        .setBasePath('/theia/api/v1/processTransaction')
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
                        mid        : null,
                        orderId    : null,
                        paymentMode: 'NET_BANKING',
                        channelCode: 'ICICI',
                        requestType: 'DEFAULT',
                ]
        ]
    }

    List<String> headAttr
    List<String> bodyAttr
    List<String> bankFormAttr
    List<String> txnInfoAttr

    @BeforeClass
    void setUp() {
        DriverManager.getDriver().get('https://developer.paytm.com/docs/process-transaction-api/')
        headAttr = DriverManager.getDriver().findElements(By.xpath("//h2[text() = 'Response Attributes']/following-sibling::h3[text()='Head']/following-sibling::table[1]//strong")).collect {
            it.text.trim()
        }
        bodyAttr = DriverManager.getDriver().findElements(By.xpath("//h2[text() = 'Response Attributes']/following-sibling::h3[text()='Body']/following-sibling::table[1]//strong")).collect {
            it.text.trim()
        }
        bankFormAttr = DriverManager.getDriver().findElements(By.xpath("//h3[text()='BankForm']/following-sibling::table[1]//strong")).collect {
            it.text.trim()
        }
        txnInfoAttr = DriverManager.getDriver().findElements(By.xpath("//h3[text()='TxnInfo']/following-sibling::table[1]//strong")).collect {
            it.text.trim()
        }
    }

    @AfterClass(alwaysRun = true)
    void tearDown() {
        DriverManager.closeCurrentDriver()
    }


    @Merchant
    @Test()
    void testResponseAttributesAreAsExpected() {
        def root = root()
        assert m().orders.add(new OrderV2(1))
        root.head.txnToken = m().orders.last().transaction.token
        root.body << [mid: m().id, orderId: m().orders.last().id]
        req().queryParams([mid: m().id, orderId: m().orders.last().id]).body(root).post().then().statusCode(200).extract().path('').with {
            assert this.headAttr.containsAll(it.head.keySet())
            assert this.bodyAttr.containsAll(it.body.keySet())
            if (it.body.bankForm) assert this.bankFormAttr.containsAll(it.body.bankForm.keySet())
            if (it.body.txnInfo) assert this.txnInfoAttr.containsAll(it.body.txnInfo.keySet())
        }
    }
}
