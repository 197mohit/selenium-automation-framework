package scripts.UPI.OfflineUpi;

import com.paytm.LocalConfig;
import com.paytm.api.Instaproxy.ReqAuthUPICreateOrder;
import com.paytm.api.Instaproxy.ReqAuthUPICreateOrder.PayerInstrument;
import com.paytm.api.Instaproxy.ReqVal;
import com.paytm.api.JustPayCallback;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.pg.crypto.AesEncryption;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.SoftAssertions;
import org.json.JSONObject;
import org.testng.annotations.Test;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class JustPayIntegrationInstaTest extends PGPBaseTest {

  Random random = new Random();
  String payerVPA = "8840500363@ypay";
  //String payeeVPA = "paytm.ud106956918222@pty";
  String payeeVPA ="paytm.ud956915885@pty";
  SoftAssertions softly = new SoftAssertions();


  @Test(description = "Verify successful reqVal request")
  @Owner(Constants.Owner.ABHISHEK_VERMA)
  @Feature("PAPR-5848")
  public void successfulReqVal() {
    ReqVal reqVal = new ReqVal().buildRequest(payeeVPA, "V");
    JsonPath reqValResponse = reqVal.execute().jsonPath();

    softly.assertThat(reqValResponse.getString("status")).isEqualTo("S");
    softly.assertThat(reqValResponse.getString("responseMessage")).isEqualTo("SUCCESS");
    softly.assertThat(reqValResponse.getString("mid")).isEqualTo("qa12id49893344049514");
    softly.assertThat(reqValResponse.getString("responseCode")).isEqualTo("00");
    softly.assertThat(reqValResponse.getString("ifsc")).isEqualTo("YESB0MCHUPI");
    softly.assertAll();
  }

  @Test(description = "Verify ReqValAdd for incorrect VPA")
  @Owner(Constants.Owner.ABHISHEK_VERMA)
  @Feature("PAPR-5848")
  public void reqValForIncorrectVpa()
  {
    ReqVal reqVal = new ReqVal().buildRequest("paytm.us102343476@pty", "V");
    JsonPath reqValResponse = reqVal.execute().jsonPath();

    softly.assertThat(reqValResponse.getString("status")).isEqualTo("F");
    softly.assertThat(reqValResponse.getString("responseMessage")).isEqualTo("FAILED");
    softly.assertThat(reqValResponse.getString("mid")).isEqualTo(null);
    softly.assertThat(reqValResponse.getString("responseCode")).isEqualTo("ZH");
    softly.assertThat(reqValResponse.getString("ifsc")).isEqualTo(null);
    softly.assertAll();
  }

  @Test(description = "Verify ReqValAdd response if type='' in request")
  @Owner(Constants.Owner.ABHISHEK_VERMA)
  @Feature("PAPR-5848")
  public void verifyWhenEmptyTypeValueIsSentInRequest()
  {
    ReqVal reqVal = new ReqVal().buildRequest(payeeVPA, "");
    JsonPath reqValResponse = reqVal.execute().jsonPath();

    softly.assertThat(reqValResponse.getString("status")).isEqualTo("S");
    softly.assertThat(reqValResponse.getString("responseMessage")).isEqualTo("SUCCESS");
    softly.assertThat(reqValResponse.getString("mid")).isEqualTo("qa12id49893344049514");
    softly.assertThat(reqValResponse.getString("responseCode")).isEqualTo("00");
    softly.assertThat(reqValResponse.getString("ifsc")).isEqualTo("YESB0MCHUPI");
    softly.assertAll();
  }


  @Test(description = "Verify ReqValAdd response if type != V in request")
  @Owner(Constants.Owner.ABHISHEK_VERMA)
  @Feature("PAPR-5848")
  public void verifyWhenTypeValueIsNotVInRequest()
  {
    ReqVal reqVal = new ReqVal().buildRequest(payeeVPA, "A");
    JsonPath reqValResponse = reqVal.execute().jsonPath();

    softly.assertThat(reqValResponse.getString("status")).isEqualTo("S");
    softly.assertThat(reqValResponse.getString("responseMessage")).isEqualTo("SUCCESS");
    softly.assertThat(reqValResponse.getString("mid")).isEqualTo("qa12id49893344049514");
    softly.assertThat(reqValResponse.getString("responseCode")).isEqualTo("00");
    softly.assertThat(reqValResponse.getString("ifsc")).isEqualTo("YESB0MCHUPI");
    softly.assertAll();
  }

  @Test(description = "Verify ReqValAdd response if type in null")
  @Owner(Constants.Owner.ABHISHEK_VERMA)
  @Feature("PAPR-5848")
  public void verifyWhenTypeValueIsNullInRequest()
  {
    ReqVal reqVal = new ReqVal().buildRequest(payeeVPA, null);
    JsonPath reqValResponse = reqVal.execute().jsonPath();

    softly.assertThat(reqValResponse.getString("status")).isEqualTo("S");
    softly.assertThat(reqValResponse.getString("responseMessage")).isEqualTo("SUCCESS");
    softly.assertThat(reqValResponse.getString("mid")).isEqualTo("qa12id49893344049514");
    softly.assertThat(reqValResponse.getString("responseCode")).isEqualTo("00");
    softly.assertThat(reqValResponse.getString("ifsc")).isEqualTo("YESB0MCHUPI");
    softly.assertAll();
  }

  @Test(description = "Verify ReqValAdd response if type parameter is not present in request")
  @Owner(Constants.Owner.ABHISHEK_VERMA)
  @Feature("PAPR-5848")
  public void verifyWhenTypeValueIsNotSentInRequest()
  {
    ReqVal reqVal = new ReqVal().buildRequest(payeeVPA, null);
    reqVal.deleteContext("type");
    JsonPath reqValResponse = reqVal.execute().jsonPath();

    softly.assertThat(reqValResponse.getString("status")).isEqualTo("S");
    softly.assertThat(reqValResponse.getString("responseMessage")).isEqualTo("SUCCESS");
    softly.assertThat(reqValResponse.getString("mid")).isEqualTo("qa12id49893344049514");
    softly.assertThat(reqValResponse.getString("responseCode")).isEqualTo("00");
    softly.assertThat(reqValResponse.getString("ifsc")).isEqualTo("YESB0MCHUPI");
    softly.assertAll();
  }

  @Test(description = "Verify successful reqAuth Txn")
  @Owner(Constants.Owner.ABHISHEK_VERMA)
  @Feature("PAPR-5848")
  public void successJustPayReqAuth() {
    String orderId = CommonHelpers.generateOrderId();
//    "9fb3adb3211747ada74606c7ba1fcbe7";
    System.out.println("OrdersId is: " + orderId);
    String npciOrderId = "PTMCR" + String.format("%011d%011d", random.nextInt((int) Math.pow(10, 11)),
        random.nextInt((int) Math.pow(10, 11)));
//    "FAILPTMCR0196047018001857591553";
    String epochSeconds = String.valueOf(Instant.now().toEpochMilli());
    ReqAuthUPICreateOrder upiCreateOrder = new ReqAuthUPICreateOrder().buildRequest(
        PayerInstrument.SAVINGS, "10.00", payerVPA, payeeVPA, orderId, npciOrderId, "ABC",
        epochSeconds);

    Response upiCreateOrderResponse = upiCreateOrder.execute("PTYES");
    String response = upiCreateOrderResponse.asString();
    System.out.println("Encrypted response is+: " + response);
    AesEncryption aesEncryption = new AesEncryption();
    String decryptedResponse;
    try {
      decryptedResponse = aesEncryption.decrypt(response, LocalConfig.INSTA_CREATE_ORDER_KEY);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    System.out.println("decryptedResponse is: " + decryptedResponse);
    JSONObject json = new JSONObject(decryptedResponse);
    softly.assertThat(json.get("statusMsg")).isEqualTo("Request Acknowledged");
    softly.assertThat(json.get("status")).isEqualTo("accept");
    softly.assertAll();

    JustPayCallback justPayCallback = new JustPayCallback().buildRequest(payeeVPA, npciOrderId,
        "10.00", PayerInstrument.SAVINGS.name(), "AABD0000011", "ABC", payerVPA,
        "MERCHANT_CREDITED_VIA_PAY", PayerInstrument.SAVINGS.name());
    JsonPath callbackResponse = justPayCallback.execute().jsonPath();
    softly.assertThat(callbackResponse.getString("callbackStatus")).isEqualTo("Success");
    softly.assertAll();

    Map<String, String> tokenMap = new HashMap<>();
    tokenMap.put("payeeVpa", payeeVPA);
    tokenMap.put("npciTxnId", npciOrderId);
    String jwt = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts,
        LocalConfig.VALID_VAULT_JWT_KEY);

   /* UPIPspPaymentStatus upiPspPaymentStatus = new UPIPspPaymentStatus().buildRequest(payeeVPA,
        npciOrderId, epochSeconds, "JWT", jwt);
    JsonPath upiPaymentStatusResponse = upiPspPaymentStatus.executeUntilExpectedConditionMet(
        "body.resultCode", "SUCCESS", 5, 12).jsonPath();
    softly.assertThat(upiPaymentStatusResponse.getString("body.txnStatus")).isEqualTo("SUCCESS");*/
    softly.assertAll();
  }


  @Test(description = "Verify successful reqAuth Txn")
  @Owner(Constants.Owner.ABHISHEK_VERMA)
  @Feature("PAPR-5848")
  public void reqAuthWhenDailyLimitExceeded() {
    String orderId = CommonHelpers.generateOrderId();
    System.out.println("OrdersId is: " + orderId);
    String npciOrderId =
        "PTMCR" + String.format("%011d%011d", random.nextInt((int) Math.pow(10, 11)),
            random.nextInt((int) Math.pow(10, 11)));
    String epochSeconds = String.valueOf(Instant.now().toEpochMilli());
    ReqAuthUPICreateOrder upiCreateOrder = new ReqAuthUPICreateOrder().buildRequest(
        ReqAuthUPICreateOrder.PayerInstrument.SAVINGS, "80000.00", payerVPA, payeeVPA, orderId, npciOrderId, "ABC",
        epochSeconds);

    Response upiCreateOrderResponse = upiCreateOrder.execute("PTYES");
    String response = upiCreateOrderResponse.asString();
    System.out.println("Encrypted response is+: " + response);
    AesEncryption aesEncryption = new AesEncryption();
    String decryptedResponse;
    try {
      decryptedResponse = aesEncryption.decrypt(response, LocalConfig.INSTA_CREATE_ORDER_KEY);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    System.out.println("decryptedResponse is: " + decryptedResponse);
    JSONObject json = new JSONObject(decryptedResponse);
    softly.assertThat(json.get("statusMsg")).isEqualTo("Request Acknowledged");
    softly.assertThat(json.get("status")).isEqualTo("accept");
    softly.assertAll();
  }
}
