package scripts.api.theia.emiSubvention;

import com.paytm.LocalConfig;
import com.paytm.api.GetPaymentStatus;
import com.paytm.api.Peon;
import com.paytm.api.TxnStatus;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.api.theia.ApiV1ApplyPromo;
import com.paytm.api.theia.emiSubvention.ApiV1Bank;
import com.paytm.api.theia.emiSubvention.ApiV1Tenure;
import com.paytm.api.theia.emiSubvention.ApiV1Validate;
import com.paytm.appconstants.Constants;
import com.paytm.appconstants.Constants.MerchantType;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.apphelpers.SavedCardHelpers;
import com.paytm.base.test.Group;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.GetPaymentStatusRequest.GetPaymentStatusDTO;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.Item;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.PaymentOffersApplied;
import com.paytm.dto.NativeDTO.InitTxn.TxnAmount;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.PeonResponse;
import com.paytm.dto.emiSubvention.ApiV1Banks.request.ApiV1BanksRequest;
import com.paytm.dto.emiSubvention.ApiV1Tenure.request.ApiV1TenureRequest;
import com.paytm.dto.emiSubvention.ApiV1Tenure.request.Filters;
import com.paytm.dto.emiSubvention.ApiV1Validate.request.ApiV1ValidateRequest;
import com.paytm.dto.emiSubvention.ApiV1Validate.request.OfferDetails;
import com.paytm.dto.emiSubvention.ApiV1Validate.request.PaymentDetails;
import com.paytm.framework.api.curlloggingutil.CurlLoggingRestAssuredConfigBuilder;
import com.paytm.framework.reporting.Owners;
import com.paytm.framework.reporting.filters.RequestResponseLoggingFilter;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
import com.paytm.utils.ff4j.FF4JFlags;
import com.paytm.utils.merchant.api.pgp.theia.paytm_express.GetCardToken;
import com.paytm.utils.merchant.merchant.util.Merchant;
import com.paytm.utils.merchant.merchant.util.Promo;
import com.paytm.utils.merchant.util.PayMethodType;
import io.qameta.allure.*;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.assertj.core.api.Assertions;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.math.BigInteger;
import java.util.*;

import static com.paytm.appconstants.Constants.Owner.GAGANDEEP;
import static com.paytm.dto.PaymentDTO.BAJAJ_FINSERV_CREDIT_CARD_NUMBER;
import static com.paytm.dto.PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * For execution of EMI_SUBVENTION on QA environment following are the
 * pre-requisites:
 * mock:
 * theia --> facade.properties --> emi.subvention.base.url=http://10.144.18.102:8088/mockbank/emi
 * notification-queue-handler --> facade.properties --> emi.subvention.base.url=http://10.144.18.102:8088/mockbank/emi
 */

@Epic(Constants.Sprint.SPRINT30_1)
@Feature("PGP-16623")
@Owner("Tarun")
@Owners(author = "Tarun", qa = "Ankur")
public class EmiSubventionApiTests extends PGPBaseTest {

    private final CheckoutPage checkoutPage = new CheckoutPage();

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

    private final static ResponseSpecification CHECKSUM_PROVIDED_IS_INVALID = new ResponseSpecBuilder()
            .expectStatusCode(200)
            .expectBody("body.resultInfo.resultStatus", equalToIgnoringCase("F"))
            .expectBody("body.resultInfo.resultCode", equalToIgnoringCase("1001"))
            .expectBody("body.resultInfo.resultMsg", equalToIgnoringCase("Checksum provided is invalid"))
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
                .setConfig(new CurlLoggingRestAssuredConfigBuilder().build())
                .build();
    }


    @Test(description = "Verify success response of /theia/api/v1/emiSubvention/banks when SSO tokenType is passed")
    public void t1() throws Exception {
        MerchantType m = MerchantType.EMI;
        User user = userManager.getForRead(Label.BASIC);
        ApiV1BanksRequest req = new ApiV1BanksRequest.Builder()
                .setMid(m.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();

        ApiV1Bank api = new ApiV1Bank(m.getId(), req);
        Response r = api.execute();
        JsonPath j = r.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.emiTypes", not(empty()))
                .body("body.emiTypes.type", containsInAnyOrder("STANDARD", "SUBVENTION"))
                .body("body.emiTypes.find{it.type == 'STANDARD'}.label", equalToIgnoringCase("Standard EMI"))
                .body("body.emiTypes.find{it.type == 'STANDARD'}.cardTypes", not(empty()))
                .body("body.emiTypes.find{it.type == 'STANDARD'}.cardTypes.cardType", containsInAnyOrder("DEBIT_CARD", "CREDIT_CARD"))
                .body("body.emiTypes.find{it.type == 'STANDARD'}.cardTypes.bankDetails", not(empty()))
                .body("body.emiTypes.find{it.type == 'STANDARD'}.cardTypes.bankDetails", everyItem(everyItem(hasKey("bankName"))))
                .body("body.emiTypes.find{it.type == 'STANDARD'}.cardTypes.bankDetails", everyItem(everyItem(hasKey("bankCode"))))
                .body("body.emiTypes.find{it.type == 'STANDARD'}.cardTypes.bankDetails", everyItem(everyItem(hasKey("bankLogoUrl"))))
                .body("body.emiTypes.find{it.type == 'SUBVENTION'}.label", equalToIgnoringCase("Zero Cost EMI"))
                .body("body.emiTypes.find{it.type == 'SUBVENTION'}.cardTypes", not(empty()))
                .body("body.emiTypes.find{it.type == 'SUBVENTION'}.cardTypes.cardType", containsInAnyOrder("CREDIT_CARD"))
                .body("body.emiTypes.find{it.type == 'SUBVENTION'}.cardTypes.bankDetails", not(empty()))
                .body("body.emiTypes.find{it.type == 'SUBVENTION'}.cardTypes.bankDetails", everyItem(everyItem(hasKey("bankName"))))
                .body("body.emiTypes.find{it.type == 'SUBVENTION'}.cardTypes.bankDetails", everyItem(everyItem(hasKey("bankCode"))))
                .body("body.emiTypes.find{it.type == 'SUBVENTION'}.cardTypes.bankDetails", everyItem(everyItem(hasKey("bankLogoUrl"))))
                .extract().jsonPath();
    }

    @Test(description = "Verify success response of /theia/api/v1/emiSubvention/banks when CHECKSUM tokenType is passed")
    public void t2() throws Exception {
        MerchantType m = MerchantType.EMI;
        ApiV1BanksRequest req = new ApiV1BanksRequest.Builder()
                .setMid(m.getId())
                .setMerchantKey(m.getKey())
                .setTokenType("CHECKSUM")
                .build();

        ApiV1Bank api = new ApiV1Bank(m.getId(), req);
        Response r = api.execute();
        JsonPath j = r.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.emiTypes", not(empty()))
                .body("body.emiTypes.type", containsInAnyOrder("STANDARD", "SUBVENTION"))
                .body("body.emiTypes.find{it.type == 'STANDARD'}.label", equalToIgnoringCase("Standard EMI"))
                .body("body.emiTypes.find{it.type == 'STANDARD'}.cardTypes", not(empty()))
                .body("body.emiTypes.find{it.type == 'STANDARD'}.cardTypes.cardType", containsInAnyOrder("DEBIT_CARD", "CREDIT_CARD"))
                .body("body.emiTypes.find{it.type == 'STANDARD'}.cardTypes.bankDetails", not(empty()))
                .body("body.emiTypes.find{it.type == 'STANDARD'}.cardTypes.bankDetails", everyItem(everyItem(hasKey("bankName"))))
                .body("body.emiTypes.find{it.type == 'STANDARD'}.cardTypes.bankDetails", everyItem(everyItem(hasKey("bankCode"))))
                .body("body.emiTypes.find{it.type == 'STANDARD'}.cardTypes.bankDetails", everyItem(everyItem(hasKey("bankLogoUrl"))))
                .body("body.emiTypes.find{it.type == 'SUBVENTION'}.label", equalToIgnoringCase("Zero Cost EMI"))
                .body("body.emiTypes.find{it.type == 'SUBVENTION'}.cardTypes", not(empty()))
                .body("body.emiTypes.find{it.type == 'SUBVENTION'}.cardTypes.cardType", containsInAnyOrder("CREDIT_CARD"))
                .body("body.emiTypes.find{it.type == 'SUBVENTION'}.cardTypes.bankDetails", not(empty()))
                .body("body.emiTypes.find{it.type == 'SUBVENTION'}.cardTypes.bankDetails", everyItem(everyItem(hasKey("bankName"))))
                .body("body.emiTypes.find{it.type == 'SUBVENTION'}.cardTypes.bankDetails", everyItem(everyItem(hasKey("bankCode"))))
                .body("body.emiTypes.find{it.type == 'SUBVENTION'}.cardTypes.bankDetails", everyItem(everyItem(hasKey("bankLogoUrl"))))
                .extract().jsonPath();
    }

    @Test(description = "Verify failure response of /theia/api/v1/emiSubvention/banks when SSO token is not passed")
    public void t3() throws Exception {
        MerchantType m = MerchantType.EMI;
        ApiV1BanksRequest req = new ApiV1BanksRequest.Builder()
                .setMid(m.getId())
                .setTokenType("SSO")
                .build();

        ApiV1Bank api = new ApiV1Bank(m.getId(), req);
        Response r = api.execute();
        JsonPath j = r.then()
                .spec(REQUEST_PARAMETER_NOT_VALID)
                .extract().jsonPath();
    }

    @Test(description = "Verify failure response of /theia/api/v1/emiSubvention/banks when wrong SSO token is passed")
    public void t4() throws Exception {
        MerchantType m = MerchantType.EMI;
        ApiV1BanksRequest req = new ApiV1BanksRequest.Builder()
                .setMid(m.getId())
                .setTokenType("SSO")
                .setToken("12342343")
                .build();

        ApiV1Bank api = new ApiV1Bank(m.getId(), req);
        Response r = api.execute();
        JsonPath j = r.then()
                .spec(SSO_TOKEN_INVALID)
                .extract().jsonPath();
    }

    @Test(description = "Verify failure response of /theia/api/v1/emiSubvention/banks when wrong CHECKSUM token is passed")
    public void t5() throws Exception {
        MerchantType m = MerchantType.EMI;
        ApiV1BanksRequest req = new ApiV1BanksRequest.Builder()
                .setMid(m.getId())
                .setMerchantKey(m.getKey())
                .setTokenType("CHECKSUM")
                .build();
        req.getHead().setToken("asjhdg");

        ApiV1Bank api = new ApiV1Bank(m.getId(), req);
        Response r = api.execute();
        JsonPath j = r.then()
                .spec(CHECKSUM_PROVIDED_IS_INVALID)
                .extract().jsonPath();
    }


    @Test(description = "Verify success response of /theia/api/v1/emiSubvention/tenure when SSO tokenType is passed")
    public void t6() throws Exception {
        MerchantType m = MerchantType.EMI;
        User user = userManager.getForRead(Label.BASIC);
        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder()
                .setMid(m.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();

        ApiV1Tenure api = new ApiV1Tenure(m.getId(), req);
        Response r = api.execute();
        r.then()
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
    public void t7() throws Exception {
        MerchantType m = MerchantType.EMI;
        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder()
                .setMid(m.getId())
                .setTokenType("SSO")
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();

        ApiV1Tenure api = new ApiV1Tenure(m.getId(), req);
        Response r = api.execute();
        r.then()
                .spec(REQUEST_PARAMETER_NOT_VALID);
    }

    @Test(description = "Verify failure response of /theia/api/v1/emiSubvention/tenure when tokenType is not passed")
    public void t8() throws Exception {
        MerchantType m = MerchantType.EMI;
        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder()
                .setMid(m.getId())
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();

        ApiV1Tenure api = new ApiV1Tenure(m.getId(), req);
        Response r = api.execute();
        r.then()
                .spec(TOKEN_TYPE_NOT_SUPPORTED);
    }

    @Test(description = "Verify failure response of /theia/api/v1/emiSubvention/tenure when SSO token is wrong")
    public void t9() throws Exception {
        MerchantType m = MerchantType.EMI;
        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder()
                .setMid(m.getId())
                .setTokenType("SSO")
                .setToken("asdasds")
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();

        ApiV1Tenure api = new ApiV1Tenure(m.getId(), req);
        Response r = api.execute();
        r.then()
                .spec(SSO_TOKEN_INVALID);
    }

    @Test(description = "Verify success response of /theia/api/v1/emiSubvention/tenure when CHECKSUM tokenType is passed")
    public void t10() throws Exception {
        MerchantType m = MerchantType.EMI;
        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder()
                .setMid(m.getId())
                .setMerchantKey(m.getKey())
                .setTokenType("CHECKSUM")
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();

        ApiV1Tenure api = new ApiV1Tenure(m.getId(), req);
        Response r = api.execute();
        r.then()
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

    @Test(description = "Verify success response of /theia/api/v1/emiSubvention/tenure when CHECKSUM token is wrong")
    public void t11() throws Exception {
        MerchantType m = MerchantType.EMI;
        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder()
                .setMid(m.getId())
                .setMerchantKey(m.getKey())
                .setTokenType("CHECKSUM")
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();

        req.getHead().setToken("abcd");

        ApiV1Tenure api = new ApiV1Tenure(m.getId(), req);
        Response r = api.execute();
        r.then()
                .spec(CHECKSUM_PROVIDED_IS_INVALID);
    }


    @Test(description = "Verify success response of /theia/api/v1/emiSubvention/validateEmi when SSO token is passed")
    public void t12() throws Exception {
        MerchantType m = MerchantType.EMI;
        User user = userManager.getForRead(Label.BASIC);
        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder()
                .setMid(m.getId())
                .setMerchantKey(m.getKey())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();

        ApiV1Tenure api = new ApiV1Tenure(m.getId(), req);
        Response r = api.execute();
        JsonPath tenureResp = r.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.planDetails.emiType", hasItems("SUBVENTION"))
                .extract().jsonPath();

        String planId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId");
        BigInteger intplanid= new BigInteger(planId);
        ApiV1ValidateRequest req2 = new ApiV1ValidateRequest.Builder(user.ssoToken(), "SSO", m.getId())
                .setMerchantKey(m.getKey())
                .setPlanId(intplanid)
                .setPaymentDetails(
                        new PaymentDetails()
                                .setTotalTransactionAmount("5")
                                .setCardNumber("|" + PGPHelpers.getFormattedPaymentDetails("EMI", new PaymentDTO())))
                .setGenerateTokenForIntent(true)
                .build();
        ApiV1Validate api2 = new ApiV1Validate(m.getId(), req2);
        Response r2 = api2.execute();
        r2.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.bankId", not(empty()),
                        "body.bankId", not(empty()),
                        "body.bankName", not(empty()),
                        "body.bankCode", not(empty()),
                        "body.cardType", not(empty()),
                        "body.bankLogoUrl", not(empty()),
                        "body.planId", equalToIgnoringCase(planId),
                        "body.pgPlanId", not(empty()),
                        "body.rate", not(empty()),
                        "body.interval", not(empty()),
                        "body.emi", not(empty()),
                        "body.interest", not(empty()),
                        "body.emiType", not(empty()),
                        "body.emiLabel", not(empty()),
                        "body.gratifications", not(empty()),
                        "body.gratifications.value", everyItem(not(empty())),
                        "body.gratifications.type", everyItem(not(empty())),
                        "body.gratifications.label", everyItem(not(empty())),
                        "body.itemBreakUpList", not(empty())
                )
                .body("body.emiSubventionToken", not(empty()))
                .body("body.finalTransactionAmount", not(empty()))
        ;
    }

    @Test(description = "Verify failure response of /theia/api/v1/emiSubvention/validateEmi when SSO token is passed invalid")
    public void t13() throws Exception {
        MerchantType m = MerchantType.EMI;
        User user = userManager.getForRead(Label.BASIC);
        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder()
                .setMid(m.getId())
                .setMerchantKey(m.getKey())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();

        ApiV1Tenure api = new ApiV1Tenure(m.getId(), req);
        Response r = api.execute();
        JsonPath tenureResp = r.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.planDetails.emiType", hasItems("SUBVENTION"))
                .extract().jsonPath();

        String planId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId");
        BigInteger intplanid= new BigInteger(planId);
        ApiV1ValidateRequest req2 = new ApiV1ValidateRequest.Builder("1234", "SSO", m.getId())
                .setMerchantKey(m.getKey())
                .setPlanId(intplanid)
                .setPaymentDetails(
                        new PaymentDetails()
                                .setTotalTransactionAmount("5")
                                .setCardNumber("|" + PGPHelpers.getFormattedPaymentDetails("EMI", new PaymentDTO())))
                .setGenerateTokenForIntent(true)
                .build();
        ApiV1Validate api2 = new ApiV1Validate(m.getId(), req2);
        Response r2 = api2.execute();
        r2.then()
                .spec(SSO_TOKEN_INVALID);
    }

    @Test(description = "Verify failure response of /theia/api/v1/emiSubvention/validateEmi when SSO token is passed blank")
    public void t14() throws Exception {
        MerchantType m = MerchantType.EMI;
        User user = userManager.getForRead(Label.BASIC);
        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder()
                .setMid(m.getId())
                .setMerchantKey(m.getKey())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();

        ApiV1Tenure api = new ApiV1Tenure(m.getId(), req);
        Response r = api.execute();
        JsonPath tenureResp = r.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.planDetails.emiType", hasItems("SUBVENTION"))
                .extract().jsonPath();

        String planId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId");
        BigInteger intplanid= new BigInteger(planId);
        ApiV1ValidateRequest req2 = new ApiV1ValidateRequest.Builder("", "SSO", m.getId())
                .setMerchantKey(m.getKey())
                .setPlanId(intplanid)
                .setPaymentDetails(
                        new PaymentDetails()
                                .setTotalTransactionAmount("5")
                                .setCardNumber("|" + PGPHelpers.getFormattedPaymentDetails("EMI", new PaymentDTO())))
                .setGenerateTokenForIntent(true)
                .build();
        ApiV1Validate api2 = new ApiV1Validate(m.getId(), req2);
        Response r2 = api2.execute();
        r2.then()
                .spec(REQUEST_PARAMETER_NOT_VALID);
    }


    @Test(description = "Verify success response of /theia/api/v1/emiSubvention/validateEmi when CHECKSUM token is passed")
    public void t15() throws Exception {
        MerchantType m = MerchantType.EMI;
        User user = userManager.getForRead(Label.BASIC);
        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder()
                .setMid(m.getId())
                .setMerchantKey(m.getKey())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();

        ApiV1Tenure api = new ApiV1Tenure(m.getId(), req);
        Response r = api.execute();
        JsonPath tenureResp = r.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.planDetails.emiType", hasItems("SUBVENTION"))
                .extract().jsonPath();

        String planId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId");
        BigInteger intplanid= new BigInteger(planId);
        ApiV1ValidateRequest req2 = new ApiV1ValidateRequest.Builder("", "CHECKSUM", m.getId())
                .setMerchantKey(m.getKey())
                .setPlanId(intplanid)
                .setPaymentDetails(
                        new PaymentDetails()
                                .setTotalTransactionAmount("5")
                                .setCardNumber("|" + PGPHelpers.getFormattedPaymentDetails("EMI", new PaymentDTO())))
                .setGenerateTokenForIntent(true)
                .build();
        ApiV1Validate api2 = new ApiV1Validate(m.getId(), req2);
        Response r2 = api2.execute();
        r2.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.bankId", not(empty()),
                        "body.bankId", not(empty()),
                        "body.bankName", not(empty()),
                        "body.bankCode", not(empty()),
                        "body.cardType", not(empty()),
                        "body.bankLogoUrl", not(empty()),
                        "body.planId", equalToIgnoringCase(planId),
                        "body.pgPlanId", not(empty()),
                        "body.rate", not(empty()),
                        "body.interval", not(empty()),
                        "body.emi", not(empty()),
                        "body.interest", not(empty()),
                        "body.emiType", not(empty()),
                        "body.emiLabel", not(empty()),
                        "body.gratifications", not(empty()),
                        "body.gratifications.value", everyItem(not(empty())),
                        "body.gratifications.type", everyItem(not(empty())),
                        "body.gratifications.label", everyItem(not(empty())),
                        "body.itemBreakUpList", not(empty())
                )
                .body("body.emiSubventionToken", not(empty()))
                .body("body.finalTransactionAmount", not(empty()))
        ;
    }

    @Test(description = "Verify failure response of /theia/api/v1/emiSubvention/validateEmi when CHECKSUM token is passed invalid")
    public void t16() throws Exception {
        MerchantType m = MerchantType.EMI;
        User user = userManager.getForRead(Label.BASIC);
        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder()
                .setMid(m.getId())
                .setMerchantKey(m.getKey())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();

        ApiV1Tenure api = new ApiV1Tenure(m.getId(), req);
        Response r = api.execute();
        JsonPath tenureResp = r.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.planDetails.emiType", hasItems("SUBVENTION"))
                .extract().jsonPath();

        String planId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId");
        BigInteger intplanid= new BigInteger(planId);
        ApiV1ValidateRequest req2 = new ApiV1ValidateRequest.Builder("", "CHECKSUM", m.getId())
                .setMerchantKey(m.getKey())
                .setPlanId(intplanid)
                .setPaymentDetails(
                        new PaymentDetails()
                                .setTotalTransactionAmount("5")
                                .setCardNumber("|" + PGPHelpers.getFormattedPaymentDetails("EMI", new PaymentDTO())))
                .setGenerateTokenForIntent(true)
                .build();

        req2.getHead().setToken("123as");
        ApiV1Validate api2 = new ApiV1Validate(m.getId(), req2);
        Response r2 = api2.execute();
        r2.then()
                .spec(CHECKSUM_PROVIDED_IS_INVALID)
        ;
    }

    @Test(description = "Verify success response of /theia/api/v1/emiSubvention/validateEmi when SSO token is passed and generateTokenForIntent is false")
    public void t17() throws Exception {
        MerchantType m = MerchantType.EMI;
        User user = userManager.getForRead(Label.BASIC);
        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder()
                .setMid(m.getId())
                .setMerchantKey(m.getKey())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();

        ApiV1Tenure api = new ApiV1Tenure(m.getId(), req);
        Response r = api.execute();
        JsonPath tenureResp = r.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.planDetails.emiType", hasItems("SUBVENTION"))
                .extract().jsonPath();

        String planId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId");
        BigInteger intplanid= new BigInteger(planId);
        ApiV1ValidateRequest req2 = new ApiV1ValidateRequest.Builder("", "CHECKSUM", m.getId())
                .setMerchantKey(m.getKey())
                .setPlanId(intplanid)
                .setPaymentDetails(
                        new PaymentDetails()
                                .setTotalTransactionAmount("5")
                                .setCardNumber("|" + PGPHelpers.getFormattedPaymentDetails("EMI", new PaymentDTO())))
                .setGenerateTokenForIntent(false)
                .build();

        ApiV1Validate api2 = new ApiV1Validate(m.getId(), req2);
        Response r2 = api2.execute();
        r2.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.emiSubventionToken", nullValue())
        ;
    }

    @Test(description = "Verify failure response of /theia/api/v1/emiSubvention/validateEmi when SSO token is passed and paymentDetails is not correct")
    public void t18() throws Exception {
        MerchantType m = MerchantType.EMI;
        User user = userManager.getForRead(Label.BASIC);
        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder()
                .setMid(m.getId())
                .setMerchantKey(m.getKey())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();

        ApiV1Tenure api = new ApiV1Tenure(m.getId(), req);
        Response r = api.execute();
        JsonPath tenureResp = r.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.planDetails.emiType", hasItems("SUBVENTION"))
                .extract().jsonPath();

        String planId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId");
        BigInteger intplanid= new BigInteger(planId);
        ApiV1ValidateRequest req2 = new ApiV1ValidateRequest.Builder("", "CHECKSUM", m.getId())
                .setMerchantKey(m.getKey())
                .setPlanId(intplanid)
                .setPaymentDetails(
                        new PaymentDetails()
                                .setTotalTransactionAmount("5")
                                .setCardNumber("|1" + PGPHelpers.getFormattedPaymentDetails("EMI", new PaymentDTO())))
                .setGenerateTokenForIntent(true)
                .build();

        ApiV1Validate api2 = new ApiV1Validate(m.getId(), req2);
        Response r2 = api2.execute();
        r2.then()
                .spec(CARD_DETAIL_NOT_VALID)
        ;
    }

    @Test(description = "Verify failure response of /theia/api/v1/emiSubvention/validateEmi when SSO token is passed and paymentDetails is blank")
    public void t19() throws Exception {
        MerchantType m = MerchantType.EMI;
        User user = userManager.getForRead(Label.BASIC);
        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder()
                .setMid(m.getId())
                .setMerchantKey(m.getKey())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();

        ApiV1Tenure api = new ApiV1Tenure(m.getId(), req);
        Response r = api.execute();
        JsonPath tenureResp = r.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.planDetails.emiType", hasItems("SUBVENTION"))
                .extract().jsonPath();

        String planId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId");
        BigInteger intplanid= new BigInteger(planId);
        ApiV1ValidateRequest req2 = new ApiV1ValidateRequest.Builder("", "CHECKSUM", m.getId())
                .setMerchantKey(m.getKey())
                .setPlanId(intplanid)
                .setPaymentDetails(
                        new PaymentDetails()
                                .setTotalTransactionAmount("5")
                                .setCardNumber(""))
                .setGenerateTokenForIntent(true)
                .build();

        ApiV1Validate api2 = new ApiV1Validate(m.getId(), req2);
        Response r2 = api2.execute();
        r2.then()
                .statusCode(200)
                .body("body.resultInfo.resultStatus", equalToIgnoringCase("F"),
                        "body.resultInfo.resultCode", equalToIgnoringCase("1007"),
                        "body.resultInfo.resultMsg", equalToIgnoringCase("Card Details are missing"))
        ;
    }


    @Test(description = "Verify failure response of /theia/api/v1/emiSubvention/validateEmi when SSO token is passed and totalTransaction is less than item")
    public void t20() throws Exception {
        MerchantType m = MerchantType.EMI;
        User user = userManager.getForRead(Label.BASIC);
        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder()
                .setMid(m.getId())
                .setMerchantKey(m.getKey())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();

        ApiV1Tenure api = new ApiV1Tenure(m.getId(), req);
        Response r = api.execute();
        JsonPath tenureResp = r.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.planDetails.emiType", hasItems("SUBVENTION"))
                .extract().jsonPath();

        String planId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId");
        BigInteger intplanid= new BigInteger(planId);
        ApiV1ValidateRequest req2 = new ApiV1ValidateRequest.Builder("", "CHECKSUM", m.getId())
                .setMerchantKey(m.getKey())
                .setPlanId(intplanid)
                .setPaymentDetails(
                        new PaymentDetails()
                                .setTotalTransactionAmount("0.5")
                                .setCardNumber("|" + PGPHelpers.getFormattedPaymentDetails("EMI", new PaymentDTO())))
                .setGenerateTokenForIntent(true)
                .build();

        ApiV1Validate api2 = new ApiV1Validate(m.getId(), req2);
        Response r2 = api2.execute();
        r2.then()
                .statusCode(200)
                .body("body.resultInfo.resultStatus", equalToIgnoringCase("F"),
                        "body.resultInfo.resultCode", equalToIgnoringCase("1007"),
                        "body.resultInfo.resultMsg", equalToIgnoringCase("items total amount  does not match the totalTransaction Amount"))
        ;
    }

    @Test(description = "Verify success of /theia/api/v1/fetchPaymentOptions API when isEmiSubventionRequired=true, " +
            "emiSubventedTransactionAmount=60, " +
            "emiSubventionCustomerId!=null and no cards are saved")
    public void t21() throws Exception {
        MerchantType m = MerchantType.EMI;
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        ApiV1BanksRequest req = new ApiV1BanksRequest.Builder()
                .setMid(m.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();

        ApiV1Bank api = new ApiV1Bank(m.getId(), req);
        Response r = api.execute();
        r.then()
                .spec(SUCCESS_RESPONSE);

        String unqId = CommonHelpers.generateOrderId();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId("true")
                .setMid(m.getId())
                .setToken(user.ssoToken())
                .setEmiSubventionRequired(true)
                .setEmiSubventedTransactionAmount("60")
                .setEmiSubventionCustomerId(unqId)
                .setItems(Arrays.asList(new Item[]{new Item()}))
                .build();

        FetchPaymentOption fpo = new FetchPaymentOption(m.getId(), fetchPaymentOptionsDTO);
        fpo.execute().then()
                .spec(SUCCESS_RESPONSE)
                .body("body", hasKey("emiSubventionBanks"))
                .body("body", hasKey("orderId"))
                .body("body.emiSubventionBanks.emiTypes.type", containsInAnyOrder("STANDARD", "SUBVENTION"))
                .body("body.emiSubventionBanks.emiTypes.cardTypes", allOf(not(empty())))
                .body("body.merchantPayOption.savedInstruments", empty());

        given().spec(reqSpec)
                .contentType(ContentType.JSON)
                .baseUri(LocalConfig.PGP_HOST).basePath("/mockbank/emi/v1/userSummaryBulkPaymentOptions")
                .queryParams("customer-id", unqId)
                .get()
                .then()
                .statusCode(200);
    }

    @Test(description = "Verify success of /theia/api/v1/fetchPaymentOptions API when isEmiSubventionRequired=true, " +
            "emiSubventedTransactionAmount=60, " +
            "emiSubventionCustomerId!=null and cards are saved")
    public void t21_1() throws Exception {
        MerchantType m = MerchantType.EMI;
        User user = userManager.getForWrite(Label.VPAENABLED);
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        ApiV1BanksRequest req = new ApiV1BanksRequest.Builder()
                .setMid(m.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();

        ApiV1Bank api = new ApiV1Bank(m.getId(), req);
        Response r = api.execute();
        r.then()
                .spec(SUCCESS_RESPONSE);

        String unqId = CommonHelpers.generateOrderId();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId("true")
                .setMid(m.getId())
                .setToken(user.ssoToken())
                .setEmiSubventionRequired(true)
                .setEmiSubventedTransactionAmount("60")
                .setEmiSubventionCustomerId(unqId)
                .setItems(Arrays.asList(new Item[]{new Item()}))
                .build();

        FetchPaymentOption fpo = new FetchPaymentOption(m.getId(), fetchPaymentOptionsDTO);
        fpo.execute().then()
                .spec(SUCCESS_RESPONSE)
                .body("body", hasKey("emiSubventionBanks"))
                .body("body", hasKey("orderId"))
                .body("body.emiSubventionBanks.emiTypes.type", containsInAnyOrder("STANDARD", "SUBVENTION"))
                .body("body.emiSubventionBanks.emiTypes.cardTypes", allOf(not(empty())))
                .body("body.merchantPayOption.savedInstruments", not(empty()))
                .body("body.merchantPayOption.savedInstruments.find{it.isDisabled.status == 'false' && it.issuingBank == 'HDFC'}", anyOf(hasKey("savedCardEmisubventionDetail")))
                .body("body.merchantPayOption.savedInstruments.find{it.isDisabled.status == 'false' && it.issuingBank == 'HDFC'}.savedCardEmisubventionDetail",
                        hasKey("emiExists"),
                        "body.merchantPayOption.savedInstruments.find{it.isDisabled.status == 'false' && it.issuingBank == 'HDFC'}.savedCardEmisubventionDetail",
                        hasKey("emiType"),
                        "body.merchantPayOption.savedInstruments.find{it.isDisabled.status == 'false' && it.issuingBank == 'HDFC'}.savedCardEmisubventionDetail",
                        hasKey("lowestEmiAvailable"),
                        "body.merchantPayOption.savedInstruments.find{it.isDisabled.status == 'false' && it.issuingBank == 'HDFC'}.savedCardEmisubventionDetail",
                        hasKey("subventionType"))
                .body("body.merchantPayOption.savedInstruments.find{it.isDisabled.status == 'false' && it.issuingBank == 'HDFC'}.savedCardEmisubventionDetail.emiType",
                        equalToIgnoringCase("SUBVENTION"));

        given().spec(reqSpec)
                .contentType(ContentType.JSON)
                .baseUri(LocalConfig.PGP_HOST).basePath("/mockbank/emi/v1/userSummaryBulkPaymentOptions")
                .queryParams("customer-id", unqId)
                .get()
                .then()
                .statusCode(200);
    }

    @Test(description = "Verify failure of /theia/api/v1/fetchPaymentOptions API when isEmiSubventionRequired=true, " +
            "emiSubventedTransactionAmount=60, " +
            "emiSubventionCustomerId!=null and no cards are saved and SSO token not passed")
    public void t22() throws Exception {
        MerchantType m = MerchantType.PG2_AMEX_EMI;
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        ApiV1BanksRequest req = new ApiV1BanksRequest.Builder()
                .setMid(m.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();

        ApiV1Bank api = new ApiV1Bank(m.getId(), req);
        Response r = api.execute();
        r.then()
                .spec(SUCCESS_RESPONSE);

        String unqId = CommonHelpers.generateOrderId();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId("true")
                .setMid(m.getId())
                .setToken("")
                .setEmiSubventionRequired(true)
                .setEmiSubventedTransactionAmount("60")
                .setEmiSubventionCustomerId(unqId)
                .setItems(Arrays.asList(new Item[]{new Item()}))
                .build();

        FetchPaymentOption fpo = new FetchPaymentOption(m.getId(), fetchPaymentOptionsDTO);
        fpo.execute().then()
                .spec(REQUEST_PARAMETER_NOT_VALID);
    }

    @Test(description = "Verify failure of /theia/api/v1/fetchPaymentOptions API when isEmiSubventionRequired=true, " +
            "emiSubventedTransactionAmount=60, " +
            "emiSubventionCustomerId!=null and no cards are saved and wrong SSO token passed")
    public void t23() throws Exception {
        MerchantType m = MerchantType.PG2_AMEX_EMI;
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        ApiV1BanksRequest req = new ApiV1BanksRequest.Builder()
                .setMid(m.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();

        ApiV1Bank api = new ApiV1Bank(m.getId(), req);
        Response r = api.execute();
        r.then()
                .spec(SUCCESS_RESPONSE);

        String unqId = CommonHelpers.generateOrderId();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId("true")
                .setMid(m.getId())
                .setToken("12313")
                .setEmiSubventionRequired(true)
                .setEmiSubventedTransactionAmount("60")
                .setEmiSubventionCustomerId(unqId)
                .setItems(Arrays.asList(new Item[]{new Item()}))
                .build();

        FetchPaymentOption fpo = new FetchPaymentOption(m.getId(), fetchPaymentOptionsDTO);
        fpo.execute().then()
                .spec(SSO_TOKEN_INVALID);
    }

    @Test(description = "Verify success of /theia/api/v1/fetchPaymentOptions API when isEmiSubventionRequired=true, " +
            "emiSubventedTransactionAmount=60, " +
            "emiSubventionCustomerId!=null and no cards are saved and generate order Id flag as false")
    public void t24() throws Exception {
        MerchantType m = MerchantType.PG2_AMEX_EMI;
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        ApiV1BanksRequest req = new ApiV1BanksRequest.Builder()
                .setMid(m.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();

        ApiV1Bank api = new ApiV1Bank(m.getId(), req);
        Response r = api.execute();
        r.then()
                .spec(SUCCESS_RESPONSE);

        String unqId = CommonHelpers.generateOrderId();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId("false")
                .setMid(m.getId())
                .setToken(user.ssoToken())
                .setEmiSubventionRequired(true)
                .setEmiSubventedTransactionAmount("60")
                .setEmiSubventionCustomerId(unqId)
                .setItems(Arrays.asList(new Item[]{new Item()}))
                .build();

        FetchPaymentOption fpo = new FetchPaymentOption(m.getId(), fetchPaymentOptionsDTO);
        fpo.execute().then()
                .spec(SUCCESS_RESPONSE)
                .body("body", not(hasKey("orderId")));

    }

    @Test(description = "Verify success of /theia/api/v1/fetchPaymentOptions API when isEmiSubventionRequired=false, " +
            "emiSubventedTransactionAmount=60, " +
            "emiSubventionCustomerId!=null and no cards are saved")
    public void t25() throws Exception {
        MerchantType m = MerchantType.EMI;
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        ApiV1BanksRequest req = new ApiV1BanksRequest.Builder()
                .setMid(m.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();

        ApiV1Bank api = new ApiV1Bank(m.getId(), req);
        Response r = api.execute();
        r.then()
                .spec(SUCCESS_RESPONSE);

        String unqId = CommonHelpers.generateOrderId();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId("true")
                .setMid(m.getId())
                .setToken(user.ssoToken())
                .setEmiSubventionRequired(false)
                .setEmiSubventedTransactionAmount("60")
                .setEmiSubventionCustomerId(unqId)
                .setItems(Arrays.asList(new Item[]{new Item()}))
                .build();

        FetchPaymentOption fpo = new FetchPaymentOption(m.getId(), fetchPaymentOptionsDTO);
        fpo.execute().then()
                .spec(SUCCESS_RESPONSE)
                .body("body", not(hasKey("emiSubventionBanks")))
                .body("body", hasKey("orderId"));

        given().spec(reqSpec)
                .contentType(ContentType.JSON)
                .baseUri(LocalConfig.PGP_HOST).basePath("/mockbank/emi/v1/userSummaryBulkPaymentOptions")
                .queryParams("customer-id", unqId)
                .get()
                .then()
                .statusCode(204);
    }

    @Test(description = "Verify success of Initiate tn api when emiSubventionToken is sent in request")
    public void t26() throws Exception {
        MerchantType m = MerchantType.EMISubvention;
        User user = userManager.getForRead(Label.BASIC);
        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder()
                .setMid(m.getId())
                .setMerchantKey(m.getKey())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();

        ApiV1Tenure api = new ApiV1Tenure(m.getId(), req);
        Response r = api.execute();
        JsonPath tenureResp = r.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.planDetails.emiType", hasItems("SUBVENTION"))
                .extract().jsonPath();

        String planId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId");
        BigInteger intplanid= new BigInteger(planId);
        ApiV1ValidateRequest req2 = new ApiV1ValidateRequest.Builder(user.ssoToken(), "SSO", m.getId())
                .setMerchantKey(m.getKey())
                .setPlanId(intplanid)
                .setPaymentDetails(
                        new PaymentDetails()
                                .setTotalTransactionAmount("5")
                                .setCardNumber("|" + PGPHelpers.getFormattedPaymentDetails("EMI", new PaymentDTO())))
                .setGenerateTokenForIntent(true)
                .build();
        ApiV1Validate api2 = new ApiV1Validate(m.getId(), req2);
        Response r2 = api2.execute();
        JsonPath validateResp = r2.then()
                .spec(SUCCESS_RESPONSE)
                .body("body", hasKey("emiSubventionToken"))
                .extract().jsonPath();

        String emiSubventionToken = validateResp.getString("body.emiSubventionToken");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.EMI)
                .setEmiSubventionToken(emiSubventionToken)
                .setTxnValue("5")
                .setPayableAmount(new TxnAmount("4"))
                .build();

        Response rsp = new InitTxn(initTxnDTO).execute();
        rsp.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.txnToken", not(empty()));
    }

    @Issue("PGP-22105")
    @Parameters({"isNativePlus"})
    @Test(description = "Verify ORDER_STAMP API and CHECKOUT API is being called by theia and STATUS_UPDATE API by notification-queue-handler " +
            "when PTC is executed and transaction is successful")
    public void t27(@Optional("false") boolean isNativePlus) throws Exception {
        MerchantType m = MerchantType.EMI;
        User user = userManager.getForRead(Label.BASIC);
        String unqId = CommonHelpers.generateOrderId();
        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder()
                .setMid(m.getId())
                .setMerchantKey(m.getKey())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();

        String planId = new ApiV1Tenure(m.getId(), req).execute()
                .then().spec(SUCCESS_RESPONSE)
                .body("body.planDetails.emiType", hasItems("SUBVENTION"))
                .extract().jsonPath().getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId");

        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(paymentDTO.getEmiCard());
        BigInteger intplanid= new BigInteger(planId);
        ApiV1ValidateRequest req2 = new ApiV1ValidateRequest.Builder(user.ssoToken(), "SSO", m.getId())
                .setMerchantKey(m.getKey())
                .setPlanId(intplanid)
                .setCustomerId(unqId)
                .setPaymentDetails(
                        new PaymentDetails()
                                .setTotalTransactionAmount("5")
                                .setCardNumber("|" + PGPHelpers.getFormattedPaymentDetails("EMI", paymentDTO)))
                .setGenerateTokenForIntent(true)
                .build();
        JsonPath validateResp = new ApiV1Validate(m.getId(), req2).execute()
                .then()
                .spec(SUCCESS_RESPONSE)
                .body("body", hasKey("emiSubventionToken"))
                .extract().jsonPath();
        String emiSubventionToken = validateResp.getString("body.emiSubventionToken");
        String pgPlanId = validateResp.getString("body.pgPlanId");

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.EMI)
                .setEmiSubventionToken(emiSubventionToken)
                .setTxnValue("5")
                .setPayableAmount(new TxnAmount("4"))
                .build();
        String txnToken = new InitTxn(initTxnDTO).execute().then()
                .spec(SUCCESS_RESPONSE)
                .body("body.txnToken", not(empty()))
                .extract().jsonPath().getString("body.txnToken");

        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.EMI, initTxnDTO.orderFromBody(), txnToken, paymentDTO, PayMethodType.EMI)
                .setChannelCode(Constants.Bank.HDFC.toString())
                .setPlanId(pgPlanId)
                .setAUTH_MODE("otp")
                .setREQUEST_TYPE("NATIVE")
                .setWEBSITE("retail")
                .setEMI_TYPE("CREDIT_CARD")
                .setINDUSTRY_TYPE_ID("Retail").build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount("4.0")
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

        given().spec(reqSpec)
                .baseUri(LocalConfig.PGP_HOST)
                .basePath("mockbank/emi/v1/checkoutWithOrder")
                .queryParam("customer-id", unqId)
                .get()
                .then()
                .statusCode(200);
     /*   given().spec(reqSpec)
                .baseUri(LocalConfig.PGP_HOST)
                .basePath("mockbank/emi/v1/orderStamp")
                .queryParam("order-id", orderDTO.getORDER_ID())
                .get()
                .then()
                .statusCode(200);*/
        given().spec(reqSpec)
                .baseUri(LocalConfig.PGP_HOST)
                .basePath("/mockbank/emi/v1/statusUpdate")
                .queryParam("order-id", orderDTO.getORDER_ID())
                .get()
                .then()
                .statusCode(200);
    }

    @Issue("PGP-21955")
    @Parameters({"isNativePlus"})
    @Test(description = "Verify ORDER_STAMP API and CHECKOUT API is being called by theia and STATUS_UPDATE API by notification-queue-handler " +
            "when cacheCardToken is sent in Validate API when PTC is executed and transaction is successful")
    public void t28(@Optional("false") boolean isNativePlus) throws Exception {
        MerchantType m = MerchantType.EMI;
        User user = userManager.getForRead(Label.BASIC);
        String unqId = CommonHelpers.generateOrderId();
        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder()
                .setMid(m.getId())
                .setMerchantKey(m.getKey())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();

        String planId = new ApiV1Tenure(m.getId(), req).execute()
                .then().spec(SUCCESS_RESPONSE)
                .body("body.planDetails.emiType", hasItems("SUBVENTION"))
                .extract().jsonPath().getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId");

        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        paymentDTO.setEmiCard(VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        String cacheCardToken = new GetCardToken(LocalConfig.PGP_HOST, m.getId(), unqId,
                paymentDTO.getCreditCardNumber(),
                paymentDTO.getExpMonth(),
                paymentDTO.getExpYear(),
                paymentDTO.getCvvNumber())
                .execute()
                .then().statusCode(200)
                .extract().jsonPath().getString("TOKEN");
        BigInteger intplanid= new BigInteger(planId);
        ApiV1ValidateRequest req2 = new ApiV1ValidateRequest.Builder(user.ssoToken(), "SSO", m.getId())
                .setMerchantKey(m.getKey())
                .setPlanId(intplanid)
                .setCustomerId(unqId)
                .setCacheCardToken(cacheCardToken)
                .setPaymentDetails(
                        new PaymentDetails()
                                .setTotalTransactionAmount("5"))
                .setGenerateTokenForIntent(true)
                .build();
        JsonPath validateResp = new ApiV1Validate(m.getId(), req2).execute()
                .then()
                .spec(SUCCESS_RESPONSE)
                .body("body", hasKey("emiSubventionToken"))
                .extract().jsonPath();
        String emiSubventionToken = validateResp.getString("body.emiSubventionToken");
        String pgPlanId = validateResp.getString("body.pgPlanId");

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.EMI)
                .setEmiSubventionToken(emiSubventionToken)
                .setTxnValue("5")
                .setPayableAmount(new TxnAmount("4"))
                .build();
        String txnToken = new InitTxn(initTxnDTO).execute().then()
                .spec(SUCCESS_RESPONSE)
                .body("body.txnToken", not(empty()))
                .extract().jsonPath().getString("body.txnToken");

        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.EMI, initTxnDTO.orderFromBody(), txnToken, paymentDTO, PayMethodType.EMI)
                .setChannelCode(Constants.Bank.HDFC.toString())
                .setPlanId(pgPlanId)
                .setAUTH_MODE("otp")
                .setREQUEST_TYPE("NATIVE")
                .setWEBSITE("retail")
                .setEMI_TYPE("CREDIT_CARD")
                .setINDUSTRY_TYPE_ID("Retail").build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getPayableAmount().getValue())
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

        given().spec(reqSpec)
                .baseUri(LocalConfig.PGP_HOST)
                .basePath("mockbank/emi/v1/checkoutWithOrder")
                .queryParam("customer-id", unqId)
                .get()
                .then()
                .statusCode(200);
       /* given().spec(reqSpec)
                .baseUri(LocalConfig.PGP_HOST)
                .basePath("mockbank/emi/v1/orderStamp")
                .queryParam("order-id", orderDTO.getORDER_ID())
                .get()
                .then()
                .statusCode(200);*/
        given().spec(reqSpec)
                .baseUri(LocalConfig.PGP_HOST)
                .basePath("/mockbank/emi/v1/statusUpdate")
                .queryParam("order-id", orderDTO.getORDER_ID())
                .get()
                .then()
                .statusCode(200);
    }

    ///////////////////////////////// EMI Subvention Phase 2 //////////////////////////////////////

    //Bank API

    @Test(description = "Bank API : Verify the response when api is hit with amount based support i.e with price and subvention amount in the request",groups = "P0")
    public void t29bankAPIPhase2() throws Exception {
        Constants.MerchantType emiMerchant = Constants.MerchantType.EMI;
        User user = userManager.getForRead(Label.BASIC);
        String price ="1000";
        String subventionAmount = "200";

        ApiV1BanksRequest apiV1BanksRequest = new ApiV1BanksRequest.Builder(price,subventionAmount)
                .setMid(emiMerchant.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken()).build();

        ApiV1Bank api = new ApiV1Bank(emiMerchant.getId(), apiV1BanksRequest);
        Response r = api.execute();
        JsonPath j = r.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.emiTypes", not(empty()))
                .body("body.emiTypes.type", containsInAnyOrder("STANDARD", "SUBVENTION"))
                .body("body.emiTypes.find{it.type == 'STANDARD'}.label", equalToIgnoringCase("Standard EMI"))
                .body("body.emiTypes.find{it.type == 'STANDARD'}.cardTypes", not(empty()))
                .body("body.emiTypes.find{it.type == 'STANDARD'}.cardTypes.cardType", containsInAnyOrder("DEBIT_CARD", "CREDIT_CARD"))
                .body("body.emiTypes.find{it.type == 'STANDARD'}.cardTypes.bankDetails", not(empty()))
                .body("body.emiTypes.find{it.type == 'STANDARD'}.cardTypes.bankDetails", everyItem(everyItem(hasKey("bankName"))))
                .body("body.emiTypes.find{it.type == 'STANDARD'}.cardTypes.bankDetails", everyItem(everyItem(hasKey("bankCode"))))
                .body("body.emiTypes.find{it.type == 'STANDARD'}.cardTypes.bankDetails", everyItem(everyItem(hasKey("bankLogoUrl"))))
                .body("body.emiTypes.find{it.type == 'SUBVENTION'}.label", equalToIgnoringCase("Zero Cost EMI"))
                .body("body.emiTypes.find{it.type == 'SUBVENTION'}.cardTypes", not(empty()))
                .body("body.emiTypes.find{it.type == 'SUBVENTION'}.cardTypes.cardType", containsInAnyOrder("CREDIT_CARD"))
                .body("body.emiTypes.find{it.type == 'SUBVENTION'}.cardTypes.bankDetails", not(empty()))
                .body("body.emiTypes.find{it.type == 'SUBVENTION'}.cardTypes.bankDetails", everyItem(everyItem(hasKey("bankName"))))
                .body("body.emiTypes.find{it.type == 'SUBVENTION'}.cardTypes.bankDetails", everyItem(everyItem(hasKey("bankCode"))))
                .body("body.emiTypes.find{it.type == 'SUBVENTION'}.cardTypes.bankDetails", everyItem(everyItem(hasKey("bankLogoUrl"))))
                .extract().jsonPath();

    }

    @Test(description = "Bank API : Verify the response if one of the request param for amount based strategy is not sent in the request (\"price\",\"subventionAmount\")",groups = "P1")
    public void t30bankAPIPhase2() throws Exception {
        Constants.MerchantType emiMerchant = Constants.MerchantType.EMI;
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
        Response r = api.execute();
        JsonPath j = r.then()
                .spec(INVALID_AMOUNT_DETAILS)
                .extract().jsonPath();

    }

    @Test(description = "Bank API : Verify the response if product based request param i.e items is sent in the request along with amount details",groups = "P1")
    public void t31bankAPIPhase2() throws Exception {
        Constants.MerchantType emiMerchant = Constants.MerchantType.EMI;
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
        Response r = api.execute();
        JsonPath j = r.then()
                .body("body.resultInfo.resultStatus",equalTo("F"))
                .body("body.resultInfo.resultCode",equalTo("1007"))
                .body("body.resultInfo.resultMsg", equalTo("Both item list and amount cannot be non empty in request"))
                .extract().jsonPath();

    }

    @Test(description = "Bank API : Verify the API response with token type SSO (with correct and incorrect sso)",groups = "P1")
    public void t32bankAPIPhase2() throws Exception {
        Constants.MerchantType emiMerchant = Constants.MerchantType.EMI;
        String price = "1000";
        String subventionAmount = "200";

        ApiV1BanksRequest apiV1BanksRequest = new ApiV1BanksRequest.Builder(price,subventionAmount)
                .setMid(emiMerchant.getId())
                .setTokenType("SSO")
                .setToken("Incorrect SSO token")
                .setItems(null)
                .build();

        ApiV1Bank api = new ApiV1Bank(emiMerchant.getId(), apiV1BanksRequest);
        Response r = api.execute();
        JsonPath j = r.then()
                .body("body.resultInfo.resultStatus",equalTo("F"))
                .body("body.resultInfo.resultCode",equalTo("2004"))
                .body("body.resultInfo.resultMsg", equalTo("SSO Token is invalid"))
                .extract().jsonPath();

    }

    @Test(description = "Bank API : Verify the API response with token type CHECKSUM with correct checksum",groups = "P0")
    public void t33bankAPIPhase2() throws Exception {
        Constants.MerchantType emiMerchant = Constants.MerchantType.EMI;
        String price = "1000";
        String subventionAmount = "200";

        ApiV1BanksRequest apiV1BanksRequest = new ApiV1BanksRequest.Builder(price,subventionAmount)
                .setMid(emiMerchant.getId())
                .setMerchantKey(emiMerchant.getKey())
                .setTokenType("CHECKSUM")
                .setItems(null)
                .build();

        ApiV1Bank api = new ApiV1Bank(emiMerchant.getId(), apiV1BanksRequest);
        Response r = api.execute();
        JsonPath j = r.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.emiTypes", not(empty()))
                .body("body.emiTypes.type", containsInAnyOrder("STANDARD", "SUBVENTION"))
                .body("body.emiTypes.find{it.type == 'STANDARD'}.label", equalToIgnoringCase("Standard EMI"))
                .body("body.emiTypes.find{it.type == 'STANDARD'}.cardTypes", not(empty()))
                .body("body.emiTypes.find{it.type == 'STANDARD'}.cardTypes.cardType", containsInAnyOrder("DEBIT_CARD", "CREDIT_CARD"))
                .body("body.emiTypes.find{it.type == 'STANDARD'}.cardTypes.bankDetails", not(empty()))
                .body("body.emiTypes.find{it.type == 'STANDARD'}.cardTypes.bankDetails", everyItem(everyItem(hasKey("bankName"))))
                .body("body.emiTypes.find{it.type == 'STANDARD'}.cardTypes.bankDetails", everyItem(everyItem(hasKey("bankCode"))))
                .body("body.emiTypes.find{it.type == 'STANDARD'}.cardTypes.bankDetails", everyItem(everyItem(hasKey("bankLogoUrl"))))
                .body("body.emiTypes.find{it.type == 'SUBVENTION'}.label", equalToIgnoringCase("Zero Cost EMI"))
                .body("body.emiTypes.find{it.type == 'SUBVENTION'}.cardTypes", not(empty()))
                .body("body.emiTypes.find{it.type == 'SUBVENTION'}.cardTypes.cardType", containsInAnyOrder("CREDIT_CARD"))
                .body("body.emiTypes.find{it.type == 'SUBVENTION'}.cardTypes.bankDetails", not(empty()))
                .body("body.emiTypes.find{it.type == 'SUBVENTION'}.cardTypes.bankDetails", everyItem(everyItem(hasKey("bankName"))))
                .body("body.emiTypes.find{it.type == 'SUBVENTION'}.cardTypes.bankDetails", everyItem(everyItem(hasKey("bankCode"))))
                .body("body.emiTypes.find{it.type == 'SUBVENTION'}.cardTypes.bankDetails", everyItem(everyItem(hasKey("bankLogoUrl"))))
                .extract().jsonPath();

    }


    @Test(description = "Bank API : Verify the API response with token type CHECKSUM with incorrect checksum",groups = "P0")
    public void t34bankAPIPhase2() throws Exception {
        Constants.MerchantType emiMerchant = Constants.MerchantType.EMI;
        String price = "1000";
        String subventionAmount = "200";

        ApiV1BanksRequest apiV1BanksRequest = new ApiV1BanksRequest.Builder(price,subventionAmount)
                .setMid(emiMerchant.getId())
                .setTokenType("CHECKSUM")
                .setItems(null)
                .build();

        ApiV1Bank api = new ApiV1Bank(emiMerchant.getId(), apiV1BanksRequest);
        Response r = api.execute();
        JsonPath j = r.then()
                .spec(CHECKSUM_PROVIDED_IS_INVALID)
                .extract().jsonPath();

    }

    @Test(description = "Bank API : Verify the API response when isEmiEnabled flag is false (items) and item not eligible for emi",groups = "P2")
    public void t35bankAPIPhase2() throws Exception {
        Constants.MerchantType emiMerchant = MerchantType.EMI_DC;
        User user = userManager.getForRead(Label.BASIC);
        ApiV1BanksRequest apiV1BanksRequest = new ApiV1BanksRequest.Builder()
                .setMid(emiMerchant.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setItems(Arrays.asList(new com.paytm.dto.emiSubvention.ApiV1Banks.request.Item[]
                        {new com.paytm.dto.emiSubvention.ApiV1Banks.request.Item().setIsEmiEnabled(false)}))
                .build();

        ApiV1Bank api = new ApiV1Bank(emiMerchant.getId(), apiV1BanksRequest);
        Response r = api.execute();
        JsonPath j = r.then()
                .statusCode(200)
                .body("body.resultInfo.resultStatus", equalToIgnoringCase("U"),
                        "body.resultInfo.resultCode", equalToIgnoringCase("00000900"),
                        "body.resultInfo.resultMsg", equalToIgnoringCase("System error"))
                .extract().jsonPath();

    }

    @Test(description = "Bank API : Verify the API with diff MID'S in request and in query parameters",groups = "P2")
    public void t36bankAPIPhase2() throws Exception {
        Constants.MerchantType emiMerchant = Constants.MerchantType.EMI;
        User user = userManager.getForRead(Label.BASIC);
        ApiV1BanksRequest apiV1BanksRequest = new ApiV1BanksRequest.Builder()
                .setMid(emiMerchant.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();

        ApiV1Bank api = new ApiV1Bank(MerchantType.PGOnly.getId(), apiV1BanksRequest);
        Response r = api.execute();
        JsonPath j = r.then()
                .body("body.resultInfo.resultStatus",equalTo("F"))
                .body("body.resultInfo.resultCode",equalTo("1007"))
                .body("body.resultInfo.resultMsg", equalTo("mid passed in query params and request body does not match"))
                .extract().jsonPath();

    }

    @Test(description = "Bank API : Verify the response when items field is sent as blank and it is sent along with the amount info",groups = "P1")
    public void t37bankAPIPhase2() throws Exception {
        Constants.MerchantType emiMerchant = Constants.MerchantType.EMI;
        User user = userManager.getForRead(Label.BASIC);
        String price = "1000";
        String subventionAmount ="200";
        ApiV1BanksRequest apiV1BanksRequest = new ApiV1BanksRequest.Builder(price,subventionAmount)
                .setMid(emiMerchant.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();

        ApiV1Bank api = new ApiV1Bank(emiMerchant.getId(), apiV1BanksRequest);
        Response r = api.execute();
        JsonPath j = r.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.emiTypes", not(empty()))
                .body("body.emiTypes.type", containsInAnyOrder("STANDARD", "SUBVENTION"))
                .body("body.emiTypes.find{it.type == 'STANDARD'}.label", equalToIgnoringCase("Standard EMI"))
                .body("body.emiTypes.find{it.type == 'STANDARD'}.cardTypes", not(empty()))
                .body("body.emiTypes.find{it.type == 'STANDARD'}.cardTypes.cardType", containsInAnyOrder("DEBIT_CARD", "CREDIT_CARD"))
                .body("body.emiTypes.find{it.type == 'STANDARD'}.cardTypes.bankDetails", not(empty()))
                .body("body.emiTypes.find{it.type == 'STANDARD'}.cardTypes.bankDetails", everyItem(everyItem(hasKey("bankName"))))
                .body("body.emiTypes.find{it.type == 'STANDARD'}.cardTypes.bankDetails", everyItem(everyItem(hasKey("bankCode"))))
                .body("body.emiTypes.find{it.type == 'STANDARD'}.cardTypes.bankDetails", everyItem(everyItem(hasKey("bankLogoUrl"))))
                .body("body.emiTypes.find{it.type == 'SUBVENTION'}.label", equalToIgnoringCase("Zero Cost EMI"))
                .body("body.emiTypes.find{it.type == 'SUBVENTION'}.cardTypes", not(empty()))
                .body("body.emiTypes.find{it.type == 'SUBVENTION'}.cardTypes.cardType", containsInAnyOrder("CREDIT_CARD"))
                .body("body.emiTypes.find{it.type == 'SUBVENTION'}.cardTypes.bankDetails", not(empty()))
                .body("body.emiTypes.find{it.type == 'SUBVENTION'}.cardTypes.bankDetails", everyItem(everyItem(hasKey("bankName"))))
                .body("body.emiTypes.find{it.type == 'SUBVENTION'}.cardTypes.bankDetails", everyItem(everyItem(hasKey("bankCode"))))
                .body("body.emiTypes.find{it.type == 'SUBVENTION'}.cardTypes.bankDetails", everyItem(everyItem(hasKey("bankLogoUrl"))))
                .extract().jsonPath();

    }

    @Test(description = "Bank API : Verify the response when price field is sent as blank along with a valid subventionAmount ",groups = "P1")
    public void t38bankAPIPhase2() throws Exception {
        Constants.MerchantType emiMerchant = Constants.MerchantType.EMI;
        User user = userManager.getForRead(Label.BASIC);

        String subventionAmount = "200";

        ApiV1BanksRequest apiV1BanksRequest = new ApiV1BanksRequest.Builder("",subventionAmount)
                .setMid(emiMerchant.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();

        ApiV1Bank api = new ApiV1Bank(emiMerchant.getId(), apiV1BanksRequest);
        Response r = api.execute();
        JsonPath j = r.then()
                .spec(INVALID_AMOUNT_DETAILS)
                .extract().jsonPath();

    }

    @Test(description = "Bank API : Verify the response when a valid price field is sent along with a blank subventionAmount ",groups = "P1")
    public void t39bankAPIPhase2() throws Exception {
        Constants.MerchantType emiMerchant = Constants.MerchantType.EMI;
        User user = userManager.getForRead(Label.BASIC);
        String price ="1000";

        ApiV1BanksRequest apiV1BanksRequest = new ApiV1BanksRequest.Builder(price,"")
                .setMid(emiMerchant.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();

        ApiV1Bank api = new ApiV1Bank(emiMerchant.getId(), apiV1BanksRequest);
        Response r = api.execute();
        JsonPath j = r.then()
                .spec(INVALID_AMOUNT_DETAILS)
                .extract().jsonPath();

    }


    @Test(description = "Bank API : Verify the response when price field is sent as zero along with a valid subventionAmount ",groups = "P1")
    public void t40bankAPIPhase2() throws Exception {
        Constants.MerchantType emiMerchant = Constants.MerchantType.EMI;
        User user = userManager.getForRead(Label.BASIC);
        String price ="0";
        String subventionAmount = "200";

        ApiV1BanksRequest apiV1BanksRequest = new ApiV1BanksRequest.Builder(price,subventionAmount)
                .setMid(emiMerchant.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();

        ApiV1Bank api = new ApiV1Bank(emiMerchant.getId(), apiV1BanksRequest);
        Response r = api.execute();
        JsonPath j = r.then()
                .spec(INVALID_AMOUNT_DETAILS)
                .extract().jsonPath();

    }


    @Test(description = "Bank API : Verify the response when a valid price field is sent along with a zero subventionAmount  ",groups = "P1")
    public void t41bankAPIPhase2() throws Exception {
        Constants.MerchantType emiMerchant = Constants.MerchantType.EMI;
        User user = userManager.getForRead(Label.BASIC);
        String price ="500";
        String subventionAmount = "0";

        ApiV1BanksRequest apiV1BanksRequest = new ApiV1BanksRequest.Builder(price,subventionAmount)
                .setMid(emiMerchant.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();

        ApiV1Bank api = new ApiV1Bank(emiMerchant.getId(), apiV1BanksRequest);
        Response r = api.execute();
        JsonPath j = r.then()
                .spec(INVALID_AMOUNT_DETAILS)
                .extract().jsonPath();

    }

    @Test(description = "Bank API : Verify the response when price and subventionAmount is sent as zero in the request ",groups = "P2")
    public void t42bankAPIPhase2() throws Exception {
        Constants.MerchantType emiMerchant = Constants.MerchantType.EMI;
        User user = userManager.getForRead(Label.BASIC);
        String price ="0";
        String subventionAmount = "0";

        ApiV1BanksRequest apiV1BanksRequest = new ApiV1BanksRequest.Builder(price,subventionAmount)
                .setMid(emiMerchant.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();

        ApiV1Bank api = new ApiV1Bank(emiMerchant.getId(), apiV1BanksRequest);
        Response r = api.execute();
        JsonPath j = r.then()
                .spec(INVALID_AMOUNT_DETAILS)
                .extract().jsonPath();

    }

    @Test(description = "Bank API : Verify the response when valid items value is sent along with price and subventionAmount as zero in the request ",groups = "P1")
    public void t43bankAPIPhase2() throws Exception {
        Constants.MerchantType emiMerchant = Constants.MerchantType.EMI;
        User user = userManager.getForRead(Label.BASIC);
        String price ="0";
        String subventionAmount = "0";

        ApiV1BanksRequest apiV1BanksRequest = new ApiV1BanksRequest.Builder()
                .setMid(emiMerchant.getId())
                .setPrice(price)
                .setSubventionAmount(subventionAmount)
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();

        ApiV1Bank api = new ApiV1Bank(emiMerchant.getId(), apiV1BanksRequest);
        Response r = api.execute();
        JsonPath j = r.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.emiTypes", not(empty()))
                .body("body.emiTypes.type", containsInAnyOrder("STANDARD", "SUBVENTION"))
                .body("body.emiTypes.find{it.type == 'STANDARD'}.label", equalToIgnoringCase("Standard EMI"))
                .body("body.emiTypes.find{it.type == 'STANDARD'}.cardTypes", not(empty()))
                .body("body.emiTypes.find{it.type == 'STANDARD'}.cardTypes.cardType", containsInAnyOrder("DEBIT_CARD", "CREDIT_CARD"))
                .body("body.emiTypes.find{it.type == 'STANDARD'}.cardTypes.bankDetails", not(empty()))
                .body("body.emiTypes.find{it.type == 'STANDARD'}.cardTypes.bankDetails", everyItem(everyItem(hasKey("bankName"))))
                .body("body.emiTypes.find{it.type == 'STANDARD'}.cardTypes.bankDetails", everyItem(everyItem(hasKey("bankCode"))))
                .body("body.emiTypes.find{it.type == 'STANDARD'}.cardTypes.bankDetails", everyItem(everyItem(hasKey("bankLogoUrl"))))
                .body("body.emiTypes.find{it.type == 'SUBVENTION'}.label", equalToIgnoringCase("Zero Cost EMI"))
                .body("body.emiTypes.find{it.type == 'SUBVENTION'}.cardTypes", not(empty()))
                .body("body.emiTypes.find{it.type == 'SUBVENTION'}.cardTypes.cardType", containsInAnyOrder("CREDIT_CARD"))
                .body("body.emiTypes.find{it.type == 'SUBVENTION'}.cardTypes.bankDetails", not(empty()))
                .body("body.emiTypes.find{it.type == 'SUBVENTION'}.cardTypes.bankDetails", everyItem(everyItem(hasKey("bankName"))))
                .body("body.emiTypes.find{it.type == 'SUBVENTION'}.cardTypes.bankDetails", everyItem(everyItem(hasKey("bankCode"))))
                .body("body.emiTypes.find{it.type == 'SUBVENTION'}.cardTypes.bankDetails", everyItem(everyItem(hasKey("bankLogoUrl"))))
                .extract().jsonPath();
    }

    @Test(description = "Bank API : Verify the response when amount info is sent in negative in the request",groups = "P2")
    public void t44bankAPIPhase2() throws Exception {
        Constants.MerchantType emiMerchant = Constants.MerchantType.EMI;
        User user = userManager.getForRead(Label.BASIC);
        String price ="1000";
        String subventionAmount = "-400";

        ApiV1BanksRequest apiV1BanksRequest = new ApiV1BanksRequest.Builder(price,subventionAmount)
                .setMid(emiMerchant.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();

        ApiV1Bank api = new ApiV1Bank(emiMerchant.getId(), apiV1BanksRequest);
        Response r = api.execute();
        JsonPath j = r.then()
                .spec(INVALID_AMOUNT_DETAILS)
                .extract().jsonPath();
    }

    @Test(description = "Bank API : Verify the response when amount info is sent other than a Double data type value in the request",groups = "P2")
    public void t45bankAPIPhase2() throws Exception {
        Constants.MerchantType emiMerchant = Constants.MerchantType.EMI;
        User user = userManager.getForRead(Label.BASIC);
        String price ="1000";
        String subventionAmount = "1oo9";

        ApiV1BanksRequest apiV1BanksRequest = new ApiV1BanksRequest.Builder(price,subventionAmount)
                .setMid(emiMerchant.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();

        ApiV1Bank api = new ApiV1Bank(emiMerchant.getId(), apiV1BanksRequest);
        Response r = api.execute();
        JsonPath j = r.then()
                .statusCode(200)
                .body("body.resultInfo.resultStatus", equalToIgnoringCase("U"),
                        "body.resultInfo.resultCode", equalToIgnoringCase("00000900"),
                        "body.resultInfo.resultMsg", equalToIgnoringCase("System error"))
                .extract().jsonPath();
    }


    @Test(description = "Bank API : Verify the response when a out of range value is sent in the amount info in the request",groups = "P2")
    public void t46bankAPIPhase2() throws Exception {
        Constants.MerchantType emiMerchant = Constants.MerchantType.EMI;
        User user = userManager.getForRead(Label.BASIC);
        String price ="103732487236387377837873878787866763675765765653839839839388378378379383787387333648732678463287468723648723687462387462387478230032342343243243333";
        String subventionAmount = "10373248723638737783787387878786676367576576565383983983938837837837938378738733364873267846328746872364872368746238746238747823003234234324323";

        ApiV1BanksRequest apiV1BanksRequest = new ApiV1BanksRequest.Builder(price,subventionAmount)
                .setMid(emiMerchant.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();

        ApiV1Bank api = new ApiV1Bank(emiMerchant.getId(), apiV1BanksRequest);
        Response r = api.execute();
        JsonPath j = r.then()
                .statusCode(200)
                .body("body.resultInfo.resultStatus", equalToIgnoringCase("U"),
                        "body.resultInfo.resultCode", equalToIgnoringCase("00000900"),
                        "body.resultInfo.resultMsg", equalToIgnoringCase("System error"))
                .extract().jsonPath();
    }

    //Tenure API

    @Test(description = "Tenure API : Verify the response when api is hit with amount based support i.e by adding price and subventionAmount in the request",groups = "P0")
    public void t47tenureAPIPhase2() throws Exception {
        Constants.MerchantType emiMerchant = Constants.MerchantType.EMI;
        User user = userManager.getForRead(Label.BASIC);
        String price ="1000";
        String subventionAmount = "200";

        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder(price,subventionAmount)
                .setMid(emiMerchant.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();

        ApiV1Tenure api = new ApiV1Tenure(emiMerchant.getId(), req);
        Response r = api.execute();
        r.then()
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

    @Test(description = "Tenure API : Verify the response if one of the request param for amount based strategy is not sent in the request (\"price\",\"subventionAmount\")",groups = "P0")
    public void t48tenureAPIPhase2() throws Exception {
        Constants.MerchantType emiMerchant = Constants.MerchantType.EMI;
        User user = userManager.getForRead(Label.BASIC);
        String price ="1000";
        String subventionAmount = "";

        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder(price,subventionAmount)
                .setMid(emiMerchant.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();

        ApiV1Tenure api = new ApiV1Tenure(emiMerchant.getId(), req);
        Response r = api.execute();
        JsonPath j = r.then()
                .spec(INVALID_AMOUNT_DETAILS)
                .extract().jsonPath();

    }

    @Test(description = "Tenure API : Verify the API response with token type SSO (with incorrect sso))",groups = "P0")
    public void t49tenureAPIPhase2() throws Exception {
        Constants.MerchantType emiMerchant = Constants.MerchantType.EMI;
        String price ="1000";
        String subventionAmount = "200";

        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder(price,subventionAmount)
                .setMid(emiMerchant.getId())
                .setTokenType("SSO")
                .setToken("INCORRECT SSO TOKEN")
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();

        ApiV1Tenure api = new ApiV1Tenure(emiMerchant.getId(), req);
        Response r = api.execute();
        JsonPath j = r.then()
                .body("body.resultInfo.resultStatus",equalTo("F"))
                .body("body.resultInfo.resultCode",equalTo("2004"))
                .body("body.resultInfo.resultMsg", equalTo("SSO Token is invalid"))
                .extract().jsonPath();

    }


    @Test(description = "Tenure API : Verify the API response with token type CHECKSUM with correct checksum",groups = "P0")
    public void t50tenureAPIPhase2()  {
        Constants.MerchantType emiMerchant = Constants.MerchantType.EMI;
        String price ="1000";
        String subventionAmount = "200";

        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder(price,subventionAmount)
                .setMid(emiMerchant.getId())
                .setTokenType("CHECKSUM")
                .setMerchantKey(emiMerchant.getKey())
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();

        ApiV1Tenure api = new ApiV1Tenure(emiMerchant.getId(), req);
        Response r = api.execute();
        r.then()
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

    @Test(description = "Tenure API : Verify the API response with token type CHECKSUM with incorrect checksum",groups = "P0")
    public void t51tenureAPIPhase2()  {
        Constants.MerchantType emiMerchant = Constants.MerchantType.EMI;
        String price ="1000";
        String subventionAmount = "200";

        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder(price,subventionAmount)
                .setMid(emiMerchant.getId())
                .setTokenType("CHECKSUM")
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();

        ApiV1Tenure api = new ApiV1Tenure(emiMerchant.getId(), req);
        Response r = api.execute();
        JsonPath j = r.then()
                .spec(CHECKSUM_PROVIDED_IS_INVALID)
                .extract().jsonPath();

    }


    @Test(description = "Tenure API : Verify the response when items field is sent as blank and it is sent along with the amount info",groups = "P0")
    public void t52tenureAPIPhase2() throws Exception {
        Constants.MerchantType emiMerchant = Constants.MerchantType.EMI;
        User user = userManager.getForRead(Label.BASIC);
        String price ="1000";
        String subventionAmount = "200";

        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder(price,subventionAmount)
                .setMid(emiMerchant.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .setItems(new ArrayList())
                .build();

        ApiV1Tenure api = new ApiV1Tenure(emiMerchant.getId(), req);
        Response r = api.execute();
        r.then()
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

    @Test(description = "Tenure API : Verify the response when price field is sent as blank along with a valid subventionAmount ",groups = "P0")
    public void t53tenureAPIPhase2() throws Exception {
        Constants.MerchantType emiMerchant = Constants.MerchantType.EMI;
        User user = userManager.getForRead(Label.BASIC);
        String price ="";
        String subventionAmount = "200";

        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder(price,subventionAmount)
                .setMid(emiMerchant.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();

        ApiV1Tenure api = new ApiV1Tenure(emiMerchant.getId(), req);
        Response r = api.execute();
        r.then()
                .spec(INVALID_AMOUNT_DETAILS);


    }

    @Test(description = "Tenure API : Verify the response when price field is sent as blank along with a blank subventionAmount ",groups = "P0")
    public void t54tenureAPIPhase2() throws Exception {
        Constants.MerchantType emiMerchant = Constants.MerchantType.EMI;
        User user = userManager.getForRead(Label.BASIC);
        String price ="1000";
        String subventionAmount = "";

        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder(price,subventionAmount)
                .setMid(emiMerchant.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();

        ApiV1Tenure api = new ApiV1Tenure(emiMerchant.getId(), req);
        Response r = api.execute();
        r.then()
                .spec(INVALID_AMOUNT_DETAILS);


    }



    @Test(description = "Tenure API : Verify the response when price field is sent as zero along with a valid subventionAmount  ",groups = "P0")
    public void t55tenureAPIPhase2() throws Exception {
        Constants.MerchantType emiMerchant = Constants.MerchantType.EMI;
        User user = userManager.getForRead(Label.BASIC);
        String price ="0";
        String subventionAmount = "200";

        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder(price,subventionAmount)
                .setMid(emiMerchant.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();

        ApiV1Tenure api = new ApiV1Tenure(emiMerchant.getId(), req);
        Response r = api.execute();
        r.then()
                .spec(INVALID_AMOUNT_DETAILS);


    }

    @Test(description = "Tenure API : Verify the response when a valid price field is sent along with a zero subventionAmount ",groups = "P0")
    public void t56tenureAPIPhase2() throws Exception {
        Constants.MerchantType emiMerchant = Constants.MerchantType.EMI;
        User user = userManager.getForRead(Label.BASIC);
        String price ="1000";
        String subventionAmount = "0";

        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder(price,subventionAmount)
                .setMid(emiMerchant.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();

        ApiV1Tenure api = new ApiV1Tenure(emiMerchant.getId(), req);
        Response r = api.execute();
        r.then()
                .spec(INVALID_AMOUNT_DETAILS);


    }

    @Test(description = "Tenure API :Verify the response when price and subventionAmount is sent as zero in the request ",groups = "P0")
    public void t57tenureAPIPhase2() throws Exception {
        Constants.MerchantType emiMerchant = Constants.MerchantType.EMI;
        User user = userManager.getForRead(Label.BASIC);
        String price ="0";
        String subventionAmount = "0";

        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder(price,subventionAmount)
                .setMid(emiMerchant.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();

        ApiV1Tenure api = new ApiV1Tenure(emiMerchant.getId(), req);
        Response r = api.execute();
        r.then()
                .spec(INVALID_AMOUNT_DETAILS);


    }

    @Test(description = "Tenure API :Verify the response when valid items value is sent along with price and subventionAmount as zero in the request",groups = "P0")
    public void t58tenureAPIPhase2() throws Exception {
        Constants.MerchantType emiMerchant = Constants.MerchantType.EMI;
        User user = userManager.getForRead(Label.BASIC);
        String price ="0";
        String subventionAmount = "0";

        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder(user.ssoToken(),"SSO",emiMerchant.getId())
                .setPrice(price)
                .setSubventionAmount(subventionAmount)
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();

        ApiV1Tenure api = new ApiV1Tenure(emiMerchant.getId(), req);
        Response r = api.execute();
        r.then()
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

    @Test(description = "Tenure API :Verify the response when api is hit with an bank name and code in the filter which has not been configured along with the price and subventionAmount",groups = "P0")
    public void t59tenureAPIPhase2() throws Exception {
        Constants.MerchantType emiMerchant = Constants.MerchantType.EMI;
        User user = userManager.getForRead(Label.BASIC);
        String price ="1000";
        String subventionAmount = "200";

        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder(price,subventionAmount)
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setMid(emiMerchant.getId())
                .setFilters(new Filters()
                        .setBankCode("AXIS")
                        .setCardType("DEBIT_CARD"))
                .build();

        ApiV1Tenure api = new ApiV1Tenure(emiMerchant.getId(), req);
        Response r = api.execute();
        r.then()
                .body("body.resultInfo.resultStatus",equalTo("U"))
                .body("body.resultInfo.resultCode",equalTo("00000900"))
                .body("body.resultInfo.resultMsg", equalTo("System error"));

    }


    @Test(description = "Tenure API : Verify the finalTransactionAmount when item list is sent in the amount based strategy",groups = "P0")
    public void t60tenureAPIPhase2() throws Exception {
        Constants.MerchantType emiMerchant = Constants.MerchantType.EMI;
        User user = userManager.getForRead(Label.BASIC);
        String price ="1000";
        String subventionAmount = "200";

        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder(user.ssoToken(),"SSO",emiMerchant.getId())
                .setPrice(price)
                .setSubventionAmount(subventionAmount)
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();

        ApiV1Tenure api = new ApiV1Tenure(emiMerchant.getId(), req);
        Response r = api.execute();
        r.then()
                .body("body.resultInfo.resultStatus",equalTo("F"))
                .body("body.resultInfo.resultCode",equalTo("1007"))
                .body("body.resultInfo.resultMsg", equalTo("Both item list and amount cannot be non empty in request"));

    }

    @Test(description = "Tenure API : Verify the response when amount info is sent in negative in the request",groups = "P0")
    public void t61tenureAPIPhase2() throws Exception {
        Constants.MerchantType emiMerchant = Constants.MerchantType.EMI;
        User user = userManager.getForRead(Label.BASIC);
        String price ="1000";
        String subventionAmount = "-1";

        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder(price,subventionAmount)
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setMid(emiMerchant.getId())
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();

        ApiV1Tenure api = new ApiV1Tenure(emiMerchant.getId(), req);
        Response r = api.execute();
        r.then()
                .spec(INVALID_AMOUNT_DETAILS);

    }

    @Test(description = "Tenure API : Verify the response when amount info is sent other than a Double data type value in the request",groups = "P0")
    public void t62tenureAPIPhase2() throws Exception {
        Constants.MerchantType emiMerchant = Constants.MerchantType.EMI;
        User user = userManager.getForRead(Label.BASIC);
        String price ="1000";
        String subventionAmount = "1oo9";

        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder(price,subventionAmount)
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setMid(emiMerchant.getId())
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();

        ApiV1Tenure api = new ApiV1Tenure(emiMerchant.getId(), req);
        Response r = api.execute();
        JsonPath j = r.then()
                .statusCode(200)
                .body("body.resultInfo.resultStatus", equalToIgnoringCase("U"),
                        "body.resultInfo.resultCode", equalToIgnoringCase("00000900"),
                        "body.resultInfo.resultMsg", equalToIgnoringCase("System error"))
                .extract().jsonPath();

    }


    //////// Validate API  ////////////

    @Test(description = "Validate API : Verify the response when api is hit with amount based support i.e by adding price and subventionAmount in the request",groups = "P0")
    public void t63validateAPIPhase2() throws Exception {
        Constants.MerchantType emiMerchant = MerchantType.EMI;
        User user = userManager.getForRead(Label.BASIC);
        String price ="1000";
        String subventionAmount = "200";

        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder(price,subventionAmount)
                .setMid(emiMerchant.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();

        ApiV1Tenure api = new ApiV1Tenure(emiMerchant.getId(), req);
        Response r = api.execute();
        JsonPath tenureResp = r.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.planDetails.emiType", hasItems("SUBVENTION"))
                .extract().jsonPath();

        String planId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId");
        BigInteger intplanid= new BigInteger(planId);
        ApiV1ValidateRequest req2 = new ApiV1ValidateRequest.Builder(price,subventionAmount)
                .setMid(emiMerchant.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setPlanId(intplanid)
                .setPaymentDetails(
                        new PaymentDetails()
                                .setTotalTransactionAmount(price)
                                .setCardNumber("|" + PGPHelpers.getFormattedPaymentDetails("EMI", new PaymentDTO())))
                .setGenerateTokenForIntent(true)
                .build();
        ApiV1Validate api2 = new ApiV1Validate(emiMerchant.getId(), req2);
        Response r2 = api2.execute();
        r2.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.bankId", not(empty()),
                        "body.bankId", not(empty()),
                        "body.bankName", not(empty()),
                        "body.bankCode", not(empty()),
                        "body.cardType", not(empty()),
                        "body.bankLogoUrl", not(empty()),
                        "body.planId", equalToIgnoringCase(planId),
                        "body.pgPlanId", not(empty()),
                        "body.rate", not(empty()),
                        "body.interval", not(empty()),
                        "body.emi", not(empty()),
                        "body.interest", not(empty()),
                        "body.emiType", not(empty()),
                        "body.emiLabel", not(empty()),
                        "body.gratifications", not(empty()),
                        "body.gratifications.value", everyItem(not(empty())),
                        "body.gratifications.type", everyItem(not(empty())),
                        "body.gratifications.label", everyItem(not(empty())),
                        "body.itemBreakUpList", not(empty())
                )
                .body("body.emiSubventionToken", not(empty()))
                .body("body.finalTransactionAmount", not(empty()));
    }

    @Test(description = "Validate API : Verify the response if one of the request param for amount based strategy is not sent in the request (\"price\",\"subventionAmount\")",groups = "P0")
    public void t64validateAPIPhase2() throws Exception {
        Constants.MerchantType emiMerchant = MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String price ="1000";
        String subventionAmount = "200";

        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder(price,subventionAmount)
                .setMid(emiMerchant.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();

        ApiV1Tenure api = new ApiV1Tenure(emiMerchant.getId(), req);
        Response r = api.execute();
        JsonPath tenureResp = r.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.planDetails.emiType", hasItems("SUBVENTION"))
                .extract().jsonPath();

        String planId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId");
        BigInteger intplanid= new BigInteger(planId);
        ApiV1ValidateRequest req2 = new ApiV1ValidateRequest.Builder(price,"")
                .setMid(emiMerchant.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setPlanId(intplanid)
                .setPaymentDetails(
                        new PaymentDetails()
                                .setTotalTransactionAmount(price)
                                .setCardNumber("|" + PGPHelpers.getFormattedPaymentDetails("EMI", new PaymentDTO())))
                .setGenerateTokenForIntent(true)
                .build();
        ApiV1Validate api2 = new ApiV1Validate(emiMerchant.getId(), req2);
        Response r2 = api2.execute();
        r2.then()
                .spec(MANDATORY_PARAM_MISSING);
    }


    @Test(description = "Validate API : Verify the API response with token type SSO (with incorrect sso))",groups = "P1")
    public void t65validateAPIPhase2() throws Exception {
        Constants.MerchantType emiMerchant = MerchantType.EMI;
        User user = userManager.getForRead(Label.BASIC);
        String price ="1000";
        String subventionAmount = "200";

        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder(price,subventionAmount)
                .setMid(emiMerchant.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();

        ApiV1Tenure api = new ApiV1Tenure(emiMerchant.getId(), req);
        Response r = api.execute();
        JsonPath tenureResp = r.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.planDetails.emiType", hasItems("SUBVENTION"))
                .extract().jsonPath();

        String planId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId");
        BigInteger intplanid= new BigInteger(planId);
        ApiV1ValidateRequest req2 = new ApiV1ValidateRequest.Builder(price,subventionAmount)
                .setMid(emiMerchant.getId())
                .setTokenType("SSO")
                .setToken("Incorrect SSO token")
                .setPlanId(intplanid)
                .setPaymentDetails(
                        new PaymentDetails()
                                .setTotalTransactionAmount(price)
                                .setCardNumber("|" + PGPHelpers.getFormattedPaymentDetails("EMI", new PaymentDTO())))
                .setGenerateTokenForIntent(true)
                .build();
        ApiV1Validate api2 = new ApiV1Validate(emiMerchant.getId(), req2);
        Response r2 = api2.execute();
        r2.then()
                .spec(SSO_TOKEN_INVALID);
    }

    @Test(description = "Validate API : Verify the API response with token type CHECKSUM with correct checksum",groups = "P1")
    public void t66validateAPIPhase2() throws Exception {
        Constants.MerchantType emiMerchant = MerchantType.EMI;
        User user = userManager.getForRead(Label.BASIC);
        String price ="1000";
        String subventionAmount = "200";

        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder(price,subventionAmount)
                .setMid(emiMerchant.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();

        ApiV1Tenure api = new ApiV1Tenure(emiMerchant.getId(), req);
        Response r = api.execute();
        JsonPath tenureResp = r.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.planDetails.emiType", hasItems("SUBVENTION"))
                .extract().jsonPath();

        String planId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId");
        BigInteger intplanid= new BigInteger(planId);
        ApiV1ValidateRequest req2 = new ApiV1ValidateRequest.Builder(price,subventionAmount)
                .setMid(emiMerchant.getId())
                .setTokenType("CHECKSUM")
                .setMerchantKey(emiMerchant.getKey())
                .setPlanId(intplanid)
                .setCustomerId(CommonHelpers.generateOrderId())
                .setPaymentDetails(
                        new PaymentDetails()
                                .setTotalTransactionAmount(price)
                                .setCardNumber("|" + PGPHelpers.getFormattedPaymentDetails("EMI", new PaymentDTO())))
                .setGenerateTokenForIntent(true)
                .build();
        ApiV1Validate api2 = new ApiV1Validate(emiMerchant.getId(), req2);
        Response r2 = api2.execute();
        r2.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.bankId", not(empty()),
                        "body.bankId", not(empty()),
                        "body.bankName", not(empty()),
                        "body.bankCode", not(empty()),
                        "body.cardType", not(empty()),
                        "body.bankLogoUrl", not(empty()),
                        "body.planId", equalToIgnoringCase(planId),
                        "body.pgPlanId", not(empty()),
                        "body.rate", not(empty()),
                        "body.interval", not(empty()),
                        "body.emi", not(empty()),
                        "body.interest", not(empty()),
                        "body.emiType", not(empty()),
                        "body.emiLabel", not(empty()),
                        "body.gratifications", not(empty()),
                        "body.gratifications.value", everyItem(not(empty())),
                        "body.gratifications.type", everyItem(not(empty())),
                        "body.gratifications.label", everyItem(not(empty())),
                        "body.itemBreakUpList", not(empty())
                )
                .body("body.emiSubventionToken", not(empty()))
                .body("body.finalTransactionAmount", not(empty()));
    }


    @Test(description = "Validate API : Verify the API response with token type CHECKSUM with incorrect checksum",groups = "P1")
    public void t67validateAPIPhase2() throws Exception {
        Constants.MerchantType emiMerchant = MerchantType.EMI;
        User user = userManager.getForRead(Label.BASIC);
        String price ="1000";
        String subventionAmount = "200";

        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder(price,subventionAmount)
                .setMid(emiMerchant.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();

        ApiV1Tenure api = new ApiV1Tenure(emiMerchant.getId(), req);
        Response r = api.execute();
        JsonPath tenureResp = r.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.planDetails.emiType", hasItems("SUBVENTION"))
                .extract().jsonPath();

        String planId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId");
        BigInteger intplanid= new BigInteger(planId);
        ApiV1ValidateRequest req2 = new ApiV1ValidateRequest.Builder(price,subventionAmount)
                .setMid(emiMerchant.getId())
                .setTokenType("CHECKSUM")
                .setPlanId(intplanid)
                .setCustomerId(CommonHelpers.generateOrderId())
                .setPaymentDetails(
                        new PaymentDetails()
                                .setTotalTransactionAmount(price)
                                .setCardNumber("|" + PGPHelpers.getFormattedPaymentDetails("EMI", new PaymentDTO())))
                .setGenerateTokenForIntent(true)
                .build();
        ApiV1Validate api2 = new ApiV1Validate(emiMerchant.getId(), req2);
        Response r2 = api2.execute();
        r2.then()
                .spec(CHECKSUM_PROVIDED_IS_INVALID);

    }

    @Test(description = "Validate API : Verify the response when customerId not provided",groups = "P0")
    public void t68validateAPIPhase2() throws Exception {
        Constants.MerchantType emiMerchant = MerchantType.EMI;
        User user = userManager.getForRead(Label.BASIC);
        String price ="1000";
        String subventionAmount = "200";

        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder(price,subventionAmount)
                .setMid(emiMerchant.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();

        ApiV1Tenure api = new ApiV1Tenure(emiMerchant.getId(), req);
        Response r = api.execute();
        JsonPath tenureResp = r.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.planDetails.emiType", hasItems("SUBVENTION"))
                .extract().jsonPath();

        String planId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId");
        BigInteger intplanid= new BigInteger(planId);
        ApiV1ValidateRequest req2 = new ApiV1ValidateRequest.Builder(price,subventionAmount)
                .setMid(emiMerchant.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setPlanId(intplanid)
                .setPaymentDetails(new PaymentDetails())
                .setCustomerId(null)
                .setPaymentDetails(
                        new PaymentDetails()
                                .setTotalTransactionAmount(price)
                                .setCardNumber("|" + PGPHelpers.getFormattedPaymentDetails("EMI", new PaymentDTO())))
                .build();
        ApiV1Validate api2 = new ApiV1Validate(emiMerchant.getId(), req2);
        Response r2 = api2.execute();
        r2.then()
                .statusCode(200)
                .spec(SUCCESS_RESPONSE);


    }

    @Test(description = "Validate API : Verify the response with incorrect MID and without item info",groups = "P0")
    public void t69validateAPIPhase2() throws Exception {
        Constants.MerchantType emiMerchant = MerchantType.EMI;
        User user = userManager.getForRead(Label.BASIC);
        String price ="1000";
        String subventionAmount = "200";

        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder(price,subventionAmount)
                .setMid(emiMerchant.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();

        ApiV1Tenure api = new ApiV1Tenure(emiMerchant.getId(), req);
        Response r = api.execute();
        JsonPath tenureResp = r.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.planDetails.emiType", hasItems("SUBVENTION"))
                .extract().jsonPath();

        String planId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId");
        BigInteger intplanid= new BigInteger(planId);
        ApiV1ValidateRequest req2 = new ApiV1ValidateRequest.Builder(price,subventionAmount)
                .setMid(MerchantType.PGOnly.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setPlanId(intplanid)
                .setPaymentDetails(
                        new PaymentDetails()
                                .setTotalTransactionAmount(price)
                                .setCardNumber("|" + PGPHelpers.getFormattedPaymentDetails("EMI", new PaymentDTO())))
                .setGenerateTokenForIntent(true)
                .build();
        ApiV1Validate api2 = new ApiV1Validate(MerchantType.PGOnly.getId(), req2);
        Response r2 = api2.execute();
        r2.then()
                .statusCode(200)
                .body("body.resultInfo.resultStatus", equalToIgnoringCase("U"),
                        "body.resultInfo.resultCode", equalToIgnoringCase("00000900"),
                        "body.resultInfo.resultMsg", equalToIgnoringCase("System error"));


    }

    @Test(description = "Validate API : Verify the response with customerId as NULL and without item info",groups = "P0")
    public void t70validateAPIPhase2() throws Exception {
        Constants.MerchantType emiMerchant = MerchantType.EMI;
        User user = userManager.getForRead(Label.BASIC);
        String price ="1000";
        String subventionAmount = "200";

        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder(price,subventionAmount)
                .setMid(emiMerchant.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();

        ApiV1Tenure api = new ApiV1Tenure(emiMerchant.getId(), req);
        Response r = api.execute();
        JsonPath tenureResp = r.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.planDetails.emiType", hasItems("SUBVENTION"))
                .extract().jsonPath();

        String planId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId");
        BigInteger intplanid= new BigInteger(planId);
        ApiV1ValidateRequest req2 = new ApiV1ValidateRequest.Builder(price,subventionAmount)
                .setMid(emiMerchant.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setPlanId(intplanid)
                .setPaymentDetails(
                        new PaymentDetails()
                                .setTotalTransactionAmount(price)
                                .setCardNumber("|" + PGPHelpers.getFormattedPaymentDetails("EMI", new PaymentDTO())))
                .setGenerateTokenForIntent(true)
                .setCustomerId("")
                .build();
        ApiV1Validate api2 = new ApiV1Validate(emiMerchant.getId(), req2);
        Response r2 = api2.execute();
        r2.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.bankId", not(empty()),
                        "body.bankId", not(empty()),
                        "body.bankName", not(empty()),
                        "body.bankCode", not(empty()),
                        "body.cardType", not(empty()),
                        "body.bankLogoUrl", not(empty()),
                        "body.planId", equalToIgnoringCase(planId),
                        "body.pgPlanId", not(empty()),
                        "body.rate", not(empty()),
                        "body.interval", not(empty()),
                        "body.emi", not(empty()),
                        "body.interest", not(empty()),
                        "body.emiType", not(empty()),
                        "body.emiLabel", not(empty()),
                        "body.gratifications", not(empty()),
                        "body.gratifications.value", everyItem(not(empty())),
                        "body.gratifications.type", everyItem(not(empty())),
                        "body.gratifications.label", everyItem(not(empty())),
                        "body.itemBreakUpList", not(empty())
                )
                .body("body.emiSubventionToken", not(empty()))
                .body("body.finalTransactionAmount", not(empty()));


    }

    @Test(description = "Validate API : Verify the response with incorrect plan ID",groups = "P0")
    public void t71validateAPIPhase2() throws Exception {
        Constants.MerchantType emiMerchant = MerchantType.EMI;
        User user = userManager.getForRead(Label.BASIC);
        String price ="1000";
        String subventionAmount = "200";

        String planId = "9631";
        BigInteger intplanid= new BigInteger(planId);
        ApiV1ValidateRequest req2 = new ApiV1ValidateRequest.Builder(price,subventionAmount)
                .setMid(emiMerchant.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setPlanId(intplanid)
                .setPaymentDetails(
                        new PaymentDetails()
                                .setTotalTransactionAmount(price)
                                .setCardNumber("|" + PGPHelpers.getFormattedPaymentDetails("EMI", new PaymentDTO())))
                .setGenerateTokenForIntent(true)
                .build();
        ApiV1Validate api2 = new ApiV1Validate(emiMerchant.getId(), req2);
        Response r2 = api2.execute();
        r2.then()
                .statusCode(200)
                .body("body.resultInfo.resultStatus", equalToIgnoringCase("U"),
                        "body.resultInfo.resultCode", equalToIgnoringCase("00000900"),
                        "body.resultInfo.resultMsg", equalToIgnoringCase("System error"));


    }

    @Test(description = "Validate API : Verify the response with value as false in param - generateTokenForIntent",groups = "P0")
    public void t72validateAPIPhase2() throws Exception {
        Constants.MerchantType emiMerchant = MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String price ="1000";
        String subventionAmount = "200";

        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder(price,subventionAmount)
                .setMid(emiMerchant.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();

        ApiV1Tenure api = new ApiV1Tenure(emiMerchant.getId(), req);
        Response r = api.execute();
        JsonPath tenureResp = r.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.planDetails.emiType", hasItems("SUBVENTION"))
                .extract().jsonPath();

        String planId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId");
        BigInteger intplanid= new BigInteger(planId);
        ApiV1ValidateRequest req2 = new ApiV1ValidateRequest.Builder(price,subventionAmount)
                .setMid(emiMerchant.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setPlanId(intplanid)
                .setPaymentDetails(
                        new PaymentDetails()
                                .setTotalTransactionAmount(price)
                                .setCardNumber("|" + PGPHelpers.getFormattedPaymentDetails("EMI", new PaymentDTO())))
                .setGenerateTokenForIntent(false)
                .build();
        ApiV1Validate api2 = new ApiV1Validate(emiMerchant.getId(), req2);
        Response r2 = api2.execute();
        r2.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.emiSubventionToken", equalTo(null));



    }

    @Test(description = "Validate API : Verify that if cacheCardToken is sent instead of cardNumber and generateTokenForIntent is false then success response should be returned",groups = "P0")
    public void t73validateAPIPhase2() throws Exception {
        Constants.MerchantType emiMerchant = MerchantType.EMI;
        PaymentDTO paymentDTO = new PaymentDTO();
        String orderId = CommonHelpers.generateOrderId();
        User user = userManager.getForRead(Label.BASIC);
        String price ="1000";
        String subventionAmount = "200";

        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder(price,subventionAmount)
                .setMid(emiMerchant.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();

        ApiV1Tenure api = new ApiV1Tenure(emiMerchant.getId(), req);
        Response r = api.execute();
        JsonPath tenureResp = r.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.planDetails.emiType", hasItems("SUBVENTION"))
                .extract().jsonPath();

        String planId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId");
        BigInteger intplanid= new BigInteger(planId);
        String cacheCardToken = new GetCardToken(LocalConfig.PGP_HOST, emiMerchant.getId(), orderId,
                paymentDTO.getCreditCardNumber(),
                paymentDTO.getExpMonth(),
                paymentDTO.getExpYear(),
                paymentDTO.getCvvNumber())
                .execute()
                .then().statusCode(200)
                .extract().jsonPath().getString("TOKEN");

        ApiV1ValidateRequest req2 = new ApiV1ValidateRequest.Builder(price,subventionAmount)
                .setMid(emiMerchant.getId())
                .setTokenType("SSO")
                .setCustomerId(orderId)
                .setToken(user.ssoToken())
                .setPlanId(intplanid)
                .setPaymentDetails(
                        new PaymentDetails()
                                .setTotalTransactionAmount(price)
                                .setCardNumber(""))
                .setCacheCardToken(cacheCardToken)
                .setGenerateTokenForIntent(false)
                .build();
        ApiV1Validate api2 = new ApiV1Validate(emiMerchant.getId(), req2);
        Response r2 = api2.execute();
        r2.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.emiSubventionToken", equalTo(null));


    }


    @Test(description = "Validate API : Verify that if cacheCardToken is sent instead of cardNumber and generateTokenForIntent is true then success response should be returned",groups = "P0")
    public void t74validateAPIPhase2() throws Exception {
        Constants.MerchantType emiMerchant = MerchantType.EMI;
        PaymentDTO paymentDTO = new PaymentDTO();
        String orderId = CommonHelpers.generateOrderId();
        User user = userManager.getForRead(Label.BASIC);
        String price ="1000";
        String subventionAmount = "200";

        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder(price,subventionAmount)
                .setMid(emiMerchant.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();

        ApiV1Tenure api = new ApiV1Tenure(emiMerchant.getId(), req);
        Response r = api.execute();
        JsonPath tenureResp = r.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.planDetails.emiType", hasItems("SUBVENTION"))
                .extract().jsonPath();

        String planId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId");
        
        String cacheCardToken = new GetCardToken(LocalConfig.PGP_HOST, emiMerchant.getId(), orderId,
                paymentDTO.getCreditCardNumber(),
                paymentDTO.getExpMonth(),
                paymentDTO.getExpYear(),
                paymentDTO.getCvvNumber())
                .execute()
                .then().statusCode(200)
                .extract().jsonPath().getString("TOKEN");
        BigInteger intplanid= new BigInteger(planId);
        ApiV1ValidateRequest req2 = new ApiV1ValidateRequest.Builder(price,subventionAmount)
                .setMid(emiMerchant.getId())
                .setTokenType("SSO")
                .setCustomerId(orderId)
                .setToken(user.ssoToken())
                .setPlanId(intplanid)
                .setPaymentDetails(
                        new PaymentDetails()
                                .setTotalTransactionAmount(price)
                                .setCardNumber(""))
                .setCacheCardToken(cacheCardToken)
                .setGenerateTokenForIntent(true)
                .build();
        ApiV1Validate api2 = new ApiV1Validate(emiMerchant.getId(), req2);
        Response r2 = api2.execute();
        r2.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.bankId", not(empty()),
                        "body.bankId", not(empty()),
                        "body.bankName", not(empty()),
                        "body.bankCode", not(empty()),
                        "body.cardType", not(empty()),
                        "body.bankLogoUrl", not(empty()),
                        "body.planId", equalToIgnoringCase(planId),
                        "body.pgPlanId", not(empty()),
                        "body.rate", not(empty()),
                        "body.interval", not(empty()),
                        "body.emi", not(empty()),
                        "body.interest", not(empty()),
                        "body.emiType", not(empty()),
                        "body.emiLabel", not(empty()),
                        "body.gratifications", not(empty()),
                        "body.gratifications.value", everyItem(not(empty())),
                        "body.gratifications.type", everyItem(not(empty())),
                        "body.gratifications.label", everyItem(not(empty())),
                        "body.itemBreakUpList", not(empty())
                )
                .body("body.emiSubventionToken", not(empty()))
                .body("body.finalTransactionAmount", not(empty()));

    }

    @Test(description = "Validate API :Verify the response with offerDetails and price sent in the request and subventionAmount is not sent",groups = "P0")
    public void t75validateAPIPhase2() throws Exception {
        Constants.MerchantType emiMerchant = MerchantType.EMI;
        PaymentDTO paymentDTO = new PaymentDTO();
        String orderId = CommonHelpers.generateOrderId();
        User user = userManager.getForRead(Label.BASIC);
        String price ="1000";
        String subventionAmount = "200";

        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder(price,subventionAmount)
                .setMid(emiMerchant.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();

        ApiV1Tenure api = new ApiV1Tenure(emiMerchant.getId(), req);
        Response r = api.execute();
        JsonPath tenureResp = r.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.planDetails.emiType", hasItems("SUBVENTION"))
                .extract().jsonPath();

        String planId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId");
        BigInteger intplanid= new BigInteger(planId);
        String cacheCardToken = new GetCardToken(LocalConfig.PGP_HOST, emiMerchant.getId(), orderId,
                paymentDTO.getCreditCardNumber(),
                paymentDTO.getExpMonth(),
                paymentDTO.getExpYear(),
                paymentDTO.getCvvNumber())
                .execute()
                .then().statusCode(200)
                .extract().jsonPath().getString("TOKEN");

        ApiV1ValidateRequest req2 = new ApiV1ValidateRequest.Builder(price,subventionAmount)
                .setMid(emiMerchant.getId())
                .setTokenType("SSO")
                .setCustomerId(orderId)
                .setToken(user.ssoToken())
                .setPlanId(intplanid)
                .setOfferDetails(new OfferDetails().setOfferId("40599"))
                .setPaymentDetails(
                        new PaymentDetails()
                                .setTotalTransactionAmount(price)
                                .setCardNumber(""))
                .setCacheCardToken(cacheCardToken)
                .setGenerateTokenForIntent(true)
                .build();
        ApiV1Validate api2 = new ApiV1Validate(emiMerchant.getId(), req2);
        Response r2 = api2.execute();
        r2.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.bankId", not(empty()),
                        "body.bankId", not(empty()),
                        "body.bankName", not(empty()),
                        "body.bankCode", not(empty()),
                        "body.cardType", not(empty()),
                        "body.bankLogoUrl", not(empty()),
                        "body.planId", equalToIgnoringCase(planId),
                        "body.pgPlanId", not(empty()),
                        "body.rate", not(empty()),
                        "body.interval", not(empty()),
                        "body.emi", not(empty()),
                        "body.interest", not(empty()),
                        "body.emiType", not(empty()),
                        "body.emiLabel", not(empty()),
                        "body.gratifications", not(empty()),
                        "body.gratifications.value", everyItem(not(empty())),
                        "body.gratifications.type", everyItem(not(empty())),
                        "body.gratifications.label", everyItem(not(empty())),
                        "body.itemBreakUpList", not(empty())
                )
                .body("body.emiSubventionToken", not(empty()))
                .body("body.finalTransactionAmount", not(empty()));


    }

    @Test(description = "Verify the response with expired cacheCardToken in the request",groups = "P0")
    public void t76validateAPIPhase2() throws Exception {
        Constants.MerchantType emiMerchant = MerchantType.EMI;
        String orderId = CommonHelpers.generateOrderId();
        User user = userManager.getForRead(Label.BASIC);
        String price ="1000";
        String subventionAmount = "200";

        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder(price,subventionAmount)
                .setMid(emiMerchant.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();

        ApiV1Tenure api = new ApiV1Tenure(emiMerchant.getId(), req);
        Response r = api.execute();
        JsonPath tenureResp = r.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.planDetails.emiType", hasItems("SUBVENTION"))
                .extract().jsonPath();

        String planId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId");

        String cacheCardToken = "cd92e07a4dc54b23b970d75f1957997615942994856681"; //expired cache token
        BigInteger intplanid= new BigInteger(planId);
        ApiV1ValidateRequest req2 = new ApiV1ValidateRequest.Builder(price,subventionAmount)
                .setMid(emiMerchant.getId())
                .setTokenType("SSO")
                .setCustomerId(orderId)
                .setToken(user.ssoToken())
                .setPlanId(intplanid)
                .setPaymentDetails(
                        new PaymentDetails()
                                .setTotalTransactionAmount(price)
                                .setCardNumber(""))
                .setCacheCardToken(cacheCardToken)
                .setGenerateTokenForIntent(true)
                .build();
        ApiV1Validate api2 = new ApiV1Validate(emiMerchant.getId(), req2);
        Response r2 = api2.execute();
        r2.then()
                .statusCode(200)
                .body("body.resultInfo.resultStatus", equalToIgnoringCase("F"),
                        "body.resultInfo.resultCode", equalToIgnoringCase("1006"),
                        "body.resultInfo.resultMsg", equalToIgnoringCase("card details are not valid"));

    }

    @Test(description = "Validate API :Verify the response with blank cacheCardToken in the request (card details also blank)",groups = "P0")
    public void t77validateAPIPhase2() throws Exception {
        Constants.MerchantType emiMerchant = MerchantType.EMI_DISCOVERY;
        String orderId = CommonHelpers.generateOrderId();
        User user = userManager.getForRead(Label.BASIC);
        String price ="1000";
        String subventionAmount = "200";

        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder(price,subventionAmount)
                .setMid(emiMerchant.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();

        ApiV1Tenure api = new ApiV1Tenure(emiMerchant.getId(), req);
        Response r = api.execute();
        JsonPath tenureResp = r.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.planDetails.emiType", hasItems("SUBVENTION"))
                .extract().jsonPath();

        String planId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId");
        BigInteger intplanid= new BigInteger(planId);
        ApiV1ValidateRequest req2 = new ApiV1ValidateRequest.Builder(price,subventionAmount)
                .setMid(emiMerchant.getId())
                .setTokenType("SSO")
                .setCustomerId(orderId)
                .setToken(user.ssoToken())
                .setPlanId(intplanid)
                .setPaymentDetails(
                        new PaymentDetails()
                                .setTotalTransactionAmount(price)
                                .setCardNumber(""))
                .setCacheCardToken("")
                .setGenerateTokenForIntent(true)
                .build();
        ApiV1Validate api2 = new ApiV1Validate(emiMerchant.getId(), req2);
        Response r2 = api2.execute();
        r2.then()
                .statusCode(200)
                .body("body.resultInfo.resultStatus", equalToIgnoringCase("F"),
                        "body.resultInfo.resultCode", equalToIgnoringCase("1007"),
                        "body.resultInfo.resultMsg", equalToIgnoringCase("Card Details are missing"));

    }

    @Test(description = "Validate API :Verify the API with wrong totalTransactionAmount in payment details section",groups = "P0")
    public void t78validateAPIPhase2() throws Exception {
        Constants.MerchantType emiMerchant = MerchantType.EMI;
        String orderId = CommonHelpers.generateOrderId();
        User user = userManager.getForRead(Label.BASIC);
        String price ="1000";
        String subventionAmount = "200";

        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder(price,subventionAmount)
                .setMid(emiMerchant.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();

        ApiV1Tenure api = new ApiV1Tenure(emiMerchant.getId(), req);
        Response r = api.execute();
        JsonPath tenureResp = r.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.planDetails.emiType", hasItems("SUBVENTION"))
                .extract().jsonPath();

        String planId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId");
        BigInteger intplanid= new BigInteger(planId);
        ApiV1ValidateRequest req2 = new ApiV1ValidateRequest.Builder(price,subventionAmount)
                .setMid(emiMerchant.getId())
                .setTokenType("SSO")
                .setCustomerId(orderId)
                .setToken(user.ssoToken())
                .setPlanId(intplanid)
                .setPaymentDetails(
                        new PaymentDetails()
                                .setTotalTransactionAmount("1600")
                                .setCardNumber("|" + PGPHelpers.getFormattedPaymentDetails("EMI", new PaymentDTO())))
                .setGenerateTokenForIntent(true)
                .build();
        ApiV1Validate api2 = new ApiV1Validate(emiMerchant.getId(), req2);
        Response r2 = api2.execute();
        r2.then()
                .statusCode(200)
                .body("body.resultInfo.resultStatus", equalToIgnoringCase("F"),
                        "body.resultInfo.resultCode", equalToIgnoringCase("1007"),
                        "body.resultInfo.resultMsg", equalToIgnoringCase("items total amount  does not match the totalTransaction Amount"));

    }


    @Test(description = "Validate API :Verify the API without totalTransactionAmount in payment details section in request",groups = "P0")
    public void t79validateAPIPhase2() throws Exception {
        Constants.MerchantType emiMerchant = MerchantType.EMI;
        String orderId = CommonHelpers.generateOrderId();
        User user = userManager.getForRead(Label.BASIC);
        String price ="1000";
        String subventionAmount = "200";

        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder(price,subventionAmount)
                .setMid(emiMerchant.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();

        ApiV1Tenure api = new ApiV1Tenure(emiMerchant.getId(), req);
        Response r = api.execute();
        JsonPath tenureResp = r.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.planDetails.emiType", hasItems("SUBVENTION"))
                .extract().jsonPath();

        String planId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId");
        BigInteger intplanid= new BigInteger(planId);
        ApiV1ValidateRequest req2 = new ApiV1ValidateRequest.Builder(price,subventionAmount)
                .setMid(emiMerchant.getId())
                .setTokenType("SSO")
                .setCustomerId(orderId)
                .setToken(user.ssoToken())
                .setPlanId(intplanid)
                .setPaymentDetails(
                        new PaymentDetails()
                                .setCardNumber("|" + PGPHelpers.getFormattedPaymentDetails("EMI", new PaymentDTO())))
                .setGenerateTokenForIntent(true)
                .build();
        ApiV1Validate api2 = new ApiV1Validate(emiMerchant.getId(), req2);
        Response r2 = api2.execute();
        r2.then()
                .statusCode(200)
                .body("body.resultInfo.resultStatus", equalToIgnoringCase("F"),
                        "body.resultInfo.resultCode", equalToIgnoringCase("1007"),
                        "body.resultInfo.resultMsg", equalToIgnoringCase("Total transaction amount is missing"));

    }

    @Test(description = "Validate API :Verify the API with both payment details and cacheCardToken",groups = "P0")
    public void t80validateAPIPhase2() throws Exception {
        Constants.MerchantType emiMerchant = MerchantType.EMI;
        PaymentDTO paymentDTO = new PaymentDTO();
        String orderId = CommonHelpers.generateOrderId();
        User user = userManager.getForRead(Label.BASIC);
        String price ="1000";
        String subventionAmount = "200";

        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder(price,subventionAmount)
                .setMid(emiMerchant.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();

        String cacheCardToken = new GetCardToken(LocalConfig.PGP_HOST, emiMerchant.getId(), orderId,
                paymentDTO.getCreditCardNumber(),
                paymentDTO.getExpMonth(),
                paymentDTO.getExpYear(),
                paymentDTO.getCvvNumber())
                .execute()
                .then().statusCode(200)
                .extract().jsonPath().getString("TOKEN");

        ApiV1Tenure api = new ApiV1Tenure(emiMerchant.getId(), req);
        Response r = api.execute();
        JsonPath tenureResp = r.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.planDetails.emiType", hasItems("SUBVENTION"))
                .extract().jsonPath();

        String planId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId");
        BigInteger intplanid= new BigInteger(planId);
        ApiV1ValidateRequest req2 = new ApiV1ValidateRequest.Builder(price,subventionAmount)
                .setMid(emiMerchant.getId())
                .setTokenType("SSO")
                .setCustomerId(orderId)
                .setToken(user.ssoToken())
                .setPlanId(intplanid)
                .setPaymentDetails(
                        new PaymentDetails()
                                .setTotalTransactionAmount(price)
                                .setCardNumber("|" + PGPHelpers.getFormattedPaymentDetails("EMI", new PaymentDTO())))
                .setCacheCardToken(cacheCardToken)
                .setGenerateTokenForIntent(true)
                .build();
        ApiV1Validate api2 = new ApiV1Validate(emiMerchant.getId(), req2);
        Response r2 = api2.execute();
        r2.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.bankId", not(empty()),
                        "body.bankId", not(empty()),
                        "body.bankName", not(empty()),
                        "body.bankCode", not(empty()),
                        "body.cardType", not(empty()),
                        "body.bankLogoUrl", not(empty()),
                        "body.planId", equalToIgnoringCase(planId),
                        "body.pgPlanId", not(empty()),
                        "body.rate", not(empty()),
                        "body.interval", not(empty()),
                        "body.emi", not(empty()),
                        "body.interest", not(empty()),
                        "body.emiType", not(empty()),
                        "body.emiLabel", not(empty()),
                        "body.gratifications", not(empty()),
                        "body.gratifications.value", everyItem(not(empty())),
                        "body.gratifications.type", everyItem(not(empty())),
                        "body.gratifications.label", everyItem(not(empty())),
                        "body.itemBreakUpList", not(empty())
                )
                .body("body.emiSubventionToken", not(empty()))
                .body("body.finalTransactionAmount", not(empty()));

    }

    @Test(description = "Initiate API :Verify the initiate API response when amount in subvention token is same as amount generated in validate API",groups = "P0")
    public void t81initiateAPIPhase2() throws Exception {
        Constants.MerchantType emiMerchant = MerchantType.EMI;
        String orderId = CommonHelpers.generateOrderId();
        User user = userManager.getForRead(Label.BASIC);
        String price ="5";
        String subventionAmount = "4";

        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder(price,subventionAmount)
                .setMid(emiMerchant.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();

        ApiV1Tenure api = new ApiV1Tenure(emiMerchant.getId(), req);
        Response r = api.execute();
        JsonPath tenureResp = r.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.planDetails.emiType", hasItems("SUBVENTION"))
                .extract().jsonPath();

        String planId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId");
        String finalTxnAmount = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.finalTransactionAmount");
        BigInteger intplanid= new BigInteger(planId);
        
        ApiV1ValidateRequest req2 = new ApiV1ValidateRequest.Builder(price,subventionAmount)
                .setMid(emiMerchant.getId())
                .setTokenType("SSO")
                .setCustomerId(orderId)
                .setToken(user.ssoToken())
                .setPlanId(intplanid)
                .setPaymentDetails(
                        new PaymentDetails()
                                .setTotalTransactionAmount(price)
                                .setCardNumber("|" + PGPHelpers.getFormattedPaymentDetails("EMI", new PaymentDTO())))
                .setGenerateTokenForIntent(true)
                .build();
        ApiV1Validate api2 = new ApiV1Validate(emiMerchant.getId(), req2);
        Response r2 = api2.execute();
        JsonPath validateResponse =  r2.then()
                .spec(SUCCESS_RESPONSE).extract().jsonPath();

        String emiSubventionToken = validateResponse.getString("body.emiSubventionToken");

        //Initiate Txn
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),emiMerchant)
                .setTxnValue(price)
                .setPayableAmount(new TxnAmount(finalTxnAmount))
                .setOrderId(orderId)
                .setEmiSubventionToken(emiSubventionToken)
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        Assertions.assertThat(initTxnResponse.getBody().getTxnToken()).
                as("Txn token is not generated in initiate txn response").isNotEmpty();

    }

    @Test(description = "Initiate API : Verify the initiate API response when payable amount in subvention token is different from payable amount generated in validate API but txn amount is same as the amount generated in validate api",groups = "P0")
    public void t82initiateAPIPhase2() throws Exception {
        Constants.MerchantType emiMerchant = MerchantType.EMI;
        String orderId = CommonHelpers.generateOrderId();
        User user = userManager.getForRead(Label.BASIC);
        String price ="5";
        String subventionAmount = "4";

        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder(price,subventionAmount)
                .setMid(emiMerchant.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();

        ApiV1Tenure api = new ApiV1Tenure(emiMerchant.getId(), req);
        Response r = api.execute();
        JsonPath tenureResp = r.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.planDetails.emiType", hasItems("SUBVENTION"))
                .extract().jsonPath();

        String planId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId");
        BigInteger intplanid= new BigInteger(planId);
        ApiV1ValidateRequest req2 = new ApiV1ValidateRequest.Builder(price,subventionAmount)
                .setMid(emiMerchant.getId())
                .setTokenType("SSO")
                .setCustomerId(orderId)
                .setToken(user.ssoToken())
                .setPlanId(intplanid)
                .setPaymentDetails(
                        new PaymentDetails()
                                .setTotalTransactionAmount(price)
                                .setCardNumber("|" + PGPHelpers.getFormattedPaymentDetails("EMI", new PaymentDTO())))
                .setGenerateTokenForIntent(true)
                .build();
        ApiV1Validate api2 = new ApiV1Validate(emiMerchant.getId(), req2);
        Response r2 = api2.execute();
        JsonPath validateResponse =  r2.then()
                .spec(SUCCESS_RESPONSE).extract().jsonPath();

        String emiSubventionToken = validateResponse.getString("body.emiSubventionToken");

        //Initiate Txn
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),emiMerchant)
                .setTxnValue(price)
                .setPayableAmount(new TxnAmount("1"))
                .setOrderId(orderId)
                .setEmiSubventionToken(emiSubventionToken)
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        Assertions.assertThat(initTxnResponse.getBody().getTxnToken()).
                as("Txn token should not be generated in initiate txn response").isNull();
        Assertions.assertThat(initTxnResponse.getBody().getResultInfo().getResultMsg()).
                as("Result Msg is incorrect").isEqualTo("Txn amount is invalid");

    }

    @Test(description = "Initiate, PTC API : Checksum -> Verify the initiate API response when payable amount in subvention token is different from payable amount generated in validate API but txn amount is same as the amount generated in validate api",groups = "P0")
    public void t83initiatePTCAPIPhase2() throws Exception {
        Constants.MerchantType emiMerchant = MerchantType.EMI;
        String orderId = CommonHelpers.generateOrderId();
        User user = userManager.getForRead(Label.BASIC);
        String price ="5";
        String subventionAmount = "4";

        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder(price,subventionAmount)
                .setMid(emiMerchant.getId())
                .setTokenType("CHECKSUM")
                .setMerchantKey(emiMerchant.getKey())
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();

        ApiV1Tenure api = new ApiV1Tenure(emiMerchant.getId(), req);
        Response r = api.execute();
        JsonPath tenureResp = r.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.planDetails.emiType", hasItems("SUBVENTION"))
                .extract().jsonPath();

        String planId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId");
        String finalTxnAmount = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.finalTransactionAmount");
        BigInteger intplanid= new BigInteger(planId);
        ApiV1ValidateRequest req2 = new ApiV1ValidateRequest.Builder(price,subventionAmount)
                .setMid(emiMerchant.getId())
                .setTokenType("CHECKSUM")
                .setMerchantKey(emiMerchant.getKey())
                .setCustomerId(orderId)
                .setToken(user.ssoToken())
                .setPlanId(intplanid)
                .setPaymentDetails(
                        new PaymentDetails()
                                .setTotalTransactionAmount(price)
                                .setCardNumber("|" + PGPHelpers.getFormattedPaymentDetails("EMI", new PaymentDTO())))
                .setGenerateTokenForIntent(true)
                .build();
        ApiV1Validate api2 = new ApiV1Validate(emiMerchant.getId(), req2);
        Response r2 = api2.execute();
        JsonPath validateResponse =  r2.then()
                .spec(SUCCESS_RESPONSE).extract().jsonPath();

        String emiSubventionToken = validateResponse.getString("body.emiSubventionToken");

        //Initiate Txn
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),emiMerchant)
                .setTxnValue(price)
                .setPayableAmount(new TxnAmount(finalTxnAmount))
                .setOrderId(orderId)
                .setEmiSubventionToken(emiSubventionToken)
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        Assertions.assertThat(initTxnResponse.getBody().getTxnToken()).
                as("Txn token is not generated in initiate txn response").isNotEmpty();

    }

    @Test(description = "Initiate, PTC API : Verify that on hitting v1/ptc , checkout and order stamp API is being called on backend and item list is correct that is sent from theia to subvention team and find success txnstatus and peon",groups = "P0")
    public void t84initiatePTCAPIPhase2() throws Exception {
        Constants.MerchantType emiMerchant = MerchantType.EMI;
        PaymentDTO paymentDTO = new PaymentDTO();
        String orderId = CommonHelpers.generateOrderId();
        User user = userManager.getForRead(Label.BASIC);
        String price ="5";
        String subventionAmount = "5";

        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder(price,subventionAmount)
                .setMid(emiMerchant.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();

        ApiV1Tenure api = new ApiV1Tenure(emiMerchant.getId(), req);
        Response r = api.execute();
        JsonPath tenureResp = r.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.planDetails.emiType", hasItems("SUBVENTION"))
                .extract().jsonPath();

        String planId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId");
        String finalTxnAmount = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.finalTransactionAmount");
        BigInteger intplanid= new BigInteger(planId);
        ApiV1ValidateRequest req2 = new ApiV1ValidateRequest.Builder(price,subventionAmount)
                .setMid(emiMerchant.getId())
                .setMerchantKey(emiMerchant.getKey())
                .setCustomerId(orderId)
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setPlanId(intplanid)
                .setPaymentDetails(
                        new PaymentDetails()
                                .setTotalTransactionAmount(price)
                                .setCardNumber("|" + PGPHelpers.getFormattedPaymentDetails("EMI", paymentDTO)))
                .setGenerateTokenForIntent(true)
                .setOfferDetails(new OfferDetails().setOfferId("40599"))
                .build();
        ApiV1Validate api2 = new ApiV1Validate(emiMerchant.getId(), req2);
        Response r2 = api2.execute();
        JsonPath validateResponse =  r2.then()
                .spec(SUCCESS_RESPONSE).extract().jsonPath();

        String emiSubventionToken = validateResponse.getString("body.emiSubventionToken");
        String pgPlanId = validateResponse.getString("body.pgPlanId");

        //Initiate Txn
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),emiMerchant)
                .setTxnValue(price)
                .setPayableAmount(new TxnAmount(finalTxnAmount))
                .setOrderId(orderId)
                .setEmiSubventionToken(emiSubventionToken)
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        Assertions.assertThat(initTxnResponse.getBody().getTxnToken()).
                as("Txn token is not generated in initiate txn response").isNotEmpty();

        String txnToken = initTxnResponse.getBody().getTxnToken();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId("true")
                .setMid(emiMerchant.getId())
                .setToken(user.ssoToken())
                .setEmiSubventionRequired(true)
                .setEmiSubventedTransactionAmount(subventionAmount)
                .setEmiSubventionCustomerId(orderId)
                .setItems(Arrays.asList(new Item[]{new Item()}))
                .build();

        FetchPaymentOption fpo = new FetchPaymentOption(emiMerchant.getId(), fetchPaymentOptionsDTO);
        fpo.execute().then()
                .spec(SUCCESS_RESPONSE)
                .body("body", (hasKey("emiSubventionBanks")))
                .body("body", hasKey("orderId"));

        OrderDTO orderDTO = new OrderFactory.Native(emiMerchant, initTxnDTO.orderFromBody(), txnToken, paymentDTO, PayMethodType.EMI)
                .setChannelCode("HDFC")
                .setPlanId(pgPlanId)
                .setTXN_AMOUNT(price)
                .setAUTH_MODE("otp")
                .setREQUEST_TYPE("NATIVE")
                .setWEBSITE("retail")
                .setEMI_TYPE("CREDIT_CARD")
                .setCardInfo("|" + PGPHelpers.getFormattedPaymentDetails("EMI", paymentDTO))
                .setINDUSTRY_TYPE_ID("Retail").build();
        checkoutPage.createNativeOrder(orderDTO, false);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(finalTxnAmount)
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

        given().spec(reqSpec)
                .baseUri(LocalConfig.PGP_HOST)
                .basePath("mockbank/emi/v1/checkoutWithOrder")
                .queryParam("customer-id", orderDTO.getORDER_ID())
                .get()
                .then()
                .statusCode(200);
      /*  given().spec(reqSpec)
                .baseUri(LocalConfig.PGP_HOST)
                .basePath("mockbank/emi/v1/orderStamp")
                .queryParam("order-id", orderDTO.getORDER_ID())
                .get()
                .then()
                .statusCode(200);*/
        given().spec(reqSpec)
                .baseUri(LocalConfig.PGP_HOST)
                .basePath("/mockbank/emi/v1/statusUpdate")
                .queryParam("order-id", orderDTO.getORDER_ID())
                .get()
                .then()
                .statusCode(200);

        Peon peon = new Peon(orderDTO.getORDER_ID());
        peon.executeUntilGetResponse();
        orderDTO.setTXN_AMOUNT(finalTxnAmount);
        peon.validatePeon_EMIIndexToken(orderDTO,Constants.Bank.HDFCBANK.toString(),"HDFC","TXN_SUCCESS");

    }

    @Test(description = "Initiate, PTC API : Verify that on hitting v1/ptc , checkout and order stamp API is being called on backend and item list is correct that is sent from theia to subvention team",groups = "P0")
    public void t85initiatePTCAPIPhase2() throws Exception {
        Constants.MerchantType emiMerchant = MerchantType.PGONLY_EMI_MIN_MAX;
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        String orderId = CommonHelpers.generateOrderId();
        User user = userManager.getForRead(Label.BASIC);
        String price ="5";
        String subventionAmount = "5";

        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder(price,subventionAmount)
                .setMid(emiMerchant.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();

        ApiV1Tenure api = new ApiV1Tenure(emiMerchant.getId(), req);
        Response r = api.execute();
        JsonPath tenureResp = r.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.planDetails.emiType", hasItems("SUBVENTION"))
                .extract().jsonPath();

        String planId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId");
        String finalTxnAmount = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.finalTransactionAmount");
        BigInteger intplanid= new BigInteger(planId);
        ApiV1ValidateRequest req2 = new ApiV1ValidateRequest.Builder(price,subventionAmount)
                .setMid(emiMerchant.getId())
                .setMerchantKey(emiMerchant.getKey())
                .setCustomerId(orderId)
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setPlanId(intplanid)
                .setPaymentDetails(
                        new PaymentDetails()
                                .setTotalTransactionAmount(price)
                                .setCardNumber("|4444333322221111|618|092023"))
                .setGenerateTokenForIntent(true)
                .setOfferDetails(new OfferDetails().setOfferId("40599"))
                .build();
        ApiV1Validate api2 = new ApiV1Validate(emiMerchant.getId(), req2);
        Response r2 = api2.execute();
        JsonPath validateResponse =  r2.then()
                .spec(SUCCESS_RESPONSE).extract().jsonPath();

        String emiSubventionToken = validateResponse.getString("body.emiSubventionToken");
        String pgPlanId = validateResponse.getString("body.pgPlanId");

        //Initiate Txn
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),emiMerchant)
                .setTxnValue(price)
                .setPayableAmount(new TxnAmount(finalTxnAmount))
                .setOrderId(orderId)
                .setEmiSubventionToken(emiSubventionToken)
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        Assertions.assertThat(initTxnResponse.getBody().getTxnToken()).
                as("Txn token is not generated in initiate txn response").isNotEmpty();

        String txnToken = initTxnResponse.getBody().getTxnToken();

        OrderDTO orderDTO = new OrderFactory.Native(emiMerchant, initTxnDTO.orderFromBody(), txnToken, paymentDTO, PayMethodType.EMI)
                .setChannelCode("HDFC")
                .setPlanId(pgPlanId)
                .setTXN_AMOUNT(price)
                .setAUTH_MODE("otp")
                .setCardInfo("4718650100010336|618|092023")
                .setREQUEST_TYPE("NATIVE")
                .setWEBSITE("retail")
                .setINDUSTRY_TYPE_ID("Retail").build();
        checkoutPage.createNativeOrder(orderDTO, false);
        ResponsePage responsePage = new  ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_FAILURE")
                .validateRespCode("810")
                .validateRespMsg("Payment failed due to a technical error. Please try after some time.").assertAll();

    }

    @Test(description = "Initiate, PTC API : Failure Case : Verify that on hitting v1/ptc , checkout and order stamp API is being called on backend and item list is correct that is sent from theia to subvention team and find failure txnstatus and peon",groups = "P0")
    public void t86initiatePTCAPIPhase2() throws Exception {
        Constants.MerchantType emiMerchant = MerchantType.EMI;
        PaymentDTO paymentDTO = new PaymentDTO();
        String orderId = CommonHelpers.generateOrderId();
        User user = userManager.getForRead(Label.BASIC);
        String price ="101.98";
        String subventionAmount = "2";

        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder(price,subventionAmount)
                .setMid(emiMerchant.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();

        ApiV1Tenure api = new ApiV1Tenure(emiMerchant.getId(), req);
        Response r = api.execute();
        JsonPath tenureResp = r.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.planDetails.emiType", hasItems("SUBVENTION"))
                .extract().jsonPath();

        String planId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId");
        String finalTxnAmount = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.finalTransactionAmount");
        BigInteger intplanid= new BigInteger(planId);
        ApiV1ValidateRequest req2 = new ApiV1ValidateRequest.Builder(price,subventionAmount)
                .setMid(emiMerchant.getId())
                .setMerchantKey(emiMerchant.getKey())
                .setCustomerId(orderId)
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setPlanId(intplanid)
                .setPaymentDetails(
                        new PaymentDetails()
                                .setTotalTransactionAmount(price)
                                .setCardNumber("|" + PGPHelpers.getFormattedPaymentDetails("EMI", paymentDTO)))

                .setGenerateTokenForIntent(true)
                .setOfferDetails(new OfferDetails().setOfferId("40599"))
                .build();
        ApiV1Validate api2 = new ApiV1Validate(emiMerchant.getId(), req2);
        Response r2 = api2.execute();
        JsonPath validateResponse =  r2.then()
                .spec(SUCCESS_RESPONSE).extract().jsonPath();

        String emiSubventionToken = validateResponse.getString("body.emiSubventionToken");
        String pgPlanId = validateResponse.getString("body.pgPlanId");

        //Initiate Txn
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),emiMerchant)
                .setTxnValue(price)
                .setPayableAmount(new TxnAmount(finalTxnAmount))
                .setOrderId(orderId)
                .setEmiSubventionToken(emiSubventionToken)
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        Assertions.assertThat(initTxnResponse.getBody().getTxnToken()).
                as("Txn token is not generated in initiate txn response").isNotEmpty();

        String txnToken = initTxnResponse.getBody().getTxnToken();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId("true")
                .setMid(emiMerchant.getId())
                .setToken(user.ssoToken())
                .setEmiSubventionRequired(true)
                .setEmiSubventedTransactionAmount(subventionAmount)
                .setEmiSubventionCustomerId(orderId)
                .setItems(Arrays.asList(new Item[]{new Item()}))
                .build();

        FetchPaymentOption fpo = new FetchPaymentOption(emiMerchant.getId(), fetchPaymentOptionsDTO);
        fpo.execute().then()
                .spec(SUCCESS_RESPONSE)
                .body("body", (hasKey("emiSubventionBanks")))
                .body("body", hasKey("orderId"));

        given().spec(reqSpec)
                .contentType(ContentType.JSON)
                .baseUri(LocalConfig.PGP_HOST).basePath("/mockbank/emi/v1/userSummaryBulkPaymentOptions")
                .queryParams("customer-id", orderId)
                .get()
                .then()
                .statusCode(200);

        OrderDTO orderDTO = new OrderFactory.Native(emiMerchant, initTxnDTO.orderFromBody(), txnToken, paymentDTO, PayMethodType.EMI)
                .setChannelCode("HDFC")
                .setPlanId(pgPlanId)
                .setTXN_AMOUNT(price)
                .setAUTH_MODE("otp")
                .setREQUEST_TYPE("NATIVE")
                .setWEBSITE("retail")
                .setEMI_TYPE("CREDIT_CARD")
                .setCardInfo("|" + PGPHelpers.getFormattedPaymentDetails("EMI", paymentDTO))
                .setINDUSTRY_TYPE_ID("Retail").build();
        checkoutPage.createNativeOrder(orderDTO, false);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(finalTxnAmount)
                .validateStatus("TXN_FAILURE")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("227")
                .validateRespMsg("Your payment has been declined by your bank. Please contact your bank for any queries. If money has been deducted from your account, your bank will inform us within 48 hrs and we will refund the same")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();

        given().spec(reqSpec)
                .baseUri(LocalConfig.MOCK_HOST)
                .basePath("/mockbank/emi/v1/checkoutWithOrder")
                .queryParam("customer-id", orderDTO.getORDER_ID())
                .get()
                .then()
                .statusCode(200);
      /*  given().spec(reqSpec)
                .baseUri(LocalConfig.PGP_HOST)
                .basePath("mockbank/emi/v1/orderStamp")
                .queryParam("order-id", orderDTO.getORDER_ID())
                .get()
                .then()
                .statusCode(200);*/
        given().spec(reqSpec)
                .baseUri(LocalConfig.PGP_HOST)
                .basePath("/mockbank/emi/v1/statusUpdate")
                .queryParam("order-id", orderDTO.getORDER_ID())
                .get()
                .then()
                .statusCode(200);

        Peon peon = new Peon(orderDTO.getORDER_ID());
        peon.executeUntilGetResponse();
        orderDTO.setTXN_AMOUNT(finalTxnAmount);
        peon.validatePeon_EMIIndexToken(orderDTO,Constants.Bank.HDFCBANK.toString(),"HDFC","TXN_FAILURE");

    }


    @Test(description = "Initiate, PTC API : Pending Case : Verify that on hitting v1/ptc , checkout and order stamp API is being called on backend and item list is correct that is sent from theia to subvention team and find failure txnstatus and peon",groups = "P0")
    public void t87initiatePTCAPIPhase2() throws Exception {
        Constants.MerchantType emiMerchant = MerchantType.PG2_AMEX_EMI;
        PaymentDTO paymentDTO = new PaymentDTO();
        String orderId = CommonHelpers.generateOrderId();
        User user = userManager.getForRead(Label.BASIC);
        String price ="101.84";
        String subventionAmount = "2";

        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder(price,subventionAmount)
                .setMid(emiMerchant.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();

        ApiV1Tenure api = new ApiV1Tenure(emiMerchant.getId(), req);
        Response r = api.execute();
        JsonPath tenureResp = r.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.planDetails.emiType", hasItems("SUBVENTION"))
                .extract().jsonPath();

        String planId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId");
        String finalTxnAmount = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.finalTransactionAmount");
        BigInteger intplanid= new BigInteger(planId);
        ApiV1ValidateRequest req2 = new ApiV1ValidateRequest.Builder(price,subventionAmount)
                .setMid(emiMerchant.getId())
                .setMerchantKey(emiMerchant.getKey())
                .setCustomerId(orderId)
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setPlanId(intplanid)
                .setPaymentDetails(
                        new PaymentDetails()
                                .setTotalTransactionAmount(price)
                                .setCardNumber("|" + PGPHelpers.getFormattedPaymentDetails("EMI", paymentDTO)))
                .setGenerateTokenForIntent(true)
                .setOfferDetails(new OfferDetails().setOfferId("40599"))
                .build();
        ApiV1Validate api2 = new ApiV1Validate(emiMerchant.getId(), req2);
        Response r2 = api2.execute();
        JsonPath validateResponse =  r2.then()
                .spec(SUCCESS_RESPONSE).extract().jsonPath();

        String emiSubventionToken = validateResponse.getString("body.emiSubventionToken");
        String pgPlanId = validateResponse.getString("body.pgPlanId");

        //Initiate Txn
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),emiMerchant)
                .setTxnValue(price)
                .setPayableAmount(new TxnAmount(finalTxnAmount))
                .setOrderId(orderId)
                .setEmiSubventionToken(emiSubventionToken)
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        Assertions.assertThat(initTxnResponse.getBody().getTxnToken()).
                as("Txn token is not generated in initiate txn response").isNotEmpty();

        String txnToken = initTxnResponse.getBody().getTxnToken();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId("true")
                .setMid(emiMerchant.getId())
                .setToken(user.ssoToken())
                .setEmiSubventionRequired(true)
                .setEmiSubventedTransactionAmount(subventionAmount)
                .setEmiSubventionCustomerId(orderId)
                .setItems(Arrays.asList(new Item[]{new Item()}))
                .build();

        FetchPaymentOption fpo = new FetchPaymentOption(emiMerchant.getId(), fetchPaymentOptionsDTO);
        fpo.execute().then()
                .spec(SUCCESS_RESPONSE)
                .body("body", (hasKey("emiSubventionBanks")))
                .body("body", hasKey("orderId"));

        given().spec(reqSpec)
                .contentType(ContentType.JSON)
                .baseUri(LocalConfig.MOCK_HOST).basePath("/mockbank/emi/v1/userSummaryBulkPaymentOptions")
                .queryParams("customer-id", orderId)
                .get()
                .then()
                .statusCode(200);

        OrderDTO orderDTO = new OrderFactory.Native(emiMerchant, initTxnDTO.orderFromBody(), txnToken, paymentDTO, PayMethodType.EMI)
                .setChannelCode("HDFC")
                .setPlanId(pgPlanId)
                .setTXN_AMOUNT(price)
                .setAUTH_MODE("otp")
                .setREQUEST_TYPE("NATIVE")
                .setWEBSITE("retail")
                .setCardInfo("|" + PGPHelpers.getFormattedPaymentDetails("EMI", paymentDTO))
                .setEMI_TYPE("CREDIT_CARD")
                .setINDUSTRY_TYPE_ID("Retail").build();
        checkoutPage.createNativeOrder(orderDTO, false);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("PENDING")
                .validateRespCode("402")
                .validateRespMsg("We are processing your transaction.").assertAll();

    }


    @Test(description = "Initiate, PTC API : Cache Card token : Verify that on hitting v1/ptc , checkout and order stamp API is being called on backend and item list is correct that is sent from theia to subvention team and find failure txnstatus and peon",groups = "P0")
    public void t88initiatePTCAPIPhase2() throws Exception {
        Constants.MerchantType emiMerchant = MerchantType.EMI;
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        paymentDTO.setEmiCard(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        String orderId = CommonHelpers.generateOrderId();
        User user = userManager.getForRead(Label.BASIC);
        String price ="5";
        String subventionAmount = "2";

        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder(price,subventionAmount)
                .setMid(emiMerchant.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();

        ApiV1Tenure api = new ApiV1Tenure(emiMerchant.getId(), req);
        Response r = api.execute();
        JsonPath tenureResp = r.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.planDetails.emiType", hasItems("SUBVENTION"))
                .extract().jsonPath();

        String planId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId");
        String finalTxnAmount = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.finalTransactionAmount");

        String cacheCardToken = new GetCardToken(LocalConfig.PGP_HOST, emiMerchant.getId(), orderId,
                paymentDTO.getCreditCardNumber(),
                paymentDTO.getExpMonth(),
                paymentDTO.getExpYear(),
                paymentDTO.getCvvNumber())
                .execute()
                .then().statusCode(200)
                .extract().jsonPath().getString("TOKEN");
        BigInteger intplanid= new BigInteger(planId);
        ApiV1ValidateRequest req2 = new ApiV1ValidateRequest.Builder(price,subventionAmount)
                .setMid(emiMerchant.getId())
                .setMerchantKey(emiMerchant.getKey())
                .setCustomerId(orderId)
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setPlanId(intplanid)
                .setPaymentDetails(
                        new PaymentDetails()
                                .setTotalTransactionAmount(price)
                                .setCardNumber(""))
                .setCacheCardToken(cacheCardToken)
                .setGenerateTokenForIntent(true)
                .setOfferDetails(new OfferDetails().setOfferId("40599"))
                .build();
        ApiV1Validate api2 = new ApiV1Validate(emiMerchant.getId(), req2);
        Response r2 = api2.execute();
        JsonPath validateResponse =  r2.then()
                .spec(SUCCESS_RESPONSE).extract().jsonPath();

        String emiSubventionToken = validateResponse.getString("body.emiSubventionToken");
        String pgPlanId = validateResponse.getString("body.pgPlanId");

        //Initiate Txn
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),emiMerchant)
                .setTxnValue(price)
                .setPayableAmount(new TxnAmount(finalTxnAmount))
                .setOrderId(orderId)
                .setEmiSubventionToken(emiSubventionToken)
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        Assertions.assertThat(initTxnResponse.getBody().getTxnToken()).
                as("Txn token is not generated in initiate txn response").isNotEmpty();

        String txnToken = initTxnResponse.getBody().getTxnToken();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId("true")
                .setMid(emiMerchant.getId())
                .setToken(user.ssoToken())
                .setEmiSubventionRequired(true)
                .setEmiSubventedTransactionAmount(subventionAmount)
                .setEmiSubventionCustomerId(orderId)
                .setItems(Arrays.asList(new Item[]{new Item()}))
                .build();

        FetchPaymentOption fpo = new FetchPaymentOption(emiMerchant.getId(), fetchPaymentOptionsDTO);
        fpo.execute().then()
                .spec(SUCCESS_RESPONSE)
                .body("body", (hasKey("emiSubventionBanks")))
                .body("body", hasKey("orderId"));

        given().spec(reqSpec)
                .contentType(ContentType.JSON)
                .baseUri(LocalConfig.MOCK_HOST).basePath("/mockbank/emi/v1/userSummaryBulkPaymentOptions")
                .queryParams("customer-id", orderId)
                .get()
                .then()
                .statusCode(200);

        OrderDTO orderDTO = new OrderFactory.Native(emiMerchant, initTxnDTO.orderFromBody(), txnToken, paymentDTO, PayMethodType.EMI)
                .setChannelCode("HDFC")
                .setPlanId(pgPlanId)
                .setTXN_AMOUNT(price)
                .setAUTH_MODE("otp")
                .setREQUEST_TYPE("NATIVE")
                .setWEBSITE("retail")
                .setEMI_TYPE("CREDIT_CARD")
                .setINDUSTRY_TYPE_ID("Retail").build();
        checkoutPage.createNativeOrder(orderDTO, false);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(finalTxnAmount)
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

        given().spec(reqSpec)
                .baseUri(LocalConfig.PGP_HOST)
                .basePath("mockbank/emi/v1/checkoutWithOrder")
                .queryParam("customer-id", orderDTO.getORDER_ID())
                .get()
                .then()
                .statusCode(200);
      /*  given().spec(reqSpec)
                .baseUri(LocalConfig.PGP_HOST)
                .basePath("mockbank/emi/v1/orderStamp")
                .queryParam("order-id", orderDTO.getORDER_ID())
                .get()
                .then()
                .statusCode(200);*/
        given().spec(reqSpec)
                .baseUri(LocalConfig.PGP_HOST)
                .basePath("/mockbank/emi/v1/statusUpdate")
                .queryParam("order-id", orderDTO.getORDER_ID())
                .get()
                .then()
                .statusCode(200);

        Peon peon = new Peon(orderDTO.getORDER_ID());
        peon.executeUntilGetResponse();
        orderDTO.setTXN_AMOUNT(finalTxnAmount);
        peon.validatePeon_EMIIndexToken(orderDTO,Constants.Bank.HDFCBANK.toString(),"HDFC","TXN_SUCCESS");

    }

    @Owner(GAGANDEEP)
    @Feature("PGP-28962")
    @Description("EMI Subvention: card information as mandatory should be removed from Validate EMI API")
    @Test(description = "Verify success response of /theia/api/v1/emiSubvention/validateEmi when card Bin is passed instead of cardnumber")
    public void t89SuccessValidateEMIUsingCardBin() throws Exception {
        MerchantType m = MerchantType.EMI;
        User user = userManager.getForRead(Label.BASIC);
        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder()
                .setMid(m.getId())
                .setMerchantKey(m.getKey())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();

        ApiV1Tenure api = new ApiV1Tenure(m.getId(), req);
        Response r = api.execute();
        JsonPath tenureResp = r.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.planDetails.emiType", hasItems("SUBVENTION"))
                .extract().jsonPath();

        String planId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId");
        BigInteger intplanid= new BigInteger(planId);
        ApiV1ValidateRequest req2 = new ApiV1ValidateRequest.Builder(user.ssoToken(), "SSO", m.getId())
                .setMerchantKey(m.getKey())
                .setPlanId(intplanid)
                .setPaymentDetails(
                        new PaymentDetails()
                                .setTotalTransactionAmount("5")
                                .setCardNumber(null)
                                .setCardBin(new PaymentDTO().getCreditCardNumber().substring(0,6)))
                .build();
        ApiV1Validate api2 = new ApiV1Validate(m.getId(), req2);
        Response r2 = api2.execute();
        r2.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.bankId", not(empty()),
                        "body.bankId", not(empty()),
                        "body.bankName", not(empty()),
                        "body.bankCode", not(empty()),
                        "body.cardType", not(empty()),
                        "body.bankLogoUrl", not(empty()),
                        "body.planId", equalToIgnoringCase(planId),
                        "body.pgPlanId", not(empty()),
                        "body.rate", not(empty()),
                        "body.interval", not(empty()),
                        "body.emi", not(empty()),
                        "body.interest", not(empty()),
                        "body.emiType", not(empty()),
                        "body.emiLabel", not(empty()),
                        "body.gratifications", not(empty()),
                        "body.gratifications.value", everyItem(not(empty())),
                        "body.gratifications.type", everyItem(not(empty())),
                        "body.gratifications.label", everyItem(not(empty())),
                        "body.itemBreakUpList", not(empty())
                )
                .body("body.emiSubventionToken", not(empty()))
                .body("body.finalTransactionAmount", not(empty()))
        ;
    }

    @Owner(GAGANDEEP)
    @Feature("PGP-28962")
    @Description("EMI Subvention: card information as mandatory should be removed from Validate EMI API")
    @Test(description = "Verify success response of /theia/api/v1/emiSubvention/validateEmi when both card Bin and Cardnumber is passed")
    public void t90SuccessValidateEMIUsingBothCardBinAndNumber() throws Exception
    {
        MerchantType m = MerchantType.EMI;
        User user = userManager.getForRead(Label.BASIC);
        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder()
                .setMid(m.getId())
                .setMerchantKey(m.getKey())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();

        ApiV1Tenure api = new ApiV1Tenure(m.getId(), req);
        Response r = api.execute();
        JsonPath tenureResp = r.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.planDetails.emiType", hasItems("SUBVENTION"))
                .extract().jsonPath();

        String planId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId");
        BigInteger intplanid= new BigInteger(planId);
        ApiV1ValidateRequest req2 = new ApiV1ValidateRequest.Builder(user.ssoToken(), "SSO", m.getId())
                .setMerchantKey(m.getKey())
                .setPlanId(intplanid)
                .setPaymentDetails(
                        new PaymentDetails()
                                .setTotalTransactionAmount("5")
                                .setCardBin(new PaymentDTO().getCreditCardNumber().substring(0,6))
                                .setCardNumber("|" + PGPHelpers.getFormattedPaymentDetails("EMI", new PaymentDTO())))
                .build();
        ApiV1Validate api2 = new ApiV1Validate(m.getId(), req2);
        Response r2 = api2.execute();
        r2.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.bankId", not(empty()),
                        "body.bankId", not(empty()),
                        "body.bankName", not(empty()),
                        "body.bankCode", not(empty()),
                        "body.cardType", not(empty()),
                        "body.bankLogoUrl", not(empty()),
                        "body.planId", equalToIgnoringCase(planId),
                        "body.pgPlanId", not(empty()),
                        "body.rate", not(empty()),
                        "body.interval", not(empty()),
                        "body.emi", not(empty()),
                        "body.interest", not(empty()),
                        "body.emiType", not(empty()),
                        "body.emiLabel", not(empty()),
                        "body.gratifications", not(empty()),
                        "body.gratifications.value", everyItem(not(empty())),
                        "body.gratifications.type", everyItem(not(empty())),
                        "body.gratifications.label", everyItem(not(empty())),
                        "body.itemBreakUpList", not(empty())
                )
                .body("body.emiSubventionToken", not(empty()))
                .body("body.finalTransactionAmount", not(empty()))
        ;
    }

    //Changes required in mock to fail PG is not validating the same
//    @Owner(GAGANDEEP)
//    @Feature("PGP-28962")
//    @Description("EMI Subvention: card information as mandatory should be removed from Validate EMI API")
//    @Test(enabled = false ,description = "Verify if Validate API is hit with new field CardBin and Bin not belongs to same bank as planid")
    public void t91SuccessValidateDifferentCardBinAndPlanId() throws Exception {
        MerchantType m = MerchantType.EMI;
        User user = userManager.getForRead(Label.BASIC);
        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder()
                .setMid(m.getId())
                .setMerchantKey(m.getKey())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();

        ApiV1Tenure api = new ApiV1Tenure(m.getId(), req);
        Response r = api.execute();
        JsonPath tenureResp = r.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.planDetails.emiType", hasItems("SUBVENTION"))
                .extract().jsonPath();

        String planId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId");
        BigInteger intplanid= new BigInteger(planId);
        ApiV1ValidateRequest req2 = new ApiV1ValidateRequest.Builder(user.ssoToken(), "SSO", m.getId())
                .setMerchantKey(m.getKey())
                .setPlanId(intplanid)
                .setPaymentDetails(
                        new PaymentDetails()
                                .setTotalTransactionAmount("5")
                                .setCardBin(PaymentDTO.MASTER_ICICI_DEBIT_CARD_NUMBER.substring(0,6)))

                .build();
        ApiV1Validate api2 = new ApiV1Validate(m.getId(), req2);
        Response r2 = api2.execute();
        r2.then()
                .body("body.resultInfo.resultStatus", equalToIgnoringCase("F"))
                .body("body.resultInfo.resultCode", equalToIgnoringCase("1007"))
                .body("body.resultInfo.resultMsg", equalToIgnoringCase("Card Details are missing"));
        ;
    }



    @Owner(GAGANDEEP)
    @Feature("PGP-28962")
    @Description("EMI Subvention: card information as mandatory should be removed from Validate EMI API")
    @Test(description = "Verify if Validate API is hit with empty CardBin")
    public void t92SuccessValidateEMIUsingBothCardBinAndNumber() throws Exception {
        MerchantType m = MerchantType.EMI;
        User user = userManager.getForRead(Label.BASIC);
        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder()
                .setMid(m.getId())
                .setMerchantKey(m.getKey())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();

        ApiV1Tenure api = new ApiV1Tenure(m.getId(), req);
        Response r = api.execute();
        JsonPath tenureResp = r.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.planDetails.emiType", hasItems("SUBVENTION"))
                .extract().jsonPath();

        String planId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId");
        BigInteger intplanid= new BigInteger(planId);
        ApiV1ValidateRequest req2 = new ApiV1ValidateRequest.Builder(user.ssoToken(), "SSO", m.getId())
                .setMerchantKey(m.getKey())
                .setPlanId(intplanid)
                .setPaymentDetails(
                        new PaymentDetails()
                                .setTotalTransactionAmount("5")
                                .setCardNumber(null)
                                .setCardBin(""))
                .build();
        ApiV1Validate api2 = new ApiV1Validate(m.getId(), req2);
        Response r2 = api2.execute();
        r2.then()
                .body("body.resultInfo.resultStatus", equalToIgnoringCase("F"))
                .body("body.resultInfo.resultCode", equalToIgnoringCase("1007"))
                .body("body.resultInfo.resultMsg", equalToIgnoringCase("Card Details are missing"))
        ;
    }

    @Owner(GAGANDEEP)
    @Feature("PGP-28962")
    @Description("EMI Subvention: card information as mandatory should be removed from Validate EMI API")
    @Test(description = "Verify if Validate API is hit with CardBin less than 6")
    public void t93SuccessValidateEMIUsingBothCardBinAndNumber() throws Exception {
        MerchantType m = MerchantType.EMI;
        User user = userManager.getForRead(Label.BASIC);
        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder()
                .setMid(m.getId())
                .setMerchantKey(m.getKey())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();

        ApiV1Tenure api = new ApiV1Tenure(m.getId(), req);
        Response r = api.execute();
        JsonPath tenureResp = r.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.planDetails.emiType", hasItems("SUBVENTION"))
                .extract().jsonPath();

        String planId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId");
        BigInteger intplanid= new BigInteger(planId);
        ApiV1ValidateRequest req2 = new ApiV1ValidateRequest.Builder(user.ssoToken(), "SSO", m.getId())
                .setMerchantKey(m.getKey())
                .setPlanId(intplanid)
                .setPaymentDetails(
                        new PaymentDetails()
                                .setTotalTransactionAmount("5")
                                .setCardNumber(null)
                                .setCardBin(new PaymentDTO().getCreditCardNumber().substring(0,4)))
                .build();
        ApiV1Validate api2 = new ApiV1Validate(m.getId(), req2);
        Response r2 = api2.execute();
        r2.then()
                .body("body.resultInfo.resultStatus", equalToIgnoringCase("F"))
                .body("body.resultInfo.resultCode", equalToIgnoringCase("1001"))
                .body("body.resultInfo.resultMsg", equalToIgnoringCase("card details are not valid"))
        ;
    }

    @Owner(GAGANDEEP)
    @Feature("PGP-28962")
    @Description("EMI Subvention: card information as mandatory should be removed from Validate EMI API")
    @Test(description = "Verify success of Initiate tn api when emiSubventionToken is sent in request with cardBin in ValidateEMI api")
    public void t94ValidateSucessTxnWithCardBin() throws Exception {
        MerchantType m = MerchantType.EMISubvention;
        User user = userManager.getForRead(Label.BASIC);
        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder()
                .setMid(m.getId())
                .setMerchantKey(m.getKey())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();

        ApiV1Tenure api = new ApiV1Tenure(m.getId(), req);
        Response r = api.execute();
        JsonPath tenureResp = r.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.planDetails.emiType", hasItems("SUBVENTION"))
                .extract().jsonPath();

        String planId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId");
        BigInteger intplanid= new BigInteger(planId);
        ApiV1ValidateRequest req2 = new ApiV1ValidateRequest.Builder(user.ssoToken(), "SSO", m.getId())
                .setMerchantKey(m.getKey())
                .setPlanId(intplanid)
                .setPaymentDetails(
                        new PaymentDetails()
                                .setTotalTransactionAmount("5")
                                .setCardNumber(null)
                                .setCardBin(new PaymentDTO().getCreditCardNumber().substring(0,6)))
                .setGenerateTokenForIntent(true)
                .build();
        ApiV1Validate api2 = new ApiV1Validate(m.getId(), req2);
        Response r2 = api2.execute();
        JsonPath validateResp = r2.then()
                .spec(SUCCESS_RESPONSE)
                .body("body", hasKey("emiSubventionToken"))
                .extract().jsonPath();

        String emiSubventionToken = validateResp.getString("body.emiSubventionToken");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.EMI)
                .setEmiSubventionToken(emiSubventionToken)
                .setTxnValue("5")
                .setPayableAmount(new TxnAmount("4"))
                .build();

        Response rsp = new InitTxn(initTxnDTO).execute();
        rsp.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.txnToken", not(empty()));
    }

    @Owner(GAGANDEEP)
    @Feature("PGP-28962")
    @Description("EMI Subvention: card information as mandatory should be removed from Validate EMI API")
    @Test(description = "Validate API : Verify the response when api is hit with amount based support with card bin")
    public void t95validateAPIPhaseAmountBasedWithCardBin() throws Exception {
        Constants.MerchantType emiMerchant = MerchantType.EMI;
        User user = userManager.getForRead(Label.BASIC);
        String price ="1000";
        String subventionAmount = "200";

        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder(price,subventionAmount)
                .setMid(emiMerchant.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();

        ApiV1Tenure api = new ApiV1Tenure(emiMerchant.getId(), req);
        Response r = api.execute();
        JsonPath tenureResp = r.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.planDetails.emiType", hasItems("SUBVENTION"))
                .extract().jsonPath();

        String planId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId");
        BigInteger intplanid= new BigInteger(planId);
        ApiV1ValidateRequest req2 = new ApiV1ValidateRequest.Builder(price,subventionAmount)
                .setMid(emiMerchant.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setPlanId(intplanid)
                .setPaymentDetails(
                        new PaymentDetails()
                                .setTotalTransactionAmount(price)
                                .setCardNumber(null)
                                .setCardBin(new PaymentDTO().getCreditCardNumber().substring(0,6)))
                .setGenerateTokenForIntent(true)
                .build();
        ApiV1Validate api2 = new ApiV1Validate(emiMerchant.getId(), req2);
        Response r2 = api2.execute();
        r2.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.bankId", not(empty()),
                        "body.bankId", not(empty()),
                        "body.bankName", not(empty()),
                        "body.bankCode", not(empty()),
                        "body.cardType", not(empty()),
                        "body.bankLogoUrl", not(empty()),
                        "body.planId", equalToIgnoringCase(planId),
                        "body.pgPlanId", not(empty()),
                        "body.rate", not(empty()),
                        "body.interval", not(empty()),
                        "body.emi", not(empty()),
                        "body.interest", not(empty()),
                        "body.emiType", not(empty()),
                        "body.emiLabel", not(empty()),
                        "body.gratifications", not(empty()),
                        "body.gratifications.value", everyItem(not(empty())),
                        "body.gratifications.type", everyItem(not(empty())),
                        "body.gratifications.label", everyItem(not(empty())),
                        "body.itemBreakUpList", not(empty())
                )
                .body("body.emiSubventionToken", not(empty()))
                .body("body.finalTransactionAmount", not(empty()));
    }

    @Owner(GAGANDEEP)
    @Feature("PGP-28962")
    @Description("EMI Subvention: card information as mandatory should be removed from Validate EMI API")
    @Test(description = "Validate API : Verify the response when api is hit with amount based support with cardBin less than 6 digits")
    public void t96validateAPIAmountBaseWithCardBinLessThan6Digits() throws Exception {
        Constants.MerchantType emiMerchant = MerchantType.EMI;
        User user = userManager.getForRead(Label.BASIC);
        String price ="1000";
        String subventionAmount = "200";

        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder(price,subventionAmount)
                .setMid(emiMerchant.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();

        ApiV1Tenure api = new ApiV1Tenure(emiMerchant.getId(), req);
        Response r = api.execute();
        JsonPath tenureResp = r.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.planDetails.emiType", hasItems("SUBVENTION"))
                .extract().jsonPath();

        String planId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId");
        BigInteger intplanid= new BigInteger(planId);
        ApiV1ValidateRequest req2 = new ApiV1ValidateRequest.Builder(price,subventionAmount)
                .setMid(emiMerchant.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setPlanId(intplanid)
                .setPaymentDetails(
                        new PaymentDetails()
                                .setTotalTransactionAmount(price)
                                .setCardBin(new PaymentDTO().getCreditCardNumber().substring(0,4)))
                .setGenerateTokenForIntent(true)
                .build();
        ApiV1Validate api2 = new ApiV1Validate(emiMerchant.getId(), req2);
        Response r2 = api2.execute();
        r2.then()
                .body("body.resultInfo.resultStatus", equalToIgnoringCase("F"))
                .body("body.resultInfo.resultCode", equalToIgnoringCase("1001"))
                .body("body.resultInfo.resultMsg", equalToIgnoringCase("card details are not valid"));
    }

    @Owner(GAGANDEEP)
    @Feature("PGP-28962")
    @Description("EMI Subvention: card information as mandatory should be removed from Validate EMI API")
    @Test(description = "Validate API : Verify the response when api is hit with amount based support with cardBin empty")
    public void t97validateAPIAmountBaseWithCardBinEmpty() throws Exception {
        Constants.MerchantType emiMerchant = MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String price ="1000";
        String subventionAmount = "200";

        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder(price,subventionAmount)
                .setMid(emiMerchant.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();

        ApiV1Tenure api = new ApiV1Tenure(emiMerchant.getId(), req);
        Response r = api.execute();
        JsonPath tenureResp = r.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.planDetails.emiType", hasItems("SUBVENTION"))
                .extract().jsonPath();

        String planId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId");
        BigInteger intplanid= new BigInteger(planId);
        ApiV1ValidateRequest req2 = new ApiV1ValidateRequest.Builder(price,subventionAmount)
                .setMid(emiMerchant.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setPlanId(intplanid)
                .setPaymentDetails(
                        new PaymentDetails()
                                .setTotalTransactionAmount(price)
                                .setCardBin(""))
                .setGenerateTokenForIntent(true)
                .build();
        ApiV1Validate api2 = new ApiV1Validate(emiMerchant.getId(), req2);
        Response r2 = api2.execute();
        r2.then()
                .body("body.resultInfo.resultStatus", equalToIgnoringCase("F"))
                .body("body.resultInfo.resultCode", equalToIgnoringCase("1007"))
                .body("body.resultInfo.resultMsg", equalToIgnoringCase("Card Details are missing"));
    }


    @Owner(GAGANDEEP)
    @Feature("PGP-28962")
    @Description("EMI Subvention: card information as mandatory should be removed from Validate EMI API")
    @Test(description = "Validate API : Verify the response when api is hit with amount based support with cardBin more than 6")
    public void t99validateAPIAmountBaseWithCardBinMoreThan6() throws Exception {
        Constants.MerchantType emiMerchant = MerchantType.EMI;
        User user = userManager.getForRead(Label.BASIC);
        String price ="1000";
        String subventionAmount = "200";

        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder(price,subventionAmount)
                .setMid(emiMerchant.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();

        ApiV1Tenure api = new ApiV1Tenure(emiMerchant.getId(), req);
        Response r = api.execute();
        JsonPath tenureResp = r.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.planDetails.emiType", hasItems("SUBVENTION"))
                .extract().jsonPath();

        String planId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId");
        BigInteger intplanid= new BigInteger(planId);
        ApiV1ValidateRequest req2 = new ApiV1ValidateRequest.Builder(price,subventionAmount)
                .setMid(emiMerchant.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setPlanId(intplanid)
                .setPaymentDetails(
                        new PaymentDetails()
                                .setTotalTransactionAmount(price)
                                .setCardBin(new PaymentDTO().getCreditCardNumber().substring(0,8)))
                .build();
        ApiV1Validate api2 = new ApiV1Validate(emiMerchant.getId(), req2);
        Response r2 = api2.execute();
        r2.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.bankId", not(empty()),
                        "body.bankId", not(empty()),
                        "body.bankName", not(empty()),
                        "body.bankCode", not(empty()),
                        "body.cardType", not(empty()),
                        "body.bankLogoUrl", not(empty()),
                        "body.planId", equalToIgnoringCase(planId),
                        "body.pgPlanId", not(empty()),
                        "body.rate", not(empty()),
                        "body.interval", not(empty()),
                        "body.emi", not(empty()),
                        "body.interest", not(empty()),
                        "body.emiType", not(empty()),
                        "body.emiLabel", not(empty()),
                        "body.gratifications", not(empty()),
                        "body.gratifications.value", everyItem(not(empty())),
                        "body.gratifications.type", everyItem(not(empty())),
                        "body.gratifications.label", everyItem(not(empty())),
                        "body.itemBreakUpList", not(empty())
                )
                .body("body.emiSubventionToken", not(empty()))
                .body("body.finalTransactionAmount", not(empty()));
    }

    @Owner(GAGANDEEP)
    @Feature("PGP-28962")
    @Description("EMI Subvention: card information as mandatory should be removed from Validate EMI API")
    @Test(description = "Validate API : Verify the response when api is hit with amount based support with cardBin more than 6")
    public void t98validateAPIAmountBaseWithCardBinMoreThan6() throws Exception {
        Constants.MerchantType emiMerchant = MerchantType.EMI;
        User user = userManager.getForRead(Label.BASIC);
        String price ="1000";
        String subventionAmount = "200";

        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder(price,subventionAmount)
                .setMid(emiMerchant.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();

        ApiV1Tenure api = new ApiV1Tenure(emiMerchant.getId(), req);
        Response r = api.execute();
        JsonPath tenureResp = r.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.planDetails.emiType", hasItems("SUBVENTION"))
                .extract().jsonPath();

        String planId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId");
        BigInteger intplanid= new BigInteger(planId);
        ApiV1ValidateRequest req2 = new ApiV1ValidateRequest.Builder(price,subventionAmount)
                .setMid(emiMerchant.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setPlanId(intplanid)
                .setPaymentDetails(
                        new PaymentDetails()
                                .setTotalTransactionAmount(price)
                                .setCardBin(new PaymentDTO().getCreditCardNumber().substring(0,8)))
                .build();
        ApiV1Validate api2 = new ApiV1Validate(emiMerchant.getId(), req2);
        Response r2 = api2.execute();
        r2.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.bankId", not(empty()),
                        "body.bankId", not(empty()),
                        "body.bankName", not(empty()),
                        "body.bankCode", not(empty()),
                        "body.cardType", not(empty()),
                        "body.bankLogoUrl", not(empty()),
                        "body.planId", equalToIgnoringCase(planId),
                        "body.pgPlanId", not(empty()),
                        "body.rate", not(empty()),
                        "body.interval", not(empty()),
                        "body.emi", not(empty()),
                        "body.interest", not(empty()),
                        "body.emiType", not(empty()),
                        "body.emiLabel", not(empty()),
                        "body.gratifications", not(empty()),
                        "body.gratifications.value", everyItem(not(empty())),
                        "body.gratifications.type", everyItem(not(empty())),
                        "body.gratifications.label", everyItem(not(empty())),
                        "body.itemBreakUpList", not(empty())
                )
                .body("body.emiSubventionToken", not(empty()))
                .body("body.finalTransactionAmount", not(empty()));
    }


    @Feature("PGP-30167")
    @Epic(Constants.Sprint.SPRINT37_0)
    @Owner(Constants.Owner.ESHANI)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify that Item based emi data is in merchant status response and not in peon with peon FlagOFF and MerchantStatus Flag ON")
    public void ItemEMISubvention_peonOFF_MerchantStatusON(@Optional("true") boolean isNativePlus) throws Exception {
        MerchantType m = MerchantType.XIAOMI1;

     //   FF4JFlags.enableMidBased("merchantStatus.setEmiSubventionInfo", m.getId());
        //FF4JFlags.disable("notiQueueHandler.setEmiSubventionInfo");

        User user = userManager.getForRead(Label.BASIC);
        String unqId = CommonHelpers.generateOrderId();
        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder()
                .setMid(m.getId())
                .setMerchantKey(m.getKey())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();

        String planId = new ApiV1Tenure(m.getId(), req).execute()
                .then().spec(SUCCESS_RESPONSE)
                .body("body.planDetails.emiType", hasItems("SUBVENTION"))
                .extract().jsonPath().getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId");

        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        BigInteger intplanid= new BigInteger(planId);
        ApiV1ValidateRequest req2 = new ApiV1ValidateRequest.Builder(user.ssoToken(), "SSO", m.getId())
                .setMerchantKey(m.getKey())
                .setPlanId(intplanid)
                .setOfferDetails(new OfferDetails().setOfferId("offerId"))
                .setCustomerId(unqId)
                .setPaymentDetails(
                        new PaymentDetails()
                                .setTotalTransactionAmount("5")
                                .setCardNumber("|" + PGPHelpers.getFormattedPaymentDetails("EMI", paymentDTO)))
                .setGenerateTokenForIntent(true)
                .build();
        JsonPath validateResp = new ApiV1Validate(m.getId(), req2).execute()
                .then()
                .spec(SUCCESS_RESPONSE)
                .body("body", hasKey("emiSubventionToken"))
                .extract().jsonPath();
        String emiSubventionToken = validateResp.getString("body.emiSubventionToken");
        String pgPlanId = validateResp.getString("body.pgPlanId");

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), m)
                .setEmiSubventionToken(emiSubventionToken)
                .setTxnValue("5")
                .setPayableAmount(new TxnAmount("4"))
                .build();
        String txnToken = new InitTxn(initTxnDTO).execute().then()
                .spec(SUCCESS_RESPONSE)
                .body("body.txnToken", not(empty()))
                .extract().jsonPath().getString("body.txnToken");


        OrderDTO orderDTO = new OrderFactory.Native(m, initTxnDTO.orderFromBody(), txnToken, paymentDTO, PayMethodType.EMI)
                .setChannelCode(Constants.Bank.HDFC.toString())
                .setPlanId(pgPlanId)
                .setAUTH_MODE("otp")
                .setREQUEST_TYPE("NATIVE")
                .setWEBSITE("retail")
                .setEMI_TYPE("CREDIT_CARD")
                .setINDUSTRY_TYPE_ID("Retail").build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateEmiSubventionInfoPresent()
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount("4.0")
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


        com.paytm.api.Peon peon = new Peon(orderDTO.getORDER_ID());
        peon.executeUntilGetResponse();
        PeonResponse peonResponse;
        peonResponse = peon.getPeonData(orderDTO.getORDER_ID());
        Assertions.assertThat(peonResponse.getEmiSubventionInfo()).as("Emi Subvention Info is present in peon").isNull();

        GetPaymentStatusDTO getPaymentStatusDTO = new GetPaymentStatusDTO.Builder
                (orderDTO.getORDER_ID(), orderDTO.getAggrMid(), m.getKey(), orderDTO.getMID())
                .build();
        GetPaymentStatus getPaymentStatus = new GetPaymentStatus(getPaymentStatusDTO);
        Response response1 = getPaymentStatus.execute();
        JsonPath jsonPath = response1.jsonPath();

        SoftAssert softAssert= new SoftAssert();
        softAssert.assertEquals(jsonPath.getString("body.resultInfo.resultStatus"),"TXN_SUCCESS");
        softAssert.assertNotNull(jsonPath.getString("body.emiSubventionInfo"));
        softAssert.assertAll();

    }


    @Feature("PGP-30167")
    @Epic(Constants.Sprint.SPRINT37_0)
    @Owner(Constants.Owner.ESHANI)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify that Amount based emi data is not present in merchant status response and is in peon with peon FlagON and MerchantStatus Flag OFF")
    public void AmountEMISubvention_peonON_MerchantStatusOFF(@Optional("true") boolean isNativePlus) throws Exception {
        MerchantType m = MerchantType.XIAOMI2;

        //FF4JFlags.disable("merchantStatus.setEmiSubventionInfo");
      //  FF4JFlags.enableMidBased("notiQueueHandler.setEmiSubventionInfo",m.getId());

        String price ="100";
        String subventionAmount = "20";

        User user = userManager.getForRead(Label.LOGIN);
        String unqId = CommonHelpers.generateOrderId();
        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder(price,subventionAmount)
                .setMid(m.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();

        ApiV1Tenure api = new ApiV1Tenure(m.getId(), req);
        Response r = api.execute();
        JsonPath tenureResp = r.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.planDetails.emiType", hasItems("SUBVENTION"))
                .extract().jsonPath();

        String planId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId");
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        BigInteger intplanid= new BigInteger(planId);
        ApiV1ValidateRequest req2 = new ApiV1ValidateRequest.Builder(price,subventionAmount)
                .setMid(m.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setPlanId(intplanid)
                .setOfferDetails(new OfferDetails().setOfferId("offerId"))
                .setGenerateTokenForIntent(true)
                .setPaymentDetails(
                        new PaymentDetails()
                                .setTotalTransactionAmount(price)
                                .setCardBin(paymentDTO.getCreditCardNumber().substring(0,6)))
                .build();
        JsonPath validateResp = new ApiV1Validate(m.getId(), req2).execute()
                .then()
                .spec(SUCCESS_RESPONSE)
                .body("body", hasKey("emiSubventionToken"))
                .extract().jsonPath();

        String emiSubventionToken = validateResp.getString("body.emiSubventionToken");

        String pgPlanId = validateResp.getString("body.pgPlanId");
        String finalTxnAmount = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.finalTransactionAmount");

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), m)
                .setEmiSubventionToken(emiSubventionToken)
                .setTxnValue(price)
                .setPayableAmount(new TxnAmount(finalTxnAmount))
                .build();

        String txnToken = new InitTxn(initTxnDTO).execute().then()
                .spec(SUCCESS_RESPONSE)
                .body("body.txnToken", not(empty()))
                .extract().jsonPath().getString("body.txnToken");

        OrderDTO orderDTO = new OrderFactory.Native(m, initTxnDTO.orderFromBody(), txnToken, new PaymentDTO().setCreditCardNumber(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER), PayMethodType.EMI)
                .setChannelCode(Constants.Bank.HDFC.toString())
                .setPlanId(pgPlanId)
                .setAUTH_MODE("otp")
                .setREQUEST_TYPE("NATIVE")
                .setWEBSITE("retail")
                .setEMI_TYPE("CREDIT_CARD")
                .setINDUSTRY_TYPE_ID("Retail").build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateEmiSubventionInfoNotPresent()
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount("98.0")
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

        com.paytm.api.Peon peon = new Peon(orderDTO.getORDER_ID());
        peon.executeUntilGetResponse();
        PeonResponse peonResponse;
        peonResponse = peon.getPeonData(orderDTO.getORDER_ID());
        Assertions.assertThat(peonResponse.getEmiSubventionInfo()).as("Emi Subvention Info is not present in peon").isNotNull();

        GetPaymentStatusDTO getPaymentStatusDTO = new GetPaymentStatusDTO.Builder
                (orderDTO.getORDER_ID(), orderDTO.getAggrMid(), m.getKey(), orderDTO.getMID())
                .build();
        GetPaymentStatus getPaymentStatus = new GetPaymentStatus(getPaymentStatusDTO);
        Response response1 = getPaymentStatus.execute();
        JsonPath jsonPath = response1.jsonPath();

        SoftAssert softAssert= new SoftAssert();
        softAssert.assertEquals(jsonPath.getString("body.resultInfo.resultStatus"),"TXN_SUCCESS");
        softAssert.assertNull(jsonPath.getString("body.emiSubventionInfo"));
        softAssert.assertAll();

    }


    //new case of simple emi+ apply promo
    @Feature("PGP-30167")
    @Epic(Constants.Sprint.SPRINT37_0)
    @Owner(Constants.Owner.ESHANI)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify that Apply Promo data not in merchant status and not in peon and Amount EMI Subvention data is not present in merchant status response and is in peon")
    public void ApplyPromoAndAmountEmiSubventionBothApplied_PeonON_MerchantStatusOFF_PromoOFF(@Optional("true") boolean isNativePlus) throws Exception {
        MerchantType m = MerchantType.XIAOMI2;

        //FF4JFlags.enable("merchantStatus.setEmiSubventionInfo");
   //     FF4JFlags.enableMidBased("notiQueueHandler.setEmiSubventionInfo", m.getId());
//           FF4JFlags.disableMidBased("theia.promoDataInMerchantStatusService",m.getId());


        String price ="100";
        String subventionAmount = "20";

        User user = userManager.getForRead(Label.BASIC);
        String unqId = CommonHelpers.generateOrderId();
        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder(price,subventionAmount)
                .setMid(m.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();

        ApiV1Tenure api = new ApiV1Tenure(m.getId(), req);
        Response r = api.execute();
        JsonPath tenureResp = r.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.planDetails.emiType", hasItems("SUBVENTION"))
                .extract().jsonPath();

        String planId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId");

        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        BigInteger intplanid= new BigInteger(planId);
        ApiV1ValidateRequest req2 = new ApiV1ValidateRequest.Builder(price,subventionAmount)
                .setMid(m.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setPlanId(intplanid)
                .setOfferDetails(new OfferDetails().setOfferId("OfferId"))
                .setGenerateTokenForIntent(true)
                .setPaymentDetails(
                        new PaymentDetails()
                                .setTotalTransactionAmount(price)
                                .setCardBin(paymentDTO.getCreditCardNumber().substring(0,6)))
                .build();
        JsonPath validateResp = new ApiV1Validate(m.getId(), req2).execute()
                .then()
                .spec(SUCCESS_RESPONSE)
                .body("body", hasKey("emiSubventionToken"))
                .extract().jsonPath();
        String emiSubventionToken = validateResp.getString("body.emiSubventionToken");
        String pgPlanId = validateResp.getString("body.pgPlanId");
        String finalTxnAmount = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.finalTransactionAmount");

        Merchant merchant = new Merchant(m.getId(), true);
        Promo promo = new Promo();
        merchant.getPromos().add(promo);

        Map<String, Object> paymentOption = new HashMap<>();
        paymentOption.put("transactionAmount", price);
        paymentOption.put("payMethod", "CREDIT_CARD");
        paymentOption.put("cardNo", new PaymentDTO().getCreditCardNumber());

        ApiV1ApplyPromo apiV1ApplyPromo = new ApiV1ApplyPromo(merchant.getId());
        apiV1ApplyPromo.setContext("body.promocode", promo.getName())
                .setContext("head.token", user.ssoToken())
                .setContext("body.totalTransactionAmount", price)
                .setContext("body.paymentOptions", Arrays.asList(paymentOption));

        Response response = apiV1ApplyPromo.execute();

        HashMap<String, Object> paymentOffersAppliedResponse = response.jsonPath().get("body.paymentOffer");

        PaymentOffersApplied paymentOffersApplied = new PaymentOffersApplied(paymentOffersAppliedResponse);

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), m, paymentOffersApplied)
                .setEmiSubventionToken(emiSubventionToken)
                .setTxnValue(price)
                .setPayableAmount(new TxnAmount(finalTxnAmount))
                .build();

        String txnToken = new InitTxn(initTxnDTO).execute().then()
                .spec(SUCCESS_RESPONSE)
                .body("body.txnToken", not(empty()))
                .extract().jsonPath().getString("body.txnToken");

        OrderDTO orderDTO = new OrderFactory.Native(m, initTxnDTO.orderFromBody(), txnToken, paymentDTO, PayMethodType.EMI)
                .setChannelCode(Constants.Bank.HDFC.toString())
                .setPlanId(pgPlanId)
                .setAUTH_MODE("otp")
                .setREQUEST_TYPE("NATIVE")
                .setWEBSITE("retail")
                .setINDUSTRY_TYPE_ID("Retail")
                .setEMI_TYPE("CREDIT_CARD")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateEmiSubventionInfoNotPresent()
                .validatePaymentPromoCheckoutDataNotPresent()
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(finalTxnAmount)
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();

        com.paytm.api.Peon peon = new com.paytm.api.Peon(orderDTO.getORDER_ID());
        peon.executeUntilGetResponse();
        PeonResponse peonResponse;
        peonResponse = peon.getPeonData(orderDTO.getORDER_ID());
        Assertions.assertThat(peonResponse.getEmiSubventionInfo()).as("Emi Subvention Info is not present in peon").isNotNull();
        Assertions.assertThat(peonResponse.getPaymentPromoCheckoutData()).as("Promo data is not present in peon").isNotNull();


        GetPaymentStatusDTO getPaymentStatusDTO = new GetPaymentStatusDTO.Builder
                (orderDTO.getORDER_ID(), orderDTO.getAggrMid(), m.getKey(), orderDTO.getMID())
                .build();
        GetPaymentStatus getPaymentStatus = new GetPaymentStatus(getPaymentStatusDTO);
        Response response1 = getPaymentStatus.execute();
        JsonPath jsonPath = response1.jsonPath();

        SoftAssert softAssert= new SoftAssert();
        softAssert.assertEquals(jsonPath.getString("body.resultInfo.resultStatus"),"TXN_SUCCESS");
        softAssert.assertNull(jsonPath.getString("body.emiSubventionInfo"));
        softAssert.assertNull(jsonPath.getString("body.paymentPromoCheckoutData"));
        softAssert.assertAll();

    }

    @Owner(Constants.Owner.ESHANI)
    @Feature("PGP-30167")
    @Epic(Constants.Sprint.SPRINT37_0)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify that Apply Promo data in merchant status and Item EMI Subvention data is present in merchant status response and is in peon")
    public void ApplyPromoAndItemEmiSubventionBothApplied_PeonON_MerchantStatusON_PromoON(@Optional("true") boolean isNativePlus) throws Exception {
        MerchantType m = MerchantType.XIAOMI4;

     //   FF4JFlags.enableMidBased("merchantStatus.setEmiSubventionInfo",m.getId());
     //   FF4JFlags.enableMidBased("notiQueueHandler.setEmiSubventionInfo", m.getId());
     //   FF4JFlags.enableMidBased("theia.promoDataInMerchantStatusService", m.getId());

        User user = userManager.getForRead(Label.BASIC);
        String unqId = CommonHelpers.generateOrderId();
        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder()
                .setMid(m.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();

        ApiV1Tenure api = new ApiV1Tenure(m.getId(), req);
        Response r = api.execute();
        JsonPath tenureResp = r.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.planDetails.emiType", hasItems("SUBVENTION"))
                .extract().jsonPath();

        String planId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId");
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        BigInteger intplanid= new BigInteger(planId);
        ApiV1ValidateRequest req2 = new ApiV1ValidateRequest.Builder()
                .setMid(m.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setPlanId(intplanid)
                .setPaymentDetails(
                        new PaymentDetails()
                                .setTotalTransactionAmount("5")
                                .setCardNumber("|" + PGPHelpers.getFormattedPaymentDetails("EMI", paymentDTO)))
                .setGenerateTokenForIntent(true)
                .build();

        JsonPath validateResp = new ApiV1Validate(m.getId(), req2).execute()
                .then()
                .spec(SUCCESS_RESPONSE)
                .body("body", hasKey("emiSubventionToken"))
                .extract().jsonPath();
        String emiSubventionToken = validateResp.getString("body.emiSubventionToken");
        String pgPlanId = validateResp.getString("body.pgPlanId");
        String finalTxnAmount = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.finalTransactionAmount");

        Merchant merchant = new Merchant(m.getId(), true);
        Promo promo = new Promo();
        merchant.getPromos().add(promo);
        Map<String, Object> paymentOption = new HashMap<>();
        paymentOption.put("transactionAmount", "5");
        paymentOption.put("payMethod", "CREDIT_CARD");
        paymentOption.put("cardNo", new PaymentDTO().getCreditCardNumber());

        ApiV1ApplyPromo apiV1ApplyPromo = new ApiV1ApplyPromo(merchant.getId());
        apiV1ApplyPromo.setContext("body.promocode", promo.getName())
                .setContext("head.token", user.ssoToken())
                .setContext("body.totalTransactionAmount", "5")
                .setContext("body.paymentOptions", Arrays.asList(paymentOption));
        Response response = apiV1ApplyPromo.execute()
                .then()
                .spec(ApiV1ApplyPromo.ResultInfo.SUCCESS)
                .extract().response();

        HashMap<String, Object> paymentOffersAppliedResponse = response.jsonPath().get("body.paymentOffer");

        PaymentOffersApplied paymentOffersApplied = new PaymentOffersApplied(paymentOffersAppliedResponse);

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), m, paymentOffersApplied)
                .setEmiSubventionToken(emiSubventionToken)
                .setTxnValue("5")
                .setPayableAmount(new TxnAmount(finalTxnAmount))
                .build();

        String txnToken = new InitTxn(initTxnDTO).execute().then()
                .spec(SUCCESS_RESPONSE)
                .body("body.txnToken", not(empty()))
                .extract().jsonPath().getString("body.txnToken");

        OrderDTO orderDTO = new OrderFactory.Native(m, initTxnDTO.orderFromBody(), txnToken, paymentDTO, PayMethodType.EMI)
                .setChannelCode(Constants.Bank.HDFC.toString())
                .setPlanId(pgPlanId)
                .setAUTH_MODE("otp")
                .setREQUEST_TYPE("NATIVE")
                .setWEBSITE("retail")
                .setEMI_TYPE("CREDIT_CARD")
                .setCardInfo("|" + PGPHelpers.getFormattedPaymentDetails("EMI", paymentDTO))
                .setINDUSTRY_TYPE_ID("Retail")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateEmiSubventionInfoPresent()
                .validatePaymentPromoCheckoutDataPresent()
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(finalTxnAmount)
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();

        com.paytm.api.Peon peon = new com.paytm.api.Peon(orderDTO.getORDER_ID());
        peon.executeUntilGetResponse();
        PeonResponse peonResponse;
        peonResponse = peon.getPeonData(orderDTO.getORDER_ID());
        Assertions.assertThat(peonResponse.getEmiSubventionInfo()).as("Emi Subvention Info is not present in peon").isNotNull();
        Assertions.assertThat(peonResponse.getPaymentPromoCheckoutData()).as("Promo data is not present in peon").isNotNull();

        GetPaymentStatusDTO getPaymentStatusDTO = new GetPaymentStatusDTO.Builder
                (orderDTO.getORDER_ID(), orderDTO.getAggrMid(), m.getKey(), orderDTO.getMID())
                .build();
        GetPaymentStatus getPaymentStatus = new GetPaymentStatus(getPaymentStatusDTO);
        Response response1 = getPaymentStatus.execute();
        JsonPath jsonPath = response1.jsonPath();

        SoftAssert softAssert= new SoftAssert();
        softAssert.assertEquals(jsonPath.getString("body.resultInfo.resultStatus"),"TXN_SUCCESS");
        softAssert.assertNotNull(jsonPath.getString("body.emiSubventionInfo"));
        softAssert.assertNotNull(jsonPath.getString("body.paymentPromoCheckoutData"));
        softAssert.assertAll();
    }


    @Feature("PGP-30167")
    @Epic(Constants.Sprint.SPRINT37_0)
    @Owner(Constants.Owner.ESHANI)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify that failure txn data for Amount EMI Subvention data is present in merchant status response and peon")
    public void FailureTxn_AmountEmiSubventionBothApplied_PeonON_MerchantStatusON_PromoON(@Optional("true") boolean isNativePlus) throws Exception {

        MerchantType m = MerchantType.EMI;

    //    FF4JFlags.enableMidBased("merchantStatus.setEmiSubventionInfo",m.getId());
    //    FF4JFlags.enableMidBased("notiQueueHandler.setEmiSubventionInfo", m.getId());

        String price ="99.98";
        String subventionAmount = "20";
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        User user = userManager.getForRead(Label.BASIC);
        String unqId = CommonHelpers.generateOrderId();
        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder(price,subventionAmount)
                .setMid(m.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();

        ApiV1Tenure api = new ApiV1Tenure(m.getId(), req);
        Response r = api.execute();
        JsonPath tenureResp = r.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.planDetails.emiType", hasItems("SUBVENTION"))
                .extract().jsonPath();

        String planId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId");
        BigInteger intplanid= new BigInteger(planId);
        ApiV1ValidateRequest req2 = new ApiV1ValidateRequest.Builder(price,subventionAmount)
                .setMid(m.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setPlanId(intplanid)
                .setOfferDetails(new OfferDetails().setOfferId("offerId"))
                .setGenerateTokenForIntent(true)
                .setPaymentDetails(
                        new PaymentDetails()
                                .setTotalTransactionAmount(price)
                                .setCardBin(paymentDTO.getCreditCardNumber().substring(0,6)))
                .build();
        JsonPath validateResp = new ApiV1Validate(m.getId(), req2).execute()
                .then()
                .spec(SUCCESS_RESPONSE)
                .body("body", hasKey("emiSubventionToken"))
                .extract().jsonPath();
        String emiSubventionToken = validateResp.getString("body.emiSubventionToken");
        String pgPlanId = validateResp.getString("body.pgPlanId");
        String finalTxnAmount = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.finalTransactionAmount");

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), m)
                .setEmiSubventionToken(emiSubventionToken)
                .setTxnValue(price)
                .setPayableAmount(new TxnAmount(finalTxnAmount))
                .build();

        String txnToken = new InitTxn(initTxnDTO).execute().then()
                .spec(SUCCESS_RESPONSE)
                .body("body.txnToken", not(empty()))
                .extract().jsonPath().getString("body.txnToken");

        OrderDTO orderDTO = new OrderFactory.Native(m, initTxnDTO.orderFromBody(), txnToken, paymentDTO, PayMethodType.EMI)
                .setChannelCode(Constants.Bank.HDFC.toString())
                .setPlanId(pgPlanId)
                .setAUTH_MODE("otp")
                .setREQUEST_TYPE("NATIVE")
                .setWEBSITE("retail")
                .setEMI_TYPE("CREDIT_CARD")
                .setINDUSTRY_TYPE_ID("Retail")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateEmiSubventionInfoPresent()
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(finalTxnAmount)
                .validateStatus("TXN_FAILURE")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("227")
                .validateRespMsg("Your payment has been declined by your bank. Please contact your bank for any queries. If money has been deducted from your account, your bank will inform us within 48 hrs and we will refund the same")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();

        com.paytm.api.Peon peon = new com.paytm.api.Peon(orderDTO.getORDER_ID());
        peon.executeUntilGetResponse();
        PeonResponse peonResponse;
        peonResponse = peon.getPeonData(orderDTO.getORDER_ID());
        Assertions.assertThat(peonResponse.getEmiSubventionInfo()).as("Emi Subvention Info is not present in peon").isNotNull();
//            Assertions.assertThat(peonResponse.getPaymentPromoCheckoutData()).as("Promo data is not present in peon").isNotNull();

        GetPaymentStatusDTO getPaymentStatusDTO = new GetPaymentStatusDTO.Builder
                (orderDTO.getORDER_ID(), orderDTO.getAggrMid(), m.getKey(), orderDTO.getMID())
                .build();
        GetPaymentStatus getPaymentStatus = new GetPaymentStatus(getPaymentStatusDTO);
        Response response1 = getPaymentStatus.execute();
        JsonPath jsonPath = response1.jsonPath();

        SoftAssert softAssert= new SoftAssert();
        softAssert.assertEquals(jsonPath.getString("body.resultInfo.resultStatus"),"TXN_FAILURE");
        softAssert.assertNotNull(jsonPath.getString("body.emiSubventionInfo"));
        softAssert.assertAll();

    }


}
