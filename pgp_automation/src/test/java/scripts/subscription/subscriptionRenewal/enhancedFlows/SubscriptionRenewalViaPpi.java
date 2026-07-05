package scripts.subscription.subscriptionRenewal.enhancedFlows;

import com.paytm.api.TxnStatus;
import com.paytm.api.nativeAPI.RenewSubscription;
import com.paytm.appconstants.Constants;
import com.paytm.appconstants.Constants.MerchantType;
import com.paytm.appconstants.Constants.PayMode;
import com.paytm.apphelpers.*;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.RenewSubscription.RenewSubscriptionDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.framework.reporting.Reporter;
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
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * Created by sureshgupta on 26/09/17.
 */
@Owner("Tarun")
public class SubscriptionRenewalViaPpi extends PGPBaseTest {

    private final CheckoutPage checkoutPage = new CheckoutPage();
    private String payMode;
    private final String successMessage = "Subscription Txn accepted.";

    private void validateRenew(Response response, String successMessage){
        JsonPath jsonPath = response.jsonPath();
        String respMsg = jsonPath.get("RESPMSG");
        Reporter.report.info("<br>Renew response message: " + respMsg, new Object[0]);
        Assertions.assertThat(respMsg).isEqualToIgnoringCase(successMessage);
    }

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



    @Parameters({"payMode"})
    @BeforeClass
    public void setPayMode(@Optional("DC") String payMode) {
        this.payMode = payMode.toUpperCase();
    }




    @Parameters({"theme"})
    @Test(description = "merchant initiate the transaction after the end of subscription contract")
    public void PGP_220_RenewalRequestAfterSubscriptionContractExpired(@Optional("enhancedweb") String theme) throws Exception {
        /*Subs*/
        MerchantType merchantType = MerchantType.SUBSCRIPTION_PPI;
        User user = userManager.getForWrite(Label.BASIC,Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.SubscriptionPPI(merchantType, theme)
                .build();
        String subsId = subscribe(orderDTO, theme, user,payMode);
        /*Modifying Subscription End Date to one day back from now*/
        LocalDateTime newSubsEndDate = LocalDateTime.now().minusDays(1);
        PGPHelpers.modifySubscriptionEndDate(Long.valueOf(subsId), newSubsEndDate);
        LocalDateTime newSubsStartDate = LocalDateTime.now().minusMonths(1).minusDays(1);
        PGPHelpers.modifySubscriptionStartDate(Long.valueOf(subsId), newSubsStartDate);
        /*Modify Wallet Balance*/
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.0);
        /*Renew Subs*/
        executeRenewalAndFetchOrderId(merchantType,subsId,orderDTO.getTXN_AMOUNT(),"The subscription has already expired");
    }

//    @Parameters({"theme"})
//    @Test(description = "merchant initiate the renewal request within grace period and retry count exhausted", enabled = false)
    public void PGP_218_RetryRenewalRequestWithinGracePeriodWhenRetryCountGotExhausted(@Optional("enhancedweb") String theme) throws Exception {
        throw new SkipException("retry logic pending");
    }

//    @Parameters({"theme"})
//    @Test(description = "merchant initiate the renewal req within grace period, retry count>0, prior renewal req pending",enabled = false)
    public void PGP_216_RetryRenewalRequestWhenRetryCountIsGreaterThanZero(@Optional("enhancedweb") String theme) throws Exception {
        throw new SkipException("retry logic pending");
    }

    @Issue("PGP-16625")
    @Parameters({"theme"})
    @Test(description = "To verify renewal if merchant initiate the request after the grace period")
    public void PGP_214_RenewalRequestAfterGracePeriod(@Optional("enhancedweb") String theme) throws Exception {
        /*Subs*/
        MerchantType merchantType = MerchantType.SUBSCRIPTION_PPI;
        User user = userManager.getForWrite(Label.BASIC,Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.SubscriptionPPI(merchantType, theme)
                .build();
        String subsId = subscribe(orderDTO, theme, user,payMode);
        /*Modifying Subscription Start Date*/
        LocalDateTime newSubsStartDate = LocalDateTime.now().minusMonths(1).minusDays(Long.valueOf(orderDTO.getSUBS_GRACE_DAYS()) + 2);
        PGPHelpers.modifySubscriptionStartDate(Long.valueOf(subsId), newSubsStartDate);
         /*Modify Wallet Balance*/
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.0);
         /*Renew Subs*/
        String orderId = executeRenewalAndFetchOrderId(merchantType,subsId,orderDTO.getTXN_AMOUNT(),"Payment Request date is not as per the subscription contract");
    }

    @Parameters({"theme"})
    @Test(description = "To verify renewal if merchant initiated the request within the grace period")
    public void PGP_212_RenewalRequestWithinGracePeriod(@Optional("enhancedweb") String theme) throws Exception {
        /*Subs*/
        MerchantType merchantType = MerchantType.SUBSCRIPTION_PPI;
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.SubscriptionPPI(merchantType, theme)
                .build();
        String subsId = subscribe(orderDTO, theme, user,payMode);
         /*Modifying Subscription Date to one month back from now*/
        PGPHelpers.modifySubscriptionStartDate(Long.valueOf(subsId), LocalDateTime.now().minusMonths(1).minusDays(2));
        /*Modify Wallet Balance*/
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.0);
        /*Renew Subs*/
        String orderId = executeRenewalAndFetchOrderId(merchantType,subsId,orderDTO.getTXN_AMOUNT(),successMessage);
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, orderId)).isNotNull().isNotEmpty();
        Assertions.assertThat(PGPHelpers.executeToFetchStatusInPaymentDetails(subsId,orderId)).isEqualTo("ACTIVE");
    }

    @Parameters({"theme"})
    @Test(description = "Renewal success and merchant initiated the same renewal request within the grace period")
    public void PGP_210_multipleRenewalRequestWithinGracePeriod(@Optional("enhancedweb") String theme) throws Exception {
        /*Subs*/
        MerchantType merchantType = MerchantType.SUBSCRIPTION_PPI;
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.SubscriptionPPI(merchantType, theme)
                .build();
        String subsId = subscribe(orderDTO, theme, user,payMode);
        /*Modify Wallet Balance*/
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) + 1.0);
        /*First Renew Subs*/
        String orderId = executeRenewalAndFetchOrderId(merchantType,subsId,orderDTO.getTXN_AMOUNT(), successMessage);
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, orderId)).isNotNull().isNotEmpty();
        /*Modify Wallet Balance*/
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.0);
        /*Second Renew Subs Request*/
        executeRenewalAndFetchOrderId(merchantType,subsId,orderDTO.getTXN_AMOUNT(), "Payment request is already success in this frequency cycle");
    }
    @Parameters({"theme"})
    @Test(description = "To verify successful renewal for Subs First Request via PPI having sufficient wallet balance")
    public void PGP_195_RenewalRequestWhenSufficientWalletBalance(@Optional("enhancedweb") String theme) throws Exception {
       // Subs
        MerchantType merchantType = MerchantType.SUBSCRIPTION_PPI;
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.SubscriptionPPI(merchantType, theme)
                .build();
        String subsId = subscribe(orderDTO, theme, user,payMode);
       // Modify Wallet Balance
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) + 1.00);
       // Renew Subs
        String orderId = executeRenewalAndFetchOrderId(merchantType,subsId,orderDTO.getTXN_AMOUNT(), successMessage);
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, orderId)).isNotNull().isNotEmpty();
        Assertions.assertThat(PGPHelpers.executeToFetchStatusInPaymentDetails(subsId,orderId)).isEqualTo("ACTIVE");

    }

    @Parameters({"theme"})
    @Test(description = "To verify successful renewal for Subs First Request via PPI having insufficient wallet balance")
    public void PGP_196_RenewalRequestWhenInsufficientWalletBalance(@Optional("enhancedweb") String theme) throws Exception {
        /*Subs*/
        MerchantType merchantType = MerchantType.SUBSCRIPTION_PPI;
        User user = userManager.getForWrite(Label.BASIC,Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.SubscriptionPPI(merchantType, theme)
                .build();
        String subsId = subscribe(orderDTO, theme, user,payMode);
        /*Modify Wallet Balance*/
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.0);
        /*Renew Subs*/
        String orderId = executeRenewalAndFetchOrderId(merchantType,subsId,orderDTO.getTXN_AMOUNT(), successMessage);
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, orderId)).isNotNull().isNotEmpty();
        Assertions.assertThat(PGPHelpers.executeToFetchStatusInPaymentDetails(subsId,orderId)).isEqualTo("ACTIVE");

    }

    @Parameters({"theme"})
    @Test(description = "To verify successful renewal for Subs First Request via PPI having zero wallet balance")
    public void PGP_197_RenewalRequestWhenZeroWalletBalance(@Optional("enhancedweb") String theme) throws Exception {
        /*Subs*/
        MerchantType merchantType = MerchantType.SUBSCRIPTION_PPI;
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.SubscriptionPPI(merchantType, theme)
                .build();
        String subsId = subscribe(orderDTO, theme, user,payMode);
        /*Modify Wallet Balance*/
        WalletHelpers.setZeroBalance(user);
        /*Renew Subs*/
        String orderId = executeRenewalAndFetchOrderId(merchantType,subsId,orderDTO.getTXN_AMOUNT(), successMessage);
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, orderId)).isNotNull().isNotEmpty();
        Assertions.assertThat(PGPHelpers.executeToFetchStatusInPaymentDetails(subsId,orderId)).isEqualTo("ACTIVE");

        /*System.out.println(QRHelper.getExtendedInfoBizOrder(orderDTO.getORDER_ID()));*/

    }

    private String subscribe(OrderDTO orderDTO, String theme, User user , String paymentType) throws AuthException, WalletException {
        SavedCardHelpers.deleteSavedCard(user);
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.0);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.login(user);

            if(paymentType.equalsIgnoreCase("DC"))
                cashierPage.payBy(PayMode.DC);
            else  if (paymentType.equalsIgnoreCase( "CC"))
                cashierPage.payBy(PayMode.CC);
            else
                throw new RuntimeException("Invalid PAYMENT_TYPE_ID");

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
                .validateTxnDate(new Date())
                .validateSubsid(Constants.ValidationType.NON_EMPTY)
                .AssertAll();
        String subsId = txnStatus.getResponse().getSUBS_ID();
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, orderDTO.getORDER_ID())).isNotNull().isNotEmpty();
        Assertions.assertThat(PGPHelpers.getSavedCardId(subsId)).isNotNull().isNotEmpty();

        return subsId;
    }

}
