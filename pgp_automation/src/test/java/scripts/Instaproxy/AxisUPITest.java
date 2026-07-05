package scripts.Instaproxy;

import com.paytm.LocalConfig;
import com.paytm.api.Instaproxy.OfflineTxnDataCreate;
import com.paytm.api.Instaproxy.ReqAuthUPICreateOrder;
import com.paytm.api.Instaproxy.ReqAuthUPICreateOrder.PayerInstrument;
import com.paytm.api.upipsp.UPIPspPaymentStatus;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.ui.base.test.BaseTest;
import com.paytm.pg.crypto.AesEncryption;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.assertj.core.api.SoftAssertions;
import org.json.JSONObject;
import org.testng.annotations.Test;

public class AxisUPITest extends BaseTest {
    String payerVPA = "9999725804@ypay";
    String payeeVPA = "paytm.us354lk8@pta";
    String payeeVPA2 = "paytm.us108995sb@pty";
    SoftAssertions softly= new SoftAssertions();
    @Test(description = "Verify successful reqVal request")
    public void test() {
        String orderId = "AXIS" + LocalConfig.ENV_NAME + CommonHelpers.generateOrderId();
        System.out.println("OrdersId is: " + orderId);
        String epochSeconds = String.valueOf(Instant.now().toEpochMilli());
        ReqAuthUPICreateOrder upiCreateOrder = new ReqAuthUPICreateOrder().buildRequestV2(
                ReqAuthUPICreateOrder.PayerInstrument.UPI_LITE.name(), "2000.00", payerVPA, payeeVPA, orderId, orderId, "ABC",
                epochSeconds, "", "SAVINGS", Constants.MerchantType.OCIL_YES.getId(), "DEFERRED_SETTLEMENT");
        


        Response upiCreateOrderResponse = upiCreateOrder.executeV2("PTAB");
        String response = upiCreateOrderResponse.asString();
        System.out.println("Encrypted response is+: " + response);
        AesEncryption aesEncryption = new AesEncryption();
        String decryptedResponse;
        try {
          decryptedResponse = aesEncryption.decrypt(response, LocalConfig.INSTA_CREATE_ORDER_KEY);
         // "{\"status\":\"accept\",\"statusMsg\":\"Request Acknowledged\"}";
          
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
        System.out.println("decryptedResponse is: " + decryptedResponse);
        JSONObject json = new JSONObject(decryptedResponse);
        softly.assertThat(json.get("statusMsg")).isEqualTo("Request Acknowledged");
        softly.assertThat(json.get("status")).isEqualTo("accept");
        softly.assertAll();
        if(json.get("status").equals("accept"))
        {
            String query="insert into PGP_QA_UTILS.axisUpi_txn_data(npciTxnId,orderId,amount,payerVpa,payeeVpa,payerName,paymentInstrument) VALUES ('" + orderId + "','" + orderId + "','" + 2000.00 + "','" + payerVPA + "','" + payeeVPA + "','" + "ABC" + "','" + PayerInstrument.UPI_LITE.name() + "')";
            System.out.println("query is: "+query);
            OfflineTxnDataCreate offlineTxnDataCreate= new OfflineTxnDataCreate();
            offlineTxnDataCreate.buildRequest("PTAB",query);
            io.restassured.path.json.JsonPath offlineTxnDataCreateResponse = offlineTxnDataCreate.execute().jsonPath();
            softly.assertThat(offlineTxnDataCreateResponse.getString("status")).isEqualTo("S");
            softly.assertThat(offlineTxnDataCreateResponse.getString("responseMessage")).isEqualTo("Order Added in table");
            softly.assertAll();
        }
             
    
        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("payeeVpa", payeeVPA);
        tokenMap.put("npciTxnId", orderId);
        System.out.println("npciTxnId for JWT is: "+ tokenMap.get("npciTxnId"));
        String jwt = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts,
            LocalConfig.UPI_PSP_PAYMENT_STATUS_JWT_KEY);
    
        UPIPspPaymentStatus upiPspPaymentStatus = new UPIPspPaymentStatus().buildRequest(payeeVPA,
            orderId, epochSeconds, "JWT", jwt);
        io.restassured.path.json.JsonPath upiPaymentStatusResponse = upiPspPaymentStatus.executeUntilExpectedConditionMet(
            "body.resultCode", "SUCCESS", 5, 12).jsonPath();
        softly.assertThat(upiPaymentStatusResponse.getString("body.txnStatus")).isEqualTo("SUCCESS");
        softly.assertAll();

    }
}
