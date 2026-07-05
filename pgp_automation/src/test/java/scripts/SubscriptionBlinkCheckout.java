package scripts;

import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.SavedCardHelpers;
import com.paytm.apphelpers.WalletHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.MandateAccountDetails;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.checkoutjs.MerchantConfig;
import com.paytm.framework.core.DriverManager;
import com.paytm.framework.ui.element.UIElement;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutJsCheckoutPage;
import com.paytm.pages.ResponsePage;
import io.netty.util.Constant;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import scala.collection.immutable.Stream;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import java.time.Month;
import java.util.*;

@Owner(Constants.Owner.ROHIT)
public class SubscriptionBlinkCheckout extends PGPBaseTest {
    private final CheckoutJsCheckoutPage checkoutPage = new CheckoutJsCheckoutPage();

    //Deprecated CC/DC Flow after SI Hub Release
//    @Parameters({"theme"})
//    @Test(enabled = false, description = "Creating subscription with paymode CC,with start date, amount type FIX and unsaved card")
    public void sC_VIA_CC01(@Optional("checkoutjs_web") String theme) throws Exception {
        String order_id = CommonHelpers.generateOrderId();
        User user = userManager.getForRead(Label.BASIC);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("CC")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("DAY")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setOrderId(order_id)
                .build();

        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);

        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(responseDTO.getBody().getTxnToken());
        config.data.setOrderId(order_id);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS")
                .validateRespMsg("Txn Success")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .validateRespCode("01")
                .validatePaymentMode("CC")
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .assertAll();
    }


    @Parameters({"theme"})
    @Test(description = "Create subscription with paymode PPI")
    public void sC_VIA_PPI(@Optional("checkoutjs_web_revamp") String theme) throws Exception {//done
        String order_id = CommonHelpers.generateOrderId();
        User user = userManager.getForWrite(Label.BASIC);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PPI;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("PPI")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("1")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("DAY")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setOrderId(order_id)
                .build();

        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        WalletHelpers.modifyBalance(user, 10.00);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(responseDTO.getBody().getTxnToken());
        config.data.setOrderId(order_id);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
      // To be confirmed
       /* if(theme.equalsIgnoreCase(Constants.Theme.CHECKOUTJS_WEB_REVAMP) || theme.equalsIgnoreCase(Constants.Theme.CHECKOUTJS_WAP_REVAMP)) {
            if (!cashierPage.checkBoxPPI().isChecked()){
                cashierPage.checkBoxPPI().check();
            }
        } */
        cashierPage.checkBoxPPI().check();
        cashierPage.payBy(Constants.PayMode.WALLET);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .validateRespMsg("Txn Success")
                .validateRespCode("01")
                .validatePaymentMode("PPI")
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .assertAll();
    }

    //Deprecated CC/DC Flow after SI Hub Release
//    @Parameters({"theme"})
//    @Test(enabled = false, description = "Buying subscription with  payment mode = DC, FrequencyUnit = DAY")
    public void sC_VIA_DC(@Optional("checkoutjs_web") String theme) throws Exception {
        String order_id = CommonHelpers.generateOrderId();
        User user = userManager.getForRead(Label.BASIC);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("DC")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("DAY")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setOrderId(order_id)
                .build();

        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        //PaymentDTO pdto = new PaymentDTO();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(responseDTO.getBody().getTxnToken());
        config.data.setOrderId(order_id);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.DC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .validateRespMsg("Txn Success")
                .validateRespCode("01")
                .validatePaymentMode("DC")
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .assertAll();
    }

    //Deprecated CC/DC Flow after SI Hub Release
//    @Parameters({"theme"})
//    @Test(enabled = false, description = "Buying subscription with FrequencyUnit = MONTH")
    public void sC_VIA_DC2(@Optional("checkoutjs_web") String theme) throws Exception {
        String order_id = CommonHelpers.generateOrderId();
        User user = userManager.getForRead(Label.BASIC);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("CC")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setOrderId(order_id)
                .build();

        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        //PaymentDTO pdto = new PaymentDTO();

        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(responseDTO.getBody().getTxnToken());
        config.data.setOrderId(order_id);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .validateRespMsg("Txn Success")
                .validateRespCode("01")
                .validatePaymentMode("CC")
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .assertAll();
    }

    //Deprecated CC/DC Flow after SI Hub Release
//    @Parameters({"theme"})
//    @Test(enabled = false, description = " Buying subscription with FrequencyUnit = YEAR")
    public void sC_VIA_DC3(@Optional("checkoutjs_web") String theme) throws Exception {
        String order_id = CommonHelpers.generateOrderId();
        User user = userManager.getForRead(Label.BASIC);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("DC")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("YEAR")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setOrderId(order_id)
                .build();

        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        //PaymentDTO pdto = new PaymentDTO();

        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(responseDTO.getBody().getTxnToken());
        config.data.setOrderId(order_id);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.DC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .validateRespMsg("Txn Success")
                .validateRespCode("01")
                .validatePaymentMode("DC")
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .assertAll();
    }

    //Deprecated CC/DC Flow after SI Hub Release
//    @Parameters({"theme"})
//    @Test(enabled = false, description = " Buying subscription with AmountType = VARIABLE and Txn amount is less than Subscription Max amount")
    public void sC_VIA_DC4(@Optional("checkoutjs_web") String theme) throws Exception {
        String order_id = CommonHelpers.generateOrderId();
        User user = userManager.getForRead(Label.BASIC);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("CC")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("50")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setOrderId(order_id)
                .build();

        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        //PaymentDTO pdto = new PaymentDTO();

        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(responseDTO.getBody().getTxnToken());
        config.data.setOrderId(order_id);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .validateRespMsg("Txn Success")
                .validateRespCode("01")
                .validatePaymentMode("CC")
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .assertAll();
    }

    //Deprecated CC/DC Flow after SI Hub Release
//    @Parameters({"theme"})
//    @Test(enabled = false, description = " Validating case with no subscription starting date")
    public void sC_VIA_DC5(@Optional("checkoutjs_web") String theme) throws Exception {
        String order_id = CommonHelpers.generateOrderId();
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("DC")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate("")
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setOrderId(order_id)
                .build();

        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultStatus())
                .as("Result Status mismatch")
                .isEqualToIgnoringCase("F");
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultCode())
                .as("Result Code mismatch")
                .isEqualToIgnoringCase("4001");
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultMsg())
                .as("Result MSg mismatch")
                .isEqualToIgnoringCase("Invalid Subscription start date");

    }

    //Deprecated CC/DC Flow after SI Hub Release
//    @Parameters({"theme"})
//    @Test(enabled = false, description = " Buying subscription with FrequencyUnit WEEK")
    public void sC_VIA_DC7(@Optional("checkoutjs_web") String theme) throws Exception {
        String order_id = CommonHelpers.generateOrderId();
        User user = userManager.getForRead(Label.BASIC);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("DC")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("WEEK")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setOrderId(order_id)
                .build();

        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        PaymentDTO pdto = new PaymentDTO();

        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(responseDTO.getBody().getTxnToken());
        config.data.setOrderId(order_id);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.DC, pdto);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .validateRespMsg("Txn Success")
                .validateRespCode("01")
                .validatePaymentMode("DC")
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .assertAll();
    }

    //Deprecated CC/DC Flow after SI Hub Release
//    @Parameters({"theme"})
//    @Test(enabled = false, description = " Buying subscription with FrequencyUnit QUARTER")
    public void sC_VIA_DC8(@Optional("checkoutjs_web") String theme) throws Exception {
        String order_id = CommonHelpers.generateOrderId();
        User user = userManager.getForRead(Label.BASIC);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("CC")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("50")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("QUARTER")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setOrderId(order_id)
                .build();

        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);

        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(responseDTO.getBody().getTxnToken());
        config.data.setOrderId(order_id);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .validateRespMsg("Txn Success")
                .validateRespCode("01")
                .validatePaymentMode("CC")
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .assertAll();
    }

    //Deprecated CC/DC Flow after SI Hub Release
//    @Parameters({"theme"})
//    @Test(enabled = false, description = " Buying subscription with FrequencyUnit BI_MONTHLY")
    public void sC_VIA_D9(@Optional("checkoutjs_web") String theme) throws Exception {
        String order_id = CommonHelpers.generateOrderId();
        User user = userManager.getForRead(Label.BASIC);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("DC")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("BI_MONTHLY")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setOrderId(order_id)
                .build();

        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        PaymentDTO pdto = new PaymentDTO();

        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(responseDTO.getBody().getTxnToken());
        config.data.setOrderId(order_id);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.DC, pdto);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .validateRespMsg("Txn Success")
                .validateRespCode("01")
                .validatePaymentMode("DC")
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .assertAll();
    }

    //Deprecated CC/DC Flow after SI Hub Release
//    @Parameters({"theme"})
//    @Test(enabled = false, description = " Buying subscription with FrequencyUnit SEMI_ANNUALLY")
    public void sC_VIA_DC10(@Optional("checkoutjs_web") String theme) throws Exception {
        String order_id = CommonHelpers.generateOrderId();
        User user = userManager.getForRead(Label.BASIC);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("CC")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("SEMI_ANNUALLY")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setOrderId(order_id)
                .build();

        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);

        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(responseDTO.getBody().getTxnToken());
        config.data.setOrderId(order_id);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .validateRespMsg("Txn Success")
                .validateRespCode("01")
                .validatePaymentMode("CC")
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .assertAll();
    }

    //Deprecated CC/DC Flow after SI Hub Release
//    @Parameters({"theme"})
//    @Test(enabled = false, description = " Validating case when FrequencyUnit is ONDEMAND when it is not allowed")
    public void sC_VIA_DC11(@Optional("checkoutjs_web") String theme) throws Exception {
        String order_id = CommonHelpers.generateOrderId();
        User user = userManager.getForRead(Label.BASIC);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_ADDNPAY;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("DC")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("50")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setOrderId(order_id)
                .build();

        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultStatus())
                .as("Result Status mismatch")
                .isEqualToIgnoringCase("TXN_FAILURE");
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultMsg())
                .as("Result MSg mismatch")
                .isEqualToIgnoringCase("OnDemand Subscriptions are not allowed on merchant");
    }


    //Deprecated CC/DC Flow after SI Hub Release
//    @Parameters({"theme"})
//    @Test(enabled = false, description = " Creating subscription with SAVED_CARD")
    public void sC_VIA_DC14(@Optional("checkoutjs_web") String theme) throws Exception {
        String order_id = CommonHelpers.generateOrderId();
        User user = userManager.getForRead(Label.BASIC);
        //SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("50")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("YEAR")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setOrderId(order_id)
                .build();

        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);

        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(responseDTO.getBody().getTxnToken());
        config.data.setOrderId(order_id);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.SAVED_CARD);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .validateRespMsg("Txn Success")
                .validateRespCode("01")
                .validatePaymentMode("DC")
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .assertAll();
    }


    @Parameters({"theme"})
    @Test(description = " Creating subscription with no payment mode mention")
    public void sC_VIA_DC15(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        String order_id = CommonHelpers.generateOrderId();
        User user = userManager.getForRead(Label.PPBL);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PPI;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("50")
                .setSubscriptionPaymentMode("")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("50")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("YEAR")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setOrderId(order_id)
                .build();

        WalletHelpers.modifyBalance(user, 10.00);
        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);

        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(responseDTO.getBody().getTxnToken());
        config.data.setOrderId(order_id);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.PPBL);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .validateRespMsg("Txn Success")
                .validateRespCode("01")
                .validatePaymentMode("PPI")
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .assertAll();
    }

    //Deprecated CC/DC Flow after SI Hub Release
//    @Parameters({"theme"})
//    @Test(enabled = false, description = "Create subscription with payment mode NORMAL")
    public void sC_VIA_DC6(@Optional("checkoutjs_web") String theme) throws Exception {
        String order_id = CommonHelpers.generateOrderId();
        User user = userManager.getForWrite(Label.BASIC);
        //SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_ADDNPAY;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("50")
                .setSubscriptionPaymentMode("NORMAL")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("50")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setOrderId(order_id)
                .build();

        WalletHelpers.modifyBalance(user, 10.00);

        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);

        config.data.setToken(responseDTO.getBody().getTxnToken());
        config.data.setOrderId(order_id);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.SAVED_CARD);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .validateRespMsg("Txn Success")
                .validateRespCode("01")
                .validatePaymentMode("PPI")
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .assertAll();
    }


    @Parameters({"theme"})
    @Test(description = "Validating case when wallet amount is less then txn amount")
    public void sC_VIA_PPI01(@Optional("checkoutjs_web") String theme) throws Exception {
        String order_id = CommonHelpers.generateOrderId();
        User user = userManager.getForWrite(Label.BASIC);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PPI;
        WalletHelpers.modifyBalance(user, 0.0);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("50")
                .setSubscriptionPaymentMode("PPI")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("50")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("DAY")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setOrderId(order_id)
                .build();

        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);

        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(responseDTO.getBody().getTxnToken());
        config.data.setOrderId(order_id);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        //to be confirmed
        if(theme.equalsIgnoreCase(Constants.Theme.CHECKOUTJS_WEB_REVAMP) || theme.equalsIgnoreCase(Constants.Theme.CHECKOUTJS_WAP_REVAMP)) {
            cashierPage.checkBoxPPI().check();
        }
        Assertions.assertThat(cashierPage.insufficientPPIBalanceIconMsg().getText()).isEqualTo("You do not have enough balance for this payment");
    }

    @Owner(Constants.Owner.PRIYANSHI)
    @Parameters({"theme"})
    @Test(description = "UPI paymode error should not be persistent")
    public void TC_01(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        String order_id = CommonHelpers.generateOrderId();
        User user = userManager.getForRead(Label.BASIC);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_UPI;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("50")
                .setSubscriptionPaymentMode("PPI")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("50")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setOrderId(order_id)
                .build();

        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(responseDTO.getBody().getTxnToken());
        config.data.setOrderId(order_id);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabUPI().click();
        cashierPage.textBoxVPA().clearAndType("upi@isgonnnafailafterthatvpa00077");
        cashierPage.verifyVPALinkText().click();
        cashierPage.buttonPGPayNow().click();
        cashierPage.invalidVpaText().assertVisible();
        cashierPage.closeCardPay().click();
        cashierPage.tabUPI().click();
        cashierPage.invalidVpaText().assertNotVisible();
    }

    @Owner(Constants.Owner.PRIYANSHI)
    @Parameters({"theme"})
    @Test(description = "In Subscription helper icon, the text on frequency needs to be changed when frequency is Fortnight in a particular scenario")
    public void TC_02(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        String order_id = CommonHelpers.generateOrderId();
        User user = userManager.getForRead(Label.BASIC);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_UPI;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("20")
                .setSubscriptionPaymentMode("PPI")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("50")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("FORTNIGHT")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setOrderId(order_id)
                .build();

        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(responseDTO.getBody().getTxnToken());
        config.data.setOrderId(order_id);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.subscriptionDetails().click();
        cashierPage.subscriptionDetails().waitUntilVisible();
        Assertions.assertThat(cashierPage.fortnightFreqText().getText().equals("Every fortnight"));
    }

    @Owner(Constants.Owner.PRIYANSHI)
    @Parameters({"theme"})
    @Test(description = "When the transaction is Add and Pay, there should be a message on the paymode that the Renewal will happen from wallet and not from this paymode.")
    public void TC_03(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        String order_id = CommonHelpers.generateOrderId();
        User user = userManager.getForWrite(Label.BASIC);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        WalletHelpers.modifyBalance(user,10.00);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_UPI;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("20")
                .setSubscriptionPaymentMode("PPI")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("50")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setOrderId(order_id)
                .build();

        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(responseDTO.getBody().getTxnToken());
        config.data.setOrderId(order_id);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabCreditCard().click();
        Assertions.assertThat(cashierPage.alertMessage().getText().equals("Subscription payments will happen via wallet"));
    }

    @Owner(Constants.Owner.PRIYANSHI)
    @Parameters({"theme"})
    @Test(description ="In case of Add and Pay txn, the name of UPI paymode should be UPI only")
    public void TC_04(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        String order_id = CommonHelpers.generateOrderId();
        User user = userManager.getForWrite(Label.BASIC);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        WalletHelpers.modifyBalance(user,10.00);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_UPI;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("50")
                .setSubscriptionPaymentMode("PPI")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("50")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setOrderId(order_id)
                .build();

        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(responseDTO.getBody().getTxnToken());
        config.data.setOrderId(order_id);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        if(theme.equals("checkoutjs_web")){
        Assertions.assertThat(cashierPage.tabUPI().getText()).isEqualTo("BHIM UPI");
        }
        else{
            Assertions.assertThat(cashierPage.tabUPI().getText()).isEqualTo("UPI");
        }

    }


    @Owner(Constants.Owner.PRIYANSHI)
    @Parameters({"theme"})
    @Test(description = "Amount to be deducted text need to be on the CTA. Proposed solution- We can put this text on the Pay button itself.")
    public void TC_05(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        String order_id = CommonHelpers.generateOrderId();
        User user = userManager.getForWrite(Label.BASIC);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
//        WalletHelpers.modifyBalance(user,10.00);
        Constants.MerchantType merchant = Constants.MerchantType.SUBS_UI_TEXT;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("50")
                .setSubscriptionPaymentMode("")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("50")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setOrderId(order_id)
                .setMandateAccountDetails(new MandateAccountDetails()) //Sending mandate
                .build();

        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(responseDTO.getBody().getTxnToken());
        config.data.setOrderId(order_id);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.BankMandateOption().click();
        cashierPage.buttonSecureSignIn().click();
        Assertions.assertThat(cashierPage.subConfirmTxtBM().getText().contains("Amount will be deducted within 2-4 days"));
    }

    @Owner(Constants.Owner.PRIYANSHI)
    @Parameters({"theme"})
    @Test(description = "UPI paymode name should be UPI autopay when user is not logged in")
    public void TC_06(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        String order_id = CommonHelpers.generateOrderId();
        User user = userManager.getForRead(Label.BASIC);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_UPI;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("50")
                .setSubscriptionPaymentMode("")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("50")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setOrderId(order_id)
                .build();

        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(responseDTO.getBody().getTxnToken());
        config.data.setOrderId(order_id);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        /* Commented the below line UPI AUTOPAY is now UPI only - PGP-35247 */
        //cashierPage.upiAutoPay().assertVisible();
        cashierPage.tabUPI().assertVisible();
    }

    @Owner(Constants.Owner.PRIYANSHI)
    @Parameters({"theme"})
    @Test(description = "Verify if Renewal Amount is named as Recurring Amount on Checkout JS and ribbon text for monthType= VARIABLE and FrequencyUnit =MONTH")
    public void toVerifyRecurringAmountAndRibbonTextLabel_InVariable_and_Month(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        String order_id = CommonHelpers.generateOrderId();
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", merchant)
                .setTxnValue("399")
                .setSubscriptionPaymentMode("PPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("500")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setOrderId(order_id)
                .build();

        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(responseDTO.getBody().getTxnToken());
        config.data.setOrderId(order_id);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.subsLabelDueInfo().assertVisible();
        cashierPage.subscriptionDetails().waitUntilVisible();
        cashierPage.subscriptionDetails().assertClickable();
        cashierPage.subscriptionDetails().click();
        cashierPage.clickPgOverlay();
        cashierPage.subsDetailsRecurringAmount().assertVisible();
        Assertions.assertThat(cashierPage.subsDetailsRecurringAmount().getText()).isEqualTo("Recurring Bill Amount*");
        cashierPage.amountToBePaid().assertVisible();
        //cashierPage.toBePaidTab().assertVisible();

    }

    @Owner(Constants.Owner.PRIYANSHI)
    @Parameters({"theme"})
    @Test(description = "Verify if Renewal Amount is named as Recurring Amount on Checkout JS and ribbon text for monthType= FIX and FrequencyUnit =MONTH")
    public void toVerifyRecurringAmountAndRibbonTextLabel_InFix_and_Month(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        String order_id = CommonHelpers.generateOrderId();
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", merchant)
                .setTxnValue("0")
                .setSubscriptionPaymentMode("PPI")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("50")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setOrderId(order_id)
                .build();

        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(responseDTO.getBody().getTxnToken());
        config.data.setOrderId(order_id);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.subsLabelDueInfo().waitUntilVisible();
        cashierPage.subsLabelDueInfo().assertVisible();
        cashierPage.subscriptionDetails().waitUntilVisible();
        cashierPage.subscriptionDetails().assertClickable();
        cashierPage.subscriptionDetails().click();
        cashierPage.clickPgOverlay();
        //Changes due to PGP-35247
        cashierPage.subsDetailsRecurringAmount().assertVisible();
        Assertions.assertThat(cashierPage.subsDetailsRecurringAmount().getText()).isEqualTo("Recurring Bill Amount*");
        //cashierPage.toBePaidTab().assertVisible();
        cashierPage.amountToBePaid().assertVisible();
    }

    @Owner(Constants.Owner.PRIYANSHI)
    @Parameters({"theme"})
    @Test(description = "Verify if Renewal Amount is named as Recurring Amount on Checkout JS and ribbon text for monthType= FIX and FrequencyUnit = ONDEMAND")
    public void toVerifyRecurringAmountAndRibbonTextLabel_InFix_and_Ondemand(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        String order_id = CommonHelpers.generateOrderId();
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.BANK_MANDATE;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", merchant)
                .setTxnValue("20")
                .setSubscriptionPaymentMode("PPI")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("50")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setOrderId(order_id)
                .build();

        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(responseDTO.getBody().getTxnToken());
        config.data.setOrderId(order_id);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.subsLabelDueInfo().assertVisible();
        cashierPage.subscriptionDetails().waitUntilVisible();
        cashierPage.subscriptionDetails().assertClickable();
        cashierPage.subscriptionDetails().click();
        cashierPage.clickPgOverlay();
        cashierPage.subsDetailsRecurringAmount().assertVisible();
        //Changed as per PGP-35247
        Assertions.assertThat(cashierPage.subsDetailsRecurringAmount().getText()).isEqualTo("Recurring Bill Amount*");
        cashierPage.toBePaidTab().assertVisible();
    }

    @Owner(Constants.Owner.PRIYANSHI)
    @Parameters({"theme"})
    @Test(description = "Verify if Renewal Amount is named as Recurring Amount on Checkout JS and ribbon text for monthType= VARIABLE and FrequencyUnit = ONDEMAND")
    public void toVerifyRecurringAmountAndRibbonTextLabel_InVariable_and_Ondemand(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        String order_id = CommonHelpers.generateOrderId();
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.BANK_MANDATE;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", merchant)
                .setTxnValue("10.50")
                .setSubscriptionPaymentMode("PPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("50")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setOrderId(order_id)
                .build();

        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(responseDTO.getBody().getTxnToken());
        config.data.setOrderId(order_id);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        //cashierPage.subsLabelDueInfo().assertNotVisible();
        cashierPage.subsLabelDueInfo().waitUntilVisible();
        cashierPage.subsLabelDueInfo().assertVisible();
        cashierPage.subscriptionDetails().assertClickable();
        cashierPage.subscriptionDetails().click();
        cashierPage.clickPgOverlay();
        cashierPage.subsDetailsRecurringAmount().assertVisible();
        //Changes due to PGP-35247
        Assertions.assertThat(cashierPage.subsDetailsRecurringAmount().getText()).isEqualTo("Recurring Bill Amount*");
        //cashierPage.toBePaidTab().assertVisible();
        cashierPage.amountToBePaid().assertVisible();
    }

    @Owner(Constants.Owner.TAMANA_TATHAN)
    @Feature("PGP-35247")
    @Parameters({"theme"})
    @Test(description = "Validate the UI Changes for YEAR and PPI")
    public void PGP_35247_TC_01_YEAR(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        String order_id = CommonHelpers.generateOrderId();
        User user = userManager.getForWrite(Label.BASIC);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        String SubscriptionEndDate = CommonHelpers.addYears(SubscriptionStartDate, "yyyy-MM-dd", 1);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PPI;
//        WalletHelpers.modifyBalance(user, 100.00);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("PPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("100")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("YEAR")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setSubscriptionExpiryDate(SubscriptionEndDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setOrderId(order_id)
                .build();

        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(responseDTO.getBody().getTxnToken());
        config.data.setOrderId(order_id);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        List<String> actualValues = new ArrayList<>();
        actualValues.add(cashierPage.toBePaidTab().getText());
        List<String> expectedValue = Arrays.asList("To be paid now", "₹10", "Recurring Bill Amount", "Upto ₹100", "Recurring Bill Frequency", "Every Year");

        List<UIElement> subsElementOnCashierPage = cashierPage.subscriptionDetailsOnCashierPage();
        for(int i=0; i<subsElementOnCashierPage.size(); i++){
            actualValues.add(subsElementOnCashierPage.get(i).getText());
        }
        for(int i=0; i<expectedValue.size(); i++){
            Assert.assertEquals(expectedValue.get(i), actualValues.get(i), "Validating subscription details on cashier page");
        }
        Assert.assertTrue(cashierPage.checkBoxPPI().isElementPresent(), "Paytm Wallet is displayed");
        cashierPage.subsLabelDueInfo().waitUntilVisible();
        cashierPage.subsLabelDueInfo().click();

        String[] validityStartDate = SubscriptionStartDate.split("-");
        String[] validityEndDate = SubscriptionEndDate.split("-");
        String validity = validityStartDate[2]+" "+ Month.of(Integer.parseInt(validityStartDate[1])).toString().substring(0,3)+" '"+validityStartDate[0].substring(2,4)+" - "+validityEndDate[2]+" "+ Month.of(Integer.parseInt(validityEndDate[1])).toString().substring(0,3)+" '"+validityEndDate[0].substring(2,4);
        String nextPayment = validityStartDate[2]+" "+Month.of(Integer.parseInt(validityStartDate[1])).toString().substring(0,3)+" '"+validityEndDate[0].substring(2,4);

        List<String> expectedValuesForSubsDetail = Arrays.asList("₹10", "Upto ₹100", validity, "Every Year", nextPayment);
        List<String> actualValuesForSubsDetail = cashierPage.getChildElementsText(cashierPage.subscriptionDetailsOnInfoTab());
        for(int i=0; i<expectedValuesForSubsDetail.size(); i++){
            Assert.assertTrue(actualValuesForSubsDetail.get(i).equalsIgnoreCase(expectedValuesForSubsDetail.get(i)), "Validating Subscription details on detail info tab");
        }
    }

    @Owner(Constants.Owner.TAMANA_TATHAN)
    @Feature("PGP-35247")
    @Parameters({"theme"})
    @Test(description = "Validate the UI Changes for MONTH and PPBL")
    public void PGP_35247_TC_02_MONTH(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        String order_id = CommonHelpers.generateOrderId();
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PPI;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("PPBL")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("100")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setOrderId(order_id)
                .build();

        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(responseDTO.getBody().getTxnToken());
        config.data.setOrderId(order_id);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.pause(10);

        List<String> actualValues = cashierPage.getChildElementsText(cashierPage.subscriptionDetailsOnCashierPage());
        Assert.assertTrue(actualValues.contains("Every Month"), "Validating Recurring Bill Frequency Value");
        Assert.assertTrue(cashierPage.checkboxPPBL().isElementPresent(), "PPBL is displayed");
        cashierPage.subsLabelDueInfo().waitUntilVisible();
        cashierPage.subsLabelDueInfo().click();

        String[] nextDateArray = SubscriptionStartDate.split("-");
        String nextPayment = nextDateArray[2]+" "+Month.of(Integer.parseInt(nextDateArray[1])+1).toString().toLowerCase().substring(0, 3)+" '"+nextDateArray[0].substring(2,4);
        List<String> actualValuesForSubsDetail = new ArrayList<>();
        List<UIElement> elementsOnSubsDetailWindow = cashierPage.subscriptionDetailsOnInfoTab();
        for(int i=0;i<elementsOnSubsDetailWindow.size();i++){
            cashierPage.pause(1);
            actualValuesForSubsDetail.add(elementsOnSubsDetailWindow.get(i).getText().toLowerCase());
        }
        Assert.assertTrue(actualValuesForSubsDetail.contains("every month"), "Validating Recurring Bill Frequency Value on details tray");
        Assert.assertTrue(actualValuesForSubsDetail.contains(nextPayment), "Validating Next Payment value for MONTH Subs");
    }

    @Owner(Constants.Owner.TAMANA_TATHAN)
    @Feature("PGP-35247")
    @Parameters({"theme"})
    @Test(description = "Validate the UI Changes for DAY , ADDnPAY")
    public void PGP_35247_TC_03_DAY(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        String order_id = CommonHelpers.generateOrderId();
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PPI;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("200")
                .setSubscriptionPaymentMode("")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("DAY")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setOrderId(order_id)
                .build();

        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(responseDTO.getBody().getTxnToken());
        config.data.setOrderId(order_id);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        List<String> actualValues = cashierPage.getChildElementsText(cashierPage.subscriptionDetailsOnCashierPage());
        Assert.assertFalse(actualValues.contains("Recurring Bill Frequency"), "As the Frequncy Unit is day , then Recurring Bill Frequency should not be visible");
        cashierPage.checkBoxPPI().click();
        cashierPage.pause(1);
        Assert.assertTrue(cashierPage.verifyPaymentModeDisplayed(Constants.PayMode.CC), "Validated AddnPay scenario");
        cashierPage.subsLabelDueInfo().waitUntilVisible();
        cashierPage.subsLabelDueInfo().click();
        List<String> actualValuesForSubsDetail = cashierPage.getChildElementsText(cashierPage.subscriptionDetailsOnInfoTab());
        Assert.assertFalse(actualValuesForSubsDetail.contains("Next Payment"));
    }

    @Owner(Constants.Owner.TAMANA_TATHAN)
    @Feature("PGP-35247")
    @Parameters({"theme"})
    @Test(description = "Validate the UI Changes for Amount Type = FIX and UPI")
    public void PGP_35247_TC_04_FIX(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        String order_id = CommonHelpers.generateOrderId();
        User user = userManager.getForRead(Label.BASIC);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_UPI;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("50")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("50")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setOrderId(order_id)
                .build();

        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(responseDTO.getBody().getTxnToken());
        config.data.setOrderId(order_id);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        List<String> actualValues  = cashierPage.getChildElementsText(cashierPage.subscriptionDetailsOnCashierPage());
        Assert.assertTrue(actualValues.contains("₹50"), "Validating the to be paid now field's value");
        Assert.assertTrue(actualValues.contains("₹50"), "validating the value of recurring Bill Amount as the Amount type is 'FIX' ");
        Assert.assertTrue(cashierPage.tabUPI().isElementPresent(), "Validating the Payment Mode = UPI");
        cashierPage.subsLabelDueInfo().waitUntilVisible();
        cashierPage.subsLabelDueInfo().click();
        List<String> actualValuesForSubsDetail = cashierPage.getChildElementsText(cashierPage.subscriptionDetailsOnInfoTab());
        Assert.assertTrue(actualValuesForSubsDetail.contains("₹50"), "Amount to be Paid is validated ");
    }

    @Owner(Constants.Owner.TAMANA_TATHAN)
    @Feature("PGP-35247")
    @Parameters({"theme"})
    @Test(description = "Validate the UI Changes for Ondemand")
    public void PGP_35247_TC_05_ONDEMAND(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        String order_id = CommonHelpers.generateOrderId();
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.BANK_MANDATE;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("50")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setOrderId(order_id)
                .build();

        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(responseDTO.getBody().getTxnToken());
        config.data.setOrderId(order_id);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        List<String> actualValues  = cashierPage.getChildElementsText(cashierPage.subscriptionDetailsOnCashierPage());
        //Assert.assertTrue(actualValues.contains("On-Demand"));
        cashierPage.subsLabelDueInfo().waitUntilVisible();
        cashierPage.subsLabelDueInfo().click();
        List<String> actualValuesForSubsDetail = cashierPage.getChildElementsText(cashierPage.subscriptionDetailsOnInfoTab());
        Assert.assertTrue(actualValuesForSubsDetail.contains("On Demand"), "Validating frequency on subscription details page ");
        Assert.assertTrue(actualValuesForSubsDetail.size()==4, "Validating the Next payment field is not present");

    }

    @Owner(Constants.Owner.PUSPA)
    @Feature("PGP-37902")
    @Parameters({"theme"})
    @Test(description = "Verify for closed wallet user RBI guidelines error message should be shown")
    public void verify_closed_wallet_user_error_message_SubsFlow(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        String order_id = CommonHelpers.generateOrderId();
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        User user = userManager.getForRead(Label.DEACTIVATEDUSER);
        Constants.MerchantType merchant = Constants.MerchantType.DEACTIVATED_WALLET;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("100")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setOrderId(order_id)
                .build();

        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(responseDTO.getBody().getTxnToken());
        config.data.setOrderId(order_id);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        Assertions.assertThat(cashierPage.getUserDeactivatedErrorMessage().getText()).isEqualTo("Your wallet has been deactivated as mandated by RBI.Know more");
        Assertions.assertThat(cashierPage.isWalletDisabled()).isTrue();
        Assertions.assertThat(cashierPage.getKnowMoreText()).isEqualTo("As per RBI guidelines, all wallet accounts with no transactions in the past one year have been deactivated.");

    }
    @Owner(Constants.Owner.VISHNU_SHEKAR)
    @Feature("PGP-40026")
    @Parameters({"theme"})
    @Test(description = "Verify the upi text and verify default vpa suggestions")
    public void PGP_40026_TC_01_VerifyDefaultUPIHandles(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        String order_id = CommonHelpers.generateOrderId();
        User user = userManager.getForRead(Label.BASIC);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_UPI;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setOrderId(order_id)
                .build();
        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(responseDTO.getBody().getTxnToken());
        config.data.setOrderId(order_id);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabUPI().click();
        cashierPage.textBoxVPA().clearAndType("srivastavaprateek@");
        Assertions.assertThat(cashierPage.upiHandlers())
                .as("UPI handlers")
                .containsSequence("@paytm@upi@ybl@ibl@axl@okhdfcbank");
    }
    @Owner(Constants.Owner.VISHNU_SHEKAR)
    @Feature("PGP-40026")
    @Parameters({"theme"})
    @Test(description = "Verify the upi text and verify additional vpa suggestions")
    public void PGP_40026_TC_02_VerifyAdditionalUPIHandles(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        String[] upiHandles = {"@okaxis", "@apl", "@indus", "@boi", "@cnrb", "@icici", "@dbs"};
        String order_id = CommonHelpers.generateOrderId();
        User user = userManager.getForRead(Label.BASIC);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_UPI;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setOrderId(order_id)
                .build();
        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(responseDTO.getBody().getTxnToken());
        config.data.setOrderId(order_id);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabUPI().click();
        for (int i = 0; i < upiHandles.length; i++) {
            cashierPage.waitUntilLoads();
            Assertions.assertThat(cashierPage.getEnhancedUPIText().getText()).isEqualTo("Enter UPI ID");
            if(i<=0) {
                Assertions.assertThat(cashierPage.getEnhancedUPIText().getText()).isEqualTo("Enter UPI ID");
                cashierPage.textBoxVPA().clearAndType("srivastavaprateek" + upiHandles[i]);
                Assertions.assertThat(cashierPage.enhancedUPIHandles().getText()).isEqualTo(upiHandles[i]);
            }
            else{
                cashierPage.retryVPATextBox().clearAndType("srivastavaprateek" + upiHandles[i]);
                Assertions.assertThat(cashierPage.enhancedUPIHandles().getText()).isEqualTo(upiHandles[i]);
            }
        }
    }
    @Owner(Constants.Owner.VISHNU_SHEKAR)
    @Feature("PGP-40026")
    @Parameters({"theme"})
    @Test(description = "Validate Debit Card is selected as Default Authentication Mode is Debit Card")
    public void PGP_40026_TC_BankMandateDefaultAuthMode(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        String order_id = CommonHelpers.generateOrderId();
        User user = userManager.getForRead(Label.BASIC);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.BANK_MANDATE;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setOrderId(order_id)
                .build();
        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(responseDTO.getBody().getTxnToken());
        config.data.setOrderId(order_id);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.BankMandateRadioButton().click();
        cashierPage.waitUntilLoads();
        cashierPage.getBankMandateList().get(0).click();
        Assertions.assertThat(cashierPage.getDefaultAuthMode().isSelected()).isTrue();
        Assertions.assertThat(cashierPage.getDefaultAuthMode().getAttribute("value").equals("DEBIT_CARD"));
    }
    @Owner(Constants.Owner.VISHNU_SHEKAR)
    @Feature("PGP_41837")
    @Parameters({"theme"})
    @Test(description = "Validate JS Bank UI Changes")
    public void PGP_41837_TC01_BankMandateJS(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        String order_id = CommonHelpers.generateOrderId();
        User user = userManager.getForRead(Label.BASIC);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.BANK_MANDATE;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setOrderId(order_id)
                .build();
        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(responseDTO.getBody().getTxnToken());
        config.data.setOrderId(order_id);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.BankMandateRadioButton().click();
        cashierPage.waitUntilLoads();
        String bankHeading = cashierPage.selectBankHeading().getText();
        Assertions.assertThat(bankHeading).contains("Select your Bank");

    }

    @Owner(Constants.Owner.VISHNU_SHEKAR)
    @Feature("PGP_41837")
    @Parameters({"theme"})
    @Test(description = "Validate JS Bank UI Changes")
    public void PGP_41837_TC02_BankMandateJS(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        {
            String order_id = CommonHelpers.generateOrderId();
            User user = userManager.getForWrite(Label.BASIC);
            String SubscriptionStartDate = CommonHelpers.getDate().toString();
            Constants.MerchantType merchant = Constants.MerchantType.SIHUB_Subs;
            InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                    .setTxnValue("0")
                    .setSubscriptionPaymentMode("")
                    .setSubscriptionAmountType("VARIABLE")
                    .setSubscriptionMaxAmount("50")
                    .setSubscriptionFrequency("1")
                    .setSubscriptionFrequencyUnit("MONTH")
                    .setSubscriptionGraceDays("0")
                    .setSubscriptionStartDate(SubscriptionStartDate)
                    .setRequestType("NATIVE_SUBSCRIPTION")
                    .setOrderId(order_id)
                    .setMandateAccountDetails(new MandateAccountDetails())
                    .build();

            InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);
            MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
            config.data.setToken(responseDTO.getBody().getTxnToken());
            config.data.setOrderId(order_id);
            checkoutPage.createCheckoutJsOrder(config);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.BankMandateOption().click();
            cashierPage.buttonSecureSignIn().click();
            Assertions.assertThat(cashierPage.buttonPGPayNow().getText()).doesNotContain("Amount will be deducted within 2-4 days");
        }

    }

    @Owner(Constants.Owner.VISHNU_SHEKAR)
    @Feature("PGP_41837")
    @Parameters({"theme"})
    @Test(description = "Verify Checkout JS flow text changes when Auto Refund is True")
    public void TCPGP_41837_AutoRefundisTrueJCTA(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        String order_id = CommonHelpers.generateOrderId();
        User user = userManager.getForWrite(Label.LOGIN);
        WalletHelpers.modifyBalance(user, 10.00);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.SIHUB_Subs;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("0")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("50")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setOrderId(order_id)
                .setMandateAccountDetails(new MandateAccountDetails())
                .build();

        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(responseDTO.getBody().getTxnToken());
        config.data.setOrderId(order_id);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.checkBoxPPI().click();
        String walletCTA = cashierPage.payText().getText();
        Assertions.assertThat(walletCTA).contains("₹1 will be deducted now for account verification & refunded within 2-4 days");
        cashierPage.tabCreditCard().click();
        String cardCTA = cashierPage.payText().getText();
        Assertions.assertThat(cardCTA).contains("₹1 will be deducted now for account verification & refunded within 2-4 days");

    }

    @Owner(Constants.Owner.VISHNU_SHEKAR)
    @Feature("PGP_41837")
    @Parameters({"theme"})
    @Test(description = "Validate JS UI Changes")
    public void TCPGP_41837_AutoRefundisFalseJSUPItCTA(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        String order_id = CommonHelpers.generateOrderId();
        User user = userManager.getForWrite(Label.LOGIN);
        WalletHelpers.modifyBalance(user, 10.00);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.BANK_MANDATE;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("0")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("50")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setOrderId(order_id)
                .setMandateAccountDetails(new MandateAccountDetails())
                .build();
        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(responseDTO.getBody().getTxnToken());
        config.data.setOrderId(order_id);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.checkBoxPPI().click();
        String walletCTA = cashierPage.payText().getText();
        Assertions.assertThat(walletCTA).contains("₹1 will be deducted now for account verification");
    }
    @Owner(Constants.Owner.VISHNU_SHEKAR)
    @Feature("PGP_41837")
    @Parameters({"theme"})
    @Test(description = "Validate JS Bank UI Changes")
    public void TCPGP_41837_AutoRefundisFalseJSWalletCTA(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        String order_id = CommonHelpers.generateOrderId();
        User user = userManager.getForRead(Label.LOGIN);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.AUTOREFUND_FALSE;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("0")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("50")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setOrderId(order_id)
                .setMandateAccountDetails(new MandateAccountDetails())
                .build();
        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(responseDTO.getBody().getTxnToken());
        config.data.setOrderId(order_id);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.checkBoxPPI().click();
        String walletCTA = cashierPage.payText().getText();
        Assertions.assertThat(walletCTA).contains("₹1 will be deducted now for account verification");
    }

}
