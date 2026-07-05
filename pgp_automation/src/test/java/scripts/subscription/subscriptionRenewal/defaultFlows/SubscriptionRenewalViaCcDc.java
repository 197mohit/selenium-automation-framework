package scripts.subscription.subscriptionRenewal.defaultFlows;

import com.paytm.api.TxnStatus;
import com.paytm.appconstants.Constants;
import com.paytm.appconstants.Constants.MerchantType;
import com.paytm.appconstants.Constants.PayMode;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.apphelpers.SavedCardHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.framework.reporting.Reporter;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.time.LocalDateTime;

/**
 * Created by sureshgupta on 26/09/17.
 */
@Owner("Tarun")
public class SubscriptionRenewalViaCcDc extends PGPBaseTest {

    private final CheckoutPage checkoutPage = new CheckoutPage();
    private String payMode;
    private final String successMessage = "Subscription Txn accepted.";

    private void validateRenew(Response response, String successMessage){
        JsonPath jsonPath = response.jsonPath();
        String respMsg = jsonPath.get("RESPMSG");
        Reporter.report.info("<br>Renew response message: " + respMsg);
        Assertions.assertThat(respMsg).isEqualToIgnoringCase(successMessage);
    }

    private TxnStatus validateTxnStatus(OrderDTO orderDTO){
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("HDFC")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName("HDFC Bank")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode(payMode)
                .validateRefundAmnt("0.00")
                .validateSubsid(Constants.ValidationType.NON_EMPTY)
                .AssertAll();
        return txnStatus;
    }

    @Parameters({"payMode"})
    @BeforeClass
    public void setPayMode(@Optional("CC") String payMode) {
        this.payMode = payMode.toUpperCase();
    }

    @Parameters({"theme"})
    @Test(description = "To verify successful renewal for Subs First Request via CC")
    public void PGP_192_verifySuccessfulRenewalOnSubsViaCC(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.SubscriptionCC_DC(MerchantType.Subscription_PGOnly, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        SavedCardHelpers.deleteSavedCard(user);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.valueOf(payMode));

        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = validateTxnStatus(orderDTO);
        String subsId = txnStatus.getResponse().getSUBS_ID();
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, orderDTO.getORDER_ID())).isNotNull().isNotEmpty();
        Assertions.assertThat(PGPHelpers.getSavedCardId(subsId)).isNotNull().isNotEmpty();
        /*Renew Subs*/
        OrderDTO renewOrderDto = new OrderFactory.SubscriptionRenew(MerchantType.Subscription_PGOnly, subsId, orderDTO.getTXN_AMOUNT()).build();
        validateRenew(PGPHelpers.executeProcessTransaction(renewOrderDto), successMessage);
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, renewOrderDto.getORDER_ID())).isNotNull();
    }

//    @Parameters({"theme"})
//    @Test(description = "To verify successful renewal for Subs First Request via DC", enabled = false)
    public void PGP_193_verifySuccessfulRenewalOnSubsViaDC(@Optional("merchant4") String theme) throws Exception {//need to configure DC paymode in merchant
        /*Subs*/
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.SubscriptionCC_DC(MerchantType.Subscription_PGOnly, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        SavedCardHelpers.deleteSavedCard(user);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        String dcNum = paymentDTO.getDebitCardNumber();
        paymentDTO.setCreditCardNumber(dcNum);
        cashierPage.payBy(PayMode.DC);

        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("HDFC")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName("HDFC Bank")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .validateSubsid(Constants.ValidationType.NON_EMPTY)
                .AssertAll();
        String subsId = txnStatus.getResponse().getSUBS_ID();
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, orderDTO.getORDER_ID())).isNotNull().isNotEmpty();
        Assertions.assertThat(PGPHelpers.getSavedCardId(subsId)).isNotNull().isNotEmpty();
        /*Renew Subs*/
        OrderDTO renewOrderDto = new OrderFactory.SubscriptionRenew(MerchantType.Subscription_PGOnly, subsId, orderDTO.getTXN_AMOUNT()).build();
        validateRenew(PGPHelpers.executeProcessTransaction(renewOrderDto), successMessage);
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, renewOrderDto.getORDER_ID())).isNotNull();

    }

//    @Parameters({"theme"})
//    @Test(description = "Verify the Renewal failure when saved card is removed", enabled = false)
    public void PGP_194_verifyRenewalFailureWhenSavedCardIsRemoved(@Optional("merchant4") String theme) throws Exception {
        /*Subs*/
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.SubscriptionCC_DC(MerchantType.Subscription_PGOnly, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        SavedCardHelpers.deleteSavedCard(user);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.valueOf(payMode));

        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("HDFC")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName("HDFC Bank")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode(payMode)
                .validateRefundAmnt("0.00")
                .validateSubsid(Constants.ValidationType.NON_EMPTY)
                .AssertAll();
        String subsId = txnStatus.getResponse().getSUBS_ID();
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, orderDTO.getORDER_ID())).isNotNull().isNotEmpty();
        Assertions.assertThat(PGPHelpers.getSavedCardId(subsId)).isNotNull().isNotEmpty();
        /*Renew Subs*/
        SavedCardHelpers.deleteSavedCard(user);
        String orderIdForRenew = CommonHelpers.generateOrderId();
        String respMsg = PGPHelpers.renewSubscription(orderIdForRenew, orderDTO.getMID(), Double.valueOf(orderDTO.getTXN_AMOUNT()), subsId);
        Assertions.assertThat(respMsg).isEqualToIgnoringCase("Invalid stored card id.");
    }

    @Parameters({"theme"})
    @Test(description = "Verify new Renewal should fail when existing renewal success and merchant initiated the same renewal request within the grace period")
    public void PGP_203_multipleRenewalRequestWithinGracePeriod(@Optional("merchant4") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.SubscriptionCC_DC(MerchantType.Subscription_PGOnly, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        SavedCardHelpers.deleteSavedCard(user);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.valueOf(payMode));

        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = validateTxnStatus(orderDTO);
        String subsId = txnStatus.getResponse().getSUBS_ID();
        /*First Renew Subs*/
        OrderDTO renewOrderDto = new OrderFactory.SubscriptionRenew(MerchantType.Subscription_PGOnly, subsId, orderDTO.getTXN_AMOUNT()).build();
        validateRenew(PGPHelpers.executeProcessTransaction(renewOrderDto), successMessage);
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, renewOrderDto.getORDER_ID())).isNotNull();
          /*Second Renew Subs Request*/
       OrderDTO renewOrderDto1 = new OrderFactory.SubscriptionRenew(MerchantType.Subscription_PGOnly, subsId, orderDTO.getTXN_AMOUNT()).build();
        validateRenew(PGPHelpers.executeProcessTransaction(renewOrderDto1),"Payment request is already success in this frequency cycle");
    }

    @Parameters({"theme"})
    @Test(description = "To verify successful renewal if merchant initiate the request within the grace period")
    public void PGP_204_RenewalRequestWithinGracePeriod(@Optional("merchant4") String theme) throws Exception {
        /*Subs*/
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.SubscriptionCC_DC(MerchantType.Subscription_PGOnly, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        SavedCardHelpers.deleteSavedCard(user);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.valueOf(payMode));

        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = validateTxnStatus(orderDTO);
        String subsId = txnStatus.getResponse().getSUBS_ID();
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, orderDTO.getORDER_ID())).isNotNull().isNotEmpty();
        Assertions.assertThat(PGPHelpers.getSavedCardId(subsId)).isNotNull().isNotEmpty();
        /*Modifying Subscription Date to one month back from now*/
        PGPHelpers.modifySubscriptionStartDate(Long.valueOf(subsId), LocalDateTime.now().minusMonths(1));
        /*Renew Subs*/
        OrderDTO renewOrderDto = new OrderFactory.SubscriptionRenew(MerchantType.Subscription_PGOnly, subsId, orderDTO.getTXN_AMOUNT()).build();
        validateRenew(PGPHelpers.executeProcessTransaction(renewOrderDto), successMessage);
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, renewOrderDto.getORDER_ID())).isNotNull();
    }

    @Parameters({"theme"})
    @Test(description = "To verify renewal failure if merchant initiate the request after the grace period.")
    public void PGP_205_RenewalRequestAfterGracePeriod(@Optional("merchant4") String theme) throws Exception {
        /*Subs*/
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.SubscriptionCC_DC(MerchantType.Subscription_PGOnly, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        SavedCardHelpers.deleteSavedCard(user);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.valueOf(payMode));

        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = validateTxnStatus(orderDTO);
        String subsId = txnStatus.getResponse().getSUBS_ID();
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, orderDTO.getORDER_ID())).isNotNull().isNotEmpty();
        Assertions.assertThat(PGPHelpers.getSavedCardId(subsId)).isNotNull().isNotEmpty();
        /*Modifying Subscription Start Date to one month and one day back from now*/
        LocalDateTime newDate = LocalDateTime.now().minusMonths(1).minusDays(Long.valueOf(orderDTO.getSUBS_GRACE_DAYS()) + 2);
        PGPHelpers.modifySubscriptionStartDate(Long.valueOf(subsId), newDate);
        /*Renew Subs*/
        OrderDTO renewOrderDto = new OrderFactory.SubscriptionRenew(MerchantType.Subscription_PGOnly, subsId, orderDTO.getTXN_AMOUNT()).build();
        validateRenew(PGPHelpers.executeProcessTransaction(renewOrderDto), "Payment Request date is not as per the subscription contract");
    }



    @Parameters({"theme"})
    @Test(description = "merchant initiate the transaction after the end of subscription contract")
    public void PGP_208_RenewalRequestAfterContractExpiration(@Optional("merchant4") String theme) throws Exception {
         /*Subs*/
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.SubscriptionCC_DC(MerchantType.Subscription_PGOnly, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        SavedCardHelpers.deleteSavedCard(user);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.valueOf(payMode));

        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = validateTxnStatus(orderDTO);
        String subsId = txnStatus.getResponse().getSUBS_ID();
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, orderDTO.getORDER_ID())).isNotNull().isNotEmpty();
        Assertions.assertThat(PGPHelpers.getSavedCardId(subsId)).isNotNull().isNotEmpty();
        /*Modifying Subscription End Date to one day back from now*/
        LocalDateTime newSubsEndDate = LocalDateTime.now().minusDays(1);
        PGPHelpers.modifySubscriptionEndDate(Long.valueOf(subsId), newSubsEndDate);
        /*Renew Subs*/
        OrderDTO renewOrderDto = new OrderFactory.SubscriptionRenew(MerchantType.Subscription_PGOnly, subsId, orderDTO.getTXN_AMOUNT()).build();
        validateRenew(PGPHelpers.executeProcessTransaction(renewOrderDto), "The subscription has already expired");

    }


    @Parameters({"theme"})
    @Test(description = "To verify failed renew when save card is expired")
    public void verifyFailedRenew_WithExpiredCardDetail(@Optional("merchant4") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.SubscriptionCC_DC(MerchantType.Subscription_PGOnly, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        SavedCardHelpers.deleteSavedCard(user);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.valueOf(payMode));

        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = validateTxnStatus(orderDTO);
        String subsId = txnStatus.getResponse().getSUBS_ID();
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, orderDTO.getORDER_ID())).isNotNull().isNotEmpty();
        Assertions.assertThat(PGPHelpers.getSavedCardId(subsId)).isNotNull().isNotEmpty();
        //Update savecard expiry with expired value
        SavedCardHelpers.updateCardExpiry_withExpiredExpiry(PGPHelpers.getSavedCardId(subsId), user);
        /*Renew Subs*/
        OrderDTO renewOrderDto = new OrderFactory.SubscriptionRenew(MerchantType.Subscription_PGOnly, subsId, orderDTO.getTXN_AMOUNT()).build();
        validateRenew(PGPHelpers.executeProcessTransaction(renewOrderDto), "Invalid SavedCardID");
    }

}
