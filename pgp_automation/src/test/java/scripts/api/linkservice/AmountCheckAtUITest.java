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
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static com.paytm.api.linkAPI.linkConstant.PGPAPIResourcePath.*;

public class AmountCheckAtUITest extends PGPBaseTest {
    User user;
    String mid;
    void setUserAndMId(String merchant) throws Exception {
        user = userManager.getForRead(Label.LINK);
        mid = merchant;
    }
    @DataProvider(name = "Dataset")
    public Object[][] linkTypeSet()
    {
        return new Object[][]
                {
                        {"FIXED", "checkoutjs_web_revamp"},
                        {"INVOICE", "checkoutjs_web_revamp"}
                };
    }

    @Owner("Anushka Goldi")
    @Feature("PGP-40469")
    @Test(dataProvider = "Dataset", description = "verify Txn amount is right for FIXED & INVOICE LINK with amount 100 Crore")
    public void MaxAmountCheckUI_01(String Linktype, String theme) throws Exception {
        RestAssured.useRelaxedHTTPSValidation();
        setUserAndMId(Constants.MerchantType.MAX_AMOUNT_CHECK.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,Linktype,"1000000000");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,Linktype,"web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        Assert.assertTrue(cashierPage.amountAtCashierPage().isElementPresent());
    }

    @Owner("Anushka Goldi")
    @Feature("PGP-40469")
    @Test(dataProvider = "Dataset",description = "verify Merchant name is displayed in one line for FIXED & INVOICE LINK")
    public void MaxAmountCheckUI_02(String Linktype, String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.MAX_AMOUNT_CHECK.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,Linktype,"10000000");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,Linktype,"web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        Assert.assertTrue(cashierPage.merchantNameAtCashierPage().isElementPresent());
    }

    @Owner("Anushka Goldi")
    @Feature("PGP-40469")
    @Test(dataProvider = "Dataset",description = "verify link description cannot exceed 30 letters link for Fixed & Invoice Link")
    public void MaxAmountCheckUI_03(String linkType, String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, linkType, "200");
        createNewLink.setContext("body.linkDescription", "The size of link description must not exceed 30 letters");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        Assert.assertEquals(withDrawJson1.getString("body.resultInfo.resultMessage"),LINK_DESCRIPTION_BLANK);
        Assert.assertEquals(withDrawJson1.getString("body.resultInfo.resultStatus"),CREATE_LINK_FAILEDMSG);
    }

    @Owner("Anushka Goldi")
    @Feature("PGP-40469")
    @Test(dataProvider = "Dataset",description = "verify Txn is not successful For Invoice & FIXED LINK  with amount more than 100 Crore")
    public void MaxAmountCheckUI_04(String linkType, String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.MAX_AMOUNT_CHECK.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,linkType,"1000000001");
        JsonPath withDrawJson = createNewLink.execute().jsonPath();
        Assert.assertEquals(withDrawJson.getString("body.resultInfo.resultMessage"),MSG_AMOUNT_MORETHAN_100CR);
        Assert.assertEquals(withDrawJson.getString("body.resultInfo.resultCode"),CODE_AMOUNT_MORETHAN_100CR);
        Assert.assertEquals(withDrawJson.getString("body.resultInfo.resultStatus"),CREATE_LINK_FAILED_STATUS);
    }

    @Owner("Anushka Goldi")
    @Feature("PGP-40469")
    @Test(dataProvider = "Dataset",description = "verify link description with 30 letters link for Fixed & Invoice Link is displayed correctly")
    public void MaxAmountCheckUI_05(String LinkType, String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.MAX_AMOUNT_CHECK.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, LinkType, "200");
        createNewLink.setContext("body.linkDescription", "1234567890 123456789 123456789");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,LinkType,"web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        Assert.assertTrue(cashierPage.merchantDescriptionAtCashierPage().isElementPresent());
    }
}
