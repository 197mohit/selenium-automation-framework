package scripts.api.theia.emiSubvention;

import com.paytm.api.TxnStatus;

import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.api.theia.emiSubvention.ApiV1Bank;
import com.paytm.api.theia.emiSubvention.ApiV1Tenure;
import com.paytm.api.theia.emiSubvention.ApiV1Validate;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.*;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.TxnAmount;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.emiSubvention.ApiV1Banks.request.ApiV1BanksRequest;
import com.paytm.dto.emiSubvention.ApiV1Tenure.request.ApiV1TenureRequest;
import com.paytm.dto.emiSubvention.ApiV1Tenure.request.Filters;
import com.paytm.dto.emiSubvention.ApiV1Validate.request.ApiV1ValidateRequest;
import com.paytm.dto.emiSubvention.ApiV1Validate.request.OfferDetails;
import com.paytm.dto.emiSubvention.ApiV1Validate.request.PaymentDetails;
import com.paytm.framework.api.curlloggingutil.CurlLoggingRestAssuredConfigBuilder;
import com.paytm.framework.reporting.filters.RequestResponseLoggingFilter;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
import com.paytm.utils.merchant.util.PayMethodType;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.assertj.core.api.Assertions;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Optional;
import org.testng.annotations.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.paytm.appconstants.Constants.Owner.PUSPA;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@Owner(PUSPA)
@Feature("PGP-49046")
public class EmiSubventionAPIsMigration extends PGPBaseTest {


    private final static ResponseSpecification SUCCESS_RESPONSE = new ResponseSpecBuilder()
            .expectStatusCode(200)
            .expectBody("body.resultInfo.resultStatus", equalToIgnoringCase("s"))
            .expectBody("body.resultInfo.resultCode", equalToIgnoringCase("0000"))
            .expectBody("body.resultInfo.resultMsg", equalToIgnoringCase("Success"))
            .build();

    private final static ResponseSpecification REQUEST_PARAMETER_NOT_VALID = new ResponseSpecBuilder()
            .expectStatusCode(200)
            .expectBody("body.resultInfo.resultStatus", equalToIgnoringCase("F"))
            .expectBody("body.resultInfo.resultCode", equalToIgnoringCase("1001"))
            .expectBody("body.resultInfo.resultMsg", equalToIgnoringCase("Request parameters are not valid"))
            .build();

    private final static ResponseSpecification INVALID_AMOUNT_DETAILS = new ResponseSpecBuilder()
            .expectStatusCode(200)
            .expectBody("body.resultInfo.resultStatus", equalToIgnoringCase("F"))
            .expectBody("body.resultInfo.resultCode", equalToIgnoringCase("1001"))
            .expectBody("body.resultInfo.resultMsg", equalToIgnoringCase("Invalid Amount Details"))
            .build();

    private final static ResponseSpecification SSO_TOKEN_INVALID = new ResponseSpecBuilder()
            .expectStatusCode(401)
            .expectBody("body.resultInfo.resultStatus", equalToIgnoringCase("F"))
            .expectBody("body.resultInfo.resultCode", equalToIgnoringCase("2004"))
            .expectBody("body.resultInfo.resultMsg", equalToIgnoringCase("SSO Token is invalid"))
            .build();

    private final static ResponseSpecification TOKEN_TYPE_NOT_SUPPORTED = new ResponseSpecBuilder()
            .expectStatusCode(200)
            .expectBody("body.resultInfo.resultStatus", equalToIgnoringCase("F"))
            .expectBody("body.resultInfo.resultCode", equalToIgnoringCase("REQUEST_PARAMS_VALIDATION_EXCEPTION"))
            .expectBody("body.resultInfo.resultMsg", equalToIgnoringCase("token type not supported"))
            .build();

    private final static ResponseSpecification CARD_DETAIL_NOT_VALID = new ResponseSpecBuilder()
            .expectStatusCode(200)
            .expectBody("body.resultInfo.resultStatus", equalToIgnoringCase("F"))
            .expectBody("body.resultInfo.resultCode", equalToIgnoringCase("1006"))
            .expectBody("body.resultInfo.resultMsg", equalToIgnoringCase("card details are not valid"))
            .build();

    private final static ResponseSpecification MANDATORY_PARAM_MISSING = new ResponseSpecBuilder()
            .expectStatusCode(200)
            .expectBody("body.resultInfo.resultStatus", equalToIgnoringCase("F"))
            .expectBody("body.resultInfo.resultCode", equalToIgnoringCase("1007"))
            .expectBody("body.resultInfo.resultMsg", equalToIgnoringCase("Both listing price and subvention amount is mandatory"))
            .build();

    RequestSpecification reqSpec = null;

    @BeforeClass
    public void setRequest() {
        reqSpec = new RequestSpecBuilder()
                .addFilter(new RequestResponseLoggingFilter())
                .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                .setConfig(new CurlLoggingRestAssuredConfigBuilder().build())
                .build();
    }


    @Test(description = "Verify success response of /theia/api/v1/emiSubvention/banks when SSO tokenType is passed")
    public void verifyBankhitsDiscoveryAPIwithSSO() throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        ApiV1BanksRequest req = new ApiV1BanksRequest.Builder()
                .setMid(merchantType.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setSubventionAmount("1000")
                .setPrice("1000")
                .setItems(null)
                .build();

        ApiV1Bank api = new ApiV1Bank(merchantType.getId(), req);
        Response response = api.execute();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, merchantType.getId(), "AFFORDABILITY_PLATFORM_DISCOVERY", "REQUEST");
        Assertions.assertThat(logs).contains("FUNCTION", "ads/v2/offer/discovery");
        Assertions.assertThat(logs).contains("\"withCalculation\":false");
        JsonPath jsonPath = response.then()
                .body("body.emiTypes", not(empty()))
                .body("body.emiTypes.type", containsInAnyOrder("STANDARD", "SUBVENTION"))
                .body("body.emiTypes.find{it.type == 'STANDARD'}.label", equalToIgnoringCase("Standard EMI"))
                .body("body.emiTypes.find{it.type == 'STANDARD'}.cardTypes", not(empty()))
                .body("body.emiTypes.find{it.type == 'STANDARD'}.cardTypes.cardType", containsInAnyOrder("DEBIT_CARD", "CREDIT_CARD", "NBFC"))
                .body("body.emiTypes.find{it.type == 'STANDARD'}.cardTypes.bankDetails", not(empty()))
                .body("body.emiTypes.find{it.type == 'STANDARD'}.cardTypes.bankDetails", everyItem(everyItem(hasKey("bankName"))))
                .body("body.emiTypes.find{it.type == 'STANDARD'}.cardTypes.bankDetails", everyItem(everyItem(hasKey("bankCode"))))
                .body("body.emiTypes.find{it.type == 'STANDARD'}.cardTypes.bankDetails", everyItem(everyItem(hasKey("bankLogoUrl"))))
                .body("body.emiTypes.find{it.type == 'SUBVENTION'}.label", equalToIgnoringCase("Zero/Low Cost EMI"))
                .body("body.emiTypes.find{it.type == 'SUBVENTION'}.cardTypes", not(empty()))
                .body("body.emiTypes.find{it.type == 'SUBVENTION'}.cardTypes.cardType", containsInAnyOrder("DEBIT_CARD", "CREDIT_CARD", "NBFC"))
                .body("body.emiTypes.find{it.type == 'SUBVENTION'}.cardTypes.bankDetails", not(empty()))
                .body("body.emiTypes.find{it.type == 'SUBVENTION'}.cardTypes.bankDetails", everyItem(everyItem(hasKey("bankName"))))
                .body("body.emiTypes.find{it.type == 'SUBVENTION'}.cardTypes.bankDetails", everyItem(everyItem(hasKey("bankCode"))))
                .body("body.emiTypes.find{it.type == 'SUBVENTION'}.cardTypes.bankDetails", everyItem(everyItem(hasKey("bankLogoUrl"))))
                .extract().jsonPath();
    }

    @Test(description = "Verify success response of /theia/api/v1/emiSubvention/banks when Checksum tokenType is passed")
    public void verifyBankhitsDiscoveryAPIwithChecksum() throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.EMI_DISCOVERY;
        ApiV1BanksRequest req = new ApiV1BanksRequest.Builder()
                .setMid(merchantType.getId())
                .setSubventionAmount("100")
                .setPrice("100")
                .setItems(null)
                .setTokenType("CHECKSUM")
                .build();

        ApiV1Bank api = new ApiV1Bank(merchantType.getId(), req);
        Response response = api.execute();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, merchantType.getId(), "AFFORDABILITY_PLATFORM_DISCOVERY", "REQUEST");
        Assertions.assertThat(logs).contains("FUNCTION", "ads/v2/offer/discovery");
        Assertions.assertThat(logs).contains("\"withCalculation\":false");
        JsonPath jsonPath = response.then()
                .body("body.emiTypes", not(empty()))
                .body("body.emiTypes.type", containsInAnyOrder("STANDARD", "SUBVENTION"))
                .body("body.emiTypes.find{it.type == 'STANDARD'}.label", equalToIgnoringCase("Standard EMI"))
                .body("body.emiTypes.find{it.type == 'STANDARD'}.cardTypes", not(empty()))
                .body("body.emiTypes.find{it.type == 'STANDARD'}.cardTypes.cardType", containsInAnyOrder("DEBIT_CARD", "CREDIT_CARD", "NBFC"))
                .body("body.emiTypes.find{it.type == 'STANDARD'}.cardTypes.bankDetails", not(empty()))
                .body("body.emiTypes.find{it.type == 'STANDARD'}.cardTypes.bankDetails", everyItem(everyItem(hasKey("bankName"))))
                .body("body.emiTypes.find{it.type == 'STANDARD'}.cardTypes.bankDetails", everyItem(everyItem(hasKey("bankCode"))))
                .body("body.emiTypes.find{it.type == 'STANDARD'}.cardTypes.bankDetails", everyItem(everyItem(hasKey("bankLogoUrl"))))
                .body("body.emiTypes.find{it.type == 'SUBVENTION'}.label", equalToIgnoringCase("Zero/Low Cost EMI"))
                .body("body.emiTypes.find{it.type == 'SUBVENTION'}.cardTypes", not(empty()))
                .body("body.emiTypes.find{it.type == 'SUBVENTION'}.cardTypes.cardType", containsInAnyOrder("DEBIT_CARD", "CREDIT_CARD", "NBFC"))
                .body("body.emiTypes.find{it.type == 'SUBVENTION'}.cardTypes.bankDetails", not(empty()))
                .body("body.emiTypes.find{it.type == 'SUBVENTION'}.cardTypes.bankDetails", everyItem(everyItem(hasKey("bankName"))))
                .body("body.emiTypes.find{it.type == 'SUBVENTION'}.cardTypes.bankDetails", everyItem(everyItem(hasKey("bankCode"))))
                .body("body.emiTypes.find{it.type == 'SUBVENTION'}.cardTypes.bankDetails", everyItem(everyItem(hasKey("bankLogoUrl"))))
                .extract().jsonPath();
    }
    @Test(description = "Verify failure response of /theia/api/v1/emiSubvention/banks when SSO token is not passed")
    public void verifyBankhitsDiscoveryAPIwithoutSSO() throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.EMI_DISCOVERY;
        ApiV1BanksRequest req = new ApiV1BanksRequest.Builder()
                .setMid(merchantType.getId())
                .setTokenType("SSO")
                .setSubventionAmount("100")
                .setPrice("100")
                .setItems(null)
                .build();

        ApiV1Bank api = new ApiV1Bank(merchantType.getId(), req);
        Response response = api.execute();
        JsonPath jsonPath = response.then()
                .spec(REQUEST_PARAMETER_NOT_VALID)
                .extract().jsonPath();
    }
    @Test(description = "Verify failure response of /theia/api/v1/emiSubvention/banks when wrong SSO token is passed")
    public void verifyBankhitsDiscoveryAPIwithWrongSSO() throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.EMI_DISCOVERY;
        ApiV1BanksRequest req = new ApiV1BanksRequest.Builder()
                .setMid(merchantType.getId())
                .setTokenType("SSO")
                .setToken("12342343")
                .setSubventionAmount("100")
                .setPrice("100")
                .setItems(null)
                .build();

        ApiV1Bank api = new ApiV1Bank(merchantType.getId(), req);
        Response response = api.execute();
        JsonPath jsonPath = response.then()
                .spec(SSO_TOKEN_INVALID)
                .extract().jsonPath();
    }

    @Test(description = "Verify success response of /theia/api/v1/emiSubvention/tenure when SSO tokenType is passed")
    public void verifyTenurehitsDiscoveryAPIwithSSO() throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder()
                .setMid(merchantType.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setPrice("100")
                .setSubventionAmount("100")
                .setItems(null)
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();

        System.out.println(req);

        ApiV1Tenure api = new ApiV1Tenure(merchantType.getId(), req);
        Response response = api.execute();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, merchantType.getId(), "AFFORDABILITY_PLATFORM_DISCOVERY", "REQUEST");
        Assertions.assertThat(logs).contains("FUNCTION", "ads/v2/offer/discovery");
        Assertions.assertThat(logs).contains("\"withCalculation\":true");
        response.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.bankCode", equalToIgnoringCase("HDFC"),
                        "body.bankLogoUrl", not(empty()),
                        "body.bankName", equalToIgnoringCase("HDFC Bank Credit Card"),
                        "body.cardType", equalToIgnoringCase("CREDIT_CARD"),
                        "body.planDetails", not(empty()))
                .body("body.planDetails.emiType", hasItems("SUBVENTION"))
                .body("body.planDetails.find{it.emiType == 'SUBVENTION'}", hasKey("effectivePrice"),
                        "body.planDetails.find{it.emiType == 'SUBVENTION'}", hasKey("emi"),
                        "body.planDetails.find{it.emiType == 'SUBVENTION'}", hasKey("emiLabel"),
                        "body.planDetails.find{it.emiType == 'SUBVENTION'}", hasKey("finalTransactionAmount"),
                        "body.planDetails.find{it.emiType == 'SUBVENTION'}", hasKey("interest"),
                        "body.planDetails.find{it.emiType == 'SUBVENTION'}", hasKey("interval"),
                        "body.planDetails.find{it.emiType == 'SUBVENTION'}", hasKey("pgPlanId"),
                        "body.planDetails.find{it.emiType == 'SUBVENTION'}", hasKey("planId"),
                        "body.planDetails.find{it.emiType == 'SUBVENTION'}", hasKey("rate"),
                        "body.planDetails.find{it.emiType == 'SUBVENTION'}", hasKey("gratifications"),
                        "body.planDetails.find{it.emiType == 'SUBVENTION'}", hasKey("itemBreakUp"))
                .body("body.planDetails.find{it.emiType == 'SUBVENTION'}.gratifications", everyItem(hasKey("label")),
                        "body.planDetails.find{it.emiType == 'SUBVENTION'}.gratifications", everyItem(hasKey("type")),
                        "body.planDetails.find{it.emiType == 'SUBVENTION'}.gratifications", everyItem(hasKey("value")))
                .body("body.planDetails.find{it.emiType == 'SUBVENTION'}.itemBreakUp", everyItem(hasKey("emi")),
                        "body.planDetails.find{it.emiType == 'SUBVENTION'}.itemBreakUp", everyItem(hasKey("id")),
                        "body.planDetails.find{it.emiType == 'SUBVENTION'}.itemBreakUp", everyItem(hasKey("interest")),
                        "body.planDetails.find{it.emiType == 'SUBVENTION'}.itemBreakUp", everyItem(hasKey("offerId")),
                        "body.planDetails.find{it.emiType == 'SUBVENTION'}.itemBreakUp", everyItem(hasKey("subventionType")));
    }
    @Test(description = "Verify failure response of /theia/api/v1/emiSubvention/tenure when SSO token is not passed")
    public void verifyTenurehitsDiscoveryAPIwithoutSSO() throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.EMI_DISCOVERY;
        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder()
                .setMid(merchantType.getId())
                .setTokenType("SSO")
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();

        ApiV1Tenure api = new ApiV1Tenure(merchantType.getId(), req);
        Response response = api.execute();
        response.then()
                .spec(REQUEST_PARAMETER_NOT_VALID);
    }
    @Test(description = "Verify failure response of /theia/api/v1/emiSubvention/tenure when tokenType is not passed")
    public void verifyTenurehitsDiscoveryAPIwithouttokenType() throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.EMI;
        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder()
                .setMid(merchantType.getId())
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();

        ApiV1Tenure api = new ApiV1Tenure(merchantType.getId(), req);
        Response r = api.execute();
        r.then()
                .spec(TOKEN_TYPE_NOT_SUPPORTED);
    }
    @Test(description = "Verify success response of /theia/api/v1/emiSubvention/tenure when CHECKSUM tokenType is passed")
    public void verifyTenurehitsDiscoveryAPIwithChecksum() throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.EMI_DISCOVERY;
        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder()
                .setMid(merchantType.getId())
                .setMerchantKey(merchantType.getKey())
                .setTokenType("CHECKSUM")
                .setPrice("1000")
                .setSubventionAmount("1000")
                .setItems(null)
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();

        ApiV1Tenure api = new ApiV1Tenure(merchantType.getId(), req);
        Response response = api.execute();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, merchantType.getId(), "AFFORDABILITY_PLATFORM_DISCOVERY", "REQUEST");
        Assertions.assertThat(logs).contains("FUNCTION", "ads/v2/offer/discovery");
        Assertions.assertThat(logs).contains("\"withCalculation\":true");
        response.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.bankCode", equalToIgnoringCase("HDFC"),
                        "body.bankLogoUrl", not(empty()),
                        "body.bankName", equalToIgnoringCase("HDFC Bank Credit Card"),
                        "body.cardType", equalToIgnoringCase("CREDIT_CARD"),
                        "body.planDetails", not(empty()))
                .body("body.planDetails.emiType", hasItems("SUBVENTION"))
                .body("body.planDetails.find{it.emiType == 'SUBVENTION'}", hasKey("effectivePrice"),
                        "body.planDetails.find{it.emiType == 'SUBVENTION'}", hasKey("emi"),
                        "body.planDetails.find{it.emiType == 'SUBVENTION'}", hasKey("emiLabel"),
                        "body.planDetails.find{it.emiType == 'SUBVENTION'}", hasKey("finalTransactionAmount"),
                        "body.planDetails.find{it.emiType == 'SUBVENTION'}", hasKey("interest"),
                        "body.planDetails.find{it.emiType == 'SUBVENTION'}", hasKey("interval"),
                        "body.planDetails.find{it.emiType == 'SUBVENTION'}", hasKey("pgPlanId"),
                        "body.planDetails.find{it.emiType == 'SUBVENTION'}", hasKey("planId"),
                        "body.planDetails.find{it.emiType == 'SUBVENTION'}", hasKey("rate"),
                        "body.planDetails.find{it.emiType == 'SUBVENTION'}", hasKey("gratifications"),
                        "body.planDetails.find{it.emiType == 'SUBVENTION'}", hasKey("itemBreakUp"))
                .body("body.planDetails.find{it.emiType == 'SUBVENTION'}.gratifications", everyItem(hasKey("label")),
                        "body.planDetails.find{it.emiType == 'SUBVENTION'}.gratifications", everyItem(hasKey("type")),
                        "body.planDetails.find{it.emiType == 'SUBVENTION'}.gratifications", everyItem(hasKey("value")))
                .body("body.planDetails.find{it.emiType == 'SUBVENTION'}.itemBreakUp", everyItem(hasKey("emi")),
                        "body.planDetails.find{it.emiType == 'SUBVENTION'}.itemBreakUp", everyItem(hasKey("id")),
                        "body.planDetails.find{it.emiType == 'SUBVENTION'}.itemBreakUp", everyItem(hasKey("interest")),
                        "body.planDetails.find{it.emiType == 'SUBVENTION'}.itemBreakUp", everyItem(hasKey("offerId")),
                        "body.planDetails.find{it.emiType == 'SUBVENTION'}.itemBreakUp", everyItem(hasKey("subventionType")));
    }
    @Test(description = "Verify success response of /theia/api/v1/emiSubvention/validateEmi when SSO token is passed and generateTokenForIntent is false")
    public void verifyValidateEMIhitsDiscoveryAPIwithSSO() throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder()
                .setMid(merchantType.getId())
                .setMerchantKey(merchantType.getKey())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setPrice("100")
                .setSubventionAmount("100")
                .setItems(null)
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();

        ApiV1Tenure api = new ApiV1Tenure(merchantType.getId(), req);
        Response response = api.execute();
        JsonPath tenureResp = response.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.planDetails.emiType", hasItems("SUBVENTION"))
                .extract().jsonPath();

        String planId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId");

        BigInteger intplanid= new BigInteger(planId);
        ApiV1ValidateRequest req2 = new ApiV1ValidateRequest.Builder("", "CHECKSUM", merchantType.getId())
                .setMerchantKey(merchantType.getKey())
                .setPlanId(intplanid)
                .setPrice("100")
                .setSubventionAmount("100")
                .setItems(null)
                .setOfferDetails(new OfferDetails().setOfferId("2378530"))
                .setPaymentDetails(
                        new PaymentDetails()
                                .setTotalTransactionAmount("100")
                                .setCardNumber("|" + PGPHelpers.getFormattedPaymentDetails("EMI", new PaymentDTO())))
                .setGenerateTokenForIntent(false)
                .build();

        ApiV1Validate api2 = new ApiV1Validate(merchantType.getId(), req2);
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, merchantType.getId(), "AFFORDABILITY_PLATFORM_DISCOVERY", "REQUEST");
        Assertions.assertThat(logs).contains("FUNCTION", "ads/v2/offer/discovery");
        Response response1 = api2.execute();
        response1.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.emiSubventionToken", nullValue());
    }
    @Test(description = "Verify failure response of /theia/api/v1/emiSubvention/validateEmi when SSO token is passed and paymentDetails is not correct")
    public void verifyValidateEMIhitsDiscoveryAPIInvalidCard() throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder()
                .setMid(merchantType.getId())
                .setMerchantKey(merchantType.getKey())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setSubventionAmount("100")
                .setPrice("100")
                .setItems(null)
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();

        ApiV1Tenure api = new ApiV1Tenure(merchantType.getId(), req);
        Response response = api.execute();
        JsonPath tenureResp = response.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.planDetails.emiType", hasItems("SUBVENTION"))
                .extract().jsonPath();

        String planId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId");
        BigInteger intplanid= new BigInteger(planId);
        ApiV1ValidateRequest req2 = new ApiV1ValidateRequest.Builder("", "CHECKSUM", merchantType.getId())
                .setMerchantKey(merchantType.getKey())
                .setPlanId(intplanid)
                .setSubventionAmount("100")
                .setPrice("100")
                .setItems(null)
                .setOfferDetails(new OfferDetails().setOfferId("2352888"))
                .setPaymentDetails(
                        new PaymentDetails()
                                .setTotalTransactionAmount("100")
                                .setCardNumber("|1" + PGPHelpers.getFormattedPaymentDetails("EMI", new PaymentDTO())))
                .setGenerateTokenForIntent(true)
                .build();

        ApiV1Validate api2 = new ApiV1Validate(merchantType.getId(), req2);
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, merchantType.getId(), "AFFORDABILITY_PLATFORM_DISCOVERY", "REQUEST");
        Assertions.assertThat(logs).contains("FUNCTION", "ads/v2/offer/discovery");
        Response r2 = api2.execute();
        r2.then()
                .spec(CARD_DETAIL_NOT_VALID);
    }
    @Test(description = "Verify failure response of /theia/api/v1/emiSubvention/validateEmi when SSO token is passed and totalTransaction is less than item")
    public void verifyValidateEMIhitsDiscoveryAPIforWrongAmount() throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder()
                .setMid(merchantType.getId())
                .setMerchantKey(merchantType.getKey())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setSubventionAmount("100")
                .setPrice("100")
                .setItems(null)
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();

        ApiV1Tenure api = new ApiV1Tenure(merchantType.getId(), req);
        Response response = api.execute();
        JsonPath tenureResp = response.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.planDetails.emiType", hasItems("SUBVENTION"))
                .extract().jsonPath();

        String planId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId");
        BigInteger intplanid= new BigInteger(planId);
        ApiV1ValidateRequest req2 = new ApiV1ValidateRequest.Builder("", "CHECKSUM", merchantType.getId())
                .setMerchantKey(merchantType.getKey())
                .setPlanId(intplanid)
                .setSubventionAmount("100")
                .setPrice("100")
                .setItems(null)
                .setOfferDetails(new OfferDetails().setOfferId("2352888"))
                .setPaymentDetails(
                        new PaymentDetails()
                                .setTotalTransactionAmount("90")
                                .setCardNumber("|" + PGPHelpers.getFormattedPaymentDetails("EMI", new PaymentDTO())))
                .setGenerateTokenForIntent(true)
                .build();

        ApiV1Validate api2 = new ApiV1Validate(merchantType.getId(), req2);
        Response r2 = api2.execute();
        r2.then()
                .statusCode(200)
                .body("body.resultInfo.resultStatus", equalToIgnoringCase("F"),
                        "body.resultInfo.resultCode", equalToIgnoringCase("1007"),
                        "body.resultInfo.resultMsg", equalToIgnoringCase("items total amount  does not match the totalTransaction Amount"));
    }


    @Test(description = "Verify  CHECKOUT API is being called by theia and when PTC is executed and transaction is successful")
    public void verifyCheckoutAPIhitsDiscoveryAPI(@Optional("false") boolean isNativePlus) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String unqId = CommonHelpers.generateOrderId();
        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder()
                .setMid(merchantType.getId())
                .setMerchantKey(merchantType.getKey())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setSubventionAmount("100")
                .setPrice("100")
                .setItems(null)
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();

        String planId = new ApiV1Tenure(merchantType.getId(), req).execute()
                .then().spec(SUCCESS_RESPONSE)
                .body("body.planDetails.emiType", hasItems("SUBVENTION"))
                .extract().jsonPath().getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId");

        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(paymentDTO.getEmiCard());
        BigInteger intplanid = new BigInteger(planId);
        ApiV1ValidateRequest req2 = new ApiV1ValidateRequest.Builder(user.ssoToken(), "SSO", merchantType.getId())
                .setMerchantKey(merchantType.getKey())
                .setPlanId(intplanid)
                .setCustomerId(unqId)
                .setSubventionAmount("100")
                .setPrice("100")
                .setItems(null)
                .setOfferDetails(new OfferDetails().setOfferId("2378530"))
                .setPaymentDetails(
                        new PaymentDetails()
                                .setTotalTransactionAmount("100")
                                .setCardNumber("|" + PGPHelpers.getFormattedPaymentDetails("EMI", paymentDTO)))
                .setGenerateTokenForIntent(true)
                .build();
        JsonPath validateResp = new ApiV1Validate(merchantType.getId(), req2).execute()
                .then()
                .spec(SUCCESS_RESPONSE)
                .body("body", hasKey("emiSubventionToken"))
                .extract().jsonPath();
        String emiSubventionToken = validateResp.getString("body.emiSubventionToken");
        String pgPlanId = validateResp.getString("body.pgPlanId");

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.EMI_DISCOVERY)
                .setEmiSubventionToken(emiSubventionToken)
                .setTxnValue("100")
                .setPayableAmount(new TxnAmount("93"))
                .build();
        String txnToken = new InitTxn(initTxnDTO).execute().then()
                .spec(SUCCESS_RESPONSE)
                .body("body.txnToken", not(empty()))
                .extract().jsonPath().getString("body.txnToken");

        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.EMI_DISCOVERY, initTxnDTO.orderFromBody(), txnToken, paymentDTO, PayMethodType.EMI)
                .setChannelCode(Constants.Bank.HDFC.toString())
                .setPlanId(pgPlanId)
                .setAUTH_MODE("otp")
                .setCallBack_URL("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setREQUEST_TYPE("NATIVE")
                .setWEBSITE("retail")
                .setEMI_TYPE("CREDIT_CARD")
                .setINDUSTRY_TYPE_ID("Retail")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, merchantType.getId(), "AFFORDABILITY_PLATFORM", "REQUEST");
        Assertions.assertThat(logs).contains("FUNCTION", "ats/v2/order/checkout");
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount("93")
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }
    @Test(description = "Bank API : Verify the response if one of the request param for amount based strategy is not sent in the request (\"price\",\"subventionAmount\")",groups = "P1")
    public void t30bankAPIPhase2() throws Exception {
        Constants.MerchantType emiMerchant = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String subventionAmount = "200";

        ApiV1BanksRequest apiV1BanksRequest = new ApiV1BanksRequest.Builder()
                .setMid(emiMerchant.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setSubventionAmount(subventionAmount)
                .setItems(new ArrayList<>())
                .build();

        ApiV1Bank api = new ApiV1Bank(emiMerchant.getId(), apiV1BanksRequest);
        Response response = api.execute();
        JsonPath jsonPath = response.then()
                .spec(INVALID_AMOUNT_DETAILS)
                .extract().jsonPath();

    }
    @Test(description = "Bank API : Verify the response if product based request param i.e items is sent in the request along with amount details",groups = "P1")
    public void t31bankAPIPhase2() throws Exception {
        Constants.MerchantType emiMerchant = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String price ="1000";
        String subventionAmount = "200";

        ApiV1BanksRequest apiV1BanksRequest = new ApiV1BanksRequest.Builder()
                .setMid(emiMerchant.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setPrice(price)
                .setSubventionAmount(subventionAmount)
                .build();

        ApiV1Bank api = new ApiV1Bank(emiMerchant.getId(), apiV1BanksRequest);
        Response response = api.execute();
        JsonPath jsonPath = response.then()
                .body("body.resultInfo.resultStatus",equalTo("F"))
                .body("body.resultInfo.resultCode",equalTo("1007"))
                .body("body.resultInfo.resultMsg", equalTo("Both item list and amount cannot be non empty in request"))
                .extract().jsonPath();

    }

    //ItemBased
    @Test(description = "Verify success response of /theia/api/v1/emiSubvention/banks when SSO tokenType is passed")
    public void verifyBankhitsDiscoveryAPIwithSSOItem() throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        List<String> category =new ArrayList<>();
        category.add("6224");
        com.paytm.dto.emiSubvention.ApiV1Banks.request.Item itemm =new com.paytm.dto.emiSubvention.ApiV1Banks.request.Item();
        itemm.setProductId("321067334");
        itemm.setCategoryList(category);
        itemm.setModel("G531BT-BQ002T");
        itemm.setBrandId("18084");
        itemm.setPrice(100.0);
        itemm.setId("1");
        List<com.paytm.dto.emiSubvention.ApiV1Banks.request.Item> items = new ArrayList<>();
        items.add(itemm);
        ApiV1BanksRequest req = new ApiV1BanksRequest.Builder()
                .setMid(merchantType.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setItems(items)
                .build();

        ApiV1Bank api = new ApiV1Bank(merchantType.getId(), req);
        Response response = api.execute();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, merchantType.getId(), "AFFORDABILITY_PLATFORM_DISCOVERY", "REQUEST");
        Assertions.assertThat(logs).contains("FUNCTION", "ads/v2/offer/discovery");
        Assertions.assertThat(logs).contains("\"withCalculation\":false");
        JsonPath jsonPath = response.then()
                .body("body.emiTypes", not(empty()))
                .body("body.emiTypes.type", containsInAnyOrder("STANDARD", "SUBVENTION"))
                .body("body.emiTypes.find{it.type == 'STANDARD'}.label", equalToIgnoringCase("Standard EMI"))
                .body("body.emiTypes.find{it.type == 'STANDARD'}.cardTypes", not(empty()))
                .body("body.emiTypes.find{it.type == 'STANDARD'}.cardTypes.cardType", containsInAnyOrder("DEBIT_CARD", "CREDIT_CARD", "NBFC"))
                .body("body.emiTypes.find{it.type == 'STANDARD'}.cardTypes.bankDetails", not(empty()))
                .body("body.emiTypes.find{it.type == 'STANDARD'}.cardTypes.bankDetails", everyItem(everyItem(hasKey("bankName"))))
                .body("body.emiTypes.find{it.type == 'STANDARD'}.cardTypes.bankDetails", everyItem(everyItem(hasKey("bankCode"))))
                .body("body.emiTypes.find{it.type == 'STANDARD'}.cardTypes.bankDetails", everyItem(everyItem(hasKey("bankLogoUrl"))))
                .body("body.emiTypes.find{it.type == 'SUBVENTION'}.label", equalToIgnoringCase("Zero/Low Cost EMI"))
                .body("body.emiTypes.find{it.type == 'SUBVENTION'}.cardTypes", not(empty()))
                .body("body.emiTypes.find{it.type == 'SUBVENTION'}.cardTypes.cardType", containsInAnyOrder("DEBIT_CARD", "CREDIT_CARD", "NBFC"))
                .body("body.emiTypes.find{it.type == 'SUBVENTION'}.cardTypes.bankDetails", not(empty()))
                .body("body.emiTypes.find{it.type == 'SUBVENTION'}.cardTypes.bankDetails", everyItem(everyItem(hasKey("bankName"))))
                .body("body.emiTypes.find{it.type == 'SUBVENTION'}.cardTypes.bankDetails", everyItem(everyItem(hasKey("bankCode"))))
                .body("body.emiTypes.find{it.type == 'SUBVENTION'}.cardTypes.bankDetails", everyItem(everyItem(hasKey("bankLogoUrl"))))
                .extract().jsonPath();
    }
    @Test(description = "Verify success response of /theia/api/v1/emiSubvention/tenure when SSO tokenType is passed")
    public void verifyTenurehitsDiscoveryAPIwithSSOItem() throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        List<String> category =new ArrayList<>();
        category.add("6224");
        com.paytm.dto.emiSubvention.ApiV1Banks.request.Item itemm =new com.paytm.dto.emiSubvention.ApiV1Banks.request.Item();
        itemm.setProductId("321067334");
        itemm.setCategoryList(category);
        itemm.setModel("G531BT-BQ002T");
        itemm.setBrandId("18084");
        itemm.setPrice(100.0);
        itemm.setId("1");
        List<com.paytm.dto.emiSubvention.ApiV1Banks.request.Item> items = new ArrayList<>();
        items.add(itemm);
        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder()
                .setMid(merchantType.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setItems(items)
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();

        System.out.println(req);

        ApiV1Tenure api = new ApiV1Tenure(merchantType.getId(), req);
        Response response = api.execute();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, merchantType.getId(), "AFFORDABILITY_PLATFORM_DISCOVERY", "REQUEST");
        Assertions.assertThat(logs).contains("FUNCTION", "ads/v2/offer/discovery");
        Assertions.assertThat(logs).contains("\"withCalculation\":true");
        response.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.bankCode", equalToIgnoringCase("HDFC"),
                        "body.bankLogoUrl", not(empty()),
                        "body.bankName", equalToIgnoringCase("HDFC Bank Credit Card"),
                        "body.cardType", equalToIgnoringCase("CREDIT_CARD"),
                        "body.planDetails", not(empty()))
                .body("body.planDetails.emiType", hasItems("SUBVENTION"))
                .body("body.planDetails.find{it.emiType == 'SUBVENTION'}", hasKey("effectivePrice"),
                        "body.planDetails.find{it.emiType == 'SUBVENTION'}", hasKey("emi"),
                        "body.planDetails.find{it.emiType == 'SUBVENTION'}", hasKey("emiLabel"),
                        "body.planDetails.find{it.emiType == 'SUBVENTION'}", hasKey("finalTransactionAmount"),
                        "body.planDetails.find{it.emiType == 'SUBVENTION'}", hasKey("interest"),
                        "body.planDetails.find{it.emiType == 'SUBVENTION'}", hasKey("interval"),
                        "body.planDetails.find{it.emiType == 'SUBVENTION'}", hasKey("pgPlanId"),
                        "body.planDetails.find{it.emiType == 'SUBVENTION'}", hasKey("planId"),
                        "body.planDetails.find{it.emiType == 'SUBVENTION'}", hasKey("rate"),
                        "body.planDetails.find{it.emiType == 'SUBVENTION'}", hasKey("gratifications"),
                        "body.planDetails.find{it.emiType == 'SUBVENTION'}", hasKey("itemBreakUp"))
                .body("body.planDetails.find{it.emiType == 'SUBVENTION'}.gratifications", everyItem(hasKey("label")),
                        "body.planDetails.find{it.emiType == 'SUBVENTION'}.gratifications", everyItem(hasKey("type")),
                        "body.planDetails.find{it.emiType == 'SUBVENTION'}.gratifications", everyItem(hasKey("value")))
                .body("body.planDetails.find{it.emiType == 'SUBVENTION'}.itemBreakUp", everyItem(hasKey("emi")),
                        "body.planDetails.find{it.emiType == 'SUBVENTION'}.itemBreakUp", everyItem(hasKey("id")),
                        "body.planDetails.find{it.emiType == 'SUBVENTION'}.itemBreakUp", everyItem(hasKey("interest")),
                        "body.planDetails.find{it.emiType == 'SUBVENTION'}.itemBreakUp", everyItem(hasKey("offerId")),
                        "body.planDetails.find{it.emiType == 'SUBVENTION'}.itemBreakUp", everyItem(hasKey("subventionType")));
    }

    @Test(description = "Verify success response of /theia/api/v1/emiSubvention/validateEmi when SSO token is passed and generateTokenForIntent is false")
    public void verifyValidateEMIhitsDiscoveryAPIwithSSOItem() throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        List<String> category =new ArrayList<>();
        category.add("6224");
        com.paytm.dto.emiSubvention.ApiV1Banks.request.Item itemm =new com.paytm.dto.emiSubvention.ApiV1Banks.request.Item();
        itemm.setProductId("321067334");
        itemm.setCategoryList(category);
        itemm.setModel("G531BT-BQ002T");
        itemm.setBrandId("18084");
        itemm.setPrice(100.0);
        itemm.setId("1");
        List<com.paytm.dto.emiSubvention.ApiV1Banks.request.Item> items = new ArrayList<>();
        items.add(itemm);
        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder()
                .setMid(merchantType.getId())
                .setMerchantKey(merchantType.getKey())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setItems(items)
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();

        ApiV1Tenure api = new ApiV1Tenure(merchantType.getId(), req);
        Response response = api.execute();
        JsonPath tenureResp = response.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.planDetails.emiType", hasItems("SUBVENTION"))
                .extract().jsonPath();

        String planId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId");

        BigInteger intplanid= new BigInteger(planId);
        com.paytm.dto.emiSubvention.ApiV1Validate.request.Item itemm1 =new com.paytm.dto.emiSubvention.ApiV1Validate.request.Item();
        itemm1.setProductId("321067334");
        itemm1.setCategoryList(category);
        itemm1.setModel("G531BT-BQ002T");
        itemm1.setBrandId("18084");
        itemm1.setPrice(100.0);
        itemm1.setId("1");
        List<com.paytm.dto.emiSubvention.ApiV1Validate.request.Item> items1 = new ArrayList<>();
        items1.add(itemm1);

        ApiV1ValidateRequest req2 = new ApiV1ValidateRequest.Builder("", "CHECKSUM", merchantType.getId())
                .setMerchantKey(merchantType.getKey())
                .setPlanId(intplanid)
                .setItems(items1)
                .setOfferDetails(new OfferDetails().setOfferId("2384097"))
                .setPaymentDetails(
                        new PaymentDetails()
                                .setTotalTransactionAmount("100")
                                .setCardNumber("|" + PGPHelpers.getFormattedPaymentDetails("EMI", new PaymentDTO())))
                .setGenerateTokenForIntent(false)
                .build();

        ApiV1Validate api2 = new ApiV1Validate(merchantType.getId(), req2);
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, merchantType.getId(), "AFFORDABILITY_PLATFORM_DISCOVERY", "REQUEST");
        Assertions.assertThat(logs).contains("FUNCTION", "ads/v2/offer/discovery");
        Response response1 = api2.execute();
        response1.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.emiSubventionToken", nullValue());
    }

    @Test(description = "Verify  CHECKOUT API is being called by theia and when PTC is executed and transaction is successful")
    public void verifyCheckoutAPIhitsDiscoveryAPIItem(@Optional("false") boolean isNativePlus) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PG2_AMEX_EMI;
        User user = userManager.getForRead(Label.BASIC);
        String unqId = CommonHelpers.generateOrderId();
        List<String> category =new ArrayList<>();
        category.add("6224");
        com.paytm.dto.emiSubvention.ApiV1Banks.request.Item itemm =new com.paytm.dto.emiSubvention.ApiV1Banks.request.Item();
        itemm.setProductId("123");
        itemm.setCategoryList(category);
        itemm.setModel("G531BT-BQ002T");
        itemm.setBrandId("18084");
        itemm.setPrice(100.0);
        itemm.setId("1");
        List<com.paytm.dto.emiSubvention.ApiV1Banks.request.Item> items = new ArrayList<>();
        items.add(itemm);
        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder()
                .setMid(merchantType.getId())
                .setMerchantKey(merchantType.getKey())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setItems(items)
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();

        String planId = new ApiV1Tenure(merchantType.getId(), req).execute()
                .then().spec(SUCCESS_RESPONSE)
                .body("body.planDetails.emiType", hasItems("SUBVENTION"))
                .extract().jsonPath().getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId");

        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(paymentDTO.getEmiCard());
        BigInteger intplanid = new BigInteger(planId);
        com.paytm.dto.emiSubvention.ApiV1Validate.request.Item itemm1 =new com.paytm.dto.emiSubvention.ApiV1Validate.request.Item();
        itemm1.setProductId("123");
        itemm1.setCategoryList(category);
        itemm1.setModel("G531BT-BQ002T");
        itemm1.setBrandId("18084");
        itemm1.setPrice(100.0);
        itemm1.setId("1");
        itemm1.setQuantity(1.0);
        itemm1.setOfferDetails(new OfferDetails().setOfferId("2193577"));
        List<com.paytm.dto.emiSubvention.ApiV1Validate.request.Item> items1 = new ArrayList<>();
        items1.add(itemm1);
        ApiV1ValidateRequest req2 = new ApiV1ValidateRequest.Builder(user.ssoToken(), "SSO", merchantType.getId())
                .setMerchantKey(merchantType.getKey())
                .setPlanId(intplanid)
                .setCustomerId(unqId)
                .setItems(items1)
                .setPaymentDetails(
                        new PaymentDetails()
                                .setTotalTransactionAmount("100")
                                .setCardNumber("|" + PGPHelpers.getFormattedPaymentDetails("EMI", paymentDTO)))
                .setGenerateTokenForIntent(true)
                .build();
        JsonPath validateResp = new ApiV1Validate(merchantType.getId(), req2).execute()
                .then()
                .spec(SUCCESS_RESPONSE)
                .body("body", hasKey("emiSubventionToken"))
                .extract().jsonPath();
        String emiSubventionToken = validateResp.getString("body.emiSubventionToken");
        String pgPlanId = validateResp.getString("body.pgPlanId");

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.PG2_AMEX_EMI)
                .setEmiSubventionToken(emiSubventionToken)
                .setTxnValue("100")
                .setPayableAmount(new TxnAmount("100"))
                .build();
        String txnToken = new InitTxn(initTxnDTO).execute().then()
                .spec(SUCCESS_RESPONSE)
                .body("body.txnToken", not(empty()))
                .extract().jsonPath().getString("body.txnToken");

        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.PG2_AMEX_EMI, initTxnDTO.orderFromBody(), txnToken, paymentDTO, PayMethodType.EMI)
                .setChannelCode(Constants.Bank.HDFC.toString())
                .setPlanId(pgPlanId)
                .setAUTH_MODE("otp")
                .setCallBack_URL("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setREQUEST_TYPE("NATIVE")
                .setWEBSITE("retail")
                .setEMI_TYPE("CREDIT_CARD")
                .setINDUSTRY_TYPE_ID("Retail")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, merchantType.getId(), "AFFORDABILITY_PLATFORM", "REQUEST");
        Assertions.assertThat(logs).contains("FUNCTION", "ats/v2/order/checkout");
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount("50.0")
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

}