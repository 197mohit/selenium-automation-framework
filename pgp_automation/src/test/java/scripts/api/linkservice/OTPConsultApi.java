package scripts.api.linkservice;
import com.paytm.ServerConfigProvider;
import com.paytm.api.linkAPI.CreateNewLink;
import com.paytm.appconstants.Constants;
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
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import static com.paytm.apphelpers.LogsValidationHelper.getLogsOnServer;

import com.paytm.base.test.PGPBaseTest;
@Feature("PGP-37787")
public class OTPConsultApi extends PGPBaseTest {
    User user;
    String mid;
    void setUserAndMId(String merchant) throws Exception {
        user = userManager.getForRead(Label.LINK);
        mid = merchant;
    }
    @Owner("Nirottam")
    @Feature("PGP-37787")
    @Parameters({"theme"})
    @Test(description = "verify linkId in Link Otp Consult API Request Body")
    public void GenerateTxnToken_001(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        RestAssured.useRelaxedHTTPSValidation();
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","20");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId,"LinkOTPConsultRequest");

//        String grepcmd = "grep \"" + "\" /paytm/logs/linkService.log | " +
//                "grep  \"Link otp consult request received\"";
//        String linkServiceLogs = getLogsOnServer(ServerConfigProvider.SERVICE.LINKSERVICE, grepcmd);
        Assertions.assertThat(linkServiceLogs).contains("linkId="+linkId);

    }
    @Owner("Nirottam")
    @Feature("PGP-37787")
    @Parameters({"theme"})
    @Test(description = "verify mobile No in Link Otp Consult API Request Body")
    public void GenerateTxnToken_002(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","20");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId,"LinkOTPConsultRequest");

//        String grepcmd = "grep \"" + "\" /paytm/logs/linkService.log | " +
//                "grep  \"Link otp consult request received\"";
//        String linkServiceLogs = getLogsOnServer(ServerConfigProvider.SERVICE.LINKSERVICE, grepcmd);
        Assertions.assertThat(linkServiceLogs).contains("mobileNumber");

    }
    @Owner("Nirottam")
    @Feature("PGP-37787")
    @Parameters({"theme"})
    @Test(description = "verify linkId in Link Otp Consult API Request Body")
    public void GenerateTxnToken_003(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","20");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId,"LinkOTPConsultRequest");

//        String grepcmd = "grep \"" + "\" /paytm/logs/linkService.log | " +
//                "grep  \"Link otp consult request received\"";
//        String linkServiceLogs = getLogsOnServer(ServerConfigProvider.SERVICE.LINKSERVICE, grepcmd);
        Assertions.assertThat(linkServiceLogs).contains("amount=20");

    }
    @Owner("Nirottam")
    @Feature("PGP-37787")
    @Parameters({"theme"})
    @Test(description = "verify  LinkOTPConsultRequestBody in Link Otp Consult  API Request Body")
    public void GenerateTxnToken_004(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","20");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,"Link otp consult request received");

        Assertions.assertThat(linkServiceLogs).contains("LinkOTPConsultRequestBody");

    }
    @Owner("Nirottam")
    @Feature("PGP-37787")
    @Parameters({"theme"})
    @Test(description = "verify linkId in Link Otp Consult API Request Body")
    public void GenerateTxnToken_005(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"GENERIC","2000");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"GENERIC","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId,"LinkOTPConsultRequest");

//        String grepcmd = "grep \"" + "\" /paytm/logs/linkService.log | " +
//                "grep  \"Link otp consult request received\"";
//        String linkServiceLogs = getLogsOnServer(ServerConfigProvider.SERVICE.LINKSERVICE, grepcmd);
        Assertions.assertThat(linkServiceLogs).contains("linkId="+linkId);
        Assertions.assertThat(linkServiceLogs).contains("mobileNumber");
        Assertions.assertThat(linkServiceLogs).contains("amount=2000");

    }
    @Owner("Nirottam")
    @Feature("PGP-37787")
    @Parameters({"theme"})
    @Test(description = "verify linkId in Link Otp Consult API Request Body")
    public void GenerateTxnToken_006(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"INVOICE","20");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"INVOICE","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId,"LinkOTPConsultRequest");

//        String grepcmd = "grep \"" + "\" /paytm/logs/linkService.log | " +
//                "grep  \"Link otp consult request received\"";
//        String linkServiceLogs = getLogsOnServer(ServerConfigProvider.SERVICE.LINKSERVICE, grepcmd);
        Assertions.assertThat(linkServiceLogs).contains("linkId="+linkId);
        Assertions.assertThat(linkServiceLogs).contains("mobileNumber");
        Assertions.assertThat(linkServiceLogs).contains("amount=20");

    }
    @Owner("Nirottam")
    @Feature("PGP-37787")
    @Parameters({"theme"})
    @Test(description = "verify Successfull Response of  Link Otp Consult Api")
    public void GenerateTxnToken_007(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        RestAssured.useRelaxedHTTPSValidation();
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","20");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId);

//        String grepcmd = "grep \"" + "\" /paytm/logs/linkService.log | " +
//                "grep  \"Response being sent back is\"";
//        String linkServiceLogs = getLogsOnServer(ServerConfigProvider.SERVICE.LINKSERVICE, grepcmd);
        Assertions.assertThat(linkServiceLogs).contains("\"resultStatus\":\"S\"");
        Assertions.assertThat(linkServiceLogs).contains("\"resultCode\":\"200\"");
        Assertions.assertThat(linkServiceLogs).contains("\"resultMsg\":\"Request Successfully Processed\"");
        Assertions.assertThat(linkServiceLogs).contains("\"linkDescription\":\"123\"");
        Assertions.assertThat(linkServiceLogs).contains("\"mid\":\"qa8Aut98589027264834\"");
        Assertions.assertThat(linkServiceLogs).contains("\"amount\":\"20.0\"");
        Assertions.assertThat(linkServiceLogs).contains("\"merchantName\":\"abhishek\"");
    }
    @Owner("Nirottam")
    @Feature("PGP-37787")
    @Parameters({"theme"})
    @Test(description = "verify Successfull Response of  Link Otp Consult Api")
    public void GenerateTxnToken_008(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"GENERIC","20");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"GENERIC","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId);

//        String grepcmd = "grep \"" + "\" /paytm/logs/linkService.log | " +
//                "grep  \"Response being sent back is\"";
//        String linkServiceLogs = getLogsOnServer(ServerConfigProvider.SERVICE.LINKSERVICE, grepcmd);
        Assertions.assertThat(linkServiceLogs).contains("\"resultStatus\":\"S\"");
        Assertions.assertThat(linkServiceLogs).contains("\"resultCode\":\"200\"");
        Assertions.assertThat(linkServiceLogs).contains("\"resultMsg\":\"Request Successfully Processed\"");
        Assertions.assertThat(linkServiceLogs).contains("\"linkDescription\":\"123\"");
        Assertions.assertThat(linkServiceLogs).contains("\"mid\":\"qa8Aut98589027264834\"");
        Assertions.assertThat(linkServiceLogs).contains("\"amount\":\"2000.0\"");
        Assertions.assertThat(linkServiceLogs).contains("\"merchantName\":\"abhishek\"");
    }
    @Owner("Nirottam")
    @Feature("PGP-37787")
    @Parameters({"theme"})
    @Test(description = "verify Successfull Response of  Link Otp Consult Api")
    public void GenerateTxnToken_009(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"INVOICE","20");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"INVOICE","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId);

//        String grepcmd = "grep \"" + "\" /paytm/logs/linkService.log | " +
//                "grep  \"Response being sent back is\"";
//        String linkServiceLogs = getLogsOnServer(ServerConfigProvider.SERVICE.LINKSERVICE, grepcmd);
        Assertions.assertThat(linkServiceLogs).contains("\"resultStatus\":\"S\"");
        Assertions.assertThat(linkServiceLogs).contains("\"resultCode\":\"200\"");
        Assertions.assertThat(linkServiceLogs).contains("\"resultMsg\":\"Request Successfully Processed\"");
        Assertions.assertThat(linkServiceLogs).contains("\"linkDescription\":\"123\"");
        Assertions.assertThat(linkServiceLogs).contains("\"mid\":\"qa8Aut98589027264834\"");
        Assertions.assertThat(linkServiceLogs).contains("\"amount\":\"20.0\"");
        Assertions.assertThat(linkServiceLogs).contains("\"merchantName\":\"abhishek\"");
    }
}
