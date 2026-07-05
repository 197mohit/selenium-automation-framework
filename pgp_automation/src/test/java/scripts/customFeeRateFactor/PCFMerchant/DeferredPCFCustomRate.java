package scripts.customFeeRateFactor.PCFMerchant;

import com.paytm.LocalConfig;
import com.paytm.api.*;
import com.paytm.api.nativeAPI.FetchPcfDetail;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.apphelpers.QRHelper;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.GetPaymentStatusRequest.GetPaymentStatusDTO;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.response.FetchPaymentOptResponseDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.fetchPcfDetail.Body;
import com.paytm.dto.NativeDTO.fetchPcfDetail.FetchPcfRequestWithSSO;
import com.paytm.dto.NativeDTO.fetchPcfDetail.PayMethod;
import com.paytm.dto.NativeDTO.fetchPcfDetail.feeRateFactors;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.framework.utils.DatabaseUtil;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
import com.paytm.utils.merchant.util.PayMethodType;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import scripts.Native.PcfHelpher;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.Format;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Feature("PGP-21710")
@Owner("Gagandeep")
public class DeferredPCFCustomRate extends PGPBaseTest {

    private final static Format format = new DecimalFormat("0.00");
    private static final String MasterCCPaymentDetails = "|5452260891234678|882|052026";
    private static final String InternationalCCPaymentDetails = "|4639170013374311|882|052026";
    private static final String PrepaidDCPaymentDetails = "|4766413897814514|882|052026";
    private static final String VISADCPaymentDetails = "|4444333322221111|882|052026";
    private static final String successMessage = "Txn Success";
    private static final String successStatus = "TXN_SUCCESS";
    private final CheckoutPage checkoutPage = new CheckoutPage();
    Constants.MerchantType pcfRateFactor = Constants.MerchantType.CUSTOMFEEPCF;
    Constants.MerchantType pcfRate = Constants.MerchantType.PCF_MERCHANT;
    Constants.MerchantType pcfRateFactorPg2Rtdd = Constants.MerchantType.CUSTOMFEEPCF_PG2_RTDD;
    Constants.MerchantType pcfRatePg2Rtdd = Constants.MerchantType.PCF_MERCHANT_PG2_RTDD;


    public static JsonPath fetchPCFDetailsWithSSO(String mid, String txnAmount, String instId, User user, String payMode) {
        List<PayMethod> payMethods = new ArrayList<>();
        payMethods.add(new PayMethod().setInstId(instId).setPayMethod(payMode));

        FetchPcfRequestWithSSO fetchPcfRequest = new FetchPcfRequestWithSSO()
                .setHead(new com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.Head("WEB").setVersion("v1").setRequestTimestamp("Time").setTokenType("SSO").setToken(user.ssoToken()))
                .setBody(new Body().setPayMethods(payMethods).setMid(mid).setTxnAmount(txnAmount));
        FetchPcfDetail fetchPcfDetail = new FetchPcfDetail(mid, fetchPcfRequest);
        JsonPath jsonPath = fetchPcfDetail.execute().jsonPath();
        return jsonPath;
    }

    public static JsonPath fetchPCFDetailsWithSSO(String mid, String txnAmount, String instId, User user, String payMode, feeRateFactors feeRateFactors) {
        List<PayMethod> payMethods = new ArrayList<>();
        payMethods.add(new PayMethod().setInstId(instId).setPayMethod(payMode).setFeeRateFactors(feeRateFactors));

        FetchPcfRequestWithSSO fetchPcfRequest = new FetchPcfRequestWithSSO()
                .setHead(new com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.Head("WEB").setVersion("v1").setRequestTimestamp("Time").setTokenType("SSO").setToken(user.ssoToken()))
                .setBody(new Body()
                        .setPayMethods(payMethods)
                        .setMid(mid)
                        .setTxnAmount(txnAmount));
        FetchPcfDetail fetchPcfDetail = new FetchPcfDetail(mid, fetchPcfRequest);
        JsonPath jsonPath = fetchPcfDetail.execute().jsonPath();
        return jsonPath;
    }

    public static void validateSuccessTxnStatusListMDRTxn(String mid, String OrderId, String txnAmount, String payMode) {
        TxnStatusList txnStatusList = new TxnStatusList();
        txnStatusList.getTxnStatusList(mid, OrderId);
        txnStatusList
                .validateBankTxnIdNonEmpty()
                .validateOrderId(OrderId)
                .validateTxnAmount(txnAmount)
                .validateStatus(successStatus)
                .validateTXNTYPE("SALE")
                .validateResponseCode("01")
                .validateResponseMessage("Txn Successful.")
                .validateMID(mid)
                .validatePaymentMode(payMode)
                .validateRefundId("")
                .assertAll();


    }

    public static void validateSuccessResponsePCFTxn(String OrderId, String txnAmount, Constants.MerchantType merchantType, String payMode, Double chargeAmount) {
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(com.paytm.appconstants.Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(merchantType.getId())
                .validateOrderId(OrderId)
                .validatePaymentMode(payMode)
                .validateRespCode("01")
                .validateRespMsg(successMessage)
                .validateStatus(successStatus)
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(txnAmount))
                .validateTxnDate(new Date())
                .validateTxnId(com.paytm.appconstants.Constants.ValidationType.NON_EMPTY)
                .validateChargeAmount(format.format(chargeAmount))
                .assertAll();
    }

    public static TxnStatus validateSuccessTxnStatusPCFTxn(String OrderId, String txnAmount, Constants.MerchantType merchantType, String payMode, Double chargeAmount) {
        TxnStatus txnStatus = new TxnStatus(merchantType.getId(), OrderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(OrderId)
                .validateTxnAmount(txnAmount)
                .validateStatus(successStatus)
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg(successMessage)
                .validateMid(merchantType.getId())
                .validatePaymentMode(payMode)
                .validateRefundAmnt("0.00")
                .validateChargeAmount(format.format(chargeAmount))
                .validateTxnDate(new Date())
                .AssertAll();
        return txnStatus;

    }

    public static TxnStatus validateSuccessNativeTxnStatusPCFTxn(String OrderId, String txnAmount, Constants.MerchantType merchantType, String payMode, Double chargeAmount) {
        TxnStatus txnStatus = new TxnStatus();
        txnStatus.getNativeStatus(merchantType.getId(), OrderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(OrderId)
                .validateTxnAmount(txnAmount)
                .validateStatus(successStatus)
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg(successMessage)
                .validateMid(merchantType.getId())
                .validatePaymentMode(payMode)
                .validateRefundAmnt("0.00")
                .validateChargeAmount(format.format(chargeAmount))
                .validateTxnDate(new Date())
                .AssertAll();
        return txnStatus;

    }

    public static void validateSuccessTxnStatusListPCFTxn(String OrderId, String txnAmount, Constants.MerchantType merchantType, String payMode, Double chargeAmount) {
        TxnStatusList txnStatusList = new TxnStatusList();
        txnStatusList.getTxnStatusList(merchantType.getId(), OrderId);
        txnStatusList
                .validateBankTxnIdNonEmpty()
                .validateOrderId(OrderId)
                .validateTxnAmount(txnAmount)
                .validateStatus(successStatus)
                .validateTXNTYPE("SALE")
                .validateGatewayName("")
                .validateResponseCode("01")
                .validateResponseMessage("Txn Successful.")
                .validateMID(merchantType.getId())
                .assertAll();


    }

    public static JsonPath validateSuccessPaymentStatusAPIPCFTxn(String OrderId, String txnAmount, Constants.MerchantType merchantType, String payMode, Double chargeAmount) {
        GetPaymentStatusDTO getPaymentStatusDTO = new GetPaymentStatusDTO.Builder
                (OrderId, "", merchantType.getKey(), merchantType.getId())
                .build();
        GetPaymentStatus merchant = new GetPaymentStatus(getPaymentStatusDTO);
        JsonPath getPaymentStatus = merchant.execute().jsonPath();
        Assertions.assertThat(getPaymentStatus.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase(successStatus);
        Assertions.assertThat(getPaymentStatus.getString("body.resultInfo.resultMsg")).isEqualToIgnoringCase(successMessage);
        Assertions.assertThat(getPaymentStatus.getString("body.orderId")).isEqualToIgnoringCase(OrderId);
        Assertions.assertThat(getPaymentStatus.getString("body.mid")).isEqualToIgnoringCase(merchantType.getId());
        Assertions.assertThat(getPaymentStatus.getString("body.paymentMode")).isEqualToIgnoringCase(payMode);

        Assertions.assertThat(getPaymentStatus.getDouble("body.chargeAmount")).isEqualTo(chargeAmount);

        return getPaymentStatus;
    }

    public static void validateOrderStatusAPI(String OrderId, String txnAmount, Constants.MerchantType merchantType, String payMode, Double chargeAmount) {
        OrderStatus orderStatus = new OrderStatus();
        Response response = orderStatus.getOrderStatus(merchantType.getId(), OrderId);
        Assertions.assertThat(response.jsonPath().getString("body.txnId")).isNotEmpty().isNotNull();
        Assertions.assertThat(response.jsonPath().getString("body.orderId")).isEqualTo(OrderId);
        Assertions.assertThat(response.jsonPath().getString("body.txnStatus")).isEqualTo(successStatus);
        Assertions.assertThat(response.jsonPath().getString("body.txnResponseMsg")).isEqualTo(successMessage);
        Assertions.assertThat(response.jsonPath().getString("body.paymentMode")).isEqualTo(payMode);
        Assertions.assertThat(Double.parseDouble(response.jsonPath().getString("body.txnAmount"))).isEqualTo(Double.parseDouble(txnAmount));
        Assertions.assertThat(response.jsonPath().getString("body.mid")).isEqualTo(merchantType.getId());
        Assertions.assertThat(response.jsonPath().getDouble("body.chargeAmount")).isEqualTo(chargeAmount);
        Assertions.assertThat(response.jsonPath().getString("body.txnResponseCode")).isEqualTo("01");


    }

    /*
    public void enablePrepaidCardFF4jFlag() {
        String queryForExp = "UPDATE FF4J_FEATURES SET ENABLE='1' WHERE FEAT_UID ='prepaidCard'";
        DatabaseUtil.getInstance().executeUpdateQuery(LocalConfig.PGP_DB_CONNECTION_URL, queryForExp);
        RedisAPI.deleteKey("FF4J_FEATURE_prepaidCard");
    }

     */

    private static FetchPaymentOptResponseDTO fetchPaymentOptionResponse(User user, Boolean generateOrderId, String mid) throws IOException {
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(generateOrderId))
                .setMid(mid)
                .setToken(user.ssoToken())
                .build();
        return PGPHelpers.executeFetchPaymentOpt(mid, "", fetchPaymentOptionsDTO, generateOrderId);
    }

    private static FetchPaymentOptResponseDTO fetchPaymentOptionResponse(User user, Boolean generateOrderId, Double amount, String mid) throws IOException {
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(generateOrderId))
                .setMid(mid)
                .setToken(user.ssoToken())
                .setAmount(amount)
                .build();
        return PGPHelpers.executeFetchPaymentOpt(mid, "", fetchPaymentOptionsDTO, generateOrderId);
    }

    private FetchPaymentOptResponseDTO fetchPaymentOpt(User user, String paymentMode, String amount, String mid) throws IOException {
        FetchPaymentOptResponseDTO fetchPaymentOptResponse;
        if (amount.equals("")) {
            fetchPaymentOptResponse = fetchPaymentOptionResponse(user, true, mid);
        } else {
            fetchPaymentOptResponse = fetchPaymentOptionResponse(user, true, Double.valueOf(amount), mid);
        }
        System.out.println(fetchPaymentOptResponse.getBody().getOrderId());
        boolean status = QRHelper.validatePaymentModeEnabled(fetchPaymentOptResponse, paymentMode, false);
        Assertions.assertThat(status).as(paymentMode + " is not enabled on fetchPaymentOption API").isTrue();
        return fetchPaymentOptResponse;
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify a successful DC txn on PCF merchant having VISA 2% commission and default 1 %")
    public void successfulFeeFactorDCTxnForPCF(@Optional("false") boolean isNativePlus) throws Exception {

        PcfHelpher.assertDynamicChargeTarget(pcfRateFactorPg2Rtdd, "Y");

        PcfHelpher.assertConvFee(pcfRateFactorPg2Rtdd, "Y");

        String payMode = "DC";
        String txnAmount = "2";

        User user = userManager.getForWrite(Label.BASIC);

        FetchPaymentOptResponseDTO fetchPaymentOptResponse = fetchPaymentOpt(user, "DEBIT_CARD", txnAmount, pcfRateFactorPg2Rtdd.getId());
        String orderId = fetchPaymentOptResponse.getBody().getOrderId();


        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), pcfRateFactorPg2Rtdd)
                .setOrderId(orderId)
                .setTxnValue(txnAmount)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);


        OrderDTO orderDTO = new OrderFactory.Native(pcfRateFactorPg2Rtdd, orderId, txnToken, "DEBIT_CARD")
                .setCardInfo(VISADCPaymentDetails).build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);


        JsonPath fetchPCFResponse = fetchPCFDetailsWithSSO(pcfRateFactorPg2Rtdd.getId(), txnAmount, "VISA", user, "DEBIT_CARD");
        Double chargeAmount = PcfHelpher.fetchTotalConvenienceChargesPCF(fetchPCFResponse, "DEBIT_CARD");


        validateSuccessResponsePCFTxn(orderId, txnAmount, pcfRateFactorPg2Rtdd, payMode, chargeAmount);

        //MerchantStatus
        TxnStatus txnStatus = validateSuccessTxnStatusPCFTxn(orderId, txnAmount, pcfRateFactorPg2Rtdd, payMode, chargeAmount);
        validateSuccessNativeTxnStatusPCFTxn(orderId, txnAmount, pcfRateFactorPg2Rtdd, payMode, chargeAmount);
        validateSuccessPaymentStatusAPIPCFTxn(orderId, txnAmount, pcfRateFactorPg2Rtdd, payMode, chargeAmount);
        validateSuccessTxnStatusListPCFTxn(orderId, txnAmount, pcfRateFactorPg2Rtdd, payMode, chargeAmount);
        validateOrderStatusAPI(orderId, txnAmount, pcfRateFactorPg2Rtdd, payMode, chargeAmount);

        //SearchTransaction PGPLUS BO

        Response response = PcfHelpher.searchTxnPgPlusBO(txnStatus);

        //FEE FACTOR For PAYMODE
        Assertions.assertThat(response.jsonPath().getString("result.cardScheme[0]")).isEqualTo("VISA");

        Assertions.assertThat(CommonHelpers.getFeeFactorAsList(response.jsonPath().getString("result.feeFactor[0]")))
                .as("FEE Factor is Not coming from PGPLUS BO")
                .containsExactlyInAnyOrder("DEBIT_CARD", "VISA", "HDFC", "API");
    }


    @Parameters({"isNativePlus"})
    @Test(description = "Verify a successful International CC txn on PCF merchant having 10 Rs commission , VISA 2% commission and default 1 %")
    public void successfulFeeFactorInternationalCCTxnForPCF(@Optional("false") boolean isNativePlus) throws Exception {

        PcfHelpher.assertDynamicChargeTarget(pcfRatePg2Rtdd, "Y");
        PcfHelpher.assertConvFee(pcfRatePg2Rtdd, "Y");
        String payMode = "CC";
        String txnAmount = "2";
        feeRateFactors feeRateFactors = new feeRateFactors();
        feeRateFactors.setInternationalCardPayment(true);
        User user = userManager.getForWrite(Label.BASIC);


        FetchPaymentOptResponseDTO fetchPaymentOptResponse = fetchPaymentOpt(user, "CREDIT_CARD", txnAmount, pcfRatePg2Rtdd.getId());
        String orderId = fetchPaymentOptResponse.getBody().getOrderId();


        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), pcfRatePg2Rtdd)
                .setOrderId(orderId)
                .setTxnValue(txnAmount)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        OrderDTO orderDTO = new OrderFactory.Native(pcfRatePg2Rtdd, orderId, txnToken, "CREDIT_CARD")
                .setCardInfo(InternationalCCPaymentDetails).build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);

        JsonPath fetchPCFResponse = fetchPCFDetailsWithSSO(pcfRatePg2Rtdd.getId(), txnAmount, "VISA", user, "CREDIT_CARD", feeRateFactors);
        Double chargeAmount = PcfHelpher.fetchTotalConvenienceChargesPCF(fetchPCFResponse, "CREDIT_CARD");

        validateSuccessResponsePCFTxn(orderId, txnAmount, pcfRatePg2Rtdd, payMode, chargeAmount);

        //MerchantStatus
        TxnStatus txnStatus = validateSuccessTxnStatusPCFTxn(orderId, txnAmount, pcfRatePg2Rtdd, payMode, chargeAmount);
        validateSuccessNativeTxnStatusPCFTxn(orderId, txnAmount, pcfRatePg2Rtdd, payMode, chargeAmount);
        validateSuccessPaymentStatusAPIPCFTxn(orderId, txnAmount, pcfRatePg2Rtdd, payMode, chargeAmount);
        validateSuccessTxnStatusListPCFTxn(orderId, txnAmount, pcfRatePg2Rtdd, payMode, chargeAmount);
        validateOrderStatusAPI(orderId, txnAmount, pcfRatePg2Rtdd, payMode, chargeAmount);

        //SearchTransaction PGPLUS BO

        Response response = PcfHelpher.searchTxnPgPlusBO(txnStatus);

        //FEE FACTOR For PAYMODE
        Assertions.assertThat(response.jsonPath().getString("result.cardScheme[0]")).isEqualTo("VISA");

        Assertions.assertThat(CommonHelpers.getFeeFactorAsList(response.jsonPath().getString("result.feeFactor[0]")))
                .as("FEE Factor is Not coming from PGPLUS BO")
                .containsExactlyInAnyOrder("CREDIT_CARD", "VISA", "IHDF","internationalCardPayment", "API");
    }


    @Parameters({"theme"})
    @Test(description = "Verify a successful NB txn on PCF merchant having Slab based Commission 0 to 25 flat 10rs, 25 to 50 both 12 " +
            "and above MDR commission with 12 Rs flat")
    public void successfulFeeFactorNBTxnSlabBasedforAmountLessThan100ForPCF(@Optional("false") boolean isNativePlus) throws Exception {


        PcfHelpher.assertDynamicChargeTarget(pcfRateFactorPg2Rtdd, "Y");

        PcfHelpher.assertConvFee(pcfRateFactorPg2Rtdd, "Y");

        String payMode = "NB";

        String txnAmount = "2";

        User user = userManager.getForWrite(Label.BASIC);


        FetchPaymentOptResponseDTO fetchPaymentOptResponse = fetchPaymentOpt(user, "NET_BANKING", txnAmount, pcfRateFactorPg2Rtdd.getId());
        String orderId = fetchPaymentOptResponse.getBody().getOrderId();

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), pcfRateFactorPg2Rtdd)
                .setOrderId(orderId)
                .setTxnValue(txnAmount)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        OrderDTO orderDTO = new OrderFactory.Native(pcfRateFactorPg2Rtdd, orderId, txnToken, PayMethodType.NET_BANKING)
                .setChannelCode("ICICI").build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);


        JsonPath fetchPCFResponse = fetchPCFDetailsWithSSO(pcfRateFactorPg2Rtdd.getId(), txnAmount, "ICICI", user, "NET_BANKING");
        Double chargeAmount = PcfHelpher.fetchTotalConvenienceChargesPCF(fetchPCFResponse, "NET_BANKING");

        validateSuccessResponsePCFTxn(orderId, txnAmount, pcfRateFactorPg2Rtdd, payMode, chargeAmount);

        //MerchantStatus
        TxnStatus txnStatus = validateSuccessTxnStatusPCFTxn(orderId, txnAmount, pcfRateFactorPg2Rtdd, payMode, chargeAmount);
        validateSuccessNativeTxnStatusPCFTxn(orderId, txnAmount, pcfRateFactorPg2Rtdd, payMode, chargeAmount);
        validateSuccessPaymentStatusAPIPCFTxn(orderId, txnAmount, pcfRateFactorPg2Rtdd, payMode, chargeAmount);
        validateSuccessTxnStatusListPCFTxn(orderId, txnAmount, pcfRateFactorPg2Rtdd, payMode, chargeAmount);
        validateOrderStatusAPI(orderId, txnAmount, pcfRateFactorPg2Rtdd, payMode, chargeAmount);


        //SearchTransaction PGPLUS BO

        Response response = PcfHelpher.searchTxnPgPlusBO(txnStatus);

        //FEE FACTOR For PAYMODE

        Assertions.assertThat(CommonHelpers.getFeeFactorAsList(response.jsonPath().getString("result.feeFactor[0]")))
                .as("FEE Factor is Not coming from PGPLUS BO")
                .containsExactlyInAnyOrder("NET_BANKING", "ICICI", "ICICI", "API");
    }


    @Parameters({"isNativePlus"})
    @Test(description = "Verify a successful NB txn on PCF merchant having Slab based Commission 0 to 25 flat 10rs, 25 to 50 both 12 " +
            "and above MDR commission with 12 Rs flat")
    public void successfulFeeFactorNBTxnSlabBasedforAmountMoreThan25LessThan50ForPCF(@Optional("false") boolean isNativePlus) throws Exception {


        PcfHelpher.assertDynamicChargeTarget(pcfRateFactorPg2Rtdd, "Y");

        PcfHelpher.assertConvFee(pcfRateFactorPg2Rtdd, "Y");

        String payMode = "NB";

        String txnAmount = "30";

        User user = userManager.getForWrite(Label.BASIC);


        FetchPaymentOptResponseDTO fetchPaymentOptResponse = fetchPaymentOpt(user, "NET_BANKING", txnAmount, pcfRateFactorPg2Rtdd.getId());
        String orderId = fetchPaymentOptResponse.getBody().getOrderId();

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), pcfRateFactorPg2Rtdd)
                .setOrderId(orderId)
                .setTxnValue(txnAmount)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        OrderDTO orderDTO = new OrderFactory.Native(pcfRateFactorPg2Rtdd, orderId, txnToken, PayMethodType.NET_BANKING)
                .setChannelCode("ICICI").build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);


        JsonPath fetchPCFResponse = fetchPCFDetailsWithSSO(pcfRateFactorPg2Rtdd.getId(), txnAmount, "ICICI", user, "NET_BANKING");
        Double chargeAmount = PcfHelpher.fetchTotalConvenienceChargesPCF(fetchPCFResponse, "NET_BANKING");

        validateSuccessResponsePCFTxn(orderId, txnAmount, pcfRateFactorPg2Rtdd, payMode, chargeAmount);

        //MerchantStatus
        TxnStatus txnStatus = validateSuccessTxnStatusPCFTxn(orderId, txnAmount, pcfRateFactorPg2Rtdd, payMode, chargeAmount);
        validateSuccessNativeTxnStatusPCFTxn(orderId, txnAmount, pcfRateFactorPg2Rtdd, payMode, chargeAmount);
        validateSuccessPaymentStatusAPIPCFTxn(orderId, txnAmount, pcfRateFactorPg2Rtdd, payMode, chargeAmount);
        validateSuccessTxnStatusListPCFTxn(orderId, txnAmount, pcfRateFactorPg2Rtdd, payMode, chargeAmount);
        validateOrderStatusAPI(orderId, txnAmount, pcfRateFactorPg2Rtdd, payMode, chargeAmount);

        //SearchTransaction PGPLUS BO

        Response response = PcfHelpher.searchTxnPgPlusBO(txnStatus);

        //FEE FACTOR For PAYMODE

        Assertions.assertThat(CommonHelpers.getFeeFactorAsList(response.jsonPath().getString("result.feeFactor[0]")))
                .as("FEE Factor is Not coming from PGPLUS BO")
                .containsExactlyInAnyOrder("NET_BANKING", "ICICI", "ICICI", "API");
    }


    @Parameters({"isNativePlus"})
    @Test(description = "Verify a successful NB txn on PCF merchant having Slab based Commission 0 to 25 flat 10rs, 25 to 50 both 12 " +
            "and above MDR commission with 12 Rs flat")
    public void successfulFeeFactorNBTxnSlabBasedforAmountMore50ForPCFTOMDR(@Optional("false") boolean isNativePlus) throws Exception {

        PcfHelpher.assertDynamicChargeTarget(pcfRateFactorPg2Rtdd, "Y");

        PcfHelpher.assertConvFee(pcfRateFactorPg2Rtdd, "Y");

        String payMode = "NB";

        String txnAmount = "60";

        User user = userManager.getForWrite(Label.BASIC);


        FetchPaymentOptResponseDTO fetchPaymentOptResponse = fetchPaymentOpt(user, "NET_BANKING", txnAmount, pcfRateFactorPg2Rtdd.getId());
        String orderId = fetchPaymentOptResponse.getBody().getOrderId();

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), pcfRateFactorPg2Rtdd)
                .setOrderId(orderId)
                .setTxnValue(txnAmount)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        OrderDTO orderDTO = new OrderFactory.Native(pcfRateFactorPg2Rtdd, orderId, txnToken, PayMethodType.NET_BANKING)
                .setChannelCode("ICICI").build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);


        JsonPath fetchPCFResponse = fetchPCFDetailsWithSSO(pcfRateFactorPg2Rtdd.getId(), txnAmount, "ICICI", user, "NET_BANKING");
        Double chargeAmount = PcfHelpher.fetchTotalConvenienceChargesPCF(fetchPCFResponse, "NET_BANKING");

        validateSuccessResponsePCFTxn(orderId, txnAmount, pcfRateFactorPg2Rtdd, payMode, chargeAmount);

        //MerchantStatus
        TxnStatus txnStatus = validateSuccessTxnStatusPCFTxn(orderId, txnAmount, pcfRateFactorPg2Rtdd, payMode, chargeAmount);
        validateSuccessNativeTxnStatusPCFTxn(orderId, txnAmount, pcfRateFactorPg2Rtdd, payMode, chargeAmount);
        validateSuccessPaymentStatusAPIPCFTxn(orderId, txnAmount, pcfRateFactorPg2Rtdd, payMode, chargeAmount);
        validateSuccessTxnStatusListMDRTxn(pcfRateFactorPg2Rtdd.getId(),orderId, txnAmount, payMode);
        validateOrderStatusAPI(orderId, txnAmount, pcfRateFactorPg2Rtdd, payMode, chargeAmount);

        //SearchTransaction PGPLUS BO

        Response response = PcfHelpher.searchTxnPgPlusBO(txnStatus);

        //FEE FACTOR For PAYMODE

        Assertions.assertThat(CommonHelpers.getFeeFactorAsList(response.jsonPath().getString("result.feeFactor[0]")))
                .as("FEE Factor is Not coming from PGPLUS BO")
                .containsExactlyInAnyOrder("NET_BANKING", "ICICI", "ICICI", "API");
    }


    @Parameters({"isNativePlus"})
    @Test(description = "Verify a successful CC txn on PCF merchant having flat 6 rs MDR commission ")
    public void successfulFeeFactorCCMasterTxnForPCF_MDR(@Optional("false") boolean isNativePlus) throws Exception {

        PcfHelpher.assertDynamicChargeTarget(pcfRateFactorPg2Rtdd, "Y");

        PcfHelpher.assertConvFee(pcfRateFactorPg2Rtdd, "Y");

        String payMode = "CC";
        String txnAmount = "2";

        User user = userManager.getForWrite(Label.BASIC);

        FetchPaymentOptResponseDTO fetchPaymentOptResponse = fetchPaymentOpt(user, "DEBIT_CARD", txnAmount, pcfRateFactorPg2Rtdd.getId());
        String orderId = fetchPaymentOptResponse.getBody().getOrderId();

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), pcfRateFactorPg2Rtdd)
                .setOrderId(orderId)
                .setTxnValue(txnAmount)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);


        OrderDTO orderDTO = new OrderFactory.Native(pcfRateFactorPg2Rtdd, orderId, txnToken, "DEBIT_CARD")
                .setCardInfo(MasterCCPaymentDetails).build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);

        JsonPath fetchPCFResponse = fetchPCFDetailsWithSSO(pcfRateFactorPg2Rtdd.getId(), txnAmount, "MASTER", user, "CREDIT_CARD");
        Double chargeAmount = PcfHelpher.fetchTotalConvenienceChargesPCF(fetchPCFResponse, "CREDIT_CARD");

        validateSuccessResponsePCFTxn(orderId, txnAmount, pcfRateFactorPg2Rtdd, payMode, chargeAmount);

        //MerchantStatus
        TxnStatus txnStatus = validateSuccessTxnStatusPCFTxn(orderId, txnAmount, pcfRateFactorPg2Rtdd, payMode, chargeAmount);
        validateSuccessNativeTxnStatusPCFTxn(orderId, txnAmount, pcfRateFactorPg2Rtdd, payMode, chargeAmount);
        validateSuccessPaymentStatusAPIPCFTxn(orderId, txnAmount, pcfRateFactorPg2Rtdd, payMode, chargeAmount);
        validateSuccessTxnStatusListMDRTxn(pcfRateFactorPg2Rtdd.getId(),orderId, txnAmount, payMode);
        validateOrderStatusAPI(orderId, txnAmount, pcfRateFactorPg2Rtdd, payMode, chargeAmount);

        //SearchTransaction PGPLUS BO

        Response response = PcfHelpher.searchTxnPgPlusBO(txnStatus);

        //FEE FACTOR For PAYMODE
        Assertions.assertThat(response.jsonPath().getString("result.cardScheme[0]")).isEqualTo("MASTER");

        Assertions.assertThat(CommonHelpers.getFeeFactorAsList(response.jsonPath().getString("result.feeFactor[0]")))
                .as("FEE Factor is Not coming from PGPLUS BO")
                .containsExactlyInAnyOrder("CREDIT_CARD", "MASTER", "HDFC", "API");

    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify a successful DC Prepaid card txn on PCF merchant having flat 20 flat PCF commission")
    public void successfulFeeFactorDCPrepaidTxnForPCF_MDR(@Optional("false") boolean isNativePlus) throws Exception {

        PcfHelpher.assertDynamicChargeTarget(pcfRateFactorPg2Rtdd, "Y");

        PcfHelpher.assertConvFee(pcfRateFactorPg2Rtdd, "Y");

        //enablePrepaidCardFF4jFlag();

        String payMode = "DC";
        String txnAmount = "2";

        feeRateFactors feeRateFactors = new feeRateFactors();
        feeRateFactors.setPrepaidCard(true);

        User user = userManager.getForWrite(Label.BASIC);

        FetchPaymentOptResponseDTO fetchPaymentOptResponse = fetchPaymentOpt(user, "DEBIT_CARD", txnAmount, pcfRateFactorPg2Rtdd.getId());
        String orderId = fetchPaymentOptResponse.getBody().getOrderId();

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), pcfRateFactorPg2Rtdd)
                .setOrderId(orderId)
                .setTxnValue(txnAmount)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);


        OrderDTO orderDTO = new OrderFactory.Native(pcfRateFactorPg2Rtdd, orderId, txnToken, "DEBIT_CARD")
                .setCardInfo(PrepaidDCPaymentDetails).build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);

        JsonPath fetchPCFResponse = fetchPCFDetailsWithSSO(pcfRateFactorPg2Rtdd.getId(), txnAmount, "VISA", user, "DEBIT_CARD",feeRateFactors);
        Double chargeAmount = PcfHelpher.fetchTotalConvenienceChargesPCF(fetchPCFResponse, "DEBIT_CARD");

        validateSuccessResponsePCFTxn(orderId, txnAmount, pcfRateFactorPg2Rtdd, payMode, chargeAmount);

        //MerchantStatus
        TxnStatus txnStatus = validateSuccessTxnStatusPCFTxn(orderId, txnAmount, pcfRateFactorPg2Rtdd, payMode, chargeAmount);
        validateSuccessNativeTxnStatusPCFTxn(orderId, txnAmount, pcfRateFactorPg2Rtdd, payMode, chargeAmount);
        validateSuccessPaymentStatusAPIPCFTxn(orderId, txnAmount, pcfRateFactorPg2Rtdd, payMode, chargeAmount);
        validateSuccessTxnStatusListPCFTxn(orderId, txnAmount, pcfRateFactorPg2Rtdd, payMode, chargeAmount);
        validateOrderStatusAPI(orderId, txnAmount, pcfRateFactorPg2Rtdd, payMode, chargeAmount);

        //SearchTransaction PGPLUS BO

        Response response = PcfHelpher.searchTxnPgPlusBO(txnStatus);

        //FEE FACTOR For PAYMODE
        Assertions.assertThat(response.jsonPath().getString("result.cardScheme[0]")).isEqualTo("VISA");

        Assertions.assertThat(CommonHelpers.getFeeFactorAsList(response.jsonPath().getString("result.feeFactor[0]")))
                .as("FEE Factor is Not coming from PGPLUS BO")
                .containsExactlyInAnyOrder("DEBIT_CARD", "VISA", "HDFC","prepaidCard", "API");
    }


}
