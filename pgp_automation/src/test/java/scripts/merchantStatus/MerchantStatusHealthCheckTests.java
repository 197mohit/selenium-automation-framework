package scripts.merchantStatus;

import com.paytm.api.MerchantStatusHealthCheckApi;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.framework.reporting.Reporter;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.response.Response;
import org.assertj.core.api.SoftAssertions;
import org.testng.annotations.Test;

import static com.paytm.appconstants.Constants.Owner.MOHIT_KHARE;

/**
 * E2E tests for the merchant-status healthcheck endpoint:
 *   GET /merchant-status/healthcheck
 *
 * Target coverage:
 *   HealthCheckService.healthCheck()
 *     → if ${catalina.base}/conf/healthcheck.txt exists → 200 SUCCESS
 *     → else → 404 FAILED
 *
 * This endpoint is used by load balancer probes. The test verifies the service is
 * reachable and responds with a valid health status.
 */
public class MerchantStatusHealthCheckTests extends PGPBaseTest {

    /**
     * MS-HC-01
     * Verify GET /merchant-status/healthcheck returns a response with expected body.
     * When the service is healthy (healthcheck.txt present): HTTP 200 with body "SUCCESS".
     * When the service is draining (healthcheck.txt absent): HTTP 404 with body "FAILED".
     *
     * Branch: HealthCheckService.healthCheck()
     *   → file.exists() == true → return 200 SUCCESS
     *   → file.exists() == false → return 404 FAILED
     */
    @Owner(MOHIT_KHARE)
    @Feature("MS-HEALTHCHECK")
    @Test(description = "Verify /merchant-status/healthcheck returns SUCCESS or FAILED based on service state.")
    public void merchantStatus_HealthCheck() {
        Reporter.report.info("Testing GET /merchant-status/healthcheck");

        MerchantStatusHealthCheckApi api = new MerchantStatusHealthCheckApi();
        Response response = api.execute();

        String body = response.body().asString();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.statusCode())
                .as("HTTP status should be 200 (healthy) or 404 (draining)")
                .isIn(200, 404);
        softly.assertThat(body.contains("SUCCESS") || body.contains("FAILED"))
                .as("Body must contain SUCCESS or FAILED")
                .isTrue();
        softly.assertAll();
    }

    /**
     * MS-HC-02
     * Verify /merchant-status/healthcheck responds within reasonable latency.
     * The healthcheck should be a fast file-existence check, not a downstream dependency check.
     */
    @Owner(MOHIT_KHARE)
    @Feature("MS-HEALTHCHECK")
    @Test(description = "Verify /merchant-status/healthcheck responds within 5 seconds.")
    public void merchantStatus_HealthCheck_Latency() {
        Reporter.report.info("Testing healthcheck latency");

        MerchantStatusHealthCheckApi api = new MerchantStatusHealthCheckApi();

        long start = System.currentTimeMillis();
        Response response = api.execute();
        long elapsed = System.currentTimeMillis() - start;

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.statusCode())
                .as("HTTP status").isIn(200, 404);
        softly.assertThat(elapsed)
                .as("Healthcheck should respond within 5000ms")
                .isLessThan(5000L);
        softly.assertAll();
    }
}
