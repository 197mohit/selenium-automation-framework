package scripts.notification;

import com.paytm.ServerConfigProvider;
import com.paytm.api.PaymentService;
import com.paytm.api.ProcessTransactionV1;
import com.paytm.api.TxnStatus;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.api.theia.FetchQRPaymentDetails;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.*;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchQRPaymentDetailsDTO.FetchQRPaymentDetailsDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.OrderAdditionalInfo;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.processTransactionV1.ExtendInfo;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.dto.processTransactionV1.response.ProcessTxnV1Response;
import com.paytm.framework.utils.DatabaseUtil;
import com.paytm.pages.*;
import com.paytm.utils.merchant.util.PayMethodType;
import com.paytm.utils.merchant.util.exception.pgpException.PGPException;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;


import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.paytm.apphelpers.LogsValidationHelper.getLogsOnServer;
import static com.paytm.apphelpers.LogsValidationHelper.verifyLogsOnPod;


@Owner(Constants.Owner.TAMANA_TATHAN)
public class NotificationTest extends PGPBaseTest {

    private final CheckoutPage checkoutPage = new CheckoutPage();

    @Parameters({"theme"})
    @Feature("PGP-36261")
    @Owner(Constants.Owner.TAMANA_TATHAN)
    @Test(description = "Validating Payload in Pushed in NQH for paymode CC")
    public void pushPaymentNotifyWithPaymodeCC(@Optional("enhancedweb_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.PGOnly;
        OrderDTO orderDTO = new OrderFactory.PGOnly(merchant, theme)
                .setCHANNEL_ID("WEB")
                .setTXN_AMOUNT("99.98")
                .setSUBS_PAYMENT_MODE("CC")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_FAILURE");
        responsePage.validatePaymentMode("CC");
        responsePage.validateRespCode("227");
        String orderId = orderDTO.getORDER_ID();
        String commandForNewTopic = "grep -E '" +orderId+ ".*PAYMENT_NOTIFY_DWH' /paytm/logs/notificationQueueHandler.log";
        String logsForNewTopic = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.NOTIFICATION_QUEUE_HANDLER, commandForNewTopic);
        Assert.assertTrue(logsForNewTopic.contains("in kafka PAYMENT_NOTIFY_DWH"), "Payment Notify Dwh Topic is present as 'PUSH_TRANSACTION_FAIL_NOTIFY_DWH' flag is on");
        Assert.assertTrue(logsForNewTopic.contains("Successfully pushed payment DWH payload"), "Payment Notify Dwh Topic is present as 'PUSH_TRANSACTION_FAIL_NOTIFY_DWH' flag is on");
        String commandForOldTopic = "grep '" +orderId+ "' -e 'ORDER_CLOSE_NOTIFY_DWH' /paytm/logs/notificationQueueHandler.log";
        String logsForOldTopic = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.NOTIFICATION_QUEUE_HANDLER, commandForOldTopic);
        Assert.assertTrue(logsForOldTopic.contains("Pushing ORDER_CLOSE_NOTIFY_DWH payload in KAFKA topic ORDER_CLOSE_NOTIFY_DWH"), "Order Close Notify Topic DWH is present as 'PUSH_TRANSACTION_FAIL_NOTIFY_DWH' flag is on");
    }

    @Parameters({"theme"})
    @Feature("PGP-36261")
    @Owner(Constants.Owner.TAMANA_TATHAN)
    @Test(description = "Validating Payload in Pushed in NQH for paymode UPI")
    public void pushPaymentNotifyWithPaymodeUPI(@Optional("enhancedweb_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.PGOnly;
        OrderDTO orderDTO = new OrderFactory.PGOnly(merchant, theme)
                .setCHANNEL_ID("WEB")
                .setTXN_AMOUNT("99.99")
                .setSUBS_PAYMENT_MODE("UPI")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(Constants.PayMode.UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_FAILURE");
        responsePage.validatePaymentMode("CC");
        responsePage.validateRespCode("227");
        String orderId = orderDTO.getORDER_ID();
        String commandForNewTopic = "grep -E '" +orderId+ ".*PAYMENT_NOTIFY_DWH' /paytm/logs/notificationQueueHandler.log";
        String logsForNewTopic = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.NOTIFICATION_QUEUE_HANDLER, commandForNewTopic);
        Assert.assertTrue(logsForNewTopic.contains("in kafka PAYMENT_NOTIFY_DWH"), "Payment Notify Dwh Topic is present as 'PUSH_TRANSACTION_FAIL_NOTIFY_DWH' flag is on");
        Assert.assertTrue(logsForNewTopic.contains("Successfully pushed payment DWH payload"), "Payment Notify Dwh Topic is present as 'PUSH_TRANSACTION_FAIL_NOTIFY_DWH' flag is on");
        String commandForOldTopic = "grep '" +orderId+ "' -e 'ORDER_CLOSE_NOTIFY_DWH' /paytm/logs/notificationQueueHandler.log";
        String logsForOldTopic = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.NOTIFICATION_QUEUE_HANDLER, commandForOldTopic);
        Assert.assertTrue(logsForOldTopic.contains("Pushing ORDER_CLOSE_NOTIFY_DWH payload in KAFKA topic ORDER_CLOSE_NOTIFY_DWH"), "Order Close Notify Topic DWH is present as 'PUSH_TRANSACTION_FAIL_NOTIFY_DWH' flag is on");
    }

    @Parameters({"theme"})
    @Feature("PGP-36261")
    @Owner(Constants.Owner.TAMANA_TATHAN)
    @Test(description = "Validating Payload in Pushed in NQH for paymode PPBL")
    public void pushPaymentNotifyWithPaymodePPBL(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.PGOnly;
        OrderDTO orderDTO = new OrderFactory.PGOnly(merchant, theme)
                .setCHANNEL_ID("WEB")
                .setTXN_AMOUNT("60.10")
                .setSUBS_PAYMENT_MODE("PPBL")
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(Constants.PayMode.PPBL);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_FAILURE");
        responsePage.validatePaymentMode("NB");
        responsePage.validateRespCode("227");
        String orderId = orderDTO.getORDER_ID();
        String commandForNewTopic = "grep -E '" +orderId+ ".*PAYMENT_NOTIFY_DWH' /paytm/logs/notificationQueueHandler.log";
        String logsForNewTopic = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.NOTIFICATION_QUEUE_HANDLER, commandForNewTopic);
        Assert.assertTrue(logsForNewTopic.contains("in kafka PAYMENT_NOTIFY_DWH"), "Payment Notify Dwh Topic is present as 'PUSH_TRANSACTION_FAIL_NOTIFY_DWH' flag is on");
        Assert.assertTrue(logsForNewTopic.contains("Successfully pushed payment DWH payload"), "Payment Notify Dwh Topic is present as 'PUSH_TRANSACTION_FAIL_NOTIFY_DWH' flag is on");
        String commandForOldTopic = "grep '" +orderId+ "' -e 'ORDER_CLOSE_NOTIFY_DWH' /paytm/logs/notificationQueueHandler.log";
        String logsForOldTopic = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.NOTIFICATION_QUEUE_HANDLER, commandForOldTopic);
        Assert.assertTrue(logsForOldTopic.contains("Pushing ORDER_CLOSE_NOTIFY_DWH payload in KAFKA topic ORDER_CLOSE_NOTIFY_DWH"), "Order Close Notify DWH Topic is present as 'PUSH_TRANSACTION_FAIL_NOTIFY_DWH' flag is on");
    }

    @Parameters({"theme"})
    @Feature("PGP-36261")
    @Owner(Constants.Owner.TAMANA_TATHAN)
    @Test(description = "Validating Payload in Pushed in NQH for Add n pay scenario")
    public void pushPaymentNotifyWithAddNPay(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.PGOnly;
        WalletHelpers.setZeroBalance(user);
        OrderDTO orderDTO = new OrderFactory.PGOnly(merchant, theme)
                .setCHANNEL_ID("WEB")
                .setTXN_AMOUNT("99.98")
                .setSUBS_PAYMENT_MODE("PPI")
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(Constants.PayMode.DC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_FAILURE");
        responsePage.validatePaymentMode("DC");
        responsePage.validateRespCode("227");
        String orderId = orderDTO.getORDER_ID();
        String commandForNewTopic = "grep -E '" +orderId+ ".*PAYMENT_NOTIFY_DWH' /paytm/logs/notificationQueueHandler.log";
        String logsForNewTopic = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.NOTIFICATION_QUEUE_HANDLER, commandForNewTopic);
        Assert.assertTrue(logsForNewTopic.contains("in kafka PAYMENT_NOTIFY_DWH"), "Payment Notify Dwh Topic is present as 'PUSH_TRANSACTION_FAIL_NOTIFY_DWH' flag is on");
        Assert.assertTrue(logsForNewTopic.contains("Successfully pushed payment DWH payload"), "Payment Notify Dwh Topic is present as 'PUSH_TRANSACTION_FAIL_NOTIFY_DWH' flag is on");
        String commandForOldTopic = "grep '" +orderId+ "' -e 'ORDER_CLOSE_NOTIFY_DWH' /paytm/logs/notificationQueueHandler.log";
        String logsForOldTopic = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.NOTIFICATION_QUEUE_HANDLER, commandForOldTopic);
        Assert.assertTrue(logsForOldTopic.contains("Pushing ORDER_CLOSE_NOTIFY_DWH payload in KAFKA topic ORDER_CLOSE_NOTIFY_DWH"), "Order Close Notify DWH Topic is present as 'PUSH_TRANSACTION_FAIL_NOTIFY_DWH' flag is on");
    }

    @Feature("PGP-36262")
    @Owner(Constants.Owner.TAMANA_TATHAN)
    @Test(description = "Validating Close notify Payload is Pushed in NQH for paymode CC")
    public void pushCloseNotifyWithPaymodeCC() throws Exception {
        pushOrderCloseNotify(Constants.Theme.ENHANCEDWEB_REVAMP, false, "CC", "99.98", Constants.PayMode.CC, "CC");
    }

    @Feature("PGP-36262")
    @Owner(Constants.Owner.TAMANA_TATHAN)
    @Test(description = "Validating Close notify payload is Pushed in NQH for paymode UPI")
    public void pushCloseNotifyWithPaymodeUPI() throws Exception {
        pushOrderCloseNotify(Constants.Theme.ENHANCEDWEB_REVAMP, false, "UPI", "99.99", Constants.PayMode.UPI, "UPI");
    }

    @Feature("PGP-36262")
    @Owner(Constants.Owner.TAMANA_TATHAN)
    @Test(description = "Validating Close notify Payload is Pushed in NQH for paymode PPBL")
    public void pushCloseNotifyWithPaymodePPBL() throws Exception {
        pushOrderCloseNotify(Constants.Theme.ENHANCEDWEB_REVAMP, false, "PPBL", "60.10", Constants.PayMode.PPBL, "NB");
    }

    @Feature("PGP-36262")
    @Owner(Constants.Owner.TAMANA_TATHAN)
    @Test(description = "Validating Close notify Payload in Pushed in NQH for Add n pay scenario")
    public void pushCloseNotifyWithAddnPay() throws Exception {
        pushOrderCloseNotify(Constants.Theme.ENHANCEDWEB_REVAMP, true, "PPI", "101.98", Constants.PayMode.DC, "PPI");
    }

    public void pushOrderCloseNotify(String theme, Boolean ifAddnPay, String paymode, String amount, Constants.PayMode payMode, String responseMode) throws Exception {
        Constants.MerchantType merchant;
        OrderDTO orderDTO;
        if(ifAddnPay){
            merchant = Constants.MerchantType.AddnPay;
            User user = userManager.getForWrite(Label.BASIC);
            WalletHelpers.modifyBalance(user, 2.00);
            orderDTO = new OrderFactory.PGOnly(merchant, theme)
                    .setCHANNEL_ID("WEB")
                    .setTXN_AMOUNT(amount)
                    .setSUBS_PAYMENT_MODE(paymode)
                    .setSSO_TOKEN(user.ssoToken())
                    .build();
            checkoutPage.createOrder(orderDTO);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.waitUntilLoads();
            cashierPage.payBy(payMode);
        }else{
            merchant = Constants.MerchantType.PGOnly;
            User user = userManager.getForRead(Label.BASIC);
            orderDTO = new OrderFactory.PGOnly(merchant, theme)
                    .setCHANNEL_ID("WEB")
                    .setTXN_AMOUNT(amount)
                    .setSUBS_PAYMENT_MODE(paymode)
                    .setSSO_TOKEN(user.ssoToken())
                    .build();
            checkoutPage.createOrder(orderDTO);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.waitUntilLoads();
            cashierPage.payBy(payMode);
        }
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_FAILURE");
        responsePage.validatePaymentMode(responseMode);
        responsePage.validateRespCode("227");
        String orderId = orderDTO.getORDER_ID();

        String commandForNewTopic = "awk '/"+orderId+"/  && /ORDER_CLOSE_NOTIFY_DWH_NEW/' /paytm/logs/notificationQueueHandler.log";
        String logsForNewTopic = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.NOTIFICATION_QUEUE_HANDLER, commandForNewTopic);
        Assert.assertTrue(logsForNewTopic.contains("Successfully pushed order close DWH payload"), "Close Notify Dwh Topic is present as 'PUSH_ORDER_CLOSE_NOTIFY_DWH_NEW' flag is on");
        Assert.assertTrue(logsForNewTopic.contains("in kafka topic ORDER_CLOSE_NOTIFY_DWH_NEW"), "Close Notify Dwh Topic is present as 'PUSH_ORDER_CLOSE_NOTIFY_DWH_NEW' flag is on");

        String commandForOldTopic = "awk '/" +orderId+ "/  && /ORDER_CLOSE_NOTIFY_DWH/' /paytm/logs/notificationQueueHandler.log";
        String topicCountCommand = "awk '/" +orderId+ "/  && /ORDER_CLOSE_NOTIFY_DWH/' /paytm/logs/notificationQueueHandler.log | grep -c 'Pushing ORDER_CLOSE_NOTIFY_DWH payload in KAFKA topic ORDER_CLOSE_NOTIFY_DWH'";
        String logsForOldTopic = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.NOTIFICATION_QUEUE_HANDLER, commandForOldTopic);
        String topicCount = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.NOTIFICATION_QUEUE_HANDLER, topicCountCommand);
        logsForOldTopic.contains("Pushing ORDER_CLOSE_NOTIFY_DWH payload in KAFKA topic ORDER_CLOSE_NOTIFY_DWH");
        Assert.assertTrue(topicCount.equalsIgnoreCase("2"), "2 loggers are seen for ORDER_CLOSE_NOTIFY_DWH");
        Assert.assertTrue(logsForOldTopic.contains("Pushing ORDER_CLOSE_NOTIFY_DWH payload in KAFKA topic ORDER_CLOSE_NOTIFY_DWH"), "Order Close Notify DWH Topic is present as 'PUSH_ORDER_CLOSE_NOTIFY_DWH_NEW' flag is on");
    }

    @Feature("PGP-37474")
    @Owner(Constants.Owner.TAMANA_TATHAN)
    @Test(description = "Validating Payload is not getting Pushed in NQH and peon , sms and email is generated for CC")
    public void validateTopicForRTDD_CC() throws Exception{
        validateTopicForRTDDdependency(Constants.Theme.ENHANCEDWEB_REVAMP, false, "CC", "99.98", Constants.PayMode.CC, "CC");
    }

    @Feature("PGP-37474")
    @Owner(Constants.Owner.TAMANA_TATHAN)
    @Test(description = "Validating Payload is not getting Pushed in NQH and peon , sms and email is generated for UPI")
    public void validateTopicForRTDD_UPI() throws Exception {
        validateTopicForRTDDdependency(Constants.Theme.ENHANCEDWEB_REVAMP, false, "UPI", "99.99", Constants.PayMode.UPI, "UPI");
    }

    @Feature("PGP-37474")
    @Owner(Constants.Owner.TAMANA_TATHAN)
    @Test(description = "Validating Payload is not getting Pushed in NQH and peon , sms and email is generated for PPBL")
    public void validateTopicForRTDD_PPBL() throws Exception{
        validateTopicForRTDDdependency(Constants.Theme.ENHANCEDWEB_REVAMP, false, "PPBL", "60.10", Constants.PayMode.PPBL, "NB");
    }

    @Feature("PGP-37474")
    @Owner(Constants.Owner.TAMANA_TATHAN)
    @Test(description = "Validating Payload is not getting Pushed in NQH and peon , sms and email is generated for Add n pay scenario")
    public void validateTopicForRTDD_AddNPay() throws Exception{
        validateTopicForRTDDdependency(Constants.Theme.ENHANCEDWEB_REVAMP, true, "PPI", "101.98", Constants.PayMode.DC, "PPI");
    }

    public void validateTopicForRTDDdependency(String theme, Boolean ifAddnPay, String paymode, String amount, Constants.PayMode payMode, String responseMode) throws Exception {
        Constants.MerchantType merchant;
        OrderDTO orderDTO;
        if(ifAddnPay){
            merchant = Constants.MerchantType.Notification_RTDD;
            User user = userManager.getForWrite(Label.BASIC);
            WalletHelpers.modifyBalance(user, 2.00);
            orderDTO = new OrderFactory.PGOnly(merchant, theme)
                    .setCHANNEL_ID("WEB")
                    .setTXN_AMOUNT(amount)
                    .setSUBS_PAYMENT_MODE(paymode)
                    .setSSO_TOKEN(user.ssoToken())
                    .build();
            checkoutPage.createOrder(orderDTO);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.waitUntilLoads();
            cashierPage.payBy(payMode);
        }else{
            merchant = Constants.MerchantType.Notification_RTDD;
            User user = userManager.getForRead(Label.BASIC);
            orderDTO = new OrderFactory.PGOnly(merchant, theme)
                    .setCHANNEL_ID("WEB")
                    .setTXN_AMOUNT(amount)
                    .setSUBS_PAYMENT_MODE(paymode)
                    .setSSO_TOKEN(user.ssoToken())
                    .build();
            checkoutPage.createOrder(orderDTO);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.waitUntilLoads();
           // cashierPage.checkBoxPPI().unCheck();
            cashierPage.payBy(payMode);
        }
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_FAILURE");
        responsePage.validatePaymentMode(responseMode);
        responsePage.validateRespCode("227");
        String orderId = orderDTO.getORDER_ID();

        String command1 = "awk '/" +orderId+ "/  && /ORDER_CLOSE_NOTIFY/' /paytm/logs/notificationQueueHandler.log | grep -c 'Pushing ORDER_CLOSE_NOTIFY payload in KAFKA topic ORDER_CLOSE_NOTIFY'";
        String command2 = "awk '/" +orderId+ "/  && /ORDER_CLOSE_NOTIFY_DWH/' /paytm/logs/notificationQueueHandler.log | grep -c 'Pushing ORDER_CLOSE_NOTIFY_DWH payload in KAFKA topic ORDER_CLOSE_NOTIFY_DWH'";
        String topicCount1 = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.NOTIFICATION_QUEUE_HANDLER, command1);
        String topicCount2 = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.NOTIFICATION_QUEUE_HANDLER, command2);

        Assert.assertTrue(topicCount1.equalsIgnoreCase("0"), "No Loggers has been printed for topic ORDER_CLOSE_NOTIFY");
        Assert.assertTrue(topicCount2.equalsIgnoreCase("1"), "One Loggers has been printed for topic ORDER_CLOSE_NOTIFY_DWH");

        String command = "grep '" +orderId+ "' /paytm/logs/communicationGateway.log ";
        String logs = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.COMMUNICATION_GATEWAY, command);
        Assert.assertTrue(logs.contains("Peon Sent successfully to MID: "+merchant.getId()+" for OrderId:"+orderId+""));
        Assert.assertTrue(logs.contains("Inside HIGH_PRIORITY_SMS message queue"));
        Assert.assertTrue(logs.contains("emailTo : [abc@gmail.com] , subject : Payment against " +orderId+ " has failed"));
    }

    @Parameters({"theme"})
    @Feature("PGP-43236")
    @Owner(Constants.Owner.VISHNU_SHEKAR)
    @Test(description = "Validating acquiring close Notify is Pushed in PGProxyNotification for CC scenario")
    public void acquiringCloseNotifyNotifyWithCC(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.PGOnly;
        OrderDTO orderDTO = new OrderFactory.PGOnly(merchant, theme)
                .setCHANNEL_ID("WEB")
                .setTXN_AMOUNT("99.98")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_FAILURE");
        responsePage.validatePaymentMode("CC");
        responsePage.validateRespCode("227");

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.pgproxy_notification,orderDTO.getORDER_ID());
        Assertions.assertThat(logs).contains("NS_T_ACQUIRING_CLOSE_ORDER__NOTIFY");
        Assertions.assertThat(logs).contains("\"resultCodeId\":\"00000000\"");
        Assertions.assertThat(logs).contains("\"resultCode\":\"SUCCESS\"");

    }

    @Parameters({"theme"})
    @Feature("PGP-43236")
    @Owner(Constants.Owner.VISHNU_SHEKAR)
    @Test(description = "Validating acquiring close Notify is Pushed in PGProxyNotification for DC scenario")
    public void acquiringCloseNotifyNotifyWithDC(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.PGOnly;
        OrderDTO orderDTO = new OrderFactory.PGOnly(merchant, theme)
                .setCHANNEL_ID("WEB")
                .setTXN_AMOUNT("99.98")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(Constants.PayMode.DC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_FAILURE");
        responsePage.validatePaymentMode("DC");
        responsePage.validateRespCode("227");

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.pgproxy_notification,orderDTO.getORDER_ID());
        Assertions.assertThat(logs).contains("NS_T_ACQUIRING_CLOSE_ORDER__NOTIFY");
        Assertions.assertThat(logs).contains("\"resultCodeId\":\"00000000\"");
        Assertions.assertThat(logs).contains("\"resultCode\":\"SUCCESS\"");

    }

    @Parameters({"theme"})
    @Feature("PGP-43236")
    @Owner(Constants.Owner.VISHNU_SHEKAR)
    @Test(description = "Validating acquiring close Notify is Pushed in PGProxyNotification for NB scenario")
    public void acquiringCloseNotifyNotifyWithNB(@Optional("enhancedweb_revamp") String theme) throws Exception {

        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.setZeroBalance(user);
        Constants.MerchantType merchant = Constants.MerchantType.PPBLYONLY;
        OrderDTO orderDTO = new OrderFactory.PGOnly(merchant, theme)
                .setCHANNEL_ID("WEB")
                .setTXN_AMOUNT("99.99")
                .setPaymentMode("NB")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(Constants.PayMode.NB);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_FAILURE");
        responsePage.validatePaymentMode("NB");
        responsePage.validateRespCode("227");

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.pgproxy_notification,orderDTO.getORDER_ID());
        Assertions.assertThat(logs).contains("NS_T_ACQUIRING_CLOSE_ORDER__NOTIFY");
        Assertions.assertThat(logs).contains("\"resultCodeId\":\"00000000\"");
        Assertions.assertThat(logs).contains("\"resultCode\":\"SUCCESS\"");
    }

    @Parameters({"theme"})
    @Feature("PGP-43236")
    @Owner(Constants.Owner.VISHNU_SHEKAR)
    @Test(description = "Validating acquiring close Notify is Pushed in PGProxyNotification for UPI scenario")
    public void acquiringCloseNotifyNotifyWithUPI(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_NATIVE_SUBS;
        OrderDTO orderDTO = new OrderFactory.PGOnly(merchant, theme)
                .setCHANNEL_ID("WEB")
                .setTXN_AMOUNT("99.99")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(Constants.PayMode.UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_FAILURE");
        responsePage.validatePaymentMode("UPI");
        responsePage.validateRespCode("227");
        String orderId = orderDTO.getORDER_ID();
     //   String grepcmd = "grep \"" + orderId + "\" /paytm/logs/pgproxy-notification.log | grep \"Type=CloseNotify} - Response :\"" ;
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.pgproxy_notification,orderDTO.getORDER_ID());
        Assertions.assertThat(logs).contains("NS_T_ACQUIRING_CLOSE_ORDER__NOTIFY");
        Assertions.assertThat(logs).contains("\"resultCodeId\":\"00000000\"");
        Assertions.assertThat(logs).contains("\"resultCode\":\"SUCCESS\"");
    }

    @Parameters({"theme"})
    @Feature("PGP-43236")
    @Owner(Constants.Owner.VISHNU_SHEKAR)
    @Test(description = "Validating acquiring close Notify is Pushed in PGProxyNotification for AddnPay scenario")
    public void acquiringCloseNotifyNotifyWithAddnPay(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.AddnPay;
        WalletHelpers.setZeroBalance(user);
        WalletHelpers.modifyBalance(user,12.00);
        OrderDTO orderDTO = new OrderFactory.PGOnly(merchant, theme)
                .setCHANNEL_ID("WEB")
                .setTXN_AMOUNT("111.98")
                .setPaymentMode("PPI")
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_FAILURE");
        responsePage.validatePaymentMode("PPI");
        responsePage.validateRespCode("227");
        String orderId = orderDTO.getORDER_ID();
        String grepcmd = "grep \"" + orderId + "\" /paytm/logs/pgproxy-notification.log | grep \"Type=CloseNotify} - Response :\"" ;
        String logs = getLogsOnServer(ServerConfigProvider.SERVICE.PG_PROXY_NOTIFICATION, grepcmd);
        Assertions.assertThat(logs).contains("\"function\":\"oldPG.acquiring.order.closeNotify\"");
        Assertions.assertThat(logs).contains("\"resultCodeId\":\"00000000\"");
        Assertions.assertThat(logs).contains("\"resultCode\":\"SUCCESS\"");
    }
    @Parameters({"theme"})
    @Feature("PGP-43236")
    @Owner(Constants.Owner.VISHNU_SHEKAR)
    @Test(description = "Validating acquiring close Notify is Pushed in PGProxyNotification for Hybrid scenario")
    public void acquiringCloseNotifyNotifyWithHybrid(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.setZeroBalance(user);
        Constants.MerchantType merchant = Constants.MerchantType.HYBRID_MID;
        WalletHelpers.setZeroBalance(user);
        WalletHelpers.modifyBalance(user,10.0);
        OrderDTO orderDTO = new OrderFactory.PGOnly(merchant, theme)
                .setCHANNEL_ID("WEB")
                .setTXN_AMOUNT("109.99")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.checkBoxPPI().check();
        cashierPage.payBy(Constants.PayMode.NB);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_FAILURE");
        responsePage.validatePaymentMode("NB");
        responsePage.validateRespCode("227");
        String orderId = orderDTO.getORDER_ID();
        String grepcmd = "grep \"" + orderId + "\"Type=CloseNotify\" /paytm/logs/pgproxy-notification.log" ;
        String logs = getLogsOnServer(ServerConfigProvider.SERVICE.PG_PROXY_NOTIFICATION, grepcmd);
        Assertions.assertThat(logs).contains("\"function\":\"oldPG.acquiring.order.closeNotify\"");
        Assertions.assertThat(logs).contains("\"resultCodeId\":\"00000000\"");
        Assertions.assertThat(logs).contains("\"resultCode\":\"SUCCESS\"");
    }
    @Parameters({"theme"})
    @Feature("PGP-43236")
    @Owner(Constants.Owner.VISHNU_SHEKAR)
    @Test(description = "Validating acquiring close Notify is Pushed in PGProxyNotification for PPI scenario")
    public void acquiringCloseNotifyNotifyWithPPI(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.setZeroBalance(user);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.WalletOnly, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT("2.00").build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()));
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        WalletHelpers.modifyBalance(user, 1.00);
        WalletHelpers.validateBalance(user, 1.00);
        cashierPage.checkBoxPPI().check();
        cashierPage.payBy(Constants.PayMode.WALLET);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_FAILURE");
        responsePage.validatePaymentMode("WALLET");
        responsePage.validateRespCode("235");
        String orderId = orderDTO.getORDER_ID();
        String grepcmd = "grep \"" + orderId + "\" /paytm/logs/pgproxy-notification.log | grep \"Type=CloseNotify} - Response :\"" ;
        String logs = getLogsOnServer(ServerConfigProvider.SERVICE.PG_PROXY_NOTIFICATION, grepcmd);
        Assertions.assertThat(logs).contains("\"function\":\"oldPG.acquiring.order.closeNotify\"");
        Assertions.assertThat(logs).contains("\"resultCodeId\":\"00000000\"");
        Assertions.assertThat(logs).contains("\"resultCode\":\"SUCCESS\"");
    }
    @Parameters({"theme"})
    @Feature("PGP-43104")
    @Test(description = "Validate payment notify sent for successful CC txn")
    public void verifyPaymentNotifyWithCC(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.PGOnly;
        OrderDTO orderDTO = new OrderFactory.PGOnly(merchant, theme)
                .setCHANNEL_ID("WEB")
                .setTXN_AMOUNT("10")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.pgproxy_notification,orderDTO.getORDER_ID());
        Assertions.assertThat(logs).contains("TP_S_1212_EC_EVENTLOG_2001");
        Assertions.assertThat(logs).contains("\"resultCodeId\":\"00000000\"");
        Assertions.assertThat(logs).contains("\"resultCode\":\"SUCCESS\"");
    }
    @Parameters({"theme"})
    @Feature("PGP-43104")
    @Test(description = "Validate payment notify sent for successful DC txn")
    public void verifyPaymentNotifyWithDC(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.PGOnly;
        OrderDTO orderDTO = new OrderFactory.PGOnly(merchant, theme)
                .setCHANNEL_ID("WEB")
                .setTXN_AMOUNT("10")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(Constants.PayMode.DC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validatePaymentMode("DC")
                .validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.pgproxy_notification,orderDTO.getORDER_ID());
        Assertions.assertThat(logs).contains("\"resultCodeId\":\"00000000\"");
        Assertions.assertThat(logs).contains("\"resultCode\":\"SUCCESS\"");
    }
    @Parameters({"theme"})
    @Feature("PGP-43104")
    @Test(description = "Validate payment notify sent for successful NB txn")
    public void verifyPaymentNotifyWithNB(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.PPBLYONLY;
        OrderDTO orderDTO = new OrderFactory.PGOnly(merchant, theme)
                .setCHANNEL_ID("WEB")
                .setTXN_AMOUNT("10")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(Constants.PayMode.NB);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validatePaymentMode("NB")
                .validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.pgproxy_notification,orderDTO.getORDER_ID());
        Assertions.assertThat(logs).contains("\"resultCodeId\":\"00000000\"");
        Assertions.assertThat(logs).contains("\"resultCode\":\"SUCCESS\"");
    }

    @Parameters({"theme"})
    @Feature("PGP-43104")
    @Test(description = "Validate payment notify sent for successful UPI txn")
    public void verifyPaymentNotifyWithUPI(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.PGOnly;
        OrderDTO orderDTO = new OrderFactory.PGOnly(merchant, theme)
                .setCHANNEL_ID("WEB")
                .setTXN_AMOUNT("10")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(Constants.PayMode.UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.pgproxy_notification,orderDTO.getORDER_ID());
        Assertions.assertThat(logs).contains("\"resultCodeId\":\"00000000\"");
        Assertions.assertThat(logs).contains("\"resultCode\":\"SUCCESS\"");
    }
    @Parameters({"theme"})
    @Feature("PGP-43104")
    @Test(description = "Validate payment notify sent for successful AddnPay txn")
    public void verifyPaymentNotifyWithAddnPay(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.AddnPay(Constants.MerchantType.PGOnly, theme)
                .setSSO_TOKEN(user.ssoToken()).build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.00);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
        String orderId = orderDTO.getORDER_ID();
        String grepcmd = "grep \"" + orderId + "\" /paytm/logs/pgproxy-notification.log | grep \"Type=PaymentNotify} - Response :\"" ;
        String logs = getLogsOnServer(ServerConfigProvider.SERVICE.PG_PROXY_NOTIFICATION, grepcmd);
        Assertions.assertThat(logs).contains("\"function\":\"oldPG.acquiring.order.paymentNotify\"");
        Assertions.assertThat(logs).contains("\"resultCodeId\":\"00000000\"");
        Assertions.assertThat(logs).contains("\"resultCode\":\"SUCCESS\"");
    }
    @Parameters({"theme"})
    @Feature("PGP-43104")
    @Test(description = "Validate payment notify sent for successful Hybrid txn")
    public void verifyPaymentNotifyWithHybrid(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.modifyBalance(user, 2.00);
        Constants.MerchantType merchant = Constants.MerchantType.Hybrid;
        OrderDTO orderDTO = new OrderFactory.PGOnly(merchant, theme)
                .setCHANNEL_ID("WEB")
                .setTXN_AMOUNT("5")
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validatePaymentMode("HYBRID")
                .validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
        String orderId = orderDTO.getORDER_ID();
        String grepcmd = "grep \"Type=PaymentNotify\" /paytm/logs/pgproxy-notification.log | grep " + orderId + " | grep \"Response\" ";
        String logs = getLogsOnServer(ServerConfigProvider.SERVICE.PG_PROXY_NOTIFICATION, grepcmd);
        Assertions.assertThat(logs).contains("\"function\\\":\\\"alipayplus.acquiring.order.paymentNotify\\\"");
        Assertions.assertThat(logs).contains("\"resultCodeId\\\":\\\"00000000\\\"");
        Assertions.assertThat(logs).contains("\\\"resultCode\\\":\\\"SUCCESS\\\"");
    }
    @Parameters({"theme"})
    @Feature("PGP-43104")
    @Test(description = "Validate payment notify sent for successful Wallet txn")
    public void verifyPaymentNotifyWithWallet(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.WalletOnly(Constants.MerchantType.WalletOnly, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT("5.00").build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()));
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.WALLET);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validatePaymentMode("PPI")
                .validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
        String orderId = orderDTO.getORDER_ID();
        String grepcmd = "grep \"" + orderId + "\" /paytm/logs/pgproxy-notification.log | grep \"Type=PaymentNotify} - Response :\"" ;
        String logs = getLogsOnServer(ServerConfigProvider.SERVICE.PG_PROXY_NOTIFICATION, grepcmd);
        Assertions.assertThat(logs).contains("\"function\":\"oldPG.acquiring.order.paymentNotify\"");
        Assertions.assertThat(logs).contains("\"resultCodeId\":\"00000000\"");
        Assertions.assertThat(logs).contains("\"resultCode\":\"SUCCESS\"");
    }

    @Owner("Shubham Soni")
    @Parameters({"theme"})
    @Feature("PGP-43686")
    @Test(description = "Verify sms triggred by PPSL user for successful CC transaction")
    public void successfulPGOnlyCCEventsPPSL(@Optional("enhancedweb_revamp") String theme) throws Exception {
        //FF4J flag notification.isPPSLUserEnabled is on
        User user = userManager.getForRead(Label.ZEROWALLET);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.SMS_BIFURCATION_NOTIF, theme).
                setTXN_AMOUNT("25").
                setSSO_TOKEN(user.ssoToken()).
                build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber("4718650100010336");
        cashierPage.payBy(Constants.PayMode.CC,paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
        String orderId = responsePage.textOrderID().getText();
        String txnId = responsePage.textTxnID().getText();

        String grepcmd = "BUSINESS_FLOW_NAME=ACQUIRING_PAYMENT_NOTIFY, KAFKA_TOPIC_NAME=NS_T_ACQUIRING_ORDER_PAYMENT_NOTIFY";
        String nqh_logs = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler,orderId,grepcmd);
        String PGP_id = nqh_logs.substring(nqh_logs.indexOf("PGPID="),nqh_logs.indexOf("} -"));
        System.out.println("PGP_ID = "+PGP_id);

    //    String response = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler,orderDTO.getORDER_ID());
        String response1 = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.communication_Gateway,PGP_id);

    //    Assertions.assertThat(response).contains("\"merchantSolutionType\":\"ONLINE\"");
     //   Assertions.assertThat(response).contains("{OID=" +orderId+", PGPID=N/A, TID="+txnId+", Type=ProcessTxnNotification} - Received Payload to get NotificationTemplateInfos, received notificationTemplateConfig : NotificationTemplateConfig{notificationType='SMS', serviceType='PAYMENT', recipient='MERCHANT', category='ONLINE_OS', status='SUCCESS', paymode='ALL, bankCode='ALL', instErrorCode='DEFAULT', mid='"+orderDTO.getMID()+"'} ");
    //    Assertions.assertThat(response).contains("businessType=ONLINE_BUSINESS");
        Assertions.assertThat(response1).contains("user=PPSL");
        Assertions.assertThat(response1).contains("template_id=[1507160569625369118]");
        Assertions.assertThat(response1).contains("text=[Rs. 25 received for Mandeep by 89XXXX7421");
        Assertions.assertThat(response1).contains("It will settle to your bank by 7 am tomorrow. Visit b.paytm.me for details. :Paytm]");
       // Assertions.assertThat(response1).contains("You+just+made+a+successful+payment+of+Rs.+25.00+at+Mandeep%0A+For+future+reference%2C+your+Order+Id+is+"+orderId);
    }

    @Owner("Shubham Soni")
    @Parameters({"theme"})
    @Feature("PGP-43686")
    @Test(description = "Verify sms triggred by PPSL user for successful DC transaction")
    public void successfulPGOnlyDCEventsPPSL(@Optional("enhancedweb_revamp") String theme) throws Exception {
        //FF4J flag notification.isPPSLUserEnabled is on
        User user = userManager.getForRead(Label.ZEROWALLET);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.SMS_BIFURCATION_NOTIF, theme).
                setTXN_AMOUNT("25").
                setSSO_TOKEN(user.ssoToken()).
                build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber("4718650100010336");
        cashierPage.payBy(Constants.PayMode.DC,paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validatePaymentMode("DC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
        String orderId = responsePage.textOrderID().getText();
        String txnId = responsePage.textTxnID().getText();

        String grepcmd = "BUSINESS_FLOW_NAME=ACQUIRING_PAYMENT_NOTIFY, KAFKA_TOPIC_NAME=NS_T_ACQUIRING_ORDER_PAYMENT_NOTIFY";
        String nqh_logs = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler,orderId,grepcmd);
        String PGP_id = nqh_logs.substring(nqh_logs.indexOf("PGPID="),nqh_logs.indexOf("} -"));
        System.out.println("PGP_ID = "+PGP_id);

//        String response = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler,orderDTO.getORDER_ID());
        String response1 = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.communication_Gateway,PGP_id);

/*        Assertions.assertThat(response).contains("merchantSolutionType='ONLINE'");
        Assertions.assertThat(response).contains("{OID=" +orderId+", PGPID=N/A, TID="+txnId+", Type=ProcessTxnNotification} - Received Payload to get NotificationTemplateInfos, received notificationTemplateConfig : NotificationTemplateConfig{notificationType='SMS', serviceType='PAYMENT', recipient='MERCHANT', category='ONLINE_OS', status='SUCCESS', paymode='ALL, bankCode='ALL', instErrorCode='DEFAULT', mid='"+orderDTO.getMID()+"'} ");
        Assertions.assertThat(response).contains("businessType=ONLINE_BUSINESS");*/
        Assertions.assertThat(response1).contains("user=PPSL");
        Assertions.assertThat(response1).contains("template_id=[1507160569625369118]");
        Assertions.assertThat(response1).contains("text=[Rs. 25 received for Mandeep by 89XXXX7421");
        Assertions.assertThat(response1).contains("It will settle to your bank by 7 am tomorrow. Visit b.paytm.me for details. :Paytm]");
    }
    @Owner("Shubham Soni")
    @Parameters({"theme"})
    @Feature("PGP-43686")
    @Test(description = "Verify sms triggred by PPSL user for successful NB transaction")
    public void successfulPGOnlyNBEventsPPSL(@Optional("enhancedweb_revamp") String theme) throws Exception {
        //FF4J flag notification.isPPSLUserEnabled is on
        User user = userManager.getForRead(Label.ZEROWALLET);
        Constants.MerchantType merchant = Constants.MerchantType.SMS_BIFURCATION_NOTIF;
        OrderDTO orderDTO = new OrderFactory.PGOnly(merchant, theme)
                .setCHANNEL_ID("WEB")
                .setTXN_AMOUNT("25")
                .setSSO_TOKEN(user.ssoToken())

                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PaymentDTO paymentDTO = new PaymentDTO().setBankName("ICICI");
        cashierPage.payBy(Constants.PayMode.NB,paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validatePaymentMode("NB")
                .validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
        String orderId = responsePage.textOrderID().getText();
        String txnId = responsePage.textTxnID().getText();

        String grepcmd = "BUSINESS_FLOW_NAME=ACQUIRING_PAYMENT_NOTIFY, KAFKA_TOPIC_NAME=NS_T_ACQUIRING_ORDER_PAYMENT_NOTIFY";
        String nqh_logs = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler,orderId,grepcmd);
        String PGP_id = nqh_logs.substring(nqh_logs.indexOf("PGPID="),nqh_logs.indexOf("} -"));
        System.out.println("PGP_ID = "+PGP_id);

    //    String response = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler,orderDTO.getORDER_ID());
        String response1 = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.communication_Gateway,PGP_id);

/*        Assertions.assertThat(response).contains("merchantSolutionType='ONLINE'");
        Assertions.assertThat(response).contains("{OID=" +orderId+", PGPID=N/A, TID="+txnId+", Type=ProcessTxnNotification} - Received Payload to get NotificationTemplateInfos, received notificationTemplateConfig : NotificationTemplateConfig{notificationType='SMS', serviceType='PAYMENT', recipient='MERCHANT', category='ONLINE_OS', status='SUCCESS', paymode='ALL, bankCode='ALL', instErrorCode='DEFAULT', mid='"+orderDTO.getMID()+"'} ");
        Assertions.assertThat(response).contains("businessType=ONLINE_BUSINESS");*/
        Assertions.assertThat(response1).contains("user=PPSL");
        Assertions.assertThat(response1).contains("template_id=[1507160569625369118]");
        Assertions.assertThat(response1).contains("text=[Rs. 25 received for Mandeep by 89XXXX7421");
        Assertions.assertThat(response1).contains("It will settle to your bank by 7 am tomorrow. Visit b.paytm.me for details. :Paytm]");
    }
    @Owner("Shubham Soni")
    @Parameters({"theme"})
    @Feature("PGP-43686")
    @Test(description = "Verify sms triggred by PPSL user for successful Wallet transaction")
    public void successfulPGOnlyWalletEventsPPSL(@Optional("enhancedweb_revamp") String theme) throws Exception {
        //FF4J flag notification.isPPSLUserEnabled is on
        User user = userManager.getForWrite(Label.ZEROWALLET);
        OrderDTO orderDTO = new OrderFactory.WalletOnly(Constants.MerchantType.SMS_BIFURCATION_NOTIF, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT("25.00").build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()));
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.WALLET);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validatePaymentMode("PPI")
                .validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
        String orderId = responsePage.textOrderID().getText();
        String txnId = responsePage.textTxnID().getText();

        String response = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler,orderDTO.getORDER_ID());
        String response1 = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.communication_Gateway,txnId);

        Assertions.assertThat(response).contains("merchantSolutionType='ONLINE'");
        Assertions.assertThat(response).contains("{OID=" +orderId+", PGPID=N/A, TID="+txnId+", Type=ProcessTxnNotification} - Received Payload to get NotificationTemplateInfos, received notificationTemplateConfig : NotificationTemplateConfig{notificationType='SMS', serviceType='PAYMENT', recipient='MERCHANT', category='ONLINE_OS', status='SUCCESS', paymode='ALL, bankCode='ALL', instErrorCode='DEFAULT', mid='"+orderDTO.getMID()+"'} ");
        Assertions.assertThat(response).contains("businessType=ONLINE_BUSINESS");
        Assertions.assertThat(response1).contains("user=PPSL");
        Assertions.assertThat(response1).contains("Json Request null, Url https://automation-pg-ext.paytm.in/mockbank/smsMockPrimaryChannel?ref_id="+orderId+"&pass=A%40ps%21197&auth=3ponline&from=iPaytm");
        Assertions.assertThat(response1).contains("text=Rs.+25+received+for+Mandeep+by");
        Assertions.assertThat(response1).contains("You+just+made+a+successful+payment+of+Rs.+25.00+at+Mandeep%0A+For+future+reference%2C+your+Order+Id+is+"+orderId);
    }
    @Owner("Shubham Soni")
    @Parameters({"theme"})
    @Feature("PGP-43686")
    @Test(description = "Verify sms triggred by PPSL user for successful UPI transaction")
    public void successfulPGOnlyUPIEventsPPSL(@Optional("enhancedweb_revamp") String theme) throws Exception {
        //FF4J flag notification.isPPSLUserEnabled is on
        User user = userManager.getForRead(Label.ZEROWALLET);
        Constants.MerchantType merchant = Constants.MerchantType.SMS_BIFURCATION_NOTIF;
        OrderDTO orderDTO = new OrderFactory.PGOnly(merchant, theme)
                .setCHANNEL_ID("WEB")
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT("25")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(Constants.PayMode.UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
        String orderId = responsePage.textOrderID().getText();
        String txnId = responsePage.textTxnID().getText();

        String grepcmd = "BUSINESS_FLOW_NAME=ACQUIRING_PAYMENT_NOTIFY, KAFKA_TOPIC_NAME=NS_T_ACQUIRING_ORDER_PAYMENT_NOTIFY";
        String nqh_logs = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler,orderId,grepcmd);
        String PGP_id = nqh_logs.substring(nqh_logs.indexOf("PGPID="),nqh_logs.indexOf("} -"));
        System.out.println("PGP_ID = "+PGP_id);

    //    String response = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler,orderDTO.getORDER_ID());
        String response1 = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.communication_Gateway,PGP_id);

/*        Assertions.assertThat(response).contains("merchantSolutionType='ONLINE'");
        Assertions.assertThat(response).contains("{OID=" +orderId+", PGPID=N/A, TID="+txnId+", Type=ProcessTxnNotification} - Received Payload to get NotificationTemplateInfos, received notificationTemplateConfig : NotificationTemplateConfig{notificationType='SMS', serviceType='PAYMENT', recipient='MERCHANT', category='ONLINE_OS', status='SUCCESS', paymode='ALL, bankCode='ALL', instErrorCode='DEFAULT', mid='"+orderDTO.getMID()+"'} ");
        Assertions.assertThat(response).contains("businessType=ONLINE_BUSINESS");*/
        Assertions.assertThat(response1).contains("user=PPSL");
        Assertions.assertThat(response1).contains("template_id=[1507160569625369118]");
        Assertions.assertThat(response1).contains("text=[Rs. 25 received for Mandeep by 89XXXX7421");
        Assertions.assertThat(response1).contains("It will settle to your bank by 7 am tomorrow. Visit b.paytm.me for details. :Paytm]");
    }
    @Owner("Shubham Soni")
    @Parameters({"theme"})
    @Feature("PGP-43686")
    @Test(description = "Verify sms triggred by PPSL user for successful Postpaid transaction")
    public void successfulPGOnlyPostpaidEventsPPSL(@Optional("enhancedweb_revamp") String theme) throws Exception {
        //FF4J flag notification.isPPSLUserEnabled is on
        User user = userManager.getForWrite(Label.PG2POSTPAIDUSER);
        PostpaidHelpers.updateBalance("25");
        Constants.MerchantType merchant = Constants.MerchantType.SMS_BIFURCATION_NOTIF;
        OrderDTO orderDTO = new OrderFactory.PGOnly(merchant, theme)
                .setCHANNEL_ID("WEB")
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT("25")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(Constants.PayMode.PAYTM_DIGITAL_CARD);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validatePaymentMode("Paytm Postpaid")
                .validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
        String orderId = responsePage.textOrderID().getText();
        String txnId = responsePage.textTxnID().getText();
        String response = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler,orderDTO.getORDER_ID());
        String response1 = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.communication_Gateway,txnId);

        Assertions.assertThat(response).contains("merchantSolutionType='ONLINE'");
        Assertions.assertThat(response).contains("{OID=" +orderId+", PGPID=N/A, TID="+txnId+", Type=ProcessTxnNotification} - Received Payload to get NotificationTemplateInfos, received notificationTemplateConfig : NotificationTemplateConfig{notificationType='SMS', serviceType='PAYMENT', recipient='MERCHANT', category='ONLINE_OS', status='SUCCESS', paymode='ALL, bankCode='ALL', instErrorCode='DEFAULT', mid='"+orderDTO.getMID()+"'} ");
        Assertions.assertThat(response).contains("businessType=ONLINE_BUSINESS");
        Assertions.assertThat(response1).contains("user=PPSL");
        Assertions.assertThat(response1).contains("Json Request null, Url https://automation-pg-ext.paytm.in/mockbank/smsMockPrimaryChannel?ref_id="+orderId+"&pass=A%40ps%21197&auth=3ponline&from=iPaytm");
        Assertions.assertThat(response1).contains("text=Rs.+25+received+for+Mandeep+by");
        Assertions.assertThat(response1).contains("You+just+made+a+successful+payment+of+Rs.+25.00+at+Mandeep%0A+For+future+reference%2C+your+Order+Id+is+"+orderId);
    }

    @Owner("Shubham Soni")
    @Parameters({"theme"})
    @Feature("PGP-43686")
    @Test(description = "Verify sms triggred by PPSL user for successful EMI transaction")
    public void successfulPGOnlyEMIEventsPPSL(@Optional("enhancedweb_revamp") String theme) throws Exception {
        //FF4J flag notification.isPPSLUserEnabled is on
        User user = userManager.getForRead(Label.ZEROWALLET);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.SMS_BIFURCATION_NOTIF, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT("6000")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.EMI, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
        String orderId = responsePage.textOrderID().getText();
        String txnId = responsePage.textTxnID().getText();

        String grepcmd = "BUSINESS_FLOW_NAME=ACQUIRING_PAYMENT_NOTIFY, KAFKA_TOPIC_NAME=NS_T_ACQUIRING_ORDER_PAYMENT_NOTIFY";
        String nqh_logs = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler,orderId,grepcmd);
        String PGP_id = nqh_logs.substring(nqh_logs.indexOf("PGPID="),nqh_logs.indexOf("} -"));
        System.out.println("PGP_ID = "+PGP_id);

  //      String response = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler,orderDTO.getORDER_ID());
        String response1 = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.communication_Gateway,PGP_id);

/*        Assertions.assertThat(response).contains("merchantSolutionType='ONLINE'");
        Assertions.assertThat(response).contains("Received Payload to get NotificationTemplateInfos, received notificationTemplateConfig");
        Assertions.assertThat(response).contains("businessType=ONLINE_BUSINESS");*/
        Assertions.assertThat(response1).contains("user=PPSL");
        Assertions.assertThat(response1).contains("template_id=[1507160569625369118]");
        Assertions.assertThat(response1).contains("text=[Rs. 6000 received for Mandeep by 89XXXX7421");
        Assertions.assertThat(response1).contains("It will settle to your bank by 7 am tomorrow. Visit b.paytm.me for details. :Paytm]");
    }
    private final String additionalinfo = "payeeType:MERCHANT|service:P2M|posId:Dynamic042|mode:QR_CODE|offlinePostConvenience:false|mappingId:{{mid}}|pgEnabled:true|qrCodeId:{{qrCodeId}}|merchantTransId:{{orderId}}|merchantVerified:true|orderAlreadyCreated:{orderAlreadyCreated}|orderQr:true|REQUEST_TYPE:QR_MERCHANT|EXPIRY_DATE:1643621579000|MERCHANT_NAME:Dynamic043|NAME:Dynamic043|MOBILE_NO:1234567890|TXN_AMOUNT:10|INDUSTRY_TYPE_ID:Retail|MERCHANT_GUID:2abbb055-3e6e-491b-b28a-ab864e1dd045|ORDER_ID:{{orderId}}|CHANNEL_ID:QRCODE|qr_code_id:{{qrCodeId}}|RECENTS_NAME:merchante|comment:|REQUEST_TYPE:QR_MERCHANT|orderAlreadyCreated:{orderAlreadyCreated}";

    @Owner(Constants.Owner.UPAMA)
    @Feature("PGP-49256")
    @Test(description = "SMS is sent only to seconday number i.e number linked with the Paytm QR for Request_Type :QR_MERCHANT")
    public void validateREQUEST_TYPEasQR_MERCHANTinNQH_DynamicQRTC01() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String mid = Constants.MerchantType.SMS_BIFURCATION_NOTIF.getId();
        String amount = "10";
        String orderId = CommonHelpers.generateOrderId();

        PaymentService paymentService = new PaymentService(Constants.MerchantType.SMS_BIFURCATION_NOTIF, amount, orderId);
        String qrCodeId = paymentService.execute().then()
                .statusCode(200)
                .rootPath("body")
                .body("resultInfo.resultStatus", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("resultInfo.resultCode", Matchers.equalToIgnoringCase("QR_0001"))
                .body("resultInfo.resultMsg", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("qrCodeId", Matchers.notNullValue())
                .body("qrData", Matchers.notNullValue())
                .extract().jsonPath()
                .getString("body.qrCodeId");

        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID(mid)
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();
        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        JsonPath fetchQRResponse = fetchQRPaymentDetails.execute().jsonPath();
        Assertions.assertThat(fetchQRResponse.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        ExtendInfo extendInfo = new ExtendInfo();
        extendInfo.setAdditionalInfo(additionalinfo.replace("{{qrCodeId}}", qrCodeId).replace("{{orderId}}", orderId).replace("{{mid}}", mid).replace("{orderAlreadyCreated}", "true"));
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid, "SSO", user.ssoToken(), orderId , amount)
                .setChannelId("APP")
                .setOrderId(orderId)
                .setRequestType("QR_MERCHANT")
                .setWebsite("retail")
                .setPaymentMode("UPI")
                .setPreferredOtpPage("bank")
                .setAuthMode("USRPWD")
                .setRequestType("QR_MERCHANT")
                .setExtendInfo(extendInfo)
                .setQRCodeId(qrCodeId)
                .build();

        ProcessTxnV1Response ptcResponse =NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler,orderId,"", "");

        Assertions.assertThat(logs).contains("Offline Merchant Sms is being forwarded on secondary merchantNo to Payload Service for  phoneNo : ");
    }
    @Owner(Constants.Owner.UPAMA)
    @Feature("PGP-49256")
    @Test(description = "SMS is sent only to seconday number i.e number linked with the Paytm QR for Request_Type :SELF_DECLARED_MERCHANT")
    public void validateREQUEST_TYPEasQR_MERCHANTinNQH_DynamicQRTC02() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String mid = Constants.MerchantType.SMS_BIFURCATION_NOTIF.getId();
        String amount = "10";
        String orderId = CommonHelpers.generateOrderId();

        PaymentService paymentService = new PaymentService(Constants.MerchantType.SMS_BIFURCATION_NOTIF, amount, orderId);
        String qrCodeId = paymentService.execute().then()
                .statusCode(200)
                .rootPath("body")
                .body("resultInfo.resultStatus", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("resultInfo.resultCode", Matchers.equalToIgnoringCase("QR_0001"))
                .body("resultInfo.resultMsg", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("qrCodeId", Matchers.notNullValue())
                .body("qrData", Matchers.notNullValue())
                .extract().jsonPath()
                .getString("body.qrCodeId");

        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID(mid)
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();
        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        JsonPath fetchQRResponse = fetchQRPaymentDetails.execute().jsonPath();
        Assertions.assertThat(fetchQRResponse.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        ExtendInfo extendInfo = new ExtendInfo();
        extendInfo.setAdditionalInfo(additionalinfo.replace("{{qrCodeId}}", qrCodeId).replace("{{orderId}}", orderId).replace("{{mid}}", mid).replace("{orderAlreadyCreated}", "true"));
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid, "SSO", user.ssoToken(), orderId , amount)
                .setChannelId("APP")
                .setOrderId(orderId)
                .setRequestType("SELF_DECLARED_MERCHANT")
                .setWebsite("retail")
                .setPaymentMode("UPI")
                .setPreferredOtpPage("bank")
                .setAuthMode("USRPWD")
                .setRequestType("SELF_DECLARED_MERCHANT")
                .setExtendInfo(extendInfo)
                .setQRCodeId(qrCodeId)
                .build();

        ProcessTxnV1Response ptcResponse =NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler,orderId,"", "");

        Assertions.assertThat(logs).contains("Offline Merchant Sms is being forwarded on secondary merchantNo to Payload Service for  phoneNo : ");
    }
    @Owner(Constants.Owner.UPAMA)
    @Feature("PGP-49256")
    @Test(description = "SMS is sent only to seconday number i.e number linked with the Paytm QR for Request_Type :UPI_QR_CODE")
    public void validateREQUEST_TYPEasQR_MERCHANTinNQH_DynamicQRTC03() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String mid = Constants.MerchantType.SMS_BIFURCATION_NOTIF.getId();
        String amount = "10";
        String orderId = CommonHelpers.generateOrderId();

        PaymentService paymentService = new PaymentService(Constants.MerchantType.SMS_BIFURCATION_NOTIF, amount, orderId);
        String qrCodeId = paymentService.execute().then()
                .statusCode(200)
                .rootPath("body")
                .body("resultInfo.resultStatus", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("resultInfo.resultCode", Matchers.equalToIgnoringCase("QR_0001"))
                .body("resultInfo.resultMsg", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("qrCodeId", Matchers.notNullValue())
                .body("qrData", Matchers.notNullValue())
                .extract().jsonPath()
                .getString("body.qrCodeId");

        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID(mid)
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();
        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        JsonPath fetchQRResponse = fetchQRPaymentDetails.execute().jsonPath();
        Assertions.assertThat(fetchQRResponse.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        ExtendInfo extendInfo = new ExtendInfo();
        extendInfo.setAdditionalInfo(additionalinfo.replace("{{qrCodeId}}", qrCodeId).replace("{{orderId}}", orderId).replace("{{mid}}", mid).replace("{orderAlreadyCreated}", "true"));
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid, "SSO", user.ssoToken(), orderId , amount)
                .setChannelId("APP")
                .setOrderId(orderId)
                .setRequestType("UPI_QR_CODE")
                .setWebsite("retail")
                .setPaymentMode("UPI")
                .setPreferredOtpPage("bank")
                .setAuthMode("USRPWD")
                .setRequestType("UPI_QR_CODE")
                .setExtendInfo(extendInfo)
                .setQRCodeId(qrCodeId)
                .build();

        ProcessTxnV1Response ptcResponse =NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler,orderId,"", "");

        Assertions.assertThat(logs).contains("Offline Merchant Sms is being forwarded on secondary merchantNo to Payload Service for  phoneNo : ");
    }
    @Owner(Constants.Owner.UPAMA)
    @Feature("PGP-49256")
    @Test(description = "SMS is sent only to seconday number i.e number linked with the Paytm QR for Request_Type :DYNAMIC_QR")
    public void validateREQUEST_TYPEasQR_MERCHANTinNQH_DynamicQRTC04() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String mid = Constants.MerchantType.SMS_BIFURCATION_NOTIF.getId();
        String amount = "10";
        String orderId = CommonHelpers.generateOrderId();

        PaymentService paymentService = new PaymentService(Constants.MerchantType.SMS_BIFURCATION_NOTIF, amount, orderId);
        String qrCodeId = paymentService.execute().then()
                .statusCode(200)
                .rootPath("body")
                .body("resultInfo.resultStatus", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("resultInfo.resultCode", Matchers.equalToIgnoringCase("QR_0001"))
                .body("resultInfo.resultMsg", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("qrCodeId", Matchers.notNullValue())
                .body("qrData", Matchers.notNullValue())
                .extract().jsonPath()
                .getString("body.qrCodeId");

        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID(mid)
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();
        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        JsonPath fetchQRResponse = fetchQRPaymentDetails.execute().jsonPath();
        Assertions.assertThat(fetchQRResponse.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        ExtendInfo extendInfo = new ExtendInfo();
        extendInfo.setAdditionalInfo(additionalinfo.replace("{{qrCodeId}}", qrCodeId).replace("{{orderId}}", orderId).replace("{{mid}}", mid).replace("{orderAlreadyCreated}", "true"));
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid, "SSO", user.ssoToken(), orderId , amount)
                .setChannelId("APP")
                .setOrderId(orderId)
                .setRequestType("DYNAMIC_QR")
                .setWebsite("retail")
                .setPaymentMode("UPI")
                .setPreferredOtpPage("bank")
                .setAuthMode("USRPWD")
                .setRequestType("DYNAMIC_QR")
                .setExtendInfo(extendInfo)
                .setQRCodeId(qrCodeId)
                .build();

        ProcessTxnV1Response ptcResponse =NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler,orderId,"", "");

        Assertions.assertThat(logs).contains("Offline Merchant Sms is being forwarded on secondary merchantNo to Payload Service for  phoneNo : ");
    }
    @Owner(Constants.Owner.UPAMA)
    @Feature("PGP-49256")
    @Test(description = "SMS is sent only to seconday number i.e number linked with the Paytm QR for Request_Type :OFFLINE_DEALS")
    public void validateREQUEST_TYPEasQR_MERCHANTinNQH_DynamicQRTC05() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String mid = Constants.MerchantType.SMS_BIFURCATION_NOTIF.getId();
        String amount = "10";
        String orderId = CommonHelpers.generateOrderId();

        PaymentService paymentService = new PaymentService(Constants.MerchantType.SMS_BIFURCATION_NOTIF, amount, orderId);
        String qrCodeId = paymentService.execute().then()
                .statusCode(200)
                .rootPath("body")
                .body("resultInfo.resultStatus", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("resultInfo.resultCode", Matchers.equalToIgnoringCase("QR_0001"))
                .body("resultInfo.resultMsg", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("qrCodeId", Matchers.notNullValue())
                .body("qrData", Matchers.notNullValue())
                .extract().jsonPath()
                .getString("body.qrCodeId");

        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID(mid)
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();
        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        JsonPath fetchQRResponse = fetchQRPaymentDetails.execute().jsonPath();
        Assertions.assertThat(fetchQRResponse.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        ExtendInfo extendInfo = new ExtendInfo();
        extendInfo.setAdditionalInfo(additionalinfo.replace("{{qrCodeId}}", qrCodeId).replace("{{orderId}}", orderId).replace("{{mid}}", mid).replace("{orderAlreadyCreated}", "true"));
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid, "SSO", user.ssoToken(), orderId , amount)
                .setChannelId("APP")
                .setOrderId(orderId)
                .setRequestType("OFFLINE_DEALS")
                .setWebsite("retail")
                .setPaymentMode("UPI")
                .setPreferredOtpPage("bank")
                .setAuthMode("USRPWD")
                .setRequestType("OFFLINE_DEALS")
                .setExtendInfo(extendInfo)
                .setQRCodeId(qrCodeId)
                .build();

        ProcessTxnV1Response ptcResponse =NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler,orderId,"", "");

        Assertions.assertThat(logs).contains("Offline Merchant Sms is being forwarded on secondary merchantNo to Payload Service for  phoneNo : ");
    }

    @Parameters("isNativePlus")
    @Owner(Constants.Owner.VIDHI)
    @Feature("PGP-54909")
    @Test(description = "Verify the CA,P4B notification logger when CA and CHAT_BE is returned from UPS")
    public void pushNotifications_CA_P4B_001(@Optional("false") Boolean isNativePlus) throws Exception {

        Constants.MerchantType merchant = Constants.MerchantType.BFF_LAYERED_FPO;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setRequestType("NATIVE")
                .setTxnValue("20.00")
                .build();

        String TxnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        PaymentDTO paymentDTO = new PaymentDTO();
        OrderDTO orderDTO = new OrderFactory.Native(merchant, initTxnDTO.orderFromBody(), TxnToken, paymentDTO, PayMethodType.CREDIT_CARD)
                .build();
        
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.execute();
        txnStatus.validateStatus("TXN_SUCCESS");
        
        String orderId = orderDTO.getORDER_ID();
        String grepcmd = "BUSINESS_FLOW_NAME=ACQUIRING_PAYMENT_NOTIFY, KAFKA_TOPIC_NAME=NS_T_ACQUIRING_ORDER_PAYMENT_NOTIFY";

        String nqh_logs = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler,orderId,grepcmd);
        String PGP_id = nqh_logs.substring(nqh_logs.indexOf("PGPID="),nqh_logs.indexOf("} -"));
        System.out.println("PGP_ID = "+PGP_id);

        String nqh_logs_UPS = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler,PGP_id,"UPS Entity preference request");
        Assertions.assertThat(nqh_logs_UPS).contains("[\"ocl.notification.merchant.app_preference\",\"ocl.notification.merchant.push\",\"ocl.notification.merchant.reminder_widget_preference\"]");

        String nqh_logs_UPS1 = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler,PGP_id,"appPushNotificationsType after chat back end");
        Assertions.assertThat(nqh_logs_UPS1).contains("[CA, CHAT_BE]");

    }

    @Parameters({"isNativePlus"})
    @Owner(Constants.Owner.VIDHI)
    @Feature("PGP-54909")
    @Test(description = "Verify the CA,P4B notification logger when CA and P4B is returned from UPS")
    public void pushNotifications_CA_P4B_002(@Optional("false") Boolean isNativePlus) throws Exception {

        Constants.MerchantType merchant = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT_PUSH;

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setRequestType("NATIVE")
                .setTxnValue("20.00")
                .build();

        String TxnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        PaymentDTO paymentDTO = new PaymentDTO();
        OrderDTO orderDTO = new OrderFactory.Native(merchant, initTxnDTO.orderFromBody(), TxnToken, paymentDTO, PayMethodType.CREDIT_CARD)
                .build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS").assertAll();

        String orderId = orderDTO.getORDER_ID();
        String grepcmd = "BUSINESS_FLOW_NAME=ACQUIRING_PAYMENT_NOTIFY, KAFKA_TOPIC_NAME=NS_T_ACQUIRING_ORDER_PAYMENT_NOTIFY";

        String nqh_logs = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler,orderId,grepcmd);
        String PGP_id = nqh_logs.substring(nqh_logs.indexOf("PGPID="),nqh_logs.indexOf("} -"));
        System.out.println("PGP_ID = "+PGP_id);

        String nqh_logs_UPS = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler,PGP_id,"UPS Entity preference request");
        Assertions.assertThat(nqh_logs_UPS).contains("[\"ocl.notification.merchant.app_preference\",\"ocl.notification.merchant.push\",\"ocl.notification.merchant.reminder_widget_preference\"]");

        String nqh_logs_UPS1 = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler,PGP_id,"appPushNotificationsType after chat back end");
        Assertions.assertThat(nqh_logs_UPS1).contains("[P4B, CA]");

    }

    @Parameters("isNativePlus")
    @Owner(Constants.Owner.VIDHI)
    @Feature("PGP-54909")
    @Test(description = "Verify the CA,P4B notification logger when CHAT_BE is returned from UPS")
    public void pushNotifications_CA_P4B_003(@Optional("false") Boolean isNativePlus) throws Exception {

        Constants.MerchantType merchant = Constants.MerchantType.CHAT_BE_NOTIFICATION_MID;

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setRequestType("NATIVE")
                .setTxnValue("20.00")
                .build();

        String TxnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        PaymentDTO paymentDTO = new PaymentDTO();
        OrderDTO orderDTO = new OrderFactory.Native(merchant, initTxnDTO.orderFromBody(), TxnToken, paymentDTO, PayMethodType.CREDIT_CARD)
                .build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS").assertAll();

        String orderId = orderDTO.getORDER_ID();
        String grepcmd = "BUSINESS_FLOW_NAME=ACQUIRING_PAYMENT_NOTIFY, KAFKA_TOPIC_NAME=NS_T_ACQUIRING_ORDER_PAYMENT_NOTIFY";
        String nqh_logs = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler,orderId,grepcmd);
        String PGP_id = nqh_logs.substring(nqh_logs.indexOf("PGPID="),nqh_logs.indexOf("} -"));
        System.out.println("PGP_ID = "+PGP_id);

        String nqh_logs_UPS = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler,PGP_id,"UPS Entity preference request");
        Assertions.assertThat(nqh_logs_UPS).contains("[\"ocl.notification.merchant.app_preference\",\"ocl.notification.merchant.push\",\"ocl.notification.merchant.reminder_widget_preference\"]");

        String nqh_logs_UPS1 = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler,PGP_id,"appPushNotificationsType after chat back end");
        Assertions.assertThat(nqh_logs_UPS1).contains("[CHAT_BE]");

    }

    @Parameters("isNativePlus")
    @Owner(Constants.Owner.VIDHI)
    @Feature("PGP-54909")
    @Test(description = "Verify the CA,P4B notification logger  when P4B and CHAT_BE is returned from UPS")
    public void pushNotifications_CA_P4B_004(@Optional("false") Boolean isNativePlus) throws Exception {

        Constants.MerchantType merchant = Constants.MerchantType.P4B_NOTIFICATION_MID;

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setRequestType("NATIVE")
                .setTxnValue("20.00")
                .build();

        String TxnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        PaymentDTO paymentDTO = new PaymentDTO();
        OrderDTO orderDTO = new OrderFactory.Native(merchant, initTxnDTO.orderFromBody(), TxnToken, paymentDTO, PayMethodType.CREDIT_CARD)
                .build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS").assertAll();

        String orderId = orderDTO.getORDER_ID();
        String grepcmd = "BUSINESS_FLOW_NAME=ACQUIRING_PAYMENT_NOTIFY, KAFKA_TOPIC_NAME=NS_T_ACQUIRING_ORDER_PAYMENT_NOTIFY";
        String nqh_logs = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler,orderId,grepcmd);
        String PGP_id = nqh_logs.substring(nqh_logs.indexOf("PGPID="),nqh_logs.indexOf("} -"));
        System.out.println("PGP_ID = "+PGP_id);

        String nqh_logs_UPS = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler,PGP_id,"UPS Entity preference request");
       // Assertions.assertThat(nqh_logs_UPS).contains("[\"ocl.notification.merchant.app_preference\",\"ocl.notification.merchant.push\",\"ocl.notification.merchant.reminder_widget_preference\"]");
        Assertions.assertThat(nqh_logs_UPS).contains("\"ocl.notification.merchant.app_preference\",\"ocl.notification.merchant.push\",\"ocl.notification.merchant.reminder_widget_preference\"");

        String nqh_logs_UPS1 = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler,PGP_id,"appPushNotificationsType after chat back end");
       // Assertions.assertThat(nqh_logs_UPS1).contains("[P4B, CHAT_BE]");
        Assertions.assertThat(nqh_logs_UPS1).contains("P4B, CHAT_BE");
    }
    @Parameters("isNativePlus")
    @Owner(Constants.Owner.VIDHI)
    @Feature("PGP-54909")
    @Test(description = "Verify the CA,P4B notification logger when CA and P4B is returned from UPS")
    public void pushNotifications_CA_P4B_005(@Optional("false") Boolean isNativePlus) throws Exception {

        Constants.MerchantType merchant = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT;

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setRequestType("NATIVE")
                .setTxnValue("20.00")
                .build();

        String TxnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        PaymentDTO paymentDTO = new PaymentDTO();
        OrderDTO orderDTO = new OrderFactory.Native(merchant, initTxnDTO.orderFromBody(), TxnToken, paymentDTO, PayMethodType.CREDIT_CARD)
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS").assertAll();

        String orderId = orderDTO.getORDER_ID();
        String grepcmd = "BUSINESS_FLOW_NAME=ACQUIRING_PAYMENT_NOTIFY, KAFKA_TOPIC_NAME=NS_T_ACQUIRING_ORDER_PAYMENT_NOTIFY";
        String nqh_logs = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler,orderId,grepcmd);
        String PGP_id = nqh_logs.substring(nqh_logs.indexOf("PGPID="),nqh_logs.indexOf("} -"));
        System.out.println("PGP_ID = "+PGP_id);

        String nqh_logs_UPS = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler,PGP_id,"UPS Entity preference request");
        Assertions.assertThat(nqh_logs_UPS).contains("[\"ocl.notification.merchant.app_preference\",\"ocl.notification.merchant.push\",\"ocl.notification.merchant.reminder_widget_preference\"]");

        String nqh_logs_UPS1 = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler,PGP_id,"appPushNotificationsType after chat back end");
        Assertions.assertThat(nqh_logs_UPS1).contains("[P4B, CA]");
    }

    @Parameters("isNativePlus")
    @Owner(Constants.Owner.VIDHI)
    @Feature("PGP-54909")
    @Test(description = "Verify the notificationReceiverIdentifier in push when UPS is returning [CHAT_BE, CA]")
    public void pushNotificationsIdentifier_CA_P4B_001(@Optional("true") Boolean isNativePlus) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.BFF_LAYERED_FPO;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setRequestType("NATIVE")
                .setTxnValue("20.00")
                .build();
        String TxnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchant, initTxnDTO.orderFromBody(), TxnToken, PayMethodType.NET_BANKING)
                .setChannelCode("ICICI")
                .setTXN_TOKEN(TxnToken)
                .setPaymentMode("NET_BANKING")
                .setAUTH_MODE("USRPWD")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        String orderId = orderDTO.getORDER_ID();
        String grepcmd = "BUSINESS_FLOW_NAME=ACQUIRING_PAYMENT_NOTIFY, KAFKA_TOPIC_NAME=NS_T_ACQUIRING_ORDER_PAYMENT_NOTIFY";
        String nqh_logs = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler,orderId,grepcmd);
        String PGP_id = nqh_logs.substring(nqh_logs.indexOf("PGPID="),nqh_logs.indexOf("} -"));
        System.out.println("PGP_ID = "+PGP_id);
        String nqh_logs_UPS = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler,PGP_id,"UPS Entity preference request");
        Assertions.assertThat(nqh_logs_UPS).contains("[\"ocl.notification.merchant.app_preference\",\"ocl.notification.merchant.push\",\"ocl.notification.merchant.reminder_widget_preference\"]");
        String nqh_logs_UPS1 = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler,PGP_id,"appPushNotificationsType after chat back end");
        Assertions.assertThat(nqh_logs_UPS1).contains("[CA, CHAT_BE]");

        String cgw_logs_ca = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.communication_Gateway, orderId,"merchant_push_paytm_app");
        System.out.println("CA LOGS - "+cgw_logs_ca);

        String custId_CA_PUSH = cgw_logs_ca.substring(cgw_logs_ca.indexOf("\"notificationReceiverIdentifier\":[\"")+35,cgw_logs_ca.indexOf("\"]}"));
        System.out.println("cust id = "+custId_CA_PUSH);
        Assertions.assertThat(cgw_logs_ca).contains("\"notificationReceiverType\":\"CUSTOMERID\"");
        Assertions.assertThat(custId_CA_PUSH).isNotEqualTo(merchant.getId());
        
        String cgw_logs_chat_be = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.communication_Gateway, PGP_id,"Chat Back End Notification Payload");
        System.out.println("CHAT BE LOGS - "+cgw_logs_chat_be);
        Assertions.assertThat(cgw_logs_chat_be).contains("\"templateName\":\"Merchant-Transactions-Chat\"");

        String cgw_logs_chat_be_response = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.communication_Gateway, PGP_id,"Chat BackEnd Notification Response");
        System.out.println("CHAT BE LOGS - "+cgw_logs_chat_be_response);
        Assertions.assertThat(cgw_logs_chat_be_response).contains("\"message\":\"chat job created successfully\"");
    }
    @Parameters("isNativePlus")
    @Owner(Constants.Owner.VIDHI)
    @Feature("PGP-54909")
    @Test(description = "Verify the notificationReceiverIdentifier in push when UPS is returning [CA, P4B]")
    public void pushNotificationsIdentifier_CA_P4B_002(@Optional("true") Boolean isNativePlus) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT_PUSH;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setRequestType("NATIVE")
                .setTxnValue("20.00")
                .build();
        String TxnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchant, initTxnDTO.orderFromBody(), TxnToken, PayMethodType.NET_BANKING)
                .setChannelCode("ICICI")
                .setTXN_TOKEN(TxnToken)
                .setPaymentMode("NET_BANKING")
                .setAUTH_MODE("USRPWD")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS").assertAll();
        String orderId = orderDTO.getORDER_ID();
        String grepcmd = "BUSINESS_FLOW_NAME=ACQUIRING_PAYMENT_NOTIFY, KAFKA_TOPIC_NAME=NS_T_ACQUIRING_ORDER_PAYMENT_NOTIFY";
        String nqh_logs = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler,orderId,grepcmd);
        String PGP_id = nqh_logs.substring(nqh_logs.indexOf("PGPID="),nqh_logs.indexOf("} -"));
        System.out.println("PGP_ID = "+PGP_id);
        String nqh_logs_UPS = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler,PGP_id,"UPS Entity preference request");
        Assertions.assertThat(nqh_logs_UPS).contains("[\"ocl.notification.merchant.app_preference\",\"ocl.notification.merchant.push\",\"ocl.notification.merchant.reminder_widget_preference\"]");
        String nqh_logs_UPS1 = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler,PGP_id,"appPushNotificationsType after chat back end");
        Assertions.assertThat(nqh_logs_UPS1).contains("[P4B, CA]");

        String cgw_logs_ca = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.communication_Gateway, orderId,"merchant_push_paytm_app");
        System.out.println("CA LOGS - "+cgw_logs_ca);
        String cgw_logs_p4b = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.communication_Gateway, orderId,"test_template_for_business_app");
        System.out.println("P4B LOGS - "+cgw_logs_p4b);

        String custId_CA_PUSH = cgw_logs_ca.substring(cgw_logs_ca.indexOf("\"notificationReceiverIdentifier\":[\"")+35,cgw_logs_ca.indexOf("\"]}"));
        System.out.println("cust id = "+custId_CA_PUSH);
        Assertions.assertThat(cgw_logs_ca).contains("\"notificationReceiverType\":\"CUSTOMERID\"");
        Assertions.assertThat(custId_CA_PUSH).isNotEqualTo(merchant.getId());

        String custId_P4B_PUSH = cgw_logs_p4b.substring(cgw_logs_ca.indexOf("\"notificationReceiverIdentifier\":[\"")+42,cgw_logs_p4b.indexOf("\"]}"));
        System.out.println("cust id p4b = "+custId_P4B_PUSH);
        Assertions.assertThat(cgw_logs_p4b).contains("\"notificationReceiverType\":\"MERCHANTID\"");
        Assertions.assertThat(custId_P4B_PUSH).isEqualTo(merchant.getId());
    }
    @Parameters("isNativePlus")
    @Owner(Constants.Owner.VIDHI)
    @Feature("PGP-54909")
    @Test(description = "Verify the notificationReceiverIdentifier in push when UPS is returning [CHAT_BE]")
    public void pushNotificationsIdentifier_CA_P4B_003(@Optional("true") Boolean isNativePlus) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.CHAT_BE_NOTIFICATION_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setRequestType("NATIVE")
                .setTxnValue("20.00")
                .build();
        String TxnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        OrderDTO orderDTO = new OrderFactory.Native(merchant, initTxnDTO.orderFromBody(), TxnToken, paymentDTO, PayMethodType.CREDIT_CARD)
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS").assertAll();
        String orderId = orderDTO.getORDER_ID();
        String grepcmd = "BUSINESS_FLOW_NAME=ACQUIRING_PAYMENT_NOTIFY, KAFKA_TOPIC_NAME=NS_T_ACQUIRING_ORDER_PAYMENT_NOTIFY";
        String nqh_logs = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler,orderId,grepcmd);
        String PGP_id = nqh_logs.substring(nqh_logs.indexOf("PGPID="),nqh_logs.indexOf("} -"));
        System.out.println("PGP_ID = "+PGP_id);
        String nqh_logs_UPS = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler,PGP_id,"UPS Entity preference request");
        Assertions.assertThat(nqh_logs_UPS).contains("[\"ocl.notification.merchant.app_preference\",\"ocl.notification.merchant.push\",\"ocl.notification.merchant.reminder_widget_preference\"]");
        String nqh_logs_UPS1 = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler,PGP_id,"appPushNotificationsType after chat back end");
        Assertions.assertThat(nqh_logs_UPS1).contains("[CHAT_BE]");

        String cgw_logs_chat_be = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.communication_Gateway, PGP_id,"Chat Back End Notification Payload");
        System.out.println("CHAT BE LOGS - "+cgw_logs_chat_be);
        Assertions.assertThat(cgw_logs_chat_be).contains("\"templateName\":\"Merchant-Transactions-Chat\"");

        String cgw_logs_chat_be_response = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.communication_Gateway, PGP_id,"Chat BackEnd Notification Response");
        System.out.println("CHAT BE LOGS - "+cgw_logs_chat_be_response);
        Assertions.assertThat(cgw_logs_chat_be_response).contains("\"message\":\"chat job created successfully\"");
    }
    @Parameters("isNativePlus")
    @Owner(Constants.Owner.VIDHI)
    @Feature("PGP-54909")
    @Test(description = "Verify the notificationReceiverIdentifier in push when UPS is returning [P4B, CA]")
    public void pushNotificationsIdentifier_CA_P4B_004(@Optional("true") Boolean isNativePlus) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setRequestType("NATIVE")
                .setTxnValue("20.00")
                .build();
        String TxnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchant, initTxnDTO.orderFromBody(), TxnToken, PayMethodType.NET_BANKING)
                .setChannelCode("ICICI")
                .setTXN_TOKEN(TxnToken)
                .setPaymentMode("NET_BANKING")
                .setAUTH_MODE("USRPWD")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS").assertAll();
        String orderId = orderDTO.getORDER_ID();
        String grepcmd = "BUSINESS_FLOW_NAME=ACQUIRING_PAYMENT_NOTIFY, KAFKA_TOPIC_NAME=NS_T_ACQUIRING_ORDER_PAYMENT_NOTIFY";
        String nqh_logs = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler,orderId,grepcmd);
        String PGP_id = nqh_logs.substring(nqh_logs.indexOf("PGPID="),nqh_logs.indexOf("} -"));
        System.out.println("PGP_ID = "+PGP_id);
        String nqh_logs_UPS = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler,PGP_id,"UPS Entity preference request");
        Assertions.assertThat(nqh_logs_UPS).contains("[\"ocl.notification.merchant.app_preference\",\"ocl.notification.merchant.push\",\"ocl.notification.merchant.reminder_widget_preference\"]");
        String nqh_logs_UPS1 = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler,PGP_id,"appPushNotificationsType after chat back end");
        Assertions.assertThat(nqh_logs_UPS1).contains("[P4B, CA]");

        String cgw_logs_ca = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.communication_Gateway, orderId,"merchant_push_paytm_app");
        System.out.println("CA LOGS - "+cgw_logs_ca);
        String cgw_logs_p4b = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.communication_Gateway, orderId,"test_template_for_business_app");
        System.out.println("P4B LOGS - "+cgw_logs_p4b);

        String custId_CA_PUSH = cgw_logs_ca.substring(cgw_logs_ca.indexOf("\"notificationReceiverIdentifier\":[\"")+35,cgw_logs_ca.indexOf("\"]}"));
        System.out.println("cust id = "+custId_CA_PUSH);
        Assertions.assertThat(cgw_logs_ca).contains("\"notificationReceiverType\":\"CUSTOMERID\"");
        Assertions.assertThat(custId_CA_PUSH).isNotEqualTo(merchant.getId());

        String custId_P4B_PUSH = cgw_logs_p4b.substring(cgw_logs_p4b.indexOf("\"notificationReceiverIdentifier\":[\"")+35,cgw_logs_p4b.indexOf("\"]}"));
        System.out.println("cust id p4b = "+custId_P4B_PUSH);
        Assertions.assertThat(cgw_logs_p4b).contains("\"notificationReceiverType\":\"MERCHANTID\"");
        Assertions.assertThat(custId_P4B_PUSH).isEqualTo(merchant.getId());
    }
    @Parameters("isNativePlus")
    @Owner(Constants.Owner.VIDHI)
    @Feature("PGP-54909")
    @Test(description = "Verify the notificationReceiverIdentifier in push when UPS is returning [P4B, CA]")
    public void pushNotificationsIdentifier_CA_P4B_005(@Optional("true") Boolean isNativePlus) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setRequestType("NATIVE")
                .setTxnValue("20.00")
                .build();
        String TxnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchant, initTxnDTO.orderFromBody(), TxnToken, PayMethodType.NET_BANKING)
                .setChannelCode("ICICI")
                .setTXN_TOKEN(TxnToken)
                .setPaymentMode("NET_BANKING")
                .setAUTH_MODE("USRPWD")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS").assertAll();
        String orderId = orderDTO.getORDER_ID();
        String grepcmd = "BUSINESS_FLOW_NAME=ACQUIRING_PAYMENT_NOTIFY, KAFKA_TOPIC_NAME=NS_T_ACQUIRING_ORDER_PAYMENT_NOTIFY";
        String nqh_logs = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler,orderId,grepcmd);
        String PGP_id = nqh_logs.substring(nqh_logs.indexOf("PGPID="),nqh_logs.indexOf("} -"));
        System.out.println("PGP_ID = "+PGP_id);
        String nqh_logs_UPS = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler,PGP_id,"UPS Entity preference request");
        Assertions.assertThat(nqh_logs_UPS).contains("[\"ocl.notification.merchant.app_preference\",\"ocl.notification.merchant.push\",\"ocl.notification.merchant.reminder_widget_preference\"]");
        String nqh_logs_UPS1 = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler,PGP_id,"appPushNotificationsType after chat back end");
        Assertions.assertThat(nqh_logs_UPS1).contains("[P4B, CA]");

        String cgw_logs_ca = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.communication_Gateway, orderId,"merchant_push_paytm_app");
        System.out.println("CA LOGS - "+cgw_logs_ca);
        String cgw_logs_p4b = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.communication_Gateway, orderId,"test_template_for_business_app");
        System.out.println("P4B LOGS - "+cgw_logs_p4b);

        String custId_CA_PUSH = cgw_logs_ca.substring(cgw_logs_ca.indexOf("\"notificationReceiverIdentifier\":[\"")+35,cgw_logs_ca.indexOf("\"]}"));
        System.out.println("cust id = "+custId_CA_PUSH);
        Assertions.assertThat(cgw_logs_ca).contains("\"notificationReceiverType\":\"CUSTOMERID\"");
        Assertions.assertThat(custId_CA_PUSH).isNotEqualTo(merchant.getId());

        String custId_P4B_PUSH = cgw_logs_p4b.substring(cgw_logs_p4b.indexOf("\"notificationReceiverIdentifier\":[\"")+35,cgw_logs_p4b.indexOf("\"]}"));
        System.out.println("cust id p4b = "+custId_P4B_PUSH);
        Assertions.assertThat(cgw_logs_p4b).contains("\"notificationReceiverType\":\"MERCHANTID\"");
        Assertions.assertThat(custId_P4B_PUSH).isEqualTo(merchant.getId());
    }


    @Parameters({"theme"})
    @Owner(Constants.Owner.AKSHAT)
    @Feature("PGP-56642")
    @Test(description = "Validate that Whatsapp Notify is generated for the merchant")
    public void successTxn_WhatsappNotify_01(@Optional("enhancedweb_revamp") String theme) throws Exception{

        //Static Pref : WA_PAY_NOTIFY is enabled on merchant
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.AXIS_BANK_SQR, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus
                .validateStatus("TXN_SUCCESS")
                .AssertAll();

        String orderId = orderDTO.getORDER_ID();
        String grepcmd = "BUSINESS_FLOW_NAME=ACQUIRING_PAYMENT_NOTIFY, KAFKA_TOPIC_NAME=NS_T_ACQUIRING_ORDER_PAYMENT_NOTIFY";
        String nqh_logs = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, orderId, grepcmd);
        String PGP_id = nqh_logs.substring(nqh_logs.indexOf("PGPID="), nqh_logs.indexOf("} -"));
        System.out.println("PGP_ID = " + PGP_id);

        String comm_logs = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.communication_Gateway, PGP_id);
        Assertions.assertThat(comm_logs).contains("KAFKA_TOPIC_NAME=NS_T_WHATSAPP_HIGH_PRIORITY");
        Assertions.assertThat(comm_logs).contains("templateName\":\"wa_offline_payments_paynotify\"");

    }

    @Parameters({"theme"})
    @Owner(Constants.Owner.AKSHAT)
    @Feature("PGP-56642")
    @Test(description = "Validate that Whatsapp Notify is successfully sent to the merchant")
    public void successTxn_WhatsappNotify_02(@Optional("enhancedweb_revamp") String theme) throws Exception{

        //Static Pref : WA_PAY_NOTIFY is enabled on merchant
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.AXIS_BANK_SQR, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus
                .validateStatus("TXN_SUCCESS")
                .AssertAll();

        String orderId = orderDTO.getORDER_ID();
        String grepcmd = "BUSINESS_FLOW_NAME=ACQUIRING_PAYMENT_NOTIFY, KAFKA_TOPIC_NAME=NS_T_ACQUIRING_ORDER_PAYMENT_NOTIFY";
        String nqh_logs = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, orderId, grepcmd);
        String PGP_id = nqh_logs.substring(nqh_logs.indexOf("PGPID="), nqh_logs.indexOf("} -"));
        System.out.println("PGP_ID = " + PGP_id);

        String comm_logs = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.communication_Gateway, PGP_id);
        Assertions.assertThat(comm_logs).contains("Whatsapp Notification sent successfully with jobId");
        Assertions.assertThat(comm_logs).contains("\"message\":\"whatsapp job created successfully\"");

    }

    @Parameters("isNativePlus")
    @Owner(Constants.Owner.AKSHAT)
    @Feature("PGP-56697")
    @Test(description = "Verify the ocl.notification.merchant.voice_notification_enabled is returned from UPS")
    public void pushNotifications_56997_001(@Optional("false") Boolean isNativePlus) throws Exception {

        Constants.MerchantType merchant = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT_PUSH;

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setRequestType("NATIVE")
                .setTxnValue("20.00")
                .build();

        String TxnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        PaymentDTO paymentDTO = new PaymentDTO();
        OrderDTO orderDTO = new OrderFactory.Native(merchant, initTxnDTO.orderFromBody(), TxnToken, paymentDTO, PayMethodType.CREDIT_CARD)
                .build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS").assertAll();

        String orderId = orderDTO.getORDER_ID();
        String grepcmd = "BUSINESS_FLOW_NAME=ACQUIRING_PAYMENT_NOTIFY, KAFKA_TOPIC_NAME=NS_T_ACQUIRING_ORDER_PAYMENT_NOTIFY";
        String nqh_logs = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler,orderId,grepcmd);
        String PGP_id = nqh_logs.substring(nqh_logs.indexOf("PGPID="),nqh_logs.indexOf("} -"));
        System.out.println("PGP_ID = "+PGP_id);

        String nqh_logs_UPS = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler,PGP_id,"UPS Entity preference request");
        Assertions.assertThat(nqh_logs_UPS).contains("[\"ocl.notification.merchant.app_preference\",\"ocl.notification.merchant.push\",\"ocl.notification.merchant.reminder_widget_preference\",\"ocl.notification.merchant.voice_notification_enabled\",\"ocl.notification.merchant.display_notification_flag\"]");

    }

    @Parameters("isNativePlus")
    @Owner(Constants.Owner.AKSHAT)
    @Feature("PGP-56697")
    @Test(description = "Verify the ocl.notification.merchant.display_notification_flag is returned from UPS")
    public void pushNotifications_56997_002(@Optional("false") Boolean isNativePlus) throws Exception {

        Constants.MerchantType merchant = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT_PUSH;

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setRequestType("NATIVE")
                .setTxnValue("20.00")
                .build();

        String TxnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        PaymentDTO paymentDTO = new PaymentDTO();
        OrderDTO orderDTO = new OrderFactory.Native(merchant, initTxnDTO.orderFromBody(), TxnToken, paymentDTO, PayMethodType.CREDIT_CARD)
                .build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS").assertAll();

        String orderId = orderDTO.getORDER_ID();
        String grepcmd = "BUSINESS_FLOW_NAME=ACQUIRING_PAYMENT_NOTIFY, KAFKA_TOPIC_NAME=NS_T_ACQUIRING_ORDER_PAYMENT_NOTIFY";
        String nqh_logs = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler,orderId,grepcmd);
        String PGP_id = nqh_logs.substring(nqh_logs.indexOf("PGPID="),nqh_logs.indexOf("} -"));
        System.out.println("PGP_ID = "+PGP_id);

        String nqh_logs_UPS = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler,PGP_id,"UPS Entity preference request");
        Assertions.assertThat(nqh_logs_UPS).contains("[\"ocl.notification.merchant.app_preference\",\"ocl.notification.merchant.push\",\"ocl.notification.merchant.reminder_widget_preference\",\"ocl.notification.merchant.voice_notification_enabled\",\"ocl.notification.merchant.display_notification_flag\"]");

    }

    @Parameters("isNativePlus")
    @Owner(Constants.Owner.AKSHAT)
    @Feature("PGP-56697")
    @Test(description = "Verify the voiceNotifPref is returned from UPS")
    public void pushNotifications_56997_003(@Optional("false") Boolean isNativePlus) throws Exception {

        Constants.MerchantType merchant = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT_PUSH;

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setRequestType("NATIVE")
                .setTxnValue("20.00")
                .build();

        String TxnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        PaymentDTO paymentDTO = new PaymentDTO();
        OrderDTO orderDTO = new OrderFactory.Native(merchant, initTxnDTO.orderFromBody(), TxnToken, paymentDTO, PayMethodType.CREDIT_CARD)
                .build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS").assertAll();

        String orderId = orderDTO.getORDER_ID();
        String grepcmd = "BUSINESS_FLOW_NAME=ACQUIRING_PAYMENT_NOTIFY, KAFKA_TOPIC_NAME=NS_T_ACQUIRING_ORDER_PAYMENT_NOTIFY";
        String nqh_logs = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, orderId, grepcmd);
        String PGP_id = nqh_logs.substring(nqh_logs.indexOf("PGPID="), nqh_logs.indexOf("} -"));
        System.out.println("PGP_ID = " + PGP_id);

        String comm_logs = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.communication_Gateway, PGP_id);
        Assertions.assertThat(comm_logs).contains("\"voiceNotifPref\":\"true\"");

    }

    @Parameters("isNativePlus")
    @Owner(Constants.Owner.AKSHAT)
    @Feature("PGP-56697")
    @Test(description = "Verify the fullScreenNotifPref is returned from UPS")
    public void pushNotifications_56997_004(@Optional("false") Boolean isNativePlus) throws Exception {

        Constants.MerchantType merchant = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT_PUSH;

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setRequestType("NATIVE")
                .setTxnValue("20.00")
                .build();

        String TxnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        PaymentDTO paymentDTO = new PaymentDTO();
        OrderDTO orderDTO = new OrderFactory.Native(merchant, initTxnDTO.orderFromBody(), TxnToken, paymentDTO, PayMethodType.CREDIT_CARD)
                .build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS").assertAll();

        String orderId = orderDTO.getORDER_ID();
        String grepcmd = "BUSINESS_FLOW_NAME=ACQUIRING_PAYMENT_NOTIFY, KAFKA_TOPIC_NAME=NS_T_ACQUIRING_ORDER_PAYMENT_NOTIFY";
        String nqh_logs = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, orderId, grepcmd);
        String PGP_id = nqh_logs.substring(nqh_logs.indexOf("PGPID="), nqh_logs.indexOf("} -"));
        System.out.println("PGP_ID = " + PGP_id);

        String comm_logs = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.communication_Gateway, PGP_id);
        Assertions.assertThat(comm_logs).contains("\"fullScreenNotifPref\":\"false\"");

    }


    @Parameters("isNativePlus")
    @Owner(Constants.Owner.AJEESH)
    @Feature("PGP-56697")
    @Test(description = "Verify the fullScreenNotifPref is returned from UPS")
    public void pushNotifications_56997_005(@Optional("false") Boolean isNativePlus) throws Exception {

        Constants.MerchantType merchant = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT_PUSH;

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setRequestType("NATIVE")
                .setTxnValue("20.00")
                .build();

        String TxnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        PaymentDTO paymentDTO = new PaymentDTO();
        OrderDTO orderDTO = new OrderFactory.Native(merchant, initTxnDTO.orderFromBody(), TxnToken, paymentDTO, PayMethodType.CREDIT_CARD)
                .build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS").assertAll();

        String query = "select data from kafka_consumer_data where topic='NS_T_PUSHNOTIFY_HIGH_PRIORITY'";
        List<Map<String, Object>> result = DatabaseUtil.getInstance().executeSelectQuery(Constants.DBConnectionURL.MOCK_KAFKA_DB, query);
        if (result.size() == 0) {
            throw new RuntimeException("Record not found");
        }
        String topicData = (String) result.get(result.size()-1).get("data");
        if (topicData == null || topicData.isEmpty()) {
            throw new RuntimeException("topicData is either null or empty");
        }

        //ADD VALIDATIONS ACCORDINGLY
    }

}