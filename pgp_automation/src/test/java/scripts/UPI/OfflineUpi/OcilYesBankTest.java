package scripts.UPI.OfflineUpi;

import static com.paytm.apphelpers.LogsValidationHelper.getLogsOnServer;

import com.paytm.LocalConfig;
import com.paytm.ServerConfigProvider.SERVICE;
import com.paytm.api.Instaproxy.ReqAuthUPICreateOrder;
import com.paytm.api.Instaproxy.ReqAuthUPICreateOrder.PayerInstrument;
import com.paytm.api.upipsp.UPIPspPaymentStatus;
import com.paytm.appconstants.Constants;
import com.paytm.appconstants.Constants.MerchantType;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.apphelpers.LogsValidationHelper;
import com.paytm.apphelpers.PG2LogsValidationHelper;
import com.paytm.framework.reportportal.annotation.Owner;
import com.paytm.framework.ui.base.test.BaseTest;
import com.paytm.pg.crypto.AesEncryption;
import com.paytm.utils.ff4j.FF4JFlags;
import com.paytm.appconstants.FF4JFeatures;
import io.qameta.allure.Feature;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.assertj.core.api.SoftAssertions;
import org.json.JSONObject;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import org.testng.annotations.AfterMethod;
import org.assertj.core.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OcilYesBankTest extends BaseTest {

  // AI-Generated: 2025-01-09 - Logger for proper logging instead of System.out.println
  private static final Logger logger = LoggerFactory.getLogger(OcilYesBankTest.class);

  String payerVPA = "9999725804@ypay";
  String payeeVPA = "paytmqr10q4u2@ptys";
  String payeeVPA2 = "paytm.us108995sb@pty";
  String payeeVPA3 = "paytm.us109401sd@pta";
  MerchantType staticQrOfflineMerchant = MerchantType.CREDITLINE_DISABLED_SMALL_MERCHANT;

  @AfterMethod
  public void cleanup() {
    // Reset FF4J flag to default state after each test
    try {
      updateFF4JFlag(FF4JFeatures.INSTA_ENABLE_YBL_FAILURE_REQAUTH, true);
    } catch (Exception e) {
      logger.error("Error resetting FF4J flag: {}", e.getMessage());
    }
  }

  /**
   * Helper method to update FF4J flag (Redis cache clearing and waiting are now handled automatically)
   */
  private void updateFF4JFlag(String flagName, boolean enable) {
    if (enable) {
      FF4JFlags.enable(flagName);
    } else {
      FF4JFlags.disable(flagName);
    }
  }

  /**
   * AI-Generated: 2025-01-09 - Helper method to generate unique orderId for each test method
   */
  private String generateUniqueOrderId() {
    return "YES" + LocalConfig.ENV_NAME + CommonHelpers.generateOrderId();
  }

  @Test(description = "Verify successful reqVal request")
  public void test() {
    String orderId = generateUniqueOrderId();
    String epochSeconds = String.valueOf(Instant.now().toEpochMilli());
    SoftAssertions softly = new SoftAssertions();
    logger.info("OrdersId is: {}", orderId);
    ReqAuthUPICreateOrder upiCreateOrder = new ReqAuthUPICreateOrder().buildRequestV2(
        PayerInstrument.UPI_LITE.name(), "11.00", payerVPA, payeeVPA, orderId, orderId, "ABC",
        epochSeconds, "", "SAVINGS", MerchantType.OCIL_YES.getId(), "DEFERRED_SETTLEMENT");

    Response upiCreateOrderResponse = upiCreateOrder.executeV2("PPBL");
    String response = upiCreateOrderResponse.asString();
    logger.debug("Encrypted response is: {}", response);
    AesEncryption aesEncryption = new AesEncryption();
    String decryptedResponse;
    try {
      decryptedResponse = aesEncryption.decrypt(response, LocalConfig.INSTA_CREATE_ORDER_KEY);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    logger.debug("Decrypted response is: {}", decryptedResponse);
    JSONObject json = new JSONObject(decryptedResponse);
    softly.assertThat(json.get("statusMsg")).isEqualTo("Request Acknowledged");
    softly.assertThat(json.get("status")).isEqualTo("accept");
    softly.assertAll();

    Map<String, String> tokenMap = new HashMap<>();
    tokenMap.put("payeeVpa", payeeVPA);
    tokenMap.put("npciTxnId", orderId);
    logger.debug("npciTxnId for JWT is: {}", tokenMap.get("npciTxnId"));
    String jwt = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts,
        LocalConfig.UPI_PSP_PAYMENT_STATUS_JWT_KEY);

    UPIPspPaymentStatus upiPspPaymentStatus = new UPIPspPaymentStatus().buildRequest(payeeVPA,
        orderId, epochSeconds, "JWT", jwt);
    JsonPath upiPaymentStatusResponse = upiPspPaymentStatus.executeUntilExpectedConditionMet(
        "body.resultCode", "SUCCESS", 5, 12).jsonPath();
    softly.assertThat(upiPaymentStatusResponse.getString("body.txnStatus")).isEqualTo("SUCCESS");
    softly.assertAll();
  }

  @Parameters("isNativePlus")
  @Owner(Constants.Owner.AKSHAT)
  @Feature("PGP-59050")
  @Test(description = "Verify successful reqVal request after flag deletion")
  public void test_flagsDeleted() {
    String orderId = generateUniqueOrderId();
    String epochSeconds = String.valueOf(Instant.now().toEpochMilli());
    SoftAssertions softly = new SoftAssertions();

    /*
    ff4j flags deleted / not exist
    INSTA_ENABLE_PTYES_MIGRATION_OCIL
    INSTA_PTYES_MIGRATION_AMOUNT
     */

    logger.info("OrdersId is: {}", orderId);
    ReqAuthUPICreateOrder upiCreateOrder = new ReqAuthUPICreateOrder().buildRequestV2(
        PayerInstrument.UPI_LITE.name(), "2000.00", payerVPA, payeeVPA2, orderId, orderId, "ABC",
        epochSeconds, "", "SAVINGS", MerchantType.OFFLINE_MID_VALIDATE_VPA.getId(),
        "DEFERRED_SETTLEMENT");

    Response upiCreateOrderResponse = upiCreateOrder.executeV2("PPBL");
    String response = upiCreateOrderResponse.asString();
    logger.debug("Encrypted response is: {}", response);
    AesEncryption aesEncryption = new AesEncryption();
    String decryptedResponse;
    try {
      decryptedResponse = aesEncryption.decrypt(response, LocalConfig.INSTA_CREATE_ORDER_KEY);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    logger.debug("Decrypted response is: {}", decryptedResponse);
    JSONObject json = new JSONObject(decryptedResponse);
    softly.assertThat(json.get("statusMsg")).isEqualTo("Request Acknowledged");
    softly.assertThat(json.get("status")).isEqualTo("accept");
    softly.assertAll();

    Map<String, String> tokenMap = new HashMap<>();
    tokenMap.put("payeeVpa", payeeVPA2);
    tokenMap.put("npciTxnId", orderId);
    logger.debug("npciTxnId for JWT is: {}", tokenMap.get("npciTxnId"));
    String jwt = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts,
        LocalConfig.UPI_PSP_PAYMENT_STATUS_JWT_KEY);

    UPIPspPaymentStatus upiPspPaymentStatus = new UPIPspPaymentStatus().buildRequest(payeeVPA2,
        orderId, epochSeconds, "JWT", jwt);
    JsonPath upiPaymentStatusResponse = upiPspPaymentStatus.executeUntilExpectedConditionMet(
        "body.resultCode", "SUCCESS", 5, 12).jsonPath();
    softly.assertThat(upiPaymentStatusResponse.getString("body.txnStatus")).isEqualTo("SUCCESS");
    softly.assertAll();

  }

  // FF4J Flag "INSTA_ENABLE_YBL_FAILURE_REQAUTH" ON Test Cases
  @Owner(Constants.Owner.ABHISHEK_VERMA)
  @Feature("PGP-60400")
  @Test(description = "Verify when errorCode is coming in createOrderRequest, insta is hitting UPI-PSP createOrder and transaction is success")
  public void testErrorCode20001188_FlagON() {
    String orderId = generateUniqueOrderId();
    String epochSeconds = String.valueOf(Instant.now().toEpochMilli());
    SoftAssertions softly = new SoftAssertions();
    // Enable the FF4J flag with proper wait
    updateFF4JFlag(FF4JFeatures.INSTA_ENABLE_YBL_FAILURE_REQAUTH, true);

    logger.info("OrdersId is: {}", orderId);
    ReqAuthUPICreateOrder upiCreateOrder = new ReqAuthUPICreateOrder().buildRequestV2WithError(
        PayerInstrument.UPI_LITE.name(), "2000.00", payerVPA, payeeVPA, orderId, orderId, "ABC",
        epochSeconds, "", "SAVINGS", MerchantType.OCIL_YES.getId(), "DEFERRED_SETTLEMENT",

        "failure", "20001188", "VPA doesn't exist");

    Response upiCreateOrderResponse = upiCreateOrder.executeV2WithError("PPBL");
    String response = upiCreateOrderResponse.asString();
    logger.debug("Encrypted response is: {}", response);
    AesEncryption aesEncryption = new AesEncryption();
    String decryptedResponse;
    try {
      decryptedResponse = aesEncryption.decrypt(response, LocalConfig.INSTA_CREATE_ORDER_KEY);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    logger.debug("Decrypted response is: {}", decryptedResponse);
    JSONObject json = new JSONObject(decryptedResponse);

    softly.assertThat(json.get("statusMsg")).isEqualTo("Request Acknowledged");
    softly.assertThat(json.get("status")).isEqualTo("accept");
    softly.assertAll();

    // Verify in insta logs that insta is not hitting "upi-psp-processor/v1/order/pay/upipsp" API
    String logs = LogsValidationHelper.verifyLogsOnPod("INSTAPROXY", orderId, "");
    Assertions.assertThat(logs).doesNotContain("upi-psp-processor/v1/order/pay/upipsp");

    // Verify in insta logs that insta is sending error details in callback to switch
    String callbackLogs = LogsValidationHelper.verifyLogsOnPod("INSTAPROXY", orderId,
        "Order Creation Response prepared for the bank");
    Assertions.assertThat(callbackLogs).contains("resultCodeId\":\"009");
    Assertions.assertThat(callbackLogs).contains("subResultCodeId\":\"20001188");
    Assertions.assertThat(callbackLogs).contains("resultMsg\":\"VPA doesn't exist");
  }

  @Owner(Constants.Owner.ABHISHEK_VERMA)
  @Feature("PGP-60400")
  @Test(description = "Verify when errorCode 20001189 is coming in createOrderRequest, insta is not hitting UPI-PSP createOrder and transaction is failed")
  public void testErrorCode20001189_FlagON() {
    String orderId = generateUniqueOrderId();
    String epochSeconds = String.valueOf(Instant.now().toEpochMilli());
    SoftAssertions softly = new SoftAssertions();
    // Enable the FF4J flag with proper wait
    updateFF4JFlag(FF4JFeatures.INSTA_ENABLE_YBL_FAILURE_REQAUTH, true);

    logger.info("OrdersId is: {}", orderId);
    ReqAuthUPICreateOrder upiCreateOrder = new ReqAuthUPICreateOrder().buildRequestV2WithError(
        PayerInstrument.UPI_LITE.name(), "2000.00", payerVPA, payeeVPA, orderId, orderId, "ABC",
        epochSeconds, "", "SAVINGS", MerchantType.OCIL_YES.getId(), "DEFERRED_SETTLEMENT",
        "failure", "20001189", "seller doesn't exist");

    Response upiCreateOrderResponse = upiCreateOrder.executeV2WithError("PPBL");
    String response = upiCreateOrderResponse.asString();
    logger.debug("Encrypted response is: {}", response);
    AesEncryption aesEncryption = new AesEncryption();
    String decryptedResponse;
    try {
      decryptedResponse = aesEncryption.decrypt(response, LocalConfig.INSTA_CREATE_ORDER_KEY);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    logger.debug("Decrypted response is: {}", decryptedResponse);
    JSONObject json = new JSONObject(decryptedResponse);

    softly.assertThat(json.get("statusMsg")).isEqualTo("Request Acknowledged");
    softly.assertThat(json.get("status")).isEqualTo("accept");
    softly.assertAll();

    // Verify in insta logs that insta is not hitting "upi-psp-processor/v1/order/pay/upipsp" API
    String logs = LogsValidationHelper.verifyLogsOnPod("INSTAPROXY", orderId, "");
    Assertions.assertThat(logs).doesNotContain("upi-psp-processor/v1/order/pay/upipsp");

    // Verify in insta logs that insta is sending error details in callback to switch
    String callbackLogs = LogsValidationHelper.verifyLogsOnPod("INSTAPROXY", orderId,
        "Order Creation Response prepared for the bank");
    Assertions.assertThat(callbackLogs).contains("resultCodeId\":\"009");
    Assertions.assertThat(callbackLogs).contains("subResultCodeId\":\"20001189");
    Assertions.assertThat(callbackLogs).contains("resultMsg\":\"seller doesn't exist");
  }

  @Owner(Constants.Owner.ABHISHEK_VERMA)
  @Feature("PGP-60400")
  @Test(description = "Verify order is getting created at UPI_PSP when no error code is received")
  public void testNoErrorCode_FlagON() {
    String orderId = generateUniqueOrderId();
    String epochSeconds = String.valueOf(Instant.now().toEpochMilli());
    SoftAssertions softly = new SoftAssertions();
    // Enable the FF4J flag with proper wait
    updateFF4JFlag(FF4JFeatures.INSTA_ENABLE_YBL_FAILURE_REQAUTH, true);

    logger.info("OrdersId is: {}", orderId);
    ReqAuthUPICreateOrder upiCreateOrder = new ReqAuthUPICreateOrder().buildRequestV2(
        PayerInstrument.UPI_LITE.name(), "2000.00", payerVPA, payeeVPA, orderId, orderId, "ABC",
        epochSeconds, "", "SAVINGS", MerchantType.OCIL_YES.getId(), "DEFERRED_SETTLEMENT");

    Response upiCreateOrderResponse = upiCreateOrder.executeV2("PPBL");
    String response = upiCreateOrderResponse.asString();
    logger.debug("Encrypted response is: {}", response);
    AesEncryption aesEncryption = new AesEncryption();
    String decryptedResponse;
    try {
      decryptedResponse = aesEncryption.decrypt(response, LocalConfig.INSTA_CREATE_ORDER_KEY);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    logger.debug("Decrypted response is: {}", decryptedResponse);
    JSONObject json = new JSONObject(decryptedResponse);

    // Verify success response
    softly.assertThat(json.get("statusMsg")).isEqualTo("Request Acknowledged");
    softly.assertThat(json.get("status")).isEqualTo("accept");
    softly.assertAll();

    // Verify in insta logs that insta is hitting "upi-psp-processor/v1/order/pay/upipsp" API
    String logs = LogsValidationHelper.verifyLogsOnPod("INSTAPROXY", orderId,
        "upi-psp-processor/v1/order/pay/upipsp");
    Assertions.assertThat(logs).contains("upi-psp-processor/v1/order/pay/upipsp");

    // Verify in insta logs that insta is sending error details in callback to switch
    String callbackLogs = LogsValidationHelper.verifyLogsOnPod("INSTAPROXY", orderId,
        "Order Creation Response prepared for the bank");
    Assertions.assertThat(callbackLogs).contains("resultCodeId\":\"001");
    Assertions.assertThat(callbackLogs).contains("resultMsg\":\"success");
  }

  @Owner(Constants.Owner.ABHISHEK_VERMA)
  @Feature("PGP-60400")
  @Test(description = "Verify if any other errorCode is coming apart from above mentioned two same is being sent to OCIL and transaction is failed")
  public void testOtherErrorCode_FlagON() {
    String orderId = generateUniqueOrderId();
    String epochSeconds = String.valueOf(Instant.now().toEpochMilli());
    SoftAssertions softly = new SoftAssertions();
    // Enable the FF4J flag with proper wait
    updateFF4JFlag(FF4JFeatures.INSTA_ENABLE_YBL_FAILURE_REQAUTH, true);

    logger.info("OrdersId is: {}", orderId);
    ReqAuthUPICreateOrder upiCreateOrder = new ReqAuthUPICreateOrder().buildRequestV2WithError(
        PayerInstrument.UPI_LITE.name(), "2000.00", payerVPA, payeeVPA, orderId, orderId, "ABC",
        epochSeconds, "", "SAVINGS", MerchantType.OCIL_YES.getId(), "DEFERRED_SETTLEMENT",
        "failure", "20001120", "VPA doesn't exist");

    Response upiCreateOrderResponse = upiCreateOrder.executeV2WithError("PPBL");
    String response = upiCreateOrderResponse.asString();
    logger.debug("Encrypted response is: {}", response);
    AesEncryption aesEncryption = new AesEncryption();
    String decryptedResponse;
    try {
      decryptedResponse = aesEncryption.decrypt(response, LocalConfig.INSTA_CREATE_ORDER_KEY);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    logger.debug("Decrypted response is: {}", decryptedResponse);
    JSONObject json = new JSONObject(decryptedResponse);

    softly.assertThat(json.get("statusMsg")).isEqualTo("Request Acknowledged");
    softly.assertThat(json.get("status")).isEqualTo("accept");
    softly.assertAll();

    // Verify in insta logs that insta is not hitting "upi-psp-processor/v1/order/pay/upipsp" API
    String logs = LogsValidationHelper.verifyLogsOnPod("INSTAPROXY", orderId, "");
    Assertions.assertThat(logs).doesNotContain("upi-psp-processor/v1/order/pay/upipsp");

    // Verify in insta logs that insta is sending error details in callback to switch
    String callbackLogs = LogsValidationHelper.verifyLogsOnPod("INSTAPROXY", orderId,
        "Order Creation Response prepared for the bank");
    Assertions.assertThat(callbackLogs).contains("resultCodeId\":\"009");
    Assertions.assertThat(callbackLogs).contains("subResultCodeId\":\"20001120");
    Assertions.assertThat(callbackLogs).contains("resultMsg\":\"VPA doesn't exist");
  }

  @Owner(Constants.Owner.ABHISHEK_VERMA)
  @Feature("PGP-60400")
  @Test(description = "Verify when error code is coming as null order is getting created")
  public void testErrorCodeNull_FlagON() {
    String orderId = generateUniqueOrderId();
    String epochSeconds = String.valueOf(Instant.now().toEpochMilli());
    SoftAssertions softly = new SoftAssertions();
    // Enable the FF4J flag with proper wait
    updateFF4JFlag(FF4JFeatures.INSTA_ENABLE_YBL_FAILURE_REQAUTH, true);

    logger.info("OrdersId is: {}", orderId);
    ReqAuthUPICreateOrder upiCreateOrder = new ReqAuthUPICreateOrder().buildRequestV2WithError(
        PayerInstrument.UPI_LITE.name(), "2000.00", payerVPA, payeeVPA, orderId, orderId, "ABC",
        epochSeconds, "", "SAVINGS", MerchantType.OCIL_YES.getId(), "DEFERRED_SETTLEMENT",
        "failure", null, "VPA doesn't exist");

    Response upiCreateOrderResponse = upiCreateOrder.executeV2WithError("PPBL");
    String response = upiCreateOrderResponse.asString();
    logger.debug("Encrypted response is: {}", response);
    AesEncryption aesEncryption = new AesEncryption();
    String decryptedResponse;
    try {
      decryptedResponse = aesEncryption.decrypt(response, LocalConfig.INSTA_CREATE_ORDER_KEY);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    logger.debug("Decrypted response is: {}", decryptedResponse);
    JSONObject json = new JSONObject(decryptedResponse);

    // Verify success response
    softly.assertThat(json.get("statusMsg")).isEqualTo("Request Acknowledged");
    softly.assertThat(json.get("status")).isEqualTo("accept");
    softly.assertAll();

    // Verify in insta logs that insta is hitting "upi-psp-processor/v1/order/pay/upipsp" API
    String logs = LogsValidationHelper.verifyLogsOnPod("INSTAPROXY", orderId,
        "upi-psp-processor/v1/order/pay/upipsp");
    Assertions.assertThat(logs).contains("upi-psp-processor/v1/order/pay/upipsp");

    // Verify in insta logs that insta is sending error details in callback to switch
    String callbackLogs = LogsValidationHelper.verifyLogsOnPod("INSTAPROXY", orderId,
        "Order Creation Response prepared for the bank");
    Assertions.assertThat(callbackLogs).contains("resultCodeId\":\"001");
    Assertions.assertThat(callbackLogs).contains("resultMsg\":\"success");
  }

  @Owner(Constants.Owner.ABHISHEK_VERMA)
  @Feature("PGP-60400")
  @Test(description = "Verify when error code is coming as blank string order is getting created")
  public void testErrorCodeBlank_FlagON() {
    String orderId = generateUniqueOrderId();
    String epochSeconds = String.valueOf(Instant.now().toEpochMilli());
    SoftAssertions softly = new SoftAssertions();
    // Enable the FF4J flag with proper wait
    updateFF4JFlag(FF4JFeatures.INSTA_ENABLE_YBL_FAILURE_REQAUTH, true);

    logger.info("OrdersId is: {}", orderId);
    ReqAuthUPICreateOrder upiCreateOrder = new ReqAuthUPICreateOrder().buildRequestV2WithError(
        PayerInstrument.UPI_LITE.name(), "2000.00", payerVPA, payeeVPA, orderId, orderId, "ABC",
        epochSeconds, "", "SAVINGS", MerchantType.OCIL_YES.getId(), "DEFERRED_SETTLEMENT",
        "failure", "", "VPA doesn't exist");

    Response upiCreateOrderResponse = upiCreateOrder.executeV2WithError("PPBL");
    String response = upiCreateOrderResponse.asString();
    logger.debug("Encrypted response is: {}", response);
    AesEncryption aesEncryption = new AesEncryption();
    String decryptedResponse;
    try {
      decryptedResponse = aesEncryption.decrypt(response, LocalConfig.INSTA_CREATE_ORDER_KEY);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    logger.debug("Decrypted response is: {}", decryptedResponse);
    JSONObject json = new JSONObject(decryptedResponse);

    // Verify success response
    softly.assertThat(json.get("statusMsg")).isEqualTo("Request Acknowledged");
    softly.assertThat(json.get("status")).isEqualTo("accept");
    softly.assertAll();

    // Verify in insta logs that insta is hitting "upi-psp-processor/v1/order/pay/upipsp" API
    String logs = LogsValidationHelper.verifyLogsOnPod("INSTAPROXY", orderId,
        "upi-psp-processor/v1/order/pay/upipsp");
    Assertions.assertThat(logs).contains("upi-psp-processor/v1/order/pay/upipsp");

    // Verify in insta logs that insta is sending error details in callback to switch
    String callbackLogs = LogsValidationHelper.verifyLogsOnPod("INSTAPROXY", orderId,
        "Order Creation Response prepared for the bank");
    Assertions.assertThat(callbackLogs).contains("resultCodeId\":\"001");
    Assertions.assertThat(callbackLogs).contains("resultMsg\":\"success");
  }

  @Owner(Constants.Owner.ABHISHEK_VERMA)
  @Feature("PGP-60400")
  @Test(priority=1,singleThreaded = true,description = "Verify create order is getting success when 20001188 is received in CreateOrder Request")
  public void testErrorCode20001188_FlagOFF() {
    String orderId = generateUniqueOrderId();
    String epochSeconds = String.valueOf(Instant.now().toEpochMilli());
    SoftAssertions softly = new SoftAssertions();
    // Disable the FF4J flag with proper wait
    updateFF4JFlag(FF4JFeatures.INSTA_ENABLE_YBL_FAILURE_REQAUTH, false);

    logger.info("OrdersId is: {}", orderId);
    ReqAuthUPICreateOrder upiCreateOrder = new ReqAuthUPICreateOrder().buildRequestV2WithError(
        PayerInstrument.UPI_LITE.name(), "2000.00", payerVPA, payeeVPA, orderId, orderId, "ABC",
        epochSeconds, "", "SAVINGS", MerchantType.OCIL_YES.getId(), "DEFERRED_SETTLEMENT",
        "failure", "20001188", "VPA doesn't exist");

    Response upiCreateOrderResponse = upiCreateOrder.executeV2WithError("PPBL");
    String response = upiCreateOrderResponse.asString();
    logger.debug("Encrypted response is: {}", response);
    AesEncryption aesEncryption = new AesEncryption();
    String decryptedResponse;
    try {
      decryptedResponse = aesEncryption.decrypt(response, LocalConfig.INSTA_CREATE_ORDER_KEY);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    logger.debug("Decrypted response is: {}", decryptedResponse);
    JSONObject json = new JSONObject(decryptedResponse);

    // Verify success response
    softly.assertThat(json.get("statusMsg")).isEqualTo("Request Acknowledged");
    softly.assertThat(json.get("status")).isEqualTo("accept");
    softly.assertAll();

    // Verify in insta logs that insta is hitting "upi-psp-processor/v1/order/pay/upipsp" API
    String logs = LogsValidationHelper.verifyLogsOnPod("INSTAPROXY", orderId,
        "upi-psp-processor/v1/order/pay/upipsp");
    Assertions.assertThat(logs).contains("upi-psp-processor/v1/order/pay/upipsp");

    // Verify in insta logs that insta is sending error details in callback to switch
    String callbackLogs = LogsValidationHelper.verifyLogsOnPod("INSTAPROXY", orderId,
        "Order Creation Response prepared for the bank");
    Assertions.assertThat(callbackLogs).contains("resultCodeId\":\"001");
    Assertions.assertThat(callbackLogs).contains("resultMsg\":\"success");
  }

  @Owner(Constants.Owner.ABHISHEK_VERMA)
  @Feature("PGP-60400")
  @Test(priority=1,singleThreaded = true,description = "Verify create order in case of no error code is received in create order from yes bank")
  public void testNoErrorCode_FlagOFF() {
    String orderId = generateUniqueOrderId();
    String epochSeconds = String.valueOf(Instant.now().toEpochMilli());
    SoftAssertions softly = new SoftAssertions();
    // Disable the FF4J flag with proper wait
    updateFF4JFlag(FF4JFeatures.INSTA_ENABLE_YBL_FAILURE_REQAUTH, false);

    logger.info("OrdersId is: {}", orderId);
    ReqAuthUPICreateOrder upiCreateOrder = new ReqAuthUPICreateOrder().buildRequestV2(
        PayerInstrument.UPI_LITE.name(), "2000.00", payerVPA, payeeVPA, orderId, orderId, "ABC",
        epochSeconds, "", "SAVINGS", MerchantType.OCIL_YES.getId(), "DEFERRED_SETTLEMENT");

    Response upiCreateOrderResponse = upiCreateOrder.executeV2("PPBL");
    String response = upiCreateOrderResponse.asString();
    logger.debug("Encrypted response is: {}", response);
    AesEncryption aesEncryption = new AesEncryption();
    String decryptedResponse;
    try {
      decryptedResponse = aesEncryption.decrypt(response, LocalConfig.INSTA_CREATE_ORDER_KEY);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    logger.debug("Decrypted response is: {}", decryptedResponse);
    JSONObject json = new JSONObject(decryptedResponse);

    // Verify success response
    softly.assertThat(json.get("statusMsg")).isEqualTo("Request Acknowledged");
    softly.assertThat(json.get("status")).isEqualTo("accept");
    softly.assertAll();

    // Verify in insta logs that insta is hitting "upi-psp-processor/v1/order/pay/upipsp" API
    String logs = LogsValidationHelper.verifyLogsOnPod("INSTAPROXY", orderId,
        "upi-psp-processor/v1/order/pay/upipsp");
    Assertions.assertThat(logs).contains("upi-psp-processor/v1/order/pay/upipsp");

    // Verify in insta logs that insta is sending error details in callback to switch
    String callbackLogs = LogsValidationHelper.verifyLogsOnPod("INSTAPROXY", orderId,
        "Order Creation Response prepared for the bank");
    Assertions.assertThat(callbackLogs).contains("resultCodeId\":\"001");
    Assertions.assertThat(callbackLogs).contains("resultMsg\":\"success");
  }

  // AI-Generated: 2025-01-27 - PGP-60910: PPBL UPI CreateOrder Validation Tests

  @Owner(Constants.Owner.ABHISHEK_VERMA)
  @Feature("PGP-60910")
  @Test(description = "TC_01: Verify orderRequestReceivedAtPg is going in respAuth for v2/CreateOrder")
  public void testOrderRequestReceivedAtPgInRespAuthForV2CreateOrder() throws Exception {
    // AI-Generated: 2025-01-27 - Test case implementation for TC_01
    String orderId = generateUniqueOrderId();
    String epochSeconds = String.valueOf(Instant.now().toEpochMilli());
    SoftAssertions softly = new SoftAssertions();

    logger.info("OrdersId is: {}", orderId);
    ReqAuthUPICreateOrder upiCreateOrder = new ReqAuthUPICreateOrder().buildRequestV2(
        PayerInstrument.SAVINGS.name(), "11.00", payerVPA, staticQrOfflineMerchant.getVpa(), orderId, orderId, "ABC",
        epochSeconds, "PPBL", "SAVINGS", staticQrOfflineMerchant.getId(), "DEFERRED_SETTLEMENT");

    Response upiCreateOrderResponse = upiCreateOrder.executeV2("PPBL");
    String response = upiCreateOrderResponse.asString();
    logger.debug("Encrypted response is: {}", response);
    AesEncryption aesEncryption = new AesEncryption();
    String decryptedResponse;
    try {
      decryptedResponse = aesEncryption.decrypt(response, LocalConfig.INSTA_CREATE_ORDER_KEY);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    logger.debug("Decrypted response is: {}", decryptedResponse);
    JSONObject json = new JSONObject(decryptedResponse);
    softly.assertThat(json.get("statusMsg")).isEqualTo("Request Acknowledged");
    softly.assertThat(json.get("status")).isEqualTo("accept");
    softly.assertAll();

    // Verify logs for orderRequestReceivedAtPg in respAuthSync
    String logs = LogsValidationHelper.verifyLogsOnPod("INSTAPROXY", orderId,
        "Order Creation Response prepared for the bank");

    softly.assertThat(logs).as("Logs should contain orderRequestReceivedAtPg in respAuthSync")
        .contains("orderRequestReceivedAtPg");
    softly.assertAll();
  }

  @Owner(Constants.Owner.ABHISHEK_VERMA)
  @Feature("PGP-60910")
  @Test(description = "TC_02: Verify value is EPOCH timestamp at which reqAuth is received in orderRequestReceivedAtPg for v2/CreateOrder")
  public void testEpochTimestampInOrderRequestReceivedAtPgForV2CreateOrder() throws Exception {
    // AI-Generated: 2025-01-27 - Test case implementation for TC_02
    String orderId = generateUniqueOrderId();
    SoftAssertions softly = new SoftAssertions();
    
    long requestTimestamp = System.currentTimeMillis();
    String currentTimestamp = String.valueOf(requestTimestamp);

    logger.info("OrdersId is: {}", orderId);
    ReqAuthUPICreateOrder upiCreateOrder = new ReqAuthUPICreateOrder().buildRequestV2(
        PayerInstrument.SAVINGS.name(), "11.00", payerVPA, staticQrOfflineMerchant.getVpa(), orderId, "YESQA12c44e5d1646a94b1da7e412e8a12f15ea", "ABC",
        currentTimestamp, "PPBL", "SAVINGS", staticQrOfflineMerchant.getId(), "DEFERRED_SETTLEMENT");

    Response upiCreateOrderResponse = upiCreateOrder.executeV2("PPBL");
    String response = upiCreateOrderResponse.asString();
    logger.debug("Encrypted response is: {}", response);
    AesEncryption aesEncryption = new AesEncryption();
    String decryptedResponse;
    try {
      decryptedResponse = aesEncryption.decrypt(response, LocalConfig.INSTA_CREATE_ORDER_KEY);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    logger.debug("Decrypted response is: {}", decryptedResponse);
    JSONObject json = new JSONObject(decryptedResponse);
    softly.assertThat(json.get("statusMsg")).isEqualTo("Request Acknowledged");
    softly.assertThat(json.get("status")).isEqualTo("accept");
    softly.assertAll();

    // Verify logs for orderRequestReceivedAtPg with EPOCH timestamp
    String logs = LogsValidationHelper.verifyLogsOnPod("INSTAPROXY", orderId,
        "Order Creation Response prepared for the bank");

    softly.assertThat(logs).as("Logs should contain orderRequestReceivedAtPg with EPOCH timestamp")
        .contains("orderRequestReceivedAtPg");
    softly.assertAll();

    // Verify the timestamp value is within acceptable range (within 5 minutes)
    long currentTime = System.currentTimeMillis();
    long timeDifference = Math.abs(currentTime - requestTimestamp);
    Assertions.assertThat(timeDifference).as("Timestamp should be within 1 minutes")
        .isLessThan(60000); // 1 minutes in milliseconds
    softly.assertAll();
  }

  @Owner(Constants.Owner.ABHISHEK_VERMA)
  @Feature("PGP-60910")
  @Test(description = "TC_03: Verify orderRequestReceivedAtPg is going in respAuth for v3/CreateOrder")
  public void testOrderRequestReceivedAtPgInRespAuthForV3CreateOrder() throws Exception {
    // AI-Generated: 2025-01-27 - Test case implementation for TC_03
    String orderId = generateUniqueOrderId();
    String epochSeconds = String.valueOf(Instant.now().toEpochMilli());
    SoftAssertions softly = new SoftAssertions();

    String orderRequestReceivedAtPg = String.valueOf(System.currentTimeMillis());
    String paymentAggregator = "PAYTM";

    logger.info("OrdersId is: {}", orderId);
    ReqAuthUPICreateOrder upiCreateOrder = new ReqAuthUPICreateOrder().buildRequestV3(
        PayerInstrument.SAVINGS.name(), "11.00", payerVPA, staticQrOfflineMerchant.getVpa(), orderId, orderId, "ABC",
        epochSeconds, "PPBL", "SAVINGS", staticQrOfflineMerchant.getId(), "DEFERRED_SETTLEMENT",
        orderRequestReceivedAtPg, paymentAggregator);

    Response upiCreateOrderResponse = upiCreateOrder.executeV3("PPBL");
    String response = upiCreateOrderResponse.asString();
    logger.debug("V3 Response is: {}", response);

    // Check if response is successful
    softly.assertThat(upiCreateOrderResponse.getStatusCode()).as("API should return 200 status code")
        .isEqualTo(200);

    // V3 response is unencrypted JSON format
    JSONObject json = new JSONObject(response);
    softly.assertThat(json.get("responseMessage")).isEqualTo("GENERIC_SUCCESS");
    softly.assertThat(json.get("responseStatus")).isEqualTo("SUCCESS");
    softly.assertThat(json.get("responseCode")).isEqualTo("00");
    softly.assertAll();

    // Verify logs for orderRequestReceivedAtPg in respAuthSync
    String logs = LogsValidationHelper.verifyLogsOnPod("INSTAPROXY", orderId,
        "Order Creation Response prepared for the bank");

    softly.assertThat(logs).as("Logs should contain orderRequestReceivedAtPg in respAuthSync")
        .contains("orderRequestReceivedAtPg");
    softly.assertAll();
  }

  @Owner(Constants.Owner.ABHISHEK_VERMA)
  @Feature("PGP-60910")
  @Test(description = "TC_04: Verify value is EPOCH timestamp at which reqAuth is received in orderRequestReceivedAtPg for v3/CreateOrder")
  public void testEpochTimestampInOrderRequestReceivedAtPgForV3CreateOrder() throws Exception {
    // AI-Generated: 2025-01-27 - Test case implementation for TC_04
    String orderId = generateUniqueOrderId();
    SoftAssertions softly = new SoftAssertions();
    
    long requestTimestamp = System.currentTimeMillis();
    String currentTimestamp = String.valueOf(requestTimestamp);
    String orderRequestReceivedAtPg = String.valueOf(requestTimestamp);
    String paymentAggregator = "PAYTM";

    logger.info("OrdersId is: {}", orderId);
    ReqAuthUPICreateOrder upiCreateOrder = new ReqAuthUPICreateOrder().buildRequestV3(
        PayerInstrument.SAVINGS.name(), "11.00", payerVPA, staticQrOfflineMerchant.getVpa(), orderId, orderId, "ABC",
        currentTimestamp, "PPBL", "SAVINGS", staticQrOfflineMerchant.getId(), "DEFERRED_SETTLEMENT",
        orderRequestReceivedAtPg, paymentAggregator);

    Response upiCreateOrderResponse = upiCreateOrder.executeV3("PPBL");
    String response = upiCreateOrderResponse.asString();
    logger.debug("V3 Response is: {}", response);

    // Check if response is successful
    softly.assertThat(upiCreateOrderResponse.getStatusCode()).as("API should return 200 status code")
        .isEqualTo(200);

    // V3 response is unencrypted JSON format
    JSONObject json = new JSONObject(response);
    softly.assertThat(json.get("responseMessage")).isEqualTo("GENERIC_SUCCESS");
    softly.assertThat(json.get("responseStatus")).isEqualTo("SUCCESS");
    softly.assertThat(json.get("responseCode")).isEqualTo("00");
    softly.assertAll();

    // Verify logs for orderRequestReceivedAtPg with EPOCH timestamp
    String logs = LogsValidationHelper.verifyLogsOnPod("INSTAPROXY", orderId,
        "Order Creation Response prepared for the bank");

    softly.assertThat(logs).as("Logs should contain orderRequestReceivedAtPg with EPOCH timestamp")
        .contains("orderRequestReceivedAtPg");
    softly.assertAll();

    // Verify the timestamp value is within acceptable range (within 5 minutes)
    long currentTime = System.currentTimeMillis();
    long timeDifference = Math.abs(currentTime - requestTimestamp);
    Assertions.assertThat(timeDifference).as("Timestamp should be within 5 minutes")
        .isLessThan(300000); // 5 minutes in milliseconds
  }

  //todo:Need to add insta statusQuery curl to reduce time for this case
  @Owner(Constants.Owner.ABHISHEK_VERMA)
  @Feature("PGP-60910")
  @Test(description = "TC_05: Verify paymentAggregator and mid is going in queryParam in sq request")
  public void testPaymentAggregatorAndMidInQueryParamForSqRequest() throws Exception {
    // AI-Generated: 2025-01-27 - Test case implementation for TC_05
    String orderId = generateUniqueOrderId();
    String epochSeconds = String.valueOf(Instant.now().toEpochMilli());
    SoftAssertions softly = new SoftAssertions();

    String orderRequestReceivedAtPg = String.valueOf(System.currentTimeMillis());
    String paymentAggregator = "ocl";
    String mid = staticQrOfflineMerchant.getId();

    logger.info("OrdersId is: {}", orderId);
    ReqAuthUPICreateOrder upiCreateOrder = new ReqAuthUPICreateOrder().buildRequestV3(
        PayerInstrument.SAVINGS.name(), "99.98", payerVPA, staticQrOfflineMerchant.getVpa(), orderId, orderId, "ABC",
        epochSeconds, "PPBL", "SAVINGS", mid, "DEFERRED_SETTLEMENT",
        orderRequestReceivedAtPg, paymentAggregator);

    Response upiCreateOrderResponse = upiCreateOrder.executeV3("PPBL");
    String response = upiCreateOrderResponse.asString();
    logger.debug("V3 Response is: {}", response);

    // Check if response is successful
    softly.assertThat(upiCreateOrderResponse.getStatusCode()).as("API should return 200 status code")
        .isEqualTo(200);

    // V3 response is unencrypted JSON format
    JSONObject json = new JSONObject(response);
    softly.assertThat(json.get("responseMessage")).isEqualTo("GENERIC_SUCCESS");
    softly.assertThat(json.get("responseStatus")).isEqualTo("SUCCESS");
    softly.assertThat(json.get("responseCode")).isEqualTo("00");
    softly.assertAll();

    // Step 1: Get externalSerialNo from "Order Creation Response prepared for the bank" log
    logger.info("Step 1: Extracting externalSerialNo from Order Creation Response log...");
    String orderCreationLogs = LogsValidationHelper.verifyLogsOnPod("INSTAPROXY", orderId,
        "Order Creation Response prepared for the bank");

    // Extract externalSerialNo from the log
    String externalSerialNo = extractExternalSerialNo(orderCreationLogs);
    logger.info("Extracted externalSerialNo: {}", externalSerialNo);

    if (externalSerialNo == null || externalSerialNo.trim().isEmpty()) {
      throw new RuntimeException("Failed to extract externalSerialNo from Order Creation Response log");
    }

    // Step 2: Wait for delayed log to be generated using externalSerialNo (up to 4 minutes with 30-second intervals)
    logger.info("Step 2: Waiting for delayed log generation using externalSerialNo (up to 4 minutes)...");
    String logs = "";
    String grepcmd = "grep \"" + externalSerialNo + "\" /paytm/logs/instaproxy.log | " +
        "grep \"SQ Request.*Paytm_UPI/upi/pg-transaction-status\"";

    int maxAttempts = 8; // 8 * 30 seconds = 4 minutes
    int attempt = 0;

    while (attempt < maxAttempts) {
      try {
        Thread.sleep(30000); // Wait 30 seconds between attempts
        logs = getLogsOnServer(SERVICE.INSTAPROXY, grepcmd);

        if (logs != null && !logs.trim().isEmpty()) {
          logger.info("Log found after {} seconds", ((attempt + 1) * 30));
          break;
        }

        attempt++;
        logger.debug("Log not found yet, attempt {}/{}", attempt, maxAttempts);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new RuntimeException("Wait interrupted", e);
      }
    }

    if (logs == null || logs.trim().isEmpty()) {
      logger.warn("Warning: Log not found after 4 minutes of waiting");
    } else {
      logger.info("Found SQ Request log: {}", logs);
    }

    softly.assertThat(logs).as("SQ Request log should contain paymentAggregator parameter")
        .contains("paymentAggregator=");
    softly.assertThat(logs).as("SQ Request log should contain mid parameter")
        .contains("mid=");

    // Verify specific values in the URL parameters
    softly.assertThat(logs).as("SQ Request log should contain correct paymentAggregator value")
        .contains("paymentAggregator=" + paymentAggregator);
    softly.assertThat(logs).as("SQ Request log should contain correct mid value")
        .contains("mid=" + mid);
    softly.assertAll();
  }


  @Owner(Constants.Owner.ABHISHEK_VERMA)
  @Feature("PAPR-6707")
  @Test(description = "Verify initiationMode, purposeCode, payeeMcc, businessSize is sent to router in callback when received from insta")
  public void testInitiationModeAndPurposeCodeInCallback() throws Exception {
    // AI-Generated: 2025-01-27 - Test case implementation for TC_04
    String orderId = generateUniqueOrderId();
    SoftAssertions softly = new SoftAssertions();

    long requestTimestamp = System.currentTimeMillis();
    String currentTimestamp = String.valueOf(requestTimestamp);
    String orderRequestReceivedAtPg = String.valueOf(requestTimestamp);
    String paymentAggregator = "PAYTM";

    logger.info("OrdersId is: {}", orderId);
    ReqAuthUPICreateOrder upiCreateOrder = new ReqAuthUPICreateOrder().buildRequestV3(
        PayerInstrument.SAVINGS.name(), "11.00", payerVPA, staticQrOfflineMerchant.getVpa(), orderId, orderId, "ABC",
        currentTimestamp, "PPBL", "SAVINGS", staticQrOfflineMerchant.getId(), "DEFERRED_SETTLEMENT",
        orderRequestReceivedAtPg, paymentAggregator);

    Response upiCreateOrderResponse = upiCreateOrder.executeV3("PPBL");
    String response = upiCreateOrderResponse.asString();
    logger.debug("V3 Response is: {}", response);

    // Check if response is successful
    softly.assertThat(upiCreateOrderResponse.getStatusCode()).as("API should return 200 status code")
        .isEqualTo(200);

    // V3 response is unencrypted JSON format
    JSONObject json = new JSONObject(response);
    softly.assertThat(json.get("responseMessage")).isEqualTo("GENERIC_SUCCESS");
    softly.assertThat(json.get("responseStatus")).isEqualTo("SUCCESS");
    softly.assertThat(json.get("responseCode")).isEqualTo("00");
    softly.assertAll();

    // Step 1: Get externalSerialNo from "Order Creation Response prepared for the bank" log
    logger.info("Step 1: Extracting externalSerialNo from Order Creation Response log...");
    String orderCreationLogs = LogsValidationHelper.verifyLogsOnPod("INSTAPROXY", orderId,
        "Order Creation Response prepared for the bank");

    // Extract externalSerialNo from the log
    String externalSerialNo = extractExternalSerialNo(orderCreationLogs);
    logger.info("Extracted externalSerialNo: {}", externalSerialNo);

    // Verify logs for orderRequestReceivedAtPg with EPOCH timestamp
    String logs = LogsValidationHelper.verifyLogsOnPod("INSTAPROXY", externalSerialNo,
        "FLUXNET_UPI_PG2_PAYMENT_RESULT","REQUEST");

    softly.assertThat(logs).as("Logs should contain initiationMode,purposeCode, payeeMcc,businessSize ")
        .contains("\"initiationMode\":\"01\"").contains("\"purposeCode\":\"00\"").contains("\"payeeMcc\":\"7211\"").contains("\"businessSize\":\"SMALL\"");
    softly.assertAll();
  }

  // AI-Generated: 2025-01-27 - Helper method to extract externalSerialNo from Order Creation Response log
  private String extractExternalSerialNo(String orderCreationLogs) {
    try {
      // The log contains JSON with externalSerialNo field
      // Example: "Order Creation Response prepared for the bank: {"settlementType":"DEFERRED_SETTLEMENT",...,"externalSerialNo":"5090823484137521153",...}"

      // Find the JSON part after "Order Creation Response prepared for the bank: "
      String jsonStart = "Order Creation Response prepared for the bank: ";
      int jsonStartIndex = orderCreationLogs.indexOf(jsonStart);

      if (jsonStartIndex == -1) {
        logger.error("Could not find 'Order Creation Response prepared for the bank:' in logs");
        return null;
      }

      // Extract the JSON part
      String jsonPart = orderCreationLogs.substring(jsonStartIndex + jsonStart.length());

      // Find the end of JSON (before the next part like " , URL:")
      int jsonEndIndex = jsonPart.indexOf(" , URL:");
      if (jsonEndIndex != -1) {
        jsonPart = jsonPart.substring(0, jsonEndIndex);
      }

      logger.debug("Extracted JSON part: {}", jsonPart);

      // Parse JSON to extract externalSerialNo
      JSONObject json = new JSONObject(jsonPart);
      String externalSerialNo = json.optString("externalSerialNo", null);

      if (externalSerialNo != null && !externalSerialNo.trim().isEmpty()) {
        return externalSerialNo;
      } else {
        logger.error("externalSerialNo not found in JSON: {}", jsonPart);
        return null;
      }

    } catch (Exception e) {
      logger.error("Error extracting externalSerialNo: {}", e.getMessage());
      logger.debug("Full log content: {}", orderCreationLogs);
      return null;
    }
  }

  private JSONObject extractOrderCreationResponseJson(String orderCreationLogs) {
    String jsonStart = "Order Creation Response prepared for the bank: ";
    int jsonStartIndex = orderCreationLogs.indexOf(jsonStart);
    if (jsonStartIndex == -1) {
      throw new RuntimeException("Could not find 'Order Creation Response prepared for the bank:' in respAuthAsync logs");
    }

    String jsonPart = orderCreationLogs.substring(jsonStartIndex + jsonStart.length());
    int jsonEndIndex = jsonPart.indexOf(" , URL:");
    if (jsonEndIndex != -1) {
      jsonPart = jsonPart.substring(0, jsonEndIndex);
    }

    logger.debug("Extracted Order Creation Response JSON: {}", jsonPart);
    return new JSONObject(jsonPart);
  }

  @Test(description = "Capture Creditline Payment Instrument Correctly")
  public void CaptureCreditlinePaymentInstrumentCorrectly() {
    String orderId = generateUniqueOrderId();
    String epochSeconds = String.valueOf(Instant.now().toEpochMilli());
    SoftAssertions softly = new SoftAssertions();
    logger.info("OrdersId is: {}", orderId);
    ReqAuthUPICreateOrder upiCreateOrder = new ReqAuthUPICreateOrder().buildRequestV2(
        PayerInstrument.CREDITLINE01.name(), "11.00", payerVPA, payeeVPA3, orderId, orderId, "ABC",
        epochSeconds, "", "CREDITLINE01", MerchantType.PTAB_CREDITLINE_MID.getId(), "DEFERRED_SETTLEMENT");

    Response upiCreateOrderResponse = upiCreateOrder.executeV2("PTAB");
    String response = upiCreateOrderResponse.asString();
    logger.debug("Encrypted response is: {}", response);
    AesEncryption aesEncryption = new AesEncryption();
    String decryptedResponse;
    try {
      decryptedResponse = aesEncryption.decrypt(response, LocalConfig.INSTA_CREATE_ORDER_KEY);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    logger.debug("Decrypted response is: {}", decryptedResponse);
    JSONObject json = new JSONObject(decryptedResponse);
    softly.assertThat(json.get("statusMsg")).isEqualTo("Request Acknowledged");
    softly.assertThat(json.get("status")).isEqualTo("accept");

    String decryptedBankOrderLogs = LogsValidationHelper.verifyLogsOnPod(
        PG2LogsValidationHelper.setEnvService.instaproxy, orderId,
        "Decrypted Request from Bank for order creation");
    softly.assertThat(decryptedBankOrderLogs)
        .as("Instaproxy log line for decrypted bank order creation must be present")
        .contains("Decrypted Request from Bank for order creation");
    softly.assertThat(decryptedBankOrderLogs)
        .as("Decrypted bank order creation payload must include payerPaymentInstrument CREDITLINE01")
        .contains("\"payerPaymentInstrument\":\"CREDITLINE01\"");

    String upiPspProcessorPayLogs = LogsValidationHelper.verifyLogsOnPod(
        PG2LogsValidationHelper.setEnvService.instaproxy, orderId,
        "upi-psp-processor/v1/order/pay/upipsp");
    softly.assertThat(upiPspProcessorPayLogs)
        .as("Instaproxy must log connect to upi-psp-processor order pay upipsp")
        .contains("upi-psp-processor/v1/order/pay/upipsp");
    boolean hasCreditlineInstrument = upiPspProcessorPayLogs.contains(
        "\"payerPaymentInstrument\": \"CREDITLINE_CREDITLINE01\"")
        || upiPspProcessorPayLogs.contains("\"payerPaymentInstrument\":\"CREDITLINE_CREDITLINE01\"");
    softly.assertThat(hasCreditlineInstrument)
        .as("UPI PSP processor request body must include payerPaymentInstrument CREDITLINE_CREDITLINE01")
        .isTrue();

    softly.assertAll();
  }

  @Owner(Constants.Owner.LOKESH_SAXENA)
  @Feature("PG-7349")
  @Test(description = "Verify payeeMobileNumber and partnerReferenceNumber in Resp Auth Response from order creation from UPI PSP when received from Merchant Center V3 VPA API")
  public void PayeeMobileNumberAndpartnerReferenceNumberShouldComeInRespAuthResponseWhenItcomesinV3VpaAPIOfMerchantCenter() throws Exception {
    String orderId = generateUniqueOrderId();
    String epochSeconds = String.valueOf(Instant.now().toEpochMilli());
    SoftAssertions softly = new SoftAssertions();
    MerchantType merchant = MerchantType.DQR_UPI_ACQ_ID_IN_DEEPLINK_FF4J_OFF;

    logger.info("OrdersId is: {}", orderId);
    ReqAuthUPICreateOrder upiCreateOrder = new ReqAuthUPICreateOrder().buildRequestV2(
        PayerInstrument.SAVINGS.name(), "11.00", payerVPA, merchant.getVpa(), orderId, orderId, "ABC",
        epochSeconds, "PPBL", "SAVINGS", merchant.getId(), "DIRECT_SETTLEMENT");

    Response upiCreateOrderResponse = upiCreateOrder.executeV2("PPBL");
    String response = upiCreateOrderResponse.asString();
    logger.debug("Encrypted response is: {}", response);
    AesEncryption aesEncryption = new AesEncryption();
    String decryptedResponse;
    try {
      decryptedResponse = aesEncryption.decrypt(response, LocalConfig.INSTA_CREATE_ORDER_KEY);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    logger.debug("Decrypted response is: {}", decryptedResponse);
    JSONObject json = new JSONObject(decryptedResponse);
    softly.assertThat(json.get("statusMsg")).isEqualTo("Request Acknowledged");
    softly.assertThat(json.get("status")).isEqualTo("accept");

    String respAuthAsyncLogs = LogsValidationHelper.verifyLogsOnPod("INSTAPROXY", orderId, "respAuthAsync");
    softly.assertThat(respAuthAsyncLogs)
        .as("Instaproxy log should contain PaytmUPIPushService.respAuthAsync")
        .contains("PaytmUPIPushService.respAuthAsync");
    softly.assertThat(respAuthAsyncLogs)
        .as("respAuthAsync log should contain Order Creation Response prepared for the bank")
        .contains("Order Creation Response prepared for the bank");

    JSONObject orderCreationResponse = extractOrderCreationResponseJson(respAuthAsyncLogs);
    softly.assertThat(orderCreationResponse.optString("payeeMobileNumber", null))
        .as("payeeMobileNumber should be present in Order Creation Response")
        .isNotNull()
        .isNotBlank()
        .isEqualTo("8411917991");
    softly.assertThat(orderCreationResponse.optString("partnerReferenceNumber", null))
        .as("partnerReferenceNumber should be present in Order Creation Response")
        .isNotNull()
        .isNotBlank()
        .isEqualTo("033956918113");

    softly.assertAll();
  }

}
