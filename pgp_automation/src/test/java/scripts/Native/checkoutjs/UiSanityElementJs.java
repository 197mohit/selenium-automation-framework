package scripts.Native.checkoutjs;

import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.*;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.MandateAccountDetails;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.checkoutjs.MerchantConfig;
import com.paytm.framework.core.DriverManager;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutJsCheckoutMerchantElementPage;
import com.paytm.pages.ResponsePage;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.Date;

import static com.paytm.appconstants.Constants.MerchantType.SUBS_UI_TEXT;
import static com.paytm.appconstants.Constants.Owner.ROHIT_SHARMA;

public class UiSanityElementJs extends PGPBaseTest {



    @Owner(ROHIT_SHARMA)
    @Feature("Sanity")
    @Parameters({"theme"})
    @Test(description = "Verify Success Credit Card Txn")
    public void newCreditCardSuccessTxn(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.PGOnly_Retry).setTxnValue("10")
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
        cashierPage.tabCreditCard().isDisplayed();
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
    @Owner(ROHIT_SHARMA)
    @Feature("Sanity")
    @Parameters({"theme"})
    @Test(description = "Verify Success Debit Card Txn")
    public void newDebitCardSuccessTxn(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.PGOnly_Retry).setTxnValue("10")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.pause(3);
        checkoutPage.createAndInvokePaymode("DC");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabCreditCard().isDisplayed();
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
                .assertAll();
    }
    @Owner(ROHIT_SHARMA)
    @Feature("Sanity")
    @Parameters({"theme"})
    @Test(description = "Verify Success NetBanking Txn")
    public void nbSuccessTxn(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.PGOnly_Retry).setTxnValue("10")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.pause(3);
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
                .assertAll();
    }
    @Owner(ROHIT_SHARMA)
    @Feature("Sanity")
    @Parameters({"theme"})
    @Test(description = "Verify Success PPBL Txn")
    public void ppblSuccessTxn(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.UPIPUSHPG2);
        WalletHelpers.modifyBalance(user, Double.parseDouble("20.00"));
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.PGOnly_Retry).setTxnValue("1")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.pause(3);
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
                .assertAll();
    }
    @Owner(ROHIT_SHARMA)
    @Feature("Sanity")
    @Parameters({"theme"})
    @Test(description = "Verify Success Wallet Txn")
    public void walletSuccessTxn(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.WalletOnly;
        User user = userManager.getForWrite(Label.UPIPUSHPG2);
        WalletHelpers.modifyBalance(user, Double.parseDouble("20.00"));
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("2.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.pause(3);
        checkoutPage.createAndInvokePaymode("PAY WITH PAYTM");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
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
    @Owner(ROHIT_SHARMA)
    @Feature("Sanity")
    @Parameters({"theme"})
    @Test(description = "Verify Success UPI Collect via VPA Txn")
    public void upiCollectSuccessTxn(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.UPIPUSHPG2;
        User user = userManager.getForRead(Label.PPBL);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("2.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        PostpaidHelpers.updateBalance(initTxnDTO.txnAmountFromBody());
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
                .validateCheckSum(merchantType.getKey())
                .assertAll();

    }
    @Owner(ROHIT_SHARMA)
    @Feature("Sanity")
    @Parameters({"theme"})
    @Test(description = "Verify Success Saved VPA as collect Txn")
    public void upiPushasCollectSuccessTxn(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.UPIPUSHPG2;
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("2.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.pause(3);
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
    @Owner(ROHIT_SHARMA)
    @Feature("Sanity")
    @Parameters({"theme"})
    @Test(description = "Verify Login with otp and remember me checkbox  sucess Txn")
    public void loginOtpSuccessTxn(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly_Retry;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setTxnValue("1.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.pause(3);
        checkoutPage.createAndInvokePaymode("PAY WITH PAYTM");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        User user = userManager.getForWrite(Label.PPBL);
        WalletHelpers.modifyBalance(user, Double.parseDouble("20.00"));
        cashierPage.login("8006006993");
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
    @Owner(ROHIT_SHARMA)
    @Feature("Sanity")
    @Parameters({"theme"})
    @Test(description = "Verify Upi NumericId sucess Txn")
    public void upiNumericIdSuccessTxn(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.UPIPUSHPG2;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setTxnValue("1.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.pause(3);
        checkoutPage.createAndInvokePaymode("UPI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.UpiNumericId().waitUntilClickable();
        cashierPage.UpiNumericId().click();
        cashierPage.UpiNumericId().sendKeys("8006006993");
       cashierPage.buttonPGPayNow().waitUntilClickable();
       cashierPage.buttonPGPayNow().click();
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
    @Owner(ROHIT_SHARMA)
    @Feature("Sanity")
    @Parameters({"theme"})
    @Test(description = "Verify Upi NumericId error msg")
    public void upiNumericIdErrormsg(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.UPIPUSHPG2;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setTxnValue("1.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.pause(3);
        checkoutPage.createAndInvokePaymode("UPI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.UpiNumericId().waitUntilClickable();
        cashierPage.UpiNumericId().click();
        cashierPage.UpiNumericId().sendKeys("9999988888");
        cashierPage.buttonPGPayNow().waitUntilClickable();
        cashierPage.buttonPGPayNow().click();
        Assertions.assertThat(cashierPage.UpiNumericIdErrorMsgClass().getText()).isEqualTo(Constants.MessageAssert.UPI_NUMBER_ERROR.toString());

    }
    @Owner(ROHIT_SHARMA)
    @Feature("Sanity")
    @Parameters({"theme"})
    @Test(description = "Verify Upi VPA error msg")
    public void upiVPAErrormsg(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.UPIPUSHPG2;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setTxnValue("1.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.pause(3);
        checkoutPage.createAndInvokePaymode("UPI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.textBoxVPA().waitUntilVisible();
        cashierPage.textBoxVPA().clearAndType("invaliUPI@paytm");
        cashierPage.buttonPGPayNow().waitUntilClickable();
        cashierPage.buttonPGPayNow().click();
        Assertions.assertThat(cashierPage.vpaerrormsg().getText()).isEqualTo(Constants.MessageAssert.INVALID_VPA.toString());


    }
    @Owner(ROHIT_SHARMA)
    @Feature("Sanity")
    @Parameters({"theme"})
    @Test(description = "Verify Login with invalid otp and verify error msg")
    public void invalidloginOtpError(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly_Retry;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setTxnValue("10.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.pause(3);
        checkoutPage.createAndInvokePaymode("PAY WITH PAYTM");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.loginStrip().waitUntilVisible();
        cashierPage.loginStrip().click();
        cashierPage.textBoxPhoneNumber().waitUntilVisible();
        cashierPage.textBoxPhoneNumber().clearAndType("8006006993");
        cashierPage.buttonSecureSignIn().click();
        cashierPage.fillLoginOtp("123");
        cashierPage.buttonSecureSignIn().click();
        Assertions.assertThat(cashierPage.getErrorMessageInvalidOTP().getText().contains(Constants.MessageAssert.VALID_OTP.toString())).isTrue();

    }
    @Owner(ROHIT_SHARMA)
    @Feature("Sanity")
    @Parameters({"theme"})
    @Test(description = "Verify Invalid card error msg text")
    public void invalidCardErrorMsg(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.EMI;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setTxnValue("10.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.pause(3);
        checkoutPage.createAndInvokePaymode("CC");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber("7111111111111111");
        cashierPage.payBy(Constants.PayMode.CC,paymentDTO);
        DriverManager.getDriver().switchTo().frame(0);
        String text = cashierPage.paymentContainer().getText();
        Assertions.assertThat(text).contains(Constants.MessageAssert.INVALID_CARD_NUMBER.toString());

    }
    @Owner(ROHIT_SHARMA)
    @Feature("Sanity")
    @Parameters({"theme"})
    @Test(description = "Verify Invalid cvv error msg text")
    public void invalidCvvErrorMsg(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.EMI;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setTxnValue("10.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.pause(3);
        checkoutPage.createAndInvokePaymode("CC");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setCvvNumber("1");
        cashierPage.payBy(Constants.PayMode.CC,paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validateRespMsg("Invalid CVV")
                .validateStatus("TXN_FAILURE")
                .assertAll();
    }
    @Owner(ROHIT_SHARMA)
    @Feature("Sanity")
    @Parameters({"theme"})
    @Test(description = "Verify Invalid date error msg text")
    public void invalidDateErrorMsg(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.EMI;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setTxnValue("10.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.pause(3);
        checkoutPage.createAndInvokePaymode("CC");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setExpMonth("13");
        cashierPage.payBy(Constants.PayMode.CC,paymentDTO);
        DriverManager.getDriver().switchTo().frame(0);
        String text = cashierPage.paymentContainer().getText();
        Assertions.assertThat(text).contains("Invalid Expiry Date");
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
    @Owner(ROHIT_SHARMA)
    @Feature("Sanity")
    @Parameters({"theme"})
    @Test(description = "Verify NB PCF txn")
    public void nbPcfTxn(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.PGOnly_Pcf)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.pause(3);
        checkoutPage.createAndInvokePaymode("NB");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabNetBanking().click();
        cashierPage.dropdownNB().selectByValue("ICICI");
        cashierPage.pause(2);
        SoftAssertions softAssert = new SoftAssertions();
        validateCommission(softAssert, cashierPage, Double.valueOf(initTxnDTO.txnAmountFromBody()), 6, 0, "");
        softAssert.assertAll();
        double expectedChargeFeeAmt = convenienceFeeCalculator(Double.valueOf(initTxnDTO.txnAmountFromBody()), 6, 0, "");
        String cashier_text = cashierPage.convFeeMessageCashierPage().getText();
        Assertions.assertThat(cashier_text).isEqualTo("₹"+expectedChargeFeeAmt+ " " + Constants.MessageAssert.CONVENIENCE_FEE.toString());
        String paybutton_text= cashierPage.payButtonConvFeeMsg().getText();
        Assertions.assertThat(paybutton_text).isEqualTo(Constants.MessageAssert.CONVENIENCE_CHARGES.toString());
        cashierPage.payButton().assertVisible();
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
                .validateCheckSum(Constants.MerchantType.PGOnly_Pcf.getKey())
                .validateResponsePageParameters()
                .assertAll();
    }
    @Owner(ROHIT_SHARMA)
    @Feature("Sanity")
    @Parameters({"theme"})
    @Test(description = "Verify success EMI txn")
    public void emiSuccessTxn(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.EMI;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setTxnValue("100.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.pause(3);
        checkoutPage.createAndInvokePaymode("EMI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.EMI);
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
                .assertAll();
    }
    @Owner(ROHIT_SHARMA)
    @Feature("Sanity")
    @Parameters({"theme"})
    @Test(description = "Verify wallet disabled msg")
    public void walletDisabledPaymode(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        WalletHelpers.modifyBalance(user, Double.parseDouble("20.00"));
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.PGOnly_Retry).setTxnValue("5000")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.pause(3);
        checkoutPage.createAndInvokePaymode("PAY WITH PAYTM");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        String text = cashierPage.paymodeDisabledContainer().getText();
        Assertions.assertThat(text).contains("You have insufficient funds in your Paytm Wallet Account. Please use other payment option to complete this transaction.");
    }
    @Owner(ROHIT_SHARMA)
    @Feature("Sanity")
    @Parameters({"theme"})
    @Test(description = "Verify AddnPay NB txn")
    public void addnpayNBtxn(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.AddnPay;
        User user = userManager.getForWrite(Label.LOGIN);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("20.00")
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
        if(cashierPage.checkBoxPPI().isChecked()) {
            cashierPage.tabNetBanking().click();
            cashierPage.dropdownNB().selectByValue("ICICI");
            cashierPage.pause(2);
            cashierPage.buttonPGPayNow().click();
        }
        else{
            cashierPage.checkBoxPPI().waitUntilChecked();
            cashierPage.tabNetBanking().click();
            cashierPage.dropdownNB().selectByValue("ICICI");
            cashierPage.pause(2);
            cashierPage.buttonPGPayNow().click();
        }
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
    @Feature("Sanity")
    @Parameters({"theme"})
    @Test(description = "Verify ppbl disabled msg")
    public void ppblDisabledPaymode(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.PPBL);
        WalletHelpers.modifyBalance(user, Double.parseDouble("20.00"));
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.PGOnly_Retry).setTxnValue("5000")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.pause(3);
        checkoutPage.createAndInvokePaymode("PAY WITH PAYTM");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.checkboxPPBL().check();
        cashierPage.waitUntilLoads();
        String text = cashierPage.paymodeDisabledContainer().getText();
        Assertions.assertThat(text).contains("You have insufficient funds in your Paytm Bank Account. Please use other payment option to complete this transaction.");
    }
    @Owner(ROHIT_SHARMA)
    @Feature("Sanity")
    @Parameters({"theme"})
    @Test(description = "Verify PPBL PCF txn")
    public void ppblPcfTxn(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.PGOnly_Pcf)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.pause(3);
        checkoutPage.createAndInvokePaymode("PAY WITH PAYTM");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.checkboxPPBL().check();
        DriverManager.getDriver().switchTo().frame(cashierPage.ppbl_passcodeIframe());
        cashierPage.textBoxPPBLPassCode().waitUntilEditable();
        cashierPage.textBoxPPBLPassCode().clearAndType("1234");
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.pause(2);
        SoftAssertions softAssert = new SoftAssertions();
        validateCommission(softAssert, cashierPage, Double.valueOf(initTxnDTO.txnAmountFromBody()), 6, 0, "");
        softAssert.assertAll();
        double expectedChargeFeeAmt = convenienceFeeCalculator(Double.valueOf(initTxnDTO.txnAmountFromBody()), 6, 0, "");
        String cashier_text = cashierPage.convFeeMessageCashierPage().getText();
        Assertions.assertThat(cashier_text).isEqualTo("₹"+expectedChargeFeeAmt+ " " + Constants.MessageAssert.CONVENIENCE_FEE.toString());
        String paybutton_text= cashierPage.payButtonConvFeeMsg().getText();
        Assertions.assertThat(paybutton_text).isEqualTo(Constants.MessageAssert.CONVENIENCE_CHARGES.toString());
        cashierPage.payButton().assertVisible();
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
                .validateGatewayName("PPBL")
                .validateBankName("PPBL")
                .validateCheckSum(Constants.MerchantType.PGOnly_Pcf.getKey())
                .validateResponsePageParameters()
                .assertAll();
    }
    @Owner(ROHIT_SHARMA)
    @Feature("Sanity")
    @Parameters({"theme"})
    @Test(description = "Verify Wallet PCF txn")
    public void walletPcfTxn(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.UPIPUSHPG2);
        WalletHelpers.modifyBalance(user, Double.parseDouble("20.00"));
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.WALLETOnly_PCF)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.pause(3);
        checkoutPage.createAndInvokePaymode("PAY WITH PAYTM");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.checkBoxPPI().waitUntilChecked();
        String paybutton_text= cashierPage.payButtonConvFeeMsg().getText();
        Assertions.assertThat(paybutton_text).isEqualTo(Constants.MessageAssert.CONVENIENCE_CHARGES.toString());
        cashierPage.payButton().assertVisible();
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
                .validateGatewayName("WALLET")
                .validateBankName("WALLET")
                .validateCheckSum(Constants.MerchantType.WALLETOnly_PCF.getKey())
                .validateResponsePageParameters()
                .assertAll();
    }
    @Owner(ROHIT_SHARMA)
    @Feature("Sanity")
    @Parameters({"theme"})
    @Test(description = "Verify CC PCF txn")
    public void ccPcfTxn(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.PGOnly_Pcf)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.pause(3);
        checkoutPage.createAndInvokePaymode("CC");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabDebitCard().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_cardIframe());
        cashierPage.textBoxCardNumber().clearAndType("4718650100010336");
        cashierPage.fillExpiryMonth("12");
        cashierPage.fillExpiryYear("2030");
        cashierPage.textBoxCVVNumber().clearAndType("123");
        SoftAssertions softAssert = new SoftAssertions();
        DriverManager.getDriver().switchTo().defaultContent();
        validateCommission(softAssert, cashierPage, Double.valueOf(initTxnDTO.txnAmountFromBody()), 7, 0, "CC");
        softAssert.assertAll();
        double expectedChargeFeeAmt = convenienceFeeCalculator(Double.valueOf(initTxnDTO.txnAmountFromBody()), 7, 0, "CC");
        String cashier_text = cashierPage.convFeeMessageCashierPage().getText();
        Assertions.assertThat(cashier_text).isEqualTo("₹"+expectedChargeFeeAmt+ " " + Constants.MessageAssert.CONVENIENCE_FEE.toString());
        String paybutton_text= cashierPage.payButtonConvFeeMsg().getText();
        Assertions.assertThat(paybutton_text).isEqualTo(Constants.MessageAssert.CONVENIENCE_CHARGES.toString());
        cashierPage.payButton().assertVisible();
        cashierPage.buttonPGPayNow().click();
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
                .validateCheckSum(Constants.MerchantType.PGOnly_Pcf.getKey())
                .validateResponsePageParameters()
                .assertAll();
    }
    @Owner(ROHIT_SHARMA)
    @Feature("Sanity")
    @Parameters({"theme"})
    @Test(description = "Verify DC PCF txn")
    public void dcPcfTxn(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.PGOnly_Pcf)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.pause(3);
        checkoutPage.createAndInvokePaymode("CC");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabDebitCard().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_cardIframe());
        cashierPage.textBoxCardNumber().clearAndType("4444333322221111");
        cashierPage.fillExpiryMonth("12");
        cashierPage.fillExpiryYear("2030");
        cashierPage.textBoxCVVNumber().clearAndType("123");
        SoftAssertions softAssert = new SoftAssertions();
        DriverManager.getDriver().switchTo().defaultContent();
        validateCommission(softAssert, cashierPage, Double.valueOf(initTxnDTO.txnAmountFromBody()), 1, 0, "CC");
        softAssert.assertAll();
        double expectedChargeFeeAmt = convenienceFeeCalculator(Double.valueOf(initTxnDTO.txnAmountFromBody()), 1, 0, "CC");
        String cashier_text = cashierPage.convFeeMessageCashierPage().getText();
        Assertions.assertThat(cashier_text).isEqualTo("₹"+expectedChargeFeeAmt+ " " + Constants.MessageAssert.CONVENIENCE_FEE.toString());
        String paybutton_text= cashierPage.payButtonConvFeeMsg().getText();
        Assertions.assertThat(paybutton_text).isEqualTo(Constants.MessageAssert.CONVENIENCE_CHARGES.toString());
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
                .validateCheckSum(Constants.MerchantType.PGOnly_Pcf.getKey())
                .validateResponsePageParameters()
                .assertAll();
    }
    @Owner(ROHIT_SHARMA)
    @Feature("Sanity")
    @Parameters({"theme"})
    @Test(description = "Verify AddnPay CC txn")
    public void addnpayCCtxn(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.ADDNPAYPEON;
        User user = userManager.getForWrite(Label.LOGIN);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("20.00")
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
        if(cashierPage.checkBoxPPI().isChecked()) {
            cashierPage.tabCreditCard().click();
            PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber("4718650100010336");
            cashierPage.fillAndSubmitCCDetails(paymentDTO, false);
        }
        else{
            cashierPage.checkBoxPPI().waitUntilChecked();
            cashierPage.tabCreditCard().click();
            PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber("4718650100010336");
            cashierPage.fillAndSubmitCCDetails(paymentDTO, false);
        }
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
    @Feature("Sanity")
    @Parameters({"theme"})
    @Test(description = "Verify AddnPay DC txn")
    public void addnpayDCtxn(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.ADDNPAYPEON;
        User user = userManager.getForWrite(Label.LOGIN);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("20.00")
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
        if(cashierPage.checkBoxPPI().isChecked()) {
            cashierPage.tabDebitCard().click();
            PaymentDTO paymentDTO = new PaymentDTO().setDebitCardNumber("4444333322221111");
            cashierPage.fillAndSubmitDCDetails(paymentDTO, false);
        }
        else{
            cashierPage.checkBoxPPI().waitUntilChecked();
            cashierPage.tabDebitCard().click();
            PaymentDTO paymentDTO = new PaymentDTO().setDebitCardNumber("4444333322221111");
            cashierPage.fillAndSubmitDCDetails(paymentDTO, false);
        }
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
    @Feature("Sanity")
    @Parameters({"theme"})
    @Test(description = "Verify Success POSTPAID Txn")
    public void postpaidSuccessTxn(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.UPIPUSHPG2);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.PGOnly_Retry).setTxnValue("1")
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
                .validateCheckSum(Constants.MerchantType.PGOnly.getKey())
                .assertAll();
    }
    @Owner(ROHIT_SHARMA)
    @Feature("Sanity")
    @Parameters({"theme"})
    @Test(description = "Verify POSTPAID PCF txn")
    public void postpaidPcfTxn(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.PGOnly_Pcf)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
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
        cashierPage.pause(2);
        SoftAssertions softAssert = new SoftAssertions();
        validateCommission(softAssert, cashierPage, Double.valueOf(initTxnDTO.txnAmountFromBody()), 6, 0, "");
        softAssert.assertAll();
        double expectedChargeFeeAmt = convenienceFeeCalculator(Double.valueOf(initTxnDTO.txnAmountFromBody()), 6, 0, "");
        String cashier_text = cashierPage.convFeeMessageCashierPage().getText();
        Assertions.assertThat(cashier_text).isEqualTo("₹"+expectedChargeFeeAmt+ " " + Constants.MessageAssert.CONVENIENCE_FEE.toString());
        String paybutton_text= cashierPage.payButtonConvFeeMsg().getText();
        Assertions.assertThat(paybutton_text).isEqualTo(Constants.MessageAssert.CONVENIENCE_CHARGES.toString());
        cashierPage.payButton().assertVisible();
        cashierPage.buttonPGPayNow().click();
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
                .validateCheckSum(Constants.MerchantType.POSTCONV_DEFAULT.getKey())
                .assertAll();
    }
    @Owner(ROHIT_SHARMA)
    @Feature("Sanity")
    @Parameters({"theme"})
    @Test(description = "Verify Postpaid disabled msg")
    public void postpaidDisabledPaymode(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.PGOnly_Retry).setTxnValue("50000")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
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
        Assertions.assertThat(text).contains("You have insufficient funds in your Paytm Postpaid Account. Please use other payment option to complete this transaction.");
    }
    @Owner(ROHIT_SHARMA)
    @Feature("Sanity")
    @Parameters({"theme"})
    @Test(description = "Verify Success saved Credit Card Txn")
    public void savedCcSuccesstxn(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(),
                paymentDTO.getCreditCardNumber());
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.PGOnly).setTxnValue("10")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.pause(3);
        checkoutPage.createAndInvokePaymode("PAY WITH PAYTM");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
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
                .assertAll();
    }
    @Owner(ROHIT_SHARMA)
    @Feature("Sanity")
    @Parameters({"theme"})
    @Test(description = "Verify Success saved Debit Card Txn")
    public void savedDcSuccesstxn(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(),
                paymentDTO.getDebitCardNumber());
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.PGOnly).setTxnValue("10")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.pause(3);
        checkoutPage.createAndInvokePaymode("PAY WITH PAYTM");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(Constants.PayMode.SAVED_CARD);
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
                .assertAll();
    }
    @Owner(ROHIT_SHARMA)
    @Feature("Sanity")
    @Parameters({"theme"})
    @Test(description = "Verify success EMI_DC txn")
    public void emi_dc_SuccessTxn(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.EMIDC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.PG2_AMEX_EMI)
                .setTxnValue("200")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setEmiCard(PaymentDTO.ICICI_DEBIT_CARD_NUMBER_EMI);
        paymentDTO.setBankName("ICICI");
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.pause(3);
        checkoutPage.createAndInvokePaymode("EMI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.EMI,paymentDTO);
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
    @Owner(ROHIT_SHARMA)
    @Feature("Sanity")
    @Parameters({"theme"})
    @Test(description = "Verfiy successfull UPI Subscription txn")
    public void subscriptionUpitxn(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
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
                .validateSubsId(subsId)
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.PPBLC.toString())
                .validateCheckSum(Constants.MerchantType.SUBS_UI_TEXT.getKey())
                .assertAll();

    }
    @Owner(ROHIT_SHARMA)
    @Feature("Sanity")
    @Parameters({"theme"})
    @Test(description = "Verify Success saved Card EMI Txn")
    public void savedEmiSuccesstxn(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(),
                "4761360075860428");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.PG2_AMEX_EMI).setTxnValue("10")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.pause(3);
        checkoutPage.createAndInvokePaymode("EMI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(Constants.PayMode.EMI_SAVED_CARD);
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
                .assertAll();
    }

}
