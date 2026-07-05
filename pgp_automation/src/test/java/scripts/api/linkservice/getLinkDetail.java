package scripts.api.linkservice;
import com.paytm.ServerConfigProvider;
import com.paytm.api.linkAPI.CreateNewLink;
import com.paytm.api.linkAPI.GetLinkDetail;
import com.paytm.api.linkAPI.LinkHelper;
import com.paytm.api.linkAPI.templateApis.FetchTemplate;
import com.paytm.api.linkAPI.templateApis.SaveUpdateTemplate;
import com.paytm.api.linkAPI.templateApis.deleteTemplate;
import com.paytm.api.linkAPI.templateApis.SubmitUserForm;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.LogsValidationHelper;
import com.paytm.apphelpers.PG2LogsValidationHelper;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
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
import static com.paytm.api.linkAPI.linkConstant.PGPAPIResourcePath.*;
import static com.paytm.apphelpers.LogsValidationHelper.getLogsOnServer;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import java.time.LocalDateTime;
@Feature("PGP-37787")
public class getLinkDetail extends PGPBaseTest  {
    User user;
    String mid;
    void setUserAndMId(String merchant) throws Exception {
        user = userManager.getForWrite(Label.LINK);
        mid = merchant;
    }

    public void deleteFirsttemplate(){
        FetchTemplate fetchTemplate =new FetchTemplate();
        fetchTemplate.buildRequest(mid);
        JsonPath fetchTemplateresponse=fetchTemplate.execute().jsonPath();
        String templateId=fetchTemplateresponse.getString("body.templates[0].id");
        deleteTemplate DeleteTemplate=new deleteTemplate().buildRequest(mid,templateId);
        DeleteTemplate.execute();
    }

    @Owner("Nirottam")
    @Feature("PGP-37787")
    @Parameters({"theme"})
    @Test(description = "verify Mid For FIXED LINK IN GetLinkDetail API",groups={"smoke","sanity","regression"})
    public void Link_Detail_004(@Optional("enhancedweb_revamp") String theme) throws Exception {
        RestAssured.useRelaxedHTTPSValidation();
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId,"LinkDetailRequestBody");


//        String grepcmd = "grep \""  + "\" /paytm/logs/linkService.log | " +
//                "grep \"" + Constants.MerchantType.LINK_PGONLY_OFFLINE.getId()+ "\" | grep \"getLinkDetail request :LinkDetailRequest\" | grep \"body=LinkDetailRequestBody\" ";
//        String linkServiceLogs = getLogsOnServer(ServerConfigProvider.SERVICE.LINKSERVICE, grepcmd);
        Assertions.assertThat(linkServiceLogs).contains("mid="+Constants.MerchantType.LINK_PGONLY_OFFLINE.getId());

    }
    @Owner("Nirottam")
    @Feature("PGP-37787")
    @Parameters({"theme"})
    @Test(description = "verify MID For GENERIC LINK IN GetLinkDetail API",groups={"smoke","sanity","regression"})
    public void Link_Detail_005(@Optional("enhancedweb_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"GENERIC","200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"GENERIC","web");
        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId,"LinkDetailRequestBody");


//        String grepcmd = "grep \""  + "\" /paytm/logs/linkService.log | " +
//                "grep \"" + Constants.MerchantType.LINK_PGONLY_OFFLINE.getId()+ "\" | grep \"getLinkDetail request :LinkDetailRequest\" | grep \"body=LinkDetailRequestBody\" ";
//        String linkServiceLogs = getLogsOnServer(ServerConfigProvider.SERVICE.LINKSERVICE, grepcmd);
        Assertions.assertThat(linkServiceLogs).contains("mid="+Constants.MerchantType.LINK_PGONLY_OFFLINE.getId());

    }
    @Owner("Nirottam")
    @Feature("PGP-37787")
    @Parameters({"theme"})
    @Test(description = "verify MID For INVOICE LINK IN GetLinkDetail API",groups={"smoke","sanity","regression"})
    public void Link_Detail_006(@Optional("enhancedweb_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"INVOICE","2000");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"INVOICE","web");
        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId,"LinkDetailRequestBody");


//        String grepcmd = "grep \""  + "\" /paytm/logs/linkService.log | " +
//                "grep \"" + Constants.MerchantType.LINK_PGONLY_OFFLINE.getId()+ "\" | grep \"getLinkDetail request :LinkDetailRequest\" | grep \"body=LinkDetailRequestBody\" ";
//        String linkServiceLogs = getLogsOnServer(ServerConfigProvider.SERVICE.LINKSERVICE, grepcmd);
        Assertions.assertThat(linkServiceLogs).contains("mid="+Constants.MerchantType.LINK_PGONLY_OFFLINE.getId());

    }
    @Owner("Nirottam")
    @Feature("PGP-37787")
    @Parameters({"theme"})
    @Test(description = "verify LinkId For FIXED LINK IN GetLinkDetail API",groups={"smoke","sanity","regression"})
    public void Link_Detail_007(@Optional("enhancedweb_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linKId =withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linKId,"LinkDetailRequestBody");


//        String grepcmd = "grep \"" +  "\" /paytm/logs/linkService.log | " +
//                "grep \"" + Constants.MerchantType.LINK_PGONLY_OFFLINE.getId() + "\" | grep \"getLinkDetail request :LinkDetailRequest\" | grep \"body=LinkDetailRequestBody\" ";
//        String linkServiceLogs = getLogsOnServer(ServerConfigProvider.SERVICE.LINKSERVICE, grepcmd);
        Assertions.assertThat(linkServiceLogs).contains("linkId");

    }
    @Owner("Nirottam")
    @Feature("PGP-37787")
    @Parameters({"theme"})
    @Test(description = "verify Amount For FIXED LINK IN GetLinkDetail API",groups={"smoke","sanity","regression"})
    public void Link_Detail_008(@Optional("enhancedweb_revamp") String theme) throws Exception {
        RestAssured.useRelaxedHTTPSValidation();
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","2000");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId);


//        String grepcmd = "grep \"" + "\" /paytm/logs/linkService.log | " +
//                "grep \"" + Constants.MerchantType.LINK_PGONLY_OFFLINE.getId() + "\" | grep \"getLinkDetail request :LinkDetailRequest\" | grep \"body=LinkDetailRequestBody\" ";
//        String linkServiceLogs = getLogsOnServer(ServerConfigProvider.SERVICE.LINKSERVICE, grepcmd);
        Assertions.assertThat(linkServiceLogs).contains("amount=2000");

    }
    @Owner("Nirottam")
    @Feature("PGP-37787")
    @Parameters({"theme"})
    @Test(description = "verify OrderId For FIXED LINK IN GetLinkDetail API",groups={"smoke","sanity","regression"})
    public void Link_Detail_009(@Optional("enhancedweb_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","2000");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId,"LinkDetailRequestBody");


//        String grepcmd = "grep \"" +  "\" /paytm/logs/linkService.log | " +
//                "grep \"" + Constants.MerchantType.LINK_PGONLY_OFFLINE.getId() + "\" | grep \"getLinkDetail request :LinkDetailRequest\" | grep \"body=LinkDetailRequestBody\" ";
//        String linkServiceLogs = getLogsOnServer(ServerConfigProvider.SERVICE.LINKSERVICE, grepcmd);
        Assertions.assertThat(linkServiceLogs).contains("orderId");

    }
    @Owner("Nirottam")
    @Feature("PGP-37787")
    @Parameters({"theme"})
    @Test(description = "verify Invoice Id For INVOICE LINK IN GetLinkDetail API ",groups={"smoke","sanity","regression"})
    public void Link_Detail_0010(@Optional("enhancedweb_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"INVOICE","2000");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"INVOICE","web");
        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId,"LinkDetailRequestBody");


//        String grepcmd = "grep \"" +  "\" /paytm/logs/linkService.log | " +
//                "grep \"" +Constants.MerchantType.LINK_PGONLY_OFFLINE.getId() + "\" | grep \"getLinkDetail request :LinkDetailRequest\" | grep \"body=LinkDetailRequestBody\" ";
//        String linkServiceLogs = getLogsOnServer(ServerConfigProvider.SERVICE.LINKSERVICE, grepcmd);
        Assertions.assertThat(linkServiceLogs).contains("invoiceId");

    }
    @Owner("Nirottam")
    @Feature("PGP-37787")
    @Parameters({"theme"})
    @Test(description = "verify Result Msg in ResponseBody   IN GetLinkDetail API ",groups={"smoke","sanity","regression"})
    public void Link_Detail_0011(@Optional("enhancedweb_revamp") String theme) throws Exception {
        RestAssured.useRelaxedHTTPSValidation();
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","2000");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId);


//        String grepcmd = "grep \"" + "\" /paytm/logs/linkService.log | " +
//                "grep \"getLinkDetail response :LinkDetailResponse\" ";
//        String linkServiceLogs = getLogsOnServer(ServerConfigProvider.SERVICE.LINKSERVICE, grepcmd);
        Assertions.assertThat(linkServiceLogs).contains("Request Successfully Processed");

    }
    @Owner("Nirottam")
    @Feature("PGP-37787")
    @Parameters({"theme"})
    @Test(description = "verify amount Msg in ResponseBody   IN GetLinkDetail API ",groups={"smoke","sanity","regression"})
    public void Link_Detail_0012(@Optional("enhancedweb_revamp") String theme) throws Exception {
        RestAssured.useRelaxedHTTPSValidation();
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","2000");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId,"LinkDetailResponseBody");

//        String grepcmd = "grep \"" + "\" /paytm/logs/linkService.log | " +
//                "grep \"getLinkDetail response :LinkDetailResponse\" ";
//        String linkServiceLogs = getLogsOnServer(ServerConfigProvider.SERVICE.LINKSERVICE, grepcmd);

        Assertions.assertThat(linkServiceLogs).contains("amount=2000.0")
                .contains("linkDescription=123")
                .contains("linkName=TestingLink");


    }
    @Owner("Nirottam")
    @Feature("PGP-37787")
    @Parameters({"theme"})
    @Test(description = "verify LinkRisekInfo in ResponseBody   IN GetLinkDetail API ",groups={"smoke","sanity","regression"})
    public void Link_Detail_0013(@Optional("enhancedweb_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","2000");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId,"LinkDetailResponseBody");


//        String grepcmd = "grep \"" + "\" /paytm/logs/linkService.log | " +
//                "grep \"getLinkDetail response :LinkDetailResponse\"  ";
//        String linkServiceLogs = getLogsOnServer(ServerConfigProvider.SERVICE.LINKSERVICE, grepcmd);
        Assertions.assertThat(linkServiceLogs).contains("linkPaymentRiskInfo=LinkPaymentRiskInfo");

    }
    @Owner("Nirottam")
    @Feature("PGP-37787")
    @Parameters({"theme"})
    @Test(description = "verify Value of isLinkPaymentRequest in ResponseBody   IN GetLinkDetail API ",groups={"smoke","sanity","regression"})
    public void Link_Detail_0014(@Optional("enhancedweb_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","2000");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");

        String linkId = withDrawJson1.getString("body.linkId");
        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId,"LinkDetailResponseBody");
//        String grepcmd = "grep \"" + "\" /paytm/logs/linkService.log | " +
//                "grep \"getLinkDetail response :LinkDetailResponse\" ";
//        String linkServiceLogs = getLogsOnServer(ServerConfigProvider.SERVICE.LINKSERVICE, grepcmd);
        Assertions.assertThat(linkServiceLogs).contains("isLinkPaymentRequest=true");



    }
    @Owner("Nirottam")
    @Feature("PGP-37787")
    @Parameters({"theme"})
    @Test(description = "verify LinkRisekInfo in ResponseBody   IN GetLinkDetail API ",groups={"smoke","sanity","regression"})
    public void Link_Detail_0015(@Optional("enhancedweb_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","2000");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linKId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linKId,"LinkDetailResponseBody");

//        String grepcmd = "grep \"" + "\" /paytm/logs/linkService.log | " +
//                "grep \"getLinkDetail response :LinkDetailResponse\" ";
//        String linkServiceLogs = getLogsOnServer(ServerConfigProvider.SERVICE.LINKSERVICE, grepcmd);
        Assertions.assertThat(linkServiceLogs).contains("linkType=FIXED");
        Assertions.assertThat(linkServiceLogs).contains("requestType=LinkType");
        Assertions.assertThat(linkServiceLogs).contains("linkAmount=200000");
        Assertions.assertThat(linkServiceLogs).contains("linkId");
        Assertions.assertThat(linkServiceLogs).contains("resellerId=null");
        Assertions.assertThat(linkServiceLogs).contains("resellerName=null");
        Assertions.assertThat(linkServiceLogs).contains("merchantMode=ONLINE");
    }
    @Owner("Nirottam")
    @Feature("PGP-37787")
    @Parameters({"theme"})
    @Test(description = "verify ExtendInfo in ResponseBody   IN GetLinkDetail API ",groups={"smoke","sanity","regression"})
    public void Link_Detail_0016(@Optional("enhancedweb_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","2000");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linKId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linKId,"LinkDetailResponseBody");


//        String grepcmd = "grep \"" + "\" /paytm/logs/linkService.log | " +
//                "grep \"getLinkDetail response :LinkDetailResponse\" ";
//
//        String linkServiceLogs = getLogsOnServer(ServerConfigProvider.SERVICE.LINKSERVICE, grepcmd);
        Assertions.assertThat(linkServiceLogs).contains("linkCustomerEmail=nirottam.singh@paytm.com");
        Assertions.assertThat(linkServiceLogs).contains("linkCustomerMobile=7014107741");
        Assertions.assertThat(linkServiceLogs).contains("linkCustomerName=nirottam");
        Assertions.assertThat(linkServiceLogs).contains("search6=plcn-nirottam");
        Assertions.assertThat(linkServiceLogs).contains("search4=plmn-7014107741");
        Assertions.assertThat(linkServiceLogs).contains("search5=plcei-nirottam.singh@paytm.com");

    }
    @Owner("Nirottam")
    @Feature("PGP-37787")
    @Parameters({"theme"})
    @Test(description = "verify ExtendInfo For Generic Link in ResponseBody   IN GetLinkDetail API ",groups={"smoke","sanity","regression"})
    public void Link_Detail_0017(@Optional("enhancedweb_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"GENERIC","2000");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linKId = withDrawJson1.getString("body.linkId");

        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"GENERIC","web");
        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linKId,"LinkDetailResponseBody");


//        String grepcmd = "grep \"" + "\" /paytm/logs/linkService.log | " +
//                "grep \"getLinkDetail response :LinkDetailResponse\"";
//
//        String linkServiceLogs = getLogsOnServer(ServerConfigProvider.SERVICE.LINKSERVICE, grepcmd);
        Assertions.assertThat(linkServiceLogs).contains("linkCustomerEmail=nirottam.singh@paytm.com");
        Assertions.assertThat(linkServiceLogs).contains("linkCustomerMobile=7014107741");
        Assertions.assertThat(linkServiceLogs).contains("linkCustomerName=nirottam");
        Assertions.assertThat(linkServiceLogs).contains("search6=plcn-nirottam");
        Assertions.assertThat(linkServiceLogs).contains("search4=plmn-7014107741");
        Assertions.assertThat(linkServiceLogs).contains("search5=plcei-nirottam.singh@paytm.com");

    }

    @Owner("Himanshu")
    @Test(description = "verify successful response IN GetLinkDetail API ")
    public void Link_Detail_0018() throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linKId = withDrawJson1.getString("body.linkId");
        GetLinkDetail getLinkDetail=new GetLinkDetail().buildRequest(mid,linKId,"200","1234");
        getLinkDetail.deleteContext("body.paymentFormId");
        JsonPath withDrawJson2 = getLinkDetail.execute().jsonPath();
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultMsg"),GET_LINK_DETAIL_MSG);
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultCode"),GET_LINK_DETAIL_CODE);
    }

    @Owner("Himanshu")
    @Test(description = "verify successful response IN GetLinkDetail API ")
    public void Link_Detail_0019() throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linKId = withDrawJson1.getString("body.linkId");
        GetLinkDetail getLinkDetail=new GetLinkDetail().buildRequest(null,linKId,"200","1234");
        getLinkDetail.deleteContext("body.paymentFormId");
        JsonPath withDrawJson2 = getLinkDetail.execute().jsonPath();
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultMsg"),GET_LINK_NULL_MID);
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultCode"),GET_LINK_NULL_MID_CODE);
    }

    @Owner("Himanshu")
    @Test(description = "verify successful response IN GetLinkDetail API ")
    public void Link_Detail_0020() throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linKId = withDrawJson1.getString("body.linkId");
        GetLinkDetail getLinkDetail=new GetLinkDetail().buildRequest(mid,null,"200","1234");
        getLinkDetail.deleteContext("body.paymentFormId");
        JsonPath withDrawJson2 = getLinkDetail.execute().jsonPath();
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultMsg"),GET_LINK_NULL_LINKID);
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultCode"),GET_LINK_NULL_LINKID_CODE);
    }

    @Owner("Himanshu")
    @Test(description = "verify successful response IN GetLinkDetail API ")
    public void Link_Detail_0021() throws Exception {
        RestAssured.useRelaxedHTTPSValidation();
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        deleteFirsttemplate();
        SaveUpdateTemplate saveUpdateTemplate = new SaveUpdateTemplate(mid,"school form");
        saveUpdateTemplate.execute();
        FetchTemplate fetchTemplate =new FetchTemplate();
        fetchTemplate.buildRequest(mid);
        JsonPath fetchTemplateresponse=fetchTemplate.execute().jsonPath();
        String templateId=fetchTemplateresponse.getString("body.templates[0].id");
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","200");
        createNewLink.setContext("body.templateId",templateId);
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linKId = withDrawJson1.getString("body.linkId");
        SubmitUserForm SubmitUserForm=new SubmitUserForm();
        SubmitUserForm.buildRequest(linKId);
        JsonPath submitUserFormResponse=SubmitUserForm.execute().jsonPath();
        LinkHelper linkHelper=new LinkHelper();
        String paymentFormLink=submitUserFormResponse.getString("body.longUrl");
        String paymentFormId=linkHelper.getPaymentFormId(paymentFormLink);
        GetLinkDetail getLinkDetail=new GetLinkDetail().buildRequest(mid,linKId,"200","12340");
        getLinkDetail.setContext("body.paymentFormId",paymentFormId);
        JsonPath withDrawJson2 = getLinkDetail.execute().jsonPath();
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultMsg"),GET_LINK_DETAIL_MSG);
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultCode"),GET_LINK_DETAIL_CODE);
    }

    @Owner("Himanshu")
    @Feature("PGP-47918")
    @Test(description = "verify successful response IN GetLinkDetail API ")
    public void Link_Detail_0022() throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        GetLinkDetail getLinkDetail=new GetLinkDetail().buildRequest(mid,linkId,"200","1234");
        JsonPath withDrawJson2 = getLinkDetail.execute().jsonPath();
        String linkExchangeLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_exchange,linkId);

        Assertions.assertThat(linkExchangeLogs).contains(linkId);
        Assertions.assertThat(linkExchangeLogs).contains("\"resultStatus\":\"SUCCESS\"");
        Assertions.assertThat(linkExchangeLogs).contains("\"resultCode\":\"200\"");
    }

    @Owner("Himanshu")
    @Feature("PGP-47918")
    @Test(description = "verify successful response IN GetLinkDetail API with couponcode")
    public void Link_Detail_0023() throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink EdcLink=new CreateNewLink(mid,"HIMANSHU");
        JsonPath withDrawJson1 = EdcLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        GetLinkDetail getLinkDetail=new GetLinkDetail().buildRequest(mid,null,"200","1234");
        JsonPath withDrawJson2 = getLinkDetail.execute().jsonPath();
        String linkExchangeLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_exchange,linkId);

        Assertions.assertThat(linkExchangeLogs).contains(linkId);
        Assertions.assertThat(linkExchangeLogs).contains("couponCode");
        Assertions.assertThat(linkExchangeLogs).contains("\"resultStatus\":\"SUCCESS\"");
        Assertions.assertThat(linkExchangeLogs).contains("\"resultCode\":\"200\"");
    }

    @Owner("Himanshu")
    @Test(description = "verify failure response IN GetLinkDetail API.")
    public void Link_Detail_0024() throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        GetLinkDetail getLinkDetail=new GetLinkDetail().buildRequest(mid,"9999999999","200","1234");
        JsonPath withDrawJson2 = getLinkDetail.execute().jsonPath();
        String linkExchangeLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_exchange,"9999999999");

        Assertions.assertThat(linkExchangeLogs).contains("\"status\":\"F\"");
        Assertions.assertThat(linkExchangeLogs).contains("\"responseCode\":\"404\"");
    }


}

