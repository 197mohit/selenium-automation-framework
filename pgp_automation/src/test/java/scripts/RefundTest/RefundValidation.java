package scripts.RefundTest;

import com.paytm.LocalConfig;
import com.paytm.api.InstaproxyPtybliResponse;
import com.paytm.api.MasterRefund;
import com.paytm.api.TxnStatus;
import com.paytm.api.upipsp.UpiPspConsultFee;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.api.refund.SyncRefund;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.LogsValidationHelper;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.PG2LogsValidationHelper;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.response.FetchPaymentOptResponseDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.SimplifiedUnifiedOffers;
import com.paytm.dto.NativeDTO.InitTxn.TxnAmount;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.NativeDTO.OfferApply.Item;
import com.paytm.dto.NativeDTO.OfferApply.OfferApplyDTO;
import com.paytm.dto.NativeDTO.OfferApply.PaymentDetails;
import com.paytm.dto.NativeDTO.OfferApply.PaymentOption;
import com.paytm.dto.NativeDTO.OfferApply.Tenure;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.dto.processTransactionV1.response.ProcessTxnV1Response;
import com.paytm.framework.reporting.Reporter;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutJsCheckoutPage;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.NativePlusHoldpayPage;
import com.paytm.pages.ResponsePage;
import com.paytm.apphelpers.QRHelper;
import com.paytm.utils.merchant.util.PayMethodType;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static com.paytm.appconstants.Constants.Owner.CHAKSHU;
import static com.paytm.dto.PaymentDTO.AlternateID_VISA_CARD;
import static io.restassured.RestAssured.given;

public class RefundValidation extends PGPBaseTest {

    protected final ThreadLocal<FetchPaymentOptResponseDTO> fetchPaymentOptResponseDTO = new ThreadLocal<>();
    private final CheckoutPage checkoutPage = new CheckoutPage();
    private final CheckoutJsCheckoutPage checkoutJsPage = new CheckoutJsCheckoutPage();
    private final NativePlusHoldpayPage nativePlusHoldpayPage = new NativePlusHoldpayPage();

    private static final String NATIVE_PLUS_JSON_POST =
            "/checkoutpage/new_nplus_page.jsp?env=" + LocalConfig.ENV_NAME;
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

    public void assertPTCCommonResponse(ProcessTxnV1Response ptcResponse){
        Assertions.assertThat(ptcResponse.getBody().getBankForm().getPageType()).isEqualTo("redirect");
        Assertions.assertThat(ptcResponse.getBody().getBankForm().getRedirectForm().getActionUrl()).isNotNull();
        Assertions.assertThat(ptcResponse.getBody().getBankForm().getRedirectForm().getMethod()).isEqualTo("post");
        Assertions.assertThat(ptcResponse.getBody().getBankForm().getRedirectForm().getType()).isEqualTo("redirect");
        Assertions.assertThat(ptcResponse.getBody().getBankForm().getRedirectForm().getHeaders()).isNotNull();
        Assertions.assertThat(ptcResponse.getBody().getBankForm().getRedirectForm().getHeaders()).isNotNull();
        Assertions.assertThat(ptcResponse.getBody().getBankForm().getRedirectForm().getContent()).isNotNull();
    }

    @Owner(CHAKSHU)
    @Feature("PGP-54522")
    @Test(description = "Validate risk refund")
    public void validateriskrefundforRiskUser() throws Exception {
        User user = userManager.getForRead(Label.RISKREFUNDUSER);
        Constants.MerchantType merchant = Constants.MerchantType.PG2_CC_DC;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderID=initTxnDTO.getBody().getOrderId();
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
                .validatePaymentMode("DC")
                .validateMid(mid)
                .AssertAll();


        SyncRefund syncRefund = new SyncRefund();
        Response asyncRefundResp = given().spec(syncRefund.reqSpecAsyncRiskInfo(Constants.MerchantType.PG2_CC_DC, initTxnDTO.txnAmountFromBody(), initTxnDTO.getBody().getOrderId(),
                        txnStatus.getResponse().getTXNID()))
                .post().then().extract().response();
        System.out.println("Async Refund Response " + asyncRefundResp.body().prettyPrint());
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.orderId")).isEqualTo(initTxnDTO.getBody().getOrderId());
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.mid")).isEqualTo(mid);
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.refId")).isNotNull();
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("TXN_FAILURE");
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo("680");
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("Refund rejected by Paytm due to risk checks");

    }

    @Owner(CHAKSHU)
    @Feature("PGP-54522")
    @Test(description = "Validate risk refund Credit Card")
    public void validateriskrefundforRiskUserCC() throws Exception {
        User user = userManager.getForRead(Label.RISKREFUNDUSER);
        Constants.MerchantType merchant = Constants.MerchantType.PG2_CC_DC;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderID=initTxnDTO.getBody().getOrderId();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "CREDIT_CARD", "false");
        String mid = merchant.getId();
        PaymentDTO paymentDTO = new PaymentDTO();
        String cardInfo = "|" +paymentDTO.getCreditCardNumber()+"|"+paymentDTO.getCvvNumber() + "|" + paymentDTO.getExpMonth()+ paymentDTO.getExpYear();
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
        Response asyncRefundResp = given().spec(syncRefund.reqSpecAsyncRiskInfo(Constants.MerchantType.PG2_CC_DC, initTxnDTO.txnAmountFromBody(), initTxnDTO.getBody().getOrderId(),
                        txnStatus.getResponse().getTXNID()))
                .post().then().extract().response();
        System.out.println("Async Refund Response " + asyncRefundResp.body().prettyPrint());
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.orderId")).isEqualTo(initTxnDTO.getBody().getOrderId());
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.mid")).isEqualTo(mid);
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.refId")).isNotNull();
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("TXN_FAILURE");
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo("680");
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("Refund rejected by Paytm due to risk checks");

    }

    @Owner(CHAKSHU)
    @Feature("PGP-54522")
    @Test(description = "validate risk refund for CC Non LoggedIn More Than 50k Flag On")
    public void validateriskrefundforCCNonLoggedInMoreThan50kFlagOn() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.PG2_CC_DC;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(merchant).setTxnValue("60000").build();
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

        SyncRefund syncRefund = new SyncRefund();
        Response asyncRefundResp = given().spec(syncRefund.reqSpecAsyncRiskInfo(Constants.MerchantType.PG2_CC_DC, initTxnDTO.txnAmountFromBody(), initTxnDTO.getBody().getOrderId(),
                        txnStatus.getResponse().getTXNID()))
                .post().then().extract().response();
        System.out.println("Async Refund Response " + asyncRefundResp.body().prettyPrint());
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.orderId")).isEqualTo(initTxnDTO.getBody().getOrderId());
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.mid")).isEqualTo(mid);
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.refId")).isNotNull();
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("TXN_FAILURE");
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo("680");
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("Refund rejected by Paytm due to risk checks");

    }

    @Owner(CHAKSHU)
    @Feature("PGP-54522")
    @Test(description = "validate risk refund for Non Logged More Than 50k In FlagOn")
    public void validateriskrefundforNonLoggedMoreThan50kInFlagOn() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.PG2_CC_DC;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(merchant).setTxnValue("60000").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderID=initTxnDTO.getBody().getOrderId();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "CREDIT_CARD", "false");
        String mid = merchant.getId();
        PaymentDTO paymentDTO = new PaymentDTO();
        String cardInfo = "|" +paymentDTO.DEBIT_CARD_NUMBER+"|"+paymentDTO.getCvvNumber() + "|" + paymentDTO.getExpMonth()+ paymentDTO.getExpYear();
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
                .validatePaymentMode("DC")
                .validateMid(mid)
                .AssertAll();

        SyncRefund syncRefund = new SyncRefund();
        Response asyncRefundResp = given().spec(syncRefund.reqSpecAsyncRiskInfo(Constants.MerchantType.PG2_CC_DC, initTxnDTO.txnAmountFromBody(), initTxnDTO.getBody().getOrderId(),
                        txnStatus.getResponse().getTXNID()))
                .post().then().extract().response();
        System.out.println("Async Refund Response " + asyncRefundResp.body().prettyPrint());
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.orderId")).isEqualTo(initTxnDTO.getBody().getOrderId());
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.mid")).isEqualTo(mid);
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.refId")).isNotNull();
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("TXN_FAILURE");
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo("680");
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("Refund rejected by Paytm due to risk checks");

    }

    @Owner(CHAKSHU)
    @Feature("PGP-54522")
    @Test(description = "validate risk refund for CC Non LoggedIn Equal to 50k Flag On")
    public void validateriskrefundforCCNonLoggedInEqualto50kFlagOn() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.PG2_CC_DC;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(merchant).setTxnValue("60000").build();
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

        SyncRefund syncRefund = new SyncRefund();
        Response asyncRefundResp = given().spec(syncRefund.reqSpecAsyncRiskInfo(Constants.MerchantType.PG2_CC_DC, "50000", initTxnDTO.getBody().getOrderId(),
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

    }

    @Owner(CHAKSHU)
    @Feature("PGP-54522")
    @Test(description = "validate risk refund for Non LoggedIn Equal to 50k Flag On ")
    public void validateriskrefundforNonLoggedInEqualto50kFlagOn() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.PG2_CC_DC;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(merchant).setTxnValue("60000").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderID=initTxnDTO.getBody().getOrderId();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "CREDIT_CARD", "false");
        String mid = merchant.getId();
        PaymentDTO paymentDTO = new PaymentDTO();
        String cardInfo = "|" +paymentDTO.DEBIT_CARD_NUMBER+"|"+paymentDTO.getCvvNumber() + "|" + paymentDTO.getExpMonth()+ paymentDTO.getExpYear();
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
                .validatePaymentMode("DC")
                .validateMid(mid)
                .AssertAll();

        SyncRefund syncRefund = new SyncRefund();
        Response asyncRefundResp = given().spec(syncRefund.reqSpecAsyncRiskInfo(Constants.MerchantType.PG2_CC_DC, "50000", initTxnDTO.getBody().getOrderId(),
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

    }

    @Owner(CHAKSHU)
    @Feature("PGP-54522")
    @Test(description = "validate risk refund for CC Non LoggedIn FlagOn less than 50k")
    public void validateriskrefundforCCNonLoggedInFlagOnlessthan50k() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.PG2_CC_DC;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(merchant).setTxnValue("60000").build();
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

        SyncRefund syncRefund = new SyncRefund();
        Response asyncRefundResp = given().spec(syncRefund.reqSpecAsyncRiskInfo(Constants.MerchantType.PG2_CC_DC, "1", initTxnDTO.getBody().getOrderId(),
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

    }

    @Owner(CHAKSHU)
    @Feature("PGP-54522")
    @Test(description = "validate risk refund CC for Non Risk User FlagOn ")
    public void validateriskrefundCCforNonRiskUserFlagOn() throws Exception {
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

        SyncRefund syncRefund = new SyncRefund();
        Response asyncRefundResp = given().spec(syncRefund.reqSpecAsyncRiskInfo(Constants.MerchantType.PG2_CC_DC, initTxnDTO.txnAmountFromBody(), initTxnDTO.getBody().getOrderId(),
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

    }

    @Owner(CHAKSHU)
    @Feature("PGP-54522")
    @Test(description = "validate risk refund for Non Risk User Flag On ")
    public void validateriskrefundforNonRiskUserFlagOn() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.PG2_CC_DC;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderID=initTxnDTO.getBody().getOrderId();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "CREDIT_CARD", "false");
        String mid = merchant.getId();
        PaymentDTO paymentDTO = new PaymentDTO();
        String cardInfo = "|" +paymentDTO.DEBIT_CARD_NUMBER+"|"+paymentDTO.getCvvNumber() + "|" + paymentDTO.getExpMonth()+ paymentDTO.getExpYear();
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
                .validatePaymentMode("DC")
                .validateMid(mid)
                .AssertAll();

        SyncRefund syncRefund = new SyncRefund();
        Response asyncRefundResp = given().spec(syncRefund.reqSpecAsyncRiskInfo(Constants.MerchantType.PG2_CC_DC, initTxnDTO.txnAmountFromBody(), initTxnDTO.getBody().getOrderId(),
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

    }

    @Owner(CHAKSHU)
    @Feature("PGP-54522")
    @Test(description = "validate risk refund for CC Non Risk User FlagOn more than 50k ")
    public void validateriskrefundforCCNonRiskUserFlagOnmorethan50k() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.PG2_CC_DC;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue("60000").build();
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

        SyncRefund syncRefund = new SyncRefund();
        Response asyncRefundResp = given().spec(syncRefund.reqSpecAsyncRiskInfo(Constants.MerchantType.PG2_CC_DC, "50001", initTxnDTO.getBody().getOrderId(),
                        txnStatus.getResponse().getTXNID()))
                .post().then().extract().response();
        System.out.println("Async Refund Response " + asyncRefundResp.body().prettyPrint());
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.orderId")).isEqualTo(initTxnDTO.getBody().getOrderId());
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.mid")).isEqualTo(mid);
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.refId")).isNotNull();
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("TXN_FAILURE");
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo("680");
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("Refund rejected by Paytm due to risk checks");

    }

    @Owner(CHAKSHU)
    @Feature("PGP-54522")
    @Test(description = "validate risk refund for Non Risk User Flag On more than 50k")
    public void validateriskrefundforNonRiskUserFlagOnmorethan50k() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.PG2_CC_DC;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue("60000").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderID=initTxnDTO.getBody().getOrderId();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "CREDIT_CARD", "false");
        String mid = merchant.getId();
        PaymentDTO paymentDTO = new PaymentDTO();
        String cardInfo = "|" +paymentDTO.DEBIT_CARD_NUMBER+"|"+paymentDTO.getCvvNumber() + "|" + paymentDTO.getExpMonth()+ paymentDTO.getExpYear();
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
                .validatePaymentMode("DC")
                .validateMid(mid)
                .AssertAll();

        SyncRefund syncRefund = new SyncRefund();
        Response asyncRefundResp = given().spec(syncRefund.reqSpecAsyncRiskInfo(Constants.MerchantType.PG2_CC_DC, "50001", initTxnDTO.getBody().getOrderId(),
                        txnStatus.getResponse().getTXNID()))
                .post().then().extract().response();
        System.out.println("Async Refund Response " + asyncRefundResp.body().prettyPrint());
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.orderId")).isEqualTo(initTxnDTO.getBody().getOrderId());
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.mid")).isEqualTo(mid);
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.refId")).isNotNull();
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("TXN_FAILURE");
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo("680");
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("Refund rejected by Paytm due to risk checks");

    }

    @Owner(CHAKSHU)
    @Feature("PGP-54522")
    @Test(description = "validate risk refund for CC Non Risk User Flag On less than 50k")
    public void validateriskrefundforCCNonRiskUserFlagOnlessthan50k() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.PG2_CC_DC;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue("60000").build();
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

        SyncRefund syncRefund = new SyncRefund();
        Response asyncRefundResp = given().spec(syncRefund.reqSpecAsyncRiskInfo(Constants.MerchantType.PG2_CC_DC, "1", initTxnDTO.getBody().getOrderId(),
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

    }

    @Owner(CHAKSHU)
    @Feature("PGP-54522")
    @Test(description = "validate risk refund for NonRisk User FlagOn less than 50k")
    public void validateriskrefundforNonRiskUserFlagOnlessthan50k() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.PG2_CC_DC;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue("60000").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderID=initTxnDTO.getBody().getOrderId();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "CREDIT_CARD", "false");
        String mid = merchant.getId();
        PaymentDTO paymentDTO = new PaymentDTO();
        String cardInfo = "|" +paymentDTO.DEBIT_CARD_NUMBER+"|"+paymentDTO.getCvvNumber() + "|" + paymentDTO.getExpMonth()+ paymentDTO.getExpYear();
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
                .validatePaymentMode("DC")
                .validateMid(mid)
                .AssertAll();

        SyncRefund syncRefund = new SyncRefund();
        Response asyncRefundResp = given().spec(syncRefund.reqSpecAsyncRiskInfo(Constants.MerchantType.PG2_CC_DC, "1", initTxnDTO.getBody().getOrderId(),
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

    }

    @Owner(CHAKSHU)
    @Feature("PGP-54522")
    @Test(description = "validate risk refund for CC Non Risk User FlagOn equal to 50k ")
    public void validateriskrefundforCCNonRiskUserFlagOnequalto50k() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.PG2_CC_DC;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue("60000").build();
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

        SyncRefund syncRefund = new SyncRefund();
        Response asyncRefundResp = given().spec(syncRefund.reqSpecAsyncRiskInfo(Constants.MerchantType.PG2_CC_DC, "50000", initTxnDTO.getBody().getOrderId(),
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

    }

    @Owner(CHAKSHU)
    @Feature("PGP-54522")
    @Test(description = " validate risk refund for Non Risk User Flag On equal to 50k ")
    public void validateriskrefundforNonRiskUserFlagOnequalto50k() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.PG2_CC_DC;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue("60000").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderID=initTxnDTO.getBody().getOrderId();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "CREDIT_CARD", "false");
        String mid = merchant.getId();
        PaymentDTO paymentDTO = new PaymentDTO();
        String cardInfo = "|" +paymentDTO.DEBIT_CARD_NUMBER+"|"+paymentDTO.getCvvNumber() + "|" + paymentDTO.getExpMonth()+ paymentDTO.getExpYear();
        OrderDTO orderDTO = new OrderFactory.Native(merchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .setORDER_ID(orderID)
                .setCardInfo(cardInfo)
                .setAUTH_MODE("otp")
                .setCHANNEL_ID("WEB").build();

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
                .validatePaymentMode("DC")
                .validateMid(mid)
                .AssertAll();

        SyncRefund syncRefund = new SyncRefund();
        Response asyncRefundResp = given().spec(syncRefund.reqSpecAsyncRiskInfo(Constants.MerchantType.PG2_CC_DC, "50000", initTxnDTO.getBody().getOrderId(),
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

    }

    @Owner(CHAKSHU)
    @Feature("PGP-54522")
    @Test(description = "Validate risk refund")
    public void validateriskrefundforRiskUserMasterRefund() throws Exception {
        User user = userManager.getForRead(Label.RISKREFUNDUSER);
        Constants.MerchantType merchant = Constants.MerchantType.RISK_REFUND_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderID=initTxnDTO.getBody().getOrderId();
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
                .validatePaymentMode("DC")
                .validateMid(mid)
                .AssertAll();

        String RefundAmount = initTxnDTO.txnAmountFromBody();
        MasterRefund masterRefund = new MasterRefund( Constants.MerchantType.RISK_REFUND_MID,txnStatus.getResponse().getTXNID(),orderID, RefundAmount);
        JsonPath jsonpath=masterRefund.execute().jsonPath();
        System.out.println(jsonpath);
        Assertions.assertThat(jsonpath.getString("STATUS")).isEqualTo("TXN_FAILURE");
        Assertions.assertThat(jsonpath.getString("RESPCODE")).isEqualTo("680");
        Assertions.assertThat(jsonpath.getString("RESPMSG")).isEqualTo("Refund rejected by Paytm due to risk checks");
        Assertions.assertThat(jsonpath.getString("TXNID")).isEqualTo(txnStatus.getResponse().getTXNID());
        Assertions.assertThat(jsonpath.getString("MID")).isEqualTo(mid);
        Assertions.assertThat(jsonpath.getString("REFID")).isNotNull();

    }

    @Owner(CHAKSHU)
    @Feature("PGP-54522")
    @Test(description = "Validate risk refund Credit Card MasterRefund")
    public void validateriskrefundforRiskUserCCMasterRefund() throws Exception {
        User user = userManager.getForRead(Label.RISKREFUNDUSER);
        Constants.MerchantType merchant = Constants.MerchantType.RISK_REFUND_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderID=initTxnDTO.getBody().getOrderId();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "CREDIT_CARD", "false");
        String mid = merchant.getId();
        PaymentDTO paymentDTO = new PaymentDTO();
        String cardInfo = "|" +paymentDTO.getCreditCardNumber()+"|"+paymentDTO.getCvvNumber() + "|" + paymentDTO.getExpMonth()+ paymentDTO.getExpYear();
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

        String RefundAmount = initTxnDTO.txnAmountFromBody();
        MasterRefund masterRefund = new MasterRefund( Constants.MerchantType.RISK_REFUND_MID,txnStatus.getResponse().getTXNID(),orderID, RefundAmount);
        JsonPath jsonpath=masterRefund.execute().jsonPath();
        System.out.println(jsonpath);
        Assertions.assertThat(jsonpath.getString("STATUS")).isEqualTo("TXN_FAILURE");
        Assertions.assertThat(jsonpath.getString("RESPCODE")).isEqualTo("680");
        Assertions.assertThat(jsonpath.getString("RESPMSG")).isEqualTo("Refund rejected by Paytm due to risk checks");
        Assertions.assertThat(jsonpath.getString("TXNID")).isEqualTo(txnStatus.getResponse().getTXNID());
        Assertions.assertThat(jsonpath.getString("MID")).isEqualTo(mid);
        Assertions.assertThat(jsonpath.getString("REFID")).isNotNull();

    }

    @Owner(CHAKSHU)
    @Feature("PGP-54522")
    @Test(description = "validate risk refund for CC Non LoggedIn More Than 50k Flag On MasterRefund")
    public void validateriskrefundforCCNonLoggedInMoreThan50kFlagOnMasterRefund() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.RISK_REFUND_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(merchant).setTxnValue("60000").build();
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

        String RefundAmount = initTxnDTO.txnAmountFromBody();
        MasterRefund masterRefund = new MasterRefund( Constants.MerchantType.RISK_REFUND_MID,txnStatus.getResponse().getTXNID(),orderID, RefundAmount);
        JsonPath jsonpath=masterRefund.execute().jsonPath();
        System.out.println(jsonpath);
        Assertions.assertThat(jsonpath.getString("STATUS")).isEqualTo("TXN_FAILURE");
        Assertions.assertThat(jsonpath.getString("RESPCODE")).isEqualTo("680");
        Assertions.assertThat(jsonpath.getString("RESPMSG")).isEqualTo("Refund rejected by Paytm due to risk checks");
        Assertions.assertThat(jsonpath.getString("TXNID")).isEqualTo(txnStatus.getResponse().getTXNID());
        Assertions.assertThat(jsonpath.getString("MID")).isEqualTo(mid);
        Assertions.assertThat(jsonpath.getString("REFID")).isNotNull();

    }

    @Owner(CHAKSHU)
    @Feature("PGP-54522")
    @Test(description = "validate risk refund for Non Logged More Than 50k In FlagOn MasterRefund")
    public void validateriskrefundforNonLoggedMoreThan50kInFlagOnMasterRefund() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.RISK_REFUND_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(merchant).setTxnValue("60000").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderID=initTxnDTO.getBody().getOrderId();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "CREDIT_CARD", "false");
        String mid = merchant.getId();
        PaymentDTO paymentDTO = new PaymentDTO();
        String cardInfo = "|" +paymentDTO.DEBIT_CARD_NUMBER+"|"+paymentDTO.getCvvNumber() + "|" + paymentDTO.getExpMonth()+ paymentDTO.getExpYear();
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
                .validatePaymentMode("DC")
                .validateMid(mid)
                .AssertAll();

        String RefundAmount = initTxnDTO.txnAmountFromBody();
        MasterRefund masterRefund = new MasterRefund( Constants.MerchantType.RISK_REFUND_MID,txnStatus.getResponse().getTXNID(),orderID, RefundAmount);
        JsonPath jsonpath=masterRefund.execute().jsonPath();
        System.out.println(jsonpath);
        Assertions.assertThat(jsonpath.getString("STATUS")).isEqualTo("TXN_FAILURE");
        Assertions.assertThat(jsonpath.getString("RESPCODE")).isEqualTo("680");
        Assertions.assertThat(jsonpath.getString("RESPMSG")).isEqualTo("Refund rejected by Paytm due to risk checks");
        Assertions.assertThat(jsonpath.getString("TXNID")).isEqualTo(txnStatus.getResponse().getTXNID());
        Assertions.assertThat(jsonpath.getString("MID")).isEqualTo(mid);
        Assertions.assertThat(jsonpath.getString("REFID")).isNotNull();

    }

    @Owner(CHAKSHU)
    @Feature("PGP-54522")
    @Test(description = "validate risk refund for CC Non LoggedIn Equal to 50k Flag On MasterRefund")
    public void validateriskrefundforCCNonLoggedInEqualto50kFlagOnMasterRefund() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.RISK_REFUND_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(merchant).setTxnValue("60000").build();
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

        String RefundAmount = "50000";
        MasterRefund masterRefund = new MasterRefund( Constants.MerchantType.RISK_REFUND_MID,txnStatus.getResponse().getTXNID(),orderID, RefundAmount);
        JsonPath jsonpath=masterRefund.execute().jsonPath();
        System.out.println(jsonpath);
        Assertions.assertThat(jsonpath.getString("STATUS")).isEqualTo("TXN_SUCCESS");
        Assertions.assertThat(jsonpath.getString("RESPCODE")).isEqualTo("10");
        Assertions.assertThat(jsonpath.getString("RESPMSG")).isEqualTo("Refund Successfull");
        Assertions.assertThat(jsonpath.getString("TXNID")).isEqualTo(txnStatus.getResponse().getTXNID());
        Assertions.assertThat(jsonpath.getString("MID")).isEqualTo(mid);
        Assertions.assertThat(jsonpath.getString("REFID")).isNotNull();

    }

    @Owner(CHAKSHU)
    @Feature("PGP-54522")
    @Test(description = "validate risk refund for Non LoggedIn Equal to 50k Flag On MasterRefund ")
    public void validateriskrefundforNonLoggedInEqualto50kFlagOnMasterRefund() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.RISK_REFUND_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(merchant).setTxnValue("60000").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderID=initTxnDTO.getBody().getOrderId();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "CREDIT_CARD", "false");
        String mid = merchant.getId();
        PaymentDTO paymentDTO = new PaymentDTO();
        String cardInfo = "|" +paymentDTO.DEBIT_CARD_NUMBER+"|"+paymentDTO.getCvvNumber() + "|" + paymentDTO.getExpMonth()+ paymentDTO.getExpYear();
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
                .validatePaymentMode("DC")
                .validateMid(mid)
                .AssertAll();

        String RefundAmount = "50000";
        MasterRefund masterRefund = new MasterRefund( Constants.MerchantType.RISK_REFUND_MID,txnStatus.getResponse().getTXNID(),orderID, RefundAmount);
        JsonPath jsonpath=masterRefund.execute().jsonPath();
        System.out.println(jsonpath);
        Assertions.assertThat(jsonpath.getString("STATUS")).isEqualTo("TXN_SUCCESS");
        Assertions.assertThat(jsonpath.getString("RESPCODE")).isEqualTo("10");
        Assertions.assertThat(jsonpath.getString("RESPMSG")).isEqualTo("Refund Successfull");
        Assertions.assertThat(jsonpath.getString("TXNID")).isEqualTo(txnStatus.getResponse().getTXNID());
        Assertions.assertThat(jsonpath.getString("MID")).isEqualTo(mid);
        Assertions.assertThat(jsonpath.getString("REFID")).isNotNull();

    }

    @Owner(CHAKSHU)
    @Feature("PGP-54522")
    @Test(description = "validate risk refund for CC Non LoggedIn FlagOn less than 50k MasterRefund")
    public void validateriskrefundforCCNonLoggedInFlagOnlessthan50kMasterRefund() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.RISK_REFUND_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(merchant).setTxnValue("60000").build();
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

        String RefundAmount = "1";
        MasterRefund masterRefund = new MasterRefund( Constants.MerchantType.RISK_REFUND_MID,txnStatus.getResponse().getTXNID(),orderID, RefundAmount);
        JsonPath jsonpath=masterRefund.execute().jsonPath();
        System.out.println(jsonpath);
        Assertions.assertThat(jsonpath.getString("STATUS")).isEqualTo("TXN_SUCCESS");
        Assertions.assertThat(jsonpath.getString("RESPCODE")).isEqualTo("10");
        Assertions.assertThat(jsonpath.getString("RESPMSG")).isEqualTo("Refund Successfull");
        Assertions.assertThat(jsonpath.getString("TXNID")).isEqualTo(txnStatus.getResponse().getTXNID());
        Assertions.assertThat(jsonpath.getString("MID")).isEqualTo(mid);
        Assertions.assertThat(jsonpath.getString("REFID")).isNotNull();

    }

    @Owner(CHAKSHU)
    @Feature("PGP-54522")
    @Test(description = "validate risk refund CC for Non Risk User FlagOn MasterRefund")
    public void validateriskrefundCCforNonRiskUserFlagOnMasterRefund() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.RISK_REFUND_MID;
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

        String RefundAmount = initTxnDTO.txnAmountFromBody();
        MasterRefund masterRefund = new MasterRefund( Constants.MerchantType.RISK_REFUND_MID,txnStatus.getResponse().getTXNID(),orderID, RefundAmount);
        JsonPath jsonpath=masterRefund.execute().jsonPath();
        System.out.println(jsonpath);
        Assertions.assertThat(jsonpath.getString("STATUS")).isEqualTo("TXN_SUCCESS");
        Assertions.assertThat(jsonpath.getString("RESPCODE")).isEqualTo("10");
        Assertions.assertThat(jsonpath.getString("RESPMSG")).isEqualTo("Refund Successfull");
        Assertions.assertThat(jsonpath.getString("TXNID")).isEqualTo(txnStatus.getResponse().getTXNID());
        Assertions.assertThat(jsonpath.getString("MID")).isEqualTo(mid);
        Assertions.assertThat(jsonpath.getString("REFID")).isNotNull();

    }

    @Owner(CHAKSHU)
    @Feature("PGP-54522")
    @Test(description = "validate risk refund for Non Risk User Flag On MasterRefund")
    public void validateriskrefundforNonRiskUserFlagOnMasterRefund() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.RISK_REFUND_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderID=initTxnDTO.getBody().getOrderId();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "CREDIT_CARD", "false");
        String mid = merchant.getId();
        PaymentDTO paymentDTO = new PaymentDTO();
        String cardInfo = "|" +paymentDTO.DEBIT_CARD_NUMBER+"|"+paymentDTO.getCvvNumber() + "|" + paymentDTO.getExpMonth()+ paymentDTO.getExpYear();
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
                .validatePaymentMode("DC")
                .validateMid(mid)
                .AssertAll();

        String RefundAmount = initTxnDTO.txnAmountFromBody();
        MasterRefund masterRefund = new MasterRefund( Constants.MerchantType.RISK_REFUND_MID,txnStatus.getResponse().getTXNID(),orderID, RefundAmount);
        JsonPath jsonpath=masterRefund.execute().jsonPath();
        System.out.println(jsonpath);
        Assertions.assertThat(jsonpath.getString("STATUS")).isEqualTo("TXN_SUCCESS");
        Assertions.assertThat(jsonpath.getString("RESPCODE")).isEqualTo("10");
        Assertions.assertThat(jsonpath.getString("RESPMSG")).isEqualTo("Refund Successfull");
        Assertions.assertThat(jsonpath.getString("TXNID")).isEqualTo(txnStatus.getResponse().getTXNID());
        Assertions.assertThat(jsonpath.getString("MID")).isEqualTo(mid);
        Assertions.assertThat(jsonpath.getString("REFID")).isNotNull();

    }

    @Owner(CHAKSHU)
    @Feature("PGP-54522")
    @Test(description = "validate risk refund for CC Non Risk User FlagOn more than 50k MasterRefund")
    public void validateriskrefundforCCNonRiskUserFlagOnmorethan50kMasterRefund() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.RISK_REFUND_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue("60000").build();
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

        String RefundAmount = "50001";
        MasterRefund masterRefund = new MasterRefund( Constants.MerchantType.RISK_REFUND_MID,txnStatus.getResponse().getTXNID(),orderID, RefundAmount);
        JsonPath jsonpath=masterRefund.execute().jsonPath();
        System.out.println(jsonpath);
        Assertions.assertThat(jsonpath.getString("STATUS")).isEqualTo("TXN_FAILURE");
        Assertions.assertThat(jsonpath.getString("RESPCODE")).isEqualTo("680");
        Assertions.assertThat(jsonpath.getString("RESPMSG")).isEqualTo("Refund rejected by Paytm due to risk checks");
        Assertions.assertThat(jsonpath.getString("TXNID")).isEqualTo(txnStatus.getResponse().getTXNID());
        Assertions.assertThat(jsonpath.getString("MID")).isEqualTo(mid);
        Assertions.assertThat(jsonpath.getString("REFID")).isNotNull();

    }

    @Owner(CHAKSHU)
    @Feature("PGP-54522")
    @Test(description = "validate risk refund for Non Risk User Flag On more than 50k MasterRefund")
    public void validateriskrefundforNonRiskUserFlagOnmorethan50kMasterRefund() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.RISK_REFUND_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue("60000").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderID=initTxnDTO.getBody().getOrderId();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "CREDIT_CARD", "false");
        String mid = merchant.getId();
        PaymentDTO paymentDTO = new PaymentDTO();
        String cardInfo = "|" +paymentDTO.DEBIT_CARD_NUMBER+"|"+paymentDTO.getCvvNumber() + "|" + paymentDTO.getExpMonth()+ paymentDTO.getExpYear();
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
                .validatePaymentMode("DC")
                .validateMid(mid)
                .AssertAll();

        String RefundAmount = "50001";
        MasterRefund masterRefund = new MasterRefund( Constants.MerchantType.RISK_REFUND_MID,txnStatus.getResponse().getTXNID(),orderID, RefundAmount);
        JsonPath jsonpath=masterRefund.execute().jsonPath();
        System.out.println(jsonpath);
        Assertions.assertThat(jsonpath.getString("STATUS")).isEqualTo("TXN_FAILURE");
        Assertions.assertThat(jsonpath.getString("RESPCODE")).isEqualTo("680");
        Assertions.assertThat(jsonpath.getString("RESPMSG")).isEqualTo("Refund rejected by Paytm due to risk checks");
        Assertions.assertThat(jsonpath.getString("TXNID")).isEqualTo(txnStatus.getResponse().getTXNID());
        Assertions.assertThat(jsonpath.getString("MID")).isEqualTo(mid);
        Assertions.assertThat(jsonpath.getString("REFID")).isNotNull();

    }

    @Owner(CHAKSHU)
    @Feature("PGP-54522")
    @Test(description = "validate risk refund for CC Non Risk User Flag On less than 50k MasterRefund")
    public void validateriskrefundforCCNonRiskUserFlagOnlessthan50kMasterRefund() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.RISK_REFUND_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue("60000").build();
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

        String RefundAmount = "1";
        MasterRefund masterRefund = new MasterRefund( Constants.MerchantType.RISK_REFUND_MID,txnStatus.getResponse().getTXNID(),orderID, RefundAmount);
        JsonPath jsonpath=masterRefund.execute().jsonPath();
        System.out.println(jsonpath);
        Assertions.assertThat(jsonpath.getString("STATUS")).isEqualTo("TXN_SUCCESS");
        Assertions.assertThat(jsonpath.getString("RESPCODE")).isEqualTo("10");
        Assertions.assertThat(jsonpath.getString("RESPMSG")).isEqualTo("Refund Successfull");
        Assertions.assertThat(jsonpath.getString("TXNID")).isEqualTo(txnStatus.getResponse().getTXNID());
        Assertions.assertThat(jsonpath.getString("MID")).isEqualTo(mid);
        Assertions.assertThat(jsonpath.getString("REFID")).isNotNull();

    }

    @Owner(CHAKSHU)
    @Feature("PGP-54522")
    @Test(description = "validate risk refund for NonRisk User FlagOn less than 50k MasterRefund")
    public void validateriskrefundforNonRiskUserFlagOnlessthan50kMasterRefund() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.RISK_REFUND_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue("60000").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderID=initTxnDTO.getBody().getOrderId();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "CREDIT_CARD", "false");
        String mid = merchant.getId();
        PaymentDTO paymentDTO = new PaymentDTO();
        String cardInfo = "|" +paymentDTO.DEBIT_CARD_NUMBER+"|"+paymentDTO.getCvvNumber() + "|" + paymentDTO.getExpMonth()+ paymentDTO.getExpYear();
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
                .validatePaymentMode("DC")
                .validateMid(mid)
                .AssertAll();

        String RefundAmount = "1";
        MasterRefund masterRefund = new MasterRefund( Constants.MerchantType.RISK_REFUND_MID,txnStatus.getResponse().getTXNID(),orderID, RefundAmount);
        JsonPath jsonpath=masterRefund.execute().jsonPath();
        System.out.println(jsonpath);
        Assertions.assertThat(jsonpath.getString("STATUS")).isEqualTo("TXN_SUCCESS");
        Assertions.assertThat(jsonpath.getString("RESPCODE")).isEqualTo("10");
        Assertions.assertThat(jsonpath.getString("RESPMSG")).isEqualTo("Refund Successfull");
        Assertions.assertThat(jsonpath.getString("TXNID")).isEqualTo(txnStatus.getResponse().getTXNID());
        Assertions.assertThat(jsonpath.getString("MID")).isEqualTo(mid);
        Assertions.assertThat(jsonpath.getString("REFID")).isNotNull();

    }

    @Owner(CHAKSHU)
    @Feature("PGP-54522")
    @Test(description = "validate risk refund for CC Non Risk User FlagOn equal to 50k MasterRefund")
    public void validateriskrefundforCCNonRiskUserFlagOnequalto50kMasterRefund() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.RISK_REFUND_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue("60000").build();
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

        String RefundAmount = "50000";
        MasterRefund masterRefund = new MasterRefund( Constants.MerchantType.RISK_REFUND_MID,txnStatus.getResponse().getTXNID(),orderID, RefundAmount);
        JsonPath jsonpath=masterRefund.execute().jsonPath();
        System.out.println(jsonpath);
        Assertions.assertThat(jsonpath.getString("STATUS")).isEqualTo("TXN_SUCCESS");
        Assertions.assertThat(jsonpath.getString("RESPCODE")).isEqualTo("10");
        Assertions.assertThat(jsonpath.getString("RESPMSG")).isEqualTo("Refund Successfull");
        Assertions.assertThat(jsonpath.getString("TXNID")).isEqualTo(txnStatus.getResponse().getTXNID());
        Assertions.assertThat(jsonpath.getString("MID")).isEqualTo(mid);
        Assertions.assertThat(jsonpath.getString("REFID")).isNotNull();

    }

    @Owner(CHAKSHU)
    @Feature("PGP-54522")
    @Test(description = " validate risk refund for Non Risk User Flag On equal to 50k MasterRefund")
    public void validateriskrefundforNonRiskUserFlagOnequalto50kMasterRefund() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.RISK_REFUND_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue("60000").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderID=initTxnDTO.getBody().getOrderId();
      //  Validate_FetchPayInstrument(txnToken, initTxnDTO, "CREDIT_CARD", "false");
        String mid = merchant.getId();
        PaymentDTO paymentDTO = new PaymentDTO();
        String cardInfo = "|" +paymentDTO.DEBIT_CARD_NUMBER+"|"+paymentDTO.getCvvNumber() + "|" + paymentDTO.getExpMonth()+ paymentDTO.getExpYear();
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
                .validatePaymentMode("DC")
                .validateMid(mid)
                .AssertAll();

        String RefundAmount = "50000";
        MasterRefund masterRefund = new MasterRefund( Constants.MerchantType.RISK_REFUND_MID,txnStatus.getResponse().getTXNID(),orderID, RefundAmount);
        JsonPath jsonpath=masterRefund.execute().jsonPath();
        System.out.println(jsonpath);
        Assertions.assertThat(jsonpath.getString("STATUS")).isEqualTo("TXN_SUCCESS");
        Assertions.assertThat(jsonpath.getString("RESPCODE")).isEqualTo("10");
        Assertions.assertThat(jsonpath.getString("RESPMSG")).isEqualTo("Refund Successfull");
        Assertions.assertThat(jsonpath.getString("TXNID")).isEqualTo(txnStatus.getResponse().getTXNID());
        Assertions.assertThat(jsonpath.getString("MID")).isEqualTo(mid);
        Assertions.assertThat(jsonpath.getString("REFID")).isNotNull();

    }


    @Owner(CHAKSHU)
    @Feature("PGP-54522")
    @Test(description = "Validate async refund when ACQUIRING_REFUND is giving system error")
    public void validateAsyncRefundforSystemErrorFromACQUIRING_REFUND() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.ACQUIRING_REFUND_SYSTEM_ERROR_MID;
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
        Response asyncRefundResp = given().spec(syncRefund.reqSpecAsyncRiskInfo(Constants.MerchantType.ACQUIRING_REFUND_SYSTEM_ERROR_MID, initTxnDTO.txnAmountFromBody(), initTxnDTO.getBody().getOrderId(),
                        txnStatus.getResponse().getTXNID()))
                .post().then().extract().response();
        System.out.println("Async Refund Response " + asyncRefundResp.body().prettyPrint());
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.orderId")).isEqualTo(initTxnDTO.getBody().getOrderId());
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.mid")).isEqualTo(mid);
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("TXN_FAILURE");
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo("652");
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("Refund failed due to system issue. Try again later.");

    }

    @Owner(CHAKSHU)
    @Feature("PGP-54522")
    @Test(description = "Validate async refund when ACQUIRING_REFUND is giving system error")
    public void validateMasterRefundforSystemErrorFromACQUIRING_REFUND() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.ACQUIRING_REFUND_SYSTEM_ERROR_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "CREDIT_CARD", "false");
        String mid = merchant.getId();
        String orderId = initTxnDTO.orderFromBody();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("CREDIT_CARD")
                .setAuthMode("3D")
                .setCardInfo("|4718650100010336|644|012030")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        NativeHelpers.submitJsonFormInBrowser(ptcResponse.toString());
        ResponsePage responsePage = new ResponsePage();

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


        MasterRefund masterRefund = new MasterRefund( Constants.MerchantType.ACQUIRING_REFUND_SYSTEM_ERROR_MID,txnStatus.getResponse().getTXNID(),orderId, "1");
        JsonPath jsonpath=masterRefund.execute().jsonPath();
        System.out.println(jsonpath);
        Assertions.assertThat(jsonpath.getString("STATUS")).isEqualTo("TXN_FAILURE");
        Assertions.assertThat(jsonpath.getString("RESPCODE")).isEqualTo("652");
        Assertions.assertThat(jsonpath.getString("RESPMSG")).isEqualTo("Refund failed due to system issue. Try again later.");
        Assertions.assertThat(jsonpath.getString("TXNID")).isEqualTo(txnStatus.getResponse().getTXNID());
        Assertions.assertThat(jsonpath.getString("MID")).isEqualTo(mid);

    }

    @Owner(CHAKSHU)
    @Feature("PG-3933")
    @Test(description = "Validate async refund UPI Intent redirected then UPI Intent success in second pay leg")
    public void ValidateasyncRefundUPIIntentRedirectedThenUPIIntentSuccessInSecondpayleg() throws Exception {
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

        

        ProcessTxnV1Request processTxnV1Request1 = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("3D")
                .setRiskExtendInfo("userLBSLatitude:26.88|userLBSLongitude:81.01|userAgent:Mozilla/5.0 (Linux; Android 8.1.0; Redmi Note 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36|timeZone:Asia/Calcutta|operationType:PAYMENT|networkType:4g|businessFlow:JS_CHECKOUT|amount:2|language:en-US|screenResolution:393X786|deviceType:mobile|channelId:WAP|platform:mWeb|osType:Android|deviceModel:Redmi Note 5|browserType:Chrome|browserVersion:96.0.4664.104|osVersion:8.1.0|deviceManufacturer:Xiaomi")
                .build();
        ProcessTxnV1Response ptcResponse1 = NativeHelpers.executeProcessTxnV1(processTxnV1Request1);
        Assertions.assertThat(ptcResponse1.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        String deeplink1 = ptcResponse1.getBody().getDeepLinkInfo().getDeepLink();
        String externalSerialNo1 = deeplink.substring(deeplink1.indexOf("tr")+3,deeplink.indexOf("&am"));
        String bankRrn1 = CommonHelpers.generateOrderId();
        System.out.println(externalSerialNo1);
        Assertions.assertThat(ptcResponse1.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse1.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");


        UpiPspConsultFee upiPspConsultFeeobject =new UpiPspConsultFee( mid,  txnAmountupipsp, paymentFee ,  payeeVPA,  externalSerialNo1 ,  payerPaymentInstrument,  subAccountTypeValue) ;
        JsonPath responseJsonPath = upiPspConsultFeeobject.execute().jsonPath();
        Assertions.assertThat(responseJsonPath.getString("body.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(responseJsonPath.getString("body.resultCodeId")).isEqualTo("001");
        Assertions.assertThat(responseJsonPath.getString("body.resultMsg")).isEqualTo("success");
        Assertions.assertThat(responseJsonPath.getString("body.mid")).isEqualTo(mid);
        Assertions.assertThat(responseJsonPath.getString("body.txnAmount")).isEqualTo(txnAmountupipsp);
        Assertions.assertThat(responseJsonPath.getString("body.externalSerialNo")).isEqualTo(externalSerialNo1);
        Assertions.assertThat(responseJsonPath.getString("body.payerPaymentInstrument")).isEqualTo(payerPaymentInstrument);
        Assertions.assertThat(responseJsonPath.getString("body.convenienceFeeValidation")).isEqualTo("MATCH");

        InstaproxyPtybliResponse successresponse = new InstaproxyPtybliResponse(txnAmountupipsp,externalSerialNo1 ,bankRrn1, callbackpaymentInstrument , "UPI" );
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

        String payableAmountItem1 = initTxnDTO.txnAmountFromBody();
                
                String refundItemsJson =
                "[{\"itemId\":\"Item001_"
                        + orderId
                        + "\",\"productId\":\"123047\",\"itemRefundAmount\":\""
                        + payableAmountItem1
                        + "\"}]";
        SyncRefund syncRefund = new SyncRefund();
        Response asyncRefundResp =
                given()
                        .spec(
                                syncRefund.reqSpecAsyncRefundWithRefundItemLines(
                                        Constants.MerchantType.MDR_FEE_ON_UPI_SUBTYPE,
                                        txnStatus.getResponse().getORDERID(),
                                        txnStatus.getResponse().getTXNID(),
                                        null,
                                        String.valueOf(payableAmountItem1),
                                        refundItemsJson))
                        .post()
                        .then()
                        .extract()
                        .response();
        JsonPath asyncJp = asyncRefundResp.jsonPath();
        Assertions.assertThat(asyncJp.getString("body.orderId")).isEqualTo(initTxnDTO.getBody().getOrderId());
        Assertions.assertThat(asyncJp.getString("body.mid")).isEqualTo(mid);
        Assertions.assertThat(asyncJp.getString("body.refId")).isNotNull();
        Assertions.assertThat(asyncJp.getString("body.resultInfo.resultStatus")).isEqualTo("PENDING");
        Assertions.assertThat(asyncJp.getString("body.resultInfo.resultCode")).isEqualTo("601");
        Assertions.assertThat(asyncJp.getString("body.resultInfo.resultMsg"))
                .isEqualTo("Refund request was raised for this transaction. But it is pending state");
        Assertions.assertThat(asyncJp.getString("body.refundAmount")).isEqualTo(payableAmountItem1);

    }

    @Owner(CHAKSHU)
    @Feature("PG-3933")
    @Test(description = "Validate async refund CC Failed then UPI Intent success in second pay leg")
    public void ValidateasyncRefundCCFailedThenUPIIntentSuccessInSecondpayleg() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.MDR_FEE_ON_UPI_SUBTYPE;
        String mid = Constants.MerchantType.MDR_FEE_ON_UPI_SUBTYPE.getId();
        String txnAmount = "99.99";
        String txnAmountupipsp = "99.99";
        String paymentFee = "0";
        String payeeVPA = "paytm-9759417321@ptybl";
        String payerPaymentInstrument = "UPI_SAVINGS";
        String callbackpaymentInstrument = "";
        String subAccountTypeValue = "";

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderID=initTxnDTO.getBody().getOrderId();
                Validate_FetchPayInstrument(txnToken, initTxnDTO, "CREDIT_CARD", "false");
  
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
                .validateRespCode("750")
                .validateStatus("TXN_FAILURE");

        
        ProcessTxnV1Request processTxnV1Request1 = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderID ,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("3D")
                .setRiskExtendInfo("userLBSLatitude:26.88|userLBSLongitude:81.01|userAgent:Mozilla/5.0 (Linux; Android 8.1.0; Redmi Note 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36|timeZone:Asia/Calcutta|operationType:PAYMENT|networkType:4g|businessFlow:JS_CHECKOUT|amount:2|language:en-US|screenResolution:393X786|deviceType:mobile|channelId:WAP|platform:mWeb|osType:Android|deviceModel:Redmi Note 5|browserType:Chrome|browserVersion:96.0.4664.104|osVersion:8.1.0|deviceManufacturer:Xiaomi")
                .build();
        ProcessTxnV1Response ptcResponse1 = NativeHelpers.executeProcessTxnV1(processTxnV1Request1);
        Assertions.assertThat(ptcResponse1.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        String deeplink = ptcResponse1.getBody().getDeepLinkInfo().getDeepLink();
        String externalSerialNo = deeplink.substring(deeplink.indexOf("tr")+3,deeplink.indexOf("&am"));
        String bankRrn = CommonHelpers.generateOrderId();
        System.out.println(externalSerialNo);
        Assertions.assertThat(ptcResponse1.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse1.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");


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

        TxnStatus txnStatus = new TxnStatus(mid, orderID);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderID)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PTYBLI.toString())
                .validateMid(mid)
                .AssertAll();

        String payableAmountItem1 = initTxnDTO.txnAmountFromBody();
                
                String refundItemsJson =
                "[{\"itemId\":\"Item001_"
                        + orderID
                        + "\",\"productId\":\"123047\",\"itemRefundAmount\":\""
                        + payableAmountItem1
                        + "\"}]";
        SyncRefund syncRefund = new SyncRefund();
        Response asyncRefundResp =
                given()
                        .spec(
                                syncRefund.reqSpecAsyncRefundWithRefundItemLines(
                                        Constants.MerchantType.MDR_FEE_ON_UPI_SUBTYPE,
                                        txnStatus.getResponse().getORDERID(),
                                        txnStatus.getResponse().getTXNID(),
                                        null,
                                        String.valueOf(payableAmountItem1),
                                        refundItemsJson))
                        .post()
                        .then()
                        .extract()
                        .response();
        JsonPath asyncJp = asyncRefundResp.jsonPath();
        Assertions.assertThat(asyncJp.getString("body.orderId")).isEqualTo(initTxnDTO.getBody().getOrderId());
        Assertions.assertThat(asyncJp.getString("body.mid")).isEqualTo(mid);
        Assertions.assertThat(asyncJp.getString("body.refId")).isNotNull();
        Assertions.assertThat(asyncJp.getString("body.resultInfo.resultStatus")).isEqualTo("PENDING");
        Assertions.assertThat(asyncJp.getString("body.resultInfo.resultCode")).isEqualTo("601");
        Assertions.assertThat(asyncJp.getString("body.resultInfo.resultMsg"))
                .isEqualTo("Refund request was raised for this transaction. But it is pending state");
        Assertions.assertThat(asyncJp.getString("body.refundAmount")).isEqualTo(payableAmountItem1);

    }



    @Owner(CHAKSHU)
    @Feature("PG-3933")
    @Test(description = "Validate async refund for Item based Txn")
    public void ValidateasyncrefundforItembasedTxn() throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String txnAmount = "1100";
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_ENABLE;
        ArrayList<String> promoCode = new ArrayList<>();
        promoCode.add("");
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "123047", "18084", 1100.00, "6226");
        items.add(item1);
        SimplifiedUnifiedOffers.PromoDetails promoDetails = new
                SimplifiedUnifiedOffers.PromoDetails(promoCode, "true", "false", "false", null);
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails = new
                SimplifiedUnifiedOffers.SubventionDetails("false", null, null, null);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new
                SimplifiedUnifiedOffers(subventionDetails, promoDetails);
        simplifiedUnifiedOffers.setItem(items);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), mid)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        String theme = PGPHelpers.openJs("checkoutjs", initTxnDTO, txnToken, mid, "SAVED_CARD");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(AlternateID_VISA_CARD);
        cashierPage.payByCC(cashierPage, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).isNull();
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).containsIgnoringCase("savings");

        String payableAmountItem1 = txnStatus.getResponse().getTXNAMOUNT();
                
                String refundItemsJson =
                "[{\"itemId\":\"Item001_"
                        + orderId
                        + "\",\"productId\":\"123047\",\"itemRefundAmount\":\""
                        + payableAmountItem1
                        + "\"}]";
        SyncRefund syncRefund = new SyncRefund();
        Response asyncRefundResp =
                given()
                        .spec(
                                syncRefund.reqSpecAsyncRefundWithRefundItemLines(
                                        Constants.MerchantType.EMI_REG_CUSTOM_ENABLE,
                                        txnStatus.getResponse().getORDERID(),
                                        txnStatus.getResponse().getTXNID(),
                                        null,
                                        String.valueOf(payableAmountItem1),
                                        refundItemsJson))
                        .post()
                        .then()
                        .extract()
                        .response();
        JsonPath asyncJp = asyncRefundResp.jsonPath();
        Assertions.assertThat(asyncJp.getString("body.orderId")).isEqualTo(initTxnDTO.getBody().getOrderId());
        Assertions.assertThat(asyncJp.getString("body.mid")).isEqualTo(mid.getId());
        Assertions.assertThat(asyncJp.getString("body.refId")).isNotNull();
        Assertions.assertThat(asyncJp.getString("body.resultInfo.resultStatus")).isEqualTo("PENDING");
        Assertions.assertThat(asyncJp.getString("body.resultInfo.resultCode")).isEqualTo("601");
        Assertions.assertThat(asyncJp.getString("body.resultInfo.resultMsg"))
                .isEqualTo("Refund request was raised for this transaction. But it is pending state");
        Assertions.assertThat(asyncJp.getString("body.refundAmount")).isEqualTo(payableAmountItem1);

    }

    @Owner(CHAKSHU)
    @Feature("PG-3933")
    @Test(description = "Validate async refund for multiple Item based Txn")
    public void ValidateasyncrefundforMultipleItembasedTxn(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String txnAmount = "2200";
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_ENABLE;
        ArrayList<String> promoCode = new ArrayList<>();
        promoCode.add("");
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "123047", "18084", 1100.00, "6226");
        SimplifiedUnifiedOffers.Items item2 = new SimplifiedUnifiedOffers.Items("Item002_" + orderId, "123047", "18084", 1100.00, "6226");
        items.add(item1);
        items.add(item2);
        SimplifiedUnifiedOffers.PromoDetails promoDetails = new
                SimplifiedUnifiedOffers.PromoDetails(promoCode, "true", "false", "false", null);
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails = new
                SimplifiedUnifiedOffers.SubventionDetails("false", null, null, null);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new
                SimplifiedUnifiedOffers(subventionDetails, promoDetails);
        simplifiedUnifiedOffers.setItem(items);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), mid)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        String theme = PGPHelpers.openJs(JsType, initTxnDTO, txnToken, mid, "SAVED_CARD");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(AlternateID_VISA_CARD);
        cashierPage.payByCC(cashierPage, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).isNull();
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).containsIgnoringCase("savings");

        String totalRefundAmount = txnStatus.getResponse().getTXNAMOUNT();
        BigDecimal total = new BigDecimal(totalRefundAmount);
        String itemRefundEach =
                total.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP).toPlainString();
        String refundItemsJson =
                "[{\"itemId\":\"Item001_"
                        + orderId
                        + "\",\"productId\":\"123047\",\"itemRefundAmount\":\""
                        + itemRefundEach
                        + "\"},{\"itemId\":\"Item002_"
                        + orderId
                        + "\",\"productId\":\"123047\",\"itemRefundAmount\":\""
                        + itemRefundEach
                        + "\"}]";
        SyncRefund syncRefund = new SyncRefund();
        Response asyncRefundResp =
                given()
                        .spec(
                                syncRefund.reqSpecAsyncRefundWithRefundItemLines(
                                        Constants.MerchantType.EMI_REG_CUSTOM_ENABLE,
                                        txnStatus.getResponse().getORDERID(),
                                        txnStatus.getResponse().getTXNID(),
                                        null,
                                        String.valueOf(totalRefundAmount),
                                        refundItemsJson))
                        .post()
                        .then()
                        .extract()
                        .response();
        JsonPath asyncJp = asyncRefundResp.jsonPath();
        Assertions.assertThat(asyncJp.getString("body.orderId")).isEqualTo(initTxnDTO.getBody().getOrderId());
        Assertions.assertThat(asyncJp.getString("body.mid")).isEqualTo(mid.getId());
        Assertions.assertThat(asyncJp.getString("body.refId")).isNotNull();
        Assertions.assertThat(asyncJp.getString("body.resultInfo.resultStatus")).isEqualTo("PENDING");
        Assertions.assertThat(asyncJp.getString("body.resultInfo.resultCode")).isEqualTo("601");
        Assertions.assertThat(asyncJp.getString("body.resultInfo.resultMsg"))
                .isEqualTo("Refund request was raised for this transaction. But it is pending state");
        Assertions.assertThat(asyncJp.getString("body.refundAmount")).isEqualTo(totalRefundAmount);

    }

}
