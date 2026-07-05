package scripts.api.linkservice;

import com.paytm.api.linkAPI.CreateNewLink;
import com.paytm.api.linkAPI.ExpireLink;
import com.paytm.api.linkAPI.ResendNotificationLink;
import com.paytm.appconstants.Constants;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.framework.reportportal.annotation.Owner;
import io.qameta.allure.Feature;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static com.paytm.api.linkAPI.linkConstant.PGPAPIResourcePath.*;

@Feature("PGP-40182")
public class ResendNotificationApiTest extends PGPBaseTest {

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
                        {"FIXED"},
                        {"GENERIC"},
                        {"INVOICE"}
                };
    }


    @Owner("Himanshu Arora")
    @Test(dataProvider = "Dataset",description="verify successful ResendNotificationLink link response for FIXED,INVOICE,GENERIC link.")
    public void resendNotificationLink_01(String linkType) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,linkType , "200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        ResendNotificationLink resendNotificationLinkLink = new ResendNotificationLink().buildRequest(mid,linkId);
        JsonPath withDrawJson2 = resendNotificationLinkLink.execute().jsonPath();
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultMessage"),RESEND_LINK_MSG);
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultCode"),RESEND_LINK_SUCCESS_CODE);
        Assertions.assertThat(withDrawJson2.getString("body.notificationDetails.customerName")).isNotNull();
    }

    @Owner("Himanshu Arora")
    @Test(dataProvider = "Dataset",description="verify ResendNotificationLink link response when mid is null for FIXED,INVOICE,GENERIC link.")
    public void resendNotificationLink_02(String linkType) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, linkType, "200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        ResendNotificationLink resendNotificationLinkLink = new ResendNotificationLink().buildRequest(null,linkId);
        JsonPath withDrawJson2 = resendNotificationLinkLink.execute().jsonPath();
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultMessage"),RESEND_LINK_NULLMID_MSG);
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultCode"),RESEND_LINK_NULLMID_CODE);
    }

    @Owner("Himanshu Arora")
    @Test(dataProvider = "Dataset",description="verify ResendNotificationLink link response when linkid is null for FIXED,INVOICE,GENERIC link.")
    public void resendNotificationLink_03(String linkType) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, linkType, "200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        ResendNotificationLink resendNotificationLinkLink = new ResendNotificationLink().buildRequest(mid,null);
        JsonPath withDrawJson2 = resendNotificationLinkLink.execute().jsonPath();
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultMessage"),RESEND_LINK_NULL_LINKID_MSG);
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultCode"),RESEND_LINK_NULL_LINKID_CODE);
    }

    @Owner("Himanshu Arora")
    @Test(dataProvider = "Dataset",description="verify ResendNotificationLink link response when link is expired  for FIXED,INVOICE,GENERIC link.")
    public void resendNotificationLink_04(String linkType) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, linkType, "200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        ExpireLink expireLink = new ExpireLink().buildRequest(mid,linkId);
        JsonPath withDrawJson2 = expireLink.execute().jsonPath();
        ResendNotificationLink resendNotificationLinkLink = new ResendNotificationLink().buildRequest(mid,linkId);
        JsonPath withDrawJson3 = resendNotificationLinkLink.execute().jsonPath();
        Assert.assertEquals(withDrawJson3.getString("body.resultInfo.resultMessage"),RESEND_LINK_EXPIRE_LINK_MSG);
        Assert.assertEquals(withDrawJson3.getString("body.resultInfo.resultCode"),RESEND_LINK_EXPIRE_LINK_CODE);
    }
}
