package scripts.UPI.OnlineUpi.AxisUpiOnline;

import com.paytm.LocalConfig;
import com.paytm.api.AxisIntentCallBack;
import com.paytm.api.CloseOrderV2Api;
import com.paytm.api.TxnStatus;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.appconstants.Constants;
import com.paytm.appconstants.Constants.AxisVPAs;
import com.paytm.appconstants.Constants.MerchantType;
import com.paytm.appconstants.Constants.NativeAPIResourcePath;
import com.paytm.appconstants.Constants.ResponseCode;
import com.paytm.appconstants.Constants.TXNSTATUS;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.dto.processTransactionV1.response.ProcessTxnV1Response;
import com.paytm.pages.ResponsePage;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import org.apache.commons.lang.RandomStringUtils;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.testng.annotations.Test;


public class AxisUpiOnlineTransactionsTest extends PGPBaseTest {

  public static final String intentCallbackUrl=LocalConfig.PGP_HOST+ NativeAPIResourcePath.AXIU_INSTA_CALLBACK_URL;
  Constants.MerchantType axisMerchant = MerchantType.ISSUER_TOKEN_3P;
  String custId = RandomStringUtils.randomAlphabetic(10) + Instant.now().toEpochMilli();

  @Feature("PGP-54059/PGP-54058 ")
  @Owner(Constants.Owner.ABHISHEK_VERMA)
  @Test(description = "Verify e2e success upi collect transaction with Axis bank")
  public void e2eSuccessAxisUpiTransaction() throws InterruptedException {
    InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null,axisMerchant)
        .setCustId(custId)
        .setTxnValue("2.00")
        .build();
    InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);

    ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
        .Builder(axisMerchant.getId(), initTxnResponse.getBody().getTxnToken(), initTxnDTO.orderFromBody())
        .setPaymentMode("UPI")
        .setPayerAccount(AxisVPAs.validVpa)
        .setAuthMode("USRPWD")
        .build();

    ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
    Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
    Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
    Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

    NativeHelpers.submitJsonFormInBrowser(processTxnV1Response.toString());

    ResponsePage responsePage = new ResponsePage();
    responsePage.waitUntilLoads();
    responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
        .validateCurrency("INR")
        .validateMid(axisMerchant.getId())
        .validateOrderId(initTxnDTO.orderFromBody())
        .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
        .validateRespMsg(Constants.ResponseCode.TXN_SUCCESS.getRespMsg())
        .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
        .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
        .validateTxnDate(new Date())
        .validateTxnId(Constants.ValidationType.NON_EMPTY)
        .validateGatewayName("AXIS")
        .assertAll();

    TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
    txnStatus.executeUntilNotPending();
    Assertions.assertThat(txnStatus.txnStatusResponse.TXNID.contains("0000")).isTrue();
    txnStatus.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
        .validateOrderid(initTxnDTO.getBody().getOrderId())
        .validateTxnAmount(initTxnDTO.txnAmountFromBody())
        .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
        .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
        .validateRespMsg(Constants.ResponseCode.TXN_SUCCESS.getRespMsg())
        .validatePaymentMode("UPI")
        .validateMid(axisMerchant.getId())
        .validateGatewayName("AXIS")
        .AssertAll();
  }

  @Feature("PGP-54059/PGP-54058 ")
  @Owner(Constants.Owner.ABHISHEK_VERMA)
  @Test(description = "Verify e2e failed upi collect transaction with Axis bank")
  public void e2eFailedAxisUpiTransaction() throws InterruptedException {
    InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null,axisMerchant)
        .setCustId(custId)
        .setTxnValue("99.60")
        .build();
    InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);

    ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
        .Builder(axisMerchant.getId(), initTxnResponse.getBody().getTxnToken(), initTxnDTO.orderFromBody())
        .setPaymentMode("UPI")
        .setPayerAccount(AxisVPAs.validVpa)
        .setAuthMode("USRPWD")
        .build();

    ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
    Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
    Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
    Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

    NativeHelpers.submitJsonFormInBrowser(processTxnV1Response.toString());

    ResponsePage responsePage = new ResponsePage();
    responsePage.waitUntilLoads();
    responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
        .validateCurrency("INR")
        .validateMid(axisMerchant.getId())
        .validatePaymentMode("UPI")
        .validateOrderId(initTxnDTO.orderFromBody())
        .validateRespCode(ResponseCode.PAYMENT_DECLINED_BY_BANK.getRespCode())
        .validateRespMsg(ResponseCode.PAYMENT_DECLINED_BY_BANK.getRespMsg())
        .validateStatus(Constants.TXNSTATUS.TXN_FAILURE.toString())
        .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
        .validateTxnDate(new Date())
        .validateTxnId(Constants.ValidationType.NON_EMPTY)
        .validateGatewayName("AXIS")
        .assertAll();

    TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
    txnStatus.executeUntilNotPending();
    Assertions.assertThat(txnStatus.txnStatusResponse.TXNID.contains("0000")).isTrue();
    txnStatus.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
        .validateOrderid(initTxnDTO.getBody().getOrderId())
        .validateTxnAmount(initTxnDTO.txnAmountFromBody())
        .validateStatus(TXNSTATUS.TXN_FAILURE.toString())
        .validateRespCode(ResponseCode.PAYMENT_DECLINED_BY_BANK.getRespCode())
        .validateRespMsg(ResponseCode.PAYMENT_DECLINED_BY_BANK.getRespMsg())
        .validatePaymentMode("UPI")
        .validateMid(axisMerchant.getId())
        .validateGatewayName("AXIS")
        .AssertAll();
  }

  @Feature("PGP-54059/PGP-54058 ")
  @Owner(Constants.Owner.ABHISHEK_VERMA)
  @Test(description = "Verify e2e online intent transaction for Axis bank")
  public void axisOnlineIntentSuccessTransaction() throws Exception {
    InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, axisMerchant)
        .setRequestType("NATIVE")
        .setTxnValue("2.0")
        .build();
    InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);

    ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
        .Builder(axisMerchant.getId(),initTxnResponse.getBody().getTxnToken() , initTxnDTO.orderFromBody())
        .setPaymentMode("UPI_INTENT")
        .build();
    ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
    Map<String, String> map = PGPHelpers.parseUpiIntentDeepLink(processTxnV1Response.getBody().getDeepLinkInfo().getDeepLink());

    AxisIntentCallBack axisIntentCallBack= new AxisIntentCallBack();
    axisIntentCallBack.buildRequest(axisMerchant.getId(), intentCallbackUrl,map.get("pa"),map.get("tr"),map.get("am"));
    JsonPath axisIntentCallbackResponse = axisIntentCallBack.execute().jsonPath();
    SoftAssertions assertions= new SoftAssertions();
    assertions.assertThat(axisIntentCallbackResponse.getString("orderStatus")).isEqualToIgnoringCase("Success");
    assertions.assertAll();

    PGPHelpers.getTxnStatus(axisMerchant.getId(), initTxnDTO.orderFromBody())
        .validateStatus("TXN_SUCCESS")
        .validatePaymentMode("UPI")
        .validateGatewayName("AXIU")
        .AssertAll();
  }

  @Feature("PGP-54059/PGP-54058 ")
  @Owner(Constants.Owner.ABHISHEK_VERMA)
  @Test(description = "Verify e2e online failed intent transaction for Axis bank")
  public void axisOnlineIntentFailedTransaction() throws Exception {
    InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, axisMerchant)
        .setRequestType("NATIVE")
        .setTxnValue("99")
        .build();
    InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);

    ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
        .Builder(axisMerchant.getId(),initTxnResponse.getBody().getTxnToken() , initTxnDTO.orderFromBody())
        .setPaymentMode("UPI_INTENT")
        .build();
    ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
    Map<String, String> map = PGPHelpers.parseUpiIntentDeepLink(processTxnV1Response.getBody().getDeepLinkInfo().getDeepLink());

    AxisIntentCallBack axisIntentCallBack= new AxisIntentCallBack();
    axisIntentCallBack.buildRequest(axisMerchant.getId(), intentCallbackUrl,map.get("pa"),map.get("tr"),map.get("am"));
    JsonPath axisIntentCallbackResponse = axisIntentCallBack.execute().jsonPath();
    SoftAssertions assertions= new SoftAssertions();
    assertions.assertThat(axisIntentCallbackResponse.getString("orderStatus")).isEqualToIgnoringCase("Success");
    assertions.assertAll();

    CloseOrderV2Api closeOrderV2Api = new CloseOrderV2Api("TXN_TOKEN", axisMerchant.getId(), initTxnDTO.getBody().getOrderId(), initTxnResponse.getBody().getTxnToken(), "true");
    Response closeOrderResponse = closeOrderV2Api.execute();
    Assertions.assertThat(closeOrderResponse.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("SUCCESS");
    Assertions.assertThat(closeOrderResponse.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("S");
    assertions.assertAll();

    PGPHelpers.getTxnStatus(axisMerchant.getId(), initTxnDTO.orderFromBody())
        .validateStatus("TXN_FAILURE")
        .validatePaymentMode("UPI")
        .validateGatewayName("AXIU")
        .AssertAll();
  }
}
