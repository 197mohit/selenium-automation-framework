package scripts.coft.theia;

import com.mongodb.client.MongoCollection;
import com.paytm.LocalConfig;
import com.paytm.api.TxnStatus;
import com.paytm.api.coft.saveCard.SavedCardByMidCustId;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.appconstants.Constants;
import com.paytm.appconstants.Constants.MerchantType;
import com.paytm.appconstants.Constants.TokenStatus;
import com.paytm.apphelpers.LogsValidationHelper;
import com.paytm.apphelpers.MongoDBHelper;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.PG2LogsValidationHelper;
import com.paytm.apphelpers.RedisHelper;
import com.paytm.apphelpers.SavedCardHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.processTransactionV1.CoftConsent;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.dto.processTransactionV1.response.ProcessTxnV1Response;
import com.paytm.utils.ff4j.FF4JFlags;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.Year;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import org.apache.commons.lang.RandomStringUtils;
import org.assertj.core.api.SoftAssertions;
import org.bson.Document;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class IssuerTokenization extends PGPBaseTest {

  MerchantType issuerEnabledMerchant = MerchantType.ISSUER_TOKEN_3P;
  MerchantType issuerDisabledMerchant = MerchantType.ISSUER_TOKEN_3P_DISABLE;
  MerchantType issuerEnabledMerchantONUS = MerchantType.COFT_MERCHANT;
  RedisHelper redisHelper= RedisHelper.getInstance(LocalConfig.TRANSACTIONAL_REDIS_CLUSTER_URI,LocalConfig.PG_REDIS_CLUSTER_PASS);
  String custId;
  SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss a");
  String consentDate = formatter.format(new Date());
  ArrayList<String> providerType = new ArrayList<>();
  MongoDBHelper mongoDBHelper = new MongoDBHelper();

  public IssuerTokenization() throws InterruptedException {
    providerType.add("CARD_NETWORK");
    providerType.add("CARD_ISSUER");
  }

  @BeforeClass
  public void setFf4j() {
    FF4JFlags.enable("returnSavedCardsFromPlatformForMidCustId");
    redisHelper.delete("FF4J_FEATURE_returnSavedCardsFromPlatformForMidCustId");
    FF4JFlags.enable("returnSavedCardsFromPlatformForUserId");
    redisHelper.delete("FF4J_FEATURE_returnSavedCardsFromPlatformForUserId");
    FF4JFlags.enable("insta.createIssuerToken");
    redisHelper.delete("FF4J_FEATURE_insta.createIssuerToken");
    FF4JFlags.enable("theia.enableIssuerTokenization");
    redisHelper.delete("FF4J_FEATURE_theia.enableIssuerTokenization");
  }

  @Owner(Constants.Owner.ABHISHEK_VERMA)
  @Feature("PAPR-4802")
  @Test(description = "Verify issuer token is saved on 3P merchant in case of successful transaction")
  public void issuerTokenIsSavedInPTC_3PMerchant() {
    custId = RandomStringUtils.randomAlphabetic(10) + Instant.now().toEpochMilli();
    CoftConsent coftConsent = new CoftConsent();
    coftConsent.setUserConsent("1");
    coftConsent.setCreatedAt(consentDate);
    coftConsent.setUserConsentId(custId);
    PaymentDTO paymentDTO = new PaymentDTO();

    InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, issuerEnabledMerchant)
        .setTxnValue("2")
        .setCustId(custId)
        .build();
    InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
    ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
        issuerEnabledMerchant.getId(), initTxnResponse.getBody().getTxnToken(),
        initTxnDTO.getBody().getOrderId())
        .setPaymentMode("CREDIT_CARD")
        .setAuthMode("otp")
        .setCardInfo("|" + paymentDTO.VISA_COFT_CARD_NUMBER + "|" + paymentDTO.getCvvNumber() + "|"
            + paymentDTO.getExpMonth() + String.valueOf(Year.now().getValue() + 5))
        .setCoftConset(coftConsent)
        .build();
    ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(
        processTxnV1Request);
    NativeHelpers.submitJsonFormInBrowser(processTxnV1Response.toString());
    TxnStatus txnStatus = new TxnStatus(issuerEnabledMerchant.getId(),
        initTxnDTO.getBody().getOrderId());
    txnStatus.executeUntilNotPending();
    txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY).
        validateStatus("TXN_SUCCESS").
        validatePaymentMode("CC").AssertAll();

    SavedCardByMidCustId savedCardByMidCustId = new SavedCardByMidCustId().buildRequest(
        issuerEnabledMerchant,
        custId, "CHECKSUM", providerType);
    JsonPath saveCardByMidCustIdResponse = savedCardByMidCustId.execute().jsonPath();
    if (saveCardByMidCustIdResponse.getList("response").size() == 0) {
      try {
        Thread.sleep((long) (10 * 1000));
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
      saveCardByMidCustIdResponse = savedCardByMidCustId.execute().jsonPath();
    }
    SoftAssertions softAssertions = new SoftAssertions();
    softAssertions.assertThat(saveCardByMidCustIdResponse.getString("response[1].subAssetType"))
        .isEqualTo("CARD_ISSUER");
    softAssertions.assertAll();
  }

  @Owner(Constants.Owner.ABHISHEK_VERMA)
  @Feature("PAPR-4802")
  @Test(description = "Verify user is able to do transaction via issuerToken ")
  public void transactionViaIssuerToken_3PMerchant() {
    MongoCollection<Document> collection = MongoDBHelper.getCollection("TOKEN_DATA");
    MongoDBHelper.deleteDeadTokens(collection);
    custId = RandomStringUtils.randomAlphabetic(10) + Instant.now().toEpochMilli();
    PaymentDTO paymentDTO = new PaymentDTO();
    SavedCardHelpers.addIssuerTokenOnMidCustId(MerchantType.ISSUER_TOKEN_3P, custId,
        paymentDTO.EXP_MONTH, String.valueOf(Year.now().getValue() + 5), paymentDTO.ISSUER_TOKENIZATION_VISA_CARD,
        providerType);

    InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, issuerEnabledMerchant)
        .setTxnValue("2")
        .setCustId(custId)
        .build();
    InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);

    ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
        issuerEnabledMerchant.getId(), initTxnResponse.getBody().getTxnToken(),
        initTxnDTO.getBody().getOrderId())
        .setPaymentMode("CREDIT_CARD")
        .setAuthMode("otp")
        .setCardInfo(SavedCardHelpers.getTin() + "||" + paymentDTO.getCvvNumber() + "|")
        .build();
    ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(
        processTxnV1Request);
    SoftAssertions softAssertions = new SoftAssertions();
    softAssertions.assertThat(
        processTxnV1Response.getBody().getResultInfo().getResultStatus().equalsIgnoreCase("S"));
    softAssertions.assertAll();
    NativeHelpers.submitJsonFormInBrowser(processTxnV1Response.toString());
    TxnStatus txnStatus = new TxnStatus(issuerEnabledMerchant.getId(),
        initTxnDTO.getBody().getOrderId());
    txnStatus.executeUntilNotPending();
    txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY).
        validateStatus("TXN_SUCCESS").
        validatePaymentMode("CC").AssertAll();

    String logs = LogsValidationHelper.verifyLogsOnPod(
        PG2LogsValidationHelper.setEnvService.instaproxy, initTxnDTO.getBody().getOrderId(),
        "directPassThroughInfo");
    System.out.println(logs);
    ArrayList<String> arrayList = new ArrayList<String>();
    Collections.addAll(arrayList, "tokenRequestorId", "custId", "tavv", "isSavedCard");
    softAssertions.assertThat(
        SavedCardHelpers.verifyParamsPresentInDirectPassthrough(logs, arrayList)).isEqualTo(true);
    softAssertions.assertThat(logs).contains("ISSUER_TOKEN");
    softAssertions.assertAll();
  }

  @Owner(Constants.Owner.ABHISHEK_VERMA)
  @Feature("PAPR-4802")
  @Test(description = "Verify transaction is going via network token when issuer token is not in active state")
  public void transactionViaNetworkTokenWhenIssuerTokenDead_3PMerchant() {
    custId = RandomStringUtils.randomAlphabetic(10) + Instant.now().toEpochMilli();
    MongoCollection<Document> collection = MongoDBHelper.getCollection("TOKEN_DATA");
    MongoDBHelper.deleteDeadTokens(collection);
    SavedCardHelpers.updateTokenWithPar("V0010013021361288827541720480", TokenStatus.DEAD);
    PaymentDTO paymentDTO = new PaymentDTO();
    SavedCardHelpers.addIssuerTokenOnMidCustId(MerchantType.ISSUER_TOKEN_3P, custId,
        paymentDTO.EXP_MONTH, String.valueOf(Year.now().getValue() + 5), paymentDTO.ISSUER_TOKENIZATION_VISA_CARD,
        providerType);
    System.out.println("Issuer token is: " + SavedCardHelpers.getIssuerTin());
    SavedCardHelpers.updateTokenStatus(SavedCardHelpers.getIssuerTin(), TokenStatus.DEAD);

    InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, issuerEnabledMerchant)
        .setTxnValue("2")
        .setCustId(custId)
        .build();
    InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);

    ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
        issuerEnabledMerchant.getId(), initTxnResponse.getBody().getTxnToken(),
        initTxnDTO.getBody().getOrderId())
        .setPaymentMode("CREDIT_CARD")
        .setAuthMode("otp")
        .setCardInfo(SavedCardHelpers.getTin() + "||" + paymentDTO.getCvvNumber() + "|")
        .build();
    ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(
        processTxnV1Request);
    SoftAssertions softAssertions = new SoftAssertions();
    softAssertions.assertThat(
        processTxnV1Response.getBody().getResultInfo().getResultStatus().equalsIgnoreCase("S"));
    softAssertions.assertAll();
    NativeHelpers.submitJsonFormInBrowser(processTxnV1Response.toString());
    TxnStatus txnStatus = new TxnStatus(issuerEnabledMerchant.getId(),
        initTxnDTO.getBody().getOrderId());
    txnStatus.executeUntilNotPending();
    txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY).
        validateStatus("TXN_SUCCESS").
        validatePaymentMode("CC").AssertAll();
    String logs = LogsValidationHelper.verifyLogsOnPod(
        PG2LogsValidationHelper.setEnvService.instaproxy, initTxnDTO.getBody().getOrderId(),
        "directPassThroughInfo");
    System.out.println(logs);
    ArrayList<String> arrayList = new ArrayList<String>();
    Collections.addAll(arrayList, "custId", "tavv", "isSavedCard");
    softAssertions.assertThat(
        SavedCardHelpers.verifyParamsPresentInDirectPassthrough(logs, arrayList)).isEqualTo(true);
    softAssertions.assertThat(logs).contains("NETWORK_TOKEN");
    softAssertions.assertAll();
  }

  @Owner(Constants.Owner.ABHISHEK_VERMA)
  @Feature("PAPR-4802")
  @Test(description = "Verify transaction is going via network token when ff4j theia.enableIssuerTokenization is OFF")
  public void transactionViaNetworkTokenWhenFF4JDisabled_3PMerchant() {
    custId = RandomStringUtils.randomAlphabetic(10) + Instant.now().toEpochMilli();
    PaymentDTO paymentDTO = new PaymentDTO();
    SavedCardHelpers.addIssuerTokenOnMidCustId(issuerDisabledMerchant, custId,
        paymentDTO.EXP_MONTH, String.valueOf(Year.now().getValue() + 5), paymentDTO.ISSUER_TOKENIZATION_VISA_CARD,
        providerType);
    MongoCollection<Document> collection = MongoDBHelper.getCollection("TOKEN_DATA");
    MongoDBHelper.deleteDeadTokens(collection);
    InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, issuerDisabledMerchant)
        .setTxnValue("2")
        .setCustId(custId)
        .build();
    InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);

    ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
        issuerDisabledMerchant.getId(), initTxnResponse.getBody().getTxnToken(),
        initTxnDTO.getBody().getOrderId())
        .setPaymentMode("CREDIT_CARD")
        .setAuthMode("otp")
        .setCardInfo(SavedCardHelpers.getTin() + "||" + paymentDTO.getCvvNumber() + "|")
        .build();
    ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(
        processTxnV1Request);
    SoftAssertions softAssertions = new SoftAssertions();
    softAssertions.assertThat(
        processTxnV1Response.getBody().getResultInfo().getResultStatus().equalsIgnoreCase("S"));
    softAssertions.assertAll();
    NativeHelpers.submitJsonFormInBrowser(processTxnV1Response.toString());
    TxnStatus txnStatus = new TxnStatus(issuerDisabledMerchant.getId(),
        initTxnDTO.getBody().getOrderId());
    txnStatus.executeUntilNotPending();
    txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY).
        validateStatus("TXN_SUCCESS").
        validatePaymentMode("CC").AssertAll();
    String logs = LogsValidationHelper.verifyLogsOnPod(
        PG2LogsValidationHelper.setEnvService.instaproxy, initTxnDTO.getBody().getOrderId(),
        "directPassThroughInfo");
    System.out.println(logs);
    ArrayList<String> arrayList = new ArrayList<String>();
    Collections.addAll(arrayList, "custId", "tavv", "isSavedCard");
    softAssertions.assertThat(
        SavedCardHelpers.verifyParamsPresentInDirectPassthrough(logs, arrayList)).isEqualTo(true);
    softAssertions.assertThat(logs).contains("NETWORK_TOKEN");
    softAssertions.assertAll();
  }

  @Owner(Constants.Owner.ABHISHEK_VERMA)
  @Feature("PAPR-4802")
  @Test(description = "Verify issuer token is saved on ONUS merchant in case of successful transaction")
  public void issuerTokenIsSavedInPTC_ONUSMerchant() throws Exception {
    User user = userManager.getForWrite(PGPBaseTest.Label.LOGIN);
    custId = user.custId();
    CoftConsent coftConsent = new CoftConsent();
    coftConsent.setUserConsent("1");
    coftConsent.setCreatedAt(consentDate);
    coftConsent.setUserConsentId(custId);
    PaymentDTO paymentDTO = new PaymentDTO();
    InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), issuerEnabledMerchantONUS)
        .setTxnValue("2")
        .setSsoToken(user.ssoToken())
        .setCustId(custId)
        .build();
    InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);

    ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
        issuerEnabledMerchantONUS.getId(), initTxnResponse.getBody().getTxnToken(),
        initTxnDTO.getBody().getOrderId())
        .setPaymentMode("CREDIT_CARD")
        .setAuthMode("otp")
        .setCardInfo("|" + paymentDTO.VISA_COFT_CARD_NUMBER + "|" + paymentDTO.getCvvNumber() + "|"
            + paymentDTO.getExpMonth() + String.valueOf(Year.now().getValue() + 5))
        .setCoftConset(coftConsent)
        .build();
    ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(
        processTxnV1Request);
    NativeHelpers.submitJsonFormInBrowser(processTxnV1Response.toString());
    TxnStatus txnStatus = new TxnStatus(issuerEnabledMerchantONUS.getId(),
        initTxnDTO.getBody().getOrderId());
    txnStatus.executeUntilNotPending();
    txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY).
        validateStatus("TXN_SUCCESS").
        validatePaymentMode("CC").AssertAll();

    String logs = LogsValidationHelper.verifyLogsOnPod(
        PG2LogsValidationHelper.setEnvService.instaproxy, initTxnDTO.getBody().getOrderId(),
        "Response from COFT");
    SoftAssertions softAssertions = new SoftAssertions();
    softAssertions.assertThat(logs).contains("CARD_ISSUER");
    softAssertions.assertAll();
  }

  @Owner(Constants.Owner.ABHISHEK_VERMA)
  @Feature("PAPR-4802")
  @Test(description = "Verify transaction is going via network token when ff4j theia.enableIssuerTokenization is OFF for ONUS merchant", dependsOnMethods = {"issuerTokenIsSavedInPTC_3PMerchant", "transactionViaIssuerToken_3PMerchant", "transactionViaNetworkTokenWhenIssuerTokenDead_3PMerchant", "issuerTokenIsSavedInPTC_ONUSMerchant"})
  public void transactionViaNetworkTokenWhenFF4JDisabled_ONUSMerchant() throws Exception {
    FF4JFlags.disable("theia.enableIssuerTokenization");
    redisHelper.delete("FF4J_FEATURE_theia.enableIssuerTokenization");
    System.out.println("Feature is now :"+ FF4JFlags.getFeatureValue("theia.enableIssuerTokenization"));
    //adding below check as test case is failing multiple times although FF4J feature has been disabled above
    int i=0;
    while (FF4JFlags.getFeatureValue("theia.enableIssuerTokenization") && i<5)
    {
      FF4JFlags.disable("theia.enableIssuerTokenization");
      redisHelper.delete("FF4J_FEATURE_theia.enableIssuerTokenization");
      i++;
    }
    System.out.println("Feature is now :"+ FF4JFlags.getFeatureValue("theia.enableIssuerTokenization")+ "Updated after: "+i+1+"th attempt");

    User user = userManager.getForWrite(PGPBaseTest.Label.LOGIN);
    custId = user.custId();
    PaymentDTO paymentDTO = new PaymentDTO();
    SavedCardHelpers.addIssuerTokenOnUser(user, PaymentDTO.EXP_MONTH, PaymentDTO.EXP_YEAR, PaymentDTO.VISA_COFT_CARD_NUMBER,providerType,issuerEnabledMerchantONUS.getId());
    MongoCollection<Document> collection = MongoDBHelper.getCollection("TOKEN_DATA");
    MongoDBHelper.deleteDeadTokens(collection);

    InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), issuerEnabledMerchantONUS)
        .setTxnValue("2")
        .setCustId(custId)
        .build();
    InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);

    ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
        issuerEnabledMerchantONUS.getId(), initTxnResponse.getBody().getTxnToken(),
        initTxnDTO.getBody().getOrderId())
        .setPaymentMode("CREDIT_CARD")
        .setAuthMode("otp")
        .setCardInfo(SavedCardHelpers.getTin() + "||" + paymentDTO.getCvvNumber() + "|")
        .build();
    ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(
        processTxnV1Request);
    SoftAssertions softAssertions = new SoftAssertions();
    softAssertions.assertThat(
        processTxnV1Response.getBody().getResultInfo().getResultStatus().equalsIgnoreCase("S"));
    softAssertions.assertAll();
    NativeHelpers.submitJsonFormInBrowser(processTxnV1Response.toString());
    TxnStatus txnStatus = new TxnStatus(issuerEnabledMerchantONUS.getId(),
        initTxnDTO.getBody().getOrderId());
    txnStatus.executeUntilNotPending();
    txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY).
        validateStatus("TXN_SUCCESS").
        validatePaymentMode("CC").AssertAll();
    String logs = LogsValidationHelper.verifyLogsOnPod(
        PG2LogsValidationHelper.setEnvService.instaproxy, initTxnDTO.getBody().getOrderId(),
        "directPassThroughInfo");
    System.out.println(logs);
    ArrayList<String> arrayList = new ArrayList<String>();
    Collections.addAll(arrayList, "custId", "tavv", "isSavedCard");
    softAssertions.assertThat(
        SavedCardHelpers.verifyParamsPresentInDirectPassthrough(logs, arrayList)).isEqualTo(true);
    softAssertions.assertThat(logs).contains("NETWORK_TOKEN");
    softAssertions.assertAll();
    FF4JFlags.enable("theia.enableIssuerTokenization");
    redisHelper.delete("FF4J_FEATURE_theia.enableIssuerTokenization");
  }
}
