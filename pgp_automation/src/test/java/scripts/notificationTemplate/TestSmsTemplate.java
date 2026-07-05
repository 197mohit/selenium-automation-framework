package scripts.notificationTemplate;

import com.paytm.api.MappingService.notificationTemplate.CreateEditSmsTemplate;
import com.paytm.api.MappingService.notificationTemplate.GetSmsTemplate;
import com.paytm.appconstants.Constants;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.utils.merchant.util.DbQueriesUtil;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.json.simple.JSONObject;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

@Owner(Constants.Owner.PUSHKAL)
@Feature("PGP-35260")
public class TestSmsTemplate extends PGPBaseTest {
    String mid = Constants.MerchantType.AddnPay.getId();
    String id;
    String templateId;

    @Test
    public void createSmsTemplate() {
        CreateEditSmsTemplate createSmsTemplate = new CreateEditSmsTemplate(mid).editRequest("", "", "Y");
        Response response = createSmsTemplate.execute();
        String resultStatus = response.jsonPath().get("resultInfo.resultStatus");
        Assertions.assertThat(resultStatus).isEqualTo("S");
        id = response.jsonPath().getJsonObject("response.id").toString();
        templateId = response.jsonPath().getJsonObject("response.templateId").toString();

    }

    @Test (dependsOnMethods = "createSmsTemplate")
    public void getSmsTemplate() {
        GetSmsTemplate getSmsTemplate = new GetSmsTemplate(mid);
        Response response = getSmsTemplate.execute();
        List<JSONObject> j = response.jsonPath().getJsonObject("response.smsTemplateDetails");
        String enabledTemplateQuery = "SELECT * from PGPDB.NOTIFICATION_TEMPLATE_CONFIG WHERE MID="+"'"+mid+"'"+" AND ENABLED='Y'";
        List<Map<String, Object>> result = DbQueriesUtil.selectFromPGPDB(enabledTemplateQuery);
        Assertions.assertThat(j.size()).isEqualTo(result.size());
    }

    @Test (dependsOnMethods = "getSmsTemplate")
    public void editSmsTemplate() {
        CreateEditSmsTemplate editSmsTemplate = new CreateEditSmsTemplate(mid).editRequest(id, templateId, "N");
        Response response = editSmsTemplate.execute();
        System.out.println(response.getBody().asString());
        String resultStatus = response.jsonPath().get("resultInfo.resultStatus");
        Integer responseId = response.jsonPath().get("response.id");
        Integer responseTemplateId = response.jsonPath().get("response.templateId");
        Assertions.assertThat(responseId.toString()).isEqualTo(id);
        Assertions.assertThat(responseTemplateId.toString()).isEqualTo(templateId);
        Assertions.assertThat(resultStatus).isEqualTo("S");
    }

    @Test
    public void createSmsTemplateWithoutMid() {
        CreateEditSmsTemplate createSmsTemplate = new CreateEditSmsTemplate("").editRequest("", "", "Y");
        Response responseJson = createSmsTemplate.execute();
        String resultStatus = responseJson.jsonPath().get("resultInfo.resultStatus");
        String resultCode = responseJson.jsonPath().get("resultInfo.resultCode");
        String message = responseJson.jsonPath().get("resultInfo.messaage");
        String response = responseJson.jsonPath().get("response");
        Assertions.assertThat(resultStatus).isEqualTo("F");
        Assertions.assertThat(resultCode).isEqualTo("00003");
        Assertions.assertThat(message).isEqualTo("One or more mandatory parameters is/are missing");
        Assertions.assertThat(response).isNull();
    }

    @Test
    public void getSmsTemplateForInvalidMid() {
        GetSmsTemplate getSmsTemplate = new GetSmsTemplate("invalid");
        Response responseJson = getSmsTemplate.execute();
        String resultStatus = responseJson.jsonPath().get("resultInfo.resultStatus");
        String resultCode = responseJson.jsonPath().get("resultInfo.resultCode");
        String message = responseJson.jsonPath().get("resultInfo.messaage");
        String response = responseJson.jsonPath().get("response");
        Assertions.assertThat(resultStatus).isEqualTo("F");
        Assertions.assertThat(resultCode).isEqualTo("00001");
        Assertions.assertThat(message).isEqualTo("Entry is not available ");
        Assertions.assertThat(response).isNull();
    }
}
