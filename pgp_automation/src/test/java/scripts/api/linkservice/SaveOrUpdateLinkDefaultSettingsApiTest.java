package scripts.api.linkservice;

import com.paytm.api.linkAPI.CreateNewLink;
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

@Feature("PGP-40242")
public class SaveOrUpdateLinkDefaultSettingsApiTest extends PGPBaseTest {
    User user;
    String mid;
    void setUserAndMId(String merchant) throws Exception {
        user = userManager.getForRead(Label.LINK);
        mid = merchant;
    }
    ArrayList<Integer> daysAfterIssueDate=new ArrayList<Integer>();
    ArrayList<Integer> daysBeforeExpiry=new ArrayList<Integer>();
    ArrayList<String>  channels=new ArrayList<String>();

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
    @Test(dataProvider = "Dataset",description = "verify successful saveOrUpdateLinkDefaultSettingsApi link response for FIXED,GENERIC,INVOICE link.")
    public void saveDefault_01(String linkType) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, linkType, "200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        daysBeforeExpiry.clear();
        daysAfterIssueDate.clear();
        channels.clear();
        daysAfterIssueDate.add(1);
        daysBeforeExpiry.add(1);
        channels.add("SMS");
        SaveOrUpdateLinkDefaultSettingsApi saveOrUpdateLinkDefaultSettingsApi=new SaveOrUpdateLinkDefaultSettingsApi().buildRequest(mid,daysAfterIssueDate,daysBeforeExpiry,channels);
        JsonPath withDrawJson2 = saveOrUpdateLinkDefaultSettingsApi.execute().jsonPath();
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultMessage"),SAVE_DEFAULT_SETTINGS_MSG);
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultCode"),SAVE_DEFAULT_SETTINGS_CODE);
    }

    @Owner("Himanshu Arora")
    @Test(dataProvider = "Dataset",description = "verify saveOrUpdateLinkDefaultSettingsApi link response when mid is null for FIXED,Generic,Invoice link.")
    public void saveDefault_02(String linkType) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, linkType, "200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        daysBeforeExpiry.clear();
        daysAfterIssueDate.clear();
        channels.clear();
        daysAfterIssueDate.add(1);
        daysBeforeExpiry.add(1);
        channels.add("SMS");
        SaveOrUpdateLinkDefaultSettingsApi saveOrUpdateLinkDefaultSettingsApi=new SaveOrUpdateLinkDefaultSettingsApi().buildRequest(null,daysAfterIssueDate,daysBeforeExpiry,channels);
        JsonPath withDrawJson2 = saveOrUpdateLinkDefaultSettingsApi.execute().jsonPath();
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultMessage"),SAVE_DEFAULT_SETTINGS_MIDNULL);
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultCode"),SAVE_DEFAULT_SETTINGS_MIDNULL_CODE);
    }

    @Owner("Himanshu Arora")
    @Test(dataProvider = "Dataset",description = "verify saveOrUpdateLinkDefaultSettingsApi link response when reminder is null for FIXED,Generic,Invoice link.")
    public void saveDefault_03(String linkType) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, linkType, "200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        daysBeforeExpiry.clear();
        daysAfterIssueDate.clear();
        channels.clear();
        channels.add("SMS");
        SaveOrUpdateLinkDefaultSettingsApi saveOrUpdateLinkDefaultSettingsApi=new SaveOrUpdateLinkDefaultSettingsApi().buildRequest(mid,daysAfterIssueDate,daysBeforeExpiry,channels);
        JsonPath withDrawJson2 = saveOrUpdateLinkDefaultSettingsApi.execute().jsonPath();
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultMessage"),SAVE_DEFAULT_SETTINGS_REMINDER_NULL);
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultCode"),SAVE_DEFAULT_SETTINGS_REMINDER_NULL_CODE);
    }

    @Owner("Himanshu Arora")
    @Test(dataProvider = "Dataset",description = "verify saveOrUpdateLinkDefaultSettingsApi link response when channel is null for FIXED,Generic,Invoice link.")
    public void saveDefault_04(String linkType) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, linkType, "200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        daysBeforeExpiry.clear();
        daysAfterIssueDate.clear();
        channels.clear();
        daysAfterIssueDate.add(1);
        daysBeforeExpiry.add(1);
        SaveOrUpdateLinkDefaultSettingsApi saveOrUpdateLinkDefaultSettingsApi=new SaveOrUpdateLinkDefaultSettingsApi().buildRequest(mid,daysAfterIssueDate,daysBeforeExpiry,channels);
        JsonPath withDrawJson2 = saveOrUpdateLinkDefaultSettingsApi.execute().jsonPath();
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultMessage"),SAVE_DEFAULT_SETTINGS_CHANNEL_NULL);
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultCode"),SAVE_DEFAULT_SETTINGS_CHANNEL_NULL_CODE);
    }

    @Owner("Himanshu Arora")
    @Test(description = "verify successful archive link response for PAYMENT_BUTTON link.")
    public void saveDefault_05() throws Exception {
        setUserAndMId(Constants.MerchantType.ENABLE_DISABLE_PAYMENT_BUTTON.getId().toString());
        CreateNewLink createNewLinkPaymentBtn = new CreateNewLink().buildRequest(mid,"PAYMENT_BUTTON","200");
        createNewLinkPaymentBtn.deleteContext("body.sendSms");
        createNewLinkPaymentBtn.deleteContext("body.sendEmail");
        createNewLinkPaymentBtn.deleteContext("body.customerContact");
        JsonPath withDrawJson1 = createNewLinkPaymentBtn.execute().jsonPath();
        daysBeforeExpiry.clear();
        daysAfterIssueDate.clear();
        channels.clear();
        daysAfterIssueDate.add(1);
        daysBeforeExpiry.add(1);
        channels.add("SMS");
        SaveOrUpdateLinkDefaultSettingsApi saveOrUpdateLinkDefaultSettingsApi=new SaveOrUpdateLinkDefaultSettingsApi().buildRequest(mid,daysAfterIssueDate,daysBeforeExpiry,channels);
        JsonPath withDrawJson2 = saveOrUpdateLinkDefaultSettingsApi.execute().jsonPath();
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultMessage"),SAVE_DEFAULT_SETTINGS_MSG);
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultCode"),SAVE_DEFAULT_SETTINGS_CODE);
    }

    @Owner("Himanshu Arora")
    @Test(dataProvider = "Dataset",description = "verify successful saveOrUpdateLinkDefaultSettingsApi link response for FIXED,GENERIC,INVOICE link.")
    public void saveDefault_06(String linkType) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, linkType, "200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        daysBeforeExpiry.clear();
        daysAfterIssueDate.clear();
        channels.clear();
        SaveOrUpdateLinkDefaultSettingsApi saveOrUpdateLinkDefaultSettingsApi=new SaveOrUpdateLinkDefaultSettingsApi().buildRequest(mid,daysAfterIssueDate,daysBeforeExpiry,channels);
        JsonPath withDrawJson2 = saveOrUpdateLinkDefaultSettingsApi.execute().jsonPath();
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultMessage"),SAVE_DEFAULT_SETTINGS_REMINDER_NULL);
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultCode"),SAVE_DEFAULT_SETTINGS_REMINDER_NULL_CODE);
    }
}
