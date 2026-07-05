package scripts.notificationTemplate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.api.MappingService.notificationTemplate.GetTemplateGlobalConfiguration;
import com.paytm.appconstants.Constants;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.utils.merchant.util.DbQueriesUtil;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.response.Response;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Owner(Constants.Owner.PUSHKAL)
@Feature("PGP-35260")
public class TestGetNotificationGlobalConfig extends PGPBaseTest {
    SoftAssert softAssert = new SoftAssert();

    @Test
    public void getNotificationGlobalConfig() throws IOException {
        GetTemplateGlobalConfiguration getTemplateGlobalConfiguration = new GetTemplateGlobalConfiguration();
        Response response = getTemplateGlobalConfiguration.execute();
        Map<String, String> emailMap = response.jsonPath().getMap("response.emailConfig");
        Map<String, String> smsMap = response.jsonPath().getMap("response.smsConfig");
        String queryForEmailConfig = "SELECT * FROM PGPDB.global_configuration gc WHERE `key` = 'EMAIL_CONFIG'";
        String queryForSmsConfig = "SELECT * FROM PGPDB.global_configuration gc WHERE `key` = 'SMS_CONFIG'";
        String dbEmailConfig = DbQueriesUtil.selectFromPGPDB(queryForEmailConfig, "value").trim();
        String dbSmsConfig = DbQueriesUtil.selectFromPGPDB(queryForSmsConfig, "value").trim();
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, List<String>> dbEmailMap = objectMapper.readValue(dbEmailConfig, new TypeReference<Map<String, List<String>>>() {});
        Map<String, List<String>> dbSmsMap = objectMapper.readValue(dbSmsConfig, new TypeReference<Map<String, List<String>>>() {});

        softAssert.assertEquals(emailMap.get("recipient"), dbEmailMap.get("recipient"));
        softAssert.assertEquals(emailMap.get("serviceType"), dbEmailMap.get("serviceType"));
        softAssert.assertEquals(emailMap.get("category"), dbEmailMap.get("category"));
        softAssert.assertEquals(emailMap.get("status"), dbEmailMap.get("status"));
        softAssert.assertEquals(emailMap.get("bankCode"), dbEmailMap.get("bankCode"));
        softAssert.assertEquals(emailMap.get("errorCode"), dbEmailMap.get("errorCode"));
        softAssert.assertEquals(emailMap.get("paymode"), dbEmailMap.get("paymode"));
        softAssert.assertEquals(emailMap.get("enabled"), dbEmailMap.get("enabled"));

        softAssert.assertEquals(smsMap.get("recipient"), dbSmsMap.get("recipient"));
        softAssert.assertEquals(smsMap.get("serviceType"), dbSmsMap.get("serviceType"));
        softAssert.assertEquals(smsMap.get("category"), dbSmsMap.get("category"));
        softAssert.assertEquals(smsMap.get("txnStatus"), dbSmsMap.get("txnStatus"));
        softAssert.assertEquals(smsMap.get("notificationType"), dbSmsMap.get("notificationType"));
        softAssert.assertEquals(smsMap.get("bankCode"), dbSmsMap.get("bankCode"));
        softAssert.assertEquals(smsMap.get("instErrorCode"), dbSmsMap.get("instErrorCode"));
        softAssert.assertEquals(smsMap.get("paymode"), dbSmsMap.get("paymode"));
        softAssert.assertEquals(smsMap.get("enabled"), dbSmsMap.get("enabled"));

        softAssert.assertAll();
    }
}
