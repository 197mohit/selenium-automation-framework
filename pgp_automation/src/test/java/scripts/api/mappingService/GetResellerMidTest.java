package scripts.api.mappingService;

import com.paytm.api.MappingService.GetResellerMidDetail;
import com.paytm.appconstants.Constants;
import com.paytm.base.test.PGPBaseTest;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.SoftAssertions;
import org.testng.annotations.Test;

public class GetResellerMidTest extends PGPBaseTest {
    @Owner("Anushka_Goldi")
    @Feature("PGP-39099")
    @Test(description = "To verify Result in Response of API ")
    public void verifyingResultInfo()  {
        String mid = Constants.MerchantType.RESELLER_MID.getId();
        GetResellerMidDetail merchant = new GetResellerMidDetail(mid);
        JsonPath withDrawJson = merchant.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(withDrawJson.getString("resultInfo.messaage").equalsIgnoreCase("Success"));
        softly.assertAll();
    }
}
