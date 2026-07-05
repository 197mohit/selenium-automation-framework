package scripts.Native.checkoutjs;

import com.paytm.CreateToken;
import com.paytm.LocalConfig;
import com.paytm.ServerConfigProvider;
import com.paytm.api.TxnStatus;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.api.nativeAPI.FetchPaymentOptionV5;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.api.theia.FetchQRPaymentDetails;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.*;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.FetchQRPaymentDetailsDTO.FetchQRPaymentDetailsDTO;
import com.paytm.dto.NativeDTO.InitTxn.*;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.checkoutjs.MerchantConfig;
import com.paytm.dto.checkoutjs.UserDetail;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.framework.core.DriverManager;
import com.paytm.framework.ui.element.UIElement;
import com.paytm.pages.*;
import com.paytm.utils.merchant.GiftVoucher;
import com.paytm.utils.merchant.intersections.MerchantUserIntersection;
import com.paytm.utils.merchant.merchant.util.Merchant;
import com.paytm.utils.merchant.merchant.util.Promo;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.*;

import static com.paytm.appconstants.Constants.MerchantType.*;
import static com.paytm.appconstants.Constants.Owner.*;
import static com.paytm.apphelpers.LogsValidationHelper.getLogsOnServer;

public class UISanityCheckoutJS extends CheckoutJsBase {
    private final static int INSTANT_DISCOUNT_PERCENTAGE = 5;
   // String ui_msg = "Please enter a valid VPA of the form username@bank";
      String new_ui_msg= "Invalid VPA, Try Again";
    //   String log_msg = "Invalid UPI address";


    @Owner(MAYURI)
    @Parameters({"theme"})
    @Test(description = "Successful txn using PPBL ")
    public void ppbblSuccessTxn(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.AddnPay;
        User user = userManager.getForWrite(PGPBaseTest.Label.PG2POSTPAIDUSER);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("2")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
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

    @Owner(MAYURI)
    @Parameters({"theme"})
    @Test(description = "Verify Login using phone number and otp")
    public void verifyLoginWithOTP(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.AUTOLOGIN);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.ADDnPAY_CCLIMIT).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutPage checkoutPage = new CheckoutJsCheckoutPage();
        MerchantConfig config = checkoutPage.loadMerchantConfigwithoutdeletingcookie(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.loginStrip().click();
        cashierPage.signin(user.mobNo(), user.mobNo());
        cashierPage.waitUntilLoads();
        Assertions.assertThat(cashierPage.verifyPaymentModeDisplayed(Constants.PayMode.WALLET));

    }

    @Owner(MAYURI)
    @Parameters({"theme"})
    @Test(description = "Verify Login using phone number and invalid otp")
    public void verifyLoginWithInvalidOTP(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.ADDnPAY_CCLIMIT).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutPage checkoutPage = new CheckoutJsCheckoutPage();
        MerchantConfig config = checkoutPage.loadMerchantConfigwithoutdeletingcookie(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.loginStrip().click();
        cashierPage.buttonSecureSignIn().click();
        cashierPage.fillLoginOtp("123");
        cashierPage.otpVerifyButton().click();
        cashierPage.waitUntilLoads();
        String expected_text = cashierPage.getErrorMessageInvalidOTP().getText();
        Assertions.assertThat(expected_text).isEqualTo("Please Enter Valid Otp");
    }

    @Owner(MAYURI)
    @Feature("PGP-33928")
    @Parameters({"theme"})
    @Test(description = "Successful txn using paytm postpaid")
    public void postpaidSuccessTxn(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.AddnPay;
        User user = userManager.getForWrite(Label.PG2POSTPAIDUSER);
        PostpaidHelpers.updateBalance("10");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("2")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.checkBoxPPI().click();
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

    @Owner(MAYURI)
    @Parameters({"theme"})
    @Test(description = "Validate the EMI paymode txn using HDFC card.")
    public void ValidateSuccessTxnUsingEMI(@Optional("checkoutjs_web_revamp") String theme) throws IOException, InterruptedException {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, EMI_DC_CC)
                .setTxnValue("2")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setEmiCard(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
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
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateCheckSum(EMI_DC_CC.getKey())
                .validateResponsePageParameters()
                .assertAll();
    }


    @Owner(MAYURI)
    @Parameters({"theme"})
    @Test(description = "verify Successful EMI_DC transaction")
    public void SuccessfulEmiDCTransactionUsingMobNoInInitiateAPI(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.EMIDC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), EMI)
                .setTxnValue("200")
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath iniJsonPath = initTxn.execute().jsonPath();
        String txnToken = iniJsonPath.getString("body.txnToken");
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        if (cashierPage.checkedPPIForCheckoutJS().isEnabled()) {
            cashierPage.checkedPPIForCheckoutJS().click();
        }
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setEmiCard(PaymentDTO.ICICI_DEBIT_CARD_NUMBER_EMI);
        paymentDTO.setBankName("ICICI");
        cashierPage.payBy(Constants.PayMode.EMI_DC, paymentDTO);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
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

    @Owner(MAYURI)
    @Parameters({"theme"})
    @Test(description = "Verfiy successfull NB txn using Checkout js flow")
    public void ValidateSuccessTxnUsingNB(@Optional("checkoutjs_web_revamp") String theme) throws IOException, InterruptedException {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, PGOnly_Retry)
                .build();
        PaymentDTO paymentDTO = new PaymentDTO();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
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
                .validateBankName(Constants.Bank.ICICINB.toString())
                .validateCheckSum(Constants.MerchantType.PGOnly_Retry.getKey())
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
                .validateGatewayName(Constants.Bank.ICICI.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(Constants.Bank.ICICINB.toString())
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("NB")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
    }

    @Owner(MAYURI)
    @Parameters({"theme"})
    @Test(description = "Validate validations on Expiry date and CVV for EMI ")
    public void ValidateEMI_invalidExpiryDate(@Optional("checkoutjs_web_revamp") String theme) throws IOException, InterruptedException {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, EMI_DC_CC)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        PaymentDTO paymentDTO = new PaymentDTO();
        cashierPage.tabEMI().click();
        cashierPage.pause(1);
        cashierPage.selectEMIBank(paymentDTO);
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_emiIframe());
        cashierPage.textBoxCardNumber().clearAndType(paymentDTO.getCreditCardNumber());
        cashierPage.pause(2);
        cashierPage.emiPlan().click();
        DriverManager.getDriver().switchTo().defaultContent();
        if (theme.equalsIgnoreCase(Constants.Theme.CHECKOUTJS_WEB_REVAMP) || theme.equalsIgnoreCase(Constants.Theme.CHECKOUTJS_WAP_REVAMP)) {
            cashierPage.proceedToConvertEMI().click();
        }
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_expCvv_cardIframe());
        cashierPage.textBoxExpiryMonthEMI().waitUntilEditable();
        cashierPage.textBoxExpiryMonthEMI().clearAndType("11");
        cashierPage.textBoxExpiryYearEMI().waitUntilEditable();
        cashierPage.textBoxExpiryYearEMI().clearAndType("11");
        cashierPage.error_invalidExpiryDate().assertVisible();

    }

    @Owner(MAYURI)
    @Parameters({"theme"})
    @Test(description = "Validate validations on Expiry date and CVV for EMI ")
    public void ValidateEMI_invalidCVV(@Optional("checkoutjs_web_revamp") String theme) throws IOException, InterruptedException {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, EMI_DC_CC)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.tabEMI().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_emiIframe());
        cashierPage.textBoxCardNumber().clearAndType(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.pause(2);

        cashierPage.proceedToSelectEmiPlan().waitUntilVisible();
        cashierPage.proceedToSelectEmiPlan().click();
        cashierPage.waitUntilLoads();
        DriverManager.getDriver().switchTo().defaultContent();
        if (theme.equalsIgnoreCase(Constants.Theme.CHECKOUTJS_WEB_REVAMP) || theme.equalsIgnoreCase(Constants.Theme.CHECKOUTJS_WAP_REVAMP)) {
            cashierPage.proceedToConvertEMI().click();
        }
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_expCvv_cardIframe());
        cashierPage.textBoxExpiryMonthEMI().waitUntilEditable();
        cashierPage.textBoxExpiryMonthEMI().clearAndType("12");
        cashierPage.textBoxExpiryYearEMI().waitUntilEditable();
        cashierPage.textBoxExpiryYearEMI().clearAndType("30");
        cashierPage.textBoxCVVNumber().clearAndType("00");
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.payButton().assertVisible();
        cashierPage.payButton().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_expCvv_cardIframe());
        String actual_text = cashierPage.getError_invalidCVV().getText();
        Assertions.assertThat(actual_text).isEqualTo("Please enter valid CVV");

    }

    @Owner(MAYURI)
    @Parameters({"theme"})
    @Test(description = "Validate Invalid Expiry date for CC")
    public void ValidateCC_invalidExpiryDate(@Optional("checkoutjs_web_revamp") String theme) throws IOException, InterruptedException {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, EMI_DC_CC)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        PaymentDTO paymentDTO = new PaymentDTO();
        cashierPage.tabCreditCard().waitUntilClickable();
        cashierPage.tabCreditCard().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_cardIframe());
        cashierPage.textBoxCardNumber().clearAndType(paymentDTO.getCreditCardNumber());
        cashierPage.pause(2);
        cashierPage.fillExpiryMonth("11");
        cashierPage.fillExpiryYear("2011");
        cashierPage.textBoxCVVNumber().clearAndType(paymentDTO.getCvvNumber());
        cashierPage.error_invalidExpiryDate().assertVisible();

    }

    @Owner(MAYURI)
    @Parameters({"theme"})
    @Test(description = "Validate Invalid CVV for CC")
    public void ValidateCC_invalidCVV(@Optional("checkoutjs_web_revamp") String theme) throws IOException, InterruptedException {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, EMI_DC_CC)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        PaymentDTO paymentDTO = new PaymentDTO();
        cashierPage.tabCreditCard().waitUntilClickable();
        cashierPage.tabCreditCard().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_cardIframe());
        cashierPage.textBoxCardNumber().clearAndType(paymentDTO.getCreditCardNumber());
        cashierPage.pause(2);
        cashierPage.fillExpiryMonth("12");
        cashierPage.fillExpiryYear("2030");
        cashierPage.textBoxCVVNumber().clearAndType("00");
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.payButton().assertVisible();
        cashierPage.payButton().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_cardIframe());
        String actual_text = cashierPage.getError_invalidCVV().getText();
        Assertions.assertThat(actual_text).isEqualTo("Please enter valid CVV");
    }

    @Owner(MAYURI)
    @Parameters({"theme"})
    @Test(description = "Verfiy successfull Wallet txn using Checkout js flow on cashier page login")
    public void ValidateSuccessTxnUsingWallet(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.ADDnPAY_CCLIMIT;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setTxnValue("1.00")
                .build();
        User user = userManager.getForWrite(Label.LOGIN);
        WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()));
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.loginStrip().click();
        cashierPage.login("9818686101", "888888");
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
                .validateGatewayName("WALLET")
                .validateBankName("WALLET")
                .validateCheckSum(merchantType.getKey())
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
                .validateGatewayName("WALLET")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName("WALLET")
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("PPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
    }

    @Owner(MAYURI)
    @Parameters({"theme"})
    @Feature("PGPUI-1319")
    @Test(description = "Verfiy successfull saved vpa txn using Checkout js flow")
    public void validateSuccessTxnUsingSavedVPA(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.SAVEDVPA);
        Constants.MerchantType merchantType = Constants.MerchantType.PPBLC_ONLY;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("5")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        WalletHelpers.modifyBalance(user, 0.0);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.tabSavedUPI(1);
        cashierPage.payBy(Constants.PayMode.SAVED_UPI);
        Assert.assertFalse(cashierPage.waitForNewWindow(2), "New window is not open");
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
                .validateGatewayName("PPBLC")
                .validateCheckSum(merchantType.getKey())
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("PPBLC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("UPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }


    @Owner(MAYURI)
    @Parameters({"theme"})
    @Test(description = "Success Txn of addNpay using CC and wallet")
    public void SuccessfuladdNpayCCTxn(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_ADDNPAY;
        User user = userManager.getForWrite(Label.LOGIN);
        WalletHelpers.modifyBalance(user, 2.00);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("3")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        String paymentFlow = "ADDANDPAY";
        String paymentFlowFPO = fetchPaymentOptionsJson.getString("body.paymentFlow");
        Assertions.assertThat(paymentFlowFPO).describedAs("Payment flow is not addNpay").isEqualTo(paymentFlow);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        if (cashierPage.uncheckedPPIForCheckoutJS().isSelected()) {
            cashierPage.uncheckedPPIForCheckoutJS().click();
        }
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
                .assertAll();
    }

    @Owner(MAYURI)
    @Parameters({"theme"})
    @Test(description = "Success Txn of addNpay using NB and wallet")
    public void SuccessfuladdNpayNBTxn(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_ADDNPAY;
        User user = userManager.getForWrite(Label.LOGIN);
        WalletHelpers.modifyBalance(user, 2.00);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("3")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        String paymentFlow = "ADDANDPAY";
        String paymentFlowFPO = fetchPaymentOptionsJson.getString("body.paymentFlow");
        Assertions.assertThat(paymentFlowFPO).describedAs("Payment flow is not addNpay").isEqualTo(paymentFlow);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        if (cashierPage.uncheckedPPIForCheckoutJS().isSelected()) {
            cashierPage.uncheckedPPIForCheckoutJS().click();
        }
        cashierPage.tabNetBanking().click();
        cashierPage.waitUntilLoads();
        cashierPage.tabNetBanking().click();
        cashierPage.dropdownNB().selectByValue("ICICI");
        cashierPage.pause(3);
        PaymentDTO paymentDTO = new PaymentDTO().setBankName("ICICI");
        cashierPage.payBy(Constants.PayMode.NB, paymentDTO);
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

    @Owner(MAYURI)
    @Parameters({"theme"})
    @Test(description = "Verfiy successfull UPI Subscription using Checkout js flow")
    public void ValidateSuccessUPISubscription(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchant = SUBS_UI_TEXT;
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
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
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
                .validateCheckSum(Constants.MerchantType.SUBS_UI_TEXT.getKey())
                .assertAll();
    }

    @Owner(MAYURI)
    @Parameters({"theme"})
    @Test(description = "Verfiy successfull CC txn using Checkout js flow")
    public void ValidateSuccessTxnUsingCC(@Optional("checkoutjs_web_revamp") String theme) throws IOException, InterruptedException {
        Constants.MerchantType merchantType = NATIVE_HYBRID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());

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
                .validateCheckSum(merchantType.getKey())
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Owner(MAYURI)
    @Parameters({"theme"})
    @Test(description = "Verfiy successfull DC txn using Checkout js flow")
    public void ValidateSuccessTxnUsingDC(@Optional("checkoutjs_web_revamp") String theme) throws IOException, InterruptedException {
        Constants.MerchantType merchantType = COBRANDED_DEPRIORITISE_DC;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());

        cashierPage.payBy(Constants.PayMode.DC);
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
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateCheckSum(merchantType.getKey())
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
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
    }

    @Owner(MAYURI)
    @Parameters({"theme"})
    @Test(description = "Verfiy successfull UPI txn using Checkout js flow")
    public void ValidateSuccessTxnUsingUPI(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PG2_COMMON_MERCHANT;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
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
                .validateCheckSum(merchantType.getKey())
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

    private void validateCommission(SoftAssertions softAssert, CashierPage cashierPage, double baseAmount, double percentCommission, double flatCommission, String paymentMode) {
        double actualChargeFeeAmt;
        double actualTotalAmt;

        double expectedChargeFeeAmt = convenienceFeeCalculator(baseAmount, percentCommission, flatCommission, paymentMode);
        double expectedTotalAmt = CommonHelpers.doubleHalfUpConvertor(baseAmount + expectedChargeFeeAmt);

        actualChargeFeeAmt = Double.valueOf(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(cashierPage.chargeFeeAmtPG().getText()));
        actualTotalAmt = Double.valueOf(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(cashierPage.totalAmtPG().getText()));


        softAssert.assertThat(actualChargeFeeAmt).as(paymentMode).isEqualTo(expectedChargeFeeAmt);
        softAssert.assertThat(actualTotalAmt).as(paymentMode).isEqualTo(expectedTotalAmt);
    }

    @Owner(MAYURI)
    @Parameters({"theme"})
    @Test(description = "Verfiy PCF Amount for Net Banking in Checkout js flow")
    public void validatePCFforNB(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.FLAT_PCF)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setBankName("ICICI");
        cashierPage.tabNetBanking().click();
        cashierPage.dropdownNB().selectByValue(paymentDTO.getBankName());
        SoftAssertions softAssert = new SoftAssertions();
        double expectedChargeFeeAmt = convenienceFeeCalculator(Double.valueOf(initTxnDTO.txnAmountFromBody()), 0, 4.72, "NB");
        cashierPage.pause(2);
        validateCommission(softAssert, cashierPage, Double.valueOf(initTxnDTO.txnAmountFromBody()), 0, 4.72, "NB");
        softAssert.assertAll();
        cashierPage.buttonPGPayNow().click();
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
                .validateBankName(Constants.Bank.ICICINB.toString())
                .validateCheckSum(Constants.MerchantType.FLAT_PCF.getKey())
                .validateChargeAmount(Double.toString(expectedChargeFeeAmt))
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
                .validateGatewayName(Constants.Bank.ICICI.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(Constants.Bank.ICICINB.toString())
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("NB")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
    }

    @Owner(MAYURI)
    @Parameters({"theme"})
    @Test(description = "Verfiy PCF Amount for Wallet Paymode in Checkout js flow")
    public void ValidatePCFwithWallet(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.WALLETOnly_PCF)
                .setTxnValue("1")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        WalletHelpers.modifyBalance(user, 5.00);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.pause(2);
        cashierPage.checkBoxPPI().assertVisible();
        String paybutton_text = cashierPage.payButtonConvFeeMsg().getText();
        Assertions.assertThat(paybutton_text).isEqualTo("It includes Convenience charges");
        cashierPage.payButton().assertVisible();
        cashierPage.buttonPGPayNow().click();
        ResponsePage responsePage = new ResponsePage();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
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
                .validateGatewayName("WALLET")
                .validateBankName("WALLET")
                .validateCheckSum(Constants.MerchantType.WALLETOnly_PCF.getKey())
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
                .validateGatewayName("WALLET")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName("WALLET")
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("PPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
    }

    @Owner(MAYURI)
    @Parameters({"theme"})
    @Test(description = "Verfiy PCF Amount for HDFC Debit Card in Checkout js flow")
    public void ValidatePCFwithDebitCard(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = POSTCONV_DEFAULT;

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setDebitCardNumber(PaymentDTO.DEBIT_CARD_NUMBER);
        cashierPage.tabDebitCard().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_cardIframe());
        cashierPage.textBoxCardNumber().clearAndType(paymentDTO.DEBIT_CARD_NUMBER);
        cashierPage.fillExpiryMonth("12");
        cashierPage.fillExpiryYear("2030");
        cashierPage.textBoxCVVNumber().clearAndType("123");
        double expectedChargeFeeAmt = convenienceFeeCalculator(Double.valueOf(initTxnDTO.txnAmountFromBody()), 1, 0, "DC");
        DriverManager.getDriver().switchTo().defaultContent();

        String cashier_text = cashierPage.convFeeMessageCashierPage().getText();
        Assertions.assertThat(cashier_text).isEqualTo("₹" + expectedChargeFeeAmt + " Convenience feewill be charged");
        String paybutton_text = cashierPage.payButtonConvFeeMsg().getText();
        Assertions.assertThat(paybutton_text).isEqualTo("It includes Convenience charges");
        cashierPage.payButton().assertVisible();
        cashierPage.buttonPGPayNow().click();
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
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateCheckSum(merchantType.getKey())
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
                .validateGatewayName(Constants.Bank.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
    }

    @Owner(MAYURI)
    @Parameters({"theme"})
    @Test(description = "Verfiy PCF Amount for PPBL in Checkout js flow")
    public void ValidatePCFwithPPBL(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.PG2POSTPAIDUSER);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.POSTCONV_DEFAULT)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        DriverManager.getDriver().switchTo().frame(cashierPage.ppbl_passcodeIframe());
        cashierPage.textBoxPPBLPassCode().sendKeys("1234");
        DriverManager.getDriver().switchTo().defaultContent();
        double expectedChargeFeeAmt = convenienceFeeCalculator(Double.valueOf(initTxnDTO.txnAmountFromBody()), 1, 0, "DC");
        String cashier_text = cashierPage.convFeeMessageCashierPage().getText();
        Assertions.assertThat(cashier_text).isEqualTo("₹" + expectedChargeFeeAmt + " Convenience feewill be charged");
        String paybutton_text = cashierPage.payButtonConvFeeMsg().getText();
        Assertions.assertThat(paybutton_text).isEqualTo("It includes Convenience charges");
        cashierPage.payButton().assertVisible();
        cashierPage.buttonPGPayNow().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("NB")
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_SUCCESS.getRespMsg())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.PPBL.toString())
                .validateBankName(Constants.Gateway.PPBL.toString())
                .assertAll();
    }


    @Owner(MAYURI)
    @Parameters({"theme"})
    @Test(description = "Validate the successful transaction with NB paymode when simplifiedPaymentOffers Discount promo applied")
    public void validateBankOffersSimplifiedFlowforNBTxnDiscountPromo(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType pwpDefault = Constants.MerchantType.NATIVE_HYBRID;
        for (int i = 0; i < 2; i++) {
            Promo promo = new Promo();
            new Merchant(pwpDefault.getId(), true).getPromos().add(promo);
        }
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode(Constants.MessageAssert.PROMO_DISCOUNT.toString()).setApplyAvailablePromo("false").setValidatePromo("true");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", pwpDefault, simplifiedPaymentOffers)
                .setTxnValue("10.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        fetchPaymentOptionsDTO.getHead().setWorkFlow("checkout");
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.simplifiedPaymentOffers.promoCode")).isEqualTo("discount");
        Assertions.assertThat(fetchPaymentOptionsJson.getBoolean("body.simplifiedPaymentOffers.applyAvailablePromo")).isFalse();
        Assertions.assertThat(fetchPaymentOptionsJson.getBoolean("body.simplifiedPaymentOffers.validatePromo")).isTrue();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.tabNetBanking().click();
        cashierPage.pause(2);
        Double discountedAmt = Double.parseDouble(initTxnDTO.txnAmountFromBody()) * (1.00 - INSTANT_DISCOUNT_PERCENTAGE * 0.01);
        String finalAmt = CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(String.valueOf(discountedAmt));
        Assertions.assertThat(cashierPage.buttonPGPayNow().getText()).isEqualToIgnoringCase("Pay ₹" + finalAmt);
        cashierPage.payBy(Constants.PayMode.NB, new PaymentDTO().setBankName("ICICI"));
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
                .validateTxnAmount(String.valueOf(discountedAmt))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
    }

    @Owner(MAYURI)
    @Parameters({"theme"})
    @Test(description = "Validate the successful transaction with NB paymode when simplifiedPaymentOffers")
    public void validateBankOffersSimplifiedFlowforNBTxnSpecificCashbackPromo(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.PG2POSTPAIDUSER);
        Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_HYBRID;
        Promo promo = new Promo();
        for (int i = 0; i < 2; i++) {
            new Merchant(merchantType.getId(), true).getPromos().add(promo);
        }
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode(promo.getName()).setApplyAvailablePromo("true").setValidatePromo("true");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType, simplifiedPaymentOffers)
                .setTxnValue("10.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        if (cashierPage.checkBoxPPI().isChecked()) {
            cashierPage.checkBoxPPI().unCheck();
        }
        Double discountedAmt = Double.parseDouble(initTxnDTO.txnAmountFromBody()) * (1.00 - INSTANT_DISCOUNT_PERCENTAGE * 0.01);
        String DA = CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(String.valueOf(discountedAmt));
        String discount = CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(String.valueOf(Double.parseDouble(initTxnDTO.txnAmountFromBody()) - discountedAmt));
        cashierPage.tabNetBanking().click();
        String buttonText = new UIElement(By.xpath("//*[@id='checkout-button']/button"), "cashier-page", "pay-button").getText();
        cashierPage.payButtonPromoText().waitUntilVisible();
        cashierPage.validateButtonText(DA, discount);
//        Assertions.assertThat(buttonText).isEqualTo("Effective price after offer ₹"+DA);
//        Assertions.assertThat(buttonText).isEqualTo("(Effective price: ₹ "+DA+" with ₹ "+discount+" cashback)");
        cashierPage.payBy(Constants.PayMode.NB, new PaymentDTO().setBankName("ICICI"));
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
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();

    }

    @Owner(MAYURI)
    @Parameters({"theme"})
    @Test(description = "Validate the successful transaction with EMI paymode when simplifiedPaymentOffers")
    public void validateBankOffersSimplifiedFlowforEMITxnWithoutSpecificPromo(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.PG2POSTPAIDUSER);
        Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_HYBRID;
        Promo promo = new Promo();
        for (int i = 0; i < 2; i++) {
            new Merchant(merchantType.getId(), true).getPromos().add(promo);
        }
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode("").setApplyAvailablePromo("true").setValidatePromo("true");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType, simplifiedPaymentOffers)
                .setTxnValue("10.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        if (cashierPage.checkBoxPPI().isChecked()) {
            cashierPage.checkBoxPPI().unCheck();
        }
        cashierPage.tabEMI().click();
        cashierPage.pause(1);

        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_emiIframe());
        cashierPage.textBoxCardNumber().clearAndType(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.pause(2);
        cashierPage.proceedToSelectEmiPlan().waitUntilVisible();
        cashierPage.proceedToSelectEmiPlan().click();
        cashierPage.waitUntilLoads();
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.proceedToConvertEMI().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_expCvv_cardIframe());
        cashierPage.textBoxExpiryMonthEMI().waitUntilEditable();
        cashierPage.textBoxExpiryMonthEMI().clearAndType("12");
        cashierPage.textBoxExpiryYearEMI().waitUntilEditable();
        cashierPage.textBoxExpiryYearEMI().clearAndType("30");
        cashierPage.textBoxCVVNumber().clearAndType("123");
        DriverManager.getDriver().switchTo().defaultContent();


        Double discountedAmt = Double.parseDouble(initTxnDTO.txnAmountFromBody()) * (1.00 - INSTANT_DISCOUNT_PERCENTAGE * 0.01);
        String DA = CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(String.valueOf(discountedAmt));
        String discount = CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(String.valueOf(Double.parseDouble(initTxnDTO.txnAmountFromBody()) - discountedAmt));
        String buttonText = new UIElement(By.xpath("//*[@id='checkout-button']/button"), "cashier-page", "pay-button").getText();
        //        Assertions.assertThat(buttonText).isEqualTo("Effective price after offer ₹"+DA);
//        Assertions.assertThat(buttonText).isEqualTo("(Effective price: ₹ "+DA+" with ₹ "+discount+" cashback)");
        cashierPage.validateButtonText(DA, discount);
        cashierPage.payButtonPromoText().waitUntilVisible();

        cashierPage.payButton().assertVisible();
        cashierPage.payButton().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();

    }

    @Owner(MAYURI)
    @Parameters({"theme"})
    @Test(description = "Validate the successful transaction with CC paymode when simplifiedPaymentOffers")
    public void validateBankOffersSimplifiedFlowforCCTxnSpecificCashbackPromo(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.PPBL);
        Constants.MerchantType merchantType = NATIVE_HYBRID;
        Promo promo = new Promo();
        for (int i = 0; i < 1; i++) {
            new Merchant(merchantType.getId(), true).getPromos().add(promo);
        }
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode(promo.getName()).setApplyAvailablePromo("true").setValidatePromo("true");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType, simplifiedPaymentOffers)
                .setTxnValue("10.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        WalletHelpers.modifyBalance(user, 0.00);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        Double discountedAmt = Double.parseDouble(initTxnDTO.txnAmountFromBody()) * (1.00 - INSTANT_DISCOUNT_PERCENTAGE * 0.01);
        String DA = CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(String.valueOf(discountedAmt));
        String discount = CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(String.valueOf(Double.parseDouble(initTxnDTO.txnAmountFromBody()) - discountedAmt));
        cashierPage.tabCreditCard().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_cardIframe());
        cashierPage.textBoxCardNumber().clearAndType(new PaymentDTO().getCreditCardNumber());
        DriverManager.getDriver().switchTo().defaultContent();
        String promoTxt = new UIElement(By.xpath("//div[contains(@class,'ptm-message ptm-bo-cashtrip ')]"), "cashier-page", "promo text").getText();
        Assertions.assertThat(promoTxt).contains("cashback applicable");
        //String buttonText = new UIElement(By.xpath("//*[@id='checkout-button']/button"), "cashier-page", "pay-button").getText();//span
        //cashierPage.payButtonPromoText().waitUntilVisible();
        //cashierPage.validateButtonText(DA, discount);
//        Assertions.assertThat(buttonText).isEqualTo("Effective price after offer ₹"+DA);
//        Assertions.assertThat(buttonText).isEqualTo("(Effective price: ₹ "+DA+" with ₹ "+discount+" cashback)");
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
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateTxnDate(new Date())
                .assertAll();

    }

    @Owner(MAYURI)
    @Parameters({"theme"})
    @Test(description = "Validate the successful transaction with UPI paymode when simplifiedPaymentOffers Discount promo applied")
    public void validateBankOffersSimplifiedFlowforUPITxnDiscountPromo(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType pwpDefault = Constants.MerchantType.NATIVE_HYBRID;
        for (int i = 0; i < 2; i++) {
            Promo promo = new Promo();
            new Merchant(pwpDefault.getId(), true).getPromos().add(promo);
        }
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode(Constants.MessageAssert.PROMO_DISCOUNT.toString()).setApplyAvailablePromo("false").setValidatePromo("true");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", pwpDefault, simplifiedPaymentOffers)
                .setTxnValue("10.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        fetchPaymentOptionsDTO.getHead().setWorkFlow("checkout");
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.simplifiedPaymentOffers.promoCode")).isEqualTo("discount");
        Assertions.assertThat(fetchPaymentOptionsJson.getBoolean("body.simplifiedPaymentOffers.applyAvailablePromo")).isFalse();
        Assertions.assertThat(fetchPaymentOptionsJson.getBoolean("body.simplifiedPaymentOffers.validatePromo")).isTrue();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.tabNetBanking().click();
        cashierPage.pause(2);
        Double discountedAmt = Double.parseDouble(initTxnDTO.txnAmountFromBody()) * (1.00 - INSTANT_DISCOUNT_PERCENTAGE * 0.01);
        String finalAmt = CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(String.valueOf(discountedAmt));
        Assertions.assertThat(cashierPage.buttonPGPayNow().getText()).isEqualToIgnoringCase("Pay ₹" + finalAmt);
        cashierPage.payBy(Constants.PayMode.NB, new PaymentDTO().setBankName("ICICI"));
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
                .validateTxnAmount(String.valueOf(discountedAmt))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
    }

    @Owner(MAYURI)
    @Parameters({"theme"})
    @Test(description = "Validate the successful transaction with PPBL paymode when simplifiedPaymentOffers Discount promo applied")
    public void validateBankOffersSimplifiedFlowforPPBLTxnDiscountPromo(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType pwpDefault = Constants.MerchantType.NATIVE_HYBRID;
        for (int i = 0; i < 2; i++) {
            Promo promo = new Promo();
            new Merchant(pwpDefault.getId(), true).getPromos().add(promo);
        }
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode(Constants.MessageAssert.PROMO_DISCOUNT.toString()).setApplyAvailablePromo("false").setValidatePromo("true");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", pwpDefault, simplifiedPaymentOffers)
                .setTxnValue("10.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        fetchPaymentOptionsDTO.getHead().setWorkFlow("checkout");
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.simplifiedPaymentOffers.promoCode")).isEqualTo("discount");
        Assertions.assertThat(fetchPaymentOptionsJson.getBoolean("body.simplifiedPaymentOffers.applyAvailablePromo")).isFalse();
        Assertions.assertThat(fetchPaymentOptionsJson.getBoolean("body.simplifiedPaymentOffers.validatePromo")).isTrue();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.tabNetBanking().click();
        cashierPage.pause(2);
        Double discountedAmt = Double.parseDouble(initTxnDTO.txnAmountFromBody()) * (1.00 - INSTANT_DISCOUNT_PERCENTAGE * 0.01);
        String finalAmt = CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(String.valueOf(discountedAmt));
        Assertions.assertThat(cashierPage.buttonPGPayNow().getText()).isEqualToIgnoringCase("Pay ₹" + finalAmt);
        cashierPage.payBy(Constants.PayMode.NB, new PaymentDTO().setBankName("ICICI"));
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
                .validateTxnAmount(String.valueOf(discountedAmt))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
    }


    @Owner(MAYURI)
    @Parameters({"theme"})
    @Test(description = "Validate the successful transaction with Wallet paymode when simplifiedPaymentOffers Discount promo applied")
    public void validateBankOffersSimplifiedFlowforWalletTxnDiscountPromo(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType pwpDefault = Constants.MerchantType.NATIVE_HYBRID;
        for (int i = 0; i < 2; i++) {
            Promo promo = new Promo();
            new Merchant(pwpDefault.getId(), true).getPromos().add(promo);
        }
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode(Constants.MessageAssert.PROMO_DISCOUNT.toString()).setApplyAvailablePromo("false").setValidatePromo("true");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", pwpDefault, simplifiedPaymentOffers)
                .setTxnValue("10.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        fetchPaymentOptionsDTO.getHead().setWorkFlow("checkout");
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.simplifiedPaymentOffers.promoCode")).isEqualTo("discount");
        Assertions.assertThat(fetchPaymentOptionsJson.getBoolean("body.simplifiedPaymentOffers.applyAvailablePromo")).isFalse();
        Assertions.assertThat(fetchPaymentOptionsJson.getBoolean("body.simplifiedPaymentOffers.validatePromo")).isTrue();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.tabNetBanking().click();
        cashierPage.pause(2);
        Double discountedAmt = Double.parseDouble(initTxnDTO.txnAmountFromBody()) * (1.00 - INSTANT_DISCOUNT_PERCENTAGE * 0.01);
        String finalAmt = CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(String.valueOf(discountedAmt));
        Assertions.assertThat(cashierPage.buttonPGPayNow().getText()).isEqualToIgnoringCase("Pay ₹" + finalAmt);
        cashierPage.payBy(Constants.PayMode.NB, new PaymentDTO().setBankName("ICICI"));
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
                .validateTxnAmount(String.valueOf(discountedAmt))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
    }

    public void ui_vpa_msg(CashierPage cashierPage) throws Exception {
        cashierPage.textBoxVPA().waitUntilClickable();
        cashierPage.textBoxVPA().clearAndType("11invalid@pay11");
        cashierPage.payButton().click();
        String d1 = cashierPage.vpaerrormsg().getText();
        Assertions.assertThat(d1).isEqualTo(new_ui_msg);
    }

    @Owner(MAYURI)
    @Parameters({"theme"})
    @Test(description = "Verify error message:Please enter a valid UPI ID should be displayed after entering an invalid \"UPI ID\" and clicking on pay button for NONE payment flow")
    public void validate_vpa_msg(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = AddnPay;
        User user = userManager.getForRead(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("20.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabUPI().click();
        if (theme.equals(Constants.Theme.CHECKOUTJS_WAP_REVAMP)) {
            cashierPage.tabUPIId().click();
        }
        ui_vpa_msg(cashierPage);
    }

    public void theia_facade_vpa_msg(CashierPage cashierPage, String orderid) throws Exception {
        cashierPage.textBoxVPA().waitUntilVisible();
        cashierPage.textBoxVPA().clearAndType("invaliUPI@paytm");
        cashierPage.payButton().click();
        cashierPage.vpaerrormsg().waitUntilVisible();
        String d1 = cashierPage.vpaerrormsg().getText();
        Assertions.assertThat(d1).isEqualTo(Constants.MessageAssert.INVALID_VPA.toString());
        //response msg returned from UPI_SECURE Component
        String grepcmd = "grep \"" + orderid + "\" " + LocalConfig.THEIA_FACADE_LOGS +
                " | grep \"UPI_SECURE\" | " + "grep \"RESPONSE\"";
        String theiaFacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assertions.assertThat(theiaFacadeLogs).contains("\"" + Constants.MessageAssert.INVALID_UPI.toString() + "\"");
    }


    @Owner(MAYURI)
    @Parameters({"theme"})
    @Test(description = "Verify error message:Invalid UPI ID should be displayed after entering an invalid UPIID on UI and theia_facade logs as well for none payment flow")
    public void validate_vpa_msg_logs(@Optional("checkoutjs_wap_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = AddnPay;
        User user = userManager.getForRead(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("2.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        String orderid = initTxnDTO.orderFromBody();
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabUPI().click();
        if (theme.equals(Constants.Theme.CHECKOUTJS_WAP_REVAMP)) {
            cashierPage.tabUPIId().click();
        }
        theia_facade_vpa_msg(cashierPage, orderid);
    }

    @Owner(MAYURI)
    @Parameters({"theme"})
    @Test(description = "Verify error message:Please enter a valid UPI ID should be displayed after entering an invalid \"UPI ID\" and clicking on pay button for NONE payment flow")
    public void nonlogeedin_validate_vpa_msg(@Optional("checkoutjs_wap_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.AddnPay;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("2.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabUPI().click();
        if (theme.equals(Constants.Theme.CHECKOUTJS_WAP_REVAMP)) {
            cashierPage.tabUPIId().click();
        }
        ui_vpa_msg(cashierPage);
    }


    @Parameters("theme")
    @Owner(MAYURI)
    @Test(description = "validate upi numeric id error message when its passed as alphanumeric in checkoutJS.")
    public void upiNumericIdErrorWhenAlphanumericValuePassed(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.PG2_COMMON_MERCHANT)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.tabUPI().click();
        cashierPage.waitUntilLoads();
        cashierPage.UpiNumericId().sendKeys("12abcd");
        cashierPage.verifyUpiNumericID().click();
        Assert.assertEquals(cashierPage.vpaerrormsg().getText(), "Only numbers to be entered for UPI Number");

    }

    @Owner(MAYURI)
    @Parameters("theme")
    @Test(description = "validate upi numeric id error message when its passed as less than 10 digits in checkoutJS.")
    public void upiNumericIdErrorWhenLessthan10DigitsPassed(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, PGOnly_Retry)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.tabUPI().click();
        cashierPage.waitUntilLoads();
        cashierPage.UpiNumericId().click();
        cashierPage.UpiNumericId().sendKeys("12");
        cashierPage.verifyUpiNumericID().click();
        Assert.assertEquals(cashierPage.vpaerrormsg().getText(), "UPI Number can be 8 to 10 digit length only");

    }


    @Owner(MAYURI)
    @Parameters({"theme"})
    @Test(description = "Verfiy unsuccessfull txn when bank page is closed by user and there is zero retry allowed")
    public void validateZeroRetry(@Optional("checkoutjs_wap_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType).setTxnValue("33.33")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.payBy(Constants.PayMode.CC);
        cashierPage.closeChildWindow();
        cashierPage.waitUntilLoads();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validateRespCode("1006")
                .validateStatus("TXN_FAILURE")
                .assertAll();
    }

    @Owner(MAYURI)
    @Parameters({"theme"})
    @Test(description = "Verify success transaction of FD as Paymode")
    public void verifying_success_FD_txn(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.FD_PAYMODE;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("3001")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setPasscode("3315");
        cashierPage.payBy(Constants.PayMode.PPBL, paymentDTO);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
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
                .assertAll();
    }


    @Owner(MAYURI)
    @Parameters({"theme"})
    @Test(description = "Validate successfull bank mandate transaction on checkout flow")
    public void verifyBankmandateTxn(@Optional("checkoutjs_web_revamp") String theme) throws Exception {

        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.BANK_MANDATE;
        PaymentDTO paymentDTO = new PaymentDTO().setMandateAuthMode("Net Banking");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)       //Initiate subs request
                .setTxnValue("5")
                .setSubscriptionPaymentMode("BANK_MANDATE")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setMandateAccountDetails(new MandateAccountDetails())
                .build();
        InitTxnResponseDTO initTxnResponse = validateSuccessInitiateSubscription(initTxnDTO);
        String orderId = initTxnDTO.getBody().getOrderId();
        String txnToken = initTxnResponse.getBody().getTxnToken();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setOrderId(orderId);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        if (theme.equalsIgnoreCase(Constants.Theme.CHECKOUTJS_WAP_REVAMP) || theme.equalsIgnoreCase(Constants.Theme.CHECKOUTJS_WEB_REVAMP)) {
            cashierPage.tabBankMandate().click();
        }
        cashierPage.bankmandateAuthMode(paymentDTO.getMandateAuthMode()).click();
        cashierPage.buttonPGPayNow().click();
        BankMandatePage bankMandatePage = BankMandatePageFactory.getBankMandatePage(theme);
        bankMandatePage.confirmButton().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateOrderId(initTxnDTO.orderFromBody())
                .validateMid(merchant.getId())
                .validatePaymentMode(Constants.PayMode.BANK_MANDATE.toString())
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("3006")
                .validateRespMsg("SUCCESS")
                .validateGatewayName("PPBL")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .validateMandateType("E_MANDATE")
                .assertAll();

    }

    @Owner(MAYURI)
    @Parameters({"theme"})
    @Test(description = "Verfiy Retry breach in Checkout js flow with Merchant having NativeJsonRequest Pref set to F")
    public void validateRetryBreachTxnwithNativeJsonPrefN(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.PPBL_NB_PCF)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()));
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        SoftAssertions softAssert = new SoftAssertions();
        cashierPage.tabDebitCard().click();
        PaymentDTO paymentDetailsForRetry = new PaymentDTO().setCreditCardNumber(PaymentDTO.CREDIT_CARD_FOR_FAILED_TXN);
        cashierPage.payBy(Constants.PayMode.CC, paymentDetailsForRetry);
        cashierPage.waitUntilLoads();
        cashierPage.clickInvalidOTPEnteredButtonIfDisplayed();
        cashierPage.payBy(Constants.PayMode.CC);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateCheckSum(Constants.MerchantType.PPBL_NB_PCF.getKey())
                .assertAll();
    }

    @Owner(MAYURI)
    @Parameters({"theme"})
    @Test(description = "Validate successful AddMoney Transaction using DC")
    public void AddMoneyOnJsCheckoutWithDC(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.AddMoney;
        WalletHelpers.modifyBalance(user, 1.0);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("2")
                .setIsNativeAddMoney("true")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabDebitCard().click();
        cashierPage.payBy(Constants.PayMode.DC);
//        DirectBankOTPPage directBankOTPPage = new DirectBankOTPPage();
//        directBankOTPPage.waitUntilLoads();
//        directBankOTPPage.submitOtp("888888");
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("DC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
        WalletHelpers.validateBalance(user, 3);
    }


    @Owner(MAYURI)
    @Parameters({"theme"})
    @Test(description = "Validate successful AddMoney Transaction using NB")
    public void AddMoneyOnJsCheckoutWithNB(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.VPAENABLED);
        Constants.MerchantType merchant = Constants.MerchantType.AddMoney;
        WalletHelpers.modifyBalance(user, 1.0);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("2")
                .setIsNativeAddMoney("true")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PaymentDTO paymentDTO = new PaymentDTO();
        cashierPage.payBy(Constants.PayMode.NB, paymentDTO.setBankName("ICICI"));

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("NB")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
        WalletHelpers.validateBalance(user, 3);
    }

    @Owner(MAYURI)
    @Parameters({"theme"})
    @Test(description = " No EMI subvention offer visibility on the payment page for standard EMI")
    public void verifyEMISubventionStrip_ForStandardEMI(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.EMI;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("1")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setTokenType("TXN_TOKEN");
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body")).doesNotContain("emiSubventionBanks");
        cashierPage.getEMIStrip().assertNotVisible();
    }

    @Owner(MAYURI)
    @Parameters({"theme"})
    @Test(description = "success txn on checkoutjs for amount based low cost emi subvention+ promo discount")
    public void EMISubventionAmountBasedAndPromoSuccess(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.EMISubvention;
        Merchant merchant = new Merchant(merchantType.getId(), true);
        Promo promocode = new Promo(false);
        merchant.getPromos().add(promocode);
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode("discount").setApplyAvailablePromo("true").setValidatePromo("true");
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention("1234", null, "10", null);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType, simplifiedPaymentOffers)
                .setTxnValue("20")
                .setSimplifiedSubvention(simplifiedSubvention)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setEmiCard(paymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setTokenType("TXN_TOKEN");
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.EMI, paymentDTO);
        Double subventiondiscount = 2.0; // for amount based emi subvention discount is of 2 Rs
        Double discountedAmount = Double.valueOf(initTxnDTO.txnAmountFromBody()) * 5 / 100;
        Double finalTxnAmount = Double.valueOf(initTxnDTO.txnAmountFromBody()) - subventiondiscount - discountedAmount;
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

    @Owner(MAYURI)
    @Parameters({"theme"})
    @Test(description = "Verify successful TXN in checkout js for item based 0 cost emi subvention+ promo discount")
    public void EMISubCheckoutJsItembasedAndPromoSuccess(@Optional("checkoutjs_wap_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.EMI;
        Merchant merchant = new Merchant(merchantType.getId(), true);
        Promo promocode = new Promo(false);
        merchant.getPromos().add(promocode);
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
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setTokenType("TXN_TOKEN");
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setEmiCard(paymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        cashierPage.payBy(Constants.PayMode.EMI, paymentDTO);
        Double subventiondiscount = 1.0; // for item based emi subvention discount is of 1 Rs
        Double discountedAmount = Double.valueOf(initTxnDTO.txnAmountFromBody()) * 5 / 100;
        Double finalTxnAmount = Double.valueOf(initTxnDTO.txnAmountFromBody()) - subventiondiscount - discountedAmount;
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

    @Owner(MAYURI)
    @Parameters({"theme"})
    @Test(description = "EMI subvention offer visibility on the payment page based on emiSubventionBank for Amount based")
    public void verifyEMISubventionStrip_ForAmountBasedEMI(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.EMI;
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention("1234", null, "1", null);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("2")
                .setSimplifiedSubvention(simplifiedSubvention)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setTokenType("TXN_TOKEN");
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.emiSubventionBanks.emiTypes.type")).contains("SUBVENTION");
        Assertions.assertThat(cashierPage.getEMIStrip().getText()).isEqualTo(Constants.MessageAssert.ZERO_EMI.toString());


    }

    @Owner(MAYURI)
    @Parameters({"theme"})
    @Test(description = "success txn on checkoutjs for amount based low cost emi subvention")
    public void EMISubventionAmountBasedSuccess(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.EMISubvention;
        Merchant merchant = new Merchant(merchantType.getId(), true);
        Promo promocode = new Promo(false);
        merchant.getPromos().add(promocode);
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention("1234", null, "10", null);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setTxnValue("20")
                .setSimplifiedSubvention(simplifiedSubvention)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setEmiCard(paymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setTokenType("TXN_TOKEN");
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.EMI, paymentDTO);
        Double subventiondiscount = 2.0; // for amount based emi subvention discount is of 2 Rs
        Double finalTxnAmount = Double.valueOf(initTxnDTO.txnAmountFromBody()) - subventiondiscount;
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

    @Owner(MAYURI)
    @Parameters({"theme"})
    @Test(description = "Verify successful TXN in checkout js for item based 0 cost emi subvention")
    public void EMISubCheckoutJsItembasedSuccess(@Optional("checkoutjs_wap_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.EMI;
        Merchant merchant = new Merchant(merchantType.getId(), true);
        Promo promocode = new Promo(false);
        merchant.getPromos().add(promocode);
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
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setTokenType("TXN_TOKEN");
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setEmiCard(paymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        cashierPage.payBy(Constants.PayMode.EMI, paymentDTO);
        Double subventiondiscount = 1.0; // for item based emi subvention discount is of 1 Rs
        Double finalTxnAmount = Double.valueOf(initTxnDTO.txnAmountFromBody()) - subventiondiscount;
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

    @Owner(MAYURI)
    @Parameters({"theme"})
    @Test(description = "Verify UPI QR is present when only UPI is enabled")
    public void validateUPIQRpresentWhenUPIEnabled(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.QR_ENABLED_MERCHANT;
        EnablePaymentMode enablePaymentMode = new EnablePaymentMode(new String[]{}, "UPI");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setEnablePaymentMode(new EnablePaymentMode[]{enablePaymentMode}).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        String paymentModes = "body.merchantPayOption.paymentModes.paymentMode";
        Assertions.assertThat(fetchPaymentOptionsJson.getList(paymentModes)).containsOnly("UPI");
        String channelCodes = "body.merchantPayOption.paymentModes.find{it.paymentMode  == 'UPI'}.payChannelOptions.channelCode";
        Assertions.assertThat(fetchPaymentOptionsJson.getList(channelCodes)).containsOnly("UPIPUSH", "UPI", "UPIPUSHEXPRESS");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.imgScanPayQRCode().assertVisible();
        String QRText = cashierPage.qrCodeCheckoutJSText().getText();
        Assertions.assertThat(QRText).contains("Scan QR with Paytm");
        String orderId = initTxnDTO.getBody().getOrderId();
        String grepcmd = "grep \"" + orderId + "\" " + LocalConfig.THEIA_LOGS + " | grep \"CREATE_DYNAMIC_QR\"";
        String theiaLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assertions.assertThat(theiaLogs).contains("qrType=UPI_QR");
        Assertions.assertThat(theiaLogs).contains("Dynamic Qr is Processed Successfully");
    }

    @Owner(MAYURI)
    @Parameters({"theme"})
    @Test(description = "Verify UPI QR is not present when only UPIPUSH channel is enabled")
    public void validateUPIQRabsentWhenUPIPUSHEnabled(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.QR_ENABLED_MERCHANT;
        EnablePaymentMode enablePaymentMode = new EnablePaymentMode(new String[]{"UPIPUSH"}, "UPI");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setEnablePaymentMode(new EnablePaymentMode[]{enablePaymentMode}).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        String paymentModes = "body.merchantPayOption.paymentModes.paymentMode";
        Assertions.assertThat(fetchPaymentOptionsJson.getList(paymentModes)).containsOnly("UPI");
        String channelCodes = "body.merchantPayOption.paymentModes.find{it.paymentMode  == 'UPI'}.payChannelOptions.channelCode";
        Assertions.assertThat(fetchPaymentOptionsJson.getList(channelCodes)).containsOnly("UPIPUSH");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.imgScanPayQRCode().assertNotVisible();
        String orderId = initTxnDTO.getBody().getOrderId();
        String grepcmd = "grep \"" + orderId + "\" " + LocalConfig.THEIA_LOGS + " | grep \"CREATE_DYNAMIC_QR\"";
        String theiaLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assertions.assertThat(theiaLogs).contains("COMPLETED Task: CREATE_DYNAMIC_QR, Status: false");
        Assertions.assertThat(theiaLogs).doesNotContain("Dynamic Qr is Processed Successfully");
    }

    @Owner(MAYURI)
    @Parameters({"theme"})
    @Test(description = "Verify UPI QR is not present when only UPI channel is enabled")
    public void validateUPIQRabsentWhenUPIEnabled(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.QR_ENABLED_MERCHANT;
        EnablePaymentMode enablePaymentMode = new EnablePaymentMode(new String[]{"UPI"}, "UPI");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setEnablePaymentMode(new EnablePaymentMode[]{enablePaymentMode}).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        String paymentModes = "body.merchantPayOption.paymentModes.paymentMode";
        Assertions.assertThat(fetchPaymentOptionsJson.getList(paymentModes)).containsOnly("UPI");
        String channelCodes = "body.merchantPayOption.paymentModes.find{it.paymentMode  == 'UPI'}.payChannelOptions.channelCode";
        Assertions.assertThat(fetchPaymentOptionsJson.getList(channelCodes)).containsOnly("UPI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.imgScanPayQRCode().assertNotVisible();
        String orderId = initTxnDTO.getBody().getOrderId();
        String grepcmd = "grep \"" + orderId + "\" " + LocalConfig.THEIA_LOGS + " | grep \"CREATE_DYNAMIC_QR\"";
        String theiaLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assertions.assertThat(theiaLogs).contains("COMPLETED Task: CREATE_DYNAMIC_QR, Status: false");
        Assertions.assertThat(theiaLogs).doesNotContain("Dynamic Qr is Processed Successfully");
    }

    @Owner(MAYURI)
    @Parameters({"theme"})
    @Test(description = "Verify UPI QR is not present when only UPIPUSHEXPRESS channel is enabled")
    public void validateUPIQRabsentWhenUPIPUSHEXPRESSEnabled(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.QR_ENABLED_MERCHANT;
        EnablePaymentMode enablePaymentMode = new EnablePaymentMode(new String[]{"UPIPUSHEXPRESS"}, "UPI");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setEnablePaymentMode(new EnablePaymentMode[]{enablePaymentMode}).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        String paymentModes = "body.merchantPayOption.paymentModes.paymentMode";
        Assertions.assertThat(fetchPaymentOptionsJson.getList(paymentModes)).containsOnly("UPI");
        String channelCodes = "body.merchantPayOption.paymentModes.find{it.paymentMode  == 'UPI'}.payChannelOptions.channelCode";
        Assertions.assertThat(fetchPaymentOptionsJson.getList(channelCodes)).containsOnly("UPIPUSHEXPRESS");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.imgScanPayQRCode().assertNotVisible();
        String orderId = initTxnDTO.getBody().getOrderId();
        String grepcmd = "grep \"" + orderId + "\" " + LocalConfig.THEIA_LOGS + " | grep \"CREATE_DYNAMIC_QR\"";
        String theiaLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assertions.assertThat(theiaLogs).contains("COMPLETED Task: CREATE_DYNAMIC_QR, Status: false");
        Assertions.assertThat(theiaLogs).doesNotContain("Dynamic Qr is Processed Successfully");
    }

    @Owner(MAYURI)
    @Parameters({"theme"})
    @Test(description = "Verify UPI QR is present when UPI is disabled and UPIPUSH, UPIPUSHEXPRESS channels are enabled")
    public void validateUPIQRpresentWhenUPIDisabledUPIPUSHAndUPIPUSHEXPRESSEnabled(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.QR_ENABLED_MERCHANT;
        EnablePaymentMode enablePaymentMode = new EnablePaymentMode(new String[]{"UPIPUSH", "UPIPUSHEXPRESS"}, "UPI");
        DisablePaymentMode disablePaymentMode = new DisablePaymentMode(new String[]{"UPI"}, "UPI");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setEnablePaymentMode(new EnablePaymentMode[]{enablePaymentMode})
                .setDisablePaymentMode(new DisablePaymentMode[]{disablePaymentMode}).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        String paymentModes = "body.merchantPayOption.paymentModes.paymentMode";
        Assertions.assertThat(fetchPaymentOptionsJson.getList(paymentModes)).containsOnly("UPI");
        String channelCodes = "body.merchantPayOption.paymentModes.find{it.paymentMode  == 'UPI'}.payChannelOptions.channelCode";
        Assertions.assertThat(fetchPaymentOptionsJson.getList(channelCodes)).containsOnly("UPIPUSH", "UPIPUSHEXPRESS");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.imgScanPayQRCode().assertVisible();
        String QRText = cashierPage.qrCodeCheckoutJSText().getText();
        Assertions.assertThat(QRText).contains("Scan QR with Paytm");
        String orderId = initTxnDTO.getBody().getOrderId();
        String grepcmd = "grep \"" + orderId + "\" " + LocalConfig.THEIA_LOGS + " | grep \"CREATE_DYNAMIC_QR\"";
        String theiaLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assertions.assertThat(theiaLogs).contains("qrType=UPI_QR");
        Assertions.assertThat(theiaLogs).contains("Dynamic Qr is Processed Successfully");
    }

    @Owner(MAYURI)
    @Feature("PGP-48963")
    @Parameters({"theme"})
    @Test(description = "Verfiy successfull CC txn when wallet is not available on mid using Logged in Checkout js flow on cashier page login")
    public void ValidateSuccessTxnUsingCCWhenWalletIsNotAvailableOnMID(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.ONLY_CC;
        User user = userManager.getForWrite(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("2.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        PaymentDTO paymentDTO = new PaymentDTO();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_cardIframe());
        
        cashierPage.textBoxCardNumber().clearAndType(paymentDTO.getCreditCardNumber());
        cashierPage.fillExpiryMonth(paymentDTO.getExpMonth());
        cashierPage.fillExpiryYear(paymentDTO.getExpYear().substring(2));
        cashierPage.textBoxCVVNumber().waitUntilVisible();
        cashierPage.textBoxCVVNumber().clearAndType(paymentDTO.getCvvNumber());
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.pause(2);
        cashierPage.buttonPGPayNow().click();

        ResponsePage responsePage = new ResponsePage();
        Thread.sleep(10000);
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Owner(MAYURI)
    @Feature("PGP-48963")
    @Parameters({"theme"})
    @Test(description = "Verfiy successfull CC txn when wallet is not available on mid using Logged out Checkout js flow on cashier page login")
    public void ValidateSuccessTxnUsingCCWhenWalletIsNotAvailableOnMIDLoggedOut(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.ONLY_CC;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setTxnValue("2.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        PaymentDTO paymentDTO = new PaymentDTO();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_cardIframe());
        
        cashierPage.textBoxCardNumber().clearAndType(paymentDTO.getCreditCardNumber());
        cashierPage.fillExpiryMonth(paymentDTO.getExpMonth());
        cashierPage.fillExpiryYear(paymentDTO.getExpYear().substring(2));
        cashierPage.textBoxCVVNumber().waitUntilVisible();
        cashierPage.textBoxCVVNumber().clearAndType(paymentDTO.getCvvNumber());
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.pause(2);
        cashierPage.buttonPGPayNow().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }


//    @Owner(MAYURI)
//    @Feature("PGP-48963")
//    @Parameters({"theme"})
//    @Test(description = "Verfiy  Wallet  when mid is having wallet+UPI paymode using Checkout js flow on cashier page login", enabled = false)
    public void ValidateWalletWhenWalletAndUPIonMID(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.PG2WALLETUSER);
        Constants.MerchantType merchantType = Constants.MerchantType.WALLET_UPI_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("1.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        if (!cashierPage.uncheckedPPIForCheckoutJS().isEnabled()) {
            cashierPage.uncheckedPPIForCheckoutJS().click();
        }
        String text = cashierPage.PaymodeEnableDisableWalletMessage().getText();
        Assertions.assertThat(text).contains(Constants.MessageAssert.PAYTM_BALANCE.toString());


    }

    @Owner(MAYURI)
    @Feature("PGP-48963")
    @Parameters({"theme"})
    @Test(description = "Verfiy successfull UPI txn when mid is having wallet+UPI paymode using Checkout js flow on cashier page login")
    public void ValidateSuccessTxnUsingUPIWhenWalletAndUPIonMID(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.WALLET_UPI_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
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
                .validateCheckSum(merchantType.getKey())
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

    @Owner(MAYURI)
    @Feature("PGP-48963")
    @Parameters({"theme"})
    @Test(description = "Verfiy  Wallet  By entering phone number using Checkout js flow on cashier page login")
    public void ValidatWalletByEnteringPhoneNumber(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.LOGIN_STRIP_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.loginStrip().click();
        cashierPage.login("9818686101", "888888");
        cashierPage.waitUntilLoads();
        if (!cashierPage.uncheckedPPIForCheckoutJS().isEnabled()) {
            cashierPage.uncheckedPPIForCheckoutJS().click();
        }
        String text = cashierPage.PaymodeEnableDisableWalletMessage().getText();
        Assertions.assertThat(text).contains(Constants.MessageAssert.PAYTM_BALANCE.toString());
    }


    @Owner(MAYURI)
    @Feature("PGP-48963")
    @Parameters({"theme"})
    @Test(description = "Verfiy Wallet Paymode By entering phone number for Wallet only MID using Checkout js flow on cashier page login")
    public void ValidateWalletByEnteringPhoneNumberForWalletOnlyMID(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        Constants.MerchantType merchantType = WalletOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setTxnValue("1.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.loginStrip().click();
        cashierPage.login(user.mobNo(), "888888");
        cashierPage.waitUntilLoads();
        if (!cashierPage.uncheckedPPIForCheckoutJS().isEnabled()) {
            cashierPage.uncheckedPPIForCheckoutJS().click();
        }
        String text = cashierPage.PaymodeEnableDisableWalletMessage().getText();
        Assertions.assertThat(text).contains(Constants.MessageAssert.PAYTM_BALANCE.toString());

    }

    @Owner(MAYURI)
    @Feature("PGP-48963")
    @Parameters({"theme"})
    @Test(description = "Verfiy  Wallet Paymode by entering SSO for Wallet only MID using Checkout js flow on cashier page login")
    public void ValidateWalletByEnteringSSOForWalletOnlyMID(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.PG2WALLETUSER);
        Constants.MerchantType merchantType = WalletOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.waitUntilLoads();
        if (!cashierPage.uncheckedPPIForCheckoutJS().isEnabled()) {
            cashierPage.uncheckedPPIForCheckoutJS().click();
        }
        String text = cashierPage.PaymodeEnableDisableWalletMessage().getText();
        Assertions.assertThat(text).contains(Constants.MessageAssert.PAYTM_BALANCE.toString());

    }

//    @Owner(VAIBHAV)
//    @Feature("PGP-49595")
//    @Parameters({"theme"})
//    @Test(description = "To verify successful wallet 2FA transaction using wallet when passcode is already set on user in checkoutJs flow and amount is greater than 5k", enabled = false)
    public void validateWallet2FACorrectPasscode(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.WALLETBALANCE);
        Constants.MerchantType merchantType = LOGIN_STRIP_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("6500")
                .build();
        WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()));
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        DriverManager.getDriver().manage().deleteAllCookies();
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.waitUntilLoads();
        if (!cashierPage.uncheckedPPIForCheckoutJS().isEnabled()) {
            cashierPage.uncheckedPPIForCheckoutJS().click();
        }
        DriverManager.getDriver().switchTo().frame(cashierPage.ppbl_passcodeIframe());
        cashierPage.textBoxPPBLPassCode().sendKeys("1234");
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.payButton().assertVisible();
        cashierPage.buttonPGPayNow().click();
        ResponsePage responsePage = new ResponsePage();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
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
                .validateGatewayName("WALLET")
                .validateBankName("WALLET")
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
                .validateGatewayName("WALLET")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName("WALLET")
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("PPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
    }

//    @Owner(VAIBHAV)
//    @Feature("PGP-49595")
//    @Parameters({"theme"})
//    @Test(description = "To verify wallet 2FA transaction using wallet while passing incorrect passcode and passcode is already set on user in checkoutJs flow", enabled = false)
    public void validateWallet2FAIncorrectPasscode(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.WALLETBALANCE);
        Constants.MerchantType merchantType = LOGIN_STRIP_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("5001")
                .build();
        WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()));
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        DriverManager.getDriver().manage().deleteAllCookies();
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.waitUntilLoads();
        if (!cashierPage.uncheckedPPIForCheckoutJS().isEnabled()) {
            cashierPage.uncheckedPPIForCheckoutJS().click();
        }
        DriverManager.getDriver().switchTo().frame(cashierPage.ppbl_passcodeIframe());
        cashierPage.textBoxPPBLPassCode().sendKeys("3456");
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.payButton().assertVisible();
        cashierPage.buttonPGPayNow().click();
        ResponsePage responsePage = new ResponsePage();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        responsePage.waitUntilLoads();
        responsePage
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validateRespCode("810")
                .validateRespMsg("Payment failed due to a technical error. Please try after some time.")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .assertAll();
    }

//    @Owner(VAIBHAV)
//    @Feature("PGP-49595")
//    @Parameters({"theme"})
//    @Test(description = "To verify wallet 2FA Error message while clicking on pay button without entering passcode in checkoutJs flow", enabled = false)
    public void validateWallet2FAEmptyPasscode(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.WALLETBALANCE);
        Constants.MerchantType merchantType = LOGIN_STRIP_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("5001")
                .build();
        WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()));
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        DriverManager.getDriver().manage().deleteAllCookies();
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.waitUntilLoads();
        if (!cashierPage.uncheckedPPIForCheckoutJS().isEnabled()) {
            cashierPage.uncheckedPPIForCheckoutJS().click();
        }
        cashierPage.waitUntilLoads();
        cashierPage.buttonPpblSumbit().waitUntilClickable();
        cashierPage.buttonPpblSumbit().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ppbl_passcodeIframe());
        cashierPage.waitUntilLoads();
        String actual_text = cashierPage.verified2FAPasscodeErrorMsg().getText();
        System.out.println("*actual_text" + actual_text);
        Assertions.assertThat(actual_text).isEqualTo("Enter Passcode");
    }

//    @Owner(VAIBHAV)
//    @Feature("PGP-49595")
//    @Parameters({"theme"})
//    @Test(description = "To verify wallet 2FA transaction using wallet while passing incomplete passcode and passcode is already set on user in checkoutJs flow", enabled = false)
    public void validateWallet2FAIncompletePasscode(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.WALLETBALANCE);
        Constants.MerchantType merchantType = LOGIN_STRIP_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("5001")
                .build();
        WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()));
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        DriverManager.getDriver().manage().deleteAllCookies();
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.waitUntilLoads();
        if (!cashierPage.uncheckedPPIForCheckoutJS().isEnabled()) {
            cashierPage.uncheckedPPIForCheckoutJS().click();
        }
        DriverManager.getDriver().switchTo().frame(cashierPage.ppbl_passcodeIframe());
        cashierPage.textBoxPPBLPassCode().sendKeys("12");
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.payButton().assertVisible();
        cashierPage.buttonPGPayNow().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ppbl_passcodeIframe());
        cashierPage.waitUntilLoads();
        String actual_text = cashierPage.verified2FAIncorrectPasscodeErrorMsg().getText();
        System.out.println("*actual_text" + actual_text);
        Assertions.assertThat(actual_text).isEqualTo("Enter valid passcode");
    }

//    @Owner(VAIBHAV)
//    @Feature("PGP-49595")
//    @Parameters({"theme"})
//    @Test(description = "To verify wallet 2FA while clicking on Forgot Passcode button over cashier page without entering passcode and passcode is already set on user in checkoutJs flow", enabled = false)
    public void validateWallet2FAForgotPasscode(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.WALLETBALANCE);
        Constants.MerchantType merchantType = LOGIN_STRIP_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("5001")
                .build();
        WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()));
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        DriverManager.getDriver().manage().deleteAllCookies();
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.waitUntilLoads();
        if (!cashierPage.uncheckedPPIForCheckoutJS().isEnabled()) {
            cashierPage.uncheckedPPIForCheckoutJS().click();
        }
        cashierPage.waitUntilLoads();

        if (cashierPage.verified2FALinkForgotPasscode() != null) {
            cashierPage.verified2FALinkForgotPasscode().click();

        } else {
            System.out.println("not found");
        }
    }


//    @Owner(VAIBHAV)
//    @Feature("PGP-49595")
//    @Parameters({"theme"})
//    @Test(description = "To verify successful wallet 2FA transaction using wallet when passcode is already set on user in checkoutJs flow and amount is equal to 5k", enabled = false)
    public void validateWallet2FAWalletBalanceEqualTo5k(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.WALLETBALANCE);
        Constants.MerchantType merchantType = LOGIN_STRIP_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("5000")
                .build();
        WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()));
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        DriverManager.getDriver().manage().deleteAllCookies();
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.waitUntilLoads();
        if (!cashierPage.uncheckedPPIForCheckoutJS().isEnabled()) {
            cashierPage.uncheckedPPIForCheckoutJS().click();
        }
        DriverManager.getDriver().switchTo().frame(cashierPage.ppbl_passcodeIframe());
        cashierPage.textBoxPPBLPassCode().sendKeys("1234");
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.payButton().assertVisible();
        cashierPage.buttonPGPayNow().click();
        ResponsePage responsePage = new ResponsePage();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
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
                .validateGatewayName("WALLET")
                .validateBankName("WALLET")
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
                .validateGatewayName("WALLET")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName("WALLET")
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("PPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
    }



    @Owner(VAIBHAV)
    @Feature("PGP-52441")
    @Parameters({"theme"})
    @Test(description = "To verify successful wallet 2FA transaction using wallet when passcode is already set on user in checkoutJs flow and amount is greater than 5k after adding pcf fee")
    public void validateWallet2FACorrectPasscodeAmountGreaterThan5kAfterAddingPcfFee(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.WALLETBALANCE);
        Constants.MerchantType merchantType = TIP_AMOUNT;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("4885")
                .build();
        WalletHelpers.modifyBalance(user,5000.29);
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        DriverManager.getDriver().manage().deleteAllCookies();
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.waitUntilLoads();
        if (cashierPage.uncheckedPPIForCheckoutJS().isEnabled()) {
            cashierPage.uncheckedPPIForCheckoutJS().click();
        }
        DriverManager.getDriver().switchTo().frame(cashierPage.ppbl_passcodeIframe());
        cashierPage.textBoxPPBLPassCode().sendKeys("1234");
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.payButton().assertVisible();
        cashierPage.buttonPGPayNow().click();
        ResponsePage responsePage = new ResponsePage();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
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
                .validateGatewayName("WALLET")
                .validateBankName("WALLET")
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("WALLET")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName("WALLET")
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("PPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }



    @Owner(VAIBHAV)
    @Feature("PGP-52441")
    @Parameters({"theme"})
    @Test(description = "To verify wallet 2FA transaction using wallet while passing incorrect passcode and passcode is already set on user in checkoutJs flow and amount is greater than 5k after adding pcf fee")
    public void validateWallet2FAIncorrectPasscodeAmountGreaterThan5kAfterAddingPcfFee(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.WALLETBALANCE);
        Constants.MerchantType merchantType = TIP_AMOUNT;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("4885")
                .build();
        WalletHelpers.modifyBalance(user,5000.29);
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        DriverManager.getDriver().manage().deleteAllCookies();
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.waitUntilLoads();
        if (cashierPage.uncheckedPPIForCheckoutJS().isEnabled()) {
            cashierPage.uncheckedPPIForCheckoutJS().click();
        }
        DriverManager.getDriver().switchTo().frame(cashierPage.ppbl_passcodeIframe());
        cashierPage.textBoxPPBLPassCode().sendKeys("3456");
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.payButton().assertVisible();
        cashierPage.buttonPGPayNow().click();
        ResponsePage responsePage = new ResponsePage();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        responsePage.waitUntilLoads();
        responsePage
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validateRespCode("209")
                .validateRespMsg("Invalid Payment Details")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .assertAll();
    }



    @Owner(VAIBHAV)
    @Feature("PGP-52441")
    @Parameters({"theme"})
    @Test(description = "To verify wallet 2FA Error message while clicking on pay button without entering passcode in checkoutJs flow and amount is greater than 5k after adding pcf fee")
    public void validateWallet2FAEmptyPasscodeAmountGreaterThan5kAfterAddingPcfFee(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.WALLETBALANCE);
        Constants.MerchantType merchantType = TIP_AMOUNT;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("4885")
                .build();
        WalletHelpers.modifyBalance(user,5000.29);
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        DriverManager.getDriver().manage().deleteAllCookies();
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.waitUntilLoads();
        if (cashierPage.uncheckedPPIForCheckoutJS().isEnabled()) {
            cashierPage.uncheckedPPIForCheckoutJS().click();
        }
        cashierPage.waitUntilLoads();
        cashierPage.buttonPpblSumbit().waitUntilClickable();
        cashierPage.buttonPpblSumbit().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ppbl_passcodeIframe());
        cashierPage.waitUntilLoads();
        String actual_text = cashierPage.verified2FAPasscodeErrorMsg().getText();
        System.out.println("*actual_text" + actual_text);
        Assertions.assertThat(actual_text).isEqualTo("Enter Passcode");
    }



    @Owner(VAIBHAV)
    @Feature("PGP-52441")
    @Parameters({"theme"})
    @Test(description = "To verify wallet 2FA transaction using wallet while passing incomplete passcode and passcode is already set on user in checkoutJs flow and amount is greater than 5k after adding pcf fee")
    public void validateWallet2FAIncompletePasscodeAmountGreaterThan5kAfterAddingPcfFee(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.WALLETBALANCE);
        Constants.MerchantType merchantType = TIP_AMOUNT;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("4885")
                .build();
        WalletHelpers.modifyBalance(user,5000.29);
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        DriverManager.getDriver().manage().deleteAllCookies();
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.waitUntilLoads();
        if (cashierPage.uncheckedPPIForCheckoutJS().isEnabled()) {
            cashierPage.uncheckedPPIForCheckoutJS().click();
        }
        DriverManager.getDriver().switchTo().frame(cashierPage.ppbl_passcodeIframe());
        cashierPage.textBoxPPBLPassCode().sendKeys("12");
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.payButton().assertVisible();
        cashierPage.buttonPGPayNow().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ppbl_passcodeIframe());
        cashierPage.waitUntilLoads();
        String actual_text = cashierPage.verified2FAIncorrectPasscodeErrorMsg().getText();
        System.out.println("*actual_text" + actual_text);
        Assertions.assertThat(actual_text).isEqualTo("Enter valid passcode");
    }



    @Owner(VAIBHAV)
    @Feature("PGP-52441")
    @Parameters({"theme"})
    @Test(description = "To verify wallet 2FA while clicking on Forgot Passcode button over cashier page without entering passcode and passcode is already set on user in checkoutJs flow and amount is greater than 5k after adding pcf fee")
    public void validateWallet2FAForgotPasscodeAmountGreaterThan5kAfterAddingPcfFee(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.WALLETBALANCE);
        Constants.MerchantType merchantType = TIP_AMOUNT;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("4885")
                .build();
        WalletHelpers.modifyBalance(user,5000.29);
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        DriverManager.getDriver().manage().deleteAllCookies();
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.waitUntilLoads();
        if (cashierPage.uncheckedPPIForCheckoutJS().isEnabled()) {
            cashierPage.uncheckedPPIForCheckoutJS().click();
        }
        cashierPage.waitUntilLoads();

        if (cashierPage.verified2FALinkForgotPasscode() != null) {
            cashierPage.verified2FALinkForgotPasscode().click();

        } else {
            System.out.println("not found");
        }
    }



    @Owner(VAIBHAV)
    @Feature("PGP-52441")
    @Parameters({"theme"})
    @Test(description = "To verify successful wallet 2FA transaction using wallet when passcode is already set on user in checkoutJs flow and amount is equal to 5k after adding pcf fee")
    public void validateWallet2FAWalletBalanceEqualTo5kAfterAddingPcfFee(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.WALLETBALANCE);
        Constants.MerchantType merchantType = TIP_AMOUNT;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("4884.75")
                .build();
        WalletHelpers.modifyBalance(user,5000.29);
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        DriverManager.getDriver().manage().deleteAllCookies();
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.waitUntilLoads();
        if (cashierPage.uncheckedPPIForCheckoutJS().isEnabled()) {
            cashierPage.uncheckedPPIForCheckoutJS().click();
        }
        DriverManager.getDriver().switchTo().frame(cashierPage.ppbl_passcodeIframe());
        cashierPage.textBoxPPBLPassCode().sendKeys("1234");
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.payButton().assertVisible();
        cashierPage.buttonPGPayNow().click();
        ResponsePage responsePage = new ResponsePage();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
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
                .validateGatewayName("WALLET")
                .validateBankName("WALLET")
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("WALLET")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName("WALLET")
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("PPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }
    @Owner(PUSPA)
    @Feature("PGP-49206")
    @Parameters({"theme"})
    @Test(description = "Verify Card details page open when single paymode EMI is Present")
    public void verifySinglePaymodeEMI(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = EMI_DC_CC;
        EnablePaymentMode enablePaymentMode = new EnablePaymentMode(new String[]{"HDFC", "ICICI"}, "EMI");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setEnablePaymentMode(new EnablePaymentMode[]{enablePaymentMode}).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabEMI().assertNotVisible();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_emiIframe());
        cashierPage.textBoxCardNumberEMI().assertVisible();

    }

    @Owner(PUSPA)
    @Feature("PGP-49206")
    @Parameters({"theme"})
    @Test(description = "Verify Card details page open when single paymode CREDIT_CARD is Present")
    public void verifySinglePaymodeCC(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = EMI_DC_CC;
        EnablePaymentMode enablePaymentMode = new EnablePaymentMode(null, "CREDIT_CARD", new String[]{"HDFC", "ICICI"});
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setEnablePaymentMode(new EnablePaymentMode[]{enablePaymentMode}).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabCreditCard().assertNotVisible();
        DriverManager.getDriver().switchTo().frame(cashierPage.cc_dc_iframe());
        cashierPage.textBoxCardNumber().assertVisible();
    }

    @Owner(PUSPA)
    @Feature("PGP-49206")
    @Parameters({"theme"})
    @Test(description = "Verify Card details page open when single paymode DEBIT_CARD is Present")
    public void verifySinglePaymodeDC(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = EMI_DC_CC;
        EnablePaymentMode enablePaymentMode = new EnablePaymentMode(null, "DEBIT_CARD", new String[]{"HDFC", "ICICI"});
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setEnablePaymentMode(new EnablePaymentMode[]{enablePaymentMode}).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabDebitCard().assertNotVisible();
        DriverManager.getDriver().switchTo().frame(cashierPage.cc_dc_iframe());
        cashierPage.textBoxCardNumber().assertVisible();
    }

    @Owner(PUSPA)
    @Feature("PGP-49206")
    @Parameters({"theme"})
    @Test(description = "Verify NB details page open when single paymode NET BANKING is Present")
    public void verifySinglePaymodeNB(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = EMI_DC_CC;
        EnablePaymentMode enablePaymentMode = new EnablePaymentMode(new String[]{"HDFC", "ICICI"}, "NET_BANKING");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setEnablePaymentMode(new EnablePaymentMode[]{enablePaymentMode}).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        PaymentDTO paymentDTO =new PaymentDTO().setBankName("ICICI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabNetBanking().assertNotVisible();
        cashierPage.dropdownNB().selectByValue(paymentDTO.getBankName());
        cashierPage.buttonPGPayNow().waitUntilClickable();
        cashierPage.buttonPGPayNow().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("NB")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS");

    }


    @Owner(PUSPA)
    @Feature("PGP-49206")
    @Parameters({"theme"})
    @Test(description = "Verify cashier page render same way when qr details is present")
    public void verifySinglePaymodeUPI(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = PG2_JS_Checkout_Paytm_Domain;
        EnablePaymentMode enablePaymentMode = new EnablePaymentMode(new String[]{}, "UPI");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setEnablePaymentMode(new EnablePaymentMode[]{enablePaymentMode}).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabUPI().assertVisible();
    }
    @Owner(PUSPA)
    @Feature("PGP-55803")
    @Parameters({"theme"})
    @Test(description = "Verify offer details get hide on click of Card when single paymode is EMI")
    public void verifyOfferHidesOnclickofCard(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = EmiInfo_COP;
        EnablePaymentMode enablePaymentMode = new EnablePaymentMode(new String[]{"HDFC", "ICICI"}, "EMI");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setEnablePaymentMode(new EnablePaymentMode[]{enablePaymentMode})
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_emiIframe());
        cashierPage.textBoxCardNumber().clearAndType(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.viewAllOffersAvialable().assertVisible();


    }

    @Owner(PUSPA)
    @Feature("PGP-55803")
    @Parameters({"theme"})
    @Test(description = "Verify offer details get hide on Select of Bank when single paymode is EMI")
    public void verifyOfferHidesOnSelectofbank(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = EmiInfo_COP;
        EnablePaymentMode enablePaymentMode = new EnablePaymentMode(new String[]{"HDFC", "ICICI"}, "EMI");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setEnablePaymentMode(new EnablePaymentMode[]{enablePaymentMode})
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.dropdownEmiBanksV5().selectByVisibleText("HDFC");
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_emiIframe());
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.viewAllOffersAvialable().assertVisible();

    }
    @Owner(PUSPA)
    @Feature("PGP-55803")
    @Parameters({"theme"})
    @Test(description = "Verify Offer Hide for NB when single paymode NET BANKING is Present")
    public void verifyOfferHideNB(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = EmiInfo_COP;
        EnablePaymentMode enablePaymentMode = new EnablePaymentMode(new String[]{"HDFC", "ICICI"}, "NET_BANKING");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setEnablePaymentMode(new EnablePaymentMode[]{enablePaymentMode}).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        PaymentDTO paymentDTO = new PaymentDTO().setBankName("ICICI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.dropdownNB().selectByValue(paymentDTO.getBankName());
        cashierPage.viewAllOffersAvialable().assertVisible();

    }
    @Owner(PUSPA)
    @Feature("PGP-55803")
    @Parameters({"theme"})
    @Test(description = "Verify Offer Hide on enter of Card for single paymode CREDIT_CARD is Present")
    public void verifyHideOfferCC(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = EmiInfo_COP;
        EnablePaymentMode enablePaymentMode = new EnablePaymentMode(null, "CREDIT_CARD", new String[]{"HDFC", "ICICI"});
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setEnablePaymentMode(new EnablePaymentMode[]{enablePaymentMode}).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabCreditCard().assertNotVisible();
        DriverManager.getDriver().switchTo().frame(cashierPage.cc_dc_iframe());
        cashierPage.textBoxCardNumber().clearAndType(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.viewAllOffersAvialable().assertVisible();
    }
    @Owner(PUSPA)
    @Feature("PGP-55803")
    @Parameters({"theme"})
    @Test(description = "Verify No Payment option text when payment methode is not present")
    public void verifyNoPaymentOptionText(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = EmiInfo_COP;
        EnablePaymentMode enablePaymentMode = new EnablePaymentMode(new String[]{"HDFC", "ICICI"}, "EMI_DC");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setEnablePaymentMode(new EnablePaymentMode[]{enablePaymentMode})
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        Assertions.assertThat(cashierPage.NoPaymentOptionAvailable().getText()).isEqualTo("No Payment Options available");
    }
    @Owner(PUSPA)
    @Feature("PGP-PGP-55988")
    @Parameters({"theme"})
    @Test(description = "Order Amount is not getting updated if user goes back in the flow in EMI")
    public void verifyAmountModify(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = EMI_DC_CC;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("100")
                .build();
        PaymentDTO paymentDTO =new PaymentDTO();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabEMI().click();
        cashierPage.dropdownEmiBanksV5().selectByVisibleText("HDFC");
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_emiIframe());
        cashierPage.textBoxCardNumberEMI().clearAndType(paymentDTO.getEmiCard());
        cashierPage.waitUntilLoads();
        DriverManager.getDriver().switchTo().parentFrame();
        cashierPage.waitUntilLoads();
        cashierPage.proceedToSelectEmiPlan().click();
        cashierPage.proceedToConvertEMI().click();
        cashierPage.CrossButtonOnEntercardpage().click();
        String amountToBePaid= cashierPage.amountToBePaidOnCashierPage().getText();
        amountToBePaid=amountToBePaid.replaceAll("₹","");
        cashierPage.waitUntilLoads();
        Assertions.assertThat(initTxnDTO.getBody().getTxnAmount().getValue()).isEqualTo(amountToBePaid);
    }

}



