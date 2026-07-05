package scripts;

import com.paytm.LocalConfig;
import com.paytm.api.TxnStatus;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.base.test.Group;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.framework.utils.DatabaseUtil;
import com.paytm.framework.utils.RedisUtil;
import com.paytm.pages.*;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import org.fest.assertions.api.Assertions;
import org.testng.annotations.*;

import java.util.Date;

import static com.paytm.appconstants.Constants.PayMode.CC;
import static com.paytm.appconstants.Constants.ValidationType.NON_EMPTY;

@Owner("Tarun")
@Epic("direct-bank")
@Feature("PGP-14682")
public class SliderOTP extends PGPBaseTest {

    private final CheckoutPage checkoutPage = new CheckoutPage();
    private final OTPSlider  otpSlider = new OTPSlider();

    @BeforeClass
    public void UpdateSliderOTPtheme() {
        String queryForExp = "UPDATE FF4J_FEATURES SET EXPRESSION='weight=0.0'WHERE FEAT_UID='directTheme2'";
        DatabaseUtil.getInstance().executeUpdateQuery(LocalConfig.PGP_DB_CONNECTION_URL, queryForExp);
        TRANSACTIONAL_REDIS_CLUSTER().del("FF4J_FEATURE_directTheme2");
        RedisUtil.getInstance().getConnection(LocalConfig.PG_REDIS_URI).del("FF4J_FEATURE_directTheme2");
    }

    @Parameters({"theme"})
    @Test(description = " Validate Resend OTP button is not Visible in HDDO Flow", groups = {"regression", Group.Theme.ENHANCEDWAP})
    public void VerifyResendOTPbuttonNotVisibleHDDO(@Optional("enhancedwap") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.HDDOMERCHANT, theme).build();
        checkoutPage.createOrder(orderDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.ICICI_CC_CARD);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC, paymentDTO);
        cashierPage.waitUntilLoads();
        cashierPage.pause(6);
        otpSlider.ResendButton().assertNotVisible();
    }

    @Parameters({"theme"})
    @Test(description = "Validate For Merchant Retry after Invalid OTP entered, it lands to Cashier Page", groups = {"regression", Group.Theme.ENHANCEDWAP})
    public void ValidateForMerchantRetryAfterInvalidOTPLandToCashierPageforOTPSlider(@Optional("enhancedwap") String theme){
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.HDDOMERCHANT, theme).build();
        checkoutPage.createOrder(orderDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.ICICI_CC_CARD);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC, paymentDTO);
        otpSlider.submitOtp("808080"); //808080 is invalid otp for HDDO
        cashierPage.waitUntilLoads();
        cashierPage.clickFailedTxnGotItButtonIfDisplayed();
        cashierPage.payBy(Constants.PayMode.CC, paymentDTO);
        otpSlider.submitOtp("808080");
        cashierPage.waitUntilLoads();
        cashierPage.clickFailedTxnGotItButtonIfDisplayed();
        cashierPage.payBy(Constants.PayMode.CC, paymentDTO);
        otpSlider.submitOtp("888888");
        cashierPage.assertVisible();
    }

    @Parameters({"theme"})
    @Test(description = " Validate Header Message on OTP Slider Page", groups = {"regression", Group.Theme.ENHANCEDWAP})
    public void VerifyHeaderTextOnOTPSlider(@Optional("enhancedwap") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.ICIO_CC_Enabled_Merchant, theme).build();
        checkoutPage.createOrder(orderDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.ICICI_CC_CARD);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC, paymentDTO);
        cashierPage.waitUntilLoads();
        otpSlider.HeaderMsg().assertText("Complete Payment");

    }

    @Parameters({"theme"})
    @Test(description = " Validate Footer Message on OTP Slider Page", groups = {"regression", Group.Theme.ENHANCEDWAP})
    public void VerifyFooterTextOnOTPSlider(@Optional("enhancedwap") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.ICIO_CC_Enabled_Merchant, theme).build();
        checkoutPage.createOrder(orderDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.ICICI_CC_CARD);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC, paymentDTO);
        cashierPage.waitUntilLoads();
        otpSlider.FooterMsg().assertText("An OTP has been sent to your registered mobile number");
    }

    @Parameters({"theme"})
    @Test(description = " Validate Close Button on OTP Slider Page", groups = {"regression", Group.Theme.ENHANCEDWAP})
    public void VerifyCloseButton(@Optional("enhancedwap") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.ICIO_CC_Enabled_Merchant, theme).build();
        checkoutPage.createOrder(orderDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.ICICI_CC_CARD);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC, paymentDTO);
        cashierPage.waitUntilLoads();
        otpSlider.CloseButton().assertVisible();
    }
    @Parameters({"theme"})
    @Test(description = " Validate Resend OTP button", groups = {"regression", Group.Theme.ENHANCEDWAP})
    public void VerifyResendOTPbutton(@Optional("enhancedwap") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.ICIO_CC_Enabled_Merchant, theme).build();
        checkoutPage.createOrder(orderDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.ICICI_CC_CARD);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC, paymentDTO);
        cashierPage.waitUntilLoads();
        cashierPage.pause(6);
        otpSlider.ResendButton().assertVisible();
    }

    @Parameters({"theme"})
    @Test(description = " Validate Submit Button", groups = {"regression", Group.Theme.ENHANCEDWAP})
    public void VerifySubmitButton(@Optional("enhancedwap") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.ICIO_CC_Enabled_Merchant, theme).build();
        checkoutPage.createOrder(orderDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.ICICI_CC_CARD);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC, paymentDTO);
        cashierPage.waitUntilLoads();
        otpSlider.SubmitButton().assertVisible();
    }

    @Parameters({"theme"})
    @Test(description = " Validate Text on OTP Field", groups = {"regression", Group.Theme.ENHANCEDWAP})
    public void VerifyTextOnOTPField(@Optional("enhancedwap") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.ICIO_CC_Enabled_Merchant, theme).build();
        checkoutPage.createOrder(orderDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.ICICI_CC_CARD);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC, paymentDTO);
        cashierPage.waitUntilLoads();
        otpSlider.OTPField().assertText("Enter OTP");
    }

    @Parameters({"theme"})
    @Test(description = "Validate user should be able to retry when OTP is INVALID", groups = {"regression", Group.Theme.ENHANCEDWAP})
    public void ValidateRetryAfterInvalidOTP(@Optional("enhancedwap") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.ICIO_CC_Enabled_Merchant, theme).build();
        checkoutPage.createOrder(orderDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.ICICI_CC_CARD);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC, paymentDTO);
        otpSlider.submitOtp("888888");
        cashierPage.waitUntilLoads();
        otpSlider.VerifyRequestMsg("Incorrect OTP entered. Kindly enter the new OTP sent");
        otpSlider.submitOtp("123456");
        cashierPage.waitUntilLoads();
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
    @Test(description = "Validate For Merchant Retry after Invalid OTP Retry Limit it lands to Cashier Page", groups = {"regression", Group.Theme.ENHANCEDWAP})
    public void ValidateForMerchantRetryAfterInvalidOTPLimitItLandToCashierPageforOTPSlider(@Optional("enhancedwap") String theme){
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.ICIO_CC_Enabled_Merchant_Retry, theme).build();
        checkoutPage.createOrder(orderDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.ICICI_CC_CARD);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC, paymentDTO);
        otpSlider.submitOtp("888888");
        cashierPage.waitUntilLoads();
        otpSlider.VerifyRequestMsg("Incorrect OTP entered. Kindly enter the new OTP sent");
        otpSlider.submitOtp("888888");
        cashierPage.waitUntilLoads();
        cashierPage.assertContainsTitle("Paytm Secure Online Payment Gateway");
    }

    @Parameters({"theme"})
    @Test(description = " Validate Resend OTP Message", groups = {"regression", Group.Theme.ENHANCEDWAP})
    public void VerifyResendOTPMessage(@Optional("enhancedwap") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.ICIO_CC_Enabled_Merchant, theme).build();
        checkoutPage.createOrder(orderDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.ICICI_CC_CARD);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC, paymentDTO);
        cashierPage.waitUntilLoads();
        cashierPage.pause(6);
        otpSlider.ResendButton().assertVisible();
        otpSlider.ResendButton().click();
        otpSlider.VerifyRequestMsg("OTP has been sent to your registered mobile number");


    }


    @Parameters({"theme"})
    @Test(description = " Validate Resend OTP Message after Resend Timer is over", groups = {"regression", Group.Theme.ENHANCEDWAP})
    public void verifyResendOtpAfterResendTimerIsOver(@Optional("enhancedwap") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.ICIO_CC_Enabled_Merchant, theme).build();
        checkoutPage.createOrder(orderDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.ICICI_CC_CARD);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC, paymentDTO);
        cashierPage.pause(6);
        otpSlider.ResendButton().assertVisible();
        otpSlider.ResendButton().click();
        otpSlider.VerifyRequestMsg("OTP has been sent to your registered mobile number");
        otpSlider.pause(6);
        otpSlider.ResendButton().click();
        otpSlider.VerifyRequestMsg("OTP has been sent to your registered mobile number");
        otpSlider.ResendButton().assertNotVisible();


    }


    //-----------non mandatory feedback----------------

    @Parameters({"theme"})
    @Test(description = " Verify that Cancel Payment icon is displayed on top left of feedback screen", groups = {"regression", Group.Theme.ENHANCEDWAP})
    public void cancelButtonAlignment(@Optional("enhancedwap") String theme)  {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.ICIO_CC_Enabled_Merchant, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.ICICI_CC_CARD);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC, paymentDTO);
        otpSlider.waitUntilLoads();
        otpSlider.CloseButton().click();
        Assertions.assertThat(otpSlider.cancelPaymentAlignment()).as("Cancel Payment is not left aligned").isEqualTo("text-align: left;");

    }


    @Test(description = "Verify for enhanced-native that when user clicks on Cancel Payment then feedback is not mandatory and txn is cancelled when retry count is 0", groups = {"regression"})
    public void validateCancelPaymentFunctionalityEnhanced(@Optional("enhancedwap") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.ICIO_CC_Enabled_Merchant, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.ICICI_CC_CARD);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC, paymentDTO);
        otpSlider.waitUntilLoads();
        otpSlider.CloseButton().click();
        otpSlider.cancelPayment().click();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_FAILURE")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Bank.ICI0.toString())
                .validateRespCode("227")
                .validateRespMsg("User cancelled the transaction from 3D secure/OTP page")
                .validateBankName(Constants.Gateway.ICICO.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();

    }


    @Test(description = "Verify for enhanced-native that when retry is enabled on merchant and user clicks on Cancel Payment then user should be redirected to cashier page and retry the txn", groups = {"regression"})
    public void validateCashierPage(@Optional("enhancedwap") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.HDFO_ADDNPAY, theme)//retrycount should not be 0
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(CC);
        otpSlider.waitUntilLoads();
        otpSlider.CloseButton().click();
        otpSlider.cancelPayment().click();
        cashierPage.modalRetryPayment().accept();
        cashierPage.payBy(CC);
        otpSlider.waitUntilLoads();
        otpSlider.submitOtp(PaymentDTO.OTP);
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
                .validateGatewayName(Constants.Gateway.HDFO.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();

    }

    @Test(description = "Verify for enhanced-native that when user clicks on cross button the feedback screen should be closed and user should be able to interact with the direct page again", groups = {"regression"})
    public void validateCloseButton(@Optional("enhancedwap") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.HDFO_ADDNPAY, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        otpSlider.waitUntilLoads();
        otpSlider.CloseButton().click();
        otpSlider.otpBox().assertVisible();

    }






    @AfterClass
    public void UpdateDirectOTPtheme() {
        String queryForExp = "UPDATE FF4J_FEATURES SET EXPRESSION='weight=1.0'WHERE FEAT_UID='directTheme2'";
        DatabaseUtil.getInstance().executeUpdateQuery(LocalConfig.PGP_DB_CONNECTION_URL, queryForExp);
        TRANSACTIONAL_REDIS_CLUSTER().del("FF4J_FEATURE_directTheme2");
        RedisUtil.getInstance().getConnection(LocalConfig.PG_REDIS_URI).del("FF4J_FEATURE_directTheme2");
    }
}