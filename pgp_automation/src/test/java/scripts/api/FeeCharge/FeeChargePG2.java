package scripts.api.FeeCharge;

import com.paytm.LocalConfig;
import com.paytm.ServerConfigProvider;
import com.paytm.api.TransactionStatusV1API;
import com.paytm.api.TxnStatus;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.api.nativeAPI.FetchPcfDetail;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.api.refund.SyncRefund;
import com.paytm.appconstants.Constants;
import com.paytm.appconstants.Constants.MerchantType;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.LogsValidationHelper;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.WalletHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.NativeDTO.fetchPcfDetail.FetchPcfRequest;
import com.paytm.dto.NativeDTO.fetchPcfDetail.PayMethod;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.TransactionStatusV1.Body;
import com.paytm.dto.TransactionStatusV1.Head;
import com.paytm.dto.TransactionStatusV1.TransactionStatusV1DTO;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.dto.processTransactionV1.response.ProcessTxnV1Response;
import com.paytm.framework.reporting.Reporter;
import com.paytm.pages.ResponsePage;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.paytm.appconstants.Constants.Owner.CHAKSHU;
import static io.restassured.RestAssured.given;

public class FeeChargePG2 extends PGPBaseTest {

    private static final String API_VERSION_V2 = "v2";
    private static final String PCF_PRODUCT_CODE = "\"productCode\":\"51051000100000000002\"";
    private static final String MDRPCF_PRODUCT_CODE = "\"productCode\":\"51051000100000000101\"";
    private static final String MDR_PRODUCT_CODE = "\"productCode\\\":\\\"51051000100000000001\\\"";
    private static final String PAYER_CHARGE_TARGET = "\"chargeTarget\\\":\\\"PAYER\\\"";
    private static final String RECEIVER_CHARGE_TARGET = "\"chargeTarget\\\":\\\"RECEIVER\\\"";
    private static final String RESULT_CODE_ID = "\"resultCodeId\":\"00000000\"";
    private static final String RESLUT_STATUS = "\"resultStatus\":\"S\"";
    private static final String RESULT_CODE = "\"resultCode\":\"SUCCESS\"";
    private static final String RETURN_CHARGE_TO_PAYER_TRUE = "\"returnChargeToPayer\":true,\"";
    private static final String RETURN_CHARGE_TO_PAYER_FALSE = "\"returnChargeToPayer\":false,\"";
    private static final String REFUND_AMOUNT_STRING = "refundAmount" + '"' + ":\"";
    private static final String NOTIFY_STRING = "value" + '"' + ":\"";
    private final String mpin = "NPCI,20150822,2.0|IvebqsSA1dDVYs3OBn4Q9\\/cOgJ5RecQQW7WCe4EOJBniwCUqI9ocIE50GMcbA5UPqdQuSO3urywKs47UTc1q1pN51zAeQ0ISxai+Yfii8amtYVeWL67G2lL9RS5NEp29C+7PQc+cL\\/j34mKrtUZvxA\\/GUiAjllwuTnTuud7hMhGNmO8h+fGmctKMrJsWtbULX4EMG\\/bO\\/ayMUpLRynqvR3nM2g8nfblqnukxApr2QJCy3LG0tzaNgVZc8rBmAFVCyweGKijmf0TKSv0dEEmm9js8In1+VH8da13zfwB52zfEPKc6gMtY6QYymGt6Z3Hekcz6gVR+XS8TSIswlReXCA==";
    private final String riskExtendedInfo = "deviceType:Mobile|timeZone:IST|osType:IOS|osVersion:15.1|platform:APP|terminalType:APP|deviceManufacturer:Apple|channelId:WAP|paymentFlow:NONE|versionCode:5109|screenResolution:750x1334|appVersion:9.21.0|operationType:PAYMENT|userLBSLatitude:32.19|isRooted:false|deviceId:1DCB75C8-1A6F-4A7B-94B6-C497542397D8|businessFlow:DEFER_CHECKOUT|deviceModel:iPhone 6s (iOS 15.1)|userLBSLongitude:75.65|language:en-IN";

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

    public String validatePCFChargeAmount(String txnAmount, Double percentCommsion, Double flatCommission) throws InterruptedException {
        double expectedChargeFeeAmt = convenienceFeeCalculator(Double.parseDouble(txnAmount), percentCommsion, flatCommission, "");
        return String.valueOf(expectedChargeFeeAmt);
    }

    public void ValidateBossChargeBatchFeeConsult(String orderId,String productcode, String expectedChargeAmount)  throws InterruptedException
    {
        String bossChargeRequest = "grep " + orderId + " " + LocalConfig.THEIA_FACADE_LOGS +
                " | grep \"FEE_BATCH_CONSULT\" | grep \"REQUEST\"";

        String bossChargeRequestLogs = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, bossChargeRequest);
        Assertions.assertThat(bossChargeRequestLogs).
                contains(productcode);

        String expectedChargeAmountcent = String.valueOf((int)(Double.parseDouble(expectedChargeAmount)*100));

        String bossChargeResponse= "grep " + orderId + " " + LocalConfig.THEIA_FACADE_LOGS +
                " | grep \"FEE_BATCH_CONSULT\" | grep \"RESPONSE\"";

        String bossChargeResponseLogs =  LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, bossChargeResponse);
        Assertions.assertThat(bossChargeResponseLogs).
                contains(RESULT_CODE_ID)
                .contains(RESLUT_STATUS)
                .contains(RESULT_CODE)
                .contains(expectedChargeAmountcent);
    }

    public void ValidateRefundWithChargeInInstaProxy(String refundid, String expectedRefundwithChargeAmount )  throws InterruptedException
    {
        String instarefund = "grep " + refundid + " " + LocalConfig.INSTAPROXY_LOGS;
        System.out.println("instarefund grep is---"+instarefund);

        String instarefundLogs =  LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.INSTAPROXY, instarefund);
        Assertions.assertThat(instarefundLogs).
                contains(expectedRefundwithChargeAmount);
    }

    public void ValidateChargeAmountInRefundSuccessNotify(String refundid, String returnChargeToPayer, String expextedChargeAmountInRefundSuccessNotify)  throws InterruptedException
    {
        String refundSuccessNotifyRequest = "grep " + refundid + " " + LocalConfig.PGPROXY_LOGS +
                " | grep \"request\" | grep \"alipayplus.acquiring.refund.refundSuccessNotify\"";

        String refundSuccessNotifyRequestLogs = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.PG_PROXY_NOTIFICATION, refundSuccessNotifyRequest);
        Assertions.assertThat(refundSuccessNotifyRequestLogs).
                contains(returnChargeToPayer)
                .contains(expextedChargeAmountInRefundSuccessNotify);
    }

    public void ValidateChargeAmountInPaymentNotify(String orderId,String productcode, String chargeTarget,  String expectedChargeAmountPaymentNotify)  throws InterruptedException
    {
        String paymentnotifyRequest = "grep " + orderId + " " + LocalConfig.PGPROXY_LOGS +
                " | grep \"request\" | grep \"alipayplus.acquiring.order.paymentNotify\"";

        String paymentnotifyRequestLogs =  LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.PG_PROXY_NOTIFICATION, paymentnotifyRequest);
        Assertions.assertThat(paymentnotifyRequestLogs).
                contains(productcode)
                . contains(chargeTarget)
                .contains(expectedChargeAmountPaymentNotify);
    }
    public void assertPTCCommonResponseForm(ProcessTxnV1Response ptcResponse){
        Assertions.assertThat(ptcResponse.getBody().getBankForm().getPageType()).isEqualTo("redirect");
        Assertions.assertThat(ptcResponse.getBody().getBankForm().getRedirectForm().getActionUrl()).isNotNull();
        Assertions.assertThat(ptcResponse.getBody().getBankForm().getRedirectForm().getMethod()).isEqualTo("post");
        Assertions.assertThat(ptcResponse.getBody().getBankForm().getRedirectForm().getType()).isEqualTo("redirect");
        Assertions.assertThat(ptcResponse.getBody().getBankForm().getRedirectForm().getHeaders()).isNotNull();
        Assertions.assertThat(ptcResponse.getBody().getBankForm().getRedirectForm().getHeaders()).isNotNull();
        Assertions.assertThat(ptcResponse.getBody().getBankForm().getRedirectForm().getContent()).isNotNull();
    }

    public void assertPTCCommonResponseFormUPICollect(ProcessTxnV1Response ptcResponse){
        Assertions.assertThat(ptcResponse.getBody().getBankForm().getPageType()).isEqualTo("redirect");
        Assertions.assertThat(ptcResponse.getBody().getBankForm().getRedirectForm().getActionUrl()).isNotNull();
        Assertions.assertThat(ptcResponse.getBody().getBankForm().getRedirectForm().getMethod()).isEqualTo("POST");
        Assertions.assertThat(ptcResponse.getBody().getBankForm().getRedirectForm().getType()).isEqualTo("redirect");
        Assertions.assertThat(ptcResponse.getBody().getBankForm().getRedirectForm().getHeaders()).isNotNull();
        Assertions.assertThat(ptcResponse.getBody().getBankForm().getRedirectForm().getHeaders()).isNotNull();
        Assertions.assertThat(ptcResponse.getBody().getBankForm().getRedirectForm().getContent()).isNotNull();
    }

    @Owner(CHAKSHU)
    @Feature("PG2-7035")
    @Test(description = "Validate PCF 02 product code txn with chargeamount for theia.payment.adapter.feature Postpaid and refund with chargeamount ")
    public void validateTheiaPaymentAdapterFeaturePostPaidPCFTxn() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String txnamount = "100";
        MerchantType merchant = MerchantType.Payment_Adapter_Config_PCF_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnamount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "PAYTM_DIGITAL_CREDIT", "false");
        String mid = merchant.getId();
        String orderId = initTxnDTO.getBody().getOrderId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid, initTxnResponse.getBody().getTxnToken(), orderId, "")
                .setPaymentMode("PAYTM_DIGITAL_CREDIT")
                .setAuthMode("3D")
                .setChannelId("WEB")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse, initTxnDTO);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getPAYMENTMODE()).isEqualTo("Paytm Postpaid");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPCODE()).isEqualTo("01");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getSTATUS()).isEqualTo(Constants.TXNSTATUS.TXN_SUCCESS.toString());

        CommonHelpers.verifyEsnLength(initTxnDTO.getBody().getOrderId(),19);
        String expectedChargeAmount = validatePCFChargeAmount(txnamount,2.0, 0.0);

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("Paytm Postpaid")
                .validateGatewayName(Constants.Gateway.PAYTMCC.toString())
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
        Assertions.assertThat(response.jsonPath().getString("body.txnInfo.CHARGEAMOUNT")).isEqualTo(expectedChargeAmount);

        ValidateBossChargeBatchFeeConsult(orderId, PCF_PRODUCT_CODE, expectedChargeAmount);

        String expectedChargeAmountNotify = String.valueOf((int)(Double.parseDouble(expectedChargeAmount)*100));
        String expectedChargeAmountNotifyString =  NOTIFY_STRING +  expectedChargeAmountNotify;
        ValidateChargeAmountInPaymentNotify(orderId, PCF_PRODUCT_CODE, PAYER_CHARGE_TARGET, expectedChargeAmountNotifyString);

        //passing txnType as "R" for returning charge amount
        SyncRefund refundWithChargeAmount = new SyncRefund();
        Response asyncRefundResp = given().spec(refundWithChargeAmount.reqSpecAsyncRefundtxnType(merchant, initTxnDTO.txnAmountFromBody(), orderId,
                        txnStatus.getResponse().getTXNID(), "R"))
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

        String refundid = asyncRefundResp.jsonPath().getString("body.refundId");
        String expectedRefundWithChargeAmount = String.valueOf((int)((Double.parseDouble(txnamount) + Double.parseDouble(expectedChargeAmount))*100));

        String expectedrefundamountstring = REFUND_AMOUNT_STRING +  expectedRefundWithChargeAmount;
        ValidateRefundWithChargeInInstaProxy(refundid, expectedrefundamountstring);

        String expectedchargeamountRefundSuccessNotify = NOTIFY_STRING +  expectedChargeAmountNotify;
        ValidateChargeAmountInRefundSuccessNotify(refundid, RETURN_CHARGE_TO_PAYER_TRUE, expectedchargeamountRefundSuccessNotify);
    }

    @Owner(CHAKSHU)
    @Feature("PG2-7035")
    @Test(description = "Validate PCF 02 product code txn with chargeamount for theia.payment.adapter.feature Postpaid and partial refund ")
    public void validateTheiaPaymentAdapterFeaturePostPaidPCFTxnPartialRefund() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String txnamount = "100";
        MerchantType merchant = MerchantType.Payment_Adapter_Config_PCF_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnamount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "PAYTM_DIGITAL_CREDIT", "false");
        String mid = merchant.getId();
        String orderId = initTxnDTO.getBody().getOrderId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid, initTxnResponse.getBody().getTxnToken(), orderId, "")
                .setPaymentMode("PAYTM_DIGITAL_CREDIT")
                .setAuthMode("3D")
                .setChannelId("WEB")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse, initTxnDTO);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getPAYMENTMODE()).isEqualTo("Paytm Postpaid");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPCODE()).isEqualTo("01");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getSTATUS()).isEqualTo(Constants.TXNSTATUS.TXN_SUCCESS.toString());

        CommonHelpers.verifyEsnLength(initTxnDTO.getBody().getOrderId(),19);
        String expectedChargeAmount = validatePCFChargeAmount(txnamount,2.0, 0.0);

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("Paytm Postpaid")
                .validateGatewayName(Constants.Gateway.PAYTMCC.toString())
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
        Assertions.assertThat(response.jsonPath().getString("body.txnInfo.CHARGEAMOUNT")).isEqualTo(expectedChargeAmount);

        ValidateBossChargeBatchFeeConsult(orderId, PCF_PRODUCT_CODE, expectedChargeAmount);

//        String expectedChargeAmountNotify = String.valueOf((int)(Double.parseDouble(expectedChargeAmount)*100));
//        String expectedChargeAmountNotifyString =  NOTIFY_STRING +  expectedChargeAmountNotify;
//        ValidateChargeAmountInPaymentNotify(orderId, PCF_PRODUCT_CODE, PAYER_CHARGE_TARGET, expectedChargeAmountNotifyString);

        String refundAmount = "50";
        SyncRefund syncRefund = new SyncRefund();
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

        String refundid = asyncRefundResp.jsonPath().getString("body.refundId");
        String expectedRefundAmount = String.valueOf((int)( Double.parseDouble(refundAmount)*100));

        String expectedrefundamountstring = REFUND_AMOUNT_STRING +  expectedRefundAmount;
        ValidateRefundWithChargeInInstaProxy(refundid, expectedrefundamountstring);

//        String expectedchargeamountRefundSuccessNotify = NOTIFY_STRING +  expectedRefundAmount;
//        ValidateChargeAmountInRefundSuccessNotify(refundid, RETURN_CHARGE_TO_PAYER_FALSE, expectedchargeamountRefundSuccessNotify);
    }


    @Owner(CHAKSHU)
    @Feature("PG2-7035")
    @Test(description = "Validate MDR + PCF 101 product code txn with chargeamount for theia.payment.adapter.feature UPI Push Express ")
    public void validateTheiaPaymentAdapterFeatureUPIPushExpressMDRPCFTxn() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String txnamount = "100";
        MerchantType merchant = MerchantType.Payment_Adapter_Config_MDRPCF_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnamount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        String mid = merchant.getId();
        String orderId = initTxnDTO.getBody().getOrderId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setPreferredOtpPage("bank")
                .setAuthMode("USRPWD")
                .setChannelCode("push")
                .setChannelId("WAP")
                .setMpin(mpin)
                .setRiskExtendInfo(riskExtendedInfo)
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse, initTxnDTO);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPCODE()).isEqualTo("01");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getSTATUS()).isEqualTo(Constants.TXNSTATUS.TXN_SUCCESS.toString());

        CommonHelpers.verifyEsnLength(initTxnDTO.getBody().getOrderId(),19);
        String expectedChargeAmount = validatePCFChargeAmount(txnamount,2.0, 0.0);

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("UPI")
                .validateChargeAmount(expectedChargeAmount)
                .validateGatewayName(Constants.Gateway.PPBEX.toString())
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
        Assertions.assertThat(response.jsonPath().getString("body.txnInfo.CHARGEAMOUNT")).isEqualTo(expectedChargeAmount);

        ValidateBossChargeBatchFeeConsult(orderId, MDRPCF_PRODUCT_CODE, expectedChargeAmount);

//        String expectedChargeAmountNotify = String.valueOf((int)(Double.parseDouble(expectedChargeAmount)*100));
//        String expectedChargeAmountNotifyString =  NOTIFY_STRING +  expectedChargeAmountNotify;
//        ValidateChargeAmountInPaymentNotify(orderId, MDRPCF_PRODUCT_CODE, PAYER_CHARGE_TARGET, expectedChargeAmountNotifyString);

        //passing txnType as "R" for returning charge amount
        SyncRefund refundWithChargeAmount = new SyncRefund();
        Response asyncRefundResp = given().spec(refundWithChargeAmount.reqSpecAsyncRefundtxnType(merchant, initTxnDTO.txnAmountFromBody(), orderId,
                        txnStatus.getResponse().getTXNID(), "R"))
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

        String refundid = asyncRefundResp.jsonPath().getString("body.refundId");
        String expectedRefundWithChargeAmount = String.valueOf((int)((Double.parseDouble(txnamount) + Double.parseDouble(expectedChargeAmount))*100));

        String expectedrefundamountstring = REFUND_AMOUNT_STRING +  expectedRefundWithChargeAmount;
        ValidateRefundWithChargeInInstaProxy(refundid, expectedrefundamountstring);
    }

    @Owner(CHAKSHU)
    @Feature("PG2-7035")
    @Test(description = "Validate MDR 01 product code txn with chargeamount 0 for theia.payment.adapter.feature PPBL NB ")
    public void validateTheiaPaymentAdapterFeaturePPBLNBMDRTxn() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String txnamount = "100";
        MerchantType merchant = MerchantType.Payment_Adapter_Config_MDR_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnamount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, Constants.Bank.PPBL.toString(), "false");
        String mid = merchant.getId();
        String orderId = initTxnDTO.getBody().getOrderId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode(Constants.Bank.PPBL.toString())
                .setAuthMode("USRPWD")
                .setMpin("1234")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponseForm(ptcResponse);
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
                .validateOrderId(orderId)
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

        String expectedChargeAmount = validatePCFChargeAmount(txnamount,0.0, 0.0);

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
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

//        String expectedChargeAmountNotify = String.valueOf((int)(Double.parseDouble(expectedChargeAmount)*100));
//        String expectedChargeAmountNotifyString =  NOTIFY_STRING +  expectedChargeAmountNotify;
//        ValidateChargeAmountInPaymentNotify(orderId, MDR_PRODUCT_CODE, RECEIVER_CHARGE_TARGET, expectedChargeAmountNotifyString);

        //passing txnType as "R" for returning charge amount
        SyncRefund refundWithChargeAmount = new SyncRefund();
        Response asyncRefundResp = given().spec(refundWithChargeAmount.reqSpecAsyncRefundtxnType(merchant, initTxnDTO.txnAmountFromBody(), orderId,
                        txnStatus.getResponse().getTXNID(), "R"))
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

        String refundid = asyncRefundResp.jsonPath().getString("body.refundId");
        String expectedRefundWithChargeAmount = String.valueOf((int)((Double.parseDouble(txnamount) + Double.parseDouble(expectedChargeAmount))*100));

        String expectedrefundamountstring = REFUND_AMOUNT_STRING +  expectedRefundWithChargeAmount;
        ValidateRefundWithChargeInInstaProxy(refundid, expectedrefundamountstring);
    }

    @Owner(CHAKSHU)
    @Feature("PG2-7162")
    @Test(description = "Validate PG2_ENABLED Debit Card PCF 02 product code with platform fee txn and refund with chargeamount ")
    public void validatePlatformFeePG2ENABLEDPCFDCTxn() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String txnamount = "100";
        Constants.MerchantType merchant = Constants.MerchantType.PG2_ENABLED_PCF_Platform_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnamount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "DEBIT_CARD", "false");
        String mid = merchant.getId();
        String orderId = initTxnDTO.getBody().getOrderId();
        PaymentDTO paymentDTO = new PaymentDTO();
        String cardInfo = "|" +paymentDTO.DEBIT_CARD_NUMBER+"|"+paymentDTO.getCvvNumber() + "|" + paymentDTO.getExpMonth()+ paymentDTO.getExpYear();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("DEBIT_CARD")
                .setAuthMode("otp")
                .setCardInfo(cardInfo)
                .setStoreInstrument("0")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponseForm(ptcResponse);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        NativeHelpers.submitJsonFormInBrowser(ptcResponse.toString());
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(mid)
                .validateOrderId(orderId)
                .validatePaymentMode("DC")
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_SUCCESS.getRespMsg())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Bank.HDFC.toString())
                .validateBankName(Constants.Bank.HDFCBANK.toString())
                .assertAll();

        CommonHelpers.verifyEsnLength(initTxnDTO.getBody().getOrderId(),19);
        String txnChargAmount = validatePCFChargeAmount(txnamount,2.0, 1.0);
        String platformChargAmount = validatePCFChargeAmount(txnamount,2.0, 1.0);
        String expectedChargeAmount = String.valueOf(CommonHelpers.doubleHalfUpConvertor(Double.parseDouble(txnChargAmount) + Double.parseDouble(platformChargAmount)));

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_SUCCESS.getRespMsg())
                .validatePaymentMode("DC")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
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
        Assertions.assertThat(response.jsonPath().getString("body.txnInfo.CHARGEAMOUNT")).isEqualTo(expectedChargeAmount);

        ValidateBossChargeBatchFeeConsult(orderId, PCF_PRODUCT_CODE, expectedChargeAmount);

//        String expectedChargeAmountNotify = String.valueOf((int)(Double.parseDouble(expectedChargeAmount)*100));
//        String expectedChargeAmountNotifyString =  NOTIFY_STRING +  expectedChargeAmountNotify;
//        ValidateChargeAmountInPaymentNotify(orderId, PCF_PRODUCT_CODE, PAYER_CHARGE_TARGET, expectedChargeAmountNotifyString);

        //passing txnType as "R" for returning charge amount
        SyncRefund refundWithChargeAmount = new SyncRefund();
        Response asyncRefundResp = given().spec(refundWithChargeAmount.reqSpecAsyncRefundtxnType(merchant, initTxnDTO.txnAmountFromBody(), orderId,
                        txnStatus.getResponse().getTXNID(), "R"))
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

        String refundid = asyncRefundResp.jsonPath().getString("body.refundId");
        String expectedRefundWithChargeAmount = String.valueOf((int)((Double.parseDouble(txnamount) + Double.parseDouble(expectedChargeAmount))*100));

        String expectedrefundamountstring = REFUND_AMOUNT_STRING +  expectedRefundWithChargeAmount;
        ValidateRefundWithChargeInInstaProxy(refundid, expectedrefundamountstring);

//        String expectedchargeamountRefundSuccessNotify = NOTIFY_STRING +  expectedChargeAmountNotify;
//        ValidateChargeAmountInRefundSuccessNotify(refundid, RETURN_CHARGE_TO_PAYER_TRUE, expectedchargeamountRefundSuccessNotify);
    }

    @Owner(CHAKSHU)
    @Feature("PG2-7162")
    @Test(description = "Validate PG2_ENABLED Credit Card MDRPCF 101 product code with platform fee as MDR txn and refund with chargeamount ")
    public void validatePlatformFeeAsMDRPG2ENABLEDMDRPCFCCTxn() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String txnamount = "100";
        Constants.MerchantType merchant = Constants.MerchantType.PG2_ENABLED_MDRPCF_Platform_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnamount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "CREDIT_CARD", "false");
        String mid = merchant.getId();
        String orderId = initTxnDTO.getBody().getOrderId();
        PaymentDTO paymentDTO = new PaymentDTO();
        String cardInfo = "|" +paymentDTO.MASTER_CREDIT_CARD +"|"+paymentDTO.getCvvNumber() + "|" + paymentDTO.getExpMonth()+ paymentDTO.getExpYear();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("CREDIT_CARD")
                .setAuthMode("otp")
                .setCardInfo(cardInfo)
                .setStoreInstrument("0")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponseForm(ptcResponse);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        NativeHelpers.submitJsonFormInBrowser(ptcResponse.toString());
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(mid)
                .validateOrderId(orderId)
                .validatePaymentMode("CC")
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_SUCCESS.getRespMsg())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Bank.HDFC.toString())
                .validateBankName(Constants.Bank.HDFCBANK.toString())
                .assertAll();

        CommonHelpers.verifyEsnLength(initTxnDTO.getBody().getOrderId(),19);
        String expectedChargeAmount = validatePCFChargeAmount(txnamount,2.0, 1.0);

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_SUCCESS.getRespMsg())
                .validatePaymentMode("CC")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateChargeAmount(expectedChargeAmount)
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
        Assertions.assertThat(response.jsonPath().getString("body.txnInfo.CHARGEAMOUNT")).isEqualTo(expectedChargeAmount);

        ValidateBossChargeBatchFeeConsult(orderId, MDRPCF_PRODUCT_CODE, expectedChargeAmount);

//        String expectedChargeAmountNotify = String.valueOf((int)(Double.parseDouble(expectedChargeAmount)*100));
//        String expectedChargeAmountNotifyString =  NOTIFY_STRING +  expectedChargeAmountNotify;
//        ValidateChargeAmountInPaymentNotify(orderId, MDRPCF_PRODUCT_CODE, PAYER_CHARGE_TARGET, expectedChargeAmountNotifyString);

        //passing txnType as "R" for returning charge amount
        SyncRefund refundWithChargeAmount = new SyncRefund();
        Response asyncRefundResp = given().spec(refundWithChargeAmount.reqSpecAsyncRefundtxnType(merchant, initTxnDTO.txnAmountFromBody(), orderId,
                        txnStatus.getResponse().getTXNID(), "R"))
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

        String refundid = asyncRefundResp.jsonPath().getString("body.refundId");
        String expectedRefundWithChargeAmount = String.valueOf((int)((Double.parseDouble(txnamount) + Double.parseDouble(expectedChargeAmount))*100));

        String expectedrefundamountstring = REFUND_AMOUNT_STRING +  expectedRefundWithChargeAmount;
        ValidateRefundWithChargeInInstaProxy(refundid, expectedrefundamountstring);

//        String expectedchargeamountRefundSuccessNotify = NOTIFY_STRING +  expectedChargeAmountNotify;
//        ValidateChargeAmountInRefundSuccessNotify(refundid, RETURN_CHARGE_TO_PAYER_TRUE, expectedchargeamountRefundSuccessNotify);
    }

    @Owner(CHAKSHU)
    @Feature("PG2-7164")
    @Test(description = "Validate UPI Collect FULL PG2 TRAFFIC ENABLED COP PCF 02 product code with txn SLAB and refund with chargeamount Rs. 1.0 + 1.0 % for transaction amount <= 1000.0 with upperlimit: true ")
    public void validateFirstTxnSlabFullPG2TrafficCOPEnabledPCFUPICollectTxn() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String txnamount = "1000";
        Constants.MerchantType merchant = Constants.MerchantType.FULL_PG2_TRAFFIC_ENABLED_COP_PCF_TXN_SLAB_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnamount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        String mid = merchant.getId();
        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setAuthMode("USRPWD")
                .setPayerAccount("test9972746530@paytm")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponseFormUPICollect(ptcResponse);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        String expectedChargeAmount = validatePCFChargeAmount(txnamount,1.0, 1.0);

        NativeHelpers.submitJsonFormInBrowser(ptcResponse.toString());
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(mid)
                .validateOrderId(orderId)
                .validatePaymentMode("UPI")
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_SUCCESS.getRespMsg())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.PPBLC.toString())
                .validateChargeAmount(expectedChargeAmount)
                .assertAll();

        CommonHelpers.verifyEsnLength(initTxnDTO.getBody().getOrderId(),19);

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PPBLC.toString())
                .validateMid(mid)
                .AssertAll();

        ValidateBossChargeBatchFeeConsult(orderId, PCF_PRODUCT_CODE, expectedChargeAmount);

//        String expectedChargeAmountNotify = String.valueOf((int)(Double.parseDouble(expectedChargeAmount)*100));
//        String expectedChargeAmountNotifyString =  NOTIFY_STRING +  expectedChargeAmountNotify;
//        ValidateChargeAmountInPaymentNotify(orderId, PCF_PRODUCT_CODE, PAYER_CHARGE_TARGET, expectedChargeAmountNotifyString);

        //passing txnType as "R" for returning charge amount
        SyncRefund refundWithChargeAmount = new SyncRefund();
        Response asyncRefundResp = given().spec(refundWithChargeAmount.reqSpecAsyncRefundtxnType(merchant, initTxnDTO.txnAmountFromBody(), orderId,
                        txnStatus.getResponse().getTXNID(), "R"))
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

        String refundid = asyncRefundResp.jsonPath().getString("body.refundId");
        String expectedRefundWithChargeAmount = String.valueOf((int)((Double.parseDouble(txnamount) + Double.parseDouble(expectedChargeAmount))*100));

        String expectedrefundamountstring = REFUND_AMOUNT_STRING +  expectedRefundWithChargeAmount;
        ValidateRefundWithChargeInInstaProxy(refundid, expectedrefundamountstring);
    }

    @Owner(CHAKSHU)
    @Feature("PG2-7164")
    @Test(description = "Validate UPI Collect FULL PG2 TRAFFIC ENABLED COP PCF 02 product code with txn SLAB and refund with chargeamount Rs. 1.0 + 2.0 % for transaction amount between 1000.0 to 2000.0")
    public void validateSecondTxnSlabFullPG2TrafficCOPEnabledPCFUPICollectTxn() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String txnamount = "1010";
        Constants.MerchantType merchant = Constants.MerchantType.FULL_PG2_TRAFFIC_ENABLED_COP_PCF_TXN_SLAB_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnamount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        String mid = merchant.getId();
        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setAuthMode("USRPWD")
                .setPayerAccount("test9972746530@paytm")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponseFormUPICollect(ptcResponse);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        String chargeAmount = validatePCFChargeAmount(txnamount,2.0, 1.0);
        String expectedChargeAmount = String.valueOf(Double.parseDouble(chargeAmount));

        NativeHelpers.submitJsonFormInBrowser(ptcResponse.toString());
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(mid)
                .validateOrderId(orderId)
                .validatePaymentMode("UPI")
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_SUCCESS.getRespMsg())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.PPBLC.toString())
                .validateChargeAmount(expectedChargeAmount)
                .assertAll();

        CommonHelpers.verifyEsnLength(initTxnDTO.getBody().getOrderId(),19);
        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PPBLC.toString())
                .validateMid(mid)
                .AssertAll();

        ValidateBossChargeBatchFeeConsult(orderId, PCF_PRODUCT_CODE, expectedChargeAmount);

//        String expectedChargeAmountNotify = String.valueOf((int)(Double.parseDouble(expectedChargeAmount)*100));
//        String expectedChargeAmountNotifyString =  NOTIFY_STRING +  expectedChargeAmountNotify;
//        ValidateChargeAmountInPaymentNotify(orderId, PCF_PRODUCT_CODE, PAYER_CHARGE_TARGET, expectedChargeAmountNotifyString);

        //passing txnType as "R" for returning charge amount
        SyncRefund refundWithChargeAmount = new SyncRefund();
        Response asyncRefundResp = given().spec(refundWithChargeAmount.reqSpecAsyncRefundtxnType(merchant, initTxnDTO.txnAmountFromBody(), orderId,
                        txnStatus.getResponse().getTXNID(), "R"))
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

        String refundid = asyncRefundResp.jsonPath().getString("body.refundId");
        String expectedRefundWithChargeAmount = String.valueOf((int)((Double.parseDouble(txnamount) + Double.parseDouble(expectedChargeAmount))*100));

        String expectedrefundamountstring = REFUND_AMOUNT_STRING +  expectedRefundWithChargeAmount;
        ValidateRefundWithChargeInInstaProxy(refundid, expectedrefundamountstring);

    }

    @Owner(CHAKSHU)
    @Feature("PG2-7164")
    @Test(description = "Validate UPI Collect FULL PG2 TRAFFIC ENABLED COP PCF 02 product code with txn SLAB and refund with chargeamount Rs. 1.0 + 2.0 % for transaction amount between 1000.0 to 2000.0 with upperlimit: true ")
    public void validateThirdTxnSlabFullPG2TrafficCOPEnabledPCFUPICollectTxn() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String txnamount = "2000";
        Constants.MerchantType merchant = Constants.MerchantType.FULL_PG2_TRAFFIC_ENABLED_COP_PCF_TXN_SLAB_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnamount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        String mid = merchant.getId();
        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setAuthMode("USRPWD")
                .setPayerAccount("test9972746530@paytm")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponseFormUPICollect(ptcResponse);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        String expectedChargeAmount = validatePCFChargeAmount(txnamount,2.0, 1.0);

        NativeHelpers.submitJsonFormInBrowser(ptcResponse.toString());
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(mid)
                .validateOrderId(orderId)
                .validatePaymentMode("UPI")
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_SUCCESS.getRespMsg())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.PPBLC.toString())
                .validateChargeAmount(expectedChargeAmount)
                .assertAll();

        CommonHelpers.verifyEsnLength(initTxnDTO.getBody().getOrderId(),19);

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PPBLC.toString())
                .validateMid(mid)
                .AssertAll();

        ValidateBossChargeBatchFeeConsult(orderId, PCF_PRODUCT_CODE, expectedChargeAmount);

//        String expectedChargeAmountNotify = String.valueOf((int)(Double.parseDouble(expectedChargeAmount)*100));
//        String expectedChargeAmountNotifyString =  NOTIFY_STRING +  expectedChargeAmountNotify;
//        ValidateChargeAmountInPaymentNotify(orderId, PCF_PRODUCT_CODE, PAYER_CHARGE_TARGET, expectedChargeAmountNotifyString);

        //passing txnType as "R" for returning charge amount
        SyncRefund refundWithChargeAmount = new SyncRefund();
        Response asyncRefundResp = given().spec(refundWithChargeAmount.reqSpecAsyncRefundtxnType(merchant, initTxnDTO.txnAmountFromBody(), orderId,
                        txnStatus.getResponse().getTXNID(), "R"))
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

        String refundid = asyncRefundResp.jsonPath().getString("body.refundId");
        String expectedRefundWithChargeAmount = String.valueOf((int)((Double.parseDouble(txnamount) + Double.parseDouble(expectedChargeAmount))*100));

        String expectedrefundamountstring = REFUND_AMOUNT_STRING +  expectedRefundWithChargeAmount;
        ValidateRefundWithChargeInInstaProxy(refundid, expectedrefundamountstring);

    }

    @Owner(CHAKSHU)
    @Feature("PG2-7164")
    @Test(description = "Validate UPI Collect FULL PG2 TRAFFIC ENABLED COP PCF 02 product code with txn SLAB and refund with chargeamount Rs. 1.0 + 3.0 % for transaction amount  > 2000.0 ")
    public void validateFourthTxnSlabFullPG2TrafficCOPEnabledPCFUPICollectTxn() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String txnamount = "2001";
        Constants.MerchantType merchant = Constants.MerchantType.FULL_PG2_TRAFFIC_ENABLED_COP_PCF_TXN_SLAB_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnamount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        String mid = merchant.getId();
        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setAuthMode("USRPWD")
                .setPayerAccount("test9972746530@paytm")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponseFormUPICollect(ptcResponse);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        String expectedChargeAmount = validatePCFChargeAmount(txnamount,3.0, 1.0);

        NativeHelpers.submitJsonFormInBrowser(ptcResponse.toString());
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(mid)
                .validateOrderId(orderId)
                .validatePaymentMode("UPI")
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_SUCCESS.getRespMsg())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.PPBLC.toString())
                .validateChargeAmount(expectedChargeAmount)
                .assertAll();

        CommonHelpers.verifyEsnLength(initTxnDTO.getBody().getOrderId(),19);

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PPBLC.toString())
                .validateMid(mid)
                .AssertAll();

        ValidateBossChargeBatchFeeConsult(orderId, PCF_PRODUCT_CODE, expectedChargeAmount);

//        String expectedChargeAmountNotify = String.valueOf((int)(Double.parseDouble(expectedChargeAmount)*100));
//        String expectedChargeAmountNotifyString =  NOTIFY_STRING +  expectedChargeAmountNotify;
//        ValidateChargeAmountInPaymentNotify(orderId, PCF_PRODUCT_CODE, PAYER_CHARGE_TARGET, expectedChargeAmountNotifyString);

        //passing txnType as "R" for returning charge amount
        SyncRefund refundWithChargeAmount = new SyncRefund();
        Response asyncRefundResp = given().spec(refundWithChargeAmount.reqSpecAsyncRefundtxnType(merchant, initTxnDTO.txnAmountFromBody(), orderId,
                        txnStatus.getResponse().getTXNID(), "R"))
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

        String refundid = asyncRefundResp.jsonPath().getString("body.refundId");
        String expectedRefundWithChargeAmount = String.valueOf((int)((Double.parseDouble(txnamount) + Double.parseDouble(expectedChargeAmount))*100));

        String expectedrefundamountstring = REFUND_AMOUNT_STRING +  expectedRefundWithChargeAmount;
        ValidateRefundWithChargeInInstaProxy(refundid, expectedrefundamountstring);
    }

    @Owner(CHAKSHU)
    @Feature("PG2-7164")
    @Test(description = "Validate PPBL NB FULL PG2 TRAFFIC ENABLED COP MDRPCF 101 product code with txn SLAB and refund with chargeamount Rs. 1.0 + 1.0 % for transaction amount <= 1000.0 with upperlimit: true ")
    public void validateFirstTxnSlabFullPG2TrafficCOPEnabledMDRPCFPPBLNBTxn() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String txnamount = "1000";
        Constants.MerchantType merchant = Constants.MerchantType.FULL_PG2_TRAFFIC_ENABLED_COP_MDRPCF_TXN_SLAB_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnamount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, Constants.Bank.PPBL.toString(), "false");
        String mid = merchant.getId();
        String orderId = initTxnDTO.getBody().getOrderId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode(Constants.Bank.PPBL.toString())
                .setAuthMode("USRPWD")
                .setMpin("1234")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponseForm(ptcResponse);
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
                .validateOrderId(orderId)
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

        String expectedChargeAmount = validatePCFChargeAmount(txnamount,1.0, 1.0);

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_SUCCESS.getRespMsg())
                .validatePaymentMode("NB")
                .validateGatewayName(Constants.Gateway.PPBL.toString())
                .validateMid(mid)
                .validateChargeAmount(expectedChargeAmount)
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
        Assertions.assertThat(response.jsonPath().getString("body.txnInfo.CHARGEAMOUNT")).isEqualTo(expectedChargeAmount);

        ValidateBossChargeBatchFeeConsult(orderId, MDRPCF_PRODUCT_CODE, expectedChargeAmount);

//        String expectedChargeAmountNotify = String.valueOf((int)(Double.parseDouble(expectedChargeAmount)*100));
//        String expectedChargeAmountNotifyString =  NOTIFY_STRING +  expectedChargeAmountNotify;
//        ValidateChargeAmountInPaymentNotify(orderId, MDRPCF_PRODUCT_CODE, PAYER_CHARGE_TARGET, expectedChargeAmountNotifyString);

        //passing txnType as "R" for returning charge amount
        SyncRefund refundWithChargeAmount = new SyncRefund();
        Response asyncRefundResp = given().spec(refundWithChargeAmount.reqSpecAsyncRefundtxnType(merchant, initTxnDTO.txnAmountFromBody(), orderId,
                        txnStatus.getResponse().getTXNID(), "R"))
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

        String refundid = asyncRefundResp.jsonPath().getString("body.refundId");
        String expectedRefundWithChargeAmount = String.valueOf((int)((Double.parseDouble(txnamount) + Double.parseDouble(expectedChargeAmount))*100));

        String expectedrefundamountstring = REFUND_AMOUNT_STRING +  expectedRefundWithChargeAmount;
        ValidateRefundWithChargeInInstaProxy(refundid, expectedrefundamountstring);

//        String expectedchargeamountRefundSuccessNotify = NOTIFY_STRING +  expectedChargeAmountNotify;
//        ValidateChargeAmountInRefundSuccessNotify(refundid, RETURN_CHARGE_TO_PAYER_TRUE, expectedchargeamountRefundSuccessNotify);
    }

    @Owner(CHAKSHU)
    @Feature("PG2-7164")
    @Test(description = "Validate PPBL NB FULL PG2 TRAFFIC ENABLED COP MDRPCF 101 product code with txn SLAB and refund with chargeamount Rs. 1.0 + 2.0 % for transaction amount between 1000.0 to 2000.0 ")
    public void validateSecondTxnSlabFullPG2TrafficCOPEnabledMDRPCFPPBLNBTxn() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String txnamount = "1010";
        Constants.MerchantType merchant = Constants.MerchantType.FULL_PG2_TRAFFIC_ENABLED_COP_MDRPCF_TXN_SLAB_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnamount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, Constants.Bank.PPBL.toString(), "false");
        String mid = merchant.getId();
        String orderId = initTxnDTO.getBody().getOrderId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode(Constants.Bank.PPBL.toString())
                .setAuthMode("USRPWD")
                .setMpin("1234")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponseForm(ptcResponse);
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
                .validateOrderId(orderId)
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

        String expectedChargeAmount = validatePCFChargeAmount(txnamount,2.0, 1.0);

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_SUCCESS.getRespMsg())
                .validatePaymentMode("NB")
                .validateGatewayName(Constants.Gateway.PPBL.toString())
                .validateMid(mid)
                .validateChargeAmount(expectedChargeAmount)
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
        Assertions.assertThat(response.jsonPath().getString("body.txnInfo.CHARGEAMOUNT")).isEqualTo(expectedChargeAmount);

        ValidateBossChargeBatchFeeConsult(orderId, MDRPCF_PRODUCT_CODE, expectedChargeAmount);

//        String expectedChargeAmountNotify = String.valueOf((int)(Double.parseDouble(expectedChargeAmount)*100));
//        String expectedChargeAmountNotifyString =  NOTIFY_STRING +  expectedChargeAmountNotify;
//        ValidateChargeAmountInPaymentNotify(orderId, MDRPCF_PRODUCT_CODE, PAYER_CHARGE_TARGET, expectedChargeAmountNotifyString);

        //passing txnType as "R" for returning charge amount
        SyncRefund refundWithChargeAmount = new SyncRefund();
        Response asyncRefundResp = given().spec(refundWithChargeAmount.reqSpecAsyncRefundtxnType(merchant, initTxnDTO.txnAmountFromBody(), orderId,
                        txnStatus.getResponse().getTXNID(), "R"))
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

        String refundid = asyncRefundResp.jsonPath().getString("body.refundId");
        String expectedRefundWithChargeAmount = String.valueOf((int)((Double.parseDouble(txnamount) + Double.parseDouble(expectedChargeAmount))*100));

        String expectedrefundamountstring = REFUND_AMOUNT_STRING +  expectedRefundWithChargeAmount;
        ValidateRefundWithChargeInInstaProxy(refundid, expectedrefundamountstring);

//        String expectedchargeamountRefundSuccessNotify = NOTIFY_STRING +  expectedChargeAmountNotify;
//        ValidateChargeAmountInRefundSuccessNotify(refundid, RETURN_CHARGE_TO_PAYER_TRUE, expectedchargeamountRefundSuccessNotify);
    }

    @Owner(CHAKSHU)
    @Feature("PG2-7164")
    @Test(description = "Validate PPBL NB FULL PG2 TRAFFIC ENABLED COP MDRPCF 101 product code with txn SLAB and refund with chargeamount Rs. 1.0 + 2.0 % for transaction amount between 1000.0 to 2000.0 with upperlimit : true ")
    public void validateThirdTxnSlabFullPG2TrafficCOPEnabledMDRPCFPPBLNBTxn() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String txnamount = "2000";
        Constants.MerchantType merchant = Constants.MerchantType.FULL_PG2_TRAFFIC_ENABLED_COP_MDRPCF_TXN_SLAB_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnamount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, Constants.Bank.PPBL.toString(), "false");
        String mid = merchant.getId();
        String orderId = initTxnDTO.getBody().getOrderId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode(Constants.Bank.PPBL.toString())
                .setAuthMode("USRPWD")
                .setMpin("1234")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponseForm(ptcResponse);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        String esn = ptcResponse.getBody().getBankForm().getRedirectForm().getContent().getExtSerialNo();
        System.out.println("Esn is " + esn);
        Assertions.assertThat(esn.length()).isEqualTo(19);

        NativeHelpers.submitJsonFormInBrowser(ptcResponse.toString());
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(mid)
                .validateOrderId(orderId)
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

        String expectedChargeAmount = validatePCFChargeAmount(txnamount,2.0, 1.0);

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_SUCCESS.getRespMsg())
                .validatePaymentMode("NB")
                .validateGatewayName(Constants.Gateway.PPBL.toString())
                .validateMid(mid)
                .validateChargeAmount(expectedChargeAmount)
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
        Assertions.assertThat(response.jsonPath().getString("body.txnInfo.CHARGEAMOUNT")).isEqualTo(expectedChargeAmount);

        ValidateBossChargeBatchFeeConsult(orderId, MDRPCF_PRODUCT_CODE, expectedChargeAmount);

//        String expectedChargeAmountNotify = String.valueOf((int)(Double.parseDouble(expectedChargeAmount)*100));
//        String expectedChargeAmountNotifyString =  NOTIFY_STRING +  expectedChargeAmountNotify;
//        ValidateChargeAmountInPaymentNotify(orderId, MDRPCF_PRODUCT_CODE, PAYER_CHARGE_TARGET, expectedChargeAmountNotifyString);

        //passing txnType as "R" for returning charge amount
        SyncRefund refundWithChargeAmount = new SyncRefund();
        Response asyncRefundResp = given().spec(refundWithChargeAmount.reqSpecAsyncRefundtxnType(merchant, initTxnDTO.txnAmountFromBody(), orderId,
                        txnStatus.getResponse().getTXNID(), "R"))
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

        String refundid = asyncRefundResp.jsonPath().getString("body.refundId");
        String expectedRefundWithChargeAmount = String.valueOf((int)((Double.parseDouble(txnamount) + Double.parseDouble(expectedChargeAmount))*100));

        String expectedrefundamountstring = REFUND_AMOUNT_STRING +  expectedRefundWithChargeAmount;
        ValidateRefundWithChargeInInstaProxy(refundid, expectedrefundamountstring);

//        String expectedchargeamountRefundSuccessNotify = NOTIFY_STRING +  expectedChargeAmountNotify;
//        ValidateChargeAmountInRefundSuccessNotify(refundid, RETURN_CHARGE_TO_PAYER_TRUE, expectedchargeamountRefundSuccessNotify);
    }

    @Owner(CHAKSHU)
    @Feature("PG2-7164")
    @Test(description = "Validate PPBL NB FULL PG2 TRAFFIC ENABLED COP MDRPCF 101 product code with txn SLAB and refund with chargeamount Rs. 1.0 + 3.0 % for transaction amount > 2000.0  ")
    public void validateFourthTxnSlabFullPG2TrafficCOPEnabledMDRPCFPPBLNBTxn() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String txnamount = "2001";
        Constants.MerchantType merchant = Constants.MerchantType.FULL_PG2_TRAFFIC_ENABLED_COP_MDRPCF_TXN_SLAB_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnamount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, Constants.Bank.PPBL.toString(), "false");
        String mid = merchant.getId();
        String orderId = initTxnDTO.getBody().getOrderId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode(Constants.Bank.PPBL.toString())
                .setAuthMode("USRPWD")
                .setMpin("1234")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponseForm(ptcResponse);
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
                .validateOrderId(orderId)
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

        String expectedChargeAmount = validatePCFChargeAmount(txnamount,3.0, 1.0);

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_SUCCESS.getRespMsg())
                .validatePaymentMode("NB")
                .validateGatewayName(Constants.Gateway.PPBL.toString())
                .validateMid(mid)
                .validateChargeAmount(expectedChargeAmount)
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
        Assertions.assertThat(response.jsonPath().getString("body.txnInfo.CHARGEAMOUNT")).isEqualTo(expectedChargeAmount);

        ValidateBossChargeBatchFeeConsult(orderId, MDRPCF_PRODUCT_CODE, expectedChargeAmount);

//        String expectedChargeAmountNotify = String.valueOf((int)(Double.parseDouble(expectedChargeAmount)*100));
//        String expectedChargeAmountNotifyString =  NOTIFY_STRING +  expectedChargeAmountNotify;
//        ValidateChargeAmountInPaymentNotify(orderId, MDRPCF_PRODUCT_CODE, PAYER_CHARGE_TARGET, expectedChargeAmountNotifyString);

        //passing txnType as "R" for returning charge amount
        SyncRefund refundWithChargeAmount = new SyncRefund();
        Response asyncRefundResp = given().spec(refundWithChargeAmount.reqSpecAsyncRefundtxnType(merchant, initTxnDTO.txnAmountFromBody(), orderId,
                        txnStatus.getResponse().getTXNID(), "R"))
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

        String refundid = asyncRefundResp.jsonPath().getString("body.refundId");
        String expectedRefundWithChargeAmount = String.valueOf((int)((Double.parseDouble(txnamount) + Double.parseDouble(expectedChargeAmount))*100));

        String expectedrefundamountstring = REFUND_AMOUNT_STRING +  expectedRefundWithChargeAmount;
        ValidateRefundWithChargeInInstaProxy(refundid, expectedrefundamountstring);

//        String expectedchargeamountRefundSuccessNotify = NOTIFY_STRING +  expectedChargeAmountNotify;
//        ValidateChargeAmountInRefundSuccessNotify(refundid, RETURN_CHARGE_TO_PAYER_TRUE, expectedchargeamountRefundSuccessNotify);
    }

    @Owner(CHAKSHU)
    @Feature("PG2-7165")
    @Test(description = "Validate UPI Push Express FULL PG2 TRAFFIC ENABLED COTP PCF 02 product code with Platform fee & txn SLAB and refund with chargeamount Rs. 1.0 + 1.0 % for transaction amount <= 1000.0 where upperlimit : true ")
    public void validateFirstTxnSlabWithPlatformfeeCOTPFullPG2TrafficEnabledPCFUPIPushExpressTxn() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String txnamount = "100";
        Constants.MerchantType merchant = Constants.MerchantType.FULL_PG2_TRAFFIC_ENABLED_COTP_PCF_PLATFORM_TXN_SLAB_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnamount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        String mid = merchant.getId();
        String orderId = initTxnDTO.getBody().getOrderId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(), orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setPreferredOtpPage("bank")
                .setAuthMode("USRPWD")
                .setChannelCode("push")
                .setChannelId("WAP")
                .setMpin(mpin)
                .setRiskExtendInfo(riskExtendedInfo)
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse, initTxnDTO);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPCODE()).isEqualTo("01");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getSTATUS()).isEqualTo(Constants.TXNSTATUS.TXN_SUCCESS.toString());

        CommonHelpers.verifyEsnLength(initTxnDTO.getBody().getOrderId(),19);
        String txnChargAmount = validatePCFChargeAmount(txnamount,1.0, 1.0);
        String platformChargAmount = validatePCFChargeAmount(txnamount,2.0, 1.0);
        String expectedChargeAmount = String.valueOf(CommonHelpers.doubleHalfUpConvertor(Double.parseDouble(txnChargAmount) + Double.parseDouble(platformChargAmount)));

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PPBEX.toString())
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
        Assertions.assertThat(response.jsonPath().getString("body.txnInfo.CHARGEAMOUNT")).isEqualTo(expectedChargeAmount);

        ValidateBossChargeBatchFeeConsult(orderId, PCF_PRODUCT_CODE, expectedChargeAmount);

//        String expectedChargeAmountNotify = String.valueOf((int)(Double.parseDouble(expectedChargeAmount)*100));
//        String expectedChargeAmountNotifyString =  NOTIFY_STRING +  expectedChargeAmountNotify;
//        ValidateChargeAmountInPaymentNotify(orderId, PCF_PRODUCT_CODE, PAYER_CHARGE_TARGET, expectedChargeAmountNotifyString);

        //passing txnType as "R" for returning charge amount
        SyncRefund refundWithChargeAmount = new SyncRefund();
        Response asyncRefundResp = given().spec(refundWithChargeAmount.reqSpecAsyncRefundtxnType(merchant, initTxnDTO.txnAmountFromBody(), orderId,
                        txnStatus.getResponse().getTXNID(), "R"))
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

        String refundid = asyncRefundResp.jsonPath().getString("body.refundId");
        String expectedRefundWithChargeAmount = String.valueOf((int)((Double.parseDouble(txnamount) + Double.parseDouble(expectedChargeAmount))*100));

        String expectedrefundamountstring = REFUND_AMOUNT_STRING +  expectedRefundWithChargeAmount;
        ValidateRefundWithChargeInInstaProxy(refundid, expectedrefundamountstring);

    }

    @Owner(CHAKSHU)
    @Feature("PG2-7165")
    @Test(description = "Validate UPI Push Express FULL PG2 TRAFFIC ENABLED COTP PCF 02 product code with Platform fee & txn SLAB and refund with chargeamount Rs. 1.0 + 2.0 % for transaction amount between 1000.0 to 2000.0 ")
    public void validateSecondTxnSlabWithPlatformfeeCOTPFullPG2TrafficEnabledPCFUPIPushExpressTxn() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String txnamount = "1010";
        Constants.MerchantType merchant = Constants.MerchantType.FULL_PG2_TRAFFIC_ENABLED_COTP_PCF_PLATFORM_TXN_SLAB_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnamount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        String mid = merchant.getId();
        String orderId = initTxnDTO.getBody().getOrderId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setPreferredOtpPage("bank")
                .setAuthMode("USRPWD")
                .setChannelCode("push")
                .setChannelId("WAP")
                .setMpin(mpin)
                .setRiskExtendInfo(riskExtendedInfo)
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse, initTxnDTO);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPCODE()).isEqualTo("01");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getSTATUS()).isEqualTo(Constants.TXNSTATUS.TXN_SUCCESS.toString());

        CommonHelpers.verifyEsnLength(initTxnDTO.getBody().getOrderId(),19);
        String txnChargAmount = validatePCFChargeAmount(txnamount,2.0, 1.0);
        String platformChargAmount = validatePCFChargeAmount(txnamount,2.0, 1.0);
        String expectedChargeAmount = String.valueOf(CommonHelpers.doubleHalfUpConvertor(Double.parseDouble(txnChargAmount) + Double.parseDouble(platformChargAmount)));

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PPBEX.toString())
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
        Assertions.assertThat(response.jsonPath().getString("body.txnInfo.CHARGEAMOUNT")).isEqualTo(expectedChargeAmount);

        ValidateBossChargeBatchFeeConsult(orderId, PCF_PRODUCT_CODE, expectedChargeAmount);

//        String expectedChargeAmountNotify = String.valueOf((int)(Double.parseDouble(expectedChargeAmount)*100));
//        String expectedChargeAmountNotifyString =  NOTIFY_STRING +  expectedChargeAmountNotify;
//        ValidateChargeAmountInPaymentNotify(orderId, PCF_PRODUCT_CODE, PAYER_CHARGE_TARGET, expectedChargeAmountNotifyString);

        //passing txnType as "R" for returning charge amount
        SyncRefund refundWithChargeAmount = new SyncRefund();
        Response asyncRefundResp = given().spec(refundWithChargeAmount.reqSpecAsyncRefundtxnType(merchant, initTxnDTO.txnAmountFromBody(), orderId,
                        txnStatus.getResponse().getTXNID(), "R"))
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

        String refundid = asyncRefundResp.jsonPath().getString("body.refundId");
        String expectedRefundWithChargeAmount = String.valueOf((int)((Double.parseDouble(txnamount) + Double.parseDouble(expectedChargeAmount))*100));

        String expectedrefundamountstring = REFUND_AMOUNT_STRING +  expectedRefundWithChargeAmount;
        ValidateRefundWithChargeInInstaProxy(refundid, expectedrefundamountstring);
    }

    @Owner(CHAKSHU)
    @Feature("PG2-7165")
    @Test(description = "Validate UPI Push Express FULL PG2 TRAFFIC ENABLED COTP PCF 02 product code with Platform fee & txn SLAB and refund with chargeamount Rs. 1.0 + 2.0 % for transaction amount between 1000.0 to 2000.0 where upperlimit : true ")
    public void validateThirdTxnSlabWithPlatformfeeCOTPFullPG2TrafficEnabledPCFUPIPushExpressTxn() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String txnamount = "2000";
        Constants.MerchantType merchant = Constants.MerchantType.FULL_PG2_TRAFFIC_ENABLED_COTP_PCF_PLATFORM_TXN_SLAB_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnamount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        String mid = merchant.getId();
        String orderId = initTxnDTO.getBody().getOrderId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setPreferredOtpPage("bank")
                .setAuthMode("USRPWD")
                .setChannelCode("push")
                .setChannelId("WAP")
                .setMpin(mpin)
                .setRiskExtendInfo(riskExtendedInfo)
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse, initTxnDTO);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPCODE()).isEqualTo("01");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getSTATUS()).isEqualTo(Constants.TXNSTATUS.TXN_SUCCESS.toString());

        CommonHelpers.verifyEsnLength(initTxnDTO.getBody().getOrderId(),19);
        String txnChargAmount = validatePCFChargeAmount(txnamount,2.0, 1.0);
        String platformChargAmount = validatePCFChargeAmount(txnamount,2.0, 1.0);
        String expectedChargeAmount = String.valueOf(CommonHelpers.doubleHalfUpConvertor(Double.parseDouble(txnChargAmount) + Double.parseDouble(platformChargAmount)));

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PPBEX.toString())
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
        Assertions.assertThat(response.jsonPath().getString("body.txnInfo.CHARGEAMOUNT")).isEqualTo(expectedChargeAmount);

        ValidateBossChargeBatchFeeConsult(orderId, PCF_PRODUCT_CODE, expectedChargeAmount);

//        String expectedChargeAmountNotify = String.valueOf((int)(Double.parseDouble(expectedChargeAmount)*100));
//        String expectedChargeAmountNotifyString =  NOTIFY_STRING +  expectedChargeAmountNotify;
//        ValidateChargeAmountInPaymentNotify(orderId, PCF_PRODUCT_CODE, PAYER_CHARGE_TARGET, expectedChargeAmountNotifyString);

        //passing txnType as "R" for returning charge amount
        SyncRefund refundWithChargeAmount = new SyncRefund();
        Response asyncRefundResp = given().spec(refundWithChargeAmount.reqSpecAsyncRefundtxnType(merchant, initTxnDTO.txnAmountFromBody(), orderId,
                        txnStatus.getResponse().getTXNID(), "R"))
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

        String refundid = asyncRefundResp.jsonPath().getString("body.refundId");
        String expectedRefundWithChargeAmount = String.valueOf((int)((Double.parseDouble(txnamount) + Double.parseDouble(expectedChargeAmount))*100));

        String expectedrefundamountstring = REFUND_AMOUNT_STRING +  expectedRefundWithChargeAmount;
        ValidateRefundWithChargeInInstaProxy(refundid, expectedrefundamountstring);
    }

    @Owner(CHAKSHU)
    @Feature("PG2-7165")
    @Test(description = "Validate UPI Push Express FULL PG2 TRAFFIC ENABLED COTP PCF 02 product code with Platform fee & txn SLAB and refund with chargeamount Rs. 1.0 + 3.0 % for transaction amount> 2000.0 ")
    public void validateFourthTxnSlabWithPlatformfeeCOTPFullPG2TrafficEnabledPCFUPIPushExpressTxn() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String txnamount = "2001";
        Constants.MerchantType merchant = Constants.MerchantType.FULL_PG2_TRAFFIC_ENABLED_COTP_PCF_PLATFORM_TXN_SLAB_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnamount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        String mid = merchant.getId();
        String orderId = initTxnDTO.getBody().getOrderId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setPreferredOtpPage("bank")
                .setAuthMode("USRPWD")
                .setChannelCode("push")
                .setChannelId("WAP")
                .setMpin(mpin)
                .setRiskExtendInfo(riskExtendedInfo)
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse, initTxnDTO);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPCODE()).isEqualTo("01");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getSTATUS()).isEqualTo(Constants.TXNSTATUS.TXN_SUCCESS.toString());

        CommonHelpers.verifyEsnLength(initTxnDTO.getBody().getOrderId(),19);
        String txnChargAmount = validatePCFChargeAmount(txnamount,3.0, 1.0);
        String platformChargAmount = validatePCFChargeAmount(txnamount,2.0, 1.0);
        String expectedChargeAmount = String.valueOf(CommonHelpers.doubleHalfUpConvertor(Double.parseDouble(txnChargAmount) + Double.parseDouble(platformChargAmount)));

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PPBEX.toString())
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
        Assertions.assertThat(response.jsonPath().getString("body.txnInfo.CHARGEAMOUNT")).isEqualTo(expectedChargeAmount);

        ValidateBossChargeBatchFeeConsult(orderId, PCF_PRODUCT_CODE, expectedChargeAmount);

//        String expectedChargeAmountNotify = String.valueOf((int)(Double.parseDouble(expectedChargeAmount)*100));
//        String expectedChargeAmountNotifyString =  NOTIFY_STRING +  expectedChargeAmountNotify;
//        ValidateChargeAmountInPaymentNotify(orderId, PCF_PRODUCT_CODE, PAYER_CHARGE_TARGET, expectedChargeAmountNotifyString);

        //passing txnType as "R" for returning charge amount
        SyncRefund refundWithChargeAmount = new SyncRefund();
        Response asyncRefundResp = given().spec(refundWithChargeAmount.reqSpecAsyncRefundtxnType(merchant, initTxnDTO.txnAmountFromBody(), orderId,
                        txnStatus.getResponse().getTXNID(), "R"))
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

        String refundid = asyncRefundResp.jsonPath().getString("body.refundId");
        String expectedRefundWithChargeAmount = String.valueOf((int)((Double.parseDouble(txnamount) + Double.parseDouble(expectedChargeAmount))*100));

        String expectedrefundamountstring = REFUND_AMOUNT_STRING +  expectedRefundWithChargeAmount;
        ValidateRefundWithChargeInInstaProxy(refundid, expectedrefundamountstring);
    }

    @Owner(CHAKSHU)
    @Feature("PG2-7165")
    @Test(description = "Validate Postpaid FULL PG2 TRAFFIC ENABLED COTP MDRPCF 101 product code with Platform fee as MDR & txn SLAB and refund with chargeamount Rs. 1.0 + 1.0 % for transaction amount <= 1000.0 where upperlimit : true ")
    public void validateFirstTxnSlabWithPlatformfeeAsMDRCOTPFullPG2TrafficEnabledMDRPCFPostPaidTxn() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String txnamount = "1000";
        Constants.MerchantType merchant = Constants.MerchantType.FULL_PG2_TRAFFIC_ENABLED_COTP_MDRPCF_PLATFORM_TXN_SLAB_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnamount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "PAYTM_DIGITAL_CREDIT", "false");
        String mid = merchant.getId();
        String orderId = initTxnDTO.getBody().getOrderId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid, initTxnResponse.getBody().getTxnToken(), orderId, "")
                .setPaymentMode("PAYTM_DIGITAL_CREDIT")
                .setAuthMode("3D")
                .setChannelId("WEB")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse, initTxnDTO);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getPAYMENTMODE()).isEqualTo("Paytm Postpaid");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPCODE()).isEqualTo("01");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getSTATUS()).isEqualTo(Constants.TXNSTATUS.TXN_SUCCESS.toString());

        CommonHelpers.verifyEsnLength(initTxnDTO.getBody().getOrderId(),19);
        String expectedChargeAmount = validatePCFChargeAmount(txnamount,1.0, 1.0);

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("Paytm Postpaid")
                .validateGatewayName(Constants.Gateway.PAYTMCC.toString())
                .validateMid(mid)
                .validateChargeAmount(expectedChargeAmount)
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
        Assertions.assertThat(response.jsonPath().getString("body.txnInfo.CHARGEAMOUNT")).isEqualTo(expectedChargeAmount);

        ValidateBossChargeBatchFeeConsult(orderId, MDRPCF_PRODUCT_CODE, expectedChargeAmount);

//        String expectedChargeAmountNotify = String.valueOf((int)(Double.parseDouble(expectedChargeAmount)*100));
//        String expectedChargeAmountNotifyString =  NOTIFY_STRING +  expectedChargeAmountNotify;
//        ValidateChargeAmountInPaymentNotify(orderId, MDRPCF_PRODUCT_CODE, PAYER_CHARGE_TARGET, expectedChargeAmountNotifyString);

        //passing txnType as "R" for returning charge amount
        SyncRefund refundWithChargeAmount = new SyncRefund();
        Response asyncRefundResp = given().spec(refundWithChargeAmount.reqSpecAsyncRefundtxnType(merchant, initTxnDTO.txnAmountFromBody(), orderId,
                        txnStatus.getResponse().getTXNID(), "R"))
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

        String refundid = asyncRefundResp.jsonPath().getString("body.refundId");
        String expectedRefundWithChargeAmount = String.valueOf((int)((Double.parseDouble(txnamount) + Double.parseDouble(expectedChargeAmount))*100));

        String expectedrefundamountstring = REFUND_AMOUNT_STRING +  expectedRefundWithChargeAmount;
        ValidateRefundWithChargeInInstaProxy(refundid, expectedrefundamountstring);

//        String expectedchargeamountRefundSuccessNotify = NOTIFY_STRING +  expectedChargeAmountNotify;
//        ValidateChargeAmountInRefundSuccessNotify(refundid, RETURN_CHARGE_TO_PAYER_TRUE, expectedchargeamountRefundSuccessNotify);
    }

    @Owner(CHAKSHU)
    @Feature("PG2-7165")
    @Test(description = "Validate Postpaid FULL PG2 TRAFFIC ENABLED COTP MDRPCF 101 product code with Platform fee as MDR & txn SLAB and refund with chargeamount Rs. 1.0 + 2.0 % for transaction amount between 1000.0 to 2000.0 ")
    public void validateSecondTxnSlabWithPlatformfeeAsMDRCOTPFullPG2TrafficEnabledMDRPCFPostPaidTxn() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String txnamount = "1010";
        Constants.MerchantType merchant = Constants.MerchantType.FULL_PG2_TRAFFIC_ENABLED_COTP_MDRPCF_PLATFORM_TXN_SLAB_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnamount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "PAYTM_DIGITAL_CREDIT", "false");
        String mid = merchant.getId();
        String orderId = initTxnDTO.getBody().getOrderId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid, initTxnResponse.getBody().getTxnToken(), orderId, "")
                .setPaymentMode("PAYTM_DIGITAL_CREDIT")
                .setAuthMode("3D")
                .setChannelId("WEB")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse, initTxnDTO);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getPAYMENTMODE()).isEqualTo("Paytm Postpaid");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPCODE()).isEqualTo("01");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getSTATUS()).isEqualTo(Constants.TXNSTATUS.TXN_SUCCESS.toString());

        CommonHelpers.verifyEsnLength(initTxnDTO.getBody().getOrderId(),19);
        String expectedChargeAmount = validatePCFChargeAmount(txnamount,2.0, 1.0);

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("Paytm Postpaid")
                .validateGatewayName(Constants.Gateway.PAYTMCC.toString())
                .validateMid(mid)
                .validateChargeAmount(expectedChargeAmount)
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
        Assertions.assertThat(response.jsonPath().getString("body.txnInfo.CHARGEAMOUNT")).isEqualTo(expectedChargeAmount);

        ValidateBossChargeBatchFeeConsult(orderId, MDRPCF_PRODUCT_CODE, expectedChargeAmount);

//        String expectedChargeAmountNotify = String.valueOf((int)(Double.parseDouble(expectedChargeAmount)*100));
//        String expectedChargeAmountNotifyString =  NOTIFY_STRING +  expectedChargeAmountNotify;
//        ValidateChargeAmountInPaymentNotify(orderId, MDRPCF_PRODUCT_CODE, PAYER_CHARGE_TARGET, expectedChargeAmountNotifyString);

        //passing txnType as "R" for returning charge amount
        SyncRefund refundWithChargeAmount = new SyncRefund();
        Response asyncRefundResp = given().spec(refundWithChargeAmount.reqSpecAsyncRefundtxnType(merchant, initTxnDTO.txnAmountFromBody(), orderId,
                        txnStatus.getResponse().getTXNID(), "R"))
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

        String refundid = asyncRefundResp.jsonPath().getString("body.refundId");
        String expectedRefundWithChargeAmount = String.valueOf((int)((Double.parseDouble(txnamount) + Double.parseDouble(expectedChargeAmount))*100));

        String expectedrefundamountstring = REFUND_AMOUNT_STRING +  expectedRefundWithChargeAmount;
        ValidateRefundWithChargeInInstaProxy(refundid, expectedrefundamountstring);

//        String expectedchargeamountRefundSuccessNotify = NOTIFY_STRING +  expectedChargeAmountNotify;
//        ValidateChargeAmountInRefundSuccessNotify(refundid, RETURN_CHARGE_TO_PAYER_TRUE, expectedchargeamountRefundSuccessNotify);
    }

    @Owner(CHAKSHU)
    @Feature("PG2-7165")
    @Test(description = "Validate Postpaid FULL PG2 TRAFFIC ENABLED COTP MDRPCF 101 product code with Platform fee as MDR & txn SLAB and refund with chargeamount Rs. 1.0 + 2.0 % for transaction amount between 1000.0 to 2000.0 where upperlimit : true ")
    public void validateThirdTxnSlabWithPlatformfeeAsMDRCOTPFullPG2TrafficEnabledMDRPCFPostPaidTxn() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String txnamount = "2000";
        Constants.MerchantType merchant = Constants.MerchantType.FULL_PG2_TRAFFIC_ENABLED_COTP_MDRPCF_PLATFORM_TXN_SLAB_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnamount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "PAYTM_DIGITAL_CREDIT", "false");
        String mid = merchant.getId();
        String orderId = initTxnDTO.getBody().getOrderId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid, initTxnResponse.getBody().getTxnToken(), orderId, "")
                .setPaymentMode("PAYTM_DIGITAL_CREDIT")
                .setAuthMode("3D")
                .setChannelId("WEB")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse, initTxnDTO);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getPAYMENTMODE()).isEqualTo("Paytm Postpaid");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPCODE()).isEqualTo("01");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getSTATUS()).isEqualTo(Constants.TXNSTATUS.TXN_SUCCESS.toString());

        CommonHelpers.verifyEsnLength(initTxnDTO.getBody().getOrderId(),19);
        String expectedChargeAmount = validatePCFChargeAmount(txnamount,2.0, 1.0);

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("Paytm Postpaid")
                .validateGatewayName(Constants.Gateway.PAYTMCC.toString())
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
        Assertions.assertThat(response.jsonPath().getString("body.txnInfo.CHARGEAMOUNT")).isEqualTo(expectedChargeAmount);

        ValidateBossChargeBatchFeeConsult(orderId, MDRPCF_PRODUCT_CODE, expectedChargeAmount);

//        String expectedChargeAmountNotify = String.valueOf((int)(Double.parseDouble(expectedChargeAmount)*100));
//        String expectedChargeAmountNotifyString =  NOTIFY_STRING +  expectedChargeAmountNotify;
//        ValidateChargeAmountInPaymentNotify(orderId, MDRPCF_PRODUCT_CODE, PAYER_CHARGE_TARGET, expectedChargeAmountNotifyString);

        //passing txnType as "R" for returning charge amount
        SyncRefund refundWithChargeAmount = new SyncRefund();
        Response asyncRefundResp = given().spec(refundWithChargeAmount.reqSpecAsyncRefundtxnType(merchant, initTxnDTO.txnAmountFromBody(), orderId,
                        txnStatus.getResponse().getTXNID(), "R"))
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

        String refundid = asyncRefundResp.jsonPath().getString("body.refundId");
        String expectedRefundWithChargeAmount = String.valueOf((int)((Double.parseDouble(txnamount) + Double.parseDouble(expectedChargeAmount))*100));

        String expectedrefundamountstring = REFUND_AMOUNT_STRING +  expectedRefundWithChargeAmount;
        ValidateRefundWithChargeInInstaProxy(refundid, expectedrefundamountstring);

//        String expectedchargeamountRefundSuccessNotify = NOTIFY_STRING +  expectedChargeAmountNotify;
//        ValidateChargeAmountInRefundSuccessNotify(refundid, RETURN_CHARGE_TO_PAYER_TRUE, expectedchargeamountRefundSuccessNotify);
    }

    @Owner(CHAKSHU)
    @Feature("PG2-7165")
    @Test(description = "Validate Postpaid FULL PG2 TRAFFIC ENABLED COTP MDRPCF 101 product code with Platform fee as MDR & txn SLAB and refund with chargeamount Rs. 1.0 + 3.0 % for transaction amount > 2000.0 ")
    public void validateFourthTxnSlabWithPlatformfeeAsMDRCOTPFullPG2TrafficEnabledMDRPCFPostPaidTxn() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String txnamount = "2001";
        Constants.MerchantType merchant = Constants.MerchantType.FULL_PG2_TRAFFIC_ENABLED_COTP_MDRPCF_PLATFORM_TXN_SLAB_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnamount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "PAYTM_DIGITAL_CREDIT", "false");
        String mid = merchant.getId();
        String orderId = initTxnDTO.getBody().getOrderId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid, initTxnResponse.getBody().getTxnToken(), orderId, "")
                .setPaymentMode("PAYTM_DIGITAL_CREDIT")
                .setAuthMode("3D")
                .setChannelId("WEB")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse, initTxnDTO);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getPAYMENTMODE()).isEqualTo("Paytm Postpaid");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPCODE()).isEqualTo("01");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getSTATUS()).isEqualTo(Constants.TXNSTATUS.TXN_SUCCESS.toString());

        CommonHelpers.verifyEsnLength(initTxnDTO.getBody().getOrderId(),19);
        String expectedChargeAmount = validatePCFChargeAmount(txnamount,3.0, 1.0);

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("Paytm Postpaid")
                .validateGatewayName(Constants.Gateway.PAYTMCC.toString())
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
        Assertions.assertThat(response.jsonPath().getString("body.txnInfo.CHARGEAMOUNT")).isEqualTo(expectedChargeAmount);

        ValidateBossChargeBatchFeeConsult(orderId, MDRPCF_PRODUCT_CODE, expectedChargeAmount);

//        String expectedChargeAmountNotify = String.valueOf((int)(Double.parseDouble(expectedChargeAmount)*100));
//        String expectedChargeAmountNotifyString =  NOTIFY_STRING +  expectedChargeAmountNotify;
//        ValidateChargeAmountInPaymentNotify(orderId, MDRPCF_PRODUCT_CODE, PAYER_CHARGE_TARGET, expectedChargeAmountNotifyString);

        //passing txnType as "R" for returning charge amount
        SyncRefund refundWithChargeAmount = new SyncRefund();
        Response asyncRefundResp = given().spec(refundWithChargeAmount.reqSpecAsyncRefundtxnType(merchant, initTxnDTO.txnAmountFromBody(), orderId,
                        txnStatus.getResponse().getTXNID(), "R"))
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

        String refundid = asyncRefundResp.jsonPath().getString("body.refundId");
        String expectedRefundWithChargeAmount = String.valueOf((int)((Double.parseDouble(txnamount) + Double.parseDouble(expectedChargeAmount))*100));

        String expectedrefundamountstring = REFUND_AMOUNT_STRING +  expectedRefundWithChargeAmount;
        ValidateRefundWithChargeInInstaProxy(refundid, expectedrefundamountstring);

//        String expectedchargeamountRefundSuccessNotify = NOTIFY_STRING +  expectedChargeAmountNotify;
//        ValidateChargeAmountInRefundSuccessNotify(refundid, RETURN_CHARGE_TO_PAYER_TRUE, expectedchargeamountRefundSuccessNotify);
    }

    @Owner(CHAKSHU)
    @Feature("PG2-7164")
    @Test(description = "Validate Wallet FULL PG2 TRAFFIC ENABLED COP PCF 02 product code with txn SLAB and refund with chargeamount Rs. 1.0 + 1.0 % for transaction amount <= 1000.0 ")
    public void validateTxnSlabFullPG2TrafficCOPEnabledPCFWalletTxn() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        WalletHelpers.modifyBalance(user,1000.0);
        String txnamount = "100";
        Constants.MerchantType merchant = Constants.MerchantType.FULL_PG2_TRAFFIC_ENABLED_COP_PCF_TXN_SLAB_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnamount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "BALANCE", "false");
        String mid = merchant.getId();
        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(), initTxnDTO.getBody().getOrderId(), "")
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

        CommonHelpers.verifyEsnLength(initTxnDTO.getBody().getOrderId(),19);
        String expectedChargeAmount = validatePCFChargeAmount(txnamount,1.0, 1.0);

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
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
        Assertions.assertThat(response.jsonPath().getString("body.txnInfo.CHARGEAMOUNT")).isEqualTo(expectedChargeAmount);

        ValidateBossChargeBatchFeeConsult(orderId, PCF_PRODUCT_CODE, expectedChargeAmount);

//        String expectedChargeAmountNotify = String.valueOf((int)(Double.parseDouble(expectedChargeAmount)*100));
//        String expectedChargeAmountNotifyString =  NOTIFY_STRING +  expectedChargeAmountNotify;
//        ValidateChargeAmountInPaymentNotify(orderId, PCF_PRODUCT_CODE, PAYER_CHARGE_TARGET, expectedChargeAmountNotifyString);

        //passing txnType as "R" for returning charge amount
        SyncRefund refundWithChargeAmount = new SyncRefund();
        Response asyncRefundResp = given().spec(refundWithChargeAmount.reqSpecAsyncRefundtxnType(merchant, initTxnDTO.txnAmountFromBody(), orderId,
                        txnStatus.getResponse().getTXNID(), "R"))
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

        String refundid = asyncRefundResp.jsonPath().getString("body.refundId");
        String expectedRefundWithChargeAmount = String.valueOf((int)((Double.parseDouble(txnamount) + Double.parseDouble(expectedChargeAmount))*100));

        String expectedrefundamountstring = REFUND_AMOUNT_STRING +  expectedRefundWithChargeAmount;
        ValidateRefundWithChargeInInstaProxy(refundid, expectedrefundamountstring);

//        String expectedchargeamountRefundSuccessNotify = NOTIFY_STRING +  expectedChargeAmountNotify;
//        ValidateChargeAmountInRefundSuccessNotify(refundid, RETURN_CHARGE_TO_PAYER_TRUE, expectedchargeamountRefundSuccessNotify);
    }

    @Owner(CHAKSHU)
    @Feature("PG2-7281")
    @Test(description = "Validate pcfdetail api for PCF 02 productcode with txn slab & platform fee for full traffic PG2, FEE_BATCH_CONSULT will route to PG2 ")
    public void validatePCFDetailApiForPcfFullTrafficPG2Mid() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String txnamount = "1000";
        Constants.MerchantType merchant = Constants.MerchantType.FULL_PG2_TRAFFIC_ENABLED_COTP_PCF_PLATFORM_TXN_SLAB_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnamount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String payMethod= "CREDIT_CARD";
        String payMethod1= "DEBIT_CARD";
        String payMethod2= "UPI";
        String payMethod3= "NET_BANKING";
        String payMethod4= "PAYTM_DIGITAL_CREDIT";
        String payMethod5= "BALANCE";

        String txnChargAmount = validatePCFChargeAmount(txnamount,1.0, 1.0);
        String platformChargAmount = validatePCFChargeAmount(txnamount,2.0, 1.0);
        String expectedChargeAmount = String.valueOf(CommonHelpers.doubleHalfUpConvertor(Double.parseDouble(txnChargAmount) + Double.parseDouble(platformChargAmount)));

        String expectedTotalAmount = String.valueOf(Double.parseDouble(txnamount) + Double.parseDouble(expectedChargeAmount));

        //Creating fetchPcf request
        List<PayMethod> payMethods = new ArrayList<>();
        payMethods.add(new PayMethod().setInstId("").setPayMethod(payMethod));
        payMethods.add(new PayMethod().setInstId("").setPayMethod(payMethod1));
        payMethods.add(new PayMethod().setInstId("").setPayMethod(payMethod2));
        payMethods.add(new PayMethod().setInstId("").setPayMethod(payMethod3));
        payMethods.add(new PayMethod().setInstId("").setPayMethod(payMethod4));
        payMethods.add(new PayMethod().setInstId("").setPayMethod(payMethod5));
        FetchPcfRequest fetchPcfRequest = new FetchPcfRequest()
                .setHead(new com.paytm.dto.NativeDTO.fetchPcfDetail.Head().setTxnToken(txnToken))
                .setBody(new com.paytm.dto.NativeDTO.fetchPcfDetail.Body().setPayMethods(payMethods));
        FetchPcfDetail fetchPcfDetail = new FetchPcfDetail(fetchPcfRequest, initTxnDTO);
        JsonPath FJsonPath = fetchPcfDetail.execute().jsonPath();
        System.out.println(FJsonPath.toString());

        Assertions.assertThat(FJsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(FJsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(FJsonPath.getString("body.consultDetails."+payMethod+".totalTransactionAmount.value")).isEqualTo(expectedTotalAmount);
        Assertions.assertThat(FJsonPath.getString("body.consultDetails."+payMethod1+".totalTransactionAmount.value")).isEqualTo(expectedTotalAmount);
        Assertions.assertThat(FJsonPath.getString("body.consultDetails."+payMethod2+".totalTransactionAmount.value")).isEqualTo(expectedTotalAmount);
        Assertions.assertThat(FJsonPath.getString("body.consultDetails."+payMethod3+".totalTransactionAmount.value")).isEqualTo(expectedTotalAmount);
        Assertions.assertThat(FJsonPath.getString("body.consultDetails."+payMethod4+".totalTransactionAmount.value")).isEqualTo(expectedTotalAmount);
        Assertions.assertThat(FJsonPath.getString("body.consultDetails."+payMethod5+".totalTransactionAmount.value")).isEqualTo(expectedTotalAmount);
    }

    @Owner(CHAKSHU)
    @Feature("PG2-7281")
    @Test(description = "Validate pcfdetail api for MDRPCF 101 productcode with txn slab  for full traffic PG2, FEE_BATCH_CONSULT will route to PG2 ")
    public void validatePCFDetailApiForMDRPcfFullTrafficPG2Mid() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String txnamount = "1000";
        Constants.MerchantType merchant = Constants.MerchantType.FULL_PG2_TRAFFIC_ENABLED_COP_MDRPCF_TXN_SLAB_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnamount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String payMethod= "CREDIT_CARD";
        String payMethod1= "DEBIT_CARD";
        String payMethod2= "UPI";
        String payMethod3= "NET_BANKING";
        String payMethod4= "PAYTM_DIGITAL_CREDIT";
        String payMethod5= "BALANCE";

        String expectedChargeAmount = validatePCFChargeAmount(txnamount,1.0, 1.0);
        String expectedTotalAmount = String.valueOf(Double.parseDouble(txnamount) + Double.parseDouble(expectedChargeAmount));

        //Creating fetchPcf request
        List<PayMethod> payMethods = new ArrayList<>();
        payMethods.add(new PayMethod().setInstId("").setPayMethod(payMethod));
        payMethods.add(new PayMethod().setInstId("").setPayMethod(payMethod1));
        payMethods.add(new PayMethod().setInstId("").setPayMethod(payMethod2));
        payMethods.add(new PayMethod().setInstId("").setPayMethod(payMethod3));
        payMethods.add(new PayMethod().setInstId("").setPayMethod(payMethod4));
        payMethods.add(new PayMethod().setInstId("").setPayMethod(payMethod5));
        FetchPcfRequest fetchPcfRequest = new FetchPcfRequest()
                .setHead(new com.paytm.dto.NativeDTO.fetchPcfDetail.Head().setTxnToken(txnToken))
                .setBody(new com.paytm.dto.NativeDTO.fetchPcfDetail.Body().setPayMethods(payMethods));
        FetchPcfDetail fetchPcfDetail = new FetchPcfDetail(fetchPcfRequest, initTxnDTO);
        JsonPath FJsonPath = fetchPcfDetail.execute().jsonPath();
        System.out.println(FJsonPath.toString());

        Assertions.assertThat(FJsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(FJsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(FJsonPath.getString("body.consultDetails."+payMethod+".totalTransactionAmount.value")).isEqualTo(expectedTotalAmount);
        Assertions.assertThat(FJsonPath.getString("body.consultDetails."+payMethod1+".totalTransactionAmount.value")).isEqualTo(expectedTotalAmount);
        Assertions.assertThat(FJsonPath.getString("body.consultDetails."+payMethod2+".totalTransactionAmount.value")).isEqualTo(expectedTotalAmount);
        Assertions.assertThat(FJsonPath.getString("body.consultDetails."+payMethod3+".totalTransactionAmount.value")).isEqualTo(expectedTotalAmount);
        Assertions.assertThat(FJsonPath.getString("body.consultDetails."+payMethod4+".totalTransactionAmount.value")).isEqualTo(expectedTotalAmount);
        Assertions.assertThat(FJsonPath.getString("body.consultDetails."+payMethod5+".totalTransactionAmount.value")).isEqualTo(expectedTotalAmount);
    }
}