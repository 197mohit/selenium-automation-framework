package scripts.api.sts;

import com.paytm.api.sts.FundTransfer;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.base.test.PGPBaseTest;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.SoftAssertions;
import org.testng.annotations.Test;

import java.util.Map;

public class FundTransferAPI extends PGPBaseTest {

    @Feature("PGP-40714")
    @Owner(Constants.Owner.GAURAV)
    @Test(description = "Verify success response for Fund transfer API and verify the settlement transfer table status in DB - autoM2B")
    public void verifySuccessfulFundTransferWithAutoM2B() throws InterruptedException {
        FundTransfer fundTransfer = new FundTransfer().buildRequest(Constants.MerchantType.STS_MID.getId(),"1002002", false);
        JsonPath fundTransferResponse = fundTransfer.execute().jsonPath();
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(fundTransferResponse.getString("response.body.resultInfo.resultStatus")).isEqualTo("S");
        softAssertions.assertThat(fundTransferResponse.getString("response.body.resultInfo.resultMsg")).isEqualTo("SUCCESS");
        softAssertions.assertThat(fundTransferResponse.getString("response.body.orderId")).isNotNull();

        String payoutId = fundTransfer.getOrderId();
        Thread.sleep(3000);
        Map<String, Object> settlementTransferStatus = PGPHelpers.getSettlementTransferStatusFromDB(payoutId);

        softAssertions.assertThat(settlementTransferStatus.get("sts_transfer_id")).isNotNull();
        softAssertions.assertThat(settlementTransferStatus.get("payout_id")).isEqualTo(payoutId);
        softAssertions.assertThat(settlementTransferStatus.get("request_id")).isEqualTo(payoutId);
        softAssertions.assertThat(settlementTransferStatus.get("settlement_status")).isEqualTo("UNSETTLED");
        softAssertions.assertThat(settlementTransferStatus.get("approval_strategy")).isEqualTo("AUTOAPPROVAL");
        softAssertions.assertThat(settlementTransferStatus.get("sts_status_code")).isEqualTo("STS_400");
        softAssertions.assertThat(settlementTransferStatus.get("transaction_status")).isEqualTo("FUND_PENDING");
        softAssertions.assertThat(settlementTransferStatus.get("gateway_id")).isEqualTo("PPSL_PPBL_AUTO");
        softAssertions.assertThat(settlementTransferStatus.get("source_id")).isEqualTo("settlement");
        softAssertions.assertAll();
    }

    @Feature("PGP-40714")
    @Owner(Constants.Owner.GAURAV)
    @Test(description = "Verify success response for Fund transfer API and verify the settlement transfer table status in DB - manualM2B")
    public void verifySuccessfulFundTransferWithManualM2B() throws InterruptedException {
        FundTransfer fundTransfer = new FundTransfer().buildRequest(Constants.MerchantType.STS_MID.getId(),"1002002", true);
        JsonPath fundTransferResponse = fundTransfer.execute().jsonPath();
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(fundTransferResponse.getString("response.body.resultInfo.resultStatus")).isEqualTo("S");
        softAssertions.assertThat(fundTransferResponse.getString("response.body.resultInfo.resultMsg")).isEqualTo("SUCCESS");
        softAssertions.assertThat(fundTransferResponse.getString("response.body.orderId")).isNotNull();

        String payoutId = fundTransfer.getOrderId();
        Thread.sleep(3000);
        Map<String, Object> settlementTransferStatus = PGPHelpers.getSettlementTransferStatusFromDB(payoutId);

        softAssertions.assertThat(settlementTransferStatus.get("sts_transfer_id")).isNotNull();
        softAssertions.assertThat(settlementTransferStatus.get("payout_id")).isEqualTo(payoutId);
        softAssertions.assertThat(settlementTransferStatus.get("request_id")).isEqualTo(payoutId);
        softAssertions.assertThat(settlementTransferStatus.get("settlement_status")).isEqualTo("UNSETTLED");
        softAssertions.assertThat(settlementTransferStatus.get("approval_strategy")).isEqualTo("AUTOAPPROVAL");
        softAssertions.assertThat(settlementTransferStatus.get("sts_status_code")).isEqualTo("STS_400");
        softAssertions.assertThat(settlementTransferStatus.get("transaction_status")).isEqualTo("FUND_PENDING");
        softAssertions.assertThat(settlementTransferStatus.get("gateway_id")).isEqualTo("PPSL_PPBL_UPI");
        softAssertions.assertThat(settlementTransferStatus.get("source_id")).isEqualTo("settlement");
        softAssertions.assertAll();
    }
}
