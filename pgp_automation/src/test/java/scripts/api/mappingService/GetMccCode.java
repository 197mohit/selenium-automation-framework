package scripts.api.mappingService;

import com.paytm.api.MappingService.MccCodeDetail;
import com.paytm.appconstants.Constants;
import com.paytm.base.test.PGPBaseTest;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.SoftAssertions;
import org.testng.annotations.Test;


public class GetMccCode extends PGPBaseTest {

    @Owner("Anushka_Goldi")
    @Feature("PGP-38488")
    @Test(description = "To verify MCC Code in Response of API ")
    public void verifyingResultInfo()  {
        String mid = Constants.MerchantType.MCC_CODE_MERCH.getId();
        MccCodeDetail merchant = new MccCodeDetail("11000529", "PEDC");
        JsonPath withDrawJson = merchant.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(withDrawJson.getString("response.mccCode").equalsIgnoreCase("6012"));
        softly.assertAll();
    }

}
