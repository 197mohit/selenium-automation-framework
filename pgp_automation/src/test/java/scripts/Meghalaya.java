package scripts;

import com.paytm.ServerConfigProvider;
import com.paytm.appconstants.Constants;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.pages.*;
import io.qameta.allure.Owner;
import io.qameta.allure.*;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import static com.paytm.apphelpers.LogsValidationHelper.getLogsOnServer;


@Owner(Constants.Owner.AAYUSH)
@Feature("PGP-34915")

public class Meghalaya extends PGPBaseTest {

    private final CheckoutPage checkoutPage = new CheckoutPage();
    @Parameters({"theme"})
    @Test(description = "To verify different logs value in notification_q_handler logs when DC txn is done")
    public void verifyingLogsFromServer_DC(@Optional("enhancedweb_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.Meghalaya_Merchant, theme)
                .setMERC_UNQ_REF("Aayush")
                .setEMAIL("test@paytm.com")
                .setMobileNumber("9999988888")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.DC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        //Checking ff4j flag value
        String grepcmd = "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/notificationQueueHandler.log | " +
                "grep \"" +"CommunicationTemplateEngineUtils.getFF4jFlag()" + "\" | grep \"FF4J Flag is : true for feature : EMAIL_TEMPLATE_CONFIG_FLAG\" ";
        String notificationqueuehandler=getLogsOnServer(ServerConfigProvider.SERVICE.NOTIFICATION_QUEUE_HANDLER,grepcmd);

        //Checking Phone number sms has gone
        String grepcmd2 = "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/notificationQueueHandler.log | " +
                "grep \"" +"NotificationPayloadServiceImpl.sendSMSWithPriority()" + "\" | grep \"Successfully produced SmsInfo in Kafka topic HIGH_PRIORITY_SMS for PhoneNo :9999988888\" ";
        String notificationqueuehandler2=getLogsOnServer(ServerConfigProvider.SERVICE.NOTIFICATION_QUEUE_HANDLER,grepcmd2);

        //Checking for email being sent on specified email
        String grepcmd3 = "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/notificationQueueHandler.log | " +
                "grep \"" +"NotificationPayloadServiceImpl.sendMail()" + "\" | grep \"Successfully produced EmailInfo in Queue email_cg_notify for EmailId :test@paytm.com\" ";
        String notificationqueuehandler3=getLogsOnServer(ServerConfigProvider.SERVICE.NOTIFICATION_QUEUE_HANDLER,grepcmd3);

        //Checking that the response recieved from mapping service for the template of the mail is not empty
        String grepcmd4 = "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/notificationQueueHandler.log | " +
                "grep \"" +"MappingClientWrapper.getEmailTemplateConfig()" + "\" | grep \"Response received from mapping service mid:Meghaa70655202233845, response:EmailTemplateInfo(serviceType=PAYMENT, category=ALL, recipient=USER, status=SUCCESS, bankCode=ALL, errorCode=ALL, mid=ALL, payMode=ALL, params=null, enabled=Y, senderEmail=noreply@paytm.com, senderName=iPaytm, replyTo=noreply@paytm.com, templateAttachment\" ";
        String notificationqueuehandler4=getLogsOnServer(ServerConfigProvider.SERVICE.NOTIFICATION_QUEUE_HANDLER,grepcmd4);

        //Checking in notification queue handler log that the mail sent is not a normal/simple mail but of Meghalaya format
        String grepcmd5 = "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/notificationQueueHandler.log | " +
                "grep \"" +"MappingClientWrapper.getEmailTemplateConfig()" + "\" | grep \"Online Post-Paid Electricity Bill Payment Confirmation - MEECL\" ";
        String notificationqueuehandler5=getLogsOnServer(ServerConfigProvider.SERVICE.NOTIFICATION_QUEUE_HANDLER,grepcmd5);

        //Communication gateway checking the mail is being sent and is of Meghalaya format only by the subject
        String grepcmd6 = "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/communicationGateway.log | " +
                "grep \"EmailSentServiceImpl.run()\"";
        String notificationqueuehandler6 = getLogsOnServer(ServerConfigProvider.SERVICE.COMMUNICATION_GATEWAY,grepcmd6);

        //Assertions
        Assertions.assertThat(notificationqueuehandler).contains("FF4J Flag is : true for feature : EMAIL_TEMPLATE_CONFIG_FLAG");
        Assertions.assertThat(notificationqueuehandler2).contains("Successfully produced SmsInfo in Kafka topic HIGH_PRIORITY_SMS for PhoneNo :9999988888");
        Assertions.assertThat(notificationqueuehandler3).contains("Successfully produced EmailInfo in Queue email_cg_notify for EmailId :test@paytm.com");
        Assertions.assertThat(notificationqueuehandler4).contains("Response received from mapping service mid:Meghaa70655202233845, response:EmailTemplateInfo(serviceType=PAYMENT, category=ALL, recipient=USER, status=SUCCESS, bankCode=ALL, errorCode=ALL, mid=ALL, payMode=ALL, params=null, enabled=Y, senderEmail=noreply@paytm.com, senderName=iPaytm, replyTo=noreply@paytm.com, templateAttachment=");
        Assertions.assertThat(notificationqueuehandler5).contains("Online Post-Paid Electricity Bill Payment Confirmation - MEECL");
        Assertions.assertThat(notificationqueuehandler6).contains("Sending email to :[test@paytm.com] , emailSubject : Online Post-Paid Electricity Bill Payment Confirmation - MEECL");
    }

    @Parameters({"theme"})
    @Test(description = "To verify different logs value in notification_q_handler logs when CC txn is done")
    public void verifyingLogsFromServer_CC(@Optional("enhancedweb_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.Meghalaya_Merchant, theme)
                .setMERC_UNQ_REF("Aayush")
                .setEMAIL("test@paytm.com")
                .setMobileNumber("9999988888")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        //Checking ff4j flag value
        String grepcmd = "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/notificationQueueHandler.log | " +
                "grep \"" +"CommunicationTemplateEngineUtils.getFF4jFlag()" + "\" | grep \"FF4J Flag is : true for feature : EMAIL_TEMPLATE_CONFIG_FLAG\" ";
        String notificationqueuehandler=getLogsOnServer(ServerConfigProvider.SERVICE.NOTIFICATION_QUEUE_HANDLER,grepcmd);
//Checking Phone number sms has gone
        String grepcmd2 = "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/notificationQueueHandler.log | " +
                "grep \"" +"NotificationPayloadServiceImpl.sendSMSWithPriority()" + "\" | grep \"Successfully produced SmsInfo in Kafka topic HIGH_PRIORITY_SMS for PhoneNo :9999988888\" ";
        String notificationqueuehandler2=getLogsOnServer(ServerConfigProvider.SERVICE.NOTIFICATION_QUEUE_HANDLER,grepcmd2);

        //Checking for email being sent on specified email
        String grepcmd3 = "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/notificationQueueHandler.log | " +
                "grep \"" +"NotificationPayloadServiceImpl.sendMail()" + "\" | grep \"Successfully produced EmailInfo in Queue email_cg_notify for EmailId :test@paytm.com\" ";
        String notificationqueuehandler3=getLogsOnServer(ServerConfigProvider.SERVICE.NOTIFICATION_QUEUE_HANDLER,grepcmd3);

        //Checking that the response recieved from mapping service for the template of the mail is not empty
        String grepcmd4 = "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/notificationQueueHandler.log | " +
                "grep \"" +"MappingClientWrapper.getEmailTemplateConfig()" + "\" | grep \"Response received from mapping service mid:Meghaa70655202233845, response:EmailTemplateInfo(serviceType=PAYMENT, category=ALL, recipient=USER, status=SUCCESS, bankCode=ALL, errorCode=ALL, mid=ALL, payMode=ALL, params=null, enabled=Y, senderEmail=noreply@paytm.com, senderName=iPaytm, replyTo=noreply@paytm.com, templateAttachment\" ";
        String notificationqueuehandler4=getLogsOnServer(ServerConfigProvider.SERVICE.NOTIFICATION_QUEUE_HANDLER,grepcmd4);

        //Checking in notification queue handler log that the mail sent is not a normal/simple mail but of Meghalaya format
        String grepcmd5 = "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/notificationQueueHandler.log | " +
                "grep \"" +"MappingClientWrapper.getEmailTemplateConfig()" + "\" | grep \"Online Post-Paid Electricity Bill Payment Confirmation - MEECL\" ";
        String notificationqueuehandler5=getLogsOnServer(ServerConfigProvider.SERVICE.NOTIFICATION_QUEUE_HANDLER,grepcmd5);

        //Communication gateway checking the mail is being sent and is of Meghalaya format only by the subject
        String grepcmd6 = "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/communicationGateway.log | " +
                "grep \"EmailSentServiceImpl.run()\"";
        String notificationqueuehandler6 = getLogsOnServer(ServerConfigProvider.SERVICE.COMMUNICATION_GATEWAY,grepcmd6);

        //Assertions
        Assertions.assertThat(notificationqueuehandler).contains("FF4J Flag is : true for feature : EMAIL_TEMPLATE_CONFIG_FLAG");
        Assertions.assertThat(notificationqueuehandler2).contains("Successfully produced SmsInfo in Kafka topic HIGH_PRIORITY_SMS for PhoneNo :9999988888");
        Assertions.assertThat(notificationqueuehandler3).contains("Successfully produced EmailInfo in Queue email_cg_notify for EmailId :test@paytm.com");
        Assertions.assertThat(notificationqueuehandler4).contains("Response received from mapping service mid:Meghaa70655202233845, response:EmailTemplateInfo(serviceType=PAYMENT, category=ALL, recipient=USER, status=SUCCESS, bankCode=ALL, errorCode=ALL, mid=ALL, payMode=ALL, params=null, enabled=Y, senderEmail=noreply@paytm.com, senderName=iPaytm, replyTo=noreply@paytm.com, templateAttachment=");
        Assertions.assertThat(notificationqueuehandler5).contains("Online Post-Paid Electricity Bill Payment Confirmation - MEECL");
        Assertions.assertThat(notificationqueuehandler6).contains("Sending email to :[test@paytm.com] , emailSubject : Online Post-Paid Electricity Bill Payment Confirmation - MEECL");
    }


    @Parameters({"theme"})
    @Test(description = "To verify different logs value in notification_q_handler logs when NB txn is done")
    public void verifyingLogsFromServer_NB(@Optional("enhancedweb_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.Meghalaya_Merchant, theme)
                .setMERC_UNQ_REF("Aayush")
                .setEMAIL("test@paytm.com")
                .setMobileNumber("9999988888")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.NB);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        //Checking ff4j flag value
        String grepcmd = "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/notificationQueueHandler.log | " +
                "grep \"" +"CommunicationTemplateEngineUtils.getFF4jFlag()" + "\" | grep \"FF4J Flag is : true for feature : EMAIL_TEMPLATE_CONFIG_FLAG\" ";
        String notificationqueuehandler=getLogsOnServer(ServerConfigProvider.SERVICE.NOTIFICATION_QUEUE_HANDLER,grepcmd);

        //Checking Phone number sms has gone
        String grepcmd2 = "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/notificationQueueHandler.log | " +
                "grep \"" +"NotificationPayloadServiceImpl.sendSMSWithPriority()" + "\" | grep \"Successfully produced SmsInfo in Kafka topic HIGH_PRIORITY_SMS for PhoneNo :9999988888\" ";
        String notificationqueuehandler2=getLogsOnServer(ServerConfigProvider.SERVICE.NOTIFICATION_QUEUE_HANDLER,grepcmd2);

        //Checking for email being sent on specified email
        String grepcmd3 = "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/notificationQueueHandler.log | " +
                "grep \"" +"NotificationPayloadServiceImpl.sendMail()" + "\" | grep \"Successfully produced EmailInfo in Queue email_cg_notify for EmailId :test@paytm.com\" ";
        String notificationqueuehandler3=getLogsOnServer(ServerConfigProvider.SERVICE.NOTIFICATION_QUEUE_HANDLER,grepcmd3);

        //Checking that the response recieved from mapping service for the template of the mail is not empty
        String grepcmd4 = "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/notificationQueueHandler.log | " +
                "grep \"" +"MappingClientWrapper.getEmailTemplateConfig()" + "\" | grep \"Response received from mapping service mid:Meghaa70655202233845, response:EmailTemplateInfo(serviceType=PAYMENT, category=ALL, recipient=USER, status=SUCCESS, bankCode=ALL, errorCode=ALL, mid=ALL, payMode=ALL, params=null, enabled=Y, senderEmail=noreply@paytm.com, senderName=iPaytm, replyTo=noreply@paytm.com, templateAttachment\" ";
        String notificationqueuehandler4=getLogsOnServer(ServerConfigProvider.SERVICE.NOTIFICATION_QUEUE_HANDLER,grepcmd4);

        //Checking in notification queue handler log that the mail sent is not a normal/simple mail but of Meghalaya format
        String grepcmd5 = "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/notificationQueueHandler.log | " +
                "grep \"" +"MappingClientWrapper.getEmailTemplateConfig()" + "\" | grep \"Online Post-Paid Electricity Bill Payment Confirmation - MEECL\" ";
        String notificationqueuehandler5=getLogsOnServer(ServerConfigProvider.SERVICE.NOTIFICATION_QUEUE_HANDLER,grepcmd5);

        //Communication gateway checking the mail is being sent and is of Meghalaya format only by the subject
        String grepcmd6 = "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/communicationGateway.log | " +
                "grep \"EmailSentServiceImpl.run()\"";
        String notificationqueuehandler6 = getLogsOnServer(ServerConfigProvider.SERVICE.COMMUNICATION_GATEWAY,grepcmd6);

        //Assertions
        Assertions.assertThat(notificationqueuehandler).contains("FF4J Flag is : true for feature : EMAIL_TEMPLATE_CONFIG_FLAG");
        Assertions.assertThat(notificationqueuehandler2).contains("Successfully produced SmsInfo in Kafka topic HIGH_PRIORITY_SMS for PhoneNo :9999988888");
        Assertions.assertThat(notificationqueuehandler3).contains("Successfully produced EmailInfo in Queue email_cg_notify for EmailId :test@paytm.com");
        Assertions.assertThat(notificationqueuehandler4).contains("Response received from mapping service mid:Meghaa70655202233845, response:EmailTemplateInfo(serviceType=PAYMENT, category=ALL, recipient=USER, status=SUCCESS, bankCode=ALL, errorCode=ALL, mid=ALL, payMode=ALL, params=null, enabled=Y, senderEmail=noreply@paytm.com, senderName=iPaytm, replyTo=noreply@paytm.com, templateAttachment=");
        Assertions.assertThat(notificationqueuehandler5).contains("Online Post-Paid Electricity Bill Payment Confirmation - MEECL");
        Assertions.assertThat(notificationqueuehandler6).contains("Sending email to :[test@paytm.com] , emailSubject : Online Post-Paid Electricity Bill Payment Confirmation - MEECL");
    }
}