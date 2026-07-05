package scripts.api.theia;

import com.paytm.api.FetchUPIOptions;
import com.paytm.appconstants.Constants;
import com.paytm.base.test.PGPBaseTest;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

@Owner(Constants.Owner.VIDHI)
@Feature("PGP-30172")
public class FetchUPIOptionsTests extends PGPBaseTest {

    @Test(description = "Verify success response of FetchUPI PSP  Options API")
    public void FetchSuccessUPIPSPOptions_validChecksum() throws Exception
    {
        Constants.MerchantType m = Constants.MerchantType.NATIVE_ADDNPAY;

        FetchUPIOptions api = new FetchUPIOptions(m);
        JsonPath js=api.execute().jsonPath();
        Assertions.assertThat(js.getString("head.version")).isEqualTo("v1");
        Assertions.assertThat(js.getString("body.extraParamsMap")).isEqualTo(null);
        Assertions.assertThat(js.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(js.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(js.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(js.getString("body.upiPspOptions.name[0]")).isEqualTo("paytm");
        Assertions.assertThat(js.getString("body.upiPspOptions.iconUrl[0]")).contains("/native/psp/paytm.png");
        Assertions.assertThat(js.getString("body.upiPspOptions.name[1]")).isEqualTo("phonepe");
        Assertions.assertThat(js.getString("body.upiPspOptions.iconUrl[1]")).contains("/native/psp/phonepe.png");
        Assertions.assertThat(js.getString("body.upiPspOptions.name[2]")).isEqualTo("googlepay");
        Assertions.assertThat(js.getString("body.upiPspOptions.iconUrl[2]")).contains("/native/psp/googlepay.png");
        Assertions.assertThat(js.getString("body.upiPspOptions.name[3]")).isEqualTo("bhimupi");
       Assertions.assertThat(js.getString("body.upiPspOptions.iconUrl[3]")).contains("/native/psp/bhimupi.png");
    }

    @Test(description = "Verify FetchUPI PSP Options API response when invalid checksum is passed")

    public void FetchSUPIPSPOptionsResponse_invalidChecksum() throws Exception
    {
        Constants.MerchantType m =Constants.MerchantType.NATIVE_ADDNPAY;

        FetchUPIOptions api = new FetchUPIOptions(m,"abcdefgh");
        JsonPath js=api.execute().jsonPath();
        Assertions.assertThat(js.getString("head.version")).isEqualTo("v1");
        Assertions.assertThat(js.getString("body.extraParamsMap")).isEqualTo(null);
        Assertions.assertThat(js.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(js.getString("body.resultInfo.resultCode")).isEqualTo("2005");
        Assertions.assertThat(js.getString("body.resultInfo.resultMsg")).isEqualTo("Checksum provided is invalid");
       }
}

