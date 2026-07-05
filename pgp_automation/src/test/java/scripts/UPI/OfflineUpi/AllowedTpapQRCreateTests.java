package scripts.UPI.OfflineUpi;


import static com.paytm.apphelpers.QRHelper.parseDeeplinkInfo;
import com.paytm.api.PaymentService;
import com.paytm.api.TxnStatus;
import com.paytm.api.UpiPspProcessor;
import com.paytm.api.Instaproxy.UPISecureResponse;
import com.paytm.appconstants.Constants;
import com.paytm.appconstants.Constants.ResultCode;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.dto.upiIntent.staticQR.StaticQrUpiPSPRequest;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import java.util.Map;
import org.assertj.core.api.SoftAssertions;
import org.hamcrest.Matchers;
import org.testng.annotations.Test;
import java.util.Arrays;
import java.util.List;

@Owner(Constants.Owner.ABHISHEK_VERMA)
@Feature("PGP-60145")
public class AllowedTpapQRCreateTests extends PGPBaseTest {

  private static final String SUCCESS_PAYER_VPA = "9759417123vpa4ba@ptyes";
  private static final String FAILURE_PAYER_VPA = "9759417123vpa4ba@yespay";
  private static final String PARTIAL_MATCH_VPA = "9759417123vpa4ba@pty";
  private static final String TXN_AMOUNT = "100.00";
  private static final List<String> ALLOWED_TPAPS = Arrays.asList("PTYES", "PTAXIS", "PTHDFC");
  private static final List<String> SINGLE_TPAP = Arrays.asList("PTYES");
  private static final List<String> EMPTY_TPAP = Arrays.asList();

  @Test(description = "TC_01: Verify successful transaction when payerVpa is in allowedTpap list")
  public void testSuccessfulTransactionWithAllowedTpap() throws Exception {
    String orderId = CommonHelpers.generateOrderId();
    Constants.MerchantType merchant = Constants.MerchantType.ALLOWED_TPAP_MERCHANT;
    String mid = merchant.getId();
    SoftAssertions softAssertions = new SoftAssertions();

    // Step 1: Create QR with allowedTpap list
    PaymentService paymentService = new PaymentService(merchant, TXN_AMOUNT, orderId,
        ALLOWED_TPAPS);
    JsonPath qrResponse = paymentService.execute().jsonPath();

    softAssertions.assertThat(qrResponse.getString("body.resultInfo.resultStatus"))
        .isEqualTo(Constants.ResultCode.QR_SUCCESS.getResultStatus());
    softAssertions.assertThat(qrResponse.getString("body.resultInfo.resultMsg"))
        .isEqualToIgnoringCase(Constants.ResultCode.QR_SUCCESS.getResultMsg());
    softAssertions.assertThat(qrResponse.getString("body.resultInfo.resultCode"))
        .isEqualToIgnoringCase(Constants.ResultCode.QR_SUCCESS.getResultCodeId());
    softAssertions.assertAll();
    String qrData = qrResponse.getString("body.qrData");

    Map<String, String> deeplinkInfo = parseDeeplinkInfo(qrData);
    String payeeVpa = deeplinkInfo.get("payeeVpa");
    String amount = deeplinkInfo.get("amount");
    String tr = deeplinkInfo.get("tr");
    // Step 2: Process UPI PSP with allowed payerVpa
    StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, tr,
        amount,
        payeeVpa, SUCCESS_PAYER_VPA, "", "");
    StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "", tr);
    UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);

    JsonPath response = upiPspProcessor.execute().jsonPath();

    softAssertions.assertThat(response.getString("body.resultCode"))
        .isEqualToIgnoringCase(Constants.ResultCode.SUCCESS.getCode());
    softAssertions.assertThat(response.getString("body.resultCodeId"))
        .isEqualToIgnoringCase(Constants.ResultCode.SUCCESS.getResultCodeId());
    softAssertions.assertThat(response.getString("body.resultMsg"))
        .isEqualToIgnoringCase(Constants.ResultCode.SUCCESS.getResultMsg());
    softAssertions.assertAll();
    String esn = response.getString("body.externalSerialNo");

    // Step 3: Verify transaction success
    verifyTransactionSuccess(mid, orderId, TXN_AMOUNT, esn, payeeVpa);
    softAssertions.assertAll();
  }


  @Test(description = "TC_02: Verify transaction failure when payerVpa is not in allowedTpap list")
  public void testFailedTransactionWithNonAllowedTpap() throws Exception {
    String orderId = CommonHelpers.generateOrderId();
    Constants.MerchantType merchant = Constants.MerchantType.ALLOWED_TPAP_MERCHANT;
    String mid = merchant.getId();
    SoftAssertions softAssertions = new SoftAssertions();

    // Step 1: Create QR with allowedTpap list
    PaymentService paymentService = new PaymentService(merchant, TXN_AMOUNT, orderId,
        ALLOWED_TPAPS);
    JsonPath qrResponse = paymentService.execute().jsonPath();

    softAssertions.assertThat(qrResponse.getString("body.resultInfo.resultStatus"))
        .isEqualTo(Constants.ResultCode.QR_SUCCESS.getResultStatus());
    softAssertions.assertThat(qrResponse.getString("body.resultInfo.resultMsg"))
        .isEqualToIgnoringCase(Constants.ResultCode.QR_SUCCESS.getResultMsg());
    softAssertions.assertThat(qrResponse.getString("body.resultInfo.resultCode"))
        .isEqualToIgnoringCase(Constants.ResultCode.QR_SUCCESS.getResultCodeId());
    softAssertions.assertAll();

    String qrData = qrResponse.getString("body.qrData");

    Map<String, String> deeplinkInfo = parseDeeplinkInfo(qrData);
    String payeeVpa = deeplinkInfo.get("payeeVpa");
    String amount = deeplinkInfo.get("amount");
    String tr = deeplinkInfo.get("tr");
    // Step 2: Process UPI PSP with not-allowed payerVpa
    StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, tr,
        amount,
        payeeVpa, FAILURE_PAYER_VPA, "", "");
    StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "", tr);
    UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);

    JsonPath response = upiPspProcessor.execute().jsonPath();

    softAssertions.assertThat(response.getString("body.resultCode")).isEqualToIgnoringCase(
        ResultCode.RISK_REJECT.getCode());
    softAssertions.assertThat(response.getString("body.resultCodeId"))
        .isEqualToIgnoringCase(Constants.ResultCode.RISK_REJECT.getResultCodeId());
    softAssertions.assertThat(response.getString("body.resultMsg"))
        .isEqualToIgnoringCase(Constants.ResultCode.RISK_REJECT.getResultMsg());

    softAssertions.assertAll();
  }

  @Test(description = "TC_03: Verify transaction success when allowedTpap list is not sent")
  public void testTransactionSuccessWithoutAllowedTpap() throws Exception {
    String orderId = CommonHelpers.generateOrderId();
    Constants.MerchantType merchant = Constants.MerchantType.ALLOWED_TPAP_MERCHANT;
    String mid = merchant.getId();
    SoftAssertions softAssertions = new SoftAssertions();

    // Step 1: Create QR with allowedTpap list
    PaymentService paymentService = new PaymentService(merchant, TXN_AMOUNT, orderId,
        ALLOWED_TPAPS);
    paymentService.deleteContext("body.additionalInfo.allowedTpaps");
    JsonPath qrResponse = paymentService.execute().jsonPath();

    softAssertions.assertThat(qrResponse.getString("body.resultInfo.resultStatus"))
        .isEqualTo(Constants.ResultCode.QR_SUCCESS.getResultStatus());
    softAssertions.assertThat(qrResponse.getString("body.resultInfo.resultMsg"))
        .isEqualToIgnoringCase(Constants.ResultCode.QR_SUCCESS.getResultMsg());
    softAssertions.assertThat(qrResponse.getString("body.resultInfo.resultCode"))
        .isEqualToIgnoringCase(Constants.ResultCode.QR_SUCCESS.getResultCodeId());
    softAssertions.assertAll();
    String qrData = qrResponse.getString("body.qrData");

    Map<String, String> deeplinkInfo = parseDeeplinkInfo(qrData);
    String payeeVpa = deeplinkInfo.get("payeeVpa");
    String amount = deeplinkInfo.get("amount");
    String tr = deeplinkInfo.get("tr");
    // Step 2: Process UPI PSP with allowed payerVpa
    StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, tr,
        amount,
        payeeVpa, SUCCESS_PAYER_VPA, "", "");
    StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "", tr);
    UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);

    JsonPath response = upiPspProcessor.execute().jsonPath();

    softAssertions.assertThat(response.getString("body.resultCode"))
        .isEqualToIgnoringCase(Constants.ResultCode.SUCCESS.getCode());
    softAssertions.assertThat(response.getString("body.resultCodeId"))
        .isEqualToIgnoringCase(Constants.ResultCode.SUCCESS.getResultCodeId());
    softAssertions.assertThat(response.getString("body.resultMsg"))
        .isEqualToIgnoringCase(Constants.ResultCode.SUCCESS.getResultMsg());
    softAssertions.assertAll();
    String esn = response.getString("body.externalSerialNo");

    // Step 3: Verify transaction success
    verifyTransactionSuccess(mid, orderId, TXN_AMOUNT, esn, payeeVpa);
    softAssertions.assertAll();
  }

  @Test(description = "TC_04: Verify transaction fails for partial match in allowedTpap")
  public void testTransactionFailureForPartialMatch() throws Exception {
    String orderId = CommonHelpers.generateOrderId();
    Constants.MerchantType merchant = Constants.MerchantType.ALLOWED_TPAP_MERCHANT;
    String mid = merchant.getId();
    SoftAssertions softAssertions = new SoftAssertions();

    // Step 1: Create QR with allowedTpap list
    PaymentService paymentService = new PaymentService(merchant, TXN_AMOUNT, orderId,
        ALLOWED_TPAPS);
    JsonPath qrResponse = paymentService.execute().jsonPath();

    softAssertions.assertThat(qrResponse.getString("body.resultInfo.resultStatus"))
        .isEqualTo(Constants.ResultCode.QR_SUCCESS.getResultStatus());
    softAssertions.assertThat(qrResponse.getString("body.resultInfo.resultMsg"))
        .isEqualToIgnoringCase(Constants.ResultCode.QR_SUCCESS.getResultMsg());
    softAssertions.assertThat(qrResponse.getString("body.resultInfo.resultCode"))
        .isEqualToIgnoringCase(Constants.ResultCode.QR_SUCCESS.getResultCodeId());
    softAssertions.assertAll();
    String qrData = qrResponse.getString("body.qrData");

    Map<String, String> deeplinkInfo = parseDeeplinkInfo(qrData);
    String payeeVpa = deeplinkInfo.get("payeeVpa");
    String amount = deeplinkInfo.get("amount");
    String tr = deeplinkInfo.get("tr");
    // Step 2: Process UPI PSP with partial match payerVpa
    StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, tr,
        amount,
        payeeVpa, PARTIAL_MATCH_VPA, "", "");
    StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "", tr);
    UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);

    JsonPath response = upiPspProcessor.execute().jsonPath();

    softAssertions.assertThat(response.getString("body.resultCode"))
        .isEqualToIgnoringCase(Constants.ResultCode.RISK_REJECT.getCode());
    softAssertions.assertThat(response.getString("body.resultCodeId"))
        .isEqualToIgnoringCase(Constants.ResultCode.RISK_REJECT.getResultCodeId());
    softAssertions.assertThat(response.getString("body.resultMsg"))
        .isEqualToIgnoringCase(Constants.ResultCode.RISK_REJECT.getResultMsg());

    softAssertions.assertAll();
  }

  @Test(description = "TC_05: Verify successful transaction with single allowedTpap")
  public void testSuccessfulTransactionWithSingleTpap() throws Exception {
    String orderId = CommonHelpers.generateOrderId();
    Constants.MerchantType merchant = Constants.MerchantType.ALLOWED_TPAP_MERCHANT;
    String mid = merchant.getId();
    SoftAssertions softAssertions = new SoftAssertions();

    // Step 1: Create QR with single allowedTpap
    PaymentService paymentService = new PaymentService(merchant, TXN_AMOUNT, orderId, SINGLE_TPAP);
    JsonPath qrResponse = paymentService.execute().jsonPath();

    softAssertions.assertThat(qrResponse.getString("body.resultInfo.resultStatus"))
        .isEqualTo(Constants.ResultCode.QR_SUCCESS.getResultStatus());
    softAssertions.assertThat(qrResponse.getString("body.resultInfo.resultMsg"))
        .isEqualToIgnoringCase(Constants.ResultCode.QR_SUCCESS.getResultMsg());
    softAssertions.assertAll();
    String qrData = qrResponse.getString("body.qrData");

    Map<String, String> deeplinkInfo = parseDeeplinkInfo(qrData);
    String payeeVpa = deeplinkInfo.get("payeeVpa");
    String amount = deeplinkInfo.get("amount");
    String tr = deeplinkInfo.get("tr");
    // Step 2: Process UPI PSP with allowed payerVpa
    StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, tr,
        amount,
        payeeVpa, SUCCESS_PAYER_VPA, "", "");
    StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "", tr);
    UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);

    JsonPath response = upiPspProcessor.execute().jsonPath();

    softAssertions.assertThat(response.getString("body.resultCode"))
        .isEqualToIgnoringCase(Constants.ResultCode.SUCCESS.getCode());
    softAssertions.assertThat(response.getString("body.resultCodeId"))
        .isEqualToIgnoringCase(Constants.ResultCode.SUCCESS.getResultCodeId());
    softAssertions.assertThat(response.getString("body.resultMsg"))
        .isEqualToIgnoringCase(Constants.ResultCode.SUCCESS.getResultMsg());
    softAssertions.assertAll();
    String esn = response.getString("body.externalSerialNo");

    // Step 3: Verify transaction success
    verifyTransactionSuccess(mid, orderId, TXN_AMOUNT, esn, payeeVpa);
    softAssertions.assertAll();
  }

  @Test(description = "TC_06: Verify successful transaction with empty allowedTpap list")
  public void testSuccessfulTransactionWithEmptyTpapList() throws Exception {
    String orderId = CommonHelpers.generateOrderId();
    Constants.MerchantType merchant = Constants.MerchantType.ALLOWED_TPAP_MERCHANT;
    String mid = merchant.getId();
    SoftAssertions softAssertions = new SoftAssertions();

    // Step 1: Create QR with single allowedTpap
    PaymentService paymentService = new PaymentService(merchant, TXN_AMOUNT, orderId, EMPTY_TPAP);
    JsonPath qrResponse = paymentService.execute().jsonPath();

    softAssertions.assertThat(qrResponse.getString("body.resultInfo.resultStatus"))
        .isEqualTo(Constants.ResultCode.QR_SUCCESS.getResultStatus());
    softAssertions.assertThat(qrResponse.getString("body.resultInfo.resultMsg"))
        .isEqualToIgnoringCase(Constants.ResultCode.QR_SUCCESS.getResultMsg());
    softAssertions.assertAll();
    String qrData = qrResponse.getString("body.qrData");

    Map<String, String> deeplinkInfo = parseDeeplinkInfo(qrData);
    String payeeVpa = deeplinkInfo.get("payeeVpa");
    String amount = deeplinkInfo.get("amount");
    String tr = deeplinkInfo.get("tr");
    // Step 2: Process UPI PSP with not-allowed payerVpa
    StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, tr,
        amount,
        payeeVpa, FAILURE_PAYER_VPA, "", "");
    StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "", tr);
    UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);

    JsonPath response = upiPspProcessor.execute().jsonPath();

    softAssertions.assertThat(response.getString("body.resultCode"))
        .isEqualToIgnoringCase(Constants.ResultCode.SUCCESS.getCode());
    softAssertions.assertThat(response.getString("body.resultCodeId"))
        .isEqualToIgnoringCase(Constants.ResultCode.SUCCESS.getResultCodeId());
    softAssertions.assertThat(response.getString("body.resultMsg"))
        .isEqualToIgnoringCase(Constants.ResultCode.SUCCESS.getResultMsg());
    softAssertions.assertAll();
    String esn = response.getString("body.externalSerialNo");

    // Step 3: Verify transaction success
    verifyTransactionSuccess(mid, orderId, TXN_AMOUNT, esn, payeeVpa);
    softAssertions.assertAll();
  }


  @Test(description = "TC_07: Verify retry mechanism with allowed tpap after failure")
  public void testRetryWithAllowedTpap() throws Exception {
    String orderId = CommonHelpers.generateOrderId();
    Constants.MerchantType merchant = Constants.MerchantType.ALLOWED_TPAP_MERCHANT;
    String mid = merchant.getId();
    SoftAssertions softAssertions = new SoftAssertions();

    // Step 1: Create QR with allowedTpap list
    PaymentService paymentService = new PaymentService(merchant, TXN_AMOUNT, orderId,
        ALLOWED_TPAPS);
    JsonPath qrResponse = paymentService.execute().jsonPath();

    softAssertions.assertThat(qrResponse.getString("body.resultInfo.resultStatus"))
        .isEqualTo(Constants.ResultCode.QR_SUCCESS.getResultStatus());
    softAssertions.assertThat(qrResponse.getString("body.resultInfo.resultMsg"))
        .isEqualToIgnoringCase(Constants.ResultCode.QR_SUCCESS.getResultMsg());
    softAssertions.assertThat(qrResponse.getString("body.resultInfo.resultCode"))
        .isEqualToIgnoringCase(Constants.ResultCode.QR_SUCCESS.getResultCodeId());
    softAssertions.assertAll();
    String qrData = qrResponse.getString("body.qrData");

    Map<String, String> deeplinkInfo = parseDeeplinkInfo(qrData);
    String payeeVpa = deeplinkInfo.get("payeeVpa");
    String amount = deeplinkInfo.get("amount");
    String tr = deeplinkInfo.get("tr");
    // Step 2: Process UPI PSP with allowed payerVpa
    StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, tr,
        amount,
        payeeVpa, FAILURE_PAYER_VPA, "", "");
    StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "", tr);
    UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);

    JsonPath response1 = upiPspProcessor.execute().jsonPath();

    softAssertions.assertThat(response1.getString("body.resultCode"))
        .isEqualToIgnoringCase(ResultCode.RISK_REJECT.getCode());
    softAssertions.assertThat(response1.getString("body.resultCodeId"))
        .isEqualToIgnoringCase(Constants.ResultCode.RISK_REJECT.getResultCodeId());
    softAssertions.assertThat(response1.getString("body.resultMsg"))
        .isEqualToIgnoringCase(Constants.ResultCode.RISK_REJECT.getResultMsg());
    softAssertions.assertAll();
    StaticQrUpiPSPRequest.Builder builder1 = new StaticQrUpiPSPRequest.Builder(mid, tr,
        amount,
        payeeVpa, SUCCESS_PAYER_VPA, "", "");
    StaticQrUpiPSPRequest staticQrUpiPSPRequest1 = new StaticQrUpiPSPRequest(builder1, "", tr);
    UpiPspProcessor upiPspProcessor1 = new UpiPspProcessor(staticQrUpiPSPRequest1);

    JsonPath response2 = upiPspProcessor1.execute().jsonPath();

    softAssertions.assertThat(response2.getString("body.resultCodeId"))
        .isEqualTo(ResultCode.SUCCESS.getResultCodeId());
    softAssertions.assertThat(response2.getString("body.resultCode"))
        .isEqualToIgnoringCase(Constants.ResultCode.SUCCESS.getCode());
    softAssertions.assertAll();
    String esn = response2.getString("body.externalSerialNo");

    // Step 4: Verify transaction success
    verifyTransactionSuccess(mid, orderId, TXN_AMOUNT, esn, payeeVpa);
    softAssertions.assertAll();
  }

  //helper methods
  private void verifyTransactionSuccess(String mid, String orderId, String txnAmount,
      String externalSerialNo, String payeeVpa) {

    String bankCode;
    String gatewayName;
    if (payeeVpa.contains("@pty")) {
      bankCode = "PTYES";
      gatewayName = Constants.Gateway.PTYES.toString();
    } else if (payeeVpa.contains("@ptab")) {
      bankCode = "PTAB";
      gatewayName = Constants.Gateway.PPBL.toString();
    } else {
      bankCode = "PPBL";
      gatewayName = Constants.Gateway.PPBL.toString();
    }
    UPISecureResponse secureResponse = new UPISecureResponse().buildRequest(
        "Abhishek Verma",
        "paytmTest@ptys",
        "paytm-956932428@ptybl",
        "Punjab National Bank",
        "PUNB",
        externalSerialNo,
        "SUCCESS",
        String.format("%011d", (long) (Math.random() * 100000000000L)), //rrn
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
        .validateMid(mid)
        .AssertAll();
  }

} 