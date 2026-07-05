package scripts.Autologin;

import com.paytm.api.TxnStatus;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.apphelpers.WalletHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.checkoutjs.MerchantConfig;
import com.paytm.framework.core.DriverManager;
import com.paytm.pages.*;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.qameta.allure.Step;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.openqa.selenium.Cookie;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import scala.collection.immutable.Stream;

@Owner("Gagandeep")
public class AutoLogin extends PGPBaseTest {

    private static final String COOKIE_NAME = "pg_login";

    @Step
    private boolean validate_MerchantPreference(String mid) {
        return PGPHelpers.validate_MerchantPreference(mid, "PG_AUTOLOGIN_ENABLED", "Y");
    }

    @Parameters({"theme"})
    @Test(description = "Validate auto login for enhanced subscription flow ")
    public void autologin_enhancedSubscriptionFlow(@Optional("enhancedweb") String theme) throws Exception {
        prerequisite:
        {
            validate_MerchantPreference(Constants.MerchantType.AddnPay.getId());
        }
        DriverManager.getDriver().manage().deleteCookieNamed(COOKIE_NAME);
        User user = userManager.getForWrite(Label.AUTOLOGIN);
        OrderDTO orderDTO = new OrderFactory.SubscriptionWalletOnly(Constants.MerchantType.AddnPay, theme)
                .setTXN_AMOUNT("2.00")
                .setSSO_TOKEN("")
                .build();
        WalletHelpers.modifyBalance(user, Double.parseDouble(orderDTO.getTXN_AMOUNT()));
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.loginStrip().click();
        cashierPage.rememberMeCheckbox().check();
        cashierPage.signin(user.mobNo(), user.password());
        cashierPage.waitUntilLoads();
        cashierPage.payBy(Constants.PayMode.WALLET);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .AssertAll();
        checkoutPage.setDeleteCookie(false);

        OrderDTO orderDTO2 = new OrderFactory.SubscriptionWalletOnly(Constants.MerchantType.AddnPay, theme)
                .setTXN_AMOUNT("1.00")
                .setSSO_TOKEN("")
                .build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO2.getTXN_AMOUNT()));
        checkoutPage.createOrder(orderDTO2);
        cashierPage.pause(2);
        Cookie cookie = DriverManager.getDriver().manage().getCookieNamed(COOKIE_NAME);
        if (null == cookie)
            Assertions.fail("pg_login cookie not found in browser session");
        //Assert user is logged in on cashier page
        cashierPage.WalletTitle().assertVisible();
    }

    @Parameters({"theme"})
    @Test(description = "To verify if user is not auto login when Remember me option is not visible")
    public void autologin_validateRememberMeNotVisible(@Optional("enhancedweb") String theme) throws Exception {
        DriverManager.getDriver().manage().deleteCookieNamed(COOKIE_NAME);
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.Hybrid(Constants.MerchantType.WalletOnly, theme, user)
                .setTXN_AMOUNT("2.00")
                .setSSO_TOKEN("")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.loginStrip().click();
        cashierPage.rememberMeCheckbox().assertNotVisible();

    }

    // Disablinng TC as logout is not supported now
//    @Parameters({"theme"})
//    @Test(enabled = false, description = "Validate auto login for second user after logging out from first")
    public void autologin_validateSecondUser(@Optional("enhancedweb_revamp") String theme) throws Exception {
        prerequisite:
        {
            validate_MerchantPreference(Constants.MerchantType.AddnPay.getId());
        }
        DriverManager.getDriver().manage().deleteCookieNamed(COOKIE_NAME);
        User user1 = userManager.getForRead(Label.LOGIN, Label.PPBL);
        OrderDTO orderDTO1 = new OrderFactory.SubscriptionWalletOnly(Constants.MerchantType.AddnPay, theme)
                .setTXN_AMOUNT("2.00")
                .setSSO_TOKEN(user1.ssoToken())
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO1);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.logout(user1);

        User user2 = userManager.getForRead(Label.AUTOLOGIN);
        OrderDTO orderDTO2 = new OrderFactory.SubscriptionWalletOnly(Constants.MerchantType.AddnPay, theme)
                .setTXN_AMOUNT("2.00")
                .setSSO_TOKEN("")
                .build();
        checkoutPage.createOrder(orderDTO2);
        WalletHelpers.modifyBalance(user2, Double.valueOf(orderDTO2.getTXN_AMOUNT()));
        cashierPage.rememberMeCheckbox().check();
        cashierPage.login(user2);
        cashierPage.payBy(Constants.PayMode.WALLET);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO2.getMID(), orderDTO2.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO2.getORDER_ID())
                .validateTxnAmount(orderDTO2.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO2.getMID())
                .validatePaymentMode("PPI")
                .AssertAll();
        checkoutPage.setDeleteCookie(false);

        WalletHelpers.modifyBalance(user2, Double.valueOf(orderDTO2.getTXN_AMOUNT()) - 1.00);
        OrderDTO orderDTO3 = new OrderFactory.SubscriptionWalletOnly(Constants.MerchantType.AddnPay, theme)
                .setTXN_AMOUNT("1.00")
                .setSSO_TOKEN("")
                .build();
        checkoutPage.createOrder(orderDTO3);
        cashierPage.pause(2);
        Cookie cookie = DriverManager.getDriver().manage().getCookieNamed(COOKIE_NAME);
        if (null == cookie)
            Assertions.fail("pg_login cookie not found in browser session");
        cashierPage.WalletTitle().assertVisible();
    }


    @Parameters({"theme"})
    @Test(description = "To verify if user is not auto login when we clear the cookies")
    public void autologin_afterClearCookie(@Optional("enhancedweb") String theme) throws Exception {
        OrderDTO orderDTO;
        User user;
        CashierPage cashierPage;
        prerequisite:
        {
            validate_MerchantPreference(Constants.MerchantType.AddnPay.getId());
        }
        DriverManager.getDriver().manage().deleteCookieNamed(COOKIE_NAME);
        user = userManager.getForWrite(Label.AUTOLOGIN);
        orderDTO = new OrderFactory.Hybrid(Constants.MerchantType.AddnPay, theme, user)
                .setTXN_AMOUNT("2.00")
                .setSSO_TOKEN("")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.loginStrip().click();
        cashierPage.rememberMeCheckbox().check();
        cashierPage.signin(user.mobNo(), user.password());
        cashierPage.waitUntilLoads();
        Cookie cookie = DriverManager.getDriver().manage().getCookieNamed(COOKIE_NAME);
        if (null == cookie)
            Assertions.fail("pg_login cookie not found in browser session");
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .AssertAll();
        DriverManager.getDriver().manage().deleteCookieNamed(COOKIE_NAME);


        OrderDTO orderDTO2 = new OrderFactory.Hybrid(Constants.MerchantType.AddnPay, theme, user)
                .setTXN_AMOUNT("1.00")
                .setSSO_TOKEN("")
                .build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()));
        checkoutPage.createOrder(orderDTO2);
        cashierPage.waitUntilLoads();
        cashierPage.pause(6);
        if (null == cookie)
            Assertions.fail("pg_login cookie not found in browser session");
        cashierPage.WalletTitle().assertNotVisible();
    }

    @Parameters({"theme"})
    @Test(description = "To verify for some another merchant on same browser")
    public void autologin_anotherMerchant(@Optional("enhancedwap_revamp") String theme) throws Exception {
        OrderDTO orderDTO = null;
        User user = null;
        CashierPage cashierPage = null;
        prerequisite:
        {
            validate_MerchantPreference(Constants.MerchantType.AddnPay.getId());
            validate_MerchantPreference(Constants.MerchantType.PPBLYONLY.getId());
        }
        DriverManager.getDriver().manage().deleteCookieNamed(COOKIE_NAME);
        user = userManager.getForWrite(Label.AUTOLOGIN);
        orderDTO = new OrderFactory.Hybrid(Constants.MerchantType.AddnPay, theme, user)
                .setTXN_AMOUNT("2.00")
                .setSSO_TOKEN("")
                .build();
        WalletHelpers.modifyBalance(user, Double.parseDouble(orderDTO.getTXN_AMOUNT()) - 1);
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.loginStrip().click();
        cashierPage.rememberMeCheckbox().check();
        cashierPage.signin(user.mobNo(), user.password());
        cashierPage.waitUntilLoads();
        Cookie cookie = DriverManager.getDriver().manage().getCookieNamed(COOKIE_NAME);
        if (null == cookie)
            Assertions.fail("pg_login cookie not found in browser session");
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .AssertAll();

        OrderDTO orderDTO2 = new OrderFactory.Hybrid(Constants.MerchantType.PPBLYONLY, theme, user)
                .setTXN_AMOUNT("2.00")
                .setSSO_TOKEN("")
                .build();
        checkoutPage.setDeleteCookie(false);
        checkoutPage.createOrder(orderDTO2);
        cashierPage.pause(2);
        if (null == cookie)
            Assertions.fail("pg_login cookie not found in browser session");
        cashierPage.textBoxPPBLPassCode().assertVisible();
    }


    @Parameters({"theme"})
    @Test(description = "Validate cookie should be created at browser session after user login")
    public void autologin_validateCookie(@Optional("enhancedweb_revamp") String theme) throws Exception {
        prerequisite:
        {
            validate_MerchantPreference(Constants.MerchantType.Hybrid.getId());
        }
        DriverManager.getDriver().manage().deleteCookieNamed(COOKIE_NAME);
        User user = userManager.getForWrite(Label.AUTOLOGIN);
        OrderDTO orderDTO = new OrderFactory.Hybrid(Constants.MerchantType.Hybrid, theme, user)
                .setTXN_AMOUNT("2.00")
                .setSSO_TOKEN("")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.loginStrip().click();
        cashierPage.rememberMeCheckbox().check();
        cashierPage.signin(user.mobNo(), user.password());
        cashierPage.waitUntilLoads();
        Cookie cookie = DriverManager.getDriver().manage().getCookieNamed(COOKIE_NAME);
        if (null == cookie)
            Assertions.fail("pg_login cookie not found in browser session");
    }

    @Parameters({"theme"})
    @Test(description = "Validate cookie should be deleted when logging out")
    public void autologin_validateCookieDeletedAfterLogin(@Optional("enhancedweb") String theme) throws Exception {
        OrderDTO orderDTO = null;
        User user = null;
        CashierPage cashierPage = null;
        DriverManager.getDriver().manage().deleteCookieNamed(COOKIE_NAME);
        prerequisite:
        {
            validate_MerchantPreference(Constants.MerchantType.Hybrid.getId());
            user = userManager.getForWrite(Label.AUTOLOGIN);
            orderDTO = new OrderFactory.Hybrid(Constants.MerchantType.Hybrid, theme, user)
                    .setTXN_AMOUNT("2.00")
                    .setSSO_TOKEN("")
                    .build();
            CheckoutPage checkoutPage = new CheckoutPage();
            checkoutPage.createOrder(orderDTO);
            cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.loginStrip().click();
            cashierPage.rememberMeCheckbox().check();
            cashierPage.signin(user.mobNo(), user.password());
            cashierPage.waitUntilLoads();
            Cookie cookie = DriverManager.getDriver().manage().getCookieNamed(COOKIE_NAME);
            if (null == cookie)
                Assertions.fail("pg_login cookie not found in browser session");
        }

        try {
            cashierPage.logout(user);
        } finally {
            user.purge();
        }
        cashierPage.waitUntilLoads();
        Cookie cookie = DriverManager.getDriver().manage().getCookieNamed(COOKIE_NAME);
        if (null != cookie)
            Assertions.fail("pg_login cookie not found in browser session");

    }

    @Parameters({"theme"})
    @Test(description = "Validate user is logged in after successful txn with autologin enabled")
    public void autologin_validateUserLoggedInAfterTxn(@Optional("enhancedweb") String theme) throws Exception {
        prerequisite:
        {
            validate_MerchantPreference(Constants.MerchantType.Hybrid.getId());
        }
        DriverManager.getDriver().manage().deleteCookieNamed(COOKIE_NAME);
        User user = userManager.getForWrite(Label.AUTOLOGIN);
        OrderDTO orderDTO = new OrderFactory.Hybrid(Constants.MerchantType.Hybrid, theme, user)
                .setTXN_AMOUNT("2.00")
                .setSSO_TOKEN("")
                .build();
        WalletHelpers.setZeroBalance(user);
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.loginStrip().click();
        cashierPage.rememberMeCheckbox().check();
        cashierPage.signin(user.mobNo(), user.password());
        cashierPage.waitUntilLoads();
        cashierPage.payBy(Constants.PayMode.CC);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .AssertAll();
        checkoutPage.setDeleteCookie(false);
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.00);
        checkoutPage.createOrder(orderDTO);
        cashierPage.pause(2);
        Cookie cookie = DriverManager.getDriver().manage().getCookieNamed(COOKIE_NAME);
        if (null == cookie)
            Assertions.fail("pg_login cookie not found in browser session");
        cashierPage.WalletTitle().assertVisible();
    }

    @Parameters({"theme"})
    @Test(description = "Validate user is logged with autologin enabled to complete PPBL Transaction")
    public void autologin_PPBL_S(@Optional("enhancedweb") String theme) throws Exception {
        prerequisite:
        {
            validate_MerchantPreference(Constants.MerchantType.PPBLYONLY.getId());
        }
        DriverManager.getDriver().manage().deleteCookieNamed(COOKIE_NAME);
        User user = userManager.getForWrite(Label.PPBL, Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.Hybrid(Constants.MerchantType.PPBLYONLY, theme, user)
                .setTXN_AMOUNT("10.00")
                .setSSO_TOKEN("")
                .build();
        WalletHelpers.setZeroBalance(user);
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.loginStrip().click();
        cashierPage.rememberMeCheckbox().check();
        cashierPage.signin(user.mobNo(), user.password());
        cashierPage.waitUntilLoads();
        Cookie cookie = DriverManager.getDriver().manage().getCookieNamed(COOKIE_NAME);
        if (null == cookie)
            Assertions.fail("pg_login cookie not found in browser session");
        cashierPage.payBy(Constants.PayMode.PPBL);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("NB")
                .AssertAll();
    }


    @Parameters({"theme"})
    @Epic("PGP-22222")
    @Owner("Tarun")
    @Test(description = "Validate auto login refresh functionality testing")
    public void autoLoginRefresh(@Optional("enhancedweb") String theme) throws Exception {
        prerequisite:
        {
            validate_MerchantPreference(Constants.MerchantType.Hybrid.getId());
        }
        DriverManager.getDriver().manage().deleteCookieNamed(COOKIE_NAME);
        User user = userManager.getForWrite(Label.AUTOLOGIN);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.Hybrid, theme)
                .setTXN_AMOUNT("2.00")
                .setSSO_TOKEN("")
                .build();
        WalletHelpers.modifyBalance(user, Double.parseDouble(orderDTO.getTXN_AMOUNT()));
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.loginStrip().click();
        cashierPage.rememberMeCheckbox().check();
        cashierPage.signin(user.mobNo(), user.password());
        cashierPage.waitUntilLoads();
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .AssertAll();
        checkoutPage.setDeleteCookie(false);

        OrderDTO orderDTO2 = new OrderFactory.PGOnly(Constants.MerchantType.Hybrid, theme)
                .setSSO_TOKEN("")
                .build();
        checkoutPage.createOrder(orderDTO2);
        cashierPage.waitUntilLoads();
        Cookie cookie = DriverManager.getDriver().manage().getCookieNamed(COOKIE_NAME);

        if (null == cookie)
            Assertions.fail("pg_login cookie not found in browser session");

        cashierPage.refresh();
        cashierPage.payBy(Constants.PayMode.DC);

        ResponsePage responsePage2 = new ResponsePage();
        responsePage2.waitUntilLoads();
        responsePage2.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO2.getMID())
                .validateOrderId(orderDTO2.getORDER_ID())
                .validatePaymentMode("DC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();

        TxnStatus txnStatus2 = new TxnStatus(orderDTO2.getMID(), orderDTO2.getORDER_ID());
        txnStatus2.executeUntilNotPending();
        txnStatus2.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO2.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO2.getMID())
                .validatePaymentMode("DC")
                .AssertAll();
    }
    @Owner("SRINIVAS")
    @Feature("PGP-36595")
    @Parameters({"theme"})
    @Test(description = "Validate auto login for appinvokeflow on enhanced page")
    public void AutoLoginforappInvokeflow(@Optional("enhancedweb_revamp") String theme) throws Exception {
        validate_MerchantPreference(Constants.MerchantType.AppInvoke.getId());
        DriverManager.getDriver().manage().deleteCookieNamed(COOKIE_NAME);
        User user = userManager.getForRead(Label.AUTOLOGIN);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", Constants.MerchantType.AppInvoke).build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath JsonPath = initTxn.execute().jsonPath();
        String txnToken = JsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(Constants.MerchantType.AppInvoke, initTxnDTO.getBody().getOrderId(), txnToken).build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.loginStrip().click();
        cashierPage.signin(user.mobNo(), user.password());
        cashierPage.waitUntilLoads();
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("CC")
                .validateBankName("HDFC Bank")
                .AssertAll();
        OrderDTO orderDTO1 = new OrderFactory.AppInvokeOrder(Constants.MerchantType.AppInvoke, initTxnDTO.getBody().getOrderId(), txnToken).build();
        CheckoutPage checkoutPage1 = new CheckoutPage();
        checkoutPage1.createAppInvokeOrder(orderDTO1);
        CashierPage cashierPage1 = CashierPageFactory.getCashierPage(theme);
        cashierPage1.waitUntilLoads();
        cashierPage1.loginStrip().assertNotVisible();
    }


    @Owner("SRINIVAS")
    @Feature("PGP-36595")
    @Parameters({"theme"})
    @Test(description = "Validate user should not be autologin when remember me box is unchecked")
    public void AutologinRememberboxisunchecked(@Optional("enhancedweb_revamp") String theme) throws Exception {
        validate_MerchantPreference(Constants.MerchantType.AppInvoke.getId());
        DriverManager.getDriver().manage().deleteCookieNamed(COOKIE_NAME);
        User user = userManager.getForRead(Label.AUTOLOGIN);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", Constants.MerchantType.AppInvoke).build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");

        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(Constants.MerchantType.AppInvoke, initTxnDTO.getBody().getOrderId(), txnToken).build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.loginStrip().click();
        cashierPage.rememberMeCheckbox().click();
        cashierPage.signin(user.mobNo(), user.password());
        cashierPage.waitUntilLoads();
        cashierPage.payBy(Constants.PayMode.CC);

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("CC")
                .validateBankName("HDFC Bank")
                .AssertAll();

        InitTxnDTO initTxnDTO1 = new InitTxnDTO.Builder("", Constants.MerchantType.AppInvoke).build();
        InitTxn initTxn1 = new InitTxn(initTxnDTO1);
        JsonPath JsonPath1 = initTxn1.execute().jsonPath();
        String txnToken1 = JsonPath1.getString("body.txnToken");

        OrderDTO orderDTO1 = new OrderFactory.AppInvokeOrder(Constants.MerchantType.AppInvoke, initTxnDTO1.getBody().getOrderId(), txnToken1).build();
        CheckoutPage checkoutPage1 = new CheckoutPage();
        checkoutPage1.createAppInvokeOrder(orderDTO1);
        CashierPage cashierPage1 = CashierPageFactory.getCashierPage(theme);
        cashierPage1.waitUntilLoads();
        cashierPage1.loginStrip().assertVisible();
    }


    @Owner("SRINIVAS")
    @Feature("PGP-36595")
    @Parameters({"theme"})
    @Test(description = "Validate user should not be autologin when cookie is deleted")
    public void Autologinaftercookiecleared(@Optional("enhancedweb_revamp") String theme) throws Exception {
        validate_MerchantPreference(Constants.MerchantType.AppInvoke.getId());
        DriverManager.getDriver().manage().deleteCookieNamed(COOKIE_NAME);
        User user = userManager.getForRead(Label.AUTOLOGIN);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", Constants.MerchantType.AppInvoke).build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath JsonPath = initTxn.execute().jsonPath();
        String txnToken = JsonPath.getString("body.txnToken");

        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(Constants.MerchantType.AppInvoke, initTxnDTO.getBody().getOrderId(), txnToken).build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.loginStrip().click();
        cashierPage.rememberMeCheckbox().click();
        cashierPage.signin(user.mobNo(), user.password());
        cashierPage.waitUntilLoads();
        cashierPage.payBy(Constants.PayMode.CC);

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("CC")
                .validateBankName("HDFC Bank")
                .AssertAll();
        DriverManager.getDriver().manage().deleteCookieNamed(COOKIE_NAME);
        InitTxnDTO initTxnDTO1 = new InitTxnDTO.Builder("", Constants.MerchantType.AppInvoke).build();
        InitTxn initTxn1 = new InitTxn(initTxnDTO1);
        JsonPath JsonPath1 = initTxn1.execute().jsonPath();
        String txnToken1 = JsonPath1.getString("body.txnToken");

        OrderDTO orderDTO1 = new OrderFactory.AppInvokeOrder(Constants.MerchantType.AppInvoke, initTxnDTO1.getBody().getOrderId(), txnToken1).build();
        CheckoutPage checkoutPage1 = new CheckoutPage();
        checkoutPage1.createAppInvokeOrder(orderDTO1);
        CashierPage cashierPage1 = CashierPageFactory.getCashierPage(theme);
        cashierPage1.waitUntilLoads();
        cashierPage1.loginStrip().assertVisible();
    }

    @Owner(Constants.Owner.PUSPA)
    @Feature("PGP-41941")
    @Parameters({"theme"})
    @Test(description = "Validate If user logged in via Cookies then postpaid should show above wallet")
    public void verifyUserLoggedInViaCokkiePostpaidaboveWallet_jsonEnhance(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        validate_MerchantPreference(Constants.MerchantType.AUTOLOGIN_MID.getId());
        DriverManager.getDriver().manage().deleteCookieNamed(COOKIE_NAME);
        User user= userManager.getForRead(Label.POSTPAID);
        CheckoutPage checkoutPage =new CheckoutPage();
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.AUTOLOGIN_MID, theme)
                .setTXN_AMOUNT("1.00")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.loginStrip().click();
        cashierPage.signin(user.mobNo(),user.mobNo());
        cashierPage.payBy(Constants.PayMode.CC);

        ResponsePage responsePage=new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validatePaymentMode("CC")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
        checkoutPage.setDeleteCookie(false);

        OrderDTO orderDTO1 = new OrderFactory.PGOnly(Constants.MerchantType.AUTOLOGIN_MID, theme)
                .setTXN_AMOUNT("1.00")
                .build();
        checkoutPage.createOrder(orderDTO1);
        CashierPage cashierPage1 = CashierPageFactory.getCashierPage(theme);
        cashierPage1.waitUntilLoads();
        cashierPage1.radioButtonPaytmPostpaid().isSelected();
    }
}

