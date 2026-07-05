package scripts.customFeeRateFactor.MDRMerchant;

import com.paytm.LocalConfig;
import com.paytm.api.*;
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
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.dto.processTransactionV1.TxnAmount;
import com.paytm.framework.utils.DatabaseUtil;
import com.paytm.pages.CheckoutPage;
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
import java.util.Date;

@Feature("PGP-21710")
@Owner("Gagandeep")
public class DeferredMDRCustomRate extends PGPBaseTest {

    private static final String successMessage = "Txn Success";
    private static final String successStatus = "TXN_SUCCESS";
    private final CheckoutPage checkoutPage = new CheckoutPage();
    Constants.MerchantType mdrCustomRate = Constants.MerchantType.MDR_CUSTOM_RATE;
    Constants.MerchantType mdrCustom = Constants.MerchantType.MDR_INTL_MERC;
    private static final String MasterDCPaymentDetails = "|"+ PaymentDTO.DC +"|882|052026";
    private static final String InternationalCCPaymentDetails = "|4639170013374311|882|052026";
    private static final String PrepaidDCPaymentDetails = "|4766413897814514|882|052026";

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

   /* public void enablePrepaidCardFF4jFlag() {
        String queryForExp = "UPDATE FF4J_FEATURES SET ENABLE='1' WHERE FEAT_UID ='prepaidCard'";
        DatabaseUtil.getInstance().executeUpdateQuery(LocalConfig.PGP_DB_CONNECTION_URL, queryForExp);
        RedisAPI.deleteKey("FF4J_FEATURE_prepaidCard");
    }

    */

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
    @Test(description = "Verify a successful DC txn on MDR merchant having MASTER 10rs flat commission and default 1 %")
    public void successfulFeeFactorDCTxnForMDROffline(@Optional("false") boolean isNativePlus) throws Exception {

        PcfHelpher.assertConvFee(mdrCustomRate, "N");

        String payMode = "DC";

        User user = userManager.getForWrite(Label.BASIC);

        String txnAmount = "2";

        FetchPaymentOptResponseDTO fetchPaymentOptResponse = fetchPaymentOpt(user, "DEBIT_CARD", txnAmount, mdrCustomRate.getId());
        String orderId = fetchPaymentOptResponse.getBody().getOrderId();


        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), mdrCustomRate)
                .setOrderId(orderId)
                .setTxnValue(txnAmount)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);


        OrderDTO orderDTO = new OrderFactory.Native(mdrCustomRate, orderId, txnToken, "DEBIT_CARD")
                .setCardInfo(MasterDCPaymentDetails).build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);


        //MerchantStatus
        TxnStatus txnStatus = validateSuccessTxnStatusMDRTxn(mdrCustomRate.getId(), txnAmount, orderId, payMode);
        validateSuccessNativeTxnStatusMDRTxn(mdrCustomRate.getId(), txnAmount, orderId, payMode);
        validateSuccessPaymentStatusAPIMDRTxn(orderId, mdrCustomRate, payMode);
        validateSuccessTxnStatusListMDRTxn(mdrCustomRate.getId(), txnAmount, orderId, payMode);
        validateOrderStatusAPI(mdrCustomRate.getId(), txnAmount, orderId, payMode);

        //SearchTransaction PGPLUS BO

        Response response = PcfHelpher.searchTxnPgPlusBO(txnStatus);

        //FEE FACTOR For PAYMODE
        Assertions.assertThat(response.jsonPath().getString("result.cardScheme[0]")).isEqualTo("MASTER");

        Assertions.assertThat(CommonHelpers.getFeeFactorAsList(response.jsonPath().getString("result.feeFactor[0]")))
                .as("FEE Factor is Not coming from PGPLUS BO")
                .containsExactlyInAnyOrder("DEBIT_CARD", "MASTER", "HDFC", "API");

        double flatCommission = 10.0;


        Assertions.assertThat(response.jsonPath().getString("result.acquiringServiceFee[0]")).isEqualTo(String.valueOf(flatCommission))
                .as("FEE Factor is Not coming from PGPLUS BO");
    }



    @Parameters({"isNativePlus"})
    @Test(description = "Verify a successful CC International txn on MDR merchant having slab bases >25 10 flat commission and default 1 %")
        public void successfulFeeFactorCCInternationalLessThan25TxnForMDR(@Optional("false") boolean isNativePlus) throws Exception {

            PcfHelpher.assertConvFee(mdrCustom, "N");

            String payMode = "CC";
            User user = userManager.getForWrite(Label.BASIC);

            String txnAmount = "2";

            FetchPaymentOptResponseDTO fetchPaymentOptResponse = fetchPaymentOpt(user, "CREDIT_CARD", txnAmount, mdrCustom.getId());
            String orderId = fetchPaymentOptResponse.getBody().getOrderId();

            InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), mdrCustom)
                    .setOrderId(orderId)
                    .setTxnValue(txnAmount)
                    .build();
            String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);


            OrderDTO orderDTO = new OrderFactory.Native(mdrCustom, orderId, txnToken, "CREDIT_CARD")
                    .setCardInfo(InternationalCCPaymentDetails).build();
            checkoutPage.createNativeOrder(orderDTO, isNativePlus);


            //MerchantStatus
            TxnStatus txnStatus = validateSuccessTxnStatusMDRTxn(mdrCustom.getId(), txnAmount, orderId, payMode);
            validateSuccessNativeTxnStatusMDRTxn(mdrCustom.getId(), txnAmount, orderId, payMode);
            validateSuccessPaymentStatusAPIMDRTxn(orderId, mdrCustom, payMode);
            validateSuccessTxnStatusListMDRTxn(mdrCustom.getId(), txnAmount, orderId, payMode);
            validateOrderStatusAPI(mdrCustom.getId(), txnAmount, orderId, payMode);



            //SearchTransaction PGPLUS BO

            Response response = PcfHelpher.searchTxnPgPlusBO(txnStatus);

            //FEE FACTOR For PAYMODE
            Assertions.assertThat(response.jsonPath().getString("result.cardScheme[0]")).isEqualTo("VISA");

        Assertions.assertThat(CommonHelpers.getFeeFactorAsList(response.jsonPath().getString("result.feeFactor[0]")))
                .as("FEE Factor is Not coming from PGPLUS BO")
                .containsExactlyInAnyOrder("CREDIT_CARD", "VISA", "internationalCardPayment", "API", "IHDF");

        double flatCommission = 10.0;


        Assertions.assertThat(response.jsonPath().getString("result.acquiringServiceFee[0]")).isEqualTo(String.valueOf(flatCommission))
                    .as("FEE Factor is Not coming from PGPLUS BO");
        }

        @Parameters({"isNativePlus"})
        @Test(description = "Verify a successful CC International txn on MDR merchant having slab bases <25 12 flat and percentage both commission and default 1 %")
        public void successfulFeeFactorCCInternationalMoreThan100TxnForMDR(@Optional("false") boolean isNativePlus) throws Exception {

            PcfHelpher.assertConvFee(mdrCustom, "N");
            String payMode = "CC";
            User user = userManager.getForWrite(Label.BASIC);

            String txnAmount = "30";

            FetchPaymentOptResponseDTO fetchPaymentOptResponse = fetchPaymentOpt(user, "CREDIT_CARD", txnAmount, mdrCustomRate.getId());
            String orderId = fetchPaymentOptResponse.getBody().getOrderId();

            InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), mdrCustom)
                    .setOrderId(orderId)
                    .setTxnValue(txnAmount)
                    .build();
            String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);


            OrderDTO orderDTO = new OrderFactory.Native(mdrCustom, orderId, txnToken, "CREDIT_CARD")
                    .setCardInfo(InternationalCCPaymentDetails).build();
            checkoutPage.createNativeOrder(orderDTO, isNativePlus);


            //MerchantStatus
            TxnStatus txnStatus = validateSuccessTxnStatusMDRTxn(mdrCustom.getId(), txnAmount, orderId, payMode);
            validateSuccessNativeTxnStatusMDRTxn(mdrCustom.getId(), txnAmount, orderId, payMode);
            validateSuccessPaymentStatusAPIMDRTxn(orderId, mdrCustom, payMode);
            validateSuccessTxnStatusListMDRTxn(mdrCustom.getId(), txnAmount, orderId, payMode);
            validateOrderStatusAPI(mdrCustom.getId(), txnAmount, orderId, payMode);



            //SearchTransaction PGPLUS BO

            Response response = PcfHelpher.searchTxnPgPlusBO(txnStatus);

            //FEE FACTOR For PAYMODE
            Assertions.assertThat(response.jsonPath().getString("result.cardScheme[0]")).isEqualTo("VISA");

            Assertions.assertThat(CommonHelpers.getFeeFactorAsList(response.jsonPath().getString("result.feeFactor[0]")))
                    .as("FEE Factor is Not coming from PGPLUS BO")
                    .containsExactlyInAnyOrder("CREDIT_CARD", "VISA", "internationalCardPayment", "IHDF", "API");


            double flatCommission = 12.0;
            double percentCommission = 30 * 0.12;
            double TotalCommission = flatCommission + percentCommission;

            Assertions.assertThat(response.jsonPath().getString("result.acquiringServiceFee[0]")).isEqualTo(String.valueOf(TotalCommission))
                    .as("FEE Factor is Not coming from PGPLUS BO");

        }

        @Parameters({"isNativePlus"})
        @Test(description = "Verify a successful DC PREPAID txn on MDR merchant having flat commission of 10 and default 1 %")
        public void successfulFeeFactorDCPrePaidCard(@Optional("false") boolean isNativePlus) throws Exception {

            PcfHelpher.assertConvFee(Constants.MerchantType.MASKED_MOBILE_ENABLED_PG2_RTDD, "N");
            User user = userManager.getForWrite(Label.BASIC);

            String payMode = "DC";
            String txnAmount = "2";

         //   enablePrepaidCardFF4jFlag();

            FetchPaymentOptResponseDTO fetchPaymentOptResponse = fetchPaymentOpt(user, "DEBIT_CARD", txnAmount, mdrCustomRate.getId());
            String orderId = fetchPaymentOptResponse.getBody().getOrderId();

            InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.MASKED_MOBILE_ENABLED_PG2_RTDD)
                    .setOrderId(orderId)
                    .setTxnValue(txnAmount)
                    .build();
            String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);


            OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.MASKED_MOBILE_ENABLED, orderId, txnToken, "DEBIT_CARD")
                    .setCardInfo(PrepaidDCPaymentDetails).build();
            checkoutPage.createNativeOrder(orderDTO, isNativePlus);


            //MerchantStatus
            TxnStatus txnStatus = validateSuccessTxnStatusMDRTxn(Constants.MerchantType.MASKED_MOBILE_ENABLED_PG2_RTDD.getId(), txnAmount, orderId, payMode);
            validateSuccessNativeTxnStatusMDRTxn(Constants.MerchantType.MASKED_MOBILE_ENABLED_PG2_RTDD.getId(), txnAmount, orderId, payMode);
            validateSuccessPaymentStatusAPIMDRTxn(orderId, Constants.MerchantType.MASKED_MOBILE_ENABLED_PG2_RTDD, payMode);
            validateSuccessTxnStatusListMDRTxn(Constants.MerchantType.MASKED_MOBILE_ENABLED_PG2_RTDD.getId(), txnAmount, orderId, payMode);
            validateOrderStatusAPI(Constants.MerchantType.MASKED_MOBILE_ENABLED_PG2_RTDD.getId(), txnAmount, orderId, payMode);


            //SearchTransaction PGPLUS BO

            Response response = PcfHelpher.searchTxnPgPlusBO(txnStatus);

            //FEE FACTOR For PAYMODE
            Assertions.assertThat(response.jsonPath().getString("result.cardScheme[0]")).isEqualTo("VISA");

            Assertions.assertThat(CommonHelpers.getFeeFactorAsList(response.jsonPath().getString("result.feeFactor[0]")))
                    .as("FEE Factor is Not coming from PGPLUS BO")
                    .containsExactlyInAnyOrder("DEBIT_CARD", "VISA", "prepaidCard", "HDFC", "API");

            double flatCommission = 10.0;

            Assertions.assertThat(response.jsonPath().getString("result.acquiringServiceFee[0]")).isEqualTo(String.valueOf(flatCommission))
                    .as("FEE Factor is Not coming from PGPLUS BO");

        }

        @Parameters({"isNativePlus"})
        @Test(description = "Verify a successful NB txn on MDR merchant having flat commission of 15 and default 1 %")
        public void successfulFeeFactorNBForSlabMDR(@Optional("false") boolean isNativePlus)  throws Exception {

            PcfHelpher.assertConvFee(mdrCustomRate, "N");

            User user = userManager.getForWrite(Label.BASIC);

            String payMode = "NB";

            String txnAmount = "2";

            FetchPaymentOptResponseDTO fetchPaymentOptResponse = fetchPaymentOpt(user, "NET_BANKING", txnAmount, mdrCustomRate.getId());

            String orderId = fetchPaymentOptResponse.getBody().getOrderId();

            InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), mdrCustomRate)
                    .setOrderId(orderId)
                    .setTxnValue(txnAmount)
                    .build();
            String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

            OrderDTO orderDTO = new OrderFactory.Native(mdrCustomRate, orderId, txnToken, PayMethodType.NET_BANKING)
                    .setChannelCode("ICICI").build();

            checkoutPage.createNativeOrder(orderDTO, isNativePlus);

            //MerchantStatus
            TxnStatus txnStatus = validateSuccessTxnStatusMDRTxn(mdrCustomRate.getId(), txnAmount, orderId, payMode);
            validateSuccessNativeTxnStatusMDRTxn(mdrCustomRate.getId(), txnAmount, orderId, payMode);
            validateSuccessPaymentStatusAPIMDRTxn(orderId, mdrCustomRate, payMode);
            validateSuccessTxnStatusListMDRTxn(mdrCustomRate.getId(), txnAmount, orderId, payMode);
            validateOrderStatusAPI(mdrCustomRate.getId(), txnAmount, orderId, payMode);



            //SearchTransaction PGPLUS BO

            Response response = PcfHelpher.searchTxnPgPlusBO(txnStatus);

            //FEE FACTOR For PAYMODE

            Assertions.assertThat(CommonHelpers.getFeeFactorAsList(response.jsonPath().getString("result.feeFactor[0]")))
                    .as("FEE Factor is Not coming from PGPLUS BO")
                    .containsExactlyInAnyOrder("NET_BANKING", "ICICI", "ICICI", "API");

            double flatCommission = 15.0;

            Assertions.assertThat(response.jsonPath().getString("result.acquiringServiceFee[0]")).isEqualTo(String.valueOf(flatCommission))
                    .as("FEE Factor is Not coming from PGPLUS BO");

        }

    }
