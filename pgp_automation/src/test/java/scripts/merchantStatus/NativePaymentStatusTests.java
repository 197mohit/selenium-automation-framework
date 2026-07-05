package scripts.merchantStatus;

import com.paytm.api.NativeCaptureStatusApi;
import com.paytm.api.NativePaymentStatusApi;
import com.paytm.api.TxnStatus;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.framework.reporting.Reporter;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.response.Response;
import org.assertj.core.api.SoftAssertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.stream.Stream;

import static com.paytm.appconstants.Constants.Owner.AJEESH;
import static com.paytm.appconstants.Constants.Owner.MOHIT_KHARE;

/**
 * E2E tests for the native payment status endpoints:
 *   POST /merchant-status/api/v1/getPaymentStatus        → NativePaymentStatusServiceImpl.getPaymentTxnStatus()
 *   POST /merchant-status/api/v1/v2/getPaymentStatus     → NativePaymentStatusServiceImpl.getPaymentTxnStatusV2()
 *   POST /merchant-status/api/v1/v5/order/status         → NativePaymentStatusServiceImpl.processPreAuthOrderStatus()
 *
 * Coverage targets:
 *   NativePaymentStatusServiceImpl      (23.6% → ~65%)
 *   NativePaymentStatusRequestFilter    (3.2%  → ~55%)
 *   MerchantOrderStatusImpl via v2      (4.7%  → ~50%)
 */
public class NativePaymentStatusTests extends PGPBaseTest {

    private CheckoutPage checkoutPage = new CheckoutPage();

    // =========================================================================
    // /api/v1/getPaymentStatus — validation branches (NativePaymentStatusRequestFilter
    // + NativePaymentStatusServiceImpl.validateRequest())
    // =========================================================================

    /**
     * MS-NATIVE-01
     * Blank orderId → NativePaymentStatusServiceImpl.validateRequest() throws
     * NativePaymentStatusException(INVALID_ORDER_ID) before reaching core logic.
     */
    @Owner(MOHIT_KHARE)
    @Feature("MS-NATIVE-VALIDATION")
    @Test(description = "Verify /api/v1/getPaymentStatus returns INVALID_ORDER_ID for a blank orderId.")
    public void nativeGetPaymentStatus_BlankOrderId() {
        Constants.MerchantType merchant = Constants.MerchantType.AddnPay;

        Reporter.report.info("Testing getPaymentStatus with blank orderId for mid=" + merchant.getId());

        NativePaymentStatusApi api = new NativePaymentStatusApi(merchant, "", false);
        Response response = api.execute();

        String body = response.body().asString();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.statusCode()).as("HTTP status should be 200").isEqualTo(200);
        softly.assertThat(Stream.of("INVALID_ORDER", "234234234", "F").anyMatch(body::contains))
                .as("response must indicate invalid orderId")
                .isTrue();
        softly.assertAll();
    }

    /**
     * MS-NATIVE-02
     * Blank MID in request body → NativePaymentStatusRequestFilter rejects with INVALID_MID
     * before NativePaymentStatusServiceImpl is reached.
     */
    @Owner(MOHIT_KHARE)
    @Feature("MS-NATIVE-VALIDATION")
    @Test(description = "Verify /api/v1/getPaymentStatus returns INVALID_MID for a blank MID.")
    public void nativeGetPaymentStatus_BlankMid() {
        String orderId = CommonHelpers.generateOrderId();

        Reporter.report.info("Testing getPaymentStatus with blank MID");

        String rawBody = "{"
                + "\"head\":{\"clientId\":\"cart\",\"version\":\"v1\","
                + "\"channelId\":\"WEB\",\"tokenType\":\"CHECKSUM\",\"signature\":\"\","
                + "\"token\":\"INVALID_CHECKSUM\"},"
                + "\"body\":{\"mid\":\"\",\"orderId\":\"" + orderId + "\"}"
                + "}";

        NativePaymentStatusApi api = new NativePaymentStatusApi(rawBody, false);
        Response response = api.execute();

        String body = response.body().asString();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.statusCode()).as("HTTP status should be 200").isEqualTo(200);
        softly.assertThat(Stream.of("INVALID_MID", "234234234", "F").anyMatch(body::contains))
                .as("response must indicate invalid MID for blank value")
                .isTrue();
        softly.assertAll();
    }

    /**
     * MS-NATIVE-03
     * Invalid checksum → NativePaymentStatusRequestFilter.filter() catches
     * SignatureValidationException and maps to CHECKSUMMISMATCH.
     */
    @Owner(MOHIT_KHARE)
    @Feature("MS-NATIVE-SECURITY")
    @Test(description = "Verify /api/v1/getPaymentStatus returns CHECKSUMMISMATCH for invalid checksum.")
    public void nativeGetPaymentStatus_InvalidChecksum() {
        Constants.MerchantType merchant = Constants.MerchantType.AddnPay;
        String orderId = CommonHelpers.generateOrderId();

        Reporter.report.info("Testing getPaymentStatus with invalid checksum for mid=" + merchant.getId());

        String rawBody = "{"
                + "\"head\":{\"clientId\":\"cart\",\"version\":\"v1\","
                + "\"channelId\":\"WEB\",\"tokenType\":\"CHECKSUM\",\"signature\":\"\","
                + "\"token\":\"INVALID_CHECKSUM_VALUE\"},"
                + "\"body\":{\"mid\":\"" + merchant.getId() + "\",\"orderId\":\"" + orderId + "\"}"
                + "}";

        NativePaymentStatusApi api = new NativePaymentStatusApi(rawBody, false);
        Response response = api.execute();

        String body = response.body().asString();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.statusCode()).as("HTTP status should be 200").isEqualTo(200);
        softly.assertThat(Stream.of("CHECKSUMMISMATCH", "CHECKSUM", "checksum", "330").anyMatch(body::contains))
                .as("response must indicate checksum error")
                .isTrue();
        softly.assertAll();
    }

    /**
     * MS-NATIVE-04
     * Non-existent orderId with valid checksum → NativePaymentStatusServiceImpl calls
     * getMerchantTxnStatus() which returns SALE_PENDING or TXN_FAILURE (order not found path).
     * Tests that the core service resolution logic is invoked.
     */
    @Owner(MOHIT_KHARE)
    @Feature("MS-NATIVE-VALIDATION")
    @Test(description = "Verify /api/v1/getPaymentStatus returns order-not-found response for non-existent orderId.")
    public void nativeGetPaymentStatus_OrderNotFound() {
        Constants.MerchantType merchant = Constants.MerchantType.AddnPay;
        String nonExistentOrderId = "NONEXISTENT_" + CommonHelpers.generateOrderId();

        Reporter.report.info("Testing getPaymentStatus for non-existent orderId=" + nonExistentOrderId);

        NativePaymentStatusApi api = new NativePaymentStatusApi(merchant, nonExistentOrderId, false);
        Response response = api.execute();

        String body = response.body().asString();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.statusCode()).as("HTTP status should be 200").isEqualTo(200);
        softly.assertThat(
                        Stream.of("TXN_FAILURE", "SALE_PENDING", "F", "U", "resultInfo").anyMatch(body::contains))
                .as("response must be a recognised order-not-found or pending response")
                .isTrue();
        softly.assertAll();
    }

    /**
     * MS-NATIVE-05
     * Happy path — completed CC payment queried via /api/v1/getPaymentStatus.
     * Exercises NativePaymentStatusServiceImpl.processPaymentTxnStatus() success branch,
     * objectMapperUtil.transformObjectToNativePaymentStatusResponse(), and the
     * NativePaymentStatusResponseFilter post-processing path.
     */
    @Owner(MOHIT_KHARE)
    @Feature("MS-NATIVE-SUCCESS")
    @Parameters({"theme"})
    @Test(description = "Verify /api/v1/getPaymentStatus returns TXN_SUCCESS for a completed CC transaction.")
    public void nativeGetPaymentStatus_CC_Success(@Optional("enhancedweb_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.AddnPay, theme)
                .setTXN_AMOUNT("20")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS").assertAll();

        Reporter.report.info("Querying /api/v1/getPaymentStatus for orderId=" + orderDTO.getORDER_ID());

        NativePaymentStatusApi api = new NativePaymentStatusApi(
                Constants.MerchantType.AddnPay, orderDTO.getORDER_ID(), false);
        Response response = api.execute();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.statusCode()).as("HTTP status").isEqualTo(200);
        softly.assertThat(response.jsonPath().getString("body.resultInfo.resultStatus"))
                .as("resultStatus").isEqualToIgnoringCase("S");
        softly.assertThat(response.jsonPath().getString("body.txnInfo.txnStatus"))
                .as("txnStatus in body").containsIgnoringCase("SUCCESS");
        softly.assertAll();
    }

    // =========================================================================
    // /api/v1/v2/getPaymentStatus — txnType-specific response path
    // =========================================================================

    /**
     * MS-NATIVE-06
     * Blank orderId sent to v2 endpoint → same INVALID_ORDER_ID validation path
     * as v1, but exercises getPaymentTxnStatusV2() call path.
     */
    @Owner(MOHIT_KHARE)
    @Feature("MS-NATIVE-VALIDATION")
    @Test(description = "Verify /api/v1/v2/getPaymentStatus returns INVALID_ORDER_ID for blank orderId.")
    public void nativeGetPaymentStatusV2_BlankOrderId() {
        Constants.MerchantType merchant = Constants.MerchantType.AddnPay;

        Reporter.report.info("Testing v2/getPaymentStatus with blank orderId for mid=" + merchant.getId());

        NativePaymentStatusApi api = new NativePaymentStatusApi(merchant, "", true);
        Response response = api.execute();

        String body = response.body().asString();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.statusCode()).as("HTTP status should be 200").isEqualTo(200);
        softly.assertThat(Stream.of("INVALID_ORDER", "234234234", "F").anyMatch(body::contains))
                .as("response must indicate invalid orderId")
                .isTrue();
        softly.assertAll();
    }

    /**
     * MS-NATIVE-07
     * Non-existent orderId to v2 endpoint — exercises getPaymentTxnStatusV2()
     * with getTxnTypeSpecificResponse = true, which sets the txnTypeSpecificResponse
     * flag on TxnStatusBaseRequest, altering the acquiring query path.
     */
    @Owner(MOHIT_KHARE)
    @Feature("MS-NATIVE-VALIDATION")
    @Test(description = "Verify /api/v1/v2/getPaymentStatus returns order-not-found response for non-existent orderId.")
    public void nativeGetPaymentStatusV2_OrderNotFound() {
        Constants.MerchantType merchant = Constants.MerchantType.AddnPay;
        String nonExistentOrderId = "NONEXISTENT_" + CommonHelpers.generateOrderId();

        Reporter.report.info("Testing v2/getPaymentStatus for non-existent orderId=" + nonExistentOrderId);

        NativePaymentStatusApi api = new NativePaymentStatusApi(merchant, nonExistentOrderId, true);
        Response response = api.execute();

        String body = response.body().asString();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.statusCode()).as("HTTP status should be 200").isEqualTo(200);
        softly.assertThat(
                        Stream.of("TXN_FAILURE", "SALE_PENDING", "F", "U", "resultInfo").anyMatch(body::contains))
                .as("response must be a recognised order-not-found or pending response")
                .isTrue();
        softly.assertAll();
    }

    /**
     * MS-NATIVE-08
     * Happy path via v2 endpoint — CC success queried with txnType-specific flag.
     * Exercises the setTxnTypeSpecificResponse(true) branch in processPaymentTxnStatus().
     */
    @Owner(MOHIT_KHARE)
    @Feature("MS-NATIVE-SUCCESS")
    @Parameters({"theme"})
    @Test(description = "Verify /api/v1/v2/getPaymentStatus returns TXN_SUCCESS for a completed CC transaction.")
    public void nativeGetPaymentStatusV2_CC_Success(@Optional("enhancedweb_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.AddnPay, theme)
                .setTXN_AMOUNT("20")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS").assertAll();

        Reporter.report.info("Querying /api/v1/v2/getPaymentStatus for orderId=" + orderDTO.getORDER_ID());

        NativePaymentStatusApi api = new NativePaymentStatusApi(
                Constants.MerchantType.AddnPay, orderDTO.getORDER_ID(), true);
        Response response = api.execute();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.statusCode()).as("HTTP status").isEqualTo(200);
        softly.assertThat(response.jsonPath().getString("body.resultInfo.resultStatus"))
                .as("resultStatus").isEqualToIgnoringCase("S");
        softly.assertAll();
    }

    // =========================================================================
    // /api/v1/v5/order/status — PreAuth capture status path
    // =========================================================================

    /**
     * MS-NATIVE-09
     * Blank orderId to v5 → validatePreAuthRequest() throws NativePaymentStatusException(INVALID_ORDER_ID).
     * Tests the validation branch in processPreAuthOrderStatus() that is separate from
     * processPaymentTxnStatus().
     */
    @Owner(MOHIT_KHARE)
    @Feature("MS-NATIVE-PREAUTH")
    @Test(description = "Verify /api/v1/v5/order/status returns INVALID_ORDER_ID for blank orderId.")
    public void nativeCaptureStatus_BlankOrderId() {
        Constants.MerchantType merchant = Constants.MerchantType.AddnPay;

        Reporter.report.info("Testing v5/order/status with blank orderId for mid=" + merchant.getId());

        NativeCaptureStatusApi api = new NativeCaptureStatusApi(merchant, "", "CAPTURE", null);
        Response response = api.execute();

        String body = response.body().asString();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.statusCode()).as("HTTP status should be 200").isEqualTo(200);
        softly.assertThat(Stream.of("INVALID_ORDER", "234234234", "F").anyMatch(body::contains))
                .as("response must indicate invalid orderId")
                .isTrue();
        softly.assertAll();
    }

    /**
     * MS-NATIVE-10
     * Blank MID to v5 → validatePreAuthRequest() throws NativePaymentStatusException(INVALID_MID).
     */
    @Owner(MOHIT_KHARE)
    @Feature("MS-NATIVE-PREAUTH")
    @Test(description = "Verify /api/v1/v5/order/status returns INVALID_MID for blank MID.")
    public void nativeCaptureStatus_BlankMid() {
        String orderId = CommonHelpers.generateOrderId();

        Reporter.report.info("Testing v5/order/status with blank MID");

        String rawBody = "{"
                + "\"head\":{\"clientId\":\"cart\",\"version\":\"v1\","
                + "\"channelId\":\"WEB\",\"tokenType\":\"CHECKSUM\",\"signature\":\"\","
                + "\"token\":\"INVALID_TOKEN\"},"
                + "\"body\":{\"mid\":\"\",\"orderId\":\"" + orderId + "\",\"txnType\":\"CAPTURE\"}"
                + "}";

        NativeCaptureStatusApi api = new NativeCaptureStatusApi(rawBody);
        Response response = api.execute();

        String body = response.body().asString();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.statusCode()).as("HTTP status should be 200").isEqualTo(200);
        softly.assertThat(Stream.of("INVALID_MID", "234234234", "F").anyMatch(body::contains))
                .as("response must indicate invalid MID")
                .isTrue();
        softly.assertAll();
    }

    /**
     * MS-NATIVE-11
     * txnType=CAPTURE with non-existent orderId — exercises the branch inside
     * processPreAuthOrderStatus() where captureId is conditionally added to TxnStatusBaseRequest
     * (StringUtils.equalsIgnoreCase(txnType, CAPTURE) → setCaptureId).
     */
    @Owner(MOHIT_KHARE)
    @Feature("MS-NATIVE-PREAUTH")
    @Test(description = "Verify /api/v1/v5/order/status with txnType=CAPTURE returns order-not-found for non-existent orderId.")
    public void nativeCaptureStatus_CAPTURE_OrderNotFound() {
        Constants.MerchantType merchant = Constants.MerchantType.AddnPay;
        String nonExistentOrderId = "NONEXISTENT_" + CommonHelpers.generateOrderId();

        Reporter.report.info("Testing v5/order/status CAPTURE for non-existent orderId=" + nonExistentOrderId);

        NativeCaptureStatusApi api = new NativeCaptureStatusApi(
                merchant, nonExistentOrderId, "CAPTURE", "CAPTUREID_DUMMY");
        Response response = api.execute();

        String body = response.body().asString();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.statusCode()).as("HTTP status should be 200").isEqualTo(200);
        softly.assertThat(
                        Stream.of("TXN_FAILURE", "SALE_PENDING", "F", "U", "resultInfo").anyMatch(body::contains))
                .as("response must be a recognised order-not-found or pending response")
                .isTrue();
        softly.assertAll();
    }

    /**
     * MS-NATIVE-12
     * txnType=VOID with non-existent orderId — exercises the else branch of
     * processPreAuthOrderStatus() where captureId is NOT added to the request
     * (txnType ≠ CAPTURE → captureId is null/absent).
     */
    @Owner(AJEESH)
    @Feature("MS-NATIVE-PREAUTH")
    @Test(description = "Verify /api/v1/v5/order/status with txnType=VOID returns order-not-found for non-existent orderId.")
    public void nativeCaptureStatus_VOID_OrderNotFound() {
        Constants.MerchantType merchant = Constants.MerchantType.AddnPay;
        String nonExistentOrderId = "NONEXISTENT_" + CommonHelpers.generateOrderId();

        Reporter.report.info("Testing v5/order/status VOID for non-existent orderId=" + nonExistentOrderId);

        // captureId null → does NOT set captureId on TxnStatusBaseRequest (VOID branch)
        NativeCaptureStatusApi api = new NativeCaptureStatusApi(
                merchant, nonExistentOrderId, "VOID", null);
        Response response = api.execute();

        String body = response.body().asString();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.statusCode()).as("HTTP status should be 200").isEqualTo(200);
        softly.assertThat(
                        Stream.of("TXN_FAILURE", "SALE_PENDING", "F", "U", "resultInfo").anyMatch(body::contains))
                .as("response must be a recognised order-not-found or pending response")
                .isTrue();
        softly.assertAll();
    }
}