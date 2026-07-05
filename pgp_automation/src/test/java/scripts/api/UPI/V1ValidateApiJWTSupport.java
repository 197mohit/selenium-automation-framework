package scripts.api.UPI;

import com.paytm.api.V1ValidateVpa;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.appconstants.Constants;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import static com.paytm.appconstants.Constants.Owner.HIMANSHU;

public class V1ValidateApiJWTSupport
{
    @Owner(HIMANSHU)
    @Feature("PGP-56497")
    @Parameters({"isNativePlus"})
    @Test(description = "Check if v1/validatevpa api is giving payer name if JTW Token is passed in api call",groups = "Security")
    public void v1ValidateVpa_withJWT(@Optional("false") Boolean isNativePlus) throws Exception
    {
        Constants.MerchantType merchantType= Constants.MerchantType.PG2_CC_DC;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", merchantType).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String vpa="test@paytm";
        V1ValidateVpa v1ValidateVpa = new V1ValidateVpa().buildRequestwithJWT(vpa,initTxnDTO.orderFromBody(),merchantType.getId(),initTxnResponse.getBody().getTxnToken());
        JsonPath v1ValidateVpaResponse = v1ValidateVpa.execute().jsonPath();
        Assertions.assertThat(v1ValidateVpaResponse.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("S");
        Assertions.assertThat(v1ValidateVpaResponse.getString("body.resultInfo.resultCode")).isEqualToIgnoringCase("0000");
        Assertions.assertThat(v1ValidateVpaResponse.getString("body.resultInfo.resultMsg")).isEqualToIgnoringCase("Success");
        Assertions.assertThat(v1ValidateVpaResponse.getString("body.valid")).isEqualToIgnoringCase("true");
        Assertions.assertThat(v1ValidateVpaResponse.getString("body.vpa")).isEqualToIgnoringCase("test@paytm");
        Assertions.assertThat(v1ValidateVpaResponse.getString("body.payerName")).isEqualToIgnoringCase("Test User");

    }

    @Owner(HIMANSHU)
    @Feature("PGP-56497")
    @Parameters({"isNativePlus"})
    @Test(description = "Check if v1/validatevpa api isn't giving payer name if JTW Token isn't passed in api call",groups = "Security")
    public void v1ValidateVpa_withoutJWT(@Optional("false") Boolean isNativePlus) throws Exception
    {
        Constants.MerchantType merchantType= Constants.MerchantType.PG2_CC_DC;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", merchantType).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String vpa="test@paytm";
        V1ValidateVpa v1ValidateVpa = new V1ValidateVpa().buildRequest("TXN_TOKEN",initTxnResponse.getBody().getTxnToken(), vpa,initTxnDTO.orderFromBody(),null,
                merchantType.getId(), null);
        JsonPath v1ValidateVpaResponse = v1ValidateVpa.execute().jsonPath();
        Assertions.assertThat(v1ValidateVpaResponse.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("S");
        Assertions.assertThat(v1ValidateVpaResponse.getString("body.resultInfo.resultCode")).isEqualToIgnoringCase("0000");
        Assertions.assertThat(v1ValidateVpaResponse.getString("body.resultInfo.resultMsg")).isEqualToIgnoringCase("Success");
        Assertions.assertThat(v1ValidateVpaResponse.getString("body.valid")).isEqualToIgnoringCase("true");
        Assertions.assertThat(v1ValidateVpaResponse.getString("body.vpa")).isEqualToIgnoringCase("test@paytm");
        Assertions.assertThat(v1ValidateVpaResponse.getString("body")).doesNotContain("payerName");
    }
}
