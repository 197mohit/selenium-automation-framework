package scripts.api.linkservice;

import com.paytm.api.linkAPI.CreateNewLink;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.LogsValidationHelper;
import com.paytm.apphelpers.PG2LogsValidationHelper;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.framework.reportportal.annotation.Owner;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.LinkPaymentLoginPage;
import io.qameta.allure.Feature;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.testng.Assert;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import static com.paytm.api.linkAPI.linkConstant.PGPAPIResourcePath.SINGLETXN_GENERIC_LINK;
import static com.paytm.api.linkAPI.linkConstant.PGPAPIResourcePath.SINGLETXN_GENERIC_LINK_CODE;

public class HdfcDigipos extends PGPBaseTest {
    User user;
    String mid;
    void setUserAndMId(String merchant) throws Exception {
        user = userManager.getForRead(Label.LINK);
        mid = merchant;
    }

    @Owner("Himanshu Arora")
    @Feature("PGP-50922")
    @Parameters({"theme"})
    @Test(description = "Verify isHDFCDigiPOSMerchant True for digipos link based on request parameters FIXED LINK.")
    public void HdfcDigipos_01(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.DIGIPOS_LINK.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, "FIXED", "1");
        createNewLink.setContext("body.isHDFCDigiPOSMerchant",true);
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId);
        Assertions.assertThat(linkServiceLogs).contains("isHDFCDigiPOSMerchant=true");
    }


    @Owner("Himanshu Arora")
    @Feature("PGP-50922")
    @Parameters({"theme"})
    @Test(description = "Verify isHDFCDigiPOSMerchant True for digipos link based on static preference FIXED LINK.")
    public void HdfcDigipos_02(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.DIGIPOS_LINK.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, "FIXED", "1");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId);
        Assertions.assertThat(linkServiceLogs).contains("isHDFCDigiPOSMerchant=true");
    }


    @Owner("Himanshu Arora")
    @Feature("PGP-50922")
    @Parameters({"theme"})
    @Test(description = "Verify isHDFCDigiPOSMerchant True for digipos link based on request parameters GENERIC LINK.")
    public void HdfcDigipos_03(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.DIGIPOS_LINK.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, "GENERIC", "1");
        createNewLink.setContext("body.isHDFCDigiPOSMerchant",true);
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"GENERIC","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId);
        Assertions.assertThat(linkServiceLogs).contains("isHDFCDigiPOSMerchant=true");
    }


    @Owner("Himanshu Arora")
    @Feature("PGP-50922")
    @Parameters({"theme"})
    @Test(description = "Verify isHDFCDigiPOSMerchant True for digipos link based on request parameters INVOICE LINK.")
    public void HdfcDigipos_04(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.DIGIPOS_LINK.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, "INVOICE", "1");
        createNewLink.setContext("body.isHDFCDigiPOSMerchant",true);
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"INVOICE","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId);
        Assertions.assertThat(linkServiceLogs).contains("isHDFCDigiPOSMerchant=true");
    }


    @Owner("Himanshu Arora")
    @Feature("PGP-50922")
    @Parameters({"theme"})
    @Test(description = "Verify isHDFCDigiPOSMerchant True for digipos link based on static preference GENERIC LINK.")
    public void HdfcDigipos_05(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.DIGIPOS_LINK.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, "GENERIC", "1");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"GENERIC","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId);
        Assertions.assertThat(linkServiceLogs).contains("isHDFCDigiPOSMerchant=true");
    }


    @Owner("Himanshu Arora")
    @Feature("PGP-50922")
    @Parameters({"theme"})
    @Test(description = "Verify isHDFCDigiPOSMerchant True for digipos link based on static preference INVOICE LINK.")
    public void HdfcDigipos_06(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.DIGIPOS_LINK.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, "INVOICE", "1");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"INVOICE","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId);
        Assertions.assertThat(linkServiceLogs).contains("isHDFCDigiPOSMerchant=true");
    }


    @Owner("Himanshu Arora")
    @Feature("PGP-50922")
    @Parameters({"theme"})
    @Test(description = "Verify disablePaymentMode upi for digipos link based on request parameters FIXED LINK.")
    public void HdfcDigipos_07(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.DIGIPOS_LINK.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, "FIXED", "1");
        createNewLink.setContext("body.isHDFCDigiPOSMerchant",true);
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId,"Payload for initiate transaction request");
        Assertions.assertThat(linkServiceLogs).contains("disablePaymentMode=[PaymentMode(mode=UPI, channels=null)]");
    }


    @Owner("Himanshu Arora")
    @Feature("PGP-50922")
    @Parameters({"theme"})
    @Test(description = "Verify disablePaymentMode upi for digipos link based on request parameters GENERIC LINK.")
    public void HdfcDigipos_08(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.DIGIPOS_LINK.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, "GENERIC", "1");
        createNewLink.setContext("body.isHDFCDigiPOSMerchant",true);
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"GENERIC","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId,"Payload for initiate transaction request");
        Assertions.assertThat(linkServiceLogs).contains("disablePaymentMode=[PaymentMode(mode=UPI, channels=null)]");
    }


    @Owner("Himanshu Arora")
    @Feature("PGP-50922")
    @Parameters({"theme"})
    @Test(description = "Verify disablePaymentMode upi for digipos link based on request parameters INVOICE LINK.")
    public void HdfcDigipos_09(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.DIGIPOS_LINK.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, "INVOICE", "1");
        createNewLink.setContext("body.isHDFCDigiPOSMerchant",true);
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"INVOICE","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId,"Payload for initiate transaction request");
        Assertions.assertThat(linkServiceLogs).contains("disablePaymentMode=[PaymentMode(mode=UPI, channels=null)]");
    }


    @Owner("Himanshu Arora")
    @Feature("PGP-50922")
    @Parameters({"theme"})
    @Test(description = "Verify disablePaymentMode upi for digipos link based on static preference FIXED LINK.")
    public void HdfcDigipos_10(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.DIGIPOS_LINK.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, "FIXED", "1");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId,"Payload for initiate transaction request");
        Assertions.assertThat(linkServiceLogs).contains("disablePaymentMode=[PaymentMode(mode=UPI, channels=null)]");
    }


    @Owner("Himanshu Arora")
    @Feature("PGP-50922")
    @Parameters({"theme"})
    @Test(description = "Verify disablePaymentMode upi for digipos link based on static preference GENERIC LINK.")
    public void HdfcDigipos_11(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.DIGIPOS_LINK.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, "GENERIC", "1");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"GENERIC","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId,"Payload for initiate transaction request");
        Assertions.assertThat(linkServiceLogs).contains("disablePaymentMode=[PaymentMode(mode=UPI, channels=null)]");
    }


    @Owner("Himanshu Arora")
    @Feature("PGP-50922")
    @Parameters({"theme"})
    @Test(description = "Verify disablePaymentMode upi for digipos link based on static preference INVOICE LINK.")
    public void HdfcDigipos_12(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.DIGIPOS_LINK.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, "INVOICE", "1");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"INVOICE","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId,"Payload for initiate transaction request");
        Assertions.assertThat(linkServiceLogs).contains("disablePaymentMode=[PaymentMode(mode=UPI, channels=null)]");
    }


    @Owner("Himanshu Arora")
    @Feature("PGP-50922")
    @Parameters({"theme"})
    @Test(description = "Verify success txn for digipos link based on request parameters FIXED LINK.")
    public void HdfcDigipos_13(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.DIGIPOS_LINK.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, "FIXED", "1");
        createNewLink.setContext("body.isHDFCDigiPOSMerchant",true);
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        cashierPage.SuccessfullScreen().waitUntilVisible();
        Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Paid Successfully");

        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId,"Payload for initiate transaction request");
        Assertions.assertThat(linkServiceLogs).contains("isHDFCDigiPOSMerchant=true");
    }


    @Owner("Himanshu Arora")
    @Feature("PGP-50922")
    @Parameters({"theme"})
    @Test(description = "Verify success txn for digipos link based on static preference FIXED LINK.")
    public void HdfcDigipos_14(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.DIGIPOS_LINK.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, "FIXED", "1");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        cashierPage.SuccessfullScreen().waitUntilVisible();
        Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Paid Successfully");

        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId,"Payload for initiate transaction request");
        Assertions.assertThat(linkServiceLogs).contains("isHDFCDigiPOSMerchant=true");
    }


    @Owner("Himanshu Arora")
    @Feature("PGP-50922")
    @Parameters({"theme"})
    @Test(description = "Verify isHDFCDigiPOSMerchant false for digipos link based on request parameters FIXED LINK.")
    public void HdfcDigipos_15(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_TXN_PG2.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, "FIXED", "1");
        createNewLink.setContext("body.isHDFCDigiPOSMerchant",false);
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId);
        Assertions.assertThat(linkServiceLogs).contains("isHDFCDigiPOSMerchant=false");
    }


    @Owner("Himanshu Arora")
    @Feature("PGP-50922")
    @Parameters({"theme"})
    @Test(description = "Verify isHDFCDigiPOSMerchant false for digipos link based on request parameters GENERIC LINK.")
    public void HdfcDigipos_16(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_TXN_PG2.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, "GENERIC", "1");
        createNewLink.setContext("body.isHDFCDigiPOSMerchant",false);
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"GENERIC","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId);
        Assertions.assertThat(linkServiceLogs).contains("isHDFCDigiPOSMerchant=false");
    }


    @Owner("Himanshu Arora")
    @Feature("PGP-50922")
    @Parameters({"theme"})
    @Test(description = "Verify isHDFCDigiPOSMerchant false for digipos link based on request parameters INVOICE LINK.")
    public void HdfcDigipos_17(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_TXN_PG2.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, "INVOICE", "1");
        createNewLink.setContext("body.isHDFCDigiPOSMerchant",false);
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"INVOICE","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId);
        Assertions.assertThat(linkServiceLogs).contains("isHDFCDigiPOSMerchant=false");
    }


    @Owner("Himanshu Arora")
    @Feature("PGP-50922")
    @Parameters({"theme"})
    @Test(description = "Verify success txn for digipos link based on request parameters GENERIC LINK.")
    public void HdfcDigipos_18(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.DIGIPOS_LINK.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, "GENERIC", "1");
        createNewLink.setContext("body.isHDFCDigiPOSMerchant",true);
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"GENERIC","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        cashierPage.SuccessfullScreen().waitUntilVisible();
        Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Paid Successfully");

        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId,"Payload for initiate transaction request");
        Assertions.assertThat(linkServiceLogs).contains("isHDFCDigiPOSMerchant=true");
    }


    @Owner("Himanshu Arora")
    @Feature("PGP-50922")
    @Parameters({"theme"})
    @Test(description = "Verify success txn for digipos link based on request parameters INVOICE LINK.")
    public void HdfcDigipos_19(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.DIGIPOS_LINK.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, "INVOICE", "1");
        createNewLink.setContext("body.isHDFCDigiPOSMerchant",true);
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"INVOICE","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        cashierPage.SuccessfullScreen().waitUntilVisible();
        Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Paid Successfully");

        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId,"Payload for initiate transaction request");
        Assertions.assertThat(linkServiceLogs).contains("isHDFCDigiPOSMerchant=true");
    }


    @Owner("Himanshu Arora")
    @Feature("PGP-50922")
    @Parameters({"theme"})
    @Test(description = "Verify success txn for digipos link based on static preference GENERIC LINK.")
    public void HdfcDigipos_20(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.DIGIPOS_LINK.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, "GENERIC", "1");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"GENERIC","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        cashierPage.SuccessfullScreen().waitUntilVisible();
        Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Paid Successfully");

        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId,"Payload for initiate transaction request");
        Assertions.assertThat(linkServiceLogs).contains("isHDFCDigiPOSMerchant=true");
    }


    @Owner("Himanshu Arora")
    @Feature("PGP-50922")
    @Parameters({"theme"})
    @Test(description = "Verify success txn for digipos link based on static preference INVOICE LINK.")
    public void HdfcDigipos_21(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.DIGIPOS_LINK.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, "INVOICE", "1");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"INVOICE","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        cashierPage.SuccessfullScreen().waitUntilVisible();
        Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Paid Successfully");

        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId,"Payload for initiate transaction request");
        Assertions.assertThat(linkServiceLogs).contains("isHDFCDigiPOSMerchant=true");
    }


}
