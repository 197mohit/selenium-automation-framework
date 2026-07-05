package scripts.coft.theia;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.api.TxnStatus;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.appconstants.Constants;
import com.paytm.appconstants.Constants.MerchantType;
import com.paytm.appconstants.Constants.assetType;
import com.paytm.apphelpers.LogsValidationHelper;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.PG2LogsValidationHelper;
import com.paytm.apphelpers.PG2LogsValidationHelper.setEnvService;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.processTransactionV1.CoftConsent;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.dto.processTransactionV1.response.ProcessTxnV1Response;
import com.paytm.utils.ff4j.FF4JFlags;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.Year;
import java.util.ArrayList;
import java.util.Date;
import org.apache.commons.lang.RandomStringUtils;
import org.assertj.core.api.SoftAssertions;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class IssuerTokenizationOnGuestCheckout extends PGPBaseTest {

  private static final ObjectMapper MAPPER = new ObjectMapper();
  MerchantType hdfcIssuerEnabledMerchant = MerchantType.HDFC_ISSUER_TOKEN_GUEST_CHECKOUT;
  MerchantType axisIssuerEnabledMerchant = MerchantType.AXIS_ISSUER_TOKEN_GUEST_CHECKOUT;
  MerchantType iciciIssuerEnabledMerchant = MerchantType.ICICI_ISSUER_TOKEN_GUEST_CHECKOUT;
  String custId;
  SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss a");
  String consentDate = formatter.format(new Date());

  @BeforeClass
  public void setFf4j() {
    FF4JFlags.enable("returnSavedCardsFromPlatformForMidCustId");
    FF4JFlags.enable("returnSavedCardsFromPlatformForUserId");
  }

  @Owner(Constants.Owner.ABHISHEK_VERMA)
  @Feature("PAPR-4802")
  @Test(description = "Verify e2e txn with HDFC issuer token when ff4j enableIssuerForTokenizationInGuestCheckout is ON")
  public void issuerTokenGuestCheckoutHDFC() {
    custId = RandomStringUtils.randomAlphabetic(10) + Instant.now().toEpochMilli();
    CoftConsent coftConsent = new CoftConsent();
    coftConsent.setUserConsent("1");
    coftConsent.setCreatedAt(consentDate);
    coftConsent.setUserConsentId(custId);
    PaymentDTO paymentDTO = new PaymentDTO();

    InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, hdfcIssuerEnabledMerchant)
        .setTxnValue("2")
        .setCustId(custId)
        .build();
    InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
    ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
        hdfcIssuerEnabledMerchant.getId(), initTxnResponse.getBody().getTxnToken(),
        initTxnDTO.getBody().getOrderId())
        .setPaymentMode("CREDIT_CARD")
        .setAuthMode("otp")
        .setCardInfo(
            "|" + PaymentDTO.HDFC_GUEST_CHECKOUT_ISSUER_CARD + "|" + paymentDTO.getCvvNumber() + "|"
                + paymentDTO.getExpMonth() + (Year.now().getValue() + 5))
        .setCoftConset(coftConsent)
        .build();
    ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(
        processTxnV1Request);
    NativeHelpers.submitJsonFormInBrowser(processTxnV1Response.toString());
    TxnStatus txnStatus = new TxnStatus(hdfcIssuerEnabledMerchant.getId(),
        initTxnDTO.getBody().getOrderId());
    txnStatus.executeUntilNotPending();
    txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY).
        validateStatus("TXN_SUCCESS").
        validatePaymentMode("CC").AssertAll();
    String asset_center_request;
    String gc_generate_token_response;
    try {
      asset_center_request = LogsValidationHelper.verifyLogsOnPod(
          PG2LogsValidationHelper.setEnvService.theia_facade, initTxnDTO.getBody().getOrderId(),
          "asset-service/asset/v2/cache/save", "REQUEST");
      gc_generate_token_response = LogsValidationHelper.verifyLogsOnPod(
          PG2LogsValidationHelper.setEnvService.theia_facade, initTxnDTO.getBody().getOrderId(),
          "/v2/token/gc/generateTokenData", "RESPONSE");
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
    JsonNode gcGenerateTokenJson = extractResponseJson(gc_generate_token_response);
    JsonNode tokens = gcGenerateTokenJson.path("body").path("tokens");
    JsonNode networkTokenInfo = tokens.get(0).path("tokenInfo");
    JsonNode issuerTokenInfo = tokens.get(1).path("tokenInfo");
    JsonNode assetCenterJson = extractResponseJson(asset_center_request);

    SoftAssertions softAssertions = new SoftAssertions();
    //network token assertions
    softAssertions.assertThat(assetCenterJson.path("assetNo ").asText()).contains(networkTokenInfo.path("tokenSuffix").asText());
    softAssertions.assertThat(assetCenterJson.path("assetType").asText()).isEqualTo(assetType.ALT_TOKEN.name());
    softAssertions.assertThat(assetCenterJson.path("source").asText()).isEqualTo("Theia");
    softAssertions.assertThat(assetCenterJson.path("assetExpireYear").asText())
        .contains(networkTokenInfo.path("tokenExpiry").asText().substring(4,6));
    softAssertions.assertThat(assetCenterJson.path("assetExpireMonth").asText())
        .isNotNull();
    softAssertions.assertThat(assetCenterJson.path("globalPanIndex").asText())
        .isEqualTo(networkTokenInfo.path("globalPanIndex").asText());
    softAssertions.assertThat(assetCenterJson.path("par").asText())
        .isEqualTo(networkTokenInfo.path("panUniqueReference").asText());
    softAssertions.assertThat(assetCenterJson.path("last4ref").asText())
        .isEqualTo(networkTokenInfo.path("cardSuffix").asText());
    softAssertions.assertThat(assetCenterJson.path("isPersist").asText())
        .isNotNull();
    softAssertions.assertThat(assetCenterJson.path("isRequiredPostAuth").asText())
        .isNotNull();
    softAssertions.assertThat(assetCenterJson.path("providerType").asText()).isEqualTo("CARD_NETWORK");
    softAssertions.assertThat(assetCenterJson.path("tavv").asText()).isNotNull();

    //issuer token assertions
    softAssertions.assertThat(assetCenterJson.path("additionalAsset").path("assetNo ").asText()).contains(issuerTokenInfo.path("tokenSuffix").asText());
    softAssertions.assertThat(assetCenterJson.path("additionalAsset").path("assetExpireYear").asText())
        .contains(issuerTokenInfo.path("tokenExpiry").asText().substring(4,6));
    softAssertions.assertThat(assetCenterJson.path("additionalAsset").path("assetExpireMonth").asText())
        .isNotNull();
    softAssertions.assertThat(assetCenterJson.path("additionalAsset").path("providerType").asText()).isEqualTo("CARD_ISSUER");
    softAssertions.assertThat(assetCenterJson.path("additionalAsset").path("tokenUniqueReference").asText()).isEqualTo(issuerTokenInfo.path("tokenUniqueReference").asText());
    softAssertions.assertThat(assetCenterJson.path("additionalAsset").path("tokenRequestorId").asText()).isEqualTo(issuerTokenInfo.path("tokenRequestorId").asText());
    softAssertions.assertThat(assetCenterJson.path("additionalAsset").path("encryptedAssetData").asText()).isNotNull();

    String insta_payment_request = LogsValidationHelper.verifyLogsOnPod(
        setEnvService.instaproxy, initTxnDTO.getBody().getOrderId(),
        "pg.router.paytm.bankcard.authenticate.request");
    softAssertions.assertThat(insta_payment_request).contains("\"TOKEN_TYPE_TXN\":\"ISSUER_TOKEN\"");
    softAssertions.assertThat(insta_payment_request).contains("\"Transaction_via\":ALT_ID\"");
    softAssertions.assertThat(insta_payment_request).contains("\"appId\":\"HDFCC1IN02\"");
    softAssertions.assertAll();
  }

  @Owner(Constants.Owner.ABHISHEK_VERMA)
  @Feature("PAPR-6427")
  @Test(description = "Verify e2e txn with Axis issuer token when ff4j enableIssuerForTokenizationInGuestCheckout is ON")
  public void issuerTokenGuestCheckoutAxis() {
    custId = RandomStringUtils.randomAlphabetic(10) + Instant.now().toEpochMilli();
    CoftConsent coftConsent = new CoftConsent();
    coftConsent.setUserConsent("1");
    coftConsent.setCreatedAt(consentDate);
    coftConsent.setUserConsentId(custId);
    PaymentDTO paymentDTO = new PaymentDTO();

    InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, axisIssuerEnabledMerchant)
        .setTxnValue("2")
        .setCustId(custId)
        .build();
    InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
    ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
        axisIssuerEnabledMerchant.getId(), initTxnResponse.getBody().getTxnToken(),
        initTxnDTO.getBody().getOrderId())
        .setPaymentMode("CREDIT_CARD")
        .setAuthMode("otp")
        .setCardInfo(
            "|" + PaymentDTO.AXIS_GUEST_CHECKOUT_ISSUER_CARD + "|" + paymentDTO.getCvvNumber() + "|"
                + paymentDTO.getExpMonth() + (Year.now().getValue() + 5))
        .setCoftConset(coftConsent)
        .build();
    ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(
        processTxnV1Request);
    NativeHelpers.submitJsonFormInBrowser(processTxnV1Response.toString());
    String asset_center_request;
    String gc_generate_token_response;
    try {
      asset_center_request = LogsValidationHelper.verifyLogsOnPod(
          PG2LogsValidationHelper.setEnvService.theia_facade, initTxnDTO.getBody().getOrderId(),
          "asset-service/asset/v2/cache/save", "REQUEST");
      gc_generate_token_response = LogsValidationHelper.verifyLogsOnPod(
          PG2LogsValidationHelper.setEnvService.theia_facade, initTxnDTO.getBody().getOrderId(),
          "/v2/token/gc/generateTokenData", "RESPONSE");
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
    JsonNode gcGenerateTokenJson = extractResponseJson(gc_generate_token_response);
    JsonNode tokens = gcGenerateTokenJson.path("body").path("tokens");
    JsonNode networkTokenInfo = tokens.get(0).path("tokenInfo");
    JsonNode issuerTokenInfo = tokens.get(1).path("tokenInfo");
    JsonNode assetCenterJson = extractResponseJson(asset_center_request);

    SoftAssertions softAssertions = new SoftAssertions();
    //network token assertions
    softAssertions.assertThat(assetCenterJson.path("assetNo ").asText()).contains(networkTokenInfo.path("tokenSuffix").asText());
    softAssertions.assertThat(assetCenterJson.path("assetType").asText()).isEqualTo(assetType.ALT_TOKEN.name());
    softAssertions.assertThat(assetCenterJson.path("source").asText()).isEqualTo("Theia");
    softAssertions.assertThat(assetCenterJson.path("assetExpireYear").asText())
        .contains(networkTokenInfo.path("tokenExpiry").asText().substring(4,6));
    softAssertions.assertThat(assetCenterJson.path("assetExpireMonth").asText())
        .isNotNull();
    softAssertions.assertThat(assetCenterJson.path("globalPanIndex").asText())
        .isEqualTo(networkTokenInfo.path("globalPanIndex").asText());
    softAssertions.assertThat(assetCenterJson.path("par").asText())
        .isEqualTo(networkTokenInfo.path("panUniqueReference").asText());
    softAssertions.assertThat(assetCenterJson.path("last4ref").asText())
        .isEqualTo(networkTokenInfo.path("cardSuffix").asText());
    softAssertions.assertThat(assetCenterJson.path("isPersist").asText())
        .isNotNull();
    softAssertions.assertThat(assetCenterJson.path("isRequiredPostAuth").asText())
        .isNotNull();
    softAssertions.assertThat(assetCenterJson.path("providerType").asText()).isEqualTo("CARD_NETWORK");
    softAssertions.assertThat(assetCenterJson.path("tavv").asText()).isNotNull();

    //issuer token assertions
    softAssertions.assertThat(assetCenterJson.path("additionalAsset").path("assetNo ").asText()).contains(issuerTokenInfo.path("tokenSuffix").asText());
    softAssertions.assertThat(assetCenterJson.path("additionalAsset").path("assetExpireYear").asText())
        .contains(issuerTokenInfo.path("tokenExpiry").asText().substring(4,6));
    softAssertions.assertThat(assetCenterJson.path("additionalAsset").path("assetExpireMonth").asText())
        .isNotNull();
    softAssertions.assertThat(assetCenterJson.path("additionalAsset").path("providerType").asText()).isEqualTo("CARD_ISSUER");
    softAssertions.assertThat(assetCenterJson.path("additionalAsset").path("tokenUniqueReference").asText()).isEqualTo(issuerTokenInfo.path("tokenUniqueReference").asText());
    softAssertions.assertThat(assetCenterJson.path("additionalAsset").path("tokenRequestorId").asText()).isEqualTo(issuerTokenInfo.path("tokenRequestorId").asText());
    softAssertions.assertThat(assetCenterJson.path("additionalAsset").path("encryptedAssetData").asText()).isNotNull();

    String insta_payment_request = LogsValidationHelper.verifyLogsOnPod(
        setEnvService.instaproxy, initTxnDTO.getBody().getOrderId(),
        "pg.router.paytm.bankcard.authenticate.request");
    softAssertions.assertThat(insta_payment_request).contains("\"TOKEN_TYPE_TXN\":\"ISSUER_TOKEN\"");
    softAssertions.assertThat(insta_payment_request).contains("\"Transaction_via\":ALT_ID\"");
    softAssertions.assertThat(insta_payment_request).contains("\"appId\":\"AXISC1IN02\"");
    softAssertions.assertAll();
  }

  @Owner(Constants.Owner.ABHISHEK_VERMA)
  @Feature("PAPR-6605")
  @Test(description = "Verify e2e txn with ICICI issuer token when ff4j enableIssuerForTokenizationInGuestCheckout is ON")
  public void issuerTokenGuestCheckoutICICI() {
    custId = RandomStringUtils.randomAlphabetic(10) + Instant.now().toEpochMilli();
    CoftConsent coftConsent = new CoftConsent();
    coftConsent.setUserConsent("1");
    coftConsent.setCreatedAt(consentDate);
    coftConsent.setUserConsentId(custId);
    PaymentDTO paymentDTO = new PaymentDTO();

    InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, iciciIssuerEnabledMerchant)
        .setTxnValue("2")
        .setCustId(custId)
        .build();
    InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
    ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
        iciciIssuerEnabledMerchant.getId(), initTxnResponse.getBody().getTxnToken(),
        initTxnDTO.getBody().getOrderId())
        .setPaymentMode("CREDIT_CARD")
        .setAuthMode("otp")
        .setCardInfo(
            "|" + PaymentDTO.ICICI_GUEST_CHECKOUT_ISSUER_CARD + "|" + paymentDTO.getCvvNumber() + "|"
                + paymentDTO.getExpMonth() + (Year.now().getValue() + 5))
        .setCoftConset(coftConsent)
        .build();
    ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(
        processTxnV1Request);
    NativeHelpers.submitJsonFormInBrowser(processTxnV1Response.toString());
    String asset_center_request;
    String gc_generate_token_response;
    String gc_generate_token_request;
    try {
      asset_center_request = LogsValidationHelper.verifyLogsOnPod(
          PG2LogsValidationHelper.setEnvService.theia_facade, initTxnDTO.getBody().getOrderId(),
          "asset-service/asset/v2/cache/save", "REQUEST");
      gc_generate_token_request = LogsValidationHelper.verifyLogsOnPod(
          PG2LogsValidationHelper.setEnvService.theia_facade, initTxnDTO.getBody().getOrderId(),
          "/v2/token/gc/generateTokenData", "REQUEST");
      gc_generate_token_response = LogsValidationHelper.verifyLogsOnPod(
          PG2LogsValidationHelper.setEnvService.theia_facade, initTxnDTO.getBody().getOrderId(),
          "/v2/token/gc/generateTokenData", "RESPONSE");
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
    JsonNode gcGenerateTokenJson = extractResponseJson(gc_generate_token_response);
    JsonNode tokens = gcGenerateTokenJson.path("body").path("tokens");
    JsonNode networkTokenInfo = tokens.get(0).path("tokenInfo");
    JsonNode assetCenterJson = extractResponseJson(asset_center_request);

    SoftAssertions softAssertions = new SoftAssertions();
    //request assertion
    softAssertions.assertThat(gc_generate_token_request).doesNotContain("CARD_ISSUER");
    //network token assertions
    softAssertions.assertThat(assetCenterJson.path("assetNo ").asText()).contains(networkTokenInfo.path("tokenSuffix").asText());
    softAssertions.assertThat(assetCenterJson.path("assetType").asText()).isEqualTo(assetType.ALT_TOKEN.name());
    softAssertions.assertThat(assetCenterJson.path("source").asText()).isEqualTo("Theia");
    softAssertions.assertThat(assetCenterJson.path("assetExpireYear").asText())
        .contains(networkTokenInfo.path("tokenExpiry").asText().substring(4,6));
    softAssertions.assertThat(assetCenterJson.path("assetExpireMonth").asText())
        .isNotNull();
    softAssertions.assertThat(assetCenterJson.path("globalPanIndex").asText())
        .isEqualTo(networkTokenInfo.path("globalPanIndex").asText());
    softAssertions.assertThat(assetCenterJson.path("par").asText())
        .isEqualTo(networkTokenInfo.path("panUniqueReference").asText());
    softAssertions.assertThat(assetCenterJson.path("last4ref").asText())
        .isEqualTo(networkTokenInfo.path("cardSuffix").asText());
    softAssertions.assertThat(assetCenterJson.path("isPersist").asText())
        .isNotNull();
    softAssertions.assertThat(assetCenterJson.path("isRequiredPostAuth").asText())
        .isNotNull();
    softAssertions.assertThat(assetCenterJson.path("providerType").asText()).isEqualTo("CARD_NETWORK");
    softAssertions.assertThat(assetCenterJson.path("tavv").asText()).isNotNull();

    //issuer token assertions
    softAssertions.assertThat(assetCenterJson.path("additionalAsset").path("assetNo ").asText()).contains(PaymentDTO.ICICI_GUEST_CHECKOUT_ISSUER_CARD.substring(12,15));
    softAssertions.assertThat(assetCenterJson.path("additionalAsset").path("assetExpireYear").asText())
        .contains(String.valueOf(Year.now().getValue() + 4).substring(2,4));
    softAssertions.assertThat(assetCenterJson.path("additionalAsset").path("assetExpireMonth").asText())
        .isNotNull();
    softAssertions.assertThat(assetCenterJson.path("additionalAsset").path("providerType").asText()).isEqualTo("CARD_ISSUER");
    softAssertions.assertThat(assetCenterJson.path("additionalAsset").path("assetType").asText()).isEqualTo(
        assetType.ISO_CARD.name());
    softAssertions.assertThat(assetCenterJson.path("additionalAsset").path("cvv2").asText()).isNotNull();
    softAssertions.assertThat(assetCenterJson.path("additionalAsset").path("encryptedAssetData").asText()).isNotNull();

    String insta_payment_request = LogsValidationHelper.verifyLogsOnPod(
        setEnvService.instaproxy, initTxnDTO.getBody().getOrderId(),
        "pg.router.paytm.bankcard.authenticate.request");
    softAssertions.assertThat(insta_payment_request).contains("\"TOKEN_TYPE_TXN\":\"ISSUER_TOKEN\"");
    softAssertions.assertThat(insta_payment_request).contains("\"Transaction_via\":ALT_ID\"");
    softAssertions.assertThat(insta_payment_request).contains("\"appId\":\"ICILC1IN02\"");
    softAssertions.assertAll();
  }


  //helper methods
  public static JsonNode extractResponseJson(String logger) {
    if (logger == null) {
      throw new IllegalArgumentException("logger cannot be null");
    }
    try {
      // Try fast path when full line is valid JSON and entity is clean
      try {
        JsonNode root = MAPPER.readTree(logger);
        JsonNode entityNode = root.path("RESPONSE").path("entity");
        if (!entityNode.isMissingNode() && !entityNode.isNull()) {
          String entityJson = entityNode.isTextual() ? entityNode.asText() : entityNode.toString();
          return MAPPER.readTree(entityJson);
        }
      } catch (Exception ignore) {
        // fallthrough to resilient extraction
      }

      String jsonCandidate = extractBalancedJsonAfter(logger, "[\\\"']?entity[\\\"']?\\s*:");
      if (jsonCandidate == null) {
        jsonCandidate = extractBalancedJsonAfter(logger, "\\bentity\\s*:");
      }
      if (jsonCandidate == null) {
        jsonCandidate = extractBalancedJsonAfter(logger, "[\\\"']?entity[\\\"']?\\s*=");
      }
      if (jsonCandidate == null) {
        jsonCandidate = extractBalancedJsonAfter(logger, "\\bentity\\s*=");
      }
      if (jsonCandidate == null) {
        throw new IllegalArgumentException("Could not locate entity JSON in log line");
      }
      String sanitized = sanitizeLoggingArtifacts(jsonCandidate);
      String balanced = ensureBalancedBraces(sanitized);
      return MAPPER.readTree(balanced);
    } catch (Exception e) {
      throw new RuntimeException("Failed to parse logger and extract RESPONSE.entity JSON", e);
    }
  }

  private static String extractBalancedJsonAfter(String text, String anchorRegex) {
    Pattern anchor = Pattern.compile(anchorRegex, Pattern.CASE_INSENSITIVE);
    Matcher m = anchor.matcher(text);
    if (!m.find()) {
      return null;
    }
    int idx = m.end();
    while (idx < text.length() && text.charAt(idx) != '{') {
      idx++;
    }
    if (idx >= text.length()) {
      return null;
    }
    int start = idx;
    int depth = 0;
    boolean inString = false;
    boolean escaped = false;
    for (int i = start; i < text.length(); i++) {
      char c = text.charAt(i);
      if (inString) {
        if (escaped) {
          escaped = false;
        } else if (c == '\\') {
          escaped = true;
        } else if (c == '"') {
          inString = false;
        }
      } else {
        if (c == '"') {
          inString = true;
        } else if (c == '{') {
          depth++;
        } else if (c == '}') {
          depth--;
          if (depth == 0) {
            return text.substring(start, i + 1);
          }
        }
      }
    }
    return null;
  }

  private static String sanitizeLoggingArtifacts(String s) {
    String out = s;
    // Collapse duplicate quotes introduced by logging
    out = out.replaceAll("\"\"", "\"");
    // Remove whitespace between end quote and comma/brace (e.g., '"  ,')
    out = out.replaceAll("\"\\s+,", "\",");
    out = out.replaceAll("\"\\s+}", "\"}");
    // Trim control characters
    out = out.replaceAll("[\r\n\t]", "");
    out = out.trim();
    return out;
  }

  private static String ensureBalancedBraces(String s) {
    int depth = 0;
    boolean inString = false;
    boolean escaped = false;
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (inString) {
        if (escaped) {
          escaped = false;
        } else if (c == '\\') {
          escaped = true;
        } else if (c == '"') {
          inString = false;
        }
      } else {
        if (c == '"') {
          inString = true;
        } else if (c == '{') {
          depth++;
        } else if (c == '}') {
          depth--;
        }
      }
    }
    if (depth > 0) {
      StringBuilder sb = new StringBuilder(s);
      for (int i = 0; i < depth; i++) {
        sb.append('}');
      }
      return sb.toString();
    }
    return s;
  }
}
