package scripts.notification;

import com.paytm.api.TxnStatus;
import com.paytm.api.notification.*;
import com.paytm.api.notification.directNQHNotify.SubsCloseNotifyWOPayment;
import com.paytm.appconstants.Constants;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.pages.CheckoutPage;
import com.paytm.dto.PaymentDTO;
import com.paytm.pages.*;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.Date;
import static com.paytm.appconstants.Constants.Owner.AKSHAT;
import static com.paytm.appconstants.Constants.Owner.POOJA;
import static com.paytm.apphelpers.LogsValidationHelper.verifyLogsOnPod;

import com.paytm.apphelpers.LogsValidationHelper;
import com.paytm.apphelpers.PG2LogsValidationHelper;

public class AcquiringOrderCloseNotify extends PGPBaseTest {

    private final CheckoutPage checkoutPage = new CheckoutPage();

    @Feature("PGP-48380")
    @Owner(AKSHAT)
    @Parameters({"theme"})
    @Test(description = "Verify successful acquiringOrderCloseNotify generated for failed CC transactions ")
    public void acquiringOrderCloseNotify_CC(@Optional("enhancedweb_revamp") String theme) throws Exception {

        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.NATIVE_HYBRID, theme).setTXN_AMOUNT("99.95").build();

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("CC")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("HDFC")
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_FAILURE")
                .validateTxnType("SALE")
                .validateGatewayName("HDFC")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateTxnDate(new Date())
                .AssertAll();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.pgproxy_notification,orderDTO.getORDER_ID());
        Assertions.assertThat(logs).contains("NS_T_ACQUIRING_CLOSE_ORDER__NOTIFY");
        Assertions.assertThat(logs).contains("\"resultCodeId\":\"00000000\"");
        Assertions.assertThat(logs).contains("\"resultCode\":\"SUCCESS\"");
    }

    @Feature("PGP-48380")
    @Owner(AKSHAT)
    @Parameters({"theme"})
    @Test(description = "Verify successful acquiringOrderCloseNotify generated for failed DC transactions ")
    public void acquiringOrderCloseNotify_DC(@Optional("enhancedweb_revamp") String theme) throws Exception {

        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.NATIVE_HYBRID, theme).setTXN_AMOUNT("99.95").build();

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.DC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("DC")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("HDFC")
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_FAILURE")
                .validateTxnType("SALE")
                .validateGatewayName("HDFC")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateTxnDate(new Date())
                .AssertAll();

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.pgproxy_notification,orderDTO.getORDER_ID());
        Assertions.assertThat(logs).contains("NS_T_ACQUIRING_CLOSE_ORDER__NOTIFY");
        Assertions.assertThat(logs).contains("\"resultCodeId\":\"00000000\"");
        Assertions.assertThat(logs).contains("\"resultCode\":\"SUCCESS\"");
    }

    @Feature("PGP-48380")
    @Owner(AKSHAT)
    @Parameters({"theme"})
    @Test(description = "Verify successful acquiringOrderCloseNotify generated for failed UPI transactions ")
    public void acquiringOrderCloseNotify_UPI(@Optional("enhancedweb_revamp") String theme) throws Exception {

        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PGOnly, theme).setTXN_AMOUNT("99.99").build();

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("UPI")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("PPBLC")
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_FAILURE")
                .validateTxnType("SALE")
                .validateGatewayName("PPBLC")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("UPI")
                .validateTxnDate(new Date())
                .AssertAll();

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.pgproxy_notification,orderDTO.getORDER_ID());
        Assertions.assertThat(logs).contains("NS_T_ACQUIRING_CLOSE_ORDER__NOTIFY");
        Assertions.assertThat(logs).contains("\"resultCodeId\":\"00000000\"");
        Assertions.assertThat(logs).contains("\"resultCode\":\"SUCCESS\"");
    }

    @Feature("PGP-48380")
    @Owner(AKSHAT)
    @Parameters({"theme"})
    @Test(description = "Verify successful acquiringOrderCloseNotify generated for failed NB transactions ")
    public void acquiringOrderCloseNotify_NB(@Optional("enhancedweb_revamp") String theme) throws Exception {

        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.NATIVE_HYBRID, theme).setTXN_AMOUNT("99.99").build();

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.NB);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("NB")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("ICICI")
                .validateBankName("ICICI")
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_FAILURE")
                .validateTxnType("SALE")
                .validateGatewayName("ICICI")
                .validateBankName("ICICI")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("NB")
                .validateTxnDate(new Date())
                .AssertAll();

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.pgproxy_notification,orderDTO.getORDER_ID());
        Assertions.assertThat(logs).contains("NS_T_ACQUIRING_CLOSE_ORDER__NOTIFY");
        Assertions.assertThat(logs).contains("\"resultCodeId\":\"00000000\"");
        Assertions.assertThat(logs).contains("\"resultCode\":\"SUCCESS\"");
    }

    @Feature("PGP-48380")
    @Owner(AKSHAT)
    @Parameters({"theme"})
    @Test(description = "Verify successful acquiringOrderCloseNotify generated for failed PPBL_NB transactions ")
    public void acquiringOrderCloseNotify_PPBL_NB(@Optional("enhancedweb_revamp") String theme) throws Exception {

        User user = userManager.getForWrite(Label.PPBL);
        OrderDTO orderDTO = new OrderFactory.Hybrid(Constants.MerchantType.PPBLYONLY, theme, user).setTXN_AMOUNT("60.10").build();

        checkoutPage.createOrder(orderDTO);
        String orderId = orderDTO.getORDER_ID();
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.checkboxPPBL().check();
        cashierPage.textBoxPPBLPassCode().clearAndType(new PaymentDTO().getPasscode());
        cashierPage.buttonPpblSumbit().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("NB")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("PPBL")
                .validateBankName("PPBL")
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        responsePage.validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("NB")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("PPBL")
                .validateBankName("PPBL")
                .assertAll();

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.pgproxy_notification,orderDTO.getORDER_ID());
        Assertions.assertThat(logs).contains("oldPG.acquiring.order.closeNotify");
        Assertions.assertThat(logs).contains("\"resultCodeId\":\"00000000\"");
        Assertions.assertThat(logs).contains("\"resultCode\":\"SUCCESS\"");
    }


    @Feature("PGP-53761")
    @Owner(POOJA)
    @Test(description = "Verify that errorCode=ORDER_CLOSE is sent in SubscriptionCloseOrderInfo data from NQH to subs when paymentView is not received in closeNotify:\n" +
            "NATIVE_SUBSCRIPTION")
    public void errorCodeValueAsORDER_CLOSEWhenPaymentViewISNotPresent() throws Exception {
        String mid = Constants.MerchantType.Subscription_PGOnly.getId();
        String orderId = "PARCEL541264";
        SubsCloseNotifyWOPayment subsCloseNotifyWOPayment = new SubsCloseNotifyWOPayment();
        JsonPath subsCloseNotifyWOPaymentResponse = subsCloseNotifyWOPayment
                .setMID(mid)
                .execute().jsonPath();

        String nqhLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, orderId, "subscriptionConsumer");
        String PGPID= nqhLogs.substring(nqhLogs.indexOf("PGPID")+6, nqhLogs.indexOf("}"));

        String nqhLogs2=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, PGPID, "SubscriptionCloseOrderInfo");
        Assertions.assertThat(nqhLogs2).contains("errorCode=ORDER_CLOSE");
        Assertions.assertThat(nqhLogs2).contains("requestType=NATIVE_SUBSCRIPTION");
    }

    @Feature("PGP-53761")
    @Owner(POOJA)
    @Test(description = "Verify that errorCode=ORDER_CLOSE is sent in SUBSCRIPTION_ORDER_CLOSE_NOTIFY kafka topic from NQH\n" +
            "NATIVE_SUBSCRIPTION")
    public void errorCodePushToTopicSUBSCRIPTION_ORDER_CLOSE_NOTIFY() throws Exception {
        String mid = Constants.MerchantType.Subscription_PGOnly.getId();
        String orderId = "PARCEL541264";
        SubsCloseNotifyWOPayment subsCloseNotifyWOPayment = new SubsCloseNotifyWOPayment();
        JsonPath subsCloseNotifyWOPaymentResponse = subsCloseNotifyWOPayment
                .setMID(mid)
                .execute().jsonPath();

        String nqhLogs = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, orderId, "subscriptionConsumer");
        String PGPID = nqhLogs.substring(nqhLogs.indexOf("PGPID") + 6, nqhLogs.indexOf("}"));

        String nqhLogs2 = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, PGPID  );
        Assertions.assertThat(nqhLogs2).contains("subscriptionConsumer data topic : SUBSCRIPTION_ORDER_CLOSE_NOTIFY");

    }

}
