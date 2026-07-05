package scripts.api.theia;

import com.paytm.api.FetchPspApps;
import com.paytm.appconstants.Constants;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

public class FetchPspAppsTests extends PGPBaseTest {

    @Owner(Constants.Owner.VIDHI)
    @Feature("PGP-53182")
    @Test(description = "Verify success response of FetchUPI PSP  Options API when SSO token is passed")
    public void FetchSuccessUPIPSPOptions_validSsoToken() throws Exception {

        Constants.MerchantType merchantType = Constants.MerchantType.Deferred_Subs_MID;
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String sso = user.ssoToken().toString();
        FetchPspApps fetchPspApps = new FetchPspApps(merchantType, sso);
        Response response = fetchPspApps.execute();
        System.out.println(response);
        JsonPath jsonPath=response.jsonPath();
        Assertions.assertThat(jsonPath.getString("body.pspSchemas.displayName[0]")).isEqualTo("PhonePe");
        Assertions.assertThat(jsonPath.getString("body.pspSchemas.name[0]")).isEqualTo("PhonePe");
        Assertions.assertThat(jsonPath.getString("body.pspSchemas.icon[0]")).isEqualTo("phonepe.png");
        Assertions.assertThat(jsonPath.getString("body.pspSchemas.scheme[0]")).isEqualTo("phonepe");

        Assertions.assertThat(jsonPath.getString("body.pspSchemas.displayName[1]")).isEqualTo("Paytm");
        Assertions.assertThat(jsonPath.getString("body.pspSchemas.name[1]")).isEqualTo("paytm payments bank");
        Assertions.assertThat(jsonPath.getString("body.pspSchemas.icon[1]")).isEqualTo("paytm.png");
        Assertions.assertThat(jsonPath.getString("body.pspSchemas.scheme[1]")).isEqualTo("paytmmp");

        Assertions.assertThat(jsonPath.getString("body.pspSchemas.displayName[2]")).isEqualTo("Google Pay");
        Assertions.assertThat(jsonPath.getString("body.pspSchemas.name[2]")).isEqualTo("google pay");
        Assertions.assertThat(jsonPath.getString("body.pspSchemas.icon[2]")).isEqualTo("gpay.png");
        Assertions.assertThat(jsonPath.getString("body.pspSchemas.scheme[2]")).isEqualTo("tez");

        Assertions.assertThat(jsonPath.getString("body.pspSchemas.displayName[3]")).isEqualTo("BHIM");
        Assertions.assertThat(jsonPath.getString("body.pspSchemas.name[3]")).isEqualTo("BHIM");
        Assertions.assertThat(jsonPath.getString("body.pspSchemas.icon[3]")).isEqualTo("BHIM.png");
        Assertions.assertThat(jsonPath.getString("body.pspSchemas.scheme[3]")).isEqualTo("bhim");

        Assertions.assertThat(jsonPath.getString("body.pspSchemas.displayName[4]")).isEqualTo("WhatsApp");
        Assertions.assertThat(jsonPath.getString("body.pspSchemas.name[4]")).isEqualTo("whatsapp pay");
        Assertions.assertThat(jsonPath.getString("body.pspSchemas.icon[4]")).isEqualTo("whatsapp.png");
        Assertions.assertThat(jsonPath.getString("body.pspSchemas.scheme[4]")).isEqualTo("whatsapp");

    }

    @Owner(Constants.Owner.VIDHI)
    @Feature("PGP-53182")
    @Test(description = "Verify success response of FetchUPI PSP  Options API when invalid SSO token is passed")
    public void FetchSuccessUPIPSPOptions_invalidSsoToken() throws Exception {

        Constants.MerchantType merchantType = Constants.MerchantType.Deferred_Subs_MID;
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        FetchPspApps fetchPspApps = new FetchPspApps(merchantType, "83ac6c64-ee00-4b6c-af0f-073789458000");
        Response response = fetchPspApps.execute();
        System.out.println(response);
        JsonPath jsonPath=response.jsonPath();

        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("2004");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("SSO Token is invalid");

    }

    @Owner(Constants.Owner.VIDHI)
    @Feature("PGP-53182")
    @Test(description = "Verify success response of FetchUPI PSP  Options API when null is passed in SSO token field")
    public void FetchSuccessUPIPSPOptions_nullSsoToken() throws Exception {

        Constants.MerchantType merchantType = Constants.MerchantType.Deferred_Subs_MID;
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        FetchPspApps fetchPspApps = new FetchPspApps(merchantType, "");
        Response response = fetchPspApps.execute();
        System.out.println(response);
        JsonPath jsonPath=response.jsonPath();

        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("1001");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Request parameters are not valid");

    }
}
