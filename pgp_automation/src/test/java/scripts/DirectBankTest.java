package scripts;

import com.paytm.api.TxnStatus;
import com.paytm.appconstants.Constants;
import com.paytm.appconstants.Constants.Bank;
import com.paytm.appconstants.Constants.Gateway;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.pages.*;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import org.fest.assertions.api.Assertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.Date;

import static com.paytm.appconstants.Constants.PayMode.CC;
import static com.paytm.appconstants.Constants.ValidationType.NON_EMPTY;
import static com.paytm.base.test.Group.Theme;

@Owner("Tarun")
@Epic("direct-bank")
@Feature("PGP-14682")
public class DirectBankTest extends PGPBaseTest {

    private final CheckoutPage checkoutPage = new CheckoutPage();
    private final DirectBankOTPPage directBankOTPPage = new DirectBankOTPPage();

    @Parameters("theme")
    @Test(groups = {Theme.MERCHANT4, Theme.ENHANCEDWAP, Theme.ENHANCEDWEB})
    public void verifySuccessfulPGOnlyTxn(@Optional("merchant4") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.NATIVE_HDFO, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(CC);
        directBankOTPPage.waitUntilLoads();
        directBankOTPPage.submitOtp(PaymentDTO.OTP);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(NON_EMPTY)
                .validateBankTxnId(NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Gateway.HDFO.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Parameters("theme")
    @Test(groups = {Theme.MERCHANT4, Theme.ENHANCEDWAP, Theme.ENHANCEDWEB})
    public void verifySuccessfulRetriedPGOnlyTxn(@Optional("enhancedwap") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.NATIVE_HDFO, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(CC);
        DirectBankOTPPage directBankOTPPage = DirectBankPageFactory.getDirectBankPage(theme);
        directBankOTPPage.waitUntilLoads();
        directBankOTPPage.cancel().click();
        directBankOTPPage.modalCancelPayment().accept();
        if ("enhancedwap".equalsIgnoreCase(theme)) {
            cashierPage.modalRetryPayment().accept();
        }
        cashierPage.payBy(CC);
        directBankOTPPage.waitUntilLoads();
        directBankOTPPage.submitOtp(PaymentDTO.OTP);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(NON_EMPTY)
                .validateBankTxnId(NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Gateway.HDFO.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Parameters("theme")
    @Test(groups = {Theme.MERCHANT4, Theme.ENHANCEDWAP, Theme.ENHANCEDWEB})
    public void verifySuccessfulPGOnlyTxnOnAddNpayMerchant(@Optional("enhancedwap") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.AddnPay(Constants.MerchantType.HDFO_ADDNPAY, theme, user).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.checkBoxPPI().unCheck();
        cashierPage.payBy(CC);
        directBankOTPPage.waitUntilLoads();
        directBankOTPPage.submitOtp(PaymentDTO.OTP);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Gateway.HDFO.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Parameters("theme")
    @Test(groups = {Theme.MERCHANT4, Theme.ENHANCEDWAP, Theme.ENHANCEDWEB})
    public void verifySuccessfulRetriedPGOnlyTxnOnAddNpayMerchant(@Optional("enhancedwap") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.AddnPay(Constants.MerchantType.HDFO_ADDNPAY, theme, user).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.checkBoxPPI().unCheck();
        cashierPage.payBy(CC);
        DirectBankOTPPage directBankOTPPage = DirectBankPageFactory.getDirectBankPage(theme);
        directBankOTPPage.waitUntilLoads();
        directBankOTPPage.cancel().click();
        directBankOTPPage.modalCancelPayment().accept();
        cashierPage.checkBoxPPI().unCheck();
        if ("enhancedwap".equalsIgnoreCase(theme)) {
            cashierPage.modalRetryPayment().accept();
        }
        cashierPage.payBy(CC);
        directBankOTPPage.waitUntilLoads();
        directBankOTPPage.submitOtp(PaymentDTO.OTP);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Gateway.HDFO.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Owner("gagandeep10.singh")
    @Parameters("theme")
    @Test(description = " Validate Resend Timer Direct Page", groups = {"regression"})
    public void ValidateResendTimerDirectPage(@Optional("enhancedwap") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.ICIO_CC_Enabled_Merchant, theme).build();
        checkoutPage.createOrder(orderDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.ICICI_CC_CARD);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC, paymentDTO);
        DirectBankOTPPage directBankOTPPage = DirectBankPageFactory.getDirectBankPage(theme);
        directBankOTPPage.RequestOTPTimer().assertVisible();

    }
    @Parameters("theme")
    @Test(description = " Validate Resend link is appear after Timer expires Direct Page", groups = {"regression"})
    public void ValidateResendOTPAppearAfterTimerExpires(@Optional("enhancedwap_revamp") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.ICIO_CC_Enabled_Merchant, theme).build();
        checkoutPage.createOrder(orderDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.ICICI_CC_CARD);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC, paymentDTO);
        DirectBankOTPPage directBankOTPPage = DirectBankPageFactory.getDirectBankPage(theme);
        directBankOTPPage.pause(15);
        directBankOTPPage.ResendOTPLink().assertVisible();

    }
    @Parameters("theme")
    @Test(description = " Validate the OTP length 6 digit", groups = {"regression"})
    public void SixDigitOTPErrorMsg(@Optional("enhancedwap") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.ICIO_CC_Enabled_Merchant, theme).build();
        checkoutPage.createOrder(orderDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.ICICI_CC_CARD);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC, paymentDTO);
        DirectBankOTPPage directBankOTPPage = DirectBankPageFactory.getDirectBankPage(theme);
        directBankOTPPage.submitOtp("12345");
        directBankOTPPage.pause(2);
        directBankOTPPage.VerifyErrorMessage("OTP Incomplete");
    }

    @Parameters("theme")
    @Test(description = " Validate Duration of Error Message", groups = {"regression"})
    public void DurationOfErrorMsg(@Optional("enhancedwap") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.ICIO_CC_Enabled_Merchant, theme).build();
        checkoutPage.createOrder(orderDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.ICICI_CC_CARD);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC, paymentDTO);
        DirectBankOTPPage directBankOTPPage = DirectBankPageFactory.getDirectBankPage(theme);
        directBankOTPPage.submitOtp("12345");
        directBankOTPPage.pause(6);
        directBankOTPPage.errorMessage().assertNotVisible();
    }

    @Parameters("theme")
    @Test(description = " Validate bank logo and name should be visible", groups = {"regression"})
    public void visibilityOfBankLogo(@Optional("enhancedwap") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.ICIO_CC_Enabled_Merchant, theme).build();
        checkoutPage.createOrder(orderDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.ICICI_CC_CARD);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC, paymentDTO);
        DirectBankOTPPage directBankOTPPage = DirectBankPageFactory.getDirectBankPage(theme);
        directBankOTPPage.assertBankLogo("ICICI");
    }


    @Parameters("theme")
    @Test(description = "Validate txn amount can be in decimals and should be appended with indian currency", groups = {"regression"})
    public void validateAmountCurrency(@Optional("enhancedwap") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.ICIO_CC_Enabled_Merchant, theme)
                .setTXN_AMOUNT("2.50")
                .build();
        checkoutPage.createOrder(orderDTO);
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.ICICI_CC_CARD);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC, paymentDTO);
        DirectBankOTPPage directBankOTPPage = DirectBankPageFactory.getDirectBankPage(theme);
        Assertions.assertThat(directBankOTPPage.getAmountWithCurrency()).as("Currency Amount combo is not correct").isEqualTo("Rs" + orderDTO.getTXN_AMOUNT().replace(".00",""));
    }

    @Parameters("theme")
    @Test(description = "Verify if OTP is blank and submit is pressed  : OTP Missing message is appearing", groups = {"regression"})
    public void validateotpMissingPage(@Optional("enhancedwap") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.ICIO_CC_Enabled_Merchant, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.ICICI_CC_CARD);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC, paymentDTO);
        DirectBankOTPPage directBankOTPPage = DirectBankPageFactory.getDirectBankPage(theme);
        directBankOTPPage.submitOtp("");
        Assertions.assertThat(directBankOTPPage.getOTPTextboxErrorMessage()).as("Incorrect error message").isEqualTo("OTP Missing");
        }

    @Parameters("theme")
    @Test(description = "Verify if submit is pressed without entering the complete 6 digit OTP , OTP Incomplete message is appearing", groups = {"regression"})
    public void validateotpIncompleteMessage(@Optional("enhancedwap") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.ICIO_CC_Enabled_Merchant, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.ICICI_CC_CARD);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC, paymentDTO);
        DirectBankOTPPage directBankOTPPage = DirectBankPageFactory.getDirectBankPage(theme);
        directBankOTPPage.submitOtp("1234");
        Assertions.assertThat(directBankOTPPage.getOTPTextboxErrorMessage()).as("Incorrect error message").isEqualTo("OTP Incomplete");
    }

    @Parameters("theme")
    @Test(description = "Verify duration of the error message is 5 seconds ", groups = {"regression"})
    public void validateMessageDuration(@Optional("enhancedwap") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.ICIO_CC_Enabled_Merchant, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.ICICI_CC_CARD);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC, paymentDTO);
        DirectBankOTPPage directBankOTPPage = DirectBankPageFactory.getDirectBankPage(theme);
        directBankOTPPage.submitOtp("");
        Assertions.assertThat(directBankOTPPage.getOTPTextboxErrorMessage()).as("Incorrect error message").isEqualTo("OTP Missing");
        directBankOTPPage.pause(5);
        Assertions.assertThat(directBankOTPPage.getOtpTextboxPlaceholder()).as("Incorrect placeholder").isEqualTo("Enter OTP");
        directBankOTPPage.submitOtp("1234");
        Assertions.assertThat(directBankOTPPage.getOTPTextboxErrorMessage()).as("Incorrect error message").isEqualTo("OTP Incomplete");
        directBankOTPPage.pause(5);
        Assertions.assertThat(directBankOTPPage.getOtpTextboxPlaceholder()).as("Incorrect placeholder").isEqualTo("Enter OTP");
    }

    @Parameters("theme")
    @Test(description = "Verify error message is removed on entering otp before 5 seconds", groups = {"regression"})
    public void verifyMessageAfterCorrectOTP(@Optional("enhancedwap") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.ICIO_CC_Enabled_Merchant, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.ICICI_CC_CARD);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC, paymentDTO);
        DirectBankOTPPage directBankOTPPage = DirectBankPageFactory.getDirectBankPage(theme);
        directBankOTPPage.submitOtp("1234");
        Assertions.assertThat(directBankOTPPage.getOTPTextboxErrorMessage()).as("Incorrect error message").isEqualTo("OTP Incomplete");
        directBankOTPPage.otpBox().clearAndType(PaymentDTO.bankOtp);
        Assertions.assertThat(directBankOTPPage.getOtpTextboxPlaceholder()).as("Incorrect placeholder").isEqualTo("Enter OTP");
    }

    @Parameters("theme")
    @Test(description = "Verfiy resend timer on direct page and first resend button will appear after OTP time expires", groups = {"regression"})
    public void verifyResendTimerAndOTPTimer(@Optional("enhancedwap") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.ICIO_CC_Enabled_Merchant, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.ICICI_CC_CARD);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC, paymentDTO);
        DirectBankOTPPage directBankOTPPage = DirectBankPageFactory.getDirectBankPage(theme);
        //OTP Time expires
        directBankOTPPage.pause(15);
        directBankOTPPage.ResendOTPLink().assertVisible();
    }

    @Parameters("theme")
    @Test(description = "Verify after resend exhausted, resend button and timer will disappear and redirectToBankPage will appear", groups = {"regression"})
    public void verifyResendOTPExhausted(@Optional("enhancedwap") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.ICIO_CC_Enabled_Merchant, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.ICICI_CC_CARD);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC, paymentDTO);
        DirectBankOTPPage directBankOTPPage = DirectBankPageFactory.getDirectBankPage(theme);
        //OTP Time expires
        directBankOTPPage.pause(15);
        directBankOTPPage.ResendOTPLink().assertVisible();
        directBankOTPPage.ResendOTPLink().click();
        directBankOTPPage.pause(15);
        directBankOTPPage.ResendOTPLink().assertVisible();
        directBankOTPPage.ResendOTPLink().click();
        directBankOTPPage.GoToBankWebsiteLink().assertVisible();
        directBankOTPPage.ResendOTPLink().assertNotVisible();
    }

    @Parameters("theme")
    @Test(description = "Verify if user is not performing any activity on direct bank page, then option to complete transaction on bank page will be provided and validate text", groups = {"regression"})
    public void verifyRedirectionalPage(@Optional("enhancedwap") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.ICIO_CC_Enabled_Merchant, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.ICICI_CC_CARD);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC, paymentDTO);
        DirectBankOTPPage directBankOTPPage = DirectBankPageFactory.getDirectBankPage(theme);
        directBankOTPPage.pause(30);//expiry time
        directBankOTPPage.GoToBankWebsiteLink().assertVisible();
        Assertions.assertThat(directBankOTPPage.GoToBankWebsiteLink().getText())
                .as("Bank Redirectional message is incorrect")
                .isEqualTo("Complete this payment on ICICI Bank's Website using OTP or static password");
    }

    @Parameters("theme")
    @Test(description = "Verify if user is not performing any activity on direct bank page, then option to complete transaction on bank page will be provided", groups = {"regression"})
    public void verifyPageExpiryTime(@Optional("enhancedwap") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.ICIO_CC_Enabled_Merchant, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.ICICI_CC_CARD);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC, paymentDTO);
        DirectBankOTPPage directBankOTPPage = DirectBankPageFactory.getDirectBankPage(theme);
        directBankOTPPage.pause(180);// page expiry time
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_FAILURE")
                .validateRespMsg("User cancelled the transaction from 3D secure/OTP page")
                .validateRespCode("227")
                .assertAll();
    }

    @Parameters("theme")
    @Test(description = "Verify initial counter is 3 min and reverse counter will be available on the bottom of the page", groups = {"regression"})
    public void validateReverseCounter(@Optional("enhancedwap") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.ICIO_CC_Enabled_Merchant, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.ICICI_CC_CARD);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC, paymentDTO);
        DirectBankOTPPage directBankOTPPage = DirectBankPageFactory.getDirectBankPage(theme);

        Assertions.assertThat(directBankOTPPage.fetchExpiryTime()).as("Expiry time not fetched").contains("03 min : 00 sec");
        directBankOTPPage.pause(5);
        Assertions.assertThat(directBankOTPPage.fetchExpiryTime()).as("Expiry time not fetched").contains("02 min : 56 sec");
    }

    @Parameters("theme")
    @Test(description = "Verify after clicking on cancel button, feedback options page is appearing", groups = {"regression"})
    public void validateCaptureFeedback(@Optional("enhancedwap") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.ICIO_CC_Enabled_Merchant, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.ICICI_CC_CARD);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC, paymentDTO);
        DirectBankOTPPage directBankOTPPage = DirectBankPageFactory.getDirectBankPage(theme);
        directBankOTPPage.cancel().click();
        directBankOTPPage.pause(2);
        Assertions.assertThat(directBankOTPPage.captureFeedback().size()).isEqualTo(5);
        Assertions.assertThat(directBankOTPPage.captureFeedback().get(0).getText().trim())
                .as("Message incorrect")
                .isEqualTo("Want to complete this payment on Bank website");
        Assertions.assertThat(directBankOTPPage.captureFeedback().get(4).getText().trim())
                .as("Message Incorrect")
                .isEqualTo("Other");

    }

    @Parameters("theme")
    @Test(description = "Verify visibility of alert expiry time after 120 sec", groups = {"regression"})
    public void validateAlertExpiryVisibility(@Optional("enhancedwap") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.ICIO_CC_Enabled_Merchant, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.ICICI_CC_CARD);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC, paymentDTO);
        DirectBankOTPPage directBankOTPPage = DirectBankPageFactory.getDirectBankPage(theme);
        directBankOTPPage.waitUntilLoads();
        directBankOTPPage.pause(120);
        directBankOTPPage.alertExpiry().assertVisible();
    }

    //-------------------------Non mandatory feedback TC ----------------

    @Parameters("theme")
    @Test(description = "Verify that Cancel Payment icon is displayed on top left of feedback screen", groups = {"regression"})
    public void validateCancelPaymentAlignment(@Optional("enhancedwap") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.ICIO_CC_Enabled_Merchant, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.ICICI_CC_CARD);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC, paymentDTO);
        DirectBankOTPPage directBankOTPPage = DirectBankPageFactory.getDirectBankPage(theme);
        directBankOTPPage.waitUntilLoads();
        directBankOTPPage.cancel().click();
        Assertions.assertThat(directBankOTPPage.cancelPaymentAlignment()).as("Cancel Payment is not left aligned").isEqualTo("text-align: left;");
    }

    @Parameters("theme")
    @Test(description = "Verify for enhanced-native that when user clicks on Cancel Payment then feedback is not mandatory and txn is cancelled when retry count is 0", groups = {"regression"})
    public void validateCancelPaymentFunctionalityEnhanced(@Optional("enhancedwap") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.ICIO_CC_Enabled_Merchant, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.ICICI_CC_CARD);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC, paymentDTO);
        DirectBankOTPPage directBankOTPPage = DirectBankPageFactory.getDirectBankPage(theme);
        directBankOTPPage.waitUntilLoads();
        directBankOTPPage.cancel().click();
        directBankOTPPage.cancelPayment().click();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_FAILURE")
                .validateTxnType("SALE")
                .validateGatewayName(Bank.ICI0.toString())
                .validateRespCode("227")
                .validateRespMsg("User cancelled the transaction from 3D secure/OTP page")
                .validateBankName(Gateway.ICICO.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();

    }

    @Parameters("theme")
    @Test(description = "Verify for native that when user clicks on Cancel Payment then feedback is not mandatory and txn is cancelled and lands on response page if retryCount is 0", groups = {"regression"})
    public void validateCancelPaymentFunctionalityNative(@Optional("enhancedwap") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.NATIVE_HDFO_PEON_DISABLED, theme)//retrycount should be 0
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        DirectBankOTPPage directBankOTPPage = DirectBankPageFactory.getDirectBankPage(theme);
        directBankOTPPage.waitUntilLoads();
        directBankOTPPage.cancel().click();
        directBankOTPPage.cancelPayment().click();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_FAILURE")
                .validateTxnType("SALE")
                .validateGatewayName(Gateway.HDFO.toString())
                .validateRespCode("227")
                .validateRespMsg("User cancelled the transaction from 3D secure/OTP page")
                .validateBankName(Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();

    }

    @Parameters("theme")
    @Test(description = "Verify for enhanced-native that when retry is enabled on merchant and user clicks on Cancel Payment then user should be redirected to cashier page and retry the txn", groups = {"regression"})
    public void validateCashierPage(@Optional("enhancedwap") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.HDFO_ADDNPAY, theme)//retrycount should not be 0
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(CC);
        DirectBankOTPPage directBankOTPPage = DirectBankPageFactory.getDirectBankPage(theme);
        directBankOTPPage.waitUntilLoads();
        directBankOTPPage.cancel().click();
        directBankOTPPage.cancelPayment().click();
        cashierPage.modalRetryPayment().accept();
        cashierPage.payBy(CC);
        directBankOTPPage.waitUntilLoads();
        directBankOTPPage.submitOtp(PaymentDTO.OTP);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(NON_EMPTY)
                .validateBankTxnId(NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Gateway.HDFO.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();

    }


    @Parameters("theme")
    @Test(description = "Verify for native that when retry is enabled on merchant and user clicks on Cancel Payment then user should be redirected to cashier page and retry the txn", groups = {"regression"})
    public void validateRetryTxn(@Optional("enhancedwap") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.NATIVE_HDFO, theme) //retrycount should not be 0
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(CC);
        DirectBankOTPPage directBankOTPPage = DirectBankPageFactory.getDirectBankPage(theme);
        directBankOTPPage.waitUntilLoads();
        directBankOTPPage.cancel().click();
        directBankOTPPage.cancelPayment().click();
        cashierPage.modalRetryPayment().accept();
        cashierPage.payBy(CC);
        directBankOTPPage.waitUntilLoads();
        directBankOTPPage.submitOtp(PaymentDTO.OTP);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(NON_EMPTY)
                .validateBankTxnId(NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Gateway.HDFO.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();

    }


    @Parameters("theme")
    @Test(description = "Verify for enhanced-native that when user clicks on cross button the feedback screen should be closed and user should be able to interact with the direct page again", groups = {"regression"})
    public void validateCloseButton(@Optional("enhancedwap") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.HDFO_ADDNPAY, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        DirectBankOTPPage directBankOTPPage = DirectBankPageFactory.getDirectBankPage(theme);
        directBankOTPPage.waitUntilLoads();
        directBankOTPPage.cancel().click();
        directBankOTPPage.closeButton().click();
        directBankOTPPage.otpBox().assertVisible();

    }

    @Parameters("theme")
    @Test(description = "Verify for native that when user clicks on cross button the feedback screen should be closed and user should be able to interact with the direct page again", groups = {"regression"})
    public void validateCloseButtonNative(@Optional("enhancedwap") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.NATIVE_HDFO, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        DirectBankOTPPage directBankOTPPage = DirectBankPageFactory.getDirectBankPage(theme);
        directBankOTPPage.waitUntilLoads();
        directBankOTPPage.cancel().click();
        directBankOTPPage.closeButton().click();
        directBankOTPPage.otpBox().assertVisible();

    }
    }
