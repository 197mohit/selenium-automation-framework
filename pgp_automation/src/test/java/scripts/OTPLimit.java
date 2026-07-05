package scripts;

import com.paytm.api.TxnStatus;
import com.paytm.appconstants.Constants;
import com.paytm.base.test.Group;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.framework.core.DriverManager;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
import io.qameta.allure.Owner;
import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import org.assertj.core.api.Assertions;

@Owner("Gagandeep")
public class OTPLimit extends PGPBaseTest {

    private final CheckoutPage checkoutPage = new CheckoutPage();
    private static final int maxRetry=5;
    @Parameters({"theme"})
    @Test(description = "Validate sent OTP Limit breach", groups = {"regression"})
    public void OTPLimitBreach(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForRead(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.AddnPay(Constants.MerchantType.AddnPay, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        cashierPage.RequestloginOTP(user.mobNo());

        for (int i = 0; i < maxRetry; i++) {
            cashierPage.RequestOTP().click();
        }
        cashierPage.waitUntilContainsText("Oops");
        String textmessage = cashierPage.validateOTPLimitBreachOnTop().getText();
        Assertions.assertThat(textmessage).isEqualTo("Oops ! You have reached OTP limit, please raise a query at paytm.com/care.");

    }


    @Parameters({"theme"})
    @Test(description = "Validate send OTP Limit breach on different numbers", groups = {"regression"})
    public void OTPLimitBreachWitDifferentNo(@Optional("enhancedweb") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.AddnPay(Constants.MerchantType.AddnPay, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        User user=userManager.getForWrite(Label.LOGIN);
        for(int i = 0; i < maxRetry; i++) {
            cashierPage.RequestloginOTP(user.mobNo());
            try {
                cashierPage.clickChangeNumber();
            } finally {
                user.purge();
            }
            if(i!=maxRetry-1)
                user=userManager.getForWrite(Label.LOGIN);
        }
        cashierPage.RequestloginOTP(user.mobNo());
        cashierPage.waitUntilContainsText("Oops");
        String textmessage = cashierPage.validateOTPLimitBreachOnTop().getText();
        Assertions.assertThat(textmessage).isEqualTo("Oops ! You have reached OTP limit, please raise a query at paytm.com/care.");

    }


    @Parameters({"theme"})
    @Test(description = "Validate after login/logout wih 1 number other number have remaining OTP limit", groups = {"regression"})
    public void ValidateAfterLoginLogoutWithRemainingOTPLimit(@Optional("enhancedweb_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.AddnPay(Constants.MerchantType.AddnPay, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        User user=userManager.getForWrite(Label.LOGIN);
        for (int i = 0; i < maxRetry; i++) {
            cashierPage.login(user);
            try {
                cashierPage.logout(user);
            } finally {
                user.purge();
            }
            userManager.release();

            if (i != maxRetry - 1)
                user = userManager.getForWrite(Label.LOGIN);
        }
        cashierPage.RequestloginOTP(user.mobNo());
        cashierPage.waitUntilContainsText("Oops");
        String textmessage = cashierPage.ResponseMsgRequestOtp();
        Assertions.assertThat(textmessage).isEqualTo("Oops ! You have reached OTP limit, please raise a query at paytm.com/care.");

    }


    @Parameters({"theme"})
    @Test(description = "Validate login in 5th attempt then logout again requesting for OTP will Breach the limit", groups = {"regression"})
    public void LoginWithMaxLimitThenReLoginOTPLimitBreach(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.AddnPay(Constants.MerchantType.AddnPay, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.RequestloginOTP(user.mobNo());
        for (int i = 0; i < maxRetry-2; i++) {
            cashierPage.RequestOTP().click();
        }
        cashierPage.clickChangeNumber();
        cashierPage.login(user);
        try {
            cashierPage.logout(user);
        }finally {
            user.purge();
        }
        cashierPage.RequestloginOTP(user.mobNo());
        cashierPage.waitUntilContainsText("Oops");
        String textmessage = cashierPage.ResponseMsgRequestOtp();
        Assertions.assertThat(textmessage).isEqualTo("Oops ! You have reached OTP limit, please raise a query at paytm.com/care.");

    }

    @Parameters({"theme"})
    @Test(description = "Verify the orange strip message on Header when otp limit gets breached from oauth", groups = {"regression"})
    public void ErrorHeaderAfterLimitBreached(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.AddnPay(Constants.MerchantType.AddnPay, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        cashierPage.RequestloginOTP(user.mobNo());

        for (int i = 0; i < maxRetry; i++) {
            cashierPage.RequestOTP().click();
        }
        cashierPage.waitUntilContainsText("Oops");
        Assert.assertEquals("#ffad00",cashierPage.GetHeaderErrorColor());
        cashierPage.GetHeaderError().assertText("Oops ! You have reached OTP limit, please raise a query at paytm.com/care.");

    }

    @Parameters({"theme"})
    @Test(description = "Verify after limit breached otp section is disabled", groups = {"regression"})
    public void ValidateRequestOTPisDisabledAfterLimitBreached(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForRead(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.AddnPay(Constants.MerchantType.AddnPay, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.RequestloginOTP(user.mobNo());
        for (int i = 0; i < maxRetry; i++) {
            cashierPage.RequestOTP().click();
        }
        cashierPage.waitUntilContainsText("Oops");
        Assert.assertFalse(cashierPage.loginSection().isEnabled());
    }

    @Parameters({"theme"})
    @Test(description = "Verify after limit breached you can pay through other paymodes", groups = {"regression"})
    public void ValidateAfterLimitBreachPaymentAnotherPaymode(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForRead(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.AddnPay(Constants.MerchantType.AddnPay, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.RequestloginOTP(user.mobNo());
        for (int i = 0; i < maxRetry; i++) {
            cashierPage.RequestOTP().click();
        }
        cashierPage.payBy(Constants.PayMode.CC);
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

    }

    @Parameters({"theme"})
    @Test(description = "Verify after limit breached payment through another mode failed in retry case again requesting otp will display same error", groups = {"regression"})
    public void ValidateAfterLimitBreachPaymentAnotherPaymodeFailsInRetryErrorWillBeSameForRequestingOTP(@Optional("enhancedwap") String theme) throws Exception {
        User user = userManager.getForRead(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.AddnPay(Constants.MerchantType.AddnPay_Retry, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.RequestloginOTP(user.mobNo());
        for (int i = 0; i < maxRetry; i++) {
            cashierPage.RequestOTP().click();
        }
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(paymentDTO.CREDIT_CARD_FOR_FAILED_TXN);
        cashierPage.payBy(Constants.PayMode.CC,paymentDTO);
        cashierPage.waitUntilLoads();
        cashierPage.RequestloginOTP(user.mobNo());
        cashierPage.waitUntilContainsText("Oops");
        String textmessage = cashierPage.ResponseMsgRequestOtp();
        Assertions.assertThat(textmessage).isEqualTo("Oops ! You have reached OTP limit, please raise a query at paytm.com/care.");






    }



}