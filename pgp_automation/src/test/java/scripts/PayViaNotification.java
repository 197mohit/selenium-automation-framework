package scripts;

import com.paytm.ServerConfigProvider;
import com.paytm.api.TxnStatus;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.appconstants.Constants;
import com.paytm.appconstants.Constants.MerchantType;
import com.paytm.appconstants.Constants.PayMode;
import com.paytm.appconstants.Constants.ResponseCode;
import com.paytm.appconstants.Constants.ValidationType;
import com.paytm.apphelpers.*;
import com.paytm.base.test.Group;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.checkoutjs.MerchantConfig;
import com.paytm.framework.core.DriverManager;
import com.paytm.pages.*;
import com.paytm.utils.ff4j.FF4JFlags;
import io.qameta.allure.*;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.awaitility.Awaitility;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static com.paytm.appconstants.Constants.Owner.*;
import static com.paytm.apphelpers.LogsValidationHelper.getLogsOnServer;

@Owner(HIMANSHU)
public class PayViaNotification extends PGPBaseTest
{
    Constants.MerchantType merchantWithPayViaNotification = MerchantType.PG_MID_CLIENT;
    Constants.MerchantType merchantWithPayViaNotificationAndUPIQR = MerchantType.STORE_CASH;
    Constants.MerchantType merchantWithUPIQR = MerchantType.QR_ENABLED_MERCHANT;
    private final CheckoutJsCheckoutPage checkoutPage = new CheckoutJsCheckoutPage();
    int maxRetriesResendNotification=2;//max retry value for resend notification fetched from ff4j property theia.notification.number.max.retries.allowed
    int maxRetriesEditPhoneNo=2;//max retry value for editing phone no fetched from ff4j property theia.notification.order.max.retries.allowed


    @Owner(HIMANSHU)
    @Feature("PGP-56583")
    @Parameters({"theme"})
    @Test(description = "Verify presence of 'pay via notification tab' if PAY_VIA_NOTIFICATION_DESKTOP/PAY_VIA_NOTIFICATION_MWEB is Y, ENHANCE_QR_DISABLED:Y, DISABLED_LOGIN_STRIP: N, IS_LOGIN_QR_ENABLED:N")
    public void payViaNotification_01(@Optional("enhancedwap_revamp") String theme) throws Exception
    {
        Constants.MerchantType mid = merchantWithPayViaNotification;
        OrderDTO orderDTO = new OrderFactory.PGOnly(mid,theme).build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payViaNotificationTab().assertVisible();
        cashierPage.qrImg().assertNotVisible();
    }

    @Owner(HIMANSHU)
    @Feature("PGP-56583")
    @Parameters({"theme"})
    @Test(description = "Verify presence of 'pay via notification tab' if PAY_VIA_NOTIFICATION_DESKTOP/PAY_VIA_NOTIFICATION_MWEB is Y, ENHANCE_QR_DISABLED:Y, DISABLED_LOGIN_STRIP: N, IS_LOGIN_QR_ENABLED:N")
    public void payViaNotification_02(@Optional("checkoutjs_wap_revamp") String theme) throws Exception
    {
        Constants.MerchantType mid = merchantWithPayViaNotification;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payViaNotificationTab().assertVisible();
        cashierPage.qrImg().assertNotVisible();
    }

    @Owner(HIMANSHU)
    @Feature("PGP-56583")
    @Parameters({"theme"})
    @Test(description = "Verify presence of 'pay via notification tab+UPI QR' if PAY_VIA_NOTIFICATION_DESKTOP/PAY_VIA_NOTIFICATION_MWEB is Y, ENHANCE_QR_DISABLED:N, DISABLED_LOGIN_STRIP: N, IS_LOGIN_QR_ENABLED:N, ALLOW_LOGIN_ON_DESKTOP:Y")
    public void payViaNotification_04(@Optional("checkoutjs_web_revamp") String theme) throws Exception
    {
        Constants.MerchantType mid = merchantWithPayViaNotificationAndUPIQR;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payViaNotificationTab().assertVisible();
        cashierPage.qrImg().assertVisible();
    }

    @Owner(HIMANSHU)
    @Feature("PGP-56583")
    @Parameters({"theme"})
    @Test(description = "Verify presence of 'UPI QR' if PAY_VIA_NOTIFICATION_DESKTOP is Y, ENHANCE_QR_DISABLED:N, DISABLED_LOGIN_STRIP: Y, IS_LOGIN_QR_ENABLED:Y, ALLOW_LOGIN_ON_DESKTOP: Y  ")
    public void payViaNotification_05(@Optional("enhancedweb_revamp") String theme) throws Exception
    {
        Constants.MerchantType mid = merchantWithUPIQR;
        OrderDTO orderDTO = new OrderFactory.PGOnly(mid,theme).build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payViaNotificationTab().assertNotVisible();
        cashierPage.qrImgNew().assertVisible();

    }

    @Owner(HIMANSHU)
    @Feature("PGP-56583")
    @Parameters({"theme"})
    @Test(description = "Verify presence of 'UPI QR' if PAY_VIA_NOTIFICATION_DESKTOP is Y, ENHANCE_QR_DISABLED:N, DISABLED_LOGIN_STRIP: Y, IS_LOGIN_QR_ENABLED:Y, ALLOW_LOGIN_ON_DESKTOP: Y")
    public void payViaNotification_06(@Optional("checkoutjs_web_revamp") String theme) throws Exception
    {
        Constants.MerchantType mid = merchantWithUPIQR;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payViaNotificationTab().assertNotVisible();
        cashierPage.qrImg().assertVisible();
    }

    @Owner(HIMANSHU)
    @Feature("PGP-56583")
    @Parameters({"theme"})
    @Test(description = "Verify presence of 'Pay via notification tab' if PAY_VIA_NOTIFICATION_DESKTOP is Y, ENHANCE_QR_DISABLED:N, DISABLED_LOGIN_STRIP: Y, IS_LOGIN_QR_ENABLED:Y, ALLOW_LOGIN_ON_DESKTOP: Y ")
    public void payViaNotification_07(@Optional("enhancedwap_revamp") String theme) throws Exception
    {
        Constants.MerchantType mid = merchantWithUPIQR;
        OrderDTO orderDTO = new OrderFactory.PGOnly(mid,theme).build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payViaNotificationTab().assertNotVisible();
    }

    @Owner(HIMANSHU)
    @Feature("PGP-56583")
    @Parameters({"theme"})
    @Test(description = "Verify message shown to unregistered user in case of pay via notification flow")
    public void payViaNotification_09(@Optional("enhancedweb_revamp") String theme) throws Exception
    {
        Constants.MerchantType mid = merchantWithPayViaNotification;
        User user = userManager.getForRead(Label.UNREGISTEREDUSER);
        OrderDTO orderDTO = new OrderFactory.PGOnly(mid,theme).build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.phoneNO_PayViaNotification().clearAndType(user.mobNo());
        cashierPage.proceedSecurelyBtn_PayViaNotification().click();
        cashierPage.waitForNewWindow(1);
        cashierPage.unregisteredUserMsg_payViaNotification().assertVisible();
    }


    @Owner(HIMANSHU)
    @Feature("PGP-56583")
    @Parameters({"theme"})
    @Test(description = "Verify functionality for registered user in case of pay via notification flow")
    public void payViaNotification_10(@Optional("enhancedweb_revamp") String theme) throws Exception
    {
        Constants.MerchantType mid = merchantWithPayViaNotification;
        User user = userManager.getForWrite(Label.STORECASH);
        OrderDTO orderDTO = new OrderFactory.PGOnly(mid,theme).build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.phoneNO_PayViaNotification().clearAndType(user.mobNo());
        cashierPage.proceedSecurelyBtn_PayViaNotification().click();
        cashierPage.waitForNewWindow(1);
        cashierPage.unregisteredUserMsg_payViaNotification().assertNotVisible();
        cashierPage.payViaNotificationTab().assertVisible();
    }



    @Owner(HIMANSHU)
    @Feature("PGP-56583")
    @Parameters({"theme"})
    @Test(description = "Verify retry exhausted for resending notification/sms message in case of pay via notification flow  ")
    public void payViaNotification_11(@Optional("enhancedweb_revamp") String theme) throws Exception
    {
        Constants.MerchantType mid = merchantWithPayViaNotification;
        User user = userManager.getForWrite(Label.STORECASH);
        OrderDTO orderDTO = new OrderFactory.PGOnly(mid,theme).build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.phoneNO_PayViaNotification().clearAndType(user.mobNo());
        cashierPage.proceedSecurelyBtn_PayViaNotification().click();
        /*once notifcation is sent, resend notification button is clickable but due to timer running in background,
        it doesn't work hence adding loop after first time and if condition for second last time
         */
        cashierPage.resendNotification_payViaNotification().waitUntilClickable();
        cashierPage.resendNotification_payViaNotification().click();
        for (int i=0;i<maxRetriesResendNotification;i++)
        {
                cashierPage.waitForNewWindow(5);
                cashierPage.resendNotification_payViaNotification().waitUntilClickable();
                cashierPage.resendNotification_payViaNotification().click();
                cashierPage.waitForNewWindow(5);
                if(cashierPage.resendNotification_payViaNotification().isElementPresent())
                {
                    cashierPage.resendNotification_payViaNotification().click();
                }
        }
        cashierPage.waitForNewWindow(5);
        cashierPage.retryLimitExhaustedMSG_payViaNotification().assertVisible();
    }

    @Owner(HIMANSHU)
    @Feature("PGP-56583")
    @Parameters({"theme"})
    @Test(description = "Verify edit number button functionality in case of pay via notification flow  ")
    public void payViaNotification_12(@Optional("enhancedweb_revamp") String theme) throws Exception
    {
        Constants.MerchantType mid = merchantWithPayViaNotification;
        User user = userManager.getForWrite(Label.STORECASH);
        OrderDTO orderDTO = new OrderFactory.PGOnly(mid,theme).build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.phoneNO_PayViaNotification().clearAndType(user.mobNo());
        cashierPage.proceedSecurelyBtn_PayViaNotification().click();
        for (int i=0;i<maxRetriesEditPhoneNo;i++)
        {
            cashierPage.waitForNewWindow(5);
            cashierPage.editPhoneNo_payViaNotification().click();
            cashierPage.waitForNewWindow(5);
            cashierPage.phoneNO_PayViaNotification().clearAndType(user.mobNo());
            cashierPage.proceedSecurelyBtn_PayViaNotification().click();
        }
        cashierPage.waitForNewWindow(5);
        cashierPage.retryLimitExhaustedMSG_payViaNotification().assertVisible();

    }

}
