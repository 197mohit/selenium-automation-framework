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
import com.paytm.pages.ResponsePage;
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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.ServerConfigProvider;
import com.paytm.api.linkAPI.CreateNewLink;
import com.paytm.appconstants.Constants;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.LinkPaymentLoginPage;
import com.paytm.pages.ResponsePage;
import io.qameta.allure.Feature;

import io.qameta.allure.Owner;

import io.restassured.path.json.JsonPath;

import org.assertj.core.api.Assertions;

import org.testng.Assert;

import org.testng.annotations.Optional;

import org.testng.annotations.Parameters;

import org.testng.annotations.Test;

import java.time.LocalDateTime;

import static com.paytm.apphelpers.LogsValidationHelper.getLogsOnServer;


import static com.paytm.apphelpers.LogsValidationHelper.getLogsOnServer;

@Feature("PGP-37787")
public class GenerateTxnToken extends PGPBaseTest {
    User user;
    String mid;
    void setUserAndMId(String merchant) throws Exception {
        user = userManager.getForWrite(Label.LINK);
        mid = merchant;
    }

    @Owner("Nirottam")
    @Parameters({"theme"})
    @Test(description = "verify linkId in GeneratetxnToekn API Request Body",groups={"smoke","sanity","regression"})
    public void GenerateTxnToken_001(@Optional("enhancedweb_revamp") String theme) throws Exception {
        RestAssured.useRelaxedHTTPSValidation();
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","20");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId,"Generating txn token for request");

        Assertions.assertThat(linkServiceLogs).contains("linkId");
        Assertions.assertThat(linkId).isNotNull();

    }

    @Owner("Nirottam")
    @Parameters({"theme"})
    @Test(description = "verify Amount field in GenerateTxnToken Request Body",groups={"smoke","sanity","regression"})
    public void GenerateTxnToken_002(@Optional("enhancedweb_revamp") String theme) throws Exception {
        RestAssured.useRelaxedHTTPSValidation();
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","20");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId,"Generating txn token for request");

        Assertions.assertThat(linkServiceLogs).contains("amount=null");

    }

    @Owner("Nirottam")
    @Parameters({"theme"})
    @Test(description = "verify Amount field in GenerateTxnToken Request Body",groups={"smoke","sanity","regression"})
    public void GenerateTxnToken_008(@Optional("enhancedweb_revamp") String theme) throws Exception {
        RestAssured.useRelaxedHTTPSValidation();
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"GENERIC","20");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"GENERIC","web");
        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId,"Generating txn token for request");

        Assertions.assertThat(linkServiceLogs).contains("amount=2000.0")
                .contains("ssoToken")
                .contains("custId")
                .contains("channelId=WEB");

    }

    @Owner("Nirottam")
    @Parameters({"theme"})
    @Test(description = "verify Amount field in GenerateTxnToken Request Body",groups={"smoke","sanity","regression"})
    public void GenerateTxnToken_009(@Optional("enhancedweb_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"INVOICE","20");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"INVOICE","web");
        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId,"Generating txn token for request");

        Assertions.assertThat(linkServiceLogs).contains("amount=null")
                .contains("ssoToken")
                .contains("custId")
                .contains("channelId=WEB");

    }

    @Owner("Nirottam")
    @Parameters({"theme"})
    @Test(description = "verify SSO Token field in Generate Txn Token API Request Body",groups={"smoke","sanity","regression"})
    public void GenerateTxnToken_003(@Optional("enhancedweb_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","20");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId,"Generating txn token for request");

        Assertions.assertThat(linkServiceLogs).contains("ssoToken");

    }

    @Owner("Nirottam")
    @Parameters({"theme"})
    @Test(description = "verify custid and channelId field in GenerateTxnToekn Api Request Body",groups={"smoke","sanity","regression"})
    public void GenerateTxnToken_004(@Optional("enhancedweb_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","20");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId,"Generating txn token for request");

        Assertions.assertThat(linkServiceLogs).contains("custId")
       .contains("channelId=WEB");
    }

    @Owner("Nirottam")
    @Parameters({"theme"})
    @Test(description = "verify ResultInfo Field For FIXED LINK IN GenerateTxnToken API",groups={"smoke","sanity","regression"})
    public void GenerateTxnToken_006(@Optional("enhancedweb_revamp") String theme) throws Exception {
        RestAssured.useRelaxedHTTPSValidation();
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","20");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,"Response being sent back is :");

        Assertions.assertThat(linkServiceLogs).contains("\"resultStatus\":\"SUCCESS\"");
        Assertions.assertThat(linkServiceLogs).contains("\"resultCode\":\"200\"");
        Assertions.assertThat(linkServiceLogs).contains("\"resultMessage\":\"SUCCESS\"");
        Assertions.assertThat(linkServiceLogs).contains("txnToken");
        Assertions.assertThat(linkServiceLogs).contains("orderId");
    }

    @Owner("Nirottam")
    @Parameters({"theme"})
    @Test(description = "verify Txn token generated successfully for order id For FIXED LINK IN GenerateTxnToken API",groups={"smoke","sanity","regression"})
    public void GenerateTxnToken_007(@Optional("enhancedweb_revamp") String theme) throws Exception {
        RestAssured.useRelaxedHTTPSValidation();
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","20");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,"Txn token generated successfully for order id");

        Assertions.assertThat(linkServiceLogs).contains("Txn token generated successfully for order id");
    }
    @Owner("Nirottam")
    @Parameters({"theme"})
    @Test(description = "verify ResultInfo Field For INVOICE LINK IN GenerateTxnToken API",groups={"smoke","sanity","regression"})
    public void GenerateTxnToken_0010(@Optional("enhancedweb_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"INVOICE","20");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"INVOICE","web");
        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,"Response being sent back is :");

        Assertions.assertThat(linkServiceLogs).contains("\"resultStatus\":\"SUCCESS\"")
        .contains("\"resultCode\":\"200\"")
        .contains("\"resultMessage\":\"SUCCESS\"")
        .contains("txnToken")
       .contains("orderId");
    }

    @Owner("Nirottam")
    @Parameters({"theme"})
    @Test(description = "verify ResultInfo Field For INVOICE LINK IN GenerateTxnToken API",groups={"smoke","sanity","regression"})
    public void GenerateTxnToken_0011(@Optional("enhancedweb_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"GENERIC","20");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"GENERIC","web");
        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,"Response being sent back is :");

        Assertions.assertThat(linkServiceLogs).contains("\"resultStatus\":\"SUCCESS\"")
        .contains("\"resultCode\":\"200\"")
        .contains("\"resultMessage\":\"SUCCESS\"")
        .contains("txnToken")
        .contains("orderId");
    }
}
