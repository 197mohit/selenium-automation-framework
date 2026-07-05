package scripts.api.PPBL;

import com.paytm.ServerConfigProvider;
import com.paytm.api.*;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.api.nativeAPI.FetchPaymentOptionV2;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.api.refund.SyncRefund;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.*;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.CloseOrder.CloseOrderDTO;
import com.paytm.dto.NativeDTO.InitTxn.SimplifiedPaymentOffers;
import com.paytm.dto.NativeDTO.InitTxn.TxnAmount;
import com.paytm.dto.NativeDTO.UpdateTransaction.UpdateTransactionDTO;
import com.paytm.dto.NativeDTO.InitTxn.UltimateBeneficiaryDetails;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.response.FetchPaymentOptResponseDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.TransactionStatusV1.Body;
import com.paytm.dto.TransactionStatusV1.Head;
import com.paytm.dto.checkoutjs.MerchantConfig;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.dto.processTransactionV1.response.ProcessTxnV1Response;
import com.paytm.framework.reporting.Reporter;
import com.paytm.framework.utils.RedisUtil;
import com.paytm.pages.*;
import com.paytm.utils.merchant.merchant.util.Merchant;
import com.paytm.utils.merchant.merchant.util.Promo;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;
import com.paytm.dto.TransactionStatusV1.TransactionStatusV1DTO;
import com.paytm.LocalConfig;
import redis.clients.jedis.Jedis;

import java.util.Arrays;
import java.util.Date;

import static com.paytm.appconstants.Constants.Owner.*;
import static io.restassured.RestAssured.given;

public class PPBLPG2  extends PGPBaseTest {
    private static final String API_VERSION_V2 = "v2";
    protected final ThreadLocal<FetchPaymentOptResponseDTO> fetchPaymentOptResponseDTO = new ThreadLocal<>();
    private final CheckoutPage checkoutPage = new CheckoutPage();
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
    public void validateAcquiringOrderModify(String orderId) throws InterruptedException {
        String grepAcquiringOrderModify = "grep " + orderId + " " + LocalConfig.THEIA_FACADE_LOGS +
                " | grep \"ACQUIRING_ORDER_MODIFY\" | grep \"RESPONSE\"";
        String acquringOrderModifyLogs = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepAcquiringOrderModify);
        Assertions.assertThat(acquringOrderModifyLogs).
                contains("\"resultCodeId\":\"00000000\"")
                .contains("\"resultStatus\":\"S\"")
                .contains("\"resultCode\":\"SUCCESS\"");
    }

    public void validateAcquiringOrderMerchantTransIDLogs(String orderId, String riskInfo) throws InterruptedException {
        String grepAcquiringOrderQueryMerchant = "grep " + orderId + " " +  LocalConfig.MERCHANT_FACADE_LOGS +
                " | grep \"ACQUIRING_INQUIRE_WITH_MERCHANT_TRANS_ID\" | grep \"RESPONSE\"";
        String grepAcquiringOrderQueryMerchantTransLogs = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.MERCHANT_STATUS, grepAcquiringOrderQueryMerchant);
        Assertions.assertThat(grepAcquiringOrderQueryMerchantTransLogs)
                .contains(riskInfo)
                .containsIgnoringCase("orderModifyExtendInfo");
    }

    public void verifyOrderModifyParam(String orderId, String riskInfo) throws InterruptedException {
        String grepOrderModifyParam = "grep " + orderId + " " + LocalConfig.THEIA_FACADE_LOGS +
                " | grep \"ACQUIRING_INQUIRE_WITH_ACQ_ID\" | grep \"RESPONSE\"";
        String extOrderModifyParam = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepOrderModifyParam);
        Assertions.assertThat(extOrderModifyParam).containsIgnoringCase("orderModifyExtendInfo")
                .contains(riskInfo);

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
    @Owner(ABHISHEK_TEWARI)
    @Feature("PGP-38280")
    @Test(description = "Validate Success PPBL transaction for PG2 MID routed through PG2")
    public void validateSuccessPPBLTxnPG2MID() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.PPBL_NB;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, Constants.Bank.PPBL.toString(), "false");
        String mid = merchant.getId();
        String orderId = initTxnDTO.orderFromBody();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode(Constants.Bank.PPBL.toString())
                .setAuthMode("USRPWD")
                .setMpin("1234")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
//      assertPTCCommonResponse(ptcResponse);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        CommonHelpers.verifyEsnLength(initTxnDTO.getBody().getOrderId(),19);

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
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Bank.PPBL.toString())
                .validateBankName(Constants.Bank.PPBL.toString())
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
                .validateGatewayName(Constants.Gateway.PPBL.toString())
                .validateMid(mid)
                .AssertAll();
    // Deprecated Logger moved to kafka Payload
    //    CommonHelpers.verifyPaymentNotify(orderId);
    }

    @Owner(ABHISHEK_TEWARI)
    @Feature("PGP-38280")
    @Test(description = "Validate Pending PPBL transaction for PG2 MID routed through PG2, close order V1 and then check merchant status")
    public void validatePendingPPBLTxnPG2MIDCloseOrderV1ThenCheckMerchantStatus() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.UPIPUSHPG2;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue("60.2").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, Constants.Bank.PPBL.toString(), "false");
        String mid = merchant.getId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode(Constants.Bank.PPBL.toString())
                .setAuthMode("USRPWD")
                .setMpin("1234")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        CommonHelpers.verifyEsnLength(initTxnDTO.getBody().getOrderId(),19);

        NativeHelpers.submitJsonFormInBrowser(ptcResponse.toString());
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(mid)
                .validateOrderId(initTxnDTO.orderFromBody())
                .validatePaymentMode("NB")
                .validateRespCode(Constants.ResponseCode.TXN_STATUS_PENDING.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_STATUS_PENDING.getRespMsg())
                .validateStatus(Constants.TXNSTATUS.PENDING.toString())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Bank.PPBL.toString())
                .validateBankName(Constants.Bank.PPBL.toString())
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.PENDING.toString())
                .validateRespCode(Constants.ResponseCode.TXN_STATUS_PENDING.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_STATUS_PENDING.getRespMsg())
                .validatePaymentMode("NB")
                .validateGatewayName(Constants.Gateway.PPBL.toString())
                .validateMid(mid)
                .AssertAll();

        CloseOrderDTO closeOrderDTO = new CloseOrderDTO();
        closeOrderDTO.setHead(new com.paytm.dto.CloseOrder.Head().setChannelId("WAP").setVersion("v1").setSigature(""))
                .setBody(new com.paytm.dto.CloseOrder.Body().setIsForceClose(true).setOrderId(initTxnDTO.getBody().getOrderId()).setMid(mid));
        CloseOrderAPI closeOrderAPI = new CloseOrderAPI(closeOrderDTO);
        Response closeOrderResponse = closeOrderAPI.execute();
        Assertions.assertThat(closeOrderResponse.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("SUCCESS");
        Assertions.assertThat(closeOrderResponse.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(closeOrderResponse.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo("01");

        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_FAILURE.toString())
                .validateRespCode(Constants.ResponseCode.PAYMENT_FAILED.getRespCode())
                .validateRespMsg(Constants.ResponseCode.PAYMENT_FAILED.getRespMsg())
                .validatePaymentMode("NB")
                .validateMid(mid)
                .AssertAll();
    }

    @Owner(ABHISHEK_TEWARI)
    @Feature("PGP-38280")
    @Test(description = "Validate Pending PPBL transaction for PG2 MID routed through PG2, close order V2 and then check merchant status")
    public void validatePendingPPBLTxnPG2MIDCloseOrderV2ThenCheckMerchantStatus() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.UPIPUSHPG2;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue("60.2").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, Constants.Bank.PPBL.toString(), "false");
        String mid = merchant.getId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode(Constants.Bank.PPBL.toString())
                .setAuthMode("USRPWD")
                .setMpin("1234")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        CommonHelpers.verifyEsnLength(initTxnDTO.getBody().getOrderId(),19);

        NativeHelpers.submitJsonFormInBrowser(ptcResponse.toString());
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(mid)
                .validateOrderId(initTxnDTO.orderFromBody())
                .validatePaymentMode("NB")
                .validateRespCode(Constants.ResponseCode.TXN_STATUS_PENDING.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_STATUS_PENDING.getRespMsg())
                .validateStatus(Constants.TXNSTATUS.PENDING.toString())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Bank.PPBL.toString())
                .validateBankName(Constants.Bank.PPBL.toString())
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.PENDING.toString())
                .validateRespCode(Constants.ResponseCode.TXN_STATUS_PENDING.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_STATUS_PENDING.getRespMsg())
                .validatePaymentMode("NB")
                .validateGatewayName(Constants.Gateway.PPBL.toString())
                .validateMid(mid)
                .AssertAll();

        String txnId = txnStatus.txnStatusResponse.TXNID;
        CloseOrderV2Api closeOrderV2Api = new CloseOrderV2Api("TXN_TOKEN", mid, initTxnDTO.getBody().getOrderId(), initTxnResponse.getBody().getTxnToken(), "true");
        Response closeOrderResponse = closeOrderV2Api.execute();
        Assertions.assertThat(closeOrderResponse.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("SUCCESS");
        Assertions.assertThat(closeOrderResponse.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(closeOrderResponse.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo(Constants.ResponseCode.TXN_SUCCESS.getRespCode());
        Assertions.assertThat(closeOrderResponse.jsonPath().getString("body.txnInfo.CURRENCY")).isEqualTo("INR");
        Assertions.assertThat(closeOrderResponse.jsonPath().getString("body.txnInfo.MID")).isEqualTo(mid);
        Assertions.assertThat(closeOrderResponse.jsonPath().getString("body.txnInfo.ORDERID")).isEqualTo(initTxnDTO.getBody().getOrderId());
        Assertions.assertThat(closeOrderResponse.jsonPath().getString("body.txnInfo.RESPCODE")).isEqualTo(Constants.ResponseCode.CLOSEORDERV2_TXN_PENDING.getRespCode());
        Assertions.assertThat(closeOrderResponse.jsonPath().getString("body.txnInfo.RESPMSG")).isEqualTo(Constants.ResponseCode.CLOSEORDERV2_TXN_PENDING.getRespMsg());
        Assertions.assertThat(closeOrderResponse.jsonPath().getString("body.txnInfo.STATUS")).isEqualTo(Constants.TXNSTATUS.TXN_FAILURE.toString());
        Assertions.assertThat(closeOrderResponse.jsonPath().getString("body.txnInfo.TXNAMOUNT")).isEqualTo(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.getBody().getTxnAmount().getValue()));
        Assertions.assertThat(closeOrderResponse.jsonPath().getString("body.txnInfo.TXNID")).isEqualTo(txnId);

        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_FAILURE.toString())
                .validateRespCode(Constants.ResponseCode.PAYMENT_FAILED.getRespCode())
                .validateRespMsg(Constants.ResponseCode.PAYMENT_FAILED.getRespMsg())
                .validatePaymentMode("NB")
                .validateMid(mid)
                .AssertAll();
    }

    @Owner(ABHISHEK_TEWARI)
    @Feature("PGP-38280")
    @Test(description = "Validate that if user id is not present in FF4J Flag theia.payment.adapter.feature then txn should be routed to P+")
    public void userNotPrsentInFF4JPPBLTxnRoutedToPPlus() throws Exception{
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        Constants.MerchantType merchant = Constants.MerchantType.UPIPUSHPG2;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, Constants.Bank.PPBL.toString(), "false");
        String mid = merchant.getId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode(Constants.Bank.PPBL.toString())
                .setAuthMode("USRPWD")
                .setMpin("1234")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        CommonHelpers.verifyEsnLength(initTxnDTO.getBody().getOrderId(),19);

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
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Bank.PPBL.toString())
                .validateBankName(Constants.Bank.PPBL.toString())
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
                .validateGatewayName(Constants.Gateway.PPBL.toString())
                .validateMid(mid)
                .AssertAll();
    }

    @Owner(ABHISHEK_TEWARI)
    @Feature("PGP-38280")
    @Test(description = "Validate that if mid is not present in FF4J Flag theia.payment.adapter.feature but pref is enabled on mid then txn should be routed to PG2, txn is pending then close order")
    public void midNotPrsentInFF4JButPrefEnabledPPBLRoutedToPG2PendingTxn() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.UPIPUSHPG2FF4JNOPREFYES;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue("60.2").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, Constants.Bank.PPBL.toString(), "false");
        String mid = merchant.getId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode(Constants.Bank.PPBL.toString())
                .setAuthMode("USRPWD")
                .setMpin("1234")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        CommonHelpers.verifyEsnLength(initTxnDTO.getBody().getOrderId(),19);

        NativeHelpers.submitJsonFormInBrowser(ptcResponse.toString());
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(mid)
                .validateOrderId(initTxnDTO.orderFromBody())
                .validatePaymentMode("NB")
                .validateRespCode(Constants.ResponseCode.TXN_STATUS_PENDING.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_STATUS_PENDING.getRespMsg())
                .validateStatus(Constants.TXNSTATUS.PENDING.toString())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Bank.PPBL.toString())
                .validateBankName(Constants.Bank.PPBL.toString())
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.PENDING.toString())
                .validateRespCode(Constants.ResponseCode.TXN_STATUS_PENDING.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_STATUS_PENDING.getRespMsg())
                .validatePaymentMode("NB")
                .validateGatewayName(Constants.Gateway.PPBL.toString())
                .validateMid(mid)
                .AssertAll();

        CloseOrderDTO closeOrderDTO = new CloseOrderDTO();
        closeOrderDTO.setHead(new com.paytm.dto.CloseOrder.Head().setChannelId("WAP").setVersion("v1").setSigature(""))
                .setBody(new com.paytm.dto.CloseOrder.Body().setIsForceClose(true).setOrderId(initTxnDTO.getBody().getOrderId()).setMid(mid));
        CloseOrderAPI closeOrderAPI = new CloseOrderAPI(closeOrderDTO);
        Response closeOrderResponse = closeOrderAPI.execute();
        Assertions.assertThat(closeOrderResponse.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("SUCCESS");
        Assertions.assertThat(closeOrderResponse.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(closeOrderResponse.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo("01");

        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_FAILURE.toString())
                .validateRespCode(Constants.ResponseCode.PAYMENT_FAILED.getRespCode())
                .validateRespMsg(Constants.ResponseCode.PAYMENT_FAILED.getRespMsg())
                .validatePaymentMode("NB")
                .validateMid(mid)
                .AssertAll();
    }

    @Owner(ABHISHEK_TEWARI)
    @Feature("PGP-38280")
    @Test(description = "Validate that if user id is present but mid is not present in FF4J Flag theia.payment.adapter.feature then txn should be routed to P+")
    public void userPrsentMidNotPresentInFF4JPPBLTxnRoutedToPPlus() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.UPIPG2FF4JDISABLED;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, Constants.Bank.PPBL.toString(), "false");
        String mid = merchant.getId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode(Constants.Bank.PPBL.toString())
                .setAuthMode("USRPWD")
                .setMpin("1234")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        CommonHelpers.verifyEsnLength(initTxnDTO.getBody().getOrderId(),20);

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
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Bank.PPBL.toString())
                .validateBankName(Constants.Bank.PPBL.toString())
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
                .validateGatewayName(Constants.Gateway.PPBL.toString())
                .validateMid(mid)
                .AssertAll();
    }
    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-38366")
    @Test(description = "Validate Failed PPBL transaction for PG2 MID routed through PG2")
    public void validateFailedPPBLTxnPG2MID() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.PPBL_NB;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue("60.1").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, Constants.Bank.PPBL.toString(), "false");
        String mid = merchant.getId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode(Constants.Bank.PPBL.toString())
                .setAuthMode("USRPWD")
                .setMpin("1234")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
//      assertPTCCommonResponse(ptcResponse);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("F");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0001");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Payment failed due to a technical error. Please try after some time.");
        CommonHelpers.verifyEsnLength(initTxnDTO.getBody().getOrderId(),19);

        NativeHelpers.submitJsonFormInBrowser(ptcResponse.toString());
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(mid)
                .validateOrderId(initTxnDTO.orderFromBody())
                .validatePaymentMode("NB")
                .validateRespCode("810")
                .validateRespMsg("Payment failed due to a technical error. Please try after some time.")
                .validateStatus(Constants.TXNSTATUS.TXN_FAILURE.toString())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Bank.PPBL.toString())
                .validateBankName(Constants.Bank.PPBL.toString())
                .assertAll();

        // adding this code to manually close the order when order is not getting closed for some txn.
        CloseOrderDTO closeOrderDTO = new CloseOrderDTO();
        closeOrderDTO.setHead(new com.paytm.dto.CloseOrder.Head().setChannelId("WAP").setVersion("v1").setSigature(""))
                .setBody(new com.paytm.dto.CloseOrder.Body().setIsForceClose(true).setOrderId(initTxnDTO.getBody().getOrderId()).setMid(merchant.getId()));
        CloseOrderAPI closeOrderAPI = new CloseOrderAPI(closeOrderDTO);
        closeOrderAPI.execute();
//In Txn Failure cases sometime BankTXNId is coming as blank so we are removing the BankTXNID validation
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_FAILURE.toString())
                .validateRespCode("810")
                .validateRespMsg("Payment failed due to a technical error. Please try after some time.")
                .validatePaymentMode("NB")
                .validateGatewayName(Constants.Gateway.PPBL.toString())
                .validateMid(mid)
                .AssertAll();
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-38675")
    @Test(description = "Validate PPBL full Async Refund transaction for PG2 MID routed through PG2")
    public void PPBLPG2FullAsyncRefund() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.UPIPUSHPG2;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, Constants.Bank.PPBL.toString(), "false");
        String mid = merchant.getId();
        String orderID= initTxnDTO.orderFromBody();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode(Constants.Bank.PPBL.toString())
                .setAuthMode("USRPWD")
                .setMpin("1234")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        CommonHelpers.verifyEsnLength(initTxnDTO.getBody().getOrderId(),19);

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
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Bank.PPBL.toString())
                .validateBankName(Constants.Bank.PPBL.toString())
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
                .validateGatewayName(Constants.Gateway.PPBL.toString())
                .validateMid(mid)
                .AssertAll();

        SyncRefund syncRefund = new SyncRefund();
        Response asyncRefundResp = given().spec(syncRefund.reqSpecAsyncRefund(merchant, initTxnDTO.txnAmountFromBody(), initTxnDTO.getBody().getOrderId(),
                        txnStatus.getResponse().getTXNID()))
                .post().then().extract().response();
        System.out.println("Async Refund Response " + asyncRefundResp.body().prettyPrint());
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.orderId")).isEqualTo(initTxnDTO.getBody().getOrderId());
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.mid")).isEqualTo(mid);
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.refId")).isNotNull();
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo(Constants.TXNSTATUS.PENDING.toString());
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo(Constants.ResponseCode.REFUND_PENDING.getRespCode());
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo(Constants.ResponseCode.REFUND_PENDING.getRespMsg());
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.refundId")).isNotNull();
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.txnId")).isEqualTo(txnStatus.getResponse().getTXNID());
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.refundAmount")).isEqualTo(initTxnDTO.txnAmountFromBody());
        PGPHelpers.assertRefundSuccessNotifyPresence(orderID);
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-38675")
    @Test(description = "Validate PPBL partial Async Refund transaction for PG2 MID routed through PG2")
    public void PPBLPG2PartialAsyncRefund() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.UPIPUSHPG2;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue("2").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, Constants.Bank.PPBL.toString(), "false");
        String mid = merchant.getId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode(Constants.Bank.PPBL.toString())
                .setAuthMode("USRPWD")
                .setMpin("1234")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        CommonHelpers.verifyEsnLength(initTxnDTO.getBody().getOrderId(),19);

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
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Bank.PPBL.toString())
                .validateBankName(Constants.Bank.PPBL.toString())
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
                .validateGatewayName(Constants.Gateway.PPBL.toString())
                .validateMid(mid)
                .AssertAll();

        SyncRefund syncRefund = new SyncRefund();
        String refundAmount = "1";
        Response asyncRefundResp = given().spec(syncRefund.reqSpecAsyncRefund(merchant, refundAmount, initTxnDTO.getBody().getOrderId(),
                        txnStatus.getResponse().getTXNID()))
                .post().then().extract().response();
        System.out.println("Async Refund Response " + asyncRefundResp.body().prettyPrint());
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.orderId")).isEqualTo(initTxnDTO.getBody().getOrderId());
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.mid")).isEqualTo(mid);
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.refId")).isNotNull();
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo(Constants.TXNSTATUS.PENDING.toString());
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo(Constants.ResponseCode.REFUND_PENDING.getRespCode());
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo(Constants.ResponseCode.REFUND_PENDING.getRespMsg());
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.refundId")).isNotNull();
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.txnId")).isEqualTo(txnStatus.getResponse().getTXNID());
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.refundAmount")).isEqualTo(refundAmount);
    }

    @Owner(Constants.Owner.VIKASH_VERMA)
    @Feature("PGP-38720")
    @Test(description = "Validate that if mid is not present in FF4J flag refund.pg2.enabled then refund should fail")
    public void PPBLmidNotPresentInRefundFF4J() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.PG2_FF4J_THEIAENABLE_YES_REFUND_NO;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, Constants.Bank.PPBL.toString(), "false");
        String mid = merchant.getId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode(Constants.Bank.PPBL.toString())
                .setAuthMode("USRPWD")
                .setMpin("1234")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        CommonHelpers.verifyEsnLength(initTxnDTO.getBody().getOrderId(),19);

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
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Bank.PPBL.toString())
                .validateBankName(Constants.Bank.PPBL.toString())
                .assertAll();

        //  merchantStatus.pg2.enabled flags is off for given mid with theia.payment.adapter.feature flag on. txn is done on PG2 but merchant status will route to P+ because of flag off. so it will show , invalid orderId
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateStatus(Constants.TXNSTATUS.TXN_FAILURE.toString())
                .validateRespCode(Constants.ResponseCode.INVALID_ORDERID.getRespCode())
                .validateRespMsg(Constants.ResponseCode.INVALID_ORDERID.getRespMsg())
                .validateMid(mid)
                .AssertAll();

        // refund.pg2.enabled flags is off for given mid with theia.payment.adapter.feature flag on. txn is done on PG2 but refund will route to P+ because of flag off. so it will show , invalid refund request
        SyncRefund syncRefund = new SyncRefund();
        Response asyncRefundResp = given().spec(syncRefund.reqSpecAsyncRefund(merchant, initTxnDTO.txnAmountFromBody(), initTxnDTO.getBody().getOrderId(),
                        txnStatus.getResponse().getTXNID()))
                .post().then().extract().response();
        System.out.println("Async Refund Response " + asyncRefundResp.body().prettyPrint());
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.orderId")).isEqualTo(initTxnDTO.getBody().getOrderId());
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.mid")).isEqualTo(mid);
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo(Constants.TXNSTATUS.TXN_FAILURE.toString());
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo(Constants.ResponseCode.INVALID_REFUND_REQUEST.getRespCode());
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo(Constants.ResponseCode.INVALID_REFUND_REQUEST.getRespMsg());
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.refundAmount")).isEqualTo(initTxnDTO.txnAmountFromBody());
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-38778")
    @Test(description = "Validate Success Co Then Pay PPBL transaction for PG2 MID routed through PG2")
    public void validateFullTrafficYSuccessCoThenPayPPBLTxnPG2MID() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, Constants.Bank.PPBL.toString(), "false");
        String mid = merchant.getId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode(Constants.Bank.PPBL.toString())
                .setAuthMode("USRPWD")
                .setMpin("1234")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        CommonHelpers.verifyEsnLength(initTxnDTO.getBody().getOrderId(),19);

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
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Bank.PPBL.toString())
                .validateBankName(Constants.Bank.PPBL.toString())
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
                .validateGatewayName(Constants.Gateway.PPBL.toString())
                .validateMid(mid)
                .AssertAll();
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-38778")
    @Test(description = "Validate Fail Co Then Pay PPBL transaction for PG2 MID routed through PG2")
    public void validateFullTrafficYFailCoThenPayPPBLTxnPG2MID() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue("60.1").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, Constants.Bank.PPBL.toString(), "false");
        String mid = merchant.getId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode(Constants.Bank.PPBL.toString())
                .setAuthMode("USRPWD")
                .setMpin("1234")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        CommonHelpers.verifyEsnLength(initTxnDTO.getBody().getOrderId(),19);

        NativeHelpers.submitJsonFormInBrowser(ptcResponse.toString());
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(mid)
                .validateOrderId(initTxnDTO.orderFromBody())
                .validatePaymentMode("NB")
                .validateRespCode(Constants.ResponseCode.BANK_TXN_FAILURE.getRespCode())
                .validateRespMsg(Constants.ResponseCode.BANK_TXN_FAILURE.getRespMsg())
                .validateStatus(Constants.TXNSTATUS.TXN_FAILURE.toString())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Bank.PPBL.toString())
                .validateBankName(Constants.Bank.PPBL.toString())
                .assertAll();

        // adding this code to manually close the order when order is not getting closed for some txn.
        CloseOrderDTO closeOrderDTO = new CloseOrderDTO();
        closeOrderDTO.setHead(new com.paytm.dto.CloseOrder.Head().setChannelId("WAP").setVersion("v1").setSigature(""))
                .setBody(new com.paytm.dto.CloseOrder.Body().setIsForceClose(true).setOrderId(initTxnDTO.getBody().getOrderId()).setMid(merchant.getId()));
        CloseOrderAPI closeOrderAPI = new CloseOrderAPI(closeOrderDTO);
        closeOrderAPI.execute();

        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_FAILURE.toString())
                .validateRespCode(Constants.ResponseCode.BANK_TXN_FAILURE.getRespCode())
                .validateRespMsg(Constants.ResponseCode.BANK_TXN_FAILURE.getRespMsg())
                .validatePaymentMode("NB")
                .validateGatewayName(Constants.Gateway.PPBL.toString())
                .validateMid(mid)
                .AssertAll();
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-38778")
    @Test(description = "Validate Pending Co Then Pay PPBL transaction for PG2 MID routed through PG2")
    public void validateFullTrafficYPendingCoThenPayPPBLTxnPG2MID() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue("60.2").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, Constants.Bank.PPBL.toString(), "false");
        String mid = merchant.getId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode(Constants.Bank.PPBL.toString())
                .setAuthMode("USRPWD")
                .setMpin("1234")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        CommonHelpers.verifyEsnLength(initTxnDTO.getBody().getOrderId(),19);

        NativeHelpers.submitJsonFormInBrowser(ptcResponse.toString());
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(mid)
                .validateOrderId(initTxnDTO.orderFromBody())
                .validatePaymentMode("NB")
                .validateRespCode(Constants.ResponseCode.TXN_STATUS_PENDING.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_STATUS_PENDING.getRespMsg())
                .validateStatus(Constants.TXNSTATUS.PENDING.toString())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Bank.PPBL.toString())
                .validateBankName(Constants.Bank.PPBL.toString())
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.PENDING.toString())
                .validateRespCode(Constants.ResponseCode.TXN_STATUS_PENDING.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_STATUS_PENDING.getRespMsg())
                .validatePaymentMode("NB")
                .validateGatewayName(Constants.Gateway.PPBL.toString())
                .validateMid(mid)
                .AssertAll();

        CloseOrderDTO closeOrderDTO = new CloseOrderDTO();
        closeOrderDTO.setHead(new com.paytm.dto.CloseOrder.Head().setChannelId("WAP").setVersion("v1").setSigature(""))
                .setBody(new com.paytm.dto.CloseOrder.Body().setIsForceClose(true).setOrderId(initTxnDTO.getBody().getOrderId()).setMid(merchant.getId()));
        CloseOrderAPI closeOrderAPI = new CloseOrderAPI(closeOrderDTO);
        Response closeOrderResponse = closeOrderAPI.execute();
        Assertions.assertThat(closeOrderResponse.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("SUCCESS");
        Assertions.assertThat(closeOrderResponse.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(closeOrderResponse.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo("01");

        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_FAILURE.toString())
                .validateRespCode(Constants.ResponseCode.PAYMENT_FAILED.getRespCode())
                .validateRespMsg(Constants.ResponseCode.PAYMENT_FAILED.getRespMsg())
                .validatePaymentMode("NB")
                .validateMid(mid)
                .AssertAll();
    }

    @Owner(Constants.Owner.VIKASH_VERMA)
    @Feature("PG2-4085")
    @Test(description = "Validate Success COP PPBL transaction for PG2 MID routed through PG2")
    public void validateFullTrafficYSuccessCOPPPBLTxnPG2MID() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.PG2_COP_FULL_TRAFFIC_Y).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, Constants.Bank.PPBL.toString(), "false");
        String mid = Constants.MerchantType.PG2_COP_FULL_TRAFFIC_Y.getId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode(Constants.Bank.PPBL.toString())
                .setAuthMode("USRPWD")
                .setMpin("1234")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        CommonHelpers.verifyEsnLength(initTxnDTO.getBody().getOrderId(),19);

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
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Bank.PPBL.toString())
                .validateBankName(Constants.Bank.PPBL.toString())
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
                .validateGatewayName(Constants.Gateway.PPBL.toString())
                .validateMid(mid)
                .AssertAll();
    }

    @Owner(Constants.Owner.VIKASH_VERMA)
    @Feature("PG2-4085")
    @Test(description = "Validate Fail COP PPBL transaction for PG2 MID routed through PG2")
    public void validateFullTrafficYFailCOPPayPPBLTxnPG2MID() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.PG2_COP_FULL_TRAFFIC_Y).setTxnValue("60.1").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, Constants.Bank.PPBL.toString(), "false");
        String mid = Constants.MerchantType.PG2_COP_FULL_TRAFFIC_Y.getId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode(Constants.Bank.PPBL.toString())
                .setAuthMode("USRPWD")
                .setMpin("1234")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        String esn = ptcResponse.getBody().getBankForm().getRedirectForm().getContent().getExtSerialNo();
        Assertions.assertThat(esn.length()).isEqualTo(19);

        NativeHelpers.submitJsonFormInBrowser(ptcResponse.toString());
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(mid)
                .validateOrderId(initTxnDTO.orderFromBody())
                .validatePaymentMode("NB")
                .validateRespCode(Constants.ResponseCode.BANK_TXN_FAILURE.getRespCode())
                .validateRespMsg(Constants.ResponseCode.BANK_TXN_FAILURE.getRespMsg())
                .validateStatus(Constants.TXNSTATUS.TXN_FAILURE.toString())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Bank.PPBL.toString())
                .validateBankName(Constants.Bank.PPBL.toString())
                .assertAll();

        // adding this code to manually close the order when order is not getting closed for some txn.
        CloseOrderDTO closeOrderDTO = new CloseOrderDTO();
        closeOrderDTO.setHead(new com.paytm.dto.CloseOrder.Head().setChannelId("WEB").setVersion("v1").setSigature(""))
                .setBody(new com.paytm.dto.CloseOrder.Body().setIsForceClose(true).setOrderId(initTxnDTO.getBody().getOrderId()).setMid(mid));
        CloseOrderAPI closeOrderAPI = new CloseOrderAPI(closeOrderDTO);
        closeOrderAPI.execute();

        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_FAILURE.toString())
                .validateRespCode(Constants.ResponseCode.BANK_TXN_FAILURE.getRespCode())
                .validateRespMsg(Constants.ResponseCode.BANK_TXN_FAILURE.getRespMsg())
                .validatePaymentMode("NB")
                .validateGatewayName(Constants.Gateway.PPBL.toString())
                .validateMid(mid)
                .AssertAll();
    }

    @Owner(Constants.Owner.VIKASH_VERMA)
    @Feature("PG2-4085")
    @Test(description = "Validate Pending COP PPBL transaction for PG2 MID routed through PG2")
    public void validateFullTrafficYPendingCOPPPBLTxnPG2MID() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.PG2_COP_FULL_TRAFFIC_Y).setTxnValue("60.2").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, Constants.Bank.PPBL.toString(), "false");
        String mid = Constants.MerchantType.PG2_COP_FULL_TRAFFIC_Y.getId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode(Constants.Bank.PPBL.toString())
                .setAuthMode("USRPWD")
                .setMpin("1234")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        CommonHelpers.verifyEsnLength(initTxnDTO.getBody().getOrderId(),19);

        NativeHelpers.submitJsonFormInBrowser(ptcResponse.toString());
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(mid)
                .validateOrderId(initTxnDTO.orderFromBody())
                .validatePaymentMode("NB")
                .validateRespCode(Constants.ResponseCode.TXN_STATUS_PENDING.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_STATUS_PENDING.getRespMsg())
                .validateStatus(Constants.TXNSTATUS.PENDING.toString())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Bank.PPBL.toString())
                .validateBankName(Constants.Bank.PPBL.toString())
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.PENDING.toString())
                .validateRespCode(Constants.ResponseCode.TXN_STATUS_PENDING.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_STATUS_PENDING.getRespMsg())
                .validatePaymentMode("NB")
                .validateGatewayName(Constants.Gateway.PPBL.toString())
                .validateMid(mid)
                .AssertAll();

        CloseOrderDTO closeOrderDTO = new CloseOrderDTO();
        closeOrderDTO.setHead(new com.paytm.dto.CloseOrder.Head().setChannelId("WAP").setVersion("v1").setSigature(""))
                .setBody(new com.paytm.dto.CloseOrder.Body().setIsForceClose(true).setOrderId(initTxnDTO.getBody().getOrderId()).setMid(Constants.MerchantType.PG2_COP_FULL_TRAFFIC_Y.getId()));
        CloseOrderAPI closeOrderAPI = new CloseOrderAPI(closeOrderDTO);
        Response closeOrderResponse = closeOrderAPI.execute();
        Assertions.assertThat(closeOrderResponse.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("SUCCESS");
        Assertions.assertThat(closeOrderResponse.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(closeOrderResponse.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo("01");

        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_FAILURE.toString())
                .validateRespCode(Constants.ResponseCode.PAYMENT_FAILED.getRespCode())
                .validateRespMsg(Constants.ResponseCode.PAYMENT_FAILED.getRespMsg())
                .validatePaymentMode("NB")
                .validateMid(mid)
                .AssertAll();
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PG2-4086")
    @Test(description = "Validate Full Async Refund Co Then Pay PPBL transaction for PG2 MID routed through PG2")
    public void validateFullAsyncRefundCoThenPayPPBLTxnPG2MID() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, Constants.Bank.PPBL.toString(), "false");
        String mid = merchant.getId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode(Constants.Bank.PPBL.toString())
                .setAuthMode("USRPWD")
                .setMpin("1234")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);

        assertPTCCommonResponse(ptcResponse);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        CommonHelpers.verifyEsnLength(initTxnDTO.getBody().getOrderId(),19);

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
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Bank.PPBL.toString())
                .validateBankName(Constants.Bank.PPBL.toString())
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
                .validateGatewayName(Constants.Gateway.PPBL.toString())
                .validateMid(mid)
                .AssertAll();

        SyncRefund syncRefund = new SyncRefund();
        Response asyncRefundResp = given().spec(syncRefund.reqSpecAsyncRefund(merchant, initTxnDTO.txnAmountFromBody(), initTxnDTO.getBody().getOrderId(),
                        txnStatus.getResponse().getTXNID()))
                .post().then().extract().response();
        System.out.println("Async Refund Response " + asyncRefundResp.body().prettyPrint());
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.orderId")).isEqualTo(initTxnDTO.getBody().getOrderId());
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.mid")).isEqualTo(mid);
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.refId")).isNotNull();
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo(Constants.TXNSTATUS.PENDING.toString());
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo(Constants.ResponseCode.REFUND_PENDING.getRespCode());
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo(Constants.ResponseCode.REFUND_PENDING.getRespMsg());
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.refundId")).isNotNull();
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.txnId")).isEqualTo(txnStatus.getResponse().getTXNID());
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.refundAmount")).isEqualTo(initTxnDTO.txnAmountFromBody());
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PG2-4086")
    @Test(description = "Validate Partial Async Refund Co Then Pay PPBL transaction for PG2 MID routed through PG2")
    public void validatePartialAsyncRefundCoThenPayPPBLTxnPG2MID() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue("2").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, Constants.Bank.PPBL.toString(), "false");
        String mid = merchant.getId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode(Constants.Bank.PPBL.toString())
                .setAuthMode("USRPWD")
                .setMpin("1234")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        CommonHelpers.verifyEsnLength(initTxnDTO.getBody().getOrderId(),19);

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
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Bank.PPBL.toString())
                .validateBankName(Constants.Bank.PPBL.toString())
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
                .validateGatewayName(Constants.Gateway.PPBL.toString())
                .validateMid(mid)
                .AssertAll();

        SyncRefund syncRefund = new SyncRefund();
        String refundAmount = "1";
        Response asyncRefundResp = given().spec(syncRefund.reqSpecAsyncRefund(merchant, refundAmount, initTxnDTO.getBody().getOrderId(),
                        txnStatus.getResponse().getTXNID()))
                .post().then().extract().response();
        System.out.println("Async Refund Response " + asyncRefundResp.body().prettyPrint());
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.orderId")).isEqualTo(initTxnDTO.getBody().getOrderId());
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
    @Feature("PG2-4579")
    @Parameters({"theme"})
    @Test(description = "Validate Full Traffic Co Then Pay Success PPBL app invoke Txn")
    public void validatePG2COThenPayFullTrafficYPPBLSuccessInvokeAppTxn(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(merchant,initTxnDTO.getBody().getOrderId(),txnToken).build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.PPBL);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("NB")
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_SUCCESS.getRespMsg())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.PPBL.toString())
                .validateBankName(Constants.Gateway.PPBL.toString())
                .assertAll();

        String txnid = responsePage.textTxnID().getText();
        Assertions.assertThat(txnid).contains("0000");

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
                .validateGatewayName(Constants.Gateway.PPBL.toString())
                .validateMid(initTxnDTO.getBody().getMid())
                .AssertAll();
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PG2-4579")
    @Parameters({"theme"})
    @Test(description = "Validate Full Traffic Co Then Pay Failure PPBL app invoke Txn")
    public void validatePG2COThenPayFullTrafficYPPBLFailureInvokeAppTxn(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue("60.1").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(merchant,initTxnDTO.getBody().getOrderId(),txnToken).build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.PPBL);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("NB")
                .validateRespCode(Constants.ResponseCode.BANK_TXN_FAILURE.getRespCode())
                .validateRespMsg(Constants.ResponseCode.BANK_TXN_FAILURE.getRespMsg())
                .validateStatus(Constants.TXNSTATUS.TXN_FAILURE.toString())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.PPBL.toString())
                .validateBankName(Constants.Gateway.PPBL.toString())
                .assertAll();

        String txnid = responsePage.textTxnID().getText();
        Assertions.assertThat(txnid).contains("0000");

        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_FAILURE.toString())
                .validateRespCode(Constants.ResponseCode.BANK_TXN_FAILURE.getRespCode())
                .validateRespMsg(Constants.ResponseCode.BANK_TXN_FAILURE.getRespMsg())
                .validatePaymentMode("NB")
                .validateGatewayName(Constants.Gateway.PPBL.toString())
                .validateMid(initTxnDTO.getBody().getMid())
                .AssertAll();
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PG2-4579")
    @Parameters({"theme"})
    @Test(description = "Validate Full Traffic Co Then Pay Pending PPBL app invoke Txn")
    public void validatePG2COThenPayFullTrafficYPPBLPendingInvokeAppTxn(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue("60.2").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(merchant,initTxnDTO.getBody().getOrderId(),txnToken).build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.PPBL);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("NB")
                .validateRespCode(Constants.ResponseCode.TXN_STATUS_PENDING.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_STATUS_PENDING.getRespMsg())
                .validateStatus(Constants.TXNSTATUS.PENDING.toString())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.PPBL.toString())
                .validateBankName(Constants.Gateway.PPBL.toString())
                .assertAll();

        String txnid = responsePage.textTxnID().getText();
        Assertions.assertThat(txnid).contains("0000");

        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.PENDING.toString())
                .validateRespCode(Constants.ResponseCode.TXN_STATUS_PENDING.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_STATUS_PENDING.getRespMsg())
                .validatePaymentMode("NB")
                .validateGatewayName(Constants.Gateway.PPBL.toString())
                .validateMid(initTxnDTO.getBody().getMid())
                .AssertAll();
    }

    @Owner(Constants.Owner.VIKASH_VERMA)
    @Feature("PG2-4571")
    @Test(description = "PG2 COP Initiate Txn is executed with Amt 60.1, PPBL failed TXn is done PTC is executed again with same paymode")
    public void validateFullTrafficCOPFailedPPBLTxnretrywithwallet() throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        WalletHelpers.modifyBalance(user, 61.0);
        Constants.MerchantType merchant = Constants.MerchantType.PG2_COP_FULL_TRAFFIC_Y_Retry;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue("60.1").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, Constants.Bank.PPBL.toString(), "false");
        String mid = merchant.getId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid, initTxnResponse.getBody().getTxnToken(), initTxnDTO.getBody().getOrderId(), "PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode(Constants.Bank.PPBL.toString())
                .setAuthMode("USRPWD")
                .setMpin("1234")
                .build();

        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        CommonHelpers.verifyEsnLength(initTxnDTO.getBody().getOrderId(),19);
        NativeHelpers.submitJsonFormInBrowser(ptcResponse.toString());
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateCurrency("INR")
                .validateMid(mid)
                .validateOrderId(initTxnDTO.orderFromBody())
                .validatePaymentMode("NB")
                .validateRespCode(Constants.ResponseCode.BANK_TXN_FAILURE.getRespCode())
                .validateRespMsg(Constants.ResponseCode.BANK_TXN_FAILURE.getRespMsg())
                .validateStatus(Constants.TXNSTATUS.TXN_FAILURE.toString())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Bank.PPBL.toString())
                .validateBankName(Constants.Bank.PPBL.toString())
                .assertAll();


        ProcessTxnV1Request processTxnV1Request1 = new ProcessTxnV1Request.Builder(
                Constants.MerchantType.PG2_COP_FULL_TRAFFIC_Y_Retry.getId(), initTxnResponse.getBody().getTxnToken(), initTxnDTO.getBody().getOrderId(), "")
                .setPaymentMode("BALANCE")
                .setAuthMode("USRPWD")
                .build();

        ProcessTxnV1Response ptcResponse1 = NativeHelpers.executeProcessTxnV1(processTxnV1Request1);
        assertPTCCommonResponse(ptcResponse1, initTxnDTO);
        Assertions.assertThat(ptcResponse1.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse1.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse1.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        Assertions.assertThat(ptcResponse1.getBody().getTxnInfo().getPAYMENTMODE()).isEqualTo("PPI");
        Assertions.assertThat(ptcResponse1.getBody().getTxnInfo().getRESPCODE()).isEqualTo("01");
        Assertions.assertThat(ptcResponse1.getBody().getTxnInfo().getSTATUS()).isEqualTo(Constants.TXNSTATUS.TXN_SUCCESS.toString());

        String txnid = ptcResponse1.getBody().getTxnInfo().getTXNID();
        Assertions.assertThat(txnid).contains("0000");
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("PPI")
                .validateGatewayName(Constants.Gateway.WALLET.toString())
                .validateMid(Constants.MerchantType.PG2_COP_FULL_TRAFFIC_Y_Retry.getId())
                .AssertAll();


        TransactionStatusV1DTO transactionStatusV1DTO = new TransactionStatusV1DTO();
        transactionStatusV1DTO.setBody(new Body().setIsCallbackUrlRequired(true).setIsFinalTxnStatusRequired(false).setMid(Constants.MerchantType.PG2_COP_FULL_TRAFFIC_Y_Retry.getId()).setOrderId(initTxnDTO.getBody().getOrderId()))
                .setHead(new Head().setTokenType("TXN_TOKEN").setToken(txnToken));
        TransactionStatusV1API transactionStatusV1API = new TransactionStatusV1API(transactionStatusV1DTO);
        Response theiaTxnStatusResponse = transactionStatusV1API.execute();
        assertTheiaTxnStatusCommonResponse(theiaTxnStatusResponse, initTxnDTO, Constants.ResponseCode.TXN_SUCCESS);
        Assertions.assertThat(theiaTxnStatusResponse.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(theiaTxnStatusResponse.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo(Constants.ResponseCode.TXN_SUCCESS.getRespCode());
        Assertions.assertThat(theiaTxnStatusResponse.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo(Constants.ResponseCode.TXN_SUCCESS.getRespMsg());
        Assertions.assertThat(theiaTxnStatusResponse.jsonPath().getString("body.txnInfo.STATUS")).isEqualTo(Constants.TXNSTATUS.TXN_SUCCESS.toString());
    }
    @Owner(PRAMOD_KUMAR)
    @Feature("PG2-4499")
    @Test(description = "Validate full traffic Success COP PPBL Internal Order Modify txn through PG2")
    public void validateFullTrafficYSuccessCOPPPBLInternalOrderModifyTxnPG2MID() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.PG2_COP_FULL_TRAFFIC_Y).setTxnValue("5.6").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, Constants.Bank.PPBL.toString(), "false");
        String mid = Constants.MerchantType.PG2_COP_FULL_TRAFFIC_Y.getId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid, initTxnResponse.getBody().getTxnToken(), initTxnDTO.getBody().getOrderId(), "PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode(Constants.Bank.PPBL.toString())
                .setAuthMode("USRPWD")
                .setMpin("1234")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        CommonHelpers.verifyEsnLength(initTxnDTO.getBody().getOrderId(),19);

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
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Bank.PPBL.toString())
                .validateBankName(Constants.Bank.PPBL.toString())
                .assertAll();

        String orderId = responsePage.textOrderID().getText();
//      order modify logs not come now
//      validateAcquiringOrderModify(orderId);

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
                .validateGatewayName(Constants.Gateway.PPBL.toString())
                .validateMid(mid)
                .AssertAll();

        TransactionStatusV1DTO transactionStatusV1DTO = new TransactionStatusV1DTO();
        transactionStatusV1DTO.setBody(new Body().setIsCallbackUrlRequired(true).setIsFinalTxnStatusRequired(false).setMid(mid).setOrderId(orderId))
                .setHead(new Head().setVersion(API_VERSION_V2).setTokenType("TXN_TOKEN").setToken(txnToken));
        TransactionStatusV1API transactionStatusV1API = new TransactionStatusV1API(transactionStatusV1DTO);
        Response response = transactionStatusV1API.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo(Constants.ResponseCode.TXN_SUCCESS.getRespCode());
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo(Constants.ResponseCode.TXN_SUCCESS.getRespMsg());
        Assertions.assertThat(response.jsonPath().getString("body.txnInfo.STATUS")).isEqualTo(Constants.TXNSTATUS.TXN_SUCCESS.toString());
        Assertions.assertThat(response.jsonPath().getString("body.txnInfo.STATUS")).isEqualTo("TXN_SUCCESS");
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PG2-4589")
    @Test(description = "Validate PG2_ENABLED Co Then Pay Success PPBL transaction for PG2 MID routed through P+")
    public void validatePG2EnabledCoThenPaySuccessPPBLTxnPG2MID() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.PG2_ENABLED_CO_THEN_PAY;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, Constants.Bank.PPBL.toString(), "false");
        String mid = merchant.getId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode(Constants.Bank.PPBL.toString())
                .setAuthMode("USRPWD")
                .setMpin("1234")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        CommonHelpers.verifyEsnLength(initTxnDTO.getBody().getOrderId(),20);

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
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Bank.PPBL.toString())
                .validateBankName(Constants.Bank.PPBL.toString())
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
                .validateGatewayName(Constants.Gateway.PPBL.toString())
                .validateMid(mid)
                .AssertAll();
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PG2-4589")
    @Test(description = "Validate PG2_ENABLED Co Then Pay Fail PPBL transaction for PG2 MID routed through P+")
    public void validatePG2EnabledCoThenPayFailPPBLTxnPG2MID() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.PG2_ENABLED_CO_THEN_PAY;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue("60.1").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, Constants.Bank.PPBL.toString(), "false");
        String mid = merchant.getId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode(Constants.Bank.PPBL.toString())
                .setAuthMode("USRPWD")
                .setMpin("1234")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        CommonHelpers.verifyEsnLength(initTxnDTO.getBody().getOrderId(),20);

        NativeHelpers.submitJsonFormInBrowser(ptcResponse.toString());
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(mid)
                .validateOrderId(initTxnDTO.orderFromBody())
                .validatePaymentMode("NB")
                .validateRespCode(Constants.ResponseCode.BANK_TXN_FAILURE.getRespCode())
                .validateRespMsg(Constants.ResponseCode.BANK_TXN_FAILURE.getRespMsg())
                .validateStatus(Constants.TXNSTATUS.TXN_FAILURE.toString())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Bank.PPBL.toString())
                .validateBankName(Constants.Bank.PPBL.toString())
                .assertAll();

        // adding this code to manually close the order when order is not getting closed for some txn.
        CloseOrderDTO closeOrderDTO = new CloseOrderDTO();
        closeOrderDTO.setHead(new com.paytm.dto.CloseOrder.Head().setChannelId("WEB").setVersion("v1").setSigature(""))
                .setBody(new com.paytm.dto.CloseOrder.Body().setIsForceClose(true).setOrderId(initTxnDTO.getBody().getOrderId()).setMid(merchant.getId()));
        CloseOrderAPI closeOrderAPI = new CloseOrderAPI(closeOrderDTO);
        closeOrderAPI.execute();

        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_FAILURE.toString())
                .validateRespCode(Constants.ResponseCode.BANK_TXN_FAILURE.getRespCode())
                .validateRespMsg(Constants.ResponseCode.BANK_TXN_FAILURE.getRespMsg())
                .validatePaymentMode("NB")
                .validateGatewayName(Constants.Gateway.PPBL.toString())
                .validateMid(mid)
                .AssertAll();
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PG2-4589")
    @Test(description = "Validate PG2_ENABLED Co Then Pay Pending PPBL transaction for PG2 MID routed through P+")
    public void validatePG2EnabledCoThenPayPendingPPBLTxnPG2MID() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.PG2_ENABLED_CO_THEN_PAY;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue("60.2").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, Constants.Bank.PPBL.toString(), "false");
        String mid = merchant.getId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode(Constants.Bank.PPBL.toString())
                .setAuthMode("USRPWD")
                .setMpin("1234")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        CommonHelpers.verifyEsnLength(initTxnDTO.getBody().getOrderId(),20);

        NativeHelpers.submitJsonFormInBrowser(ptcResponse.toString());
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(mid)
                .validateOrderId(initTxnDTO.orderFromBody())
                .validatePaymentMode("NB")
                .validateRespCode(Constants.ResponseCode.TXN_STATUS_PENDING.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_STATUS_PENDING.getRespMsg())
                .validateStatus(Constants.TXNSTATUS.PENDING.toString())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.PENDING.toString())
                .validateRespCode(Constants.ResponseCode.TXN_STATUS_PENDING.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_STATUS_PENDING.getRespMsg())
                .validateMid(mid)
                .AssertAll();

        CloseOrderDTO closeOrderDTO = new CloseOrderDTO();
        closeOrderDTO.setHead(new com.paytm.dto.CloseOrder.Head().setChannelId("WEB").setVersion("v1").setSigature(""))
                .setBody(new com.paytm.dto.CloseOrder.Body().setIsForceClose(true).setOrderId(initTxnDTO.getBody().getOrderId()).setMid(merchant.getId()));
        CloseOrderAPI closeOrderAPI = new CloseOrderAPI(closeOrderDTO);
        Response closeOrderResponse = closeOrderAPI.execute();
        Assertions.assertThat(closeOrderResponse.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("SUCCESS");
        Assertions.assertThat(closeOrderResponse.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(closeOrderResponse.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo("01");

        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_FAILURE.toString())
                .validateRespCode(Constants.ResponseCode.PAYMENT_FAILED.getRespCode())
                .validateRespMsg(Constants.ResponseCode.PAYMENT_FAILED.getRespMsg())
                .validateMid(mid)
                .AssertAll();
    }

    @Owner(Constants.Owner.VIKASH_VERMA)
    @Feature("PG2-4725")
    @Test(description = "PG2 CO Then Pay Initiate Txn is executed with Amt 60.1, PPBL failed TXn is done PTC is executed again with same paymode")
    public void validateFullTrafficCOThenpayFailedPPBLTxnretrywithwallet() throws Exception {
        User user = userManager.getForWrite(Label.UPIPUSHPG2);
        WalletHelpers.modifyBalance(user, 61.0);
        Constants.MerchantType merchant = Constants.MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y_Retry;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue("60.1").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, Constants.Bank.PPBL.toString(), "false");
        String mid = merchant.getId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid, initTxnResponse.getBody().getTxnToken(), initTxnDTO.getBody().getOrderId(), "PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode(Constants.Bank.PPBL.toString())
                .setAuthMode("USRPWD")
                .setMpin("1234")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        CommonHelpers.verifyEsnLength(initTxnDTO.getBody().getOrderId(),19);
        NativeHelpers.submitJsonFormInBrowser(ptcResponse.toString());
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateCurrency("INR")
                .validateMid(mid)
                .validateOrderId(initTxnDTO.orderFromBody())
                .validatePaymentMode("NB")
                .validateRespCode(Constants.ResponseCode.BANK_TXN_FAILURE.getRespCode())
                .validateRespMsg(Constants.ResponseCode.BANK_TXN_FAILURE.getRespMsg())
                .validateStatus(Constants.TXNSTATUS.TXN_FAILURE.toString())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Bank.PPBL.toString())
                .validateBankName(Constants.Bank.PPBL.toString())
                .assertAll();
        ProcessTxnV1Request processTxnV1Request1 = new ProcessTxnV1Request.Builder(
                Constants.MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y_Retry.getId(), initTxnResponse.getBody().getTxnToken(), initTxnDTO.getBody().getOrderId(), "")
                .setPaymentMode("BALANCE")
                .setAuthMode("USRPWD")
                .build();
        ProcessTxnV1Response ptcResponse1 = NativeHelpers.executeProcessTxnV1(processTxnV1Request1);
        assertPTCCommonResponse(ptcResponse1, initTxnDTO);
        Assertions.assertThat(ptcResponse1.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse1.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse1.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        Assertions.assertThat(ptcResponse1.getBody().getTxnInfo().getPAYMENTMODE()).isEqualTo("PPI");
        Assertions.assertThat(ptcResponse1.getBody().getTxnInfo().getRESPCODE()).isEqualTo("01");
        Assertions.assertThat(ptcResponse1.getBody().getTxnInfo().getSTATUS()).isEqualTo(Constants.TXNSTATUS.TXN_SUCCESS.toString());
        String txnid = ptcResponse1.getBody().getTxnInfo().getTXNID();
        Assertions.assertThat(txnid).contains("0000");
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("PPI")
                .validateGatewayName(Constants.Gateway.WALLET.toString())
                .validateMid(Constants.MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y_Retry.getId())
                .AssertAll();
        TransactionStatusV1DTO transactionStatusV1DTO = new TransactionStatusV1DTO();
        transactionStatusV1DTO.setBody(new Body().setIsCallbackUrlRequired(true).setIsFinalTxnStatusRequired(false).setMid(Constants.MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y_Retry.getId()).setOrderId(initTxnDTO.getBody().getOrderId()))
                .setHead(new Head().setTokenType("TXN_TOKEN").setToken(txnToken));
        TransactionStatusV1API transactionStatusV1API = new TransactionStatusV1API(transactionStatusV1DTO);
        Response theiaTxnStatusResponse = transactionStatusV1API.execute();
        assertTheiaTxnStatusCommonResponse(theiaTxnStatusResponse, initTxnDTO, Constants.ResponseCode.TXN_SUCCESS);
        Assertions.assertThat(theiaTxnStatusResponse.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(theiaTxnStatusResponse.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo(Constants.ResponseCode.TXN_SUCCESS.getRespCode());
        Assertions.assertThat(theiaTxnStatusResponse.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo(Constants.ResponseCode.TXN_SUCCESS.getRespMsg());
        Assertions.assertThat(theiaTxnStatusResponse.jsonPath().getString("body.txnInfo.STATUS")).isEqualTo(Constants.TXNSTATUS.TXN_SUCCESS.toString());
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PG2-4617")
    @Parameters({"theme"})
    @Test(description = "Validate Full Traffic Co Then Pay Success PPBL Checkout JS Page Txn")
    public void validatePG2COThenPayFullTrafficYPPBLSuccessCheckoutJSPageTxn(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        MerchantConfig config = checkoutJsPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutJsPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.PPBL);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("NB")
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_SUCCESS.getRespMsg())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.PPBL.toString())
                .validateBankName(Constants.Gateway.PPBL.toString())
                .assertAll();
        String txnid = responsePage.textTxnID().getText();
        Assertions.assertThat(txnid).contains("0000");
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
                .validateGatewayName(Constants.Gateway.PPBL.toString())
                .validateMid(initTxnDTO.getBody().getMid())
                .AssertAll();
    }
    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PG2-4617")
    @Parameters({"theme"})
    @Test(description = "Validate Full Traffic Co Then Pay Fail PPBL Checkout JS Page Txn")
    public void validatePG2COThenPayFullTrafficYPPBLFailCheckoutJSPageTxn(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue("60.1").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        MerchantConfig config = checkoutJsPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutJsPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.PPBL);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("NB")
                .validateRespCode(Constants.ResponseCode.BANK_TXN_FAILURE.getRespCode())
                .validateRespMsg(Constants.ResponseCode.BANK_TXN_FAILURE.getRespMsg())
                .validateStatus(Constants.TXNSTATUS.TXN_FAILURE.toString())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.PPBL.toString())
                .validateBankName(Constants.Gateway.PPBL.toString())
                .assertAll();
        String txnid = responsePage.textTxnID().getText();
        Assertions.assertThat(txnid).contains("0000");
        // adding this code to manually close the order when order is not getting closed for some txn.
        CloseOrderDTO closeOrderDTO = new CloseOrderDTO();
        closeOrderDTO.setHead(new com.paytm.dto.CloseOrder.Head().setChannelId("WEB").setVersion("v1").setSigature(""))
                .setBody(new com.paytm.dto.CloseOrder.Body().setIsForceClose(true).setOrderId(initTxnDTO.getBody().getOrderId()).setMid(merchant.getId()));
        CloseOrderAPI closeOrderAPI = new CloseOrderAPI(closeOrderDTO);
        closeOrderAPI.execute();
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_FAILURE.toString())
                .validateRespCode(Constants.ResponseCode.BANK_TXN_FAILURE.getRespCode())
                .validateRespMsg(Constants.ResponseCode.BANK_TXN_FAILURE.getRespMsg())
                .validatePaymentMode("NB")
                .validateGatewayName(Constants.Gateway.PPBL.toString())
                .validateMid(initTxnDTO.getBody().getMid())
                .AssertAll();
    }
    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PG2-4617")
    @Parameters({"theme"})
    @Test(description = "Validate Full Traffic Co Then Pay Pending PPBL Checkout JS Page Txn")
    public void validatePG2COThenPayFullTrafficYPPBLPendingCheckoutJSPageTxn(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue("60.2").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        MerchantConfig config = checkoutJsPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutJsPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.PPBL);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("NB")
                .validateRespCode(Constants.ResponseCode.TXN_STATUS_PENDING.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_STATUS_PENDING.getRespMsg())
                .validateStatus(Constants.TXNSTATUS.PENDING.toString())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.PPBL.toString())
                .validateBankName(Constants.Gateway.PPBL.toString())
                .assertAll();
        String txnid = responsePage.textTxnID().getText();
        Assertions.assertThat(txnid).contains("0000");
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.PENDING.toString())
                .validateRespCode(Constants.ResponseCode.TXN_STATUS_PENDING.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_STATUS_PENDING.getRespMsg())
                .validatePaymentMode("NB")
                .validateGatewayName(Constants.Gateway.PPBL.toString())
                .validateMid(initTxnDTO.getBody().getMid())
                .AssertAll();
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PG2-4804")
    @Test(description = "PG2 CO then Pay full traffic Y Initiate Txn is executed with Amt 5.6 rent mid PostPaid Txn done")
    public void validateCoThenPaySuccessRentMidOrderModifyPPBLTxnPG2MID() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue("5.6").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, Constants.Bank.PPBL.toString(), "false");
        String mid = merchant.getId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode(Constants.Bank.PPBL.toString())
                .setAuthMode("USRPWD")
                .setMpin("1234")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        CommonHelpers.verifyEsnLength(initTxnDTO.getBody().getOrderId(),19);
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
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Bank.PPBL.toString())
                .validateBankName(Constants.Bank.PPBL.toString())
                .assertAll();
        String orderId = responsePage.textOrderID().getText();
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
                .validateGatewayName(Constants.Gateway.PPBL.toString())
                .validateMid(mid)
                .AssertAll();
        TransactionStatusV1DTO transactionStatusV1DTO = new TransactionStatusV1DTO();
        transactionStatusV1DTO.setBody(new Body().setIsCallbackUrlRequired(true).setIsFinalTxnStatusRequired(false).setMid(mid).setOrderId(orderId))
                .setHead(new Head().setVersion(API_VERSION_V2).setTokenType("TXN_TOKEN").setToken(txnToken));
        TransactionStatusV1API transactionStatusV1API = new TransactionStatusV1API(transactionStatusV1DTO);
        Response response = transactionStatusV1API.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo(Constants.ResponseCode.TXN_SUCCESS.getRespCode());
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo(Constants.ResponseCode.TXN_SUCCESS.getRespMsg());
        Assertions.assertThat(response.jsonPath().getString("body.txnInfo.STATUS")).isEqualTo(Constants.TXNSTATUS.TXN_SUCCESS.toString());
        Assertions.assertThat(response.jsonPath().getString("body.txnInfo.STATUS")).isEqualTo("TXN_SUCCESS");
    }

    @Owner(PRAMOD_KUMAR)
    @Feature("PG2-4562")
    @Test(description = "Validate Success Co Then Pay PPBL Simplified Promo transaction for PG2 MID routed through PG2")
    public void validateCoThenPaySimplifiedPromoOrderModifyPPBLTxnPG2MID() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y;
        String mid = Constants.MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y.getId();
        Promo promocode = new Promo(true);
        new Merchant(mid, true ).getPromos().add(promocode);
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode("discount").setApplyAvailablePromo("true").setValidatePromo("true");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant,simplifiedPaymentOffers).setTxnValue("100").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, Constants.Bank.PPBL.toString(), "false");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode(Constants.Bank.PPBL.toString())
                .setAuthMode("USRPWD")
                .setMpin("1234")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        CommonHelpers.verifyEsnLength(initTxnDTO.getBody().getOrderId(),19);
        NativeHelpers.submitJsonFormInBrowser(ptcResponse.toString());
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        String discountedTxnAmmount= responsePage.textTXNAMOUNT().getText();
        String orderID= responsePage.textOrderID().getText();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(mid)
                .validateOrderId(initTxnDTO.orderFromBody())
                .validatePaymentMode("NB")
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_SUCCESS.getRespMsg())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(discountedTxnAmmount))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Bank.PPBL.toString())
                .validateBankName(Constants.Bank.PPBL.toString())
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(discountedTxnAmmount)
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_SUCCESS.getRespMsg())
                .validatePaymentMode("NB")
                .validateGatewayName(Constants.Gateway.PPBL.toString())
                .validateMid(mid)
                .AssertAll();
        TransactionStatusV1DTO transactionStatusV1DTO = new TransactionStatusV1DTO();
        transactionStatusV1DTO.setBody(new Body().setIsCallbackUrlRequired(true).setIsFinalTxnStatusRequired(false).setMid(mid).setOrderId(orderID))
                .setHead(new Head().setVersion(API_VERSION_V2).setTokenType("TXN_TOKEN").setToken(txnToken));
        TransactionStatusV1API transactionStatusV1API = new TransactionStatusV1API(transactionStatusV1DTO);
        Response response = transactionStatusV1API.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo(Constants.ResponseCode.TXN_SUCCESS.getRespCode());
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo(Constants.ResponseCode.TXN_SUCCESS.getRespMsg());
        Assertions.assertThat(response.jsonPath().getString("body.txnInfo.STATUS")).isEqualTo(Constants.TXNSTATUS.TXN_SUCCESS.toString());
        Assertions.assertThat(response.jsonPath().getString("body.txnInfo.STATUS")).isEqualTo("TXN_SUCCESS");
    }
    @Owner(PRAMOD_KUMAR)
    @Feature("PG2-4800")
    @Test(description = "Validate full traffic Success COP PPBL Order Modify txn through PG2")
    public void validateFullTrafficYSuccessCOPPPBLOrderModifyTxnPG2MID() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.PG2_COP_Full_Traffic_NoRENTPref).setTxnValue("5").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, Constants.Bank.PPBL.toString(), "false");
        String mid = Constants.MerchantType.PG2_COP_Full_Traffic_NoRENTPref.getId();

        Double updatedTxnAmount = Double.parseDouble(initTxnDTO.txnAmountFromBody()) + 1;
        TxnAmount txnAmount = initTxnDTO.getBody().getTxnAmount();
        txnAmount.setValue(String.valueOf(updatedTxnAmount));
        UpdateTransactionDTO updateTransactionDTO = new UpdateTransactionDTO();
        updateTransactionDTO.setHead(new com.paytm.dto.NativeDTO.UpdateTransaction.Head(txnToken))
                .setBody(new com.paytm.dto.NativeDTO.UpdateTransaction.Body().setExtendInfo(initTxnDTO.getBody().getExtendInfo())
                        .setGoods(Arrays.asList(initTxnDTO.getBody().getGoods().clone()))
                        .setShippingInfo(initTxnDTO.getBody().getShippingInfo())
                        .setTxnAmount(txnAmount));
        updateTransactionDTO.setChecksum(Constants.MerchantType.PG2_COP_Full_Traffic_NoRENTPref.getKey());
        UpdateTransaction updateTransaction = new UpdateTransaction(updateTransactionDTO, initTxnDTO.getBody().getMid(), initTxnDTO.orderFromBody());
        updateTransaction.execute();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid, initTxnResponse.getBody().getTxnToken(), initTxnDTO.getBody().getOrderId(), "PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode(Constants.Bank.PPBL.toString())
                .setAuthMode("USRPWD")
                .setMpin("1234")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        CommonHelpers.verifyEsnLength(initTxnDTO.getBody().getOrderId(),19);

        NativeHelpers.submitJsonFormInBrowser(ptcResponse.toString());
        ResponsePage responsePage = new ResponsePage();
        String orderModifyTxnAmount= responsePage.textTXNAMOUNT().getText();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(mid)
                .validateOrderId(initTxnDTO.orderFromBody())
                .validatePaymentMode("NB")
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_SUCCESS.getRespMsg())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderModifyTxnAmount))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Bank.PPBL.toString())
                .validateBankName(Constants.Bank.PPBL.toString())
                .assertAll();

        String orderId = responsePage.textOrderID().getText();
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(orderModifyTxnAmount)
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_SUCCESS.getRespMsg())
                .validatePaymentMode("NB")
                .validateGatewayName(Constants.Gateway.PPBL.toString())
                .validateMid(mid)
                .AssertAll();

        TransactionStatusV1DTO transactionStatusV1DTO = new TransactionStatusV1DTO();
        transactionStatusV1DTO.setBody(new Body().setIsCallbackUrlRequired(true).setIsFinalTxnStatusRequired(false).setMid(mid).setOrderId(orderId))
                .setHead(new Head().setVersion(API_VERSION_V2).setTokenType("TXN_TOKEN").setToken(txnToken));
        TransactionStatusV1API transactionStatusV1API = new TransactionStatusV1API(transactionStatusV1DTO);
        Response response = transactionStatusV1API.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo(Constants.ResponseCode.TXN_SUCCESS.getRespCode());
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo(Constants.ResponseCode.TXN_SUCCESS.getRespMsg());
        Assertions.assertThat(response.jsonPath().getString("body.txnInfo.STATUS")).isEqualTo(Constants.TXNSTATUS.TXN_SUCCESS.toString());
        Assertions.assertThat(response.jsonPath().getString("body.txnInfo.STATUS")).isEqualTo("TXN_SUCCESS");

    }
    @Owner(PRAMOD_KUMAR)
    @Feature("PG2-4800")
    @Test(description = "Validate full traffic Success CO then Pay PPBL Order Modify txn through PG2")
    public void validateFullTrafficYSuccessCOThenPayPPPBLOrderModifyTxnPG2MID() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.PG2_CO_Then_Pay_Full_Traffic_NoRentPref).setTxnValue("5").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, Constants.Bank.PPBL.toString(), "false");
        String mid = Constants.MerchantType.PG2_CO_Then_Pay_Full_Traffic_NoRentPref.getId();

        Double updatedTxnAmount = Double.parseDouble(initTxnDTO.txnAmountFromBody()) + 1;
        TxnAmount txnAmount = initTxnDTO.getBody().getTxnAmount();
        txnAmount.setValue(String.valueOf(updatedTxnAmount));
        UpdateTransactionDTO updateTransactionDTO = new UpdateTransactionDTO();
        updateTransactionDTO.setHead(new com.paytm.dto.NativeDTO.UpdateTransaction.Head(txnToken))
                .setBody(new com.paytm.dto.NativeDTO.UpdateTransaction.Body().setExtendInfo(initTxnDTO.getBody().getExtendInfo())
                        .setGoods(Arrays.asList(initTxnDTO.getBody().getGoods().clone()))
                        .setShippingInfo(initTxnDTO.getBody().getShippingInfo())
                        .setTxnAmount(txnAmount));
        updateTransactionDTO.setChecksum(Constants.MerchantType.PG2_CO_Then_Pay_Full_Traffic_NoRentPref.getKey());
        UpdateTransaction updateTransaction = new UpdateTransaction(updateTransactionDTO, initTxnDTO.getBody().getMid(), initTxnDTO.orderFromBody());
        updateTransaction.execute();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid, initTxnResponse.getBody().getTxnToken(), initTxnDTO.getBody().getOrderId(), "PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode(Constants.Bank.PPBL.toString())
                .setAuthMode("USRPWD")
                .setMpin("1234")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        CommonHelpers.verifyEsnLength(initTxnDTO.getBody().getOrderId(),19);

        NativeHelpers.submitJsonFormInBrowser(ptcResponse.toString());
        ResponsePage responsePage = new ResponsePage();
        String orderModifyTxnAmount= responsePage.textTXNAMOUNT().getText();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(mid)
                .validateOrderId(initTxnDTO.orderFromBody())
                .validatePaymentMode("NB")
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_SUCCESS.getRespMsg())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderModifyTxnAmount))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Bank.PPBL.toString())
                .validateBankName(Constants.Bank.PPBL.toString())
                .assertAll();

        String orderId = responsePage.textOrderID().getText();
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(orderModifyTxnAmount)
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_SUCCESS.getRespMsg())
                .validatePaymentMode("NB")
                .validateGatewayName(Constants.Gateway.PPBL.toString())
                .validateMid(mid)
                .AssertAll();

        TransactionStatusV1DTO transactionStatusV1DTO = new TransactionStatusV1DTO();
        transactionStatusV1DTO.setBody(new Body().setIsCallbackUrlRequired(true).setIsFinalTxnStatusRequired(false).setMid(mid).setOrderId(orderId))
                .setHead(new Head().setVersion(API_VERSION_V2).setTokenType("TXN_TOKEN").setToken(txnToken));
        TransactionStatusV1API transactionStatusV1API = new TransactionStatusV1API(transactionStatusV1DTO);
        Response response = transactionStatusV1API.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo(Constants.ResponseCode.TXN_SUCCESS.getRespCode());
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo(Constants.ResponseCode.TXN_SUCCESS.getRespMsg());
        Assertions.assertThat(response.jsonPath().getString("body.txnInfo.STATUS")).isEqualTo(Constants.TXNSTATUS.TXN_SUCCESS.toString());
        Assertions.assertThat(response.jsonPath().getString("body.txnInfo.STATUS")).isEqualTo("TXN_SUCCESS");
    }
    @Owner(HIMANSHU)
    @Test(description = "Validate PG2 txn through enhanced flow")
    public void ppblEnhancedFlow(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        OrderDTO orderDTO = new OrderFactory.AddnPay(Constants.MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y, theme, user).build();
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        //cashierPage.login(user);
        cashierPage.payBy(Constants.PayMode.PPBL);
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

    @Owner(VIKASH_VERMA)
    @Feature("PGP-46552")
    @Test(description = "Validate cop ultimate beneficiary name for PPBL transaction")
    public void validateCOPUltimateBeneNameInPPBLtxn() throws Exception {
        User user = userManager.getForRead(Label.PPBL);
        UltimateBeneficiaryDetails ultimateBeneficiaryDetails = new UltimateBeneficiaryDetails();
        ultimateBeneficiaryDetails.setUltimateBeneficiaryName("UltimateMerchantNameForPPBL");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.PG2_COP_FULL_TRAFFIC_Y)
                .setTxnValue("5")
                .setUltimateBeneficiaryDetails(ultimateBeneficiaryDetails).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();

        JsonPath fpoResponse = Validate_FetchPayInstrument(txnToken, initTxnDTO, Constants.Bank.PPBL.toString(), "false");
        Assertions.assertThat(fpoResponse.getString("body.ultimateBeneficiaryDetails.ultimateBeneficiaryName")).isEqualTo(ultimateBeneficiaryDetails.getUltimateBeneficiaryName());
        String mid = Constants.MerchantType.PG2_COP_FULL_TRAFFIC_Y.getId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid, initTxnResponse.getBody().getTxnToken(), initTxnDTO.getBody().getOrderId(), "PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode(Constants.Bank.PPBL.toString())
                .setAuthMode("USRPWD")
                .setMpin("1234")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
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
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Bank.PPBL.toString())
                .validateBankName(Constants.Bank.PPBL.toString())
                .assertAll();

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.getBody().getOrderId(),"ACQUIRING_CREATE_ORDER_AND_PAY");
        logs=logs.substring(logs.indexOf("passThroughExtendInfo")+24);
        String passThroughExtendInfologs = logs.substring(0,logs.indexOf("\",\"merchantType\""));
        String decrypted=PGPHelpers.Base64Decode(passThroughExtendInfologs);
        Assertions.assertThat(decrypted).contains("\"ultimateBeneficiaryName\":\""+ultimateBeneficiaryDetails.getUltimateBeneficiaryName()+"\"");

        String esnLog = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,initTxnDTO.orderFromBody(),"ExtSN=");
        String extSnValue = esnLog.substring(esnLog.indexOf("ExtSN="), esnLog.indexOf(", OrderId=")).replace("ExtSN=", "");
        System.out.println("Esn is " + extSnValue);
        String instaproxy = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,extSnValue,"DEBIT_MONEY_FROM_CASA");
        Assertions.assertThat(instaproxy).contains("\"merchant_name\":\""+ultimateBeneficiaryDetails.getUltimateBeneficiaryName()+"\"");

    }

    @Owner(VIKASH_VERMA)
    @Feature("PGP-46552")
    @Test(description = "Validate CO then pay ultimate beneficiary name for PPBL transaction")
    public void validateCOthenpayUltimateBeneNameInPPBLtxn() throws Exception {
        User user = userManager.getForRead(Label.PPBL);
        UltimateBeneficiaryDetails ultimateBeneficiaryDetails = new UltimateBeneficiaryDetails();
        ultimateBeneficiaryDetails.setUltimateBeneficiaryName("UltimateMerchantNameForPPBL");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y)
                .setTxnValue("5")
                .setUltimateBeneficiaryDetails(ultimateBeneficiaryDetails).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();

        JsonPath fpoResponse = Validate_FetchPayInstrument(txnToken, initTxnDTO, Constants.Bank.PPBL.toString(), "false");
        Assertions.assertThat(fpoResponse.getString("body.ultimateBeneficiaryDetails.ultimateBeneficiaryName")).isEqualTo(ultimateBeneficiaryDetails.getUltimateBeneficiaryName());
        String mid = Constants.MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y.getId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid, initTxnResponse.getBody().getTxnToken(), initTxnDTO.getBody().getOrderId(), "PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode(Constants.Bank.PPBL.toString())
                .setAuthMode("USRPWD")
                .setMpin("1234")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
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
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Bank.PPBL.toString())
                .validateBankName(Constants.Bank.PPBL.toString())
                .assertAll();

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.getBody().getOrderId(),"ACQUIRING_PAY_ORDER");
        String passThroughExtendInfologs = logs.substring(logs.indexOf("passThroughExtendInfo")+24,logs.indexOf("\",\"merchantType\""));
        String decrypted=PGPHelpers.Base64Decode(passThroughExtendInfologs);
        Assertions.assertThat(decrypted).contains("\"ultimateBeneficiaryName\":\""+ultimateBeneficiaryDetails.getUltimateBeneficiaryName()+"\"");

        String esnLog = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,initTxnDTO.orderFromBody(),"ExtSN=");
        String extSnValue = esnLog.substring(esnLog.indexOf("ExtSN="), esnLog.indexOf(", OrderId=")).replace("ExtSN=", "");
        System.out.println("Esn is " + extSnValue);
        String instaproxy = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,extSnValue,"DEBIT_MONEY_FROM_CASA");
        Assertions.assertThat(instaproxy).contains("\"merchant_name\":\""+ultimateBeneficiaryDetails.getUltimateBeneficiaryName()+"\"");

    }

    @Owner(VIKASH_VERMA)
    @Feature("PGP-46552")
    @Test(description = "Validate COP ultimate beneficiary name for more than fifty characters via PPBL transaction")
    public void validateCOPUltimateBeneNamemorethanfiftycharactersInPPBLtxn() throws Exception {
        User user = userManager.getForRead(Label.PPBL);
        UltimateBeneficiaryDetails ultimateBeneficiaryDetails = new UltimateBeneficiaryDetails();
        ultimateBeneficiaryDetails.setUltimateBeneficiaryName("UltimateMerchantNameForPPBLformorethanfiftycharacters");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.PPBL_NB)
                .setTxnValue("5")
                .setUltimateBeneficiaryDetails(ultimateBeneficiaryDetails).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();

        JsonPath fpoResponse = Validate_FetchPayInstrument(txnToken, initTxnDTO, Constants.Bank.PPBL.toString(), "false");
        Assertions.assertThat(fpoResponse.getString("body.ultimateBeneficiaryDetails.ultimateBeneficiaryName")).isEqualTo(ultimateBeneficiaryDetails.getUltimateBeneficiaryName());
        String mid = Constants.MerchantType.PPBL_NB.getId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid, initTxnResponse.getBody().getTxnToken(), initTxnDTO.getBody().getOrderId(), "PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode(Constants.Bank.PPBL.toString())
                .setAuthMode("USRPWD")
                .setMpin("1234")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
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
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Bank.PPBL.toString())
                .validateBankName(Constants.Bank.PPBL.toString())
                .assertAll();


        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.getBody().getOrderId(),"ACQUIRING_CREATE_ORDER_AND_PAY");
        logs=logs.substring(logs.indexOf("passThroughExtendInfo")+24);
        String passThroughExtendInfologs = logs.substring(0,logs.indexOf("\",\"merchantType\""));
        String decrypted=PGPHelpers.Base64Decode(passThroughExtendInfologs);
        Assertions.assertThat(decrypted).contains("\"ultimateBeneficiaryName\":\""+ultimateBeneficiaryDetails.getUltimateBeneficiaryName()+"\"");

        String esnLog = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,initTxnDTO.orderFromBody(),"ExtSN=");
        String extSnValue = esnLog.substring(esnLog.indexOf("ExtSN="), esnLog.indexOf(", OrderId=")).replace("ExtSN=", "");
        System.out.println("Esn is " + extSnValue);
        String instaproxy = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,extSnValue,"DEBIT_MONEY_FROM_CASA");
        Assertions.assertThat(instaproxy).contains("\"merchant_name\":\""+ultimateBeneficiaryDetails.getUltimateBeneficiaryName().substring(0,50)+"\"");

    }

    @Owner(VIKASH_VERMA)
    @Feature("PGP-46552")
    @Test(description = "Validate CO THEN PAY ultimate beneficiary name for more than fifty characters via PPBL transaction")
    public void validateCOTHENPAYPUltimateBeneNamemorethanfiftycharactersInPPBLtxn() throws Exception {
        User user = userManager.getForRead(Label.PPBL);
        UltimateBeneficiaryDetails ultimateBeneficiaryDetails = new UltimateBeneficiaryDetails();
        ultimateBeneficiaryDetails.setUltimateBeneficiaryName("UltimateMerchantNameForPPBLformorethanfiftycharacters");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y)
                .setTxnValue("5")
                .setUltimateBeneficiaryDetails(ultimateBeneficiaryDetails).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();

        JsonPath fpoResponse = Validate_FetchPayInstrument(txnToken, initTxnDTO, Constants.Bank.PPBL.toString(), "false");
        Assertions.assertThat(fpoResponse.getString("body.ultimateBeneficiaryDetails.ultimateBeneficiaryName")).isEqualTo(ultimateBeneficiaryDetails.getUltimateBeneficiaryName());
        String mid = Constants.MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y.getId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid, initTxnResponse.getBody().getTxnToken(), initTxnDTO.getBody().getOrderId(), "PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode(Constants.Bank.PPBL.toString())
                .setAuthMode("USRPWD")
                .setMpin("1234")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
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
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Bank.PPBL.toString())
                .validateBankName(Constants.Bank.PPBL.toString())
                .assertAll();

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.getBody().getOrderId(),"ACQUIRING_PAY_ORDER");
        String passThroughExtendInfologs = logs.substring(logs.indexOf("passThroughExtendInfo")+24,logs.indexOf("\",\"merchantType\""));
        String decrypted=PGPHelpers.Base64Decode(passThroughExtendInfologs);
        Assertions.assertThat(decrypted).contains("\"ultimateBeneficiaryName\":\""+ultimateBeneficiaryDetails.getUltimateBeneficiaryName()+"\"");

        String esnLog = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,initTxnDTO.orderFromBody(),"ExtSN=");
        String extSnValue = esnLog.substring(esnLog.indexOf("ExtSN="), esnLog.indexOf(", OrderId=")).replace("ExtSN=", "");
        System.out.println("Esn is " + extSnValue);
        String instaproxy = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,extSnValue,"DEBIT_MONEY_FROM_CASA");
        Assertions.assertThat(instaproxy).contains("\"merchant_name\":\""+ultimateBeneficiaryDetails.getUltimateBeneficiaryName().substring(0,50)+"\"");

    }




    @Owner("Shubham Soni")
    @Feature("PGP-52619")
    @Parameters({"theme"})
    @Test(description = "Validate PPBL Account Found From bank API on Enhnacedweb_revamp")
    public void validatePPBLAccountFound(@Optional("enhancedweb_revamp") String theme) throws Exception {

        User user = userManager.getForRead(Label.ZEROWALLET);
        if(STATIC_REDIS_CLUSTER().hexists(user.ssoToken() ,"USER_ACCOUNT_DETAILS"))
        {
            STATIC_REDIS_CLUSTER().hdel(user.ssoToken(), "USER_ACCOUNT_DETAILS");
        }
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.BAJAJFN_MID_DBD, theme).
                setSSO_TOKEN(user.ssoToken()).
                setTXN_AMOUNT("1.0").build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.checkBoxPPI().unCheck();
        cashierPage.payBy(Constants.PayMode.PPBL);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.PPBL.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Constants.Bank.PPBL.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("NB")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();

        String theiaFacadeLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderDTO.getORDER_ID(),"FETCH_USER_ACCOUNT_DETAILS");
        Assertions.assertThat(theiaFacadeLogs).contains("\"responseMessage\":\"Account found\",\"status\":\"SUCCESS\"");
        Assertions.assertThat(theiaFacadeLogs).contains("\"accountStatus\":\"ACTIVE\"");
    }

    @Owner("Shubham Soni")
    @Feature("PGP-52619")
    @Parameters({"theme"})
    @Test(description = "Validate PPBL Account Not Found From bank API on Enhnacedweb_revamp")
    public void validatePPBLAccountNotFound(@Optional("enhancedweb_revamp") String theme) throws Exception {

        User user = userManager.getForRead(Label.USERWITHNAMEMORETHAN60CHARACTER);
        if(STATIC_REDIS_CLUSTER().hexists(user.ssoToken() ,"USER_ACCOUNT_DETAILS"))
        {
            STATIC_REDIS_CLUSTER().hdel(user.ssoToken(), "USER_ACCOUNT_DETAILS");
        }
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.BAJAJFN_MID_DBD, theme).
                setSSO_TOKEN(user.ssoToken()).
                setTXN_AMOUNT("1.0").build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.checkBoxPPI().unCheck();
        boolean PPBLPresent = cashierPage.checkboxPPBL().isElementPresent();
        Assertions.assertThat(PPBLPresent).isEqualTo(false);
        cashierPage.payBy(Constants.PayMode.DC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .AssertAll();
        String theiaFacadeLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderDTO.getORDER_ID(),"FETCH_USER_ACCOUNT_DETAILS");
        Assertions.assertThat(theiaFacadeLogs).contains("\"responseMessage\":\"Account not found\",\"status\":\"FAILURE\"");
    }

    @Owner("Shubham Soni")
    @Feature("PGP-52619")
    @Parameters({"theme"})
    @Test(description = "Validate PPBL Account Not Found From bank API flag off on Enhnacedweb_revamp")
    public void validatePPBLAccountFF4JFlagOFF(@Optional("enhancedweb_revamp") String theme) throws Exception {

        User user = userManager.getForRead(Label.ZEROWALLET);
        if(STATIC_REDIS_CLUSTER().hexists(user.ssoToken() ,"USER_ACCOUNT_DETAILS"))
        {
            STATIC_REDIS_CLUSTER().hdel(user.ssoToken(), "USER_ACCOUNT_DETAILS");
        }
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.HIGH_PRIORITY_SMS, theme).
                setSSO_TOKEN(user.ssoToken()).
                setTXN_AMOUNT("1.0").build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.checkBoxPPI().unCheck();
       boolean PPBLPresent = cashierPage.checkboxPPBL().isElementPresent();
        Assertions.assertThat(PPBLPresent).isEqualTo(false);
        cashierPage.payBy(Constants.PayMode.DC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .AssertAll();

        String theiaFacadeLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderDTO.getORDER_ID());
        Assertions.assertThat(theiaFacadeLogs).doesNotContain("FETCH_USER_ACCOUNT_DETAILS");
    }



    @Owner("Shubham Soni")
    @Feature("PGP-52619")
    @Parameters({"theme"})
    @Test(description = "Validate no hit of bank API on Enhnacedweb")
    public void validateSkipingPPBLForNONSSO(@Optional("enhancedweb_revamp") String theme) throws Exception {

        User user = userManager.getForRead(Label.ZEROWALLET);
        if(STATIC_REDIS_CLUSTER().hexists(user.ssoToken() ,"USER_ACCOUNT_DETAILS"))
        {
            STATIC_REDIS_CLUSTER().hdel(user.ssoToken(), "USER_ACCOUNT_DETAILS");
        }
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.BAJAJFN_MID_DBD, theme).
                setTXN_AMOUNT("1.0").build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.DC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .AssertAll();

        String theiaFacadeLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderDTO.getORDER_ID());
        Assertions.assertThat(theiaFacadeLogs).doesNotContain("FETCH_USER_ACCOUNT_DETAILS");
    }

    @Owner("Shubham Soni")
    @Feature("PGP-52619")
    @Test(description = "Validate PPBL Account Found From bank API on native")
    public void validatePPBLAccountFoundNative() throws Exception{
        User user = userManager.getForRead(Label.ZEROWALLET);
        if(STATIC_REDIS_CLUSTER().hexists(user.ssoToken() ,"USER_ACCOUNT_DETAILS"))
        {
            STATIC_REDIS_CLUSTER().hdel(user.ssoToken(), "USER_ACCOUNT_DETAILS");
        }
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.BAJAJFN_MID_DBD)
                .setTxnValue("1.00")
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOptionV2 fetchPaymentOption = new FetchPaymentOptionV2(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                Constants.MerchantType.BAJAJFN_MID_DBD.getId(),initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId())
                .setPaymentMode("PPBL")
                .setAuthMode("3D")
                .setMpin("1234")
                .setChannelId("WEB")
                .build();
        NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .AssertAll();


        String theiaFacadeLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.getBody().getOrderId(),"FETCH_USER_ACCOUNT_DETAILS");
        Assertions.assertThat(theiaFacadeLogs).contains("\"responseMessage\":\"Account found\",\"status\":\"SUCCESS\"");
        Assertions.assertThat(theiaFacadeLogs).contains("\"accountStatus\":\"ACTIVE\"");

    }


}