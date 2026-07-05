package scripts.api.UpiPsp;

import com.paytm.api.*;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.api.refund.SyncRefund;
import com.paytm.api.upipsp.UpiPspConsultFee;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.apphelpers.LogsValidationHelper;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.PG2LogsValidationHelper;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.CloseOrder.CloseOrderDTO;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.dto.processTransactionV1.response.ProcessTxnV1Response;
import com.paytm.dto.upiIntent.staticQR.Response.StaticQrUpiPSPResponse;
import com.paytm.dto.upiIntent.staticQR.StaticQrUpiPSPRequest;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.hamcrest.Matchers;
import org.json.JSONObject;
import org.testng.annotations.Test;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import java.util.Map;

import static com.paytm.apphelpers.QRHelper.parseDeeplinkInfo;
import static io.restassured.RestAssured.given;

public class UpiPspConsultFeeTest  extends PGPBaseTest {

    String successCallbackCreditLine = "10744326|{ESN}|{callbackAmount}|2025:09:11 05:47:32|SUCCESS|Transaction success|00|NA|9520345244@ptyes|382034275364|NA|null|null|null|null|null|MyCM!857994698322155!AABG0879548!919520345244|PAY!NA!NA!PTMa9217f5e068043348eb7d0520b48f1fa!NA!|NA!NA!NA|CREDITLINE!NA!NA!NA!NA|ABC!NA!NA!NA!NA ";
    String successCallbackSavings    = "10726101|{ESN}|{callbackAmount}|2025:09:10 06:42:00|SUCCESS|Transaction success|00|NA|9520345244@ptyes|382034275197|NA|null|null|null|null|null|MYPSP!857609478532522!AABC0876543!919520345244|PAY!NA!NA!PTM858824d5ac714342a7eb152d4feaa397!NA!|NA!NA!NA|CURRENT!NA!NA!NA!NA|ABC!NA!NA!NA!NA";
    String failureCallbackCreditLine = "10744326|{ESN}|{callbackAmount}|2025:09:11 05:47:32|FAILURE|Transaction Failure|U08|NA|9520345244@ptyes|382034275364|NA|null|null|null|null|null|MyCM!857994698322155!AABG0879548!919520345244|PAY!NA!NA!PTMa9217f5e068043348eb7d0520b48f1fa!NA!|NA!NA!NA|CREDITLINE!NA!NA!NA!NA|ABC!NA!NA!NA!NA ";


    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59551")
    @Test(description = "validate UPI Intent Upi Psp Consult for mdr Upi Savings")
    public void validateUpiIntentUpiPspConsultformdrUpiSavings() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String mid = Constants.MerchantType.MDR_FEE_ON_UPI_SUBTYPE.getId();
        String txnAmount = "200";
        String txnAmountupipsp = "200";
        String paymentFee = "0";
        String payeeVPA = "paytm-9759417321@ptybl";
        String payerPaymentInstrument = "UPI_SAVINGS";
        String callbackpaymentInstrument = "";
        String subAccountTypeValue = "";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.MDR_FEE_ON_UPI_SUBTYPE).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("3D")
                .setRiskExtendInfo("userLBSLatitude:26.88|userLBSLongitude:81.01|userAgent:Mozilla/5.0 (Linux; Android 8.1.0; Redmi Note 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36|timeZone:Asia/Calcutta|operationType:PAYMENT|networkType:4g|businessFlow:JS_CHECKOUT|amount:2|language:en-US|screenResolution:393X786|deviceType:mobile|channelId:WAP|platform:mWeb|osType:Android|deviceModel:Redmi Note 5|browserType:Chrome|browserVersion:96.0.4664.104|osVersion:8.1.0|deviceManufacturer:Xiaomi")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        String deeplink = ptcResponse.getBody().getDeepLinkInfo().getDeepLink();
        String externalSerialNo = deeplink.substring(deeplink.indexOf("tr")+3,deeplink.indexOf("&am"));
        String bankRrn = CommonHelpers.generateOrderId();
        System.out.println(externalSerialNo);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,orderId);
        String validateparam = "merchantAllowedUpiPaymentInstruments\":[\"VOUCHER\",\"CREDIT_CARD\",\"PPI_WALLET\",\"CREDITLINE_ALL\"]";
        Assertions.assertThat(logs).contains(validateparam);

        UpiPspConsultFee upiPspConsultFeeobject =new UpiPspConsultFee( mid,  txnAmountupipsp, paymentFee ,  payeeVPA,  externalSerialNo ,  payerPaymentInstrument,  subAccountTypeValue) ;
        JsonPath responseJsonPath = upiPspConsultFeeobject.execute().jsonPath();
        Assertions.assertThat(responseJsonPath.getString("body.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(responseJsonPath.getString("body.resultCodeId")).isEqualTo("001");
        Assertions.assertThat(responseJsonPath.getString("body.resultMsg")).isEqualTo("success");
        Assertions.assertThat(responseJsonPath.getString("body.mid")).isEqualTo(mid);
        Assertions.assertThat(responseJsonPath.getString("body.txnAmount")).isEqualTo(txnAmountupipsp);
        Assertions.assertThat(responseJsonPath.getString("body.externalSerialNo")).isEqualTo(externalSerialNo);
        Assertions.assertThat(responseJsonPath.getString("body.payerPaymentInstrument")).isEqualTo(payerPaymentInstrument);
        Assertions.assertThat(responseJsonPath.getString("body.convenienceFeeValidation")).isEqualTo("MATCH");

        InstaproxyPtybliResponse successresponse = new InstaproxyPtybliResponse(txnAmountupipsp,externalSerialNo ,bankRrn, callbackpaymentInstrument , "UPI" );
        JsonPath callbackres = successresponse.execute().jsonPath();
        Assertions.assertThat(callbackres.getString("body.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(callbackres.getString("body.resultMsg")).isEqualTo("success");

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PTYBLI.toString())
                .validateMid(mid)
                .AssertAll();
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59551")
    @Test(description = "validate UPI Intent Upi Psp Consult for mdr Upi Credit Card")
    public void validateUpiIntentUpiPspConsultformdrUpiCreditCard() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String mid = Constants.MerchantType.MDR_FEE_ON_UPI_SUBTYPE.getId();
        String txnAmount = "200";
        String txnAmountupipsp = "200";
        String paymentFee = "0";
        String payeeVPA = "paytm-9759417321@ptybl";
        String payerPaymentInstrument = "UPI_CREDIT_CARD";
        String callbackpaymentInstrument = "CREDIT_CARD";
        String subAccountTypeValue = "";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.MDR_FEE_ON_UPI_SUBTYPE).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("3D")
                .setRiskExtendInfo("userLBSLatitude:26.88|userLBSLongitude:81.01|userAgent:Mozilla/5.0 (Linux; Android 8.1.0; Redmi Note 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36|timeZone:Asia/Calcutta|operationType:PAYMENT|networkType:4g|businessFlow:JS_CHECKOUT|amount:2|language:en-US|screenResolution:393X786|deviceType:mobile|channelId:WAP|platform:mWeb|osType:Android|deviceModel:Redmi Note 5|browserType:Chrome|browserVersion:96.0.4664.104|osVersion:8.1.0|deviceManufacturer:Xiaomi")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        String deeplink = ptcResponse.getBody().getDeepLinkInfo().getDeepLink();
        String externalSerialNo = deeplink.substring(deeplink.indexOf("tr")+3,deeplink.indexOf("&am"));
        String bankRrn = CommonHelpers.generateOrderId();
        System.out.println(externalSerialNo);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        UpiPspConsultFee upiPspConsultFeeobject =new UpiPspConsultFee( mid,  txnAmountupipsp, paymentFee ,  payeeVPA,  externalSerialNo ,  payerPaymentInstrument,  subAccountTypeValue) ;
        JsonPath responseJsonPath = upiPspConsultFeeobject.execute().jsonPath();
        Assertions.assertThat(responseJsonPath.getString("body.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(responseJsonPath.getString("body.resultCodeId")).isEqualTo("001");
        Assertions.assertThat(responseJsonPath.getString("body.resultMsg")).isEqualTo("success");
        Assertions.assertThat(responseJsonPath.getString("body.mid")).isEqualTo(mid);
        Assertions.assertThat(responseJsonPath.getString("body.txnAmount")).isEqualTo(txnAmountupipsp);
        Assertions.assertThat(responseJsonPath.getString("body.externalSerialNo")).isEqualTo(externalSerialNo);
        Assertions.assertThat(responseJsonPath.getString("body.payerPaymentInstrument")).isEqualTo(payerPaymentInstrument);
        Assertions.assertThat(responseJsonPath.getString("body.convenienceFeeValidation")).isEqualTo("MATCH");

        InstaproxyPtybliResponse successresponse = new InstaproxyPtybliResponse(txnAmountupipsp,externalSerialNo ,bankRrn, callbackpaymentInstrument , "upiCcBin" );
        JsonPath callbackres = successresponse.execute().jsonPath();
        Assertions.assertThat(callbackres.getString("body.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(callbackres.getString("body.resultMsg")).isEqualTo("success");

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PTYBLI.toString())
                .validateMid(mid)
                .AssertAll();

        Assertions.assertThat(txnStatus.execute().getBody().prettyPrint()).contains("UPICC");
        Assertions.assertThat(txnStatus.execute().getBody().prettyPrint()).contains("652925");
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59551")
    @Test(description = "validate UPI Intent Upi Psp Consult for mdr Upi Credit Line")
    public void validateUpiIntentUpiPspConsultformdrUpiCreditLine() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String mid = Constants.MerchantType.MDR_FEE_ON_UPI_SUBTYPE.getId();
        String txnAmount = "200";
        String txnAmountupipsp = "200";
        String paymentFee = "0";
        String payeeVPA = "paytm-9759417321@ptybl";
        String payerPaymentInstrument = "UPI_CREDITLINE";
        String subAccountTypeValue = "CREDITLINE01";
        String callbackpaymentInstrument = "CREDITLINE_CREDITLINE01";

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.MDR_FEE_ON_UPI_SUBTYPE).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("3D")
                .setRiskExtendInfo("userLBSLatitude:26.88|userLBSLongitude:81.01|userAgent:Mozilla/5.0 (Linux; Android 8.1.0; Redmi Note 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36|timeZone:Asia/Calcutta|operationType:PAYMENT|networkType:4g|businessFlow:JS_CHECKOUT|amount:2|language:en-US|screenResolution:393X786|deviceType:mobile|channelId:WAP|platform:mWeb|osType:Android|deviceModel:Redmi Note 5|browserType:Chrome|browserVersion:96.0.4664.104|osVersion:8.1.0|deviceManufacturer:Xiaomi")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        String deeplink = ptcResponse.getBody().getDeepLinkInfo().getDeepLink();
        String externalSerialNo = deeplink.substring(deeplink.indexOf("tr")+3,deeplink.indexOf("&am"));
        String bankRrn = CommonHelpers.generateOrderId();
        System.out.println(externalSerialNo);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        UpiPspConsultFee upiPspConsultFeeobject =new UpiPspConsultFee( mid,  txnAmountupipsp, paymentFee ,  payeeVPA,  externalSerialNo ,  payerPaymentInstrument,  subAccountTypeValue) ;
        JsonPath responseJsonPath = upiPspConsultFeeobject.execute().jsonPath();
        Assertions.assertThat(responseJsonPath.getString("body.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(responseJsonPath.getString("body.resultCodeId")).isEqualTo("001");
        Assertions.assertThat(responseJsonPath.getString("body.resultMsg")).isEqualTo("success");
        Assertions.assertThat(responseJsonPath.getString("body.mid")).isEqualTo(mid);
        Assertions.assertThat(responseJsonPath.getString("body.txnAmount")).isEqualTo(txnAmountupipsp);
        Assertions.assertThat(responseJsonPath.getString("body.externalSerialNo")).isEqualTo(externalSerialNo);
        Assertions.assertThat(responseJsonPath.getString("body.payerPaymentInstrument")).isEqualTo(payerPaymentInstrument);
        Assertions.assertThat(responseJsonPath.getString("body.convenienceFeeValidation")).isEqualTo("MATCH");

        InstaproxyPtybliResponse successresponse = new InstaproxyPtybliResponse(txnAmountupipsp,externalSerialNo ,bankRrn, callbackpaymentInstrument );
        JsonPath callbackres = successresponse.execute().jsonPath();
        Assertions.assertThat(callbackres.getString("body.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(callbackres.getString("body.resultMsg")).isEqualTo("success");

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PTYBLI.toString())
                .validateMid(mid)
                .AssertAll();

        Assertions.assertThat(txnStatus.execute().getBody().prettyPrint()).contains("UPICL");
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59551")
    @Test(description = "validate UPI Intent Upi Psp Consult for mdr Upi Ppi Wallet")
    public void validateUpiIntentUpiPspConsultformdrUpiPpiWallet() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String mid = Constants.MerchantType.MDR_FEE_ON_UPI_SUBTYPE.getId();
        String txnAmount = "200";
        String txnAmountupipsp = "200";
        String paymentFee = "0";
        String payeeVPA = "paytm-9759417321@ptybl";
        String payerPaymentInstrument = "PPI_WALLET";
        String subAccountTypeValue = "";
        String callbackpaymentInstrument = "PPI_WALLET";

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.MDR_FEE_ON_UPI_SUBTYPE).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("3D")
                .setRiskExtendInfo("userLBSLatitude:26.88|userLBSLongitude:81.01|userAgent:Mozilla/5.0 (Linux; Android 8.1.0; Redmi Note 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36|timeZone:Asia/Calcutta|operationType:PAYMENT|networkType:4g|businessFlow:JS_CHECKOUT|amount:2|language:en-US|screenResolution:393X786|deviceType:mobile|channelId:WAP|platform:mWeb|osType:Android|deviceModel:Redmi Note 5|browserType:Chrome|browserVersion:96.0.4664.104|osVersion:8.1.0|deviceManufacturer:Xiaomi")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        String deeplink = ptcResponse.getBody().getDeepLinkInfo().getDeepLink();
        String externalSerialNo = deeplink.substring(deeplink.indexOf("tr")+3,deeplink.indexOf("&am"));
        String bankRrn = CommonHelpers.generateOrderId();
        System.out.println(externalSerialNo);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        UpiPspConsultFee upiPspConsultFeeobject =new UpiPspConsultFee( mid,  txnAmountupipsp, paymentFee ,  payeeVPA,  externalSerialNo ,  payerPaymentInstrument,  subAccountTypeValue) ;
        JsonPath responseJsonPath = upiPspConsultFeeobject.execute().jsonPath();
        Assertions.assertThat(responseJsonPath.getString("body.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(responseJsonPath.getString("body.resultCodeId")).isEqualTo("001");
        Assertions.assertThat(responseJsonPath.getString("body.resultMsg")).isEqualTo("success");
        Assertions.assertThat(responseJsonPath.getString("body.mid")).isEqualTo(mid);
        Assertions.assertThat(responseJsonPath.getString("body.txnAmount")).isEqualTo(txnAmountupipsp);
        Assertions.assertThat(responseJsonPath.getString("body.externalSerialNo")).isEqualTo(externalSerialNo);
        Assertions.assertThat(responseJsonPath.getString("body.payerPaymentInstrument")).isEqualTo(payerPaymentInstrument);
        Assertions.assertThat(responseJsonPath.getString("body.convenienceFeeValidation")).isEqualTo("MATCH");

        InstaproxyPtybliResponse successresponse = new InstaproxyPtybliResponse(txnAmountupipsp,externalSerialNo ,bankRrn, callbackpaymentInstrument );
        JsonPath callbackres = successresponse.execute().jsonPath();
        Assertions.assertThat(callbackres.getString("body.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(callbackres.getString("body.resultMsg")).isEqualTo("success");

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PTYBLI.toString())
                .validateMid(mid)
                .AssertAll();

        Assertions.assertThat(txnStatus.execute().getBody().prettyPrint()).contains("UPI_PPIWALLET");
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59551")
    @Test(description = "validate UPI Collect Upi Psp Consult for mdr Upi Savings")
    public void validateUpiCollectUpiPspConsultformdrUpiSavings() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String mid = Constants.MerchantType.MDR_FEE_ON_UPI_SUBTYPE.getId();
        String txnAmount = "200";
        String txnAmountupipsp = "200";
        String paymentFee = "0";
        String payeeVPA = "paytm-9759417321@ptybl";
        String payerPaymentInstrument = "UPI_SAVINGS";
        String subAccountTypeValue = "";

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.MDR_FEE_ON_UPI_SUBTYPE).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setAuthMode("USRPWD")
                .setPayerAccount("9759417329@paytm")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        String jsonResponse  = ptcResponse.toString();

        JSONObject root = new JSONObject(jsonResponse);
        String externalSerialNo = root.getJSONObject("body")
                .getJSONObject("bankForm")
                .getJSONObject("redirectForm")
                .getJSONObject("content")
                .getString("externalSrNo");

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,orderId);
        String validateparam = "merchantAllowedUpiPaymentInstrumentsCommaSeparated=VOUCHER,CREDIT_CARD,PPI_WALLET,CREDITLINE_ALL";
        Assertions.assertThat(logs).contains(validateparam);

        UpiPspConsultFee upiPspConsultFeeobject =new UpiPspConsultFee( mid,  txnAmountupipsp, paymentFee ,  payeeVPA,  externalSerialNo ,  payerPaymentInstrument,  subAccountTypeValue) ;
        JsonPath responseJsonPath = upiPspConsultFeeobject.execute().jsonPath();
        Assertions.assertThat(responseJsonPath.getString("body.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(responseJsonPath.getString("body.resultCodeId")).isEqualTo("001");
        Assertions.assertThat(responseJsonPath.getString("body.resultMsg")).isEqualTo("success");
        Assertions.assertThat(responseJsonPath.getString("body.mid")).isEqualTo(mid);
        Assertions.assertThat(responseJsonPath.getString("body.txnAmount")).isEqualTo(txnAmountupipsp);
        Assertions.assertThat(responseJsonPath.getString("body.externalSerialNo")).isEqualTo(externalSerialNo);
        Assertions.assertThat(responseJsonPath.getString("body.payerPaymentInstrument")).isEqualTo(payerPaymentInstrument);
        Assertions.assertThat(responseJsonPath.getString("body.convenienceFeeValidation")).isEqualTo("MATCH");

        NativeHelpers.submitJsonFormInBrowser(ptcResponse.toString());
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(mid)
                .validateOrderId(orderId)
                .validatePaymentMode("UPI")
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.PTYBLC.toString())
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PTYBLC.toString())
                .validateMid(mid)
                .AssertAll();
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59551")
    @Test(description = "validate UPI Collect Upi Psp Consult for mdr Upi Ppi Wallet")
    public void validateUpiCollectUpiPspConsultformdrUpiPpiWallet() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String mid = Constants.MerchantType.MDR_FEE_ON_UPI_SUBTYPE.getId();
        String txnAmount = "205.01";
        String txnAmountupipsp = "205.01";
        String paymentFee = "0";
        String payeeVPA = "paytm-9759417321@ptybl";
        String payerPaymentInstrument = "PPI_WALLET";
        String subAccountTypeValue = "";

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.MDR_FEE_ON_UPI_SUBTYPE).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setAuthMode("USRPWD")
                .setPayerAccount("9759417329@paytm")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        String jsonResponse  = ptcResponse.toString();

        JSONObject root = new JSONObject(jsonResponse);
        String externalSerialNo = root.getJSONObject("body")
                .getJSONObject("bankForm")
                .getJSONObject("redirectForm")
                .getJSONObject("content")
                .getString("externalSrNo");

        UpiPspConsultFee upiPspConsultFeeobject =new UpiPspConsultFee( mid,  txnAmountupipsp, paymentFee ,  payeeVPA,  externalSerialNo ,  payerPaymentInstrument,  subAccountTypeValue) ;
        JsonPath responseJsonPath = upiPspConsultFeeobject.execute().jsonPath();
        Assertions.assertThat(responseJsonPath.getString("body.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(responseJsonPath.getString("body.resultCodeId")).isEqualTo("001");
        Assertions.assertThat(responseJsonPath.getString("body.resultMsg")).isEqualTo("success");
        Assertions.assertThat(responseJsonPath.getString("body.mid")).isEqualTo(mid);
        Assertions.assertThat(responseJsonPath.getString("body.txnAmount")).isEqualTo(txnAmountupipsp);
        Assertions.assertThat(responseJsonPath.getString("body.externalSerialNo")).isEqualTo(externalSerialNo);
        Assertions.assertThat(responseJsonPath.getString("body.payerPaymentInstrument")).isEqualTo(payerPaymentInstrument);
        Assertions.assertThat(responseJsonPath.getString("body.convenienceFeeValidation")).isEqualTo("MATCH");
        NativeHelpers.submitJsonFormInBrowser(ptcResponse.toString());
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(mid)
                .validateOrderId(orderId)
                .validatePaymentMode("UPI")
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.PTYBLC.toString())
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PTYBLC.toString())
                .validateMid(mid)
                .AssertAll();

        Assertions.assertThat(txnStatus.execute().getBody().prettyPrint()).contains("UPI_PPIWALLET");

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59551")
    @Test(description = "validate UPI Collect Upi Psp Consult for mdr Upi Credit Card")
    public void validateUpiCollectUpiPspConsultformdrUpiCreditCard() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String mid = Constants.MerchantType.MDR_FEE_ON_UPI_SUBTYPE.getId();
        String txnAmount = "205.02";
        String txnAmountupipsp = "205.02";
        String paymentFee = "0";
        String payeeVPA = "paytm-9759417321@ptybl";
        String payerPaymentInstrument = "UPI_CREDIT_CARD";
        String subAccountTypeValue = "";

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.MDR_FEE_ON_UPI_SUBTYPE).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setAuthMode("USRPWD")
                .setPayerAccount("9759417329@paytm")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        String jsonResponse  = ptcResponse.toString();

        JSONObject root = new JSONObject(jsonResponse);
        String externalSerialNo = root.getJSONObject("body")
                .getJSONObject("bankForm")
                .getJSONObject("redirectForm")
                .getJSONObject("content")
                .getString("externalSrNo");

        UpiPspConsultFee upiPspConsultFeeobject =new UpiPspConsultFee( mid,  txnAmountupipsp, paymentFee ,  payeeVPA,  externalSerialNo ,  payerPaymentInstrument,  subAccountTypeValue) ;
        JsonPath responseJsonPath = upiPspConsultFeeobject.execute().jsonPath();
        Assertions.assertThat(responseJsonPath.getString("body.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(responseJsonPath.getString("body.resultCodeId")).isEqualTo("001");
        Assertions.assertThat(responseJsonPath.getString("body.resultMsg")).isEqualTo("success");
        Assertions.assertThat(responseJsonPath.getString("body.mid")).isEqualTo(mid);
        Assertions.assertThat(responseJsonPath.getString("body.txnAmount")).isEqualTo(txnAmountupipsp);
        Assertions.assertThat(responseJsonPath.getString("body.externalSerialNo")).isEqualTo(externalSerialNo);
        Assertions.assertThat(responseJsonPath.getString("body.payerPaymentInstrument")).isEqualTo(payerPaymentInstrument);
        Assertions.assertThat(responseJsonPath.getString("body.convenienceFeeValidation")).isEqualTo("MATCH");
        NativeHelpers.submitJsonFormInBrowser(ptcResponse.toString());
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(mid)
                .validateOrderId(orderId)
                .validatePaymentMode("UPI")
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.PTYBLC.toString())
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PTYBLC.toString())
                .validateMid(mid)
                .AssertAll();

        Assertions.assertThat(txnStatus.execute().getBody().prettyPrint()).contains("UPICC");

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59551")
    @Test(description = "validate UPI Collect Upi Psp Consult for mdr Upi Credit Line")
    public void validateUpiCollectUpiPspConsultformdrUpiCreditLine() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String mid = Constants.MerchantType.MDR_FEE_ON_UPI_SUBTYPE.getId();
        String txnAmount = "205.03";
        String txnAmountupipsp = "205.03";
        String paymentFee = "0";
        String payeeVPA = "paytm-9759417321@ptybl";
        String payerPaymentInstrument = "UPI_CREDITLINE";
        String subAccountTypeValue = "CREDITLINE01";

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.MDR_FEE_ON_UPI_SUBTYPE).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setAuthMode("USRPWD")
                .setPayerAccount("9759417329@paytm")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        String jsonResponse  = ptcResponse.toString();

        JSONObject root = new JSONObject(jsonResponse);
        String externalSerialNo = root.getJSONObject("body")
                .getJSONObject("bankForm")
                .getJSONObject("redirectForm")
                .getJSONObject("content")
                .getString("externalSrNo");

        UpiPspConsultFee upiPspConsultFeeobject =new UpiPspConsultFee( mid,  txnAmountupipsp, paymentFee ,  payeeVPA,  externalSerialNo ,  payerPaymentInstrument,  subAccountTypeValue) ;
        JsonPath responseJsonPath = upiPspConsultFeeobject.execute().jsonPath();
        Assertions.assertThat(responseJsonPath.getString("body.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(responseJsonPath.getString("body.resultCodeId")).isEqualTo("001");
        Assertions.assertThat(responseJsonPath.getString("body.resultMsg")).isEqualTo("success");
        Assertions.assertThat(responseJsonPath.getString("body.mid")).isEqualTo(mid);
        Assertions.assertThat(responseJsonPath.getString("body.txnAmount")).isEqualTo(txnAmountupipsp);
        Assertions.assertThat(responseJsonPath.getString("body.externalSerialNo")).isEqualTo(externalSerialNo);
        Assertions.assertThat(responseJsonPath.getString("body.payerPaymentInstrument")).isEqualTo(payerPaymentInstrument);
        Assertions.assertThat(responseJsonPath.getString("body.convenienceFeeValidation")).isEqualTo("MATCH");

        NativeHelpers.submitJsonFormInBrowser(ptcResponse.toString());
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(mid)
                .validateOrderId(orderId)
                .validatePaymentMode("UPI")
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.PTYBLC.toString())
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PTYBLC.toString())
                .validateMid(mid)
                .AssertAll();

        Assertions.assertThat(txnStatus.execute().getBody().prettyPrint()).contains("UPICL");

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59551")
    @Test(description = "validate Upi Upi Intent PCF Psp Consult for mdr Upi Savings")
    public void validateUpiIntentPCFUpiPspConsultformdrUpiSavings() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String mid = Constants.MerchantType.PCF_FEE_ON_UPI_SUBTYPE.getId();
        String txnAmount = "200";
        String txnAmountupipsp = "200";
        String paymentFee = "0";
        String payeeVPA = "paytm-9759417322@ptybl";
        String payerPaymentInstrument = "UPI_SAVINGS";
        String callbackpaymentInstrument = "";
        String subAccountTypeValue = "";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.PCF_FEE_ON_UPI_SUBTYPE).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("3D")
                .setRiskExtendInfo("userLBSLatitude:26.88|userLBSLongitude:81.01|userAgent:Mozilla/5.0 (Linux; Android 8.1.0; Redmi Note 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36|timeZone:Asia/Calcutta|operationType:PAYMENT|networkType:4g|businessFlow:JS_CHECKOUT|amount:2|language:en-US|screenResolution:393X786|deviceType:mobile|channelId:WAP|platform:mWeb|osType:Android|deviceModel:Redmi Note 5|browserType:Chrome|browserVersion:96.0.4664.104|osVersion:8.1.0|deviceManufacturer:Xiaomi")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        String deeplink = ptcResponse.getBody().getDeepLinkInfo().getDeepLink();
        String externalSerialNo = deeplink.substring(deeplink.indexOf("tr")+3,deeplink.indexOf("&am"));
        String bankRrn = CommonHelpers.generateOrderId();
        System.out.println(externalSerialNo);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        UpiPspConsultFee upiPspConsultFeeobject =new UpiPspConsultFee( mid,  txnAmountupipsp, paymentFee ,  payeeVPA,  externalSerialNo ,  payerPaymentInstrument,  subAccountTypeValue) ;
        JsonPath responseJsonPath = upiPspConsultFeeobject.execute().jsonPath();
        Assertions.assertThat(responseJsonPath.getString("body.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(responseJsonPath.getString("body.resultCodeId")).isEqualTo("001");
        Assertions.assertThat(responseJsonPath.getString("body.resultMsg")).isEqualTo("success");
        Assertions.assertThat(responseJsonPath.getString("body.mid")).isEqualTo(mid);
        Assertions.assertThat(responseJsonPath.getString("body.txnAmount")).isEqualTo(txnAmountupipsp);
        Assertions.assertThat(responseJsonPath.getString("body.externalSerialNo")).isEqualTo(externalSerialNo);
        Assertions.assertThat(responseJsonPath.getString("body.payerPaymentInstrument")).isEqualTo(payerPaymentInstrument);
        Assertions.assertThat(responseJsonPath.getString("body.convenienceFeeValidation")).isEqualTo("MATCH");

        InstaproxyPtybliResponse successresponse = new InstaproxyPtybliResponse(txnAmountupipsp,externalSerialNo ,bankRrn, callbackpaymentInstrument );
        JsonPath callbackres = successresponse.execute().jsonPath();
        Assertions.assertThat(callbackres.getString("body.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(callbackres.getString("body.resultMsg")).isEqualTo("success");

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PTYBLI.toString())
                .validateMid(mid)
                .AssertAll();
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59551")
    @Test(description = "validate Upi Intent PCF Upi Psp Consult for mdr Upi Credit Card")
    public void validateUpiIntentPCFUpiPspConsultformdrUpiCreditCard() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String mid = Constants.MerchantType.PCF_FEE_ON_UPI_SUBTYPE.getId();
        String txnAmount = "200";
        String txnAmountupipsp = "204.72";
        String paymentFee = "4.72";
        String payeeVPA = "paytm-9759417322@ptybl";
        String payerPaymentInstrument = "UPI_CREDIT_CARD";
        String callbackpaymentInstrument = "CREDIT_CARD";
        String subAccountTypeValue = "";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.PCF_FEE_ON_UPI_SUBTYPE).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("3D")
                .setRiskExtendInfo("userLBSLatitude:26.88|userLBSLongitude:81.01|userAgent:Mozilla/5.0 (Linux; Android 8.1.0; Redmi Note 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36|timeZone:Asia/Calcutta|operationType:PAYMENT|networkType:4g|businessFlow:JS_CHECKOUT|amount:2|language:en-US|screenResolution:393X786|deviceType:mobile|channelId:WAP|platform:mWeb|osType:Android|deviceModel:Redmi Note 5|browserType:Chrome|browserVersion:96.0.4664.104|osVersion:8.1.0|deviceManufacturer:Xiaomi")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        String deeplink = ptcResponse.getBody().getDeepLinkInfo().getDeepLink();
        String externalSerialNo = deeplink.substring(deeplink.indexOf("tr")+3,deeplink.indexOf("&am"));
        String bankRrn = CommonHelpers.generateOrderId();
        System.out.println(externalSerialNo);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        UpiPspConsultFee upiPspConsultFeeobject =new UpiPspConsultFee( mid,  txnAmountupipsp, paymentFee ,  payeeVPA,  externalSerialNo ,  payerPaymentInstrument,  subAccountTypeValue) ;
        JsonPath responseJsonPath = upiPspConsultFeeobject.execute().jsonPath();
        Assertions.assertThat(responseJsonPath.getString("body.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(responseJsonPath.getString("body.resultCodeId")).isEqualTo("001");
        Assertions.assertThat(responseJsonPath.getString("body.resultMsg")).isEqualTo("success");
        Assertions.assertThat(responseJsonPath.getString("body.mid")).isEqualTo(mid);
        Assertions.assertThat(responseJsonPath.getString("body.txnAmount")).isEqualTo(txnAmountupipsp);
        Assertions.assertThat(responseJsonPath.getString("body.externalSerialNo")).isEqualTo(externalSerialNo);
        Assertions.assertThat(responseJsonPath.getString("body.payerPaymentInstrument")).isEqualTo(payerPaymentInstrument);
        Assertions.assertThat(responseJsonPath.getString("body.convenienceFeeValidation")).isEqualTo("MATCH");

        InstaproxyPtybliResponse successresponse = new InstaproxyPtybliResponse(txnAmountupipsp,externalSerialNo ,bankRrn, callbackpaymentInstrument , "upiCcBin" );
        JsonPath callbackres = successresponse.execute().jsonPath();
        Assertions.assertThat(callbackres.getString("body.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(callbackres.getString("body.resultMsg")).isEqualTo("success");

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PTYBLI.toString())
                .validateMid(mid)
                .AssertAll();

        Assertions.assertThat(txnStatus.execute().getBody().prettyPrint()).contains("UPICC");
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59551")
    @Test(description = "validate Upi Intent PCF Upi Psp Consult for mdr Upi Credit Line")
    public void validateUpiIntentPCFUpiPspConsultformdrUpiCreditLine() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String mid = Constants.MerchantType.PCF_FEE_ON_UPI_SUBTYPE.getId();
        String txnAmount = "200";
        String txnAmountupipsp = "204.72";
        String paymentFee = "4.72";
        String payeeVPA = "paytm-9759417322@ptybl";
        String payerPaymentInstrument = "UPI_CREDITLINE";
        String subAccountTypeValue = "CREDITLINE01";
        String callbackpaymentInstrument = "CREDITLINE_CREDITLINE01";

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.PCF_FEE_ON_UPI_SUBTYPE).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("3D")
                .setRiskExtendInfo("userLBSLatitude:26.88|userLBSLongitude:81.01|userAgent:Mozilla/5.0 (Linux; Android 8.1.0; Redmi Note 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36|timeZone:Asia/Calcutta|operationType:PAYMENT|networkType:4g|businessFlow:JS_CHECKOUT|amount:2|language:en-US|screenResolution:393X786|deviceType:mobile|channelId:WAP|platform:mWeb|osType:Android|deviceModel:Redmi Note 5|browserType:Chrome|browserVersion:96.0.4664.104|osVersion:8.1.0|deviceManufacturer:Xiaomi")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        String deeplink = ptcResponse.getBody().getDeepLinkInfo().getDeepLink();
        String externalSerialNo = deeplink.substring(deeplink.indexOf("tr")+3,deeplink.indexOf("&am"));
        String bankRrn = CommonHelpers.generateOrderId();
        System.out.println(externalSerialNo);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        UpiPspConsultFee upiPspConsultFeeobject =new UpiPspConsultFee( mid,  txnAmountupipsp, paymentFee ,  payeeVPA,  externalSerialNo ,  payerPaymentInstrument,  subAccountTypeValue) ;
        JsonPath responseJsonPath = upiPspConsultFeeobject.execute().jsonPath();
        Assertions.assertThat(responseJsonPath.getString("body.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(responseJsonPath.getString("body.resultCodeId")).isEqualTo("001");
        Assertions.assertThat(responseJsonPath.getString("body.resultMsg")).isEqualTo("success");
        Assertions.assertThat(responseJsonPath.getString("body.mid")).isEqualTo(mid);
        Assertions.assertThat(responseJsonPath.getString("body.txnAmount")).isEqualTo(txnAmountupipsp);
        Assertions.assertThat(responseJsonPath.getString("body.externalSerialNo")).isEqualTo(externalSerialNo);
        Assertions.assertThat(responseJsonPath.getString("body.payerPaymentInstrument")).isEqualTo(payerPaymentInstrument);
        Assertions.assertThat(responseJsonPath.getString("body.convenienceFeeValidation")).isEqualTo("MATCH");

        InstaproxyPtybliResponse successresponse = new InstaproxyPtybliResponse(txnAmountupipsp,externalSerialNo ,bankRrn, callbackpaymentInstrument );
        JsonPath callbackres = successresponse.execute().jsonPath();
        Assertions.assertThat(callbackres.getString("body.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(callbackres.getString("body.resultMsg")).isEqualTo("success");

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PTYBLI.toString())
                .validateMid(mid)
                .AssertAll();

        Assertions.assertThat(txnStatus.execute().getBody().prettyPrint()).contains("UPICL");
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59551")
    @Test(description = "validate Upi Intent PCF Upi Psp Consult for mdr Upi Ppi Wallet")
    public void validateUpiIntentPCFUpiPspConsultformdrUpiPpiWallet() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String mid = Constants.MerchantType.PCF_FEE_ON_UPI_SUBTYPE.getId();
        String txnAmount = "200";
        String txnAmountupipsp = "204.72";
        String paymentFee = "4.72";
        String payeeVPA = "paytm-9759417322@ptybl";
        String payerPaymentInstrument = "PPI_WALLET";
        String subAccountTypeValue = "";
        String callbackpaymentInstrument = "PPI_WALLET";

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.PCF_FEE_ON_UPI_SUBTYPE).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("3D")
                .setRiskExtendInfo("userLBSLatitude:26.88|userLBSLongitude:81.01|userAgent:Mozilla/5.0 (Linux; Android 8.1.0; Redmi Note 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36|timeZone:Asia/Calcutta|operationType:PAYMENT|networkType:4g|businessFlow:JS_CHECKOUT|amount:2|language:en-US|screenResolution:393X786|deviceType:mobile|channelId:WAP|platform:mWeb|osType:Android|deviceModel:Redmi Note 5|browserType:Chrome|browserVersion:96.0.4664.104|osVersion:8.1.0|deviceManufacturer:Xiaomi")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        String deeplink = ptcResponse.getBody().getDeepLinkInfo().getDeepLink();
        String externalSerialNo = deeplink.substring(deeplink.indexOf("tr")+3,deeplink.indexOf("&am"));
        String bankRrn = CommonHelpers.generateOrderId();
        System.out.println(externalSerialNo);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        UpiPspConsultFee upiPspConsultFeeobject =new UpiPspConsultFee( mid,  txnAmountupipsp, paymentFee ,  payeeVPA,  externalSerialNo ,  payerPaymentInstrument,  subAccountTypeValue) ;
        JsonPath responseJsonPath = upiPspConsultFeeobject.execute().jsonPath();
        Assertions.assertThat(responseJsonPath.getString("body.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(responseJsonPath.getString("body.resultCodeId")).isEqualTo("001");
        Assertions.assertThat(responseJsonPath.getString("body.resultMsg")).isEqualTo("success");
        Assertions.assertThat(responseJsonPath.getString("body.mid")).isEqualTo(mid);
        Assertions.assertThat(responseJsonPath.getString("body.txnAmount")).isEqualTo(txnAmountupipsp);
        Assertions.assertThat(responseJsonPath.getString("body.externalSerialNo")).isEqualTo(externalSerialNo);
        Assertions.assertThat(responseJsonPath.getString("body.payerPaymentInstrument")).isEqualTo(payerPaymentInstrument);
        Assertions.assertThat(responseJsonPath.getString("body.convenienceFeeValidation")).isEqualTo("MATCH");

        InstaproxyPtybliResponse successresponse = new InstaproxyPtybliResponse(txnAmountupipsp,externalSerialNo ,bankRrn, callbackpaymentInstrument );
        JsonPath callbackres = successresponse.execute().jsonPath();
        Assertions.assertThat(callbackres.getString("body.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(callbackres.getString("body.resultMsg")).isEqualTo("success");

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PTYBLI.toString())
                .validateMid(mid)
                .AssertAll();

        Assertions.assertThat(txnStatus.execute().getBody().prettyPrint()).contains("UPI_PPIWALLET");
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59551")
    @Test(description = "validate Upi Collect PCF Upi Psp Consult for mdr Upi Savings")
    public void validateUpiCollectPCFUpiPspConsultformdrUpiSavings() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String mid = Constants.MerchantType.PCF_FEE_ON_UPI_SUBTYPE.getId();
        String txnAmount = "200";
        String txnAmountupipsp = "200";
        String paymentFee = "0";
        String payeeVPA = "paytm-9759417322@ptybl";
        String payerPaymentInstrument = "UPI_SAVINGS";
        String subAccountTypeValue = "";

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.PCF_FEE_ON_UPI_SUBTYPE).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setAuthMode("USRPWD")
                .setPayerAccount("9759417329@paytm")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        String jsonResponse  = ptcResponse.toString();

        JSONObject root = new JSONObject(jsonResponse);
        String externalSerialNo = root.getJSONObject("body")
                .getJSONObject("bankForm")
                .getJSONObject("redirectForm")
                .getJSONObject("content")
                .getString("externalSrNo");

        UpiPspConsultFee upiPspConsultFeeobject =new UpiPspConsultFee( mid,  txnAmountupipsp, paymentFee ,  payeeVPA,  externalSerialNo ,  payerPaymentInstrument,  subAccountTypeValue) ;
        JsonPath responseJsonPath = upiPspConsultFeeobject.execute().jsonPath();
        Assertions.assertThat(responseJsonPath.getString("body.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(responseJsonPath.getString("body.resultCodeId")).isEqualTo("001");
        Assertions.assertThat(responseJsonPath.getString("body.resultMsg")).isEqualTo("success");
        Assertions.assertThat(responseJsonPath.getString("body.mid")).isEqualTo(mid);
        Assertions.assertThat(responseJsonPath.getString("body.txnAmount")).isEqualTo(txnAmountupipsp);
        Assertions.assertThat(responseJsonPath.getString("body.externalSerialNo")).isEqualTo(externalSerialNo);
        Assertions.assertThat(responseJsonPath.getString("body.payerPaymentInstrument")).isEqualTo(payerPaymentInstrument);
        Assertions.assertThat(responseJsonPath.getString("body.convenienceFeeValidation")).isEqualTo("MATCH");

        NativeHelpers.submitJsonFormInBrowser(ptcResponse.toString());
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(mid)
                .validateOrderId(orderId)
                .validatePaymentMode("UPI")
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.PTYBLC.toString())
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PTYBLC.toString())
                .validateMid(mid)
                .AssertAll();
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59551")
    @Test(description = "validate Upi Collect PCF Upi Psp Consult for mdr Upi Ppi Wallet")
    public void validateUpiCollectPCFUpiPspConsultformdrUpiPpiWallet() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String mid = Constants.MerchantType.PCF_FEE_ON_UPI_SUBTYPE.getId();
        String txnAmount = "205.01";
        String txnAmountupipsp = "205.01";
        String paymentFee = "0";
        String payeeVPA = "paytm-9759417322@ptybl";
        String payerPaymentInstrument = "PPI_WALLET";
        String subAccountTypeValue = "";

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.PCF_FEE_ON_UPI_SUBTYPE).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setAuthMode("USRPWD")
                .setPayerAccount("9759417329@paytm")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        String jsonResponse  = ptcResponse.toString();

        JSONObject root = new JSONObject(jsonResponse);
        String externalSerialNo = root.getJSONObject("body")
                .getJSONObject("bankForm")
                .getJSONObject("redirectForm")
                .getJSONObject("content")
                .getString("externalSrNo");

        UpiPspConsultFee upiPspConsultFeeobject =new UpiPspConsultFee( mid,  txnAmountupipsp, paymentFee ,  payeeVPA,  externalSerialNo ,  payerPaymentInstrument,  subAccountTypeValue) ;
        JsonPath responseJsonPath = upiPspConsultFeeobject.execute().jsonPath();
        Assertions.assertThat(responseJsonPath.getString("body.resultCode")).isEqualTo("FAIL");
        Assertions.assertThat(responseJsonPath.getString("body.resultCodeId")).isEqualTo("009");
        Assertions.assertThat(responseJsonPath.getString("body.resultMsg")).isEqualTo("Fee mismatch");
        Assertions.assertThat(responseJsonPath.getString("body.mid")).isEqualTo(mid);
        Assertions.assertThat(responseJsonPath.getString("body.txnAmount")).isEqualTo(txnAmountupipsp);
        Assertions.assertThat(responseJsonPath.getString("body.externalSerialNo")).isEqualTo(externalSerialNo);
        Assertions.assertThat(responseJsonPath.getString("body.subResultCodeId")).isEqualTo("15008");
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59551")
    @Test(description = "validate Upi Collect PCF Upi Psp Consult for mdr Upi Credit Card")
    public void validateUpiCollectPCFUpiPspConsultformdrUpiCreditCard() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String mid = Constants.MerchantType.PCF_FEE_ON_UPI_SUBTYPE.getId();
        String txnAmount = "205.02";
        String txnAmountupipsp = "205.02";
        String paymentFee = "0";
        String payeeVPA = "paytm-9759417322@ptybl";
        String payerPaymentInstrument = "UPI_CREDIT_CARD";
        String subAccountTypeValue = "";

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.PCF_FEE_ON_UPI_SUBTYPE).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setAuthMode("USRPWD")
                .setPayerAccount("9759417329@paytm")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        String jsonResponse  = ptcResponse.toString();

        JSONObject root = new JSONObject(jsonResponse);
        String externalSerialNo = root.getJSONObject("body")
                .getJSONObject("bankForm")
                .getJSONObject("redirectForm")
                .getJSONObject("content")
                .getString("externalSrNo");

        UpiPspConsultFee upiPspConsultFeeobject =new UpiPspConsultFee( mid,  txnAmountupipsp, paymentFee ,  payeeVPA,  externalSerialNo ,  payerPaymentInstrument,  subAccountTypeValue) ;
        JsonPath responseJsonPath = upiPspConsultFeeobject.execute().jsonPath();
        Assertions.assertThat(responseJsonPath.getString("body.resultCode")).isEqualTo("FAIL");
        Assertions.assertThat(responseJsonPath.getString("body.resultCodeId")).isEqualTo("009");
        Assertions.assertThat(responseJsonPath.getString("body.resultMsg")).isEqualTo("Fee mismatch");
        Assertions.assertThat(responseJsonPath.getString("body.mid")).isEqualTo(mid);
        Assertions.assertThat(responseJsonPath.getString("body.txnAmount")).isEqualTo(txnAmountupipsp);
        Assertions.assertThat(responseJsonPath.getString("body.externalSerialNo")).isEqualTo(externalSerialNo);
        Assertions.assertThat(responseJsonPath.getString("body.subResultCodeId")).isEqualTo("15008");

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59551")
    @Test(description = "validate Upi Collect PCF Upi Psp Consult for mdr Upi Credit Line")
    public void validateUpiCollectPCFUpiPspConsultformdrUpiCreditLine() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String mid = Constants.MerchantType.PCF_FEE_ON_UPI_SUBTYPE.getId();
        String txnAmount = "205.03";
        String txnAmountupipsp = "205.03";
        String paymentFee = "0";
        String payeeVPA = "paytm-9759417322@ptybl";
        String payerPaymentInstrument = "UPI_CREDITLINE";
        String subAccountTypeValue = "CREDITLINE01";

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.PCF_FEE_ON_UPI_SUBTYPE).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setAuthMode("USRPWD")
                .setPayerAccount("9759417329@paytm")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        String jsonResponse  = ptcResponse.toString();

        JSONObject root = new JSONObject(jsonResponse);
        String externalSerialNo = root.getJSONObject("body")
                .getJSONObject("bankForm")
                .getJSONObject("redirectForm")
                .getJSONObject("content")
                .getString("externalSrNo");

        UpiPspConsultFee upiPspConsultFeeobject =new UpiPspConsultFee( mid,  txnAmountupipsp, paymentFee ,  payeeVPA,  externalSerialNo ,  payerPaymentInstrument,  subAccountTypeValue) ;
        JsonPath responseJsonPath = upiPspConsultFeeobject.execute().jsonPath();
        Assertions.assertThat(responseJsonPath.getString("body.resultCode")).isEqualTo("FAIL");
        Assertions.assertThat(responseJsonPath.getString("body.resultCodeId")).isEqualTo("009");
        Assertions.assertThat(responseJsonPath.getString("body.resultMsg")).isEqualTo("Fee mismatch");
        Assertions.assertThat(responseJsonPath.getString("body.mid")).isEqualTo(mid);
        Assertions.assertThat(responseJsonPath.getString("body.txnAmount")).isEqualTo(txnAmountupipsp);
        Assertions.assertThat(responseJsonPath.getString("body.externalSerialNo")).isEqualTo(externalSerialNo);
        Assertions.assertThat(responseJsonPath.getString("body.subResultCodeId")).isEqualTo("15008");

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59551")
    @Test(description = "validate  Upi Intent PCF and platform upi Psp Consult for mdr Upi Savings")
    public void validateUpiIntentPCFandplatformUpiPspConsultformdrUpiSavings() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.PCF_PLATFORM_FEE_ON_UPI_SUBTYPE ;
        String mid = Constants.MerchantType.PCF_PLATFORM_FEE_ON_UPI_SUBTYPE.getId();
        String txnAmount = "200";
        String txnAmountupipsp = "204.72";
        String paymentFee = "0";
        String payeeVPA = "paytm-9759417325@ptybl";
        String payerPaymentInstrument = "UPI_SAVINGS";
        String callbackpaymentInstrument = "";
        String subAccountTypeValue = "";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("3D")
                .setRiskExtendInfo("userLBSLatitude:26.88|userLBSLongitude:81.01|userAgent:Mozilla/5.0 (Linux; Android 8.1.0; Redmi Note 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36|timeZone:Asia/Calcutta|operationType:PAYMENT|networkType:4g|businessFlow:JS_CHECKOUT|amount:2|language:en-US|screenResolution:393X786|deviceType:mobile|channelId:WAP|platform:mWeb|osType:Android|deviceModel:Redmi Note 5|browserType:Chrome|browserVersion:96.0.4664.104|osVersion:8.1.0|deviceManufacturer:Xiaomi")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        String deeplink = ptcResponse.getBody().getDeepLinkInfo().getDeepLink();
        String externalSerialNo = deeplink.substring(deeplink.indexOf("tr")+3,deeplink.indexOf("&am"));
        String amAmount = deeplink.substring(deeplink.indexOf("&am")+4,deeplink.indexOf("&cu"));
        String bankRrn = CommonHelpers.generateOrderId();
        System.out.println(externalSerialNo);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        Assertions.assertThat(amAmount).isEqualTo("204.72");

        UpiPspConsultFee upiPspConsultFeeobject =new UpiPspConsultFee( mid,  txnAmountupipsp, paymentFee ,  payeeVPA,  externalSerialNo ,  payerPaymentInstrument,  subAccountTypeValue) ;
        JsonPath responseJsonPath = upiPspConsultFeeobject.execute().jsonPath();
        Assertions.assertThat(responseJsonPath.getString("body.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(responseJsonPath.getString("body.resultCodeId")).isEqualTo("001");
        Assertions.assertThat(responseJsonPath.getString("body.resultMsg")).isEqualTo("success");
        Assertions.assertThat(responseJsonPath.getString("body.mid")).isEqualTo(mid);
        Assertions.assertThat(responseJsonPath.getString("body.txnAmount")).isEqualTo(txnAmountupipsp);
        Assertions.assertThat(responseJsonPath.getString("body.externalSerialNo")).isEqualTo(externalSerialNo);
        Assertions.assertThat(responseJsonPath.getString("body.payerPaymentInstrument")).isEqualTo(payerPaymentInstrument);
        Assertions.assertThat(responseJsonPath.getString("body.convenienceFeeValidation")).isEqualTo("MATCH");

        InstaproxyPtybliResponse successresponse = new InstaproxyPtybliResponse(txnAmountupipsp,externalSerialNo ,bankRrn, callbackpaymentInstrument );
        JsonPath callbackres = successresponse.execute().jsonPath();
        Assertions.assertThat(callbackres.getString("body.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(callbackres.getString("body.resultMsg")).isEqualTo("success");

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PTYBLI.toString())
                .validateMid(mid)
                .AssertAll();
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59551")
    @Test(description = "validate Upi Intent PCF and platform Upi Psp Consult for mdr Upi Credit Card")
    public void validateUpiIntentPCFandplatformUpiPspConsultformdrUpiCreditCard() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.PCF_PLATFORM_FEE_ON_UPI_SUBTYPE ;
        String mid = Constants.MerchantType.PCF_PLATFORM_FEE_ON_UPI_SUBTYPE.getId();
        String txnAmount = "200";
        String txnAmountupipsp = "209.44";
        String paymentFee = "4.72";
        String payeeVPA = "paytm-9759417325@ptybl";
        String payerPaymentInstrument = "UPI_CREDIT_CARD";
        String callbackpaymentInstrument = "CREDIT_CARD";
        String subAccountTypeValue = "";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("3D")
                .setRiskExtendInfo("userLBSLatitude:26.88|userLBSLongitude:81.01|userAgent:Mozilla/5.0 (Linux; Android 8.1.0; Redmi Note 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36|timeZone:Asia/Calcutta|operationType:PAYMENT|networkType:4g|businessFlow:JS_CHECKOUT|amount:2|language:en-US|screenResolution:393X786|deviceType:mobile|channelId:WAP|platform:mWeb|osType:Android|deviceModel:Redmi Note 5|browserType:Chrome|browserVersion:96.0.4664.104|osVersion:8.1.0|deviceManufacturer:Xiaomi")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        String deeplink = ptcResponse.getBody().getDeepLinkInfo().getDeepLink();
        String externalSerialNo = deeplink.substring(deeplink.indexOf("tr")+3,deeplink.indexOf("&am"));
        String bankRrn = CommonHelpers.generateOrderId();
        System.out.println(externalSerialNo);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        UpiPspConsultFee upiPspConsultFeeobject =new UpiPspConsultFee( mid,  txnAmountupipsp, paymentFee ,  payeeVPA,  externalSerialNo ,  payerPaymentInstrument,  subAccountTypeValue) ;
        JsonPath responseJsonPath = upiPspConsultFeeobject.execute().jsonPath();
        Assertions.assertThat(responseJsonPath.getString("body.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(responseJsonPath.getString("body.resultCodeId")).isEqualTo("001");
        Assertions.assertThat(responseJsonPath.getString("body.resultMsg")).isEqualTo("success");
        Assertions.assertThat(responseJsonPath.getString("body.mid")).isEqualTo(mid);
        Assertions.assertThat(responseJsonPath.getString("body.txnAmount")).isEqualTo(txnAmountupipsp);
        Assertions.assertThat(responseJsonPath.getString("body.externalSerialNo")).isEqualTo(externalSerialNo);
        Assertions.assertThat(responseJsonPath.getString("body.payerPaymentInstrument")).isEqualTo(payerPaymentInstrument);
        Assertions.assertThat(responseJsonPath.getString("body.convenienceFeeValidation")).isEqualTo("MATCH");

        InstaproxyPtybliResponse successresponse = new InstaproxyPtybliResponse(txnAmountupipsp,externalSerialNo ,bankRrn, callbackpaymentInstrument , "upiCcBin" );
        JsonPath callbackres = successresponse.execute().jsonPath();
        Assertions.assertThat(callbackres.getString("body.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(callbackres.getString("body.resultMsg")).isEqualTo("success");

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PTYBLI.toString())
                .validateMid(mid)
                .AssertAll();

        Assertions.assertThat(txnStatus.execute().getBody().prettyPrint()).contains("UPICC");
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59551")
    @Test(description = "validate Upi Intent PCF and platform Upi Psp Consult for mdr Upi Credit Line")
    public void validateUpiIntentPCFandplatformUpiPspConsultformdrUpiCreditLine() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.PCF_PLATFORM_FEE_ON_UPI_SUBTYPE ;
        String mid = Constants.MerchantType.PCF_PLATFORM_FEE_ON_UPI_SUBTYPE.getId();
        String txnAmount = "200";
        String txnAmountupipsp = "209.44";
        String paymentFee = "4.72";
        String payeeVPA = "paytm-9759417325@ptybl";
        String payerPaymentInstrument = "UPI_CREDITLINE";
        String subAccountTypeValue = "CREDITLINE01";
        String callbackpaymentInstrument = "CREDITLINE_CREDITLINE01";

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("3D")
                .setRiskExtendInfo("userLBSLatitude:26.88|userLBSLongitude:81.01|userAgent:Mozilla/5.0 (Linux; Android 8.1.0; Redmi Note 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36|timeZone:Asia/Calcutta|operationType:PAYMENT|networkType:4g|businessFlow:JS_CHECKOUT|amount:2|language:en-US|screenResolution:393X786|deviceType:mobile|channelId:WAP|platform:mWeb|osType:Android|deviceModel:Redmi Note 5|browserType:Chrome|browserVersion:96.0.4664.104|osVersion:8.1.0|deviceManufacturer:Xiaomi")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        String deeplink = ptcResponse.getBody().getDeepLinkInfo().getDeepLink();
        String externalSerialNo = deeplink.substring(deeplink.indexOf("tr")+3,deeplink.indexOf("&am"));
        String bankRrn = CommonHelpers.generateOrderId();
        System.out.println(externalSerialNo);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        UpiPspConsultFee upiPspConsultFeeobject =new UpiPspConsultFee( mid, txnAmountupipsp , paymentFee ,  payeeVPA,  externalSerialNo ,  payerPaymentInstrument,  subAccountTypeValue) ;
        JsonPath responseJsonPath = upiPspConsultFeeobject.execute().jsonPath();
        Assertions.assertThat(responseJsonPath.getString("body.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(responseJsonPath.getString("body.resultCodeId")).isEqualTo("001");
        Assertions.assertThat(responseJsonPath.getString("body.resultMsg")).isEqualTo("success");
        Assertions.assertThat(responseJsonPath.getString("body.mid")).isEqualTo(mid);
        Assertions.assertThat(responseJsonPath.getString("body.txnAmount")).isEqualTo(txnAmountupipsp);
        Assertions.assertThat(responseJsonPath.getString("body.externalSerialNo")).isEqualTo(externalSerialNo);
        Assertions.assertThat(responseJsonPath.getString("body.payerPaymentInstrument")).isEqualTo(payerPaymentInstrument);
        Assertions.assertThat(responseJsonPath.getString("body.convenienceFeeValidation")).isEqualTo("MATCH");

        InstaproxyPtybliResponse successresponse = new InstaproxyPtybliResponse(txnAmountupipsp,externalSerialNo ,bankRrn, callbackpaymentInstrument );
        JsonPath callbackres = successresponse.execute().jsonPath();
        Assertions.assertThat(callbackres.getString("body.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(callbackres.getString("body.resultMsg")).isEqualTo("success");

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PTYBLI.toString())
                .validateMid(mid)
                .AssertAll();

        Assertions.assertThat(txnStatus.execute().getBody().prettyPrint()).contains("UPICL");
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59551")
    @Test(description = "validate Upi Intent PCF and platform Upi Psp Consult for mdr Upi Ppi Wallet")
    public void validateUpiIntentPCFandplatformUpiPspConsultformdrUpiPpiWallet() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.PCF_PLATFORM_FEE_ON_UPI_SUBTYPE ;
        String mid = Constants.MerchantType.PCF_PLATFORM_FEE_ON_UPI_SUBTYPE.getId();
        String txnAmount = "200";
        String txnAmountupipsp = "209.44";
        String paymentFee = "4.72";
        String payeeVPA = "paytm-9759417325@ptybl";
        String payerPaymentInstrument = "PPI_WALLET";
        String subAccountTypeValue = "";
        String callbackpaymentInstrument = "PPI_WALLET";

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("3D")
                .setRiskExtendInfo("userLBSLatitude:26.88|userLBSLongitude:81.01|userAgent:Mozilla/5.0 (Linux; Android 8.1.0; Redmi Note 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36|timeZone:Asia/Calcutta|operationType:PAYMENT|networkType:4g|businessFlow:JS_CHECKOUT|amount:2|language:en-US|screenResolution:393X786|deviceType:mobile|channelId:WAP|platform:mWeb|osType:Android|deviceModel:Redmi Note 5|browserType:Chrome|browserVersion:96.0.4664.104|osVersion:8.1.0|deviceManufacturer:Xiaomi")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        String deeplink = ptcResponse.getBody().getDeepLinkInfo().getDeepLink();
        String externalSerialNo = deeplink.substring(deeplink.indexOf("tr")+3,deeplink.indexOf("&am"));
        String bankRrn = CommonHelpers.generateOrderId();
        System.out.println(externalSerialNo);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        UpiPspConsultFee upiPspConsultFeeobject =new UpiPspConsultFee( mid,  txnAmountupipsp, paymentFee ,  payeeVPA,  externalSerialNo ,  payerPaymentInstrument,  subAccountTypeValue) ;
        JsonPath responseJsonPath = upiPspConsultFeeobject.execute().jsonPath();
        Assertions.assertThat(responseJsonPath.getString("body.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(responseJsonPath.getString("body.resultCodeId")).isEqualTo("001");
        Assertions.assertThat(responseJsonPath.getString("body.resultMsg")).isEqualTo("success");
        Assertions.assertThat(responseJsonPath.getString("body.mid")).isEqualTo(mid);
        Assertions.assertThat(responseJsonPath.getString("body.txnAmount")).isEqualTo(txnAmountupipsp);
        Assertions.assertThat(responseJsonPath.getString("body.externalSerialNo")).isEqualTo(externalSerialNo);
        Assertions.assertThat(responseJsonPath.getString("body.payerPaymentInstrument")).isEqualTo(payerPaymentInstrument);
        Assertions.assertThat(responseJsonPath.getString("body.convenienceFeeValidation")).isEqualTo("MATCH");

        InstaproxyPtybliResponse successresponse = new InstaproxyPtybliResponse(txnAmountupipsp,externalSerialNo ,bankRrn, callbackpaymentInstrument );
        JsonPath callbackres = successresponse.execute().jsonPath();
        Assertions.assertThat(callbackres.getString("body.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(callbackres.getString("body.resultMsg")).isEqualTo("success");

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PTYBLI.toString())
                .validateMid(mid)
                .AssertAll();

        Assertions.assertThat(txnStatus.execute().getBody().prettyPrint()).contains("UPI_PPIWALLET");
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59551")
    @Test(description = "validate Upi Collect PCF and platform Upi Psp Consult for mdr Upi Savings")
    public void validateUpiCollectPCFandplatformUpiPspConsultformdrUpiSavings() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.PCF_PLATFORM_FEE_ON_UPI_SUBTYPE ;
        String mid = Constants.MerchantType.PCF_PLATFORM_FEE_ON_UPI_SUBTYPE.getId();
        String txnAmount = "200";
        String txnAmountupipsp = "204.72";
        String paymentFee = "0";
        String payeeVPA = "paytm-9759417325@ptybl";
        String payerPaymentInstrument = "UPI_SAVINGS";
        String subAccountTypeValue = "";

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setAuthMode("USRPWD")
                .setPayerAccount("9759417329@paytm")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        String jsonResponse  = ptcResponse.toString();

        JSONObject root = new JSONObject(jsonResponse);
        String externalSerialNo = root.getJSONObject("body")
                .getJSONObject("bankForm")
                .getJSONObject("redirectForm")
                .getJSONObject("content")
                .getString("externalSrNo");

        UpiPspConsultFee upiPspConsultFeeobject =new UpiPspConsultFee( mid,  txnAmountupipsp, paymentFee ,  payeeVPA,  externalSerialNo ,  payerPaymentInstrument,  subAccountTypeValue) ;
        JsonPath responseJsonPath = upiPspConsultFeeobject.execute().jsonPath();
        Assertions.assertThat(responseJsonPath.getString("body.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(responseJsonPath.getString("body.resultCodeId")).isEqualTo("001");
        Assertions.assertThat(responseJsonPath.getString("body.resultMsg")).isEqualTo("success");
        Assertions.assertThat(responseJsonPath.getString("body.mid")).isEqualTo(mid);
        Assertions.assertThat(responseJsonPath.getString("body.txnAmount")).isEqualTo(txnAmountupipsp);
        Assertions.assertThat(responseJsonPath.getString("body.externalSerialNo")).isEqualTo(externalSerialNo);
        Assertions.assertThat(responseJsonPath.getString("body.payerPaymentInstrument")).isEqualTo(payerPaymentInstrument);
        Assertions.assertThat(responseJsonPath.getString("body.convenienceFeeValidation")).isEqualTo("MATCH");

        NativeHelpers.submitJsonFormInBrowser(ptcResponse.toString());
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(mid)
                .validateOrderId(orderId)
                .validatePaymentMode("UPI")
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.PTYBLC.toString())
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PTYBLC.toString())
                .validateMid(mid)
                .AssertAll();
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59551")
    @Test(description = "validate Upi Collect PCF and platform Upi Psp Consult for mdr Upi Ppi Wallet")
    public void validateUpiCollectPCFandplatformUpiPspConsultformdrUpiPpiWallet() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.PCF_PLATFORM_FEE_ON_UPI_SUBTYPE ;
        String mid = Constants.MerchantType.PCF_PLATFORM_FEE_ON_UPI_SUBTYPE.getId();
        String txnAmount = "300";
        String txnAmountupipsp = "307.08";
        String paymentFee = "0";
        String payeeVPA = "paytm-9759417325@ptybl";
        String payerPaymentInstrument = "PPI_WALLET";
        String subAccountTypeValue = "";

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setAuthMode("USRPWD")
                .setPayerAccount("9759417329@paytm")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        String jsonResponse  = ptcResponse.toString();

        JSONObject root = new JSONObject(jsonResponse);
        String externalSerialNo = root.getJSONObject("body")
                .getJSONObject("bankForm")
                .getJSONObject("redirectForm")
                .getJSONObject("content")
                .getString("externalSrNo");

        UpiPspConsultFee upiPspConsultFeeobject =new UpiPspConsultFee( mid,  txnAmountupipsp, paymentFee ,  payeeVPA,  externalSerialNo ,  payerPaymentInstrument,  subAccountTypeValue) ;
        JsonPath responseJsonPath = upiPspConsultFeeobject.execute().jsonPath();
        Assertions.assertThat(responseJsonPath.getString("body.resultCode")).isEqualTo("FAIL");
        Assertions.assertThat(responseJsonPath.getString("body.resultCodeId")).isEqualTo("009");
        Assertions.assertThat(responseJsonPath.getString("body.resultMsg")).isEqualTo("Fee mismatch");
        Assertions.assertThat(responseJsonPath.getString("body.mid")).isEqualTo(mid);
        Assertions.assertThat(responseJsonPath.getString("body.txnAmount")).isEqualTo(txnAmountupipsp);
        Assertions.assertThat(responseJsonPath.getString("body.externalSerialNo")).isEqualTo(externalSerialNo);
        Assertions.assertThat(responseJsonPath.getString("body.subResultCodeId")).isEqualTo("15008");
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59551")
    @Test(description = "validate Upi Collect PCF and platform Upi Psp Consult for mdr Upi Credit Card")
    public void validateUpiCollectPCFandplatformUpiPspConsultformdrUpiCreditCard() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.PCF_PLATFORM_FEE_ON_UPI_SUBTYPE ;
        String mid = Constants.MerchantType.PCF_PLATFORM_FEE_ON_UPI_SUBTYPE.getId();
        String txnAmount = "310";
        String txnAmountupipsp = "317.32";
        String paymentFee = "0";
        String payeeVPA = "paytm-9759417325@ptybl";
        String payerPaymentInstrument = "UPI_CREDIT_CARD";
        String subAccountTypeValue = "";

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setAuthMode("USRPWD")
                .setPayerAccount("9759417329@paytm")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        String jsonResponse  = ptcResponse.toString();

        JSONObject root = new JSONObject(jsonResponse);
        String externalSerialNo = root.getJSONObject("body")
                .getJSONObject("bankForm")
                .getJSONObject("redirectForm")
                .getJSONObject("content")
                .getString("externalSrNo");

        UpiPspConsultFee upiPspConsultFeeobject =new UpiPspConsultFee( mid,  txnAmountupipsp, paymentFee ,  payeeVPA,  externalSerialNo ,  payerPaymentInstrument,  subAccountTypeValue) ;
        JsonPath responseJsonPath = upiPspConsultFeeobject.execute().jsonPath();
        Assertions.assertThat(responseJsonPath.getString("body.resultCode")).isEqualTo("FAIL");
        Assertions.assertThat(responseJsonPath.getString("body.resultCodeId")).isEqualTo("009");
        Assertions.assertThat(responseJsonPath.getString("body.resultMsg")).isEqualTo("Fee mismatch");
        Assertions.assertThat(responseJsonPath.getString("body.mid")).isEqualTo(mid);
        Assertions.assertThat(responseJsonPath.getString("body.txnAmount")).isEqualTo(txnAmountupipsp);
        Assertions.assertThat(responseJsonPath.getString("body.externalSerialNo")).isEqualTo(externalSerialNo);
        Assertions.assertThat(responseJsonPath.getString("body.subResultCodeId")).isEqualTo("15008");
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59551")
    @Test(description = "validate Upi Collect PCF and platform Upi Psp Consult for mdr Upi Credit Line")
    public void validateUpiCollectPCFandplatformUpiPspConsultformdrUpiCreditLine() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.PCF_PLATFORM_FEE_ON_UPI_SUBTYPE ;
        String mid = Constants.MerchantType.PCF_PLATFORM_FEE_ON_UPI_SUBTYPE.getId();
        String txnAmount = "320";
        String txnAmountupipsp = "327.55";
        String paymentFee = "0";
        String payeeVPA = "paytm-9759417325@ptybl";
        String payerPaymentInstrument = "UPI_CREDITLINE";
        String subAccountTypeValue = "CREDITLINE01";

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setAuthMode("USRPWD")
                .setPayerAccount("9759417329@paytm")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        String jsonResponse  = ptcResponse.toString();

        JSONObject root = new JSONObject(jsonResponse);
        String externalSerialNo = root.getJSONObject("body")
                .getJSONObject("bankForm")
                .getJSONObject("redirectForm")
                .getJSONObject("content")
                .getString("externalSrNo");

        UpiPspConsultFee upiPspConsultFeeobject =new UpiPspConsultFee( mid,  txnAmountupipsp, paymentFee ,  payeeVPA,  externalSerialNo ,  payerPaymentInstrument,  subAccountTypeValue) ;
        JsonPath responseJsonPath = upiPspConsultFeeobject.execute().jsonPath();
        Assertions.assertThat(responseJsonPath.getString("body.resultCode")).isEqualTo("FAIL");
        Assertions.assertThat(responseJsonPath.getString("body.resultCodeId")).isEqualTo("009");
        Assertions.assertThat(responseJsonPath.getString("body.resultMsg")).isEqualTo("Fee mismatch");
        Assertions.assertThat(responseJsonPath.getString("body.mid")).isEqualTo(mid);
        Assertions.assertThat(responseJsonPath.getString("body.txnAmount")).isEqualTo(txnAmountupipsp);
        Assertions.assertThat(responseJsonPath.getString("body.externalSerialNo")).isEqualTo(externalSerialNo);
        Assertions.assertThat(responseJsonPath.getString("body.subResultCodeId")).isEqualTo("15008");

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59551")
    @Test(description = "validate Upi  Intent MDR PCF Upi Psp Consult for mdr Upi Savings")
    public void validateUpiIntentMDRPCFUpiPspConsultformdrUpiSavings() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.MDR_PCF_FEE_ON_UPI_SUBTYPE ;
        String mid = Constants.MerchantType.MDR_PCF_FEE_ON_UPI_SUBTYPE.getId();
        String txnAmount = "200";
        String txnAmountupipsp = "200";
        String paymentFee = "0";
        String payeeVPA = "paytm-9759417323@ptybl";
        String payerPaymentInstrument = "UPI_SAVINGS";
        String callbackpaymentInstrument = "";
        String subAccountTypeValue = "";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("3D")
                .setRiskExtendInfo("userLBSLatitude:26.88|userLBSLongitude:81.01|userAgent:Mozilla/5.0 (Linux; Android 8.1.0; Redmi Note 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36|timeZone:Asia/Calcutta|operationType:PAYMENT|networkType:4g|businessFlow:JS_CHECKOUT|amount:2|language:en-US|screenResolution:393X786|deviceType:mobile|channelId:WAP|platform:mWeb|osType:Android|deviceModel:Redmi Note 5|browserType:Chrome|browserVersion:96.0.4664.104|osVersion:8.1.0|deviceManufacturer:Xiaomi")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        String deeplink = ptcResponse.getBody().getDeepLinkInfo().getDeepLink();
        String externalSerialNo = deeplink.substring(deeplink.indexOf("tr")+3,deeplink.indexOf("&am"));
        String bankRrn = CommonHelpers.generateOrderId();
        System.out.println(externalSerialNo);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        UpiPspConsultFee upiPspConsultFeeobject =new UpiPspConsultFee( mid,  txnAmountupipsp, paymentFee ,  payeeVPA,  externalSerialNo ,  payerPaymentInstrument,  subAccountTypeValue) ;
        JsonPath responseJsonPath = upiPspConsultFeeobject.execute().jsonPath();
        Assertions.assertThat(responseJsonPath.getString("body.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(responseJsonPath.getString("body.resultCodeId")).isEqualTo("001");
        Assertions.assertThat(responseJsonPath.getString("body.resultMsg")).isEqualTo("success");
        Assertions.assertThat(responseJsonPath.getString("body.mid")).isEqualTo(mid);
        Assertions.assertThat(responseJsonPath.getString("body.txnAmount")).isEqualTo(txnAmountupipsp);
        Assertions.assertThat(responseJsonPath.getString("body.externalSerialNo")).isEqualTo(externalSerialNo);
        Assertions.assertThat(responseJsonPath.getString("body.payerPaymentInstrument")).isEqualTo(payerPaymentInstrument);
        Assertions.assertThat(responseJsonPath.getString("body.convenienceFeeValidation")).isEqualTo("MATCH");

        InstaproxyPtybliResponse successresponse = new InstaproxyPtybliResponse(txnAmountupipsp,externalSerialNo ,bankRrn, callbackpaymentInstrument );
        JsonPath callbackres = successresponse.execute().jsonPath();
        Assertions.assertThat(callbackres.getString("body.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(callbackres.getString("body.resultMsg")).isEqualTo("success");

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PTYBLI.toString())
                .validateMid(mid)
                .AssertAll();
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59551")
    @Test(description = "validate Upi Intent MDR PCF Upi Psp Consult for mdr Upi Credit Card")
    public void validateUpiIntentMDRPCFUpiPspConsultformdrUpiCreditCard() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.MDR_PCF_FEE_ON_UPI_SUBTYPE ;
        String mid = Constants.MerchantType.MDR_PCF_FEE_ON_UPI_SUBTYPE.getId();
        String txnAmount = "200";
        String txnAmountupipsp = "204.72";
        String paymentFee = "4.72";
        String payeeVPA = "paytm-9759417323@ptybl";
        String payerPaymentInstrument = "UPI_CREDIT_CARD";
        String callbackpaymentInstrument = "CREDIT_CARD";
        String subAccountTypeValue = "";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("3D")
                .setRiskExtendInfo("userLBSLatitude:26.88|userLBSLongitude:81.01|userAgent:Mozilla/5.0 (Linux; Android 8.1.0; Redmi Note 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36|timeZone:Asia/Calcutta|operationType:PAYMENT|networkType:4g|businessFlow:JS_CHECKOUT|amount:2|language:en-US|screenResolution:393X786|deviceType:mobile|channelId:WAP|platform:mWeb|osType:Android|deviceModel:Redmi Note 5|browserType:Chrome|browserVersion:96.0.4664.104|osVersion:8.1.0|deviceManufacturer:Xiaomi")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        String deeplink = ptcResponse.getBody().getDeepLinkInfo().getDeepLink();
        String externalSerialNo = deeplink.substring(deeplink.indexOf("tr")+3,deeplink.indexOf("&am"));
        String bankRrn = CommonHelpers.generateOrderId();
        System.out.println(externalSerialNo);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        UpiPspConsultFee upiPspConsultFeeobject =new UpiPspConsultFee( mid,  txnAmountupipsp, paymentFee ,  payeeVPA,  externalSerialNo ,  payerPaymentInstrument,  subAccountTypeValue) ;
        JsonPath responseJsonPath = upiPspConsultFeeobject.execute().jsonPath();
        Assertions.assertThat(responseJsonPath.getString("body.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(responseJsonPath.getString("body.resultCodeId")).isEqualTo("001");
        Assertions.assertThat(responseJsonPath.getString("body.resultMsg")).isEqualTo("success");
        Assertions.assertThat(responseJsonPath.getString("body.mid")).isEqualTo(mid);
        Assertions.assertThat(responseJsonPath.getString("body.txnAmount")).isEqualTo(txnAmountupipsp);
        Assertions.assertThat(responseJsonPath.getString("body.externalSerialNo")).isEqualTo(externalSerialNo);
        Assertions.assertThat(responseJsonPath.getString("body.payerPaymentInstrument")).isEqualTo(payerPaymentInstrument);
        Assertions.assertThat(responseJsonPath.getString("body.convenienceFeeValidation")).isEqualTo("MATCH");

        InstaproxyPtybliResponse successresponse = new InstaproxyPtybliResponse(txnAmountupipsp,externalSerialNo ,bankRrn, callbackpaymentInstrument , "upiCcBin" );
        JsonPath callbackres = successresponse.execute().jsonPath();
        Assertions.assertThat(callbackres.getString("body.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(callbackres.getString("body.resultMsg")).isEqualTo("success");

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PTYBLI.toString())
                .validateMid(mid)
                .AssertAll();

        Assertions.assertThat(txnStatus.execute().getBody().prettyPrint()).contains("UPICC");
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59551")
    @Test(description = "validate Upi Intent MDR PCF Upi Psp Consult for mdr Upi Credit Line")
    public void validateUpiIntentMDRPCFUpiPspConsultformdrUpiCreditLine() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.MDR_PCF_FEE_ON_UPI_SUBTYPE ;
        String mid = Constants.MerchantType.MDR_PCF_FEE_ON_UPI_SUBTYPE.getId();
        String txnAmount = "200";
        String txnAmountupipsp = "204.72";
        String paymentFee = "4.72";
        String payeeVPA = "paytm-9759417323@ptybl";
        String payerPaymentInstrument = "UPI_CREDITLINE";
        String subAccountTypeValue = "CREDITLINE01";
        String callbackpaymentInstrument = "CREDITLINE_CREDITLINE01";

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("3D")
                .setRiskExtendInfo("userLBSLatitude:26.88|userLBSLongitude:81.01|userAgent:Mozilla/5.0 (Linux; Android 8.1.0; Redmi Note 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36|timeZone:Asia/Calcutta|operationType:PAYMENT|networkType:4g|businessFlow:JS_CHECKOUT|amount:2|language:en-US|screenResolution:393X786|deviceType:mobile|channelId:WAP|platform:mWeb|osType:Android|deviceModel:Redmi Note 5|browserType:Chrome|browserVersion:96.0.4664.104|osVersion:8.1.0|deviceManufacturer:Xiaomi")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        String deeplink = ptcResponse.getBody().getDeepLinkInfo().getDeepLink();
        String externalSerialNo = deeplink.substring(deeplink.indexOf("tr")+3,deeplink.indexOf("&am"));
        String bankRrn = CommonHelpers.generateOrderId();
        System.out.println(externalSerialNo);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        UpiPspConsultFee upiPspConsultFeeobject =new UpiPspConsultFee( mid,  txnAmountupipsp, paymentFee ,  payeeVPA,  externalSerialNo ,  payerPaymentInstrument,  subAccountTypeValue) ;
        JsonPath responseJsonPath = upiPspConsultFeeobject.execute().jsonPath();
        Assertions.assertThat(responseJsonPath.getString("body.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(responseJsonPath.getString("body.resultCodeId")).isEqualTo("001");
        Assertions.assertThat(responseJsonPath.getString("body.resultMsg")).isEqualTo("success");
        Assertions.assertThat(responseJsonPath.getString("body.mid")).isEqualTo(mid);
        Assertions.assertThat(responseJsonPath.getString("body.txnAmount")).isEqualTo(txnAmountupipsp);
        Assertions.assertThat(responseJsonPath.getString("body.externalSerialNo")).isEqualTo(externalSerialNo);
        Assertions.assertThat(responseJsonPath.getString("body.payerPaymentInstrument")).isEqualTo(payerPaymentInstrument);
        Assertions.assertThat(responseJsonPath.getString("body.convenienceFeeValidation")).isEqualTo("MATCH");

        InstaproxyPtybliResponse successresponse = new InstaproxyPtybliResponse(txnAmountupipsp,externalSerialNo ,bankRrn, callbackpaymentInstrument );
        JsonPath callbackres = successresponse.execute().jsonPath();
        Assertions.assertThat(callbackres.getString("body.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(callbackres.getString("body.resultMsg")).isEqualTo("success");

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PTYBLI.toString())
                .validateMid(mid)
                .AssertAll();

        Assertions.assertThat(txnStatus.execute().getBody().prettyPrint()).contains("UPICL");
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59551")
    @Test(description = "validate Upi Intent MDR PCF Upi Psp Consult for mdr Upi Ppi Wallet")
    public void validateUpiIntentMDRPCFUpiPspConsultformdrUpiPpiWallet() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.MDR_PCF_FEE_ON_UPI_SUBTYPE ;
        String mid = Constants.MerchantType.MDR_PCF_FEE_ON_UPI_SUBTYPE.getId();
        String txnAmount = "200";
        String txnAmountupipsp = "209.44";
        String paymentFee = "9.44";
        String payeeVPA = "paytm-9759417323@ptybl";
        String payerPaymentInstrument = "PPI_WALLET";
        String subAccountTypeValue = "";
        String callbackpaymentInstrument = "PPI_WALLET";

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("3D")
                .setRiskExtendInfo("userLBSLatitude:26.88|userLBSLongitude:81.01|userAgent:Mozilla/5.0 (Linux; Android 8.1.0; Redmi Note 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36|timeZone:Asia/Calcutta|operationType:PAYMENT|networkType:4g|businessFlow:JS_CHECKOUT|amount:2|language:en-US|screenResolution:393X786|deviceType:mobile|channelId:WAP|platform:mWeb|osType:Android|deviceModel:Redmi Note 5|browserType:Chrome|browserVersion:96.0.4664.104|osVersion:8.1.0|deviceManufacturer:Xiaomi")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        String deeplink = ptcResponse.getBody().getDeepLinkInfo().getDeepLink();
        String externalSerialNo = deeplink.substring(deeplink.indexOf("tr")+3,deeplink.indexOf("&am"));
        String bankRrn = CommonHelpers.generateOrderId();
        System.out.println(externalSerialNo);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        UpiPspConsultFee upiPspConsultFeeobject =new UpiPspConsultFee( mid,  txnAmountupipsp, paymentFee ,  payeeVPA,  externalSerialNo ,  payerPaymentInstrument,  subAccountTypeValue) ;
        JsonPath responseJsonPath = upiPspConsultFeeobject.execute().jsonPath();
        Assertions.assertThat(responseJsonPath.getString("body.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(responseJsonPath.getString("body.resultCodeId")).isEqualTo("001");
        Assertions.assertThat(responseJsonPath.getString("body.resultMsg")).isEqualTo("success");
        Assertions.assertThat(responseJsonPath.getString("body.mid")).isEqualTo(mid);
        Assertions.assertThat(responseJsonPath.getString("body.txnAmount")).isEqualTo(txnAmountupipsp);
        Assertions.assertThat(responseJsonPath.getString("body.externalSerialNo")).isEqualTo(externalSerialNo);
        Assertions.assertThat(responseJsonPath.getString("body.payerPaymentInstrument")).isEqualTo(payerPaymentInstrument);
        Assertions.assertThat(responseJsonPath.getString("body.convenienceFeeValidation")).isEqualTo("MATCH");

        InstaproxyPtybliResponse successresponse = new InstaproxyPtybliResponse(txnAmountupipsp,externalSerialNo ,bankRrn, callbackpaymentInstrument );
        JsonPath callbackres = successresponse.execute().jsonPath();
        Assertions.assertThat(callbackres.getString("body.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(callbackres.getString("body.resultMsg")).isEqualTo("success");

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PTYBLI.toString())
                .validateMid(mid)
                .AssertAll();

        Assertions.assertThat(txnStatus.execute().getBody().prettyPrint()).contains("UPI_PPIWALLET");
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59551")
    @Test(description = "validate Upi Collect MDR PCF Upi Psp Consult for mdr Upi Savings")
    public void validateUpiCollectMDRPCFUpiPspConsultformdrUpiSavings() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.MDR_PCF_FEE_ON_UPI_SUBTYPE ;
        String mid = Constants.MerchantType.MDR_PCF_FEE_ON_UPI_SUBTYPE.getId();
        String txnAmount = "200";
        String txnAmountupipsp = "200";
        String paymentFee = "0";
        String payeeVPA = "paytm-9759417323@ptybl";
        String payerPaymentInstrument = "UPI_SAVINGS";
        String subAccountTypeValue = "";

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setAuthMode("USRPWD")
                .setPayerAccount("9759417329@paytm")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        String jsonResponse  = ptcResponse.toString();

        JSONObject root = new JSONObject(jsonResponse);
        String externalSerialNo = root.getJSONObject("body")
                .getJSONObject("bankForm")
                .getJSONObject("redirectForm")
                .getJSONObject("content")
                .getString("externalSrNo");

        UpiPspConsultFee upiPspConsultFeeobject =new UpiPspConsultFee( mid,  txnAmountupipsp, paymentFee ,  payeeVPA,  externalSerialNo ,  payerPaymentInstrument,  subAccountTypeValue) ;
        JsonPath responseJsonPath = upiPspConsultFeeobject.execute().jsonPath();
        Assertions.assertThat(responseJsonPath.getString("body.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(responseJsonPath.getString("body.resultCodeId")).isEqualTo("001");
        Assertions.assertThat(responseJsonPath.getString("body.resultMsg")).isEqualTo("success");
        Assertions.assertThat(responseJsonPath.getString("body.mid")).isEqualTo(mid);
        Assertions.assertThat(responseJsonPath.getString("body.txnAmount")).isEqualTo(txnAmountupipsp);
        Assertions.assertThat(responseJsonPath.getString("body.externalSerialNo")).isEqualTo(externalSerialNo);
        Assertions.assertThat(responseJsonPath.getString("body.payerPaymentInstrument")).isEqualTo(payerPaymentInstrument);
        Assertions.assertThat(responseJsonPath.getString("body.convenienceFeeValidation")).isEqualTo("MATCH");

        NativeHelpers.submitJsonFormInBrowser(ptcResponse.toString());
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(mid)
                .validateOrderId(orderId)
                .validatePaymentMode("UPI")
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.PTYBLC.toString())
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PTYBLC.toString())
                .validateMid(mid)
                .AssertAll();
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59551")
    @Test(description = "validate Upi Collect MDR PCF Upi Psp Consult for mdr Upi Ppi Wallet")
    public void validateUpiCollectMDRPCFUpiPspConsultformdrUpiPpiWallet() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.MDR_PCF_FEE_ON_UPI_SUBTYPE ;
        String mid = Constants.MerchantType.MDR_PCF_FEE_ON_UPI_SUBTYPE.getId();
        String txnAmount = "205.01";
        String txnAmountupipsp = "205.01";
        String paymentFee = "0";
        String payeeVPA = "paytm-9759417323@ptybl";
        String payerPaymentInstrument = "PPI_WALLET";
        String subAccountTypeValue = "";

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),merchant).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setAuthMode("USRPWD")
                .setPayerAccount("9759417329@paytm")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        String jsonResponse  = ptcResponse.toString();

        JSONObject root = new JSONObject(jsonResponse);
        String externalSerialNo = root.getJSONObject("body")
                .getJSONObject("bankForm")
                .getJSONObject("redirectForm")
                .getJSONObject("content")
                .getString("externalSrNo");

        UpiPspConsultFee upiPspConsultFeeobject =new UpiPspConsultFee( mid,  txnAmountupipsp, paymentFee ,  payeeVPA,  externalSerialNo ,  payerPaymentInstrument,  subAccountTypeValue) ;
        JsonPath responseJsonPath = upiPspConsultFeeobject.execute().jsonPath();
        Assertions.assertThat(responseJsonPath.getString("body.resultCode")).isEqualTo("FAIL");
        Assertions.assertThat(responseJsonPath.getString("body.resultCodeId")).isEqualTo("009");
        Assertions.assertThat(responseJsonPath.getString("body.resultMsg")).isEqualTo("Fee mismatch");
        Assertions.assertThat(responseJsonPath.getString("body.mid")).isEqualTo(mid);
        Assertions.assertThat(responseJsonPath.getString("body.txnAmount")).isEqualTo(txnAmountupipsp);
        Assertions.assertThat(responseJsonPath.getString("body.externalSerialNo")).isEqualTo(externalSerialNo);
        Assertions.assertThat(responseJsonPath.getString("body.subResultCodeId")).isEqualTo("15008");

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59551")
    @Test(description = "validate Upi Collect MDR PCF Upi Psp Consult for mdr Upi Credit Card")
    public void validateUpiCollectMDRPCFUpiPspConsultformdrUpiCreditCard() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.MDR_PCF_FEE_ON_UPI_SUBTYPE ;
        String mid = Constants.MerchantType.MDR_PCF_FEE_ON_UPI_SUBTYPE.getId();
        String txnAmount = "205.02";
        String txnAmountupipsp = "205.02";
        String paymentFee = "0";
        String payeeVPA = "paytm-9759417323@ptybl";
        String payerPaymentInstrument = "UPI_CREDIT_CARD";
        String subAccountTypeValue = "";

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setAuthMode("USRPWD")
                .setPayerAccount("9759417329@paytm")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        String jsonResponse  = ptcResponse.toString();

        JSONObject root = new JSONObject(jsonResponse);
        String externalSerialNo = root.getJSONObject("body")
                .getJSONObject("bankForm")
                .getJSONObject("redirectForm")
                .getJSONObject("content")
                .getString("externalSrNo");

        UpiPspConsultFee upiPspConsultFeeobject =new UpiPspConsultFee( mid,  txnAmountupipsp, paymentFee ,  payeeVPA,  externalSerialNo ,  payerPaymentInstrument,  subAccountTypeValue) ;
        JsonPath responseJsonPath = upiPspConsultFeeobject.execute().jsonPath();
        Assertions.assertThat(responseJsonPath.getString("body.resultCode")).isEqualTo("FAIL");
        Assertions.assertThat(responseJsonPath.getString("body.resultCodeId")).isEqualTo("009");
        Assertions.assertThat(responseJsonPath.getString("body.resultMsg")).isEqualTo("Fee mismatch");
        Assertions.assertThat(responseJsonPath.getString("body.mid")).isEqualTo(mid);
        Assertions.assertThat(responseJsonPath.getString("body.txnAmount")).isEqualTo(txnAmountupipsp);
        Assertions.assertThat(responseJsonPath.getString("body.externalSerialNo")).isEqualTo(externalSerialNo);
        Assertions.assertThat(responseJsonPath.getString("body.subResultCodeId")).isEqualTo("15008");

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59551")
    @Test(description = "validate Upi Collect MDR PCF Upi Psp Consult for mdr Upi Credit Line")
    public void validateUpiCollectMDRPCFUpiPspConsultformdrUpiCreditLine() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.MDR_PCF_FEE_ON_UPI_SUBTYPE ;
        String mid = Constants.MerchantType.MDR_PCF_FEE_ON_UPI_SUBTYPE.getId();
        String txnAmount = "205.03";
        String txnAmountupipsp = "205.03";
        String paymentFee = "0";
        String payeeVPA = "paytm-9759417323@ptybl";
        String payerPaymentInstrument = "UPI_CREDITLINE";
        String subAccountTypeValue = "CREDITLINE01";

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setAuthMode("USRPWD")
                .setPayerAccount("9759417329@paytm")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        String jsonResponse  = ptcResponse.toString();

        JSONObject root = new JSONObject(jsonResponse);
        String externalSerialNo = root.getJSONObject("body")
                .getJSONObject("bankForm")
                .getJSONObject("redirectForm")
                .getJSONObject("content")
                .getString("externalSrNo");

        UpiPspConsultFee upiPspConsultFeeobject =new UpiPspConsultFee( mid,  txnAmountupipsp, paymentFee ,  payeeVPA,  externalSerialNo ,  payerPaymentInstrument,  subAccountTypeValue) ;
        JsonPath responseJsonPath = upiPspConsultFeeobject.execute().jsonPath();
        Assertions.assertThat(responseJsonPath.getString("body.resultCode")).isEqualTo("FAIL");
        Assertions.assertThat(responseJsonPath.getString("body.resultCodeId")).isEqualTo("009");
        Assertions.assertThat(responseJsonPath.getString("body.resultMsg")).isEqualTo("Fee mismatch");
        Assertions.assertThat(responseJsonPath.getString("body.mid")).isEqualTo(mid);
        Assertions.assertThat(responseJsonPath.getString("body.txnAmount")).isEqualTo(txnAmountupipsp);
        Assertions.assertThat(responseJsonPath.getString("body.externalSerialNo")).isEqualTo(externalSerialNo);
        Assertions.assertThat(responseJsonPath.getString("body.subResultCodeId")).isEqualTo("15008");
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59551")
    @Test(description = "validate  Upi Intent MDR PCF and platform upi Psp Consult for mdr Upi Savings")
    public void validateUpiIntentMDRPCFandplatformUpiPspConsultformdrUpiSavings() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.MDR_PCF_PLATFORM_FEE_ON_UPI_SUBTYPE ;
        String mid = Constants.MerchantType.MDR_PCF_PLATFORM_FEE_ON_UPI_SUBTYPE.getId();
        String txnAmount = "200";
        String txnAmountupipsp = "209.44";
        String paymentFee = "0";
        String payeeVPA = "paytm-9759417326@ptybl";
        String payerPaymentInstrument = "UPI_SAVINGS";
        String callbackpaymentInstrument = "";
        String subAccountTypeValue = "";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("3D")
                .setRiskExtendInfo("userLBSLatitude:26.88|userLBSLongitude:81.01|userAgent:Mozilla/5.0 (Linux; Android 8.1.0; Redmi Note 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36|timeZone:Asia/Calcutta|operationType:PAYMENT|networkType:4g|businessFlow:JS_CHECKOUT|amount:2|language:en-US|screenResolution:393X786|deviceType:mobile|channelId:WAP|platform:mWeb|osType:Android|deviceModel:Redmi Note 5|browserType:Chrome|browserVersion:96.0.4664.104|osVersion:8.1.0|deviceManufacturer:Xiaomi")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        String deeplink = ptcResponse.getBody().getDeepLinkInfo().getDeepLink();
        String externalSerialNo = deeplink.substring(deeplink.indexOf("tr")+3,deeplink.indexOf("&am"));
        String amAmount = deeplink.substring(deeplink.indexOf("&am")+4,deeplink.indexOf("&cu"));
        String bankRrn = CommonHelpers.generateOrderId();
        System.out.println(externalSerialNo);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        Assertions.assertThat(amAmount).isEqualTo("209.44");

        UpiPspConsultFee upiPspConsultFeeobject =new UpiPspConsultFee( mid,  txnAmountupipsp, paymentFee ,  payeeVPA,  externalSerialNo ,  payerPaymentInstrument,  subAccountTypeValue) ;
        JsonPath responseJsonPath = upiPspConsultFeeobject.execute().jsonPath();
        Assertions.assertThat(responseJsonPath.getString("body.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(responseJsonPath.getString("body.resultCodeId")).isEqualTo("001");
        Assertions.assertThat(responseJsonPath.getString("body.resultMsg")).isEqualTo("success");
        Assertions.assertThat(responseJsonPath.getString("body.mid")).isEqualTo(mid);
        Assertions.assertThat(responseJsonPath.getString("body.txnAmount")).isEqualTo(txnAmountupipsp);
        Assertions.assertThat(responseJsonPath.getString("body.externalSerialNo")).isEqualTo(externalSerialNo);
        Assertions.assertThat(responseJsonPath.getString("body.payerPaymentInstrument")).isEqualTo(payerPaymentInstrument);
        Assertions.assertThat(responseJsonPath.getString("body.convenienceFeeValidation")).isEqualTo("MATCH");

        InstaproxyPtybliResponse successresponse = new InstaproxyPtybliResponse(txnAmountupipsp,externalSerialNo ,bankRrn, callbackpaymentInstrument );
        JsonPath callbackres = successresponse.execute().jsonPath();
        Assertions.assertThat(callbackres.getString("body.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(callbackres.getString("body.resultMsg")).isEqualTo("success");

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PTYBLI.toString())
                .validateMid(mid)
                .AssertAll();
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59551")
    @Test(description = "validate Upi Intent MDR PCF and platform Upi Psp Consult for mdr Upi Credit Card")
    public void validateUpiIntentMDRPCFandplatformUpiPspConsultformdrUpiCreditCard() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.MDR_PCF_PLATFORM_FEE_ON_UPI_SUBTYPE ;
        String mid = Constants.MerchantType.MDR_PCF_PLATFORM_FEE_ON_UPI_SUBTYPE.getId();
        String txnAmount = "200";
        String txnAmountupipsp = "218.88";
        String paymentFee = "9.44";
        String payeeVPA = "paytm-9759417326@ptybl";
        String payerPaymentInstrument = "UPI_CREDIT_CARD";
        String callbackpaymentInstrument = "CREDIT_CARD";
        String subAccountTypeValue = "";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("3D")
                .setRiskExtendInfo("userLBSLatitude:26.88|userLBSLongitude:81.01|userAgent:Mozilla/5.0 (Linux; Android 8.1.0; Redmi Note 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36|timeZone:Asia/Calcutta|operationType:PAYMENT|networkType:4g|businessFlow:JS_CHECKOUT|amount:2|language:en-US|screenResolution:393X786|deviceType:mobile|channelId:WAP|platform:mWeb|osType:Android|deviceModel:Redmi Note 5|browserType:Chrome|browserVersion:96.0.4664.104|osVersion:8.1.0|deviceManufacturer:Xiaomi")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        String deeplink = ptcResponse.getBody().getDeepLinkInfo().getDeepLink();
        String externalSerialNo = deeplink.substring(deeplink.indexOf("tr")+3,deeplink.indexOf("&am"));
        String bankRrn = CommonHelpers.generateOrderId();
        System.out.println(externalSerialNo);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        UpiPspConsultFee upiPspConsultFeeobject =new UpiPspConsultFee( mid,  txnAmountupipsp, paymentFee ,  payeeVPA,  externalSerialNo ,  payerPaymentInstrument,  subAccountTypeValue) ;
        JsonPath responseJsonPath = upiPspConsultFeeobject.execute().jsonPath();
        Assertions.assertThat(responseJsonPath.getString("body.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(responseJsonPath.getString("body.resultCodeId")).isEqualTo("001");
        Assertions.assertThat(responseJsonPath.getString("body.resultMsg")).isEqualTo("success");
        Assertions.assertThat(responseJsonPath.getString("body.mid")).isEqualTo(mid);
        Assertions.assertThat(responseJsonPath.getString("body.txnAmount")).isEqualTo(txnAmountupipsp);
        Assertions.assertThat(responseJsonPath.getString("body.externalSerialNo")).isEqualTo(externalSerialNo);
        Assertions.assertThat(responseJsonPath.getString("body.payerPaymentInstrument")).isEqualTo(payerPaymentInstrument);
        Assertions.assertThat(responseJsonPath.getString("body.convenienceFeeValidation")).isEqualTo("MATCH");

        InstaproxyPtybliResponse successresponse = new InstaproxyPtybliResponse(txnAmountupipsp,externalSerialNo ,bankRrn, callbackpaymentInstrument , "upiCcBin" );
        JsonPath callbackres = successresponse.execute().jsonPath();
        Assertions.assertThat(callbackres.getString("body.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(callbackres.getString("body.resultMsg")).isEqualTo("success");

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PTYBLI.toString())
                .validateMid(mid)
                .AssertAll();

        Assertions.assertThat(txnStatus.execute().getBody().prettyPrint()).contains("UPICC");
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59551")
    @Test(description = "validate Upi Intent MDR PCF and platform Upi Psp Consult for mdr Upi Credit Line")
    public void validateUpiIntentMDRPCFandplatformUpiPspConsultformdrUpiCreditLine() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.MDR_PCF_PLATFORM_FEE_ON_UPI_SUBTYPE ;
        String mid = Constants.MerchantType.MDR_PCF_PLATFORM_FEE_ON_UPI_SUBTYPE.getId();
        String txnAmount = "200";
        String txnAmountupipsp = "218.88";
        String paymentFee = "9.44";
        String payeeVPA = "paytm-9759417326@ptybl";
        String payerPaymentInstrument = "UPI_CREDITLINE";
        String subAccountTypeValue = "CREDITLINE01";
        String callbackpaymentInstrument = "CREDITLINE_CREDITLINE01";

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("3D")
                .setRiskExtendInfo("userLBSLatitude:26.88|userLBSLongitude:81.01|userAgent:Mozilla/5.0 (Linux; Android 8.1.0; Redmi Note 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36|timeZone:Asia/Calcutta|operationType:PAYMENT|networkType:4g|businessFlow:JS_CHECKOUT|amount:2|language:en-US|screenResolution:393X786|deviceType:mobile|channelId:WAP|platform:mWeb|osType:Android|deviceModel:Redmi Note 5|browserType:Chrome|browserVersion:96.0.4664.104|osVersion:8.1.0|deviceManufacturer:Xiaomi")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        String deeplink = ptcResponse.getBody().getDeepLinkInfo().getDeepLink();
        String externalSerialNo = deeplink.substring(deeplink.indexOf("tr")+3,deeplink.indexOf("&am"));
        String bankRrn = CommonHelpers.generateOrderId();
        System.out.println(externalSerialNo);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        UpiPspConsultFee upiPspConsultFeeobject =new UpiPspConsultFee( mid, txnAmountupipsp , paymentFee ,  payeeVPA,  externalSerialNo ,  payerPaymentInstrument,  subAccountTypeValue) ;
        JsonPath responseJsonPath = upiPspConsultFeeobject.execute().jsonPath();
        Assertions.assertThat(responseJsonPath.getString("body.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(responseJsonPath.getString("body.resultCodeId")).isEqualTo("001");
        Assertions.assertThat(responseJsonPath.getString("body.resultMsg")).isEqualTo("success");
        Assertions.assertThat(responseJsonPath.getString("body.mid")).isEqualTo(mid);
        Assertions.assertThat(responseJsonPath.getString("body.txnAmount")).isEqualTo(txnAmountupipsp);
        Assertions.assertThat(responseJsonPath.getString("body.externalSerialNo")).isEqualTo(externalSerialNo);
        Assertions.assertThat(responseJsonPath.getString("body.payerPaymentInstrument")).isEqualTo(payerPaymentInstrument);
        Assertions.assertThat(responseJsonPath.getString("body.convenienceFeeValidation")).isEqualTo("MATCH");

        InstaproxyPtybliResponse successresponse = new InstaproxyPtybliResponse(txnAmountupipsp,externalSerialNo ,bankRrn, callbackpaymentInstrument );
        JsonPath callbackres = successresponse.execute().jsonPath();
        Assertions.assertThat(callbackres.getString("body.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(callbackres.getString("body.resultMsg")).isEqualTo("success");

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PTYBLI.toString())
                .validateMid(mid)
                .AssertAll();

        Assertions.assertThat(txnStatus.execute().getBody().prettyPrint()).contains("UPICL");
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59551")
    @Test(description = "validate Upi Intent MDR PCF and platform Upi Psp Consult for mdr Upi Ppi Wallet")
    public void validateUpiIntentMDRPCFandplatformUpiPspConsultformdrUpiPpiWallet() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.MDR_PCF_PLATFORM_FEE_ON_UPI_SUBTYPE ;
        String mid = Constants.MerchantType.MDR_PCF_PLATFORM_FEE_ON_UPI_SUBTYPE.getId();
        String txnAmount = "200";
        String txnAmountupipsp = "209.44";
        String paymentFee = "";
        String payeeVPA = "paytm-9759417326@ptybl";
        String payerPaymentInstrument = "PPI_WALLET";
        String subAccountTypeValue = "";
        String callbackpaymentInstrument = "PPI_WALLET";

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("3D")
                .setRiskExtendInfo("userLBSLatitude:26.88|userLBSLongitude:81.01|userAgent:Mozilla/5.0 (Linux; Android 8.1.0; Redmi Note 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36|timeZone:Asia/Calcutta|operationType:PAYMENT|networkType:4g|businessFlow:JS_CHECKOUT|amount:2|language:en-US|screenResolution:393X786|deviceType:mobile|channelId:WAP|platform:mWeb|osType:Android|deviceModel:Redmi Note 5|browserType:Chrome|browserVersion:96.0.4664.104|osVersion:8.1.0|deviceManufacturer:Xiaomi")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        String deeplink = ptcResponse.getBody().getDeepLinkInfo().getDeepLink();
        String externalSerialNo = deeplink.substring(deeplink.indexOf("tr")+3,deeplink.indexOf("&am"));
        String bankRrn = CommonHelpers.generateOrderId();
        System.out.println(externalSerialNo);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        UpiPspConsultFee upiPspConsultFeeobject =new UpiPspConsultFee( mid,  txnAmountupipsp, paymentFee ,  payeeVPA,  externalSerialNo ,  payerPaymentInstrument,  subAccountTypeValue) ;
        JsonPath responseJsonPath = upiPspConsultFeeobject.execute().jsonPath();
        Assertions.assertThat(responseJsonPath.getString("body.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(responseJsonPath.getString("body.resultCodeId")).isEqualTo("001");
        Assertions.assertThat(responseJsonPath.getString("body.resultMsg")).isEqualTo("success");
        Assertions.assertThat(responseJsonPath.getString("body.mid")).isEqualTo(mid);
        Assertions.assertThat(responseJsonPath.getString("body.txnAmount")).isEqualTo(txnAmountupipsp);
        Assertions.assertThat(responseJsonPath.getString("body.externalSerialNo")).isEqualTo(externalSerialNo);
        Assertions.assertThat(responseJsonPath.getString("body.payerPaymentInstrument")).isEqualTo(payerPaymentInstrument);
        Assertions.assertThat(responseJsonPath.getString("body.convenienceFeeValidation")).isEqualTo("MATCH");

        InstaproxyPtybliResponse successresponse = new InstaproxyPtybliResponse(txnAmountupipsp,externalSerialNo ,bankRrn, callbackpaymentInstrument );
        JsonPath callbackres = successresponse.execute().jsonPath();
        Assertions.assertThat(callbackres.getString("body.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(callbackres.getString("body.resultMsg")).isEqualTo("success");

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PTYBLI.toString())
                .validateMid(mid)
                .AssertAll();

        Assertions.assertThat(txnStatus.execute().getBody().prettyPrint()).contains("UPI_PPIWALLET");
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59551")
    @Test(description = "validate Upi Collect MDR PCF and platform Upi Psp Consult for mdr Upi Savings")
    public void validateUpiCollectMDRPCFandplatformUpiPspConsultformdrUpiSavings() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.MDR_PCF_PLATFORM_FEE_ON_UPI_SUBTYPE ;
        String mid = Constants.MerchantType.MDR_PCF_PLATFORM_FEE_ON_UPI_SUBTYPE.getId();
        String txnAmount = "200";
        String txnAmountupipsp = "209.44";
        String paymentFee = "0";
        String payeeVPA = "paytm-9759417326@ptybl";
        String payerPaymentInstrument = "UPI_SAVINGS";
        String subAccountTypeValue = "";

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setAuthMode("USRPWD")
                .setPayerAccount("9759417329@paytm")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        String jsonResponse  = ptcResponse.toString();

        JSONObject root = new JSONObject(jsonResponse);
        String externalSerialNo = root.getJSONObject("body")
                .getJSONObject("bankForm")
                .getJSONObject("redirectForm")
                .getJSONObject("content")
                .getString("externalSrNo");

        UpiPspConsultFee upiPspConsultFeeobject =new UpiPspConsultFee( mid,  txnAmountupipsp, paymentFee ,  payeeVPA,  externalSerialNo ,  payerPaymentInstrument,  subAccountTypeValue) ;
        JsonPath responseJsonPath = upiPspConsultFeeobject.execute().jsonPath();
        Assertions.assertThat(responseJsonPath.getString("body.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(responseJsonPath.getString("body.resultCodeId")).isEqualTo("001");
        Assertions.assertThat(responseJsonPath.getString("body.resultMsg")).isEqualTo("success");
        Assertions.assertThat(responseJsonPath.getString("body.mid")).isEqualTo(mid);
        Assertions.assertThat(responseJsonPath.getString("body.txnAmount")).isEqualTo(txnAmountupipsp);
        Assertions.assertThat(responseJsonPath.getString("body.externalSerialNo")).isEqualTo(externalSerialNo);
        Assertions.assertThat(responseJsonPath.getString("body.payerPaymentInstrument")).isEqualTo(payerPaymentInstrument);
        Assertions.assertThat(responseJsonPath.getString("body.convenienceFeeValidation")).isEqualTo("MATCH");

        NativeHelpers.submitJsonFormInBrowser(ptcResponse.toString());
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(mid)
                .validateOrderId(orderId)
                .validatePaymentMode("UPI")
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.PTYBLC.toString())
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PTYBLC.toString())
                .validateMid(mid)
                .AssertAll();
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59551")
    @Test(description = "validate Upi Collect MDR PCF and platform Upi Psp Consult for mdr Upi Ppi Wallet")
    public void validateUpiCollectMDRPCFandplatformUpiPspConsultformdrUpiPpiWallet() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.MDR_PCF_PLATFORM_FEE_ON_UPI_SUBTYPE ;
        String mid = Constants.MerchantType.MDR_PCF_PLATFORM_FEE_ON_UPI_SUBTYPE.getId();
        String txnAmount = "300";
        String txnAmountupipsp = "314.16";
        String paymentFee = "0";
        String payeeVPA = "paytm-9759417326@ptybl";
        String payerPaymentInstrument = "PPI_WALLET";
        String subAccountTypeValue = "";

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setAuthMode("USRPWD")
                .setPayerAccount("9759417329@paytm")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        String jsonResponse  = ptcResponse.toString();

        JSONObject root = new JSONObject(jsonResponse);
        String externalSerialNo = root.getJSONObject("body")
                .getJSONObject("bankForm")
                .getJSONObject("redirectForm")
                .getJSONObject("content")
                .getString("externalSrNo");

        UpiPspConsultFee upiPspConsultFeeobject =new UpiPspConsultFee( mid,  txnAmountupipsp, paymentFee ,  payeeVPA,  externalSerialNo ,  payerPaymentInstrument,  subAccountTypeValue) ;
        JsonPath responseJsonPath = upiPspConsultFeeobject.execute().jsonPath();
        Assertions.assertThat(responseJsonPath.getString("body.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(responseJsonPath.getString("body.resultCodeId")).isEqualTo("001");
        Assertions.assertThat(responseJsonPath.getString("body.resultMsg")).isEqualTo("success");
        Assertions.assertThat(responseJsonPath.getString("body.mid")).isEqualTo(mid);
        Assertions.assertThat(responseJsonPath.getString("body.txnAmount")).isEqualTo(txnAmountupipsp);
        Assertions.assertThat(responseJsonPath.getString("body.externalSerialNo")).isEqualTo(externalSerialNo);
        Assertions.assertThat(responseJsonPath.getString("body.payerPaymentInstrument")).isEqualTo(payerPaymentInstrument);
        Assertions.assertThat(responseJsonPath.getString("body.convenienceFeeValidation")).isEqualTo("MATCH");
        NativeHelpers.submitJsonFormInBrowser(ptcResponse.toString());
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(mid)
                .validateOrderId(orderId)
                .validatePaymentMode("UPI")
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.PTYBLC.toString())
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PTYBLC.toString())
                .validateMid(mid)
                .AssertAll();

        Assertions.assertThat(txnStatus.execute().getBody().prettyPrint()).contains("UPI_PPIWALLET");

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59551")
    @Test(description = "validate Upi Collect MDR PCF and platform Upi Psp Consult for mdr Upi Credit Card")
    public void validateUpiCollectMDRPCFandplatformUpiPspConsultformdrUpiCreditCard() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.MDR_PCF_PLATFORM_FEE_ON_UPI_SUBTYPE ;
        String mid = Constants.MerchantType.MDR_PCF_PLATFORM_FEE_ON_UPI_SUBTYPE.getId();
        String txnAmount = "310";
        String txnAmountupipsp = "324.63";
        String paymentFee = "0";
        String payeeVPA = "paytm-9759417326@ptybl";
        String payerPaymentInstrument = "UPI_CREDIT_CARD";
        String subAccountTypeValue = "";

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setAuthMode("USRPWD")
                .setPayerAccount("9759417329@paytm")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        String jsonResponse  = ptcResponse.toString();

        JSONObject root = new JSONObject(jsonResponse);
        String externalSerialNo = root.getJSONObject("body")
                .getJSONObject("bankForm")
                .getJSONObject("redirectForm")
                .getJSONObject("content")
                .getString("externalSrNo");

        UpiPspConsultFee upiPspConsultFeeobject =new UpiPspConsultFee( mid,  txnAmountupipsp, paymentFee ,  payeeVPA,  externalSerialNo ,  payerPaymentInstrument,  subAccountTypeValue) ;
        JsonPath responseJsonPath = upiPspConsultFeeobject.execute().jsonPath();
        Assertions.assertThat(responseJsonPath.getString("body.resultCode")).isEqualTo("FAIL");
        Assertions.assertThat(responseJsonPath.getString("body.resultCodeId")).isEqualTo("009");
        Assertions.assertThat(responseJsonPath.getString("body.resultMsg")).isEqualTo("Fee mismatch");
        Assertions.assertThat(responseJsonPath.getString("body.mid")).isEqualTo(mid);
        Assertions.assertThat(responseJsonPath.getString("body.txnAmount")).isEqualTo(txnAmountupipsp);
        Assertions.assertThat(responseJsonPath.getString("body.externalSerialNo")).isEqualTo(externalSerialNo);
        Assertions.assertThat(responseJsonPath.getString("body.subResultCodeId")).isEqualTo("15008");

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59551")
    @Test(description = "validate Upi Collect MDR PCF and platform Upi Psp Consult for mdr Upi Credit Line")
    public void validateUpiCollectMDRPCFandplatformUpiPspConsultformdrUpiCreditLine() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.MDR_PCF_PLATFORM_FEE_ON_UPI_SUBTYPE ;
        String mid = Constants.MerchantType.MDR_PCF_PLATFORM_FEE_ON_UPI_SUBTYPE.getId();
        String txnAmount = "320";
        String txnAmountupipsp = "335.10";
        String paymentFee = "";
        String payeeVPA = "paytm-9759417326@ptybl";
        String payerPaymentInstrument = "UPI_CREDITLINE";
        String subAccountTypeValue = "CREDITLINE01";

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setAuthMode("USRPWD")
                .setPayerAccount("9759417329@paytm")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        String jsonResponse  = ptcResponse.toString();

        JSONObject root = new JSONObject(jsonResponse);
        String externalSerialNo = root.getJSONObject("body")
                .getJSONObject("bankForm")
                .getJSONObject("redirectForm")
                .getJSONObject("content")
                .getString("externalSrNo");

        UpiPspConsultFee upiPspConsultFeeobject =new UpiPspConsultFee( mid,  txnAmountupipsp, paymentFee ,  payeeVPA,  externalSerialNo ,  payerPaymentInstrument,  subAccountTypeValue) ;
        JsonPath responseJsonPath = upiPspConsultFeeobject.execute().jsonPath();
        Assertions.assertThat(responseJsonPath.getString("body.resultCode")).isEqualTo("FAIL");
        Assertions.assertThat(responseJsonPath.getString("body.resultCodeId")).isEqualTo("009");
        Assertions.assertThat(responseJsonPath.getString("body.resultMsg")).isEqualTo("Fee mismatch");
        Assertions.assertThat(responseJsonPath.getString("body.mid")).isEqualTo(mid);
        Assertions.assertThat(responseJsonPath.getString("body.txnAmount")).isEqualTo(txnAmountupipsp);
        Assertions.assertThat(responseJsonPath.getString("body.externalSerialNo")).isEqualTo(externalSerialNo);
        Assertions.assertThat(responseJsonPath.getString("body.subResultCodeId")).isEqualTo("15008");
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59551")
    @Test(description = "validate UPI Intent Upi Psp Consult for mdr Upi Voucher")
    public void validateUpiIntentUpiPspConsultformdrUpiVoucher() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String mid = Constants.MerchantType.MDR_FEE_ON_UPI_SUBTYPE.getId();
        String txnAmount = "200";
        String txnAmountupipsp = "200";
        String paymentFee = "0";
        String payeeVPA = "paytm-9759417321@ptybl";
        String payerPaymentInstrument = "UPI_VOUCHER";
        String callbackpaymentInstrument = "VOUCHER";
        String subAccountTypeValue = "";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.MDR_FEE_ON_UPI_SUBTYPE).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("3D")
                .setRiskExtendInfo("userLBSLatitude:26.88|userLBSLongitude:81.01|userAgent:Mozilla/5.0 (Linux; Android 8.1.0; Redmi Note 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36|timeZone:Asia/Calcutta|operationType:PAYMENT|networkType:4g|businessFlow:JS_CHECKOUT|amount:2|language:en-US|screenResolution:393X786|deviceType:mobile|channelId:WAP|platform:mWeb|osType:Android|deviceModel:Redmi Note 5|browserType:Chrome|browserVersion:96.0.4664.104|osVersion:8.1.0|deviceManufacturer:Xiaomi")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        String deeplink = ptcResponse.getBody().getDeepLinkInfo().getDeepLink();
        String externalSerialNo = deeplink.substring(deeplink.indexOf("tr")+3,deeplink.indexOf("&am"));
        String bankRrn = CommonHelpers.generateOrderId();
        System.out.println(externalSerialNo);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        UpiPspConsultFee upiPspConsultFeeobject =new UpiPspConsultFee( mid,  txnAmountupipsp, paymentFee ,  payeeVPA,  externalSerialNo ,  payerPaymentInstrument,  subAccountTypeValue) ;
        JsonPath responseJsonPath = upiPspConsultFeeobject.execute().jsonPath();
        Assertions.assertThat(responseJsonPath.getString("body.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(responseJsonPath.getString("body.resultCodeId")).isEqualTo("001");
        Assertions.assertThat(responseJsonPath.getString("body.resultMsg")).isEqualTo("success");
        Assertions.assertThat(responseJsonPath.getString("body.mid")).isEqualTo(mid);
        Assertions.assertThat(responseJsonPath.getString("body.txnAmount")).isEqualTo(txnAmountupipsp);
        Assertions.assertThat(responseJsonPath.getString("body.externalSerialNo")).isEqualTo(externalSerialNo);
        Assertions.assertThat(responseJsonPath.getString("body.payerPaymentInstrument")).isEqualTo(payerPaymentInstrument);
        Assertions.assertThat(responseJsonPath.getString("body.convenienceFeeValidation")).isEqualTo("MATCH");

        InstaproxyPtybliResponse successresponse = new InstaproxyPtybliResponse(txnAmountupipsp,externalSerialNo ,bankRrn, callbackpaymentInstrument );
        JsonPath callbackres = successresponse.execute().jsonPath();
        Assertions.assertThat(callbackres.getString("body.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(callbackres.getString("body.resultMsg")).isEqualTo("success");

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PTYBLI.toString())
                .validateMid(mid)
                .AssertAll();

        Assertions.assertThat(txnStatus.execute().getBody().prettyPrint()).contains("UPI_VOUCHER");
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate Theia UPI PSP Req Auth Flow in COTP")
    public void validateTheiaUPIPSPReqAuthFlowinCOTP() throws Exception {
        String txnAmount = "200.00";
        Constants.MerchantType merchant = Constants.MerchantType.MDR_FEE_ON_UPI_SUBTYPE;
        String mid = merchant.getId();
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.MDR_FEE_ON_UPI_SUBTYPE, "enhancedweb")
                .setTXN_AMOUNT(txnAmount)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage("enhancedweb");
        String orderId = orderDTO.getORDER_ID();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder("SEAMLESS_3D_FORM",merchant.getId(),orderId, txnAmount, "yuioty", "CREDITLINE_CREDITLINE01");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"CREDITLINE_CREDITLINE01");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.txnAmount", Matchers.equalTo(txnAmount))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .extract().as(StaticQrUpiPSPResponse.class);

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderId,"ACQUIRING_PAY_ORDER");
        Assertions.assertThat(logs).contains("\"authRequestType\":\"REQ_AUTH\"");
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59551")
    @Test(description = "validate Theia Upi Intent PTC Non ReqAuth Flow in COTP")
    public void validateTheiaUpiIntentPTCNonReqAuthFlowinCOTP() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String mid = Constants.MerchantType.MDR_FEE_ON_UPI_SUBTYPE.getId();
        String txnAmount = "200";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.MDR_FEE_ON_UPI_SUBTYPE).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("3D")
                .setRiskExtendInfo("userLBSLatitude:26.88|userLBSLongitude:81.01|userAgent:Mozilla/5.0 (Linux; Android 8.1.0; Redmi Note 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36|timeZone:Asia/Calcutta|operationType:PAYMENT|networkType:4g|businessFlow:JS_CHECKOUT|amount:2|language:en-US|screenResolution:393X786|deviceType:mobile|channelId:WAP|platform:mWeb|osType:Android|deviceModel:Redmi Note 5|browserType:Chrome|browserVersion:96.0.4664.104|osVersion:8.1.0|deviceManufacturer:Xiaomi")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderId,"ACQUIRING_PAY_ORDER");
        Assertions.assertThat(logs).doesNotContain("\"authRequestType\":\"REQ_AUTH\"");
    }


    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59551")
    @Test(description = "validate Theia Upi Collect PTC Non ReqAuth Flow in COTP")
    public void validateTheiaUpiCollectPTCNonReqAuthFlowinCOTP() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String mid = Constants.MerchantType.MDR_FEE_ON_UPI_SUBTYPE.getId();
        String txnAmount = "200";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),  Constants.MerchantType.MDR_FEE_ON_UPI_SUBTYPE).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setAuthMode("USRPWD")
                .setPayerAccount("9759417329@paytm")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderId,"ACQUIRING_PAY_ORDER");
        Assertions.assertThat(logs).doesNotContain("\"authRequestType\":\"REQ_AUTH\"");
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59551")
    @Test(description = "validate Upi Psp Consult Order Closed")
    public void validateUpiPspConsultOrderClosed() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String mid = Constants.MerchantType.MDR_FEE_ON_UPI_SUBTYPE.getId();
        String txnAmount = "200";
        String txnAmountupipsp = "200";
        String paymentFee = "0";
        String payeeVPA = "paytm-9759417321@ptybl";
        String payerPaymentInstrument = "UPI_SAVINGS";
        String subAccountTypeValue = "";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.MDR_FEE_ON_UPI_SUBTYPE).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("3D")
                .setRiskExtendInfo("userLBSLatitude:26.88|userLBSLongitude:81.01|userAgent:Mozilla/5.0 (Linux; Android 8.1.0; Redmi Note 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36|timeZone:Asia/Calcutta|operationType:PAYMENT|networkType:4g|businessFlow:JS_CHECKOUT|amount:2|language:en-US|screenResolution:393X786|deviceType:mobile|channelId:WAP|platform:mWeb|osType:Android|deviceModel:Redmi Note 5|browserType:Chrome|browserVersion:96.0.4664.104|osVersion:8.1.0|deviceManufacturer:Xiaomi")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        String deeplink = ptcResponse.getBody().getDeepLinkInfo().getDeepLink();
        String externalSerialNo = deeplink.substring(deeplink.indexOf("tr")+3,deeplink.indexOf("&am"));

        System.out.println(externalSerialNo);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        CloseOrderDTO closeOrderDTO = new CloseOrderDTO();
        closeOrderDTO.setHead(new com.paytm.dto.CloseOrder.Head().setChannelId("WAP").setVersion("v1").setSigature(""))
                .setBody(new com.paytm.dto.CloseOrder.Body().setIsForceClose(true).setOrderId(orderId).setMid(mid));
        CloseOrderAPI closeOrderAPI = new CloseOrderAPI(closeOrderDTO);
        closeOrderAPI.execute();

        UpiPspConsultFee upiPspConsultFeeobject =new UpiPspConsultFee( mid,  txnAmountupipsp, paymentFee ,  payeeVPA,  externalSerialNo ,  payerPaymentInstrument,  subAccountTypeValue) ;
        JsonPath responseJsonPath = upiPspConsultFeeobject.execute().jsonPath();
        Assertions.assertThat(responseJsonPath.getString("body.resultCode")).isEqualTo("FAIL");
        Assertions.assertThat(responseJsonPath.getString("body.resultCodeId")).isEqualTo("009");
        Assertions.assertThat(responseJsonPath.getString("body.resultMsg")).isEqualTo("Order is closed");
        Assertions.assertThat(responseJsonPath.getString("body.mid")).isEqualTo(mid);
        Assertions.assertThat(responseJsonPath.getString("body.txnAmount")).isEqualTo(txnAmountupipsp);
        Assertions.assertThat(responseJsonPath.getString("body.externalSerialNo")).isEqualTo(externalSerialNo);
        Assertions.assertThat(responseJsonPath.getString("body.subResultCodeId")).isEqualTo("15013");

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59551")
    @Test(description = "validate Upi Psp Consult Vpa does not exist")
    public void validateUpiPspConsultVpadoesnotexist() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String mid = Constants.MerchantType.MDR_FEE_ON_UPI_SUBTYPE.getId();
        String txnAmount = "200";
        String txnAmountupipsp = "200";
        String paymentFee = "0";
        String payeeVPA = "paytm-1234567898@ptybl";
        String payerPaymentInstrument = "UPI_SAVINGS";
        String subAccountTypeValue = "";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.MDR_FEE_ON_UPI_SUBTYPE).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("3D")
                .setRiskExtendInfo("userLBSLatitude:26.88|userLBSLongitude:81.01|userAgent:Mozilla/5.0 (Linux; Android 8.1.0; Redmi Note 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36|timeZone:Asia/Calcutta|operationType:PAYMENT|networkType:4g|businessFlow:JS_CHECKOUT|amount:2|language:en-US|screenResolution:393X786|deviceType:mobile|channelId:WAP|platform:mWeb|osType:Android|deviceModel:Redmi Note 5|browserType:Chrome|browserVersion:96.0.4664.104|osVersion:8.1.0|deviceManufacturer:Xiaomi")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        String deeplink = ptcResponse.getBody().getDeepLinkInfo().getDeepLink();
        String externalSerialNo = deeplink.substring(deeplink.indexOf("tr")+3,deeplink.indexOf("&am"));

        System.out.println(externalSerialNo);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        UpiPspConsultFee upiPspConsultFeeobject =new UpiPspConsultFee( mid,  txnAmountupipsp, paymentFee ,  payeeVPA,  externalSerialNo ,  payerPaymentInstrument,  subAccountTypeValue) ;
        JsonPath responseJsonPath = upiPspConsultFeeobject.execute().jsonPath();
        Assertions.assertThat(responseJsonPath.getString("body.resultCode")).isEqualTo("FAIL");
        Assertions.assertThat(responseJsonPath.getString("body.resultCodeId")).isEqualTo("006");
        Assertions.assertThat(responseJsonPath.getString("body.resultMsg")).isEqualTo("VPA does not exist");
        Assertions.assertThat(responseJsonPath.getString("body.mid")).isEqualTo(mid);
        Assertions.assertThat(responseJsonPath.getString("body.txnAmount")).isEqualTo(txnAmountupipsp);
        Assertions.assertThat(responseJsonPath.getString("body.externalSerialNo")).isEqualTo(externalSerialNo);
        Assertions.assertThat(responseJsonPath.getString("body.subResultCodeId")).isEqualTo("15006");

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59551")
    @Test(description = "validate Upi Psp Consult Order does not exist")
    public void validateUpiPspConsultOrderdoesnotexist() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String mid = Constants.MerchantType.MDR_FEE_ON_UPI_SUBTYPE.getId();
        String txnAmount = "200";
        String txnAmountupipsp = "200";
        String paymentFee = "0";
        String payeeVPA = "paytm-9759417321@ptybl";
        String payerPaymentInstrument = "UPI_SAVINGS";
        String subAccountTypeValue = "";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.MDR_FEE_ON_UPI_SUBTYPE).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("3D")
                .setRiskExtendInfo("userLBSLatitude:26.88|userLBSLongitude:81.01|userAgent:Mozilla/5.0 (Linux; Android 8.1.0; Redmi Note 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36|timeZone:Asia/Calcutta|operationType:PAYMENT|networkType:4g|businessFlow:JS_CHECKOUT|amount:2|language:en-US|screenResolution:393X786|deviceType:mobile|channelId:WAP|platform:mWeb|osType:Android|deviceModel:Redmi Note 5|browserType:Chrome|browserVersion:96.0.4664.104|osVersion:8.1.0|deviceManufacturer:Xiaomi")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        String deeplink = ptcResponse.getBody().getDeepLinkInfo().getDeepLink();
        String externalSerialNo = "5061724993685082a13";

        System.out.println(externalSerialNo);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        UpiPspConsultFee upiPspConsultFeeobject =new UpiPspConsultFee( mid,  txnAmountupipsp, paymentFee ,  payeeVPA,  externalSerialNo ,  payerPaymentInstrument,  subAccountTypeValue) ;
        JsonPath responseJsonPath = upiPspConsultFeeobject.execute().jsonPath();
        Assertions.assertThat(responseJsonPath.getString("body.resultCode")).isEqualTo("FAIL");
        Assertions.assertThat(responseJsonPath.getString("body.resultCodeId")).isEqualTo("006");
        Assertions.assertThat(responseJsonPath.getString("body.resultMsg")).isEqualTo("Order does not exist");
        Assertions.assertThat(responseJsonPath.getString("body.mid")).isEqualTo(mid);
        Assertions.assertThat(responseJsonPath.getString("body.txnAmount")).isEqualTo(txnAmountupipsp);
        Assertions.assertThat(responseJsonPath.getString("body.externalSerialNo")).isEqualTo(externalSerialNo);
        Assertions.assertThat(responseJsonPath.getString("body.subResultCodeId")).isEqualTo("15012");

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59551")
    @Test(description = "validate Upi Psp Consult txnAmount Invalid")
    public void validateUpiPspConsulttxnAmountInvalid() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String mid = Constants.MerchantType.MDR_FEE_ON_UPI_SUBTYPE.getId();
        String txnAmount = "200";
        String txnAmountupipsp = "201";
        String paymentFee = "0";
        String payeeVPA = "paytm-9759417321@ptybl";
        String payerPaymentInstrument = "UPI_SAVINGS";
        String subAccountTypeValue = "";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.MDR_FEE_ON_UPI_SUBTYPE).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("3D")
                .setRiskExtendInfo("userLBSLatitude:26.88|userLBSLongitude:81.01|userAgent:Mozilla/5.0 (Linux; Android 8.1.0; Redmi Note 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36|timeZone:Asia/Calcutta|operationType:PAYMENT|networkType:4g|businessFlow:JS_CHECKOUT|amount:2|language:en-US|screenResolution:393X786|deviceType:mobile|channelId:WAP|platform:mWeb|osType:Android|deviceModel:Redmi Note 5|browserType:Chrome|browserVersion:96.0.4664.104|osVersion:8.1.0|deviceManufacturer:Xiaomi")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        String deeplink = ptcResponse.getBody().getDeepLinkInfo().getDeepLink();
        String externalSerialNo = deeplink.substring(deeplink.indexOf("tr")+3,deeplink.indexOf("&am"));

        System.out.println(externalSerialNo);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        UpiPspConsultFee upiPspConsultFeeobject =new UpiPspConsultFee( mid,  txnAmountupipsp, paymentFee ,  payeeVPA,  externalSerialNo ,  payerPaymentInstrument,  subAccountTypeValue) ;
        JsonPath responseJsonPath = upiPspConsultFeeobject.execute().jsonPath();
        Assertions.assertThat(responseJsonPath.getString("body.resultCode")).isEqualTo("FAIL");
        Assertions.assertThat(responseJsonPath.getString("body.resultCodeId")).isEqualTo("009");
        Assertions.assertThat(responseJsonPath.getString("body.resultMsg")).isEqualTo("Invalid transaction amount");
        Assertions.assertThat(responseJsonPath.getString("body.mid")).isEqualTo(mid);
        Assertions.assertThat(responseJsonPath.getString("body.txnAmount")).isEqualTo(txnAmountupipsp);
        Assertions.assertThat(responseJsonPath.getString("body.externalSerialNo")).isEqualTo(externalSerialNo);
        Assertions.assertThat(responseJsonPath.getString("body.subResultCodeId")).isEqualTo("15010");
        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PTYBLI.toString())
                .validateMid(mid)
                .AssertAll();

        Assertions.assertThat(txnStatus.execute().getBody().prettyPrint()).contains("UPICX-UPI");

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59551")
    @Test(description = "validate Upi Psp Consult mid Invalid")
    public void validateUpiPspConsultmidInvalid() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String mid = Constants.MerchantType.MDR_FEE_ON_UPI_SUBTYPE.getId();
        String middiff = Constants.MerchantType.MDR_PLATFORM_FEE_ON_UPI_SUBTYPE.getId();
        String txnAmount = "200";
        String txnAmountupipsp = "201";
        String paymentFee = "0";
        String payeeVPA = "paytm-9759417321@ptybl";
        String payerPaymentInstrument = "UPI_SAVINGS";
        String subAccountTypeValue = "";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.MDR_FEE_ON_UPI_SUBTYPE).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("3D")
                .setRiskExtendInfo("userLBSLatitude:26.88|userLBSLongitude:81.01|userAgent:Mozilla/5.0 (Linux; Android 8.1.0; Redmi Note 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36|timeZone:Asia/Calcutta|operationType:PAYMENT|networkType:4g|businessFlow:JS_CHECKOUT|amount:2|language:en-US|screenResolution:393X786|deviceType:mobile|channelId:WAP|platform:mWeb|osType:Android|deviceModel:Redmi Note 5|browserType:Chrome|browserVersion:96.0.4664.104|osVersion:8.1.0|deviceManufacturer:Xiaomi")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        String deeplink = ptcResponse.getBody().getDeepLinkInfo().getDeepLink();
        String externalSerialNo = deeplink.substring(deeplink.indexOf("tr")+3,deeplink.indexOf("&am"));

        System.out.println(externalSerialNo);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        UpiPspConsultFee upiPspConsultFeeobject =new UpiPspConsultFee( middiff,  txnAmountupipsp, paymentFee ,  payeeVPA,  externalSerialNo ,  payerPaymentInstrument,  subAccountTypeValue) ;
        JsonPath responseJsonPath = upiPspConsultFeeobject.execute().jsonPath();
        Assertions.assertThat(responseJsonPath.getString("body.resultCode")).isEqualTo("FAIL");
        Assertions.assertThat(responseJsonPath.getString("body.resultCodeId")).isEqualTo("009");
        Assertions.assertThat(responseJsonPath.getString("body.resultMsg")).isEqualTo("MID is invalid");
        Assertions.assertThat(responseJsonPath.getString("body.mid")).isEqualTo(middiff);
        Assertions.assertThat(responseJsonPath.getString("body.txnAmount")).isEqualTo(txnAmountupipsp);
        Assertions.assertThat(responseJsonPath.getString("body.externalSerialNo")).isEqualTo(externalSerialNo);
        Assertions.assertThat(responseJsonPath.getString("body.subResultCodeId")).isEqualTo("15011");

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59551")
    @Test(description = "validate Upi Psp Consult Merchant Ineligible UPI_PPIWALLET")
    public void validateUpiPspConsultMerchantIneligibleUPIPPIWALLET() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String mid = Constants.MerchantType.MDR_PLATFORM_FEE_ON_UPI_SUBTYPE.getId();
        String txnAmount = "200";
        String txnAmountupipsp = "200";
        String paymentFee = "0";
        String payeeVPA = "paytm-9759417324@ptybl";
        String payerPaymentInstrument = "PPI_WALLET";
        String subAccountTypeValue = "";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.MDR_PLATFORM_FEE_ON_UPI_SUBTYPE).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("3D")
                .setRiskExtendInfo("userLBSLatitude:26.88|userLBSLongitude:81.01|userAgent:Mozilla/5.0 (Linux; Android 8.1.0; Redmi Note 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36|timeZone:Asia/Calcutta|operationType:PAYMENT|networkType:4g|businessFlow:JS_CHECKOUT|amount:2|language:en-US|screenResolution:393X786|deviceType:mobile|channelId:WAP|platform:mWeb|osType:Android|deviceModel:Redmi Note 5|browserType:Chrome|browserVersion:96.0.4664.104|osVersion:8.1.0|deviceManufacturer:Xiaomi")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        String deeplink = ptcResponse.getBody().getDeepLinkInfo().getDeepLink();
        String externalSerialNo = deeplink.substring(deeplink.indexOf("tr")+3,deeplink.indexOf("&am"));

        System.out.println(externalSerialNo);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        UpiPspConsultFee upiPspConsultFeeobject =new UpiPspConsultFee( mid,  txnAmountupipsp, paymentFee ,  payeeVPA,  externalSerialNo ,  payerPaymentInstrument,  subAccountTypeValue) ;
        JsonPath responseJsonPath = upiPspConsultFeeobject.execute().jsonPath();
        Assertions.assertThat(responseJsonPath.getString("body.resultCode")).isEqualTo("FAIL");
        Assertions.assertThat(responseJsonPath.getString("body.resultCodeId")).isEqualTo("009");
        Assertions.assertThat(responseJsonPath.getString("body.resultMsg")).isEqualTo("Merchant Ineligible UPI_PPIWALLET");
        Assertions.assertThat(responseJsonPath.getString("body.mid")).isEqualTo(mid);
        Assertions.assertThat(responseJsonPath.getString("body.txnAmount")).isEqualTo(txnAmountupipsp);
        Assertions.assertThat(responseJsonPath.getString("body.externalSerialNo")).isEqualTo(externalSerialNo);
        Assertions.assertThat(responseJsonPath.getString("body.subResultCodeId")).isEqualTo("003");

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59551")
    @Test(description = "validate Upi Psp Consult Merchant Ineligible UPI_CREDIT_CARD")
    public void validateUpiPspConsultMerchantIneligibleUPICREDITCARD() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String mid = Constants.MerchantType.MDR_PLATFORM_FEE_ON_UPI_SUBTYPE.getId();
        String txnAmount = "200";
        String txnAmountupipsp = "200";
        String paymentFee = "0";
        String payeeVPA = "paytm-9759417324@ptybl";
        String payerPaymentInstrument = "UPI_CREDIT_CARD";
        String subAccountTypeValue = "";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.MDR_PLATFORM_FEE_ON_UPI_SUBTYPE).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("3D")
                .setRiskExtendInfo("userLBSLatitude:26.88|userLBSLongitude:81.01|userAgent:Mozilla/5.0 (Linux; Android 8.1.0; Redmi Note 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36|timeZone:Asia/Calcutta|operationType:PAYMENT|networkType:4g|businessFlow:JS_CHECKOUT|amount:2|language:en-US|screenResolution:393X786|deviceType:mobile|channelId:WAP|platform:mWeb|osType:Android|deviceModel:Redmi Note 5|browserType:Chrome|browserVersion:96.0.4664.104|osVersion:8.1.0|deviceManufacturer:Xiaomi")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        String deeplink = ptcResponse.getBody().getDeepLinkInfo().getDeepLink();
        String externalSerialNo = deeplink.substring(deeplink.indexOf("tr")+3,deeplink.indexOf("&am"));

        System.out.println(externalSerialNo);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        UpiPspConsultFee upiPspConsultFeeobject =new UpiPspConsultFee( mid,  txnAmountupipsp, paymentFee ,  payeeVPA,  externalSerialNo ,  payerPaymentInstrument,  subAccountTypeValue) ;
        JsonPath responseJsonPath = upiPspConsultFeeobject.execute().jsonPath();
        Assertions.assertThat(responseJsonPath.getString("body.resultCode")).isEqualTo("FAIL");
        Assertions.assertThat(responseJsonPath.getString("body.resultCodeId")).isEqualTo("009");
        Assertions.assertThat(responseJsonPath.getString("body.resultMsg")).isEqualTo("Merchant Ineligible UPI_CREDIT_CARD");
        Assertions.assertThat(responseJsonPath.getString("body.mid")).isEqualTo(mid);
        Assertions.assertThat(responseJsonPath.getString("body.txnAmount")).isEqualTo(txnAmountupipsp);
        Assertions.assertThat(responseJsonPath.getString("body.externalSerialNo")).isEqualTo(externalSerialNo);
        Assertions.assertThat(responseJsonPath.getString("body.subResultCodeId")).isEqualTo("002");

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59551")
    @Test(description = "validate Upi Psp Consult Merchant does not support UPI_CREDITLINE  Interest free")
    public void validateUpiPspConsultMerchantdoesnotsupportUPICREDITLINEInterestFree() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String mid = Constants.MerchantType.MDR_PLATFORM_FEE_ON_UPI_SUBTYPE.getId();
        String txnAmount = "200";
        String txnAmountupipsp = "200";
        String paymentFee = "0";
        String payeeVPA = "paytm-9759417324@ptybl";
        String payerPaymentInstrument = "UPI_CREDITLINE";
        String subAccountTypeValue = "CREDITLINE01";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.MDR_PLATFORM_FEE_ON_UPI_SUBTYPE).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("3D")
                .setRiskExtendInfo("userLBSLatitude:26.88|userLBSLongitude:81.01|userAgent:Mozilla/5.0 (Linux; Android 8.1.0; Redmi Note 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36|timeZone:Asia/Calcutta|operationType:PAYMENT|networkType:4g|businessFlow:JS_CHECKOUT|amount:2|language:en-US|screenResolution:393X786|deviceType:mobile|channelId:WAP|platform:mWeb|osType:Android|deviceModel:Redmi Note 5|browserType:Chrome|browserVersion:96.0.4664.104|osVersion:8.1.0|deviceManufacturer:Xiaomi")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        String deeplink = ptcResponse.getBody().getDeepLinkInfo().getDeepLink();
        String externalSerialNo = deeplink.substring(deeplink.indexOf("tr")+3,deeplink.indexOf("&am"));

        System.out.println(externalSerialNo);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        UpiPspConsultFee upiPspConsultFeeobject =new UpiPspConsultFee( mid,  txnAmountupipsp, paymentFee ,  payeeVPA,  externalSerialNo ,  payerPaymentInstrument,  subAccountTypeValue) ;
        JsonPath responseJsonPath = upiPspConsultFeeobject.execute().jsonPath();
        Assertions.assertThat(responseJsonPath.getString("body.resultCode")).isEqualTo("FAIL");
        Assertions.assertThat(responseJsonPath.getString("body.resultCodeId")).isEqualTo("009");
        Assertions.assertThat(responseJsonPath.getString("body.resultMsg")).isEqualTo("Payment failed as this merchant is not accepting Credit Line on UPI. Try using other options.");
        Assertions.assertThat(responseJsonPath.getString("body.mid")).isEqualTo(mid);
        Assertions.assertThat(responseJsonPath.getString("body.txnAmount")).isEqualTo(txnAmountupipsp);
        Assertions.assertThat(responseJsonPath.getString("body.externalSerialNo")).isEqualTo(externalSerialNo);
        Assertions.assertThat(responseJsonPath.getString("body.subResultCodeId")).isEqualTo("004");

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59551")
    @Test(description = "validate Upi Psp Consult Merchant does not support UPI_CREDITLINE  Interest Bearing")
    public void validateUpiPspConsultMerchantdoesnotsupportUPICREDITLINEInterestBearing() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String mid = Constants.MerchantType.MDR_PLATFORM_FEE_ON_UPI_SUBTYPE.getId();
        String txnAmount = "200";
        String txnAmountupipsp = "200";
        String paymentFee = "0";
        String payeeVPA = "paytm-9759417324@ptybl";
        String payerPaymentInstrument = "UPI_CREDITLINE";
        String subAccountTypeValue = "CL01";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.MDR_PLATFORM_FEE_ON_UPI_SUBTYPE).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("3D")
                .setRiskExtendInfo("userLBSLatitude:26.88|userLBSLongitude:81.01|userAgent:Mozilla/5.0 (Linux; Android 8.1.0; Redmi Note 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36|timeZone:Asia/Calcutta|operationType:PAYMENT|networkType:4g|businessFlow:JS_CHECKOUT|amount:2|language:en-US|screenResolution:393X786|deviceType:mobile|channelId:WAP|platform:mWeb|osType:Android|deviceModel:Redmi Note 5|browserType:Chrome|browserVersion:96.0.4664.104|osVersion:8.1.0|deviceManufacturer:Xiaomi")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        String deeplink = ptcResponse.getBody().getDeepLinkInfo().getDeepLink();
        String externalSerialNo = deeplink.substring(deeplink.indexOf("tr")+3,deeplink.indexOf("&am"));

        System.out.println(externalSerialNo);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        UpiPspConsultFee upiPspConsultFeeobject =new UpiPspConsultFee( mid,  txnAmountupipsp, paymentFee ,  payeeVPA,  externalSerialNo ,  payerPaymentInstrument,  subAccountTypeValue) ;
        JsonPath responseJsonPath = upiPspConsultFeeobject.execute().jsonPath();
        Assertions.assertThat(responseJsonPath.getString("body.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(responseJsonPath.getString("body.resultCodeId")).isEqualTo("001");
        Assertions.assertThat(responseJsonPath.getString("body.resultMsg")).isEqualTo("success");
        Assertions.assertThat(responseJsonPath.getString("body.mid")).isEqualTo(mid);
        Assertions.assertThat(responseJsonPath.getString("body.txnAmount")).isEqualTo(txnAmountupipsp);
        Assertions.assertThat(responseJsonPath.getString("body.externalSerialNo")).isEqualTo(externalSerialNo);
        Assertions.assertThat(responseJsonPath.getString("body.payerPaymentInstrument")).isEqualTo(payerPaymentInstrument);
        Assertions.assertThat(responseJsonPath.getString("body.subAccountType")).isEqualTo(subAccountTypeValue);
        Assertions.assertThat(responseJsonPath.getString("body.convenienceFeeValidation")).isEqualTo("MATCH");

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59551")
    @Test(description = "validate Upi Psp Consult Merchant does not support UPI_CREDITLINE with random subAccountTypeValue")
    public void validateUpiPspConsultMerchantdoesnotsupportUPICREDITLINEwithrandomsubAccountTypeValue() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String mid = Constants.MerchantType.MDR_PLATFORM_FEE_ON_UPI_SUBTYPE.getId();
        String txnAmount = "200";
        String txnAmountupipsp = "200";
        String paymentFee = "0";
        String payeeVPA = "paytm-9759417324@ptybl";
        String payerPaymentInstrument = "UPI_CREDITLINE";
        String subAccountTypeValue = "DELHICREDIT";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.MDR_PLATFORM_FEE_ON_UPI_SUBTYPE).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("3D")
                .setRiskExtendInfo("userLBSLatitude:26.88|userLBSLongitude:81.01|userAgent:Mozilla/5.0 (Linux; Android 8.1.0; Redmi Note 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36|timeZone:Asia/Calcutta|operationType:PAYMENT|networkType:4g|businessFlow:JS_CHECKOUT|amount:2|language:en-US|screenResolution:393X786|deviceType:mobile|channelId:WAP|platform:mWeb|osType:Android|deviceModel:Redmi Note 5|browserType:Chrome|browserVersion:96.0.4664.104|osVersion:8.1.0|deviceManufacturer:Xiaomi")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        String deeplink = ptcResponse.getBody().getDeepLinkInfo().getDeepLink();
        String externalSerialNo = deeplink.substring(deeplink.indexOf("tr")+3,deeplink.indexOf("&am"));

        System.out.println(externalSerialNo);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        UpiPspConsultFee upiPspConsultFeeobject =new UpiPspConsultFee( mid,  txnAmountupipsp, paymentFee ,  payeeVPA,  externalSerialNo ,  payerPaymentInstrument,  subAccountTypeValue) ;
        JsonPath responseJsonPath = upiPspConsultFeeobject.execute().jsonPath();
        Assertions.assertThat(responseJsonPath.getString("body.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(responseJsonPath.getString("body.resultCodeId")).isEqualTo("001");
        Assertions.assertThat(responseJsonPath.getString("body.resultMsg")).isEqualTo("success");
        Assertions.assertThat(responseJsonPath.getString("body.mid")).isEqualTo(mid);
        Assertions.assertThat(responseJsonPath.getString("body.txnAmount")).isEqualTo(txnAmountupipsp);
        Assertions.assertThat(responseJsonPath.getString("body.externalSerialNo")).isEqualTo(externalSerialNo);
        Assertions.assertThat(responseJsonPath.getString("body.payerPaymentInstrument")).isEqualTo(payerPaymentInstrument);
        Assertions.assertThat(responseJsonPath.getString("body.subAccountType")).isEqualTo(subAccountTypeValue);
        Assertions.assertThat(responseJsonPath.getString("body.convenienceFeeValidation")).isEqualTo("MATCH");

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59551")
    @Test(description = "validate Upi Psp Consult Fee mismatch UPI Credit Card")
    public void validateUpiPspConsulttxnFeemismatchUpiCreditCard() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.PCF_PLATFORM_FEE_ON_UPI_SUBTYPE ;
        String mid = Constants.MerchantType.PCF_PLATFORM_FEE_ON_UPI_SUBTYPE.getId();
        String txnAmount = "200";
        String txnAmountupipsp = "209.44";
        String paymentFee = "9.44";
        String payeeVPA = "paytm-9759417325@ptybl";
        String payerPaymentInstrument = "UPI_CREDIT_CARD";
        String subAccountTypeValue = "";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("3D")
                .setRiskExtendInfo("userLBSLatitude:26.88|userLBSLongitude:81.01|userAgent:Mozilla/5.0 (Linux; Android 8.1.0; Redmi Note 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36|timeZone:Asia/Calcutta|operationType:PAYMENT|networkType:4g|businessFlow:JS_CHECKOUT|amount:2|language:en-US|screenResolution:393X786|deviceType:mobile|channelId:WAP|platform:mWeb|osType:Android|deviceModel:Redmi Note 5|browserType:Chrome|browserVersion:96.0.4664.104|osVersion:8.1.0|deviceManufacturer:Xiaomi")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        String deeplink = ptcResponse.getBody().getDeepLinkInfo().getDeepLink();
        String externalSerialNo = deeplink.substring(deeplink.indexOf("tr")+3,deeplink.indexOf("&am"));
        String bankRrn = CommonHelpers.generateOrderId();
        System.out.println(externalSerialNo);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        UpiPspConsultFee upiPspConsultFeeobject =new UpiPspConsultFee( mid,  txnAmountupipsp, paymentFee ,  payeeVPA,  externalSerialNo ,  payerPaymentInstrument,  subAccountTypeValue) ;
        JsonPath responseJsonPath = upiPspConsultFeeobject.execute().jsonPath();
        Assertions.assertThat(responseJsonPath.getString("body.resultCode")).isEqualTo("FAIL");
        Assertions.assertThat(responseJsonPath.getString("body.resultCodeId")).isEqualTo("009");
        Assertions.assertThat(responseJsonPath.getString("body.resultMsg")).isEqualTo("Fee mismatch");
        Assertions.assertThat(responseJsonPath.getString("body.mid")).isEqualTo(mid);
        Assertions.assertThat(responseJsonPath.getString("body.txnAmount")).isEqualTo(txnAmountupipsp);
        Assertions.assertThat(responseJsonPath.getString("body.externalSerialNo")).isEqualTo(externalSerialNo);
        Assertions.assertThat(responseJsonPath.getString("body.subResultCodeId")).isEqualTo("15008");

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59551")
    @Test(description = "validate Upi Psp Consult Fee mismatch UPI Credit Line")
    public void validateUpiPspConsulttxnFeemismatchUpiCreditLine() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.PCF_PLATFORM_FEE_ON_UPI_SUBTYPE ;
        String mid = Constants.MerchantType.PCF_PLATFORM_FEE_ON_UPI_SUBTYPE.getId();
        String txnAmount = "200";
        String txnAmountupipsp = "209.44";
        String paymentFee = "0";
        String payeeVPA = "paytm-9759417325@ptybl";
        String payerPaymentInstrument = "UPI_CREDITLINE";
        String subAccountTypeValue = "CL01";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("3D")
                .setRiskExtendInfo("userLBSLatitude:26.88|userLBSLongitude:81.01|userAgent:Mozilla/5.0 (Linux; Android 8.1.0; Redmi Note 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36|timeZone:Asia/Calcutta|operationType:PAYMENT|networkType:4g|businessFlow:JS_CHECKOUT|amount:2|language:en-US|screenResolution:393X786|deviceType:mobile|channelId:WAP|platform:mWeb|osType:Android|deviceModel:Redmi Note 5|browserType:Chrome|browserVersion:96.0.4664.104|osVersion:8.1.0|deviceManufacturer:Xiaomi")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        String deeplink = ptcResponse.getBody().getDeepLinkInfo().getDeepLink();
        String externalSerialNo = deeplink.substring(deeplink.indexOf("tr")+3,deeplink.indexOf("&am"));
        String bankRrn = CommonHelpers.generateOrderId();
        System.out.println(externalSerialNo);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        UpiPspConsultFee upiPspConsultFeeobject =new UpiPspConsultFee( mid,  txnAmountupipsp, paymentFee ,  payeeVPA,  externalSerialNo ,  payerPaymentInstrument,  subAccountTypeValue) ;
        JsonPath responseJsonPath = upiPspConsultFeeobject.execute().jsonPath();
        Assertions.assertThat(responseJsonPath.getString("body.resultCode")).isEqualTo("FAIL");
        Assertions.assertThat(responseJsonPath.getString("body.resultCodeId")).isEqualTo("009");
        Assertions.assertThat(responseJsonPath.getString("body.resultMsg")).isEqualTo("Fee mismatch");
        Assertions.assertThat(responseJsonPath.getString("body.mid")).isEqualTo(mid);
        Assertions.assertThat(responseJsonPath.getString("body.txnAmount")).isEqualTo(txnAmountupipsp);
        Assertions.assertThat(responseJsonPath.getString("body.externalSerialNo")).isEqualTo(externalSerialNo);
        Assertions.assertThat(responseJsonPath.getString("body.subResultCodeId")).isEqualTo("15008");

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59551")
    @Test(description = "validate Upi Psp Consult Fee mismatch UPI Ppi Wallet")
    public void validateUpiPspConsulttxnFeemismatchUpiPpiWallet() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.PCF_PLATFORM_FEE_ON_UPI_SUBTYPE ;
        String mid = Constants.MerchantType.PCF_PLATFORM_FEE_ON_UPI_SUBTYPE.getId();
        String txnAmount = "200";
        String txnAmountupipsp = "209.44";
        String paymentFee = "4.71";
        String payeeVPA = "paytm-9759417325@ptybl";
        String payerPaymentInstrument = "PPI_WALLET";
        String subAccountTypeValue = "";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("3D")
                .setRiskExtendInfo("userLBSLatitude:26.88|userLBSLongitude:81.01|userAgent:Mozilla/5.0 (Linux; Android 8.1.0; Redmi Note 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36|timeZone:Asia/Calcutta|operationType:PAYMENT|networkType:4g|businessFlow:JS_CHECKOUT|amount:2|language:en-US|screenResolution:393X786|deviceType:mobile|channelId:WAP|platform:mWeb|osType:Android|deviceModel:Redmi Note 5|browserType:Chrome|browserVersion:96.0.4664.104|osVersion:8.1.0|deviceManufacturer:Xiaomi")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        String deeplink = ptcResponse.getBody().getDeepLinkInfo().getDeepLink();
        String externalSerialNo = deeplink.substring(deeplink.indexOf("tr")+3,deeplink.indexOf("&am"));
        String bankRrn = CommonHelpers.generateOrderId();
        System.out.println(externalSerialNo);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        UpiPspConsultFee upiPspConsultFeeobject =new UpiPspConsultFee( mid,  txnAmountupipsp, paymentFee ,  payeeVPA,  externalSerialNo ,  payerPaymentInstrument,  subAccountTypeValue) ;
        JsonPath responseJsonPath = upiPspConsultFeeobject.execute().jsonPath();
        Assertions.assertThat(responseJsonPath.getString("body.resultCode")).isEqualTo("FAIL");
        Assertions.assertThat(responseJsonPath.getString("body.resultCodeId")).isEqualTo("009");
        Assertions.assertThat(responseJsonPath.getString("body.resultMsg")).isEqualTo("Fee mismatch");
        Assertions.assertThat(responseJsonPath.getString("body.mid")).isEqualTo(mid);
        Assertions.assertThat(responseJsonPath.getString("body.txnAmount")).isEqualTo(txnAmountupipsp);
        Assertions.assertThat(responseJsonPath.getString("body.externalSerialNo")).isEqualTo(externalSerialNo);
        Assertions.assertThat(responseJsonPath.getString("body.subResultCodeId")).isEqualTo("15008");

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59551")
    @Test(description = "validate UPI Intent Upi Psp Consult for mdr with random payerPaymentInstrument")
    public void validateUpiIntentUpiPspConsultformdrwithrandompayerPaymentInstrument() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String mid = Constants.MerchantType.MDR_FEE_ON_UPI_SUBTYPE.getId();
        String txnAmount = "200";
        String txnAmountupipsp = "200";
        String paymentFee = "";
        String payeeVPA = "paytm-9759417321@ptybl";
        String payerPaymentInstrument = "UPI_ABCD";
        String callbackpaymentInstrument = "";
        String subAccountTypeValue = "";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.MDR_FEE_ON_UPI_SUBTYPE).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("3D")
                .setRiskExtendInfo("userLBSLatitude:26.88|userLBSLongitude:81.01|userAgent:Mozilla/5.0 (Linux; Android 8.1.0; Redmi Note 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36|timeZone:Asia/Calcutta|operationType:PAYMENT|networkType:4g|businessFlow:JS_CHECKOUT|amount:2|language:en-US|screenResolution:393X786|deviceType:mobile|channelId:WAP|platform:mWeb|osType:Android|deviceModel:Redmi Note 5|browserType:Chrome|browserVersion:96.0.4664.104|osVersion:8.1.0|deviceManufacturer:Xiaomi")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        String deeplink = ptcResponse.getBody().getDeepLinkInfo().getDeepLink();
        String externalSerialNo = deeplink.substring(deeplink.indexOf("tr")+3,deeplink.indexOf("&am"));
        String bankRrn = CommonHelpers.generateOrderId();
        System.out.println(externalSerialNo);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        UpiPspConsultFee upiPspConsultFeeobject =new UpiPspConsultFee( mid,  txnAmountupipsp, paymentFee ,  payeeVPA,  externalSerialNo ,  payerPaymentInstrument,  subAccountTypeValue) ;
        JsonPath responseJsonPath = upiPspConsultFeeobject.execute().jsonPath();
        Assertions.assertThat(responseJsonPath.getString("body.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(responseJsonPath.getString("body.resultCodeId")).isEqualTo("001");
        Assertions.assertThat(responseJsonPath.getString("body.resultMsg")).isEqualTo("success");
        Assertions.assertThat(responseJsonPath.getString("body.mid")).isEqualTo(mid);
        Assertions.assertThat(responseJsonPath.getString("body.txnAmount")).isEqualTo(txnAmountupipsp);
        Assertions.assertThat(responseJsonPath.getString("body.externalSerialNo")).isEqualTo(externalSerialNo);
        Assertions.assertThat(responseJsonPath.getString("body.payerPaymentInstrument")).isEqualTo(payerPaymentInstrument);
        Assertions.assertThat(responseJsonPath.getString("body.convenienceFeeValidation")).isEqualTo("MATCH");

        InstaproxyPtybliResponse successresponse = new InstaproxyPtybliResponse(txnAmountupipsp,externalSerialNo ,bankRrn, callbackpaymentInstrument );
        JsonPath callbackres = successresponse.execute().jsonPath();
        Assertions.assertThat(callbackres.getString("body.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(callbackres.getString("body.resultMsg")).isEqualTo("success");

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PTYBLI.toString())
                .validateMid(mid)
                .AssertAll();
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59551")
    @Test(description = "validate Upi Intent Request for creditline when CREDITLINE_ON_UPI_RAILS_ENABLED is N")
    public void validateUpiIntentRequestforcreditlinewhenCreditLineOnUpiRailedDisabled() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String mid = Constants.MerchantType.MDR_PLATFORM_FEE_ON_UPI_SUBTYPE.getId();
        String txnAmount = "200";

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.MDR_PLATFORM_FEE_ON_UPI_SUBTYPE).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("3D")
                .setRiskExtendInfo("userLBSLatitude:26.88|userLBSLongitude:81.01|userAgent:Mozilla/5.0 (Linux; Android 8.1.0; Redmi Note 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36|timeZone:Asia/Calcutta|operationType:PAYMENT|networkType:4g|businessFlow:JS_CHECKOUT|amount:2|language:en-US|screenResolution:393X786|deviceType:mobile|channelId:WAP|platform:mWeb|osType:Android|deviceModel:Redmi Note 5|browserType:Chrome|browserVersion:96.0.4664.104|osVersion:8.1.0|deviceManufacturer:Xiaomi")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        String deeplink = ptcResponse.getBody().getDeepLinkInfo().getDeepLink();
        String externalSerialNo = deeplink.substring(deeplink.indexOf("tr")+3,deeplink.indexOf("&am"));
        String bankRrn = CommonHelpers.generateOrderId();
        System.out.println(externalSerialNo);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");


        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,orderId);
        String validateparam = "merchantAllowedUpiPaymentInstruments\":[\"VOUCHER\",\"CREDITLINE_ALL\"]";
        Assertions.assertThat(logs).contains(validateparam);
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59551")
    @Test(description = "validate Upi Collect Request for creditline when CREDITLINE_ON_UPI_RAILS_ENABLED is N")
    public void validateUpiCollectRequestforcreditlinewhenCreditLineOnUpiRailedDisabled() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String mid = Constants.MerchantType.MDR_PLATFORM_FEE_ON_UPI_SUBTYPE.getId();
        String txnAmount = "200";

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.MDR_PLATFORM_FEE_ON_UPI_SUBTYPE).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setAuthMode("USRPWD")
                .setPayerAccount("9759417329@paytm")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");


        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,orderId);
        String validateparam = "merchantAllowedUpiPaymentInstrumentsCommaSeparated=VOUCHER,CREDITLINE_ALL";
        Assertions.assertThat(logs).contains(validateparam);
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59551")
    @Test(description = "validate Upi Intent Request for creditline when CREDITLINE limit is breached")
    public void validateUpiIntentRequestforcreditlinewhenCreditlinelimitisbreached() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String mid = Constants.MerchantType.UPI_VOUCHER_ENABLED_MID.getId();
        String txnAmount = "7000";

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.UPI_VOUCHER_ENABLED_MID).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("3D")
                .setRiskExtendInfo("userLBSLatitude:26.88|userLBSLongitude:81.01|userAgent:Mozilla/5.0 (Linux; Android 8.1.0; Redmi Note 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36|timeZone:Asia/Calcutta|operationType:PAYMENT|networkType:4g|businessFlow:JS_CHECKOUT|amount:2|language:en-US|screenResolution:393X786|deviceType:mobile|channelId:WAP|platform:mWeb|osType:Android|deviceModel:Redmi Note 5|browserType:Chrome|browserVersion:96.0.4664.104|osVersion:8.1.0|deviceManufacturer:Xiaomi")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        String deeplink = ptcResponse.getBody().getDeepLinkInfo().getDeepLink();
        String externalSerialNo = deeplink.substring(deeplink.indexOf("tr")+3,deeplink.indexOf("&am"));
        String bankRrn = CommonHelpers.generateOrderId();
        System.out.println(externalSerialNo);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");


        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,orderId);
        String validateparam = "merchantAllowedUpiPaymentInstruments\":[\"VOUCHER\",\"CREDIT_CARD\",\"PPI_WALLET\"]";
        Assertions.assertThat(logs).contains(validateparam);
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59551")
    @Test(description = "validate Upi Collect Request for creditline when CREDITLINE limit is breached")
    public void validateUpiCollectRequestforcreditlinewhenCreditlinelimitisbreached() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String mid = Constants.MerchantType.UPI_VOUCHER_ENABLED_MID.getId();
        String txnAmount = "7000";

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.UPI_VOUCHER_ENABLED_MID).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setAuthMode("USRPWD")
                .setPayerAccount("9759417329@paytm")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");


        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,orderId);
        String validateparam = "merchantAllowedUpiPaymentInstrumentsCommaSeparated=VOUCHER,CREDIT_CARD,PPI_WALLET";
        Assertions.assertThat(logs).contains(validateparam);
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59551")
    @Test(description = "validate Upi Intent PCF and platform Upi Psp Consult for mdr Upi Credit Card MisMatch Amount")
    public void validateUpiIntentPCFandplatformUpiPspConsultformdrUpiCreditCardMisMatchAmount() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.PCF_PLATFORM_FEE_ON_UPI_SUBTYPE ;
        String mid = Constants.MerchantType.PCF_PLATFORM_FEE_ON_UPI_SUBTYPE.getId();
        String txnAmount = "200";
        String txnAmountupipsp = "209.44";
        String paymentFee = "4.72";
        String payeeVPA = "paytm-9759417325@ptybl";
        String payerPaymentInstrument = "UPI_CREDIT_CARD";
        String callbackpaymentInstrument = "CREDIT_CARD";
        String subAccountTypeValue = "";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("3D")
                .setRiskExtendInfo("userLBSLatitude:26.88|userLBSLongitude:81.01|userAgent:Mozilla/5.0 (Linux; Android 8.1.0; Redmi Note 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36|timeZone:Asia/Calcutta|operationType:PAYMENT|networkType:4g|businessFlow:JS_CHECKOUT|amount:2|language:en-US|screenResolution:393X786|deviceType:mobile|channelId:WAP|platform:mWeb|osType:Android|deviceModel:Redmi Note 5|browserType:Chrome|browserVersion:96.0.4664.104|osVersion:8.1.0|deviceManufacturer:Xiaomi")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        String deeplink = ptcResponse.getBody().getDeepLinkInfo().getDeepLink();
        String externalSerialNo = deeplink.substring(deeplink.indexOf("tr")+3,deeplink.indexOf("&am"));
        String bankRrn = CommonHelpers.generateOrderId();
        System.out.println(externalSerialNo);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        UpiPspConsultFee upiPspConsultFeeobject =new UpiPspConsultFee( mid,  txnAmountupipsp, paymentFee ,  payeeVPA,  externalSerialNo ,  payerPaymentInstrument,  subAccountTypeValue) ;
        JsonPath responseJsonPath = upiPspConsultFeeobject.execute().jsonPath();
        Assertions.assertThat(responseJsonPath.getString("body.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(responseJsonPath.getString("body.resultCodeId")).isEqualTo("001");
        Assertions.assertThat(responseJsonPath.getString("body.resultMsg")).isEqualTo("success");
        Assertions.assertThat(responseJsonPath.getString("body.mid")).isEqualTo(mid);
        Assertions.assertThat(responseJsonPath.getString("body.txnAmount")).isEqualTo(txnAmountupipsp);
        Assertions.assertThat(responseJsonPath.getString("body.externalSerialNo")).isEqualTo(externalSerialNo);
        Assertions.assertThat(responseJsonPath.getString("body.payerPaymentInstrument")).isEqualTo(payerPaymentInstrument);
        Assertions.assertThat(responseJsonPath.getString("body.convenienceFeeValidation")).isEqualTo("MATCH");

        InstaproxyPtybliResponse successresponse = new InstaproxyPtybliResponse("204.72",externalSerialNo ,bankRrn, callbackpaymentInstrument , "upiCcBin" );
        JsonPath callbackres = successresponse.execute().jsonPath();
        Assertions.assertThat(callbackres.getString("body.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(callbackres.getString("body.resultMsg")).isEqualTo("success");

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,externalSerialNo);
        String validateparam = "\"resultCode\":\"FGW_PAID_AMOUNT_MISMATCH\"";
        Assertions.assertThat(logs).contains(validateparam);

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.execute();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.PENDING.toString())
                .validateRespCode("402")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PTYBLI.toString())
                .validateMid(mid)
                .AssertAll();
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59551")
    @Test(description = "validate Upi Intent PCF and platform Upi Psp Consult for mdr Upi Credit Line MisMatch Amount")
    public void validateUpiIntentPCFandplatformUpiPspConsultformdrUpiCreditLineMisMatchAmount() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.PCF_PLATFORM_FEE_ON_UPI_SUBTYPE ;
        String mid = Constants.MerchantType.PCF_PLATFORM_FEE_ON_UPI_SUBTYPE.getId();
        String txnAmount = "200";
        String txnAmountupipsp = "209.44";
        String paymentFee = "4.72";
        String payeeVPA = "paytm-9759417325@ptybl";
        String payerPaymentInstrument = "UPI_CREDITLINE";
        String subAccountTypeValue = "CREDITLINE01";
        String callbackpaymentInstrument = "CREDITLINE_CREDITLINE01";

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("3D")
                .setRiskExtendInfo("userLBSLatitude:26.88|userLBSLongitude:81.01|userAgent:Mozilla/5.0 (Linux; Android 8.1.0; Redmi Note 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36|timeZone:Asia/Calcutta|operationType:PAYMENT|networkType:4g|businessFlow:JS_CHECKOUT|amount:2|language:en-US|screenResolution:393X786|deviceType:mobile|channelId:WAP|platform:mWeb|osType:Android|deviceModel:Redmi Note 5|browserType:Chrome|browserVersion:96.0.4664.104|osVersion:8.1.0|deviceManufacturer:Xiaomi")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        String deeplink = ptcResponse.getBody().getDeepLinkInfo().getDeepLink();
        String externalSerialNo = deeplink.substring(deeplink.indexOf("tr")+3,deeplink.indexOf("&am"));
        String bankRrn = CommonHelpers.generateOrderId();
        System.out.println(externalSerialNo);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        UpiPspConsultFee upiPspConsultFeeobject =new UpiPspConsultFee( mid, txnAmountupipsp , paymentFee ,  payeeVPA,  externalSerialNo ,  payerPaymentInstrument,  subAccountTypeValue) ;
        JsonPath responseJsonPath = upiPspConsultFeeobject.execute().jsonPath();
        Assertions.assertThat(responseJsonPath.getString("body.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(responseJsonPath.getString("body.resultCodeId")).isEqualTo("001");
        Assertions.assertThat(responseJsonPath.getString("body.resultMsg")).isEqualTo("success");
        Assertions.assertThat(responseJsonPath.getString("body.mid")).isEqualTo(mid);
        Assertions.assertThat(responseJsonPath.getString("body.txnAmount")).isEqualTo(txnAmountupipsp);
        Assertions.assertThat(responseJsonPath.getString("body.externalSerialNo")).isEqualTo(externalSerialNo);
        Assertions.assertThat(responseJsonPath.getString("body.payerPaymentInstrument")).isEqualTo(payerPaymentInstrument);
        Assertions.assertThat(responseJsonPath.getString("body.convenienceFeeValidation")).isEqualTo("MATCH");

        InstaproxyPtybliResponse successresponse = new InstaproxyPtybliResponse("209.45",externalSerialNo ,bankRrn, callbackpaymentInstrument );
        JsonPath callbackres = successresponse.execute().jsonPath();
        Assertions.assertThat(callbackres.getString("body.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(callbackres.getString("body.resultMsg")).isEqualTo("success");

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,externalSerialNo);
        String validateparam = "\"resultCode\":\"FGW_PAID_AMOUNT_MISMATCH\"";
        Assertions.assertThat(logs).contains(validateparam);

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.execute();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.PENDING.toString())
                .validateRespCode("402")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PTYBLI.toString())
                .validateMid(mid)
                .AssertAll();
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59551")
    @Test(description = "validate Upi Intent PCF and platform Upi Psp Consult for mdr Upi Ppi Wallet MisMatch Amount")
    public void validateUpiIntentPCFandplatformUpiPspConsultformdrUpiPpiWalletMisMatchAmount() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.PCF_PLATFORM_FEE_ON_UPI_SUBTYPE ;
        String mid = Constants.MerchantType.PCF_PLATFORM_FEE_ON_UPI_SUBTYPE.getId();
        String txnAmount = "200";
        String txnAmountupipsp = "209.44";
        String paymentFee = "4.72";
        String payeeVPA = "paytm-9759417325@ptybl";
        String payerPaymentInstrument = "PPI_WALLET";
        String subAccountTypeValue = "";
        String callbackpaymentInstrument = "PPI_WALLET";

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("3D")
                .setRiskExtendInfo("userLBSLatitude:26.88|userLBSLongitude:81.01|userAgent:Mozilla/5.0 (Linux; Android 8.1.0; Redmi Note 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36|timeZone:Asia/Calcutta|operationType:PAYMENT|networkType:4g|businessFlow:JS_CHECKOUT|amount:2|language:en-US|screenResolution:393X786|deviceType:mobile|channelId:WAP|platform:mWeb|osType:Android|deviceModel:Redmi Note 5|browserType:Chrome|browserVersion:96.0.4664.104|osVersion:8.1.0|deviceManufacturer:Xiaomi")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        String deeplink = ptcResponse.getBody().getDeepLinkInfo().getDeepLink();
        String externalSerialNo = deeplink.substring(deeplink.indexOf("tr")+3,deeplink.indexOf("&am"));
        String bankRrn = CommonHelpers.generateOrderId();
        System.out.println(externalSerialNo);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        UpiPspConsultFee upiPspConsultFeeobject =new UpiPspConsultFee( mid,  txnAmountupipsp, paymentFee ,  payeeVPA,  externalSerialNo ,  payerPaymentInstrument,  subAccountTypeValue) ;
        JsonPath responseJsonPath = upiPspConsultFeeobject.execute().jsonPath();
        Assertions.assertThat(responseJsonPath.getString("body.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(responseJsonPath.getString("body.resultCodeId")).isEqualTo("001");
        Assertions.assertThat(responseJsonPath.getString("body.resultMsg")).isEqualTo("success");
        Assertions.assertThat(responseJsonPath.getString("body.mid")).isEqualTo(mid);
        Assertions.assertThat(responseJsonPath.getString("body.txnAmount")).isEqualTo(txnAmountupipsp);
        Assertions.assertThat(responseJsonPath.getString("body.externalSerialNo")).isEqualTo(externalSerialNo);
        Assertions.assertThat(responseJsonPath.getString("body.payerPaymentInstrument")).isEqualTo(payerPaymentInstrument);
        Assertions.assertThat(responseJsonPath.getString("body.convenienceFeeValidation")).isEqualTo("MATCH");

        InstaproxyPtybliResponse successresponse = new InstaproxyPtybliResponse("200",externalSerialNo ,bankRrn, callbackpaymentInstrument );
        JsonPath callbackres = successresponse.execute().jsonPath();
        Assertions.assertThat(callbackres.getString("body.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(callbackres.getString("body.resultMsg")).isEqualTo("success");

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,externalSerialNo);
        String validateparam = "\"resultCode\":\"FGW_PAID_AMOUNT_MISMATCH\"";
        Assertions.assertThat(logs).contains(validateparam);

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.execute();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.PENDING.toString())
                .validateRespCode("402")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PTYBLI.toString())
                .validateMid(mid)
                .AssertAll();
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59551")
    @Test(description = "validate Upi Intent PCF and platform Upi Psp Consult for mdr Upi Credit Line Refund")
    public void validateUpiIntentPCFandplatformUpiPspConsultformdrUpiCreditLineRefund() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.PCF_PLATFORM_FEE_ON_UPI_SUBTYPE ;
        String mid = Constants.MerchantType.PCF_PLATFORM_FEE_ON_UPI_SUBTYPE.getId();
        String txnAmount = "200";
        String txnAmountupipsp = "209.44";
        String paymentFee = "4.72";
        String payeeVPA = "paytm-9759417325@ptybl";
        String payerPaymentInstrument = "UPI_CREDITLINE";
        String subAccountTypeValue = "CREDITLINE01";
        String callbackpaymentInstrument = "CREDITLINE_CREDITLINE01";

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("3D")
                .setRiskExtendInfo("userLBSLatitude:26.88|userLBSLongitude:81.01|userAgent:Mozilla/5.0 (Linux; Android 8.1.0; Redmi Note 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36|timeZone:Asia/Calcutta|operationType:PAYMENT|networkType:4g|businessFlow:JS_CHECKOUT|amount:2|language:en-US|screenResolution:393X786|deviceType:mobile|channelId:WAP|platform:mWeb|osType:Android|deviceModel:Redmi Note 5|browserType:Chrome|browserVersion:96.0.4664.104|osVersion:8.1.0|deviceManufacturer:Xiaomi")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        String deeplink = ptcResponse.getBody().getDeepLinkInfo().getDeepLink();
        String externalSerialNo = deeplink.substring(deeplink.indexOf("tr")+3,deeplink.indexOf("&am"));
        String bankRrn = CommonHelpers.generateOrderId();
        System.out.println(externalSerialNo);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

    /*    UpiPspConsultFee upiPspConsultFeeobject =new UpiPspConsultFee( mid, txnAmountupipsp , paymentFee ,  payeeVPA,  externalSerialNo ,  payerPaymentInstrument,  subAccountTypeValue) ;
        JsonPath responseJsonPath = upiPspConsultFeeobject.execute().jsonPath();
        Assertions.assertThat(responseJsonPath.getString("body.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(responseJsonPath.getString("body.resultCodeId")).isEqualTo("001");
        Assertions.assertThat(responseJsonPath.getString("body.resultMsg")).isEqualTo("success");
        Assertions.assertThat(responseJsonPath.getString("body.mid")).isEqualTo(mid);
        Assertions.assertThat(responseJsonPath.getString("body.txnAmount")).isEqualTo(txnAmountupipsp);
        Assertions.assertThat(responseJsonPath.getString("body.externalSerialNo")).isEqualTo(externalSerialNo);
        Assertions.assertThat(responseJsonPath.getString("body.payerPaymentInstrument")).isEqualTo(payerPaymentInstrument);
        Assertions.assertThat(responseJsonPath.getString("body.convenienceFeeValidation")).isEqualTo("MATCH");*/

        InstaproxyPtybliResponse successresponse = new InstaproxyPtybliResponse(txnAmountupipsp,externalSerialNo ,bankRrn, callbackpaymentInstrument );
        JsonPath callbackres = successresponse.execute().jsonPath();
        Assertions.assertThat(callbackres.getString("body.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(callbackres.getString("body.resultMsg")).isEqualTo("success");

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.execute();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
            /*    .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PTYBLI.toString())
                .validateMid(mid)*/
                .AssertAll();

        Assertions.assertThat(txnStatus.execute().getBody().prettyPrint()).contains("UPICL");
    //    Assertions.assertThat(txnStatus.execute().getBody().prettyPrint()).contains("UPI_CREDITLINE");

        String refundAmount = "200";
        SyncRefund refundWithChargeAmount = new SyncRefund();
        Response asyncRefundResp = given().spec(refundWithChargeAmount.reqSpecAsyncRefundtxnType(merchant, initTxnDTO.txnAmountFromBody(), orderId,
                        txnStatus.getResponse().getTXNID(), "R"))
                .post().then().extract().response();
        System.out.println("Async Refund Response " + asyncRefundResp.body().prettyPrint());
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.orderId")).isEqualTo(orderId);
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.mid")).isEqualTo(mid);
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.refId")).isNotNull();
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo(Constants.TXNSTATUS.PENDING.toString());
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo(Constants.ResponseCode.REFUND_PENDING.getRespCode());
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo(Constants.ResponseCode.REFUND_PENDING.getRespMsg());
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.refundId")).isNotNull();
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.txnId")).isEqualTo(txnStatus.getResponse().getTXNID());
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.refundAmount")).isEqualTo(refundAmount);
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-60586")
    @Test(description = "validate Upi Intent MDR PCF Upi Psp Consult for Upi Credit Card Confee greater than base amount")
    public void validateUpiIntentMDRPCFUpiPspConsultforUpiCreditCardConfeegreaterthanbaseamount() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.MDR_PCF_FLAT_FEE_ON_UPI_SUBTYPE ;
        String mid = Constants.MerchantType.MDR_PCF_FLAT_FEE_ON_UPI_SUBTYPE.getId();
        String txnAmount = "11.79";
        String txnAmountupipsp = "23.59";
        String paymentFee = "11.80";
        String payeeVPA = "paytm-9759417327@ptybl";
        String payerPaymentInstrument = "UPI_CREDIT_CARD";
        String callbackpaymentInstrument = "CREDIT_CARD";
        String subAccountTypeValue = "";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("3D")
                .setRiskExtendInfo("userLBSLatitude:26.88|userLBSLongitude:81.01|userAgent:Mozilla/5.0 (Linux; Android 8.1.0; Redmi Note 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36|timeZone:Asia/Calcutta|operationType:PAYMENT|networkType:4g|businessFlow:JS_CHECKOUT|amount:2|language:en-US|screenResolution:393X786|deviceType:mobile|channelId:WAP|platform:mWeb|osType:Android|deviceModel:Redmi Note 5|browserType:Chrome|browserVersion:96.0.4664.104|osVersion:8.1.0|deviceManufacturer:Xiaomi")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        String deeplink = ptcResponse.getBody().getDeepLinkInfo().getDeepLink();
        String externalSerialNo = deeplink.substring(deeplink.indexOf("tr")+3,deeplink.indexOf("&am"));
        String bankRrn = CommonHelpers.generateOrderId();
        System.out.println(externalSerialNo);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        UpiPspConsultFee upiPspConsultFeeobject =new UpiPspConsultFee( mid,  txnAmountupipsp, paymentFee ,  payeeVPA,  externalSerialNo ,  payerPaymentInstrument,  subAccountTypeValue) ;
        JsonPath responseJsonPath = upiPspConsultFeeobject.execute().jsonPath();
        Assertions.assertThat(responseJsonPath.getString("body.resultCode")).isEqualTo("FAIL");
        Assertions.assertThat(responseJsonPath.getString("body.resultCodeId")).isEqualTo("009");
        Assertions.assertThat(responseJsonPath.getString("body.resultMsg")).isEqualTo("Invalid Amount. Convenience Fee greater than Order Amount");
        Assertions.assertThat(responseJsonPath.getString("body.mid")).isEqualTo(mid);
        Assertions.assertThat(responseJsonPath.getString("body.txnAmount")).isEqualTo(txnAmountupipsp);
        Assertions.assertThat(responseJsonPath.getString("body.externalSerialNo")).isEqualTo(externalSerialNo);
        Assertions.assertThat(responseJsonPath.getString("body.subResultCodeId")).isEqualTo("15014");

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-60586")
    @Test(description = "validate Upi Intent MDR PCF Upi Psp Consult for Upi Credit Card Confee equal to base amount")
    public void validateUpiIntentMDRPCFUpiPspConsultforUpiCreditCardConfeeequaltobaseamount() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.MDR_PCF_FLAT_FEE_ON_UPI_SUBTYPE ;
        String mid = Constants.MerchantType.MDR_PCF_FLAT_FEE_ON_UPI_SUBTYPE.getId();
        String txnAmount = "11.80";
        String txnAmountupipsp = "23.6";
        String paymentFee = "11.80";
        String payeeVPA = "paytm-9759417327@ptybl";
        String payerPaymentInstrument = "UPI_CREDIT_CARD";
        String callbackpaymentInstrument = "CREDIT_CARD";
        String subAccountTypeValue = "";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("3D")
                .setRiskExtendInfo("userLBSLatitude:26.88|userLBSLongitude:81.01|userAgent:Mozilla/5.0 (Linux; Android 8.1.0; Redmi Note 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36|timeZone:Asia/Calcutta|operationType:PAYMENT|networkType:4g|businessFlow:JS_CHECKOUT|amount:2|language:en-US|screenResolution:393X786|deviceType:mobile|channelId:WAP|platform:mWeb|osType:Android|deviceModel:Redmi Note 5|browserType:Chrome|browserVersion:96.0.4664.104|osVersion:8.1.0|deviceManufacturer:Xiaomi")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        String deeplink = ptcResponse.getBody().getDeepLinkInfo().getDeepLink();
        String externalSerialNo = deeplink.substring(deeplink.indexOf("tr")+3,deeplink.indexOf("&am"));
        String bankRrn = CommonHelpers.generateOrderId();
        System.out.println(externalSerialNo);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        UpiPspConsultFee upiPspConsultFeeobject =new UpiPspConsultFee( mid,  txnAmountupipsp, paymentFee ,  payeeVPA,  externalSerialNo ,  payerPaymentInstrument,  subAccountTypeValue) ;
        JsonPath responseJsonPath = upiPspConsultFeeobject.execute().jsonPath();
        Assertions.assertThat(responseJsonPath.getString("body.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(responseJsonPath.getString("body.resultCodeId")).isEqualTo("001");
        Assertions.assertThat(responseJsonPath.getString("body.resultMsg")).isEqualTo("success");
        Assertions.assertThat(responseJsonPath.getString("body.mid")).isEqualTo(mid);
        Assertions.assertThat(responseJsonPath.getString("body.txnAmount")).isEqualTo(txnAmountupipsp);
        Assertions.assertThat(responseJsonPath.getString("body.externalSerialNo")).isEqualTo(externalSerialNo);
        Assertions.assertThat(responseJsonPath.getString("body.payerPaymentInstrument")).isEqualTo(payerPaymentInstrument);
        Assertions.assertThat(responseJsonPath.getString("body.convenienceFeeValidation")).isEqualTo("MATCH");

        InstaproxyPtybliResponse successresponse = new InstaproxyPtybliResponse(txnAmountupipsp,externalSerialNo ,bankRrn, callbackpaymentInstrument , "upiCcBin" );
        JsonPath callbackres = successresponse.execute().jsonPath();
        Assertions.assertThat(callbackres.getString("body.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(callbackres.getString("body.resultMsg")).isEqualTo("success");

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PTYBLI.toString())
                .validateMid(mid)
                .AssertAll();

        Assertions.assertThat(txnStatus.execute().getBody().prettyPrint()).contains("UPICC");
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-60586")
    @Test(description = "validate Upi Intent MDR PCF Upi Psp Consult for Upi Credit Card Confee less than base amount")
    public void validateUpiIntentMDRPCFUpiPspConsultforUpiCreditCardConfeelessthanbaseamount() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.MDR_PCF_FLAT_FEE_ON_UPI_SUBTYPE ;
        String mid = Constants.MerchantType.MDR_PCF_FLAT_FEE_ON_UPI_SUBTYPE.getId();
        String txnAmount = "11.81";
        String txnAmountupipsp = "23.61";
        String paymentFee = "11.80";
        String payeeVPA = "paytm-9759417327@ptybl";
        String payerPaymentInstrument = "UPI_CREDIT_CARD";
        String callbackpaymentInstrument = "CREDIT_CARD";
        String subAccountTypeValue = "";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("3D")
                .setRiskExtendInfo("userLBSLatitude:26.88|userLBSLongitude:81.01|userAgent:Mozilla/5.0 (Linux; Android 8.1.0; Redmi Note 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36|timeZone:Asia/Calcutta|operationType:PAYMENT|networkType:4g|businessFlow:JS_CHECKOUT|amount:2|language:en-US|screenResolution:393X786|deviceType:mobile|channelId:WAP|platform:mWeb|osType:Android|deviceModel:Redmi Note 5|browserType:Chrome|browserVersion:96.0.4664.104|osVersion:8.1.0|deviceManufacturer:Xiaomi")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        String deeplink = ptcResponse.getBody().getDeepLinkInfo().getDeepLink();
        String externalSerialNo = deeplink.substring(deeplink.indexOf("tr")+3,deeplink.indexOf("&am"));
        String bankRrn = CommonHelpers.generateOrderId();
        System.out.println(externalSerialNo);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        UpiPspConsultFee upiPspConsultFeeobject =new UpiPspConsultFee( mid,  txnAmountupipsp, paymentFee ,  payeeVPA,  externalSerialNo ,  payerPaymentInstrument,  subAccountTypeValue) ;
        JsonPath responseJsonPath = upiPspConsultFeeobject.execute().jsonPath();
        Assertions.assertThat(responseJsonPath.getString("body.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(responseJsonPath.getString("body.resultCodeId")).isEqualTo("001");
        Assertions.assertThat(responseJsonPath.getString("body.resultMsg")).isEqualTo("success");
        Assertions.assertThat(responseJsonPath.getString("body.mid")).isEqualTo(mid);
        Assertions.assertThat(responseJsonPath.getString("body.txnAmount")).isEqualTo(txnAmountupipsp);
        Assertions.assertThat(responseJsonPath.getString("body.externalSerialNo")).isEqualTo(externalSerialNo);
        Assertions.assertThat(responseJsonPath.getString("body.payerPaymentInstrument")).isEqualTo(payerPaymentInstrument);
        Assertions.assertThat(responseJsonPath.getString("body.convenienceFeeValidation")).isEqualTo("MATCH");

        InstaproxyPtybliResponse successresponse = new InstaproxyPtybliResponse(txnAmountupipsp,externalSerialNo ,bankRrn, callbackpaymentInstrument , "upiCcBin" );
        JsonPath callbackres = successresponse.execute().jsonPath();
        Assertions.assertThat(callbackres.getString("body.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(callbackres.getString("body.resultMsg")).isEqualTo("success");

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PTYBLI.toString())
                .validateMid(mid)
                .AssertAll();

        Assertions.assertThat(txnStatus.execute().getBody().prettyPrint()).contains("UPICC");
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-60586")
    @Test(description = "validate Upi Intent MDR PCF Upi Psp Consult for Upi savings fee 0 less than base amount")
    public void validateUpiIntentMDRPCFUpiPspConsultforUpisavingsfee0lessthanbaseamount() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.MDR_PCF_FLAT_FEE_ON_UPI_SUBTYPE ;
        String mid = Constants.MerchantType.MDR_PCF_FLAT_FEE_ON_UPI_SUBTYPE.getId();
        String txnAmount = "10";
        String txnAmountupipsp = "10";
        String paymentFee = "";
        String payeeVPA = "paytm-9759417327@ptybl";
        String payerPaymentInstrument = "UPI_SAVINGS";
        String callbackpaymentInstrument = "";
        String subAccountTypeValue = "";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("3D")
                .setRiskExtendInfo("userLBSLatitude:26.88|userLBSLongitude:81.01|userAgent:Mozilla/5.0 (Linux; Android 8.1.0; Redmi Note 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36|timeZone:Asia/Calcutta|operationType:PAYMENT|networkType:4g|businessFlow:JS_CHECKOUT|amount:2|language:en-US|screenResolution:393X786|deviceType:mobile|channelId:WAP|platform:mWeb|osType:Android|deviceModel:Redmi Note 5|browserType:Chrome|browserVersion:96.0.4664.104|osVersion:8.1.0|deviceManufacturer:Xiaomi")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        String deeplink = ptcResponse.getBody().getDeepLinkInfo().getDeepLink();
        String externalSerialNo = deeplink.substring(deeplink.indexOf("tr")+3,deeplink.indexOf("&am"));
        String bankRrn = CommonHelpers.generateOrderId();
        System.out.println(externalSerialNo);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        UpiPspConsultFee upiPspConsultFeeobject =new UpiPspConsultFee( mid,  txnAmountupipsp, paymentFee ,  payeeVPA,  externalSerialNo ,  payerPaymentInstrument,  subAccountTypeValue) ;
        JsonPath responseJsonPath = upiPspConsultFeeobject.execute().jsonPath();
        Assertions.assertThat(responseJsonPath.getString("body.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(responseJsonPath.getString("body.resultCodeId")).isEqualTo("001");
        Assertions.assertThat(responseJsonPath.getString("body.resultMsg")).isEqualTo("success");
        Assertions.assertThat(responseJsonPath.getString("body.mid")).isEqualTo(mid);
        Assertions.assertThat(responseJsonPath.getString("body.txnAmount")).isEqualTo(txnAmountupipsp);
        Assertions.assertThat(responseJsonPath.getString("body.externalSerialNo")).isEqualTo(externalSerialNo);
        Assertions.assertThat(responseJsonPath.getString("body.payerPaymentInstrument")).isEqualTo(payerPaymentInstrument);
        Assertions.assertThat(responseJsonPath.getString("body.convenienceFeeValidation")).isEqualTo("MATCH");

        InstaproxyPtybliResponse successresponse = new InstaproxyPtybliResponse(txnAmountupipsp,externalSerialNo ,bankRrn, callbackpaymentInstrument );
        JsonPath callbackres = successresponse.execute().jsonPath();
        Assertions.assertThat(callbackres.getString("body.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(callbackres.getString("body.resultMsg")).isEqualTo("success");

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PTYBLI.toString())
                .validateMid(mid)
                .AssertAll();
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-60586")
    @Test(description = "validate Upi Intent MDR PCF Upi Psp Consult for Upi Credit Line Confee greater than base amount")
    public void validateUpiIntentMDRPCFUpiPspConsultforUpiCreditLineConfeegreaterthanbaseamount() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.MDR_PCF_FLAT_FEE_ON_UPI_SUBTYPE ;
        String mid = Constants.MerchantType.MDR_PCF_FLAT_FEE_ON_UPI_SUBTYPE.getId();
        String txnAmount = "11.79";
        String txnAmountupipsp = "23.59";
        String paymentFee = "11.80";
        String payeeVPA = "paytm-9759417327@ptybl";
        String payerPaymentInstrument = "UPI_CREDITLINE";
        String callbackpaymentInstrument = "CREDITLINE_CREDITLINE01";
        String subAccountTypeValue = "CREDITLINE01";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("3D")
                .setRiskExtendInfo("userLBSLatitude:26.88|userLBSLongitude:81.01|userAgent:Mozilla/5.0 (Linux; Android 8.1.0; Redmi Note 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36|timeZone:Asia/Calcutta|operationType:PAYMENT|networkType:4g|businessFlow:JS_CHECKOUT|amount:2|language:en-US|screenResolution:393X786|deviceType:mobile|channelId:WAP|platform:mWeb|osType:Android|deviceModel:Redmi Note 5|browserType:Chrome|browserVersion:96.0.4664.104|osVersion:8.1.0|deviceManufacturer:Xiaomi")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        String deeplink = ptcResponse.getBody().getDeepLinkInfo().getDeepLink();
        String externalSerialNo = deeplink.substring(deeplink.indexOf("tr")+3,deeplink.indexOf("&am"));
        String bankRrn = CommonHelpers.generateOrderId();
        System.out.println(externalSerialNo);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        UpiPspConsultFee upiPspConsultFeeobject =new UpiPspConsultFee( mid,  txnAmountupipsp, paymentFee ,  payeeVPA,  externalSerialNo ,  payerPaymentInstrument,  subAccountTypeValue) ;
        JsonPath responseJsonPath = upiPspConsultFeeobject.execute().jsonPath();
        Assertions.assertThat(responseJsonPath.getString("body.resultCode")).isEqualTo("FAIL");
        Assertions.assertThat(responseJsonPath.getString("body.resultCodeId")).isEqualTo("009");
        Assertions.assertThat(responseJsonPath.getString("body.resultMsg")).isEqualTo("Invalid Amount. Convenience Fee greater than Order Amount");
        Assertions.assertThat(responseJsonPath.getString("body.mid")).isEqualTo(mid);
        Assertions.assertThat(responseJsonPath.getString("body.txnAmount")).isEqualTo(txnAmountupipsp);
        Assertions.assertThat(responseJsonPath.getString("body.externalSerialNo")).isEqualTo(externalSerialNo);
        Assertions.assertThat(responseJsonPath.getString("body.subResultCodeId")).isEqualTo("15014");

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-60586")
    @Test(description = "validate Upi Intent MDR PCF Upi Psp Consult for Upi Credit Line Confee equal to base amount")
    public void validateUpiIntentMDRPCFUpiPspConsultforUpiCreditLineConfeeequaltobaseamount() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.MDR_PCF_FLAT_FEE_ON_UPI_SUBTYPE ;
        String mid = Constants.MerchantType.MDR_PCF_FLAT_FEE_ON_UPI_SUBTYPE.getId();
        String txnAmount = "11.80";
        String txnAmountupipsp = "23.6";
        String paymentFee = "11.80";
        String payeeVPA = "paytm-9759417327@ptybl";
        String payerPaymentInstrument = "UPI_CREDITLINE";
        String callbackpaymentInstrument = "CREDITLINE_CREDITLINE01";
        String subAccountTypeValue = "CREDITLINE01";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("3D")
                .setRiskExtendInfo("userLBSLatitude:26.88|userLBSLongitude:81.01|userAgent:Mozilla/5.0 (Linux; Android 8.1.0; Redmi Note 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36|timeZone:Asia/Calcutta|operationType:PAYMENT|networkType:4g|businessFlow:JS_CHECKOUT|amount:2|language:en-US|screenResolution:393X786|deviceType:mobile|channelId:WAP|platform:mWeb|osType:Android|deviceModel:Redmi Note 5|browserType:Chrome|browserVersion:96.0.4664.104|osVersion:8.1.0|deviceManufacturer:Xiaomi")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        String deeplink = ptcResponse.getBody().getDeepLinkInfo().getDeepLink();
        String externalSerialNo = deeplink.substring(deeplink.indexOf("tr")+3,deeplink.indexOf("&am"));
        String bankRrn = CommonHelpers.generateOrderId();
        System.out.println(externalSerialNo);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        UpiPspConsultFee upiPspConsultFeeobject =new UpiPspConsultFee( mid,  txnAmountupipsp, paymentFee ,  payeeVPA,  externalSerialNo ,  payerPaymentInstrument,  subAccountTypeValue) ;
        JsonPath responseJsonPath = upiPspConsultFeeobject.execute().jsonPath();
        Assertions.assertThat(responseJsonPath.getString("body.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(responseJsonPath.getString("body.resultCodeId")).isEqualTo("001");
        Assertions.assertThat(responseJsonPath.getString("body.resultMsg")).isEqualTo("success");
        Assertions.assertThat(responseJsonPath.getString("body.mid")).isEqualTo(mid);
        Assertions.assertThat(responseJsonPath.getString("body.txnAmount")).isEqualTo(txnAmountupipsp);
        Assertions.assertThat(responseJsonPath.getString("body.externalSerialNo")).isEqualTo(externalSerialNo);
        Assertions.assertThat(responseJsonPath.getString("body.payerPaymentInstrument")).isEqualTo(payerPaymentInstrument);
        Assertions.assertThat(responseJsonPath.getString("body.convenienceFeeValidation")).isEqualTo("MATCH");

        InstaproxyPtybliResponse successresponse = new InstaproxyPtybliResponse(txnAmountupipsp,externalSerialNo ,bankRrn, callbackpaymentInstrument , "upiCcBin" );
        JsonPath callbackres = successresponse.execute().jsonPath();
        Assertions.assertThat(callbackres.getString("body.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(callbackres.getString("body.resultMsg")).isEqualTo("success");

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PTYBLI.toString())
                .validateMid(mid)
                .AssertAll();

        Assertions.assertThat(txnStatus.execute().getBody().prettyPrint()).contains("UPICL");
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-60586")
    @Test(description = "validate Upi Intent MDR PCF Upi Psp Consult for Upi Ppi Wallet Confee greater than base amount")
    public void validateUpiIntentMDRPCFUpiPspConsultforUpiPpiWalletConfeegreaterthanbaseamount() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.MDR_PCF_FLAT_FEE_ON_UPI_SUBTYPE ;
        String mid = Constants.MerchantType.MDR_PCF_FLAT_FEE_ON_UPI_SUBTYPE.getId();
        String txnAmount = "23.59";
        String txnAmountupipsp = "47.19";
        String paymentFee = "23.6";
        String payeeVPA = "paytm-9759417327@ptybl";
        String payerPaymentInstrument = "PPI_WALLET";
        String callbackpaymentInstrument = "PPI_WALLET";
        String subAccountTypeValue = "";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("3D")
                .setRiskExtendInfo("userLBSLatitude:26.88|userLBSLongitude:81.01|userAgent:Mozilla/5.0 (Linux; Android 8.1.0; Redmi Note 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36|timeZone:Asia/Calcutta|operationType:PAYMENT|networkType:4g|businessFlow:JS_CHECKOUT|amount:2|language:en-US|screenResolution:393X786|deviceType:mobile|channelId:WAP|platform:mWeb|osType:Android|deviceModel:Redmi Note 5|browserType:Chrome|browserVersion:96.0.4664.104|osVersion:8.1.0|deviceManufacturer:Xiaomi")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        String deeplink = ptcResponse.getBody().getDeepLinkInfo().getDeepLink();
        String externalSerialNo = deeplink.substring(deeplink.indexOf("tr")+3,deeplink.indexOf("&am"));
        String bankRrn = CommonHelpers.generateOrderId();
        System.out.println(externalSerialNo);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        UpiPspConsultFee upiPspConsultFeeobject =new UpiPspConsultFee( mid,  txnAmountupipsp, paymentFee ,  payeeVPA,  externalSerialNo ,  payerPaymentInstrument,  subAccountTypeValue) ;
        JsonPath responseJsonPath = upiPspConsultFeeobject.execute().jsonPath();
        Assertions.assertThat(responseJsonPath.getString("body.resultCode")).isEqualTo("FAIL");
        Assertions.assertThat(responseJsonPath.getString("body.resultCodeId")).isEqualTo("009");
        Assertions.assertThat(responseJsonPath.getString("body.resultMsg")).isEqualTo("Invalid Amount. Convenience Fee greater than Order Amount");
        Assertions.assertThat(responseJsonPath.getString("body.mid")).isEqualTo(mid);
        Assertions.assertThat(responseJsonPath.getString("body.txnAmount")).isEqualTo(txnAmountupipsp);
        Assertions.assertThat(responseJsonPath.getString("body.externalSerialNo")).isEqualTo(externalSerialNo);
        Assertions.assertThat(responseJsonPath.getString("body.subResultCodeId")).isEqualTo("15014");

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-60586")
    @Test(description = "validate Upi Intent MDR PCF Upi Psp Consult for Upi Ppi Wallet Confee equal to base amount")
    public void validateUpiIntentMDRPCFUpiPspConsultforUpiPpiWalletConfeeequaltobaseamount() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.MDR_PCF_FLAT_FEE_ON_UPI_SUBTYPE ;
        String mid = Constants.MerchantType.MDR_PCF_FLAT_FEE_ON_UPI_SUBTYPE.getId();
        String txnAmount = "23.6";
        String txnAmountupipsp = "47.2";
        String paymentFee = "23.6";
        String payeeVPA = "paytm-9759417327@ptybl";
        String payerPaymentInstrument = "PPI_WALLET";
        String callbackpaymentInstrument = "PPI_WALLET";
        String subAccountTypeValue = "";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("3D")
                .setRiskExtendInfo("userLBSLatitude:26.88|userLBSLongitude:81.01|userAgent:Mozilla/5.0 (Linux; Android 8.1.0; Redmi Note 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36|timeZone:Asia/Calcutta|operationType:PAYMENT|networkType:4g|businessFlow:JS_CHECKOUT|amount:2|language:en-US|screenResolution:393X786|deviceType:mobile|channelId:WAP|platform:mWeb|osType:Android|deviceModel:Redmi Note 5|browserType:Chrome|browserVersion:96.0.4664.104|osVersion:8.1.0|deviceManufacturer:Xiaomi")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        String deeplink = ptcResponse.getBody().getDeepLinkInfo().getDeepLink();
        String externalSerialNo = deeplink.substring(deeplink.indexOf("tr")+3,deeplink.indexOf("&am"));
        String bankRrn = CommonHelpers.generateOrderId();
        System.out.println(externalSerialNo);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        UpiPspConsultFee upiPspConsultFeeobject =new UpiPspConsultFee( mid,  txnAmountupipsp, paymentFee ,  payeeVPA,  externalSerialNo ,  payerPaymentInstrument,  subAccountTypeValue) ;
        JsonPath responseJsonPath = upiPspConsultFeeobject.execute().jsonPath();
        Assertions.assertThat(responseJsonPath.getString("body.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(responseJsonPath.getString("body.resultCodeId")).isEqualTo("001");
        Assertions.assertThat(responseJsonPath.getString("body.resultMsg")).isEqualTo("success");
        Assertions.assertThat(responseJsonPath.getString("body.mid")).isEqualTo(mid);
        Assertions.assertThat(responseJsonPath.getString("body.txnAmount")).isEqualTo(txnAmountupipsp);
        Assertions.assertThat(responseJsonPath.getString("body.externalSerialNo")).isEqualTo(externalSerialNo);
        Assertions.assertThat(responseJsonPath.getString("body.payerPaymentInstrument")).isEqualTo(payerPaymentInstrument);
        Assertions.assertThat(responseJsonPath.getString("body.convenienceFeeValidation")).isEqualTo("MATCH");

        InstaproxyPtybliResponse successresponse = new InstaproxyPtybliResponse(txnAmountupipsp,externalSerialNo ,bankRrn, callbackpaymentInstrument , "upiCcBin" );
        JsonPath callbackres = successresponse.execute().jsonPath();
        Assertions.assertThat(callbackres.getString("body.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(callbackres.getString("body.resultMsg")).isEqualTo("success");

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PTYBLI.toString())
                .validateMid(mid)
                .AssertAll();

        Assertions.assertThat(txnStatus.execute().getBody().prettyPrint()).contains("UPI_PPIWALLET");
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59551")
    @Test(description = "validate UPI Intent HDFU for mdr Upi Savings")
    public void validateUpiIntentHDFUformdrUpiSavings() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.MDR_FEE_ON_HDFC_UPI_SUBTYPE;
        String mid = Constants.MerchantType.MDR_FEE_ON_HDFC_UPI_SUBTYPE.getId();
        String txnAmount = "200";
        String BankresponseAmount = "200.00";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("3D")
                .setRiskExtendInfo("userLBSLatitude:26.88|userLBSLongitude:81.01|userAgent:Mozilla/5.0 (Linux; Android 8.1.0; Redmi Note 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36|timeZone:Asia/Calcutta|operationType:PAYMENT|networkType:4g|businessFlow:JS_CHECKOUT|amount:2|language:en-US|screenResolution:393X786|deviceType:mobile|channelId:WAP|platform:mWeb|osType:Android|deviceModel:Redmi Note 5|browserType:Chrome|browserVersion:96.0.4664.104|osVersion:8.1.0|deviceManufacturer:Xiaomi")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        String deeplink = ptcResponse.getBody().getDeepLinkInfo().getDeepLink();
        String bankRrn = CommonHelpers.generateOrderId();

        String qrData =  deeplink;
        Map<String, String> deeplinkInfo = parseDeeplinkInfo(qrData);
        String payeeVpa = deeplinkInfo.get("payeeVpa");
        String amount = deeplinkInfo.get("amount");
        String esn = deeplinkInfo.get("tr");
        String cconfee = deeplinkInfo.get("cconfee");
        String pconfee = deeplinkInfo.get("pconfee");

        String callbackResponse = successCallbackSavings.replace("{ESN}",esn).replace("{callbackAmount}",BankresponseAmount);

        InstaproxyHDFCIntentCallback callback = new InstaproxyHDFCIntentCallback(callbackResponse, esn);
        callback.execute();

        Thread.sleep(2000);

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.HDFU.toString())
                .validateMid(mid)
                .AssertAll();

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59551")
    @Test(description = "validate UPI Intent HDFU for mdr Upi Creditline")
    public void validateUpiIntentHDFUformdrUpiCrediline() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.MDR_FEE_ON_HDFC_UPI_SUBTYPE;
        String mid = Constants.MerchantType.MDR_FEE_ON_HDFC_UPI_SUBTYPE.getId();
        String txnAmount = "200";
        String BankresponseAmount = "200.00";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("3D")
                .setRiskExtendInfo("userLBSLatitude:26.88|userLBSLongitude:81.01|userAgent:Mozilla/5.0 (Linux; Android 8.1.0; Redmi Note 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36|timeZone:Asia/Calcutta|operationType:PAYMENT|networkType:4g|businessFlow:JS_CHECKOUT|amount:2|language:en-US|screenResolution:393X786|deviceType:mobile|channelId:WAP|platform:mWeb|osType:Android|deviceModel:Redmi Note 5|browserType:Chrome|browserVersion:96.0.4664.104|osVersion:8.1.0|deviceManufacturer:Xiaomi")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        String deeplink = ptcResponse.getBody().getDeepLinkInfo().getDeepLink();
        String bankRrn = CommonHelpers.generateOrderId();

        String qrData =  deeplink;
        Map<String, String> deeplinkInfo = parseDeeplinkInfo(qrData);
        String payeeVpa = deeplinkInfo.get("payeeVpa");
        String amount = deeplinkInfo.get("amount");
        String esn = deeplinkInfo.get("tr");
        String cconfee = deeplinkInfo.get("cconfee");
        String pconfee = deeplinkInfo.get("pconfee");

        String callbackResponse = successCallbackCreditLine.replace("{ESN}",esn).replace("{callbackAmount}",BankresponseAmount);

        InstaproxyHDFCIntentCallback callback = new InstaproxyHDFCIntentCallback(callbackResponse, esn);
        callback.execute();

        Thread.sleep(2000);

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.HDFU.toString())
                .validateMid(mid)
                .AssertAll();

        Assertions.assertThat(txnStatus.execute().getBody().prettyPrint()).contains("UPI_CREDITLINE");

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59551")
    @Test(description = "validate UPI Intent HDFU for PCF Upi Savings")
    public void validateUpiIntentHDFUforPCFUpiSavings() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.PCF_FEE_ON_HDFC_UPI_SUBTYPE;
        String mid = Constants.MerchantType.PCF_FEE_ON_HDFC_UPI_SUBTYPE.getId();
        String txnAmount = "200";
        String BankresponseAmount = "200.00";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("3D")
                .setRiskExtendInfo("userLBSLatitude:26.88|userLBSLongitude:81.01|userAgent:Mozilla/5.0 (Linux; Android 8.1.0; Redmi Note 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36|timeZone:Asia/Calcutta|operationType:PAYMENT|networkType:4g|businessFlow:JS_CHECKOUT|amount:2|language:en-US|screenResolution:393X786|deviceType:mobile|channelId:WAP|platform:mWeb|osType:Android|deviceModel:Redmi Note 5|browserType:Chrome|browserVersion:96.0.4664.104|osVersion:8.1.0|deviceManufacturer:Xiaomi")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        String deeplink = ptcResponse.getBody().getDeepLinkInfo().getDeepLink();
        String bankRrn = CommonHelpers.generateOrderId();

        String qrData =  deeplink;
        Map<String, String> deeplinkInfo = parseDeeplinkInfo(qrData);
        String payeeVpa = deeplinkInfo.get("payeeVpa");
        String amount = deeplinkInfo.get("amount");
        String esn = deeplinkInfo.get("tr");
        String cconfee = deeplinkInfo.get("cconfee");
        String pconfee = deeplinkInfo.get("pconfee");

        Assertions.assertThat(cconfee).isEqualTo("4.72");
        Assertions.assertThat(pconfee).isEqualTo("9.44");

        String callbackResponse = successCallbackSavings.replace("{ESN}",esn).replace("{callbackAmount}",BankresponseAmount);

        InstaproxyHDFCIntentCallback callback = new InstaproxyHDFCIntentCallback(callbackResponse, esn);
        callback.execute();

        Thread.sleep(2000);

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.HDFU.toString())
                .validateMid(mid)
                .AssertAll();

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59551")
    @Test(description = "validate UPI Intent HDFU for PCF Upi Creditline")
    public void validateUpiIntentHDFUforPCFUpiCrediline() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.PCF_FEE_ON_HDFC_UPI_SUBTYPE;
        String mid = Constants.MerchantType.PCF_FEE_ON_HDFC_UPI_SUBTYPE.getId();
        String txnAmount = "200";
        String BankresponseAmount = "204.72";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("3D")
                .setRiskExtendInfo("userLBSLatitude:26.88|userLBSLongitude:81.01|userAgent:Mozilla/5.0 (Linux; Android 8.1.0; Redmi Note 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36|timeZone:Asia/Calcutta|operationType:PAYMENT|networkType:4g|businessFlow:JS_CHECKOUT|amount:2|language:en-US|screenResolution:393X786|deviceType:mobile|channelId:WAP|platform:mWeb|osType:Android|deviceModel:Redmi Note 5|browserType:Chrome|browserVersion:96.0.4664.104|osVersion:8.1.0|deviceManufacturer:Xiaomi")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        String deeplink = ptcResponse.getBody().getDeepLinkInfo().getDeepLink();
        String bankRrn = CommonHelpers.generateOrderId();

        String qrData =  deeplink;
        Map<String, String> deeplinkInfo = parseDeeplinkInfo(qrData);
        String payeeVpa = deeplinkInfo.get("payeeVpa");
        String amount = deeplinkInfo.get("amount");
        String esn = deeplinkInfo.get("tr");
        String cconfee = deeplinkInfo.get("cconfee");
        String pconfee = deeplinkInfo.get("pconfee");

        Assertions.assertThat(amount).isEqualTo("200");
        Assertions.assertThat(cconfee).isEqualTo("4.72");
        Assertions.assertThat(pconfee).isEqualTo("9.44");

        String callbackResponse = successCallbackCreditLine.replace("{ESN}",esn).replace("{callbackAmount}",BankresponseAmount);

        InstaproxyHDFCIntentCallback callback = new InstaproxyHDFCIntentCallback(callbackResponse, esn);
        callback.execute();

        Thread.sleep(2000);

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.HDFU.toString())
                .validateMid(mid)
                .AssertAll();

        Assertions.assertThat(txnStatus.execute().getBody().prettyPrint()).contains("UPI_CREDITLINE");

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59551")
    @Test(description = "validate UPI Intent HDFU for PCF and platform Upi Savings")
    public void validateUpiIntentHDFUforPCFandPlatformUpiSavings() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.PCF_PLATFORM_FEE_ON_HDFC_UPI_SUBTYPE;
        String mid = Constants.MerchantType.PCF_PLATFORM_FEE_ON_HDFC_UPI_SUBTYPE.getId();
        String txnAmount = "200";
        String BankresponseAmount = "204.72";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("3D")
                .setRiskExtendInfo("userLBSLatitude:26.88|userLBSLongitude:81.01|userAgent:Mozilla/5.0 (Linux; Android 8.1.0; Redmi Note 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36|timeZone:Asia/Calcutta|operationType:PAYMENT|networkType:4g|businessFlow:JS_CHECKOUT|amount:2|language:en-US|screenResolution:393X786|deviceType:mobile|channelId:WAP|platform:mWeb|osType:Android|deviceModel:Redmi Note 5|browserType:Chrome|browserVersion:96.0.4664.104|osVersion:8.1.0|deviceManufacturer:Xiaomi")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        String deeplink = ptcResponse.getBody().getDeepLinkInfo().getDeepLink();
        String bankRrn = CommonHelpers.generateOrderId();

        String qrData =  deeplink;
        Map<String, String> deeplinkInfo = parseDeeplinkInfo(qrData);
        String payeeVpa = deeplinkInfo.get("payeeVpa");
        String amount = deeplinkInfo.get("amount");
        String esn = deeplinkInfo.get("tr");
        String cconfee = deeplinkInfo.get("cconfee");
        String pconfee = deeplinkInfo.get("pconfee");

        Assertions.assertThat(amount).isEqualTo("204.72");
        Assertions.assertThat(cconfee).isEqualTo("4.72");
        Assertions.assertThat(pconfee).isEqualTo("4.72");

        String callbackResponse = successCallbackSavings.replace("{ESN}",esn).replace("{callbackAmount}",BankresponseAmount);

        InstaproxyHDFCIntentCallback callback = new InstaproxyHDFCIntentCallback(callbackResponse, esn);
        callback.execute();

        Thread.sleep(2000);

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.HDFU.toString())
                .validateMid(mid)
                .AssertAll();

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59551")
    @Test(description = "validate UPI Intent HDFU for PCF and platform Upi Creditline")
    public void validateUpiIntentHDFUforPCFandPlatformUpiCrediline() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.PCF_PLATFORM_FEE_ON_HDFC_UPI_SUBTYPE;
        String mid = Constants.MerchantType.PCF_PLATFORM_FEE_ON_HDFC_UPI_SUBTYPE.getId();
        String txnAmount = "200";
        String BankresponseAmount = "209.44";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("3D")
                .setRiskExtendInfo("userLBSLatitude:26.88|userLBSLongitude:81.01|userAgent:Mozilla/5.0 (Linux; Android 8.1.0; Redmi Note 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36|timeZone:Asia/Calcutta|operationType:PAYMENT|networkType:4g|businessFlow:JS_CHECKOUT|amount:2|language:en-US|screenResolution:393X786|deviceType:mobile|channelId:WAP|platform:mWeb|osType:Android|deviceModel:Redmi Note 5|browserType:Chrome|browserVersion:96.0.4664.104|osVersion:8.1.0|deviceManufacturer:Xiaomi")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        String deeplink = ptcResponse.getBody().getDeepLinkInfo().getDeepLink();
        String bankRrn = CommonHelpers.generateOrderId();

        String qrData =  deeplink;
        Map<String, String> deeplinkInfo = parseDeeplinkInfo(qrData);
        String payeeVpa = deeplinkInfo.get("payeeVpa");
        String amount = deeplinkInfo.get("amount");
        String esn = deeplinkInfo.get("tr");
        String cconfee = deeplinkInfo.get("cconfee");
        String pconfee = deeplinkInfo.get("pconfee");

        Assertions.assertThat(amount).isEqualTo("204.72");
        Assertions.assertThat(cconfee).isEqualTo("4.72");
        Assertions.assertThat(pconfee).isEqualTo("4.72");

        String callbackResponse = successCallbackCreditLine.replace("{ESN}",esn).replace("{callbackAmount}",BankresponseAmount);

        InstaproxyHDFCIntentCallback callback = new InstaproxyHDFCIntentCallback(callbackResponse, esn);
        callback.execute();

        Thread.sleep(2000);

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.HDFU.toString())
                .validateMid(mid)
                .AssertAll();

        Assertions.assertThat(txnStatus.execute().getBody().prettyPrint()).contains("UPI_CREDITLINE");
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59551")
    @Test(description = "validate UPI Intent HDFU for Mdr and PCF Upi Savings")
    public void validateUpiIntentHDFUforMDRandPCFUpiSavings() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.MDR_PCF_FEE_ON_HDFC_UPI_SUBTYPE;
        String mid = Constants.MerchantType.MDR_PCF_FEE_ON_HDFC_UPI_SUBTYPE.getId();
        String txnAmount = "200";
        String BankresponseAmount = "200.00";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("3D")
                .setRiskExtendInfo("userLBSLatitude:26.88|userLBSLongitude:81.01|userAgent:Mozilla/5.0 (Linux; Android 8.1.0; Redmi Note 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36|timeZone:Asia/Calcutta|operationType:PAYMENT|networkType:4g|businessFlow:JS_CHECKOUT|amount:2|language:en-US|screenResolution:393X786|deviceType:mobile|channelId:WAP|platform:mWeb|osType:Android|deviceModel:Redmi Note 5|browserType:Chrome|browserVersion:96.0.4664.104|osVersion:8.1.0|deviceManufacturer:Xiaomi")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        String deeplink = ptcResponse.getBody().getDeepLinkInfo().getDeepLink();
        String bankRrn = CommonHelpers.generateOrderId();

        String qrData =  deeplink;
        Map<String, String> deeplinkInfo = parseDeeplinkInfo(qrData);
        String payeeVpa = deeplinkInfo.get("payeeVpa");
        String amount = deeplinkInfo.get("amount");
        String esn = deeplinkInfo.get("tr");
        String cconfee = deeplinkInfo.get("cconfee");
        String pconfee = deeplinkInfo.get("pconfee");

        Assertions.assertThat(amount).isEqualTo("200");
        Assertions.assertThat(cconfee).isEqualTo("4.72");
        Assertions.assertThat(pconfee).isEqualTo("9.44");

        String callbackResponse = successCallbackSavings.replace("{ESN}",esn).replace("{callbackAmount}",BankresponseAmount);

        InstaproxyHDFCIntentCallback callback = new InstaproxyHDFCIntentCallback(callbackResponse, esn);
        callback.execute();

        Thread.sleep(2000);

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.HDFU.toString())
                .validateMid(mid)
                .AssertAll();

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59551")
    @Test(description = "validate UPI Intent HDFU for MDR and PCF  Upi Creditline")
    public void validateUpiIntentHDFUforMDRandPCFUpiCrediline() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.MDR_PCF_FEE_ON_HDFC_UPI_SUBTYPE;
        String mid = Constants.MerchantType.MDR_PCF_FEE_ON_HDFC_UPI_SUBTYPE.getId();
        String txnAmount = "200";
        String BankresponseAmount = "204.72";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("3D")
                .setRiskExtendInfo("userLBSLatitude:26.88|userLBSLongitude:81.01|userAgent:Mozilla/5.0 (Linux; Android 8.1.0; Redmi Note 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36|timeZone:Asia/Calcutta|operationType:PAYMENT|networkType:4g|businessFlow:JS_CHECKOUT|amount:2|language:en-US|screenResolution:393X786|deviceType:mobile|channelId:WAP|platform:mWeb|osType:Android|deviceModel:Redmi Note 5|browserType:Chrome|browserVersion:96.0.4664.104|osVersion:8.1.0|deviceManufacturer:Xiaomi")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        String deeplink = ptcResponse.getBody().getDeepLinkInfo().getDeepLink();
        String bankRrn = CommonHelpers.generateOrderId();

        String qrData =  deeplink;
        Map<String, String> deeplinkInfo = parseDeeplinkInfo(qrData);
        String payeeVpa = deeplinkInfo.get("payeeVpa");
        String amount = deeplinkInfo.get("amount");
        String esn = deeplinkInfo.get("tr");
        String cconfee = deeplinkInfo.get("cconfee");
        String pconfee = deeplinkInfo.get("pconfee");

        Assertions.assertThat(amount).isEqualTo("200");
        Assertions.assertThat(cconfee).isEqualTo("4.72");
        Assertions.assertThat(pconfee).isEqualTo("9.44");

        String callbackResponse = successCallbackCreditLine.replace("{ESN}",esn).replace("{callbackAmount}",BankresponseAmount);

        InstaproxyHDFCIntentCallback callback = new InstaproxyHDFCIntentCallback(callbackResponse, esn);
        callback.execute();

        Thread.sleep(2000);

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.HDFU.toString())
                .validateMid(mid)
                .AssertAll();

        Assertions.assertThat(txnStatus.execute().getBody().prettyPrint()).contains("UPI_CREDITLINE");

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59551")
    @Test(description = "validate UPI Intent HDFU for Mdr and PCF and platform Upi Savings")
    public void validateUpiIntentHDFUforMDRandPCFandPlatformUpiSavings() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.MDR_PCF_PLATFORM_FEE_ON_HDFC_UPI_SUBTYPE;
        String mid = Constants.MerchantType.MDR_PCF_PLATFORM_FEE_ON_HDFC_UPI_SUBTYPE.getId();
        String txnAmount = "200";
        String BankresponseAmount = "204.72";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("3D")
                .setRiskExtendInfo("userLBSLatitude:26.88|userLBSLongitude:81.01|userAgent:Mozilla/5.0 (Linux; Android 8.1.0; Redmi Note 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36|timeZone:Asia/Calcutta|operationType:PAYMENT|networkType:4g|businessFlow:JS_CHECKOUT|amount:2|language:en-US|screenResolution:393X786|deviceType:mobile|channelId:WAP|platform:mWeb|osType:Android|deviceModel:Redmi Note 5|browserType:Chrome|browserVersion:96.0.4664.104|osVersion:8.1.0|deviceManufacturer:Xiaomi")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        String deeplink = ptcResponse.getBody().getDeepLinkInfo().getDeepLink();
        String bankRrn = CommonHelpers.generateOrderId();

        String qrData =  deeplink;
        Map<String, String> deeplinkInfo = parseDeeplinkInfo(qrData);
        String payeeVpa = deeplinkInfo.get("payeeVpa");
        String amount = deeplinkInfo.get("amount");
        String esn = deeplinkInfo.get("tr");
        String cconfee = deeplinkInfo.get("cconfee");
        String pconfee = deeplinkInfo.get("pconfee");

        Assertions.assertThat(amount).isEqualTo("204.72");
        Assertions.assertThat(cconfee).isEqualTo("4.72");
        Assertions.assertThat(pconfee).isNull();

        String callbackResponse = successCallbackSavings.replace("{ESN}",esn).replace("{callbackAmount}",BankresponseAmount);

        InstaproxyHDFCIntentCallback callback = new InstaproxyHDFCIntentCallback(callbackResponse, esn);
        callback.execute();

        Thread.sleep(2000);

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.HDFU.toString())
                .validateMid(mid)
                .AssertAll();

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59551")
    @Test(description = "validate UPI Intent HDFU for MDR and PCF  and platform Upi Creditline")
    public void validateUpiIntentHDFUforMDRandPCFandPlatformUpiCrediline() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.MDR_PCF_PLATFORM_FEE_ON_HDFC_UPI_SUBTYPE;
        String mid = Constants.MerchantType.MDR_PCF_PLATFORM_FEE_ON_HDFC_UPI_SUBTYPE.getId();
        String txnAmount = "200";
        String BankresponseAmount = "209.44";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("3D")
                .setRiskExtendInfo("userLBSLatitude:26.88|userLBSLongitude:81.01|userAgent:Mozilla/5.0 (Linux; Android 8.1.0; Redmi Note 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36|timeZone:Asia/Calcutta|operationType:PAYMENT|networkType:4g|businessFlow:JS_CHECKOUT|amount:2|language:en-US|screenResolution:393X786|deviceType:mobile|channelId:WAP|platform:mWeb|osType:Android|deviceModel:Redmi Note 5|browserType:Chrome|browserVersion:96.0.4664.104|osVersion:8.1.0|deviceManufacturer:Xiaomi")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        String deeplink = ptcResponse.getBody().getDeepLinkInfo().getDeepLink();
        String bankRrn = CommonHelpers.generateOrderId();

        String qrData =  deeplink;
        Map<String, String> deeplinkInfo = parseDeeplinkInfo(qrData);
        String payeeVpa = deeplinkInfo.get("payeeVpa");
        String amount = deeplinkInfo.get("amount");
        String esn = deeplinkInfo.get("tr");
        String cconfee = deeplinkInfo.get("cconfee");
        String pconfee = deeplinkInfo.get("pconfee");

        Assertions.assertThat(amount).isEqualTo("204.72");
        Assertions.assertThat(cconfee).isEqualTo("4.72");
        Assertions.assertThat(pconfee).isNull();

        String callbackResponse = successCallbackCreditLine.replace("{ESN}",esn).replace("{callbackAmount}",BankresponseAmount);

        InstaproxyHDFCIntentCallback callback = new InstaproxyHDFCIntentCallback(callbackResponse, esn);
        callback.execute();

        Thread.sleep(2000);

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.HDFU.toString())
                .validateMid(mid)
                .AssertAll();

        Assertions.assertThat(txnStatus.execute().getBody().prettyPrint()).contains("UPI_CREDITLINE");

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59551")
    @Test(description = "validate UPI Intent HDFU for MDR and PCF  and platform Upi Creditline FGW_PAID_AMOUNT_MISMATCH Error ")
    public void validateUpiIntentHDFUforMDRandPCFandPlatformUpiCredilineFGW_PAID_AMOUNT_MISMATCHError() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.MDR_PCF_PLATFORM_FEE_ON_HDFC_UPI_SUBTYPE;
        String mid = Constants.MerchantType.MDR_PCF_PLATFORM_FEE_ON_HDFC_UPI_SUBTYPE.getId();
        String txnAmount = "200";
        String BankresponseAmount = "204.72";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("3D")
                .setRiskExtendInfo("userLBSLatitude:26.88|userLBSLongitude:81.01|userAgent:Mozilla/5.0 (Linux; Android 8.1.0; Redmi Note 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36|timeZone:Asia/Calcutta|operationType:PAYMENT|networkType:4g|businessFlow:JS_CHECKOUT|amount:2|language:en-US|screenResolution:393X786|deviceType:mobile|channelId:WAP|platform:mWeb|osType:Android|deviceModel:Redmi Note 5|browserType:Chrome|browserVersion:96.0.4664.104|osVersion:8.1.0|deviceManufacturer:Xiaomi")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        String deeplink = ptcResponse.getBody().getDeepLinkInfo().getDeepLink();
        String bankRrn = CommonHelpers.generateOrderId();

        String qrData =  deeplink;
        Map<String, String> deeplinkInfo = parseDeeplinkInfo(qrData);
        String payeeVpa = deeplinkInfo.get("payeeVpa");
        String amount = deeplinkInfo.get("amount");
        String esn = deeplinkInfo.get("tr");
        String cconfee = deeplinkInfo.get("cconfee");
        String pconfee = deeplinkInfo.get("pconfee");

        Assertions.assertThat(amount).isEqualTo("204.72");
        Assertions.assertThat(cconfee).isEqualTo("4.72");
        Assertions.assertThat(pconfee).isNull();

        String callbackResponse = successCallbackCreditLine.replace("{ESN}",esn).replace("{callbackAmount}",BankresponseAmount);

        InstaproxyHDFCIntentCallback callback = new InstaproxyHDFCIntentCallback(callbackResponse, esn);
        callback.execute();

        Thread.sleep(4000);

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,esn);
        String validateparam = "\"resultCode\":\"FGW_PAID_AMOUNT_MISMATCH\"";
        Assertions.assertThat(logs).contains(validateparam);

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.execute();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.PENDING.toString())
                .validateRespCode("402")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.HDFU.toString())
                .validateMid(mid)
                .AssertAll();

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59551")
    @Test(description = "validate UPI Intent HDFU for PCF Upi Creditline Failure")
    public void validateUpiIntentHDFUforPCFUpiCredilineFailure() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.PCF_FEE_ON_HDFC_UPI_SUBTYPE;
        String mid = Constants.MerchantType.PCF_FEE_ON_HDFC_UPI_SUBTYPE.getId();
        String txnAmount = "200";
        String BankresponseAmount = "204.72";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("3D")
                .setRiskExtendInfo("userLBSLatitude:26.88|userLBSLongitude:81.01|userAgent:Mozilla/5.0 (Linux; Android 8.1.0; Redmi Note 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36|timeZone:Asia/Calcutta|operationType:PAYMENT|networkType:4g|businessFlow:JS_CHECKOUT|amount:2|language:en-US|screenResolution:393X786|deviceType:mobile|channelId:WAP|platform:mWeb|osType:Android|deviceModel:Redmi Note 5|browserType:Chrome|browserVersion:96.0.4664.104|osVersion:8.1.0|deviceManufacturer:Xiaomi")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        String deeplink = ptcResponse.getBody().getDeepLinkInfo().getDeepLink();
        String bankRrn = CommonHelpers.generateOrderId();

        String qrData =  deeplink;
        Map<String, String> deeplinkInfo = parseDeeplinkInfo(qrData);
        String payeeVpa = deeplinkInfo.get("payeeVpa");
        String amount = deeplinkInfo.get("amount");
        String esn = deeplinkInfo.get("tr");
        String cconfee = deeplinkInfo.get("cconfee");
        String pconfee = deeplinkInfo.get("pconfee");

        Assertions.assertThat(amount).isEqualTo("200");
        Assertions.assertThat(cconfee).isEqualTo("4.72");
        Assertions.assertThat(pconfee).isEqualTo("9.44");

        String callbackResponse = failureCallbackCreditLine.replace("{ESN}",esn).replace("{callbackAmount}",BankresponseAmount);

        InstaproxyHDFCIntentCallback callback = new InstaproxyHDFCIntentCallback(callbackResponse, esn);
        callback.execute();

        Thread.sleep(2000);

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_FAILURE.toString())
                .validateRespCode("227")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.HDFU.toString())
                .validateMid(mid)
                .AssertAll();
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59551")
    @Test(description = "validate UPI Intent HDFU for PCF Upi Creditline with refundType R ")
    public void validateUpiIntentHDFUforPCFUpiCredilinewithrefundTypeR() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.PCF_FEE_ON_HDFC_UPI_SUBTYPE;
        String mid = Constants.MerchantType.PCF_FEE_ON_HDFC_UPI_SUBTYPE.getId();
        String txnAmount = "200";
        String BankresponseAmount = "204.72";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("3D")
                .setRiskExtendInfo("userLBSLatitude:26.88|userLBSLongitude:81.01|userAgent:Mozilla/5.0 (Linux; Android 8.1.0; Redmi Note 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36|timeZone:Asia/Calcutta|operationType:PAYMENT|networkType:4g|businessFlow:JS_CHECKOUT|amount:2|language:en-US|screenResolution:393X786|deviceType:mobile|channelId:WAP|platform:mWeb|osType:Android|deviceModel:Redmi Note 5|browserType:Chrome|browserVersion:96.0.4664.104|osVersion:8.1.0|deviceManufacturer:Xiaomi")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        String deeplink = ptcResponse.getBody().getDeepLinkInfo().getDeepLink();
        String bankRrn = CommonHelpers.generateOrderId();

        String qrData =  deeplink;
        Map<String, String> deeplinkInfo = parseDeeplinkInfo(qrData);
        String payeeVpa = deeplinkInfo.get("payeeVpa");
        String amount = deeplinkInfo.get("amount");
        String esn = deeplinkInfo.get("tr");
        String cconfee = deeplinkInfo.get("cconfee");
        String pconfee = deeplinkInfo.get("pconfee");

        Assertions.assertThat(amount).isEqualTo("200");
        Assertions.assertThat(cconfee).isEqualTo("4.72");
        Assertions.assertThat(pconfee).isEqualTo("9.44");

        String callbackResponse = successCallbackCreditLine.replace("{ESN}",esn).replace("{callbackAmount}",BankresponseAmount);

        InstaproxyHDFCIntentCallback callback = new InstaproxyHDFCIntentCallback(callbackResponse, esn);
        callback.execute();

        Thread.sleep(2000);

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.HDFU.toString())
                .validateMid(mid)
                .AssertAll();

        Assertions.assertThat(txnStatus.execute().getBody().prettyPrint()).contains("UPI_CREDITLINE");

        String refundAmount = "200";
        SyncRefund refundWithChargeAmount = new SyncRefund();
        Response asyncRefundResp = given().spec(refundWithChargeAmount.reqSpecAsyncRefundtxnType(merchant, initTxnDTO.txnAmountFromBody(), orderId,
                        txnStatus.getResponse().getTXNID(), "R"))
                .post().then().extract().response();
        System.out.println("Async Refund Response " + asyncRefundResp.body().prettyPrint());
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.orderId")).isEqualTo(orderId);
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.mid")).isEqualTo(mid);
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.refId")).isNotNull();
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo(Constants.TXNSTATUS.PENDING.toString());
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo(Constants.ResponseCode.REFUND_PENDING.getRespCode());
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo(Constants.ResponseCode.REFUND_PENDING.getRespMsg());
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.refundId")).isNotNull();
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.txnId")).isEqualTo(txnStatus.getResponse().getTXNID());
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.refundAmount")).isEqualTo(refundAmount);

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59551")
    @Test(description = "validate UPI Intent HDFU for PCF Upi Creditline with refundType Refund ")
    public void validateUpiIntentHDFUforPCFUpiCredilinewithrefundTypeRefund() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.PCF_FEE_ON_HDFC_UPI_SUBTYPE;
        String mid = Constants.MerchantType.PCF_FEE_ON_HDFC_UPI_SUBTYPE.getId();
        String txnAmount = "200";
        String BankresponseAmount = "204.72";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("3D")
                .setRiskExtendInfo("userLBSLatitude:26.88|userLBSLongitude:81.01|userAgent:Mozilla/5.0 (Linux; Android 8.1.0; Redmi Note 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36|timeZone:Asia/Calcutta|operationType:PAYMENT|networkType:4g|businessFlow:JS_CHECKOUT|amount:2|language:en-US|screenResolution:393X786|deviceType:mobile|channelId:WAP|platform:mWeb|osType:Android|deviceModel:Redmi Note 5|browserType:Chrome|browserVersion:96.0.4664.104|osVersion:8.1.0|deviceManufacturer:Xiaomi")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        String deeplink = ptcResponse.getBody().getDeepLinkInfo().getDeepLink();
        String bankRrn = CommonHelpers.generateOrderId();

        String qrData =  deeplink;
        Map<String, String> deeplinkInfo = parseDeeplinkInfo(qrData);
        String payeeVpa = deeplinkInfo.get("payeeVpa");
        String amount = deeplinkInfo.get("amount");
        String esn = deeplinkInfo.get("tr");
        String cconfee = deeplinkInfo.get("cconfee");
        String pconfee = deeplinkInfo.get("pconfee");

        Assertions.assertThat(amount).isEqualTo("200");
        Assertions.assertThat(cconfee).isEqualTo("4.72");
        Assertions.assertThat(pconfee).isEqualTo("9.44");

        String callbackResponse = successCallbackCreditLine.replace("{ESN}",esn).replace("{callbackAmount}",BankresponseAmount);

        InstaproxyHDFCIntentCallback callback = new InstaproxyHDFCIntentCallback(callbackResponse, esn);
        callback.execute();

        Thread.sleep(2000);

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.HDFU.toString())
                .validateMid(mid)
                .AssertAll();

        Assertions.assertThat(txnStatus.execute().getBody().prettyPrint()).contains("UPI_CREDITLINE");

        String refundAmount = "200";
        SyncRefund refundWithChargeAmount = new SyncRefund();
        Response asyncRefundResp = given().spec(refundWithChargeAmount.reqSpecAsyncRefundtxnType(merchant, initTxnDTO.txnAmountFromBody(), orderId,
                        txnStatus.getResponse().getTXNID(), "Refund"))
                .post().then().extract().response();
        System.out.println("Async Refund Response " + asyncRefundResp.body().prettyPrint());
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.orderId")).isEqualTo(orderId);
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.mid")).isEqualTo(mid);
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.refId")).isNotNull();
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo(Constants.TXNSTATUS.PENDING.toString());
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo(Constants.ResponseCode.REFUND_PENDING.getRespCode());
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo(Constants.ResponseCode.REFUND_PENDING.getRespMsg());
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.refundId")).isNotNull();
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.txnId")).isEqualTo(txnStatus.getResponse().getTXNID());
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.refundAmount")).isEqualTo(refundAmount);

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59551")
    @Test(description = "validate UPI Intent HDFU for MDR and PCF  and platform Upi Creditline with refundType R")
    public void validateUpiIntentHDFUforMDRandPCFandPlatformUpiCredilinewithrefundTypeR() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.MDR_PCF_PLATFORM_FEE_ON_HDFC_UPI_SUBTYPE;
        String mid = Constants.MerchantType.MDR_PCF_PLATFORM_FEE_ON_HDFC_UPI_SUBTYPE.getId();
        String txnAmount = "200";
        String BankresponseAmount = "209.44";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("3D")
                .setRiskExtendInfo("userLBSLatitude:26.88|userLBSLongitude:81.01|userAgent:Mozilla/5.0 (Linux; Android 8.1.0; Redmi Note 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36|timeZone:Asia/Calcutta|operationType:PAYMENT|networkType:4g|businessFlow:JS_CHECKOUT|amount:2|language:en-US|screenResolution:393X786|deviceType:mobile|channelId:WAP|platform:mWeb|osType:Android|deviceModel:Redmi Note 5|browserType:Chrome|browserVersion:96.0.4664.104|osVersion:8.1.0|deviceManufacturer:Xiaomi")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        String deeplink = ptcResponse.getBody().getDeepLinkInfo().getDeepLink();
        String bankRrn = CommonHelpers.generateOrderId();

        String qrData =  deeplink;
        Map<String, String> deeplinkInfo = parseDeeplinkInfo(qrData);
        String payeeVpa = deeplinkInfo.get("payeeVpa");
        String amount = deeplinkInfo.get("amount");
        String esn = deeplinkInfo.get("tr");
        String cconfee = deeplinkInfo.get("cconfee");
        String pconfee = deeplinkInfo.get("pconfee");

        Assertions.assertThat(amount).isEqualTo("204.72");
        Assertions.assertThat(cconfee).isEqualTo("4.72");
        Assertions.assertThat(pconfee).isNull();

        String callbackResponse = successCallbackCreditLine.replace("{ESN}",esn).replace("{callbackAmount}",BankresponseAmount);

        InstaproxyHDFCIntentCallback callback = new InstaproxyHDFCIntentCallback(callbackResponse, esn);
        callback.execute();

        Thread.sleep(2000);

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.HDFU.toString())
                .validateMid(mid)
                .AssertAll();

        Assertions.assertThat(txnStatus.execute().getBody().prettyPrint()).contains("UPI_CREDITLINE");

        String refundAmount = "200";
        SyncRefund refundWithChargeAmount = new SyncRefund();
        Response asyncRefundResp = given().spec(refundWithChargeAmount.reqSpecAsyncRefundtxnType(merchant, initTxnDTO.txnAmountFromBody(), orderId,
                        txnStatus.getResponse().getTXNID(), "R"))
                .post().then().extract().response();
        System.out.println("Async Refund Response " + asyncRefundResp.body().prettyPrint());
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.orderId")).isEqualTo(orderId);
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.mid")).isEqualTo(mid);
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.refId")).isNotNull();
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo(Constants.TXNSTATUS.PENDING.toString());
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo(Constants.ResponseCode.REFUND_PENDING.getRespCode());
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo(Constants.ResponseCode.REFUND_PENDING.getRespMsg());
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.refundId")).isNotNull();
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.txnId")).isEqualTo(txnStatus.getResponse().getTXNID());
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.refundAmount")).isEqualTo(refundAmount);

    }

    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PG-1634")
    @Test(description = "Validate final payment amount for Upi Intent PCF")
    public void validateFinalPaymentAmountForUpiIntentPCF() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String mid = Constants.MerchantType.PCF_FEE_ON_UPI_SUBTYPE.getId();
        String txnAmount = "200";
        String txnAmountupipsp = "204.72";
        String paymentFee = "4.72";
        String payeeVPA = "paytm-9759417322@ptybl";
        String payerPaymentInstrument = "UPI_CREDIT_CARD";
        String callbackpaymentInstrument = "CREDIT_CARD";
        String subAccountTypeValue = "";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.PCF_FEE_ON_UPI_SUBTYPE).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("3D")
                .setRiskExtendInfo("userLBSLatitude:26.88|userLBSLongitude:81.01|userAgent:Mozilla/5.0 (Linux; Android 8.1.0; Redmi Note 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36|timeZone:Asia/Calcutta|operationType:PAYMENT|networkType:4g|businessFlow:JS_CHECKOUT|amount:2|language:en-US|screenResolution:393X786|deviceType:mobile|channelId:WAP|platform:mWeb|osType:Android|deviceModel:Redmi Note 5|browserType:Chrome|browserVersion:96.0.4664.104|osVersion:8.1.0|deviceManufacturer:Xiaomi")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        String deeplink = ptcResponse.getBody().getDeepLinkInfo().getDeepLink();
        String externalSerialNo = deeplink.substring(deeplink.indexOf("tr")+3,deeplink.indexOf("&am"));
        String bankRrn = CommonHelpers.generateOrderId();
        System.out.println(externalSerialNo);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        UpiPspConsultFee upiPspConsultFeeobject =new UpiPspConsultFee( mid,  txnAmountupipsp, paymentFee ,  payeeVPA,  externalSerialNo ,  payerPaymentInstrument,  subAccountTypeValue) ;
        JsonPath responseJsonPath = upiPspConsultFeeobject.execute().jsonPath();
        Assertions.assertThat(responseJsonPath.getString("body.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(responseJsonPath.getString("body.resultCodeId")).isEqualTo("001");
        Assertions.assertThat(responseJsonPath.getString("body.resultMsg")).isEqualTo("success");
        Assertions.assertThat(responseJsonPath.getString("body.mid")).isEqualTo(mid);
        Assertions.assertThat(responseJsonPath.getString("body.txnAmount")).isEqualTo(txnAmountupipsp);
        Assertions.assertThat(responseJsonPath.getString("body.externalSerialNo")).isEqualTo(externalSerialNo);
        Assertions.assertThat(responseJsonPath.getString("body.payerPaymentInstrument")).isEqualTo(payerPaymentInstrument);
        Assertions.assertThat(responseJsonPath.getString("body.convenienceFeeValidation")).isEqualTo("MATCH");

        InstaproxyPtybliResponse successresponse = new InstaproxyPtybliResponse(txnAmountupipsp,externalSerialNo ,bankRrn, callbackpaymentInstrument , "upiCcBin" );
        JsonPath callbackres = successresponse.execute().jsonPath();
        Assertions.assertThat(callbackres.getString("body.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(callbackres.getString("body.resultMsg")).isEqualTo("success");

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PTYBLI.toString())
                .validateMid(mid)
                //.validateFinalPaymentAmount(String.valueOf(Double.parseDouble(txnAmountupipsp)))
                .AssertAll();
    }

    @Owner(Constants.Owner.LOKESH_SAXENA)
    @Feature("PG-8232")
    @Test(description = "Payer IfSC and Payer Account Should Come In FluxnetPG2PaymentRequest")
    public void PayerIFSCAndPayerAccountShouldComeInFluxnetPG2PaymentRequest() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.MDR_FEE_ON_HDFC_UPI_SUBTYPE;
        String mid = Constants.MerchantType.MDR_FEE_ON_HDFC_UPI_SUBTYPE.getId();
        String txnAmount = "200";
        String BankresponseAmount = "200.00";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("3D")
                .setRiskExtendInfo("userLBSLatitude:26.88|userLBSLongitude:81.01|userAgent:Mozilla/5.0 (Linux; Android 8.1.0; Redmi Note 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36|timeZone:Asia/Calcutta|operationType:PAYMENT|networkType:4g|businessFlow:JS_CHECKOUT|amount:2|language:en-US|screenResolution:393X786|deviceType:mobile|channelId:WAP|platform:mWeb|osType:Android|deviceModel:Redmi Note 5|browserType:Chrome|browserVersion:96.0.4664.104|osVersion:8.1.0|deviceManufacturer:Xiaomi")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        String deeplink = ptcResponse.getBody().getDeepLinkInfo().getDeepLink();
        String bankRrn = CommonHelpers.generateOrderId();

        String qrData =  deeplink;
        Map<String, String> deeplinkInfo = parseDeeplinkInfo(qrData);
        String payeeVpa = deeplinkInfo.get("payeeVpa");
        String amount = deeplinkInfo.get("amount");
        String esn = deeplinkInfo.get("tr");
        String cconfee = deeplinkInfo.get("cconfee");
        String pconfee = deeplinkInfo.get("pconfee");

        String callbackResponse = successCallbackSavings.replace("{ESN}",esn).replace("{callbackAmount}",BankresponseAmount);

        InstaproxyHDFCIntentCallback callback = new InstaproxyHDFCIntentCallback(callbackResponse, esn);
        callback.execute();

        Thread.sleep(2000);

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.HDFU.toString())
                .validateMid(mid)
                .AssertAll();

        String fluxnetPaymentResultLogs = LogsValidationHelper.verifyLogsOnPod(
                PG2LogsValidationHelper.setEnvService.instaproxy, esn, "FLUXNET_UPI_PG2_PAYMENT_RESULT");
        SoftAssertions fluxnetAssertions = new SoftAssertions();
            fluxnetAssertions.assertThat(fluxnetPaymentResultLogs)
                    .as("FLUXNET_UPI_PG2_PAYMENT_RESULT log must contain upiAdditionalData")
                    .contains("upiAdditionalData");

            String upiAdditionalDataKey = "\"upiAdditionalData\":\"";
            int upiAdditionalDataStart = fluxnetPaymentResultLogs.indexOf(upiAdditionalDataKey);
            fluxnetAssertions.assertThat(upiAdditionalDataStart)
                    .as("upiAdditionalData field must be present in FLUXNET_UPI_PG2_PAYMENT_RESULT log")
                    .isGreaterThan(-1);

            String upiAdditionalDataBase64 = fluxnetPaymentResultLogs.substring(
                    upiAdditionalDataStart + upiAdditionalDataKey.length(),
                    fluxnetPaymentResultLogs.indexOf("\"", upiAdditionalDataStart + upiAdditionalDataKey.length()));
            JSONObject upiAdditionalDataJson = new JSONObject(PGPHelpers.Base64Decode(upiAdditionalDataBase64));
            fluxnetAssertions.assertThat(upiAdditionalDataJson.optString("payerAccount", null))
                    .as("Decoded upiAdditionalData must contain payerAccount")
                    .isEqualTo("857609478532522");
            fluxnetAssertions.assertThat(upiAdditionalDataJson.optString("payerIfsc", null))
                    .as("Decoded upiAdditionalData must contain payerIfsc")
                    .isEqualTo("AABC0876543");
            fluxnetAssertions.assertAll();
    }

}
