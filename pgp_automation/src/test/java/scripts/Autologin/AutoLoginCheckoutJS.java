package scripts.Autologin;

import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.checkoutjs.MerchantConfig;
import com.paytm.framework.core.DriverManager;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutJsCheckoutPage;
import com.paytm.pages.ResponsePage;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

public class AutoLoginCheckoutJS extends PGPBaseTest{
    private static final String COOKIE_NAME = "pg_login";

    private boolean validate_MerchantPreference(String mid) {
        return PGPHelpers.validate_MerchantPreference(mid, "PG_AUTOLOGIN_ENABLED", "Y");
    }

    @Owner("SRINIVAS")
    @Feature("PGP-35755")
    @Parameters({"theme"})
    @Test(description = "Validate user should not be autologin when the remember me box is unchecked checkoutjs page")
    public void autoLoginforcheckoutjsremembermeboxisunchecked(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        validate_MerchantPreference(Constants.MerchantType.PGOnly.getId());
        DriverManager.getDriver().manage().deleteCookieNamed(COOKIE_NAME);
        User user=userManager.getForRead(PGPBaseTest.Label.AUTOLOGIN);

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.PGOnly)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutPage checkoutPage = new CheckoutJsCheckoutPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.loginStrip().click();
        cashierPage.rememberMeCheckbox().click();
        cashierPage.signin(user.mobNo(),user.mobNo());
        cashierPage.payBy(Constants.PayMode.DC);

        ResponsePage responsePage=new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
        .validateBankName("HDFC")
        .validateCurrency("INR")
        .validatePaymentMode("DC")
        .validateStatus("TXN_SUCCESS")
        .assertAll();

        InitTxnDTO initTxnDTO1=new InitTxnDTO.Builder(null,Constants.MerchantType.PGOnly).build();
        String txnToken1 = NativeHelpers.Validate_InitTxn(initTxnDTO1);
        CheckoutJsCheckoutPage checkoutPage1 = new CheckoutJsCheckoutPage();
        MerchantConfig config1 = checkoutPage1.loadMerchantConfig(initTxnDTO1, theme);
        config1.data.setToken(txnToken1);
        checkoutPage1.createCheckoutJsOrder(config1);
        CashierPage cashierPage1 = CashierPageFactory.getCashierPage(theme);
        cashierPage1.waitUntilLoads();
        cashierPage1.loginStrip().assertVisible();
    }

    @Owner("SRINIVAS")
    @Feature("PGP-35755")
    @Parameters({"theme"})
    @Test(description = "Validate user should not be autologin when the cookie is deleted on checkoutjs")
    public void autoLoginaftercookiecleared(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        validate_MerchantPreference(Constants.MerchantType.PGOnly.getId());
        DriverManager.getDriver().manage().deleteCookieNamed(COOKIE_NAME);
        User user=userManager.getForRead(PGPBaseTest.Label.AUTOLOGIN);

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.PGOnly)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutPage checkoutPage = new CheckoutJsCheckoutPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.loginStrip().click();
        cashierPage.signin(user.mobNo(),user.mobNo());
        cashierPage.payBy(Constants.PayMode.DC);

        ResponsePage responsePage=new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
        .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
        .validateBankName("HDFC")
        .validatePaymentMode("DC")
        .validateStatus("TXN_SUCCESS")
        .assertAll();
        DriverManager.getDriver().manage().deleteCookieNamed(COOKIE_NAME);

        cashierPage.waitUntilLoads();
        InitTxnDTO initTxnDTO1=new InitTxnDTO.Builder(null,Constants.MerchantType.PGOnly).build();
        String txnToken1 = NativeHelpers.Validate_InitTxn(initTxnDTO1);
        CheckoutJsCheckoutPage checkoutPage1 = new CheckoutJsCheckoutPage();
        MerchantConfig config1 = checkoutPage1.loadMerchantConfig(initTxnDTO1, theme);
        config1.data.setToken(txnToken1);
        checkoutPage1.createCheckoutJsOrder(config1);
        CashierPage cashierPage1 = CashierPageFactory.getCashierPage(theme);
        cashierPage1.waitUntilLoads();
        cashierPage1.loginStrip().assertVisible();
    }
    @Owner("SRINIVAS")
    @Feature("PGP-35755")
    @Parameters({"theme"})
    @Test(description ="Validate Autologin Feature for checkoutjs page")
    public void autologinforcheckoutjs(@Optional("checkoutjs_web_revamp")String theme) throws Exception
    {
        validate_MerchantPreference(Constants.MerchantType.PGOnly.getId());
        DriverManager.getDriver().manage().deleteCookieNamed(COOKIE_NAME);
        User user=userManager.getForRead(Label.AUTOLOGIN);

        InitTxnDTO initTxnDTO=new InitTxnDTO.Builder(null, Constants.MerchantType.PGOnly).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutPage checkoutPage = new CheckoutJsCheckoutPage();
        MerchantConfig config = checkoutPage.loadMerchantConfigwithoutdeletingcookie(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.loginStrip().click();
        cashierPage.signin(user.mobNo(),user.mobNo());
        cashierPage.waitUntilLoads();
        cashierPage.payBy(Constants.PayMode.NB);

        ResponsePage responsePage=new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
        .validatePaymentMode("NB")
        .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
        .validateStatus("TXN_SUCCESS")
        .assertAll();


        InitTxnDTO initTxnDTO1=new InitTxnDTO.Builder(null,Constants.MerchantType.PGOnly).build();
        String txnToken1=NativeHelpers.Validate_InitTxn(initTxnDTO1);
        MerchantConfig config1=checkoutPage.loadMerchantConfigwithoutdeletingcookie(initTxnDTO1,theme);
        config1.data.setToken(txnToken1);
        checkoutPage.createCheckoutJsOrder(config1);
        CashierPage cashierPage1=CashierPageFactory.getCashierPage(theme);
        cashierPage1.waitUntilLoads();
        cashierPage1.loginStrip().assertNotVisible();
    }

    @Owner(Constants.Owner.PUSPA)
    @Feature("PGP-41941")
    @Parameters({"theme"})
    @Test(description = "Validate If user logged in via Cookies then postpaid should show above wallet")
    public void verifyUserLoggedInViaCokkiePostpaidaboveWallet_checkoutjs(@Optional("checkoutjs_web_revamp")String theme) throws Exception
    {
        validate_MerchantPreference(Constants.MerchantType.AUTOLOGIN_MID.getId());
        DriverManager.getDriver().manage().deleteCookieNamed(COOKIE_NAME);
        User user=userManager.getForRead(Label.POSTPAID);

        InitTxnDTO initTxnDTO=new InitTxnDTO.Builder(null, Constants.MerchantType.AUTOLOGIN_MID).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutPage checkoutPage = new CheckoutJsCheckoutPage();
        MerchantConfig config = checkoutPage.loadMerchantConfigwithoutdeletingcookie(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.loginStrip().click();
        cashierPage.signin(user.mobNo(),user.mobNo());
        cashierPage.payBy(Constants.PayMode.CC);

        ResponsePage responsePage=new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validatePaymentMode("CC")
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateStatus("TXN_SUCCESS")
                .assertAll();

        checkoutPage.setDeleteCookie(false);
        InitTxnDTO initTxnDTO1=new InitTxnDTO.Builder(null,Constants.MerchantType.AUTOLOGIN_MID).build();
        String txnToken1=NativeHelpers.Validate_InitTxn(initTxnDTO1);
        MerchantConfig config1=checkoutPage.loadMerchantConfigwithoutdeletingcookie(initTxnDTO1,theme);
        config1.data.setToken(txnToken1);
        checkoutPage.createCheckoutJsOrder(config1);
        CashierPage cashierPage1=CashierPageFactory.getCashierPage(theme);
        cashierPage1.waitUntilLoads();
        cashierPage1.radioButtonPaytmPostpaid().isSelected();

    }

}
