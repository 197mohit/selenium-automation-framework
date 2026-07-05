package scripts.api.Wallet;
import com.paytm.ServerConfigProvider;
import com.paytm.api.*;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.api.qr.GenerateQR;
import com.paytm.api.refund.SyncRefund;
import com.paytm.api.theia.ApiV1ApplyPromo;
import com.paytm.api.theia.CloseOrder;
import com.paytm.api.theia.FetchQRPaymentDetails;
import com.paytm.appconstants.Constants;
import com.paytm.appconstants.Constants.MerchantType;
import com.paytm.apphelpers.*;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.CloseOrder.CloseOrderDTO;
import com.paytm.dto.FastForwardApp.request.FastForwardAppRequest;
import com.paytm.dto.NativeDTO.FetchQRPaymentDetailsDTO.FetchQRPaymentDetailsDTO;
import com.paytm.dto.NativeDTO.InitTxn.TxnAmount;
import com.paytm.dto.NativeDTO.UpdateTransaction.UpdateTransactionDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.response.FetchPaymentOptResponseDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.PaymentOffersApplied;
import com.paytm.dto.NativeDTO.InitTxn.SimplifiedPaymentOffers;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.PromoDTO.ApplyPromoDTO.ApplyPromoDTO;
import com.paytm.dto.PromoDTO.ApplyPromoDTO.PaymentOptions;
import com.paytm.dto.TransactionStatusV1.Body;
import com.paytm.dto.TransactionStatusV1.Head;
import com.paytm.dto.TransactionStatusV1.TransactionStatusV1DTO;
import com.paytm.dto.checkoutjs.MerchantConfig;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.dto.processTransactionV1.response.ProcessTxnV1Response;
import com.paytm.framework.reporting.Reporter;
import com.paytm.pages.*;
import com.paytm.utils.merchant.merchant.util.Merchant;
import com.paytm.utils.merchant.merchant.util.Promo;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.testng.Assert;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import reactor.core.Exceptions;
import com.paytm.LocalConfig;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

import static com.paytm.appconstants.Constants.Owner.*;
import static io.restassured.RestAssured.given;


public class PG2Wallet extends PGPBaseTest {

    private static final String API_VERSION_V2 = "V2";
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
    public static String generateQRViaWallet(Constants.MerchantType merchantType)
    {
        GenerateQR generateQR = new GenerateQR(merchantType.getId(),"");
     //   JsonPath generateJson = generateQR.execute().jsonPath();
        String resp = generateQR.execute().getBody().asString().replace("\\", "").replace("\"{","{").replace("}\"","}");
        JsonPath generateJson = JsonPath.given(resp);
        Assertions.assertThat(generateJson.getString("statusCode")).isEqualTo("200");
        String qrCodeId = generateJson.getString("response.qrCodeId").replaceAll("\\p{P}", "");
        return qrCodeId;
    }
    public void verifyEsnLength(InitTxnDTO initTxnDTO, int length) throws InterruptedException {
        String grepEsn = "grep " + initTxnDTO.getBody().getOrderId() + " " + LocalConfig.INSTAPROXY_LOGS + "|grep \"ExtSN=\"";
        String extSn = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.INSTAPROXY, grepEsn);
        String extSnValue = extSn.substring(extSn.indexOf("ExtSN="), extSn.indexOf(", OrderId=")).replace("ExtSN=", "");
        System.out.println("Esn is " + extSnValue);
        Assertions.assertThat(extSnValue.length()).isEqualTo(length);
    }

    public void assertPTCCommonResponse(ProcessTxnV1Response ptcResponse, InitTxnDTO initTxnDTO) {
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getBANKTXNID()).isNotNull();
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getCURRENCY()).isEqualTo("INR");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getMID()).isEqualTo(initTxnDTO.getBody().getMid());
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getORDERID()).isEqualTo(initTxnDTO.getBody().getOrderId());
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getTXNAMOUNT()).isEqualTo(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.getBody().getTxnAmount().getValue()));
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getTXNDATE()).isNotNull();
    }

    public void assertTheiaTxnStatusCommonResponse(Response theiaTxnStatusResponse, InitTxnDTO initTxnDTO, Constants.ResponseCode responseCode) {
        Assertions.assertThat(theiaTxnStatusResponse.jsonPath().getString("body.txnInfo.CURRENCY")).isEqualTo("INR");
        Assertions.assertThat(theiaTxnStatusResponse.jsonPath().getString("body.txnInfo.MID")).isEqualTo(initTxnDTO.getBody().getMid());
        Assertions.assertThat(theiaTxnStatusResponse.jsonPath().getString("body.txnInfo.ORDERID")).isEqualTo(initTxnDTO.getBody().getOrderId());
        Assertions.assertThat(theiaTxnStatusResponse.jsonPath().getString("body.txnInfo.RESPCODE")).isEqualTo(responseCode.getRespCode());
        Assertions.assertThat(theiaTxnStatusResponse.jsonPath().getString("body.txnInfo.RESPMSG")).isEqualTo(responseCode.getRespMsg());
        Assertions.assertThat(theiaTxnStatusResponse.jsonPath().getString("body.txnInfo.TXNAMOUNT")).isEqualTo(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.getBody().getTxnAmount().getValue()));
    }

    public void validateAgainRetrygivesorderispaid(String orderId) throws InterruptedException {
        String grepAgainRetrygivesorederis_paid= "grep " + orderId + " " + LocalConfig.THEIA_FACADE_LOGS +
                " | grep \"ACQUIRING_PAY_ORDER\" | grep \"RESPONSE\"";
        String grepRetryagaingivesorderispaid = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepAgainRetrygivesorederis_paid);
        Assertions.assertThat(grepRetryagaingivesorderispaid)
                .contains("\"resultCode\":\"ORDER_IS_PAID\"");
    }

    @Owner(SAGAR)
    @Feature("PG2-3596")
    @Test(description = "Validate Success Wallet transaction for PG2 MID routed through PG2")
    public void validateSuccessWalletTxnPG2MID() throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        WalletHelpers.modifyBalance(user,10.0);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.UPIPUSHPG2).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "BALANCE", "false");
        String orderID= initTxnDTO.orderFromBody();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                MerchantType.UPIPUSHPG2.getId(), initTxnResponse.getBody().getTxnToken(), initTxnDTO.getBody().getOrderId(), "")
                .setPaymentMode("BALANCE")
                .setAuthMode("USRPWD")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse, initTxnDTO);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getPAYMENTMODE()).isEqualTo("PPI");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPCODE()).isEqualTo("01");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getSTATUS()).isEqualTo(Constants.TXNSTATUS.TXN_SUCCESS.toString());

        verifyEsnLength(initTxnDTO, 19);
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("PPI")
                .validateGatewayName(Constants.Gateway.WALLET.toString())
                .validateMid(MerchantType.UPIPUSHPG2.getId())
                .AssertAll();
        CommonHelpers.verifyPaymentNotify(orderID);
    }

    @Owner(SAGAR)
    @Feature("PG2-3596")
    @Test(description = "Validate Failed wallet transaction for PG2 MID routed through PG2")
    public void validateFailedWalletTxnPG2MID() throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        WalletHelpers.modifyBalance(user,10.0);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.UPIPUSHPG2).setTxnValue("200000").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "BALANCE", "false");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                MerchantType.UPIPUSHPG2.getId(), initTxnResponse.getBody().getTxnToken(), initTxnDTO.getBody().getOrderId(), "")
                .setPaymentMode("BALANCE")
                .setAuthMode("USRPWD")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse, initTxnDTO);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("F");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0001");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo(Constants.ResponseCode.WALLET_INSUFFICIENT_BALANCE.getRespMsg());
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getPAYMENTMODE()).isEqualTo("PPI");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPCODE()).isEqualTo(Constants.ResponseCode.WALLET_INSUFFICIENT_BALANCE.getRespCode());
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPMSG()).isEqualTo(Constants.ResponseCode.WALLET_INSUFFICIENT_BALANCE.getRespMsg());
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getSTATUS()).isEqualTo(Constants.TXNSTATUS.TXN_FAILURE.toString());

        verifyEsnLength(initTxnDTO, 19);
        CloseOrderDTO closeOrderDTO = new CloseOrderDTO();
        closeOrderDTO.setHead(new com.paytm.dto.CloseOrder.Head().setChannelId("WAP").setVersion("v1").setSigature(""))
                .setBody(new com.paytm.dto.CloseOrder.Body().setIsForceClose(true).setOrderId(initTxnDTO.getBody().getOrderId()).setMid(MerchantType.UPIPUSHPG2.getId()));
        CloseOrderAPI closeOrderAPI = new CloseOrderAPI(closeOrderDTO);
        closeOrderAPI.execute();
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_FAILURE.toString())
                .validateRespCode(Constants.ResponseCode.WALLET_INSUFFICIENT_BALANCE.getRespCode())
                .validateRespMsg(Constants.ResponseCode.WALLET_INSUFFICIENT_BALANCE.getRespMsg())
                .validatePaymentMode("PPI")
                .validateGatewayName(Constants.Gateway.WALLET.toString())
                .validateMid(MerchantType.UPIPUSHPG2.getId())
                .AssertAll();
    }

    @Owner(SAGAR)
    @Feature("PG2-4088")
    @Test(description = "Validate Wallet PG2 transaction full Async Refund")
    public void validateWalletPG2FullAsyncRefund() throws Exception{
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        WalletHelpers.modifyBalance(user,10.0);
        String mid = MerchantType.UPIPUSHPG2.getId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.UPIPUSHPG2).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "BALANCE", "false");
        String orderID= initTxnDTO.orderFromBody();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"")
                .setPaymentMode("BALANCE")
                .setAuthMode("USRPWD")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse, initTxnDTO);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getPAYMENTMODE()).isEqualTo("PPI");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPCODE()).isEqualTo("01");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getSTATUS()).isEqualTo(Constants.TXNSTATUS.TXN_SUCCESS.toString());

        verifyEsnLength(initTxnDTO ,19);
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("PPI")
                .validateGatewayName(Constants.Gateway.WALLET.toString())
                .validateMid(mid)
                .AssertAll();

        SyncRefund syncRefund = new SyncRefund();
        Response asyncRefundResp = given().spec(syncRefund.reqSpecAsyncRefund(MerchantType.UPIPUSHPG2, initTxnDTO.txnAmountFromBody(), initTxnDTO.getBody().getOrderId(),
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
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.txnId")).isEqualTo(ptcResponse.getBody().getTxnInfo().getTXNID());
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.refundAmount")).isEqualTo(initTxnDTO.txnAmountFromBody());
        PGPHelpers.assertRefundSuccessNotifyPresence(orderID);
    }

    @Owner(SAGAR)
    @Feature("PG2-4088")
    @Test(description = "Validate Wallet PG2 transaction partial Async Refund")
    public void validateWalletPG2PartialAsyncRefund() throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        WalletHelpers.modifyBalance(user,10.0);
        String mid = MerchantType.UPIPUSHPG2.getId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.UPIPUSHPG2).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "BALANCE", "false");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid, initTxnResponse.getBody().getTxnToken(), initTxnDTO.getBody().getOrderId(), "")
                .setPaymentMode("BALANCE")
                .setAuthMode("USRPWD")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse, initTxnDTO);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getPAYMENTMODE()).isEqualTo("PPI");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPCODE()).isEqualTo("01");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getSTATUS()).isEqualTo(Constants.TXNSTATUS.TXN_SUCCESS.toString());

        verifyEsnLength(initTxnDTO, 19);
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("PPI")
                .validateGatewayName(Constants.Gateway.WALLET.toString())
                .validateMid(mid)
                .AssertAll();

        SyncRefund syncRefund = new SyncRefund();
        String refundAmount = "1";
        Response asyncRefundResp = given().spec(syncRefund.reqSpecAsyncRefund(MerchantType.UPIPUSHPG2, refundAmount, initTxnDTO.getBody().getOrderId(),
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
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.txnId")).isEqualTo(ptcResponse.getBody().getTxnInfo().getTXNID());
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.refundAmount")).isEqualTo(refundAmount);
    }
    @Owner(PRAMOD_KUMAR)
    @Feature("PG2-4194")
    @Test(description = "Validate Success CO Then Pay full traffic Wallet transaction for PG2 MID routed through PG2")
    public void validateSuccessCOThenPayFullTFullTrafficWalletTxnPG2MID() throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        WalletHelpers.modifyBalance(user,10.0);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "BALANCE", "false");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y.getId(), initTxnResponse.getBody().getTxnToken(), initTxnDTO.getBody().getOrderId(), "")
                .setPaymentMode("BALANCE")
                .setAuthMode("USRPWD")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse, initTxnDTO);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getPAYMENTMODE()).isEqualTo("PPI");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPCODE()).isEqualTo("01");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getSTATUS()).isEqualTo(Constants.TXNSTATUS.TXN_SUCCESS.toString());

        verifyEsnLength(initTxnDTO, 19);
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("PPI")
                .validateGatewayName(Constants.Gateway.WALLET.toString())
                .validateMid(MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y.getId())
                .AssertAll();
    }

    @Owner(PRAMOD_KUMAR)
    @Feature("PG2-4194")
    @Test(description = "Validate Failed CO then pay full traffic wallet transaction for PG2 MID routed through PG2")
    public void validateFailedFullTrafficCOThenPayWalletTxnPG2MID() throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        WalletHelpers.modifyBalance(user,10.0);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y).setTxnValue("21").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "BALANCE", "false");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y.getId(), initTxnResponse.getBody().getTxnToken(), initTxnDTO.getBody().getOrderId(), "")
                .setPaymentMode("BALANCE")
                .setAuthMode("USRPWD")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse, initTxnDTO);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("F");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0001");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo(Constants.ResponseCode.WALLET_INSUFFICIENT_BALANCE.getRespMsg());
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getPAYMENTMODE()).isEqualTo("PPI");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPCODE()).isEqualTo(Constants.ResponseCode.WALLET_INSUFFICIENT_BALANCE.getRespCode());
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPMSG()).isEqualTo(Constants.ResponseCode.WALLET_INSUFFICIENT_BALANCE.getRespMsg());
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getSTATUS()).isEqualTo(Constants.TXNSTATUS.TXN_FAILURE.toString());

        verifyEsnLength(initTxnDTO, 19);
        CloseOrderDTO closeOrderDTO = new CloseOrderDTO();
        closeOrderDTO.setHead(new com.paytm.dto.CloseOrder.Head().setChannelId("WAP").setVersion("v1").setSigature(""))
                .setBody(new com.paytm.dto.CloseOrder.Body().setIsForceClose(true).setOrderId(initTxnDTO.getBody().getOrderId()).setMid(MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y.getId()));
        CloseOrderAPI closeOrderAPI = new CloseOrderAPI(closeOrderDTO);
        closeOrderAPI.execute();
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_FAILURE.toString())
                .validateRespCode(Constants.ResponseCode.WALLET_INSUFFICIENT_BALANCE.getRespCode())
                .validateRespMsg(Constants.ResponseCode.WALLET_INSUFFICIENT_BALANCE.getRespMsg())
                .validatePaymentMode("PPI")
                .validateGatewayName(Constants.Gateway.WALLET.toString())
                .validateMid(MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y.getId())
                .AssertAll();
    }


    @Owner(VIKASH_VERMA)
    @Feature("PG2-4120")
    @Test(description = "Validate CO then Pay Wallet PG2 transaction full Async Refund")
    public void validateFullTrafficYSuccessCoThenPayWalletPG2FullAsyncRefund() throws Exception{
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        WalletHelpers.modifyBalance(user,10.0);
        String mid = MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y.getId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "BALANCE", "false");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"")
                .setPaymentMode("BALANCE")
                .setAuthMode("USRPWD")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse, initTxnDTO);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getPAYMENTMODE()).isEqualTo("PPI");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPCODE()).isEqualTo("01");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getSTATUS()).isEqualTo(Constants.TXNSTATUS.TXN_SUCCESS.toString());

        verifyEsnLength(initTxnDTO ,19);
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("PPI")
                .validateGatewayName(Constants.Gateway.WALLET.toString())
                .validateMid(mid)
                .AssertAll();

        SyncRefund syncRefund = new SyncRefund();
        Response asyncRefundResp = given().spec(syncRefund.reqSpecAsyncRefund(MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y, initTxnDTO.txnAmountFromBody(), initTxnDTO.getBody().getOrderId(),
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
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.txnId")).isEqualTo(ptcResponse.getBody().getTxnInfo().getTXNID());
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.refundAmount")).isEqualTo(initTxnDTO.txnAmountFromBody());
    }

    @Owner(VIKASH_VERMA)
    @Feature("PG2-4120")
    @Test(description = "Validate CO then Pay Wallet PG2 transaction partial Async Refund")
    public void validateFullTrafficYSuccessCoThenPayWalletPG2partialAsyncRefund() throws Exception{
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        WalletHelpers.modifyBalance(user,10.0);
        String mid = MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y.getId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y).setTxnValue("5").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "BALANCE", "false");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"")
                .setPaymentMode("BALANCE")
                .setAuthMode("USRPWD")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse, initTxnDTO);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getPAYMENTMODE()).isEqualTo("PPI");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPCODE()).isEqualTo("01");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getSTATUS()).isEqualTo(Constants.TXNSTATUS.TXN_SUCCESS.toString());

        verifyEsnLength(initTxnDTO ,19);
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("PPI")
                .validateGatewayName(Constants.Gateway.WALLET.toString())
                .validateMid(mid)
                .AssertAll();

        SyncRefund syncRefund = new SyncRefund();
        String refundAmount = "2";
        Response asyncRefundResp = given().spec(syncRefund.reqSpecAsyncRefund(MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y, refundAmount, initTxnDTO.getBody().getOrderId(),
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
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.txnId")).isEqualTo(ptcResponse.getBody().getTxnInfo().getTXNID());
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.refundAmount")).isEqualTo(refundAmount);
    }

    @Owner(SAGAR)
    @Feature("PG2-2863")
    @Test(description = "Validate that if user id is not present in FF4J Flag theia.payment.adapter.feature then txn should be routed to P+")
    public void validateUserNotPresentInFF4JWalletTxnRoutedToPPlus() throws Exception{
        User user = userManager.getForWrite(Label.PG2WALLETUSER);
        WalletHelpers.modifyBalance(user,10.0);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.UPIPUSHPG2).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "BALANCE", "false");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                MerchantType.UPIPUSHPG2.getId(),initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"")
                .setPaymentMode("BALANCE")
                .setAuthMode("USRPWD")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse, initTxnDTO);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPCODE()).isEqualTo("01");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getSTATUS()).isEqualTo(Constants.TXNSTATUS.TXN_SUCCESS.toString());

        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getTXNID()).contains("1212");
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("PPI")
                .validateGatewayName(Constants.Gateway.WALLET.toString())
                .validateMid(MerchantType.UPIPUSHPG2.getId())
                .AssertAll();
    }
    @Owner(PRAMOD_KUMAR)
    @Feature("PG2-4499")
    @Test(description = "Validate full traffic Success CO then pay wallet Internal Order Modify txn through PG2")
    public void validateFullTrafficYSuccessCOThenPayWalletInternalOrderModifyTxnPG2MID() throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        WalletHelpers.modifyBalance(user,10.0);
        String mid = MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y.getId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y).setTxnValue("5.6").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "BALANCE", "false");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"")
                .setPaymentMode("BALANCE")
                .setAuthMode("USRPWD")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse, initTxnDTO);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getPAYMENTMODE()).isEqualTo("PPI");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPCODE()).isEqualTo("01");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getSTATUS()).isEqualTo(Constants.TXNSTATUS.TXN_SUCCESS.toString());


        String orderId = ptcResponse.getBody().getTxnInfo().getORDERID();
        verifyEsnLength(initTxnDTO ,19);
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("PPI")
                .validateGatewayName(Constants.Gateway.WALLET.toString())
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

        // mock has been removed friom risk domain so commenting these validation
        // String riskInfo = response.jsonPath().getString("body.txnInfo.riskInfo");
        // verifyOrderModifyParam(orderId, riskInfo);
        //validateAcquiringOrderMerchantTransIDLogs(orderId, riskInfo);
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PG2-4580")
    @Parameters({"theme"})
    @Test(description = "Validate Full Traffic Co Then Pay Success Wallet app invoke Txn")
    public void validatePG2COThenPayFullTrafficYWalletSuccessInvokeAppTxn(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        WalletHelpers.modifyBalance(user, 100.0);
        Constants.MerchantType merchant = Constants.MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(merchant,initTxnDTO.getBody().getOrderId(),txnToken).build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.WALLET);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("PPI")
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_SUCCESS.getRespMsg())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.WALLET.toString())
                .validateBankName(Constants.Gateway.WALLET.toString())
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
                .validatePaymentMode("PPI")
                .validateGatewayName(Constants.Gateway.WALLET.toString())
                .validateMid(initTxnDTO.getBody().getMid())
                .AssertAll();
    }

    @Owner(VIKASH_VERMA)
    @Feature("PG2-4571")
    @Test(description = "PG2 COP Initiate Txn is executed with Amt 2 rs , wallet failed TXn is done PTC is executed again with Postpaid")
    public void validateFullTrafficCOPFailedwalletTxnretrywithpostpaid() throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        WalletHelpers.modifyBalance(user, 1.0);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.PG2_COP_FULL_TRAFFIC_Y_Retry).setTxnValue("2").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "BALANCE", "false");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                MerchantType.PG2_COP_FULL_TRAFFIC_Y_Retry.getId(), initTxnResponse.getBody().getTxnToken(), initTxnDTO.getBody().getOrderId(), "")
                .setPaymentMode("BALANCE")
                .setAuthMode("USRPWD")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse, initTxnDTO);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("F");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0001");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo(Constants.ResponseCode.WALLET_INSUFFICIENT_BALANCE.getRespMsg());
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getPAYMENTMODE()).isEqualTo("PPI");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPCODE()).isEqualTo(Constants.ResponseCode.WALLET_INSUFFICIENT_BALANCE.getRespCode());
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPMSG()).isEqualTo(Constants.ResponseCode.WALLET_INSUFFICIENT_BALANCE.getRespMsg());
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getSTATUS()).isEqualTo(Constants.TXNSTATUS.TXN_FAILURE.toString());
        verifyEsnLength(initTxnDTO, 19);
        ProcessTxnV1Request processTxnV1Request1 = new ProcessTxnV1Request.Builder(
                MerchantType.PG2_COP_FULL_TRAFFIC_Y_Retry.getId(), initTxnResponse.getBody().getTxnToken(), initTxnDTO.getBody().getOrderId(), "")
                .setPaymentMode("PAYTM_DIGITAL_CREDIT")
                .setAuthMode("3D")
                .setChannelId("WEB")
                .build();
        ProcessTxnV1Response ptcResponse1 = NativeHelpers.executeProcessTxnV1(processTxnV1Request1);
        assertPTCCommonResponse(ptcResponse1, initTxnDTO);
        Assertions.assertThat(ptcResponse1.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse1.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse1.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        Assertions.assertThat(ptcResponse1.getBody().getTxnInfo().getPAYMENTMODE()).isEqualTo("Paytm Postpaid");
        Assertions.assertThat(ptcResponse1.getBody().getTxnInfo().getRESPCODE()).isEqualTo("01");
        Assertions.assertThat(ptcResponse1.getBody().getTxnInfo().getSTATUS()).isEqualTo(Constants.TXNSTATUS.TXN_SUCCESS.toString());
        verifyEsnLength(initTxnDTO, 19);
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("Paytm Postpaid")
                .validateGatewayName(Constants.Gateway.PAYTMCC.toString())
                .validateMid(MerchantType.PG2_COP_FULL_TRAFFIC_Y_Retry.getId())
                .AssertAll();
        TransactionStatusV1DTO transactionStatusV1DTO = new TransactionStatusV1DTO();
        transactionStatusV1DTO.setBody(new Body().setIsCallbackUrlRequired(true).setIsFinalTxnStatusRequired(false).setMid(MerchantType.PG2_COP_FULL_TRAFFIC_Y_Retry.getId()).setOrderId(initTxnDTO.getBody().getOrderId()))
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
    @Feature("PG2-4620")
    @Parameters({"theme"})
    @Test(description = "Validate Full Traffic Co Then Pay Success Wallet Checkout JS Page Txn")
    public void validatePG2COThenPayFullTrafficYWalletSuccessCheckoutJSPageTxn(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        WalletHelpers.modifyBalance(user, 100.0);
        Constants.MerchantType merchant = Constants.MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        MerchantConfig config = checkoutJsPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutJsPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.WALLET);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("PPI")
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_SUCCESS.getRespMsg())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.WALLET.toString())
                .validateBankName(Constants.Gateway.WALLET.toString())
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
                .validatePaymentMode("PPI")
                .validateGatewayName(Constants.Gateway.WALLET.toString())
                .validateMid(initTxnDTO.getBody().getMid())
                .AssertAll();

    }

    @Owner(VIKASH_VERMA)
    @Feature("PG2-4725")
    @Test(description = "PG2 CO then pay Initiate Txn is executed with Amt 2 rs , wallet failed TXn is done PTC is executed again with Postpaid")
    public void validateFullTrafficCOThenpayFailedwalletTxnretrywithpostpaid() throws Exception {
        User user = userManager.getForWrite(Label.UPIPUSHPG2);
        WalletHelpers.modifyBalance(user, 1.0);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y_Retry).setTxnValue("2").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "BALANCE", "false");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y_Retry.getId(), initTxnResponse.getBody().getTxnToken(), initTxnDTO.getBody().getOrderId(), "")
                .setPaymentMode("BALANCE")
                .setAuthMode("USRPWD")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse, initTxnDTO);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("F");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0001");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo(Constants.ResponseCode.WALLET_INSUFFICIENT_BALANCE.getRespMsg());
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getPAYMENTMODE()).isEqualTo("PPI");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPCODE()).isEqualTo(Constants.ResponseCode.WALLET_INSUFFICIENT_BALANCE.getRespCode());
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPMSG()).isEqualTo(Constants.ResponseCode.WALLET_INSUFFICIENT_BALANCE.getRespMsg());
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getSTATUS()).isEqualTo(Constants.TXNSTATUS.TXN_FAILURE.toString());
        verifyEsnLength(initTxnDTO, 19);
        ProcessTxnV1Request processTxnV1Request1 = new ProcessTxnV1Request.Builder(
                MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y_Retry.getId(), initTxnResponse.getBody().getTxnToken(), initTxnDTO.getBody().getOrderId(), "")
                .setPaymentMode("PAYTM_DIGITAL_CREDIT")
                .setAuthMode("3D")
                .setChannelId("WEB")
                .build();
        ProcessTxnV1Response ptcResponse1 = NativeHelpers.executeProcessTxnV1(processTxnV1Request1);
        assertPTCCommonResponse(ptcResponse1, initTxnDTO);
        Assertions.assertThat(ptcResponse1.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse1.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse1.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        Assertions.assertThat(ptcResponse1.getBody().getTxnInfo().getPAYMENTMODE()).isEqualTo("Paytm Postpaid");
        Assertions.assertThat(ptcResponse1.getBody().getTxnInfo().getRESPCODE()).isEqualTo("01");
        Assertions.assertThat(ptcResponse1.getBody().getTxnInfo().getSTATUS()).isEqualTo(Constants.TXNSTATUS.TXN_SUCCESS.toString());
        verifyEsnLength(initTxnDTO, 19);
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("Paytm Postpaid")
                .validateGatewayName(Constants.Gateway.PAYTMCC.toString())
                .validateMid(MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y_Retry.getId())
                .AssertAll();
        TransactionStatusV1DTO transactionStatusV1DTO = new TransactionStatusV1DTO();
        transactionStatusV1DTO.setBody(new Body().setIsCallbackUrlRequired(true).setIsFinalTxnStatusRequired(false).setMid(MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y_Retry.getId()).setOrderId(initTxnDTO.getBody().getOrderId()))
                .setHead(new Head().setTokenType("TXN_TOKEN").setToken(txnToken));
        TransactionStatusV1API transactionStatusV1API = new TransactionStatusV1API(transactionStatusV1DTO);
        Response theiaTxnStatusResponse = transactionStatusV1API.execute();
        assertTheiaTxnStatusCommonResponse(theiaTxnStatusResponse, initTxnDTO, Constants.ResponseCode.TXN_SUCCESS);
        Assertions.assertThat(theiaTxnStatusResponse.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(theiaTxnStatusResponse.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo(Constants.ResponseCode.TXN_SUCCESS.getRespCode());
        Assertions.assertThat(theiaTxnStatusResponse.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo(Constants.ResponseCode.TXN_SUCCESS.getRespMsg());
        Assertions.assertThat(theiaTxnStatusResponse.jsonPath().getString("body.txnInfo.STATUS")).isEqualTo(Constants.TXNSTATUS.TXN_SUCCESS.toString());
    }


    @Owner(VIKASH_VERMA)
    @Feature("PG2-4832")
    @Test(description = "PG2 CO then Pay Initiate Txn is executed with Amt 1, Wallet Txn is done PTC is executed again with Wallet")
    public void validateSuccessWalletTxnagainretrywithwallet() throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        WalletHelpers.modifyBalance(user,10.0);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y_Retry).setTxnValue("1").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "BALANCE", "false");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y_Retry.getId(), initTxnResponse.getBody().getTxnToken(), initTxnDTO.getBody().getOrderId(), "")
                .setPaymentMode("BALANCE")
                .setAuthMode("USRPWD")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse, initTxnDTO);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getPAYMENTMODE()).isEqualTo("PPI");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPCODE()).isEqualTo("01");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getSTATUS()).isEqualTo(Constants.TXNSTATUS.TXN_SUCCESS.toString());

        verifyEsnLength(initTxnDTO, 19);

        Validate_FetchPayInstrument(txnToken, initTxnDTO, "BALANCE", "false");
        ProcessTxnV1Request processTxnV1Request1 = new ProcessTxnV1Request.Builder(
                MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y_Retry.getId(), initTxnResponse.getBody().getTxnToken(), initTxnDTO.getBody().getOrderId(), "")
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
        verifyEsnLength(initTxnDTO, 19);
        String orderId=ptcResponse.getBody().getTxnInfo().getORDERID();
        validateAgainRetrygivesorderispaid(orderId);
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("PPI")
                .validateGatewayName(Constants.Gateway.WALLET.toString())
                .validateMid(MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y_Retry.getId())
                .AssertAll();
    }
    @Owner(PRAMOD_KUMAR)
    @Feature("PG2-4562")
    @Test(description = "Validate Success CO Then Pay full traffic Simplified Promo Wallet transaction for PG2 MID routed through PG2")
    public void validateCoThenPaySimplifiedPromoOrderModifyWalletTxnPG2MID() throws Exception {
        User user = userManager.getForWrite(Label.PG2POSTPAIDUSER);
        WalletHelpers.modifyBalance(user,100.0);
        String mid = MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y.getId();
        Promo promocode = new Promo(true);
        new Merchant(mid, true ).getPromos().add(promocode);
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode("discount").setApplyAvailablePromo("true").setValidatePromo("true");

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y,simplifiedPaymentOffers).setTxnValue("100").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "BALANCE", "false");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y.getId(), initTxnResponse.getBody().getTxnToken(), initTxnDTO.getBody().getOrderId(), "")
                .setPaymentMode("BALANCE")
                .setAuthMode("USRPWD")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);

        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getPAYMENTMODE()).isEqualTo("PPI");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPCODE()).isEqualTo("01");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getSTATUS()).isEqualTo(Constants.TXNSTATUS.TXN_SUCCESS.toString());
        String discountedTxnAmmount= ptcResponse.getBody().getTxnInfo().getTXNAMOUNT();
        verifyEsnLength(initTxnDTO, 19);
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(discountedTxnAmmount)
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("PPI")
                .validateGatewayName(Constants.Gateway.WALLET.toString())
                .validateMid(MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y.getId())
                .AssertAll();
        String orderID= ptcResponse.getBody().getTxnInfo().getORDERID();
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
//      acquiring order modify logs not come now
//      validateAcquiringOrderModify(orderID);
    }
    @Owner(PRAMOD_KUMAR)
    @Feature("PG2-4800")
    @Test(description = "Validate Full Traffic COP Success Wallet Order Modify transaction for PG2 MID routed through PG2")
    public void validateCOPSuccessOrderModifyWalletTxnPG2MID() throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        WalletHelpers.modifyBalance(user, 100.0);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.PG2_COP_Full_Traffic_NoRENTPref).setTxnValue("5").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "BALANCE", "false");
        String mid = MerchantType.PG2_COP_Full_Traffic_NoRENTPref.getId();

        Double updatedTxnAmount = Double.parseDouble(initTxnDTO.txnAmountFromBody()) + 1;
        TxnAmount txnAmount = initTxnDTO.getBody().getTxnAmount();
        txnAmount.setValue(String.valueOf(updatedTxnAmount));
        UpdateTransactionDTO updateTransactionDTO = new UpdateTransactionDTO();
        updateTransactionDTO.setHead(new com.paytm.dto.NativeDTO.UpdateTransaction.Head(txnToken))
                .setBody(new com.paytm.dto.NativeDTO.UpdateTransaction.Body().setExtendInfo(initTxnDTO.getBody().getExtendInfo())
                        .setGoods(Arrays.asList(initTxnDTO.getBody().getGoods().clone()))
                        .setShippingInfo(initTxnDTO.getBody().getShippingInfo())
                        .setTxnAmount(txnAmount));
        updateTransactionDTO.setChecksum(MerchantType.PG2_COP_Full_Traffic_NoRENTPref.getKey());
        UpdateTransaction updateTransaction = new UpdateTransaction(updateTransactionDTO, initTxnDTO.getBody().getMid(), initTxnDTO.orderFromBody());
        updateTransaction.execute();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid, initTxnResponse.getBody().getTxnToken(), initTxnDTO.getBody().getOrderId(), "")
                .setPaymentMode("BALANCE")
                .setAuthMode("3D")
                .setChannelId("WEB")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse, initTxnDTO);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getPAYMENTMODE()).isEqualTo("PPI");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPCODE()).isEqualTo("01");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getTXNAMOUNT().equals(txnAmount));
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getSTATUS()).isEqualTo(Constants.TXNSTATUS.TXN_SUCCESS.toString());
        verifyEsnLength(initTxnDTO, 19);

        String modifiedTxnAmount=ptcResponse.getBody().getTxnInfo().getTXNAMOUNT();
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(modifiedTxnAmount)
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("PPI")
                .validateGatewayName(Constants.Gateway.WALLET.toString())
                .validateMid(mid)
                .AssertAll();
        String orderID= ptcResponse.getBody().getTxnInfo().getORDERID();
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
        Assertions.assertThat(response.jsonPath().getString("body.txnInfo.TXNAMOUNT").equals(txnAmount));

    }
    @Owner(PRAMOD_KUMAR)
    @Feature("PG2-4800")
    @Test(description = "Validate Full Traffic CO then pay  Success Wallet Order Modify transaction for PG2 MID routed through PG2")
    public void validateCOThenPaySuccessOrderModifyWalletTxnPG2MID() throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        WalletHelpers.modifyBalance(user, 100.0);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.PG2_CO_Then_Pay_Full_Traffic_NoRentPref).setTxnValue("5").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "BALANCE", "false");
        String mid = MerchantType.PG2_CO_Then_Pay_Full_Traffic_NoRentPref.getId();

        Double updatedTxnAmount = Double.parseDouble(initTxnDTO.txnAmountFromBody()) + 1;
        TxnAmount txnAmount = initTxnDTO.getBody().getTxnAmount();
        txnAmount.setValue(String.valueOf(updatedTxnAmount));
        UpdateTransactionDTO updateTransactionDTO = new UpdateTransactionDTO();
        updateTransactionDTO.setHead(new com.paytm.dto.NativeDTO.UpdateTransaction.Head(txnToken))
                .setBody(new com.paytm.dto.NativeDTO.UpdateTransaction.Body().setExtendInfo(initTxnDTO.getBody().getExtendInfo())
                        .setGoods(Arrays.asList(initTxnDTO.getBody().getGoods().clone()))
                        .setShippingInfo(initTxnDTO.getBody().getShippingInfo())
                        .setTxnAmount(txnAmount));
        updateTransactionDTO.setChecksum(MerchantType.PG2_CO_Then_Pay_Full_Traffic_NoRentPref.getKey());
        UpdateTransaction updateTransaction = new UpdateTransaction(updateTransactionDTO, initTxnDTO.getBody().getMid(), initTxnDTO.orderFromBody());
        updateTransaction.execute();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid, initTxnResponse.getBody().getTxnToken(), initTxnDTO.getBody().getOrderId(), "")
                .setPaymentMode("BALANCE")
                .setAuthMode("3D")
                .setChannelId("WEB")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse, initTxnDTO);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getPAYMENTMODE()).isEqualTo("PPI");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPCODE()).isEqualTo("01");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getTXNAMOUNT().equals(txnAmount));
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getSTATUS()).isEqualTo(Constants.TXNSTATUS.TXN_SUCCESS.toString());
        verifyEsnLength(initTxnDTO, 19);

        String modifiedTxnAmount=ptcResponse.getBody().getTxnInfo().getTXNAMOUNT();
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(modifiedTxnAmount)
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("PPI")
                .validateGatewayName(Constants.Gateway.WALLET.toString())
                .validateMid(mid)
                .AssertAll();
        String orderID= ptcResponse.getBody().getTxnInfo().getORDERID();
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
        Assertions.assertThat(response.jsonPath().getString("body.txnInfo.TXNAMOUNT").equals(txnAmount));
//        validateAcquiringOrderModify(orderID);
    }
    @Owner(PRAMOD_KUMAR)
    @Feature("PG2-4853")
    @Test(description = "Validate full traffic Success CO then pay wallet Internal Order Modify txn through PG2")
    public void validateFullTrafficYSuccessCOPWalletInternalOrderModifyTxnPG2MID() throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        WalletHelpers.modifyBalance(user,10.0);
        String mid = MerchantType.PG2_COP_FULL_TRAFFIC_Y.getId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.PG2_COP_FULL_TRAFFIC_Y).setTxnValue("5.6").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "BALANCE", "false");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"")
                .setPaymentMode("BALANCE")
                .setAuthMode("USRPWD")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse, initTxnDTO);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getPAYMENTMODE()).isEqualTo("PPI");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPCODE()).isEqualTo("01");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getSTATUS()).isEqualTo(Constants.TXNSTATUS.TXN_SUCCESS.toString());
        String orderId = ptcResponse.getBody().getTxnInfo().getORDERID();
        verifyEsnLength(initTxnDTO ,19);
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("PPI")
                .validateGatewayName(Constants.Gateway.WALLET.toString())
                .validateMid(mid)
                .AssertAll();
//       order modify logs do not come now
//       validateAcquiringOrderModify(orderId);
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
        //  mock has been removed friom risk domain so commenting these validation
        //String riskInfo = response.jsonPath().getString("body.txnInfo.riskInfo");
        //verifyOrderModifyParam(orderId, riskInfo);
        //validateAcquiringOrderMerchantTransIDLogs(orderId, riskInfo);
    }
    @Owner(PRAMOD_KUMAR)
    @Feature("PG2-4590")
    @Test(description = "Validate PG2_ENABLED Co Then Pay Success Wallet transaction for PG2 MID routed through P+")
    public void validatePG2EnabledCoThenPaySuccessWalletTxnPG2MID() throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        WalletHelpers.modifyBalance(user,10.0);
        Constants.MerchantType merchant = Constants.MerchantType.PG2_ENABLED_CO_THEN_PAY;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "BALANCE", "false");
        String mid = merchant.getId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid, initTxnResponse.getBody().getTxnToken(), initTxnDTO.getBody().getOrderId(), "")
                .setPaymentMode("BALANCE")
                .setAuthMode("USRPWD")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse, initTxnDTO);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getPAYMENTMODE()).isEqualTo("PPI");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPCODE()).isEqualTo("01");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getSTATUS()).isEqualTo(Constants.TXNSTATUS.TXN_SUCCESS.toString());
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getTXNID()).contains("1212");
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("PPI")
                .validateGatewayName(Constants.Gateway.WALLET.toString())
                .validateMid(mid)
                .AssertAll();
    }

    @Owner(PRAMOD_KUMAR)
    @Feature("PG2-4590")
    @Test(description = "Validate PG2_ENABLED Co Then Pay Fail Wallet transaction for PG2 MID routed through P+")
    public void validatePG2EnabledCoThenPayFailWalletTxnPG2MID() throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        WalletHelpers.modifyBalance(user, 1.0);
        Constants.MerchantType merchant = Constants.MerchantType.PG2_ENABLED_CO_THEN_PAY;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue("2").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "BALANCE", "false");
        String mid = merchant.getId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid, initTxnResponse.getBody().getTxnToken(), initTxnDTO.getBody().getOrderId(), "")
                .setPaymentMode("BALANCE")
                .setAuthMode("USRPWD")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse, initTxnDTO);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("F");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0001");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo(Constants.ResponseCode.WALLET_INSUFFICIENT_BALANCE.getRespMsg());
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getPAYMENTMODE()).isEqualTo("PPI");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPCODE()).isEqualTo(Constants.ResponseCode.WALLET_INSUFFICIENT_BALANCE.getRespCode());
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPMSG()).isEqualTo(Constants.ResponseCode.WALLET_INSUFFICIENT_BALANCE.getRespMsg());
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getSTATUS()).isEqualTo(Constants.TXNSTATUS.TXN_FAILURE.toString());
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getTXNID()).contains("1212");
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
                .validateRespCode(Constants.ResponseCode.WALLET_INSUFFICIENT_BALANCE.getRespCode())
                .validateRespMsg(Constants.ResponseCode.WALLET_INSUFFICIENT_BALANCE.getRespMsg())
                .validatePaymentMode("PPI")
                .validateGatewayName(Constants.Gateway.WALLET.toString())
                .validateMid(mid)
                .AssertAll();
    }

    @Owner(PRAMOD_KUMAR)
    @Feature("PG2-4781")
    @Test(description = "PG2 COP Fast Forward Success Txn is done")
    public void validatePG2COPFullTrafficYSuccessFastForwardTxn() throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        WalletHelpers.modifyBalance(user, 100.0);
        Constants.MerchantType merchant = MerchantType.PG2_COP_FULL_TRAFFIC_Y;
        String qrCodeId = generateQRViaWallet(merchant);
        //FetchQRPaymentDetails
        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID(merchant.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();
        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        JsonPath fetchQRResponse = fetchQRPaymentDetails.execute().jsonPath();
        Assertions.assertThat(fetchQRResponse.getString("body.qrInfo.response.orderAlreadyCreated")).as("Static QR configuration is incorrect").isEqualTo(null);
        Assertions.assertThat(fetchQRResponse.getString("body.qrInfo.response.orderQr")).as("Static QR configuration is incorrect").isEqualTo("false");
        Assertions.assertThat(fetchQRResponse.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        String orderId = fetchQRResponse.getString("body.paymentOptions.orderId");
        FastForwardAppRequest fastForwardAppRequest = new FastForwardAppRequest
                .Builder(merchant.getId(), orderId, "50")
                .setPaymentMode("PPI")
                .setReqType("CLW_APP_PAY")
                .setToken(user.ssoToken())
                .setTokenType("SSO")
                .setCustomerId(user.custId())
                .setQrCodeId(qrCodeId)
                .setOrderId(orderId)
                .setOrderAlreadyCreated("false")
                .build();
        FastForward fastForward = new FastForward(fastForwardAppRequest);
        Response fastForwardResponse = fastForward.execute();
        fastForwardResponse.prettyPrint();
        Assertions.assertThat(fastForwardResponse.jsonPath().getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("TXN_SUCCESS");
        Assertions.assertThat(fastForwardResponse.jsonPath().getString("body.txnId")).contains("0000");

        TxnStatus txnStatus = new TxnStatus(merchant.getId(), orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_SUCCESS.getRespMsg())
                .validatePaymentMode("PPI")
                .validateGatewayName(Constants.Gateway.WALLET.toString())
                .validateMid(merchant.getId())
                .AssertAll();
    }
    @Owner(PRAMOD_KUMAR)
    @Feature("PG2-4781")
    @Test(description = "PG2 COP Fast Forward Success Txn is done  PG2_Enabled")
    public void validatePG2COPYSuccessFastForwardTxn() throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        WalletHelpers.modifyBalance(user, 100.0);
        Constants.MerchantType merchant = MerchantType.UPIPUSHPG2;
        String qrCodeId = generateQRViaWallet(merchant);
        //FetchQRPaymentDetails
        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID(merchant.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();
        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        JsonPath fetchQRResponse = fetchQRPaymentDetails.execute().jsonPath();
        Assertions.assertThat(fetchQRResponse.getString("body.qrInfo.response.orderAlreadyCreated")).as("Static QR configuration is incorrect").isEqualTo(null);
        Assertions.assertThat(fetchQRResponse.getString("body.qrInfo.response.orderQr")).as("Static QR configuration is incorrect").isEqualTo("false");
        Assertions.assertThat(fetchQRResponse.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        String orderId = fetchQRResponse.getString("body.paymentOptions.orderId");
        FastForwardAppRequest fastForwardAppRequest = new FastForwardAppRequest
                .Builder(merchant.getId(), orderId, "50")
                .setPaymentMode("PPI")
                .setReqType("CLW_APP_PAY")
                .setToken(user.ssoToken())
                .setTokenType("SSO")
                .setCustomerId(user.custId())
                .setQrCodeId(qrCodeId)
                .setOrderId(orderId)
                .setOrderAlreadyCreated("false")
                .build();
        FastForward fastForward = new FastForward(fastForwardAppRequest);
        Response fastForwardResponse = fastForward.execute();
        fastForwardResponse.prettyPrint();
        Assertions.assertThat(fastForwardResponse.jsonPath().getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("TXN_SUCCESS");
        Assertions.assertThat(fastForwardResponse.jsonPath().getString("body.txnId")).contains("0000");

        TxnStatus txnStatus = new TxnStatus(merchant.getId(), orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_SUCCESS.getRespMsg())
                .validatePaymentMode("PPI")
                .validateGatewayName(Constants.Gateway.WALLET.toString())
                .validateMid(merchant.getId())
                .AssertAll();
    }

    @Owner(HIMANSHU)
    @Test(description = "Validate PG2 txn through enhanced flow")
    public void walletEnhancedFlow(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.AddnPay(MerchantType.AddnPay, theme, user).build();
        WalletHelpers.modifyBalance(user, 100.0);
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        //cashierPage.login(user);
        cashierPage.payBy(Constants.PayMode.WALLET);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("PPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("WALLET")
                .validateBankName("WALLET")
                .validateCheckSum(MerchantType.AddnPay.getKey())
                .validateResponsePageParameters()
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("WALLET")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName("WALLET")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
    }
    @Owner(HIMANSHU)
    @Parameters({"theme"})
    @Test(description = "Validate PG2 txn through enhanced flow")
    public void walletCheckoutJSFlow(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        MerchantConfig config = checkoutJsPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutJsPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.WALLET);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_SUCCESS.getRespMsg())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
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
                .validateMid(initTxnDTO.getBody().getMid())
                .AssertAll();
    }

    @Owner(VAIBHAV)
    @Feature("PGP-49415")
    @Test(description = "Validate clientId & agentInfo must be passed into ACQUIRING_REFUND API request to PG2")
    public void validateClientIdAndAgentInfoAsyncRefund() throws Exception{
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        //WalletHelpers.modifyBalance(user,10.0);
        String mid = MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y.getId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "BALANCE", "false");
        String orderID= initTxnDTO.orderFromBody();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"")
                .setPaymentMode("BALANCE")
                .setAuthMode("USRPWD")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse, initTxnDTO);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getPAYMENTMODE()).isEqualTo("PPI");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPCODE()).isEqualTo("01");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getSTATUS()).isEqualTo(Constants.TXNSTATUS.TXN_SUCCESS.toString());

        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("PPI")
                .validateGatewayName(Constants.Gateway.WALLET.toString())
                .validateMid(mid)
                .AssertAll();

        SyncRefund syncRefund = new SyncRefund();
        Response asyncRefundResp = given().spec(syncRefund.reqSpecAsyncRefundAgentInfo(MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y, initTxnDTO.txnAmountFromBody(), initTxnDTO.getBody().getOrderId(),
                txnStatus.getResponse().getTXNID()))
                .post().then().extract().response();
        System.out.println("Async Refund Response " + asyncRefundResp.body().prettyPrint());
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.orderId")).isEqualTo(initTxnDTO.getBody().getOrderId());
    }

    @Owner(CHAKSHU)
    @Feature("PG2-13078")
    @Test(description = "for logged In flow Validate NickName Substring  less than 60 characters when user has name more than 60 characters")
    public void validateNickName() throws Exception {
        User user = userManager.getForWrite(Label.USERWITHNAMEMORETHAN60CHARACTER);
   //     WalletHelpers.modifyBalance(user,10.0);
        String mid = MerchantType.PG2_CC_DC.getId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.PG2_CC_DC).setTxnValue("5").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        PaymentDTO paymentDTO = new PaymentDTO();
        String cardInfo = "|" +paymentDTO.DEBIT_CARD_NUMBER+"|"+paymentDTO.getCvvNumber() + "|" + paymentDTO.getExpMonth()+ paymentDTO.getExpYear();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("DEBIT_CARD")
                .setAuthMode("otp")
                .setCardInfo(cardInfo)
                .setStoreInstrument("0")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.getBody().getOrderId(),"ACQUIRING_CREATE_ORDER");
        String nicknamevalue = PG2LogsValidationHelper.getKeyParameterValueFromLogs("nickname",logs);
        Assertions.assertThat(nicknamevalue.length()).isEqualTo(60);

    }
}