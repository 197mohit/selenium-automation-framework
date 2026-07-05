package scripts;

import com.paytm.api.nativeAPI.InitProcessTxnNew;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.WalletHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.framework.core.DriverManager;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.hamcrest.Matchers;
import org.testng.ITest;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.SkipException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.lang.reflect.Method;

import static com.paytm.appconstants.Constants.Owner.ESHANI;

public class AppIntentUrlPgp32827 extends PGPBaseTest implements ITest {

    private static final String KEY = "theme";
    public ThreadLocal<String> theme = new ThreadLocal<>();
    private ThreadLocal<String> testName = new ThreadLocal<>();


    @Override
    public String getTestName() {
        return testName.get();
    }

    @BeforeMethod(alwaysRun = true)
    public void beforeClass(ITestContext context, Method method, ITestResult testResult, Object[] testData) {
        try {
            theme.set(null == context.getCurrentXmlTest().getParameter(KEY) ? "enhancedweb_revamp" : context.getCurrentXmlTest().getParameter(KEY));
            System.err.println("theme = " + theme.get());
        } catch (Throwable e) {
            testResult.setStatus(ITestResult.SKIP);
            testResult.setThrowable(new SkipException(method.getName(), e));
        }
    }


    @DataProvider(name = "differentPaymodes")
    public Object[][] differentPaymodes() {

        Object[][] paymodes = new Object[2][1];
        paymodes[0][0] = Constants.PayMode.CC;
        paymodes[1][0] = Constants.PayMode.WALLET;
        return paymodes;
    }


    @Owner(ESHANI)
    @Feature("PGP-32827")
    @Test(description = "Verify that after txn is done page moves to appIntentUrl specified in Initiate request", dataProvider = "differentPaymodes")
    public void VerifyRedirectiontoappIntentUrlforNonPCF(Constants.PayMode paymode) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        switch (paymode) {
            case WALLET:
                //WalletHelpers.modifyBalance(user, 5.0);
                break;
        }
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType).build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        String sentUrl = "https://pgp-automation.paytm.in/merchant-checkout/checkout.html";
        initTxn.setContext("body.needAppIntentEndpoint", true)
                .setContext("body.appCallbackUrl", sentUrl)
                .setContext("body.browserName", "googlechrome");

        Response response = initTxn.execute(true);
        response.then()
                .statusCode(200)
                .body("body", Matchers.notNullValue())
                .body("body.appIntentUrl", Matchers.containsString("api/v2/showPaymentPage?"));

        String url = response.jsonPath().getString("body.appIntentUrl");

        DriverManager.getDriver().get(url);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme.get());
        cashierPage.payBy(paymode);
        cashierPage.waitUntilLoads();
        Assertions.assertThat(DriverManager.getCurrentWebDriver().getCurrentUrl())
                .as("Page has not moved to the url sent in Initiate request")
                .isEqualToIgnoringCase(sentUrl);
    }

    @Owner(ESHANI)
    @Feature("PGP-32827")
    @Test(description = "Verify that after txn is done page moves to appIntentUrl specified in Initiate request for pcf merchant", dataProvider = "differentPaymodes")
    public void VerifyRedirectiontoappIntentUrlforPCF(Constants.PayMode paymode) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        switch (paymode) {
            case WALLET:
                WalletHelpers.modifyBalance(user, 5.0);
                break;
        }

        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly_Pcf;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType).build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        String sentUrl = "https://pgp-automation.paytm.in/merchant-checkout/checkout.html";
        initTxn.setContext("body.needAppIntentEndpoint", true)
                .setContext("body.appCallbackUrl", sentUrl)
                .setContext("body.browserName", "googlechrome");

        Response response = initTxn.execute(true);
        response.then()
                .statusCode(200)
                .body("body", Matchers.notNullValue())
                .body("body.appIntentUrl", Matchers.containsString("api/v3/showPaymentPage?"));

        String url = response.jsonPath().getString("body.appIntentUrl");

        DriverManager.getDriver().get(url);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme.get());
        cashierPage.payBy(paymode);
        cashierPage.waitUntilLoads();
        Assertions.assertThat(DriverManager.getCurrentWebDriver().getCurrentUrl())
                .as("Page has not moved to the url sent in Initiate request")
                .isEqualTo(sentUrl);
    }

    @Owner(ESHANI)
    @Feature("PGP-32827")
    @Test(description = "Verify that appIntentUrl not received if needAppIntentEndpoint is specified as false in Initiate request ")
    public void VerifyAppIntentUrlNotReceivedWhenneedAppIntentEndpointisFalse() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType).build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        String sentUrl = "https://pgp-automation.paytm.in/merchant-checkout/checkout.html";
        initTxn.setContext("body.needAppIntentEndpoint", false)
                .setContext("body.appCallbackUrl", sentUrl)
                .setContext("body.browserName", "googlechrome");

        Response response = initTxn.execute(true);
        response.then()
                .statusCode(200)
                .body("body", Matchers.notNullValue())
                .body("body.txnToken", Matchers.notNullValue())
                .body("body.appIntentUrl", Matchers.nullValue());

    }


    @Owner(ESHANI)
    @Feature("PGP-32827")
    @Test(description = "Verify that after txn is done page moves to appIntentUrl specified in new Initiate/processTxn request", dataProvider = "differentPaymodes")
    public void VerifyRedirectiontoappIntentUrlforNonPCFinNewInitAPI(Constants.PayMode paymode) throws Exception {

        User user = userManager.getForWrite(Label.BASIC);
        switch (paymode) {
            case WALLET:
                WalletHelpers.modifyBalance(user, 5.0);
                break;
        }

        Constants.MerchantType merchantType = Constants.MerchantType.NOCHECKSUM_DCSUBVENTION;
        String sentUrl = "https://pgp-automation.paytm.in/merchant-checkout/checkout.html";

        OrderDTO orderDTO = new OrderFactory.PGOnly(merchantType, "merchant4")
                .setSSO_TOKEN(user.ssoToken())
                .build();


        InitProcessTxnNew initProcessTxnNew = new InitProcessTxnNew(orderDTO, sentUrl);
        Response response = initProcessTxnNew.execute();
        response.then()
                .statusCode(200)
                .body("body", Matchers.notNullValue())
                .body("body.appIntentUrl", Matchers.containsString("api/v2/showPaymentPage?"));

        String url = response.jsonPath().getString("body.appIntentUrl");

        DriverManager.getDriver().get(url);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme.get());
        cashierPage.payBy(paymode);
        cashierPage.waitUntilLoads();
        Assertions.assertThat(DriverManager.getCurrentWebDriver().getCurrentUrl())
                .as("Page has not moved to the url sent in Initiate request")
                .isEqualTo(sentUrl);
    }

    @Owner(ESHANI)
    @Feature("PGP-32827")
    @Test(description = "Verify that after txn is done page moves to appIntentUrl specified in new Initiate/processTxn request for pcf merchant", dataProvider = "differentPaymodes")
    public void VerifyRedirectiontoappIntentUrlforPCFinNewInitTxnAPI(Constants.PayMode paymode) throws Exception {

        User user = userManager.getForWrite(Label.BASIC);
        switch (paymode) {
            case WALLET:
                WalletHelpers.modifyBalance(user, 5.0);
                break;
        }

        Constants.MerchantType merchantType = Constants.MerchantType.NOCHECKSUM_PCF;
        OrderDTO orderDTO = new OrderFactory.PGOnly(merchantType, theme.get())
                .setSSO_TOKEN(user.ssoToken())
                .build();

        String sentUrl = "https://pgp-automation.paytm.in/merchant-checkout/checkout.html";

        InitProcessTxnNew initProcessTxnNew = new InitProcessTxnNew(orderDTO, sentUrl);

        Response response = initProcessTxnNew.execute();
        response.then()
                .statusCode(200)
                .body("body", Matchers.notNullValue())
                .body("body.appIntentUrl", Matchers.containsString("api/v3/showPaymentPage?"));

        String url = response.jsonPath().getString("body.appIntentUrl");

        DriverManager.getDriver().get(url);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme.get());
        cashierPage.payBy(paymode);
        cashierPage.waitUntilLoads();
        Assertions.assertThat(DriverManager.getCurrentWebDriver().getCurrentUrl())
                .as("Page has not moved to the url sent in Initiate request")
                .isEqualTo(sentUrl);
    }

}
