package scripts.UPI.OnlineUpi;

import com.paytm.LocalConfig;
import com.paytm.api.Deals.GetPaymentStatus;
import com.paytm.api.PTYBLIIntentCallback;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.api.theia.Handlerinternaltxnstatus;
import com.paytm.appconstants.Constants;
import com.paytm.appconstants.Constants.AxisVPAs;
import com.paytm.appconstants.Constants.MerchantType;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.dto.processTransactionV1.response.ProcessTxnV1Response;
import com.paytm.pages.ResponsePage;
import com.paytm.pg.crypto.AesEncryption;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import java.util.Date;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.testng.annotations.Test;

public class PayerAndPayeeVpaInMSResponse extends PGPBaseTest {

  MerchantType merchantWithPrefOn = MerchantType.ALLOWED_TPAP_MERCHANT_PPSL;
  MerchantType merchantWithPrefOff = MerchantType.CALLBACK_NOT_IN_THEIA_ORDERPAY;

  @Owner(Constants.Owner.ABHISHEK_VERMA)
  @Feature("PGP-57943")
  @Test(description = "Verify payerVpa and payeeVpa are returned in MS response when pref UPI_PAYER_VPA is ON")
  public void test01() throws Exception {
    User user = userManager.getForWrite(Label.BASIC);
    InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantWithPrefOn)
        .setTxnValue("2.00")
        .setSsoToken(user.ssoToken())
        .build();
    InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);

    ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
        .Builder(merchantWithPrefOn.getId(), initTxnResponse.getBody().getTxnToken(),
        initTxnDTO.orderFromBody())
        .setPaymentMode("UPI")
        .setPayerAccount(AxisVPAs.validVpa)
        .setAuthMode("USRPWD")
        .build();

    ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(
        processTxnV1Request);
    Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultStatus())
        .isEqualTo("S");
    Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultCode())
        .isEqualTo("0000");
    Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultMsg())
        .isEqualTo("Success");

    NativeHelpers.submitJsonFormInBrowser(processTxnV1Response.toString());

    ResponsePage responsePage = new ResponsePage();
    responsePage.waitUntilLoads();
    responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
        .validateCurrency("INR")
        .validateMid(merchantWithPrefOn.getId())
        .validateOrderId(initTxnDTO.orderFromBody())
        .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
        .validateRespMsg(Constants.ResponseCode.TXN_SUCCESS.getRespMsg())
        .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
        .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(
            initTxnDTO.txnAmountFromBody()))
        .validateTxnDate(new Date())
        .validateTxnId(Constants.ValidationType.NON_EMPTY)
        .validateGatewayName("PTYBLC")
        .assertAll();

    GetPaymentStatus getPaymentStatus = new GetPaymentStatus().buildWithParameters(
        merchantWithPrefOn.getId(),
        initTxnDTO.orderFromBody());
    JsonPath paymentStatusResponse = getPaymentStatus.execute().jsonPath();
    AesEncryption aesEncryption= new AesEncryption();
    String decryptedAdditionalInfo=aesEncryption.decrypt(paymentStatusResponse.getString("body.encAdditionalInfo"), merchantWithPrefOn.getKey());
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(decryptedAdditionalInfo).contains("payerName");
    softly.assertThat(decryptedAdditionalInfo).contains("GAGAN DEEP SINGH");
    softly.assertThat(decryptedAdditionalInfo).contains("payerVpa");
    softly.assertThat(decryptedAdditionalInfo).contains(AxisVPAs.validVpa);
    softly.assertThat(paymentStatusResponse.getString("body.resultInfo.resultStatus"))
        .isEqualToIgnoringCase("TXN_SUCCESS");
    softly.assertAll();

    Handlerinternaltxnstatus txnStatus = new Handlerinternaltxnstatus(merchantWithPrefOn,initTxnDTO.orderFromBody(),user.ssoToken());
    JsonPath jsonpath=txnStatus.execute().jsonPath();
    String decryptedAdditionalInfo2=aesEncryption.decrypt(jsonpath.getString("body.ENCADDITIONALINFO"), merchantWithPrefOn.getKey());
    softly.assertThat(decryptedAdditionalInfo2).contains("payerName");
    softly.assertThat(decryptedAdditionalInfo2).contains("GAGAN DEEP SINGH");
    softly.assertThat(decryptedAdditionalInfo2).contains("payerVpa");
    softly.assertThat(decryptedAdditionalInfo2).contains(AxisVPAs.validVpa);
    softly.assertThat(jsonpath.getString("body.STATUS"))
        .isEqualToIgnoringCase("TXN_SUCCESS");
    softly.assertAll();
  }

  @Owner(Constants.Owner.ABHISHEK_VERMA)
  @Feature("PGP-57943")
  @Test(description = "Verify payerVpa and payeeVpa are returned in MS response when pref UPI_PAYER_VPA is ON for upi Intent ")
  public void test02() throws Exception {
    User user = userManager.getForWrite(Label.BASIC);
    InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantWithPrefOn)
        .setRequestType("NATIVE")
        .setTxnValue("2.0")
        .build();
    InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);

    ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
        .Builder(merchantWithPrefOn.getId(),initTxnResponse.getBody().getTxnToken() , initTxnDTO.orderFromBody())
        .setPaymentMode("UPI_INTENT")
        .build();
    ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
    Map<String, String> map = PGPHelpers.parseUpiIntentDeepLink(processTxnV1Response.getBody().getDeepLinkInfo().getDeepLink());

    PTYBLIIntentCallback ptybliIntentCallback= new PTYBLIIntentCallback();
    ptybliIntentCallback.buildRequest("GAGAN DEEP SINGH", LocalConfig.PGP_HOST,map.get("pa"),map.get("tr"),AxisVPAs.validVpa);
    JsonPath ptybliCallbackResponse = ptybliIntentCallback.execute().jsonPath();
    SoftAssertions assertions= new SoftAssertions();
    assertions.assertThat(ptybliCallbackResponse.getString("body.resultCode")).isEqualToIgnoringCase("SUCCESS");
    assertions.assertAll();

    GetPaymentStatus getPaymentStatus = new GetPaymentStatus().buildWithParameters(
        merchantWithPrefOn.getId(),
        initTxnDTO.orderFromBody());
    JsonPath paymentStatusResponse = getPaymentStatus.executeUntilExpectedConditionMet("body.resultInfo.resultStatus","TXN_SUCCESS",5,12).jsonPath();
    AesEncryption aesEncryption= new AesEncryption();
    String decryptedAdditionalInfo=aesEncryption.decrypt(paymentStatusResponse.getString("body.encAdditionalInfo"), merchantWithPrefOn.getKey());
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(decryptedAdditionalInfo).contains("payerName");
    softly.assertThat(decryptedAdditionalInfo).contains("GAGAN DEEP SINGH");
    softly.assertThat(decryptedAdditionalInfo).contains("payerVpa");
    softly.assertThat(decryptedAdditionalInfo).contains(AxisVPAs.validVpa);
    softly.assertThat(paymentStatusResponse.getString("body.resultInfo.resultStatus"))
        .isEqualToIgnoringCase("TXN_SUCCESS");
    softly.assertAll();

    Handlerinternaltxnstatus txnStatus = new Handlerinternaltxnstatus(merchantWithPrefOn,initTxnDTO.orderFromBody(),user.ssoToken());
    JsonPath jsonpath=txnStatus.execute().jsonPath();
    String decryptedAdditionalInfo2=aesEncryption.decrypt(jsonpath.getString("body.ENCADDITIONALINFO"), merchantWithPrefOn.getKey());
    softly.assertThat(decryptedAdditionalInfo2).contains("payerName");
    softly.assertThat(decryptedAdditionalInfo2).contains("GAGAN DEEP SINGH");
    softly.assertThat(decryptedAdditionalInfo2).contains("payerVpa");
    softly.assertThat(decryptedAdditionalInfo2).contains(AxisVPAs.validVpa);
    softly.assertThat(jsonpath.getString("body.STATUS"))
        .isEqualToIgnoringCase("TXN_SUCCESS");
    softly.assertThat(jsonpath.getString("body.GATEWAYNAME"))
        .isEqualToIgnoringCase("PTYBLI");
    softly.assertAll();
  }

  @Owner(Constants.Owner.ABHISHEK_VERMA)
  @Feature("PGP-57943")
  @Test(description = "Verify payerVpa and payeeVpa are not returned in MS response when pref UPI_PAYER_VPA is OFF")
  public void test03() throws Exception {
    User user = userManager.getForWrite(Label.BASIC);
    InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantWithPrefOff)
        .setTxnValue("2.00")
        .setSsoToken(user.ssoToken())
        .build();
    InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);

    ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
        .Builder(merchantWithPrefOff.getId(), initTxnResponse.getBody().getTxnToken(),
        initTxnDTO.orderFromBody())
        .setPaymentMode("UPI")
        .setPayerAccount(AxisVPAs.validVpa)
        .setAuthMode("USRPWD")
        .build();

    ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(
        processTxnV1Request);
    Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultStatus())
        .isEqualTo("S");
    Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultCode())
        .isEqualTo("0000");
    Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultMsg())
        .isEqualTo("Success");

    NativeHelpers.submitJsonFormInBrowser(processTxnV1Response.toString());

    ResponsePage responsePage = new ResponsePage();
    responsePage.waitUntilLoads();
    responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
        .validateCurrency("INR")
        .validateMid(merchantWithPrefOff.getId())
        .validateOrderId(initTxnDTO.orderFromBody())
        .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
        .validateRespMsg(Constants.ResponseCode.TXN_SUCCESS.getRespMsg())
        .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
        .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(
            initTxnDTO.txnAmountFromBody()))
        .validateTxnDate(new Date())
        .validateTxnId(Constants.ValidationType.NON_EMPTY)
        .validateGatewayName("PTYBLC")
        .assertAll();

    GetPaymentStatus getPaymentStatus = new GetPaymentStatus().buildWithParameters(
        merchantWithPrefOff.getId(),
        initTxnDTO.orderFromBody());
    JsonPath paymentStatusResponse = getPaymentStatus.execute().jsonPath();

    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(paymentStatusResponse.getString("body.encAdditionalInfo")).isNullOrEmpty();
    softly.assertThat(paymentStatusResponse.getString("body.resultInfo.resultStatus"))
        .isEqualToIgnoringCase("TXN_SUCCESS");
    softly.assertAll();

    Handlerinternaltxnstatus txnStatus = new Handlerinternaltxnstatus(merchantWithPrefOff,initTxnDTO.orderFromBody(),user.ssoToken());
    JsonPath jsonpath=txnStatus.execute().jsonPath();
    softly.assertThat(jsonpath.getString("body.ENCADDITIONALINFO")).isNullOrEmpty();
    softly.assertThat(jsonpath.getString("body.STATUS"))
        .isEqualToIgnoringCase("TXN_SUCCESS");
    softly.assertAll();
  }

  @Owner(Constants.Owner.ABHISHEK_VERMA)
  @Feature("PGP-57943")
  @Test(description = "Verify payerVpa and payeeVpa are returned in MS response when pref UPI_PAYER_VPA is ON for upi Intent ")
  public void test04() throws Exception {
    User user = userManager.getForWrite(Label.BASIC);
    InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantWithPrefOff)
        .setRequestType("NATIVE")
        .setTxnValue("2.0")
        .build();
    InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);

    ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
        .Builder(merchantWithPrefOff.getId(),initTxnResponse.getBody().getTxnToken() , initTxnDTO.orderFromBody())
        .setPaymentMode("UPI_INTENT")
        .build();
    ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
    Map<String, String> map = PGPHelpers.parseUpiIntentDeepLink(processTxnV1Response.getBody().getDeepLinkInfo().getDeepLink());

    PTYBLIIntentCallback ptybliIntentCallback= new PTYBLIIntentCallback();
    ptybliIntentCallback.buildRequest("GAGAN DEEP SINGH", LocalConfig.PGP_HOST,map.get("pa"),map.get("tr"),AxisVPAs.validVpa);
    JsonPath ptybliCallbackResponse = ptybliIntentCallback.execute().jsonPath();
    SoftAssertions assertions= new SoftAssertions();
    assertions.assertThat(ptybliCallbackResponse.getString("body.resultCode")).isEqualToIgnoringCase("SUCCESS");
    assertions.assertAll();

    GetPaymentStatus getPaymentStatus = new GetPaymentStatus().buildWithParameters(
        merchantWithPrefOff.getId(),
        initTxnDTO.orderFromBody());
    JsonPath paymentStatusResponse = getPaymentStatus.executeUntilExpectedConditionMet("body.resultInfo.resultStatus","TXN_SUCCESS",5,12).jsonPath();

    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(paymentStatusResponse.getString("body.encAdditionalInfo")).isNullOrEmpty();
    softly.assertThat(paymentStatusResponse.getString("body.resultInfo.resultStatus"))
        .isEqualToIgnoringCase("TXN_SUCCESS");
    softly.assertAll();

    Handlerinternaltxnstatus txnStatus = new Handlerinternaltxnstatus(merchantWithPrefOff,initTxnDTO.orderFromBody(),user.ssoToken());
    JsonPath jsonpath=txnStatus.execute().jsonPath();
    softly.assertThat(jsonpath.getString("body.ENCADDITIONALINFO")).isNullOrEmpty();
    softly.assertThat(jsonpath.getString("body.STATUS"))
        .isEqualToIgnoringCase("TXN_SUCCESS");
    softly.assertThat(jsonpath.getString("body.GATEWAYNAME"))
        .isEqualToIgnoringCase("PTYBLI");
    softly.assertAll();
  }
}