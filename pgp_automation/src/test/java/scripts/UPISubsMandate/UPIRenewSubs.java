package scripts.UPISubsMandate;

import com.paytm.api.PreNotify;
import com.paytm.api.TxnStatus;
import com.paytm.api.nativeAPI.RenewSubscription;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.response.FetchPaymentOptResponseDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.NativeDTO.RenewSubscription.RenewSubscriptionDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.framework.utils.CommonUtils;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.qameta.allure.Step;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.awaitility.Duration;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import static com.paytm.appconstants.Constants.MerchantType.MGV_ADDNPAY;
import static org.awaitility.Awaitility.await;

@Owner("Gagandeep")
public class UPIRenewSubs extends PGPBaseTest {
    private final static String NATIVE_SUBSCRIPTION = "NATIVE_SUBSCRIPTION";
    private final static String NATIVE_MF_SIP = "NATIVE_MF_SIP";
    private final static String NATIVE_RENEW_MF_SIP = "SUBS_RENEWAL_MF_SIP";
    private final CheckoutPage checkoutPage = new CheckoutPage();
    private final String successMessage = "Subscription Txn accepted.";

    @Step()
    private InitTxnResponseDTO validateSuccessInitiateSubscription(InitTxnDTO initTxnDTO) {
        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultStatus())
                .as("resultStatus mismatch")
                .isEqualTo("S");
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultCode())
                .as("resultCode mismatch")
                .isEqualTo("0000");
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultMsg())
                .as("resultMsg mismatch")
                .isEqualToIgnoringCase("Success");
        return responseDTO;

    }


    private void validateTxnStatus(InitTxnDTO initTxnDTO, OrderDTO orderDTO) {
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("PPBLC")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateRefundAmnt("0.00")
                .validateSubsid(Constants.ValidationType.NON_EMPTY)
                .validateTxnDate(new Date())
                .AssertAll();
    }

    private void validateTxnStatus(InitTxnDTO initTxnDTO, ProcessTxnV1Request processTxnV1Request) {

        TxnStatus txnStatus = new TxnStatus(processTxnV1Request.getBody().getMid(), processTxnV1Request.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(processTxnV1Request.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("PPBL")
                .validateMid(processTxnV1Request.getBody().getMid())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateRefundAmnt("0.00")
                .validateSubsid(Constants.ValidationType.NON_EMPTY)
                .validateTxnDate(new Date())
                .AssertAll();
    }

    public static String executeRenewalAndFetchOrderId(Constants.MerchantType merchantType, String subsId, String txnAmount, String requestType) {
        RenewSubscriptionDTO renewSubscriptionDTO = new RenewSubscriptionDTO.Builder
                (merchantType.getId(), subsId, txnAmount)
                .setRequestType(requestType)
                .setMerchantKey(merchantType.getKey())
                .build();
        RenewSubscription renewSubscription = new RenewSubscription(renewSubscriptionDTO);
        JsonPath jsonPath = renewSubscription.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg"))
                .as("Result Message mismatch").isEqualTo("Subscription Txn accepted.");

        return renewSubscriptionDTO.getBody().getOrderId();
    }


    public static void modifySubsDatesInDB(String subsId, LocalDateTime FreqDate) {

        PGPHelpers.modifySubscriptionStartDate(Long.valueOf(subsId), FreqDate);
        PGPHelpers.modifySubscriptionPaymentCreateDate(Long.valueOf(subsId), FreqDate);
        PGPHelpers.modifySubscriptionPaymentUpdateDate(Long.valueOf(subsId), FreqDate);
        PGPHelpers.modifySubscriptionUpidetailCreateTime(Long.valueOf(subsId), FreqDate);
        PGPHelpers.modifySubscriptionUpidetailUpdateTime(Long.valueOf(subsId), FreqDate);
    }

    public static void modifyNotifyDatesInDB(String paytmRefId) {

        PGPHelpers.modifySubscriptionPreNotifyStatus(Long.valueOf(paytmRefId), "1");
        PGPHelpers.modifySubscriptionPreNotifyDate(Long.valueOf(paytmRefId), LocalDateTime.now().minusDays(2));
        PGPHelpers.modifySubscriptionPreNotifyTxnDate(Long.valueOf(paytmRefId), LocalDateTime.now());

    }





    /*----------------------------------------------------------------------------*/
    /*            Test cases For Native Mutual SIP Subscription RequestType       */
    /*----------------------------------------------------------------------------*/


    @Parameters({"isNativePlus"})
    @Test(description = "Renewal with subscriptionAmountType: FIX" +
            "Frequency Unit: Month Payment UPI Collect RequestType: SUBS_RENEWAL_MF_SIP")
    public void TC_001_Renew_MF_Subs_AmountTypeFix_Freq_Month(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_MUTUAL_MF;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_MF_SIP)
                .build();

        InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = responseDTO.getBody().getTxnToken();
        String subsId = responseDTO.getBody().getSubscriptionId();

        FetchPaymentOptResponseDTO fetchPaymentOptResponseDTO = NativeHelpers.fetchPaymentOptionResponse(txnToken, merchant.getId(), initTxnDTO.orderFromBody());
        boolean paymodeStatus = NativeHelpers.isFetchPaymentOptionStatusMatched(fetchPaymentOptResponseDTO, "UPI", false);
        Assertions.assertThat(paymodeStatus).as("UPI paymode status mismatch").isTrue();

        OrderDTO orderDTO = new OrderFactory.UPIMandate(merchant, txnToken, initTxnDTO.orderFromBody(), subsId)
                .setPayerAccount("test@paytm")
                .build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        validateTxnStatus(initTxnDTO, orderDTO);

        //Modify Dates to previous month and two days after to send preNotify

        LocalDateTime PreviousDate = LocalDateTime.now().minusMonths(1).plusDays(2);
        PGPHelpers.modifySubscriptionDueDate(Long.valueOf(subsId), LocalDateTime.now().plusDays(2));
        modifySubsDatesInDB(subsId, PreviousDate);

        //PreNotify sent
        PreNotify preNotify = new PreNotify(merchant, initTxnDTO.txnAmountFromBody(), subsId);
        Response response = preNotify.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.status"))
                .isEqualTo("SUCCESS").as("PreNotify is Not successful for Given Dates");

        String paytmRefId = response.jsonPath().getString("body.paytmReferenceId");
        modifyNotifyDatesInDB(paytmRefId);

        //Modify dates to previous month to execute renew now
        LocalDateTime TodayDate = LocalDateTime.now().minusMonths(1);
        modifySubsDatesInDB(subsId, TodayDate);

        PGPHelpers.modifySubscriptionDueDate(Long.valueOf(subsId), LocalDateTime.now());

        String orderId = executeRenewalAndFetchOrderId(merchant, subsId, initTxnDTO.txnAmountFromBody(), NATIVE_RENEW_MF_SIP);
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, orderId)).isNotNull();
        Assertions.assertThat(PGPHelpers.executeToFetchStatusInPaymentDetails(subsId, orderId)).isEqualTo("ACTIVE");
        Assertions.assertThat(PGPHelpers.getSubsDate(subsId)).contains(
                CommonHelpers.addMonths(CommonUtils.getdate("yyyy-MM-dd"), "yyyy-MM-dd", 1))
                .as("Due Date is Not updated as per Frequency Cycle");


        TxnStatus txnStatus = new TxnStatus(merchant.getId(), orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("PPBS")
                .validateMid(merchant.getId())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateRefundAmnt("0.00")
                .validateSubsid(Constants.ValidationType.NON_EMPTY)
                .validateTxnDate(new Date())
                .AssertAll();

    }


    @Parameters({"isNativePlus"})
    @Test(description = "Renewal with subscriptionAmountType: Variable" +
            "Frequency Unit: DAY Payment UPI Collect RequestType: SUBS_RENEWAL_MF_SIP")
    public void TC_002_Renew_MF_Subs_AmountTypeVariable_Freq_Day(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_MUTUAL_MF;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("DAY")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_MF_SIP)
                .build();

        InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = responseDTO.getBody().getTxnToken();
        String subsId = responseDTO.getBody().getSubscriptionId();

        FetchPaymentOptResponseDTO fetchPaymentOptResponseDTO = NativeHelpers.fetchPaymentOptionResponse(txnToken, merchant.getId(), initTxnDTO.orderFromBody());
        boolean paymodeStatus = NativeHelpers.isFetchPaymentOptionStatusMatched(fetchPaymentOptResponseDTO, "UPI", false);
        Assertions.assertThat(paymodeStatus).as("UPI paymode status mismatch").isTrue();

        OrderDTO orderDTO = new OrderFactory.UPIMandate(merchant, txnToken, initTxnDTO.orderFromBody(), subsId)
                .setPayerAccount("test@paytm")
                .build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);

        validateTxnStatus(initTxnDTO, orderDTO);
        String date = CommonUtils.getdate("dd-MM-yyyy");
        PreNotify preNotify = new PreNotify(merchant, initTxnDTO.txnAmountFromBody(), subsId, CommonHelpers.addDays(date, "dd-MM-yyyy", 1));

        Response response = preNotify.execute();

        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.status"))
                .isEqualTo("SUCCESS").as("PreNotify is Not successful for Given Dates");

        String paytmRefId = response.jsonPath().getString("body.paytmReferenceId");

        //Modify Notify dates to two days before and txn date to day
        PGPHelpers.modifySubscriptionPreNotifyStatus(Long.valueOf(paytmRefId), "1");
        PGPHelpers.modifySubscriptionPreNotifyDate(Long.valueOf(paytmRefId), LocalDateTime.now().minusDays(1));
        PGPHelpers.modifySubscriptionPreNotifyTxnDate(Long.valueOf(paytmRefId), LocalDateTime.now());

        LocalDateTime TodayDate = LocalDateTime.now().minusDays(1);
        modifySubsDatesInDB(subsId, TodayDate);

        String orderId = executeRenewalAndFetchOrderId(merchant, subsId, initTxnDTO.txnAmountFromBody(), NATIVE_RENEW_MF_SIP);
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, orderId)).isNotNull();
        Assertions.assertThat(PGPHelpers.executeToFetchStatusInPaymentDetails(subsId, orderId)).isEqualTo("ACTIVE");
        Assertions.assertThat(PGPHelpers.getSubsDate(subsId)).contains(
                CommonHelpers.addDays(CommonUtils.getdate("yyyy-MM-dd"), "yyyy-MM-dd", 1))
                .as("Due Date is Not updated as per Frequency Cycle");

    }


    @Parameters({"isNativePlus"})
    @Test(description = "Renewal with subscriptionAmountType: Variable" +
            "Frequency Unit: YEAR Payment, UPI Intent ,Request Type: SUBS_RENEWAL_MF_SIP")
    public void TC_003_Renew_MF_Subs_AmountTypeVariable_Freq_Year(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_MUTUAL_MF;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1000")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("2000")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("YEAR")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_MF_SIP)
                .build();

        InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);

        String txnToken = responseDTO.getBody().getTxnToken();
        String subsId = responseDTO.getBody().getSubscriptionId();

        FetchPaymentOptResponseDTO fetchPaymentOptResponseDTO = NativeHelpers.fetchPaymentOptionResponse(txnToken, merchant.getId(), initTxnDTO.orderFromBody());
        boolean paymodeStatus = NativeHelpers.isFetchPaymentOptionStatusMatched(fetchPaymentOptResponseDTO, "UPI", false);
        Assertions.assertThat(paymodeStatus).as("UPI paymode status mismatch").isTrue();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchant.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("UPI_INTENT")
                .build();

        NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        validateTxnStatus(initTxnDTO, processTxnV1Request);

        LocalDateTime PreviousDate = LocalDateTime.now().minusYears(1).plusDays(2);
        PGPHelpers.modifySubscriptionDueDate(Long.valueOf(subsId), LocalDateTime.now().plusDays(2));
        modifySubsDatesInDB(subsId, PreviousDate);


        PreNotify preNotify = new PreNotify(merchant, initTxnDTO.txnAmountFromBody(), subsId);
        Response response = preNotify.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.status"))
                .isEqualTo("SUCCESS").as("PreNotify is Not successful for Given Dates");

        String paytmRefId = response.jsonPath().getString("body.paytmReferenceId");
        modifyNotifyDatesInDB(paytmRefId);

        LocalDateTime frequencyReqDate = LocalDateTime.now().minusYears(1);

        PGPHelpers.modifySubscriptionDueDate(Long.valueOf(subsId), LocalDateTime.now());

        modifySubsDatesInDB(subsId, frequencyReqDate);

        String orderId = executeRenewalAndFetchOrderId(merchant, subsId, initTxnDTO.txnAmountFromBody(), NATIVE_RENEW_MF_SIP);
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, orderId)).isNotNull();
        Assertions.assertThat(PGPHelpers.executeToFetchStatusInPaymentDetails(subsId, orderId)).isEqualTo("ACTIVE");
        Assertions.assertThat(PGPHelpers.getSubsDate(subsId)).contains(
                CommonHelpers.addYears(CommonUtils.getdate("yyyy-MM-dd"), "yyyy-MM-dd", 1))
                .as("Due Date is Not updated as per Frequency Cycle");

    }


    @Parameters({"isNativePlus"})
    @Test(description = "Renewal with subscriptionAmountType: FIX" +
            "Frequency Unit: WEEK Payment, UPI Intent ,Request Type:SUBS_RENEWAL_MF_SIP ")
    public void TC_004_Renew_MF_Subs_AmountTypeVariable_Freq_Week(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_MUTUAL_MF;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1000")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("1000")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("WEEK")
                .setSubscriptionGraceDays("6")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_MF_SIP)
                .build();

        InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);

        String txnToken = responseDTO.getBody().getTxnToken();
        String subsId = responseDTO.getBody().getSubscriptionId();

        FetchPaymentOptResponseDTO fetchPaymentOptResponseDTO = NativeHelpers.fetchPaymentOptionResponse(txnToken, merchant.getId(), initTxnDTO.orderFromBody());
        boolean paymodeStatus = NativeHelpers.isFetchPaymentOptionStatusMatched(fetchPaymentOptResponseDTO, "UPI", false);
        Assertions.assertThat(paymodeStatus).as("UPI paymode status mismatch").isTrue();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchant.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("UPI_INTENT")
                .build();

        NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        validateTxnStatus(initTxnDTO, processTxnV1Request);

        LocalDateTime PreviousDate = LocalDateTime.now().minusDays(7).plusDays(2);

        PGPHelpers.modifySubscriptionDueDate(Long.valueOf(subsId), LocalDateTime.now().plusDays(2));
        modifySubsDatesInDB(subsId, PreviousDate);


        PreNotify preNotify = new PreNotify(merchant, initTxnDTO.txnAmountFromBody(), subsId);
        Response response = preNotify.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.status"))
                .isEqualTo("SUCCESS").as("PreNotify is Not successful for Given Dates");

        String paytmRefId = response.jsonPath().getString("body.paytmReferenceId");

        modifyNotifyDatesInDB(paytmRefId);

        LocalDateTime frequencyReqDate = LocalDateTime.now().minusDays(7);
        modifySubsDatesInDB(subsId, frequencyReqDate);

        PGPHelpers.modifySubscriptionDueDate(Long.valueOf(subsId), LocalDateTime.now());
        String orderId = executeRenewalAndFetchOrderId(merchant, subsId, initTxnDTO.txnAmountFromBody(), NATIVE_RENEW_MF_SIP);
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, orderId)).isNotNull();
        Assertions.assertThat(PGPHelpers.executeToFetchStatusInPaymentDetails(subsId, orderId)).isEqualTo("ACTIVE");
        Assertions.assertThat(PGPHelpers.getSubsDate(subsId))
                .contains(CommonHelpers.addDays(CommonUtils.getdate("yyyy-MM-dd"), "yyyy-MM-dd", 7))
                .as("Due Date is Not updated as per Frequency Cycle");

    }


    @Parameters({"isNativePlus"})
    @Test(description = "Renewal with subscriptionAmountType: VARIABLE" +
            "Frequency Unit: QUARTER Payment, UPI Collect ,Request Type:SUBS_RENEWAL_MF_SIP ")
    public void TC_005_Renew_MF_Subs_AmountTypeVariable_Freq_Quarter(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_MUTUAL_MF;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("100")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("1000")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("QUARTER")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_MF_SIP)
                .build();

        InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);

        String txnToken = responseDTO.getBody().getTxnToken();
        String subsId = responseDTO.getBody().getSubscriptionId();

        FetchPaymentOptResponseDTO fetchPaymentOptResponseDTO = NativeHelpers.fetchPaymentOptionResponse(txnToken, merchant.getId(), initTxnDTO.orderFromBody());
        boolean paymodeStatus = NativeHelpers.isFetchPaymentOptionStatusMatched(fetchPaymentOptResponseDTO, "UPI", false);
        Assertions.assertThat(paymodeStatus).as("UPI paymode status mismatch").isTrue();

        OrderDTO orderDTO = new OrderFactory.UPIMandate(merchant, txnToken, initTxnDTO.orderFromBody(), subsId)
                .setPayerAccount("test@paytm")
                .build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);

        validateTxnStatus(initTxnDTO, orderDTO);


        LocalDateTime PreviousDate = LocalDateTime.now().minusMonths(3).plusDays(2);
        PGPHelpers.modifySubscriptionDueDate(Long.valueOf(subsId), LocalDateTime.now().plusDays(2));
        modifySubsDatesInDB(subsId, PreviousDate);


        PreNotify preNotify = new PreNotify(merchant, initTxnDTO.txnAmountFromBody(), subsId);
        Response response = preNotify.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.status"))
                .isEqualTo("SUCCESS").as("PreNotify is Not successful for Given Dates");

        String paytmRefId = response.jsonPath().getString("body.paytmReferenceId");
        modifyNotifyDatesInDB(paytmRefId);

        LocalDateTime frequencyReqDate = LocalDateTime.now().minusMonths(3);
        modifySubsDatesInDB(subsId, frequencyReqDate);

        PGPHelpers.modifySubscriptionDueDate(Long.valueOf(subsId), LocalDateTime.now());

        String orderId = executeRenewalAndFetchOrderId(merchant, subsId, initTxnDTO.txnAmountFromBody(), NATIVE_RENEW_MF_SIP);
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, orderId)).isNotNull();
        Assertions.assertThat(PGPHelpers.executeToFetchStatusInPaymentDetails(subsId, orderId)).isEqualTo("ACTIVE");
        Assertions.assertThat(PGPHelpers.getSubsDate(subsId)).contains(
                CommonHelpers.addMonths(CommonUtils.getdate("yyyy-MM-dd"), "yyyy-MM-dd", 3))
                .as("Due Date is Not updated as per Frequency Cycle");
    }


    @Parameters({"isNativePlus"})
    @Test(description = "Renewal with subscriptionAmountType: FIX" +
            "Frequency Unit: BI_MONTHLY Payment, UPI Intent ,Request Type:SUBS_RENEWAL_MF_SIP ")
    public void TC_006_Renew_MF_Subs_AmountTypeVariable_Freq_BiMonthly(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_MUTUAL_MF;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1500")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("1500")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("BI_MONTHLY")
                .setSubscriptionGraceDays("5")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_MF_SIP)
                .build();

        InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);

        String txnToken = responseDTO.getBody().getTxnToken();
        String subsId = responseDTO.getBody().getSubscriptionId();

        FetchPaymentOptResponseDTO fetchPaymentOptResponseDTO = NativeHelpers.fetchPaymentOptionResponse(txnToken, merchant.getId(), initTxnDTO.orderFromBody());
        boolean paymodeStatus = NativeHelpers.isFetchPaymentOptionStatusMatched(fetchPaymentOptResponseDTO, "UPI", false);
        Assertions.assertThat(paymodeStatus).as("UPI paymode status mismatch").isTrue();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchant.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("UPI_INTENT")
                .build();

        NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        validateTxnStatus(initTxnDTO, processTxnV1Request);

        LocalDateTime PreviousDate = LocalDateTime.now().minusMonths(2).plusDays(2);
        PGPHelpers.modifySubscriptionDueDate(Long.valueOf(subsId), LocalDateTime.now().plusDays(2));
        modifySubsDatesInDB(subsId, PreviousDate);


        PreNotify preNotify = new PreNotify(merchant, initTxnDTO.txnAmountFromBody(), subsId);
        Response response = preNotify.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.status"))
                .isEqualTo("SUCCESS").as("PreNotify is Not successful for Given Dates");

        String paytmRefId = response.jsonPath().getString("body.paytmReferenceId");
        modifyNotifyDatesInDB(paytmRefId);

        LocalDateTime frequencyReqDate = LocalDateTime.now().minusMonths(2);
        modifySubsDatesInDB(subsId, frequencyReqDate);
        PGPHelpers.modifySubscriptionDueDate(Long.valueOf(subsId), LocalDateTime.now());

        String orderId = executeRenewalAndFetchOrderId(merchant, subsId, initTxnDTO.txnAmountFromBody(), NATIVE_RENEW_MF_SIP);
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, orderId)).isNotNull();
        Assertions.assertThat(PGPHelpers.executeToFetchStatusInPaymentDetails(subsId, orderId)).isEqualTo("ACTIVE");
        Assertions.assertThat(PGPHelpers.getSubsDate(subsId)).contains(
                CommonHelpers.addMonths(CommonUtils.getdate("yyyy-MM-dd"), "yyyy-MM-dd", 2))
                .as("Due Date is Not updated as per Frequency Cycle");

    }


    @Parameters({"isNativePlus"})
    @Test(description = "Renewal with subscriptionAmountType: Variable" +
            "Frequency Unit: SEMI_ANNUALLY Payment, UPI Collect ,Request Type:SUBS_RENEWAL_MF_SIP ")
    public void TC_007_Renew_MF_Subs_AmountTypeVariable_Freq_SemiAnnually(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_MUTUAL_MF;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("20")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("200")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("SEMI_ANNUALLY")
                .setSubscriptionGraceDays("2")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_MF_SIP)
                .build();

        InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);

        String txnToken = responseDTO.getBody().getTxnToken();
        String subsId = responseDTO.getBody().getSubscriptionId();

        FetchPaymentOptResponseDTO fetchPaymentOptResponseDTO = NativeHelpers.fetchPaymentOptionResponse(txnToken, merchant.getId(), initTxnDTO.orderFromBody());
        boolean paymodeStatus = NativeHelpers.isFetchPaymentOptionStatusMatched(fetchPaymentOptResponseDTO, "UPI", false);
        Assertions.assertThat(paymodeStatus).as("UPI paymode status mismatch").isTrue();

        OrderDTO orderDTO = new OrderFactory.UPIMandate(merchant, txnToken, initTxnDTO.orderFromBody(), subsId)
                .setPayerAccount("test@paytm")
                .build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        validateTxnStatus(initTxnDTO, orderDTO);


        LocalDateTime PreviousDate = LocalDateTime.now().minusMonths(6).plusDays(2);
        PGPHelpers.modifySubscriptionDueDate(Long.valueOf(subsId), LocalDateTime.now().plusDays(2));
        modifySubsDatesInDB(subsId, PreviousDate);


        PreNotify preNotify = new PreNotify(merchant, initTxnDTO.txnAmountFromBody(), subsId);
        Response response = preNotify.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.status"))
                .isEqualTo("SUCCESS").as("PreNotify is Not successful for Given Dates");

        String paytmRefId = response.jsonPath().getString("body.paytmReferenceId");
        modifyNotifyDatesInDB(paytmRefId);

        LocalDateTime frequencyReqDate = LocalDateTime.now().minusMonths(6);
        modifySubsDatesInDB(subsId, frequencyReqDate);
        PGPHelpers.modifySubscriptionDueDate(Long.valueOf(subsId), LocalDateTime.now());

        String orderId = executeRenewalAndFetchOrderId(merchant, subsId, initTxnDTO.txnAmountFromBody(), NATIVE_RENEW_MF_SIP);
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, orderId)).isNotNull();
        Assertions.assertThat(PGPHelpers.executeToFetchStatusInPaymentDetails(subsId, orderId)).isEqualTo("ACTIVE");
        Assertions.assertThat(PGPHelpers.getSubsDate(subsId)).contains(
                CommonHelpers.addMonths(CommonUtils.getdate("yyyy-MM-dd"), "yyyy-MM-dd", 6))
                .as("Due Date is Not updated as per Frequency Cycle");


    }

    @Parameters({"isNativePlus"})
    @Test(description = "Multiple Renewal on Same Subscription" +
            "Frequency Unit: Day, UPI Collect ,Request Type:SUBS_RENEWAL_MF_SIP ")
    public void TC_001_Renew_MF_Subs_MultipleRenewal(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.MF_SIP_NEW_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("1000")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("YEAR")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_MF_SIP)
                .build();

        InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);

        String txnToken = responseDTO.getBody().getTxnToken();
        String subsId = responseDTO.getBody().getSubscriptionId();

        FetchPaymentOptResponseDTO fetchPaymentOptResponseDTO = NativeHelpers.fetchPaymentOptionResponse(txnToken, merchant.getId(), initTxnDTO.orderFromBody());
        boolean paymodeStatus = NativeHelpers.isFetchPaymentOptionStatusMatched(fetchPaymentOptResponseDTO, "UPI", false);
        Assertions.assertThat(paymodeStatus).as("UPI paymode status mismatch").isTrue();

        OrderDTO orderDTO = new OrderFactory.UPIMandate(merchant, txnToken, initTxnDTO.orderFromBody(), subsId)
                .setPayerAccount("test@paytm")
                .build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        validateTxnStatus(initTxnDTO, orderDTO);


        //Modify Dates to previous month and two days after to send preNotify

        LocalDateTime PreviousDate = LocalDateTime.now().minusYears(1).plusDays(2);
        PGPHelpers.modifySubscriptionDueDate(Long.valueOf(subsId), LocalDateTime.now().plusDays(2));
        modifySubsDatesInDB(subsId, PreviousDate);

        //PreNotify sent
        PreNotify preNotify = new PreNotify(merchant, initTxnDTO.txnAmountFromBody(), subsId);
        Response response = preNotify.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.status"))
                .isEqualTo("SUCCESS").as("PreNotify is Not successful for Given Dates");

        String paytmRefId = response.jsonPath().getString("body.paytmReferenceId");
        modifyNotifyDatesInDB(paytmRefId);

        //Modify dates to previous month to execute renew now
        LocalDateTime TodayDate = LocalDateTime.now().minusYears(1);
        modifySubsDatesInDB(subsId, TodayDate);

        PGPHelpers.modifySubscriptionDueDate(Long.valueOf(subsId), LocalDateTime.now());

        String orderId = executeRenewalAndFetchOrderId(merchant, subsId, initTxnDTO.txnAmountFromBody(), NATIVE_RENEW_MF_SIP);
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, orderId)).isNotNull();
        Assertions.assertThat(PGPHelpers.executeToFetchStatusInPaymentDetails(subsId, orderId)).isEqualTo("ACTIVE");
        Assertions.assertThat(PGPHelpers.getSubsDate(subsId)).contains(
                CommonHelpers.addYears(CommonUtils.getdate("yyyy-MM-dd"), "yyyy-MM-dd", 1))
                .as("Due Date is Not updated as per Frequency Cycle");


        //Send Second Renewal for same subs ID

        LocalDateTime PreviousDateSecRenew = LocalDateTime.now().minusYears(2).plusDays(2);
        PGPHelpers.modifySubscriptionDueDate(Long.valueOf(subsId), LocalDateTime.now().plusDays(2));


        modifySubsDatesInDB(subsId, PreviousDateSecRenew);


        PGPHelpers.modifySubscriptionPaymentCreateDate(Long.valueOf(subsId), LocalDateTime.now().minusYears(1).plusDays(2), "RENEWAL");
        PGPHelpers.modifySubscriptionPaymentUpdateDate(Long.valueOf(subsId), LocalDateTime.now().minusYears(1).plusDays(2), "RENEWAL");

        //PreNotify sent
        PreNotify preNotifySecondRenew = new PreNotify(merchant, initTxnDTO.txnAmountFromBody(), subsId);
        Response responseSecondRenew = preNotifySecondRenew.execute();
        Assertions.assertThat(responseSecondRenew.jsonPath().getString("body.resultInfo.status"))
                .isEqualTo("SUCCESS").as("PreNotify is Not successful for Given Dates");

        String paytmRefIdSecRenew = responseSecondRenew.jsonPath().getString("body.paytmReferenceId");
        modifyNotifyDatesInDB(paytmRefIdSecRenew);

        //Modify dates to previous month to execute renew now
        LocalDateTime TodayDate2Renew = LocalDateTime.now().minusYears(2);
        modifySubsDatesInDB(subsId, TodayDate2Renew);

        PGPHelpers.modifySubscriptionPaymentCreateDate(Long.valueOf(subsId), LocalDateTime.now().minusYears(1), "RENEWAL");
        PGPHelpers.modifySubscriptionPaymentUpdateDate(Long.valueOf(subsId), LocalDateTime.now().minusYears(1), "RENEWAL");

        PGPHelpers.modifySubscriptionDueDate(Long.valueOf(subsId), LocalDateTime.now());

        String orderId2 = executeRenewalAndFetchOrderId(merchant, subsId, initTxnDTO.txnAmountFromBody(), NATIVE_RENEW_MF_SIP);
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, orderId2)).isNotNull();
        Assertions.assertThat(PGPHelpers.executeToFetchStatusInPaymentDetails(subsId, orderId2)).isEqualTo("ACTIVE");
        Assertions.assertThat(PGPHelpers.getSubsDate(subsId)).contains(
                CommonHelpers.addYears(CommonUtils.getdate("yyyy-MM-dd"), "yyyy-MM-dd", 1))
                .as("Due Date is Not updated as per Frequency Cycle");
    }


    //GRACE PERIOD CHECK


    @Parameters({"isNativePlus"})
    @Test(description = "Renew Within Grace Period" +
            "Frequency Unit: Day, UPI INTENT ,Request Type:Subscription_Default ")
    public void TC_001_Renew_MF_Subs_WithInGracePeriod(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_MUTUAL_MF;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("1000")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("YEAR")
                .setSubscriptionGraceDays("350")                            //Grace Period
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_MF_SIP)
                .build();

        InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);

        String txnToken = responseDTO.getBody().getTxnToken();
        String subsId = responseDTO.getBody().getSubscriptionId();

        FetchPaymentOptResponseDTO fetchPaymentOptResponseDTO = NativeHelpers.fetchPaymentOptionResponse(txnToken, merchant.getId(), initTxnDTO.orderFromBody());
        boolean paymodeStatus = NativeHelpers.isFetchPaymentOptionStatusMatched(fetchPaymentOptResponseDTO, "UPI", false);
        Assertions.assertThat(paymodeStatus).as("UPI paymode status mismatch").isTrue();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchant.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("UPI_INTENT")
                .build();

        NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        validateTxnStatus(initTxnDTO, processTxnV1Request);


        //Modify Dates to previous month and two days after to send preNotify

        LocalDateTime PreviousDate = LocalDateTime.now().minusMonths(1).plusDays(2);                //frequency is within
        PGPHelpers.modifySubscriptionDueDate(Long.valueOf(subsId), LocalDateTime.now().plusDays(2));
        modifySubsDatesInDB(subsId, PreviousDate);

        //PreNotify sent
        PreNotify preNotify = new PreNotify(merchant, initTxnDTO.txnAmountFromBody(), subsId);
        Response response = preNotify.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.status"))
                .isEqualTo("SUCCESS").as("PreNotify is Not successful for Given Dates");

        String paytmRefId = response.jsonPath().getString("body.paytmReferenceId");
        modifyNotifyDatesInDB(paytmRefId);

        //Modify dates to previous month to execute renew now
        LocalDateTime TodayDate = LocalDateTime.now().minusMonths(1);
        modifySubsDatesInDB(subsId, TodayDate);

        PGPHelpers.modifySubscriptionDueDate(Long.valueOf(subsId), LocalDateTime.now());

        String orderId = executeRenewalAndFetchOrderId(merchant, subsId, initTxnDTO.txnAmountFromBody(), NATIVE_RENEW_MF_SIP);
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, orderId)).isNotNull();
        Assertions.assertThat(PGPHelpers.executeToFetchStatusInPaymentDetails(subsId, orderId)).isEqualTo("ACTIVE");
        Assertions.assertThat(PGPHelpers.getSubsDate(subsId)).contains(
                CommonHelpers.addYears(CommonUtils.getdate("yyyy-MM-dd"), "yyyy-MM-dd", 1))
                .as("Due Date is Not updated as per Frequency Cycle");


    }


//different Amount is Send for Renewal

    @Parameters({"isNativePlus"})
    @Test(description = "Renewal with subscriptionAmountType: FIX" +
            "Frequency Unit: Month Payment UPI Collect RequestType: SUBS_RENEWAL_MF_SIP")
    public void TC_001_Renew_MF_Failed_DifferentAmountSent(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_MUTUAL_MF;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("100")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("1000")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_MF_SIP)
                .build();

        InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = responseDTO.getBody().getTxnToken();
        String subsId = responseDTO.getBody().getSubscriptionId();

        FetchPaymentOptResponseDTO fetchPaymentOptResponseDTO = NativeHelpers.fetchPaymentOptionResponse(txnToken, merchant.getId(), initTxnDTO.orderFromBody());
        boolean paymodeStatus = NativeHelpers.isFetchPaymentOptionStatusMatched(fetchPaymentOptResponseDTO, "UPI", false);
        Assertions.assertThat(paymodeStatus).as("UPI paymode status mismatch").isTrue();

        OrderDTO orderDTO = new OrderFactory.UPIMandate(merchant, txnToken, initTxnDTO.orderFromBody(), subsId)
                .setPayerAccount("test@paytm")
                .build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        validateTxnStatus(initTxnDTO, orderDTO);

        //Modify Dates to previous month and two days after to send preNotify

        LocalDateTime PreviousDate = LocalDateTime.now().minusMonths(1).plusDays(2);
        PGPHelpers.modifySubscriptionDueDate(Long.valueOf(subsId), LocalDateTime.now().plusDays(2));
        modifySubsDatesInDB(subsId, PreviousDate);

        //PreNotify sent
        PreNotify preNotify = new PreNotify(merchant, initTxnDTO.txnAmountFromBody(), subsId);
        Response response = preNotify.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.status"))
                .isEqualTo("SUCCESS").as("PreNotify is Not successful for Given Dates");

        String paytmRefId = response.jsonPath().getString("body.paytmReferenceId");
        modifyNotifyDatesInDB(paytmRefId);

        //Modify dates to previous month to execute renew now
        LocalDateTime TodayDate = LocalDateTime.now().minusMonths(1);
        modifySubsDatesInDB(subsId, TodayDate);

        PGPHelpers.modifySubscriptionDueDate(Long.valueOf(subsId), LocalDateTime.now());

        RenewSubscriptionDTO renewSubscriptionDTO = new RenewSubscriptionDTO.Builder
                (merchant.getId(), subsId, "12")
                .setRequestType(NATIVE_RENEW_MF_SIP)
                .setMerchantKey(merchant.getKey())
                .build();
        RenewSubscription renewSubscription = new RenewSubscription(renewSubscriptionDTO);
        JsonPath jsonPath = renewSubscription.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg"))
                .as("Result Message mismatch").isEqualTo("The debit amount does not match with the one sent in the notification request");
    }


    @Parameters({"isNativePlus"})
    @Test(description = "Renewal with subscriptionAmountType: FIX" +
            "Frequency Unit: Month Payment UPI Collect RequestType: SUBS_RENEWAL_MF_SIP")
    public void TC_001_Renew_Failure_MF_Subs_UPICollect(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.PG2_SUBS_MFSIP;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("99.99")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("1000")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("YEAR")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_MF_SIP)
                .build();

        InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = responseDTO.getBody().getTxnToken();
        String subsId = responseDTO.getBody().getSubscriptionId();

        FetchPaymentOptResponseDTO fetchPaymentOptResponseDTO = NativeHelpers.fetchPaymentOptionResponse(txnToken, merchant.getId(), initTxnDTO.orderFromBody());
        boolean paymodeStatus = NativeHelpers.isFetchPaymentOptionStatusMatched(fetchPaymentOptResponseDTO, "UPI", false);
        Assertions.assertThat(paymodeStatus).as("UPI paymode status mismatch").isTrue();

        OrderDTO orderDTO = new OrderFactory.UPIMandate(merchant, txnToken, initTxnDTO.orderFromBody(), subsId)
                .setPayerAccount("test@paytm")
                .build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        validateTxnStatus(initTxnDTO, orderDTO);

        //Modify Dates to previous month and two days after to send preNotify

        LocalDateTime PreviousDate = LocalDateTime.now().minusYears(1).plusDays(2);
        PGPHelpers.modifySubscriptionDueDate(Long.valueOf(subsId), LocalDateTime.now().plusDays(2));
        modifySubsDatesInDB(subsId, PreviousDate);

        //PreNotify sent
        PreNotify preNotify = new PreNotify(merchant, initTxnDTO.txnAmountFromBody(), subsId);
        Response response = preNotify.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.status"))
                .isEqualTo("SUCCESS").as("PreNotify is Not successful for Given Dates");

        String paytmRefId = response.jsonPath().getString("body.paytmReferenceId");
        modifyNotifyDatesInDB(paytmRefId);

        //Modify dates to previous month to execute renew now
        LocalDateTime TodayDate = LocalDateTime.now().minusYears(1);
        modifySubsDatesInDB(subsId, TodayDate);

        PGPHelpers.modifySubscriptionDueDate(Long.valueOf(subsId), LocalDateTime.now());

        String orderId = executeRenewalAndFetchOrderId(merchant, subsId, initTxnDTO.txnAmountFromBody(), NATIVE_RENEW_MF_SIP);
        TxnStatus txnStatus = new TxnStatus(merchant.getId(), orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_FAILURE")
                .validateTxnType("SALE")
                .validateGatewayName("PPBS")
                .validateMid(merchant.getId())
                .validatePaymentMode("UPI")
                .validateRespCode("227")
                .validateRefundAmnt("0.00")
                .validateSubsid(Constants.ValidationType.NON_EMPTY)
                .validateTxnDate(new Date())
                .AssertAll();

    }

    /*----------------------------------------------------------------------------*/
    /*            Test cases For Native Subscription RequestType Default          */
    /*----------------------------------------------------------------------------*/


    @Parameters({"isNativePlus"})
    @Test(description = "Renewal with subscriptionAmountType: VARIABLE" +
            "Frequency Unit: Month Payment UPI_INTENT RequestType: Subscription Default")
    public void TC_001_Renew_Subs_AmountTypeFix_Freq_Month(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("100")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("1000")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("10")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_SUBSCRIPTION)
                .build();

        InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = responseDTO.getBody().getTxnToken();
        String subsId = responseDTO.getBody().getSubscriptionId();

        FetchPaymentOptResponseDTO fetchPaymentOptResponseDTO = NativeHelpers.fetchPaymentOptionResponse(txnToken, merchant.getId(), initTxnDTO.orderFromBody());
        boolean paymodeStatus = NativeHelpers.isFetchPaymentOptionStatusMatched(fetchPaymentOptResponseDTO, "UPI", false);
        Assertions.assertThat(paymodeStatus).as("UPI paymode status mismatch").isTrue();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchant.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("UPI_INTENT")
                .build();

        NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        validateTxnStatus(initTxnDTO, processTxnV1Request);

        //Modify Dates to previous month and two days after to send preNotify

        LocalDateTime PreviousDate = LocalDateTime.now().minusMonths(1).plusDays(2);
        PGPHelpers.modifySubscriptionDueDate(Long.valueOf(subsId), LocalDateTime.now().plusDays(2));
        modifySubsDatesInDB(subsId, PreviousDate);

        //PreNotify sent
        PreNotify preNotify = new PreNotify(merchant, initTxnDTO.txnAmountFromBody(), subsId);
        Response response = preNotify.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.status"))
                .isEqualTo("SUCCESS").as("PreNotify is Not successful for Given Dates");

        String paytmRefId = response.jsonPath().getString("body.paytmReferenceId");
        modifyNotifyDatesInDB(paytmRefId);

        //Modify dates to previous month to execute renew now
        LocalDateTime TodayDate = LocalDateTime.now().minusMonths(1);
        modifySubsDatesInDB(subsId, TodayDate);

        PGPHelpers.modifySubscriptionDueDate(Long.valueOf(subsId), LocalDateTime.now());

        String orderId = executeRenewalAndFetchOrderId(merchant, subsId, initTxnDTO.txnAmountFromBody(), ""); //Blank in case of Default
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, orderId)).isNotNull();
        Assertions.assertThat(PGPHelpers.executeToFetchStatusInPaymentDetails(subsId, orderId)).isEqualTo("ACTIVE");
        Assertions.assertThat(PGPHelpers.getSubsDate(subsId)).contains(
                CommonHelpers.addMonths(CommonUtils.getdate("yyyy-MM-dd"), "yyyy-MM-dd", 1))
                .as("Due Date is Not updated as per Frequency Cycle");


        TxnStatus txnStatus = new TxnStatus(merchant.getId(), orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("PPBS")
                .validateMid(merchant.getId())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateRefundAmnt("0.00")
                .validateSubsid(Constants.ValidationType.NON_EMPTY)
                .validateTxnDate(new Date())
                .AssertAll();

    }


    @Parameters({"isNativePlus"})
    @Test(description = "Renewal with subscriptionAmountType: VARIABLE" +
            "Frequency Unit: DAY Payment UPI_INTENT RequestType: Subscription Default")
    public void TC_002_Renew_Subs_AmountTypeVariable_Freq_Day(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1500")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("2000")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("DAY")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_SUBSCRIPTION)
                .build();

        InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = responseDTO.getBody().getTxnToken();
        String subsId = responseDTO.getBody().getSubscriptionId();

        FetchPaymentOptResponseDTO fetchPaymentOptResponseDTO = NativeHelpers.fetchPaymentOptionResponse(txnToken, merchant.getId(), initTxnDTO.orderFromBody());
        boolean paymodeStatus = NativeHelpers.isFetchPaymentOptionStatusMatched(fetchPaymentOptResponseDTO, "UPI", false);
        Assertions.assertThat(paymodeStatus).as("UPI paymode status mismatch").isTrue();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchant.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("UPI_INTENT")
                .build();

        NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        validateTxnStatus(initTxnDTO, processTxnV1Request);

        String date = CommonUtils.getdate("dd-MM-yyyy");
        PreNotify preNotify = new PreNotify(merchant, initTxnDTO.txnAmountFromBody(), subsId, CommonHelpers.addDays(date, "dd-MM-yyyy", 1));

        Response response = preNotify.execute();

        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.status"))
                .isEqualTo("SUCCESS").as("PreNotify is Not successful for Given Dates");

        String paytmRefId = response.jsonPath().getString("body.paytmReferenceId");

        //Modify Notify dates to two days before and txn date to day
        PGPHelpers.modifySubscriptionPreNotifyStatus(Long.valueOf(paytmRefId), "1");
        PGPHelpers.modifySubscriptionPreNotifyDate(Long.valueOf(paytmRefId), LocalDateTime.now().minusDays(1));
        PGPHelpers.modifySubscriptionPreNotifyTxnDate(Long.valueOf(paytmRefId), LocalDateTime.now());

        LocalDateTime TodayDate = LocalDateTime.now().minusDays(1);
        modifySubsDatesInDB(subsId, TodayDate);

        String orderId = executeRenewalAndFetchOrderId(merchant, subsId, initTxnDTO.txnAmountFromBody(), "");
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, orderId)).isNotNull();
        Assertions.assertThat(PGPHelpers.executeToFetchStatusInPaymentDetails(subsId, orderId)).isEqualTo("ACTIVE");
        Assertions.assertThat(PGPHelpers.getSubsDate(subsId)).contains(
                CommonHelpers.addDays(CommonUtils.getdate("yyyy-MM-dd"), "yyyy-MM-dd", 1))
                .as("Due Date is Not updated as per Frequency Cycle");

    }


    @Parameters({"isNativePlus"})
    @Test(description = "Renewal with subscriptionAmountType: Variable" +
            "Frequency Unit: YEAR Payment, UPI Collect ,Request Type: Subscription_Default")
    public void TC_003_Renew_Subs_AmountTypeFix_Freq_Year(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("2000")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("2000")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("YEAR")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_SUBSCRIPTION)
                .build();

        InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);

        String txnToken = responseDTO.getBody().getTxnToken();
        String subsId = responseDTO.getBody().getSubscriptionId();

        FetchPaymentOptResponseDTO fetchPaymentOptResponseDTO = NativeHelpers.fetchPaymentOptionResponse(txnToken, merchant.getId(), initTxnDTO.orderFromBody());
        boolean paymodeStatus = NativeHelpers.isFetchPaymentOptionStatusMatched(fetchPaymentOptResponseDTO, "UPI", false);
        Assertions.assertThat(paymodeStatus).as("UPI paymode status mismatch").isTrue();

        OrderDTO orderDTO = new OrderFactory.UPIMandate(merchant, txnToken, initTxnDTO.orderFromBody(), subsId)
                .setPayerAccount("test@paytm")
                .build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        validateTxnStatus(initTxnDTO, orderDTO);

        LocalDateTime PreviousDate = LocalDateTime.now().minusYears(1).plusDays(2);
        PGPHelpers.modifySubscriptionDueDate(Long.valueOf(subsId), LocalDateTime.now().plusDays(2));
        modifySubsDatesInDB(subsId, PreviousDate);


        PreNotify preNotify = new PreNotify(merchant, initTxnDTO.txnAmountFromBody(), subsId);
        Response response = preNotify.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.status"))
                .isEqualTo("SUCCESS").as("PreNotify is Not successful for Given Dates");

        String paytmRefId = response.jsonPath().getString("body.paytmReferenceId");
        modifyNotifyDatesInDB(paytmRefId);

        LocalDateTime frequencyReqDate = LocalDateTime.now().minusYears(1);

        PGPHelpers.modifySubscriptionDueDate(Long.valueOf(subsId), LocalDateTime.now());

        modifySubsDatesInDB(subsId, frequencyReqDate);

        String orderId = executeRenewalAndFetchOrderId(merchant, subsId, initTxnDTO.txnAmountFromBody(), "");
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, orderId)).isNotNull();
        Assertions.assertThat(PGPHelpers.executeToFetchStatusInPaymentDetails(subsId, orderId)).isEqualTo("ACTIVE");
        Assertions.assertThat(PGPHelpers.getSubsDate(subsId)).contains(
                CommonHelpers.addYears(CommonUtils.getdate("yyyy-MM-dd"), "yyyy-MM-dd", 1))
                .as("Due Date is Not updated as per Frequency Cycle");

    }


    @Parameters({"isNativePlus"})
    @Test(description = "Renewal with subscriptionAmountType: VARIABLE" +
            "Frequency Unit: WEEK Payment, UPI Intent ,Request Type:Subscription_Default ")
    public void TC_004_Renew_Subs_AmountTypeVariable_Freq_Week(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1999")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("2000")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("WEEK")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_SUBSCRIPTION)
                .build();

        InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);

        String txnToken = responseDTO.getBody().getTxnToken();
        String subsId = responseDTO.getBody().getSubscriptionId();

        FetchPaymentOptResponseDTO fetchPaymentOptResponseDTO = NativeHelpers.fetchPaymentOptionResponse(txnToken, merchant.getId(), initTxnDTO.orderFromBody());
        boolean paymodeStatus = NativeHelpers.isFetchPaymentOptionStatusMatched(fetchPaymentOptResponseDTO, "UPI", false);
        Assertions.assertThat(paymodeStatus).as("UPI paymode status mismatch").isTrue();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchant.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("UPI_INTENT")
                .build();

        NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        validateTxnStatus(initTxnDTO, processTxnV1Request);

        LocalDateTime PreviousDate = LocalDateTime.now().minusDays(7).plusDays(2);

        PGPHelpers.modifySubscriptionDueDate(Long.valueOf(subsId), LocalDateTime.now().plusDays(2));
        modifySubsDatesInDB(subsId, PreviousDate);


        PreNotify preNotify = new PreNotify(merchant, initTxnDTO.txnAmountFromBody(), subsId);
        Response response = preNotify.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.status"))
                .isEqualTo("SUCCESS").as("PreNotify is Not successful for Given Dates");

        String paytmRefId = response.jsonPath().getString("body.paytmReferenceId");

        modifyNotifyDatesInDB(paytmRefId);

        LocalDateTime frequencyReqDate = LocalDateTime.now().minusDays(7);
        modifySubsDatesInDB(subsId, frequencyReqDate);

        PGPHelpers.modifySubscriptionDueDate(Long.valueOf(subsId), LocalDateTime.now());
        String orderId = executeRenewalAndFetchOrderId(merchant, subsId, initTxnDTO.txnAmountFromBody(), "");
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, orderId)).isNotNull();
        Assertions.assertThat(PGPHelpers.executeToFetchStatusInPaymentDetails(subsId, orderId)).isEqualTo("ACTIVE");
        Assertions.assertThat(PGPHelpers.getSubsDate(subsId))
                .contains(CommonHelpers.addDays(CommonUtils.getdate("yyyy-MM-dd"), "yyyy-MM-dd", 7))
                .as("Due Date is Not updated as per Frequency Cycle");

    }


    @Parameters({"isNativePlus"})
    @Test(description = "Renewal with subscriptionAmountType: FIX" +
            "Frequency Unit: QUARTER Payment, UPI INTENT ,Request Type:Subscription_Default ")
    public void TC_005_Renew_Subs_AmountTypeFix_Freq_Quarter(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_NATIVE_SUBS;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("100")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("100")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("QUARTER")
                .setSubscriptionGraceDays("3")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_SUBSCRIPTION)
                .build();

        InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);

        String txnToken = responseDTO.getBody().getTxnToken();
        String subsId = responseDTO.getBody().getSubscriptionId();

        FetchPaymentOptResponseDTO fetchPaymentOptResponseDTO = NativeHelpers.fetchPaymentOptionResponse(txnToken, merchant.getId(), initTxnDTO.orderFromBody());
        boolean paymodeStatus = NativeHelpers.isFetchPaymentOptionStatusMatched(fetchPaymentOptResponseDTO, "UPI", false);
        Assertions.assertThat(paymodeStatus).as("UPI paymode status mismatch").isTrue();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchant.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("UPI_INTENT")
                .build();

        NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        validateTxnStatus(initTxnDTO, processTxnV1Request);


        LocalDateTime PreviousDate = LocalDateTime.now().minusMonths(3).plusDays(2);
        PGPHelpers.modifySubscriptionDueDate(Long.valueOf(subsId), LocalDateTime.now().plusDays(2));
        modifySubsDatesInDB(subsId, PreviousDate);


        PreNotify preNotify = new PreNotify(merchant, initTxnDTO.txnAmountFromBody(), subsId);
        Response response = preNotify.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.status"))
                .isEqualTo("SUCCESS").as("PreNotify is Not successful for Given Dates");

        String paytmRefId = response.jsonPath().getString("body.paytmReferenceId");
        modifyNotifyDatesInDB(paytmRefId);

        LocalDateTime frequencyReqDate = LocalDateTime.now().minusMonths(3);
        modifySubsDatesInDB(subsId, frequencyReqDate);

        PGPHelpers.modifySubscriptionDueDate(Long.valueOf(subsId), LocalDateTime.now());

        String orderId = executeRenewalAndFetchOrderId(merchant, subsId, initTxnDTO.txnAmountFromBody(), "");
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, orderId)).isNotNull();
        Assertions.assertThat(PGPHelpers.executeToFetchStatusInPaymentDetails(subsId, orderId)).isEqualTo("ACTIVE");
        Assertions.assertThat(PGPHelpers.getSubsDate(subsId)).contains(
                CommonHelpers.addMonths(CommonUtils.getdate("yyyy-MM-dd"), "yyyy-MM-dd", 3))
                .as("Due Date is Not updated as per Frequency Cycle");
    }


    @Parameters({"isNativePlus"})
    @Test(description = "Renewal with subscriptionAmountType: VARIABLE" +
            "Frequency Unit: BI_MONTHLY Payment, UPI Collect ,Request Type:Subscription_Default ")
    public void TC_006_Renew_Subs_AmountTypeVariable_Freq_BiMonthly(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("1500")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("BI_MONTHLY")
                .setSubscriptionGraceDays("5")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_SUBSCRIPTION)
                .build();

        InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);

        String txnToken = responseDTO.getBody().getTxnToken();
        String subsId = responseDTO.getBody().getSubscriptionId();

        FetchPaymentOptResponseDTO fetchPaymentOptResponseDTO = NativeHelpers.fetchPaymentOptionResponse(txnToken, merchant.getId(), initTxnDTO.orderFromBody());
        boolean paymodeStatus = NativeHelpers.isFetchPaymentOptionStatusMatched(fetchPaymentOptResponseDTO, "UPI", false);
        Assertions.assertThat(paymodeStatus).as("UPI paymode status mismatch").isTrue();

        OrderDTO orderDTO = new OrderFactory.UPIMandate(merchant, txnToken, initTxnDTO.orderFromBody(), subsId)
                .setPayerAccount("test@paytm")
                .build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        validateTxnStatus(initTxnDTO, orderDTO);

        LocalDateTime PreviousDate = LocalDateTime.now().minusMonths(2).plusDays(2);
        PGPHelpers.modifySubscriptionDueDate(Long.valueOf(subsId), LocalDateTime.now().plusDays(2));
        modifySubsDatesInDB(subsId, PreviousDate);


        PreNotify preNotify = new PreNotify(merchant, initTxnDTO.txnAmountFromBody(), subsId);
        Response response = preNotify.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.status"))
                .isEqualTo("SUCCESS").as("PreNotify is Not Successful for Given Dates");

        String paytmRefId = response.jsonPath().getString("body.paytmReferenceId");
        modifyNotifyDatesInDB(paytmRefId);

        LocalDateTime frequencyReqDate = LocalDateTime.now().minusMonths(2);
        modifySubsDatesInDB(subsId, frequencyReqDate);
        PGPHelpers.modifySubscriptionDueDate(Long.valueOf(subsId), LocalDateTime.now());

        String orderId = executeRenewalAndFetchOrderId(merchant, subsId, initTxnDTO.txnAmountFromBody(), "");
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, orderId)).isNotNull();
        Assertions.assertThat(PGPHelpers.executeToFetchStatusInPaymentDetails(subsId, orderId)).isEqualTo("ACTIVE");
        Assertions.assertThat(PGPHelpers.getSubsDate(subsId)).contains(
                CommonHelpers.addMonths(CommonUtils.getdate("yyyy-MM-dd"), "yyyy-MM-dd", 2))
                .as("Due Date is Not updated as per Frequency Cycle");

    }


    @Parameters({"isNativePlus"})
    @Test(description = "Renewal with subscriptionAmountType: Variable" +
            "Frequency Unit: SEMI_ANNUALLY Payment, UPI INTENT ,Request Type:Subscription_Default ")
    public void TC_007_Renew_Subs_AmountTypeVariable_Freq_SemiAnnually(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("2000")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("2000")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("SEMI_ANNUALLY")
                .setSubscriptionGraceDays("5")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_SUBSCRIPTION)
                .build();

        InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);

        String txnToken = responseDTO.getBody().getTxnToken();
        String subsId = responseDTO.getBody().getSubscriptionId();

        FetchPaymentOptResponseDTO fetchPaymentOptResponseDTO = NativeHelpers.fetchPaymentOptionResponse(txnToken, merchant.getId(), initTxnDTO.orderFromBody());
        boolean paymodeStatus = NativeHelpers.isFetchPaymentOptionStatusMatched(fetchPaymentOptResponseDTO, "UPI", false);
        Assertions.assertThat(paymodeStatus).as("UPI paymode status mismatch").isTrue();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchant.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("UPI_INTENT")
                .build();

        NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        validateTxnStatus(initTxnDTO, processTxnV1Request);

        LocalDateTime PreviousDate = LocalDateTime.now().minusMonths(6).plusDays(2);
        PGPHelpers.modifySubscriptionDueDate(Long.valueOf(subsId), LocalDateTime.now().plusDays(2));
        modifySubsDatesInDB(subsId, PreviousDate);


        PreNotify preNotify = new PreNotify(merchant, initTxnDTO.txnAmountFromBody(), subsId);
        Response response = preNotify.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.status"))
                .isEqualTo("SUCCESS").as("PreNotify is Not Successful for Given Dates");

        String paytmRefId = response.jsonPath().getString("body.paytmReferenceId");
        modifyNotifyDatesInDB(paytmRefId);

        LocalDateTime frequencyReqDate = LocalDateTime.now().minusMonths(6);
        modifySubsDatesInDB(subsId, frequencyReqDate);
        PGPHelpers.modifySubscriptionDueDate(Long.valueOf(subsId), LocalDateTime.now());

        String orderId = executeRenewalAndFetchOrderId(merchant, subsId, initTxnDTO.txnAmountFromBody(), "");
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, orderId)).isNotNull();
        Assertions.assertThat(PGPHelpers.executeToFetchStatusInPaymentDetails(subsId, orderId)).isEqualTo("ACTIVE");
        Assertions.assertThat(PGPHelpers.getSubsDate(subsId)).contains(
                CommonHelpers.addMonths(CommonUtils.getdate("yyyy-MM-dd"), "yyyy-MM-dd", 6))
                .as("Due Date is Not updated as per Frequency Cycle");
    }


//Multiple Renewal with same SubsID


    @Parameters({"isNativePlus"})
    @Test(description = "Multiple Renewal on Same Subscription" +
            "Frequency Unit: Month, UPI INTENT ,Request Type:Subscription_Default ")
    public void TC_001_Renew_Subs_MultipleRenewal(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("1000")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("5")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_SUBSCRIPTION)
                .build();

        InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);

        String txnToken = responseDTO.getBody().getTxnToken();
        String subsId = responseDTO.getBody().getSubscriptionId();

        FetchPaymentOptResponseDTO fetchPaymentOptResponseDTO = NativeHelpers.fetchPaymentOptionResponse(txnToken, merchant.getId(), initTxnDTO.orderFromBody());
        boolean paymodeStatus = NativeHelpers.isFetchPaymentOptionStatusMatched(fetchPaymentOptResponseDTO, "UPI", false);
        Assertions.assertThat(paymodeStatus).as("UPI paymode status mismatch").isTrue();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchant.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("UPI_INTENT")
                .build();

        NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        validateTxnStatus(initTxnDTO, processTxnV1Request);


        //Modify Dates to previous month and two days after to send preNotify

        LocalDateTime PreviousDate = LocalDateTime.now().minusMonths(1).plusDays(2);
        PGPHelpers.modifySubscriptionDueDate(Long.valueOf(subsId), LocalDateTime.now().plusDays(2));
        modifySubsDatesInDB(subsId, PreviousDate);

        //PreNotify sent
        PreNotify preNotify = new PreNotify(merchant, initTxnDTO.txnAmountFromBody(), subsId);
        Response response = preNotify.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.status"))
                .isEqualTo("SUCCESS").as("PreNotify is Not Successful for Given Dates");

        String paytmRefId = response.jsonPath().getString("body.paytmReferenceId");
        modifyNotifyDatesInDB(paytmRefId);

        //Modify dates to previous month to execute renew now
        LocalDateTime TodayDate = LocalDateTime.now().minusMonths(1);
        modifySubsDatesInDB(subsId, TodayDate);

        PGPHelpers.modifySubscriptionDueDate(Long.valueOf(subsId), LocalDateTime.now());

        String orderId = executeRenewalAndFetchOrderId(merchant, subsId, initTxnDTO.txnAmountFromBody(), "");
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, orderId)).isNotNull();
        Assertions.assertThat(PGPHelpers.executeToFetchStatusInPaymentDetails(subsId, orderId)).isEqualTo("ACTIVE");
        Assertions.assertThat(PGPHelpers.getSubsDate(subsId)).contains(
                CommonHelpers.addMonths(CommonUtils.getdate("yyyy-MM-dd"), "yyyy-MM-dd", 1))
                .as("Due Date is Not updated as per Frequency Cycle");


        //Send Second Renewal for same subs ID

        LocalDateTime PreviousDateSecRenew = LocalDateTime.now().minusMonths(2).plusDays(2);
        PGPHelpers.modifySubscriptionDueDate(Long.valueOf(subsId), LocalDateTime.now().plusDays(2));


        modifySubsDatesInDB(subsId, PreviousDateSecRenew);


        PGPHelpers.modifySubscriptionPaymentCreateDate(Long.valueOf(subsId), LocalDateTime.now().minusMonths(1).plusDays(2), "RENEWAL");
        PGPHelpers.modifySubscriptionPaymentUpdateDate(Long.valueOf(subsId), LocalDateTime.now().minusMonths(1).plusDays(2), "RENEWAL");

        //PreNotify sent
        PreNotify preNotifySecondRenew = new PreNotify(merchant, initTxnDTO.txnAmountFromBody(), subsId);
        Response responseSecondRenew = preNotifySecondRenew.execute();
        Assertions.assertThat(responseSecondRenew.jsonPath().getString("body.resultInfo.status"))
                .isEqualTo("SUCCESS").as("PreNotify is Not Successful for Given Dates");

        String paytmRefIdSecRenew = responseSecondRenew.jsonPath().getString("body.paytmReferenceId");
        modifyNotifyDatesInDB(paytmRefIdSecRenew);

        //Modify dates to previous month to execute renew now
        LocalDateTime TodayDate2Renew = LocalDateTime.now().minusMonths(2);
        modifySubsDatesInDB(subsId, TodayDate2Renew);

        PGPHelpers.modifySubscriptionPaymentCreateDate(Long.valueOf(subsId), LocalDateTime.now().minusMonths(1), "RENEWAL");
        PGPHelpers.modifySubscriptionPaymentUpdateDate(Long.valueOf(subsId), LocalDateTime.now().minusMonths(1), "RENEWAL");

        PGPHelpers.modifySubscriptionDueDate(Long.valueOf(subsId), LocalDateTime.now());

        String orderId2 = executeRenewalAndFetchOrderId(merchant, subsId, initTxnDTO.txnAmountFromBody(), "");
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, orderId2)).isNotNull();
        Assertions.assertThat(PGPHelpers.executeToFetchStatusInPaymentDetails(subsId, orderId2)).isEqualTo("ACTIVE");
        Assertions.assertThat(PGPHelpers.getSubsDate(subsId)).contains(
                CommonHelpers.addMonths(CommonUtils.getdate("yyyy-MM-dd"), "yyyy-MM-dd", 1))
                .as("Due Date is Not updated as per Frequency Cycle");
    }


//GRACE PERIOD CHECK

    @Parameters({"isNativePlus"})
    @Test(description = "Multiple Renewal on Same Subscription" +
            "Frequency Unit: Day, UPI INTENT ,Request Type:Subscription_Default ")
    public void TC_001_Renew_Subs_WithInGracePeriod(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_NATIVE_SUBS;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("1000")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("YEAR")
                .setSubscriptionGraceDays("350")                            //Grace Period
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_SUBSCRIPTION)
                .build();

        InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);

        String txnToken = responseDTO.getBody().getTxnToken();
        String subsId = responseDTO.getBody().getSubscriptionId();

        FetchPaymentOptResponseDTO fetchPaymentOptResponseDTO = NativeHelpers.fetchPaymentOptionResponse(txnToken, merchant.getId(), initTxnDTO.orderFromBody());
        boolean paymodeStatus = NativeHelpers.isFetchPaymentOptionStatusMatched(fetchPaymentOptResponseDTO, "UPI", false);
        Assertions.assertThat(paymodeStatus).as("UPI paymode status mismatch").isTrue();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchant.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("UPI_INTENT")
                .build();

        NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        validateTxnStatus(initTxnDTO, processTxnV1Request);


        //Modify Dates to previous month and two days after to send preNotify

        LocalDateTime PreviousDate = LocalDateTime.now().minusMonths(1).plusDays(2);                //frequency is within
        PGPHelpers.modifySubscriptionDueDate(Long.valueOf(subsId), LocalDateTime.now().plusDays(2));
        modifySubsDatesInDB(subsId, PreviousDate);

        //PreNotify sent
        PreNotify preNotify = new PreNotify(merchant, initTxnDTO.txnAmountFromBody(), subsId);
        Response response = preNotify.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.status"))
                .isEqualTo("SUCCESS").as("PreNotify is Not Successfull for Given Dates");

        String paytmRefId = response.jsonPath().getString("body.paytmReferenceId");
        modifyNotifyDatesInDB(paytmRefId);

        //Modify dates to previous month to execute renew now
        LocalDateTime TodayDate = LocalDateTime.now().minusMonths(1);
        modifySubsDatesInDB(subsId, TodayDate);

        PGPHelpers.modifySubscriptionDueDate(Long.valueOf(subsId), LocalDateTime.now());

        String orderId = executeRenewalAndFetchOrderId(merchant, subsId, initTxnDTO.txnAmountFromBody(), "");
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, orderId)).isNotNull();
        Assertions.assertThat(PGPHelpers.executeToFetchStatusInPaymentDetails(subsId, orderId)).isEqualTo("ACTIVE");
        Assertions.assertThat(PGPHelpers.getSubsDate(subsId)).contains(
                CommonHelpers.addYears(CommonUtils.getdate("yyyy-MM-dd"), "yyyy-MM-dd", 1))
                .as("Due Date is Not updated as per Frequency Cycle");


    }


    //different Amount is Send for Renewal

    @Parameters({"isNativePlus"})
    @Test(description = "Renewal with subscriptionAmountType: FIX" +
            "Frequency Unit: Month Payment UPI Intent RequestType: Subscription_Default")
    public void TC_001_Renew_Subs_Failed_DifferentAmountSent(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_MUTUAL_MF;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("100")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("100")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("DAY")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_MF_SIP)
                .build();

        InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = responseDTO.getBody().getTxnToken();
        String subsId = responseDTO.getBody().getSubscriptionId();

        FetchPaymentOptResponseDTO fetchPaymentOptResponseDTO = NativeHelpers.fetchPaymentOptionResponse(txnToken, merchant.getId(), initTxnDTO.orderFromBody());
        boolean paymodeStatus = NativeHelpers.isFetchPaymentOptionStatusMatched(fetchPaymentOptResponseDTO, "UPI", false);
        Assertions.assertThat(paymodeStatus).as("UPI paymode status mismatch").isTrue();


        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchant.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("UPI_INTENT")
                .build();

        NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        validateTxnStatus(initTxnDTO, processTxnV1Request);


        //Modify Dates to previous month and two days after to send preNotify

        LocalDateTime PreviousDate = LocalDateTime.now().minusMonths(1).plusDays(2);
        PGPHelpers.modifySubscriptionDueDate(Long.valueOf(subsId), LocalDateTime.now().plusDays(2));
        modifySubsDatesInDB(subsId, PreviousDate);

        //PreNotify sent
        PreNotify preNotify = new PreNotify(merchant, initTxnDTO.txnAmountFromBody(), subsId);
        Response response = preNotify.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.status"))
                .isEqualTo("SUCCESS").as("PreNotify is Not Successfull for Given Dates");

        String paytmRefId = response.jsonPath().getString("body.paytmReferenceId");
        modifyNotifyDatesInDB(paytmRefId);

        //Modify dates to previous month to execute renew now
        LocalDateTime TodayDate = LocalDateTime.now().minusMonths(1);
        modifySubsDatesInDB(subsId, TodayDate);

        PGPHelpers.modifySubscriptionDueDate(Long.valueOf(subsId), LocalDateTime.now());

        RenewSubscriptionDTO renewSubscriptionDTO = new RenewSubscriptionDTO.Builder
                (merchant.getId(), subsId, "12")
                .setRequestType(NATIVE_RENEW_MF_SIP)
                .setMerchantKey(merchant.getKey())
                .build();
        RenewSubscription renewSubscription = new RenewSubscription(renewSubscriptionDTO);
        JsonPath jsonPath = renewSubscription.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg"))
                .as("Result Message mismatch").isEqualTo("Payment request amount does not match the Subscription contract");
    }


//Refund of renewal
    // Refund of UPI is not configuired
//    @Parameters({"isNativePlus"})
//    @Test(description = "Renewal with subscriptionAmountType: FIX" +
//            "Frequency Unit: Month Payment UPI Collect RequestType: SUBS_RENEWAL_MF_SIP", enabled = false)
    public void TC_001_Renew_MF_Subs_Refund_Freq_Month(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_MUTUAL_MF;


        prerequisite:
        {

            PGPHelpers.validateRefundAllowedWithChecksum(merchant.getId());
        }


        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_MF_SIP)
                .build();

        InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = responseDTO.getBody().getTxnToken();
        String subsId = responseDTO.getBody().getSubscriptionId();

        FetchPaymentOptResponseDTO fetchPaymentOptResponseDTO = NativeHelpers.fetchPaymentOptionResponse(txnToken, merchant.getId(), initTxnDTO.orderFromBody());
        boolean paymodeStatus = NativeHelpers.isFetchPaymentOptionStatusMatched(fetchPaymentOptResponseDTO, "UPI", false);
        Assertions.assertThat(paymodeStatus).as("UPI paymode status mismatch").isTrue();

        OrderDTO orderDTO = new OrderFactory.UPIMandate(merchant, txnToken, initTxnDTO.orderFromBody(), subsId)
                .setPayerAccount("test@paytm")
                .build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        validateTxnStatus(initTxnDTO, orderDTO);

        //Modify Dates to previous month and two days after to send preNotify

        LocalDateTime PreviousDate = LocalDateTime.now().minusMonths(1).plusDays(2);
        PGPHelpers.modifySubscriptionDueDate(Long.valueOf(subsId), LocalDateTime.now().plusDays(2));
        modifySubsDatesInDB(subsId, PreviousDate);

        //PreNotify sent
        PreNotify preNotify = new PreNotify(merchant, initTxnDTO.txnAmountFromBody(), subsId);
        Response response = preNotify.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.status"))
                .isEqualTo("SUCCESS").as("PreNotify is Not Successfull for Given Dates");

        String paytmRefId = response.jsonPath().getString("body.paytmReferenceId");
        modifyNotifyDatesInDB(paytmRefId);

        //Modify dates to previous month to execute renew now
        LocalDateTime TodayDate = LocalDateTime.now().minusMonths(1);
        modifySubsDatesInDB(subsId, TodayDate);

        PGPHelpers.modifySubscriptionDueDate(Long.valueOf(subsId), LocalDateTime.now());

        String orderId = executeRenewalAndFetchOrderId(merchant, subsId, initTxnDTO.txnAmountFromBody(), NATIVE_RENEW_MF_SIP);
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, orderId)).isNotNull();
        Assertions.assertThat(PGPHelpers.executeToFetchStatusInPaymentDetails(subsId, orderId)).isEqualTo("ACTIVE");
        Assertions.assertThat(PGPHelpers.getSubsDate(subsId)).contains(
                CommonHelpers.addMonths(CommonUtils.getdate("yyyy-MM-dd"), "yyyy-MM-dd", 1))
                .as("Due Date is Not updated as per Frequency Cycle");

        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();

        Test:
        {
            PGPHelpers.initiateRefundRequest(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT(), txnStatus.getResponse().getTXNID(), "");
            PGPHelpers.getRefundStatus(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), true)
                    .validateStatus("TXN_SUCCESS", 0)
                    .assertAll();
        }
    }


    //PreNotify Test cases


    //Invalid Cycle is Passed in PreNotify


    @Parameters({"isNativePlus"})
    @Test(description = "Renewal with subscriptionAmountType: FIX" +
            "Frequency Unit: Month Payment UPI Collect RequestType: SUBS_RENEWAL_MF_SIP")
    public void TC_001_Prenotify_Invalid_Cycle(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_MUTUAL_MF;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_MF_SIP)
                .build();

        InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = responseDTO.getBody().getTxnToken();
        String subsId = responseDTO.getBody().getSubscriptionId();

        FetchPaymentOptResponseDTO fetchPaymentOptResponseDTO = NativeHelpers.fetchPaymentOptionResponse(txnToken, merchant.getId(), initTxnDTO.orderFromBody());
        boolean paymodeStatus = NativeHelpers.isFetchPaymentOptionStatusMatched(fetchPaymentOptResponseDTO, "UPI", false);
        Assertions.assertThat(paymodeStatus).as("UPI paymode status mismatch").isTrue();

        OrderDTO orderDTO = new OrderFactory.UPIMandate(merchant, txnToken, initTxnDTO.orderFromBody(), subsId)
                .setPayerAccount("test@paytm")
                .build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        validateTxnStatus(initTxnDTO, orderDTO);

        //PreNotify sent
        PreNotify preNotify = new PreNotify(merchant, initTxnDTO.txnAmountFromBody(), subsId);
        Response response = preNotify.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.status"))
                .isEqualTo("FAILURE").as("PreNotify is Successful for future cycles");

        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.message"))
                .isEqualTo("Payment Request date cannot be for future frequency cycles")
                .as("PreNotify is Successfull for future cycles");


    }


    //Payment Requesting again with Success Renewal


    @Parameters({"isNativePlus"})
    @Test(description = "Renewal with subscriptionAmountType: FIX" +
            "Frequency Unit: Month Payment UPI Collect RequestType: SUBS_RENEWAL_MF_SIP")
    public void TC_002_Prenotify_Past_Date(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_NATIVE_SUBS;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("100")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("YEAR")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_SUBSCRIPTION)
                .build();

        InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = responseDTO.getBody().getTxnToken();
        String subsId = responseDTO.getBody().getSubscriptionId();

        FetchPaymentOptResponseDTO fetchPaymentOptResponseDTO = NativeHelpers.fetchPaymentOptionResponse(txnToken, merchant.getId(), initTxnDTO.orderFromBody());
        boolean paymodeStatus = NativeHelpers.isFetchPaymentOptionStatusMatched(fetchPaymentOptResponseDTO, "UPI", false);
        Assertions.assertThat(paymodeStatus).as("UPI paymode status mismatch").isTrue();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchant.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("UPI_INTENT")
                .build();

        NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        validateTxnStatus(initTxnDTO, processTxnV1Request);

        //PreNotify sent
        String date = CommonUtils.getdate("dd-MM-yyyy");
        PreNotify preNotify = new PreNotify(merchant, initTxnDTO.txnAmountFromBody(), subsId, CommonHelpers.subtractDays(date, "dd-MM-yyyy", 1));
        Response response = preNotify.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.status"))
                .isEqualTo("FAILURE").as("PreNotify is Successful for Past Date");

        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.message"))
                .isEqualTo("Invalid txnDate : txnDate is in Past")
                .as("PreNotify is Successful for Past Date");


    }
    //Payment request is already pending in this frequency cycle

    @Parameters({"isNativePlus"})
    @Test(description = "Payment request is already pending in this frequency cycle for Renewal with subscriptionAmountType: FIX" +
            "Frequency Unit: Month Payment UPI Collect RequestType: SUBS_RENEWAL_MF_SIP")
    public void TC_001_Renew_PaymentRequestAlreadyPendingForFrequencyCycle(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.MF_SIP_NEW_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_MF_SIP)
                .build();

        InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = responseDTO.getBody().getTxnToken();
        String subsId = responseDTO.getBody().getSubscriptionId();

        FetchPaymentOptResponseDTO fetchPaymentOptResponseDTO = NativeHelpers.fetchPaymentOptionResponse(txnToken, merchant.getId(), initTxnDTO.orderFromBody());
        boolean paymodeStatus = NativeHelpers.isFetchPaymentOptionStatusMatched(fetchPaymentOptResponseDTO, "UPI", false);
        Assertions.assertThat(paymodeStatus).as("UPI paymode status mismatch").isTrue();

        OrderDTO orderDTO = new OrderFactory.UPIMandate(merchant, txnToken, initTxnDTO.orderFromBody(), subsId)
                .setPayerAccount("test@paytm")
                .build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        validateTxnStatus(initTxnDTO, orderDTO);

        //Modify Dates to previous month and two days after to send preNotify

        LocalDateTime PreviousDate = LocalDateTime.now().minusMonths(1).plusDays(2);
        PGPHelpers.modifySubscriptionDueDate(Long.valueOf(subsId), LocalDateTime.now().plusDays(2));
        modifySubsDatesInDB(subsId, PreviousDate);

        //PreNotify sent
        PreNotify preNotify = new PreNotify(merchant, initTxnDTO.txnAmountFromBody(), subsId);
        Response response = preNotify.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.status"))
                .isEqualTo("SUCCESS").as("PreNotify is Not Successfull for Given Dates");

        String paytmRefId = response.jsonPath().getString("body.paytmReferenceId");
        modifyNotifyDatesInDB(paytmRefId);

        //Modify dates to previous month to execute renew now
        LocalDateTime TodayDate = LocalDateTime.now().minusMonths(1);
        modifySubsDatesInDB(subsId, TodayDate);

        PGPHelpers.modifySubscriptionDueDate(Long.valueOf(subsId), LocalDateTime.now());


        String orderId = executeRenewalAndFetchOrderId(merchant, subsId, initTxnDTO.txnAmountFromBody(), NATIVE_RENEW_MF_SIP);

        RenewSubscriptionDTO renewSubscriptionDTO2 = new RenewSubscriptionDTO.Builder
                (merchant.getId(), subsId, initTxnDTO.txnAmountFromBody())
                .setRequestType(NATIVE_RENEW_MF_SIP)
                .setOrderId(orderId)
                .setMerchantKey(merchant.getKey())
                .build();
        RenewSubscription renewSubscription2 = new RenewSubscription(renewSubscriptionDTO2);
        Response renewResponse = renewSubscription2.execute();

        Assertions.assertThat(renewResponse.jsonPath().getString("body.resultInfo.resultStatus"))
                .isEqualTo("F").as("Renew Again is getting Success");

        Assertions.assertThat(renewResponse.jsonPath().getString("body.resultInfo.resultMsg"))
                .isEqualTo("Payment request is already in progress in this frequency cycle")
                .as("Renewal again is getting Success");


    }

    //Failure Renew


    @Parameters({"isNativePlus"})
    @Test(description = "Renewal Failure with subscriptionAmountType: VARIABLE" +
            "Frequency Unit: Month Payment UPI Collect RequestType: SUBS_RENEWAL_MF_SIP")
    public void TC_001_Renew_Failure_Subs_UPIIntent(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_NATIVE_SUBS;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("99.99")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("1000")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_SUBSCRIPTION)
                .build();

        InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = responseDTO.getBody().getTxnToken();
        String subsId = responseDTO.getBody().getSubscriptionId();

        FetchPaymentOptResponseDTO fetchPaymentOptResponseDTO = NativeHelpers.fetchPaymentOptionResponse(txnToken, merchant.getId(), initTxnDTO.orderFromBody());
        boolean paymodeStatus = NativeHelpers.isFetchPaymentOptionStatusMatched(fetchPaymentOptResponseDTO, "UPI", false);
        Assertions.assertThat(paymodeStatus).as("UPI paymode status mismatch").isTrue();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchant.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("UPI_INTENT")
                .build();

        NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        validateTxnStatus(initTxnDTO, processTxnV1Request);

        //Modify Dates to previous month and two days after to send preNotify

        LocalDateTime PreviousDate = LocalDateTime.now().minusMonths(1).plusDays(2);
        PGPHelpers.modifySubscriptionDueDate(Long.valueOf(subsId), LocalDateTime.now().plusDays(2));
        modifySubsDatesInDB(subsId, PreviousDate);

        //PreNotify sent
        PreNotify preNotify = new PreNotify(merchant, initTxnDTO.txnAmountFromBody(), subsId);
        Response response = preNotify.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.status"))
                .isEqualTo("SUCCESS").as("PreNotify is Not Successful for Given Dates");

        String paytmRefId = response.jsonPath().getString("body.paytmReferenceId");
        modifyNotifyDatesInDB(paytmRefId);

        //Modify dates to previous month to execute renew now
        LocalDateTime TodayDate = LocalDateTime.now().minusMonths(1);
        modifySubsDatesInDB(subsId, TodayDate);

        PGPHelpers.modifySubscriptionDueDate(Long.valueOf(subsId), LocalDateTime.now());

        String orderId = executeRenewalAndFetchOrderId(merchant, subsId, initTxnDTO.txnAmountFromBody(), "");
        TxnStatus txnStatus = new TxnStatus(merchant.getId(), orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_FAILURE")
                .validateTxnType("SALE")
                .validateGatewayName("PPBS")
                .validateMid(merchant.getId())
                .validatePaymentMode("UPI")
                .validateRespCode("227")
                .validateRefundAmnt("0.00")
                .validateSubsid(Constants.ValidationType.NON_EMPTY)
                .validateTxnDate(new Date())
                .AssertAll();

    }


    //Payment renew request as per Frequency

    @Parameters({"isNativePlus"})
    @Test(description = "Renewal with subscriptionAmountType: VARIABLE" +
            "Frequency Unit: Month Payment UPI Collect RequestType: SUBS_RENEWAL_MF_SIP")
    public void TC_001_Renew_Failure_Subs_NotPerFrequency(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_MUTUAL_MF;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("99.99")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("1000")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("YEAR")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_MF_SIP)
                .build();

        InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = responseDTO.getBody().getTxnToken();
        String subsId = responseDTO.getBody().getSubscriptionId();

        FetchPaymentOptResponseDTO fetchPaymentOptResponseDTO = NativeHelpers.fetchPaymentOptionResponse(txnToken, merchant.getId(), initTxnDTO.orderFromBody());
        boolean paymodeStatus = NativeHelpers.isFetchPaymentOptionStatusMatched(fetchPaymentOptResponseDTO, "UPI", false);
        Assertions.assertThat(paymodeStatus).as("UPI paymode status mismatch").isTrue();

        OrderDTO orderDTO = new OrderFactory.UPIMandate(merchant, txnToken, initTxnDTO.orderFromBody(), subsId)
                .setPayerAccount("test@paytm")
                .build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        validateTxnStatus(initTxnDTO, orderDTO);

        //Modify Dates to previous month and two days after to send preNotify

        LocalDateTime PreviousDate = LocalDateTime.now().minusMonths(1).plusDays(2);
        PGPHelpers.modifySubscriptionDueDate(Long.valueOf(subsId), LocalDateTime.now().plusDays(2));
        modifySubsDatesInDB(subsId, PreviousDate);

        //PreNotify sent
        PreNotify preNotify = new PreNotify(merchant, initTxnDTO.txnAmountFromBody(), subsId);
        Response response = preNotify.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.status"))
                .isEqualTo("SUCCESS").as("PreNotify is Not Successful for Given Dates");

        String paytmRefId = response.jsonPath().getString("body.paytmReferenceId");
        modifyNotifyDatesInDB(paytmRefId);

        //Modify dates to previous month to execute renew now
        LocalDateTime TodayDate = LocalDateTime.now().minusMonths(1);
        modifySubsDatesInDB(subsId, TodayDate);

        PGPHelpers.modifySubscriptionDueDate(Long.valueOf(subsId), LocalDateTime.now());

        RenewSubscriptionDTO renewSubscriptionDTO = new RenewSubscriptionDTO.Builder
                (merchant.getId(), subsId, initTxnDTO.txnAmountFromBody())
                .setRequestType(NATIVE_RENEW_MF_SIP)
                .setMerchantKey(merchant.getKey())
                .build();
        RenewSubscription renewSubscription = new RenewSubscription(renewSubscriptionDTO);
        JsonPath jsonPath = renewSubscription.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg"))
                .as("Result Message mismatch").isEqualTo("Payment Request date is not as per the subscription contract");

    }



    //Retry Count Breach


    @Parameters({"isNativePlus"})
    @Test(description = "Retry Breach for Renewal with subscriptionAmountType: Variable" +
            "Frequency Unit: Month Payment UPI Collect RequestType: SUBS_RENEWAL_MF_SIP")
    public void TC_001_Renew_RetryCountBreach(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_MUTUAL_MF;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("100")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("DAY")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_MF_SIP)
                .build();

        InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = responseDTO.getBody().getTxnToken();
        String subsId = responseDTO.getBody().getSubscriptionId();

        FetchPaymentOptResponseDTO fetchPaymentOptResponseDTO = NativeHelpers.fetchPaymentOptionResponse(txnToken, merchant.getId(), initTxnDTO.orderFromBody());
        boolean paymodeStatus = NativeHelpers.isFetchPaymentOptionStatusMatched(fetchPaymentOptResponseDTO, "UPI", false);
        Assertions.assertThat(paymodeStatus).as("UPI paymode status mismatch").isTrue();

        OrderDTO orderDTO = new OrderFactory.UPIMandate(merchant, txnToken, initTxnDTO.orderFromBody(), subsId)
                .setPayerAccount("test@paytm")
                .build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        validateTxnStatus(initTxnDTO, orderDTO);

        //Modify Dates to previous month and two days after to send preNotify

        LocalDateTime PreviousDate = LocalDateTime.now().minusMonths(1).plusDays(2);
        PGPHelpers.modifySubscriptionDueDate(Long.valueOf(subsId), LocalDateTime.now().plusDays(2));
        modifySubsDatesInDB(subsId, PreviousDate);

        //PreNotify sent
        PreNotify preNotify = new PreNotify(merchant, initTxnDTO.txnAmountFromBody(), subsId);
        Response response = preNotify.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.status"))
                .isEqualTo("SUCCESS").as("PreNotify is Not Successfull for Given Dates");

        String paytmRefId = response.jsonPath().getString("body.paytmReferenceId");
        modifyNotifyDatesInDB(paytmRefId);

        //Modify dates to previous month to execute renew now
        LocalDateTime TodayDate = LocalDateTime.now().minusMonths(1);
        modifySubsDatesInDB(subsId, TodayDate);

        PGPHelpers.modifySubscriptionDueDate(Long.valueOf(subsId), LocalDateTime.now());

        RenewSubscriptionDTO renewSubscriptionDTO1 = new RenewSubscriptionDTO.Builder
                (merchant.getId(), subsId, initTxnDTO.txnAmountFromBody())
                .setRequestType("")                         //invalid Request type for Limit Breach
                .setMerchantKey(merchant.getKey())
                .build();
        RenewSubscription renewSubscription1 = new RenewSubscription(renewSubscriptionDTO1);
        Response InvalidResp= renewSubscription1.execute();

        Assertions.assertThat(InvalidResp.jsonPath().getString("body.resultInfo.resultMsg"))
                .isEqualTo("Invalid Request Type").as("Renew is sucessful for Invalid Request Type");


        RenewSubscriptionDTO renewSubscriptionDTO2 = new RenewSubscriptionDTO.Builder
                (merchant.getId(), subsId, initTxnDTO.txnAmountFromBody())
                .setRequestType("")
                .setMerchantKey(merchant.getKey())
                .build();

        RenewSubscription renewSubscription2 = new RenewSubscription(renewSubscriptionDTO2);
        await().pollInterval(Duration.TEN_SECONDS).atMost(Duration.FIVE_MINUTES).untilAsserted(() ->
                Assertions.assertThat(renewSubscription2.execute().jsonPath()
                        .getString("body.resultInfo.resultMsg"))
                        .isEqualToIgnoringCase("Invalid Request Type"));
        RenewSubscriptionDTO renewSubscriptionDTO3 = new RenewSubscriptionDTO.Builder
                (merchant.getId(), subsId, initTxnDTO.txnAmountFromBody())
                .setRequestType("")                         //invalid Request type for Limit Breach
                .setMerchantKey(merchant.getKey())
                .build();

        RenewSubscription renewSubscription3 = new RenewSubscription(renewSubscriptionDTO3);
        await().pollInterval(Duration.TEN_SECONDS).atMost(Duration.TWO_MINUTES).untilAsserted(() ->
                Assertions.assertThat(renewSubscription3.execute().jsonPath()
                        .getString("body.resultInfo.resultMsg"))
                        .isEqualToIgnoringCase("Retry count for payment request in this cycle has been exhausted"));

    }



    @Parameters({"isNativePlus"})
    @Test(description = "Retry Breach for Renewal with subscriptionAmountType: Variable" +
            "Frequency Unit: Month Payment UPI Collect RequestType: SUBS_RENEWAL_MF_SIP")
    public void TC_001_Renew_Without_PreNotify(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_MUTUAL_MF;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("100")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("100")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("29")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_MF_SIP)
                .build();

        InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = responseDTO.getBody().getTxnToken();
        String subsId = responseDTO.getBody().getSubscriptionId();

        FetchPaymentOptResponseDTO fetchPaymentOptResponseDTO = NativeHelpers.fetchPaymentOptionResponse(txnToken, merchant.getId(), initTxnDTO.orderFromBody());
        boolean paymodeStatus = NativeHelpers.isFetchPaymentOptionStatusMatched(fetchPaymentOptResponseDTO, "UPI", false);
        Assertions.assertThat(paymodeStatus).as("UPI paymode status mismatch").isTrue();

        OrderDTO orderDTO = new OrderFactory.UPIMandate(merchant, txnToken, initTxnDTO.orderFromBody(), subsId)
                .setPayerAccount("test@paytm")
                .build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        validateTxnStatus(initTxnDTO, orderDTO);

        //Modify Dates to previous month and two days after to send preNotify

        LocalDateTime PreviousDate = LocalDateTime.now().minusMonths(1).plusDays(2);
        PGPHelpers.modifySubscriptionDueDate(Long.valueOf(subsId), LocalDateTime.now().plusDays(2));
        modifySubsDatesInDB(subsId, PreviousDate);


        //Modify dates to previous month to execute renew now
        LocalDateTime TodayDate = LocalDateTime.now().minusMonths(1);
        modifySubsDatesInDB(subsId, TodayDate);

        PGPHelpers.modifySubscriptionDueDate(Long.valueOf(subsId), LocalDateTime.now());

        RenewSubscriptionDTO renewSubscriptionDTO1 = new RenewSubscriptionDTO.Builder
                (merchant.getId(), subsId, initTxnDTO.txnAmountFromBody())
                .setRequestType("")                         //invalid Request type for Limit Breach
                .setMerchantKey(merchant.getKey())
                .build();
        RenewSubscription renewSubscription = new RenewSubscription(renewSubscriptionDTO1);
        Response InvalidResp= renewSubscription.execute();

        Assertions.assertThat(InvalidResp.jsonPath().getString("body.resultInfo.resultMsg"))
                .isEqualTo("Prenotify not found for subscriptionId of this transaction date and amount")
                .as("Renew is successful for without PreNotify");


    }

    /* ONDEMAND Cases PGP-26500*/

    @Parameters({"isNativePlus"})
    @Test(description = "Renewal with subscriptionAmountType: Variable" +
            "Frequency Unit: ONDEMAND Payment UPI Collect RequestType: SUBS_RENEWAL_MF_SIP")
    public void TC_001_Renew_MF_Subs_AmountTypeVariable_Freq_ONDEMAND(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_MUTUAL_MF;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_MF_SIP)
                .build();

        InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = responseDTO.getBody().getTxnToken();
        String subsId = responseDTO.getBody().getSubscriptionId();

        FetchPaymentOptResponseDTO fetchPaymentOptResponseDTO = NativeHelpers.fetchPaymentOptionResponse(txnToken, merchant.getId(), initTxnDTO.orderFromBody());
        boolean paymodeStatus = NativeHelpers.isFetchPaymentOptionStatusMatched(fetchPaymentOptResponseDTO, "UPI", false);
        Assertions.assertThat(paymodeStatus).as("UPI paymode status mismatch").isTrue();

        OrderDTO orderDTO = new OrderFactory.UPIMandate(merchant, txnToken, initTxnDTO.orderFromBody(), subsId)
                .setPayerAccount("test@paytm")
                .build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);

        validateTxnStatus(initTxnDTO, orderDTO);
        String date = CommonUtils.getdate("dd-MM-yyyy");
        PreNotify preNotify = new PreNotify(merchant, initTxnDTO.txnAmountFromBody(), subsId, CommonHelpers.addDays(date, "dd-MM-yyyy", 1));

        Response response = preNotify.execute();

        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.status"))
                .isEqualTo("SUCCESS").as("PreNotify is Not successful for Given Dates");

        String paytmRefId = response.jsonPath().getString("body.paytmReferenceId");

        //Modify Notify dates to two days before and txn date to day
        PGPHelpers.modifySubscriptionPreNotifyStatus(Long.valueOf(paytmRefId), "1");
        PGPHelpers.modifySubscriptionPreNotifyDate(Long.valueOf(paytmRefId), LocalDateTime.now().minusDays(1));
        PGPHelpers.modifySubscriptionPreNotifyTxnDate(Long.valueOf(paytmRefId), LocalDateTime.now().truncatedTo(ChronoUnit.DAYS));

        LocalDateTime TodayDate = LocalDateTime.now().minusDays(1);
        modifySubsDatesInDB(subsId, TodayDate);

        String orderId = executeRenewalAndFetchOrderId(merchant, subsId, initTxnDTO.txnAmountFromBody(), NATIVE_RENEW_MF_SIP);
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, orderId)).isNotNull();
        Assertions.assertThat(PGPHelpers.executeToFetchStatusInPaymentDetails(subsId, orderId)).isEqualTo("ACTIVE");

    }

    @Parameters({"isNativePlus"})
    @Test(description = "Renewal with subscriptionAmountType: VARIABLE" +
            "Frequency Unit: ONDEMAND Payment UPI_INTENT RequestType: Subscription Default")
    public void TC_002_Renew_Subs_AmountTypeVariable_Freq_ONDEMAND(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1500")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("2000")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_SUBSCRIPTION)
                .build();

        InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = responseDTO.getBody().getTxnToken();
        String subsId = responseDTO.getBody().getSubscriptionId();

        FetchPaymentOptResponseDTO fetchPaymentOptResponseDTO = NativeHelpers.fetchPaymentOptionResponse(txnToken, merchant.getId(), initTxnDTO.orderFromBody());
        boolean paymodeStatus = NativeHelpers.isFetchPaymentOptionStatusMatched(fetchPaymentOptResponseDTO, "UPI", false);
        Assertions.assertThat(paymodeStatus).as("UPI paymode status mismatch").isTrue();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchant.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("UPI_INTENT")
                .build();

        NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        validateTxnStatus(initTxnDTO, processTxnV1Request);

        String date = CommonUtils.getdate("dd-MM-yyyy");
        PreNotify preNotify = new PreNotify(merchant, initTxnDTO.txnAmountFromBody(), subsId, CommonHelpers.addDays(date, "dd-MM-yyyy", 1));

        Response response = preNotify.execute();

        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.status"))
                .isEqualTo("SUCCESS").as("PreNotify is Not successful for Given Dates");

        String paytmRefId = response.jsonPath().getString("body.paytmReferenceId");

        //Modify Notify dates to two days before and txn date to day
        PGPHelpers.modifySubscriptionPreNotifyStatus(Long.valueOf(paytmRefId), "1");
        PGPHelpers.modifySubscriptionPreNotifyDate(Long.valueOf(paytmRefId), LocalDateTime.now().minusDays(1));
        PGPHelpers.modifySubscriptionPreNotifyTxnDate(Long.valueOf(paytmRefId), LocalDateTime.now().truncatedTo(ChronoUnit.DAYS));

        LocalDateTime TodayDate = LocalDateTime.now().minusDays(1);
        modifySubsDatesInDB(subsId, TodayDate);

        String orderId = executeRenewalAndFetchOrderId(merchant, subsId, initTxnDTO.txnAmountFromBody(), "");
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, orderId)).isNotNull();
        Assertions.assertThat(PGPHelpers.executeToFetchStatusInPaymentDetails(subsId, orderId)).isEqualTo("ACTIVE");

    }

    @Owner(Constants.Owner.ABHAY)
    @Feature("PGP-29589")
    @Test(description = "Verify Flexi:true flag on success native renew subs when PAYMENT_MODE=UPI")
    public void VerifyFlexiTrueInSubsContractV2ForSubsRenewalWithUPI() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("2000")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("2000")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("YEAR")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_SUBSCRIPTION)
                .build();

        InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);

        String txnToken = responseDTO.getBody().getTxnToken();
        String subsId = responseDTO.getBody().getSubscriptionId();

        FetchPaymentOptResponseDTO fetchPaymentOptResponseDTO = NativeHelpers.fetchPaymentOptionResponse(txnToken, merchant.getId(), initTxnDTO.orderFromBody());
        boolean paymodeStatus = NativeHelpers.isFetchPaymentOptionStatusMatched(fetchPaymentOptResponseDTO, "UPI", false);
        Assertions.assertThat(paymodeStatus).as("UPI paymode status mismatch").isTrue();

        OrderDTO orderDTO = new OrderFactory.UPIMandate(merchant, txnToken, initTxnDTO.orderFromBody(), subsId)
                .setPayerAccount("test@paytm")
                .build();

        checkoutPage.createNativeOrder(orderDTO, false);
        validateTxnStatus(initTxnDTO, orderDTO);

        LocalDateTime PreviousDate = LocalDateTime.now().minusYears(1).plusDays(2);
        PGPHelpers.modifySubscriptionDueDate(Long.valueOf(subsId), LocalDateTime.now().plusDays(2));
        modifySubsDatesInDB(subsId, PreviousDate);


        PreNotify preNotify = new PreNotify(merchant, initTxnDTO.txnAmountFromBody(), subsId);
        Response response = preNotify.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.status"))
                .isEqualTo("SUCCESS").as("PreNotify is Not successful for Given Dates");

        String paytmRefId = response.jsonPath().getString("body.paytmReferenceId");
        modifyNotifyDatesInDB(paytmRefId);

        LocalDateTime frequencyReqDate = LocalDateTime.now().minusYears(1);

        PGPHelpers.modifySubscriptionDueDate(Long.valueOf(subsId), LocalDateTime.now());

        modifySubsDatesInDB(subsId, frequencyReqDate);

        String orderId = executeRenewalAndFetchOrderId(merchant, subsId, initTxnDTO.txnAmountFromBody(), "");
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("metadata", subsId, initTxnDTO.orderFromBody()))
                .as("flexi flag is expected to be true")
                .contains("\"flexi\":true");
    }
    @Feature("PGP-42375")
    @Owner(Constants.Owner.VISHNU_SHEKAR)
    @Test(description = "Same day Renewal with subscriptionAmountType: VARIABLE" +
            "Frequency Unit: ONDEMAND ")
    public void TC_PGP_42375_Renew_Subs_Freq_ONDEMANDSameDayRenewal()throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1500")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("2000")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_SUBSCRIPTION)
                .build();
        InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = responseDTO.getBody().getTxnToken();
        String subsId = responseDTO.getBody().getSubscriptionId();
        FetchPaymentOptResponseDTO fetchPaymentOptResponseDTO = NativeHelpers.fetchPaymentOptionResponse(txnToken, merchant.getId(), initTxnDTO.orderFromBody());
        boolean paymodeStatus = NativeHelpers.isFetchPaymentOptionStatusMatched(fetchPaymentOptResponseDTO, "UPI", false);
        Assertions.assertThat(paymodeStatus).as("UPI paymode status mismatch").isTrue();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchant.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("UPI_INTENT")
                .build();
        NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        validateTxnStatus(initTxnDTO, processTxnV1Request);
        PreNotify preNotify = new PreNotify(merchant, initTxnDTO.txnAmountFromBody(), subsId);
        Response response = preNotify.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.status"))
                .isEqualTo("SUCCESS").as("PreNotify is Not successful for Given Dates");
        String paytmRefId = response.jsonPath().getString("body.paytmReferenceId");
        PGPHelpers.modifySubscriptionPreNotifyStatus(Long.valueOf(paytmRefId), "1");
        PGPHelpers.modifySubscriptionPreNotifyDate(Long.valueOf(paytmRefId), LocalDateTime.now());
        PGPHelpers.modifySubscriptionPreNotifyTxnDate(Long.valueOf(paytmRefId), LocalDateTime.now().truncatedTo(ChronoUnit.DAYS));
        String orderId = executeRenewalAndFetchOrderId(merchant, subsId, initTxnDTO.txnAmountFromBody(), "");
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, orderId)).isNotNull();
        Assertions.assertThat(PGPHelpers.executeToFetchStatusInPaymentDetails(subsId, orderId)).isEqualTo("ACTIVE");
    }
    @Feature("PGP-42375")
    @Owner(Constants.Owner.VISHNU_SHEKAR)
    @Test(description = "Two day Renewal with subscriptionAmountType: VARIABLE" +
            "Frequency Unit: ONDEMAND ")
    public void TC_PGP_42375_Renew_Subs_Freq_ONDEMAND_OneDaysRenewal() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1500")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("2000")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_SUBSCRIPTION)
                .build();
        InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = responseDTO.getBody().getTxnToken();
        String subsId = responseDTO.getBody().getSubscriptionId();
        FetchPaymentOptResponseDTO fetchPaymentOptResponseDTO = NativeHelpers.fetchPaymentOptionResponse(txnToken, merchant.getId(), initTxnDTO.orderFromBody());
        boolean paymodeStatus = NativeHelpers.isFetchPaymentOptionStatusMatched(fetchPaymentOptResponseDTO, "UPI", false);
        Assertions.assertThat(paymodeStatus).as("UPI paymode status mismatch").isTrue();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchant.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("UPI_INTENT")
                .build();
        NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        validateTxnStatus(initTxnDTO, processTxnV1Request);
        PreNotify preNotify = new PreNotify(merchant, initTxnDTO.txnAmountFromBody(), subsId);
        Response response = preNotify.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.status"))
                .isEqualTo("SUCCESS").as("PreNotify is Not successful for Given Dates");
        String paytmRefId = response.jsonPath().getString("body.paytmReferenceId");
        PGPHelpers.modifySubscriptionPreNotifyStatus(Long.valueOf(paytmRefId), "1");
        PGPHelpers.modifySubscriptionPreNotifyDate(Long.valueOf(paytmRefId), LocalDateTime.now());
        PGPHelpers.modifySubscriptionPreNotifyTxnDate(Long.valueOf(paytmRefId), LocalDateTime.now().truncatedTo(ChronoUnit.DAYS).minusDays(1));
        String orderId = executeRenewalAndFetchOrderId(merchant, subsId, initTxnDTO.txnAmountFromBody(), "");
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, orderId)).isNotNull();
        Assertions.assertThat(PGPHelpers.executeToFetchStatusInPaymentDetails(subsId, orderId)).isEqualTo("ACTIVE");
    }
    @Feature("PGP-42375")
    @Owner(Constants.Owner.VISHNU_SHEKAR)
    @Test(description = "Two day Renewal with subscriptionAmountType: VARIABLE" +
            "Frequency Unit: ONDEMAND ")
    public void TC_PGP_42375_Renew_Subs_Freq_ONDEMAND_TwoDaysRenewal() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1500")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("2000")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_SUBSCRIPTION)
                .build();
        InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = responseDTO.getBody().getTxnToken();
        String subsId = responseDTO.getBody().getSubscriptionId();
        FetchPaymentOptResponseDTO fetchPaymentOptResponseDTO = NativeHelpers.fetchPaymentOptionResponse(txnToken, merchant.getId(), initTxnDTO.orderFromBody());
        boolean paymodeStatus = NativeHelpers.isFetchPaymentOptionStatusMatched(fetchPaymentOptResponseDTO, "UPI", false);
        Assertions.assertThat(paymodeStatus).as("UPI paymode status mismatch").isTrue();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchant.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("UPI_INTENT")
                .build();
        NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        validateTxnStatus(initTxnDTO, processTxnV1Request);
        PreNotify preNotify = new PreNotify(merchant, initTxnDTO.txnAmountFromBody(), subsId);
        Response response = preNotify.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.status"))
                .isEqualTo("SUCCESS").as("PreNotify is Not successful for Given Dates");
        String paytmRefId = response.jsonPath().getString("body.paytmReferenceId");
        PGPHelpers.modifySubscriptionPreNotifyStatus(Long.valueOf(paytmRefId), "1");
        PGPHelpers.modifySubscriptionPreNotifyDate(Long.valueOf(paytmRefId), LocalDateTime.now());
        PGPHelpers.modifySubscriptionPreNotifyTxnDate(Long.valueOf(paytmRefId), LocalDateTime.now().truncatedTo(ChronoUnit.DAYS).minusDays(2));
        String orderId = executeRenewalAndFetchOrderId(merchant, subsId, initTxnDTO.txnAmountFromBody(), "");
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, orderId)).isNotNull();
        Assertions.assertThat(PGPHelpers.executeToFetchStatusInPaymentDetails(subsId, orderId)).isEqualTo("ACTIVE");
    }
    @Feature("PGP-42375")
    @Owner(Constants.Owner.VISHNU_SHEKAR)
    @Test(description = "Verify PreNotify failes when subscription renewed on 3rd day with subscriptionAmountType: VARIABLE" +
            "Frequency Unit: ONDEMAND ")
    public void TC_PGP_42375_Renew_Subs_Freq_ONDEMAND_ThreeDaysRenewal() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1500")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("2000")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_SUBSCRIPTION)
                .build();
        InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = responseDTO.getBody().getTxnToken();
        String subsId = responseDTO.getBody().getSubscriptionId();
        FetchPaymentOptResponseDTO fetchPaymentOptResponseDTO = NativeHelpers.fetchPaymentOptionResponse(txnToken, merchant.getId(), initTxnDTO.orderFromBody());
        boolean paymodeStatus = NativeHelpers.isFetchPaymentOptionStatusMatched(fetchPaymentOptResponseDTO, "UPI", false);
        Assertions.assertThat(paymodeStatus).as("UPI paymode status mismatch").isTrue();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchant.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("UPI_INTENT")
                .build();
        NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        validateTxnStatus(initTxnDTO, processTxnV1Request);
        PreNotify preNotify = new PreNotify(merchant, initTxnDTO.txnAmountFromBody(), subsId);
        Response response = preNotify.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.status"))
                .isEqualTo("SUCCESS").as("PreNotify is Not successful for Given Dates");
        String paytmRefId = response.jsonPath().getString("body.paytmReferenceId");
        PGPHelpers.modifySubscriptionPreNotifyStatus(Long.valueOf(paytmRefId), "1");
        PGPHelpers.modifySubscriptionPreNotifyDate(Long.valueOf(paytmRefId), LocalDateTime.now());
        PGPHelpers.modifySubscriptionPreNotifyTxnDate(Long.valueOf(paytmRefId), LocalDateTime.now().truncatedTo(ChronoUnit.DAYS).minusDays(3));
        RenewSubscriptionDTO renewSubscriptionDTO1 = new RenewSubscriptionDTO.Builder
                (merchant.getId(), subsId, initTxnDTO.txnAmountFromBody())
                .setRequestType("")
                .setMerchantKey(merchant.getKey())
                .build();
        RenewSubscription renewSubscription = new RenewSubscription(renewSubscriptionDTO1);
        Response InvalidResp= renewSubscription.execute();
        Assertions.assertThat(InvalidResp.jsonPath().getString("body.resultInfo.resultMsg"))
                .isEqualTo("Prenotify not found for subscriptionId of this transaction date and amount");
    }
}
