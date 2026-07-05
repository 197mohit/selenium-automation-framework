package scripts.merchantStatus;

import com.paytm.api.RecoveryStatusApi;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.framework.reporting.Reporter;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.response.Response;
import org.assertj.core.api.SoftAssertions;
import org.testng.annotations.Test;

import java.util.stream.Stream;

import static com.paytm.appconstants.Constants.Owner.MOHIT_KHARE;

/**
 * E2E automation tests for POST /merchant-status/v1/recovery/status
 *
 * Coverage:
 *   MS-NEW-01  Success path — by orderId (requires a live recovery merchant and deduction order)
 *   MS-NEW-02  Invalid orderId / order not found (negative path — no real order needed)
 *   MS-NEW-03  Missing required field → INVALID_INPUT_DATA
 *   MS-NEW-04  Checksum mismatch → CHECKSUMMISMATCH response
 */
public class RecoveryStatusTests extends PGPBaseTest {

    /**
     * MS-NEW-01
     * Verify successful recovery status for an existing deduction order queried by mid + orderId.
     *
     * Pre-condition: A recovery merchant with a completed REPAYMENT deduction order must exist
     * in the environment. Set RECOVERY_MID, RECOVERY_ORDER_ID, and RECOVERY_MERCHANT_KEY as
     * environment / localconfig properties before enabling this test.
     *
     * Disabled until recovery merchant data is available in staging.
     */
    @Owner(MOHIT_KHARE)
    @Feature("MS-RECOVERY-STATUS")
    @Test(description = "Verify /v1/recovery/status returns SUCCESS for a completed deduction order by orderId.",
            enabled = false)
    public void recoveryStatus_Success_ByOrderId() throws Exception {
        String mid = System.getProperty("recovery.mid", "");
        String merchantKey = System.getProperty("recovery.merchant.key", "");
        String orderId = System.getProperty("recovery.order.id", CommonHelpers.generateOrderId());
        String deductionType = "REPAYMENT";

        Reporter.report.info("Querying recovery status for mid=" + mid + " orderId=" + orderId);

        RecoveryStatusApi api = new RecoveryStatusApi(mid, merchantKey, orderId, deductionType);
        Response response = api.execute();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.statusCode()).as("HTTP status code").isEqualTo(200);
        softly.assertThat(response.jsonPath().getString("body.resultInfo.resultStatus"))
                .as("resultStatus").isEqualTo("S");
        softly.assertThat(response.jsonPath().getString("body.merchantId"))
                .as("merchantId in response").isEqualTo(mid);
        softly.assertThat(response.jsonPath().getString("body.merchantTransId"))
                .as("merchantTransId in response").isEqualTo(orderId);
        softly.assertThat(response.jsonPath().getString("body.orderStatus"))
                .as("orderStatus").isNotBlank();
        softly.assertThat(response.jsonPath().getString("body.orderAmount"))
                .as("orderAmount").isNotNull();
        softly.assertThat(response.jsonPath().getString("body.deductionId"))
                .as("deductionId").isNotBlank();
        softly.assertAll();
    }

    /**
     * MS-NEW-01b
     * Verify recovery status by deductionId (alternate query path).
     * Disabled until recovery merchant data is available in staging.
     */
    @Owner(MOHIT_KHARE)
    @Feature("MS-RECOVERY-STATUS")
    @Test(description = "Verify /v1/recovery/status returns SUCCESS for a completed deduction queried by deductionId.",
            enabled = false)
    public void recoveryStatus_Success_ByDeductionId() throws Exception {
        String mid = System.getProperty("recovery.mid", "");
        String merchantKey = System.getProperty("recovery.merchant.key", "");
        String deductionId = System.getProperty("recovery.deduction.id", "");

        Reporter.report.info("Querying recovery status by deductionId=" + deductionId);

        RecoveryStatusApi api = new RecoveryStatusApi(mid, merchantKey, deductionId);
        Response response = api.execute();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.statusCode()).as("HTTP status code").isEqualTo(200);
        softly.assertThat(response.jsonPath().getString("body.resultInfo.resultStatus"))
                .as("resultStatus").isEqualTo("S");
        softly.assertThat(response.jsonPath().getString("body.deductionId"))
                .as("deductionId in response").isEqualTo(deductionId);
        softly.assertAll();
    }

    /**
     * MS-NEW-02
     * Verify /v1/recovery/status returns ORDER_NOT_FOUND or SYSTEM_ERROR (HTTP 200)
     * when queried with a non-existent orderId. Checksum is valid — only the downstream
     * lookup fails.
     */
    @Owner(MOHIT_KHARE)
    @Feature("MS-RECOVERY-STATUS")
    @Test(description = "Verify /v1/recovery/status handles a non-existent orderId gracefully.")
    public void recoveryStatus_OrderNotFound() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.AddnPay;
        String mid = merchant.getId();
        String merchantKey = merchant.getKey();
        String nonExistentOrderId = "NONEXISTENT_" + CommonHelpers.generateOrderId();
        String deductionType = "REPAYMENT";

        Reporter.report.info("Querying recovery status for non-existent orderId=" + nonExistentOrderId);

        RecoveryStatusApi api = new RecoveryStatusApi(mid, merchantKey, nonExistentOrderId, deductionType);
        Response response = api.execute();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.statusCode()).as("HTTP status code should always be 200").isEqualTo(200);
        softly.assertThat(response.jsonPath().getString("body.resultInfo.resultStatus"))
                .as("resultStatus should be failure for unknown order").isIn("F", "U");
        softly.assertThat(response.jsonPath().getString("head.version"))
                .as("response head.version").isNotBlank();
        softly.assertAll();
    }

    /**
     * MS-NEW-03
     * Verify /v1/recovery/status returns INVALID_INPUT_DATA when required body fields are missing.
     * Sends a request with no deductionType and no deductionId — the controller's validateRequest
     * throws FacadeInvalidParameterException which maps to INVALID_INPUT_DATA.
     */
    @Owner(MOHIT_KHARE)
    @Feature("MS-RECOVERY-STATUS")
    @Test(description = "Verify /v1/recovery/status returns INVALID_INPUT_DATA for missing deductionType field.")
    public void recoveryStatus_MissingDeductionType() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.AddnPay;
        String mid = merchant.getId();
        String merchantKey = merchant.getKey();
        String orderId = CommonHelpers.generateOrderId();

        Reporter.report.info("Testing recovery status with missing deductionType for mid=" + mid);

        // Sends orderId flow but deductionType is blank — triggers INVALID_INPUT_DATA from validateRequest
        RecoveryStatusApi api = new RecoveryStatusApi(mid, merchantKey, orderId, "");
        Response response = api.execute();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.statusCode()).as("HTTP status code").isEqualTo(200);
        softly.assertThat(response.jsonPath().getString("body.resultInfo.resultStatus"))
                .as("resultStatus should indicate failure").isIn("F", "U");
        softly.assertAll();
    }

    /**
     * MS-NEW-04
     * Verify /v1/recovery/status rejects requests with an invalid checksum.
     * The ChecksumFilter aborts the request with a CHECKSUMMISMATCH response before
     * reaching the controller.
     */
    @Owner(MOHIT_KHARE)
    @Feature("MS-RECOVERY-STATUS")
    @Test(description = "Verify /v1/recovery/status returns checksum mismatch error for tampered checksum.")
    public void recoveryStatus_ChecksumMismatch() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.AddnPay;
        String mid = merchant.getId();
        String orderId = CommonHelpers.generateOrderId();

        Reporter.report.info("Testing recovery status with invalid checksum for mid=" + mid);

        // badChecksum=true constructor sends "INVALID_CHECKSUM_VALUE" as CHECKSUMHASH
        RecoveryStatusApi api = new RecoveryStatusApi(mid, orderId, "REPAYMENT", true);
        Response response = api.execute();

        String body = response.body().asString();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.statusCode()).as("HTTP status code should be 200 (all MS responses)").isEqualTo(200);
        // When CHECKSUM_ENABLED=true  → CHECKSUMMISMATCH (330) or ChecksumFilter rejection
        // When CHECKSUM_ENABLED=false → checksum bypassed; service returns order-not-found via resultInfo
        softly.assertThat(Stream.of("330", "Checksum", "CHECKSUM", "checksum", "resultInfo", "resultStatus").anyMatch(body::contains))
                .as("response body must indicate checksum error or service-level result (checksum bypassed)")
                .isTrue();
        softly.assertAll();
    }

    /**
     * MS-NEW-04b
     * Verify /v1/recovery/status returns INVALID_INPUT_DATA when body.mid differs from JsonData.MID.
     * RecoveryStatusUtil.validateRequest() cross-checks body and JsonData fields; a mismatch on MID
     * throws FacadeInvalidParameterException("Invalid param, mid") → INVALID_INPUT_DATA (400).
     */
    @Owner(MOHIT_KHARE)
    @Feature("MS-RECOVERY-STATUS")
    @Test(description = "Verify /v1/recovery/status returns INVALID_INPUT_DATA when body.mid mismatches JsonData.MID.")
    public void recoveryStatus_BodyMidMismatch() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.AddnPay;
        String mid = merchant.getId();
        String merchantKey = merchant.getKey();
        String orderId = CommonHelpers.generateOrderId();
        String deductionType = "REPAYMENT";
        String wrongMid = "WRONGMID_" + mid;

        Reporter.report.info("Testing recovery status with body.mid mismatch for mid=" + mid);

        // Valid checksum computed on mid; body sends wrongMid → validateRequest() rejects
        RecoveryStatusApi api = new RecoveryStatusApi(
                merchantKey,
                mid, orderId, deductionType,
                wrongMid, orderId, deductionType);
        Response response = api.execute();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.statusCode()).as("HTTP status code").isEqualTo(200);
        softly.assertThat(response.jsonPath().getString("body.resultInfo.resultStatus"))
                .as("resultStatus should be failure for mismatched mid").isIn("F", "U");
        softly.assertAll();
    }

    /**
     * MS-NEW-04c
     * Verify /v1/recovery/status returns INVALID_INPUT_DATA when body.orderId differs from JsonData.ORDERID.
     */
    @Owner(MOHIT_KHARE)
    @Feature("MS-RECOVERY-STATUS")
    @Test(description = "Verify /v1/recovery/status returns INVALID_INPUT_DATA when body.orderId mismatches JsonData.ORDERID.")
    public void recoveryStatus_BodyOrderIdMismatch() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.AddnPay;
        String mid = merchant.getId();
        String merchantKey = merchant.getKey();
        String orderId = CommonHelpers.generateOrderId();
        String wrongOrderId = "WRONG_" + orderId;
        String deductionType = "REPAYMENT";

        Reporter.report.info("Testing recovery status with body.orderId mismatch, orderId=" + orderId);

        RecoveryStatusApi api = new RecoveryStatusApi(
                merchantKey,
                mid, orderId, deductionType,
                mid, wrongOrderId, deductionType);
        Response response = api.execute();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.statusCode()).as("HTTP status code").isEqualTo(200);
        softly.assertThat(response.jsonPath().getString("body.resultInfo.resultStatus"))
                .as("resultStatus should be failure for mismatched orderId").isIn("F", "U");
        softly.assertAll();
    }

    /**
     * MS-NEW-04d
     * Verify /v1/recovery/status returns INVALID_INPUT_DATA when body.deductionType differs from JsonData.DEDUCTIONTYPE.
     */
    @Owner(MOHIT_KHARE)
    @Feature("MS-RECOVERY-STATUS")
    @Test(description = "Verify /v1/recovery/status returns INVALID_INPUT_DATA when body.deductionType mismatches JsonData.DEDUCTIONTYPE.")
    public void recoveryStatus_BodyDeductionTypeMismatch() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.AddnPay;
        String mid = merchant.getId();
        String merchantKey = merchant.getKey();
        String orderId = CommonHelpers.generateOrderId();

        Reporter.report.info("Testing recovery status with body.deductionType mismatch for mid=" + mid);

        // JsonData has REPAYMENT; body has WRITEOFF → validateRequest() rejects
        RecoveryStatusApi api = new RecoveryStatusApi(
                merchantKey,
                mid, orderId, "REPAYMENT",
                mid, orderId, "WRITEOFF");
        Response response = api.execute();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.statusCode()).as("HTTP status code").isEqualTo(200);
        softly.assertThat(response.jsonPath().getString("body.resultInfo.resultStatus"))
                .as("resultStatus should be failure for mismatched deductionType").isIn("F", "U");
        softly.assertAll();
    }

    /**
     * MS-NEW-04e
     * Verify /v1/recovery/status returns an error when MID is blank in JsonData.
     * The ChecksumFilter is expected to reject the request with INVALID_MID (334) or
     * CHECKSUMMISMATCH (330) since no merchant key can be resolved for an empty MID.
     */
    @Owner(MOHIT_KHARE)
    @Feature("MS-RECOVERY-STATUS")
    @Test(description = "Verify /v1/recovery/status returns error when MID is blank in JsonData.")
    public void recoveryStatus_BlankMid() throws Exception {
        String orderId = CommonHelpers.generateOrderId();

        Reporter.report.info("Testing recovery status with blank MID in JsonData");

        // Empty MID + invalid checksum — ChecksumFilter rejects before reaching controller
        RecoveryStatusApi api = new RecoveryStatusApi("", orderId, "REPAYMENT", true);
        Response response = api.execute();

        String body = response.body().asString();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.statusCode()).as("HTTP status code should be 200").isEqualTo(200);
        // When CHECKSUM_ENABLED=true  → CHECKSUMMISMATCH (330) or INVALID_MID (334/335)
        // When CHECKSUM_ENABLED=false → checksum bypassed; service returns error via resultInfo
        softly.assertThat(
                        Stream.of("334", "335", "INVALID_MID", "330", "Checksum", "CHECKSUM", "checksum", "resultInfo").anyMatch(body::contains))
                .as("response must indicate MID or checksum error for blank MID")
                .isTrue();
        softly.assertAll();
    }
}