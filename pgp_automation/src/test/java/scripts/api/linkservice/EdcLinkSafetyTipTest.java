package scripts.api.linkservice;

import com.paytm.api.MappingService.MerchantAddPreferenceInfo;
import com.paytm.api.linkAPI.CreateNewLink;
import com.paytm.appconstants.Constants;
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
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

public class EdcLinkSafetyTipTest extends PGPBaseTest {
    User user;
    String mid;

    void setUserAndMId(String merchant) throws Exception {
        user = userManager.getForRead(Label.LINK);
        mid = merchant;
    }

    @Owner("Anushka Goldi")
    @Feature("PGP-40319")
    @Parameters({"theme"})
    @Test(description = "Verify safety tip status when OPERATION_ORIGIN is passed other then EDC_LINK in Online link Flow")
    public void EDC_Link_SafetyTip_01(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.EDC_LINK_ONLINE.getId());
        MerchantAddPreferenceInfoReq merchantAddPreferenceInfoReq1 =
                new MerchantAddPreferenceInfoReq.Builder(mid, "ONLINE_FLOW_ENABLED","ACTIVE","Y")
                        .build();
        MerchantAddPreferenceInfo merchantAddPreferenceInfo1 = new MerchantAddPreferenceInfo(merchantAddPreferenceInfoReq1);
        merchantAddPreferenceInfo1.execute();
        MerchantAddPreferenceInfoReq merchantAddPreferenceInfoReq2 =
                new MerchantAddPreferenceInfoReq.Builder(mid, "JS_CHECKOUT_ONLINE_FLOW_ENABLED","ACTIVE","Y")
                        .build();
        MerchantAddPreferenceInfo merchantAddPreferenceInfo2 = new MerchantAddPreferenceInfo(merchantAddPreferenceInfoReq2);
        merchantAddPreferenceInfo2.execute();
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","200", "UNKNOWN");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        Assertions.assertThat(cashierPage.safetyMessage().getText()).contains("Please ensure you are paying only to a trusted merchant");
    }

    @Owner("Anushka Goldi")
    @Feature("PGP-40319")
    @Parameters({"theme"})
    @Test(description = "Verify safety tip status when OPERATION_ORIGIN is passed other then EDC_LINK in Offline link Flow")
    public void EDC_Link_SafetyTip_02(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.EDC_LINK_ONLINE.getId());
        MerchantAddPreferenceInfoReq merchantAddPreferenceInfoReq1 =
                new MerchantAddPreferenceInfoReq.Builder(mid, "ONLINE_FLOW_ENABLED","ACTIVE","N")
                        .build();
        MerchantAddPreferenceInfo merchantAddPreferenceInfo1 = new MerchantAddPreferenceInfo(merchantAddPreferenceInfoReq1);
        merchantAddPreferenceInfo1.execute();
        MerchantAddPreferenceInfoReq merchantAddPreferenceInfoReq2 =
                new MerchantAddPreferenceInfoReq.Builder(mid, "JS_CHECKOUT_ONLINE_FLOW_ENABLED","ACTIVE","N")
                        .build();
        MerchantAddPreferenceInfo merchantAddPreferenceInfo2 = new MerchantAddPreferenceInfo(merchantAddPreferenceInfoReq2);
        merchantAddPreferenceInfo2.execute();
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","200", "P4B");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        Assertions.assertThat(cashierPage.safetyMessage().getText()).contains("Please ensure you are paying only to a trusted merchant");
    }

    @Owner("Anushka Goldi")
    @Feature("PGP-40319")
    @Parameters({"theme"})
    @Test(description = "Verify that safety tip is Not present when OPERATION_ORIGIN is EDC_LINK in Online link Flow")
    public void EDC_Link_SafetyTip_03(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.EDC_LINK_ONLINE.getId());
        MerchantAddPreferenceInfoReq merchantAddPreferenceInfoReq1 =
                new MerchantAddPreferenceInfoReq.Builder(mid, "ONLINE_FLOW_ENABLED","ACTIVE","Y")
                        .build();
        MerchantAddPreferenceInfo merchantAddPreferenceInfo1 = new MerchantAddPreferenceInfo(merchantAddPreferenceInfoReq1);
        merchantAddPreferenceInfo1.execute();
        MerchantAddPreferenceInfoReq merchantAddPreferenceInfoReq2 =
                new MerchantAddPreferenceInfoReq.Builder(mid, "JS_CHECKOUT_ONLINE_FLOW_ENABLED","ACTIVE","Y")
                        .build();
        MerchantAddPreferenceInfo merchantAddPreferenceInfo2 = new MerchantAddPreferenceInfo(merchantAddPreferenceInfoReq2);
        merchantAddPreferenceInfo2.execute();
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","200", "EDC_LINK");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        Assert.assertFalse(cashierPage.safetyMessage().isElementPresent());
    }

    @Owner("Anushka Goldi")
    @Feature("PGP-40319")
    @Parameters({"theme"})
    @Test(description = "Verify that safety tip is Not present when OPERATION_ORIGIN is EDC_LINK in Offline link Flow")
    public void EDC_Link_SafetyTip_04(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.EDC_LINK_ONLINE.getId());
        MerchantAddPreferenceInfoReq merchantAddPreferenceInfoReq1 =
                new MerchantAddPreferenceInfoReq.Builder(mid, "ONLINE_FLOW_ENABLED","ACTIVE","N")
                        .build();
        MerchantAddPreferenceInfo merchantAddPreferenceInfo1 = new MerchantAddPreferenceInfo(merchantAddPreferenceInfoReq1);
        merchantAddPreferenceInfo1.execute();
        MerchantAddPreferenceInfoReq merchantAddPreferenceInfoReq2 =
                new MerchantAddPreferenceInfoReq.Builder(mid, "JS_CHECKOUT_ONLINE_FLOW_ENABLED","ACTIVE","N")
                        .build();
        MerchantAddPreferenceInfo merchantAddPreferenceInfo2 = new MerchantAddPreferenceInfo(merchantAddPreferenceInfoReq2);
        merchantAddPreferenceInfo2.execute();
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","200", "EDC_LINK");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        Assert.assertFalse(cashierPage.safetyMessage().isElementPresent());
    }
}
