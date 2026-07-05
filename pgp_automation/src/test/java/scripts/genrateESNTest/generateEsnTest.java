package scripts.genrateESNTest;

import com.paytm.api.MappingApisPG2;
import com.paytm.api.MappingService.GetMerchantPreferenceInfoExt;
import com.paytm.api.PG2MappingApisHelper;
import com.paytm.api.generateESN.generateEsn;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.*;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.response.FetchPaymentOptResponseDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.dto.processTransactionV1.response.ProcessTxnV1Response;
import com.paytm.framework.reporting.Reporter;
import com.paytm.utils.merchant.util.PayMethodType;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.luaj.vm2.ast.Str;
import org.testng.Assert;
import org.testng.annotations.Test;
import scripts.api.UPI.UpiTransactionTests;

import java.util.Map;
import java.util.Optional;

import static io.restassured.RestAssured.given;

public class generateEsnTest extends PGPBaseTest {
    generateEsnHelper generateEsnHelper=new generateEsnHelper();
    UpiTransactionTests upiTransactionTests=new UpiTransactionTests();


        @Owner("Manish")
        @Feature("PGP-44577")
        @Test(description = "Verify successfully generate newEsn in bank form optimize flow")
        public void newESN_BankFormOptimizedFlow() throws Exception {
            Constants.MerchantType merchant = Constants.MerchantType.UPI_INTENT_RETRY_OPTIMISE;
            InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                    .setRequestType("NATIVE")
                    .setTxnValue("2.0")
                    .build();
            String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
            FetchPaymentOptResponseDTO fetchPaymentOptResponseDTO =
                    NativeHelpers.fetchPaymentOptionResponse(txnToken, merchant.getId(), initTxnDTO.orderFromBody());
            Assertions.assertThat(NativeHelpers.isFetchPaymentOptionStatusMatched(fetchPaymentOptResponseDTO, PayMethodType.UPI.toString(), false))
                    .as(PayMethodType.UPI.toString() + " paymethod status mismatched")
                    .isTrue();
            ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                    .Builder(merchant.getId(), txnToken, initTxnDTO.orderFromBody())
                    .setPaymentMode("UPI_INTENT")
                    .build();
            ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
            Map<String, String> map = PGPHelpers.parseUpiIntentDeepLink(processTxnV1Response.getBody().getDeepLinkInfo().getDeepLink());
            //System.out.println("extNo is: "+map.get("tr"));
            generateEsn generateEsn=new generateEsn();
            JsonPath generateEsnResponse = given().spec(generateEsn.reqSpec(map.get("tr"),"false")).post().jsonPath();
            String logs=LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,generateEsnHelper.getNewESNFromAPI(generateEsnResponse),"COMMON_STORAGE_FETCH_FORM");
            Assert.assertTrue(logs.contains(generateEsnHelper.getNewESNFromAPI(generateEsnResponse)));

            // Verify insta logs
           String InstLogs=LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,map.get("tr"),"payload");
            System.out.println("Insta Logs: "+InstLogs);
            Assert.assertTrue(InstLogs.contains("\"isRetryable\":\"true\""));
            Assert.assertTrue(InstLogs.contains("\"maxRetries\":\"3\""));

        }

    @Owner("Manish")
    @Feature("PGP-44577")
    @Test(description = "Verify successfully generate newEsn in non bankFormOptimized flow")
    public void newESN_NormaTxnFlow() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.UPI_INTENT_RETRY;
        User user = userManager.getForRead(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setRequestType("NATIVE")
                .setTxnValue("2.0")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptResponseDTO fetchPaymentOptResponseDTO =
                NativeHelpers.fetchPaymentOptionResponse(txnToken, merchant.getId(), initTxnDTO.orderFromBody());
        Assertions.assertThat(NativeHelpers.isFetchPaymentOptionStatusMatched(fetchPaymentOptResponseDTO, PayMethodType.UPI.toString(), false))
                .as(PayMethodType.UPI + " paymethod status mismatched")
                .isTrue();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchant.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("UPI_INTENT")
                .build();
        ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Map<String, String> map = PGPHelpers.parseUpiIntentDeepLink(processTxnV1Response.getBody().getDeepLinkInfo().getDeepLink());
        generateEsn generateEsn=new generateEsn();
        JsonPath generateEsnResponse = given().spec(generateEsn.reqSpec(map.get("tr"),"false")).post().jsonPath();
        String logs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,generateEsnHelper.getNewESNFromAPI(generateEsnResponse),"PAYMENT_BIZ_PAY_RESULT_QUERY");
        Assertions.assertThat(logs.contains(generateEsnHelper.getNewESNFromAPI(generateEsnResponse))).isEqualTo(true);

        // Verify insta logs
        String InstLogs=LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,map.get("tr"),"payload");
        System.out.println("Insta Logs: "+InstLogs);
        Assert.assertTrue(InstLogs.contains("\"isRetryable\":\"true\""));
        Assert.assertTrue(InstLogs.contains("\"maxRetries\":\"3\""));
    }

    @Owner("Manish")
    @Feature("PGP-44577")
    @Test(description = "Verify successfully generate newEsn in non bankFormOptimized subscription flow")
    public void newESN_Normal_NativeSubsFlow() throws Exception {

        Constants.MerchantType merchant = Constants.MerchantType.UPI_INTENT_RETRY_SUBS;

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", merchant)
                .setTxnValue("10")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("100")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);

        String txnToken = responseDTO.getBody().getTxnToken();
        upiTransactionTests.Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                merchant.getId(),responseDTO.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),null)
                .setPaymentMode("UPI_INTENT")
                .setChannelCode("push")
                .setChannelId("WAP")
                .build();
        //NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        ProcessTxnV1Response processTxnV1Response=NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        String EsnFromDeepLink=generateEsnHelper.getSubsESNFromDeepLink(processTxnV1Response.getBody().getDeepLinkInfo().getDeepLink());

        generateEsn generateEsn=new generateEsn();
        JsonPath generateEsnResponse = given().spec(generateEsn.reqSpec(EsnFromDeepLink,"true")).post().jsonPath();
        generateEsnResponse.prettyPrint();
        String logs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,generateEsnHelper.getNewESNFromAPI(generateEsnResponse),"PAYMENT_BIZ_PAY_RESULT_QUERY");
        System.out.println("Logs for subs: "+logs);
        Assertions.assertThat(logs.contains(generateEsnHelper.getNewESNFromAPI(generateEsnResponse)));

        // Verify insta logs
        String InstLogs=LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,EsnFromDeepLink,"payload");
        System.out.println("Insta Logs: "+InstLogs);
        Assert.assertTrue(InstLogs.contains("\"isRetryable\":\"true\""));
        Assert.assertTrue(InstLogs.contains("\"maxRetries\":\"3\""));
    }

    @Owner("Manish")
    @Feature("PGP-44577")
    @Test(description = "Verify successfully generate newEsn in non bankFormOptimized flow when Pref is present in PreferenceInfo and not in PreferenceInfoExt")
    public void CheckPref_In_PreferenceInfoExt() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.UPI_INTENT_RETRY;
        User user = userManager.getForRead(Label.BASIC);
        String INTENT_RETRY_PAYTM="INTENT_RETRY_PAYTM";
        GetMerchantPreferenceInfoExt getMerchantPreferenceInfoExt =
                new GetMerchantPreferenceInfoExt(merchant.getId(), "INTENT_RETRY_PAYTM");
        Response response = getMerchantPreferenceInfoExt.execute();
        Assertions.assertThat(response.getStatusCode()).isEqualTo(200);
        Assertions.assertThat(response.jsonPath().getString("resultResp.merchantPreferenceInfos[0].prefValue")).isNull();


        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Merchant_get_preference_info(merchant.getId());
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyQueryMerchantPreferenceInfo(objectHead);
        //pg2MappingApisHelper.verifyPG2Routes(Constants.MerchantType.Mapping_PG2_AttributeWithoutPaymode.getId().toString());
        Assert.assertEquals(withDrawJson1.getString("resultInfo.messaage"), "Success");
        Assert.assertEquals(withDrawJson1.getString("resultInfo.resultStatus"), "S");
        Assert.assertEquals(withDrawJson1.getString("resultInfo.resultCode"), "00000");
        Assert.assertEquals(withDrawJson1.getString("merchantId"), merchant.getId());
        Assert.assertTrue(withDrawJson1.getString("merchantPreferenceInfos.prefType").contains("INTENT_RETRY_PAYTM"));
        Assertions.assertThat(withDrawJson1.getString("merchantPreferenceInfos.find {it.prefType == '" + INTENT_RETRY_PAYTM + "'}.prefStatus").replaceAll("\\[|\\]", ""))
                .isEqualTo("ACTIVE");
        Assertions.assertThat(withDrawJson1.getString("merchantPreferenceInfos.find {it.prefType == '" + INTENT_RETRY_PAYTM + "'}.prefValue").replaceAll("\\[|\\]", ""))
                .isEqualTo("3");

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setRequestType("NATIVE")
                .setTxnValue("2.0")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptResponseDTO fetchPaymentOptResponseDTO =
                NativeHelpers.fetchPaymentOptionResponse(txnToken, merchant.getId(), initTxnDTO.orderFromBody());
        Assertions.assertThat(NativeHelpers.isFetchPaymentOptionStatusMatched(fetchPaymentOptResponseDTO, PayMethodType.UPI.toString(), false))
                .as(PayMethodType.UPI + " paymethod status mismatched")
                .isTrue();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchant.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("UPI_INTENT")
                .build();
        ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Map<String, String> map = PGPHelpers.parseUpiIntentDeepLink(processTxnV1Response.getBody().getDeepLinkInfo().getDeepLink());
        generateEsn generateEsn=new generateEsn();
        JsonPath generateEsnResponse = given().spec(generateEsn.reqSpec(map.get("tr"),"false")).post().jsonPath();
        String logs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,generateEsnHelper.getNewESNFromAPI(generateEsnResponse),"PAYMENT_BIZ_PAY_RESULT_QUERY");
        Assertions.assertThat(logs.contains(generateEsnHelper.getNewESNFromAPI(generateEsnResponse))).isEqualTo(true);

        // Verify insta logs
        String InstLogs=LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,map.get("tr"),"payload");
        System.out.println("Insta Logs: "+InstLogs);
        Assert.assertTrue(InstLogs.contains("\"isRetryable\":\"true\""));
        Assert.assertTrue(InstLogs.contains("\"maxRetries\":\"3\""));
    }
}