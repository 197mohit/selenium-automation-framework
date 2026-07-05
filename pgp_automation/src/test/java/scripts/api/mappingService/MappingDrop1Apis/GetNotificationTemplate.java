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


public class GetNotificationTemplate extends PGPBaseTest {

    @Owner("Anushka Goldi")
    @Feature("PGP-45207")
    @Test(description = "Verify Successfull response of notificationTemplateInfos in NotificationGetNotificationTemplate")
    void GetNotificationTemplate_01() {
        MappingApisPG2 mappingApisPG2 = new MappingApisPG2();
        mappingApisPG2.Get_Notification_Template();
        mappingApisPG2.SetGetNotificationTemplate("SMS", "PAYMENT", "MERCHANT", "OFFLINE_MERCHANT_SMS_PRINTER", "SUCCESS");
        JsonPath withDrawJson1 = mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper = new PG2MappingApisHelper();
        Map<String, String> objectHead = withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyGetNotificationTemplate(objectHead);
        Assert.assertNotNull(withDrawJson1.getString("notificationTemplateInfos"));
    }

    @Owner("Anushka Goldi")
    @Feature("PGP-45207")
    @Test(description = "Verify Successfull response of notificationTemplateInfos in NotificationGetNotificationTemplate")
    void GetNotificationTemplate_02(){
        MappingApisPG2 mappingApisPG2 = new MappingApisPG2();
        mappingApisPG2.Get_Notification_Template();
        mappingApisPG2.SetGetNotificationTemplate("SMS", "PAYMENT", "MERCHANT", "OFFLINE_MERCHANT_SMS_PRINTER", "SUCCESS");
        JsonPath withDrawJson1 = mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper = new PG2MappingApisHelper();
        Map<String, String> objectHead = withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyGetNotificationTemplate(objectHead);
        int s= withDrawJson1.getList("notificationTemplateInfos").size();
        if(s==0){
            Assert.fail("NotificationTemplateInfos is Empty");
        }
        Assert.assertEquals(withDrawJson1.getString("notificationTemplateInfos["+ 0 +"].templateId"), "263");
        Assert.assertEquals(withDrawJson1.getString("notificationTemplateInfos["+ 0 +"].templateName"), "PAYMENT_OFFLINE_MERCHANT_SMS_PRINTER_SUCCESS_SMS");
        Assert.assertEquals(withDrawJson1.getString("notificationTemplateInfos["+ 0 +"].notificationType"), "SMS");
        Assert.assertEquals(withDrawJson1.getString("notificationTemplateInfos["+ 0 +"].serviceType"), "PAYMENT");
        Assert.assertEquals(withDrawJson1.getString("notificationTemplateInfos["+ 0 +"].recipient"), "MERCHANT");
        Assert.assertEquals(withDrawJson1.getString("notificationTemplateInfos["+ 0 +"].category"), "OFFLINE_MERCHANT_SMS_PRINTER");
        Assert.assertEquals(withDrawJson1.getString("notificationTemplateInfos["+ 0 +"].txnStatus"), "SUCCESS");
        Assert.assertNull(withDrawJson1.getString("notificationTemplateInfos["+ 0 +"].paymode"));
        Assert.assertNull(withDrawJson1.getString("notificationTemplateInfos["+ 0 +"].bankCode"));
        Assert.assertNull(withDrawJson1.getString("notificationTemplateInfos["+ 0 +"].instErrorCode"));
        Assert.assertNull(withDrawJson1.getString("notificationTemplateInfos["+ 0 +"].mid"));
        Assert.assertNull(withDrawJson1.getString("notificationTemplateInfos["+ 0 +"].params"));
        Assert.assertNull(withDrawJson1.getString("notificationTemplateInfos["+ 0 +"].dltTemplateId"));
        Assert.assertEquals(withDrawJson1.getString("notificationTemplateInfos["+ 0 +"].dltEntityId"), "1501601290000011395");
        Assert.assertEquals(withDrawJson1.getString("notificationTemplateInfos["+ 0 +"].sender"), "iPaytm");
        Assert.assertNull(withDrawJson1.getString("notificationTemplateInfos["+ 0 +"].templateHeader"));
        Assert.assertEquals(withDrawJson1.getString("notificationTemplateInfos["+ 0 +"].templateBody"), "#if(${currentTxnCount}) Count#${currentTxnCount}:Rs ${TXN_AMOUNT} paid to ${MERCHANT_NAME} by ${CUSTOMER_DETAILS} at ${TXN_DATE_TIME}. OrderId ${Order_Id}. For concerns, call:0120-4440440 #else Rs ${TXN_AMOUNT} paid to ${MERCHANT_NAME} by ${CUSTOMER_DETAILS} at ${TXN_DATE_TIME}. OrderId ${Order_Id}. For concerns, call:0120-4440440 #end");
        Assert.assertNull(withDrawJson1.getString("notificationTemplateInfos["+ 0 +"].extendedTemplateBody"));
        Assert.assertNull(withDrawJson1.getString("notificationTemplateInfos["+ 0 +"].templateDescription"), "");
        Assert.assertEquals(withDrawJson1.getString("notificationTemplateInfos["+ 0 +"].isEnabled"), "Y");
    }
}

