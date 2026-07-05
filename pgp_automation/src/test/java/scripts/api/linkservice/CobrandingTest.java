package scripts.api.linkservice;

import com.paytm.api.linkAPI.CreateNewLink;
import com.paytm.api.nativeAPI.MerchantPGPUITheme;
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
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import static com.paytm.appconstants.Constants.Owner.SRINIVAS;

public class CobrandingTest extends PGPBaseTest {
    User user;
    String mid;
    void setUserAndMId(String merchant) throws Exception {
        user = userManager.getForWrite(Label.LINK);
        mid = merchant;
    }

    @Owner(SRINIVAS)
    @Feature("PGP-41281")
    @Parameters({"theme"})
    @Test(description = "Verify the icici cobranding logo and header color is displayed on the payment links on parent mid without making any customisation on child mid ")
    public void Verify_icici_cobranding_logo_and_headercolor_is_displayed_on_paymentlinks_without_applying_customisation_on_childmid(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.CHILD_COBRANDING_MID.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","200");
        createNewLink.setContext("body.resellerId",Constants.MerchantType.RESELLER_COBRANDING_MID.getId().toString());
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLink(paymentLink,"web");


        MerchantPGPUITheme merchantPGPUIThemeParentMID=new MerchantPGPUITheme(Constants.MerchantType.CHILD_COBRANDING_MID,true);
        JsonPath getMerchantPGPUIThemeParentMIDJson = merchantPGPUIThemeParentMID.execute().jsonPath();
        String isParentThemeOverwriteEnabledFromAPI = getMerchantPGPUIThemeParentMIDJson.getString("body.merchantPreferenceInfos.parentConfig.theme.isParentThemeOverwriteEnabled");
        String isParentThemeOverwriteEnabledExpected = "true";
        String parentmid=getMerchantPGPUIThemeParentMIDJson.getString("body.merchantPreferenceInfos.parentConfig.mid");
        Assertions.assertThat(parentmid).isEqualTo("HRSHCE44598388088972");
        Assertions.assertThat(isParentThemeOverwriteEnabledFromAPI).as("isParentThemeOverwriteEnabled flag in merchantpgpui/theme API").isEqualTo(isParentThemeOverwriteEnabledExpected);


        MerchantPGPUITheme merchantPGPUIThemeChildMID=new MerchantPGPUITheme(Constants.MerchantType.CHILD_COBRANDING_MID,true);
        JsonPath getMerchantPGPUIThemeChildMIDJson = merchantPGPUIThemeChildMID.execute().jsonPath();
        String isParentThemeOverwriteEnabledFromChildAPI = getMerchantPGPUIThemeChildMIDJson.getString("body.merchantPreferenceInfos.childConfig.theme.isParentThemeOverwriteEnabled");
        String isParentThemeOverwriteEnabledExpectedChildAPI = "false";
        String childmid=getMerchantPGPUIThemeChildMIDJson.getString("body.merchantPreferenceInfos.childConfig.mid");
        Assertions.assertThat(childmid).isEqualTo("HRSHCF06247372764665");
        Assertions.assertThat(isParentThemeOverwriteEnabledFromChildAPI).as("isParentThemeOverwriteEnabled flag in merchantpgpui/theme API").isEqualTo(isParentThemeOverwriteEnabledExpectedChildAPI);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.easypaylogo().assertVisible();
    }
}
