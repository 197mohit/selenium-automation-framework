package scripts.api.linkservice;

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
import org.testng.annotations.Test;

import static com.paytm.apphelpers.LogsValidationHelper.getLogsOnServer;

@Feature("PGP-37787")
public class CreateOrderApi extends PGPBaseTest {
    User user;
    String mid;
    void setUserAndMId(String merchant) throws Exception {
        user = userManager.getForWrite(Label.LINK);
        mid = merchant;
    }

    @Owner("Nirottam")
    @Test(description = "verify User Info For FIXED LINK IN Create Order  API",groups={"smoke","sanity","regression"})
    public void Create_Order_001(@Optional("enhancedweb_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","2000");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.pause(50);

        String grepcmd = "grep \"" + "\" /paytm/logs/theia_facade.log  | " +
                "grep \"" + Constants.MerchantType.LINK_PGONLY.getId() + "\" | grep \"ACQUIRING_CREATE_ORDER\" | grep \"REQUEST\" ";
        String theiaFaacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);

        Assertions.assertThat(theiaFaacadeLogs).contains("userId")
                .contains("externalUserId")
                .contains("nickname")
                .contains("\"externalUserType\":\"MERCHANT\"");

    }
    @Owner("Nirottam")
    @Test(description = "verify User Info For GENERIC LINK IN Create Order  API",groups={"smoke","sanity","regression"})
    public void Create_Order_002(@Optional("enhancedweb_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"GENERIC","2000");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"GENERIC","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);


        String grepcmd = "grep \"" +  "\" /paytm/logs/theia_facade.log  | " +
                "grep \"" + Constants.MerchantType.LINK_PGONLY.getId()+ "\" | grep \"ACQUIRING_CREATE_ORDER\" | grep \"REQUEST\" ";
        String theiaFaacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);

        Assertions.assertThat(theiaFaacadeLogs).contains("userId")
                .contains("externalUserId")
                .contains("nickname")
                .contains("\"externalUserType\":\"MERCHANT\"");

    }

    @Owner("Nirottam")
    @Test(description = "verify User Info For INVOICE LINK IN Create Order  API",groups={"smoke","sanity","regression"})
    public void Create_Order_003(@Optional("enhancedweb_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"INVOICE","2000");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"INVOICE","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);



        String grepcmd = "grep \"" +  "\" /paytm/logs/theia_facade.log  | " +
                "grep \"" + Constants.MerchantType.LINK_PGONLY.getId()+ "\" | grep \"ACQUIRING_CREATE_ORDER\" | grep \"REQUEST\" ";
        String theiaFaacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);

        Assertions.assertThat(theiaFaacadeLogs).contains("userId").isNotNull()
                .contains("externalUserId").isNotNull()
                .contains("nickname")
                .contains("\"externalUserType\":\"MERCHANT\"");

    }
    @Owner("Nirottam")
    @Test(description = "verify orderTitle and orderAmount For FIXED LINK IN Create Order  API",groups={"smoke","sanity","regression"})
    public void Create_Order_004(@Optional("enhancedweb_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","2000");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);



        String grepcmd = "grep \"" + "\" /paytm/logs/theia_facade.log  | " +
                "grep \"" + Constants.MerchantType.LINK_PGONLY.getId() + "\" | grep \"ACQUIRING_CREATE_ORDER\" | grep \"REQUEST\"";
        String theiaFaacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);

        Assertions.assertThat(theiaFaacadeLogs).contains("orderTitle").isNotNull()
                .contains("\"currency\":\"INR\"")
                .contains("\"value\":\"200000\"");

    }
    @Owner("Nirottam")
    @Test(description = "verify orderTitle and orderAmount For GENERIC LINK IN Create Order  API",groups={"smoke","sanity","regression"})
    public void Create_Order_005(@Optional("enhancedweb_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"GENERIC","2000");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"GENERIC","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);


        String grepcmd = "grep \"" +  "\" /paytm/logs/theia_facade.log | " +
                "grep \"" + Constants.MerchantType.LINK_PGONLY.getId()+ "\" | grep \"ACQUIRING_CREATE_ORDER\" | grep \"REQUEST\"";
        String theiaFaacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);

        Assertions.assertThat(theiaFaacadeLogs).contains("orderTitle").isNotNull()
                .contains("\"currency\":\"INR\"")
                .contains("\"value\":\"200000\"");
    }
    @Owner("Nirottam")
    @Test(description = "verify orderTitle and orderAmount For INVOICE LINK IN Create Order  API",groups={"smoke","sanity","regression"})
    public void Create_Order_006(@Optional("enhancedweb_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"INVOICE","2000");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"INVOICE","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);




        String grepcmd = "grep \"" + "\" /paytm/logs/theia_facade.log | " +
                "grep \"" + Constants.MerchantType.LINK_PGONLY.getId()+ "\" | grep \"ACQUIRING_CREATE_ORDER\" | grep \"REQUEST\"";
        String theiaFaacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assertions.assertThat(theiaFaacadeLogs).contains("orderTitle").isNotNull()
                .contains("\"currency\":\"INR\"")
                .contains("\"value\":\"200000\"");

    }
    @Owner("Nirottam")
    @Test(description = "verify merchantTransId  For FIXED LINK IN Create Order  API",groups={"smoke","sanity","regression"})
    public void Create_Order_007(@Optional("enhancedweb_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","2000");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);




        String grepcmd = "grep \"" +  "\" /paytm/logs/theia_facade.log | " +
                "grep \"" + Constants.MerchantType.LINK_PGONLY.getId() + "\" | grep \"ACQUIRING_CREATE_ORDER\" | grep \"REQUEST\"";
        String theiaFaacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);

        Assertions.assertThat(theiaFaacadeLogs).contains("merchantTransId").isNotNull();
    }
    @Owner("Nirottam")
    @Test(description = "verify merchantTransId  For GENERIC LINK IN Create Order  API",groups={"smoke","sanity","regression"})
    public void Create_Order_008(@Optional("enhancedweb_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"GENERIC","2000");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"GENERIC","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);


        String grepcmd = "grep \"" + "\" /paytm/logs/theia_facade.log | " +
                "grep \"" + Constants.MerchantType.LINK_PGONLY.getId() + "\" | grep \"ACQUIRING_CREATE_ORDER\" | grep \"REQUEST\"";
        String theiaFaacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assertions.assertThat(theiaFaacadeLogs).contains("merchantTransId").isNotNull();
    }
    @Owner("Nirottam")
    @Test(description = "verify merchantTransId  For INVOICE LINK IN Create Order  API",groups={"smoke","sanity","regression"})
    public void Create_Order_009(@Optional("enhancedweb_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"INVOICE","2000");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"INVOICE","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);


        String grepcmd = "grep \"" + "\" /paytm/logs/theia_facade.log | " +
                "grep \"" + Constants.MerchantType.LINK_PGONLY.getId() + "\" | grep \"ACQUIRING_CREATE_ORDER\" | grep \"REQUEST\"";
        String theiaFaacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);

        Assertions.assertThat(theiaFaacadeLogs).contains("merchantTransId").isNotNull();
    }
    @Owner("Nirottam")
    @Test(description = "verify ExtendInfo  For FIXED LINK IN Create Order  API",groups={"smoke","sanity","regression"})
    public void Create_Order_0010(@Optional("enhancedweb_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","2000");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);


        String grepcmd = "grep \"" + "\" /paytm/logs/theia_facade.log | " +
                "grep \"" + Constants.MerchantType.LINK_PGONLY.getId()+ "\" | grep \"ACQUIRING_CREATE_ORDER\" | grep \"REQUEST\"";
        String theiaFaacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);

        Assertions.assertThat(theiaFaacadeLogs).contains("extendInfo");
    }
    @Owner("Nirottam")
    @Test(description = "verify ExtendInfo  For GENERIC LINK IN Create Order  API",groups={"smoke","sanity","regression"})
    public void Create_Order_0011(@Optional("enhancedweb_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"GENERIC","2000");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"GENERIC","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);


        String grepcmd = "grep \"" +  "\" /paytm/logs/theia_facade.log | " +
                "grep \"" + Constants.MerchantType.LINK_PGONLY.getId() + "\" | grep \"ACQUIRING_CREATE_ORDER\" | grep \"REQUEST\"";
        String theiaFaacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);

        Assertions.assertThat(theiaFaacadeLogs).contains("extendInfo");
    }
    @Owner("Nirottam")
    @Test(description = "verify ExtendInfo  For INVOICE LINK IN Create Order  API",groups={"smoke","sanity","regression"})
    public void Create_Order_0012(@Optional("enhancedweb_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"INVOICE","2000");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"INVOICE","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);


        String grepcmd = "grep \"" +  "\" /paytm/logs/theia_facade.log | " +
                "grep \"" + Constants.MerchantType.LINK_PGONLY.getId()+ "\" | grep \"ACQUIRING_CREATE_ORDER\" | grep \"REQUEST\"";
        String theiaFaacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);

        Assertions.assertThat(theiaFaacadeLogs).contains("extendInfo");
    }
    @Owner("Nirottam")
    @Test(description = "verify amount in ExtendInfo  For FIXED LINK IN Create Order  API",groups={"smoke","sanity","regression"})
    public void Create_Order_0013(@Optional("enhancedweb_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","2000");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");

        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);


        String grepcmd = "grep \""  + "\" /paytm/logs/theia_facade.log | " +
                "grep \"" +Constants.MerchantType.LINK_PGONLY.getId() + "\" | grep \"ACQUIRING_CREATE_ORDER\" | grep \"REQUEST\"| grep \"extendInfo\"";
        String theiaFaacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);

        Assertions.assertThat(theiaFaacadeLogs).contains("\"totalTxnAmount\\\":\\\"200000\\\"");
    }
    @Owner("Nirottam")
    @Test(description = "verify amount in ExtendInfo  For GENERIC LINK IN Create Order  API",groups={"smoke","sanity","regression"})
    public void Create_Order_0014(@Optional("enhancedweb_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"GENERIC","2000");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"GENERIC","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);


        String grepcmd = "grep \"" +  "\" /paytm/logs/theia_facade.log | " +
                "grep \"" + Constants.MerchantType.LINK_PGONLY.getId() + "\" | grep \"ACQUIRING_CREATE_ORDER\" | grep \"REQUEST\"| grep \"extendInfo\"";
        String theiaFaacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);

        Assertions.assertThat(theiaFaacadeLogs).contains("\"totalTxnAmount\\\":\\\"200000\\\"");
    }
    @Owner("Nirottam")
    @Test(description = "verify mandatory parameters in ExtendInfo  For FIXED LINK IN Create Order  API",groups={"smoke","sanity","regression"})
    public void Create_Order_0015(@Optional("enhancedweb_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","2000");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");

        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        String grepcmd = "grep \"" +  "\" /paytm/logs/theia_facade.log | " +
                "grep \"" + Constants.MerchantType.LINK_PGONLY.getId() + "\" | grep \"ACQUIRING_CREATE_ORDER\" | grep \"REQUEST\"| grep \"extendInfo\"";
        String theiaFaacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);

        Assertions.assertThat(theiaFaacadeLogs).contains("PAYTM_USER_ID").isNotNull()
                .contains("\"pushDataToDynamicQR\\\":\\\"false\\\"")
                .contains("\"communicationManager\\\":\\\"false\\\"")
                .contains("\"linkName\\\":\\\"TestingLink\\\"")
                .contains("merchantName").isNotNull()
                .contains("\\\"mccCode\\\":\\\"Retail\\\"")
                .contains("userMobile").isNotNull()
                .contains("linkBasedNonInvoicePayment").isNotNull()
                .contains("merchantTransId").isNotNull().contains("paytmMerchantId").isNotNull()
                .contains("\\\"search3\\\":\\\"plcid-3454\\\"")
                .contains("\\\"search6\\\":\\\"plcn-nirottam\\\"")
                .contains("\\\"search4\\\":\\\"plmn-7014107741\\\"")
                .contains("\\\"search5\\\":\\\"plcei-nirottam.singh@paytm.com\\\"")
                .contains("website").isNotNull()
                .contains("\\\"callBackURL\\\":\\\"https://pgp-automation.paytm.in/theia/linkPaymentRedirect\\\"")
                .contains("\\\"requestType\\\":\\\"LINK_BASED_PAYMENT\\\"");


    }
    @Owner("Nirottam")
    @Test(description = "verify  mandatory parameters in ExtendInfo  For GENERIC LINK IN Create Order  API",groups={"smoke","sanity","regression"})
    public void Create_Order_0016(@Optional("enhancedweb_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"GENERIC","2000");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"GENERIC","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);


        String grepcmd = "grep \"" +  "\" /paytm/logs/theia_facade.log | " +
                "grep \"" + Constants.MerchantType.LINK_PGONLY.getId() + "\" | grep \"ACQUIRING_CREATE_ORDER\" | grep \"REQUEST\"| grep \"extendInfo\"";
        String theiaFaacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);

        Assertions.assertThat(theiaFaacadeLogs).contains("PAYTM_USER_ID").isNotNull()
                .contains("\"pushDataToDynamicQR\\\":\\\"false\\\"")
                .contains("\"communicationManager\\\":\\\"false\\\"")
                .contains("\"linkName\\\":\\\"TestingLink\\\"")
                .contains("merchantName").isNotNull()
                .contains("\\\"mccCode\\\":\\\"Retail\\\"")
                .contains("userMobile").isNotNull()
                .contains("linkBasedNonInvoicePayment").isNotNull()
                .contains("merchantTransId").isNotNull().contains("paytmMerchantId").isNotNull()
                .contains("\\\"search3\\\":\\\"plcid-3454\\\"")
                .contains("\\\"search6\\\":\\\"plcn-nirottam\\\"")
                .contains("\\\"search4\\\":\\\"plmn-7014107741\\\"")
                .contains("\\\"search5\\\":\\\"plcei-nirottam.singh@paytm.com\\\"")
                .contains("website").isNotNull()
                .contains("\\\"callBackURL\\\":\\\"https://pgp-automation.paytm.in/theia/linkPaymentRedirect\\\"")
                .contains("\\\"requestType\\\":\\\"LINK_BASED_PAYMENT\\\"");

    }
    @Owner("Nirottam")
    @Test(description = "verify  mandatory parameters in ExtendInfo  For INVOICE LINK IN Create Order  API",groups={"smoke","sanity","regression"})
    public void Create_Order_0017(@Optional("enhancedweb_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"INVOICE","2000");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"INVOICE","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);


        String grepcmd = "grep \"" +  "\" /paytm/logs/theia_facade.log | " +
                "grep \"" + Constants.MerchantType.LINK_PGONLY.getId() + "\" | grep \"ACQUIRING_CREATE_ORDER\" | grep \"REQUEST\"| grep \"extendInfo\"";
        String theiaFaacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);

        Assertions.assertThat(theiaFaacadeLogs).contains("PAYTM_USER_ID").isNotNull()
                .contains("\"pushDataToDynamicQR\\\":\\\"false\\\"")
                .contains("\"communicationManager\\\":\\\"false\\\"")
                .contains("\"linkName\\\":\\\"TestingLink\\\"")
                .contains("merchantName").isNotNull()
                .contains("\\\"mccCode\\\":\\\"Retail\\\"")
                .contains("userMobile").isNotNull()
                .contains("linkBasedNonInvoicePayment").isNotNull()
                .contains("merchantTransId").isNotNull().contains("paytmMerchantId").isNotNull()
                .contains("\\\"search3\\\":\\\"plcid-3454\\\"")
                .contains("\\\"search6\\\":\\\"plcn-nirottam\\\"")
                .contains("\\\"search4\\\":\\\"plmn-7014107741\\\"")
                .contains("\\\"search5\\\":\\\"plcei-nirottam.singh@paytm.com\\\"")
                .contains("website").isNotNull()
                .contains("\\\"callBackURL\\\":\\\"https://pgp-automation.paytm.in/theia/linkPaymentRedirect\\\"")
                .contains("\\\"requestType\\\":\\\"LINK_BASED_PAYMENT\\\"");

    }
    @Owner("Nirottam")
    @Test(description = "verify  mandatory parameters in ExtendInfo  For FIXED LINK IN Create Order  API",groups={"smoke","sanity","regression"})
    public void Create_Order_0018(@Optional("enhancedweb_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","2000");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);


        String grepcmd = "grep \"" +  "\" /paytm/logs/theia_facade.log | " +
                "grep \"" + Constants.MerchantType.LINK_PGONLY.getId() + "\" | grep \"ACQUIRING_CREATE_ORDER\" | grep \"REQUEST\"| grep \"extendInfo\"";
        String theiaFaacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);

        Assertions.assertThat(theiaFaacadeLogs).contains("\\\"linkCustomerEmail\\\":\\\"nirottam.singh@paytm.com\\\"")
                .contains("\\\"subsRenewOrderAlreadyCreated\\\"")
                .contains("productCode")
                .contains("\\\"autoRenewal\\\":\\\"false\\\"")
                .contains("\\\"autoRetry\\\":\\\"false\\\"")
                .contains("\\\"cardTokenRequired\\\":\\\"false\\\"")
                .contains("\\\"enhancedNative\\\":\\\"false\\\"")
                .contains("\\\"paytmMerchantId\\\":\\\"testli61258254741921\\\"")
                .contains("\"fromAoaMerchant\":false")
                .contains("\\\"custID\\\"")
                .contains("\\\"merchantTransId\\\"")
                .contains("\\\"offlineFlow\\\":\\\"false\\\"");

    }
    @Owner("Nirottam")
    @Test(description = "verify  mandatory parameters in ExtendInfo  For GENERIC LINK IN Create Order  API",groups={"smoke","sanity","regression"})
    public void Create_Order_0019(@Optional("enhancedweb_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"GENERIC","2000");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"GENERIC","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);


        String grepcmd = "grep \"" +  "\" /paytm/logs/theia_facade.log | " +
                "grep \"" + Constants.MerchantType.LINK_PGONLY.getId() + "\" | grep \"ACQUIRING_CREATE_ORDER\" | grep \"REQUEST\"| grep \"extendInfo\"";
        String theiaFaacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);

        Assertions.assertThat(theiaFaacadeLogs).contains("\\\"linkCustomerEmail\\\":\\\"nirottam.singh@paytm.com\\\"")
                .contains("\\\"subsRenewOrderAlreadyCreated\\\"")
                .contains("productCode")
                .contains("\\\"autoRenewal\\\":\\\"false\\\"")
                .contains("\\\"autoRetry\\\":\\\"false\\\"")
                .contains("\\\"cardTokenRequired\\\":\\\"false\\\"")
                .contains("\\\"enhancedNative\\\":\\\"false\\\"")
                .contains("\\\"paytmMerchantId\\\":\\\"testli61258254741921\\\"")
                .contains("\"fromAoaMerchant\":false")
                .contains("\\\"custID\\\"")
                .contains("\\\"merchantTransId\\\"")
                .contains("\\\"offlineFlow\\\":\\\"false\\\"");


    }
    @Owner("Nirottam")
    @Test(description = "verify  mandatory parameters in ExtendInfo  For FIXED LINK IN Create Order  API",groups={"smoke","sanity","regression"})
    public void Create_Order_0020(@Optional("enhancedweb_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","2000");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);


        String grepcmd = "grep \"" +  "\" /paytm/logs/theia_facade.log | " +
                "grep \"" + Constants.MerchantType.LINK_PGONLY.getId() + "\" | grep \"ACQUIRING_CREATE_ORDER\" | grep \"REQUEST\"| grep \"extendInfo\"";
        String theiaFaacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);

        Assertions.assertThat(theiaFaacadeLogs).contains("\\\"linkCustomerEmail\\\":\\\"nirottam.singh@paytm.com\\\"")
                .contains("\\\"subsRenewOrderAlreadyCreated\\\"")
                .contains("productCode")
                .contains("\\\"autoRenewal\\\":\\\"false\\\"")
                .contains("\\\"autoRetry\\\":\\\"false\\\"")
                .contains("\\\"cardTokenRequired\\\":\\\"false\\\"")
                .contains("\\\"enhancedNative\\\":\\\"false\\\"")
                .contains("\\\"paytmMerchantId\\\":\\\"testli61258254741921\\\"")
                .contains("\"fromAoaMerchant\":false")
                .contains("\\\"custID\\\"")
                .contains("\\\"merchantTransId\\\"")
                .contains("\\\"offlineFlow\\\":\\\"false\\\"");

    }
    @Owner("Nirottam")
    @Test(description = "verify Create order Api Success    For FIXED LINK ",groups={"smoke","sanity","regression"})
    public void Create_Order_0021(@Optional("enhancedweb_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","2000");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);


        String grepcmd = "grep \"" +  "\" /paytm/logs/theia_facade.log | " +
                "grep \"" + Constants.MerchantType.LINK_PGONLY.getId() + "\" | grep \"ACQUIRING_CREATE_ORDER\" | grep \"RESPONSE\"| grep \"body\"";
        String theiaFaacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);

        Assertions.assertThat(theiaFaacadeLogs).contains("\"resultMsg\":\"success\"")
                .contains("\"resultStatus\":\"S\"")
                .contains("\"resultCode\":\"SUCCESS\"")
                .contains("\"resultCodeId\":\"00000000\"");
    }
    @Owner("Nirottam")
    @Test(description = "verify Create order Api Success    For GENERIC LINK ",groups={"smoke","sanity","regression"})
    public void Create_Order_0022(@Optional("enhancedweb_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"GENERIC","2000");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"GENERIC","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);


        String grepcmd = "grep \"" +  "\" /paytm/logs/theia_facade.log | " +
                "grep \"" + Constants.MerchantType.LINK_PGONLY.getId() + "\" | grep \"ACQUIRING_CREATE_ORDER\" | grep \"RESPONSE\"| grep \"body\"";
        String theiaFaacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);

        Assertions.assertThat(theiaFaacadeLogs).contains("\"resultMsg\":\"success\"")
                .contains("\"resultStatus\":\"S\"")
                .contains("\"resultCode\":\"SUCCESS\"")
                .contains("\"resultCodeId\":\"00000000\"");

    }


}
