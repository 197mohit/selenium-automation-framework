package scripts.api.linkservice;

import com.paytm.apphelpers.LogsValidationHelper;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.ServerConfigProvider;
import com.paytm.api.linkAPI.CreateNewLink;
import com.paytm.appconstants.Constants;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.LinkPaymentLoginPage;
import com.paytm.pages.ResponsePage;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.awaitility.Duration;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import static com.paytm.apphelpers.LogsValidationHelper.getLogsOnServer;
@Feature("PGP-37787")
public class UpdateStatusApi extends PGPBaseTest {
    User user;
    String mid;
    void setUserAndMId(String merchant) throws Exception {
        user = userManager.getForWrite(Label.LINK);
        mid = merchant;
    }
    @Owner("Nirottam")
    @Feature("PGP-37787")
    @Parameters({"theme"})
    @Test(description = "verify Payload for Update Status Api Request Body for Fixed Link")
    public void UpdateStatus_001(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payByNBForNewFlow();
         String grepcmd = "grep \"" + "\" /paytm/logs/linkService.log  | " +
                "grep \"" + linkId + "\" | grep \"success in updating status for request\"";
        Awaitility.await().until(() -> LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.LINKSERVICE, grepcmd), linkServiceLogs -> !"".equals(linkServiceLogs));
        String linkServiceLogs = getLogsOnServer(ServerConfigProvider.SERVICE.LINKSERVICE, grepcmd);
        Assertions.assertThat(linkServiceLogs).contains("linkId");
        Assertions.assertThat(linkServiceLogs).contains("status=SUCCESS");
        Assertions.assertThat(linkServiceLogs).contains("orderId");
        Assertions.assertThat(linkServiceLogs).contains("amount=200.0");
        Assertions.assertThat(linkServiceLogs).contains("merchantLinkDetails");
        Assertions.assertThat(linkServiceLogs).contains("longUrl");
        Assertions.assertThat(linkServiceLogs).contains("shortUrl");
        Assertions.assertThat(linkServiceLogs).contains("merchantId");
        Assertions.assertThat(linkServiceLogs).contains("linkDescription='123'");
        Assertions.assertThat(linkServiceLogs).contains("linkName='TestingLink'");
        Assertions.assertThat(linkServiceLogs).contains("linkType='FIXED'");
        Assertions.assertThat(linkServiceLogs).contains("amount=200.0");
        Assertions.assertThat(linkServiceLogs).contains("paymentStatus='PENDING");
        Assertions.assertThat(linkServiceLogs).contains("successfulPaymentCount=1");
        Assertions.assertThat(linkServiceLogs).contains("customerId=3454");
        Assertions.assertThat(linkServiceLogs).contains("totalAmount=200.0");
        Assertions.assertThat(linkServiceLogs).contains("customerContact");
        String grepcmds = "grep \"" + "\" /paytm/logs/linkService.log | " +
                "grep  \"NOTI_QUEUE_HANDLER\" | grep  \"Response being sent back is\"";
         linkServiceLogs = getLogsOnServer(ServerConfigProvider.SERVICE.LINKSERVICE, grepcmds);
        Assertions.assertThat(linkServiceLogs).contains("\"resultStatus\":\"SUCCESS\"")
                .contains("\"resultCode\":\"200\"")
                .contains("\"resultMessage\":\"SUCCESS\"");
    }
    @Owner("Nirottam")
    @Feature("PGP-37787")
    @Parameters({"theme"})
    @Test(description = "verify Payload for Update Status Api Request Body For Generic Link")
    public void UpdateStatus_002(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"GENERIC","200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"GENERIC","web");

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payByNBForNewFlow();

        String grepcmd = "grep \"" + "\" /paytm/logs/linkService.log  | " +
                "grep \"" + linkId + "\" | grep \"success in updating status for request\"";
        Awaitility.await().until(() -> LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.LINKSERVICE, grepcmd), linkServiceLogs -> !"".equals(linkServiceLogs));
        String linkServiceLogs = getLogsOnServer(ServerConfigProvider.SERVICE.LINKSERVICE, grepcmd);
        Assertions.assertThat(linkServiceLogs).contains("linkId");
        Assertions.assertThat(linkServiceLogs).contains("status=SUCCESS");
        Assertions.assertThat(linkServiceLogs).contains("orderId");
        Assertions.assertThat(linkServiceLogs).contains("amount=2000.0");
        Assertions.assertThat(linkServiceLogs).contains("merchantLinkDetails");
        Assertions.assertThat(linkServiceLogs).contains("longUrl");
        Assertions.assertThat(linkServiceLogs).contains("shortUrl");
        Assertions.assertThat(linkServiceLogs).contains("merchantId");
        Assertions.assertThat(linkServiceLogs).contains("linkDescription='123'");
        Assertions.assertThat(linkServiceLogs).contains("linkName='TestingLink'");
        Assertions.assertThat(linkServiceLogs).contains("linkType='GENERIC'");
        Assertions.assertThat(linkServiceLogs).contains("amount=2000.0");
        Assertions.assertThat(linkServiceLogs).contains("paymentStatus='PENDING");
        Assertions.assertThat(linkServiceLogs).contains("successfulPaymentCount=1");
        Assertions.assertThat(linkServiceLogs).contains("customerId=3454");
        Assertions.assertThat(linkServiceLogs).contains("totalAmount=2000.0");
        Assertions.assertThat(linkServiceLogs).contains("customerContact");
        String grepcmds = "grep \"" + "\" /paytm/logs/linkService.log | " +
                "grep  \"NOTI_QUEUE_HANDLER\" | grep  \"Response being sent back is\"";
        linkServiceLogs = getLogsOnServer(ServerConfigProvider.SERVICE.LINKSERVICE, grepcmds);
        Assertions.assertThat(linkServiceLogs).contains("\"resultStatus\":\"SUCCESS\"")
                .contains("\"resultCode\":\"200\"")
                .contains("\"resultMessage\":\"SUCCESS\"");

    }
    @Owner("Nirottam")
    @Feature("PGP-37787")
    @Parameters({"theme"})
    @Test(description = "verify Payload for Update Status Api Request Body For INVOICE Link")
    public void UpdateStatus_003(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"INVOICE","200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"INVOICE","web");

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payByNBForNewFlow();
        String grepcmd = "grep \"" + "\" /paytm/logs/linkService.log  | " +
                "grep \"" + linkId + "\" | grep \"success in updating status for request\"";
        Awaitility.await().until(() -> LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.LINKSERVICE, grepcmd), linkServiceLogs -> !"".equals(linkServiceLogs));
        String linkServiceLogs = getLogsOnServer(ServerConfigProvider.SERVICE.LINKSERVICE, grepcmd);
        Assertions.assertThat(linkServiceLogs).contains("linkId");
        Assertions.assertThat(linkServiceLogs).contains("status=SUCCESS");
        Assertions.assertThat(linkServiceLogs).contains("orderId");
        Assertions.assertThat(linkServiceLogs).contains("amount=200.0");
        Assertions.assertThat(linkServiceLogs).contains("merchantLinkDetails");
        Assertions.assertThat(linkServiceLogs).contains("longUrl");
        Assertions.assertThat(linkServiceLogs).contains("shortUrl");
        Assertions.assertThat(linkServiceLogs).contains("merchantId");
        Assertions.assertThat(linkServiceLogs).contains("linkDescription='123'");
        Assertions.assertThat(linkServiceLogs).contains("linkName='TestingLink'");
        Assertions.assertThat(linkServiceLogs).contains("linkType='INVOICE'");
        Assertions.assertThat(linkServiceLogs).contains("amount=200.0");
        Assertions.assertThat(linkServiceLogs).contains("paymentStatus='INIT");
        Assertions.assertThat(linkServiceLogs).contains("successfulPaymentCount=1");
        Assertions.assertThat(linkServiceLogs).contains("customerId=3454");
        Assertions.assertThat(linkServiceLogs).contains("amount=200.0");
        Assertions.assertThat(linkServiceLogs).contains("customerContact");
        String grepcmds = "grep \"" + "\" /paytm/logs/linkService.log | " +
                "grep  \"NOTI_QUEUE_HANDLER\" | grep  \"Response being sent back is\"";
        linkServiceLogs = getLogsOnServer(ServerConfigProvider.SERVICE.LINKSERVICE, grepcmds);
        Assertions.assertThat(linkServiceLogs).contains("\"resultStatus\":\"SUCCESS\"")
                .contains("\"resultCode\":\"200\"")
                .contains("\"resultMessage\":\"SUCCESS\"");
    }
    @Owner("Nirottam")
    @Feature("PGP-37787")
    @Parameters({"theme"})
    @Test(description = "verify UpdateLinkStatusResponseBody for Fixed Link")
    public void UpdateStatus_004(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payByNBForNewFlow();
        String grepcmd = "grep \"" + "\" /paytm/logs/linkService.log  | " +
                "grep \"updateLinkStatus Merchant Link response UpdateLinkStatusResponse\"";

        Awaitility.await().until(() -> LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.LINKSERVICE, grepcmd), linkServiceLogs -> !"".equals(linkServiceLogs));
        String linkServiceLogs = getLogsOnServer(ServerConfigProvider.SERVICE.LINKSERVICE, grepcmd);
        Assertions.assertThat(linkServiceLogs).contains("UpdateLinkStatusResponseBody()");

    }
    @Owner("Nirottam")
    @Feature("PGP-37787")
    @Parameters({"theme"})
    @Test(description = "verify UpdateLinkStatusResponseBody for GENERIC LINK")
    public void UpdateStatus_005(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"GENERIC","200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"GENERIC","web");


        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payByNBForNewFlow();
        String grepcmd = "grep \"" + "\" /paytm/logs/linkService.log  | " +
                "grep \"updateLinkStatus Merchant Link response UpdateLinkStatusResponse\"";
        Awaitility.await().until(() -> LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.LINKSERVICE, grepcmd), linkServiceLogs -> !"".equals(linkServiceLogs));
        String linkServiceLogs = getLogsOnServer(ServerConfigProvider.SERVICE.LINKSERVICE, grepcmd);
        Assertions.assertThat(linkServiceLogs).contains("UpdateLinkStatusResponseBody()");

    }
    @Owner("Nirottam")
    @Feature("PGP-37787")
    @Parameters({"theme"})
    @Test(description = "verify UpdateLinkStatusResponseBody for INVOICE Link")
    public void UpdateStatus_006(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"INVOICE","200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"INVOICE","web");


        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payByNBForNewFlow();
        String grepcmd = "grep \"" + "\" /paytm/logs/linkService.log  | " +
                "grep \"updateLinkStatus Merchant Link response UpdateLinkStatusResponse\"";
        Awaitility.await().until(() -> LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.LINKSERVICE, grepcmd), linkServiceLogs -> !"".equals(linkServiceLogs));
        String linkServiceLogs = getLogsOnServer(ServerConfigProvider.SERVICE.LINKSERVICE, grepcmd);
        Assertions.assertThat(linkServiceLogs).contains("UpdateLinkStatusResponseBody()");

    }

}
