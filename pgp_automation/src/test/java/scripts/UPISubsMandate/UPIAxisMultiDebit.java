package scripts.UPISubsMandate;

import com.paytm.api.nativeAPI.RenewSubscription;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.api.TxnStatus;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.*;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.response.FetchPaymentOptResponseDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.NativeDTO.RenewSubscription.RenewSubscriptionDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.api.nativeAPI.FetchPaymentOptionV5;
import com.paytm.pages.ResponsePage;
import com.paytm.pages.CheckoutPage;

import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.qameta.allure.Step;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.testng.Assert;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.Date;


public class UPIAxisMultiDebit extends PGPBaseTest {

    // AI-Generated: 2025-01-08 - Class creation based on UPISubsAxisOTM with SBDM_ONDEMAND subscription type
    private final static String NATIVE_SUBSCRIPTION = "NATIVE_SUBSCRIPTION";
    private final static String NATIVE_MF_SIP = "NATIVE_MF_SIP";
    private final CheckoutPage checkoutPage = new CheckoutPage();
    private final static String NATIVE_RENEW_MF_SIP = "SUBS_RENEWAL_MF_SIP";

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
              //  .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
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
            //    .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
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
                .validateGatewayName("AXIS")
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

    @Feature("PGP-60974")
    @Owner(Constants.Owner.AKSHAT)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify that SBMD creation is failed when expiry is over 180 days")
    public void TC_001_SBMD_expiryFailure(@Optional("true") Boolean isNativePlus) throws Exception {

        Constants.MerchantType merchant = Constants.MerchantType.AXIS_SBMD_ONDEMAND;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("0")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("1000")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("SBMD_ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setSubscriptionExpiryDate(CommonHelpers.getDate().plusDays(181).toString())
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
                .isEqualToIgnoringCase("Subscription Expiry Date cannot be greater than 180 days from Subscription Start Date");

    }



    @Feature("PGP-60974")
    @Owner(Constants.Owner.AKSHAT)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify that SBMD creation is failed when amount is greater then 1 lakh")
    public void TC_002_SBMD_over1lakh(@Optional("true") Boolean isNativePlus) throws Exception {

        Constants.MerchantType merchant = Constants.MerchantType.AXIS_SBMD_ONDEMAND;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("0")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("100001")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("SBMD_ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setSubscriptionExpiryDate(CommonHelpers.getDate().plusDays(180).toString())
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
                .isEqualToIgnoringCase("Max amount is greater than the maximum permissible amount i.e 100,000");

    }

    @Feature("PGP-60974")
    @Owner(Constants.Owner.AKSHAT)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify that SBMD creation is failed when frequencyUnit is FIX")
    public void TC_003_SBMD_FIX(@Optional("true") Boolean isNativePlus) throws Exception {

        Constants.MerchantType merchant = Constants.MerchantType.AXIS_SBMD_ONDEMAND;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("0")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("2000")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("SBMD_ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setSubscriptionExpiryDate(CommonHelpers.getDate().plusDays(180).toString())
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

    @Feature("PGP-60974")
    @Owner(Constants.Owner.AKSHAT)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify that SBMD creation is failed when transaction amount is greater then 0")
    public void TC_004_SBMD_amountGreaterThen0(@Optional("true") Boolean isNativePlus) throws Exception {

        Constants.MerchantType merchant = Constants.MerchantType.AXIS_SBMD_ONDEMAND;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("5")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("20000")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("SBMD_ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setSubscriptionExpiryDate(CommonHelpers.getDate().plusDays(180).toString())
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
                .isEqualToIgnoringCase("Transaction amount must be zero for SBMD registration");

    }



    @Feature("PGP-60974")
    @Owner(Constants.Owner.AKSHAT)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify that SBMD is successfully created for requestType=NativeSubscription")
    public void TC_005_SBMD_NativeSubscription(@Optional("true") Boolean isNativePlus) throws Exception {

        Constants.MerchantType merchant = Constants.MerchantType.AXIS_SBMD_ONDEMAND;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("0")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("100000")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("SBMD_ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setSubscriptionExpiryDate(CommonHelpers.getDate().plusDays(180).toString())
                .setRequestType(NATIVE_SUBSCRIPTION)
                .build();
        InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);

    }

    @Feature("PGP-60974")
    @Owner(Constants.Owner.AKSHAT)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify that SBMD is successfully created for requestType=NATIVE_MF_SIP")
    public void TC_006_SBMD_MF_SIP(@Optional("true") Boolean isNativePlus) throws Exception {

        Constants.MerchantType merchant = Constants.MerchantType.AXIS_SBMD_ONDEMAND_SIP;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("0")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("45000")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("SBMD_ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setSubscriptionExpiryDate(CommonHelpers.getDate().plusDays(180).toString())
                .setRequestType(NATIVE_MF_SIP)
                .build();
        InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);

    }


    @Feature("PGP-60974")
    @Owner(Constants.Owner.AKSHAT)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify that order is successfully created for 0 amount for Native Subscription")
    public void TC_007_SBMD_NativeSubscription(@Optional("true") Boolean isNativePlus) throws Exception {

        Constants.MerchantType merchant = Constants.MerchantType.AXIS_SBMD_ONDEMAND;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("0")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("100000")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("SBMD_ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setSubscriptionExpiryDate(CommonHelpers.getDate().plusDays(180).toString())
                .setRequestType(NATIVE_SUBSCRIPTION)
                .build();
        InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, initTxnDTO.orderFromBody(), "ACQUIRING_CREATE_ORDER");
        Assertions.assertThat(logs).contains("\"orderAmount\":{\"currency\":\"INR\",\"value\":0}");
        Assertions.assertThat(logs).contains("\"totalTxnAmount\":\"0\"");
        Assertions.assertThat(logs).contains("\"payConfirmFlowType\":\"MANDATE_AUTH\"");

    }


    @Feature("PGP-60974")
    @Owner(Constants.Owner.AKSHAT)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify that order is successfully created for 0 amount for MF_SIP")
    public void TC_008_SBMD_nativeMFSIP(@Optional("true") Boolean isNativePlus) throws Exception {

        Constants.MerchantType merchant = Constants.MerchantType.AXIS_SBMD_ONDEMAND_SIP;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("0")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("50000")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("SBMD_ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setSubscriptionExpiryDate(CommonHelpers.getDate().plusDays(180).toString())
                .setRequestType(NATIVE_MF_SIP)
                .build();
        InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, initTxnDTO.orderFromBody(), "ACQUIRING_CREATE_ORDER");
        Assertions.assertThat(logs).contains("\"orderAmount\":{\"currency\":\"INR\",\"value\":0}");
        Assertions.assertThat(logs).contains("\"totalTxnAmount\":\"0\"");
        Assertions.assertThat(logs).contains("\"payConfirmFlowType\":\"MANDATE_AUTH\"");

    }


    @Feature("PGP-60974")
    @Owner(Constants.Owner.AKSHAT)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify that in SBDM_ONDEMAND is returned in fpo response for subscription")
    public void TC_009_SBMD_inFPOresponse_subs(@Optional("true") Boolean isNativePlus) throws Exception {

        Constants.MerchantType merchant = Constants.MerchantType.AXIS_SBMD_ONDEMAND;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("0")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("100000")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("SBMD_ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setSubscriptionExpiryDate(CommonHelpers.getDate().plusDays(180).toString())
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
        Assert.assertTrue(fetchPaymentOptionsJson.getString("body.subscriptionDetail.subsfrequency").equals("SBMD_ONDEMAND"));


    }

    @Feature("PGP-60974")
    @Owner(Constants.Owner.AKSHAT)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify that in SBMD_ONDEMAND is returned in fpo response for MF_SIP ")
    public void TC_010_SBDM_inFPOresponse_sip(@Optional("true") Boolean isNativePlus) throws Exception {

        Constants.MerchantType merchant = Constants.MerchantType.AXIS_SBMD_ONDEMAND_SIP;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("0")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("50000")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("SBMD_ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setSubscriptionExpiryDate(CommonHelpers.getDate().plusDays(180).toString())
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
        Assert.assertTrue(fetchPaymentOptionsJson.getString("body.subscriptionDetail.subsfrequency").equals("SBMD_ONDEMAND"));


    }

    @Feature("PGP-60974")
    @Owner(Constants.Owner.AKSHAT)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify that in SBDM_ONDEMAND is returned in fpo response for subscription")
    public void TC_011_SBMD_ccUPI_FPOresponse(@Optional("true") Boolean isNativePlus) throws Exception {

        Constants.MerchantType merchant = Constants.MerchantType.AXIS_SBMD_ONDEMAND;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("0")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("100000")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("SBMD_ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setSubscriptionExpiryDate(CommonHelpers.getDate().plusDays(180).toString())
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
        Assert.assertTrue(fetchPaymentOptionsJson.getString("body.ccOnUPIAllowed").equals("true"));


    }

    @Feature("PGP-60974")
    @Owner(Constants.Owner.AKSHAT)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify that in UPI_CREDITLINE is returned in fpo response for subscription")
    public void TC_012_SBMD_upiCreditLine_FPOresponse(@Optional("true") Boolean isNativePlus) throws Exception {

        Constants.MerchantType merchant = Constants.MerchantType.AXIS_SBMD_ONDEMAND;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("0")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("100000")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("SBMD_ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setSubscriptionExpiryDate(CommonHelpers.getDate().plusDays(180).toString())
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
        Assert.assertTrue(fetchPaymentOptionsJson.getString("body.upiSubTypeMerchantEligibility").contains("UPI_CREDITLINE"));

    }

    @Feature("PGP-60974")
    @Owner(Constants.Owner.AKSHAT)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify that isSBMD=true is passed in ACQUIRING_PAY_ORDER request for subscription")
    public void TC_013_SBMD_Subs_ACQUIRING_PAY_ORDER(@Optional("true") Boolean isNativePlus) throws Exception {

        Constants.MerchantType merchant = Constants.MerchantType.AXIS_SBMD_ONDEMAND;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("0")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10000")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("SBMD_ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setSubscriptionExpiryDate(CommonHelpers.getDate().plusDays(180).toString())

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
        Assertions.assertThat(logs).contains("\"isSBMD\":\"true\"");

    }

    @Feature("PGP-60974")
    @Owner(Constants.Owner.AKSHAT)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify that isSBMD=true is passed in ACQUIRING_PAY_ORDER request for mf_sip")
    public void TC_014_SBMD_mfSIP_ACQUIRING_PAY_ORDER(@Optional("true") Boolean isNativePlus) throws Exception {

        Constants.MerchantType merchant = Constants.MerchantType.AXIS_SBMD_ONDEMAND_SIP;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("0")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10000")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("SBMD_ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setSubscriptionExpiryDate(CommonHelpers.getDate().plusDays(180).toString())
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
        Assertions.assertThat(logs).contains("\"isSBMD\":\"true\"");

    }


    @Feature("PGP-61022")
    @Owner(Constants.Owner.AKSHAT)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify that isSBMD=true mandate is e-2-e created for native_subscription")
    public void TC_015_SBMD_Subs_CreateSuccess(@Optional("true") Boolean isNativePlus) throws Exception {

        Constants.MerchantType merchant = Constants.MerchantType.AXIS_SBMD_ONDEMAND;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("0")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("1000")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("SBMD_ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setSubscriptionExpiryDate(CommonHelpers.getDate().plusDays(180).toString())

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

    @Feature("PGP-61022")
    @Owner(Constants.Owner.AKSHAT)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify that isSBMD=true mandate is e-2-e created for mf_sip")
    public void TC_016_SBMD_mfSIP_CreateSuccess(@Optional("true") Boolean isNativePlus) throws Exception {

        Constants.MerchantType merchant = Constants.MerchantType.AXIS_SBMD_ONDEMAND_SIP;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("0")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("1000")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("SBMD_ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setSubscriptionExpiryDate(CommonHelpers.getDate().plusDays(180).toString())
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

    @Feature("PGP-61022")
    @Owner(Constants.Owner.AKSHAT)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify that multi debit/renewals are success for SBMD transactions")
    public void TC_017_SBMD_Subs_DebitSuccess(@Optional("true") Boolean isNativePlus) throws Exception {

        Constants.MerchantType merchant = Constants.MerchantType.AXIS_SBMD_ONDEMAND;
        String renewalAmounta = "120";
        String renewalAmountb = "150";

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("0")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("1000")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("SBMD_ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setSubscriptionExpiryDate(CommonHelpers.getDate().plusDays(180).toString())

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

        String orderId = executeRenewalAndFetchOrderId(merchant, subsId, renewalAmounta, NATIVE_SUBSCRIPTION);

        TxnStatus txnStatus = new TxnStatus(merchant.getId(), orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                //  .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(renewalAmounta)
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

        String orderId_two = executeRenewalAndFetchOrderId(merchant, subsId, renewalAmountb, NATIVE_SUBSCRIPTION);

        TxnStatus txnStatus_two = new TxnStatus(merchant.getId(), orderId_two);
        txnStatus_two.executeUntilNotPending();
        txnStatus_two.validateTxnId(Constants.ValidationType.NON_EMPTY)
                //  .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId_two)
                .validateTxnAmount(renewalAmountb)
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

    @Feature("PGP-61022")
    @Owner(Constants.Owner.AKSHAT)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify that multi debit/renewals are success for SBMD_SIP transactions")
    public void TC_018_SBMD_mfSIP_DebitSuccess(@Optional("true") Boolean isNativePlus) throws Exception {

        Constants.MerchantType merchant = Constants.MerchantType.AXIS_SBMD_ONDEMAND_SIP;
        String renewalAmounta = "120";
        String renewalAmountb = "150";

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("0")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("1000")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("SBMD_ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setSubscriptionExpiryDate(CommonHelpers.getDate().plusDays(180).toString())
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

        String orderId = executeRenewalAndFetchOrderId(merchant, subsId, renewalAmounta, NATIVE_RENEW_MF_SIP);

        TxnStatus txnStatus = new TxnStatus(merchant.getId(), orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
              //  .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(renewalAmounta)
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

        String orderId_two = executeRenewalAndFetchOrderId(merchant, subsId, renewalAmountb, NATIVE_RENEW_MF_SIP);

        TxnStatus txnStatus_two = new TxnStatus(merchant.getId(), orderId_two);
        txnStatus_two.executeUntilNotPending();
        txnStatus_two.validateTxnId(Constants.ValidationType.NON_EMPTY)
                //  .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId_two)
                .validateTxnAmount(renewalAmountb)
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
