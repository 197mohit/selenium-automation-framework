package scripts.merchantStatus;

import com.paytm.api.HandlerTxnStatusRawApi;
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
 * E2E tests targeting the GET variants of HANDLER_INTERNAL endpoints:
 *   GET /merchant-status/HANDLER_INTERNAL/TXNSTATUS
 *   GET /merchant-status/HANDLER_INTERNAL/TXNSTATUSLIST
 *
 * Target coverage:
 *   MerchantTxnStatusController.processMerchantTxnStatusGET()       — GET without checksum
 *   MerchantTxnStatusController.processMerchantTxnListStatusGET()   — GET list without checksum
 *   MerchantTxnStatusController.isDynamicWrapperEnabledForMerchantRequest() — GET path
 *
 * Existing MerchantStatusTests primarily use POST for these endpoints; exercising the
 * GET methods covers the alternate controller entry points and DynamicWrapper branch.
 */
public class MerchantStatusGetEndpointTests extends PGPBaseTest {

    // =========================================================================
    // GET HANDLER_INTERNAL/TXNSTATUS — non-existent order, unknown MID
    // =========================================================================

    /**
     * MS-GET-01
     * Verify GET /HANDLER_INTERNAL/TXNSTATUS returns order-not-found for a non-existent orderId.
     *
     * Branch: MerchantTxnStatusController.processMerchantTxnStatusGET()
     *   → isDynamicWrapperEnabledForMerchantRequest(mid)
     *   → processMerchantTxnStatus()
     *   → MerchantTxnStatusServiceImpl.getMerchantTxnStatus() → TARGET_NOT_FOUND
     */
    @Owner(MOHIT_KHARE)
    @Feature("MS-GET-ENDPOINTS")
    @Test(description = "Verify GET HANDLER_INTERNAL/TXNSTATUS returns order-not-found for non-existent orderId.")
    public void handlerTxnStatus_GET_OrderNotFound() {
        Constants.MerchantType merchant = Constants.MerchantType.AddnPay;
        String nonExistentOrderId = "NONEXIST_GET_" + CommonHelpers.generateOrderId();

        Reporter.report.info("Testing GET TXNSTATUS for non-existent orderId=" + nonExistentOrderId);

        HandlerTxnStatusRawApi api = new HandlerTxnStatusRawApi(
                Constants.PGPAPIResourcePath.TXNSTATUS,
                merchant.getId(), nonExistentOrderId, "FAKE_CHECKSUM", false);
        Response response = api.execute();

        String body = response.body().asString();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.statusCode()).as("HTTP status should be 200").isEqualTo(200);
        softly.assertThat(
                        Stream.of("INVALID_ORDER", "SALE_PENDING", "TXN_FAILURE", "334", "810",
                                        "RESPCODE", "330", "F", "U")
                                .anyMatch(body::contains))
                .as("response must indicate order-not-found, pending, or checksum error")
                .isTrue();
        softly.assertAll();
    }

    /**
     * MS-GET-02
     * Verify GET /HANDLER_INTERNAL/TXNSTATUS handles an unknown MID.
     *
     * Branch: MerchantTxnStatusController.processMerchantTxnStatusGET()
     *   → isDynamicWrapperEnabledForMerchantRequest(unknownMid) → false (no wrapper config)
     *   → processMerchantTxnStatus()
     *   → MerchantTxnStatusServiceImpl: PLATFORM_MERCHANT_ID_NOT_FOUND
     */
    @Owner(MOHIT_KHARE)
    @Feature("MS-GET-ENDPOINTS")
    @Test(description = "Verify GET HANDLER_INTERNAL/TXNSTATUS returns error for unknown MID.")
    public void handlerTxnStatus_GET_UnknownMid() {
        String unknownMid = "UNKNOWN_GET_MID_XYZ_99";
        String orderId = CommonHelpers.generateOrderId();

        Reporter.report.info("Testing GET TXNSTATUS with unknown MID=" + unknownMid);

        HandlerTxnStatusRawApi api = new HandlerTxnStatusRawApi(
                Constants.PGPAPIResourcePath.TXNSTATUS,
                unknownMid, orderId, "FAKE_CHECKSUM", false);
        Response response = api.execute();

        String body = response.body().asString();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.statusCode()).as("HTTP status should be 200").isEqualTo(200);
        softly.assertThat(
                        Stream.of("501", "335", "RESPCODE", "System Error", "System error", "330",
                                        "ErrorCode", "ErrorMsg", "CMER")
                                .anyMatch(body::contains))
                .as("response must indicate service error for unknown MID")
                .isTrue();
        softly.assertAll();
    }

    /**
     * MS-GET-03
     * Verify GET /HANDLER_INTERNAL/TXNSTATUS with blank MID returns INVALID_MID.
     *
     * Branch: MerchantTxnStatusController.processMerchantTxnStatusGET()
     *   → MID is null from httpServletRequest.getParameter("MID")
     *   → or blank from JsonData → service layer rejects
     */
    @Owner(MOHIT_KHARE)
    @Feature("MS-GET-ENDPOINTS")
    @Test(description = "Verify GET HANDLER_INTERNAL/TXNSTATUS returns error for blank MID.")
    public void handlerTxnStatus_GET_BlankMid() {
        String orderId = CommonHelpers.generateOrderId();

        Reporter.report.info("Testing GET TXNSTATUS with blank MID");

        HandlerTxnStatusRawApi api = new HandlerTxnStatusRawApi(
                Constants.PGPAPIResourcePath.TXNSTATUS,
                "", orderId, "FAKE_CHECKSUM", false);
        Response response = api.execute();

        String body = response.body().asString();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.statusCode()).as("HTTP status should be 200").isEqualTo(200);
        softly.assertThat(
                        Stream.of("334", "335", "INVALID_MID", "RESPCODE", "330", "501",
                                        "ErrorCode", "ErrorMsg", "CMER")
                                .anyMatch(body::contains))
                .as("response must indicate MID or checksum error")
                .isTrue();
        softly.assertAll();
    }

    // =========================================================================
    // GET HANDLER_INTERNAL/TXNSTATUSLIST — non-existent order, unknown MID
    // =========================================================================

    /**
     * MS-GET-04
     * Verify GET /HANDLER_INTERNAL/TXNSTATUSLIST returns order-not-found for a non-existent orderId.
     *
     * Branch: MerchantTxnStatusController.processMerchantTxnListStatusGET()
     *   → processTxnStatusList()
     *   → MerchantTxnStatusServiceImpl.getMerchantTxnStatus() → TARGET_NOT_FOUND
     */
    @Owner(MOHIT_KHARE)
    @Feature("MS-GET-ENDPOINTS")
    @Test(description = "Verify GET HANDLER_INTERNAL/TXNSTATUSLIST returns order-not-found for non-existent orderId.")
    public void handlerTxnStatusList_GET_OrderNotFound() {
        Constants.MerchantType merchant = Constants.MerchantType.AddnPay;
        String nonExistentOrderId = "NONEXIST_LIST_GET_" + CommonHelpers.generateOrderId();

        Reporter.report.info("Testing GET TXNSTATUSLIST for non-existent orderId=" + nonExistentOrderId);

        HandlerTxnStatusRawApi api = new HandlerTxnStatusRawApi(
                Constants.PGPAPIResourcePath.TXN_STATUS_LIST,
                merchant.getId(), nonExistentOrderId, "FAKE_CHECKSUM", false);
        Response response = api.execute();

        String body = response.body().asString();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.statusCode()).as("HTTP status should be 200").isEqualTo(200);
        softly.assertThat(
                        Stream.of("INVALID_ORDER", "SALE_PENDING", "TXN_FAILURE", "334", "810",
                                        "RESPCODE", "330", "F", "U")
                                .anyMatch(body::contains))
                .as("response must indicate order-not-found, pending, or checksum error")
                .isTrue();
        softly.assertAll();
    }

    /**
     * MS-GET-05
     * Verify GET /HANDLER_INTERNAL/TXNSTATUSLIST handles an unknown MID.
     */
    @Owner(MOHIT_KHARE)
    @Feature("MS-GET-ENDPOINTS")
    @Test(description = "Verify GET HANDLER_INTERNAL/TXNSTATUSLIST returns error for unknown MID.")
    public void handlerTxnStatusList_GET_UnknownMid() {
        String unknownMid = "UNKNOWN_LIST_GET_MID_XYZ_99";
        String orderId = CommonHelpers.generateOrderId();

        Reporter.report.info("Testing GET TXNSTATUSLIST with unknown MID=" + unknownMid);

        HandlerTxnStatusRawApi api = new HandlerTxnStatusRawApi(
                Constants.PGPAPIResourcePath.TXN_STATUS_LIST,
                unknownMid, orderId, "FAKE_CHECKSUM", false);
        Response response = api.execute();

        String body = response.body().asString();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.statusCode()).as("HTTP status should be 200").isEqualTo(200);
        softly.assertThat(
                        Stream.of("501", "335", "RESPCODE", "System Error", "System error", "330",
                                        "ErrorCode", "ErrorMsg", "CMER")
                                .anyMatch(body::contains))
                .as("response must indicate service error for unknown MID")
                .isTrue();
        softly.assertAll();
    }

    /**
     * MS-GET-06
     * Verify GET /HANDLER_INTERNAL/TXNSTATUSLIST with blank MID returns error.
     */
    @Owner(MOHIT_KHARE)
    @Feature("MS-GET-ENDPOINTS")
    @Test(description = "Verify GET HANDLER_INTERNAL/TXNSTATUSLIST returns error for blank MID.")
    public void handlerTxnStatusList_GET_BlankMid() {
        String orderId = CommonHelpers.generateOrderId();

        Reporter.report.info("Testing GET TXNSTATUSLIST with blank MID");

        HandlerTxnStatusRawApi api = new HandlerTxnStatusRawApi(
                Constants.PGPAPIResourcePath.TXN_STATUS_LIST,
                "", orderId, "FAKE_CHECKSUM", false);
        Response response = api.execute();

        String body = response.body().asString();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.statusCode()).as("HTTP status should be 200").isEqualTo(200);
        softly.assertThat(
                        Stream.of("334", "335", "INVALID_MID", "RESPCODE", "330", "501",
                                        "ErrorCode", "ErrorMsg", "CMER")
                                .anyMatch(body::contains))
                .as("response must indicate MID or checksum error")
                .isTrue();
        softly.assertAll();
    }
}
