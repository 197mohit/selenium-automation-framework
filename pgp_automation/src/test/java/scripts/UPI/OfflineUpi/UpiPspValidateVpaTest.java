package scripts.UPI.OfflineUpi;

import com.paytm.LocalConfig;
import com.paytm.api.upipsp.UpiPspValidateVpa;
import com.paytm.appconstants.Constants;
import com.paytm.appconstants.Constants.MerchantType;
import com.paytm.apphelpers.PGPHelpers;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.assertj.core.api.SoftAssertions;
import org.testng.annotations.Test;

public class UpiPspValidateVpaTest {

  public static Constants.MerchantType offlineMid = Constants.MerchantType.OFFLINE_MID_VALIDATE_VPA;
  private final String payeeVpaOnline="paytm-956917621@ptybl";
  private final String multipleMidVpa = "paytmd.946912699@axis";
  private final String inactiveVpa= "paytms.100072@axis";
  public static Constants.MerchantType ccDisabledBigMid = MerchantType.UPICC_DISABLED_BIG_MERCHANT;
  public static Constants.MerchantType ppiDisabledBigMid = MerchantType.UPI_PPIWALLET_DIABLED_BIG_MERCHANT;
  private final String blockedMerchant="paytm.us7n63jp@pty";
  public static Constants.MerchantType ccDisabledSmallMid = MerchantType.UPICC_DISABLED_SMALL_MERCHANT;
  public static Constants.MerchantType ppiDisabledSmallMid = Constants.MerchantType.UPI_PPIWALLET_DIABLED_SMALL_MERCHANT;
  public static Constants.MerchantType creditlineDisabledSmallMid = Constants.MerchantType.CREDITLINE_DISABLED_SMALL_MERCHANT;
  public static Constants.MerchantType creditlineDisabledBigMid = MerchantType.CREDITLINE_DISABLED_BIG_MERCHANT;
  public static Constants.MerchantType allSubTypeDisabledMerchant = MerchantType.ALL_SUBTYPE_DISABLED_MERCHANT;
  private final String inactiveAcquiringVpa = "paytm.us1083433sb@ptys";  // VPA with inactive acquiring
  private final String nonExistentVpa = "paytm.nonexistent@paytm";
  private final String systemErroVpa= "paytm.us7nalmi@pty";
  private final String inactiveAquiringWithFlagOff="paytm.us7namkv@pty";

/* Note:
Feature tags are returned as per merchant preferences for UPI_CC,PPI Wallet and, Credit Line, Refer to
merchant.yaml for exact pref
*/

  @Feature("PAPR-6100,PGP-56811")
  @Owner(Constants.Owner.ABHISHEK_VERMA)
  @Test(description = "Verify clientId is mandatory in headers")
  public void verifyClientIdMandatory() {
    String epochSeconds = String.valueOf(Instant.now().getEpochSecond());
    UpiPspValidateVpa upiPspValidateVpa = new UpiPspValidateVpa("");  // Empty clientId
    upiPspValidateVpa.buildRequest(offlineMid.getVpa(), null, epochSeconds, null);
    JsonPath response = upiPspValidateVpa.execute().jsonPath();

    SoftAssertions softAssertions = new SoftAssertions();
    softAssertions.assertThat(response.getString("resultCodeId")).isEqualTo("007");
    softAssertions.assertThat(response.getString("resultCode")).isEqualTo("PARAM_ILLEGAL");
    softAssertions.assertThat(response.getString("resultMsg")).isEqualTo("Mandatory params missing");
    softAssertions.assertThat(response.getString("npciErrorCode")).isEqualTo("YG");
    softAssertions.assertThat(response.getString("subResultCode")).isEqualTo("15009");
    softAssertions.assertThat(response.getString("featureTags")).isEqualTo("05|06|31");
    softAssertions.assertAll();
  }

  @Feature("PAPR-6100,PGP-56811")
  @Test(description = "Verify requestTimestamp is mandatory in Request Head")
  public void verifyRequestTimestampMandatory() {
    Map<String, String> tokenMap = new HashMap<>();
    tokenMap.put("vpa", offlineMid.getVpa());
    String jwt = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts,
        LocalConfig.UPI_PSP_PAYMENT_STATUS_JWT_KEY);
    UpiPspValidateVpa upiPspValidateVpa = new UpiPspValidateVpa("OCIL");
    // Not sending requestTimestamp
    upiPspValidateVpa.buildRequest(offlineMid.getVpa(), null, null, jwt);
    JsonPath response = upiPspValidateVpa.execute().jsonPath();

    SoftAssertions softAssertions = new SoftAssertions();
    softAssertions.assertThat(response.getString("resultCodeId")).isEqualTo("007");
    softAssertions.assertThat(response.getString("resultCode")).isEqualTo("PARAM_ILLEGAL");
    softAssertions.assertThat(response.getString("resultMsg")).isEqualTo("Mandatory params missing");
    softAssertions.assertThat(response.getString("npciErrorCode")).isEqualTo("YG");
    softAssertions.assertThat(response.getString("subResultCode")).isEqualTo("15009");
    softAssertions.assertThat(response.getString("featureTags")).isEqualTo("05|06|31");
    softAssertions.assertAll();
  }

  @Feature("PAPR-6100,PGP-56811")
  @Test(description = "Verify signature is mandatory in Request Head")
  public void verifySignatureMandatory() {
    String epochSeconds = String.valueOf(Instant.now().getEpochSecond());
    UpiPspValidateVpa upiPspValidateVpa = new UpiPspValidateVpa("OCIL");
    // Not sending signature (jwt)
    upiPspValidateVpa.buildRequest(offlineMid.getVpa(), null, epochSeconds, null);
    JsonPath response = upiPspValidateVpa.execute().jsonPath();

    SoftAssertions softAssertions = new SoftAssertions();
    softAssertions.assertThat(response.getString("resultCodeId")).isEqualTo("007");
    softAssertions.assertThat(response.getString("resultCode")).isEqualTo("PARAM_ILLEGAL");
    softAssertions.assertThat(response.getString("resultMsg")).isEqualTo("Mandatory params missing");
    softAssertions.assertThat(response.getString("npciErrorCode")).isEqualTo("YG");
    softAssertions.assertThat(response.getString("subResultCode")).isEqualTo("15009");
    softAssertions.assertThat(response.getString("featureTags")).isEqualTo("05|06|31");
    softAssertions.assertAll();
  }

  @Feature("PAPR-6100,PGP-56811")
  @Test(description = "Verify JWT validation")
  public void verifyInvalidJwtKey() {
    String epochSeconds = String.valueOf(Instant.now().getEpochSecond());
    Map<String, String> tokenMap = new HashMap<>();
    tokenMap.put("vpa", offlineMid.getVpa());
    String invalidJwt = "invalid.jwt.token";

    UpiPspValidateVpa upiPspValidateVpa = new UpiPspValidateVpa("OCIL");
    upiPspValidateVpa.buildRequest(offlineMid.getVpa(), null, epochSeconds, invalidJwt);
    JsonPath response = upiPspValidateVpa.execute().jsonPath();

    SoftAssertions softAssertions = new SoftAssertions();
    softAssertions.assertThat(response.getString("resultCode")).isEqualTo("FAIL");
    softAssertions.assertThat(response.getString("resultCodeId")).isEqualTo("009");
    softAssertions.assertThat(response.getString("npciErrorCode")).isEqualTo("YG");
    softAssertions.assertThat(response.getString("subResultCode")).isEqualTo("15007");
    softAssertions.assertThat(response.getString("resultMsg")).isEqualTo("Jwt validation failed");
    softAssertions.assertThat(response.getString("featureTags")).isEqualTo("05|06|31");
    softAssertions.assertAll();
  }

  @Feature("PAPR-6100,PGP-56811")
  @Test(description = "Verify merchantVpa is mandatory param")
  public void verifyMerchantVpaMandatory() {
    String epochSeconds = String.valueOf(Instant.now().getEpochSecond());
    Map<String, String> tokenMap = new HashMap<>();
    tokenMap.put("vpa", offlineMid.getVpa());
    String jwt = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts,
        LocalConfig.UPI_PSP_PAYMENT_STATUS_JWT_KEY);

    UpiPspValidateVpa upiPspValidateVpa = new UpiPspValidateVpa("OCIL");
    // Sending null as merchantVpa
    upiPspValidateVpa.buildRequest(null, null, epochSeconds, jwt);
    JsonPath response = upiPspValidateVpa.execute().jsonPath();

    SoftAssertions softAssertions = new SoftAssertions();
    softAssertions.assertThat(response.getString("resultCodeId")).isEqualTo("007");
    softAssertions.assertThat(response.getString("resultCode")).isEqualTo("PARAM_ILLEGAL");
    softAssertions.assertThat(response.getString("resultMsg")).isEqualTo("Mandatory params missing");
    softAssertions.assertThat(response.getString("npciErrorCode")).isEqualTo("YG");
    softAssertions.assertThat(response.getString("subResultCode")).isEqualTo("15009");
    softAssertions.assertThat(response.getString("featureTags")).isEqualTo("05|06|31");
    softAssertions.assertAll();
  }

  @Feature("PAPR-6100,PGP-56811")
  @Test(description = "Verify mid is optional param")
  public void verifyMidIsOptional() {
    String epochSeconds = String.valueOf(Instant.now().getEpochSecond());
    Map<String, String> tokenMap = new HashMap<>();
    tokenMap.put("vpa", offlineMid.getVpa());
    String jwt = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts,
        LocalConfig.UPI_PSP_PAYMENT_STATUS_JWT_KEY);

    UpiPspValidateVpa upiPspValidateVpa = new UpiPspValidateVpa("OCIL");
    // Not sending mid parameter
    upiPspValidateVpa.buildRequest(offlineMid.getVpa(), null, epochSeconds, jwt);
    JsonPath response = upiPspValidateVpa.execute().jsonPath();

    SoftAssertions softAssertions = new SoftAssertions();
    // Verify successful response
    softAssertions.assertThat(response.getString("resultCodeId")).isEqualTo("001");
    softAssertions.assertThat(response.getString("resultCode")).isEqualTo("SUCCESS");
    softAssertions.assertThat(response.getString("resultMsg")).isEqualTo("Success");

    // Verify other mandatory fields are present
    softAssertions.assertThat(response.getString("npciErrorCode")).isNotNull();
    softAssertions.assertThat(response.getString("brandName")).isNotNull();
    softAssertions.assertThat(response.getString("franchise")).isNotEmpty();
    softAssertions.assertThat(response.getString("genre")).isNotNull();
    softAssertions.assertThat(response.getString("ifsc")).isNotNull();
    softAssertions.assertThat(response.getString("legalName")).isNotNull();
    softAssertions.assertThat(response.getString("merchantName")).isNotNull();
    softAssertions.assertThat(response.getString("merchantType")).isNotNull();

    // Verify mid can be null/empty
    softAssertions.assertThat(response.getString("mid")).isNotNull();

    softAssertions.assertAll();
  }

  @Feature("PAPR-6100,PGP-56811")
  @Owner(Constants.Owner.ABHISHEK_VERMA)
  @Test(description = "Verify successful validate vpa request")
  public void successOfflineVpa()
  {
    String epochSeconds = String.valueOf(Instant.now().getEpochSecond());
    Map<String, String> tokenMap = new HashMap<>();
    tokenMap.put("vpa", offlineMid.getVpa());
    String jwt = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts,
        LocalConfig.UPI_PSP_PAYMENT_STATUS_JWT_KEY);
    UpiPspValidateVpa upiPspValidateVpa= new UpiPspValidateVpa("OCIL");
    upiPspValidateVpa.buildRequest(offlineMid.getVpa(), null,epochSeconds,jwt);
    JsonPath upiPspValidateVpaResponse= upiPspValidateVpa.execute().jsonPath();

    SoftAssertions softAssertions = new SoftAssertions();
    softAssertions.assertThat(upiPspValidateVpaResponse.getString("resultCodeId")).isEqualTo("001");
    softAssertions.assertThat(upiPspValidateVpaResponse.getString("resultCode")).isEqualTo("SUCCESS");
    softAssertions.assertThat(upiPspValidateVpaResponse.getString("resultMsg")).isEqualTo("Success");
    softAssertions.assertThat(upiPspValidateVpaResponse.getString("npciErrorCode")).isNotNull();
    softAssertions.assertThat(upiPspValidateVpaResponse.getString("brandName")).isNotNull();
    softAssertions.assertThat(upiPspValidateVpaResponse.getString("franchise")).isNotEmpty();
    softAssertions.assertThat(upiPspValidateVpaResponse.getString("genre")).isEqualTo("OFFLINE");
    softAssertions.assertThat(upiPspValidateVpaResponse.getString("ifsc")).isNotNull();
    softAssertions.assertThat(upiPspValidateVpaResponse.getString("legalName")).isNotNull();
    softAssertions.assertThat(upiPspValidateVpaResponse.getString("mcc").toCharArray().length).isEqualTo(4);
    softAssertions.assertThat(upiPspValidateVpaResponse.getString("merchantName")).isNotNull();
    softAssertions.assertThat(upiPspValidateVpaResponse.getString("merchantType")).isEqualTo("SMALL");
    softAssertions.assertThat(upiPspValidateVpaResponse.getString("mid")).isNotNull();
    softAssertions.assertThat(upiPspValidateVpaResponse.getString("onboardingType")).isNotNull();
    softAssertions.assertThat(upiPspValidateVpaResponse.getString("settlementType")).isNotNull();
    softAssertions.assertThat(upiPspValidateVpaResponse.getString("verifiedMerchant")).isNotNull();
    softAssertions.assertThat(upiPspValidateVpaResponse.getString("sid")).isNotNull();
    softAssertions.assertThat(upiPspValidateVpaResponse.getString("tid")).isNotNull();
    softAssertions.assertThat(upiPspValidateVpaResponse.getString("featureTags")).isNotNull();
    softAssertions.assertAll();
  }

  // Merchant Type Tests
  @Feature("PAPR-6100,PGP-56811")
  @Test(description = "Verify merchant type and ownership type")
  public void verifyMerchantAndOwnershipType() {
    String epochSeconds = String.valueOf(Instant.now().getEpochSecond());
    Map<String, String> tokenMap = new HashMap<>();
    tokenMap.put("vpa", payeeVpaOnline);
    String jwt = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts,
        LocalConfig.UPI_PSP_PAYMENT_STATUS_JWT_KEY);

    UpiPspValidateVpa upiPspValidateVpa = new UpiPspValidateVpa("OCIL");
    upiPspValidateVpa.buildRequest(payeeVpaOnline, null, epochSeconds, jwt);
    JsonPath response = upiPspValidateVpa.execute().jsonPath();

    SoftAssertions softAssertions = new SoftAssertions();
    softAssertions.assertThat(response.getString("merchantType")).isIn("SMALL", "LARGE");
    softAssertions.assertThat(response.getString("ownershipType"))
        .isIn("PROPRIETARY", "PARTNERSHIP", "PRIVATE", "PUBLIC", "OTHERS");
    softAssertions.assertThat(response.getBoolean("verifiedMerchant")).isIn(true, false);
    softAssertions.assertThat(response.getString("genre")).isIn("ONLINE", "OFFLINE");
    softAssertions.assertAll();
  }

  @Feature("PAPR-6100,PGP-56811")
  @Owner(Constants.Owner.ABHISHEK_VERMA)
  @Test(description = "Verify response when multiple mids are mapped against single VPA")
  public void multipleMidAgainstVpa()
  {
    String epochSeconds = String.valueOf(Instant.now().getEpochSecond());
    Map<String, String> tokenMap = new HashMap<>();
    tokenMap.put("vpa", multipleMidVpa);
    String jwt = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts,
        LocalConfig.UPI_PSP_PAYMENT_STATUS_JWT_KEY);
    UpiPspValidateVpa upiPspValidateVpa= new UpiPspValidateVpa("OCIL");
    upiPspValidateVpa.buildRequest(multipleMidVpa,null,epochSeconds,jwt);
    JsonPath upiPspValidateVpaResponse= upiPspValidateVpa.execute().jsonPath();

    SoftAssertions softAssertions = new SoftAssertions();
    softAssertions.assertThat(upiPspValidateVpaResponse.getString("resultCodeId")).isEqualTo("009");
    softAssertions.assertThat(upiPspValidateVpaResponse.getString("resultCode")).isEqualTo("FAIL");
    softAssertions.assertThat(upiPspValidateVpaResponse.getString("resultMsg")).isEqualTo("Multiple merchantId linked to one VPA");
    softAssertions.assertThat(upiPspValidateVpaResponse.getString("npciErrorCode")).isEqualTo("YG");
    softAssertions.assertThat(upiPspValidateVpaResponse.getString("subResultCode")).isEqualTo("15003");
    softAssertions.assertAll();
  }

  // Feature Tags Tests
  @Feature("PAPR-6100,PGP-56811")
  @Test(description = "Verify feature tags for small merchant with disabled UPI CC")
  public void verifyFeatureTagsSmallMerchantDisabledUpiCC() {
    String epochSeconds = String.valueOf(Instant.now().getEpochSecond());
    Map<String, String> tokenMap = new HashMap<>();
    tokenMap.put("vpa", ccDisabledSmallMid.getVpa());
    String jwt = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts,
        LocalConfig.UPI_PSP_PAYMENT_STATUS_JWT_KEY);

    UpiPspValidateVpa upiPspValidateVpa = new UpiPspValidateVpa("OCIL");
    upiPspValidateVpa.buildRequest(ccDisabledSmallMid.getVpa(), null, epochSeconds, jwt);
    JsonPath response = upiPspValidateVpa.execute().jsonPath();

    SoftAssertions softAssertions = new SoftAssertions();
    softAssertions.assertThat(response.getString("featureTags")).isEqualTo("05|11");
    softAssertions.assertThat(response.getString("merchantType")).isEqualTo("SMALL");
    softAssertions.assertAll();
  }

  @Feature("PAPR-6100,PGP-56811")
  @Test(description = "Verify feature tag 05 for Credit Card Ineligible")
  public void verifyCreditCardIneligibleFeatureTag() {
    String epochSeconds = String.valueOf(Instant.now().getEpochSecond());
    Map<String, String> tokenMap = new HashMap<>();
    tokenMap.put("vpa", ccDisabledBigMid.getVpa());  // Using a VPA that's ineligible for credit card
    String jwt = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts,
        LocalConfig.UPI_PSP_PAYMENT_STATUS_JWT_KEY);

    UpiPspValidateVpa upiPspValidateVpa = new UpiPspValidateVpa("OCIL");
    upiPspValidateVpa.buildRequest(ccDisabledBigMid.getVpa(), null, epochSeconds, jwt);
    JsonPath response = upiPspValidateVpa.execute().jsonPath();

    SoftAssertions softAssertions = new SoftAssertions();
    // Verify successful response
    softAssertions.assertThat(response.getString("resultCodeId")).isEqualTo("001");
    softAssertions.assertThat(response.getString("resultCode")).isEqualTo("SUCCESS");
    softAssertions.assertThat(response.getString("resultMsg")).isEqualTo("Success");

    // Verify feature tags contain 05 for Credit Card ineligibility
    String featureTags = response.getString("featureTags");
    softAssertions.assertThat(featureTags).contains("05");

    // If feature tags contain multiple values, verify they are pipe separated
    if (featureTags.contains("|")) {
      String[] tags = featureTags.split("\\|");
      softAssertions.assertThat(Arrays.asList(tags)).contains("05");
    }

    softAssertions.assertAll();
  }

  @Feature("PAPR-6100,PGP-56811")
  @Test(description = "Verify feature tag 06 for PPI WALLET Ineligible")
  public void verifyPpiWalletIneligibleFeatureTag() {
    String epochSeconds = String.valueOf(Instant.now().getEpochSecond());
    Map<String, String> tokenMap = new HashMap<>();
    tokenMap.put("vpa", ppiDisabledBigMid.getVpa());  // Using a VPA that's ineligible for PPI WALLET
    String jwt = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts,
        LocalConfig.UPI_PSP_PAYMENT_STATUS_JWT_KEY);

    UpiPspValidateVpa upiPspValidateVpa = new UpiPspValidateVpa("OCIL");
    upiPspValidateVpa.buildRequest(ppiDisabledBigMid.getVpa(), null, epochSeconds, jwt);
    JsonPath response = upiPspValidateVpa.execute().jsonPath();

    SoftAssertions softAssertions = new SoftAssertions();
    // Verify successful response
    softAssertions.assertThat(response.getString("resultCodeId")).isEqualTo("001");
    softAssertions.assertThat(response.getString("resultCode")).isEqualTo("SUCCESS");
    softAssertions.assertThat(response.getString("resultMsg")).isEqualTo("Success");


    // Verify feature tags contain 06 for PPI WALLET ineligibility
    String featureTags = response.getString("featureTags");
    softAssertions.assertThat(featureTags).contains("06");

    // If feature tags contain multiple values, verify they are pipe separated
    if (featureTags.contains("|")) {
      String[] tags = featureTags.split("\\|");
      softAssertions.assertThat(Arrays.asList(tags)).contains("06");
    }
    softAssertions.assertAll();
  }

  @Feature("PAPR-6100,PGP-56811")
  @Test(description = "Verify feature tag 05|11 for UPI CC for small merchant")
  public void verifyUpiCcSmallMerchantFeatureTag() {
    String epochSeconds = String.valueOf(Instant.now().getEpochSecond());
    Map<String, String> tokenMap = new HashMap<>();
    System.out.println("Vpa is :"+ccDisabledSmallMid.getVpa());
    tokenMap.put("vpa", ccDisabledSmallMid.getVpa());  // Using a VPA for small merchant
    String jwt = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts,
        LocalConfig.UPI_PSP_PAYMENT_STATUS_JWT_KEY);

    UpiPspValidateVpa upiPspValidateVpa = new UpiPspValidateVpa("OCIL");
    upiPspValidateVpa.buildRequest(ccDisabledSmallMid.getVpa(), null, epochSeconds, jwt);
    JsonPath response = upiPspValidateVpa.execute().jsonPath();

    SoftAssertions softAssertions = new SoftAssertions();
    // Verify successful response
    softAssertions.assertThat(response.getString("resultCodeId")).isEqualTo("001");
    softAssertions.assertThat(response.getString("resultCode")).isEqualTo("SUCCESS");
    softAssertions.assertThat(response.getString("resultMsg")).isEqualTo("Success");
    softAssertions.assertThat(response.getString("mid")).isEqualTo(ccDisabledSmallMid.getId());
    // Verify merchant type is SMALL
    softAssertions.assertThat(response.getString("merchantType")).isEqualTo("SMALL");

    // Verify feature tags contain 11 for UPI CC small merchant
    String featureTags = response.getString("featureTags");
    softAssertions.assertThat(featureTags).contains("05|11");

    // Verify that 11 only appears in conjunction with 05 and never as standalone
    String[] tags = featureTags.split("\\|");
    boolean has05 = Arrays.asList(tags).contains("05");
    boolean has11 = Arrays.asList(tags).contains("11");
    softAssertions.assertThat(has05).isEqualTo(has11).as("Feature tag 11 must always appear with 05");
    if (has05) {
      softAssertions.assertThat(featureTags).contains("05|11").as("When 11 is present, it must be in conjunction with 05");
    }
    softAssertions.assertAll();
  }

  @Feature("PAPR-6100,PGP-56811")
  @Test(description = "Verify feature tag 06|12 for UPI PPI Wallet for small merchant")
  public void verifyUpiPpiWalletSmallMerchantFeatureTag() {
    String epochSeconds = String.valueOf(Instant.now().getEpochSecond());
    Map<String, String> tokenMap = new HashMap<>();
    tokenMap.put("vpa", ppiDisabledSmallMid.getVpa());  // Using a VPA for small merchant with PPI Wallet
    String jwt = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts,
        LocalConfig.UPI_PSP_PAYMENT_STATUS_JWT_KEY);

    UpiPspValidateVpa upiPspValidateVpa = new UpiPspValidateVpa("OCIL");
    upiPspValidateVpa.buildRequest(ppiDisabledSmallMid.getVpa(), null, epochSeconds, jwt);
    JsonPath response = upiPspValidateVpa.execute().jsonPath();

    SoftAssertions softAssertions = new SoftAssertions();
    // Verify successful response
    softAssertions.assertThat(response.getString("resultCodeId")).isEqualTo("001");
    softAssertions.assertThat(response.getString("resultCode")).isEqualTo("SUCCESS");
    softAssertions.assertThat(response.getString("resultMsg")).isEqualTo("Success");

    // Verify merchant type is SMALL
    softAssertions.assertThat(response.getString("merchantType")).isEqualTo("SMALL");

    // Verify feature tags contain 06|12 for UPI PPI Wallet small merchant
    String featureTags = response.getString("featureTags");
    softAssertions.assertThat(featureTags).contains("06|12");

    // Verify that 13 only appears in conjunction with 31 and never as standalone
    String[] tags = featureTags.split("\\|");
    boolean has06 = Arrays.asList(tags).contains("06");
    boolean has12 = Arrays.asList(tags).contains("12");
    softAssertions.assertThat(has06).isEqualTo(has12).as("Feature tag 12 must always appear with 06");
    if (has06) {
      softAssertions.assertThat(featureTags).contains("06|12").as("When 12 is present, it must be in conjunction with 06");
    }
    softAssertions.assertAll();
  }

  @Feature("PAPR-6100,PGP-56811")
  @Test(description = "Verify feature tags 13|31 for UPI CREDITLINE for small merchant")
  public void verifyUpiCreditlineSmallMerchantFeatureTag() {
    String epochSeconds = String.valueOf(Instant.now().getEpochSecond());
    Map<String, String> tokenMap = new HashMap<>();
    tokenMap.put("vpa", creditlineDisabledSmallMid.getVpa());  // Using a VPA for small merchant with CREDITLINE
    String jwt = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts,
        LocalConfig.UPI_PSP_PAYMENT_STATUS_JWT_KEY);

    UpiPspValidateVpa upiPspValidateVpa = new UpiPspValidateVpa("OCIL");
    upiPspValidateVpa.buildRequest(creditlineDisabledSmallMid.getVpa(), null, epochSeconds, jwt);
    JsonPath response = upiPspValidateVpa.execute().jsonPath();

    SoftAssertions softAssertions = new SoftAssertions();
    // Verify successful response
    softAssertions.assertThat(response.getString("resultCodeId")).isEqualTo("001");
    softAssertions.assertThat(response.getString("resultCode")).isEqualTo("SUCCESS");
    softAssertions.assertThat(response.getString("resultMsg")).isEqualTo("Success");

    // Verify merchant type is SMALL
    softAssertions.assertThat(response.getString("merchantType")).isEqualTo("SMALL");

    // Verify feature tags contain 13|31 for UPI CREDITLINE small merchant
    String featureTags = response.getString("featureTags");
    softAssertions.assertThat(featureTags).contains("13|31");
    
    // Verify that 13 only appears in conjunction with 31 and never as standalone
    String[] tags = featureTags.split("\\|");
    boolean has13 = Arrays.asList(tags).contains("13");
    boolean has31 = Arrays.asList(tags).contains("31");
    softAssertions.assertThat(has13).isEqualTo(has31).as("Feature tag 13 must always appear with 31");
    if (has13) {
        softAssertions.assertThat(featureTags).contains("13|31").as("When 13 is present, it must be in conjunction with 31");
    }
    softAssertions.assertAll();
  }

  @Feature("PAPR-6100,PGP-56811")
  @Test(description = "Verify feature tags 31 for UPI CREDITLINE for Big merchant")
  public void verifyUpiCreditlineBigMerchantFeatureTag() {
    String epochSeconds = String.valueOf(Instant.now().getEpochSecond());
    Map<String, String> tokenMap = new HashMap<>();
    tokenMap.put("vpa", creditlineDisabledBigMid.getVpa());  // Using a VPA for small merchant with CREDITLINE
    String jwt = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts,
        LocalConfig.UPI_PSP_PAYMENT_STATUS_JWT_KEY);

    UpiPspValidateVpa upiPspValidateVpa = new UpiPspValidateVpa("OCIL");
    upiPspValidateVpa.buildRequest(creditlineDisabledBigMid.getVpa(), null, epochSeconds, jwt);
    JsonPath response = upiPspValidateVpa.execute().jsonPath();

    SoftAssertions softAssertions = new SoftAssertions();
    // Verify successful response
    softAssertions.assertThat(response.getString("resultCodeId")).isEqualTo("001");
    softAssertions.assertThat(response.getString("resultCode")).isEqualTo("SUCCESS");
    softAssertions.assertThat(response.getString("resultMsg")).isEqualTo("Success");

    // Verify merchant type is BIG
    softAssertions.assertThat(response.getString("merchantType")).isEqualTo("LARGE");

    // Verify feature tags contain 31 for UPI CREDITLINE big merchant
    String featureTags = response.getString("featureTags");
    softAssertions.assertThat(featureTags).contains("31");
    softAssertions.assertAll();
  }

  @Feature("PAPR-6100,PGP-56811")
  @Test(description = "Verify no feature tags are sent when merchant is eligible for all payment modes")
  public void verifyNoFeatureTagsForAllPayModesEnabledMerchant() {
    String epochSeconds = String.valueOf(Instant.now().getEpochSecond());
    Map<String, String> tokenMap = new HashMap<>();
    tokenMap.put("vpa", offlineMid.getVpa());  // Using OFFLINE_MID_VALIDATE_VPA which has all payment modes enabled
    String jwt = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts,
        LocalConfig.UPI_PSP_PAYMENT_STATUS_JWT_KEY);

    UpiPspValidateVpa upiPspValidateVpa = new UpiPspValidateVpa("OCIL");
    upiPspValidateVpa.buildRequest(offlineMid.getVpa(), null, epochSeconds, jwt);
    JsonPath response = upiPspValidateVpa.execute().jsonPath();

    SoftAssertions softAssertions = new SoftAssertions();
    // Verify successful response
    softAssertions.assertThat(response.getString("resultCodeId")).isEqualTo("001");
    softAssertions.assertThat(response.getString("resultCode")).isEqualTo("SUCCESS");
    softAssertions.assertThat(response.getString("resultMsg")).isEqualTo("Success");

    // Verify feature tags is empty or null when all payment modes are enabled
    String featureTags = response.getString("featureTags");
    softAssertions.assertThat(featureTags)
        .as("Feature tags should be empty or null when merchant is eligible for all payment modes")
        .isIn("", null);

    softAssertions.assertAll();
  }

  @Feature("PAPR-6100,PGP-56811")
  @Test(description = "Verify feature tags format when merchant is ineligible for multiple payment modes")
  public void verifyFeatureTagsFormatForMultiplePaymodes() {
    String epochSeconds = String.valueOf(Instant.now().getEpochSecond());
    Map<String, String> tokenMap = new HashMap<>();
    // Using a merchant that is ineligible for multiple payment modes
    tokenMap.put("vpa", allSubTypeDisabledMerchant.getVpa());
    String jwt = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts,
        LocalConfig.UPI_PSP_PAYMENT_STATUS_JWT_KEY);

    UpiPspValidateVpa upiPspValidateVpa = new UpiPspValidateVpa("OCIL");
    upiPspValidateVpa.buildRequest(allSubTypeDisabledMerchant.getVpa(), null, epochSeconds, jwt);
    JsonPath response = upiPspValidateVpa.execute().jsonPath();

    SoftAssertions softAssertions = new SoftAssertions();

    String featureTags = response.getString("featureTags");
    
    // Verify feature tags are not empty
    softAssertions.assertThat(featureTags).isNotEmpty();
    
    // Verify multiple values are pipe separated
    softAssertions.assertThat(featureTags).contains("05|06|11|12|13|31");
    
    // Split and verify each tag
    String[] tags = featureTags.split("\\|");
    softAssertions.assertThat(tags.length).isGreaterThan(1).as("Multiple feature tags should be present");
    
    // Convert tags to integers for comparing order
    int[] tagNumbers = Arrays.stream(tags)
        .mapToInt(Integer::parseInt)
        .toArray();
    
    // Verify tags are in sequential order
    for (int i = 0; i < tagNumbers.length - 1; i++) {
        softAssertions.assertThat(tagNumbers[i])
            .as("Feature tag at position " + i + " should be less than next tag")
            .isLessThan(tagNumbers[i + 1]);
    }
    
    // Additional verification that tags follow the pattern like 05|06|10|11|12
    // First verify all tags are 2 digits
    for (String tag : tags) {
        softAssertions.assertThat(tag.length())
            .as("Each feature tag should be 2 digits")
            .isEqualTo(2);
    }
    
    // Verify the format matches expected pattern (05|06|10|11|12 etc)
    String joinedTags = String.join("|", tags);
    softAssertions.assertThat(joinedTags).matches("\\d{2}(\\|\\d{2})*")
        .as("Feature tags should be in format like 05|06|10|11|12");

    softAssertions.assertAll();
  }

  @Feature("PAPR-6100,PGP-56811")
  @Test(description = "Verify response values when merchant is blocked")
  public void verifyResponseForBlockedMerchant() {
    String epochSeconds = String.valueOf(Instant.now().getEpochSecond());
    Map<String, String> tokenMap = new HashMap<>();
    tokenMap.put("vpa", blockedMerchant);
    String jwt = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts,
        LocalConfig.UPI_PSP_PAYMENT_STATUS_JWT_KEY);

    UpiPspValidateVpa upiPspValidateVpa = new UpiPspValidateVpa("OCIL");
    upiPspValidateVpa.buildRequest(blockedMerchant, null, epochSeconds, jwt);
    JsonPath response = upiPspValidateVpa.execute().jsonPath();

    SoftAssertions softAssertions = new SoftAssertions();
    
    // Verify response values for blocked merchant
    softAssertions.assertThat(response.getString("resultCode")).isEqualTo("FAIL");
    softAssertions.assertThat(response.getString("resultCodeId")).isEqualTo("009");
    softAssertions.assertThat(response.getString("npciErrorCode")).isEqualTo("ZE");
    softAssertions.assertThat(response.getString("subResultCode")).isEqualTo("15001");
    softAssertions.assertThat(response.getString("resultMsg")).isEqualTo("Merchant is blocked for all payments");
    softAssertions.assertThat(response.getString("featureTags")).isEqualTo("05|06|31");

    softAssertions.assertAll();
  }

  //Todo: Need to fix merchant config
  @Feature("PAPR-6100,PGP-56811")
  @Test(description = "Verify response values when VPA is inactive")
  public void verifyResponseForInactiveVpa() {
    String epochSeconds = String.valueOf(Instant.now().getEpochSecond());
    Map<String, String> tokenMap = new HashMap<>();
    tokenMap.put("vpa", inactiveVpa);
    String jwt = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts,
        LocalConfig.UPI_PSP_PAYMENT_STATUS_JWT_KEY);

    UpiPspValidateVpa upiPspValidateVpa = new UpiPspValidateVpa("OCIL");
    upiPspValidateVpa.buildRequest(inactiveVpa, null, epochSeconds, jwt);
    JsonPath response = upiPspValidateVpa.execute().jsonPath();

    SoftAssertions softAssertions = new SoftAssertions();
    
    // Verify response values for inactive VPA
    softAssertions.assertThat(response.getString("resultCode")).isEqualTo("FAIL");
    softAssertions.assertThat(response.getString("resultCodeId")).isEqualTo("009");
    softAssertions.assertThat(response.getString("npciErrorCode")).isEqualTo("ZE");
    softAssertions.assertThat(response.getString("subResultCode")).isEqualTo("15002");
    softAssertions.assertThat(response.getString("resultMsg")).isEqualTo("VPA is inactive");
    softAssertions.assertThat(response.getString("featureTags")).isEqualTo("05|06|31");

    softAssertions.assertAll();
  }

  @Feature("PAPR-6100,PGP-56811,PPSL-953")
  @Test(description = "Verify response values when Acquiring is inactive")
  public void verifyResponseForInactiveAcquiringWithFlagEnabled() {
    String epochSeconds = String.valueOf(Instant.now().getEpochSecond());
    Map<String, String> tokenMap = new HashMap<>();
    tokenMap.put("vpa", inactiveAcquiringVpa);
    String jwt = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts,
        LocalConfig.UPI_PSP_PAYMENT_STATUS_JWT_KEY);

    UpiPspValidateVpa upiPspValidateVpa = new UpiPspValidateVpa("OCIL");
    upiPspValidateVpa.buildRequest(inactiveAcquiringVpa, null, epochSeconds, jwt);
    JsonPath response = upiPspValidateVpa.execute().jsonPath();

    SoftAssertions softAssertions = new SoftAssertions();
    
    // Verify response values for inactive acquiring
    softAssertions.assertThat(response.getString("resultCode")).isEqualTo("FAIL");
    softAssertions.assertThat(response.getString("resultCodeId")).isEqualTo("009");
    softAssertions.assertThat(response.getString("npciErrorCode")).isEqualTo("ZE");
    softAssertions.assertThat(response.getString("subResultCode")).isEqualTo("15004");
    softAssertions.assertThat(response.getString("resultMsg")).isEqualTo("Acquiring is inactive");
    softAssertions.assertThat(response.getString("featureTags")).isEqualTo("05|06|31");
    softAssertions.assertAll();
  }

  @Feature("PAPR-6100,PGP-56811")
  @Test(description = "Verify response values when VPA does not exist")
  public void verifyResponseForNonExistentVpa() {
    String epochSeconds = String.valueOf(Instant.now().getEpochSecond());
    Map<String, String> tokenMap = new HashMap<>();
    tokenMap.put("vpa", nonExistentVpa);
    String jwt = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts,
        LocalConfig.UPI_PSP_PAYMENT_STATUS_JWT_KEY);

    UpiPspValidateVpa upiPspValidateVpa = new UpiPspValidateVpa("OCIL");
    upiPspValidateVpa.buildRequest(nonExistentVpa, null, epochSeconds, jwt);
    JsonPath response = upiPspValidateVpa.execute().jsonPath();

    SoftAssertions softAssertions = new SoftAssertions();
    
    // Verify response values for non-existent VPA
    softAssertions.assertThat(response.getString("resultCode")).isEqualTo("FAIL");
    softAssertions.assertThat(response.getString("resultCodeId")).isEqualTo("009");
    softAssertions.assertThat(response.getString("npciErrorCode")).isEqualTo("ZH");
    softAssertions.assertThat(response.getString("subResultCode")).isEqualTo("15006");
    softAssertions.assertThat(response.getString("resultMsg")).isEqualTo("VPA does not exist");
    softAssertions.assertThat(response.getString("featureTags")).isEqualTo("05|06|31");

    softAssertions.assertAll();
  }

  @Feature("PPSL-953")
  @Test(description = "Verify response values when Acquiring is inactive")
  public void verifyResponseForInactiveAcquiringWithFlagDisabled() {
    String epochSeconds = String.valueOf(Instant.now().getEpochSecond());
    Map<String, String> tokenMap = new HashMap<>();
    tokenMap.put("vpa", inactiveAquiringWithFlagOff);
    String jwt = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts,
        LocalConfig.UPI_PSP_PAYMENT_STATUS_JWT_KEY);

    UpiPspValidateVpa upiPspValidateVpa = new UpiPspValidateVpa("OCIL");
    upiPspValidateVpa.buildRequest(inactiveAquiringWithFlagOff, null, epochSeconds, jwt);
    JsonPath response = upiPspValidateVpa.execute().jsonPath();

    SoftAssertions softAssertions = new SoftAssertions();

    // Verify response values for inactive acquiring
    softAssertions.assertThat(response.getString("resultCode")).isEqualTo("SUCCESS");
    softAssertions.assertThat(response.getString("resultCodeId")).isEqualTo("001");
    softAssertions.assertThat(response.getString("npciErrorCode")).isEqualTo("00");
    softAssertions.assertThat(response.getString("resultMsg")).isEqualTo("Success");
    softAssertions.assertAll();
  }

}
