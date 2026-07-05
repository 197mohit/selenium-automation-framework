package scripts.UPISubsMandate;


import com.paytm.LocalConfig;
import com.paytm.api.TxnStatus;
import com.paytm.api.subscription.CheckStatus;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.*;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.PayerAccountDetail;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.response.FetchPaymentOptResponseDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.api.subscription.HDFCIntentPay;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.checkoutjs.MerchantConfig;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.framework.utils.CommonUtils;
import com.paytm.pages.*;

import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;

import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.qameta.allure.Step;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.Date;

import static com.paytm.apphelpers.LogsValidationHelper.getLogsOnServer;
public class UPISubsHDFC extends PGPBaseTest {

    private final static String NATIVE_SUBSCRIPTION = "NATIVE_SUBSCRIPTION";
    private final static String INSTA_Callback = LocalConfig.PGP_HOST + "/instaproxy/bankresponse/HDFC/UPI/RESP";
    private final static String NATIVE_MF_SIP = "NATIVE_MF_SIP";
    private final CheckoutPage checkoutPage = new CheckoutPage();
    private final CheckoutJsCheckoutPage checkoutJSPage = new CheckoutJsCheckoutPage();

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
                .validateGatewayName("HDFC")
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
                .validateGatewayName("HDFC")
                .validateSubsId(subsId)
             //   .validateCheckSum(merchantKey)
                .assertAll();
    }


    /*----------------------------------------------------------------------------*/
    /*          Test cases For Native Subscription RequestType                      */
    /*----------------------------------------------------------------------------*/


    @Feature("PGP-54077")
    @Owner(Constants.Owner.AKSHAT)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify that subscription is successfully generated when for freq=MONTH ")
    public void TC_001_Native_Subs_Month(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.HDFC_Subs_Collect;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
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
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);

        validateSuccessResponsePage(initTxnDTO,orderDTO,subsId,merchant.getKey());

        validateSuccessTxnStatus(initTxnDTO,orderDTO,subsId);

    }

    @Feature("PGP-54077")
    @Owner(Constants.Owner.AKSHAT)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify that subscription is successfully generated when for freq=ONDEMAND ")
    public void TC_002_Native_Subs_ONDEMAND(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.HDFC_Subs_Collect;
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

        validateSuccessTxnStatus(initTxnDTO,orderDTO,subsId);
    }


    @Feature("PGP-54077")
    @Owner(Constants.Owner.AKSHAT)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify that subscription is successfully generated when for freq=YEAR ")
    public void TC_003_Native_Subs_Year(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.HDFC_Subs_Collect;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("2")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("25")
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

        validateSuccessResponsePage(initTxnDTO,orderDTO,subsId,merchant.getKey());

        validateSuccessTxnStatus(initTxnDTO,orderDTO,subsId);

    }

    @Feature("PGP-54077")
    @Owner(Constants.Owner.AKSHAT)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify that subscription is successfully generated when for freq=WEEK ")
    public void TC_004_Native_Subs_WEEK(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.HDFC_Subs_Collect;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("5")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("50")
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

        OrderDTO orderDTO = new OrderFactory.UPIMandate(merchant, txnToken, initTxnDTO.orderFromBody(), subsId)
                .setPayerAccount("test@paytm")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);

        validateSuccessResponsePage(initTxnDTO,orderDTO,subsId,merchant.getKey());

        validateSuccessTxnStatus(initTxnDTO,orderDTO,subsId);

    }

    @Feature("PGP-54077")
    @Owner(Constants.Owner.AKSHAT)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify that subscription is successfully generated when for freq=QUARTER ")
    public void TC_005_Native_Subs_QUARTER(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.HDFC_Subs_Collect;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("4")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("50")
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

    }

    @Feature("PGP-54077")
    @Owner(Constants.Owner.AKSHAT)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify that subscription is successfully generated when for freq=BI_MONTHLY ")
    public void TC_006_Native_Subs_BI_MONTHLY(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.HDFC_Subs_Collect;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("5")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("25")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("BI_MONTHLY")
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

    }

    @Feature("PGP-54077")
    @Owner(Constants.Owner.AKSHAT)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify that subscription is successfully generated when for freq=SEMI_ANNUALLY ")
    public void TC_007_Native_Subs_SEMI_ANNUALLY(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.HDFC_Subs_Collect;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("5")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("100")
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

    }

    @Feature("PGP-54077")
    @Owner(Constants.Owner.AKSHAT)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify that subscription is successfully generated when for freq=FORTNIGHT ")
    public void TC_008_Native_Subs_FORTNIGHT(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.HDFC_Subs_Collect;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("5")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("100")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("FORTNIGHT")
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

    }

//** HFDC UPI Autopay Enhanced Flow **//

    @Feature("PGP-54077")
    @Owner(Constants.Owner.AKSHAT)
    @Parameters({"theme"})
    @Test(description = "Verify that subscription is successfully generated when for freq=Month ")
    public void TC_001_Enhanced_Subs_Month(@Optional("enhancedweb_revamp") String theme) throws Exception {

        String date = CommonUtils.getdate("yyyy-MM-dd");
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.HDFC_Subs_Collect;

        OrderDTO orderDTO = new OrderFactory.UPIMandate(merchant, theme, user)
                .setSUBS_FREQUENCY_UNIT("MONTH")
                .setTXN_AMOUNT("3")
                .setSUBS_MAX_AMOUNT("100")
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("0")
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.UPI);
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
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("HDFC")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("HDFC")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateRefundAmnt("0.00")
                .validateSubsid(Constants.ValidationType.NON_EMPTY)
                .validateTxnDate(new Date())
                .AssertAll();


    }

    @Feature("PGP-54077")
    @Owner(Constants.Owner.AKSHAT)
    @Parameters({"theme"})
    @Test(description = "Verify that subscription is successfully generated when for freq=ONDEMAND ")
    public void TC_002_Enhanced_Subs_ONDEMAND(@Optional("enhancedweb_revamp") String theme) throws Exception {

        String date = CommonUtils.getdate("yyyy-MM-dd");
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.HDFC_Subs_Collect;

        OrderDTO orderDTO = new OrderFactory.UPIMandate(merchant, theme, user)
                .setSUBS_FREQUENCY_UNIT("ONDEMAND")
                .setTXN_AMOUNT("5")
                .setSUBS_MAX_AMOUNT("100")
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("0")
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.UPI);
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
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("HDFC")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("HDFC")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateRefundAmnt("0.00")
                .validateSubsid(Constants.ValidationType.NON_EMPTY)
                .validateTxnDate(new Date())
                .AssertAll();


    }

    @Feature("PGP-54077")
    @Owner(Constants.Owner.AKSHAT)
    @Parameters({"theme"})
    @Test(description = "Verify that subscription is successfully generated when for freq=YEAR ")
    public void TC_003_Enhanced_Subs_YEAR(@Optional("enhancedweb_revamp") String theme) throws Exception {

        String date = CommonUtils.getdate("yyyy-MM-dd");
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.HDFC_Subs_Collect;

        OrderDTO orderDTO = new OrderFactory.UPIMandate(merchant, theme, user)
                .setSUBS_FREQUENCY_UNIT("YEAR")
                .setTXN_AMOUNT("10")
                .setSUBS_MAX_AMOUNT("150")
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("0")
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.UPI);
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
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("HDFC")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("HDFC")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateRefundAmnt("0.00")
                .validateSubsid(Constants.ValidationType.NON_EMPTY)
                .validateTxnDate(new Date())
                .AssertAll();


    }

    @Feature("PGP-54077")
    @Owner(Constants.Owner.AKSHAT)
    @Parameters({"theme"})
    @Test(description = "Verify that subscription is successfully generated when for freq=WEEK ")
    public void TC_004_Enhanced_Subs_WEEK(@Optional("enhancedweb_revamp") String theme) throws Exception {

        String date = CommonUtils.getdate("yyyy-MM-dd");
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.HDFC_Subs_Collect;

        OrderDTO orderDTO = new OrderFactory.UPIMandate(merchant, theme, user)
                .setSUBS_FREQUENCY_UNIT("WEEK")
                .setTXN_AMOUNT("5")
                .setSUBS_MAX_AMOUNT("150")
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("0")
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.UPI);
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
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("HDFC")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("HDFC")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateRefundAmnt("0.00")
                .validateSubsid(Constants.ValidationType.NON_EMPTY)
                .validateTxnDate(new Date())
                .AssertAll();


    }

    @Feature("PGP-54077")
    @Owner(Constants.Owner.AKSHAT)
    @Parameters({"theme"})
    @Test(description = "Verify that subscription is successfully generated when for freq=QUARTER ")
    public void TC_005_Enhanced_Subs_QUARTER(@Optional("enhancedweb_revamp") String theme) throws Exception {

        String date = CommonUtils.getdate("yyyy-MM-dd");
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.HDFC_Subs_Collect;

        OrderDTO orderDTO = new OrderFactory.UPIMandate(merchant, theme, user)
                .setSUBS_FREQUENCY_UNIT("QUARTER")
                .setTXN_AMOUNT("10")
                .setSUBS_MAX_AMOUNT("50")
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("0")
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.UPI);
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
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("HDFC")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("HDFC")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateRefundAmnt("0.00")
                .validateSubsid(Constants.ValidationType.NON_EMPTY)
                .validateTxnDate(new Date())
                .AssertAll();


    }


    @Feature("PGP-54077")
    @Owner(Constants.Owner.AKSHAT)
    @Parameters({"theme"})
    @Test(description = "Verify that subscription is successfully generated when for freq=BI_MONTHLY ")
    public void TC_006_Enhanced_Subs_BI_MONTHLY(@Optional("enhancedweb_revamp") String theme) throws Exception {

        String date = CommonUtils.getdate("yyyy-MM-dd");
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.HDFC_Subs_Collect;

        OrderDTO orderDTO = new OrderFactory.UPIMandate(merchant, theme, user)
                .setSUBS_FREQUENCY_UNIT("BI_MONTHLY")
                .setTXN_AMOUNT("5")
                .setSUBS_MAX_AMOUNT("50")
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("0")
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.UPI);
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
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("HDFC")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("HDFC")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateRefundAmnt("0.00")
                .validateSubsid(Constants.ValidationType.NON_EMPTY)
                .validateTxnDate(new Date())
                .AssertAll();


    }

    @Feature("PGP-54077")
    @Owner(Constants.Owner.AKSHAT)
    @Parameters({"theme"})
    @Test(description = "Verify that subscription is successfully generated when for freq=SEMI_ANNUALLY ")
    public void TC_007_Enhanced_Subs_SEMI_ANNUALLY(@Optional("enhancedweb_revamp") String theme) throws Exception {

        String date = CommonUtils.getdate("yyyy-MM-dd");
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.HDFC_Subs_Collect;

        OrderDTO orderDTO = new OrderFactory.UPIMandate(merchant, theme, user)
                .setSUBS_FREQUENCY_UNIT("SEMI_ANNUALLY")
                .setTXN_AMOUNT("2")
                .setSUBS_MAX_AMOUNT("100")
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("0")
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.UPI);
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
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("HDFC")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("HDFC")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateRefundAmnt("0.00")
                .validateSubsid(Constants.ValidationType.NON_EMPTY)
                .validateTxnDate(new Date())
                .AssertAll();


    }

    @Feature("PGP-54077")
    @Owner(Constants.Owner.AKSHAT)
    @Parameters({"theme"})
    @Test(description = "Verify that subscription is successfully generated when for freq=MONTH")
    public void TC_001_checkoutJS_Subs_Month(@Optional("checkoutjs_web_revamp") String theme) throws Exception {

        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.Subs_HDFC_CheckoutJS;

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("20")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("20")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();

        InitTxnResponseDTO initTxnResponseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        MerchantConfig config = checkoutJSPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutJSPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabUPI().assertVisible();
        cashierPage.payBy(Constants.PayMode.UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(merchant.getId())
                .validateOrderId(initTxnDTO.orderFromBody())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("HDFC")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(merchant.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("HDFC")
                .validateMid(merchant.getId())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateRefundAmnt("0.00")
                .validateSubsid(Constants.ValidationType.NON_EMPTY)
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Feature("PGP-54077")
    @Owner(Constants.Owner.AKSHAT)
    @Parameters({"theme"})
    @Test(description = "Verify that subscription is successfully generated when for freq=ONDEMAND")
    public void TC_002_checkoutJS_Subs_ONDEMAND(@Optional("checkoutjs_web_revamp") String theme) throws Exception {

        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.Subs_HDFC_CheckoutJS;

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("50")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("50")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();

        InitTxnResponseDTO initTxnResponseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        MerchantConfig config = checkoutJSPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutJSPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabUPI().assertVisible();
        cashierPage.payBy(Constants.PayMode.UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(merchant.getId())
                .validateOrderId(initTxnDTO.orderFromBody())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("HDFC")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(merchant.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("HDFC")
                .validateMid(merchant.getId())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateRefundAmnt("0.00")
                .validateSubsid(Constants.ValidationType.NON_EMPTY)
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Feature("PGP-54077")
    @Owner(Constants.Owner.AKSHAT)
    @Parameters({"theme"})
    @Test(description = "Verify that subscription is successfully generated when for freq=WEEK")
    public void TC_003_checkoutJS_Subs_WEEK(@Optional("checkoutjs_web_revamp") String theme) throws Exception {

        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.Subs_HDFC_CheckoutJS;

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("5")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("50")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("WEEK")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();

        InitTxnResponseDTO initTxnResponseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        MerchantConfig config = checkoutJSPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutJSPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabUPI().assertVisible();
        cashierPage.payBy(Constants.PayMode.UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(merchant.getId())
                .validateOrderId(initTxnDTO.orderFromBody())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("HDFC")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(merchant.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("HDFC")
                .validateMid(merchant.getId())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateRefundAmnt("0.00")
                .validateSubsid(Constants.ValidationType.NON_EMPTY)
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Feature("PGP-54077")
    @Owner(Constants.Owner.AKSHAT)
    @Parameters({"theme"})
    @Test(description = "Verify that subscription is successfully generated when for freq=YEAR")
    public void TC_004_checkoutJS_Subs_YEAR(@Optional("checkoutjs_web_revamp") String theme) throws Exception {

        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.Subs_HDFC_CheckoutJS;

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("50")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("YEAR")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();

        InitTxnResponseDTO initTxnResponseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        MerchantConfig config = checkoutJSPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutJSPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabUPI().assertVisible();
        cashierPage.payBy(Constants.PayMode.UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(merchant.getId())
                .validateOrderId(initTxnDTO.orderFromBody())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("HDFC")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(merchant.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("HDFC")
                .validateMid(merchant.getId())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateRefundAmnt("0.00")
                .validateSubsid(Constants.ValidationType.NON_EMPTY)
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Feature("PGP-54077")
    @Owner(Constants.Owner.AKSHAT)
    @Parameters({"theme"})
    @Test(description = "Verify that subscription is successfully generated when for freq=QUARTER")
    public void TC_005_checkoutJS_Subs_QUARTER(@Optional("checkoutjs_web_revamp") String theme) throws Exception {

        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.Subs_HDFC_CheckoutJS;

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("2")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("100")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("QUARTER")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();

        InitTxnResponseDTO initTxnResponseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        MerchantConfig config = checkoutJSPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutJSPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabUPI().assertVisible();
        cashierPage.payBy(Constants.PayMode.UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(merchant.getId())
                .validateOrderId(initTxnDTO.orderFromBody())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("HDFC")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(merchant.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("HDFC")
                .validateMid(merchant.getId())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateRefundAmnt("0.00")
                .validateSubsid(Constants.ValidationType.NON_EMPTY)
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Feature("PGP-54077")
    @Owner(Constants.Owner.AKSHAT)
    @Parameters({"theme"})
    @Test(description = "Verify that subscription is successfully generated when for freq=BI_MONTHLY")
    public void TC_006_checkoutJS_Subs_BI_MONTHLY(@Optional("checkoutjs_web_revamp") String theme) throws Exception {

        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.Subs_HDFC_CheckoutJS;

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("5")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("100")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("BI_MONTHLY")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();

        InitTxnResponseDTO initTxnResponseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        MerchantConfig config = checkoutJSPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutJSPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabUPI().assertVisible();
        cashierPage.payBy(Constants.PayMode.UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(merchant.getId())
                .validateOrderId(initTxnDTO.orderFromBody())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("HDFC")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(merchant.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("HDFC")
                .validateMid(merchant.getId())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateRefundAmnt("0.00")
                .validateSubsid(Constants.ValidationType.NON_EMPTY)
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Feature("PGP-54077")
    @Owner(Constants.Owner.AKSHAT)
    @Parameters({"theme"})
    @Test(description = "Verify that subscription is successfully generated when for freq=SEMI_ANNUALLY")
    public void TC_007_checkoutJS_Subs_SEMI_ANNUALLY(@Optional("checkoutjs_web_revamp") String theme) throws Exception {

        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.Subs_HDFC_CheckoutJS;

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("5")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("100")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("BI_MONTHLY")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();

        InitTxnResponseDTO initTxnResponseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        MerchantConfig config = checkoutJSPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutJSPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabUPI().assertVisible();
        cashierPage.payBy(Constants.PayMode.UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(merchant.getId())
                .validateOrderId(initTxnDTO.orderFromBody())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("HDFC")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(merchant.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("HDFC")
                .validateMid(merchant.getId())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateRefundAmnt("0.00")
                .validateSubsid(Constants.ValidationType.NON_EMPTY)
                .validateTxnDate(new Date())
                .AssertAll();
    }


    @Feature("PGP-54077")
    @Owner(Constants.Owner.AKSHAT)
    @Parameters({"theme"})
    @Test(description = "Verify that subscription is successfully generated when for freq=Month ")
    public void TC_001_subscriptionClientRemoval_createCollectFlowHDFC(@Optional("enhancedweb_revamp") String theme) throws Exception {

        String date = CommonUtils.getdate("yyyy-MM-dd");
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.HDFC_Subs_Collect;

        OrderDTO orderDTO = new OrderFactory.UPIMandate(merchant, theme, user)
                .setSUBS_FREQUENCY_UNIT("MONTH")
                .setTXN_AMOUNT("3")
                .setSUBS_MAX_AMOUNT("100")
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("0")
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.UPI);
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
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("HDFC")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("HDFC")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateRefundAmnt("0.00")
                .validateSubsid(Constants.ValidationType.NON_EMPTY)
                .validateTxnDate(new Date())
                .AssertAll();

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderDTO.getORDER_ID());
        Assertions.assertThat(logs).contains("\"COMPONENT\": \"SUBSCRIPTION_SERVICE\"");
        Assertions.assertThat(logs).contains(LocalConfig.PGP_HOST +"/subscription/subscription/create");

    }

// HDFC UPI Autopay - Intent Flow //

    @Feature("PGP-54077")
    @Owner(Constants.Owner.AKSHAT)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify that Subscription is successfully generated for UPI Intent flow for freq=ONDEMAND ")
    public void TC_001_HDFC_Subs_Intet_Ondemand(@Optional("true") Boolean isNativePlus) throws Exception {


        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.HDFC_Subs_Collect;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("55")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("55")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONDEMAND")
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

     String deeplink = NativeHelpers.executeProcessTxnV1(processTxnV1Request).getBody().getDeepLinkInfo().getDeepLink();

        HDFCIntentPay hdfcIntentPay = new HDFCIntentPay();
        JsonPath hdfcIntentPayRespones = hdfcIntentPay
                .setInstaURL(INSTA_Callback)
                .setDeeplink(deeplink)
                .setOrderId(initTxnDTO.orderFromBody())
                .execute().jsonPath();

        TxnStatus txnStatus = new TxnStatus(merchant.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("HDFU")
                .validateMid(merchant.getId())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateRefundAmnt("0.00")
                .validateSubsid(Constants.ValidationType.NON_EMPTY)
                .validateTxnDate(new Date())
                .AssertAll();

        CheckStatus checkStatus = new CheckStatus();
        JsonPath checkStatusResponse = checkStatus
                .setMID(merchant.getId())
                .setOrderID(initTxnDTO.orderFromBody())
                .setSubsId(subsId)
                .execute().jsonPath();

        Assertions.assertThat(checkStatusResponse.getString("body.status")).isEqualTo("ACTIVE");
        Assertions.assertThat(checkStatusResponse.getString("body.frequencyUnit")).isEqualTo("ONDEMAND");
        Assertions.assertThat(checkStatusResponse.getString("body.subsPaymentInstDetails.paymentMode")).isEqualTo("UPI");

    }


    @Feature("PGP-54077")
    @Owner(Constants.Owner.AKSHAT)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify that Subscription is successfully generated for UPI Intent flow for freq=MONTH")
    public void TC_002_HDFC_Subs_Intet_Month(@Optional("true") Boolean isNativePlus) throws Exception {


        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.HDFC_Subs_Collect;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("15")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("30")
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

        String deeplink = NativeHelpers.executeProcessTxnV1(processTxnV1Request).getBody().getDeepLinkInfo().getDeepLink();

        HDFCIntentPay hdfcIntentPay = new HDFCIntentPay();
        JsonPath hdfcIntentPayRespones = hdfcIntentPay
                .setInstaURL(INSTA_Callback)
                .setDeeplink(deeplink)
                .setOrderId(initTxnDTO.orderFromBody())
                .execute().jsonPath();

        TxnStatus txnStatus = new TxnStatus(merchant.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("HDFU")
                .validateMid(merchant.getId())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateRefundAmnt("0.00")
                .validateSubsid(Constants.ValidationType.NON_EMPTY)
                .validateTxnDate(new Date())
                .AssertAll();

        CheckStatus checkStatus = new CheckStatus();
        JsonPath checkStatusResponse = checkStatus
                .setMID(merchant.getId())
                .setOrderID(initTxnDTO.orderFromBody())
                .setSubsId(subsId)
                .execute().jsonPath();

        Assertions.assertThat(checkStatusResponse.getString("body.status")).isEqualTo("ACTIVE");
        Assertions.assertThat(checkStatusResponse.getString("body.frequencyUnit")).isEqualTo("MONTH");
        Assertions.assertThat(checkStatusResponse.getString("body.subsPaymentInstDetails.paymentMode")).isEqualTo("UPI");

    }

    @Feature("PGP-54077")
    @Owner(Constants.Owner.AKSHAT)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify that Subscription is successfully generated for UPI Intent flow for freq=YEAR")
    public void TC_003_HDFC_Subs_Intet_Year(@Optional("true") Boolean isNativePlus) throws Exception {


        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.HDFC_Subs_Collect;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("15")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("30")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("YEAR")
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

        String deeplink = NativeHelpers.executeProcessTxnV1(processTxnV1Request).getBody().getDeepLinkInfo().getDeepLink();

        HDFCIntentPay hdfcIntentPay = new HDFCIntentPay();
        JsonPath hdfcIntentPayRespones = hdfcIntentPay
                .setInstaURL(INSTA_Callback)
                .setDeeplink(deeplink)
                .setOrderId(initTxnDTO.orderFromBody())
                .execute().jsonPath();

        TxnStatus txnStatus = new TxnStatus(merchant.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("HDFU")
                .validateMid(merchant.getId())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateRefundAmnt("0.00")
                .validateSubsid(Constants.ValidationType.NON_EMPTY)
                .validateTxnDate(new Date())
                .AssertAll();

        CheckStatus checkStatus = new CheckStatus();
        JsonPath checkStatusResponse = checkStatus
                .setMID(merchant.getId())
                .setOrderID(initTxnDTO.orderFromBody())
                .setSubsId(subsId)
                .execute().jsonPath();

        Assertions.assertThat(checkStatusResponse.getString("body.status")).isEqualTo("ACTIVE");
        Assertions.assertThat(checkStatusResponse.getString("body.frequencyUnit")).isEqualTo("YEAR");
        Assertions.assertThat(checkStatusResponse.getString("body.subsPaymentInstDetails.paymentMode")).isEqualTo("UPI");

    }

    @Feature("PGP-54077")
    @Owner(Constants.Owner.AKSHAT)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify that Subscription is successfully generated for UPI Intent flow for freq=WEEK")
    public void TC_004_HDFC_Subs_Intet_WEEK(@Optional("true") Boolean isNativePlus) throws Exception {


        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.HDFC_Subs_Collect;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("25")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("30")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("WEEK")
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

        String deeplink = NativeHelpers.executeProcessTxnV1(processTxnV1Request).getBody().getDeepLinkInfo().getDeepLink();

        HDFCIntentPay hdfcIntentPay = new HDFCIntentPay();
        JsonPath hdfcIntentPayRespones = hdfcIntentPay
                .setInstaURL(INSTA_Callback)
                .setDeeplink(deeplink)
                .setOrderId(initTxnDTO.orderFromBody())
                .execute().jsonPath();

        TxnStatus txnStatus = new TxnStatus(merchant.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("HDFU")
                .validateMid(merchant.getId())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateRefundAmnt("0.00")
                .validateSubsid(Constants.ValidationType.NON_EMPTY)
                .validateTxnDate(new Date())
                .AssertAll();

        CheckStatus checkStatus = new CheckStatus();
        JsonPath checkStatusResponse = checkStatus
                .setMID(merchant.getId())
                .setOrderID(initTxnDTO.orderFromBody())
                .setSubsId(subsId)
                .execute().jsonPath();

        Assertions.assertThat(checkStatusResponse.getString("body.status")).isEqualTo("ACTIVE");
        Assertions.assertThat(checkStatusResponse.getString("body.frequencyUnit")).isEqualTo("WEEK");
        Assertions.assertThat(checkStatusResponse.getString("body.subsPaymentInstDetails.paymentMode")).isEqualTo("UPI");

    }

    @Feature("PGP-54077")
    @Owner(Constants.Owner.AKSHAT)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify that Subscription is successfully generated for UPI Intent flow for freq=BI_MONTHLY")
    public void TC_005_HDFC_Subs_Intet_BIMONTHLY(@Optional("true") Boolean isNativePlus) throws Exception {


        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.HDFC_Subs_Collect;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("25")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("30")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("BI_MONTHLY")
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

        String deeplink = NativeHelpers.executeProcessTxnV1(processTxnV1Request).getBody().getDeepLinkInfo().getDeepLink();

        HDFCIntentPay hdfcIntentPay = new HDFCIntentPay();
        JsonPath hdfcIntentPayRespones = hdfcIntentPay
                .setInstaURL(INSTA_Callback)
                .setDeeplink(deeplink)
                .setOrderId(initTxnDTO.orderFromBody())
                .execute().jsonPath();

        TxnStatus txnStatus = new TxnStatus(merchant.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("HDFU")
                .validateMid(merchant.getId())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateRefundAmnt("0.00")
                .validateSubsid(Constants.ValidationType.NON_EMPTY)
                .validateTxnDate(new Date())
                .AssertAll();

        CheckStatus checkStatus = new CheckStatus();
        JsonPath checkStatusResponse = checkStatus
                .setMID(merchant.getId())
                .setOrderID(initTxnDTO.orderFromBody())
                .setSubsId(subsId)
                .execute().jsonPath();

        Assertions.assertThat(checkStatusResponse.getString("body.status")).isEqualTo("ACTIVE");
        Assertions.assertThat(checkStatusResponse.getString("body.frequencyUnit")).isEqualTo("BI_MONTHLY");
        Assertions.assertThat(checkStatusResponse.getString("body.subsPaymentInstDetails.paymentMode")).isEqualTo("UPI");

    }

    @Feature("PGP-54077")
    @Owner(Constants.Owner.AKSHAT)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify that Subscription is successfully generated for UPI Intent flow for freq=QUARTER")
    public void TC_006_HDFC_Subs_Intet_QUARTER(@Optional("true") Boolean isNativePlus) throws Exception {


        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.HDFC_Subs_Collect;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("30")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("QUARTER")
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

        String deeplink = NativeHelpers.executeProcessTxnV1(processTxnV1Request).getBody().getDeepLinkInfo().getDeepLink();

        HDFCIntentPay hdfcIntentPay = new HDFCIntentPay();
        JsonPath hdfcIntentPayRespones = hdfcIntentPay
                .setInstaURL(INSTA_Callback)
                .setDeeplink(deeplink)
                .setOrderId(initTxnDTO.orderFromBody())
                .execute().jsonPath();

        TxnStatus txnStatus = new TxnStatus(merchant.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("HDFU")
                .validateMid(merchant.getId())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateRefundAmnt("0.00")
                .validateSubsid(Constants.ValidationType.NON_EMPTY)
                .validateTxnDate(new Date())
                .AssertAll();

        CheckStatus checkStatus = new CheckStatus();
        JsonPath checkStatusResponse = checkStatus
                .setMID(merchant.getId())
                .setOrderID(initTxnDTO.orderFromBody())
                .setSubsId(subsId)
                .execute().jsonPath();

        Assertions.assertThat(checkStatusResponse.getString("body.status")).isEqualTo("ACTIVE");
        Assertions.assertThat(checkStatusResponse.getString("body.frequencyUnit")).isEqualTo("QUARTER");
        Assertions.assertThat(checkStatusResponse.getString("body.subsPaymentInstDetails.paymentMode")).isEqualTo("UPI");

    }


    @Feature("PGP-54077")
    @Owner(Constants.Owner.AKSHAT)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify that Subscription is successfully generated for UPI Intent flow for freq=SEMI_ANNUALLY")
    public void TC_007_HDFC_Subs_Intet_SEMIANNUALLY(@Optional("true") Boolean isNativePlus) throws Exception {


        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.HDFC_Subs_Collect;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("30")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("SEMI_ANNUALLY")
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

        String deeplink = NativeHelpers.executeProcessTxnV1(processTxnV1Request).getBody().getDeepLinkInfo().getDeepLink();

        HDFCIntentPay hdfcIntentPay = new HDFCIntentPay();
        JsonPath hdfcIntentPayRespones = hdfcIntentPay
                .setInstaURL(INSTA_Callback)
                .setDeeplink(deeplink)
                .setOrderId(initTxnDTO.orderFromBody())
                .execute().jsonPath();

        TxnStatus txnStatus = new TxnStatus(merchant.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("HDFU")
                .validateMid(merchant.getId())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateRefundAmnt("0.00")
                .validateSubsid(Constants.ValidationType.NON_EMPTY)
                .validateTxnDate(new Date())
                .AssertAll();

        CheckStatus checkStatus = new CheckStatus();
        JsonPath checkStatusResponse = checkStatus
                .setMID(merchant.getId())
                .setOrderID(initTxnDTO.orderFromBody())
                .setSubsId(subsId)
                .execute().jsonPath();

        Assertions.assertThat(checkStatusResponse.getString("body.status")).isEqualTo("ACTIVE");
        Assertions.assertThat(checkStatusResponse.getString("body.frequencyUnit")).isEqualTo("SEMI_ANNUALLY");
        Assertions.assertThat(checkStatusResponse.getString("body.subsPaymentInstDetails.paymentMode")).isEqualTo("UPI");

    }

    @Feature("PG-7176")
    @Owner(Constants.Owner.AKSHAT)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify ONDEMAND native subscription with payerAccountDetails in init txn request")
    public void TC_001_Native_Subs_ONDEMAND_withPayerAccountDetails(@Optional("true") Boolean isNativePlus) throws Exception {

        /* ff4j flag theia.enablePayerAccountDetails should be enabled */

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.HDFC_Subs_Collect;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("0")
                .setSubscriptionEnableRetry("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_SUBSCRIPTION)
                .setPayerAccountDetails(
                        new PayerAccountDetail("103567890458", "HDFC0000975", "Akshat"),
                        new PayerAccountDetail("103567890456", "SBIN0000123", "Akshat"))
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

        validateSuccessResponsePage(initTxnDTO, orderDTO, subsId, merchant.getKey());

        validateSuccessTxnStatus(initTxnDTO, orderDTO, subsId);
    }

    @Feature("PG-7176")
    @Owner(Constants.Owner.AKSHAT)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify HDFC UPI Intent ONDEMAND subscription with payerAccountDetails in init txn request")
    public void TC_001_HDFC_Subs_Intet_Ondemand_withPayerAccountDetails(@Optional("true") Boolean isNativePlus) throws Exception {

        /* ff4j flag theia.enablePayerAccountDetails should be enabled */

        Constants.MerchantType merchant = Constants.MerchantType.HDFC_Subs_Collect;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("55")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("55")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_SUBSCRIPTION)
                .setPayerAccountDetails(
                        new PayerAccountDetail("103567890458", "HDFC0000975", "Akshat"),
                        new PayerAccountDetail("103567890456", "SBIN0000123", "Akshat"))
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

        String deeplink = NativeHelpers.executeProcessTxnV1(processTxnV1Request).getBody().getDeepLinkInfo().getDeepLink();


        HDFCIntentPay hdfcIntentPay = new HDFCIntentPay();
        hdfcIntentPay
                .setInstaURL(INSTA_Callback)
                .setDeeplink(deeplink)
                .setOrderId(initTxnDTO.orderFromBody())
                .execute().jsonPath();

        TxnStatus txnStatus = new TxnStatus(merchant.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("HDFU")
                .validateMid(merchant.getId())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateRefundAmnt("0.00")
                .validateSubsid(Constants.ValidationType.NON_EMPTY)
                .validateTxnDate(new Date())
                .AssertAll();

        CheckStatus checkStatus = new CheckStatus();
        JsonPath checkStatusResponse = checkStatus
                .setMID(merchant.getId())
                .setOrderID(initTxnDTO.orderFromBody())
                .setSubsId(subsId)
                .execute().jsonPath();

        Assertions.assertThat(checkStatusResponse.getString("body.status")).isEqualTo("ACTIVE");
        Assertions.assertThat(checkStatusResponse.getString("body.frequencyUnit")).isEqualTo("ONDEMAND");
        Assertions.assertThat(checkStatusResponse.getString("body.subsPaymentInstDetails.paymentMode")).isEqualTo("UPI");

    }


}
