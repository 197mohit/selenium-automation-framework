package scripts.api.linkservice;

import com.paytm.api.linkAPI.CreateNewLink;
import com.paytm.api.linkAPI.ExpireLink;
import com.paytm.api.linkAPI.FetchTransactionApi;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.paytm.api.linkAPI.linkConstant.PGPAPIResourcePath.FETCH_TRANSACTION_NULLMID;
import static com.paytm.api.linkAPI.linkConstant.PGPAPIResourcePath.FETCH_TRANSACTION_NULLMID_CODE;

public class StoreQrCheckout extends PGPBaseTest {
    User user;
    String mid;
    void setUserAndMId(String merchant) throws Exception {
        user = userManager.getForWrite(Label.LINK);
        mid = merchant;
    }
    public List<String> dateFetchTransaction(int days){
        List<String> dates=new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String toDate= CommonHelpers.addDays(sdf.format(new Date()),"dd/MM/yyyy",days);
        String fromdate = sdf.format(new Date());
        dates.add(fromdate);
        dates.add(toDate);
        return dates;
    }

    @Owner("Himanshu Arora")
    @Feature("PGP-46148")
    @Test(description = "verify successful FetchTransactionApi link response when paymentLinkStatus=PENDING")
    public void StoreQrCheckout_01() throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","2");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        Integer linkId = withDrawJson1.getInt("body.linkId");
        List<String>dates=dateFetchTransaction(5);
        FetchTransactionApi fetchTransactionApi=new FetchTransactionApi().buildRequest(mid,linkId,dates.get(0),dates.get((1)));
        JsonPath withDrawJson2 = fetchTransactionApi.execute().jsonPath();
       Assert.assertEquals(withDrawJson2.getString("body.paymentLinkStatus"),"PENDING");
    }

    @Owner("Himanshu Arora")
    @Feature("PGP-46148")
    @Test(description = "verify successful FetchTransactionApi link response when paymentLinkStatus=EXPIRED")
    public void StoreQrCheckout_02() throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","2");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        Integer linkId = withDrawJson1.getInt("body.linkId");
        ExpireLink expireLink = new ExpireLink().buildRequest(mid,linkId.toString());
        JsonPath withDrawJson2 = expireLink.execute().jsonPath();
        List<String>dates=dateFetchTransaction(5);
        FetchTransactionApi fetchTransactionApi=new FetchTransactionApi().buildRequest(mid,linkId,dates.get(0),dates.get((1)));
        JsonPath withDrawJson3 = fetchTransactionApi.execute().jsonPath();
        Assert.assertEquals(withDrawJson3.getString("body.paymentLinkStatus"),"EXPIRED");
    }

    @Owner("Himanshu Arora")
    @Feature("PGP-46148")
    @Parameters({"theme"})
    @Test(description = "verify successful FetchTransactionApi link response when payment is done through card for fixed link.")
    public void StoreQrCheckout_03(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_TXN_PG2.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","1");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        Integer linkId = withDrawJson1.getInt("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        cashierPage.SuccessfullScreen().waitUntilVisible();
        List<String>dates=dateFetchTransaction(5);
        FetchTransactionApi fetchTransactionApi=new FetchTransactionApi().buildRequest(mid,linkId,dates.get(0),dates.get((1)));
        JsonPath withDrawJson2 = fetchTransactionApi.execute().jsonPath();
        Assert.assertEquals(withDrawJson2.getString("body.orders[0].paymentDetails.payMethod"),"CREDIT_CARD");
        Assert.assertFalse(withDrawJson2.getString("body.orders[0].paymentDetails.paytmUserId").isEmpty());
        Assert.assertFalse(withDrawJson2.getString("body.orders[0].paymentDetails.referenceNumber").isEmpty());
        Assert.assertFalse(withDrawJson2.getString("body.orders[0].paymentDetails.cardLastFourDigits").isEmpty());

    }

    @Owner("Himanshu Arora")
    @Feature("PGP-46148")
    @Parameters({"theme"})
    @Test(description = "verify successful FetchTransactionApi link response when payment is done through NB for fixed link.")
    public void StoreQrCheckout_04(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_TXN_PG2.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","1");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        Integer linkId = withDrawJson1.getInt("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.NB);
        cashierPage.SuccessfullScreen().waitUntilVisible();
        List<String>dates=dateFetchTransaction(5);
        FetchTransactionApi fetchTransactionApi=new FetchTransactionApi().buildRequest(mid,linkId,dates.get(0),dates.get((1)));
        JsonPath withDrawJson2 = fetchTransactionApi.execute().jsonPath();
        Assert.assertEquals(withDrawJson2.getString("body.orders[0].paymentDetails.payMethod"),"NET_BANKING");
        Assert.assertEquals(withDrawJson2.getString("body.orders[0].paymentDetails.issuingBankName"),"ICICI Bank");
        Assert.assertFalse(withDrawJson2.getString("body.orders[0].paymentDetails.paytmUserId").isEmpty());
        Assert.assertFalse(withDrawJson2.getString("body.orders[0].paymentDetails.referenceNumber").isEmpty());
    }

    @Owner("Himanshu Arora")
    @Feature("PGP-46148")
    @Parameters({"theme"})
    @Test(description = "verify successful FetchTransactionApi link response when paymentLinkStatus=PAID for Invoice link.")
    public void StoreQrCheckout_05(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_TXN_PG2.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"INVOICE","1");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        Integer linkId = withDrawJson1.getInt("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"INVOICE","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.NB);
        cashierPage.SuccessfullScreen().waitUntilVisible();
        List<String>dates=dateFetchTransaction(5);
        FetchTransactionApi fetchTransactionApi=new FetchTransactionApi().buildRequest(mid,linkId,dates.get(0),dates.get((1)));
        JsonPath withDrawJson2 = fetchTransactionApi.execute().jsonPath();
        Assert.assertEquals(withDrawJson2.getString("body.paymentLinkStatus"),"PAID");
        Assert.assertEquals(withDrawJson2.getString("body.orders[0].paymentDetails.payMethod"),"NET_BANKING");
    }

    @Owner("Himanshu Arora")
    @Feature("PGP-46148")
    @Parameters({"theme"})
    @Test(description = "verify successful FetchTransactionApi link response when payment is done through UPI for fixed link.")
    public void StoreQrCheckout_06(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.PG2_UPI.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","1");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        Integer linkId = withDrawJson1.getInt("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.UPI);
        cashierPage.SuccessfullScreen().waitUntilVisible();
        List<String>dates=dateFetchTransaction(5);
        FetchTransactionApi fetchTransactionApi=new FetchTransactionApi().buildRequest(mid,linkId,dates.get(0),dates.get((1)));
        JsonPath withDrawJson2 = fetchTransactionApi.execute().jsonPath();
        Assert.assertEquals(withDrawJson2.getString("body.orders[0].paymentDetails.payMethod"),"UPI");
        Assert.assertFalse(withDrawJson2.getString("body.orders[0].paymentDetails.paytmUserId").isEmpty());
        Assert.assertFalse(withDrawJson2.getString("body.orders[0].paymentDetails.referenceNumber").isEmpty());
        Assert.assertFalse(withDrawJson2.getString("body.orders[0].paymentDetails.virtualPaymentAddress").isEmpty());
    }

    @Owner("Himanshu Arora")
    @Feature("PGP-46148")
    @Parameters({"theme"})
    @Test(description = "verify successful FetchTransactionApi link response when paymentLinkStatus=PAID & maxPaymentsAllowed is used for fixed link.")
    public void StoreQrCheckout_07(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_TXN_PG2.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","1");
        createNewLink.setContext("body.maxPaymentsAllowed","1");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        Integer linkId = withDrawJson1.getInt("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.NB);
        cashierPage.SuccessfullScreen().waitUntilVisible();
        List<String>dates=dateFetchTransaction(5);
        FetchTransactionApi fetchTransactionApi=new FetchTransactionApi().buildRequest(mid,linkId,dates.get(0),dates.get((1)));
        JsonPath withDrawJson2 = fetchTransactionApi.execute().jsonPath();
        Assert.assertEquals(withDrawJson2.getString("body.paymentLinkStatus"),"PAID");
        Assert.assertEquals(withDrawJson2.getString("body.orders[0].paymentDetails.payMethod"),"NET_BANKING");
    }


}
