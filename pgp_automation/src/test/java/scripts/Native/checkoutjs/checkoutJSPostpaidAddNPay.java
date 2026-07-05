package scripts.Native.checkoutjs;

import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.PostpaidHelpers;
import com.paytm.apphelpers.WalletHelpers;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.checkoutjs.MerchantConfig;
import com.paytm.pages.*;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.Date;

import static com.paytm.appconstants.Constants.Owner.MAYURI;
import static com.paytm.appconstants.Constants.Owner.ROHIT;

public class checkoutJSPostpaidAddNPay extends CheckoutJsBase {
    private final CheckoutJsCheckoutPage checkoutPage = new CheckoutJsCheckoutPage();

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
    @Feature("PGP-33928")
    @Parameters({"theme"})
    @Test(description = "Success Txn from Paytm postpaid and wallet using merchant NOT having postpaid and wallet(SCW) merchant having postpaid paymode")
    public void SuccessfulAddNPayPostpaidTxnForMIDNotHavingPostapidPaymode(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_ADDNPAY;
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        PostpaidHelpers.updateBalance("2");
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
        String paymentFlow="ADDANDPAY";
        String paymentFlowFPO = fetchPaymentOptionsJson.getString("body.paymentFlow");
        Assertions.assertThat(paymentFlowFPO).describedAs("Payment flow is not addNpay").isEqualTo(paymentFlow);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.checkBoxPPI().click();
        cashierPage.payBy(Constants.PayMode.PAYTM_DIGITAL_CARD);
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

    @Owner(MAYURI)
    @Feature("PGP-33928")
    @Parameters({"theme"})
    @Test(description = "Postpaid paymode should not be available since merchant doesn't have postapid acquiring and wallet merchant has postpaid")
    public void postpaidNotAvailableWhenDeselectedWallet(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_ADDNPAY;
        User user = userManager.getForWrite(Label.PG2POSTPAIDUSER);
        PostpaidHelpers.updateBalance("10");
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
        String paymentFlow="ADDANDPAY";
        String paymentFlowFPO = fetchPaymentOptionsJson.getString("body.paymentFlow");
        Assertions.assertThat(paymentFlowFPO).describedAs("Payment flow is not addNpay").isEqualTo(paymentFlow);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.checkBoxPPI().check();
        Assertions.assertThat(cashierPage.isPPIChecked()).isFalse().as("Wallet not checked");
        Assertions.assertThat(cashierPage.isPaytmCCChecked()).isFalse().as("Postpaid is not available for deselcted wallet");
    }


    @Owner(MAYURI)
    @Feature("PGP-33928")
    @Parameters({"theme"})
    @Test(description = "Success Txn from Paytm postpaid and wallet using merchant having postpaid and wallet(SCW) merchant having postpaid paymode")
    public void SuccessfulAddNPayPostpaidTxnForMIDAndWalletMIDHavingPostapidPaymode(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.AddnPay;
        User user = userManager.getForWrite(Label.PG2POSTPAIDUSER);
        PostpaidHelpers.updateBalance("2");
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
        String paymentFlow="ADDANDPAY";
        String paymentFlowFPO = fetchPaymentOptionsJson.getString("body.paymentFlow");
        Assertions.assertThat(paymentFlowFPO).describedAs("Payment flow is not addNpay").isEqualTo(paymentFlow);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.PAYTM_DIGITAL_CARD);
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

    @Owner(MAYURI)
    @Feature("PGP-33928")
    @Parameters({"theme"})
    @Test(description = "Success Txn from Paytm postpaid when deselecting wallet")
    public void SuccessfulPostpaidTxnForWalletDeselected(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.AddnPay;
        User user = userManager.getForWrite(Label.PG2POSTPAIDUSER);
        WalletHelpers.modifyBalance(user,1.0);
        PostpaidHelpers.updateBalance("10");
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
        String paymentFlow="ADDANDPAY";
        String paymentFlowFPO = fetchPaymentOptionsJson.getString("body.paymentFlow");
        Assertions.assertThat(paymentFlowFPO).describedAs("Payment flow is not addNpay").isEqualTo(paymentFlow);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        /*
        balance isn't autometically selected:https://jira.mypaytm.com/browse/PGP-41941
        cashierPage.checkBoxPPI().check();
         */
     // cashierPage.checkBoxPPI().check();  //removing this check due to https://jira.mypaytm.com/browse/PGP-41941 jira changes
        Assertions.assertThat(cashierPage.isPPIChecked()).isFalse().as("Wallet not checked");
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


    @Owner(ROHIT)
    @Feature("PGP-35633")
    @Parameters({"theme"})
    @Test(description = "Signup to Paytm Postpaid banner should be visible, when merchant has postpaid and POSTPAID_ENABLED_ON_MERCHANT perf is Y, and user dont have postpaid")
    public void postpaidSignupBannerVisibleCheckoutJS(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.AddnPay;
        User user = userManager.getForWrite(Label.NOPOSTPAID);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("2")
                .build();
        CheckoutJsCheckoutPage checkoutPage = new CheckoutJsCheckoutPage();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.postpaidSignUpStrip().assertVisible();
    }

    @Owner(ROHIT)
    @Feature("PGP-35633")
    @Parameters({"theme"})
    @Test(description = "Signup to Paytm Postpaid banner should not be visible, when merchant has postpaid and POSTPAID_ENABLED_ON_MERCHANT perf is Y, and user have postpaid")
    public void postpaidSignupBannerNotVisibleCheckoutJS(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.AddnPay;
        User user = userManager.getForWrite(Label.PG2POSTPAIDUSER);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("2")
                .build();
        CheckoutJsCheckoutPage checkoutPage = new CheckoutJsCheckoutPage();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.postpaidSignUpStrip().assertNotVisible();
    }

}
