package scripts.api.Debitcard;

import com.paytm.LocalConfig;
import com.paytm.ServerConfigProvider;
import com.paytm.api.CloseOrderAPI;
import com.paytm.api.TxnStatus;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.api.refund.SyncRefund;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.LogsValidationHelper;
import com.paytm.apphelpers.SavedCardHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.CloseOrder.CloseOrderDTO;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.response.FetchPaymentOptResponseDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.checkoutjs.MerchantConfig;
import com.paytm.dto.processTransactionV1.response.ProcessTxnV1Response;
import com.paytm.framework.reporting.Reporter;
import com.paytm.pages.*;
import com.paytm.utils.merchant.util.PayMethodType;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import static com.paytm.appconstants.Constants.MerchantType.PG2_CC_PG2_ENABLED;
import static com.paytm.appconstants.Constants.Owner.HIMANSHU;
import static com.paytm.appconstants.Constants.Owner.VIKASH_VERMA;
import static com.paytm.apphelpers.PGPHelpers.assertRefundSuccessNotifyPresence;
import static io.restassured.RestAssured.given;


public class PG2Debitcard  extends PGPBaseTest {
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
    public void validateaddmoneyfundAcquiringOrderModify(String orderId) throws InterruptedException {
        String grepAcquiringOrderModify = "grep " + orderId + " " + LocalConfig.THEIA_FACADE_LOGS +
                " | grep \"CREATE_FUND_USER_TOPUP_FROM_MERCHANT\" | grep \"REQUEST\"";
        String acquringOrderModifyLogs = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepAcquiringOrderModify);
        Assertions.assertThat(acquringOrderModifyLogs).
                contains("\"route\":\"PG2\"");
    }
    public void validateaddmoneyfundpayOrderModify(String orderId) throws InterruptedException {
        String grepAcquiringOrderModify = "grep " + orderId + " " + LocalConfig.THEIA_FACADE_LOGS +
                " | grep \"FUND_ORDER_PAY\" | grep \"REQUEST\"";
        String acquringOrderModifyLogs = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepAcquiringOrderModify);
        Assertions.assertThat(acquringOrderModifyLogs).
                contains("\"route\":\"PG2\"");
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

    @Owner(VIKASH_VERMA)
    @Feature("PG2-7072")
    @Test(description = "Validate Success Debit card transaction for PG2 MID routed through PG2")
    public void validateSuccessDCTxnPG2MID() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.PG2_CC_DC;
        String mid = merchant.getId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue("100").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String orderID=initTxnDTO.getBody().getOrderId();
        String txnToken = initTxnResponse.getBody().getTxnToken();
        PaymentDTO paymentDTO = new PaymentDTO();
        String cardInfo = "|" +paymentDTO.DEBIT_CARD_NUMBER+"|"+paymentDTO.getCvvNumber() + "|" + paymentDTO.getExpMonth()+ paymentDTO.getExpYear();
        OrderDTO orderDTO = new OrderFactory.Native(merchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.DEBIT_CARD)
                .setMID(mid)
                .setORDER_ID(orderID)
                .setCardInfo(cardInfo)
                .setAUTH_MODE("otp")
                .setCHANNEL_ID("WAP").build();
        checkoutPage.createNativeOrder(orderDTO, true);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderId(orderDTO.getORDER_ID())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
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
                .validatePaymentMode("DC")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateMid(merchant.getId())
                .AssertAll();
        Assertions.assertThat(txnStatus.getResponse().getTXNID()).contains("0000");

    }

    @Owner(VIKASH_VERMA)
    @Feature("PG2-7072")
    @Test(description = "Validate Pending Debit card transaction for PG2 MID routed through PG2")
    public void validatePendingDCTxnPG2MID() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.PG2_CC_DC;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue("99.84").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String orderID=initTxnDTO.getBody().getOrderId();
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "DEBIT_CARD", "false");
        String mid = merchant.getId();
        PaymentDTO paymentDTO = new PaymentDTO();
        String cardInfo = "|" +paymentDTO.DEBIT_CARD_NUMBER+"|"+paymentDTO.getCvvNumber() + "|" + paymentDTO.getExpMonth()+ paymentDTO.getExpYear();
        OrderDTO orderDTO = new OrderFactory.Native(merchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.DEBIT_CARD)
                .setORDER_ID(orderID)
                .setCardInfo(cardInfo)
                .setAUTH_MODE("otp")
                .setCHANNEL_ID("WAP").build();

        checkoutPage.createNativeOrder(orderDTO, true);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderId(orderDTO.getORDER_ID())
                .validateStatus(Constants.TXNSTATUS.PENDING.toString())
                .validateRespCode(Constants.ResponseCode.TXN_STATUS_PENDING.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_STATUS_PENDING.getRespMsg());
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.PENDING.toString())
                .validateRespCode(Constants.ResponseCode.MERCHANT_STATUS_TXN_PENDING.getRespCode())
                .validateRespMsg(Constants.ResponseCode.MERCHANT_STATUS_TXN_PENDING.getRespMsg())
                .validatePaymentMode("DC")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
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
                .validateRespCode(Constants.ResponseCode.TXN_FAILURE.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_FAILURE.getRespMsg())
                .validatePaymentMode("DC")
                .validateMid(mid)
                .AssertAll();
        Assertions.assertThat(txnStatus.getResponse().getTXNID()).contains("0000");
    }

    @Owner(VIKASH_VERMA)
    @Feature("PG2-7072")
    @Test(description = "Validate Failed Debit card transaction for PG2 MID routed through PG2")
    public void validateFailedDCTxnPG2MID() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.PG2_CC_DC;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue("99.98").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderID=initTxnDTO.getBody().getOrderId();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "DEBIT_CARD", "false");
        String mid = merchant.getId();
        PaymentDTO paymentDTO = new PaymentDTO();
        String cardInfo = "|" +paymentDTO.DEBIT_CARD_NUMBER+"|"+paymentDTO.getCvvNumber() + "|" + paymentDTO.getExpMonth()+ paymentDTO.getExpYear();
        OrderDTO orderDTO = new OrderFactory.Native(merchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.DEBIT_CARD)
                .setMID(mid)
                .setORDER_ID(orderID)
                .setCardInfo(cardInfo)
                .setAUTH_MODE("otp")
                .setCHANNEL_ID("WAP").build();
        checkoutPage.createNativeOrder(orderDTO, true);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderId(orderDTO.getORDER_ID())
                .validateStatus("TXN_FAILURE")
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
                .validateRespCode(Constants.ResponseCode.BANK_TXN_FAILURE.getRespCode())
                .validateRespMsg(Constants.ResponseCode.BANK_TXN_FAILURE.getRespMsg())
                .validatePaymentMode("DC")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateMid(mid)
                .AssertAll();
        Assertions.assertThat(txnStatus.getResponse().getTXNID()).contains("0000");

    }
    @Owner(HIMANSHU)
    @Feature("PG2-7678")
    @Test(description = "Validate Success SAVED DC transaction for PG2 MID routed through PG2")
    public void validateSuccessSavedDCTxnPG2MID() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.PG2_CC_PG2_ENABLED;
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getDebitCardNumber());
        String cardId = SavedCardHelpers.getSavedCardId(user, 0);
        String cardInfo = cardId + "||" + paymentDTO.getCvvNumber() + "|";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.PG2_CC_PG2_ENABLED)
                .setCustId("Test102")
                .setTxnValue("2")
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(Constants.MerchantType.PG2_CC_PG2_ENABLED.getId(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        String mid = merchant.getId();
        String orderID=initTxnDTO.getBody().getOrderId();
//        PaymentDTO paymentDTO = new PaymentDTO();
//        String cardInfo = "|" +paymentDTO.DEBIT_CARD_NUMBER+"|"+paymentDTO.getCvvNumber() + "|" + paymentDTO.getExpMonth()+ paymentDTO.getExpYear();
        OrderDTO orderDTO = new OrderFactory.Native(merchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.DEBIT_CARD)
                .setMID(mid)
                .setORDER_ID(orderID)
                .setCardInfo(cardInfo)
                .setAUTH_MODE("otp")
                .setCHANNEL_ID("WAP").build();
        checkoutPage.createNativeOrder(orderDTO, true);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderId(orderDTO.getORDER_ID())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
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
                .validateMid(mid)
                .AssertAll();
        String grepcmd = "grep " + initTxnDTO.getBody().getOrderId() + " /paytm/logs/pgproxy-notification.log | grep 'alipayplus.acquiring.order.paymentNotify' | grep 'request'";
        Awaitility.await().atMost(30, TimeUnit.SECONDS).until(() -> LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.PG_PROXY_NOTIFICATION, grepcmd), s -> !"".equals(s));
    }
    @Owner(HIMANSHU)
    @Feature("PG2-7678")
    @Test(description = "Validate Pending saved DC transaction for PG2 MID with PG2_ENABLED PREF routed through PG2, close order V1 and then check merchant status")
    public void validatePendingSavedDCTxnPG2MID_PG2Enabled_CloseOrderV1ThenCheckMerchantStatus() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = PG2_CC_PG2_ENABLED;
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO().setExpYear("2099");
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getDebitCardNumber());
        String cardId = SavedCardHelpers.getSavedCardId(user, 0);
        String cardInfo = cardId + "||" + paymentDTO.getCvvNumber() + "|";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.PG2_CC_PG2_ENABLED)
                .setCustId("Test102")
                .setTxnValue("99.84")
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "DEBIT_CARD", "false");
        String mid = merchant.getId();
        String orderID=initTxnDTO.getBody().getOrderId();
//        PaymentDTO paymentDTO = new PaymentDTO();
//        String cardInfo = "|" +paymentDTO.DEBIT_CARD_NUMBER+"|"+paymentDTO.getCvvNumber() + "|" + paymentDTO.getExpMonth()+ paymentDTO.getExpYear();
        OrderDTO orderDTO = new OrderFactory.Native(merchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.DEBIT_CARD)
                .setMID(mid)
                .setORDER_ID(orderID)
                .setCardInfo(cardInfo)
                .setAUTH_MODE("otp")
                .setCHANNEL_ID("WAP").build();
        checkoutPage.createNativeOrder(orderDTO, true);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateOrderId(orderDTO.getORDER_ID())
                .validateStatus("PENDING")
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilPending();
        Assertions.assertThat(txnStatus.txnStatusResponse.TXNID.contains("0000")).isTrue();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.PENDING.toString())
                .validateRespCode(Constants.ResponseCode.MERCHANT_STATUS_TXN_PENDING.getRespCode())
                .validateRespMsg(Constants.ResponseCode.MERCHANT_STATUS_TXN_PENDING.getRespMsg())
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
                .validateRespCode(Constants.ResponseCode.TXN_FAILURE.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_FAILURE.getRespMsg())
                .validateMid(mid)
                .AssertAll();
    }

    @Owner(HIMANSHU)
    @Feature("PG2-7678")
    @Test(description = "Validate Failed Saved DC transaction for PG2 MID with PG2_ENABLED pref routed through PG2")
    public void validateFailedSavedDCTxnPG2MID_PG2Enabled() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = PG2_CC_PG2_ENABLED;

        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getDebitCardNumber());
        String cardId = SavedCardHelpers.getSavedCardId(user, 0);
        String cardInfo = cardId + "||" + paymentDTO.getCvvNumber() + "|";

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue("99.99").setCustId("Test102").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "DEBIT_CARD", "false");
        String mid = merchant.getId();
        String orderID=initTxnDTO.getBody().getOrderId();
        OrderDTO orderDTO = new OrderFactory.Native(merchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.DEBIT_CARD)
                .setMID(mid)
                .setORDER_ID(orderID)
                .setCardInfo(cardInfo)
                .setAUTH_MODE("otp")
                .setCHANNEL_ID("WAP").build();
        checkoutPage.createNativeOrder(orderDTO, true);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderId(orderDTO.getORDER_ID())
                .validateRespCode("01")
                .validateStatus("TXN_FAILURE")
                .assertAll();

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
                .validateMid(mid)
                .AssertAll();
    }

    @Owner(HIMANSHU)
    @Feature("PG2-7681")
    @Test(description = "Validate Success refund of SAVED DC transaction for PG2 MID routed through PG2")
    public void validateSuccessFullAsyncRefund_SavedDCTxnPG2MID() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.COFT_MERCHANT;
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getDebitCardNumber());
        String cardId = SavedCardHelpers.getSavedCardId(user, 0);
        String cardInfo = cardId + "||" + paymentDTO.getCvvNumber() + "|";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.COFT_MERCHANT)
                .setCustId("Test102")
                .setTxnValue("2")
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(Constants.MerchantType.COFT_MERCHANT.getId(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        String mid = merchant.getId();
        String orderID=initTxnDTO.getBody().getOrderId();
        OrderDTO orderDTO = new OrderFactory.Native(merchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.DEBIT_CARD)
                .setMID(mid)
                .setORDER_ID(orderID)
                .setCardInfo(cardInfo)
                .setAUTH_MODE("otp")
                .setCHANNEL_ID("WAP").build();
        checkoutPage.createNativeOrder(orderDTO, true);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderId(orderDTO.getORDER_ID())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
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
                .validateMid(mid)
                .AssertAll();
        SyncRefund syncRefund = new SyncRefund();
        Response asyncRefundResp = given().spec(syncRefund.reqSpecAsyncRefund(Constants.MerchantType.COFT_MERCHANT, "1", initTxnDTO.getBody().getOrderId(),
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
        assertRefundSuccessNotifyPresence(initTxnDTO.getBody().getOrderId());
    }
    @Owner(HIMANSHU)
    @Test(description = "Validate PG2 txn through enhanced flow")
    public void dcEnhancedFlow(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        OrderDTO orderDTO = new OrderFactory.AddnPay(Constants.MerchantType.PG2_CC_PG2_ENABLED, theme, user).build();
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        //cashierPage.login(user);
        cashierPage.payBy(Constants.PayMode.DC);
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
    @Owner(HIMANSHU)
    @Parameters({"theme"})
    @Test(description = "Validate PG2 txn through enhanced flow")
    public void dcCheckoutJSFlow(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        MerchantConfig config = checkoutJsPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutJsPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.DC);
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
}