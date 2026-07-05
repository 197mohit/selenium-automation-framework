package scripts.api.linkservice;

import com.paytm.ServerConfigProvider;
import com.paytm.api.linkAPI.*;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.LogsValidationHelper;
import com.paytm.apphelpers.PG2LogsValidationHelper;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.LinkPaymentLoginPage;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.paytm.apphelpers.LogsValidationHelper.getLogsOnServer;

public class KafkaMessagePayload extends PGPBaseTest {
    User user;
    String mid;
    void setUserAndMId(String merchant) throws Exception {
        user = userManager.getForRead(Label.LINK);
        mid = merchant;
    }
    public List<String> dateFetch(int days){
        List<String> dates=new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String toDate= CommonHelpers.addDays(sdf.format(new Date()),"dd/MM/yyyy",days);
        String fromdate = sdf.format(new Date());
        dates.add(fromdate);
        dates.add(toDate);
        return dates;
    }


    @Owner("Anushka_Goldi")
    @Feature("PGP-41056")
    @Test(description = "Create a link  and check the PAYMENT_LINK_CREATION messagePayload in Kafka Logs")
    public void MessagePayload_01() throws Exception {
        RestAssured.useRelaxedHTTPSValidation();
        setUserAndMId(Constants.MerchantType.MAX_AMOUNT_CHECK.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        String eventsLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service_event,"PAYMENT_LINK_CREATION");
        Assertions.assertThat(eventsLogs).contains(linkId);
        Assertions.assertThat(eventsLogs).contains("PAYMENT_LINK_CREATION");
        Assertions.assertThat(eventsLogs).contains("\"responseStatus\":\"SUCCESS\"");
        Assertions.assertThat(eventsLogs).contains("\"responseCode\":\"200\"");
        Assertions.assertThat(eventsLogs).contains("\"responseMessage\":\"Payment link is created successfully\"");
        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId);
        Assertions.assertThat(linkServiceLogs).contains(linkId);
        Assertions.assertThat(linkServiceLogs).contains("Sending link events info to kafka");
        Assertions.assertThat(linkServiceLogs).contains("messagePayload=LinkEventsInfo");
        Assertions.assertThat(linkServiceLogs).contains("eventName=PAYMENT_LINK_CREATION");
        Assertions.assertThat(linkServiceLogs).contains("topic=LINK_EVENTS_DWH");
        Assertions.assertThat(linkServiceLogs).contains("traceId=LINK_EVENTS_DWH");
        Assertions.assertThat(linkServiceLogs).contains("responseStatus=SUCCESS");
        Assertions.assertThat(linkServiceLogs).contains("buttonDetails");
        Assertions.assertThat(linkServiceLogs).contains("linkNotes");
        Assertions.assertThat(linkServiceLogs).contains("formLink");
        Assertions.assertThat(linkServiceLogs).contains("partialPayment");
        Assertions.assertThat(linkServiceLogs).contains("emiSubvention");
        Assertions.assertThat(linkServiceLogs).contains("bankOffers");
        Assertions.assertThat(linkServiceLogs).contains("edcBankOfferLink");
        Assertions.assertThat(linkServiceLogs).contains("splitSettlement");
        Assertions.assertThat(linkServiceLogs).contains("resellerId");
        Assertions.assertThat(linkServiceLogs).contains("maxPaymentsAllowed");
        Assertions.assertThat(linkServiceLogs).contains("reminders");
        Assertions.assertThat(linkServiceLogs).contains("responseMessage=Payment link is created successfully");
    }

    @Owner("Anushka_Goldi")
    @Feature("PGP-41056")
    @Test(description = "Create a link to open it and check the PAYMENT_LINK_CLICKED messagePayload in Kafka Logs")
    public void MessagePayload_02() throws Exception {
        RestAssured.useRelaxedHTTPSValidation();
        setUserAndMId(Constants.MerchantType.MAX_AMOUNT_CHECK.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        String eventsLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service_event,"PAYMENT_LINK_CLICKED");
        Assertions.assertThat(eventsLogs).contains(linkId);
        Assertions.assertThat(eventsLogs).contains("PAYMENT_LINK_CLICKED");
        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,"Sending link events info to kafka");
        Assertions.assertThat(linkServiceLogs).contains(linkId);
        Assertions.assertThat(linkServiceLogs).contains("Sending link events info to kafka");
        Assertions.assertThat(linkServiceLogs).contains("messagePayload=LinkEventsInfo");
        Assertions.assertThat(linkServiceLogs).contains("eventName=PAYMENT_LINK_CLICKED");
        Assertions.assertThat(linkServiceLogs).contains("topic=LINK_EVENTS_DWH");
        Assertions.assertThat(linkServiceLogs).contains("buttonDetails");
        Assertions.assertThat(linkServiceLogs).contains("linkNotes");
        Assertions.assertThat(linkServiceLogs).contains("traceId=LINK_EVENTS_DWH");
    }



    @Owner("Anushka_Goldi")
    @Feature("PGP-41056")
    @Test(description = "Update a link and check the PAYMNENT_LINK_UPDATED messagePayload in Kafka Logs")
    public void MessagePayload_03() throws Exception {
        RestAssured.useRelaxedHTTPSValidation();
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, "FIXED", "200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        UpdateLink updateLink =new UpdateLink(mid,linkId);
        updateLink.setContext("body.amount","10");
        JsonPath withDrawJson = updateLink.execute().jsonPath();
        String eventsLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service_event,"PAYMNENT_LINK_UPDATED");
        Assertions.assertThat(eventsLogs).contains(linkId);
        Assertions.assertThat(eventsLogs).contains("PAYMNENT_LINK_UPDATED");
        Assertions.assertThat(eventsLogs).contains("\"responseStatus\":\"SUCCESS\"");
        Assertions.assertThat(eventsLogs).contains("\"responseCode\":\"200\"");
        Assertions.assertThat(eventsLogs).contains("\"responseMessage\":\"Payment link is processed successfully\"");
        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId);
        Assertions.assertThat(linkServiceLogs).contains(linkId);
        Assertions.assertThat(linkServiceLogs).contains("Sending link events info to kafka");
        Assertions.assertThat(linkServiceLogs).contains("messagePayload=LinkEventsInfo");
        Assertions.assertThat(linkServiceLogs).contains("eventName=PAYMNENT_LINK_UPDATED");
        Assertions.assertThat(linkServiceLogs).contains("responseStatus=SUCCESS");
        Assertions.assertThat(linkServiceLogs).contains("responseCode=200");
        Assertions.assertThat(linkServiceLogs).contains("buttonDetails");
        Assertions.assertThat(linkServiceLogs).contains("linkNotes");
        Assertions.assertThat(linkServiceLogs).contains("formLink");
        Assertions.assertThat(linkServiceLogs).contains("maxPaymentsAllowed");
        Assertions.assertThat(linkServiceLogs).contains("responseMessage=Payment link is processed successfully");
        Assertions.assertThat(linkServiceLogs).contains("topic=LINK_EVENTS_DWH");
        Assertions.assertThat(linkServiceLogs).contains("traceId=LINK_EVENTS_DWH");
    }

    @Owner("Anushka_Goldi")
    @Feature("PGP-41056")
    @Test(description = "Hit the resendNotification api and check the PAYMENT_LINK_RESEND_NOTIFICATION messagePayload in Kafka Logs")
    public void MessagePayload_04() throws Exception {
        RestAssured.useRelaxedHTTPSValidation();
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED", "200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        ResendNotificationLink resendNotificationLinkLink = new ResendNotificationLink().buildRequest(mid,linkId);
        JsonPath withDrawJson = resendNotificationLinkLink.execute().jsonPath();
        String eventsLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service_event,"PAYMENT_LINK_RESEND_NOTIFICATION");
        Assertions.assertThat(eventsLogs).contains(linkId);
        Assertions.assertThat(eventsLogs).contains("PAYMENT_LINK_RESEND_NOTIFICATION");
        Assertions.assertThat(eventsLogs).contains("\"responseStatus\":\"SUCCESS\"");
        Assertions.assertThat(eventsLogs).contains("\"responseCode\":\"200\"");
        Assertions.assertThat(eventsLogs).contains("\"responseMessage\":\"Notifications for the link are processed\"");
        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId);
        Assertions.assertThat(linkServiceLogs).contains(linkId);
        Assertions.assertThat(linkServiceLogs).contains("Sending link events info to kafka");
        Assertions.assertThat(linkServiceLogs).contains("messagePayload=LinkEventsInfo");
        Assertions.assertThat(linkServiceLogs).contains("eventName=PAYMENT_LINK_RESEND_NOTIFICATION");
        Assertions.assertThat(linkServiceLogs).contains("responseStatus=SUCCESS");
        Assertions.assertThat(linkServiceLogs).contains("responseCode=200");
        Assertions.assertThat(linkServiceLogs).contains("responseMessage=Notifications for the link are processed");
        Assertions.assertThat(linkServiceLogs).contains("buttonDetails");
        Assertions.assertThat(linkServiceLogs).contains("linkNotes");
        Assertions.assertThat(linkServiceLogs).contains("topic=LINK_EVENTS_DWH");
        Assertions.assertThat(linkServiceLogs).contains("traceId=LINK_EVENTS_DWH");
    }

    @Owner("Anushka_Goldi")
    @Feature("PGP-41056")
    @Test(description = "Hit the fetchTransaction api and check the PAYMENT_LINK_FETCH_TRANSACTION messagePayload in Kafka Logs")
    public void MessagePayload_05() throws Exception {
        RestAssured.useRelaxedHTTPSValidation();
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED", "200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        Integer linkId = withDrawJson1.getInt("body.linkId");
        List<String> dates=dateFetch(5);
        FetchTransactionApi fetchTransactionApi=new FetchTransactionApi().buildRequest(mid,linkId,dates.get(0),dates.get((1)));
        JsonPath withDrawJson = fetchTransactionApi.execute().jsonPath();
        String eventsLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service_event,"PAYMENT_LINK_FETCH_TRANSACTION");
        Assertions.assertThat(eventsLogs).contains("PAYMENT_LINK_FETCH_TRANSACTION");
        Assertions.assertThat(eventsLogs).contains("\"responseStatus\":\"SUCCESS\"");
        Assertions.assertThat(eventsLogs).contains("\"responseCode\":\"200\"");
        Assertions.assertThat(eventsLogs).contains("\"responseMessage\":\"Payment link is processed successfully\"");
        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,"Sending link events info to kafka");
        Assertions.assertThat(linkServiceLogs).contains("Sending link events info to kafka");
        Assertions.assertThat(linkServiceLogs).contains("messagePayload=LinkEventsInfo");
        Assertions.assertThat(linkServiceLogs).contains("eventName=PAYMENT_LINK_FETCH_TRANSACTION");
        Assertions.assertThat(linkServiceLogs).contains("responseStatus=SUCCESS");
        Assertions.assertThat(linkServiceLogs).contains("responseCode=200");
        Assertions.assertThat(linkServiceLogs).contains("responseMessage=Payment link is processed successfully");
        Assertions.assertThat(linkServiceLogs).contains("buttonDetails");
        Assertions.assertThat(linkServiceLogs).contains("linkNotes");
        Assertions.assertThat(linkServiceLogs).contains("startDate");
        Assertions.assertThat(linkServiceLogs).contains("endDate");
        Assertions.assertThat(linkServiceLogs).contains("topic=LINK_EVENTS_DWH");
        Assertions.assertThat(linkServiceLogs).contains("traceId=LINK_EVENTS_DWH");

    }

    @Owner("Anushka_Goldi")
    @Feature("PGP-41056")
    @Test(description = "Hit the link fetch api and check the PAYMENT_LINK_FETCHED messagePayload in Kafka Logs")
    public void MessagePayload_06() throws Exception {
        RestAssured.useRelaxedHTTPSValidation();
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        FetchLinkApi fetchLinkApi=new FetchLinkApi();
        List<String>dates=dateFetch(5);
        fetchLinkApi.buildRequest(mid,linkId,dates.get(1),dates.get((0)));
        JsonPath fetchlinkresponse=fetchLinkApi.execute().jsonPath();
        String eventsLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service_event,"PAYMENT_LINK_FETCHED");
        Assertions.assertThat(eventsLogs).contains(linkId);
        Assertions.assertThat(eventsLogs).contains("PAYMENT_LINK_FETCHED");
        Assertions.assertThat(eventsLogs).contains("\"responseStatus\":\"SUCCESS\"");
        Assertions.assertThat(eventsLogs).contains("\"responseCode\":\"200\"");
        Assertions.assertThat(eventsLogs).contains("\"responseMessage\":\"Payment link is processed successfully\"");
        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId);
        Assertions.assertThat(linkServiceLogs).contains(linkId);
        Assertions.assertThat(linkServiceLogs).contains("Sending link events info to kafka");
        Assertions.assertThat(linkServiceLogs).contains("messagePayload=LinkEventsInfo");
        Assertions.assertThat(linkServiceLogs).contains("eventName=PAYMENT_LINK_FETCHED");
        Assertions.assertThat(linkServiceLogs).contains("responseStatus=SUCCESS");
        Assertions.assertThat(linkServiceLogs).contains("responseCode=200");
        Assertions.assertThat(linkServiceLogs).contains("responseMessage=Payment link is processed successfully");
        Assertions.assertThat(linkServiceLogs).contains("buttonDetails");
        Assertions.assertThat(linkServiceLogs).contains("linkNotes");
        Assertions.assertThat(linkServiceLogs).contains("topic=LINK_EVENTS_DWH");
        Assertions.assertThat(linkServiceLogs).contains("traceId=LINK_EVENTS_DWH");
    }



    @Owner("Anushka_Goldi")
    @Feature("PGP-41056")
    @Test(description = "Create a link and check the PAYMENT_LINK_EXPIRED messagePayload in Kafka Logs")
    public void MessagePayload_08() throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, "FIXED", "200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        ExpireLink expireLink = new ExpireLink().buildRequest(mid,linkId);
        JsonPath withDrawJson = expireLink.execute().jsonPath();
        String eventsLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service_event,"PAYMENT_LINK_EXPIRED");
        Assertions.assertThat(eventsLogs).contains(linkId);
        Assertions.assertThat(eventsLogs).contains("PAYMENT_LINK_EXPIRED");
        Assertions.assertThat(eventsLogs).contains("\"responseStatus\":\"SUCCESS\"");
        Assertions.assertThat(eventsLogs).contains("\"responseCode\":\"200\"");
        Assertions.assertThat(eventsLogs).contains("\"responseMessage\":\"Payment link is expired successfully\"");
        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId);
        Assertions.assertThat(linkServiceLogs).contains(linkId);
        Assertions.assertThat(linkServiceLogs).contains("Sending link events info to kafka");
        Assertions.assertThat(linkServiceLogs).contains("messagePayload=LinkEventsInfo");
        Assertions.assertThat(linkServiceLogs).contains("eventName=PAYMENT_LINK_EXPIRED");
        Assertions.assertThat(linkServiceLogs).contains("responseStatus=SUCCESS");
        Assertions.assertThat(linkServiceLogs).contains("responseCode=200");
        Assertions.assertThat(linkServiceLogs).contains("responseMessage=Payment link is expired successfully");
        Assertions.assertThat(linkServiceLogs).contains("buttonDetails");
        Assertions.assertThat(linkServiceLogs).contains("linkNotes");
        Assertions.assertThat(linkServiceLogs).contains("topic=LINK_EVENTS_DWH");
        Assertions.assertThat(linkServiceLogs).contains("traceId=LINK_EVENTS_DWH");
    }

    @Owner("Anushka_Goldi")
    @Feature("PGP-41056")
    @Test(description = "Create a link and check the PAYMENT_LINK_ARCHIVED messagePayload in Kafka Logs")
    public void MessagePayload_09() throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, "FIXED", "200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        ArchiveLink archiveLink = new ArchiveLink().buildRequest(mid,linkId);
        JsonPath withDrawJson = archiveLink.execute().jsonPath();
        String eventsLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service_event,"PAYMENT_LINK_ARCHIVED");
        Assertions.assertThat(eventsLogs).contains(linkId);
        Assertions.assertThat(eventsLogs).contains("PAYMENT_LINK_ARCHIVED");
        Assertions.assertThat(eventsLogs).contains("\"responseStatus\":\"SUCCESS\"");
        Assertions.assertThat(eventsLogs).contains("\"responseCode\":\"200\"");
        Assertions.assertThat(eventsLogs).contains("\"responseMessage\":\"Payment link is archived successfully\"");
        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId);
        Assertions.assertThat(linkServiceLogs).contains(linkId);
        Assertions.assertThat(linkServiceLogs).contains("Sending link events info to kafka");
        Assertions.assertThat(linkServiceLogs).contains("messagePayload=LinkEventsInfo");
        Assertions.assertThat(linkServiceLogs).contains("eventName=PAYMENT_LINK_ARCHIVED");
        Assertions.assertThat(linkServiceLogs).contains("responseStatus=SUCCESS");
        Assertions.assertThat(linkServiceLogs).contains("responseCode=200");
        Assertions.assertThat(linkServiceLogs).contains("responseMessage=Payment link is archived successfully");
        Assertions.assertThat(linkServiceLogs).contains("buttonDetails");
        Assertions.assertThat(linkServiceLogs).contains("linkNotes");
        Assertions.assertThat(linkServiceLogs).contains("topic=LINK_EVENTS_DWH");
        Assertions.assertThat(linkServiceLogs).contains("traceId=LINK_EVENTS_DWH");
    }

    @Owner("Anushka_Goldi")
    @Feature("PGP-41056")
    @Test(description = "Create a link and check the MOBILE_BINDING_NUMBER_SUBMITTED messagePayload in Kafka Logs")
    public void MessagePayload_10() throws Exception {
        RestAssured.useRelaxedHTTPSValidation();
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED", "20");
        createNewLink.setContext("body.bindLinkIdMobile", true);
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkandSubmitOTPofmobilebinding(user,paymentLink,"FIXED","10");
        String eventsLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service_event,"MOBILE_BINDING_NUMBER_SUBMITTED");
        Assertions.assertThat(eventsLogs).contains(linkId);
        Assertions.assertThat(eventsLogs).contains("MOBILE_BINDING_NUMBER_SUBMITTED");
        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId);
        Assertions.assertThat(linkServiceLogs).contains(linkId);
        Assertions.assertThat(linkServiceLogs).contains("Sending link events info to kafka");
        Assertions.assertThat(linkServiceLogs).contains("messagePayload=LinkEventsInfo");
        Assertions.assertThat(linkServiceLogs).contains("eventName=MOBILE_BINDING_NUMBER_SUBMITTED");
        Assertions.assertThat(linkServiceLogs).contains("topic=LINK_EVENTS_DWH");
        Assertions.assertThat(linkServiceLogs).contains("buttonDetails");
        Assertions.assertThat(linkServiceLogs).contains("linkNotes");
        Assertions.assertThat(linkServiceLogs).contains("traceId=LINK_EVENTS_DWH");
    }


    @Owner("Anushka_Goldi")
    @Feature("PGP-41056")
    @Test(description = "Create a link and check the PARTIAL_PAYMENT_SUBMITTED messagePayload in Kafka Logs")
    public void MessagePayload_11() throws Exception {
        RestAssured.useRelaxedHTTPSValidation();
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED", "20");
        createNewLink.setContext("body.partialPayment", true);
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkofPartialPayment(user,paymentLink,"FIXED","PayPartialPayment","10");
        String eventsLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service_event,"PARTIAL_PAYMENT_SUBMITTED");
        Assertions.assertThat(eventsLogs).contains(linkId);
        Assertions.assertThat(eventsLogs).contains("PARTIAL_PAYMENT_SUBMITTED");
        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId);
        Assertions.assertThat(linkServiceLogs).contains(linkId);
        Assertions.assertThat(linkServiceLogs).contains("Sending link events info to kafka");
        Assertions.assertThat(linkServiceLogs).contains("messagePayload=LinkEventsInfo");
        Assertions.assertThat(linkServiceLogs).contains("eventName=PARTIAL_PAYMENT_SUBMITTED");
        Assertions.assertThat(linkServiceLogs).contains("topic=LINK_EVENTS_DWH");
        Assertions.assertThat(linkServiceLogs).contains("traceId=LINK_EVENTS_DWH");
    }

    @Owner("Anushka_Goldi")
    @Feature("PGP-41056")
    @Test(description = "Create a link and check the PAYMENT_FORM_SUBMITTED messagePayload in Kafka Logs")
    public void MessagePayload_12() throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_ALLIN_ONE_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","200");
        createNewLink.setContext("body.templateId","7");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPWithForms(user,paymentLink,"FIXED","PAYMENTFORM","200");
        String eventsLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service_event,"PARTIAL_PAYMENT_SUBMITTED");
        Assertions.assertThat(eventsLogs).contains(linkId);
        Assertions.assertThat(eventsLogs).contains("PAYMENT_FORM_SUBMITTED");
        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId);
        Assertions.assertThat(linkServiceLogs).contains(linkId);
        Assertions.assertThat(linkServiceLogs).contains("Sending link events info to kafka");
        Assertions.assertThat(linkServiceLogs).contains("messagePayload=LinkEventsInfo");
        Assertions.assertThat(linkServiceLogs).contains("eventName=PAYMENT_FORM_SUBMITTED");
        Assertions.assertThat(linkServiceLogs).contains("topic=LINK_EVENTS_DWH");
        Assertions.assertThat(linkServiceLogs).contains("traceId=LINK_EVENTS_DWH");
    }


    @Owner("Anushka_Goldi")
    @Feature("PGP-41056")
    @Test(description = "Create a link and check the CLICK_PROCEED messagePayload in Kafka Logs")
    public void MessagePayload_13() throws Exception {
        RestAssured.useRelaxedHTTPSValidation();
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"INVOICE", "20");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"INVOICE","web");
        String eventsLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service_event,"CLICK_PROCEED");
        Assertions.assertThat(eventsLogs).contains(linkId);
        Assertions.assertThat(eventsLogs).contains("CLICK_PROCEED");
        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId);
        Assertions.assertThat(linkServiceLogs).contains(linkId);
        Assertions.assertThat(linkServiceLogs).contains("Sending link events info to kafka");
        Assertions.assertThat(linkServiceLogs).contains("messagePayload=LinkEventsInfo");
        Assertions.assertThat(linkServiceLogs).contains("eventName=CLICK_PROCEED");
        Assertions.assertThat(linkServiceLogs).contains("topic=LINK_EVENTS_DWH");
        Assertions.assertThat(linkServiceLogs).contains("traceId=LINK_EVENTS_DWH");
    }

    @Owner("Anushka_Goldi")
    @Feature("PGP-41056")
    @Test(description = "Create a link and check the SKIP_LOGIN_SUBMITTED messagePayload in Kafka Logs")
    public void MessagePayload_14() throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_SKIPLOGIN_ONLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPWithForms(user,paymentLink,"FIXED", "SKIPLOGIN", "20000000");
        String eventsLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service_event,"SKIP_LOGIN_SUBMITTED");
        Assertions.assertThat(eventsLogs).contains(linkId);
        Assertions.assertThat(eventsLogs).contains("SKIP_LOGIN_SUBMITTED");
        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId);
        Assertions.assertThat(linkServiceLogs).contains(linkId);
        Assertions.assertThat(linkServiceLogs).contains("Sending link events info to kafka");
        Assertions.assertThat(linkServiceLogs).contains("messagePayload=LinkEventsInfo");
        Assertions.assertThat(linkServiceLogs).contains("eventName=SKIP_LOGIN_SUBMITTED");
        Assertions.assertThat(linkServiceLogs).contains("topic=LINK_EVENTS_DWH");
        Assertions.assertThat(linkServiceLogs).contains("traceId=LINK_EVENTS_DWH");
    }

    @Owner("Anushka_Goldi")
    @Feature("PGP-41056")
    @Parameters({"theme"})
    @Test(description = "Create a link and check the SKIP_LOGIN_COMPLETES_PAYMENT messagePayload in Kafka Logs")
    public void MessagePayload_15(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_SKIPLOGIN_ONLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPWithForms(user,paymentLink,"FIXED", "SKIPLOGIN", "20000000");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payByNBForNewFlow();
        cashierPage.waitUntilLoads();
        String eventsLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service_event,"SKIP_LOGIN_COMPLETES_PAYMENT");
        Assertions.assertThat(eventsLogs).contains(linkId);
        Assertions.assertThat(eventsLogs).contains("SKIP_LOGIN_COMPLETES_PAYMENT");
        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId);
        Assertions.assertThat(linkServiceLogs).contains(linkId);
        Assertions.assertThat(linkServiceLogs).contains("Sending link events info to kafka");
        Assertions.assertThat(linkServiceLogs).contains("messagePayload=LinkEventsInfo");
        Assertions.assertThat(linkServiceLogs).contains("eventName=SKIP_LOGIN_COMPLETES_PAYMENT");
        Assertions.assertThat(linkServiceLogs).contains("topic=LINK_EVENTS_DWH");
        Assertions.assertThat(linkServiceLogs).contains("traceId=LINK_EVENTS_DWH");
    }

    @Owner("Anushka_Goldi")
    @Feature("PGP-41056")
    @Test(description = "Create a link and check the INVALID_LINK_SCREEN_LOAD messagePayload in Kafka Logs")
    public void MessagePayload_16() throws Exception {
        String paymentLink = "https://pgp-automation.paytm.in/link/goldi/LL_39517";
        String linkId= "39517";
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.launchLoginPage(paymentLink);
        String grepcmds = "grep \"" + "\" /paytm/logs/linkService.log  | " +
                "grep \"" + linkId + "\" | grep \"Sending link events info to kafka\"";
        Awaitility.await().until(() -> LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.LINKSERVICE, grepcmds), linkServiceLogs -> !"".equals(linkServiceLogs));
        String linkServiceLogs = getLogsOnServer(ServerConfigProvider.SERVICE.LINKSERVICE, grepcmds);
        Assertions.assertThat(linkServiceLogs).contains(linkId);
        Assertions.assertThat(linkServiceLogs).contains("Sending link events info to kafka");
        Assertions.assertThat(linkServiceLogs).contains("messagePayload=LinkEventsInfo");
        Assertions.assertThat(linkServiceLogs).contains("eventName=INVALID_LINK_SCREEN_LOAD");
        Assertions.assertThat(linkServiceLogs).contains("topic=LINK_EVENTS_DWH");
        Assertions.assertThat(linkServiceLogs).contains("traceId=LINK_EVENTS_DWH");
    }

    @Owner("Anushka_Goldi")
    @Feature("PGP-41056")
    @Test(description = "Create a link and check the PAYMENT_FORM_OPENED messagePayload in Kafka Logs")
    public void MessagePayload_17() throws Exception {
        RestAssured.useRelaxedHTTPSValidation();
        setUserAndMId(Constants.MerchantType.LINK_ALLIN_ONE_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","200");
        createNewLink.setContext("body.templateId","12");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPWithForms(user,paymentLink,"FIXED","PAYMENTFORM","200");
        String eventsLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service_event,"PAYMENT_FORM_OPENED");
        Assertions.assertThat(eventsLogs).contains(linkId);
        Assertions.assertThat(eventsLogs).contains("PAYMENT_FORM_OPENED");
        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId);
        Assertions.assertThat(linkServiceLogs).contains(linkId);
        Assertions.assertThat(linkServiceLogs).contains("Sending link events info to kafka");
        Assertions.assertThat(linkServiceLogs).contains("messagePayload=LinkEventsInfo");
        Assertions.assertThat(linkServiceLogs).contains("eventName=PAYMENT_FORM_OPENED");
        Assertions.assertThat(linkServiceLogs).contains("topic=LINK_EVENTS_DWH");
        Assertions.assertThat(linkServiceLogs).contains("traceId=LINK_EVENTS_DWH");
    }
}
