package scripts.subscription.subscriptionRenewal.enhancedFlows;

import com.paytm.api.PreNotify;
import com.paytm.api.TxnStatus;
import com.paytm.api.nativeAPI.RenewSubscription;
import com.paytm.appconstants.Constants;
import com.paytm.appconstants.Constants.MerchantType;
import com.paytm.apphelpers.*;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.RenewSubscription.RenewSubscriptionDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.framework.utils.CommonUtils;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
import com.paytm.utils.merchant.util.exception.authException.AuthException;
import com.paytm.utils.merchant.util.exception.walletException.WalletException;
import io.qameta.allure.Issue;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.time.LocalDateTime;

import static com.paytm.appconstants.Constants.Owner.Amanpreet;

/**
 * Created by sureshgupta on 26/09/17.
 */
@Owner("Tarun")
public class SubscriptionRenewalViaWalletOnly extends PGPBaseTest {

    private final CheckoutPage checkoutPage = new CheckoutPage();
    private final String successMessage = "Subscription Txn accepted.";

    private String executeRenewalAndFetchOrderId(MerchantType merchantType, String subsId, String txnAmount,String message)
    {
        RenewSubscriptionDTO renewSubscriptionDTO = new RenewSubscriptionDTO.Builder
                (merchantType.getId(), subsId, txnAmount)
                .setMerchantKey(merchantType.getKey())
                .build();
        RenewSubscription renewSubscription = new RenewSubscription(renewSubscriptionDTO);
        JsonPath jsonPath = renewSubscription.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg"))
                .as("Result Message mismatch").isEqualTo(message);
        return renewSubscriptionDTO.getBody().getOrderId();
    }

    @Parameters({"theme"})
    @Test(description = "To verify failure when merchant initiate the transaction after the end of subscription contract.")
    public void PGP_219_RenewalRequestAfterSubscriptionContractExpired(@Optional("enhancedweb") String theme) throws Exception {
    /*Subs*/
        MerchantType merchantType = MerchantType.Subscription_Pg2_MID1;
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.SubscriptionWalletOnly(merchantType, theme)
                .build();
        String subsId = subscribe(orderDTO, theme, user);
    /*Modifying Subscription Start to one month and one day back and End Date to one day back from now*/
        LocalDateTime newSubsEndDate = LocalDateTime.now().minusDays(1);
        PGPHelpers.modifySubscriptionEndDate(Long.valueOf(subsId), newSubsEndDate);
        LocalDateTime newSubsStartDate = LocalDateTime.now().minusMonths(1).minusDays(1);
        PGPHelpers.modifySubscriptionStartDate(Long.valueOf(subsId), newSubsStartDate);
    /*Modify Wallet Balance*/
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) + 1.0);
    /*Renew Subs*/
       executeRenewalAndFetchOrderId(merchantType,subsId,orderDTO.getTXN_AMOUNT(),"The subscription has already expired");
    }

    //To be discussed with Pulkit 217,215 are same TC
    @Parameters({"theme"})
    @Test(description = "merchant initiate the renewal request within grace period and retry count exhausted")
    public void PGP_217_RetryRenewalRequestWithinGracePeriodWhenRetryCountGotExhausted(@Optional("enhancedweb") String theme) throws Exception {
        /*Subs*/
        MerchantType merchantType = MerchantType.SUBSCRIPTION_WALLET_ONLY;
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.SubscriptionWalletOnly(merchantType, theme)
                .setSUBS_RETRY_COUNT("1")
                .build();
        String subsId = subscribe(orderDTO, theme,user);
        /*Modify Wallet Balance*/
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.0);
        /*Modifying start date to one month back*/
        PGPHelpers.modifySubscriptionStartDate(Long.valueOf(subsId), LocalDateTime.now().minusMonths(1));
        /*Renew Subs*/
        String orderId = executeRenewalAndFetchOrderId(merchantType,subsId,orderDTO.getTXN_AMOUNT(),successMessage);
        Assertions.assertThat(PGPHelpers.executeToFetchStatusInPaymentDetails(subsId,orderId)).isEqualTo("FAIL");
        /*Renew Subs*/
        executeRenewalAndFetchOrderId(merchantType,subsId,orderDTO.getTXN_AMOUNT(),"Subscription Renewal Rejected.");
        /*Modify Wallet Balance*/
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) + 1.00);
        /*Renew Subs retry count exhausted*/
       executeRenewalAndFetchOrderId(merchantType,subsId,orderDTO.getTXN_AMOUNT(),"Subscription Renewal Rejected.");
    }

    @Parameters({"theme"})
    @Test(description = "merchant initiate the renewal req within grace period, retry count>0, prior renewal req pending")
    public void PGP_215_RetryRenewalRequestWhenRetryCountIsGreaterThanZero(@Optional("enhancedweb") String theme) throws Exception {
        /*Subs*/
        MerchantType merchantType = MerchantType.SUBSCRIPTION_WALLET_ONLY;
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.SubscriptionWalletOnly(merchantType, theme)
                .setSUBS_RETRY_COUNT("1")
                .build();
        String subsId = subscribe(orderDTO, theme,user);
        /*Modify Wallet Balance*/
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.0);
        /*Renew Subs*/
        String orderId = executeRenewalAndFetchOrderId(merchantType,subsId,orderDTO.getTXN_AMOUNT(),successMessage);
        Assertions.assertThat(PGPHelpers.executeToFetchStatusInPaymentDetails(subsId,orderId)).isEqualTo("FAIL");
        /*Modify Wallet Balance*/
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) + 1.00);
        /*Renew Subs retry*/
        executeRenewalAndFetchOrderId(merchantType,subsId,orderDTO.getTXN_AMOUNT(),"Retry count for payment request in this cycle has been exhausted");

    }
    // what should be the response
    @Issue("PGP-16625")
    @Parameters({"theme"})
    @Test(description = "To verify renewal if merchant initiate the request after the grace period")
    public void PGP_213_RenewalRequestAfterGracePeriod(@Optional("enhancedweb") String theme) throws Exception {
        /*Subs*/
        MerchantType merchantType = MerchantType.Subscription_Pg2_MID1;
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.SubscriptionWalletOnly(merchantType, theme)
                .build();
        String subsId = subscribe(orderDTO, theme,user);
        /*Modifying Subscription Start Date*/
        LocalDateTime newDate = LocalDateTime.now().minusMonths(1).minusDays(Long.valueOf(orderDTO.getSUBS_GRACE_DAYS()) + 2);
        PGPHelpers.modifySubscriptionStartDate(Long.valueOf(subsId), newDate);
        /*Modify Wallet Balance*/
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) + 1.00);
        /*Renew Subs*/
       executeRenewalAndFetchOrderId(merchantType,subsId,orderDTO.getTXN_AMOUNT(), "Prenotify not found for subscriptionId of this transaction date and amount");
    }

    @Parameters({"theme"})
    @Test(description = "To verify renewal if merchant initiated the request within the grace period")
    public void PGP_211_RenewalRequestWithinGracePeriod(@Optional("enhancedweb") String theme) throws Exception {
        /*Subs*/
        MerchantType merchantType = MerchantType.SUBSCRIPTION_WALLET_ONLY;
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.SubscriptionWalletOnly(merchantType, theme)
                .build();
        String subsId = subscribe(orderDTO, theme,user);
        Assertions.assertThat(PGPHelpers.executeToFetchStatusInPaymentDetails(subsId,orderDTO.getORDER_ID())).isEqualTo("ACTIVE");
        /*Modifying Subscription Date to one month back from now*/
        PGPHelpers.modifySubscriptionStartDate(Long.valueOf(subsId), LocalDateTime.now().minusMonths(1).minusDays(2));
        /*Modify Wallet Balance*/
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) + 1);
        /*Renew Subs*/
        String orderId = executeRenewalAndFetchOrderId(merchantType,subsId,orderDTO.getTXN_AMOUNT(),successMessage);
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, orderId)).isNotNull().isNotEmpty();
        Assertions.assertThat(PGPHelpers.executeToFetchStatusInPaymentDetails(subsId,orderId)).isEqualTo("ACTIVE");
    }

    @Parameters({"theme"})
    @Test(description = "To verify succesful renewal for Subs First Request via Wallet only having sufficient wallet balance")
    public void PGP_198_RenewalRequestWhenSufficientWalletBalance(@Optional("enhancedweb") String theme) throws Exception {
        /*Subs*/
        MerchantType merchantType = MerchantType.SUBSCRIPTION_WALLET_ONLY;
        User user = userManager.getForWrite(Label.LOGIN,Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.SubscriptionWalletOnly(merchantType, theme)
                .build();
        String subsId = subscribe(orderDTO, theme,user);
        /*Modify Wallet Balance*/
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) + 1.00);
        /*Renew Subs*/
        String orderId = executeRenewalAndFetchOrderId(merchantType,subsId,orderDTO.getTXN_AMOUNT(),successMessage);
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, orderId)).isNotNull().isNotEmpty();
        Assertions.assertThat(PGPHelpers.executeToFetchStatusInPaymentDetails(subsId,orderId)).isEqualTo("ACTIVE");
    }

    @Parameters({"theme"})
    @Test(description = "To verify unsuccessful renewal for Subs First Request via wallet only having insufficient wallet balance")
    public void PGP_199_RenewalRequestWhenInsufficientWalletBalance(@Optional("enhancedweb") String theme) throws Exception {
        /*Subs*/
        MerchantType merchantType = MerchantType.SUBSCRIPTION_WALLET_ONLY;
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.SubscriptionWalletOnly(merchantType, theme)
                .build();
        String subsId = subscribe(orderDTO, theme,user);

        /*Modify Wallet Balance*/
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.0);
        /*Renew Subs*/
        String orderId =  executeRenewalAndFetchOrderId(merchantType,subsId,orderDTO.getTXN_AMOUNT(),successMessage);
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId,orderId)).isNull();//TODO
        Assertions.assertThat(PGPHelpers.executeToFetchStatusInPaymentDetails(subsId,orderId)).isEqualTo("FAIL");
    }

    @Parameters({"theme"})
    @Test(description = "Renewal success and merchant initiated the same renewal request within the grace period")
    public void PGP_209_multipleRenewalRequestWithinGracePeriod(@Optional("enhancedweb") String theme) throws Exception {
        /*Subs*/
        MerchantType merchantType = MerchantType.SUBSCRIPTION_WALLET_ONLY;
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.SubscriptionWalletOnly(merchantType, theme)
                .build();
        String subsId = subscribe(orderDTO, theme,user);
        /*Modify Wallet Balance*/
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) + 1.00);
        /*First Renew Subs*/
        String orderId = executeRenewalAndFetchOrderId(merchantType,subsId,orderDTO.getTXN_AMOUNT(),successMessage);
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, orderId)).isNotNull().isNotEmpty();
        Assertions.assertThat(PGPHelpers.executeToFetchStatusInPaymentDetails(subsId,orderId)).isEqualTo("ACTIVE");
         /*Modify Wallet Balance*/
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) + 1.00);
        /*Second Renew Subs Request*/
        executeRenewalAndFetchOrderId(merchantType,subsId,orderDTO.getTXN_AMOUNT(),"Prenotify not found for subscriptionId of this transaction date and amount");

    }

    @Owner(Amanpreet)
    @Parameters({"theme"})
    @Test(description = "Send Prenotify with grace days=t+0")
    public void gd_00(@Optional("enhancedweb_revamp") String theme) throws Exception {
        MerchantType merchantType = MerchantType.SUBSCRIPTION_PG2_LATEST_ALL;
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.SubscriptionWalletOnly(merchantType, theme)
                .setSUBS_GRACE_DAYS("2")
                .build();
        String subsId = subscribe(orderDTO, theme,user);
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) + 1.00);
        LocalDateTime PreviousDate = LocalDateTime.now().minusMonths(1);
       modifySubsDatesInDB(subsId, PreviousDate);

        /*Prenotify Subs*/
        String date = CommonHelpers.addDays(CommonUtils.getdate("dd-MM-yyyy"), "dd-MM-yyyy", 0);
        PreNotify preNotify = new PreNotify(merchantType, orderDTO.getTXN_AMOUNT(), subsId,date);
        Response response = preNotify.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.status"))
                .isEqualTo("FAILURE");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.message")).isEqualTo("notification can not be send at given txnDate");
    }

    @Owner(Amanpreet)
    @Parameters({"theme"})
    @Test(description = "Send Prenotify with grace days=t+1")
    public void gd_01(@Optional("enhancedweb_revamp") String theme) throws Exception {
        MerchantType merchantType = MerchantType.SUBSCRIPTION_PG2_LATEST_ALL;
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.SubscriptionWalletOnly(merchantType, theme)
                .setSUBS_GRACE_DAYS("2")
                .build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) + 1.00);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.buttonWalletPayNow().click();
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateStatus("TXN_SUCCESS");
        String subsId = txnStatus.getResponse().getSUBS_ID();

        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) + 1.00);
        LocalDateTime PreviousDate = LocalDateTime.now().minusMonths(1);
        modifySubsDatesInDB(subsId, PreviousDate);

        /*Prenotify Subs*/
        String date = CommonHelpers.addDays(CommonUtils.getdate("dd-MM-yyyy"), "dd-MM-yyyy", 1);
        PreNotify preNotify = new PreNotify(merchantType, orderDTO.getTXN_AMOUNT(), subsId,date);
        Response response = preNotify.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.status")).isEqualTo("SUCCESS");
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.subscription,orderDTO.getORDER_ID(),"PRE_PAYMENT_REMINDER");
        Assertions.assertThat(logs).contains("\"subDueDate\":\""+CommonHelpers.addDays(CommonUtils.getdate("yyyy-MM-dd"), "yyyy-MM-dd", 1)+" 00:00:00\"");
    }

    @Owner(Amanpreet)
    @Parameters({"theme"})
    @Test(description = "Send Prenotify with grace days=t+2")
    public void gd_02(@Optional("enhancedweb_revamp") String theme) throws Exception {
        /*Subs*/
        MerchantType merchantType = MerchantType.SUBSCRIPTION_PG2_LATEST_ALL;
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.SubscriptionWalletOnly(merchantType, theme)
                .setSUBS_GRACE_DAYS("2")
                .build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) + 1.00);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.buttonWalletPayNow().click();
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateStatus("TXN_SUCCESS");
        String subsId = txnStatus.getResponse().getSUBS_ID();

        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) + 1.00);
        LocalDateTime PreviousDate = LocalDateTime.now().minusMonths(1);
        modifySubsDatesInDB(subsId, PreviousDate);

        /*Prenotify Subs*/
        String date = CommonHelpers.addDays(CommonUtils.getdate("dd-MM-yyyy"), "dd-MM-yyyy", 2);
        PreNotify preNotify = new PreNotify(merchantType, orderDTO.getTXN_AMOUNT(), subsId,date);
        Response response = preNotify.execute();

        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.status"))
                .isEqualTo("SUCCESS");
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.subscription,orderDTO.getORDER_ID(),"PRE_PAYMENT_REMINDER");
        Assertions.assertThat(logs).contains("\"subDueDate\":\""+CommonHelpers.addDays(CommonUtils.getdate("yyyy-MM-dd"), "yyyy-MM-dd", 2)+" 00:00:00\"");

    }

    @Owner(Amanpreet)
    @Parameters({"theme"})
    @Test(description = "Send Prenotify with grace days=t+3")
    public void gd_03(@Optional("enhancedweb_revamp") String theme) throws Exception {
        /*Subs*/
        MerchantType merchantType = MerchantType.SUBSCRIPTION_PG2_LATEST_ALL;
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.SubscriptionWalletOnly(merchantType, theme)
                .setSUBS_GRACE_DAYS("2")
                .build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) + 1.00);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.buttonWalletPayNow().click();
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateStatus("TXN_SUCCESS");
        String subsId = txnStatus.getResponse().getSUBS_ID();

        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) + 1.00);
        LocalDateTime PreviousDate = LocalDateTime.now().minusMonths(1);
        modifySubsDatesInDB(subsId, PreviousDate);

        /*Prenotify Subs*/
        String date = CommonHelpers.addDays(CommonUtils.getdate("dd-MM-yyyy"), "dd-MM-yyyy", 3);
        PreNotify preNotify = new PreNotify(merchantType, orderDTO.getTXN_AMOUNT(), subsId,date);
        Response response = preNotify.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.status"))
                .isEqualTo("FAILURE");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.message")).isEqualTo("Payment Request date cannot be for future frequency cycles");

    }

    private String subscribe(OrderDTO orderDTO, String theme, User user) throws AuthException, WalletException {
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) + 1.00);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.login(user);
        cashierPage.waitUntilLoads();
        cashierPage.buttonWalletPayNow().click();
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("WALLET")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName("WALLET")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .validateRefundAmnt("0.00")
                .validateSubsid(Constants.ValidationType.NON_EMPTY);
         String subsId = txnStatus.getResponse().getSUBS_ID();
         Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, orderDTO.getORDER_ID())).isNotNull().isNotEmpty();
         Assertions.assertThat(PGPHelpers.getSavedCardId(subsId)).isNull();
        return subsId;
    }
    public static void modifySubsDatesInDB(String subsId, LocalDateTime FreqDate) {

        PGPHelpers.modifySubscriptionStartDate(Long.valueOf(subsId), FreqDate);
        PGPHelpers.modifySubscriptionPaymentCreateDate(Long.valueOf(subsId), FreqDate);
        PGPHelpers.modifySubscriptionPaymentUpdateDate(Long.valueOf(subsId), FreqDate);
        PGPHelpers.modifySubscriptionUpidetailCreateTime(Long.valueOf(subsId), FreqDate);
        PGPHelpers.modifySubscriptionUpidetailUpdateTime(Long.valueOf(subsId), FreqDate);
    }

}
