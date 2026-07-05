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
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.time.LocalDateTime;

import static com.paytm.apphelpers.LogsValidationHelper.getLogsOnServer;

@Feature("PGP-37787")
public class initiateTxnApi extends PGPBaseTest {
    User user;
    String mid;
    void setUserAndMId(String merchant) throws Exception {
        user = userManager.getForWrite(Label.LINK);
        mid = merchant;
    }

    @Owner("Nirottam")
    @Feature("PGP-37787")
    @Parameters({"theme"})
    @Test(description = "verify ExtendInfo For FIXED LINK IN InitiateTxnToken API")
    public void Initiate_Txn_001(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        RestAssured.useRelaxedHTTPSValidation();
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","3000");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        String linkServiceLogs=LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId,"LinkBSFIPaymentInitiateRequestBody");

        Assertions.assertThat(linkServiceLogs).contains("linkCustomerEmail");
        Assertions.assertThat(linkServiceLogs).contains("linkCustomerMobile");
        Assertions.assertThat(linkServiceLogs).contains("linkCustomerName");
        Assertions.assertThat(linkServiceLogs).contains("search6");
        Assertions.assertThat(linkServiceLogs).contains("search4");
        Assertions.assertThat(linkServiceLogs).contains("search5");

    }
    @Owner("Nirottam")
    @Feature("PGP-37787")
    @Parameters({"theme"})
    @Test(description = "verify ExtendInfo For GENERIC LINK IN InitiateTxnToken API")
    public void Initiate_Txn_002(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        RestAssured.useRelaxedHTTPSValidation();
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"GENERIC","2000");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"GENERIC","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        String linkServiceLogs=LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId,"LinkBSFIPaymentInitiateRequestBody");

        Assertions.assertThat(linkServiceLogs).contains("linkCustomerEmail");
        Assertions.assertThat(linkServiceLogs).contains("linkCustomerMobile");
        Assertions.assertThat(linkServiceLogs).contains("linkCustomerName");
        Assertions.assertThat(linkServiceLogs).contains("search6");
        Assertions.assertThat(linkServiceLogs).contains("search4");
        Assertions.assertThat(linkServiceLogs).contains("search5");
    }
    @Owner("Nirottam")
    @Feature("PGP-37787")
    @Parameters({"theme"})
    @Test(description = "verify ExtendInfo For INVOICE LINK IN InitiateTxnToken API")
    public void Initiate_Txn_003(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        RestAssured.useRelaxedHTTPSValidation();
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"INVOICE","2000");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"INVOICE","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        String linkServiceLogs=LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId,"LinkBSFIPaymentInitiateRequestBody");


        Assertions.assertThat(linkServiceLogs).contains("linkCustomerEmail");
        Assertions.assertThat(linkServiceLogs).contains("linkCustomerMobile");
        Assertions.assertThat(linkServiceLogs).contains("linkCustomerName");
        Assertions.assertThat(linkServiceLogs).contains("search6");
        Assertions.assertThat(linkServiceLogs).contains("search4");
        Assertions.assertThat(linkServiceLogs).contains("search5");
    }
    @Owner("Nirottam")
    @Feature("PGP-37787")
    @Parameters({"theme"})
    @Test(description = "verify UserInfo For FIXED LINK IN InitiateTxnToken API")
    public void Initiate_Txn_004(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        RestAssured.useRelaxedHTTPSValidation();
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        String linkServiceLogs=LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId,"LinkBSFIPaymentInitiateRequestBody");

        Assertions.assertThat(linkServiceLogs).contains("mobile=7014107741");
        Assertions.assertThat(linkServiceLogs).contains("email=nirottam.singh@paytm.com");
        Assertions.assertThat(linkServiceLogs).contains("firstName=nirottam");
        Assertions.assertThat(linkServiceLogs).contains("lastName=null");
    }
    @Owner("Nirottam")
    @Feature("PGP-37787")
    @Parameters({"theme"})
    @Test(description = "verify mid For FIXED LINK IN InitiateTxnToken API")
    public void Initiate_Txn_005(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        RestAssured.useRelaxedHTTPSValidation();
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","2000");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        String linkServiceLogs=LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId,"LinkBSFIPaymentInitiateRequestBody");

        Assertions.assertThat(linkServiceLogs).contains("mid="+Constants.MerchantType.LINK_PGONLY.getId());

    }
    @Owner("Nirottam")
    @Feature("PGP-37787")
    @Parameters({"theme"})
    @Test(description = "verify txnAmount For FIXED LINK IN InitiateTxnToken API")
    public void Initiate_Txn_006(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        RestAssured.useRelaxedHTTPSValidation();
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","2000");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        String linkServiceLogs=LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId,"LinkBSFIPaymentInitiateRequestBody");


        Assertions.assertThat(linkServiceLogs).contains("currency=INR").contains(" value=2000");

    }
    @Owner("Nirottam")
    @Feature("PGP-37787")
    @Parameters({"theme"})
    @Test(description = "verify requestType For FIXED LINK IN InitiateTxnToken API")
    public void Initiate_Txn_007(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        RestAssured.useRelaxedHTTPSValidation();
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","2000");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        String linkServiceLogs=LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId,"LinkBSFIPaymentInitiateRequestBody");

        Assertions.assertThat(linkServiceLogs).contains(" requestType=LINK_BASED_PAYMENT");

    }
    @Owner("Nirottam")
    @Feature("PGP-37787")
    @Parameters({"theme"})
    @Test(description = "verify orderId For FIXED LINK IN InitiateTxnToken API")
    public void Initiate_Txn_008(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        RestAssured.useRelaxedHTTPSValidation();
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","2000");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        String linkServiceLogs=LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId,"LinkBSFIPaymentInitiateRequestBody");

        Assertions.assertThat(linkServiceLogs).contains("orderId").isNotNull();

    }
    @Owner("Nirottam")
    @Feature("PGP-37787")
    @Parameters({"theme"})
    @Test(description = "verify websiteName For FIXED LINK IN InitiateTxnToken API")
    public void Initiate_Txn_009(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        RestAssured.useRelaxedHTTPSValidation();
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","2000");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        String linkServiceLogs=LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId,"LinkBSFIPaymentInitiateRequestBody");

        Assertions.assertThat(linkServiceLogs).contains("websiteName").isNotNull();

    }
    @Owner("Nirottam")
    @Feature("PGP-37787")
    @Parameters({"theme"})
    @Test(description = "verify callbackUrl For FIXED LINK IN InitiateTxnToken API")
    public void Initiate_Txn_0010(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        RestAssured.useRelaxedHTTPSValidation();
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","2000");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        String linkServiceLogs=LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId,"LinkBSFIPaymentInitiateRequestBody");

        Assertions.assertThat(linkServiceLogs).contains("callbackUrl=https://pgp-ite.paytm.in/theia/linkPaymentRedirect");
    }
    @Owner("Nirottam")
    @Feature("PGP-37787")
    @Parameters({"theme"})
    @Test(description = "verify LinkDetailResponseBody For FIXED LINK IN InitiateTxnToken API")
    public void Initiate_Txn_0011(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        RestAssured.useRelaxedHTTPSValidation();
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","2000");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        String linkServiceLogs=LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId,"LinkBSFIPaymentInitiateRequestBody");

        Assertions.assertThat(linkServiceLogs).contains("LinkDetailResponseBody").isNotNull();

    }
    @Owner("Nirottam")
    @Feature("PGP-37787")
    @Parameters({"theme"})
    @Test(description = "verify LinkDetailResponseBody For FIXED LINK IN InitiateTxnToken API")
    public void Initiate_Txn_0012(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        RestAssured.useRelaxedHTTPSValidation();
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","2000");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        String linkServiceLogs=LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId,"LinkBSFIPaymentInitiateRequestBody");


        Assertions.assertThat(linkServiceLogs).contains("statusCallBackURL=https://example.test/test");
        Assertions.assertThat(linkServiceLogs).contains("resultInfo");
        Assertions.assertThat(linkServiceLogs).contains("resultCode=200");
        Assertions.assertThat(linkServiceLogs).contains("resultMsg=Request Successfully Processed");
        Assertions.assertThat(linkServiceLogs).contains("amount=2000.0");
        Assertions.assertThat(linkServiceLogs).contains("linkName=TestingLink");
        Assertions.assertThat(linkServiceLogs).contains("linkDescription=123");
        Assertions.assertThat(linkServiceLogs).contains("isRedirect");
        Assertions.assertThat(linkServiceLogs).contains("bankRetry");
        Assertions.assertThat(linkServiceLogs).contains(" retry");
        Assertions.assertThat(linkServiceLogs).contains("userRetryAllowed");
        Assertions.assertThat(linkServiceLogs).contains("linkNotes");
        Assertions.assertThat(linkServiceLogs).contains("paymentFormId");
    }
    @Owner("Nirottam")
    @Feature("PGP-37787")
    @Parameters({"theme"})
    @Test(description = "verify LinkPaymentRiskInfo For FIXED LINK IN InitiateTxnToken API")
    public void Initiate_Txn_0013(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        RestAssured.useRelaxedHTTPSValidation();
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","2000");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        String linkServiceLogs=LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId,"LinkBSFIPaymentInitiateRequestBody");

        Assertions.assertThat(linkServiceLogs).contains("LinkPaymentRiskInfo");

    }
    @Owner("Nirottam")
    @Feature("PGP-37787")
    @Parameters({"theme"})
    @Test(description = "verify LinkPaymentRiskInfo For FIXED LINK IN InitiateTxnToken API")
    public void Initiate_Txn_0014(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        RestAssured.useRelaxedHTTPSValidation();
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","2000");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");

        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        String linkServiceLogs=LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId,"LinkBSFIPaymentInitiateRequestBody");

        Assertions.assertThat(linkServiceLogs).contains("isLinkPaymentRequest=true");
        Assertions.assertThat(linkServiceLogs).contains("linkName=TestingLink");
        Assertions.assertThat(linkServiceLogs).contains("linkDescription=123");
        Assertions.assertThat(linkServiceLogs).contains("linkCreationTime");
        Assertions.assertThat(linkServiceLogs).contains("linkOriginLatitude");
        Assertions.assertThat(linkServiceLogs).contains("linkOriginLongitude");
        Assertions.assertThat(linkServiceLogs).contains("merchantLimit");
        Assertions.assertThat(linkServiceLogs).contains("linkType=FIXED");
        Assertions.assertThat(linkServiceLogs).contains("requestType=LinkType");
        Assertions.assertThat(linkServiceLogs).contains("linkOpenTime");
        Assertions.assertThat(linkServiceLogs).contains("linkAmount=200000");
        Assertions.assertThat(linkServiceLogs).contains("merchantMode");
        Assertions.assertThat(linkServiceLogs).contains("linkReferrerSite");
        Assertions.assertThat(linkServiceLogs).contains("resellerId");
        Assertions.assertThat(linkServiceLogs).contains("resellerName");
        Assertions.assertThat(linkServiceLogs).contains("linkId");
    }
    @Owner("Nirottam")
    @Feature("PGP-37787")
    @Parameters({"theme"})
    @Test(description = "verify Null paymentFormDetails when payment form is not attached For FIXED LINK IN InitiateTxnToken API")
    public void Initiate_Txn_0015(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        RestAssured.useRelaxedHTTPSValidation();
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","2000");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        String linkServiceLogs=LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId,"LinkBSFIPaymentInitiateRequestBody");

        Assertions.assertThat(linkServiceLogs).contains("paymentFormDetails=null");

    }
    @Owner("Nirottam")
    @Feature("PGP-37787")
    @Parameters({"theme"})
    @Test(description = "verify resellerId For FIXED LINK IN InitiateTxnToken API")
    public void Initiate_Txn_0016(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        RestAssured.useRelaxedHTTPSValidation();
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","2000");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        String linkServiceLogs=LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId,"LinkBSFIPaymentInitiateRequestBody");

        Assertions.assertThat(linkServiceLogs).contains("resellerId=null");
        Assertions.assertThat(linkServiceLogs).contains("resellerName=null");
        Assertions.assertThat(linkServiceLogs).contains("resellerName=null");

    }
    @Owner("Nirottam")
    @Feature("PGP-37787")
    @Parameters({"theme"})
    @Test(description = "verify resellerName For FIXED LINK IN InitiateTxnToken API")
    public void Initiate_Txn_0017(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        RestAssured.useRelaxedHTTPSValidation();
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","2000");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        String linkServiceLogs=LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId,"LinkBSFIPaymentInitiateRequestBody");

        Assertions.assertThat(linkServiceLogs).contains("resellerName=null");

    }
    @Owner("Nirottam")
    @Feature("PGP-37787")
    @Parameters({"theme"})
    @Test(description = "verify subRequestType For FIXED LINK IN InitiateTxnToken API")
    public void Initiate_Txn_0018(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        RestAssured.useRelaxedHTTPSValidation();
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","2000");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        String linkServiceLogs=LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId,"LinkBSFIPaymentInitiateRequestBody");

        Assertions.assertThat(linkServiceLogs).contains("subRequestType=LINK_BASED_PAYMENT");

    }
    @Owner("Nirottam")
    @Feature("PGP-37787")
    @Parameters({"theme"})
    @Test(description = "verify linkId For FIXED LINK IN InitiateTxnToken API")
    public void Initiate_Txn_0019(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        RestAssured.useRelaxedHTTPSValidation();
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","2000");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        String linkServiceLogs=LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId,"LinkBSFIPaymentInitiateRequestBody");

        Assertions.assertThat(linkServiceLogs).contains("linkId");

    }

    @Owner("Nirottam")
    @Feature("PGP-37787")
    @Parameters({"theme"})
    @Test(description = "verify linkName For FIXED LINK IN InitiateTxnToken API")
    public void Initiate_Txn_0022(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        RestAssured.useRelaxedHTTPSValidation();
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","2000");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        String linkServiceLogs=LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId,"LinkBSFIPaymentInitiateRequestBody");

        Assertions.assertThat(linkServiceLogs).contains("linkName=TestingLink");
    }
    @Owner("Nirottam")
    @Feature("PGP-37787")
    @Parameters({"theme"})
    @Test(description = "verify subRequestType for Mutual fund  transaction type IN InitiateTxnToken API")
    public void Initiate_Txn_0023(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_MF.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","20");
        createNewLink.setContext("body.transactionType","MUTUAL FUND");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLink(paymentLink,"FIXED");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();


        String grepcmd = "grep \"" + "\" /paytm/logs/linkService.log | " +
                "grep \"" + linkId+ "\" | grep \"Payload for initiate transaction request\" | grep \"LinkBSFIPaymentInitiateRequestBody\" ";
        String linkServiceLogs = getLogsOnServer(ServerConfigProvider.SERVICE.LINKSERVICE, grepcmd);
        Assertions.assertThat(linkServiceLogs).contains("subRequestType=NATIVE_MF");
    }
    @Owner("Nirottam")
    @Feature("PGP-37787")
    @Parameters({"theme"})
    @Test(description = "verify subRequestType for STOCK TRADE  transaction type IN InitiateTxnToken API")
    public void Initiate_Txn_0024(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_ST.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","20");
        createNewLink.setContext("body.transactionType","STOCK TRADE");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLink(paymentLink,"FIXED");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();


        String grepcmd = "grep \"" + "\" /paytm/logs/linkService.log | " +
                "grep \"" + linkId+ "\" | grep \"Payload for initiate transaction request\" | grep \"LinkBSFIPaymentInitiateRequestBody\" ";
        String linkServiceLogs = getLogsOnServer(ServerConfigProvider.SERVICE.LINKSERVICE, grepcmd);
        Assertions.assertThat(linkServiceLogs).contains("subRequestType=NATIVE_ST");
    }
    @Owner("Nirottam")
    @Feature("PGP-37787")
    @Parameters({"theme"})
    @Test(description = "verify extendInfo field for STOCK TRADE  transaction type IN InitiateTxnToken API")
    public void Initiate_Txn_0025(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_ST.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","20");
        createNewLink.setContext("body.transactionType","STOCK TRADE");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLink(paymentLink,"FIXED");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();


        String grepcmd = "grep \"" + "\" /paytm/logs/linkService.log | " +
                "grep \"" + linkId+ "\" | grep \"Payload for initiate transaction request\" | grep \"LinkBSFIPaymentInitiateRequestBody\" ";
        String linkServiceLogs = getLogsOnServer(ServerConfigProvider.SERVICE.LINKSERVICE, grepcmd);
        Assertions.assertThat(linkServiceLogs).contains("extendInfo=null");
    }
    @Owner("Nirottam")
    @Feature("PGP-37787")
    @Parameters({"theme"})
    @Test(description = "verify extend info for Mutual fund  transaction type IN InitiateTxnToken API")
    public void Initiate_Txn_0026(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_MF.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","20");
        createNewLink.setContext("body.transactionType","MUTUAL FUND");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLink(paymentLink,"FIXED");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();


        String grepcmd = "grep \"" + "\" /paytm/logs/linkService.log | " +
                "grep \"" + linkId+ "\" | grep \"Payload for initiate transaction request\" | grep \"LinkBSFIPaymentInitiateRequestBody\" ";
        String linkServiceLogs = getLogsOnServer(ServerConfigProvider.SERVICE.LINKSERVICE, grepcmd);
        Assertions.assertThat(linkServiceLogs).contains("extendInfo=null");
    }
    @Owner("Nirottam")
    @Feature("PGP-37787")
    @Parameters({"theme"})
    @Test(description = "verify  Fields for Mutual fund  IN InitiateTxnToken API")
    public void Initiate_Txn_0027(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_MF.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","20");
        createNewLink.setMFInfo("MUTUAL FUND","true","true","123123123123");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLink(paymentLink,"FIXED");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();


        String grepcmd = "grep \"" + "\" /paytm/logs/linkService.log | " +
                "grep \"" + linkId+ "\" | grep \"Payload for initiate transaction request\" | grep \"LinkBSFIPaymentInitiateRequestBody\" ";
        String linkServiceLogs = getLogsOnServer(ServerConfigProvider.SERVICE.LINKSERVICE, grepcmd);

        Assertions.assertThat(linkServiceLogs).contains("accountNumber=123123123123");
        Assertions.assertThat(linkServiceLogs).contains("bankAccountNumbers=[123123123123]");
        Assertions.assertThat(linkServiceLogs).contains("validateAccountNumber=true");
        Assertions.assertThat(linkServiceLogs).contains("allowUnverifiedAccount=true");
        Assertions.assertThat(linkServiceLogs).contains("additionalInfo");
        Assertions.assertThat(linkServiceLogs).contains("ref1=def");
        Assertions.assertThat(linkServiceLogs).contains("ref2=qwe");
        Assertions.assertThat(linkServiceLogs).contains("ref3=asdf");
        Assertions.assertThat(linkServiceLogs).contains("ref4=wed");
        Assertions.assertThat(linkServiceLogs).contains("subRequestType=NATIVE_MF");
    }
    @Owner("Nirottam")
    @Feature("PGP-37787")
    @Parameters({"theme"})
    @Test(description = "verify  Fields for STOCK TRADE transaction type IN InitiateTxnToken API")
    public void Initiate_Txn_0028(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_ST.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","20");
        createNewLink.setMFInfo("STOCK TRADE","true","true","123123123123");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLink(paymentLink,"FIXED");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();



        String grepcmd = "grep \"" + "\" /paytm/logs/linkService.log | " +
                "grep \"" + linkId+ "\" | grep \"Payload for initiate transaction request\" | grep \"LinkBSFIPaymentInitiateRequestBody\" ";
        String linkServiceLogs = getLogsOnServer(ServerConfigProvider.SERVICE.LINKSERVICE, grepcmd);
        Assertions.assertThat(linkServiceLogs).contains("accountNumber=123123123123");
        Assertions.assertThat(linkServiceLogs).contains("bankAccountNumbers=[123123123123]");
        Assertions.assertThat(linkServiceLogs).contains("validateAccountNumber=true");
        Assertions.assertThat(linkServiceLogs).contains("allowUnverifiedAccount=true");
        Assertions.assertThat(linkServiceLogs).contains("additionalInfo");
        Assertions.assertThat(linkServiceLogs).contains("ref1=def");
        Assertions.assertThat(linkServiceLogs).contains("ref2=qwe");
        Assertions.assertThat(linkServiceLogs).contains("ref3=asdf");
        Assertions.assertThat(linkServiceLogs).contains("ref4=wed");
        Assertions.assertThat(linkServiceLogs).contains("subRequestType=NATIVE_ST");
    }
    @Owner("Nirottam")
    @Feature("PGP-37787")
    @Parameters({"theme"})
    @Test(description = "verify InitiateTxn Payload For GENERIC LINK IN InitiateTxnToken API")
    public void Initiate_Txn_0029(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        RestAssured.useRelaxedHTTPSValidation();
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"GENERIC","200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"GENERIC","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        String linkServiceLogs=LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId,"LinkBSFIPaymentInitiateRequestBody");

        Assertions.assertThat(linkServiceLogs).contains("mobile=7014107741");
        Assertions.assertThat(linkServiceLogs).contains("email=nirottam.singh@paytm.com");
        Assertions.assertThat(linkServiceLogs).contains("firstName=nirottam");
        Assertions.assertThat(linkServiceLogs).contains("lastName=null");
        Assertions.assertThat(linkServiceLogs).contains("mid="+Constants.MerchantType.LINK_PGONLY.getId());
        Assertions.assertThat(linkServiceLogs).contains("currency=INR").contains(" value=2000");
        Assertions.assertThat(linkServiceLogs).contains(" requestType=LINK_BASED_PAYMENT");
        Assertions.assertThat(linkServiceLogs).contains("orderId");
        Assertions.assertThat(linkServiceLogs).contains("websiteName");
        Assertions.assertThat(linkServiceLogs).contains("statusCallBackURL=https://example.test/test");
        Assertions.assertThat(linkServiceLogs).contains("resultInfo");
        Assertions.assertThat(linkServiceLogs).contains("resultCode=200");
        Assertions.assertThat(linkServiceLogs).contains("resultMsg=Request Successfully Processed");
        Assertions.assertThat(linkServiceLogs).contains("amount=2000.0");
        Assertions.assertThat(linkServiceLogs).contains("linkName=TestingLink");
        Assertions.assertThat(linkServiceLogs).contains("linkDescription=123");
        Assertions.assertThat(linkServiceLogs).contains("isRedirect");
        Assertions.assertThat(linkServiceLogs).contains("bankRetry");
        Assertions.assertThat(linkServiceLogs).contains(" retry");
        Assertions.assertThat(linkServiceLogs).contains("userRetryAllowed");
        Assertions.assertThat(linkServiceLogs).contains("linkNotes");
        Assertions.assertThat(linkServiceLogs).contains("paymentFormId");
        Assertions.assertThat(linkServiceLogs).contains("isLinkPaymentRequest=true");
        Assertions.assertThat(linkServiceLogs).contains("linkName=TestingLink");
        Assertions.assertThat(linkServiceLogs).contains("linkDescription=123");
        Assertions.assertThat(linkServiceLogs).contains("linkCreationTime");
        Assertions.assertThat(linkServiceLogs).contains("linkOriginLatitude");
        Assertions.assertThat(linkServiceLogs).contains("linkOriginLongitude");
        Assertions.assertThat(linkServiceLogs).contains("merchantLimit");
        Assertions.assertThat(linkServiceLogs).contains("linkType=GENERIC");
        Assertions.assertThat(linkServiceLogs).contains("requestType=LinkType");
        Assertions.assertThat(linkServiceLogs).contains("linkOpenTime");
        Assertions.assertThat(linkServiceLogs).contains("merchantMode");
        Assertions.assertThat(linkServiceLogs).contains("linkReferrerSite");
        Assertions.assertThat(linkServiceLogs).contains("resellerId");
        Assertions.assertThat(linkServiceLogs).contains("resellerName");
        Assertions.assertThat(linkServiceLogs).contains("linkId");
    }
    @Owner("Nirottam")
    @Feature("PGP-37787")
    @Parameters({"theme"})
    @Test(description = "verify InitiateTxn Payload For INVOICE LINK IN InitiateTxnToken API")
    public void Initiate_Txn_0030(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        RestAssured.useRelaxedHTTPSValidation();
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"INVOICE","200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"INVOICE","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        String linkServiceLogs=LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId,"LinkBSFIPaymentInitiateRequestBody");

        Assertions.assertThat(linkServiceLogs).contains("mobile=7014107741");
        Assertions.assertThat(linkServiceLogs).contains("email=nirottam.singh@paytm.com");
        Assertions.assertThat(linkServiceLogs).contains("firstName=nirottam");
        Assertions.assertThat(linkServiceLogs).contains("lastName=null");
        Assertions.assertThat(linkServiceLogs).contains("mid="+Constants.MerchantType.LINK_PGONLY.getId());
        Assertions.assertThat(linkServiceLogs).contains("currency=INR");
        Assertions.assertThat(linkServiceLogs).contains(" value=200");
        Assertions.assertThat(linkServiceLogs).contains(" requestType=LINK_BASED_PAYMENT");
        Assertions.assertThat(linkServiceLogs).contains("orderId");
        Assertions.assertThat(linkServiceLogs).contains("websiteName");
        Assertions.assertThat(linkServiceLogs).contains("statusCallBackURL=https://example.test/test");
        Assertions.assertThat(linkServiceLogs).contains("resultInfo");
        Assertions.assertThat(linkServiceLogs).contains("resultCode=200");
        Assertions.assertThat(linkServiceLogs).contains("resultMsg=Request Successfully Processed");
        Assertions.assertThat(linkServiceLogs).contains("amount=200.0");
        Assertions.assertThat(linkServiceLogs).contains("linkName=TestingLink");
        Assertions.assertThat(linkServiceLogs).contains("linkDescription=123");
        Assertions.assertThat(linkServiceLogs).contains("isRedirect");
        Assertions.assertThat(linkServiceLogs).contains("bankRetry");
        Assertions.assertThat(linkServiceLogs).contains(" retry");
        Assertions.assertThat(linkServiceLogs).contains("userRetryAllowed");
        Assertions.assertThat(linkServiceLogs).contains("linkNotes");
        Assertions.assertThat(linkServiceLogs).contains("paymentFormId");
        Assertions.assertThat(linkServiceLogs).contains("isLinkPaymentRequest=true");
        Assertions.assertThat(linkServiceLogs).contains("linkName=TestingLink");
        Assertions.assertThat(linkServiceLogs).contains("linkDescription=123");
        Assertions.assertThat(linkServiceLogs).contains("linkCreationTime");
        Assertions.assertThat(linkServiceLogs).contains("linkOriginLatitude");
        Assertions.assertThat(linkServiceLogs).contains("linkOriginLongitude");
        Assertions.assertThat(linkServiceLogs).contains("merchantLimit");
        Assertions.assertThat(linkServiceLogs).contains("linkType=INVOICE");
        Assertions.assertThat(linkServiceLogs).contains("requestType=LinkType");
        Assertions.assertThat(linkServiceLogs).contains("linkOpenTime");
        Assertions.assertThat(linkServiceLogs).contains("merchantMode");
        Assertions.assertThat(linkServiceLogs).contains("linkReferrerSite");
        Assertions.assertThat(linkServiceLogs).contains("resellerId");
        Assertions.assertThat(linkServiceLogs).contains("resellerName");
        Assertions.assertThat(linkServiceLogs).contains("linkId");
    }
    @Owner("Nirottam")
    @Feature("PGP-37787")
    @Parameters({"theme"})
    @Test(description = "verify Successfull Hit  For INVOICE LINK IN InitiateTxnToken API")
    public void Initiate_Txn_0031(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        RestAssured.useRelaxedHTTPSValidation();
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"INVOICE","200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"INVOICE","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        String linkServiceLogs=LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId,"LinkBSFIPaymentInitiateRequestBody");

        Assertions.assertThat(linkServiceLogs).contains("resultMsg=Request Successfully Processed");

    }
    @Owner("Nirottam")
    @Feature("PGP-37787")
    @Parameters({"theme"})
    @Test(description = "verify Successfull Hit For FIXED LINK IN InitiateTxnToken API")
    public void Initiate_Txn_0032(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        RestAssured.useRelaxedHTTPSValidation();
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        String linkServiceLogs=LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId,"LinkBSFIPaymentInitiateRequestBody");

        Assertions.assertThat(linkServiceLogs).contains("resultMsg=Request Successfully Processed");

    }
    @Owner("Nirottam")
    @Feature("PGP-37787")
    @Parameters({"theme"})
    @Test(description = "verify Statcic Url  For INVOICE LINK IN InitiateTxnToken API")
    public void Initiate_Txn_0033(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        RestAssured.useRelaxedHTTPSValidation();
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"INVOICE","200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"INVOICE","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();

        String grepcmd = "grep \"" + "\" /paytm/logs/linkService.log | " +
                "grep \"https://pgp-automation.paytm.in/theia/api/v1/initiateTransaction\" ";


        String linkServiceLogs = getLogsOnServer(ServerConfigProvider.SERVICE.LINKSERVICE, grepcmd);
        Assertions.assertThat(linkServiceLogs).contains("\"staticUrl\":\"https://pgp-automation.paytm.in/theia/api/v1/initiateTransaction\"");

    }
    @Owner("Nirottam")
    @Feature("PGP-37787")
    @Parameters({"theme"})
    @Test(description = "verify Statcic Url For FIXED LINK IN InitiateTxnToken API")
    public void Initiate_Txn_0034(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        RestAssured.useRelaxedHTTPSValidation();
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();

        String grepcmd = "grep \"" + "\" /paytm/logs/linkService.log | " +
                "grep \"https://pgp-automation.paytm.in/theia/api/v1/initiateTransaction\" ";


        String linkServiceLogs = getLogsOnServer(ServerConfigProvider.SERVICE.LINKSERVICE, grepcmd);
        Assertions.assertThat(linkServiceLogs).contains("\"staticUrl\":\"https://pgp-automation.paytm.in/theia/api/v1/initiateTransaction\"");

    }
    @Owner("Nirottam")
    @Feature("PGP-37787")
    @Parameters({"theme"})
    @Test(description = "verify Success In Response Back For FIXED LINK IN InitiateTxnToken API")
    public void Initiate_Txn_0035(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        RestAssured.useRelaxedHTTPSValidation();
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        String linkServiceLogs=LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,"Initiate transaction Response ::");

        Assertions.assertThat(linkServiceLogs).contains("\"resultStatus\":\"S\"");

    }

}

