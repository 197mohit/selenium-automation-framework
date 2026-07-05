package scripts.api.UPI;

import com.paytm.api.nativeAPI.FetchPaymentOptionV5;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.WalletHelpers;
import com.paytm.apphelpers.supercashhelpers.superCashHelper;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.checkoutjs.MerchantConfig;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutJsCheckoutPage;
import com.paytm.pages.CheckoutPage;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;
import org.testng.annotations.Parameters;
import org.testng.annotations.Optional;

public class UPIErrorCode extends PGPBaseTest {
    private final CheckoutJsCheckoutPage checkoutJsCheckoutPage = new CheckoutJsCheckoutPage();
    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PGP-50167")
    @Parameters({"theme"})
    @Test(description = "Verify Error MSG for Overall UPI Limit Exhaust ")
    public void Overall_UPI_Limit_Exhaust(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.PPBL);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_OVERALL_LIMIT;
        //WalletHelpers.modifyBalance(user,10.0);
        String orderID = CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("110000.00")
                .setSsoToken(user.ssoToken())
                .setOrderId(orderID)
                .setChannelId("WEB")
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").setIsLiteEligible(true).build();

        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        String userErrorMsgForUPI=fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.userErrorMessage");
        System.out.println("User error msg in fpo: "+fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.userErrorMessage"));
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.userErrorMessage")).isEqualTo("UPI transaction above Rs. 100000 is not allowed for this merchant category.");

        MerchantConfig config = checkoutJsCheckoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutJsCheckoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.validateUPILimitMsg(userErrorMsgForUPI);
    }

    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PGP-50167")
    @Parameters({"theme"})
    @Test(description = "Verify Error MSG for UPI Collect Per transaction Limit Exhaust ")
    public void UPI_Collect_PerTxn_Limit_Exhaust(@Optional("checkoutjs_wap_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.PPBL);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_COLLECT_LIMIT;
        //WalletHelpers.modifyBalance(user,10.0);
        String orderID = CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("20.00")
                .setSsoToken(user.ssoToken())
                .setOrderId(orderID)
                .setChannelId("WEB")
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").setIsLiteEligible(true).build();

        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        String userErrorMsgForUPI=fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.payOptionRemainingLimits.find {it.limitType== 'UPI'}.userErrorMessage");
        System.out.println("User error msg in fpo: "+fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.payOptionRemainingLimits.find {it.limitType== 'UPI'}.userErrorMessage"));
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.payOptionRemainingLimits.find {it.limitType== 'UPI'}.userErrorMessage")).isEqualTo("Merchant cannot accept payments on UPI at the moment. Try using other options.");

        MerchantConfig config = checkoutJsCheckoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutJsCheckoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabUPI().waitUntilClickable();
        cashierPage.tabUPI().click();
        cashierPage.tabUPICollect().click();
        cashierPage.validateCollectUPILimitMsg(userErrorMsgForUPI);
    }


    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PGP-50167")
    @Parameters({"theme"})
    @Test(description = "Verify Error MSG for UPI Intent Per Transaction Limit Exhaust ")
    public void UPI_Intent_PerTxn_Limit_Exhaust(@Optional("checkoutjs_wap_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.PPBL);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_INTENT_LIMIT;
        String orderID = CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("20.00")
                .setSsoToken(user.ssoToken())
                .setOrderId(orderID)
                .setChannelId("WEB")
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").setIsLiteEligible(true).build();

        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        String userErrorMsgForUPI=fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.payOptionRemainingLimits.find {it.limitType== 'UPI_PUSH'}.userErrorMessage");
        System.out.println("User error msg in fpo: "+fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.payOptionRemainingLimits.find {it.limitType== 'UPI_PUSH'}.userErrorMessage"));
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.payOptionRemainingLimits.find {it.limitType== 'UPI_PUSH'}.userErrorMessage")).isEqualTo("Merchant cannot accept payments on UPI at the moment. Try using other options.");

        MerchantConfig config = checkoutJsCheckoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutJsCheckoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabUPI().waitUntilClickable();
        cashierPage.tabUPI().click();
        cashierPage.tabUPICollect().click();
        cashierPage.validateIntentUPILimitMsg(userErrorMsgForUPI);
    }


    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PGP-50167")
    @Parameters({"theme"})
    @Test(description = "Verify Error MSG for UPI PPI Wallet PerTxn Limit Exhaust ")
    public void UPI_PPI_WALLET_PerTxn_Limit_Exhaust(@Optional("checkoutjs_wap_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.PPBL);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_PPI_WALLET;
        String orderID = CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("20.00")
                .setSsoToken(user.ssoToken())
                .setOrderId(orderID)
                .setChannelId("WEB")
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").setIsLiteEligible(true).build();

        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        String userErrorMsgForUPI=fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.subPayModeRemainingLimits.find {it.limitType == 'PPIWALLET'}.userErrorMessage");
        System.out.println("User error msg in fpo: "+fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.subPayModeRemainingLimits.find {it.limitType == 'PPIWALLET'}.userErrorMessage"));
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.subPayModeRemainingLimits.find {it.limitType == 'PPIWALLET'}.userErrorMessage")).isEqualTo("Merchant cannot accept Wallet on UPI at the moment. Try using other options.");

        MerchantConfig config = checkoutJsCheckoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutJsCheckoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabUPI().waitUntilClickable();

        cashierPage.tabUPI().click();
        cashierPage.validateUPIPPIWalletLimitMsg(userErrorMsgForUPI);

    }

    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PGP-50167")
    @Parameters({"theme"})
    @Test(description = "Verify Error MSG for UPI CC Per Transaction Limit Exhaust ")
    public void UPI_CC_PerTxn_Limit_Exhaust(@Optional("checkoutjs_wap_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.PPBL);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_CC_LIMITUI;
        String orderID = CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("20.00")
                .setSsoToken(user.ssoToken())
                .setOrderId(orderID)
                .setChannelId("WEB")
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").setIsLiteEligible(true).build();

        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        String userErrorMsgForUPI=fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.payOptionRemainingLimits.find {it.limitType == 'UPI_CC'}.userErrorMessage");
        System.out.println("User error msg in fpo: "+fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.payOptionRemainingLimits.find {it.limitType == 'UPI_CC'}.userErrorMessage"));
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.payOptionRemainingLimits.find {it.limitType == 'UPI_CC'}.userErrorMessage")).isEqualTo(userErrorMsgForUPI);

        MerchantConfig config = checkoutJsCheckoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutJsCheckoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabUPI().waitUntilClickable();


        cashierPage.validateUPICCLimitMsg(userErrorMsgForUPI);
        cashierPage.tabUPI().click();
        cashierPage.validateUPICCLimitMsgInnerUPI(userErrorMsgForUPI);
        cashierPage.tabUPICollect().click();
        cashierPage.validateUPICCLimitMsgInnerUPI(userErrorMsgForUPI);

    }

    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PGP-50167")
    @Parameters({"theme"})
    @Test(description = "Verify Eligibility MSG for UPI CC and PPI Wallet ")
    public void Eligibility_UPICC_PPIWALLET(@Optional("checkoutjs_wap_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.PPBL);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_CC_PPI_WALLET_Eligibility;
        String orderID = CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("20.00")
                .setSsoToken(user.ssoToken())
                .setOrderId(orderID)
                .setChannelId("WEB")
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").setIsLiteEligible(true).build();

        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        String userErrorMsgForUPI=fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.payOptionRemainingLimits.find {it.limitType == 'UPI_CC'}.userErrorMessage");
        System.out.println("User error msg in fpo: "+fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.payOptionRemainingLimits.find {it.limitType == 'UPI_CC'}.userErrorMessage"));
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.payOptionRemainingLimits.find {it.limitType == 'UPI_CC'}.userErrorMessage")).isEqualTo("This Merchant can accept Credit Card on UPI upto Rs. 2000 only.");

        MerchantConfig config = checkoutJsCheckoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutJsCheckoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabUPI().waitUntilClickable();

        cashierPage.tabUPI().click();
        cashierPage.EligibilityUPICC_PPIWALLET();

    }


    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PGP-50167")
    @Parameters({"theme"})
    @Test(description = "Verify Error MSG for UPI CC is not allowed")
    public void UPI_CC_NotAllowed(@Optional("checkoutjs_wap_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.PPBL);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_PPI_WALLET;
        String orderID = CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("20.00")
                .setSsoToken(user.ssoToken())
                .setOrderId(orderID)
                .setChannelId("WEB")
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").setIsLiteEligible(true).build();

        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        String userErrorMsgForUPI=fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.payOptionRemainingLimits.find {it.limitType == 'UPI_CC'}.userErrorMessage");
        System.out.println("User error msg in fpo: "+fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.payOptionRemainingLimits.find {it.limitType == 'UPI_CC'}.userErrorMessage"));
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.payOptionRemainingLimits.find {it.limitType == 'UPI_CC'}.userErrorMessage")).isEqualTo("This merchant is not accepting Credit Card on UPI.");

        MerchantConfig config = checkoutJsCheckoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutJsCheckoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabUPI().waitUntilClickable();

        cashierPage.tabUPI().click();
        cashierPage.validateUPICCNotAllowed(userErrorMsgForUPI);


    }

    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PGP-50167")
    @Parameters({"theme"})
    @Test(description = "Verify Error MSG for UPI Collect Per Month Limit Exhaust ")
    public void UPI_Collect_PerMonth_Limit_Exhaust(@Optional("checkoutjs_wap_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.PPBL);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_COLLECT_LIMIT;
        //WalletHelpers.modifyBalance(user,10.0);
        String orderID = CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("20.00")
                .setSsoToken(user.ssoToken())
                .setOrderId(orderID)
                .setChannelId("WEB")
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").setIsLiteEligible(true).build();

        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        String userErrorMsgForUPI=fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.payOptionRemainingLimits.find {it.limitType== 'UPI'}.userErrorMessage");
        System.out.println("User error msg in fpo: "+fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.payOptionRemainingLimits.find {it.limitType== 'UPI'}.userErrorMessage"));
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.payOptionRemainingLimits.find {it.limitType== 'UPI'}.userErrorMessage")).isEqualTo("Merchant cannot accept payments on UPI at the moment. Try using other options.");

        MerchantConfig config = checkoutJsCheckoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutJsCheckoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabUPI().waitUntilClickable();
        cashierPage.tabUPI().click();
        cashierPage.tabUPICollect().click();
        cashierPage.validateCollectUPILimitMsg(userErrorMsgForUPI);
    }
    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PGP-50167")
    @Parameters({"theme"})
    @Test(description = "Verify Error MSG for UPI Intent Per Month Limit Exhaust ")
    public void UPI_Intent_PerMonth_Limit_Exhaust(@Optional("checkoutjs_wap_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.PPBL);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_INTENT_LIMIT;
        String orderID = CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("20.00")
                .setSsoToken(user.ssoToken())
                .setOrderId(orderID)
                .setChannelId("WEB")
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").setIsLiteEligible(true).build();

        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        String userErrorMsgForUPI=fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.payOptionRemainingLimits.find {it.limitType== 'UPI_PUSH'}.userErrorMessage");
        System.out.println("User error msg in fpo: "+fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.payOptionRemainingLimits.find {it.limitType== 'UPI_PUSH'}.userErrorMessage"));
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.payOptionRemainingLimits.find {it.limitType== 'UPI_PUSH'}.userErrorMessage")).isEqualTo("Merchant cannot accept payments on UPI at the moment. Try using other options.");

        MerchantConfig config = checkoutJsCheckoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutJsCheckoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabUPI().waitUntilClickable();
        cashierPage.tabUPI().click();
        cashierPage.tabUPICollect().click();
        cashierPage.validateIntentUPILimitMsg(userErrorMsgForUPI);
    }
    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PGP-50167")
    @Parameters({"theme"})
    @Test(description = "Verify Error MSG for UPI PPI Wallet PerMonth Transaction Limit Exhaust ")
    public void UPI_PPI_WALLET_PerMonth_Limit_Exhaust(@Optional("checkoutjs_wap_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.PPBL);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_PPI_WALLET;
        String orderID = CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("20.00")
                .setSsoToken(user.ssoToken())
                .setOrderId(orderID)
                .setChannelId("WEB")
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").setIsLiteEligible(true).build();

        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        String userErrorMsgForUPI=fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.subPayModeRemainingLimits.find {it.limitType == 'PPIWALLET'}.userErrorMessage");
        System.out.println("User error msg in fpo: "+fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.subPayModeRemainingLimits.find {it.limitType == 'PPIWALLET'}.userErrorMessage"));
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.subPayModeRemainingLimits.find {it.limitType == 'PPIWALLET'}.userErrorMessage")).isEqualTo("Merchant cannot accept Wallet on UPI at the moment. Try using other options.");

        MerchantConfig config = checkoutJsCheckoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutJsCheckoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabUPI().waitUntilClickable();

        cashierPage.tabUPI().click();
        cashierPage.validateUPIPPIWalletLimitMsg(userErrorMsgForUPI);

        cashierPage.tabUPICollect().click();
        cashierPage.validateColletPPIWalletLimitMsg(userErrorMsgForUPI);
        cashierPage.validateIntentPPIWalletLimitMsg(userErrorMsgForUPI);

    }
    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PGP-50167")
    @Parameters({"theme"})
    @Test(description = "Verify Error MSG for UPI CC Per Month Transaction Limit Exhaust ")
    public void UPI_CC_PerMonth_Limit_Exhaust(@Optional("checkoutjs_wap_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.PPBL);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_CC_LIMITUI;
        String orderID = CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("20.00")
                .setSsoToken(user.ssoToken())
                .setOrderId(orderID)
                .setChannelId("WEB")
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").setIsLiteEligible(true).build();

        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        String userErrorMsgForUPI=fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.payOptionRemainingLimits.find {it.limitType == 'UPI_CC'}.userErrorMessage");
        System.out.println("User error msg in fpo: "+fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.payOptionRemainingLimits.find {it.limitType == 'UPI_CC'}.userErrorMessage"));
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.payOptionRemainingLimits.find {it.limitType == 'UPI_CC'}.userErrorMessage")).isEqualTo(userErrorMsgForUPI);

        MerchantConfig config = checkoutJsCheckoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutJsCheckoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabUPI().waitUntilClickable();


        cashierPage.validateUPICCLimitMsg(userErrorMsgForUPI);
        cashierPage.tabUPI().click();
        cashierPage.validateUPICCLimitMsgInnerUPI(userErrorMsgForUPI);
        cashierPage.tabUPICollect().click();
        cashierPage.validateUPICCLimitMsgInnerUPI(userErrorMsgForUPI);

    }
    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PGP-50167")
    @Parameters({"theme"})
    @Test(description = "Verify Error MSG for UPI Collect Daily Limit Exhaust ")
    public void UPI_Collect_Daily_Limit_Exhaust(@Optional("checkoutjs_wap_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.PPBL);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_COLLECT_LIMIT;
        //WalletHelpers.modifyBalance(user,10.0);
        String orderID = CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("2100.00")
                .setSsoToken(user.ssoToken())
                .setOrderId(orderID)
                .setChannelId("WEB")
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").setIsLiteEligible(true).build();

        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        String userErrorMsgForUPI=fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.payOptionRemainingLimits.find {it.limitType== 'UPI'}.userErrorMessage");
        System.out.println("User error msg in fpo: "+fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.payOptionRemainingLimits.find {it.limitType== 'UPI'}.userErrorMessage"));
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.payOptionRemainingLimits.find {it.limitType== 'UPI'}.userErrorMessage")).isEqualTo("You can pay upto Rs. 2000 using this payment option. Please use other options to pay");

        MerchantConfig config = checkoutJsCheckoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutJsCheckoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabUPI().waitUntilClickable();
        cashierPage.tabUPI().click();
        //cashierPage.tabUPICollect().click();
        cashierPage.validateCollectUPILimitMsg(userErrorMsgForUPI);
    }
    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PGP-50167")
    @Parameters({"theme"})
    @Test(description = "Verify Error MSG for UPI Intent Daily Limit Exhaust ")
    public void UPI_Intent_Daily_Limit_Exhaust(@Optional("checkoutjs_wap_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.PPBL);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_INTENT_LIMIT;
        String orderID = CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("20.00")
                .setSsoToken(user.ssoToken())
                .setOrderId(orderID)
                .setChannelId("WEB")
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").setIsLiteEligible(true).build();

        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        String userErrorMsgForUPI=fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.payOptionRemainingLimits.find {it.limitType== 'UPI_PUSH'}.userErrorMessage");
        System.out.println("User error msg in fpo: "+fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.payOptionRemainingLimits.find {it.limitType== 'UPI_PUSH'}.userErrorMessage"));
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.payOptionRemainingLimits.find {it.limitType== 'UPI_PUSH'}.userErrorMessage")).isEqualTo("Merchant cannot accept payments on UPI at the moment. Try using other options.");

        MerchantConfig config = checkoutJsCheckoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutJsCheckoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabUPI().waitUntilClickable();
        cashierPage.tabUPI().click();
        cashierPage.tabUPICollect().click();
        cashierPage.validateIntentUPILimitMsg(userErrorMsgForUPI);
    }
    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PGP-50167")
    @Parameters({"theme"})
    @Test(description = "Verify Error MSG for UPI PPI Wallet Weekly Limit Exhaust ")
    public void UPI_PPI_WALLET_Weekly_Limit_Exhaust(@Optional("checkoutjs_wap_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.PPBL);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_PPI_WALLET;
        String orderID = CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("20.00")
                .setSsoToken(user.ssoToken())
                .setOrderId(orderID)
                .setChannelId("WEB")
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").setIsLiteEligible(true).build();

        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        String userErrorMsgForUPI=fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.subPayModeRemainingLimits.find {it.limitType == 'PPIWALLET'}.userErrorMessage");
        System.out.println("User error msg in fpo: "+fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.subPayModeRemainingLimits.find {it.limitType == 'PPIWALLET'}.userErrorMessage"));
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.subPayModeRemainingLimits.find {it.limitType == 'PPIWALLET'}.userErrorMessage")).isEqualTo("Merchant cannot accept Wallet on UPI at the moment. Try using other options.");

        MerchantConfig config = checkoutJsCheckoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutJsCheckoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabUPI().waitUntilClickable();

        cashierPage.tabUPI().click();
        cashierPage.validateUPIPPIWalletLimitMsg(userErrorMsgForUPI);

        cashierPage.tabUPICollect().click();
        cashierPage.validateColletPPIWalletLimitMsg(userErrorMsgForUPI);
        cashierPage.validateIntentPPIWalletLimitMsg(userErrorMsgForUPI);

    }
    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PGP-50167")
    @Parameters({"theme"})
    @Test(description = "Verify Error MSG for UPI CC Daily Transaction Limit Exhaust ")
    public void UPI_CC_Daily_Limit_Exhaust(@Optional("checkoutjs_wap_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.PPBL);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_CC_LIMITUI;
        String orderID = CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("20.00")
                .setSsoToken(user.ssoToken())
                .setOrderId(orderID)
                .setChannelId("WEB")
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").setIsLiteEligible(true).build();

        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        String userErrorMsgForUPI=fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.payOptionRemainingLimits.find {it.limitType == 'UPI_CC'}.userErrorMessage");
        System.out.println("User error msg in fpo: "+fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.payOptionRemainingLimits.find {it.limitType == 'UPI_CC'}.userErrorMessage"));
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.payOptionRemainingLimits.find {it.limitType == 'UPI_CC'}.userErrorMessage")).isEqualTo(userErrorMsgForUPI);

        MerchantConfig config = checkoutJsCheckoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutJsCheckoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabUPI().waitUntilClickable();


        cashierPage.validateUPICCLimitMsg(userErrorMsgForUPI);
        cashierPage.tabUPI().click();
        cashierPage.validateUPICCLimitMsgInnerUPI(userErrorMsgForUPI);
        cashierPage.tabUPICollect().click();
        cashierPage.validateUPICCLimitMsgInnerUPI(userErrorMsgForUPI);

    }

    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PGP-50167")
    @Parameters({"theme"})
    @Test(description = "Verify Error MSG for UPI Intent and Collect Daily Limit Exhaust ")
    public void UPI_Intent_Collect_Daily_Limit_Exhaust(@Optional("checkoutjs_wap_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.PPBL);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_COLLECT_INTENT_LIMIT;
        String orderID = CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("20.00")
                .setSsoToken(user.ssoToken())
                .setOrderId(orderID)
                .setChannelId("WEB")
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").setIsLiteEligible(true).build();

        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        String userErrorMsgForUPIIntent=fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.payOptionRemainingLimits.find {it.limitType== 'UPI_PUSH'}.userErrorMessage");
        String userErrorMsgForUPICollect=fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.payOptionRemainingLimits.find {it.limitType== 'UPI'}.userErrorMessage");
        System.out.println("User error msg in fpo for INTENT: "+fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.payOptionRemainingLimits.find {it.limitType== 'UPI_PUSH'}.userErrorMessage"));
        System.out.println("User error msg in fpo for COLLECT: "+fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.payOptionRemainingLimits.find {it.limitType== 'UPI'}.userErrorMessage"));
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.payOptionRemainingLimits.find {it.limitType== 'UPI_PUSH'}.userErrorMessage")).isEqualTo("Merchant cannot accept payments on UPI at the moment. Try using other options.");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.payOptionRemainingLimits.find {it.limitType== 'UPI'}.userErrorMessage")).isEqualTo("Merchant cannot accept payments on UPI at the moment. Try using other options.");

        MerchantConfig config = checkoutJsCheckoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutJsCheckoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabUPI().waitUntilClickable();
        cashierPage.tabUPI().click();
        cashierPage.validateUPILimitMsg(userErrorMsgForUPIIntent);
    }
    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PGP-50167")
    @Parameters({"theme"})
    @Test(description = "Verify Error MSG for UPI Intent and Collect Monthly Limit Exhaust ")
    public void UPI_Intent_Collect_Monthly_Limit_Exhaust(@Optional("checkoutjs_wap_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.PPBL);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_COLLECT_INTENT_LIMIT;
        String orderID = CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("20.00")
                .setSsoToken(user.ssoToken())
                .setOrderId(orderID)
                .setChannelId("WEB")
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").setIsLiteEligible(true).build();

        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        String userErrorMsgForUPIIntent=fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.payOptionRemainingLimits.find {it.limitType== 'UPI_PUSH'}.userErrorMessage");
        String userErrorMsgForUPICollect=fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.payOptionRemainingLimits.find {it.limitType== 'UPI'}.userErrorMessage");
        System.out.println("User error msg in fpo for INTENT: "+fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.payOptionRemainingLimits.find {it.limitType== 'UPI_PUSH'}.userErrorMessage"));
        System.out.println("User error msg in fpo for COLLECT: "+fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.payOptionRemainingLimits.find {it.limitType== 'UPI'}.userErrorMessage"));
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.payOptionRemainingLimits.find {it.limitType== 'UPI_PUSH'}.userErrorMessage")).isEqualTo("Merchant cannot accept payments on UPI at the moment. Try using other options.");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.payOptionRemainingLimits.find {it.limitType== 'UPI'}.userErrorMessage")).isEqualTo("Merchant cannot accept payments on UPI at the moment. Try using other options.");

        MerchantConfig config = checkoutJsCheckoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutJsCheckoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabUPI().waitUntilClickable();
        cashierPage.tabUPI().click();
        cashierPage.validateUPILimitMsg(userErrorMsgForUPIIntent);
    }

    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PGP-50167")
    @Parameters({"theme"})
    @Test(description = "Verify Error MSG for UPI Intent and Collect Per Transacton Limit Exhaust ")
    public void UPI_Intent_Collect_PerTxn_Limit_Exhaust(@Optional("checkoutjs_web") String theme) throws Exception {
        User user = userManager.getForRead(Label.PPBL);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_COLLECT_INTENT_LIMIT;
        String orderID = CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("20.00")
                .setSsoToken(user.ssoToken())
                .setOrderId(orderID)
                .setChannelId("WAP")
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").setIsLiteEligible(true).build();

        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        String userErrorMsgForUPIIntent=fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.payOptionRemainingLimits.find {it.limitType== 'UPI_PUSH'}.userErrorMessage");
        String userErrorMsgForUPICollect=fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.payOptionRemainingLimits.find {it.limitType== 'UPI'}.userErrorMessage");
        System.out.println("User error msg in fpo for INTENT: "+fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.payOptionRemainingLimits.find {it.limitType== 'UPI_PUSH'}.userErrorMessage"));
        System.out.println("User error msg in fpo for COLLECT: "+fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.payOptionRemainingLimits.find {it.limitType== 'UPI'}.userErrorMessage"));
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.payOptionRemainingLimits.find {it.limitType== 'UPI_PUSH'}.userErrorMessage")).isEqualTo("Merchant cannot accept payments on UPI at the moment. Try using other options.");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.payOptionRemainingLimits.find {it.limitType== 'UPI'}.userErrorMessage")).isEqualTo("Merchant cannot accept payments on UPI at the moment. Try using other options.");

        MerchantConfig config = checkoutJsCheckoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutJsCheckoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabUPI().waitUntilClickable();
        cashierPage.tabUPI().click();
        cashierPage.validateUPILimitMsg(userErrorMsgForUPIIntent);
    }

    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PGP-50167")
    @Parameters({"theme"})
    @Test(description = "Verify Error MSG for UPI PPI Wallet Daily Transaction Limit Exhaust ")
    public void UPI_PPI_WALLET_Daily_Limit_Exhaust(@Optional("checkoutjs_wap_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.PPBL);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_PPI_WALLET;
        String orderID = CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("20.00")
                .setSsoToken(user.ssoToken())
                .setOrderId(orderID)
                .setChannelId("WEB")
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").setIsLiteEligible(true).build();

        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        String userErrorMsgForUPI=fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.subPayModeRemainingLimits.find {it.limitType == 'PPIWALLET'}.userErrorMessage");
        System.out.println("User error msg in fpo: "+fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.subPayModeRemainingLimits.find {it.limitType == 'PPIWALLET'}.userErrorMessage"));
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.subPayModeRemainingLimits.find {it.limitType == 'PPIWALLET'}.userErrorMessage")).isEqualTo("Merchant cannot accept Wallet on UPI at the moment. Try using other options.");

        MerchantConfig config = checkoutJsCheckoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutJsCheckoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabUPI().waitUntilClickable();

        cashierPage.tabUPI().click();
        cashierPage.validateUPIPPIWalletLimitMsg(userErrorMsgForUPI);

        cashierPage.tabUPICollect().click();
        cashierPage.validateColletPPIWalletLimitMsg(userErrorMsgForUPI);
        cashierPage.validateIntentPPIWalletLimitMsg(userErrorMsgForUPI);

    }

    @Owner(Constants.Owner.ABHISHEK_KULKARNI)
    @Feature("PGP-53502")
    @Parameters({"theme"})
    @Test(description = "Verify Eligibility ErrorMsg Of UPI CreditLine")
    public void UPI_CreditLine_NotAllowed(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.UPILITECC);
        Constants.MerchantType merchant = Constants.MerchantType.CREDITLINE_ELIGIBILITY;
        String orderID = CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("20.00")
                .setSsoToken(user.ssoToken())
                .setOrderId(orderID)
                .setChannelId("WEB")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").setIsLiteEligible(true).build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        String userErrorMsgForUPI=fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.subPayModeRemainingLimits.find {it.limitType == 'CREDIT_LINE'}.userErrorMessage");
        System.out.println("User error msg in fpo: "+fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.subPayModeRemainingLimits.find {it.limitType == 'CREDIT_LINE'}.userErrorMessage"));
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.subPayModeRemainingLimits.find {it.limitType == 'CREDIT_LINE'}.userErrorMessage")).isEqualTo(userErrorMsgForUPI);
        MerchantConfig config = checkoutJsCheckoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutJsCheckoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabUPI().waitUntilClickable();
        cashierPage.tabUPI().click();
        cashierPage.waitUntilLoads();
        cashierPage.validateUpiSubPayModeNotAllowed(userErrorMsgForUPI);
    }

    @Owner(Constants.Owner.ABHISHEK_KULKARNI)
    @Feature("PGP-53502")
    @Parameters({"theme"})
    @Test(description = "Verify Eligibility ErrorMsg Of UPI CreditLine And PPIWallet")
    public void UPI_CreditLineAndWallet_NotAllowed(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        String userErrorMsgWalletAndCreditLine = "This merchant is not accepting Wallet, Credit Line on UPI.";
        User user = userManager.getForRead(Label.UPILITECC);
        Constants.MerchantType merchant = Constants.MerchantType.CREDITLINE_WALLET_ELIGIBILITY;
        String orderID = CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("20.00")
                .setSsoToken(user.ssoToken())
                .setOrderId(orderID)
                .setChannelId("WEB")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").setIsLiteEligible(true).build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        String userErrorMsgForCreditLine=fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.subPayModeRemainingLimits.find {it.limitType == 'CREDIT_LINE'}.userErrorMessage");
        System.out.println("User error msg in fpo: "+fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.subPayModeRemainingLimits.find {it.limitType == 'CREDIT_LINE'}.userErrorMessage"));
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.subPayModeRemainingLimits.find {it.limitType == 'CREDIT_LINE'}.userErrorMessage")).isEqualTo(userErrorMsgForCreditLine);
        String userErrorMsgForPpiWallet=fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.subPayModeRemainingLimits.find {it.limitType == 'PPIWALLET'}.userErrorMessage");
        System.out.println("User error msg in fpo: "+fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.subPayModeRemainingLimits.find {it.limitType == 'PPIWALLET'}.userErrorMessage"));
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.subPayModeRemainingLimits.find {it.limitType == 'PPIWALLET'}.userErrorMessage")).isEqualTo(userErrorMsgForPpiWallet);
        MerchantConfig config = checkoutJsCheckoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutJsCheckoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabUPI().waitUntilClickable();
        cashierPage.tabUPI().click();
        cashierPage.waitUntilLoads();
        cashierPage.validateUpiSubPayModeNotAllowed(userErrorMsgWalletAndCreditLine);
    }

    @Owner(Constants.Owner.ABHISHEK_KULKARNI)
    @Feature("PGP-53502")
    @Parameters({"theme"})
    @Test(description = "Verify Eligibility ErrorMsg Of UPI CreditLine Which is Pick form locale")
    public void UPI_CreditLineLocale_NotAllowed(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        String userErrorMsgForUPI = "This merchant is not accepting Credit Line on UPI.";
        User user = userManager.getForRead(Label.UPILITECC);
        Constants.MerchantType merchant = Constants.MerchantType.CREDITLINE_ELIGIBILITY_LOCALE;
        String orderID = CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("20.00")
                .setSsoToken(user.ssoToken())
                .setOrderId(orderID)
                .setChannelId("WEB")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").setIsLiteEligible(true).build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.subPayModeRemainingLimits.find {it.limitType == 'CREDIT_LINE'}")).doesNotContain("userErrorMessage");
        MerchantConfig config = checkoutJsCheckoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutJsCheckoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabUPI().waitUntilClickable();
        cashierPage.tabUPI().click();
        cashierPage.waitUntilLoads();
        cashierPage.validateUpiSubPayModeNotAllowed(userErrorMsgForUPI);
    }

    @Owner(Constants.Owner.ABHISHEK_KULKARNI)
    @Feature("PGP-53502")
    @Parameters({"theme"})
    @Test(description = "Verify Error MSG for UPICreditLine Limit Exhaust")
    public void UPICreditLineLimitExhaust(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.UPILITECC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_CREDITLINE_LIMIT;
        String orderID = CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("20.00")
                .setSsoToken(user.ssoToken())
                .setOrderId(orderID)
                .setChannelId("WEB")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").setIsLiteEligible(true).build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        String userErrorMsgForUPI=fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.subPayModeRemainingLimits.find {it.limitType == 'CREDIT_LINE'}.userErrorMessage");
        System.out.println("User error msg in fpo: "+fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.subPayModeRemainingLimits.find {it.limitType == 'CREDIT_LINE'}.userErrorMessage"));
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.subPayModeRemainingLimits.find {it.limitType == 'CREDIT_LINE'}.userErrorMessage")).isEqualTo(userErrorMsgForUPI);
        MerchantConfig config = checkoutJsCheckoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutJsCheckoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabUPI().waitUntilClickable();
        cashierPage.tabUPI().click();
        cashierPage.waitUntilLoads();
        cashierPage.validateUPILimitMsg(userErrorMsgForUPI);
    }

    @Owner(Constants.Owner.ABHISHEK_KULKARNI)
    @Feature("PGP-53502")
    @Parameters({"theme"})
    @Test(description = "Verify Error MSG for UPICreditLine Limit And PPIWallet Limit Exhaust")
    public void UPICreditLineAndWalletLimitExhaust(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        String userErrorMsgWalletAndCreditLine = "Merchant cannot accept Wallet, Credit Line on UPI at the moment. Try using other options.";
        User user = userManager.getForRead(Label.UPILITECC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_CREDITLINE_WALLET_LIMIT;
        String orderID = CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("20.00")
                .setSsoToken(user.ssoToken())
                .setOrderId(orderID)
                .setChannelId("WEB")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").setIsLiteEligible(true).build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        String userErrorMsgForCreditLine=fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.subPayModeRemainingLimits.find {it.limitType == 'CREDIT_LINE'}.userErrorMessage");
        System.out.println("User error msg in fpo: "+fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.subPayModeRemainingLimits.find {it.limitType == 'CREDIT_LINE'}.userErrorMessage"));
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.subPayModeRemainingLimits.find {it.limitType == 'CREDIT_LINE'}.userErrorMessage")).isEqualTo(userErrorMsgForCreditLine);
        String userErrorMsgForPpiWallet=fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.subPayModeRemainingLimits.find {it.limitType == 'PPIWALLET'}.userErrorMessage");
        System.out.println("User error msg in fpo: "+fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.subPayModeRemainingLimits.find {it.limitType == 'PPIWALLET'}.userErrorMessage"));
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.subPayModeRemainingLimits.find {it.limitType == 'PPIWALLET'}.userErrorMessage")).isEqualTo(userErrorMsgForPpiWallet);
        MerchantConfig config = checkoutJsCheckoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutJsCheckoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabUPI().waitUntilClickable();
        cashierPage.tabUPI().click();
        cashierPage.waitUntilLoads();
        cashierPage.validateUPILimitMsg(userErrorMsgWalletAndCreditLine);
    }

    @Owner(Constants.Owner.ABHISHEK_KULKARNI)
    @Feature("PGP-53502")
    @Parameters({"theme"})
    @Test(description = "Verify Error MSG for UPICreditLine Limit Exhaust Pick From Locale")
    public void UPICreditLineLimitExhaustFromLocale(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        String userErrorMsgForUPI = "Merchant cannot accept Credit Line on UPI at the moment. Try using other options.";
        User user = userManager.getForRead(Label.UPILITECC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_CREDITLINE_LIMIT_LOCALE;
        String orderID = CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("20.00")
                .setSsoToken(user.ssoToken())
                .setOrderId(orderID)
                .setChannelId("WEB")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").setIsLiteEligible(true).build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.subPayModeRemainingLimits.find {it.limitType == 'CREDIT_LINE'}")).doesNotContain("userErrorMessage");
        MerchantConfig config = checkoutJsCheckoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutJsCheckoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabUPI().waitUntilClickable();
        cashierPage.tabUPI().click();
        cashierPage.waitUntilLoads();
        cashierPage.validateUPILimitMsg(userErrorMsgForUPI);
    }
}
