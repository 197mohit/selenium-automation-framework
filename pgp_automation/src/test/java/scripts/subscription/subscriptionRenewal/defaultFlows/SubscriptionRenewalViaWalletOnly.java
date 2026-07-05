package scripts.subscription.subscriptionRenewal.defaultFlows;

import com.paytm.api.TxnStatus;
import com.paytm.appconstants.Constants;
import com.paytm.appconstants.Constants.MerchantType;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.apphelpers.WalletHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
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
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.time.LocalDateTime;

/**
 * Created by sureshgupta on 26/09/17.
 */
@Owner("Tarun")
public class SubscriptionRenewalViaWalletOnly extends PGPBaseTest {

    private final CheckoutPage checkoutPage = new CheckoutPage();
    private final String successMessage = "Subscription Txn accepted.";

    private void validateRenew(Response response, String successMessage){
        JsonPath jsonPath = response.jsonPath();
        String respMsg = jsonPath.get("RESPMSG");
        Reporter.report.info("<br>Renew response message: " + respMsg, new Object[0]);
        Assertions.assertThat(respMsg).isEqualToIgnoringCase(successMessage);
    }

    @Parameters({"theme"})
    @Test(description = "To verify failure when merchant initiate the transaction after the end of subscription contract.")
    public void PGP_219_RenewalRequestAfterSubscriptionContractExpired(@Optional("merchant4") String theme) throws Exception {
    /*Subs*/
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.SubscriptionWalletOnly(MerchantType.SUBSCRIPTION_WALLET_ONLY, theme)
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
        OrderDTO renewOrderDto = new OrderFactory.SubscriptionRenew(MerchantType.SUBSCRIPTION_WALLET_ONLY, subsId, orderDTO.getTXN_AMOUNT()).build();
        validateRenew(PGPHelpers.executeProcessTransaction(renewOrderDto), "Validation failed");
    }

    @Parameters({"theme"})
    @Test(description = "merchant initiate the renewal request within grace period and retry count exhausted")
    public void PGP_217_RetryRenewalRequestWithinGracePeriodWhenRetryCountGotExhausted(@Optional("merchant") String theme) throws Exception {
        /*Subs*/
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.SubscriptionWalletOnly(MerchantType.SUBSCRIPTION_WALLET_ONLY, theme)
                .setSUBS_RETRY_COUNT("1")
                .build();
        String subsId = subscribe(orderDTO, theme,user);
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, orderDTO.getORDER_ID())).isNotNull().isNotEmpty();
        /*Modify Wallet Balance*/
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.0);
        /*Renew Subs*/
        OrderDTO renewOrderDto = new OrderFactory.SubscriptionRenew(MerchantType.SUBSCRIPTION_WALLET_ONLY, subsId, orderDTO.getTXN_AMOUNT()).build();
        validateRenew(PGPHelpers.executeProcessTransaction(renewOrderDto), "Validation failed");
        /*Renew Subs*/
        OrderDTO renewFirstRetryOrder = new OrderFactory.SubscriptionRenew(MerchantType.SUBSCRIPTION_WALLET_ONLY, subsId, orderDTO.getTXN_AMOUNT()).build();
        validateRenew(PGPHelpers.executeProcessTransaction(renewFirstRetryOrder), "Validation failed");

        /*Modify Wallet Balance*/
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) + 1.00);
        /*Renew Subs retry*/
        OrderDTO renewSecondRetryOrder = new OrderFactory.SubscriptionRenew(MerchantType.SUBSCRIPTION_WALLET_ONLY, subsId, orderDTO.getTXN_AMOUNT()).build();
        validateRenew(PGPHelpers.executeProcessTransaction(renewSecondRetryOrder), "failure msg");//TODO add proper failure msg
    }

    @Parameters({"theme"})
    @Test(description = "merchant initiate the renewal req within grace period, retry count>0, prior renewal req pending")
    public void PGP_215_RetryRenewalRequestWhenRetryCountIsGreaterThanZero(@Optional("merchant4") String theme) throws Exception {
        /*Subs*/
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.SubscriptionWalletOnly(MerchantType.SUBSCRIPTION_WALLET_ONLY, theme)
                .setSUBS_RETRY_COUNT("1")
                .build();
        String subsId = subscribe(orderDTO, theme,user);
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, orderDTO.getORDER_ID())).isNotNull().isNotEmpty();
        /*Modify Wallet Balance*/
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.0);
        /*Renew Subs*/
        OrderDTO renewOrderDto = new OrderFactory.SubscriptionRenew(MerchantType.SUBSCRIPTION_WALLET_ONLY, subsId, orderDTO.getTXN_AMOUNT()).build();
        validateRenew(PGPHelpers.executeProcessTransaction(renewOrderDto), "Validation failed");

        /*Modify Wallet Balance*/
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) + 1.00);
        /*Renew Subs retry*/
        OrderDTO renewRetryOrder = new OrderFactory.SubscriptionRenew(MerchantType.SUBSCRIPTION_WALLET_ONLY, subsId, orderDTO.getTXN_AMOUNT()).build();
        validateRenew(PGPHelpers.executeProcessTransaction(renewRetryOrder), successMessage);
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, renewOrderDto.getORDER_ID())).isNotNull();
    }

    @Parameters({"theme"})
    @Test(description = "To verify renewal if merchant initiate the request after the grace period")
    public void PGP_213_RenewalRequestAfterGracePeriod(@Optional("merchant4") String theme) throws Exception {
        /*Subs*/
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.SubscriptionWalletOnly(MerchantType.SUBSCRIPTION_WALLET_ONLY, theme)
                .build();
        String subsId = subscribe(orderDTO, theme,user);
        /*Modifying Subscription Start Date*/
        LocalDateTime newDate = LocalDateTime.now().minusMonths(1).minusDays(Long.valueOf(orderDTO.getSUBS_GRACE_DAYS()) + 2);
        PGPHelpers.modifySubscriptionStartDate(Long.valueOf(subsId), newDate);
        /*Modify Wallet Balance*/
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) + 1.00);
        /*Renew Subs*/
        OrderDTO renewOrderDto = new OrderFactory.SubscriptionRenew(MerchantType.SUBSCRIPTION_WALLET_ONLY, subsId, orderDTO.getTXN_AMOUNT()).build();
        validateRenew(PGPHelpers.executeProcessTransaction(renewOrderDto), "Validation failed");
    }

    @Parameters({"theme"})
    @Test(description = "To verify renewal if merchant initiated the request within the grace period")
    public void PGP_211_RenewalRequestWithinGracePeriod(@Optional("merchant4") String theme) throws Exception {
        /*Subs*/
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.SubscriptionWalletOnly(MerchantType.SUBSCRIPTION_WALLET_ONLY, theme)
                .build();
        String subsId = subscribe(orderDTO, theme,user);
        /*Modifying Subscription Date to one month back from now*/
        PGPHelpers.modifySubscriptionStartDate(Long.valueOf(subsId), LocalDateTime.now().minusMonths(1));
        /*Modify Wallet Balance*/
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) + 1);
        /*Renew Subs*/
        OrderDTO renewOrderDto = new OrderFactory.SubscriptionRenew(MerchantType.SUBSCRIPTION_WALLET_ONLY, subsId, orderDTO.getTXN_AMOUNT()).build();
        validateRenew(PGPHelpers.executeProcessTransaction(renewOrderDto), successMessage);
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, renewOrderDto.getORDER_ID())).isNotNull();
    }

    @Parameters({"theme"})
    @Test(description = "To verify succesful renewal for Subs First Request via Wallet only having sufficient wallet balance")
    public void PGP_198_RenewalRequestWhenSufficientWalletBalance(@Optional("merchant4") String theme) throws Exception {
        /*Subs*/
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.SubscriptionWalletOnly(MerchantType.SUBSCRIPTION_WALLET_ONLY, theme)
                .build();
        String subsId = subscribe(orderDTO, theme,user);
        /*Modify Wallet Balance*/
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) + 1.00);
        /*Renew Subs*/
        OrderDTO renewOrderDto = new OrderFactory.SubscriptionRenew(MerchantType.SUBSCRIPTION_WALLET_ONLY, subsId, orderDTO.getTXN_AMOUNT()).build();
        validateRenew(PGPHelpers.executeProcessTransaction(renewOrderDto), successMessage);
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, renewOrderDto.getORDER_ID())).isNotNull();
    }

    @Parameters({"theme"})
    @Test(description = "To verify unsuccessful renewal for Subs First Request via wallet only having insufficient wallet balance")
    public void PGP_199_RenewalRequestWhenInsufficientWalletBalance(@Optional("merchant4") String theme) throws Exception {
        /*Subs*/
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.SubscriptionWalletOnly(MerchantType.SUBSCRIPTION_WALLET_ONLY, theme)
                .build();
        String subsId = subscribe(orderDTO, theme,user);
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, orderDTO.getORDER_ID())).isNotNull().isNotEmpty();
        /*Modify Wallet Balance*/
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.0);
        /*Renew Subs*/
        OrderDTO renewOrderDto = new OrderFactory.SubscriptionRenew(MerchantType.SUBSCRIPTION_WALLET_ONLY, subsId, orderDTO.getTXN_AMOUNT()).build();
        validateRenew(PGPHelpers.executeProcessTransaction(renewOrderDto), "Validation failed");
    }

    @Parameters({"theme"})
    @Test(description = "Renewal success and merchant initiated the same renewal request within the grace period")
    public void PGP_209_multipleRenewalRequestWithinGracePeriod(@Optional("merchant4") String theme) throws Exception {
        /*Subs*/
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.SubscriptionWalletOnly(MerchantType.SUBSCRIPTION_WALLET_ONLY, theme)
                .build();
        String subsId = subscribe(orderDTO, theme,user);
        /*Modify Wallet Balance*/
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) + 1.00);
        /*First Renew Subs*/
        OrderDTO renewOrderDto = new OrderFactory.SubscriptionRenew(MerchantType.SUBSCRIPTION_WALLET_ONLY, subsId, orderDTO.getTXN_AMOUNT()).build();
        validateRenew(PGPHelpers.executeProcessTransaction(renewOrderDto), successMessage);
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, renewOrderDto.getORDER_ID())).isNotNull();
         /*Modify Wallet Balance*/
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) + 1.00);
        /*Second Renew Subs Request*/
        OrderDTO renewOrderDto1 = new OrderFactory.SubscriptionRenew(MerchantType.SUBSCRIPTION_WALLET_ONLY, subsId, orderDTO.getTXN_AMOUNT()).build();
        validateRenew(PGPHelpers.executeProcessTransaction(renewOrderDto1), "Subscription already in progress.");
    }

    private String subscribe(OrderDTO orderDTO, String theme, User user) throws AuthException, WalletException {
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) + 1.00);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.login(user);
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
        return subsId;
    }

}
