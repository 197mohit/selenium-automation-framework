package scripts.notificationTemplate;

import com.paytm.api.MappingService.notificationTemplate.CreateEditEmailTemplate;
import com.paytm.api.MappingService.notificationTemplate.GetEmailTemplate;
import com.paytm.appconstants.Constants;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.utils.merchant.util.DbQueriesUtil;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Owner(Constants.Owner.PUSHKAL)
@Feature("PGP-35260")
public class TestEmailTemplate extends PGPBaseTest {

    String mid = Constants.MerchantType.AddnPay.getId();
    String id;
    String templateId;

    @Test
    public void createEmailTemplate() {
        CreateEditEmailTemplate createEmailTemplate = new CreateEditEmailTemplate(mid).editRequest("", "", "Y");
        Response response = createEmailTemplate.execute();
        String resultStatus = response.jsonPath().get("resultInfo.resultStatus");
        Assertions.assertThat(resultStatus).isEqualTo("S");
        id = response.jsonPath().getJsonObject("response.id").toString();
        templateId = response.jsonPath().getJsonObject("response.templateId").toString();
    }

    @Test (dependsOnMethods = "createEmailTemplate")
    public void getEmailTemplate() throws IOException {
        GetEmailTemplate getEmailTemplate = new GetEmailTemplate(mid);
        Response response = getEmailTemplate.execute();
        List<Map<String, String>> j = response.jsonPath().getList("response.emailTemplateDetails");
        String enabledTemplateQuery = "SELECT * from PGPDB.EMAIL_TEMPLATE_CONFIG WHERE MID="+"'"+mid+"'"+" AND ENABLED='Y'";
        List<Map<String, Object>> result = DbQueriesUtil.selectFromPGPDB(enabledTemplateQuery);
        Assertions.assertThat(j.size()).isEqualTo(result.size());
    }

    @Test (dependsOnMethods = "getEmailTemplate")
    public void editEmailTemplate() {
        CreateEditEmailTemplate editEmailTemplate = new CreateEditEmailTemplate(mid).editRequest(id, templateId, "N");
        Response response = editEmailTemplate.execute();
        String resultStatus = response.jsonPath().get("resultInfo.resultStatus");
        Integer responseId = response.jsonPath().get("response.id");
        Integer responseTemplateId = response.jsonPath().get("response.templateId");
        Assertions.assertThat(responseId.toString()).isEqualTo(id);
        Assertions.assertThat(responseTemplateId.toString()).isEqualTo(templateId);
        Assertions.assertThat(resultStatus).isEqualTo("S");
    }

    @Test
    public void createEmailTemplateWithoutMid() {
        CreateEditEmailTemplate createEmailTemplate = new CreateEditEmailTemplate("").editRequest("", "", "Y");
        Response responseJson = createEmailTemplate.execute();
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
    public void getEmailTemplateForInvalidMid() {
        GetEmailTemplate getEmailTemplate = new GetEmailTemplate("invalid");
        Response responseJson = getEmailTemplate.execute();
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
