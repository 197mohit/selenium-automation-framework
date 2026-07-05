package scripts.UPI.OnlineUpi;

import static com.paytm.apphelpers.QRHelper.parseDeeplinkInfo;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants.MerchantType;
import com.paytm.apphelpers.QRHelper;
import com.paytm.apphelpers.RedisHelper;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.dto.NativeDTO.fetchPcfDetail.FetchPcfRequest;
import com.paytm.dto.NativeDTO.fetchPcfDetail.PayMethod;
import com.paytm.api.nativeAPI.FetchPcfDetail;
import com.paytm.appconstants.Constants;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.utils.ff4j.FF4JFlags;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.SoftAssertions;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.api.StaticQrUpiPSP;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.upiIntent.staticQR.StaticQrUpiPSPRequest;
import com.paytm.dto.upiIntent.staticQR.Response.StaticQrUpiPSPResponse;
import io.restassured.response.Response;
import com.paytm.api.Instaproxy.UPISecureResponse;

import java.util.HashMap;
import java.util.Map;

public class ConvenienceFeeOnUPiIntentTest extends PGPBaseTest {

  RedisHelper redisHelper = RedisHelper.getInstance(LocalConfig.TRANSACTIONAL_REDIS_CLUSTER_URI,
      LocalConfig.PG_REDIS_CLUSTER_PASS);
  Constants.MerchantType convFeeAndPlatformFeeMerchant = Constants.MerchantType.CONVENIENCE_AND_PLATFORM_FEE_ON_UPI_SUBTYPE;
  Constants.MerchantType platformFeeMerchant = MerchantType.PLATFORM_FEE_ON_UPI_SUBTYPE;
  Constants.MerchantType convFeeMerchant = MerchantType.CONVENIENCE_FEE_ON_UPI_SUBTYPE;
  ConvenienceFeeUpiIntentUITest convenienceFeeUpiIntentUI= new ConvenienceFeeUpiIntentUITest();



  @Test(description = "Verify all UPI mode subtypes are supported in fetchPCF request")
  public void testCase_01() {
    SoftAssertions softAssert = new SoftAssertions();

    InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, convFeeAndPlatformFeeMerchant)
        .setTxnValue("500.00")
        .build();

    InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
    String txnToken = initTxnResponse.getBody().getTxnToken();

    List<PayMethod> payMethods = new ArrayList<>();
    PayMethod upiPayMethod = new PayMethod()
        .setPayMethod("UPI");
    upiPayMethod.setAdditionalProperty("upiModeSubTypes",
        Arrays.asList("UPI_CREDITLINE", "UPI_CREDIT_CARD", "UPI_PPIWALLET"));
    upiPayMethod.setAdditionalProperty("payOption", "UPI_INTENT");
    payMethods.add(upiPayMethod);

    FetchPcfRequest fetchPcfRequest = new FetchPcfRequest.Builder(txnToken, "TXN_TOKEN")
        .setMid(convFeeAndPlatformFeeMerchant.getId())
        .setTxnAmount("500.00")
        .setPayMethods(payMethods)
        .build();

    // Execute fetchPCF request
    FetchPcfDetail fetchPcfDetail = new FetchPcfDetail(convFeeAndPlatformFeeMerchant.getId(),
        initTxnDTO.orderFromBody(), fetchPcfRequest);
    JsonPath jsonPath = fetchPcfDetail.execute().jsonPath();
    HashMap<String,Double> expFee=convenienceFeeUpiIntentUI.calculateExpectedFees(Double.valueOf(initTxnDTO.txnAmountFromBody()),convFeeAndPlatformFeeMerchant);

    // Verify response
    softAssert.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
    softAssert.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
    softAssert.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

    // Verify UPI consult details
    softAssert.assertThat(jsonPath.getString("body.consultDetails.UPI.payMethod")).isEqualTo("UPI");
    softAssert.assertThat(jsonPath.getString("body.consultDetails.UPI.baseTransactionAmount.value"))
        .isEqualTo(initTxnDTO.txnAmountFromBody());
    softAssert.assertThat(
            jsonPath.getString("body.consultDetails.UPI.totalConvenienceCharges.value"))
        .isEqualTo(String.format("%.2f",expFee.get("expectedPlatformFeeAmt")));
    softAssert.assertThat(jsonPath.getString("body.consultDetails.UPI.totalTransactionAmount.value"))
        .isEqualTo(String.format("%.2f", Double.parseDouble(initTxnDTO.txnAmountFromBody()) + expFee.get("expectedPlatformFeeAmt")));
    softAssert.assertThat(jsonPath.getString("body.consultDetails.UPI.text"))
        .isEqualTo("Convenience fee of Rs. "+String.format("%.2f",expFee.get("expectedPlatformFeeAmt"))+" is applicable.");
    softAssert.assertThat(jsonPath.getString("body.consultDetails.UPI.displayText"))
        .isEqualTo("BHIM UPI");
    softAssert.assertThat(jsonPath.getString("body.consultDetails.UPI.convenienceCharges.value"))
        .isEqualTo("0.00");
    softAssert.assertThat(jsonPath.getString("body.consultDetails.UPI.platformCharges.value"))
        .isEqualTo(String.format("%.2f",expFee.get("expectedPlatformFeeAmt")));

    // Verify payModeSubTypeConsultDetails
    List<String> payModeSubTypes = jsonPath.getList(
        "body.consultDetails.UPI.payModeSubTypeConsultDetails.payModeSubType");
    softAssert.assertThat(payModeSubTypes)
        .contains("UPI_CREDITLINE", "UPI_CREDIT_CARD", "UPI_PPIWALLET");

    // Verify UPI_CREDITLINE details
    softAssert.assertThat(jsonPath.getString(
            "body.consultDetails.UPI.payModeSubTypeConsultDetails[0].payModeSubType"))
        .isEqualTo("UPI_CREDITLINE");
    softAssert.assertThat(jsonPath.getString(
            "body.consultDetails.UPI.payModeSubTypeConsultDetails[0].baseTransactionAmount.value"))
        .isEqualTo(initTxnDTO.txnAmountFromBody());
    softAssert.assertThat(jsonPath.getString(
            "body.consultDetails.UPI.payModeSubTypeConsultDetails[0].totalConvenienceCharges.value"))
        .isEqualTo(String.format("%.2f",(expFee.get("expectedConvFeeAmtUpiCL")+expFee.get("expectedPlatformFeeAmt"))));
    softAssert.assertThat(jsonPath.getString(
            "body.consultDetails.UPI.payModeSubTypeConsultDetails[0].totalTransactionAmount.value"))
        .isEqualTo(String.format("%.2f", Double.parseDouble(initTxnDTO.txnAmountFromBody()) + expFee.get("expectedConvFeeAmtUpiCL")+expFee.get("expectedPlatformFeeAmt")));
    softAssert.assertThat(jsonPath.getString(
            "body.consultDetails.UPI.payModeSubTypeConsultDetails[0].convenienceCharges.value"))
        .isEqualTo(String.format("%.2f",(expFee.get("expectedConvFeeAmtUpiCL"))));
    softAssert.assertThat(jsonPath.getString(
            "body.consultDetails.UPI.payModeSubTypeConsultDetails[0].platformCharges.value"))
        .isEqualTo(String.format("%.2f",expFee.get("expectedPlatformFeeAmt")));

    // Verify UPI_CREDIT_CARD details
    softAssert.assertThat(jsonPath.getString(
            "body.consultDetails.UPI.payModeSubTypeConsultDetails[1].payModeSubType"))
        .isEqualTo("UPI_CREDIT_CARD");
    softAssert.assertThat(jsonPath.getString(
            "body.consultDetails.UPI.payModeSubTypeConsultDetails[1].baseTransactionAmount.value"))
        .isEqualTo(initTxnDTO.txnAmountFromBody());
    softAssert.assertThat(jsonPath.getString(
            "body.consultDetails.UPI.payModeSubTypeConsultDetails[1].totalConvenienceCharges.value"))
        .isEqualTo(String.format("%.2f",(expFee.get("expectedConvFeeAmtUpiCL")+expFee.get("expectedPlatformFeeAmt"))));
    softAssert.assertThat(jsonPath.getString(
            "body.consultDetails.UPI.payModeSubTypeConsultDetails[1].totalTransactionAmount.value"))
        .isEqualTo(String.format("%.2f", Double.parseDouble(initTxnDTO.txnAmountFromBody()) + expFee.get("expectedConvFeeAmtUpiCL")+expFee.get("expectedPlatformFeeAmt")));
    softAssert.assertThat(jsonPath.getString(
            "body.consultDetails.UPI.payModeSubTypeConsultDetails[1].convenienceCharges.value"))
        .isEqualTo(String.format("%.2f",expFee.get("expectedConvFeeAmtUpiCL")));
    softAssert.assertThat(jsonPath.getString(
            "body.consultDetails.UPI.payModeSubTypeConsultDetails[1].platformCharges.value"))
        .isEqualTo(String.format("%.2f",expFee.get("expectedPlatformFeeAmt")));


    // Verify UPI_PPIWALLET details
    softAssert.assertThat(jsonPath.getString(
            "body.consultDetails.UPI.payModeSubTypeConsultDetails[2].payModeSubType"))
        .isEqualTo("UPI_PPIWALLET");
    softAssert.assertThat(jsonPath.getString(
            "body.consultDetails.UPI.payModeSubTypeConsultDetails[2].baseTransactionAmount.value"))
        .isEqualTo(initTxnDTO.txnAmountFromBody());
    softAssert.assertThat(jsonPath.getString(
            "body.consultDetails.UPI.payModeSubTypeConsultDetails[2].totalConvenienceCharges.value"))
        .isEqualTo(String.format("%.2f",(expFee.get("expectedConvFeeAmtUpiPPI")+expFee.get("expectedPlatformFeeAmt"))));
    softAssert.assertThat(jsonPath.getString(
            "body.consultDetails.UPI.payModeSubTypeConsultDetails[2].totalTransactionAmount.value"))
        .isEqualTo(String.format("%.2f", Double.parseDouble(initTxnDTO.txnAmountFromBody()) + expFee.get("expectedConvFeeAmtUpiPPI")+expFee.get("expectedPlatformFeeAmt")));
    softAssert.assertThat(jsonPath.getString(
            "body.consultDetails.UPI.payModeSubTypeConsultDetails[2].convenienceCharges.value"))
        .isEqualTo(String.format("%.2f",expFee.get("expectedConvFeeAmtUpiPPI")));
    softAssert.assertThat(jsonPath.getString(
            "body.consultDetails.UPI.payModeSubTypeConsultDetails[2].platformCharges.value"))
        .isEqualTo(String.format("%.2f",expFee.get("expectedPlatformFeeAmt")));
    softAssert.assertAll();
  }

  @Test(description = "Verify subtypes are not returned when only payoption is sent")
  public void testCase_02() {
    SoftAssertions softAssert = new SoftAssertions();

    List<PayMethod> payMethods = new ArrayList<>();
    PayMethod upiPayMethod = new PayMethod();
    upiPayMethod.setPayMethod("UPI");
    upiPayMethod.setAdditionalProperty("payOption", "UPI_INTENT");

    payMethods.add(upiPayMethod);

    InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, convFeeAndPlatformFeeMerchant)
        .setTxnValue("500.00")
        .build();

    InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
    String txnToken = initTxnResponse.getBody().getTxnToken();

    FetchPcfRequest fetchPcfRequest = new FetchPcfRequest.Builder(txnToken, "TXN_TOKEN")
        .setMid(convFeeAndPlatformFeeMerchant.getId())
        .setTxnAmount("500.00")
        .setPayMethods(payMethods)
        .build();

    FetchPcfDetail fetchPcfDetail = new FetchPcfDetail(convFeeAndPlatformFeeMerchant.getId(),
        initTxnDTO.orderFromBody(), fetchPcfRequest);
    JsonPath jsonPath = fetchPcfDetail.execute().jsonPath();

    softAssert.assertThat(jsonPath.getList("body.consultDetails.UPI.payModeSubTypeConsultDetails"))
        .isNull();
    softAssert.assertAll();
  }

  @Test(description = "Verify UPI CC fee is returned only when UPI_CREDIT_CARD is sent")
  public void testCase_03() {
    SoftAssertions softAssert = new SoftAssertions();

    List<PayMethod> payMethods = new ArrayList<>();
    PayMethod upiPayMethod = new PayMethod();
    upiPayMethod.setPayMethod("UPI");
    upiPayMethod.setAdditionalProperty("upiModeSubTypes", Arrays.asList("UPI_CREDIT_CARD"));
    upiPayMethod.setAdditionalProperty("payOption", "UPI_INTENT");

    payMethods.add(upiPayMethod);

    InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, convFeeAndPlatformFeeMerchant)
        .setTxnValue("500.00")
        .build();

    InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
    String txnToken = initTxnResponse.getBody().getTxnToken();

    FetchPcfRequest fetchPcfRequest = new FetchPcfRequest.Builder(txnToken, "TXN_TOKEN")
        .setMid(convFeeAndPlatformFeeMerchant.getId())
        .setTxnAmount("500.00")
        .setPayMethods(payMethods)
        .build();

    FetchPcfDetail fetchPcfDetail = new FetchPcfDetail(convFeeAndPlatformFeeMerchant.getId(),
        initTxnDTO.orderFromBody(), fetchPcfRequest);
    JsonPath jsonPath = fetchPcfDetail.execute().jsonPath();
    HashMap<String,Double> expFee=convenienceFeeUpiIntentUI.calculateExpectedFees(Double.valueOf(initTxnDTO.txnAmountFromBody()),convFeeAndPlatformFeeMerchant);


    List<String> payModeSubTypes = jsonPath.getList(
        "body.consultDetails.UPI.payModeSubTypeConsultDetails.payModeSubType");
    softAssert.assertThat(payModeSubTypes).containsExactly("UPI_CREDIT_CARD");
    softAssert.assertThat(jsonPath.getString(
            "body.consultDetails.UPI.payModeSubTypeConsultDetails[0].baseTransactionAmount.value"))
        .isEqualTo(initTxnDTO.txnAmountFromBody());
    softAssert.assertThat(jsonPath.getString(
            "body.consultDetails.UPI.payModeSubTypeConsultDetails[0].totalConvenienceCharges.value"))
        .isEqualTo(String.format("%.2f",(expFee.get("expectedConvFeeAmtUpiCC")+expFee.get("expectedPlatformFeeAmt"))));
    softAssert.assertThat(jsonPath.getString(
            "body.consultDetails.UPI.payModeSubTypeConsultDetails[0].totalTransactionAmount.value"))
        .isEqualTo(String.format("%.2f", Double.parseDouble(initTxnDTO.txnAmountFromBody()) + expFee.get("expectedConvFeeAmtUpiCC")+expFee.get("expectedPlatformFeeAmt")));
    softAssert.assertThat(jsonPath.getString(
            "body.consultDetails.UPI.payModeSubTypeConsultDetails[0].convenienceCharges.value"))
        .isEqualTo(String.format("%.2f",(expFee.get("expectedConvFeeAmtUpiCC"))));
    softAssert.assertThat(jsonPath.getString(
            "body.consultDetails.UPI.payModeSubTypeConsultDetails[0].platformCharges.value"))
        .isEqualTo(String.format("%.2f",expFee.get("expectedPlatformFeeAmt")));
    softAssert.assertAll();
  }

  @Test(description = "Verify PPI Wallet fee is returned only when UPI_PPIWALLET is sent")
  public void testCase_04() {
    SoftAssertions softAssert = new SoftAssertions();

    List<PayMethod> payMethods = new ArrayList<>();
    PayMethod upiPayMethod = new PayMethod();
    upiPayMethod.setPayMethod("UPI");
    upiPayMethod.setAdditionalProperty("upiModeSubTypes", Arrays.asList("UPI_PPIWALLET"));
    upiPayMethod.setAdditionalProperty("payOption", "UPI_INTENT");

    payMethods.add(upiPayMethod);

    InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, convFeeAndPlatformFeeMerchant)
        .setTxnValue("500.00")
        .build();

    InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
    String txnToken = initTxnResponse.getBody().getTxnToken();

    FetchPcfRequest fetchPcfRequest = new FetchPcfRequest.Builder(txnToken, "TXN_TOKEN")
        .setMid(convFeeAndPlatformFeeMerchant.getId())
        .setTxnAmount("500.00")
        .setPayMethods(payMethods)
        .build();

    FetchPcfDetail fetchPcfDetail = new FetchPcfDetail(convFeeAndPlatformFeeMerchant.getId(),
        initTxnDTO.orderFromBody(), fetchPcfRequest);
    JsonPath jsonPath = fetchPcfDetail.execute().jsonPath();
    HashMap<String,Double> expFee=convenienceFeeUpiIntentUI.calculateExpectedFees(Double.valueOf(initTxnDTO.txnAmountFromBody()),convFeeAndPlatformFeeMerchant);

    List<String> payModeSubTypes = jsonPath.getList(
        "body.consultDetails.UPI.payModeSubTypeConsultDetails.payModeSubType");
    // Verify UPI_PPIWALLET details
    softAssert.assertThat(jsonPath.getString(
            "body.consultDetails.UPI.payModeSubTypeConsultDetails[0].payModeSubType"))
        .isEqualTo("UPI_PPIWALLET");
    softAssert.assertThat(jsonPath.getString(
            "body.consultDetails.UPI.payModeSubTypeConsultDetails[0].baseTransactionAmount.value"))
        .isEqualTo(initTxnDTO.txnAmountFromBody());
    softAssert.assertThat(jsonPath.getString(
            "body.consultDetails.UPI.payModeSubTypeConsultDetails[0].totalConvenienceCharges.value"))
        .isEqualTo(String.format("%.2f",(expFee.get("expectedConvFeeAmtUpiPPI")+expFee.get("expectedPlatformFeeAmt"))));
    softAssert.assertThat(jsonPath.getString(
            "body.consultDetails.UPI.payModeSubTypeConsultDetails[0].totalTransactionAmount.value"))
        .isEqualTo(String.format("%.2f", Double.parseDouble(initTxnDTO.txnAmountFromBody()) + expFee.get("expectedConvFeeAmtUpiPPI")+expFee.get("expectedPlatformFeeAmt")));
    softAssert.assertThat(jsonPath.getString(
            "body.consultDetails.UPI.payModeSubTypeConsultDetails[0].convenienceCharges.value"))
        .isEqualTo(String.format("%.2f",expFee.get("expectedConvFeeAmtUpiPPI")));
    softAssert.assertThat(jsonPath.getString(
            "body.consultDetails.UPI.payModeSubTypeConsultDetails[0].platformCharges.value"))
        .isEqualTo(String.format("%.2f",expFee.get("expectedPlatformFeeAmt")));
    softAssert.assertAll();
  }

  @Test(description = "Verify Credit Line fee is returned only when UPI_CREDITLINE is sent")
  public void testCase_05() {
    SoftAssertions softAssert = new SoftAssertions();

    List<PayMethod> payMethods = new ArrayList<>();
    PayMethod upiPayMethod = new PayMethod();
    upiPayMethod.setPayMethod("UPI");
    upiPayMethod.setAdditionalProperty("upiModeSubTypes", Arrays.asList("UPI_CREDITLINE"));
    upiPayMethod.setAdditionalProperty("payOption", "UPI_INTENT");

    payMethods.add(upiPayMethod);

    InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, convFeeAndPlatformFeeMerchant)
        .setTxnValue("500.00")
        .build();

    InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
    String txnToken = initTxnResponse.getBody().getTxnToken();

    FetchPcfRequest fetchPcfRequest = new FetchPcfRequest.Builder(txnToken, "TXN_TOKEN")
        .setMid(convFeeAndPlatformFeeMerchant.getId())
        .setTxnAmount("500.00")
        .setPayMethods(payMethods)
        .build();

    FetchPcfDetail fetchPcfDetail = new FetchPcfDetail(convFeeAndPlatformFeeMerchant.getId(),
        initTxnDTO.orderFromBody(), fetchPcfRequest);
    JsonPath jsonPath = fetchPcfDetail.execute().jsonPath();
    HashMap<String,Double> expFee=convenienceFeeUpiIntentUI.calculateExpectedFees(Double.valueOf(initTxnDTO.txnAmountFromBody()),convFeeAndPlatformFeeMerchant);

    List<String> payModeSubTypes = jsonPath.getList(
        "body.consultDetails.UPI.payModeSubTypeConsultDetails.payModeSubType");
    softAssert.assertThat(payModeSubTypes).containsExactly("UPI_CREDITLINE");
    softAssert.assertThat(jsonPath.getString(
            "body.consultDetails.UPI.payModeSubTypeConsultDetails[0].baseTransactionAmount.value"))
        .isEqualTo(initTxnDTO.txnAmountFromBody());
    softAssert.assertThat(jsonPath.getString(
            "body.consultDetails.UPI.payModeSubTypeConsultDetails[0].totalConvenienceCharges.value"))
        .isEqualTo(String.format("%.2f",(expFee.get("expectedConvFeeAmtUpiCL")+expFee.get("expectedPlatformFeeAmt"))));
    softAssert.assertThat(jsonPath.getString(
            "body.consultDetails.UPI.payModeSubTypeConsultDetails[0].totalTransactionAmount.value"))
        .isEqualTo(String.format("%.2f", Double.parseDouble(initTxnDTO.txnAmountFromBody()) + expFee.get("expectedConvFeeAmtUpiCL")+expFee.get("expectedPlatformFeeAmt")));
    softAssert.assertThat(jsonPath.getString(
            "body.consultDetails.UPI.payModeSubTypeConsultDetails[0].convenienceCharges.value"))
        .isEqualTo(String.format("%.2f",(expFee.get("expectedConvFeeAmtUpiCL"))));
    softAssert.assertThat(jsonPath.getString(
            "body.consultDetails.UPI.payModeSubTypeConsultDetails[0].platformCharges.value"))
        .isEqualTo(String.format("%.2f",expFee.get("expectedPlatformFeeAmt")));
    softAssert.assertAll();
  }

  @Test(description = "Verify Fee details for merchant with only Convenience Fee")
  public void testCase_06() {
    SoftAssertions softAssert = new SoftAssertions();

    List<PayMethod> payMethods = new ArrayList<>();
    PayMethod upiPayMethod = new PayMethod();
    upiPayMethod.setPayMethod("UPI");
    upiPayMethod.setAdditionalProperty("upiModeSubTypes",
        Arrays.asList("UPI_CREDITLINE", "UPI_CREDIT_CARD", "UPI_PPIWALLET"));
    upiPayMethod.setAdditionalProperty("payOption", "UPI_INTENT");
    payMethods.add(upiPayMethod);

    // Test with merchant that has UPI CC enabled
    InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, convFeeMerchant)
        .setTxnValue("500.00")
        .build();

    InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
    String txnToken = initTxnResponse.getBody().getTxnToken();

    FetchPcfRequest fetchPcfRequest = new FetchPcfRequest.Builder(txnToken, "TXN_TOKEN")
        .setMid(convFeeMerchant.getId())
        .setTxnAmount("500.00")
        .setPayMethods(payMethods)
        .build();

    // Execute fetchPCF request
    FetchPcfDetail fetchPcfDetail = new FetchPcfDetail(convFeeMerchant.getId(),
        initTxnDTO.orderFromBody(), fetchPcfRequest);
    JsonPath jsonPath = fetchPcfDetail.execute().jsonPath();
    HashMap<String,Double> expFee=convenienceFeeUpiIntentUI.calculateExpectedFees(Double.valueOf(initTxnDTO.txnAmountFromBody()),convFeeMerchant);

    // Verify response
    softAssert.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
    softAssert.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
    softAssert.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

    // Verify UPI consult details
    softAssert.assertThat(jsonPath.getString("body.consultDetails.UPI.payMethod")).isEqualTo("UPI");
    softAssert.assertThat(jsonPath.getString("body.consultDetails.UPI.baseTransactionAmount.value"))
        .isEqualTo(initTxnDTO.txnAmountFromBody());
    softAssert.assertThat(
            jsonPath.getString("body.consultDetails.UPI.totalConvenienceCharges.value"))
        .isEqualTo(String.format("%.2f",expFee.get("expectedPlatformFeeAmt")));
    softAssert.assertThat(jsonPath.getString("body.consultDetails.UPI.totalTransactionAmount.value"))
        .isEqualTo(String.format("%.2f", Double.parseDouble(initTxnDTO.txnAmountFromBody()) + expFee.get("expectedPlatformFeeAmt")));
    softAssert.assertThat(jsonPath.getString("body.consultDetails.UPI.text"))
        .isEqualTo("Convenience fee of Rs. "+String.format("%.2f",expFee.get("expectedPlatformFeeAmt"))+" is applicable.");
    softAssert.assertThat(jsonPath.getString("body.consultDetails.UPI.displayText"))
        .isEqualTo("BHIM UPI");
    softAssert.assertThat(jsonPath.getString("body.consultDetails.UPI.convenienceCharges.value"))
        .isEqualTo("0.00");
    softAssert.assertThat(jsonPath.getString("body.consultDetails.UPI.platformCharges.value"))
        .isEqualTo("0");

    // Verify payModeSubTypeConsultDetails
    List<String> payModeSubTypes = jsonPath.getList(
        "body.consultDetails.UPI.payModeSubTypeConsultDetails.payModeSubType");
    softAssert.assertThat(payModeSubTypes)
        .contains("UPI_CREDITLINE", "UPI_CREDIT_CARD", "UPI_PPIWALLET");

    // Verify UPI_CREDITLINE details
    softAssert.assertThat(jsonPath.getString(
            "body.consultDetails.UPI.payModeSubTypeConsultDetails[0].payModeSubType"))
        .isEqualTo("UPI_CREDITLINE");
    softAssert.assertThat(jsonPath.getString(
            "body.consultDetails.UPI.payModeSubTypeConsultDetails[0].baseTransactionAmount.value"))
        .isEqualTo(initTxnDTO.txnAmountFromBody());
    softAssert.assertThat(jsonPath.getString(
            "body.consultDetails.UPI.payModeSubTypeConsultDetails[0].totalConvenienceCharges.value"))
        .isEqualTo(String.format("%.2f",(expFee.get("expectedConvFeeAmtUpiCL")+expFee.get("expectedPlatformFeeAmt"))));
    softAssert.assertThat(jsonPath.getString(
            "body.consultDetails.UPI.payModeSubTypeConsultDetails[0].totalTransactionAmount.value"))
        .isEqualTo(String.format("%.2f", Double.parseDouble(initTxnDTO.txnAmountFromBody()) + expFee.get("expectedConvFeeAmtUpiCL")+expFee.get("expectedPlatformFeeAmt")));
    softAssert.assertThat(jsonPath.getString(
            "body.consultDetails.UPI.payModeSubTypeConsultDetails[0].convenienceCharges.value"))
        .isEqualTo(String.format("%.2f",(expFee.get("expectedConvFeeAmtUpiCL"))));
    softAssert.assertThat(jsonPath.getString(
            "body.consultDetails.UPI.payModeSubTypeConsultDetails[0].platformCharges.value"))
        .isEqualTo("0");

    // Verify UPI_CREDIT_CARD details
    softAssert.assertThat(jsonPath.getString(
            "body.consultDetails.UPI.payModeSubTypeConsultDetails[1].payModeSubType"))
        .isEqualTo("UPI_CREDIT_CARD");
    softAssert.assertThat(jsonPath.getString(
            "body.consultDetails.UPI.payModeSubTypeConsultDetails[1].baseTransactionAmount.value"))
        .isEqualTo(initTxnDTO.txnAmountFromBody());
    softAssert.assertThat(jsonPath.getString(
            "body.consultDetails.UPI.payModeSubTypeConsultDetails[1].totalConvenienceCharges.value"))
        .isEqualTo(String.format("%.2f",(expFee.get("expectedConvFeeAmtUpiCL")+expFee.get("expectedPlatformFeeAmt"))));
    softAssert.assertThat(jsonPath.getString(
            "body.consultDetails.UPI.payModeSubTypeConsultDetails[1].totalTransactionAmount.value"))
        .isEqualTo(String.format("%.2f", Double.parseDouble(initTxnDTO.txnAmountFromBody()) + expFee.get("expectedConvFeeAmtUpiCL")+expFee.get("expectedPlatformFeeAmt")));
    softAssert.assertThat(jsonPath.getString(
            "body.consultDetails.UPI.payModeSubTypeConsultDetails[1].convenienceCharges.value"))
        .isEqualTo(String.format("%.2f",expFee.get("expectedConvFeeAmtUpiCL")));
    softAssert.assertThat(jsonPath.getString(
            "body.consultDetails.UPI.payModeSubTypeConsultDetails[1].platformCharges.value"))
        .isEqualTo("0");


    // Verify UPI_PPIWALLET details
    softAssert.assertThat(jsonPath.getString(
            "body.consultDetails.UPI.payModeSubTypeConsultDetails[2].payModeSubType"))
        .isEqualTo("UPI_PPIWALLET");
    softAssert.assertThat(jsonPath.getString(
            "body.consultDetails.UPI.payModeSubTypeConsultDetails[2].baseTransactionAmount.value"))
        .isEqualTo(initTxnDTO.txnAmountFromBody());
    softAssert.assertThat(jsonPath.getString(
            "body.consultDetails.UPI.payModeSubTypeConsultDetails[2].totalConvenienceCharges.value"))
        .isEqualTo(String.format("%.2f",(expFee.get("expectedConvFeeAmtUpiPPI")+expFee.get("expectedPlatformFeeAmt"))));
    softAssert.assertThat(jsonPath.getString(
            "body.consultDetails.UPI.payModeSubTypeConsultDetails[2].totalTransactionAmount.value"))
        .isEqualTo(String.format("%.2f", Double.parseDouble(initTxnDTO.txnAmountFromBody()) + expFee.get("expectedConvFeeAmtUpiPPI")+expFee.get("expectedPlatformFeeAmt")));
    softAssert.assertThat(jsonPath.getString(
            "body.consultDetails.UPI.payModeSubTypeConsultDetails[2].convenienceCharges.value"))
        .isEqualTo(String.format("%.2f",expFee.get("expectedConvFeeAmtUpiPPI")));
    softAssert.assertThat(jsonPath.getString(
            "body.consultDetails.UPI.payModeSubTypeConsultDetails[2].platformCharges.value"))
        .isEqualTo("0");
    softAssert.assertAll();
  }

  @Test(description = "Verify fee for merchant which has only platform fee")
  public void testCase_07() {
    SoftAssertions softAssert = new SoftAssertions();

    InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, platformFeeMerchant)
        .setTxnValue("500.00")
        .build();

    InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
    String txnToken = initTxnResponse.getBody().getTxnToken();

    List<PayMethod> payMethods = new ArrayList<>();
    PayMethod upiPayMethod = new PayMethod()
        .setPayMethod("UPI");
    upiPayMethod.setAdditionalProperty("upiModeSubTypes",
        Arrays.asList("UPI_CREDITLINE", "UPI_CREDIT_CARD", "UPI_PPIWALLET"));
    upiPayMethod.setAdditionalProperty("payOption", "UPI_INTENT");
    payMethods.add(upiPayMethod);

    FetchPcfRequest fetchPcfRequest = new FetchPcfRequest.Builder(txnToken, "TXN_TOKEN")
        .setMid(platformFeeMerchant.getId())
        .setTxnAmount("500.00")
        .setPayMethods(payMethods)
        .build();

    // Execute fetchPCF request
    FetchPcfDetail fetchPcfDetail = new FetchPcfDetail(platformFeeMerchant.getId(),
        initTxnDTO.orderFromBody(), fetchPcfRequest);
    JsonPath jsonPath = fetchPcfDetail.execute().jsonPath();
    HashMap<String,Double> expFee=convenienceFeeUpiIntentUI.calculateExpectedFees(Double.valueOf(initTxnDTO.txnAmountFromBody()),platformFeeMerchant);

    // Verify response
    softAssert.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
    softAssert.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
    softAssert.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

    // Verify UPI consult details
    softAssert.assertThat(jsonPath.getString("body.consultDetails.UPI.payMethod")).isEqualTo("UPI");
    softAssert.assertThat(jsonPath.getString("body.consultDetails.UPI.baseTransactionAmount.value"))
        .isEqualTo(initTxnDTO.txnAmountFromBody());
    softAssert.assertThat(
            jsonPath.getString("body.consultDetails.UPI.totalConvenienceCharges.value"))
        .isEqualTo(String.format("%.2f",expFee.get("expectedPlatformFeeAmt")));
    softAssert.assertThat(jsonPath.getString("body.consultDetails.UPI.totalTransactionAmount.value"))
        .isEqualTo(String.format("%.2f", Double.parseDouble(initTxnDTO.txnAmountFromBody()) + expFee.get("expectedPlatformFeeAmt")));
    softAssert.assertThat(jsonPath.getString("body.consultDetails.UPI.text"))
        .isEqualTo("Convenience fee of Rs. "+String.format("%.2f",expFee.get("expectedPlatformFeeAmt"))+" is applicable.");
    softAssert.assertThat(jsonPath.getString("body.consultDetails.UPI.displayText"))
        .isEqualTo("BHIM UPI");
    softAssert.assertThat(jsonPath.getString("body.consultDetails.UPI.convenienceCharges.value"))
        .isEqualTo("0.00");
    softAssert.assertThat(jsonPath.getString("body.consultDetails.UPI.platformCharges.value"))
        .isEqualTo(String.format("%.2f",expFee.get("expectedPlatformFeeAmt")));

    // Verify payModeSubTypeConsultDetails
    List<String> payModeSubTypes = jsonPath.getList(
        "body.consultDetails.UPI.payModeSubTypeConsultDetails.payModeSubType");
    softAssert.assertThat(payModeSubTypes)
        .contains("UPI_CREDITLINE", "UPI_CREDIT_CARD", "UPI_PPIWALLET");

    // Verify UPI_CREDITLINE details
    softAssert.assertThat(jsonPath.getString(
            "body.consultDetails.UPI.payModeSubTypeConsultDetails[0].payModeSubType"))
        .isEqualTo("UPI_CREDITLINE");
    softAssert.assertThat(jsonPath.getString(
            "body.consultDetails.UPI.payModeSubTypeConsultDetails[0].baseTransactionAmount.value"))
        .isEqualTo(initTxnDTO.txnAmountFromBody());
    softAssert.assertThat(jsonPath.getString(
            "body.consultDetails.UPI.payModeSubTypeConsultDetails[0].totalConvenienceCharges.value"))
        .isEqualTo(String.format("%.2f",(expFee.get("expectedConvFeeAmtUpiCL")+expFee.get("expectedPlatformFeeAmt"))));
    softAssert.assertThat(jsonPath.getString(
            "body.consultDetails.UPI.payModeSubTypeConsultDetails[0].totalTransactionAmount.value"))
        .isEqualTo(String.format("%.2f", Double.parseDouble(initTxnDTO.txnAmountFromBody()) + expFee.get("expectedConvFeeAmtUpiCL")+expFee.get("expectedPlatformFeeAmt")));
    softAssert.assertThat(jsonPath.getString(
            "body.consultDetails.UPI.payModeSubTypeConsultDetails[0].convenienceCharges.value"))
        .isEqualTo(String.format("%.2f",(expFee.get("expectedConvFeeAmtUpiCL"))));
    softAssert.assertThat(jsonPath.getString(
            "body.consultDetails.UPI.payModeSubTypeConsultDetails[0].platformCharges.value"))
        .isEqualTo(String.format("%.2f",expFee.get("expectedPlatformFeeAmt")));

    // Verify UPI_CREDIT_CARD details
    softAssert.assertThat(jsonPath.getString(
            "body.consultDetails.UPI.payModeSubTypeConsultDetails[1].payModeSubType"))
        .isEqualTo("UPI_CREDIT_CARD");
    softAssert.assertThat(jsonPath.getString(
            "body.consultDetails.UPI.payModeSubTypeConsultDetails[1].baseTransactionAmount.value"))
        .isEqualTo(initTxnDTO.txnAmountFromBody());
    softAssert.assertThat(jsonPath.getString(
            "body.consultDetails.UPI.payModeSubTypeConsultDetails[1].totalConvenienceCharges.value"))
        .isEqualTo(String.format("%.2f",(expFee.get("expectedConvFeeAmtUpiCL")+expFee.get("expectedPlatformFeeAmt"))));
    softAssert.assertThat(jsonPath.getString(
            "body.consultDetails.UPI.payModeSubTypeConsultDetails[1].totalTransactionAmount.value"))
        .isEqualTo(String.format("%.2f", Double.parseDouble(initTxnDTO.txnAmountFromBody()) + expFee.get("expectedConvFeeAmtUpiCL")+expFee.get("expectedPlatformFeeAmt")));
    softAssert.assertThat(jsonPath.getString(
            "body.consultDetails.UPI.payModeSubTypeConsultDetails[1].convenienceCharges.value"))
        .isEqualTo(String.format("%.2f",expFee.get("expectedConvFeeAmtUpiCL")));
    softAssert.assertThat(jsonPath.getString(
            "body.consultDetails.UPI.payModeSubTypeConsultDetails[1].platformCharges.value"))
        .isEqualTo(String.format("%.2f",expFee.get("expectedPlatformFeeAmt")));


    // Verify UPI_PPIWALLET details
    softAssert.assertThat(jsonPath.getString(
            "body.consultDetails.UPI.payModeSubTypeConsultDetails[2].payModeSubType"))
        .isEqualTo("UPI_PPIWALLET");
    softAssert.assertThat(jsonPath.getString(
            "body.consultDetails.UPI.payModeSubTypeConsultDetails[2].baseTransactionAmount.value"))
        .isEqualTo(initTxnDTO.txnAmountFromBody());
    softAssert.assertThat(jsonPath.getString(
            "body.consultDetails.UPI.payModeSubTypeConsultDetails[2].totalConvenienceCharges.value"))
        .isEqualTo(String.format("%.2f",(expFee.get("expectedConvFeeAmtUpiPPI")+expFee.get("expectedPlatformFeeAmt"))));
    softAssert.assertThat(jsonPath.getString(
            "body.consultDetails.UPI.payModeSubTypeConsultDetails[2].totalTransactionAmount.value"))
        .isEqualTo(String.format("%.2f", Double.parseDouble(initTxnDTO.txnAmountFromBody()) + expFee.get("expectedConvFeeAmtUpiPPI")+expFee.get("expectedPlatformFeeAmt")));
    softAssert.assertThat(jsonPath.getString(
            "body.consultDetails.UPI.payModeSubTypeConsultDetails[2].convenienceCharges.value"))
        .isEqualTo(String.format("%.2f",expFee.get("expectedConvFeeAmtUpiPPI")));
    softAssert.assertThat(jsonPath.getString(
            "body.consultDetails.UPI.payModeSubTypeConsultDetails[2].platformCharges.value"))
        .isEqualTo(String.format("%.2f",expFee.get("expectedPlatformFeeAmt")));
    softAssert.assertAll();
  }


  @Test(description = "Verify QR code decoding from FPO response")
  public void testCase_101() {
    SoftAssertions softAssert = new SoftAssertions();

    // Sample FPO response QR code data
    String base64QrCode = "iVBORw0KGgoAAAANSUhEUgAAAeAAAAHgAQAAAABVr4M4AAAG6UlEQVR4Xu2XUa6lOAxEky+WcXcKyU5ZBl946pS53a0ZjTTkfY4jRCe2j5FeUeZ2ix+s9vfAm1Xwy1Xwy1Xwy/U/ha+m1a8Pm+1sW8yr7e3zFFztcJzI9RmuHBCfgpdg6qZC29kjbhdNb4YaeTPjPGj0RKb3Ba/B40mfu6XS8W5tp1cy7bsRwJKWveCfwFcT3J1LtW7BjyWA1euwhGMr+MdwIMkO5rkDQIqmHFM8hZhHBf8EVmzyuueUOUk8ylFKAS3E84y8TBW8AFuYSwL812tAFPzPon+7/oBz2QB69Z2zPJpH+gAwhjgOW2UGBV+m4PewEpu+rNige39YPEWOiOEtwlyOmJxpmIKX4GlVNNaPAJh8X4VxD9VtCNlpxHeXY2SXgt/D/N2RoYM19o17t08GpTwg/A1oj0/oVfAa3C9+rzQr0e0Kk8QfTNXyTMM2pOj+KXgFdnRKBuUamu1PI5Tj+AV68CRFGi4qeAkOD3GXSo95+b1Xx+3pO3CCPwwAYIOBFQWvwBdDX2fxkmcgmxoBe4MHMgiDhVDxplHBC3CaQYdshDEwA2d9cbHBNKNKPWD48ip4AQboKY9nzU36aXE4mJOoWchsMQtehTPtatLTCn0x6iaAHnD66CcVvAg3r4+LuA+mDG7pFxNnZN6YOgYtclPwAmxJGOWI0YnSJXyN7TuJTN5W7sYeBS/B8eWdEKZLUnXsAQNJF+zRjDkbBa/A1ycsQ6YRpqkXyimY2X6hpbP4ZEe/gtdgRBoAbMLwzeVGjCRS2vvoiOqpLHgBRiStkXUeRqpwHOC4Wko1HrVcIIcUvAY3LSc0g5jpLFXP1OY5UqOUGjXuBS/BVH+CUc5d8z3y+sbzuunCA8A4RsErsAFFG5bgeFOETrut4iN+0PGRk2PBS7ABGI/+X8eRMyhQKLNf8jkWvAo3rSPnES20gLWfETmVDur+mEoFr8EXu9si6T4QhtLbI35HMLeQbS4iekZgm4LXYDCPIQO6S63tVMfAHlLOstH9PPLHDQUFL8EO7a4ejRYTJRzUDxc2aBO44ul76ANAr4IX4G/1d9OtkDa7zaCO3fG2PbxbRMFLMP/V2Qmx0dWo5hIswYh4GKlLMzZailfwCnxIiSsTvPF3a4eHvvs+2qjgoB01Vi4KXoFNHtkCnbgjkmU7+KzaG2GRHN/V/foUvAL/NoPOjwcegHnkph5MkSahERIWvAj7jQ9V2AypRFbvjU3u1ZQaPsaMrYJX4MuSWJgZ1Cnd2dgbD9McCRUEcvJPwUswzM35bBZpuldW3B73EkybW8FGSjXjK1XBL2FCaODR36zT7chArYTpNanMRqlWwe/hyJkSoxlWTibxMOKSQmohjEZmLgoKXoStUL8+kQ5BCfeCYT88oQh6GKnXvvGMgpfgnCyoErSggqV96uemdoVEymBuCn4Pu4KFKr/qXPodPY04VlEjXOHigpfgdIVHz/Y4JFIYKugYqOXBtJ2HC5wq+D3sgyTZt7g3xvotYRwcRCiYef3yScHrMDM9DEuV23DYAD20p1pZmIj5q1g1BS/AFqC5FJhqnZQLquN00gWIlNsPDyh4Ab7Q4FmYQeMmX/3z0JEfkY9DlDoinfNb54LfwXr7t7OHmPTAo9a+WSo+BlkAj0P8DPYFr8AnNvB8EdbQg7pJWzqqbwKSzZWQzhb8HkYVpDqQ4eSyPXQPupxqmjUih2Ge90hV8EsYPTj3nEfWbGxsVA1MCgk7LWQVWtOx4AX44q8/kIGclnKoYt4RSgdmQL/blQUvwn77dzRou8d928THrX08QHw3t1MjPw8Fr8AP2elCL+0PB0ezKuFNGqOhq5CDwoLfwzq4+rBm7an7THgFc74HXfyA4aMfU/B7WFOG957z3fhN0xn3Wp80DEGVJpmWsHkKXoH1ulOhnBRqyHPBH3bI7sF0BDNo8oxTTSddPgUvwTqASQmpMi8Zw5NIcQdpIc2+3RHJ2YJXYP/d7+/0GRgDSwgLjkTadvpSozQJ9QWvwFaiIZKK1AgzqFRm2G2YHR4Jb/cNTIJtCl6BI/i7ZwvLMDyJ4CmySA2F5B8x0xH1LXgFvj4A37T51lAFXncaNd15QLdOrILX4FzOHRpJmIF98wDa4X1U68TI4pOCV2CHOh9aLUl1Kj2xh1OGhe2NgmkXzYLXYf0TyLA5p/uFNrvH/cQJqc1TMCykUgWvwcNF3cCRdSwNJoKdI1+ClM3SMrMKXocZ7p875bnIEbFO08Koe3v452d9wT+BtY6ItESuXaViWCjU7Qq6oFwUvAbr30mC1V3kUshs161WeN/4Buex4AX4YbjHbz+kN9oGPy1ndlRk39Cs4BV4fRX8chX8chX8chX8cv0Fo1PywYwiBB4AAAAASUVORK5CYII=";

    String decodedUrl = QRHelper.getDeeplinkFromQRImage(base64QrCode);
    System.out.println("Decoded URL: " + decodedUrl);

    // Verify the decoded URL is not empty and contains expected format
    softAssert.assertThat(decodedUrl).isNotEmpty();
    softAssert.assertThat(decodedUrl).startsWith("upi://");

    softAssert.assertAll();
  }

  @Test(description = "Verify end-to-end UPI payment flow with convenience fees and platform fee for UPI PPI Wallet")
  public void testCase_08() {
    SoftAssertions softAssert = new SoftAssertions();
    FF4JFlags.enable("theia.upipsp.enable.PPIWallet");
    redisHelper.delete("FF4J_FEATURE_theia.upipsp.enable.PPIWallet");
    FF4JFlags.enable("merchant.status.passUpiSubPayModeContextInResponse");
    redisHelper.delete("FF4J_FEATURE_merchant.status.passUpiSubPayModeContextInResponse");
    // 1. Hit initiate txn API
    InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, convFeeAndPlatformFeeMerchant)
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
    FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(convFeeAndPlatformFeeMerchant.getId(), orderId,
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
    String pconfee = deeplinkInfo.get("pconfee");
    String tr = deeplinkInfo.get("tr");

    HashMap<String,Double> expFee=convenienceFeeUpiIntentUI.calculateExpectedFees(Double.valueOf(initTxnDTO.txnAmountFromBody()),convFeeAndPlatformFeeMerchant);

    softAssert.assertThat(pconfee).isEqualTo(String.format("%.2f",expFee.get("expectedConvFeeAmtUpiPPI")));
    softAssert.assertThat(amount).isEqualTo(String.format("%.2f",Double.parseDouble(initTxnDTO.txnAmountFromBody())+expFee.get("expectedPlatformFeeAmt")));
    softAssert.assertAll();

    String paymentInstrument = "PPI_WALLET";
    String txnAmount;
    String payerPaymentInstrumentFee;

    if ("PPI_WALLET".equals(paymentInstrument)) {
      System.out.println("Updating amount for UPI PPI WALLET");
    }
    txnAmount = String.format("%.2f", Double.parseDouble(amount) + Double.parseDouble(pconfee));
    payerPaymentInstrumentFee = String.format("%.2f", Double.parseDouble(pconfee));

    StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(convFeeAndPlatformFeeMerchant.getId(), tr,
        txnAmount, payeeVpa, "paytmTest@ptys", paymentInstrument, payerPaymentInstrumentFee);
    builder.setPayerName("test");
    builder.setPayerPSP("Phonepe");
    builder.build();
    StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder);
    StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
    Response response = staticQrUpiPSP.execute();
    StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.as(StaticQrUpiPSPResponse.class);

    // Verify order/pay response
    softAssert.assertThat(staticQrUpiPSPResponse.getBody().getResultCode())
        .as("Result code mismatch")
        .isEqualToIgnoringCase("SUCCESS");
    softAssert.assertAll();
    String externalSerialNo = staticQrUpiPSPResponse.getBody().getExternalSerialNo();

    // 5. Hit callback API using UPISecureResponse
    UPISecureResponse secureResponse = new UPISecureResponse().buildRequest(
        "Abhishek Verma",
        "paytmTest@ptys",
        payeeVpa,
        "Punjab National Bank",
        "PUNB",
        externalSerialNo,
        "SUCCESS",
        String.format("%011d", (long) (Math.random() * 100000000000L)),
        txnAmount,
        "0",
        "Transaction is successful",
        "DEFERRED_SETTLEMENT",
        paymentInstrument
    );

    JsonPath callbackResponse = secureResponse.execute().jsonPath();
    softAssert.assertThat(callbackResponse.getString("body.resultCodeId"))
        .as("Result status mismatch")
        .isEqualToIgnoringCase("001");
    softAssert.assertThat(callbackResponse.getString("body.resultCode"))
        .as("Result code mismatch")
        .isEqualToIgnoringCase("SUCCESS");

    // Verify transaction status
    PGPHelpers.getTxnStatus(convFeeAndPlatformFeeMerchant.getId(), orderId)
        .validateStatus("TXN_SUCCESS")
        .validatePaymentMode("UPI")
        .validateGatewayName("PTYBLI")
        .validateTxnAmount(initTxnDTO.txnAmountFromBody())
        .validateUpiModeSubType("UPI_PPIWALLET")
        .validateChargeAmount(String.format("%.2f",
            Double.valueOf(txnAmount) - Double.valueOf(initTxnDTO.txnAmountFromBody())))
        .AssertAll();
    softAssert.assertAll();
  }

  @Test(description = "Verify end-to-end UPI payment flow with convenience fees and platform fee for UPI CC")
  public void testCase_12() {
    SoftAssertions softAssert = new SoftAssertions();
    FF4JFlags.enable("merchant.status.passUpiSubPayModeContextInResponse");
    redisHelper.delete("FF4J_FEATURE_merchant.status.passUpiSubPayModeContextInResponse");
    InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, convFeeAndPlatformFeeMerchant)
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
    FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(convFeeAndPlatformFeeMerchant.getId(), orderId,
        fetchPaymentOptionsDTO);
    JsonPath fpoResponse = fetchPaymentOption.execute().jsonPath();
    softAssert.assertThat(fpoResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
    softAssert.assertThat(fpoResponse.getString("body.resultInfo.resultCode")).isEqualTo("0000");
    softAssert.assertAll();

    // 3. Get QR data and decode deeplink
    String base64QrCode = fpoResponse.getString("body.qrDetail.dataUrl");
    String deeplink = QRHelper.getDeeplinkFromQRImage(base64QrCode);
    System.out.println("Decoded deeplink: " + deeplink);

    HashMap<String,Double> expFee=convenienceFeeUpiIntentUI.calculateExpectedFees(Double.valueOf(initTxnDTO.txnAmountFromBody()),convFeeAndPlatformFeeMerchant);

    // Parse deeplink information
    Map<String, String> deeplinkInfo = parseDeeplinkInfo(deeplink);
    String payeeVpa = deeplinkInfo.get("payeeVpa");
    String amount = deeplinkInfo.get("amount");
    String cconfee = deeplinkInfo.get("cconfee");
    String tr = deeplinkInfo.get("tr");
    softAssert.assertThat(cconfee).isEqualTo(String.format("%.2f",expFee.get("expectedConvFeeAmtUpiCC")));
    softAssert.assertThat(amount).isEqualTo(String.format("%.2f",Double.parseDouble(initTxnDTO.txnAmountFromBody())+expFee.get("expectedPlatformFeeAmt")));
    softAssert.assertAll();

    String paymentInstrument = "UPI_CREDIT_CARD";
    String txnAmount;
    String payerPaymentInstrumentFee = "0.00";

    txnAmount = String.format("%.2f", Double.parseDouble(amount) + Double.parseDouble(cconfee));
    payerPaymentInstrumentFee = String.format("%.2f", Double.parseDouble(cconfee));

    StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(convFeeAndPlatformFeeMerchant.getId(), tr,
        txnAmount, payeeVpa, "paytmTest@ptys", paymentInstrument, payerPaymentInstrumentFee);
    builder.setPayerName("test");
    builder.setPayerPSP("Phonepe");
    builder.build();
    StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder);
    StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
    Response response = staticQrUpiPSP.execute();
    StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.as(StaticQrUpiPSPResponse.class);

    // Verify order/pay response
    softAssert.assertThat(staticQrUpiPSPResponse.getBody().getResultCode())
        .as("Result code mismatch")
        .isEqualToIgnoringCase("SUCCESS");
    softAssert.assertAll();
    String externalSerialNo = staticQrUpiPSPResponse.getBody().getExternalSerialNo();

    // 5. Hit callback API using UPISecureResponse with creditCardInfo
    UPISecureResponse secureResponse = new UPISecureResponse().buildRequest(
        "Abhishek Verma",
        "paytmTest@ptys",
        payeeVpa,
        "Punjab National Bank",
        "PUNB",
        externalSerialNo,
        "SUCCESS",
        String.format("%011d", (long) (Math.random() * 100000000000L)),
        txnAmount,
        "0",
        "Transaction is successful",
        "DEFERRED_SETTLEMENT",
        paymentInstrument
    );

    JsonPath callbackResponse = secureResponse.execute().jsonPath();
    softAssert.assertThat(callbackResponse.getString("body.resultCodeId"))
        .as("Result status mismatch")
        .isEqualToIgnoringCase("001");
    softAssert.assertThat(callbackResponse.getString("body.resultCode"))
        .as("Result code mismatch")
        .isEqualToIgnoringCase("SUCCESS");

    // Verify transaction status
    PGPHelpers.getTxnStatus(convFeeAndPlatformFeeMerchant.getId(), orderId)
        .validateStatus("TXN_SUCCESS")
        .validatePaymentMode("UPI")
        .validateGatewayName("PTYBLI")
        .validateTxnAmount(initTxnDTO.txnAmountFromBody())
        .validateUpiModeSubType("UPI_CREDIT_CARD")
        .validateChargeAmount(String.format("%.2f",
            Double.valueOf(txnAmount) - Double.valueOf(initTxnDTO.txnAmountFromBody())))
        .AssertAll();

    softAssert.assertAll();
  }

  @Test(description = "Verify end-to-end UPI payment flow with convenience fees and platform fee for UPI CREDIT LINE")
  public void testCase_13() {
    SoftAssertions softAssert = new SoftAssertions();
    FF4JFlags.enable("merchant.status.passUpiSubPayModeContextInResponse");
    redisHelper.delete("FF4J_FEATURE_merchant.status.passUpiSubPayModeContextInResponse");

    // 1. Hit initiate txn API
    InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, convFeeAndPlatformFeeMerchant)
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
    FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(convFeeAndPlatformFeeMerchant.getId(), orderId,
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
    String cconfee = deeplinkInfo.get("cconfee");
    String tr = deeplinkInfo.get("tr");

    HashMap<String,Double> expFee=convenienceFeeUpiIntentUI.calculateExpectedFees(Double.valueOf(initTxnDTO.txnAmountFromBody()),convFeeAndPlatformFeeMerchant);
    softAssert.assertThat(cconfee).isEqualTo(String.format("%.2f",expFee.get("expectedConvFeeAmtUpiCL")));
    softAssert.assertThat(amount).isEqualTo(String.format("%.2f",Double.parseDouble(initTxnDTO.txnAmountFromBody())+expFee.get("expectedPlatformFeeAmt")));
    softAssert.assertAll();

    String paymentInstrument = "CREDITLINE_CREDITLINE01";
    String txnAmount;
    String payerPaymentInstrumentFee = "0.00";

    txnAmount = String.format("%.2f", Double.parseDouble(amount) + Double.parseDouble(cconfee));
    payerPaymentInstrumentFee = String.format("%.2f", Double.parseDouble(cconfee));

    StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(convFeeAndPlatformFeeMerchant.getId(), tr,
        txnAmount, payeeVpa, "paytmTest@ptys", paymentInstrument, payerPaymentInstrumentFee);
    builder.setPayerName("test");
    builder.setPayerPSP("Phonepe");
    builder.build();
    StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder);
    StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
    Response response = staticQrUpiPSP.execute();
    StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.as(StaticQrUpiPSPResponse.class);

    // Verify order/pay response
    softAssert.assertThat(staticQrUpiPSPResponse.getBody().getResultCode())
        .as("Result code mismatch")
        .isEqualToIgnoringCase("SUCCESS");
    softAssert.assertAll();
    String externalSerialNo = staticQrUpiPSPResponse.getBody().getExternalSerialNo();

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
        txnAmount,
        "0",
        "Transaction is successful",
        "DEFERRED_SETTLEMENT",
        paymentInstrument
    );

    JsonPath callbackResponse = secureResponse.execute().jsonPath();
    softAssert.assertThat(callbackResponse.getString("body.resultCodeId"))
        .as("Result status mismatch")
        .isEqualToIgnoringCase("001");
    softAssert.assertThat(callbackResponse.getString("body.resultCode"))
        .as("Result code mismatch")
        .isEqualToIgnoringCase("SUCCESS");

    // Verify transaction status
    PGPHelpers.getTxnStatus(convFeeAndPlatformFeeMerchant.getId(), orderId)
        .validateStatus("TXN_SUCCESS")
        .validatePaymentMode("UPI")
        .validateGatewayName("PTYBLI")
        .validateTxnAmount(initTxnDTO.txnAmountFromBody())
        .validateUpiModeSubType("UPI_CREDITLINE")
        .validateChargeAmount(String.format("%.2f",
            Double.valueOf(txnAmount) - Double.valueOf(initTxnDTO.txnAmountFromBody())))
        .AssertAll();

    softAssert.assertAll();
  }

  @Test(description = "Verify end-to-end UPI payment flow with only convenience fees UPI PPI Wallet")
  public void testCase_14() {
    SoftAssertions softAssert = new SoftAssertions();
    FF4JFlags.enable("theia.upipsp.enable.PPIWallet");
    redisHelper.delete("FF4J_FEATURE_theia.upipsp.enable.PPIWallet");
    FF4JFlags.enable("merchant.status.passUpiSubPayModeContextInResponse");
    redisHelper.delete("FF4J_FEATURE_merchant.status.passUpiSubPayModeContextInResponse");

    InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, convFeeMerchant)
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
    FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(convFeeMerchant.getId(), orderId,
        fetchPaymentOptionsDTO);
    JsonPath fpoResponse = fetchPaymentOption.execute().jsonPath();
    softAssert.assertThat(fpoResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
    softAssert.assertThat(fpoResponse.getString("body.resultInfo.resultCode")).isEqualTo("0000");
    softAssert.assertAll();

    // 3. Get QR data and decode deeplink
    String base64QrCode = fpoResponse.getString("body.qrDetail.dataUrl");
    String deeplink = QRHelper.getDeeplinkFromQRImage(base64QrCode);
    System.out.println("Decoded deeplink: " + deeplink);

    HashMap<String,Double> expFee=convenienceFeeUpiIntentUI.calculateExpectedFees(Double.valueOf(initTxnDTO.txnAmountFromBody()),convFeeMerchant);

    // Parse deeplink information
    Map<String, String> deeplinkInfo = parseDeeplinkInfo(deeplink);
    String payeeVpa = deeplinkInfo.get("payeeVpa");
    String amount = deeplinkInfo.get("amount");
    String pconfee = deeplinkInfo.get("pconfee");
    String tr = deeplinkInfo.get("tr");

    softAssert.assertThat(pconfee).isEqualTo(String.format("%.2f",expFee.get("expectedConvFeeAmtUpiPPI")));
    softAssert.assertThat(amount).isEqualTo(String.format("%.2f",Double.parseDouble(initTxnDTO.txnAmountFromBody())));
    softAssert.assertAll();

    String paymentInstrument = "PPI_WALLET";
    String txnAmount;
    String payerPaymentInstrumentFee;

    if ("PPI_WALLET".equals(paymentInstrument)) {
      System.out.println("Updating amount for UPI PPI WALLET");
    }
    txnAmount = String.format("%.2f", Double.parseDouble(amount) + Double.parseDouble(pconfee));
    payerPaymentInstrumentFee = String.format("%.2f", Double.parseDouble(pconfee));

    StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(convFeeMerchant.getId(), tr,
        txnAmount, payeeVpa, "paytmTest@ptys", paymentInstrument, payerPaymentInstrumentFee);
    builder.setPayerName("test");
    builder.setPayerPSP("Phonepe");
    builder.build();
    StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder);
    StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
    Response response = staticQrUpiPSP.execute();
    StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.as(StaticQrUpiPSPResponse.class);

    // Verify order/pay response
    softAssert.assertThat(staticQrUpiPSPResponse.getBody().getResultCode())
        .as("Result code mismatch")
        .isEqualToIgnoringCase("SUCCESS");
    softAssert.assertAll();
    String externalSerialNo = staticQrUpiPSPResponse.getBody().getExternalSerialNo();

    // 5. Hit callback API using UPISecureResponse
    UPISecureResponse secureResponse = new UPISecureResponse().buildRequest(
        "Abhishek Verma",
        "paytmTest@ptys",
        payeeVpa,
        "Punjab National Bank",
        "PUNB",
        externalSerialNo,
        "SUCCESS",
        String.format("%011d", (long) (Math.random() * 100000000000L)),
        txnAmount,
        "0",
        "Transaction is successful",
        "DEFERRED_SETTLEMENT",
        paymentInstrument
    );

    JsonPath callbackResponse = secureResponse.execute().jsonPath();
    softAssert.assertThat(callbackResponse.getString("body.resultCodeId"))
        .as("Result status mismatch")
        .isEqualToIgnoringCase("001");
    softAssert.assertThat(callbackResponse.getString("body.resultCode"))
        .as("Result code mismatch")
        .isEqualToIgnoringCase("SUCCESS");

    // Verify transaction status
    PGPHelpers.getTxnStatus(convFeeMerchant.getId(), orderId)
        .validateStatus("TXN_SUCCESS")
        .validatePaymentMode("UPI")
        .validateGatewayName("PTYBLI")
        .validateTxnAmount(initTxnDTO.txnAmountFromBody())
        .validateUpiModeSubType("UPI_PPIWALLET")
        .validateChargeAmount(String.format("%.2f",
            Double.valueOf(txnAmount) - Double.valueOf(initTxnDTO.txnAmountFromBody())))
        .AssertAll();

    softAssert.assertAll();
  }

  @Test(description ="Verify end-to-end UPI payment flow with only convenience fees for UPI CC ")
  public void testCase_15() {
    SoftAssertions softAssert = new SoftAssertions();
    FF4JFlags.enable("merchant.status.passUpiSubPayModeContextInResponse");
    redisHelper.delete("FF4J_FEATURE_merchant.status.passUpiSubPayModeContextInResponse");
    // 1. Hit initiate txn API
    InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, convFeeMerchant)
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
    FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(convFeeMerchant.getId(), orderId,
        fetchPaymentOptionsDTO);
    JsonPath fpoResponse = fetchPaymentOption.execute().jsonPath();
    softAssert.assertThat(fpoResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
    softAssert.assertThat(fpoResponse.getString("body.resultInfo.resultCode")).isEqualTo("0000");
    softAssert.assertAll();

    // 3. Get QR data and decode deeplink
    String base64QrCode = fpoResponse.getString("body.qrDetail.dataUrl");
    String deeplink = QRHelper.getDeeplinkFromQRImage(base64QrCode);
    System.out.println("Decoded deeplink: " + deeplink);

    HashMap<String,Double> expFee=convenienceFeeUpiIntentUI.calculateExpectedFees(Double.valueOf(initTxnDTO.txnAmountFromBody()),convFeeMerchant);

    // Parse deeplink information
    Map<String, String> deeplinkInfo = parseDeeplinkInfo(deeplink);
    String payeeVpa = deeplinkInfo.get("payeeVpa");
    String amount = deeplinkInfo.get("amount");
    String cconfee = deeplinkInfo.get("cconfee");
    String tr = deeplinkInfo.get("tr");
    softAssert.assertThat(cconfee).isEqualTo(String.format("%.2f",expFee.get("expectedConvFeeAmtUpiCC")));
    softAssert.assertThat(amount).isEqualTo(String.format("%.2f",Double.parseDouble(initTxnDTO.txnAmountFromBody())));
    softAssert.assertAll();

    String paymentInstrument = "UPI_CREDIT_CARD";
    String txnAmount;
    String payerPaymentInstrumentFee = "0.00";

    txnAmount = String.format("%.2f", Double.parseDouble(amount) + Double.parseDouble(cconfee));
    payerPaymentInstrumentFee = String.format("%.2f", Double.parseDouble(cconfee));

    StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(convFeeMerchant.getId(), tr,
        txnAmount, payeeVpa, "paytmTest@ptys", paymentInstrument, payerPaymentInstrumentFee);
    builder.setPayerName("test");
    builder.setPayerPSP("Phonepe");
    builder.build();
    StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder);
    StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
    Response response = staticQrUpiPSP.execute();
    StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.as(StaticQrUpiPSPResponse.class);

    // Verify order/pay response
    softAssert.assertThat(staticQrUpiPSPResponse.getBody().getResultCode())
        .as("Result code mismatch")
        .isEqualToIgnoringCase("SUCCESS");
    softAssert.assertAll();
    String externalSerialNo = staticQrUpiPSPResponse.getBody().getExternalSerialNo();

    // 5. Hit callback API using UPISecureResponse with creditCardInfo
    UPISecureResponse secureResponse = new UPISecureResponse().buildRequest(
        "Abhishek Verma",
        "paytmTest@ptys",
        payeeVpa,
        "Punjab National Bank",
        "PUNB",
        externalSerialNo,
        "SUCCESS",
        String.format("%011d", (long) (Math.random() * 100000000000L)),
        txnAmount,
        "0",
        "Transaction is successful",
        "DEFERRED_SETTLEMENT",
        paymentInstrument
    );

    JsonPath callbackResponse = secureResponse.execute().jsonPath();
    softAssert.assertThat(callbackResponse.getString("body.resultCodeId"))
        .as("Result status mismatch")
        .isEqualToIgnoringCase("001");
    softAssert.assertThat(callbackResponse.getString("body.resultCode"))
        .as("Result code mismatch")
        .isEqualToIgnoringCase("SUCCESS");

    // Verify transaction status
    PGPHelpers.getTxnStatus(convFeeMerchant.getId(), orderId)
        .validateStatus("TXN_SUCCESS")
        .validatePaymentMode("UPI")
        .validateGatewayName("PTYBLI")
        .validateTxnAmount(initTxnDTO.txnAmountFromBody())
        .validateUpiModeSubType("UPI_CREDIT_CARD")
        .validateChargeAmount(String.format("%.2f",
            Double.valueOf(txnAmount) - Double.valueOf(initTxnDTO.txnAmountFromBody())))
        .AssertAll();

    softAssert.assertAll();
  }

  @Test(description = "Verify end-to-end UPI payment flow with only convenience fees for UPI CREDIT LINE")
  public void testCase_16() {
    SoftAssertions softAssert = new SoftAssertions();
    FF4JFlags.enable("merchant.status.passUpiSubPayModeContextInResponse");
    redisHelper.delete("FF4J_FEATURE_merchant.status.passUpiSubPayModeContextInResponse");
    // 1. Hit initiate txn API

    InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, convFeeMerchant)
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
    FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(convFeeMerchant.getId(), orderId,
        fetchPaymentOptionsDTO);
    JsonPath fpoResponse = fetchPaymentOption.execute().jsonPath();
    softAssert.assertThat(fpoResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
    softAssert.assertThat(fpoResponse.getString("body.resultInfo.resultCode")).isEqualTo("0000");
    softAssert.assertAll();

    // 3. Get QR data and decode deeplink
    String base64QrCode = fpoResponse.getString("body.qrDetail.dataUrl");
    String deeplink = QRHelper.getDeeplinkFromQRImage(base64QrCode);
    System.out.println("Decoded deeplink: " + deeplink);

    HashMap<String,Double> expFee=convenienceFeeUpiIntentUI.calculateExpectedFees(Double.valueOf(initTxnDTO.txnAmountFromBody()),convFeeMerchant);

    // Parse deeplink information
    Map<String, String> deeplinkInfo = parseDeeplinkInfo(deeplink);
    String payeeVpa = deeplinkInfo.get("payeeVpa");
    String amount = deeplinkInfo.get("amount");
    String cconfee = deeplinkInfo.get("cconfee");
    String tr = deeplinkInfo.get("tr");

    softAssert.assertThat(cconfee).isEqualTo(String.format("%.2f",expFee.get("expectedConvFeeAmtUpiCL")));
    softAssert.assertThat(amount).isEqualTo(String.format("%.2f",Double.parseDouble(initTxnDTO.txnAmountFromBody())));
    softAssert.assertAll();

    String paymentInstrument = "CREDITLINE_CREDITLINE01";
    String txnAmount;
    String payerPaymentInstrumentFee = "0.00";

    txnAmount = String.format("%.2f", Double.parseDouble(amount) + Double.parseDouble(cconfee));
    payerPaymentInstrumentFee = String.format("%.2f", Double.parseDouble(cconfee));

    StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(convFeeMerchant.getId(), tr,
        txnAmount, payeeVpa, "paytmTest@ptys", paymentInstrument, payerPaymentInstrumentFee);
    builder.setPayerName("test");
    builder.setPayerPSP("Phonepe");
    builder.build();
    StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder);
    StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
    Response response = staticQrUpiPSP.execute();
    StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.as(StaticQrUpiPSPResponse.class);

    // Verify order/pay response
    softAssert.assertThat(staticQrUpiPSPResponse.getBody().getResultCode())
        .as("Result code mismatch")
        .isEqualToIgnoringCase("SUCCESS");
    softAssert.assertAll();
    String externalSerialNo = staticQrUpiPSPResponse.getBody().getExternalSerialNo();

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
        txnAmount,
        "0",
        "Transaction is successful",
        "DEFERRED_SETTLEMENT",
        paymentInstrument
    );

    JsonPath callbackResponse = secureResponse.execute().jsonPath();
    softAssert.assertThat(callbackResponse.getString("body.resultCodeId"))
        .as("Result status mismatch")
        .isEqualToIgnoringCase("001");
    softAssert.assertThat(callbackResponse.getString("body.resultCode"))
        .as("Result code mismatch")
        .isEqualToIgnoringCase("SUCCESS");

    // Verify transaction status
    PGPHelpers.getTxnStatus(convFeeMerchant.getId(), orderId)
        .validateStatus("TXN_SUCCESS")
        .validatePaymentMode("UPI")
        .validateGatewayName("PTYBLI")
        .validateTxnAmount(initTxnDTO.txnAmountFromBody())
        .validateUpiModeSubType("UPI_CREDITLINE")
        .validateChargeAmount(String.format("%.2f",
            Double.valueOf(txnAmount) - Double.valueOf(initTxnDTO.txnAmountFromBody())))
        .AssertAll();

    softAssert.assertAll();
  }

  @Test(description = "Verify end-to-end UPI payment flow with only platform fees UPI PPI Wallet")
  public void testCase_17() {
    SoftAssertions softAssert = new SoftAssertions();
    FF4JFlags.enable("theia.upipsp.enable.PPIWallet");
    redisHelper.delete("FF4J_FEATURE_theia.upipsp.enable.PPIWallet");
    FF4JFlags.enable("merchant.status.passUpiSubPayModeContextInResponse");
    redisHelper.delete("FF4J_FEATURE_merchant.status.passUpiSubPayModeContextInResponse");


    InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, platformFeeMerchant)
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
    FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(platformFeeMerchant.getId(), orderId,
        fetchPaymentOptionsDTO);
    JsonPath fpoResponse = fetchPaymentOption.execute().jsonPath();
    softAssert.assertThat(fpoResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
    softAssert.assertThat(fpoResponse.getString("body.resultInfo.resultCode")).isEqualTo("0000");
    softAssert.assertAll();

    // 3. Get QR data and decode deeplink
    String base64QrCode = fpoResponse.getString("body.qrDetail.dataUrl");
    String deeplink = QRHelper.getDeeplinkFromQRImage(base64QrCode);
    System.out.println("Decoded deeplink: " + deeplink);

    HashMap<String,Double> expFee=convenienceFeeUpiIntentUI.calculateExpectedFees(Double.valueOf(initTxnDTO.txnAmountFromBody()),platformFeeMerchant);

    // Parse deeplink information
    Map<String, String> deeplinkInfo = parseDeeplinkInfo(deeplink);
    String payeeVpa = deeplinkInfo.get("payeeVpa");
    String amount = deeplinkInfo.get("amount");
    String pconfee = deeplinkInfo.get("pconfee");
    String tr = deeplinkInfo.get("tr");

    softAssert.assertThat(pconfee).isEqualTo(String.format("%.2f",expFee.get("expectedConvFeeAmtUpiPPI")));
    softAssert.assertThat(amount).isEqualTo(String.format("%.2f",Double.parseDouble(initTxnDTO.txnAmountFromBody())+expFee.get("expectedPlatformFeeAmt")));
    softAssert.assertAll();

    String paymentInstrument = "PPI_WALLET";
    String txnAmount;
    String payerPaymentInstrumentFee;

    if ("PPI_WALLET".equals(paymentInstrument)) {
      System.out.println("Updating amount for UPI PPI WALLET");
    }
    txnAmount = String.format("%.2f", Double.parseDouble(amount) + Double.parseDouble(pconfee));
    payerPaymentInstrumentFee = String.format("%.2f", Double.parseDouble(pconfee));

    StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(platformFeeMerchant.getId(), tr,
        txnAmount, payeeVpa, "paytmTest@ptys", paymentInstrument, payerPaymentInstrumentFee);
    builder.setPayerName("test");
    builder.setPayerPSP("Phonepe");
    builder.build();
    StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder);
    StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
    Response response = staticQrUpiPSP.execute();
    StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.as(StaticQrUpiPSPResponse.class);

    // Verify order/pay response
    softAssert.assertThat(staticQrUpiPSPResponse.getBody().getResultCode())
        .as("Result code mismatch")
        .isEqualToIgnoringCase("SUCCESS");
    softAssert.assertAll();
    String externalSerialNo = staticQrUpiPSPResponse.getBody().getExternalSerialNo();

    // 5. Hit callback API using UPISecureResponse
    UPISecureResponse secureResponse = new UPISecureResponse().buildRequest(
        "Abhishek Verma",
        "paytmTest@ptys",
        payeeVpa,
        "Punjab National Bank",
        "PUNB",
        externalSerialNo,
        "SUCCESS",
        String.format("%011d", (long) (Math.random() * 100000000000L)),
        txnAmount,
        "0",
        "Transaction is successful",
        "DEFERRED_SETTLEMENT",
        paymentInstrument
    );

    JsonPath callbackResponse = secureResponse.execute().jsonPath();
    softAssert.assertThat(callbackResponse.getString("body.resultCodeId"))
        .as("Result status mismatch")
        .isEqualToIgnoringCase("001");
    softAssert.assertThat(callbackResponse.getString("body.resultCode"))
        .as("Result code mismatch")
        .isEqualToIgnoringCase("SUCCESS");

    // Verify transaction status
    PGPHelpers.getTxnStatus(platformFeeMerchant.getId(), orderId)
        .validateStatus("TXN_SUCCESS")
        .validatePaymentMode("UPI")
        .validateGatewayName("PTYBLI")
        .validateTxnAmount(initTxnDTO.txnAmountFromBody())
        .validateUpiModeSubType("UPI_PPIWALLET")
        .validateChargeAmount(String.format("%.2f",
            Double.valueOf(txnAmount) - Double.valueOf(initTxnDTO.txnAmountFromBody())))
        .AssertAll();

    softAssert.assertAll();
  }

  @Test(description = "Verify end-to-end UPI payment flow with only platform fees for UPI CC")
  public void testCase_18() {
    SoftAssertions softAssert = new SoftAssertions();
    FF4JFlags.enable("merchant.status.passUpiSubPayModeContextInResponse");
    redisHelper.delete("FF4J_FEATURE_merchant.status.passUpiSubPayModeContextInResponse");
    // 1. Hit initiate txn API
    InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, platformFeeMerchant)
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
    FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(platformFeeMerchant.getId(), orderId,
        fetchPaymentOptionsDTO);
    JsonPath fpoResponse = fetchPaymentOption.execute().jsonPath();
    softAssert.assertThat(fpoResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
    softAssert.assertThat(fpoResponse.getString("body.resultInfo.resultCode")).isEqualTo("0000");
    softAssert.assertAll();

    // 3. Get QR data and decode deeplink
    String base64QrCode = fpoResponse.getString("body.qrDetail.dataUrl");
    String deeplink = QRHelper.getDeeplinkFromQRImage(base64QrCode);
    System.out.println("Decoded deeplink: " + deeplink);

    HashMap<String,Double> expFee=convenienceFeeUpiIntentUI.calculateExpectedFees(Double.valueOf(initTxnDTO.txnAmountFromBody()),platformFeeMerchant);

    // Parse deeplink information
    Map<String, String> deeplinkInfo = parseDeeplinkInfo(deeplink);
    String payeeVpa = deeplinkInfo.get("payeeVpa");
    String amount = deeplinkInfo.get("amount");
    String cconfee = deeplinkInfo.get("cconfee");
    String tr = deeplinkInfo.get("tr");
    softAssert.assertThat(cconfee).isEqualTo(String.format("%.2f",expFee.get("expectedConvFeeAmtUpiCC")));
    softAssert.assertThat(amount).isEqualTo(String.format("%.2f",Double.parseDouble(initTxnDTO.txnAmountFromBody())+expFee.get("expectedPlatformFeeAmt")));
    softAssert.assertAll();

    String paymentInstrument = "UPI_CREDIT_CARD";
    String txnAmount;
    String payerPaymentInstrumentFee = "0.00";

    txnAmount = String.format("%.2f", Double.parseDouble(amount) + Double.parseDouble(cconfee));
    payerPaymentInstrumentFee = String.format("%.2f", Double.parseDouble(cconfee));

    StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(platformFeeMerchant.getId(), tr,
        txnAmount, payeeVpa, "paytmTest@ptys", paymentInstrument, payerPaymentInstrumentFee);
    builder.setPayerName("test");
    builder.setPayerPSP("Phonepe");
    builder.build();
    StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder);
    StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
    Response response = staticQrUpiPSP.execute();
    StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.as(StaticQrUpiPSPResponse.class);

    // Verify order/pay response
    softAssert.assertThat(staticQrUpiPSPResponse.getBody().getResultCode())
        .as("Result code mismatch")
        .isEqualToIgnoringCase("SUCCESS");
    softAssert.assertAll();
    String externalSerialNo = staticQrUpiPSPResponse.getBody().getExternalSerialNo();

    // 5. Hit callback API using UPISecureResponse with creditCardInfo
    UPISecureResponse secureResponse = new UPISecureResponse().buildRequest(
        "Abhishek Verma",
        "paytmTest@ptys",
        payeeVpa,
        "Punjab National Bank",
        "PUNB",
        externalSerialNo,
        "SUCCESS",
        String.format("%011d", (long) (Math.random() * 100000000000L)),
        txnAmount,
        "0",
        "Transaction is successful",
        "DEFERRED_SETTLEMENT",
        paymentInstrument
    );

    JsonPath callbackResponse = secureResponse.execute().jsonPath();
    softAssert.assertThat(callbackResponse.getString("body.resultCodeId"))
        .as("Result status mismatch")
        .isEqualToIgnoringCase("001");
    softAssert.assertThat(callbackResponse.getString("body.resultCode"))
        .as("Result code mismatch")
        .isEqualToIgnoringCase("SUCCESS");

    // Verify transaction status
    PGPHelpers.getTxnStatus(platformFeeMerchant.getId(), orderId)
        .validateStatus("TXN_SUCCESS")
        .validatePaymentMode("UPI")
        .validateGatewayName("PTYBLI")
        .validateTxnAmount(initTxnDTO.txnAmountFromBody())
        .validateUpiModeSubType("UPI_CREDIT_CARD")
        .validateChargeAmount(String.format("%.2f",
            Double.valueOf(txnAmount) - Double.valueOf(initTxnDTO.txnAmountFromBody())))
        .AssertAll();

    softAssert.assertAll();
  }

  @Test(description = "Verify end-to-end UPI payment flow with only platform fees for UPI CREDIT LINE")
  public void testCase_19() {
    SoftAssertions softAssert = new SoftAssertions();
    FF4JFlags.enable("merchant.status.passUpiSubPayModeContextInResponse");
    redisHelper.delete("FF4J_FEATURE_merchant.status.passUpiSubPayModeContextInResponse");
    // 1. Hit initiate txn API

    InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, platformFeeMerchant)
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
    FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(platformFeeMerchant.getId(), orderId,
        fetchPaymentOptionsDTO);
    JsonPath fpoResponse = fetchPaymentOption.execute().jsonPath();
    softAssert.assertThat(fpoResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
    softAssert.assertThat(fpoResponse.getString("body.resultInfo.resultCode")).isEqualTo("0000");
    softAssert.assertAll();

    // 3. Get QR data and decode deeplink
    String base64QrCode = fpoResponse.getString("body.qrDetail.dataUrl");
    String deeplink = QRHelper.getDeeplinkFromQRImage(base64QrCode);
    System.out.println("Decoded deeplink: " + deeplink);

    HashMap<String,Double> expFee=convenienceFeeUpiIntentUI.calculateExpectedFees(Double.valueOf(initTxnDTO.txnAmountFromBody()),platformFeeMerchant);

    // Parse deeplink information
    Map<String, String> deeplinkInfo = parseDeeplinkInfo(deeplink);
    String payeeVpa = deeplinkInfo.get("payeeVpa");
    String amount = deeplinkInfo.get("amount");
    String cconfee = deeplinkInfo.get("cconfee");
    String tr = deeplinkInfo.get("tr");

    softAssert.assertThat(cconfee).isEqualTo(String.format("%.2f",expFee.get("expectedConvFeeAmtUpiCL")));
    softAssert.assertThat(amount).isEqualTo(String.format("%.2f",Double.parseDouble(initTxnDTO.txnAmountFromBody())+expFee.get("expectedPlatformFeeAmt")));
    softAssert.assertAll();

    String paymentInstrument = "CREDITLINE_CREDITLINE01";
    String txnAmount;
    String payerPaymentInstrumentFee = "0.00";

    txnAmount = String.format("%.2f", Double.parseDouble(amount) + Double.parseDouble(cconfee));
    payerPaymentInstrumentFee = String.format("%.2f", Double.parseDouble(cconfee));

    StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(platformFeeMerchant.getId(), tr,
        txnAmount, payeeVpa, "paytmTest@ptys", paymentInstrument, payerPaymentInstrumentFee);
    builder.setPayerName("test");
    builder.setPayerPSP("Phonepe");
    builder.build();
    StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder);
    StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
    Response response = staticQrUpiPSP.execute();
    StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.as(StaticQrUpiPSPResponse.class);

    // Verify order/pay response
    softAssert.assertThat(staticQrUpiPSPResponse.getBody().getResultCode())
        .as("Result code mismatch")
        .isEqualToIgnoringCase("SUCCESS");
    softAssert.assertAll();
    String externalSerialNo = staticQrUpiPSPResponse.getBody().getExternalSerialNo();

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
        txnAmount,
        "0",
        "Transaction is successful",
        "DEFERRED_SETTLEMENT",
        paymentInstrument
    );

    JsonPath callbackResponse = secureResponse.execute().jsonPath();
    softAssert.assertThat(callbackResponse.getString("body.resultCodeId"))
        .as("Result status mismatch")
        .isEqualToIgnoringCase("001");
    softAssert.assertThat(callbackResponse.getString("body.resultCode"))
        .as("Result code mismatch")
        .isEqualToIgnoringCase("SUCCESS");

    // Verify transaction status
    PGPHelpers.getTxnStatus(platformFeeMerchant.getId(), orderId)
        .validateStatus("TXN_SUCCESS")
        .validatePaymentMode("UPI")
        .validateGatewayName("PTYBLI")
        .validateTxnAmount(initTxnDTO.txnAmountFromBody())
        .validateUpiModeSubType("UPI_CREDITLINE")
        .validateChargeAmount(String.format("%.2f",
            Double.valueOf(txnAmount) - Double.valueOf(initTxnDTO.txnAmountFromBody())))
        .AssertAll();

    softAssert.assertAll();
  }

  @Test(description = "Verify platform fee in added with amount in am tag if present on merchant")
  public void testCase_20() {
    SoftAssertions softAssert = new SoftAssertions();

    // 1. Hit initiate txn API
    InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, convFeeAndPlatformFeeMerchant)
        .setTxnValue("500.00")
        .build();
    InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
    String txnToken = initTxnResponse.getBody().getTxnToken();
    String orderId = initTxnDTO.getBody().getOrderId();

    // 2. Hit fetchPaymentOptions API
    FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken)
        .setGenerateOrderId("false")
        .setDeepLinkRequiedField(true)
        .setWorkFlow("checkout")
        .build();
    FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(
        convFeeAndPlatformFeeMerchant.getId(), orderId, fetchPaymentOptionsDTO);
    JsonPath fpoResponse = fetchPaymentOption.execute().jsonPath();

    // 3. Get QR data and decode deeplink
    String base64QrCode = fpoResponse.getString("body.qrDetail.dataUrl");
    String deeplink = QRHelper.getDeeplinkFromQRImage(base64QrCode);
    System.out.println("Decoded deeplink: " + deeplink);

    HashMap<String,Double> expFee=convenienceFeeUpiIntentUI.calculateExpectedFees(Double.valueOf(initTxnDTO.txnAmountFromBody()),convFeeAndPlatformFeeMerchant);

    // Parse deeplink information
    Map<String, String> deeplinkInfo = parseDeeplinkInfo(deeplink);
    String amount = deeplinkInfo.get("amount");

    // Verify amount includes platform fee
    double expectedAmount = Double.parseDouble(initTxnDTO.txnAmountFromBody()) + expFee.get("expectedPlatformFeeAmt");
    softAssert.assertThat(Double.parseDouble(amount))
        .as("Amount should include platform fee")
        .isEqualTo(expectedAmount);

    softAssert.assertAll();
  }

  @Test(description = "Verify CCONFEE and PCONFEE tag is added in deeplink if ff4j is ON")
  public void testCase_21() {
    SoftAssertions softAssert = new SoftAssertions();

    // 1. Hit initiate txn API
    InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, convFeeAndPlatformFeeMerchant)
        .setTxnValue("500.00")
        .build();
    InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
    String txnToken = initTxnResponse.getBody().getTxnToken();
    String orderId = initTxnDTO.getBody().getOrderId();

    // 2. Hit fetchPaymentOptions API
    FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken)
        .setGenerateOrderId("false")
        .setDeepLinkRequiedField(true)
        .setWorkFlow("checkout")
        .build();
    FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(
        convFeeAndPlatformFeeMerchant.getId(), orderId, fetchPaymentOptionsDTO);
    JsonPath fpoResponse = fetchPaymentOption.execute().jsonPath();

    // 3. Get QR data and decode deeplink
    String base64QrCode = fpoResponse.getString("body.qrDetail.dataUrl");
    String deeplink = QRHelper.getDeeplinkFromQRImage(base64QrCode);
    System.out.println("Decoded deeplink: " + deeplink);

    // Verify CCONFEE and PCONFEE tags are present
    softAssert.assertThat(deeplink)
        .as("Deeplink should contain CCONFEE tag")
        .contains("CCONFEE:");
    softAssert.assertThat(deeplink)
        .as("Deeplink should contain PCONFEE tag")
        .contains("PCONFEE:");

    softAssert.assertAll();
  }

  @Test(description = "Verify CCONFEE and PCONFEE value is going in deeplink if present on merchant")
  public void testCase_22() {
    SoftAssertions softAssert = new SoftAssertions();

    // 1. Hit initiate txn API
    InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, convFeeAndPlatformFeeMerchant)
        .setTxnValue("500.00")
        .build();
    InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
    String txnToken = initTxnResponse.getBody().getTxnToken();
    String orderId = initTxnDTO.getBody().getOrderId();

    // 2. Hit fetchPaymentOptions API
    FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken)
        .setGenerateOrderId("false")
        .setDeepLinkRequiedField(true)
        .setWorkFlow("checkout")
        .build();
    FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(
        convFeeAndPlatformFeeMerchant.getId(), orderId, fetchPaymentOptionsDTO);
    JsonPath fpoResponse = fetchPaymentOption.execute().jsonPath();

    // 3. Get QR data and decode deeplink
    String base64QrCode = fpoResponse.getString("body.qrDetail.dataUrl");
    String deeplink = QRHelper.getDeeplinkFromQRImage(base64QrCode);
    System.out.println("Decoded deeplink: " + deeplink);

    HashMap<String,Double> expFee=convenienceFeeUpiIntentUI.calculateExpectedFees(Double.valueOf(initTxnDTO.txnAmountFromBody()),convFeeAndPlatformFeeMerchant);

    // Parse deeplink information
    Map<String, String> deeplinkInfo = parseDeeplinkInfo(deeplink);
    String cconfee = deeplinkInfo.get("cconfee");
    String pconfee = deeplinkInfo.get("pconfee");

    // Verify CCONFEE and PCONFEE values
    softAssert.assertThat(cconfee)
        .as("CCONFEE should match expected value")
        .isEqualTo(String.format("%.2f",expFee.get("expectedConvFeeAmtUpiCC")));
    softAssert.assertThat(pconfee)
        .as("PCONFEE should match expected value")
        .isEqualTo(String.format("%.2f",expFee.get("expectedConvFeeAmtUpiPPI")));

    softAssert.assertAll();
  }

  @Test(description = "Verify value in CCONFEE is mapped from UPI CC commission not UPI CREDITLINE commission")
  public void testCase_23() {
    SoftAssertions softAssert = new SoftAssertions();

    // 1. Hit initiate txn API
    InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, convFeeAndPlatformFeeMerchant)
        .setTxnValue("500.00")
        .build();
    InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
    String txnToken = initTxnResponse.getBody().getTxnToken();
    String orderId = initTxnDTO.getBody().getOrderId();

    // 2. Hit fetchPaymentOptions API with UPI_CREDIT_CARD subtype
    List<PayMethod> payMethods = new ArrayList<>();
    PayMethod upiPayMethod = new PayMethod()
        .setPayMethod("UPI");
    upiPayMethod.setAdditionalProperty("upiModeSubTypes", Arrays.asList("UPI_CREDIT_CARD"));
    upiPayMethod.setAdditionalProperty("payOption", "UPI_INTENT");
    payMethods.add(upiPayMethod);

    FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken)
        .setGenerateOrderId("false")
        .setDeepLinkRequiedField(true)
        .setWorkFlow("checkout")
        .build();
    FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(
        convFeeAndPlatformFeeMerchant.getId(), orderId, fetchPaymentOptionsDTO);
    JsonPath fpoResponse = fetchPaymentOption.execute().jsonPath();

    // 3. Get QR data and decode deeplink
    String base64QrCode = fpoResponse.getString("body.qrDetail.dataUrl");
    String deeplink = QRHelper.getDeeplinkFromQRImage(base64QrCode);
    System.out.println("Decoded deeplink: " + deeplink);

    HashMap<String,Double> expFee=convenienceFeeUpiIntentUI.calculateExpectedFees(Double.valueOf(initTxnDTO.txnAmountFromBody()),convFeeAndPlatformFeeMerchant);

    // Parse deeplink information
    Map<String, String> deeplinkInfo = parseDeeplinkInfo(deeplink);
    String cconfee = deeplinkInfo.get("cconfee");

    // Verify CCONFEE value matches UPI CC commission
    softAssert.assertThat(cconfee)
        .as("CCONFEE should match UPI CC commission")
        .isEqualTo(String.format("%.2f",expFee.get("expectedConvFeeAmtUpiCC")));

    softAssert.assertAll();
  }

  @Test(description = "Verify no platform fee is added in am tag if platform fee is not present")
  public void testCase_24() {
    SoftAssertions softAssert = new SoftAssertions();

    // 1. Hit initiate txn API
    InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, convFeeMerchant)
        .setTxnValue("500.00")
        .build();
    InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
    String txnToken = initTxnResponse.getBody().getTxnToken();
    String orderId = initTxnDTO.getBody().getOrderId();

    // 2. Hit fetchPaymentOptions API
    FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken)
        .setGenerateOrderId("false")
        .setDeepLinkRequiedField(true)
        .setWorkFlow("checkout")
        .build();
    FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(convFeeMerchant.getId(), orderId,
        fetchPaymentOptionsDTO);
    JsonPath fpoResponse = fetchPaymentOption.execute().jsonPath();

    // 3. Get QR data and decode deeplink
    String base64QrCode = fpoResponse.getString("body.qrDetail.dataUrl");
    String deeplink = QRHelper.getDeeplinkFromQRImage(base64QrCode);
    System.out.println("Decoded deeplink: " + deeplink);

    HashMap<String,Double> expFee=convenienceFeeUpiIntentUI.calculateExpectedFees(Double.valueOf(initTxnDTO.txnAmountFromBody()),convFeeMerchant);

    // Parse deeplink information
    Map<String, String> deeplinkInfo = parseDeeplinkInfo(deeplink);
    String amount = deeplinkInfo.get("amount");

    // Verify amount does not include platform fee (should only include platform fee which is 0 for convFeeMerchant)
    double expectedAmount = Double.parseDouble(initTxnDTO.txnAmountFromBody()) + expFee.get("expectedPlatformFeeAmt");
    softAssert.assertThat(Double.parseDouble(amount))
        .as("Amount should not include platform fee")
        .isEqualTo(expectedAmount);

    softAssert.assertAll();
  }

  @Test(description = "Verify CCONFEE and PCONFEE is coming as 0 when fee is not configured on merchant")
  public void testCase_25() {
    SoftAssertions softAssert = new SoftAssertions();

    // 1. Hit initiate txn API
    InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, convFeeMerchant)
        .setTxnValue("500.00")
        .build();
    InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
    String txnToken = initTxnResponse.getBody().getTxnToken();
    String orderId = initTxnDTO.getBody().getOrderId();

    // 2. Hit fetchPaymentOptions API
    FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken)
        .setGenerateOrderId("false")
        .setDeepLinkRequiedField(true)
        .setWorkFlow("checkout")
        .build();
    FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(convFeeMerchant.getId(), orderId,
        fetchPaymentOptionsDTO);
    JsonPath fpoResponse = fetchPaymentOption.execute().jsonPath();

    // 3. Get QR data and decode deeplink
    String base64QrCode = fpoResponse.getString("body.qrDetail.dataUrl");
    String deeplink = QRHelper.getDeeplinkFromQRImage(base64QrCode);
    System.out.println("Decoded deeplink: " + deeplink);

    HashMap<String,Double> expFee=convenienceFeeUpiIntentUI.calculateExpectedFees(Double.valueOf(initTxnDTO.txnAmountFromBody()),convFeeMerchant);

    // Parse deeplink information
    Map<String, String> deeplinkInfo = parseDeeplinkInfo(deeplink);
    String cconfee = deeplinkInfo.get("cconfee");
    String pconfee = deeplinkInfo.get("pconfee");

    // Verify CCONFEE and PCONFEE values are 0 when platform fee is not configured
    softAssert.assertThat(cconfee)
        .as("CCONFEE should match expected value")
        .isEqualTo(String.format("%.2f",expFee.get("expectedConvFeeAmtUpiCC")));
    softAssert.assertThat(pconfee)
        .as("PCONFEE should match expected value")
        .isEqualTo(String.format("%.2f",expFee.get("expectedConvFeeAmtUpiPPI")));

    softAssert.assertAll();
  }
}
