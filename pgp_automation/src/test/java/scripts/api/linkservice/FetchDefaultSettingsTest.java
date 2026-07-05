package scripts.api.linkservice;

import com.paytm.api.linkAPI.CreateNewLink;
import com.paytm.api.linkAPI.FetchDefaultSettings;
import com.paytm.api.linkAPI.SaveOrUpdateLinkDefaultSettingsApi;
import com.paytm.appconstants.Constants;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.framework.reportportal.annotation.Owner;
import io.qameta.allure.Feature;
import io.restassured.path.json.JsonPath;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;

import static com.paytm.api.linkAPI.linkConstant.PGPAPIResourcePath.*;

@Feature("PGP-40399")
public class FetchDefaultSettingsTest extends PGPBaseTest {

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
    @Test(dataProvider = "Dataset",description="verify successful FetchDefaultSettings link response for FIXED,INVOICE,GENERIC link.")
    public void fetchDefaultSettings_01(String linkType) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,linkType , "200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        ArrayList<Integer> daysAfterIssueDate=new ArrayList<Integer>();
        ArrayList<Integer> daysBeforeExpiry=new ArrayList<Integer>();
        ArrayList<String>  channels=new ArrayList<String>();
        daysAfterIssueDate.add(1);
        daysBeforeExpiry.add(1);
        channels.add("SMS");
        SaveOrUpdateLinkDefaultSettingsApi saveOrUpdateLinkDefaultSettingsApi=new SaveOrUpdateLinkDefaultSettingsApi().buildRequest(mid,daysAfterIssueDate,daysBeforeExpiry,channels);
        JsonPath withDrawJson2 = saveOrUpdateLinkDefaultSettingsApi.execute().jsonPath();
        FetchDefaultSettings fetchDefaultSettings=new FetchDefaultSettings().buildRequest(mid);
        JsonPath withDrawJson3 = fetchDefaultSettings.execute().jsonPath();
        Assert.assertEquals(withDrawJson3.getString("body.resultInfo.resultMessage"),FETCH_DEFAULT_SETTINGS_MSG);
        Assert.assertEquals(withDrawJson3.getString("body.resultInfo.resultCode"),FETCH_DEFAULT_SETTINGS_CODE);
        Assert.assertEquals(withDrawJson3.getList("body.reminderDetails.daysAfterIssueDate"),daysAfterIssueDate);
        Assert.assertEquals(withDrawJson3.getList("body.reminderDetails.daysBeforeExpiry"),daysBeforeExpiry);
        Assert.assertEquals(withDrawJson3.getList("body.reminderDetails.channels"),channels);

    }

    @Owner("Himanshu Arora")
    @Test(dataProvider = "Dataset",description="verify successful FetchDefaultSettings link response for FIXED,INVOICE,GENERIC link when mid is null.")
    public void fetchDefaultSettings_02(String linkType) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,linkType , "200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        FetchDefaultSettings fetchDefaultSettings=new FetchDefaultSettings().buildRequest(null);
        JsonPath withDrawJson2 = fetchDefaultSettings.execute().jsonPath();
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultMessage"),FETCH_DEFAULT_SETTINGS_NULLMID);
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultCode"),FETCH_DEFAULT_SETTINGS_NULLMID_CODE);
    }

}
