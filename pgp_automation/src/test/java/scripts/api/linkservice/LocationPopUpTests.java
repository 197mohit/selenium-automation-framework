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
import org.testng.Assert;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

public class LocationPopUpTests extends PGPBaseTest {

    User user;
    String mid;
    void setUserAndMId(String merchant) throws Exception {
        user = userManager.getForWrite(Label.LINK);
        mid = merchant;
    }

    @Owner("Anushka Goldi")
    @Feature("PGP-40240")
    @Parameters({"theme"})
    @Test(description = "Verify that location Pop is coming on cashier page when locationPermission: False, locationPopupDisabled: False")
    public void locationPopUp_001(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LOCATIONPOPUPFALSE.getId().toString());
        MerchantAddPreferenceInfoReq merchantAddPreferenceInfoReq1 =
                new MerchantAddPreferenceInfoReq.Builder(mid, "LOCATION_PERMISSION","ACTIVE","N")
                        .build();
        MerchantAddPreferenceInfo merchantAddPreferenceInfo1 = new MerchantAddPreferenceInfo(merchantAddPreferenceInfoReq1);
        merchantAddPreferenceInfo1.execute();
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.getNBTabForNewFlow().click();
        cashierPage.getPayButtonNew().click();
        Assert.assertTrue(cashierPage.LocationPopUpMsg().isElementPresent());
    }

    //Working on local but not working on Jenkins, currently disabled
//    @Owner("Anushka Goldi")
//    @Feature("PGP-40240")
//    @Parameters({"theme"})
//    @Test(description = "Verify that location Pop is coming on cashier page when locationPermission: True, locationPopupDisabled: False", enabled = false)
    public void locationPopUp_002(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LOCATIONPOPUPFALSE.getId().toString());
        MerchantAddPreferenceInfoReq merchantAddPreferenceInfoReq1 =
                new MerchantAddPreferenceInfoReq.Builder(mid, "LOCATION_PERMISSION","ACTIVE","Y")
                        .build();
        MerchantAddPreferenceInfo merchantAddPreferenceInfo1 = new MerchantAddPreferenceInfo(merchantAddPreferenceInfoReq1);
        merchantAddPreferenceInfo1.execute();
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.getNBTabForNewFlow().click();
        cashierPage.getPayButtonNew().click();
        Assert.assertTrue(cashierPage.LocationPopUpMsg().isElementPresent());
    }

    @Owner("Anushka Goldi")
    @Feature("PGP-40240")
    @Parameters({"theme"})
    @Test(description = "Verify that location Pop is coming on cashier page when locationPermission: TRUE, locationPopupDisabled: TRUE")
    public void locationPopUp_003(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LOCATIONPOPUPTRUE.getId().toString());
        MerchantAddPreferenceInfoReq merchantAddPreferenceInfoReq1 =
                new MerchantAddPreferenceInfoReq.Builder(mid, "LOCATION_PERMISSION","ACTIVE","Y")
                        .build();
        MerchantAddPreferenceInfo merchantAddPreferenceInfo1 = new MerchantAddPreferenceInfo(merchantAddPreferenceInfoReq1);
        merchantAddPreferenceInfo1.execute();
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.getNBTabForNewFlow().click();
        cashierPage.getPayButtonNew().click();
        Assert.assertFalse(cashierPage.LocationPopUpMsg().isElementPresent());
    }

    @Owner("Anushka Goldi")
    @Feature("PGP-40240")
    @Parameters({"theme"})
    @Test(description = "Verify that location Pop is coming on cashier page when locationPermission: TRUE, locationPopupDisabled: TRUE")
    public void locationPopUp_004(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LOCATIONPOPUPTRUE.getId().toString());
        MerchantAddPreferenceInfoReq merchantAddPreferenceInfoReq1 =
                new MerchantAddPreferenceInfoReq.Builder(mid, "LOCATION_PERMISSION","ACTIVE","N")
                        .build();
        MerchantAddPreferenceInfo merchantAddPreferenceInfo1 = new MerchantAddPreferenceInfo(merchantAddPreferenceInfoReq1);
        merchantAddPreferenceInfo1.execute();
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.getNBTabForNewFlow().click();
        cashierPage.getPayButtonNew().click();
        Assert.assertFalse(cashierPage.LocationPopUpMsg().isElementPresent());
    }


}
