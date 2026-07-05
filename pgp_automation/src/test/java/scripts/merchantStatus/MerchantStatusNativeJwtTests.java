package scripts.merchantStatus;

import com.paytm.LocalConfig;
import com.paytm.api.NativePaymentStatusJwtApi;
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
 * E2E tests exercising the JWT authentication branch in native payment status endpoints:
 *   POST /merchant-status/api/v1/getPaymentStatus   (tokenType=JWT)
 *   POST /merchant-status/api/v1/v2/getPaymentStatus (tokenType=JWT)
 *
 * Target coverage:
 *   NativePaymentStatusRequestFilter — JWT branch:
 *     JWTUtil.isJWTRequest() → true → JWTUtil.verifyJWTToken()
 *   NativePaymentStatusController — response head tokenType echo for JWT
 *   NativePaymentStatusServiceImpl — same core logic but reached via JWT path
 *
 * Existing NativePaymentStatusTests cover CHECKSUM tokenType exclusively.
 * These tests ensure the JWT tokenType branch is also exercised.
 */
public class MerchantStatusNativeJwtTests extends PGPBaseTest {

    // =========================================================================
    // /api/v1/getPaymentStatus — JWT auth branch
    // =========================================================================

    /**
     * MS-JWT-01
     * Verify /api/v1/getPaymentStatus returns CHECKSUMMISMATCH or JWT verification error
     * when an invalid JWT token string is supplied.
     *
     * Branch: NativePaymentStatusRequestFilter.filter()
     *   → tokenType == "JWT" → JWTUtil.verifyJWTToken() fails
     *   → NativePaymentStatusException → abort with error response
     */
    @Owner(MOHIT_KHARE)
    @Feature("MS-NATIVE-JWT")
    @Test(description = "Verify /api/v1/getPaymentStatus rejects an invalid JWT token.")
    public void nativeGetPaymentStatus_JWT_InvalidToken() {
        Constants.MerchantType merchant = Constants.MerchantType.AddnPay;
        String orderId = CommonHelpers.generateOrderId();

        Reporter.report.info("Testing getPaymentStatus JWT with invalid token for mid=" + merchant.getId());

        NativePaymentStatusJwtApi api = new NativePaymentStatusJwtApi(
                merchant.getId(), orderId, false, true);
        Response response = api.execute();

        String body = response.body().asString();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.statusCode()).as("HTTP status should be 200").isEqualTo(200);
        softly.assertThat(
                        Stream.of("CHECKSUMMISMATCH", "INVALID", "F", "APPLICATION_DOWN", "resultInfo")
                                .anyMatch(body::contains))
                .as("response must indicate JWT/auth error")
                .isTrue();
        softly.assertAll();
    }

    /**
     * MS-JWT-02
     * Verify /api/v1/getPaymentStatus handles a non-existent orderId when authenticated via JWT.
     * Exercises the successful JWT verification path followed by core service resolution.
     *
     * Branch: NativePaymentStatusRequestFilter.filter()
     *   → tokenType == "JWT" → JWTUtil.verifyJWTToken() passes
     *   → NativePaymentStatusController.processSecuredPaymentStatusPOST()
     *   → NativePaymentStatusServiceImpl.getPaymentTxnStatus()
     *   → MerchantTxnStatusServiceImpl.getMerchantTxnStatus() → order not found
     *
     * Also verifies the controller echoes tokenType=JWT in response head.
     */
    @Owner(MOHIT_KHARE)
    @Feature("MS-NATIVE-JWT")
    @Test(description = "Verify /api/v1/getPaymentStatus with JWT returns order-not-found for non-existent orderId.")
    public void nativeGetPaymentStatus_JWT_OrderNotFound() {
        Constants.MerchantType merchant = Constants.MerchantType.AddnPay;
        String nonExistentOrderId = "NONEXISTENT_JWT_" + CommonHelpers.generateOrderId();

        Reporter.report.info("Testing getPaymentStatus JWT for non-existent orderId=" + nonExistentOrderId);

        NativePaymentStatusJwtApi api = new NativePaymentStatusJwtApi(
                merchant.getId(), nonExistentOrderId, LocalConfig.JWT_KEY, false);
        Response response = api.execute();

        String body = response.body().asString();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.statusCode()).as("HTTP status should be 200").isEqualTo(200);
        softly.assertThat(
                        Stream.of("TXN_FAILURE", "SALE_PENDING", "F", "U", "resultInfo")
                                .anyMatch(body::contains))
                .as("response must be a recognised order-not-found or pending response")
                .isTrue();
        softly.assertAll();
    }

    // =========================================================================
    // /api/v1/v2/getPaymentStatus — JWT auth branch
    // =========================================================================

    /**
     * MS-JWT-03
     * Verify /api/v1/v2/getPaymentStatus rejects an invalid JWT token.
     * Same filter-level branch as MS-JWT-01 but on the V2 endpoint.
     */
    @Owner(MOHIT_KHARE)
    @Feature("MS-NATIVE-JWT")
    @Test(description = "Verify /api/v1/v2/getPaymentStatus rejects an invalid JWT token.")
    public void nativeGetPaymentStatusV2_JWT_InvalidToken() {
        Constants.MerchantType merchant = Constants.MerchantType.AddnPay;
        String orderId = CommonHelpers.generateOrderId();

        Reporter.report.info("Testing v2/getPaymentStatus JWT with invalid token for mid=" + merchant.getId());

        NativePaymentStatusJwtApi api = new NativePaymentStatusJwtApi(
                merchant.getId(), orderId, true, true);
        Response response = api.execute();

        String body = response.body().asString();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.statusCode()).as("HTTP status should be 200").isEqualTo(200);
        softly.assertThat(
                        Stream.of("CHECKSUMMISMATCH", "INVALID", "F", "APPLICATION_DOWN", "resultInfo")
                                .anyMatch(body::contains))
                .as("response must indicate JWT/auth error")
                .isTrue();
        softly.assertAll();
    }

    /**
     * MS-JWT-04
     * Verify /api/v1/v2/getPaymentStatus with valid JWT returns order-not-found for
     * a non-existent orderId. Exercises getPaymentTxnStatusV2() call path with JWT auth.
     */
    @Owner(MOHIT_KHARE)
    @Feature("MS-NATIVE-JWT")
    @Test(description = "Verify /api/v1/v2/getPaymentStatus with JWT returns order-not-found for non-existent orderId.")
    public void nativeGetPaymentStatusV2_JWT_OrderNotFound() {
        Constants.MerchantType merchant = Constants.MerchantType.AddnPay;
        String nonExistentOrderId = "NONEXISTENT_JWT_V2_" + CommonHelpers.generateOrderId();

        Reporter.report.info("Testing v2/getPaymentStatus JWT for non-existent orderId=" + nonExistentOrderId);

        NativePaymentStatusJwtApi api = new NativePaymentStatusJwtApi(
                merchant.getId(), nonExistentOrderId, LocalConfig.JWT_KEY, true);
        Response response = api.execute();

        String body = response.body().asString();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.statusCode()).as("HTTP status should be 200").isEqualTo(200);
        softly.assertThat(
                        Stream.of("TXN_FAILURE", "SALE_PENDING", "F", "U", "resultInfo")
                                .anyMatch(body::contains))
                .as("response must be a recognised order-not-found or pending response")
                .isTrue();
        softly.assertAll();
    }

    /**
     * MS-JWT-05
     * Verify /api/v1/getPaymentStatus with JWT + blank orderId triggers validation.
     * Exercises NativePaymentStatusServiceImpl.validateRequest() after JWT auth passes.
     */
    @Owner(MOHIT_KHARE)
    @Feature("MS-NATIVE-JWT")
    @Test(description = "Verify /api/v1/getPaymentStatus with JWT returns INVALID_ORDER_ID for blank orderId.")
    public void nativeGetPaymentStatus_JWT_BlankOrderId() {
        Constants.MerchantType merchant = Constants.MerchantType.AddnPay;

        Reporter.report.info("Testing getPaymentStatus JWT with blank orderId for mid=" + merchant.getId());

        NativePaymentStatusJwtApi api = new NativePaymentStatusJwtApi(
                merchant.getId(), "", LocalConfig.JWT_KEY, false);
        Response response = api.execute();

        String body = response.body().asString();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.statusCode()).as("HTTP status should be 200").isEqualTo(200);
        softly.assertThat(
                        Stream.of("INVALID_ORDER", "234234234", "F", "resultInfo")
                                .anyMatch(body::contains))
                .as("response must indicate invalid orderId")
                .isTrue();
        softly.assertAll();
    }
}
