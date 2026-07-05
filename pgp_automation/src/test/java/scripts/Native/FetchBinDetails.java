package scripts.Native;

import com.paytm.CreateToken;
import com.paytm.FetchBin;
import com.paytm.api.RedisAPI;
import com.paytm.api.saveCard.GetBinBulkHash;
import com.paytm.api.nativeAPI.FetchBinDetail;
import com.paytm.api.nativeAPI.FetchEMIDetail;
import com.paytm.api.nativeAPI.FetchPaymentOptionV2;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.PG2LogsValidationHelper;
import com.paytm.apphelpers.PG2LogsValidationHelper.setEnvService;
import com.paytm.apphelpers.SavedCardHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.NativeDTO.fetchBinDetails.FetchBinDetailsRequest;
import com.paytm.dto.NativeDTO.fetchBinDetails.response.BinDetail;
import com.paytm.dto.NativeDTO.fetchBinDetails.response.FetchBinDetailResponse;
import com.paytm.dto.NativeDTO.fetchEMIDetail.FetchEMIDetailRequest;
import com.paytm.dto.PaymentDTO;
import com.paytm.utils.ff4j.FF4JFlags;
import com.paytm.utils.merchant.ff4j.FF4JFeatures;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.testng.Reporter;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import scripts.api.BinQuery.BinQuery;

import java.text.DecimalFormat;
import java.text.Format;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.paytm.apphelpers.LogsValidationHelper.getLogsOnServer;
import static com.paytm.apphelpers.LogsValidationHelper.verifyLogsOnPod;
import static org.hamcrest.Matchers.equalToIgnoringCase;

/**
 * Created by anjukumari on 15/10/18
 */
@Owner("Deepak")
public class FetchBinDetails extends PGPBaseTest {
    private final Format format = new DecimalFormat(".##");
    private static final String invalidAmexBin = "379863";
    private static final String invalidMaestroBin = "504433";

    private final static String BinTokenizationTrue = "444433";
    private final static String BinTokenizationFalse = "613033";

    @Test(description = "Verify failed response of fetch bin detail APIs when valid BIN is " +
            "passed for Merchant without AMEX acquiring with paymode CC")
    public void TC_FBD_AmexCC() throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.WalletOnly).build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath jsonPath = initTxn.execute().jsonPath();
        String txnToken = jsonPath.getString("body.txnToken");
        Reporter.log("Fetching bin detail for bin number:" + invalidAmexBin);
        FetchBinDetailsRequest request = new FetchBinDetailsRequest.Builder(txnToken, invalidAmexBin)
                .setPaymentMode("CC").build();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(request, initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId());
        JsonPath fetchBinsJson = fetchBinDetail.execute().jsonPath();
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultMsg")).
                isEqualTo("AMEX Credit card is not allowed for CC payment. " +
                        "Please try paying using other cards/options.");
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultCode")).isEqualTo("2011");
    }


    @Test(description = "Verify failed response of fetch bin detail APIs when valid BIN is " +
            "passed for Merchant without AMEX acquiring with paymode DC")
    public void TC_FBD_AmexDC() throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.WalletOnly).build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath jsonPath = initTxn.execute().jsonPath();
        String txnToken = jsonPath.getString("body.txnToken");
        Reporter.log("Fetching bin detail for bin number:" + invalidAmexBin);
        FetchBinDetailsRequest request = new FetchBinDetailsRequest.Builder(txnToken, invalidAmexBin)
                .setPaymentMode("DC").build();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(request, initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId());
        JsonPath fetchBinsJson = fetchBinDetail.execute().jsonPath();
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultMsg")).
                isEqualTo("AMEX Credit card is not allowed for DC payment. " +
                        "Please try paying using other cards/options.");
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultCode")).isEqualTo("2011");
    }

    @Test(description = "Verify failed response of fetch bin detail APIs when valid BIN is " +
            "passed for Merchant without AMEX acquiring with paymode EMI")
    public void TC_FBD_AmexEMI() throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.WalletOnly).build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath jsonPath = initTxn.execute().jsonPath();
        String txnToken = jsonPath.getString("body.txnToken");
        Reporter.log("Fetching bin detail for bin number:" + invalidAmexBin);
        FetchBinDetailsRequest request = new FetchBinDetailsRequest.Builder(txnToken, invalidAmexBin)
                .setPaymentMode("EMI").build();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(request, initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId());
        JsonPath fetchBinsJson = fetchBinDetail.execute().jsonPath();
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultMsg")).
                isEqualTo("AMEX Credit card is not allowed for EMI payment. " +
                        "Please try paying using other cards/options.");
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultCode")).isEqualTo("2011");
    }


    @Test(description = "Verify failed response of fetch bin detail APIs when valid BIN is " +
            "passed for Merchant without Maestro acquiring with paymode CC")
    public void TC_FBD_MaestroCC() throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.WalletOnly).build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath jsonPath = initTxn.execute().jsonPath();
        String txnToken = jsonPath.getString("body.txnToken");
        Reporter.log("Fetching bin detail for bin number:" + invalidMaestroBin);
        FetchBinDetailsRequest request = new FetchBinDetailsRequest.Builder(txnToken, invalidMaestroBin)
                .setPaymentMode("CC").build();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(request, initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId());
        JsonPath fetchBinsJson = fetchBinDetail.execute().jsonPath();
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultMsg")).
                isEqualTo("MAESTRO Debit card is not allowed for CC payment. Please try paying using other cards/options.");
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultCode")).isEqualTo("2011");
    }


    @Test(description = "Verify failed response of fetch bin detail APIs when valid BIN is " +
            "passed for Merchant without Maestro acquiring with paymode DC")
    public void TC_FBD_MaestroDC() throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.WalletOnly).build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath jsonPath = initTxn.execute().jsonPath();
        String txnToken = jsonPath.getString("body.txnToken");
        Reporter.log("Fetching bin detail for bin number:" + invalidMaestroBin);
        FetchBinDetailsRequest request = new FetchBinDetailsRequest.Builder(txnToken, invalidMaestroBin)
                .setPaymentMode("DC").build();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(request, initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId());
        JsonPath fetchBinsJson = fetchBinDetail.execute().jsonPath();
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultMsg")).
                isEqualTo("MAESTRO Debit card is not allowed for DC payment. Please try paying using other cards/options.");
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultCode")).isEqualTo("2011");
    }

    @Test(description = "Verify failed response of fetch bin detail APIs when valid BIN is " +
            "passed for Merchant without Maestro acquiring with paymode EMI")
    public void TC_FBD_MaestroEMI() throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.WalletOnly).build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath jsonPath = initTxn.execute().jsonPath();
        String txnToken = jsonPath.getString("body.txnToken");
        Reporter.log("Fetching bin detail for bin number:" + invalidMaestroBin);
        FetchBinDetailsRequest request = new FetchBinDetailsRequest.Builder(txnToken, invalidMaestroBin)
                .setPaymentMode("EMI").build();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(request, initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId());
        JsonPath fetchBinsJson = fetchBinDetail.execute().jsonPath();
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultMsg")).
                isEqualTo("MAESTRO Debit card is not allowed for EMI payment. Please try paying using other cards/options.");
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultCode")).isEqualTo("2011");
    }


    @Test(description = "Verify success response of fetch bin detail APIs when valid txn_token is passed.")
    public void TC_FBD_001() throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.NATIVE_HYBRID).build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath jsonPath = initTxn.execute().jsonPath();
        String txnToken = jsonPath.getString("body.txnToken");
        String binNumer = CommonHelpers.getCardFirstSixDigit(new PaymentDTO().getCreditCardNumber());
        Reporter.log("Fetching bin detail for bin numer:" + binNumer);
        FetchBinDetailsRequest request = new FetchBinDetailsRequest.Builder(txnToken, binNumer).build();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(request, initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId());
        JsonPath fetchBinsJson = fetchBinDetail.execute().jsonPath();
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail.channelName")).isEqualTo("VISA");
    }


    @Test(description = "Verify Failure response of fetch bin detail APIs when txn_token is black passed.")
    public void TC_FBD_006() throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.NATIVE_HYBRID).build();
        String binNumer = CommonHelpers.getCardFirstSixDigit(new PaymentDTO().getCreditCardNumber());
        Reporter.log("Fetching bin detail for bin numer:" + binNumer);
        FetchBinDetailsRequest request = new FetchBinDetailsRequest.Builder("", binNumer).build();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(request, initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId());
        JsonPath fetchBinsJson = fetchBinDetail.execute().jsonPath();
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultCode")).isEqualTo("1006");
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Your Session has expired.");
    }

    @Test(description = "Verify Failure response of fetch bin detail APIs when bin passed is less than 6digit.")
    public void TC_FBD_009() throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.NATIVE_HYBRID).build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath jsonPath = initTxn.execute().jsonPath();
        String txnToken = jsonPath.getString("body.txnToken");
        FetchBinDetailsRequest request = new FetchBinDetailsRequest.Builder(txnToken, "47186").build();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(request, initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId());
        JsonPath fetchBinsJson = fetchBinDetail.execute().jsonPath();
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultCode")).isEqualTo("1003");
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Bin number is not valid");
    }

    @Test(description = "Verify Failure response of fetch bin details APIs when txnToken is not passed")
    public void TC_FBD_005() throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.NATIVE_HYBRID).build();
        String binNumer = CommonHelpers.getCardFirstSixDigit(new PaymentDTO().getCreditCardNumber());
        Reporter.log("Fetching bin detail for bin numer:" + binNumer);
        FetchBinDetailsRequest request = new FetchBinDetailsRequest.Builder(null, binNumer).build();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(request, initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId());
        JsonPath fetchBinsJson = fetchBinDetail.execute().jsonPath();
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultCode")).isEqualTo("1006");
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Your Session has expired.");
    }

    @Test(description = "Verify successfull response of fetch bin details APIs when txnToken is passed")
    public void TC_FBD_004() throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.NATIVE_HYBRID).build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath jsonPath = initTxn.execute().jsonPath();
        String txnToken = jsonPath.getString("body.txnToken");
        String binNumer = CommonHelpers.getCardFirstSixDigit(new PaymentDTO().getCreditCardNumber());
        Reporter.log("Fetching bin detail for bin numer:" + binNumer);
        FetchBinDetailsRequest request = new FetchBinDetailsRequest.Builder(txnToken, binNumer).build();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(request, initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId());
        JsonPath fetchBinsJson = fetchBinDetail.execute().jsonPath();
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail.channelName")).isEqualTo("VISA");
    }

    @Test(description = "Verify failure response of fetch bin details APIs when txnToken is expired")
    public void TC_FBD_007() throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.NATIVE_HYBRID).build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath jsonPath = initTxn.execute().jsonPath();
        String txnToken = jsonPath.getString("body.txnToken");
        String binNumer = CommonHelpers.getCardFirstSixDigit(new PaymentDTO().getCreditCardNumber());
        Reporter.log("Fetching bin detail for bin number:" + binNumer);
        FetchBinDetailsRequest request = new FetchBinDetailsRequest.Builder(txnToken, binNumer).build();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(request, initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId());
        JsonPath fetchBinsJson = fetchBinDetail.execute().jsonPath();
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail.channelName")).isEqualTo("VISA");
    }

    @Test(description = "Verify pcf value for amex in response of fetch bin details APIs")
    public void FBD_PCF_AMEX() throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        Double flatCommission = 1.00;
        SavedCardHelpers.deleteSavedCard(user);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), Constants.MerchantType.AMEX_PCF).build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath jsonPath = initTxn.execute().jsonPath();
        String txnToken = jsonPath.getString("body.txnToken");
        String binNumer = CommonHelpers.getCardFirstSixDigit(PaymentDTO.getAmexCardNumber());
        Reporter.log("Fetching bin detail for bin numer:" + binNumer);
        FetchBinDetailsRequest request = new FetchBinDetailsRequest.Builder(txnToken, binNumer).build();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(request, initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId());
        Response fetchBinsJsonResp = fetchBinDetail.execute();
        Double totalAmount = Double.parseDouble(initTxnDTO.txnAmountFromBody()) + convenienceFeeCalculator(Double.parseDouble(initTxnDTO.txnAmountFromBody()), 0.0, flatCommission, "");
        fetchBinsJsonResp.then()
                .body("body.resultInfo.resultCode", equalToIgnoringCase("0000"),
                        "body.resultInfo.resultMsg", equalToIgnoringCase("Success"),
                        "body.binDetail.channelName", equalToIgnoringCase("AMEX"),
                        "body.binDetail.cvvL", equalToIgnoringCase("4"),
                        "body.binDetail.cvvR", equalToIgnoringCase("true"),
                        "body.binDetail.cnMin", equalToIgnoringCase("15"),
                        "body.pcf.feeAmount.value", equalToIgnoringCase(String.format("%.2f", flatCommission)),
                        "body.pcf.taxAmount.value", equalToIgnoringCase(String.format("%.2f", flatCommission*0.18)),
                        "body.pcf.totalTransactionAmount.value", equalToIgnoringCase(String.format("%.2f", totalAmount)));
    }

    @Test(description = "Verify pcf value for HDFC CC in response of fetch bin details APIs")
    public void FBD_PCF_CC() throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        Double flatCommission = 1.00;
        SavedCardHelpers.deleteSavedCard(user);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), Constants.MerchantType.AMEX_PCF).build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath jsonPath = initTxn.execute().jsonPath();
        String txnToken = jsonPath.getString("body.txnToken");
        String binNumer = CommonHelpers.getCardFirstSixDigit(PaymentDTO.DINERS_CARD_NUMBER);
        Reporter.log("Fetching bin detail for bin numer:" + binNumer);
        FetchBinDetailsRequest request = new FetchBinDetailsRequest.Builder(txnToken, binNumer).build();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(request, initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId());
        JsonPath fetchBinsJson = fetchBinDetail.execute().jsonPath();
        Double totalAmount = Double.parseDouble(initTxnDTO.txnAmountFromBody()) + convenienceFeeCalculator(Double.parseDouble(initTxnDTO.txnAmountFromBody()), 0.0, flatCommission, "");
        SoftAssert softAssert = new SoftAssert();
        softAssert.assertEquals(fetchBinsJson.getString("body.resultInfo.resultCode"), "0000");
        softAssert.assertEquals(fetchBinsJson.getString("body.resultInfo.resultMsg"), "Success");
        softAssert.assertEquals(fetchBinsJson.getString("body.binDetail.channelName"), "DINERS");
        softAssert.assertEquals(fetchBinsJson.getString("body.binDetail.cvvL"), "3");
        softAssert.assertEquals(fetchBinsJson.getString("body.binDetail.cvvR"), "true");
        softAssert.assertEquals(fetchBinsJson.getString("body.binDetail.cnMin"), "14");
        softAssert.assertEquals(fetchBinsJson.getDouble("body.pcf.feeAmount.value"), flatCommission);
        softAssert.assertEquals(fetchBinsJson.getDouble("body.pcf.taxAmount.value"), flatCommission * 0.18);
        softAssert.assertEquals(fetchBinsJson.getString("body.pcf.totalTransactionAmount.value"), format.format(totalAmount));
        softAssert.assertAll();
    }

    @Test(description = "Verify pcf value for ICICI DC in response of fetch bin details APIs")
    public void FBD_PCF_DC() throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        Double flatCommission = 4.00;
        SavedCardHelpers.deleteSavedCard(user);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), Constants.MerchantType.AMEX_PCF).build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath jsonPath = initTxn.execute().jsonPath();
        String txnToken = jsonPath.getString("body.txnToken");
        String binNumer = CommonHelpers.getCardFirstSixDigit(PaymentDTO.MASTER_ICICI_DEBIT_CARD_NUMBER);
        Reporter.log("Fetching bin detail for bin numer:" + binNumer);
        FetchBinDetailsRequest request = new FetchBinDetailsRequest.Builder(txnToken, binNumer).build();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(request, initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId());
        JsonPath fetchBinsJson = fetchBinDetail.execute().jsonPath();
        SoftAssert softAssert = new SoftAssert();
        Double totalAmount = Double.parseDouble(initTxnDTO.txnAmountFromBody()) + convenienceFeeCalculator(Double.parseDouble(initTxnDTO.txnAmountFromBody()), 0.0, flatCommission, "");
        softAssert.assertEquals(fetchBinsJson.getString("body.resultInfo.resultCode"), "0000");
        softAssert.assertEquals(fetchBinsJson.getString("body.resultInfo.resultMsg"), "Success");
        softAssert.assertEquals(fetchBinsJson.getString("body.binDetail.channelName"), "MASTER");
        softAssert.assertEquals(fetchBinsJson.getString("body.binDetail.cvvL"), "3");
        softAssert.assertEquals(fetchBinsJson.getString("body.binDetail.cvvR"), "true");
        softAssert.assertEquals(fetchBinsJson.getString("body.binDetail.cnMin"), "16");
        softAssert.assertEquals(fetchBinsJson.getDouble("body.pcf.feeAmount.value"), flatCommission);
        softAssert.assertEquals(fetchBinsJson.getDouble("body.pcf.taxAmount.value"), flatCommission * 0.18);
        softAssert.assertEquals(fetchBinsJson.getString("body.pcf.totalTransactionAmount.value"), format.format(totalAmount));
        softAssert.assertAll();
    }

    @Test(description = "Verify success response of FetchBinDetail API when type=GUEST, no token field, bin not having EMI and isEMIDetail = FALSE")
    public void guestWithoutTokenFieldTest() {
        String binNumber = CommonHelpers.getCardFirstSixDigit(new PaymentDTO().getDebitCardNumber());
        FetchBinDetailsRequest fetchBinDetailsRequest = new FetchBinDetailsRequest.Builder(binNumber, Constants.MerchantType.PGOnly, null, "false").build();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(fetchBinDetailsRequest, fetchBinDetailsRequest.getBody().getMid(), true);
        JsonPath fetchBinDetails = fetchBinDetail.execute().jsonPath();
        Assertions.assertThat(fetchBinDetails.getString("body.resultInfo.resultCode")).as("resultCode mismatch").isEqualTo("0000");
        Assertions.assertThat(fetchBinDetails.getString("body.resultInfo.resultMsg")).as("resultMsg mismatch").isEqualTo("Success");
        Assertions.assertThat(fetchBinDetails.getString("body.isEmiAvailable")).as("isEmiAvailable mismatch").isEqualTo("false");
        Assertions.assertThat(fetchBinDetails.getString("body")).as("emiChannel not expected but coming").doesNotContain("emiChannel");
        Assertions.assertThat(fetchBinDetails.getString("body.binDetail.channelName")).as("channelName mismatch").isEqualTo("VISA");
    }

    @Test(description = "Verify success response of FetchBinDetail API when type=GUEST, no token field, bin not having EMI and isEMIDetail = TRUE")
    public void guestNoEMIOnBinTest() {
        String binNumber = CommonHelpers.getCardFirstSixDigit(new PaymentDTO().getDebitCardNumber());
        FetchBinDetailsRequest fetchBinDetailsRequest = new FetchBinDetailsRequest.Builder(binNumber, Constants.MerchantType.PGOnly, null, "true").build();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(fetchBinDetailsRequest, fetchBinDetailsRequest.getBody().getMid(), true);
        JsonPath fetchBinDetails = fetchBinDetail.execute().jsonPath();
        Assertions.assertThat(fetchBinDetails.getString("body.resultInfo.resultCode")).as("resultCode mismatch").isEqualTo("0000");
        Assertions.assertThat(fetchBinDetails.getString("body.resultInfo.resultMsg")).as("resultMsg mismatch").isEqualTo("Success");
        Assertions.assertThat(fetchBinDetails.getString("body.isEmiAvailable")).as("isEmiAvailable mismatch").isEqualTo("false");
        Assertions.assertThat(fetchBinDetails.getString("body")).as("emiChannel not expected but coming").doesNotContain("emiChannel");
        Assertions.assertThat(fetchBinDetails.getString("body.binDetail.channelName")).as("channelName mismatch").isEqualTo("VISA");
    }

    @Test(description = "Verify success response of FetchBinDetail API when type=GUEST, no token field, bin having EMI but isEMIDetail = FALSE")
    public void guestIsEMIDetailFalseTest() {
        String binNumber = CommonHelpers.getCardFirstSixDigit(new PaymentDTO().getCreditCardNumber());
        FetchBinDetailsRequest fetchBinDetailsRequest = new FetchBinDetailsRequest.Builder(binNumber, Constants.MerchantType.PGOnly, null, "false").build();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(fetchBinDetailsRequest, fetchBinDetailsRequest.getBody().getMid(), true);
        JsonPath fetchBinDetails = fetchBinDetail.execute().jsonPath();
        Assertions.assertThat(fetchBinDetails.getString("body.resultInfo.resultCode")).as("resultCode mismatch").isEqualTo("0000");
        Assertions.assertThat(fetchBinDetails.getString("body.resultInfo.resultMsg")).as("resultMsg mismatch").isEqualTo("Success");
        Assertions.assertThat(fetchBinDetails.getString("body.isEmiAvailable")).as("isEmiAvailable mismatch").isEqualTo("true");
        Assertions.assertThat(fetchBinDetails.getString("body")).as("emiChannel not expected but coming").doesNotContain("emiChannel");
        Assertions.assertThat(fetchBinDetails.getString("body.binDetail.channelName")).as("channelName mismatch").isEqualTo("VISA");
    }

    @Test(description = "Verify success response of FetchBinDetail API when type=GUEST, no token field, bin having EMI and isEMIDetail = TRUE")
    public void guestIsEMIDetailTrueTest() {
        String binNumber = CommonHelpers.getCardFirstSixDigit(new PaymentDTO().getCreditCardNumber());
        FetchBinDetailsRequest fetchBinDetailsRequest = new FetchBinDetailsRequest.Builder(binNumber, Constants.MerchantType.PGOnly, null, "true").build();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(fetchBinDetailsRequest, fetchBinDetailsRequest.getBody().getMid(), true);
        JsonPath fetchBinDetails = fetchBinDetail.execute().jsonPath();
        Assertions.assertThat(fetchBinDetails.getString("body.resultInfo.resultCode")).as("resultCode mismatch").isEqualTo("0000");
        Assertions.assertThat(fetchBinDetails.getString("body.resultInfo.resultMsg")).as("resultMsg mismatch").isEqualTo("Success");
        Assertions.assertThat(fetchBinDetails.getString("body.isEmiAvailable")).as("isEmiAvailable mismatch").isEqualTo("true");
        Assertions.assertThat(fetchBinDetails.getString("body.emiChannel.emiType")).as("emiType mismatch").isEqualTo("CREDIT_CARD");
        Assertions.assertThat(fetchBinDetails.getString("body.emiChannel.channelCode")).as("channelCode mismatch").isEqualTo("HDFC");
        Assertions.assertThat(fetchBinDetails.getString("body.binDetail.channelName")).as("channelName mismatch").isEqualTo("VISA");
    }

    @Test(description = "Verify success response of FetchBinDetail API when type=GUEST, any token value, bin not having EMI and isEMIDetail = FALSE")
    public void guestRandomTokenTest() {
        String binNumber = CommonHelpers.getCardFirstSixDigit(new PaymentDTO().getDebitCardNumber());
        FetchBinDetailsRequest fetchBinDetailsRequest = new FetchBinDetailsRequest.Builder(binNumber, Constants.MerchantType.PGOnly, "sdf$#%$3", "false").build();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(fetchBinDetailsRequest, fetchBinDetailsRequest.getBody().getMid(), true);
        JsonPath fetchBinDetails = fetchBinDetail.execute().jsonPath();
        Assertions.assertThat(fetchBinDetails.getString("body.resultInfo.resultCode")).as("resultCode mismatch").isEqualTo("0000");
        Assertions.assertThat(fetchBinDetails.getString("body.resultInfo.resultMsg")).as("resultMsg mismatch").isEqualTo("Success");
        Assertions.assertThat(fetchBinDetails.getString("body.isEmiAvailable")).as("isEmiAvailable mismatch").isEqualTo("false");
        Assertions.assertThat(fetchBinDetails.getString("body")).as("emiChannel not expected but coming").doesNotContain("emiChannel");
        Assertions.assertThat(fetchBinDetails.getString("body.binDetail.channelName")).as("channelName mismatch").isEqualTo("VISA");
    }

    @Test(description = "Verify success response of FetchBinDetail API when type=GUEST, blank token value, bin not having EMI and isEMIDetail = TRUE")
    public void guestBlankTokenTest() {
        String binNumber = CommonHelpers.getCardFirstSixDigit(new PaymentDTO().getDebitCardNumber());
        FetchBinDetailsRequest fetchBinDetailsRequest = new FetchBinDetailsRequest.Builder(binNumber, Constants.MerchantType.PGOnly, "", "true").build();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(fetchBinDetailsRequest, fetchBinDetailsRequest.getBody().getMid(), true);
        JsonPath fetchBinDetails = fetchBinDetail.execute().jsonPath();
        Assertions.assertThat(fetchBinDetails.getString("body.resultInfo.resultCode")).as("resultCode mismatch").isEqualTo("0000");
        Assertions.assertThat(fetchBinDetails.getString("body.resultInfo.resultMsg")).as("resultMsg mismatch").isEqualTo("Success");
        Assertions.assertThat(fetchBinDetails.getString("body.isEmiAvailable")).as("isEmiAvailable mismatch").isEqualTo("false");
        Assertions.assertThat(fetchBinDetails.getString("body")).as("emiChannel not expected but coming").doesNotContain("emiChannel");
        Assertions.assertThat(fetchBinDetails.getString("body.binDetail.channelName")).as("channelName mismatch").isEqualTo("VISA");
    }

    @Test(description = "Verify success response of FetchBinDetail API when type=GUEST, blank token field, bin having EMI but isEMIDetail = FALSE")
    public void guestBlankTokenIsEMIDetailFalseTest() {
        String binNumber = CommonHelpers.getCardFirstSixDigit(new PaymentDTO().getCreditCardNumber());
        FetchBinDetailsRequest fetchBinDetailsRequest = new FetchBinDetailsRequest.Builder(binNumber, Constants.MerchantType.PGOnly, "", "false").build();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(fetchBinDetailsRequest, fetchBinDetailsRequest.getBody().getMid(), true);
        JsonPath fetchBinDetails = fetchBinDetail.execute().jsonPath();
        Assertions.assertThat(fetchBinDetails.getString("body.resultInfo.resultCode")).as("resultCode mismatch").isEqualTo("0000");
        Assertions.assertThat(fetchBinDetails.getString("body.resultInfo.resultMsg")).as("resultMsg mismatch").isEqualTo("Success");
        Assertions.assertThat(fetchBinDetails.getString("body.isEmiAvailable")).as("isEmiAvailable mismatch").isEqualTo("true");
        Assertions.assertThat(fetchBinDetails.getString("body")).as("emiChannel not expected but coming").doesNotContain("emiChannel");
        Assertions.assertThat(fetchBinDetails.getString("body.binDetail.channelName")).as("channelName mismatch").isEqualTo("VISA");
    }

    @Test(description = "Verify success response of FetchBinDetail API when type=GUEST, valid sso token, bin having EMI and isEMIDetail = TRUE")
    public void guestValidTokenIsEMIDetailTrueTest() throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String binNumber = CommonHelpers.getCardFirstSixDigit(new PaymentDTO().getEmiCard());
        FetchBinDetailsRequest fetchBinDetailsRequest = new FetchBinDetailsRequest.Builder(binNumber, Constants.MerchantType.PGOnly, user.ssoToken(), "true").build();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(fetchBinDetailsRequest, fetchBinDetailsRequest.getBody().getMid(), true);
        JsonPath fetchBinDetails = fetchBinDetail.execute().jsonPath();
        Assertions.assertThat(fetchBinDetails.getString("body.resultInfo.resultCode")).as("resultCode mismatch").isEqualTo("0000");
        Assertions.assertThat(fetchBinDetails.getString("body.resultInfo.resultMsg")).as("resultMsg mismatch").isEqualTo("Success");
        Assertions.assertThat(fetchBinDetails.getString("body.isEmiAvailable")).as("isEmiAvailable mismatch").isEqualTo("true");
        Assertions.assertThat(fetchBinDetails.getString("body.emiChannel.emiType")).as("emiType mismatch").isEqualTo("CREDIT_CARD");
        Assertions.assertThat(fetchBinDetails.getString("body.emiChannel.channelCode")).as("channelCode mismatch").isEqualTo("HDFC");
        Assertions.assertThat(fetchBinDetails.getString("body.binDetail.channelName")).as("channelName mismatch").isEqualTo("VISA");
    }

    @Test(description = "Verify failure response of FetchBinDetail API when MID is not passed in query params")
    public void guestMidNotInQueryParamsTest() throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String binNumber = CommonHelpers.getCardFirstSixDigit(new PaymentDTO().getCreditCardNumber());
        FetchBinDetailsRequest fetchBinDetailsRequest = new FetchBinDetailsRequest.Builder(binNumber, Constants.MerchantType.PGOnly, user.ssoToken(), "true").setMid(null).build();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(fetchBinDetailsRequest, fetchBinDetailsRequest.getBody().getMid(), true);
        JsonPath fetchBinDetails = fetchBinDetail.execute().jsonPath();
        Assertions.assertThat(fetchBinDetails.getString("body.resultInfo.resultCode")).as("resultCode mismatch").isEqualTo("1007");
        Assertions.assertThat(fetchBinDetails.getString("body.resultInfo.resultMsg")).as("resultMsg mismatch").isEqualTo("Mid and OrderId are mandatory in query parameter");
    }

    @Test(description = "Verify failure response of FetchBinDetail API when MID is not passed in request body")
    public void guestMidNotInRequestBodyTest() throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String binNumber = CommonHelpers.getCardFirstSixDigit(new PaymentDTO().getCreditCardNumber());
        FetchBinDetailsRequest fetchBinDetailsRequest = new FetchBinDetailsRequest.Builder(binNumber, Constants.MerchantType.PGOnly, user.ssoToken(), "true").setMid(null).build();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(fetchBinDetailsRequest, Constants.MerchantType.PGOnly.getId(), true);
        JsonPath fetchBinDetails = fetchBinDetail.execute().jsonPath();
        Assertions.assertThat(fetchBinDetails.getString("body.resultInfo.resultCode")).as("resultCode mismatch").isEqualTo("2013");
        Assertions.assertThat(fetchBinDetails.getString("body.resultInfo.resultMsg")).as("resultMsg mismatch").isEqualTo("Mid in the query param doesn't match with the Mid send in the request");
    }

    @Test(description = "Verify failure response of FetchBinDetail API when tokenType = SSO and MID is not passed in query params")
    public void ssoTypeMidNotInQueryParamsTest() throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String binNumber = CommonHelpers.getCardFirstSixDigit(new PaymentDTO().getCreditCardNumber());
        FetchBinDetailsRequest fetchBinDetailsRequest = new FetchBinDetailsRequest.Builder(binNumber, Constants.MerchantType.PGOnly, user.ssoToken(), "true").setTokenType("SSO").setMid(null).build();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(fetchBinDetailsRequest, fetchBinDetailsRequest.getBody().getMid(), true);
        JsonPath fetchBinDetails = fetchBinDetail.execute().jsonPath();
        Assertions.assertThat(fetchBinDetails.getString("body.resultInfo.resultCode")).as("resultCode mismatch").isEqualTo("1007");
        Assertions.assertThat(fetchBinDetails.getString("body.resultInfo.resultMsg")).as("resultMsg mismatch").isEqualTo("Mid and OrderId are mandatory in query parameter");
    }

    @Test(description = "Verify failure response of FetchBinDetail API when tokenType = SSO and MID is not passed in request body")
    public void ssoTypeMidNotInRequestBodyTest() throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String binNumber = CommonHelpers.getCardFirstSixDigit(new PaymentDTO().getCreditCardNumber());
        FetchBinDetailsRequest fetchBinDetailsRequest = new FetchBinDetailsRequest.Builder(binNumber, Constants.MerchantType.PGOnly, user.ssoToken(), "true").setMid(null).setTokenType("SSO").build();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(fetchBinDetailsRequest, Constants.MerchantType.PGOnly.getId(), true);
        JsonPath fetchBinDetails = fetchBinDetail.execute().jsonPath();
        Assertions.assertThat(fetchBinDetails.getString("body.resultInfo.resultCode")).as("resultCode mismatch").isEqualTo("2013");
        Assertions.assertThat(fetchBinDetails.getString("body.resultInfo.resultMsg")).as("resultMsg mismatch").isEqualTo("Mid in the query param doesn't match with the Mid send in the request");
    }

    @Test(description = "Verify success response of FetchBinDetail when tokenType = GUEST and bin not configured on merchant")
    public void guestBinNotConfiguredOnMerchantTest() {
        String binNumber = CommonHelpers.getCardFirstSixDigit(PaymentDTO.CREDIT_CARD_FOR_FAILED_TXN);
        FetchBinDetailsRequest fetchBinDetailsRequest = new FetchBinDetailsRequest.Builder(binNumber, Constants.MerchantType.NATIVE_WALLET_ONLY, null, "true").build();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(fetchBinDetailsRequest, fetchBinDetailsRequest.getBody().getMid(), true);
        JsonPath fetchBinDetails = fetchBinDetail.execute().jsonPath();
        Assertions.assertThat(fetchBinDetails.getString("body.resultInfo.resultCode")).as("resultCode mismatch").isEqualTo("2011");
        Assertions.assertThat(fetchBinDetails.getString("body.resultInfo.resultMsg")).as("resultMsg mismatch").isEqualTo("VISA Credit card is not allowed for this payment. Please try paying using other cards/options.");
    }

    @Test(description = "Verify failure response of FetchEMIDetails API when tokenType=GUEST is passed")
    public void guestFetchEMIDetailsTest() {
        FetchEMIDetailRequest fetchEMIDetailRequest = new FetchEMIDetailRequest("GUEST", "", "HDFC", Constants.MerchantType.NATIVE_HYBRID.getId());
        JsonPath fetchEMIDetail = new FetchEMIDetail(fetchEMIDetailRequest, Constants.MerchantType.NATIVE_HYBRID.getId()).execute().jsonPath();
        Assertions.assertThat(fetchEMIDetail.getString("body.resultInfo.resultCode")).as("resultCode mismatch").isEqualTo("1006");
        Assertions.assertThat(fetchEMIDetail.getString("body.resultInfo.resultMsg")).as("resultMsg mismatch").isEqualTo("Your Session has expired.");
    }

    @Test(description = "Verify that merchant key is stored in Redis after fetchBinDetails API is hit")
    public void guestKeyInSessionRedisTest() throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String binNumber = CommonHelpers.getCardFirstSixDigit(new PaymentDTO().getCreditCardNumber());
        FetchBinDetailsRequest fetchBinDetailsRequest = new FetchBinDetailsRequest.Builder(binNumber, Constants.MerchantType.PGOnly, user.ssoToken(), "true").build();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(fetchBinDetailsRequest, fetchBinDetailsRequest.getBody().getMid(), true);
        JsonPath fetchBinDetails = fetchBinDetail.execute().jsonPath();
        Assertions.assertThat(fetchBinDetails.getString("body.resultInfo.resultCode")).as("resultCode mismatch").isEqualTo("0000");
        CommonHelpers.validateCacheFromSessionRedis(fetchBinDetailsRequest.getBody().getMid() + "_GUEST");
    }

    @Test(description = "Validate Bin Tokenization is False by Default for Any Bin")
    public void verifyBinTokenizationFalse() throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.NATIVE_ADDNPAY).build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        Assertions.assertThat(response.jsonPath().getString("body.txnToken")).as("TXN Token is null").isNotNull();
        String txnToken = response.jsonPath().getString("body.txnToken");
        String mid = initTxnDTO.getBody().getMid();
        String orderId = initTxnDTO.getBody().getOrderId();
        FetchBinDetailResponse fetchBinDetailResponse = NativeHelpers.fetchBinDetailResponse(txnToken, mid, orderId, BinTokenizationFalse);
        Assertions.assertThat(fetchBinDetailResponse.getBody().getResultInfo().getResultCode())
                .as("result code mismatch")
                .isEqualTo("0000");
        Assertions.assertThat(fetchBinDetailResponse.getBody().getResultInfo().getResultMsg())
                .as("resultMsg mismatch")
                .isEqualToIgnoringCase("Success");
        BinDetail binDetail = fetchBinDetailResponse.getBody().getBinDetail();
        Assertions.assertThat(binDetail.getBinTokenization())
                .isNotNull()
                .as("Bin Tokenization is not enabled on the bin")
                .isEqualTo("false");
    }

    @Test(description = "Validate Bin Tokenization is True for enabled bin")
    public void verifyBinTokenizationTrue() throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.NATIVE_ADDNPAY).build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        Assertions.assertThat(response.jsonPath().getString("body.txnToken")).as("TXN Token is null").isNotNull();
        String txnToken = response.jsonPath().getString("body.txnToken");
        String mid = initTxnDTO.getBody().getMid();
        String orderId = initTxnDTO.getBody().getOrderId();
        FetchBinDetailResponse fetchBinDetailResponse = NativeHelpers.fetchBinDetailResponse(txnToken, mid, orderId, BinTokenizationTrue);
        Assertions.assertThat(fetchBinDetailResponse.getBody().getResultInfo().getResultCode())
                .as("result code mismatch")
                .isEqualTo("0000");
        Assertions.assertThat(fetchBinDetailResponse.getBody().getResultInfo().getResultMsg())
                .as("resultMsg mismatch")
                .isEqualToIgnoringCase("Success");
        BinDetail binDetail = fetchBinDetailResponse.getBody().getBinDetail();
        Assertions.assertThat(binDetail.getBinTokenization())
                .isNotNull()
                .as("Bin Tokenization is not enabled on the bin")
                .isEqualTo("true");

    }

    @Owner(Constants.Owner.TAMANA_TATHAN)
    @Feature("PGP-37859")
    @Test(description = " Testing the updated fetch bin API which is working using checksum FOR CC")
    public void validateFetchBinAPIForSubscriptionCC() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.BANK_MANDATE;
        String orderId = CommonHelpers.generateOrderId();
        String binNumber = new PaymentDTO().getSubsBinNumberCC();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(merchant, orderId, binNumber);
        Response response = fetchBinDetail.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultStatus")).as("Result Status").isEqualTo("S");
        Assertions.assertThat(response.jsonPath().getString("body.binDetail.paymentMode")).as("Payment Mode").isEqualTo("CREDIT_CARD");
        Assertions.assertThat(response.jsonPath().getString("body.isSubscriptionAvailable")).as("Field Validation for subscription").isEqualTo("true");
    }

    @Owner(Constants.Owner.TAMANA_TATHAN)
    @Feature("PGP-37859")
    @Test(description = " Testing the updated fetch bin API which is working using checksum FOR DC")
    public void validateFetchBinAPIForSubscriptionDC() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.BANK_MANDATE;
        String orderId = CommonHelpers.generateOrderId();
        String binNumber = new PaymentDTO().getSubsBinNumberDC();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(merchant, orderId, binNumber);
        Response response = fetchBinDetail.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultStatus")).as("Result Status").isEqualTo("S");
        Assertions.assertThat(response.jsonPath().getString("body.binDetail.paymentMode")).as("Payment Mode").isEqualTo("DEBIT_CARD");
        Assertions.assertThat(response.jsonPath().getString("body.isSubscriptionAvailable")).as("Field Validation for subscription").isEqualTo("true");
    }

    @Owner(Constants.Owner.ABHISHEK_KULKARNI)
    @Feature("PGP-39161")
    @Test(description = "Verify the Fetch bin Details api with Access Token With PCF Merchant")
    public void validateFetchBinDetailsWithAccessTokenForPcf() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.DCC_PCF;
        String refId = UUID.randomUUID().toString().substring(0, 18);
        CreateToken createToken = new CreateToken(merchantType, user.ssoToken(), refId);
        JsonPath jsonpath = createToken.execute().jsonPath();
        String AccessToken = jsonpath.getString("body.accessToken");

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder("ACCESS", AccessToken)
                .setMid(merchantType.getId()).build();
        FetchPaymentOptionV2 fpoReq = new FetchPaymentOptionV2(merchantType.getId(), fetchPaymentOptionsDTO);
        fpoReq.getRequestSpecBuilder()
                .addQueryParam("referenceId", refId);
        Response response = fpoReq.execute();
        FetchBinDetailsRequest fetchBinDetailsRequest = new FetchBinDetailsRequest.Builder("436828", Constants.MerchantType.DCC_PCF, AccessToken, "false").setTokenType("ACCESS").build();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(merchantType.getId(),fetchBinDetailsRequest,refId);
        JsonPath fetchBinsJson = fetchBinDetail.execute().jsonPath();
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
    }

    @Feature("PGP-46375")
    @Owner(Constants.Owner.ROUNAK)
    @Description("Automation JIRA : PGP-47032")
    @Test(description = "Validate nativeOtpEligible Param is present in Resp of fetchBinDetail when FF4J is ON & nativeOTPDetailRequired:true is passed in the Req with 6-Digit Bin")
    public static void ValidateNativeOtpEligibleinFetchBinDetailWith6DigitBin_TC01() throws Exception {
        //FF4J Feature Flag= theia.enableFetchBinDetailsFromBinCenterForNativeOtpEligibility (MID based)          Bin to use:-401200 BBK CC visa
        //"nativeOtpEligible" parameter is [AND of FF4J & "nativeOTPDetailRequired" passed in the Request]        //via sso
        String nativeOTPDetailRequired = "true";
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.PGOnly;
        String orderId = CommonHelpers.generateOrderId();
        String CardNumber = new PaymentDTO().getNativeOtpBinNumber();
        String binNumber = CardNumber.substring(0,6);

        FetchBin fetchBin = new FetchBin(user.ssoToken(), merchant.getId(), orderId, binNumber, nativeOTPDetailRequired);
        JsonPath jsonPath = fetchBin.execute().jsonPath();

        BinQuery binQuery = new BinQuery(binNumber);
        JsonPath jsonPath1 = binQuery.execute().jsonPath();

        if(jsonPath1.getString("binInfo.isNativeOtpEligible") == "true") {
            Assertions.assertThat(jsonPath.getString("body.nativeOtpEligible")).isEqualTo("true");
            Assertions.assertThat(jsonPath.getString("body.binDetail.bin")).isEqualTo(binNumber);
        }
        else {
            Assertions.assertThat(jsonPath.getString("body.nativeOtpEligible")).isEqualTo("false");
            Assertions.assertThat(jsonPath.getString("body.binDetail.bin")).isEqualTo(binNumber);
        }
    }

    @Feature("PGP-46375")
    @Owner(Constants.Owner.ROUNAK)
    @Description("Automation JIRA : PGP-47032")
    @Test(description = "Validate nativeOtpEligible Param is present in Resp of fetchBinDetail when FF4J is ON & nativeOTPDetailRequired:true is passed in the Req with 9-Digit Bin")
    public static void ValidateNativeOtpEligibleinFetchBinDetailWith9DigitBin_TC02() throws Exception{
        String nativeOTPDetailRequired = "true";
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.PGOnly;
        String orderId = CommonHelpers.generateOrderId();
        String CardNumber = new PaymentDTO().getNativeOtpBinNumber();
        String binNumber = CardNumber.substring(0,9);

        FetchBin fetchBin = new FetchBin(user.ssoToken(), merchant.getId(), orderId, binNumber, nativeOTPDetailRequired);
        JsonPath jsonPath = fetchBin.execute().jsonPath();

        BinQuery binQuery = new BinQuery(binNumber);
        JsonPath jsonPath1 = binQuery.execute().jsonPath();

        if(jsonPath1.getString("binInfo.isNativeOtpEligible") == "true") {
            Assertions.assertThat(jsonPath.getString("body.nativeOtpEligible")).isEqualTo("true");
            Assertions.assertThat(jsonPath.getString("body.binDetail.bin")).isEqualTo(CardNumber.substring(0,6));
        }
        else {
            Assertions.assertThat(jsonPath.getString("body.nativeOtpEligible")).isEqualTo("false");
            Assertions.assertThat(jsonPath.getString("body.binDetail.bin")).isEqualTo(CardNumber.substring(0,6));
        }
    }

    @Feature("PGP-46375")
    @Owner(Constants.Owner.ROUNAK)
    @Description("Automation JIRA : PGP-47032")
    @Test(description = "Validate nativeOtpEligible Param is present in Resp of fetchBinDetail when FF4J is ON & nativeOTPDetailRequired:true is passed in the Req with CardNumber")
    public static void ValidateNativeOtpEligibleinFetchBinDetailWithCardNo_TC03() throws Exception{
        String nativeOTPDetailRequired = "true";
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.PGOnly;
        String orderId = CommonHelpers.generateOrderId();
        String CardNumber = new PaymentDTO().getNativeOtpBinNumber();

        FetchBin fetchBin = new FetchBin(user.ssoToken(), merchant.getId(), orderId, CardNumber, nativeOTPDetailRequired);
        JsonPath jsonPath = fetchBin.execute().jsonPath();

        String binNumber = CardNumber.substring(0,9);
        BinQuery binQuery = new BinQuery(binNumber);
        JsonPath jsonPath1 = binQuery.execute().jsonPath();

        if(jsonPath1.getString("binInfo.isNativeOtpEligible") == "true") {
            Assertions.assertThat(jsonPath.getString("body.nativeOtpEligible")).isEqualTo("true");
            Assertions.assertThat(jsonPath.getString("body.binDetail.bin")).isEqualTo(CardNumber.substring(0,6));
        }
        else {
            Assertions.assertThat(jsonPath.getString("body.nativeOtpEligible")).isEqualTo("false");
            Assertions.assertThat(jsonPath.getString("body.binDetail.bin")).isEqualTo(CardNumber.substring(0,6));
        }
    }

    @Feature("PGP-46375")
    @Owner(Constants.Owner.ROUNAK)
    @Description("Automation JIRA : PGP-47032")
    @Test(description = "Validate nativeOtpEligible Param is present in Resp of fetchBinDetail when FF4J is ON & nativeOTPDetailRequired:true for NativeOTP Blocked 6-Digit Bin")
    public void ValidateNativeOtpEligibleinFetchBinDetailWith6DigitBin_TC04() throws Exception {
        //NativeOTP BLocked Bins:- in Response of fetchBinDetail, we get isNativeOtpBlocked=true    blockedbin=999994
        String nativeOTPDetailRequired = "true";
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.PGOnly;
        String orderId = CommonHelpers.generateOrderId();
        String CardNumber = new PaymentDTO().getNativeOtpBlockedBinNumber();        //Blockedbin,ie. isNativeOtpBlocked": false in Response of bin-center/v1/card/bin
        String binNumber = CardNumber.substring(0,6);

        FetchBin fetchBin = new FetchBin(user.ssoToken(), merchant.getId(), orderId, binNumber, nativeOTPDetailRequired);
        JsonPath jsonPath = fetchBin.execute().jsonPath();

        BinQuery binQuery = new BinQuery(binNumber);
        JsonPath jsonPath1 = binQuery.execute().jsonPath();

        if(jsonPath1.getString("binInfo.isNativeOtpEligible") == "true") {
            Assertions.assertThat(jsonPath.getString("body.nativeOtpEligible")).isEqualTo("true");
            Assertions.assertThat(jsonPath.getString("body.binDetail.bin")).isEqualTo(binNumber);
        }
        else {
            Assertions.assertThat(jsonPath.getString("body.nativeOtpEligible")).isEqualTo("false");
            Assertions.assertThat(jsonPath.getString("body.binDetail.bin")).isEqualTo(binNumber);
        }
    }

    @Feature("PGP-46375")
    @Owner(Constants.Owner.ROUNAK)
    @Description("Automation JIRA : PGP-47032")
    @Test(description = "Validate nativeOtpEligible Param is present in Resp of fetchBinDetail when FF4J is ON & nativeOTPDetailRequired:true for NativeOTP Blocked 9-Digit Bin")
    public void ValidateNativeOtpEligibleinFetchBinDetailWith9DigitBin_TC05() throws Exception {
        String nativeOTPDetailRequired = "true";
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.PGOnly;
        String orderId = CommonHelpers.generateOrderId();
        String CardNumber = new PaymentDTO().getNativeOtpBlockedBinNumber();
        String binNumber = CardNumber.substring(0,9);

        FetchBin fetchBin = new FetchBin(user.ssoToken(), merchant.getId(), orderId, binNumber, nativeOTPDetailRequired);
        JsonPath jsonPath = fetchBin.execute().jsonPath();

        BinQuery binQuery = new BinQuery(binNumber);
        JsonPath jsonPath1 = binQuery.execute().jsonPath();

        if(jsonPath1.getString("binInfo.isNativeOtpEligible") == "true") {
            Assertions.assertThat(jsonPath.getString("body.nativeOtpEligible")).isEqualTo("true");
            Assertions.assertThat(jsonPath.getString("body.binDetail.bin")).isEqualTo(CardNumber.substring(0,6));
        }
        else {
            Assertions.assertThat(jsonPath.getString("body.nativeOtpEligible")).isEqualTo("false");
            Assertions.assertThat(jsonPath.getString("body.binDetail.bin")).isEqualTo(CardNumber.substring(0,6));
        }
    }

    @Feature("PGP-46375")
    @Owner(Constants.Owner.ROUNAK)
    @Description("Automation JIRA : PGP-47032")
    @Test(description = "Validate nativeOtpEligible Param is present in Resp of fetchBinDetail when FF4J is ON & nativeOTPDetailRequired:true for NativeOTP Blocked 6-Digit Bins via TxnToken")
    public void ValidateNativeOtpEligibleinFetchBinDetailWithTxnToken_TC06() throws Exception {
        String nativeOTPDetailRequired = "true";
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly;
        Double txnAmount = 2.0;
        String CardNumber = new PaymentDTO().getNativeOtpBinNumber();
        String binNumber = CardNumber.substring(0,6);

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount.toString())
                .build();
        String orderId = initTxnDTO.orderFromBody();
        InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(txnToken, merchantType.getId(), orderId, binNumber, nativeOTPDetailRequired);
        JsonPath jsonPath = fetchBinDetail.execute().jsonPath();

        BinQuery binQuery = new BinQuery(binNumber);
        JsonPath jsonPath1 = binQuery.execute().jsonPath();

        if(jsonPath1.getString("binInfo.isNativeOtpEligible") == "true") {
            Assertions.assertThat(jsonPath.getString("body.nativeOtpEligible")).isEqualTo("true");
            Assertions.assertThat(jsonPath.getString("body.binDetail.bin")).isEqualTo(binNumber);
        }
        else {
            Assertions.assertThat(jsonPath.getString("body.nativeOtpEligible")).isEqualTo("false");
            Assertions.assertThat(jsonPath.getString("body.binDetail.bin")).isEqualTo(binNumber);
        }
    }

    @Feature("PGP-46375")
    @Owner(Constants.Owner.ROUNAK)
    @Description("Automation JIRA : PGP-47032")
    @Test(description = "Validate nativeOtpEligible Param is not present in Resp of fetchBinDetail when FF4J is ON & nativeOTPDetailRequired:false is passed in the Req for CardNo")
    public void ValidateNativeOtpEligibleNotPresentinFetchBinDetail_TC01() throws Exception {
        String nativeOTPDetailRequired = "false";
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.PGOnly;
        String orderId = CommonHelpers.generateOrderId();
        String CardNumber = new PaymentDTO().getNativeOtpBlockedBinNumber();

        FetchBin fetchBin = new FetchBin(user.ssoToken(), merchant.getId(), orderId, CardNumber, nativeOTPDetailRequired);
        JsonPath jsonPath = fetchBin.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.nativeOtpEligible")).isEqualToIgnoringCase("false");
        Assertions.assertThat(jsonPath.getString("body.binDetail.bin")).isEqualTo(CardNumber.substring(0,6));
    }

    @Feature("PGP-46375")
    @Owner(Constants.Owner.ROUNAK)
    @Description("Automation JIRA : PGP-47032")
    @Test(description = "Validate nativeOtpEligible Param is not present in Resp of fetchBinDetail when FF4J is OFF & nativeOTPDetailRequired:true is passed in the Req for CardNo")
    public void ValidateNativeOtpEligibleNotPresentinFetchBinDetail_TC02() throws Exception {
        String nativeOTPDetailRequired = "true";
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.LOYALTY_POINTS;
        String orderId = CommonHelpers.generateOrderId();
        String CardNumber = new PaymentDTO().getNativeOtpBlockedBinNumber();

        FetchBin fetchBin = new FetchBin(user.ssoToken(), merchant.getId(), orderId, CardNumber, nativeOTPDetailRequired);
        JsonPath jsonPath = fetchBin.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.nativeOtpEligible")).isEqualTo("false");
        Assertions.assertThat(jsonPath.getString("body.binDetail.bin")).isEqualTo(CardNumber.substring(0,6));
    }

    @Feature("PGP-46375")
    @Owner(Constants.Owner.ROUNAK)
    @Description("Automation JIRA : PGP-47032")
    @Test(description = "Validate nativeOtpEligible Param is not present in Resp of fetchBinDetail when FF4J is OFF & nativeOTPDetailRequired:false is passed in the Req for CardNo")
    public void ValidateNativeOtpEligibleNotPresentinFetchBinDetail_TC03() throws Exception {
        FF4JFlags.disable("theia.enableFetchBinDetailsFromBinCenterForNativeOtpEligibility");
        String nativeOTPDetailRequired = "false";
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.PGOnly;
        String orderId = CommonHelpers.generateOrderId();
        String CardNumber = new PaymentDTO().getNativeOtpBlockedBinNumber();

        FetchBin fetchBin = new FetchBin(user.ssoToken(), merchant.getId(), orderId, CardNumber, nativeOTPDetailRequired);
        JsonPath jsonPath = fetchBin.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.nativeOtpEligible")).isEqualTo("false");
        Assertions.assertThat(jsonPath.getString("body.binDetail.bin")).isEqualTo(CardNumber.substring(0,6));
    }

    @Feature("PGP-46375")
    @Owner(Constants.Owner.ROUNAK)
    @Description("Automation JIRA : PGP-47032")
    @Test(description = "Validate nativeOtpEligible Param is not present in Resp of fetchBinDetail when FF4J is OFF & nativeOTPDetailRequired:null is passed in the Req for CardNo")
    public void ValidateNativeOtpEligibleNotPresentinFetchBinDetail_TC04() throws Exception {
        String nativeOTPDetailRequired = "null";
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.PGOnly;
        String orderId = CommonHelpers.generateOrderId();
        String CardNumber = new PaymentDTO().getNativeOtpBlockedBinNumber();

        FetchBin fetchBin = new FetchBin(user.ssoToken(), merchant.getId(), orderId, CardNumber, nativeOTPDetailRequired);
        JsonPath jsonPath = fetchBin.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.nativeOtpEligible")).isEqualToIgnoringCase("false");
        Assertions.assertThat(jsonPath.getString("body.binDetail.bin")).isEqualTo(CardNumber.substring(0,6));
    }

    @Feature("PGP-46375")
    @Owner(Constants.Owner.ROUNAK)
    @Description("Automation JIRA : PGP-47032")
    @Test(description = "Validate nativeOtpEligible Param is not present in Resp of fetchBinDetail when FF4J is ON & nativeOTPDetailRequired:false is passed in the Req via TxnToken")
    public void ValidateNativeOtpEligibleNotPresentinFetchBinDetail_TC05() throws Exception {
        String nativeOTPDetailRequired = "false";
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly;
        Double txnAmount = 2.0;
        String CardNumber = new PaymentDTO().getNativeOtpBinNumber();
        String binNumber = CardNumber.substring(0,6);

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount.toString())
                .build();
        String orderId = initTxnDTO.orderFromBody();
        InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(txnToken, merchantType.getId(), orderId, binNumber, nativeOTPDetailRequired);
        JsonPath jsonPath = fetchBinDetail.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.nativeOtpEligible")).isEqualTo("false");
        Assertions.assertThat(jsonPath.getString("body.binDetail.bin")).isEqualTo(binNumber);
    }


    @Feature("PGP-55308")
    @Owner(Constants.Owner.ABHISHEK_VERMA)
    @Description("Automation Of PGP-55308")
    @Test(description = "Validate bin client is not called instead bin api call is going for fetching bin from bin-center")
    public void ValidateHitIsGoingOnBinAPIInsteadOfBinClient() throws Exception {
        String nativeOTPDetailRequired="true";
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly;
        String txnAmount = "2.0";
        String CardNumber = new PaymentDTO().getNativeOtpBinNumber();
        String binNumber = CardNumber.substring(0,9);
        RedisAPI.deleteKey("BIN_DB_CACHE:"+binNumber);
        RedisAPI.deleteKey("BIN_DB_DIGITAL_ASSETS_CACHE:"+binNumber);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
            .setTxnValue(txnAmount)
            .build();
        String orderId = initTxnDTO.orderFromBody();
        InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(txnToken, merchantType.getId(), orderId, binNumber, nativeOTPDetailRequired);
        JsonPath jsonPath = fetchBinDetail.execute().jsonPath();
        SoftAssertions softAssertions= new SoftAssertions();
        softAssertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        String theia_facade = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.payment_option_facade, initTxnDTO.getBody().getOrderId(), "BIN_CENTER");
        System.out.println(theia_facade);
        softAssertions.assertThat(theia_facade).contains("https://qa-pg-int.paytm.com/bin-center/v1/bin/"+binNumber+"/query");
        softAssertions.assertAll();
    }

    @Feature("PGP-55308")
    @Owner(Constants.Owner.ABHISHEK_VERMA)
    @Description("Automation Of PGP-55308")
    @Test(description = "Validate data is picked from cache and no hit is going on bin-center")
    public void ValidateDataIsPickedFromCache() throws Exception {
        String nativeOTPDetailRequired="true";
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly;
        String txnAmount = "2.0";
        String CardNumber = new PaymentDTO().getNativeOtpBinNumber();
        String binNumber = CardNumber.substring(0,9);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
            .setTxnValue(txnAmount)
            .build();
        String orderId = initTxnDTO.orderFromBody();
        InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(txnToken, merchantType.getId(), orderId, binNumber, nativeOTPDetailRequired);
        JsonPath jsonPath = fetchBinDetail.execute().jsonPath();

        InitTxnDTO initTxnDTO1 = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
            .setTxnValue(txnAmount)
            .build();
        String orderId1 = initTxnDTO1.orderFromBody();
        InitTxnResponseDTO initTxnResponseDTO1 = InitTxn.executeInitTxn(initTxnDTO1);
        String txnToken1 = initTxnResponseDTO1.getBody().getTxnToken();
        FetchBinDetail fetchBinDetail1 = new FetchBinDetail(txnToken1, merchantType.getId(), orderId1, binNumber, nativeOTPDetailRequired);
        JsonPath jsonPath1 = fetchBinDetail1.execute().jsonPath();

        SoftAssertions softAssertions= new SoftAssertions();
        softAssertions.assertThat(jsonPath1.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        String theia = verifyLogsOnPod(setEnvService.payment_option, orderId1, " binInfoMap from cache :: ");
        System.out.println("Thiea Logs: "+theia);
        softAssertions.assertThat(theia).contains(binNumber);
        softAssertions.assertAll();
    }

    @Owner(Constants.Owner.LOKESH_SAXENA)
    @Test(description = "Verify getBinBulkHash API returns hash for bulk card bins via savedcardOpenAPIService")
    public void validateGetBinBulkHashForBulkCardBins() throws Exception {
        List<String> cardBinList = Arrays.asList("40274100","40265700");
        GetBinBulkHash getBinBulkHash = new GetBinBulkHash(cardBinList);
        Response response = getBinBulkHash.execute();
        JsonPath jsonPath = response.jsonPath();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.statusCode()).as("HTTP status").isEqualTo(200);
        softly.assertThat(jsonPath.getString("body.resultInfo.resultStatus"))
                .as("resultStatus")
                .isEqualTo("S");
        softly.assertThat(jsonPath.getString("body.resultInfo.resultCode"))
                .as("resultCode")
                .isEqualTo("00000000");
        softly.assertThat(jsonPath.getString("body.resultInfo.resultMsg"))
                .as("resultMsg")
                .isEqualTo("Success");

        Map<String, String> cardBinEightDigitBinHashMap =
                jsonPath.getMap("body.cardBinEightDigitBinHashMap");
        softly.assertThat(cardBinEightDigitBinHashMap)
                .as("cardBinEightDigitBinHashMap")
                .isNotNull()
                .hasSize(cardBinList.size());
        for (String bin : cardBinList) {
            softly.assertThat(cardBinEightDigitBinHashMap.get(bin))
                    .as("hash for bin %s", bin)
                    .isNotNull()
                    .isNotBlank();
        }
        softly.assertAll();
    }

}