package scripts.api.linkservice;

import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.ServerConfigProvider;
import com.paytm.api.linkAPI.CreateNewLink;
import com.paytm.appconstants.Constants;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.LinkPaymentLoginPage;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import static com.paytm.apphelpers.LogsValidationHelper.getLogsOnServer;
@Feature("PGP-37787")
public class RiskConsultApi extends PGPBaseTest {
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
    public void Risk_Consult_001(@Optional("enhancedweb_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();

        String grepcmd = "grep \"" + "\" /paytm/logs/linkService.log  | " +
                "grep \"" + linkId + "\" | grep \"FLUXNET_ASYNC_CONSULT\" | grep \"REQUEST\" ";
        String linkServiceLogs = getLogsOnServer(ServerConfigProvider.SERVICE.LINKSERVICE, grepcmd);
        Assertions.assertThat(linkServiceLogs).contains("extendInfo");
        Assertions.assertThat(linkServiceLogs).contains("\\\"operationOrigin\\\":\\\"WEB\\\"");
        Assertions.assertThat(linkServiceLogs).contains("\\\"gmtOccur\\\"");
        Assertions.assertThat(linkServiceLogs).contains("\\\"timeZone\\\":\\\"IST\\\"");
        Assertions.assertThat(linkServiceLogs).contains("\\\"isLinkBasedPayment\\\":\\\"TRUE\\\"");
        Assertions.assertThat(linkServiceLogs).contains("\\\"linkCreationTime\\\"");
        Assertions.assertThat(linkServiceLogs).contains("\\\"userMerchant\\\"");
        Assertions.assertThat(linkServiceLogs).contains("\\\"linkReferrerSite\\\":null");
        Assertions.assertThat(linkServiceLogs).contains("\\\"linkId\\\"");
        Assertions.assertThat(linkServiceLogs).contains("\\\"eventAmount\\\":\\\"20000\\\"");
        Assertions.assertThat(linkServiceLogs).contains("\\\"linkDescription\\\":\\\"123\\\"");
        Assertions.assertThat(linkServiceLogs).contains("\\\"merchantMode\\\":\\\"ONLINE\\\"");
        Assertions.assertThat(linkServiceLogs).contains("\\\"paytmMerchantId\\\":\\\"linksO38457083060859\\\"");
        Assertions.assertThat(linkServiceLogs).contains("\\\"operationType\\\":\\\"OPEN_LINK\\\"");
        Assertions.assertThat(linkServiceLogs).contains("\\\"linkAmount\\\":\\\"20000\\\"");
        Assertions.assertThat(linkServiceLogs).contains("\\\"LinkType\\\":\\\"FIXED\\\"");
        Assertions.assertThat(linkServiceLogs).contains("\\\"linkOpenTime\\\"");
        Assertions.assertThat(linkServiceLogs).contains("\\\"RequestType\\\":\\\"LinkPayment\\\"");
        Assertions.assertThat(linkServiceLogs).contains("\\\"operationResult\\\":\\\"true\\\"");
        Assertions.assertThat(linkServiceLogs).contains("\"signature\"");
        Assertions.assertThat(linkServiceLogs).contains("\"bizScene\":\"OPEN_LINK_RESULT\"");
        Assertions.assertThat(linkServiceLogs).contains("\"envInfo\"");
        Assertions.assertThat(linkServiceLogs).contains("osType");
        Assertions.assertThat(linkServiceLogs).contains("\"terminalType\":\"WEB\"");
        Assertions.assertThat(linkServiceLogs).contains("\"clientSecret\":\"ifUJTbL6DnHwYU2xumZ9EEOmCm75wub5\"");
        Assertions.assertThat(linkServiceLogs).contains("\"reqMsgId\"");
    }
    @Owner("Nirottam")
    @Feature("PGP-37787")
    @Parameters({"theme"})
    @Test(description = "verify linkId in Link Otp Consult API Request Body")
    public void Risk_Consult_002(@Optional("enhancedweb_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"INVOICE","200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"INVOICE","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();

        String grepcmd = "grep \"" + "\" /paytm/logs/linkService.log  | " +
                "grep \"" + linkId + "\" | grep \"FLUXNET_ASYNC_CONSULT\" | grep \"REQUEST\" ";
        String linkServiceLogs = getLogsOnServer(ServerConfigProvider.SERVICE.LINKSERVICE, grepcmd);
        Assertions.assertThat(linkServiceLogs).contains("extendInfo");
        Assertions.assertThat(linkServiceLogs).contains("\\\"operationOrigin\\\":\\\"WEB\\\"");
        Assertions.assertThat(linkServiceLogs).contains("\\\"gmtOccur\\\"");
        Assertions.assertThat(linkServiceLogs).contains("\\\"timeZone\\\":\\\"IST\\\"");
        Assertions.assertThat(linkServiceLogs).contains("\\\"isLinkBasedPayment\\\":\\\"TRUE\\\"");
        Assertions.assertThat(linkServiceLogs).contains("\\\"linkCreationTime\\\"");
        Assertions.assertThat(linkServiceLogs).contains("\\\"userMerchant\\\"");
        Assertions.assertThat(linkServiceLogs).contains("\\\"linkReferrerSite\\\":null");
        Assertions.assertThat(linkServiceLogs).contains("\\\"linkId\\\"");
        Assertions.assertThat(linkServiceLogs).contains("\\\"eventAmount\\\":\\\"20000\\\"");
        Assertions.assertThat(linkServiceLogs).contains("\\\"linkDescription\\\":\\\"123\\\"");
        Assertions.assertThat(linkServiceLogs).contains("\\\"merchantMode\\\":\\\"ONLINE\\\"");
        Assertions.assertThat(linkServiceLogs).contains("\\\"paytmMerchantId\\\":\\\"linksO38457083060859\\\"");
        Assertions.assertThat(linkServiceLogs).contains("\\\"operationType\\\":\\\"OPEN_LINK\\\"");
        Assertions.assertThat(linkServiceLogs).contains("\\\"linkAmount\\\":\\\"20000\\\"");
        Assertions.assertThat(linkServiceLogs).contains("\\\"LinkType\\\":\\\"INVOICE\\\"");
        Assertions.assertThat(linkServiceLogs).contains("\\\"linkOpenTime\\\"");
        Assertions.assertThat(linkServiceLogs).contains("\\\"RequestType\\\":\\\"LinkPayment\\\"");
        Assertions.assertThat(linkServiceLogs).contains("\\\"operationResult\\\":\\\"true\\\"");
        Assertions.assertThat(linkServiceLogs).contains("\"signature\"");
        Assertions.assertThat(linkServiceLogs).contains("\"bizScene\":\"OPEN_LINK_RESULT\"");
        Assertions.assertThat(linkServiceLogs).contains("\"envInfo\"");
        Assertions.assertThat(linkServiceLogs).contains("osType");
        Assertions.assertThat(linkServiceLogs).contains("\"terminalType\":\"WEB\"");
        Assertions.assertThat(linkServiceLogs).contains("\"clientSecret\":\"ifUJTbL6DnHwYU2xumZ9EEOmCm75wub5\"");
        Assertions.assertThat(linkServiceLogs).contains("\"reqMsgId\"");
    }
    @Owner("Nirottam")
    @Feature("PGP-37787")
    @Parameters({"theme"})
    @Test(description = "verify Parameters in Risk Consult API Request Body")
    public void Risk_Consult_003(@Optional("enhancedweb_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"GENERIC","200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"GENERIC","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();

        String grepcmd = "grep \"" + "\" /paytm/logs/linkService.log  | " +
                "grep \"" + linkId + "\" | grep \"FLUXNET_ASYNC_CONSULT\" | grep \"REQUEST\" ";

        String linkServiceLogs = getLogsOnServer(ServerConfigProvider.SERVICE.LINKSERVICE, grepcmd);
        System.out.println("logs are..."+linkServiceLogs);
        Assertions.assertThat(linkServiceLogs).contains("extendInfo");
        Assertions.assertThat(linkServiceLogs).contains("\\\"operationOrigin\\\":\\\"WEB\\\"");
        Assertions.assertThat(linkServiceLogs).contains("\\\"gmtOccur\\\"");
        Assertions.assertThat(linkServiceLogs).contains("\\\"timeZone\\\":\\\"IST\\\"");
        Assertions.assertThat(linkServiceLogs).contains("\\\"isLinkBasedPayment\\\":\\\"TRUE\\\"");
        Assertions.assertThat(linkServiceLogs).contains("\\\"linkCreationTime\\\"");
        Assertions.assertThat(linkServiceLogs).contains("\\\"userMerchant\\\"");
        Assertions.assertThat(linkServiceLogs).contains("\\\"linkReferrerSite\\\":null");
        Assertions.assertThat(linkServiceLogs).contains("\\\"linkId\\\"");
        Assertions.assertThat(linkServiceLogs).contains("\\\"linkDescription\\\":\\\"123\\\"");
        Assertions.assertThat(linkServiceLogs).contains("\\\"merchantMode\\\":\\\"ONLINE\\\"");
        Assertions.assertThat(linkServiceLogs).contains("\\\"paytmMerchantId\\\":\\\"linksO38457083060859\\\"");
        Assertions.assertThat(linkServiceLogs).contains("\\\"operationType\\\":\\\"OPEN_LINK\\\"");
        Assertions.assertThat(linkServiceLogs).contains("\\\"LinkType\\\":\\\"GENERIC\\\"");
        Assertions.assertThat(linkServiceLogs).contains("\\\"linkOpenTime\\\"");
        Assertions.assertThat(linkServiceLogs).contains("\\\"RequestType\\\":\\\"LinkPayment\\\"");
        Assertions.assertThat(linkServiceLogs).contains("\\\"operationResult\\\":\\\"true\\\"");
        Assertions.assertThat(linkServiceLogs).contains("\"signature\"");
        Assertions.assertThat(linkServiceLogs).contains("\"bizScene\":\"OPEN_LINK_RESULT\"");
        Assertions.assertThat(linkServiceLogs).contains("\"envInfo\"");
        Assertions.assertThat(linkServiceLogs).contains("osType");
        Assertions.assertThat(linkServiceLogs).contains("\"terminalType\":\"WEB\"");
        Assertions.assertThat(linkServiceLogs).contains("\"clientSecret\":\"ifUJTbL6DnHwYU2xumZ9EEOmCm75wub5\"");
        Assertions.assertThat(linkServiceLogs).contains("\"reqMsgId\"");
    }
    @Owner("Nirottam")
    @Feature("PGP-37787")
    @Parameters({"theme"})
    @Test(description = "verify Successfull response in RiskConsult API")
    public void Risk_Consult_004(@Optional("enhancedweb_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();

        String grepcmd = "grep \"" + "\" /paytm/logs/linkService.log  | " +
                "grep \"FLUXNET_ASYNC_CONSULT\" | grep \"RESPONSE\" ";
        String linkServiceLogs = getLogsOnServer(ServerConfigProvider.SERVICE.LINKSERVICE, grepcmd);
        Assertions.assertThat(linkServiceLogs).contains("\"resultCode\":\"SUCCESS\"");
        Assertions.assertThat(linkServiceLogs).contains("\"resultMsg\":\"SUCCESS\"");

    }
    @Owner("Nirottam")
    @Feature("PGP-37787")
    @Parameters({"theme"})
    @Test(description = "verify Successfull response in Risk Consult API ")
    public void Risk_Consult_005(@Optional("enhancedweb_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"GENERIC","200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"GENERIC","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();

        String grepcmd = "grep \"" + "\" /paytm/logs/linkService.log  | " +
                "grep \"FLUXNET_ASYNC_CONSULT\" | grep \"RESPONSE\" ";
        String linkServiceLogs = getLogsOnServer(ServerConfigProvider.SERVICE.LINKSERVICE, grepcmd);
        Assertions.assertThat(linkServiceLogs).contains("\"resultCode\":\"SUCCESS\"");
        Assertions.assertThat(linkServiceLogs).contains("\"resultMsg\":\"SUCCESS\"");

    }
    @Owner("Nirottam")
    @Feature("PGP-37787")
    @Parameters({"theme"})
    @Test(description = "verify Successfull resonse in Risk  Consult API ")
    public void Risk_Consult_006(@Optional("enhancedweb_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"INVOICE","200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"INVOICE","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();

        String grepcmd = "grep \"" + "\" /paytm/logs/linkService.log  | " +
                "grep \"FLUXNET_ASYNC_CONSULT\" | grep \"RESPONSE\" ";
        String linkServiceLogs = getLogsOnServer(ServerConfigProvider.SERVICE.LINKSERVICE, grepcmd);
        Assertions.assertThat(linkServiceLogs).contains("\"resultCode\":\"SUCCESS\"");
        Assertions.assertThat(linkServiceLogs).contains("\"resultMsg\":\"SUCCESS\"");

    }
}
