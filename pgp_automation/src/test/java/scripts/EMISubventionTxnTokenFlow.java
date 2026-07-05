package scripts;

import com.paytm.ServerConfigProvider;
import com.paytm.api.TxnStatus;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.api.theia.emiSubvention.ApiV1Bank;
import com.paytm.api.theia.emiSubvention.ApiV1Tenure;
import com.paytm.api.theia.emiSubvention.ApiV1Validate;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.LogsValidationHelper;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.apphelpers.SavedCardHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.SimplifiedPaymentOffers;
import com.paytm.dto.NativeDTO.InitTxn.SimplifiedSubvention;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.emiSubvention.ApiV1Banks.request.ApiV1BanksRequest;
import com.paytm.dto.emiSubvention.ApiV1Tenure.request.ApiV1TenureRequest;
import com.paytm.dto.emiSubvention.ApiV1Tenure.request.Filters;
import com.paytm.dto.emiSubvention.ApiV1Validate.request.ApiV1ValidateRequest;
import com.paytm.dto.emiSubvention.ApiV1Validate.request.OfferDetails;
import com.paytm.dto.emiSubvention.ApiV1Validate.request.PaymentDetails;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.dto.processTransactionV1.response.EmiSubventionInfo;
import com.paytm.dto.processTransactionV1.response.FinalTransactionAmount;
import com.paytm.dto.processTransactionV1.response.ItemOfferDetails;
import com.paytm.dto.processTransactionV1.response.ProcessTxnV1Response;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
import com.paytm.utils.merchant.merchant.util.Merchant;
import com.paytm.utils.merchant.merchant.util.Promo;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.ResponseSpecification;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.hamcrest.Matchers;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.paytm.apphelpers.LogsValidationHelper.getLogsOnServer;
import static org.hamcrest.Matchers.*;

@Owner(Constants.Owner.ROHIT)
@Feature("PGP-28857")
public class EMISubventionTxnTokenFlow extends PGPBaseTest {
    private final CheckoutPage checkoutPage = new CheckoutPage();
    private static final String BAJAJ_FINSERV_BANK_EMI = Constants.BAJAJ_FINSERV_BANK_EMI;

    private final static ResponseSpecification SUCCESS_RESPONSE = new ResponseSpecBuilder()
            .expectStatusCode(200)
            .expectBody("body.resultInfo.resultStatus", equalToIgnoringCase("s"))
            .expectBody("body.resultInfo.resultCode", equalToIgnoringCase("0000"))
            .expectBody("body.resultInfo.resultMsg", equalToIgnoringCase("Success"))
            .build();
    // AMOUNT- BASED SUBVENTION

    @Test(description = "Verify that subvention details object is sent in FPO response" +
            "Verify following params are returned in subventionDetails object:\n" +
            "1. customerId\n" +
            "2. strategy\n" +
            "3. subventionAmount\n" +
            "4. items\"")
    public void emisub01() throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.EMI;
        User user = userManager.getForRead(Label.BASIC);
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention("1234",null,"470.555",null);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setSimplifiedSubvention(simplifiedSubvention)
                .setTxnValue("1100.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);

        fetchPaymentOption.execute()
                .then()
                .statusCode(200)
                .body("body.resultInfo.resultCode", Matchers.equalTo("0000"))
                .body("body", Matchers.notNullValue())
                .body("body.subventionDetails", Matchers.notNullValue())
                .body("body.subventionDetails", Matchers.hasKey("items"),
                        "body.subventionDetails", Matchers.hasKey("customerId"),
                        "body.subventionDetails", Matchers.hasKey("strategy"),
                        "body.subventionDetails", Matchers.hasKey("subventionAmount"));

    }

    @Test(description = "Verify that user is able to hit bank API with txn_token")
    public void emisub02() throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.EMI;
        User user = userManager.getForRead(Label.BASIC);
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention("1234",null,"470.555",null);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setSimplifiedSubvention(simplifiedSubvention)
                .setTxnValue("1100.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ApiV1BanksRequest req = new ApiV1BanksRequest.Builder(txnToken, "TXN_TOKEN", merchantType.getId())
                .setMid(merchantType.getId())
                .setTokenType("TXN_TOKEN")
                .setToken(txnToken)
                .setPrice("1100")
                .build();
        ApiV1Bank api = new ApiV1Bank(merchantType.getId(), initTxnDTO.orderFromBody(), req);
        api.setContext("body.subventionAmount","470.555");
        api.deleteContext("body.items");
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


    @Test(description = "Verify that user is able to hit tenure API with txn_token")
    public void emisub03() throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.EMI;
        User user = userManager.getForRead(Label.BASIC);
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention("1234",null,"470.555",null);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setSimplifiedSubvention(simplifiedSubvention)
                .setTxnValue("1100.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder(txnToken, "TXN_TOKEN", merchantType.getId())
                .setMid(merchantType.getId())
                .setTokenType("TXN_TOKEN")
                .setToken(txnToken)
                .setPrice("1100")
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();

        ApiV1Tenure api = new ApiV1Tenure(merchantType.getId(), initTxnDTO.orderFromBody(), req);
        api.setContext("body.subventionAmount","470.555");
        api.deleteContext("body.items");
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

    @Test(description = "Verify that user is able to hit validate API with txn_token")
    public void emisub04() throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.EMI;
        User user = userManager.getForRead(Label.BASIC);
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention("1234",null,"470.555",null);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setSimplifiedSubvention(simplifiedSubvention)
                .setTxnValue("1100.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder(txnToken, "TXN_TOKEN", merchantType.getId())
                .setMid(merchantType.getId())
                .setTokenType("TXN_TOKEN")
                .setToken(txnToken)
                .setPrice("1100")
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();
        ApiV1Tenure api = new ApiV1Tenure(merchantType.getId(), initTxnDTO.orderFromBody(), req);
        api.setContext("body.subventionAmount","470.55");
        api.deleteContext("body.items");
        Response r = api.execute();
        JsonPath tenureResp = r.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.planDetails.emiType", hasItems("SUBVENTION"))
                .extract().jsonPath();

        String planId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId");
        String offerId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.itemBreakUp.offerId");
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId")).isNotNull();
        softAssertions.assertThat(tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.itemBreakUp.offerId")).isNotNull();
        softAssertions.assertAll();
        BigInteger intplanid= new BigInteger(planId);
        ApiV1ValidateRequest req2 = new ApiV1ValidateRequest.Builder(txnToken, "TXN_TOKEN", merchantType.getId())
                .setMerchantKey(merchantType.getKey())
                .setCustomerId("1234")
                .setPlanId(intplanid)
                .setPrice("1100")
                .setPaymentDetails(
                        new PaymentDetails()
                                .setTotalTransactionAmount("1100")
                                .setCardNumber("|" + PGPHelpers.getFormattedPaymentDetails("EMI", new PaymentDTO())))
                .setGenerateTokenForIntent(false)
                .setOfferDetails(new OfferDetails().setOfferId(offerId))
                .build();
        ApiV1Validate api2 = new ApiV1Validate(merchantType.getId(), initTxnDTO.orderFromBody(), req2);
        api2.setContext("body.subventionAmount","470.55");
        api2.deleteContext("body.items");
        Response r2 = api2.execute();
        r2.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.bankId", not(empty()),
                        "body.bankId", not(empty()),
                        "body.bankName", not(empty()),
                        "body.bankCode", not(empty()),
                        "body.cardType", not(empty()),
                        "body.bankLogoUrl", not(empty()),
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

    @Test(description = "Verify emiSubventionInfo is mandatory in PTC in case selectPlanOnCashierPage=true is sent in initiate txn request")
    public void emisub05() throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.EMI;
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention("1234",null,"470.555",null);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setSimplifiedSubvention(simplifiedSubvention)
                .setTxnValue("1100.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder(txnToken, "TXN_TOKEN", merchantType.getId())
                .setMid(merchantType.getId())
                .setTokenType("TXN_TOKEN")
                .setToken(txnToken)
                .setPrice("1100.0")
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();
        ApiV1Tenure api = new ApiV1Tenure(merchantType.getId(), initTxnDTO.orderFromBody(), req);
        api.setContext("body.subventionAmount","470.555");
        api.deleteContext("body.items");
        Response r = api.execute();
        JsonPath tenureResp = r.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.planDetails.emiType", hasItems("SUBVENTION"))
                .extract().jsonPath();


        String pgplanId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.pgPlanId");
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.pgPlanId")).isNotNull();
        softAssertions.assertAll();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchantType, "TXN_TOKEN", txnToken)
                .setChannelId("WAP")
                .setPaymentFlow("NONE")
                .setRequestType("NATIVE")
                .setPlanId(pgplanId)
                .setEmiType("CREDIT_CARD")
                .setStoreInstrument("0")
                .setChannelCode("HDFC")
                .setCustId(null)
                .setPayerAccount(null)
                .setWebsite(null)
                .setExtendInfo(null)
                .setRiskExtendInfo(null)
                .setTokenType("TXN_TOKEN")
                .setOrderId(initTxnDTO.orderFromBody())
                .setToken(txnToken)
                .setAuthMode("otp")
                .setPaymentMode("EMI")
                .setemiSubventionInfo(new EmiSubventionInfo())
                .build();
        ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        softAssertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultStatus()).isEqualTo("F");
        softAssertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultCode()).isEqualTo("0001");
        softAssertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultMsg()).isEqualTo("Request parameters are not valid");
        softAssertions.assertAll();

    }

    @Test(description = "Verify proper error message is displayed if subventionPlanId is not sent in emiSubventionInfo")
    public void emisub06() throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.EMI;
        User user = userManager.getForRead(Label.BASIC);
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention("1234",null,"470.555",null);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setSimplifiedSubvention(simplifiedSubvention)
                .setTxnValue("1100.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder(txnToken, "TXN_TOKEN", merchantType.getId())
                .setMid(merchantType.getId())
                .setTokenType("TXN_TOKEN")
                .setToken(txnToken)
                .setPrice("1100.0")
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();
        ApiV1Tenure api = new ApiV1Tenure(merchantType.getId(), initTxnDTO.orderFromBody(), req);
        api.setContext("body.subventionAmount","470.555");
        api.deleteContext("body.items");
        Response r = api.execute();
        JsonPath tenureResp = r.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.planDetails.emiType", hasItems("SUBVENTION"))
                .extract().jsonPath();

        String pgplanId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.pgPlanId");
        String offerId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.itemBreakUp.offerId[0]");
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.pgPlanId")).isNotNull();
        softAssertions.assertThat(tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.itemBreakUp.offerId")).isNotNull();
        softAssertions.assertAll();

        FinalTransactionAmount finalTransactionAmount = new FinalTransactionAmount();
        finalTransactionAmount.value = "1100.0";
        ItemOfferDetails itemOfferDetails = new ItemOfferDetails();
        itemOfferDetails.id = "SUBVENTION";
        itemOfferDetails.offerId = offerId;
        List<ItemOfferDetails> itemOfferDetails1 = new ArrayList<>();
        itemOfferDetails1.add(itemOfferDetails);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchantType, "TXN_TOKEN", txnToken)
                .setEmiType("CREDIT_CARD")
                .setPaymentFlow("NONE")
                .setStoreInstrument("0")
                .setPlanId(pgplanId)
                .setCustId(null)
                .setPayerAccount(null)
                .setWebsite(null)
                .setExtendInfo(null)
                .setRiskExtendInfo(null)
                .setTokenType("TXN_TOKEN")
                .setOrderId(initTxnDTO.orderFromBody())
                .setToken(txnToken)
                .setAuthMode("opt")
                .setPaymentMode("EMI")
                .setemiSubventionInfo(new EmiSubventionInfo("", finalTransactionAmount, itemOfferDetails1))
                .build();
        ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        softAssertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultStatus()).isEqualTo("F");
        softAssertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultCode()).isEqualTo("0001");
        softAssertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultMsg()).isEqualTo("Request parameters are not valid");
        softAssertions.assertAll();

    }

    @Test(description = "Verify proper error message is displayed if incorrect subventionPlanId is sent in emiSubventionInfo")
    public void emisub07() throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.EMISubvention;
        User user = userManager.getForRead(Label.BASIC);
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention("1234",null,"470.555",null);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setSimplifiedSubvention(simplifiedSubvention)
                .setTxnValue("1100.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder(txnToken, "TXN_TOKEN", merchantType.getId())
                .setMid(merchantType.getId())
                .setTokenType("TXN_TOKEN")
                .setToken(txnToken)
                .setPrice("1100.0")
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();
        ApiV1Tenure api = new ApiV1Tenure(merchantType.getId(), initTxnDTO.orderFromBody(), req);
        api.setContext("body.subventionAmount","470.555");
        api.deleteContext("body.items");
        Response r = api.execute();
        JsonPath tenureResp = r.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.planDetails.emiType", hasItems("SUBVENTION"))
                .extract().jsonPath();

        String pgplanId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.pgPlanId");
        String offerId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.itemBreakUp.offerId[0]");
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.pgPlanId")).isNotNull();
        softAssertions.assertThat(tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.itemBreakUp.offerId")).isNotNull();
        softAssertions.assertAll();

        FinalTransactionAmount finalTransactionAmount = new FinalTransactionAmount();
        finalTransactionAmount.value = "1100.0";
        ItemOfferDetails itemOfferDetails = new ItemOfferDetails();
        itemOfferDetails.id = "SUBVENTION";
        itemOfferDetails.offerId = offerId;
        List<ItemOfferDetails> itemOfferDetails1 = new ArrayList<>();
        itemOfferDetails1.add(itemOfferDetails);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchantType, "TXN_TOKEN", txnToken)
                .setEmiType("CREDIT_CARD")
                .setPaymentFlow("NONE")
                .setStoreInstrument("0")
                .setPlanId(pgplanId)
                .setCustId(null)
                .setPayerAccount(null)
                .setWebsite(null)
                .setExtendInfo(null)
                .setRiskExtendInfo(null)
                .setTokenType("TXN_TOKEN")
                .setOrderId(initTxnDTO.orderFromBody())
                .setToken(txnToken)
                .setAuthMode("opt")
                .setPaymentMode("EMI")
                .setemiSubventionInfo(new EmiSubventionInfo("1161", finalTransactionAmount, itemOfferDetails1))
                .build();
        ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        softAssertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultStatus()).isEqualTo("F");
        softAssertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultCode()).isEqualTo("0001");
        softAssertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultMsg()).isEqualTo("'planId' provided in request does not match with 'pgPlanId' in validate subvention response");
        softAssertions.assertAll();


    }

    @Test(description = "Verify proper error message is displayed if finalTransactionAmount is not sent in emiSubventionInfo")
    public void emisub08() throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.EMISubvention;
        User user = userManager.getForRead(Label.BASIC);
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention("1234",null,"470.555",null);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setSimplifiedSubvention(simplifiedSubvention)
                .setTxnValue("1100.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder(txnToken, "TXN_TOKEN", merchantType.getId())
                .setMid(merchantType.getId())
                .setTokenType("TXN_TOKEN")
                .setToken(txnToken)
                .setPrice("1100.0")
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();
        ApiV1Tenure api = new ApiV1Tenure(merchantType.getId(), initTxnDTO.orderFromBody(), req);
        api.setContext("body.subventionAmount","470.555");
        api.deleteContext("body.items");
        Response r = api.execute();
        JsonPath tenureResp = r.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.planDetails.emiType", hasItems("SUBVENTION"))
                .extract().jsonPath();

        String planId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId");
        String pgplanId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.pgPlanId");
        String offerId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.itemBreakUp.offerId[0]");
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.pgPlanId")).isNotNull();
        softAssertions.assertThat(tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.itemBreakUp.offerId")).isNotNull();
        softAssertions.assertThat(tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId")).isNotNull();
        softAssertions.assertAll();

        FinalTransactionAmount finalTransactionAmount = new FinalTransactionAmount();
        finalTransactionAmount.value = "";
        ItemOfferDetails itemOfferDetails = new ItemOfferDetails();
        itemOfferDetails.id = "SUBVENTION";
        itemOfferDetails.offerId = offerId;
        List<ItemOfferDetails> itemOfferDetails1 = new ArrayList<>();
        itemOfferDetails1.add(itemOfferDetails);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchantType, "TXN_TOKEN", txnToken)
                .setEmiType("CREDIT_CARD")
                .setPaymentFlow("NONE")
                .setStoreInstrument("0")
                .setPlanId(pgplanId)
                .setCustId(null)
                .setPayerAccount(null)
                .setWebsite(null)
                .setExtendInfo(null)
                .setRiskExtendInfo(null)
                .setTokenType("TXN_TOKEN")
                .setOrderId(initTxnDTO.orderFromBody())
                .setToken(txnToken)
                .setAuthMode("opt")
                .setPaymentMode("EMI")
                .setemiSubventionInfo(new EmiSubventionInfo(planId, finalTransactionAmount, itemOfferDetails1))
                .build();
        ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        softAssertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultStatus()).isEqualTo("F");
        softAssertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultCode()).isEqualTo("0001");
        softAssertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultMsg()).isEqualTo("Request parameters are not valid");
        softAssertions.assertAll();

    }

    @Test(description = "Verify proper error message is displayed if incorrect finalTransactionAmount is sent in emiSubventionInfo")
    public void emisub09() throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.EMI;
        User user = userManager.getForRead(Label.BASIC);
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention("1234",null,"470.555",null);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setSimplifiedSubvention(simplifiedSubvention)
                .setTxnValue("1100.0")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder(txnToken, "TXN_TOKEN", merchantType.getId())
                .setMid(merchantType.getId())
                .setTokenType("TXN_TOKEN")
                .setToken(txnToken)
                .setPrice("1100.0")
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();
        ApiV1Tenure api = new ApiV1Tenure(merchantType.getId(), initTxnDTO.orderFromBody(), req);
        api.setContext("body.subventionAmount","470.555");
        api.deleteContext("body.items");
        Response r = api.execute();
        JsonPath tenureResp = r.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.planDetails.emiType", hasItems("SUBVENTION"))
                .extract().jsonPath();

        String planId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId");
        String pgplanId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.pgPlanId");
        String offerId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.itemBreakUp.offerId[0]");
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.pgPlanId")).isNotNull();
        softAssertions.assertThat(tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.itemBreakUp.offerId")).isNotNull();
        softAssertions.assertThat(tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId")).isNotNull();
        softAssertions.assertAll();

        FinalTransactionAmount finalTransactionAmount = new FinalTransactionAmount();
        finalTransactionAmount.value = "99999.0";
        ItemOfferDetails itemOfferDetails = new ItemOfferDetails();
        itemOfferDetails.id = "SUBVENTION";
        itemOfferDetails.offerId = offerId;
        List<ItemOfferDetails> itemOfferDetails1 = new ArrayList<>();
        itemOfferDetails1.add(itemOfferDetails);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchantType, "TXN_TOKEN", txnToken)
                .setEmiType("CREDIT_CARD")
                .setPaymentFlow("NONE")
                .setStoreInstrument("0")
                .setPlanId(pgplanId)
                .setCustId(null)
                .setPayerAccount(null)
                .setWebsite(null)
                .setExtendInfo(null)
                .setRiskExtendInfo(null)
                .setTokenType("TXN_TOKEN")
                .setOrderId(initTxnDTO.orderFromBody())
                .setToken(txnToken)
                .setAuthMode("opt")
                .setPaymentMode("EMI")
                .setemiSubventionInfo(new EmiSubventionInfo(planId, finalTransactionAmount, itemOfferDetails1))
                .build();
        ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        softAssertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultStatus()).isEqualTo("F");
        softAssertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultCode()).isEqualTo("0001");
        softAssertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultMsg()).isEqualTo("Error in validating final transaction amount in Simplified Subvention All in one SDK");
        softAssertions.assertAll();
    }

    @Test(description = "verify error when itemofferdetails is not sent in ptc")
    public void emisub10() throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.EMISubvention;
        User user = userManager.getForRead(Label.BASIC);
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention("1234",null,"470.555",null);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setSimplifiedSubvention(simplifiedSubvention)
                .setTxnValue("1100.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder(txnToken, "TXN_TOKEN", merchantType.getId())
                .setMid(merchantType.getId())
                .setTokenType("TXN_TOKEN")
                .setToken(txnToken)
                .setPrice("1100.0")
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();
        ApiV1Tenure api = new ApiV1Tenure(merchantType.getId(), initTxnDTO.orderFromBody(), req);
        api.setContext("body.subventionAmount","470.555");
        api.deleteContext("body.items");
        Response r = api.execute();
        JsonPath tenureResp = r.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.planDetails.emiType", hasItems("SUBVENTION"))
                .extract().jsonPath();

        String planId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId");
        String pgplanId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.pgPlanId");
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.pgPlanId")).isNotNull();
        softAssertions.assertThat(tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId")).isNotNull();
        softAssertions.assertAll();

        FinalTransactionAmount finalTransactionAmount = new FinalTransactionAmount();
        finalTransactionAmount.value = "1098.0";
        ItemOfferDetails itemOfferDetails = new ItemOfferDetails();
        itemOfferDetails.id = "";
        itemOfferDetails.offerId = "";
        List<ItemOfferDetails> itemOfferDetails1 = new ArrayList<>();
        itemOfferDetails1.add(itemOfferDetails);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchantType, "TXN_TOKEN", txnToken)
                .setEmiType("CREDIT_CARD")
                .setPaymentFlow("NONE")
                .setStoreInstrument("0")
                .setPlanId(pgplanId)
                .setCustId(null)
                .setPayerAccount(null)
                .setWebsite(null)
                .setExtendInfo(null)
                .setRiskExtendInfo(null)
                .setTokenType("TXN_TOKEN")
                .setOrderId(initTxnDTO.orderFromBody())
                .setToken(txnToken)
                .setAuthMode("opt")
                .setPaymentMode("EMI")
                .setemiSubventionInfo(new EmiSubventionInfo(planId, finalTransactionAmount))
                .build();
        ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        softAssertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultStatus()).isEqualTo("F");
        softAssertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultCode()).isEqualTo("0001");
        softAssertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultMsg()).isEqualTo("Request parameters are not valid");
        softAssertions.assertAll();
    }

    @Test(description = "Verify sucess is returned in response if all parameters sent are correct")
    public void emisub11() throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.EMI;
        User user = userManager.getForRead(Label.BASIC);
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention("1234",null,"470.555",null);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setSimplifiedSubvention(simplifiedSubvention)
                .setTxnValue("1100.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder(txnToken, "TXN_TOKEN", merchantType.getId())
                .setMid(merchantType.getId())
                .setTokenType("TXN_TOKEN")
                .setToken(txnToken)
                .setPrice("1100.0")
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();
        ApiV1Tenure api = new ApiV1Tenure(merchantType.getId(), initTxnDTO.orderFromBody(), req);
        api.setContext("body.subventionAmount","470.555");
        api.deleteContext("body.items");
        Response r = api.execute();
        JsonPath tenureResp = r.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.planDetails.emiType", hasItems("SUBVENTION"))
                .extract().jsonPath();

        String planId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId");
        String pgplanId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.pgPlanId");
        String offerId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.itemBreakUp.offerId[0]");
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.pgPlanId")).isNotNull();
        softAssertions.assertThat(tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.itemBreakUp.offerId")).isNotNull();
        softAssertions.assertThat(tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId")).isNotNull();
        softAssertions.assertAll();

        FinalTransactionAmount finalTransactionAmount = new FinalTransactionAmount();
        finalTransactionAmount.value = "1100.00";
        ItemOfferDetails itemOfferDetails = new ItemOfferDetails();
        itemOfferDetails.id = "SUBVENTION";
        itemOfferDetails.offerId = offerId;
        List<ItemOfferDetails> itemOfferDetails1 = new ArrayList<>();
        itemOfferDetails1.add(itemOfferDetails);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchantType, "TXN_TOKEN", txnToken)
                .setChannelId("WAP")
                .setPaymentFlow("NONE")
                .setRequestType("NATIVE")
                .setPlanId(pgplanId)
                .setEmiType("CREDIT_CARD")
                .setStoreInstrument("0")
                .setChannelCode("HDFC")
                .setCustId(null)
                .setPayerAccount(null)
                .setWebsite(null)
                .setExtendInfo(null)
                .setRiskExtendInfo(null)
                .setTokenType("TXN_TOKEN")
                .setOrderId(initTxnDTO.orderFromBody())
                .setToken(txnToken)
                .setAuthMode("otp")
                .setPaymentMode("EMI")
                .setemiSubventionInfo(new EmiSubventionInfo(planId, finalTransactionAmount, itemOfferDetails1))
                .build();
        ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        softAssertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        softAssertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        softAssertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        softAssertions.assertAll();

    }

    @Parameters("theme")
    @Test(description = "Verify subventionDetails object is sent in APP_DATA in case of showPaymentPage API (redirection flow)" +
            "\"Verify subvention details contains follwoing params:\n" +
            "1. customerId\n" +
            "2. strategy\n" +
            "3. subventionAmount\n" +
            "4. items\"")
    public void emisub12(@Optional("enhancedwap") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.EMI;
        User user = userManager.getForRead(Label.BASIC);
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention("1234",null,"470.555",null);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setSimplifiedSubvention(simplifiedSubvention)
                .setTxnValue("1100.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(Constants.MerchantType.EMI, initTxnDTO.getBody().getOrderId(), txnToken).build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        String grepcmd = "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/theia.log | " +
                "grep \"" + orderDTO.getMID() + "\" | grep \"PUSH_APP_DATA\" ";
        String theialogs = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(theialogs).contains("subventionDetails");
        softAssertions.assertThat(theialogs).contains("subventionAmount");
        softAssertions.assertThat(theialogs).contains("strategy=AMOUNT_BASED");
        softAssertions.assertThat(theialogs).contains("customerId=1234");
        softAssertions.assertThat(theialogs).contains("items=null");
        softAssertions.assertAll();
    }

    // ITEM- BASED SUBVENTION

    @Test(description = "Verify that subvention details object is sent in FPO response" +
            "\"Verify following params are returned in subventionDetails object:\n" +
            "1. customerId\n" +
            "2. strategy\n" +
            "3. subventionAmount\n" +
            "4. items\"")
    public void emisub13() throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.EMI;
        User user = userManager.getForRead(Label.BASIC);
        List<SimplifiedSubvention.Item> items = new ArrayList<>();
        SimplifiedSubvention.Item item = new SimplifiedSubvention.Item();
        item.setPrice("5.0");
        items.add(item);
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention("1234",null,items);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setSimplifiedSubvention(simplifiedSubvention)
                .setTxnValue("5.0")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        softAssertions.assertThat(fetchPaymentOptionsJson.getString("body")).contains("subventionDetails");
        softAssertions.assertThat(fetchPaymentOptionsJson.getString("body.subventionDetails")).contains("items");
        softAssertions.assertThat(fetchPaymentOptionsJson.getString("body.subventionDetails")).contains("customerId");
        softAssertions.assertThat(fetchPaymentOptionsJson.getString("body.subventionDetails")).contains("strategy");
        softAssertions.assertThat(fetchPaymentOptionsJson.getString("body.subventionDetails")).contains("subventionAmount");
        softAssertions.assertAll();
    }

    @Test(description = "Verify that user is able to hit bank API with txn_token")
    public void emisub14() throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.EMI;
        User user = userManager.getForRead(Label.BASIC);
        List<SimplifiedSubvention.Item> items = new ArrayList<>();
        SimplifiedSubvention.Item item = new SimplifiedSubvention.Item();
        item.setPrice("5.0");
        items.add(item);
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention("1234",null,items);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setSimplifiedSubvention(simplifiedSubvention)
                .setTxnValue("5.0")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ApiV1BanksRequest req = new ApiV1BanksRequest.Builder(txnToken, "TXN_TOKEN", merchantType.getId())
                .setMid(merchantType.getId())
                .setTokenType("TXN_TOKEN")
                .setToken(txnToken)
                .build();
        ApiV1Bank api = new ApiV1Bank(merchantType.getId(), initTxnDTO.orderFromBody(), req);
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

    @Test(description = "Verify that user is able to hit tenure API with txn_token")
    public void emisub15() throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.EMI;
        User user = userManager.getForRead(Label.BASIC);
        List<SimplifiedSubvention.Item> items = new ArrayList<>();
        SimplifiedSubvention.Item item = new SimplifiedSubvention.Item();
        item.setPrice("5.0");
        items.add(item);
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention("1234",null,items);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setSimplifiedSubvention(simplifiedSubvention)
                .setTxnValue("5.0")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder(txnToken, "TXN_TOKEN", merchantType.getId())
                .setMid(merchantType.getId())
                .setTokenType("TXN_TOKEN")
                .setToken(txnToken)
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();

        ApiV1Tenure api = new ApiV1Tenure(merchantType.getId(), initTxnDTO.orderFromBody(), req);
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

    @Test(description = "Verify that user is able to hit validate API with txn_token")
    public void emisub16() throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.EMI;
        User user = userManager.getForRead(Label.BASIC);
        List<SimplifiedSubvention.Item> items = new ArrayList<>();
        SimplifiedSubvention.Item item = new SimplifiedSubvention.Item();
        item.setPrice("5.0");
        items.add(item);
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention("1234",null,items);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setSimplifiedSubvention(simplifiedSubvention)
                .setTxnValue("5.0")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder(txnToken, "TXN_TOKEN", merchantType.getId())
                .setMid(merchantType.getId())
                .setTokenType("TXN_TOKEN")
                .setToken(txnToken)
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();
        ApiV1Tenure api = new ApiV1Tenure(merchantType.getId(), initTxnDTO.orderFromBody(), req);
        Response r = api.execute();
        JsonPath tenureResp = r.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.planDetails.emiType", hasItems("SUBVENTION"))
                .extract().jsonPath();

        String planId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId");
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId")).isNotNull();
        softAssertions.assertAll();
        BigInteger intplanid= new BigInteger(planId);
        ApiV1ValidateRequest req2 = new ApiV1ValidateRequest.Builder(txnToken, "TXN_TOKEN", merchantType.getId())
                .setMerchantKey(merchantType.getKey())
                .setCustomerId("1234")
                .setPlanId(intplanid)
                .setPaymentDetails(
                        new PaymentDetails()
                                .setTotalTransactionAmount("5.0")
                                .setCardNumber("|" + PGPHelpers.getFormattedPaymentDetails("EMI", new PaymentDTO())))
                .setGenerateTokenForIntent(true)
                .build();
        ApiV1Validate api2 = new ApiV1Validate(merchantType.getId(), initTxnDTO.orderFromBody(), req2);
        Response r2 = api2.execute();
        r2.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.bankId", not(empty()),
                        "body.bankId", not(empty()),
                        "body.bankName", not(empty()),
                        "body.bankCode", not(empty()),
                        "body.cardType", not(empty()),
                        "body.bankLogoUrl", not(empty()),
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

    @Test(description = "Verify emiSubventionInfo is mandatory in PTC in case selectPlanOnCashierPage=true is sent in initiate txn request")
    public void emisub17() throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.EMI;
        User user = userManager.getForRead(Label.BASIC);
        List<SimplifiedSubvention.Item> items = new ArrayList<>();
        SimplifiedSubvention.Item item = new SimplifiedSubvention.Item();
        item.setPrice("5.0");
        items.add(item);
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention("1234",null,items);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setSimplifiedSubvention(simplifiedSubvention)
                .setTxnValue("5.0")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder(txnToken, "TXN_TOKEN", merchantType.getId())
                .setMid(merchantType.getId())
                .setTokenType("TXN_TOKEN")
                .setToken(txnToken)
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();
        ApiV1Tenure api = new ApiV1Tenure(merchantType.getId(), initTxnDTO.orderFromBody(), req);
        Response r = api.execute();
        JsonPath tenureResp = r.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.planDetails.emiType", hasItems("SUBVENTION"))
                .extract().jsonPath();

        String pgplanId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.pgPlanId");
        String Id = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.itemBreakUp.id[0]");
        String offerId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.itemBreakUp.offerId[0]");
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.pgPlanId")).isNotNull();
        softAssertions.assertThat(tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.itemBreakUp.offerId")).isNotNull();
        softAssertions.assertThat(tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.itemBreakUp.id")).isNotNull();
        softAssertions.assertAll();

        FinalTransactionAmount finalTransactionAmount = new FinalTransactionAmount();
        finalTransactionAmount.value = "4.0";
        ItemOfferDetails itemOfferDetails = new ItemOfferDetails();
        itemOfferDetails.id = Id;
        itemOfferDetails.offerId = offerId;
        List<ItemOfferDetails> itemOfferDetails1 = new ArrayList<>();
        itemOfferDetails1.add(itemOfferDetails);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchantType, "TXN_TOKEN", txnToken)
                .setPlanId(pgplanId)
                .setCustId(null)
                .setPayerAccount(null)
                .setWebsite(null)
                .setExtendInfo(null)
                .setRiskExtendInfo(null)
                .setTokenType("TXN_TOKEN")
                .setOrderId(initTxnDTO.orderFromBody())
                .setToken(txnToken)
                .setAuthMode("otp")
                .setPaymentMode("EMI")
                .setemiSubventionInfo(new EmiSubventionInfo())
                .build();
        ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        softAssertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultStatus()).isEqualTo("F");
        softAssertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultCode()).isEqualTo("0001");
        softAssertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultMsg()).isEqualTo("Request parameters are not valid");
        softAssertions.assertAll();
    }

    @Test(description = "Verify proper error message is displayed if subventionPlanId is not sent in emiSubventionInfo")
    public void emisub18() throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.EMI;
        User user = userManager.getForRead(Label.BASIC);
        List<SimplifiedSubvention.Item> items = new ArrayList<>();
        SimplifiedSubvention.Item item = new SimplifiedSubvention.Item();
        item.setPrice("5.0");
        items.add(item);
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention("1234",null,items);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setSimplifiedSubvention(simplifiedSubvention)
                .setTxnValue("5.0")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder(txnToken, "TXN_TOKEN", merchantType.getId())
                .setMid(merchantType.getId())
                .setTokenType("TXN_TOKEN")
                .setToken(txnToken)
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();
        ApiV1Tenure api = new ApiV1Tenure(merchantType.getId(), initTxnDTO.orderFromBody(), req);
        Response r = api.execute();
        JsonPath tenureResp = r.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.planDetails.emiType", hasItems("SUBVENTION"))
                .extract().jsonPath();

        String pgplanId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.pgPlanId");
        String Id = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.itemBreakUp.id[0]");
        String offerId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.itemBreakUp.offerId[0]");
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.pgPlanId")).isNotNull();
        softAssertions.assertThat(tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.itemBreakUp.offerId")).isNotNull();
        softAssertions.assertThat(tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.itemBreakUp.id")).isNotNull();
        softAssertions.assertAll();

        FinalTransactionAmount finalTransactionAmount = new FinalTransactionAmount();
        finalTransactionAmount.value = "4.0";
        ItemOfferDetails itemOfferDetails = new ItemOfferDetails();
        itemOfferDetails.id = Id;
        itemOfferDetails.offerId = offerId;
        List<ItemOfferDetails> itemOfferDetails1 = new ArrayList<>();
        itemOfferDetails1.add(itemOfferDetails);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchantType, "TXN_TOKEN", txnToken)
                .setPlanId(pgplanId)
                .setCustId(null)
                .setPayerAccount(null)
                .setWebsite(null)
                .setExtendInfo(null)
                .setRiskExtendInfo(null)
                .setTokenType("TXN_TOKEN")
                .setOrderId(initTxnDTO.orderFromBody())
                .setToken(txnToken)
                .setAuthMode("otp")
                .setPaymentMode("EMI")
                .setemiSubventionInfo(new EmiSubventionInfo("", finalTransactionAmount, itemOfferDetails1))
                .build();
        ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        softAssertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultStatus()).isEqualTo("F");
        softAssertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultCode()).isEqualTo("0001");
        softAssertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultMsg()).isEqualTo("Request parameters are not valid");
        softAssertions.assertAll();
    }

    @Test(description = "Verify proper error message is displayed if incorrect subventionPlanId is sent in emiSubventionInfo")
    public void emisub19() throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.EMI;
        User user = userManager.getForRead(Label.BASIC);
        List<SimplifiedSubvention.Item> items = new ArrayList<>();
        SimplifiedSubvention.Item item = new SimplifiedSubvention.Item();
        item.setPrice("5.0");
        items.add(item);
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention("1234",null,items);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setSimplifiedSubvention(simplifiedSubvention)
                .setTxnValue("5.0")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder(txnToken, "TXN_TOKEN", merchantType.getId())
                .setMid(merchantType.getId())
                .setTokenType("TXN_TOKEN")
                .setToken(txnToken)
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();
        ApiV1Tenure api = new ApiV1Tenure(merchantType.getId(), initTxnDTO.orderFromBody(), req);
        Response r = api.execute();
        JsonPath tenureResp = r.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.planDetails.emiType", hasItems("SUBVENTION"))
                .extract().jsonPath();

        String pgplanId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.pgPlanId");
        String Id = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.itemBreakUp.id[0]");
        String offerId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.itemBreakUp.offerId[0]");
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.pgPlanId")).isNotNull();
        softAssertions.assertThat(tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.itemBreakUp.offerId")).isNotNull();
        softAssertions.assertThat(tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.itemBreakUp.id")).isNotNull();
        softAssertions.assertAll();

        FinalTransactionAmount finalTransactionAmount = new FinalTransactionAmount();
        finalTransactionAmount.value = "5.0";
        ItemOfferDetails itemOfferDetails = new ItemOfferDetails();
        itemOfferDetails.id = Id;
        itemOfferDetails.offerId = offerId;
        List<ItemOfferDetails> itemOfferDetails1 = new ArrayList<>();
        itemOfferDetails1.add(itemOfferDetails);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchantType, "TXN_TOKEN", txnToken)
                .setPlanId(pgplanId)
                .setCustId(null)
                .setPayerAccount(null)
                .setWebsite(null)
                .setExtendInfo(null)
                .setRiskExtendInfo(null)
                .setTokenType("TXN_TOKEN")
                .setOrderId(initTxnDTO.orderFromBody())
                .setToken(txnToken)
                .setAuthMode("otp")
                .setPaymentMode("EMI")
                .setemiSubventionInfo(new EmiSubventionInfo("1161", finalTransactionAmount, itemOfferDetails1))
                .build();
        ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        softAssertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultStatus()).isEqualTo("F");
        softAssertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultCode()).isEqualTo("0001");
        softAssertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultMsg()).isEqualTo("'planId' provided in request does not match with 'pgPlanId' in validate subvention response");
        softAssertions.assertAll();
    }

    @Test(description = "Verify proper error message is displayed if finalTransactionAmount is not sent in emiSubventionInfo")
    public void emisub20() throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.EMI;
        User user = userManager.getForRead(Label.BASIC);
        List<SimplifiedSubvention.Item> items = new ArrayList<>();
        SimplifiedSubvention.Item item = new SimplifiedSubvention.Item();
        item.setPrice("5.0");
        items.add(item);
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention("1234",null,items);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setSimplifiedSubvention(simplifiedSubvention)
                .setTxnValue("5.0")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder(txnToken, "TXN_TOKEN", merchantType.getId())
                .setMid(merchantType.getId())
                .setTokenType("TXN_TOKEN")
                .setToken(txnToken)
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();
        ApiV1Tenure api = new ApiV1Tenure(merchantType.getId(), initTxnDTO.orderFromBody(), req);
        Response r = api.execute();
        JsonPath tenureResp = r.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.planDetails.emiType", hasItems("SUBVENTION"))
                .extract().jsonPath();

         String planId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId");
        String pgplanId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.pgPlanId");
        String Id = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.itemBreakUp.id[0]");
        String offerId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.itemBreakUp.offerId[0]");
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.pgPlanId")).isNotNull();
        softAssertions.assertThat(tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.itemBreakUp.offerId")).isNotNull();
        softAssertions.assertThat(tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.itemBreakUp.id")).isNotNull();
        softAssertions.assertThat(tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId")).isNotNull();
        softAssertions.assertAll();

        FinalTransactionAmount finalTransactionAmount = new FinalTransactionAmount();
        finalTransactionAmount.value = "";
        ItemOfferDetails itemOfferDetails = new ItemOfferDetails();
        itemOfferDetails.id = Id;
        itemOfferDetails.offerId = offerId;
        List<ItemOfferDetails> itemOfferDetails1 = new ArrayList<>();
        itemOfferDetails1.add(itemOfferDetails);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchantType, "TXN_TOKEN", txnToken)
                .setPlanId(pgplanId)
                .setCustId(null)
                .setPayerAccount(null)
                .setWebsite(null)
                .setExtendInfo(null)
                .setRiskExtendInfo(null)
                .setTokenType("TXN_TOKEN")
                .setOrderId(initTxnDTO.orderFromBody())
                .setToken(txnToken)
                .setAuthMode("otp")
                .setPaymentMode("EMI")
                .setemiSubventionInfo(new EmiSubventionInfo(planId, finalTransactionAmount, itemOfferDetails1))
                .build();
        ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        softAssertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultStatus()).isEqualTo("F");
        softAssertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultCode()).isEqualTo("0001");
        softAssertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultMsg()).isEqualTo("Request parameters are not valid");
        softAssertions.assertAll();
    }

    @Test(description = "Verify proper error message is displayed if incorrect finalTransactionAmount is sent in emiSubventionInfo")
    public void emisub21() throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.EMI;
        User user = userManager.getForRead(Label.BASIC);
        List<SimplifiedSubvention.Item> items = new ArrayList<>();
        SimplifiedSubvention.Item item = new SimplifiedSubvention.Item();
        item.setPrice("5.0");
        items.add(item);
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention("1234",null,items);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setSimplifiedSubvention(simplifiedSubvention)
                .setTxnValue("5.0")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder(txnToken, "TXN_TOKEN", merchantType.getId())
                .setMid(merchantType.getId())
                .setTokenType("TXN_TOKEN")
                .setToken(txnToken)
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();
        ApiV1Tenure api = new ApiV1Tenure(merchantType.getId(), initTxnDTO.orderFromBody(), req);
        Response r = api.execute();
        JsonPath tenureResp = r.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.planDetails.emiType", hasItems("SUBVENTION"))
                .extract().jsonPath();

        String planId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId");
        String pgplanId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.pgPlanId");
        String Id = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.itemBreakUp.id[0]");
        String offerId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.itemBreakUp.offerId[0]");
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.pgPlanId")).isNotNull();
        softAssertions.assertThat(tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.itemBreakUp.offerId")).isNotNull();
        softAssertions.assertThat(tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.itemBreakUp.id")).isNotNull();
        softAssertions.assertThat(tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId")).isNotNull();
        softAssertions.assertAll();

        FinalTransactionAmount finalTransactionAmount = new FinalTransactionAmount();
        finalTransactionAmount.value = "14";
        ItemOfferDetails itemOfferDetails = new ItemOfferDetails();
        itemOfferDetails.id = Id;
        itemOfferDetails.offerId = offerId;
        List<ItemOfferDetails> itemOfferDetails1 = new ArrayList<>();
        itemOfferDetails1.add(itemOfferDetails);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchantType, "TXN_TOKEN", txnToken)
                .setPlanId(pgplanId)
                .setCustId(null)
                .setPayerAccount(null)
                .setWebsite(null)
                .setExtendInfo(null)
                .setRiskExtendInfo(null)
                .setTokenType("TXN_TOKEN")
                .setOrderId(initTxnDTO.orderFromBody())
                .setToken(txnToken)
                .setAuthMode("otp")
                .setPaymentMode("EMI")
                .setemiSubventionInfo(new EmiSubventionInfo(planId, finalTransactionAmount, itemOfferDetails1))
                .build();
        ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        softAssertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultStatus()).isEqualTo("F");
        softAssertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultCode()).isEqualTo("0001");
        softAssertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultMsg()).isEqualTo("Error in validating final transaction amount in Simplified Subvention All in one SDK");
        softAssertions.assertAll();
    }

    @Test(description = "Verify error when itemofferdetails is not sent in ptc")
    public void emisub22() throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.EMI;
        User user = userManager.getForRead(Label.BASIC);
        List<SimplifiedSubvention.Item> items = new ArrayList<>();
        SimplifiedSubvention.Item item = new SimplifiedSubvention.Item();
        item.setPrice("5.0");
        items.add(item);
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention("1234",null,items);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setSimplifiedSubvention(simplifiedSubvention)
                .setTxnValue("5.0")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder(txnToken, "TXN_TOKEN", merchantType.getId())
                .setMid(merchantType.getId())
                .setTokenType("TXN_TOKEN")
                .setToken(txnToken)
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();
        ApiV1Tenure api = new ApiV1Tenure(merchantType.getId(), initTxnDTO.orderFromBody(), req);
        Response r = api.execute();
        JsonPath tenureResp = r.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.planDetails.emiType", hasItems("SUBVENTION"))
                .extract().jsonPath();

        String planId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId");
        String pgplanId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.pgPlanId");
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.pgPlanId")).isNotNull();
        softAssertions.assertThat(tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId")).isNotNull();
        softAssertions.assertAll();

        FinalTransactionAmount finalTransactionAmount = new FinalTransactionAmount();
        finalTransactionAmount.value = "4.0";
        ItemOfferDetails itemOfferDetails = new ItemOfferDetails();
        itemOfferDetails.id = "";
        itemOfferDetails.offerId = "";
        List<ItemOfferDetails> itemOfferDetails1 = new ArrayList<>();
        itemOfferDetails1.add(itemOfferDetails);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchantType, "TXN_TOKEN", txnToken)
                .setPlanId(pgplanId)
                .setCustId(null)
                .setPayerAccount(null)
                .setWebsite(null)
                .setExtendInfo(null)
                .setRiskExtendInfo(null)
                .setTokenType("TXN_TOKEN")
                .setOrderId(initTxnDTO.orderFromBody())
                .setToken(txnToken)
                .setAuthMode("otp")
                .setPaymentMode("EMI")
                .setemiSubventionInfo(new EmiSubventionInfo(planId, finalTransactionAmount))
                .build();
        ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        softAssertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultStatus()).isEqualTo("F");
        softAssertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultCode()).isEqualTo("0001");
        softAssertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultMsg()).isEqualTo("Request parameters are not valid");
        softAssertions.assertAll();
    }

    @Parameters("theme")
    @Test(description = "Verify subventionDetails object is sent in APP_DATA in case of showPaymentPage API (redirection flow)" +
            "\"Verify subvention details contains follwoing params:\n" +
            "1. customerId\n" +
            "2. strategy\n" +
            "3. subventionAmount\n" +
            "4. items\"")
    public void emisub23(@Optional("enhancedwap") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.EMI;
        User user = userManager.getForRead(Label.BASIC);
        List<SimplifiedSubvention.Item> items = new ArrayList<>();
        SimplifiedSubvention.Item item = new SimplifiedSubvention.Item();
        item.setPrice("5.0");
        items.add(item);
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention("1234",null,items);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setSimplifiedSubvention(simplifiedSubvention)
                .setTxnValue("5.0")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(Constants.MerchantType.EMI, initTxnDTO.getBody().getOrderId(), txnToken).build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        String grepcmd = "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/theia.log | " +
                "grep \"" + orderDTO.getMID() + "\" | grep \"PUSH_APP_DATA\" ";
        String theialogs = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(theialogs).contains("subventionDetails");
        softAssertions.assertThat(theialogs).contains("subventionAmount=null");
        softAssertions.assertThat(theialogs).contains("strategy=ITEM_BASED");
        softAssertions.assertThat(theialogs).contains("customerId=1234");
        softAssertions.assertThat(theialogs).contains("items");
        softAssertions.assertAll();

    }

    //UI TXN

    @Parameters("theme")
    @Test(description = "Verify that EMI option is coming on showPaymentPage")
    public void emisub24(@Optional("enhancedwap") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.EMI;
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention("1234",null,"470.555",null);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setSimplifiedSubvention(simplifiedSubvention)
                .setTxnValue("1100.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(Constants.MerchantType.EMI, initTxnDTO.getBody().getOrderId(), txnToken).build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabEMI().assertVisible();

    }

    @Parameters("theme")
    @Test(description = "Verify that user is able to do sucessfull EMI subvention txn for new card amount based, updated amount")
    public void emisub25(@Optional("enhancedwap") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.EMI;
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention("1234",null,"470.555",null);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setSimplifiedSubvention(simplifiedSubvention)
                .setTxnValue("900")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(Constants.MerchantType.EMI, initTxnDTO.getBody().getOrderId(), txnToken).build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createAppInvokeOrder(orderDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        paymentDTO.setMonth(1);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.EMI,paymentDTO);
        Double subventiondiscount = 2.0;// for amount based emi subvention discount is of 2 Rs
        Double finaltxnamount =Double.valueOf( initTxnDTO.txnAmountFromBody()) - subventiondiscount;
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(String.valueOf(finaltxnamount))
                .assertAll();


    }

    @Parameters("theme")
    @Test(description = "Verify that user is able to do sucessfull EMI subvention txn for new card item based, updated amount")
    public void emisub26(@Optional("enhancedwap_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.EMI;
        List<SimplifiedSubvention.Item> items = new ArrayList<>();
        SimplifiedSubvention.Item item = new SimplifiedSubvention.Item();
        item.setPrice("5.0");
        items.add(item);
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention("1234",null,items);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setSimplifiedSubvention(simplifiedSubvention)
                .setTxnValue("5.0")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(Constants.MerchantType.EMI, initTxnDTO.getBody().getOrderId(), txnToken).build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createAppInvokeOrder(orderDTO);
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        paymentDTO.setMonth(1);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.EMI,paymentDTO);
        Double subventiondiscount = 1.0;//for item based subvention discount is 1 Rs.
        Double finaltxnamount =Double.valueOf( initTxnDTO.txnAmountFromBody()) - subventiondiscount;
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(String.valueOf(finaltxnamount))
                .assertAll();


    }

    @Parameters("theme")
    @Test(description = "Verify that user is able to do sucessfull EMI subvention txn for saved card")
    public void emisub27(@Optional("enhancedwap_revamp") String theme) throws Exception {

        Constants.MerchantType merchantType = Constants.MerchantType.EMI;
        User user = userManager.getForRead(Label.BASIC);
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        paymentDTO.setMonth(1);
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user,paymentDTO.getExpMonth(),paymentDTO.getExpYear(),paymentDTO.getCreditCardNumber());
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention("1234",null,"470.555",null);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setSimplifiedSubvention(simplifiedSubvention)
                .setTxnValue("999")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(Constants.MerchantType.EMI, initTxnDTO.getBody().getOrderId(), txnToken).build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.checkBoxPPI().click();
        cashierPage.tabSavedEmi().click();
        cashierPage.payBy(Constants.PayMode.EMI_SAVED_CARD,paymentDTO);
        Double subventiondiscount = 2.0;// for amount based emi subvention discount is of 2 Rs
        Double finaltxnamount =Double.valueOf( initTxnDTO.txnAmountFromBody()) - subventiondiscount;
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(String.valueOf(finaltxnamount))
                .validateRespMsg("Txn Success")
                .assertAll();

    }



    @Parameters("theme")
    @Test(description = "Verify that user is able to do sucessfull EMI subvention txn for discount type EMI subvention + cashback type bank offer amount based")
    public void emisub28(@Optional("enhancedwap") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.EMI;
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        paymentDTO.setMonth(1);
        Promo promo = new Promo();
        for (int i=0; i<2; i++) {
            new Merchant(merchantType.getId(), true).getPromos().add(promo);
        }
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode(promo.getName()).setApplyAvailablePromo("true").setValidatePromo("true");
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention("1234",null,"470.555",null);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType,simplifiedPaymentOffers)
                .setSimplifiedSubvention(simplifiedSubvention)
                .setTxnValue("1100")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(Constants.MerchantType.EMI, initTxnDTO.getBody().getOrderId(), txnToken).build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.EMI,paymentDTO);
//        cashierPage.buttonPGPayNow().click();
        Double subventiondiscount = 2.0;// for amount based emi subvention discount is of 2 Rs
        Double finaltxnamount =Double.valueOf( initTxnDTO.txnAmountFromBody()) - subventiondiscount;
        ResponsePage  responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(String.valueOf(finaltxnamount))
                .validateRespMsg("Txn Success")
                .assertAll();

    }

    @Parameters("theme")
    @Test(description = "Verify that user is able to do sucessfull EMI subvention txn for discount type EMI subvention + cashback type bank offer item based")
    public void emisub29(@Optional("enhancedweb_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.EMI;
        List<SimplifiedSubvention.Item> items = new ArrayList<>();
        SimplifiedSubvention.Item item = new SimplifiedSubvention.Item();
        item.setPrice("5.0");
        items.add(item);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setMonth(1);
        Promo promo = new Promo();
        for (int i=0; i<2; i++) {
            new Merchant(merchantType.getId(), true).getPromos().add(promo);
        }
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode(promo.getName()).setApplyAvailablePromo("true").setValidatePromo("true");
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention("1234",null,items);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType,simplifiedPaymentOffers)
                .setSimplifiedSubvention(simplifiedSubvention)
                .setTxnValue("5.0")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(Constants.MerchantType.EMI, initTxnDTO.getBody().getOrderId(), txnToken).build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.EMI,paymentDTO);
        Double subventiondiscount = 1.0;// for amount based emi subvention discount is of 2 Rs
        Double finaltxnamount =Double.valueOf( initTxnDTO.txnAmountFromBody()) - subventiondiscount;
        ResponsePage  responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(String.valueOf(finaltxnamount))
                .validateRespMsg("Txn Success")
                .assertAll();

    }


    @Parameters("theme")
    @Test(description = "Verify that user is able to do sucessfull EMI subvention txn for discount type EMI subvention + discount type bank offer amount based")
    public void emisub30(@Optional("enhancedwap_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.XIAOMI1;
        User user = userManager.getForRead(Label.BASIC);
        //WalletHelpers.setZeroBalance(user);
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        paymentDTO.setMonth(3);
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user,paymentDTO.getExpMonth(),paymentDTO.getExpYear(),paymentDTO.getCreditCardNumber());
        Promo promo = new Promo();
        new Merchant(merchantType.getId(), true).getPromos().add(promo);
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode("discount").setApplyAvailablePromo("true").setValidatePromo("true");
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention("1234",null,"470.555",null);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType,simplifiedPaymentOffers)
                .setSimplifiedSubvention(simplifiedSubvention)
                .setTxnValue("1100")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(Constants.MerchantType.XIAOMI1, initTxnDTO.getBody().getOrderId(), txnToken).build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.EMI,paymentDTO);
        Double subventiondiscount = 2.0;// for amount based emi subvention discount is of 2 Rs
        Double discountpercentage = 0.05;//discount percentage of discount type bank offer
        Double finaltxnamount =Double.valueOf( initTxnDTO.txnAmountFromBody()) - (Double.valueOf( initTxnDTO.txnAmountFromBody()) * discountpercentage + subventiondiscount);
        ResponsePage  responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(String.valueOf(finaltxnamount))
                .validateRespMsg("Txn Success")
                .assertAll();

    }

    @Parameters("theme")
    @Test(description = "Verify that user is able to do sucessfull EMI subvention txn for discount type EMI subvention + discount type bank offer item based based")
    public void emisub31(@Optional("enhancedwap_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.XIAOMI1;
        User user = userManager.getForRead(Label.BASIC);
        List<SimplifiedSubvention.Item> items = new ArrayList<>();
        SimplifiedSubvention.Item item = new SimplifiedSubvention.Item();
        item.setPrice("5.0");
        items.add(item);
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        paymentDTO.setMonth(3);
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user,paymentDTO.getExpMonth(),paymentDTO.getExpYear(),paymentDTO.getCreditCardNumber());
        Promo promo = new Promo();
        new Merchant(merchantType.getId(), true).getPromos().add(promo);
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode("discount").setApplyAvailablePromo("true").setValidatePromo("true");
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention("1234",null,items);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType,simplifiedPaymentOffers)
                .setSimplifiedSubvention(simplifiedSubvention)
                .setTxnValue("5.0")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(Constants.MerchantType.XIAOMI1, initTxnDTO.getBody().getOrderId(), txnToken).build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.EMI,paymentDTO);
        Double subventiondiscount = 1.0;// for amount based emi subvention discount is of 2 Rs
        Double discountpercentage = 0.05;//discount percentage of discount type bank offer
        Double finaltxnamount =Double.valueOf( initTxnDTO.txnAmountFromBody()) - (Double.valueOf( initTxnDTO.txnAmountFromBody()) * discountpercentage + subventiondiscount);
        ResponsePage  responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(String.valueOf(finaltxnamount))
                .validateRespMsg("Txn Success")
                .assertAll();

    }


    @Parameters("theme")
    @Test(description = "Verify that emiSubventionInfo in app_data item based")
    public void emisub32(@Optional("enhancedwap") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.EMI;
        List<SimplifiedSubvention.Item> items = new ArrayList<>();
        SimplifiedSubvention.Item item = new SimplifiedSubvention.Item();
        item.setPrice("5.0");
        items.add(item);
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention("1234",null,items);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setSimplifiedSubvention(simplifiedSubvention)
                .setTxnValue("5.0")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(Constants.MerchantType.EMI, initTxnDTO.getBody().getOrderId(), txnToken).build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createAppInvokeOrder(orderDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        paymentDTO.setMonth(1);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.EMI,paymentDTO);
//        cashierPage.buttonPGPayNow().click();
        Double subventiondiscount = 1.0;//for item based emi subvention discount is of 1 Rs.
        Double finaltxnamount =Double.valueOf( initTxnDTO.txnAmountFromBody()) - subventiondiscount;
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(String.valueOf(finaltxnamount))
                .assertAll();

        String grepcmd = "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/theia.log | " +
                "grep \"" + orderDTO.getMID() + "\" | grep \"PUSH_APP_DATA\" ";
        String theialogs = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(theialogs).contains("subventionDetails");
        softAssertions.assertThat(theialogs).contains("subventionAmount=null");
        softAssertions.assertThat(theialogs).contains("strategy=ITEM_BASED");
        softAssertions.assertThat(theialogs).contains("customerId=1234");
        softAssertions.assertThat(theialogs).contains("items");
        softAssertions.assertAll();



    }

    @Parameters("theme")
    @Test(description = "Verify that emiSubventionInfo in app_data amount base")
    public void emisub33(@Optional("enhancedwap") String theme) throws Exception {

        Constants.MerchantType merchantType = Constants.MerchantType.EMI;
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention("1234",null,"470.555",null);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setSimplifiedSubvention(simplifiedSubvention)
                .setTxnValue("900")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(Constants.MerchantType.EMI, initTxnDTO.getBody().getOrderId(), txnToken).build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createAppInvokeOrder(orderDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        paymentDTO.setMonth(1);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.EMI,paymentDTO);
//        cashierPage.buttonPGPayNow().click();
        Double subventiondiscount = 2.0;// for amount based emi subvention discount is of 2 Rs
        Double finaltxnamount =Double.valueOf( initTxnDTO.txnAmountFromBody()) - subventiondiscount;
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(String.valueOf(finaltxnamount))
                .assertAll();
        String grepcmd = "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/theia.log | " +
                "grep \"" + orderDTO.getMID() + "\" | grep \"PUSH_APP_DATA\" ";
        String theialogs = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(theialogs).contains("subventionDetails");
        softAssertions.assertThat(theialogs).contains("subventionAmount=470.55");
        softAssertions.assertThat(theialogs).contains("strategy=AMOUNT_BASED");
        softAssertions.assertThat(theialogs).contains("customerId=1234");
        softAssertions.assertThat(theialogs).contains("items=null");
        softAssertions.assertAll();

    }

    @Feature("PGP-36036")
    @Parameters("theme")
    @Test(description = "Verify X-CLIENT should be passed as EMI_PG_PA and the verticalId = PAYTM_EMI, isPhysical = true and isEmiEnabled = true are hardcoded by theia if not sent by the merchant in request of item based emi subvention")
    public void emiSubClientVerifyItem(@Optional("enhancedweb_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.EMISubvention;
        List<SimplifiedSubvention.Item> items = new ArrayList<>();
        SimplifiedSubvention.Item item = new SimplifiedSubvention.Item("1234","27902","table", Arrays.asList("66781"),"1","20.0");
        items.add(item);
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention("1234", null, items);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setSimplifiedSubvention(simplifiedSubvention)
                .setTxnValue("20.0")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(Constants.MerchantType.EMISubvention, initTxnDTO.getBody().getOrderId(), txnToken).build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createAppInvokeOrder(orderDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setMonth(3);
        paymentDTO.setEmiCard(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.EMI, paymentDTO);
        Double subventiondiscount = 1.0;//for item based subvention discount is 1 Rs.
        Double finaltxnamount = Double.valueOf(initTxnDTO.txnAmountFromBody()) - subventiondiscount;
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(String.valueOf(finaltxnamount))
                .assertAll();
        String grepcmdV1Banks = "grep \"" + "emi/v1/banks" + "\" /paytm/logs/theia_facade.log | " + "grep \"" + orderDTO.getORDER_ID() + "\" | grep \'\"" + "TYPE" + "\" : \"" + "REQUEST" + "\"\'";
        String logsV1Banks = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmdV1Banks);
        Assertions.assertThat(logsV1Banks).contains("X-CLIENT=[EMI_PG_PA]");
        Assertions.assertThat(logsV1Banks).contains("\"verticalId\":\"PAYTM_EMI\"");
        Assertions.assertThat(logsV1Banks).contains("\"isPhysical\":true");
        Assertions.assertThat(logsV1Banks).contains("\"isEmiEnabled\":true");

        String grepcmdV1Tenures = "grep \"" + "emi/v1/tenures" + "\" /paytm/logs/theia_facade.log | " + "grep \"" + orderDTO.getORDER_ID() + "\" | grep \'\"" + "TYPE" + "\" : \"" + "REQUEST" + "\"\'";
        String logsV1Tenures = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmdV1Tenures);
        Assertions.assertThat(logsV1Tenures).contains("X-CLIENT=[EMI_PG_PA]");
        Assertions.assertThat(logsV1Tenures).contains("\"verticalId\":\"PAYTM_EMI\"");
        Assertions.assertThat(logsV1Tenures).contains("\"isPhysical\":true");
        Assertions.assertThat(logsV1Tenures).contains("\"isEmiEnabled\":true");

        String grepcmdV1Validate = "grep \"" + "emi/v1/validate" + "\" /paytm/logs/theia_facade.log | " + "grep \"" + orderDTO.getORDER_ID() + "\" | grep \'\"" + "TYPE" + "\" : \"" + "REQUEST" + "\"\'";
        String logsV1Validate = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmdV1Validate);
        Assertions.assertThat(logsV1Validate).contains("X-CLIENT=[EMI_PG_PA]");
        Assertions.assertThat(logsV1Validate).contains("\"verticalId\":\"PAYTM_EMI\"");
        Assertions.assertThat(logsV1Validate).contains("\"isPhysical\":true");
        Assertions.assertThat(logsV1Validate).contains("\"isEmiEnabled\":true");

        String grepcmdV1Checkout = "grep \"" + "emi/v1/checkoutWithOrder" + "\" /paytm/logs/theia_facade.log | " + "grep \"" + orderDTO.getORDER_ID() + "\" | grep \'\"" + "TYPE" + "\" : \"" + "REQUEST" + "\"\'";
        String logsV1Checkout = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmdV1Checkout);
        Assertions.assertThat(logsV1Checkout).contains("X-CLIENT=[EMI_PG_PA]");
        Assertions.assertThat(logsV1Checkout).contains("\"verticalId\":\"PAYTM_EMI\"");
        Assertions.assertThat(logsV1Checkout).contains("\"isPhysical\":true");
        Assertions.assertThat(logsV1Checkout).contains("\"isEmiEnabled\":true");

      /*  String grepcmdV1OrderStamp = "grep \"" + "emi/v1/orderStamp" + "\" /paytm/logs/theia_facade.log | " + "grep \"" + orderDTO.getORDER_ID() + "\" | grep \'\"" + "TYPE" + "\" : \"" + "REQUEST" + "\"\'";
        String logsV1OrderStamp = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmdV1OrderStamp);
        Assertions.assertThat(logsV1OrderStamp).contains("X-CLIENT=[EMI_PG_PA]");
        Assertions.assertThat(logsV1OrderStamp).contains("\"verticalId\":\"PAYTM_EMI\"");
        Assertions.assertThat(logsV1OrderStamp).contains("\"isPhysical\":true");
        Assertions.assertThat(logsV1OrderStamp).contains("\"isEmiEnabled\":true");*/

    }

    @Feature("PGP-36036")
    @Parameters("theme")
    @Test(description = "Verify X-CLIENT should be passed as EMI_PG in requests for amount based emi subvention")
    public void emiSubClientVerifyAmount(@Optional("enhancedweb_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.EMISubvention;
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention("1234", null, "470.555", null);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setSimplifiedSubvention(simplifiedSubvention)
                .setTxnValue("900")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(Constants.MerchantType.EMISubvention, initTxnDTO.getBody().getOrderId(), txnToken).build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createAppInvokeOrder(orderDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setMonth(6);
        paymentDTO.setEmiCard(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.EMI, paymentDTO);
        Double subventiondiscount = 2.0;// for amount based emi subvention discount is of 2 Rs
        Double finaltxnamount = Double.valueOf(initTxnDTO.txnAmountFromBody()) - subventiondiscount;
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(String.valueOf(finaltxnamount))
                .assertAll();

        String grepcmdV1Banks = "grep \"" + "emi/v1/banks" + "\" /paytm/logs/theia_facade.log | " + "grep \"" + orderDTO.getORDER_ID() + "\" | grep \'\"" + "TYPE" + "\" : \"" + "REQUEST" + "\"\'";
        String logsV1Banks = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmdV1Banks);
        Assertions.assertThat(logsV1Banks).contains("X-CLIENT=[EMI_PG]");

        String grepcmdV1Tenures = "grep \"" + "emi/v1/tenures" + "\" /paytm/logs/theia_facade.log | " + "grep \"" + orderDTO.getORDER_ID() + "\" | grep \'\"" + "TYPE" + "\" : \"" + "REQUEST" + "\"\'";
        String logsV1Tenures = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmdV1Tenures);
        Assertions.assertThat(logsV1Tenures).contains("X-CLIENT=[EMI_PG]");

        String grepcmdV1Validate = "grep \"" + "emi/v1/validate" + "\" /paytm/logs/theia_facade.log | " + "grep \"" + orderDTO.getORDER_ID() + "\" | grep \'\"" + "TYPE" + "\" : \"" + "REQUEST" + "\"\'";
        String logsV1Validate = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmdV1Validate);
        Assertions.assertThat(logsV1Validate).contains("X-CLIENT=[EMI_PG]");

        String grepcmdV1Checkout = "grep \"" + "emi/v1/checkoutWithOrder" + "\" /paytm/logs/theia_facade.log | " + "grep \"" + orderDTO.getORDER_ID() + "\" | grep \'\"" + "TYPE" + "\" : \"" + "REQUEST" + "\"\'";
        String logsV1Checkout = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmdV1Checkout);
        Assertions.assertThat(logsV1Checkout).contains("X-CLIENT=[EMI_PG]");

        /*String grepcmdV1OrderStamp = "grep \"" + "emi/v1/orderStamp" + "\" /paytm/logs/theia_facade.log | " + "grep \"" + orderDTO.getORDER_ID() + "\" | grep \'\"" + "TYPE" + "\" : \"" + "REQUEST" + "\"\'";
        String logsV1OrderStamp = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmdV1OrderStamp);
        Assertions.assertThat(logsV1OrderStamp).contains("X-CLIENT=[EMI_PG]");*/
    }

    @Owner(Constants.Owner.RITIK)
    @Feature("PGP-37193")
    @Parameters({"theme"})
    @Test(description = "Verify special handling for brand emi cases for HDFC/SBI Bank when bank contribution is greater than 0 i.e.  " +
            "amountBearer\": {\n" +
            " \"brand\": 2.0, \"merchant\": 0.0, \n" +
            "\"platform\": 0.0 } ")
    public void verifyBrandEmiCases_forHDFCBank_whenBankContributionGreaterThanZero(@Optional("enhancedweb_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.BRAND_BO_DISC_HDFC;
        SimplifiedSubvention simplifiedSubvention= new SimplifiedSubvention("1234",null,"70",null);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO =new InitTxnDTO.Builder(null,merchant)
                .setTxnValue("151.30")
                .setSimplifiedSubvention(simplifiedSubvention)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(merchant, initTxnDTO.orderFromBody(),txnToken).build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO=new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        cashierPage.payBy(Constants.PayMode.EMI,paymentDTO);
        ResponsePage responsePage =new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateCheckSum(merchant.getKey())
                .validateResponsePageParameters()
                .assertAll();


        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
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
                .validateStatusAPIParameters()
                .AssertAll();
    }

    @Owner(Constants.Owner.RITIK)
    @Feature("PGP-37193")
    @Parameters({"theme"})
    @Test(description = "Verify special handling for brand emi cases for HDFC/SBI Bank when merchant contribution is greater than 0 i.e." +
            "amountBearer\": {\n" +
            " \"brand\": 0.0, \"merchant\": 2.0, \n" +
            "\"platform\": 0.0 }")
    public void verifyBrandEmiCases_forHDFCBank_whenMerchantContributionGreaterThanZero(@Optional("enhancedweb_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.BRAND_MERCHANT_BO_DISC_HDFC;
        SimplifiedSubvention simplifiedSubvention= new SimplifiedSubvention("1234",null,"70",null);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO =new InitTxnDTO.Builder(null,merchant)
                .setTxnValue("151.31")
                .setSimplifiedSubvention(simplifiedSubvention)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(merchant, initTxnDTO.orderFromBody(),txnToken).build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO=new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        cashierPage.payBy(Constants.PayMode.EMI,paymentDTO);
        ResponsePage responsePage =new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount("149.31")
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateCheckSum(merchant.getKey())
                .validateResponsePageParameters()
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount("149.31")
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
                .validateStatusAPIParameters()
                .AssertAll();
    }


    @Owner(Constants.Owner.RITIK)
    @Feature("PGP-37193")
    @Parameters({"theme"})
    @Test(description = "Verify handling for brand emi cases for Other Banks when bank contribution is greater than 0 i.e." +
            "amountBearer\": {\n" +
            " \"brand\": 2.0, \"merchant\": 0..0, \n" +
            "\"platform\": 0.0 }")
    public void verifyBrandEmiCases_forOtherBank_whenBankContributionGreaterThanZero(@Optional("enhancedweb_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.BRAND_BO_DISC_BAJAJ;
        SimplifiedSubvention simplifiedSubvention= new SimplifiedSubvention("1234",null,"70",null);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO =new InitTxnDTO.Builder(null,merchant)
                .setTxnValue("151.33")
                .setSimplifiedSubvention(simplifiedSubvention)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(merchant, initTxnDTO.orderFromBody(),txnToken).build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO=new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.BAJAJ_FINSERV_CREDIT_CARD_NUMBER);
        paymentDTO.setEmiCard(PaymentDTO.BAJAJ_FINSERV_CREDIT_CARD_NUMBER);
        paymentDTO.setBankName(BAJAJ_FINSERV_BANK_EMI);
        cashierPage.payBy(Constants.PayMode.EMI,paymentDTO);
        ResponsePage responsePage =new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount("149.33")
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.BAJAJFN.toString())
                .validateBankName(BAJAJ_FINSERV_BANK_EMI.toUpperCase())
                .validateCheckSum(merchant.getKey())
                .validateResponsePageParameters()
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount("149.33")
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("EMI")
                .AssertAll();
    }
}