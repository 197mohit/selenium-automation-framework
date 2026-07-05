package scripts.UPISubsMandate;

import com.paytm.ServerConfigProvider;
import com.paytm.api.PreNotify;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.checkoutjs.MerchantConfig;
import com.paytm.pages.*;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.testng.Assert;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import scripts.Native.checkoutjs.CheckoutJsBase;

import java.time.LocalDateTime;
import java.util.Date;

import static com.paytm.LocalConfig.THEIA_FACADE_LOGS;
import static com.paytm.appconstants.Constants.Owner.AKSHAT;
import static com.paytm.appconstants.Constants.Owner.GAURAV;
import static com.paytm.apphelpers.LogsValidationHelper.getLogsOnServer;

public class CheckoutJsUPICollectSubs extends CheckoutJsBase {

    private final CheckoutJsCheckoutPage checkoutPage = new CheckoutJsCheckoutPage();

    @Owner(GAURAV)
    @Feature("PGP-36177")
    @Parameters({"theme"})
    @Test(description = "Verify Subs transaction when txn amt >5k and max amt =25k")
    public void verifySubsGreaterThan5k(@Optional("checkoutjs_web_revamp") String theme) throws Exception {

        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_SUBS;

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)       //Initiate subs request
                .setTxnValue("6000")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("25000")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();

        InitTxnResponseDTO initTxnResponseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabUPI().assertVisible();
        cashierPage.payBy(Constants.PayMode.UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS")
                .validatePaymentMode("PPBLC")
                .validateSubsId(Constants.ValidationType.NON_EMPTY);

        String subsId = responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.SUBS_ID);
        LocalDateTime PreviousDate = LocalDateTime.now().minusMonths(1).plusDays(2);
        PGPHelpers.modifySubscriptionDueDate(Long.valueOf(subsId), LocalDateTime.now().plusDays(2));
        UPIRenewSubs.modifySubsDatesInDB(subsId, PreviousDate);

        PreNotify preNotify = new PreNotify(merchant, "2000", subsId);
        Response response = preNotify.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.status"))
                .isEqualTo("SUCCESS").as("PreNotify is Not successful for Given Dates");

        String paytmRefId = response.jsonPath().getString("body.paytmReferenceId");
        UPIRenewSubs.modifyNotifyDatesInDB(paytmRefId);
        LocalDateTime frequencyReqDate = LocalDateTime.now().minusMonths(1);
        PGPHelpers.modifySubscriptionDueDate(Long.valueOf(subsId), LocalDateTime.now());
        UPIRenewSubs.modifySubsDatesInDB(subsId, frequencyReqDate);

        String orderId = UPIRenewSubs.executeRenewalAndFetchOrderId(merchant, subsId, "2000", "");
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, orderId)).isNotNull();
        Assertions.assertThat(PGPHelpers.executeToFetchStatusInPaymentDetails(subsId, orderId)).isEqualTo("ACTIVE");
    }

    @Owner(GAURAV)
    @Feature("PGP-36177")
    @Parameters({"theme"})
    @Test(description = "Verify Subs transaction when txn amt <5k and max amt =25k")
    public void verifySubsLessThan5k(@Optional("checkoutjs_web_revamp") String theme) throws Exception {

        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_SUBS;

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)       //Initiate subs request
                .setTxnValue("1000")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("25000")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();

        InitTxnResponseDTO initTxnResponseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabUPI().assertVisible();
        cashierPage.payBy(Constants.PayMode.UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS")
                .validatePaymentMode("PPBLC")
                .validateSubsId(Constants.ValidationType.NON_EMPTY);

        String subsId = responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.SUBS_ID);
        LocalDateTime PreviousDate = LocalDateTime.now().minusMonths(1).plusDays(2);
        PGPHelpers.modifySubscriptionDueDate(Long.valueOf(subsId), LocalDateTime.now().plusDays(2));
        UPIRenewSubs.modifySubsDatesInDB(subsId, PreviousDate);

        PreNotify preNotify = new PreNotify(merchant, "22000", subsId);
        Response response = preNotify.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.status"))
                .isEqualTo("SUCCESS").as("PreNotify is Not successful for Given Dates");

        String paytmRefId = response.jsonPath().getString("body.paytmReferenceId");
        UPIRenewSubs.modifyNotifyDatesInDB(paytmRefId);
        LocalDateTime frequencyReqDate = LocalDateTime.now().minusMonths(1);
        PGPHelpers.modifySubscriptionDueDate(Long.valueOf(subsId), LocalDateTime.now());
        UPIRenewSubs.modifySubsDatesInDB(subsId, frequencyReqDate);

        String orderId = UPIRenewSubs.executeRenewalAndFetchOrderId(merchant, subsId, "22000", "");
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, orderId)).isNotNull();
        Assertions.assertThat(PGPHelpers.executeToFetchStatusInPaymentDetails(subsId, orderId)).isEqualTo("ACTIVE");
    }

    @Owner(GAURAV)
    @Feature("PGP-36177")
    @Parameters({"theme"})
    @Test(description = "Verify Subs transaction when txn amt >25k and max amt =25k")
    public void verifySubsGreaterThan25k(@Optional("checkoutjs_web_revamp") String theme) throws Exception {

        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_SUBS;

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)       //Initiate subs request
                .setTxnValue("26000")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("25000")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();

        InitTxnResponseDTO initTxnResponseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        String resultInfo = initTxnResponseDTO.getBody().getResultInfo().getResultMsg();
        Assertions.assertThat(resultInfo).isEqualTo("Paymode selected is not applicable when txn amount is greater than the renewal amount");
    }

    @Owner(GAURAV)
    @Feature("PGP-36177")
    @Parameters({"theme"})
    @Test(description = "Verify Subs transaction when txn amt =25k and max amt =25k")
    public void verifySubsFixTo25k(@Optional("checkoutjs_web_revamp") String theme) throws Exception {

        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_SUBS;

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)       //Initiate subs request
                .setTxnValue("25000")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("25000")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();

        InitTxnResponseDTO initTxnResponseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabUPI().assertVisible();
        cashierPage.payBy(Constants.PayMode.UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS")
                .validatePaymentMode("PPBLC")
                .validateSubsId(Constants.ValidationType.NON_EMPTY);

        String subsId = responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.SUBS_ID);
        LocalDateTime PreviousDate = LocalDateTime.now().minusMonths(1).plusDays(2);
        PGPHelpers.modifySubscriptionDueDate(Long.valueOf(subsId), LocalDateTime.now().plusDays(2));
        UPIRenewSubs.modifySubsDatesInDB(subsId, PreviousDate);

        PreNotify preNotify = new PreNotify(merchant, "25000", subsId);
        Response response = preNotify.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.status"))
                .isEqualTo("SUCCESS").as("PreNotify is Not successful for Given Dates");

        String paytmRefId = response.jsonPath().getString("body.paytmReferenceId");
        UPIRenewSubs.modifyNotifyDatesInDB(paytmRefId);
        LocalDateTime frequencyReqDate = LocalDateTime.now().minusMonths(1);
        PGPHelpers.modifySubscriptionDueDate(Long.valueOf(subsId), LocalDateTime.now());
        UPIRenewSubs.modifySubsDatesInDB(subsId, frequencyReqDate);

        String orderId = UPIRenewSubs.executeRenewalAndFetchOrderId(merchant, subsId, initTxnDTO.txnAmountFromBody(), "");
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, orderId)).isNotNull();
        Assertions.assertThat(PGPHelpers.executeToFetchStatusInPaymentDetails(subsId, orderId)).isEqualTo("ACTIVE");
    }


    @Parameters("theme")
    @Feature("PGP-41399")
    @Owner("Himanshu Arora")
    @Test(description = "validate correct Upi Vpa case in case of checkoutjs flow with respcode 0 in theia facade logs.")
    public void UpiVpaCases_03(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.SUBSCRIPTION_UPI)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.tabUPI().click();
        cashierPage.waitUntilLoads();
        cashierPage.textBoxVPA().sendKeys("8512005349@paytm");
//        cashierPage.verifyVPALinkText().click();
        cashierPage.buttonPGPayNow().click();
        String grepcmd = "grep \"" + "\" "+THEIA_FACADE_LOGS +" | grep \"UPI_SECURE\"  ";
        String theiaFaacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assertions.assertThat(theiaFaacadeLogs).contains("\"status\":\"SUCCESS\"");
        Assertions.assertThat(theiaFaacadeLogs).contains("\"respCode\":\"0\"");
    }
    @Parameters("theme")
    @Feature("PGP-41399")
    @Owner("Himanshu Arora")
    @Test(description = "validate invalid Upi Vpa case in case of checkoutjs flow with respcode 37 in theia facade logs.")
    public void UpiVpaCases_04(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.PG2_UPI)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.tabUPI().click();
        cashierPage.waitUntilLoads();
        cashierPage.textBoxVPA().sendKeys("85120053497@paytm");
//        cashierPage.verifyVPALinkText().click();
        cashierPage.buttonPGPayNow().click();
        String grepcmd = "grep \"" + "\" "+THEIA_FACADE_LOGS +" | grep \"UPI_SECURE\"  ";
        String theiaFaacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assertions.assertThat(theiaFaacadeLogs).contains("\"status\":\"FAILURE\"");
        Assertions.assertThat(theiaFaacadeLogs).contains("\"respCode\":\"37\"");
        Assert.assertTrue(cashierPage.invalidVpaText().isElementPresent());
    }



    @Parameters("theme")
    @Feature("PGP-41455")
    @Owner("Himanshu Arora")
    @Test(description = "validate only Upi Vpa is enabled on cashier page when DISABLE_UPI_COLLECT_NUMERIC_ID is true on MID in case of checkoutjs flow.")
    public void UpiVpaCases_06(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.QR_LOGIN_PREFERENCE_N)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.tabUPI().click();
        cashierPage.waitUntilLoads();
        cashierPage.textBoxVPA().sendKeys("8512005349@paytm");
//        cashierPage.verifyVPALinkText().click();
        cashierPage.buttonPGPayNow().click();
        Assert.assertFalse(cashierPage.verifyUpiNumericID().isElementPresent());
    }

    @Parameters("theme")
    @Feature("PGP-42069")
    @Owner("Himanshu Arora")
    @Test(description = "validate correct Upi numericid case in case of checkoutjs flow with respcode 0 in theia facade logs.")
    public void UpiNumericId_03(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.PG2_UPI)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.tabUPI().click();
        cashierPage.waitUntilLoads();
        cashierPage.UpiNumericId().sendKeys("8006006993");
        cashierPage.verifyUpiNumericID().click();
        String grepcmd = "grep \"" + "\" "+THEIA_FACADE_LOGS +" | grep \"UPI_SECURE\"  ";
        String theiaFaacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assertions.assertThat(theiaFaacadeLogs).contains("\"status\":\"SUCCESS\"");
        Assertions.assertThat(theiaFaacadeLogs).contains("\"respCode\":\"0\"");
    }
    @Parameters("theme")
    @Feature("PGP-42069")
    @Owner("Himanshu Arora")
    @Test(description = "validate incorrect Upi numericid case in case of checkoutjs flow with respcode INT-1766 in theia facade logs.")
    public void UpiNumericId_04(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.PG2_UPI)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.tabUPI().click();
        cashierPage.waitUntilLoads();
        cashierPage.UpiNumericId().sendKeys("8512005349");
        cashierPage.verifyUpiNumericID().click();
        String grepcmd = "grep \"" + "\" "+THEIA_FACADE_LOGS +" | grep \"UPI_SECURE\"  ";
        String theiaFaacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assertions.assertThat(theiaFaacadeLogs).contains("\"status\":\"FAILURE\"");
        Assertions.assertThat(theiaFaacadeLogs).contains("\"respCode\":\"INT-1766\"");
    }

    @Parameters("theme")
    @Feature("PGP-41075")
    @Owner("Abhishek Gupta")
    @Test(description = "validate correct Upi numericid case in case of checkoutjs flow with respcode 0 and validate payerCmid in ACQUIRING_PAY_ORDER api in theia facade logs.")
    public void UpiNumericIdCmid_01(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.PG2_UPI)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.tabUPI().click();
        cashierPage.waitUntilLoads();
        cashierPage.UpiNumericId().sendKeys("8006006993");
        cashierPage.verifyUpiNumericID().click();
        cashierPage.payButton().click();
        String grepcmd = "grep \"" + "\" "+THEIA_FACADE_LOGS +" | grep \"UPI_SECURE\"  ";
        String theiaFaacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assertions.assertThat(theiaFaacadeLogs).contains("\"status\":\"SUCCESS\"");
        Assertions.assertThat(theiaFaacadeLogs).contains("\"respCode\":\"0\"");
        Assertions.assertThat(theiaFaacadeLogs).contains("\"cmId\":\"8006006993\"");
        Assertions.assertThat(theiaFaacadeLogs).contains("\"vpa\":\"srivastavaprateek@paytm\"");
        String grepcmd1 = "grep \"" + "\" " + THEIA_FACADE_LOGS +
                " | grep \"" + Constants.MerchantType.PG2_UPI.getId() + "\" | grep \"ACQUIRING_PAY_ORDER\" ";
        String theiaFacade = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd1);
        PGPHelpers pgpHelpers= new PGPHelpers();
        String passThroughExtendInfo=pgpHelpers.getPassThroughExtendedInfo(theiaFacade);
        String decrpted=PGPHelpers.Base64Decode(passThroughExtendInfo);
        String getPayerCmid=pgpHelpers.getPayerCmid(decrpted);
        Assert.assertEquals(getPayerCmid,"8006006993");
    }

    @Parameters("theme")
    @Feature("PGP-41075")
    @Owner("Abhishek Gupta")
    @Test(description = "validate correct Upi numericid case in case of checkoutjs flow with result failure and cmid not coming in theia facade logs.")
    public void UpiNumericIdCmid_02(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.PG2_UPI)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.tabUPI().click();
        cashierPage.waitUntilLoads();
        cashierPage.UpiNumericId().sendKeys("8006006994");
        cashierPage.verifyUpiNumericID().click();
        cashierPage.pause(3);
        String grepcmd = "grep \"" + "\" /paytm/logs/theia_facade.log  | " +"grep \"UPI_SECURE\"  ";
        String theiaFaacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assertions.assertThat(theiaFaacadeLogs).contains("\"status\":\"FAILURE\"");
        Assertions.assertThat(theiaFaacadeLogs).contains("\"respCode\":\"INT-1766\"");
        Assertions.assertThat(theiaFaacadeLogs).contains("\"respMessage\":\"This UPI Number does not exist.\"");
    }

    @Owner(AKSHAT)
    @Feature("PG-5982")
    @Parameters({"theme"})
    @Test(description = "Verify that retry is enabled on subscription transactions")
    public void verifyRetryEnabled_onSubscriptionTransaction(@Optional("checkoutjs_web_revamp") String theme) throws Exception {

        // FF4j : theia.enableRetryForNativeSubscription  //

        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.Subs_PCF_fix;

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)       //Initiate subs request
                .setTxnValue("17")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("250")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();

        InitTxnResponseDTO initTxnResponseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabUPI().assertVisible();
        cashierPage.payBy(Constants.PayMode.UPI);
        cashierPage.waitUntilLoads();
        cashierPage.ErrorRetryButton().assertVisible();

    }

    @Owner(AKSHAT)
    @Feature("PG-5982")
    @Parameters({"theme"})
    @Test(description = "Verify that retry is successfully attempted (either pass/fail)")
    public void verifyRetry_isSuccessfullyAttempted(@Optional("checkoutjs_web_revamp") String theme) throws Exception {

        // FF4j : theia.enableRetryForNativeSubscription  //

        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.Subs_PCF_fix;

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)       //Initiate subs request
                .setTxnValue("17")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("250")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();

        InitTxnResponseDTO initTxnResponseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabUPI().assertVisible();
        cashierPage.payBy(Constants.PayMode.UPI);
        cashierPage.waitUntilLoads();
        cashierPage.ErrorRetryButton().assertVisible();

        cashierPage.ErrorRetryButton().click();
        cashierPage.payBy(Constants.PayMode.UPI);
        cashierPage.waitUntilLoads();
        cashierPage.ErrorRetryButton().assertVisible();

    }


    @Owner(AKSHAT)
    @Feature("PG-5982")
    @Parameters({"theme"})
    @Test(description = "Verify that for failure transaction retry is not attempted when retry=disabled")
    public void verifyRetryDisabled_forSubscriptionTransaction(@Optional("checkoutjs_web_revamp") String theme) throws Exception {

        // FF4j : theia.enableRetryForNativeSubscription  //

        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.Subs_PCF_fix;

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)       //Initiate subs request
                .setTxnValue("17")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("250")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();

        InitTxnResponseDTO initTxnResponseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabUPI().assertVisible();
        cashierPage.payBy(Constants.PayMode.UPI);

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.orderFromBody())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Your payment has been declined by your bank. Please contact your bank for any queries. If money has been deducted from your account, your bank will inform us within 48 hrs and we will refund the same")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("PPBLC")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .validateCheckSum(merchant.getKey())
                .assertAll();

    }

}
