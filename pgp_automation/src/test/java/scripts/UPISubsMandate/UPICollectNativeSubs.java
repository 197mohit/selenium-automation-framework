package scripts.UPISubsMandate;


import com.paytm.api.GetPaymentStatus;
import com.paytm.api.TxnStatus;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.*;
import com.paytm.base.test.Group;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.GetPaymentStatusRequest.GetPaymentStatusDTO;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.response.Body;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.response.FetchPaymentOptResponseDTO;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.response.PaymentModes;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.framework.conditions.SoftAssertion;
import com.paytm.framework.utils.CommonUtils;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
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
import scripts.Native.PcfHelpher;

import java.util.Date;
import java.util.List;

import static com.paytm.appconstants.Constants.Owner.GAGANDEEP;
import static com.paytm.apphelpers.LogsValidationHelper.getLogsOnServer;

public class UPICollectNativeSubs extends PGPBaseTest implements UPINativeMFSIPTests, UPINativeSubsTests {
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

    @Step()
    private void validateSuccessTxnStatus(InitTxnDTO initTxnDTO,OrderDTO orderDTO,String subsId) {

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("PTYBLC")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateRefundAmnt("0.00")
                .validateSubsid(subsId)
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Step()
    private void validateSuccessResponsePage(InitTxnDTO initTxnDTO,OrderDTO orderDTO,String subsId,String merchantKey) {

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("PTYBLC")
                .validateSubsId(subsId)
                .validateCheckSum(merchantKey)
                .assertAll();
    }

    private void validateSuccessResponsePageHDFC(InitTxnDTO initTxnDTO,OrderDTO orderDTO,String subsId,String merchantKey) {

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("HDFC")
                .validateSubsId(subsId)
                //   .validateCheckSum(merchantKey)
                .assertAll();
    }


    @Step()
    private void validateSuccessPeon(InitTxnDTO initTxnDTO,OrderDTO orderDTO,String subsId) {


        assertion.apply(peonWait.apply(() -> peons.getAt(orderDTO.getORDER_ID()) != null));
        Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
              peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "SUBS_ID"),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.gatewayName().equals("PTYBLC"),
                peon.mercUnqRef().equals(""),
                peon.mId().equals(orderDTO.getMID()),
                peon.orderId().equals(orderDTO.getORDER_ID()),
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






    /*----------------------------------------------------------------------------*/
    /*          Test cases For Native MF SIP Subs RequestType                      */
    /*----------------------------------------------------------------------------*/

    @Override
    @Parameters({"isNativePlus"})
    @Test(description = "Verify txn token and subs id generated successfully when " +
            "requestType=NATIVE_MF_SIP, Paymentmode=UPI, freq=DAY")
    public void TC_001_Native_MF_Subs_FreqUnitDay(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.MF_SIP_NEW_MID;
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

        validateSuccessResponsePage(initTxnDTO,orderDTO,subsId,merchant.getKey());

        validateSuccessTxnStatus(initTxnDTO,orderDTO,subsId);

        validateSuccessPeon(initTxnDTO,orderDTO,subsId);
    }

    @Override
    @Parameters({"isNativePlus"})
    @Test(description = "Verify txn token and subs id generated successfully when " +
            "requestType=NATIVE_MF_SIP, Paymentmode=UPI, freq=MONTH")
    public void TC_002_Native_MF_Subs_FreqUnitMonth(@Optional("true") Boolean isNativePlus) throws Exception {


        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.MF_SIP_NEW_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
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

        validateSuccessResponsePage(initTxnDTO,orderDTO,subsId,merchant.getKey());

        validateSuccessTxnStatus(initTxnDTO,orderDTO,subsId);

        validateSuccessPeon(initTxnDTO,orderDTO,subsId);
    }

    @Override
    @Parameters({"isNativePlus"})
    @Test(description = "Verify txn token and subs id generated successfully when " +
            "requestType=NATIVE_MF_SIP, Paymentmode=UPI, freq=YEAR")
    public void TC_003_Native_MF_Subs_FreqUnitYear(@Optional("true") Boolean isNativePlus) throws Exception {


        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.MF_SIP_NEW_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("YEAR")
                .setSubscriptionGraceDays("365")
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

        validateSuccessResponsePage(initTxnDTO,orderDTO,subsId,merchant.getKey());

        validateSuccessTxnStatus(initTxnDTO,orderDTO,subsId);

        validateSuccessPeon(initTxnDTO,orderDTO,subsId);

    }


    @Override
    @Parameters({"isNativePlus"})
    @Test(description = "Verify txn token and subs id generated successfully when " +
            "requestType=NATIVE_MF_SIP, Paymentmode=UPI, freq=QUARTER")
    public void TC_004_Native_MF_Subs_FreqUnitQuater(@Optional("true") Boolean isNativePlus) throws Exception {


        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.MF_SIP_NEW_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("QUARTER")
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


        validateSuccessResponsePage(initTxnDTO,orderDTO,subsId,merchant.getKey());

        validateSuccessTxnStatus(initTxnDTO,orderDTO,subsId);

        validateSuccessPeon(initTxnDTO,orderDTO,subsId);

    }

    @Override
    @Parameters({"isNativePlus"})
    @Test(description = "Verify txn token and subs id generated successfully when " +
            "requestType=NATIVE_MF_SIP, Paymentmode=UPI, freq=BI_MONTHLY")
    public void TC_005_Native_MF_Subs_FreqUnitBiMonthly(@Optional("true") Boolean isNativePlus) throws Exception {


        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.MF_SIP_NEW_MID;
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

        validateSuccessResponsePage(initTxnDTO,orderDTO,subsId,merchant.getKey());

        validateSuccessTxnStatus(initTxnDTO,orderDTO,subsId);

        validateSuccessPeon(initTxnDTO,orderDTO,subsId);
    }


    @Override
    @Parameters({"isNativePlus"})
    @Test(description = "Verify txn token and subs id generated successfully when " +
            "requestType=NATIVE_MF_SIP, Paymentmode=UPI, freq=SEMI_ANNUALLY")
    public void TC_006_Native_MF_Subs_FreqUnitSemiAnually(@Optional("true") Boolean isNativePlus) throws Exception {


        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.MF_SIP_NEW_MID;
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

        validateSuccessResponsePage(initTxnDTO,orderDTO,subsId,merchant.getKey());

        validateSuccessTxnStatus(initTxnDTO,orderDTO,subsId);

        validateSuccessPeon(initTxnDTO,orderDTO,subsId);

    }

    @Override
    @Parameters({"isNativePlus"})
    @Test(description = "Verify txn token and subs id generated successfully when " +
            "requestType=NATIVE_MF_SIP, freq=WEEK")
    public void TC_007_Native_MF_Subs_FreqUnitWeek(@Optional("true") Boolean isNativePlus) throws Exception{

        String date = CommonUtils.getdate("yyyy-MM-dd");
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.MF_SIP_NEW_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("WEEK")
                .setSubscriptionGraceDays("6")
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

        OrderDTO orderDTO = new OrderFactory.UPIMandate(merchant, txnToken, initTxnDTO.orderFromBody(), subsId)
                .setPayerAccount("test@paytm")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);

        validateSuccessResponsePage(initTxnDTO,orderDTO,subsId,merchant.getKey());

        validateSuccessTxnStatus(initTxnDTO,orderDTO,subsId);

        validateSuccessPeon(initTxnDTO,orderDTO,subsId);
    }

    //ondemamd now supported on UPI

    @Override
    @Parameters({"isNativePlus"})
    @Test(description = "Verify txn token and subs id generated successfully when" +
            "requestType=NATIVE_MF_SIP, freq=ONDEMAND")
    public void TC_008_Native_MF_Subs_FreqUnitOnDemand(@Optional("true") Boolean isNativePlus) throws Exception {


        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.MF_SIP_NEW_MID;
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

        validateSuccessResponsePage(initTxnDTO,orderDTO,subsId,merchant.getKey());

        validateSuccessTxnStatus(initTxnDTO,orderDTO,subsId);

        validateSuccessPeon(initTxnDTO,orderDTO,subsId);


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
                .setTxnValue("1")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
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
                .setTxnValue("1")
                .setSubscriptionPaymentMode("")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("2")
                .setSubscriptionFrequencyUnit("DAY")
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
                .setTxnValue("1")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("0")
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

        OrderDTO orderDTO = new OrderFactory.UPIMandate(merchant, txnToken, initTxnDTO.orderFromBody(), subsId)
                .setPayerAccount("test@paytm")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("PPBLC")
                .validateSubsId(subsId)
                .validateCheckSum(Constants.MerchantType.MF_SIP_NEW_MID.getKey())
                .assertAll();

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
                .setTxnValue("1")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
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
                .setTxnValue("1")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
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

        OrderDTO orderDTO = new OrderFactory.UPIMandate(merchant, txnToken, initTxnDTO.orderFromBody(), subsId)
                .setPayerAccount("test@paytm")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("PPBLC")
                .validateSubsId(subsId)
                .validateCheckSum(Constants.MerchantType.MF_SIP_NEW_MID.getKey())
                .assertAll();

    }

    @Override
    @Parameters({"isNativePlus"})
    @Test(description = "Verify blank subscriptionGraceDays are not supported ")
    public void TC_003_Native_MF_Subs_StartDateGraceDayNull(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_MUTUAL_MF;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
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


        OrderDTO orderDTO = new OrderFactory.UPIMandate(merchant, txnToken, initTxnDTO.orderFromBody(), subsId)
                .setPayerAccount("test@paytm")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateStatus("TXN_FAILURE")
                .validateMid(orderDTO.getMID())
                .validateRespMsg("Invalid payment mode")
                .assertAll();*/


    }

    @Override
    @Parameters({"isNativePlus"})
    @Test(description = "Verify subscriptionGraceDays:1 subscriptionStartDate: , invalid Subscription start date" +
            "requestType=NATIVE_MF_SIP")
    public void TC_004_Native_MF_Subs_StartDateIsOnlyBlank(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_MUTUAL_MF;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
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
                .setTxnValue("1")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
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
    @Test(description = "Verify when SubsRetry >2 Paymode=null , BHIM UPI is not allowed for this transaction, " +
            "kindly use some other payment mode requestType=NATIVE_MF_SIP")
    public void TC_002_Native_MF_Subs_EnableRetryGreaterThan2PaymodeBlank(@Optional("true") Boolean isNativePlus) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.MF_SIP_NEW_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("")
                .setSubscriptionAmountType("VARIABLE")
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
        Assertions.assertThat(payUPI).isFalse().as("UPI is not getting filtered out when SunsRetry>2");

        OrderDTO orderDTO = new OrderFactory.UPIMandate(merchant, txnToken, initTxnDTO.orderFromBody(), subsId)
                .setPayerAccount("test@paytm")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateStatus("TXN_FAILURE")
                .validateMid(orderDTO.getMID())
                .validateRespMsg("Invalid payment mode")
                .assertAll();

    }



    /*Subscription Max Amount TestCases*/

    //UPI Limit for Subs has been increased to 25k - PGP-36177
    @Override
    @Parameters({"isNativePlus"})
    @Test(description = "If subscriptionMaxAmount>5000 and txn amount>5000, and paymode=null," +
            "BHIM UPI should not get Filtered in FPO as UPI Subs Limit is 25000 requestType=NATIVE_MF_SIP")
    public void TC_001_Native_MF_Subs_MaxAmountGreaterThan5000PaymodeNull(@Optional("true") Boolean isNativePlus) throws Exception {


        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.MF_SIP_NEW_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("5500")
                .setSubscriptionPaymentMode("")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("6000")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("DAY")
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
     //   Assertions.assertThat(payUPI).isFalse().as("UPI is not getting filtered out when subscriptionMaxAmount>5000");
        Assertions.assertThat(payUPI).isTrue().as("UPI should not get filtered out when subscriptionMaxAmount>5000 as UPI Subs Limit is 25000");

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
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("100500")
                .setSubscriptionFrequency("1")
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
                .isEqualToIgnoringCase("Subscription Amount Limit For UPI Breached");


    }


    @Override
    @Parameters({"isNativePlus"})
    @Test(description = "If txn amount>Subscription amount, and paymode=UPI," +
            "Amount Type = Variable,requestType=NATIVE_MF_SIP")
    public void TC_003_Native_MF_Subs_TxnAmountGreaterThanMaxAmount(@Optional("true") Boolean isNativePlus) throws Exception {


        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_MUTUAL_MF;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("5")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("2")
                .setSubscriptionFrequency("1")
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
                .isEqualTo("2022");
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultMsg())
                .as("resultMsg mismatch")
                .isEqualToIgnoringCase("Paymode selected is not applicable when txn amount is greater than the renewal amount");

    }

    @Issue("PGP-26277")
    @Test(description = "If subscriptionMaxAmount>5000 and txn amount<5000, and paymode=null," +
            "requestType=NATIVE_MF_SIP", groups = Group.Status.BUG)
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
                .setSubscriptionFrequencyUnit("BI_MONTHLY")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.addMonths(date, "yyyy-MM-dd", 1))
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

    /*Amount Type TestCases*/

//As Discuss with Srishti MAX Amount check has been removed from subs sides so need to disble testcase ---- Auto REnewal flow

    @Override
    @Feature("PGP-33945")
    @Parameters({"isNativePlus"})
    @Test(description = "For FIX amount type,if  equal amount is not passed for txn amount & max amount if payment mode=UPI," +
            " resultMsg : Transaction amount is not equal to max amount set against the subscription", enabled = true)
    public void TC_001_Native_MF_Subs_FixAmountEqualAmountNotPassedUPI(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_MUTUAL_MF;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("2")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("20")
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
                .isEqualToIgnoringCase("Paymode selected is not applicable when txn amount is less than the renewal amount");


    }

    @Override
    @Feature("PGP-33945")
    @Parameters({"isNativePlus"})
    @Test(description = "For FIX amount type,if  equal amount is not passed for txn amount & max amount if payment mode=null," +
            " then it should fail at the time of process txn ")
    public void TC_002_Native_MF_Subs_FixAmountEqualAmountNotPassed(@Optional("true") Boolean isNativePlus) throws Exception {


        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.MF_SIP_NEW_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("25")
                .setSubscriptionPaymentMode("")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("20")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("DAY")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
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
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("23")
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

        OrderDTO orderDTO = new OrderFactory.UPIMandate(merchant, txnToken, initTxnDTO.orderFromBody(), subsId)
                .setPayerAccount("test@paytm")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);

        validateSuccessResponsePage(initTxnDTO,orderDTO,subsId,merchant.getKey());

        validateSuccessTxnStatus(initTxnDTO,orderDTO,subsId);

        validateSuccessPeon(initTxnDTO,orderDTO,subsId);

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
                .setValidateAccountNumber("true")
                .setSubscriptionFrequency("1")
                .setAccountNumber("112343132122")
                .setAllowUnverifiedAccount("false")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("23")
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

        OrderDTO orderDTO = new OrderFactory.UPIMandate(merchant, txnToken, initTxnDTO.orderFromBody(), subsId)
                .setPayerAccount("test@paytm")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        validateSuccessResponsePage(initTxnDTO,orderDTO,subsId,merchant.getKey());

        validateSuccessTxnStatus(initTxnDTO,orderDTO,subsId);

        validateSuccessPeon(initTxnDTO,orderDTO,subsId);


    }

    @Override
    @Parameters({"isNativePlus"})
    @Test(description = "validateAccountNumber:true, accountNumber:invalid," +
            " then it should fail at the time of process txn ")
    public void TC_002_Native_MF_Subs_AccountNumTrueAndInvalid(@Optional("true") Boolean isNativePlus) throws Exception {


        String date = CommonUtils.getdate("yyyy-MM-dd");
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.MF_SIP_NEW_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setValidateAccountNumber("true")
                .setSubscriptionFrequency("1")
                .setAccountNumber("7777777")
                .setAllowUnverifiedAccount("false")
                .setSubscriptionFrequencyUnit("YEAR")
                .setSubscriptionGraceDays("235")
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

        OrderDTO orderDTO = new OrderFactory.UPIMandate(merchant, txnToken, initTxnDTO.orderFromBody(), subsId)
                .setPayerAccount("test@paytm")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("UPI")
                .validateRespCode("227")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("PPBLC")
                .validateSubsId(subsId)
                .validateCheckSum(merchant.getKey())
                .assertAll();

    }

    @Override
    @Parameters({"isNativePlus"})
    @Test(description = "validateAccountNumber:true, accountNumber:invalid " +
            " allow Unverified account : true , Txn is Successful " )
    public void TC_003_Native_MF_Subs_AccountNumTrueAndInValidUnverifiedFalse(@Optional("true") Boolean isNativePlus) throws Exception{

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
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("10")
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

        OrderDTO orderDTO = new OrderFactory.UPIMandate(merchant, txnToken, initTxnDTO.orderFromBody(), subsId)
                .setPayerAccount("test@paytm")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("PPBLC")
                .validateSubsId(subsId)
                .validateCheckSum(merchant.getKey())
                .assertAll();
    }


    @Override
    @Parameters({"isNativePlus"})
    @Test(description = "validateAccountNumber:false, accountNumber:invalid,"+
            " unverified account:false Txn is Successful " )
    public void TC_004_Native_MF_Subs_AccountNumFalseAndValid(@Optional("true") Boolean isNativePlus) throws Exception{



        String date = CommonUtils.getdate("yyyy-MM-dd");
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.MF_SIP_NEW_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("10")
                .setValidateAccountNumber("false")
                .setSubscriptionFrequency("1")
                .setAccountNumber("7777777")
                .setAllowUnverifiedAccount("false")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("10")
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

        OrderDTO orderDTO = new OrderFactory.UPIMandate(merchant, txnToken, initTxnDTO.orderFromBody(), subsId)
                .setPayerAccount("test@paytm")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("PPBLC")
                .validateSubsId(subsId)
                .validateCheckSum(merchant.getKey())
                .assertAll();


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


        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_FAILURE")
                .validateTxnType("SALE")
                .validateGatewayName("PPBLC")
                .validateMid(merchant.getId())
                .validatePaymentMode("UPI")
                .validateRespCode("227")
                .validateRefundAmnt("0.00")
                .validateSubsid(subsId)
                .validateTxnDate(new Date())
                .AssertAll();




    }

    /*----------------------------------------------------------------------------*/
    /*            Test cases For Native Subscription RequestType                      */
    /*----------------------------------------------------------------------------*/


    @Override
    @Parameters({"isNativePlus"})
    @Test(description = "Verify txn token and subs id generated successfully when " +
            "requestType=NATIVE_SUBS, freq=DAY")
    public void TC_001_Native_Subs_FreqUnitDay(@Optional("true") Boolean isNativePlus) throws Exception {


        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL;
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

        validateSuccessResponsePage(initTxnDTO,orderDTO,subsId,merchant.getKey());

        validateSuccessTxnStatus(initTxnDTO,orderDTO,subsId);

        validateSuccessPeon(initTxnDTO,orderDTO,subsId);
    }


    @Override
    @Parameters({"isNativePlus"})
    @Test(description = "Verify txn token and subs id generated successfully when " +
            "requestType=NATIVE_SUBS, freq=MONTH")
    public void TC_002_Native_Subs_FreqUnitMonth(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("30")
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

        validateSuccessResponsePage(initTxnDTO,orderDTO,subsId,merchant.getKey());

        validateSuccessTxnStatus(initTxnDTO,orderDTO,subsId);

        validateSuccessPeon(initTxnDTO,orderDTO,subsId);
    }

    @Override
    @Parameters({"isNativePlus"})
    @Test(description = "Verify txn token and subs id generated successfully when " +
            "requestType=NATIVE_SUBS, freq=YEAR")
    public void TC_003_Native_Subs_FreqUnitYear(@Optional("true") Boolean isNativePlus) throws Exception {


        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("YEAR")
                .setSubscriptionGraceDays("365")
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

        validateSuccessResponsePage(initTxnDTO,orderDTO,subsId,merchant.getKey());

        validateSuccessTxnStatus(initTxnDTO,orderDTO,subsId);

        validateSuccessPeon(initTxnDTO,orderDTO,subsId);
    }

    @Override
    @Parameters({"isNativePlus"})
    @Test(description = "Verify txn token and subs id generated successfully when " +
            "requestType=NATIVE_SUBS, freq=QUARTER")
    public void TC_004_Native_Subs_FreqUnitQuater(@Optional("true") Boolean isNativePlus) throws Exception {


        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("QUARTER")
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

        OrderDTO orderDTO = new OrderFactory.UPIMandate(merchant, txnToken, initTxnDTO.orderFromBody(), subsId)
                .setPayerAccount("test@paytm")
                .build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);

        validateSuccessResponsePage(initTxnDTO,orderDTO,subsId,merchant.getKey());

        validateSuccessTxnStatus(initTxnDTO,orderDTO,subsId);

        validateSuccessPeon(initTxnDTO,orderDTO,subsId);
    }

    @Override
    @Parameters({"isNativePlus"})
    @Test(description = "Verify txn token and subs id generated successfully when " +
            "requestType=NATIVE_SUBS, freq=BI_MONTHLY")
    public void TC_005_Native_Subs_FreqUnitBiMonthly(@Optional("true") Boolean isNativePlus) throws Exception {


        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL;
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

        OrderDTO orderDTO = new OrderFactory.UPIMandate(merchant, txnToken, initTxnDTO.orderFromBody(), subsId)
                .setPayerAccount("test@paytm")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);

        validateSuccessResponsePage(initTxnDTO,orderDTO,subsId,merchant.getKey());

        validateSuccessTxnStatus(initTxnDTO,orderDTO,subsId);

        validateSuccessPeon(initTxnDTO,orderDTO,subsId);
    }

    @Override
    @Parameters({"isNativePlus"})
    @Test(description = "Verify txn token and subs id generated successfully when " +
            "requestType=NATIVE_SUBS, freq=SEMI_ANNUALLY")
    public void TC_006_Native_Subs_FreqUnitSemiAnually(@Optional("true") Boolean isNativePlus) throws Exception {


        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL;
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

        OrderDTO orderDTO = new OrderFactory.UPIMandate(merchant, txnToken, initTxnDTO.orderFromBody(), subsId)
                .setPayerAccount("test@paytm")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);

        validateSuccessResponsePage(initTxnDTO,orderDTO,subsId,merchant.getKey());

        validateSuccessTxnStatus(initTxnDTO,orderDTO,subsId);

        validateSuccessPeon(initTxnDTO,orderDTO,subsId);
    }


    @Override
    @Parameters({"isNativePlus"})
    @Test(description = "Verify txn token and subs id generated successfully when " +
            "requestType=NATIVE_SUBS, freq=SEMI_ANNUALLY")
    public void TC_007_Native_Subs_FreqUnitWeek(@Optional("true") Boolean isNativePlus) throws Exception {


        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("5000")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("5000")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("WEEK")
                .setSubscriptionGraceDays("6")
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

        validateSuccessResponsePage(initTxnDTO,orderDTO,subsId,merchant.getKey());

        validateSuccessTxnStatus(initTxnDTO,orderDTO,subsId);

        validateSuccessPeon(initTxnDTO,orderDTO,subsId);
    }



    @Override
    @Parameters({"isNativePlus"})
    @Test(description = "Verify txn token and subs id generated successfully when" +
            "requestType=NATIVE_SUBS, freq=ONDEMAND")
    public void TC_008_Native_Subs_FreqUnitOnDemand(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL;
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

        validateSuccessResponsePage(initTxnDTO,orderDTO,subsId,merchant.getKey());

        validateSuccessTxnStatus(initTxnDTO,orderDTO,subsId);

        validateSuccessPeon(initTxnDTO,orderDTO,subsId);


    }

    /*Frequency Related TestCases*/


    @Override
    @Parameters({"isNativePlus"})
    @Test(description = "Verify subscriptionFrequencyUnit when Freq>1 " +
            "requestType=NATIVE_SUBS")
    public void TC_001_Native_Subs_FreqGreaterThn1(@Optional("true") Boolean isNativePlus) throws Exception {


        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_NATIVE_SUBS;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("2")
                .setSubscriptionFrequencyUnit("MONTH")
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
                .isEqualToIgnoringCase("Invalid Subscription Frequency");


    }

    @Override
    @Parameters({"isNativePlus"})
    @Test(description = "Verify Freq>0 ,SubsPaymode =Unknown " +
            "requestType=NATIVE_SUBS UPI Paymode is filtered in FPO")
    public void TC_002_Native_Subs_FreqGreaterthan1_PaymodeBlank(@Optional("true") Boolean isNativePlus) throws Exception {


        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("2")
                .setSubscriptionFrequencyUnit("DAY")
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
                .isEqualToIgnoringCase("0000");;
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
                .setSubscriptionFrequencyUnit("MONTH")
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
                .isEqualToIgnoringCase("Invalid Subscription Frequency");


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

        OrderDTO orderDTO = new OrderFactory.UPIMandate(merchant, txnToken, initTxnDTO.orderFromBody(), subsId)
                .setPayerAccount("test@paytm")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("PPBLC")
                .validateSubsId(subsId)
                .validateCheckSum(merchant.getKey())
                .assertAll();


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
                .setTxnValue("1")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("YEAR")
                .setSubscriptionGraceDays("300")
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
                .setTxnValue("1")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("29")
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

        OrderDTO orderDTO = new OrderFactory.UPIMandate(merchant, txnToken, initTxnDTO.orderFromBody(), subsId)
                .setPayerAccount("test@paytm")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("PPBLC")
                .validateSubsId(subsId)
                .validateCheckSum(merchant.getKey())
                .assertAll();
    }

    @Override
    @Parameters({"isNativePlus"})
    @Test(description = "Verify blank subscriptionGraceDays are not supported ")
    public void TC_003_Native_Subs_StartDateGraceDayNull(@Optional("true") Boolean isNativePlus) throws Exception {


        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_NATIVE_SUBS;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
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

      /*  InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);

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

        OrderDTO orderDTO = new OrderFactory.UPIMandate(merchant, txnToken, initTxnDTO.orderFromBody(), subsId)
                .setPayerAccount("test@paytm")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateStatus("TXN_FAILURE")
                .validateMid(orderDTO.getMID())
                .validateRespMsg("Invalid payment mode")
                .assertAll();*/

    }

    @Override
    @Parameters({"isNativePlus"})
    @Test(description = "Verify subscriptionGraceDays:1 subscriptionStartDate: , invalid Subscription start date" +
            "requestType=NATIVE_SUBS")
    public void TC_004_Native_Subs_StartDateIsOnlyBlank(@Optional("true") Boolean isNativePlus) throws Exception {


        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_NATIVE_SUBS;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
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


        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_NATIVE_SUBS;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("2")
                .setSubscriptionFrequencyUnit("DAY")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("3")
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
                .isEqualToIgnoringCase("invalid Subscription retry count");


    }

    @Override
    @Parameters({"isNativePlus"})
    @Test(description = "Verify when Paymode=null SubsRetry >2 , BHIM UPI is not allowed for this transaction, " +
            "kindly use some other payment mode requestType=NATIVE_SUBS")
    public void TC_002_Native_Subs_EnableRetryGreaterThan2PaymodeBlank(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("DAY")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("3")
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
        Assertions.assertThat(payUPI).isFalse().as("UPI is not getting filtered out when SunsRetry>2");

    }

    /*Subscription Max Amount TestCases*/

    //UPI Limit for Subs has been increased to 25k - PGP-36177
    @Override
    @Parameters({"isNativePlus"})
    @Test(description = "If subscriptionMaxAmount>5000 and txn amount>5000, and paymode=null," +
            "BHIM UPI is allowed for this transaction")
    public void TC_001_Native_Subs_MaxAmountGreaterThan5000PaymodeNull(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("5500")
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
    //    Assertions.assertThat(payUPI).isFalse().as("UPI is not getting filtered out when subscriptionMaxAmount>5000");
        Assertions.assertThat(payUPI).isTrue().as("UPI should not get filtered out when subscriptionMaxAmount>5000 as UPI Subs limit is 25000");

    }


    //UPI Limit for Subs has been increased to 25k - PGP-36177
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

//As Discuss with Srishti MAX Amount check has been removed from subs sides so need to disble testcase ---- Auto REnewal flow

    @Override
    @Feature("PGP-33945")
    @Parameters({"isNativePlus"})
    @Test(description = "If txn amount>Subscription amount, and paymode=UPI," +
            "requestType=NATIVE_SUBS" , enabled = true)
    public void TC_003_Native_Subs_TxnAmountGreaterThanMaxAmount(@Optional("true") Boolean isNativePlus) throws Exception {


        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_NATIVE_SUBS;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1300")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("1000")
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
                .setSubscriptionMaxAmount("2500")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("WEEK")
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


    //As Discuss with Srishti MAX Amount check has been removed from subs sides so need to disble testcase ---- Auto REnewal flow

    @Override
    @Feature("PGP-33945")
    @Parameters({"isNativePlus"})
    @Test(description = "For FIX amount type,if  equal amount is not passed for txn amount & max amount if payment mode=UPI," +
            " resultMsg : Transaction amount is not equal to max amount set against the subscription",enabled = true)
    public void TC_001_Native_Subs_FixAmountEqualAmountNotPassedUPI(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_NATIVE_SUBS;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("2")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("20")
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
                .isEqualTo("2022");
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultMsg())
                .as("resultMsg mismatch")
                .isEqualToIgnoringCase("Paymode selected is not applicable when txn amount is less than the renewal amount");

    }


    @Override
    @Feature("PGP-33945")
    @Parameters({"isNativePlus"})
    @Test(description = "For FIX amount type,if  equal amount is not passed for txn amount & max amount if payment mode=null," +
            " then it should fail at the time of process txn ")
    public void TC_002_Native_Subs_FixAmountEqualAmountNotPassed(@Optional("true") Boolean isNativePlus) throws Exception {


        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_ADDNPAY;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("2")
                .setSubscriptionPaymentMode("")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("20")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
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

        OrderDTO orderDTO = new OrderFactory.UPIMandate(merchant, txnToken, initTxnDTO.orderFromBody(), subsId)
                .setPayerAccount("test@paytm")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);

        validateSuccessResponsePage(initTxnDTO,orderDTO,subsId,merchant.getKey());

        validateSuccessTxnStatus(initTxnDTO,orderDTO,subsId);

        validateSuccessPeon(initTxnDTO,orderDTO,subsId);
    }


    @Test(description = "For the Variable amount, the subscription txn amount greater" +
            "  than subscription max amount " )
    public void TC_004_Native_Subs_VariableAmountGreater(@Optional("true") Boolean isNativePlus) throws Exception{

        String date = CommonUtils.getdate("yyyy-MM-dd");
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_NATIVE_SUBS;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("100")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("29")
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


    @Test(description = "Verify response validation ,txn status for Failure Txn after creation of Subscription" )
    public void TC_001_Native_Subs_FailureTransaction(@Optional("true") Boolean isNativePlus) throws Exception{

        String date = CommonUtils.getdate("yyyy-MM-dd");
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_MUTUAL_MF;
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

        OrderDTO orderDTO = new OrderFactory.UPIMandate(merchant, txnToken, initTxnDTO.orderFromBody(), subsId)
                .setPayerAccount("test@paytm")
                .build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_FAILURE")
                .validateTxnType("SALE")
                .validateGatewayName("PPBLC")
                .validateMid(merchant.getId())
                .validatePaymentMode("UPI")
                .validateRespCode("227")
                .validateRefundAmnt("0.00")
                .validateSubsid(subsId)
                .validateTxnDate(new Date())
                .AssertAll();
    }


    @Owner(GAGANDEEP)
    @Feature("PGP-28522")
    @Test(description = "Validate Payment Service Type Native Subs full Refund")
    public void Validate_PaymentServiceTypeNative_Subs_FullRefund(@Optional("true") Boolean isNativePlus) throws Exception {

        String txnAmount = "1";
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_NATIVE_SUBS;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue(txnAmount)
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("30")
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
                .validateSubsid(subsId)
                .validateTxnDate(new Date())
                .AssertAll();


        PGPHelpers.initiateAsyncRefund(orderDTO.getMID(), orderDTO.getMerchantKey(),
                orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), txnStatus.getResponse().getTXNID(), txnAmount,
                "REFUND", "", null);
        PGPHelpers.getRefundStatus(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), true);

        String grepcmd = "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/refund_facade.log | " +
                "grep \"DATASERVICE_BIZORDER_DETAIL\" | grep \"RESPONSE\" | tail -1;";
        String refund_Facade_Logs = getLogsOnServer("10.144.18.104",grepcmd);
                Assertions.assertThat(refund_Facade_Logs).containsIgnoringCase("\"paymentServiceType\":\"REFUND\"");
    }


    @Owner(GAGANDEEP)
    @Feature("PGP-28522")
    @Test(description = "Validate Payment Service Type Native Subs partial Refund")
    public void Validate_PaymentServiceTypeNative_Subs_PartialRefund(@Optional("true") Boolean isNativePlus) throws Exception {

        String txnAmount = "10";
        String partialRefundAmount = "5";
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_NATIVE_SUBS;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue(txnAmount)
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("30")
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
                .validateSubsid(subsId)
                .validateTxnDate(new Date())
                .AssertAll();


        PGPHelpers.initiateAsyncRefund(orderDTO.getMID(), orderDTO.getMerchantKey(),
                orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), txnStatus.getResponse().getTXNID(), partialRefundAmount,
                "REFUND", "", null);
        PGPHelpers.getRefundStatus(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), true);

        String grepcmd = "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/refund_facade.log | " +
                "grep \"DATASERVICE_BIZORDER_DETAIL\" | grep \"RESPONSE\" | tail -1;";
        String refund_Facade_Logs = getLogsOnServer("10.144.18.104",grepcmd);
        Assertions.assertThat(refund_Facade_Logs).containsIgnoringCase("\"paymentServiceType\":\"REFUND\"");
    }

    /* ONDEMAND VALIDATIONS PGP-26500*/

    @Override
    @Parameters({"isNativePlus"})
    @Test(description = "Verify that Frequency, StartDate, GracePeriod, RetryAllowed & RetryCount are ignored when" +
            "requestType=NATIVE_MF_SIP, freq=ONDEMAND")
    public void TC_001_Native_MF_Subs_OnDemandValidations(@Optional("true") Boolean isNativePlus) throws Exception {


        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.MF_SIP_NEW_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("")
                .setSubscriptionRetryCount("")
                .setSubscriptionEnableRetry("")
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

        validateSuccessResponsePage(initTxnDTO,orderDTO,subsId,merchant.getKey());

    }

    @Override
    @Parameters({"isNativePlus"})
    @Test(description = "Verify that Frequency, StartDate, GracePeriod, RetryAllowed & RetryCount are ignored when" +
            "requestType=NATIVE_SUBS, freq=ONDEMAND")
    public void TC_001_Native_Subs_OnDemandValidations(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("")
                .setSubscriptionRetryCount("")
                .setSubscriptionEnableRetry("")
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

        validateSuccessResponsePage(initTxnDTO,orderDTO,subsId,merchant.getKey());
    }

    @Feature("PGP-32799")
    @Owner(Constants.Owner.AKSHAT)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify that sub-error code is displayed in MF_SIP transaction Status response")
    public void TC_001_NativeMF_SIP_Subs_Failure_ErrorCode(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.MF_SIP_NEW_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("17")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("20")
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
                .setTXN_AMOUNT("17")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("UPI")
                .validateRespCode("227")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("PPBLC")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .validateCheckSum(merchant.getKey())
                .assertAll();

        GetPaymentStatusDTO getPaymentStatusDTOFail = new GetPaymentStatusDTO.Builder
                (orderDTO.getORDER_ID(), orderDTO.getMID(), merchant.getKey())
                .build();
        GetPaymentStatus getPaymentStatusFail = new GetPaymentStatus(getPaymentStatusDTOFail);
        Response responseFail = getPaymentStatusFail.execute();
        JsonPath jsonPathFail = responseFail.jsonPath();
        Assertions.assertThat(jsonPathFail.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("TXN_FAILURE");
        Assertions.assertThat(jsonPathFail.getString("body.resultInfo.resultCode")).isEqualTo("227");
        Assertions.assertThat(jsonPathFail.getString("body.subResultInfo.resultCode")).isEqualTo("SE110");
        Assertions.assertThat(jsonPathFail.getString("body.mid")).isEqualToIgnoringCase(orderDTO.getMID());
        Assertions.assertThat(jsonPathFail.getString("body.orderId")).isEqualToIgnoringCase(orderDTO.getORDER_ID());
        Assertions.assertThat(jsonPathFail.getString("body.txnAmount")).isEqualToIgnoringCase("17.00");
    }

    @Feature("PGP-32799")
    @Owner(Constants.Owner.AKSHAT)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify that sub-error code is displayed in NATIVE_SUBS transaction Status response")
    public void TC_001_Native_Subs_Failure_ErrorCode(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("17")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("20")
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

        OrderDTO orderDTO = new OrderFactory.UPIMandate(merchant, txnToken, initTxnDTO.orderFromBody(), subsId)
                .setPayerAccount("test@paytm")
                .setTXN_AMOUNT("17")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("UPI")
                .validateRespCode("227")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("PPBLC")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .validateCheckSum(merchant.getKey())
                .assertAll();

        GetPaymentStatusDTO getPaymentStatusDTOFail = new GetPaymentStatusDTO.Builder
                (orderDTO.getORDER_ID(), orderDTO.getMID(), merchant.getKey())
                .build();
        GetPaymentStatus getPaymentStatusFail = new GetPaymentStatus(getPaymentStatusDTOFail);
        Response responseFail = getPaymentStatusFail.execute();
        JsonPath jsonPathFail = responseFail.jsonPath();
        Assertions.assertThat(jsonPathFail.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("TXN_FAILURE");
        Assertions.assertThat(jsonPathFail.getString("body.resultInfo.resultCode")).isEqualTo("227");
        Assertions.assertThat(jsonPathFail.getString("body.subResultInfo.resultCode")).isEqualTo("SE110");
        Assertions.assertThat(jsonPathFail.getString("body.mid")).isEqualToIgnoringCase(orderDTO.getMID());
        Assertions.assertThat(jsonPathFail.getString("body.orderId")).isEqualToIgnoringCase(orderDTO.getORDER_ID());
        Assertions.assertThat(jsonPathFail.getString("body.txnAmount")).isEqualToIgnoringCase("17.00");
    }
    /* HOTSTAR HOTFIX PGP-49471*/
    @Parameters({"isNativePlus"})
    @Test(description = "Verify that subscription is created for Quarter Frequency when paymode is UPI")
    public void PGP_49471_TC001_UPISubs_Created(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBS_QUARTER;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("3")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("5")
                .setSubscriptionRetryCount("1")
                .setSubscriptionEnableRetry("1")
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

        validateSuccessResponsePage(initTxnDTO,orderDTO,subsId,merchant.getKey());
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify that subscription is created for Quarter Frequency when paymode is blank")
    public void PGP_49471_TC002_UPISubs_Created(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBS_QUARTER;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("3")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("5")
                .setSubscriptionRetryCount("1")
                .setSubscriptionEnableRetry("1")
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

        validateSuccessResponsePage(initTxnDTO,orderDTO,subsId,merchant.getKey());
    }
    @Parameters({"isNativePlus"})
    @Test(description = "Verify that subscription is not created for Quarter Frequency when paymode is UPI")
    public void PGP_49471_TC003_subsNotCtreated(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("3")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("5")
                .setSubscriptionRetryCount("1")
                .setSubscriptionEnableRetry("1")
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
                .isEqualToIgnoringCase("Invalid Subscription frequency");

    }
    @Parameters({"isNativePlus"})
    @Test(description = "Verify that UPI is not returned in FPO when frequency=3 and paymode is blank")
    public void PGP_49471_TC004_UPI_notReturnedInFPO(@Optional("true") Boolean isNativePlus) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("3")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("5")
                .setSubscriptionRetryCount("1")
                .setSubscriptionEnableRetry("1")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_SUBSCRIPTION)
                .build();

        InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);

        String txnToken = responseDTO.getBody().getTxnToken();
        String subsId = responseDTO.getBody().getSubscriptionId();

        FetchPaymentOptResponseDTO fetchPaymentOptResponseDTO = NativeHelpers.fetchPaymentOptionResponse(txnToken, merchant.getId(), initTxnDTO.orderFromBody());
        boolean paymodeStatus = NativeHelpers.isFetchPaymentOptionStatusMatched(fetchPaymentOptResponseDTO, "UPI", true);
        Assertions.assertThat(paymodeStatus).as("UPI paymode status mismatch").isFalse();

    }

    @Owner(Constants.Owner.AKSHAT)
    @Feature("PGP-58496")
    @Test(description = "Verify that expiry time is passed in create_order request for Native MF_SIP")
    public void validateExpiryTime_MFSIP(@Optional("true") Boolean isNativePlus) throws Exception {


        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.PG2_SUBS_MFSIP;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("")
                .setSubscriptionRetryCount("")
                .setSubscriptionEnableRetry("")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_MF_SIP)
                .build();

        InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, initTxnDTO.orderFromBody(), "ACQUIRING_CREATE_ORDER");
        Assertions.assertThat(logs).contains("\"timeoutConfig\":[{\"timeoutType\":\"EXPIRY_TIMEOUT\",\"disabled\":false,\"timeoutInSeconds\":\"600\"}]");

    }

        @Owner(Constants.Owner.AKSHAT)
        @Feature("PGP-58496")
        @Test(description = "Verify that expiry time is passed in create_order request for Native Subscription")
        public void validateExpiryTime_NativeSubs(@Optional("true") Boolean isNativePlus) throws Exception {


            User user = userManager.getForRead(Label.BASIC);
            Constants.MerchantType merchant = Constants.MerchantType.P4B_NOTIFICATION_MID;
            InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                    .setTxnValue("1")
                    .setSubscriptionPaymentMode("UPI")
                    .setSubscriptionAmountType("VARIABLE")
                    .setSubscriptionMaxAmount("1000")
                    .setSubscriptionFrequency("1")
                    .setSubscriptionFrequencyUnit("MONTH")
                    .setSubscriptionGraceDays("0")
                    .setSubscriptionRetryCount("0")
                    .setSubscriptionEnableRetry("0")
                    .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                    .setRequestType("NATIVE_SUBSCRIPTION")
                    .build();

            InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);

            String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, initTxnDTO.orderFromBody(), "ACQUIRING_CREATE_ORDER");
            Assertions.assertThat(logs).contains("\"timeoutConfig\":[{\"timeoutType\":\"EXPIRY_TIMEOUT\",\"disabled\":false,\"timeoutInSeconds\":\"900\"}]");


        }

    @Owner(Constants.Owner.AKSHAT)
    @Feature("PGP-58835")
    @Test(description = "Verify order is created on Product Code 51051000100000000004 for PCF & MF_SIP enabled MID")
    public void validateNativeOrderCreateon_51051000100000000043(@Optional("true") Boolean isNativePlus) throws Exception {


        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SIP_PCF_fix;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("0")
                .setSubscriptionEnableRetry("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_MF_SIP)
                .build();

        InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, initTxnDTO.orderFromBody(), "ACQUIRING_CREATE_ORDER");
        Assertions.assertThat(logs).contains("\"productCode\":\"51051000100000000043\"");

    }

    @Owner(Constants.Owner.AKSHAT)
    @Feature("PGP-58835")
    @Test(description = "Verify order is created on Product Code 51051000100000000004 for PCF & Subs enabled MID")
    public void validateNativeOrderCreateon_51051000100000000004(@Optional("true") Boolean isNativePlus) throws Exception {


        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.Subs_PCF_fix;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("1000")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("0")
                .setSubscriptionEnableRetry("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();

        InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, initTxnDTO.orderFromBody(), "ACQUIRING_CREATE_ORDER");
        Assertions.assertThat(logs).contains("\"productCode\":\"51051000100000000004\"");


    }

    @Owner(Constants.Owner.AKSHAT)
    @Feature("PGP-58835")
    @Test(description = "Verify PCF is failed for subscription transaction token")
    public void validateSubs_PCF_Failure(@Optional("false") Boolean isNativePlus) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.Subs_PCF_fix;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("1000")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("0")
                .setSubscriptionEnableRetry("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();

        InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = responseDTO.getBody().getTxnToken();
        JsonPath jsonPath = PcfHelpher.fetchPCFDetailsNative("UPI","ICICI",txnToken,initTxnDTO);
        System.out.println("jsonPath - " + "\n");
        System.out.println(jsonPath);

        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("2006");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Mid is invalid");


    }

    @Owner(Constants.Owner.AKSHAT)
    @Feature("PGP-61250")
    @Parameters({"isNativePlus"})
    @Test(description = "Verify that SIP transaction is routed on Original MID gateway")
    public void TC_001_Native_MF_Subs_PennyDropGateway(@Optional("true") Boolean isNativePlus) throws Exception {

        /* HDFC UPI is added on original MID and PTYBLC UPI is added on Penny Drop MID.
           Transaction should route through HDFC */

        Constants.MerchantType merchant = Constants.MerchantType.Subs_HDFC_CheckoutJS;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("0")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("1")
                .setSubscriptionRetryCount("1")
                .setSubscriptionEnableRetry("1")
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

        validateSuccessResponsePageHDFC(initTxnDTO,orderDTO,subsId,merchant.getKey());

    }

    @Owner(Constants.Owner.AKSHAT)
    @Feature("PGP-61250")
    @Parameters({"isNativePlus"})
    @Test(description = "Verify that SIP transaction is routed on Original MID gateway")
    public void TC_002_Native_Subs_PennyDropGateway(@Optional("true") Boolean isNativePlus) throws Exception {

        /* HDFC UPI is added on original MID and PTYBLC UPI is added on Penny Drop MID.
           Transaction should route through HDFC */

        Constants.MerchantType merchant = Constants.MerchantType.Subs_HDFC_CheckoutJS;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("0")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("1")
                .setSubscriptionRetryCount("1")
                .setSubscriptionEnableRetry("1")
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

        validateSuccessResponsePageHDFC(initTxnDTO,orderDTO,subsId,merchant.getKey());
    }


    @Owner(Constants.Owner.AKSHAT)
    @Feature("PGP-61250")
    @Parameters({"isNativePlus"})
    @Test(description = "Verify that following params are passed in SIP Cashier Pay subscriptionRequestType, isZeroRupeeTxn, actualMid")
    public void TC_003_Native_MF_Subs_CashierPayParams(@Optional("true") Boolean isNativePlus) throws Exception {

        Constants.MerchantType merchant = Constants.MerchantType.Subs_HDFC_CheckoutJS;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("0")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("1")
                .setSubscriptionRetryCount("1")
                .setSubscriptionEnableRetry("1")
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

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, initTxnDTO.orderFromBody(), "ACQUIRING_PAY_ORDER");
        Assertions.assertThat(logs).contains("\"subscriptionRequestType\":\"CREATE\"");
        Assertions.assertThat(logs).contains("\"isUPIMandate\":\"true\"");
        Assertions.assertThat(logs).contains("\"isZeroRupeeTxn\":\"true\"");
        Assertions.assertThat(logs).contains("\"actualMid\":\"qa12mm54514776485488\"");

    }

    @Owner(Constants.Owner.AKSHAT)
    @Feature("PGP-61250")
    @Parameters({"isNativePlus"})
    @Test(description = "Verify that following params are passed in SIP Cashier Pay subscriptionRequestType, isZeroRupeeTxn, actualMid")
    public void TC_004_Native_MF_Subs_CashierPayParams(@Optional("true") Boolean isNativePlus) throws Exception {

        Constants.MerchantType merchant = Constants.MerchantType.Subs_HDFC_CheckoutJS;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("0")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("1")
                .setSubscriptionRetryCount("1")
                .setSubscriptionEnableRetry("1")
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

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, initTxnDTO.orderFromBody(), "ACQUIRING_PAY_ORDER");
        Assertions.assertThat(logs).contains("\"subscriptionRequestType\":\"CREATE\"");
        Assertions.assertThat(logs).contains("\"isUPIMandate\":\"true\"");
        Assertions.assertThat(logs).contains("\"isZeroRupeeTxn\":\"true\"");
        Assertions.assertThat(logs).contains("\"actualMid\":\"qa12mm54514776485488\"");

    }

}
