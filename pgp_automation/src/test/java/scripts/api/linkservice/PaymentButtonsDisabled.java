package scripts.api.linkservice;

import com.paytm.api.MappingService.MerchantAddPreferenceInfo;
import com.paytm.api.linkAPI.CreateNewLink;
import com.paytm.api.linkAPI.UpdateLink;
import com.paytm.appconstants.Constants;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.mappingService.addMerchantPreferenceReq.MerchantAddPreferenceInfoReq;
import com.paytm.framework.reportportal.annotation.Owner;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.LinkPaymentLoginPage;
import io.qameta.allure.Feature;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import static com.paytm.api.linkAPI.linkConstant.PGPAPIResourcePath.*;

public class PaymentButtonsDisabled extends PGPBaseTest {
    User user;
    String mid;
    void setUserAndMId(String merchant) throws Exception {
        user = userManager.getForWrite(Label.LINK);
        mid = merchant;
    }

    @Owner("Anushka")
    @Feature("PGP-40075")
    @Test(description = "Payment Button Deactivated PAYMENT_BUTTONS is not set for merchant or preference PAYMENT_BUTTONS is False")
    public void PaymentButton_01() throws Exception{
        setUserAndMId(Constants.MerchantType.PAYMENT_BUTTON_OFF.getId().toString());
        CreateNewLink  createNewLinkPaymentBtn= new CreateNewLink();
        createNewLinkPaymentBtn.buildRequest(mid, "PAYMENT_BUTTON", "200");
        createNewLinkPaymentBtn.deleteContext("body.sendSms");
        createNewLinkPaymentBtn.deleteContext("body.sendEmail");
        createNewLinkPaymentBtn.deleteContext("body.customerContact");
        JsonPath withDrawJson1 = createNewLinkPaymentBtn.execute().jsonPath();
        Assert.assertEquals(withDrawJson1.getString("body.resultInfo.resultMessage"),PAYMENT_BTN_NOT_SUPORTED);
        Assert.assertEquals(withDrawJson1.getString("body.resultInfo.resultCode"),PAYMENT_BTN_NOT_SUPPORTED_CODE);
        Assert.assertEquals(withDrawJson1.getString("body.resultInfo.resultStatus"),CREATE_LINK_FAILED_STATUS);
    }

    @Owner("Anushka")
    @Feature("PGP-40075")
    @Test(description = "preference PAYMENT_BUTTONS is True")
    public void PaymentButton_02() throws Exception{
        setUserAndMId(Constants.MerchantType.PAYMENT_BUTTON_DISABLE_OFF.getId().toString());
        MerchantAddPreferenceInfoReq merchantAddPreferenceInfoReq =
                new MerchantAddPreferenceInfoReq.Builder(mid, "PAYMENT_BUTTON","ACTIVE","Y")
                        .build();
        MerchantAddPreferenceInfo merchantAddPreferenceInfo = new MerchantAddPreferenceInfo(merchantAddPreferenceInfoReq);
        merchantAddPreferenceInfo.execute();
        CreateNewLink  createNewLinkPaymentBtn= new CreateNewLink();
        createNewLinkPaymentBtn.buildRequest(mid, "PAYMENT_BUTTON", "200");
        createNewLinkPaymentBtn.deleteContext("body.sendSms");
        createNewLinkPaymentBtn.deleteContext("body.sendEmail");
        createNewLinkPaymentBtn.deleteContext("body.customerContact");
        JsonPath withDrawJson1 = createNewLinkPaymentBtn.execute().jsonPath();
        Assert.assertEquals(withDrawJson1.getString("body.resultInfo.resultMessage"),CREATE_LINK_SUCCESS);
        Assert.assertEquals(withDrawJson1.getString("body.resultInfo.resultCode"),CREATE_LINK_SUCCESS_CODE);
        Assert.assertEquals(withDrawJson1.getString("body.resultInfo.resultStatus"),CREATE_LINK_SUCCESS_STATUS);
    }

    @Owner("Anushka")
    @Feature("PGP-40075")
    @Test(description = "preference PAYMENT_BUTTONS is True & Payment_Buttons_Disabled is also True in New Link Creation")
    public void PaymentButtonDisable_03() throws Exception{
        setUserAndMId(Constants.MerchantType.PAYMENT_BUTTON_ON.getId().toString());
        MerchantAddPreferenceInfoReq merchantAddPreferenceInfoReq =
                new MerchantAddPreferenceInfoReq.Builder(mid, "Payment_Buttons_Disabled","ACTIVE","Y")
                        .build();
        MerchantAddPreferenceInfo merchantAddPreferenceInfo = new MerchantAddPreferenceInfo(merchantAddPreferenceInfoReq);
        merchantAddPreferenceInfo.execute();
        CreateNewLink  createNewLinkPaymentBtn= new CreateNewLink();
        createNewLinkPaymentBtn.buildRequest(mid, "PAYMENT_BUTTON", "200");
        createNewLinkPaymentBtn.deleteContext("body.sendSms");
        createNewLinkPaymentBtn.deleteContext("body.sendEmail");
        createNewLinkPaymentBtn.deleteContext("body.customerContact");
        JsonPath withDrawJson1 = createNewLinkPaymentBtn.execute().jsonPath();
        Assert.assertEquals(withDrawJson1.getString("body.resultInfo.resultMessage"),PAYMENT_BTN_INACTIVE);
        Assert.assertEquals(withDrawJson1.getString("body.resultInfo.resultCode"),PAYMENT_BTN_INACTIVE_CODE);
        Assert.assertEquals(withDrawJson1.getString("body.resultInfo.resultStatus"),CREATE_LINK_FAILED_STATUS);
    }


    @Owner("Anushka")
    @Feature("PGP-40075")
    @Test(description = "preference Payment_Buttons_Disabled is False & preference PAYMENT_BUTTONS is True in New Link Creation")
    public void PaymentButton_04() throws Exception{
        setUserAndMId(Constants.MerchantType.PAYMENT_BUTTON_ON04.getId().toString());
        MerchantAddPreferenceInfoReq merchantAddPreferenceInfoReq =
                new MerchantAddPreferenceInfoReq.Builder(mid, "Payment_Buttons_Disabled","ACTIVE","N")
                        .build();
        MerchantAddPreferenceInfo merchantAddPreferenceInfo = new MerchantAddPreferenceInfo(merchantAddPreferenceInfoReq);
        merchantAddPreferenceInfo.execute();
        CreateNewLink  createNewLinkPaymentBtn= new CreateNewLink();
        createNewLinkPaymentBtn.buildRequest(mid, "PAYMENT_BUTTON", "200");
        createNewLinkPaymentBtn.deleteContext("body.sendSms");
        createNewLinkPaymentBtn.deleteContext("body.sendEmail");
        createNewLinkPaymentBtn.deleteContext("body.customerContact");
        JsonPath withDrawJson1 = createNewLinkPaymentBtn.execute().jsonPath();
        Assert.assertEquals(withDrawJson1.getString("body.resultInfo.resultMessage"),CREATE_LINK_SUCCESS);
        Assert.assertEquals(withDrawJson1.getString("body.resultInfo.resultCode"),CREATE_LINK_SUCCESS_CODE);
        Assert.assertEquals(withDrawJson1.getString("body.resultInfo.resultStatus"),CREATE_LINK_SUCCESS_STATUS);
    }

    @Owner("Anushka")
    @Feature("PGP-40075")
    @Test(description = "preference PAYMENT_BUTTONS is True & Payment_Buttons_Disabled is also True in Updating the existing Link")
    public void PaymentButton_05() throws Exception{
        setUserAndMId(Constants.MerchantType.PAYMENT_BUTTON_ON.getId().toString());
        MerchantAddPreferenceInfoReq merchantAddPreferenceInfoReq1 =
                new MerchantAddPreferenceInfoReq.Builder(mid, "Payment_Buttons_Disabled","ACTIVE","N")
                        .build();
        MerchantAddPreferenceInfo merchantAddPreferenceInfo1 = new MerchantAddPreferenceInfo(merchantAddPreferenceInfoReq1);
        merchantAddPreferenceInfo1.execute();
        CreateNewLink  createNewLinkPaymentBtn= new CreateNewLink();
        createNewLinkPaymentBtn.buildRequest(mid, "PAYMENT_BUTTON", "200");
        createNewLinkPaymentBtn.deleteContext("body.sendSms");
        createNewLinkPaymentBtn.deleteContext("body.sendEmail");
        createNewLinkPaymentBtn.deleteContext("body.customerContact");
        JsonPath withDrawJson1 = createNewLinkPaymentBtn.execute().jsonPath();
        String LinkId = withDrawJson1.getString("body.linkId");
        MerchantAddPreferenceInfoReq merchantAddPreferenceInfoReq =
                new MerchantAddPreferenceInfoReq.Builder(mid, "Payment_Buttons_Disabled","ACTIVE","Y")
                        .build();
        MerchantAddPreferenceInfo merchantAddPreferenceInfo = new MerchantAddPreferenceInfo(merchantAddPreferenceInfoReq);
        merchantAddPreferenceInfo.execute();
        UpdateLink updateLinkPaymentBtn= new UpdateLink(mid, LinkId);
        JsonPath withDrawJson = updateLinkPaymentBtn.execute().jsonPath();
        Assert.assertEquals(withDrawJson.getString("body.resultInfo.resultMessage"),PAYMENT_BTN_INACTIVE);
        Assert.assertEquals(withDrawJson.getString("body.resultInfo.resultCode"),PAYMENT_BTN_INACTIVE_CODE);
        Assert.assertEquals(withDrawJson.getString("body.resultInfo.resultStatus"),CREATE_LINK_FAILED_STATUS);

    }
    @Owner("Anushka")
    @Feature("PGP-40075")
    @Test(description = "preference PAYMENT_BUTTONS is True & Payment_Buttons_Disabled is False in Updating the existing Link")
    public void PaymentButton_06() throws Exception{
        setUserAndMId(Constants.MerchantType.PAYMENT_BUTTON_ON04.getId().toString());
        MerchantAddPreferenceInfoReq merchantAddPreferenceInfoReq1 =
                new MerchantAddPreferenceInfoReq.Builder(mid, "Payment_Buttons_Disabled","ACTIVE","N")
                        .build();
        MerchantAddPreferenceInfo merchantAddPreferenceInfo1 = new MerchantAddPreferenceInfo(merchantAddPreferenceInfoReq1);
        merchantAddPreferenceInfo1.execute();
        CreateNewLink  createNewLinkPaymentBtn= new CreateNewLink();
        createNewLinkPaymentBtn.buildRequest(mid, "PAYMENT_BUTTON", "200");
        createNewLinkPaymentBtn.deleteContext("body.sendSms");
        createNewLinkPaymentBtn.deleteContext("body.sendEmail");
        createNewLinkPaymentBtn.deleteContext("body.customerContact");
        JsonPath withDrawJson1 = createNewLinkPaymentBtn.execute().jsonPath();
        String LinkId = withDrawJson1.getString("body.linkId");
        MerchantAddPreferenceInfoReq merchantAddPreferenceInfoReq =
                new MerchantAddPreferenceInfoReq.Builder(mid, "Payment_Buttons_Disabled","ACTIVE","N")
                        .build();
        MerchantAddPreferenceInfo merchantAddPreferenceInfo = new MerchantAddPreferenceInfo(merchantAddPreferenceInfoReq);
        merchantAddPreferenceInfo.execute();
        UpdateLink updateLinkPaymentBtn= new UpdateLink(mid, LinkId);
        JsonPath withDrawJson = updateLinkPaymentBtn.execute().jsonPath();
        Assert.assertEquals(withDrawJson.getString("body.resultInfo.resultMessage"),UPDATE_LINK_MSG);
        Assert.assertEquals(withDrawJson.getString("body.resultInfo.resultCode"),CREATE_LINK_SUCCESS_CODE);
        Assert.assertEquals(withDrawJson.getString("body.resultInfo.resultStatus"),CREATE_LINK_SUCCESS_STATUS);
    }

    @Owner("Anushka")
    @Parameters({"theme"})
    @Feature("PGP-40075")
    @Test(description = "preference PAYMENT_BUTTONS is True & Payment_Buttons_Disabled is False in Opening the existing Link")
    public void PaymentButton_07(@Optional("checkoutjs_web_revamp") String theme) throws Exception{
        setUserAndMId(Constants.MerchantType.PAYMENT_BUTTON_ON04.getId().toString());
        MerchantAddPreferenceInfoReq merchantAddPreferenceInfoReq =
                new MerchantAddPreferenceInfoReq.Builder(mid, "Payment_Buttons_Disabled","ACTIVE","N")
                        .build();
        MerchantAddPreferenceInfo merchantAddPreferenceInfo = new MerchantAddPreferenceInfo(merchantAddPreferenceInfoReq);
        merchantAddPreferenceInfo.execute();
        CreateNewLink  createNewLinkPaymentBtn= new CreateNewLink();
        createNewLinkPaymentBtn.buildRequest(mid, "PAYMENT_BUTTON", "200");
        createNewLinkPaymentBtn.deleteContext("body.sendSms");
        createNewLinkPaymentBtn.deleteContext("body.sendEmail");
        createNewLinkPaymentBtn.deleteContext("body.customerContact");
        JsonPath withDrawJson1 = createNewLinkPaymentBtn.execute().jsonPath();
        String Linkurl = withDrawJson1.getString("body.longUrl");
        MerchantAddPreferenceInfoReq merchantAddPreferenceInfoReq1 =
                new MerchantAddPreferenceInfoReq.Builder(mid, "Payment_Buttons_Disabled","ACTIVE","N")
                        .build();
        MerchantAddPreferenceInfo merchantAddPreferenceInfo1 = new MerchantAddPreferenceInfo(merchantAddPreferenceInfoReq1);
        merchantAddPreferenceInfo1.execute();
        Assert.assertEquals(withDrawJson1.getString("body.resultInfo.resultMessage"),CREATE_LINK_SUCCESS);
        Assert.assertEquals(withDrawJson1.getString("body.resultInfo.resultCode"),CREATE_LINK_SUCCESS_CODE);
        Assert.assertEquals(withDrawJson1.getString("body.resultInfo.resultStatus"),CREATE_LINK_SUCCESS_STATUS);
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLink(Linkurl, "PAYMENT_BUTTON");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        Assertions.assertThat(cashierPage.VerifyMobileNumber().getText()).contains("Verify Mobile Number\n" +
                "We will send an OTP to this number");
    }

    @Owner("Anushka")
    @Parameters({"theme"})
    @Feature("PGP-40075")
    @Test(description = "preference PAYMENT_BUTTONS is True & Payment_Buttons_Disabled is True in Opening the existing Link")
    public void PaymentButton_08(@Optional("checkoutjs_web_revamp") String theme) throws Exception{
        setUserAndMId(Constants.MerchantType.PAYMENT_BUTTON_ON.getId().toString());
        MerchantAddPreferenceInfoReq merchantAddPreferenceInfoReq =
                new MerchantAddPreferenceInfoReq.Builder(mid, "Payment_Buttons_Disabled","ACTIVE","N")
                        .build();
        MerchantAddPreferenceInfo merchantAddPreferenceInfo = new MerchantAddPreferenceInfo(merchantAddPreferenceInfoReq);
        merchantAddPreferenceInfo.execute();
        CreateNewLink  createNewLinkPaymentBtn= new CreateNewLink();
        createNewLinkPaymentBtn.buildRequest(mid, "PAYMENT_BUTTON", "200");
        createNewLinkPaymentBtn.deleteContext("body.sendSms");
        createNewLinkPaymentBtn.deleteContext("body.sendEmail");
        createNewLinkPaymentBtn.deleteContext("body.customerContact");
        JsonPath withDrawJson1 = createNewLinkPaymentBtn.execute().jsonPath();
        String Linkurl = withDrawJson1.getString("body.longUrl");
        MerchantAddPreferenceInfoReq merchantAddPreferenceInfoReq1 =
                new MerchantAddPreferenceInfoReq.Builder(mid, "Payment_Buttons_Disabled","ACTIVE","Y")
                        .build();
        MerchantAddPreferenceInfo merchantAddPreferenceInfo1 = new MerchantAddPreferenceInfo(merchantAddPreferenceInfoReq1);
        merchantAddPreferenceInfo1.execute();
        Assert.assertEquals(withDrawJson1.getString("body.resultInfo.resultMessage"),CREATE_LINK_SUCCESS);
        Assert.assertEquals(withDrawJson1.getString("body.resultInfo.resultCode"),CREATE_LINK_SUCCESS_CODE);
        Assert.assertEquals(withDrawJson1.getString("body.resultInfo.resultStatus"),CREATE_LINK_SUCCESS_STATUS);
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLink(Linkurl, "PAYMENT_BUTTON");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        Assertions.assertThat(cashierPage.invalidLink().getText()).contains("Inactive Link\n" +
                "This link is inactive. Please reach out to the merchant to issue new link");
    }
}
