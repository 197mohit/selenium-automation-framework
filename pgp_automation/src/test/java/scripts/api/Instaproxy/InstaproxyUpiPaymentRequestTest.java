package scripts.api.Instaproxy;

import com.paytm.api.Instaproxy.UpiPayment.InstaproxyUpiPaymentRequest;
import com.paytm.LocalConfig;
import com.paytm.apphelpers.InstaproxyUpiPaymentPayload;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.dto.instaproxy.upipayment.UpiPaymentRequestDTO;
import com.paytm.dto.instaproxy.upipayment.UpiPaymentResponseDTO;
import com.paytm.framework.reporting.Reporter;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;
import com.paytm.apphelpers.PG2LogsValidationHelper;
import com.paytm.apphelpers.LogsValidationHelper;
import static com.paytm.appconstants.Constants.Owner.BHARAT;
import com.paytm.utils.ff4j.FF4JFlags;
import com.paytm.apphelpers.RedisHelper;

public class InstaproxyUpiPaymentRequestTest extends PGPBaseTest {

    private static final String DEFAULT_REQ_MSG_ID = "12345";
    private static final String DEFAULT_SIGNATURE = "werwaeq";
   

    private final RedisHelper redisHelper =
            RedisHelper.getInstance(LocalConfig.TRANSACTIONAL_REDIS_CLUSTER_URI, LocalConfig.PG_REDIS_CLUSTER_PASS);

    @Owner(BHARAT)
    @Feature("PG-3765 - Instaproxy-UPI-Payment-Request")
    @Test(description = "POST instaproxy UPI payment via InitTxn-style executeUpiPaymentRequest (default headers)")
    public void postUpiPaymentRequest_executeStyle() {
        UpiPaymentRequestDTO dto = InstaproxyUpiPaymentPayload.buildDefaultRequestDto();
        Assertions.assertThat(dto.getExtSerialNo()).matches("\\d{19}");
        Assertions.assertThat(dto.getMerchantInfo().getMerchantTransId()).matches("\\d{17}");
        Assertions.assertThat(dto.getReqTime()).matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\+05:30");
        Assertions.assertThat(dto.getCreatedTime()).isEqualTo(dto.getReqTime());
        Assertions.assertThat(dto.getPayTime()).isEqualTo(dto.getReqTime());

        UpiPaymentResponseDTO respDto = InstaproxyUpiPaymentRequest.executeUpiPaymentRequest(dto);

        Reporter.report.info("PG2 UPI payment response extSerialNo: " + respDto.getExtSerialNo());
        Assertions.assertThat(respDto.getVersion()).isEqualTo("1.1.2");
        Assertions.assertThat(respDto.getFunction()).isEqualTo("pg.router.paytm.upi.payment.request");
        Assertions.assertThat(respDto.getAppId()).isEqualTo("PTYLC1IN07");
        Assertions.assertThat(respDto.getRespTime()).isNotBlank();
        Assertions.assertThat(respDto.getExtSerialNo()).isEqualTo(dto.getExtSerialNo());
        Assertions.assertThat(respDto.getMbid()).isEqualTo(dto.getMerchantInfo().getMerchantId());
        Assertions.assertThat(respDto.getPayeeVpa()).isEqualTo(dto.getMerchantInfo().getMerchantVPA());
        Assertions.assertThat(respDto.getResultInfo()).isNotNull();
        Assertions.assertThat(respDto.getResultInfo().getResultStatus()).isEqualTo("A");
        Assertions.assertThat(respDto.getResultInfo().getResultCodeId()).isEqualTo("00000009");
        Assertions.assertThat(respDto.getResultInfo().getResultCode()).isEqualTo("ACCEPTED_SUCCESS");
        Assertions.assertThat(respDto.getResultInfo().getResultMsg()).isEqualTo("Accepted");
    }

    @Owner(BHARAT)
    @Feature("PG-3765 - Instaproxy-UPI-Payment-Request")
    @Test(description = "POST instaproxy UPI payment with explicit reqMsgId, signature, and cookie")
    public void postUpiPaymentRequest_executeStyleWithHeaders() {
        UpiPaymentRequestDTO dto = InstaproxyUpiPaymentPayload.buildDefaultRequestDto();
        String cookie = InstaproxyUpiPaymentPayload.cookieFromConfig();

        UpiPaymentResponseDTO respDto = InstaproxyUpiPaymentRequest.executeUpiPaymentRequest(
                dto, DEFAULT_REQ_MSG_ID, DEFAULT_SIGNATURE, cookie);

        Assertions.assertThat(respDto.getVersion()).isEqualTo("1.1.2");
        Assertions.assertThat(respDto.getFunction()).isEqualTo("pg.router.paytm.upi.payment.request");
        Assertions.assertThat(respDto.getAppId()).isEqualTo("PTYLC1IN07");
        Assertions.assertThat(respDto.getRespTime()).isNotBlank();
        Assertions.assertThat(respDto.getExtSerialNo()).isEqualTo(dto.getExtSerialNo());
        Assertions.assertThat(respDto.getMbid()).isEqualTo(dto.getMerchantInfo().getMerchantId());
        Assertions.assertThat(respDto.getPayeeVpa()).isEqualTo(dto.getMerchantInfo().getMerchantVPA());
        Assertions.assertThat(respDto.getResultInfo()).isNotNull();
        Assertions.assertThat(respDto.getResultInfo().getResultStatus()).isEqualTo("A");
        Assertions.assertThat(respDto.getResultInfo().getResultCodeId()).isEqualTo("00000009");
        Assertions.assertThat(respDto.getResultInfo().getResultCode()).isEqualTo("ACCEPTED_SUCCESS");
        Assertions.assertThat(respDto.getResultInfo().getResultMsg()).isEqualTo("Accepted");
    }

    @Owner(BHARAT)
    @Feature("PG-3765 - Instaproxy-UPI-Payment-Request")
    @Test(description = "Verify when FF4J(INSTA_PTYL_CREDITLINE_DISABLE) is Enabled, UPI creditline is not passed in the request")
    public void verifyUpiCreditlineNotPassedWhenPrefEnabled() {

        FF4JFlags.enable("INSTA_PTYL_CREDITLINE_DISABLE");
        redisHelper.delete("FF4J_FEATURE_INSTA_PTYL_CREDITLINE_DISABLE");

        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError("Interrupted while waiting for FF4J disable to propagate", e);
        }
        
        UpiPaymentRequestDTO dto = InstaproxyUpiPaymentPayload.buildDefaultRequestDto();
        String cookie = InstaproxyUpiPaymentPayload.cookieFromConfig();

        UpiPaymentResponseDTO respDto = InstaproxyUpiPaymentRequest.executeUpiPaymentRequest(
                dto, DEFAULT_REQ_MSG_ID, DEFAULT_SIGNATURE, cookie);

        Assertions.assertThat(respDto.getVersion()).isEqualTo("1.1.2");
        Assertions.assertThat(respDto.getFunction()).isEqualTo("pg.router.paytm.upi.payment.request");
        Assertions.assertThat(respDto.getAppId()).isEqualTo("PTYLC1IN07");
        Assertions.assertThat(respDto.getRespTime()).isNotBlank();
        Assertions.assertThat(respDto.getExtSerialNo()).isEqualTo(dto.getExtSerialNo());
        Assertions.assertThat(respDto.getMbid()).isEqualTo(dto.getMerchantInfo().getMerchantId());
        Assertions.assertThat(respDto.getPayeeVpa()).isEqualTo(dto.getMerchantInfo().getMerchantVPA());
        Assertions.assertThat(respDto.getResultInfo()).isNotNull();
        Assertions.assertThat(respDto.getResultInfo().getResultStatus()).isEqualTo("A");
        Assertions.assertThat(respDto.getResultInfo().getResultCodeId()).isEqualTo("00000009");
        Assertions.assertThat(respDto.getResultInfo().getResultCode()).isEqualTo("ACCEPTED_SUCCESS");
        Assertions.assertThat(respDto.getResultInfo().getResultMsg()).isEqualTo("Accepted");

        final String upiPaymentRequestlog = LogsValidationHelper.verifyLogsOnPod(
                PG2LogsValidationHelper.setEnvService.instaproxy,
                respDto.getExtSerialNo(),
                "Payment Request"
        );

        System.out.println("UPI Payment Request Log: " + upiPaymentRequestlog);
        Assertions.assertThat(upiPaymentRequestlog)
                // AI-Generated: 2026-04-16 - Bug fix
                .as("Payment Request log must NOT include CREDITLINE_ALL when creditline is disabled")
                .doesNotContainPattern(
                        // logger may escape JSON array brackets as \\[ ... \\]
                        "\"merchantAllowedUpiPaymentInstruments\"\\s*:\\s*\\\\?\\[[^\\]]*\"CREDITLINE_ALL\""
                );
    }

    @Owner(BHARAT)
    @Feature("PG-3765 - Instaproxy-UPI-Payment-Request")
    @Test(description = "Verify when FF4J(INSTA_PTYL_CREDITLINE_DISABLE) is Disabled, UPI creditline is passed in the request")
    public void verifyUpiCreditlineIsPassedWhenPrefDisabled() {
        
        FF4JFlags.disable("INSTA_PTYL_CREDITLINE_DISABLE");
        redisHelper.delete("FF4J_FEATURE_INSTA_PTYL_CREDITLINE_DISABLE");

        
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError("Interrupted while waiting for FF4J disable to propagate", e);
        }

        UpiPaymentRequestDTO dto = InstaproxyUpiPaymentPayload.buildDefaultRequestDto();
        String cookie = InstaproxyUpiPaymentPayload.cookieFromConfig();

        UpiPaymentResponseDTO respDto = InstaproxyUpiPaymentRequest.executeUpiPaymentRequest(
                dto, DEFAULT_REQ_MSG_ID, DEFAULT_SIGNATURE, cookie);

        Assertions.assertThat(respDto.getVersion()).isEqualTo("1.1.2");
        Assertions.assertThat(respDto.getFunction()).isEqualTo("pg.router.paytm.upi.payment.request");
        Assertions.assertThat(respDto.getAppId()).isEqualTo("PTYLC1IN07");
        Assertions.assertThat(respDto.getRespTime()).isNotBlank();
        Assertions.assertThat(respDto.getExtSerialNo()).isEqualTo(dto.getExtSerialNo());
        Assertions.assertThat(respDto.getMbid()).isEqualTo(dto.getMerchantInfo().getMerchantId());
        Assertions.assertThat(respDto.getPayeeVpa()).isEqualTo(dto.getMerchantInfo().getMerchantVPA());
        Assertions.assertThat(respDto.getResultInfo()).isNotNull();
        Assertions.assertThat(respDto.getResultInfo().getResultStatus()).isEqualTo("A");
        Assertions.assertThat(respDto.getResultInfo().getResultCodeId()).isEqualTo("00000009");
        Assertions.assertThat(respDto.getResultInfo().getResultCode()).isEqualTo("ACCEPTED_SUCCESS");
        Assertions.assertThat(respDto.getResultInfo().getResultMsg()).isEqualTo("Accepted");

        final String upiPaymentRequestlog = LogsValidationHelper.verifyLogsOnPod(
                PG2LogsValidationHelper.setEnvService.instaproxy,
                respDto.getExtSerialNo(),
                "Payment Request"
        );

        System.out.println("UPI Payment Request Log: " + upiPaymentRequestlog);
        Assertions.assertThat(upiPaymentRequestlog)
                .as("Payment Request log must include allowed UPI instruments")
                .containsPattern(
                        // logger may escape JSON array brackets as \\[ ... \\]
                        "\"merchantAllowedUpiPaymentInstruments\"\\s*:\\s*\\\\?\\[[^\\]]*\"CREDITLINE_ALL\""
                );

                

    }
}
