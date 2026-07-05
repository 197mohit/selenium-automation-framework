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
import org.testng.annotations.Test;

public class SupportForSenderContext extends PGPBaseTest {

    User user;
    String mid;
    void setUserAndMId(String merchant) throws Exception {
        user = userManager.getForRead(Label.LINK);
        mid = merchant;
    }


    @Owner("Himanshu Arora")
    @Feature("PGP-51511")
    @Test(description = "verify name is present for paymentRequesterDetails")
    public void SupportForSenderContext_01(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.EDC_LINK.getId().toString());
        CreateNewLink EdcLink=new CreateNewLink(mid,"EDCLINK");
        EdcLink.setContext("body.edcEmiFields.paymentRequesterDetails.name","Rahul Sharma");
        EdcLink.deleteContext("body.edcEmiFields.paymentRequesterDetails.mobileNumber");
        JsonPath withDrawJson1 = EdcLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");

        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLink(paymentLink,"FIXED");

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        Assert.assertTrue(cashierPage.PaymentRequesterName().isElementPresent());

    }

    @Owner("Himanshu Arora")
    @Feature("PGP-51511")
    @Test(description = "verify mobileNumber is present for paymentRequesterDetails")
    public void SupportForSenderContext_02(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.EDC_LINK.getId().toString());
        CreateNewLink EdcLink=new CreateNewLink(mid,"EDCLINK");
        EdcLink.setContext("body.edcEmiFields.paymentRequesterDetails.mobileNumber","9988776655");
        EdcLink.deleteContext("body.edcEmiFields.paymentRequesterDetails.name");
        JsonPath withDrawJson1 = EdcLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");

        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLink(paymentLink,"FIXED");

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        Assert.assertTrue(cashierPage.PaymentRequesterMobileNumber().isElementPresent());

    }

    @Owner("Himanshu Arora")
    @Feature("PGP-51511")
    @Test(description = "verify mobileNumber & name is present for paymentRequesterDetails")
    public void SupportForSenderContext_03(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.EDC_LINK.getId().toString());
        CreateNewLink EdcLink=new CreateNewLink(mid,"EDCLINK");
        EdcLink.setContext("body.edcEmiFields.paymentRequesterDetails.name","Rahul Sharma");
        EdcLink.setContext("body.edcEmiFields.paymentRequesterDetails.mobileNumber","9988776655");
        JsonPath withDrawJson1 = EdcLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");

        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLink(paymentLink,"FIXED");

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        Assert.assertTrue(cashierPage.PaymentRequesterName().isElementPresent());
        Assert.assertTrue(cashierPage.PaymentRequesterMobileNumber().isElementPresent());

    }

    @Owner("Himanshu Arora")
    @Feature("PGP-51511")
    @Test(description = "verify mobileNumber & name is not present for paymentRequesterDetails")
    public void SupportForSenderContext_04(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.EDC_LINK.getId().toString());
        CreateNewLink EdcLink=new CreateNewLink(mid,"EDCLINK");
        EdcLink.deleteContext("body.edcEmiFields.paymentRequesterDetails.name");
        EdcLink.deleteContext("body.edcEmiFields.paymentRequesterDetails.mobileNumber");
        JsonPath withDrawJson1 = EdcLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");

        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLink(paymentLink,"FIXED");

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        Assert.assertFalse(cashierPage.PaymentRequesterName().isElementPresent());
        Assert.assertFalse(cashierPage.PaymentRequesterMobileNumber().isElementPresent());

    }
}
