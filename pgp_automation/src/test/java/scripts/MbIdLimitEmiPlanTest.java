package scripts;

import com.paytm.ServerConfigProvider;
import com.paytm.api.MbIdLimitEmiPlan;
import com.paytm.api.RedisAPI;
import com.paytm.appconstants.Constants;
import com.paytm.base.test.PGPBaseTest;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

import static com.paytm.appconstants.Constants.Owner.ABHISHEK_KULKARNI;
import static com.paytm.apphelpers.LogsValidationHelper.getLogsOnServer;


@Owner(ABHISHEK_KULKARNI)
public class MbIdLimitEmiPlanTest extends PGPBaseTest {

    @Feature("PGP-39868")
    @Test(description = "Validate mbidlimit emiplan details")
    public void validateMbIdLimitEmiPlanDetails() throws Exception {
        Constants.MerchantType emiMid = Constants.MerchantType.EMI;
        MbIdLimitEmiPlan mbIdLimitEmiPlan = new MbIdLimitEmiPlan(emiMid.getId());
        JsonPath mbIdLimitPlanResponse = mbIdLimitEmiPlan.execute().jsonPath();
        Assertions.assertThat(mbIdLimitPlanResponse.getString("emiDetails")).as("emi details are empty in mbidlimit emiplan details api response").isNotEmpty();
    }

    @Feature("PGP-39868")
    @Test(description = "Validate mbidlimit emiplan details fetch from cache when we hit same request second time")
    public void validateMbIdLimitEmiPlanDetailsFromCache() throws Exception {
        Constants.MerchantType emiMid = Constants.MerchantType.EMIOnly;
        RedisAPI.deleteKey("EMI_DETAILS_" + emiMid.getId() + "_true_WAP");
        MbIdLimitEmiPlan mbIdLimitEmiPlan = new MbIdLimitEmiPlan(emiMid.getId());
        JsonPath mbIdLimitPlanResponse = mbIdLimitEmiPlan.execute().jsonPath();
        Assertions.assertThat(mbIdLimitPlanResponse.getString("emiDetails")).as("emi details are empty in mbidlimit emiplan details api response").isNotEmpty();
        // Hitting same request second time
        mbIdLimitEmiPlan = new MbIdLimitEmiPlan(emiMid.getId());
        mbIdLimitPlanResponse = mbIdLimitEmiPlan.execute().jsonPath();
        Assertions.assertThat(mbIdLimitPlanResponse.getString("emiDetails")).as("emi details are empty in mbidlimit emiplan details api response").isNotEmpty();
        // Validating mapping service logs
        String grepcmd = "grep \"" + "com.paytm.pgplus.mapping.service.rest.helper.MappingServiceHelper.getEmiDetailsByMid()" + "\" /paytm/logs/mapping-service.log | " + "grep \"" + emiMid.getId() + "\"";
        String logs = getLogsOnServer(ServerConfigProvider.SERVICE.MAPPING_SERVICE, grepcmd);
        Assertions.assertThat(logs).contains("EMI details found in cache for key: EMI_DETAILS_" + emiMid.getId() + "_true_WAP");
    }
}