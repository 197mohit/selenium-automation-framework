package scripts.subscription.subscriptionRenewal.defaultFlows;

import com.paytm.api.TxnStatus;
import com.paytm.api.nativeAPI.RenewSubscription;
import com.paytm.appconstants.Constants;
import com.paytm.appconstants.Constants.MerchantType;
import com.paytm.appconstants.Constants.PayMode;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.apphelpers.SavedCardHelpers;
import com.paytm.apphelpers.WalletHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.RenewSubscription.RenewSubscriptionDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.framework.reporting.Reporter;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
import com.paytm.utils.merchant.util.exception.authException.AuthException;
import com.paytm.utils.merchant.util.exception.walletException.WalletException;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.testng.SkipException;
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


    @Parameters({"theme"})
    @Test(description = "merchant initiate the transaction after the end of subscription contract")
    public void PGP_220_RenewalRequestAfterSubscriptionContractExpired(@Optional("merchant4") String theme) throws Exception {
        /*Subs*/
        MerchantType merchantType = MerchantType.SUBSCRIPTION_PPI;
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.SubscriptionPPI(merchantType, theme)
                .build();
        String subsId = subscribe(orderDTO, theme, user);
        /*Modifying Subscription End Date to one day back from now*/
        LocalDateTime newSubsEndDate = LocalDateTime.now().minusDays(1);
        PGPHelpers.modifySubscriptionEndDate(Long.valueOf(subsId), newSubsEndDate);
        LocalDateTime newSubsStartDate = LocalDateTime.now().minusMonths(1).minusDays(1);
        PGPHelpers.modifySubscriptionStartDate(Long.valueOf(subsId), newSubsStartDate);
        /*Modify Wallet Balance*/
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.0);
        /*Renew Subs*/
        executeRenewalAndFetchOrderId(merchantType,subsId,orderDTO.getTXN_AMOUNT(),"Validation failed");
    }

//    @Parameters({"theme"})
//    @Test(description = "merchant initiate the renewal request within grace period and retry count exhausted", enabled = false)
    public void PGP_218_RetryRenewalRequestWithinGracePeriodWhenRetryCountGotExhausted(@Optional("merchant4") String theme) throws Exception {
        throw new SkipException("retry logic pending");
    }

//    @Parameters({"theme"})
//    @Test(description = "merchant initiate the renewal req within grace period, retry count>0, prior renewal req pending",enabled = false)
    public void PGP_216_RetryRenewalRequestWhenRetryCountIsGreaterThanZero(@Optional("merchant4") String theme) throws Exception {
        throw new SkipException("retry logic pending");
    }

    @Parameters({"theme"})
    @Test(description = "To verify renewal if merchant initiate the request after the grace period")
    public void PGP_214_RenewalRequestAfterGracePeriod(@Optional("merchant4") String theme) throws Exception {
        /*Subs*/
        MerchantType merchantType = MerchantType.SUBSCRIPTION_PPI;
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.SubscriptionPPI(MerchantType.SUBSCRIPTION_PPI, theme)
                .build();
        String subsId = subscribe(orderDTO, theme, user);
        /*Modifying Subscription Start Date*/
        LocalDateTime newSubsStartDate = LocalDateTime.now().minusMonths(1).minusDays(Long.valueOf(orderDTO.getSUBS_GRACE_DAYS()) + 2);
        PGPHelpers.modifySubscriptionStartDate(Long.valueOf(subsId), newSubsStartDate);
         /*Modify Wallet Balance*/
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.0);
         /*Renew Subs*/
        OrderDTO renewOrderDto = new OrderFactory.SubscriptionRenew(MerchantType.SUBSCRIPTION_PPI, subsId, orderDTO.getTXN_AMOUNT()).build();
        validateRenew(PGPHelpers.executeProcessTransaction(renewOrderDto), "Validation failed");
    }



//    @Parameters({"theme"})
//    @Test(description = "To verify renewal if merchant initiated the request within the grace period", enabled = false)
    public void PGP_212_RenewalRequestWithinGracePeriod(@Optional("merchant4") String theme) throws Exception {
        /*Subs*/
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.SubscriptionPPI(MerchantType.SUBSCRIPTION_PPI, theme)
                .build();
        String subsId = subscribe(orderDTO, theme, user);
         /*Modifying Subscription Date to one month back from now*/
        PGPHelpers.modifySubscriptionStartDate(Long.valueOf(subsId), LocalDateTime.now().minusMonths(1));
        /*Modify Wallet Balance*/
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.0);
        /*Renew Subs*/
        OrderDTO renewOrderDto = new OrderFactory.SubscriptionRenew(MerchantType.SUBSCRIPTION_PPI, subsId, orderDTO.getTXN_AMOUNT()).build();
        validateRenew(PGPHelpers.executeProcessTransaction(renewOrderDto), successMessage);
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, renewOrderDto.getORDER_ID())).isNotNull().isNotEmpty();
    }

    @Parameters({"theme"})
    @Test(description = "Renewal success and merchant initiated the same renewal request within the grace period")
    public void PGP_210_multipleRenewalRequestWithinGracePeriod(@Optional("merchant4") String theme) throws Exception {
        /*Subs*/
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.SubscriptionPPI(MerchantType.SUBSCRIPTION_PPI, theme)
                .build();
        String subsId = subscribe(orderDTO, theme, user);
        /*Modify Wallet Balance*/
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.0);
        /*First Renew Subs*/
        OrderDTO renewOrderDto = new OrderFactory.SubscriptionRenew(MerchantType.SUBSCRIPTION_PPI, subsId, orderDTO.getTXN_AMOUNT()).build();
        validateRenew(PGPHelpers.executeProcessTransaction(renewOrderDto), successMessage);
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, renewOrderDto.getORDER_ID())).isNotNull().isNotEmpty();
        /*Modify Wallet Balance*/
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.0);
        /*Second Renew Subs Request*/
        OrderDTO renewOrderDto1 = new OrderFactory.SubscriptionRenew(MerchantType.SUBSCRIPTION_PPI, subsId, orderDTO.getTXN_AMOUNT()).build();
        validateRenew(PGPHelpers.executeProcessTransaction(renewOrderDto1), "Subscription already in progress.");
    }
    @Parameters({"theme"})
    @Test(description = "To verify successful renewal for Subs First Request via PPI having sufficient wallet balance")
    public void PGP_195_RenewalRequestWhenSufficientWalletBalance(@Optional("merchant4") String theme) throws Exception {
       // Subs
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.SubscriptionPPI(MerchantType.SUBSCRIPTION_PPI, theme)
                .build();
        String subsId = subscribe(orderDTO, theme, user);
       // Modify Wallet Balance
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) + 1.00);
       // Renew Subs
        OrderDTO renewOrderDto = new OrderFactory.SubscriptionRenew(MerchantType.SUBSCRIPTION_PPI, subsId, orderDTO.getTXN_AMOUNT()).build();
        validateRenew(PGPHelpers.executeProcessTransaction(renewOrderDto), successMessage);
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, renewOrderDto.getORDER_ID())).isNotNull().isNotEmpty();
    }

    @Parameters({"theme"})
    @Test(description = "To verify successful renewal for Subs First Request via PPI having insufficient wallet balance")
    public void PGP_196_RenewalRequestWhenInsufficientWalletBalance(@Optional("merchant4") String theme) throws Exception {
        /*Subs*/
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.SubscriptionPPI(MerchantType.SUBSCRIPTION_PPI, theme)
                .build();
        String subsId = subscribe(orderDTO, theme, user);
        /*Modify Wallet Balance*/
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.0);
        /*Renew Subs*/
        OrderDTO renewOrderDto = new OrderFactory.SubscriptionRenew(MerchantType.SUBSCRIPTION_PPI, subsId, orderDTO.getTXN_AMOUNT()).build();
        validateRenew(PGPHelpers.executeProcessTransaction(renewOrderDto), successMessage);
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, renewOrderDto.getORDER_ID())).isNotNull().isNotEmpty();
    }

    @Parameters({"theme"})
    @Test(description = "To verify successful renewal for Subs First Request via PPI having zero wallet balance")
    public void PGP_197_RenewalRequestWhenZeroWalletBalance(@Optional("merchant4") String theme) throws Exception {
        /*Subs*/
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.SubscriptionPPI(MerchantType.SUBSCRIPTION_PPI, theme)
                .build();
        String subsId = subscribe(orderDTO, theme, user);
        /*Modify Wallet Balance*/
        WalletHelpers.setZeroBalance(user);
        /*Renew Subs*/
        OrderDTO renewOrderDto = new OrderFactory.SubscriptionRenew(MerchantType.SUBSCRIPTION_PPI, subsId, orderDTO.getTXN_AMOUNT()).build();
        validateRenew(PGPHelpers.executeProcessTransaction(renewOrderDto), successMessage);
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, renewOrderDto.getORDER_ID())).isNotNull().isNotEmpty();
    }

    @Parameters({"theme"})
    @Test(description = "To verify succesful renewal for Subs First Request via S2S(PPI) having sufficient wallet balance")
    public void PGP_200_verifyRenewalRequestWhenSufficientWalletBalanceAndSubsRequestViaS2S() throws Exception {
        /*Subs*/
        User user = userManager.getForWrite(Label.LOGIN);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        String savedCardId = SavedCardHelpers.getSavedCardId(user, 0);
        System.out.println("Saved card id:" + savedCardId);
        OrderDTO orderDTO = new OrderFactory.SubscriptionS2S(MerchantType.SUBSCRIPTION_PPI, savedCardId, user)
                .setSUBS_PAYMENT_MODE("PPI")
                .build();
        Response response = PGPHelpers.executeProcessTransaction(orderDTO);
        Assertions.assertThat(response.jsonPath().get("STATUS").toString()).isEqualToIgnoringCase("TXN_SUCCESS");
        String subsId = response.jsonPath().get("SUBS_ID");
        Assertions.assertThat(PGPHelpers.getSavedCardId(subsId)).isNotNull().isNotEmpty();
         /*Modify Wallet Balance*/
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) + 1.00);
        /*Renew Subs*/
        OrderDTO renewOrderDto = new OrderFactory.SubscriptionRenew(MerchantType.SUBSCRIPTION_PPI, subsId, orderDTO.getTXN_AMOUNT()).build();
        validateRenew(PGPHelpers.executeProcessTransaction(renewOrderDto), successMessage);
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, renewOrderDto.getORDER_ID())).isNotNull().isNotEmpty();
    }

    @Parameters({"theme"})
    @Test(description = "To verify successful renewal for Subs First Request via S2S(PPI) having insufficient wallet balance")
    public void PGP_201_verifyRenewalRequestWhenSufficientWalletBalanceAndSubsRequestViaS2S() throws Exception {
        /*Subs*/
        User user = userManager.getForWrite(Label.LOGIN);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        String savedCardId = SavedCardHelpers.getSavedCardId(user, 0);
        System.out.println("Saved card id:" + savedCardId);
        OrderDTO orderDTO = new OrderFactory.SubscriptionS2S(MerchantType.SUBSCRIPTION_PPI, savedCardId, user)
                .setSUBS_PAYMENT_MODE("PPI")
                .build();
        Response response = PGPHelpers.executeProcessTransaction(orderDTO);
        Assertions.assertThat(response.jsonPath().get("STATUS").toString()).isEqualToIgnoringCase("TXN_SUCCESS");
        String subsId = response.jsonPath().get("SUBS_ID");
        Assertions.assertThat(PGPHelpers.getSavedCardId(subsId)).isNotNull().isNotEmpty();
        /*Modify Wallet Balance*/
        WalletHelpers.modifyBalance(user, 1.00);
         /*Renew Subs*/
        OrderDTO renewOrderDto = new OrderFactory.SubscriptionRenew(MerchantType.SUBSCRIPTION_PPI, subsId, orderDTO.getTXN_AMOUNT()).build();
        validateRenew(PGPHelpers.executeProcessTransaction(renewOrderDto), successMessage);
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, renewOrderDto.getORDER_ID())).isNotNull().isNotEmpty();

    }

    @Parameters({"theme"})
    @Test(description = "To verify successful renewal for Subs First Request via S2S(PPI) having zero wallet balance")
    public void PGP_202_verifyRenewalRequestWhenZeroWalletBalanceAndSubsRequestViaS2S() throws Exception {
        /*Subs*/
        User user = userManager.getForWrite(Label.LOGIN);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        String savedCardId = SavedCardHelpers.getSavedCardId(user, 0);
        System.out.println("Saved card id:" + savedCardId);
        OrderDTO orderDTO = new OrderFactory.SubscriptionS2S(MerchantType.SUBSCRIPTION_PPI, savedCardId, user)
                .setSUBS_PAYMENT_MODE("PPI")
                .build();
        Response response = PGPHelpers.executeProcessTransaction(orderDTO);
        Assertions.assertThat(response.jsonPath().get("STATUS").toString()).isEqualToIgnoringCase("TXN_SUCCESS");
        String subsId = response.jsonPath().get("SUBS_ID");
        Assertions.assertThat(PGPHelpers.getSavedCardId(subsId)).isNotNull().isNotEmpty();
        /*Modify Wallet Balance*/
        WalletHelpers.setZeroBalance(user);
         /*Renew Subs*/
        String orderIdForRenew = CommonHelpers.generateOrderId();
        String respMsg = PGPHelpers.renewSubscription(orderIdForRenew, orderDTO.getMID(), 1.00, subsId);
        Assertions.assertThat(respMsg).isEqualToIgnoringCase("Subscription Txn accepted.");
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, orderIdForRenew)).isNotNull().isNotEmpty();
    }


    private String subscribe(OrderDTO orderDTO, String theme, User user) throws AuthException, WalletException {
        SavedCardHelpers.deleteSavedCard(user);
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.0);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.login(user);
        cashierPage.payBy(PayMode.CC);
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
