package scripts.api.NetBanking;

import com.paytm.LocalConfig;
import com.paytm.api.CloseOrderAPI;
import com.paytm.api.CloseOrderV2Api;
import com.paytm.api.TxnStatus;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
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
import com.paytm.dto.checkoutjs.MerchantConfig;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.dto.processTransactionV1.response.ProcessTxnV1Response;
import com.paytm.framework.reporting.Reporter;
import com.paytm.pages.*;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.Date;

import static com.paytm.appconstants.Constants.Owner.*;

public class NBPG2 extends PGPBaseTest {
    private final CheckoutJsCheckoutPage checkoutJsPage = new CheckoutJsCheckoutPage();

    public JsonPath Validate_FetchPayInstrument(String txnToken, InitTxnDTO initTxnDTO, String payMethod, String status) {
        Reporter.report.info("Validating fetch pay options for the merchant and txn token");
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.param("status", status).getList(
                        "body.merchantPayOption.paymentModes.findAll { paymentModes -> paymentModes.isDisabled.status == status }.paymentMode"))
                .contains(payMethod);
        return fetchPaymentOptionsJson;
    }
    public void assertTheiaTxnStatusCommonResponse(Response theiaTxnStatusResponse, InitTxnDTO initTxnDTO, Constants.ResponseCode responseCode){
        Assertions.assertThat(theiaTxnStatusResponse.jsonPath().getString("body.txnInfo.CURRENCY")).isEqualTo("INR");
        Assertions.assertThat(theiaTxnStatusResponse.jsonPath().getString("body.txnInfo.MID")).isEqualTo(initTxnDTO.getBody().getMid());
        Assertions.assertThat(theiaTxnStatusResponse.jsonPath().getString("body.txnInfo.ORDERID")).isEqualTo(initTxnDTO.getBody().getOrderId());
        Assertions.assertThat(theiaTxnStatusResponse.jsonPath().getString("body.txnInfo.RESPCODE")).isEqualTo(responseCode.getRespCode());
        Assertions.assertThat(theiaTxnStatusResponse.jsonPath().getString("body.txnInfo.RESPMSG")).isEqualTo(responseCode.getRespMsg());
        Assertions.assertThat(theiaTxnStatusResponse.jsonPath().getString("body.txnInfo.TXNAMOUNT")).isEqualTo(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.getBody().getTxnAmount().getValue()));
    }
    public void assertPTCCommonResponse(ProcessTxnV1Response ptcResponse){
        Assertions.assertThat(ptcResponse.getBody().getBankForm().getPageType()).isEqualTo("redirect");
        Assertions.assertThat(ptcResponse.getBody().getBankForm().getRedirectForm().getActionUrl()).isNotNull();
        Assertions.assertThat(ptcResponse.getBody().getBankForm().getRedirectForm().getMethod()).isEqualTo("post");
        Assertions.assertThat(ptcResponse.getBody().getBankForm().getRedirectForm().getType()).isEqualTo("redirect");
        Assertions.assertThat(ptcResponse.getBody().getBankForm().getRedirectForm().getHeaders()).isNotNull();
        Assertions.assertThat(ptcResponse.getBody().getBankForm().getRedirectForm().getHeaders()).isNotNull();
        Assertions.assertThat(ptcResponse.getBody().getBankForm().getRedirectForm().getContent()).isNotNull();
    }
    public void assertPTCCommonResponse(ProcessTxnV1Response ptcResponse, InitTxnDTO initTxnDTO) {
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getBANKTXNID()).isNotNull();
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getCURRENCY()).isEqualTo("INR");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getMID()).isEqualTo(initTxnDTO.getBody().getMid());
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getORDERID()).isEqualTo(initTxnDTO.getBody().getOrderId());
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getTXNAMOUNT()).isEqualTo(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.getBody().getTxnAmount().getValue()));
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getTXNDATE()).isNotNull();
    }
    @Owner(PRAMOD_KUMAR)
    @Feature("PG2-6842")
    @Test(description = "Validate Success NB transaction for PG2 MID routed through PG2")
    public void validateSuccessNBTxnPG2MID() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String mid = Constants.MerchantType.UPIPUSHPG2.getId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),  Constants.MerchantType.UPIPUSHPG2)
                .setOrderId(CommonHelpers.generateOrderId())
                .build();
        String newOrderId=initTxnDTO.getBody().getOrderId();
        initTxnDTO.getBody().setOrderId(newOrderId);
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO,"NET_BANKING", "false");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),newOrderId,"PTM" + newOrderId)
                .setPaymentMode("NET_BANKING")
                .setChannelCode(Constants.Bank.ICICI.toString())
                .setAuthMode("USRPWD")
                .setMpin("1234")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        CommonHelpers.webFormContextNotify(newOrderId);
        NativeHelpers.submitJsonFormInBrowser(ptcResponse.toString());
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(mid)
                .validateOrderId(initTxnDTO.orderFromBody())
                .validatePaymentMode("NB")
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_SUCCESS.getRespMsg())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody())).validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Bank.ICICI.toString())
                .validateBankName(Constants.Bank.ICICINB.toString())
                .assertAll();
        CommonHelpers.verifyPaymentNotify(newOrderId);
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_SUCCESS.getRespMsg())
                .validatePaymentMode("NB")
                .validateGatewayName(Constants.Gateway.ICICI.toString())
                .validateMid(mid)
                .AssertAll();
    }
    @Owner(PRAMOD_KUMAR)
    @Feature("PG2-6842")
    @Test(description = "Validate Pending NB transaction for PG2 MID routed through PG2, close order V1 and then check merchant status")
    public void validatePendingNBTxnPG2MIDCloseOrderV1ThenCheckMerchantStatus() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.UPIPUSHPG2;
        String mid = merchant.getId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue("99.51")
                .setOrderId(LocalConfig.ENV_NAME+"_"+CommonHelpers.generateOrderId())
                .build();
        String newOrderId=initTxnDTO.getBody().getOrderId();
        initTxnDTO.getBody().setOrderId(newOrderId);
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO,"NET_BANKING", "false");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),newOrderId,"PTM" + newOrderId)
                .setPaymentMode("NET_BANKING")
                .setChannelCode(Constants.Bank.ICICI.toString())
                .setAuthMode("USRPWD")
                .setMpin("1234")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        NativeHelpers.submitJsonFormInBrowser(ptcResponse.toString());
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateCurrency("INR")
                .validateMid(mid)
                .validateOrderId(initTxnDTO.orderFromBody())
                .validatePaymentMode("NB")
                .validateRespCode(Constants.ResponseCode.TXN_STATUS_PENDING.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_STATUS_PENDING.getRespMsg())
                .validateStatus(Constants.TXNSTATUS.PENDING.toString())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankName(Constants.Bank.ICICINB.toString())
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), newOrderId);
        txnStatus.executeUntilPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(newOrderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.PENDING.toString())
                .validateRespCode(Constants.ResponseCode.MERCHANT_STATUS_TXN_PENDING.getRespCode())
                .validateRespMsg(Constants.ResponseCode.MERCHANT_STATUS_TXN_PENDING.getRespMsg())
                .validatePaymentMode("NB")
                .validateGatewayName(Constants.Gateway.ICICI.toString())
                .validateMid(mid)
                .AssertAll();
        CloseOrderDTO closeOrderDTO = new CloseOrderDTO();
        closeOrderDTO.setHead(new com.paytm.dto.CloseOrder.Head().setChannelId("WAP").setVersion("v1").setSigature(""))
                .setBody(new com.paytm.dto.CloseOrder.Body().setIsForceClose(true).setOrderId(newOrderId).setMid(mid));
        CloseOrderAPI closeOrderAPI = new CloseOrderAPI(closeOrderDTO);
        Response closeOrderResponse = closeOrderAPI.execute();
        Assertions.assertThat(closeOrderResponse.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("SUCCESS");
        Assertions.assertThat(closeOrderResponse.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(closeOrderResponse.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo("01");
        CommonHelpers.closeOrderNotify(newOrderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(newOrderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_FAILURE.toString())
                .validateRespCode(Constants.ResponseCode.PAYMENT_FAILED.getRespCode())
                .validateRespMsg(Constants.ResponseCode.PAYMENT_FAILED.getRespMsg())
                .validatePaymentMode("NB")
                .validateMid(mid)
                .AssertAll();
    }
    @Owner(PRAMOD_KUMAR)
    @Feature("PG2-6842")
    @Test(description = "Validate Pending NB transaction for PG2 MID routed through PG2, close order V2 and then check merchant status")
    public void validatePendingNBTxnPG2MIDCloseOrderV2ThenCheckMerchantStatus() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.UPIPUSHPG2;
        String mid = merchant.getId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue("99.51")
                .setOrderId(LocalConfig.ENV_NAME+"_"+CommonHelpers.generateOrderId())
                .build();
        String newOrderId=initTxnDTO.getBody().getOrderId();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO,"NET_BANKING", "false");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),newOrderId,"PTM" + newOrderId)
                .setPaymentMode("NET_BANKING")
                .setChannelCode(Constants.Bank.ICICI.toString())
                .setAuthMode("USRPWD")
                .setMpin("1234")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        NativeHelpers.submitJsonFormInBrowser(ptcResponse.toString());
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateCurrency("INR")
                .validateMid(mid)
                .validateOrderId(initTxnDTO.orderFromBody())
                .validatePaymentMode("NB")
                .validateRespCode(Constants.ResponseCode.TXN_STATUS_PENDING.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_STATUS_PENDING.getRespMsg())
                .validateStatus(Constants.TXNSTATUS.PENDING.toString())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Bank.ICICI.toString())
                .validateBankName(Constants.Bank.ICICINB.toString())
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), newOrderId);
        txnStatus.executeUntilPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(newOrderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.PENDING.toString())
                .validateRespCode(Constants.ResponseCode.MERCHANT_STATUS_TXN_PENDING.getRespCode())
                .validateRespMsg(Constants.ResponseCode.MERCHANT_STATUS_TXN_PENDING.getRespMsg())
                .validatePaymentMode("NB")
                .validateGatewayName(Constants.Gateway.ICICI.toString())
                .validateMid(mid)
                .AssertAll();
        String txnId = txnStatus.txnStatusResponse.TXNID;
        CloseOrderV2Api closeOrderV2Api = new CloseOrderV2Api("TXN_TOKEN", mid, newOrderId, initTxnResponse.getBody().getTxnToken(), "true");
        Response closeOrderResponse = closeOrderV2Api.execute();
        Assertions.assertThat(closeOrderResponse.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("SUCCESS");
        Assertions.assertThat(closeOrderResponse.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(closeOrderResponse.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo(Constants.ResponseCode.TXN_SUCCESS.getRespCode());
        Assertions.assertThat(closeOrderResponse.jsonPath().getString("body.txnInfo.CURRENCY")).isEqualTo("INR");
        Assertions.assertThat(closeOrderResponse.jsonPath().getString("body.txnInfo.MID")).isEqualTo(mid);
        Assertions.assertThat(closeOrderResponse.jsonPath().getString("body.txnInfo.ORDERID")).isEqualTo(newOrderId);
        Assertions.assertThat(closeOrderResponse.jsonPath().getString("body.txnInfo.RESPCODE")).isEqualTo(Constants.ResponseCode.CLOSEORDERV2_TXN_PENDING.getRespCode());
        Assertions.assertThat(closeOrderResponse.jsonPath().getString("body.txnInfo.RESPMSG")).isEqualTo(Constants.ResponseCode.CLOSEORDERV2_TXN_PENDING.getRespMsg());
        Assertions.assertThat(closeOrderResponse.jsonPath().getString("body.txnInfo.STATUS")).isEqualTo(Constants.TXNSTATUS.TXN_FAILURE.toString());
        Assertions.assertThat(closeOrderResponse.jsonPath().getString("body.txnInfo.TXNAMOUNT")).isEqualTo(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.getBody().getTxnAmount().getValue()));
        Assertions.assertThat(closeOrderResponse.jsonPath().getString("body.txnInfo.TXNID")).isEqualTo(txnId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(newOrderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_FAILURE.toString())
                .validateRespCode(Constants.ResponseCode.TXN_FAILURE.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_FAILURE.getRespMsg())
                .validatePaymentMode("NB")
                .validateMid(mid)
                .AssertAll();
    }

    @Owner(PRAMOD_KUMAR)
    @Feature("PG2-6842")
    @Test(description = "Validate that if user id is not present in FF4J Flag theia.payment.adapter.feature then txn should be routed to P+")
    public void userNotPrsentInFF4JNBTxnRoutedToPPlus() throws Exception{
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        Constants.MerchantType merchant = Constants.MerchantType.UPIPUSHPG2;
        String mid = merchant.getId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setOrderId(LocalConfig.ENV_NAME+"_"+CommonHelpers.generateOrderId())
                .build();
        String newOrderId=initTxnDTO.getBody().getOrderId();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO,"NET_BANKING", "false");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),newOrderId,"PTM" + newOrderId)
                .setPaymentMode("NET_BANKING")
                .setChannelCode(Constants.Bank.ICICI.toString())
                .setAuthMode("USRPWD")
                .setMpin("1234")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        NativeHelpers.submitJsonFormInBrowser(ptcResponse.toString());
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(mid)
                .validateOrderId(initTxnDTO.orderFromBody())
                .validatePaymentMode("NB")
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_SUCCESS.getRespMsg())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody())).validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Bank.ICICI.toString())
                .validateBankName(Constants.Bank.ICICINB.toString())
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_SUCCESS.getRespMsg())
                .validatePaymentMode("NB")
                .validateGatewayName(Constants.Gateway.ICICI.toString())
                .validateMid(mid)
                .AssertAll();
    }
    @Owner(PRAMOD_KUMAR)
    @Feature("PG2-6248")
    @Test(description = "Validate that if mid is not present in FF4J Flag theia.payment.adapter.feature but pref is enabled on mid then txn should be routed to PG2, txn is pending then close order")
    public void midNotPrsentInFF4JButPrefEnabledNBRoutedToPG2PendingTxn() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.UPIPUSHPG2FF4JNOPREFYES;
        String mid = merchant.getId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setOrderId(LocalConfig.ENV_NAME+"_"+CommonHelpers.generateOrderId()).setTxnValue("99.51")
                .build();
        String newOrderId=initTxnDTO.getBody().getOrderId();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO,"NET_BANKING", "false");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),newOrderId,"PTM" + newOrderId)
                .setPaymentMode("NET_BANKING")
                .setChannelCode(Constants.Bank.ICICI.toString())
                .setAuthMode("USRPWD")
                .setMpin("1234")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        NativeHelpers.submitJsonFormInBrowser(ptcResponse.toString());
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateCurrency("INR")
                .validateMid(mid)
                .validateOrderId(newOrderId)
                .validatePaymentMode("NB")
                .validateRespCode(Constants.ResponseCode.TXN_STATUS_PENDING.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_STATUS_PENDING.getRespMsg())
                .validateStatus(Constants.TXNSTATUS.PENDING.toString())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankName(Constants.Bank.ICICINB.toString())
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), newOrderId);
        txnStatus.executeUntilPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(newOrderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.PENDING.toString())
                .validateRespCode(Constants.ResponseCode.MERCHANT_STATUS_TXN_PENDING.getRespCode())
                .validateRespMsg(Constants.ResponseCode.MERCHANT_STATUS_TXN_PENDING.getRespMsg())
                .validateMid(mid)
                .AssertAll();
        CloseOrderDTO closeOrderDTO = new CloseOrderDTO();
        closeOrderDTO.setHead(new com.paytm.dto.CloseOrder.Head().setChannelId("WAP").setVersion("v1").setSigature(""))
                .setBody(new com.paytm.dto.CloseOrder.Body().setIsForceClose(true).setOrderId(newOrderId).setMid(mid));
        CloseOrderAPI closeOrderAPI = new CloseOrderAPI(closeOrderDTO);
        Response closeOrderResponse = closeOrderAPI.execute();
        Assertions.assertThat(closeOrderResponse.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("SUCCESS");
        Assertions.assertThat(closeOrderResponse.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(closeOrderResponse.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo("01");
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(newOrderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_FAILURE.toString())
                .validateRespCode(Constants.ResponseCode.TXN_FAILURE.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_FAILURE.getRespMsg())
                .validateMid(mid)
                .AssertAll();
    }
    @Owner(PRAMOD_KUMAR)
    @Feature("PG2-6842")
    @Test(description = "Validate that if user id is present but mid is not present in FF4J Flag theia.payment.adapter.feature then txn should be routed to P+")
    public void userPrsentMidNotPresentInFF4JNBTxnRoutedToPPlus() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.UPIPG2FF4JDISABLED;
        String mid = merchant.getId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setOrderId(LocalConfig.ENV_NAME+"_"+CommonHelpers.generateOrderId())
                .build();
        String newOrderId=initTxnDTO.getBody().getOrderId();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO,"NET_BANKING", "false");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),newOrderId,"PTM" + newOrderId)
                .setPaymentMode("NET_BANKING")
                .setChannelCode(Constants.Bank.ICICI.toString())
                .setAuthMode("USRPWD")
                .setMpin("1234")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        NativeHelpers.submitJsonFormInBrowser(ptcResponse.toString());
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(mid)
                .validateOrderId(initTxnDTO.orderFromBody())
                .validatePaymentMode("NB")
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_SUCCESS.getRespMsg())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody())).validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Bank.ICICI.toString())
                .validateBankName(Constants.Bank.ICICINB.toString())
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_SUCCESS.getRespMsg())
                .validatePaymentMode("NB")
                .validateGatewayName(Constants.Gateway.ICICI.toString())
                .validateMid(mid)
                .AssertAll();
    }
    @Owner(PRAMOD_KUMAR)
    @Feature("PG2-6842")
    @Test(description = "Validate Pending NB transaction for PG2 MID routed through PG2")
    public void validateCOPFullTrafficPendingNBTxnPG2MID() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.PG2_COP_FULL_TRAFFIC_Y;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setOrderId(LocalConfig.ENV_NAME+"_"+CommonHelpers.generateOrderId())
                .setTxnValue("99.51")
                .build();
        String newOrderId=initTxnDTO.getBody().getOrderId();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO,"NET_BANKING", "false");
        String mid = merchant.getId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),newOrderId,"PTM" + newOrderId)
                .setPaymentMode("NET_BANKING")
                .setChannelCode(Constants.Bank.ICICI.toString())
                .setAuthMode("USRPWD")
                .setMpin("1234")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        NativeHelpers.submitJsonFormInBrowser(ptcResponse.toString());
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateCurrency("INR")
                .validateMid(mid)
                .validateOrderId(initTxnDTO.orderFromBody())
                .validatePaymentMode("NB")
                .validateRespCode(Constants.ResponseCode.TXN_STATUS_PENDING.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_STATUS_PENDING.getRespMsg())
                .validateStatus(Constants.TXNSTATUS.PENDING.toString())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Bank.ICICI.toString())
                .validateBankName(Constants.Bank.ICICINB.toString())
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), newOrderId);
        txnStatus.executeUntilPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(newOrderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.PENDING.toString())
                .validateRespCode(Constants.ResponseCode.MERCHANT_STATUS_TXN_PENDING.getRespCode())
                .validateRespMsg(Constants.ResponseCode.MERCHANT_STATUS_TXN_PENDING.getRespMsg())
                .validatePaymentMode("NB")
                .validateGatewayName(Constants.Gateway.ICICI.toString())
                .validateMid(mid)
                .AssertAll();
    }
    @Owner(PRAMOD_KUMAR)
    @Feature("PG2-6842")
    @Test(description = "Validate Full traffic Success COP NB transaction for PG2 MID routed through PG2")
    public void validateFullTrafficCOPSuccessNBTxn() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.PG2_COP_FULL_TRAFFIC_Y;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setOrderId(LocalConfig.ENV_NAME+"_"+CommonHelpers.generateOrderId())
                .build();
        String newOrderId=initTxnDTO.getBody().getOrderId();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO,"NET_BANKING", "false");
        String mid = merchant.getId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),newOrderId,"PTM" + newOrderId)
                .setPaymentMode("NET_BANKING")
                .setChannelCode(Constants.Bank.ICICI.toString())
                .setAuthMode("USRPWD")
                .setMpin("1234")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        NativeHelpers.submitJsonFormInBrowser(ptcResponse.toString());
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(mid)
                .validateOrderId(initTxnDTO.orderFromBody())
                .validatePaymentMode("NB")
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_SUCCESS.getRespMsg())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody())).validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Bank.ICICI.toString())
                .validateBankName(Constants.Bank.ICICINB.toString())
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), newOrderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_SUCCESS.getRespMsg())
                .validatePaymentMode("NB")
                .validateGatewayName(Constants.Gateway.ICICI.toString())
                .validateMid(mid)
                .AssertAll();
    }
    @Owner(PRAMOD_KUMAR)
    @Feature("PG2-6842")
    @Test(description = "Validate full traffic COTP Pending NB transaction for PG2 MID routed through PG2")
    public void validateFullTrafficCOTPPendingNBTxn() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y;
        String mid = merchant.getId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setOrderId(LocalConfig.ENV_NAME+"_"+CommonHelpers.generateOrderId())
                .setTxnValue("99.51")
                .build();
        String newOrderId=initTxnDTO.getBody().getOrderId();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO,"NET_BANKING", "false");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),newOrderId,"PTM" + newOrderId)
                .setPaymentMode("NET_BANKING")
                .setChannelCode(Constants.Bank.ICICI.toString())
                .setAuthMode("USRPWD")
                .setMpin("1234")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        NativeHelpers.submitJsonFormInBrowser(ptcResponse.toString());
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateCurrency("INR")
                .validateMid(mid)
                .validateOrderId(initTxnDTO.orderFromBody())
                .validatePaymentMode("NB")
                .validateRespCode(Constants.ResponseCode.TXN_STATUS_PENDING.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_STATUS_PENDING.getRespMsg())
                .validateStatus(Constants.TXNSTATUS.PENDING.toString())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Bank.ICICI.toString())
                .validateBankName(Constants.Bank.ICICINB.toString())
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), newOrderId);
        txnStatus.executeUntilPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(newOrderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.PENDING.toString())
                .validateRespCode(Constants.ResponseCode.MERCHANT_STATUS_TXN_PENDING.getRespCode())
                .validateRespMsg(Constants.ResponseCode.MERCHANT_STATUS_TXN_PENDING.getRespMsg())
                .validatePaymentMode("NB")
                .validateGatewayName(Constants.Gateway.ICICI.toString())
                .validateMid(mid)
                .AssertAll();
    }
    @Owner(PRAMOD_KUMAR)
    @Feature("PG2-6842")
    @Test(description = "Validate Full traffic Success COTP NB transaction for PG2 MID routed through PG2")
    public void validateFullTrafficCOTPSuccessNBTxn() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y;
        String mid = merchant.getId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setOrderId(LocalConfig.ENV_NAME+"_"+CommonHelpers.generateOrderId())
                .build();
        String newOrderId=initTxnDTO.getBody().getOrderId();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO,"NET_BANKING", "false");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),newOrderId,"PTM" + newOrderId)
                .setPaymentMode("NET_BANKING")
                .setChannelCode(Constants.Bank.ICICI.toString())
                .setAuthMode("USRPWD")
                .setMpin("1234")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        NativeHelpers.submitJsonFormInBrowser(ptcResponse.toString());
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(mid)
                .validateOrderId(initTxnDTO.orderFromBody())
                .validatePaymentMode("NB")
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_SUCCESS.getRespMsg())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody())).validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Bank.ICICI.toString())
                .validateBankName(Constants.Bank.ICICINB.toString())
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), newOrderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_SUCCESS.getRespMsg())
                .validatePaymentMode("NB")
                .validateGatewayName(Constants.Gateway.ICICI.toString())
                .validateMid(mid)
                .AssertAll();
    }
    @Owner(PRAMOD_KUMAR)
    @Feature("PG2-6842")
    @Parameters({"theme"})
    @Test(description = "Validate Full Traffic Co Then Pay Success NB Checkout JS Page Txn")
    public void validatePG2COThenPayFullTrafficYPPBLSuccessCheckoutJSPageTxn(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setOrderId(LocalConfig.ENV_NAME+"_"+CommonHelpers.generateOrderId())
                .build();
        String newOrderId=initTxnDTO.getBody().getOrderId();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        MerchantConfig config = checkoutJsPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutJsPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.NB);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(newOrderId)
                .validatePaymentMode("NB")
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_SUCCESS.getRespMsg())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.ICICI.toString())
                .validateBankName(Constants.Bank.ICICINB.toString())
                .assertAll();
        String txnid = responsePage.textTxnID().getText();
        Assertions.assertThat(txnid).contains("0000");
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), newOrderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(newOrderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_SUCCESS.getRespMsg())
                .validatePaymentMode("NB")
                .validateGatewayName(Constants.Gateway.ICICI.toString())
                .validateMid(initTxnDTO.getBody().getMid())
                .AssertAll();
    }
    @Owner(PRAMOD_KUMAR)
    @Feature("PG2-6842")
    @Test(description = "Validate Full traffic Success COTP NB transaction for PG2 MID routed through P+ where PG2 enabled pref")
    public void validateSuccessNBTxnPG2ENABLEDPref() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.PG2_ENABLED_CO_THEN_PAY;
        String mid = merchant.getId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setOrderId(LocalConfig.ENV_NAME+"_"+CommonHelpers.generateOrderId())
                .build();
        String newOrderId=initTxnDTO.getBody().getOrderId();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO,"NET_BANKING", "false");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),newOrderId,"PTM" + newOrderId)
                .setPaymentMode("NET_BANKING")
                .setChannelCode(Constants.Bank.ICICI.toString())
                .setAuthMode("USRPWD")
                .setMpin("1234")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        NativeHelpers.submitJsonFormInBrowser(ptcResponse.toString());
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(mid)
                .validateOrderId(initTxnDTO.orderFromBody())
                .validatePaymentMode("NB")
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_SUCCESS.getRespMsg())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody())).validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Bank.ICICI.toString())
                .validateBankName(Constants.Bank.ICICINB.toString())
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), newOrderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_SUCCESS.getRespMsg())
                .validatePaymentMode("NB")
                .validateGatewayName(Constants.Gateway.ICICI.toString())
                .validateMid(mid)
                .AssertAll();
    }
    @Owner(PRAMOD_KUMAR)
    @Feature("PG2-6842")
    @Test(description = "Validate Success HDFC NB transaction for PG2 MID routed through PG2")
    public void validateSuccessHDFCNBTxnPG2MID() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.UPIPUSHPG2;
        String mid = merchant.getId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setOrderId(LocalConfig.ENV_NAME+"_"+CommonHelpers.generateOrderId())
                .build();
        String newOrderId=initTxnDTO.getBody().getOrderId();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO,"NET_BANKING", "false");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),newOrderId,"PTM" + newOrderId)
                .setPaymentMode("NET_BANKING")
                .setChannelCode(Constants.Bank.HDFC.toString())
                .setAuthMode("USRPWD")
                .setMpin("1234")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        NativeHelpers.submitJsonFormInBrowser(ptcResponse.toString());
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(mid)
                .validateOrderId(initTxnDTO.orderFromBody())
                .validatePaymentMode("NB")
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_SUCCESS.getRespMsg())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody())).validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Bank.HDFC.toString())
                .validateBankName(Constants.Bank.HDFCBANK.toString())
                .assertAll();
        CommonHelpers.verifyPaymentNotify(newOrderId);
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_SUCCESS.getRespMsg())
                .validatePaymentMode("NB")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateMid(mid)
                .AssertAll();
    }
    @Owner(HIMANSHU)
    @Test(description = "Validate PG2 txn through enhanced flow")
    public void nbEnhancedFlow(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        OrderDTO orderDTO = new OrderFactory.AddnPay(Constants.MerchantType.UPIPUSHPG2, theme, user).build();
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        //cashierPage.login(user);
        cashierPage.payBy(Constants.PayMode.NB);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("PPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
    }
    @Owner(Amanpreet)
    @Feature("PG2-46752")
    @Test(description = "Validate INACTIVE_TIMEOUT object in CO request for direct settlement MID with flag Off")
    public void validateTimeoutObjectWithFlagOffCORequest(@Optional("enhancedweb_revamp") String theme) throws Exception {
        //FF4JFlags.disable("theia.disableSendingInactiveTimeoutForDSMerchant");
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.AddnPay(Constants.MerchantType.INSTANT_SETTLEMENT_MID1, theme, user).build();
        new CheckoutPage().createOrder(orderDTO);
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderDTO.getORDER_ID(),"ACQUIRING_CREATE_ORDER_AND_PAY");
        Assertions.assertThat(logs).contains("\"timeoutType\":\"INACTIVE_TIMEOUT\"");
    }
    @Owner(Amanpreet)
    @Feature("PG2-46752")
    @Test(description = "Validate INACTIVE_TIMEOUT object in CO request for direct settlement MID with flag ON")
    public void validateTimeoutObjectWithFlagOnCORequest(@Optional("enhancedweb_revamp") String theme) throws Exception {
        //FF4JFlags.enable("theia.disableSendingInactiveTimeoutForDSMerchant");
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.AddnPay(Constants.MerchantType.INSTANT_SETTLEMENT_MID2, theme, user).build();
        new CheckoutPage().createOrder(orderDTO);
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderDTO.getORDER_ID(),"ACQUIRING_CREATE_ORDER");
        Assertions.assertThat(logs).doesNotContain("\"timeoutType\":\"INACTIVE_TIMEOUT\"");
    }
    @Owner(Amanpreet)
    @Feature("PG2-46752")
    @Test(description = "Validate INACTIVE_TIMEOUT object in COP request for direct settlement MID with flag Off")
    public void validateTimeoutObjectWithFlagOffCOPRequest() throws Exception {
        //FF4JFlags.disable("theia.disableSendingInactiveTimeoutForDSMerchant");
        User user = userManager.getForRead(Label.BASIC);
        String mid = Constants.MerchantType.INSTANT_SETTLEMENT_MID3.getId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.INSTANT_SETTLEMENT_MID3)
                .setOrderId(LocalConfig.ENV_NAME + "_" + CommonHelpers.generateOrderId())
                .build();
        String newOrderId = initTxnDTO.getBody().getOrderId();
        initTxnDTO.getBody().setOrderId(newOrderId);
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO,"NET_BANKING", "false");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),newOrderId,"PTM" + newOrderId)
                .setPaymentMode("NET_BANKING")
                .setChannelCode(Constants.Bank.ICICI.toString())
                .setAuthMode("USRPWD")
                .setMpin("1234")
                .setToken(txnToken)
                .build();
        NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.getBody().getOrderId(),"ACQUIRING_CREATE_ORDER_AND_PAY");
        Assertions.assertThat(logs).contains("\"timeoutType\":\"INACTIVE_TIMEOUT\"");
    }
    @Owner(Amanpreet)
    @Feature("PG2-46752")
    @Test(description = "Validate INACTIVE_TIMEOUT object in COP request for direct settlement MID with flag ON")
    public void validateTimeoutObjectWithFlagOnCOPRequest() throws Exception {
        //FF4JFlags.enable("theia.disableSendingInactiveTimeoutForDSMerchant");
        User user = userManager.getForRead(Label.BASIC);
        String mid = Constants.MerchantType.INSTANT_SETTLEMENT_MID4.getId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.INSTANT_SETTLEMENT_MID4)
                .setOrderId(LocalConfig.ENV_NAME + "_" + CommonHelpers.generateOrderId())
                .build();
        String newOrderId = initTxnDTO.getBody().getOrderId();
        initTxnDTO.getBody().setOrderId(newOrderId);
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO,"NET_BANKING", "false");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),newOrderId,"PTM" + newOrderId)
                .setPaymentMode("NET_BANKING")
                .setChannelCode(Constants.Bank.ICICI.toString())
                .setAuthMode("USRPWD")
                .setMpin("1234")
                .setToken(txnToken)
                .build();
        NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.getBody().getOrderId(),"ACQUIRING_CREATE_ORDER_AND_PAY");
        Assertions.assertThat(logs).doesNotContain("\"timeoutType\":\"INACTIVE_TIMEOUT\"");
    }
}