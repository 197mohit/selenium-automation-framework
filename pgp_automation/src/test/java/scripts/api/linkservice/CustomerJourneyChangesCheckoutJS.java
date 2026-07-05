package scripts.api.linkservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.ServerConfigProvider;
import com.paytm.api.linkAPI.CreateNewLink;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.LogsValidationHelper;
import com.paytm.apphelpers.PG2LogsValidationHelper;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.LinkPaymentLoginPage;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.testng.Assert;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.time.LocalDateTime;

import static com.paytm.apphelpers.LogsValidationHelper.getLogsOnServer;


@Feature("PGP-38510")
public class CustomerJourneyChangesCheckoutJS extends PGPBaseTest {
    User user;
    String mid;

    void setUserAndMId(String merchant) throws Exception {
        user = userManager.getForWrite(Label.LINK);
        mid = merchant;
    }


    @Owner("Shashank Gupta")
    @Feature("PGP-39629")
    @Parameters({"theme"})
    @Test(description = "The pop-up disclaimer will be replaced by inline message as a safety tip on the cashier page")
    public void FixedPaymentLink_PPI1(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        RestAssured.useRelaxedHTTPSValidation();
        setUserAndMId(Constants.MerchantType.LINK_SD_PPI1.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","3000");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","Web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId,"LinkBSFIPaymentInitiateRequestBody");

//        String grepcmd = "grep \"" + "\" /paytm/logs/linkService.log | " +
//                "grep \"" + linkId + "\" | grep \"Payload for initiate transaction request\" | grep \"LinkBSFIPaymentInitiateRequestBody\" | grep \"extendInfo\"";
//
//        String linkServiceLogs = getLogsOnServer(ServerConfigProvider.SERVICE.LINKSERVICE, grepcmd);
        //System.out.println("Logs are "+linkServiceLogs);
        Assertions.assertThat(linkServiceLogs).contains("displayWarningMessage=true");

    }

    @Owner("Shashank Gupta")
    @Feature("PGP-39629")
    @Parameters({"theme"})
    @Test(description = "The pop-up disclaimer will be replaced by inline message as a safety tip on the cashier page")
    public void GenericPaymentLink_PPI1(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_SD_PPI1.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"GENERIC","3000");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"GENERIC","Web");

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId,"LinkBSFIPaymentInitiateRequestBody");

//        String grepcmd = "grep \"" + "\" /paytm/logs/linkService.log | " +
//                "grep \"" + linkId + "\" | grep \"Payload for initiate transaction request\" | grep \"LinkBSFIPaymentInitiateRequestBody\" | grep \"extendInfo\"";
//
//        String linkServiceLogs = getLogsOnServer(ServerConfigProvider.SERVICE.LINKSERVICE, grepcmd);
        Assertions.assertThat(linkServiceLogs).contains("displayWarningMessage=true");

    }

    @Owner("Shashank Gupta")
    @Feature("PGP-39629")
    @Parameters({"theme"})
    @Test(description = "The pop-up disclaimer will be replaced by inline message as a safety tip on the cashier page")
    public void InvoicePaymentLink_PPI1(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_SD_PPI1.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"INVOICE","3000");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"INVOICE","Web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId,"LinkBSFIPaymentInitiateRequestBody");

//        String grepcmd = "grep \"" + "\" /paytm/logs/linkService.log | " +
//                "grep \"" + linkId + "\" | grep \"Payload for initiate transaction request\" | grep \"LinkBSFIPaymentInitiateRequestBody\" | grep \"extendInfo\"";
//
//        String linkServiceLogs = getLogsOnServer(ServerConfigProvider.SERVICE.LINKSERVICE, grepcmd);
        Assertions.assertThat(linkServiceLogs).contains("displayWarningMessage=true");
    }


    @Owner("Shashank Gupta")
    @Feature("PGP-39629")
    @Parameters({"theme"})
    @Test(description = "The pop-up disclaimer will be replaced by inline message as a safety tip on the cashier page")
    public void FixedPaymentLink_PPI3(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_SD_PPI3.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","3000");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","Web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId,"LinkBSFIPaymentInitiateRequestBody");

//        String grepcmd = "grep \"" + "\" /paytm/logs/linkService.log | " +
//                "grep \"" + linkId + "\" | grep \"Payload for initiate transaction request\" | grep \"LinkBSFIPaymentInitiateRequestBody\" | grep \"extendInfo\"";
//
//        String linkServiceLogs = getLogsOnServer(ServerConfigProvider.SERVICE.LINKSERVICE, grepcmd);
        Assertions.assertThat(linkServiceLogs).contains("displayWarningMessage=true");//false in payment button tc

    }

    @Owner("Shashank Gupta")
    @Feature("PGP-39629")
    @Parameters({"theme"})
    @Test(description = "")
    public void GenericPaymentLink_PPI3(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_SD_PPI3.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"GENERIC","3000");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"GENERIC","Web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId,"LinkBSFIPaymentInitiateRequestBody");

//        String grepcmd = "grep \"" + "\" /paytm/logs/linkService.log | " +
//                "grep \"" + linkId + "\" | grep \"Payload for initiate transaction request\" | grep \"LinkBSFIPaymentInitiateRequestBody\" | grep \"extendInfo\"";
//
//        String linkServiceLogs = getLogsOnServer(ServerConfigProvider.SERVICE.LINKSERVICE, grepcmd);
        Assertions.assertThat(linkServiceLogs).contains("displayWarningMessage=true");

    }

    @Owner("Shashank Gupta")
    @Feature("PGP-39629")
    @Parameters({"theme"})
    @Test(description = "")
    public void InvoicePaymentLink_PPI3 (@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_SD_PPI3.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"INVOICE","3000");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"INVOICE","Web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId,"LinkBSFIPaymentInitiateRequestBody");

//        String grepcmd = "grep \"" + "\" /paytm/logs/linkService.log | " +
//                "grep \"" + linkId + "\" | grep \"Payload for initiate transaction request\" | grep \"LinkBSFIPaymentInitiateRequestBody\" | grep \"extendInfo\"";
//
//        String linkServiceLogs = getLogsOnServer(ServerConfigProvider.SERVICE.LINKSERVICE, grepcmd);
        Assertions.assertThat(linkServiceLogs).contains("displayWarningMessage=true");

    }
    @Owner("Shashank Gupta")
    @Feature("PGP-39629")
    @Parameters({"theme"})
    @Test(description = "The pop-up disclaimer will be replaced by inline message as a safety tip on the cashier page")
    public void FixedPaymentLinkUI_PPI1(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_SD_PPI1.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","3000");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","Web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        String safetyTip = cashierPage.safetyPopup().getText();
        String safetyMessage = cashierPage.safetyMessage().getText();
        Assert.assertEquals(safetyTip.contains("Safety Tip"),true);
        Assert.assertEquals(safetyMessage.contains("Please ensure you are paying only to a trusted merchant"),true);
        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId,"LinkBSFIPaymentInitiateRequestBody");

        Assertions.assertThat(linkServiceLogs).contains("displayWarningMessage=true");

    }
    @Owner("Shashank Gupta")
    @Feature("PGP-39629")
    @Parameters({"theme"})
    @Test(description = "The pop-up disclaimer will be replaced by inline message as a safety tip on the cashier page")
    public void GenericPaymentLinkUI_PPI1(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        RestAssured.useRelaxedHTTPSValidation();
        setUserAndMId(Constants.MerchantType.LINK_SD_PPI1.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"GENERIC","3000");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"GENERIC","Web");

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        String safetyTip = cashierPage.safetyPopup().getText();
        String safetyMessage = cashierPage.safetyMessage().getText();
        Assert.assertEquals(safetyTip.contains("Safety Tip"),true);
        Assert.assertEquals(safetyMessage.contains("Please ensure you are paying only to a trusted merchant"),true);
        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId,"LinkBSFIPaymentInitiateRequestBody");

//        String grepcmd = "grep \"" + "\" /paytm/logs/linkService.log | " +
//                "grep \"" + linkId + "\" | grep \"Payload for initiate transaction request\" | grep \"LinkBSFIPaymentInitiateRequestBody\" | grep \"extendInfo\"";
//
//        String linkServiceLogs = getLogsOnServer(ServerConfigProvider.SERVICE.LINKSERVICE, grepcmd);
        Assertions.assertThat(linkServiceLogs).contains("displayWarningMessage=true");

    }
    @Owner("Shashank Gupta")
    @Feature("PGP-39629")
    @Parameters({"theme"})
    @Test(description = "The pop-up disclaimer will be replaced by inline message as a safety tip on the cashier page")
    public void InvoicePaymentLinkUI_PPI1(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_SD_PPI1.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"INVOICE","3000");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"INVOICE","Web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        String safetyTip = cashierPage.safetyPopup().getText();
        String safetyMessage = cashierPage.safetyMessage().getText();
        Assert.assertEquals(safetyTip.contains("Safety Tip"),true);
        Assert.assertEquals(safetyMessage.contains("Please ensure you are paying only to a trusted merchant"),true);
        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId,"LinkBSFIPaymentInitiateRequestBody");


//        String grepcmd = "grep \"" + "\" /paytm/logs/linkService.log | " +
//                "grep \"" + linkId + "\" | grep \"Payload for initiate transaction request\" | grep \"LinkBSFIPaymentInitiateRequestBody\" | grep \"extendInfo\"";
//
//        String linkServiceLogs = getLogsOnServer(ServerConfigProvider.SERVICE.LINKSERVICE, grepcmd);
        Assertions.assertThat(linkServiceLogs).contains("displayWarningMessage=true");

    }
    @Owner("Shashank Gupta")
    @Feature("PGP-39629")
    @Parameters({"theme"})
    @Test(description = "The pop-up disclaimer will be replaced by inline message as a safety tip on the cashier page")
    public void FixedPaymentLinkUI_PPI3(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_SD_PPI3.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","3000");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","Web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        String safetyTip = cashierPage.safetyPopup().getText();
        String safetyMessage = cashierPage.safetyMessage().getText();
        Assert.assertEquals(safetyTip.contains("Safety Tip"),true);
        Assert.assertEquals(safetyMessage.contains("Please ensure you are paying only to a trusted merchant"),true);
        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId,"LinkBSFIPaymentInitiateRequestBody");

//        String grepcmd = "grep \"" + "\" /paytm/logs/linkService.log | " +
//                "grep \"" + linkId + "\" | grep \"Payload for initiate transaction request\" | grep \"LinkBSFIPaymentInitiateRequestBody\" | grep \"extendInfo\"";
//
//        String linkServiceLogs = getLogsOnServer(ServerConfigProvider.SERVICE.LINKSERVICE, grepcmd);
        Assertions.assertThat(linkServiceLogs).contains("displayWarningMessage=true");

    }
    @Owner("Shashank Gupta")
    @Feature("PGP-39629")
    @Parameters({"theme"})
    @Test(description = "")
    public void GenericPaymentLinkUI_PPI3(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_SD_PPI3.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"GENERIC","3000");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"GENERIC","Web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        String safetyTip = cashierPage.safetyPopup().getText();
        String safetyMessage = cashierPage.safetyMessage().getText();
        Assert.assertEquals(safetyTip.contains("Safety Tip"),true);
        Assert.assertEquals(safetyMessage.contains("Please ensure you are paying only to a trusted merchant"),true);
        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId,"LinkBSFIPaymentInitiateRequestBody");

//        String grepcmd = "grep \"" + "\" /paytm/logs/linkService.log | " +
//                "grep \"" + linkId + "\" | grep \"Payload for initiate transaction request\" | grep \"LinkBSFIPaymentInitiateRequestBody\" | grep \"extendInfo\"";
//
//        String linkServiceLogs = getLogsOnServer(ServerConfigProvider.SERVICE.LINKSERVICE, grepcmd);
        Assertions.assertThat(linkServiceLogs).contains("displayWarningMessage=true");

    }
    @Owner("Shashank Gupta")
    @Feature("PGP-39629")
    @Parameters({"theme"})
    @Test(description = "")
    public void InvoicePaymentLinkUI_PPI3 (@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_SD_PPI3.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"INVOICE","3000");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"INVOICE","Web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        String safetyTip = cashierPage.safetyPopup().getText();
        String safetyMessage = cashierPage.safetyMessage().getText();
        Assert.assertEquals(safetyTip.contains("Safety Tip"),true);
        Assert.assertEquals(safetyMessage.contains("Please ensure you are paying only to a trusted merchant"),true);
        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId,"LinkBSFIPaymentInitiateRequestBody");

//        String grepcmd = "grep \"" + "\" /paytm/logs/linkService.log | " +
//                "grep \"" + linkId + "\" | grep \"Payload for initiate transaction request\" | grep \"LinkBSFIPaymentInitiateRequestBody\" | grep \"extendInfo\"";
//
//        String linkServiceLogs = getLogsOnServer(ServerConfigProvider.SERVICE.LINKSERVICE, grepcmd);
        Assertions.assertThat(linkServiceLogs).contains("displayWarningMessage=true");
    }


}