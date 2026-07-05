package scripts.UPI.OnlineUpi.AxisUpiOnline;

import com.paytm.LocalConfig;
import com.paytm.api.V4ValidateVpa;
import com.paytm.appconstants.Constants;
import com.paytm.appconstants.Constants.AxisVPAs;
import com.paytm.appconstants.Constants.MerchantType;
import com.paytm.appconstants.FF4JFeatures;
import com.paytm.utils.ff4j.FF4JFlags;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;

import java.time.Instant;
import java.util.Date;

import org.apache.commons.lang.RandomStringUtils;
import org.assertj.core.api.SoftAssertions;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Feature("PGP-54059")
@Owner(Constants.Owner.ABHISHEK_VERMA)
public class V4ValidateVpaTest {

    Constants.MerchantType axisMerchant = MerchantType.ISSUER_TOKEN_3P;
    Constants.MerchantType VpaMerchant = MerchantType.APPLY_OFFER_MID;


    @BeforeClass()
    public void ff4jCheck() {
        FF4JFlags.enable(FF4JFeatures.THEIA_ENABLE_AXIS_VPA_VALIDATE);
        FF4JFlags.enable(FF4JFeatures.THEIA_ENABLE_AXIS_VPA_VALIDATE_ON_STANDARD_FLOW);
    }

    @Test(description = "Verify error is returned in response when incorrect JWT is sent in request")
    public void incorrectJwt() {
        long currentTimestamp = new Date().getTime() / 1000;
        String referenceId = RandomStringUtils.randomAlphabetic(10) + Instant.now().toEpochMilli();
        V4ValidateVpa v4ValidateVpa = new V4ValidateVpa().buildRequest(axisMerchant.getId(), referenceId, currentTimestamp,
                AxisVPAs.validVpa, LocalConfig.SUPERGW_JWT_KEY + 1, "supergw");
        JsonPath v4ValidateVpaResponse = v4ValidateVpa.execute().jsonPath();

        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(v4ValidateVpaResponse.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("U");
        softAssertions.assertThat(v4ValidateVpaResponse.getString("body.resultInfo.resultCode")).isEqualToIgnoringCase("00000900");
        softAssertions.assertThat(v4ValidateVpaResponse.getString("body.resultInfo.resultMsg")).isEqualToIgnoringCase("System error");
        softAssertions.assertAll();
    }

    @Test(description = "Verify response when valid VPA is sent in request")
    public void validVpa() {
        long currentTimestamp = new Date().getTime() / 1000;
        String referenceId = RandomStringUtils.randomAlphabetic(10) + Instant.now().toEpochMilli();
        V4ValidateVpa v4ValidateVpa = new V4ValidateVpa().buildRequest(axisMerchant.getId(), referenceId, currentTimestamp, AxisVPAs.validVpa, LocalConfig.SUPERGW_JWT_KEY, "supergw");
        JsonPath v4ValidateVpaResponse = v4ValidateVpa.execute().jsonPath();

        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(v4ValidateVpaResponse.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("S");
        softAssertions.assertThat(v4ValidateVpaResponse.getString("body.resultInfo.resultCode")).isEqualToIgnoringCase("0000");
        softAssertions.assertThat(v4ValidateVpaResponse.getString("body.resultInfo.resultMsg")).isEqualToIgnoringCase("Success");
        softAssertions.assertThat(v4ValidateVpaResponse.getString("body.valid")).isEqualToIgnoringCase("true");
        softAssertions.assertAll();
    }

    @Test(description = "Verify response when invalid VPA is sent in request")
    public void invalidVpa() {
        long currentTimestamp = new Date().getTime() / 1000;
        String referenceId = RandomStringUtils.randomAlphabetic(10) + Instant.now().toEpochMilli();
        V4ValidateVpa v4ValidateVpa = new V4ValidateVpa().buildRequest(axisMerchant.getId(), referenceId, currentTimestamp, AxisVPAs.invalidVpa, LocalConfig.SUPERGW_JWT_KEY, "supergw");
        JsonPath v4ValidateVpaResponse = v4ValidateVpa.execute().jsonPath();

        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(v4ValidateVpaResponse.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("F");
        softAssertions.assertThat(v4ValidateVpaResponse.getString("body.resultInfo.resultCode")).isEqualToIgnoringCase("0001");
        softAssertions.assertThat(v4ValidateVpaResponse.getString("body.resultInfo.resultMsg")).isEqualToIgnoringCase("INVALID VIRTUAL ADDRESS.");
        softAssertions.assertThat(v4ValidateVpaResponse.getString("body.valid")).isEqualToIgnoringCase("false");
        softAssertions.assertAll();
    }

    @Test(description = "Verify response when generic error from bank for vpa case")
    public void genericErrorVpa() {
        long currentTimestamp = new Date().getTime() / 1000;
        String referenceId = RandomStringUtils.randomAlphabetic(10) + Instant.now().toEpochMilli();
        V4ValidateVpa v4ValidateVpa = new V4ValidateVpa().buildRequest(axisMerchant.getId(), referenceId, currentTimestamp, AxisVPAs.genericErrorVpa, LocalConfig.SUPERGW_JWT_KEY, "supergw");
        JsonPath v4ValidateVpaResponse = v4ValidateVpa.execute().jsonPath();

        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(v4ValidateVpaResponse.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("F");
        softAssertions.assertThat(v4ValidateVpaResponse.getString("body.resultInfo.resultCode")).isEqualToIgnoringCase("0001");
        softAssertions.assertThat(v4ValidateVpaResponse.getString("body.resultInfo.resultMsg")).isEqualToIgnoringCase("MM2");
        softAssertions.assertThat(v4ValidateVpaResponse.getString("body.valid")).isEqualToIgnoringCase("false");
        softAssertions.assertAll();
    }

    @Test(description = "Verify response when valid Phone Number is sent in request")
    public void validMobile() {
        long currentTimestamp = new Date().getTime() / 1000;
        String referenceId = RandomStringUtils.randomAlphabetic(10) + Instant.now().toEpochMilli();
        V4ValidateVpa v4ValidateVpa = new V4ValidateVpa().buildRequest(axisMerchant.getId(), referenceId, currentTimestamp, AxisVPAs.validMobile, LocalConfig.SUPERGW_JWT_KEY, "supergw");
        JsonPath v4ValidateVpaResponse = v4ValidateVpa.execute().jsonPath();

        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(v4ValidateVpaResponse.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("S");
        softAssertions.assertThat(v4ValidateVpaResponse.getString("body.resultInfo.resultCode")).isEqualToIgnoringCase("0000");
        softAssertions.assertThat(v4ValidateVpaResponse.getString("body.resultInfo.resultMsg")).isEqualToIgnoringCase("Success");
        softAssertions.assertThat(v4ValidateVpaResponse.getString("body.valid")).isEqualToIgnoringCase("true");
        softAssertions.assertAll();
    }

    @Test(description = "Verify response when invalid phone number is sent in request")
    public void invalidMobile() {
        long currentTimestamp = new Date().getTime() / 1000;
        String referenceId = RandomStringUtils.randomAlphabetic(10) + Instant.now().toEpochMilli();
        V4ValidateVpa v4ValidateVpa = new V4ValidateVpa().buildRequest(axisMerchant.getId(), referenceId, currentTimestamp, AxisVPAs.invalidMobile, LocalConfig.SUPERGW_JWT_KEY, "supergw");
        JsonPath v4ValidateVpaResponse = v4ValidateVpa.execute().jsonPath();

        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(v4ValidateVpaResponse.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("F");
        softAssertions.assertThat(v4ValidateVpaResponse.getString("body.resultInfo.resultCode")).isEqualToIgnoringCase("0001");
        softAssertions.assertThat(v4ValidateVpaResponse.getString("body.resultInfo.resultMsg")).isEqualToIgnoringCase("INVALID VIRTUAL ADDRESS.");
        softAssertions.assertThat(v4ValidateVpaResponse.getString("body.valid")).isEqualToIgnoringCase("false");
        softAssertions.assertAll();
    }

    @Test(description = "Verify response when generic error from bank for mobile number case")
    public void genericErrorMobile() {
        long currentTimestamp = new Date().getTime() / 1000;
        String referenceId = RandomStringUtils.randomAlphabetic(10) + Instant.now().toEpochMilli();
        V4ValidateVpa v4ValidateVpa = new V4ValidateVpa().buildRequest(axisMerchant.getId(), referenceId, currentTimestamp, AxisVPAs.genericErrorMobile, LocalConfig.SUPERGW_JWT_KEY, "supergw");
        JsonPath v4ValidateVpaResponse = v4ValidateVpa.execute().jsonPath();

        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(v4ValidateVpaResponse.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("F");
        softAssertions.assertThat(v4ValidateVpaResponse.getString("body.resultInfo.resultCode")).isEqualToIgnoringCase("0001");
        softAssertions.assertThat(v4ValidateVpaResponse.getString("body.resultInfo.resultMsg")).isEqualToIgnoringCase("MM2");
        softAssertions.assertThat(v4ValidateVpaResponse.getString("body.valid")).isEqualToIgnoringCase("false");
        softAssertions.assertAll();
    }

    @Owner(Constants.Owner.AJEESH)
    @Feature("PPSL-406")
    @Test(description = "Verify when vpa handle ending with @paytm then expected error message should be shown.")
    public void VerifyVPAwithPaytmthrowsError() {
        long currentTimestamp = new Date().getTime() / 1000;
        String referenceId = RandomStringUtils.randomAlphabetic(10) + Instant.now().toEpochMilli();
        V4ValidateVpa v4ValidateVpa = new V4ValidateVpa().buildRequest(VpaMerchant.getId(), referenceId, currentTimestamp, "7777777777@paytm", LocalConfig.SUPERGW_JWT_KEY, "supergw");
        JsonPath v4ValidateVpaResponse = v4ValidateVpa.execute().jsonPath();

        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(v4ValidateVpaResponse.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("F");
        softAssertions.assertThat(v4ValidateVpaResponse.getString("body.resultInfo.resultCode")).isEqualToIgnoringCase("0001");
        softAssertions.assertThat(v4ValidateVpaResponse.getString("body.resultInfo.resultMsg")).isEqualToIgnoringCase("Invalid VPA. Please use one of the new VPA handles created in the Paytm app.");
        softAssertions.assertAll();
    }

    @Owner(Constants.Owner.AJEESH)
    @Feature("PPSL-406")
    @Test(description = "Verify when vpa handle ending with @paytm then response is Success.")
    public void VerifyVPAwithPaytmthrowsNoError() {
        long currentTimestamp = new Date().getTime() / 1000;
        String referenceId = RandomStringUtils.randomAlphabetic(10) + Instant.now().toEpochMilli();
        V4ValidateVpa v4ValidateVpa = new V4ValidateVpa().buildRequest(axisMerchant.getId(), referenceId, currentTimestamp, "7777777777@paytm", LocalConfig.SUPERGW_JWT_KEY, "supergw");
        JsonPath v4ValidateVpaResponse = v4ValidateVpa.execute().jsonPath();

        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(v4ValidateVpaResponse.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("S");
        softAssertions.assertThat(v4ValidateVpaResponse.getString("body.resultInfo.resultCode")).isEqualToIgnoringCase("0000");
        softAssertions.assertThat(v4ValidateVpaResponse.getString("body.resultInfo.resultMsg")).isEqualToIgnoringCase("Success");
        softAssertions.assertThat(v4ValidateVpaResponse.getString("body.valid")).isEqualToIgnoringCase("true");
        softAssertions.assertThat(v4ValidateVpaResponse.getString("body.vpa")).isEqualToIgnoringCase("7777777777@paytm");

        softAssertions.assertAll();
    }


}
