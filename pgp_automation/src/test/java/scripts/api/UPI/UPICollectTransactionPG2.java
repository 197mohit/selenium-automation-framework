package scripts.api.UPI;

import com.paytm.LocalConfig;
import com.paytm.ServerConfigProvider;
import com.paytm.api.CloseOrderAPI;
import com.paytm.api.TxnStatus;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.api.nativeAPI.FetchPaymentOptionV2;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.api.refund.SyncRefund;
import com.paytm.appconstants.Constants;
import com.paytm.appconstants.Constants.MerchantType;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.LogsValidationHelper;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.PG2LogsValidationHelper;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.CloseOrder.CloseOrderDTO;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.response.FetchPaymentOptResponseDTO;
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

import static com.paytm.appconstants.Constants.Owner.HIMANSHU;
import static com.paytm.apphelpers.LogsValidationHelper.verifyLogsOnPod;
import static com.paytm.apphelpers.PGPHelpers.assertRefundSuccessNotifyPresence;
import static io.restassured.RestAssured.given;

public class UPICollectTransactionPG2 extends PGPBaseTest {

    protected final ThreadLocal<FetchPaymentOptResponseDTO> fetchPaymentOptResponseDTO = new ThreadLocal<>();
    private final CheckoutJsCheckoutPage checkoutJSPage = new CheckoutJsCheckoutPage();
    private final CheckoutPage checkoutPage = new CheckoutPage();

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

    public void assertPTCCommonResponse(ProcessTxnV1Response ptcResponse, InitTxnDTO initTxnDTO){
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getBANKTXNID()).isNotNull();
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getCURRENCY()).isEqualTo("INR");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getMID()).isEqualTo(initTxnDTO.getBody().getMid());
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getORDERID()).isEqualTo(initTxnDTO.getBody().getOrderId());
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getTXNAMOUNT()).isEqualTo(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.getBody().getTxnAmount().getValue()));
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getTXNDATE()).isNotNull();
    }

    public void assertTheiaTxnStatusCommonResponse(Response theiaTxnStatusResponse, InitTxnDTO initTxnDTO, Constants.ResponseCode responseCode){
        Assertions.assertThat(theiaTxnStatusResponse.jsonPath().getString("body.txnInfo.CURRENCY")).isEqualTo("INR");
        Assertions.assertThat(theiaTxnStatusResponse.jsonPath().getString("body.txnInfo.MID")).isEqualTo(initTxnDTO.getBody().getMid());
        Assertions.assertThat(theiaTxnStatusResponse.jsonPath().getString("body.txnInfo.ORDERID")).isEqualTo(initTxnDTO.getBody().getOrderId());
        Assertions.assertThat(theiaTxnStatusResponse.jsonPath().getString("body.txnInfo.RESPCODE")).isEqualTo(responseCode.getRespCode());
        Assertions.assertThat(theiaTxnStatusResponse.jsonPath().getString("body.txnInfo.RESPMSG")).isEqualTo(responseCode.getRespMsg());
        Assertions.assertThat(theiaTxnStatusResponse.jsonPath().getString("body.txnInfo.TXNAMOUNT")).isEqualTo(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.getBody().getTxnAmount().getValue()));
    }

    public void verifyEsnLength(InitTxnDTO initTxnDTO, int length) throws InterruptedException {
        String grepEsn = "grep " + initTxnDTO.getBody().getOrderId() + " " + LocalConfig.INSTAPROXY_LOGS + "|grep \"ExtSN=\"" + " | grep \"alipayplus.fluxnet.paytm.upi.payment.request\"";
        String extSn = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.INSTAPROXY, grepEsn);
        String extSnValue = extSn.substring(extSn.indexOf("ExtSN="),extSn.indexOf(", OrderId=")).replace("ExtSN=", "");
        System.out.println("Esn is " + extSnValue);
        Assertions.assertThat(extSnValue.length()).isEqualTo(length);
    }

    public JsonPath Validate_FetchV2PayInstrument(String txnToken, InitTxnDTO initTxnDTO, String payMethod, String status) {
        Reporter.report.info("Validating fetch pay options for the merchant and txn token");
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOptionV2 fetchPaymentOption = new FetchPaymentOptionV2(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        return fetchPaymentOptionsJson;
    }

    @Owner(HIMANSHU)
    @Feature("PG2-8009")
    @Test(description = "Validate Success UPI Collect transaction for PG2 MID routed through PG2 with FULL_TRAFFIC_ENABLED pref")
    public void validateSuccessPG2UPICollectTxn_PG2MID_FullTrafficenabled() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String txnamount = "2";
        MerchantType merchant = MerchantType.PG2_COP_FULL_TRAFFIC_Y;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnamount)
                .setOrderId(CommonHelpers.generateOrderId())
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        String mid = merchant.getId();
        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setTxnValue(txnamount)
                .setAuthMode("USRPWD")
                .setPayerAccount("test9972746530@paytm")
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
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_SUCCESS.getRespMsg())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
        CloseOrderDTO closeOrderDTO = new CloseOrderDTO();
        closeOrderDTO.setHead(new com.paytm.dto.CloseOrder.Head().setChannelId("WAP").setVersion("v1").setSigature(""))
                .setBody(new com.paytm.dto.CloseOrder.Body().setIsForceClose(true).setOrderId(initTxnDTO.getBody().getOrderId()).setMid(merchant.getId()));
        CloseOrderAPI closeOrderAPI = new CloseOrderAPI(closeOrderDTO);
        closeOrderAPI.execute();

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
                .validateMid(mid)
                .AssertAll();
        CommonHelpers.verifyPaymentNotify(orderId);
    }

    @Owner(HIMANSHU)
    @Feature("PG2-8009")
    @Parameters({"theme"})
    @Test(description = "Validate Success UPI Collect transaction (cotp flow: checkoutjs) for PG2 MID routed through PG2")
    public void validateSuccessPG2UPICollectTxn_COTP_PG2MID_FullTrafficenabled(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String txnamount = "2";
        MerchantType merchant = MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnamount)
                .setOrderId(CommonHelpers.generateOrderId())
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutJSPage.loadMerchantConfig(initTxnDTO, theme);
        config.merchant.setMid(MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y.getId());
        config.data.setOrderId(initTxnDTO.orderFromBody());
        config.data.setToken(txnToken);
        config.data.setAmount(txnamount);
        checkoutJSPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(Constants.PayMode.UPI);
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
        CommonHelpers.verifyPaymentNotify(initTxnDTO.orderFromBody());
    }

    @Owner(HIMANSHU)
    @Feature("PG2-8009")
    @Test(description = "Validate fail UPI Collect transaction for PG2 MID routed through PG2 full traffic enabled pref")
    public void validateFailPG2UPICollectTxn_PG2MID_FullTrafficenabled() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String txnamount = "99.99";
        MerchantType merchant = MerchantType.PG2_COP_FULL_TRAFFIC_Y;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnamount)
                .setOrderId(LocalConfig.UPI_ORDERWITH_ENV_NAME+CommonHelpers.generateOrderId())
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        String mid = merchant.getId();
        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setTxnValue(txnamount)
                .setAuthMode("USRPWD")
                .setPayerAccount("test9972746530@paytm")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        NativeHelpers.submitJsonFormInBrowser(ptcResponse.toString());

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateMid(mid)
                .validateOrderId(initTxnDTO.orderFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_FAILURE.toString())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .assertAll();
        CloseOrderDTO closeOrderDTO = new CloseOrderDTO();
        closeOrderDTO.setHead(new com.paytm.dto.CloseOrder.Head().setChannelId("WAP").setVersion("v1").setSigature(""))
                .setBody(new com.paytm.dto.CloseOrder.Body().setIsForceClose(true).setOrderId(initTxnDTO.getBody().getOrderId()).setMid(merchant.getId()));
        CloseOrderAPI closeOrderAPI = new CloseOrderAPI(closeOrderDTO);
        closeOrderAPI.execute();

        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        Assertions.assertThat(txnStatus.txnStatusResponse.TXNID.contains("0000")).isTrue();
        txnStatus.validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_FAILURE.toString())
                .validatePaymentMode("UPI")
                .validateMid(mid)
                .AssertAll();
    }

    @Owner(HIMANSHU)
    @Feature("PG2-8009")
    @Parameters({"theme"})
    @Test(description = "Validate fail UPI Collect transaction (cotp flow: checkoutjs) for PG2 MID routed through PG2")
    public void validateFailPG2UPICollectTxn_COTP_PG2MID_FullTrafficenabled(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String txnamount = "99.99";
        MerchantType merchant = MerchantType.PG2_JS_Checkout_Paytm_Domain;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnamount)
                .setOrderId(LocalConfig.UPI_ORDERWITH_ENV_NAME+CommonHelpers.generateOrderId())
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutJSPage.loadMerchantConfig(initTxnDTO, theme);
        config.merchant.setMid(MerchantType.PG2_JS_Checkout_Paytm_Domain.getId());
        config.data.setOrderId(initTxnDTO.orderFromBody());
        config.data.setToken(txnToken);
        config.data.setAmount(txnamount);
        checkoutJSPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(Constants.PayMode.UPI);

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus(Constants.TXNSTATUS.TXN_FAILURE.toString())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
        Assertions.assertThat(responsePage.textTxnID().getText()).contains("0000");
    }

    @Owner(HIMANSHU)
    @Feature("PG2-8011")
    @Test(description = "Validate Success UPI Collect transaction for PG2 MID routed through PG2")
    public void validateSuccessPG2UPICollectTxn_PG2MID_PG2Enabled() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String txnamount = "2";
        MerchantType merchant = MerchantType.PG2_ENABLED_PCF_Platform_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnamount)
                .setOrderId(LocalConfig.UPI_ORDERWITH_ENV_NAME+CommonHelpers.generateOrderId())
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        String mid = merchant.getId();
        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setTxnValue(txnamount)
                .setAuthMode("USRPWD")
                .setPayerAccount("test9972746530@paytm")
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
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_SUCCESS.getRespMsg())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
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
                .validateMid(mid)
                .AssertAll();
        CommonHelpers.verifyPaymentNotify(orderId);
    }

    @Owner(HIMANSHU)
    @Feature("PG2-8011")
    @Test(description = "Validate fail UPI Collect transaction for PG2 MID routed through PG2 pg2 enabled pref")
    public void validateFailPG2UPICollectTxn_PG2MID_PG2Enabled() throws Exception
    {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String txnamount = "99.99";
        MerchantType merchant = MerchantType.PG2_CC_PG2_ENABLED;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnamount)
                .setOrderId(LocalConfig.UPI_ORDERWITH_ENV_NAME+CommonHelpers.generateOrderId())
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        String mid = merchant.getId();
        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setTxnValue(txnamount)
                .setAuthMode("USRPWD")
                .setPayerAccount("test9972746530@paytm")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        NativeHelpers.submitJsonFormInBrowser(ptcResponse.toString());

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateMid(mid)
                .validateOrderId(initTxnDTO.orderFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_FAILURE.toString())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .assertAll();
        CloseOrderDTO closeOrderDTO = new CloseOrderDTO();
        closeOrderDTO.setHead(new com.paytm.dto.CloseOrder.Head().setChannelId("WAP").setVersion("v1").setSigature(""))
                .setBody(new com.paytm.dto.CloseOrder.Body().setIsForceClose(true).setOrderId(initTxnDTO.getBody().getOrderId()).setMid(merchant.getId()));
        CloseOrderAPI closeOrderAPI = new CloseOrderAPI(closeOrderDTO);
        closeOrderAPI.execute();

        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        Assertions.assertThat(txnStatus.txnStatusResponse.TXNID.contains("0000")).isTrue();
        txnStatus.validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_FAILURE.toString())
                .validatePaymentMode("UPI")
                .validateMid(mid)
                .AssertAll();
    }

    @Owner(HIMANSHU)
    @Feature("PG2-8012")
    @Test(description = "Validate Success UPI Collect transaction for PG2 MID routed through PG2 through payment.adapter flag")
    public void validateSuccessPG2UPICollectTxn_PG2MID_PaymentAdapter() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String txnamount = "2";
        MerchantType merchant = MerchantType.Payment_Adapter_Config_MDR_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnamount)
                .setOrderId(LocalConfig.UPI_ORDERWITH_ENV_NAME+CommonHelpers.generateOrderId())
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        String mid = merchant.getId();
        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setTxnValue(txnamount)
                .setAuthMode("USRPWD")
                .setPayerAccount("test9972746530@paytm")
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
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_SUCCESS.getRespMsg())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
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
                .validateMid(mid)
                .AssertAll();
        CommonHelpers.verifyPaymentNotify(orderId);
    }

    @Owner(HIMANSHU)
    @Feature("PG2-8012")
    @Test(description = "Validate fail UPI Collect transaction for PG2 MID routed through PG2 payment adapter flag")
    public void validateFailPG2UPICollectTxn_PG2MID_PaymentAdapter() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String txnamount = "99.99";
        MerchantType merchant = MerchantType.Payment_Adapter_Config_MDR_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnamount)
                .setOrderId(LocalConfig.UPI_ORDERWITH_ENV_NAME+CommonHelpers.generateOrderId())
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        String mid = merchant.getId();
        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setTxnValue(txnamount)
                .setAuthMode("USRPWD")
                .setPayerAccount("test9972746530@paytm")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        NativeHelpers.submitJsonFormInBrowser(ptcResponse.toString());

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateMid(mid)
                .validateOrderId(initTxnDTO.orderFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_FAILURE.toString())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .assertAll();
        CloseOrderDTO closeOrderDTO = new CloseOrderDTO();
        closeOrderDTO.setHead(new com.paytm.dto.CloseOrder.Head().setChannelId("WAP").setVersion("v1").setSigature(""))
                .setBody(new com.paytm.dto.CloseOrder.Body().setIsForceClose(true).setOrderId(initTxnDTO.getBody().getOrderId()).setMid(merchant.getId()));
        CloseOrderAPI closeOrderAPI = new CloseOrderAPI(closeOrderDTO);
        closeOrderAPI.execute();

        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        Assertions.assertThat(txnStatus.txnStatusResponse.TXNID.contains("0000")).isTrue();
        txnStatus.validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_FAILURE.toString())
                .validatePaymentMode("UPI")
                .validateMid(mid)
                .AssertAll();
    }

    @Owner(HIMANSHU)
    @Feature("PG2-8013")
    @Test(description = "Validate partial async refund for Success UPI Collect transaction for PG2 MID with FULL_TRAFFIC_ENABLED pref")
    public void validatePartialAsyncRefund_UPICollectTxnPG2MID_FullTrafficEnabled() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String txnamount = "2";
        MerchantType merchant = MerchantType.PG2_CC_PG2_ENABLED;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnamount)
                .setOrderId(LocalConfig.UPI_ORDERWITH_ENV_NAME+CommonHelpers.generateOrderId())
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        String mid = merchant.getId();
        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setTxnValue(txnamount)
                .setAuthMode("USRPWD")
                .setPayerAccount("test9972746530@paytm")
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
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_SUCCESS.getRespMsg())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
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
                .validateMid(mid)
                .AssertAll();
        SyncRefund syncRefund = new SyncRefund();
        Response asyncRefundResp = given().spec(syncRefund.reqSpecAsyncRefund(MerchantType.PG2_CC_PG2_ENABLED, "1", initTxnDTO.getBody().getOrderId(),
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
    @Feature("PG2-8013")
    @Test(description = "Validate full async refund for Success UPI Collect transaction for PG2 MID with FULL_TRAFFIC_ENABLED pref")
    public void validateFullAsyncRefund_UPICollectTxnPG2MID_FullTrafficEnabled() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String txnamount = "1";
        MerchantType merchant = MerchantType.PG2_COP_FULL_TRAFFIC_Y_Retry;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnamount)
                .setOrderId(LocalConfig.UPI_ORDERWITH_ENV_NAME+CommonHelpers.generateOrderId())
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        String mid = merchant.getId();
        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setTxnValue(txnamount)
                .setAuthMode("USRPWD")
                .setPayerAccount("test9972746530@paytm")
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
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_SUCCESS.getRespMsg())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
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
                .validateMid(mid)
                .AssertAll();
        SyncRefund syncRefund = new SyncRefund();
        Response asyncRefundResp = given().spec(syncRefund.reqSpecAsyncRefund(MerchantType.PG2_COP_FULL_TRAFFIC_Y_Retry, "1", initTxnDTO.getBody().getOrderId(),
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
    public void upiCollectEnhancedFlow(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        OrderDTO orderDTO = new OrderFactory.AddnPay(Constants.MerchantType.UPIPUSHPG2, theme, user).build();
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        //cashierPage.login(user);
        cashierPage.payBy(Constants.PayMode.UPI);
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
    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PG2-12876")
    @Test(description = "Advanced filters to disable  UPI Collect transactions on Cashier Page")
    public void verifyAdvanceFilteronBossDisableUPIPaymethod(@Optional("enhancedweb_revamp") String theme){
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.ADVANCE_FILTER_BOSS, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabUPI().assertNotVisible();
    }
    @Owner(Constants.Owner.ABHISHEK_KULKARNI)
    @Feature("PGP-52310")
    @Test(description = "Validate the Txn Limit Error Msg For UPI Collect Txn")
    public void ValidateTxnLimitForUPICollect() throws Exception {
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        String txnamount = "211";
        MerchantType merchant = MerchantType.UPILITE_LIMIT;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnamount)
                .setOrderId(CommonHelpers.generateOrderId())
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        String mid = merchant.getId();
        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid, initTxnResponse.getBody().getTxnToken(), orderId, "PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setTxnValue(txnamount)
                .setAuthMode("USRPWD")
                .setPayerAccount("test9972746530@paytm")
                .build();
        ProcessTxnV1Response response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        String resultMsg = response.getBody().getResultInfo().getResultMsg();
        String theia_facade = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, initTxnDTO.getBody().getOrderId(), "ACQUIRING_CREATE_ORDER_AND_PAY");
        Assertions.assertThat(theia_facade).contains("\"resultMsg\":\"" + resultMsg + "\"");
        Assertions.assertThat(theia_facade).contains("\"resultCode\":\"UPI_COLLECT_LIMIT_CHECK_AMOUNT_OVER_MAX_SINGLE_LIMIT\"");
        Assertions.assertThat(theia_facade).contains("\"resultCodeId\":\"12110049\"");
    }

    @Owner(Constants.Owner.ABHISHEK_KULKARNI)
    @Feature("PGP-52310")
    @Test(description = "Validate the Daily Limit Error Msg For UPI Collect Txn")
    public void ValidateDailyLimitForUPICollect() throws Exception {
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        String txnamount = "221";
        MerchantType merchant = MerchantType.UPI_CC_LIMIT;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnamount)
                .setOrderId(CommonHelpers.generateOrderId())
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        String mid = merchant.getId();
        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid, initTxnResponse.getBody().getTxnToken(), orderId, "PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setTxnValue(txnamount)
                .setAuthMode("USRPWD")
                .setPayerAccount("test9972746530@paytm")
                .build();
        ProcessTxnV1Response response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        String resultMsg = response.getBody().getResultInfo().getResultMsg();
        String theia_facade = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, initTxnDTO.getBody().getOrderId(), "ACQUIRING_CREATE_ORDER_AND_PAY");
        Assertions.assertThat(theia_facade).contains("resultMsg\":\"Merchant cannot accept payments on UPI at the moment. Try using other options.");
        Assertions.assertThat(theia_facade).contains("\"resultCode\":\"UPI_COLLECT_LIMIT_CHECK_AMOUNT_OVER_DAILY_LIMIT\"");
        Assertions.assertThat(theia_facade).contains("\"resultCodeId\":\"12110047\"");
    }

    @Owner(Constants.Owner.ABHISHEK_KULKARNI)
    @Feature("PGP-52310")
    @Test(description = "Validate the MONTHLY Limit Error Msg For UPI Collect Txn")
    public void ValidateMonthlyLimitForUPICollect() throws Exception {
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        String txnamount = "231";
        MerchantType merchant = MerchantType.UPI_CC_LIMIT;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnamount)
                .setOrderId(CommonHelpers.generateOrderId())
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        String mid = merchant.getId();
        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid, initTxnResponse.getBody().getTxnToken(), orderId, "PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setTxnValue(txnamount)
                .setAuthMode("USRPWD")
                .setPayerAccount("test9972746530@paytm")
                .build();
        ProcessTxnV1Response response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        String resultMsg = response.getBody().getResultInfo().getResultMsg();
        String theia_facade = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, initTxnDTO.getBody().getOrderId(), "ACQUIRING_CREATE_ORDER_AND_PAY");
        Assertions.assertThat(theia_facade).contains("resultMsg\":\"Merchant cannot accept payments on UPI at the moment. Try using other options.");
        Assertions.assertThat(theia_facade).contains("\"resultCode\":\"UPI_COLLECT_LIMIT_CHECK_AMOUNT_OVER_MONTHLY_LIMIT\"");
        Assertions.assertThat(theia_facade).contains("\"resultCodeId\":\"12110048\"");
    }

    @Owner(Constants.Owner.ABHISHEK_KULKARNI)
    @Feature("PGP-52310")
    @Test(description = "Validate the Txn Limit Error Msg For UPI Collect COTP Txn")
    public void ValidateTxnLimitForUPICollectCotp() throws Exception {
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        String txnamount = "211";
        MerchantType merchant = MerchantType.UPI_PUSH_TXNLIMIT_COTP;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnamount)
                .setOrderId(CommonHelpers.generateOrderId())
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        String mid = merchant.getId();
        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid, initTxnResponse.getBody().getTxnToken(), orderId, "PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setTxnValue(txnamount)
                .setAuthMode("USRPWD")
                .setPayerAccount("test9972746530@paytm")
                .build();
        ProcessTxnV1Response response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        String resultMsg = response.getBody().getResultInfo().getResultMsg();
        String theia_facade = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, initTxnDTO.getBody().getOrderId(), "ACQUIRING_PAY_ORDER");
        Assertions.assertThat(theia_facade).contains("\"resultMsg\":\"" + resultMsg + "\"");
        Assertions.assertThat(theia_facade).contains("\"resultCode\":\"UPI_COLLECT_LIMIT_CHECK_AMOUNT_OVER_MAX_SINGLE_LIMIT\"");
        Assertions.assertThat(theia_facade).contains("\"resultCodeId\":\"12110049\"");
    }

    @Owner(Constants.Owner.ABHISHEK_KULKARNI)
    @Feature("PGP-52310")
    @Test(description = "Validate the Daily Limit Error Msg For UPI Collect COTP Txn")
    public void ValidateDailyLimitForUPICollectCotp() throws Exception {
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        String txnamount = "321";
        MerchantType merchant = MerchantType.UPI_PUSH_LIMIT_COTP;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnamount)
                .setOrderId(CommonHelpers.generateOrderId())
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        String mid = merchant.getId();
        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid, initTxnResponse.getBody().getTxnToken(), orderId, "PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setTxnValue(txnamount)
                .setAuthMode("USRPWD")
                .setPayerAccount("test9972746530@paytm")
                .build();
        ProcessTxnV1Response response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        String resultMsg = response.getBody().getResultInfo().getResultMsg();
        String theia_facade = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, initTxnDTO.getBody().getOrderId(), "ACQUIRING_PAY_ORDER");
        Assertions.assertThat(theia_facade).contains("\"resultMsg\":\"" + resultMsg + "\"");
        Assertions.assertThat(theia_facade).contains("\"resultCode\":\"UPI_COLLECT_LIMIT_CHECK_AMOUNT_OVER_DAILY_LIMIT\"");
        Assertions.assertThat(theia_facade).contains("\"resultCodeId\":\"12110047\"");
    }

    @Owner(Constants.Owner.ABHISHEK_KULKARNI)
    @Feature("PGP-52310")
    @Test(description = "Validate the MONTHLY Limit Error Msg For UPI Collect COTP Txn")
    public void ValidateMonthlyLimitForUPICollectCotp() throws Exception {
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        String txnamount = "231";
        MerchantType merchant = MerchantType.UPI_PUSH_LIMIT_COTP;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnamount)
                .setOrderId(CommonHelpers.generateOrderId())
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        String mid = merchant.getId();
        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid, initTxnResponse.getBody().getTxnToken(), orderId, "PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setTxnValue(txnamount)
                .setAuthMode("USRPWD")
                .setPayerAccount("test9972746530@paytm")
                .build();
        ProcessTxnV1Response response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        String resultMsg = response.getBody().getResultInfo().getResultMsg();
        String theia_facade = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, initTxnDTO.getBody().getOrderId(), "ACQUIRING_PAY_ORDER");
        Assertions.assertThat(theia_facade).contains("\"resultMsg\":\"" + resultMsg + "\"");
        Assertions.assertThat(theia_facade).contains("\"resultCode\":\"UPI_COLLECT_LIMIT_CHECK_AMOUNT_OVER_MONTHLY_LIMIT\"");
        Assertions.assertThat(theia_facade).contains("\"resultCodeId\":\"12110048\"");
    }
}