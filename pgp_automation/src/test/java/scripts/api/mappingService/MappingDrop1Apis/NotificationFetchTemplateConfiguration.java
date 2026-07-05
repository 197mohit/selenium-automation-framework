package scripts.api.mappingService.MappingDrop1Apis;

import com.paytm.api.MappingApisPG2;
import com.paytm.api.PG2MappingApisHelper;
import com.paytm.base.test.PGPBaseTest;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

public class NotificationFetchTemplateConfiguration extends PGPBaseTest {

    @Owner("Anushka Goldi")
    @Feature("PGP-45207")
    @Test(description = "Verify response of NotificationFetchTemplateConfiguration API ")
    void verifyNotificationFetchTemplateConfiguration_01() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Notification_Fetch_Template_Configuration();
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyNotificationFetchTemplateConfiguration(objectHead);
        Assert.assertNotNull(withDrawJson1.getString("response.emailConfig"));
        Assert.assertNotNull(withDrawJson1.getString("response.smsConfig"));
    }

    @Owner("Anushka Goldi")
    @Feature("PGP-45207")
    @Test(description = "Verify resultinfo of NotificationFetchTemplateConfiguration API ")
    void verifyNotificationFetchTemplateConfiguration_02() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Notification_Fetch_Template_Configuration();
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyNotificationFetchTemplateConfiguration(objectHead);
        Assert.assertEquals(withDrawJson1.getString("resultInfo.resultCode"), "00000");
        Assert.assertEquals(withDrawJson1.getString("resultInfo.resultStatus"), "S");
        Assert.assertEquals(withDrawJson1.getString("resultInfo.messaage"), "Success");
    }

    @Owner("Anushka Goldi")
    @Feature("PGP-45207")
    @Test(description = "Verify response of emailConfig in NotificationFetchTemplateConfiguration API ")
    void verifyNotificationFetchTemplateConfiguration_03() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Notification_Fetch_Template_Configuration();
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyNotificationFetchTemplateConfiguration(objectHead);
        Assert.assertNotNull(withDrawJson1.getString("response.emailConfig.recipient"));
        Assert.assertNotNull(withDrawJson1.getString("response.emailConfig.serviceType"));
        Assert.assertNotNull(withDrawJson1.getString("response.emailConfig.category"));
        Assert.assertNotNull(withDrawJson1.getString("response.emailConfig.status"));
        Assert.assertNotNull(withDrawJson1.getString("response.emailConfig.bankCode"));
        Assert.assertNotNull(withDrawJson1.getString("response.emailConfig.errorCode"));
        Assert.assertNotNull(withDrawJson1.getString("response.emailConfig.payMode"));
        Assert.assertNotNull(withDrawJson1.getString("response.emailConfig.enabled"));
    }

    @Owner("Anushka")
    @Test(description = "Verify response of smsConfig in NotificationFetchTemplateConfiguration API ")
    void verifyNotificationFetchTemplateConfiguration_04() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Notification_Fetch_Template_Configuration();
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyNotificationFetchTemplateConfiguration(objectHead);
        Assert.assertNotNull(withDrawJson1.getString("response.smsConfig.recipient"));
        Assert.assertNotNull(withDrawJson1.getString("response.smsConfig.serviceType"));
        Assert.assertNotNull(withDrawJson1.getString("response.smsConfig.category"));
        Assert.assertNotNull(withDrawJson1.getString("response.smsConfig.txnStatus"));
        Assert.assertNotNull(withDrawJson1.getString("response.smsConfig.notificationType"));
        Assert.assertNotNull(withDrawJson1.getString("response.smsConfig.bankCode"));
        Assert.assertNotNull(withDrawJson1.getString("response.smsConfig.instErrorCode"));
        Assert.assertNotNull(withDrawJson1.getString("response.smsConfig.payMode"));
        Assert.assertNotNull(withDrawJson1.getString("response.smsConfig.enabled"));
    }
}
