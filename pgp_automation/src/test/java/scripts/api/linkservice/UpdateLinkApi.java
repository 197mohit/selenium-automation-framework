package scripts.api.linkservice;

import com.paytm.api.linkAPI.CreateNewLink;
import com.paytm.api.linkAPI.UpdateLink;
import com.paytm.appconstants.Constants;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.framework.reportportal.annotation.Owner;
import io.qameta.allure.Feature;
import io.restassured.path.json.JsonPath;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static com.paytm.api.linkAPI.linkConstant.PGPAPIResourcePath.*;

@Feature("PGP-40191")
public class UpdateLinkApi extends PGPBaseTest{
    User user;
    String mid;
    void setUserAndMId(String merchant) throws Exception {
        user = userManager.getForRead(PGPBaseTest.Label.LINK);
        mid = merchant;
    }
    @DataProvider(name = "Dataset")
    public Object[][] linkTypeSet(){
        return new Object[][]{
                {"FIXED"},
                {"GENERIC"},
                {"INVOICE"}
        };
    }

    @Owner("Himanshu Arora")
    @Test(dataProvider = "Dataset",description = "verify successful update link response for FIXED,GENERIC,INVOICE link.")
    public void updateLink_01(String linkType) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, linkType, "200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        UpdateLink updateLink =new UpdateLink(mid,linkId);
        JsonPath withDrawJson2 = updateLink.execute().jsonPath();
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultMessage"),UPDATE_LINK_SUCCESS);
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultCode"),UPDATE_LINK_SUCCESS_CODE);
    }

    @Owner("Himanshu Arora")
    @Test(dataProvider = "Dataset",description = "verify update link response for null link id  for FIXED,GENERIC,INVOICE link.")
    public void updateLink_02(String linkType) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, linkType, "200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        UpdateLink updateLink =new UpdateLink(mid,null);
        JsonPath withDrawJson2 = updateLink.execute().jsonPath();
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultMessage"),UPDATE_LINK_EMPTY_LINKID);
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultCode"),UPDATE_LINK_EMPTY_LINKID_CODE);
    }

    @Owner("Himanshu Arora")
    @Test(dataProvider = "Dataset",description = "verify update link response for null link id  for FIXED,GENERIC,INVOICE link.")
    public void updateLink_03(String linkType) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, linkType, "200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        UpdateLink updateLink =new UpdateLink(null,linkId);
        JsonPath withDrawJson2 = updateLink.execute().jsonPath();
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultMessage"),UPDATE_LINK_EMPTY_MID);
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultCode"),UPDATE_LINK_EMPTY_MIDCODE);
    }

    @Owner("Himanshu Arora")
    @Test(description = "verify successful expire link response for PAYMENT_BUTTON link.")
    public void updateLink_04() throws Exception {
        setUserAndMId(Constants.MerchantType.ENABLE_DISABLE_PAYMENT_BUTTON.getId().toString());
        CreateNewLink createNewLinkPaymentBtn = new CreateNewLink().buildRequest(mid,"PAYMENT_BUTTON","200");
        createNewLinkPaymentBtn.deleteContext("body.sendSms");
        createNewLinkPaymentBtn.deleteContext("body.sendEmail");
        createNewLinkPaymentBtn.deleteContext("body.customerContact");
        JsonPath withDrawJson1 = createNewLinkPaymentBtn.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        UpdateLink updateLink =new UpdateLink(mid,linkId);
        JsonPath withDrawJson2 = updateLink.execute().jsonPath();
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultMessage"),UPDATE_LINK_SUCCESS);
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultCode"),UPDATE_LINK_SUCCESS_CODE);
    }

    @Owner("Himanshu Arora")
    @Test(dataProvider = "Dataset",description = "verify successful update link response for FIXED,GENERIC,INVOICE link.")
    public void updateLink_05(String linkType) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, linkType, "200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        UpdateLink updateLink =new UpdateLink(mid,linkId);
        updateLink.setContext("body.expiryDate","16/09/2028 16:27:00");
        updateLink.setContext("body.linkDescription","link paymen123t");
        JsonPath withDrawJson2 = updateLink.execute().jsonPath();
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultMessage"),UPDATE_LINK_SUCCESS);
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultCode"),UPDATE_LINK_SUCCESS_CODE);
    }

    @Owner("Himanshu Arora")
    @Test(description = "verify successful update link response for FIXED link for change amount.")
    public void updateLink_06() throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, "FIXED", "200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        UpdateLink updateLink =new UpdateLink(mid,linkId);
        updateLink.setContext("body.amount","10");
        JsonPath withDrawJson2 = updateLink.execute().jsonPath();
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultMessage"),UPDATE_LINK_SUCCESS);
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultCode"),UPDATE_LINK_SUCCESS_CODE);
    }

    @Owner("Himanshu Arora")
    @Test(description = "verify successful update link response for FIXED link for change amount.")
    public void updateLink_07() throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, "GENERIC", "200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        UpdateLink updateLink =new UpdateLink(mid,linkId);
        updateLink.setContext("body.amount","10");
        JsonPath withDrawJson2 = updateLink.execute().jsonPath();
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultMessage"),UPDATE_LINK_AMOUNT_MSG);
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultCode"),UPDATE_LINK_AMOUNT_CODE);
    }

}
