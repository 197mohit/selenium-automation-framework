package scripts.api.CC;

import com.paytm.LocalConfig;
import com.paytm.ServerConfigProvider;
import com.paytm.api.CloseOrderAPI;
import com.paytm.api.TxnStatus;
import com.paytm.api.nativeAPI.FetchBinDetail;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.dto.NativeDTO.fetchBinDetails.FetchBinDetailsRequest;
import com.paytm.api.refund.SyncRefund;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.LogsValidationHelper;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.PG2LogsValidationHelper;
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
import org.assertj.core.api.SoftAssertions;
import org.awaitility.Awaitility;
import org.testng.annotations.Optional;
import org.testng.annotations.Test;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import static com.paytm.appconstants.Constants.MerchantType.*;
import static com.paytm.appconstants.Constants.Owner.HIMANSHU;
import static com.paytm.appconstants.Constants.Owner.RONIKA;
import static com.paytm.apphelpers.PGPHelpers.assertRefundSuccessNotifyPresence;
import static com.paytm.dto.PaymentDTO.CORPORATE_INDIAN_DC;
import static com.paytm.dto.PaymentDTO.PREPAID_CARD;
import static io.restassured.RestAssured.given;

public class CC_PG2 extends PGPBaseTest {
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

    public void assertTheiaTxnStatusCommonResponse(Response theiaTxnStatusResponse, InitTxnDTO initTxnDTO, Constants.ResponseCode responseCode) {
        Assertions.assertThat(theiaTxnStatusResponse.jsonPath().getString("body.txnInfo.CURRENCY")).isEqualTo("INR");
        Assertions.assertThat(theiaTxnStatusResponse.jsonPath().getString("body.txnInfo.MID")).isEqualTo(initTxnDTO.getBody().getMid());
        Assertions.assertThat(theiaTxnStatusResponse.jsonPath().getString("body.txnInfo.ORDERID")).isEqualTo(initTxnDTO.getBody().getOrderId());
        Assertions.assertThat(theiaTxnStatusResponse.jsonPath().getString("body.txnInfo.RESPCODE")).isEqualTo(responseCode.getRespCode());
        Assertions.assertThat(theiaTxnStatusResponse.jsonPath().getString("body.txnInfo.RESPMSG")).isEqualTo(responseCode.getRespMsg());
        Assertions.assertThat(theiaTxnStatusResponse.jsonPath().getString("body.txnInfo.TXNAMOUNT")).isEqualTo(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.getBody().getTxnAmount().getValue()));
    }


    public void assertPTCCommonResponse(ProcessTxnV1Response ptcResponse) {
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
    @Owner(HIMANSHU)
    @Feature("PG2-7186")
    @Test(description = "Validate Success unsaved CC transaction for PG2 MID routed through PG2")
    public void validateSuccessUnsavedCCTxnPG2MID() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.PG2_CC_DC;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderID=initTxnDTO.getBody().getOrderId();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "CREDIT_CARD", "false");
        String mid = merchant.getId();
        PaymentDTO paymentDTO = new PaymentDTO();
        String cardInfo = "|" +paymentDTO.VISA_CREDIT_CARD_NUMBER+"|"+paymentDTO.getCvvNumber() + "|" + paymentDTO.getExpMonth()+ paymentDTO.getExpYear();
        OrderDTO orderDTO = new OrderFactory.Native(merchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
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
                .validateStatus("TXN_SUCCESS");
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        Assertions.assertThat(txnStatus.txnStatusResponse.TXNID.contains("0000")).isTrue();
        txnStatus.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_SUCCESS.getRespMsg())
                .validatePaymentMode("CC")
                .validateMid(mid)
                .AssertAll();
        CommonHelpers.verifyPaymentNotify(orderID);
    }

    @Owner(HIMANSHU)
    @Feature("PG2-7186")
    @Test(description = "Validate Pending unsaved CC transaction for PG2 MID routed through PG2, close order V1 and then check merchant status")
    public void validatePendingUnsavedCCTxnPG2MIDCloseOrderV1ThenCheckMerchantStatus() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.PG2_CC_DC;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue("99.84").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderID=initTxnDTO.getBody().getOrderId();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "CREDIT_CARD", "false");
        String mid = merchant.getId();
        PaymentDTO paymentDTO = new PaymentDTO();
        String cardInfo = "|" +paymentDTO.VISA_CREDIT_CARD_NUMBER+"|"+paymentDTO.getCvvNumber() + "|" + paymentDTO.getExpMonth()+ paymentDTO.getExpYear();
        OrderDTO orderDTO = new OrderFactory.Native(merchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
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
        Assertions.assertThat(txnStatus.txnStatusResponse.TXNID.contains("0000")).isTrue();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.PENDING.toString())
                .validateRespCode(Constants.ResponseCode.MERCHANT_STATUS_TXN_PENDING.getRespCode())
                .validateRespMsg(Constants.ResponseCode.MERCHANT_STATUS_TXN_PENDING.getRespMsg())
                .validatePaymentMode("CC")
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
                .validatePaymentMode("CC")
                .validateMid(mid)
                .AssertAll();
    }


    @Owner(HIMANSHU)
    @Feature("PG2-7186")
    @Test(description = "Validate Failed unsaved CC transaction for PG2 MID routed through PG2")
    public void validateFailedUnsavedCCTxnPG2MID() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.PG2_CC_DC;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue("99.99").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderID=initTxnDTO.getBody().getOrderId();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "CREDIT_CARD", "false");
        String mid = merchant.getId();
        PaymentDTO paymentDTO = new PaymentDTO();
        String cardInfo = "|" +paymentDTO.VISA_CREDIT_CARD_NUMBER+"|"+paymentDTO.getCvvNumber() + "|" + paymentDTO.getExpMonth()+ paymentDTO.getExpYear();
        OrderDTO orderDTO = new OrderFactory.Native(merchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .setORDER_ID(orderID)
                .setCardInfo(cardInfo)
                .setAUTH_MODE("otp")
                .setCHANNEL_ID("WAP").build();

        checkoutPage.createNativeOrder(orderDTO, true);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderId(orderDTO.getORDER_ID())
                .validateStatus(Constants.TXNSTATUS.TXN_FAILURE.toString())
                .validateRespCode(Constants.ResponseCode.TXN_FAILURE.getRespCode());
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
                .validatePaymentMode("CC")
                .validateMid(mid)
                .AssertAll();
        CommonHelpers.closeOrderNotify(orderID);
    }
    @Owner(HIMANSHU)
    @Feature("PG2-7189")
    @Test(description = "Validate Success unsaved CC transaction for PG2 MID with FULL TRAFFIC ENABLED pref routed through PG2")
    public void validateSuccessUnsavedCCTxnPG2MID_FullTrafficEnabled() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = PG2_CC_FULL_TRAFFIC_ENABLED;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderID=initTxnDTO.getBody().getOrderId();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "CREDIT_CARD", "false");
        String mid = merchant.getId();
        PaymentDTO paymentDTO = new PaymentDTO();
        String cardInfo = "|" +paymentDTO.VISA_CREDIT_CARD_NUMBER+"|"+paymentDTO.getCvvNumber() + "|" + paymentDTO.getExpMonth()+ paymentDTO.getExpYear();
        OrderDTO orderDTO = new OrderFactory.Native(merchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .setORDER_ID(orderID)
                .setCardInfo(cardInfo)
                .setAUTH_MODE("otp")
                .setCHANNEL_ID("WAP").build();

        checkoutPage.createNativeOrder(orderDTO, true);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderId(orderDTO.getORDER_ID())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode());
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        Assertions.assertThat(txnStatus.txnStatusResponse.TXNID.contains("0000")).isTrue();
        txnStatus.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_SUCCESS.getRespMsg())
                .validatePaymentMode("CC")
                .validateMid(mid)
                .AssertAll();

    }

    @Owner(HIMANSHU)
    @Feature("PG2-7189")
    @Test(description = "Validate Pending unsaved CC transaction for PG2 MID with FULL_TRAFFIC_ENABLED PREF routed through PG2, close order V1 and then check merchant status")
    public void validatePendingUnsavedCCTxnPG2MID_fullTrafficEnabled_CloseOrderV1ThenCheckMerchantStatus() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = PG2_CC_FULL_TRAFFIC_ENABLED;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue("99.84").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderID=initTxnDTO.getBody().getOrderId();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "CREDIT_CARD", "false");
        String mid = merchant.getId();
        PaymentDTO paymentDTO = new PaymentDTO();
        String cardInfo = "|" +paymentDTO.VISA_CREDIT_CARD_NUMBER+"|"+paymentDTO.getCvvNumber() + "|" + paymentDTO.getExpMonth()+ paymentDTO.getExpYear();
        OrderDTO orderDTO = new OrderFactory.Native(merchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
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
        Assertions.assertThat(txnStatus.txnStatusResponse.TXNID.contains("0000")).isTrue();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.PENDING.toString())
                .validateRespCode(Constants.ResponseCode.MERCHANT_STATUS_TXN_PENDING.getRespCode())
                .validateRespMsg(Constants.ResponseCode.MERCHANT_STATUS_TXN_PENDING.getRespMsg())
                .validatePaymentMode("CC")
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
                .validatePaymentMode("CC")
                .validateMid(mid)
                .AssertAll();
    }

    @Owner(HIMANSHU)
    @Feature("PG2-7189")
    @Test(description = "Validate Failed unsaved CC transaction for PG2 MID with FULL_TRAFFIC ENABLED pref routed through PG2")
    public void validateFailedUnsavedCCTxnPG2MID_fullTrafficEnabled() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = PG2_CC_FULL_TRAFFIC_ENABLED;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue("99.99").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderID=initTxnDTO.getBody().getOrderId();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "CREDIT_CARD", "false");
        String mid = merchant.getId();
        PaymentDTO paymentDTO = new PaymentDTO();
        String cardInfo = "|" +paymentDTO.VISA_CREDIT_CARD_NUMBER+"|"+paymentDTO.getCvvNumber() + "|" + paymentDTO.getExpMonth()+ paymentDTO.getExpYear();
        OrderDTO orderDTO = new OrderFactory.Native(merchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .setORDER_ID(orderID)
                .setCardInfo(cardInfo)
                .setAUTH_MODE("otp")
                .setCHANNEL_ID("WAP").build();

        checkoutPage.createNativeOrder(orderDTO, true);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderId(orderDTO.getORDER_ID())
                .validateStatus(Constants.TXNSTATUS.TXN_FAILURE.toString())
                .validateRespCode(Constants.ResponseCode.TXN_FAILURE.getRespCode());
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
                .validatePaymentMode("CC")
                .validateMid(mid)
                .AssertAll();
    }
    @Owner(HIMANSHU)
    @Feature("PG2-7188")
    @Test(description = "Validate Success unsaved CC transaction for PG2 MID with PG2_ENABLED pref routed through PG2")
    public void validateSuccessUnsavedCCTxnPG2MID_PG2Enabled() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderID=initTxnDTO.getBody().getOrderId();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "CREDIT_CARD", "false");
        String mid = merchant.getId();
        PaymentDTO paymentDTO = new PaymentDTO();
        String cardInfo = "|" +paymentDTO.VISA_CREDIT_CARD_NUMBER+"|"+paymentDTO.getCvvNumber() + "|" + paymentDTO.getExpMonth()+ paymentDTO.getExpYear();
        OrderDTO orderDTO = new OrderFactory.Native(merchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .setORDER_ID(orderID)
                .setCardInfo(cardInfo)
                .setAUTH_MODE("otp")
                .setCHANNEL_ID("WAP").build();

        checkoutPage.createNativeOrder(orderDTO, true);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderId(orderDTO.getORDER_ID())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode());
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        Assertions.assertThat(txnStatus.txnStatusResponse.TXNID.contains("0000")).isTrue();
        txnStatus.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_SUCCESS.getRespMsg())
                .validatePaymentMode("CC")
                .validateMid(mid)
                .AssertAll();

    }
    @Owner(HIMANSHU)
    @Feature("PG2-7188")
    @Test(description = "Validate Pending unsaved CC transaction for PG2 MID with PG2_ENABLED PREF routed through PG2, close order V1 and then check merchant status")
    public void validatePendingUnsavedCCTxnPG2MID_PG2Enabled_CloseOrderV1ThenCheckMerchantStatus() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = PG2_CC_PG2_ENABLED;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue("99.84").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderID=initTxnDTO.getBody().getOrderId();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "CREDIT_CARD", "false");
        String mid = merchant.getId();
        PaymentDTO paymentDTO = new PaymentDTO();
        String cardInfo = "|" +paymentDTO.VISA_CREDIT_CARD_NUMBER+"|"+paymentDTO.getCvvNumber() + "|" + paymentDTO.getExpMonth()+ paymentDTO.getExpYear();
        OrderDTO orderDTO = new OrderFactory.Native(merchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
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
        Assertions.assertThat(txnStatus.txnStatusResponse.TXNID.contains("0000")).isTrue();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.PENDING.toString())
                .validateRespCode(Constants.ResponseCode.MERCHANT_STATUS_TXN_PENDING.getRespCode())
                .validateRespMsg(Constants.ResponseCode.MERCHANT_STATUS_TXN_PENDING.getRespMsg())
                .validatePaymentMode("CC")
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
                .validatePaymentMode("CC")
                .validateMid(mid)
                .AssertAll();
    }

    @Owner(HIMANSHU)
    @Feature("PG2-7188")
    @Test(description = "Validate Failed unsaved CC transaction for PG2 MID with PG2_ENABLED pref routed through PG2")
    public void validateFailedUnsavedCCTxnPG2MID_PG2Enabled() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue("99.99").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderID=initTxnDTO.getBody().getOrderId();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "CREDIT_CARD", "false");
        String mid = merchant.getId();
        PaymentDTO paymentDTO = new PaymentDTO();
        String cardInfo = "|" +paymentDTO.VISA_CREDIT_CARD_NUMBER+"|"+paymentDTO.getCvvNumber() + "|" + paymentDTO.getExpMonth()+ paymentDTO.getExpYear();
        OrderDTO orderDTO = new OrderFactory.Native(merchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .setORDER_ID(orderID)
                .setCardInfo(cardInfo)
                .setAUTH_MODE("otp")
                .setCHANNEL_ID("WAP").build();

        checkoutPage.createNativeOrder(orderDTO, true);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderId(orderDTO.getORDER_ID())
                .validateStatus(Constants.TXNSTATUS.TXN_FAILURE.toString())
                .validateRespCode(Constants.ResponseCode.TXN_FAILURE.getRespCode());
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
                .validatePaymentMode("CC")
                .validateMid(mid)
                .AssertAll();
    }

    @Owner(HIMANSHU)
    @Feature("PG2-7270")
    @Test(description = "Validate Success unsaved CC transaction for PG2 MID routed through P+ if user id is not present in payment.adapter flag")
    public void validateSuccessUnsavedPplusCCTxnPG2MID() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.PG2_CC_DC;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderID=initTxnDTO.getBody().getOrderId();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "CREDIT_CARD", "false");
        String mid = merchant.getId();
        PaymentDTO paymentDTO = new PaymentDTO();
        String cardInfo = "|" +paymentDTO.VISA_CREDIT_CARD_NUMBER+"|"+paymentDTO.getCvvNumber() + "|" + paymentDTO.getExpMonth()+ paymentDTO.getExpYear();
        OrderDTO orderDTO = new OrderFactory.Native(merchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .setORDER_ID(orderID)
                .setCardInfo(cardInfo)
                .setAUTH_MODE("otp")
                .setCHANNEL_ID("WAP").build();

        checkoutPage.createNativeOrder(orderDTO, true);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderId(orderDTO.getORDER_ID())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode());
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        Assertions.assertThat(txnStatus.txnStatusResponse.TXNID.contains("1212")).isTrue();
        txnStatus.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_SUCCESS.getRespMsg())
                .validatePaymentMode("CC")
                .validateMid(mid)
                .AssertAll();

    }
    @Owner(HIMANSHU)
    @Feature("PG2-7252")
    @Test(description = "Validate full async refund for Success unsaved CC transaction for PG2 MID with PG2_ENABLED pref")
    public void validateFullAsyncRefund_UnsavedCCTxnPG2MID_PG2Enabled() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String mid = Constants.MerchantType.PG2_CC_PG2_ENABLED.getId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.PG2_CC_PG2_ENABLED).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderID=initTxnDTO.getBody().getOrderId();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "CREDIT_CARD", "false");
        PaymentDTO paymentDTO = new PaymentDTO();
        String cardInfo = "|" +paymentDTO.VISA_CREDIT_CARD_NUMBER+"|"+paymentDTO.getCvvNumber() + "|" + paymentDTO.getExpMonth()+ paymentDTO.getExpYear();
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.PG2_CC_PG2_ENABLED, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .setORDER_ID(orderID)
                .setCardInfo(cardInfo)
                .setAUTH_MODE("otp")
                .setCHANNEL_ID("WAP").build();

        checkoutPage.createNativeOrder(orderDTO, true);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderId(orderDTO.getORDER_ID())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode());
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        Assertions.assertThat(txnStatus.txnStatusResponse.TXNID.contains("0000")).isTrue();
        txnStatus.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_SUCCESS.getRespMsg())
                .validatePaymentMode("CC")
                .validateMid(mid)
                .AssertAll();
        SyncRefund syncRefund = new SyncRefund();
        Response asyncRefundResp = given().spec(syncRefund.reqSpecAsyncRefund(Constants.MerchantType.PG2_CC_PG2_ENABLED, initTxnDTO.txnAmountFromBody(), initTxnDTO.getBody().getOrderId(),
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
        //only validating presence of refund success notify in logs
        assertRefundSuccessNotifyPresence(initTxnDTO.getBody().getOrderId());

    }
    @Owner(HIMANSHU)
    @Feature("PG2-7252")
    @Test(description = "Validate partial async refund for Success unsaved CC transaction for PG2 MID with PG2_ENABLED pref")
    public void validatePartialAsyncRefund_UnsavedCCTxnPG2MID_PG2Enabled() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String mid = Constants.MerchantType.PG2_CC_PG2_ENABLED.getId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.PG2_CC_PG2_ENABLED).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderID=initTxnDTO.getBody().getOrderId();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "CREDIT_CARD", "false");
        PaymentDTO paymentDTO = new PaymentDTO();
        String cardInfo = "|" +paymentDTO.VISA_CREDIT_CARD_NUMBER+"|"+paymentDTO.getCvvNumber() + "|" + paymentDTO.getExpMonth()+ paymentDTO.getExpYear();
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.PG2_CC_PG2_ENABLED, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .setORDER_ID(orderID)
                .setCardInfo(cardInfo)
                .setAUTH_MODE("otp")
                .setCHANNEL_ID("WAP").build();

        checkoutPage.createNativeOrder(orderDTO, true);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderId(orderDTO.getORDER_ID())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode());
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        Assertions.assertThat(txnStatus.txnStatusResponse.TXNID.contains("0000")).isTrue();
        txnStatus.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_SUCCESS.getRespMsg())
                .validatePaymentMode("CC")
                .validateMid(mid)
                .AssertAll();
        SyncRefund syncRefund = new SyncRefund();
        Response asyncRefundResp = given().spec(syncRefund.reqSpecAsyncRefund(Constants.MerchantType.PG2_CC_PG2_ENABLED, "1", initTxnDTO.getBody().getOrderId(),
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
    @Feature("PG2-7579")
    @Test(description = "Validate Success prepaid card transaction for PG2 MID routed through PG2")
    public void validateSuccessUnsavedPrepaidCardTxnPG2MID(@Optional("checkoutjs_web_revamp") String theme) throws Exception
    {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.PG2_CC_FULL_TRAFFIC_ENABLED).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutJsPage.loadMerchantConfig(initTxnDTO, theme);
        config.merchant.setMid(Constants.MerchantType.PG2_CC_FULL_TRAFFIC_ENABLED.getId());
        config.data.setOrderId(initTxnDTO.orderFromBody());
        config.data.setToken(txnToken);
        checkoutJsPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabCreditCard().click();
        cashierPage.payBy(Constants.PayMode.CC,new PaymentDTO().setCreditCardNumber(PREPAID_CARD));
        //cashierPage.merchantLOGO().assertVisible();
        Assertions.assertThat(cashierPage.footerLOGO().isDisplayed()).isTrue();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
        Assertions.assertThat(responsePage.textTxnID().getText()).contains("0000");
    }
    @Owner(HIMANSHU)
    @Feature("PG2-7579")
    @Test(description = "Validate Success corporate card transaction for PG2 MID routed through PG2")
    public void validateSuccessUnsavedCorporateCardTxnPG2MID(@Optional("checkoutjsweb_revamp") String theme) throws Exception
    {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, PG2_CC_DC).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutJsPage.loadMerchantConfig(initTxnDTO, theme);
        config.merchant.setMid(PG2_CC_DC.getId());
        config.data.setOrderId(initTxnDTO.orderFromBody());
        config.data.setToken(txnToken);
        checkoutJsPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabCreditCard().click();
        cashierPage.payBy(Constants.PayMode.CC,new PaymentDTO().setCreditCardNumber(CORPORATE_INDIAN_DC));
        //cashierPage.merchantLOGO().assertVisible();
        Assertions.assertThat(cashierPage.footerLOGO().isDisplayed()).isTrue();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
        Assertions.assertThat(responsePage.textTxnID().getText()).contains("0000");
    }

    @Owner(HIMANSHU)
    @Feature("PG2-7670")
    @Test(description = "Validate Success SAVED CC transaction for PG2 MID routed through PG2")
    public void validateSuccessSavedCCTxnPG2MID() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.PG2_CC_PG2_ENABLED;
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getDebitCardNumber());
        String cardId = SavedCardHelpers.getSavedCardId(user, 0);
        String cardInfo = cardId + "||" + paymentDTO.getCvvNumber() + "|";

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.PG2_CC_PG2_ENABLED)
                .setCustId("Test101")
                .setTxnValue("200")
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderID=initTxnDTO.getBody().getOrderId();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "CREDIT_CARD", "false");
        String mid = merchant.getId();
        OrderDTO orderDTO = new OrderFactory.Native(merchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
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
                .validateStatus("TXN_SUCCESS");
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        Assertions.assertThat(txnStatus.txnStatusResponse.TXNID.contains("0000")).isTrue();
        txnStatus.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_SUCCESS.getRespMsg())
                .validatePaymentMode("CC")
                .validateMid(mid)
                .AssertAll();
        String grepcmd = "grep " + initTxnDTO.getBody().getOrderId() + " /paytm/logs/pgproxy-notification.log | grep 'alipayplus.acquiring.order.paymentNotify' | grep 'request'";
        Awaitility.await().atMost(30, TimeUnit.SECONDS).until(() -> LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.PG_PROXY_NOTIFICATION, grepcmd), s -> !"".equals(s));
    }


    @Owner(HIMANSHU)
    @Feature("PG2-7670")
    @Test(description = "Validate Pending saved CC transaction for PG2 MID with PG2_ENABLED PREF routed through PG2, close order V1 and then check merchant status")
    public void validatePendingSavedCCTxnPG2MID_PG2Enabled_CloseOrderV1ThenCheckMerchantStatus() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = PG2_CC_PG2_ENABLED;
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getDebitCardNumber());
        String cardId = SavedCardHelpers.getSavedCardId(user, 0);
        String cardInfo = cardId + "||" + paymentDTO.getCvvNumber() + "|";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue("99.84").setCustId("Test101").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderID=initTxnDTO.getBody().getOrderId();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "CREDIT_CARD", "false");
        String mid = merchant.getId();
        OrderDTO orderDTO = new OrderFactory.Native(merchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
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
                .validateStatus("PENDING");
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        Assertions.assertThat(txnStatus.txnStatusResponse.TXNID.contains("0000")).isTrue();
        txnStatus.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_SUCCESS.getRespMsg())
                .validatePaymentMode("CC")
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
                .validatePaymentMode("CC")
                .validateMid(mid)
                .AssertAll();
    }

    @Owner(HIMANSHU)
    @Feature("PG2-7670")
    @Test(description = "Validate Failed Saved CC transaction for PG2 MID with PG2_ENABLED pref routed through PG2")
    public void validateFailedSavedCCTxnPG2MID_PG2Enabled() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = PG2_CC_PG2_ENABLED;
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getDebitCardNumber());
        String cardId = SavedCardHelpers.getSavedCardId(user, 0);
        String cardInfo = cardId + "||" + paymentDTO.getCvvNumber() + "|";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue("99.99").setCustId("Test101").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderID=initTxnDTO.getBody().getOrderId();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "CREDIT_CARD", "false");
        String mid = merchant.getId();
        OrderDTO orderDTO = new OrderFactory.Native(merchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .setORDER_ID(orderID)
                .setCardInfo(cardInfo)
                .setAUTH_MODE("otp")
                .setCHANNEL_ID("WAP").build();

        checkoutPage.createNativeOrder(orderDTO, true);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderId(orderDTO.getORDER_ID())
                .validateStatus("TXN_FAILURE");
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        Assertions.assertThat(txnStatus.txnStatusResponse.TXNID.contains("0000")).isTrue();
        txnStatus.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_SUCCESS.getRespMsg())
                .validatePaymentMode("CC")
                .validateMid(mid)
                .AssertAll();
        CloseOrderDTO closeOrderDTO = new CloseOrderDTO();
        closeOrderDTO.setHead(new com.paytm.dto.CloseOrder.Head().setChannelId("WAP").setVersion("v1").setSigature(""))
                .setBody(new com.paytm.dto.CloseOrder.Body().setIsForceClose(true).setOrderId(initTxnDTO.getBody().getOrderId()).setMid(merchant.getId()));
        CloseOrderAPI closeOrderAPI = new CloseOrderAPI(closeOrderDTO);
        closeOrderAPI.execute();
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_FAILURE.toString())
                .validatePaymentMode("CC")
                .validateMid(mid)
                .AssertAll();
    }
    @Owner(HIMANSHU)
    @Feature("PG2-7671")
    @Test(description = "Validate Success refund of SAVED CC transaction for PG2 MID routed through PG2")
    public void validateSuccessFullAsyncRefund_SavedCCTxnPG2MID() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.PG2_CC_PG2_ENABLED;
        //SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getDebitCardNumber());
        String cardId = SavedCardHelpers.getSavedCardId(user, 0);
        String cardInfo = cardId + "||" + paymentDTO.getCvvNumber() + "|";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.PG2_CC_PG2_ENABLED)
                .setCustId("Test101")
                .setTxnValue("1")
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderID=initTxnDTO.getBody().getOrderId();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "CREDIT_CARD", "false");
        String mid = merchant.getId();
        OrderDTO orderDTO = new OrderFactory.Native(merchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
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
                .validateStatus("TXN_SUCCESS");
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        Assertions.assertThat(txnStatus.txnStatusResponse.TXNID.contains("0000")).isTrue();
        txnStatus.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_SUCCESS.getRespMsg())
                .validatePaymentMode("CC")
                .validateMid(mid)
                .AssertAll();
        SyncRefund syncRefund = new SyncRefund();
        Response asyncRefundResp = given().spec(syncRefund.reqSpecAsyncRefund(Constants.MerchantType.PG2_CC_PG2_ENABLED, "1", initTxnDTO.getBody().getOrderId(),
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
    public void ccEnhancedFlow(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        OrderDTO orderDTO = new OrderFactory.AddnPay(Constants.MerchantType.PG2_CC_PG2_ENABLED, theme, user).build();
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        //cashierPage.login(user);
        cashierPage.payBy(Constants.PayMode.CC);
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

    @Owner(RONIKA)
    @Feature("PG-2158")
    @Test(description = "Validate Card Min Max Length, Bin Query CARD MinMax = fetchBin CARD MinMax ")
    public void validateCardMinMaxLength() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.PG2_CC_DC;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderID = initTxnDTO.getBody().getOrderId();
        String mid = merchant.getId();

        Validate_FetchPayInstrument(txnToken, initTxnDTO, "CREDIT_CARD", "false");

        PaymentDTO paymentDTO = new PaymentDTO();
        String bin = paymentDTO.VISA_CREDIT_CARD_NUMBER.substring(0, 6);
        FetchBinDetailsRequest fetchBinDetailsRequest = new FetchBinDetailsRequest.Builder(txnToken, bin).build();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(fetchBinDetailsRequest, mid, orderID);
        JsonPath fetchBinJson = fetchBinDetail.execute().jsonPath();
        Assertions.assertThat(fetchBinJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchBinJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchBinJson.getString("body.binDetail.paymentMode")).isEqualTo("CREDIT_CARD");
        Assertions.assertThat(fetchBinJson.getString("body.binDetail.bin")).isEqualTo(bin);
        String cnMin = fetchBinJson.getString("body.binDetail.cnMin");
        String cnMax = fetchBinJson.getString("body.binDetail.cnMax");
        Assertions.assertThat(cnMin).isNotNull();
        Assertions.assertThat(cnMax).isNotNull();
        String cardInfo = "|" + paymentDTO.VISA_CREDIT_CARD_NUMBER + "|" + paymentDTO.getCvvNumber() + "|" + paymentDTO.getExpMonth() + paymentDTO.getExpYear();
        OrderDTO orderDTO = new OrderFactory.Native(merchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
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
                .validateStatus("TXN_SUCCESS");
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        Assertions.assertThat(txnStatus.txnStatusResponse.TXNID.contains("0000")).isTrue();
        txnStatus.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_SUCCESS.getRespMsg())
                .validatePaymentMode("CC")
                .validateMid(mid)
                .AssertAll();
        CommonHelpers.verifyPaymentNotify(orderID);

        SoftAssertions softAssert = new SoftAssertions();
        String binQueryLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderID, "BIN_CENTER_V1_BIN_QUERY", "RESPONSE");
        softAssert.assertThat(binQueryLogs).contains("cardNumberMinLength=" + cnMin);
        softAssert.assertThat(binQueryLogs).contains("cardNumberMaxLength=" + cnMax);
        softAssert.assertAll();
    }
}