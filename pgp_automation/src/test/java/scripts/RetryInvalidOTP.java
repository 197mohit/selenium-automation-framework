package scripts;

import com.paytm.apphelpers.CommonHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.PaymentDTO;
import com.paytm.pages.*;
import io.qameta.allure.Owner;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import com.paytm.appconstants.Constants;
import com.paytm.dto.OrderFactory;
import org.testng.annotations.Test;

import java.util.Date;

@Owner("Gagandeep")
public class RetryInvalidOTP extends PGPBaseTest {


    private final CheckoutPage checkoutPage = new CheckoutPage();

    @Parameters({"theme"})
    @Test(description = "Validate user should be able to retry when OTP is INVALID", groups = {"regression"})
    public void ValidateRetryAfterInvalidOTP(@Optional("enhancedwap") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.ICIO_CC_Enabled_Merchant, theme).build();
        checkoutPage.createOrder(orderDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.ICICI_CC_CARD);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC, paymentDTO);
        DirectBankOTPPage directBankOTPPage = new DirectBankOTPPage();
        directBankOTPPage.submitOtp("888888");
        directBankOTPPage.VerifyRequestMsg("Incorrect OTP entered. Kindly enter the new OTP sent");     //TODO: locator changes but message is correct
        directBankOTPPage.submitOtp("123456");
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.ICICO.toString())
                .validateBankName(Constants.Bank.ICI0.toString())
                .validateResponsePageParameters();
    }


    @Parameters({"theme"})
    @Test(description = "Validate Resend OTP Link is Available", groups = {"regression"})
    public void AvailabilityOfResendOTPLink(@Optional("enhancedwap") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.ICIO_CC_Enabled_Merchant, theme).build();
        checkoutPage.createOrder(orderDTO);
        PaymentDTO paymentDTO=new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.ICICI_CC_CARD);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC,paymentDTO);
        DirectBankOTPPage directBankOTPPage = DirectBankPageFactory.getDirectBankPage(theme);
        directBankOTPPage.ResendOTPLink().assertVisible();

    }


    @Parameters({"theme"})
    @Test(description = "Validate Go to Bank Site Link is Available", groups = {"regression"})
    public void AvailabilityOfGotToBankSiteLink(@Optional("enhancedwap") String theme){
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.ICIO_CC_Enabled_Merchant, theme).build();
        checkoutPage.createOrder(orderDTO);
        PaymentDTO paymentDTO=new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.ICICI_CC_CARD);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC,paymentDTO);
        DirectBankOTPPage directBankOTPPage = DirectBankPageFactory.getDirectBankPage(theme);
        directBankOTPPage.GoToBankWebsiteLink().assertVisible();

    }

    @Parameters({"theme"})
    @Test(description = "Validate Resend OTP Using Resend Link After Entering Invalid OTP", groups = {"regression"})
    public void ResendOTPUsingResendLinkAfterEnteringInvalidOTP(@Optional("enhancedweb") String theme){
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.ICIO_CC_Enabled_Merchant, theme).build();
        checkoutPage.createOrder(orderDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.ICICI_CC_CARD);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC, paymentDTO);
        DirectBankOTPPage directBankOTPPage = DirectBankPageFactory.getDirectBankPage(theme);
        directBankOTPPage.submitOtp("888888");
        directBankOTPPage.waitUntilLoads();
        directBankOTPPage.ResendOTPLink().waitUntilClickable();
        directBankOTPPage.ResendOTPLink().click();
        directBankOTPPage.VerifyRequestMsg("OTP has been sent to your registered mobile number");


    }


    @Parameters({"theme"})
    @Test(description = "Validate OTP required message if clicking on pay button without otp", groups = {"regression"})
    public void OTPIsRequiredMessageIfPayedWithoutEnteringOTP(@Optional("enhancedwap") String theme){
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.ICIO_CC_Enabled_Merchant, theme).build();
        checkoutPage.createOrder(orderDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.ICICI_CC_CARD);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC, paymentDTO);
        DirectBankOTPPage directBankOTPPage = DirectBankPageFactory.getDirectBankPage(theme);
        directBankOTPPage.submitOtp("");
        if(theme.equalsIgnoreCase("enhancedweb"))
        directBankOTPPage.VerifyErrorMessage("OTP is required");
        else
            directBankOTPPage.VerifyErrorMessage("OTP Missing");
    }


    @Parameters({"theme"})
    @Test(description = "Validate After Last OTP resend limit Resend Button will disappear ", groups = {"regression"})
    public void LastOTPResendButtonWillDisappear(@Optional("enhancedwap") String theme){
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.ICIO_CC_Enabled_Merchant, theme).build();
        checkoutPage.createOrder(orderDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.ICICI_CC_CARD);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC, paymentDTO);
        DirectBankOTPPage directBankOTPPage = DirectBankPageFactory.getDirectBankPage(theme);
        directBankOTPPage.ResendOTPLink().click();
        directBankOTPPage.VerifyRequestMsg("OTP has been sent to your registered mobile number");
        directBankOTPPage.pause(15);
        directBankOTPPage.ResendOTPLink().click();
        directBankOTPPage.VerifyRequestMsg("OTP has been sent to your registered mobile number");
        directBankOTPPage.ResendOTPLink().assertNotVisible();
    }

    //TODO: message is correct but test failing due to locator changes
    @Parameters({"theme"})
    @Test(description = "Validate Count of Theia submit Retry Count if 1 will control the No of Retry attempt which on Direct Page ", groups = {"regression"})
    public void ValidateTheiaSubmitRetryCountReflectsNumberOfRetryAttempts(@Optional("enhancedwap") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.ICIO_CC_Enabled_Merchant, theme).build();
        checkoutPage.createOrder(orderDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.ICICI_CC_CARD);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC, paymentDTO);
        DirectBankOTPPage directBankOTPPage = new DirectBankOTPPage();
        directBankOTPPage.submitOtp("888888");
        cashierPage.waitUntilLoads();
        directBankOTPPage.VerifyRequestMsg("Incorrect OTP entered. Kindly enter the new OTP sent");     //TODO: message is correct but test failing due to locator changes
        directBankOTPPage.submitOtp("888888");
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("CC")
                .validateRespCode("227")
                .validateRespMsg("Looks like OTP entered was incorrect. Please try again.")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.ICICO.toString())
                .validateBankName(Constants.Bank.ICI0.toString())
                .validateResponsePageParameters();

    }


    @Parameters({"theme"})
    @Test(description = "Validate Count of Theia resend Retry Count if 2 will control the No of Retry attempt which on Direct Page ", groups = {"regression"})
    public void ValidateTheiaResendRetryCountReflectsNumberOfRetryAttempts(@Optional("enhancedwap") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.ICIO_CC_Enabled_Merchant, theme).build();
        checkoutPage.createOrder(orderDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.ICICI_CC_CARD);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC, paymentDTO);
        DirectBankOTPPage directBankOTPPage = DirectBankPageFactory.getDirectBankPage(theme);
        directBankOTPPage.ResendOTPLink().click();
        directBankOTPPage.VerifyRequestMsg("OTP has been sent to your registered mobile number");
        directBankOTPPage.ResendOTPLink().click();
        directBankOTPPage.VerifyRequestMsg("OTP has been sent to your registered mobile number");
        directBankOTPPage.ResendOTPLink().assertNotVisible();
    }

    //TODO: message is correct but test failing due to locator changes
    @Parameters({"theme"})
    @Test(description = "Validate For Merchant Retry after Invalid OTP Retry Limit it lands to Cashier Page ", groups = {"regression"})
    public void ValidateForMerchantRetryAfterInvalidOTPLimitItLandToCashierPage(@Optional("enhancedwap") String theme){
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.ICIO_CC_Enabled_Merchant_Retry, theme).build();
        checkoutPage.createOrder(orderDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.ICICI_CC_CARD);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC, paymentDTO);
        DirectBankOTPPage directBankOTPPage = new DirectBankOTPPage();
        directBankOTPPage.submitOtp("888888");
        directBankOTPPage.VerifyRequestMsg("Incorrect OTP entered. Kindly enter the new OTP sent");     //TODO: message is correct but test failing due to locator changes
        directBankOTPPage.submitOtp("888888");
        cashierPage.waitUntilLoads();
        cashierPage.assertContainsTitle("Paytm Secure Online Payment Gateway");

    }


}
