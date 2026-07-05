package scripts.api.mappingService;

import com.paytm.api.MappingService.GETEmiDetails;
import com.paytm.api.MappingService.UpdateEMI;
import com.paytm.appconstants.Constants;
import com.paytm.base.test.PGPBaseTest;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.json.JSONObject;
import org.testng.annotations.Test;

public class GetEmiDetailsTest extends PGPBaseTest {

    @Owner("PUSPA")
    @Feature("PGP-38446")
    @Test(description = "Remove duplicate EMIs")
    public void verifyEMIDetails() {
           String mid = Constants.MerchantType.UNIQUE_EMI_PLANS.getId();
           UpdateEMI updateEMI = new UpdateEMI().buildRequest(mid,"ICICI","ICICI Bank");
           JsonPath withDrawJson = updateEMI.execute().jsonPath();
           Integer emiConfigObjectSize1 = new JSONObject(updateEMI.getRequest()).getJSONArray("emiConfigInfos").length();

           GETEmiDetails getEmiDetails = new GETEmiDetails(mid);
           String response = getEmiDetails.execute().jsonPath().prettify();
           Integer emiConfigObjectSize = new JSONObject(response).getJSONObject("response").getJSONArray("emiConfigInfos").length();
           Assertions.assertThat(emiConfigObjectSize1.equals(emiConfigObjectSize));


}

}
