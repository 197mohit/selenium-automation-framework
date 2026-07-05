package scripts.UPISubsMandate;

import com.paytm.api.nativeAPI.RenewSubscription;
import com.paytm.LocalConfig;
import com.paytm.api.TxnStatus;
import com.paytm.api.subscription.CheckStatus;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.*;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.RenewSubscription.RenewSubscriptionDTO;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.response.FetchPaymentOptResponseDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.api.nativeAPI.FetchPaymentOptionV5;
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
import org.testng.Assert;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.Date;


public class UPISubsAxisOTM extends PGPBaseTest {

    private final static String NATIVE_SUBSCRIPTION = "NATIVE_SUBSCRIPTION";
    private final static String INSTA_Callback = LocalConfig.PGP_HOST + "/instaproxy/bankresponse/HDFC/UPI/RESP";
    private final static String NATIVE_MF_SIP = "NATIVE_MF_SIP";
    private final CheckoutPage checkoutPage = new CheckoutPage();
    private final static String NATIVE_RENEW_MF_SIP = "SUBS_RENEWAL_MF_SIP";
    private final CheckoutJsCheckoutPage checkoutJSPage = new CheckoutJsCheckoutPage();

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
               // .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("AXIS")
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
        responsePage
              //  .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
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

    @Feature("PGP-59264")
    @Owner(Constants.Owner.AKSHAT)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify that OTM creation is failed when expiry is over 14 days")
    public void TC_001_OTM_expiryFailure(@Optional("true") Boolean isNativePlus) throws Exception {

        Constants.MerchantType merchant = Constants.MerchantType.Axis_OTM;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("0")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("1000")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONETIME")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_SUBSCRIPTION)
                .build();
        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);

        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultStatus())
                .as("resultStatus mismatch")
                .isEqualTo("TXN_FAILURE");
/*        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultCode())
                .as("resultCode mismatch")
                .isEqualTo("4001");*/
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultMsg())
                .as("resultMsg mismatch")
                .isEqualToIgnoringCase("Expiry date for one time mandate cannot be greater than 14 days from the subscription start date (including start date)");

    }



    @Feature("PGP-59264")
    @Owner(Constants.Owner.AKSHAT)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify that OTM creation is failed when amount is greater then 2 lakh")
    public void TC_002_OTM_over2lakh(@Optional("true") Boolean isNativePlus) throws Exception {

        Constants.MerchantType merchant = Constants.MerchantType.Axis_OTM;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("0")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("200001")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONETIME")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setSubscriptionExpiryDate(CommonHelpers.getDate().plusDays(13).toString())
                .setRequestType(NATIVE_SUBSCRIPTION)
                .build();
        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);

        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultStatus())
                .as("resultStatus mismatch")
                .isEqualTo("TXN_FAILURE");
/*        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultCode())
                .as("resultCode mismatch")
                .isEqualTo("2022");*/
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultMsg())
                .as("resultMsg mismatch")
                .isEqualToIgnoringCase("Max amount is greater than the maximum permissible amount i.e 200,000");

    }

    @Feature("PGP-59264")
    @Owner(Constants.Owner.AKSHAT)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify that OTM creation is failed when amount is greater then 2 lakh")
    public void TC_003_OTM_FIX(@Optional("true") Boolean isNativePlus) throws Exception {

        Constants.MerchantType merchant = Constants.MerchantType.Axis_OTM;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("0")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("2000")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONETIME")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setSubscriptionExpiryDate(CommonHelpers.getDate().plusDays(13).toString())
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
                .isEqualToIgnoringCase("SubscriptionAmountType cannot be FIX for OTM or SBMD_ONDEMAND");

    }

    @Feature("PGP-59264")
    @Owner(Constants.Owner.AKSHAT)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify that OTM creation is failed when transaction amount is greater then 0")
    public void TC_004_OTM_amountGreaterThen0(@Optional("true") Boolean isNativePlus) throws Exception {

        Constants.MerchantType merchant = Constants.MerchantType.Axis_OTM;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("5")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("20000")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONETIME")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setSubscriptionExpiryDate(CommonHelpers.getDate().plusDays(13).toString())
                .setRequestType(NATIVE_SUBSCRIPTION)
                .build();
        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);

        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultStatus())
                .as("resultStatus mismatch")
                .isEqualTo("TXN_FAILURE");
/*        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultCode())
                .as("resultCode mismatch")
                .isEqualTo("4001");*/
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultMsg())
                .as("resultMsg mismatch")
                .isEqualToIgnoringCase("Transaction amount cannot be greater than zero for One Time Mandates");

    }


    @Feature("PGP-59264")
    @Owner(Constants.Owner.AKSHAT)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify that OTM creation is failed when paymode is other then UPI")
    public void TC_005_OTM_nonUPI(@Optional("true") Boolean isNativePlus) throws Exception {

        Constants.MerchantType merchant = Constants.MerchantType.Axis_OTM;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("0")
                .setSubscriptionPaymentMode("CREDIT_CARD")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("20000")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONETIME")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setSubscriptionExpiryDate(CommonHelpers.getDate().plusDays(13).toString())
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
                .isEqualToIgnoringCase("Paymode selected is not enabled for your merchant account. Please reach out to support teams to enable");

    }


    @Feature("PGP-59264")
    @Owner(Constants.Owner.AKSHAT)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify that OTM is successfully created for requestType=NativeSubscription")
    public void TC_006_OTM_NativeSubscription(@Optional("true") Boolean isNativePlus) throws Exception {

        Constants.MerchantType merchant = Constants.MerchantType.Axis_OTM;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("0")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("200000")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONETIME")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setSubscriptionExpiryDate(CommonHelpers.getDate().plusDays(13).toString())
                .setRequestType(NATIVE_SUBSCRIPTION)
                .build();
        InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);

    }

    @Feature("PGP-59264")
    @Owner(Constants.Owner.AKSHAT)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify that OTM is successfully created for requestType=NATIVE_MF_SIP")
    public void TC_007_OTM_MF_SIP(@Optional("true") Boolean isNativePlus) throws Exception {

        Constants.MerchantType merchant = Constants.MerchantType.Axis_OTM;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("0")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("200000")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONETIME")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setSubscriptionExpiryDate(CommonHelpers.getDate().plusDays(13).toString())
                .setRequestType(NATIVE_MF_SIP)
                .build();
        InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);

    }


    @Feature("PGP-59264")
    @Owner(Constants.Owner.AKSHAT)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify that order is successfully created for 0 amount for Native Subscription")
    public void TC_008_OTM_NativeSubscription(@Optional("true") Boolean isNativePlus) throws Exception {

        Constants.MerchantType merchant = Constants.MerchantType.Axis_OTM;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("0")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("200000")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONETIME")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setSubscriptionExpiryDate(CommonHelpers.getDate().plusDays(13).toString())
                .setRequestType(NATIVE_SUBSCRIPTION)
                .build();
        InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, initTxnDTO.orderFromBody(), "ACQUIRING_CREATE_ORDER");
        Assertions.assertThat(logs).contains("\"orderAmount\":{\"currency\":\"INR\",\"value\":0}");
        Assertions.assertThat(logs).contains("\"totalTxnAmount\":\"0\"");
        Assertions.assertThat(logs).contains("\"payConfirmFlowType\":\"MANDATE_AUTH\"");

    }


    @Feature("PGP-59264")
    @Owner(Constants.Owner.AKSHAT)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify that order is successfully created for 0 amount for MF_SIP")
    public void TC_009_OTM_nativeMFSIP(@Optional("true") Boolean isNativePlus) throws Exception {

        Constants.MerchantType merchant = Constants.MerchantType.Axis_OTM;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("0")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("200000")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONETIME")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setSubscriptionExpiryDate(CommonHelpers.getDate().plusDays(13).toString())
                .setRequestType(NATIVE_MF_SIP)
                .build();
        InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, initTxnDTO.orderFromBody(), "ACQUIRING_CREATE_ORDER");
        Assertions.assertThat(logs).contains("\"orderAmount\":{\"currency\":\"INR\",\"value\":0}");
        Assertions.assertThat(logs).contains("\"totalTxnAmount\":\"0\"");
        Assertions.assertThat(logs).contains("\"payConfirmFlowType\":\"MANDATE_AUTH\"");

    }


    @Feature("PGP-59264")
    @Owner(Constants.Owner.AKSHAT)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify that in ONEIME is returned in fpo response for subscription")
    public void TC_010_OTM_inFPOresponse(@Optional("true") Boolean isNativePlus) throws Exception {

        Constants.MerchantType merchant = Constants.MerchantType.Axis_OTM;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("0")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("200000")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONETIME")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setSubscriptionExpiryDate(CommonHelpers.getDate().plusDays(13).toString())
                .setRequestType(NATIVE_SUBSCRIPTION)
                .build();
        InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);

        String txnToken = responseDTO.getBody().getTxnToken();
        String subsId = responseDTO.getBody().getSubscriptionId();
        String orderId = initTxnDTO.getBody().getOrderId();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(),
                orderId, fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assert.assertTrue(fetchPaymentOptionsJson.getString("body.subscriptionDetail.subsfrequency").equals("ONETIME"));


    }

    @Feature("PGP-59264")
    @Owner(Constants.Owner.AKSHAT)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify that in ONEIME is returned in fpo response for MF_SIP ")
    public void TC_011_OTM_inFPOresponse(@Optional("true") Boolean isNativePlus) throws Exception {

        Constants.MerchantType merchant = Constants.MerchantType.Axis_OTM;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("0")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("200000")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONETIME")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setSubscriptionExpiryDate(CommonHelpers.getDate().plusDays(13).toString())
                .setRequestType(NATIVE_MF_SIP)
                .build();
        InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);

        String txnToken = responseDTO.getBody().getTxnToken();
        String subsId = responseDTO.getBody().getSubscriptionId();
        String orderId = initTxnDTO.getBody().getOrderId();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(),
                orderId, fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assert.assertTrue(fetchPaymentOptionsJson.getString("body.subscriptionDetail.subsfrequency").equals("ONETIME"));


    }

    @Feature("PGP-59265")
    @Owner(Constants.Owner.AKSHAT)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify that OTM=true mandate is e-2-e created for native_subscription")
    public void TC_012_OTM_Subs_CreateSuccess(@Optional("true") Boolean isNativePlus) throws Exception {

        Constants.MerchantType merchant = Constants.MerchantType.Axis_OTM;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("0")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("1000")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONETIME")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setSubscriptionExpiryDate(CommonHelpers.getDate().plusDays(13).toString())

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

    @Feature("PGP-59265")
    @Owner(Constants.Owner.AKSHAT)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify that OTM=true mandate is e-2-e created for mf_sip")
    public void TC_013_OTM_mfSIP_CreateSuccess(@Optional("true") Boolean isNativePlus) throws Exception {

        Constants.MerchantType merchant = Constants.MerchantType.Axis_OTM;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("0")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("1000")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONETIME")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setSubscriptionExpiryDate(CommonHelpers.getDate().plusDays(13).toString())
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

    }

    @Feature("PGP-59265")
    @Owner(Constants.Owner.AKSHAT)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify that debit/renewals are success for OTM transactions")
    public void TC_014_OTM_Subs_DebitSuccess(@Optional("true") Boolean isNativePlus) throws Exception {

        Constants.MerchantType merchant = Constants.MerchantType.Axis_OTM;
        String renewalAmount = "120";

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("0")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("1000")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONETIME")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setSubscriptionExpiryDate(CommonHelpers.getDate().plusDays(13).toString())

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

        String orderId = executeRenewalAndFetchOrderId(merchant, subsId, renewalAmount, NATIVE_SUBSCRIPTION);

        TxnStatus txnStatus = new TxnStatus(merchant.getId(), orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                //  .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(renewalAmount)
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("AXIRC1IN")
                .validateMid(merchant.getId())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateRefundAmnt("0.00")
                .validateSubsid(Constants.ValidationType.NON_EMPTY)
                .validateTxnDate(new Date())
                .AssertAll();

    }

    @Feature("PGP-59265")
    @Owner(Constants.Owner.AKSHAT)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify that debit/renewals are success for OTM_SIP transactions")
    public void TC_015_OTM_mfSIP_DebitSuccess(@Optional("true") Boolean isNativePlus) throws Exception {

        Constants.MerchantType merchant = Constants.MerchantType.Axis_OTM;
        String renewalAmount = "100";

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("0")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("1000")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONETIME")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setSubscriptionExpiryDate(CommonHelpers.getDate().plusDays(13).toString())
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

        String orderId = executeRenewalAndFetchOrderId(merchant, subsId, renewalAmount, NATIVE_RENEW_MF_SIP);

        TxnStatus txnStatus = new TxnStatus(merchant.getId(), orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                //  .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(renewalAmount)
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("AXIRC1IN")
                .validateMid(merchant.getId())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateRefundAmnt("0.00")
                .validateSubsid(Constants.ValidationType.NON_EMPTY)
                .validateTxnDate(new Date())
                .AssertAll();


    }

}
