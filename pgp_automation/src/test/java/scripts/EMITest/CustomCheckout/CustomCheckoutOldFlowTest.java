package scripts.EMITest.CustomCheckout;

import com.paytm.LocalConfig;
import com.paytm.api.TxnStatus;
import com.paytm.api.theia.ApplyPromoV2Api;
import com.paytm.api.theia.emiSubvention.ApiV1Tenure;
import com.paytm.api.theia.emiSubvention.ApiV1Validate;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.*;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.ApplyPromoV2DTO.*;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.PaymentOffersApplied;
import com.paytm.dto.NativeDTO.InitTxn.TxnAmount;
import com.paytm.dto.emiSubvention.ApiV1Tenure.request.ApiV1TenureRequest;
import com.paytm.dto.emiSubvention.ApiV1Tenure.request.Filters;
import com.paytm.dto.emiSubvention.ApiV1Validate.request.ApiV1ValidateRequest;
import com.paytm.dto.emiSubvention.ApiV1Validate.request.PaymentDetails;
import com.paytm.dto.processTransactionV1.CardTokenInfo;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.pages.NativePlusHoldpayPage;
import io.qameta.allure.Owner;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.ResponseSpecification;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

import static com.paytm.appconstants.Constants.Owner.MEHUL_GUPTA;
import static com.paytm.dto.PaymentDTO.*;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.hasItems;

public class CustomCheckoutOldFlowTest  extends PGPBaseTest {

    private static final String RUPAY_CC_CARD_NO = AlternateID_RUPAY_CARD;
    private static final String VISA_CC_CARD_NO = AlternateID_VISA_CARD;
    private static final String MASTER_ICICI_DC_NUMBER = MASTER_ICICI_DEBIT_CARD_NUMBER;
    private static final String DINERS_CC_CARD_NO = DINERS_CC_CARD_NUMBER;
    private static final String RUPAY_COFT_TOKEN = COFT_RUPAY_TOKEN;
    private static final String VISA_COFT_TOKEN = COFT_VISA_TOKEN;
    private static final String VISA_ALT_TOKEN = COFT_VISA_TOKEN;
    private static final String dinersExpiry = "022030";
    private static final String expiry = "122027";

    private static final String JSON_POST_URL = "/checkoutpage/new_nplus_page.jsp?env=" + LocalConfig.ENV_NAME;
    private final NativePlusHoldpayPage nativePlusHoldpayPage = new NativePlusHoldpayPage();

    private final static ResponseSpecification SUCCESS_RESPONSE = new ResponseSpecBuilder()
            .expectStatusCode(200)
            .expectBody("body.resultInfo.resultStatus", equalToIgnoringCase("s"))
            .expectBody("body.resultInfo.resultCode", equalToIgnoringCase("0000"))
            .expectBody("body.resultInfo.resultMsg", equalToIgnoringCase("Success"))
            .build();

    @Owner(MEHUL_GUPTA)
    @Test(description="Custom Checkout with RUPAY CARD , Old Apis migrated to new flow with PAR config disable - EMI and Bank offers Applied, Amount Based")
    public void testCustomCheckoutOldFlow_EMI_BO_RUPAY_CARD_PAR_DISABLE() throws Exception {
        User user=userManager.getForRead(Label.BASIC);
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_DISABLE;
        String custid=user.custId();
        String orderId = CommonHelpers.generateOrderId();
        String transactionAmount="400";
        PaymentOption paymentOption= new PaymentOption(transactionAmount,"EMI",null,"HDFC",RUPAY_CC_CARD_NO,"3");
        ApplyPromoV2DTO applyPromoV2DTO = new ApplyPromoV2DTO.Builder()
                .setChannelId("WEB")
                .setRequestId(orderId)
                .setToken(user.ssoToken())
                .setTokenType("SSO")
                .setCustId(custid)
                .setPaymentOptions(Arrays.asList(paymentOption))
                .setMid(mid.getId())
                .setTotalTransactionAmount(transactionAmount)
                .build();
        JsonPath jsonPath= ApplyPromoV2Api.applyPromoV2(applyPromoV2DTO, mid,"");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.paymentOffer.offerBreakup[0].promocodeApplied")).isNotNull();
//        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderId,"AFFORDABILITY_PLATFORM_DISCOVERY");
//        Assertions.assertThat(logs).contains("/ads/v2/offer/apply");
        HashMap<String, Object> paymentOffersObject= jsonPath.get("body.paymentOffer");
        PaymentOffersApplied paymentOffersApplied= new PaymentOffersApplied(paymentOffersObject);
        String cashback = jsonPath.getString("body.paymentOffer.totalCashbackAmount");
        String discount = jsonPath.getString("body.paymentOffer.totalInstantDiscount");
        double payableAmount = 0;
        if (transactionAmount != null && (cashback != null || discount != null)) {
            try {
                payableAmount = Double.valueOf(transactionAmount) - Double.valueOf(cashback != null ? "0" : "0") - Double.valueOf(discount != null ? discount : "0");
            } catch (NumberFormatException e) {
                System.out.println("Error parsing integer values: " + e.getMessage());
            }
        } else {
            System.out.println("One or both values are null: transactionAmount = " + transactionAmount + ", cashback = " + cashback);
        }
        TxnAmount txnAmount = new TxnAmount(String.valueOf(payableAmount));

        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder()
                .setMid(mid.getId())
                .setMerchantKey(mid.getKey())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .setItems(null)
                .setSubventionAmount(String.valueOf(payableAmount))
                .setPrice(String.valueOf(payableAmount))
                .build();
        ApiV1Tenure api = new ApiV1Tenure(mid.getId(), req);
        Response r = api.execute();
        JsonPath tenureResp = r.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.planDetails.emiType", hasItems("SUBVENTION"))
                .extract().jsonPath();

        String planId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId");
        BigInteger intplanid= new BigInteger(planId);
        ApiV1ValidateRequest req2 = new ApiV1ValidateRequest.Builder(user.ssoToken(), "SSO", mid.getId())
                .setMerchantKey(mid.getKey())
                .setPlanId(intplanid)
                .setItems(null)
                .setPaymentDetails(
                        new PaymentDetails()
                                .setTotalTransactionAmount(String.valueOf(payableAmount))
                                .setCardNumber("|"+RUPAY_CC_CARD_NO+"|618|"+expiry)
                )
                .setGenerateTokenForIntent(true)
                .setPrice(String.valueOf(payableAmount))
                .setSubventionAmount(String.valueOf(payableAmount))
                .build();
        ApiV1Validate api2 = new ApiV1Validate(mid.getId(), req2);
        Response r2 = api2.execute();
        r2.then()
                .spec(SUCCESS_RESPONSE);
        JsonPath jsonPath2 = r2.jsonPath();
//        String logs2 = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,"AFFORDABILITY_PLATFORM_DISCOVERY");
//        Assertions.assertThat(logs2).contains("/ads/v2/offer/apply");
        double payableAmount2 = Double.valueOf(jsonPath2.getString("body.finalTransactionAmount"));
        TxnAmount txnAmount2 = new TxnAmount(String.valueOf(payableAmount2));
        String emiSubventionToken = jsonPath2.getString("body.emiSubventionToken");
        String pgPlanId = jsonPath2.getString("body.pgPlanId");

        String orderId2 = CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), mid)
                .setTxnValue(transactionAmount)
                .setPayableAmount(txnAmount2)
                .setOrderId(orderId2)
                .setEmiSubventionToken(emiSubventionToken)
                .setPaymentOffersApplied(paymentOffersApplied)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(),"TXN_TOKEN",txnToken,initTxnDTO.orderFromBody().toString(),txnAmount.getValue())
                .setPaymentMode("EMI")
                .setCardInfo("|" + RUPAY_CC_CARD_NO + "|618|"+expiry)
                .setAuthMode("otp")
                .setChannelCode("RUPAY")
                .setTxnAmount(null)
                .setEmiType("CREDIT_CARD")
                .setPlanId(pgPlanId)
                .build();
        String jsonForm = QRHelper.executeProcessTransactionV1(processTxnV1Request);
        Assertions.assertThat(jsonForm).contains("\"resultStatus\":\"S\"");
        String logsResponse = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody().toString(), "/ats/v2/order/checkout", "RESPONSE");
        Assertions.assertThat(logsResponse).contains("Success");
        Assertions.assertThat(logsResponse).contains("\"isOfferCheckoutRequired\":true");
        Assertions.assertThat(logsResponse).contains("\"isSubventionOfferApplied\":true");
        Assertions.assertThat(logsResponse).contains("\"isBankOfferApplied\":true");
        //System.out.println("Checkout -> "+logsResponse);
        nativePlusHoldpayPage.
                launch(LocalConfig.MOCK_HOST + JSON_POST_URL).
                fillAndSubmitJsonForm(jsonForm);
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(mid.getId())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateTxnAmount(String.valueOf(payableAmount2))
                .AssertAll();
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).contains(pgPlanId);
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).contains("savings");
    }

    @Owner(MEHUL_GUPTA)
    @Test(description="Custom Checkout with VISA CARD , Old Apis migrated to new flow with PAR config disable - EMI and Bank offers Applied, Amount Based")
    public void testCustomCheckoutOldFlow_EMI_BO_VISA_PAR_CARD_DISABLE() throws Exception {
        User user=userManager.getForRead(Label.BASIC);
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_DISABLE;
        String custid=user.custId();
        String orderId = CommonHelpers.generateOrderId();
        String transactionAmount="400";
        PaymentOption paymentOption= new PaymentOption(transactionAmount,"EMI",null,"HDFC",VISA_CC_CARD_NO,"3");
        ApplyPromoV2DTO applyPromoV2DTO = new ApplyPromoV2DTO.Builder()
                .setChannelId("WEB")
                .setRequestId(orderId)
                .setToken(user.ssoToken())
                .setTokenType("SSO")
                .setCustId(custid)
                .setPaymentOptions(Arrays.asList(paymentOption))
                .setMid(mid.getId())
                .setTotalTransactionAmount(transactionAmount)
                .build();
        JsonPath jsonPath= ApplyPromoV2Api.applyPromoV2(applyPromoV2DTO, mid,"");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.paymentOffer.offerBreakup[0].promocodeApplied")).isNotNull();
//        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,"orderId","AFFORDABILITY_PLATFORM_DISCOVERY");
//        Assertions.assertThat(logs).contains("/ads/v2/offer/apply");
        HashMap<String, Object> paymentOffersObject= jsonPath.get("body.paymentOffer");
        PaymentOffersApplied paymentOffersApplied= new PaymentOffersApplied(paymentOffersObject);
        String cashback = jsonPath.getString("body.paymentOffer.totalCashbackAmount");
        String discount = jsonPath.getString("body.paymentOffer.totalInstantDiscount");
        double payableAmount = 0;
        if (transactionAmount != null && (cashback != null || discount != null)) {
            try {
                payableAmount = Double.valueOf(transactionAmount) - Double.valueOf(cashback != null ? "0" : "0") - Double.valueOf(discount != null ? discount : "0");
            } catch (NumberFormatException e) {
                System.out.println("Error parsing integer values: " + e.getMessage());
            }
        } else {
            System.out.println("One or both values are null: transactionAmount = " + transactionAmount + ", cashback = " + cashback);
        }
        TxnAmount txnAmount = new TxnAmount(String.valueOf(payableAmount));

        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder()
                .setMid(mid.getId())
                .setMerchantKey(mid.getKey())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD")
                )
                .setItems(null)
                .setSubventionAmount(String.valueOf(payableAmount))
                .setPrice(String.valueOf(payableAmount))
                .build();
        ApiV1Tenure api = new ApiV1Tenure(mid.getId(), req);
        Response r = api.execute();
        JsonPath tenureResp = r.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.planDetails.emiType", hasItems("SUBVENTION"))
                .extract().jsonPath();

        String planId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId");
        BigInteger intplanid= new BigInteger(planId);
        ApiV1ValidateRequest req2 = new ApiV1ValidateRequest.Builder(user.ssoToken(), "SSO", mid.getId())
                .setMerchantKey(mid.getKey())
                .setPlanId(intplanid)
                .setItems(null)
                .setPaymentDetails(
                        new PaymentDetails()
                                .setTotalTransactionAmount(String.valueOf(payableAmount))
                                .setCardNumber("|"+VISA_CC_CARD_NO+"|618|"+expiry)
                )
                .setGenerateTokenForIntent(true)
                .setPrice(String.valueOf(payableAmount))
                .setSubventionAmount(String.valueOf(payableAmount))
                .build();
        ApiV1Validate api2 = new ApiV1Validate(mid.getId(), req2);
        Response r2 = api2.execute();
        r2.then()
                .spec(SUCCESS_RESPONSE);
        JsonPath jsonPath2 = r2.jsonPath();
//        String logs2 = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,"AFFORDABILITY_PLATFORM_DISCOVERY");
//        Assertions.assertThat(logs2).contains("/ads/v2/offer/apply");
        double payableAmount2 = Double.valueOf(jsonPath2.getString("body.finalTransactionAmount"));
        TxnAmount txnAmount2 = new TxnAmount(String.valueOf(payableAmount2));
        String emiSubventionToken = jsonPath2.getString("body.emiSubventionToken");
        String pgPlanId = jsonPath2.getString("body.pgPlanId");

        String orderId2 = CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), mid)
                .setTxnValue(transactionAmount)
                .setPayableAmount(txnAmount2)
                .setOrderId(orderId2)
                .setEmiSubventionToken(emiSubventionToken)
                .setPaymentOffersApplied(paymentOffersApplied)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(),"TXN_TOKEN",txnToken,initTxnDTO.orderFromBody().toString(),txnAmount.getValue())
                .setPaymentMode("EMI")
                .setCardInfo("|" + VISA_CC_CARD_NO + "|618|"+expiry)
                .setAuthMode("otp")
                .setChannelCode("VISA")
                .setTxnAmount(null)
                .setEmiType("CREDIT_CARD")
                .setPlanId(pgPlanId)
                .build();
        String jsonForm = QRHelper.executeProcessTransactionV1(processTxnV1Request);
        Assertions.assertThat(jsonForm).contains("\"resultStatus\":\"S\"");
        String logsResponse = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody().toString(), "/ats/v2/order/checkout", "RESPONSE");
        Assertions.assertThat(logsResponse).contains("Success");
        Assertions.assertThat(logsResponse).contains("\"isOfferCheckoutRequired\":true");
        Assertions.assertThat(logsResponse).contains("\"isSubventionOfferApplied\":true");
        Assertions.assertThat(logsResponse).contains("\"isBankOfferApplied\":true");
        nativePlusHoldpayPage.
                launch(LocalConfig.MOCK_HOST + JSON_POST_URL).
                fillAndSubmitJsonForm(jsonForm);
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(mid.getId())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateTxnAmount(String.valueOf(payableAmount2))
                .AssertAll();
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).contains(pgPlanId);
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).contains("savings");
    }

    @Owner(MEHUL_GUPTA)
    @Test(description="Custom Checkout with MASTER CARD , Old Apis migrated to new flow with PAR config disable - EMI DC Subvention Applied, Amount Based")
    public void testCustomCheckoutOldFlow_EMIDC_MASTER_CARD_PAR_DISABLE() throws Exception {
        User user=userManager.getForRead(Label.BASIC);
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_DISABLE_MASTER;
        String custid=user.custId();
        String transactionAmount="800";

        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder()
                .setMid(mid.getId())
                .setMerchantKey(mid.getKey())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setFilters(new Filters()
                        .setBankCode("ICICI")
                        .setCardType("DEBIT_CARD"))
                .setItems(null)
                .setSubventionAmount(transactionAmount)
                .setPrice(transactionAmount)
                .build();
        ApiV1Tenure api = new ApiV1Tenure(mid.getId(), req);
        Response r = api.execute();
        JsonPath tenureResp = r.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.planDetails.emiType", hasItems("SUBVENTION"))
                .extract().jsonPath();

        String planId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId");
        BigInteger intplanid= new BigInteger(planId);
        ApiV1ValidateRequest req2 = new ApiV1ValidateRequest.Builder(user.ssoToken(), "SSO", mid.getId())
                .setMerchantKey(mid.getKey())
                .setPlanId(intplanid)
                .setItems(null)
                .setPaymentDetails(
                        new PaymentDetails()
                                .setTotalTransactionAmount(transactionAmount)
                                .setCardNumber("|"+MASTER_ICICI_DC_NUMBER+"|618|"+expiry)
                )
                .setGenerateTokenForIntent(true)
                .setPrice(transactionAmount)
                .setSubventionAmount(transactionAmount)
                .build();
        ApiV1Validate api2 = new ApiV1Validate(mid.getId(), req2);
        Response r2 = api2.execute();
        r2.then()
                .spec(SUCCESS_RESPONSE);
        JsonPath jsonPath = r2.jsonPath();
//        String logs2 = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,"AFFORDABILITY_PLATFORM_DISCOVERY");
//        Assertions.assertThat(logs2).contains("/ads/v2/offer/apply");
        double payableAmount = Double.valueOf(jsonPath.getString("body.finalTransactionAmount"));
        TxnAmount txnAmount = new TxnAmount(String.valueOf(payableAmount));
        String emiSubventionToken = jsonPath.getString("body.emiSubventionToken");
        String pgPlanId = jsonPath.getString("body.pgPlanId");

        String orderId = CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), mid)
                .setTxnValue(transactionAmount)
                .setPayableAmount(txnAmount)
                .setOrderId(orderId)
                .setEmiSubventionToken(emiSubventionToken)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(),"TXN_TOKEN",txnToken,initTxnDTO.orderFromBody().toString(),txnAmount.getValue())
                .setPaymentMode("EMI")
                .setCardInfo("|" + MASTER_ICICI_DC_NUMBER + "|618|"+expiry)
                .setAuthMode("otp")
                .setChannelCode("MASTER")
                .setTxnAmount(null)
                .setEmiType("DEBIT_CARD")
                .setPlanId(pgPlanId)
                .build();
        String jsonForm = QRHelper.executeProcessTransactionV1(processTxnV1Request);
        Assertions.assertThat(jsonForm).contains("\"resultStatus\":\"S\"");
        String logsResponse = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody().toString(), "/ats/v2/order/checkout", "RESPONSE");
        Assertions.assertThat(logsResponse).contains("Success");
        Assertions.assertThat(logsResponse).contains("\"isOfferCheckoutRequired\":true");
        Assertions.assertThat(logsResponse).contains("\"isSubventionOfferApplied\":true");
        Assertions.assertThat(logsResponse).contains("\"isBankOfferApplied\":false");
        //System.out.println("Checkout -> "+logsResponse);
        nativePlusHoldpayPage.
                launch(LocalConfig.MOCK_HOST + JSON_POST_URL).
                fillAndSubmitJsonForm(jsonForm);
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(mid.getId())
                .validatePaymentMode("EMI_DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateTxnAmount(String.valueOf(payableAmount))
                .AssertAll();
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).contains(pgPlanId);
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).isNull();
    }

    @Owner(MEHUL_GUPTA)
    @Test(description="Custom Checkout with DINERS CARD , Old Apis migrated to new flow with PAR config disable - EMI Subvention Applied, Amount Based")
    public void testCustomCheckoutOldFlow_EMI_DINERS_CARD_PAR_DISABLE() throws Exception {
        User user=userManager.getForRead(Label.BASIC);
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_DISABLE_DINERS;
        String custid=user.custId();
        String transactionAmount="800";

        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder()
                .setMid(mid.getId())
                .setMerchantKey(mid.getKey())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .setItems(null)
                .setSubventionAmount(transactionAmount)
                .setPrice(transactionAmount)
                .build();
        ApiV1Tenure api = new ApiV1Tenure(mid.getId(), req);
        Response r = api.execute();
        JsonPath tenureResp = r.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.planDetails.emiType", hasItems("SUBVENTION"))
                .extract().jsonPath();

        String planId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId");
        BigInteger intplanid= new BigInteger(planId);
        ApiV1ValidateRequest req2 = new ApiV1ValidateRequest.Builder(user.ssoToken(), "SSO", mid.getId())
                .setMerchantKey(mid.getKey())
                .setPlanId(intplanid)
                .setItems(null)
                .setPaymentDetails(
                        new PaymentDetails()
                                .setTotalTransactionAmount(transactionAmount)
                                .setCardNumber("|"+DINERS_CC_CARD_NO+"|618|"+dinersExpiry)
                )
                .setGenerateTokenForIntent(true)
                .setPrice(transactionAmount)
                .setSubventionAmount(transactionAmount)
                .build();
        ApiV1Validate api2 = new ApiV1Validate(mid.getId(), req2);
        Response r2 = api2.execute();
        r2.then()
                .spec(SUCCESS_RESPONSE);
        JsonPath jsonPath = r2.jsonPath();
//        String logs2 = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,"AFFORDABILITY_PLATFORM_DISCOVERY");
//        Assertions.assertThat(logs2).contains("/ads/v2/offer/apply");
        double payableAmount = Double.valueOf(jsonPath.getString("body.finalTransactionAmount"));
        TxnAmount txnAmount = new TxnAmount(String.valueOf(payableAmount));
        String emiSubventionToken = jsonPath.getString("body.emiSubventionToken");
        String pgPlanId = jsonPath.getString("body.pgPlanId");

        String orderId2 = CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), mid)
                .setTxnValue(transactionAmount)
                .setPayableAmount(txnAmount)
                .setOrderId(orderId2)
                .setEmiSubventionToken(emiSubventionToken)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(),"TXN_TOKEN",txnToken,initTxnDTO.orderFromBody().toString(),txnAmount.getValue())
                .setPaymentMode("EMI")
                .setCardInfo("|" + DINERS_CC_CARD_NO + "|618|"+dinersExpiry)
                .setAuthMode("otp")
                .setChannelCode("DINERS")
                .setTxnAmount(null)
                .setEmiType("CREDIT_CARD")
                .setPlanId(pgPlanId)
                .build();
        String jsonForm = QRHelper.executeProcessTransactionV1(processTxnV1Request);
        Assertions.assertThat(jsonForm).contains("\"resultStatus\":\"S\"");
        String logsResponse = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody().toString(), "/ats/v2/order/checkout", "RESPONSE");
        Assertions.assertThat(logsResponse).contains("Success");
        Assertions.assertThat(logsResponse).contains("\"isOfferCheckoutRequired\":true");
        Assertions.assertThat(logsResponse).contains("\"isSubventionOfferApplied\":true");
        Assertions.assertThat(logsResponse).contains("\"isBankOfferApplied\":false");
        //System.out.println("Checkout -> "+logsResponse);
        nativePlusHoldpayPage.
                launch(LocalConfig.MOCK_HOST + JSON_POST_URL).
                fillAndSubmitJsonForm(jsonForm);
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(mid.getId())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateTxnAmount(String.valueOf(payableAmount))
                .AssertAll();
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).contains(pgPlanId);
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).isNull();
    }

    @Owner(MEHUL_GUPTA)
    @Test(description="Custom Checkout with RUPAY CARD , Old Apis migrated to new flow with PAR config enable - EMI and Bank offers Applied, Amount Based")
    public void testCustomCheckoutOldFlow_EMI_BO_RUPAY_CARD_PAR_ENABLE() throws Exception {
        User user=userManager.getForRead(Label.BASIC);
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_ENABLE;
        String custid=user.custId();
        String orderId = CommonHelpers.generateOrderId();
        String transactionAmount="400";
        PaymentOption paymentOption= new PaymentOption(transactionAmount,"EMI",null,"HDFC",RUPAY_CC_CARD_NO,"3");
        ApplyPromoV2DTO applyPromoV2DTO = new ApplyPromoV2DTO.Builder()
                .setChannelId("WEB")
                .setRequestId(orderId)
                .setToken(user.ssoToken())
                .setTokenType("SSO")
                .setCustId(custid)
                .setPaymentOptions(Arrays.asList(paymentOption))
                .setMid(mid.getId())
                .setTotalTransactionAmount(transactionAmount)
                .build();
        JsonPath jsonPath= ApplyPromoV2Api.applyPromoV2(applyPromoV2DTO, mid,"");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.paymentOffer.offerBreakup[0].promocodeApplied")).isNotNull();
//        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderId,"AFFORDABILITY_PLATFORM_DISCOVERY");
//        Assertions.assertThat(logs).contains("/ads/v2/offer/apply");
        HashMap<String, Object> paymentOffersObject= jsonPath.get("body.paymentOffer");
        PaymentOffersApplied paymentOffersApplied= new PaymentOffersApplied(paymentOffersObject);
        String cashback = jsonPath.getString("body.paymentOffer.totalCashbackAmount");
        String discount = jsonPath.getString("body.paymentOffer.totalInstantDiscount");
        double payableAmount = 0;
        if (transactionAmount != null && (cashback != null || discount != null)) {
            try {
                payableAmount = Double.valueOf(transactionAmount) - Double.valueOf(cashback != null ? "0" : "0") - Double.valueOf(discount != null ? discount : "0");
            } catch (NumberFormatException e) {
                System.out.println("Error parsing integer values: " + e.getMessage());
            }
        } else {
            System.out.println("One or both values are null: transactionAmount = " + transactionAmount + ", cashback = " + cashback);
        }
        TxnAmount txnAmount = new TxnAmount(String.valueOf(payableAmount));

        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder()
                .setMid(mid.getId())
                .setMerchantKey(mid.getKey())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .setItems(null)
                .setSubventionAmount(String.valueOf(payableAmount))
                .setPrice(String.valueOf(payableAmount))
                .build();
        ApiV1Tenure api = new ApiV1Tenure(mid.getId(), req);
        Response r = api.execute();
        JsonPath tenureResp = r.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.planDetails.emiType", hasItems("SUBVENTION"))
                .extract().jsonPath();

        String planId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId");
        BigInteger intplanid= new BigInteger(planId);
        ApiV1ValidateRequest req2 = new ApiV1ValidateRequest.Builder(user.ssoToken(), "SSO", mid.getId())
                .setMerchantKey(mid.getKey())
                .setPlanId(intplanid)
                .setItems(null)
                .setPaymentDetails(
                        new PaymentDetails()
                                .setTotalTransactionAmount(String.valueOf(payableAmount))
                                .setCardNumber("|"+RUPAY_CC_CARD_NO+"|618|"+expiry)
                )
                .setGenerateTokenForIntent(true)
                .setPrice(String.valueOf(payableAmount))
                .setSubventionAmount(String.valueOf(payableAmount))
                .build();
        ApiV1Validate api2 = new ApiV1Validate(mid.getId(), req2);
        Response r2 = api2.execute();
        r2.then()
                .spec(SUCCESS_RESPONSE);
        JsonPath jsonPath2 = r2.jsonPath();
//        String logs2 = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,"AFFORDABILITY_PLATFORM_DISCOVERY");
//        Assertions.assertThat(logs2).contains("/ads/v2/offer/apply");
        double payableAmount2 = Double.valueOf(jsonPath2.getString("body.finalTransactionAmount"));
        TxnAmount txnAmount2 = new TxnAmount(String.valueOf(payableAmount2));
        String emiSubventionToken = jsonPath2.getString("body.emiSubventionToken");
        String pgPlanId = jsonPath2.getString("body.pgPlanId");

        String orderId2 = CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), mid)
                .setTxnValue(transactionAmount)
                .setPayableAmount(txnAmount2)
                .setOrderId(orderId2)
                .setEmiSubventionToken(emiSubventionToken)
                .setPaymentOffersApplied(paymentOffersApplied)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(),"TXN_TOKEN",txnToken,initTxnDTO.orderFromBody().toString(),txnAmount.getValue())
                .setPaymentMode("EMI")
                .setCardInfo("|" + RUPAY_CC_CARD_NO + "|618|"+expiry)
                .setAuthMode("otp")
                .setChannelCode("RUPAY")
                .setTxnAmount(null)
                .setEmiType("CREDIT_CARD")
                .setPlanId(pgPlanId)
                .build();
        String jsonForm = QRHelper.executeProcessTransactionV1(processTxnV1Request);
        Assertions.assertThat(jsonForm).contains("\"resultStatus\":\"S\"");
        String logsResponse = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody().toString(), "/ats/v2/order/checkout", "RESPONSE");
        Assertions.assertThat(logsResponse).contains("Success");
        Assertions.assertThat(logsResponse).contains("\"isOfferCheckoutRequired\":true");
        Assertions.assertThat(logsResponse).contains("\"isSubventionOfferApplied\":true");
        Assertions.assertThat(logsResponse).contains("\"isBankOfferApplied\":true");
        //System.out.println("Checkout -> "+logsResponse);
        nativePlusHoldpayPage.
                launch(LocalConfig.MOCK_HOST + JSON_POST_URL).
                fillAndSubmitJsonForm(jsonForm);
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(mid.getId())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateTxnAmount(String.valueOf(payableAmount2))
                .AssertAll();
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).contains(pgPlanId);
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).contains("savings");
    }

    @Owner(MEHUL_GUPTA)
    @Test(description="Custom Checkout with VISA CARD , Old Apis migrated to new flow with PAR config enable - EMI and Bank offers Applied, Amount Based")
    public void testCustomCheckoutOldFlow_EMI_BO_VISA_CARD_PAR_ENABLE() throws Exception {
        User user=userManager.getForRead(Label.BASIC);
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_ENABLE;
        String custid=user.custId();
        String orderId = CommonHelpers.generateOrderId();
        String transactionAmount="400";
        PaymentOption paymentOption= new PaymentOption(transactionAmount,"EMI",null,"HDFC",VISA_CC_CARD_NO,"3");
        ApplyPromoV2DTO applyPromoV2DTO = new ApplyPromoV2DTO.Builder()
                .setChannelId("WEB")
                .setRequestId(orderId)
                .setToken(user.ssoToken())
                .setTokenType("SSO")
                .setCustId(custid)
                .setPaymentOptions(Arrays.asList(paymentOption))
                .setMid(mid.getId())
                .setTotalTransactionAmount(transactionAmount)
                .build();
        JsonPath jsonPath= ApplyPromoV2Api.applyPromoV2(applyPromoV2DTO, mid,"");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.paymentOffer.offerBreakup[0].promocodeApplied")).isNotNull();
//        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,"orderId","AFFORDABILITY_PLATFORM_DISCOVERY");
//        Assertions.assertThat(logs).contains("/ads/v2/offer/apply");
        HashMap<String, Object> paymentOffersObject= jsonPath.get("body.paymentOffer");
        PaymentOffersApplied paymentOffersApplied= new PaymentOffersApplied(paymentOffersObject);
        String cashback = jsonPath.getString("body.paymentOffer.totalCashbackAmount");
        String discount = jsonPath.getString("body.paymentOffer.totalInstantDiscount");
        double payableAmount = 0;
        if (transactionAmount != null && (cashback != null || discount != null)) {
            try {
                payableAmount = Double.valueOf(transactionAmount) - Double.valueOf(cashback != null ? "0" : "0") - Double.valueOf(discount != null ? discount : "0");
            } catch (NumberFormatException e) {
                System.out.println("Error parsing integer values: " + e.getMessage());
            }
        } else {
            System.out.println("One or both values are null: transactionAmount = " + transactionAmount + ", cashback = " + cashback);
        }
        TxnAmount txnAmount = new TxnAmount(String.valueOf(payableAmount));

        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder()
                .setMid(mid.getId())
                .setMerchantKey(mid.getKey())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD")
                )
                .setItems(null)
                .setSubventionAmount(String.valueOf(payableAmount))
                .setPrice(String.valueOf(payableAmount))
                .build();
        ApiV1Tenure api = new ApiV1Tenure(mid.getId(), req);
        Response r = api.execute();
        JsonPath tenureResp = r.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.planDetails.emiType", hasItems("SUBVENTION"))
                .extract().jsonPath();

        String planId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId");
        BigInteger intplanid= new BigInteger(planId);
        ApiV1ValidateRequest req2 = new ApiV1ValidateRequest.Builder(user.ssoToken(), "SSO", mid.getId())
                .setMerchantKey(mid.getKey())
                .setPlanId(intplanid)
                .setItems(null)
                .setPaymentDetails(
                        new PaymentDetails()
                                .setTotalTransactionAmount(String.valueOf(payableAmount))
                                .setCardNumber("|"+VISA_CC_CARD_NO+"|618|"+expiry)
                )
                .setGenerateTokenForIntent(true)
                .setPrice(String.valueOf(payableAmount))
                .setSubventionAmount(String.valueOf(payableAmount))
                .build();
        ApiV1Validate api2 = new ApiV1Validate(mid.getId(), req2);
        Response r2 = api2.execute();
        r2.then()
                .spec(SUCCESS_RESPONSE);
        JsonPath jsonPath2 = r2.jsonPath();
//        String logs2 = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,"AFFORDABILITY_PLATFORM_DISCOVERY");
//        Assertions.assertThat(logs2).contains("/ads/v2/offer/apply");
        double payableAmount2 = Double.valueOf(jsonPath2.getString("body.finalTransactionAmount"));
        TxnAmount txnAmount2 = new TxnAmount(String.valueOf(payableAmount2));
        String emiSubventionToken = jsonPath2.getString("body.emiSubventionToken");
        String pgPlanId = jsonPath2.getString("body.pgPlanId");

        String orderId2 = CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), mid)
                .setTxnValue(transactionAmount)
                .setPayableAmount(txnAmount2)
                .setOrderId(orderId2)
                .setEmiSubventionToken(emiSubventionToken)
                .setPaymentOffersApplied(paymentOffersApplied)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(),"TXN_TOKEN",txnToken,initTxnDTO.orderFromBody().toString(),txnAmount.getValue())
                .setPaymentMode("EMI")
                .setCardInfo("|" + VISA_CC_CARD_NO + "|618|"+expiry)
                .setAuthMode("otp")
                .setChannelCode("VISA")
                .setTxnAmount(null)
                .setEmiType("CREDIT_CARD")
                .setPlanId(pgPlanId)
                .build();
        String jsonForm = QRHelper.executeProcessTransactionV1(processTxnV1Request);
        Assertions.assertThat(jsonForm).contains("\"resultStatus\":\"S\"");
        String logsResponse = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody().toString(), "/ats/v2/order/checkout", "RESPONSE");
        Assertions.assertThat(logsResponse).contains("Success");
        Assertions.assertThat(logsResponse).contains("\"isOfferCheckoutRequired\":true");
        Assertions.assertThat(logsResponse).contains("\"isSubventionOfferApplied\":true");
        Assertions.assertThat(logsResponse).contains("\"isBankOfferApplied\":true");
        nativePlusHoldpayPage.
                launch(LocalConfig.MOCK_HOST + JSON_POST_URL).
                fillAndSubmitJsonForm(jsonForm);
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(mid.getId())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateTxnAmount(String.valueOf(payableAmount2))
                .AssertAll();
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).contains(pgPlanId);
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).contains("savings");
    }


    @Owner(MEHUL_GUPTA)
    @Test(description="Custom Checkout with MASTER CARD , Old Apis migrated to new flow with PAR config enable - EMI DC Subvention Applied, Amount Based")
    public void testCustomCheckoutOldFlow_EMIDC_MASTER_CARD_PAR_ENABLE() throws Exception {
        User user=userManager.getForRead(Label.BASIC);
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_ENABLE_MASTER;
        String custid=user.custId();
        String transactionAmount="800";

        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder()
                .setMid(mid.getId())
                .setMerchantKey(mid.getKey())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setFilters(new Filters()
                        .setBankCode("ICICI")
                        .setCardType("DEBIT_CARD"))
                .setItems(null)
                .setSubventionAmount(transactionAmount)
                .setPrice(transactionAmount)
                .build();
        ApiV1Tenure api = new ApiV1Tenure(mid.getId(), req);
        Response r = api.execute();
        JsonPath tenureResp = r.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.planDetails.emiType", hasItems("SUBVENTION"))
                .extract().jsonPath();

        String planId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId");
        BigInteger intplanid= new BigInteger(planId);
        ApiV1ValidateRequest req2 = new ApiV1ValidateRequest.Builder(user.ssoToken(), "SSO", mid.getId())
                .setMerchantKey(mid.getKey())
                .setPlanId(intplanid)
                .setItems(null)
                .setPaymentDetails(
                        new PaymentDetails()
                                .setTotalTransactionAmount(transactionAmount)
                                .setCardNumber("|"+MASTER_ICICI_DC_NUMBER+"|618|"+expiry)
                )
                .setGenerateTokenForIntent(true)
                .setPrice(transactionAmount)
                .setSubventionAmount(transactionAmount)
                .build();
        ApiV1Validate api2 = new ApiV1Validate(mid.getId(), req2);
        Response r2 = api2.execute();
        r2.then()
                .spec(SUCCESS_RESPONSE);
        JsonPath jsonPath = r2.jsonPath();
//        String logs2 = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,"AFFORDABILITY_PLATFORM_DISCOVERY");
//        Assertions.assertThat(logs2).contains("/ads/v2/offer/apply");
        double payableAmount = Double.valueOf(jsonPath.getString("body.finalTransactionAmount"));
        TxnAmount txnAmount = new TxnAmount(String.valueOf(payableAmount));
        String emiSubventionToken = jsonPath.getString("body.emiSubventionToken");
        String pgPlanId = jsonPath.getString("body.pgPlanId");

        String orderId = CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), mid)
                .setTxnValue(transactionAmount)
                .setPayableAmount(txnAmount)
                .setOrderId(orderId)
                .setEmiSubventionToken(emiSubventionToken)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(),"TXN_TOKEN",txnToken,initTxnDTO.orderFromBody().toString(),txnAmount.getValue())
                .setPaymentMode("EMI")
                .setCardInfo("|" + MASTER_ICICI_DC_NUMBER + "|618|"+expiry)
                .setAuthMode("otp")
                .setChannelCode("MASTER")
                .setTxnAmount(null)
                .setEmiType("DEBIT_CARD")
                .setPlanId(pgPlanId)
                .build();
        String jsonForm = QRHelper.executeProcessTransactionV1(processTxnV1Request);
        Assertions.assertThat(jsonForm).contains("\"resultStatus\":\"S\"");
        String logsResponse = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody().toString(), "/ats/v2/order/checkout", "RESPONSE");
        Assertions.assertThat(logsResponse).contains("Success");
        Assertions.assertThat(logsResponse).contains("\"isOfferCheckoutRequired\":true");
        Assertions.assertThat(logsResponse).contains("\"isSubventionOfferApplied\":true");
        Assertions.assertThat(logsResponse).contains("\"isBankOfferApplied\":false");
        //System.out.println("Checkout -> "+logsResponse);
        nativePlusHoldpayPage.
                launch(LocalConfig.MOCK_HOST + JSON_POST_URL).
                fillAndSubmitJsonForm(jsonForm);
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(mid.getId())
                .validatePaymentMode("EMI_DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateTxnAmount(String.valueOf(payableAmount))
                .AssertAll();
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).contains(pgPlanId);
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).isNull();
    }


    @Owner(MEHUL_GUPTA)
    @Test(description="Custom Checkout with DINERS CARD , Old Apis migrated to new flow with PAR config enable - EMI Subvention Applied, Amount Based")
    public void testCustomCheckoutOldFlow_EMI_DINERS_CARD_PAR_ENABLE() throws Exception {
        User user=userManager.getForRead(Label.BASIC);
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_ENABLE_DINERS;
        String custid=user.custId();
        String transactionAmount="800";

        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder()
                .setMid(mid.getId())
                .setMerchantKey(mid.getKey())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .setItems(null)
                .setSubventionAmount(transactionAmount)
                .setPrice(transactionAmount)
                .build();
        ApiV1Tenure api = new ApiV1Tenure(mid.getId(), req);
        Response r = api.execute();
        JsonPath tenureResp = r.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.planDetails.emiType", hasItems("SUBVENTION"))
                .extract().jsonPath();

        String planId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId");
        BigInteger intplanid= new BigInteger(planId);
        ApiV1ValidateRequest req2 = new ApiV1ValidateRequest.Builder(user.ssoToken(), "SSO", mid.getId())
                .setMerchantKey(mid.getKey())
                .setPlanId(intplanid)
                .setItems(null)
                .setPaymentDetails(
                        new PaymentDetails()
                                .setTotalTransactionAmount(transactionAmount)
                                .setCardNumber("|"+DINERS_CC_CARD_NO+"|618|"+dinersExpiry)
                )
                .setGenerateTokenForIntent(true)
                .setPrice(transactionAmount)
                .setSubventionAmount(transactionAmount)
                .build();
        ApiV1Validate api2 = new ApiV1Validate(mid.getId(), req2);
        Response r2 = api2.execute();
        r2.then()
                .spec(SUCCESS_RESPONSE);
        JsonPath jsonPath = r2.jsonPath();
//        String logs2 = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,"AFFORDABILITY_PLATFORM_DISCOVERY");
//        Assertions.assertThat(logs2).contains("/ads/v2/offer/apply");
        double payableAmount = Double.valueOf(jsonPath.getString("body.finalTransactionAmount"));
        TxnAmount txnAmount = new TxnAmount(String.valueOf(payableAmount));
        String emiSubventionToken = jsonPath.getString("body.emiSubventionToken");
        String pgPlanId = jsonPath.getString("body.pgPlanId");

        String orderId2 = CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), mid)
                .setTxnValue(transactionAmount)
                .setPayableAmount(txnAmount)
                .setOrderId(orderId2)
                .setEmiSubventionToken(emiSubventionToken)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(),"TXN_TOKEN",txnToken,initTxnDTO.orderFromBody().toString(),txnAmount.getValue())
                .setPaymentMode("EMI")
                .setCardInfo("|" + DINERS_CC_CARD_NO + "|618|"+dinersExpiry)
                .setAuthMode("otp")
                .setChannelCode("DINERS")
                .setTxnAmount(null)
                .setEmiType("CREDIT_CARD")
                .setPlanId(pgPlanId)
                .build();
        String jsonForm = QRHelper.executeProcessTransactionV1(processTxnV1Request);
        Assertions.assertThat(jsonForm).contains("\"resultStatus\":\"S\"");
        String logsResponse = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody().toString(), "/ats/v2/order/checkout", "RESPONSE");
        Assertions.assertThat(logsResponse).contains("Success");
        Assertions.assertThat(logsResponse).contains("\"isOfferCheckoutRequired\":true");
        Assertions.assertThat(logsResponse).contains("\"isSubventionOfferApplied\":true");
        Assertions.assertThat(logsResponse).contains("\"isBankOfferApplied\":false");
        //System.out.println("Checkout -> "+logsResponse);
        nativePlusHoldpayPage.
                launch(LocalConfig.MOCK_HOST + JSON_POST_URL).
                fillAndSubmitJsonForm(jsonForm);
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(mid.getId())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateTxnAmount(String.valueOf(payableAmount))
                .AssertAll();
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).contains(pgPlanId);
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).isNull();
    }

    @Owner(MEHUL_GUPTA)
    @Test(description="Custom Checkout with RUPAY External Coft Token , Old Apis migrated to new flow with PAR config disable - EMI Subvention Applied, Amount Based")
    public void testCustomCheckoutOldFlow_EMI_RUPAY_COFT_PAR_DISABLE() throws Exception {
        User user=userManager.getForRead(Label.BASIC);
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_DISABLE;
        String custid=user.custId();
        String transactionAmount="800";
        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder()
                .setMid(mid.getId())
                .setMerchantKey(mid.getKey())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .setItems(null)
                .setSubventionAmount(transactionAmount)
                .setPrice(transactionAmount)
                .build();
        ApiV1Tenure api = new ApiV1Tenure(mid.getId(), req);
        Response r = api.execute();
        JsonPath tenureResp = r.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.planDetails.emiType", hasItems("SUBVENTION"))
                .extract().jsonPath();

        String planId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId");
        BigInteger intplanid= new BigInteger(planId);
        ApiV1ValidateRequest req2 = new ApiV1ValidateRequest.Builder(user.ssoToken(), "SSO", mid.getId())
                .setMerchantKey(mid.getKey())
                .setPlanId(intplanid)
                .setItems(null)
                .setPaymentDetails(
                        new PaymentDetails()
                                .setTotalTransactionAmount(transactionAmount)
                                .setCardBin(RUPAY_CC_CARD_NO.substring(0,9))
                )
                .setGenerateTokenForIntent(true)
                .setPrice(transactionAmount)
                .setCacheCardToken(RUPAY_COFT_TOKEN)
                .setSubventionAmount(transactionAmount)
                .build();
        ApiV1Validate api2 = new ApiV1Validate(mid.getId(), req2);
        Response r2 = api2.execute();
        r2.then()
                .spec(SUCCESS_RESPONSE);
        JsonPath jsonPath2 = r2.jsonPath();
//        String logs2 = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,"AFFORDABILITY_PLATFORM_DISCOVERY");
//        Assertions.assertThat(logs2).contains("/ads/v2/offer/apply");
        double payableAmount = Double.valueOf(jsonPath2.getString("body.finalTransactionAmount"));
        TxnAmount txnAmount = new TxnAmount(String.valueOf(payableAmount));
        String emiSubventionToken = jsonPath2.getString("body.emiSubventionToken");
        String pgPlanId = jsonPath2.getString("body.pgPlanId");

        String orderId = CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), mid)
                .setTxnValue(transactionAmount)
                .setPayableAmount(txnAmount)
                .setOrderId(orderId)
                .setEmiSubventionToken(emiSubventionToken)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CardTokenInfo cardTokenInfo = new CardTokenInfo();
        cardTokenInfo.setCardToken(RUPAY_COFT_TOKEN);
        cardTokenInfo.setTokenExpiry(expiry);
        cardTokenInfo.setCardSuffix("0001");
        cardTokenInfo.setTavv("AgAAAAAKh8QqrzAABFcgm0AAAA=");
        cardTokenInfo.setPanUniqueReference("V0010013021361288827541727409");
        cardTokenInfo.setTokenType("COFT");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(),"TXN_TOKEN",txnToken,initTxnDTO.orderFromBody().toString(),txnAmount.getValue())
                .setPaymentMode("EMI")
                .setCardInfo("||618|")
                .setAuthMode("otp")
                .setcardTokenInfo(cardTokenInfo)
                .setChannelCode("RUPAY")
                .setTxnAmount(null)
                .setEmiType("CREDIT_CARD")
                .setPlanId(pgPlanId)
                .build();
        String jsonForm = QRHelper.executeProcessTransactionV1(processTxnV1Request);
        Assertions.assertThat(jsonForm).contains("\"resultStatus\":\"S\"");
        String logsResponse = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody().toString(), "/ats/v2/order/checkout", "RESPONSE");
        Assertions.assertThat(logsResponse).contains("Success");
        Assertions.assertThat(logsResponse).contains("\"isOfferCheckoutRequired\":true");
        Assertions.assertThat(logsResponse).contains("\"isSubventionOfferApplied\":true");
        Assertions.assertThat(logsResponse).contains("\"isBankOfferApplied\":false");
        //System.out.println("Checkout -> "+logsResponse);
        nativePlusHoldpayPage.
                launch(LocalConfig.MOCK_HOST + JSON_POST_URL).
                fillAndSubmitJsonForm(jsonForm);
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(mid.getId())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateTxnAmount(String.valueOf(payableAmount))
                .AssertAll();
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).contains(pgPlanId);
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).isNull();
    }

    @Owner(MEHUL_GUPTA)
    @Test(description="Custom Checkout with VISA External Coft Token , Old Apis migrated to new flow with PAR config disable - EMI Subvention Applied, Amount Based")
    public void testCustomCheckoutOldFlow_EMI_VISA_COFT_PAR_DISABLE() throws Exception {
        User user=userManager.getForRead(Label.BASIC);
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_DISABLE;
        String custid=user.custId();
        String transactionAmount="800";
        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder()
                .setMid(mid.getId())
                .setMerchantKey(mid.getKey())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .setItems(null)
                .setSubventionAmount(transactionAmount)
                .setPrice(transactionAmount)
                .build();
        ApiV1Tenure api = new ApiV1Tenure(mid.getId(), req);
        Response r = api.execute();
        JsonPath tenureResp = r.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.planDetails.emiType", hasItems("SUBVENTION"))
                .extract().jsonPath();

        String planId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId");
        BigInteger intplanid= new BigInteger(planId);
        ApiV1ValidateRequest req2 = new ApiV1ValidateRequest.Builder(user.ssoToken(), "SSO", mid.getId())
                .setMerchantKey(mid.getKey())
                .setPlanId(intplanid)
                .setItems(null)
                .setPaymentDetails(
                        new PaymentDetails()
                                .setTotalTransactionAmount(transactionAmount)
                                .setCardBin(VISA_COFT_TOKEN.substring(0,9))
                )
                .setGenerateTokenForIntent(true)
                .setPrice(transactionAmount)
                .setCacheCardToken(VISA_COFT_TOKEN)
                .setSubventionAmount(transactionAmount)
                .build();
        ApiV1Validate api2 = new ApiV1Validate(mid.getId(), req2);
        Response r2 = api2.execute();
        r2.then()
                .spec(SUCCESS_RESPONSE);
        JsonPath jsonPath2 = r2.jsonPath();
//        String logs2 = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,"AFFORDABILITY_PLATFORM_DISCOVERY");
//        Assertions.assertThat(logs2).contains("/ads/v2/offer/apply");
        double payableAmount = Double.valueOf(jsonPath2.getString("body.finalTransactionAmount"));
        TxnAmount txnAmount = new TxnAmount(String.valueOf(payableAmount));
        String emiSubventionToken = jsonPath2.getString("body.emiSubventionToken");
        String pgPlanId = jsonPath2.getString("body.pgPlanId");

        String orderId = CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), mid)
                .setTxnValue(transactionAmount)
                .setPayableAmount(txnAmount)
                .setOrderId(orderId)
                .setEmiSubventionToken(emiSubventionToken)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CardTokenInfo cardTokenInfo = new CardTokenInfo();
        cardTokenInfo.setCardToken(VISA_COFT_TOKEN);
        cardTokenInfo.setTokenExpiry(expiry);
        cardTokenInfo.setCardSuffix("0336");
        cardTokenInfo.setTavv("AgAAAAoAPd52XQkAmeNXghMAAAA");
        cardTokenInfo.setPanUniqueReference("V0010013021361288827541710336");
        cardTokenInfo.setTokenType("COFT");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(),"TXN_TOKEN",txnToken,initTxnDTO.orderFromBody().toString(),txnAmount.getValue())
                .setPaymentMode("EMI")
                .setCardInfo("||618|")
                .setAuthMode("otp")
                .setcardTokenInfo(cardTokenInfo)
                .setChannelCode("VISA")
                .setTxnAmount(null)
                .setEmiType("CREDIT_CARD")
                .setPlanId(pgPlanId)
                .build();
        String jsonForm = QRHelper.executeProcessTransactionV1(processTxnV1Request);
        Assertions.assertThat(jsonForm).contains("\"resultStatus\":\"S\"");
        String logsResponse = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody().toString(), "/ats/v2/order/checkout", "RESPONSE");
        Assertions.assertThat(logsResponse).contains("Success");
        Assertions.assertThat(logsResponse).contains("\"isOfferCheckoutRequired\":true");
        Assertions.assertThat(logsResponse).contains("\"isSubventionOfferApplied\":true");
        Assertions.assertThat(logsResponse).contains("\"isBankOfferApplied\":false");
        //System.out.println("Checkout -> "+logsResponse);
        nativePlusHoldpayPage.
                launch(LocalConfig.MOCK_HOST + JSON_POST_URL).
                fillAndSubmitJsonForm(jsonForm);
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(mid.getId())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateTxnAmount(String.valueOf(payableAmount))
                .AssertAll();
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).contains(pgPlanId);
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).isNull();
    }

    @Owner(MEHUL_GUPTA)
    @Test(description="Custom Checkout with RUPAY External Coft Token , Old Apis migrated to new flow with PAR config disable - EMI Subvention Applied, Amount Based")
    public void testCustomCheckoutOldFlow_EMI_RUPAY_COFT_PAR_ENABLE() throws Exception {
        User user=userManager.getForRead(Label.BASIC);
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_ENABLE;
        String custid=user.custId();
        String transactionAmount="800";
        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder()
                .setMid(mid.getId())
                .setMerchantKey(mid.getKey())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .setItems(null)
                .setSubventionAmount(transactionAmount)
                .setPrice(transactionAmount)
                .build();
        ApiV1Tenure api = new ApiV1Tenure(mid.getId(), req);
        Response r = api.execute();
        JsonPath tenureResp = r.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.planDetails.emiType", hasItems("SUBVENTION"))
                .extract().jsonPath();

        String planId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId");
        BigInteger intplanid= new BigInteger(planId);
        ApiV1ValidateRequest req2 = new ApiV1ValidateRequest.Builder(user.ssoToken(), "SSO", mid.getId())
                .setMerchantKey(mid.getKey())
                .setPlanId(intplanid)
                .setItems(null)
                .setPaymentDetails(
                        new PaymentDetails()
                                .setTotalTransactionAmount(transactionAmount)
                                .setCardBin(RUPAY_CC_CARD_NO.substring(0,9))
                )
                .setGenerateTokenForIntent(true)
                .setPrice(transactionAmount)
                .setCacheCardToken(RUPAY_COFT_TOKEN)
                .setSubventionAmount(transactionAmount)
                .build();
        ApiV1Validate api2 = new ApiV1Validate(mid.getId(), req2);
        Response r2 = api2.execute();
        r2.then()
                .spec(SUCCESS_RESPONSE);
        JsonPath jsonPath2 = r2.jsonPath();
//        String logs2 = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,"AFFORDABILITY_PLATFORM_DISCOVERY");
//        Assertions.assertThat(logs2).contains("/ads/v2/offer/apply");
        double payableAmount = Double.valueOf(jsonPath2.getString("body.finalTransactionAmount"));
        TxnAmount txnAmount = new TxnAmount(String.valueOf(payableAmount));
        String emiSubventionToken = jsonPath2.getString("body.emiSubventionToken");
        String pgPlanId = jsonPath2.getString("body.pgPlanId");

        String orderId = CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), mid)
                .setTxnValue(transactionAmount)
                .setPayableAmount(txnAmount)
                .setOrderId(orderId)
                .setEmiSubventionToken(emiSubventionToken)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CardTokenInfo cardTokenInfo = new CardTokenInfo();
        cardTokenInfo.setCardToken(RUPAY_COFT_TOKEN);
        cardTokenInfo.setTokenExpiry(expiry);
        cardTokenInfo.setCardSuffix("0001");
        cardTokenInfo.setTavv("AgAAAAAKh8QqrzAABFcgm0AAAA=");
        cardTokenInfo.setPanUniqueReference("V0010013021361288827541727409");
        cardTokenInfo.setTokenType("COFT");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(),"TXN_TOKEN",txnToken,initTxnDTO.orderFromBody().toString(),txnAmount.getValue())
                .setPaymentMode("EMI")
                .setCardInfo("||618|")
                .setAuthMode("otp")
                .setcardTokenInfo(cardTokenInfo)
                .setChannelCode("RUPAY")
                .setTxnAmount(null)
                .setEmiType("CREDIT_CARD")
                .setPlanId(pgPlanId)
                .build();
        String jsonForm = QRHelper.executeProcessTransactionV1(processTxnV1Request);
        Assertions.assertThat(jsonForm).contains("\"resultStatus\":\"S\"");
        String logsResponse = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody().toString(), "/ats/v2/order/checkout", "RESPONSE");
        Assertions.assertThat(logsResponse).contains("Success");
        Assertions.assertThat(logsResponse).contains("\"isOfferCheckoutRequired\":true");
        Assertions.assertThat(logsResponse).contains("\"isSubventionOfferApplied\":true");
        Assertions.assertThat(logsResponse).contains("\"isBankOfferApplied\":false");
        //System.out.println("Checkout -> "+logsResponse);
        nativePlusHoldpayPage.
                launch(LocalConfig.MOCK_HOST + JSON_POST_URL).
                fillAndSubmitJsonForm(jsonForm);
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(mid.getId())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateTxnAmount(String.valueOf(payableAmount))
                .AssertAll();
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).contains(pgPlanId);
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).isNull();
    }

    @Owner(MEHUL_GUPTA)
    @Test(description="Custom Checkout with VISA External Coft Token , Old Apis migrated to new flow with PAR config disable - EMI Subvention Applied, Amount Based")
    public void testCustomCheckoutOldFlow_EMI_VISA_COFT_PAR_ENABLE() throws Exception {
        User user=userManager.getForRead(Label.BASIC);
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_ENABLE;
        String custid=user.custId();
        String transactionAmount="800";
        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder()
                .setMid(mid.getId())
                .setMerchantKey(mid.getKey())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .setItems(null)
                .setSubventionAmount(transactionAmount)
                .setPrice(transactionAmount)
                .build();
        ApiV1Tenure api = new ApiV1Tenure(mid.getId(), req);
        Response r = api.execute();
        JsonPath tenureResp = r.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.planDetails.emiType", hasItems("SUBVENTION"))
                .extract().jsonPath();

        String planId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId");
        BigInteger intplanid= new BigInteger(planId);
        ApiV1ValidateRequest req2 = new ApiV1ValidateRequest.Builder(user.ssoToken(), "SSO", mid.getId())
                .setMerchantKey(mid.getKey())
                .setPlanId(intplanid)
                .setItems(null)
                .setPaymentDetails(
                        new PaymentDetails()
                                .setTotalTransactionAmount(transactionAmount)
                                .setCardBin(VISA_COFT_TOKEN.substring(0,9))
                )
                .setGenerateTokenForIntent(true)
                .setPrice(transactionAmount)
                .setCacheCardToken(VISA_COFT_TOKEN)
                .setSubventionAmount(transactionAmount)
                .build();
        ApiV1Validate api2 = new ApiV1Validate(mid.getId(), req2);
        Response r2 = api2.execute();
        r2.then()
                .spec(SUCCESS_RESPONSE);
        JsonPath jsonPath2 = r2.jsonPath();
//        String logs2 = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,"AFFORDABILITY_PLATFORM_DISCOVERY");
//        Assertions.assertThat(logs2).contains("/ads/v2/offer/apply");
        double payableAmount = Double.valueOf(jsonPath2.getString("body.finalTransactionAmount"));
        TxnAmount txnAmount = new TxnAmount(String.valueOf(payableAmount));
        String emiSubventionToken = jsonPath2.getString("body.emiSubventionToken");
        String pgPlanId = jsonPath2.getString("body.pgPlanId");

        String orderId = CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), mid)
                .setTxnValue(transactionAmount)
                .setPayableAmount(txnAmount)
                .setOrderId(orderId)
                .setEmiSubventionToken(emiSubventionToken)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CardTokenInfo cardTokenInfo = new CardTokenInfo();
        cardTokenInfo.setCardToken(VISA_COFT_TOKEN);
        cardTokenInfo.setTokenExpiry(expiry);
        cardTokenInfo.setCardSuffix("0336");
        cardTokenInfo.setTavv("AgAAAAoAPd52XQkAmeNXghMAAAA");
        cardTokenInfo.setPanUniqueReference("V0010013021361288827541710336");
        cardTokenInfo.setTokenType("COFT");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(),"TXN_TOKEN",txnToken,initTxnDTO.orderFromBody().toString(),txnAmount.getValue())
                .setPaymentMode("EMI")
                .setCardInfo("||618|")
                .setAuthMode("otp")
                .setcardTokenInfo(cardTokenInfo)
                .setChannelCode("VISA")
                .setTxnAmount(null)
                .setEmiType("CREDIT_CARD")
                .setPlanId(pgPlanId)
                .build();
        String jsonForm = QRHelper.executeProcessTransactionV1(processTxnV1Request);
        Assertions.assertThat(jsonForm).contains("\"resultStatus\":\"S\"");
        String logsResponse = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody().toString(), "/ats/v2/order/checkout", "RESPONSE");
        Assertions.assertThat(logsResponse).contains("Success");
        Assertions.assertThat(logsResponse).contains("\"isOfferCheckoutRequired\":true");
        Assertions.assertThat(logsResponse).contains("\"isSubventionOfferApplied\":true");
        Assertions.assertThat(logsResponse).contains("\"isBankOfferApplied\":false");
        //System.out.println("Checkout -> "+logsResponse);
        nativePlusHoldpayPage.
                launch(LocalConfig.MOCK_HOST + JSON_POST_URL).
                fillAndSubmitJsonForm(jsonForm);
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(mid.getId())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateTxnAmount(String.valueOf(payableAmount))
                .AssertAll();
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).contains(pgPlanId);
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).isNull();
    }


    @Owner(MEHUL_GUPTA)
    @Test(description="Custom Checkout with RUPAY Alt Token , Old Apis migrated to new flow with PAR config enable - EMI Subvention Applied, Amount Based")
    public void testCustomCheckoutOldFlow_EMI_RUPAY_ALT_PAR_ENABLE() throws Exception {
        User user=userManager.getForRead(Label.BASIC);
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_ENABLE;
        String custid=user.custId();
        String transactionAmount="800";
        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder()
                .setMid(mid.getId())
                .setMerchantKey(mid.getKey())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .setItems(null)
                .setSubventionAmount(transactionAmount)
                .setPrice(transactionAmount)
                .build();
        ApiV1Tenure api = new ApiV1Tenure(mid.getId(), req);
        Response r = api.execute();
        JsonPath tenureResp = r.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.planDetails.emiType", hasItems("SUBVENTION"))
                .extract().jsonPath();

        String planId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId");
        BigInteger intplanid= new BigInteger(planId);
        ApiV1ValidateRequest req2 = new ApiV1ValidateRequest.Builder(user.ssoToken(), "SSO", mid.getId())
                .setMerchantKey(mid.getKey())
                .setPlanId(intplanid)
                .setItems(null)
                .setPaymentDetails(
                        new PaymentDetails()
                                .setTotalTransactionAmount(transactionAmount)
                                .setCardBin(RUPAY_CC_CARD_NO.substring(0,9))
                )
                .setGenerateTokenForIntent(true)
                .setPrice(transactionAmount)
                .setCacheCardToken(RUPAY_COFT_TOKEN)
                .setSubventionAmount(transactionAmount)
                .build();
        ApiV1Validate api2 = new ApiV1Validate(mid.getId(), req2);
        Response r2 = api2.execute();
        r2.then()
                .spec(SUCCESS_RESPONSE);
        JsonPath jsonPath2 = r2.jsonPath();
//        String logs2 = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,"AFFORDABILITY_PLATFORM_DISCOVERY");
//        Assertions.assertThat(logs2).contains("/ads/v2/offer/apply");
        double payableAmount = Double.valueOf(jsonPath2.getString("body.finalTransactionAmount"));
        TxnAmount txnAmount = new TxnAmount(String.valueOf(payableAmount));
        String emiSubventionToken = jsonPath2.getString("body.emiSubventionToken");
        String pgPlanId = jsonPath2.getString("body.pgPlanId");

        String orderId = CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), mid)
                .setTxnValue(transactionAmount)
                .setPayableAmount(txnAmount)
                .setOrderId(orderId)
                .setEmiSubventionToken(emiSubventionToken)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CardTokenInfo cardTokenInfo = new CardTokenInfo();
        cardTokenInfo.setCardToken(RUPAY_COFT_TOKEN);
        cardTokenInfo.setTokenExpiry(expiry);
        cardTokenInfo.setCardSuffix("0001");
        cardTokenInfo.setTavv("AgAAAAAKh8QqrzAABFcgm0AAAA=");
        cardTokenInfo.setPanUniqueReference("V0010013021361288827541727409");
        cardTokenInfo.setTokenType("ALTERNATE");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(),"TXN_TOKEN",txnToken,initTxnDTO.orderFromBody().toString(),txnAmount.getValue())
                .setPaymentMode("EMI")
                .setCardInfo("||618|")
                .setAuthMode("otp")
                .setcardTokenInfo(cardTokenInfo)
                .setChannelCode("RUPAY")
                .setTxnAmount(null)
                .setEmiType("CREDIT_CARD")
                .setPlanId(pgPlanId)
                .build();
        String jsonForm = QRHelper.executeProcessTransactionV1(processTxnV1Request);
        Assertions.assertThat(jsonForm).contains("\"resultStatus\":\"F\"");
        Assertions.assertThat(jsonForm).contains("\"resultCode\":\"0001\"");
        Assertions.assertThat(jsonForm).contains("\"resultMsg\":\"Alt ID Transaction via Token for Rupay is not Supported\"");
    }

    @Owner(MEHUL_GUPTA)
    @Test(description="Custom Checkout with VISA Alt Token , Old Apis migrated to new flow with PAR config enable - EMI Subvention Applied, Amount Based")
    public void testCustomCheckoutOldFlow_EMI_VISA_ALT_PAR_ENABLE() throws Exception {
        User user=userManager.getForRead(Label.BASIC);
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_ENABLE;
        String custid=user.custId();
        String transactionAmount="800";
        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder()
                .setMid(mid.getId())
                .setMerchantKey(mid.getKey())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .setItems(null)
                .setSubventionAmount(transactionAmount)
                .setPrice(transactionAmount)
                .build();
        ApiV1Tenure api = new ApiV1Tenure(mid.getId(), req);
        Response r = api.execute();
        JsonPath tenureResp = r.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.planDetails.emiType", hasItems("SUBVENTION"))
                .extract().jsonPath();

        String planId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId");
        BigInteger intplanid= new BigInteger(planId);
        ApiV1ValidateRequest req2 = new ApiV1ValidateRequest.Builder(user.ssoToken(), "SSO", mid.getId())
                .setMerchantKey(mid.getKey())
                .setPlanId(intplanid)
                .setItems(null)
                .setPaymentDetails(
                        new PaymentDetails()
                                .setTotalTransactionAmount(transactionAmount)
                                .setCardBin(VISA_CC_CARD_NO.substring(0,9))
                )
                .setGenerateTokenForIntent(true)
                .setPrice(transactionAmount)
                .setCacheCardToken(VISA_ALT_TOKEN)
                .setSubventionAmount(transactionAmount)
                .build();
        ApiV1Validate api2 = new ApiV1Validate(mid.getId(), req2);
        Response r2 = api2.execute();
        r2.then()
                .spec(SUCCESS_RESPONSE);
        JsonPath jsonPath2 = r2.jsonPath();
//        String logs2 = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,"AFFORDABILITY_PLATFORM_DISCOVERY");
//        Assertions.assertThat(logs2).contains("/ads/v2/offer/apply");
        double payableAmount = Double.valueOf(jsonPath2.getString("body.finalTransactionAmount"));
        TxnAmount txnAmount = new TxnAmount(String.valueOf(payableAmount));
        String emiSubventionToken = jsonPath2.getString("body.emiSubventionToken");
        String pgPlanId = jsonPath2.getString("body.pgPlanId");

        String orderId = CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), mid)
                .setTxnValue(transactionAmount)
                .setPayableAmount(txnAmount)
                .setOrderId(orderId)
                .setEmiSubventionToken(emiSubventionToken)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CardTokenInfo cardTokenInfo = new CardTokenInfo();
        cardTokenInfo.setCardToken(VISA_ALT_TOKEN);
        cardTokenInfo.setTokenExpiry(expiry);
        cardTokenInfo.setCardSuffix("2363");
        cardTokenInfo.setTavv("AgAAAAAKh8QqrzAABFcgm0AAAA=");
        cardTokenInfo.setPanUniqueReference("V001001402220600348758669991");
        cardTokenInfo.setTokenType("ALTERNATE");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(),"TXN_TOKEN",txnToken,initTxnDTO.orderFromBody().toString(),txnAmount.getValue())
                .setPaymentMode("EMI")
                .setCardInfo("||618|")
                .setAuthMode("otp")
                .setcardTokenInfo(cardTokenInfo)
                .setChannelCode("VISA")
                .setTxnAmount(null)
                .setEmiType("CREDIT_CARD")
                .setPlanId(pgPlanId)
                .build();
        String jsonForm = QRHelper.executeProcessTransactionV1(processTxnV1Request);
        Assertions.assertThat(jsonForm).contains("\"resultStatus\":\"S\"");
        String logsResponse = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody().toString(), "/ats/v2/order/checkout", "RESPONSE");
        Assertions.assertThat(logsResponse).contains("Success");
        Assertions.assertThat(logsResponse).contains("\"isOfferCheckoutRequired\":true");
        Assertions.assertThat(logsResponse).contains("\"isSubventionOfferApplied\":true");
        Assertions.assertThat(logsResponse).contains("\"isBankOfferApplied\":false");
        //System.out.println("Checkout -> "+logsResponse);
        nativePlusHoldpayPage.
                launch(LocalConfig.MOCK_HOST + JSON_POST_URL).
                fillAndSubmitJsonForm(jsonForm);
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(mid.getId())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateTxnAmount(String.valueOf(payableAmount))
                .AssertAll();
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).contains(pgPlanId);
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).isNull();
    }


    @Owner(MEHUL_GUPTA)
    @Test(description="Custom Checkout with RUPAY Alt Token , Old Apis migrated to new flow with PAR config enable - EMI Subvention Applied, Amount Based")
    public void testCustomCheckoutOldFlow_EMI_RUPAY_ALT_PAR_DISABLE() throws Exception {
        User user=userManager.getForRead(Label.BASIC);
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_DISABLE;
        String custid=user.custId();
        String transactionAmount="800";
        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder()
                .setMid(mid.getId())
                .setMerchantKey(mid.getKey())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .setItems(null)
                .setSubventionAmount(transactionAmount)
                .setPrice(transactionAmount)
                .build();
        ApiV1Tenure api = new ApiV1Tenure(mid.getId(), req);
        Response r = api.execute();
        JsonPath tenureResp = r.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.planDetails.emiType", hasItems("SUBVENTION"))
                .extract().jsonPath();

        String planId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId");
        BigInteger intplanid= new BigInteger(planId);
        ApiV1ValidateRequest req2 = new ApiV1ValidateRequest.Builder(user.ssoToken(), "SSO", mid.getId())
                .setMerchantKey(mid.getKey())
                .setPlanId(intplanid)
                .setItems(null)
                .setPaymentDetails(
                        new PaymentDetails()
                                .setTotalTransactionAmount(transactionAmount)
                                .setCardBin(RUPAY_CC_CARD_NO.substring(0,9))
                )
                .setGenerateTokenForIntent(true)
                .setPrice(transactionAmount)
                .setCacheCardToken(RUPAY_COFT_TOKEN)
                .setSubventionAmount(transactionAmount)
                .build();
        ApiV1Validate api2 = new ApiV1Validate(mid.getId(), req2);
        Response r2 = api2.execute();
        r2.then()
                .spec(SUCCESS_RESPONSE);
        JsonPath jsonPath2 = r2.jsonPath();
//        String logs2 = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,"AFFORDABILITY_PLATFORM_DISCOVERY");
//        Assertions.assertThat(logs2).contains("/ads/v2/offer/apply");
        double payableAmount = Double.valueOf(jsonPath2.getString("body.finalTransactionAmount"));
        TxnAmount txnAmount = new TxnAmount(String.valueOf(payableAmount));
        String emiSubventionToken = jsonPath2.getString("body.emiSubventionToken");
        String pgPlanId = jsonPath2.getString("body.pgPlanId");

        String orderId = CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), mid)
                .setTxnValue(transactionAmount)
                .setPayableAmount(txnAmount)
                .setOrderId(orderId)
                .setEmiSubventionToken(emiSubventionToken)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CardTokenInfo cardTokenInfo = new CardTokenInfo();
        cardTokenInfo.setCardToken(RUPAY_COFT_TOKEN);
        cardTokenInfo.setTokenExpiry(expiry);
        cardTokenInfo.setCardSuffix("0001");
        cardTokenInfo.setTavv("AgAAAAAKh8QqrzAABFcgm0AAAA=");
        cardTokenInfo.setPanUniqueReference("V0010013021361288827541727409");
        cardTokenInfo.setTokenType("ALTERNATE");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(),"TXN_TOKEN",txnToken,initTxnDTO.orderFromBody().toString(),txnAmount.getValue())
                .setPaymentMode("EMI")
                .setCardInfo("||618|")
                .setAuthMode("otp")
                .setcardTokenInfo(cardTokenInfo)
                .setChannelCode("RUPAY")
                .setTxnAmount(null)
                .setEmiType("CREDIT_CARD")
                .setPlanId(pgPlanId)
                .build();
        String jsonForm = QRHelper.executeProcessTransactionV1(processTxnV1Request);
        JsonPath jsonPath1 = new JsonPath(jsonForm);
        Assertions.assertThat(jsonPath1.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(jsonPath1.getString("body.resultInfo.resultCode")).isEqualTo("0001");
        Assertions.assertThat(jsonPath1.getString("body.resultInfo.resultMsg")).isEqualTo("Alt ID Transaction via Token for Rupay is not Supported");
//        Assertions.assertThat(jsonForm).contains("\"resultStatus\":\"F\"");
//        Assertions.assertThat(jsonForm).contains("\"resultCode\":\"0001\"");
//        Assertions.assertThat(jsonForm).contains("\"resultMsg\":\"Alt ID Transaction via Token for Rupay is not Supported\"");
    }

//
//    @Owner(MEHUL_GUPTA)
//    @Test(description="Custom Checkout for Multi Item , Old Apis migrated to new flow - Only BO CC Txn")
//    public void testCustomCheckoutOldFlow_MultiItem_NoSubvention_And_BO_CCTxn() throws Exception {
//        User user=userManager.getForRead(Label.BASIC);
//        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_DISABLE;
//        String custid=user.custId();
//        String orderId = CommonHelpers.generateOrderId();
//        String transactionAmount="2200";
//        ArrayList categoryIds1 = new ArrayList();
//        categoryIds1.add("6226");
//        ProductDetail productDetail1= new ProductDetail("123047","18084",categoryIds1);
//        Item item1= new Item("Item001_" + orderId,1100,productDetail1);
//
//        ArrayList categoryIds2 = new ArrayList();
//        categoryIds2.add("6226");
//        ProductDetail productDetail2= new ProductDetail("123047","18084",categoryIds2);
//        Item item2= new Item("Item002_" + orderId,1100,productDetail2);
//
//        CartDetails cartDetails= new CartDetails(Arrays.asList(item1,item2));
//        PaymentOption paymentOption= new PaymentOption(transactionAmount,"CREDIT_CARD",null,"HDFC",VISA_CC_CARD_NO,null);
//        ApplyPromoV2DTO applyPromoV2DTO = new ApplyPromoV2DTO.Builder()
//                .setChannelId("WEB")
//                .setRequestId(orderId)
//                .setToken(user.ssoToken())
//                .setTokenType("SSO")
//                .setCustId(custid)
//                .setPaymentOptions(Arrays.asList(paymentOption))
//                .setMid(mid.getId())
//                .setCartDetails(cartDetails)
//                .setTotalTransactionAmount(transactionAmount)
//                .build();
//        JsonPath jsonPath= ApplyPromoV2Api.applyPromoV2(applyPromoV2DTO, mid,"");
//        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
//        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
//        Assertions.assertThat(jsonPath.getString("body.paymentOffer.offerBreakup[0].promocodeApplied")).isNotNull();
//        String cashback = jsonPath.getString("body.paymentOffer.totalCashbackAmount");
//        String discount = jsonPath.getString("body.paymentOffer.totalInstantDiscount");
////        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,"orderId","AFFORDABILITY_PLATFORM_DISCOVERY");
////        Assertions.assertThat(logs).contains("/ads/v2/offer/apply");
//        HashMap<String, Object> paymentOffersObject= jsonPath.get("body.paymentOffer");
//        PaymentOffersApplied paymentOffersApplied= new PaymentOffersApplied(paymentOffersObject);
//
//        String orderId2 = CommonHelpers.generateOrderId();
//        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), mid)
//                .setTxnValue(transactionAmount)
//                .setOrderId(orderId2)
//                .setPaymentOffersApplied(paymentOffersApplied)
//                .build();
//        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
//        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(),"TXN_TOKEN",txnToken,initTxnDTO.orderFromBody().toString(),null)
//                .setPaymentMode("CREDIT_CARD")
//                .setCardInfo("|" + VISA_CC_CARD_NO + "|618|"+expiry)
//                .setAuthMode("otp")
//                .setChannelCode("VISA")
//                .build();
//        String jsonForm = QRHelper.executeProcessTransactionV1(processTxnV1Request);
//        Assertions.assertThat(jsonForm).contains("\"resultStatus\":\"S\"");
//        String logsResponse = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody().toString(), "/ats/v2/order/checkout", "RESPONSE");
//        Assertions.assertThat(logsResponse).contains("Success");
//        Assertions.assertThat(logsResponse).contains("\"isOfferCheckoutRequired\":true");
//        Assertions.assertThat(logsResponse).contains("\"isBankOfferApplied\":true");
//        nativePlusHoldpayPage.
//                launch(LocalConfig.MOCK_HOST + JSON_POST_URL).
//                fillAndSubmitJsonForm(jsonForm);
//        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
//        txnStatus.executeUntilNotPending();
//        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
//                .validateOrderid(initTxnDTO.orderFromBody())
//                .validateStatus("TXN_SUCCESS")
//                .validateTxnType("SALE")
//                .validateRespCode("01")
//                .validateRespMsg("Txn Success")
//                .validateMid(mid.getId())
//                .validatePaymentMode("CC")
//                .validateRefundAmnt("0.00")
//                .validateTxnDate(new Date())
//                .validateTxnAmount(String.valueOf(Double.parseDouble(transactionAmount)-Double.parseDouble(discount)))
//                .AssertAll();
//        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).isNull();
//        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).contains("savings");
//    }

    @Owner(MEHUL_GUPTA)
    @Test(description="Custom Checkout for Single Item , Old Apis migrated to new flow - Only BO CC Txn")
    public void testCustomCheckoutOldFlow_SingleItem_NoSubvention_And_BO_CCTxn() throws Exception {
        User user=userManager.getForRead(Label.BASIC);
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_DISABLE;
        String custid=user.custId();
        String orderId = CommonHelpers.generateOrderId();
        String transactionAmount="1100";
        ArrayList categoryIds1 = new ArrayList();
        categoryIds1.add("6226");
        ProductDetail productDetail1= new ProductDetail("123047","18084",categoryIds1);
        Item item1= new Item("Item001_" + orderId,1100,productDetail1);
        CartDetails cartDetails= new CartDetails(Arrays.asList(item1));
        PaymentOption paymentOption= new PaymentOption(transactionAmount,"CREDIT_CARD",null,"HDFC",RUPAY_CC_CARD_NO,null);
        ApplyPromoV2DTO applyPromoV2DTO = new ApplyPromoV2DTO.Builder()
                .setChannelId("WEB")
                .setRequestId(orderId)
                .setToken(user.ssoToken())
                .setTokenType("SSO")
                .setCustId(custid)
                .setPaymentOptions(Arrays.asList(paymentOption))
                .setMid(mid.getId())
                .setCartDetails(cartDetails)
                .setTotalTransactionAmount(transactionAmount)
                .build();
        JsonPath jsonPath= ApplyPromoV2Api.applyPromoV2(applyPromoV2DTO, mid,"");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.paymentOffer.offerBreakup[0].promocodeApplied")).isNotNull();
        String cashback = jsonPath.getString("body.paymentOffer.totalCashbackAmount");
        String discount = jsonPath.getString("body.paymentOffer.totalInstantDiscount");
//        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,"orderId","AFFORDABILITY_PLATFORM_DISCOVERY");
//        Assertions.assertThat(logs).contains("/ads/v2/offer/apply");
        HashMap<String, Object> paymentOffersObject= jsonPath.get("body.paymentOffer");
        PaymentOffersApplied paymentOffersApplied= new PaymentOffersApplied(paymentOffersObject);

        String orderId2 = CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), mid)
                .setTxnValue(transactionAmount)
                .setOrderId(orderId2)
                .setPaymentOffersApplied(paymentOffersApplied)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(),"TXN_TOKEN",txnToken,initTxnDTO.orderFromBody().toString(),null)
                .setPaymentMode("CREDIT_CARD")
                .setCardInfo("|" + RUPAY_CC_CARD_NO + "|618|"+expiry)
                .setAuthMode("otp")
                .build();
        String jsonForm = QRHelper.executeProcessTransactionV1(processTxnV1Request);

        //PGP-58730 Test originalCardHash being sent in extendInfo and channelInfo in COP/CO&P and hash is created using Card Number
        String payLog = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"ACQUIRING_PAY_ORDER","REQUEST");
        String cleanedJson = payLog.strip()
                .replace("\\\"", "\"") // Replace escaped quotes
                .replace("\"{", "{")   // Fix object boundaries
                .replace("}\"", "}");  // Fix object boundaries
        JsonPath acqLogs = new JsonPath(cleanedJson);
        Assertions.assertThat(acqLogs.getString("REQUEST.extendInfo")).contains("originalCardHash:9fff5b365aa8e3934355eaccf00604a26833423301424bdb34914648855a214c");
        Assertions.assertThat(acqLogs.getString("REQUEST.paymentInfo.paymentBillOption[0].channelInfo")).contains("originalCardHash:9fff5b365aa8e3934355eaccf00604a26833423301424bdb34914648855a214c");

        Assertions.assertThat(jsonForm).contains("\"resultStatus\":\"S\"");
        String logsResponse = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody().toString(), "/ats/v2/order/checkout", "RESPONSE");
        Assertions.assertThat(logsResponse).contains("Success");
        Assertions.assertThat(logsResponse).contains("\"isOfferCheckoutRequired\":true");
        Assertions.assertThat(logsResponse).contains("\"isBankOfferApplied\":true");
        nativePlusHoldpayPage.
                launch(LocalConfig.MOCK_HOST + JSON_POST_URL).
                fillAndSubmitJsonForm(jsonForm);
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(mid.getId())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateTxnAmount(String.valueOf(Double.parseDouble(transactionAmount)-Double.parseDouble(discount)))
                .AssertAll();
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).isNull();
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).contains("savings");
    }

//
//    @Owner(MEHUL_GUPTA)
//    @Test(description="Custom Checkout for Multi Item , Old Apis migrated to new flow - Only BO Standard EMI Txn")
//    public void testCustomCheckoutOldFlow_MultiItem_NoSubvention_And_BO_EMITxn() throws Exception {
//        User user=userManager.getForRead(Label.BASIC);
//        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_DISABLE;
//        String custid=user.custId();
//        String orderId = CommonHelpers.generateOrderId();
//        String transactionAmount="2200";
//        ArrayList categoryIds1 = new ArrayList();
//        categoryIds1.add("6226");
//        ProductDetail productDetail1= new ProductDetail("123047","18084",categoryIds1);
//        Item item1= new Item("Item001_" + orderId,1100,productDetail1);
//        ArrayList categoryIds2 = new ArrayList();
//        categoryIds2.add("78225");
//        ProductDetail productDetail2= new ProductDetail("55005","18260",categoryIds2);
//        Item item2= new Item("Item002_" + orderId,1100,productDetail2);
//        CartDetails cartDetails= new CartDetails(Arrays.asList(item1,item2));
//        PaymentOption paymentOption= new PaymentOption(transactionAmount,"EMI",null,"HDFC",VISA_CC_CARD_NO,"3");
//        ApplyPromoV2DTO applyPromoV2DTO = new ApplyPromoV2DTO.Builder()
//                .setChannelId("WEB")
//                .setRequestId(orderId)
//                .setToken(user.ssoToken())
//                .setTokenType("SSO")
//                .setCustId(custid)
//                .setPaymentOptions(Arrays.asList(paymentOption))
//                .setMid(mid.getId())
//                .setCartDetails(cartDetails)
//                .setTotalTransactionAmount(transactionAmount)
//                .build();
//        JsonPath jsonPath= ApplyPromoV2Api.applyPromoV2(applyPromoV2DTO, mid,"");
//        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
//        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
//        Assertions.assertThat(jsonPath.getString("body.paymentOffer.offerBreakup[0].promocodeApplied")).isNotNull();
////        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,"orderId","AFFORDABILITY_PLATFORM_DISCOVERY");
////        Assertions.assertThat(logs).contains("/ads/v2/offer/apply");
//        HashMap<String, Object> paymentOffersObject= jsonPath.get("body.paymentOffer");
//        PaymentOffersApplied paymentOffersApplied= new PaymentOffersApplied(paymentOffersObject);
//
//        String orderId2 = CommonHelpers.generateOrderId();
//        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), mid)
//                .setTxnValue(transactionAmount)
//                .setOrderId(orderId2)
//                .setPaymentOffersApplied(paymentOffersApplied)
//                .build();
//        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
//        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(),"TXN_TOKEN",txnToken,initTxnDTO.orderFromBody().toString(),null)
//                .setPaymentMode("EMI")
//                .setCardInfo("|" + VISA_CC_CARD_NO + "|618|"+expiry)
//                .setAuthMode("otp")
//                .setChannelCode("VISA")
//                .setEmiType("CREDIT_CARD")
//                .setPlanId("HDFC|3")
//                .build();
//        String jsonForm = QRHelper.executeProcessTransactionV1(processTxnV1Request);
//        Assertions.assertThat(jsonForm).contains("\"resultStatus\":\"S\"");
//        String logsResponse = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody().toString(), "/ats/v2/order/checkout", "RESPONSE");
//        Assertions.assertThat(logsResponse).contains("Success");
//        Assertions.assertThat(logsResponse).contains("\"isOfferCheckoutRequired\":true");
//        Assertions.assertThat(logsResponse).contains("\"isBankOfferApplied\":true");
//        nativePlusHoldpayPage.
//                launch(LocalConfig.MOCK_HOST + JSON_POST_URL).
//                fillAndSubmitJsonForm(jsonForm);
//        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
//        txnStatus.executeUntilNotPending();
//        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
//                .validateOrderid(initTxnDTO.orderFromBody())
//                .validateStatus("TXN_SUCCESS")
//                .validateTxnType("SALE")
//                .validateRespCode("01")
//                .validateRespMsg("Txn Success")
//                .validateMid(mid.getId())
//                .validatePaymentMode("EMI")
//                .validateRefundAmnt("0.00")
//                .validateTxnDate(new Date())
//                .validateTxnAmount(String.valueOf(transactionAmount))
//                .AssertAll();
//        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).isNull();
//        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).contains("savings");
//    }
//
//    @Owner(MEHUL_GUPTA)
//    @Test(description="Custom Checkout for Single Item , Old Apis migrated to new flow - Only BO Standard EMI Txn")
//    public void testCustomCheckoutOldFlow_SingleItem_NoSubvention_And_BO_EMITxn() throws Exception {
//        User user=userManager.getForRead(Label.BASIC);
//        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_DISABLE;
//        String custid=user.custId();
//        String orderId = CommonHelpers.generateOrderId();
//        String transactionAmount="1100";
//        ArrayList categoryIds1 = new ArrayList();
//        categoryIds1.add("6226");
//        ProductDetail productDetail1= new ProductDetail("123047","18084",categoryIds1);
//        Item item1= new Item("Item001_" + orderId,1100,productDetail1);
//        CartDetails cartDetails= new CartDetails(Arrays.asList(item1));
//        PaymentOption paymentOption= new PaymentOption(transactionAmount,"EMI",null,"HDFC",VISA_CC_CARD_NO,"3");
//        ApplyPromoV2DTO applyPromoV2DTO = new ApplyPromoV2DTO.Builder()
//                .setChannelId("WEB")
//                .setRequestId(orderId)
//                .setToken(user.ssoToken())
//                .setTokenType("SSO")
//                .setCustId(custid)
//                .setPaymentOptions(Arrays.asList(paymentOption))
//                .setMid(mid.getId())
//                .setCartDetails(cartDetails)
//                .setTotalTransactionAmount(transactionAmount)
//                .build();
//        JsonPath jsonPath= ApplyPromoV2Api.applyPromoV2(applyPromoV2DTO, mid,"");
//        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
//        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
//        Assertions.assertThat(jsonPath.getString("body.paymentOffer.offerBreakup[0].promocodeApplied")).isNotNull();
////        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,"orderId","AFFORDABILITY_PLATFORM_DISCOVERY");
////        Assertions.assertThat(logs).contains("/ads/v2/offer/apply");
//        HashMap<String, Object> paymentOffersObject= jsonPath.get("body.paymentOffer");
//        PaymentOffersApplied paymentOffersApplied= new PaymentOffersApplied(paymentOffersObject);
//
//        String orderId2 = CommonHelpers.generateOrderId();
//        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), mid)
//                .setTxnValue(transactionAmount)
//                .setOrderId(orderId2)
//                .setPaymentOffersApplied(paymentOffersApplied)
//                .build();
//        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
//        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(),"TXN_TOKEN",txnToken,initTxnDTO.orderFromBody().toString(),null)
//                .setPaymentMode("EMI")
//                .setCardInfo("|" + VISA_CC_CARD_NO + "|618|"+expiry)
//                .setAuthMode("otp")
//                .setChannelCode("VISA")
//                .setEmiType("CREDIT_CARD")
//                .setPlanId("HDFC|3")
//                .build();
//        String jsonForm = QRHelper.executeProcessTransactionV1(processTxnV1Request);
//        Assertions.assertThat(jsonForm).contains("\"resultStatus\":\"S\"");
//        String logsResponse = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody().toString(), "/ats/v2/order/checkout", "RESPONSE");
//        Assertions.assertThat(logsResponse).contains("Success");
//        Assertions.assertThat(logsResponse).contains("\"isOfferCheckoutRequired\":true");
//        Assertions.assertThat(logsResponse).contains("\"isBankOfferApplied\":true");
//        nativePlusHoldpayPage.
//                launch(LocalConfig.MOCK_HOST + JSON_POST_URL).
//                fillAndSubmitJsonForm(jsonForm);
//        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
//        txnStatus.executeUntilNotPending();
//        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
//                .validateOrderid(initTxnDTO.orderFromBody())
//                .validateStatus("TXN_SUCCESS")
//                .validateTxnType("SALE")
//                .validateRespCode("01")
//                .validateRespMsg("Txn Success")
//                .validateMid(mid.getId())
//                .validatePaymentMode("EMI")
//                .validateRefundAmnt("0.00")
//                .validateTxnDate(new Date())
//                .validateTxnAmount(String.valueOf(transactionAmount))
//                .AssertAll();
//        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).isNull();
//        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).contains("savings");
//    }
//
//
//    @Owner(MEHUL_GUPTA)
//    @Test(description="Custom Checkout for Multi Item , Old Apis migrated to new flow - Only Subvention Applied ")
//    public void testCustomCheckoutOldFlow_MultiItem_Subvention_And_NoBO_EMITxn() throws Exception {
//        User user=userManager.getForRead(Label.BASIC);
//        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_DISABLE_MASTER;
//        String custid=user.custId();
//        String transactionAmount="2200";
//        String orderId = CommonHelpers.generateOrderId();
//
//        List<String> category1 =new ArrayList<>();
//        category1.add("6223");
//        com.paytm.dto.emiSubvention.ApiV1Banks.request.Item item1 =new com.paytm.dto.emiSubvention.ApiV1Banks.request.Item();
//        item1.setProductId("123045");
//        item1.setCategoryList(category1);
//        item1.setModel("123045");
//        item1.setBrandId("18084");
//        item1.setPrice(1100.0);
//        item1.setId("Item001_" + orderId);
//        item1.setOriginalPrice(1100.0);
//
//        List<String> category2 =new ArrayList<>();
//        category2.add("16225");
//        com.paytm.dto.emiSubvention.ApiV1Banks.request.Item item2 =new com.paytm.dto.emiSubvention.ApiV1Banks.request.Item();
//        item2.setProductId("123046");
//        item2.setCategoryList(category2);
//        item2.setModel("123046");
//        item2.setBrandId("18084");
//        item2.setPrice(1100.0);
//        item2.setId("Item002_" + orderId);
//        item2.setOriginalPrice(1100.0);
//
//        List<com.paytm.dto.emiSubvention.ApiV1Banks.request.Item> items = new ArrayList<>();
//        items.add(item1);
//        items.add(item2);
//
//        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder()
//                .setMid(mid.getId())
//                .setMerchantKey(mid.getKey())
//                .setTokenType("SSO")
//                .setToken(user.ssoToken())
//                .setFilters(new Filters()
//                        .setBankCode("HDFC")
//                        .setCardType("CREDIT_CARD"))
//                .setItems(items)
//                .setSubventionAmount(transactionAmount)
//                .setPrice(transactionAmount)
//                .setOriginalPrice(transactionAmount)
//                .build();
//        ApiV1Tenure api = new ApiV1Tenure(mid.getId(), req);
//        Response r = api.execute();
//        JsonPath tenureResp = r.then()
//                .spec(SUCCESS_RESPONSE)
//                .body("body.planDetails.emiType", hasItems("SUBVENTION"))
//                .extract().jsonPath();
//
//        String planId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId");
//        BigInteger intplanid= new BigInteger(planId);
//
//
//        com.paytm.dto.emiSubvention.ApiV1Validate.request.Item itemm1 =new com.paytm.dto.emiSubvention.ApiV1Validate.request.Item();
//        itemm1.setProductId("123045");
//        itemm1.setCategoryList(category1);
//        itemm1.setModel("123045");
//        itemm1.setBrandId("18084");
//        itemm1.setPrice(1100.0);
//        itemm1.setId("Item001_" + orderId);
//
//        com.paytm.dto.emiSubvention.ApiV1Validate.request.Item itemm2 =new com.paytm.dto.emiSubvention.ApiV1Validate.request.Item();
//        itemm2.setProductId("123046");
//        itemm2.setCategoryList(category2);
//        itemm2.setModel("123046");
//        itemm2.setBrandId("18084");
//        itemm2.setPrice(1100.0);
//        itemm2.setId("Item002_" + orderId);
//        List<com.paytm.dto.emiSubvention.ApiV1Validate.request.Item> items1 = new ArrayList<>();
//        items1.add(itemm1);
//        items1.add(itemm2);
//
//
//        ApiV1ValidateRequest req2 = new ApiV1ValidateRequest.Builder(user.ssoToken(), "SSO", mid.getId())
//                .setMerchantKey(mid.getKey())
//                .setPlanId(intplanid)
//                .setItems(items1)
//                .setPaymentDetails(
//                        new PaymentDetails()
//                                .setTotalTransactionAmount(transactionAmount)
//                                .setCardNumber("|"+VISA_CC_CARD_NO+"|618|"+expiry)
//                )
//                .setGenerateTokenForIntent(true)
//                .setPrice(transactionAmount)
//                .setSubventionAmount(transactionAmount)
//                .setOriginalPrice(transactionAmount)
//                .build();
//        ApiV1Validate api2 = new ApiV1Validate(mid.getId(), req2);
//        Response r2 = api2.execute();
//        r2.then()
//                .spec(SUCCESS_RESPONSE);
//        JsonPath jsonPath = r2.jsonPath();
////        String logs2 = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,"AFFORDABILITY_PLATFORM_DISCOVERY");
////        Assertions.assertThat(logs2).contains("/ads/v2/offer/apply");
//        double payableAmount = Double.valueOf(jsonPath.getString("body.finalTransactionAmount"));
//        TxnAmount txnAmount = new TxnAmount(String.valueOf(payableAmount));
//        String emiSubventionToken = jsonPath.getString("body.emiSubventionToken");
//        String pgPlanId = jsonPath.getString("body.pgPlanId");
//
//        String orderId2 = CommonHelpers.generateOrderId();
//        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), mid)
//                .setTxnValue(transactionAmount)
//                .setPayableAmount(txnAmount)
//                .setOrderId(orderId2)
//                .setEmiSubventionToken(emiSubventionToken)
//                .build();
//        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
//        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(),"TXN_TOKEN",txnToken,initTxnDTO.orderFromBody().toString(),txnAmount.getValue())
//                .setPaymentMode("EMI")
//                .setCardInfo("|" + VISA_CC_CARD_NO + "|618|"+expiry)
//                .setAuthMode("otp")
//                .setTxnAmount(null)
//                .setEmiType("CREDIT_CARD")
//                .setPlanId(pgPlanId)
//                .build();
//        String jsonForm = QRHelper.executeProcessTransactionV1(processTxnV1Request);
//        Assertions.assertThat(jsonForm).contains("\"resultStatus\":\"S\"");
//        String logsResponse = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody().toString(), "/ats/v2/order/checkout", "RESPONSE");
//        Assertions.assertThat(logsResponse).contains("Success");
//        Assertions.assertThat(logsResponse).contains("\"isOfferCheckoutRequired\":true");
//        Assertions.assertThat(logsResponse).contains("\"isSubventionOfferApplied\":true");
//        Assertions.assertThat(logsResponse).contains("\"isBankOfferApplied\":false");
//        //System.out.println("Checkout -> "+logsResponse);
//        nativePlusHoldpayPage.
//                launch(LocalConfig.MOCK_HOST + JSON_POST_URL).
//                fillAndSubmitJsonForm(jsonForm);
//        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
//        txnStatus.executeUntilNotPending();
//        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
//                .validateOrderid(initTxnDTO.orderFromBody())
//                .validateStatus("TXN_SUCCESS")
//                .validateTxnType("SALE")
//                .validateRespCode("01")
//                .validateRespMsg("Txn Success")
//                .validateMid(mid.getId())
//                .validatePaymentMode("EMI")
//                .validateRefundAmnt("0.00")
//                .validateTxnDate(new Date())
//                .validateTxnAmount(String.valueOf(payableAmount))
//                .AssertAll();
//        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).contains(pgPlanId);
//        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).isNull();
//    }
//
//    @Owner(MEHUL_GUPTA)
//    @Test(description="Custom Checkout for Single Item , Old Apis migrated to new flow - Only Subvention Applied ")
//    public void testCustomCheckoutOldFlow_SingleItem_Subvention_And_NoBO_EMITxn() throws Exception {
//        User user=userManager.getForRead(Label.BASIC);
//        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_DISABLE_MASTER;
//        String custid=user.custId();
//        String transactionAmount="1100";
//        String orderId = CommonHelpers.generateOrderId();
//
//        List<String> category1 =new ArrayList<>();
//        category1.add("6223");
//        com.paytm.dto.emiSubvention.ApiV1Banks.request.Item item1 =new com.paytm.dto.emiSubvention.ApiV1Banks.request.Item();
//        item1.setProductId("123045");
//        item1.setCategoryList(category1);
//        item1.setModel("123045");
//        item1.setBrandId("18084");
//        item1.setPrice(1100.0);
//        item1.setId("Item001_" + orderId);
//
//        List<com.paytm.dto.emiSubvention.ApiV1Banks.request.Item> items = new ArrayList<>();
//        items.add(item1);
//
//        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder()
//                .setMid(mid.getId())
//                .setMerchantKey(mid.getKey())
//                .setTokenType("SSO")
//                .setToken(user.ssoToken())
//                .setFilters(new Filters()
//                        .setBankCode("HDFC")
//                        .setCardType("CREDIT_CARD"))
//                .setItems(items)
//                //.setSubventionAmount(transactionAmount)
//                .setPrice(transactionAmount)
//                .build();
//        ApiV1Tenure api = new ApiV1Tenure(mid.getId(), req);
//        Response r = api.execute();
//        JsonPath tenureResp = r.then()
//                .spec(SUCCESS_RESPONSE)
//                .body("body.planDetails.emiType", hasItems("SUBVENTION"))
//                .extract().jsonPath();
//
//        String planId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId");
//        BigInteger intplanid= new BigInteger(planId);
//
//
//        com.paytm.dto.emiSubvention.ApiV1Validate.request.Item itemm1 =new com.paytm.dto.emiSubvention.ApiV1Validate.request.Item();
//        itemm1.setProductId("123045");
//        itemm1.setCategoryList(category1);
//        itemm1.setModel("123045");
//        itemm1.setBrandId("18084");
//        itemm1.setPrice(1100.0);
//        itemm1.setId("Item001_" + orderId);
//
//        List<com.paytm.dto.emiSubvention.ApiV1Validate.request.Item> items1 = new ArrayList<>();
//        items1.add(itemm1);
//
//        ApiV1ValidateRequest req2 = new ApiV1ValidateRequest.Builder(user.ssoToken(), "SSO", mid.getId())
//                .setMerchantKey(mid.getKey())
//                .setPlanId(intplanid)
//                .setItems(items1)
//                .setPaymentDetails(
//                        new PaymentDetails()
//                                .setTotalTransactionAmount(transactionAmount)
//                                .setCardNumber("|"+VISA_CC_CARD_NO+"|618|"+expiry)
//                )
//                .setGenerateTokenForIntent(true)
//                .setPrice(transactionAmount)
//                //.setSubventionAmount(transactionAmount)
//                .build();
//        ApiV1Validate api2 = new ApiV1Validate(mid.getId(), req2);
//        Response r2 = api2.execute();
//        r2.then()
//                .spec(SUCCESS_RESPONSE);
//        JsonPath jsonPath = r2.jsonPath();
////        String logs2 = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,"AFFORDABILITY_PLATFORM_DISCOVERY");
////        Assertions.assertThat(logs2).contains("/ads/v2/offer/apply");
//        double payableAmount = Double.valueOf(jsonPath.getString("body.finalTransactionAmount"));
//        TxnAmount txnAmount = new TxnAmount(String.valueOf(payableAmount));
//        String emiSubventionToken = jsonPath.getString("body.emiSubventionToken");
//        String pgPlanId = jsonPath.getString("body.pgPlanId");
//
//        String orderId2 = CommonHelpers.generateOrderId();
//        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), mid)
//                .setTxnValue(transactionAmount)
//                .setPayableAmount(txnAmount)
//                .setOrderId(orderId2)
//                .setEmiSubventionToken(emiSubventionToken)
//                .build();
//        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
//        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(),"TXN_TOKEN",txnToken,initTxnDTO.orderFromBody().toString(),txnAmount.getValue())
//                .setPaymentMode("EMI")
//                .setCardInfo("|" + VISA_CC_CARD_NO + "|618|"+expiry)
//                .setAuthMode("otp")
//                .setTxnAmount(null)
//                .setEmiType("CREDIT_CARD")
//                .setPlanId(pgPlanId)
//                .build();
//        String jsonForm = QRHelper.executeProcessTransactionV1(processTxnV1Request);
//        Assertions.assertThat(jsonForm).contains("\"resultStatus\":\"S\"");
//        String logsResponse = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody().toString(), "/ats/v2/order/checkout", "RESPONSE");
//        Assertions.assertThat(logsResponse).contains("Success");
//        Assertions.assertThat(logsResponse).contains("\"isOfferCheckoutRequired\":true");
//        Assertions.assertThat(logsResponse).contains("\"isSubventionOfferApplied\":true");
//        Assertions.assertThat(logsResponse).contains("\"isBankOfferApplied\":false");
//        //System.out.println("Checkout -> "+logsResponse);
//        nativePlusHoldpayPage.
//                launch(LocalConfig.MOCK_HOST + JSON_POST_URL).
//                fillAndSubmitJsonForm(jsonForm);
//        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
//        txnStatus.executeUntilNotPending();
//        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
//                .validateOrderid(initTxnDTO.orderFromBody())
//                .validateStatus("TXN_SUCCESS")
//                .validateTxnType("SALE")
//                .validateRespCode("01")
//                .validateRespMsg("Txn Success")
//                .validateMid(mid.getId())
//                .validatePaymentMode("EMI")
//                .validateRefundAmnt("0.00")
//                .validateTxnDate(new Date())
//                .validateTxnAmount(String.valueOf(payableAmount))
//                .AssertAll();
//        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).contains(pgPlanId);
//        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).isNull();
//    }


    @Owner(MEHUL_GUPTA)
    @Test(description="Custom Checkout for 6 digit Bin offers, Old Apis migrated to new flow - CC Txn")
    public void testCustomCheckoutOldFlow_6digitBin() throws Exception {
        User user=userManager.getForRead(Label.BASIC);
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_6DIGIT_BIN;
        String custid=user.custId();
        String orderId = CommonHelpers.generateOrderId();
        String transactionAmount="400";
        PaymentOption paymentOption= new PaymentOption(transactionAmount,"CREDIT_CARD",null,"HDFC",VISA_CC_CARD_NO,null);
        ApplyPromoV2DTO applyPromoV2DTO = new ApplyPromoV2DTO.Builder()
                .setChannelId("WEB")
                .setRequestId(orderId)
                .setToken(user.ssoToken())
                .setTokenType("SSO")
                .setCustId(custid)
                .setPaymentOptions(Arrays.asList(paymentOption))
                .setMid(mid.getId())
                .setTotalTransactionAmount(transactionAmount)
                .build();
        JsonPath jsonPath= ApplyPromoV2Api.applyPromoV2(applyPromoV2DTO, mid,"");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.paymentOffer.offerBreakup[0].promocodeApplied")).isNotNull();
        String cashback = jsonPath.getString("body.paymentOffer.totalCashbackAmount");
        String discount = jsonPath.getString("body.paymentOffer.totalInstantDiscount");
//        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,"orderId","AFFORDABILITY_PLATFORM_DISCOVERY");
//        Assertions.assertThat(logs).contains("/ads/v2/offer/apply");
        HashMap<String, Object> paymentOffersObject= jsonPath.get("body.paymentOffer");
        PaymentOffersApplied paymentOffersApplied= new PaymentOffersApplied(paymentOffersObject);

        String orderId2 = CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), mid)
                .setTxnValue(transactionAmount)
                .setOrderId(orderId2)
                .setPaymentOffersApplied(paymentOffersApplied)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(),"TXN_TOKEN",txnToken,initTxnDTO.orderFromBody().toString(),null)
                .setPaymentMode("CREDIT_CARD")
                .setCardInfo("|" + VISA_CC_CARD_NO + "|618|"+expiry)
                .setAuthMode("otp")
                .setChannelCode("VISA")
                .build();
        String jsonForm = QRHelper.executeProcessTransactionV1(processTxnV1Request);
        Assertions.assertThat(jsonForm).contains("\"resultStatus\":\"S\"");
        String logsResponse = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody().toString(), "/ats/v2/order/checkout", "RESPONSE");
        Assertions.assertThat(logsResponse).contains("Success");
        Assertions.assertThat(logsResponse).contains("\"isOfferCheckoutRequired\":true");
        Assertions.assertThat(logsResponse).contains("\"isBankOfferApplied\":true");
        nativePlusHoldpayPage.
                launch(LocalConfig.MOCK_HOST + JSON_POST_URL).
                fillAndSubmitJsonForm(jsonForm);
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(mid.getId())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateTxnAmount(String.valueOf(Double.parseDouble(transactionAmount)-Double.parseDouble(discount)))
                .AssertAll();
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).isNull();
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).contains("savings");
    }


    @Owner(MEHUL_GUPTA)
    @Test(description="Custom Checkout for 8 digit Bin offers, Old Apis migrated to new flow - CC Txn")
    public void testCustomCheckoutOldFlow_8digitBin() throws Exception {
        User user=userManager.getForRead(Label.BASIC);
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_8DIGIT_BIN;
        String custid=user.custId();
        String orderId = CommonHelpers.generateOrderId();
        String transactionAmount="400";
        PaymentOption paymentOption= new PaymentOption(transactionAmount,"CREDIT_CARD",null,"HDFC",VISA_CC_CARD_NO,null);
        ApplyPromoV2DTO applyPromoV2DTO = new ApplyPromoV2DTO.Builder()
                .setChannelId("WEB")
                .setRequestId(orderId)
                .setToken(user.ssoToken())
                .setTokenType("SSO")
                .setCustId(custid)
                .setPaymentOptions(Arrays.asList(paymentOption))
                .setMid(mid.getId())
                .setTotalTransactionAmount(transactionAmount)
                .build();
        JsonPath jsonPath= ApplyPromoV2Api.applyPromoV2(applyPromoV2DTO, mid,"");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.paymentOffer.offerBreakup[0].promocodeApplied")).isNotNull();
        String cashback = jsonPath.getString("body.paymentOffer.totalCashbackAmount");
        String discount = jsonPath.getString("body.paymentOffer.totalInstantDiscount");
//        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,"orderId","AFFORDABILITY_PLATFORM_DISCOVERY");
//        Assertions.assertThat(logs).contains("/ads/v2/offer/apply");
        HashMap<String, Object> paymentOffersObject= jsonPath.get("body.paymentOffer");
        PaymentOffersApplied paymentOffersApplied= new PaymentOffersApplied(paymentOffersObject);

        String orderId2 = CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), mid)
                .setTxnValue(transactionAmount)
                .setOrderId(orderId2)
                .setPaymentOffersApplied(paymentOffersApplied)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(),"TXN_TOKEN",txnToken,initTxnDTO.orderFromBody().toString(),null)
                .setPaymentMode("CREDIT_CARD")
                .setCardInfo("|" + VISA_CC_CARD_NO + "|618|"+expiry)
                .setAuthMode("otp")
                .setChannelCode("VISA")
                .build();
        String jsonForm = QRHelper.executeProcessTransactionV1(processTxnV1Request);
        Assertions.assertThat(jsonForm).contains("\"resultStatus\":\"S\"");
        String logsResponse = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody().toString(), "/ats/v2/order/checkout", "RESPONSE");
        Assertions.assertThat(logsResponse).contains("Success");
        Assertions.assertThat(logsResponse).contains("\"isOfferCheckoutRequired\":true");
        Assertions.assertThat(logsResponse).contains("\"isBankOfferApplied\":true");
        nativePlusHoldpayPage.
                launch(LocalConfig.MOCK_HOST + JSON_POST_URL).
                fillAndSubmitJsonForm(jsonForm);
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(mid.getId())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateTxnAmount(String.valueOf(Double.parseDouble(transactionAmount)-Double.parseDouble(discount)))
                .AssertAll();
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).isNull();
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).contains("savings");
    }


//
//    @Owner(MEHUL_GUPTA)
//    @Test(description="Custom Checkout for Amount based BO and Amount based subvention, Old Apis migrated to new flow")
//    public void testCustomCheckoutOldFlow_AmountBasedSubvention_And_AmountBasedBO_EMITxn() throws Exception {
//        User user=userManager.getForRead(Label.BASIC);
//        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_DISABLE;
//        String custid=user.custId();
//        String orderId = CommonHelpers.generateOrderId();
//        String transactionAmount="800";
//        PaymentOption paymentOption= new PaymentOption(transactionAmount,"EMI",null,"HDFC",VISA_CC_CARD_NO,"3");
//        ApplyPromoV2DTO applyPromoV2DTO = new ApplyPromoV2DTO.Builder()
//                .setChannelId("WEB")
//                .setRequestId(orderId)
//                .setToken(user.ssoToken())
//                .setTokenType("SSO")
//                .setCustId(custid)
//                .setPaymentOptions(Arrays.asList(paymentOption))
//                .setMid(mid.getId())
//                .setTotalTransactionAmount(transactionAmount)
//                .build();
//        JsonPath jsonPath= ApplyPromoV2Api.applyPromoV2(applyPromoV2DTO, mid,"");
//        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
//        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
//        Assertions.assertThat(jsonPath.getString("body.paymentOffer.offerBreakup[0].promocodeApplied")).isNotNull();
////        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderId,"AFFORDABILITY_PLATFORM_DISCOVERY");
////        Assertions.assertThat(logs).contains("/ads/v2/offer/apply");
//        HashMap<String, Object> paymentOffersObject= jsonPath.get("body.paymentOffer");
//        PaymentOffersApplied paymentOffersApplied= new PaymentOffersApplied(paymentOffersObject);
//        String cashback = jsonPath.getString("body.paymentOffer.totalCashbackAmount");
//        String discount = jsonPath.getString("body.paymentOffer.totalInstantDiscount");
//        double payableAmount = 0;
//        if (transactionAmount != null && (cashback != null || discount != null)) {
//            try {
//                payableAmount = Double.valueOf(transactionAmount) - Double.valueOf(cashback != null ? "0" : "0") - Double.valueOf(discount != null ? discount : "0");
//            } catch (NumberFormatException e) {
//                System.out.println("Error parsing integer values: " + e.getMessage());
//            }
//        } else {
//            System.out.println("One or both values are null: transactionAmount = " + transactionAmount + ", cashback = " + cashback);
//        }
//        TxnAmount txnAmount = new TxnAmount(String.valueOf(payableAmount));
//
//        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder()
//                .setMid(mid.getId())
//                .setMerchantKey(mid.getKey())
//                .setTokenType("SSO")
//                .setToken(user.ssoToken())
//                .setFilters(new Filters()
//                        .setBankCode("HDFC")
//                        .setCardType("CREDIT_CARD"))
//                .setItems(null)
//                .setSubventionAmount(String.valueOf(payableAmount))
//                .setPrice(String.valueOf(payableAmount))
//                .setOriginalPrice(transactionAmount)
//                .build();
//        ApiV1Tenure api = new ApiV1Tenure(mid.getId(), req);
//        Response r = api.execute();
//        JsonPath tenureResp = r.then()
//                .spec(SUCCESS_RESPONSE)
//                .body("body.planDetails.emiType", hasItems("SUBVENTION"))
//                .extract().jsonPath();
//
//        String planId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId");
//        BigInteger intplanid= new BigInteger(planId);
//        ApiV1ValidateRequest req2 = new ApiV1ValidateRequest.Builder(user.ssoToken(), "SSO", mid.getId())
//                .setMerchantKey(mid.getKey())
//                .setPlanId(intplanid)
//                .setItems(null)
//                .setPaymentDetails(
//                        new PaymentDetails()
//                                .setTotalTransactionAmount(String.valueOf(payableAmount))
//                                .setCardNumber("|"+VISA_CC_CARD_NO+"|618|"+expiry)
//                )
//                .setGenerateTokenForIntent(true)
//                .setPrice(String.valueOf(payableAmount))
//                .setSubventionAmount(String.valueOf(payableAmount))
//                .setOriginalPrice(transactionAmount)
//                .build();
//        ApiV1Validate api2 = new ApiV1Validate(mid.getId(), req2);
//        Response r2 = api2.execute();
//        r2.then()
//                .spec(SUCCESS_RESPONSE);
//        JsonPath jsonPath2 = r2.jsonPath();
////        String logs2 = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,"AFFORDABILITY_PLATFORM_DISCOVERY");
////        Assertions.assertThat(logs2).contains("/ads/v2/offer/apply");
//        double payableAmount2 = Double.valueOf(jsonPath2.getString("body.finalTransactionAmount"));
//        TxnAmount txnAmount2 = new TxnAmount(String.valueOf(payableAmount2));
//        String emiSubventionToken = jsonPath2.getString("body.emiSubventionToken");
//        String pgPlanId = jsonPath2.getString("body.pgPlanId");
//
//        String orderId2 = CommonHelpers.generateOrderId();
//        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), mid)
//                .setTxnValue(transactionAmount)
//                .setPayableAmount(txnAmount2)
//                .setOrderId(orderId2)
//                .setEmiSubventionToken(emiSubventionToken)
//                .setPaymentOffersApplied(paymentOffersApplied)
//                .build();
//        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
//        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(),"TXN_TOKEN",txnToken,initTxnDTO.orderFromBody().toString(),txnAmount.getValue())
//                .setPaymentMode("EMI")
//                .setCardInfo("|" + VISA_CC_CARD_NO + "|618|"+expiry)
//                .setAuthMode("otp")
//                .setChannelCode("VISA")
//                .setTxnAmount(null)
//                .setEmiType("CREDIT_CARD")
//                .setPlanId(pgPlanId)
//                .build();
//        String jsonForm = QRHelper.executeProcessTransactionV1(processTxnV1Request);
//        Assertions.assertThat(jsonForm).contains("\"resultStatus\":\"S\"");
//        String logsResponse = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody().toString(), "/ats/v2/order/checkout", "RESPONSE");
//        Assertions.assertThat(logsResponse).contains("Success");
//        Assertions.assertThat(logsResponse).contains("\"isOfferCheckoutRequired\":true");
//        Assertions.assertThat(logsResponse).contains("\"isSubventionOfferApplied\":true");
//        Assertions.assertThat(logsResponse).contains("\"isBankOfferApplied\":true");
//        //System.out.println("Checkout -> "+logsResponse);
//        nativePlusHoldpayPage.
//                launch(LocalConfig.MOCK_HOST + JSON_POST_URL).
//                fillAndSubmitJsonForm(jsonForm);
//        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
//        txnStatus.executeUntilNotPending();
//        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
//                .validateOrderid(initTxnDTO.orderFromBody())
//                .validateStatus("TXN_SUCCESS")
//                .validateTxnType("SALE")
//                .validateRespCode("01")
//                .validateRespMsg("Txn Success")
//                .validateMid(mid.getId())
//                .validatePaymentMode("EMI")
//                .validateRefundAmnt("0.00")
//                .validateTxnAmount(String.valueOf(payableAmount2))
//                .validateTxnDate(new Date())
//                .AssertAll();
//        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).contains(pgPlanId);
//        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).contains("savings");
//    }

}
