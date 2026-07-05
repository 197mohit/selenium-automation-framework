package scripts.UPI.OfflineUpi;

// AI-Generated: 2025-01-09 - UPI-PSP E2E test cases for DQR creation with acquirement ID

import com.paytm.LocalConfig;
import com.paytm.api.PaymentService;
import com.paytm.api.UpiPspProcessor;
import com.paytm.appconstants.Constants;
import com.paytm.appconstants.FF4JFeatures;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.LogsValidationHelper;
import com.paytm.apphelpers.PG2LogsValidationHelper;
import com.paytm.apphelpers.QRHelper;
import com.paytm.apphelpers.RedisHelper;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.dto.upiIntent.staticQR.StaticQrUpiPSPRequest;
import com.paytm.utils.ff4j.FF4JFlags;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.qameta.allure.Step;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.SoftAssertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;
import java.util.Arrays;
import java.util.List;

/**
 * Test class for UPI-PSP E2E transaction test cases
 *
 * @author AI-Generated
 * @version 1.0
 * @since 2025-01-09
 */
@Owner(Constants.Owner.ABHISHEK_VERMA)
@Feature("PGP-60897")
public class DQRCreationOnAcqId extends PGPBaseTest {

  // AI-Generated: 2025-01-09 - Logger for proper logging instead of System.out.println
  private static final Logger logger = LoggerFactory.getLogger(DQRCreationOnAcqId.class);

  // AI-Generated: 2025-01-09 - Constants for log validation
  private static final String CREATE_ORDER_AND_PAY = "ACQUIRING_CREATE_ORDER_PAY";
  private static final String PAY_ORDER = "ACQUIRING_PAY_ORDER";
  private static final String INQUIRE_WITH_ACQ_ID = "ACQUIRING_INQUIRE_WITH_ACQ_ID";
  private static final String INQUIRE_WITH_ORDER_ID = "ACQUIRING_ORDER_QUERY_BY_MERCHANT_TRANS_ID";
  // AI-Generated: 2025-01-09 - FF4J flag for acquirement ID QR update
  private static final String ACQUIREMENT_ID_QR_UPDATE_FLAG = FF4JFeatures.PAYMENT_SERVICE_ADD_ACQUIREMENT_ID_IN_DQR;
  // AI-Generated: 2025-01-09 - Test data constants
  private static final String TEST_AMOUNT = "11.00";
  private static final String PAYER_VPA = "9999725804@ptyes";
  private static final String PAYMENT_INSTRUMENT = "UPI_SAVINGS";
  
  // AI-Generated: 2025-01-09 - Allowed TPAP list for QR creation
  private static final List<String> ALLOWED_TPAP = Arrays.asList("PTYES", "PHDFC", "PTAXIS", "PTSBI");


  /**
   * AI-Generated: 2025-01-09 - Helper method to validate acquiring logs
   */
  @Step("Validate acquiring logs for {logType}")
  private String getDownStreamLogs(String orderId) throws InterruptedException {
    return LogsValidationHelper.verifyLogsOnPod(
        PG2LogsValidationHelper.setEnvService.DOWN_STEAM_REQUEST_RESPONSE,
        orderId,
        "ACQUIRING_SERVICE",
        "REQUEST"
    );
  }

  /**
   * AI-Generated: 2025-01-09 - Helper method to validate UPI-PSP processor response
   */
  @Step("Validate UPI-PSP processor response")
  private void validateUpiPspProcessorResponse(Response response) {
    JsonPath jsonPath = response.jsonPath();
    SoftAssertions softAssertions = new SoftAssertions();
    softAssertions.assertThat(jsonPath.getString("body.resultCode"))
        .as("UPI-PSP processor should return success")
        .isEqualTo("SUCCESS");
    softAssertions.assertAll();
  }


  @Test(description = "TC_01: Verify for e2e SQR transaction")
  public void tc01_VerifyE2ESQRTransaction() throws Exception {
    logger.info("Starting TC_01: Verify for e2e SQR transaction");

    // AI-Generated: 2025-01-09 - Generate test data
    String orderId = CommonHelpers.generateOrderId();
    Constants.MerchantType merchantType = Constants.MerchantType.CREDITLINE_DISABLED_SMALL_MERCHANT;
    String mid = merchantType.getId();

    // AI-Generated: 2025-01-09 - Create UPI-PSP request with custom format
    StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId,
        TEST_AMOUNT, merchantType.getVpa(), PAYER_VPA, PAYMENT_INSTRUMENT, "");
    StaticQrUpiPSPRequest customRequest = new StaticQrUpiPSPRequest(builder, PAYMENT_INSTRUMENT,
        orderId);

    // AI-Generated: 2025-01-09 - Execute UPI-PSP processor API
    UpiPspProcessor upiPspProcessor = new UpiPspProcessor(customRequest);
    Response response = upiPspProcessor.execute();

    // AI-Generated: 2025-01-09 - Validate UPI-PSP processor response
    validateUpiPspProcessorResponse(response);

    String acquiringLogs = getDownStreamLogs(orderId);
    System.out.printf("acquiring logs fetched are: " + acquiringLogs);

    SoftAssertions softAssertions = new SoftAssertions();
    softAssertions.assertThat(acquiringLogs).doesNotContain(INQUIRE_WITH_ACQ_ID);
    softAssertions.assertThat(acquiringLogs).contains(CREATE_ORDER_AND_PAY);
    softAssertions.assertThat(acquiringLogs).doesNotContain(PAY_ORDER);
    softAssertions.assertThat(acquiringLogs).doesNotContain(INQUIRE_WITH_ORDER_ID);
    softAssertions.assertAll();
  }


  @Test(description = "TC_02: Verify for e2e SQR transaction retry")
  public void tc02_VerifyE2ESQRTransactionRetry() throws Exception {
    logger.info("Starting TC_02: Verify for e2e SQR transaction retry");

    // AI-Generated: 2025-01-09 - Generate test data
    String orderId = CommonHelpers.generateOrderId();
    Constants.MerchantType merchantType = Constants.MerchantType.CREDITLINE_DISABLED_SMALL_MERCHANT;
    String mid = merchantType.getId();

    // AI-Generated: 2025-01-09 - First UPI-PSP processor call
    StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId,
        TEST_AMOUNT, merchantType.getVpa(), PAYER_VPA, PAYMENT_INSTRUMENT, "");
    StaticQrUpiPSPRequest customRequest = new StaticQrUpiPSPRequest(builder, PAYMENT_INSTRUMENT,
        orderId);

    UpiPspProcessor upiPspProcessor = new UpiPspProcessor(customRequest);
    Response response = upiPspProcessor.execute();
    validateUpiPspProcessorResponse(response);
    // AI-Generated: 2025-01-09 - Validate first call logs
    String firstAcquiringLogs = getDownStreamLogs(orderId);
    SoftAssertions firstSoftAssertions = new SoftAssertions();
    firstSoftAssertions.assertThat(firstAcquiringLogs).doesNotContain(INQUIRE_WITH_ACQ_ID);
    firstSoftAssertions.assertThat(firstAcquiringLogs).doesNotContain(INQUIRE_WITH_ORDER_ID);
    firstSoftAssertions.assertThat(firstAcquiringLogs).contains(CREATE_ORDER_AND_PAY);
    firstSoftAssertions.assertThat(firstAcquiringLogs).doesNotContain(PAY_ORDER);
    firstSoftAssertions.assertAll();


    // AI-Generated: 2025-01-09 - Retry UPI-PSP processor call with same orderId
    StaticQrUpiPSPRequest.Builder retryBuilder = new StaticQrUpiPSPRequest.Builder(mid, orderId,
        TEST_AMOUNT, merchantType.getVpa(), PAYER_VPA, PAYMENT_INSTRUMENT, "");
    StaticQrUpiPSPRequest retryRequest = new StaticQrUpiPSPRequest(retryBuilder, PAYMENT_INSTRUMENT,
        orderId);

    UpiPspProcessor retryProcessor = new UpiPspProcessor(retryRequest);
    Response retryResponse = retryProcessor.execute();
    validateUpiPspProcessorResponse(retryResponse);

    // AI-Generated: 2025-01-09 - Validate retry call logs
    String retryAcquiringLogs = getDownStreamLogs(orderId);
    SoftAssertions retrySoftAssertions = new SoftAssertions();
    retrySoftAssertions.assertThat(retryAcquiringLogs).doesNotContain(INQUIRE_WITH_ACQ_ID);
    retrySoftAssertions.assertThat(retryAcquiringLogs).doesNotContain(INQUIRE_WITH_ORDER_ID);
    retrySoftAssertions.assertThat(retryAcquiringLogs).contains(CREATE_ORDER_AND_PAY);
    retrySoftAssertions.assertThat(retryAcquiringLogs).doesNotContain(PAY_ORDER);
    retrySoftAssertions.assertAll();
  }

  @Test(description = "TC_03: Verify e2e DQR transaction with acquirement id is working")
  public void tc03_VerifyE2EDQRTransactionWithAcquirementId() throws Exception {

    // AI-Generated: 2025-01-09 - Enable FF4J flag for acquirement ID QR update
    FF4JFlags.enable(ACQUIREMENT_ID_QR_UPDATE_FLAG);
    FF4JFlags.enable(ACQUIREMENT_ID_QR_UPDATE_FLAG);

    // AI-Generated: 2025-01-09 - Generate test data
    String orderId = CommonHelpers.generateOrderId();
    Constants.MerchantType merchantType = Constants.MerchantType.DQR_UPI_ACQ_ID_IN_DEEPLINK;
    String mid = merchantType.getId();

    // AI-Generated: 2025-01-09 - Create QR via PaymentService
    PaymentService paymentService = new PaymentService(merchantType, TEST_AMOUNT, orderId, ALLOWED_TPAP);
    Response qrResponse = paymentService.execute();
    JsonPath qrJsonPath = qrResponse.jsonPath();

    // AI-Generated: 2025-01-09 - Validate QR creation response
    SoftAssertions qrSoftAssertions = new SoftAssertions();
    qrSoftAssertions.assertThat(qrJsonPath.getString("body.resultInfo.resultStatus"))
        .as("QR creation should be successful")
        .isEqualTo("SUCCESS");

    String acquirementId = qrJsonPath.getString("body.extendedInfo.acquirementId");
    String qrData = qrJsonPath.getString("body.qrData");

    logger.info("Acquirement ID: {}", acquirementId);

    // AI-Generated: 2025-01-09 - Verify in deeplink received in body.qrData tr value is equal to body.extendedInfo.acquirementId
    qrSoftAssertions.assertThat(qrData)
        .as("QR data should contain acquirement ID")
        .contains(acquirementId);
    
    qrSoftAssertions.assertAll();
    // AI-Generated: 2025-01-09 - Hit UPI-PSP processor API with acquirement ID as orderId
    StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, acquirementId,
        TEST_AMOUNT, QRHelper.parseDeeplinkInfo(qrData).get("payeeVpa"), PAYER_VPA, PAYMENT_INSTRUMENT, "");
    StaticQrUpiPSPRequest customRequest = new StaticQrUpiPSPRequest(builder, PAYMENT_INSTRUMENT,
        acquirementId);

    UpiPspProcessor upiPspProcessor = new UpiPspProcessor(customRequest);
    Response response = upiPspProcessor.execute();
    validateUpiPspProcessorResponse(response);

    // AI-Generated: 2025-01-09 - Validate acquiring logs
    String acquiringLogs = getDownStreamLogs(acquirementId);
    SoftAssertions softAssertions = new SoftAssertions();
    softAssertions.assertThat(acquiringLogs).contains(INQUIRE_WITH_ACQ_ID);
    softAssertions.assertThat(acquiringLogs).doesNotContain(INQUIRE_WITH_ORDER_ID);
    softAssertions.assertThat(acquiringLogs).contains(PAY_ORDER);
    softAssertions.assertThat(acquiringLogs).doesNotContain(CREATE_ORDER_AND_PAY);
    softAssertions.assertAll();
  }

  @Test(description = "TC_04: Verify e2e DQR transaction with orderId is working",priority = -1)
  public void tc04_VerifyE2EDQRTransactionWithOrderId() throws Exception {
    Thread.sleep(10000);
    // AI-Generated: 2025-01-09 - Generate test data
    String orderId = CommonHelpers.generateOrderId();
    Constants.MerchantType merchantType = Constants.MerchantType.DQR_UPI_ACQ_ID_IN_DEEPLINK_FF4J_OFF;
    String mid = merchantType.getId();

    // AI-Generated: 2025-01-09 - Create QR via PaymentService
    PaymentService paymentService = new PaymentService(merchantType, TEST_AMOUNT, orderId, ALLOWED_TPAP);
    Response qrResponse = paymentService.execute();
    JsonPath qrJsonPath = qrResponse.jsonPath();

    // AI-Generated: 2025-01-09 - Validate QR creation response
    SoftAssertions qrSoftAssertions = new SoftAssertions();
    qrSoftAssertions.assertThat(qrJsonPath.getString("body.resultInfo.resultStatus"))
        .as("QR creation should be successful")
        .isEqualTo("SUCCESS");

    String qrCodeId = qrJsonPath.getString("body.qrCodeId");
    String qrData = qrJsonPath.getString("body.qrData");

    logger.info("QR Code ID: {}, Order ID: {}", qrCodeId, orderId);

    // AI-Generated: 2025-01-09 - Verify in deeplink received in body.qrData tr value is equal to orderId
    qrSoftAssertions.assertThat(qrData)
        .as("QR data should contain order ID")
        .contains(orderId);
    
    qrSoftAssertions.assertAll();

    // AI-Generated: 2025-01-09 - Hit UPI-PSP processor API with orderId
    StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId,
        TEST_AMOUNT, QRHelper.parseDeeplinkInfo(qrData).get("payeeVpa"), PAYER_VPA, PAYMENT_INSTRUMENT, "");
    StaticQrUpiPSPRequest customRequest = new StaticQrUpiPSPRequest(builder, PAYMENT_INSTRUMENT,
        orderId);

    UpiPspProcessor upiPspProcessor = new UpiPspProcessor(customRequest);
    Response response = upiPspProcessor.execute();
    validateUpiPspProcessorResponse(response);

    // AI-Generated: 2025-01-09 - Validate acquiring logs
    String acquiringLogs = getDownStreamLogs(orderId);
    SoftAssertions softAssertions = new SoftAssertions();
    softAssertions.assertThat(acquiringLogs).contains(INQUIRE_WITH_ORDER_ID);
    softAssertions.assertThat(acquiringLogs).doesNotContain(INQUIRE_WITH_ACQ_ID);
    softAssertions.assertThat(acquiringLogs).contains(PAY_ORDER);
    softAssertions.assertThat(acquiringLogs).doesNotContain(CREATE_ORDER_AND_PAY);
    softAssertions.assertAll();
  }
}
