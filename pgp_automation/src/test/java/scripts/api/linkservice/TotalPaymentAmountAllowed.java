package scripts.api.linkservice;

import com.paytm.api.linkAPI.CreateNewLink;
import com.paytm.api.linkAPI.ExpireLink;
import com.paytm.api.linkAPI.LinkHelper;
import com.paytm.api.linkAPI.UpdateLink;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
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

import java.util.Map;

import static com.paytm.api.linkAPI.linkConstant.PGPAPIResourcePath.*;
import static com.paytm.api.linkAPI.linkConstant.PGPAPIResourcePath.EXPIRE_LINK_SUCCESS_CODE;

public class TotalPaymentAmountAllowed extends PGPBaseTest {
    User user;
    String mid;
    void setUserAndMId(String merchant) throws Exception {
        user = userManager.getForRead(Label.LINK);
        mid = merchant;
    }

    @Owner("Himanshu Arora")
    @Feature("PGP-42566")
    @Test(description = "verify the case when total amount is less than link amount.")
    public void TotalPaymentAmountAllowed_01() throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, "FIXED", "2");
        createNewLink.setContext("body.totalPaymentAmountAllowed","1");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
       Assert.assertEquals(withDrawJson1.getString("body.resultInfo.resultMessage"),TOATALAMOUNT_LESS);
        Assert.assertEquals(withDrawJson1.getString("body.resultInfo.resultCode"),TOATALAMOUNT_LESSCODE);
    }

    @Owner("Himanshu Arora")
    @Feature("PGP-42566")
    @Test(description = "verify the case when total amount is used with invoice link.")
    public void TotalPaymentAmountAllowed_02() throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, "INVOICE", "2");
        createNewLink.setContext("body.totalPaymentAmountAllowed","4");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
       Assert.assertEquals(withDrawJson1.getString("body.resultInfo.resultMessage"),TOATALAMOUNT_INVOIVE);
        Assert.assertEquals(withDrawJson1.getString("body.resultInfo.resultCode"),TOATALAMOUNT_INVOICECODE);
    }

    @Owner("Himanshu Arora")
    @Feature("PGP-42566")
    @Test(description = "verify the case when total amount is used with partial payment.")
    public void TotalPaymentAmountAllowed_03() throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, "FIXED", "2");
        createNewLink.setContext("body.partialPayment",true);
        createNewLink.setContext("body.totalPaymentAmountAllowed","4");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        Assert.assertEquals(withDrawJson1.getString("body.resultInfo.resultMessage"),TOATALAMOUNT_PARTIAL);
        Assert.assertEquals(withDrawJson1.getString("body.resultInfo.resultCode"),TOATALAMOUNT_PARTIALCODE);
    }

    @Owner("Himanshu Arora")
    @Feature("PGP-42566")
    @Test(description = "verify the case when total amount is used with more than supported value.")
    public void TotalPaymentAmountAllowed_04() throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, "FIXED", "2");
        createNewLink.setContext("body.totalPaymentAmountAllowed","1000000000000000");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        Assert.assertEquals(withDrawJson1.getString("body.resultInfo.resultMessage"),TOATALAMOUNT_VALUE);
        Assert.assertEquals(withDrawJson1.getString("body.resultInfo.resultCode"),TOATALAMOUNT_VALUECODE);
    }

    @Owner("Himanshu Arora")
    @Feature("PGP-42566")
    @Test(description = "verify the case when total amount is used with zero value.")
    public void TotalPaymentAmountAllowed_05() throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, "FIXED", "2");
        createNewLink.setContext("body.totalPaymentAmountAllowed","0");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        Assert.assertEquals(withDrawJson1.getString("body.resultInfo.resultMessage"),TOATALAMOUNT_ZERO);
        Assert.assertEquals(withDrawJson1.getString("body.resultInfo.resultCode"),TOATALAMOUNT_ZEROCODE);
    }

    @Owner("Himanshu Arora")
    @Feature("PGP-42566")
    @Parameters({"theme"})
    @Test(description = "Verify the total amount limi breached case after success txn for fixed link.")
    public void TotalPaymentAmountAllowed_06(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_TXN_PG2.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, "FIXED", "2");
        createNewLink.setContext("body.totalPaymentAmountAllowed","3");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        cashierPage.SuccessfullScreen().waitUntilVisible();
        Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Paid Successfully");
        linkPaymentLoginPage.openLink(paymentLink,"FIXED");
        Assertions.assertThat(cashierPage.TotalAmountMsg().getText()).contains("Total payment amount allowed on this link is reached. Please reach out to the merchant in case of any query.");

    }

    @Owner("Himanshu Arora")
    @Feature("PGP-42566")
    @Parameters({"theme"})
    @Test(description = "Verify the total amount limi breached case after success txn for generic link.")
    public void TotalPaymentAmountAllowed_07(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_TXN_PG2.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, "GENERIC", "1");
        createNewLink.setContext("body.totalPaymentAmountAllowed","1");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLinkForTotalAmount(user,paymentLink,"GENERIC","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        cashierPage.SuccessfullScreen().waitUntilVisible();
        Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Paid Successfully");
        linkPaymentLoginPage.openLink(paymentLink,"GENERIC");
        Assertions.assertThat(cashierPage.TotalAmountMsg().getText()).contains("Total payment amount allowed on this link is reached. Please reach out to the merchant in case of any query.");

    }

    @Owner("Himanshu Arora")
    @Feature("PGP-42566")
    @Test(description = "verify the case when total amount is less than link amount.")
    public void TotalPaymentAmountAllowed_08() throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, "FIXED", "2");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        UpdateLink updateLink =new UpdateLink(mid,linkId);
        updateLink.setContext("body.totalPaymentAmountAllowed","1");
        JsonPath withDrawJson2 = updateLink.execute().jsonPath();
       Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultMessage"),TOATALAMOUNT_LESS);
       Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultCode"),TOATALAMOUNT_LESSCODE);
    }

    @Owner("Himanshu Arora")
    @Feature("PGP-42566")
    @Test(description = "verify the case when total amount is less than link amount in update link api.")
    public void TotalPaymentAmountAllowed_09() throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, "FIXED", "2");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        UpdateLink updateLink =new UpdateLink(mid,linkId);
        updateLink.setContext("body.totalPaymentAmountAllowed","1");
        JsonPath withDrawJson2 = updateLink.execute().jsonPath();
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultMessage"),TOATALAMOUNT_LESS);
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultCode"),TOATALAMOUNT_LESSCODE);
    }

    @Owner("Himanshu Arora")
    @Feature("PGP-42566")
    @Test(description = "verify the case when total amount is used with more than supported value in update link api.")
    public void TotalPaymentAmountAllowed_10() throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, "FIXED", "2");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        UpdateLink updateLink =new UpdateLink(mid,linkId);
        updateLink.setContext("body.totalPaymentAmountAllowed","120000000000000");
        JsonPath withDrawJson2 = updateLink.execute().jsonPath();
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultMessage"),TOATALAMOUNT_VALUE);
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultCode"),TOATALAMOUNT_VALUECODE);
    }

    @Owner("Himanshu Arora")
    @Feature("PGP-42566")
    @Test(description = "verify the case when total amount is used with more than supported value in update link api.")
    public void TotalPaymentAmountAllowed_11() throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, "INVOICE", "2");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        UpdateLink updateLink =new UpdateLink(mid,linkId);
        updateLink.setContext("body.totalPaymentAmountAllowed","10");
        JsonPath withDrawJson2 = updateLink.execute().jsonPath();
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultMessage"),TOATALAMOUNT_UPDATEINVOICE);
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultCode"),TOATALAMOUNT_UPDATEINVOICECODE);
    }

    @Owner("Himanshu Arora")
    @Feature("PGP-42566")
    @Test(description = "verify the case when total amount is used with more than supported value in update link api.")
    public void TotalPaymentAmountAllowed_12() throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, "FIXED", "2");
        createNewLink.setContext("body.partialPayment",true);
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        UpdateLink updateLink =new UpdateLink(mid,linkId);
        updateLink.setContext("body.totalPaymentAmountAllowed","4");
        JsonPath withDrawJson2 = updateLink.execute().jsonPath();
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultMessage"),TOATALAMOUNT_UPDATEPARTIAL);
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultCode"),TOATALAMOUNT_UPDATEPARTIALCODE);
    }

    @Owner("Himanshu Arora")
    @Feature("PGP-42566")
    @Test(description = "Verify error in update link api when amount=11100000000000000.")
    public void TotalPaymentAmountAllowed_13() throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, "FIXED", "2");
        createNewLink.setContext("body.partialPayment",true);
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        UpdateLink updateLink =new UpdateLink(mid,linkId);
        updateLink.setContext("body.amount","11100000000000000");
        JsonPath withDrawJson2 = updateLink.execute().jsonPath();
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultMessage"),MSG_AMOUNT_MORETHAN_100CR);
    }
}
