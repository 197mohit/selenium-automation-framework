package scripts.contractValidation

import com.paytm.apphelpers.CommonHelpers
import com.paytm.apphelpers.PGPHelpers
import com.paytm.base.test.TestSetUp
import com.paytm.framework.core.DriverManager
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
class InitiateSubscriptionAPI extends TestSetUp implements ContractTest {

    final def req = { mId, orderId ->
        given(
                new RequestSpecBuilder()
                        .addRequestSpecification(reqSpec())
                        .setContentType(ContentType.JSON)
                        .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                        .setBaseUri(PGP_HOST)
                        .setBasePath('/subscription/create')
                        .addQueryParams([mid: mId, orderId: orderId])
                        .build()
        )
    }

    final def root = {
        [
                head: [
                        signature: null,
                ],
                body: [
                        requestType              : 'NATIVE_SUBSCRIPTION',
                        mid                      : null,
                        orderId                  : null,
                        websiteName              : null,
                        txnAmount                : [
                                currency: 'INR',
                                value   : null,
                        ],
                        userInfo                 : [
                                custId: CommonHelpers.generateOrderId()
                        ],
                        subscriptionAmountType   : 'FIX',
                        subscriptionFrequencyUnit: 'MONTH',
                        subscriptionFrequency    : 1,
                        subscriptionExpiryDate   : '2022-12-30',
                        subscriptionEnableRetry  : 0,
                ]
        ]
    }

    List<String> headAttr
    List<String> bodyAttr

    @BeforeClass
    void setUp() {
        DriverManager.getDriver().get('https://developer.paytm.com/docs/initiate-subscription-api/')
        headAttr = DriverManager.getDriver().findElements(By.xpath("//h2[text() = 'Response Attributes']/following-sibling::h3[text()='Head']/following-sibling::div[1]/table[1]//tr/td[1]")).collect {
            it.text.split('\n')[0].trim()
        }
        bodyAttr = DriverManager.getDriver().findElements(By.xpath("//h2[text() = 'Response Attributes']/following-sibling::h3[text()='Body']/following-sibling::div[1]/table[1]//tr/td[1]")).collect {
            it.text.split('\n')[0].trim()
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
        root.body << [mid: m().id, orderId: CommonHelpers.generateOrderId(), websiteName: 'retail']
        root.body.txnAmount.value = 1
        root.head.signature = getChecksum(m().key, toJson(root.body))
        req(root.body.mid, root.body.orderId).body(root).post().then().statusCode(200).extract().path('').with {
            assert this.headAttr.containsAll(it.head.keySet())
            assert this.bodyAttr.containsAll(it.body.keySet())
        }
    }
}
