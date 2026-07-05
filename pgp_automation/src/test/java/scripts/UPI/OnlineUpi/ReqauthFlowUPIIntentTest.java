package scripts.UPI.OnlineUpi;
import com.paytm.api.nativeAPI.InitTxn;
import org.assertj.core.api.SoftAssertions;
import com.paytm.apphelpers.LogsValidationHelper;
import com.paytm.apphelpers.PG2LogsValidationHelper;
import com.paytm.appconstants.Constants;
import com.paytm.appconstants.FF4JFeatures;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.apphelpers.QRHelper;
import com.paytm.LocalConfig;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.dto.processTransactionV1.response.ProcessTxnV1Response;
import com.paytm.dto.processTransactionV1.ExtendInfo;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.json.JSONObject;
import com.paytm.utils.ff4j.FF4JFlags;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.paytm.framework.api.BaseApi;
import com.paytm.api.nativeAPI.SubscriptionCreate;
import com.paytm.api.StaticQrUpiPSP;
import com.paytm.dto.upiIntent.staticQR.StaticQrUpiPSPRequest;
import com.paytm.api.InstaproxyPtybliResponse;
import com.paytm.api.PTYBLIIntentCallback;
import com.paytm.api.Deals.GetPaymentStatus;

public class ReqauthFlowUPIIntentTest extends PGPBaseTest{

        @BeforeMethod
        public void enableCreateOrderInInitTxnFeature() {
            FF4JFlags.enable(FF4JFeatures.CREATE_ORDER_IN_INTTXN);
        }

        @Owner(Constants.Owner.LOKESH_SAXENA)
        @Feature("PGP-60677")
        @Test(description = "ReqAuth flow txn when convinience fee and platform fee is applied on merchant and UPI_SAVINGS is selected as payerPaymentInstrument")
        public void VerfyReqAuthTxnWhenConvenienceFeeIsAppliedOnMerchantAndUpiSavingsIsSelectedAsPayerPaymentInstrument() throws Exception{
            // AI-Generated: 2025-01-02 - Refactoring: Updated to use all caps MID constant
            Constants.MerchantType merchant = Constants.MerchantType.REQAUTH_FLOW_UPI_CONFEE_MID;
            // Create InitTxnDTO with the provided parameters
            String custId = "Test" + CommonHelpers.generateOrderId();
            String orderId = "test" + CommonHelpers.generateOrderId();
            InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                    .setTxnValue("800")
                    .setCustId(custId)
                    .setCallbackUrl(LocalConfig.MOCK_HOST + "/mockbank" + Constants.PagePath.COMMON_RESPONSE_PAGE_PATH)
                    .setWebsiteName("retail")
                    .setOrderId(orderId)
                    .build();
            
            // Execute the initiate transaction
            InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
            
            String txnToken = initTxnResponseDTO.getBody().getTxnToken();
    
            // Create Process Transaction Request with UPI_INTENT
            ExtendInfo extendInfo = new ExtendInfo();
            extendInfo.setAdditionalProperty("payerCmid", "8006006993");
            
            ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(merchant.getId(), txnToken, orderId)
                    .setPaymentMode("UPI_INTENT")
                    .setAuthMode("USRPWD")
                    .setQrImageRequired(true)
                    .setSeqNumber("PYTM0123456")
                    .setExtendInfo(extendInfo)
                    .build();
    
            // Execute Process Transaction
            ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
            Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
                    String deeplink = processTxnV1Response.getBody().getDeepLinkInfo().getDeepLink();
            // Extract deeplink information using QRHelper
            Map<String, String> deeplinkInfo = QRHelper.parseDeeplinkInfo(deeplink);
            String transactionRef = deeplinkInfo.get("tr");
            String amountFromDeeplink = deeplinkInfo.get("amount");
            String payeeVpa = deeplinkInfo.get("payeeVpa");
            
                    // Build UPI PSP payment using StaticQrUpiPSP
            StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder("SEAMLESS_3D_FORM", merchant.getId(), transactionRef, amountFromDeeplink, "QR_PAYMENT");
            builder.setPayerVpa("paytmTest@ptys")
                   .setPayeeVpa(payeeVpa)
                   .setPayerName("test")
                   .setPayerPSP("Phonepe")
                   .setPayerPaymentInstrument("UPI_SAVINGS")
                   .setPayerPaymentInstrumentFee("0.00");
            StaticQrUpiPSPRequest staticQrUpiPSPRequest = builder.build();
            StaticQrUpiPSP upiPspPaymentAPI = new StaticQrUpiPSP(staticQrUpiPSPRequest);
            
            // Execute the API call and get response
            JsonPath responseJsonPath = upiPspPaymentAPI.execute().jsonPath();
            
            // Extract externalSerialNo from the response
            String externalSerialNo = responseJsonPath.getString("body.externalSerialNo");
            
            // Validate the response using soft assertions
            SoftAssertions softly1 = new SoftAssertions();
            softly1.assertThat(responseJsonPath.getString("body.resultCode")).as("API response should return SUCCESS").isEqualTo("SUCCESS");
            softly1.assertThat(responseJsonPath.getString("body.orderId")).as("Order ID should match transaction reference").isEqualTo(transactionRef);
            softly1.assertThat(responseJsonPath.getString("body.txnAmount")).as("Transaction amount should match deeplink amount").isEqualTo(amountFromDeeplink);
            softly1.assertAll();

            // Execute UPI Secure Response API using the externalSerialNo from UPI PSP response
            InstaproxyPtybliResponse upiSecureResponseAPI = (InstaproxyPtybliResponse) new InstaproxyPtybliResponse(
                amountFromDeeplink, // amount from deeplink
                externalSerialNo, // externalSerialNo from UPI PSP response
                "27850124741", // bankRrn
                "UPI_SAVINGS" // paymentInstrument
            )
            .setContext("body.payeeVpa", payeeVpa)
            .deleteContext("body.paymentInstrument")
            .deleteContext("body.creditCardInfo");

            // Execute the secure response API call
            JsonPath secureResponseJsonPath = upiSecureResponseAPI.execute().jsonPath();

            // Validate the secure response using soft assertions
            SoftAssertions softlySecure = new SoftAssertions();
            softlySecure.assertThat(secureResponseJsonPath.getString("body.resultCodeId")).as("Secure result code id").contains("001");
            softlySecure.assertThat(secureResponseJsonPath.getString("body.resultCode")).as("Secure result code").contains("SUCCESS");
            softlySecure.assertAll();

            // Check theia_facade log for ROUTER_CONSULT API response using LogsValidationHelper
            String theiaFacadeLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderId, "ROUTER_CONSULT", "RESPONSE");
            
            // Check theia_facade log for ACQUIRING_INQUIRE_WITH_MERCHANT_TRANS_ID API response using LogsValidationHelper
            String acquiringLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderId, "ACQUIRING_INQUIRE_WITH_MERCHANT_TRANS_ID", "RESPONSE");
            
            // AI-Generated: 2025-01-02 - Feature addition: Extract PGPID from PROCESS_TXN request in THEIA_REQ_RESP and verify no ACQUIRING_PAY_ORDER logs
            String ptcLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.THEIA_REQ_RESP, orderId, "v1/processTransaction", "REQUEST");
            String pgpId = ptcLogs.substring(ptcLogs.indexOf("\"PGP_ID\": \"") + 11, ptcLogs.indexOf("\",", ptcLogs.indexOf("\"PGP_ID\": \"")));
            
            // Verify there are no ACQUIRING_PAY_ORDER logs for the extracted PGPID
            String acquiringPayOrderLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, pgpId, "ACQUIRING_PAY_ORDER", "REQUEST");
            
            SoftAssertions softly = new SoftAssertions();
            softly.assertThat(theiaFacadeLogs)
                    .as("ROUTER_CONSULT response should contain selectedChannel PTYBLI")
                    .contains("\"selectedChannel\":\"PTYBLI\"");
            softly.assertThat(theiaFacadeLogs)
                    .as("ROUTER_CONSULT response should contain reqAuthSupported true")
                    .contains("\"reqAuthSupported\":\"true\"");
            softly.assertThat(acquiringLogs)
                    .as("ACQUIRING_INQUIRE_WITH_MERCHANT_TRANS_ID response should contain acquirementStatus INIT")
                    .contains("\"acquirementStatus\":\"INIT\"");
            softly.assertThat(acquiringPayOrderLogs)
                    .as("Should not find any ACQUIRING_PAY_ORDER logs for PGPID from PROCESS_TXN request")
                    .isEmpty();
            softly.assertAll();

        }


        @Owner(Constants.Owner.LOKESH_SAXENA)
        @Feature("PGP-60677")
        @Test(description = "ReqAuth flow txn when convinience fee and platform fee is applied on merchant and UPI_CREDITLINE is selected as payerPaymentInstrument")
        public void VerfyReqAuthTxnWhenConvenienceFeeIsAppliedOnMerchantAndUPI_CREDITLINEIsSelectedAsPayerPaymentInstrument() throws Exception{
            // AI-Generated: 2025-01-02 - Refactoring: Updated to use all caps MID constant
            Constants.MerchantType merchant = Constants.MerchantType.REQAUTH_FLOW_UPI_CONFEE_MID;
            // Create InitTxnDTO with the provided parameters
            String custId = "Test" + CommonHelpers.generateOrderId();
            String orderId = "test" + CommonHelpers.generateOrderId();
            InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                    .setTxnValue("800")
                    .setCustId(custId)
                    .setCallbackUrl(LocalConfig.MOCK_HOST + "/mockbank" + Constants.PagePath.COMMON_RESPONSE_PAGE_PATH)
                    .setWebsiteName("retail")
                    .setOrderId(orderId)
                    .build();
            
            // Execute the initiate transaction
            InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
            
            String txnToken = initTxnResponseDTO.getBody().getTxnToken();
    
            // Create Process Transaction Request with UPI_INTENT
            ExtendInfo extendInfo = new ExtendInfo();
            extendInfo.setAdditionalProperty("payerCmid", "8006006993");
            
            ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(merchant.getId(), txnToken, orderId)
                    .setPaymentMode("UPI_INTENT")
                    .setAuthMode("USRPWD")
                    .setQrImageRequired(true)
                    .setSeqNumber("PYTM0123456")
                    .setExtendInfo(extendInfo)
                    .build();
    
            // Execute Process Transaction
            ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
            Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
                    String deeplink = processTxnV1Response.getBody().getDeepLinkInfo().getDeepLink();
            // Extract deeplink information using QRHelper
            Map<String, String> deeplinkInfo = QRHelper.parseDeeplinkInfo(deeplink);
            String transactionRef = deeplinkInfo.get("tr");
            String amountFromDeeplink = deeplinkInfo.get("amount");
            String payeeVpa = deeplinkInfo.get("payeeVpa");
        
            // Extract CCONFEE and PCONFEE from deeplink using QRHelper
            String cconfee = deeplinkInfo.getOrDefault("cconfee", "0.00");
            String pconfee = deeplinkInfo.getOrDefault("pconfee", "0.00");
        
            // Calculate total amount (amountFromDeeplink + CCONFEE)
            double totalAmount = Double.parseDouble(amountFromDeeplink) + Double.parseDouble(cconfee);
            String totalAmountString = String.valueOf(totalAmount);
    
            
                    // Build UPI PSP payment using StaticQrUpiPSP
            StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder("SEAMLESS_3D_FORM", merchant.getId(), transactionRef, totalAmountString, "QR_PAYMENT");
            builder.setPayerVpa("paytmTest@ptys")
                   .setPayeeVpa(payeeVpa)
                   .setPayerName("test")
                   .setPayerPSP("Phonepe")
                   .setPayerPaymentInstrument("UPI_CREDITLINE")
                   .setPayerPaymentInstrumentFee("18.88");
            StaticQrUpiPSPRequest staticQrUpiPSPRequest = builder.build();
            StaticQrUpiPSP upiPspPaymentAPI = new StaticQrUpiPSP(staticQrUpiPSPRequest);
            
            // Execute the API call and get response
            JsonPath responseJsonPath = upiPspPaymentAPI.execute().jsonPath();
            
            // Extract externalSerialNo from the response
            String externalSerialNo = responseJsonPath.getString("body.externalSerialNo");
            
            // Validate the response using soft assertions
            SoftAssertions softly4 = new SoftAssertions();
            softly4.assertThat(responseJsonPath.getString("body.resultCode")).as("API response should return SUCCESS").isEqualTo("SUCCESS");
            softly4.assertThat(responseJsonPath.getString("body.orderId")).as("Order ID should match transaction reference").isEqualTo(transactionRef);
            softly4.assertThat(responseJsonPath.getString("body.txnAmount")).as("Transaction amount should match total amount").isEqualTo(totalAmountString);
            softly4.assertAll();

            // Execute UPI Secure Response API using the externalSerialNo from UPI PSP response
            InstaproxyPtybliResponse upiSecureResponseAPI = (InstaproxyPtybliResponse) new InstaproxyPtybliResponse(
                totalAmountString, // amount (using total amount: amountFromDeeplink + CCONFEE)
                externalSerialNo, // externalSerialNo from UPI PSP response
                "27850124741", // bankRrn
                "UPI_CREDITLINE" // paymentInstrument
            )
            .setContext("body.payeeVpa", payeeVpa)
            .deleteContext("body.paymentInstrument")
            .deleteContext("body.creditCardInfo");

            // Execute the secure response API call
            JsonPath secureResponseJsonPath = upiSecureResponseAPI.execute().jsonPath();

            // Validate the secure response using soft assertions
            SoftAssertions softlySecure2 = new SoftAssertions();
            softlySecure2.assertThat(secureResponseJsonPath.getString("body.resultCodeId")).as("Secure result code id").contains("001");
            softlySecure2.assertThat(secureResponseJsonPath.getString("body.resultCode")).as("Secure result code").contains("SUCCESS");
            softlySecure2.assertAll();

            // Check theia_facade log for ROUTER_CONSULT API response using LogsValidationHelper
            String theiaFacadeLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderId, "ROUTER_CONSULT", "RESPONSE");
            
            // Check theia_facade log for ACQUIRING_INQUIRE_WITH_MERCHANT_TRANS_ID API response using LogsValidationHelper
            String acquiringLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderId, "ACQUIRING_INQUIRE_WITH_MERCHANT_TRANS_ID", "RESPONSE");
            
            // AI-Generated: 2025-01-02 - Feature addition: Extract PGPID from PROCESS_TXN request in THEIA_REQ_RESP and verify no ACQUIRING_PAY_ORDER logs
            String ptcLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.THEIA_REQ_RESP, orderId, "v1/processTransaction", "REQUEST");
            String pgpId = ptcLogs.substring(ptcLogs.indexOf("\"PGP_ID\": \"") + 11, ptcLogs.indexOf("\",", ptcLogs.indexOf("\"PGP_ID\": \"")));
            
            // Verify there are no ACQUIRING_PAY_ORDER logs for the extracted PGPID
            String acquiringPayOrderLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, pgpId, "ACQUIRING_PAY_ORDER", "REQUEST");
            
            SoftAssertions softly = new SoftAssertions();
            softly.assertThat(theiaFacadeLogs)
                    .as("ROUTER_CONSULT response should contain selectedChannel PTYBLI")
                    .contains("\"selectedChannel\":\"PTYBLI\"");
            softly.assertThat(theiaFacadeLogs)
                    .as("ROUTER_CONSULT response should contain reqAuthSupported true")
                    .contains("\"reqAuthSupported\":\"true\"");
            softly.assertThat(acquiringLogs)
                    .as("ACQUIRING_INQUIRE_WITH_MERCHANT_TRANS_ID response should contain acquirementStatus INIT")
                    .contains("\"acquirementStatus\":\"INIT\"");
            softly.assertThat(acquiringPayOrderLogs)
                    .as("Should not find any ACQUIRING_PAY_ORDER logs for PGPID from PROCESS_TXN request")
                    .isEmpty();
            softly.assertAll();

        }


        @Owner(Constants.Owner.LOKESH_SAXENA)
        @Feature("PGP-60677")
        @Test(description = "ReqAuth flow txn when convinience fee is applied on merchant and UPI_CREDIT_CARD is selected as payerPaymentInstrument")
        public void VerfyReqAuthTxnWhenConvenienceFeeIsAppliedOnMerchantAndUPI_CREDIT_CARDIsSelectedAsPayerPaymentInstrument() throws Exception{
            // AI-Generated: 2025-01-02 - Refactoring: Updated to use all caps MID constant
            Constants.MerchantType merchant = Constants.MerchantType.REQAUTH_FLOW_UPI_CONFEE_MID;
            // Create InitTxnDTO with the provided parameters
            String custId = "Test" + CommonHelpers.generateOrderId();
            String orderId = "test" + CommonHelpers.generateOrderId();
            InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                    .setTxnValue("800")
                    .setCustId(custId)
                    .setCallbackUrl(LocalConfig.MOCK_HOST + "/mockbank" + Constants.PagePath.COMMON_RESPONSE_PAGE_PATH)
                    .setWebsiteName("retail")
                    .setOrderId(orderId)
                    .build();
            
            // Execute the initiate transaction
            InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
            
            String txnToken = initTxnResponseDTO.getBody().getTxnToken();
    
            // Create Process Transaction Request with UPI_INTENT
            ExtendInfo extendInfo = new ExtendInfo();
            extendInfo.setAdditionalProperty("payerCmid", "8006006993");
            
            ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(merchant.getId(), txnToken, orderId)
                    .setPaymentMode("UPI_INTENT")
                    .setAuthMode("USRPWD")
                    .setQrImageRequired(true)
                    .setSeqNumber("PYTM0123456")
                    .setExtendInfo(extendInfo)
                    .build();
    
            // Execute Process Transaction
            ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
            Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
                    String deeplink = processTxnV1Response.getBody().getDeepLinkInfo().getDeepLink();
            // Extract deeplink information using QRHelper
            Map<String, String> deeplinkInfo = QRHelper.parseDeeplinkInfo(deeplink);
            String transactionRef = deeplinkInfo.get("tr");
            String amountFromDeeplink = deeplinkInfo.get("amount");
            String payeeVpa = deeplinkInfo.get("payeeVpa");
        
            // Extract CCONFEE and PCONFEE from deeplink using QRHelper
            String cconfee = deeplinkInfo.getOrDefault("cconfee", "0.00");
            String pconfee = deeplinkInfo.getOrDefault("pconfee", "0.00");
        
            // Calculate total amount (amountFromDeeplink + CCONFEE)
            double totalAmount = Double.parseDouble(amountFromDeeplink) + Double.parseDouble(cconfee);
            String totalAmountString = String.valueOf(totalAmount);
            
                    // Build UPI PSP payment using StaticQrUpiPSP
            StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder("SEAMLESS_3D_FORM", merchant.getId(), transactionRef, totalAmountString, "QR_PAYMENT");
            builder.setPayerVpa("paytmTest@ptys")
                   .setPayeeVpa(payeeVpa)
                   .setPayerName("test")
                   .setPayerPSP("Phonepe")
                   .setPayerPaymentInstrument("UPI_CREDIT_CARD")
                   .setPayerPaymentInstrumentFee("18.88");
            StaticQrUpiPSPRequest staticQrUpiPSPRequest = builder.build();
            StaticQrUpiPSP upiPspPaymentAPI = new StaticQrUpiPSP(staticQrUpiPSPRequest);
            
            // Execute the API call and get response
            JsonPath responseJsonPath = upiPspPaymentAPI.execute().jsonPath();
            
            // Extract externalSerialNo from the response
            String externalSerialNo = responseJsonPath.getString("body.externalSerialNo");
            
            // Validate the response using soft assertions
            SoftAssertions softly5 = new SoftAssertions();
            softly5.assertThat(responseJsonPath.getString("body.resultCode")).as("API response should return SUCCESS").isEqualTo("SUCCESS");
            softly5.assertThat(responseJsonPath.getString("body.orderId")).as("Order ID should match transaction reference").isEqualTo(transactionRef);
            softly5.assertThat(responseJsonPath.getString("body.txnAmount")).as("Transaction amount should match total amount").isEqualTo(totalAmountString);
            softly5.assertAll();

            // Execute UPI Secure Response API using the externalSerialNo from UPI PSP response
            InstaproxyPtybliResponse upiSecureResponseAPI = (InstaproxyPtybliResponse) new InstaproxyPtybliResponse(
                totalAmountString, // amount (using total amount: amountFromDeeplink + CCONFEE)
                externalSerialNo, // externalSerialNo from UPI PSP response
                "27850124741", // bankRrn
                "UPI_CREDIT_CARD" // paymentInstrument
            )
            .setContext("body.payeeVpa", payeeVpa)
            .deleteContext("body.paymentInstrument")
            .deleteContext("body.creditCardInfo");

            // Execute the secure response API call
            JsonPath secureResponseJsonPath = upiSecureResponseAPI.execute().jsonPath();

            // Validate the secure response using soft assertions
            SoftAssertions softlySecure3 = new SoftAssertions();
            softlySecure3.assertThat(secureResponseJsonPath.getString("body.resultCodeId")).as("Secure result code id").contains("001");
            softlySecure3.assertThat(secureResponseJsonPath.getString("body.resultCode")).as("Secure result code").contains("SUCCESS");
            softlySecure3.assertAll();

            // Check theia_facade log for ROUTER_CONSULT API response using LogsValidationHelper
            String theiaFacadeLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderId, "ROUTER_CONSULT", "RESPONSE");
            
            // Check theia_facade log for ACQUIRING_INQUIRE_WITH_MERCHANT_TRANS_ID API response using LogsValidationHelper
            String acquiringLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderId, "ACQUIRING_INQUIRE_WITH_MERCHANT_TRANS_ID", "RESPONSE");
            
            // AI-Generated: 2025-01-02 - Feature addition: Extract PGPID from PROCESS_TXN request in THEIA_REQ_RESP and verify no ACQUIRING_PAY_ORDER logs
            String ptcLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.THEIA_REQ_RESP, orderId, "v1/processTransaction", "REQUEST");
            String pgpId = ptcLogs.substring(ptcLogs.indexOf("\"PGP_ID\": \"") + 11, ptcLogs.indexOf("\",", ptcLogs.indexOf("\"PGP_ID\": \"")));
            
            // Verify there are no ACQUIRING_PAY_ORDER logs for the extracted PGPID
            String acquiringPayOrderLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, pgpId, "ACQUIRING_PAY_ORDER", "REQUEST");
            
            SoftAssertions softly = new SoftAssertions();
            softly.assertThat(theiaFacadeLogs)
                    .as("ROUTER_CONSULT response should contain selectedChannel PTYBLI")
                    .contains("\"selectedChannel\":\"PTYBLI\"");
            softly.assertThat(theiaFacadeLogs)
                    .as("ROUTER_CONSULT response should contain reqAuthSupported true")
                    .contains("\"reqAuthSupported\":\"true\"");
            softly.assertThat(acquiringLogs)
                    .as("ACQUIRING_INQUIRE_WITH_MERCHANT_TRANS_ID response should contain acquirementStatus INIT")
                    .contains("\"acquirementStatus\":\"INIT\"");
            softly.assertThat(acquiringPayOrderLogs)
                    .as("Should not find any ACQUIRING_PAY_ORDER logs for PGPID from PROCESS_TXN request")
                    .isEmpty();
            softly.assertAll();

        }


        @Owner(Constants.Owner.LOKESH_SAXENA)
        @Feature("PGP-60677")
        @Test(description = "ReqAuth flow txn when convinience fee is applied on merchant and PPI_WALLET is selected as payerPaymentInstrument")
        public void VerfyReqAuthTxnWhenConvenienceFeeIsAppliedOnMerchantAndPPI_WALLETIsSelectedAsPayerPaymentInstrument() throws Exception{
            // AI-Generated: 2025-01-02 - Refactoring: Updated to use all caps MID constant
            Constants.MerchantType merchant = Constants.MerchantType.REQAUTH_FLOW_UPI_CONFEE_MID;
            // Create InitTxnDTO with the provided parameters
            String custId = "Test" + CommonHelpers.generateOrderId();
            String orderId = "test" + CommonHelpers.generateOrderId();
            InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                    .setTxnValue("800")
                    .setCustId(custId)
                    .setCallbackUrl(LocalConfig.MOCK_HOST + "/mockbank" + Constants.PagePath.COMMON_RESPONSE_PAGE_PATH)
                    .setWebsiteName("retail")
                    .setOrderId(orderId)
                    .build();
            
            // Execute the initiate transaction
            InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
            
            String txnToken = initTxnResponseDTO.getBody().getTxnToken();
    
            // Create Process Transaction Request with UPI_INTENT
            ExtendInfo extendInfo = new ExtendInfo();
            extendInfo.setAdditionalProperty("payerCmid", "8006006993");
            
            ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(merchant.getId(), txnToken, orderId)
                    .setPaymentMode("UPI_INTENT")
                    .setAuthMode("USRPWD")
                    .setQrImageRequired(true)
                    .setSeqNumber("PYTM0123456")
                    .setExtendInfo(extendInfo)
                    .build();
    
            // Execute Process Transaction
            ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
            Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
                    String deeplink = processTxnV1Response.getBody().getDeepLinkInfo().getDeepLink();
            // Extract deeplink information using QRHelper
            Map<String, String> deeplinkInfo = QRHelper.parseDeeplinkInfo(deeplink);
            String transactionRef = deeplinkInfo.get("tr");
            String amountFromDeeplink = deeplinkInfo.get("amount");
            String payeeVpa = deeplinkInfo.get("payeeVpa");
        
            // Extract CCONFEE and PCONFEE from deeplink using QRHelper
            String cconfee = deeplinkInfo.getOrDefault("cconfee", "0.00");
            String pconfee = deeplinkInfo.getOrDefault("pconfee", "0.00");
        
            // Calculate total amount (amountFromDeeplink + PCONFEE)
            double totalAmount = Double.parseDouble(amountFromDeeplink) + Double.parseDouble(pconfee);
            String totalAmountString = String.valueOf(totalAmount);
            
                    // Build UPI PSP payment using StaticQrUpiPSP
            StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder("SEAMLESS_3D_FORM", merchant.getId(), transactionRef, totalAmountString, "QR_PAYMENT");
            builder.setPayerVpa("paytmTest@ptys")
                   .setPayeeVpa(payeeVpa)
                   .setPayerName("test")
                   .setPayerPSP("Phonepe")
                   .setPayerPaymentInstrument("PPI_WALLET")
                   .setPayerPaymentInstrumentFee("18.88");
            StaticQrUpiPSPRequest staticQrUpiPSPRequest = builder.build();
            StaticQrUpiPSP upiPspPaymentAPI = new StaticQrUpiPSP(staticQrUpiPSPRequest);
            
            // Execute the API call and get response
            JsonPath responseJsonPath = upiPspPaymentAPI.execute().jsonPath();
            
            // Extract externalSerialNo from the response
            String externalSerialNo = responseJsonPath.getString("body.externalSerialNo");
            
            // Validate the response using soft assertions
            SoftAssertions softly6 = new SoftAssertions();
            softly6.assertThat(responseJsonPath.getString("body.resultCode")).as("API response should return SUCCESS").isEqualTo("SUCCESS");
            softly6.assertThat(responseJsonPath.getString("body.orderId")).as("Order ID should match transaction reference").isEqualTo(transactionRef);
            softly6.assertThat(responseJsonPath.getString("body.txnAmount")).as("Transaction amount should match total amount").isEqualTo(totalAmountString);
            softly6.assertAll();

            // Execute UPI Secure Response API using the externalSerialNo from UPI PSP response
            InstaproxyPtybliResponse upiSecureResponseAPI = (InstaproxyPtybliResponse) new InstaproxyPtybliResponse(
                totalAmountString, // amount (using total amount: amountFromDeeplink + CCONFEE)
                externalSerialNo, // externalSerialNo from UPI PSP response
                "27850124741", // bankRrn
                "PPI_WALLET" // paymentInstrument
            )
            .setContext("body.payeeVpa", payeeVpa)
            .deleteContext("body.paymentInstrument")
            .deleteContext("body.creditCardInfo");

            // Execute the secure response API call
            JsonPath secureResponseJsonPath = upiSecureResponseAPI.execute().jsonPath();

            // Validate the secure response using soft assertions
            SoftAssertions softlySecure4 = new SoftAssertions();
            softlySecure4.assertThat(secureResponseJsonPath.getString("body.resultCodeId")).as("Secure result code id").contains("001");
            softlySecure4.assertThat(secureResponseJsonPath.getString("body.resultCode")).as("Secure result code").contains("SUCCESS");
            softlySecure4.assertAll();

            // Check theia_facade log for ROUTER_CONSULT API response using LogsValidationHelper
            String theiaFacadeLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderId, "ROUTER_CONSULT", "RESPONSE");
            
            // Check theia_facade log for ACQUIRING_INQUIRE_WITH_MERCHANT_TRANS_ID API response using LogsValidationHelper
            String acquiringLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderId, "ACQUIRING_INQUIRE_WITH_MERCHANT_TRANS_ID", "RESPONSE");
            
            // AI-Generated: 2025-01-02 - Feature addition: Extract PGPID from PROCESS_TXN request in THEIA_REQ_RESP and verify no ACQUIRING_PAY_ORDER logs
            String ptcLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.THEIA_REQ_RESP, orderId, "v1/processTransaction", "REQUEST");
            String pgpId = ptcLogs.substring(ptcLogs.indexOf("\"PGP_ID\": \"") + 11, ptcLogs.indexOf("\",", ptcLogs.indexOf("\"PGP_ID\": \"")));
            
            // Verify there are no ACQUIRING_PAY_ORDER logs for the extracted PGPID
            String acquiringPayOrderLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, pgpId, "ACQUIRING_PAY_ORDER", "REQUEST");
            
            SoftAssertions softly = new SoftAssertions();
            softly.assertThat(theiaFacadeLogs)
                    .as("ROUTER_CONSULT response should contain selectedChannel PTYBLI")
                    .contains("\"selectedChannel\":\"PTYBLI\"");
            softly.assertThat(theiaFacadeLogs)
                    .as("ROUTER_CONSULT response should contain reqAuthSupported true")
                    .contains("\"reqAuthSupported\":\"true\"");
            softly.assertThat(acquiringLogs)
                    .as("ACQUIRING_INQUIRE_WITH_MERCHANT_TRANS_ID response should contain acquirementStatus INIT")
                    .contains("\"acquirementStatus\":\"INIT\"");
            softly.assertThat(acquiringPayOrderLogs)
                    .as("Should not find any ACQUIRING_PAY_ORDER logs for PGPID from PROCESS_TXN request")
                    .isEmpty();
            softly.assertAll();

        }


        @Owner(Constants.Owner.LOKESH_SAXENA)
        @Feature("PGP-60677")
        @Test(description = "ReqAuth flow txn when UPI_TR_ACQ_ID_ENABLE is enabled on merchant")
        public void VerfyReqAuthTxnWhenUPI_TR_ACQ_ID_ENABLEIsEnabledOnMerchant() throws Exception{
            // AI-Generated: 2025-01-02 - Refactoring: Updated to use all caps MID constant
            Constants.MerchantType merchant = Constants.MerchantType.REQAUTH_FLOW_UPI_ACQ_ID_MID;
            // Create InitTxnDTO with the provided parameters
            String custId = "Test" + CommonHelpers.generateOrderId();
            String orderId = "test" + CommonHelpers.generateOrderId();
            InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                    .setTxnValue("800")
                    .setCustId(custId)
                    .setCallbackUrl(LocalConfig.MOCK_HOST + "/mockbank" + Constants.PagePath.COMMON_RESPONSE_PAGE_PATH)
                    .setWebsiteName("retail")
                    .setOrderId(orderId)
                    .build();
            
            // Execute the initiate transaction
            InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
            
            String txnToken = initTxnResponseDTO.getBody().getTxnToken();
    
            // Create Process Transaction Request with UPI_INTENT
            ExtendInfo extendInfo = new ExtendInfo();
            extendInfo.setAdditionalProperty("payerCmid", "8006006993");
            
            ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(merchant.getId(), txnToken, orderId)
                    .setPaymentMode("UPI_INTENT")
                    .setAuthMode("USRPWD")
                    .setQrImageRequired(true)
                    .setSeqNumber("PYTM0123456")
                    .setExtendInfo(extendInfo)
                    .build();
    
            // Execute Process Transaction
            ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
            Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
                    String deeplink = processTxnV1Response.getBody().getDeepLinkInfo().getDeepLink();
            // Extract deeplink information using QRHelper
            Map<String, String> deeplinkInfo = QRHelper.parseDeeplinkInfo(deeplink);
            String transactionRef = deeplinkInfo.get("tr");
            String amountFromDeeplink = deeplinkInfo.get("amount");
            String payeeVpa = deeplinkInfo.get("payeeVpa");
            
                    // Build UPI PSP payment using StaticQrUpiPSP
            StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder("SEAMLESS_3D_FORM", merchant.getId(), transactionRef, amountFromDeeplink, "QR_PAYMENT");
            builder.setPayerVpa("paytmTest@ptys")
                   .setPayeeVpa(payeeVpa)
                   .setPayerName("test")
                   .setPayerPSP("Phonepe")
                   .setPayerPaymentInstrument("")
                   .setPayerPaymentInstrumentFee("0.00");
            StaticQrUpiPSPRequest staticQrUpiPSPRequest = builder.build();
            StaticQrUpiPSP upiPspPaymentAPI = new StaticQrUpiPSP(staticQrUpiPSPRequest);
            
            // Execute the API call and get response
            JsonPath responseJsonPath = upiPspPaymentAPI.execute().jsonPath();
            
            // Extract externalSerialNo from the response
            String externalSerialNo = responseJsonPath.getString("body.externalSerialNo");
            
            // Validate the response using soft assertions
            SoftAssertions softly2 = new SoftAssertions();
            softly2.assertThat(responseJsonPath.getString("body.resultCode")).as("API response should return SUCCESS").isEqualTo("SUCCESS");
            softly2.assertThat(responseJsonPath.getString("body.orderId")).as("Order ID should match transaction reference").isEqualTo(transactionRef);
            softly2.assertThat(responseJsonPath.getString("body.txnAmount")).as("Transaction amount should match deeplink amount").isEqualTo(amountFromDeeplink);
            softly2.assertAll();

            // Execute UPI Secure Response API using the externalSerialNo from UPI PSP response
            InstaproxyPtybliResponse upiSecureResponseAPI = (InstaproxyPtybliResponse) new InstaproxyPtybliResponse(
                amountFromDeeplink, // amount from deeplink
                externalSerialNo, // externalSerialNo from UPI PSP response
                "27850124741", // bankRrn
                "UPI_SAVINGS" // paymentInstrument
            )
            .setContext("body.payeeVpa", payeeVpa)
            .deleteContext("body.paymentInstrument")
            .deleteContext("body.creditCardInfo");

            // Execute the secure response API call
            JsonPath secureResponseJsonPath = upiSecureResponseAPI.execute().jsonPath();

            // Validate the secure response using soft assertions
            SoftAssertions softlySecure5 = new SoftAssertions();
            softlySecure5.assertThat(secureResponseJsonPath.getString("body.resultCodeId")).as("Secure result code id").contains("001");
            softlySecure5.assertThat(secureResponseJsonPath.getString("body.resultCode")).as("Secure result code").contains("SUCCESS");
            softlySecure5.assertAll();

            // Check theia_facade log for ROUTER_CONSULT API response using LogsValidationHelper
            String theiaFacadeLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderId, "ROUTER_CONSULT", "RESPONSE");
            
            // Check theia_facade log for ACQUIRING_INQUIRE_WITH_ACQ_ID API response using soft assertion
            String acquiringLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderId, "ACQUIRING_INQUIRE_WITH_ACQ_ID", "RESPONSE");
            
            SoftAssertions softly = new SoftAssertions();
            softly.assertThat(theiaFacadeLogs)
                    .as("ROUTER_CONSULT response should contain selectedChannel PTYBLI")
                    .contains("\"selectedChannel\":\"PTYBLI\"");
            softly.assertThat(theiaFacadeLogs)
                    .as("ROUTER_CONSULT response should contain reqAuthSupported true")
                    .contains("\"reqAuthSupported\":\"true\"");
            softly.assertThat(acquiringLogs)
                    .as("ACQUIRING_INQUIRE_WITH_ACQ_ID response should contain acquirementStatus INIT")
                    .contains("\"acquirementStatus\":\"INIT\"");
            softly.assertAll();

        }


        @Owner(Constants.Owner.LOKESH_SAXENA)
        @Feature("PGP-60677")
        @Test(description = "Verify ReqAuth flow txn for subscriptions")
        public void VerfyReqAuthTxnForSubscriptions() throws Exception{
            // Use the merchant type that corresponds to the merchant ID from curl
            // AI-Generated: 2025-01-02 - Refactoring: Updated to use all caps MID constant
            Constants.MerchantType merchant = Constants.MerchantType.REQAUTH_FLOW_UPI_SUBSCRIPTION_MID;
            
            // Generate dynamic values using helpers from codebase
            String orderId = "test" + CommonHelpers.generateOrderId();
            String custId = "Test" + CommonHelpers.generateOrderId();
            
            // Use the Default method and set fields to match curl
            BaseApi subscriptionCreateRequest = SubscriptionCreate.Default(
                merchant.getId(), 
                merchant.getKey(), 
                orderId, 
                "", // ssoToken - empty for this test
                "2" // txnAmt - using a small amount for subscription
            )
            .setContext("body.subscriptionPaymentMode", "UPI")
            .setContext("body.requestType", "")
            .setContext("body.subscriptionRetryCount", "0");
            
            // Execute the subscription create request
            InitTxnResponseDTO initTxnResponse = subscriptionCreateRequest.execute().as(InitTxnResponseDTO.class);
            
            String txnToken = initTxnResponse.getBody().getTxnToken();
     
            // Create Process Transaction Request with UPI_INTENT
            ExtendInfo extendInfo = new ExtendInfo();
            extendInfo.setAdditionalProperty("payerCmid", "8006006993");
            
            ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(merchant.getId(), txnToken, orderId)
                    .setPaymentMode("UPI_INTENT")
                    .setAuthMode("SUBS")
                    .setQrImageRequired(true)
                    .setSeqNumber("PYTM0123456")
                    .setExtendInfo(extendInfo)
                    .build();
    
            // Execute Process Transaction
            ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
            Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
                    String deeplink = processTxnV1Response.getBody().getDeepLinkInfo().getDeepLink();
        
            // Extract subscription deeplink information using QRHelper
            Map<String, String> deeplinkInfo = QRHelper.parseDeeplinkInfo(deeplink);
            String transactionRef = deeplinkInfo.get("tr");
            // Extract amount (am) from deeplink - this is the max amount for subscription
            String amountFromDeeplink = deeplinkInfo.get("amount");
            // Extract first amount (fam) from deeplink - this is the current transaction amount
            String firstAmountFromDeeplink = deeplinkInfo.get("fam");
            String payeeVpa = deeplinkInfo.get("payeeVpa");
        
            
                    // Build UPI PSP payment using StaticQrUpiPSP
            StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder("SEAMLESS_3D_FORM", merchant.getId(), transactionRef, amountFromDeeplink, "QR_SUBSCRIPTION");
            builder.setPayerVpa("paytmTest@ptys")
                   .setPayeeVpa(payeeVpa)
                   .setPayerName("test")
                   .setPayerPSP("Phonepe")
                   .setPayerPaymentInstrument("")
                   .setPayerPaymentInstrumentFee("0.00");
            StaticQrUpiPSPRequest staticQrUpiPSPRequest = builder.build();
            StaticQrUpiPSP upiPspPaymentAPI = new StaticQrUpiPSP(staticQrUpiPSPRequest);
            
            // Execute the API call and get response
            JsonPath responseJsonPath = upiPspPaymentAPI.execute().jsonPath();
            
            // Extract externalSerialNo from the response
            String externalSerialNo = responseJsonPath.getString("body.externalSerialNo");
            
            // Validate the response using soft assertions
            SoftAssertions softly3 = new SoftAssertions();
            softly3.assertThat(responseJsonPath.getString("body.resultCode")).as("API response should return SUCCESS").isEqualTo("SUCCESS");
            softly3.assertThat(responseJsonPath.getString("body.orderId")).as("Order ID should match transaction reference").isEqualTo(transactionRef);
            softly3.assertThat(responseJsonPath.getString("body.txnAmount")).as("Transaction amount should match deeplink amount").isEqualTo(amountFromDeeplink);
            softly3.assertAll();

            // Execute UPI Secure Response API using the externalSerialNo from UPI PSP response
            InstaproxyPtybliResponse upiSecureResponseAPI = (InstaproxyPtybliResponse) new InstaproxyPtybliResponse(
                amountFromDeeplink, // amount (using max amount from deeplink)
                externalSerialNo, // externalSerialNo from UPI PSP response
                "27850124741", // bankRrn
                "UPI_SAVINGS" // paymentInstrument
            )
            .setContext("body.payeeVpa", payeeVpa)
            .deleteContext("body.paymentInstrument")
            .deleteContext("body.creditCardInfo");

            // Execute the secure response API call
            JsonPath secureResponseJsonPath = upiSecureResponseAPI.execute().jsonPath();

            // Validate the secure response using soft assertions
            SoftAssertions softlySecure6 = new SoftAssertions();
            softlySecure6.assertThat(secureResponseJsonPath.getString("body.resultCodeId")).as("Secure result code id").contains("001");
            softlySecure6.assertThat(secureResponseJsonPath.getString("body.resultCode")).as("Secure result code").contains("SUCCESS");
            softlySecure6.assertAll();

            // Check theia_facade log for ROUTER_CONSULT API response using LogsValidationHelper
            String theiaFacadeLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderId, "ROUTER_CONSULT", "RESPONSE");
            
            // AI-Generated: 2025-01-02 - Feature addition: Extract PGPID from PROCESS_TXN request in THEIA_REQ_RESP and verify no ACQUIRING_PAY_ORDER logs
            String ptcLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.THEIA_REQ_RESP, orderId, "v1/processTransaction", "REQUEST");
            String pgpId = ptcLogs.substring(ptcLogs.indexOf("\"PGP_ID\": \"") + 11, ptcLogs.indexOf("\",", ptcLogs.indexOf("\"PGP_ID\": \"")));
            
            // Verify there are no ACQUIRING_PAY_ORDER logs for the extracted PGPID
            String acquiringPayOrderLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, pgpId, "ACQUIRING_PAY_ORDER", "REQUEST");
            
            SoftAssertions softly = new SoftAssertions();
            softly.assertThat(theiaFacadeLogs)
                    .as("ROUTER_CONSULT response should contain selectedChannel PTYBLI")
                    .contains("\"selectedChannel\":\"PTYBLI\"");
            softly.assertThat(theiaFacadeLogs)
                    .as("ROUTER_CONSULT response should contain reqAuthSupported true")
                    .contains("\"reqAuthSupported\":\"true\"");
            softly.assertThat(acquiringPayOrderLogs)
                    .as("Should not find any ACQUIRING_PAY_ORDER logs for PGPID from PROCESS_TXN request")
                    .isEmpty();
            softly.assertAll();

        }

        @Owner(Constants.Owner.LOKESH_SAXENA)
        @Test(description = "Vpa should be visible in get payment status when this preference and ff4j is on \"RETURN_USER_VPA_IN_RESPONSE\"")
        public void VpaShouldBeVisibleInGetPaymentStatusWhenThisPreferenceAndFf4jIsOnReturnUserVpaInResponse() throws Exception{
            // AI-Generated: 2025-01-02 - Refactoring: Updated to use all caps MID constant
            Constants.MerchantType merchant = Constants.MerchantType.REQAUTH_FLOW_UPI_CONFEE_MID;
            // Create InitTxnDTO with the provided parameters
            String custId = "Test" + CommonHelpers.generateOrderId();
            String orderId = "test" + CommonHelpers.generateOrderId();
            InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                    .setTxnValue("800")
                    .setCustId(custId)
                    .setCallbackUrl(LocalConfig.MOCK_HOST + "/mockbank" + Constants.PagePath.COMMON_RESPONSE_PAGE_PATH)
                    .setWebsiteName("retail")
                    .setOrderId(orderId)
                    .build();
            
            // Execute the initiate transaction
            InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
            
            String txnToken = initTxnResponseDTO.getBody().getTxnToken();
    
            // Create Process Transaction Request with UPI_INTENT
            ExtendInfo extendInfo = new ExtendInfo();
            extendInfo.setAdditionalProperty("payerCmid", "8006006993");
            
            ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(merchant.getId(), txnToken, orderId)
                    .setPaymentMode("UPI_INTENT")
                    .setAuthMode("USRPWD")
                    .setQrImageRequired(true)
                    .setSeqNumber("PYTM0123456")
                    .setExtendInfo(extendInfo)
                    .build();
    
            // Execute Process Transaction
            ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
            Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
                    String deeplink = processTxnV1Response.getBody().getDeepLinkInfo().getDeepLink();
            // Extract deeplink information using QRHelper
            Map<String, String> deeplinkInfo = QRHelper.parseDeeplinkInfo(deeplink);
            String transactionRef = deeplinkInfo.get("tr");
            String amountFromDeeplink = deeplinkInfo.get("amount");
            String payeeVpa = deeplinkInfo.get("payeeVpa");
            
                    // Build UPI PSP payment using StaticQrUpiPSP
            StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder("SEAMLESS_3D_FORM", merchant.getId(), transactionRef, amountFromDeeplink, "QR_PAYMENT");
            builder.setPayerVpa("paytmTest@ptys")
                   .setPayeeVpa(payeeVpa)
                   .setPayerName("test")
                   .setPayerPSP("Phonepe")
                   .setPayerPaymentInstrument("UPI_SAVINGS")
                   .setPayerPaymentInstrumentFee("0.00");
            StaticQrUpiPSPRequest staticQrUpiPSPRequest = builder.build();
            StaticQrUpiPSP upiPspPaymentAPI = new StaticQrUpiPSP(staticQrUpiPSPRequest);
            
            // Execute the API call and get response
            JsonPath responseJsonPath = upiPspPaymentAPI.execute().jsonPath();
            
            // Extract externalSerialNo from the response
            String externalSerialNo = responseJsonPath.getString("body.externalSerialNo");
            
            // Validate the response using soft assertions
            SoftAssertions softly1 = new SoftAssertions();
            softly1.assertThat(responseJsonPath.getString("body.resultCode")).as("API response should return SUCCESS").isEqualTo("SUCCESS");
            softly1.assertThat(responseJsonPath.getString("body.orderId")).as("Order ID should match transaction reference").isEqualTo(transactionRef);
            softly1.assertThat(responseJsonPath.getString("body.txnAmount")).as("Transaction amount should match deeplink amount").isEqualTo(amountFromDeeplink);
            softly1.assertAll();

            // Execute UPI Secure Response API using the externalSerialNo from UPI PSP response
            InstaproxyPtybliResponse upiSecureResponseAPI = (InstaproxyPtybliResponse) new InstaproxyPtybliResponse(
                amountFromDeeplink, // amount from deeplink
                externalSerialNo, // externalSerialNo from UPI PSP response
                "27850124741", // bankRrn
                "UPI_SAVINGS" // paymentInstrument
            )
            .setContext("body.payeeVpa", payeeVpa)
            .deleteContext("body.paymentInstrument")
            .deleteContext("body.creditCardInfo");

            // Execute the secure response API call
            JsonPath secureResponseJsonPath = upiSecureResponseAPI.execute().jsonPath();

            // Validate the secure response using soft assertions
            SoftAssertions softlySecure = new SoftAssertions();
            softlySecure.assertThat(secureResponseJsonPath.getString("body.resultCodeId")).as("Secure result code id").contains("001");
            softlySecure.assertThat(secureResponseJsonPath.getString("body.resultCode")).as("Secure result code").contains("SUCCESS");
            softlySecure.assertAll();

            GetPaymentStatus getPaymentStatus = new GetPaymentStatus().buildWithParameters(
                merchant.getId(),
                orderId);
                JsonPath paymentStatusJsonPath = getPaymentStatus.executeUntilExpectedConditionMet(
                "body.resultInfo.resultStatus", "TXN_SUCCESS", 5, 12).jsonPath();
        
                // Validate payment status
            SoftAssertions paymentAssertions = new SoftAssertions();
            paymentAssertions.assertThat(paymentStatusJsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("TXN_SUCCESS");
            paymentAssertions.assertThat(paymentStatusJsonPath.getString("body.vpa")).as("VPA in response").isEqualTo("paytmTest@ptys");
            paymentAssertions.assertAll();
            
        }

        @Owner(Constants.Owner.LOKESH_SAXENA)
        @Test(description = "UPI TPV success txn when payer account details are going in encrypted form in encrypted params")
        public void UPITPVSuccessTxnWhenPayerAccountDetailsAreGoingInEncryptedFormInEncryptedParams() throws Exception{
            Constants.MerchantType merchant = Constants.MerchantType.EMI_REG_FPO_UPI_CC_LITE;
            String custId = "1000036031";
            String orderId = "test" + CommonHelpers.generateOrderId();
            
            String encryptedParams = "eyJraWQiOiJLUF8xIiwiZW5jIjoiQTI1NkdDTSIsImFsZyI6IlJTQS1PQUVQLTI1NiJ9.JP-LaAWTwVv8ZYZ_nrMJWom07SbZyPJjUJsx2JptE2ipUxcRkbA-R24XCygaZBdSUQqvizD3J3z4ehmlyQ0RzJAIm2I4QrZTAQq2ILYuhar4iq8txuFm0Tg7WjlwWwRkcF37X_BTKtkgjHSKzam3IruXHOlkrx08WFfZkxggauBzyXyx8IRPmUiUOQF30vMG0zJhLfBy2oDzAp0mKRHVOj0s97TuEjw3-3bkfJfm6TlQg4oHHpfu7GyfLbAnf8LeB2LxOIyTrMVnOWbbnxiiNtu4xVsoOMIFQ46AVuIllMwHsakqjjU8vsCal2QUS2xbhJ20YMzBk-SOyPG09wbBjA.qci2XA31ZsywzMNL.LVvP4ALRzAe0KX7_KALYSm1YxMM8XCMkskiDDrIVRXV7A-kv1WSNI9MTp4yoc93xmXnojG2mJhOKf3R9cE7OKf1Go7tft5pljiC7kA4Vex0oYianKnaH0hJ_4fXkoKNBgmY2kunLYW5OzsGJoscfRDQHId6AyMOgUxGnaLCD1ep81rtc_mnc1BHwE7PJChBmcjaujMno4VjCBkG8y2TQgWBnjlS31Hdk_orwInMBK9tO5pLUpklQLYr8uspOgM4A567bqC3MlJQ6epkypmQjQ-RGiWy9KTCwSgNB1XzmkzsQ61dph8fw-5BQ-xNVqWD22AbH2-cukhAwGrc2A6XYcMAoyZiMqw1MlEHH.NzWvG0ytuYXh_HiaDuLG-w";
            String encKeyId = "KP_1";
            
            InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                    .setTxnValue("20")
                    .setCustId(custId)
                    .setCallbackUrl(LocalConfig.MOCK_HOST + "/mockbank" + Constants.PagePath.COMMON_RESPONSE_PAGE_PATH)
                    .setWebsiteName("retail")
                    .setOrderId(orderId)
                    .setValidateAccountNumber("true")
                    .setAllowUnverifiedAccount("false")
                    .setEncryptedParams(encryptedParams)
                    .setEncKeyId(encKeyId)
                    .build();
            
            // Execute the initiate transaction
            InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
            
            String txnToken = initTxnResponseDTO.getBody().getTxnToken();
    
            // Create Process Transaction Request with UPI_INTENT
            ExtendInfo extendInfo = new ExtendInfo();
            extendInfo.setAdditionalProperty("payerCmid", "8006006993");
            
            ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(merchant.getId(), txnToken, orderId)
                    .setPaymentMode("UPI_INTENT")
                    .setAuthMode("USRPWD")
                    .setQrImageRequired(false)
                    .setSeqNumber("PYTM0123456")
                    .setExtendInfo(extendInfo)
                    .build();
    
            // Execute Process Transaction
            ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
            Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
                    String deeplink = processTxnV1Response.getBody().getDeepLinkInfo().getDeepLink();
            // Extract deeplink information using QRHelper
            Map<String, String> deeplinkInfo = QRHelper.parseDeeplinkInfo(deeplink);
            String transactionRef = deeplinkInfo.get("tr");
            String amountFromDeeplink = deeplinkInfo.get("amount");
            String payeeVpa = deeplinkInfo.get("payeeVpa");
            
            // Execute PTYBLIIntentCallback to complete the UPI Intent transaction
            PTYBLIIntentCallback ptybliIntentCallback = new PTYBLIIntentCallback();
            ptybliIntentCallback.buildRequest("Abhishek Verma", LocalConfig.PGP_HOST, payeeVpa, transactionRef, "payervpa@test");
            JsonPath ptybliCallbackResponse = ptybliIntentCallback.execute().jsonPath();
            
            // Validate callback response
            SoftAssertions softly1 = new SoftAssertions();
            softly1.assertThat(ptybliCallbackResponse.getString("body.resultCode")).as("Callback result code should be SUCCESS").isEqualToIgnoringCase("SUCCESS");
            softly1.assertAll();

            // Verify instaproxy logs: PTYBL_UPI/upi/order-details request must contain payerAccountDetails and isTxnAllowed (both non-null and non-empty)
            String instaproxyLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy, orderId, "order-details");
            SoftAssertions logAssertions = new SoftAssertions();
            logAssertions.assertThat(instaproxyLogs).as("Instaproxy order-details log must contain payerAccountDetails").contains("payerAccountDetails");
            logAssertions.assertThat(instaproxyLogs).as("payerAccountDetails must not be null in order-details request").doesNotContain("\"payerAccountDetails\":null");
            logAssertions.assertThat(instaproxyLogs).as("payerAccountDetails must not be empty in order-details request").doesNotContain("\"payerAccountDetails\":[]");
            logAssertions.assertThat(instaproxyLogs).as("Instaproxy order-details log must contain isTxnAllowed").contains("isTxnAllowed");
            logAssertions.assertThat(instaproxyLogs).as("isTxnAllowed must not be null in order-details request").doesNotContain("\"isTxnAllowed\":null");
            logAssertions.assertThat(instaproxyLogs).as("isTxnAllowed must not be empty in order-details request").doesNotContain("\"isTxnAllowed\":\"\"");
            logAssertions.assertAll();

            GetPaymentStatus getPaymentStatus = new GetPaymentStatus().buildWithParameters(
                merchant.getId(),
                orderId);
                JsonPath paymentStatusJsonPath = getPaymentStatus.executeUntilExpectedConditionMet(
                "body.resultInfo.resultStatus", "TXN_SUCCESS", 5, 12).jsonPath();
        
                // Validate payment status
            SoftAssertions paymentAssertions = new SoftAssertions();
            paymentAssertions.assertThat(paymentStatusJsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("TXN_SUCCESS");
            paymentAssertions.assertAll();
            
        }

        @Owner(Constants.Owner.LOKESH_SAXENA)
        @Test(description = "UPI TPV name mismatch error")
        public void UPITPVNameMismatchError() throws Exception{
            Constants.MerchantType merchant = Constants.MerchantType.EMI_REG_FPO_UPI_CC_LITE;
            String custId = "1000036031";
            String orderId = "test" + CommonHelpers.generateOrderId();
            
            String encryptedParams = "eyJraWQiOiJLUF8xIiwiZW5jIjoiQTI1NkdDTSIsImFsZyI6IlJTQS1PQUVQLTI1NiJ9.JP-LaAWTwVv8ZYZ_nrMJWom07SbZyPJjUJsx2JptE2ipUxcRkbA-R24XCygaZBdSUQqvizD3J3z4ehmlyQ0RzJAIm2I4QrZTAQq2ILYuhar4iq8txuFm0Tg7WjlwWwRkcF37X_BTKtkgjHSKzam3IruXHOlkrx08WFfZkxggauBzyXyx8IRPmUiUOQF30vMG0zJhLfBy2oDzAp0mKRHVOj0s97TuEjw3-3bkfJfm6TlQg4oHHpfu7GyfLbAnf8LeB2LxOIyTrMVnOWbbnxiiNtu4xVsoOMIFQ46AVuIllMwHsakqjjU8vsCal2QUS2xbhJ20YMzBk-SOyPG09wbBjA.qci2XA31ZsywzMNL.LVvP4ALRzAe0KX7_KALYSm1YxMM8XCMkskiDDrIVRXV7A-kv1WSNI9MTp4yoc93xmXnojG2mJhOKf3R9cE7OKf1Go7tft5pljiC7kA4Vex0oYianKnaH0hJ_4fXkoKNBgmY2kunLYW5OzsGJoscfRDQHId6AyMOgUxGnaLCD1ep81rtc_mnc1BHwE7PJChBmcjaujMno4VjCBkG8y2TQgWBnjlS31Hdk_orwInMBK9tO5pLUpklQLYr8uspOgM4A567bqC3MlJQ6epkypmQjQ-RGiWy9KTCwSgNB1XzmkzsQ61dph8fw-5BQ-xNVqWD22AbH2-cukhAwGrc2A6XYcMAoyZiMqw1MlEHH.NzWvG0ytuYXh_HiaDuLG-w";
            String encKeyId = "KP_1";
            
            InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                    .setTxnValue("20.36")
                    .setCustId(custId)
                    .setCallbackUrl(LocalConfig.MOCK_HOST + "/mockbank" + Constants.PagePath.COMMON_RESPONSE_PAGE_PATH)
                    .setWebsiteName("retail")
                    .setOrderId(orderId)
                    .setValidateAccountNumber("true")
                    .setAllowUnverifiedAccount("false")
                    .setEncryptedParams(encryptedParams)
                    .setEncKeyId(encKeyId)
                    .build();
            
            // Execute the initiate transaction
            InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
            
            String txnToken = initTxnResponseDTO.getBody().getTxnToken();
    
            // Create Process Transaction Request with UPI_INTENT
            ExtendInfo extendInfo = new ExtendInfo();
            extendInfo.setAdditionalProperty("payerCmid", "8006006993");
            
            ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(merchant.getId(), txnToken, orderId)
                    .setPaymentMode("UPI_INTENT")
                    .setAuthMode("USRPWD")
                    .setQrImageRequired(false)
                    .setSeqNumber("PYTM0123456")
                    .setExtendInfo(extendInfo)
                    .build();
    
            // Execute Process Transaction
            ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
            Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
                    String deeplink = processTxnV1Response.getBody().getDeepLinkInfo().getDeepLink();
            // Extract deeplink information using QRHelper
            Map<String, String> deeplinkInfo = QRHelper.parseDeeplinkInfo(deeplink);
            String transactionRef = deeplinkInfo.get("tr");
            String amountFromDeeplink = deeplinkInfo.get("amount");
            String payeeVpa = deeplinkInfo.get("payeeVpa");
            
            // Execute PTYBLIIntentCallback to complete the UPI Intent transaction
            PTYBLIIntentCallback ptybliIntentCallback = new PTYBLIIntentCallback();
            ptybliIntentCallback.buildRequest("Abhishek Verma", LocalConfig.PGP_HOST, payeeVpa, transactionRef, "payervpa@test");
            JsonPath ptybliCallbackResponse = ptybliIntentCallback.execute().jsonPath();
            
            // Validate callback response
            SoftAssertions softly1 = new SoftAssertions();
            softly1.assertThat(ptybliCallbackResponse.getString("body.resultCode")).as("Callback result code should be SUCCESS").isEqualToIgnoringCase("SUCCESS");
            softly1.assertAll();

            // Verify instaproxy logs: PTYBL_UPI/upi/order-details request must contain payerAccountDetails and isTxnAllowed (both non-null and non-empty)
            String instaproxyLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy, orderId, "order-details");
            SoftAssertions logAssertions = new SoftAssertions();
            logAssertions.assertThat(instaproxyLogs).as("Instaproxy order-details log must contain payerAccountDetails").contains("payerAccountDetails");
            logAssertions.assertThat(instaproxyLogs).as("payerAccountDetails must not be null in order-details request").doesNotContain("\"payerAccountDetails\":null");
            logAssertions.assertThat(instaproxyLogs).as("payerAccountDetails must not be empty in order-details request").doesNotContain("\"payerAccountDetails\":[]");
            logAssertions.assertThat(instaproxyLogs).as("Instaproxy order-details log must contain isTxnAllowed").contains("isTxnAllowed");
            logAssertions.assertThat(instaproxyLogs).as("isTxnAllowed must not be null in order-details request").doesNotContain("\"isTxnAllowed\":null");
            logAssertions.assertThat(instaproxyLogs).as("isTxnAllowed must not be empty in order-details request").doesNotContain("\"isTxnAllowed\":\"\"");
            logAssertions.assertAll();

            // Verify FLUXNET_UPI_PG2_PAYMENT_RESULT log contains result code FGW_TPV_NAME_MISMATCH
            String extSnLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy, orderId, "ExtSN=");
            String extSnValue = extSnLogs.substring(extSnLogs.indexOf("ExtSN="), extSnLogs.indexOf(", OrderId=")).replace("ExtSN=", "");
            String fluxnetPaymentResultLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy, extSnValue, "FLUXNET_UPI_PG2_PAYMENT_RESULT");
            Assertions.assertThat(fluxnetPaymentResultLogs).as("FLUXNET_UPI_PG2_PAYMENT_RESULT log must contain result code FGW_TPV_NAME_MISMATCH").contains("FGW_TPV_NAME_MISMATCH");
        }

        @Owner(Constants.Owner.LOKESH_SAXENA)
        @Test(description = "Payer IfSC and Payer Account Should Come In FluxnetPG2PaymentRequest")
        public void PayerIfSCAndPayerAccountShouldComeInFluxnetPG2PaymentRequest() throws Exception{
            // AI-Generated: 2025-01-02 - Refactoring: Updated to use all caps MID constant
            Constants.MerchantType merchant = Constants.MerchantType.REQAUTH_FLOW_UPI_CONFEE_MID;
            // Create InitTxnDTO with the provided parameters
            String custId = "Test" + CommonHelpers.generateOrderId();
            String orderId = "test" + CommonHelpers.generateOrderId();
            InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                    .setTxnValue("800")
                    .setCustId(custId)
                    .setCallbackUrl(LocalConfig.MOCK_HOST + "/mockbank" + Constants.PagePath.COMMON_RESPONSE_PAGE_PATH)
                    .setWebsiteName("retail")
                    .setOrderId(orderId)
                    .build();
            
            // Execute the initiate transaction
            InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
            
            String txnToken = initTxnResponseDTO.getBody().getTxnToken();
    
            // Create Process Transaction Request with UPI_INTENT
            ExtendInfo extendInfo = new ExtendInfo();
            extendInfo.setAdditionalProperty("payerCmid", "8006006993");
            
            ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(merchant.getId(), txnToken, orderId)
                    .setPaymentMode("UPI_INTENT")
                    .setAuthMode("USRPWD")
                    .setQrImageRequired(false)
                    .setSeqNumber("PYTM0123456")
                    .setExtendInfo(extendInfo)
                    .build();
    
            // Execute Process Transaction
            ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
            Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
                    String deeplink = processTxnV1Response.getBody().getDeepLinkInfo().getDeepLink();
            // Extract deeplink information using QRHelper
            Map<String, String> deeplinkInfo = QRHelper.parseDeeplinkInfo(deeplink);
            String transactionRef = deeplinkInfo.get("tr");
            String amountFromDeeplink = deeplinkInfo.get("amount");
            String payeeVpa = deeplinkInfo.get("payeeVpa");
            
            // Execute Instaproxy PTYBLI secure response to complete the UPI Intent transaction
            InstaproxyPtybliResponse upiSecureResponseAPI = (InstaproxyPtybliResponse) new InstaproxyPtybliResponse(
                    amountFromDeeplink,
                    transactionRef,
                    "442189799230",
                    "UPI_SAVINGS"
            )
            .setContext("body.payeeVpa", payeeVpa)
            .setContext("body.payerVpa", "payervpa@test")
            .setContext("body.payerName", "Abhishek Verma")
            .setContext("body.payerMaskedAccount", "72382899292903353")
            .deleteContext("body.paymentInstrument")
            .deleteContext("body.creditCardInfo");

            JsonPath ptybliCallbackResponse = upiSecureResponseAPI.execute().jsonPath();
            
            // Validate callback response
            SoftAssertions softly1 = new SoftAssertions();
            softly1.assertThat(ptybliCallbackResponse.getString("body.resultCodeId")).as("Secure result code id").contains("001");
            softly1.assertThat(ptybliCallbackResponse.getString("body.resultCode")).as("Callback result code should be SUCCESS").isEqualToIgnoringCase("SUCCESS");
            softly1.assertAll();

            String fluxnetPaymentResultLogs = LogsValidationHelper.verifyLogsOnPod(
                    PG2LogsValidationHelper.setEnvService.instaproxy, transactionRef, "FLUXNET_UPI_PG2_PAYMENT_RESULT");
            SoftAssertions fluxnetAssertions = new SoftAssertions();
            fluxnetAssertions.assertThat(fluxnetPaymentResultLogs)
                    .as("FLUXNET_UPI_PG2_PAYMENT_RESULT log must contain upiAdditionalData")
                    .contains("upiAdditionalData");

            String upiAdditionalDataKey = "\"upiAdditionalData\":\"";
            int upiAdditionalDataStart = fluxnetPaymentResultLogs.indexOf(upiAdditionalDataKey);
            fluxnetAssertions.assertThat(upiAdditionalDataStart)
                    .as("upiAdditionalData field must be present in FLUXNET_UPI_PG2_PAYMENT_RESULT log")
                    .isGreaterThan(-1);

            String upiAdditionalDataBase64 = fluxnetPaymentResultLogs.substring(
                    upiAdditionalDataStart + upiAdditionalDataKey.length(),
                    fluxnetPaymentResultLogs.indexOf("\"", upiAdditionalDataStart + upiAdditionalDataKey.length()));
            JSONObject upiAdditionalDataJson = new JSONObject(PGPHelpers.Base64Decode(upiAdditionalDataBase64));
            fluxnetAssertions.assertThat(upiAdditionalDataJson.optString("payerAccount", null))
                    .as("Decoded upiAdditionalData must contain payerAccount")
                    .isEqualTo("72382899292903353");
            fluxnetAssertions.assertThat(upiAdditionalDataJson.optString("payerIfsc", null))
                    .as("Decoded upiAdditionalData must contain payerIfsc")
                    .isEqualTo("PUNB");
            fluxnetAssertions.assertAll();

            GetPaymentStatus getPaymentStatus = new GetPaymentStatus().buildWithParameters(
                merchant.getId(),
                orderId);
                JsonPath paymentStatusJsonPath = getPaymentStatus.executeUntilExpectedConditionMet(
                "body.resultInfo.resultStatus", "TXN_SUCCESS", 5, 12).jsonPath();
        
                // Validate payment status
            SoftAssertions paymentAssertions = new SoftAssertions();
            paymentAssertions.assertThat(paymentStatusJsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("TXN_SUCCESS");
            paymentAssertions.assertAll();
            
        }
        
}
