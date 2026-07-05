package scripts.api.linkservice;

import com.paytm.api.linkAPI.CreateNewLink;
import com.paytm.appconstants.Constants;
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


@Feature("PGP-38296")
public class LinkTxnOneCrore extends PGPBaseTest {
    User user;
    String mid;
    void setUserAndMId(String merchant) throws Exception {
        user = userManager.getForWrite(Label.LINK);
        mid = merchant;
    }

    @Owner("Anushka_Goldi")
    @Feature("PGP-38296")
    @Parameters({"theme"})
    @Test(description = "verify Txn is successful For FIXED LINK with amount more than 1 Crore")
    public void Create_Order_001(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        RestAssured.useRelaxedHTTPSValidation();
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","20000000");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payByNBForNewFlow();
        cashierPage.waitUntilLoads();
        Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Paid Successfully");
    }

    @Owner("Anushka_Goldi")
    @Feature("PGP-38296")
    @Parameters({"theme"})
    @Test(description = "verify Txn is successful For GENERIC LINK with amount more than 1 Crore")
    public void Create_Order_002(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"GENERIC","20000000");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"GENERIC","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payByNBForNewFlow();
        cashierPage.waitUntilLoads();
        Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Paid Successfully");
    }

    @Owner("Anushka_Goldi")
    @Feature("PGP-38296")
    @Parameters({"theme"})
    @Test(description = "verify Txn is successful For INVOICE LINK with amount more than 1 Crore")
    public void Create_Order_003(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"INVOICE","20000000");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"INVOICE","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payByNBForNewFlow();
        cashierPage.waitUntilLoads();
        Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Paid Successfully");
    }

    @Owner("Anushka_Goldi")
    @Feature("PGP-38296")
    @Parameters({"theme"})
    @Test(description = "verify Txn is successful For Skip Login & FIXED LINK with amount more than 1 Crore")
    public void Skip_Login_001(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_SKIPLOGIN_ONLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","20000000");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPWithForms(user,paymentLink,"FIXED", "SKIPLOGIN", "20000000");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payByNBForNewFlow();
        cashierPage.waitUntilLoads();
        Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Paid Successfully");
    }

    @Owner("Anushka_Goldi")
    @Feature("PGP-38296")
    @Parameters({"theme"})
    @Test(description = "verify Txn is successful For Skip Login & GENERIC LINK with amount more than 1 Crore")
    public void Skip_Login_002_1(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_SKIPLOGIN_ONLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"GENERIC","1000000");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPWithForms(user,paymentLink,"GENERIC", "SKIPLOGIN", "1000000");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payByNBForNewFlow();
        cashierPage.waitUntilLoads();
        Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Paid Successfully");
    }

    @Owner("Anushka_Goldi")
    @Feature("PGP-38296")
    @Parameters({"theme"})
    @Test(description = "verify Txn is successful For Skip Login & GENERIC LINK with amount more than 1 Crore")
    public void Skip_Login_002_2(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_SKIPLOGIN_ONLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"GENERIC","20000000");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPWithForms(user,paymentLink,"GENERIC", "SKIPLOGIN", "20000000");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        Assertions.assertThat(cashierPage.failTxnScreen().getText()).contains("Amount can be between 1 and 10000000");
    }

    @Owner("Anushka_Goldi")
    @Feature("PGP-38296")
    @Parameters({"theme"})
    @Test(description = "verify Txn is successful For Skip Login & INVOICE LINK with amount more than 1 Crore")
    public void Skip_Login_003(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_SKIPLOGIN_ONLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"INVOICE","20000000");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPWithForms(user,paymentLink,"INVOICE", "SKIPLOGIN", "20000000");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payByNBForNewFlow();
        cashierPage.waitUntilLoads();
        Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Paid Successfully");
    }

    @Owner("Anushka_Goldi")
    @Feature("PGP-38296")
    @Parameters({"theme"})
    @Test(description = "verify Txn is successful For All in one & FIXED LINK with amount more than 1 Crore")
    public void All_in1_001(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_ALLIN_ONE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","20000000");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPWithForms(user,paymentLink,"FIXED","ALLINONE","20000000");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payByNBForNewFlow();
        cashierPage.waitUntilLoads();
        Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Paid Successfully");
    }

    @Owner("Anushka_Goldi")
    @Feature("PGP-38296")
    @Parameters({"theme"})
    @Test(description = "verify Txn is successful For All in one & INVOICE LINK with amount more than 1 Crore")
    public void All_in1_002(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_ALLIN_ONE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"INVOICE","20000000");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPWithForms(user,paymentLink,"INVOICE","ALLINONE","20000000");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payByNBForNewFlow();
        cashierPage.waitUntilLoads();
        Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Paid Successfully");
    }

    @Owner("Anushka_Goldi")
    @Feature("PGP-38296")
    @Parameters({"theme"})
    @Test(description = "verify Txn is successful For All in one & GENERIC LINK with amount more than 1 Crore")
    public void All_in1_003_1(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_ALLIN_ONE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"GENERIC","20000000");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPWithForms(user,paymentLink,"GENERIC","ALLINONE","2000000");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payByNBForNewFlow();
        cashierPage.waitUntilLoads();
        Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Paid Successfully");
    }

    @Owner("Anushka_Goldi")
    @Feature("PGP-38296")
    @Parameters({"theme"})
    @Test(description = "verify Txn is successful For All in one & GENERIC LINK with amount more than 1 Crore")
    public void All_in1_003_2(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_ALLIN_ONE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"GENERIC","200000000");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPWithForms(user,paymentLink,"GENERIC","ALLINONE","20000000");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        Assertions.assertThat(cashierPage.failTxnScreen().getText()).contains("Amount can be between 1 and 10000000");
    }

    @Owner("Anushka_Goldi")
    @Feature("PGP-38296")
    @Parameters({"theme"})
    @Test(description = "verify Txn is successful For Payment Form & FIXED LINK with amount more than 1 Crore")
    public void Paymentform_01(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_ALLIN_ONE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","20000000");
        createNewLink.setContext("body.templateId","1");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPWithForms(user,paymentLink,"FIXED","PAYMENTFORM","2000000");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payByNBForNewFlow();
        cashierPage.waitUntilLoads();
        Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Paid Successfully");
    }

    @Owner("Anushka_Goldi")
    @Feature("PGP-38296")
    @Parameters({"theme"})
    @Test(description = "verify Txn is successful For Payment Form & GENERIC LINK with amount more than 1 Crore")
    public void Paymentform_02(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_ALLIN_ONE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"GENERIC","20000000");
        createNewLink.setContext("body.templateId","1");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPWithForms(user,paymentLink,"GENERIC","PAYMENTFORM","2000000");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payByNBForNewFlow();
        cashierPage.waitUntilLoads();
        Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Paid Successfully");
    }

    @Owner("Anushka_Goldi")
    @Feature("PGP-38296")
    @Parameters({"theme"})
    @Test(description = "verify Txn is successful For Payment Form & INVOICE LINK with amount more than 1 Crore")
    public void Paymentform_03(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_ALLIN_ONE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"INVOICE","20000000");
        createNewLink.setContext("body.templateId","1");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPWithForms(user,paymentLink,"INVOICE","PAYMENTFORM","2000000");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payByNBForNewFlow();
        cashierPage.waitUntilLoads();
        Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Paid Successfully");
    }

    @Owner("Anushka_Goldi")
    @Feature("PGP-38296")
    @Parameters({"theme"})
    @Test(description = "verify OFFLINE Txn is successful For FIXED LINK with amount more than 1 Crore")
    public void Fixed_001_OFFLINE(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","20000000");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payByNBForOldFlow();
        cashierPage.waitUntilLoads();
        Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Paid Successfully");
    }

    @Owner("Anushka_Goldi")
    @Feature("PGP-38296")
    @Parameters({"theme"})
    @Test(description = "verify OFFLINE Txn is successful For GENERIC LINK with amount more than 1 Crore")
    public void Generic_002_OFFLINE(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"GENERIC","20000000");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"GENERIC","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payByNBForOldFlow();
        cashierPage.waitUntilLoads();
        Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Paid Successfully");
    }

    @Owner("Anushka_Goldi")
    @Feature("PGP-38296")
    @Parameters({"theme"})
    @Test(description = "verify OFFLINE Txn is successful For INVOICE LINK with amount more than 1 Crore")
    public void Invoice_003_OFFLINE(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"INVOICE","20000000");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"INVOICE","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payByNBForOldFlow();
        cashierPage.waitUntilLoads();
        Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Paid Successfully");
    }

    @Owner("Anushka_Goldi")
    @Feature("PGP-38296")
    @Parameters({"theme"})
    @Test(description = "verify OFFLINE Txn is successful For Skip Login & FIXED LINK with amount more than 1 Crore")
    public void Skip_Login_001_OFFLINE(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_SKIPLOGIN.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","20000000");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPWithForms(user,paymentLink,"FIXED", "SKIPLOGIN", "20000000");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payByNBForOldFlow();
        cashierPage.waitUntilLoads();
        Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Paid Successfully");
    }

    @Owner("Anushka_Goldi")
    @Feature("PGP-38296")
    @Parameters({"theme"})
    @Test(description = "verify OFFLINE Txn is successful For Skip Login & GENERIC LINK with amount more than 1 Crore")
    public void Skip_Login_002_1_OFFLINE(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_SKIPLOGIN.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"GENERIC","1000000");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPWithForms(user,paymentLink,"GENERIC", "SKIPLOGIN", "1000000");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payByNBForOldFlow();
        cashierPage.waitUntilLoads();
        Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Paid Successfully");
    }

    @Owner("Anushka_Goldi")
    @Feature("PGP-38296")
    @Parameters({"theme"})
    @Test(description = "verify OFFLINE Txn is successful For Skip Login & GENERIC LINK with amount more than 1 Crore")
    public void Skip_Login_002_2_OFFLINE(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_SKIPLOGIN.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"GENERIC","20000000");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPWithForms(user,paymentLink,"GENERIC", "SKIPLOGIN", "20000000");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        Assertions.assertThat(cashierPage.failTxnScreen().getText()).contains("Amount can be between 1 and 10000000");
    }

    @Owner("Anushka_Goldi")
    @Feature("PGP-38296")
    @Parameters({"theme"})
    @Test(description = "verify OFFLINE Txn is successful For Skip Login & INVOICE LINK with amount more than 1 Crore")
    public void Skip_Login_003_OFFLINE(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_SKIPLOGIN.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"INVOICE","20000000");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPWithForms(user,paymentLink,"INVOICE", "SKIPLOGIN", "20000000");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payByNBForOldFlow();
        cashierPage.waitUntilLoads();
        Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Paid Successfully");
    }

    @Owner("Anushka_Goldi")
    @Feature("PGP-38296")
    @Parameters({"theme"})
    @Test(description = "verify OFFLINE Txn is successful For All in one & FIXED LINK with amount more than 1 Crore")
    public void All_in1_001_OFFLINE(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_ALLIN_ONE_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","20000000");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPWithForms(user,paymentLink,"FIXED","ALLINONE","20000000");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payByNBForOldFlow();
        cashierPage.waitUntilLoads();
        Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Paid Successfully");
    }

    @Owner("Anushka_Goldi")
    @Feature("PGP-38296")
    @Parameters({"theme"})
    @Test(description = "verify OFFLINE Txn is successful For All in one & INVOICE LINK with amount more than 1 Crore")
    public void All_in1_002_OFFLINE(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_ALLIN_ONE_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"INVOICE","20000000");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPWithForms(user,paymentLink,"INVOICE","ALLINONE","20000000");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payByNBForOldFlow();
        cashierPage.waitUntilLoads();
        Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Paid Successfully");
    }

    @Owner("Anushka_Goldi")
    @Feature("PGP-38296")
    @Parameters({"theme"})
    @Test(description = "verify OFFLINE Txn is successful For All in one & GENERIC LINK with amount more than 1 Crore")
    public void All_in1_003_1_OFFLINE(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_ALLIN_ONE_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"GENERIC","20000000");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPWithForms(user,paymentLink,"GENERIC","ALLINONE","2000000");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payByNBForOldFlow();
        cashierPage.waitUntilLoads();
        Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Paid Successfully");
    }

    @Owner("Anushka_Goldi")
    @Feature("PGP-38296")
    @Parameters({"theme"})
    @Test(description = "verify OFFLINE Txn is successful For All in one & GENERIC LINK with amount more than 1 Crore")
    public void All_in1_003_2_OFFLINE(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        RestAssured.useRelaxedHTTPSValidation();
        setUserAndMId(Constants.MerchantType.LINK_ALLIN_ONE_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"GENERIC","200000000");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPWithForms(user,paymentLink,"GENERIC","ALLINONE","20000000");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        Assertions.assertThat(cashierPage.failTxnScreen().getText()).contains("Amount can be between 1 and 10000000");
    }


    @Owner("Anushka_Goldi")
    @Feature("PGP-38296")
    @Parameters({"theme"})
    @Test(description = "verify OFFLINE Txn is successful For Payment Form & FIXED LINK with amount more than 1 Crore")
    public void Paymentform_01_OFFLINE(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_ALLIN_ONE_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","20000000");
        createNewLink.setContext("body.templateId","7");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPWithForms(user,paymentLink,"FIXED","PAYMENTFORM","2000000");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payByNBForOldFlow();
        cashierPage.waitUntilLoads();
        Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Paid Successfully");
    }

    @Owner("Anushka_Goldi")
    @Feature("PGP-38296")
    @Parameters({"theme"})
    @Test(description = "verify OFFLINE Txn is successful For Payment Form & GENERIC LINK with amount more than 1 Crore")
    public void Paymentform_02_OFFLINE(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_ALLIN_ONE_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"GENERIC","20000000");
        createNewLink.setContext("body.templateId","7");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPWithForms(user,paymentLink,"GENERIC","PAYMENTFORM","2000000");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payByNBForOldFlow();
        cashierPage.waitUntilLoads();
        Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Paid Successfully");
    }

    @Owner("Anushka_Goldi")
    @Feature("PGP-38296")
    @Parameters({"theme"})
    @Test(description = "verify OFFLINE Txn is successful For Payment Form & INVOICE LINK with amount more than 1 Crore")
    public void Paymentform_03_OFFLINE(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_ALLIN_ONE_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"INVOICE","20000000");
        createNewLink.setContext("body.templateId","7");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPWithForms(user,paymentLink,"INVOICE","PAYMENTFORM","2000000");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payByNBForOldFlow();
        cashierPage.waitUntilLoads();
        Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Paid Successfully");
    }
    @Owner("Rohit_Sharma")
    @Feature("PGP-43587")
    @Parameters({"theme"})
    @Test(description = "verify Merchant Display Name For FIXED LINK with succesful transaction")
    public void Merchant_Display_Name_for_FixedLink(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_TXN_PG2.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","2");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        cashierPage.SuccessfullScreen().waitUntilVisible();
        Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Paid Successfully");
        Assertions.assertThat(cashierPage.getmerchantName().getText()).contains("Amazon Retail");
    }
    @Owner("Rohit_Sharma")
    @Feature("PGP-43587")
    @Parameters({"theme"})
    @Test(description = "verify Merchant Display Name For GENERIC LINK with succesful transaction")
    public void Merchant_Display_Name_for_GenericLink(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_TXN_PG2.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"GENERIC","2");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"GENERIC","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        cashierPage.SuccessfullScreen().waitUntilVisible();
        Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Paid Successfully");
        Assertions.assertThat(cashierPage.getmerchantName().getText()).contains("Amazon Retail");
    }
}
