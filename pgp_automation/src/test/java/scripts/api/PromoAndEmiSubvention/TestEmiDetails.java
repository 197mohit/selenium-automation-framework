package scripts.api.PromoAndEmiSubvention;

import com.paytm.LocalConfig;

import static com.paytm.appconstants.Constants.Owner.SATWIK_SHARMA;
import static org.hamcrest.Matchers.*;
import com.paytm.api.nativeAPI.GetEMIDetails;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.dto.NativeDTO.getEMIDetails.request.GetEMIDetailsRequest;

import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;


public class TestEmiDetails extends PGPBaseTest {


    @Test
    @Owner(SATWIK_SHARMA)
    @Feature("PGP-56308.xml")
    public void testGetEmiDetailsDynamically() throws Exception {
        String mid = "qa12FU97229952596781";
        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("mid", mid);
        String signature = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.subvention, LocalConfig.JWT_EMI_KEY);
        GetEMIDetailsRequest getEMIDetailsRequest = new GetEMIDetailsRequest(signature, mid);
        Response response = new GetEMIDetails(getEMIDetailsRequest, mid).execute();

        performAssertions(response) ;
    }


    public  void performAssertions(Response response) throws Exception {
        response.then().assertThat()
                .body("body.emiDetails.channelCode", hasItems("ICICI", "HDFC"))
                .body("body.emiDetails.channelName", hasItems("ICICI Bank Credit Card", "HDFC Bank Credit Card", "ICICI Bank Debit Card"))
                .body("body.emiDetails.emiType", hasItems("CREDIT_CARD", "DEBIT_CARD"))
                .body("body.emiDetails.iconUrl", hasItems(containsString("https://staticgw.paytm.in/native/bank/")));

// Assert each value for emiChannelInfos within emiDetails
        response.then().assertThat()
                .body("body.emiDetails.emiChannelInfos.emiId", everyItem(notNullValue()))
                .body("body.emiDetails.emiChannelInfos.planId", everyItem(notNullValue()))
                .body("body.emiDetails.emiChannelInfos.interestRate", everyItem(notNullValue()))
                .body("body.emiDetails.emiChannelInfos.ofMonths", everyItem(notNullValue()))
                .body("body.emiDetails.emiChannelInfos.minAmount.value", everyItem(notNullValue()))
                .body("body.emiDetails.emiChannelInfos.maxAmount.value", everyItem(notNullValue()))
                .body("body.emiDetails.multiItemEmiSupported", everyItem(is(true)));
// Assert the resultInfo section
        response.then().assertThat()
                .body("body.resultInfo.resultStatus", equalTo("S"))
                .body("body.resultInfo.resultCode", equalTo("0000"))
                .body("body.resultInfo.resultMsg", equalTo("Success"));
    }
}
