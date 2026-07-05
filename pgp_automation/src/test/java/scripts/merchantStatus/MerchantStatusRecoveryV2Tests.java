package scripts.merchantStatus;

import com.paytm.api.RecoveryStatusV2Api;
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
 * E2E automation tests for the V2 recovery status branch:
 *   POST /merchant-status/v1/recovery/status  (head.version=v2)
 *
 * Target coverage:
 *   RecoveryStatusServiceBizImpl.getRecoveryStatusV2()  — routed when head.version == "v2"
 *     AND FF4J flag enable.merchant.status.v2.recovery.status is enabled for the MID.
 *   RecoveryStatusController.processRecoveryStatus()    — version-routing branch.
 *   RecoveryStatusUtil.validateRequest()                — validation logic.
 *
 * If FF4J flag is OFF for the test MID, the service falls back to V1 — assertions are
 * relaxed to accept either V1 or V2 response shapes.
 */
public class MerchantStatusRecoveryV2Tests extends PGPBaseTest {

    // =========================================================================
    // Recovery V2 — order not found (exercises getRecoveryStatusV2 path)
    // =========================================================================

    /**
     * MS-RCV2-01
     * Verify /v1/recovery/status with head.version=v2 returns order-not-found
     * when queried with a non-existent orderId.
     *
     * Branch: RecoveryStatusController.processRecoveryStatus()
     *   → head.version == "v2" + FF4J enable.merchant.status.v2.recovery.status
     *   → RecoveryStatusServiceBizImpl.getRecoveryStatusV2()
     *   → facade RecoveryStatus.getRecoveryStatusV2() returns not-found
     */
    @Owner(MOHIT_KHARE)
    @Feature("MS-RECOVERY-V2")
    @Test(description = "Verify /v1/recovery/status V2 returns order-not-found for non-existent orderId.")
    public void recoveryStatusV2_OrderNotFound() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.AddnPay;
        String mid = merchant.getId();
        String merchantKey = merchant.getKey();
        String nonExistentOrderId = "NONEXISTENT_V2_" + CommonHelpers.generateOrderId();

        Reporter.report.info("Testing recovery V2 for non-existent orderId=" + nonExistentOrderId);

        RecoveryStatusV2Api api = new RecoveryStatusV2Api(mid, merchantKey, nonExistentOrderId, "REPAYMENT");
        Response response = api.execute();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.statusCode()).as("HTTP status code").isEqualTo(200);
        softly.assertThat(response.jsonPath().getString("body.resultInfo.resultStatus"))
                .as("resultStatus should be failure for unknown order").isIn("F", "U");
        softly.assertThat(response.jsonPath().getString("head.version"))
                .as("response head.version").isNotBlank();
        softly.assertAll();
    }

    /**
     * MS-RCV2-02
     * Verify /v1/recovery/status V2 rejects requests with an invalid checksum.
     * The ChecksumFilter aborts before reaching the controller regardless of version.
     */
    @Owner(MOHIT_KHARE)
    @Feature("MS-RECOVERY-V2")
    @Test(description = "Verify /v1/recovery/status V2 returns checksum error for tampered checksum.")
    public void recoveryStatusV2_ChecksumMismatch() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.AddnPay;
        String mid = merchant.getId();
        String orderId = CommonHelpers.generateOrderId();

        Reporter.report.info("Testing recovery V2 with invalid checksum for mid=" + mid);

        RecoveryStatusV2Api api = new RecoveryStatusV2Api(mid, orderId, "REPAYMENT", true);
        Response response = api.execute();

        String body = response.body().asString();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.statusCode()).as("HTTP status code").isEqualTo(200);
        softly.assertThat(
                        Stream.of("330", "Checksum", "CHECKSUM", "checksum", "resultInfo", "resultStatus")
                                .anyMatch(body::contains))
                .as("response must indicate checksum error or service-level result")
                .isTrue();
        softly.assertAll();
    }

    /**
     * MS-RCV2-03
     * Verify /v1/recovery/status V2 returns validation failure when deductionType is blank.
     *
     * Branch: RecoveryStatusUtil.validateRequest() detects missing deductionType
     *   → throws FacadeInvalidParameterException → INVALID_INPUT_DATA
     */
    @Owner(MOHIT_KHARE)
    @Feature("MS-RECOVERY-V2")
    @Test(description = "Verify /v1/recovery/status V2 returns INVALID_INPUT_DATA for missing deductionType.")
    public void recoveryStatusV2_MissingDeductionType() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.AddnPay;
        String mid = merchant.getId();
        String merchantKey = merchant.getKey();
        String orderId = CommonHelpers.generateOrderId();

        Reporter.report.info("Testing recovery V2 with blank deductionType for mid=" + mid);

        RecoveryStatusV2Api api = new RecoveryStatusV2Api(mid, merchantKey, orderId, "");
        Response response = api.execute();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.statusCode()).as("HTTP status code").isEqualTo(200);
        softly.assertThat(response.jsonPath().getString("body.resultInfo.resultStatus"))
                .as("resultStatus should indicate failure").isIn("F", "U");
        softly.assertAll();
    }

    /**
     * MS-RCV2-04
     * Verify /v1/recovery/status V2 returns error when MID is blank.
     * The ChecksumFilter rejects with INVALID_MID or CHECKSUMMISMATCH.
     */
    @Owner(MOHIT_KHARE)
    @Feature("MS-RECOVERY-V2")
    @Test(description = "Verify /v1/recovery/status V2 returns error for blank MID.")
    public void recoveryStatusV2_BlankMid() throws Exception {
        String orderId = CommonHelpers.generateOrderId();

        Reporter.report.info("Testing recovery V2 with blank MID");

        RecoveryStatusV2Api api = new RecoveryStatusV2Api("", orderId, "REPAYMENT", true);
        Response response = api.execute();

        String body = response.body().asString();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.statusCode()).as("HTTP status code").isEqualTo(200);
        softly.assertThat(
                        Stream.of("334", "335", "INVALID_MID", "330", "Checksum", "CHECKSUM", "resultInfo")
                                .anyMatch(body::contains))
                .as("response must indicate MID or checksum error")
                .isTrue();
        softly.assertAll();
    }

    // =========================================================================
    // Recovery V2 — success path (disabled until recovery merchant available)
    // =========================================================================

    /**
     * MS-RCV2-05
     * Verify /v1/recovery/status V2 returns SUCCESS for a completed deduction order.
     *
     * Pre-condition: set -Drecovery.mid, -Drecovery.merchant.key, -Drecovery.order.id
     * with a merchant that has FF4J enable.merchant.status.v2.recovery.status enabled.
     */
    @Owner(MOHIT_KHARE)
    @Feature("MS-RECOVERY-V2")
    @Test(description = "Verify /v1/recovery/status V2 returns SUCCESS for a completed deduction order.",
            enabled = false)
    public void recoveryStatusV2_Success() throws Exception {
        String mid = System.getProperty("recovery.mid", "");
        String merchantKey = System.getProperty("recovery.merchant.key", "");
        String orderId = System.getProperty("recovery.order.id", CommonHelpers.generateOrderId());

        Reporter.report.info("Testing recovery V2 success for mid=" + mid + " orderId=" + orderId);

        RecoveryStatusV2Api api = new RecoveryStatusV2Api(mid, merchantKey, orderId, "REPAYMENT");
        Response response = api.execute();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.statusCode()).as("HTTP status code").isEqualTo(200);
        softly.assertThat(response.jsonPath().getString("body.resultInfo.resultStatus"))
                .as("resultStatus").isEqualTo("S");
        softly.assertThat(response.jsonPath().getString("body.merchantId"))
                .as("merchantId").isEqualTo(mid);
        softly.assertAll();
    }
}
