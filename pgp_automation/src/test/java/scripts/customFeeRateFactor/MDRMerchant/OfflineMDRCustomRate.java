package scripts.customFeeRateFactor.MDRMerchant;

import com.paytm.LocalConfig;
import com.paytm.api.*;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.GetPaymentStatusRequest.GetPaymentStatusDTO;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.dto.processTransactionV1.TxnAmount;
import com.paytm.framework.utils.DatabaseUtil;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;
import scripts.Native.PcfHelpher;

import java.util.Date;

@Feature("PGP-21710")
@Owner("Gagandeep")
public class OfflineMDRCustomRate extends PGPBaseTest {


    private static final String MasterDCPaymentDetails = "|"+ PaymentDTO.DC +"|882|052026";
    private static final String InternationalCCPaymentDetails = "|4639170013374311|123|102023";
    private static final String PrepaidDCPaymentDetails = "|4766413897814514|882|052026";
    private static final String successMessage = "Txn Success";
    private static final String successStatus = "TXN_SUCCESS";
    Constants.MerchantType mdrCustomRate = Constants.MerchantType.MDR_CUSTOM_RATE;
    Constants.MerchantType mdrCustom = Constants.MerchantType.MDR_INTL_MERC;

    public static TxnStatus validateSuccessTxnStatusMDRTxn(String mid, String txnAmount, String OrderId, String payMode) {
        TxnStatus txnStatus = new TxnStatus(mid, OrderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(OrderId)
                .validateTxnAmount(txnAmount)
                .validateStatus(successStatus)
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg(successMessage)
                .validateMid(mid)
                .validatePaymentMode(payMode)
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        return txnStatus;

    }

    /*
    public void enablePrepaidCardFF4jFlag() {
        String queryForExp = "UPDATE FF4J_FEATURES SET ENABLE='1' WHERE FEAT_UID ='prepaidCard'";
        DatabaseUtil.getInstance().executeUpdateQuery(LocalConfig.PGP_DB_CONNECTION_URL, queryForExp);
        RedisAPI.deleteKey("FF4J_FEATURE_prepaidCard");
    }
    */

    public static TxnStatus validateSuccessNativeTxnStatusMDRTxn(String mid, String txnAmount, String OrderId, String payMode) {
        TxnStatus txnStatus = new TxnStatus();
        txnStatus.getNativeStatus(mid, OrderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(OrderId)
                .validateTxnAmount(txnAmount)
                .validateStatus(successStatus)
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg(successMessage)
                .validateMid(mid)
                .validatePaymentMode(payMode)
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        return txnStatus;

    }

    public static void validateSuccessTxnStatusListMDRTxn(String mid, String txnAmount, String OrderId, String payMode) {

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


    public static JsonPath validateSuccessPaymentStatusAPIMDRTxn(String OrderId, Constants.MerchantType merchantType, String payMode) {
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

        return getPaymentStatus;
    }

    public static void validateOrderStatusAPI(String mid, String txnAmount, String OrderId, String payMode) {
        OrderStatus orderStatus = new OrderStatus();
        Response response = orderStatus.getOrderStatus(mid, OrderId);
        Assertions.assertThat(response.jsonPath().getString("body.txnId")).isNotEmpty().isNotNull();
        Assertions.assertThat(response.jsonPath().getString("body.orderId")).isEqualTo(OrderId);
        Assertions.assertThat(response.jsonPath().getString("body.txnStatus")).isEqualTo(successStatus);
        Assertions.assertThat(response.jsonPath().getString("body.txnResponseMsg")).isEqualTo(successMessage);
        Assertions.assertThat(response.jsonPath().getString("body.paymentMode")).isEqualTo(payMode);
        Assertions.assertThat(Double.parseDouble(response.jsonPath().getString("body.txnAmount"))).isEqualTo(Double.parseDouble(txnAmount));
        Assertions.assertThat(response.jsonPath().getString("body.mid")).isEqualTo(mid);
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


    @Test(description = "Verify a successful DC txn on MDR merchant having MASTER 10rs flat commission and default 1 %")
    public void successfulFeeFactorDCTxnForMDROffline() throws Exception {

        PcfHelpher.assertConvFee(mdrCustomRate, "N");

        String payMode = "DC";

        User user = userManager.getForWrite(Label.BASIC);

        String orderId = validateFPO(user, mdrCustomRate);


        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mdrCustomRate, "SSO", user.ssoToken())
                .setOrderId(orderId)
                .setTxnAmount(new TxnAmount().setValue("20"))
                .setPaymentMode("DEBIT_CARD")
                .setCardInfo(MasterDCPaymentDetails)
                .setAuthMode("otp")
                .setExtendInfoOrderAlreadyCreated(false)
                .build();


        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);


        //MerchantStatus
        TxnStatus txnStatus = validateSuccessTxnStatusMDRTxn(mdrCustomRate.getId(), "20", orderId, payMode);
        validateSuccessNativeTxnStatusMDRTxn(mdrCustomRate.getId(), "20", orderId, payMode);
        validateSuccessPaymentStatusAPIMDRTxn(orderId, mdrCustomRate, payMode);
        validateSuccessTxnStatusListMDRTxn(mdrCustomRate.getId(), "20", orderId, payMode);
        validateOrderStatusAPI(mdrCustomRate.getId(), "20", orderId, payMode);

        //SearchTransaction PGPLUS BO

        Response response = PcfHelpher.searchTxnPgPlusBO(txnStatus);

        //FEE FACTOR For PAYMODE
        Assertions.assertThat(response.jsonPath().getString("result.cardScheme[0]")).isEqualTo("MASTER");

        Assertions.assertThat(CommonHelpers.getFeeFactorAsList(response.jsonPath().getString("result.feeFactor[0]")))
                .as("FEE Factor is Not coming from PGPLUS BO")
                .containsExactlyInAnyOrder("DEBIT_CARD", "MASTER", "HDFC", "QR");

        double flatCommission = 10.0;


        Assertions.assertThat(response.jsonPath().getString("result.acquiringServiceFee[0]")).isEqualTo(String.valueOf(flatCommission))
                .as("FEE Factor is Not coming from PGPLUS BO");
    }


    @Test(description = "Verify a successful CC International txn on MDR merchant having slab bases >25 10 flat commission and default 1 %")
    public void successfulFeeFactorCCInternationalLessThan25TxnForMDR() throws Exception {

        PcfHelpher.assertConvFee(mdrCustom, "N");

        String payMode = "CC";
        User user = userManager.getForWrite(Label.BASIC);
        String orderId = validateFPO(user, mdrCustom);

        TxnAmount txnAmount = new TxnAmount();
        txnAmount.setValue("20");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mdrCustom, "SSO", user.ssoToken())
                .setOrderId(orderId)
                .setTxnAmount(txnAmount)
                .setPaymentMode("CREDIT_CARD")
                .setCardInfo(InternationalCCPaymentDetails)
                .setAuthMode("otp")
                .setExtendInfoOrderAlreadyCreated(false)
                .build();


        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);


        //MerchantStatus
        TxnStatus txnStatus = validateSuccessTxnStatusMDRTxn(mdrCustom.getId(), txnAmount.getValue(), orderId, payMode);
        validateSuccessPaymentStatusAPIMDRTxn(orderId, mdrCustom, payMode);
        validateSuccessTxnStatusListMDRTxn(mdrCustom.getId(), txnAmount.getValue(), orderId, payMode);
        validateOrderStatusAPI(mdrCustom.getId(), txnAmount.getValue(), orderId, payMode);


        //SearchTransaction PGPLUS BO

        Response response = PcfHelpher.searchTxnPgPlusBO(txnStatus);

        //FEE FACTOR For PAYMODE
        Assertions.assertThat(response.jsonPath().getString("result.cardScheme[0]")).isEqualTo("VISA");

        Assertions.assertThat(CommonHelpers.getFeeFactorAsList(response.jsonPath().getString("result.feeFactor[0]")))
                .as("FEE Factor is Not coming from PGPLUS BO")
                .containsExactlyInAnyOrder("CREDIT_CARD", "VISA", "IHDF","internationalCardPayment", "QR");

        double flatCommission = 10.0;

        Assertions.assertThat(response.jsonPath().getString("result.acquiringServiceFee[0]")).isEqualTo(String.valueOf(flatCommission))
                .as("FEE Factor is Not coming from PGPLUS BO");
    }


    @Test(description = "Verify a successful CC International txn on MDR merchant having slab bases <25 12 flat and percentage both commission and default 1 %")
    public void successfulFeeFactorCCInternationalMoreThan25TxnForMDR() throws Exception {

        PcfHelpher.assertConvFee(mdrCustom, "N");

        String payMode = "CC";
        User user = userManager.getForWrite(Label.BASIC);
        String orderId = validateFPO(user, mdrCustom);

        TxnAmount txnAmount = new TxnAmount();
        txnAmount.setValue("40");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mdrCustom, "SSO", user.ssoToken())
                .setOrderId(orderId)
                .setTxnAmount(txnAmount)
                .setPaymentMode("CREDIT_CARD")
                .setCardInfo(InternationalCCPaymentDetails)
                .setAuthMode("otp")
                .setExtendInfoOrderAlreadyCreated(false)
                .build();


        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);


        //MerchantStatus
        TxnStatus txnStatus = validateSuccessTxnStatusMDRTxn(mdrCustom.getId(), txnAmount.getValue(), orderId, payMode);
        validateSuccessPaymentStatusAPIMDRTxn(orderId, mdrCustom, payMode);
        validateSuccessTxnStatusListMDRTxn(mdrCustom.getId(), txnAmount.getValue(), orderId, payMode);
        validateOrderStatusAPI(mdrCustom.getId(), txnAmount.getValue(), orderId, payMode);


        //SearchTransaction PGPLUS BO

        Response response = PcfHelpher.searchTxnPgPlusBO(txnStatus);

        //FEE FACTOR For PAYMODE
        Assertions.assertThat(response.jsonPath().getString("result.cardScheme[0]")).isEqualTo("VISA");

        Assertions.assertThat(CommonHelpers.getFeeFactorAsList(response.jsonPath().getString("result.feeFactor[0]")))
                .as("FEE Factor is Not coming from PGPLUS BO")
                .containsExactlyInAnyOrder("CREDIT_CARD", "VISA", "IHDF","internationalCardPayment", "QR");

        double flatCommission = 12.0;
        double percentCommission = Integer.parseInt(txnAmount.getValue()) * 0.12;
        double TotalCommission = flatCommission + percentCommission;


        Assertions.assertThat(response.jsonPath().getString("result.acquiringServiceFee[0]")).isEqualTo(String.valueOf(TotalCommission))
                .as("FEE Factor is Not coming from PGPLUS BO");

    }

    @Test(description = "Verify a successful DC PREPAID txn on MDR merchant having flat commission of 10 and default 1 %")
    public void successfulFeeFactorDCPrePaidCard() throws Exception {

        PcfHelpher.assertConvFee(Constants.MerchantType.MASKED_MOBILE_ENABLED_PG2_RTDD, "N");
        User user = userManager.getForWrite(Label.BASIC);

       // enablePrepaidCardFF4jFlag();

        String payMode = "DC";
        String orderId = validateFPO(user, Constants.MerchantType.MASKED_MOBILE_ENABLED_PG2_RTDD);

        TxnAmount txnAmount = new TxnAmount();
        txnAmount.setValue("20");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(Constants.MerchantType.MASKED_MOBILE_ENABLED_PG2_RTDD, "SSO", user.ssoToken())
                .setOrderId(orderId)
                .setTxnAmount(new TxnAmount().setValue("20"))
                .setPaymentMode("DEBIT_CARD")
                .setCardInfo(PrepaidDCPaymentDetails)
                .setAuthMode("otp")
                .setExtendInfoOrderAlreadyCreated(false)
                .build();


        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);


        //MerchantStatus
        TxnStatus txnStatus = validateSuccessTxnStatusMDRTxn(Constants.MerchantType.MASKED_MOBILE_ENABLED_PG2_RTDD.getId(), txnAmount.getValue(), orderId, payMode);
        validateSuccessNativeTxnStatusMDRTxn(Constants.MerchantType.MASKED_MOBILE_ENABLED_PG2_RTDD.getId(), txnAmount.getValue(), orderId, payMode);
        validateSuccessPaymentStatusAPIMDRTxn(orderId, Constants.MerchantType.MASKED_MOBILE_ENABLED_PG2_RTDD, payMode);
        validateSuccessTxnStatusListMDRTxn(Constants.MerchantType.MASKED_MOBILE_ENABLED_PG2_RTDD.getId(), txnAmount.getValue(), orderId, payMode);
        validateOrderStatusAPI(Constants.MerchantType.MASKED_MOBILE_ENABLED_PG2_RTDD.getId(), txnAmount.getValue(), orderId, payMode);


        //SearchTransaction PGPLUS BO

        Response response = PcfHelpher.searchTxnPgPlusBO(txnStatus);

        //FEE FACTOR For PAYMODE
        Assertions.assertThat(response.jsonPath().getString("result.cardScheme[0]")).isEqualTo("VISA");

        Assertions.assertThat(CommonHelpers.getFeeFactorAsList(response.jsonPath().getString("result.feeFactor[0]")))
                .as("FEE Factor is Not coming from PGPLUS BO")
                .containsExactlyInAnyOrder("DEBIT_CARD", "VISA", "HDFC","prepaidCard", "QR");

        double flatCommission = 10.0;

        Assertions.assertThat(response.jsonPath().getString("result.acquiringServiceFee[0]")).isEqualTo(String.valueOf(flatCommission))
                .as("FEE Factor is Not coming from PGPLUS BO");

    }


    @Test(description = "Verify a successful NB txn on MDR merchant having flat commission of 10 and default 1 %")
    public void successfulFeeFactorNBForSlabMDR() throws Exception {

        PcfHelpher.assertConvFee(mdrCustom, "N");
        User user = userManager.getForWrite(Label.BASIC);

        String payMode = "NB";

        String orderId = validateFPO(user, mdrCustom);

        TxnAmount txnAmount = new TxnAmount();
        txnAmount.setValue("20");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mdrCustom, "SSO", user.ssoToken())
                .setOrderId(orderId)
                .setTxnAmount(txnAmount)
                .setPaymentMode("NET_BANKING")
                .setChannelCode("ICICI")
                .setAuthMode("otp")
                .setExtendInfoOrderAlreadyCreated(false)
                .build();

        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);

        //MerchantStatus
        TxnStatus txnStatus = validateSuccessTxnStatusMDRTxn(mdrCustom.getId(), txnAmount.getValue(), orderId, payMode);
        validateSuccessNativeTxnStatusMDRTxn(mdrCustom.getId(), txnAmount.getValue(), orderId, payMode);
        validateSuccessPaymentStatusAPIMDRTxn(orderId, mdrCustom, payMode);
        validateSuccessTxnStatusListMDRTxn(mdrCustom.getId(), txnAmount.getValue(), orderId, payMode);
        validateOrderStatusAPI(mdrCustom.getId(), txnAmount.getValue(), orderId, payMode);

        //SearchTransaction PGPLUS BO

        Response response = PcfHelpher.searchTxnPgPlusBO(txnStatus);

        //FEE FACTOR For PAYMODE

        Assertions.assertThat(CommonHelpers.getFeeFactorAsList(response.jsonPath().getString("result.feeFactor[0]")))
                .as("FEE Factor is Not coming from PGPLUS BO")
                .containsExactlyInAnyOrder("NET_BANKING", "ICICI", "ICICI", "QR");

        double flatCommission = 10.0;

        Assertions.assertThat(response.jsonPath().getString("result.acquiringServiceFee[0]")).isEqualTo(String.valueOf(flatCommission))
                .as("FEE Factor is Not coming from PGPLUS BO");

    }

}
