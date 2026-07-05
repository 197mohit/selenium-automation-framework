package scripts.UI;

import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.apphelpers.WalletHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.checkoutjs.MerchantConfig;
import com.paytm.pages.*;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.testng.Assert;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import static com.paytm.appconstants.Constants.MerchantType.*;

import static com.paytm.appconstants.Constants.Owner.ANUSHKA_GOLDI;

public class UITextMsgTests extends PGPBaseTest {

    private final CheckoutJsCheckoutPage checkoutPage = new CheckoutJsCheckoutPage();
    private final CheckoutPage checkoutPage2 = new CheckoutPage();

    @Owner(ANUSHKA_GOLDI)
    @Feature("PGP-44212")
    @Parameters({"theme"})
    @Test(description = "Verfiy Text is present without Saved card in Checkout js flow sacn n pay")
    public void ValidateUITextMsgTestsScanNpay(@Optional("checkoutjs_wap_revamp") String theme) throws Exception {
        Constants.MerchantType mid= UI_TEXTMSG_SCANNPAY;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid)
                .build();
        User user = userManager.getForWrite(Label.AUTOLOGIN);
        WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()));
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.merchant.setMid(initTxnDTO.getBody().getMid())
                .setName(null);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());

        Assertions.assertThat(cashierPage.getUItextMsgOfPaymodes().getText())
                .contains("Pay with Paytm Postpaid, Paytm Wallet or UPI");
        String msg = cashierPage.getUItextMsgOfPaymodes().getText();
        Assertions.assertThat(msg.contains("Saved Card")).isFalse();

    }


    @Owner(ANUSHKA_GOLDI)
    @Feature("PGP-44212")
    @Parameters({"theme"})
    @Test(description = "Verfiy Text is present without Saved card in Checkout js flow Login QR when only wallet is enabled")
    public void ValidateUITextMsgTestLoginQR(@Optional("checkoutjs_wap_revamp") String theme) throws Exception {
        Constants.MerchantType mid= UI_TEXTMSG_LOGINQR;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid)
                .build();
        User user = userManager.getForWrite(Label.AUTOLOGIN);
        WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()));
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.merchant.setMid(initTxnDTO.getBody().getMid())
                .setName(null);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());

        Assertions.assertThat(cashierPage.getUItextMsgOfPaymodes().getText())
                .contains("Pay with Paytm Wallet");
        String msg = cashierPage.getUItextMsgOfPaymodes().getText();
        Assertions.assertThat(msg.contains("Saved Card"))
                .isFalse();

    }

    @Owner(ANUSHKA_GOLDI)
    @Feature("PGP-44212")
    @Parameters({"theme"})
    @Test(description = "Verfiy Text is present without Saved card in Checkout js flow Login QR when UPI and Postpaid is enabled")
    public void ValidateUITextMsgLoginQR(@Optional("checkoutjs_wap_revamp") String theme) throws Exception {
        Constants.MerchantType mid= UI_TEXTMSG_LOGINQR_UPI;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid)
                .build();
        User user = userManager.getForWrite(Label.AUTOLOGIN);
        WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()));
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.merchant.setMid(initTxnDTO.getBody().getMid())
                .setName(null);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());

        Assertions.assertThat(cashierPage.getUItextMsgOfPaymodes().getText())
                .contains("Pay with Paytm Postpaid or UPI");
        String msg = cashierPage.getUItextMsgOfPaymodes().getText();
        Assertions.assertThat(msg.contains("Saved Card")).isFalse();

    }

    @Owner(ANUSHKA_GOLDI)
    @Feature("PGP-44212")
    @Parameters({"theme"})
    @Test(description = "Verfiy Text is present without Saved card in Checkout js flow Login OTP wallet and upi added")
    public void ValidateUITextMsgTestLoginOTP(@Optional("checkoutjs_wap_revamp") String theme) throws Exception {
        Constants.MerchantType mid= UI_TEXTMSG_LOGINOTP;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid)
                .build();
        User user = userManager.getForWrite(Label.AUTOLOGIN);
        WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()));
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.merchant.setMid(initTxnDTO.getBody().getMid())
                .setName(null);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());

        Assertions.assertThat(cashierPage.getUItextMsgOfPaymodesloginOtp().getText())
                .contains("Pay with Paytm Wallet or UPI");
        String msg = cashierPage.getUItextMsgOfPaymodesloginOtp().getText();
        Assertions.assertThat(msg.contains("Saved Card")).isFalse();

    }

    @Owner(ANUSHKA_GOLDI)
    @Feature("PGP-44212")
    @Parameters({"theme"})
    @Test(description = "Verfiy Text is present without Saved card in Checkout js flow Login QR and login otp")
    public void ValidateUITextMsgTestLoginQRAndLoginOTP(@Optional("checkoutjs_wap_revamp") String theme) throws Exception {
        Constants.MerchantType mid= COBRANDED_DEPRIORITISE_DC;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid)
                .build();
        User user = userManager.getForWrite(Label.AUTOLOGIN);
        WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()));
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.merchant.setMid(initTxnDTO.getBody().getMid())
                .setName(null);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());

        Assertions.assertThat(cashierPage.getUItextMsgOfPaymodes().getText())
                .contains("Pay with Paytm Postpaid, Paytm Wallet or UPI");
        String msg = cashierPage.getUItextMsgOfPaymodes().getText();
        Assertions.assertThat(msg.contains("Saved Card"))
                .isFalse();

        Assertions.assertThat(cashierPage.getUItextMsgOfPaymodesloginOtp().getText())
                .contains("Pay with Paytm Postpaid, Paytm Wallet or UPI");
        String msg1 = cashierPage.getUItextMsgOfPaymodesloginOtp().getText();
        Assertions.assertThat(msg1.contains("Saved Card"))
                .isFalse();

    }

    @Parameters({"theme"})
    @Feature("PGP-44212")
    @Owner(ANUSHKA_GOLDI)
    @Test(description = "Verfiy Text is present with Saved card in Enhanced flow Login OTP wallet and upi also added")
    public void ValidateUITextMsgTestLoginOTPEnhancedflow(@Optional("enhancedweb_revamp") String theme) throws Exception {
        String TxnAmount = "2";
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.UI_TEXTMSG_LOGINOTP, theme)
                .setTXN_AMOUNT(TxnAmount)
                .build();
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        String payPaytm = cashierPage.noQRAvailableText().getText();
        Assertions.assertThat(payPaytm).contains("Pay with Paytm");
        cashierPage.loginStrip().assertVisible();

        String upiEnabledMessage = cashierPage.noQRPaymodesPresent().getText();
        Assertions.assertThat(upiEnabledMessage)
                .contains("Pay using Paytm Wallet, Saved Cards or UPI");
        Assertions.assertThat(upiEnabledMessage
                .contains("Saved Card")).isTrue();

    }

    @Parameters({"theme"})
    @Feature("PGP-44212")
    @Owner(ANUSHKA_GOLDI)
    @Test(description = "Verfiy Text is Saved card not enabled in Enhanced flow Login QR when UPI and Postpaid is enabled")
    public void ValidateUITextMsgTestLoginQREnhancedflow(@Optional("enhancedweb_revamp") String theme) throws Exception {
        String TxnAmount = "2";
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.UI_TEXTMSG_LOGINQR_UPI, theme)
                .setTXN_AMOUNT(TxnAmount)
                .build();
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();

        String upiEnabledMessage = cashierPage.getUItextMsgOfPaymodes().getText();
        Assertions.assertThat(upiEnabledMessage)
                .contains("Pay with Paytm Postpaid or UPI");
        Assertions.assertThat(upiEnabledMessage.contains("Saved Card"))
                .isFalse();

    }

    @Parameters({"theme"})
    @Feature("PGP-44212")
    @Owner(ANUSHKA_GOLDI)
    @Test(description = "Verfiy Text is Saved card not enabled in Enhanced flow Login QR when only wallet is enabled")
    public void ValidateUITextMsgLoginQREnhancedflow(@Optional("enhancedweb_revamp") String theme) throws Exception {
        String TxnAmount = "2";
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.UI_TEXTMSG_LOGINQR, theme)
                .setTXN_AMOUNT(TxnAmount)
                .build();
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();

        String upiEnabledMessage = cashierPage.getUItextMsgOfPaymodes().getText();
        Assertions.assertThat(upiEnabledMessage)
                .contains("Pay with Paytm Wallet");
        Assertions.assertThat(upiEnabledMessage.contains("Saved Card"))
                .isFalse();

    }

    @Parameters({"theme"})
    @Feature("PGP-44212")
    @Owner(ANUSHKA_GOLDI)
    @Test(description = "Verfiy Text is Saved card enabled in Enhanced flow Scan N pay ")
    public void ValidateUITextMsgLoginQREnhancedflowWithSavedCard(@Optional("enhancedweb_revamp") String theme) throws Exception {
        String TxnAmount = "2";
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.UI_TEXTMSG_LOGINQR_SavedCard, theme)
                .setTXN_AMOUNT(TxnAmount)
                .build();
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();

        String upiEnabledMessage = cashierPage.getUItextMsgOfPaymodesScanNPay()
                .getText();
        Assertions.assertThat(upiEnabledMessage)
                .contains("Pay using Paytm Postpaid, Paytm Wallet, Saved Cards or UPI");
        Assertions.assertThat(upiEnabledMessage.contains("Saved Card"))
                .isTrue();

    }

    @Parameters({"theme"})
    @Feature("PGP-44212")
    @Owner(ANUSHKA_GOLDI)
    @Test(description = "Verfiy Text is present with Saved card in Enhanced flow Login OTP and SanNpay wallet, postpaid and upi also added")
    public void ValidateUITextMsgLoginOTPEnhancedflow(@Optional("enhancedweb_revamp") String theme) throws Exception {
        String TxnAmount = "2";
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.UI_TEXTMSG_SCANNPAY, theme)
                .setTXN_AMOUNT(TxnAmount)
                .build();
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();

        String payPaytm = cashierPage.noQRAvailableText().getText();
        Assertions.assertThat(payPaytm).contains("Pay with Paytm");
        cashierPage.loginStrip().assertVisible();

        String upiEnabledMessage = cashierPage.noQRPaymodesPresent().getText();
        Assertions.assertThat(upiEnabledMessage)
                .contains("Pay using Paytm Postpaid, Paytm Wallet, Saved Cards or UPI");
        Assertions.assertThat(upiEnabledMessage
                .contains("Saved Card")).isTrue();

        String upiEnabledMessage1 = cashierPage.noQRPaymodesPresent().getText();
        Assertions.assertThat(upiEnabledMessage1)
                .contains("Pay using Paytm Postpaid, Paytm Wallet, Saved Cards or UPI");
        Assertions.assertThat(upiEnabledMessage1
                .contains("Saved Card")).isTrue();

    }
}
