package scripts.api.linkservice;

import com.paytm.api.linkAPI.ArchiveLink;
import com.paytm.api.linkAPI.CreateNewLink;
import com.paytm.appconstants.Constants;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.framework.reportportal.annotation.Owner;
import io.qameta.allure.Feature;
import io.restassured.path.json.JsonPath;
import org.testng.Assert;
import org.testng.annotations.Test;

import static com.paytm.api.linkAPI.linkConstant.PGPAPIResourcePath.*;

@Feature("PGP-40123")
public class ArchiveLinkApiTest extends PGPBaseTest {
    User user;
    String mid;
    void setUserAndMId(String merchant) throws Exception {
        user = userManager.getForRead(Label.LINK);
        mid = merchant;
    }

    @Owner("Himanshu Arora")
    @Test(description = "verify successful archive link response for FIXED link.")
    public void archiveLink_01() throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, "FIXED", "200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        ArchiveLink archiveLink = new ArchiveLink().buildRequest(mid,linkId);
        JsonPath withDrawJson2 = archiveLink.execute().jsonPath();
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultMessage"),ARCHIVE_LINK_SUCCESS);
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultCode"),ARCHIVE_LINK_SUCCESS_CODE);
    }

    @Owner("Himanshu Arora")
    @Test(description = "verify archive link response for null link id for FIXED link.")
    public void archiveLink_02() throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, "FIXED", "200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        ArchiveLink archiveLink = new ArchiveLink().buildRequest(mid,null);
        JsonPath withDrawJson2 = archiveLink.execute().jsonPath();
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultMessage"),ARCHIVE_LINK_NULL_LINKID_MSG);
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultCode"),ARCHIVE_LINK_NULL_LINKID_CODE);
    }

    @Owner("Himanshu Arora")
    @Test(description = "verify archive link response for null mid for FIXED link.")
    public void archiveLink_03() throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, "FIXED", "200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        ArchiveLink archiveLink = new ArchiveLink().buildRequest(null,linkId);
        JsonPath withDrawJson2 = archiveLink.execute().jsonPath();
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultMessage"),ARCHIVE_LINK_NULL_MIDMSG);
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultCode"),ARCHIVE_LINK_NULL_MID_CODE);
    }

    @Owner("Himanshu Arora")
    @Test(description = "verify archive link response for null mid & link id for FIXED link.")
    public void archiveLink_04() throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, "FIXED", "200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        ArchiveLink archiveLink = new ArchiveLink().buildRequest(null,null);
        JsonPath withDrawJson2 = archiveLink.execute().jsonPath();
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultStatus"),ARCHIVE_LINK_FAILED);
    }

    @Owner("Himanshu Arora")
    @Test(description = "verify successful archive link response for GENERIC link.")
    public void archiveLink_05() throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, "GENERIC", "200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        ArchiveLink archiveLink = new ArchiveLink().buildRequest(mid,linkId);
        JsonPath withDrawJson2 = archiveLink.execute().jsonPath();
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultMessage"),ARCHIVE_LINK_SUCCESS);
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultCode"),ARCHIVE_LINK_SUCCESS_CODE);
    }

    @Owner("Himanshu Arora")
    @Test(description = "verify successful archive link response for INVOICE link.")
    public void archiveLink_06() throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, "INVOICE", "200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        ArchiveLink archiveLink = new ArchiveLink().buildRequest(mid,linkId);
        JsonPath withDrawJson2 = archiveLink.execute().jsonPath();
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultMessage"),ARCHIVE_LINK_SUCCESS);
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultCode"),ARCHIVE_LINK_SUCCESS_CODE);
    }

    @Owner("Himanshu Arora")
    @Test(description = "verify successful archive link response for INVOICE link.")
    public void archiveLink_07() throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, "INVOICE", "200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        ArchiveLink archiveLink = new ArchiveLink().buildRequest(mid,linkId);
        JsonPath withDrawJson2 = archiveLink.execute().jsonPath();
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultMessage"),ARCHIVE_LINK_SUCCESS);
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultCode"),ARCHIVE_LINK_SUCCESS_CODE);
    }

    @Owner("Himanshu Arora")
    @Test(description = "verify successful archive link response for PAYMENT_BUTTON link.")
    public void archiveLink_08() throws Exception {
        setUserAndMId(Constants.MerchantType.ENABLE_DISABLE_PAYMENT_BUTTON.getId().toString());
        CreateNewLink createNewLinkPaymentBtn = new CreateNewLink().buildRequest(mid,"PAYMENT_BUTTON","200");
        createNewLinkPaymentBtn.deleteContext("body.sendSms");
        createNewLinkPaymentBtn.deleteContext("body.sendEmail");
        createNewLinkPaymentBtn.deleteContext("body.customerContact");
        JsonPath withDrawJson1 = createNewLinkPaymentBtn.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        ArchiveLink archiveLink = new ArchiveLink().buildRequest(mid,linkId);
        JsonPath withDrawJson2 = archiveLink.execute().jsonPath();
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultMessage"),ARCHIVE_LINK_SUCCESS);
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultCode"),ARCHIVE_LINK_SUCCESS_CODE);
    }

    @Owner("Himanshu Arora")
    @Test(description = "verify archive link response for null link id for GENERIC link.")
    public void archiveLink_09() throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, "GENERIC", "200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        ArchiveLink archiveLink = new ArchiveLink().buildRequest(mid,null);
        JsonPath withDrawJson2 = archiveLink.execute().jsonPath();
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultMessage"),ARCHIVE_LINK_NULL_LINKID_MSG);
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultCode"),ARCHIVE_LINK_NULL_LINKID_CODE);
    }

    @Owner("Himanshu Arora")
    @Test(description = "verify archive link response for null link id for INVOICE link.")
    public void archiveLink_10() throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, "INVOICE", "200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        ArchiveLink archiveLink = new ArchiveLink().buildRequest(mid,null);
        JsonPath withDrawJson2 = archiveLink.execute().jsonPath();
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultMessage"),ARCHIVE_LINK_NULL_LINKID_MSG);
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultCode"),ARCHIVE_LINK_NULL_LINKID_CODE);
    }

    @Owner("Himanshu Arora")
    @Test(description = "verify archive link response for null mid for GENERIC link.")
    public void archiveLink_11() throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, "GENERIC", "200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        ArchiveLink archiveLink = new ArchiveLink().buildRequest(null,linkId);
        JsonPath withDrawJson2 = archiveLink.execute().jsonPath();
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultMessage"),ARCHIVE_LINK_NULL_MIDMSG);
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultCode"),ARCHIVE_LINK_NULL_MID_CODE);
    }

    @Owner("Himanshu Arora")
    @Test(description = "verify archive link response for null mid for INVOICE link.")
    public void archiveLink_12() throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, "INVOICE", "200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        ArchiveLink archiveLink = new ArchiveLink().buildRequest(null,linkId);
        JsonPath withDrawJson2 = archiveLink.execute().jsonPath();
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultMessage"),ARCHIVE_LINK_NULL_MIDMSG);
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultCode"),ARCHIVE_LINK_NULL_MID_CODE);
    }

}
