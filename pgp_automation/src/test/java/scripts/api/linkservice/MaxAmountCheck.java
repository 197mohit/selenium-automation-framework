package scripts.api.linkservice;

import com.paytm.api.linkAPI.CreateNewLink;
import com.paytm.api.linkAPI.templateApis.SaveUpdateTemplate;
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
import org.junit.Assert;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import static com.paytm.api.linkAPI.linkConstant.PGPAPIResourcePath.*;

public class MaxAmountCheck extends PGPBaseTest {
    User user;
    String mid;
    void setUserAndMId(String merchant) throws Exception {
        user = userManager.getForWrite(Label.LINK);
        mid = merchant;
    }

    @Owner("Anushka_Goldi")
    @Feature("PGP-40132")
    @Parameters({"theme"})
    @Test(description = "verify Txn is successful For FIXED LINK with amount 100 Crore")
    public void MaxAmountCheckFixed_01(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.MAX_AMOUNT_CHECK.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","1000000000");
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
    @Feature("PGP-40132")
    @Test(description = "verify Txn is not successful For FIXED LINK with amount more than 100 Crore")
    public void MaxAmountCheckFixed_02() throws Exception {
        setUserAndMId(Constants.MerchantType.MAX_AMOUNT_CHECK.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","1000000001");
        JsonPath withDrawJson = createNewLink.execute().jsonPath();
        Assert.assertEquals(withDrawJson.getString("body.resultInfo.resultMessage"),MSG_AMOUNT_MORETHAN_100CR);
        Assert.assertEquals(withDrawJson.getString("body.resultInfo.resultCode"),CODE_AMOUNT_MORETHAN_100CR);
        Assert.assertEquals(withDrawJson.getString("body.resultInfo.resultStatus"),CREATE_LINK_FAILED_STATUS);
    }

    @Owner("Anushka_Goldi")
    @Feature("PGP-40132")
    @Test(description = "verify Txn  is not successful For FIXED LINK with amount less than 1")
    public void MaxAmountCheckFixed_03() throws Exception {
        setUserAndMId(Constants.MerchantType.MAX_AMOUNT_CHECK.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","0.1");
        JsonPath withDrawJson = createNewLink.execute().jsonPath();
        Assert.assertEquals(withDrawJson.getString("body.resultInfo.resultMessage"),MSG_AMOUNT_LESSTHAN_1);
        Assert.assertEquals(withDrawJson.getString("body.resultInfo.resultCode"),CODE_AMOUNT_LESSTHAN_1);
        Assert.assertEquals(withDrawJson.getString("body.resultInfo.resultStatus"),CREATE_LINK_FAILED_STATUS);
    }

    @Owner("Anushka_Goldi")
    @Feature("PGP-40132")
    @Parameters({"theme"})
    @Test(description = "verify Txn For GENERIC LINK have max amount 99999999")
    public void MaxAmountCheckGeneric_01(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        RestAssured.useRelaxedHTTPSValidation();
        setUserAndMId(Constants.MerchantType.MAX_AMOUNT_CHECK1.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"GENERIC","99999999");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForSD(user,paymentLink,"GENERIC","web","OFFLINE","99999999");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payByNBForOldFlow();
        cashierPage.waitUntilLoads();
        Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Paid Successfully");
    }

    @Owner("Anushka_Goldi")
    @Feature("PGP-40132")
    @Parameters({"theme"})
    @Test(description = "verify Txn For GENERIC LINK with amount less than 1")
    public void MaxAmountCheckGeneric_02(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.MAX_AMOUNT_CHECK.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"GENERIC","0.1");
        JsonPath withDrawJson = createNewLink.execute().jsonPath();
        Assert.assertEquals(withDrawJson.getString("body.resultInfo.resultMessage"),MSG_AMOUNT_LESSTHAN_1);
        Assert.assertEquals(withDrawJson.getString("body.resultInfo.resultCode"),CODE_AMOUNT_LESSTHAN_1);
        Assert.assertEquals(withDrawJson.getString("body.resultInfo.resultStatus"),CREATE_LINK_FAILED_STATUS);
    }

    @Owner("Anushka_Goldi")
    @Feature("PGP-40132")
    @Parameters({"theme"})
    @Test(description = "verify Txn is not successful For GENERIC LINK with amount more than 10 Crore it will alter it to 8 digits number")
    public void MaxAmountCheckGeneric_03(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.MAX_AMOUNT_CHECK.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"GENERIC","9999999999");
        JsonPath withDrawJson = createNewLink.execute().jsonPath();
        Assert.assertEquals(withDrawJson.getString("body.resultInfo.resultMessage"),MSG_AMOUNT_MORETHAN_100CR);
        Assert.assertEquals(withDrawJson.getString("body.resultInfo.resultCode"),CODE_AMOUNT_MORETHAN_100CR);
        Assert.assertEquals(withDrawJson.getString("body.resultInfo.resultStatus"),CREATE_LINK_FAILED_STATUS);
    }

    @Owner("Anushka")
    @Feature("PGP-40132")
    @Test(description = "verify Txn is successful For Invoice LINK with amount 100 Crore")
    public void MaxAmountCheckInvoice_01(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.MAX_AMOUNT_CHECK.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"INVOICE","1000000000");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"INVOICE","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payByNBForOldFlow();
        cashierPage.waitUntilLoads();
        Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Paid Successfully");
    }

    @Owner("Anushka")
    @Feature("PGP-40132")
    @Test(description = "verify Txn is not successful For Invoice LINK with amount more than 100 Crore")
    public void MaxAmountCheckInvoice_02(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.MAX_AMOUNT_CHECK.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"INVOICE","1000000001");
        JsonPath withDrawJson = createNewLink.execute().jsonPath();
        Assert.assertEquals(withDrawJson.getString("body.resultInfo.resultMessage"),MSG_AMOUNT_MORETHAN_100CR);
        Assert.assertEquals(withDrawJson.getString("body.resultInfo.resultCode"),CODE_AMOUNT_MORETHAN_100CR);
        Assert.assertEquals(withDrawJson.getString("body.resultInfo.resultStatus"),CREATE_LINK_FAILED_STATUS);
    }

    @Owner("Anushka")
    @Feature("PGP-40132")
    @Test(description = "verify Txn is not successful For Invoice LINK with amount less than 1")
    public void MaxAmountCheckInvoice_03(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.MAX_AMOUNT_CHECK.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"INVOICE","0.1");
        JsonPath withDrawJson = createNewLink.execute().jsonPath();
        Assert.assertEquals(withDrawJson.getString("body.resultInfo.resultMessage"),MSG_AMOUNT_LESSTHAN_1);
        Assert.assertEquals(withDrawJson.getString("body.resultInfo.resultCode"),CODE_AMOUNT_LESSTHAN_1);
        Assert.assertEquals(withDrawJson.getString("body.resultInfo.resultStatus"),CREATE_LINK_FAILED_STATUS);
    }

    @Owner("Anushka")
    @Feature("PGP-40132")
    @Test(description = "verify Txn is successful For Payment Btn LINK with amount 100 crore")
    public void MaxAmountCheckPaymentBtn_01(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.MAX_AMOUNT_CHECK.getId().toString());
        CreateNewLink  createNewLinkPaymentBtn= new CreateNewLink();
        createNewLinkPaymentBtn.buildRequest(mid, "PAYMENT_BUTTON", "1000000000");
        createNewLinkPaymentBtn.deleteContext("body.sendSms");
        createNewLinkPaymentBtn.deleteContext("body.sendEmail");
        createNewLinkPaymentBtn.deleteContext("body.customerContact");
        JsonPath withDrawJson1 = createNewLinkPaymentBtn.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"PAYMENT_BUTTON","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payByNBForOldFlow();
        cashierPage.waitUntilLoads();
        Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Paid Successfully");
    }

    @Owner("Anushka")
    @Feature("PGP-40132")
    @Test(description = "verify Txn is not successful For Payment Btn LINK with amount less than 1rs ")
    public void MaxAmountCheckPaymentBtn_02(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.MAX_AMOUNT_CHECK.getId().toString());
        CreateNewLink  createNewLinkPaymentBtn= new CreateNewLink();
        createNewLinkPaymentBtn.buildRequest(mid, "PAYMENT_BUTTON", "0.1");
        createNewLinkPaymentBtn.deleteContext("body.sendSms");
        createNewLinkPaymentBtn.deleteContext("body.sendEmail");
        createNewLinkPaymentBtn.deleteContext("body.customerContact");
        JsonPath withDrawJson = createNewLinkPaymentBtn.execute().jsonPath();
        Assert.assertEquals(withDrawJson.getString("body.resultInfo.resultMessage"),MSG_AMOUNT_LESSTHAN_1);
        Assert.assertEquals(withDrawJson.getString("body.resultInfo.resultCode"),CODE_AMOUNT_LESSTHAN_1);
        Assert.assertEquals(withDrawJson.getString("body.resultInfo.resultStatus"),CREATE_LINK_FAILED_STATUS);
    }

    @Owner("Anushka")
    @Feature("PGP-40132")
    @Test(description = "verify Txn is not successful For Payment Btn LINK with amount more than 100 crore ")
    public void MaxAmountCheckPaymentBtn_03(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.MAX_AMOUNT_CHECK.getId().toString());
        CreateNewLink  createNewLinkPaymentBtn= new CreateNewLink();
        createNewLinkPaymentBtn.buildRequest(mid, "PAYMENT_BUTTON", "1000000001");
        createNewLinkPaymentBtn.deleteContext("body.sendSms");
        createNewLinkPaymentBtn.deleteContext("body.sendEmail");
        createNewLinkPaymentBtn.deleteContext("body.customerContact");
        JsonPath withDrawJson = createNewLinkPaymentBtn.execute().jsonPath();
        Assert.assertEquals(withDrawJson.getString("body.resultInfo.resultMessage"),MSG_AMOUNT_MORETHAN_100CR);
        Assert.assertEquals(withDrawJson.getString("body.resultInfo.resultCode"),CODE_AMOUNT_MORETHAN_100CR);
        Assert.assertEquals(withDrawJson.getString("body.resultInfo.resultStatus"),CREATE_LINK_FAILED_STATUS);
    }

    @Owner("Anushka_Goldi")
    @Feature("PGP-40132")
    @Parameters({"theme"})
    @Test(description = "verify Txn is not successful For Skip Login & GENERIC LINK with amount more than 10 Crore it will alter it to 8 digit number")
    public void MaxAmountCheck_Skip_Login_01(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_SKIPLOGIN.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"GENERIC","1000000000");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPWithForms(user,paymentLink,"GENERIC", "SKIPLOGIN", "1000000000");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        Assertions.assertThat(cashierPage.amountAtCashierPage().getText()).contains("Rs1,00,00,000");
    }

    @Owner("Anushka_Goldi")
    @Feature("PGP-40132")
    @Parameters({"theme"})
    @Test(description = "verify Txn is not successful For Skip Login & GENERIC LINK with amount les than 1rs it will alter it to 1rs ")
    public void MaxAmountCheck_Skip_Login_02(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_SKIPLOGIN.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"GENERIC","0.1");
        JsonPath withDrawJson = createNewLink.execute().jsonPath();
        Assert.assertEquals(withDrawJson.getString("body.resultInfo.resultMessage"),MSG_AMOUNT_LESSTHAN_1);
        Assert.assertEquals(withDrawJson.getString("body.resultInfo.resultCode"),CODE_AMOUNT_LESSTHAN_1);
        Assert.assertEquals(withDrawJson.getString("body.resultInfo.resultStatus"),CREATE_LINK_FAILED_STATUS);
    }

    @Owner("Anushka_Goldi")
    @Feature("PGP-40132")
    @Parameters({"theme"})
    @Test(description = "verify Txn is not successful For Payment Form & FIXED LINK with amount more than 100 Crore")
    public void MaxAmountCheck_Paymentform_01(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_ALLIN_ONE_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","1000000001");
        createNewLink.setContext("body.templateId","7");
        JsonPath withDrawJson = createNewLink.execute().jsonPath();
        Assert.assertEquals(withDrawJson.getString("body.resultInfo.resultMessage"),MSG_AMOUNT_MORETHAN_100CR);
        Assert.assertEquals(withDrawJson.getString("body.resultInfo.resultCode"),CODE_AMOUNT_MORETHAN_100CR);
        Assert.assertEquals(withDrawJson.getString("body.resultInfo.resultStatus"),CREATE_LINK_FAILED_STATUS);
    }

    @Owner("Anushka_Goldi")
    @Feature("PGP-40132")
    @Parameters({"theme"})
    @Test(description = "verify save update api will not allow field Amount to be more than 10 crore")
    public void MaxAmountCheck_Paymentform_02(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.MAX_AMOUNT_CHECK.getId().toString());
        SaveUpdateTemplate saveUpdateTemplate = new SaveUpdateTemplate();
        saveUpdateTemplate.deleteContext("body.fields[0].constraints.maxLength");
        saveUpdateTemplate.deleteContext("body.fields[0].constraints.minLength");
        saveUpdateTemplate.buildRequest(mid,"100crore_Template", "10000000000", "1", "Amount", "Amount");
        JsonPath withDrawJson = saveUpdateTemplate.execute().jsonPath();
        Assert.assertEquals(withDrawJson.getString("body.resultInfo.resultMessage"),SAVEUPDATE_LINK_MESSAGE);
        Assert.assertEquals(withDrawJson.getString("body.resultInfo.resultCode"),SAVEUPDATE_LINK_CODE);
        Assert.assertEquals(withDrawJson.getString("body.resultInfo.resultStatus"),CREATE_LINK_FAILED_STATUS);
        Assert.assertEquals(withDrawJson.getString("body.errorFields.Amount"),SAVEUPDATE_LINK_ERRORFIELD);
    }

}
