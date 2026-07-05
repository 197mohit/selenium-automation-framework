package scripts.coft.theia;

import com.paytm.api.TxnStatus;
import com.paytm.api.coft.PTS.GenerateTokenData;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.appconstants.Constants;
import com.paytm.appconstants.Constants.MerchantType;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.apphelpers.SavedCardHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.dto.processTransactionV1.CardTokenInfo;
import com.paytm.dto.processTransactionV1.response.ProcessTxnV1Response;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Calendar;
import org.apache.commons.lang.RandomStringUtils;
import org.assertj.core.api.SoftAssertions;
import org.testng.annotations.Test;

public class PTCReqValidationTokenTavvTests extends PGPBaseTest {


  Constants.MerchantType coftMerchant3P = MerchantType.COFT_MERCHANT_3P;
  Constants.MerchantType corporateMerchant = Constants.MerchantType.CORPORATE_CARD_ONLY;
  Calendar cal = Calendar.getInstance();

  int month;
  int year;

  @Owner(Constants.Owner.PRAGYA_KURELE)
  @Feature("PGP-40452")
  @Test(description = "Verify Token Expiry in Token Tavv PTC Request when year is expired")
  public void validatePtcRequestExpiredYear() throws Exception {
    String custId= RandomStringUtils.randomAlphabetic(4)+ Instant.now().toEpochMilli();
    SavedCardHelpers.addCardOnMidCustId(coftMerchant3P,custId,PaymentDTO.EXP_MONTH,PaymentDTO.EXP_YEAR,PaymentDTO.VISA_COFT_CARD_NUMBER);
    String tokenIndexNumber = SavedCardHelpers.getTin();
    GenerateTokenData generateTokenData = new GenerateTokenData(coftMerchant3P.getId()).buildRequest("ECOM",tokenIndexNumber,custId,"").generateChecksum(coftMerchant3P.getKey());
    JsonPath generateTokenDataResponse = generateTokenData.execute().jsonPath();

    //Initiate Transaction
    InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, coftMerchant3P).setTxnValue("10").setCustId(custId).build();
    InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);

    month = new Integer(new SimpleDateFormat("MM").format(cal.getTime()));
    year = new Integer(new SimpleDateFormat("yyyy").format(cal.getTime()));

    CardTokenInfo cardTokenInfo = new CardTokenInfo();
    cardTokenInfo.setCardToken(generateTokenDataResponse.getString("body.cardToken"));
    cardTokenInfo.setTokenExpiry(month + "" + (year - 1));
    cardTokenInfo.setTavv(generateTokenDataResponse.getString("body.tavv"));
    cardTokenInfo.setCardSuffix(generateTokenDataResponse.getString("body.tokenInfo.cardSuffix"));
    cardTokenInfo.setPanUniqueReference(generateTokenDataResponse.getString("body.tokenInfo.panUniqueReference"));

    //V1 PTC
    ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
        coftMerchant3P.getId(), initTxnResponse.getBody().getTxnToken(),
        initTxnDTO.getBody().getOrderId())
        .setPaymentMode("CREDIT_CARD")
        .setAuthMode("otp")
        .setCardInfo("||123|")
        .setcardTokenInfo(cardTokenInfo)
        .build();

    ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(
        processTxnV1Request);
    SoftAssertions softAssertions = new SoftAssertions();
    softAssertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultStatus())
        .isEqualTo("F");
    softAssertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultCode())
        .isEqualTo("0001");
    softAssertions.assertThat(processTxnV1Response.getBody().getTxnInfo().getSTATUS())
        .isEqualTo("TXN_FAILURE");
    softAssertions.assertAll();
  }

  @Test(description = "Verify Token Expiry in Token Tavv PTC Request when month is expired")
  public void validatePtcRequestExpiredMonth() throws Exception {
    String custId= RandomStringUtils.randomAlphabetic(4)+ Instant.now().toEpochMilli();
    SavedCardHelpers.addCardOnMidCustId(coftMerchant3P,custId,PaymentDTO.EXP_MONTH,PaymentDTO.EXP_YEAR,PaymentDTO.VISA_COFT_CARD_NUMBER);
    String tokenIndexNumber = SavedCardHelpers.getTin();
    GenerateTokenData generateTokenData = new GenerateTokenData(coftMerchant3P.getId()).buildRequest("ECOM",tokenIndexNumber,custId,"").generateChecksum(coftMerchant3P.getKey());
    JsonPath generateTokenDataResponse = generateTokenData.execute().jsonPath();

    //Initiate Transaction
    InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, coftMerchant3P).setTxnValue("10").setCustId(custId).build();
    InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);

    month = new Integer(new SimpleDateFormat("MM").format(cal.getTime()));
    year = new Integer(new SimpleDateFormat("yyyy").format(cal.getTime()));

    if (month == 1) {
      month = month + 1;
    } else {
      month = month - 1;
    }

    CardTokenInfo cardTokenInfo = new CardTokenInfo();
    cardTokenInfo.setCardToken(generateTokenDataResponse.getString("body.cardToken"));
    cardTokenInfo.setTokenExpiry(month + "" + year);
    cardTokenInfo.setTavv(generateTokenDataResponse.getString("body.tavv"));
    cardTokenInfo.setCardSuffix(generateTokenDataResponse.getString("body.tokenInfo.cardSuffix"));
    cardTokenInfo.setPanUniqueReference(generateTokenDataResponse.getString("body.tokenInfo.panUniqueReference"));

    //V1 PTC
    ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
        coftMerchant3P.getId(), initTxnResponse.getBody().getTxnToken(),
        initTxnDTO.getBody().getOrderId())
        .setPaymentMode("CREDIT_CARD")
        .setAuthMode("otp")
        .setCardInfo("||123|")
        .setcardTokenInfo(cardTokenInfo)
        .build();

    ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(
        processTxnV1Request);
    SoftAssertions softAssertions = new SoftAssertions();
    softAssertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultStatus())
        .isEqualTo("F");
    softAssertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultCode())
        .isEqualTo("0001");
    softAssertions.assertThat(processTxnV1Response.getBody().getTxnInfo().getSTATUS())
        .isEqualTo("TXN_FAILURE");
    softAssertions.assertAll();
  }

  @Owner(Constants.Owner.DEVENDRA_SINGH)
  @Feature("PGP-40772")

  @Test(description = "Verify PTC request fails if last 4 is not provided in request body")
  public void validatePtcRequestWithoutLastFour() throws Exception {
    String custId= RandomStringUtils.randomAlphabetic(4)+ Instant.now().toEpochMilli();
    SavedCardHelpers.addCardOnMidCustId(coftMerchant3P,custId,PaymentDTO.EXP_MONTH,PaymentDTO.EXP_YEAR,PaymentDTO.VISA_COFT_CARD_NUMBER);
    String tokenIndexNumber = SavedCardHelpers.getTin();
    GenerateTokenData generateTokenData = new GenerateTokenData(coftMerchant3P.getId()).buildRequest("ECOM",tokenIndexNumber,custId,"").generateChecksum(coftMerchant3P.getKey());
    JsonPath generateTokenDataResponse = generateTokenData.execute().jsonPath();

    //Initiate Transaction
    InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, coftMerchant3P).setTxnValue("10").setCustId(custId).build();
    InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);

    CardTokenInfo cardTokenInfo = new CardTokenInfo();
    cardTokenInfo.setCardToken(generateTokenDataResponse.getString("body.cardToken"));
    cardTokenInfo.setTokenExpiry(generateTokenDataResponse.getString("body.tokenInfo.tokenExpiry"));
    cardTokenInfo.setTavv(generateTokenDataResponse.getString("body.tavv"));
    cardTokenInfo.setPanUniqueReference(generateTokenDataResponse.getString("body.tokenInfo.panUniqueReference"));


    //V1 PTC
    ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
        coftMerchant3P.getId(), initTxnResponse.getBody().getTxnToken(),
        initTxnDTO.getBody().getOrderId())
        .setPaymentMode("CREDIT_CARD")
        .setAuthMode("otp")
        .setCardInfo("||123|")
        .setcardTokenInfo(cardTokenInfo)
        .build();

    ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(
        processTxnV1Request);
    SoftAssertions softAssertions = new SoftAssertions();
    softAssertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultStatus())
        .isEqualTo("F");
    softAssertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultCode())
        .isEqualTo("0001");
    softAssertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultMsg())
        .isEqualTo("Request parameters are not valid");
    softAssertions.assertThat(processTxnV1Response.getBody().getTxnInfo().getRESPCODE())
        .isEqualTo("1001");
    softAssertions.assertThat(processTxnV1Response.getBody().getTxnInfo().getRESPMSG())
        .isEqualTo("Request parameters are not valid");
    softAssertions.assertThat(processTxnV1Response.getBody().getTxnInfo().getSTATUS())
        .isEqualTo("TXN_FAILURE");
    softAssertions.assertAll();
  }

  @Owner(Constants.Owner.DEVENDRA_SINGH)
  @Feature("PGP-40772")

  @Test(description = "Verify PTC request fails if par is not provided in request body")

  public void validatePtcRequestWithoutPar() throws Exception {
    String custId= RandomStringUtils.randomAlphabetic(4)+ Instant.now().toEpochMilli();
    SavedCardHelpers.addCardOnMidCustId(coftMerchant3P,custId,PaymentDTO.EXP_MONTH,PaymentDTO.EXP_YEAR,PaymentDTO.VISA_COFT_CARD_NUMBER);
    String tokenIndexNumber = SavedCardHelpers.getTin();
    GenerateTokenData generateTokenData = new GenerateTokenData(coftMerchant3P.getId()).buildRequest("ECOM",tokenIndexNumber,custId,"").generateChecksum(coftMerchant3P.getKey());
    JsonPath generateTokenDataResponse = generateTokenData.execute().jsonPath();

    InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, coftMerchant3P).setTxnValue("10").setCustId(custId).build();
    InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
    String orderId = initTxnDTO.getBody().getOrderId();
    CardTokenInfo cardTokenInfo = new CardTokenInfo();
    cardTokenInfo.setCardToken(generateTokenDataResponse.getString("body.cardToken"));
    cardTokenInfo.setTokenExpiry(generateTokenDataResponse.getString("body.tokenInfo.tokenExpiry"));
    cardTokenInfo.setTavv(generateTokenDataResponse.getString("body.tavv"));
    cardTokenInfo.setCardSuffix(generateTokenDataResponse.getString("body.tokenInfo.cardSuffix"));
    ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
        .Builder(coftMerchant3P.getId(),initTxnResponseDTO.getBody().getTxnToken(),orderId)
        .setPaymentMode("CREDIT_CARD")
        .setCardInfo("||123|")
        .setcardTokenInfo(cardTokenInfo)
        .setAuthMode("otp")
        .build();

    ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(
        processTxnV1Request);
    SoftAssertions softAssertions = new SoftAssertions();
    softAssertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultStatus())
        .isEqualTo("F");
    softAssertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultCode())
        .isEqualTo("0001");
    softAssertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultMsg())
        .isEqualTo("Request parameters are not valid");
    softAssertions.assertThat(processTxnV1Response.getBody().getTxnInfo().getRESPCODE())
        .isEqualTo("1001");
    softAssertions.assertThat(processTxnV1Response.getBody().getTxnInfo().getRESPMSG())
        .isEqualTo("Request parameters are not valid");
    softAssertions.assertThat(processTxnV1Response.getBody().getTxnInfo().getSTATUS())
        .isEqualTo("TXN_FAILURE");
    softAssertions.assertAll();
  }

  @Owner(Constants.Owner.ABHISHEK_VERMA)
  @Feature("PGP-37987")
  @Test(description = "Verify successful transaction with Normal CREDIT_CARD token")
  public void standardNormalCCTransactionWithToken() throws Exception {
    String custId= RandomStringUtils.randomAlphabetic(4)+ Instant.now().toEpochMilli();
    SavedCardHelpers.addCardOnMidCustId(coftMerchant3P,custId,PaymentDTO.EXP_MONTH,PaymentDTO.EXP_YEAR,PaymentDTO.VISA_COFT_CARD_NUMBER);
    String tokenIndexNumber = SavedCardHelpers.getTin();
    GenerateTokenData generateTokenData = new GenerateTokenData(coftMerchant3P.getId()).buildRequest("ECOM",tokenIndexNumber,custId,"").generateChecksum(coftMerchant3P.getKey());
    JsonPath generateTokenDataResponse = generateTokenData.execute().jsonPath();

    InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, coftMerchant3P).setTxnValue("10").setCustId(custId).build();
    InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
    String orderId = initTxnDTO.getBody().getOrderId();
    CardTokenInfo cardTokenInfo = new CardTokenInfo();
    cardTokenInfo.setCardToken(generateTokenDataResponse.getString("body.cardToken"));
    cardTokenInfo.setTokenExpiry(generateTokenDataResponse.getString("body.tokenInfo.tokenExpiry"));
    cardTokenInfo.setTavv(generateTokenDataResponse.getString("body.tavv"));
    cardTokenInfo.setCardSuffix(generateTokenDataResponse.getString("body.tokenInfo.cardSuffix"));
    cardTokenInfo.setPanUniqueReference(generateTokenDataResponse.getString("body.tokenInfo.panUniqueReference"));
    ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
        .Builder(coftMerchant3P.getId(),initTxnResponseDTO.getBody().getTxnToken(),orderId)
        .setPaymentMode("CREDIT_CARD")
        .setCardInfo("||123|")
        .setcardTokenInfo(cardTokenInfo)
        .setAuthMode("otp")
        .build();
    ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
    NativeHelpers.submitJsonFormInBrowser(processTxnV1Response.toString());
    TxnStatus txnStatus = new TxnStatus(coftMerchant3P.getId(),orderId);
    txnStatus.executeUntilNotPending();
    txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY).
        validateStatus("TXN_SUCCESS").
        validatePaymentMode("CC").AssertAll();
  }

  @Owner(Constants.Owner.ABHISHEK_VERMA)
  @Feature("PGP-37987")
  @Test(description = "Verify successful transaction with Normal DEBIT_CARD token")
  public void standardNormalDCTransactionWithToken() throws Exception {
    String custId= RandomStringUtils.randomAlphabetic(4)+ Instant.now().toEpochMilli();
    SavedCardHelpers.addCardOnMidCustId(coftMerchant3P,custId,PaymentDTO.EXP_MONTH,PaymentDTO.EXP_YEAR,PaymentDTO.ICICI_DEBIT_CARD_NUMBER);
    String tokenIndexNumber = SavedCardHelpers.getTin();
    GenerateTokenData generateTokenData = new GenerateTokenData(coftMerchant3P.getId()).buildRequest("ECOM",tokenIndexNumber,custId,"").generateChecksum(coftMerchant3P.getKey());
    JsonPath generateTokenDataResponse = generateTokenData.execute().jsonPath();

    InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, coftMerchant3P).setTxnValue("10").setCustId(custId).build();
    InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
    String orderId = initTxnDTO.getBody().getOrderId();
    CardTokenInfo cardTokenInfo = new CardTokenInfo();
    cardTokenInfo.setCardToken(generateTokenDataResponse.getString("body.cardToken"));
    cardTokenInfo.setTokenExpiry(generateTokenDataResponse.getString("body.tokenInfo.tokenExpiry"));
    cardTokenInfo.setTavv(generateTokenDataResponse.getString("body.tavv"));
    cardTokenInfo.setCardSuffix(generateTokenDataResponse.getString("body.tokenInfo.cardSuffix"));
    cardTokenInfo.setPanUniqueReference(generateTokenDataResponse.getString("body.tokenInfo.panUniqueReference"));
    ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
        .Builder(coftMerchant3P.getId(),initTxnResponseDTO.getBody().getTxnToken(),orderId)
        .setPaymentMode("DEBIT_CARD")
        .setCardInfo("||123|")
        .setcardTokenInfo(cardTokenInfo)
        .setAuthMode("otp")
        .build();
    ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
    NativeHelpers.submitJsonFormInBrowser(processTxnV1Response.toString());
    TxnStatus txnStatus = new TxnStatus(coftMerchant3P.getId(),orderId);
    txnStatus.executeUntilNotPending();
    txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY).
        validateStatus("TXN_SUCCESS").
        validatePaymentMode("DC").AssertAll();
  }

  @Owner(Constants.Owner.ABHISHEK_VERMA)
  @Feature("PGP-37987")
  @Test(description = "Verify successful transaction with CORPORATE DEBIT_CARD token")
  public void standardCorporateDebitCardTransactionWithToken() throws Exception {
    String custId= RandomStringUtils.randomAlphabetic(4)+ Instant.now().toEpochMilli();
    SavedCardHelpers.addCardOnMidCustId(corporateMerchant,custId,PaymentDTO.EXP_MONTH,PaymentDTO.EXP_YEAR,PaymentDTO.CORPORATE_INDIAN_DC);
    String tokenIndexNumber = SavedCardHelpers.getTin();
    GenerateTokenData generateTokenData = new GenerateTokenData(corporateMerchant.getId()).buildRequest("ECOM",tokenIndexNumber,custId,"").generateChecksum(corporateMerchant.getKey());
    JsonPath generateTokenDataResponse = generateTokenData.execute().jsonPath();

    InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, corporateMerchant).setTxnValue("10").setCustId(custId).build();
    InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);

    //checking if token is corporate card DC or not
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(PGPHelpers.getBinDetails(generateTokenDataResponse.getString("body.tokenInfo.tokenBin")).getCorporateCard()).isEqualTo("true");
    softly.assertThat(PGPHelpers.getBinDetails(generateTokenDataResponse.getString("body.tokenInfo.tokenBin")).getCardType()).isEqualTo("DEBIT_CARD");
    softly.assertAll();

    String orderId = initTxnDTO.getBody().getOrderId();
    CardTokenInfo cardTokenInfo = new CardTokenInfo();
    cardTokenInfo.setCardToken(generateTokenDataResponse.getString("body.cardToken"));
    cardTokenInfo.setTokenExpiry(generateTokenDataResponse.getString("body.tokenInfo.tokenExpiry"));
    cardTokenInfo.setTavv(generateTokenDataResponse.getString("body.tavv"));
    cardTokenInfo.setCardSuffix(generateTokenDataResponse.getString("body.tokenInfo.cardSuffix"));
    cardTokenInfo.setPanUniqueReference(generateTokenDataResponse.getString("body.tokenInfo.panUniqueReference"));
    ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
        .Builder(corporateMerchant.getId(),initTxnResponseDTO.getBody().getTxnToken(),orderId)
        .setPaymentMode("DEBIT_CARD")
        .setCardInfo("||123|")
        .setcardTokenInfo(cardTokenInfo)
        .setAuthMode("otp")
        .build();
    ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
    NativeHelpers.submitJsonFormInBrowser(processTxnV1Response.toString());
    TxnStatus txnStatus = new TxnStatus(corporateMerchant.getId(),orderId);
    txnStatus.executeUntilNotPending();
    txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY).
        validateStatus("TXN_SUCCESS").
        validatePaymentMode("DC").AssertAll();
  }

  @Owner(Constants.Owner.ABHISHEK_VERMA)
  @Feature("PGP-37987")
  @Test(description = "Verify successful transaction with CORPORATE CREDIT_CARD token")
  public void standardCorporateCreditCardTransactionWithToken() throws Exception {
    String custId= RandomStringUtils.randomAlphabetic(4)+ Instant.now().toEpochMilli();
    SavedCardHelpers.addCardOnMidCustId(corporateMerchant,custId,PaymentDTO.EXP_MONTH,PaymentDTO.EXP_YEAR,PaymentDTO.CORPORATE_INDIAN_CC);
    String tokenIndexNumber = SavedCardHelpers.getTin();
    GenerateTokenData generateTokenData = new GenerateTokenData(corporateMerchant.getId()).buildRequest("ECOM",tokenIndexNumber,custId,"").generateChecksum(corporateMerchant.getKey());
    JsonPath generateTokenDataResponse = generateTokenData.execute().jsonPath();

    InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, corporateMerchant).setTxnValue("10").setCustId(custId).build();
    InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);

    //checking if token is corporate card CC or not
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(PGPHelpers.getBinDetails(generateTokenDataResponse.getString("body.tokenInfo.tokenBin")).getCorporateCard()).isEqualTo("true");
    softly.assertThat(PGPHelpers.getBinDetails(generateTokenDataResponse.getString("body.tokenInfo.tokenBin")).getCardType()).isEqualTo("CREDIT_CARD");
    softly.assertAll();

    String orderId = initTxnDTO.getBody().getOrderId();
    CardTokenInfo cardTokenInfo = new CardTokenInfo();
    cardTokenInfo.setCardToken(generateTokenDataResponse.getString("body.cardToken"));
    cardTokenInfo.setTokenExpiry(generateTokenDataResponse.getString("body.tokenInfo.tokenExpiry"));
    cardTokenInfo.setTavv(generateTokenDataResponse.getString("body.tavv"));
    cardTokenInfo.setCardSuffix(generateTokenDataResponse.getString("body.tokenInfo.cardSuffix"));
    cardTokenInfo.setPanUniqueReference(generateTokenDataResponse.getString("body.tokenInfo.panUniqueReference"));
    ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
        .Builder(corporateMerchant.getId(),initTxnResponseDTO.getBody().getTxnToken(),orderId)
        .setPaymentMode("CREDIT_CARD")
        .setCardInfo("||123|")
        .setcardTokenInfo(cardTokenInfo)
        .setAuthMode("otp")
        .build();
    ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
    NativeHelpers.submitJsonFormInBrowser(processTxnV1Response.toString());
    TxnStatus txnStatus = new TxnStatus(corporateMerchant.getId(),orderId);
    txnStatus.executeUntilNotPending();
    txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY).
        validateStatus("TXN_SUCCESS").
        validatePaymentMode("CC").AssertAll();
  }

  @Owner(Constants.Owner.ABHISHEK_VERMA)
  @Feature("PGP-37987")
  @Test(description = "Verify successful transaction with PREPAID CARD token")
  public void successfulPrepaidCardTransactionWithToken() throws Exception {
    String custId= RandomStringUtils.randomAlphabetic(4)+ Instant.now().toEpochMilli();
    SavedCardHelpers.addCardOnMidCustId(corporateMerchant,custId,PaymentDTO.EXP_MONTH,PaymentDTO.EXP_YEAR,PaymentDTO.PREPAID_CARD);
    String tokenIndexNumber = SavedCardHelpers.getTin();
    GenerateTokenData generateTokenData = new GenerateTokenData(corporateMerchant.getId()).buildRequest("ECOM",tokenIndexNumber,custId,"").generateChecksum(corporateMerchant.getKey());
    JsonPath generateTokenDataResponse = generateTokenData.execute().jsonPath();

    InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, corporateMerchant).setTxnValue("10").setCustId(custId).build();
    InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);

    //checking if token is corporate card DC or not
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(PGPHelpers.getBinDetails(generateTokenDataResponse.getString("body.tokenInfo.tokenBin")).getPrepaidCard()).isEqualTo("true");
    softly.assertThat(PGPHelpers.getBinDetails(generateTokenDataResponse.getString("body.tokenInfo.tokenBin")).getCardType()).isEqualTo("DEBIT_CARD");
    softly.assertAll();

    String orderId = initTxnDTO.getBody().getOrderId();
    CardTokenInfo cardTokenInfo = new CardTokenInfo();
    cardTokenInfo.setCardToken(generateTokenDataResponse.getString("body.cardToken"));
    cardTokenInfo.setTokenExpiry(generateTokenDataResponse.getString("body.tokenInfo.tokenExpiry"));
    cardTokenInfo.setTavv(generateTokenDataResponse.getString("body.tavv"));
    cardTokenInfo.setCardSuffix(generateTokenDataResponse.getString("body.tokenInfo.cardSuffix"));
    cardTokenInfo.setPanUniqueReference(generateTokenDataResponse.getString("body.tokenInfo.panUniqueReference"));
    ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
        .Builder(corporateMerchant.getId(),initTxnResponseDTO.getBody().getTxnToken(),orderId)
        .setPaymentMode("DEBIT_CARD")
        .setCardInfo("||123|")
        .setcardTokenInfo(cardTokenInfo)
        .setAuthMode("otp")
        .build();
    ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
    NativeHelpers.submitJsonFormInBrowser(processTxnV1Response.toString());
    TxnStatus txnStatus = new TxnStatus(corporateMerchant.getId(),orderId);
    txnStatus.executeUntilNotPending();
    txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY).
        validateStatus("TXN_SUCCESS").
        validatePaymentMode("DC").AssertAll();
  }

  @Owner(Constants.Owner.ABHISHEK_VERMA)
  @Feature("PGP-37987")
  @Test(description = "Verify successful EMI transaction with Token")
  public void successfulEMITransactionWithToken() throws Exception {
    String custId= RandomStringUtils.randomAlphabetic(4)+ Instant.now().toEpochMilli();
    SavedCardHelpers.addCardOnMidCustId(corporateMerchant,custId,PaymentDTO.EXP_MONTH,PaymentDTO.EXP_YEAR,PaymentDTO.PREPAID_CARD);
    String tokenIndexNumber = SavedCardHelpers.getTin();
    GenerateTokenData generateTokenData = new GenerateTokenData(corporateMerchant.getId()).buildRequest("ECOM",tokenIndexNumber,custId,"").generateChecksum(corporateMerchant.getKey());
    JsonPath generateTokenDataResponse = generateTokenData.execute().jsonPath();

    InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, corporateMerchant).setTxnValue("10").setCustId(custId).build();
    InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);

    //checking if token is prepaid card DC or not
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(PGPHelpers.getBinDetails(generateTokenDataResponse.getString("body.tokenInfo.tokenBin")).getPrepaidCard()).isEqualTo("true");
    softly.assertThat(PGPHelpers.getBinDetails(generateTokenDataResponse.getString("body.tokenInfo.tokenBin")).getCardType()).isEqualTo("DEBIT_CARD");
    softly.assertAll();

    String orderId = initTxnDTO.getBody().getOrderId();
    CardTokenInfo cardTokenInfo = new CardTokenInfo();
    cardTokenInfo.setCardToken(generateTokenDataResponse.getString("body.cardToken"));
    cardTokenInfo.setTokenExpiry(generateTokenDataResponse.getString("body.tokenInfo.tokenExpiry"));
    cardTokenInfo.setTavv(generateTokenDataResponse.getString("body.tavv"));
    cardTokenInfo.setCardSuffix(generateTokenDataResponse.getString("body.tokenInfo.cardSuffix"));
    cardTokenInfo.setPanUniqueReference(generateTokenDataResponse.getString("body.tokenInfo.panUniqueReference"));
    ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
        .Builder(corporateMerchant.getId(),initTxnResponseDTO.getBody().getTxnToken(),orderId)
        .setPaymentMode("DEBIT_CARD")
        .setCardInfo("||123|")
        .setcardTokenInfo(cardTokenInfo)
        .setAuthMode("otp")
        .build();
    ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
    NativeHelpers.submitJsonFormInBrowser(processTxnV1Response.toString());
    TxnStatus txnStatus = new TxnStatus(corporateMerchant.getId(),orderId);
    txnStatus.executeUntilNotPending();
    txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY).
        validateStatus("TXN_SUCCESS").
        validatePaymentMode("DC").AssertAll();
  }

  @Owner(Constants.Owner.ABHISHEK_VERMA)
  @Feature("PGP-37987")
  @Test(description = "Verify PTC with Diners card is failing if param merchantTokenRequestorId is not sent in request")
  public void verifyMerchantTokenRequestorIdIsMandatoryParam() throws Exception {
    String custId= RandomStringUtils.randomAlphabetic(4)+ Instant.now().toEpochMilli();
    SavedCardHelpers.addCardOnMidCustId(corporateMerchant,custId,PaymentDTO.EXP_MONTH,PaymentDTO.EXP_YEAR,PaymentDTO.DINERS_CARD_NUMBER);
    String tokenIndexNumber = SavedCardHelpers.getTin();
    GenerateTokenData generateTokenData = new GenerateTokenData(corporateMerchant.getId()).buildRequest("ECOM",tokenIndexNumber,custId,"").generateChecksum(corporateMerchant.getKey());
    JsonPath generateTokenDataResponse = generateTokenData.execute().jsonPath();

    InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, corporateMerchant).setTxnValue("10").setCustId(custId).build();
    InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);

    //checking if token is diners CC or not
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(PGPHelpers.getBinDetails(generateTokenDataResponse.getString("body.tokenInfo.tokenBin")).getCardName()).isEqualTo("DINERS");
    softly.assertThat(PGPHelpers.getBinDetails(generateTokenDataResponse.getString("body.tokenInfo.tokenBin")).getCardType()).isEqualTo("CREDIT_CARD");
    softly.assertAll();

    String orderId = initTxnDTO.getBody().getOrderId();
    CardTokenInfo cardTokenInfo = new CardTokenInfo();
    cardTokenInfo.setCardToken(generateTokenDataResponse.getString("body.cardToken"));
    cardTokenInfo.setTokenExpiry(generateTokenDataResponse.getString("body.tokenInfo.tokenExpiry"));
    cardTokenInfo.setTavv(generateTokenDataResponse.getString("body.tavv"));
    cardTokenInfo.setCardSuffix(generateTokenDataResponse.getString("body.tokenInfo.cardSuffix"));
    cardTokenInfo.setPanUniqueReference(generateTokenDataResponse.getString("body.tokenInfo.panUniqueReference"));
    ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
        .Builder(corporateMerchant.getId(),initTxnResponseDTO.getBody().getTxnToken(),orderId)
        .setPaymentMode("CREDIT_CARD")
        .setCardInfo("||123|")
        .setcardTokenInfo(cardTokenInfo)
        .setAuthMode("otp")
        .build();
    ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
    softly.assertThat(processTxnV1Response.getBody().getResultInfo().getResultMsg()).isEqualToIgnoringCase("Request parameters are not valid");
    softly.assertThat(processTxnV1Response.getBody().getResultInfo().getResultCode()).isEqualToIgnoringCase("0001");
    softly.assertThat(processTxnV1Response.getBody().getResultInfo().getResultStatus()).isEqualToIgnoringCase("F");
    softly.assertThat(processTxnV1Response.getBody().getTxnInfo().getRESPMSG()).isEqualToIgnoringCase("Request parameters are not valid");
    softly.assertThat(processTxnV1Response.getBody().getTxnInfo().getRESPCODE()).isEqualToIgnoringCase("1001");
    softly.assertThat(processTxnV1Response.getBody().getTxnInfo().getSTATUS()).isEqualToIgnoringCase("TXN_FAILURE");
    softly.assertAll();
  }

  @Owner(Constants.Owner.ABHISHEK_VERMA)
  @Feature("PGP-37987")
  @Test(description = "Verify PTC with Diners card is failing if param tokenUniqueReference is not sent in request")
  public void verifyTokenUniqueReferenceIsMandatoryParam() throws Exception {
    String custId= RandomStringUtils.randomAlphabetic(4)+ Instant.now().toEpochMilli();
    SavedCardHelpers.addCardOnMidCustId(corporateMerchant,custId,PaymentDTO.EXP_MONTH,PaymentDTO.EXP_YEAR,PaymentDTO.DINERS_CARD_NUMBER);
    String tokenIndexNumber = SavedCardHelpers.getTin();
    GenerateTokenData generateTokenData = new GenerateTokenData(corporateMerchant.getId()).buildRequest("ECOM",tokenIndexNumber,custId,"").generateChecksum(corporateMerchant.getKey());
    JsonPath generateTokenDataResponse = generateTokenData.execute().jsonPath();

    InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, corporateMerchant).setTxnValue("10").setCustId(custId).build();
    InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);

    //checking if token is diners CC or not
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(PGPHelpers.getBinDetails(generateTokenDataResponse.getString("body.tokenInfo.tokenBin")).getCardName()).isEqualTo("DINERS");
    softly.assertThat(PGPHelpers.getBinDetails(generateTokenDataResponse.getString("body.tokenInfo.tokenBin")).getCardType()).isEqualTo("CREDIT_CARD");
    softly.assertAll();

    String orderId = initTxnDTO.getBody().getOrderId();
    CardTokenInfo cardTokenInfo = new CardTokenInfo();
    cardTokenInfo.setCardToken(generateTokenDataResponse.getString("body.cardToken"));
    cardTokenInfo.setTokenExpiry(generateTokenDataResponse.getString("body.tokenInfo.tokenExpiry"));
    cardTokenInfo.setTavv(generateTokenDataResponse.getString("body.tavv"));
    cardTokenInfo.setCardSuffix(generateTokenDataResponse.getString("body.tokenInfo.cardSuffix"));
    cardTokenInfo.setMerchantTokenRequestorId(generateTokenDataResponse.getString("body.tokenInfo.tokenRequestorId"));
    ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
        .Builder(corporateMerchant.getId(),initTxnResponseDTO.getBody().getTxnToken(),orderId)
        .setPaymentMode("CREDIT_CARD")
        .setCardInfo("||123|")
        .setcardTokenInfo(cardTokenInfo)
        .setAuthMode("otp")
        .build();
    ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
    softly.assertThat(processTxnV1Response.getBody().getResultInfo().getResultMsg()).isEqualToIgnoringCase("Request parameters are not valid");
    softly.assertThat(processTxnV1Response.getBody().getResultInfo().getResultCode()).isEqualToIgnoringCase("0001");
    softly.assertThat(processTxnV1Response.getBody().getResultInfo().getResultStatus()).isEqualToIgnoringCase("F");
    softly.assertThat(processTxnV1Response.getBody().getTxnInfo().getRESPMSG()).isEqualToIgnoringCase("Request parameters are not valid");
    softly.assertThat(processTxnV1Response.getBody().getTxnInfo().getRESPCODE()).isEqualToIgnoringCase("1001");
    softly.assertThat(processTxnV1Response.getBody().getTxnInfo().getSTATUS()).isEqualToIgnoringCase("TXN_FAILURE");
    softly.assertAll();
  }

  @Owner(Constants.Owner.ABHISHEK_VERMA)
  @Feature("PGP-37987")
  @Test(description = "Verify successful DINERS transaction with Token")
  public void successfulDinersCardTransactionWithToken() throws Exception {
    String custId= RandomStringUtils.randomAlphabetic(4)+ Instant.now().toEpochMilli();
    SavedCardHelpers.addCardOnMidCustId(corporateMerchant,custId,PaymentDTO.EXP_MONTH,PaymentDTO.EXP_YEAR,PaymentDTO.DINERS_CARD_NUMBER);
    String tokenIndexNumber = SavedCardHelpers.getTin();
    GenerateTokenData generateTokenData = new GenerateTokenData(corporateMerchant.getId()).buildRequest("ECOM",tokenIndexNumber,custId,"").generateChecksum(corporateMerchant.getKey());
    JsonPath generateTokenDataResponse = generateTokenData.execute().jsonPath();

    InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, corporateMerchant).setTxnValue("10").setCustId(custId).build();
    InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);

    //checking if token is diners CC or not
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(PGPHelpers.getBinDetails(generateTokenDataResponse.getString("body.tokenInfo.tokenBin")).getCardName()).isEqualTo("DINERS");
    softly.assertThat(PGPHelpers.getBinDetails(generateTokenDataResponse.getString("body.tokenInfo.tokenBin")).getCardType()).isEqualTo("CREDIT_CARD");
    softly.assertAll();

    String orderId = initTxnDTO.getBody().getOrderId();
    CardTokenInfo cardTokenInfo = new CardTokenInfo();
    cardTokenInfo.setCardToken(generateTokenDataResponse.getString("body.cardToken"));
    cardTokenInfo.setTokenExpiry(generateTokenDataResponse.getString("body.tokenInfo.tokenExpiry"));
    cardTokenInfo.setTavv(generateTokenDataResponse.getString("body.tavv"));
    cardTokenInfo.setCardSuffix(generateTokenDataResponse.getString("body.tokenInfo.cardSuffix"));
    cardTokenInfo.setMerchantTokenRequestorId(generateTokenDataResponse.getString("body.tokenInfo.tokenRequestorId"));
    cardTokenInfo.setTokenUniqueReference(generateTokenDataResponse.getString("body.tokenInfo.tokenUniqueReference"));
    ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
        .Builder(corporateMerchant.getId(),initTxnResponseDTO.getBody().getTxnToken(),orderId)
        .setPaymentMode("CREDIT_CARD")
        .setCardInfo("||123|")
        .setcardTokenInfo(cardTokenInfo)
        .setAuthMode("otp")
        .build();
    ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
    NativeHelpers.submitJsonFormInBrowser(processTxnV1Response.toString());
    TxnStatus txnStatus = new TxnStatus(corporateMerchant.getId(),orderId);
    txnStatus.executeUntilNotPending();
    txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY).
        validateStatus("TXN_SUCCESS").
        validatePaymentMode("CC").AssertAll();
  }

//@Owner(Constants.Owner.ABHISHEK_VERMA)
//  @Feature("PGP-37987")
//  @Test(description = "Verify transaction fails when card number is invalid")
//  public void transactionFailsWithInvalidCardNumber() throws Exception {
//    String custId= RandomStringUtils.randomAlphabetic(4)+ Instant.now().toEpochMilli();
//    SavedCardHelpers.addCardOnMidCustId(coftMerchant3P,custId,PaymentDTO.EXP_MONTH,PaymentDTO.EXP_YEAR,"1234567890123456"); // Invalid card number
//    String tokenIndexNumber = SavedCardHelpers.getTin();
//    GenerateTokenData generateTokenData = new GenerateTokenData(coftMerchant3P.getId()).buildRequest("ECOM",tokenIndexNumber,custId,"").generateChecksum(coftMerchant3P.getKey());
//    JsonPath generateTokenDataResponse = generateTokenData.execute().jsonPath();
//
//    InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, coftMerchant3P).setTxnValue("10").setCustId(custId).build();
//    InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
//
//    String orderId = initTxnDTO.getBody().getOrderId();
//    CardTokenInfo cardTokenInfo = new CardTokenInfo();
//    cardTokenInfo.setCardToken(generateTokenDataResponse.getString("body.cardToken"));
//    cardTokenInfo.setTokenExpiry(generateTokenDataResponse.getString("body.tokenInfo.tokenExpiry"));
//    cardTokenInfo.setTavv(generateTokenDataResponse.getString("body.tavv"));
//    cardTokenInfo.setCardSuffix(generateTokenDataResponse.getString("body.tokenInfo.cardSuffix"));
//    cardTokenInfo.setPanUniqueReference(generateTokenDataResponse.getString("body.tokenInfo.panUniqueReference"));
//    ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
//        .Builder(coftMerchant3P.getId(),initTxnResponseDTO.getBody().getTxnToken(),orderId)
//        .setPaymentMode("CREDIT_CARD")
//        .setCardInfo("||123|")
//        .setcardTokenInfo(cardTokenInfo)
//        .setAuthMode("otp")
//        .build();
//    ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
//    SoftAssertions softAssertions = new SoftAssertions();
//    softAssertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultStatus())
//        .isEqualTo("F");
//    softAssertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultCode())
//        .isEqualTo("0001");
//    softAssertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultMsg())
//        .isEqualTo("Invalid Card Number");
//    softAssertions.assertAll();
//  }
  /*
  Remaining cases
  1. Diners: Done
  2 EMI_DC
  3. Affordability EMI
  5. Promo
  6. Simplified Promo
  7. ADD_MONEY
  8. ADD_AND_PAY
   */

}

