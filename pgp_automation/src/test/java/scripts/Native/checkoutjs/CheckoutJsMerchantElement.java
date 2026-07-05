package scripts.Native.checkoutjs;

import com.paytm.LocalConfig;
import com.paytm.ServerConfigProvider;
import com.paytm.api.StaticQrUpiPSP;
import com.paytm.api.theia.FetchQRPaymentDetails;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.*;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchQRPaymentDetailsDTO.FetchQRPaymentDetailsDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.SimplifiedPaymentOffers;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.checkoutjs.MerchantConfig;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.dto.upiIntent.staticQR.Response.StaticQrUpiPSPResponse;
import com.paytm.dto.upiIntent.staticQR.StaticQrUpiPSPRequest;
import com.paytm.framework.ui.element.UIElement;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutJsCheckoutMerchantElementPage;
import com.paytm.pages.ResponsePage;
import com.paytm.utils.merchant.merchant.util.Merchant;
import com.paytm.utils.merchant.merchant.util.Promo;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.openqa.selenium.By;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import com.paytm.framework.core.DriverManager;
import com.paytm.dto.NativeDTO.InitTxn.SimplifiedSubvention;

import java.util.List;
import java.util.ArrayList;

import java.util.Date;

import static com.paytm.appconstants.Constants.MerchantType.EmiInfo_COP;
import static com.paytm.appconstants.Constants.Owner.ROHIT_SHARMA;
import static com.paytm.apphelpers.LogsValidationHelper.getLogsOnServer;


/**
 * TODO: rahul gulati to check for new UI revamp for elemental case.
 */
@Owner(Constants.Owner.ROHIT)
public class CheckoutJsMerchantElement extends PGPBaseTest {
    CheckoutJsCheckoutMerchantElementPage elementCheckoutPage = new CheckoutJsCheckoutMerchantElementPage();


    private final static int INSTANT_DISCOUNT_PERCENTAGE = 5;

    @Parameters({"theme"})
    @Test(description = "verify if card tab is visible and BodyBackgroundColor = #fafafb")
    public void integrateCard(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly_Retry;
        User user = userManager.getForRead(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("2.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        config.style.setBodyBackgroundColor("#fafafb");
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.createAndInvokePaymode("CC");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        String bodyBck_color = getColorCode(getElementByXpath("//*[contains(@class, 'body-bg')]", theme).getCssValue("background-color"));
        Assertions.assertThat(bodyBck_color).isEqualToIgnoringCase("#fafafb");
        cashierPage.tabCreditCard().isDisplayed();
    }

    @Parameters({"theme"})
    @Test(description = "verify if EMI tab is visible and BodyColor = #3399ff")
    public void integrateEMI(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly_Retry;
        User user = userManager.getForRead(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("2.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        config.style.setBodyColor("#3399ff");
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.createAndInvokePaymode("EMI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        String bodyColor = getColorCode(getElementByXpath("//*[contains(@class, 'ptm-select-txt')]", theme).getCssValue("color"));
        Assertions.assertThat(bodyColor).isEqualToIgnoringCase("#3399ff");
        cashierPage.tabEMI().isDisplayed();
    }


    @Parameters({"theme"})
    @Test(description = "verify if NB tab is visible and ThemeBackgroundColor = #3399ff")
    public void integrateNB(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly_Retry;
        User user = userManager.getForRead(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("2.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        config.style.setThemeBackgroundColor("#3399ff");
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.createAndInvokePaymode("NB");
        String themeBck_color = getColorCode(getElementByXpath("//*[contains(@class, 'ptm-custom-btn')]", theme).getCssValue("background-color"));
        Assertions.assertThat(themeBck_color).isEqualToIgnoringCase("#3399ff");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabNetBanking().isDisplayed();
    }

    @Parameters({"theme"})
    @Test(description = "verify if UPI tab is visible and ThemeColor = #3399ff")
    public void integrateUPI(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly_Retry;
        User user = userManager.getForRead(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("2.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        config.style.setThemeColor("#3399ff");
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.createAndInvokePaymode("UPI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        String themeColor = getColorCode(getElementByXpath("//*[contains(@class, 'ptm-custom-btn')]", theme).getCssValue("color"));
        Assertions.assertThat(themeColor).isEqualToIgnoringCase("#3399ff");
    }


    @Parameters({"theme"})
    @Test(description = "making transaction with new card while logged in using sso token")
    public void loginStateNC(@Optional("checkoutjse_web") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly;
        User user = userManager.getForRead(Label.BASIC);
        PaymentDTO paymentDTO = new PaymentDTO();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("2.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.createAndInvokePaymode("CC");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateCheckSum(merchantType.getKey())
                .assertAll();

    }

    @Parameters({"theme"})
    @Test(description = "making transaction with emi using new card while logged in using sso token")
    public void loginStateEMI_NC(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly;
        User user = userManager.getForRead(Label.BASIC);
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        paymentDTO.setBankName("HDFC BANK");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("2.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.createAndInvokePaymode("EMI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabEMI().isDisplayed();
        cashierPage.payBy(Constants.PayMode.EMI, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateCheckSum(merchantType.getKey())
                .assertAll();

    }


    @Parameters({"theme"})
    @Test(description = "making transaction with NB while logged in using sso token")
    public void loginstateNB(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly;
        User user = userManager.getForRead(Label.BASIC);
        PaymentDTO paymentDTO = new PaymentDTO();

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("2.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.createAndInvokePaymode("NB");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.NB, paymentDTO.setBankName("ICICI"));
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("NB")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.ICICI.toString())
                .validateCheckSum(merchantType.getKey())
                .assertAll();

    }

//     @Parameters({"theme"})
//     @Test(description = "making transaction with saved upi while logged in using sso token",enabled = false)
    public void loginstateSavedVPA(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.UPI_INTENT;
        User user = userManager.getForWrite(Label.SAVEDVPA);

        PaymentDTO paymentDTO = new PaymentDTO();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("2.00")
                .build();
        WalletHelpers.setZeroBalance(user);
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.createAndInvokePaymode("PAY WITH PAYTM");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.SAVED_UPI);
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
                .validateCheckSum(merchantType.getKey())
                .assertAll();

    }


//     @Parameters({"theme"})
//     @Test(description = "making transaction with paytm payments bank while logged in using sso token",enabled=false)
    public void loginstatePPBL(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly;
        User user = userManager.getForRead(Label.PPBL);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("2.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.createAndInvokePaymode("PAY WITH PAYTM");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.PPBL);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("NB")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCheckSum(merchantType.getKey())
                .assertAll();

    }

    // Deprecated since there's no logout button in elementJS
//    @Parameters({"theme"})
//    @Test(enabled = false, description = "logout and login again using different user and pay using wallet")
    public void loginstateDiffUserWallet(@Optional("checkoutjse_web_revamp") String theme) throws Exception {//error
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly;
        User user = userManager.getForRead(Label.PPBL);
        PaymentDTO paymentDTO = new PaymentDTO();

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("2.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        CheckoutJsMerchantElement.getMerchants();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.createAndInvokePaymode("PAY WITH PAYTM");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.logout(user);
        User user1 = userManager.getForWrite(Label.LOGIN);
        WalletHelpers.modifyBalance(user1, Double.parseDouble("2.00"));
        cashierPage.login(user1);
        InitTxnDTO initTxnDTO1 = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("2.00")
                .build();
        checkoutPage.createAndInvokePaymode("PAY WITH PAYTM");
        cashierPage.payBy(Constants.PayMode.WALLET);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("PPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCheckSum(merchantType.getKey())
                .assertAll();

    }


    @Parameters({"theme"})
    @Test(description = "making upi transaction  while logged in using sso token")
    public void loginstateUPI(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PG2_UPI;
        User user = userManager.getForRead(Label.PPBL);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("2.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.createAndInvokePaymode("UPI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
      //  cashierPage.tabUPIId().isDisplayed();
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
                .validateCheckSum(merchantType.getKey())
                .assertAll();

    }

    @Parameters({"theme"})
    @Test(description = "scan generalise qr and pay using wallet")
    public void loginstateSCANnPAY(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.UPI_INTENT;
        User user = userManager.getForWrite(Label.LOGIN);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("2.00")
                .build();
        WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()));
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config1 = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config1.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config1);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        checkoutPage.createAndInvokePaymode("SCAN AND PAY");
        cashierPage.imgScanPayQRCode().waitUntilVisible();
        String qrCodeID = PGPHelpers.getWalletQRCodeString(cashierPage.imgScanPayQRCode().getAttribute("src"));
        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeID)
                .setMID(merchantType.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();
        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        JsonPath fetchQRResponse = fetchQRPaymentDetails.execute().jsonPath();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                merchantType.getId(), "SSO", user.ssoToken(), fetchQRResponse.getString("body.qrInfo.response.ORDER_ID"), fetchQRResponse.getString("body.qrInfo.response.TXN_AMOUNT"))
                .setPaymentMode("PPI")
                .setQRCodeId(qrCodeID)
                .setExtendInfoDynamicFlow()
                .build();

        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("PPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("WALLET")
                .validateBankName("WALLET")
                .assertAll();
    }

    @Parameters({"theme"})
    @Test(description = "retry transaction using cc when user is not logged in")
    public void logoutccretry(@Optional("checkoutjse_web") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly_Retry;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType).setTxnValue("33.33")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.createAndInvokePaymode("CC");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabCreditCard().isDisplayed();
        cashierPage.payBy(Constants.PayMode.CC);
        cashierPage.closeChildWindow();
        cashierPage.retryBtnPopupClosedByUser().click();
        cashierPage.payBy(Constants.PayMode.CC);
        cashierPage.successfulTransactionButton();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();

    }

    // Saved cards are available only for logged in user.
//    @Parameters({"theme"})
//    @Test(enabled = false, description = "making transaction using saved card when user is not logged in")
    public void logotSC(@Optional("checkoutjse_web") String theme) throws Exception {
//        SavedCardHelpers.enableAllSavedCardFlags();
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly;
        PaymentDTO paymentDTO = new PaymentDTO();

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setTxnValue("2.00")
                .build();
//        SavedCardHelpers.deleteSavedCardsAlipay(merchantType.getId(),initTxnDTO.getBody().getUserInfo().getCustId());
//        SavedCardHelpers.addCardAlipay(merchantType.getId(),initTxnDTO.getBody().getUserInfo().getCustId(), paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        new SavedCardHelpers()
                .saveCard_custId_mId(paymentDTO.getCreditCardNumber(),
                        initTxnDTO.getBody().getUserInfo().getCustId(),
                        merchantType.getId(),
                        paymentDTO.getExpMonth() + paymentDTO.getExpYear());


        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.createAndInvokePaymode("SAVED_CARD");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        //cashierPage.login(user);
        cashierPage.payBy(Constants.PayMode.SAVED_CARD);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateCheckSum(merchantType.getKey())
                .assertAll();

    }

    // Saved cards are available only for logged in user.
//    @Parameters({"theme"})
//    @Test(enabled = false, description = "making emi transaction using saved card when user is not logged in")
    public void logotEMI_SC(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
//        SavedCardHelpers.enableAllSavedCardFlags();
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly;
        User user = userManager.getForRead(Label.BASIC);
        PaymentDTO paymentDTO = new PaymentDTO();

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setTxnValue("2.00")
                .build();
//        SavedCardHelpers.deleteSavedCardsAlipay(merchantType.getId(),initTxnDTO.getBody().getUserInfo().getCustId());
//        SavedCardHelpers.addCardAlipay(merchantType.getId(),initTxnDTO.getBody().getUserInfo().getCustId(), paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        paymentDTO.getBankName();
        new SavedCardHelpers()
                .saveCard_custId_mId(paymentDTO.getCreditCardNumber(),
                        initTxnDTO.getBody().getUserInfo().getCustId(),
                        merchantType.getId(),
                        paymentDTO.getExpMonth() + paymentDTO.getExpYear());
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.createAndInvokePaymode("EMI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        cashierPage.payBy(Constants.PayMode.EMI_SAVED_CARD, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateCheckSum(merchantType.getKey())
                .assertAll();

    }


    @Parameters({"theme"})
    @Test(description = "making transaction using NB when user is not logged in")
    public void LogoutNB(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly_Retry;
        User user = userManager.getForRead(Label.PPBL);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setTxnValue("2.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.createAndInvokePaymode("NB");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.NB);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("NB")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCheckSum(merchantType.getKey())
                .assertAll();

    }

    @Parameters({"theme"})
    @Test(description = "making transaction using UPI when user is not logged in")
    public void LogoutUPI(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly_Retry;
        User user = userManager.getForRead(Label.PPBL);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setTxnValue("2.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.createAndInvokePaymode("UPI");
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
                .validateCheckSum(merchantType.getKey())
                .assertAll();

    }

    @Parameters({"theme"})
    @Test(description = "scan generalise qr and pay using wallet")
    public void logoutstateSCANnPAYG(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.UPI_INTENT;
        User user = userManager.getForWrite(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setTxnValue("2.00")
                .build();
        WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()));
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config1 = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config1.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config1);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        checkoutPage.createAndInvokePaymode("SCAN AND PAY");
        cashierPage.waitUntilLoads();
        cashierPage.imgScanPayQRCode().waitUntilVisible();
        String qrCodeID = PGPHelpers.getWalletQRCodeString(cashierPage.imgScanPayQRCode().getAttribute("src"));
        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeID)
                .setMID(merchantType.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();
        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        JsonPath fetchQRResponse = fetchQRPaymentDetails.execute().jsonPath();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                merchantType.getId(), "SSO", user.ssoToken(), fetchQRResponse.getString("body.qrInfo.response.ORDER_ID"), fetchQRResponse.getString("body.qrInfo.response.TXN_AMOUNT"))
                .setPaymentMode("PPI")
                .setQRCodeId(qrCodeID)
                .setExtendInfoDynamicFlow()
                .build();

        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("PPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("WALLET")
                .validateBankName("WALLET")
                .assertAll();
    }

    @Parameters({"theme"})
    @Test(description = "scan Paytm only qr and pay using wallet while logged out")
    public void logoutstateSCANnPAY(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.QR_ENABLED_MERCHANT;
        User user = userManager.getForWrite(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setTxnValue("2.00")
                .build();
        WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()));
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config1 = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config1.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config1);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        checkoutPage.createAndInvokePaymode("SCAN AND PAY");
        cashierPage.waitUntilLoads();
        cashierPage.imgScanPayQRCode().waitUntilVisible();
        String qrCodeID = PGPHelpers.getWalletQRCodeString(cashierPage.imgScanPayQRCode().getAttribute("src"));
        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeID)
                .setMID(merchantType.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();
        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        JsonPath fetchQRResponse = fetchQRPaymentDetails.execute().jsonPath();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                merchantType.getId(), "SSO", user.ssoToken(), fetchQRResponse.getString("body.qrInfo.response.ORDER_ID"), fetchQRResponse.getString("body.qrInfo.response.TXN_AMOUNT"))
                .setPaymentMode("PPI")
                .setQRCodeId(qrCodeID)
                .setExtendInfoDynamicFlow()
                .build();

        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("PPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("WALLET")
                .validateBankName("WALLET")
                .assertAll();
    }


    @Parameters({"theme"})
    @Test(description = "validate failure while scaning Paytm only QR code by 3rd party app")
    public void logoutstate3rdpartyScannPay(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.QR_ENABLED_MERCHANT;
        User user = userManager.getForRead(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("2.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.createAndInvokePaymode("SCAN AND PAY");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.pause(2);
        cashierPage.scrollToElement(cashierPage.imgScanPayQRCode());
        cashierPage.imgScanPayQRCode().waitUntilVisible();
        cashierPage.imgScanPayQRCode().isDisplayed();
        String decodedQRTxt = PGPHelpers.getWalletQRCodeString(cashierPage.imgScanPayQRCode().getAttribute("src"));
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = null;

        upiPSP:
        {
            StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest.Builder(merchant.getId(), "2.0")
                    .setOrderId(initTxnDTO.orderFromBody())
                    .build();
            StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
            Response response = staticQrUpiPSP.execute();
            staticQrUpiPSPResponse = response.as(StaticQrUpiPSPResponse.class);
            Assertions.assertThat(staticQrUpiPSPResponse.getBody().getResultCode())
                    .as("Result code mismatch")
                    .isEqualToIgnoringCase("FAIL");
        }

    }

    @Parameters({"theme"})
    @Test(description = "login using otp and complete txn using paytm postpaid")
    public void logoutstateloginPostpaid(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly;
        User user = userManager.getForWrite(Label.PG2POSTPAIDUSER);
        PaymentDTO paymentDTO = new PaymentDTO();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setTxnValue("2.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        PostpaidHelpers.updateBalance(initTxnDTO.txnAmountFromBody());
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.createAndInvokePaymode("PAY WITH PAYTM");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.login(user);
        checkoutPage.createAndInvokePaymode("PAY WITH PAYTM");
        cashierPage.payBy(Constants.PayMode.PAYTM_DIGITAL_CARD);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("Paytm Postpaid")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCheckSum(merchantType.getKey())
                .assertAll();

    }


    @Parameters({"theme"})
    @Test(description = "apply promocode and verify card tab is visible")
    public void promocodelogincc(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly_Retry;
        User user = userManager.getForRead(Label.BASIC);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.MAESTRO_DEBIT_CARD_NUMBER);
        Promo promo = null;
        for (int i = 0; i < 2; i++) {
            promo = new Promo();
            new Merchant(merchantType.getId(), true).getPromos().add(promo);
        }
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode(promo.getName()).setApplyAvailablePromo("true").setValidatePromo("true");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType, simplifiedPaymentOffers)
                .setTxnValue("2.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.pause(3);
        checkoutPage.createAndInvokePaymode("CC");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabCreditCard().isDisplayed();
        cashierPage.payBy(Constants.PayMode.CC);
        Double discountedAmt = Double.parseDouble(initTxnDTO.txnAmountFromBody()) * (1.00 - INSTANT_DISCOUNT_PERCENTAGE * 0.01);
        String DA = CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(String.valueOf(discountedAmt));
        String discount = CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(String.valueOf(Double.parseDouble(initTxnDTO.txnAmountFromBody()) - discountedAmt));
        String buttonText = new UIElement(By.xpath("//div[@id='checkout-button']/button/span"), "cashier-page", "pay-button").getText();
//        Assertions.assertThat(buttonText).isEqualTo("(Effective price: ₹ " + DA + " with ₹ " + discount + " cashback)");
        Assertions.assertThat(buttonText).isEqualTo("Effective price after offer ₹" + DA);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCheckSum(merchantType.getKey())
                .assertAll();
    }

    @Parameters({"theme"})
    @Test(description = "apply promocode and pay using emi")
    public void promocodeloginEMI(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly;
        User user = userManager.getForRead(Label.BASIC);
        PaymentDTO paymentDTO = new PaymentDTO();
        Promo promo = null;
        for (int i = 0; i < 2; i++) {
            promo = new Promo();
            new Merchant(merchantType.getId(), true).getPromos().add(promo);
        }
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode(promo.getName()).setApplyAvailablePromo("true").setValidatePromo("true");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType, simplifiedPaymentOffers)
                .setTxnValue("2.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.pause(3);
        checkoutPage.createAndInvokePaymode("EMI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabEMI().isDisplayed();
        cashierPage.payBy(Constants.PayMode.EMI);
        Double discountedAmt = Double.parseDouble(initTxnDTO.txnAmountFromBody()) * (1.00 - INSTANT_DISCOUNT_PERCENTAGE * 0.01);
        String DA = CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(String.valueOf(discountedAmt));
        String discount = CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(String.valueOf(Double.parseDouble(initTxnDTO.txnAmountFromBody()) - discountedAmt));
        String buttonText = new UIElement(By.xpath("//div[@id='checkout-button']/button/span"), "cashier-page", "pay-button").getText();
        Assertions.assertThat(buttonText).isEqualTo("Effective price after offer ₹" + DA);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCheckSum(merchantType.getKey())
                .assertAll();
    }

    @Parameters({"theme"})
    @Test(description = "apply promocode and pay using maestro card")
    public void promocodeloginMaestro(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly_Retry;
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.setZeroBalance(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.MAESTRO_DEBIT_CARD_NUMBER);
        Promo promo = null;
        for (int i = 0; i < 2; i++) {
            promo = new Promo();
            new Merchant(merchantType.getId(), true).getPromos().add(promo);
        }
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode(promo.getName()).setApplyAvailablePromo("true").setValidatePromo("true");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType, simplifiedPaymentOffers)
                .setTxnValue("2.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.createAndInvokePaymode("DC");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        Double discountedAmt = Double.parseDouble(initTxnDTO.txnAmountFromBody()) * (1.00 - INSTANT_DISCOUNT_PERCENTAGE * 0.01);
        String DA = CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(String.valueOf(discountedAmt));
        String discount = CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(String.valueOf(Double.parseDouble(initTxnDTO.txnAmountFromBody()) - discountedAmt));
        String buttonText = new UIElement(By.xpath("//div[@id='checkout-button']/button/span"), "cashier-page", "pay-button").getText();
//        Assertions.assertThat(buttonText).isEqualTo("(Effective price: ₹ " + DA + " with ₹ " + discount + " cashback)");
        Assertions.assertThat(buttonText).isEqualTo("Effective price after offer ₹" + DA);
        cashierPage.savedCardPayNow().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("DC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCheckSum(merchantType.getKey())
                .assertAll();
    }


    @Parameters({"theme"})
    @Test(description = "apply promocode annd pay using nb while user is not logged in")
    public void promocodelogoutNB(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PPBL_NB_PCF;
        User user = userManager.getForRead(Label.BASIC);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setBankName("ICICI");
        Promo promo = null;
        for (int i = 0; i < 2; i++) {
            promo = new Promo();
            new Merchant(merchantType.getId(), true).getPromos().add(promo);
        }
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode(promo.getName()).setApplyAvailablePromo("true").setValidatePromo("true");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", merchantType, simplifiedPaymentOffers)
                .setTxnValue("2.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.createAndInvokePaymode("NB");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabNetBanking().isDisplayed();
        cashierPage.payBy(Constants.PayMode.NB, paymentDTO);
        Double discountedAmt = Double.parseDouble(initTxnDTO.txnAmountFromBody()) * (1.00 - INSTANT_DISCOUNT_PERCENTAGE * 0.01);
        String DA = CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(String.valueOf(discountedAmt));
        String discount = CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(String.valueOf(Double.parseDouble(initTxnDTO.txnAmountFromBody()) - discountedAmt));
        String buttonText = new UIElement(By.xpath("//div[@id='checkout-button']/button/span"), "cashier-page", "pay-button").getText();
        Assertions.assertThat(buttonText).isEqualTo("Effective price after offer ₹" + DA);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("NB")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCheckSum(merchantType.getKey())
                .assertAll();
    }

    @Parameters({"theme"})
    @Test(description = "validate pay with paytm page in logout state")
    public void promocodelogoutEMI(@Optional("checkoutjse_web") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly;
        User user = userManager.getForRead(Label.BASIC);
        PaymentDTO paymentDTO = new PaymentDTO();
        Promo promo = null;
        for (int i = 0; i < 2; i++) {
            promo = new Promo();
            new Merchant(merchantType.getId(), true).getPromos().add(promo);
        }
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode(promo.getName()).setApplyAvailablePromo("true").setValidatePromo("true");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType, simplifiedPaymentOffers)
                .setTxnValue("2.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.createAndInvokePaymode("EMI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabEMI().isDisplayed();
        cashierPage.payBy(Constants.PayMode.EMI);
        Double discountedAmt = Double.parseDouble(initTxnDTO.txnAmountFromBody()) * (1.00 - INSTANT_DISCOUNT_PERCENTAGE * 0.01);
        String DA = CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(String.valueOf(discountedAmt));
        String discount = CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(String.valueOf(Double.parseDouble(initTxnDTO.txnAmountFromBody()) - discountedAmt));
        String buttonText = new UIElement(By.xpath("//div[@id='checkout-button']/button/span"), "cashier-page", "pay-button").getText();
//        Assertions.assertThat(buttonText).isEqualTo("(Effective price: ₹ " + DA + " with ₹ " + discount + " cashback)");
        Assertions.assertThat(buttonText).isEqualTo("Effective price after offer ₹" + DA);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCheckSum(merchantType.getKey())
                .assertAll();
    }


    @Parameters({"theme"})
    @Test(description = "make add n pay transaction using NB")
    public void addnpayNB(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.AddnPay;
        User user = userManager.getForWrite(Label.BASIC);
        PaymentDTO paymentDTO = new PaymentDTO();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("2.00")
                .build();
        WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()) - 1);
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.pause(3);
        checkoutPage.createAndInvokePaymode("PAY WITH PAYTM");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(Constants.PayMode.NB);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("PPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCheckSum(merchantType.getKey())
                .assertAll();

    }

    @Parameters({"theme"})
    @Test(description = "make add and pay transaction using cc")
    public void addandpayCC(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.AddnPay;
        User user = userManager.getForWrite(Label.LOGIN);

        PaymentDTO paymentDTO = new PaymentDTO();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setTxnValue("2.00")
                .build();
        WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()) - 1);
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.createAndInvokePaymode("PAY WITH PAYTM");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.login(user);
        checkoutPage.createAndInvokePaymode("PAY WITH PAYTM");
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("PPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCheckSum(merchantType.getKey())
                .assertAll();
    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-37696")
    @Parameters({"theme"})
    @Test(description = "Validate categorization for payment flow:None")
    public void categorize(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.AddnPay;
        User user = userManager.getForRead(Label.BASIC);
        // SavedCardHelpers.deleteSavedCard(user);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("2.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.createAndInvokePaymode("PAY WITH PAYTM");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        String d1= cashierPage.getPaytmFeaturedText().getText();
        Assertions.assertThat(d1).isEqualTo("Paytm Featured");
    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-37696")
    @Parameters({"theme"})
    @Test(description = "Validate categorization for payment flow:addNpay")
    public void addnpaycategarize(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.AddnPay;
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("2.00")
                .build();
        WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()) - 1);
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.createAndInvokePaymode("PAY WITH PAYTM");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        String d1= cashierPage.getPaytmFeaturedText().getText();
        String d2 = cashierPage.getNewpaymentOption().getText();
        Assertions.assertThat(d1).isEqualTo("Paytm Featured");
        Assertions.assertThat(d2).isEqualTo("New Payment Option");
    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-37696")
    @Parameters({"theme"})
    @Test(description = "Pass hidePaymodeLabel :true in config and Validate Card for Non-logged in flow")
    public void card_nonLocckedIN_hidelable(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly_Retry;
        User user = userManager.getForRead(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setTxnValue("2.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        config.setHidePaymodeLabel(true);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.createAndInvokePaymode("CC");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        String d1= cashierPage.hidepaymodelableTrue().getText();
        Assertions.assertThat(d1).isEqualTo("");

    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-37696")
    @Parameters({"theme"})
    @Test(description = "Pass hidePaymodeLabel :true in config and Validate Card for logged in flow")
    public void card_LocckedIN_hidelable(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly;
        User user = userManager.getForRead(Label.BASIC);
        // SavedCardHelpers.deleteSavedCard(user);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),merchantType)
                .setTxnValue("2.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        config.setHidePaymodeLabel(true);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.createAndInvokePaymode("CC");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        String d1= cashierPage.hidepaymodelableTrue().getText();
        Assertions.assertThat(d1).isEqualTo("");

    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-37696")
    @Parameters({"theme"})
    @Test(description = "Pass hidePaymodeLabel :true in config and Validate UPI for Non-logged in flow")
    public void upi_nonLocckedIN_hidelable(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.EMI;
        User user = userManager.getForRead(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setTxnValue("2.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        config.setHidePaymodeLabel(true);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.createAndInvokePaymode("UPI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        String d1= cashierPage.hidepaymodelableTrue().getText();
        Assertions.assertThat(d1).isEqualTo(
                "Know more\n" +
                "OR\n" +
                "Verify\n" +
                "Pay ₹2");

    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-37696")
    @Parameters({"theme"})
    @Test(description = "Pass hidePaymodeLabel :true in config and Validate UPI for logged in flow")
    public void upi_LocckedIN_hidelable(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly_Retry;
        User user = userManager.getForRead(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),merchantType)
                .setTxnValue("2.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        config.setHidePaymodeLabel(true);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.createAndInvokePaymode("UPI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        String d1= cashierPage.hidepaymodelableTrue().getText();
        Assertions.assertThat(d1).isEqualTo("Know more\n" +
                "Verify VPA\n" +
                "OR\n" +
                "Verify\n" +
                "Pay ₹2");

    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-37696")
    @Parameters({"theme"})
    @Test(description = "Pass hidePaymodeLabel :true in config and Validate NB for Non-logged in flow")
    public void NB_nonLocckedIN_hidelable(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly_Retry;
        User user = userManager.getForRead(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setTxnValue("2.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        config.setHidePaymodeLabel(true);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.createAndInvokePaymode("NB");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        String d1= cashierPage.hidepaymodelableTrue().getText();
        Assertions.assertThat(d1).isEqualTo("ICICI\n" +
                "View All Banks");

    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-37696")
    @Parameters({"theme"})
    @Test(description = "Pass hidePaymodeLabel :true in config and Validate NB for logged in flow")
    public void NB_LocckedIN_hidelable(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly_Retry;
        User user = userManager.getForRead(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),merchantType)
                .setTxnValue("2.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        config.setHidePaymodeLabel(true);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.createAndInvokePaymode("NB");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        String d1= cashierPage.hidepaymodelableTrue().getText();
        Assertions.assertThat(d1).isEqualTo("ICICI\n" +
                "View All Banks");

    }

    // Deprecated as this is not expected after EMI revamp https://jira.mypaytm.com/browse/PGP-37696
//    @Owner(ROHIT_SHARMA)
//    @Feature("PGP-37696")
//    @Parameters({"theme"})
//    @Test(enabled = false, description = "Pass hidePaymodeLabel :true in config and Validate EMI for Non-logged in flow")
    public void EMI_nonLocckedIN_hidelable(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly_Retry;
        User user = userManager.getForRead(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setTxnValue("2.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        config.setHidePaymodeLabel(true);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.createAndInvokePaymode("EMI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        String d1= cashierPage.hidepaymodelableTrue().getText();
        Assertions.assertThat(d1).isEqualTo("Select Bank");

    }

    // Deprecated as this is not expected after EMI revamp https://jira.mypaytm.com/browse/PGP-37696
//    @Owner(ROHIT_SHARMA)
//    @Feature("PGP-37696")
//    @Parameters({"theme"})
//    @Test(enabled = false, description = "Pass hidePaymodeLabel :true in config and Validate EMI for logged in flow")
    public void EMI_LocckedIN_hidelable(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly_Retry;
        User user = userManager.getForRead(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),merchantType)
                .setTxnValue("2.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        config.setHidePaymodeLabel(true);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.createAndInvokePaymode("EMI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        String d1= cashierPage.hidepaymodelableTrue().getText();
        Assertions.assertThat(d1).isEqualTo("Select Bank");

    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-37696")
    @Parameters({"theme"})
    @Test(description = "Pass hidePaymodeLabel :false in config and Validate Card for Non-logged in flow")
    public void card_nonLocckedIN_Fhidelable(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly_Retry;
        User user = userManager.getForRead(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setTxnValue("2.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        config.setHidePaymodeLabel(false);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.createAndInvokePaymode("CC");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        String d1 = cashierPage.hidepaymodelableFalse().getText();
        Assertions.assertThat(d1).isEqualTo("Prepaid, Debit and Credit Cards");

    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-37696")
    @Parameters({"theme"})
    @Test(description = "Pass hidePaymodeLabel :false in config and Validate Card for logged in flow")
    public void card_LocckedIN_Fhidelable(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly;
        User user = userManager.getForRead(Label.BASIC);
        // SavedCardHelpers.deleteSavedCard(user);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),merchantType)
                .setTxnValue("2.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        config.setHidePaymodeLabel(false);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.createAndInvokePaymode("CC");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        String d1 = cashierPage.hidepaymodelableFalse().getText();
        Assertions.assertThat(d1).isEqualTo("Prepaid, Debit and Credit Cards");

    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-37696")
    @Parameters({"theme"})
    @Test(description = "Pass hidePaymodeLabel :false in config and Validate UPI for Non-logged in flow")
    public void upi_nonLocckedIN_Fhidelable(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly_Retry;
        User user = userManager.getForRead(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setTxnValue("2.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        config.setHidePaymodeLabel(false);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.createAndInvokePaymode("UPI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        String d1 = cashierPage.hidepaymodelableFalse().getText();
        Assertions.assertThat(d1).isEqualTo("UPI");

    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-37696")
    @Parameters({"theme"})
    @Test(description = "Pass hidePaymodeLabel :false in config and Validate UPI for logged in flow")
    public void upi_LocckedIN_Fhidelable(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly_Retry;
        User user = userManager.getForRead(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),merchantType)
                .setTxnValue("2.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        config.setHidePaymodeLabel(false);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.createAndInvokePaymode("UPI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        String d1 = cashierPage.hidepaymodelableFalse().getText();
        Assertions.assertThat(d1).isEqualTo("UPI");

    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-37696")
    @Parameters({"theme"})
    @Test(description = "Pass hidePaymodeLabel :false in config and Validate NB for Non-logged in flow")
    public void NB_nonLocckedIN_Fhidelable(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly_Retry;
        User user = userManager.getForRead(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setTxnValue("2.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        config.setHidePaymodeLabel(false);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.createAndInvokePaymode("NB");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        String d1 = cashierPage.hidepaymodelableFalse().getText();
        Assertions.assertThat(d1).isEqualTo("Net Banking");
    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-37696")
    @Parameters({"theme"})
    @Test(description = "Pass hidePaymodeLabel :false in config and Validate NB for logged in flow")
    public void NB_LocckedIN_Fhidelable(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly_Retry;
        User user = userManager.getForRead(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),merchantType)
                .setTxnValue("2.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        config.setHidePaymodeLabel(false);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.createAndInvokePaymode("NB");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        String d1 = cashierPage.hidepaymodelableFalse().getText();
        Assertions.assertThat(d1).isEqualTo("Net Banking");

    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-37696")
    @Parameters({"theme"})
    @Test(description = "Pass hidePaymodeLabel :false in config and Validate EMI for Non-logged in flow")
    public void EMI_nonLocckedIN_Fhidelable(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly_Retry;
        User user = userManager.getForRead(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setTxnValue("2.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        config.setHidePaymodeLabel(false);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.createAndInvokePaymode("EMI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        String d1 = cashierPage.hidepaymodelableFalse().getText();
        Assertions.assertThat(d1).isEqualTo("EMI");

    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-37696")
    @Parameters({"theme"})
    @Test(description = "Pass hidePaymodeLabel :false in config and Validate EMI for logged in flow")
    public void EMI_LocckedIN_Fhidelable(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.EMI;
        User user = userManager.getForRead(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),merchantType)
                .setTxnValue("2.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        config.setHidePaymodeLabel(false);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.pause(3);
        checkoutPage.createAndInvokePaymode("EMI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        String d1 = cashierPage.hidepaymodelableFalse().getText();
        Assertions.assertThat(d1).isEqualTo("EMI");

    }

    @Owner(Constants.Owner.RITIK)
    @Feature("PGP-33611")
    @Parameters({"theme"})
    @Test(description = "Verify successful EMI subvention TXN in checkout js for item based 0 cost emi")
    public void EMISubElementJsItembased01(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.UPI_INTENTONLY;
        List<SimplifiedSubvention.Item> items = new ArrayList<SimplifiedSubvention.Item>();
        SimplifiedSubvention.Item item= new SimplifiedSubvention.Item("1","321067334","124197",null,"1","20","51", true,false,null );
        items.add(item);
        SimplifiedSubvention simplifiedSubvention= new SimplifiedSubvention("1234", null, items);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO =new InitTxnDTO.Builder(null,merchantType)
                .setTxnValue("20")
                .setSimplifiedSubvention(simplifiedSubvention)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage elementCheckoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = elementCheckoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setTokenType("TXN_TOKEN");
        config.data.setToken(txnToken);
        elementCheckoutPage.createCheckoutJsOrder(config);
        elementCheckoutPage.createAndInvokePaymode("EMI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO= new PaymentDTO();
        paymentDTO.setEmiCard(paymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        cashierPage.payBy(Constants.PayMode.EMI, paymentDTO);
        Double subventiondiscount = 1.0; // for item based emi subvention discount is of 1 Rs
        Double finalTxnAmount= Double.valueOf(initTxnDTO.txnAmountFromBody())-subventiondiscount;
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(finalTxnAmount.toString())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateCheckSum(merchantType.getKey())
                .validateResponsePageParameters()
                .assertAll();

    }


    @Owner(Constants.Owner.RITIK)
    @Feature("PGP-33611")
    @Parameters({"theme"})
    @Test(description = "EMI subvention success txn on checkoutjs for amount based low cost emi")
    public void EMISubventionAmountBasedSuccess01(@Optional("checkoutjse_web_revamp") String theme) throws Exception{
        Constants.MerchantType merchantType = Constants.MerchantType.UPI_INTENTONLY;
        SimplifiedSubvention simplifiedSubvention= new SimplifiedSubvention("1234",null,"10",null);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO =new InitTxnDTO.Builder(null,merchantType)
                .setTxnValue("20")
                .setSimplifiedSubvention(simplifiedSubvention)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        PaymentDTO paymentDTO= new PaymentDTO();
        paymentDTO.setEmiCard(paymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        CheckoutJsCheckoutMerchantElementPage elementCheckoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = elementCheckoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setTokenType("TXN_TOKEN");
        config.data.setToken(txnToken);
        elementCheckoutPage.createCheckoutJsOrder(config);
        elementCheckoutPage.createAndInvokePaymode("EMI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.EMI, paymentDTO);
        Double subventiondiscount = 2.0; // for amount based emi subvention discount is of 2 Rs
        Double finalTxnAmount= Double.valueOf(initTxnDTO.txnAmountFromBody())-subventiondiscount;
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(finalTxnAmount.toString())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateCheckSum(merchantType.getKey())
                .validateResponsePageParameters()
                .assertAll();
    }

    @Owner(Constants.Owner.RITIK)
    @Feature("PGP-33611")
    @Parameters({"theme"})
    @Test(description = "EMI subvention success txn on checkoutjs for amount based 0 cost emi")
    public void EMISubventionAmountBasedSuccess02(@Optional("checkoutjse_web_revamp") String theme) throws Exception{
        Constants.MerchantType merchantType = Constants.MerchantType.EMISubvention;
        SimplifiedSubvention simplifiedSubvention= new SimplifiedSubvention("1234",null,"20",null);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO =new InitTxnDTO.Builder(null,merchantType)
                .setTxnValue("20")
                .setSimplifiedSubvention(simplifiedSubvention)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        PaymentDTO paymentDTO= new PaymentDTO();
        paymentDTO.setEmiCard(paymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        CheckoutJsCheckoutMerchantElementPage elementCheckoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = elementCheckoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setTokenType("TXN_TOKEN");
        config.data.setToken(txnToken);
        elementCheckoutPage.createCheckoutJsOrder(config);
        elementCheckoutPage.createAndInvokePaymode("EMI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.EMI, paymentDTO);
        Double subventiondiscount = 1.0; // for amount based emi subvention discount is of 2 Rs
        Double finalTxnAmount= Double.valueOf(initTxnDTO.txnAmountFromBody())-subventiondiscount;
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(finalTxnAmount.toString())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateCheckSum(merchantType.getKey())
                .validateResponsePageParameters()
                .assertAll();
    }

    @Owner(Constants.Owner.RITIK)
    @Feature("PGP-33611")
    @Parameters({"theme"})
    @Test(description = "EMI subvention success txn on checkoutjs for amount based low cost emi + promo discount")
    public void EMISubventionAmountBasedSuccess03(@Optional("checkoutjse_web_revamp") String theme) throws Exception{
        Constants.MerchantType merchantType = Constants.MerchantType.EMI_SUBVENTION;
        Merchant merchant = new Merchant(merchantType.getId(), true);
        Promo promocode = new Promo(false);
        merchant.getPromos().add(promocode);
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode("discount").setApplyAvailablePromo("true").setValidatePromo("true");
        SimplifiedSubvention simplifiedSubvention= new SimplifiedSubvention("1234",null,"10",null);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO =new InitTxnDTO.Builder(null,merchantType,simplifiedPaymentOffers)
                .setTxnValue("20")
                .setSimplifiedSubvention(simplifiedSubvention)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        PaymentDTO paymentDTO= new PaymentDTO();
        paymentDTO.setEmiCard(paymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        MerchantConfig config = elementCheckoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setTokenType("TXN_TOKEN");
        config.data.setToken(txnToken);
        elementCheckoutPage.createCheckoutJsOrder(config);
        elementCheckoutPage.createAndInvokePaymode("EMI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.EMI, paymentDTO);
        Double subventiondiscount = 2.0; // for amount based emi subvention discount is of 2 Rs
        Double discountedAmount= Double.valueOf(initTxnDTO.txnAmountFromBody())* 5 /100;
        Double finalTxnAmount= Double.valueOf(initTxnDTO.txnAmountFromBody())-subventiondiscount-discountedAmount;
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(finalTxnAmount.toString())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateCheckSum(merchantType.getKey())
                .validateResponsePageParameters()
                .assertAll();
    }

    @Owner(Constants.Owner.RITIK)
    @Feature("PGP-33611")
    @Parameters({"theme"})
    @Test(description = "Verify successful EMI subvention TXN in checkout js for item based 0 cost emi + promo discount")
    public void EMISubElementJsItembased02(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.EMI_SUBVENTION;
        Merchant merchant = new Merchant(merchantType.getId(), true);
        Promo promocode = new Promo(false);
        merchant.getPromos().add(promocode);
        List<SimplifiedSubvention.Item> items = new ArrayList<SimplifiedSubvention.Item>();
        SimplifiedSubvention.Item item= new SimplifiedSubvention.Item("1","321067334","124197",null,"1","20","51", true,false,null );
        items.add(item);
        SimplifiedSubvention simplifiedSubvention= new SimplifiedSubvention("1234", null, items);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode("discount").setApplyAvailablePromo("true").setValidatePromo("true");
        InitTxnDTO initTxnDTO =new InitTxnDTO.Builder(null,merchantType,simplifiedPaymentOffers)
                .setTxnValue("20")
                .setSimplifiedSubvention(simplifiedSubvention)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = elementCheckoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setTokenType("TXN_TOKEN");
        config.data.setToken(txnToken);
        elementCheckoutPage.createCheckoutJsOrder(config);
        elementCheckoutPage.createAndInvokePaymode("EMI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO= new PaymentDTO();
        paymentDTO.setEmiCard(paymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        cashierPage.payBy(Constants.PayMode.EMI, paymentDTO);
        Double subventiondiscount = 1.0; // for item based emi subvention discount is of 1 Rs
        Double discountedAmount= Double.valueOf(initTxnDTO.txnAmountFromBody())*5/100;
        Double finalTxnAmount= Double.valueOf(initTxnDTO.txnAmountFromBody())-subventiondiscount-discountedAmount;
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(finalTxnAmount.toString())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateCheckSum(merchantType.getKey())
                .validateResponsePageParameters()
                .assertAll();

    }

    @Owner(Constants.Owner.RITIK)
    @Feature("PGP-33611")
    @Parameters({"theme"})
    @Test(description = "Verify the error message when EMI is not supported on provided card")
    public void EMISubElementJsItembased03(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.EMISubvention;
        List<SimplifiedSubvention.Item> items = new ArrayList<SimplifiedSubvention.Item>();
        SimplifiedSubvention.Item item= new SimplifiedSubvention.Item("1","321067334","124197",null,"1","20","51", true,false,null );
        items.add(item);
        SimplifiedSubvention simplifiedSubvention= new SimplifiedSubvention("1234", null, items);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO =new InitTxnDTO.Builder(null,merchantType)
                .setTxnValue("20")
                .setSimplifiedSubvention(simplifiedSubvention)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = elementCheckoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setTokenType("TXN_TOKEN");
        config.data.setToken(txnToken);
        elementCheckoutPage.createCheckoutJsOrder(config);
        elementCheckoutPage.createAndInvokePaymode("EMI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO= new PaymentDTO();
        paymentDTO.setEmiCard(paymentDTO.INVALID_CARD);
        cashierPage.tabEMI().click();
        cashierPage.fillEMICardDetails(paymentDTO);
        cashierPage.waitUntilLoads();
        Assertions.assertThat(cashierPage.cardNotSupported().getText()).isEqualTo("EMI not available on this card. Please choose another card");
    }

    @Owner(Constants.Owner.RITIK)
    @Feature("PGP-33611")
    @Parameters({"theme"})
    @Test(description = "Verify that subvention amount and promo discount are visible on select plan page")
    public void EMISubElementJsItembased04(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.UPI_INTENTONLY;
        Merchant merchant = new Merchant(merchantType.getId(), true);
        Promo promocode = new Promo(false);
        merchant.getPromos().add(promocode);
        List<SimplifiedSubvention.Item> items = new ArrayList<SimplifiedSubvention.Item>();
        SimplifiedSubvention.Item item= new SimplifiedSubvention.Item("1","321067334","124197",null,"1","20","51", true,false,null );
        items.add(item);
        SimplifiedSubvention simplifiedSubvention= new SimplifiedSubvention("1234", null, items);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode("discount").setApplyAvailablePromo("true").setValidatePromo("true");
        InitTxnDTO initTxnDTO =new InitTxnDTO.Builder(null,merchantType,simplifiedPaymentOffers)
                .setTxnValue("20")
                .setSimplifiedSubvention(simplifiedSubvention)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = elementCheckoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setTokenType("TXN_TOKEN");
        config.data.setToken(txnToken);
        elementCheckoutPage.createCheckoutJsOrder(config);
        elementCheckoutPage.createAndInvokePaymode("EMI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO= new PaymentDTO();
        paymentDTO.setEmiCard(paymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        cashierPage.tabEMI().click();
        cashierPage.fillEMICardDetails(paymentDTO);
        DriverManager.getDriver().switchTo().parentFrame();
        cashierPage.waitUntilLoads();
        cashierPage.proceedToSelectEmiPlan().click();
        cashierPage.waitUntilLoads();
        cashierPage.SubventionDetails().assertVisible();
        cashierPage.PromoDetails().assertVisible();
        cashierPage.EMIDetails().assertVisible();
    }

    @Owner(Constants.Owner.RITIK)
    @Feature("PGP-33611")
    @Parameters({"theme"})
    @Test(description = "Verify that subvention amount and promo discount are visible on final page/conert to emi")
    public void EMISubElementJsItembased05(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.EMI_SUBVENTION;
        Merchant merchant = new Merchant(merchantType.getId(), true);
        Promo promocode = new Promo(true);
        merchant.getPromos().add(promocode);
        Double subventiondiscount = 1.0;
        List<SimplifiedSubvention.Item> items = new ArrayList<SimplifiedSubvention.Item>();
        SimplifiedSubvention.Item item= new SimplifiedSubvention.Item("1","321067334","124197",null,"1","20","51", true,false,null );
        items.add(item);
        SimplifiedSubvention simplifiedSubvention= new SimplifiedSubvention("1234", null, items);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode("discount").setApplyAvailablePromo("true").setValidatePromo("true");
        InitTxnDTO initTxnDTO =new InitTxnDTO.Builder(null,merchantType,simplifiedPaymentOffers)
                .setTxnValue("20")
                .setSimplifiedSubvention(simplifiedSubvention)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = elementCheckoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setTokenType("TXN_TOKEN");
        config.data.setToken(txnToken);
        Double discountedAmount= Double.valueOf(initTxnDTO.txnAmountFromBody())*5/100;
        Double finalTxnAmount=  Double.valueOf(initTxnDTO.txnAmountFromBody())-subventiondiscount-discountedAmount;
        elementCheckoutPage.createCheckoutJsOrder(config);
        elementCheckoutPage.createAndInvokePaymode("EMI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO= new PaymentDTO();
        paymentDTO.setEmiCard(paymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        cashierPage.tabEMI().click();
        cashierPage.fillEMICardDetails(paymentDTO);
        DriverManager.getDriver().switchTo().parentFrame();
        cashierPage.waitUntilLoads();
        cashierPage.proceedToSelectEmiPlan().click();
        cashierPage.proceedToConvertEMI().click();
        cashierPage.waitUntilLoads();
        cashierPage.SubventionDetailsOnConvertToEMIPage().assertVisible();
        cashierPage.PromoDetailsOnConvertToEMIPage().assertVisible();
        String Loantext=cashierPage.LoanAmount().getText();
        String amount=Loantext.substring(Loantext.length()-2,Loantext.length());
        Assertions.assertThat(amount).isEqualTo(String.valueOf(finalTxnAmount.intValue()));

    }

    @Owner(Constants.Owner.RITIK)
    @Feature("PGP-33611")
    @Parameters({"theme"})
    @Test(description = "Verify that success txn when user press cross button from convert to emi page and again fill the emi details and pay")
    public void EMISubElementJsItembased06(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.EMISubvention;
        List<SimplifiedSubvention.Item> items = new ArrayList<SimplifiedSubvention.Item>();
        SimplifiedSubvention.Item item= new SimplifiedSubvention.Item("1","321067334","124197",null,"1","20","51", true,false,null );
        items.add(item);
        SimplifiedSubvention simplifiedSubvention= new SimplifiedSubvention("1234", null, items);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO =new InitTxnDTO.Builder(null,merchantType)
                .setTxnValue("20")
                .setSimplifiedSubvention(simplifiedSubvention)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = elementCheckoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setTokenType("TXN_TOKEN");
        config.data.setToken(txnToken);
        elementCheckoutPage.createCheckoutJsOrder(config);
        elementCheckoutPage.createAndInvokePaymode("EMI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO= new PaymentDTO();
        paymentDTO.setEmiCard(paymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        cashierPage.tabEMI().click();
        cashierPage.fillEMICardDetails(paymentDTO);
        DriverManager.getDriver().switchTo().parentFrame();
        cashierPage.waitUntilLoads();
        cashierPage.proceedToSelectEmiPlan().click();
        cashierPage.proceedToConvertEMI().click();
        cashierPage.waitUntilLoads();
        cashierPage.crossButtonOnConvertToEMIPage().click();
        cashierPage.CrossButtonOnEntercardpage().click();
        cashierPage.newCardRadioBtn().click();
        cashierPage.payBy(Constants.PayMode.EMI, paymentDTO);
        Double subventiondiscount = 1.0; // for item based emi subvention discount is of 1 Rs
        Double finalTxnAmount= Double.valueOf(initTxnDTO.txnAmountFromBody())-subventiondiscount;
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(finalTxnAmount.toString())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateCheckSum(merchantType.getKey())
                .validateResponsePageParameters()
                .assertAll();
    }

    @Owner(Constants.Owner.RITIK)
    @Feature("PGP-33611")
    @Parameters({"theme"})
    @Test(description = "Verify that success txn when user press cross button from enter card number page and again fill the emi details and pay")
    public void EMISubElementJsItembased07(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.EMISubvention;
        List<SimplifiedSubvention.Item> items = new ArrayList<SimplifiedSubvention.Item>();
        SimplifiedSubvention.Item item= new SimplifiedSubvention.Item("1","321067334","124197",null,"1","20","51", true,false,null );
        items.add(item);
        SimplifiedSubvention simplifiedSubvention= new SimplifiedSubvention("1234", null, items);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO =new InitTxnDTO.Builder(null,merchantType)
                .setTxnValue("20")
                .setSimplifiedSubvention(simplifiedSubvention)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = elementCheckoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setTokenType("TXN_TOKEN");
        config.data.setToken(txnToken);
        elementCheckoutPage.createCheckoutJsOrder(config);
        elementCheckoutPage.createAndInvokePaymode("EMI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO= new PaymentDTO();
        paymentDTO.setEmiCard(paymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        cashierPage.tabEMI().click();
        cashierPage.fillEMICardDetails(paymentDTO);
        DriverManager.getDriver().switchTo().parentFrame();
        cashierPage.waitUntilLoads();
        cashierPage.CrossButtonOnEntercardpage().click();
        cashierPage.newCardRadioBtn().click();
        cashierPage.payBy(Constants.PayMode.EMI, paymentDTO);
        Double subventiondiscount = 1.0; // for item based emi subvention discount is of 1 Rs
        Double finalTxnAmount= Double.valueOf(initTxnDTO.txnAmountFromBody())-subventiondiscount;
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(finalTxnAmount.toString())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateCheckSum(merchantType.getKey())
                .validateResponsePageParameters()
                .assertAll();
    }

    @Owner(Constants.Owner.RITIK)
    @Feature("PGP-33611")
    @Parameters({"theme"})
    @Test(description = "Verify that when user click on View All to see bank for EMI then Zero/low cost emi and standard emi should be separated")
    public void EMISubElementJsItembased08(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.UPI_INTENTONLY;
        List<SimplifiedSubvention.Item> items = new ArrayList<SimplifiedSubvention.Item>();
        SimplifiedSubvention.Item item= new SimplifiedSubvention.Item("1","321067334","124197",null,"1","20","51", true,false,null );
        items.add(item);
        SimplifiedSubvention simplifiedSubvention= new SimplifiedSubvention("1234", null, items);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO =new InitTxnDTO.Builder(null,merchantType)
                .setTxnValue("20")
                .setSimplifiedSubvention(simplifiedSubvention)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = elementCheckoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setTokenType("TXN_TOKEN");
        config.data.setToken(txnToken);
        elementCheckoutPage.createCheckoutJsOrder(config);
        elementCheckoutPage.createAndInvokePaymode("EMI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO= new PaymentDTO();
        paymentDTO.setEmiCard(paymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        cashierPage.tabEMI().click();
        cashierPage.fillEMICardDetails(paymentDTO);
        DriverManager.getDriver().switchTo().parentFrame();
        cashierPage.waitUntilLoads();
        cashierPage.proceedToSelectEmiPlan().click();
        cashierPage.ViewAll().click();
        cashierPage.waitUntilLoads();
        cashierPage.ZeroLowCostEmiTab().assertVisible();
        cashierPage.StandardEmi().assertVisible();
    }

    @Owner(Constants.Owner.RITIK)
    @Feature("PGP-33611")
    @Parameters({"theme"})
    @Test(description = "EMI subvention success txn on checkoutjs for amount based low cost emi + promo cashback")
    public void EMISubventionAmountBasedSuccess04(@Optional("checkoutjse_web_revamp") String theme) throws Exception{
        Constants.MerchantType merchantType = Constants.MerchantType.UPI_INTENTONLY;
        Merchant merchant = new Merchant(merchantType.getId(), true);
        Promo promocode = new Promo(true);
        merchant.getPromos().add(promocode);
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode("cashback").setApplyAvailablePromo("true").setValidatePromo("true");
        SimplifiedSubvention simplifiedSubvention= new SimplifiedSubvention("1234",null,"10",null);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO =new InitTxnDTO.Builder(null,merchantType,simplifiedPaymentOffers)
                .setTxnValue("20")
                .setSimplifiedSubvention(simplifiedSubvention)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        PaymentDTO paymentDTO= new PaymentDTO();
        paymentDTO.setEmiCard(paymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        MerchantConfig config = elementCheckoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setTokenType("TXN_TOKEN");
        config.data.setToken(txnToken);
        elementCheckoutPage.createCheckoutJsOrder(config);
        elementCheckoutPage.createAndInvokePaymode("EMI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.EMI, paymentDTO);
        Double subventiondiscount = 2.0; // for amount based emi subvention discount is of 2 Rs
        Double finalTxnAmount= Double.valueOf(initTxnDTO.txnAmountFromBody())-subventiondiscount;
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(finalTxnAmount.toString())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateCheckSum(merchantType.getKey())
                .validateResponsePageParameters()
                .assertAll();
    }

    @Owner(Constants.Owner.RITIK)
    @Feature("PGP-33611")
    @Parameters({"theme"})
    @Test(description = "Verify that T&C button should be visible and clickable on select EMI plan page")
    public void EMISubElementJsItembased09(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.EMISubvention;
        List<SimplifiedSubvention.Item> items = new ArrayList<SimplifiedSubvention.Item>();
        SimplifiedSubvention.Item item = new SimplifiedSubvention.Item("1", "321067334", "124197", null, "1", "20", "51", true, false, null);
        items.add(item);
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention("1234", null, items);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode("discount").setApplyAvailablePromo("true").setValidatePromo("true");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType, simplifiedPaymentOffers)
                .setTxnValue("20")
                .setSimplifiedSubvention(simplifiedSubvention)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = elementCheckoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setTokenType("TXN_TOKEN");
        config.data.setToken(txnToken);
        elementCheckoutPage.createCheckoutJsOrder(config);
        elementCheckoutPage.createAndInvokePaymode("EMI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setEmiCard(paymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        cashierPage.tabEMI().click();
        cashierPage.fillEMICardDetails(paymentDTO);
        DriverManager.getDriver().switchTo().parentFrame();
        cashierPage.waitUntilLoads();
        cashierPage.proceedToSelectEmiPlan().click();
        cashierPage.TnCButton1().assertVisible();
        cashierPage.TnCButton1().assertClickable();
    }

    @Owner(Constants.Owner.RITIK)
    @Feature("PGP-33611")
    @Parameters({"theme"})
    @Test(description = "Verify the EMI subvention success txn with saved card when subvention is item based")
    public void EMISubElementJsItembased10(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        User user= userManager.getForRead(Label.BASIC);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getEmiCard());
        Constants.MerchantType merchantType = Constants.MerchantType.UPI_INTENTONLY;
        List<SimplifiedSubvention.Item> items = new ArrayList<SimplifiedSubvention.Item>();
        SimplifiedSubvention.Item item = new SimplifiedSubvention.Item("1", "321067334", "124197", null, "1", "20", "51", true, false, null);
        items.add(item);
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention("1234", null, items);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("20")
                .setSimplifiedSubvention(simplifiedSubvention)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = elementCheckoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setTokenType("TXN_TOKEN");
        config.data.setToken(txnToken);
        elementCheckoutPage.createCheckoutJsOrder(config);
        elementCheckoutPage.createAndInvokePaymode("EMI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.EMI_SAVED_CARD, paymentDTO);
        Double subventiondiscount = 1.0; // for item based emi subvention discount is of 1 Rs
        Double finalTxnAmount= Double.valueOf(initTxnDTO.txnAmountFromBody())-subventiondiscount;
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(finalTxnAmount.toString())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateCheckSum(merchantType.getKey())
                .validateResponsePageParameters()
                .assertAll();
    }

    @Owner(Constants.Owner.RITIK)
    @Feature("PGP-33611")
    @Parameters({"theme"})
    @Test(description = "Verify the EMI subvention success txn with saved card when subvention is amount based")
    public void EMISubventionAmountBasedSuccess05(@Optional("checkoutjse_web_revamp") String theme) throws Exception{
        User user= userManager.getForWrite(Label.BASIC);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getEmiCard());
        Constants.MerchantType merchantType = Constants.MerchantType.EMISubvention;
        SimplifiedSubvention simplifiedSubvention= new SimplifiedSubvention("1234",null,"10",null);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO =new InitTxnDTO.Builder(user.ssoToken(),merchantType)
                .setTxnValue("20")
                .setSimplifiedSubvention(simplifiedSubvention)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = elementCheckoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setTokenType("TXN_TOKEN");
        config.data.setToken(txnToken);
        elementCheckoutPage.createCheckoutJsOrder(config);
        elementCheckoutPage.createAndInvokePaymode("EMI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.EMI_SAVED_CARD, paymentDTO);
        Double subventiondiscount = 2.0; // for amount based emi subvention discount is of 2 Rs
        Double finalTxnAmount= Double.valueOf(initTxnDTO.txnAmountFromBody())-subventiondiscount;
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(finalTxnAmount.toString())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateCheckSum(merchantType.getKey())
                .validateResponsePageParameters()
                .assertAll();
    }

    @Owner(Constants.Owner.RITIK)
    @Feature("PGP-33611")
    @Parameters({"theme"})
    @Test(description = "Verify that when user click on View All and select Zero/Low cost emi bank and pay using the same")
    public void EMISubElementJsItembased11(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.EMISubvention;
        List<SimplifiedSubvention.Item> items = new ArrayList<SimplifiedSubvention.Item>();
        SimplifiedSubvention.Item item = new SimplifiedSubvention.Item("1", "321067334", "124197", null, "1", "20", "51", true, false, null);
        items.add(item);
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention("1234", null, items);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setTxnValue("20")
                .setSimplifiedSubvention(simplifiedSubvention)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = elementCheckoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setTokenType("TXN_TOKEN");
        config.data.setToken(txnToken);
        elementCheckoutPage.createCheckoutJsOrder(config);
        elementCheckoutPage.createAndInvokePaymode("EMI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
//        cashierPage.tabEMI().click();
        cashierPage.dropdownEmiBanksV5().selectByVisibleText("HDFC");
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_emiIframe());
        cashierPage.textBoxCardNumberEMI().clearAndType(paymentDTO.getEmiCard());
        cashierPage.waitUntilLoads();
        DriverManager.getDriver().switchTo().parentFrame();
        cashierPage.waitUntilLoads();
        cashierPage.proceedToSelectEmiPlan().click();
        cashierPage.proceedToConvertEMI().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_expCvv_cardIframe());
        cashierPage.textBoxExpiryMonthEMI().clearAndType(paymentDTO.getExpMonth());
        cashierPage.textBoxExpiryYearEMI().waitUntilEditable();
        cashierPage.textBoxExpiryYearEMI().clearAndType(paymentDTO.getExpYear());
        cashierPage.textBoxCVVNumber().clearAndType(paymentDTO.getCvvNumber());
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.buttonPGPayNow().click();
        Double subventiondiscount = 1.0; // for item based emi subvention discount is of 1 Rs
        Double finalTxnAmount= Double.valueOf(initTxnDTO.txnAmountFromBody())-subventiondiscount;
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(finalTxnAmount.toString())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateCheckSum(merchantType.getKey())
                .validateResponsePageParameters()
                .assertAll();
    }
    private InitTxnResponseDTO validateSuccessInitiateSubscription(InitTxnDTO initTxnDTO) {
        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultStatus())
                .as("resultStatus mismatch")
                .isEqualTo("S");
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultCode())
                .as("resultCode mismatch")
                .isEqualTo("0000");
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultMsg())
                .as("resultMsg mismatch")
                .isEqualToIgnoringCase("Success");
        return responseDTO;

    }
    String ui_msg = "Please enter a valid VPA of the form username@bank";
   String new_ui_msg= "Invalid VPA, Try Again";
    String log_msg = "Invalid UPI address";
    public void ui_vpa_msg(CashierPage cashierPage) throws Exception{
        cashierPage.textBoxVPA().waitUntilVisible();
        cashierPage.textBoxVPA().clearAndType("11invalid@pay11");
//        cashierPage.verifyVPALinkText().click();
        cashierPage.buttonPGPayNow().click();
        String d1= cashierPage.vpaerrormsg().getText();
        Assertions.assertThat(d1).isEqualTo(new_ui_msg);
    }
    public void theia_facade_vpa_msg(CashierPage cashierPage , String orderid) throws Exception{
//        cashierPage.tabUPI().click();
        cashierPage.textBoxVPA().waitUntilVisible();
        cashierPage.textBoxVPA().clearAndType("invaliUPI@paytm");
//        cashierPage.verifyVPALinkText().click();
        cashierPage.buttonPGPayNow().click();
        String d1= cashierPage.vpaerrormsg().getText();
        Assertions.assertThat(d1).isEqualTo(Constants.MessageAssert.INVALID_VPA.toString());
        //response msg returned from UPI_SECURE Component
        String grepcmd = "grep \"" + orderid + "\" "+ LocalConfig.THEIA_FACADE_LOGS
                + " | grep \"UPI_SECURE\" | " + "grep \"RESPONSE\"";
        String theiaFacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assertions.assertThat(theiaFacadeLogs).contains("\""+Constants.MessageAssert.INVALID_UPI.toString()+"\"");
    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-37186")
    @Parameters({"theme"})
    @Test(description = "Verify error message \"Please enter a valid UPI ID\" should be displayed after entering an invalid \"UPI ID\" and clicking on pay button for NONE payment flow")
    public void validate_vpa_msg(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.UPI;
        User user = userManager.getForRead(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),merchantType)
                .setTxnValue("2.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.pause(3);
        checkoutPage.createAndInvokePaymode("UPI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        ui_vpa_msg(cashierPage);
    }
//    @Owner(ROHIT_SHARMA)
//    @Feature("PGP-37186")
//    @Parameters({"theme"})
//    @Test(enabled = false, description = "Verify error message \"Please enter a valid UPI ID\" should be displayed after entering an invalid \"UPI ID\" and clicking on pay button for AddnPay payment flow")
    public void validate_vpa_msg_AddnPay(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.AddnPay;
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("2.00")
                .build();
        WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()) - 1);
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.pause(3);
        checkoutPage.createAndInvokePaymode("PAY WITH PAYTM");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabUPI().click();
        ui_vpa_msg(cashierPage);
    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-37186")
    @Parameters({"theme"})
    @Test(description = "Verify error message \"Please enter a valid UPI ID\" should be displayed after entering an invalid \"UPI ID\" and clicking on pay button for Subscription payment flow")
    public void validate_vpa_msg_Subscription(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBS_ALL_PAYMODES;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("5")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("YEAR")
                .setSubscriptionGraceDays("3")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setSubscriptionRetryCount("1")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.pause(3);
        checkoutPage.createAndInvokePaymode("UPI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        ui_vpa_msg(cashierPage);
    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-37186")
    @Parameters({"theme"})
    @Test(description = "Verify error message \"Invalid VPA ID, Try Again\"\" should be displayed after entering an invalid UPIID on UI and theia_facade should have \"Invalid UPI ID.\"\" logs as well for none payment flow")
    public void validate_vpa_msg_logs(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.AddnPay;
        User user = userManager.getForRead(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),merchant)
                .setTxnValue("2.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.pause(3);
        checkoutPage.createAndInvokePaymode("UPI");
        String orderid= initTxnDTO.orderFromBody();
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        theia_facade_vpa_msg(cashierPage,orderid);
    }
//    @Owner(ROHIT_SHARMA)
//    @Feature("PGP-37186")
//    @Parameters({"theme"})
//    @Test(enabled = false, description = "Verify error message \"Invalid VPA ID, Try Again\"\" should be displayed after entering an invalid UPIID on UI and theia_facade logs should have \"Invalid UPI ID.\"\" for addnpay payment flow")
    public void validate_vpa_msg_logs_addnpay(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.AddnPay;
        User user = userManager.getForWrite(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("2.00")
                .build();
        WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()) - 1);
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.pause(3);
        checkoutPage.createAndInvokePaymode("PAY WITH PAYTM");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabUPI().click();
        String orderid= initTxnDTO.orderFromBody();
        theia_facade_vpa_msg(cashierPage,orderid);
    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-37186")
    @Parameters({"theme"})
    @Test(description = "Verify error message \"Invalid VPA ID, Try Again\"\" should be displayed after entering an invalid UPIID on UI and theia_facade logs should have \"Invalid UPI ID.\"\" for subscription payment flow")
    public void validate_vpa_msg_logs_subscription(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBS_ALL_PAYMODES;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("5")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("YEAR")
                .setSubscriptionGraceDays("3")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setSubscriptionRetryCount("1")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.pause(3);
        checkoutPage.createAndInvokePaymode("UPI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        String orderid= initTxnDTO.orderFromBody();
        theia_facade_vpa_msg(cashierPage,orderid);
    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-37186")
    @Parameters({"theme"})
    @Test(description = "Verify error message \"Please enter a valid UPI ID\" should be displayed after entering an invalid \"UPI ID\" and clicking on pay button for NONE payment flow")
    public void Nonlogeedin_validate_vpa_msg(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.AddnPay;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null ,merchant)
                .setTxnValue("2.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.pause(3);
        checkoutPage.createAndInvokePaymode("UPI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        ui_vpa_msg(cashierPage);
    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-37186")
    @Parameters({"theme"})
    @Test(description = "Verify error message \"Please enter a valid UPI ID\" should be displayed after entering an invalid \"UPI ID\" and clicking on pay button for Subscription payment flow")
    public void Nonloggedin_validate_vpa_msg_Subscription(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.SUBS_ALL_PAYMODES;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("5")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("YEAR")
                .setSubscriptionGraceDays("3")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setSubscriptionRetryCount("1")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.pause(3);
        checkoutPage.createAndInvokePaymode("UPI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        ui_vpa_msg(cashierPage);
    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-37186")
    @Parameters({"theme"})
    @Test(description = "Verify error message \"Invalid VPA ID, Try Again\"\" should be displayed after entering an invalid UPIID on UI and theia_facade should have \"Invalid UPI ID.\"\" logs as well for none payment flow")
    public void Nonloggedin_validate_vpa_msg_logs(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.AddnPay;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null,merchant)
                .setTxnValue("2.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.pause(3);
        checkoutPage.createAndInvokePaymode("UPI");
        String orderid= initTxnDTO.orderFromBody();
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        theia_facade_vpa_msg(cashierPage,orderid);
    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-37186")
    @Parameters({"theme"})
    @Test(description = "Verify error message \"Invalid VPA ID, Try Again\"\" should be displayed after entering an invalid UPIID on UI and theia_facade should have \"Invalid UPI ID.\"\" logs as well for subscription payment flow")
    public void Nonloggedin_validate_vpa_msg_logs_subscription(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.SUBS_ALL_PAYMODES;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("5")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("YEAR")
                .setSubscriptionGraceDays("3")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setSubscriptionRetryCount("1")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.pause(3);
        checkoutPage.createAndInvokePaymode("UPI");
        String orderid= initTxnDTO.orderFromBody();
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        theia_facade_vpa_msg(cashierPage,orderid);
    }



    private String getColorCode(String color) {
        String[] hexValue = color.replace("rgba", "").replace(")", "").replace("(", "").split(",");
        hexValue[0] = hexValue[0].trim();
        int hexValue1 = Integer.parseInt(hexValue[0]);
        hexValue[1] = hexValue[1].trim();
        int hexValue2 = Integer.parseInt(hexValue[1]);
        hexValue[2] = hexValue[2].trim();
        int hexValue3 = Integer.parseInt(hexValue[2]);
        return String.format("#%02x%02x%02x", hexValue1, hexValue2, hexValue3);
    }

    private UIElement getElementByXpath(String xpath, String theme) {
        return new UIElement(By.xpath(xpath), theme, xpath);
    }

    @Owner(ROHIT_SHARMA)
    @Feature("PGP-46447")
    @Parameters({"theme"})
    @Test(description = "Verify Scan with any UPI App, is shown for the Qr rendered")
    public void verifyScanQrRevamp(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.QR_ENABLED_MERCHANT_JS).setTxnValue("10")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.pause(3);
        checkoutPage.createAndInvokePaymode("SCAN AND PAY");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        String Qrtext= cashierPage.qrCodeCheckoutJSTextNew().getText();
        Assertions.assertThat(Qrtext).contains("Scan with any UPI App");
    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-46447")
    @Parameters({"theme"})
    @Test(description = "Verify that enabled Paymodes text colour is changes to black,for the Qr rendered")
    public void verifyEnabledPaymodeColourRevamp(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.QR_ENABLED_MERCHANT_JS).setTxnValue("10")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.pause(3);
        checkoutPage.createAndInvokePaymode("SCAN AND PAY");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        String enabledColour = getColorCode(cashierPage.enabledPaymodes().getCssValue("color"));
        System.out.println(enabledColour);
        Assertions.assertThat(enabledColour).contains("#101010");
    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-46447")
    @Parameters({"theme"})
    @Test(description = "Verify that or Scan with any UPI app in grey with UPI images is shown for the Qr rendered")
    public void verifyorScanwithUPItextRevamp(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.PG2_JS_Checkout_Paytm_Domain).setTxnValue("10")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.pause(3);
        checkoutPage.createAndInvokePaymode("SCAN AND PAY");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        String qrSubText = cashierPage.qrSubTextNew().getText();
        Assertions.assertThat(qrSubText).contains("& more");
        String qrSubTestColour = getColorCode(cashierPage.qrSubTextNew().getCssValue("color"));
        System.out.println(qrSubTestColour);
     //   Assertions.assertThat(qrSubTestColour).contains("#0f0f0f");
        cashierPage.qrCodeImgNew().waitUntilVisible();
        cashierPage.qrCodeImgNew().assertVisible();
    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-47696")
    @Parameters({"theme"})
    @Test(description = "Verify that for deactive postpaid user account msg Your Postpaid account is not active. Please use other Payment option. should be displayed")
    public void postpaidDeactiveUimsg(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.UPIPUSHPG2);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.PGOnly).setTxnValue("24")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        PostpaidHelpers.updateBalance(initTxnDTO.txnAmountFromBody());
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.pause(3);
        checkoutPage.createAndInvokePaymode("PAY WITH PAYTM");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        if (!cashierPage.radioButtonPaytmPostpaid().isSelected()) {
            cashierPage.radioButtonPaytmPostpaid().click();
        }
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.waitUntilLoads();
        String text = cashierPage.paymodeDisabledContainer().getText();
        Assertions.assertThat(text).contains("Your Postpaid account is not active. Please use other Payment option");
    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-48836")
    @Parameters({"theme"})
    @Test(description = "Verify that cashierPage is loaded when no-wallet mid is used and complete the txn")
    public void no_Wallet_mid_cashierPage_loaded_loggedin(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.ONLY_CC).setTxnValue("2")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.pause(3);
        checkoutPage.createAndInvokePaymode("CC");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        Assertions.assertThat(cashierPage.tabCreditCard().isDisplayed());
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .assertAll();
    }
//     @Owner(ROHIT_SHARMA)
//     @Feature("PGP-48836")
//     @Parameters({"theme"})
//     @Test(description = "Verify that cashierPage is loaded when wallet mid is used and complete the txn", enabled = false)
    public void Wallet_mid_cashierPage_loaded_loggedin(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.UPIPUSHPG2);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.WALLET_UPI_MID).setTxnValue("2")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        WalletHelpers.modifyBalance(user, Double.parseDouble("20.00"));
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.pause(3);
        checkoutPage.createAndInvokePaymode("PAY WITH PAYTM");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.checkBoxPPI().waitUntilChecked();
        cashierPage.buttonWalletPayNow().waitUntilClickable();
        cashierPage.buttonPGPayNow().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("PPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-48836")
    @Parameters({"theme"})
    @Test(description = "Verify that cashierPage is loaded when no-wallet mid is used and complete the txn")
    public void no_Wallet_mid_cashierPage_loaded_non_loggedin(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.ONLY_CC).setTxnValue("2")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.pause(3);
        checkoutPage.createAndInvokePaymode("CC");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        Assertions.assertThat(cashierPage.tabCreditCard().isDisplayed());
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.promoCC);
        cashierPage.payBy(Constants.PayMode.CC,paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .assertAll();
    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-48836")
    @Parameters({"theme"})
    @Test(description = "Verify that cashierPage is loaded when wallet mid is used and complete the txn")
    public void Wallet_mid_cashierPage_loaded_non_loggedin(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.WALLET_UPI_MID).setTxnValue("2")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.pause(3);
        checkoutPage.createAndInvokePaymode("UPI");
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
                .assertAll();
    }
//     @Owner(ROHIT_SHARMA)
//     @Feature("PGP-48836")
//     @Parameters({"theme"})
//     @Test(description = "Verify that cashierPage is loaded when wallet mid is used and complete the txn by login otp", enabled = false)
    public void Wallet_mid_cashierPage_loaded_login_otp_txn(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.WALLET_UPI_MID).setTxnValue("2")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.pause(3);
        checkoutPage.createAndInvokePaymode("PAY WITH PAYTM");
        WalletHelpers.modifyBalance(userManager.getForWrite(Label.UPIPUSHPG2), Double.parseDouble("20.00"));
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.loginStrip().waitUntilVisible();
        cashierPage.loginStrip().click();
        cashierPage.textBoxPhoneNumber().waitUntilVisible();
        cashierPage.textBoxPhoneNumber().clearAndType("8006006993");
        cashierPage.buttonSecureSignIn().click();
        cashierPage.fillLoginOtp("888888");
        cashierPage.buttonSecureSignIn().click();
        cashierPage.waitUntilLoads();
        cashierPage.checkBoxPPI().waitUntilChecked();
        cashierPage.buttonWalletPayNow().waitUntilClickable();
        cashierPage.buttonPGPayNow().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("PPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
    }
//     @Owner(ROHIT_SHARMA)
//     @Feature("PGP-48836")
//     @Parameters({"theme"})
//     @Test(description = "Verify that cashierPage is loaded when wallet only mid is used and complete the txn by login otp", enabled = false)
    public void Wallet_only_cashierPage_loaded_login_otp_txn(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.WALLET_ONLY_MER).setTxnValue("2")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.pause(3);
        checkoutPage.createAndInvokePaymode("PAY WITH PAYTM");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        WalletHelpers.modifyBalance(userManager.getForWrite(Label.UPIPUSHPG2), Double.parseDouble("20.00"));
        cashierPage.loginStrip().waitUntilVisible();
        cashierPage.loginStrip().click();
        cashierPage.textBoxPhoneNumber().waitUntilVisible();
        cashierPage.textBoxPhoneNumber().clearAndType("8006006993");
        cashierPage.buttonSecureSignIn().click();
        cashierPage.fillLoginOtp("888888");
        cashierPage.buttonSecureSignIn().click();
        cashierPage.waitUntilLoads();
        cashierPage.checkBoxPPI().waitUntilChecked();
        cashierPage.buttonWalletPayNow().waitUntilClickable();
        cashierPage.buttonPGPayNow().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("PPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
    }
//     @Owner(ROHIT_SHARMA)
//     @Feature("PGP-48836")
//     @Parameters({"theme"})
//     @Test(description = "Verify that cashierPage is loaded when wallet only mid is used and complete the txn", enabled = false)
    public void Wallet_only_cashierPage_loaded_loggedin_txn(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.UPIPUSHPG2);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.WALLET_UPI_MID).setTxnValue("2")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        WalletHelpers.modifyBalance(user, Double.parseDouble("20.00"));
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.pause(3);
        checkoutPage.createAndInvokePaymode("PAY WITH PAYTM");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.checkBoxPPI().waitUntilChecked();
        cashierPage.buttonWalletPayNow().waitUntilClickable();
        cashierPage.buttonPGPayNow().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("PPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
    }

    public void kfs(CashierPage cashierPage , String cardNo) throws Exception {
        cashierPage.EmiRadioButton().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_emiIframe());
        cashierPage.textBoxCardNumberEMI().clearAndType(cardNo);
        cashierPage.waitUntilLoads();
        cashierPage.emiPlan().click();
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.proceedToConvertEMI().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_expCvv_cardIframe());
        cashierPage.textBoxExpiryMonthEMI().clearAndType("12");
        cashierPage.textBoxExpiryYearEMI().waitUntilEditable();
        cashierPage.textBoxExpiryYearEMI().clearAndType("25");
        cashierPage.textBoxCVVNumber().clearAndType("226");
    }

//     @Owner(ROHIT_SHARMA)
//     @Feature("PGP-48507")
//     @Parameters({"theme"})
//     @Test(description = "Verify E2E txn of ICICI emi dc using new kfs changes",enabled = false)// Need to remove this case due to  new ICICI formate 
    public void kfs_icici_dc(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.EmiInfo_COP).setTxnValue("2000")
                .build();
        PaymentDTO paymentDTO = new PaymentDTO().setEmiCard(PaymentDTO.ICICI_DEBIT_CARD_NUMBER_EMI)
                .setBankName("ICICI");
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.pause(4);
        checkoutPage.createAndInvokePaymode("EMI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.EMI_DC,paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validateRespMsg("Txn Success")
                .validateRespCode("01")
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validatePaymentMode("EMI_DC")
                .validateGatewayName("ICIE")
                .validateBankName("ICICI Bank")
                .assertAll();
    }
//     @Owner(ROHIT_SHARMA)
//     @Feature("PGP-48507")
//     @Parameters({"theme"})
//     @Test(description = "Verify that I adhere to Key Fact Statement and digital lending consent should be displayed on ui for icici emi-dc", enabled = false)// Need to remove this case due to  new ICICI formate 
    public void kfs_icici_dc_adhere_text(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.EmiInfo_COP).setTxnValue("2000")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.pause(4);
        checkoutPage.createAndInvokePaymode("EMI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        kfs(cashierPage,"4572741654006328");
        DriverManager.getDriver().switchTo().defaultContent();
        String adhere = cashierPage.kfsAdhereText().getText();
        Assertions.assertThat(adhere).contains("I adhere to Key Fact Statement and digital lending consent");
    }
//     @Owner(ROHIT_SHARMA)
//     @Feature("PGP-48507")
//     @Parameters({"theme"})
//     @Test(description = "Verify that Key Fact Statement and digital lending consent should be displayed on ui for icici emi-dc and it is clickable as link",enabled=false)
    public void kfs_icici_dc_adhere_text_link(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.EmiInfo_COP).setTxnValue("2000")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.pause(4);
        checkoutPage.createAndInvokePaymode("EMI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        kfs(cashierPage,"4572741654006328");
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.kfsLink().assertClickable();
    }
//     @Owner(ROHIT_SHARMA)
//     @Feature("PGP-48507")
//     @Parameters({"theme"})
//     @Test(description = "Verify that Key Fact statement - EMI on Debit Card is displayed on KFS page for icici emi dc",enabled = false)
    public void kfs_icici_dc_kfs_upper_text(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.EmiInfo_COP).setTxnValue("2000")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.pause(4);
        checkoutPage.createAndInvokePaymode("EMI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        kfs(cashierPage,"4572741654006328");
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.kfsLink().click();
        cashierPage.waitUntilLoads();
        DriverManager.getDriver().switchTo().frame(cashierPage.kfs_frame());
        String kfsUpper = cashierPage.kfsUpperText().getText();
        Assertions.assertThat(kfsUpper).contains("Key Fact statement - EMI on Debit Card");
    }
//     @Owner(ROHIT_SHARMA)
//     @Feature("PGP-48507")
//     @Parameters({"theme"})
//     @Test(description = "Verify that Key Fact statement - EMI on Debit Card is displayed on KFS page for icici emi dc", enabled = false)// Need to remove this case due to  new ICICI formate 
    public void kfs_icici_dc_kfs_bank_date_text(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.EmiInfo_COP).setTxnValue("2000")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.pause(4);
        checkoutPage.createAndInvokePaymode("EMI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        kfs(cashierPage,"4572741654006328");
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.kfsLink().click();
        cashierPage.waitUntilLoads();
        DriverManager.getDriver().switchTo().frame(cashierPage.kfs_frame());
        String kfsUpper = cashierPage.kfsBankDateText().getText();
       Assertions.assertThat(kfsUpper).contains("Name of Regulated entity - ICICI");
       Assertions.assertThat(kfsUpper).contains("Date:");
    }
//     @Owner(ROHIT_SHARMA)
//     @Feature("PGP-48507")
//     @Parameters({"theme"})
//     @Test(description = "Verify that KFS table text should contain all the parameters for ICICI emi dc",enabled=false)
    public void kfs_icici_dc_kfs_table_text(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.EmiInfo_COP).setTxnValue("2000")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.pause(4);
        checkoutPage.createAndInvokePaymode("EMI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        kfs(cashierPage,"4572741654006328");
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.kfsLink().click();
        cashierPage.waitUntilLoads();
        DriverManager.getDriver().switchTo().frame(cashierPage.kfs_frame());
        String kfsUpper = cashierPage.kfsTableText().getText();
        Assertions.assertThat(kfsUpper).contains("Parameter");
        Assertions.assertThat(kfsUpper).contains("Details");
        Assertions.assertThat(kfsUpper).contains("Loan Amount (amount disbursed / to be disbursed to the borrower) in Rupees");
        Assertions.assertThat(kfsUpper).contains("Total interest charge during the entire tenor of the loan (in Rupees)");
        Assertions.assertThat(kfsUpper).contains("Other up-front charges, if any");
        Assertions.assertThat(kfsUpper).contains("a. GST applicable on Processing Fees (in Rupees)");
        Assertions.assertThat(kfsUpper).contains("b. Processing Fees (in Rupees)");
        Assertions.assertThat(kfsUpper).contains("Net Disbursed Amount (in Rupees)");
        Assertions.assertThat(kfsUpper).contains("Total Amount to be paid by the borrower (sum of (i),(ii) and (iii)) (in Rupees)");
        Assertions.assertThat(kfsUpper).contains("Annual Percentage Rate - Effective annualised interest rate (in percentage) computed on net disbursed amount using IRR approach and reducing balance method (APR is exclusive of GST on processing fees)");
        Assertions.assertThat(kfsUpper).contains("Tenure of the loan in months");
        Assertions.assertThat(kfsUpper).contains("Repayment frequency");
        Assertions.assertThat(kfsUpper).contains("No. of instalments of repayment");
        Assertions.assertThat(kfsUpper).contains("Amount of each instalment of repayment in Rupees");
        Assertions.assertThat(kfsUpper).contains("Monthly");
        Assertions.assertThat(kfsUpper).contains("Details about Contingent Charges");
        Assertions.assertThat(kfsUpper).contains("Rate of annualised penal charges in case of delayed payments");
        Assertions.assertThat(kfsUpper).contains("Rate of annualized other penal charges");
        Assertions.assertThat(kfsUpper).contains("Cooling off/look-up period during which borrower shall not be charged any penalty on prepayment of loan");
        Assertions.assertThat(kfsUpper).contains("Name, designation, address and phone number of nodal grievance redressal officer designated specifically to deal with FinTech/digital lending related complaints/issues");
        Assertions.assertThat(kfsUpper).contains("Details of LSP acting as recovery agent and authorized to approach the borrower*");
    }
//     @Owner(ROHIT_SHARMA)
//     @Feature("PGP-48507")
//     @Parameters({"theme"})
//     @Test(description = "Verify that KFS Table Data for ICICI emi dc",enabled = false)
    public void kfs_icici_dc_kfs_table_Verifivation(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.EmiInfo_COP).setTxnValue("2000")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.pause(4);
        checkoutPage.createAndInvokePaymode("EMI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        kfs(cashierPage,"4572741654006328");
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.kfsLink().click();
        cashierPage.waitUntilLoads();
        DriverManager.getDriver().switchTo().frame(cashierPage.kfs_frame());
        String kfsUpper = cashierPage.kfscloanAmount().getText();
        Assertions.assertThat(kfsUpper).contains("2000");
        String kfstenure = cashierPage.kfsctenure().getText();
        Assertions.assertThat(kfstenure).contains("3");
        String kfsinstallments = cashierPage.kfsinstallments().getText();
        Assertions.assertThat(kfsinstallments).contains("3");
    }
//     @Owner(ROHIT_SHARMA)
//     @Feature("PGP-48507")
//     @Parameters({"theme"})
//     @Test(description = "Verify that I adhere to Key Fact Statement should be displayed on ui for hdfc emi-dc", enabled = false)// Need to remove this case due to  new ICICI formate 
    public void kfs_hdfc_dc_adhere_text(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.EmiInfo_COP).setTxnValue("2000")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.pause(4);
        checkoutPage.createAndInvokePaymode("EMI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        kfs(cashierPage,"4444333322221111");
        DriverManager.getDriver().switchTo().defaultContent();
        String adhere = cashierPage.kfsAdhereText().getText();
        Assertions.assertThat(adhere).isEqualTo("I adhere to Key Fact Statement");
    }
//     @Owner(ROHIT_SHARMA)
//     @Feature("PGP-48507")
//     @Parameters({"theme"})
//     @Test(description = "Verify that Key Fact Statement should be displayed on ui for hdfc emi-dc and it is clickable as link",enabled = false)
    public void kfs_hdfc_dc_adhere_text_link(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.EmiInfo_COP).setTxnValue("2000")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.pause(4);
        checkoutPage.createAndInvokePaymode("EMI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        kfs(cashierPage,"4444333322221111");
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.kfsLink().assertClickable();
    }
//     @Owner(ROHIT_SHARMA)
//     @Feature("PGP-48507")
//     @Parameters({"theme"})
//     @Test(description = "Verify that Key Fact statement - EMI on Debit Card is displayed on KFS page for hdfc dc emi",enabled = false) // Need to remove this case due to  new ICICI formate 
    public void kfs_hdfc_dc_kfs_upper_text(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.EmiInfo_COP).setTxnValue("2000")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.pause(4);
        checkoutPage.createAndInvokePaymode("EMI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        kfs(cashierPage,"4444333322221111");
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.kfsLink().click();
        cashierPage.waitUntilLoads();
        DriverManager.getDriver().switchTo().frame(cashierPage.kfs_frame());
        String kfsUpper = cashierPage.kfsUpperText().getText();
        Assertions.assertThat(kfsUpper).contains("Key Fact statement - EMI on Debit Card");
    }
//     @Owner(ROHIT_SHARMA)
//     @Feature("PGP-48507")
//     @Parameters({"theme"})
//     @Test(description = "Verify that Key Fact statement - EMI on Debit Card is displayed on KFS page for hdfc emi dc", enabled = false) // Need to remove this case due to  new ICICI formate 
    public void kfs_hdfc_dc_kfs_bank_date_text(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.EmiInfo_COP).setTxnValue("2000")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.pause(4);
        checkoutPage.createAndInvokePaymode("EMI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        kfs(cashierPage,"4444333322221111");
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.kfsLink().click();
        cashierPage.waitUntilLoads();
        DriverManager.getDriver().switchTo().frame(cashierPage.kfs_frame());
        String kfsUpper = cashierPage.kfsBankDateText().getText();
        Assertions.assertThat(kfsUpper).contains("Name of Regulated entity - HDFC");
        Assertions.assertThat(kfsUpper).contains("Date:");
    }
//     @Owner(ROHIT_SHARMA)
//     @Feature("PGP-48507")
//     @Parameters({"theme"})
//     @Test(description = "Verify that KFS table text should contain all the parameters for hdfc emi dc",enabled = false)
    public void kfs_hdfc_dc_kfs_table_text(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.EmiInfo_COP).setTxnValue("2000")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.pause(4);
        checkoutPage.createAndInvokePaymode("EMI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        kfs(cashierPage,"4444333322221111");
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.kfsLink().click();
        cashierPage.waitUntilLoads();
        DriverManager.getDriver().switchTo().frame(cashierPage.kfs_frame());
        String kfsUpper = cashierPage.kfsTableText().getText();
        Assertions.assertThat(kfsUpper).contains("Parameter");
        Assertions.assertThat(kfsUpper).contains("Details");
        Assertions.assertThat(kfsUpper).contains("Loan Amount (amount disbursed / to be disbursed to the borrower) in Rupees");
        Assertions.assertThat(kfsUpper).contains("Total interest charge during the entire tenor of the loan (in Rupees)");
        Assertions.assertThat(kfsUpper).contains("Other up-front charges, if any");
        Assertions.assertThat(kfsUpper).contains("a. GST applicable on Processing Fees (in Rupees)");
        Assertions.assertThat(kfsUpper).contains("b. Processing Fees (in Rupees)");
        Assertions.assertThat(kfsUpper).contains("Net Disbursed Amount (in Rupees)");
        Assertions.assertThat(kfsUpper).contains("Total Amount to be paid by the borrower (sum of (i),(ii) and (iii)) (in Rupees)");
        Assertions.assertThat(kfsUpper).contains("Annual Percentage Rate - Effective annualised interest rate (in percentage) computed on net disbursed amount using IRR approach and reducing balance method (APR is exclusive of GST on processing fees)");
        Assertions.assertThat(kfsUpper).contains("Tenure of the loan in months");
        Assertions.assertThat(kfsUpper).contains("Repayment frequency");
        Assertions.assertThat(kfsUpper).contains("No. of instalments of repayment");
        Assertions.assertThat(kfsUpper).contains("Amount of each instalment of repayment in Rupees");
        Assertions.assertThat(kfsUpper).contains("Monthly");
        Assertions.assertThat(kfsUpper).contains("Details about Contingent Charges");
        Assertions.assertThat(kfsUpper).contains("Rate of annualised penal charges in case of delayed payments");
        Assertions.assertThat(kfsUpper).contains("Rate of annualized other penal charges");
        Assertions.assertThat(kfsUpper).contains("Cooling off/look-up period during which borrower shall not be charged any penalty on prepayment of loan");
        Assertions.assertThat(kfsUpper).contains("Name, designation, address and phone number of nodal grievance redressal officer designated specifically to deal with FinTech/digital lending related complaints/issues");
        Assertions.assertThat(kfsUpper).contains("Details of LSP acting as recovery agent and authorized to approach the borrower*");
    }
//     @Owner(ROHIT_SHARMA)
//     @Feature("PGP-48507")
//     @Parameters({"theme"})
//     @Test(description = "Verify that KFS Disclaimer text should be displayed on KFS page hdfc emi dc",enabled = false)
    public void kfs_hdfc_dc_kfs_disclaimer_text(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.EmiInfo_COP).setTxnValue("2000")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.pause(4);
        checkoutPage.createAndInvokePaymode("EMI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        kfs(cashierPage,"4444333322221111");
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.kfsLink().click();
        cashierPage.waitUntilLoads();
        DriverManager.getDriver().switchTo().frame(cashierPage.kfs_frame());
        String kfsUpper = cashierPage.kfsDisclaimerText().getText();
        Assertions.assertThat(kfsUpper).contains("Note: There could be a difference in the amount to be paid by the borrower mentioned in (v) and that in repayment schedule shared because of any rounding off of the instalment amount in the repayment schedule.");
        Assertions.assertThat(kfsUpper).contains("*No recovery agent is assigned for loan account. In case any recovery agent is assigned, then particulars of such recovery agent will be communicated before the recovery agent contacts for recovery.");
    }
//     @Owner(ROHIT_SHARMA)
//     @Feature("PGP-48507")
//     @Parameters({"theme"})
//     @Test(description = "Verify that KFS Table Data for hdfc emi dc",enabled = false)
    public void kfs_hdfc_dc_kfs_table_Verifivation(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.EmiInfo_COP).setTxnValue("2000")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.pause(4);
        checkoutPage.createAndInvokePaymode("EMI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        kfs(cashierPage,"4444333322221111");
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.kfsLink().click();
        cashierPage.waitUntilLoads();
        DriverManager.getDriver().switchTo().frame(cashierPage.kfs_frame());
        String kfsUpper = cashierPage.kfscloanAmount().getText();
        Assertions.assertThat(kfsUpper).contains("2000");
        String kfstenure = cashierPage.kfsctenure().getText();
        Assertions.assertThat(kfstenure).contains("3");
        String kfsinstallments = cashierPage.kfsinstallments().getText();
        Assertions.assertThat(kfsinstallments).contains("3");
    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-48507")
    @Parameters({"theme"})
    @Test(description = "Verify paybutton is  in disabled state if kfs consent is not clicked for ICICI emi dc using new kfs changes")
    public void kfs_icici_dc_disabled(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.EmiInfo_COP).setTxnValue("2000")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.pause(4);
        checkoutPage.createAndInvokePaymode("EMI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        kfs(cashierPage,"4572741654006328");
        cashierPage.buttonPGPayNow().assertNotClickable();
    }
//     @Owner(ROHIT_SHARMA)
//     @Feature("PGP-48507")
//     @Parameters({"theme"})
//     @Test(description = "Verify paybutton is  in disabled state if kfs consent is not clicked for HDFC emi dc using new kfs changes",enabled = false)
    public void kfs_hdfc_dc_disabled(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.EmiInfo_COP).setTxnValue("2000")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.pause(4);
        checkoutPage.createAndInvokePaymode("EMI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        kfs(cashierPage,"4444333322221111");
        cashierPage.buttonPGPayNow().assertNotClickable();
    }
//     @Owner(ROHIT_SHARMA)
//     @Feature("PGP-48507")
//     @Parameters({"theme"})
//     @Test(description = "Verify  txn of HDFC emi dc using new kfs changes", enabled = false)// Need to remove this case due to  new ICICI formate 
    public void kfs_hdfc_dc(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.EmiInfo_COP).setTxnValue("2000")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.pause(4);
        checkoutPage.createAndInvokePaymode("EMI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        kfs(cashierPage,"4444333322221111");
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.kfsLink().click();
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.kfscloseButton().click();
        cashierPage.buttonPGPayNow().assertClickable();
    }
//     @Owner(ROHIT_SHARMA)
//     @Feature("PGP-48507")
//     @Parameters({"theme"})
//     @Test(description = "Verify that KFS Disclaimer text should be displayed on KFS page for ICICI", enabled = false)// Need to remove this case due to  new ICICI formate 
    public void kfs_icici_dc_kfs_disclaimer_text(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.EmiInfo_COP).setTxnValue("2000")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.pause(4);
        checkoutPage.createAndInvokePaymode("EMI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        kfs(cashierPage,"4572741654006328");
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.kfsLink().click();
        cashierPage.waitUntilLoads();
        DriverManager.getDriver().switchTo().frame(cashierPage.kfs_frame());
        String kfsUpper = cashierPage.kfsDisclaimerText().getText();
        Assertions.assertThat(kfsUpper).contains("Note: There could be a difference in the amount to be paid by the borrower mentioned in (v) and that in repayment schedule shared because of any rounding off of the instalment amount in the repayment schedule.");
        Assertions.assertThat(kfsUpper).contains("*No recovery agent is assigned for loan account. In case any recovery agent is assigned, then particulars of such recovery agent will be communicated before the recovery agent contacts for recovery.");
    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-48507")
    @Parameters({"theme"})
    @Test(description = "Verify that KFS loan text should be displayed on KFS page for ICICI only")
    public void kfs_icici_dc_kfs_loan_consent_text(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.EmiInfo_COP).setTxnValue("2000")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.pause(4);
        checkoutPage.createAndInvokePaymode("EMI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        kfs(cashierPage,"4572741654006328");
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.kfsLink().click();
        cashierPage.waitUntilLoads();
        DriverManager.getDriver().switchTo().frame(cashierPage.kfs_frame());
        String kfsUpper = cashierPage.kfsLoanConsentText().getText();
        Assertions.assertThat(kfsUpper).contains("I Read And Agreed");
        Assertions.assertThat(kfsUpper).contains("I/We expressly authorize and give consent to ICICI Bank to, disclose,transfer or part with any of my/our information, (including location), or an other device information when ICICI Bank considers such disclosure as necessary,with:");
        Assertions.assertThat(kfsUpper).contains("Agents of ICICI Bank, ICICI Bank's group entities in any jurisdiction");
        Assertions.assertThat(kfsUpper).contains("Auditors, credit rating agencies/credit bureaus, statutory/regulatory authorities,governmental/administrative authorities,Central Know Your Customer(CKYC) registery or SEBI Know Your Client registration agency,having jurisdiction over ICICI Bank or its group entities;");
        Assertions.assertThat(kfsUpper).contains("Service providers,or such person with whom ICICI Bank contracts or puproses to contract;");
        Assertions.assertThat(kfsUpper).contains("(Collectively referred to as \"Permitted Persons\")");
        Assertions.assertThat(kfsUpper).contains("For the purposes of:");
        Assertions.assertThat(kfsUpper).contains("Provision of the facility and completion of non-onboarding formalities; or");
        Assertions.assertThat(kfsUpper).contains("Complying with KYC requirements; or");
        Assertions.assertThat(kfsUpper).contains("Compliance with applicable laws or any order (judicial or otherwise),statutory/regulatory requirement or;");
        Assertions.assertThat(kfsUpper).contains("for credit reveiew of facilities availed; or");
        Assertions.assertThat(kfsUpper).contains("Authentication or verification; or");
        Assertions.assertThat(kfsUpper).contains("research or analysis, credit reporting and scoring,risk management,participation in any telecommunication; or");
        Assertions.assertThat(kfsUpper).contains("electronic clearing network and for use or processing of the said information/data");
        Assertions.assertThat(kfsUpper).contains("Disclosing any default in payment, for the purposes of recovering such amounts.");
    }



}

