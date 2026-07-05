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
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import static com.paytm.api.linkAPI.linkConstant.PGPAPIResourcePath.LINK_MONTHLY_TXN_LIMIT;
import static com.paytm.apphelpers.LogsValidationHelper.getLogsOnServer;

public class MonthlyLinkTxnLimit extends PGPBaseTest {
    User user;
    String mid;
    String amount="55000";
    void setUserAndMId(String merchant) throws Exception {
        user = userManager.getForWrite(Label.LINK);
        mid = merchant;
    }
    @Owner("Nirottam")
    @Feature("PGP-38424")
    @Parameters({"theme"})
    @Test(description = "verify Error Msg in theia.log for Fixed Link When Txn amount is 50K",groups={"smoke","sanity","regression"})
    public void MonthlyLinkTxnLimit_001(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_SD.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, "FIXED", amount);
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForSD(user, paymentLink, "FIXED", "web","SD",amount);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.getNBTabForNewFlow().click();
        cashierPage.getPayButtonNew().click();

        String grepcmd = "grep \"" + "\" /paytm/logs/theia.log  | " +
                "grep \"Please try with lower amount or different payment mode for this transaction\"";

        Awaitility.await().until(() -> LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd), theiaFaacadeLogs -> !"".equals(theiaFaacadeLogs));
        String theiaFaacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assertions.assertThat(theiaFaacadeLogs).contains(LINK_MONTHLY_TXN_LIMIT);
    }
    @Owner("Nirottam")
    @Feature("PGP-38424")
    @Parameters({"theme"})
    @Test(description = "verify Error Msg in theia.log for INVOICE Link When Txn amount is 50K",groups={"smoke","sanity","regression"})
    public void MonthlyLinkTxnLimit_002(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_SD.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, "INVOICE", amount);
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForSD(user, paymentLink, "INVOICE", "web","SD",amount);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.getNBTabForNewFlow().click();
        cashierPage.getPayButtonNew().click();

        String grepcmd = "grep \"" + "\" /paytm/logs/theia.log  | " +
                "grep \"Please try with lower amount or different payment mode for this transaction\"";
        Awaitility.await().until(() -> LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd), theiaFaacadeLogs -> !"".equals(theiaFaacadeLogs));
        String theiaFaacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assertions.assertThat(theiaFaacadeLogs).contains(LINK_MONTHLY_TXN_LIMIT);

    }
    @Owner("Nirottam")
    @Feature("PGP-38424")
    @Parameters({"theme"})
    @Test(description = "verify Error Msg in theia.log for GENERIC Link When Txn amount is 50K",groups={"smoke","sanity","regression"})
    public void MonthlyLinkTxnLimit_003(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_SD.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, "GENERIC", amount);
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForSD(user, paymentLink, "GENERIC", "web","SD",amount);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.getNBTabForNewFlow().click();
        cashierPage.getPayButtonNew().click();
        String grepcmd = "grep \"" + "\" /paytm/logs/theia.log  | " +
                "grep \"Please try with lower amount or different payment mode for this transaction\"";
        Awaitility.await().until(() -> LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd), theiaFaacadeLogs -> !"".equals(theiaFaacadeLogs));
        String theiaFaacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assertions.assertThat(theiaFaacadeLogs).contains(LINK_MONTHLY_TXN_LIMIT);
    }
    @Owner("Nirottam")
    @Feature("PGP-38424")
    @Parameters({"theme"})
    @Test(description = "verify Error Msg in theia.log for Fixed Link When Txn amount is 50K for Offline Flow",groups={"smoke","sanity","regression"})
    public void MonthlyLinkTxnLimit_Offline_004(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_SD_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, "FIXED", amount);
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForSD(user, paymentLink, "FIXED", "web","SD",amount);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.getNBTabForOldFlow().click();
        cashierPage.getPayButton().click();

        String grepcmd = "grep \"" + "\" /paytm/logs/theia.log  | " +
                "grep \"Please try with lower amount or different payment mode for this transaction\"";

        Awaitility.await().until(() -> LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd), theiaFaacadeLogs -> !"".equals(theiaFaacadeLogs));
        String theiaFaacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assertions.assertThat(theiaFaacadeLogs).contains(LINK_MONTHLY_TXN_LIMIT);
    }
    @Owner("Nirottam")
    @Feature("PGP-38424")
    @Parameters({"theme"})
    @Test(description = "verify Error Msg in theia.log for INVOICE Link When Txn amount is 50K for Offline Flow",groups={"smoke","sanity","regression"})
    public void MonthlyLinkTxnLimit_Offline_005(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_SD_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, "INVOICE", amount);
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForSD(user, paymentLink, "INVOICE", "web","SD",amount);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.getNBTabForOldFlow().click();
        cashierPage.getPayButton().click();

        String grepcmd = "grep \"" + "\" /paytm/logs/theia.log  | " +
                "grep \"Please try with lower amount or different payment mode for this transaction\"";

        Awaitility.await().until(() -> LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd), theiaFaacadeLogs -> !"".equals(theiaFaacadeLogs));
        String theiaFaacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assertions.assertThat(theiaFaacadeLogs).contains(LINK_MONTHLY_TXN_LIMIT);
    }
    @Owner("Nirottam")
    @Feature("PGP-38424")
    @Parameters({"theme"})
    @Test(description = "verify Error Msg in theia.log for GENERIC Link When Txn amount is 50K for Offline Flow",groups={"smoke","sanity","regression"})
    public void MonthlyLinkTxnLimit_Offline_006(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_SD_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, "GENERIC", amount);
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForSD(user, paymentLink, "GENERIC", "web","SD",amount);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.getNBTabForOldFlow().click();
        cashierPage.getPayButton().click();

        String grepcmd = "grep \"" + "\" /paytm/logs/theia.log  | " +
                "grep \"Please try with lower amount or different payment mode for this transaction\"";

        Awaitility.await().until(() -> LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd), theiaFaacadeLogs -> !"".equals(theiaFaacadeLogs));
        String theiaFaacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assertions.assertThat(theiaFaacadeLogs).contains(LINK_MONTHLY_TXN_LIMIT);
    }

}
