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
class AutoDebitAPI extends TestSetUp implements ContractTest {

    final def req = {
        given(
                new RequestSpecBuilder()
                        .addRequestSpecification(reqSpec())
                        .setContentType(ContentType.JSON)
                        .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                        .setBaseUri(PGP_HOST)
                        .setBasePath('/order/directPay')
                        .build()
        )
    }

    final def root = {
        [
                MID         : null,
                ReqType     : 'WITHDRAW',
                TxnAmount   : null,
                AppIP       : '127.0.0.1',
                OrderId     : new Random().nextLong() as String,
                DeviceId    : new Random().nextLong() as String,
                Currency    : 'INR',
                SSOToken    : null,
                PaymentMode : null,
                CustId      : new Random().nextLong() as String,
                IndustryType: 'retail',
                Channel     : 'WEB',
                AuthMode    : null,
                CheckSum    : null,
        ]
    }

    List<String> resAttr

    @BeforeClass
    void setUp() {
        DriverManager.getDriver().get('https://developer.paytm.com/docs/v1/auto-debit-api/')
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
        root << [MID: m().id, TxnAmount: 1, SSOToken: user().tokens['sso'].id, PaymentMode: 'PPI', AuthMode: 'USRPWD']
        root.CheckSum = getChecksum(m().key, toJson(root))
        req().body(root).post().then().statusCode(200).extract().path('').with {
            this.resAttr.containsAll(it.keySet())
        }
    }
}
