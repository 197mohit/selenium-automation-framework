package scripts.Native.checkoutjs;

import com.paytm.api.TxnStatus;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.SimplifiedPaymentOffers;
import com.paytm.dto.checkoutjs.MerchantConfig;
import com.paytm.framework.ui.element.UIElement;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutJsCheckoutPage;
import com.paytm.pages.ResponsePage;
import com.paytm.utils.merchant.merchant.util.Merchant;
import com.paytm.utils.merchant.merchant.util.Promo;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.fest.assertions.api.Assertions;
import org.openqa.selenium.By;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Date;

@Owner("Jai")
public class CheckoutJsUPIIntent extends PGPBaseTest {
    private final CheckoutJsCheckoutPage checkoutPage = new CheckoutJsCheckoutPage();

    @Parameters({"theme"})
    @Test(description = "Verfiy successfull UPI Collect txn using Checkout js flow with UPI Intent Enabled for checkoutjs_wap only")
    public void ValidateSuccessTxnUsingUPICollectwithIntentEnabled(@Optional("checkoutjs_wap_revamp") String theme) throws IOException {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.UPI_INTENT)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.PPBLC.toString())
                .validateCheckSum(Constants.MerchantType.UPI_INTENT.getKey())
                .validateResponsePageParameters()
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.PPBLC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("UPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Verify UI in Checkout js flow with UPI Intent Enabled for checkoutjs_wap only")
    public void ValidateUPIPaymodeUIchangeswithUPIIntentEnabled(@Optional("checkoutjs_wap_revamp") String theme) throws IOException {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.UPI_INTENT)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabUPI().waitUntilClickable();
        cashierPage.tabUPI().click();
        Assertions.assertThat(cashierPage.tabUPIIntent().isElementPresent()); //Verify UPI app option is selected by default
        Assertions.assertThat(cashierPage.upiIntentTabTick().isSelected());
        cashierPage.tabUPICollect().click();
        Assertions.assertThat(cashierPage.tabUPICollect().isElementPresent()); //Verify UPI ID option is selected
        cashierPage.upiCollectTabTick();
//        UIElement phoneIcon = new UIElement(By.xpath("//div[text()='Pay using a UPI App ']/parent::div/div/img"), cashierPage.getPageName(), "phoneIcon");
//        phoneIcon.assertVisible();
//        UIElement atUPIIcon = new UIElement(By.xpath("//div[text()='Pay using your UPI ID']/parent::div/div/img"), cashierPage.getPageName(), "@Icon");
//        atUPIIcon.assertVisible();
        UIElement knowMoreOption = new UIElement(By.xpath("//a[contains(text(), 'Know more')]"), cashierPage.getPageName(), "know-more-option");
        knowMoreOption.assertVisible();

    }

    @Parameters({"theme"})
    @Test(description = "Verify UPI Collect paymode is not visible in Checkout js flow with only UPI Intent Enabled in merchant for checkoutjs_wap only")
    public void ValidateUPICollectpaymodeNotVisiblewithIntentOnlyEnabled(@Optional("checkoutjs_wap") String theme) throws IOException {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.UPI_INTENTONLY)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabUPI().waitUntilClickable();
        cashierPage.tabUPI().click();
        Assertions.assertThat(cashierPage.tabUPIIntent().isSelected()).isEqualTo(true); //Verify UPI app option is selected by default
        if(theme.equalsIgnoreCase(Constants.Theme.CHECKOUTJS_WAP))
        { cashierPage.upiIntentTabTick().assertVisible();}
        cashierPage.tabUPICollect().assertNotVisible();
    }

    @Parameters({"theme"})
    @Test(description = "Verify Bank offers are visible on cashier page(without SSO token) and not visible after user login")
    public void PGP_27157_ValidateBankOffersNotVisiblewithPWPMerchantfterLoggingIn(@Optional("checkoutjs_wap") String theme) throws Exception {
        User user = userManager.getForRead(Label.LOGIN);
        Constants.MerchantType pwpDefault = Constants.MerchantType.PWP_DEFAULT;
        for (int i = 0; i < 2; i++) {
            Promo promo = new Promo();
            new Merchant(pwpDefault.getId(), true).getPromos().add(promo);
        }
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode("").setApplyAvailablePromo("true").setValidatePromo("true");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", pwpDefault, simplifiedPaymentOffers)
                .setTxnValue("10.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.promoOffersList().assertVisible();
        cashierPage.login(user);
        cashierPage.promoOffersList().assertNotVisible();
    }

    @Owner(Constants.Owner.JAI)
    @Parameters({"theme"})
    @Test(description = "Verfiy UPI Apps Paymode visible when UpiAppsPayModeEnabled field is enabled")
    public void PGP_28418_VerifyUPIAPPPaymodeStatusWhenFieldupiAppsPayModeEnabledisTrue(@Optional("checkoutjs_wap") String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.UPI_INTENT)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        fetchPaymentOptionsDTO.getHead().setWorkFlow("checkout");
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        Assertions.assertThat(fetchPaymentOptionsJson.getBoolean("body.upiAppsPayModeEnabled")).isTrue();
        cashierPage.tabUPI().waitUntilClickable();
        cashierPage.tabUPI().click();
        cashierPage.tabUPIIntent().click();
        cashierPage.tabUPIIntent().assertVisible();
    }

    @Owner(Constants.Owner.JAI)
    @Parameters({"theme"})
    @Test(description = "Verfiy UPI Apps Paymode not visible when UpiAppsPayModeEnabled field is disabled")
    public void PGP_28418_VerifyUPIAPPPaymodeStatusWhenFieldupiAppsPayModeEnabledisFalse(@Optional("checkoutjs_wap_revamp") String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.HDFC_UPI_COLLECT_RETRY)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        fetchPaymentOptionsDTO.getHead().setWorkFlow("checkout");
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        Assertions.assertThat(fetchPaymentOptionsJson.getBoolean("body.upiAppsPayModeEnabled")).isFalse();
        cashierPage.tabUPI().waitUntilClickable();
        cashierPage.tabUPI().click();
        cashierPage.tabUPIIntent().assertNotVisible();
    }

}
