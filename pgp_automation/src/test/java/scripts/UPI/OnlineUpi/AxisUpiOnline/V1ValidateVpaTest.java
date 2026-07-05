package scripts.UPI.OnlineUpi.AxisUpiOnline;

import com.paytm.api.V1ValidateVpa;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.appconstants.Constants;
import com.paytm.appconstants.Constants.AxisVPAs;
import com.paytm.appconstants.Constants.MerchantType;
import com.paytm.appconstants.FF4JFeatures;
import com.paytm.apphelpers.LogsValidationHelper;
import com.paytm.apphelpers.PG2LogsValidationHelper;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.utils.ff4j.FF4JFlags;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import java.time.Instant;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.RandomStringUtils;
import org.assertj.core.api.SoftAssertions;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Feature("PGP-54059")
@Owner(Constants.Owner.ABHISHEK_VERMA)
public class V1ValidateVpaTest {

    Constants.MerchantType axisMerchant = MerchantType.ISSUER_TOKEN_3P;
    Constants.MerchantType VpaMerchant = MerchantType.APPLY_OFFER_MID;
    Constants.MerchantType ocilValidateVpaMerchant = MerchantType.ALLOWED_TPAP_MERCHANT_PPSL;
    String custId = RandomStringUtils.randomAlphabetic(10) + Instant.now().toEpochMilli();


    @BeforeClass()
    public void ff4jCheck() {
        FF4JFlags.enable(FF4JFeatures.THEIA_ENABLE_AXIS_VPA_VALIDATE);
        FF4JFlags.enable(FF4JFeatures.THEIA_ENABLE_AXIS_VPA_VALIDATE_ON_STANDARD_FLOW);
    }

    @Test(description = "Verify response when valid VPA is sent in request")
    public void validVpa() throws InterruptedException {

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, axisMerchant)
            .setCustId(custId)
            .setTxnValue("2.00")
            .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);

        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderId = initTxnDTO.orderFromBody();
        V1ValidateVpa v1ValidateVpa = new V1ValidateVpa().buildRequest("TXN_TOKEN", txnToken,
            AxisVPAs.validVpa, orderId, null,
            axisMerchant.getId(), null);
        JsonPath v1ValidateVpaResponse = v1ValidateVpa.execute().jsonPath();

        SoftAssertions softAssertions = new SoftAssertions();
        String validateRequestBank = null;

        validateRequestBank = LogsValidationHelper.verifyLogsOnPod(
            PG2LogsValidationHelper.setEnvService.theia_facade,
            initTxnDTO.getBody().getOrderId(),
            "\"COMPONENT\": \"AXIS_BANK\"", "REQUEST");

        softAssertions.assertThat(validateRequestBank).isNotBlank();
        softAssertions.assertThat(v1ValidateVpaResponse.getString("body.resultInfo.resultStatus"))
            .isEqualToIgnoringCase("S");
        softAssertions.assertThat(v1ValidateVpaResponse.getString("body.resultInfo.resultCode"))
            .isEqualToIgnoringCase("0000");
        softAssertions.assertThat(v1ValidateVpaResponse.getString("body.resultInfo.resultMsg"))
            .isEqualToIgnoringCase("Success");
        softAssertions.assertThat(v1ValidateVpaResponse.getString("body.valid"))
            .isEqualToIgnoringCase("true");
        softAssertions.assertThat(v1ValidateVpaResponse.getString("body.vpa"))
            .isEqualToIgnoringCase(AxisVPAs.validVpa);
        softAssertions.assertAll();
    }

    @Test(description = "Verify response when invalid VPA is sent in request")
    public void invalidVpa() {

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, axisMerchant)
                .setCustId(custId)
                .setTxnValue("2.00")
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);

        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderId = initTxnDTO.orderFromBody();
        V1ValidateVpa v1ValidateVpa = new V1ValidateVpa().buildRequest("TXN_TOKEN", txnToken, AxisVPAs.invalidVpa, orderId, null,
                axisMerchant.getId(), null);
        JsonPath v1ValidateVpaResponse = v1ValidateVpa.execute().jsonPath();

        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(v1ValidateVpaResponse.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("F");
        softAssertions.assertThat(v1ValidateVpaResponse.getString("body.resultInfo.resultCode")).isEqualToIgnoringCase("0001");
        softAssertions.assertThat(v1ValidateVpaResponse.getString("body.resultInfo.resultMsg")).isEqualToIgnoringCase("Invalid VPA, Try Again");
        softAssertions.assertThat(v1ValidateVpaResponse.getString("body.valid")).isEqualToIgnoringCase("false");
        softAssertions.assertThat(v1ValidateVpaResponse.getString("body.vpa")).isEqualToIgnoringCase(AxisVPAs.invalidVpa);
        softAssertions.assertAll();
    }

    @Test(description = "Verify response when generic error from bank for vpa case")
    public void genericErrorVpa() {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, axisMerchant)
                .setCustId(custId)
                .setTxnValue("2.00")
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);

        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderId = initTxnDTO.orderFromBody();
        V1ValidateVpa v1ValidateVpa = new V1ValidateVpa().buildRequest("TXN_TOKEN", txnToken, AxisVPAs.genericErrorVpa, orderId, null,
                axisMerchant.getId(), null);
        JsonPath v1ValidateVpaResponse = v1ValidateVpa.execute().jsonPath();

        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(v1ValidateVpaResponse.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("F");
        softAssertions.assertThat(v1ValidateVpaResponse.getString("body.resultInfo.resultCode")).isEqualToIgnoringCase("0001");
        softAssertions.assertThat(v1ValidateVpaResponse.getString("body.resultInfo.resultMsg")).isEqualToIgnoringCase("Error in Verification, Try Again");
        softAssertions.assertThat(v1ValidateVpaResponse.getString("body.valid")).isEqualToIgnoringCase("false");
        softAssertions.assertThat(v1ValidateVpaResponse.getString("body.vpa")).isEqualToIgnoringCase(AxisVPAs.genericErrorVpa);
        softAssertions.assertAll();
    }

    @Test(description = "Verify response when valid phone number is sent in numericId field in request")
    public void validMobileInNumericId() {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, axisMerchant)
                .setCustId(custId)
                .setTxnValue("2.00")
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);

        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderId = initTxnDTO.orderFromBody();
        V1ValidateVpa v1ValidateVpa = new V1ValidateVpa().buildRequest("TXN_TOKEN", txnToken, null, orderId, AxisVPAs.validMobile,
                axisMerchant.getId(), null);
        JsonPath v1ValidateVpaResponse = v1ValidateVpa.execute().jsonPath();

        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(v1ValidateVpaResponse.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("S");
        softAssertions.assertThat(v1ValidateVpaResponse.getString("body.resultInfo.resultCode")).isEqualToIgnoringCase("0000");
        softAssertions.assertThat(v1ValidateVpaResponse.getString("body.resultInfo.resultMsg")).isEqualToIgnoringCase("Success");
        softAssertions.assertThat(v1ValidateVpaResponse.getString("body.valid")).isEqualToIgnoringCase("true");
        softAssertions.assertThat(v1ValidateVpaResponse.getString("body.vpa")).isEqualToIgnoringCase(AxisVPAs.validMobile + "@axis");
        softAssertions.assertAll();
    }

    @Test(description = "Verify response when invalid phone number is sent in numericId filed in request")
    public void invalidMobileInNumericId() {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, axisMerchant)
                .setCustId(custId)
                .setTxnValue("2.00")
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);

        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderId = initTxnDTO.orderFromBody();
        V1ValidateVpa v1ValidateVpa = new V1ValidateVpa().buildRequest("TXN_TOKEN", txnToken, null, orderId, AxisVPAs.invalidMobile,
                axisMerchant.getId(), null);
        JsonPath v1ValidateVpaResponse = v1ValidateVpa.execute().jsonPath();

        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(v1ValidateVpaResponse.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("F");
        softAssertions.assertThat(v1ValidateVpaResponse.getString("body.resultInfo.resultCode")).isEqualToIgnoringCase("0001");
        softAssertions.assertThat(v1ValidateVpaResponse.getString("body.resultInfo.resultMsg")).isEqualToIgnoringCase("Invalid UPI Number, Try Again");
        softAssertions.assertThat(v1ValidateVpaResponse.getString("body.valid")).isEqualToIgnoringCase("false");
        softAssertions.assertAll();
    }

    @Test(description = "Verify response when generic error from bank for mobile number case in numericId filed")
    public void genericErrorMobileNumericId() {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, axisMerchant)
                .setCustId(custId)
                .setTxnValue("2.00")
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);

        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderId = initTxnDTO.orderFromBody();
        V1ValidateVpa v1ValidateVpa = new V1ValidateVpa().buildRequest("TXN_TOKEN", txnToken, null, orderId, AxisVPAs.genericErrorMobile,
                axisMerchant.getId(), null);
        JsonPath v1ValidateVpaResponse = v1ValidateVpa.execute().jsonPath();

        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(v1ValidateVpaResponse.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("F");
        softAssertions.assertThat(v1ValidateVpaResponse.getString("body.resultInfo.resultCode")).isEqualToIgnoringCase("0001");
        softAssertions.assertThat(v1ValidateVpaResponse.getString("body.resultInfo.resultMsg")).isEqualToIgnoringCase("Error in Verification, Try Again");
        softAssertions.assertThat(v1ValidateVpaResponse.getString("body.valid")).isEqualToIgnoringCase("false");
        softAssertions.assertAll();
    }

    @Test(description = "Verify response when valid phone number is sent in phoneNo field in request")
    public void validMobile() {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, axisMerchant)
                .setCustId(custId)
                .setTxnValue("2.00")
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);

        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderId = initTxnDTO.orderFromBody();
        V1ValidateVpa v1ValidateVpa = new V1ValidateVpa().buildRequest("TXN_TOKEN", txnToken, null, orderId, null,
                axisMerchant.getId(), AxisVPAs.validMobile);
        JsonPath v1ValidateVpaResponse = v1ValidateVpa.execute().jsonPath();

        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(v1ValidateVpaResponse.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("S");
        softAssertions.assertThat(v1ValidateVpaResponse.getString("body.resultInfo.resultCode")).isEqualToIgnoringCase("0000");
        softAssertions.assertThat(v1ValidateVpaResponse.getString("body.resultInfo.resultMsg")).isEqualToIgnoringCase("Success");
        softAssertions.assertThat(v1ValidateVpaResponse.getString("body.valid")).isEqualToIgnoringCase("true");
        softAssertions.assertThat(v1ValidateVpaResponse.getString("body.vpa")).isEqualToIgnoringCase(AxisVPAs.validMobile + "@axis");
        softAssertions.assertAll();
    }

    @Test(description = "Verify response when invalid phone number is sent in phoneNo filed in request")
    public void invalidMobile() {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, axisMerchant)
                .setCustId(custId)
                .setTxnValue("2.00")
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);

        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderId = initTxnDTO.orderFromBody();
        V1ValidateVpa v1ValidateVpa = new V1ValidateVpa().buildRequest("TXN_TOKEN", txnToken, null, orderId, null,
                axisMerchant.getId(), AxisVPAs.invalidMobile);
        JsonPath v1ValidateVpaResponse = v1ValidateVpa.execute().jsonPath();

        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(v1ValidateVpaResponse.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("F");
        softAssertions.assertThat(v1ValidateVpaResponse.getString("body.resultInfo.resultCode")).isEqualToIgnoringCase("0001");
        softAssertions.assertThat(v1ValidateVpaResponse.getString("body.resultInfo.resultMsg")).isEqualToIgnoringCase("Invalid UPI Number, Try Again");
        softAssertions.assertThat(v1ValidateVpaResponse.getString("body.valid")).isEqualToIgnoringCase("false");
        softAssertions.assertAll();
    }

    @Test(description = "Verify response when generic error from bank for mobile number case in phoneNo filed")
    public void genericErrorMobile() {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, axisMerchant)
                .setCustId(custId)
                .setTxnValue("2.00")
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);

        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderId = initTxnDTO.orderFromBody();
        V1ValidateVpa v1ValidateVpa = new V1ValidateVpa().buildRequest("TXN_TOKEN", txnToken, null, orderId, null,
                axisMerchant.getId(), AxisVPAs.genericErrorMobile);
        JsonPath v1ValidateVpaResponse = v1ValidateVpa.execute().jsonPath();

        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(v1ValidateVpaResponse.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("F");
        softAssertions.assertThat(v1ValidateVpaResponse.getString("body.resultInfo.resultCode")).isEqualToIgnoringCase("0001");
        softAssertions.assertThat(v1ValidateVpaResponse.getString("body.resultInfo.resultMsg")).isEqualToIgnoringCase("Error in Verification, Try Again");
        softAssertions.assertThat(v1ValidateVpaResponse.getString("body.valid")).isEqualToIgnoringCase("false");
        softAssertions.assertAll();
    }

    @Owner(Constants.Owner.AJEESH)
    @Feature("PPSL-406")
    @Test(description = "Verify when vpa handle ending with @paytm then expected error message should be shown.")
    public void VerifyVPAwithPaytmthrowsError() {
        // Ff4j theia.disabledPaytmVpaHandle is On
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, VpaMerchant)
                .setCustId(custId)
                .setTxnValue("2.00")
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);

        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderId = initTxnDTO.orderFromBody();
        V1ValidateVpa v1ValidateVpa = new V1ValidateVpa().buildRequest("TXN_TOKEN", txnToken, "999999995@paytm", orderId, null,
                VpaMerchant.getId(), null);
        JsonPath v1ValidateVpaResponse = v1ValidateVpa.execute().jsonPath();

        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(v1ValidateVpaResponse.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("F");
        softAssertions.assertThat(v1ValidateVpaResponse.getString("body.resultInfo.resultCode")).isEqualToIgnoringCase("0001");
        softAssertions.assertThat(v1ValidateVpaResponse.getString("body.resultInfo.resultMsg")).isEqualToIgnoringCase("Invalid VPA. Please use one of the new VPA handles created in the Paytm app.");
        softAssertions.assertAll();
    }

    @Owner(Constants.Owner.AJEESH)
    @Feature("PPSL-406")
    @Test(description = "Verify when numericId which has mapped vpahandle ending with @paytm,then expected error message should be shown.")
    public void VerifyNumericIDmappedwithPaytmthrowsError() {
        // Ff4j theia.disabledPaytmVpaHandle is On
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, VpaMerchant)
                .setCustId(custId)
                .setTxnValue("2.00")
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);

        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderId = initTxnDTO.orderFromBody();
        V1ValidateVpa v1ValidateVpa = new V1ValidateVpa().buildRequest("TXN_TOKEN", txnToken, null, orderId, "999999995",
                VpaMerchant.getId(), null);
        JsonPath v1ValidateVpaResponse = v1ValidateVpa.execute().jsonPath();

        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(v1ValidateVpaResponse.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("F");
        softAssertions.assertThat(v1ValidateVpaResponse.getString("body.resultInfo.resultCode")).isEqualToIgnoringCase("0001");
        softAssertions.assertThat(v1ValidateVpaResponse.getString("body.resultInfo.resultMsg")).isEqualToIgnoringCase("Invalid UPI number. Please try again.");
        softAssertions.assertThat(v1ValidateVpaResponse.getString("body.valid")).isEqualToIgnoringCase("false");
        softAssertions.assertAll();
    }
    @Owner(Constants.Owner.AJEESH)
    @Feature("PPSL-406")
    @Test(description = "Verify when vpa handle ending with @paytm then response is Success.")
    public void VerifyVPAwithPaytmthrowsNoError() {
        // Ff4j theia.disabledPaytmVpaHandle is Off
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, axisMerchant)
                .setCustId(custId)
                .setTxnValue("2.00")
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);

        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderId = initTxnDTO.orderFromBody();
        V1ValidateVpa v1ValidateVpa = new V1ValidateVpa().buildRequest("TXN_TOKEN", txnToken, "999999995@paytm", orderId, null,
                axisMerchant.getId(), null);
        JsonPath v1ValidateVpaResponse = v1ValidateVpa.execute().jsonPath();

        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(v1ValidateVpaResponse.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("S");
        softAssertions.assertThat(v1ValidateVpaResponse.getString("body.resultInfo.resultCode")).isEqualToIgnoringCase("0000");
        softAssertions.assertThat(v1ValidateVpaResponse.getString("body.resultInfo.resultMsg")).isEqualToIgnoringCase("Success");
        softAssertions.assertThat(v1ValidateVpaResponse.getString("body.vpa")).isEqualToIgnoringCase("999999995@paytm");
        softAssertions.assertThat(v1ValidateVpaResponse.getString("body.valid")).isEqualToIgnoringCase("true");

        softAssertions.assertAll();
    }

    @Owner(Constants.Owner.AJEESH)
    @Feature("PPSL-406")
    @Test(description = "Verify when numericId which has mapped vpahandle ending with @paytm,then response is Success.")
    public void VerifyNumericIDmappedwithPaytmthrowsNoError() {
        // Ff4j theia.disabledPaytmVpaHandle is Off
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, axisMerchant)
                .setCustId(custId)
                .setTxnValue("2.00")
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);

        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderId = initTxnDTO.orderFromBody();
        V1ValidateVpa v1ValidateVpa = new V1ValidateVpa().buildRequest("TXN_TOKEN", txnToken, null, orderId, "999999995",
                axisMerchant.getId(), null);
        JsonPath v1ValidateVpaResponse = v1ValidateVpa.execute().jsonPath();

        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(v1ValidateVpaResponse.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("S");
        softAssertions.assertThat(v1ValidateVpaResponse.getString("body.resultInfo.resultCode")).isEqualToIgnoringCase("0000");
        softAssertions.assertThat(v1ValidateVpaResponse.getString("body.resultInfo.resultMsg")).isEqualToIgnoringCase("Success");
        softAssertions.assertThat(v1ValidateVpaResponse.getString("body.vpa")).isEqualToIgnoringCase("999999995@paytm");
        softAssertions.assertThat(v1ValidateVpaResponse.getString("body.valid")).isEqualToIgnoringCase("true");
        softAssertions.assertAll();
    }

    @Owner(Constants.Owner.ABHISHEK_VERMA)
    @Feature("PGP-58741")
    @Test(description = "Verify mid is going to OCIL switch in txn/v3/validate-address request")
    public void midIsGoingInOCILValidateVPARequest() throws InterruptedException {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, ocilValidateVpaMerchant)
            .setCustId(custId)
            .setTxnValue("2.00")
            .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);

        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderId = initTxnDTO.orderFromBody();
        V1ValidateVpa v1ValidateVpa = new V1ValidateVpa().buildRequest("TXN_TOKEN", txnToken, AxisVPAs.validVpa, orderId, null,
            ocilValidateVpaMerchant.getId(), null);
        JsonPath v1ValidateVpaResponse = v1ValidateVpa.execute().jsonPath();
        SoftAssertions softAssertions = new SoftAssertions();
        String validateRequestBank = LogsValidationHelper.verifyLogsOnPod(
                PG2LogsValidationHelper.setEnvService.theia_facade,
                initTxnDTO.getBody().getOrderId(),
                "UPI_SECURE");
        System.out.println("Validate request is"+ validateRequestBank);
        softAssertions.assertThat(validateRequestBank).isNotEmpty();
        // Extract target URL from logs and validate MID presence
        String targetUrl = null;
        Matcher matcher = Pattern.compile("target=([^,\\]]+)").matcher(validateRequestBank);
        if (matcher.find()) {
            targetUrl = matcher.group(1);
        }
        softAssertions.assertThat(targetUrl).isNotBlank();
        softAssertions.assertThat(targetUrl)
            .as("target URL should contain merchantId query param with expected MID")
            .contains("merchantId=" + ocilValidateVpaMerchant.getId());
        softAssertions.assertThat(v1ValidateVpaResponse.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("S");
        softAssertions.assertThat(v1ValidateVpaResponse.getString("body.resultInfo.resultCode")).isEqualToIgnoringCase("0000");
        softAssertions.assertThat(v1ValidateVpaResponse.getString("body.resultInfo.resultMsg")).isEqualToIgnoringCase("Success");
        softAssertions.assertThat(v1ValidateVpaResponse.getString("body.valid")).isEqualToIgnoringCase("true");
        softAssertions.assertThat(v1ValidateVpaResponse.getString("body.vpa")).isEqualToIgnoringCase(AxisVPAs.validVpa);
        softAssertions.assertAll();
    }
}
