package scripts.Native.checkoutjs;

import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.checkoutjs.MerchantConfig;
import com.paytm.framework.core.DriverManager;
import com.paytm.pages.*;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.testng.Assert;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;


import java.io.IOException;
import java.util.Date;

import static com.paytm.appconstants.Constants.Owner.MAYURI;
import static com.paytm.appconstants.Constants.Owner.PUSPA;

public class UISanityJSonRedirection extends PGPBaseTest {


    /*PreRequiste to run JS on Redirection
    1. FF4j Flag need to be enabled on mid -theia.checkout.forward.html.render.allowed
    2. pref: ENABLE_CHECKOUT_JS_ON_ENHANCED_FLOW
    3. If Cashier page does not render and breaks at API level, make merchant ONLINE from database -PAYTMPGDB.MERCHANT_SOLUTION_INFO
    */

    private final CheckoutPage checkoutPage = new CheckoutPage();
    private final ResponsePage responsePage =new ResponsePage();
    @Owner(PUSPA)
    @Parameters({"theme"})
    @Test(description = "Successful txn using Credit Card ")
    public void CCSuccessTxn(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.CHECKOUT_ON_REDIRECTION, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
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
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
    }
    @Owner(PUSPA)
    @Parameters({"theme"})
    @Test(description = "Successful txn using Debit Card ")
    public void DCSuccessTxn(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.CHECKOUT_ON_REDIRECTION, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.DEBIT_CARD_NUMBER);
        cashierPage.payBy(Constants.PayMode.CC_WITH_SINGLE_PAYMODE,paymentDTO);
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("DC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
    }
    @Owner(PUSPA)
    @Parameters({"theme"})
    @Test(description = "Successful txn using Net Banking ")
    public void NBSuccessTxn(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PG2_JS_Checkout_Paytm_Domain, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(Constants.PayMode.NB);
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("NB")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
    }

    @Owner(PUSPA)
    @Parameters({"theme"})
    @Test(description = "Successful txn using EMI")
    public void EMISuccessTxn(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PG2_JS_Checkout_Paytm_Domain, theme)
                .setTXN_AMOUNT("100")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PaymentDTO paymentDTO =new PaymentDTO().setCreditCardNumber(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        cashierPage.payBy(Constants.PayMode.EMI,paymentDTO);
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
    }


    @Owner(PUSPA)
    @Parameters({"theme"})
    @Test(description = "Successful txn using UPI")
    public void UPISuccessTxn(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PG2_JS_Checkout_Paytm_Domain, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(Constants.PayMode.UPI);
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
    }
    @Owner(PUSPA)
    @Parameters({"theme"})
    @Test(description = "Verify Login using phone number and otp")
    public void verifyLoginWithOTP(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user= userManager.getForRead(Label.AUTOLOGIN);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.AUTOLOGIN_MID, theme)
                .setTXN_AMOUNT("1.00")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.textBoxPhoneNumberNEW().sendKeys("7000000003");
        cashierPage.ProceedButtonClick().click();
        Thread.sleep(10000);
        cashierPage.EnterOTP().clearAndType("123456");
        cashierPage.VerifyOTPButton().click();
        Assertions.assertThat(cashierPage.verifyPaymentModeDisplayed(Constants.PayMode.CC));

    }
    @Owner(PUSPA)
    @Parameters({"theme"})
    @Test(description = "Validate validations on Expiry date and CVV for EMI ")
    public void ValidateEMI_invalidExpiryDate(@Optional("checkoutjs_web_revamp") String theme) throws IOException, InterruptedException {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PG2_JS_Checkout_Paytm_Domain, theme)
                .setTXN_AMOUNT("100")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO =new PaymentDTO().setEmiCard(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        cashierPage.scrollToElement(cashierPage.tabEMI());
                cashierPage.tabEMI().click();
                DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_emiIframe());
                cashierPage.textBoxCardNumberEMI().clearAndType(paymentDTO.getEmiCard());
                DriverManager.getDriver().switchTo().defaultContent();
                cashierPage.waitUntilLoads();
                cashierPage.proceedToSelectEmiPlan().waitUntilVisible();
                cashierPage.proceedToSelectEmiPlan().click();
                DriverManager.getDriver().switchTo().defaultContent();
                cashierPage.proceedToConvertEMI().click();
                DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_expCvv_cardIframe());
        cashierPage.textBoxExpiryMonthEMI().waitUntilEditable();
        cashierPage.textBoxExpiryMonthEMI().clearAndType("11");
        cashierPage.textBoxExpiryYearEMI().waitUntilEditable();
        cashierPage.textBoxExpiryYearEMI().clearAndType("11");
        cashierPage.error_invalidExpiryDate().assertVisible();

    }
    @Owner(PUSPA)
    @Parameters({"theme"})
    @Test(description = "Validate validations on Expiry date and CVV for EMI ")
    public void ValidateEMI_invalidCVV(@Optional("checkoutjs_web_revamp") String theme) throws IOException, InterruptedException {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PG2_JS_Checkout_Paytm_Domain, theme)
                .setTXN_AMOUNT("100")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO =new PaymentDTO().setEmiCard(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        cashierPage.scrollToElement(cashierPage.tabEMI());
                cashierPage.tabEMI().click();
                DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_emiIframe());
                cashierPage.textBoxCardNumberEMI().clearAndType(paymentDTO.getEmiCard());
                DriverManager.getDriver().switchTo().defaultContent();
                cashierPage.waitUntilLoads();
                cashierPage.proceedToSelectEmiPlan().waitUntilVisible();
                cashierPage.proceedToSelectEmiPlan().click();
                DriverManager.getDriver().switchTo().defaultContent();
                cashierPage.proceedToConvertEMI().click();
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
    @Owner(PUSPA)
    @Parameters({"theme"})
    @Test(description = "Validate Invalid Expiry date for CC")
    public void ValidateCC_invalidExpiryDate(@Optional("checkoutjs_web_revamp") String theme) throws IOException, InterruptedException {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PG2_JS_Checkout_Paytm_Domain, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
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

    @Owner(PUSPA)
    @Parameters({"theme"})
    @Test(description = "Successful txn and text validation for Debit card with PCF")
    public void verifyDCwithPCF(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.AUTOLOGIN_MID, theme)
                .setTXN_AMOUNT("1.00")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setDebitCardNumber(PaymentDTO.DEBIT_CARD_NUMBER);
        cashierPage.tabDebitCard().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_cardIframe());
        cashierPage.textBoxCardNumber().clearAndType(paymentDTO.DEBIT_CARD_NUMBER);
        cashierPage.fillExpiryMonth("12");
        cashierPage.fillExpiryYear("2030");
        cashierPage.textBoxCVVNumber().clearAndType("123");
        cashierPage.pause(2);
        double expectedChargeFeeAmt = convenienceFeeCalculator(Double.valueOf(orderDTO.getTXN_AMOUNT()), 6, 0, "DC");
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.FeesAppliedtext().waitUntilPresent();
        cashierPage.FeesAppliedtext().waitUntilVisible();
        cashierPage.FeesAppliedtext().assertVisible();
        cashierPage.infoIconForPCFNew().click();
        Thread.sleep(10000);
        DriverManager.getDriver().switchTo().defaultContent();
        
        String platformfee_text = cashierPage.PlatformFeeText().getText();
        cashierPage.PlatformFeeText().waitUntilVisible();
        Assertions.assertThat(platformfee_text).isEqualTo("Platform fees refer to charges levied by PG on end customers. These fees are instrument agnostic and cover the infrastructure costs of maintaining platform.");
        cashierPage.payButton().assertVisible();
        cashierPage.buttonPGPayNow().click();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("DC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
    }
    @Owner(PUSPA)
    @Parameters({"theme"})
    @Test(description = "Successful txn and text validation for UPI with PCF")
    public void verifyUPIwithPCF(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.AUTOLOGIN_MID, theme)
                .setTXN_AMOUNT("1.00")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabUPI().click();
        cashierPage.pause(2);
        double expectedChargeFeeAmt = convenienceFeeCalculator(Double.valueOf(orderDTO.getTXN_AMOUNT()), 5, 0, "DC");
        cashierPage.FeesAppliedtext().assertVisible();
        cashierPage.infoIconForPCFNew().click();
        Thread.sleep(10000);

        String platformfee_text = cashierPage.PlatformFeeText().getText();
        cashierPage.PlatformFeeText().waitUntilVisible();
        Assertions.assertThat(platformfee_text).isEqualTo("Platform fees refer to charges levied by PG on end customers. These fees are instrument agnostic and cover the infrastructure costs of maintaining platform.");
        cashierPage.waitUntilLoads();
        cashierPage.textBoxVPA().sendKeys("test@paytm");
        cashierPage.textBoxVPA().click();
        cashierPage.buttonPGPayNow().click();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();

    }
    @Owner(PUSPA)
    @Parameters({"theme"})
    @Test(description = "Verify error message:Please enter a valid UPI ID should be displayed after entering an invalid \"UPI ID\" and clicking on pay button for NONE payment flow")
    public void validate_vpa_msg(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user =userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PG2_JS_Checkout_Paytm_Domain, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT("1.00")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabUPI().click();
        if (theme.equals(Constants.Theme.CHECKOUTJS_WAP_REVAMP)) {
            cashierPage.tabUPIId().click();
        }
        cashierPage.textBoxVPA().waitUntilClickable();
        cashierPage.textBoxVPA().clearAndType("11invalid@pay11");
        cashierPage.payButton().click();
        String invalidVpa = cashierPage.vpaerrormsg().getText();
        Assertions.assertThat(invalidVpa).isEqualTo("Invalid VPA, Try Again");
    }

//     @Owner(PUSPA)
//     @Parameters({"theme"})
//     @Test(description = "Verfiy successfull saved vpa txn using Checkout js flow",enabled=false)
    public void validateSuccessTxnUsingSavedVPA(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user =userManager.getForRead(Label.SAVEDVPA);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PG2_JS_Checkout_Paytm_Domain, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT("1.00")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabSavedUPI(1);
        cashierPage.payBy(Constants.PayMode.SAVED_UPI);
        Assert.assertFalse(cashierPage.waitForNewWindow(2), "New window is not open");
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
    }

    @Owner(PUSPA)
    @Parameters({"theme"})
    @Test(description = "To verify failed CC Txn with zero retry ")
    public void Validate_Failed_CC_Retry_Txn(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PG2_JS_Checkout_Paytm_Domain, theme)
                .setTXN_AMOUNT("99.98")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDetailsForRetry = new PaymentDTO().setCreditCardNumber(PaymentDTO.CREDIT_CARD_FOR_FAILED_TXN);
        cashierPage.payBy(Constants.PayMode.CC, paymentDetailsForRetry);
        cashierPage.waitUntilLoads();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.EMPTY)
                .validateCurrency("INR")
                .validatePaymentMode("CC")
                .validateRespCode("750")
                .validateRespMsg(Constants.MessageAssert.WRONG_OTP.toString())
                .validateStatus("TXN_FAILURE");

    }
    @Owner(PUSPA)
    @Parameters({"theme"})
    @Test(description = "Successful txn and text validation for Net Banking with PCF")
    public void verifyPCFtextforNB(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.AUTOLOGIN_MID, theme).
                setTXN_AMOUNT("1").build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabNetBankingNEW().click();
   //     cashierPage.dropdownNB().selectByValue("ICICI");
        cashierPage.pause(3);
        Double actualBaseAmt = Double.valueOf(orderDTO.getTXN_AMOUNT());
        Double actualTotalAmt = Double.valueOf(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(cashierPage.totalAmtAtPG().getText()));
        Double actualChargeFeeAmt = Double.valueOf(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(String.valueOf(actualTotalAmt - actualBaseAmt)));
        Double expectedChargeFeeAmt = convenienceFeeCalculator(Double.valueOf(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT())), 6, 0, "NB");

        SoftAssertions softAssert = new SoftAssertions();
        softAssert.assertThat(actualChargeFeeAmt).as("NB").isEqualTo(expectedChargeFeeAmt);
        softAssert.assertAll();
        cashierPage.paybuttonNB().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("NB")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("ICICI")
                .assertAll();
    }
    @Owner(PUSPA)
    @Parameters({"theme"})
    @Test(description = "Verify Login using phone number and invalid otp")
    public void verifyLoginWithInvalidOTP(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.AUTOLOGIN);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.AUTOLOGIN_MID, theme)
                .setTXN_AMOUNT("1.00")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.textBoxPhoneNumberNEW().sendKeys("7000000003");
        cashierPage.ProceedButtonClick().click();
        Thread.sleep(10000);
        cashierPage.EnterOTP().clearAndType("11");
        cashierPage.VerifyOTPButton().click();
        cashierPage.waitUntilLoads();
        String expected_text = cashierPage.getErrorMessageInvalidOTPNew().getText();
        Assertions.assertThat(expected_text).isEqualTo("OTP is invalid");


    }
    @Owner(PUSPA)
    @Parameters({"theme"})
    @Test(description = "Validate Invalid CVV for CC")
    public void validateCC_invalidCVV(@Optional("checkoutjs_web_revamp") String theme) throws IOException, InterruptedException {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.CHECKOUT_ON_REDIRECTION, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        // cashierPage.tabCreditCard().waitUntilClickable();
        // cashierPage.tabCreditCard().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_cardIframe());
        cashierPage.textBoxCardNumber().clearAndType(paymentDTO.getCreditCardNumber());
        cashierPage.pause(2);
        cashierPage.fillExpiryMonth("12");
        cashierPage.fillExpiryYear("2030");
        cashierPage.textBoxCVVNumber().clearAndType("00");
        cashierPage.waitUntilLoads();
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.payButton().assertVisible();
        cashierPage.payButton().click();
        cashierPage.waitUntilLoads();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_cardIframe());
        Assertions.assertThat(cashierPage.getError_invalidCVV().getText()).isEqualTo("Please enter valid CVV");
    }
    @Owner(PUSPA)
    @Parameters({"theme"})
    @Test(description = "Validate Successful Login With Correct Otp with rememberMe checkbox ")
    public void validateSucessfullLoginWithCorrectOtp(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.AUTOLOGIN_MID, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.textBoxPhoneNumberNEW().sendKeys("7014107741");
        cashierPage.ProceedButtonClick().click();
        Thread.sleep(10000);
        cashierPage.EnterOTP().clearAndType("888888");
        cashierPage.VerifyOTPButton().click();
        Thread.sleep(10000);
        Assert.assertTrue(cashierPage.tabNetBankingNEW().isDisplayed());
    }
 //AppInvoke for JS on Redirection(showpaymentpage)

    @Owner(PUSPA)
    @Parameters({"theme"})
    @Test(description = "Successful txn using Credit Card ")
    public void CCSuccessTxn_AppInvoke(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.CHECKOUT_ON_REDIRECTION;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(merchant, initTxnDTO.orderFromBody(), txnToken).build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
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
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
    }
    @Owner(PUSPA)
    @Parameters({"theme"})
    @Test(description = "Successful txn using Debit Card ")
    public void DCSuccessTxn_AppInvoke(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.CHECKOUT_ON_REDIRECTION;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(merchant, initTxnDTO.orderFromBody(), txnToken).build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PaymentDTO paymentDTO =new PaymentDTO().setCreditCardNumber(PaymentDTO.DEBIT_CARD_NUMBER);
        cashierPage.payBy(Constants.PayMode.CC_WITH_SINGLE_PAYMODE,paymentDTO);
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("DC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
    }
    @Owner(PUSPA)
    @Parameters({"theme"})
    @Test(description = "Successful txn using Net Banking ")
    public void NBSuccessTxn_AppInvoke(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.PG2_JS_Checkout_Paytm_Domain;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(merchant, initTxnDTO.orderFromBody(), txnToken).build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(Constants.PayMode.NB);
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("NB")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
    }
    @Owner(PUSPA)
    @Parameters({"theme"})
    @Test(description = "Successful txn using EMI")
    public void EMISuccessTxn_AppInvoke(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.PG2_JS_Checkout_Paytm_Domain;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant).setTxnValue("100").build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(merchant, initTxnDTO.orderFromBody(), txnToken).build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PaymentDTO paymentDTO =new PaymentDTO().setCreditCardNumber(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        cashierPage.payBy(Constants.PayMode.EMI,paymentDTO);
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
    }
    @Owner(PUSPA)
    @Parameters({"theme"})
    @Test(description = "Successful txn using UPI")
    public void UPISuccessTxn_AppInvoke(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.PG2_JS_Checkout_Paytm_Domain;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(merchant, initTxnDTO.orderFromBody(), txnToken).build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(Constants.PayMode.UPI);
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
    }

    @Owner(MAYURI)
    @Feature("PGP-54630")
    @Parameters({"theme"})
    //pref : ENABLE_CHECKOUT_JS_ON_ENHANCED_FLOW should be enabled, ff4j flag : theia.enableMinimalPromoOrSubvention should have mid entry
    @Test(description = "MINIMAL_PROMO_MERCHANT and MINIMAL_SUBVENTION_MERCHANT pref enabled on mid")
    public void validateOfferStripOnAppInvokeFlowWhenBothPrefEnabled(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.PG2_AMEX_EMI;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant).setTxnValue("2000").build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(merchant, initTxnDTO.orderFromBody(), txnToken).build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.offerStripHideButton().assertVisible();

    }

    @Owner(MAYURI)
    @Feature("PGP-54630")
    @Parameters({"theme"})
    //pref : ENABLE_CHECKOUT_JS_ON_ENHANCED_FLOW should be enabled, ff4j flag : theia.enableMinimalPromoOrSubvention should have mid entry
    @Test(description = "MINIMAL_PROMO_MERCHANT=off and MINIMAL_SUBVENTION_MERCHANT=on on mid")
    public void validateOfferStripOnAppInvokeFlowWhenMinimalSubventionPrefEnabled(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.EMI_DISCOVERY;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant).setTxnValue("2000").build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(merchant, initTxnDTO.orderFromBody(), txnToken).build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.offerStripHideButton().assertVisible();
    }

    @Owner(MAYURI)
    @Feature("PGP-54630")
    @Parameters({"theme"})
    //pref : ENABLE_CHECKOUT_JS_ON_ENHANCED_FLOW should be enabled, ff4j flag : theia.enableMinimalPromoOrSubvention should have mid entry
    @Test(description = "MINIMAL_PROMO_MERCHANT=on and MINIMAL_SUBVENTION_MERCHANT=off on mid")
    public void validateOfferStripOnAppInvokeFlowWhenMinimalPromoPrefEnabled(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.PGONLY_EMI_MIN_MAX;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant).setTxnValue("2000").build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(merchant, initTxnDTO.orderFromBody(), txnToken).build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.emiSubventionOfferStrip().assertVisible();
    }

    @Owner(MAYURI)
    @Feature("PGP-54630")
    @Parameters({"theme"})
    //pref : ENABLE_CHECKOUT_JS_ON_ENHANCED_FLOW should be enabled, ff4j flag : theia.enableMinimalPromoOrSubvention should have mid entry
    @Test(description = "MINIMAL_PROMO_MERCHANT=off and MINIMAL_SUBVENTION_MERCHANT=off on mid")
    public void validateOfferStripOnAppInvokeFlowWhenBothDisabled(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.CUST_ID_PROMO;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant).setTxnValue("2000").build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(merchant, initTxnDTO.orderFromBody(), txnToken).build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.emiSubventionOfferStrip().assertNotVisible();
        cashierPage.offerStripHideButton().assertNotVisible();
    }

    @Owner(MAYURI)
    @Parameters({"theme"})
    @Feature("PGP-54630")
    @Test(description = "MINIMAL_PROMO_MERCHANT and MINIMAL_SUBVENTION_MERCHANT pref enabled on mid")
    public void validateOfferStripOnJSOnRedirectionFlowWhenBothPrefEnabled(@Optional("checkoutjs_web_revamp") String theme) throws IOException, InterruptedException {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PG2_AMEX_EMI, theme)
                .setTXN_AMOUNT("200")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.offerStripHideButton().assertVisible();
    }

    @Owner(MAYURI)
    @Feature("PGP-54630")
    @Parameters({"theme"})
    //pref : ENABLE_CHECKOUT_JS_ON_ENHANCED_FLOW should be enabled, ff4j flag : theia.enableMinimalPromoOrSubvention should have mid entry
    @Test(description = "MINIMAL_PROMO_MERCHANT=off and MINIMAL_SUBVENTION_MERCHANT=on on mid")
    public void validateOfferStripOnJSOnRedirectionFlowWhenMinimalSubventionPrefEnabled(@Optional("checkoutjs_web_revamp") String theme) throws IOException, InterruptedException {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.EMI_DISCOVERY, theme)
                .setTXN_AMOUNT("200")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.offerStripHideButton().assertVisible();
    }

    @Owner(MAYURI)
    @Feature("PGP-54630")
    @Parameters({"theme"})
    //pref : ENABLE_CHECKOUT_JS_ON_ENHANCED_FLOW should be enabled, ff4j flag : theia.enableMinimalPromoOrSubvention should have mid entry
    @Test(description = "MINIMAL_PROMO_MERCHANT=on and MINIMAL_SUBVENTION_MERCHANT=off on mid")
    public void validateOfferStripOnJSOnRedirectionFlowWhenMinimalPromoPrefEnabled(@Optional("checkoutjs_web_revamp") String theme) throws IOException, InterruptedException {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PGONLY_EMI_MIN_MAX, theme)
                .setTXN_AMOUNT("200")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.emiSubventionOfferStrip().assertVisible();
    }

    @Owner(MAYURI)
    @Feature("PGP-54630")
    @Parameters({"theme"})
    //pref : ENABLE_CHECKOUT_JS_ON_ENHANCED_FLOW should be enabled, ff4j flag : theia.enableMinimalPromoOrSubvention should have mid entry
    @Test(description = "MINIMAL_PROMO_MERCHANT=off and MINIMAL_SUBVENTION_MERCHANT=off on mid")
    public void validateOfferStripOnJSOnRedirectionFlowWhenBothPromoPrefDisabled(@Optional("checkoutjs_web_revamp") String theme) throws IOException, InterruptedException {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.CUST_ID_PROMO, theme)
                .setTXN_AMOUNT("200")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.emiSubventionOfferStrip().assertNotVisible();
        cashierPage.offerStripHideButton().assertNotVisible();
    }

    @Owner(Constants.Owner.VIDHI)
    @Feature("PAPR-6245/PPSL-720")
    @Parameters({"theme"})
    @Test(description = "Verify the Fees Applied text on Net Banking tab in Checkout JS on Redirection")
    public void verifyFees_applied_text(@Optional("checkoutjs_web_revamp") String theme) throws Exception{
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PCF_PLATFORM_MID, theme)
                .setTXN_AMOUNT("200")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabNetBanking().click();
        cashierPage.dropdownNB().selectByValue("ICICI");
        Assertions.assertThat(cashierPage.feesAppliedText().getText()).isEqualTo("Fees Applied");
    }

    @Owner(Constants.Owner.VIDHI)
    @Feature("PAPR-6245/PPSL-720")
    @Parameters({"theme"})
    @Test(description = "Verify the iButton on Net Banking tab in Checkout JS on Redirection")
    public void verify_i_button_heading(@Optional("checkoutjs_web_revamp") String theme) throws Exception{
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PCF_PLATFORM_MID, theme)
                .setTXN_AMOUNT("200")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabNetBanking().click();
        cashierPage.dropdownNB().selectByValue("ICICI");
        Assertions.assertThat(cashierPage.iButton_Heading().getText()).isEqualTo("Fees Applied");
    }



    @Owner(Constants.Owner.VIDHI)
    @Feature("PAPR-6245/PPSL-720")
    @Parameters({"theme"})
    @Test(description = "Verify the Conv Fee option on clicking the i-Button in Checkout JS on Redirection")
    public void verify_ibuttonConvFeeHeading(@Optional("checkoutjs_web_revamp") String theme) throws Exception{
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PCF_PLATFORM_MID, theme)
                .setTXN_AMOUNT("200")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabNetBanking().click();
        cashierPage.dropdownNB().selectByValue("ICICI");
        Assertions.assertThat(cashierPage.iButton_ConvFeeHeading().getText()).isEqualTo("Convenience Fee");
    }

    @Owner(Constants.Owner.VIDHI)
    @Feature("PAPR-6245/PPSL-720")
    @Parameters({"theme"})
    @Test(description = "Verify the Conv Fee Text on clicking the i-Button in Checkout JS on Redirection")
    public void verify_ibuttonConvFeeText(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PCF_PLATFORM_MID, theme)
                .setTXN_AMOUNT("200")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabNetBanking().click();
        cashierPage.dropdownNB().selectByValue("ICICI");
        Assertions.assertThat(cashierPage.feesAppliedText().getText()).isEqualTo("Fees Applied");
    }

    @Owner(Constants.Owner.VIDHI)
    @Feature("PAPR-6245/PPSL-720")
    @Parameters({"theme"})
    @Test(description = "Verify the Platform Fee option on clicking the i-Button in Checkout JS on Redirection")
    public void verify_ibuttonPlatformFeeHeading(@Optional("checkoutjs_web_revamp") String theme) throws Exception{
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PCF_PLATFORM_MID, theme)
                .setTXN_AMOUNT("200")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabNetBanking().click();
        cashierPage.dropdownNB().selectByValue("ICICI");
        Assertions.assertThat(cashierPage.iButton_PlatformFeeHeading().getText()).isEqualTo("Platform Fee");
    }

    @Owner(Constants.Owner.VIDHI)
    @Feature("PAPR-6245/PPSL-720")
    @Parameters({"theme"})
    @Test(description = "Verify the Platform Fee Text on clicking the i-Button in Checkout JS on Redirection")
    public void verify_ibuttonPlatformFeeText(@Optional("checkoutjs_web_revamp") String theme) throws Exception{
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PCF_PLATFORM_MID, theme)
                .setTXN_AMOUNT("200")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabNetBanking().click();
        cashierPage.dropdownNB().selectByValue("ICICI");
        Assertions.assertThat(cashierPage.iButton_PlatformFeeText().getText()).isEqualTo("Platform fees refer to charges levied by PG on end customers. These fees are instrument agnostic and cover the infrastructure costs of maintaining platform.");
    }


}
