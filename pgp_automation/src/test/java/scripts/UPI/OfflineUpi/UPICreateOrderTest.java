package scripts.UPI.OfflineUpi;

import com.paytm.LocalConfig;
import com.paytm.api.Instaproxy.UPICreateOrder;
import com.paytm.api.Instaproxy.UPICreateOrder.PayerInstrument;
import com.paytm.api.JustPayCallback;
import com.paytm.api.upipsp.UPIPspPaymentStatus;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.pg.crypto.AesEncryption;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.assertj.core.api.SoftAssertions;
import org.json.JSONObject;
import org.testng.annotations.Test;

public class UPICreateOrderTest extends PGPBaseTest {
  Random random = new Random();
  String payerVPA= "8840500363@ypay";
  String payeeVPA="paytm.ud956915885@pty";


  @Test
  public void successTransaction ()
  {
    String orderId = CommonHelpers.generateOrderId();
    System.out.println("OrdersId is: "+orderId);
    String npciOrderId= "PTMCR"+String.format("%011d%011d", random.nextInt((int)Math.pow(10, 11)), random.nextInt((int)Math.pow(10, 11)));
    String epochSeconds = String.valueOf(Instant.now().getEpochSecond());
    UPICreateOrder upiCreateOrder= new UPICreateOrder().buildRequest(PayerInstrument.SAVINGS,"10.00",payerVPA,payeeVPA,orderId,npciOrderId,"ABC",epochSeconds);

    Response upiCreateOrderResponse = upiCreateOrder.execute();
    String response= upiCreateOrderResponse.asString();
    System.out.println("Encrypted response is+: "+response);
    AesEncryption aesEncryption= new AesEncryption();
    String decryptedResponse;
    try {
      decryptedResponse= aesEncryption.decrypt(response, LocalConfig.INSTA_CREATE_ORDER_KEY);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    System.out.println("decryptedResponse is: "+decryptedResponse);
    JSONObject json = new JSONObject(decryptedResponse);
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(json.get("statusMsg")).isEqualTo("Request Acknowledged");
    softly.assertThat(json.get("status")).isEqualTo("accept");
    softly.assertAll();
    softly.assertAll();

    JustPayCallback justPayCallback= new JustPayCallback().buildRequest(payeeVPA,npciOrderId,"10.00",PayerInstrument.SAVINGS.name(),"AABD0000011","ABC",payerVPA,"MERCHANT_CREDITED_VIA_PAY",PayerInstrument.SAVINGS.name());
    JsonPath callbackResponse= justPayCallback.execute().jsonPath();
    softly.assertThat(callbackResponse.getString("callbackStatus")).isEqualTo("Success");
    softly.assertAll();

    Map<String, String> tokenMap = new HashMap<>();
    tokenMap.put("payeeVpa", payeeVPA);
    tokenMap.put("npciTxnId", npciOrderId);
    String jwt = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts,
        LocalConfig.VALID_VAULT_JWT_KEY);

    UPIPspPaymentStatus upiPspPaymentStatus= new UPIPspPaymentStatus().buildRequest(payeeVPA,npciOrderId,epochSeconds,"JWT",jwt);

    JsonPath upiPaymentStatusResponse = upiPspPaymentStatus.execute().jsonPath();

  }

}
