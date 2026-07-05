package scripts.api.MGV;

import com.paytm.api.ProcessTransactionV1;
import com.paytm.api.TxnStatus;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.LogsValidationHelper;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.pages.NativePlusHoldpayPage;
import com.paytm.pages.ResponsePage;
import com.paytm.ServerConfigProvider;
import com.paytm.LocalConfig;
import com.paytm.base.test.User;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.dto.processTransactionV1.TxnAmount;
import com.paytm.dto.processTransactionV1.ExtendInfo;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.json.simple.JSONObject;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.Date;

import static com.paytm.appconstants.Constants.MerchantType.*;
import static com.paytm.appconstants.Constants.Owner.HIMANSHU;

/**
 * MGV Process Transaction API Test Cases
 * Tests various scenarios for Merchant Gift Voucher (MGV) transactions using processTransaction API
 * 
 * @author Himanshu
 * @since 2025-01-02
 */
public class MGV_PTC extends PGPBaseTest {

    private static final String CHANNEL_ID_APP = "APP";
    private static final String REQUEST_TYPE_NATIVE = "native";
    private static final String WEBSITE_RETAIL = "retail";
    private static final String PAYMENT_MODE_GIFT_VOUCHER = "GIFTVOUCHER";
    Constants.MerchantType merchant = MGV_MERCHANT;
    
    // AI-Generated: 2025-01-02 - Test class creation for MGV processTransaction API testing
    @Feature("PGP-60525")
    @Owner(HIMANSHU)
    @Test(description = "Verify successful MGV transaction with valid gift voucher balance")
    public void testMGV_SuccessfulTransaction_WithValidBalance() throws Exception {
        // AI-Generated: 2025-01-02 - Function creation: Test successful MGV transaction
        User user = userManager.getForWrite(Label.STORECASH);
        
        // Create order
        OrderDTO orderDTO = new OrderFactory.PGOnly(merchant, "enhancedweb")
                .setTXN_AMOUNT("10.0")
                .build();
        
        // Build process transaction request
        ProcessTxnV1Request processTxnV1Request = buildMGVProcessTxnRequest(
            merchant.getId(), 
            orderDTO.getORDER_ID(), 
            "10.0",
            user.ssoToken()
        );
        
        // Execute process transaction
        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
        responsePage
                .validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .validateOrderId(orderDTO.getORDER_ID())
                .validateBankName("Paytm GV")
                .validateGatewayName("PMGV")
                .assertAll();
        // Validate transaction status  
        validateTransactionStatus(merchant.getId(), orderDTO.getORDER_ID(), "TXN_SUCCESS");
       
    }

    @Feature("PGP-60525")
    @Owner(HIMANSHU)
    @Test(description = "Verify MGV transaction failure")
    public void testMGV_TransactionFailure_UnknownError() throws Exception {
        // AI-Generated: 2025-01-27 - Function creation: Test MGV transaction with unknown error
        User user = userManager.getForRead(Label.STORECASH);
        
        OrderDTO orderDTO = new OrderFactory.PGOnly(merchant, "enhancedweb")
                .setTXN_AMOUNT("90.0")
                .build();
        
        ProcessTxnV1Request processTxnV1Request = buildMGVProcessTxnRequest(
            merchant.getId(), 
            orderDTO.getORDER_ID(), 
            "90.0",
            user.ssoToken()
        );
        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
        responsePage
                .validateRespCode("227")
                .validateStatus("TXN_FAILURE")
                .validateOrderId(orderDTO.getORDER_ID())
                .validateBankName("Paytm GV")
                .validateGatewayName("PMGV")
                .assertAll();
        // validateTransactionStatus(merchant.getId(), orderDTO.getORDER_ID(), "TXN_FAILURE");
    }
    
    /**
     * Build MGV process transaction request
     * AI-Generated: 2025-01-02 - Helper function creation
     */
    @Step("Build MGV process transaction request")
    private ProcessTxnV1Request buildMGVProcessTxnRequest(String mid, String orderId, String txnAmount, String ssoToken) {
        return new ProcessTxnV1Request.Builder(mid, "SSO", ssoToken, orderId, txnAmount)
                .setPaymentMode(PAYMENT_MODE_GIFT_VOUCHER)
                .setChannelId(CHANNEL_ID_APP)
                .setRequestType(REQUEST_TYPE_NATIVE)
                .setWebsite(WEBSITE_RETAIL)
                .setTxnAmount(new TxnAmount().setValue(txnAmount).setCurrency("INR"))
                .setExtendInfo(buildExtendInfo(orderId))
                .setRiskExtendInfo("scanType:active|isContact:false|otpReadFlag:false|contactCreateTime:0|isRooted:false|displayName:Gaurav+corner|mode:recentBeneficiary")
                .build();
    }

    /**
     * Build extend info for MGV transaction
     * AI-Generated: 2025-01-02 - Helper function creation
     */
    @Step("Build extend info for MGV transaction")
    private ExtendInfo buildExtendInfo(String orderId) {
        ExtendInfo extendInfo = new ExtendInfo();
        String additionalInfo = String.format(
            "posId:1646741152204|merchantGenre:OFFLINE|combinationRequestId:PTM0262875C80834892894BB96B8FBBB491|merchantVerified:0|mercUnqRef:RZmPEw36213230498609|comments:|additionalInfo:posId:1646741152204|merchantGenre:OFFLINE|combinationRequestId:PTM0262875C80834892894BB96B8FBBB491|merchantVerified:0"
        );
        extendInfo.setAdditionalInfo(additionalInfo);
        extendInfo.setUdf1("1646741152204");
        extendInfo.setMercUnqRef("RZmPEw36213230498609");
        extendInfo.setComments("");
        return extendInfo;
    }

    /**
     * Validate process transaction response
     * AI-Generated: 2025-01-27 - Helper function creation
     */
    @Step("Validate process transaction response")
    private void validateProcessTxnResponse(Response response, String expectedResultCode, String expectedResultMsg) {
        validateProcessTxnResponse(response, expectedResultCode, expectedResultMsg, 200);
    }

    /**
     * Validate process transaction response with custom HTTP status
     * AI-Generated: 2025-01-27 - Helper function creation
     */
    @Step("Validate process transaction response with custom HTTP status")
    private void validateProcessTxnResponse(Response response, String expectedResultCode, String expectedResultMsg, int expectedHttpStatus) {
        Assertions.assertThat(response.getStatusCode()).as("HTTP Status Code").isEqualTo(expectedHttpStatus);
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultCode"))
                .as("Result Code").isEqualTo(expectedResultCode);
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultMsg"))
                .as("Result Message").isEqualTo(expectedResultMsg);
    }

    /**
     * Validate transaction status
     * AI-Generated: 2025-01-02 - Helper function creation
     */
    @Step("Validate transaction status")
    private void validateTransactionStatus(String mid, String orderId, String expectedStatus) {
        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateMid(mid)
                .validateOrderid(orderId)
                .validateStatus(expectedStatus)
                .validatePaymentMode("GIFTVOUCHER")
                .validateTxnDate(new Date())
                .AssertAll();
    }
}
