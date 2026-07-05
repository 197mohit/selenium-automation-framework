package scripts.subscription.subscriptionRenewal.enhancedFlows;

import com.paytm.api.TxnStatus;
import com.paytm.api.nativeAPI.RenewSubscription;
import com.paytm.appconstants.Constants;
import com.paytm.appconstants.Constants.MerchantType;
import com.paytm.appconstants.Constants.PayMode;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.apphelpers.SavedCardHelpers;
import com.paytm.base.test.Group;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.RenewSubscription.RenewSubscriptionDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
import io.qameta.allure.Issue;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * Created by sureshgupta on 26/09/17.
 */
@Owner("Tarun")
public class SubscriptionRenewalViaCcDc extends PGPBaseTest {

    private final CheckoutPage checkoutPage = new CheckoutPage();
    private String payMode;
    private final String successMessage = "Subscription Txn accepted.";

    private String executeRenewalAndFetchOrderId(MerchantType merchantType, String subsId, String txnAmount, String message) {
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

    public void compareSubdatesafterRenewal(String initialDate, String renewalDate) throws ParseException, ParseException {
        SimpleDateFormat sdfo = new SimpleDateFormat("yyyy-MM-dd");
        Date initial = sdfo.parse(initialDate);
        Date renewal = sdfo.parse(renewalDate);
        Assertions.assertThat(renewal.compareTo(initial) > 0);
    }

    private TxnStatus validateTxnStatus(OrderDTO orderDTO) {
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
        MerchantType merchantType = MerchantType.Subscription_PGOnly;
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.SubscriptionCC_DC(merchantType, theme)
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
        String initialDate = PGPHelpers.getSubsDate( subsId);
        /*Renew Subs*/
        String orderId = executeRenewalAndFetchOrderId(merchantType, subsId, orderDTO.getTXN_AMOUNT(), successMessage);
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, orderId)).isNotNull();
        Assertions.assertThat(PGPHelpers.executeToFetchStatusInPaymentDetails(subsId, orderId)).isEqualTo("ACTIVE");
        String renewalDate = PGPHelpers.getSubsDate( subsId);
        compareSubdatesafterRenewal(initialDate, renewalDate); //PGP-17952
    }

//    @Parameters({"theme"})
//    @Test(description = "To verify successful renewal for Subs First Request via DC", enabled = false)
    public void PGP_193_verifySuccessfulRenewalOnSubsViaDC(@Optional("enhancedweb") String theme) throws Exception {//need to configure DC paymode in merchant
        /*Subs*/
        MerchantType merchantType = MerchantType.Subscription_PGOnly;
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.SubscriptionCC_DC(merchantType, theme)
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
        String orderId = executeRenewalAndFetchOrderId(merchantType, subsId, orderDTO.getTXN_AMOUNT(), successMessage);
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, orderId)).isNotNull();
        Assertions.assertThat(PGPHelpers.executeToFetchStatusInPaymentDetails(subsId, orderId)).isEqualTo("ACTIVE");

    }

    @Issue("PGP-18487")
    @Parameters({"theme"})
    @Test(groups = Group.Status.BUG, description = "Verify the Renewal failure when saved card is removed")
    public void PGP_194_verifyRenewalFailureWhenSavedCardIsRemoved(@Optional("enhancedwap") String theme) throws Exception {
        /*Subs*/
        MerchantType merchantType = MerchantType.Subscription_PGOnly;
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.SubscriptionCC_DC(merchantType, theme)
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
        SavedCardHelpers.deleteSavedCard(user);

        OrderDTO renewOrderDto = new OrderFactory.SubscriptionRenew(merchantType, subsId, orderDTO.getTXN_AMOUNT())
                .build();

        Response response = PGPHelpers.executeProcessTransaction(renewOrderDto);
        String respMsg = response.jsonPath().getString("RESPMSG");

        Assertions.assertThat(respMsg).isEqualToIgnoringCase("Invalid stored card id.");

    }

    @Parameters({"theme"})
    @Test(description = "Verify new Renewal should fail when existing renewal success and merchant initiated the same renewal request within the grace period")
    public void PGP_203_multipleRenewalRequestWithinGracePeriod(@Optional("enhancedweb") String theme) throws Exception {

        MerchantType merchantType = MerchantType.Subscription_PGOnly;
        User user = userManager.getForWrite(Label.LOGIN);
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
        String orderId = executeRenewalAndFetchOrderId(merchantType, subsId, orderDTO.getTXN_AMOUNT(), successMessage);
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, orderId)).isNotNull();
        Assertions.assertThat(PGPHelpers.executeToFetchStatusInPaymentDetails(subsId, orderId)).isEqualTo("ACTIVE");

        /*Second Renew Subs Request*/
        executeRenewalAndFetchOrderId(merchantType, subsId, orderDTO.getTXN_AMOUNT(), "Payment request is already success in this frequency cycle");
    }

    @Parameters({"theme"})
    @Test(description = "To verify successful renewal if merchant initiate the request within the grace period")
    public void PGP_204_RenewalRequestWithinGracePeriod(@Optional("enhancedweb") String theme) throws Exception {
        /*Subs*/
        MerchantType merchantType = MerchantType.Subscription_PGOnly;
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.SubscriptionCC_DC(merchantType, theme)
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
        String orderId = executeRenewalAndFetchOrderId(merchantType, subsId, orderDTO.getTXN_AMOUNT(), successMessage);
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, orderId)).isNotNull().isNotEmpty();
        Assertions.assertThat(PGPHelpers.executeToFetchStatusInPaymentDetails(subsId, orderId)).isEqualTo("ACTIVE");
    }

    @Parameters({"theme"})
    @Test(description = "To verify renewal failure if merchant initiate the request after the grace period.")
    public void PGP_205_RenewalRequestAfterGracePeriod(@Optional("enhancedweb") String theme) throws Exception {
        /*Subs*/
        MerchantType merchantType = MerchantType.Subscription_PGOnly;
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.SubscriptionCC_DC(merchantType, theme)
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
        executeRenewalAndFetchOrderId(merchantType, subsId, orderDTO.getTXN_AMOUNT(), "Payment Request date is not as per the subscription contract");


    }

    @Parameters({"theme"})
    @Test(description = "merchant initiate the transaction after the end of subscription contract")
    public void PGP_208_RenewalRequestAfterContractExpiration(@Optional("enhancedweb") String theme) throws Exception {

        MerchantType merchantType = MerchantType.Subscription_PGOnly;
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.SubscriptionCC_DC(merchantType, theme)
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
        executeRenewalAndFetchOrderId(merchantType, subsId, orderDTO.getTXN_AMOUNT(), "The subscription has already expired");

    }

    @Parameters({"theme"})
    @Test(description = "To verify failed renew when save card is expired")
    public void verifyFailedRenew_WithExpiredCardDetail(@Optional("enhancedweb") String theme) throws Exception {
        MerchantType merchantType = MerchantType.Subscription_PGOnly;
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.SubscriptionCC_DC(merchantType, theme)
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
        executeRenewalAndFetchOrderId(merchantType, subsId, orderDTO.getTXN_AMOUNT(), "Invalid SavedCardID");
    }


}
