package scripts.api.mappingService.migrationDetailsInfo;

import com.paytm.api.MappingService.ExtendedInfoDetail;
import com.paytm.api.MappingService.GetAlipayIdDetail;
import com.paytm.api.MappingService.V1GatewayDetail;
import com.paytm.appconstants.Constants;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.framework.reportportal.annotation.Owner;

import io.qameta.allure.Feature;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.SoftAssertions;
import org.testng.annotations.Test;

@Owner("Anushka")
@Feature("PGP-37785")
public class GetExtendedInfoTest extends PGPBaseTest {

    @Test(description = "To verify Response of ExtendedInfo API")
    public void verifyingResultInfo()  {
        String mid = Constants.MerchantType.MIGRATIONDETAIL.getId();
        ExtendedInfoDetail merchant = new ExtendedInfoDetail(mid);
        JsonPath withDrawJson = merchant.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(withDrawJson.getString("resultInfo.resultCode")).isEqualTo("00000");
        softly.assertThat(withDrawJson.getString("resultInfo.resultStatus")).isEqualTo("S");
        softly.assertThat(withDrawJson.getString("resultInfo.messaage")).isEqualTo("Success");
        softly.assertAll();
    }

    @Test(description = "To verify MID of ExtendedInfo API")
    public void verifyingMIDExtendedInfo()  {
        String mid = Constants.MerchantType.MIGRATIONDETAIL.getId();
        ExtendedInfoDetail merchant = new ExtendedInfoDetail(mid);
        JsonPath withDrawJson = merchant.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(withDrawJson.getString("merchantId").isEmpty()).isFalse();
        softly.assertAll();
    }

    @Test(description = "To verify ExtendedInfo API")
    public void verifyingExtendedInfo()  {
        String mid = Constants.MerchantType.MIGRATIONDETAIL.getId();
        ExtendedInfoDetail merchant = new ExtendedInfoDetail(mid);
        JsonPath withDrawJson = merchant.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(withDrawJson.getString("extendedInfo.status")).isEqualTo("ACTIVE");
        softly.assertThat(withDrawJson.getString("extendedInfo.keySize")).isNotEmpty();
        softly.assertAll();
    }

    @Owner("Himanshu")
    @Test(description = "To verify Response of AlipayId API")
    public void verifyingALipayIdAPI()  {
        String mid= Constants.MerchantType.MIGRATIONDETAIL.getId();
        GetAlipayIdDetail merchant = new GetAlipayIdDetail(mid);
        JsonPath withDrawJson = merchant.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(withDrawJson.getString("alipayId")).isNotNull();
        softly.assertThat(withDrawJson.getString("paytmId")).isNotNull();
        softly.assertAll();
    }

    @Owner("Himanshu")
    @Test(description = "To verify Response of V1_GATEWAY")
    public void verifyingV1GATEWAYAPI()  {
        V1GatewayDetail addMerchant = new V1GatewayDetail();
        JsonPath withDrawJson = addMerchant.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(withDrawJson.getString("restStatus")).isEqualTo("SUCCESS");
        softly.assertThat(withDrawJson.getString("response.resultCode")).isEqualTo("00000");
        softly.assertThat(withDrawJson.getString("response.resultStatus")).isEqualTo("S");
        softly.assertThat(withDrawJson.getString("response.messaage")).isEqualTo("Success");
        int s=withDrawJson.getList("resultResp").size();
        for(int i=0;i<s;i++)
        {
            softly.assertThat(withDrawJson.getString("resultResp[\" + i + \"].gatewayName"));
            softly.assertThat(withDrawJson.getString("resultResp[\" + i + \"].gatewayDisplayName"));
            softly.assertThat(withDrawJson.getString("resultResp[\" + i + \"].logoUrl"));
            int s1=withDrawJson.getList("resultResp[\" + i + \"].payMethods").size();
            for(int j=0;j<s1;j++)
            {
                softly.assertThat(withDrawJson.getString("resultResp[\" + i + \"].payMethods[\" + j + \"]")).isNotNull();
            }
        }
        softly.assertAll();
    }
}
