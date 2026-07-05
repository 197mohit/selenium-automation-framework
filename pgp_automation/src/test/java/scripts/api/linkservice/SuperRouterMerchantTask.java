package scripts.api.linkservice;

import com.paytm.api.linkAPI.CreateNewLink;
import com.paytm.api.linkAPI.FetchLinkApi;
import com.paytm.api.linkAPI.LinkHelper;
import com.paytm.api.linkAPI.linkConstant;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
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
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.paytm.api.linkAPI.linkConstant.PGPAPIResourcePath.CREATE_LINK_FAILEDMSG;

public class SuperRouterMerchantTask extends PGPBaseTest {
    User user;
    String mid;
    void setUserAndMId(String merchant) throws Exception {
        user = userManager.getForWrite(Label.LINK);
        mid = merchant;
    }
    public List<String> dateFetchLink(int days){
        List<String> dates=new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String toDate= CommonHelpers.addDays(sdf.format(new Date()),"dd/MM/yyyy",days);
        String fromdate = sdf.format(new Date());
        dates.add(fromdate);
        dates.add(toDate);
        return dates;
    }
    @Owner("Nirottam")
    @Feature("PAPR-4227")
    @Parameters({"theme"})
    @Test(description = "verify Successfull link creation for fixed link for super router link")
    public void superRouter_001(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        RestAssured.useRelaxedHTTPSValidation();
        setUserAndMId(Constants.MerchantType.SUPER_ROUTER_MERCHANT.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","1000");
        JsonPath res = createNewLink.execute().jsonPath();
        Assert.assertEquals(res.getString("body.resultInfo.resultStatus"), "SUCCESS");
        Assert.assertEquals(res.getString("body.resultInfo.resultMessage"),"Payment link is created successfully");
        String longUrl= res.getString("body.longUrl");
        boolean superRouterLongUrl= LinkHelper.verifySuperRouterMerchantLongUrl(longUrl);
        Assert.assertTrue(superRouterLongUrl);

    }
    @Owner("Nirottam")
    @Feature("PAPR-4227")
    @Parameters({"theme"})
    @Test(description = "verify Successfull link creation for generic link for super router link")
    public void superRouter_002(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        RestAssured.useRelaxedHTTPSValidation();
        setUserAndMId(Constants.MerchantType.SUPER_ROUTER_MERCHANT.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"GENERIC","1000");
        JsonPath res = createNewLink.execute().jsonPath();
        Assert.assertEquals(res.getString("body.resultInfo.resultStatus"), "SUCCESS");
        Assert.assertEquals(res.getString("body.resultInfo.resultMessage"),"Payment link is created successfully");
        String longUrl= res.getString("body.longUrl");
        boolean superRouterLongUrl= LinkHelper.verifySuperRouterMerchantLongUrl(longUrl);
        Assert.assertTrue(superRouterLongUrl);

    }
    @Owner("Nirottam")
    @Feature("PAPR-4227")
    @Parameters({"theme"})
    @Test(description = "verify Successfull link creation for Invoice link for super router link")
    public void superRouter_003(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        RestAssured.useRelaxedHTTPSValidation();
        setUserAndMId(Constants.MerchantType.SUPER_ROUTER_MERCHANT.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"INVOICE","1000");
        JsonPath res = createNewLink.execute().jsonPath();
        Assert.assertEquals(res.getString("body.resultInfo.resultStatus"), "SUCCESS");
        Assert.assertEquals(res.getString("body.resultInfo.resultMessage"),"Payment link is created successfully");
        String longUrl= res.getString("body.longUrl");
        boolean isSuperRouterLongUrl=LinkHelper.verifySuperRouterMerchantLongUrlForInvoiceLink(longUrl);
        System.out.println(isSuperRouterLongUrl);
        Assert.assertTrue(isSuperRouterLongUrl);


    }
    @Owner("Nirottam")
    @Feature("PAPR-4227")
    @Parameters({"theme"})
    @Test(description = "verify Successfull link creation for PAYMENT_BUTTON link for super router link")
    public void superRouter_004(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        RestAssured.useRelaxedHTTPSValidation();
        setUserAndMId(Constants.MerchantType.SUPER_ROUTER_MERCHANT.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"PAYMENT_BUTTON","1000");
        JsonPath res = createNewLink.execute().jsonPath();
        Assert.assertEquals(res.getString("body.resultInfo.resultStatus"), "SUCCESS");
        Assert.assertEquals(res.getString("body.resultInfo.resultMessage"), "Payment link is created successfully");
        String longUrl= res.getString("body.longUrl");
        boolean superRouterLongUrl= LinkHelper.verifySuperRouterMerchantLongUrl(longUrl);
        Assert.assertTrue(superRouterLongUrl);
    }
    @Owner("Nirottam")
    @Feature("PAPR-4227")
    @Parameters({"theme"})
    @Test(description = "verify Successfull fetch link response for fixed link for super router link")
    public void superRouter_005(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        RestAssured.useRelaxedHTTPSValidation();
        setUserAndMId(Constants.MerchantType.SUPER_ROUTER_MERCHANT.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","1000");
        JsonPath res = createNewLink.execute().jsonPath();
        Assert.assertEquals(res.getString("body.resultInfo.resultStatus"), "SUCCESS");
        Assert.assertEquals(res.getString("body.resultInfo.resultMessage"), "Payment link is created successfully");
        String longUrl= res.getString("body.longUrl");
        String linkId= res.getString("body.linkId");
        boolean superRouterLongUrl= LinkHelper.verifySuperRouterMerchantLongUrl(longUrl);
        Assert.assertTrue(superRouterLongUrl);
        FetchLinkApi fetchLinkApi=new FetchLinkApi();
        List<String>dates=dateFetchLink(5);
        fetchLinkApi.buildRequest(mid,linkId,dates.get(1),dates.get((0)));
        JsonPath fetchLinkResponse=fetchLinkApi.execute().jsonPath();
        Assert.assertEquals(fetchLinkResponse.getString("body.resultInfo.resultStatus"),"SUCCESS");

    }
    @Owner("Nirottam")
    @Feature("PAPR-4227")
    @Parameters({"theme"})
    @Test(description = "verify Successfull fetch link response for fixed link for super router link")
    public void superRouter_006(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        RestAssured.useRelaxedHTTPSValidation();
        setUserAndMId(Constants.MerchantType.SUPER_ROUTER_MERCHANT.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","1000");
        JsonPath res = createNewLink.execute().jsonPath();
        Assert.assertEquals(res.getString("body.resultInfo.resultStatus"), "SUCCESS");
        Assert.assertEquals(res.getString("body.resultInfo.resultMessage"), "Payment link is created successfully");
        String longUrl= res.getString("body.longUrl");
        String linkId= res.getString("body.linkId");
        boolean superRouterLongUrl= LinkHelper.verifySuperRouterMerchantLongUrl(longUrl);
        Assert.assertTrue(superRouterLongUrl);
        FetchLinkApi fetchLinkApi=new FetchLinkApi();
        List<String>dates=dateFetchLink(5);
        fetchLinkApi.buildRequest(mid,linkId,dates.get(1),dates.get((0)));
        JsonPath fetchLinkResponse=fetchLinkApi.execute().jsonPath();
        Assert.assertEquals(fetchLinkResponse.getString("body.resultInfo.resultStatus"),"SUCCESS");

    }
    @Owner("Nirottam")
    @Feature("PAPR-4227")
    @Parameters({"theme"})
    @Test(description = "verify Successfull fetch link response for fixed link for super router link")
    public void superRouter_007(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        RestAssured.useRelaxedHTTPSValidation();
        setUserAndMId(Constants.MerchantType.SUPER_ROUTER_MERCHANT.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"GENERIC","1000");
        JsonPath res = createNewLink.execute().jsonPath();
        Assert.assertEquals(res.getString("body.resultInfo.resultStatus"), "SUCCESS");
        Assert.assertEquals(res.getString("body.resultInfo.resultMessage"), "Payment link is created successfully");
        String longUrl= res.getString("body.longUrl");
        String linkId= res.getString("body.linkId");
        boolean superRouterLongUrl= LinkHelper.verifySuperRouterMerchantLongUrl(longUrl);
        Assert.assertTrue(superRouterLongUrl);
        FetchLinkApi fetchLinkApi=new FetchLinkApi();
        List<String>dates=dateFetchLink(5);
        fetchLinkApi.buildRequest(mid,linkId,dates.get(1),dates.get((0)));
        JsonPath fetchLinkResponse=fetchLinkApi.execute().jsonPath();
        Assert.assertEquals(fetchLinkResponse.getString("body.resultInfo.resultStatus"),"SUCCESS");

    }
    @Owner("Nirottam")
    @Feature("PAPR-4227")
    @Parameters({"theme"})
    @Test(description = "verify Successfull fetch link response for fixed link for super router link")
    public void superRouter_008(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        RestAssured.useRelaxedHTTPSValidation();
        setUserAndMId(Constants.MerchantType.SUPER_ROUTER_MERCHANT.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"INVOICE","1000");
        JsonPath res = createNewLink.execute().jsonPath();
        Assert.assertEquals(res.getString("body.resultInfo.resultStatus"), "SUCCESS");
        Assert.assertEquals(res.getString("body.resultInfo.resultMessage"), "Payment link is created successfully");
        String longUrl= res.getString("body.longUrl");
        String linkId= res.getString("body.linkId");
        FetchLinkApi fetchLinkApi=new FetchLinkApi();
        List<String>dates=dateFetchLink(5);
        fetchLinkApi.buildRequest(mid,linkId,dates.get(1),dates.get((0)));
        JsonPath fetchLinkResponse=fetchLinkApi.execute().jsonPath();
        Assert.assertEquals(fetchLinkResponse.getString("body.resultInfo.resultStatus"),"SUCCESS");
        boolean isSuperRouterLongUrl=LinkHelper.verifySuperRouterMerchantLongUrlForInvoiceLink(longUrl);
        System.out.println(isSuperRouterLongUrl);
        Assert.assertTrue(isSuperRouterLongUrl);

    }
    @Owner("Nirottam")
    @Feature("PAPR-4227")
    @Parameters({"theme"})
    @Test(description = "verify Successfull fetch link response for payment button link for super router link")
    public void superRouter_009(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        RestAssured.useRelaxedHTTPSValidation();
        setUserAndMId(Constants.MerchantType.SUPER_ROUTER_MERCHANT.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"PAYMENT_BUTTON","1000");
        JsonPath res = createNewLink.execute().jsonPath();
        Assert.assertEquals(res.getString("body.resultInfo.resultStatus"), "SUCCESS");
        Assert.assertEquals(res.getString("body.resultInfo.resultMessage"), "Payment link is created successfully");
        String longUrl= res.getString("body.longUrl");
        String linkId= res.getString("body.linkId");
        boolean superRouterLongUrl= LinkHelper.verifySuperRouterMerchantLongUrl(longUrl);
        Assert.assertTrue(superRouterLongUrl);
        FetchLinkApi fetchLinkApi=new FetchLinkApi();
        List<String>dates=dateFetchLink(5);
        fetchLinkApi.buildRequest(mid,linkId,dates.get(1),dates.get((0)));
        JsonPath fetchLinkResponse=fetchLinkApi.execute().jsonPath();
        Assert.assertEquals(fetchLinkResponse.getString("body.resultInfo.resultStatus"),"SUCCESS");

    }

}
