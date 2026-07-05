package scripts.UPISubsMandate;

import com.paytm.LocalConfig;
import com.paytm.ServerConfigProvider;
import com.paytm.api.GetPaymentStatus;
import com.paytm.api.nativeAPI.SubscriptionCreate;
import com.paytm.api.ProcessTransactionV1;
import com.paytm.api.TxnStatus;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.LogsValidationHelper;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.PG2LogsValidationHelper;
import com.paytm.base.test.Group;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.GetPaymentStatusRequest.GetPaymentStatusDTO;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.response.Body;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.response.FetchPaymentOptResponseDTO;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.response.PaymentModes;
import com.paytm.dto.NativeDTO.InitTxn.AdditionalInfo;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.MutualFundFeedInfo;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.dto.processTransactionV1.response.ProcessTxnV1Response;
import com.paytm.framework.conditions.SoftAssertion;
import com.paytm.framework.utils.CommonUtils;
import com.paytm.pages.CheckoutPage;
import com.paytm.utils.merchant.Peon;
import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import io.qameta.allure.Owner;
import io.qameta.allure.Step;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.Date;
import java.util.List;

import static com.paytm.apphelpers.LogsValidationHelper.getLogsOnServer;
public class UPIIntentNativeSubs extends PGPBaseTest implements UPINativeSubsTests, UPINativeMFSIPTests {
    private final static String NATIVE_SUBSCRIPTION = "NATIVE_SUBSCRIPTION";
    private final static String NATIVE_MF_SIP = "NATIVE_MF_SIP";
    private final CheckoutPage checkoutPage = new CheckoutPage();


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



    /*----------------------------------------------------------------------------*/
    /*            Test cases For Native Mutual SIP Subscription RequestType       */
    /*----------------------------------------------------------------------------*/


    @Parameters({"isNativePlus"})
    @Test(description = "Verify txn token and subs id generated successfully when " +
            "requestType=NATIVE_MF_SIP, Paymentmode=UPI, freq=DAY")
    public void TC_001_Native_MF_Subs_FreqUnitDay(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_MUTUAL_MF;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("FIX")
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

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchant.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("UPI_INTENT")
                .build();

        NativeHelpers.executeProcessTxnV1(processTxnV1Request);

        TxnStatus txnStatus = new TxnStatus(merchant.getId(), processTxnV1Request.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(processTxnV1Request.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("PPBL")
                .validateMid(merchant.getId())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateRefundAmnt("0.00")
                .validateSubsid(subsId)
                .validateTxnDate(new Date())
                .AssertAll();


        assertion.apply(peonWait.apply(() -> peons.getAt(processTxnV1Request.getBody().getOrderId()) != null));
        Peon peon = peons.getAt(processTxnV1Request.getBody().getOrderId());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "SUBS_ID"),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.gatewayName().equals("PPBL"),
                peon.mercUnqRef().equals(""),
                peon.mId().equals(merchant.getId()),
                peon.orderId().equals(processTxnV1Request.getBody().getOrderId()),
                peon.payMode().equals("UPI"),
                peon.respCode().equals("01"),
                peon.status().equals("TXN_SUCCESS"),
                peon.txnAmt().equals(initTxnDTO.txnAmountFromBody()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not(),
                peon.subsId().equals(subsId)

        );
        sAssert.eval();
    }


    @Override
    @Parameters({"isNativePlus"})
    @Test(description = "Verify txn token and subs id generated successfully when " +
            "requestType=NATIVE_MF_SIP, Paymentmode=UPI, freq=MONTH")
    public void TC_002_Native_MF_Subs_FreqUnitMonth(@Optional("true") Boolean isNativePlus) throws Exception {

        String date = CommonUtils.getdate("yyyy-MM-dd");
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
                .setSubscriptionStartDate(CommonHelpers.addMonths(date, "yyyy-MM-dd", 1))
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

        TxnStatus txnStatus = new TxnStatus(merchant.getId(), processTxnV1Request.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(processTxnV1Request.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("PPBL")
                .validateMid(merchant.getId())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateRefundAmnt("0.00")
                .validateSubsid(subsId)
                .validateTxnDate(new Date())
                .AssertAll();


        assertion.apply(peonWait.apply(() -> peons.getAt(processTxnV1Request.getBody().getOrderId()) != null));
        Peon peon = peons.getAt(processTxnV1Request.getBody().getOrderId());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "SUBS_ID"),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.gatewayName().equals("PPBL"),
                peon.mercUnqRef().equals(""),
                peon.mId().equals(merchant.getId()),
                peon.orderId().equals(processTxnV1Request.getBody().getOrderId()),
                peon.payMode().equals("UPI"),
                peon.respCode().equals("01"),
                peon.status().equals("TXN_SUCCESS"),
                peon.txnAmt().equals(initTxnDTO.txnAmountFromBody()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not(),
                peon.subsId().equals(subsId)

        );
        sAssert.eval();
    }

    @Override
    @Parameters({"isNativePlus"})
    @Test(description = "Verify txn token and subs id generated successfully when " +
            "requestType=NATIVE_MF_SIP, Paymentmode=UPI, freq=YEAR")
    public void TC_003_Native_MF_Subs_FreqUnitYear(@Optional("true") Boolean isNativePlus) throws Exception {

        String date = CommonUtils.getdate("yyyy-MM-dd");
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_MUTUAL_MF;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("YEAR")
                .setSubscriptionGraceDays("360")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.addYears(date, "yyyy-MM-dd", 1))
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

        TxnStatus txnStatus = new TxnStatus(merchant.getId(), processTxnV1Request.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(processTxnV1Request.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("PPBL")
                .validateMid(merchant.getId())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateRefundAmnt("0.00")
                .validateSubsid(subsId)
                .validateTxnDate(new Date())
                .AssertAll();


        assertion.apply(peonWait.apply(() -> peons.getAt(processTxnV1Request.getBody().getOrderId()) != null));
        Peon peon = peons.getAt(processTxnV1Request.getBody().getOrderId());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "SUBS_ID"),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.gatewayName().equals("PPBL"),
                peon.mercUnqRef().equals(""),
                peon.mId().equals(merchant.getId()),
                peon.orderId().equals(processTxnV1Request.getBody().getOrderId()),
                peon.payMode().equals("UPI"),
                peon.respCode().equals("01"),
                peon.status().equals("TXN_SUCCESS"),
                peon.txnAmt().equals(initTxnDTO.txnAmountFromBody()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not(),
                peon.subsId().equals(subsId)

        );
        sAssert.eval();


    }


    @Override
    @Parameters({"isNativePlus"})
    @Test(description = "Verify txn token and subs id generated successfully when " +
            "requestType=NATIVE_MF_SIP, Paymentmode=UPI, freq=QUARTER")
    public void TC_004_Native_MF_Subs_FreqUnitQuater(@Optional("true") Boolean isNativePlus) throws Exception {

        String date = CommonUtils.getdate("yyyy-MM-dd");
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_MUTUAL_MF;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("QUARTER")
                .setSubscriptionGraceDays("2")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.addMonths(date, "yyyy-MM-dd", 3))
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

        TxnStatus txnStatus = new TxnStatus(merchant.getId(), processTxnV1Request.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(processTxnV1Request.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("PPBL")
                .validateMid(merchant.getId())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateRefundAmnt("0.00")
                .validateSubsid(subsId)
                .validateTxnDate(new Date())
                .AssertAll();


        assertion.apply(peonWait.apply(() -> peons.getAt(processTxnV1Request.getBody().getOrderId()) != null));
        Peon peon = peons.getAt(processTxnV1Request.getBody().getOrderId());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "SUBS_ID"),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.gatewayName().equals("PPBL"),
                peon.mercUnqRef().equals(""),
                peon.mId().equals(merchant.getId()),
                peon.orderId().equals(processTxnV1Request.getBody().getOrderId()),
                peon.payMode().equals("UPI"),
                peon.respCode().equals("01"),
                peon.status().equals("TXN_SUCCESS"),
                peon.txnAmt().equals(initTxnDTO.txnAmountFromBody()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not(),
                peon.subsId().equals(subsId)

        );
        sAssert.eval();
    }

    @Override
    @Parameters({"isNativePlus"})
    @Test(description = "Verify txn token and subs id generated successfully when " +
            "requestType=NATIVE_MF_SIP, Paymentmode=UPI, freq=BI_MONTHLY")
    public void TC_005_Native_MF_Subs_FreqUnitBiMonthly(@Optional("true") Boolean isNativePlus) throws Exception {

        String date = CommonUtils.getdate("yyyy-MM-dd");
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.MF_SIP_NEW_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("BI_MONTHLY")
                .setSubscriptionGraceDays("14")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.addMonths(date, "yyyy-MM-dd", 2))
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

        TxnStatus txnStatus = new TxnStatus(merchant.getId(), processTxnV1Request.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(processTxnV1Request.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("PPBL")
                .validateMid(merchant.getId())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateRefundAmnt("0.00")
                .validateSubsid(subsId)
                .validateTxnDate(new Date())
                .AssertAll();


        assertion.apply(peonWait.apply(() -> peons.getAt(processTxnV1Request.getBody().getOrderId()) != null));
        Peon peon = peons.getAt(processTxnV1Request.getBody().getOrderId());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "SUBS_ID"),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.gatewayName().equals("PPBL"),
                peon.mercUnqRef().equals(""),
                peon.mId().equals(merchant.getId()),
                peon.orderId().equals(processTxnV1Request.getBody().getOrderId()),
                peon.payMode().equals("UPI"),
                peon.respCode().equals("01"),
                peon.status().equals("TXN_SUCCESS"),
                peon.txnAmt().equals(initTxnDTO.txnAmountFromBody()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not(),
                peon.subsId().equals(subsId)

        );
        sAssert.eval();
    }


    @Override
    @Parameters({"isNativePlus"})
    @Test(description = "Verify txn token and subs id generated successfully when " +
            "requestType=NATIVE_MF_SIP, Paymentmode=UPI, freq=SEMI_ANNUALLY")
    public void TC_006_Native_MF_Subs_FreqUnitSemiAnually(@Optional("true") Boolean isNativePlus) throws Exception {


        String date = CommonUtils.getdate("yyyy-MM-dd");
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_MUTUAL_MF;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("SEMI_ANNUALLY")
                .setSubscriptionGraceDays("180")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.addMonths(date, "yyyy-MM-dd", 6))
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

        TxnStatus txnStatus = new TxnStatus(merchant.getId(), processTxnV1Request.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(processTxnV1Request.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("PPBL")
                .validateMid(merchant.getId())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateRefundAmnt("0.00")
                .validateSubsid(subsId)
                .validateTxnDate(new Date())
                .AssertAll();


        assertion.apply(peonWait.apply(() -> peons.getAt(processTxnV1Request.getBody().getOrderId()) != null));
        Peon peon = peons.getAt(processTxnV1Request.getBody().getOrderId());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "SUBS_ID"),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.gatewayName().equals("PPBL"),
                peon.mercUnqRef().equals(""),
                peon.mId().equals(merchant.getId()),
                peon.orderId().equals(processTxnV1Request.getBody().getOrderId()),
                peon.payMode().equals("UPI"),
                peon.respCode().equals("01"),
                peon.status().equals("TXN_SUCCESS"),
                peon.txnAmt().equals(initTxnDTO.txnAmountFromBody()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not(),
                peon.subsId().equals(subsId)

        );
        sAssert.eval();
    }



    @Test(description = "Verify txn token and subs id generated successfully when " +
            "requestType=NATIVE_MF_SIP, freq=WEEK")
    public void TC_007_Native_MF_Subs_FreqUnitWeek(@Optional("true") Boolean isNativePlus) throws Exception{

        String date = CommonUtils.getdate("yyyy-MM-dd");
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_MUTUAL_MF;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("WEEK")
                .setSubscriptionGraceDays("5")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.addDays(date, "yyyy-MM-dd", 7))
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

        TxnStatus txnStatus = new TxnStatus(merchant.getId(), processTxnV1Request.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(processTxnV1Request.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("PPBL")
                .validateMid(merchant.getId())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateRefundAmnt("0.00")
                .validateSubsid(subsId)
                .validateTxnDate(new Date())
                .AssertAll();


        assertion.apply(peonWait.apply(() -> peons.getAt(processTxnV1Request.getBody().getOrderId()) != null));
        Peon peon = peons.getAt(processTxnV1Request.getBody().getOrderId());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "SUBS_ID"),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.gatewayName().equals("PPBL"),
                peon.mercUnqRef().equals(""),
                peon.mId().equals(merchant.getId()),
                peon.orderId().equals(processTxnV1Request.getBody().getOrderId()),
                peon.payMode().equals("UPI"),
                peon.respCode().equals("01"),
                peon.status().equals("TXN_SUCCESS"),
                peon.txnAmt().equals(initTxnDTO.txnAmountFromBody()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not(),
                peon.subsId().equals(subsId)

        );
        sAssert.eval();
    }

//ONDEMAND IS ALLOWED IN UPI PGP-26500

    @Owner("akshat.sharma")
    @Override
    @Parameters({"isNativePlus"})
    @Test(description = "Verify txn token and subs id generated successfully when" +
            "requestType=NATIVE_MF_SIP, freq=ONDEMAND")
    public void TC_008_Native_MF_Subs_FreqUnitOnDemand(@Optional("true") Boolean isNativePlus) throws Exception {


        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_MUTUAL_MF;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_MF_SIP)
                .build();


        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);

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

        TxnStatus txnStatus = new TxnStatus(merchant.getId(), processTxnV1Request.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(processTxnV1Request.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("PPBL")
                .validateMid(merchant.getId())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateRefundAmnt("0.00")
                .validateSubsid(subsId)
                .validateTxnDate(new Date())
                .AssertAll();

        assertion.apply(peonWait.apply(() -> peons.getAt(processTxnV1Request.getBody().getOrderId()) != null));
        Peon peon = peons.getAt(processTxnV1Request.getBody().getOrderId());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "SUBS_ID"),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.gatewayName().equals("PPBL"),
                peon.mercUnqRef().equals(""),
                peon.mId().equals(merchant.getId()),
                peon.orderId().equals(processTxnV1Request.getBody().getOrderId()),
                peon.payMode().equals("UPI"),
                peon.respCode().equals("01"),
                peon.status().equals("TXN_SUCCESS"),
                peon.txnAmt().equals(initTxnDTO.txnAmountFromBody()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not(),
                peon.subsId().equals(subsId)

        );
        sAssert.eval();

    }

    /*Frequency Related TestCases*/


    @Override
    @Parameters({"isNativePlus"})
    @Test(description = "Verify Invalid GraceDays when Freq>1 " +
            "requestType=NATIVE_MF_SIP")
    public void TC_001_Native_MF_Subs_FreqGreaterThn1(@Optional("true") Boolean isNativePlus) throws Exception {


        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_MUTUAL_MF;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("2")
                .setSubscriptionFrequencyUnit("DAY")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_MF_SIP)
                .build();

        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);

        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultStatus())
                .as("resultStatus mismatch")
                .isEqualTo("F");
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultCode())
                .as("resultCode mismatch")
                .isEqualTo("4001");
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultMsg())
                .as("resultMsg mismatch")
                .isEqualToIgnoringCase("Invalid Subscription Frequency");


    }


    @Override
    @Parameters({"isNativePlus"})
    @Test(description = "Verify Freq>1 ,SubsPaymode =Unknown " +
            "requestType=NATIVE_MF_SIP , UPI Paymode is filtered in FPO")
    public void TC_002_Native_MF_Subs_FreqGreaterthan1_PaymodeBlank(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.MF_SIP_NEW_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("2")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_MF_SIP)
                .build();


        InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);

        String txnToken = responseDTO.getBody().getTxnToken();


        FetchPaymentOptResponseDTO fetchPaymentOptResponseDTO = NativeHelpers.fetchPaymentOptionResponse(txnToken, merchant.getId(), initTxnDTO.orderFromBody());
        Body body = fetchPaymentOptResponseDTO.getBody();
        Assertions.assertThat(body.getResultInfo().getResultCode())
                .as("resultCode mismatch")
                .isEqualToIgnoringCase("0000");
        List<PaymentModes> paymentModes = body.getMerchantPayOption().getPaymentModes();
        boolean payUPI = false;
        for(PaymentModes paymentMode : paymentModes) {
            if(paymentMode.getPaymentMode().equalsIgnoreCase("UPI"))
                payUPI=true;
        }
        Assertions.assertThat(payUPI).isFalse().as("UPI is not getting filtered out when Frequency>1");
    }

    @Override
    @Parameters({"isNativePlus"})
    @Test(description = "Verify Freq=0 Subscription Validation Fails" +
            "requestType=NATIVE_MF_SIP freq is Mandatory")
    public void TC_003_Native_MF_Subs_FreqEqualToZero(@Optional("true") Boolean isNativePlus) throws Exception {


        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_MUTUAL_MF;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("0")
                .setSubscriptionFrequencyUnit("YEAR")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_MF_SIP)
                .build();


        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);

        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultStatus())
                .as("resultStatus mismatch")
                .isEqualTo("F");
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultCode())
                .as("resultCode mismatch")
                .isEqualTo("4001");
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultMsg())
                .as("resultMsg mismatch")
                .isEqualToIgnoringCase("Invalid Subscription Frequency");

    }


    @Override
    @Parameters({"isNativePlus"})
    @Test(description = "Verify Freq is Blank By Default it will consider it 1 , Paymode = UPI" +
            "txn is Successful for requestType=NATIVE_MF_SIP")
    public void TC_004_Native_MF_Subs_FreqIsBlank(@Optional("true") Boolean isNativePlus) throws Exception {


        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.MF_SIP_NEW_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("")
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

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchant.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("UPI_INTENT")
                .build();

        NativeHelpers.executeProcessTxnV1(processTxnV1Request);

        TxnStatus txnStatus = new TxnStatus(merchant.getId(), processTxnV1Request.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(processTxnV1Request.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("PPBL")
                .validateMid(merchant.getId())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateRefundAmnt("0.00")
                .validateSubsid(subsId)
                .validateTxnDate(new Date())
                .AssertAll();

    }



    /*Start Date Related TestCases*/

    @Override
    @Parameters({"isNativePlus"})
    @Test(description = "Verify StartDate is Null not Supported in UPI " +
            "requestType=NATIVE_MF_SIP")
    public void TC_001_Native_MF_Subs_StartDateIsNull(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_MUTUAL_MF;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("YEAR")
                .setSubscriptionGraceDays("300")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate("")
                .setRequestType(NATIVE_MF_SIP)
                .build();
        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);

        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultStatus())
                .as("resultStatus mismatch")
                .isEqualTo("F");
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultCode())
                .as("resultCode mismatch")
                .isEqualTo("4001");
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultMsg())
                .as("resultMsg mismatch")
                .isEqualToIgnoringCase("Invalid Subscription start date");


    }

    @Override
    @Parameters({"isNativePlus"})
    @Test(description = "Verify StartDate is equal to Future Date Supported in UPI " +
            "requestType=NATIVE_MF_SIP")
    public void TC_002_Native_MF_Subs_StartDateIsFuture(@Optional("true") Boolean isNativePlus) throws Exception {


        String date = CommonUtils.getdate("yyyy-MM-dd");
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.MF_SIP_NEW_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("DAY")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.addDays(date, "yyyy-MM-dd", 1))
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

        TxnStatus txnStatus = new TxnStatus(merchant.getId(), processTxnV1Request.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(processTxnV1Request.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("PPBL")
                .validateMid(merchant.getId())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateRefundAmnt("0.00")
                .validateSubsid(subsId)
                .validateTxnDate(new Date())
                .AssertAll();

    }

    @Override
    @Parameters({"isNativePlus"})
    @Test(description = "Verify blank subscriptionGraceDays are not supported ")
    public void TC_003_Native_MF_Subs_StartDateGraceDayNull(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_MUTUAL_MF;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_MF_SIP)
                .build();
        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);

        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultStatus())
                .as("resultStatus mismatch")
                .isEqualTo("F");
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultCode())
                .as("resultCode mismatch")
                .isEqualTo("4001");
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultMsg())
                .as("resultMsg mismatch")
                .isEqualToIgnoringCase("Grace days value is mandatory");

    /*    InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);

        String txnToken = responseDTO.getBody().getTxnToken();
        String subsId = responseDTO.getBody().getSubscriptionId();


        FetchPaymentOptResponseDTO fetchPaymentOptResponseDTO = NativeHelpers.fetchPaymentOptionResponse(txnToken, merchant.getId(), initTxnDTO.orderFromBody());
        Body body = fetchPaymentOptResponseDTO.getBody();
        Assertions.assertThat(body.getResultInfo().getResultCode())
                .as("resultCode mismatch")
                .isEqualToIgnoringCase("0000");
        List<PaymentModes> paymentModes = body.getMerchantPayOption().getPaymentModes();
        boolean payUPI = false;
        for(PaymentModes paymentMode : paymentModes) {
            if(paymentMode.getPaymentMode().equalsIgnoreCase("UPI"))
                payUPI=true;
        }
        Assertions.assertThat(payUPI).isFalse().as("UPI is not getting filtered out when " +
                "Subs Start and Grace day is blank");*/
    }

    @Override
    @Parameters({"isNativePlus"})
    @Test(description = "Verify subscriptionGraceDays:1 subscriptionStartDate: , invalid Subscription start date" +
            "requestType=NATIVE_MF_SIP")
    public void TC_004_Native_MF_Subs_StartDateIsOnlyBlank(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_MUTUAL_MF;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("2")
                .setSubscriptionFrequencyUnit("DAY")
                .setSubscriptionGraceDays("1")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate("")
                .setRequestType(NATIVE_MF_SIP)
                .build();


        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);


        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultStatus())
                .as("resultStatus mismatch")
                .isEqualTo("F");
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultCode())
                .as("resultCode mismatch")
                .isEqualTo("4001");
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultMsg())
                .as("resultMsg mismatch")
                .isEqualToIgnoringCase("Invalid Subscription start date");


    }


    /*Enable Retry TestCases*/
    @Override
    @Parameters({"isNativePlus"})
    @Test(description = "Verify when Paymode=UPI SubsRetry >2  , invalid Subscription retry count" +
            "requestType=NATIVE_MF_SIP")
    public void TC_001_Native_MF_Subs_EnableRetryGreaterThan2(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_MUTUAL_MF;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("2")
                .setSubscriptionFrequencyUnit("DAY")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("3")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_MF_SIP)
                .build();

        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultStatus())
                .as("resultStatus mismatch")
                .isEqualTo("F");
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultCode())
                .as("resultCode mismatch")
                .isEqualTo("4001");
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultMsg())
                .as("resultMsg mismatch")
                .isEqualToIgnoringCase("invalid Subscription retry count");
    }

    @Override
    @Parameters({"isNativePlus"})
    @Test(description = "Verify when Paymode=UPI SubsRetry >2 Paymode=null , BHIM UPI is not allowed for this transaction, " +
            "kindly use some other payment mode requestType=NATIVE_MF_SIP")
    public void TC_002_Native_MF_Subs_EnableRetryGreaterThan2PaymodeBlank(@Optional("true") Boolean isNativePlus) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_MUTUAL_MF;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("DAY")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("3")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_MF_SIP)
                .build();

        InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);

        String txnToken = responseDTO.getBody().getTxnToken();

        FetchPaymentOptResponseDTO fetchPaymentOptResponseDTO = NativeHelpers.fetchPaymentOptionResponse(txnToken, merchant.getId(), initTxnDTO.orderFromBody());
        Body body = fetchPaymentOptResponseDTO.getBody();
        Assertions.assertThat(body.getResultInfo().getResultCode())
                .as("resultCode mismatch")
                .isEqualToIgnoringCase("0000");
        List<PaymentModes> paymentModes = body.getMerchantPayOption().getPaymentModes();
        boolean payUPI = false;
        for(PaymentModes paymentMode : paymentModes) {
            if(paymentMode.getPaymentMode().equalsIgnoreCase("UPI"))
                payUPI=true;
        }
        Assertions.assertThat(payUPI).isFalse().as("UPI is not getting filtered out when SunsRetry>2");
    }



    /*Subscription Max Amount TestCases*/

    //UPI Limit for Subs has been increased to 25k - PGP-36177
    @Override
    @Parameters({"isNativePlus"})
    @Test(description = "If subscriptionMaxAmount>5000 and txn amount>5000, and paymode=null," +
            "BHIM UPI should be allowed for this transaction as UPI Subs limit is 25K")
    public void TC_001_Native_MF_Subs_MaxAmountGreaterThan5000PaymodeNull(@Optional("true") Boolean isNativePlus) throws Exception {


        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.MF_SIP_NEW_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("5500")
                .setSubscriptionPaymentMode("")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("5500")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_MF_SIP)
                .build();


        InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);

        String txnToken = responseDTO.getBody().getTxnToken();

        FetchPaymentOptResponseDTO fetchPaymentOptResponseDTO = NativeHelpers.fetchPaymentOptionResponse(txnToken, merchant.getId(), initTxnDTO.orderFromBody());
        Body body = fetchPaymentOptResponseDTO.getBody();
        Assertions.assertThat(body.getResultInfo().getResultCode())
                .as("resultCode mismatch")
                .isEqualToIgnoringCase("0000");
        List<PaymentModes> paymentModes = body.getMerchantPayOption().getPaymentModes();
        boolean payUPI = false;
        for(PaymentModes paymentMode : paymentModes) {
            if(paymentMode.getPaymentMode().equalsIgnoreCase("UPI"))
                payUPI=true;
        }
      //  Assertions.assertThat(payUPI).isFalse().as("UPI is not getting filtered out when subscriptionMaxAmount>5000");
        Assertions.assertThat(payUPI).isTrue().as("UPI should not get filtered out when subscriptionMaxAmount>5000 as UPI Subs limit is 25k now");


    }

    //UPI Limit for Subs has been increased to 25k - PGP-36177
    @Override
    @Parameters({"isNativePlus"})
    @Test(description = "If subscriptionMaxAmount>25000 and txn amount>25000, and paymode=UPI," +
            "resultMsg: Subscription Amount Limit For UPI Breached requestType=NATIVE_MF_SIP")
    public void TC_002_Native_MF_Subs_MaxAmountGreaterThan5000PaymodeUPI(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_MUTUAL_MF;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("100500")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("100500")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("YEAR")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_MF_SIP)
                .build();


        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);

        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultStatus())
                .as("resultStatus mismatch")
                .isEqualTo("F");
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultCode())
                .as("resultCode mismatch")
                .isEqualTo("4001");
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultMsg())
                .as("resultMsg mismatch")
                .isEqualToIgnoringCase("Subscription Amount Limit For UPI Breached");


    }


    @Override
    @Parameters({"isNativePlus"})
    @Test(description = "If txn amount>Subscription amount, and paymode=UPI," +
            "requestType=NATIVE_MF_SIP")
    public void TC_003_Native_MF_Subs_TxnAmountGreaterThanMaxAmount(@Optional("true") Boolean isNativePlus) throws Exception {


        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_MUTUAL_MF;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("5")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("2")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_MF_SIP)
                .build();


        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);

        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultStatus())
                .as("resultStatus mismatch")
                .isEqualTo("F");
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultCode())
                .as("resultCode mismatch")
                .isEqualTo("2022");
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultMsg())
                .as("resultMsg mismatch")
                .isEqualToIgnoringCase("Paymode selected is not applicable when txn amount is greater than the renewal amount");

    }

    /*Amount Type TestCases*/

    @Override
    @Parameters({"isNativePlus"})
    @Test(description = "For FIX amount type,if  equal amount is not passed for txn amount & max amount if payment mode=UPI," +
            " resultMsg : Transaction amount is not equal to max amount set against the subscription")
    public void TC_001_Native_MF_Subs_FixAmountEqualAmountNotPassedUPI(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_MUTUAL_MF;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("200")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("20")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("29")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_MF_SIP)
                .build();

        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);

        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultStatus())
                .as("resultStatus mismatch")
                .isEqualTo("F");
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultCode())
                .as("resultCode mismatch")
                .isEqualTo("2022");
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultMsg())
                .as("resultMsg mismatch")
                .isEqualToIgnoringCase("Paymode selected is not applicable when txn amount is greater than the renewal amount");
    }

    @Override
    @Feature("PGP-33945")
    @Parameters({"isNativePlus"})
    @Test(description = "For FIX amount type,if  equal amount is not passed for txn amount & max amount if payment mode=null," +
            " then it should fail at the time of process txn ")
    public void TC_002_Native_MF_Subs_FixAmountEqualAmountNotPassed(@Optional("true") Boolean isNativePlus) throws Exception {

        String date = CommonUtils.getdate("yyyy-MM-dd");
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.MF_SIP_NEW_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("20")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("YEAR")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.addYears(date, "yyyy-MM-dd", 1))
                .setRequestType(NATIVE_MF_SIP)
                .build();

        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);

        String txnToken = responseDTO.getBody().getTxnToken();

        FetchPaymentOptResponseDTO fetchPaymentOptResponseDTO = NativeHelpers.fetchPaymentOptionResponse(txnToken, merchant.getId(), initTxnDTO.orderFromBody());
        boolean paymodeStatus = NativeHelpers.isFetchPaymentOptionStatusMatched(fetchPaymentOptResponseDTO, "UPI", false);
        Assertions.assertThat(paymodeStatus).as("UPI paymode status mismatch").isFalse();

    }

    @Override
    @Parameters({"isNativePlus"})
    @Test(description = "For the Variable amount, the subscription txn amount should be" +
            " less than or equal to the subscription max amount")
    public void TC_003_Native_MF_Subs_VariableAmountEqualOrLessMaxAmount(@Optional("true") Boolean isNativePlus) throws Exception {

        String date = CommonUtils.getdate("yyyy-MM-dd");
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.MF_SIP_NEW_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("YEAR")
                .setSubscriptionGraceDays("300")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.addYears(date, "yyyy-MM-dd", 1))
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

        TxnStatus txnStatus = new TxnStatus(merchant.getId(), processTxnV1Request.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(processTxnV1Request.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("PPBL")
                .validateMid(merchant.getId())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateRefundAmnt("0.00")
                .validateSubsid(subsId)
                .validateTxnDate(new Date())
                .AssertAll();

    }


    @Issue("PGP-26277")
    @Test(description = "If subscriptionMaxAmount>5000 and txn amount<5000, and paymode=null," +
            "requestType=NATIVE_MF_SIP" , groups = Group.Status.BUG)
    public void TC_004_Native_MF_Subs_OnlyMaxAmountGreaterThanMaxAmount(@Optional("true") Boolean isNativePlus) throws Exception{


        String date = CommonUtils.getdate("yyyy-MM-dd");
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_MUTUAL_MF;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1000")
                .setSubscriptionPaymentMode("")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("5500")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("YEAR")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.addYears(date, "yyyy-MM-dd", 1))
                .setRequestType(NATIVE_MF_SIP)
                .build();


        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        String txnToken = responseDTO.getBody().getTxnToken();
        FetchPaymentOptResponseDTO fetchPaymentOptResponseDTO = NativeHelpers.fetchPaymentOptionResponse(txnToken, merchant.getId(), initTxnDTO.orderFromBody());
        Body body = fetchPaymentOptResponseDTO.getBody();
        Assertions.assertThat(body.getResultInfo().getResultCode())
                .as("resultCode mismatch")
                .isEqualToIgnoringCase("0000");
        List<PaymentModes> paymentModes = body.getMerchantPayOption().getPaymentModes();
        boolean payUPI = false;
        for(PaymentModes paymentMode : paymentModes) {
            if(paymentMode.getPaymentMode().equalsIgnoreCase("UPI"))
                payUPI=true;
        }
        Assertions.assertThat(payUPI).isFalse().as("UPI is not getting filtered out when subscriptionMaxAmount>5000");


    }

    @Override
    @Parameters({"isNativePlus"})
    @Test(description = "validateAccountNumber:true, accountNumber:valid " +
            " allow Unverified account false txn is successful")
    public void TC_001_Native_MF_Subs_AccountNumTrueAndValid(@Optional("true") Boolean isNativePlus) throws Exception {

        String date = CommonUtils.getdate("yyyy-MM-dd");
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.MF_SIP_NEW_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setAccountNumber("1234543123")
                .setValidateAccountNumber("true")
                .setAllowUnverifiedAccount("false")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("YEAR")
                .setSubscriptionGraceDays("300")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.addYears(date, "yyyy-MM-dd", 1))
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

        TxnStatus txnStatus = new TxnStatus(merchant.getId(), processTxnV1Request.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(processTxnV1Request.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("PPBL")
                .validateMid(merchant.getId())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateRefundAmnt("0.00")
                .validateSubsid(subsId)
                .validateTxnDate(new Date())
                .AssertAll();





    }

    @Override
    @Parameters({"isNativePlus"})
    @Test(description = "validateAccountNumber:true, accountNumber:invalid," +
            "allow unverified account : false then it should fail at the time of process txn ")
    public void TC_002_Native_MF_Subs_AccountNumTrueAndInvalid(@Optional("true") Boolean isNativePlus) throws Exception {


        String date = CommonUtils.getdate("yyyy-MM-dd");
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.MF_SIP_NEW_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("10")
                .setAccountNumber("7777777")
                .setValidateAccountNumber("true")
                .setAllowUnverifiedAccount("false")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("YEAR")
                .setSubscriptionGraceDays("300")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.addYears(date, "yyyy-MM-dd", 1))
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

        TxnStatus txnStatus = new TxnStatus(merchant.getId(), processTxnV1Request.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(processTxnV1Request.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_FAILURE")
                .validateTxnType("SALE")
                .validateGatewayName("PPBL")
                .validateMid(merchant.getId())
                .validatePaymentMode("UPI")
                .validateRespCode("227")
                .validateRefundAmnt("0.00")
                .validateSubsid(subsId)
                .validateTxnDate(new Date())
                .AssertAll();

    }


    @Test(description = "validateAccountNumber:true, accountNumber:invalid " +
            " allow Unverified account : true , Txn is Successful " )
    @Override
    public void TC_003_Native_MF_Subs_AccountNumTrueAndInValidUnverifiedFalse(@Optional("true") Boolean isNativePlus) throws Exception {


        String date = CommonUtils.getdate("yyyy-MM-dd");
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.MF_SIP_NEW_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("10")
                .setValidateAccountNumber("true")
                .setSubscriptionFrequency("1")
                .setAccountNumber("7777777")
                .setAllowUnverifiedAccount("true")
                .setSubscriptionFrequencyUnit("YEAR")
                .setSubscriptionGraceDays("10")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.addYears(date, "yyyy-MM-dd", 1))
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

        TxnStatus txnStatus = new TxnStatus(merchant.getId(), processTxnV1Request.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(processTxnV1Request.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("PPBL")
                .validateMid(merchant.getId())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateRefundAmnt("0.00")
                .validateSubsid(subsId)
                .validateTxnDate(new Date())
                .AssertAll();
    }


    @Parameters({"isNativePlus"})
    @Test(description = "validateAccountNumber:false, accountNumber:invalid,"+
            " unverified account:false Txn is Successful " )
    @Override
    public void TC_004_Native_MF_Subs_AccountNumFalseAndValid(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.MF_SIP_NEW_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setAccountNumber("7777777")
                .setValidateAccountNumber("false")
                .setAllowUnverifiedAccount("false")
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

        TxnStatus txnStatus = new TxnStatus(merchant.getId(), processTxnV1Request.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(processTxnV1Request.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("PPBL")
                .validateMid(merchant.getId())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateRefundAmnt("0.00")
                .validateSubsid(subsId)
                .validateTxnDate(new Date())
                .AssertAll();


    }

    @Test(description = "Verify response validation ,txn status for Failure Txn after creation of Subscription" )
    public void TC_001_Native_MF_Subs_FailureTransaction(@Optional("true") Boolean isNativePlus) throws Exception{

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.MF_SIP_NEW_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("17")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("100")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("BI_MONTHLY")
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

        TxnStatus txnStatus = new TxnStatus(merchant.getId(), processTxnV1Request.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(processTxnV1Request.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_FAILURE")
                .validateTxnType("SALE")
                .validateGatewayName("PPBL")
                .validateMid(merchant.getId())
                .validatePaymentMode("UPI")
                .validateRespCode("227")
                .validateRefundAmnt("0.00")
                .validateSubsid(subsId)
                .validateTxnDate(new Date())
                .AssertAll();

    }


    /*----------------------------------------------------------------------------*/
    /*            Test cases For Native Subscription RequestType                  */
    /*----------------------------------------------------------------------------*/


    @Override
    @Parameters({"isNativePlus"})
    @Test(description = "Verify txn token and subs id generated successfully when " +
            "requestType=NATIVE_SUBS, freq=DAY")
    public void TC_001_Native_Subs_FreqUnitDay(@Optional("true") Boolean isNativePlus) throws Exception {

        String date = CommonUtils.getdate("yyyy-MM-dd");
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_NATIVE_SUBS;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("DAY")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.addDays(date, "yyyy-MM-dd", 1))
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

        TxnStatus txnStatus = new TxnStatus(merchant.getId(), processTxnV1Request.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(processTxnV1Request.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("PPBL")
                .validateMid(merchant.getId())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateRefundAmnt("0.00")
                .validateSubsid(subsId)
                .validateTxnDate(new Date())
                .AssertAll();


        assertion.apply(peonWait.apply(() -> peons.getAt(processTxnV1Request.getBody().getOrderId()) != null));
        Peon peon = peons.getAt(processTxnV1Request.getBody().getOrderId());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "SUBS_ID"),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.gatewayName().equals("PPBL"),
                peon.mercUnqRef().equals(""),
                peon.mId().equals(merchant.getId()),
                peon.orderId().equals(processTxnV1Request.getBody().getOrderId()),
                peon.payMode().equals("UPI"),
                peon.respCode().equals("01"),
                peon.status().equals("TXN_SUCCESS"),
                peon.txnAmt().equals(initTxnDTO.txnAmountFromBody()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not(),
                peon.subsId().equals(subsId)

        );
        sAssert.eval();

    }


    @Override
    @Parameters({"isNativePlus"})
    @Test(description = "Verify txn token and subs id generated successfully when " +
            "requestType=NATIVE_SUBS, freq=MONTH")
    public void TC_002_Native_Subs_FreqUnitMonth(@Optional("true") Boolean isNativePlus) throws Exception {

        String date = CommonUtils.getdate("yyyy-MM-dd");
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_NATIVE_SUBS;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("30")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.addMonths(date, "yyyy-MM-dd", 1))
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

        TxnStatus txnStatus = new TxnStatus(merchant.getId(), processTxnV1Request.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(processTxnV1Request.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("PPBL")
                .validateMid(merchant.getId())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateRefundAmnt("0.00")
                .validateSubsid(subsId)
                .validateTxnDate(new Date())
                .AssertAll();


        assertion.apply(peonWait.apply(() -> peons.getAt(processTxnV1Request.getBody().getOrderId()) != null));
        Peon peon = peons.getAt(processTxnV1Request.getBody().getOrderId());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "SUBS_ID"),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.gatewayName().equals("PPBL"),
                peon.mercUnqRef().equals(""),
                peon.mId().equals(merchant.getId()),
                peon.orderId().equals(processTxnV1Request.getBody().getOrderId()),
                peon.payMode().equals("UPI"),
                peon.respCode().equals("01"),
                peon.status().equals("TXN_SUCCESS"),
                peon.txnAmt().equals(initTxnDTO.txnAmountFromBody()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not(),
                peon.subsId().equals(subsId)

        );
        sAssert.eval();

    }

    @Override
    @Parameters({"isNativePlus"})
    @Test(description = "Verify txn token and subs id generated successfully when " +
            "requestType=NATIVE_SUBS, freq=YEAR")
    public void TC_003_Native_Subs_FreqUnitYear(@Optional("true") Boolean isNativePlus) throws Exception {

        String date = CommonUtils.getdate("yyyy-MM-dd");
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_NATIVE_SUBS;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("YEAR")
                .setSubscriptionGraceDays("365")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.addYears(date, "yyyy-MM-dd", 1))
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

        TxnStatus txnStatus = new TxnStatus(merchant.getId(), processTxnV1Request.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(processTxnV1Request.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("PPBL")
                .validateMid(merchant.getId())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateRefundAmnt("0.00")
                .validateSubsid(subsId)
                .validateTxnDate(new Date())
                .AssertAll();


        assertion.apply(peonWait.apply(() -> peons.getAt(processTxnV1Request.getBody().getOrderId()) != null));
        Peon peon = peons.getAt(processTxnV1Request.getBody().getOrderId());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "SUBS_ID"),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.gatewayName().equals("PPBL"),
                peon.mercUnqRef().equals(""),
                peon.mId().equals(merchant.getId()),
                peon.orderId().equals(processTxnV1Request.getBody().getOrderId()),
                peon.payMode().equals("UPI"),
                peon.respCode().equals("01"),
                peon.status().equals("TXN_SUCCESS"),
                peon.txnAmt().equals(initTxnDTO.txnAmountFromBody()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not(),
                peon.subsId().equals(subsId)

        );
        sAssert.eval();

    }

    @Override
    @Parameters({"isNativePlus"})
    @Test(description = "Verify txn token and subs id generated successfully when " +
            "requestType=NATIVE_SUBS, freq=QUARTER")
    public void TC_004_Native_Subs_FreqUnitQuater(@Optional("true") Boolean isNativePlus) throws Exception {

        String date = CommonUtils.getdate("yyyy-MM-dd");
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("QUARTER")
                .setSubscriptionGraceDays("2")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.addMonths(date, "yyyy-MM-dd", 3))
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

        TxnStatus txnStatus = new TxnStatus(merchant.getId(), processTxnV1Request.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(processTxnV1Request.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("PPBL")
                .validateMid(merchant.getId())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateRefundAmnt("0.00")
                .validateSubsid(subsId)
                .validateTxnDate(new Date())
                .AssertAll();


        assertion.apply(peonWait.apply(() -> peons.getAt(processTxnV1Request.getBody().getOrderId()) != null));
        Peon peon = peons.getAt(processTxnV1Request.getBody().getOrderId());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "SUBS_ID"),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.gatewayName().equals("PPBL"),
                peon.mercUnqRef().equals(""),
                peon.mId().equals(merchant.getId()),
                peon.orderId().equals(processTxnV1Request.getBody().getOrderId()),
                peon.payMode().equals("UPI"),
                peon.respCode().equals("01"),
                peon.status().equals("TXN_SUCCESS"),
                peon.txnAmt().equals(initTxnDTO.txnAmountFromBody()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not(),
                peon.subsId().equals(subsId)

        );
        sAssert.eval();

    }

    @Override
    @Parameters({"isNativePlus"})
    @Test(description = "Verify txn token and subs id generated successfully when " +
            "requestType=NATIVE_SUBS, freq=BI_MONTHLY")
    public void TC_005_Native_Subs_FreqUnitBiMonthly(@Optional("true") Boolean isNativePlus) throws Exception {


        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_NATIVE_SUBS;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("BI_MONTHLY")
                .setSubscriptionGraceDays("1")
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

        TxnStatus txnStatus = new TxnStatus(merchant.getId(), processTxnV1Request.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(processTxnV1Request.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("PPBL")
                .validateMid(merchant.getId())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateSubsid(subsId)
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();


        assertion.apply(peonWait.apply(() -> peons.getAt(processTxnV1Request.getBody().getOrderId()) != null));
        Peon peon = peons.getAt(processTxnV1Request.getBody().getOrderId());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "SUBS_ID"),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.gatewayName().equals("PPBL"),
                peon.mercUnqRef().equals(""),
                peon.mId().equals(merchant.getId()),
                peon.orderId().equals(processTxnV1Request.getBody().getOrderId()),
                peon.payMode().equals("UPI"),
                peon.respCode().equals("01"),
                peon.status().equals("TXN_SUCCESS"),
                peon.txnAmt().equals(initTxnDTO.txnAmountFromBody()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not(),
                peon.subsId().equals(subsId)

        );
        sAssert.eval();
    }

    @Override
    @Parameters({"isNativePlus"})
    @Test(description = "Verify txn token and subs id generated successfully when " +
            "requestType=NATIVE_SUBS, freq=SEMI_ANNUALLY")
    public void TC_006_Native_Subs_FreqUnitSemiAnually(@Optional("true") Boolean isNativePlus) throws Exception {


        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_NATIVE_SUBS;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("SEMI_ANNUALLY")
                .setSubscriptionGraceDays("2")
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

        TxnStatus txnStatus = new TxnStatus(merchant.getId(), processTxnV1Request.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(processTxnV1Request.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("PPBL")
                .validateMid(merchant.getId())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateRefundAmnt("0.00")
                .validateSubsid(subsId)
                .validateTxnDate(new Date())
                .AssertAll();


        assertion.apply(peonWait.apply(() -> peons.getAt(processTxnV1Request.getBody().getOrderId()) != null));
        Peon peon = peons.getAt(processTxnV1Request.getBody().getOrderId());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "SUBS_ID"),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.gatewayName().equals("PPBL"),
                peon.mercUnqRef().equals(""),
                peon.mId().equals(merchant.getId()),
                peon.orderId().equals(processTxnV1Request.getBody().getOrderId()),
                peon.payMode().equals("UPI"),
                peon.respCode().equals("01"),
                peon.status().equals("TXN_SUCCESS"),
                peon.txnAmt().equals(initTxnDTO.txnAmountFromBody()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not(),
                peon.subsId().equals(subsId)

        );
        sAssert.eval();
    }


    @Override
    @Parameters({"isNativePlus"})
    @Test(description = "Verify txn token and subs id generated successfully when " +
            "requestType=NATIVE_SUBS, freq=SEMI_ANNUALLY")
    public void TC_007_Native_Subs_FreqUnitWeek(@Optional("true") Boolean isNativePlus) throws Exception {


        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_NATIVE_SUBS;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("5000")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("5000")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("WEEK")
                .setSubscriptionGraceDays("2")
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

        TxnStatus txnStatus = new TxnStatus(merchant.getId(), processTxnV1Request.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(processTxnV1Request.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("PPBL")
                .validateMid(merchant.getId())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateRefundAmnt("0.00")
                .validateSubsid(subsId)
                .validateTxnDate(new Date())
                .AssertAll();


        assertion.apply(peonWait.apply(() -> peons.getAt(processTxnV1Request.getBody().getOrderId()) != null));
        Peon peon = peons.getAt(processTxnV1Request.getBody().getOrderId());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "SUBS_ID"),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.gatewayName().equals("PPBL"),
                peon.mercUnqRef().equals(""),
                peon.mId().equals(merchant.getId()),
                peon.orderId().equals(processTxnV1Request.getBody().getOrderId()),
                peon.payMode().equals("UPI"),
                peon.respCode().equals("01"),
                peon.status().equals("TXN_SUCCESS"),
                peon.txnAmt().equals(initTxnDTO.txnAmountFromBody()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not(),
                peon.subsId().equals(subsId)

        );
        sAssert.eval();
    }

    @Owner("akshat.sharma")
    @Override
    @Parameters({"isNativePlus"})
    @Test(description = "Verify txn token and subs id generated successfully when" +
            "requestType=NATIVE_SUBS, freq=ONDEMAND")
    public void TC_008_Native_Subs_FreqUnitOnDemand(@Optional("true") Boolean isNativePlus) throws Exception {

        String date = CommonUtils.getdate("yyyy-MM-dd");
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_NATIVE_SUBS;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.addMonths(date, "yyyy-MM-dd", 1))
                .setRequestType(NATIVE_SUBSCRIPTION)
                .build();


        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);

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

        TxnStatus txnStatus = new TxnStatus(merchant.getId(), processTxnV1Request.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(processTxnV1Request.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("PPBL")
                .validateMid(merchant.getId())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateRefundAmnt("0.00")
                .validateSubsid(subsId)
                .validateTxnDate(new Date())
                .AssertAll();

        assertion.apply(peonWait.apply(() -> peons.getAt(processTxnV1Request.getBody().getOrderId()) != null));
        Peon peon = peons.getAt(processTxnV1Request.getBody().getOrderId());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "SUBS_ID"),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.gatewayName().equals("PPBL"),
                peon.mercUnqRef().equals(""),
                peon.mId().equals(merchant.getId()),
                peon.orderId().equals(processTxnV1Request.getBody().getOrderId()),
                peon.payMode().equals("UPI"),
                peon.respCode().equals("01"),
                peon.status().equals("TXN_SUCCESS"),
                peon.txnAmt().equals(initTxnDTO.txnAmountFromBody()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not(),
                peon.subsId().equals(subsId)

        );
        sAssert.eval();

    }

    /*Frequency Related TestCases*/


    @Override
    @Parameters({"isNativePlus"})
    @Test(description = "Verify subscriptionFrequencyUnit when Freq>1 " +
            "requestType=NATIVE_SUBS")
    public void TC_001_Native_Subs_FreqGreaterThn1(@Optional("true") Boolean isNativePlus) throws Exception {

        String date = CommonUtils.getdate("yyyy-MM-dd");
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_NATIVE_SUBS;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("2")
                .setSubscriptionFrequencyUnit("YEAR")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.addYears(date, "yyyy-MM-dd", 1))
                .setRequestType(NATIVE_SUBSCRIPTION)
                .build();

        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);

        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultStatus())
                .as("resultStatus mismatch")
                .isEqualTo("F");
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultCode())
                .as("resultCode mismatch")
                .isEqualTo("4001");
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultMsg())
                .as("resultMsg mismatch")
                .isEqualToIgnoringCase("Invalid Subscription Frequency");


    }

    @Override
    @Parameters({"isNativePlus"})
    @Test(description = "Verify Freq>0 ,SubsPaymode =Unknown " +
            "requestType=NATIVE_SUBS UPI Paymode is filtered in FPO")
    public void TC_002_Native_Subs_FreqGreaterthan1_PaymodeBlank(@Optional("true") Boolean isNativePlus) throws Exception {

        String date = CommonUtils.getdate("yyyy-MM-dd");
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("2")
                .setSubscriptionFrequencyUnit("YEAR")
                .setSubscriptionGraceDays("320")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.addYears(date, "yyyy-MM-dd", 1))
                .setRequestType(NATIVE_SUBSCRIPTION)
                .build();


        InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);

        String txnToken = responseDTO.getBody().getTxnToken();


        FetchPaymentOptResponseDTO fetchPaymentOptResponseDTO = NativeHelpers.fetchPaymentOptionResponse(txnToken, merchant.getId(), initTxnDTO.orderFromBody());
        Body body = fetchPaymentOptResponseDTO.getBody();
        Assertions.assertThat(body.getResultInfo().getResultCode())
                .as("resultCode mismatch")
                .isEqualToIgnoringCase("0000");
        List<PaymentModes> paymentModes = body.getMerchantPayOption().getPaymentModes();
        boolean payUPI = false;
        for(PaymentModes paymentMode : paymentModes) {
            if(paymentMode.getPaymentMode().equalsIgnoreCase("UPI"))
                payUPI=true;
        }
        Assertions.assertThat(payUPI).isFalse().as("UPI is not getting filtered out when Frequency>1");

    }

    @Override
    @Parameters({"isNativePlus"})
    @Test(description = "Verify Freq=0 Subscription Validation Fails" +
            "requestType=NATIVE_SUBS freq is Mandatory")
    public void TC_003_Native_Subs_FreqEqualToZero(@Optional("true") Boolean isNativePlus) throws Exception {


        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_NATIVE_SUBS;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("0")
                .setSubscriptionFrequencyUnit("YEAR")
                .setSubscriptionGraceDays("320")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_SUBSCRIPTION)
                .build();


        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);

        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultStatus())
                .as("resultStatus mismatch")
                .isEqualTo("F");
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultCode())
                .as("resultCode mismatch")
                .isEqualTo("4001");
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultMsg())
                .as("resultMsg mismatch")
                .isEqualToIgnoringCase("Grace days cannot be greater than the frequency set against the subscription");


    }

    @Override
    @Parameters({"isNativePlus"})
    @Test(description = "Verify Freq is Blank By Default it will consider it 1" +
            "requestType=NATIVE_SUBS")
    public void TC_004_Native_Subs_FreqIsBlank(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("29")
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

        TxnStatus txnStatus = new TxnStatus(merchant.getId(), processTxnV1Request.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(processTxnV1Request.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("PPBL")
                .validateMid(merchant.getId())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateRefundAmnt("0.00")
                .validateSubsid(subsId)
                .validateTxnDate(new Date())
                .AssertAll();


    }



    /*Start Date Related TestCases*/


    @Override
    @Parameters({"isNativePlus"})
    @Test(description = "Verify StartDate is Null not Supported in UPI " +
            "requestType=NATIVE_SUBS")
    public void TC_001_Native_Subs_StartDateIsNull(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_NATIVE_SUBS;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("YEAR")
                .setSubscriptionGraceDays("325")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate("")
                .setRequestType(NATIVE_SUBSCRIPTION)
                .build();
        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);

        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultStatus())
                .as("resultStatus mismatch")
                .isEqualTo("F");
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultCode())
                .as("resultCode mismatch")
                .isEqualTo("4001");
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultMsg())
                .as("resultMsg mismatch")
                .isEqualToIgnoringCase("Invalid Subscription start date");


    }

    @Override
    @Parameters({"isNativePlus"})
    @Test(description = "Verify StartDate is equal to Future Date Supported in UPI " +
            "requestType=NATIVE_SUBS")
    public void TC_002_Native_Subs_StartDateIsFuture(@Optional("true") Boolean isNativePlus) throws Exception {


        String date = CommonUtils.getdate("yyyy-MM-dd");
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("YEAR")
                .setSubscriptionGraceDays("321")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.addYears(date, "yyyy-MM-dd", 1))
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

        TxnStatus txnStatus = new TxnStatus(merchant.getId(), processTxnV1Request.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(processTxnV1Request.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("PPBL")
                .validateMid(merchant.getId())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateSubsid(subsId)
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Override
    @Parameters({"isNativePlus"})
    @Test(description = "Verify blank subscriptionGraceDays are not supported ")
    public void TC_003_Native_Subs_StartDateGraceDayNull(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_NATIVE_SUBS;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_SUBSCRIPTION)
                .build();
        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);

        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultStatus())
                .as("resultStatus mismatch")
                .isEqualTo("F");
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultCode())
                .as("resultCode mismatch")
                .isEqualTo("4001");
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultMsg())
                .as("resultMsg mismatch")
                .isEqualToIgnoringCase("Grace days value is mandatory");
     /*   InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);

        String txnToken = responseDTO.getBody().getTxnToken();
        String subsId = responseDTO.getBody().getSubscriptionId();


        FetchPaymentOptResponseDTO fetchPaymentOptResponseDTO = NativeHelpers.fetchPaymentOptionResponse(txnToken, merchant.getId(), initTxnDTO.orderFromBody());
        Body body = fetchPaymentOptResponseDTO.getBody();
        Assertions.assertThat(body.getResultInfo().getResultCode())
                .as("resultCode mismatch")
                .isEqualToIgnoringCase("0000");
        List<PaymentModes> paymentModes = body.getMerchantPayOption().getPaymentModes();
        boolean payUPI = false;
        for(PaymentModes paymentMode : paymentModes) {
            if(paymentMode.getPaymentMode().equalsIgnoreCase("UPI"))
                payUPI=true;
        }
        Assertions.assertThat(payUPI).isFalse().as("UPI is not getting filtered out when " +
                "Subs Start and Grace day is blank");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchant.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("UPI_INTENT")
                .build();

        ProcessTxnV1Response Resp = NativeHelpers.executeProcessTxnV1(processTxnV1Request);

        Assertions.assertThat(Resp.getBody().getResultInfo().getResultMsg())
                .isEqualTo("BHIM UPI is not allowed for this transaction, kindly use some other payment mode");

        TxnStatus txnStatus = new TxnStatus(merchant.getId(), processTxnV1Request.getBody().getOrderId());
        txnStatus.execute();
        txnStatus.validateOrderid(processTxnV1Request.getBody().getOrderId())
                .validateStatus("PENDING")
                .validateMid(merchant.getId())
                .validateRespCode("402")
                .AssertAll();*/

    }

    @Override
    @Parameters({"isNativePlus"})
    @Test(description = "Verify subscriptionGraceDays:1 subscriptionStartDate: , invalid Subscription start date" +
            "requestType=NATIVE_SUBS")
    public void TC_004_Native_Subs_StartDateIsOnlyBlank(@Optional("true") Boolean isNativePlus) throws Exception {


        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_NATIVE_SUBS;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("2")
                .setSubscriptionFrequencyUnit("DAY")
                .setSubscriptionGraceDays("1")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(null)
                .setRequestType(NATIVE_SUBSCRIPTION)
                .build();


        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);


        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultStatus())
                .as("resultStatus mismatch")
                .isEqualTo("F");
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultCode())
                .as("resultCode mismatch")
                .isEqualTo("4001");
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultMsg())
                .as("resultMsg mismatch")
                .isEqualToIgnoringCase("Invalid Subscription start date");


    }


    /*Enable Retry TestCases*/
    @Override
    @Parameters({"isNativePlus"})
    @Test(description = "Verify when Paymode=UPI SubsRetry >2  , invalid Subscription retry count" +
            "requestType=NATIVE_SUBS")
    public void TC_001_Native_Subs_EnableRetryGreaterThan2(@Optional("true") Boolean isNativePlus) throws Exception {

        String date = CommonUtils.getdate("yyyy-MM-dd");
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_NATIVE_SUBS;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("2")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("3")
                .setSubscriptionStartDate(CommonHelpers.addMonths(date, "yyyy-MM-dd", 1))
                .setRequestType(NATIVE_SUBSCRIPTION)
                .build();

        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultStatus())
                .as("resultStatus mismatch")
                .isEqualTo("F");
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultCode())
                .as("resultCode mismatch")
                .isEqualTo("4001");
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultMsg())
                .as("resultMsg mismatch")
                .isEqualToIgnoringCase("invalid Subscription retry count");


    }

    @Override
    @Parameters({"isNativePlus"})
    @Test(description = "Verify when Paymode=UPI SubsRetry >2 Paymode=null , BHIM UPI is not allowed for this transaction, " +
            "kindly use some other payment mode requestType=NATIVE_SUBS")
    public void TC_002_Native_Subs_EnableRetryGreaterThan2PaymodeBlank(@Optional("true") Boolean isNativePlus) throws Exception {

        String date = CommonUtils.getdate("yyyy-MM-dd");
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_NATIVE_SUBS;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("3")
                .setSubscriptionStartDate(CommonHelpers.addMonths(date, "yyyy-MM-dd", 1))
                .setRequestType(NATIVE_SUBSCRIPTION)
                .build();

        InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);

        String txnToken = responseDTO.getBody().getTxnToken();
        FetchPaymentOptResponseDTO fetchPaymentOptResponseDTO = NativeHelpers.fetchPaymentOptionResponse(txnToken, merchant.getId(), initTxnDTO.orderFromBody());
        Body body = fetchPaymentOptResponseDTO.getBody();
        Assertions.assertThat(body.getResultInfo().getResultCode())
                .as("resultCode mismatch")
                .isEqualToIgnoringCase("0000");
        List<PaymentModes> paymentModes = body.getMerchantPayOption().getPaymentModes();
        boolean payUPI = false;
        for(PaymentModes paymentMode : paymentModes) {
            if(paymentMode.getPaymentMode().equalsIgnoreCase("UPI"))
                payUPI=true;
        }
        Assertions.assertThat(payUPI).isFalse().as("UPI is not getting filtered out when SunsRetry>2");

    }



    /*Subscription Max Amount TestCases*/

    //UPI Limit for Subs has been increased to 25k - PGP-36177
    @Override
    @Parameters({"isNativePlus"})
    @Test(description = "If subscriptionMaxAmount>5000 and txn amount>5000, and paymode=null," +
            "BHIM UPI is allowed for this transaction as UPI Subs limit is 25000 now")
    public void TC_001_Native_Subs_MaxAmountGreaterThan5000PaymodeNull(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("5500")
                .setSubscriptionPaymentMode("")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("5500")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("YEAR")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_SUBSCRIPTION)
                .build();


        InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);

        String txnToken = responseDTO.getBody().getTxnToken();

        FetchPaymentOptResponseDTO fetchPaymentOptResponseDTO = NativeHelpers.fetchPaymentOptionResponse(txnToken, merchant.getId(), initTxnDTO.orderFromBody());
        Body body = fetchPaymentOptResponseDTO.getBody();
        Assertions.assertThat(body.getResultInfo().getResultCode())
                .as("resultCode mismatch")
                .isEqualToIgnoringCase("0000");
        List<PaymentModes> paymentModes = body.getMerchantPayOption().getPaymentModes();
        boolean payUPI = false;
        for(PaymentModes paymentMode : paymentModes) {
            if(paymentMode.getPaymentMode().equalsIgnoreCase("UPI"))
                payUPI=true;
        }
   //     Assertions.assertThat(payUPI).isFalse().as("UPI is not getting filtered out when subscriptionMaxAmount>5500");

        Assertions.assertThat(payUPI).isTrue().as("UPI Breach limit is 25000 now");


    }


    //Limit for UPI has been increased to 25k now - PGP-36177
    @Override
    @Parameters({"isNativePlus"})
    @Test(description = "If subscriptionMaxAmount>25000 and txn amount>25000, and paymode=UPI," +
            "resultMsg: Subscription Amount Limit For UPI Breached requestType=NATIVE_SUBS")
    public void TC_002_Native_Subs_MaxAmountGreaterThan5000PaymodeUPI(@Optional("true") Boolean isNativePlus) throws Exception {


        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_NATIVE_SUBS;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("100500")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("100500")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("DAY")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_SUBSCRIPTION)
                .build();


        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);

        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultStatus())
                .as("resultStatus mismatch")
                .isEqualTo("F");
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultCode())
                .as("resultCode mismatch")
                .isEqualTo("4001");
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultMsg())
                .as("resultMsg mismatch")
                .isEqualToIgnoringCase("Subscription Amount Limit For UPI Breached");


    }


    @Override
    @Parameters({"isNativePlus"})
    @Test(description = "If txn amount>Subscription amount, and paymode=UPI," +
            "requestType=NATIVE_SUBS")
    public void TC_003_Native_Subs_TxnAmountGreaterThanMaxAmount(@Optional("true") Boolean isNativePlus) throws Exception {

        String date = CommonUtils.getdate("yyyy-MM-dd");
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_NATIVE_SUBS;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1300")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("1000")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.addMonths(date, "yyyy-MM-dd", 1))
                .setRequestType(NATIVE_SUBSCRIPTION)
                .build();


        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);

        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultStatus())
                .as("resultStatus mismatch")
                .isEqualTo("F");
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultCode())
                .as("resultCode mismatch")
                .isEqualTo("2022");
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultMsg())
                .as("resultMsg mismatch")
                .isEqualToIgnoringCase("Paymode selected is not applicable when txn amount is greater than the renewal amount");


    }


    @Issue("PGP-26277")
    @Test(description = "If subscriptionMaxAmount>5000 and txn amount<5000, and paymode=null," +
            "requestType=NATIVE_MF_SIP", groups = Group.Status.BUG)
    public void TC_004_Native_Subs_OnlyMaxAmountGreaterThanMaxAmount(@Optional("true") Boolean isNativePlus) throws Exception{


        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_NATIVE_SUBS;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1300")
                .setSubscriptionPaymentMode("")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("5500")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("DAY")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_SUBSCRIPTION)
                .build();


        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        String txnToken = responseDTO.getBody().getTxnToken();
        FetchPaymentOptResponseDTO fetchPaymentOptResponseDTO = NativeHelpers.fetchPaymentOptionResponse(txnToken, merchant.getId(), initTxnDTO.orderFromBody());
        Body body = fetchPaymentOptResponseDTO.getBody();
        Assertions.assertThat(body.getResultInfo().getResultCode())
                .as("resultCode mismatch")
                .isEqualToIgnoringCase("0000");
        List<PaymentModes> paymentModes = body.getMerchantPayOption().getPaymentModes();
        boolean payUPI = false;
        for(PaymentModes paymentMode : paymentModes) {
            if(paymentMode.getPaymentMode().equalsIgnoreCase("UPI"))
                payUPI=true;
        }
        Assertions.assertThat(payUPI).isFalse().as("UPI is not getting filtered out when subscriptionMaxAmount>5000");
    }

    /*Amount Type TestCases*/


    @Override
    @Parameters({"isNativePlus"})
    @Test(description = "For FIX amount type,if  equal amount is not passed for txn amount & max amount if payment mode=UPI," +
            " resultMsg :Transaction amount is not equal to max amount set against the subscription")
    public void TC_001_Native_Subs_FixAmountEqualAmountNotPassedUPI(@Optional("true") Boolean isNativePlus) throws Exception {

        String date = CommonUtils.getdate("yyyy-MM-dd");


        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_NATIVE_SUBS;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("20")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("2")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("YEAR")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.addYears(date, "yyyy-MM-dd", 1))
                .setRequestType(NATIVE_SUBSCRIPTION)
                .build();


        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);

        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultStatus())
                .as("resultStatus mismatch")
                .isEqualTo("F");
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultCode())
                .as("resultCode mismatch")
                .isEqualTo("2022");
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultMsg())
                .as("resultMsg mismatch")
                .isEqualToIgnoringCase("Paymode selected is not applicable when txn amount is greater than the renewal amount");

    }

    @Override
    @Feature("PGP-33945")
    @Parameters({"isNativePlus"})
    @Test(description = "For FIX amount type,if  equal amount is not passed for txn amount & max amount if payment mode=null," +
            " then it should fail at the time of process txn ")
    public void TC_002_Native_Subs_FixAmountEqualAmountNotPassed(@Optional("true") Boolean isNativePlus) throws Exception {


        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("2")
                .setSubscriptionPaymentMode("")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("20")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("YEAR")
                .setSubscriptionGraceDays("29")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_SUBSCRIPTION)
                .build();

        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);

        String txnToken = responseDTO.getBody().getTxnToken();

        FetchPaymentOptResponseDTO fetchPaymentOptResponseDTO = NativeHelpers.fetchPaymentOptionResponse(txnToken, merchant.getId(), initTxnDTO.orderFromBody());
        boolean paymodeStatus = NativeHelpers.isFetchPaymentOptionStatusMatched(fetchPaymentOptResponseDTO, "UPI", false);
        Assertions.assertThat(paymodeStatus).as("UPI paymode status mismatch").isFalse();

    }


    @Override
    @Parameters({"isNativePlus"})
    @Test(description = "For the Variable amount, the subscription txn amount should be" +
            " less than or equal to the subscription max amount")
    public void TC_003_Native_Subs_VariableAmountEqualOrLessMaxAmount(@Optional("true") Boolean isNativePlus) throws Exception {


        String date = CommonUtils.getdate("yyyy-MM-dd");
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("YEAR")
                .setSubscriptionGraceDays("326")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.addYears(date, "yyyy-MM-dd", 1))
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

        TxnStatus txnStatus = new TxnStatus(merchant.getId(), processTxnV1Request.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(processTxnV1Request.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("PPBL")
                .validateMid(merchant.getId())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateRefundAmnt("0.00")
                .validateSubsid(subsId)
                .validateTxnDate(new Date())
                .AssertAll();
    }


    @Test(description = "For the Variable amount, the subscription txn amount greater" +
            "  than subscription max amount " )
    public void TC_004_Native_Subs_VariableAmountGreater(@Optional("true") Boolean isNativePlus) throws Exception{


        String date = CommonUtils.getdate("yyyy-MM-dd");


        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_NATIVE_SUBS;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("20")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("2")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("YEAR")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.addYears(date, "yyyy-MM-dd", 1))
                .setRequestType(NATIVE_SUBSCRIPTION)
                .build();


        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);

        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultStatus())
                .as("resultStatus mismatch")
                .isEqualTo("F");
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultCode())
                .as("resultCode mismatch")
                .isEqualTo("2022");
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultMsg())
                .as("resultMsg mismatch")
                .isEqualToIgnoringCase("Paymode selected is not applicable when txn amount is greater than the renewal amount");
    }


    @Test(description = "Verify response validation ,txn status for Failure Txn after creation of Subscription" )
    public void TC_001_Native_Subs_FailureTransaction(@Optional("true") Boolean isNativePlus) throws Exception{

        String date = CommonUtils.getdate("yyyy-MM-dd");
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.MF_SIP_NEW_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("17")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("100")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("SEMI_ANNUALLY")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.addMonths(date,"yyyy-MM-dd",6))
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

        TxnStatus txnStatus = new TxnStatus(merchant.getId(), processTxnV1Request.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(processTxnV1Request.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_FAILURE")
                .validateTxnType("SALE")
                .validateGatewayName("PPBL")
                .validateMid(merchant.getId())
                .validatePaymentMode("UPI")
                .validateRespCode("227")
                .validateRefundAmnt("0.00")
                .validateSubsid(subsId)
                .validateTxnDate(new Date())
                .AssertAll();



    }

    /* ONDEMAND VALIDATIONS PGP-26500*/
    @Owner("akshat.sharma")
    @Override
    @Parameters({"isNativePlus"})
    @Test(description = "Verify that Frequency, StartDate, GracePeriod, RetryAllowed & RetryCount are ignored when" +
            "requestType=NATIVE_MF_SIP, freqUnit=ONDEMAND")
    public void TC_001_Native_MF_Subs_OnDemandValidations(@Optional("true") Boolean isNativePlus) throws Exception {


        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_MUTUAL_MF;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_MF_SIP)
                .build();


        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);

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

        TxnStatus txnStatus = new TxnStatus(merchant.getId(), processTxnV1Request.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(processTxnV1Request.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("PPBL")
                .validateMid(merchant.getId())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateRefundAmnt("0.00")
                .validateSubsid(subsId)
                .validateTxnDate(new Date())
                .AssertAll();

        assertion.apply(peonWait.apply(() -> peons.getAt(processTxnV1Request.getBody().getOrderId()) != null));
        Peon peon = peons.getAt(processTxnV1Request.getBody().getOrderId());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "SUBS_ID"),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.gatewayName().equals("PPBL"),
                peon.mercUnqRef().equals(""),
                peon.mId().equals(merchant.getId()),
                peon.orderId().equals(processTxnV1Request.getBody().getOrderId()),
                peon.payMode().equals("UPI"),
                peon.respCode().equals("01"),
                peon.status().equals("TXN_SUCCESS"),
                peon.txnAmt().equals(initTxnDTO.txnAmountFromBody()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not(),
                peon.subsId().equals(subsId)

        );
        sAssert.eval();
    }

    @Owner("akshat.sharma")
    @Override
    @Parameters({"isNativePlus"})
    @Test(description = "Verify txn token and subs id generated successfully when" +
            "requestType=NATIVE_SUBS, freq=ONDEMAND")
    public void TC_001_Native_Subs_OnDemandValidations(@Optional("true") Boolean isNativePlus) throws Exception {

        String date = CommonUtils.getdate("yyyy-MM-dd");
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_NATIVE_SUBS;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.addMonths(date, "yyyy-MM-dd", 1))
                .setRequestType(NATIVE_SUBSCRIPTION)
                .build();


        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);

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

        TxnStatus txnStatus = new TxnStatus(merchant.getId(), processTxnV1Request.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(processTxnV1Request.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("PPBL")
                .validateMid(merchant.getId())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateRefundAmnt("0.00")
                .validateSubsid(subsId)
                .validateTxnDate(new Date())
                .AssertAll();

        assertion.apply(peonWait.apply(() -> peons.getAt(processTxnV1Request.getBody().getOrderId()) != null));
        Peon peon = peons.getAt(processTxnV1Request.getBody().getOrderId());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "SUBS_ID"),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.gatewayName().equals("PPBL"),
                peon.mercUnqRef().equals(""),
                peon.mId().equals(merchant.getId()),
                peon.orderId().equals(processTxnV1Request.getBody().getOrderId()),
                peon.payMode().equals("UPI"),
                peon.respCode().equals("01"),
                peon.status().equals("TXN_SUCCESS"),
                peon.txnAmt().equals(initTxnDTO.txnAmountFromBody()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not(),
                peon.subsId().equals(subsId)

        );
        sAssert.eval();
    }

    @Feature("PGP-32799")
    @Owner(Constants.Owner.AKSHAT)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify that sub-error code is displayed in MF_SIP transaction Status response")
    public void TC_001_NativeMF_SIP_Subs_Failure_ErrorCode(@Optional("true") Boolean isNativePlus) throws Exception {

        String date = CommonUtils.getdate("yyyy-MM-dd");
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_MUTUAL_MF;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("17")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("100")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("1")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.addMonths(date, "yyyy-MM-dd", 1))
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
        Thread.sleep(30000);
        GetPaymentStatusDTO getPaymentStatusDTOFail = new GetPaymentStatusDTO.Builder
                (initTxnDTO.orderFromBody(), merchant.getId(), merchant.getKey())
                .build();
        GetPaymentStatus getPaymentStatusFail = new GetPaymentStatus(getPaymentStatusDTOFail);
        Response responseFail = getPaymentStatusFail.execute();
        JsonPath jsonPathFail = responseFail.jsonPath();
        Assertions.assertThat(jsonPathFail.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("TXN_FAILURE");
        Assertions.assertThat(jsonPathFail.getString("body.resultInfo.resultCode")).isEqualTo("227");
        Assertions.assertThat(jsonPathFail.getString("body.subResultInfo.resultCode")).isEqualTo("SE110");
        Assertions.assertThat(jsonPathFail.getString("body.mid")).isEqualToIgnoringCase(merchant.getId());
        Assertions.assertThat(jsonPathFail.getString("body.orderId")).isEqualToIgnoringCase(initTxnDTO.orderFromBody());
        Assertions.assertThat(jsonPathFail.getString("body.txnAmount")).isEqualToIgnoringCase("17.00");

    }

    @Feature("PGP-32799")
    @Owner(Constants.Owner.AKSHAT)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify that sub-error code is displayed in NATIVE_SUBS transaction Status response")
    public void TC_001_Native_Subs_Failure_ErrorCode(@Optional("true") Boolean isNativePlus) throws Exception {

        String date = CommonUtils.getdate("yyyy-MM-dd");
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SIHUB_Subs;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("17")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("100")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.addMonths(date, "yyyy-MM-dd", 1))
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
        Thread.sleep(30000);
        GetPaymentStatusDTO getPaymentStatusDTOFail = new GetPaymentStatusDTO.Builder
                (initTxnDTO.orderFromBody(), merchant.getId(), merchant.getKey())
                .build();
        GetPaymentStatus getPaymentStatusFail = new GetPaymentStatus(getPaymentStatusDTOFail);
        Response responseFail = getPaymentStatusFail.execute();
        JsonPath jsonPathFail = responseFail.jsonPath();
        Assertions.assertThat(jsonPathFail.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("TXN_FAILURE");
        Assertions.assertThat(jsonPathFail.getString("body.resultInfo.resultCode")).isEqualTo("227");
        Assertions.assertThat(jsonPathFail.getString("body.subResultInfo.resultCode")).isEqualTo("SE110");
        Assertions.assertThat(jsonPathFail.getString("body.mid")).isEqualToIgnoringCase(merchant.getId());
        Assertions.assertThat(jsonPathFail.getString("body.orderId")).isEqualToIgnoringCase(initTxnDTO.orderFromBody());
        Assertions.assertThat(jsonPathFail.getString("body.txnAmount")).isEqualToIgnoringCase("17.00");

    }

    @Feature("PGP-40525")
    @Owner(Constants.Owner.VISHNU_SHEKAR)
    @Test(description = "Verify rev=N when MCC Code = 7322 and SUBSCRIPTION_REVOCABLE_FLAG is enabled")
    public void PGP_40525_TC_01_Native_Subs() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("100")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_SUBSCRIPTION)
                .build();


        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);

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

        TxnStatus txnStatus = new TxnStatus(merchant.getId(), processTxnV1Request.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(processTxnV1Request.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("PPBL")
                .validateMid(merchant.getId())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateRefundAmnt("0.00")
                .validateSubsid(subsId)
                .validateTxnDate(new Date())
                .AssertAll();
        String grepcmd = "grep \"DeepLink parameter map for mandate\" /paytm/logs/instaproxy.log | grep " + initTxnDTO.getBody().getOrderId() + "";
        String logs = getLogsOnServer(ServerConfigProvider.SERVICE.INSTAPROXY, grepcmd);
        Assertions.assertThat(logs).contains("rev" + "=" + "N");

        String grepcommand = "grep \"DeepLink parameter map for mandate\" /paytm/logs/instaproxy.log | grep "+ initTxnDTO.getBody().getOrderId() +"";
        String logsInsta = getLogsOnServer(ServerConfigProvider.SERVICE.INSTAPROXY, grepcommand);
        Assertions.assertThat(logsInsta).contains("mc=7322");
    }

    @Feature("PGP-40525")
    @Owner(Constants.Owner.VISHNU_SHEKAR)
    @Test(description = "Verify rev is not passed when MCC Code is other than 7322 and SUBSCRIPTION_REVOCABLE_FLAG is enabled")
    public void PGP_40525_TC_02_Native_Subs() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("100")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_SUBSCRIPTION)
                .build();


        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);

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
        TxnStatus txnStatus = new TxnStatus(merchant.getId(), processTxnV1Request.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(processTxnV1Request.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("PPBL")
                .validateMid(merchant.getId())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateRefundAmnt("0.00")
                .validateSubsid(subsId)
                .validateTxnDate(new Date())
                .AssertAll();
        String grepcmd = "grep \"DeepLink parameter map for mandate\" /paytm/logs/instaproxy.log | grep " + initTxnDTO.getBody().getOrderId() + "";
        String logs = getLogsOnServer(ServerConfigProvider.SERVICE.INSTAPROXY, grepcmd);
        Assertions.assertThat(logs).doesNotContain("rev");

        String grepcommand = "grep \"DeepLink parameter map for mandate\" /paytm/logs/instaproxy.log | grep "+ initTxnDTO.getBody().getOrderId() +"";
        String logsInsta = getLogsOnServer(ServerConfigProvider.SERVICE.INSTAPROXY, grepcommand);
        Assertions.assertThat(logsInsta).doesNotContain("mc=7322");
    }

    @Owner(Constants.Owner.AKSHAT)
    @Feature("PGP-55339")
    @Parameters({"isNativePlus"})
    @Test(description = "Verify that create subscription logs are printed in theia facade logs")
    public void TC_001_subscriptionClientRemoval_createIntentFlow(@Optional("true") Boolean isNativePlus) throws Exception {

        String date = CommonUtils.getdate("yyyy-MM-dd");
       User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_NATIVE_SUBS;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.addMonths(date, "yyyy-MM-dd", 1))
                .setRequestType(NATIVE_SUBSCRIPTION)
                .build();


        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);

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

        TxnStatus txnStatus = new TxnStatus(merchant.getId(), processTxnV1Request.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(processTxnV1Request.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("PPBL")
                .validateMid(merchant.getId())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateRefundAmnt("0.00")
                .validateSubsid(subsId)
                .validateTxnDate(new Date())
                .AssertAll();

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody());
        Assertions.assertThat(logs).contains("\"COMPONENT\": \"SUBSCRIPTION_SERVICE\"");
        Assertions.assertThat(logs).contains(LocalConfig.PGP_HOST +"/subscription/subscription/create");

    }

    @Owner(Constants.Owner.AKSHAT)
    @Feature("PG-7131|PG-7197")
    @Parameters({"isNativePlus"})
    @Test(description = "To verify for NATIVE_MF_SIP transaction additionalInfo passed in COP s")
    public void validateAdditionalInfoInCOP_MF_SIP(@Optional("true") Boolean isNativePlus) throws Exception {

        AdditionalInfo additionalInfo = new AdditionalInfo();

        String date = CommonUtils.getdate("yyyy-MM-dd");
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.Native_MF_ParentA;
        Constants.MerchantType aggMid = Constants.MerchantType.Native_MF_ChildA1;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("100")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("1")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.addMonths(date, "yyyy-MM-dd", 1))
                .setRequestType(NATIVE_MF_SIP)
                .setAggrMid(aggMid.getId())
                .setMerchantKey(merchant.getKey())
                .setAdditionalInfo((additionalInfo.setRef1("ref1_value").setRef2("ref2_value").setRef3("ref3_value").setRef4("ref4_value").setRef5("ref5_value").setRef6("ref6_value").setRef7("ref7_value").setRef8("ref8_value").setRef9("ref9_value").setRef10("ref10_value").setRef11("ref11_value").setRef12("ref12_value")))
                .build();


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
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, initTxnDTO.orderFromBody(), "ACQUIRING_CREATE_ORDER_AND_PAY");
        Assertions.assertThat(logs).contains("mutualFundFeedInfo");
        Assertions.assertThat(logs).contains("ref1_value").contains("ref2_value").contains("ref3_value").contains("ref4_value").contains("ref5_value").contains("ref6_value").contains("ref7_value").contains("ref8_value").contains("ref9_value").contains("ref10_value").contains("ref11_value").contains("ref12_value");


    }

    @Owner(Constants.Owner.AKSHAT)
    @Feature("PG-7131|PG-7197")
    @Parameters({"isNativePlus"})
    @Test(description = "To verify for NATIVE_MF_SIP transaction mutualFundFeedInfo passed in COP")
    public void validateMutualFundFeedInfoInCOP_MF_SIP(@Optional("true") Boolean isNativePlus) throws Exception {

        MutualFundFeedInfo mutualFundFeedInfo = new MutualFundFeedInfo();

        String date = CommonUtils.getdate("yyyy-MM-dd");
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.Native_MF_ParentA;
        Constants.MerchantType aggMid = Constants.MerchantType.Native_MF_ChildA1;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("100")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("1")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.addMonths(date, "yyyy-MM-dd", 1))
                .setRequestType(NATIVE_MF_SIP)
                .setAggrMid(aggMid.getId())
                .setMerchantKey(merchant.getKey())
                .setMutualFundFeedInfo(mutualFundFeedInfo
                        .setMfTxnId("PAYTXN20260602001")
                        .setMfBseBankCode("ICICI")
                        .setMfInternalRef("INT_REF_LUMP_01")
                        .setMfAmcCode("AMC03")
                        .setMfBseMemberId("12345")
                        .setMfUcc("UCC11223344")
                        .setMfSchemeCategoryId("EQUITY_LARGE_CAP")
                        .setMfClientTxnId("CLIENT_LUMP_001")
                        .setMfIfsc("ICIC0001234")
                        .setMfBankAccount("601234567890")
                        .setMfInvestmentType("L")
                        .setMfUpir("upir"))
                .build();


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
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, initTxnDTO.orderFromBody(), "ACQUIRING_CREATE_ORDER_AND_PAY");
        Assertions.assertThat(logs).contains("mutualFundFeedInfo");
        Assertions.assertThat(logs).contains("PAYTXN20260602001").contains("INT_REF_LUMP_01").contains("AMC03").contains("UCC11223344").contains("EQUITY_LARGE_CAP").contains("CLIENT_LUMP_001").contains("ICIC0001234").contains("601234567890");


    }

    @Owner(Constants.Owner.AKSHAT)
    @Feature("PG-7131|PG-7197")
    @Parameters({"isNativePlus"})
    @Test(description = "To verify when both mutualFundFeedInfo and additionalInfo are passed initTransaction fails")
    public void validateInitiateTxnFailure_MF_SIP(@Optional("true") Boolean isNativePlus) throws Exception {

        String date = CommonUtils.getdate("yyyy-MM-dd");
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.Native_MF_ParentA;
        Constants.MerchantType aggMid = Constants.MerchantType.Native_MF_ChildA1;

        Response response = new SubscriptionCreate(new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("100")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("1")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.addMonths(date, "yyyy-MM-dd", 1))
                .setRequestType(NATIVE_MF_SIP)
                .setAggrMid(aggMid.getId())
                .setMerchantKey(merchant.getKey())
                .setMutualFundFeedInfo(new MutualFundFeedInfo()
                        .setMfTxnId("PAYTXN20260602001")
                        .setMfBseBankCode("ICICI")
                        .setMfInternalRef("INT_REF_LUMP_01")
                        .setMfAmcCode("AMC03")
                        .setMfBseMemberId("12345")
                        .setMfUcc("UCC11223344")
                        .setMfSchemeCategoryId("EQUITY_LARGE_CAP")
                        .setMfClientTxnId("CLIENT_LUMP_001")
                        .setMfIfsc("ICIC0001234")
                        .setMfBankAccount("601234567890")
                        .setMfInvestmentType("L")
                        .setMfUpir("upir"))
                .setAdditionalInfo(new AdditionalInfo()
                        .setRef1("ref1_value").setRef2("ref2_value").setRef3("ref3_value").setRef4("ref4_value")
                        .setRef5("ref5_value").setRef6("ref6_value").setRef7("ref7_value").setRef8("ref8_value")
                        .setRef9("ref9_value").setRef10("ref10_value").setRef11("ref11_value").setRef12("ref12_value"))
                .build()).execute();

        Assertions.assertThat(response.getStatusCode())
                .as("Status code is not equal to 200")
                .isEqualTo(200);
        InitTxnResponseDTO responseDTO = NativeHelpers.convertRespToObject(response, InitTxnResponseDTO.class);
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultStatus())
                .as("resultStatus mismatch")
                .isEqualTo("F");
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultCode())
                .as("resultCode mismatch")
                .isEqualTo("1001");
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultMsg())
                .as("resultMsg mismatch")
                .isEqualToIgnoringCase("Request parameters are not valid");

    }


    }

