package scripts.UPI.OnlineUpi;

import static com.paytm.apphelpers.QRHelper.parseDeeplinkInfo;

import com.paytm.api.Instaproxy.UPISecureResponse;
import com.paytm.api.StaticQrUpiPSP;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.appconstants.Constants;
import com.paytm.appconstants.Constants.MerchantType;
import com.paytm.apphelpers.LogsValidationHelper;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.apphelpers.QRHelper;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.upiIntent.staticQR.Response.StaticQrUpiPSPResponse;
import com.paytm.dto.upiIntent.staticQR.StaticQrUpiPSPRequest;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import java.util.Map;
import org.assertj.core.api.SoftAssertions;
import org.testng.annotations.Test;

public class DeeplinkStandardisation {
  Constants.MerchantType merchant = MerchantType.REQAUTH_FLOW_UPI_ACQ_ID_MID;
  Constants.MerchantType merchantWithoutPref=MerchantType.REQAUTH_FLOW_UPI_CONFEE_MID;

  @Feature("PGP-58719")
  @Owner(Constants.Owner.ABHISHEK_VERMA)
  @Test(description = "Verify acquirment id is returned in tr parm in deeplink, and txn is getting successful with acquirment id")
  public void testCase_01() {
    SoftAssertions softAssert = new SoftAssertions();

    InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
        .setTxnValue("500.00")
        .build();
    InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
    String txnToken = initTxnResponse.getBody().getTxnToken();
    String orderId = initTxnDTO.getBody().getOrderId();
    softAssert.assertThat(initTxnResponse.getBody().getResultInfo().getResultStatus())
        .isEqualTo("S");
    softAssert.assertThat(initTxnResponse.getBody().getResultInfo().getResultCode())
        .isEqualTo("0000");
    softAssert.assertAll();

    // 2. Hit fetchPaymentOptions API
    FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken)
        .setGenerateOrderId("false")
        .setDeepLinkRequiedField(true)
        .setWorkFlow("checkout")
        .build();
    FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchant.getId(), orderId,
        fetchPaymentOptionsDTO);
    JsonPath fpoResponse = fetchPaymentOption.execute().jsonPath();
    softAssert.assertThat(fpoResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
    softAssert.assertThat(fpoResponse.getString("body.resultInfo.resultCode")).isEqualTo("0000");
    softAssert.assertAll();

    // 3. Get QR data and decode deeplink
    String base64QrCode = fpoResponse.getString("body.qrDetail.dataUrl");
    String deeplink = QRHelper.getDeeplinkFromQRImage(base64QrCode);
    System.out.println("Decoded deeplink: " + deeplink);


    // Parse deeplink information
    Map<String, String> deeplinkInfo = parseDeeplinkInfo(deeplink);
    String payeeVpa = deeplinkInfo.get("payeeVpa");
    String amount = deeplinkInfo.get("amount");
    String tr = deeplinkInfo.get("tr");
    //validating acquirement id is sent in tr in deeplink
    softAssert.assertThat(tr.length()).isEqualTo(35);
    softAssert.assertAll();

//upi-psp hit with acquirment id in orderId field
    StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder("SEAMLESS_3D_FORM",
        merchant.getId(), tr,
        amount, payeeVpa, "paytmTest@ptys");
    builder.setPayerName("test");
    builder.setPayerPSP("Phonepe");
    builder.build();
    StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder);
    StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
    Response response = staticQrUpiPSP.execute();
    StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.as(StaticQrUpiPSPResponse.class);

    // Verify order/pay response
    softAssert.assertThat(staticQrUpiPSPResponse.getBody().getResultCode())
        .isEqualToIgnoringCase("SUCCESS");
    softAssert.assertAll();
    String externalSerialNo = staticQrUpiPSPResponse.getBody().getExternalSerialNo();

    //verifying query by acquirment id is done on acquirment id instead of orderId
    String logs = null;
    try {
      logs = LogsValidationHelper.verifyLogsOnPod("THEIA_FACADE", tr, "ACQUIRING_INQUIRE_WITH_ACQ_ID","REQUEST");
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
    softAssert.assertThat(logs).contains("tr");
    softAssert.assertAll();

    // 5. Hit callback API using UPISecureResponse with creditLineInfo
    UPISecureResponse secureResponse = new UPISecureResponse().buildRequest(
        "Abhishek Verma",
        "paytmTest@ptys",
        payeeVpa,
        "Punjab National Bank",
        "PUNB",
        externalSerialNo,
        "SUCCESS",
        String.format("%011d", (long) (Math.random() * 100000000000L)),
        amount,
        "0",
        "Transaction is successful",
        "DEFERRED_SETTLEMENT",""
    );

    JsonPath callbackResponse = secureResponse.execute().jsonPath();
    softAssert.assertThat(callbackResponse.getString("body.resultCodeId"))
        .as("Result status mismatch")
        .isEqualToIgnoringCase("001");
    softAssert.assertThat(callbackResponse.getString("body.resultCode"))
        .as("Result code mismatch")
        .isEqualToIgnoringCase("SUCCESS");

    // Verify transaction status
    PGPHelpers.getTxnStatus(merchant.getId(), orderId)
        .validateStatus("TXN_SUCCESS")
        .validatePaymentMode("UPI")
        .validateGatewayName("PTYBLI")
        .validateTxnAmount(initTxnDTO.txnAmountFromBody())
        .validateChargeAmount(String.format("%.2f",
            Double.valueOf(amount) - Double.valueOf(initTxnDTO.txnAmountFromBody())))
        .AssertAll();
    softAssert.assertAll();
  }
  @Feature("PGP-58719")
  @Owner(Constants.Owner.ABHISHEK_VERMA)
  @Test(description = "Verify order id is returned in tr parm in deeplink, and txn is getting successful with order id")
  public void testCase_02() {
    SoftAssertions softAssert = new SoftAssertions();

    InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantWithoutPref)
        .setTxnValue("500.00")
        .build();
    InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
    String txnToken = initTxnResponse.getBody().getTxnToken();
    String orderId = initTxnDTO.getBody().getOrderId();
    softAssert.assertThat(initTxnResponse.getBody().getResultInfo().getResultStatus())
        .isEqualTo("S");
    softAssert.assertThat(initTxnResponse.getBody().getResultInfo().getResultCode())
        .isEqualTo("0000");
    softAssert.assertAll();

    // 2. Hit fetchPaymentOptions API
    FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken)
        .setGenerateOrderId("false")
        .setDeepLinkRequiedField(true)
        .setWorkFlow("checkout")
        .build();
    FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchantWithoutPref.getId(), orderId,
        fetchPaymentOptionsDTO);
    JsonPath fpoResponse = fetchPaymentOption.execute().jsonPath();
    softAssert.assertThat(fpoResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
    softAssert.assertThat(fpoResponse.getString("body.resultInfo.resultCode")).isEqualTo("0000");
    softAssert.assertAll();

    // 3. Get QR data and decode deeplink
    String base64QrCode = fpoResponse.getString("body.qrDetail.dataUrl");
    String deeplink = QRHelper.getDeeplinkFromQRImage(base64QrCode);
    System.out.println("Decoded deeplink: " + deeplink);


    // Parse deeplink information
    Map<String, String> deeplinkInfo = parseDeeplinkInfo(deeplink);
    String payeeVpa = deeplinkInfo.get("payeeVpa");
    String amount = deeplinkInfo.get("amount");
    String tr = deeplinkInfo.get("tr");
    //validating acquirement id is sent in tr in deeplink
    softAssert.assertThat(tr).isEqualTo(orderId);
    softAssert.assertAll();

//upi-psp hit with acquirment id in orderId field
    StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder("SEAMLESS_3D_FORM",
        merchantWithoutPref.getId(), tr,
        amount, payeeVpa, "paytmTest@ptys");
    builder.setPayerName("test");
    builder.setPayerPSP("Phonepe");
    builder.build();
    StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder);
    StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
    Response response = staticQrUpiPSP.execute();
    StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.as(StaticQrUpiPSPResponse.class);

    // Verify order/pay response
    softAssert.assertThat(staticQrUpiPSPResponse.getBody().getResultCode())
        .isEqualToIgnoringCase("SUCCESS");
    softAssert.assertAll();
    String externalSerialNo = staticQrUpiPSPResponse.getBody().getExternalSerialNo();

    //verifying query by acquirment id is done on acquirment id instead of orderId
    String logs = null;
    try {
      logs = LogsValidationHelper.verifyLogsOnPod("THEIA_FACADE", tr, "ACQUIRING_INQUIRE_WITH_MERCHANT_TRANS_ID","REQUEST");
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
    softAssert.assertThat(logs).contains("tr");
    softAssert.assertAll();

    // 5. Hit callback API using UPISecureResponse with creditLineInfo
    UPISecureResponse secureResponse = new UPISecureResponse().buildRequest(
        "Abhishek Verma",
        "paytmTest@ptys",
        payeeVpa,
        "Punjab National Bank",
        "PUNB",
        externalSerialNo,
        "SUCCESS",
        String.format("%011d", (long) (Math.random() * 100000000000L)),
        amount,
        "0",
        "Transaction is successful",
        "DEFERRED_SETTLEMENT",""
    );

    JsonPath callbackResponse = secureResponse.execute().jsonPath();
    softAssert.assertThat(callbackResponse.getString("body.resultCodeId"))
        .as("Result status mismatch")
        .isEqualToIgnoringCase("001");
    softAssert.assertThat(callbackResponse.getString("body.resultCode"))
        .as("Result code mismatch")
        .isEqualToIgnoringCase("SUCCESS");

    // Verify transaction status
    PGPHelpers.getTxnStatus(merchantWithoutPref.getId(), orderId)
        .validateStatus("TXN_SUCCESS")
        .validatePaymentMode("UPI")
        .validateGatewayName("PTYBLI")
        .validateTxnAmount(initTxnDTO.txnAmountFromBody())
        .validateChargeAmount(String.format("%.2f",
            Double.valueOf(amount) - Double.valueOf(initTxnDTO.txnAmountFromBody())))
        .AssertAll();
    softAssert.assertAll();
  }

}
