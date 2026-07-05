package scripts.api.linkservice;

import com.paytm.ServerConfigProvider;
import com.paytm.api.MappingService.MerchantAddPreferenceInfo;
import com.paytm.api.linkAPI.CreateNewLink;
import com.paytm.api.linkAPI.FetchLinkApi;
import com.paytm.api.linkAPI.LinkHelper;
import com.paytm.api.linkAPI.SaveDefaultSettings;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.mappingService.addMerchantPreferenceReq.MerchantAddPreferenceInfoReq;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.LinkPaymentLoginPage;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.paytm.apphelpers.LogsValidationHelper.getLogsOnServer;

public class MaxPaymentLimitTask extends PGPBaseTest {
    User user;
    String mid;
    void setUserAndMId(String merchant) throws Exception {
        user = userManager.getForWrite(Label.LINKZEROWALLET);
        mid = merchant;
    }
    boolean verifySafetyTipNotPresent( CashierPage cashierPage){
        if(!cashierPage.safetyPopup().isElementPresent()){
            return true;
        }
        return false;
    }

    @Owner("Nirottam")
    @Feature("PGP-40397")
    @Parameters({"theme"})
    @Test(description = "verify Safety Tip should be visible when MAX_LINK_PAYMENTS_CHECK_DISABLED is Y for SD Merchant")
    public void MAXPAYMENTLIMIT_001(@Optional("checkoutjs_web_revamp")String theme) throws Exception {
        LinkHelper linkHelper=new LinkHelper();
        setUserAndMId(Constants.MerchantType.LINK_SD.getId().toString());
        linkHelper.setPrefOnMerchant(mid,"MAX_LINK_PAYMENTS_CHECK_DISABLED","ACTIVE","Y");
        linkHelper.setPrefOnMerchant(mid,"Disable_Payment_Link_Warning_Message","ACTIVE","N");

        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","1");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForSD(user,paymentLink,"FIXED","web","SD","1");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        String safetyTip = cashierPage.safetyPopup().getText();
        String safetyMessage = cashierPage.safetyMessage().getText();
        Assert.assertEquals(safetyTip.contains("Safety Tip"),true);
        Assert.assertEquals(safetyMessage.contains("Please ensure you are paying only to a trusted merchant"),true);
    }
    @Owner("Nirottam")
    @Feature("PGP-40397")
    @Parameters({"theme"})
    @Test(description = "verify Safety Tip should be visible when MAX_LINK_PAYMENTS_CHECK_DISABLED is Y for SD Merchant for Generic Link")
    public void MAXPAYMENTLIMIT_002(@Optional("checkoutjs_web_revamp")String theme) throws Exception {
        LinkHelper linkHelper=new LinkHelper();
        setUserAndMId(Constants.MerchantType.LINK_SD.getId().toString());
        linkHelper.setPrefOnMerchant(mid,"MAX_LINK_PAYMENTS_CHECK_DISABLED","ACTIVE","Y");
        linkHelper.setPrefOnMerchant(mid,"Disable_Payment_Link_Warning_Message","ACTIVE","N");

        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"GENERIC","1");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForSD(user,paymentLink,"GENERIC","web","SD","1");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        String safetyTip = cashierPage.safetyPopup().getText();
        String safetyMessage = cashierPage.safetyMessage().getText();
        Assert.assertEquals(safetyTip.contains("Safety Tip"),true);
        Assert.assertEquals(safetyMessage.contains("Please ensure you are paying only to a trusted merchant"),true);
    }
    @Owner("Nirottam")
    @Feature("PGP-40397")
    @Parameters({"theme"})
    @Test(description = "verify Safety Tip should be visible when MAX_LINK_PAYMENTS_CHECK_DISABLED is Y for SD Merchant for Invoice Link")
    public void MAXPAYMENTLIMIT_003(@Optional("checkoutjs_web_revamp")String theme) throws Exception {
        LinkHelper linkHelper=new LinkHelper();
        setUserAndMId(Constants.MerchantType.LINK_SD.getId().toString());
        linkHelper.setPrefOnMerchant(mid,"MAX_LINK_PAYMENTS_CHECK_DISABLED","ACTIVE","Y");
        linkHelper.setPrefOnMerchant(mid,"Disable_Payment_Link_Warning_Message","ACTIVE","N");

        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"INVOICE","1");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForSD(user,paymentLink,"INVOICE","web","SD","1");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        String safetyTip = cashierPage.safetyPopup().getText();
        String safetyMessage = cashierPage.safetyMessage().getText();
        Assert.assertEquals(safetyTip.contains("Safety Tip"),true);
        Assert.assertEquals(safetyMessage.contains("Please ensure you are paying only to a trusted merchant"),true);
    }


    @Owner("Nirottam")
    @Feature("PGP-40397")
    @Parameters({"theme"})
    @Test(description = "verify Safety Tip should not be visible when MAX_LINK_PAYMENTS_CHECK_DISABLED is N and Disable_Payment_Link_Warning_Message Is Y for SD Merchant for Fixed Link")
    public void MAXPAYMENTLIMIT_004( @Optional("checkoutjs_web_revamp")String theme) throws Exception {
        LinkHelper linkHelper=new LinkHelper();
        setUserAndMId(Constants.MerchantType.LINK_SD.getId().toString());
        linkHelper.setPrefOnMerchant(mid,"MAX_LINK_PAYMENTS_CHECK_DISABLED","ACTIVE","N");
        linkHelper.setPrefOnMerchant(mid,"Disable_Payment_Link_Warning_Message","ACTIVE","Y");

        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","1");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForSD(user,paymentLink,"FIXED","web","SD","1");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        verifySafetyTipNotPresent(cashierPage);
    }
    @Owner("Nirottam")
    @Feature("PGP-40397")
    @Parameters({"theme"})
    @Test(description = "verify Safety Tip should not be visible when MAX_LINK_PAYMENTS_CHECK_DISABLED is N and Disable_Payment_Link_Warning_Message Is Y for SD Merchant for GENERIC Link")
    public void MAXPAYMENTLIMIT_005(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        LinkHelper linkHelper=new LinkHelper();
        setUserAndMId(Constants.MerchantType.LINK_SD.getId().toString());
        linkHelper.setPrefOnMerchant(mid,"MAX_LINK_PAYMENTS_CHECK_DISABLED","ACTIVE","N");
        linkHelper.setPrefOnMerchant(mid,"Disable_Payment_Link_Warning_Message","ACTIVE","Y");

        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"GENERIC","1");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForSD(user,paymentLink,"GENERIC","web","SD","1");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        verifySafetyTipNotPresent(cashierPage);
    }
    @Owner("Nirottam")
    @Feature("PGP-40397")
    @Parameters({"theme"})
    @Test(description = "verify Safety Tip should not be visible when MAX_LINK_PAYMENTS_CHECK_DISABLED is N and Disable_Payment_Link_Warning_Message Is Y for SD Merchant for INVOICE Link")
    public void MAXPAYMENTLIMIT_006(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        LinkHelper linkHelper=new LinkHelper();
        setUserAndMId(Constants.MerchantType.LINK_SD.getId().toString());
        linkHelper.setPrefOnMerchant(mid,"MAX_LINK_PAYMENTS_CHECK_DISABLED","ACTIVE","N");
        linkHelper.setPrefOnMerchant(mid,"Disable_Payment_Link_Warning_Message","ACTIVE","Y");

        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"INVOICE","1");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForSD(user,paymentLink,"INVOICE","web","SD","1");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        verifySafetyTipNotPresent(cashierPage);
    }
    @Owner("Nirottam")
    @Feature("PGP-40397")
    @Parameters({"theme"})
    @Test(description = "verify Safety Tip should not be visible when MAX_LINK_PAYMENTS_CHECK_DISABLED is Y and Disable_Payment_Link_Warning_Message Is Y for SD Merchant FOR FIXED LINK")
    public void MAXPAYMENTLIMIT_007(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        LinkHelper linkHelper=new LinkHelper();
        setUserAndMId(Constants.MerchantType.LINK_SD.getId().toString());
        linkHelper.setPrefOnMerchant(mid,"MAX_LINK_PAYMENTS_CHECK_DISABLED","ACTIVE","Y");
        linkHelper.setPrefOnMerchant(mid,"Disable_Payment_Link_Warning_Message","ACTIVE","Y");

        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","1");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForSD(user,paymentLink,"FIXED","web","SD","1");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        verifySafetyTipNotPresent(cashierPage);
    }
    @Owner("Nirottam")
    @Feature("PGP-40397")
    @Parameters({"theme"})
    @Test(description = "verify Safety Tip should not be visible when MAX_LINK_PAYMENTS_CHECK_DISABLED is Y and Disable_Payment_Link_Warning_Message Is Y for SD Merchant FOR GENERIC LINK")
    public void MAXPAYMENTLIMIT_008(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        LinkHelper linkHelper=new LinkHelper();
        setUserAndMId(Constants.MerchantType.LINK_SD.getId().toString());
        linkHelper.setPrefOnMerchant(mid,"MAX_LINK_PAYMENTS_CHECK_DISABLED","ACTIVE","Y");
        linkHelper.setPrefOnMerchant(mid,"Disable_Payment_Link_Warning_Message","ACTIVE","Y");

        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"GENERIC","1");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForSD(user,paymentLink,"GENERIC","web","SD","1");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        verifySafetyTipNotPresent(cashierPage);
    }
    @Owner("Nirottam")
    @Feature("PGP-40397")
    @Parameters({"theme"})
    @Test(description = "verify Safety Tip should not be visible when MAX_LINK_PAYMENTS_CHECK_DISABLED is Y and Disable_Payment_Link_Warning_Message Is Y for SD Merchant FOR INVOICE LINK")
    public void MAXPAYMENTLIMIT_009(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        LinkHelper linkHelper=new LinkHelper();
        setUserAndMId(Constants.MerchantType.LINK_SD.getId().toString());
        linkHelper.setPrefOnMerchant(mid,"MAX_LINK_PAYMENTS_CHECK_DISABLED","ACTIVE","Y");
        linkHelper.setPrefOnMerchant(mid,"Disable_Payment_Link_Warning_Message","ACTIVE","Y");

        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"INVOICE","1");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForSD(user,paymentLink,"INVOICE","web","SD","1");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        verifySafetyTipNotPresent(cashierPage);
    }
    @Owner("Nirottam")
    @Feature("PGP-40397")
    @Parameters({"theme"})
    @Test(description = "verify MaxPaymentAllowed value doing Successfull Txn when MAX_LINK_PAYMENTS_CHECK_DISABLED is Y for SD Merchant FOR FIXED LINK")
    public void MAXPAYMENTLIMIT_0010(@Optional("checkoutjs_web_revamp")String theme) throws Exception {
        LinkHelper linkHelper=new LinkHelper();
        setUserAndMId(Constants.MerchantType.LINK_SD.getId().toString());
        linkHelper.setPrefOnMerchant(mid,"MAX_LINK_PAYMENTS_CHECK_DISABLED","ACTIVE","Y");
        linkHelper.setPrefOnMerchant(mid,"Disable_Payment_Link_Warning_Message","ACTIVE","N");

        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","1");
        int noOfMaxPaymentAllowed=2;
        createNewLink.setContext("body.maxPaymentsAllowed",noOfMaxPaymentAllowed);
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        String typeOfLink = withDrawJson1.getString("body.linkType");
        linkHelper.verifyFieldsInFetchLinkResponse(linkId,mid,"maxPaymentsAllowed","2");

        for(int i=0;i<=noOfMaxPaymentAllowed;i++) {
            LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
            if(i==noOfMaxPaymentAllowed ){
                    linkHelper.verifyIsLinkPaid(paymentLink);
                    break;
            }
            linkPaymentLoginPage.openLinkAndSubmitOTPForSD(user,paymentLink,"FIXED","web","SD","1");
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.waitUntilLoads();
            String safetyTip = cashierPage.safetyPopup().getText();
            String safetyMessage = cashierPage.safetyMessage().getText();
            Assert.assertEquals(safetyTip.contains("Safety Tip"), true);
            Assert.assertEquals(safetyMessage.contains("Please ensure you are paying only to a trusted merchant"), true);
            cashierPage.payByNBForNewFlow();
            Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Paid Successfully");
        }
    }
    @Owner("Nirottam")
    @Feature("PGP-40397")
    @Parameters({"theme"})
    @Test(description = "verify MaxPaymentAllowed value doing Successfull Txn when MAX_LINK_PAYMENTS_CHECK_DISABLED is Y for SD Merchant FOR GENERIC LINK")
    public void MAXPAYMENTLIMIT_0011(@Optional("checkoutjs_web_revamp")String theme) throws Exception {
        LinkHelper linkHelper=new LinkHelper();
        setUserAndMId(Constants.MerchantType.LINK_SD.getId().toString());
        linkHelper.setPrefOnMerchant(mid,"MAX_LINK_PAYMENTS_CHECK_DISABLED","ACTIVE","Y");
        linkHelper.setPrefOnMerchant(mid,"Disable_Payment_Link_Warning_Message","ACTIVE","N");

        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"GENERIC","1");
        int noOfMaxPaymentAllowed=2;
        createNewLink.setContext("body.maxPaymentsAllowed",noOfMaxPaymentAllowed);
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        String typeOfLink = withDrawJson1.getString("body.linkType");
        linkHelper.verifyFieldsInFetchLinkResponse(linkId,mid,"maxPaymentsAllowed","2");

        for(int i=0;i<=noOfMaxPaymentAllowed;i++) {
            LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
            if(i==noOfMaxPaymentAllowed ){
                    linkHelper.verifyIsLinkPaid(paymentLink);
                    break;
            }
            linkPaymentLoginPage.openLinkAndSubmitOTPForSD(user,paymentLink,"GENERIC","web","SD","1");
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.waitUntilLoads();
            String safetyTip = cashierPage.safetyPopup().getText();
            String safetyMessage = cashierPage.safetyMessage().getText();
            Assert.assertEquals(safetyTip.contains("Safety Tip"), true);
            Assert.assertEquals(safetyMessage.contains("Please ensure you are paying only to a trusted merchant"), true);
            cashierPage.payByNBForNewFlow();
            Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Paid Successfully");
        }
    }
    @Owner("Nirottam")
    @Feature("PGP-40397")
    @Parameters({"theme"})
    @Test(description = "verify MaxPaymentAllowed value doing Successfull Txn when MAX_LINK_PAYMENTS_CHECK_DISABLED is Y for SD Merchant FOR INVOICE LINK")
    public void MAXPAYMENTLIMIT_0012(@Optional("checkoutjs_web_revamp")String theme) throws Exception {
        LinkHelper linkHelper=new LinkHelper();
        setUserAndMId(Constants.MerchantType.LINK_SD.getId().toString());
        linkHelper.setPrefOnMerchant(mid,"MAX_LINK_PAYMENTS_CHECK_DISABLED","ACTIVE","Y");
        linkHelper.setPrefOnMerchant(mid,"Disable_Payment_Link_Warning_Message","ACTIVE","N");

        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"INVOICE","1");
        int noOfMaxPaymentAllowed=2;
        createNewLink.setContext("body.maxPaymentsAllowed",noOfMaxPaymentAllowed);
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        String typeOfLink = withDrawJson1.getString("body.linkType");
        linkHelper.verifyFieldsInFetchLinkResponse(linkId,mid,"maxPaymentsAllowed","2");

        for(int i=0;i<=noOfMaxPaymentAllowed;i++) {
            LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
            if((typeOfLink.equals("INVOICE")&&i==1) ){
                    linkPaymentLoginPage.launchLoginPage(paymentLink);
                    linkHelper.verifyInvoiceIsPaid();
                    break;
            }
            linkPaymentLoginPage.openLinkAndSubmitOTPForSD(user,paymentLink,"INVOICE","web","SD","1");
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.waitUntilLoads();
            String safetyTip = cashierPage.safetyPopup().getText();
            String safetyMessage = cashierPage.safetyMessage().getText();
            Assert.assertEquals(safetyTip.contains("Safety Tip"), true);
            Assert.assertEquals(safetyMessage.contains("Please ensure you are paying only to a trusted merchant"), true);
            cashierPage.payByNBForNewFlow();
            Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Paid Successfully");
        }
    }
    @Owner("Nirottam")
    @Feature("PGP-37787")
    @Parameters({"theme"})
    @Test(description = "verify MaxPaymentAllowed value(txn count will be according to property in linkservice but will save as Null in DB) doing Successfull Txn when MAX_LINK_PAYMENTS_CHECK_DISABLED is N for SD Merchant FOR FIXED LINK")
    public void MAXPAYMENTLIMIT_0013(@Optional("checkoutjs_web_revamp")String theme) throws Exception {
        LinkHelper linkHelper=new LinkHelper();
        setUserAndMId(Constants.MerchantType.LINK_SD.getId().toString());

        linkHelper.setPrefOnMerchant(mid,"MAX_LINK_PAYMENTS_CHECK_DISABLED","ACTIVE","N");
        linkHelper.setPrefOnMerchant(mid,"Disable_Payment_Link_Warning_Message","ACTIVE","N");

        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","1");
        int noOfMaxPaymentAllowed=1;
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        String typeOfLink = withDrawJson1.getString("body.linkType");
        linkHelper.verifyFieldsInFetchLinkResponse(linkId,mid,"maxPaymentsAllowed",null);
        for(int i=0;i<=noOfMaxPaymentAllowed;i++) {
            LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
            if(i==noOfMaxPaymentAllowed ){
                    linkHelper.verifyIsLinkPaid(paymentLink);
                  break;
            }
            linkPaymentLoginPage.openLinkAndSubmitOTPForSD(user,paymentLink,"FIXED","web","SD","1");
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.waitUntilLoads();
            String safetyTip = cashierPage.safetyPopup().getText();
            String safetyMessage = cashierPage.safetyMessage().getText();
            Assert.assertEquals(safetyTip.contains("Safety Tip"), true);
            Assert.assertEquals(safetyMessage.contains("Please ensure you are paying only to a trusted merchant"), true);
            cashierPage.payByNBForNewFlow();
            Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Paid Successfully");
        }
    }
    @Owner("Nirottam")
    @Feature("PGP-37787")
    @Parameters({"theme"})
    @Test(description = "verify MaxPaymentAllowed value(txn count will be according to property in linkservice but will save as Null in DB) doing Successfull Txn when MAX_LINK_PAYMENTS_CHECK_DISABLED is N for SD Merchant FOR GENERIC LINK")
    public void MAXPAYMENTLIMIT_0014(@Optional("checkoutjs_web_revamp")String theme) throws Exception {
        LinkHelper linkHelper=new LinkHelper();
        setUserAndMId(Constants.MerchantType.LINK_SD.getId().toString());

        linkHelper.setPrefOnMerchant(mid,"MAX_LINK_PAYMENTS_CHECK_DISABLED","ACTIVE","N");
        linkHelper.setPrefOnMerchant(mid,"Disable_Payment_Link_Warning_Message","ACTIVE","N");

        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"GENERIC","1");
        int noOfMaxPaymentAllowed=1;
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        String typeOfLink = withDrawJson1.getString("body.linkType");
        linkHelper.verifyFieldsInFetchLinkResponse(linkId,mid,"maxPaymentsAllowed",null);
        for(int i=0;i<=noOfMaxPaymentAllowed;i++) {
            LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
            if(i==noOfMaxPaymentAllowed  ){
                    linkHelper.verifyIsLinkPaid(paymentLink);
                  break;
            }
            linkPaymentLoginPage.openLinkAndSubmitOTPForSD(user,paymentLink,"GENERIC","web","SD","1");
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.waitUntilLoads();
            String safetyTip = cashierPage.safetyPopup().getText();
            String safetyMessage = cashierPage.safetyMessage().getText();
            Assert.assertEquals(safetyTip.contains("Safety Tip"), true);
            Assert.assertEquals(safetyMessage.contains("Please ensure you are paying only to a trusted merchant"), true);
            cashierPage.payByNBForNewFlow();
            Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Paid Successfully");
        }
    }
    @Owner("Nirottam")
    @Feature("PGP-37787")
    @Parameters({"theme"})
    @Test(description = "verify MaxPaymentAllowed value(txn count will be according to property in linkservice but will save as Null in DB) doing Successfull Txn when MAX_LINK_PAYMENTS_CHECK_DISABLED is N for SD Merchant FOR INVOICE LINK")
    public void MAXPAYMENTLIMIT_0015(@Optional("checkoutjs_web_revamp")String theme) throws Exception {
        LinkHelper linkHelper=new LinkHelper();
        setUserAndMId(Constants.MerchantType.LINK_SD.getId().toString());

        linkHelper.setPrefOnMerchant(mid,"MAX_LINK_PAYMENTS_CHECK_DISABLED","ACTIVE","N");
        linkHelper.setPrefOnMerchant(mid,"Disable_Payment_Link_Warning_Message","ACTIVE","N");

        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"INVOICE","1");
        int noOfMaxPaymentAllowed=1;
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        String typeOfLink = withDrawJson1.getString("body.linkType");
        linkHelper.verifyFieldsInFetchLinkResponse(linkId,mid,"maxPaymentsAllowed",null);
        for(int i=0;i<=noOfMaxPaymentAllowed;i++) {
            LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
            if( (typeOfLink.equals("INVOICE")&&i==1) ){
                    linkPaymentLoginPage.launchLoginPage(paymentLink);
                    linkHelper.verifyInvoiceIsPaid();
                      break;
            }
            linkPaymentLoginPage.openLinkAndSubmitOTPForSD(user,paymentLink,"INVOICE","web","SD","1");
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.waitUntilLoads();
            String safetyTip = cashierPage.safetyPopup().getText();
            String safetyMessage = cashierPage.safetyMessage().getText();
            Assert.assertEquals(safetyTip.contains("Safety Tip"), true);
            Assert.assertEquals(safetyMessage.contains("Please ensure you are paying only to a trusted merchant"), true);
            cashierPage.payByNBForNewFlow();
            Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Paid Successfully");
        }
    }
    @Owner("Nirottam")
    @Feature("PGP-40397")
    @Parameters({"theme"})
    @Test(description = "verify MaxPaymentAllowed value(it will be according to property in link-service.properties but will save as NULL In DB) doing Successfull Txn when MAX_LINK_PAYMENTS_CHECK_DISABLED is N for SD Merchant FOR FIXED LINK")
    public void MAXPAYMENTLIMIT_0016(@Optional("checkoutjs_web_revamp")String theme) throws Exception {
        LinkHelper linkHelper=new LinkHelper();
        setUserAndMId(Constants.MerchantType.LINK_SD.getId().toString());
        linkHelper.setPrefOnMerchant(mid,"MAX_LINK_PAYMENTS_CHECK_DISABLED","ACTIVE","N");
        linkHelper.setPrefOnMerchant(mid,"Disable_Payment_Link_Warning_Message","ACTIVE","Y");

        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","1");
        int noOfMaxPaymentAllowed=1;
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        String typeOfLink = withDrawJson1.getString("body.linkType");
        linkHelper.verifyFieldsInFetchLinkResponse(linkId,mid,"maxPaymentsAllowed",null);
        for(int i=0;i<=noOfMaxPaymentAllowed;i++) {
            LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
            if(i==noOfMaxPaymentAllowed){
                    linkHelper.verifyIsLinkPaid(paymentLink);
                    break;
            }
            linkPaymentLoginPage.openLinkAndSubmitOTPForSD(user,paymentLink,"FIXED","web","SD","1");
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.waitUntilLoads();
            verifySafetyTipNotPresent(cashierPage);
            cashierPage.payByNBForNewFlow();
            Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Paid Successfully");
        }
    }
    @Owner("Nirottam")
    @Feature("PGP-40397")
    @Parameters({"theme"})
    @Test(description = "verify MaxPaymentAllowed value(it will be according to property in link-service.properties but will save as NULL In DB) doing Successfull Txn when MAX_LINK_PAYMENTS_CHECK_DISABLED is N for SD Merchant FOR GENERIC LINK")
    public void MAXPAYMENTLIMIT_0017(@Optional("checkoutjs_web_revamp")String theme) throws Exception {
        LinkHelper linkHelper=new LinkHelper();
        setUserAndMId(Constants.MerchantType.LINK_SD.getId().toString());
        linkHelper.setPrefOnMerchant(mid,"MAX_LINK_PAYMENTS_CHECK_DISABLED","ACTIVE","N");
        linkHelper.setPrefOnMerchant(mid,"Disable_Payment_Link_Warning_Message","ACTIVE","Y");

        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"GENERIC","1");
        int noOfMaxPaymentAllowed=1;
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        String typeOfLink = withDrawJson1.getString("body.linkType");
        linkHelper.verifyFieldsInFetchLinkResponse(linkId,mid,"maxPaymentsAllowed",null);
        for(int i=0;i<=noOfMaxPaymentAllowed;i++) {
            LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
            if(i==noOfMaxPaymentAllowed){
                    linkHelper.verifyIsLinkPaid(paymentLink);
                    break;
            }
            linkPaymentLoginPage.openLinkAndSubmitOTPForSD(user,paymentLink,"GENERIC","web","SD","1");
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.waitUntilLoads();
            verifySafetyTipNotPresent(cashierPage);
            cashierPage.payByNBForNewFlow();
            Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Paid Successfully");
        }
    }
    @Owner("Nirottam")
    @Feature("PGP-40397")
    @Parameters({"theme"})
    @Test(description = "verify MaxPaymentAllowed value(it will be according to property in link-service.properties but will save as NULL In DB) doing Successfull Txn when MAX_LINK_PAYMENTS_CHECK_DISABLED is N for SD Merchant FOR INVOICE LINK")
    public void MAXPAYMENTLIMIT_0018(@Optional("checkoutjs_web_revamp")String theme) throws Exception {
        LinkHelper linkHelper=new LinkHelper();
        setUserAndMId(Constants.MerchantType.LINK_SD.getId().toString());
        linkHelper.setPrefOnMerchant(mid,"MAX_LINK_PAYMENTS_CHECK_DISABLED","ACTIVE","N");
        linkHelper.setPrefOnMerchant(mid,"Disable_Payment_Link_Warning_Message","ACTIVE","Y");

        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"INVOICE","1");
        int noOfMaxPaymentAllowed=1;
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        String typeOfLink = withDrawJson1.getString("body.linkType");
        linkHelper.verifyFieldsInFetchLinkResponse(linkId,mid,"maxPaymentsAllowed",null);
        for(int i=0;i<=noOfMaxPaymentAllowed;i++) {
            LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
            if( (typeOfLink.equals("INVOICE")&&i==1) ){
                    linkPaymentLoginPage.launchLoginPage(paymentLink);
                    linkHelper.verifyInvoiceIsPaid();
                     break;
            }
            linkPaymentLoginPage.openLinkAndSubmitOTPForSD(user,paymentLink,"INVOICE","web","SD","1");
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.waitUntilLoads();
            verifySafetyTipNotPresent(cashierPage);
            cashierPage.payByNBForNewFlow();
            Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Paid Successfully");
        }
    }
    @Owner("Nirottam")
    @Feature("PGP-40397")
    @Parameters({"theme"})
    @Test(description = "verify MaxPaymentAllowed value(While Setting MaxPaymentsAllowed to 2 in create Link Request) doing Successfull Txn when Limit_Max_Payment_On_Link is Y for NON SD Merchant for FIXED LINK")
    public void MAXPAYMENTLIMIT_0019(@Optional("checkoutjs_web_revamp")String theme) throws Exception {
        LinkHelper linkHelper=new LinkHelper();
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        linkHelper.setPrefOnMerchant(mid,"Limit_Max_Payment_On_Link","ACTIVE","Y");
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","1");
        int noOfMaxPaymentAllowed=2;
        createNewLink.setContext("body.maxPaymentsAllowed",noOfMaxPaymentAllowed);
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        String typeOfLink = withDrawJson1.getString("body.linkType");
        linkHelper.verifyFieldsInFetchLinkResponse(linkId,mid,"maxPaymentsAllowed","2");
        for(int i=0;i<=noOfMaxPaymentAllowed;i++) {
            LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
            if(i==noOfMaxPaymentAllowed){
                    linkHelper.verifyIsLinkPaid(paymentLink);
                break;
            }
            linkPaymentLoginPage.openLinkAndSubmitOTPForSD(user,paymentLink,"FIXED","web","NONSD","1");
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.waitUntilLoads();
            verifySafetyTipNotPresent(cashierPage);
            cashierPage.payByNBForNewFlow();
            Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Paid Successfully");
        }
    }
    @Owner("Nirottam")
    @Feature("PGP-40397")
    @Parameters({"theme"})
    @Test(description = "verify MaxPaymentAllowed value(While Setting MaxPaymentsAllowed to 2 in create Link Request) doing Successfull Txn when Limit_Max_Payment_On_Link is Y for NON SD Merchant for GENERIC LINK")
    public void MAXPAYMENTLIMIT_0020(@Optional("checkoutjs_web_revamp")String theme) throws Exception {
        LinkHelper linkHelper=new LinkHelper();
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        linkHelper.setPrefOnMerchant(mid,"Limit_Max_Payment_On_Link","ACTIVE","Y");
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"GENERIC","1");
        int noOfMaxPaymentAllowed=2;
        createNewLink.setContext("body.maxPaymentsAllowed",noOfMaxPaymentAllowed);
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        String typeOfLink = withDrawJson1.getString("body.linkType");
        linkHelper.verifyFieldsInFetchLinkResponse(linkId,mid,"maxPaymentsAllowed","2");
        for(int i=0;i<=noOfMaxPaymentAllowed;i++) {
            LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
            if(i==noOfMaxPaymentAllowed){
                linkHelper.verifyIsLinkPaid(paymentLink);
                break;
            }
            linkPaymentLoginPage.openLinkAndSubmitOTPForSD(user,paymentLink,"GENERIC","web","NONSD","1");
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.waitUntilLoads();
            verifySafetyTipNotPresent(cashierPage);
            cashierPage.payByNBForNewFlow();
            Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Paid Successfully");
        }
    }
    @Owner("Nirottam")
    @Feature("PGP-40397")
    @Parameters({"theme"})
    @Test(description = "verify MaxPaymentAllowed value(While Setting MaxPaymentsAllowed to 2 in create Link Request) doing Successfull Txn when Limit_Max_Payment_On_Link is Y for NON SD Merchant for INVOICE LINK")
    public void MAXPAYMENTLIMIT_0021(@Optional("checkoutjs_web_revamp")String theme) throws Exception {
        LinkHelper linkHelper=new LinkHelper();
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        linkHelper.setPrefOnMerchant(mid,"Limit_Max_Payment_On_Link","ACTIVE","Y");
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"INVOICE","1");
        int noOfMaxPaymentAllowed=2;
        createNewLink.setContext("body.maxPaymentsAllowed",noOfMaxPaymentAllowed);
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        String typeOfLink = withDrawJson1.getString("body.linkType");
        linkHelper.verifyFieldsInFetchLinkResponse(linkId,mid,"maxPaymentsAllowed","2");
        for(int i=0;i<=noOfMaxPaymentAllowed;i++) {
            LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
            if( (typeOfLink.equals("INVOICE")&&i==1) ){
                    linkPaymentLoginPage.launchLoginPage(paymentLink);
                    linkHelper.verifyInvoiceIsPaid();
                    break;
            }
            linkPaymentLoginPage.openLinkAndSubmitOTPForSD(user,paymentLink,"INVOICE","web","NONSD","1");
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.waitUntilLoads();
            verifySafetyTipNotPresent(cashierPage);
            cashierPage.payByNBForNewFlow();
            Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Paid Successfully");
        }
    }
    @Owner("Nirottam")
    @Feature("PGP-40397")
    @Parameters({"theme"})
    @Test(description = "verify MaxPaymentAllowed value(While Setting maxPaymentLimit to 2 Via SaveDefaultSetting API) doing Successfull Txn when Limit_Max_Payment_On_Link is Y for NON SD Merchant for FIXED LINK")
    public void MAXPAYMENTLIMIT_0022(@Optional("checkoutjs_web_revamp")String theme) throws Exception {
        LinkHelper linkHelper=new LinkHelper();
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        linkHelper.setPrefOnMerchant(mid,"Limit_Max_Payment_On_Link","ACTIVE","Y");
        SaveDefaultSettings saveDefaultSettings=new SaveDefaultSettings();
        saveDefaultSettings.setContext("body.mid",mid);
        saveDefaultSettings.setContext("body.maxPaymentLimit",2);
        saveDefaultSettings.deleteContext("body.reminderDetails.removeDefaultReminder");
        JsonPath saveDefaultSettingResponse=saveDefaultSettings.execute().jsonPath();
        String actualResultStatus=saveDefaultSettingResponse.getString("body.resultInfo.resultStatus");
        Assert.assertEquals(actualResultStatus,"SUCCESS");

        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","1");
        int noOfMaxPaymentAllowed=2;
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        String typeOfLink = withDrawJson1.getString("body.linkType");
        linkHelper.verifyFieldsInFetchLinkResponse(linkId,mid,"maxPaymentsAllowed","2");
        for(int i=0;i<=noOfMaxPaymentAllowed;i++) {
            LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
            if(i==noOfMaxPaymentAllowed){
                linkHelper.verifyIsLinkPaid(paymentLink);
                break;
            }
            linkPaymentLoginPage.openLinkAndSubmitOTPForSD(user,paymentLink,"FIXED","web","NONSD","1");
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.waitUntilLoads();
            verifySafetyTipNotPresent(cashierPage);
            cashierPage.payByNBForNewFlow();
            Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Paid Successfully");
        }
    }
    @Owner("Nirottam")
    @Feature("PGP-40397")
    @Parameters({"theme"})
    @Test(description = "verify MaxPaymentAllowed value(While Setting maxPaymentLimit to 2 Via SaveDefaultSetting API) doing Successfull Txn when Limit_Max_Payment_On_Link is Y for NON SD Merchant for FIXED LINK for Generic Link")
    public void MAXPAYMENTLIMIT_0023(@Optional("checkoutjs_web_revamp")String theme) throws Exception {
        LinkHelper linkHelper=new LinkHelper();
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        linkHelper.setPrefOnMerchant(mid,"Limit_Max_Payment_On_Link","ACTIVE","Y");
        SaveDefaultSettings saveDefaultSettings=new SaveDefaultSettings();
        saveDefaultSettings.setContext("body.mid",mid);
        saveDefaultSettings.setContext("body.maxPaymentLimit",2);
        saveDefaultSettings.deleteContext("body.reminderDetails.removeDefaultReminder");
        JsonPath saveDefaultSettingResponse=saveDefaultSettings.execute().jsonPath();
        String actualResultStatus=saveDefaultSettingResponse.getString("body.resultInfo.resultStatus");
        Assert.assertEquals(actualResultStatus,"SUCCESS");

        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"GENERIC","1");
        int noOfMaxPaymentAllowed=2;
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        String typeOfLink = withDrawJson1.getString("body.linkType");
        linkHelper.verifyFieldsInFetchLinkResponse(linkId,mid,"maxPaymentsAllowed","2");
        for(int i=0;i<=noOfMaxPaymentAllowed;i++) {
            LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
            if(i==noOfMaxPaymentAllowed){
                linkHelper.verifyIsLinkPaid(paymentLink);
                break;
            }
            linkPaymentLoginPage.openLinkAndSubmitOTPForSD(user,paymentLink,"GENERIC","web","NONSD","1");
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.waitUntilLoads();
            verifySafetyTipNotPresent(cashierPage);
            cashierPage.payByNBForNewFlow();
            Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Paid Successfully");
        }
    }
    @Owner("Nirottam")
    @Feature("PGP-40397")
    @Parameters({"theme"})
    @Test(description = "verify MaxPaymentAllowed value(While Setting maxPaymentLimit to 2 Via SaveDefaultSetting API) doing Successfull Txn when Limit_Max_Payment_On_Link is Y for NON SD Merchant for FIXED LINK for INVOICE Link")
    public void MAXPAYMENTLIMIT_0024(@Optional("checkoutjs_web_revamp")String theme) throws Exception {
        LinkHelper linkHelper=new LinkHelper();
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        linkHelper.setPrefOnMerchant(mid,"Limit_Max_Payment_On_Link","ACTIVE","Y");
        SaveDefaultSettings saveDefaultSettings=new SaveDefaultSettings();
        saveDefaultSettings.setContext("body.mid",mid);
        saveDefaultSettings.setContext("body.maxPaymentLimit",2);
        saveDefaultSettings.deleteContext("body.reminderDetails.removeDefaultReminder");
        JsonPath saveDefaultSettingResponse=saveDefaultSettings.execute().jsonPath();
        String actualResultStatus=saveDefaultSettingResponse.getString("body.resultInfo.resultStatus");
        Assert.assertEquals(actualResultStatus,"SUCCESS");

        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"INVOICE","1");
        int noOfMaxPaymentAllowed=2;
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        String typeOfLink = withDrawJson1.getString("body.linkType");
        linkHelper.verifyFieldsInFetchLinkResponse(linkId,mid,"maxPaymentsAllowed","2");
        for(int i=0;i<=noOfMaxPaymentAllowed;i++) {
            LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
            if((typeOfLink.equals("INVOICE")&&i==1) ){
                    linkPaymentLoginPage.launchLoginPage(paymentLink);
                    linkHelper.verifyInvoiceIsPaid();
                    break;
            }
            linkPaymentLoginPage.openLinkAndSubmitOTPForSD(user,paymentLink,"INVOICE","web","NONSD","1");
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.waitUntilLoads();
            verifySafetyTipNotPresent(cashierPage);
            cashierPage.payByNBForNewFlow();
            Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Paid Successfully");
        }
    }
    @Owner("Nirottam")
    @Feature("PGP-40397")
    @Parameters({"theme"})
    @Test(description = "verify MaxPaymentAllowed value(it will be 1 according to property in link-service.properties) doing Successfull Txn when Limit_Max_Payment_On_Link is Y for Non SD Merchant for Fixed Link")
    public void MAXPAYMENTLIMIT_0025(@Optional("checkoutjs_web_revamp")String theme) throws Exception {
        LinkHelper linkHelper=new LinkHelper();
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        linkHelper.setPrefOnMerchant(mid,"Limit_Max_Payment_On_Link","ACTIVE","Y");
        SaveDefaultSettings saveDefaultSettings=new SaveDefaultSettings();
        saveDefaultSettings.setContext("body.mid",mid);
        saveDefaultSettings.setContext("body.maxPaymentLimit",null);
        saveDefaultSettings.deleteContext("body.reminderDetails.removeDefaultReminder");
        JsonPath saveDefaultSettingResponse=saveDefaultSettings.execute().jsonPath();
        String actualResultStatus=saveDefaultSettingResponse.getString("body.resultInfo.resultStatus");
        Assert.assertEquals(actualResultStatus,"SUCCESS");

        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","1");
        int noOfMaxPaymentAllowed=1;
        createNewLink.setContext("body.maxPaymentsAllowed",noOfMaxPaymentAllowed);
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        String typeOfLink = withDrawJson1.getString("body.linkType");
        linkHelper.verifyFieldsInFetchLinkResponse(linkId,mid,"maxPaymentsAllowed","1");
        for(int i=0;i<=noOfMaxPaymentAllowed;i++) {
            LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
            if(i==noOfMaxPaymentAllowed){
                linkHelper.verifyIsLinkPaid(paymentLink);
                break;
            }
            linkPaymentLoginPage.openLinkAndSubmitOTPForSD(user,paymentLink,"FIXED","web","NONSD","1");
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.waitUntilLoads();
            verifySafetyTipNotPresent(cashierPage);
            cashierPage.payByNBForNewFlow();
            Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Paid Successfully");
        }
    }
    @Owner("Nirottam")
    @Feature("PGP-40397")
    @Parameters({"theme"})
    @Test(description = "verify MaxPaymentAllowed value(it will be 1 according to property in link-service.properties) doing Successfull Txn when Limit_Max_Payment_On_Link is Y for Non SD Merchant for GENERIC Link")
    public void MAXPAYMENTLIMIT_0026(@Optional("checkoutjs_web_revamp")String theme) throws Exception {
        LinkHelper linkHelper=new LinkHelper();
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        linkHelper.setPrefOnMerchant(mid,"Limit_Max_Payment_On_Link","ACTIVE","Y");
        SaveDefaultSettings saveDefaultSettings=new SaveDefaultSettings();
        saveDefaultSettings.setContext("body.mid",mid);
        saveDefaultSettings.setContext("body.maxPaymentLimit",null);
        saveDefaultSettings.deleteContext("body.reminderDetails.removeDefaultReminder");
        JsonPath saveDefaultSettingResponse=saveDefaultSettings.execute().jsonPath();
        String actualResultStatus=saveDefaultSettingResponse.getString("body.resultInfo.resultStatus");
        Assert.assertEquals(actualResultStatus,"SUCCESS");

        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"GENERIC","1");
        int noOfMaxPaymentAllowed=1;
        createNewLink.setContext("body.maxPaymentsAllowed",noOfMaxPaymentAllowed);
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        String typeOfLink = withDrawJson1.getString("body.linkType");
        linkHelper.verifyFieldsInFetchLinkResponse(linkId,mid,"maxPaymentsAllowed","1");
        for(int i=0;i<=noOfMaxPaymentAllowed;i++) {
            LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
            if(i==noOfMaxPaymentAllowed){
                linkHelper.verifyIsLinkPaid(paymentLink);
                break;
            }
            linkPaymentLoginPage.openLinkAndSubmitOTPForSD(user,paymentLink,"GENERIC","web","NONSD","1");
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.waitUntilLoads();
            verifySafetyTipNotPresent(cashierPage);
            cashierPage.payByNBForNewFlow();
            Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Paid Successfully");
        }
    }
    @Owner("Nirottam")
    @Feature("PGP-40397")
    @Parameters({"theme"})
    @Test(description = "verify MaxPaymentAllowed value(it will be 1 according to property in link-service.properties) doing Successfull Txn when Limit_Max_Payment_On_Link is Y for Non SD Merchant for INVOICE Link")
    public void MAXPAYMENTLIMIT_0027(@Optional("checkoutjs_web_revamp")String theme) throws Exception {
        LinkHelper linkHelper=new LinkHelper();
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        linkHelper.setPrefOnMerchant(mid,"Limit_Max_Payment_On_Link","ACTIVE","Y");
        SaveDefaultSettings saveDefaultSettings=new SaveDefaultSettings();
        saveDefaultSettings.setContext("body.mid",mid);
        saveDefaultSettings.setContext("body.maxPaymentLimit",null);
        saveDefaultSettings.deleteContext("body.reminderDetails.removeDefaultReminder");
        JsonPath saveDefaultSettingResponse=saveDefaultSettings.execute().jsonPath();
        String actualResultStatus=saveDefaultSettingResponse.getString("body.resultInfo.resultStatus");
        Assert.assertEquals(actualResultStatus,"SUCCESS");

        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"INVOICE","1");
        int noOfMaxPaymentAllowed=1;
        createNewLink.setContext("body.maxPaymentsAllowed",noOfMaxPaymentAllowed);
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        String typeOfLink = withDrawJson1.getString("body.linkType");
        linkHelper.verifyFieldsInFetchLinkResponse(linkId,mid,"maxPaymentsAllowed","1");
        for(int i=0;i<=noOfMaxPaymentAllowed;i++) {
            LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
            if((typeOfLink.equals("INVOICE")&&i==1) ){
                    linkPaymentLoginPage.launchLoginPage(paymentLink);
                    linkHelper.verifyInvoiceIsPaid();
                     break;
            }
            linkPaymentLoginPage.openLinkAndSubmitOTPForSD(user,paymentLink,"INVOICE","web","NONSD","1");
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.waitUntilLoads();
            verifySafetyTipNotPresent(cashierPage);
            cashierPage.payByNBForNewFlow();
            Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Paid Successfully");
        }
    }
    @Owner("Nirottam")
    @Feature("PGP-40397")
    @Parameters({"theme"})
    @Test(description = "verify MaxPaymentAllowed value(it will be Saved as Null In DB )  when Limit_Max_Payment_On_Link is N for Non SD Merchant for FIXED link")
    public void MAXPAYMENTLIMIT_0028(@Optional("checkoutjs_web_revamp")String theme) throws Exception {
        LinkHelper linkHelper=new LinkHelper();
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        linkHelper.setPrefOnMerchant(mid,"Limit_Max_Payment_On_Link","ACTIVE","N");
        SaveDefaultSettings saveDefaultSettings=new SaveDefaultSettings();
        saveDefaultSettings.setContext("body.mid",mid);
        saveDefaultSettings.deleteContext("body.reminderDetails.removeDefaultReminder");
        saveDefaultSettings.setContext("body.maxPaymentLimit",null);
        JsonPath saveDefaultSettingResponse=saveDefaultSettings.execute().jsonPath();
        String actualResultStatus=saveDefaultSettingResponse.getString("body.resultInfo.resultStatus");
        Assert.assertEquals(actualResultStatus,"SUCCESS");
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","1");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        String typeOfLink = withDrawJson1.getString("body.linkType");
        linkHelper.verifyFieldsInFetchLinkResponse(linkId,mid,"maxPaymentsAllowed",null);
            LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForSD(user,paymentLink,"FIXED","web","NONSD","1");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.waitUntilLoads();
            verifySafetyTipNotPresent(cashierPage);
            cashierPage.payByNBForNewFlow();
            Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Paid Successfully");

    }
    @Owner("Nirottam")
    @Feature("PGP-40397")
    @Parameters({"theme"})
    @Test(description = "verify MaxPaymentAllowed value(it will be Saved as Null In DB )  when Limit_Max_Payment_On_Link is N for Non SD Merchant for GENERIC link")
    public void MAXPAYMENTLIMIT_0029(@Optional("checkoutjs_web_revamp")String theme) throws Exception {
        LinkHelper linkHelper=new LinkHelper();
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        linkHelper.setPrefOnMerchant(mid,"Limit_Max_Payment_On_Link","ACTIVE","N");
        SaveDefaultSettings saveDefaultSettings=new SaveDefaultSettings();
        saveDefaultSettings.setContext("body.mid",mid);
        saveDefaultSettings.deleteContext("body.reminderDetails.removeDefaultReminder");
        saveDefaultSettings.setContext("body.maxPaymentLimit",null);
        JsonPath saveDefaultSettingResponse=saveDefaultSettings.execute().jsonPath();
        String actualResultStatus=saveDefaultSettingResponse.getString("body.resultInfo.resultStatus");
        Assert.assertEquals(actualResultStatus,"SUCCESS");
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"GENERIC","1");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        String typeOfLink = withDrawJson1.getString("body.linkType");
        linkHelper.verifyFieldsInFetchLinkResponse(linkId,mid,"maxPaymentsAllowed",null);
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForSD(user,paymentLink,"GENERIC","web","NONSD","1");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        verifySafetyTipNotPresent(cashierPage);
        cashierPage.payByNBForNewFlow();
        Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Paid Successfully");

    }
    @Owner("Nirottam")
    @Feature("PGP-40397")
    @Parameters({"theme"})
    @Test(description = "verify MaxPaymentAllowed value(it will be Saved as Null In DB )  when Limit_Max_Payment_On_Link is N for Non SD Merchant for INVOICE link")
    public void MAXPAYMENTLIMIT_0030(@Optional("checkoutjs_web_revamp")String theme) throws Exception {
        LinkHelper linkHelper=new LinkHelper();
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        linkHelper.setPrefOnMerchant(mid,"Limit_Max_Payment_On_Link","ACTIVE","N");
        SaveDefaultSettings saveDefaultSettings=new SaveDefaultSettings();
        saveDefaultSettings.setContext("body.mid",mid);
        saveDefaultSettings.deleteContext("body.reminderDetails.removeDefaultReminder");
        saveDefaultSettings.setContext("body.maxPaymentLimit",null);
        JsonPath saveDefaultSettingResponse=saveDefaultSettings.execute().jsonPath();
        String actualResultStatus=saveDefaultSettingResponse.getString("body.resultInfo.resultStatus");
        Assert.assertEquals(actualResultStatus,"SUCCESS");
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"INVOICE","1");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        String typeOfLink = withDrawJson1.getString("body.linkType");
        linkHelper.verifyFieldsInFetchLinkResponse(linkId,mid,"maxPaymentsAllowed",null);
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForSD(user,paymentLink,"INVOICE","web","NONSD","1");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        verifySafetyTipNotPresent(cashierPage);
        cashierPage.payByNBForNewFlow();
        Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Paid Successfully");

    }

}
