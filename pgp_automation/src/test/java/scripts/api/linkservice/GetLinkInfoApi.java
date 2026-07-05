package scripts.api.linkservice;//package scripts.api.linkservice;

import com.paytm.ServerConfigProvider;
import com.paytm.api.linkAPI.CreateNewLink;
import com.paytm.api.linkAPI.GetLinkInfo;
import com.paytm.appconstants.Constants;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.pages.LinkPaymentLoginPage;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.testng.Assert;
import org.testng.annotations.Optional;
import org.testng.annotations.Test;

import static com.paytm.apphelpers.LogsValidationHelper.getLogsOnServer;
@Feature("PGP-37787")
public class GetLinkInfoApi extends PGPBaseTest {
    User user;
    String mid;
    void setUserAndMId(String merchant) throws Exception {
        user = userManager.getForRead(Label.LINK);
        mid = merchant;
    }
    @Owner("Nirottam")
    @Test(description = "verify link response detail  for Fixed link in GetLinkInfoApi Response")
    public void GetLinkInfo_001(@Optional("enhancedweb_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","20");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String shortUrl = withDrawJson1.getString("body.shortUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        GetLinkInfo getLinkInfo = new GetLinkInfo(linkId,"TestingLink");
        JsonPath withDrawJson = getLinkInfo.execute().jsonPath();
        Assert.assertEquals(withDrawJson.getString("id"),linkId);
        Assert.assertEquals(withDrawJson.getString("linkName"),"TestingLink");
        Assert.assertEquals(withDrawJson.getString("linkDescription"),"123");
        Assert.assertEquals(withDrawJson.getString("mid"),mid);
        Assert.assertEquals(withDrawJson.getString("amount"),"20.0");
        Assert.assertEquals(withDrawJson.getString("longUrl"),paymentLink);
        Assert.assertEquals(withDrawJson.getString("shortUrl"),shortUrl);
        Assert.assertEquals(withDrawJson.getString("linkType"),"FIXED");
        Assert.assertNotNull(withDrawJson.getString("txnToken"));
        Assert.assertNotNull(withDrawJson.getString("orderId"));
    }

    @Owner("Nirottam")
    @Test(description = "verify link response detail for INVOICE link in GetLinkInfoApi Response")
    public void GetLinkInfo_002(@Optional("enhancedweb_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"INVOICE","20");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        GetLinkInfo getLinkInfo = new GetLinkInfo(linkId,"TestingLink");
        String invoiceUrl="https://pgp-automation.paytm.in/link/onlinePayment?linkName=TestingLink&linkId=LL_"+linkId;
        JsonPath withDrawJson = getLinkInfo.execute().jsonPath();
        Assert.assertEquals(withDrawJson.getString("id"),linkId);
        Assert.assertEquals(withDrawJson.getString("linkName"),"TestingLink");
        Assert.assertEquals(withDrawJson.getString("linkDescription"),"123");
        Assert.assertEquals(withDrawJson.getString("mid"),mid);
        Assert.assertEquals(withDrawJson.getString("amount"),"20.0");
        Assert.assertEquals(withDrawJson.getString("longUrl"),invoiceUrl);
        Assert.assertNotNull(withDrawJson.getString("longUrl"));
        Assert.assertNotNull(withDrawJson.getString("shortUrl"));
        Assert.assertNotNull(withDrawJson.getString("invoiceId"));
        Assert.assertEquals(withDrawJson.getString("linkType"),"INVOICE");
        Assert.assertNotNull(withDrawJson.getString("txnToken"));
        Assert.assertNotNull(withDrawJson.getString("orderId"));
    }

    @Owner("Nirottam")
    @Test(description = "verify link response detail for GENERIC link in GetLinkInfoApi Response")
    public void GetLinkInfo_003(@Optional("enhancedweb_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"GENERIC","20");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String shortUrl = withDrawJson1.getString("body.shortUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        GetLinkInfo getLinkInfo = new GetLinkInfo(linkId,"TestingLink");
        JsonPath withDrawJson = getLinkInfo.execute().jsonPath();
        Assert.assertEquals(withDrawJson.getString("id"),linkId);
        Assert.assertEquals(withDrawJson.getString("linkName"),"TestingLink");
        Assert.assertEquals(withDrawJson.getString("linkDescription"),"123");
        Assert.assertEquals(withDrawJson.getString("mid"),mid);
        Assert.assertEquals(withDrawJson.getString("longUrl"),paymentLink);
        Assert.assertEquals(withDrawJson.getString("shortUrl"),shortUrl);
        Assert.assertEquals(withDrawJson.getString("linkType"),"GENERIC");
    }

    @Owner("Nirottam")
    @Test(description = "verify linkId for GENERIC link in GetLinkInfoApi Response")
    public void GetLinkInfo_004(@Optional("enhancedweb_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"GENERIC","20");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String shortUrl = withDrawJson1.getString("body.shortUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        GetLinkInfo getLinkInfo = new GetLinkInfo(linkId,"TestingLink");
        JsonPath withDrawJson = getLinkInfo.execute().jsonPath();
        String grepcmd = "grep \"" + "\" /paytm/logs/linkService.log | " +
                "grep \"getLinkInfo API response MerchantPaymentLink\" ";
        String linkServiceLogs = getLogsOnServer(ServerConfigProvider.SERVICE.LINKSERVICE, grepcmd);
        Assertions.assertThat(linkServiceLogs).contains("mid=testli61258254741921");
        Assertions.assertThat(linkServiceLogs).contains("linkName=TestingLink");
        Assertions.assertThat(linkServiceLogs).contains("linkDescription=123");
        Assertions.assertThat(linkServiceLogs).contains("amount=null");
        Assertions.assertThat(linkServiceLogs).contains("longUrl="+paymentLink);
        Assertions.assertThat(linkServiceLogs).contains("shortUrl="+shortUrl);
        Assertions.assertThat(linkServiceLogs).doesNotContain("longUrl=null");
        Assertions.assertThat(linkServiceLogs).doesNotContain("shortUrl=null");
        Assertions.assertThat(linkServiceLogs).contains("linkType=GENERIC");
        Assertions.assertThat(linkServiceLogs).contains("pgEnabled=true");
        Assertions.assertThat(linkServiceLogs).contains("merchantName=abhishek");
        Assertions.assertThat(linkServiceLogs).doesNotContain("expiryDate=null");
        Assertions.assertThat(linkServiceLogs).contains("expiryDate");
        Assertions.assertThat(linkServiceLogs).contains("penaltyFee=null");
        Assertions.assertThat(linkServiceLogs).contains("txnToken=null");
        Assertions.assertThat(linkServiceLogs).contains(" orderId=null");
        Assertions.assertThat(linkServiceLogs).contains("sendSMS=false");
        Assertions.assertThat(linkServiceLogs).contains("sendEmail=false");
        Assertions.assertThat(linkServiceLogs).contains("isPartialPayment=false");
        Assertions.assertThat(linkServiceLogs).contains("merchantLinkId="+linkId);
    }

    @Owner("Nirottam")
    @Test(description = "verify linkId for FIXED link in GetLinkInfoApi Response")
    public void GetLinkInfo_005(@Optional("enhancedweb_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","20");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String shortUrl = withDrawJson1.getString("body.shortUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        GetLinkInfo getLinkInfo = new GetLinkInfo(linkId,"TestingLink");
        JsonPath withDrawJson = getLinkInfo.execute().jsonPath();
        String grepcmd = "grep \"" +  "\" /paytm/logs/linkService.log  | " +
                "grep \"" + linkId+ "\" | grep \"getLinkInfo API response MerchantPaymentLink\"";

        String linkServiceLogs = getLogsOnServer(ServerConfigProvider.SERVICE.LINKSERVICE, grepcmd);
        Assertions.assertThat(linkServiceLogs).contains("mid=testli61258254741921");
        Assertions.assertThat(linkServiceLogs).contains("linkName=TestingLink");
        Assertions.assertThat(linkServiceLogs).contains("linkDescription=123");
        Assertions.assertThat(linkServiceLogs).contains("amount=20");
        Assertions.assertThat(linkServiceLogs).contains("longUrl="+paymentLink);
        Assertions.assertThat(linkServiceLogs).contains("shortUrl="+shortUrl);
        Assertions.assertThat(linkServiceLogs).doesNotContain("longUrl=null");
        Assertions.assertThat(linkServiceLogs).doesNotContain("shortUrl=null");
        Assertions.assertThat(linkServiceLogs).contains("linkType=FIXED");
        Assertions.assertThat(linkServiceLogs).contains("pgEnabled=true");
        Assertions.assertThat(linkServiceLogs).contains("merchantName=abhishek");
        Assertions.assertThat(linkServiceLogs).doesNotContain("expiryDate=null");
        Assertions.assertThat(linkServiceLogs).contains("expiryDate");
        Assertions.assertThat(linkServiceLogs).contains("penaltyFee=null");
        Assertions.assertThat(linkServiceLogs).contains("txnToken");
        Assertions.assertThat(linkServiceLogs).contains(" orderId");
        Assertions.assertThat(linkServiceLogs).doesNotContain("txnToken=null");
        Assertions.assertThat(linkServiceLogs).doesNotContain("orderId=null");
        Assertions.assertThat(linkServiceLogs).contains("sendSMS=false");
        Assertions.assertThat(linkServiceLogs).contains("sendEmail=false");
        Assertions.assertThat(linkServiceLogs).contains("isPartialPayment=false");
        Assertions.assertThat(linkServiceLogs).contains("merchantLinkId="+linkId);
    }

    @Owner("Nirottam")
    @Test(description = "verify linkId for INVOICE link in GetLinkInfoApi Response")
    public void GetLinkInfo_006(@Optional("enhancedweb_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, "INVOICE", "20");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String shortUrl = withDrawJson1.getString("body.shortUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        GetLinkInfo getLinkInfo = new GetLinkInfo(linkId, "TestingLink");
        JsonPath withDrawJson = getLinkInfo.execute().jsonPath();
        String invoiceUrl = "https://pgp-automation.paytm.in/link/onlinePayment?linkName=TestingLink&linkId=LL_" + linkId;
        String grepcmd = "grep \"" + "\" /paytm/logs/linkService.log  | " +
                "grep \"" + linkId + "\" | grep \"getLinkInfo API response MerchantPaymentLink\"";

        String linkServiceLogs = getLogsOnServer(ServerConfigProvider.SERVICE.LINKSERVICE, grepcmd);
        Assertions.assertThat(linkServiceLogs).contains("mid=testli61258254741921");
        Assertions.assertThat(linkServiceLogs).contains("linkName=TestingLink");
        Assertions.assertThat(linkServiceLogs).contains("linkDescription=123");
        Assertions.assertThat(linkServiceLogs).contains("amount=20");
        Assertions.assertThat(linkServiceLogs).contains("longUrl=" + invoiceUrl);
        Assertions.assertThat(linkServiceLogs).contains("shortUrl");
        Assertions.assertThat(linkServiceLogs).doesNotContain("shortUrl=null");
        Assertions.assertThat(linkServiceLogs).doesNotContain("longUrl=null");
        Assertions.assertThat(linkServiceLogs).doesNotContain("shortUrl=null");
        Assertions.assertThat(linkServiceLogs).contains("linkType=INVOICE");
        Assertions.assertThat(linkServiceLogs).contains("pgEnabled=true");
        Assertions.assertThat(linkServiceLogs).contains("merchantName=abhishek");
        Assertions.assertThat(linkServiceLogs).doesNotContain("expiryDate=null");
        Assertions.assertThat(linkServiceLogs).contains("expiryDate");
        Assertions.assertThat(linkServiceLogs).contains("penaltyFee=0.0");
        Assertions.assertThat(linkServiceLogs).contains("txnToken");
        Assertions.assertThat(linkServiceLogs).contains(" orderId");
        Assertions.assertThat(linkServiceLogs).doesNotContain("txnToken=null");
        Assertions.assertThat(linkServiceLogs).doesNotContain("orderId=null");
        Assertions.assertThat(linkServiceLogs).contains("sendSMS=false");
        Assertions.assertThat(linkServiceLogs).contains("sendEmail=false");
        Assertions.assertThat(linkServiceLogs).contains("isPartialPayment=false");
        Assertions.assertThat(linkServiceLogs).contains("merchantLinkId=" + linkId);
    }

}
