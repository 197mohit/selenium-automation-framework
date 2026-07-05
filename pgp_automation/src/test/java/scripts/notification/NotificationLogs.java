package scripts.notification;

import com.paytm.api.TxnStatus;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.dto.PaymentDTO;
import com.paytm.pages.CheckoutPage;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import org.testng.annotations.Optional;
import com.paytm.utils.merchant.util.exception.pgpException.PGPException;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.appconstants.Constants;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.ResponsePage;
import java.util.Date;
import static com.paytm.apphelpers.LogsValidationHelper.getLogsOnServer;
import org.assertj.core.api.Assertions;
import com.paytm.ServerConfigProvider;


@Owner("Shubham Soni")
@Feature("PGP-37195")
public class NotificationLogs extends PGPBaseTest {

    private final CheckoutPage checkoutPage = new CheckoutPage();
    @Parameters({"theme"})
    @Test(description = "Verify pgproxy and NQH events logs for successful CC transaction")
    public void successfulPGOnlyCCEvents(@Optional("enhancedweb_revamp") String theme) throws PGPException, InterruptedException {
        //FF4J flag PAYMENT_NOTIFY_NEW_PRODUCER is on
        //FF4J flag NON_TRANS_KAFKA_NEW_PRODUCER_FLAG is on
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PGOnly, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber("4893771000362085");
        cashierPage.payBy(Constants.PayMode.CC,paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateCheckSum(Constants.MerchantType.PGOnly.getKey())
                .validateResponsePageParameters()
                .assertAll();
        String orderId = responsePage.textOrderID().getText();
        String txnId = responsePage.textTxnID().getText();
        String grepcmd = "grep \"" + orderId + "\" /paytm/logs/events.log";
        String pgproxyNotificationEventsLogs = getLogsOnServer(ServerConfigProvider.SERVICE.PG_PROXY_NOTIFICATION, grepcmd);
        //Assertions.assertThat(pgproxyNotificationEventsLogs).contains("INFO  EVENT_LOGGER.(): {} - PUSH_STATUS = KAFKA_PRODUCE_SUCESS , messageId = [(ACQURING_ORDER_PAYMENT_NOTIFY_CC)_(ACQURING_ORDER_PAYMENT_NOTIFY_CC");
        Assertions.assertThat(pgproxyNotificationEventsLogs).contains("INFO  EVENT_LOGGER.(): {} - PUSH_STATUS = KAFKA_PRODUCE_SUCESS , messageId = [(ACQURING_ORDER_PAYMENT_NOTIFY_CC_DC)_(ACQURING_ORDER_PAYMENT_NOTIFY_CC_DC");
        String grepcmd1 = "grep \"" + orderId + "\" /paytm/logs/pgproxy-notification.log";
        String pgproxyNotificationLogs = getLogsOnServer(ServerConfigProvider.SERVICE.PG_PROXY_NOTIFICATION, grepcmd1);
        //Assertions.assertThat(pgproxyNotificationLogs).contains("Producing message in Kafka using new producer with UUID: [(ACQURING_ORDER_PAYMENT_NOTIFY_CC)_(ACQURING_ORDER_PAYMENT_NOTIFY_CC)");
        Assertions.assertThat(pgproxyNotificationLogs).contains("Producing message in Kafka using new producer with UUID: [(ACQURING_ORDER_PAYMENT_NOTIFY_CC_DC)_(ACQURING_ORDER_PAYMENT_NOTIFY_CC_DC)");
        String grepcmd2 = "grep \"" + orderId + "\" /paytm/logs/events.log";
        String NQHEventsLogs = getLogsOnServer(ServerConfigProvider.SERVICE.NOTIFICATION_QUEUE_HANDLER, grepcmd2);
        Assertions.assertThat(NQHEventsLogs).contains("(CONSUME_STATUS = KAFKA_CONSUME_SUCCESS , messageId = [(ACQURING_ORDER_PAYMENT_NOTIFY_CC_DC)_(ACQURING_ORDER_PAYMENT_NOTIFY_CC_DC)_");
        String grepcmd3 = "grep \"" + txnId + "\" /paytm/logs/notificationQueueHandler.log";
        String NQHLogs = getLogsOnServer(ServerConfigProvider.SERVICE.NOTIFICATION_QUEUE_HANDLER, grepcmd3);
        Assertions.assertThat(NQHLogs).contains("producing payload in kafka topic using new thirdparty kafka producer : PAYMENT_NOTIFY with messageId : [PAYMENT_NOTIFY");
        Assertions.assertThat(NQHLogs).contains("Successfully sent message in topic : PAYMENT_NOTIFY in partition");
        Assertions.assertThat(NQHLogs).contains("Producing paymentNotify payload in topic PAYMENT_NOTIFY, payload ");
    }
    @Owner("Shubham Soni")
    @Feature("PGP-36970")
    @Parameters({"theme"})
    @Test(description = "Verify peon and sms pushed to low priority topic for failed Transaction.")
    public void verifyLowPriorityOfFailedPGOnlyTxn(@Optional("enhancedwap_revamp") String theme) throws PGPException, InterruptedException {
        //FF4J flag ORDER_CLOSE_LOW_PRIORITY_FLAG -ON
        //ORDER_CLOSE_LOW_PRIORITY_EXCLUDED_MERCHANT_FLAG -OFF
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.REFUND_HYBRID, theme)
                .setTXN_AMOUNT("99.98")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateFailureResponse(Constants.ResponseCode.BANK_TXN_FAILURE.getRespCode(), Constants.ResponseCode.BANK_TXN_FAILURE.getRespMsg())
                .AssertAll();

        Test:
        {
            String grepcmd1 = "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/notificationQueueHandler.log ";
            String NQHLogs = getLogsOnServer(ServerConfigProvider.SERVICE.NOTIFICATION_QUEUE_HANDLER, grepcmd1);
            Assertions.assertThat(NQHLogs).contains("Producing message in Kafka with UUID: [(PEON_CG_NOTIFY_LOW_PRIORITY");
            Assertions.assertThat(NQHLogs).contains("Producing message in Kafka with UUID: [(LOW_PRIORITY_SMS)_(LOW_PRIORITY_SMS)");

            String grepcmd = "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/communicationGateway.log ";
            String CGLogs = getLogsOnServer(ServerConfigProvider.SERVICE.COMMUNICATION_GATEWAY, grepcmd);
            Assertions.assertThat(CGLogs).contains("Inside LOW_PRIORITY_SMS message queue :");
            Assertions.assertThat(CGLogs).contains("Inside peonConsumer , Queue PEON_CG_NOTIFY_LOW_PRIORITY :");
            Assertions.assertThat(CGLogs).contains("Peon Sent successfully to MID");
        }

    }


}
