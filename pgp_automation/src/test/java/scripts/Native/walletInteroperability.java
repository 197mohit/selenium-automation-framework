package scripts.Native;

import com.paytm.LocalConfig;
import com.paytm.api.PaymentService;
import com.paytm.api.StaticQrUpiPSP;
import com.paytm.api.qr.GenerateQR;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.PG2LogsValidationHelper;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.dto.UPIIntentRequestDTO;
import com.paytm.dto.upiIntent.staticQR.Response.StaticQrUpiPSPResponse;
import com.paytm.dto.upiIntent.staticQR.StaticQrUpiPSPRequest;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

import static com.paytm.appconstants.Constants.Owner.ABHISHEK_KULKARNI;
import static com.paytm.apphelpers.LogsValidationHelper.verifyLogsOnPod;



public class walletInteroperability extends PGPBaseTest {

    @Owner("Shubham Soni")
    @Feature("PGP-46237")
    @Test(description = "Verify Success Wallet Wallet Interpoability transaction when transaction amount less than 2000 for Online merchant, UPI_WALLET_BLACKLISTED :N & WALLET_ON_UPI_RAILS_ENABLED : Y")
    public void successUpiIntent_WI_TESTCASE01() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.WALINTER_ONLINE_NULL;
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(merchant.getId(), "200.0","yuioty","PPI_WALLET");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"PPI_WALLET");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.as(StaticQrUpiPSPResponse.class);
        Assertions.assertThat(staticQrUpiPSPResponse.getBody().getResultCode())
                .as("Result code mismatch")
                .isEqualToIgnoringCase("SUCCESS");
        UPIIntentRequestDTO upiIntentRequestDTO = new UPIIntentRequestDTO();
        upiIntentRequestDTO.setAmount("200.0")
                .setInstaUrl(LocalConfig.PGP_HOST)
                .setOrderId(staticQrUpiPSPResponse.getBody().getOrderId())
                .setExternalSerialNo(staticQrUpiPSPResponse.getBody().getExternalSerialNo())
                .setTxnStatus(Constants.Intent_Callback.SUCCESS.getStatus())
                .setResponseCode(Constants.Intent_Callback.SUCCESS.getRespCode())
                .setResponseMessage(Constants.Intent_Callback.SUCCESS.getRespmsg())
                .setMid(merchant.getId());
        response = PGPHelpers.generateUpiIntentPayRequest(upiIntentRequestDTO);
        Assertions.assertThat(response.jsonPath().getString("body.resultCode"))
                .as("Result code mismatch")
                .isEqualToIgnoringCase("SUCCESS");
        PGPHelpers.getTxnStatus(merchant.getId(), staticQrUpiPSPResponse.getBody().getOrderId())
                .validateStatus("TXN_SUCCESS")
                .validatePaymentMode("UPI")
                .validateGatewayName("PPBL")
                .AssertAll();
        String theia_facade = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,staticQrUpiPSPResponse.getBody().getOrderId(),"ACQUIRING_CREATE_ORDER_AND_PAY");
        Assertions.assertThat(theia_facade).contains("feeRateFactorsInfo\":\"{\"solutionWiseMdr\":\"QR\",\"upiModeSubType\":\"UPI_PPIWALLET\"}");
        Assertions.assertThat(theia_facade).contains("\"upiCC\":false,\"upiModeSubType\":\"UPI_PPIWALLET\"");

    }

    @Owner("Shubham Soni")
    @Feature("PGP-46237")
    @Test(description = "Verify Success Wallet Wallet Interpoability transaction when transaction amount more than 2000 for Online merchant, UPI_WALLET_BLACKLISTED :N & WALLET_ON_UPI_RAILS_ENABLED : Y")
    public void successUpiIntent_WI_TESTCASE02() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.WALINTER_ONLINE_NULL;
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(merchant.getId(), "2500.0","yuioty","PPI_WALLET");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"PPI_WALLET");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.as(StaticQrUpiPSPResponse.class);
        Assertions.assertThat(staticQrUpiPSPResponse.getBody().getResultCode())
                .as("Result code mismatch")
                .isEqualToIgnoringCase("SUCCESS");
        UPIIntentRequestDTO upiIntentRequestDTO = new UPIIntentRequestDTO();
        upiIntentRequestDTO.setAmount("2500.0")
                .setInstaUrl(LocalConfig.PGP_HOST)
                .setOrderId(staticQrUpiPSPResponse.getBody().getOrderId())
                .setExternalSerialNo(staticQrUpiPSPResponse.getBody().getExternalSerialNo())
                .setTxnStatus(Constants.Intent_Callback.SUCCESS.getStatus())
                .setResponseCode(Constants.Intent_Callback.SUCCESS.getRespCode())
                .setResponseMessage(Constants.Intent_Callback.SUCCESS.getRespmsg())
                .setMid(merchant.getId());
        response = PGPHelpers.generateUpiIntentPayRequest(upiIntentRequestDTO);
        Assertions.assertThat(response.jsonPath().getString("body.resultCode"))
                .as("Result code mismatch")
                .isEqualToIgnoringCase("SUCCESS");
        PGPHelpers.getTxnStatus(merchant.getId(), staticQrUpiPSPResponse.getBody().getOrderId())
                .validateStatus("TXN_SUCCESS")
                .validatePaymentMode("UPI")
                .validateGatewayName("PPBL")
                .AssertAll();
        String theia_facade = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,staticQrUpiPSPResponse.getBody().getOrderId(),"ACQUIRING_CREATE_ORDER_AND_PAY");
        Assertions.assertThat(theia_facade).contains("feeRateFactorsInfo\":\"{\"solutionWiseMdr\":\"QR\",\"upiModeSubType\":\"UPI_PPIWALLET\"}");
        Assertions.assertThat(theia_facade).contains("\"upiCC\":false,\"upiModeSubType\":\"UPI_PPIWALLET\"");

    }

    @Owner("Shubham Soni")
    @Feature("PGP-46237")
    @Test(description = "Verify Success Wallet Wallet Interpoability transaction when transaction amount equal to 2000 for Online merchant, UPI_WALLET_BLACKLISTED :N & WALLET_ON_UPI_RAILS_ENABLED : Y")
    public void successUpiIntent_WI_TESTCASE03() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.WALINTER_ONLINE_NULL;
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(merchant.getId(), "2000.0","yuioty","PPI_WALLET");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"PPI_WALLET");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.as(StaticQrUpiPSPResponse.class);
        Assertions.assertThat(staticQrUpiPSPResponse.getBody().getResultCode())
                .as("Result code mismatch")
                .isEqualToIgnoringCase("SUCCESS");
        UPIIntentRequestDTO upiIntentRequestDTO = new UPIIntentRequestDTO();
        upiIntentRequestDTO.setAmount("2000.0")
                .setInstaUrl(LocalConfig.PGP_HOST)
                .setOrderId(staticQrUpiPSPResponse.getBody().getOrderId())
                .setExternalSerialNo(staticQrUpiPSPResponse.getBody().getExternalSerialNo())
                .setTxnStatus(Constants.Intent_Callback.SUCCESS.getStatus())
                .setResponseCode(Constants.Intent_Callback.SUCCESS.getRespCode())
                .setResponseMessage(Constants.Intent_Callback.SUCCESS.getRespmsg())
                .setMid(merchant.getId());
        response = PGPHelpers.generateUpiIntentPayRequest(upiIntentRequestDTO);
        Assertions.assertThat(response.jsonPath().getString("body.resultCode"))
                .as("Result code mismatch")
                .isEqualToIgnoringCase("SUCCESS");
        PGPHelpers.getTxnStatus(merchant.getId(), staticQrUpiPSPResponse.getBody().getOrderId())
                .validateStatus("TXN_SUCCESS")
                .validatePaymentMode("UPI")
                .validateGatewayName("PPBL")
                .AssertAll();
        String theia_facade = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,staticQrUpiPSPResponse.getBody().getOrderId(),"ACQUIRING_CREATE_ORDER_AND_PAY");
        Assertions.assertThat(theia_facade).contains("feeRateFactorsInfo\":\"{\"solutionWiseMdr\":\"QR\",\"upiModeSubType\":\"UPI_PPIWALLET\"}");
        Assertions.assertThat(theia_facade).contains("\"upiCC\":false,\"upiModeSubType\":\"UPI_PPIWALLET\"");

    }

    @Owner("Shubham Soni")
    @Feature("PGP-46237")
    @Test(description = "Verify failure Wallet Wallet Interpoability transaction when transaction amount less than 2000 for Online merchant, UPI_WALLET_BLACKLISTED :Y & WALLET_ON_UPI_RAILS_ENABLED : Y")
    public void failureUpiIntent_WI_TESTCASE04() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.WALINTER_ONLINE_SMALL;
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(merchant.getId(), "200.0","yuioty","PPI_WALLET");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"PPI_WALLET");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.as(StaticQrUpiPSPResponse.class);
        Assertions.assertThat(staticQrUpiPSPResponse.getBody().getResultCode())
                .as("Result code mismatch")
                .isEqualToIgnoringCase("FAIL");
        Assertions.assertThat(staticQrUpiPSPResponse.getBody().getSubResultCodeId().equals("003"));
        String theiaLogs = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia,staticQrUpiPSPRequest.getHeader().getRequestMsgId());
        Assertions.assertThat(theiaLogs).contains("UPIPSPResponse [resultCode=FAIL, resultCodeId=009, resultMsg=Merchant Ineligible UPI_PPIWALLET");
        Assertions.assertThat(theiaLogs).contains("Wallet On UPI is not enabled");
        //Assertions.assertThat(theiaLogs).contains("preferenceName=UPI_WALLET_BLACKLISTED, enabled=true, preferenceValue=Y");
    }

    @Owner("Shubham Soni")
    @Feature("PGP-46237")
    @Test(description = "Verify failure Wallet Wallet Interpoability transaction when transaction amount more than 2000 for Online merchant, UPI_WALLET_BLACKLISTED :Y & WALLET_ON_UPI_RAILS_ENABLED : Y")
    public void failureUpiIntent_WI_TESTCASE05() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.WALINTER_ONLINE_SMALL;
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(merchant.getId(), "2500.0","yuioty","PPI_WALLET");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"PPI_WALLET");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.as(StaticQrUpiPSPResponse.class);
        Assertions.assertThat(staticQrUpiPSPResponse.getBody().getResultCode())
                .as("Result code mismatch")
                .isEqualToIgnoringCase("FAIL");
        Assertions.assertThat(staticQrUpiPSPResponse.getBody().getSubResultCodeId().equals("003"));
        String theiaLogs = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia,staticQrUpiPSPRequest.getHeader().getRequestMsgId());
        Assertions.assertThat(theiaLogs).contains("UPIPSPResponse [resultCode=FAIL, resultCodeId=009, resultMsg=Merchant Ineligible UPI_PPIWALLET");
        Assertions.assertThat(theiaLogs).contains("Wallet On UPI is not enabled");
        Assertions.assertThat(theiaLogs).contains("preferenceName=UPI_WALLET_BLACKLISTED, enabled=true, preferenceValue=Y");

    }

    @Owner("Shubham Soni")
    @Feature("PGP-46237")
    @Test(description = "Verify failure Wallet Wallet Interpoability transaction when transaction amount equal to 2000 for Online merchant, UPI_WALLET_BLACKLISTED :Y & WALLET_ON_UPI_RAILS_ENABLED : Y")
    public void failureUpiIntent_WI_TESTCASE06() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.WALINTER_ONLINE_SMALL;
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(merchant.getId(), "2000.0","yuioty","PPI_WALLET");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"PPI_WALLET");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.as(StaticQrUpiPSPResponse.class);
        Assertions.assertThat(staticQrUpiPSPResponse.getBody().getResultCode())
                .as("Result code mismatch")
                .isEqualToIgnoringCase("FAIL");
        Assertions.assertThat(staticQrUpiPSPResponse.getBody().getSubResultCodeId().equals("003"));
        String theiaLogs = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia,staticQrUpiPSPRequest.getHeader().getRequestMsgId());
        Assertions.assertThat(theiaLogs).contains("UPIPSPResponse [resultCode=FAIL, resultCodeId=009, resultMsg=Merchant Ineligible UPI_PPIWALLET");
        Assertions.assertThat(theiaLogs).contains("Wallet On UPI is not enabled");
        Assertions.assertThat(theiaLogs).contains("preferenceName=UPI_WALLET_BLACKLISTED, enabled=true, preferenceValue=Y");

    }
    @Owner("Shubham Soni")
    @Feature("PGP-46237")
    @Test(description = "Verify Success Wallet Wallet Interpoability transaction when transaction amount less than 2000 for OFFLINE merchant Industry Type Small, UPI_WALLET_BLACKLISTED :N & WALLET_ON_UPI_RAILS_ENABLED : Y")
    public void successUpiIntent_WI_TESTCASE07() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.WALINTER_OFFLINE_SMALL;
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(merchant.getId(), "2.0","yuioty","PPI_WALLET");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"PPI_WALLET");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.as(StaticQrUpiPSPResponse.class);
        Assertions.assertThat(staticQrUpiPSPResponse.getBody().getResultCode())
                .as("Result code mismatch")
                .isEqualToIgnoringCase("SUCCESS");
        UPIIntentRequestDTO upiIntentRequestDTO = new UPIIntentRequestDTO();
        upiIntentRequestDTO.setAmount("2.0")
                .setInstaUrl(LocalConfig.PGP_HOST)
                .setOrderId(staticQrUpiPSPResponse.getBody().getOrderId())
                .setExternalSerialNo(staticQrUpiPSPResponse.getBody().getExternalSerialNo())
                .setTxnStatus(Constants.Intent_Callback.SUCCESS.getStatus())
                .setResponseCode(Constants.Intent_Callback.SUCCESS.getRespCode())
                .setResponseMessage(Constants.Intent_Callback.SUCCESS.getRespmsg())
                .setMid(merchant.getId());
        response = PGPHelpers.generateUpiIntentPayRequest(upiIntentRequestDTO);
        Assertions.assertThat(response.jsonPath().getString("body.resultCode"))
                .as("Result code mismatch")
                .isEqualToIgnoringCase("SUCCESS");
        PGPHelpers.getTxnStatus(merchant.getId(), staticQrUpiPSPResponse.getBody().getOrderId())
                .validateStatus("TXN_SUCCESS")
                .validatePaymentMode("UPI")
                .validateGatewayName("PPBL")
                .AssertAll();
        String theia_facade = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,staticQrUpiPSPResponse.getBody().getOrderId(),"ACQUIRING_CREATE_ORDER_AND_PAY");
        Assertions.assertThat(theia_facade).contains("feeRateFactorsInfo\":\"{\"solutionWiseMdr\":\"QR\",\"upiModeSubType\":\"UPI_PPIWALLET\"}");
        Assertions.assertThat(theia_facade).contains("\"isDeepLinkFlow\":false,\"upiCC\":false,\"upiModeSubType\":\"UPI_PPIWALLET\"");

    }

    @Owner("Shubham Soni")
    @Feature("PGP-46237")
    @Test(description = "Verify Success Wallet Wallet Interpoability transaction when transaction amount more than 2000 for OFFLINE merchant Industry Type Small, UPI_WALLET_BLACKLISTED :N & WALLET_ON_UPI_RAILS_ENABLED : Y")
    public void successUpiIntent_WI_TESTCASE08() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.WALINTER_OFFLINE_SMALL;
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(merchant.getId(), "2500.0","yuioty","PPI_WALLET");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"PPI_WALLET");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.as(StaticQrUpiPSPResponse.class);
        Assertions.assertThat(staticQrUpiPSPResponse.getBody().getResultCode())
                .as("Result code mismatch")
                .isEqualToIgnoringCase("SUCCESS");
        UPIIntentRequestDTO upiIntentRequestDTO = new UPIIntentRequestDTO();
        upiIntentRequestDTO.setAmount("2500.0")
                .setInstaUrl(LocalConfig.PGP_HOST)
                .setOrderId(staticQrUpiPSPResponse.getBody().getOrderId())
                .setExternalSerialNo(staticQrUpiPSPResponse.getBody().getExternalSerialNo())
                .setTxnStatus(Constants.Intent_Callback.SUCCESS.getStatus())
                .setResponseCode(Constants.Intent_Callback.SUCCESS.getRespCode())
                .setResponseMessage(Constants.Intent_Callback.SUCCESS.getRespmsg())
                .setMid(merchant.getId());
        response = PGPHelpers.generateUpiIntentPayRequest(upiIntentRequestDTO);
        Assertions.assertThat(response.jsonPath().getString("body.resultCode"))
                .as("Result code mismatch")
                .isEqualToIgnoringCase("SUCCESS");
        PGPHelpers.getTxnStatus(merchant.getId(), staticQrUpiPSPResponse.getBody().getOrderId())
                .validateStatus("TXN_SUCCESS")
                .validatePaymentMode("UPI")
                .validateGatewayName("PPBL")
                .AssertAll();
        String theia_facade = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,staticQrUpiPSPResponse.getBody().getOrderId(),"ACQUIRING_CREATE_ORDER_AND_PAY");
        Assertions.assertThat(theia_facade).contains("feeRateFactorsInfo\":\"{\"solutionWiseMdr\":\"QR\",\"upiModeSubType\":\"UPI_PPIWALLET\"}");
        Assertions.assertThat(theia_facade).contains("\"isDeepLinkFlow\":false,\"upiCC\":false,\"upiModeSubType\":\"UPI_PPIWALLET\"");

    }

    @Owner("Shubham Soni")
    @Feature("PGP-46237")
    @Test(description = "Verify Success Wallet Wallet Interpoability transaction when transaction amount equal to 2000 for OFFLINE merchant Industry Type Small, UPI_WALLET_BLACKLISTED :N & WALLET_ON_UPI_RAILS_ENABLED : Y")
    public void successUpiIntent_WI_TESTCASE09() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.WALINTER_OFFLINE_SMALL;
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(merchant.getId(), "2000.0","yuioty","PPI_WALLET");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"PPI_WALLET");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.as(StaticQrUpiPSPResponse.class);
        Assertions.assertThat(staticQrUpiPSPResponse.getBody().getResultCode())
                .as("Result code mismatch")
                .isEqualToIgnoringCase("SUCCESS");
        UPIIntentRequestDTO upiIntentRequestDTO = new UPIIntentRequestDTO();
        upiIntentRequestDTO.setAmount("2000.0")
                .setInstaUrl(LocalConfig.PGP_HOST)
                .setOrderId(staticQrUpiPSPResponse.getBody().getOrderId())
                .setExternalSerialNo(staticQrUpiPSPResponse.getBody().getExternalSerialNo())
                .setTxnStatus(Constants.Intent_Callback.SUCCESS.getStatus())
                .setResponseCode(Constants.Intent_Callback.SUCCESS.getRespCode())
                .setResponseMessage(Constants.Intent_Callback.SUCCESS.getRespmsg())
                .setMid(merchant.getId());
        response = PGPHelpers.generateUpiIntentPayRequest(upiIntentRequestDTO);
        Assertions.assertThat(response.jsonPath().getString("body.resultCode"))
                .as("Result code mismatch")
                .isEqualToIgnoringCase("SUCCESS");
        PGPHelpers.getTxnStatus(merchant.getId(), staticQrUpiPSPResponse.getBody().getOrderId())
                .validateStatus("TXN_SUCCESS")
                .validatePaymentMode("UPI")
                .validateGatewayName("PPBL")
                .AssertAll();
        String theia_facade = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,staticQrUpiPSPResponse.getBody().getOrderId(),"ACQUIRING_CREATE_ORDER_AND_PAY");
        Assertions.assertThat(theia_facade).contains("feeRateFactorsInfo\":\"{\"solutionWiseMdr\":\"QR\",\"upiModeSubType\":\"UPI_PPIWALLET\"}");
        Assertions.assertThat(theia_facade).contains("\"isDeepLinkFlow\":false,\"upiCC\":false,\"upiModeSubType\":\"UPI_PPIWALLET\"");

    }
    @Owner("Shubham Soni")
    @Feature("PGP-46237")
    @Test(description = "Verify Success Wallet Wallet Interpoability transaction when transaction amount less than 2000 for OFFLINE merchant Industry Type NULL(Same as Small), UPI_WALLET_BLACKLISTED :N & WALLET_ON_UPI_RAILS_ENABLED : N")
    public void successUpiIntent_WI_TESTCASE10() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.WALINTER_OFFLINE_NULL;
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(merchant.getId(), "200.0","yuioty","PPI_WALLET");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"PPI_WALLET");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.as(StaticQrUpiPSPResponse.class);
        Assertions.assertThat(staticQrUpiPSPResponse.getBody().getResultCode())
                .as("Result code mismatch")
                .isEqualToIgnoringCase("SUCCESS");
        UPIIntentRequestDTO upiIntentRequestDTO = new UPIIntentRequestDTO();
        upiIntentRequestDTO.setAmount("200.0")
                .setInstaUrl(LocalConfig.PGP_HOST)
                .setOrderId(staticQrUpiPSPResponse.getBody().getOrderId())
                .setExternalSerialNo(staticQrUpiPSPResponse.getBody().getExternalSerialNo())
                .setTxnStatus(Constants.Intent_Callback.SUCCESS.getStatus())
                .setResponseCode(Constants.Intent_Callback.SUCCESS.getRespCode())
                .setResponseMessage(Constants.Intent_Callback.SUCCESS.getRespmsg())
                .setMid(merchant.getId());
        response = PGPHelpers.generateUpiIntentPayRequest(upiIntentRequestDTO);
        Assertions.assertThat(response.jsonPath().getString("body.resultCode"))
                .as("Result code mismatch")
                .isEqualToIgnoringCase("SUCCESS");
        PGPHelpers.getTxnStatus(merchant.getId(), staticQrUpiPSPResponse.getBody().getOrderId())
                .validateStatus("TXN_SUCCESS")
                .validatePaymentMode("UPI")
                .validateGatewayName("PPBL")
                .AssertAll();
        String theia_facade = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,staticQrUpiPSPResponse.getBody().getOrderId(),"ACQUIRING_CREATE_ORDER_AND_PAY");
        Assertions.assertThat(theia_facade).contains("feeRateFactorsInfo\":\"{\"solutionWiseMdr\":\"QR\",\"upiModeSubType\":\"UPI_PPIWALLET\"}");
        Assertions.assertThat(theia_facade).contains("\"upiCC\":false,\"upiModeSubType\":\"UPI_PPIWALLET\"");

    }

    @Owner("Shubham Soni")
    @Feature("PGP-46237")
    @Test(description = "Verify Failure Wallet Wallet Interpoability transaction when transaction amount more than 2000 for OFFLINE merchant Industry Type NULL(Same as Small), UPI_WALLET_BLACKLISTED :N & WALLET_ON_UPI_RAILS_ENABLED : N")
    public void failureUpiIntent_WI_TESTCASE11() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.WALINTER_OFFLINE_NULL;
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(merchant.getId(), "2500.0","yuioty","PPI_WALLET");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"PPI_WALLET");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.as(StaticQrUpiPSPResponse.class);
        Assertions.assertThat(staticQrUpiPSPResponse.getBody().getResultCode())
                .as("Result code mismatch")
                .isEqualToIgnoringCase("FAIL");
        String theiaLogs = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia,staticQrUpiPSPRequest.getHeader().getRequestMsgId());
        Assertions.assertThat(theiaLogs).contains("UPIPSPResponse [resultCode=FAIL, resultCodeId=009, resultMsg=Payment failed. This merchant can accept Wallet on UPI upto Rs. 2000.");
        Assertions.assertThat(theiaLogs).contains("offline ,small,Not enabled preference inside this where merchantType");
        Assertions.assertThat(theiaLogs).contains("Payment failed. This merchant can accept Wallet on UPI upto Rs. 2000.");

    }

    @Owner("Shubham Soni")
    @Feature("PGP-46237")
    @Test(description = "Verify Success Wallet Wallet Interpoability transaction when transaction amount equal to 2000 for OFFLINE merchant Industry Type NULL(Same as Small), UPI_WALLET_BLACKLISTED :N & WALLET_ON_UPI_RAILS_ENABLED : N")
    public void successUpiIntent_WI_TESTCASE12() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.WALINTER_OFFLINE_NULL;
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(merchant.getId(), "2000.0","yuioty","PPI_WALLET");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"PPI_WALLET");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.as(StaticQrUpiPSPResponse.class);
        Assertions.assertThat(staticQrUpiPSPResponse.getBody().getResultCode())
                .as("Result code mismatch")
                .isEqualToIgnoringCase("SUCCESS");
        UPIIntentRequestDTO upiIntentRequestDTO = new UPIIntentRequestDTO();
        upiIntentRequestDTO.setAmount("2000.0")
                .setInstaUrl(LocalConfig.PGP_HOST)
                .setOrderId(staticQrUpiPSPResponse.getBody().getOrderId())
                .setExternalSerialNo(staticQrUpiPSPResponse.getBody().getExternalSerialNo())
                .setTxnStatus(Constants.Intent_Callback.SUCCESS.getStatus())
                .setResponseCode(Constants.Intent_Callback.SUCCESS.getRespCode())
                .setResponseMessage(Constants.Intent_Callback.SUCCESS.getRespmsg())
                .setMid(merchant.getId());
        response = PGPHelpers.generateUpiIntentPayRequest(upiIntentRequestDTO);
        Assertions.assertThat(response.jsonPath().getString("body.resultCode"))
                .as("Result code mismatch")
                .isEqualToIgnoringCase("SUCCESS");
        PGPHelpers.getTxnStatus(merchant.getId(), staticQrUpiPSPResponse.getBody().getOrderId())
                .validateStatus("TXN_SUCCESS")
                .validatePaymentMode("UPI")
                .validateGatewayName("PPBL")
                .AssertAll();
        String theia_facade = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,staticQrUpiPSPResponse.getBody().getOrderId(),"ACQUIRING_CREATE_ORDER_AND_PAY");
        Assertions.assertThat(theia_facade).contains("feeRateFactorsInfo\":\"{\"solutionWiseMdr\":\"QR\",\"upiModeSubType\":\"UPI_PPIWALLET\"}");
        Assertions.assertThat(theia_facade).contains("\"upiCC\":false,\"upiModeSubType\":\"UPI_PPIWALLET\"");

    }
    @Owner("Shubham Soni")
    @Feature("PGP-46237")
    @Test(description = "Verify Success Wallet Wallet Interpoability transaction when transaction amount less than 2000 for OFFLINE merchant Industry Type BIG, UPI_WALLET_BLACKLISTED :N & WALLET_ON_UPI_RAILS_ENABLED : Y")
    public void successUpiIntent_WI_TESTCASE13() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.WALINTER_OFFLINE_BIG;
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(merchant.getId(), "200.0","yuioty","PPI_WALLET");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"PPI_WALLET");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.as(StaticQrUpiPSPResponse.class);
        Assertions.assertThat(staticQrUpiPSPResponse.getBody().getResultCode())
                .as("Result code mismatch")
                .isEqualToIgnoringCase("SUCCESS");
        UPIIntentRequestDTO upiIntentRequestDTO = new UPIIntentRequestDTO();
        upiIntentRequestDTO.setAmount("200.0")
                .setInstaUrl(LocalConfig.PGP_HOST)
                .setOrderId(staticQrUpiPSPResponse.getBody().getOrderId())
                .setExternalSerialNo(staticQrUpiPSPResponse.getBody().getExternalSerialNo())
                .setTxnStatus(Constants.Intent_Callback.SUCCESS.getStatus())
                .setResponseCode(Constants.Intent_Callback.SUCCESS.getRespCode())
                .setResponseMessage(Constants.Intent_Callback.SUCCESS.getRespmsg())
                .setMid(merchant.getId());
        response = PGPHelpers.generateUpiIntentPayRequest(upiIntentRequestDTO);
        Assertions.assertThat(response.jsonPath().getString("body.resultCode"))
                .as("Result code mismatch")
                .isEqualToIgnoringCase("SUCCESS");
        PGPHelpers.getTxnStatus(merchant.getId(), staticQrUpiPSPResponse.getBody().getOrderId())
                .validateStatus("TXN_SUCCESS")
                .validatePaymentMode("UPI")
                .validateGatewayName("PPBL")
                .AssertAll();
        String theia_facade = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,staticQrUpiPSPResponse.getBody().getOrderId(),"ACQUIRING_CREATE_ORDER_AND_PAY");
        Assertions.assertThat(theia_facade).contains("feeRateFactorsInfo\":\"{\"solutionWiseMdr\":\"QR\",\"upiModeSubType\":\"UPI_PPIWALLET\"}");
        Assertions.assertThat(theia_facade).contains("\"upiCC\":false,\"upiModeSubType\":\"UPI_PPIWALLET\"");

    }

    @Owner("Shubham Soni")
    @Feature("PGP-46237")
    @Test(description = "Verify Success Wallet Wallet Interpoability transaction when transaction amount more than 2000 for OFFLINE merchant Industry Type BIG, UPI_WALLET_BLACKLISTED :N & WALLET_ON_UPI_RAILS_ENABLED : Y")
    public void successUpiIntent_WI_TESTCASE14() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.WALINTER_OFFLINE_BIG;
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(merchant.getId(), "2500.0","yuioty","PPI_WALLET");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"PPI_WALLET");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.as(StaticQrUpiPSPResponse.class);
        Assertions.assertThat(staticQrUpiPSPResponse.getBody().getResultCode())
                .as("Result code mismatch")
                .isEqualToIgnoringCase("SUCCESS");
        UPIIntentRequestDTO upiIntentRequestDTO = new UPIIntentRequestDTO();
        upiIntentRequestDTO.setAmount("2500.0")
                .setInstaUrl(LocalConfig.PGP_HOST)
                .setOrderId(staticQrUpiPSPResponse.getBody().getOrderId())
                .setExternalSerialNo(staticQrUpiPSPResponse.getBody().getExternalSerialNo())
                .setTxnStatus(Constants.Intent_Callback.SUCCESS.getStatus())
                .setResponseCode(Constants.Intent_Callback.SUCCESS.getRespCode())
                .setResponseMessage(Constants.Intent_Callback.SUCCESS.getRespmsg())
                .setMid(merchant.getId());
        response = PGPHelpers.generateUpiIntentPayRequest(upiIntentRequestDTO);
        Assertions.assertThat(response.jsonPath().getString("body.resultCode"))
                .as("Result code mismatch")
                .isEqualToIgnoringCase("SUCCESS");
        PGPHelpers.getTxnStatus(merchant.getId(), staticQrUpiPSPResponse.getBody().getOrderId())
                .validateStatus("TXN_SUCCESS")
                .validatePaymentMode("UPI")
                .validateGatewayName("PPBL")
                .AssertAll();
        String theia_facade = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,staticQrUpiPSPResponse.getBody().getOrderId(),"ACQUIRING_CREATE_ORDER_AND_PAY");
        Assertions.assertThat(theia_facade).contains("feeRateFactorsInfo\":\"{\"solutionWiseMdr\":\"QR\",\"upiModeSubType\":\"UPI_PPIWALLET\"}");
        Assertions.assertThat(theia_facade).contains("\"upiCC\":false,\"upiModeSubType\":\"UPI_PPIWALLET\"");

    }

    @Owner("Shubham Soni")
    @Feature("PGP-46237")
    @Test(description = "Verify Success Wallet Wallet Interpoability transaction when transaction amount equal to 2000 for OFFLINE merchant Industry Type BIG, UPI_WALLET_BLACKLISTED :N & WALLET_ON_UPI_RAILS_ENABLED : Y")
    public void successUpiIntent_WI_TESTCASE15() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.WALINTER_OFFLINE_BIG;
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(merchant.getId(), "2000.0","yuioty","PPI_WALLET");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"PPI_WALLET");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.as(StaticQrUpiPSPResponse.class);
        Assertions.assertThat(staticQrUpiPSPResponse.getBody().getResultCode())
                .as("Result code mismatch")
                .isEqualToIgnoringCase("SUCCESS");
        UPIIntentRequestDTO upiIntentRequestDTO = new UPIIntentRequestDTO();
        upiIntentRequestDTO.setAmount("2000.0")
                .setInstaUrl(LocalConfig.PGP_HOST)
                .setOrderId(staticQrUpiPSPResponse.getBody().getOrderId())
                .setExternalSerialNo(staticQrUpiPSPResponse.getBody().getExternalSerialNo())
                .setTxnStatus(Constants.Intent_Callback.SUCCESS.getStatus())
                .setResponseCode(Constants.Intent_Callback.SUCCESS.getRespCode())
                .setResponseMessage(Constants.Intent_Callback.SUCCESS.getRespmsg())
                .setMid(merchant.getId());
        response = PGPHelpers.generateUpiIntentPayRequest(upiIntentRequestDTO);
        Assertions.assertThat(response.jsonPath().getString("body.resultCode"))
                .as("Result code mismatch")
                .isEqualToIgnoringCase("SUCCESS");
        PGPHelpers.getTxnStatus(merchant.getId(), staticQrUpiPSPResponse.getBody().getOrderId())
                .validateStatus("TXN_SUCCESS")
                .validatePaymentMode("UPI")
                .validateGatewayName("PPBL")
                .AssertAll();
        String theia_facade = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,staticQrUpiPSPResponse.getBody().getOrderId(),"ACQUIRING_CREATE_ORDER_AND_PAY");
        Assertions.assertThat(theia_facade).contains("feeRateFactorsInfo\":\"{\"solutionWiseMdr\":\"QR\",\"upiModeSubType\":\"UPI_PPIWALLET\"}");
        Assertions.assertThat(theia_facade).contains("\"upiCC\":false,\"upiModeSubType\":\"UPI_PPIWALLET\"");

    }

    @Owner("Shubham Soni")
    @Feature("PGP-46237")
    @Test(description = "Verify failure Wallet Wallet Interpoability transaction when transaction amount less than 2000 for OFFLINE merchant Industry Type BIG, UPI_WALLET_BLACKLISTED :N & WALLET_ON_UPI_RAILS_ENABLED : N")
    public void failureUpiIntent_WI_TESTCASE16() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.WALINTER_OFFLINE_BIG1;
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(merchant.getId(), "200.0","yuioty","PPI_WALLET");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"PPI_WALLET");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.as(StaticQrUpiPSPResponse.class);
        Assertions.assertThat(staticQrUpiPSPResponse.getBody().getResultCode())
                .as("Result code mismatch")
                .isEqualToIgnoringCase("FAIL");
        Assertions.assertThat(staticQrUpiPSPResponse.getBody().getResultMsg()).isEqualTo("Merchant Ineligible UPI_PPIWALLET");
        Assertions.assertThat(staticQrUpiPSPResponse.getBody().getSubResultCodeId()).isEqualTo("003");
        String theiaLogs = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia,staticQrUpiPSPRequest.getHeader().getRequestMsgId());
        Assertions.assertThat(theiaLogs).contains("resultMsg=Merchant Ineligible UPI_PPIWALLET");
        //Assertions.assertThat(theiaLogs).contains("offline ,big,Not enabled preference inside this where merchantType");

    }

    @Owner("Shubham Soni")
    @Feature("PGP-46237")
    @Test(description = "Verify failure Wallet Wallet Interpoability transaction when transaction amount more than 2000 for OFFLINE merchant Industry Type BIG, UPI_WALLET_BLACKLISTED :N & WALLET_ON_UPI_RAILS_ENABLED : N")
    public void failureUpiIntent_WI_TESTCASE17() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.WALINTER_OFFLINE_BIG1;
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(merchant.getId(), "2500.0","yuioty","PPI_WALLET");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"PPI_WALLET");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.as(StaticQrUpiPSPResponse.class);
        Assertions.assertThat(staticQrUpiPSPResponse.getBody().getResultCode())
                .as("Result code mismatch")
                .isEqualToIgnoringCase("FAIL");
        Assertions.assertThat(staticQrUpiPSPResponse.getBody().getResultMsg()).isEqualTo("Merchant Ineligible UPI_PPIWALLET");
        Assertions.assertThat(staticQrUpiPSPResponse.getBody().getSubResultCodeId()).isEqualTo("003");
        String theiaLogs = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia,staticQrUpiPSPRequest.getHeader().getRequestMsgId());
        Assertions.assertThat(theiaLogs).contains("resultMsg=Merchant Ineligible UPI_PPIWALLET");
       // Assertions.assertThat(theiaLogs).contains("offline ,big,Not enabled preference inside this where merchantType");

    }

    @Owner("Shubham Soni")
    @Feature("PGP-46237")
    @Test(description = "Verify failure Wallet Wallet Interpoability transaction when transaction amount equal to 2000 for OFFLINE merchant Industry Type BIG, UPI_WALLET_BLACKLISTED :N & WALLET_ON_UPI_RAILS_ENABLED : N")
    public void failureUpiIntent_WI_TESTCASE18() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.WALINTER_OFFLINE_BIG1;
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(merchant.getId(), "2000.0","yuioty","PPI_WALLET");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"PPI_WALLET");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.as(StaticQrUpiPSPResponse.class);
        Assertions.assertThat(staticQrUpiPSPResponse.getBody().getResultCode())
                .as("Result code mismatch")
                .isEqualToIgnoringCase("FAIL");
        Assertions.assertThat(staticQrUpiPSPResponse.getBody().getResultMsg()).isEqualTo("Merchant Ineligible UPI_PPIWALLET");
        Assertions.assertThat(staticQrUpiPSPResponse.getBody().getSubResultCodeId()).isEqualTo("003");
        String theiaLogs = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia,staticQrUpiPSPRequest.getHeader().getRequestMsgId());
        Assertions.assertThat(theiaLogs).contains("resultMsg=Merchant Ineligible UPI_PPIWALLET");
        //Assertions.assertThat(theiaLogs).contains("offline ,big,Not enabled preference inside this where merchantType");

    }




    @Owner("Shubham Soni")
    @Feature("PGP-46237")
    @Test(description = "DYNAIMC :Verify Success Wallet Wallet Interpoability transaction when transaction amount less than 2000 for Online merchant, UPI_WALLET_BLACKLISTED :N & WALLET_ON_UPI_RAILS_ENABLED : Y")
    public void successUpiIntent_WI_TESTCASE01_DYNAMIC() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.WALINTER_ONLINE_NULL;
        String OrderId = CommonHelpers.generateOrderId();

        PaymentService paymentService = new PaymentService(merchant,"200",OrderId);
        JsonPath jsonPath = paymentService.execute().jsonPath();

        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
        String qrCodeId = jsonPath.getString("body.qrCodeId");
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(merchant.getId(), "200.0",qrCodeId,"PPI_WALLET");
        builder.setOrderId(OrderId);
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"PPI_WALLET");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.as(StaticQrUpiPSPResponse.class);
        Assertions.assertThat(staticQrUpiPSPResponse.getBody().getResultCode())
                .as("Result code mismatch")
                .isEqualToIgnoringCase("SUCCESS");
        UPIIntentRequestDTO upiIntentRequestDTO = new UPIIntentRequestDTO();
        upiIntentRequestDTO.setAmount("200.0")
                .setInstaUrl(LocalConfig.PGP_HOST)
                .setOrderId(staticQrUpiPSPResponse.getBody().getOrderId())
                .setExternalSerialNo(staticQrUpiPSPResponse.getBody().getExternalSerialNo())
                .setTxnStatus(Constants.Intent_Callback.SUCCESS.getStatus())
                .setResponseCode(Constants.Intent_Callback.SUCCESS.getRespCode())
                .setResponseMessage(Constants.Intent_Callback.SUCCESS.getRespmsg())
                .setMid(merchant.getId());
        response = PGPHelpers.generateUpiIntentPayRequest(upiIntentRequestDTO);
        Assertions.assertThat(response.jsonPath().getString("body.resultCode"))
                .as("Result code mismatch")
                .isEqualToIgnoringCase("SUCCESS");
        PGPHelpers.getTxnStatus(merchant.getId(), staticQrUpiPSPResponse.getBody().getOrderId())
                .validateStatus("TXN_SUCCESS")
                .validatePaymentMode("UPI")
                .validateGatewayName("PPBL")
                .AssertAll();
        String theia_facade = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,staticQrUpiPSPResponse.getBody().getOrderId(),"ACQUIRING_PAY_ORDER");
        Assertions.assertThat(theia_facade).contains("feeRateFactorsInfo\":\"{\"solutionWiseMdr\":\"QR\",\"upiModeSubType\":\"UPI_PPIWALLET\"}");
        Assertions.assertThat(theia_facade).contains("\"upiCC\":false,\"upiModeSubType\":\"UPI_PPIWALLET\"");

    }

    @Owner("Shubham Soni")
    @Feature("PGP-46237")
    @Test(description = "DYNAMIC:Verify Success Wallet Wallet Interpoability transaction when transaction amount more than 2000 for Online merchant, UPI_WALLET_BLACKLISTED :N & WALLET_ON_UPI_RAILS_ENABLED : Y")
    public void successUpiIntent_WI_TESTCASE02_DYNAMIC() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.WALINTER_ONLINE_NULL;
        String OrderId = CommonHelpers.generateOrderId();

        PaymentService paymentService = new PaymentService(merchant,"2500",OrderId);
        JsonPath jsonPath = paymentService.execute().jsonPath();

        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
        String qrCodeId = jsonPath.getString("body.qrCodeId");
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(merchant.getId(), "2500.0",qrCodeId,"PPI_WALLET");
        builder.setOrderId(OrderId);
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"PPI_WALLET");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.as(StaticQrUpiPSPResponse.class);
        Assertions.assertThat(staticQrUpiPSPResponse.getBody().getResultCode())
                .as("Result code mismatch")
                .isEqualToIgnoringCase("SUCCESS");
        UPIIntentRequestDTO upiIntentRequestDTO = new UPIIntentRequestDTO();
        upiIntentRequestDTO.setAmount("2500.0")
                .setInstaUrl(LocalConfig.PGP_HOST)
                .setOrderId(staticQrUpiPSPResponse.getBody().getOrderId())
                .setExternalSerialNo(staticQrUpiPSPResponse.getBody().getExternalSerialNo())
                .setTxnStatus(Constants.Intent_Callback.SUCCESS.getStatus())
                .setResponseCode(Constants.Intent_Callback.SUCCESS.getRespCode())
                .setResponseMessage(Constants.Intent_Callback.SUCCESS.getRespmsg())
                .setMid(merchant.getId());
        response = PGPHelpers.generateUpiIntentPayRequest(upiIntentRequestDTO);
        Assertions.assertThat(response.jsonPath().getString("body.resultCode"))
                .as("Result code mismatch")
                .isEqualToIgnoringCase("SUCCESS");
        PGPHelpers.getTxnStatus(merchant.getId(), staticQrUpiPSPResponse.getBody().getOrderId())
                .validateStatus("TXN_SUCCESS")
                .validatePaymentMode("UPI")
                .validateGatewayName("PPBL")
                .AssertAll();
        String theia_facade = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,staticQrUpiPSPResponse.getBody().getOrderId(),"ACQUIRING_PAY_ORDER");
        Assertions.assertThat(theia_facade).contains("feeRateFactorsInfo\":\"{\"solutionWiseMdr\":\"QR\",\"upiModeSubType\":\"UPI_PPIWALLET\"}");
        Assertions.assertThat(theia_facade).contains("\"upiCC\":false,\"upiModeSubType\":\"UPI_PPIWALLET\"");

    }

    @Owner("Shubham Soni")
    @Feature("PGP-46237")
    @Test(description = "DYNAMIC :Verify Success Wallet Wallet Interpoability transaction when transaction amount equal to 2000 for Online merchant, UPI_WALLET_BLACKLISTED :N & WALLET_ON_UPI_RAILS_ENABLED : Y")
    public void successUpiIntent_WI_TESTCASE03_DYNAMIC() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.WALINTER_ONLINE_NULL;
        String OrderId = CommonHelpers.generateOrderId();

        PaymentService paymentService = new PaymentService(merchant,"2000",OrderId);
        JsonPath jsonPath = paymentService.execute().jsonPath();

        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
        String qrCodeId = jsonPath.getString("body.qrCodeId");
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(merchant.getId(), "2000.0",qrCodeId,"PPI_WALLET");
        builder.setOrderId(OrderId);
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"PPI_WALLET");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.as(StaticQrUpiPSPResponse.class);
        Assertions.assertThat(staticQrUpiPSPResponse.getBody().getResultCode())
                .as("Result code mismatch")
                .isEqualToIgnoringCase("SUCCESS");
        UPIIntentRequestDTO upiIntentRequestDTO = new UPIIntentRequestDTO();
        upiIntentRequestDTO.setAmount("2000.0")
                .setInstaUrl(LocalConfig.PGP_HOST)
                .setOrderId(staticQrUpiPSPResponse.getBody().getOrderId())
                .setExternalSerialNo(staticQrUpiPSPResponse.getBody().getExternalSerialNo())
                .setTxnStatus(Constants.Intent_Callback.SUCCESS.getStatus())
                .setResponseCode(Constants.Intent_Callback.SUCCESS.getRespCode())
                .setResponseMessage(Constants.Intent_Callback.SUCCESS.getRespmsg())
                .setMid(merchant.getId());
        response = PGPHelpers.generateUpiIntentPayRequest(upiIntentRequestDTO);
        Assertions.assertThat(response.jsonPath().getString("body.resultCode"))
                .as("Result code mismatch")
                .isEqualToIgnoringCase("SUCCESS");
        PGPHelpers.getTxnStatus(merchant.getId(), staticQrUpiPSPResponse.getBody().getOrderId())
                .validateStatus("TXN_SUCCESS")
                .validatePaymentMode("UPI")
                .validateGatewayName("PPBL")
                .AssertAll();
        String theia_facade = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,staticQrUpiPSPResponse.getBody().getOrderId(),"ACQUIRING_PAY_ORDER");
        Assertions.assertThat(theia_facade).contains("\"feeRateFactorsInfo\":\"{\"solutionWiseMdr\":\"QR\",\"upiModeSubType\":\"UPI_PPIWALLET\"}\"");
        Assertions.assertThat(theia_facade).contains("\"upiCC\":false,\"upiModeSubType\":\"UPI_PPIWALLET\"");

    }
    @Owner("Shubham Soni")
    @Feature("PGP-46237")
    @Test(description = "DYNAMIC:Verify failure Wallet Wallet Interpoability transaction when transaction amount less than 2000 for Online merchant, UPI_WALLET_BLACKLISTED :Y & WALLET_ON_UPI_RAILS_ENABLED : Y")
    public void failureUpiIntent_WI_TESTCASE04_DYNAMIC() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.WALINTER_ONLINE_SMALL;
        String OrderId = CommonHelpers.generateOrderId();

        PaymentService paymentService = new PaymentService(merchant,"200",OrderId);
        JsonPath jsonPath = paymentService.execute().jsonPath();

        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
        String qrCodeId = jsonPath.getString("body.qrCodeId");
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(merchant.getId(), "200.0",qrCodeId,"PPI_WALLET");
        builder.setOrderId(OrderId);
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"PPI_WALLET");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.as(StaticQrUpiPSPResponse.class);
        Assertions.assertThat(staticQrUpiPSPResponse.getBody().getResultCode())
                .as("Result code mismatch")
                .isEqualToIgnoringCase("FAIL");
        Assertions.assertThat(staticQrUpiPSPResponse.getBody().getSubResultCodeId().equals("003"));
        String theiaLogs = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia,staticQrUpiPSPRequest.getHeader().getRequestMsgId());
        Assertions.assertThat(theiaLogs).contains("UPIPSPResponse [resultCode=FAIL, resultCodeId=009, resultMsg=Merchant Ineligible UPI_PPIWALLET");
        Assertions.assertThat(theiaLogs).contains("Wallet On UPI is not enabled");
        Assertions.assertThat(theiaLogs).contains("preferenceName=UPI_WALLET_BLACKLISTED, enabled=true, preferenceValue=Y");
    }

    @Owner("Shubham Soni")
    @Feature("PGP-46237")
    @Test(description = "DYNAMIC :Verify failure Wallet Wallet Interpoability transaction when transaction amount more than 2000 for Online merchant, UPI_WALLET_BLACKLISTED :Y & WALLET_ON_UPI_RAILS_ENABLED : Y")
    public void failureUpiIntent_WI_TESTCASE05_DYNAMIC() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.WALINTER_ONLINE_SMALL;
        String OrderId = CommonHelpers.generateOrderId();

        PaymentService paymentService = new PaymentService(merchant,"2500",OrderId);
        JsonPath jsonPath = paymentService.execute().jsonPath();

        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
        String qrCodeId = jsonPath.getString("body.qrCodeId");
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(merchant.getId(), "2500.0",qrCodeId,"PPI_WALLET");
        builder.setOrderId(OrderId);
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"PPI_WALLET");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.as(StaticQrUpiPSPResponse.class);
        Assertions.assertThat(staticQrUpiPSPResponse.getBody().getResultCode())
                .as("Result code mismatch")
                .isEqualToIgnoringCase("FAIL");
        Assertions.assertThat(staticQrUpiPSPResponse.getBody().getSubResultCodeId().equals("003"));
        String theiaLogs = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia,staticQrUpiPSPRequest.getHeader().getRequestMsgId());
        Assertions.assertThat(theiaLogs).contains("UPIPSPResponse [resultCode=FAIL, resultCodeId=009, resultMsg=Merchant Ineligible UPI_PPIWALLET");
        Assertions.assertThat(theiaLogs).contains("Wallet On UPI is not enabled");
        Assertions.assertThat(theiaLogs).contains("preferenceName=UPI_WALLET_BLACKLISTED, enabled=true, preferenceValue=Y");

    }

    @Owner("Shubham Soni")
    @Feature("PGP-46237")
    @Test(description = "DYNAMIC :Verify failure Wallet Wallet Interpoability transaction when transaction amount equal to 2000 for Online merchant, UPI_WALLET_BLACKLISTED :Y & WALLET_ON_UPI_RAILS_ENABLED : Y")
    public void failureUpiIntent_WI_TESTCASE06_DYNAMIC() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.WALINTER_ONLINE_SMALL;
        String OrderId = CommonHelpers.generateOrderId();

        PaymentService paymentService = new PaymentService(merchant,"2000",OrderId);
        JsonPath jsonPath = paymentService.execute().jsonPath();

        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
        String qrCodeId = jsonPath.getString("body.qrCodeId");
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(merchant.getId(), "2000.0",qrCodeId,"PPI_WALLET");
        builder.setOrderId(OrderId);
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"PPI_WALLET");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.as(StaticQrUpiPSPResponse.class);
        Assertions.assertThat(staticQrUpiPSPResponse.getBody().getResultCode())
                .as("Result code mismatch")
                .isEqualToIgnoringCase("FAIL");
        Assertions.assertThat(staticQrUpiPSPResponse.getBody().getSubResultCodeId().equals("003"));
        String theiaLogs = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia,staticQrUpiPSPRequest.getHeader().getRequestMsgId());
        Assertions.assertThat(theiaLogs).contains("UPIPSPResponse [resultCode=FAIL, resultCodeId=009, resultMsg=Merchant Ineligible UPI_PPIWALLET");
        Assertions.assertThat(theiaLogs).contains("Wallet On UPI is not enabled");
        Assertions.assertThat(theiaLogs).contains("preferenceName=UPI_WALLET_BLACKLISTED, enabled=true, preferenceValue=Y");

    }
    @Owner("Shubham Soni")
    @Feature("PGP-46237")
    @Test(description = "DYNAMIC :Verify Success Wallet Wallet Interpoability transaction when transaction amount less than 2000 for OFFLINE merchant Industry Type Small, UPI_WALLET_BLACKLISTED :N & WALLET_ON_UPI_RAILS_ENABLED : Y")
    public void successUpiIntent_WI_TESTCASE07_DYNAMIC() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.WALINTER_OFFLINE_SMALL;
        String OrderId = CommonHelpers.generateOrderId();

        PaymentService paymentService = new PaymentService(merchant,"2",OrderId);
        JsonPath jsonPath = paymentService.execute().jsonPath();

        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
        String qrCodeId = jsonPath.getString("body.qrCodeId");
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(merchant.getId(), "200.0",qrCodeId,"PPI_WALLET");
        builder.setOrderId(OrderId);
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"PPI_WALLET");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.as(StaticQrUpiPSPResponse.class);
        Assertions.assertThat(staticQrUpiPSPResponse.getBody().getResultCode())
                .as("Result code mismatch")
                .isEqualToIgnoringCase("SUCCESS");
        UPIIntentRequestDTO upiIntentRequestDTO = new UPIIntentRequestDTO();
        upiIntentRequestDTO.setAmount("2.0")
                .setInstaUrl(LocalConfig.PGP_HOST)
                .setOrderId(staticQrUpiPSPResponse.getBody().getOrderId())
                .setExternalSerialNo(staticQrUpiPSPResponse.getBody().getExternalSerialNo())
                .setTxnStatus(Constants.Intent_Callback.SUCCESS.getStatus())
                .setResponseCode(Constants.Intent_Callback.SUCCESS.getRespCode())
                .setResponseMessage(Constants.Intent_Callback.SUCCESS.getRespmsg())
                .setMid(merchant.getId());
        response = PGPHelpers.generateUpiIntentPayRequest(upiIntentRequestDTO);
        Assertions.assertThat(response.jsonPath().getString("body.resultCode"))
                .as("Result code mismatch")
                .isEqualToIgnoringCase("SUCCESS");
        PGPHelpers.getTxnStatus(merchant.getId(), staticQrUpiPSPResponse.getBody().getOrderId())
                .validateStatus("TXN_SUCCESS")
                .validatePaymentMode("UPI")
                .validateGatewayName("PPBL")
                .AssertAll();
        String theia_facade = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,staticQrUpiPSPResponse.getBody().getOrderId(),"ACQUIRING_PAY_ORDER");
        Assertions.assertThat(theia_facade).contains("feeRateFactorsInfo\":\"{\"solutionWiseMdr\":\"QR\",\"upiModeSubType\":\"UPI_PPIWALLET\"}");
        Assertions.assertThat(theia_facade).contains("\"isDeepLinkFlow\":false,\"upiCC\":false,\"upiModeSubType\":\"UPI_PPIWALLET\"");

    }

    @Owner("Shubham Soni")
    @Feature("PGP-46237")
    @Test(description = "DYNAMIC :Verify Success Wallet Wallet Interpoability transaction when transaction amount more than 2000 for OFFLINE merchant Industry Type Small, UPI_WALLET_BLACKLISTED :N & WALLET_ON_UPI_RAILS_ENABLED : Y")
    public void successUpiIntent_WI_TESTCASE08_DYNAMIC() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.WALINTER_OFFLINE_SMALL;
        String OrderId = CommonHelpers.generateOrderId();

        PaymentService paymentService = new PaymentService(merchant,"2500",OrderId);
        JsonPath jsonPath = paymentService.execute().jsonPath();

        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
        String qrCodeId = jsonPath.getString("body.qrCodeId");
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(merchant.getId(), "2500.0",qrCodeId,"PPI_WALLET");
        builder.setOrderId(OrderId);
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"PPI_WALLET");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.as(StaticQrUpiPSPResponse.class);
        Assertions.assertThat(staticQrUpiPSPResponse.getBody().getResultCode())
                .as("Result code mismatch")
                .isEqualToIgnoringCase("SUCCESS");
        UPIIntentRequestDTO upiIntentRequestDTO = new UPIIntentRequestDTO();
        upiIntentRequestDTO.setAmount("2500.0")
                .setInstaUrl(LocalConfig.PGP_HOST)
                .setOrderId(staticQrUpiPSPResponse.getBody().getOrderId())
                .setExternalSerialNo(staticQrUpiPSPResponse.getBody().getExternalSerialNo())
                .setTxnStatus(Constants.Intent_Callback.SUCCESS.getStatus())
                .setResponseCode(Constants.Intent_Callback.SUCCESS.getRespCode())
                .setResponseMessage(Constants.Intent_Callback.SUCCESS.getRespmsg())
                .setMid(merchant.getId());
        response = PGPHelpers.generateUpiIntentPayRequest(upiIntentRequestDTO);
        Assertions.assertThat(response.jsonPath().getString("body.resultCode"))
                .as("Result code mismatch")
                .isEqualToIgnoringCase("SUCCESS");
        PGPHelpers.getTxnStatus(merchant.getId(), staticQrUpiPSPResponse.getBody().getOrderId())
                .validateStatus("TXN_SUCCESS")
                .validatePaymentMode("UPI")
                .validateGatewayName("PPBL")
                .AssertAll();
        String theia_facade = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,staticQrUpiPSPResponse.getBody().getOrderId(),"ACQUIRING_PAY_ORDER");
        Assertions.assertThat(theia_facade).contains("feeRateFactorsInfo\":\"{\"solutionWiseMdr\":\"QR\",\"upiModeSubType\":\"UPI_PPIWALLET\"}");
        Assertions.assertThat(theia_facade).contains("\"isDeepLinkFlow\":false,\"upiCC\":false,\"upiModeSubType\":\"UPI_PPIWALLET\"");

    }

    @Owner("Shubham Soni")
    @Feature("PGP-46237")
    @Test(description = "DYNAMIC :Verify Success Wallet Wallet Interpoability transaction when transaction amount equal to 2000 for OFFLINE merchant Industry Type Small, UPI_WALLET_BLACKLISTED :N & WALLET_ON_UPI_RAILS_ENABLED : Y")
    public void successUpiIntent_WI_TESTCASE09_DYNAMIC() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.WALINTER_OFFLINE_SMALL;
        String OrderId = CommonHelpers.generateOrderId();

        PaymentService paymentService = new PaymentService(merchant,"2000",OrderId);
        JsonPath jsonPath = paymentService.execute().jsonPath();

        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
        String qrCodeId = jsonPath.getString("body.qrCodeId");
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(merchant.getId(), "2000.0",qrCodeId,"PPI_WALLET");
        builder.setOrderId(OrderId);
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"PPI_WALLET");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.as(StaticQrUpiPSPResponse.class);
        Assertions.assertThat(staticQrUpiPSPResponse.getBody().getResultCode())
                .as("Result code mismatch")
                .isEqualToIgnoringCase("SUCCESS");
        UPIIntentRequestDTO upiIntentRequestDTO = new UPIIntentRequestDTO();
        upiIntentRequestDTO.setAmount("2000.0")
                .setInstaUrl(LocalConfig.PGP_HOST)
                .setOrderId(staticQrUpiPSPResponse.getBody().getOrderId())
                .setExternalSerialNo(staticQrUpiPSPResponse.getBody().getExternalSerialNo())
                .setTxnStatus(Constants.Intent_Callback.SUCCESS.getStatus())
                .setResponseCode(Constants.Intent_Callback.SUCCESS.getRespCode())
                .setResponseMessage(Constants.Intent_Callback.SUCCESS.getRespmsg())
                .setMid(merchant.getId());
        response = PGPHelpers.generateUpiIntentPayRequest(upiIntentRequestDTO);
        Assertions.assertThat(response.jsonPath().getString("body.resultCode"))
                .as("Result code mismatch")
                .isEqualToIgnoringCase("SUCCESS");
        PGPHelpers.getTxnStatus(merchant.getId(), staticQrUpiPSPResponse.getBody().getOrderId())
                .validateStatus("TXN_SUCCESS")
                .validatePaymentMode("UPI")
                .validateGatewayName("PPBL")
                .AssertAll();
        String theia_facade = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,staticQrUpiPSPResponse.getBody().getOrderId(),"ACQUIRING_PAY_ORDER");
        Assertions.assertThat(theia_facade).contains("feeRateFactorsInfo\":\"{\"solutionWiseMdr\":\"QR\",\"upiModeSubType\":\"UPI_PPIWALLET\"}");
        Assertions.assertThat(theia_facade).contains("\"isDeepLinkFlow\":false,\"upiCC\":false,\"upiModeSubType\":\"UPI_PPIWALLET\"");

    }

    @Owner("Shubham Soni")
    @Feature("PGP-46237")
    @Test(description = "DYNAMIC :Verify Success Wallet Wallet Interpoability transaction when transaction amount less than 2000 for OFFLINE merchant Industry Type NULL(Same as Small), UPI_WALLET_BLACKLISTED :N & WALLET_ON_UPI_RAILS_ENABLED : N")
    public void successUpiIntent_WI_TESTCASE10_DYNAMIC() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.WALINTER_OFFLINE_NULL;
        String OrderId = CommonHelpers.generateOrderId();

        PaymentService paymentService = new PaymentService(merchant,"200",OrderId);
        JsonPath jsonPath = paymentService.execute().jsonPath();

        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
        String qrCodeId = jsonPath.getString("body.qrCodeId");
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(merchant.getId(), "200.0",qrCodeId,"PPI_WALLET");
        builder.setOrderId(OrderId);
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"PPI_WALLET");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.as(StaticQrUpiPSPResponse.class);
        Assertions.assertThat(staticQrUpiPSPResponse.getBody().getResultCode())
                .as("Result code mismatch")
                .isEqualToIgnoringCase("SUCCESS");
        UPIIntentRequestDTO upiIntentRequestDTO = new UPIIntentRequestDTO();
        upiIntentRequestDTO.setAmount("200.0")
                .setInstaUrl(LocalConfig.PGP_HOST)
                .setOrderId(staticQrUpiPSPResponse.getBody().getOrderId())
                .setExternalSerialNo(staticQrUpiPSPResponse.getBody().getExternalSerialNo())
                .setTxnStatus(Constants.Intent_Callback.SUCCESS.getStatus())
                .setResponseCode(Constants.Intent_Callback.SUCCESS.getRespCode())
                .setResponseMessage(Constants.Intent_Callback.SUCCESS.getRespmsg())
                .setMid(merchant.getId());
        response = PGPHelpers.generateUpiIntentPayRequest(upiIntentRequestDTO);
        Assertions.assertThat(response.jsonPath().getString("body.resultCode"))
                .as("Result code mismatch")
                .isEqualToIgnoringCase("SUCCESS");
        PGPHelpers.getTxnStatus(merchant.getId(), staticQrUpiPSPResponse.getBody().getOrderId())
                .validateStatus("TXN_SUCCESS")
                .validatePaymentMode("UPI")
                .validateGatewayName("PPBL")
                .AssertAll();
        String theia_facade = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,staticQrUpiPSPResponse.getBody().getOrderId(),"ACQUIRING_PAY_ORDER");
        Assertions.assertThat(theia_facade).contains("feeRateFactorsInfo\":\"{\"solutionWiseMdr\":\"QR\",\"upiModeSubType\":\"UPI_PPIWALLET\"}");
        Assertions.assertThat(theia_facade).contains("\"upiCC\":false,\"upiModeSubType\":\"UPI_PPIWALLET\"");

    }

    @Owner("Shubham Soni")
    @Feature("PGP-46237")
    @Test(description = "DYNAMIC :Verify Failure Wallet Wallet Interpoability transaction when transaction amount more than 2000 for OFFLINE merchant Industry Type NULL(Same as Small), UPI_WALLET_BLACKLISTED :N & WALLET_ON_UPI_RAILS_ENABLED : N")
    public void failureUpiIntent_WI_TESTCASE11_DYNAMIC() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.WALINTER_OFFLINE_NULL;
        String OrderId = CommonHelpers.generateOrderId();

        PaymentService paymentService = new PaymentService(merchant,"2500",OrderId);
        JsonPath jsonPath = paymentService.execute().jsonPath();

        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
        String qrCodeId = jsonPath.getString("body.qrCodeId");
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(merchant.getId(), "2500.0",qrCodeId,"PPI_WALLET");
        builder.setOrderId(OrderId);
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"PPI_WALLET");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.as(StaticQrUpiPSPResponse.class);
        Assertions.assertThat(staticQrUpiPSPResponse.getBody().getResultCode())
                .as("Result code mismatch")
                .isEqualToIgnoringCase("FAIL");
        String theiaLogs = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia,staticQrUpiPSPRequest.getHeader().getRequestMsgId());
        Assertions.assertThat(theiaLogs).contains("UPIPSPResponse [resultCode=FAIL, resultCodeId=009, resultMsg=Payment failed. This merchant can accept Wallet on UPI upto Rs. 2000.");
        Assertions.assertThat(theiaLogs).contains("offline ,small,Not enabled preference inside this where merchantType");
        Assertions.assertThat(theiaLogs).contains("Payment failed. This merchant can accept Wallet on UPI upto Rs. 2000.");

    }

    @Owner("Shubham Soni")
    @Feature("PGP-46237")
    @Test(description = "DYNAMIC :Verify Success Wallet Wallet Interpoability transaction when transaction amount equal to 2000 for OFFLINE merchant Industry Type NULL(Same as Small), UPI_WALLET_BLACKLISTED :N & WALLET_ON_UPI_RAILS_ENABLED : N")
    public void successUpiIntent_WI_TESTCASE12_DYNAMIC() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.WALINTER_OFFLINE_NULL;
        String OrderId = CommonHelpers.generateOrderId();

        PaymentService paymentService = new PaymentService(merchant,"2000",OrderId);
        JsonPath jsonPath = paymentService.execute().jsonPath();

        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
        String qrCodeId = jsonPath.getString("body.qrCodeId");
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(merchant.getId(), "2000.0",qrCodeId,"PPI_WALLET");
        builder.setOrderId(OrderId);
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"PPI_WALLET");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.as(StaticQrUpiPSPResponse.class);
        Assertions.assertThat(staticQrUpiPSPResponse.getBody().getResultCode())
                .as("Result code mismatch")
                .isEqualToIgnoringCase("SUCCESS");
        UPIIntentRequestDTO upiIntentRequestDTO = new UPIIntentRequestDTO();
        upiIntentRequestDTO.setAmount("2000.0")
                .setInstaUrl(LocalConfig.PGP_HOST)
                .setOrderId(staticQrUpiPSPResponse.getBody().getOrderId())
                .setExternalSerialNo(staticQrUpiPSPResponse.getBody().getExternalSerialNo())
                .setTxnStatus(Constants.Intent_Callback.SUCCESS.getStatus())
                .setResponseCode(Constants.Intent_Callback.SUCCESS.getRespCode())
                .setResponseMessage(Constants.Intent_Callback.SUCCESS.getRespmsg())
                .setMid(merchant.getId());
        response = PGPHelpers.generateUpiIntentPayRequest(upiIntentRequestDTO);
        Assertions.assertThat(response.jsonPath().getString("body.resultCode"))
                .as("Result code mismatch")
                .isEqualToIgnoringCase("SUCCESS");
        PGPHelpers.getTxnStatus(merchant.getId(), staticQrUpiPSPResponse.getBody().getOrderId())
                .validateStatus("TXN_SUCCESS")
                .validatePaymentMode("UPI")
                .validateGatewayName("PPBL")
                .AssertAll();
        String theia_facade = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,staticQrUpiPSPResponse.getBody().getOrderId(),"ACQUIRING_PAY_ORDER");
        Assertions.assertThat(theia_facade).contains("feeRateFactorsInfo\":\"{\"solutionWiseMdr\":\"QR\",\"upiModeSubType\":\"UPI_PPIWALLET\"}");
        Assertions.assertThat(theia_facade).contains("\"upiCC\":false,\"upiModeSubType\":\"UPI_PPIWALLET\"");

    }
    @Owner("Shubham Soni")
    @Feature("PGP-46237")
    @Test(description = "DYNAMIC :Verify Success Wallet Wallet Interpoability transaction when transaction amount less than 2000 for OFFLINE merchant Industry Type BIG, UPI_WALLET_BLACKLISTED :N & WALLET_ON_UPI_RAILS_ENABLED : Y")
    public void successUpiIntent_WI_TESTCASE13_DYNAMIC() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.WALINTER_OFFLINE_BIG;
        String OrderId = CommonHelpers.generateOrderId();

        PaymentService paymentService = new PaymentService(merchant,"200",OrderId);
        JsonPath jsonPath = paymentService.execute().jsonPath();

        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
        String qrCodeId = jsonPath.getString("body.qrCodeId");
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(merchant.getId(), "200.0",qrCodeId,"PPI_WALLET");
        builder.setOrderId(OrderId);
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"PPI_WALLET");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.as(StaticQrUpiPSPResponse.class);
        Assertions.assertThat(staticQrUpiPSPResponse.getBody().getResultCode())
                .as("Result code mismatch")
                .isEqualToIgnoringCase("SUCCESS");
        UPIIntentRequestDTO upiIntentRequestDTO = new UPIIntentRequestDTO();
        upiIntentRequestDTO.setAmount("200.0")
                .setInstaUrl(LocalConfig.PGP_HOST)
                .setOrderId(staticQrUpiPSPResponse.getBody().getOrderId())
                .setExternalSerialNo(staticQrUpiPSPResponse.getBody().getExternalSerialNo())
                .setTxnStatus(Constants.Intent_Callback.SUCCESS.getStatus())
                .setResponseCode(Constants.Intent_Callback.SUCCESS.getRespCode())
                .setResponseMessage(Constants.Intent_Callback.SUCCESS.getRespmsg())
                .setMid(merchant.getId());
        response = PGPHelpers.generateUpiIntentPayRequest(upiIntentRequestDTO);
        Assertions.assertThat(response.jsonPath().getString("body.resultCode"))
                .as("Result code mismatch")
                .isEqualToIgnoringCase("SUCCESS");
        PGPHelpers.getTxnStatus(merchant.getId(), staticQrUpiPSPResponse.getBody().getOrderId())
                .validateStatus("TXN_SUCCESS")
                .validatePaymentMode("UPI")
                .validateGatewayName("PPBL")
                .AssertAll();
        String theia_facade = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,staticQrUpiPSPResponse.getBody().getOrderId(),"ACQUIRING_PAY_ORDER");
        Assertions.assertThat(theia_facade).contains("feeRateFactorsInfo\":\"{\"solutionWiseMdr\":\"QR\",\"upiModeSubType\":\"UPI_PPIWALLET\"}");
        Assertions.assertThat(theia_facade).contains("\"upiCC\":false,\"upiModeSubType\":\"UPI_PPIWALLET\"");

    }

    @Owner("Shubham Soni")
    @Feature("PGP-46237")
    @Test(description = "DYNAMIC :Verify Success Wallet Wallet Interpoability transaction when transaction amount more than 2000 for OFFLINE merchant Industry Type BIG, UPI_WALLET_BLACKLISTED :N & WALLET_ON_UPI_RAILS_ENABLED : Y")
    public void successUpiIntent_WI_TESTCASE14_DYNAMIC() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.WALINTER_OFFLINE_BIG;
        String OrderId = CommonHelpers.generateOrderId();

        PaymentService paymentService = new PaymentService(merchant,"2500",OrderId);
        JsonPath jsonPath = paymentService.execute().jsonPath();

        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
        String qrCodeId = jsonPath.getString("body.qrCodeId");
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(merchant.getId(), "2500.0",qrCodeId,"PPI_WALLET");
        builder.setOrderId(OrderId);
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"PPI_WALLET");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.as(StaticQrUpiPSPResponse.class);
        Assertions.assertThat(staticQrUpiPSPResponse.getBody().getResultCode())
                .as("Result code mismatch")
                .isEqualToIgnoringCase("SUCCESS");
        UPIIntentRequestDTO upiIntentRequestDTO = new UPIIntentRequestDTO();
        upiIntentRequestDTO.setAmount("2500.0")
                .setInstaUrl(LocalConfig.PGP_HOST)
                .setOrderId(staticQrUpiPSPResponse.getBody().getOrderId())
                .setExternalSerialNo(staticQrUpiPSPResponse.getBody().getExternalSerialNo())
                .setTxnStatus(Constants.Intent_Callback.SUCCESS.getStatus())
                .setResponseCode(Constants.Intent_Callback.SUCCESS.getRespCode())
                .setResponseMessage(Constants.Intent_Callback.SUCCESS.getRespmsg())
                .setMid(merchant.getId());
        response = PGPHelpers.generateUpiIntentPayRequest(upiIntentRequestDTO);
        Assertions.assertThat(response.jsonPath().getString("body.resultCode"))
                .as("Result code mismatch")
                .isEqualToIgnoringCase("SUCCESS");
        PGPHelpers.getTxnStatus(merchant.getId(), staticQrUpiPSPResponse.getBody().getOrderId())
                .validateStatus("TXN_SUCCESS")
                .validatePaymentMode("UPI")
                .validateGatewayName("PPBL")
                .AssertAll();
        String theia_facade = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,staticQrUpiPSPResponse.getBody().getOrderId(),"ACQUIRING_PAY_ORDER");
        Assertions.assertThat(theia_facade).contains("feeRateFactorsInfo\":\"{\"solutionWiseMdr\":\"QR\",\"upiModeSubType\":\"UPI_PPIWALLET\"}");
        Assertions.assertThat(theia_facade).contains("\"upiCC\":false,\"upiModeSubType\":\"UPI_PPIWALLET\"");

    }

    @Owner("Shubham Soni")
    @Feature("PGP-46237")
    @Test(description = "DYNMAIC :Verify Success Wallet Wallet Interpoability transaction when transaction amount equal to 2000 for OFFLINE merchant Industry Type BIG, UPI_WALLET_BLACKLISTED :N & WALLET_ON_UPI_RAILS_ENABLED : Y")
    public void successUpiIntent_WI_TESTCASE15_DYNAMIC() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.WALINTER_OFFLINE_BIG;
        String OrderId = CommonHelpers.generateOrderId();

        PaymentService paymentService = new PaymentService(merchant,"2000",OrderId);
        JsonPath jsonPath = paymentService.execute().jsonPath();

        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
        String qrCodeId = jsonPath.getString("body.qrCodeId");
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(merchant.getId(), "2000.0",qrCodeId,"PPI_WALLET");
        builder.setOrderId(OrderId);
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"PPI_WALLET");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.as(StaticQrUpiPSPResponse.class);
        Assertions.assertThat(staticQrUpiPSPResponse.getBody().getResultCode())
                .as("Result code mismatch")
                .isEqualToIgnoringCase("SUCCESS");
        UPIIntentRequestDTO upiIntentRequestDTO = new UPIIntentRequestDTO();
        upiIntentRequestDTO.setAmount("2000.0")
                .setInstaUrl(LocalConfig.PGP_HOST)
                .setOrderId(staticQrUpiPSPResponse.getBody().getOrderId())
                .setExternalSerialNo(staticQrUpiPSPResponse.getBody().getExternalSerialNo())
                .setTxnStatus(Constants.Intent_Callback.SUCCESS.getStatus())
                .setResponseCode(Constants.Intent_Callback.SUCCESS.getRespCode())
                .setResponseMessage(Constants.Intent_Callback.SUCCESS.getRespmsg())
                .setMid(merchant.getId());
        response = PGPHelpers.generateUpiIntentPayRequest(upiIntentRequestDTO);
        Assertions.assertThat(response.jsonPath().getString("body.resultCode"))
                .as("Result code mismatch")
                .isEqualToIgnoringCase("SUCCESS");
        PGPHelpers.getTxnStatus(merchant.getId(), staticQrUpiPSPResponse.getBody().getOrderId())
                .validateStatus("TXN_SUCCESS")
                .validatePaymentMode("UPI")
                .validateGatewayName("PPBL")
                .AssertAll();
        String theia_facade = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,staticQrUpiPSPResponse.getBody().getOrderId(),"ACQUIRING_PAY_ORDER");

        Assertions.assertThat(theia_facade).contains("feeRateFactorsInfo\":\"{\"solutionWiseMdr\":\"QR\",\"upiModeSubType\":\"UPI_PPIWALLET\"}");
        Assertions.assertThat(theia_facade).contains("\"upiCC\":false,\"upiModeSubType\":\"UPI_PPIWALLET\"");

    }

    @Owner("Shubham Soni")
    @Feature("PGP-46237")
    @Test(description = "DYNAMIC :Verify failure Wallet Wallet Interpoability transaction when transaction amount less than 2000 for OFFLINE merchant Industry Type BIG, UPI_WALLET_BLACKLISTED :N & WALLET_ON_UPI_RAILS_ENABLED : N")
    public void failureUpiIntent_WI_TESTCASE16_DYNAMIC() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.WALINTER_OFFLINE_BIG1;
        String OrderId = CommonHelpers.generateOrderId();

        PaymentService paymentService = new PaymentService(merchant,"200",OrderId);
        JsonPath jsonPath = paymentService.execute().jsonPath();

        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
        String qrCodeId = jsonPath.getString("body.qrCodeId");
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(merchant.getId(), "200.0",qrCodeId,"PPI_WALLET");
        builder.setOrderId(OrderId);
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"PPI_WALLET");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.as(StaticQrUpiPSPResponse.class);
        Assertions.assertThat(staticQrUpiPSPResponse.getBody().getResultCode())
                .as("Result code mismatch")
                .isEqualToIgnoringCase("FAIL");
        Assertions.assertThat(staticQrUpiPSPResponse.getBody().getResultMsg()).isEqualTo("Merchant Ineligible UPI_PPIWALLET");
        Assertions.assertThat(staticQrUpiPSPResponse.getBody().getSubResultCodeId()).isEqualTo("003");
        String theiaLogs = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia,staticQrUpiPSPRequest.getHeader().getRequestMsgId());
        Assertions.assertThat(theiaLogs).contains("resultMsg=Merchant Ineligible UPI_PPIWALLET");
        //Assertions.assertThat(theiaLogs).contains("offline ,big,Not enabled preference inside this where merchantType");

    }

    @Owner("Shubham Soni")
    @Feature("PGP-46237")
    @Test(description = "DYNAMIC :Verify failure Wallet Wallet Interpoability transaction when transaction amount more than 2000 for OFFLINE merchant Industry Type BIG, UPI_WALLET_BLACKLISTED :N & WALLET_ON_UPI_RAILS_ENABLED : N")
    public void failureUpiIntent_WI_TESTCASE17_DYNAMIC() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.WALINTER_OFFLINE_BIG1;
        String OrderId = CommonHelpers.generateOrderId();

        PaymentService paymentService = new PaymentService(merchant,"2500",OrderId);
        JsonPath jsonPath = paymentService.execute().jsonPath();

        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
        String qrCodeId = jsonPath.getString("body.qrCodeId");
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(merchant.getId(), "2500.0",qrCodeId,"PPI_WALLET");
        builder.setOrderId(OrderId);
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"PPI_WALLET");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.as(StaticQrUpiPSPResponse.class);
        Assertions.assertThat(staticQrUpiPSPResponse.getBody().getResultCode())
                .as("Result code mismatch")
                .isEqualToIgnoringCase("FAIL");
        Assertions.assertThat(staticQrUpiPSPResponse.getBody().getResultMsg()).isEqualTo("Merchant Ineligible UPI_PPIWALLET");
        Assertions.assertThat(staticQrUpiPSPResponse.getBody().getSubResultCodeId()).isEqualTo("003");
        String theiaLogs = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia,staticQrUpiPSPRequest.getHeader().getRequestMsgId());
        Assertions.assertThat(theiaLogs).contains("resultMsg=Merchant Ineligible UPI_PPIWALLET");
        //Assertions.assertThat(theiaLogs).contains("offline ,big,Not enabled preference inside this where merchantType");

    }

    @Owner("Shubham Soni")
    @Feature("PGP-46237")
    @Test(description = "DYNAMIC :Verify failure Wallet Wallet Interpoability transaction when transaction amount equal to 2000 for OFFLINE merchant Industry Type BIG, UPI_WALLET_BLACKLISTED :N & WALLET_ON_UPI_RAILS_ENABLED : N")
    public void failureUpiIntent_WI_TESTCASE18_DYNAMIC() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.WALINTER_OFFLINE_BIG1;
        String OrderId = CommonHelpers.generateOrderId();

        PaymentService paymentService = new PaymentService(merchant,"2000",OrderId);
        JsonPath jsonPath = paymentService.execute().jsonPath();

        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
        String qrCodeId = jsonPath.getString("body.qrCodeId");
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(merchant.getId(), "2000.0",qrCodeId,"PPI_WALLET");
        builder.setOrderId(OrderId);
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"PPI_WALLET");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.as(StaticQrUpiPSPResponse.class);
        Assertions.assertThat(staticQrUpiPSPResponse.getBody().getResultCode())
                .as("Result code mismatch")
                .isEqualToIgnoringCase("FAIL");
        Assertions.assertThat(staticQrUpiPSPResponse.getBody().getResultMsg()).isEqualTo("Merchant Ineligible UPI_PPIWALLET");
        Assertions.assertThat(staticQrUpiPSPResponse.getBody().getSubResultCodeId()).isEqualTo("003");
        String theiaLogs = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia,staticQrUpiPSPRequest.getHeader().getRequestMsgId());
        Assertions.assertThat(theiaLogs).contains("resultMsg=Merchant Ineligible UPI_PPIWALLET");
        //Assertions.assertThat(theiaLogs).contains("offline ,big,Not enabled preference inside this where merchantType");

    }
    @Owner("Shubham Soni")
    @Feature("PGP-46237")
    @Test(description = "DYNAMIC :Verify failure Wallet Wallet Interpoability transaction when FF4J Flag is OFF theia.upipsp.enable.PPIWallet")
    public void failureUpiIntent_WI_TESTCASE19_FF4J_OFF() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.WALINTER_FF4J_OFF;
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(merchant.getId(), "2000.0","qrCodeId","PPI_WALLET");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"PPI_WALLET");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.as(StaticQrUpiPSPResponse.class);
        Assertions.assertThat(staticQrUpiPSPResponse.getBody().getResultCode())
                .as("Result code mismatch")
                .isEqualToIgnoringCase("FAIL");
        Assertions.assertThat(staticQrUpiPSPResponse.getBody().getSubResultCodeId()).isEqualTo("12110075");
        Assertions.assertThat(staticQrUpiPSPResponse.getBody().getResultMsg()).isEqualTo("Merchant cannot accept Wallet on UPI at the moment. Try using other options.");
        String theiaLogs = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia,staticQrUpiPSPRequest.getHeader().getRequestMsgId());
        Assertions.assertThat(theiaLogs).contains("Merchant cannot accept Wallet on UPI at the moment. Try using other options.");
        //Assertions.assertThat(theiaLogs).contains("Wallet On UPI is not enabled");

    }

    @Owner(ABHISHEK_KULKARNI)
    @Feature("PGP-52310")
    @Test(description = "Validate the Txn Limit Error Msg For UPI PPIWallet 3pPsp Txn")
    public void ValidateTxnLimitForUPICc3pPsp() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.UPILITE_LIMIT;
        GenerateQR generateQR = new GenerateQR(merchant.getId(), "", "UPI_QR_CODE");
        String generateResponse = generateQR.execute().asString();
        generateResponse = generateResponse.replace("\\=", "\\\\=");
        generateResponse = generateResponse.replace("\\&", "\\\\&");
        JsonPath generateResponseJson = new JsonPath(generateResponse);
        Assertions.assertThat(generateResponseJson.getString("status")).isEqualTo("SUCCESS");
        String qrCodeId = generateResponseJson.getString("response[0].qrCodeId");
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(merchant.getId(), "20.00", qrCodeId, "PPI_WALLET");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "UPI_CREDIT_CARD");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.as(StaticQrUpiPSPResponse.class);
        String resultMsg = staticQrUpiPSPResponse.getBody().getResultMsg();
        String subResultCodeId = staticQrUpiPSPResponse.getBody().getSubResultCodeId();
        Assertions.assertThat(staticQrUpiPSPResponse.getBody().getResultCode())
                .as("Result code mismatch")
                .isEqualToIgnoringCase("FAIL");
        String theia_facade = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, staticQrUpiPSPRequest.getHeader().getRequestMsgId(), "ACQUIRING_CREATE_ORDER_AND_PAY");
        Assertions.assertThat(theia_facade).contains("\"resultMsg\":\"" + resultMsg + "\"");
        Assertions.assertThat(theia_facade).contains("\"resultCodeId\":\"" + subResultCodeId + "\"");
    }

    @Owner(ABHISHEK_KULKARNI)
    @Feature("PGP-52310")
    @Test(description = "Validate the Daily Limit Error Msg For UPI PPIWallet 3pPsp Txn")
    public void ValidateDailyLimitForUPICc3pPsp() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.UPI_CC_LIMIT;
        GenerateQR generateQR = new GenerateQR(merchant.getId(), "", "UPI_QR_CODE");
        String generateResponse = generateQR.execute().asString();
        generateResponse = generateResponse.replace("\\=", "\\\\=");
        generateResponse = generateResponse.replace("\\&", "\\\\&");
        JsonPath generateResponseJson = new JsonPath(generateResponse);
        Assertions.assertThat(generateResponseJson.getString("status")).isEqualTo("SUCCESS");
        String qrCodeId = generateResponseJson.getString("response[0].qrCodeId");
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(merchant.getId(), "31.00", qrCodeId, "PPI_WALLET");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "UPI_CREDIT_CARD");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.as(StaticQrUpiPSPResponse.class);
        String resultMsg = staticQrUpiPSPResponse.getBody().getResultMsg();
        String subResultCodeId = staticQrUpiPSPResponse.getBody().getSubResultCodeId();
        Assertions.assertThat(staticQrUpiPSPResponse.getBody().getResultCode())
                .as("Result code mismatch")
                .isEqualToIgnoringCase("FAIL");
        String theia_facade = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, staticQrUpiPSPRequest.getHeader().getRequestMsgId(), "ACQUIRING_CREATE_ORDER_AND_PAY");
        Assertions.assertThat(theia_facade).contains("\"resultMsg\":\"" + resultMsg + "\"");
        Assertions.assertThat(theia_facade).contains("\"resultCodeId\":\"" + subResultCodeId + "\"");
    }

    @Owner(ABHISHEK_KULKARNI)
    @Feature("PGP-52310")
    @Test(description = "Validate the Monthly Limit Error Msg For UPI PPIWallet 3pPsp Txn")
    public void ValidateMonthlyLimitForUPICc3pPsp() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.UPI_CC_LIMIT;
        GenerateQR generateQR = new GenerateQR(merchant.getId(), "", "UPI_QR_CODE");
        String generateResponse = generateQR.execute().asString();
        generateResponse = generateResponse.replace("\\=", "\\\\=");
        generateResponse = generateResponse.replace("\\&", "\\\\&");
        JsonPath generateResponseJson = new JsonPath(generateResponse);
        Assertions.assertThat(generateResponseJson.getString("status")).isEqualTo("SUCCESS");
        String qrCodeId = generateResponseJson.getString("response[0].qrCodeId");
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(merchant.getId(), "41.00", qrCodeId, "PPI_WALLET");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "PPI_WALLET");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.as(StaticQrUpiPSPResponse.class);
        String resultMsg = staticQrUpiPSPResponse.getBody().getResultMsg();
        String subResultCodeId = staticQrUpiPSPResponse.getBody().getSubResultCodeId();
        Assertions.assertThat(staticQrUpiPSPResponse.getBody().getResultCode())
                .as("Result code mismatch")
                .isEqualToIgnoringCase("FAIL");
        String theia_facade = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, staticQrUpiPSPRequest.getHeader().getRequestMsgId(), "ACQUIRING_CREATE_ORDER_AND_PAY");
        Assertions.assertThat(theia_facade).contains("\"resultMsg\":\"" + resultMsg + "\"");
        Assertions.assertThat(theia_facade).contains("\"resultCodeId\":\"" + subResultCodeId + "\"");
    }

    @Owner(ABHISHEK_KULKARNI)
    @Feature("PGP-52310")
    @Test(description = "Validate the Merchant Ineligible Msg For UPI PPIWallet Txn")
    public void ValidateMerchantIneligibleForUPIWallet() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.WALINTER_ONLINE_SMALL;
        GenerateQR generateQR = new GenerateQR(merchant.getId(), "", "UPI_QR_CODE");
        String generateResponse = generateQR.execute().asString();
        generateResponse = generateResponse.replace("\\=", "\\\\=");
        generateResponse = generateResponse.replace("\\&", "\\\\&");
        JsonPath generateResponseJson = new JsonPath(generateResponse);
        Assertions.assertThat(generateResponseJson.getString("status")).isEqualTo("SUCCESS");
        String qrCodeId = generateResponseJson.getString("response[0].qrCodeId");
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(merchant.getId(), "2.0", qrCodeId, "PPI_WALLET");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "PPI_WALLET");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.as(StaticQrUpiPSPResponse.class);
        Assertions.assertThat(staticQrUpiPSPResponse.getBody().getResultCode())
                .as("Result code mismatch")
                .isEqualToIgnoringCase("FAIL");
        Assertions.assertThat(staticQrUpiPSPResponse.getBody().getResultMsg())
                .as("Result msg mismatch")
                .isEqualToIgnoringCase("Merchant Ineligible UPI_PPIWALLET");
        Assertions.assertThat(staticQrUpiPSPResponse.getBody().getSubResultCodeId())
                .as("subResultCodeId mismatch")
                .isEqualToIgnoringCase("003");
    }

    @Owner(ABHISHEK_KULKARNI)
    @Feature("PGP-52310")
    @Test(description = "DynamicQR:Validate the Txn Limit Error Msg For UPI PPIWallet 3pPsp Txn")
    public void ValidateTxnLimitForUPICc3pPspDynamicQR() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.UPILITE_LIMIT;
        String OrderId = CommonHelpers.generateOrderId();

        PaymentService paymentService = new PaymentService(merchant, "20", OrderId);
        JsonPath jsonPath = paymentService.execute().jsonPath();

        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
        String qrCodeId = jsonPath.getString("body.qrCodeId");
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(merchant.getId(), "20", qrCodeId, "PPI_WALLET");
        builder.setOrderId(OrderId);
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "PPI_WALLET");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.as(StaticQrUpiPSPResponse.class);
        String resultMsg = staticQrUpiPSPResponse.getBody().getResultMsg();
        String subResultCodeId = staticQrUpiPSPResponse.getBody().getSubResultCodeId();
        Assertions.assertThat(staticQrUpiPSPResponse.getBody().getResultCode())
                .as("Result code mismatch")
                .isEqualToIgnoringCase("FAIL");
        String theia_facade = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, staticQrUpiPSPRequest.getHeader().getRequestMsgId(), "ACQUIRING_PAY_ORDER");
        Assertions.assertThat(theia_facade).contains("\"resultMsg\":\"" + resultMsg + "\"");
        Assertions.assertThat(theia_facade).contains("\"resultCodeId\":\"" + subResultCodeId + "\"");
    }

    @Owner(ABHISHEK_KULKARNI)
    @Feature("PGP-52310")
    @Test(description = "DynamicQR:Validate the Daily Limit Error Msg For UPI PPIWallet 3pPsp Txn")
    public void ValidateDailyLimitForUPICc3pPspDynamicQR() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.UPI_CC_LIMIT;
        String OrderId = CommonHelpers.generateOrderId();

        PaymentService paymentService = new PaymentService(merchant, "31", OrderId);
        JsonPath jsonPath = paymentService.execute().jsonPath();

        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
        String qrCodeId = jsonPath.getString("body.qrCodeId");
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(merchant.getId(), "31", qrCodeId, "PPI_WALLET");
        builder.setOrderId(OrderId);
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "PPI_WALLET");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.as(StaticQrUpiPSPResponse.class);
        String resultMsg = staticQrUpiPSPResponse.getBody().getResultMsg();
        String subResultCodeId = staticQrUpiPSPResponse.getBody().getSubResultCodeId();
        Assertions.assertThat(staticQrUpiPSPResponse.getBody().getResultCode())
                .as("Result code mismatch")
                .isEqualToIgnoringCase("FAIL");
        String theia_facade = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, staticQrUpiPSPRequest.getHeader().getRequestMsgId(), "ACQUIRING_PAY_ORDER");
        Assertions.assertThat(theia_facade).contains("\"resultMsg\":\"" + resultMsg + "\"");
        Assertions.assertThat(theia_facade).contains("\"resultCodeId\":\"" + subResultCodeId + "\"");
    }

    @Owner(ABHISHEK_KULKARNI)
    @Feature("PGP-52310")
    @Test(description = "DynamicQR:Validate the Monthly Limit Error Msg For UPI PPIWallet 3pPsp Txn")
    public void ValidateMonthlyLimitForUPICc3pPspDynamicQR() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.UPI_CC_LIMIT;
        String OrderId = CommonHelpers.generateOrderId();

        PaymentService paymentService = new PaymentService(merchant, "41", OrderId);
        JsonPath jsonPath = paymentService.execute().jsonPath();

        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
        String qrCodeId = jsonPath.getString("body.qrCodeId");
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(merchant.getId(), "41", qrCodeId, "PPI_WALLET");
        builder.setOrderId(OrderId);
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "PPI_WALLET");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.as(StaticQrUpiPSPResponse.class);
        String resultMsg = staticQrUpiPSPResponse.getBody().getResultMsg();
        String subResultCodeId = staticQrUpiPSPResponse.getBody().getSubResultCodeId();
        Assertions.assertThat(staticQrUpiPSPResponse.getBody().getResultCode())
                .as("Result code mismatch")
                .isEqualToIgnoringCase("FAIL");
        String theia_facade = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, staticQrUpiPSPRequest.getHeader().getRequestMsgId(), "ACQUIRING_PAY_ORDER");
        Assertions.assertThat(theia_facade).contains("\"resultMsg\":\"" + resultMsg + "\"");
        Assertions.assertThat(theia_facade).contains("\"resultCodeId\":\"" + subResultCodeId + "\"");
    }

}
