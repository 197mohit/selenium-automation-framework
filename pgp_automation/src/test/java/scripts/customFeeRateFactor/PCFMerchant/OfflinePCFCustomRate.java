package scripts.customFeeRateFactor.PCFMerchant;

import com.paytm.LocalConfig;
import com.paytm.api.*;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.api.nativeAPI.FetchPcfDetail;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.GetPaymentStatusRequest.GetPaymentStatusDTO;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.fetchPcfDetail.Body;
import com.paytm.dto.NativeDTO.fetchPcfDetail.FetchPcfRequestWithSSO;
import com.paytm.dto.NativeDTO.fetchPcfDetail.PayMethod;
import com.paytm.dto.NativeDTO.fetchPcfDetail.feeRateFactors;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.dto.processTransactionV1.TxnAmount;
import com.paytm.framework.utils.DatabaseUtil;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import scripts.Native.PcfHelpher;

import java.text.DecimalFormat;
import java.text.Format;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Feature("PGP-21710")
@Owner("Gagandeep")
public class OfflinePCFCustomRate extends PGPBaseTest {

    private final static Format format = new DecimalFormat("0.00");

    private static final String MasterCCPaymentDetails = "|5452260891234678|882|052026";
    private static final String InternationalCCPaymentDetails = "|4639170013374311|123|102023";
    private static final String PrepaidDCPaymentDetails = "|4766413897814514|882|052026";
    private static final String VISADCPaymentDetails = "|4444333322221111|882|052026";
    private static final String successMessage = "Txn Success";
    private static final String successStatus = "TXN_SUCCESS";
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

    /*
    public void enablePrepaidCardFF4jFlag() {
        String queryForExp = "UPDATE FF4J_FEATURES SET ENABLE='1' WHERE FEAT_UID ='prepaidCard'";
        DatabaseUtil.getInstance().executeUpdateQuery(LocalConfig.PGP_DB_CONNECTION_URL, queryForExp);
        RedisAPI.deleteKey("FF4J_FEATURE_prepaidCard");
    }

     */

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
                .validateBANKNAME("")
                .validateMID(merchantType.getId())
                .validatePaymentMode("HYBRID")
                .validateRefundId("")
                .validateChildPayMode(payMode, 0)
                .validateChildPayMode(payMode, 1)
                .validateChildTxnTXNAMOUNT(txnAmount, chargeAmount.toString())
                .validateChildTxnSTATUS(successStatus, 0)
                .validateChildTxnSTATUS(successStatus, 1)
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

    public String validateFPO(User user, Constants.MerchantType merchantType) {
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(merchantType.getId())
                .setToken(user.ssoToken())
                .build();

        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchantType.getId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionResponse = fetchPaymentOption.execute().jsonPath();
        String orderId = fetchPaymentOptionResponse.get("body.orderId").toString();
        return orderId;
    }

    @Parameters({"theme"})
    @Test(description = "Verify a successful DC txn on PCF merchant having VISA 2% commission and default 1 %")
    public void successfulFeeFactorDCTxnForPCF(@Optional("enhancedweb_revamp") String theme) throws Exception {

        PcfHelpher.assertDynamicChargeTarget(pcfRateFactorPg2Rtdd, "Y");

        PcfHelpher.assertConvFee(pcfRateFactorPg2Rtdd, "Y");

        String payMode = "DC";


        User user = userManager.getForWrite(Label.BASIC);

        String orderId = validateFPO(user, pcfRateFactorPg2Rtdd);

        TxnAmount txnAmount = new TxnAmount();
        txnAmount.setValue("20");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(pcfRateFactorPg2Rtdd, "SSO", user.ssoToken())
                .setOrderId(orderId)
                .setTxnAmount(txnAmount)
                .setPaymentMode("DEBIT_CARD")
                .setCardInfo(VISADCPaymentDetails)
                .setAuthMode("otp")
                .setExtendInfoOrderAlreadyCreated(false)
                .build();


        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);

        JsonPath fetchPCFResponse = fetchPCFDetailsWithSSO(pcfRateFactorPg2Rtdd.getId(), txnAmount.getValue(), "VISA", user, "DEBIT_CARD");
        Double chargeAmount = PcfHelpher.fetchTotalConvenienceChargesPCF(fetchPCFResponse, "DEBIT_CARD");


        validateSuccessResponsePCFTxn(orderId, txnAmount.getValue(), pcfRateFactorPg2Rtdd, payMode, chargeAmount);

        //MerchantStatus
        TxnStatus txnStatus = validateSuccessTxnStatusPCFTxn(orderId, txnAmount.getValue(), pcfRateFactorPg2Rtdd, payMode, chargeAmount);
        validateSuccessNativeTxnStatusPCFTxn(orderId, txnAmount.getValue(), pcfRateFactorPg2Rtdd, payMode, chargeAmount);
        validateSuccessPaymentStatusAPIPCFTxn(orderId, txnAmount.getValue(), pcfRateFactorPg2Rtdd, payMode, chargeAmount);
        validateSuccessTxnStatusListPCFTxn(orderId, txnAmount.getValue(), pcfRateFactorPg2Rtdd, payMode, chargeAmount);
        validateOrderStatusAPI(orderId, txnAmount.getValue(), pcfRateFactorPg2Rtdd, payMode, chargeAmount);

        //SearchTransaction PGPLUS BO

        Response response = PcfHelpher.searchTxnPgPlusBO(txnStatus);

        //FEE FACTOR For PAYMODE
        Assertions.assertThat(response.jsonPath().getString("result.cardScheme[0]")).isEqualTo("VISA");

        Assertions.assertThat(CommonHelpers.getFeeFactorAsList(response.jsonPath().getString("result.feeFactor[0]")))
                .as("FEE Factor is Not coming from PGPLUS BO")
                .containsExactlyInAnyOrder("DEBIT_CARD", "VISA", "HDFC", "QR");
    }


    @Parameters({"theme"})
    @Test(description = "Verify a successful International CC txn on PCF merchant having 10 Rs commission , VISA 2% commission and default 1 %")
    public void successfulFeeFactorInternationalCCTxnForPCF(@Optional("enhancedweb_revamp") String theme) throws Exception {

        PcfHelpher.assertDynamicChargeTarget(pcfRatePg2Rtdd, "Y");
        PcfHelpher.assertConvFee(pcfRatePg2Rtdd, "Y");
        String payMode = "CC";
        feeRateFactors feeRateFactors = new feeRateFactors();
        feeRateFactors.setInternationalCardPayment(true);
        User user = userManager.getForWrite(Label.BASIC);
        String orderId = validateFPO(user, pcfRatePg2Rtdd);

        TxnAmount txnAmount = new TxnAmount();
        txnAmount.setValue("20");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(pcfRatePg2Rtdd, "SSO", user.ssoToken())
                .setOrderId(orderId)
                .setTxnAmount(txnAmount)
                .setPaymentMode("CREDIT_CARD")
                .setCardInfo(InternationalCCPaymentDetails)
                .setAuthMode("otp")
                .setExtendInfoOrderAlreadyCreated(false)
                .build();

        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);

        JsonPath fetchPCFResponse = fetchPCFDetailsWithSSO(pcfRatePg2Rtdd.getId(), txnAmount.getValue(), "VISA", user, "CREDIT_CARD", feeRateFactors);
        Double chargeAmount = PcfHelpher.fetchTotalConvenienceChargesPCF(fetchPCFResponse, "CREDIT_CARD");

        validateSuccessResponsePCFTxn(orderId, txnAmount.getValue(), pcfRatePg2Rtdd, payMode, chargeAmount);

        //MerchantStatus
        TxnStatus txnStatus = validateSuccessTxnStatusPCFTxn(orderId, txnAmount.getValue(), pcfRatePg2Rtdd, payMode, chargeAmount);
        validateSuccessNativeTxnStatusPCFTxn(orderId, txnAmount.getValue(), pcfRatePg2Rtdd, payMode, chargeAmount);
        validateSuccessPaymentStatusAPIPCFTxn(orderId, txnAmount.getValue(), pcfRatePg2Rtdd, payMode, chargeAmount);
        validateSuccessTxnStatusListPCFTxn(orderId, txnAmount.getValue(), pcfRatePg2Rtdd, payMode, chargeAmount);
        validateOrderStatusAPI(orderId, txnAmount.getValue(), pcfRatePg2Rtdd, payMode, chargeAmount);

        //SearchTransaction PGPLUS BO

        Response response = PcfHelpher.searchTxnPgPlusBO(txnStatus);

        //FEE FACTOR For PAYMODE
        Assertions.assertThat(response.jsonPath().getString("result.cardScheme[0]")).isEqualTo("VISA");

        Assertions.assertThat(CommonHelpers.getFeeFactorAsList(response.jsonPath().getString("result.feeFactor[0]")))
                .as("FEE Factor is Not coming from PGPLUS BO")
                .containsExactlyInAnyOrder("CREDIT_CARD", "VISA", "IHDF","internationalCardPayment", "QR");
    }


    @Parameters({"theme"})
    @Test(description = "Verify a successful NB txn on PCF merchant having Slab based Commission 0 to 25 flat 10rs, 25 to 50 both 12 " +
            "and above MDR commission with 12 Rs flat")
    public void successfulFeeFactorNBTxnSlabBasedforAmountLessThan25ForPCF(@Optional("enhancedweb") String theme) throws Exception {


        PcfHelpher.assertDynamicChargeTarget(pcfRateFactorPg2Rtdd, "Y");

        PcfHelpher.assertConvFee(pcfRateFactorPg2Rtdd, "Y");

        String payMode = "NB";

        User user = userManager.getForWrite(Label.BASIC);

        String orderId = validateFPO(user, pcfRateFactorPg2Rtdd);

        TxnAmount txnAmount = new TxnAmount();
        txnAmount.setValue("2");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(pcfRateFactorPg2Rtdd, "SSO", user.ssoToken())
                .setOrderId(orderId)
                .setTxnAmount(txnAmount)
                .setPaymentMode("NET_BANKING")
                .setChannelCode("ICICI")
                .setAuthMode("otp")
                .setExtendInfoOrderAlreadyCreated(false)
                .build();

        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);

        JsonPath fetchPCFResponse = fetchPCFDetailsWithSSO(pcfRateFactorPg2Rtdd.getId(), txnAmount.getValue(), "ICICI", user, "NET_BANKING");
        Double chargeAmount = PcfHelpher.fetchTotalConvenienceChargesPCF(fetchPCFResponse, "NET_BANKING");

        validateSuccessResponsePCFTxn(orderId, txnAmount.getValue(), pcfRateFactorPg2Rtdd, payMode, chargeAmount);

        //MerchantStatus
        TxnStatus txnStatus = validateSuccessTxnStatusPCFTxn(orderId, txnAmount.getValue(), pcfRateFactorPg2Rtdd, payMode, chargeAmount);
        validateSuccessNativeTxnStatusPCFTxn(orderId, txnAmount.getValue(), pcfRateFactorPg2Rtdd, payMode, chargeAmount);
        validateSuccessPaymentStatusAPIPCFTxn(orderId, txnAmount.getValue(), pcfRateFactorPg2Rtdd, payMode, chargeAmount);
        validateSuccessTxnStatusListPCFTxn(orderId, txnAmount.getValue(), pcfRateFactorPg2Rtdd, payMode, chargeAmount);
        validateOrderStatusAPI(orderId, txnAmount.getValue(), pcfRateFactorPg2Rtdd, payMode, chargeAmount);


        //SearchTransaction PGPLUS BO

        Response response = PcfHelpher.searchTxnPgPlusBO(txnStatus);

        //FEE FACTOR For PAYMODE

        Assertions.assertThat(CommonHelpers.getFeeFactorAsList(response.jsonPath().getString("result.feeFactor[0]")))
                .as("FEE Factor is Not coming from PGPLUS BO")
                .containsExactlyInAnyOrder("NET_BANKING", "ICICI", "ICICI", "QR");
    }


    @Parameters({"theme"})
    @Test(description = "Verify a successful NB txn on PCF merchant having Slab based Commission  0 to 25 flat 10rs, 25 to 50 both 12 " +
            "and above MDR commission with 12 Rs flat")
    public void successfulFeeFactorNBTxnSlabBasedforAmountMoreThan100LessThan200ForPCF(@Optional("enhancedweb_revamp") String theme) throws Exception {


        PcfHelpher.assertDynamicChargeTarget(pcfRateFactorPg2Rtdd, "Y");

        PcfHelpher.assertConvFee(pcfRateFactorPg2Rtdd, "Y");

        String payMode = "NB";

        User user = userManager.getForWrite(Label.BASIC);

        String orderId = validateFPO(user, pcfRateFactorPg2Rtdd);

        TxnAmount txnAmount = new TxnAmount();
        txnAmount.setValue("30");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(pcfRateFactorPg2Rtdd, "SSO", user.ssoToken())
                .setOrderId(orderId)
                .setTxnAmount(txnAmount)
                .setPaymentMode("NET_BANKING")
                .setChannelCode("ICICI")
                .setAuthMode("otp")
                .setExtendInfoOrderAlreadyCreated(false)
                .build();

        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);

        JsonPath fetchPCFResponse = fetchPCFDetailsWithSSO(pcfRateFactorPg2Rtdd.getId(), txnAmount.getValue(), "ICICI", user, "NET_BANKING");
        Double chargeAmount = PcfHelpher.fetchTotalConvenienceChargesPCF(fetchPCFResponse, "NET_BANKING");

        validateSuccessResponsePCFTxn(orderId, "30", pcfRateFactorPg2Rtdd, payMode, chargeAmount);

        //MerchantStatus
        TxnStatus txnStatus = validateSuccessTxnStatusPCFTxn(orderId, txnAmount.getValue(), pcfRateFactorPg2Rtdd, payMode, chargeAmount);
        validateSuccessNativeTxnStatusPCFTxn(orderId, txnAmount.getValue(), pcfRateFactorPg2Rtdd, payMode, chargeAmount);
        validateSuccessPaymentStatusAPIPCFTxn(orderId, txnAmount.getValue(), pcfRateFactorPg2Rtdd, payMode, chargeAmount);
        validateSuccessTxnStatusListPCFTxn(orderId, txnAmount.getValue(), pcfRateFactorPg2Rtdd, payMode, chargeAmount);
        validateOrderStatusAPI(orderId, txnAmount.getValue(), pcfRateFactorPg2Rtdd, payMode, chargeAmount);


        //SearchTransaction PGPLUS BO

        Response response = PcfHelpher.searchTxnPgPlusBO(txnStatus);

        //FEE FACTOR For PAYMODE

        Assertions.assertThat(CommonHelpers.getFeeFactorAsList(response.jsonPath().getString("result.feeFactor[0]")))
                .as("FEE Factor is Not coming from PGPLUS BO")
                .containsExactlyInAnyOrder("NET_BANKING", "ICICI", "ICICI", "QR");
    }

    @Parameters({"theme"})
    @Test(description = "Verify a successful NB txn on PCF merchant having Slab based Commission  0 to 25 flat 10rs, 25 to 50 both 12 " +
            "and above MDR commission with 12 Rs flat")
    public void successfulFeeFactorNBTxnSlabBasedforAmountMore200ForPCFTOMDR(@Optional("enhancedweb") String theme) throws Exception {

        PcfHelpher.assertDynamicChargeTarget(pcfRateFactorPg2Rtdd, "Y");

        PcfHelpher.assertConvFee(pcfRateFactorPg2Rtdd, "Y");

        String payMode = "NB";

        User user = userManager.getForWrite(Label.BASIC);

        String orderId = validateFPO(user, pcfRateFactorPg2Rtdd);

        TxnAmount txnAmount = new TxnAmount();
        txnAmount.setValue("60");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(pcfRateFactorPg2Rtdd, "SSO", user.ssoToken())
                .setOrderId(orderId)
                .setTxnAmount(txnAmount)
                .setPaymentMode("NET_BANKING")
                .setChannelCode("ICICI")
                .setAuthMode("otp")
                .setExtendInfoOrderAlreadyCreated(false)
                .build();

        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);

        JsonPath fetchPCFResponse = fetchPCFDetailsWithSSO(pcfRateFactorPg2Rtdd.getId(), txnAmount.getValue(), "ICICI", user, "NET_BANKING");
        Double chargeAmount = PcfHelpher.fetchTotalConvenienceChargesPCF(fetchPCFResponse, "NET_BANKING");

        validateSuccessResponsePCFTxn(orderId, txnAmount.getValue(), pcfRateFactorPg2Rtdd, payMode, chargeAmount);

        //MerchantStatus
        TxnStatus txnStatus = validateSuccessTxnStatusPCFTxn(orderId, txnAmount.getValue(), pcfRateFactorPg2Rtdd, payMode, chargeAmount);
        validateSuccessNativeTxnStatusPCFTxn(orderId, txnAmount.getValue(), pcfRateFactorPg2Rtdd, payMode, chargeAmount);
        validateSuccessPaymentStatusAPIPCFTxn(orderId, txnAmount.getValue(), pcfRateFactorPg2Rtdd, payMode, chargeAmount);
        validateSuccessTxnStatusListMDRTxn(pcfRateFactorPg2Rtdd.getId(), orderId, txnAmount.getValue(), payMode);
        validateOrderStatusAPI(orderId, txnAmount.getValue(), pcfRateFactorPg2Rtdd, payMode, chargeAmount);


        //SearchTransaction PGPLUS BO

        Response response = PcfHelpher.searchTxnPgPlusBO(txnStatus);

        //FEE FACTOR For PAYMODE

        Assertions.assertThat(CommonHelpers.getFeeFactorAsList(response.jsonPath().getString("result.feeFactor[0]")))
                .as("FEE Factor is Not coming from PGPLUS BO")
                .containsExactlyInAnyOrder("NET_BANKING", "ICICI", "ICICI", "QR");
    }


    @Parameters({"theme"})
    @Test(description = "Verify a successful CC txn on PCF merchant having flat 6 rs MDR commission ")
    public void
    successfulFeeFactorCCMasterTxnForPCF_MDR(@Optional("enhancedweb") String theme) throws Exception {

        PcfHelpher.assertDynamicChargeTarget(pcfRateFactorPg2Rtdd, "Y");

        PcfHelpher.assertConvFee(pcfRateFactorPg2Rtdd, "Y");

        String payMode = "CC";

        User user = userManager.getForWrite(Label.BASIC);

        String orderId = validateFPO(user, pcfRateFactorPg2Rtdd);

        TxnAmount txnAmount = new TxnAmount();
        txnAmount.setValue("2");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(pcfRateFactorPg2Rtdd, "SSO", user.ssoToken())
                .setOrderId(orderId)
                .setTxnAmount(txnAmount)
                .setPaymentMode("CREDIT_CARD")
                .setCardInfo(MasterCCPaymentDetails)
                .setAuthMode("otp")
                .setExtendInfoOrderAlreadyCreated(false)
                .build();

        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);

        JsonPath fetchPCFResponse = fetchPCFDetailsWithSSO(pcfRateFactorPg2Rtdd.getId(), txnAmount.getValue(), "MASTER", user, "CREDIT_CARD");
        Double chargeAmount = PcfHelpher.fetchTotalConvenienceChargesPCF(fetchPCFResponse, "CREDIT_CARD");

        validateSuccessResponsePCFTxn(orderId, txnAmount.getValue(), pcfRateFactorPg2Rtdd, payMode, chargeAmount);

        //MerchantStatus
        TxnStatus txnStatus = validateSuccessTxnStatusPCFTxn(orderId, txnAmount.getValue(), pcfRateFactorPg2Rtdd, payMode, chargeAmount);
        validateSuccessNativeTxnStatusPCFTxn(orderId, txnAmount.getValue(), pcfRateFactorPg2Rtdd, payMode, chargeAmount);
        validateSuccessPaymentStatusAPIPCFTxn(orderId, txnAmount.getValue(), pcfRateFactorPg2Rtdd, payMode, chargeAmount);
        validateSuccessTxnStatusListMDRTxn(pcfRateFactorPg2Rtdd.getId(),orderId, txnAmount.getValue(), payMode);
        validateOrderStatusAPI(orderId, txnAmount.getValue(), pcfRateFactorPg2Rtdd, payMode, chargeAmount);

        //SearchTransaction PGPLUS BO

        Response response = PcfHelpher.searchTxnPgPlusBO(txnStatus);

        //FEE FACTOR For PAYMODE
        Assertions.assertThat(response.jsonPath().getString("result.cardScheme[0]")).isEqualTo("MASTER");

        Assertions.assertThat(CommonHelpers.getFeeFactorAsList(response.jsonPath().getString("result.feeFactor[0]")))
                .as("FEE Factor is Not coming from PGPLUS BO")
                .containsExactlyInAnyOrder("CREDIT_CARD", "MASTER", "HDFC", "QR");
    }

    @Parameters({"theme"})
    @Test(description = "Verify a successful DC Prepaid card txn on PCF merchant having flat 20 flat PCF commission")
    public void successfulFeeFactorDCPrepaidTxnForPCF_MDR(@Optional("enhancedweb_revamp") String theme) throws Exception {

        PcfHelpher.assertDynamicChargeTarget(pcfRateFactorPg2Rtdd, "Y");

        PcfHelpher.assertConvFee(pcfRateFactorPg2Rtdd, "Y");


        //enablePrepaidCardFF4jFlag();

        String payMode = "DC";

        feeRateFactors feeRateFactors = new feeRateFactors();
        feeRateFactors.setPrepaidCard(true);

        User user = userManager.getForWrite(Label.BASIC);
        String orderId = validateFPO(user, pcfRateFactorPg2Rtdd);
        TxnAmount txnAmount = new TxnAmount();
        txnAmount.setValue("20");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(pcfRateFactorPg2Rtdd, "SSO", user.ssoToken())
                .setOrderId(orderId)
                .setTxnAmount(txnAmount)
                .setPaymentMode("DEBIT_CARD")
                .setCardInfo(PrepaidDCPaymentDetails)
                .setAuthMode("otp")
                .setExtendInfoOrderAlreadyCreated(false)
                .build();

        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);

        JsonPath fetchPCFResponse = fetchPCFDetailsWithSSO(pcfRateFactorPg2Rtdd.getId(), txnAmount.getValue(), "VISA", user, "DEBIT_CARD", feeRateFactors);
        Double chargeAmount = PcfHelpher.fetchTotalConvenienceChargesPCF(fetchPCFResponse, "DEBIT_CARD");

        validateSuccessResponsePCFTxn(orderId, txnAmount.getValue(), pcfRateFactor, payMode, chargeAmount);

        //MerchantStatus
        TxnStatus txnStatus = validateSuccessTxnStatusPCFTxn(orderId, txnAmount.getValue(), pcfRateFactorPg2Rtdd, payMode, chargeAmount);
        validateSuccessNativeTxnStatusPCFTxn(orderId, txnAmount.getValue(), pcfRateFactorPg2Rtdd, payMode, chargeAmount);
        validateSuccessPaymentStatusAPIPCFTxn(orderId, txnAmount.getValue(), pcfRateFactorPg2Rtdd, payMode, chargeAmount);
        validateSuccessTxnStatusListPCFTxn(orderId, txnAmount.getValue(), pcfRateFactorPg2Rtdd, payMode, chargeAmount);
        validateOrderStatusAPI(orderId, txnAmount.getValue(), pcfRateFactorPg2Rtdd, payMode, chargeAmount);


        //SearchTransaction PGPLUS BO

        Response response = PcfHelpher.searchTxnPgPlusBO(txnStatus);

        //FEE FACTOR For PAYMODE
        Assertions.assertThat(response.jsonPath().getString("result.cardScheme[0]")).isEqualTo("VISA");

        Assertions.assertThat(CommonHelpers.getFeeFactorAsList(response.jsonPath().getString("result.feeFactor[0]")))
                .as("FEE Factor is Not coming from PGPLUS BO")
                .containsExactlyInAnyOrder("DEBIT_CARD", "VISA", "HDFC","prepaidCard", "QR");
    }
}