package scripts.GuestCheckout;

import com.paytm.LocalConfig;
import com.paytm.api.ProcessTransactionV1;
import com.paytm.api.nativeAPI.*;
import com.paytm.api.theia.emiSubvention.ApiV1Tenure;
import com.paytm.api.theia.emiSubvention.ApiV1Validate;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.*;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.TxnAmount;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.NativeDTO.fetchBinDetails.FetchBinDetailsRequest;
import com.paytm.dto.NativeDTO.getEMIDetails.request.GetEMIDetailsRequest;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.emiSubvention.ApiV1Tenure.request.ApiV1TenureRequest;
import com.paytm.dto.emiSubvention.ApiV1Tenure.request.Filters;
import com.paytm.dto.emiSubvention.ApiV1Validate.request.ApiV1ValidateRequest;
import com.paytm.dto.emiSubvention.ApiV1Validate.request.OfferDetails;
import com.paytm.dto.emiSubvention.ApiV1Validate.request.PaymentDetails;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.dto.processTransactionV1.CardTokenInfo;
import com.paytm.dto.processTransactionV1.response.ProcessTxnV1Response;
import com.paytm.framework.reportportal.annotation.Owner;
import com.paytm.pages.*;
import com.paytm.utils.merchant.api.pgp.theia.paytm_express.GetCardToken;
import io.qameta.allure.Feature;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import java.util.HashMap;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.math.BigInteger;

import static com.paytm.utils.merchant.Constants.PGP_HOST;
import static org.hamcrest.Matchers.hasItems;


public class GuestCheckoutNative extends PGPBaseTest {

    Constants.MerchantType alternateID_offus = Constants.MerchantType.Alternate_ID_Offus;
    Constants.MerchantType alternateID_onus = Constants.MerchantType.Alternate_ID_Onus;
    private final CheckoutPage checkoutPage = new CheckoutPage();

    @Owner(Constants.Owner.ASHISH_JASWAL)
    @Feature("PGP-45606")
    @Test(description = "Verify is altTokenization parameter in fetch bin response for VISA")
    public void verifyfetchBinResponseforAltVISA() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, alternateID_offus)
                .setTxnValue("20")
                .setSsoToken(ssoToken)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchBinDetailsRequest request = new FetchBinDetailsRequest.Builder(txnToken, "489538011").build();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(request, initTxnDTO.getBody().getMid(), initTxnDTO.orderFromBody());
        JsonPath fetchBinsJson = fetchBinDetail.execute().jsonPath();
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail")).isNotNull();
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail.binTokenization")).isEqualTo("true");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail.isEligibleForCoft")).isEqualTo("true");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail.isCoftPaymentSupported")).isEqualTo("true");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail.channelName")).isEqualTo("VISA");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail.channelCode")).isEqualTo("VISA");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail.isEligibleForAltId")).isEqualTo("true");

    }


    @Owner(Constants.Owner.ASHISH_JASWAL)
    @Feature("PGP-45606")
    @Test(description = "Verify is altTokenization parameter in fetch bin response for RUPAY")
    public void verifyfetchBinResponseforAltRupay() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, alternateID_offus)
                .setTxnValue("20")
                .setSsoToken(ssoToken)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchBinDetailsRequest request = new FetchBinDetailsRequest.Builder(txnToken, "608041000").build();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(request, initTxnDTO.getBody().getMid(), initTxnDTO.orderFromBody());
        JsonPath fetchBinsJson = fetchBinDetail.execute().jsonPath();
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail")).isNotNull();
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail.binTokenization")).isEqualTo("true");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail.isEligibleForCoft")).isEqualTo("true");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail.isCoftPaymentSupported")).isEqualTo("true");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail.channelName")).isEqualTo("RUPAY");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail.channelCode")).isEqualTo("RUPAY");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail.isEligibleForAltId")).isEqualTo("true");

    }

    @Owner(Constants.Owner.ASHISH_JASWAL)
    @Feature("PGP-45606")
    @Test(description = "Verify paymodes present in LPV response for Offus flow")
    public void verifyLPVResponseforAlternateIDVISA() throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, alternateID_offus)
                .setTxnValue("20")
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        Assertions.assertThat(initTxnResponse.getBody().getTxnToken()).
                as("Txn token is not generated in initiate txn response").isNotEmpty();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(initTxnResponse.getBody().getTxnToken()).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.payment_option_facade, initTxnDTO.orderFromBody(), "CHECKOUT_LITE_PAYVIEW_CONSULT");
        Assertions.assertThat(logs).contains("CREDIT_CARD_VISA");
        Assertions.assertThat(logs).contains("DEBIT_CARD_RUPAY");

    }

    @Owner(Constants.Owner.ASHISH_JASWAL)
    @Feature("PGP-45606")
    @Test(description = "Verify paymodes present in LPV response for Onus flow")
    public void verifyLPVResponseforAlternateIDRUPAY() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, alternateID_onus)
                .setTxnValue("20")
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        Assertions.assertThat(initTxnResponse.getBody().getTxnToken()).
                as("Txn token is not generated in initiate txn response").isNotEmpty();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(initTxnResponse.getBody().getTxnToken()).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.payment_option_facade, initTxnDTO.orderFromBody(), "CHECKOUT_LITE_PAYVIEW_CONSULT");
        Assertions.assertThat(logs).contains("DEBIT_CARD_RUPAY");
        Assertions.assertThat(logs).contains("CREDIT_CARD_VISA");
    }

    @Owner(Constants.Owner.ASHISH_JASWAL)
    @Feature("PGP-45606")
    @Test(description = "Verify alternate ID successful generation for VISA scheme in Offus")
    public void verifySuccessAltIDGenerationforVISA() throws Exception {

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, alternateID_offus)
                .setTxnValue("20")
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        Assertions.assertThat(initTxnResponse.getBody().getTxnToken()).
                as("Txn token is not generated in initiate txn response").isNotEmpty();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(initTxnResponse.getBody().getTxnToken()).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.payment_option_facade, initTxnDTO.orderFromBody(), "CHECKOUT_LITE_PAYVIEW_CONSULT");
        Assertions.assertThat(logs).contains("CREDIT_CARD_VISA");
        Assertions.assertThat(logs).contains("DEBIT_CARD_RUPAY");

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchBinDetailsRequest request = new FetchBinDetailsRequest.Builder(txnToken, "489538011").build();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(request, initTxnDTO.getBody().getMid(), initTxnDTO.orderFromBody());
        JsonPath fetchBinsJson = fetchBinDetail.execute().jsonPath();
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail")).isNotNull();
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail.binTokenization")).isEqualTo("true");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail.isEligibleForCoft")).isEqualTo("true");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail.isCoftPaymentSupported")).isEqualTo("true");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail.channelName")).isEqualTo("VISA");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail.channelCode")).isEqualTo("VISA");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail.isEligibleForAltId")).isEqualTo("true");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                alternateID_offus.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("CREDIT_CARD")
                .setCardInfo("|4895380115392363|545|122024")
                .setAuthMode("otp")
                .build();

        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
        Response ptcResponse = processTransactionV1.execute();
        Assert.assertEquals((ptcResponse.jsonPath().get("body.resultInfo.resultMsg")), "Success");

        logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, initTxnDTO.orderFromBody(), "/token/gc/generateTokenData");
        System.out.println("logs: "+logs);
        Assertions.assertThat(logs).contains("encryptedCardData");
        Assertions.assertThat(logs).contains("\"cardToken\":\"4895380115392364\"");
        Assertions.assertThat(logs).contains("tavv");
        Assertions.assertThat(logs).contains("\"tokenBin\":\"489538011\"");
        Assertions.assertThat(logs).contains("\"tokenSuffix\":\"2364\"");

        logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, initTxnDTO.orderFromBody(), "ASSET_CENTER_SERVICE");
        Assertions.assertThat(logs).contains("\"assetType\":\"ALT_TOKEN\"");
        Assertions.assertThat(logs).contains("assetNo");
        Assertions.assertThat(logs).contains("cacheAssetId");
        Assertions.assertThat(logs).contains("\"assetBin\":\"489538011\"");

        logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, initTxnDTO.orderFromBody(), "ACQUIRING_PAY_ORDER");
        // Assertions.assertThat(logs).contains("\"transactionVia\":\"ALT_ID\"");
        // Assertions.assertThat(logs).contains("\"cardBin\":\"489538\"");
        logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, initTxnDTO.orderFromBody(), "ACQUIRING_PAY_ORDER");
        if (!logs.contains("\"transactionVia\":\"ALT_ID\"")) {
            logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, initTxnDTO.orderFromBody(), "ACQUIRING_CREATE_ORDER_AND_PAY");
        }
        if (!logs.contains("\"transactionVia\":\"ALT_ID\"")) {
            logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, initTxnDTO.orderFromBody());
        }
        Assertions.assertThat(logs).as("theia_facade logs for order %s (ALT_ID / cardBin)", initTxnDTO.orderFromBody())
                .isNotEmpty()
                .contains("\"transactionVia\":\"ALT_ID\"")
                .contains("\"cardBin\":\"489538\"");

    }

    @Owner(Constants.Owner.ASHISH_JASWAL)
    @Feature("PGP-45606")
    @Test(description = "Verify alternate ID successful generation for VISA in Onus")
    public void verifySuccessTxnforAlternateIDVISA() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, alternateID_onus)
                .setTxnValue("20")
                .setSsoToken(ssoToken)
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        Assertions.assertThat(initTxnResponse.getBody().getTxnToken()).
                as("Txn token is not generated in initiate txn response").isNotEmpty();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(initTxnResponse.getBody().getTxnToken()).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.payment_option_facade, initTxnDTO.orderFromBody(), "CHECKOUT_LITE_PAYVIEW_CONSULT");

        Assertions.assertThat(logs).contains("CREDIT_CARD_VISA");
        Assertions.assertThat(logs).contains("DEBIT_CARD_RUPAY");

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchBinDetailsRequest request = new FetchBinDetailsRequest.Builder(txnToken, "489538011").build();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(request, initTxnDTO.getBody().getMid(), initTxnDTO.orderFromBody());
        JsonPath fetchBinsJson = fetchBinDetail.execute().jsonPath();
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail")).isNotNull();
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail.binTokenization")).isEqualTo("true");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail.isEligibleForCoft")).isEqualTo("true");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail.isCoftPaymentSupported")).isEqualTo("true");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail.channelName")).isEqualTo("VISA");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail.channelCode")).isEqualTo("VISA");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail.isEligibleForAltId")).isEqualTo("true");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                alternateID_onus.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("CREDIT_CARD")
                .setCardInfo("|4895380115392363|545|122024")
                .setAuthMode("otp")
                .build();

        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
        Response ptcResponse = processTransactionV1.execute();
        Assert.assertEquals((ptcResponse.jsonPath().get("body.resultInfo.resultMsg")), "Success");

        logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, initTxnDTO.orderFromBody(), "/token/gc/generateTokenData");
        Assertions.assertThat(logs).contains("encryptedCardData");
        Assertions.assertThat(logs).contains("\"cardToken\":\"4895380115392364\"");
        Assertions.assertThat(logs).contains("tavv");
        Assertions.assertThat(logs).contains("\"tokenBin\":\"489538011\"");
        Assertions.assertThat(logs).contains("\"tokenSuffix\":\"2364\"");

        logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, initTxnDTO.orderFromBody(), "ASSET_CENTER_SERVICE");
        Assertions.assertThat(logs).contains("\"assetType\":\"ALT_TOKEN\"");
        Assertions.assertThat(logs).contains("assetNo");
        Assertions.assertThat(logs).contains("cacheAssetId");
        Assertions.assertThat(logs).contains("\"assetBin\":\"489538011\"");

        logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, initTxnDTO.orderFromBody(), "ACQUIRING_PAY_ORDER");
        Assertions.assertThat(logs).contains("\"transactionVia\":\"ALT_ID\"");
        Assertions.assertThat(logs).contains("\"cardBin\":\"489538\"");

    }

    @Owner(Constants.Owner.ASHISH_JASWAL)
    @Feature("PGP-45606")
    @Test(description = "Verify no alternate id generation call is going in case of RUPAY card in Onus")
    public void verifyRupayCardAltGenCall() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, alternateID_onus)
                .setTxnValue("20")
                .setSsoToken(ssoToken)
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        Assertions.assertThat(initTxnResponse.getBody().getTxnToken()).
                as("Txn token is not generated in initiate txn response").isNotEmpty();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(initTxnResponse.getBody().getTxnToken()).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchBinDetailsRequest request = new FetchBinDetailsRequest.Builder(txnToken, "608041000").build();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(request, initTxnDTO.getBody().getMid(), initTxnDTO.orderFromBody());
        JsonPath fetchBinsJson = fetchBinDetail.execute().jsonPath();
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail")).isNotNull();
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail.binTokenization")).isEqualTo("true");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail.isEligibleForCoft")).isEqualTo("true");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail.isCoftPaymentSupported")).isEqualTo("true");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail.channelName")).isEqualTo("RUPAY");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail.channelCode")).isEqualTo("RUPAY");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail.isEligibleForAltId")).isEqualTo("true");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                alternateID_onus.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("DEBIT_CARD")
                .setCardInfo("|6080410000000001|123|122028")
                .setAuthMode("otp")
                .build();

        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
        Response ptcResponse = processTransactionV1.execute();
        Assert.assertEquals((ptcResponse.jsonPath().get("body.resultInfo.resultMsg")), "Success");

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, initTxnDTO.orderFromBody());
        Assertions.assertThat(logs).doesNotContain("/token/gc/generateTokenData");

        logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, initTxnDTO.orderFromBody(), "ASSET_CENTER_SERVICE");
        Assertions.assertThat(logs).contains("\"assetType\":\"ISO_CARD\"");
        Assertions.assertThat(logs).contains("\"cardBin\":\"608041000\"");
        Assertions.assertThat(logs).contains("\"assetBin\":\"608041\"");

        logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, initTxnDTO.orderFromBody(), "ACQUIRING_CREATE_ORDER_AND_PAY");
        Assertions.assertThat(logs).contains("\"transactionVia\":\"ALT_ID\"");

    }

    @Owner(Constants.Owner.ASHISH_JASWAL)
    @Feature("PGP-45606")
    @Test(description = "Verify no alternate id generation call is going in case of RUPAY card in Offus")
    public void verifyRupayCardAltGenCallOffus() throws Exception {

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, alternateID_offus)
                .setTxnValue("20")
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        Assertions.assertThat(initTxnResponse.getBody().getTxnToken()).
                as("Txn token is not generated in initiate txn response").isNotEmpty();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(initTxnResponse.getBody().getTxnToken()).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchBinDetailsRequest request = new FetchBinDetailsRequest.Builder(txnToken, "608041000").build();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(request, initTxnDTO.getBody().getMid(), initTxnDTO.orderFromBody());
        JsonPath fetchBinsJson = fetchBinDetail.execute().jsonPath();
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail")).isNotNull();
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail.binTokenization")).isEqualTo("true");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail.isEligibleForCoft")).isEqualTo("true");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail.isCoftPaymentSupported")).isEqualTo("true");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail.channelName")).isEqualTo("RUPAY");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail.channelCode")).isEqualTo("RUPAY");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail.isEligibleForAltId")).isEqualTo("true");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                alternateID_offus.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("DEBIT_CARD")
                .setCardInfo("|6080410000000001|123|122028")
                .setAuthMode("otp")
                .build();

        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
        Response ptcResponse = processTransactionV1.execute();
        Assert.assertEquals((ptcResponse.jsonPath().get("body.resultInfo.resultMsg")), "Success");

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, initTxnDTO.orderFromBody());
        Assertions.assertThat(logs).doesNotContain("/token/gc/generateTokenData");

        logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, initTxnDTO.orderFromBody(), "ASSET_CENTER_SERVICE");
        Assertions.assertThat(logs).contains("\"assetType\":\"ISO_CARD\"");
        Assertions.assertThat(logs).contains("\"cardBin\":\"608041000\"");
        Assertions.assertThat(logs).contains("\"assetBin\":\"608041\"");

        logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, initTxnDTO.orderFromBody(), "ACQUIRING_PAY_ORDER");
        Assertions.assertThat(logs).contains("\"transactionVia\":\"ALT_ID\"");

    }

    @Owner(Constants.Owner.ASHISH_JASWAL)
    @Feature("PGP-45606")
    @Test(description = "Verify alternate ID successful e-2-e txn for VISA in Onus")
    public void verifySuccessTxnE2EforAlternateIDVISA() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, alternateID_onus)
                .setTxnValue("20")
                .setSsoToken(ssoToken)
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        Assertions.assertThat(initTxnResponse.getBody().getTxnToken()).
                as("Txn token is not generated in initiate txn response").isNotEmpty();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(initTxnResponse.getBody().getTxnToken()).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchBinDetailsRequest request = new FetchBinDetailsRequest.Builder(txnToken, "489538011").build();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(request, initTxnDTO.getBody().getMid(), initTxnDTO.orderFromBody());
        JsonPath fetchBinsJson = fetchBinDetail.execute().jsonPath();
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail.isEligibleForCoft")).isEqualTo("true");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail.channelCode")).isEqualTo("VISA");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail.isEligibleForAltId")).isEqualTo("true");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(alternateID_onus.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("CREDIT_CARD")
                .setCardInfo("|4895380115392363|545|122024")
                .build();

        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
        responsePage
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateOrderId(initTxnDTO.orderFromBody())
                .validateMid(alternateID_onus.getId())
                .assertAll();

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, initTxnDTO.orderFromBody(), "/token/gc/generateTokenData");
        Assertions.assertThat(logs).contains("encryptedCardData");
        Assertions.assertThat(logs).contains("\"cardToken\":\"4895380115392364\"");
        Assertions.assertThat(logs).contains("tavv");
        Assertions.assertThat(logs).contains("\"tokenBin\":\"489538011\"");
        Assertions.assertThat(logs).contains("\"tokenSuffix\":\"2364\"");

        logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, initTxnDTO.orderFromBody(), "ASSET_CENTER_SERVICE");
        Assertions.assertThat(logs).contains("\"assetType\":\"ALT_TOKEN\"");
        Assertions.assertThat(logs).contains("assetNo");
        Assertions.assertThat(logs).contains("cacheAssetId");
        Assertions.assertThat(logs).contains("\"assetBin\":\"489538011\"");

        logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, initTxnDTO.orderFromBody(), "ACQUIRING_PAY_ORDER");
        Assertions.assertThat(logs).contains("\"transactionVia\":\"ALT_ID\"");
        Assertions.assertThat(logs).contains("\"cardBin\":\"489538\"");

        logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy, initTxnDTO.orderFromBody());
        Assertions.assertThat(logs).contains("assetType=ALT_TOKEN");
        Assertions.assertThat(logs).contains("assetNo");
        Assertions.assertThat(logs).contains("maskedAssetNo");
        Assertions.assertThat(logs).contains("01|COFT:true|ALT:true");
        Assertions.assertThat(logs).contains("Authentication & Authorization response for Enrolled Card: <result>CAPTURED</result>");
        Assertions.assertThat(logs).contains("70007981|HDFC|CC|PAYMENT|S|");

    }

    @Owner(Constants.Owner.ASHISH_JASWAL)
    @Feature("PGP-45606")
    @Test(description = "Verify alternate ID successful e-2-e txn for VISA in Offus")
    public void verifySuccessTxnE2EforAlternateIDVISAOffus() throws Exception {

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, alternateID_offus)
                .setTxnValue("20")
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        Assertions.assertThat(initTxnResponse.getBody().getTxnToken()).
                as("Txn token is not generated in initiate txn response").isNotEmpty();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(initTxnResponse.getBody().getTxnToken()).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchBinDetailsRequest request = new FetchBinDetailsRequest.Builder(txnToken, "489538011").build();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(request, initTxnDTO.getBody().getMid(), initTxnDTO.orderFromBody());
        JsonPath fetchBinsJson = fetchBinDetail.execute().jsonPath();
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail.isEligibleForCoft")).isEqualTo("true");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail.channelCode")).isEqualTo("VISA");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail.isEligibleForAltId")).isEqualTo("true");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(alternateID_offus.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("CREDIT_CARD")
                .setCardInfo("|4895380115392363|545|122024")
                .build();

        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
        responsePage
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateOrderId(initTxnDTO.orderFromBody())
                .validateMid(alternateID_offus.getId())
                .assertAll();

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, initTxnDTO.orderFromBody(), "/token/gc/generateTokenData");
        Assertions.assertThat(logs).contains("encryptedCardData");
        Assertions.assertThat(logs).contains("\"cardToken\":\"4895380115392364\"");
        Assertions.assertThat(logs).contains("tavv");
        Assertions.assertThat(logs).contains("\"tokenBin\":\"489538011\"");
        Assertions.assertThat(logs).contains("\"tokenSuffix\":\"2364\"");

        logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, initTxnDTO.orderFromBody(), "ASSET_CENTER_SERVICE");
        Assertions.assertThat(logs).contains("\"assetType\":\"ALT_TOKEN\"");
        Assertions.assertThat(logs).contains("assetNo");
        Assertions.assertThat(logs).contains("cacheAssetId");
        Assertions.assertThat(logs).contains("\"assetBin\":\"489538011\"");

        logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, initTxnDTO.orderFromBody(), "ACQUIRING_PAY_ORDER");
        Assertions.assertThat(logs).contains("\"transactionVia\":\"ALT_ID\"");
        Assertions.assertThat(logs).contains("\"cardBin\":\"489538\"");

        logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy, initTxnDTO.orderFromBody());
//        Assertions.assertThat(logs).contains("assetType=ALT_TOKEN");
//        Assertions.assertThat(logs).contains("assetNo");
//        Assertions.assertThat(logs).contains("maskedAssetNo");
        Assertions.assertThat(logs).contains("01|COFT:true|ALT:true");
        Assertions.assertThat(logs).contains("Authentication & Authorization response for Enrolled Card: <result>CAPTURED</result>");
        Assertions.assertThat(logs).contains("70007981|HDFC|CC|PAYMENT|S|");

    }

    @Owner(Constants.Owner.ASHISH_JASWAL)
    @Feature("PG2-11881")
    @Test(description = "Verify fetch bin v1 API response contains alternated ID param")
    public void verifyFetchBinV1ApiAlt() throws Exception {

        String Visa_Bin = "489538011";
        String version ="v1";

        FetchBinAlt  fetchBinAltv1 = new FetchBinAlt(Visa_Bin, version);
        Response response = fetchBinAltv1.execute();

        Assertions.assertThat(response.jsonPath().getString("bin")).isEqualTo(Visa_Bin.substring(0, 6));
        Assertions.assertThat(response.jsonPath().getString("isIndian")).isEqualTo("true");
        Assertions.assertThat(response.jsonPath().getString("bank")).isNotEmpty();
        Assertions.assertThat(response.jsonPath().getString("bankCode")).isNotEmpty();
        Assertions.assertThat(response.jsonPath().getString("displayBankName")).isNotEmpty();
        Assertions.assertThat(response.jsonPath().getString("binTokenization")).isEqualTo("true");
        Assertions.assertThat(response.jsonPath().getString("binAttributes.isEligibleForCoft")).isEqualTo("true");
        Assertions.assertThat(response.jsonPath().getString("binAttributes.IS_COFT_BIN_ELIGIBLE")).isEqualTo("true");
        Assertions.assertThat(response.jsonPath().getString("binAttributes.accountRangeCardBin")).isEqualTo(Visa_Bin);
        Assertions.assertThat(response.jsonPath().getString("binAttributes.isAltTokenizationEligible")).isEqualTo("true");

        String Rupay_Bin = "608041000";

        fetchBinAltv1 = new FetchBinAlt(Rupay_Bin, version);
        response = fetchBinAltv1.execute();

        Assertions.assertThat(response.jsonPath().getString("bin")).isEqualTo(Rupay_Bin.substring(0, 6));
        Assertions.assertThat(response.jsonPath().getString("isIndian")).isEqualTo("true");
        Assertions.assertThat(response.jsonPath().getString("bank")).isNotEmpty();
        Assertions.assertThat(response.jsonPath().getString("bankCode")).isNotEmpty();
        Assertions.assertThat(response.jsonPath().getString("displayBankName")).isNotEmpty();
        Assertions.assertThat(response.jsonPath().getString("binTokenization")).isEqualTo("true");
        Assertions.assertThat(response.jsonPath().getString("binAttributes.isEligibleForCoft")).isEqualTo("true");
        Assertions.assertThat(response.jsonPath().getString("binAttributes.IS_COFT_BIN_ELIGIBLE")).isEqualTo("true");
        Assertions.assertThat(response.jsonPath().getString("binAttributes.accountRangeCardBin")).isNotEmpty();
        Assertions.assertThat(response.jsonPath().getString("binAttributes.isAltTokenizationEligible")).isEqualTo("true");

    }

    @Owner(Constants.Owner.ASHISH_JASWAL)
    @Feature("PG2-11881")
    @Test(description = "Verify fetch bin API response contains alternated ID param")
    public void verifyFetchBinApiAlt() throws Exception {

        String bin = "489538011";
        String accountRangeCardBin= "489538011";

        FetchBinAlt  fetchBinAlt = new FetchBinAlt(bin, null);
        Response response = fetchBinAlt.execute();

        Assertions.assertThat(response.jsonPath().getString("bin")).isEqualTo(bin.substring(0,6));
        Assertions.assertThat(response.jsonPath().getString("isIndian")).isEqualTo("true");
        Assertions.assertThat(response.jsonPath().getString("bank")).isNotEmpty();
        Assertions.assertThat(response.jsonPath().getString("bankCode")).isNotEmpty();
        Assertions.assertThat(response.jsonPath().getString("displayBankName")).isNotEmpty();
        Assertions.assertThat(response.jsonPath().getString("binTokenization")).isEqualTo("true");
        Assertions.assertThat(response.jsonPath().getString("binAttributes.isEligibleForCoft")).isEqualTo("true");
        Assertions.assertThat(response.jsonPath().getString("binAttributes.IS_COFT_BIN_ELIGIBLE")).isEqualTo("true");
        Assertions.assertThat(response.jsonPath().getString("binAttributes.accountRangeCardBin")).isEqualTo(accountRangeCardBin);
        Assertions.assertThat(response.jsonPath().getString("binAttributes.isAltTokenizationEligible")).isEqualTo("true");
        String Rupay_Bin = "608041000";

        fetchBinAlt = new FetchBinAlt(Rupay_Bin, null);
        response = fetchBinAlt.execute();

        Assertions.assertThat(response.jsonPath().getString("bin")).isEqualTo(Rupay_Bin.substring(0,6));
        Assertions.assertThat(response.jsonPath().getString("isIndian")).isEqualTo("true");
        Assertions.assertThat(response.jsonPath().getString("bank")).isNotEmpty();
        Assertions.assertThat(response.jsonPath().getString("bankCode")).isNotEmpty();
        Assertions.assertThat(response.jsonPath().getString("displayBankName")).isNotEmpty();
        Assertions.assertThat(response.jsonPath().getString("binTokenization")).isEqualTo("true");
        Assertions.assertThat(response.jsonPath().getString("binAttributes.isEligibleForCoft")).isEqualTo("true");
        Assertions.assertThat(response.jsonPath().getString("binAttributes.IS_COFT_BIN_ELIGIBLE")).isEqualTo("true");
        Assertions.assertThat(response.jsonPath().getString("binAttributes.accountRangeCardBin")).isNotEmpty();
        Assertions.assertThat(response.jsonPath().getString("binAttributes.isAltTokenizationEligible")).isEqualTo("true");
    }
    @Owner(Constants.Owner.Amanpreet)
    @Feature("PGP-45606")
    @Test(description = "Verify TAVV is passed in COP directPassThroughInfo for fresh card Onus transaction")
    public void verifyTAVVforCOPinAlternateIdOnusVISA() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, alternateID_onus)
                .setTxnValue("20")
                .setSsoToken(ssoToken)
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        Assertions.assertThat(initTxnResponse.getBody().getTxnToken()).
                as("Txn token is not generated in initiate txn response").isNotEmpty();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(initTxnResponse.getBody().getTxnToken()).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchBinDetailsRequest request = new FetchBinDetailsRequest.Builder(txnToken, "489538011").build();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(request, initTxnDTO.getBody().getMid(), initTxnDTO.orderFromBody());
        JsonPath fetchBinsJson = fetchBinDetail.execute().jsonPath();
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail.isEligibleForCoft")).isEqualTo("true");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail.channelCode")).isEqualTo("VISA");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail.isEligibleForAltId")).isEqualTo("true");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(alternateID_onus.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("CREDIT_CARD")
                .setCardInfo("|4895380115392363|545|122024")
                .build();

        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
        responsePage
                .validateRespMsg("Txn Success")
                .validateOrderId(initTxnDTO.orderFromBody())
                .validateMid(alternateID_onus.getId())
                .assertAll();

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, initTxnDTO.orderFromBody(), "ACQUIRING_CREATE_ORDER_AND_PAY");
        String [] ar = logs.split("directPassThroughInfo");
        String [] ar2 = ar[1].split("\"");
        String decodedString = PGPHelpers.Base64Decode(ar2[2]);
        Assertions.assertThat(decodedString).contains("tavv");
    }
    @Owner(Constants.Owner.Amanpreet)
    @Feature("PGP-45606")
    @Test(description = "Verify TAVV is passed in PaymentCashier directPassThroughInfo for fresh card Offus transaction")
    public void verifyTAVVforPaymemtCashierinAlternateIdOffusVISA() throws Exception {

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, alternateID_offus)
                .setTxnValue("20")
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        Assertions.assertThat(initTxnResponse.getBody().getTxnToken()).
                as("Txn token is not generated in initiate txn response").isNotEmpty();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(initTxnResponse.getBody().getTxnToken()).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchBinDetailsRequest request = new FetchBinDetailsRequest.Builder(txnToken, "489538011").build();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(request, initTxnDTO.getBody().getMid(), initTxnDTO.orderFromBody());
        JsonPath fetchBinsJson = fetchBinDetail.execute().jsonPath();
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail.isEligibleForCoft")).isEqualTo("true");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail.channelCode")).isEqualTo("VISA");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail.isEligibleForAltId")).isEqualTo("true");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(alternateID_offus.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("CREDIT_CARD")
                .setCardInfo("|4895380115392363|545|122024")
                .build();

        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
        responsePage
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateOrderId(initTxnDTO.orderFromBody())
                .validateMid(alternateID_offus.getId())
                .assertAll();

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, initTxnDTO.orderFromBody(), "ACQUIRING_PAY_ORDER");
        String [] ar = logs.split("directPassThroughInfo");
        String [] ar2 = ar[1].split("\"");
        String decodedString = PGPHelpers.Base64Decode(ar2[2]);
        Assertions.assertThat(decodedString).contains("tavv");

    }
    private final NativePlusHoldpayPage nativePlusHoldpayPage = new NativePlusHoldpayPage();

    private static final String JSON_POST_URL = "/checkoutpage/new_nplus_page.jsp?env="+LocalConfig.ENV_NAME;
    private void submitProcessTxnResponseFromReq(ProcessTxnV1Request processTxnV1Request) {
        String jsonForm = QRHelper.executeProcessTransactionV1(processTxnV1Request);
        nativePlusHoldpayPage.
                launch(LocalConfig.MOCK_HOST + JSON_POST_URL).
                fillAndSubmitJsonForm(jsonForm);
    }

    @Owner(Constants.Owner.MAYURI)
    @Feature("PGP-53542")
    @Test(description = "Verify Success Txn when validate_emi having Amex card number A and PTC having Amex card number A ")
    public void verifySuccessTxnforValidateAndPTCHavingSameAmexCard() throws Exception {
        Constants.MerchantType emiMerchant = Constants.MerchantType.SIMPLIFIED_OFFERS;
        PaymentDTO paymentDTO = new PaymentDTO();
        String orderId = CommonHelpers.generateOrderId();
        User user = userManager.getForRead(Label.BASIC);
        String price ="1100";
        String subventionAmount = "1100";
        SoftAssertions softAssertions= new SoftAssertions();

        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder()
            .setMid(emiMerchant.getId())
            .setTokenType("SSO")
            .setToken(user.ssoToken())
            .setPrice(price)
            .setSubventionAmount(subventionAmount)
            .setItems(null)
            .setFilters(new Filters()
                .setBankCode("AMEX")
                .setCardType("CREDIT_CARD"))
            .build();
        ApiV1Tenure api = new ApiV1Tenure(emiMerchant.getId(), req);
        JsonPath response = api.execute().jsonPath();
        softAssertions.assertThat(response.getString("body.resultInfo.resultStatus").equalsIgnoreCase("S"));
        softAssertions.assertAll();
        String planId= response.getString("body.planDetails[0].planId");
        String pgPlanId= response.getString("body.planDetails[0].pgPlanId");
        String offerId=response.getString("body.planDetails[0].itemBreakUp[0].offerId");

        BigInteger intPlanId= new BigInteger(planId);
        ApiV1ValidateRequest req2 = new ApiV1ValidateRequest.Builder(price,subventionAmount)
                .setMid(emiMerchant.getId())
                .setMerchantKey(emiMerchant.getKey())
                .setCustomerId(orderId)
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setPlanId(intPlanId)
                .setPaymentDetails(
                        new PaymentDetails()
                                .setTotalTransactionAmount(price)
                                .setCardNumber("|"+paymentDTO.AMEX_CREDIT_CARD_NUMBER_A +"|123|07"+paymentDTO.Tokenization_Year))
                .setGenerateTokenForIntent(true)
                .setOfferDetails(new OfferDetails().setOfferId(offerId))
                .build();
        ApiV1Validate api2 = new ApiV1Validate(emiMerchant.getId(), req2);
        Response r2 = api2.execute();
        JsonPath validateResponse =  r2.then()
                .extract().jsonPath();
        softAssertions.assertThat(validateResponse.getString("body.resultInfo.resultStatus").equalsIgnoreCase("S"));
        softAssertions.assertThat(validateResponse.getString("body.planId").equalsIgnoreCase(planId));
        softAssertions.assertThat(validateResponse.getString("body.pgPlanId").equalsIgnoreCase(pgPlanId));
        softAssertions.assertThat(validateResponse.getString("body.itemBreakUpList.offerId").equalsIgnoreCase(offerId));
        softAssertions.assertAll();
        String finalTxnAmount = validateResponse.getString("body.finalTransactionAmount");
        String emiSubventionToken = validateResponse.getString("body.emiSubventionToken");

        //Initiate Txn
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),emiMerchant)
                .setTxnValue(price)
                .setPayableAmount(new TxnAmount(finalTxnAmount))
                .setOrderId(orderId)
                .setEmiSubventionToken(emiSubventionToken)
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                Constants.MerchantType.SIMPLIFIED_OFFERS.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("EMI")
                .setEmiType("CREDIT_CARD")
                .setPlanId(pgPlanId)
                .setCardInfo("|"+paymentDTO.AMEX_CREDIT_CARD_NUMBER_A +"|1234|12"+paymentDTO.Tokenization_Year)
                .setAuthMode("otp")
                .build();

        submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
        responsePage
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateOrderId(initTxnDTO.orderFromBody())
                .validateMid(Constants.MerchantType.SIMPLIFIED_OFFERS.getId())
                .assertAll();
    }

    @Owner(Constants.Owner.MAYURI)
    @Feature("PGP-53542")
    @Test(description = "Verify Success Txn when validate_emi having Amex card number A and PTC having Amex card number B ")
    public void verifySuccessTxnforValidateAndPTCHavingDifferentAmexCard() throws Exception {
        Constants.MerchantType emiMerchant = Constants.MerchantType.SIMPLIFIED_OFFERS;
        PaymentDTO paymentDTO = new PaymentDTO();
        String orderId = CommonHelpers.generateOrderId();
        User user = userManager.getForRead(Label.BASIC);
        String price ="1100";
        String subventionAmount = "1100";
        SoftAssertions softAssertions= new SoftAssertions();

        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder()
            .setMid(emiMerchant.getId())
            .setTokenType("SSO")
            .setToken(user.ssoToken())
            .setPrice(price)
            .setSubventionAmount(subventionAmount)
            .setItems(null)
            .setFilters(new Filters()
                .setBankCode("AMEX")
                .setCardType("CREDIT_CARD"))
            .build();
        ApiV1Tenure api = new ApiV1Tenure(emiMerchant.getId(), req);
        JsonPath response = api.execute().jsonPath();
        softAssertions.assertThat(response.getString("body.resultInfo.resultStatus").equalsIgnoreCase("S"));
        softAssertions.assertAll();
        String planId= response.getString("body.planDetails[0].planId");
        String pgPlanId= response.getString("body.planDetails[0].pgPlanId");
        String offerId=response.getString("body.planDetails[0].itemBreakUp[0].offerId");

        BigInteger intplanid= new BigInteger(planId);
        ApiV1ValidateRequest req2 = new ApiV1ValidateRequest.Builder(price,subventionAmount)
                .setMid(emiMerchant.getId())
                .setMerchantKey(emiMerchant.getKey())
                .setCustomerId(orderId)
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setPlanId(intplanid)
                .setPaymentDetails(
                        new PaymentDetails()
                                .setTotalTransactionAmount(price)
                                .setCardNumber("|"+paymentDTO.AMEX_CREDIT_CARD_NUMBER_A +"|123|07"+paymentDTO.Tokenization_Year))
                .setGenerateTokenForIntent(true)
                .setOfferDetails(new OfferDetails().setOfferId(offerId))
                .build();
        ApiV1Validate api2 = new ApiV1Validate(emiMerchant.getId(), req2);
        Response r2 = api2.execute();
        JsonPath validateResponse =  r2.then()
                .extract().jsonPath();
        softAssertions.assertThat(validateResponse.getString("body.resultInfo.resultStatus").equalsIgnoreCase("S"));
        softAssertions.assertThat(validateResponse.getString("body.planId").equalsIgnoreCase(planId));
        softAssertions.assertThat(validateResponse.getString("body.pgPlanId").equalsIgnoreCase(pgPlanId));
        softAssertions.assertThat(validateResponse.getString("body.itemBreakUpList.offerId").equalsIgnoreCase(offerId));
        softAssertions.assertAll();
        String finalTxnAmount = validateResponse.getString("body.finalTransactionAmount");
        String emiSubventionToken = validateResponse.getString("body.emiSubventionToken");

        //Initiate Txn
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),emiMerchant)
                .setTxnValue(price)
                .setPayableAmount(new TxnAmount(finalTxnAmount))
                .setOrderId(orderId)
                .setEmiSubventionToken(emiSubventionToken)
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
            Constants.MerchantType.SIMPLIFIED_OFFERS.getId(), txnToken, initTxnDTO.orderFromBody())
            .setPaymentMode("EMI")
            .setEmiType("CREDIT_CARD")
            .setPlanId(pgPlanId)
            .setCardInfo("|"+paymentDTO.AMEX_CREDIT_CARD_NUMBER_B+"|1234|12"+paymentDTO.Tokenization_Year)
            .setAuthMode("otp")
            .build();

        submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
        responsePage
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateOrderId(initTxnDTO.orderFromBody())
                .validateMid(Constants.MerchantType.SIMPLIFIED_OFFERS.getId())
                .assertAll();

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy, initTxnDTO.getBody().getOrderId(), "directPassThroughInfo");
        softAssertions.assertThat(SavedCardHelpers.verifyParamPresentInDirectPassthrough(logs,"tavv")).isTrue();
        softAssertions.assertAll();
    }

    @Owner(Constants.Owner.MAYURI)
    @Feature("PGP-53542")
    @Test(description = "Verify Success Txn when Validate_EMI having Amex card BIN A and PTC having Amex card number B ")
    public void verifySuccessTxnforValidateHavingCardBinAndPTCHavingDifferentAmexCard() throws Exception {
        Constants.MerchantType emiMerchant = Constants.MerchantType.SIMPLIFIED_OFFERS;
        PaymentDTO paymentDTO = new PaymentDTO();
        String orderId = CommonHelpers.generateOrderId();
        User user = userManager.getForRead(Label.BASIC);
        String price ="1100";
        String subventionAmount = "1100";
        SoftAssertions softAssertions= new SoftAssertions();

        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder()
            .setMid(emiMerchant.getId())
            .setTokenType("SSO")
            .setToken(user.ssoToken())
            .setPrice(price)
            .setSubventionAmount(subventionAmount)
            .setItems(null)
            .setFilters(new Filters()
                .setBankCode("AMEX")
                .setCardType("CREDIT_CARD"))
            .build();
        ApiV1Tenure api = new ApiV1Tenure(emiMerchant.getId(), req);
        JsonPath response = api.execute().jsonPath();
        softAssertions.assertThat(response.getString("body.resultInfo.resultStatus").equalsIgnoreCase("S"));
        softAssertions.assertAll();
        String planId= response.getString("body.planDetails[0].planId");
        String pgPlanId= response.getString("body.planDetails[0].pgPlanId");
        String offerId=response.getString("body.planDetails[0].itemBreakUp[0].offerId");


        BigInteger intplanid= new BigInteger(planId);
        ApiV1ValidateRequest req2 = new ApiV1ValidateRequest.Builder(price,subventionAmount)
                .setMid(emiMerchant.getId())
                .setMerchantKey(emiMerchant.getKey())
                .setCustomerId(orderId)
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setPlanId(intplanid)
                .setPaymentDetails(
                        new PaymentDetails()
                                .setTotalTransactionAmount(price)
                                .setCardBin(paymentDTO.AMEX_CREDIT_CARD_NUMBER_A.substring(0,6)))
                .setGenerateTokenForIntent(true)
                .setOfferDetails(new OfferDetails().setOfferId(offerId))
                .build();
        ApiV1Validate api2 = new ApiV1Validate(emiMerchant.getId(), req2);
        Response r2 = api2.execute();
        JsonPath validateResponse =  r2.then()
                .extract().jsonPath();
        softAssertions.assertThat(validateResponse.getString("body.resultInfo.resultStatus").equalsIgnoreCase("S"));
        softAssertions.assertThat(validateResponse.getString("body.planId").equalsIgnoreCase(planId));
        softAssertions.assertThat(validateResponse.getString("body.pgPlanId").equalsIgnoreCase(pgPlanId));
        softAssertions.assertThat(validateResponse.getString("body.itemBreakUpList.offerId").equalsIgnoreCase(offerId));
        softAssertions.assertAll();
        String finalTxnAmount = validateResponse.getString("body.finalTransactionAmount");
        String emiSubventionToken = validateResponse.getString("body.emiSubventionToken");

        //Initiate Txn
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),emiMerchant)
                .setTxnValue(price)
                .setPayableAmount(new TxnAmount(finalTxnAmount))
                .setOrderId(orderId)
                .setEmiSubventionToken(emiSubventionToken)
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
            Constants.MerchantType.SIMPLIFIED_OFFERS.getId(), txnToken, initTxnDTO.orderFromBody())
            .setPaymentMode("EMI")
            .setEmiType("CREDIT_CARD")
            .setPlanId(pgPlanId)
            .setCardInfo("|"+paymentDTO.AMEX_CREDIT_CARD_NUMBER_B+"|1234|12"+paymentDTO.Tokenization_Year)
            .setAuthMode("otp")
            .build();

        submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
        responsePage
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateOrderId(initTxnDTO.orderFromBody())
                .validateMid(Constants.MerchantType.SIMPLIFIED_OFFERS.getId())
                .assertAll();

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy, initTxnDTO.getBody().getOrderId(), "directPassThroughInfo");
        softAssertions = new SoftAssertions();
        softAssertions.assertThat(SavedCardHelpers.verifyParamPresentInDirectPassthrough(logs,"tavv")).isTrue();
        softAssertions.assertAll();
    }

    @Owner(Constants.Owner.MAYURI)
    @Feature("PGP-53542")
    @Test(description = "Verify Failure Txn when validate_emi having Amex card number A and PTC having ICICI card number B ")
    public void verifyFailureTxnforValidateHavingAmexAndPTCHavinICICICard() throws Exception {
        Constants.MerchantType emiMerchant = Constants.MerchantType.SIMPLIFIED_OFFERS;
        PaymentDTO paymentDTO = new PaymentDTO();
        String orderId = CommonHelpers.generateOrderId();
        User user = userManager.getForRead(Label.BASIC);
        String price ="1100";
        String subventionAmount = "1100";
        SoftAssertions softAssertions= new SoftAssertions();

        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder()
            .setMid(emiMerchant.getId())
            .setTokenType("SSO")
            .setToken(user.ssoToken())
            .setPrice(price)
            .setSubventionAmount(subventionAmount)
            .setItems(null)
            .setFilters(new Filters()
                .setBankCode("AMEX")
                .setCardType("CREDIT_CARD"))
            .build();
        ApiV1Tenure api = new ApiV1Tenure(emiMerchant.getId(), req);
        JsonPath response = api.execute().jsonPath();
        softAssertions.assertThat(response.getString("body.resultInfo.resultStatus").equalsIgnoreCase("S"));
        softAssertions.assertAll();
        String planId= response.getString("body.planDetails[0].planId");
        String pgPlanId= response.getString("body.planDetails[0].pgPlanId");
        String offerId=response.getString("body.planDetails[0].itemBreakUp[0].offerId");

        BigInteger intPlanId= new BigInteger(planId);
        ApiV1ValidateRequest req2 = new ApiV1ValidateRequest.Builder(price,subventionAmount)
                .setMid(emiMerchant.getId())
                .setMerchantKey(emiMerchant.getKey())
                .setCustomerId(orderId)
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setPlanId(intPlanId)
                .setPaymentDetails(
                        new PaymentDetails()
                                .setTotalTransactionAmount(price)
                                .setCardNumber("|"+paymentDTO.AMEX_CREDIT_CARD_NUMBER_A+"|123|07"+paymentDTO.Tokenization_Year))
                .setGenerateTokenForIntent(true)
                .setOfferDetails(new OfferDetails().setOfferId(offerId))
                .build();
        ApiV1Validate api2 = new ApiV1Validate(emiMerchant.getId(), req2);
        Response r2 = api2.execute();
        JsonPath validateResponse =  r2.then()
                .extract().jsonPath();
        softAssertions.assertThat(validateResponse.getString("body.resultInfo.resultStatus").equalsIgnoreCase("S"));
        softAssertions.assertThat(validateResponse.getString("body.planId").equalsIgnoreCase(planId));
        softAssertions.assertThat(validateResponse.getString("body.pgPlanId").equalsIgnoreCase(pgPlanId));
        softAssertions.assertThat(validateResponse.getString("body.itemBreakUpList.offerId").equalsIgnoreCase(offerId));
        softAssertions.assertAll();
        String finalTxnAmount = validateResponse.getString("body.finalTransactionAmount");
        String emiSubventionToken = validateResponse.getString("body.emiSubventionToken");

        //Initiate Txn
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),emiMerchant)
                .setTxnValue(price)
                .setPayableAmount(new TxnAmount(finalTxnAmount))
                .setOrderId(orderId)
                .setEmiSubventionToken(emiSubventionToken)
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
            Constants.MerchantType.SIMPLIFIED_OFFERS.getId(), txnToken, initTxnDTO.orderFromBody())
            .setPaymentMode("EMI")
            .setEmiType("CREDIT_CARD")
            .setPlanId(pgPlanId)
            .setCardInfo("|"+paymentDTO.ICICI_DEBIT_CARD_NUMBER_EMI+"|1234|12"+paymentDTO.Tokenization_Year)
            .setAuthMode("otp")
            .build();

        ProcessTxnV1Response ptcResponse =NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Card number entered does not match with the selected Bank");
    }

    @Owner(Constants.Owner.MAYURI)
    @Feature("PGP-53542")
    @Test(description = "Verify Success Txn when 1.Validate EMI having Amex card number A\n" +
            "2.PTC having alt id of amex A")
    public void verifySuccessTxnforValidateHavingAmexAndPTCHavinALTIDOfSameCard() throws Exception {
        Constants.MerchantType emiMerchant = Constants.MerchantType.SIMPLIFIED_OFFERS;
        PaymentDTO paymentDTO = new PaymentDTO();
        String orderId = CommonHelpers.generateOrderId();
        User user = userManager.getForRead(Label.BASIC);
        String price ="1100";
        String subventionAmount = "1100";
        SoftAssertions softAssertions= new SoftAssertions();

        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder()
            .setMid(emiMerchant.getId())
            .setTokenType("SSO")
            .setToken(user.ssoToken())
            .setPrice(price)
            .setSubventionAmount(subventionAmount)
            .setItems(null)
            .setFilters(new Filters()
                .setBankCode("AMEX")
                .setCardType("CREDIT_CARD"))
            .build();
        ApiV1Tenure api = new ApiV1Tenure(emiMerchant.getId(), req);
        JsonPath response = api.execute().jsonPath();
        softAssertions.assertThat(response.getString("body.resultInfo.resultStatus").equalsIgnoreCase("S"));
        softAssertions.assertAll();
        String planId= response.getString("body.planDetails[0].planId");
        String pgPlanId= response.getString("body.planDetails[0].pgPlanId");
        String offerId=response.getString("body.planDetails[0].itemBreakUp[0].offerId");

        BigInteger intplanid= new BigInteger(planId);
        ApiV1ValidateRequest req2 = new ApiV1ValidateRequest.Builder(price,subventionAmount)
                .setMid(emiMerchant.getId())
                .setMerchantKey(emiMerchant.getKey())
                .setCustomerId(orderId)
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setPlanId(intplanid)
                .setPaymentDetails(
                        new PaymentDetails()
                                .setTotalTransactionAmount(price)
                            .setCardNumber("|"+paymentDTO.AMEX_CREDIT_CARD_NUMBER_A+"|123|07"+paymentDTO.Tokenization_Year))
            .setGenerateTokenForIntent(true)
            .setOfferDetails(new OfferDetails().setOfferId(offerId))
            .build();
        ApiV1Validate api2 = new ApiV1Validate(emiMerchant.getId(), req2);
        Response r2 = api2.execute();
        JsonPath validateResponse =  r2.then()
                .extract().jsonPath();
        softAssertions.assertThat(validateResponse.getString("body.resultInfo.resultStatus").equalsIgnoreCase("S"));
        softAssertions.assertThat(validateResponse.getString("body.planId").equalsIgnoreCase(planId));
        softAssertions.assertThat(validateResponse.getString("body.pgPlanId").equalsIgnoreCase(pgPlanId));
        softAssertions.assertThat(validateResponse.getString("body.itemBreakUpList.offerId").equalsIgnoreCase(offerId));
        softAssertions.assertAll();
        String finalTxnAmount = validateResponse.getString("body.finalTransactionAmount");
        String emiSubventionToken = validateResponse.getString("body.emiSubventionToken");

        //Initiate Txn
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),emiMerchant)
                .setTxnValue(price)
                .setPayableAmount(new TxnAmount(finalTxnAmount))
                .setOrderId(orderId)
                .setEmiSubventionToken(emiSubventionToken)
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();

        CardTokenInfo cardTokenInfo = new CardTokenInfo();
        cardTokenInfo.setTokenType("ALTERNATE");
        cardTokenInfo.setCardToken("374245001741008");
        cardTokenInfo.setTokenExpiry("07"+paymentDTO.Tokenization_Year);
        cardTokenInfo.setTavv("F0F1F0F2F040acc17da4619ce7564bd3347e8651000000007c");
        cardTokenInfo.setCardSuffix("1006");
        cardTokenInfo.setPanUniqueReference("A0007WGW1RKCH1T5CGRBHFPTESTE2");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                Constants.MerchantType.SIMPLIFIED_OFFERS.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("EMI")
                .setEmiType("CREDIT_CARD")
                .setPlanId(pgPlanId)
                .setcardTokenInfo(cardTokenInfo)
                .setAuthMode("otp")
                .setCardInfo("||1234|")
                .build();

        submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
        responsePage
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateOrderId(initTxnDTO.orderFromBody())
                .validateMid(Constants.MerchantType.SIMPLIFIED_OFFERS.getId())
                .assertAll();

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy, initTxnDTO.getBody().getOrderId(), "directPassThroughInfo");
        softAssertions.assertThat(SavedCardHelpers.verifyParamPresentInDirectPassthrough(logs,"tavv")).isTrue();
        softAssertions.assertAll();
    }

    @Owner(Constants.Owner.MAYURI)
    @Feature("PGP-53542")
    @Test(description = "Verify Success Txn when 1.Validate EMI having Amex card number B\n" +
            "2.PTC having alt id of amex A")
    public void verifySuccessTxnforValidateHavingAmexAndPTCHavinALTIDOfDifferentCard() throws Exception {
        Constants.MerchantType emiMerchant = Constants.MerchantType.SIMPLIFIED_OFFERS;
        PaymentDTO paymentDTO = new PaymentDTO();
        String orderId = CommonHelpers.generateOrderId();
        User user = userManager.getForRead(Label.BASIC);
        String price ="1100";
        String subventionAmount = "1100";
        SoftAssertions softAssertions= new SoftAssertions();

        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder()
            .setMid(emiMerchant.getId())
            .setTokenType("SSO")
            .setToken(user.ssoToken())
            .setPrice(price)
            .setSubventionAmount(subventionAmount)
            .setItems(null)
            .setFilters(new Filters()
                .setBankCode("AMEX")
                .setCardType("CREDIT_CARD"))
            .build();
        ApiV1Tenure api = new ApiV1Tenure(emiMerchant.getId(), req);
        JsonPath response = api.execute().jsonPath();
        softAssertions.assertThat(response.getString("body.resultInfo.resultStatus").equalsIgnoreCase("S"));
        softAssertions.assertAll();
        String planId= response.getString("body.planDetails[0].planId");
        String pgPlanId= response.getString("body.planDetails[0].pgPlanId");
        String offerId=response.getString("body.planDetails[0].itemBreakUp[0].offerId");

        BigInteger intplanid= new BigInteger(planId);
        ApiV1ValidateRequest req2 = new ApiV1ValidateRequest.Builder(price,subventionAmount)
                .setMid(emiMerchant.getId())
                .setMerchantKey(emiMerchant.getKey())
                .setCustomerId(orderId)
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setPlanId(intplanid)
                .setPaymentDetails(
                        new PaymentDetails()
                                .setTotalTransactionAmount(price)
                            .setCardNumber("|"+paymentDTO.AMEX_CREDIT_CARD_NUMBER_B+"|123|07"+paymentDTO.Tokenization_Year))
            .setGenerateTokenForIntent(true)
            .setOfferDetails(new OfferDetails().setOfferId(offerId))
            .build();
        ApiV1Validate api2 = new ApiV1Validate(emiMerchant.getId(), req2);
        Response r2 = api2.execute();
        JsonPath validateResponse =  r2.then()
            .extract().jsonPath();
        softAssertions.assertThat(validateResponse.getString("body.resultInfo.resultStatus").equalsIgnoreCase("S"));
        softAssertions.assertThat(validateResponse.getString("body.planId").equalsIgnoreCase(planId));
        softAssertions.assertThat(validateResponse.getString("body.pgPlanId").equalsIgnoreCase(pgPlanId));
        softAssertions.assertThat(validateResponse.getString("body.itemBreakUpList.offerId").equalsIgnoreCase(offerId));
        softAssertions.assertAll();
        String finalTxnAmount = validateResponse.getString("body.finalTransactionAmount");
        String emiSubventionToken = validateResponse.getString("body.emiSubventionToken");

        //Initiate Txn
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),emiMerchant)
                .setTxnValue(price)
                .setPayableAmount(new TxnAmount(finalTxnAmount))
                .setOrderId(orderId)
                .setEmiSubventionToken(emiSubventionToken)
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();

        CardTokenInfo cardTokenInfo = new CardTokenInfo();
        cardTokenInfo.setTokenType("ALTERNATE");
        cardTokenInfo.setCardToken("374245001741008");
        cardTokenInfo.setTokenExpiry("07"+paymentDTO.Tokenization_Year);
        cardTokenInfo.setTavv("F0F1F0F2F040acc17da4619ce7564bd3347e8651000000007c");
        cardTokenInfo.setCardSuffix("1006");
        cardTokenInfo.setPanUniqueReference("A0007WGW1RKCH1T5CGRBHFPTESTE2");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                Constants.MerchantType.SIMPLIFIED_OFFERS.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("EMI")
                .setEmiType("CREDIT_CARD")
                .setPlanId(pgPlanId)
                .setcardTokenInfo(cardTokenInfo)
                .setAuthMode("otp")
                .setCardInfo("||1234|")
                .build();

        submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
        responsePage
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateOrderId(initTxnDTO.orderFromBody())
                .validateMid(Constants.MerchantType.SIMPLIFIED_OFFERS.getId())
                .assertAll();

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy, initTxnDTO.getBody().getOrderId(), "directPassThroughInfo");
        softAssertions.assertThat(SavedCardHelpers.verifyParamPresentInDirectPassthrough(logs,"tavv")).isTrue();
        softAssertions.assertAll();
    }

    @Owner(Constants.Owner.MAYURI)
    @Feature("PGP-53542")
    @Test(description = "Verify Failure Txn when 1.Validate EMI having Amex card number A\n" +
            "2.PTC having alt id of HDFC  A")
    public void verifyFailureTxnforValidateHavingAmexAndPTCHavinALTIDOfHDFCCard() throws Exception {
        Constants.MerchantType emiMerchant = Constants.MerchantType.SIMPLIFIED_OFFERS;
        String orderId = CommonHelpers.generateOrderId();
        User user = userManager.getForRead(Label.BASIC);
        String price ="1100";
        String subventionAmount = "1100";

        //planId is fetch from bank api for amex,CC
        String planId = "311173671414663168";
        BigInteger intplanid= new BigInteger(planId);
        ApiV1ValidateRequest req2 = new ApiV1ValidateRequest.Builder(price,subventionAmount)
                .setMid(emiMerchant.getId())
                .setMerchantKey(emiMerchant.getKey())
                .setCustomerId(orderId)
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setPlanId(intplanid)
                .setPaymentDetails(
                        new PaymentDetails()
                                .setTotalTransactionAmount(price)
                                .setCardNumber("|370295061673669|123|072024"))
                .setGenerateTokenForIntent(true)
                .setOfferDetails(new OfferDetails().setOfferId("2238033"))
                .build();
        ApiV1Validate api2 = new ApiV1Validate(emiMerchant.getId(), req2);
        Response r2 = api2.execute();
        JsonPath validateResponse =  r2.then()
                .extract().jsonPath();
        String finalTxnAmount = validateResponse.getString("body.finalTransactionAmount");
        String emiSubventionToken = validateResponse.getString("body.emiSubventionToken");

        //Initiate Txn
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),emiMerchant)
                .setTxnValue(price)
                .setPayableAmount(new TxnAmount(finalTxnAmount))
                .setOrderId(orderId)
                .setEmiSubventionToken(emiSubventionToken)
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();

        CardTokenInfo cardTokenInfo = new CardTokenInfo();
        cardTokenInfo.setTokenType("ALTERNATE");
        cardTokenInfo.setCardToken("4718650100000195");
        cardTokenInfo.setTokenExpiry("122027");
        cardTokenInfo.setTavv("AgAAAAoAPd52XQkAmeNXghMAAAA");
        cardTokenInfo.setCardSuffix("0336");
        cardTokenInfo.setPanUniqueReference("V0010013021361288827541710336");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                Constants.MerchantType.SIMPLIFIED_OFFERS.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("EMI")
                .setEmiType("CREDIT_CARD")
                .setPlanId("AMEX|3")
                .setcardTokenInfo(cardTokenInfo)
                .setAuthMode("otp")
                .setCardInfo("||545|")
                .build();

        submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
        responsePage
                .validateRespCode("810")
                .validateRespMsg("Payment failed due to a technical error. Please try after some time.")
                .validateStatus("TXN_FAILURE")
                .validateOrderId(initTxnDTO.orderFromBody())
                .validateMid(Constants.MerchantType.SIMPLIFIED_OFFERS.getId())
                .assertAll();
    }

    @Owner(Constants.Owner.MAYURI)
    @Feature("PGP-53542")
    @Test(description = "Verify Failure Txn when validate_emi having HDFC card number A and PTC having HDFC card number B ")
    public void verifyFailureTxnforValidateAndPTCHavingDifferentHDFCCard() throws Exception {
        Constants.MerchantType emiMerchant = Constants.MerchantType.SIMPLIFIED_OFFERS;
        PaymentDTO paymentDTO = new PaymentDTO();
        String orderId = CommonHelpers.generateOrderId();
        User user = userManager.getForRead(Label.BASIC);
        String price ="1100";
        String subventionAmount = "1100";

        //planId is fetch from bank api for amex,CC
        String planId = "306133932066376709";
        BigInteger intplanid= new BigInteger(planId);
        ApiV1ValidateRequest req2 = new ApiV1ValidateRequest.Builder(price,subventionAmount)
                .setMid(emiMerchant.getId())
                .setMerchantKey(emiMerchant.getKey())
                .setCustomerId(orderId)
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setPlanId(intplanid)
                .setPaymentDetails(
                        new PaymentDetails()
                                .setTotalTransactionAmount(price)
                                .setCardNumber("|4718650100010336|123|072024"))
                .setGenerateTokenForIntent(true)
                .setOfferDetails(new OfferDetails().setOfferId("2238033"))
                .build();
        ApiV1Validate api2 = new ApiV1Validate(emiMerchant.getId(), req2);
        Response r2 = api2.execute();
        JsonPath validateResponse =  r2.then()
                .extract().jsonPath();
        String finalTxnAmount = validateResponse.getString("body.finalTransactionAmount");
        String emiSubventionToken = validateResponse.getString("body.emiSubventionToken");
        String pgPlanId = validateResponse.getString("body.pgPlanId");

        //Initiate Txn
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),emiMerchant)
                .setTxnValue(price)
                .setPayableAmount(new TxnAmount(finalTxnAmount))
                .setOrderId(orderId)
                .setEmiSubventionToken(emiSubventionToken)
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                Constants.MerchantType.SIMPLIFIED_OFFERS.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("EMI")
                .setEmiType("CREDIT_CARD")
                .setPlanId("HDFC|3")
                .setCardInfo("|4895380115392363|123|122031")
                .setAuthMode("otp")
                .build();

        submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
        responsePage
                .validateRespCode("810")
                .validateRespMsg("Payment failed due to a technical error. Please try after some time.")
                .validateStatus("TXN_FAILURE")
                .validateOrderId(initTxnDTO.orderFromBody())
                .validateMid(Constants.MerchantType.SIMPLIFIED_OFFERS.getId())
                .assertAll();
    }

    @Owner(Constants.Owner.MAYURI)
    @Feature("PGP-53542")
    @Test(description = "Verify Failure Txn when validate_emi having HDFC card number A and PTC having HDFC card number B ")
    public void verifyFailureTxnforValidateHavingHDFCCardAAndPTCHavingHDFCCardB() throws Exception {
        Constants.MerchantType emiMerchant = Constants.MerchantType.SIMPLIFIED_OFFERS;
        PaymentDTO paymentDTO = new PaymentDTO();
        String orderId = CommonHelpers.generateOrderId();
        User user = userManager.getForRead(Label.BASIC);
        String price ="1100";
        String subventionAmount = "1100";

        //planId is fetch from bank api for amex,CC
        String planId = "306133932066376709";
        BigInteger intplanid= new BigInteger(planId);
        ApiV1ValidateRequest req2 = new ApiV1ValidateRequest.Builder(price,subventionAmount)
                .setMid(emiMerchant.getId())
                .setMerchantKey(emiMerchant.getKey())
                .setCustomerId(orderId)
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setPlanId(intplanid)
                .setPaymentDetails(
                        new PaymentDetails()
                                .setTotalTransactionAmount(price)
                                .setCardNumber("|4718650100010336|123|072024"))
                .setGenerateTokenForIntent(true)
                .setOfferDetails(new OfferDetails().setOfferId("2238033"))
                .build();
        ApiV1Validate api2 = new ApiV1Validate(emiMerchant.getId(), req2);
        Response r2 = api2.execute();
        JsonPath validateResponse =  r2.then()
                .extract().jsonPath();
        String finalTxnAmount = validateResponse.getString("body.finalTransactionAmount");
        String emiSubventionToken = validateResponse.getString("body.emiSubventionToken");
        String pgPlanId = validateResponse.getString("body.pgPlanId");

        //Initiate Txn
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),emiMerchant)
                .setTxnValue(price)
                .setPayableAmount(new TxnAmount(finalTxnAmount))
                .setOrderId(orderId)
                .setEmiSubventionToken(emiSubventionToken)
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                Constants.MerchantType.SIMPLIFIED_OFFERS.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("EMI")
                .setEmiType("CREDIT_CARD")
                .setPlanId("HDFC|3")
                .setCardInfo("|4895380115392363|123|122031")
                .setAuthMode("otp")
                .build();

        submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
        responsePage
                .validateRespCode("810")
                .validateRespMsg("Payment failed due to a technical error. Please try after some time.")
                .validateStatus("TXN_FAILURE")
                .validateOrderId(initTxnDTO.orderFromBody())
                .validateMid(Constants.MerchantType.SIMPLIFIED_OFFERS.getId())
                .assertAll();
    }

    @Owner(Constants.Owner.MAYURI)
    @Feature("PGP-53542")
    @Test(description = "Verify Success Txn when 1.Validate EMI having Amex card number A\n" +
            "2.PTC having alt id of amex A")
    public void verifySuccessTxnforValidateHavingAmexAndPTCHavinCOFTOfSameCard() throws Exception {
        Constants.MerchantType emiMerchant = Constants.MerchantType.SIMPLIFIED_OFFERS;
        PaymentDTO paymentDTO = new PaymentDTO();
        String orderId = CommonHelpers.generateOrderId();
        User user = userManager.getForRead(Label.BASIC);
        String price ="1100";
        String subventionAmount = "1100";
        SoftAssertions softAssertions= new SoftAssertions();

        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder()
            .setMid(emiMerchant.getId())
            .setTokenType("SSO")
            .setToken(user.ssoToken())
            .setPrice(price)
            .setSubventionAmount(subventionAmount)
            .setItems(null)
            .setFilters(new Filters()
                .setBankCode("AMEX")
                .setCardType("CREDIT_CARD"))
            .build();
        ApiV1Tenure api = new ApiV1Tenure(emiMerchant.getId(), req);
        JsonPath response = api.execute().jsonPath();
        softAssertions.assertThat(response.getString("body.resultInfo.resultStatus").equalsIgnoreCase("S"));
        softAssertions.assertAll();
        String planId= response.getString("body.planDetails[0].planId");
        String pgPlanId= response.getString("body.planDetails[0].pgPlanId");
        String offerId=response.getString("body.planDetails[0].itemBreakUp[0].offerId");

        JSONObject cardTokenInfoJson = new JSONObject();
        cardTokenInfoJson.put("tokenType","COFT");
        cardTokenInfoJson.put("cardToken","3798632970000195");
        cardTokenInfoJson.put("tokenExpiry","12"+paymentDTO.Tokenization_Year);
        cardTokenInfoJson.put("TAVV","F0F1F0F2F040acc17da4619ce7564bd3347e8651000000007c");
        cardTokenInfoJson.put("cardSuffix","1006");
        cardTokenInfoJson.put("panUniqueReference","V0010013021361288827541721006");
        GetCardToken getCardToken = new GetCardToken(PGP_HOST, "MID=" + Constants.MerchantType.SIMPLIFIED_OFFERS.getId()+"&CUST_ID=" + "1000036031"  + "&CVV=" + "123" +"&CARD_TOKEN_INFO=" + cardTokenInfoJson.toJSONString()  );
        JsonPath getCardTokenResponse = getCardToken.execute().jsonPath();
        String cacheCardToken = getCardTokenResponse.getString("TOKEN");
        BigInteger intplanid= new BigInteger(planId);
        ApiV1ValidateRequest req2 = new ApiV1ValidateRequest.Builder(price,subventionAmount)
                .setMid(emiMerchant.getId())
                .setMerchantKey(emiMerchant.getKey())
                .setCustomerId(orderId)
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setPlanId(intplanid)
                .setCacheCardToken(cacheCardToken)
                .setPaymentDetails(
                        new PaymentDetails()
                                .setTotalTransactionAmount(price))
                .setGenerateTokenForIntent(true)
            .setOfferDetails(new OfferDetails().setOfferId(offerId))
            .build();
        ApiV1Validate api2 = new ApiV1Validate(emiMerchant.getId(), req2);
        Response r2 = api2.execute();
        JsonPath validateResponse =  r2.then()
            .extract().jsonPath();
        softAssertions.assertThat(validateResponse.getString("body.resultInfo.resultStatus").equalsIgnoreCase("S"));
        softAssertions.assertThat(validateResponse.getString("body.planId").equalsIgnoreCase(planId));
        softAssertions.assertThat(validateResponse.getString("body.pgPlanId").equalsIgnoreCase(pgPlanId));
        softAssertions.assertThat(validateResponse.getString("body.itemBreakUpList.offerId").equalsIgnoreCase(offerId));
        softAssertions.assertAll();
        String finalTxnAmount = validateResponse.getString("body.finalTransactionAmount");
        String emiSubventionToken = validateResponse.getString("body.emiSubventionToken");

        //Initiate Txn
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),emiMerchant)
                .setTxnValue(price)
                .setPayableAmount(new TxnAmount(finalTxnAmount))
                .setOrderId(orderId)
                .setEmiSubventionToken(emiSubventionToken)
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();

        CardTokenInfo cardTokenInfo = new CardTokenInfo();
        cardTokenInfo.setTokenType("COFT");
        cardTokenInfo.setCardToken("3798632970000195");
        cardTokenInfo.setTokenExpiry("12"+paymentDTO.Tokenization_Year);
        cardTokenInfo.setTavv("F0F1F0F2F040acc17da4619ce7564bd3347e8651000000007c");
        cardTokenInfo.setCardSuffix("1006");
        cardTokenInfo.setPanUniqueReference("V0010013021361288827541721006");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                Constants.MerchantType.SIMPLIFIED_OFFERS.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("EMI")
                .setEmiType("CREDIT_CARD")
                .setPlanId(pgPlanId)
                .setcardTokenInfo(cardTokenInfo)
                .setAuthMode("otp")
                .setCardInfo("||1234|")
                .build();

        submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
        responsePage
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateOrderId(initTxnDTO.orderFromBody())
                .validateMid(Constants.MerchantType.SIMPLIFIED_OFFERS.getId())
                .assertAll();


        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy, initTxnDTO.getBody().getOrderId(), "directPassThroughInfo");
        softAssertions.assertThat(SavedCardHelpers.verifyParamPresentInDirectPassthrough(logs,"tavv")).isTrue();
        softAssertions.assertAll();
    }

    @Owner(Constants.Owner.MAYURI)
    @Feature("PGP-53542")
    @Test(description = "Verify Success Txn when 1.Validate EMI having Amex card number A\n" +
            "2.PTC having alt id of amex B")
    public void verifySuccessTxnforValidateHavingAmexAndPTCHavinCOFTOfDifferentCard() throws Exception {
        Constants.MerchantType emiMerchant = Constants.MerchantType.SIMPLIFIED_OFFERS;
        PaymentDTO paymentDTO = new PaymentDTO();
        String orderId = CommonHelpers.generateOrderId();
        User user = userManager.getForRead(Label.BASIC);
        String price ="1100";
        String subventionAmount = "1100";
        SoftAssertions softAssertions= new SoftAssertions();

        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder()
            .setMid(emiMerchant.getId())
            .setTokenType("SSO")
            .setToken(user.ssoToken())
            .setPrice(price)
            .setSubventionAmount(subventionAmount)
            .setItems(null)
            .setFilters(new Filters()
                .setBankCode("AMEX")
                .setCardType("CREDIT_CARD"))
            .build();
        ApiV1Tenure api = new ApiV1Tenure(emiMerchant.getId(), req);
        JsonPath response = api.execute().jsonPath();
        softAssertions.assertThat(response.getString("body.resultInfo.resultStatus").equalsIgnoreCase("S"));
        softAssertions.assertAll();
        String planId= response.getString("body.planDetails[0].planId");
        String pgPlanId= response.getString("body.planDetails[0].pgPlanId");
        String offerId=response.getString("body.planDetails[0].itemBreakUp[0].offerId");

        JSONObject cardTokenInfoJson = new JSONObject();
        cardTokenInfoJson.put("tokenType","COFT");
        cardTokenInfoJson.put("cardToken","3798632970000195");
        cardTokenInfoJson.put("tokenExpiry","12"+paymentDTO.Tokenization_Year);
        cardTokenInfoJson.put("TAVV","F0F1F0F2F040acc17da4619ce7564bd3347e8651000000007c");
        cardTokenInfoJson.put("cardSuffix","1006");
        cardTokenInfoJson.put("panUniqueReference","V0010013021361288827541721006");
        GetCardToken getCardToken = new GetCardToken(PGP_HOST, "MID=" + Constants.MerchantType.SIMPLIFIED_OFFERS.getId()+"&CUST_ID=" + "1000036031"  + "&CVV=" + "123" +"&CARD_TOKEN_INFO=" + cardTokenInfoJson.toJSONString()  );
        JsonPath getCardTokenResponse = getCardToken.execute().jsonPath();
        String cacheCardToken = getCardTokenResponse.getString("TOKEN");
        BigInteger intplanid= new BigInteger(planId);
        ApiV1ValidateRequest req2 = new ApiV1ValidateRequest.Builder(price,subventionAmount)
                .setMid(emiMerchant.getId())
                .setMerchantKey(emiMerchant.getKey())
                .setCustomerId(orderId)
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setPlanId(intplanid)
                .setCacheCardToken(cacheCardToken)
                .setPaymentDetails(
                        new PaymentDetails()
                                .setTotalTransactionAmount(price))
                .setGenerateTokenForIntent(true)
            .setOfferDetails(new OfferDetails().setOfferId(offerId))
            .build();
        ApiV1Validate api2 = new ApiV1Validate(emiMerchant.getId(), req2);
        Response r2 = api2.execute();
        JsonPath validateResponse =  r2.then()
            .extract().jsonPath();
        softAssertions.assertThat(validateResponse.getString("body.resultInfo.resultStatus").equalsIgnoreCase("S"));
        softAssertions.assertThat(validateResponse.getString("body.planId").equalsIgnoreCase(planId));
        softAssertions.assertThat(validateResponse.getString("body.pgPlanId").equalsIgnoreCase(pgPlanId));
        softAssertions.assertThat(validateResponse.getString("body.itemBreakUpList.offerId").equalsIgnoreCase(offerId));
        softAssertions.assertAll();
        String finalTxnAmount = validateResponse.getString("body.finalTransactionAmount");
        String emiSubventionToken = validateResponse.getString("body.emiSubventionToken");

        //Initiate Txn
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),emiMerchant)
                .setTxnValue(price)
                .setPayableAmount(new TxnAmount(finalTxnAmount))
                .setOrderId(orderId)
                .setEmiSubventionToken(emiSubventionToken)
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();

        CardTokenInfo cardTokenInfo = new CardTokenInfo();
        cardTokenInfo.setTokenType("COFT");
        cardTokenInfo.setCardToken("3798632970000195");
        cardTokenInfo.setTokenExpiry("07"+paymentDTO.Tokenization_Year);
        cardTokenInfo.setTavv("F0F1F0F2F040acc17da4619ce7564bd3347e8651000000007c");
        cardTokenInfo.setCardSuffix("3669");
        cardTokenInfo.setPanUniqueReference("A0007WGW1RKCH1T5CGRBHFPTESTE2");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                Constants.MerchantType.SIMPLIFIED_OFFERS.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("EMI")
                .setEmiType("CREDIT_CARD")
                .setPlanId(pgPlanId)
                .setcardTokenInfo(cardTokenInfo)
                .setAuthMode("otp")
                .setCardInfo("||1234|")
                .build();

        submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
        responsePage
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateOrderId(initTxnDTO.orderFromBody())
                .validateMid(Constants.MerchantType.SIMPLIFIED_OFFERS.getId())
                .assertAll();


        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy, initTxnDTO.getBody().getOrderId(), "directPassThroughInfo");
        softAssertions.assertThat(SavedCardHelpers.verifyParamPresentInDirectPassthrough(logs,"tavv")).isTrue();
        softAssertions.assertAll();
    }

    @Owner(Constants.Owner.MAYURI)
    @Feature("PGP-53542")
    @Test(description = "Verify Failure Txn when 1.Validate EMI having Amex card number A\n" +
            "2.PTC having alt id of HDFC A")
    public void verifyFailureTxnforValidateHavingAmexAndPTCHavinCOFTOfHDFCCard() throws Exception {
        Constants.MerchantType emiMerchant = Constants.MerchantType.SIMPLIFIED_OFFERS;
        String orderId = CommonHelpers.generateOrderId();
        User user = userManager.getForRead(Label.BASIC);
        String price ="1100";
        String subventionAmount = "1100";

        //planId is fetch from bank api for amex,CC
        String planId = "311173671414663168";
        JSONObject cardTokenInfoJson = new JSONObject();
        cardTokenInfoJson.put("tokenType","COFT");
        cardTokenInfoJson.put("cardToken","3798632970000195");
        cardTokenInfoJson.put("tokenExpiry","122027");
        cardTokenInfoJson.put("TAVV","F0F1F0F2F040acc17da4619ce7564bd3347e8651000000007c");
        cardTokenInfoJson.put("cardSuffix","1006");
        cardTokenInfoJson.put("panUniqueReference","V0010013021361288827541721006");
        GetCardToken getCardToken = new GetCardToken(PGP_HOST, "MID=" + Constants.MerchantType.SIMPLIFIED_OFFERS.getId()+"&CUST_ID=" + "1000036031"  + "&CVV=" + "123" +"&CARD_TOKEN_INFO=" + cardTokenInfoJson.toJSONString()  );
        JsonPath getCardTokenResponse = getCardToken.execute().jsonPath();
        String cacheCardToken = getCardTokenResponse.getString("TOKEN");
        BigInteger intplanid= new BigInteger(planId);
        ApiV1ValidateRequest req2 = new ApiV1ValidateRequest.Builder(price,subventionAmount)
                .setMid(emiMerchant.getId())
                .setMerchantKey(emiMerchant.getKey())
                .setCustomerId(orderId)
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setPlanId(intplanid)
                .setCacheCardToken(cacheCardToken)
                .setPaymentDetails(
                        new PaymentDetails()
                                .setTotalTransactionAmount(price))
                .setGenerateTokenForIntent(true)
                .setOfferDetails(new OfferDetails().setOfferId("2378530"))
                .build();
        ApiV1Validate api2 = new ApiV1Validate(emiMerchant.getId(), req2);
        Response r2 = api2.execute();
        JsonPath validateResponse =  r2.then()
                .extract().jsonPath();
        String finalTxnAmount = validateResponse.getString("body.finalTransactionAmount");
        String emiSubventionToken = validateResponse.getString("body.emiSubventionToken");

        //Initiate Txn
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),emiMerchant)
                .setTxnValue(price)
                .setPayableAmount(new TxnAmount(finalTxnAmount))
                .setOrderId(orderId)
                .setEmiSubventionToken(emiSubventionToken)
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();

        CardTokenInfo cardTokenInfo = new CardTokenInfo();
        cardTokenInfo.setTokenType("COFT");
        cardTokenInfo.setCardToken("4718650100000195");
        cardTokenInfo.setTokenExpiry("122027");
        cardTokenInfo.setTavv("AgAAAAoAPd52XQkAmeNXghMAAAA");
        cardTokenInfo.setCardSuffix("0336");
        cardTokenInfo.setPanUniqueReference("V0010013021361288827541710336");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                Constants.MerchantType.SIMPLIFIED_OFFERS.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("EMI")
                .setEmiType("CREDIT_CARD")
                .setPlanId("AMEX|3")
                .setcardTokenInfo(cardTokenInfo)
                .setAuthMode("otp")
                .setCardInfo("||545|")
                .build();

        submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
        responsePage
                .validateRespCode("810")
                .validateRespMsg("Payment failed due to a technical error. Please try after some time.")
                .validateStatus("TXN_FAILURE")
                .validateOrderId(initTxnDTO.orderFromBody())
                .validateMid(Constants.MerchantType.SIMPLIFIED_OFFERS.getId())
                .assertAll();
    }

    @Owner(Constants.Owner.MAYURI)
    @Feature("PGP-51804")
    @Test(description = "Validate having AMEX card number and PTC having alt id of AMEX but plan id=HDFC|3")
    public void verifyValidateHavingAmexAndPTCHavingHDFCPlanID() throws Exception {
        Constants.MerchantType emiMerchant = Constants.MerchantType.SIMPLIFIED_OFFERS;
        String orderId = CommonHelpers.generateOrderId();
        User user = userManager.getForRead(Label.BASIC);
        String price ="1100";
        String subventionAmount = "1100";

        //planId is fetch from bank api for amex,CC
        String planId = "405084491107534855";
        BigInteger intplanid= new BigInteger(planId);
        ApiV1ValidateRequest req2 = new ApiV1ValidateRequest.Builder(price,subventionAmount)
                .setMid(emiMerchant.getId())
                .setMerchantKey(emiMerchant.getKey())
                .setCustomerId(orderId)
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setPlanId(intplanid)
                .setPaymentDetails(
                        new PaymentDetails()
                                .setTotalTransactionAmount(price)
                                .setCardNumber("|370295061673669|123|072024"))
                .setGenerateTokenForIntent(true)
                .setOfferDetails(new OfferDetails().setOfferId("2378530"))
                .build();
        ApiV1Validate api2 = new ApiV1Validate(emiMerchant.getId(), req2);
        Response r2 = api2.execute();
        JsonPath validateResponse =  r2.then()
                .extract().jsonPath();
        String finalTxnAmount = validateResponse.getString("body.finalTransactionAmount");
        String emiSubventionToken = validateResponse.getString("body.emiSubventionToken");

        //Initiate Txn
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),emiMerchant)
                .setTxnValue(price)
                .setPayableAmount(new TxnAmount(finalTxnAmount))
                .setOrderId(orderId)
                .setEmiSubventionToken(emiSubventionToken)
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();

        CardTokenInfo cardTokenInfo = new CardTokenInfo();
        cardTokenInfo.setTokenType("ALTERNATE");
        cardTokenInfo.setCardToken("374245001741008");
        cardTokenInfo.setTokenExpiry("072040");
        cardTokenInfo.setTavv("F0F1F0F2F040acc17da4619ce7564bd3347e8651000000007c");
        cardTokenInfo.setCardSuffix("1006");
        cardTokenInfo.setPanUniqueReference("A0007WGW1RKCH1T5CGRBHFPTESTE2");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                Constants.MerchantType.SIMPLIFIED_OFFERS.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("EMI")
                .setEmiType("CREDIT_CARD")
                .setPlanId("HDFC|3")
                .setcardTokenInfo(cardTokenInfo)
                .setAuthMode("otp")
                .setCardInfo("||545|")
                .build();

        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
        Response ptcResponse = processTransactionV1.execute();
        Assert.assertEquals((ptcResponse.jsonPath().get("body.resultInfo.resultMsg")), "Card number entered does not match with the selected Bank");

        submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
        responsePage
                .validateRespCode("810")
                .validateRespMsg("Payment failed due to a technical error. Please try after some time.")
                .validateStatus("TXN_FAILURE")
                .validateOrderId(initTxnDTO.orderFromBody())
                .validateMid(Constants.MerchantType.SIMPLIFIED_OFFERS.getId())
                .assertAll();
    }


    @Owner(Constants.Owner.MAYURI)
    @Feature("PGP-51804")
    @Test(description = "Validate having AMEX card number and PTC having alt id of AMEX but plan id=AMEX|30")
    public void verifyValidateHavingAmexAndPTCHavingAMEXIncorrectPlanID() throws Exception {
        Constants.MerchantType emiMerchant = Constants.MerchantType.SIMPLIFIED_OFFERS;
        String orderId = CommonHelpers.generateOrderId();
        User user = userManager.getForRead(Label.BASIC);
        String price ="1100";
        String subventionAmount = "1100";

        //planId is fetch from bank api for amex,CC
        String planId = "405084491107534855";
        BigInteger intplanid= new BigInteger(planId);
        ApiV1ValidateRequest req2 = new ApiV1ValidateRequest.Builder(price,subventionAmount)
                .setMid(emiMerchant.getId())
                .setMerchantKey(emiMerchant.getKey())
                .setCustomerId(orderId)
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setPlanId(intplanid)
                .setPaymentDetails(
                        new PaymentDetails()
                                .setTotalTransactionAmount(price)
                                .setCardNumber("|370295061673669|123|072025"))
                .setGenerateTokenForIntent(true)
                .setOfferDetails(new OfferDetails().setOfferId("2378530"))
                .build();
        ApiV1Validate api2 = new ApiV1Validate(emiMerchant.getId(), req2);
        Response r2 = api2.execute();
        JsonPath validateResponse =  r2.then()
                .extract().jsonPath();
        String finalTxnAmount = validateResponse.getString("body.finalTransactionAmount");
        String emiSubventionToken = validateResponse.getString("body.emiSubventionToken");

        //Initiate Txn
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),emiMerchant)
                .setTxnValue(price)
                .setPayableAmount(new TxnAmount(finalTxnAmount))
                .setOrderId(orderId)
                .setEmiSubventionToken(emiSubventionToken)
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();

        CardTokenInfo cardTokenInfo = new CardTokenInfo();
        cardTokenInfo.setTokenType("ALTERNATE");
        cardTokenInfo.setCardToken("374245001741008");
        cardTokenInfo.setTokenExpiry("072040");
        cardTokenInfo.setTavv("F0F1F0F2F040acc17da4619ce7564bd3347e8651000000007c");
        cardTokenInfo.setCardSuffix("1006");
        cardTokenInfo.setPanUniqueReference("A0007WGW1RKCH1T5CGRBHFPTESTE2");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                Constants.MerchantType.SIMPLIFIED_OFFERS.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("EMI")
                .setEmiType("CREDIT_CARD")
                .setPlanId("AMEX|30")
                .setcardTokenInfo(cardTokenInfo)
                .setAuthMode("otp")
                .setCardInfo("||545|")
                .build();

        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
        Response ptcResponse = processTransactionV1.execute();
        Assert.assertEquals((ptcResponse.jsonPath().get("body.resultInfo.resultMsg")), "The selected EMI plan is not applicable for this transaction.");

        submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
        responsePage
                .validateRespCode("810")
                .validateRespMsg("Payment failed due to a technical error. Please try after some time.")
                .validateStatus("TXN_FAILURE")
                .validateOrderId(initTxnDTO.orderFromBody())
                .validateMid(Constants.MerchantType.SIMPLIFIED_OFFERS.getId())
                .assertAll();
    }

    @Owner(Constants.Owner.MAYURI)
    @Feature("PGP-51804")
    @Test(description = "Validate PTC by passing planId:ICICI|3, and card number of HDFC ")
    public void verifyFailureTxnforIncorrectPlanIDInPTC() throws Exception {
        Constants.MerchantType emiMerchant = Constants.MerchantType.SIMPLIFIED_OFFERS;
        String orderId = CommonHelpers.generateOrderId();
        User user = userManager.getForRead(Label.BASIC);
        String price ="20";
        //Initiate Txn
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),emiMerchant)
                .setTxnValue(price)
                .setOrderId(orderId)
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                Constants.MerchantType.SIMPLIFIED_OFFERS.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("EMI")
                .setEmiType("CREDIT_CARD")
                .setPlanId("ICICI|12")
                .setCardInfo("|4718650100010336|123|122031")
                .setAuthMode("otp")
                .build();

        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
        Response ptcResponse = processTransactionV1.execute();
        Assert.assertEquals((ptcResponse.jsonPath().get("body.resultInfo.resultMsg")), "The selected EMI plan is not applicable for this transaction.");

        submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
        responsePage
                .validateRespCode("810")
                .validateRespMsg("Payment failed due to a technical error. Please try after some time.")
                .validateStatus("TXN_FAILURE")
                .validateOrderId(initTxnDTO.orderFromBody())
                .validateMid(Constants.MerchantType.SIMPLIFIED_OFFERS.getId())
                .assertAll();
    }


    @Owner(Constants.Owner.MAYURI)
    @Feature("PGP-51804")
    @Test(description = "Validate PTC by passing planId:HDFC|3, and alt id of amex card ")
    public void verifyFailureTxnforIncorrectPlanIDUsingALTIDInPTC() throws Exception {
        Constants.MerchantType emiMerchant = Constants.MerchantType.SIMPLIFIED_OFFERS;
        String orderId = CommonHelpers.generateOrderId();
        User user = userManager.getForRead(Label.BASIC);
        String price ="20";
        //Initiate Txn
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),emiMerchant)
                .setTxnValue(price)
                .setOrderId(orderId)
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();

        CardTokenInfo cardTokenInfo = new CardTokenInfo();
        cardTokenInfo.setTokenType("ALTERNATE");
        cardTokenInfo.setCardToken("374245001741008");
        cardTokenInfo.setTokenExpiry("072040");
        cardTokenInfo.setTavv("F0F1F0F2F040acc17da4619ce7564bd3347e8651000000007c");
        cardTokenInfo.setCardSuffix("1006");
        cardTokenInfo.setPanUniqueReference("A0007WGW1RKCH1T5CGRBHFPTESTE2");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                Constants.MerchantType.SIMPLIFIED_OFFERS.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("EMI")
                .setEmiType("CREDIT_CARD")
                .setPlanId("HDFC|3")
                .setcardTokenInfo(cardTokenInfo)
                .setAuthMode("otp")
                .setCardInfo("||545|")
                .build();

        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
        Response ptcResponse = processTransactionV1.execute();
        Assert.assertEquals((ptcResponse.jsonPath().get("body.resultInfo.resultMsg")), "The selected EMI plan is not applicable for this transaction.");

        submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
        responsePage
                .validateRespCode("810")
                .validateRespMsg("Payment failed due to a technical error. Please try after some time.")
                .validateStatus("TXN_FAILURE")
                .validateOrderId(initTxnDTO.orderFromBody())
                .validateMid(Constants.MerchantType.SIMPLIFIED_OFFERS.getId())
                .assertAll();
    }

    @Owner(Constants.Owner.MAYURI)
    @Feature("PGP-51804")
    @Test(description = "Validate PTC by passing planId:HDFC|3, and COFT of amex card ")
    public void verifyFailureTxnforIncorrectPlanIDUsingCOFTInPTC() throws Exception {
        Constants.MerchantType emiMerchant = Constants.MerchantType.SIMPLIFIED_OFFERS;
        String orderId = CommonHelpers.generateOrderId();
        User user = userManager.getForRead(Label.BASIC);
        String price ="20";
        //Initiate Txn
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),emiMerchant)
                .setTxnValue(price)
                .setOrderId(orderId)
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();

        CardTokenInfo cardTokenInfo = new CardTokenInfo();
        cardTokenInfo.setTokenType("COFT");
        cardTokenInfo.setCardToken("370295061400195");
        cardTokenInfo.setTokenExpiry("112030");
        cardTokenInfo.setTavv("F0F1F0F2F040acc17da4619ce7564bd3347e8651000000007c");
        cardTokenInfo.setCardSuffix("3669");
        cardTokenInfo.setPanUniqueReference("V0010013021361288827541722214");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                Constants.MerchantType.SIMPLIFIED_OFFERS.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("EMI")
                .setEmiType("CREDIT_CARD")
                .setPlanId("HDFC|3")
                .setcardTokenInfo(cardTokenInfo)
                .setAuthMode("otp")
                .setCardInfo("||545|")
                .build();

        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
        Response ptcResponse = processTransactionV1.execute();
        Assert.assertEquals((ptcResponse.jsonPath().get("body.resultInfo.resultMsg")), "The selected EMI plan is not applicable for this transaction.");

        submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
        responsePage
                .validateRespCode("810")
                .validateRespMsg("Payment failed due to a technical error. Please try after some time.")
                .validateStatus("TXN_FAILURE")
                .validateOrderId(initTxnDTO.orderFromBody())
                .validateMid(Constants.MerchantType.SIMPLIFIED_OFFERS.getId())
                .assertAll();
    }

    @Owner(Constants.Owner.MAYURI)
    @Feature("PGP-51804")
    @Test(description = "Validate PTC by passing planId:AMEX|30, and COFT of amex card ")
    public void verifyFailureTxnforIncorrectTenureUsingCOFTInPTC() throws Exception {
        Constants.MerchantType emiMerchant = Constants.MerchantType.SIMPLIFIED_OFFERS;
        String orderId = CommonHelpers.generateOrderId();
        User user = userManager.getForRead(Label.BASIC);
        String price ="20";
        //Initiate Txn
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),emiMerchant)
                .setTxnValue(price)
                .setOrderId(orderId)
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();

        CardTokenInfo cardTokenInfo = new CardTokenInfo();
        cardTokenInfo.setTokenType("COFT");
        cardTokenInfo.setCardToken("370295061400195");
        cardTokenInfo.setTokenExpiry("112030");
        cardTokenInfo.setTavv("F0F1F0F2F040acc17da4619ce7564bd3347e8651000000007c");
        cardTokenInfo.setCardSuffix("3669");
        cardTokenInfo.setPanUniqueReference("V0010013021361288827541722214");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                Constants.MerchantType.SIMPLIFIED_OFFERS.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("EMI")
                .setEmiType("CREDIT_CARD")
                .setPlanId("AMEX|30")
                .setcardTokenInfo(cardTokenInfo)
                .setAuthMode("otp")
                .setCardInfo("||545|")
                .build();

        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
        Response ptcResponse = processTransactionV1.execute();
        Assert.assertEquals((ptcResponse.jsonPath().get("body.resultInfo.resultMsg")), "The selected EMI plan is not applicable for this transaction.");

        submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
        responsePage
                .validateRespCode("810")
                .validateRespMsg("Payment failed due to a technical error. Please try after some time.")
                .validateStatus("TXN_FAILURE")
                .validateOrderId(initTxnDTO.orderFromBody())
                .validateMid(Constants.MerchantType.SIMPLIFIED_OFFERS.getId())
                .assertAll();
    }

    @Owner(Constants.Owner.MAYURI)
    @Feature("PGP-51804")
    @Test(description = "Subvention txn : Validate having AMEX card number and PTC having COFT of AMEX but plan id=HDFC|3")
    public void verifyFailureTxnforValidateHavingAmexAndPTCHavinCOFTOfAMEXAndIncorrectPlanID() throws Exception {
        Constants.MerchantType emiMerchant = Constants.MerchantType.SIMPLIFIED_OFFERS;
        String orderId = CommonHelpers.generateOrderId();
        User user = userManager.getForRead(Label.BASIC);
        String price ="1100";
        String subventionAmount = "1100";

        //planId is fetch from bank api for amex,CC
        String planId = "405084491107534855";
        JSONObject cardTokenInfoJson = new JSONObject();
        cardTokenInfoJson.put("tokenType","COFT");
        cardTokenInfoJson.put("cardToken","370295061400195");
        cardTokenInfoJson.put("tokenExpiry","112030");
        cardTokenInfoJson.put("TAVV","F0F1F0F2F040acc17da4619ce7564bd3347e8651000000007c");
        cardTokenInfoJson.put("cardSuffix","1006");
        cardTokenInfoJson.put("panUniqueReference","V0010013021361288827541721006");
        GetCardToken getCardToken = new GetCardToken(PGP_HOST, "MID=" + Constants.MerchantType.SIMPLIFIED_OFFERS.getId()+"&CUST_ID=" + "1000036031"  + "&CVV=" + "123" +"&CARD_TOKEN_INFO=" + cardTokenInfoJson.toJSONString()  );
        JsonPath getCardTokenResponse = getCardToken.execute().jsonPath();
        String cacheCardToken = getCardTokenResponse.getString("TOKEN");
        BigInteger intplanid= new BigInteger(planId);
        ApiV1ValidateRequest req2 = new ApiV1ValidateRequest.Builder(price,subventionAmount)
                .setMid(emiMerchant.getId())
                .setMerchantKey(emiMerchant.getKey())
                .setCustomerId(orderId)
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setPlanId(intplanid)
                .setCacheCardToken(cacheCardToken)
                .setPaymentDetails(
                        new PaymentDetails()
                                .setTotalTransactionAmount(price))
                .setGenerateTokenForIntent(true)
                .setOfferDetails(new OfferDetails().setOfferId("2378530"))
                .build();
        ApiV1Validate api2 = new ApiV1Validate(emiMerchant.getId(), req2);
        Response r2 = api2.execute();
        JsonPath validateResponse =  r2.then()
                .extract().jsonPath();
        String finalTxnAmount = validateResponse.getString("body.finalTransactionAmount");
        String emiSubventionToken = validateResponse.getString("body.emiSubventionToken");

        //Initiate Txn
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),emiMerchant)
                .setTxnValue(price)
                .setPayableAmount(new TxnAmount(finalTxnAmount))
                .setOrderId(orderId)
                .setEmiSubventionToken(emiSubventionToken)
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();

        CardTokenInfo cardTokenInfo = new CardTokenInfo();
        cardTokenInfo.setTokenType("COFT");
        cardTokenInfo.setCardToken("370295061400195");
        cardTokenInfo.setTokenExpiry("112030");
        cardTokenInfo.setTavv("F0F1F0F2F040acc17da4619ce7564bd3347e8651000000007c");
        cardTokenInfo.setCardSuffix("1006");
        cardTokenInfo.setPanUniqueReference("V0010013021361288827541721006");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                Constants.MerchantType.SIMPLIFIED_OFFERS.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("EMI")
                .setEmiType("CREDIT_CARD")
                .setPlanId("HDFC|3")
                .setcardTokenInfo(cardTokenInfo)
                .setAuthMode("otp")
                .setCardInfo("||545|")
                .build();

        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
        Response ptcResponse = processTransactionV1.execute();
        Assert.assertEquals((ptcResponse.jsonPath().get("body.resultInfo.resultMsg")), "Card number entered does not match with the selected Bank");

        submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
        responsePage
                .validateRespCode("810")
                .validateRespMsg("Payment failed due to a technical error. Please try after some time.")
                .validateStatus("TXN_FAILURE")
                .validateOrderId(initTxnDTO.orderFromBody())
                .validateMid(Constants.MerchantType.SIMPLIFIED_OFFERS.getId())
                .assertAll();
    }



    public String getEmiId(String mid,String targetPlanId)
    {
        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("mid", mid);
        String signature = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.subvention, LocalConfig.JWT_EMI_KEY);
        GetEMIDetailsRequest getEMIDetailsRequest = new GetEMIDetailsRequest(signature, mid);
        Response response = new GetEMIDetails(getEMIDetailsRequest, mid).execute();

        JSONParser parser = new JSONParser();
        try {
            JSONObject jsonObject = (JSONObject) parser.parse(response.getBody().asString());
            JSONObject body = (JSONObject) jsonObject.get("body");
            org.json.simple.JSONArray emiDetailsArray = (org.json.simple.JSONArray) body.get("emiDetails");

            for (Object emiDetailObj : emiDetailsArray) {
                JSONObject emiDetail = (JSONObject) emiDetailObj;
                org.json.simple.JSONArray emiChannelInfos = (JSONArray) emiDetail.get("emiChannelInfos");

                for (Object emiChannelInfoObj : emiChannelInfos) {
                    JSONObject emiChannelInfo = (JSONObject) emiChannelInfoObj;
                    String planId = (String) emiChannelInfo.get("planId");
                    if (targetPlanId.equals(planId)) {
                        return (String) emiChannelInfo.get("emiId");
                    }
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;

    }
}