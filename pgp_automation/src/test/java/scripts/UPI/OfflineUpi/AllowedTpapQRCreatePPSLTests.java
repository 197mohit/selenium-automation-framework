package scripts.UPI.OfflineUpi;


import com.paytm.api.Instaproxy.UPISecureResponse;
import com.paytm.api.StaticQrUpiPSP;
import com.paytm.api.TxnStatus;
import com.paytm.api.nativeAPI.SubscriptionCreate;
import com.paytm.appconstants.Constants;
import com.paytm.appconstants.Constants.MerchantType;
import com.paytm.appconstants.Constants.ResultCode;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.dto.NativeDTO.InitTxn.ExtendInfo;
import com.paytm.dto.NativeDTO.InitTxn.RiskExtendInfo;
import com.paytm.dto.upiIntent.staticQR.StaticQrUpiPSPRequest;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.SoftAssertions;
import org.hamcrest.Matchers;
import org.testng.annotations.Test;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class AllowedTpapQRCreatePPSLTests extends PGPBaseTest {

  private static final MerchantType TEST_MERCHANT = MerchantType.ALLOWED_TPAP_MERCHANT_PPSL;
  private static final String SUCCESS_PAYER_VPA = "9759417123vpa4ba@ptyes";
  private static final String FAILURE_PAYER_VPA = "9759417123vpa4ba@yespay";
  private static final String PARTIAL_MATCH_VPA = "9759417123vpa4ba@pty";
  private static final List<String> ALLOWED_TPAPS = Arrays.asList("PTYES", "PTAXIS", "PTHDFC");
  private static final List<String> SINGLE_TPAP = Arrays.asList("PTYES");
  private static final List<String> EMPTY_TPAP = Arrays.asList();
  private String subscriptionId;
  private final String txnAmount = "100.00";

  @Feature("PGP-60147")
  @Owner(Constants.Owner.ABHISHEK_VERMA)
  @Test(description = "TC_01: When payerVpa is in allowedTpap List")
  public void verifyPayerVpaInAllowedTpapList() throws Exception {
    String orderId = CommonHelpers.generateOrderId();
    createsubscriptionwithallowedtpaps(ALLOWED_TPAPS, orderId);

    RiskExtendInfo riskExtendInfo = new RiskExtendInfo()
        .setBusinessType("Mandate")
        .setIsVerifiedMerchant("true")
        .setPurposeCode("00")
        .setInitiationMode("01")
        .setMerchantGenre("OFFLINE");
    StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(
        "SEAMLESS_3D_FORM",
        TEST_MERCHANT.getId(),
        orderId,
        txnAmount,
        riskExtendInfo
    );
    builder.setPayerVpa(SUCCESS_PAYER_VPA)
        .setPayeeVpa(TEST_MERCHANT.getVpa());

    StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "");
    StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
    JsonPath response = staticQrUpiPSP.execute().jsonPath();
    String esn = response.getString("body.externalSerialNo");

    // Verify transaction success
    SoftAssertions softAssertions = new SoftAssertions();
    softAssertions.assertThat(response.getString("body.resultCode"))
        .isEqualToIgnoringCase(Constants.ResultCode.SUCCESS.getCode());
    softAssertions.assertThat(response.getString("body.resultCodeId"))
        .isEqualToIgnoringCase(Constants.ResultCode.SUCCESS.getResultCodeId());
    softAssertions.assertThat(response.getString("body.resultMsg"))
        .isEqualToIgnoringCase(Constants.ResultCode.SUCCESS.getResultMsg());
    softAssertions.assertAll();
    verifyTransactionSuccess(TEST_MERCHANT.getId(), orderId, txnAmount, esn,
        TEST_MERCHANT.getVpa(),SUCCESS_PAYER_VPA);
  }

  @Feature("PGP-60147")
  @Owner(Constants.Owner.ABHISHEK_VERMA)
  @Test(description = "TC_02: When payerVpa is not in allowedTpap list")
  public void verifyPayerVpaNotInAllowedTpapList() throws Exception {
    String orderId = CommonHelpers.generateOrderId();
    createsubscriptionwithallowedtpaps(ALLOWED_TPAPS, orderId);

    RiskExtendInfo riskExtendInfo = new RiskExtendInfo()
        .setBusinessType("Mandate")
        .setIsVerifiedMerchant("true")
        .setPurposeCode("00")
        .setInitiationMode("01")
        .setMerchantGenre("OFFLINE");

    StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(
        "SEAMLESS_3D_FORM",
        TEST_MERCHANT.getId(),
        orderId,
        txnAmount,
        riskExtendInfo
    );
    builder.setPayerVpa(FAILURE_PAYER_VPA);
    builder.setPayeeVpa(TEST_MERCHANT.getVpa());

    StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "");
    StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
    JsonPath response = staticQrUpiPSP.execute().jsonPath();

    // Verify transaction fails with risk reject
    SoftAssertions softAssertions = new SoftAssertions();
    softAssertions.assertThat(response.getString("body.resultCode"))
        .isEqualTo(Constants.ResultCode.RISK_REJECT.getCode());
    softAssertions.assertThat(response.getString("body.resultCodeId"))
        .isEqualTo(Constants.ResultCode.RISK_REJECT.getResultCodeId());
    softAssertions.assertThat(response.getString("body.resultMsg"))
        .isEqualTo(Constants.ResultCode.RISK_REJECT.getResultMsg());
    softAssertions.assertThat(response.getString("body.subResultCodeId"))
        .isEqualTo(Constants.ResultCode.RISK_REJECT.getSubResultCodeId());
    softAssertions.assertAll();
  }

  @Feature("PGP-60147")
  @Owner(Constants.Owner.ABHISHEK_VERMA)
  @Test(description = "TC_03: Verify if allowedTpap list is not sent while creating DQR then all handles should be allowed")
  public void verifyAllHandlesAllowedWhenNoAllowedTpapList() throws Exception {
    String orderId = CommonHelpers.generateOrderId();
    createsubscriptionwithallowedtpaps(null,orderId);
    RiskExtendInfo riskExtendInfo = new RiskExtendInfo()
        .setBusinessType("Mandate")
        .setIsVerifiedMerchant("true")
        .setPurposeCode("00")
        .setInitiationMode("01")
        .setMerchantGenre("OFFLINE");

    StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(
        "SEAMLESS_3D_FORM",
        TEST_MERCHANT.getId(),
        orderId,
        txnAmount,
        riskExtendInfo
    );
    builder.setPayerVpa(FAILURE_PAYER_VPA);
    builder.setPayeeVpa(TEST_MERCHANT.getVpa());

    StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder);
    StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
    JsonPath response = staticQrUpiPSP.execute().jsonPath();

    // Verify transaction success
    SoftAssertions softAssertions = new SoftAssertions();
    softAssertions.assertThat(response.getString("body.resultCode"))
        .isEqualTo(Constants.ResultCode.SUCCESS.getCode());
    softAssertions.assertAll();
    String esn = response.getString("body.externalSerialNo");
    verifyTransactionSuccess(TEST_MERCHANT.getId(), orderId, txnAmount, esn,
        TEST_MERCHANT.getVpa(),SUCCESS_PAYER_VPA);
  }

  @Feature("PGP-60147")
  @Owner(Constants.Owner.ABHISHEK_VERMA)
  @Test(description = "TC_04: Verify txn is only allowed if payerVpa handle is exact match with allowedTpap, txn should fail for partial match")
  public void verifyExactMatchRequiredForAllowedTpap() throws Exception {
    String orderId = CommonHelpers.generateOrderId();
    createsubscriptionwithallowedtpaps(ALLOWED_TPAPS, orderId);

    RiskExtendInfo riskExtendInfo = new RiskExtendInfo()
        .setBusinessType("Mandate")
        .setIsVerifiedMerchant("true")
        .setPurposeCode("00")
        .setInitiationMode("01")
        .setMerchantGenre("OFFLINE");

    StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(
        "SEAMLESS_3D_FORM",
        TEST_MERCHANT.getId(),
        orderId,
        txnAmount,
        riskExtendInfo
    );
    builder.setPayerVpa(PARTIAL_MATCH_VPA);
    builder.setPayeeVpa(TEST_MERCHANT.getVpa());
    StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "");
    StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
    JsonPath response = staticQrUpiPSP.execute().jsonPath();

    // Verify transaction fails with risk reject
    SoftAssertions softAssertions = new SoftAssertions();
    softAssertions.assertThat(response.getString("body.resultCode"))
        .isEqualTo(Constants.ResultCode.RISK_REJECT.getCode());
    softAssertions.assertThat(response.getString("body.resultCodeId"))
        .isEqualTo(Constants.ResultCode.RISK_REJECT.getResultCodeId());
    softAssertions.assertThat(response.getString("body.resultMsg"))
        .isEqualTo(Constants.ResultCode.RISK_REJECT.getResultMsg());
    softAssertions.assertThat(response.getString("body.subResultCodeId"))
        .isEqualTo(Constants.ResultCode.RISK_REJECT.getSubResultCodeId());
    softAssertions.assertAll();
  }

  @Feature("PGP-60147")
  @Owner(Constants.Owner.ABHISHEK_VERMA)
  @Test(description = "TC_05: Verify when allowedTpap list have only a single tpap APP")
  public void verifySingleTpapAppInAllowedList() throws Exception {
    String orderId = CommonHelpers.generateOrderId();
    createsubscriptionwithallowedtpaps(SINGLE_TPAP, orderId);

    RiskExtendInfo riskExtendInfo = new RiskExtendInfo()
        .setBusinessType("Mandate")
        .setIsVerifiedMerchant("true")
        .setPurposeCode("00")
        .setInitiationMode("01")
        .setMerchantGenre("OFFLINE");

    StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(
        "SEAMLESS_3D_FORM",
        TEST_MERCHANT.getId(),
        orderId,
        txnAmount,
        riskExtendInfo
    );
    builder.setPayerVpa(SUCCESS_PAYER_VPA);
    builder.setPayeeVpa(TEST_MERCHANT.getVpa());

    StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder);
    StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
    JsonPath response = staticQrUpiPSP.execute().jsonPath();

    // Verify transaction success
    SoftAssertions softAssertions = new SoftAssertions();
    softAssertions.assertThat(response.getString("body.resultCode"))
        .isEqualTo(Constants.ResultCode.SUCCESS.getCode());
    softAssertions.assertAll();
    String esn = response.getString("body.externalSerialNo");
    verifyTransactionSuccess(TEST_MERCHANT.getId(), orderId, txnAmount, esn,
        TEST_MERCHANT.getVpa(),SUCCESS_PAYER_VPA);
  }

  @Feature("PGP-60147")
  @Owner(Constants.Owner.ABHISHEK_VERMA)
  @Test(description = "TC_06: Verify when empty allowedTpap list is coming")
  public void verifyEmptyAllowedTpapList() throws Exception {
    String orderId = CommonHelpers.generateOrderId();
    createsubscriptionwithallowedtpaps(EMPTY_TPAP, orderId);

    RiskExtendInfo riskExtendInfo = new RiskExtendInfo()
        .setBusinessType("Mandate")
        .setIsVerifiedMerchant("true")
        .setPurposeCode("00")
        .setInitiationMode("01")
        .setMerchantGenre("OFFLINE");

    StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(
        "SEAMLESS_3D_FORM",
        TEST_MERCHANT.getId(),
        orderId,
        txnAmount,
        riskExtendInfo
    );
    builder.setPayerVpa(FAILURE_PAYER_VPA);
    builder.setPayeeVpa(TEST_MERCHANT.getVpa());

    StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder);
    StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
    JsonPath response = staticQrUpiPSP.execute().jsonPath();

    // Verify transaction success
    SoftAssertions softAssertions = new SoftAssertions();
    softAssertions.assertThat(response.getString("body.resultCode"))
        .isEqualTo(Constants.ResultCode.SUCCESS.getCode());
    softAssertions.assertAll();
    String esn = response.getString("body.externalSerialNo");
    verifyTransactionSuccess(TEST_MERCHANT.getId(), orderId, txnAmount, esn,
        TEST_MERCHANT.getVpa(),SUCCESS_PAYER_VPA);
  }

  @Feature("PGP-60147")
  @Owner(Constants.Owner.ABHISHEK_VERMA)
  @Test(description = "TC_7: Verify retry with allowed tpap")
  public void verifyRetryWithAllowedTpap() throws Exception {
    String orderId = CommonHelpers.generateOrderId();
    createsubscriptionwithallowedtpaps(ALLOWED_TPAPS, orderId);

    // First attempt - should fail (simulating failure)
    RiskExtendInfo riskExtendInfo = new RiskExtendInfo()
        .setBusinessType("Mandate")
        .setIsVerifiedMerchant("true")
        .setPurposeCode("00")
        .setInitiationMode("01")
        .setMerchantGenre("OFFLINE");

    StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(
        "SEAMLESS_3D_FORM",
        TEST_MERCHANT.getId(),
        orderId,
        txnAmount,
        riskExtendInfo
    );
    builder.setPayerVpa(FAILURE_PAYER_VPA);
    builder.setPayeeVpa(TEST_MERCHANT.getVpa());

    StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "");
    StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
    JsonPath firstResponse = staticQrUpiPSP.execute().jsonPath();

    // Verify transaction fails with risk reject
    SoftAssertions softAssertions = new SoftAssertions();
    softAssertions.assertThat(firstResponse.getString("body.resultCode"))
        .isEqualTo(Constants.ResultCode.RISK_REJECT.getCode());
    softAssertions.assertThat(firstResponse.getString("body.resultCodeId"))
        .isEqualTo(Constants.ResultCode.RISK_REJECT.getResultCodeId());
    softAssertions.assertThat(firstResponse.getString("body.resultMsg"))
        .isEqualTo(Constants.ResultCode.RISK_REJECT.getResultMsg());
    softAssertions.assertThat(firstResponse.getString("body.subResultCodeId"))
        .isEqualTo(Constants.ResultCode.RISK_REJECT.getSubResultCodeId());
    softAssertions.assertAll();

    // Retry attempt - should succeed
    builder.setPayerVpa(SUCCESS_PAYER_VPA);
    StaticQrUpiPSPRequest staticQrUpiPSPRequestRetry = new StaticQrUpiPSPRequest(builder, "");
    StaticQrUpiPSP staticQrUpiPSPRetry = new StaticQrUpiPSP(staticQrUpiPSPRequestRetry);
    JsonPath retryResponse = staticQrUpiPSPRetry.execute().jsonPath();

    // Verify transaction success
    softAssertions.assertThat(retryResponse.getString("body.resultCode"))
        .isEqualTo(Constants.ResultCode.SUCCESS.getCode());
    softAssertions.assertAll();
    String esn = retryResponse.getString("body.externalSerialNo");
    verifyTransactionSuccess(TEST_MERCHANT.getId(), orderId, txnAmount, esn,
        TEST_MERCHANT.getVpa(),SUCCESS_PAYER_VPA);
  }

  // ==================== HELPER METHODS ====================
  private void createsubscriptionwithallowedtpaps(List<String> allowedTpaps, String orderId)
      throws Exception {

    ExtendInfo extendInfo = new ExtendInfo();
    
    // Set allowedTpaps in additionalAttributes
    if (allowedTpaps != null && !allowedTpaps.isEmpty()) {
      String allowedTpapString = String.join(",", allowedTpaps);
      extendInfo.setAdditionalProperty("additionalAttributes", Map.of("allowedTpaps", allowedTpapString));
    }

    SubscriptionCreate subscriptionCreate = new SubscriptionCreate(
        new com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO.Builder(
            TEST_MERCHANT.getId(),
            TEST_MERCHANT.getKey()
        )
            .setOrderId(orderId)
            .setTxnValue(txnAmount)
            .setSubscriptionPaymentMode("UPI")
            .setSubscriptionAmountType("VARIABLE")
            .setSubscriptionMaxAmount(txnAmount)
            .setSubscriptionFrequency("1")
            .setSubscriptionRetryCount("0")
            .setSubscriptionFrequencyUnit("MONTH")
            .setSubscriptionGraceDays("0")
            .setSubscriptionStartDate(com.paytm.apphelpers.CommonHelpers.getDate().toString())
            .setRequestType("NATIVE_SUBSCRIPTION")
            .setExtendInfo(extendInfo)
            .build()
    );

    JsonPath response = subscriptionCreate.execute().jsonPath();
    SoftAssertions softAssertions = new SoftAssertions();
    softAssertions.assertThat(response.getString("body.resultInfo.resultCode"))
        .isEqualToIgnoringCase(ResultCode.SUCCESS_SUBS.getCode());
    softAssertions.assertThat(response.getString("body.resultInfo.resultStatus"))
        .isEqualToIgnoringCase(Constants.ResultCode.SUCCESS_SUBS.getResultStatus());
    softAssertions.assertThat(response.getString("body.resultInfo.resultMsg"))
        .isEqualToIgnoringCase(Constants.ResultCode.SUCCESS_SUBS.getResultMsg());
    softAssertions.assertAll();

    subscriptionId = response.getString("body.subscriptionId");
    if (subscriptionId == null) {
      throw new RuntimeException("Failed to create subscription");
    }
  }
  private void verifyTransactionSuccess(String mid, String orderId, String txnAmount,
      String externalSerialNo, String payeeVpa,String payerVpa) {

    String bankCode;
    String gatewayName;
    bankCode = "PTYBLI";
    gatewayName = Constants.Gateway.PTYBLI.toString();
    UPISecureResponse secureResponse = new UPISecureResponse().buildRequest(
        "Abhishek Verma",
        payerVpa,
        payeeVpa,
        "Punjab National Bank",
        "PUNB",
        externalSerialNo,
        "SUCCESS",
        String.format("%011d", (long) (Math.random() * 100000000000L)),
        txnAmount,
        "0",
        "Transaction is successful",
        "DEFERRED_SETTLEMENT",
        "",
        bankCode
    );

    Response response = secureResponse.execute();
    response.then()
        .statusCode(200)
        .body("body.resultCode",
            Matchers.equalToIgnoringCase(Constants.ResultCode.SUCCESS.getCode()));

    // Verify transaction status
    TxnStatus txnStatus = new TxnStatus(mid, orderId);
    txnStatus.executeUntilNotPending();
    txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
        .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
        .validateOrderid(orderId)
        .validateTxnAmount(txnAmount)
        .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
        .validateRespCode("01")
        .validateRespMsg("Txn Success")
        .validatePaymentMode("UPI")
        .validateGatewayName(gatewayName)
        .validateMid(mid)
        .AssertAll();
  }
}
