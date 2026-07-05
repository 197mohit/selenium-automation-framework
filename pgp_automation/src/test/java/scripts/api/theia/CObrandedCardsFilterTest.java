package scripts.api.theia;

import com.paytm.LocalConfig;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.api.nativeAPI.FetchPaymentOptionV2;
import com.paytm.api.nativeAPI.FetchPaymentOptionV5;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.api.qr.GenerateQR;
import com.paytm.api.theia.FetchQRPaymentDetails;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.RedisHelper;
import com.paytm.apphelpers.SavedCardHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.FetchQRPaymentDetailsDTO.FetchQRPaymentDetailsDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.checkoutjs.MerchantConfig;
import com.paytm.utils.ff4j.FF4JFlags;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.apache.commons.lang.RandomStringUtils;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.json.JSONArray;
import org.json.JSONObject;
import org.luaj.vm2.ast.Str;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.Instant;
import java.util.Map;

public class CObrandedCardsFilterTest extends PGPBaseTest {
    public static String custId = RandomStringUtils.randomAlphabetic(10) + Instant.now().toEpochMilli();
    RedisHelper redisHelper= RedisHelper.getInstance(LocalConfig.TRANSACTIONAL_REDIS_CLUSTER_URI,LocalConfig.PG_REDIS_CLUSTER_PASS);

    @BeforeMethod
    public void setFF4j()
    {
        FF4JFlags.enable("returnSavedCardsFromPlatformForMidCustId");
        redisHelper.delete("FF4J_FEATURE_returnSavedCardsFromPlatformForMidCustId");
        FF4JFlags.enable("returnSavedCardsFromPlatformForUserId");
        redisHelper.delete("FF4J_FEATURE_returnSavedCardsFromPlatformForUserId");
        FF4JFlags.enable("theia.migrateMappingToBinCenter");
        redisHelper.delete("FF4J_FEATURE_theia.migrateMappingToBinCenter");
    }

    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PGP-47312")
    @Test(description = "Verify Credit and Debit card filter in FPO5")
    public void Saved_Card_in_FPO5() throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.CobrandedFPO;
        String orderID = CommonHelpers.generateOrderId();

        custId = RandomStringUtils.randomAlphabetic(10) + Instant.now().toEpochMilli();
        SavedCardHelpers.addCardOnMidCustId(merchantType, custId, PaymentDTO.EXP_MONTH, PaymentDTO.Tokenization_Year, PaymentDTO.COBRANDED_CC);
        String tinCCPaytm = SavedCardHelpers.getTin();

        SavedCardHelpers.addCardOnMidCustId(merchantType, custId, PaymentDTO.EXP_MONTH, PaymentDTO.Tokenization_Year, PaymentDTO.NONCOBRANDED_CC);
        String tinCCNonPaytm = SavedCardHelpers.getTin();

        SavedCardHelpers.addCardOnMidCustId(merchantType, custId, PaymentDTO.EXP_MONTH, PaymentDTO.Tokenization_Year, PaymentDTO.COBRANDED_DC);
        String tinDCPaytm = SavedCardHelpers.getTin();

        SavedCardHelpers.addCardOnMidCustId(merchantType, custId, PaymentDTO.EXP_MONTH, PaymentDTO.Tokenization_Year, PaymentDTO.NONCOBRANDED_DC);
        String tinDCNonPaytm = SavedCardHelpers.getTin();

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setCustId(custId)
                .setTxnValue("10.00")
                .setSsoToken("")
                .setOrderId(orderID)
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);


        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        SoftAssertions softAssertions= new SoftAssertions();
//Check isCardCoBranded is false for debit card when displayName contains Paytm
        softAssertions.assertThat(tinDCPaytm).isEqualTo(fetchPaymentOptionsJson.getString(
            "body.merchantPayOption.savedInstruments.findAll { it.cardType=='Debit Card' && it.displayName.contains('Paytm')}.cardDetails.cardId").replaceAll("\\[|\\]", ""));
        softAssertions.assertThat(fetchPaymentOptionsJson.getString(
            "body.merchantPayOption.savedInstruments.findAll { it.cardType=='Debit Card' && it.displayName.contains('Paytm')}.isCardCoBranded").replaceAll("\\[|\\]", "")).isEqualTo("false");
        softAssertions.assertThat(fetchPaymentOptionsJson.getString(
            "body.merchantPayOption.savedInstruments.findAll { it.cardType=='Debit Card' && it.displayName.contains('Paytm')}.isCardCoft").replaceAll("\\[|\\]", "")).isEqualTo("true");

//Check isCardCoBranded is false for debit card when displayName contains Not Paytm
        softAssertions.assertThat(tinDCNonPaytm).isEqualTo(fetchPaymentOptionsJson.getString(
            "body.merchantPayOption.savedInstruments.findAll { it.cardType=='Debit Card' && !it.displayName.contains('Paytm')}.cardDetails.cardId").replaceAll("\\[|\\]", ""));
        softAssertions.assertThat(fetchPaymentOptionsJson.getString(
            "body.merchantPayOption.savedInstruments.findAll { it.cardType=='Debit Card' && !it.displayName.contains('Paytm')}.isCardCoBranded").replaceAll("\\[|\\]", "")).isEqualTo("false");
        softAssertions.assertThat(fetchPaymentOptionsJson.getString(
            "body.merchantPayOption.savedInstruments.findAll { it.cardType=='Debit Card' && !it.displayName.contains('Paytm')}.isCardCoft").replaceAll("\\[|\\]", "")).isEqualTo("true");

//Check isCardCoBranded is true for Credit card when displayName contains Paytm
        softAssertions.assertThat(tinCCPaytm).isEqualTo(fetchPaymentOptionsJson.getString(
            "body.merchantPayOption.savedInstruments.findAll { it.cardType=='Credit Card' && it.displayName.contains('Paytm')}.cardDetails.cardId").replaceAll("\\[|\\]", ""));
        softAssertions.assertThat(fetchPaymentOptionsJson.getString(
            "body.merchantPayOption.savedInstruments.findAll { it.cardType=='Credit Card' && it.displayName.contains('Paytm')}.isCardCoBranded").replaceAll("\\[|\\]", "")).isEqualTo("true");
        softAssertions.assertThat(fetchPaymentOptionsJson.getString(
            "body.merchantPayOption.savedInstruments.findAll { it.cardType=='Credit Card' && it.displayName.contains('Paytm')}.isCardCoft").replaceAll("\\[|\\]", "")).isEqualTo("true");

//Check isCardCoBranded is false for Credit card when displayName contains Not Paytm
        softAssertions.assertThat(tinCCNonPaytm).isEqualTo(fetchPaymentOptionsJson.getString(
            "body.merchantPayOption.savedInstruments.findAll { it.cardType=='Credit Card' && !it.displayName.contains('Paytm')}.cardDetails.cardId").replaceAll("\\[|\\]", ""));
        softAssertions.assertThat(fetchPaymentOptionsJson.getString(
            "body.merchantPayOption.savedInstruments.findAll { it.cardType=='Credit Card' && !it.displayName.contains('Paytm')}.isCardCoBranded").replaceAll("\\[|\\]", "")).isEqualTo("false");
        softAssertions.assertThat(fetchPaymentOptionsJson.getString(
            "body.merchantPayOption.savedInstruments.findAll { it.cardType=='Credit Card' && !it.displayName.contains('Paytm')}.isCardCoft").replaceAll("\\[|\\]", "")).isEqualTo("true");
        softAssertions.assertAll();
    }

    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PGP-47312")
    @Test(description = "Verify Credit and Debit card filter in FPO2")
    public void Saved_Card_in_FPO2() throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.CobrandedFPO;
        String orderID = CommonHelpers.generateOrderId();

        custId = RandomStringUtils.randomAlphabetic(10) + Instant.now().toEpochMilli();
        SavedCardHelpers.addCardOnMidCustId(merchantType, custId, PaymentDTO.EXP_MONTH, PaymentDTO.Tokenization_Year, PaymentDTO.COBRANDED_CC);
        String tinCCPaytm = SavedCardHelpers.getTin();

        SavedCardHelpers.addCardOnMidCustId(merchantType, custId, PaymentDTO.EXP_MONTH, PaymentDTO.Tokenization_Year, PaymentDTO.NONCOBRANDED_CC);
        String tinCCNonPaytm = SavedCardHelpers.getTin();

        SavedCardHelpers.addCardOnMidCustId(merchantType, custId, PaymentDTO.EXP_MONTH, PaymentDTO.Tokenization_Year, PaymentDTO.COBRANDED_DC);
        String tinDCPaytm = SavedCardHelpers.getTin();

        SavedCardHelpers.addCardOnMidCustId(merchantType, custId, PaymentDTO.EXP_MONTH, PaymentDTO.Tokenization_Year, PaymentDTO.NONCOBRANDED_DC);
        String tinDCNonPaytm = SavedCardHelpers.getTin();

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setCustId(custId)
                .setTxnValue("10.00")
                .setSsoToken("")
                .setOrderId(orderID)
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);


        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v2").build();
        FetchPaymentOptionV2 fetchPaymentOption = new FetchPaymentOptionV2(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        SoftAssertions softAssertions=new SoftAssertions();

        //Check isCardCoBranded is false for debit card when displayName contains Paytm
        softAssertions.assertThat(tinDCPaytm).isEqualTo(fetchPaymentOptionsJson.getString(
            "body.merchantPayOption.savedInstruments.findAll { it.cardType=='Debit Card' && it.displayName.contains('Paytm')}.cardDetails.cardId").replaceAll("\\[|\\]", ""));
        softAssertions.assertThat(fetchPaymentOptionsJson.getString(
            "body.merchantPayOption.savedInstruments.findAll { it.cardType=='Debit Card' && it.displayName.contains('Paytm')}.isCardCoBranded").replaceAll("\\[|\\]", "")).isEqualTo("false");
        softAssertions.assertThat(fetchPaymentOptionsJson.getString(
            "body.merchantPayOption.savedInstruments.findAll { it.cardType=='Debit Card' && it.displayName.contains('Paytm')}.isCardCoft").replaceAll("\\[|\\]", "")).isEqualTo("true");

//Check isCardCoBranded is false for debit card when displayName contains Not Paytm
        softAssertions.assertThat(tinDCNonPaytm).isEqualTo(fetchPaymentOptionsJson.getString(
            "body.merchantPayOption.savedInstruments.findAll { it.cardType=='Debit Card' && !it.displayName.contains('Paytm')}.cardDetails.cardId").replaceAll("\\[|\\]", ""));
        softAssertions.assertThat(fetchPaymentOptionsJson.getString(
            "body.merchantPayOption.savedInstruments.findAll { it.cardType=='Debit Card' && !it.displayName.contains('Paytm')}.isCardCoBranded").replaceAll("\\[|\\]", "")).isEqualTo("false");
        softAssertions.assertThat(fetchPaymentOptionsJson.getString(
            "body.merchantPayOption.savedInstruments.findAll { it.cardType=='Debit Card' && !it.displayName.contains('Paytm')}.isCardCoft").replaceAll("\\[|\\]", "")).isEqualTo("true");

//Check isCardCoBranded is true for Credit card when displayName contains Paytm
        softAssertions.assertThat(tinCCPaytm).isEqualTo(fetchPaymentOptionsJson.getString(
            "body.merchantPayOption.savedInstruments.findAll { it.cardType=='Credit Card' && it.displayName.contains('Paytm')}.cardDetails.cardId").replaceAll("\\[|\\]", ""));
        softAssertions.assertThat(fetchPaymentOptionsJson.getString(
            "body.merchantPayOption.savedInstruments.findAll { it.cardType=='Credit Card' && it.displayName.contains('Paytm')}.isCardCoBranded").replaceAll("\\[|\\]", "")).isEqualTo("true");
        softAssertions.assertThat(fetchPaymentOptionsJson.getString(
            "body.merchantPayOption.savedInstruments.findAll { it.cardType=='Credit Card' && it.displayName.contains('Paytm')}.isCardCoft").replaceAll("\\[|\\]", "")).isEqualTo("true");

//Check isCardCoBranded is false for Credit card when displayName contains Not Paytm
        softAssertions.assertThat(tinCCNonPaytm).isEqualTo(fetchPaymentOptionsJson.getString(
            "body.merchantPayOption.savedInstruments.findAll { it.cardType=='Credit Card' && !it.displayName.contains('Paytm')}.cardDetails.cardId").replaceAll("\\[|\\]", ""));
        softAssertions.assertThat(fetchPaymentOptionsJson.getString(
            "body.merchantPayOption.savedInstruments.findAll { it.cardType=='Credit Card' && !it.displayName.contains('Paytm')}.isCardCoBranded").replaceAll("\\[|\\]", "")).isEqualTo("false");
        softAssertions.assertThat(fetchPaymentOptionsJson.getString(
            "body.merchantPayOption.savedInstruments.findAll { it.cardType=='Credit Card' && !it.displayName.contains('Paytm')}.isCardCoft").replaceAll("\\[|\\]", "")).isEqualTo("true");
        softAssertions.assertAll();
    }


    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PGP-47312")
    @Test(description = "Verify Credit and Debit card filter in FPO1")
    public void Saved_Card_in_FPO1() throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.CobrandedFPO;
        String orderID = CommonHelpers.generateOrderId();

        custId = RandomStringUtils.randomAlphabetic(10) + Instant.now().toEpochMilli();
        SavedCardHelpers.addCardOnMidCustId(merchantType, custId, PaymentDTO.EXP_MONTH, PaymentDTO.Tokenization_Year, PaymentDTO.COBRANDED_CC);
        String tinCCPaytm = SavedCardHelpers.getTin();

        SavedCardHelpers.addCardOnMidCustId(merchantType, custId, PaymentDTO.EXP_MONTH, PaymentDTO.Tokenization_Year, PaymentDTO.NONCOBRANDED_CC);
        String tinCCNonPaytm = SavedCardHelpers.getTin();

        SavedCardHelpers.addCardOnMidCustId(merchantType, custId, PaymentDTO.EXP_MONTH, PaymentDTO.Tokenization_Year, PaymentDTO.COBRANDED_DC);
        String tinDCPaytm = SavedCardHelpers.getTin();

        SavedCardHelpers.addCardOnMidCustId(merchantType, custId, PaymentDTO.EXP_MONTH, PaymentDTO.Tokenization_Year, PaymentDTO.NONCOBRANDED_DC);
        String tinDCNonPaytm = SavedCardHelpers.getTin();

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setCustId(custId)
                .setTxnValue("10.00")
                .setSsoToken("")
                .setOrderId(orderID)
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);

        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        SoftAssertions softAssertions = new SoftAssertions();

// Check isCardCoBranded is false for debit card when displayName contains Paytm
        softAssertions.assertThat(tinDCPaytm).isEqualTo(fetchPaymentOptionsJson.getString(
            "body.merchantPayOption.savedInstruments.findAll { it.cardType=='Debit Card' && it.displayName.contains('Paytm')}.cardDetails.cardId").replaceAll("\\[|\\]", ""));
        softAssertions.assertThat(fetchPaymentOptionsJson.getString(
            "body.merchantPayOption.savedInstruments.findAll { it.cardType=='Debit Card' && it.displayName.contains('Paytm')}.isCardCoBranded").replaceAll("\\[|\\]", "")).isEqualTo("false");
        softAssertions.assertThat(fetchPaymentOptionsJson.getString(
            "body.merchantPayOption.savedInstruments.findAll { it.cardType=='Debit Card' && it.displayName.contains('Paytm')}.isCardCoft").replaceAll("\\[|\\]", "")).isEqualTo("true");

// Check isCardCoBranded is false for debit card when displayName contains Not Paytm
        softAssertions.assertThat(tinDCNonPaytm).isEqualTo(fetchPaymentOptionsJson.getString(
            "body.merchantPayOption.savedInstruments.findAll { it.cardType=='Debit Card' && !it.displayName.contains('Paytm')}.cardDetails.cardId").replaceAll("\\[|\\]", ""));
        softAssertions.assertThat(fetchPaymentOptionsJson.getString(
            "body.merchantPayOption.savedInstruments.findAll { it.cardType=='Debit Card' && !it.displayName.contains('Paytm')}.isCardCoBranded").replaceAll("\\[|\\]", "")).isEqualTo("false");
        softAssertions.assertThat(fetchPaymentOptionsJson.getString(
            "body.merchantPayOption.savedInstruments.findAll { it.cardType=='Debit Card' && !it.displayName.contains('Paytm')}.isCardCoft").replaceAll("\\[|\\]", "")).isEqualTo("true");

// Check isCardCoBranded is true for Credit card when displayName contains Paytm
        softAssertions.assertThat(tinCCPaytm).isEqualTo(fetchPaymentOptionsJson.getString(
            "body.merchantPayOption.savedInstruments.findAll { it.cardType=='Credit Card' && it.displayName.contains('Paytm')}.cardDetails.cardId").replaceAll("\\[|\\]", ""));
        softAssertions.assertThat(fetchPaymentOptionsJson.getString(
            "body.merchantPayOption.savedInstruments.findAll { it.cardType=='Credit Card' && it.displayName.contains('Paytm')}.isCardCoBranded").replaceAll("\\[|\\]", "")).isEqualTo("true");
        softAssertions.assertThat(fetchPaymentOptionsJson.getString(
            "body.merchantPayOption.savedInstruments.findAll { it.cardType=='Credit Card' && it.displayName.contains('Paytm')}.isCardCoft").replaceAll("\\[|\\]", "")).isEqualTo("true");

// Check isCardCoBranded is false for Credit card when displayName contains Not Paytm
        softAssertions.assertThat(tinCCNonPaytm).isEqualTo(fetchPaymentOptionsJson.getString(
            "body.merchantPayOption.savedInstruments.findAll { it.cardType=='Credit Card' && !it.displayName.contains('Paytm')}.cardDetails.cardId").replaceAll("\\[|\\]", ""));
        softAssertions.assertThat(fetchPaymentOptionsJson.getString(
            "body.merchantPayOption.savedInstruments.findAll { it.cardType=='Credit Card' && !it.displayName.contains('Paytm')}.isCardCoBranded").replaceAll("\\[|\\]", "")).isEqualTo("false");
        softAssertions.assertThat(fetchPaymentOptionsJson.getString(
            "body.merchantPayOption.savedInstruments.findAll { it.cardType=='Credit Card' && !it.displayName.contains('Paytm')}.isCardCoft").replaceAll("\\[|\\]", "")).isEqualTo("true");

        softAssertions.assertAll();
    }

    //disabling this as savedcards are disabled in FQR
    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PGP-47312")
    @Test(description = "Verify Credit and Debit card filter in FPO5")
    public void Saved_Card_in_FetchQRPaymentDetail() throws Exception {
        User user = userManager.getForWrite(Label.PPBL);
        Constants.MerchantType merchantType = Constants.MerchantType.CobrandedFPO;

        SavedCardHelpers.deleteSavedCard(user);

        SavedCardHelpers.addCard(user,PaymentDTO.EXP_MONTH, PaymentDTO.Tokenization_Year, PaymentDTO.COBRANDED_CC);
        String tinCCPaytm = SavedCardHelpers.getTin();

        SavedCardHelpers.addCard(user,PaymentDTO.EXP_MONTH, PaymentDTO.Tokenization_Year, PaymentDTO.NONCOBRANDED_CC);
        String tinCCNonPaytm = SavedCardHelpers.getTin();

        SavedCardHelpers.addCard(user,PaymentDTO.EXP_MONTH, PaymentDTO.Tokenization_Year, PaymentDTO.COBRANDED_DC);
        String tinDCPaytm = SavedCardHelpers.getTin();

        SavedCardHelpers.addCard(user,PaymentDTO.EXP_MONTH, PaymentDTO.Tokenization_Year, PaymentDTO.NONCOBRANDED_DC);
        String tinDCNonPaytm = SavedCardHelpers.getTin();

        GenerateQR generateQR = new GenerateQR(merchantType.getId(), "","UPI_QR_CODE");
        String generateResponse = generateQR.execute().asString();
        generateResponse= generateResponse.replace("\\=","\\\\=");
        generateResponse =generateResponse.replace("\\&","\\\\&");

        JsonPath generateResponseJson = new JsonPath(generateResponse);
        Assertions.assertThat(generateResponseJson.getString("statusCode")).isEqualTo("200");
        String qrCodeId= generateResponseJson.getString("response[0].qrCodeId");

        FetchQRPaymentDetailsDTO paymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId).setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();
        FetchQRPaymentDetails fqrDetail = new FetchQRPaymentDetails(paymentDetailsDTO);
        JsonPath fqrResponse = fqrDetail.execute().jsonPath();

        SoftAssertions softAssertions= new SoftAssertions();

        //Check isCardCoBranded is false for debit card when displayName contains Paytm
        softAssertions.assertThat(tinDCPaytm).isEqualTo(fqrResponse.getString(
            "body.paymentOptions.merchantPayOption.savedInstruments.findAll { it.cardType=='Debit Card' && it.displayName.contains('Paytm')}.cardDetails.cardId").replaceAll("\\[|\\]", ""));
        softAssertions.assertThat(fqrResponse.getString(
            "body.paymentOptions.merchantPayOption.savedInstruments.findAll { it.cardType=='Debit Card' && it.displayName.contains('Paytm')}.isCardCoBranded").replaceAll("\\[|\\]", "")).isEqualTo("false");
        softAssertions.assertThat(fqrResponse.getString(
            "body.paymentOptions.merchantPayOption.savedInstruments.findAll { it.cardType=='Debit Card' && it.displayName.contains('Paytm')}.isCardCoft").replaceAll("\\[|\\]", "")).isEqualTo("true");

//Check isCardCoBranded is false for debit card when displayName contains Not Paytm
        softAssertions.assertThat(tinDCNonPaytm).isEqualTo(fqrResponse.getString(
            "body.paymentOptions.merchantPayOption.savedInstruments.findAll { it.cardType=='Debit Card' && !it.displayName.contains('Paytm')}.cardDetails.cardId").replaceAll("\\[|\\]", ""));
        softAssertions.assertThat(fqrResponse.getString(
            "body.paymentOptions.merchantPayOption.savedInstruments.findAll { it.cardType=='Debit Card' && !it.displayName.contains('Paytm')}.isCardCoBranded").replaceAll("\\[|\\]", "")).isEqualTo("false");
        softAssertions.assertThat(fqrResponse.getString(
            "body.paymentOptions.merchantPayOption.savedInstruments.findAll { it.cardType=='Debit Card' && !it.displayName.contains('Paytm')}.isCardCoft").replaceAll("\\[|\\]", "")).isEqualTo("true");

//Check isCardCoBranded is true for Credit card when displayName contains Paytm
        softAssertions.assertThat(tinCCPaytm).isEqualTo(fqrResponse.getString(
            "body.paymentOptions.merchantPayOption.savedInstruments.findAll { it.cardType=='Credit Card' && it.displayName.contains('Paytm')}.cardDetails.cardId").replaceAll("\\[|\\]", ""));
        softAssertions.assertThat(fqrResponse.getString(
            "body.paymentOptions.merchantPayOption.savedInstruments.findAll { it.cardType=='Credit Card' && it.displayName.contains('Paytm')}.isCardCoBranded").replaceAll("\\[|\\]", "")).isEqualTo("true");
        softAssertions.assertThat(fqrResponse.getString(
            "body.paymentOptions.merchantPayOption.savedInstruments.findAll { it.cardType=='Credit Card' && it.displayName.contains('Paytm')}.isCardCoft").replaceAll("\\[|\\]", "")).isEqualTo("true");

//Check isCardCoBranded is false for Credit card when displayName contains Not Paytm
        softAssertions.assertThat(tinCCNonPaytm).isEqualTo(fqrResponse.getString(
            "body.paymentOptions.merchantPayOption.savedInstruments.findAll { it.cardType=='Credit Card' && !it.displayName.contains('Paytm')}.cardDetails.cardId").replaceAll("\\[|\\]", ""));
        softAssertions.assertThat(fqrResponse.getString(
            "body.paymentOptions.merchantPayOption.savedInstruments.findAll { it.cardType=='Credit Card' && !it.displayName.contains('Paytm')}.isCardCoBranded").replaceAll("\\[|\\]", "")).isEqualTo("false");
        softAssertions.assertThat(fqrResponse.getString(
            "body.paymentOptions.merchantPayOption.savedInstruments.findAll { it.cardType=='Credit Card' && !it.displayName.contains('Paytm')}.isCardCoft").replaceAll("\\[|\\]", "")).isEqualTo("true");
        softAssertions.assertAll();
    }

}
