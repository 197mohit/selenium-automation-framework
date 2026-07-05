package scripts.Native.subscription;

import com.paytm.LocalConfig;
import com.paytm.ServerConfigProvider;
import com.paytm.api.PreNotify;
import com.paytm.api.ProcessTransactionV1;
import com.paytm.api.TransactionStatusV1API;
import com.paytm.api.TxnStatus;
import com.paytm.api.nativeAPI.*;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.*;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.CancelSubscription.CancelSubscriptionDTO;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.response.FetchPaymentOptResponseDTO;
import com.paytm.dto.NativeDTO.InitTxn.EnablePaymentMode;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.MandateAccountDetails;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.NativeDTO.RenewSubscription.RenewSubscriptionDTO;
import com.paytm.dto.NativeDTO.fetchBinDetails.FetchBinDetailsRequest;
import com.paytm.dto.NativeDTO.fetchBinDetails.response.BinDetail;
import com.paytm.dto.NativeDTO.fetchBinDetails.response.FetchBinDetailResponse;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.TransactionStatusV1.Body;
import com.paytm.dto.TransactionStatusV1.Head;
import com.paytm.dto.TransactionStatusV1.TransactionStatusV1DTO;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.framework.core.DriverManager;
import com.paytm.framework.reporting.Owners;
import com.paytm.framework.utils.CommonUtils;
import com.paytm.framework.utils.DatabaseUtil;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
import com.paytm.pages.*;
import com.paytm.utils.ff4j.FF4JFlags;
import com.paytm.utils.merchant.util.AuthUtil;
import com.paytm.utils.merchant.util.PayMethodType;
import io.qameta.allure.*;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.apache.commons.codec.binary.Base64;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import scripts.PostpaidSubscription.PostpaidRenewSubs;

import static com.paytm.appconstants.Constants.Owner.*;
import static com.paytm.apphelpers.LogsValidationHelper.getLogsOnServer;
import static org.hamcrest.Matchers.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.Random;

import static com.paytm.LocalConfig.PGP_HOST;
import static com.paytm.base.test.Group.Status;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;

@Owner("Tarun")
@Owners(author = "Tarun", qa = "Ankur")
public class NativeProcessSubscription extends PGPBaseTest {

    private final CheckoutPage checkoutPage = new CheckoutPage();
    private final static String CC_BIN = "471865";
    private final static String DC_BIN = "444433";



    @Deprecated
    protected static JsonPath validateFetchPaymentOptions(InitTxnDTO initTxnDTO, String trxToken, PayMethodType payMethodType) {
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(trxToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath paymentOptionJsonPath = fetchPaymentOption.execute().jsonPath();
        CommonHelpers.assertCheck(paymentOptionJsonPath, new Object[]{
                "body.resultInfo.resultStatus", "S",
                "body.resultInfo.resultCode", "0000",
                "body.resultInfo.resultMsg", "Success"
        });
        Assertions.assertThat(paymentOptionJsonPath.param("status", "false").getList(
                "body.merchantPayOption.paymentModes.findAll { paymentModes -> paymentModes.isDisabled.status == status }.paymentMode"))
                .contains(payMethodType.toString());
        return paymentOptionJsonPath;
    }

    @Deprecated
    protected static void validate_BinDetail(String txnToken, InitTxnDTO initTxnDTO, OrderDTO orderDTO, String binNum) {

        FetchBinDetailsRequest request = new FetchBinDetailsRequest.Builder(txnToken, binNum).build();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(request, initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody());
        JsonPath fetchBinsJson = fetchBinDetail.execute().jsonPath();
        CommonHelpers.assertCheck(fetchBinsJson, new Object[]{
                "body.resultInfo.resultStatus", "S",
                "body.resultInfo.resultCode", "0000",
                "body.resultInfo.resultMsg", "Success"
        });
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail")).isNotNull();
    }

    @Deprecated
    protected Object[] validateInitiateSubscriptionTrxToken(User user, String[] setInitSubscriptionParam) {
        InitTxnDTO initTxnDTO = null;
        if (setInitSubscriptionParam[0].contains("TC_SGC")) {
            initTxnDTO = new InitTxnDTO.Builder
                    (null, Constants.MerchantType.Subscription_PGOnly)
                    .setTxnValue(setInitSubscriptionParam[1])
                    .setSubscriptionPaymentMode(setInitSubscriptionParam[2])
                    .setSubscriptionAmountType(setInitSubscriptionParam[3])
                    .setSubscriptionMaxAmount(setInitSubscriptionParam[4])
                    .setSubscriptionFrequency(setInitSubscriptionParam[5])
                    .setSubscriptionFrequencyUnit(setInitSubscriptionParam[6])
                    .setSubscriptionGraceDays(setInitSubscriptionParam[7])
                    .setSubscriptionStartDate(setInitSubscriptionParam[8])
                    .setRequestType("NATIVE_SUBSCRIPTION")
                    .build();
        } else {
            initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.Subscription_PGOnly)
                    .setTxnValue(setInitSubscriptionParam[1])
                    .setSubscriptionPaymentMode(setInitSubscriptionParam[2])
                    .setSubscriptionAmountType(setInitSubscriptionParam[3])
                    .setSubscriptionMaxAmount(setInitSubscriptionParam[4])
                    .setSubscriptionFrequency(setInitSubscriptionParam[5])
                    .setSubscriptionFrequencyUnit(setInitSubscriptionParam[6])
                    .setSubscriptionGraceDays(setInitSubscriptionParam[7])
                    .setSubscriptionStartDate(setInitSubscriptionParam[8])
                    .setRequestType("NATIVE_SUBSCRIPTION")
                    .build();
        }
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath subscriptionJsonPath = initTxn.execute().jsonPath();
        CommonHelpers.assertCheck(subscriptionJsonPath, new Object[]{
                "body.resultInfo.resultStatus", "S",
                "body.resultInfo.resultCode", "0000",
                "body.resultInfo.resultMsg", "Success"
        });
        Assert.assertFalse(subscriptionJsonPath.getString("body.txnToken").isEmpty());
        Assert.assertFalse(subscriptionJsonPath.getString("body.subscriptionId").isEmpty());
        String trxToken = subscriptionJsonPath.getString("body.txnToken").toString();
        String subscriptionId = subscriptionJsonPath.getString("body.subscriptionId").toString();
        return new Object[]{trxToken, subscriptionId, initTxnDTO};
    }

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

    /**
     * Execute and validates isDisabled status of provided payment method in fetchPaymentOptions API response
     * @param txnToken
     * @param mid
     * @param orderId
     * @param payMethod
     * @param isDisabledStatus
     * @return
     */
    @Step("Validate fetchPaymentOption paymode status")
    private FetchPaymentOptResponseDTO execute_validateFetchPaymentOption(String txnToken, String mid, String orderId, String payMethod, boolean isDisabledStatus) {
        FetchPaymentOptResponseDTO fetchPaymentOptResponseDTO = NativeHelpers.
                fetchPaymentOptionResponse(txnToken, mid, orderId);
        Assertions.assertThat(NativeHelpers.isFetchPaymentOptionStatusMatched(fetchPaymentOptResponseDTO, payMethod, isDisabledStatus))
                .as(payMethod + " paymethod is disabled or not found")
                .isTrue();
        return fetchPaymentOptResponseDTO;
    }

    /**
     * Execute and validates isDisabled status of provided savedCardId in fetchPaymentOptions API response
     * @param txnToken
     * @param mid
     * @param orderId
     * @param cardId
     * @param isDisabledStatus
     * @return
     */
    @Step("Validate fetchPaymentOption savedCardId status")
    private FetchPaymentOptResponseDTO execute_validateSavedFetchPaymentOption(String txnToken, String mid, String orderId, String cardId, boolean isDisabledStatus) {
        FetchPaymentOptResponseDTO fetchPaymentOptResponseDTO = NativeHelpers.
                fetchPaymentOptionResponse(txnToken, mid, orderId);
        Assertions.assertThat(NativeHelpers.isSavedFetchPaymentOptionStatusMatched(fetchPaymentOptResponseDTO, cardId, isDisabledStatus))
                .as(cardId + " cardId is disabled or not found")
                .isTrue();
        return fetchPaymentOptResponseDTO;
    }

    /**
     * Execute and validates isDisabled status of addmoney provided payment method in fetchPaymentOptions API response
     * @param txnToken
     * @param mid
     * @param orderId
     * @param payMethod
     * @param isDisabledStatus
     * @return
     */
    @Step("Validate addmoney fetchPaymentOption paymode status")
    private FetchPaymentOptResponseDTO execute_validateAddmoneyFetchPaymentOption(String txnToken, String mid, String orderId, String payMethod, boolean isDisabledStatus) {
        FetchPaymentOptResponseDTO fetchPaymentOptResponseDTO = NativeHelpers.
                fetchPaymentOptionResponse(txnToken, mid, orderId);
        Assertions.assertThat(NativeHelpers.isAddMoneyFetchPaymentOptStatusMatched(fetchPaymentOptResponseDTO, payMethod, isDisabledStatus))
                .as(payMethod + " payMethod is disabled or not found")
                .isTrue();
        return fetchPaymentOptResponseDTO;
    }

    /**
     * Execute adn validates provided binNumber isActive status in fetchBinDetails API response
     * @param txnToken
     * @param mid
     * @param orderId
     * @param binNum
     * @param binStatus
     * @return
     */
    @Step("Validate successBinDetails for bin {3}")
    private FetchBinDetailResponse execute_validateActiveBinDetail(String txnToken, String mid, String orderId, String binNum, boolean binStatus) {
        FetchBinDetailResponse fetchBinDetailResponse = NativeHelpers.fetchBinDetailResponse(txnToken, mid, orderId, binNum);
        Assertions.assertThat(fetchBinDetailResponse.getBody().getResultInfo().getResultCode())
                .as("result code mismatch")
                .isEqualTo("0000");
        Assertions.assertThat(fetchBinDetailResponse.getBody().getResultInfo().getResultMsg())
                .as("resultMsg mismatch")
                .isEqualToIgnoringCase("Success");
        BinDetail binDetail = fetchBinDetailResponse.getBody().getBinDetail();
        if(binDetail.getBin().equalsIgnoreCase(binNum))
            Assertions.assertThat(binDetail.getIsActive())
                    .as("bin isActive status mismatch")
                    .isEqualToIgnoringCase(Boolean.toString(binStatus));
        else
            Assertions.fail(binNum + "bin number not found");
        return fetchBinDetailResponse;
    }

    //Deprecated CC/DC Flow after SI Hub Release
 //   @Test(enabled = false, description = "Validate TrxToken created for Subscription API when valid Information is passed")
    public void TC_NPS001_ValidateSubscriptionTrxToken() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.Subscription_PGOnly)     //Initiate subs request
                .setTxnValue("1")
                .setSubscriptionPaymentMode("CC")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("5")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        validateSuccessInitiateSubscription(initTxnDTO);
    }

    //Deprecated CC/DC Flow after SI Hub Release
 //   @Test(enabled = false, description = "Verify success case for Payment Options when valid txn_token is passed")
    public void TC_NPS002_ValidateSuccessPaymentOptions() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)       //Initiate subs request
                .setTxnValue("1")
                .setSubscriptionPaymentMode("CC")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("5")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        execute_validateFetchPaymentOption(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), PayMethodType.CREDIT_CARD.toString(), false);

    }
    //Deprecated CC/DC Flow after SI Hub Release
 //   @Parameters({"isNativePlus"})
 //   @Test(enabled = false, description = "Process transaction with valid CC, transaction token and SubscriptionId generated when " +
 //           "subscriptionAmountType=VARIABLE and subscriptionFrequencyUnit=MONTH and subscriptionGraceDays = 5")
    public void TC_NPS003_ProcessVariableSubscriptionCCMonth(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("CC")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("5")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);        //Initiate create subs first request
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        execute_validateFetchPaymentOption(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), PayMethodType.CREDIT_CARD.toString(), false);
        execute_validateActiveBinDetail(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), CC_BIN, true);
        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(merchant, initTxnDTO.getBody().getOrderId(),
                txnToken, PayMethodType.CREDIT_CARD, subsId)
                .setAUTH_MODE("3D")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);             // payment through created subscription request
        PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnType("SALE")
                .validateGatewayName("HDFC")
                .validateRespCode("01")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateSubsid(subsId)
                .AssertAll();

        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("subcription_start_date", subsId, orderDTO.getORDER_ID()))
                .as("subcription_start_date not available")
                .contains(CommonHelpers.getDate().toString());
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("acquirement_id", subsId, orderDTO.getORDER_ID()))
                .as("acquirement_id is null")
                .isNotNull();
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("status", subsId, orderDTO.getORDER_ID()))
                .as("status mismatch")
                .isEqualTo("ACTIVE");
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("saved_card_id", subsId, orderDTO.getORDER_ID()))
                .as("savedCardId is null")
                .isNotNull();
        NativeHelpers.assertRedisKeysNotPresent(txnToken);
    }
    //Deprecated CC/DC Flow after SI Hub Release
 //   @Parameters({"isNativePlus"})
 //   @Test(enabled = false, description = "Process transaction with valid DC, transaction token and SubscriptionId generated when " +
 //           "subscriptionAmountType=VARIABLE and subscriptionFrequencyUnit=MONTH and subscriptionGraceDays = 5")
    public void TC_NPS004_ProcessVariableSubscriptionDCMonth(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        SavedCardHelpers.deleteSavedCard(user);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("DC")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("5")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);            //Initiate create subs first request
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        execute_validateFetchPaymentOption(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), PayMethodType.DEBIT_CARD.toString(), false);
        execute_validateActiveBinDetail(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), DC_BIN, true);

        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(Constants.MerchantType.Subscription_PGOnly, initTxnDTO.getBody().getOrderId(), txnToken, PayMethodType.DEBIT_CARD, subsId)
                .setAUTH_MODE("otp")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);             //payment through created subscription ID
        PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnType("SALE")
                .validateGatewayName("HDFC")
                .validateRespCode("01")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateSubsid(subsId)
                .AssertAll();

        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("subcription_start_date", subsId, orderDTO.getORDER_ID()))
                .as("subcription_start_date mismatch")
                .contains(CommonHelpers.getDate().toString());
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("acquirement_id", subsId, orderDTO.getORDER_ID()))
                .as("acquirement_id is null")
                .isNotNull();
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("status", subsId, orderDTO.getORDER_ID()))
                .as("status mismatch")
                .isEqualTo("ACTIVE");
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("saved_card_id", subsId, orderDTO.getORDER_ID()))
                .as("saved_card_id is null")
                .isNotNull();
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Process transaction with valid PPI, transaction token and SubscriptionId generated when " +
            "subscriptionAmountType=VARIABLE and subscriptionFrequencyUnit=MONTH and subscriptionGraceDays = 5")
    public void TC_NPS005_ProcessVariableSubscriptionPPIMonth(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_ADDNPAY;
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("3")
                .setSubscriptionPaymentMode("PPI")
                .setSubsPPIOnly("Y")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("5")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);            //Initiate create subs first request
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()));
        execute_validateFetchPaymentOption(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), PayMethodType.BALANCE.toString(), false);
        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(Constants.MerchantType.SUBSCRIPTION_ADDNPAY, initTxnDTO.getBody().getOrderId(), txnToken, PayMethodType.BALANCE, subsId)
                .setAUTH_MODE("USRPWD")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);                 //payment through created subs ID
        PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnType("SALE")
                .validateGatewayName("WALLET")
                .validateRespCode("01")
                .validateBankName("WALLET")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .validateSubsid(subsId)
                .AssertAll();

        WalletHelpers.validateBalance(user, 0.0);
         Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("subcription_start_date", subsId, orderDTO.getORDER_ID()))
                .as("subcription_start_date mismatch")
                .contains(CommonHelpers.getDate().toString());
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("acquirement_id", subsId, orderDTO.getORDER_ID()))
                .as("acquirement_id is null")
                .isNotNull();
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("status", subsId, orderDTO.getORDER_ID()))
                .as("subs status mismatch")
                .isEqualTo("ACTIVE");
    }

    @Owner("Jai")
    @Parameters({"isNativePlus"})
    @Test(description = "Verify Card details are required For zero rs subscription payment, " +
            "when wallet amount is zero or less than 1 rs, Native PPIY Flow")
    public void VerifyZeroAmtPaymentWithWalletBalanceLessThanOneNativePPIY(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_WALLET_ONLY;
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        WalletHelpers.modifyBalance(user, 0.50);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("0")
                .setSubscriptionPaymentMode("PPI")
                .setSubsPPIOnly("Y")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("5")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);            //Initiate create subs first request
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(Constants.MerchantType.SUBSCRIPTION_WALLET_ONLY, initTxnDTO.getBody().getOrderId(), txnToken, PayMethodType.BALANCE, subsId)
                .setAUTH_MODE("USRPWD")
                .setTXN_AMOUNT("0")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        CashierPage cashierPage = CashierPageFactory.getCashierPage("enhancedweb");
        cashierPage.checkBoxPPI().assertDisabled();
        String payButtonText = cashierPage.buttonPGPayNow().getText();
        Assertions.assertThat(payButtonText).isEqualTo("ADD Rs "+"0.50"+" TO PAY");
        cashierPage.payBy(Constants.PayMode.DC);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount("1.00")
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("WALLET")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName("WALLET")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateSubsid(Constants.ValidationType.NON_EMPTY)
                .AssertAll();
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify Card details are required For zero rs subscription payment, " +
            "when wallet amount is zero or less than 1 rs, Native PPIN Flow")
    public void VerifyZeroAmtPaymentWithWalletBalanceLessThanOneNativePPIN(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PPI;
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        WalletHelpers.modifyBalance(user, 0.50);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("0")
                .setSubscriptionPaymentMode("PPI")
                .setSubsPPIOnly("N")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("5")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);            //Initiate create subs first request
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(Constants.MerchantType.SUBSCRIPTION_PPI, initTxnDTO.getBody().getOrderId(), txnToken, PayMethodType.BALANCE, subsId)
                .setAUTH_MODE("USRPWD")
                .setTXN_AMOUNT("0")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        CashierPage cashierPage = CashierPageFactory.getCashierPage("enhancedweb");
        cashierPage.checkBoxPPI().assertDisabled();
        String payButtonText = cashierPage.buttonPGPayNow().getText();
        Assertions.assertThat(payButtonText).isEqualTo("ADD Rs "+"0.50"+" TO PAY");
        cashierPage.payBy(Constants.PayMode.DC);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount("1.00")
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("WALLET")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName("WALLET")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateSubsid(Constants.ValidationType.NON_EMPTY)
                .AssertAll();
        NativeHelpers.assertRedisKeysNotPresent(txnToken);
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify Card details are required For zero rs subscription payment, " +
            "when wallet amount is zero or less than 1 rs, Native PPIY Flow with SubsOnWallet Preference Enabled on Merchant")
    public void VerifyZeroAmtPaymentWithWalletBalanceLessThanOneNativePPIYSubsOnWalletEnabled(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_WALLET_LIMIT;
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        WalletHelpers.modifyBalance(user, 0.50);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("" ,merchant)
                .setTxnValue("0")
                .setSubscriptionPaymentMode("PPI")
                .setSubsPPIOnly("Y")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("5")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);            //Initiate create subs first request
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        SendOTP sendotp = new SendOTP(txnToken,user.mobNo(),initTxnDTO.getBody().getMid(),initTxnDTO.getBody().getOrderId());
        Response SendOtpResponse = sendotp.execute();
        // Validating OTP by Mock now and not retrieving from logs any 6 digit otp will work
        //String otp = AuthUtil.getOtp(user.mobNo());
        String otp = "123456";
        ValidateOTP val = new ValidateOTP(txnToken,otp,initTxnDTO.getBody().getMid(),initTxnDTO.getBody().getOrderId());
        Response ValidateOTPResponse = val.execute();
        Assertions.assertThat(ValidateOTPResponse.jsonPath().getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("Success");
        Assertions.assertThat(ValidateOTPResponse.jsonPath().getInt("body.resultInfo.resultCode")).isEqualTo(01);
        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(Constants.MerchantType.SUBSCRIPTION_WALLET_LIMIT, initTxnDTO.getBody().getOrderId(), txnToken, PayMethodType.BALANCE, subsId)
                .setAUTH_MODE("USRPWD")
                .setTXN_AMOUNT("0")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        CashierPage cashierPage = CashierPageFactory.getCashierPage("enhancedweb");
        cashierPage.checkBoxPPI().assertDisabled();
        String payButtonText = cashierPage.buttonPGPayNow().getText();
        Assertions.assertThat(payButtonText).isEqualTo("ADD Rs "+"0.50"+" TO PAY");
        cashierPage.payBy(Constants.PayMode.DC);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount("1.00")
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("WALLET")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName("WALLET")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateSubsid(Constants.ValidationType.NON_EMPTY)
                .AssertAll();
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify Card details are required For zero rs subscription payment, " +
            "when wallet amount is zero or less than 1 rs, Native PPIN Flow with SubsOnWallet Preference Enabled on Merchant")
    public void VerifyZeroAmtPaymentWithWalletBalanceLessThanOneNativePPINSubsOnWalletEnabled(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_WALLET_LIMIT;
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        WalletHelpers.modifyBalance(user, 0.50);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("" ,merchant)
                .setTxnValue("0")
                .setSubscriptionPaymentMode("PPI")
                .setSubsPPIOnly("N")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("5")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);            //Initiate create subs first request
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        SendOTP sendotp = new SendOTP(txnToken,user.mobNo(),initTxnDTO.getBody().getMid(),initTxnDTO.getBody().getOrderId());
        Response SendOtpResponse = sendotp.execute();
        // Validating OTP by Mock now and not retrieving from logs any 6 digit otp will work
        //String otp = AuthUtil.getOtp(user.mobNo());
        String otp = "123456";
        ValidateOTP val = new ValidateOTP(txnToken,otp,initTxnDTO.getBody().getMid(),initTxnDTO.getBody().getOrderId());
        Response ValidateOTPResponse = val.execute();
        Assertions.assertThat(ValidateOTPResponse.jsonPath().getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("Success");
        Assertions.assertThat(ValidateOTPResponse.jsonPath().getInt("body.resultInfo.resultCode")).isEqualTo(01);
        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(Constants.MerchantType.SUBSCRIPTION_WALLET_LIMIT, initTxnDTO.getBody().getOrderId(), txnToken, PayMethodType.BALANCE, subsId)
                .setAUTH_MODE("USRPWD")
                .setTXN_AMOUNT("0")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        CashierPage cashierPage = CashierPageFactory.getCashierPage("enhancedweb");
        cashierPage.checkBoxPPI().assertDisabled();
        String payButtonText = cashierPage.buttonPGPayNow().getText();
        Assertions.assertThat(payButtonText).isEqualTo("ADD Rs "+"0.50"+" TO PAY");
        cashierPage.payBy(Constants.PayMode.DC);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount("1.00")
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("WALLET")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName("WALLET")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateSubsid(Constants.ValidationType.NON_EMPTY)
                .AssertAll();
    }

    //Deprecated CC/DC Flow after SI Hub Release
 //   @Parameters({"isNativePlus"})
 //   @Test(enabled = false, description = "Process transaction with valid CC, transaction token and SubscriptionId generated when " +
 //           "subscriptionAmountType=VARIABLE and subscriptionFrequencyUnit=DAY and subscriptionGraceDays = 0")
    public void TC_NPS006_ProcessVariableSubscriptionCCDay(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("CC")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("DAY")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        execute_validateFetchPaymentOption(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), PayMethodType.CREDIT_CARD.toString(), false);
        execute_validateActiveBinDetail(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), CC_BIN, true);

        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(Constants.MerchantType.Subscription_PGOnly, initTxnDTO.getBody().getOrderId(), txnToken, PayMethodType.CREDIT_CARD, subsId)
                .setAUTH_MODE("3D")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnType("SALE")
                .validateGatewayName("HDFC")
                .validateRespCode("01")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateSubsid(subsId)
                .AssertAll();

        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("subcription_start_date", subsId, orderDTO.getORDER_ID()))
                .as("subcription_start_date mismatch")
                .contains(CommonHelpers.getDate().toString());
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("acquirement_id", subsId, orderDTO.getORDER_ID()))
                .as("acquirement_id is null")
                .isNotNull();
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("status", subsId, orderDTO.getORDER_ID()))
                .as("subs status mismatch")
                .isEqualTo("ACTIVE");
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("saved_card_id", subsId, orderDTO.getORDER_ID()))
                .as("saved_card_id is null")
                .isNotNull();
    }
    //Deprecated CC/DC Flow after SI Hub Release
 //   @Parameters({"isNativePlus"})
 //   @Test(enabled = false, description = "Process transaction with valid DC, transaction token and SubscriptionId generated when " +
 //           "subscriptionAmountType=VARIABLE and subscriptionFrequencyUnit=DAY and subscriptionGraceDays = 0")
    public void TC_NPS006_ProcessVariableSubscriptionDCDay(@Optional("false") Boolean isNativePlus) throws Exception{
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("DC")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("DAY")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        execute_validateFetchPaymentOption(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), PayMethodType.DEBIT_CARD.toString(), false);
        execute_validateActiveBinDetail(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), DC_BIN, true);

        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(Constants.MerchantType.Subscription_PGOnly, initTxnDTO.getBody().getOrderId(), txnToken, PayMethodType.DEBIT_CARD, subsId)
                .setAUTH_MODE("otp")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnType("SALE")
                .validateGatewayName("HDFC")
                .validateRespCode("01")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateSubsid(subsId)
                .AssertAll();

        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("subcription_start_date", subsId, orderDTO.getORDER_ID()))
                .as("subcription_start_date mismatch")
                .contains(CommonHelpers.getDate().toString());
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("acquirement_id", subsId, orderDTO.getORDER_ID()))
                .as("acquirement_id is null")
                .isNotNull();
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("status", subsId, orderDTO.getORDER_ID()))
                .as("subs status mismatch")
                .isEqualTo("ACTIVE");
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("saved_card_id", subsId, orderDTO.getORDER_ID()))
                .as("saved_card_id is null")
                .isNotNull();
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Process transaction with valid PPI, transaction token and SubscriptionId generated when " +
            "subscriptionAmountType=VARIABLE and subscriptionFrequencyUnit=DAY and subscriptionGraceDays = 0")
    public void TC_NPS007_ProcessVariableSubscriptionPPIDay(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.modifyBalance(user, 0.00);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_ADDNPAY;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("PPI")
                .setSubsPPIOnly("Y")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("DAY")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()));
        execute_validateFetchPaymentOption(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), PayMethodType.BALANCE.toString(), false);

        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(Constants.MerchantType.SUBSCRIPTION_ADDNPAY, initTxnDTO.getBody().getOrderId(), txnToken, PayMethodType.BALANCE, subsId)
                .setAUTH_MODE("USRPWD")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnType("SALE")
                .validateGatewayName("WALLET")
                .validateRespCode("01")
                .validateBankName("WALLET")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .validateSubsid(subsId)
                .AssertAll();
        WalletHelpers.validateBalance(user, 0.0);
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("subcription_start_date", subsId, orderDTO.getORDER_ID()))
                .as("subcription_start_date mismatch")
                .contains(CommonHelpers.getDate().toString());
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("acquirement_id", subsId, orderDTO.getORDER_ID()))
                .as("acquirement_id null")
                .isNotNull();
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("status", subsId, orderDTO.getORDER_ID()))
                .as("subs status mismatch")
                .isEqualTo("ACTIVE");
    }
    //Deprecated CC/DC Flow after SI Hub Release
 //   @Parameters({"isNativePlus"})
 //   @Test(enabled = false, description = "Process transaction with valid CC, transaction token and SubscriptionId generated when " +
 //           "subscriptionAmountType=VARIABLE and subscriptionFrequencyUnit=YEAR and subscriptionGraceDays = 15")
    public void TC_NPS008_ProcessVariableSubscriptionCCYear(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("CC")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("YEAR")
                .setSubscriptionGraceDays("15")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        execute_validateFetchPaymentOption(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), PayMethodType.CREDIT_CARD.toString(), false);
        execute_validateActiveBinDetail(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), CC_BIN, true);

        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(Constants.MerchantType.Subscription_PGOnly, initTxnDTO.getBody().getOrderId(), txnToken, PayMethodType.CREDIT_CARD, subsId)
                .setAUTH_MODE("3D")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnType("SALE")
                .validateGatewayName("HDFC")
                .validateRespCode("01")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateSubsid(subsId)
                .AssertAll();
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("subcription_start_date", subsId, orderDTO.getORDER_ID()))
                .as("subcription_start_date mismatch")
                .contains(CommonHelpers.getDate().toString());
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("acquirement_id", subsId, orderDTO.getORDER_ID()))
                .as("acquirement_id is null")
                .isNotNull();
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("status", subsId, orderDTO.getORDER_ID()))
                .as("subs status mismatch")
                .isEqualTo("ACTIVE");
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("saved_card_id", subsId, orderDTO.getORDER_ID()))
                .as("saved_card_id is null")
                .isNotNull();
    }

    //Deprecated CC/DC Flow after SI Hub Release
//    @Parameters({"isNativePlus"})
//    @Test(enabled = false, description = "Process transaction with valid DC, transaction token and SubscriptionId generated when " +
//            "subscriptionAmountType=VARIABLE and subscriptionFrequencyUnit=YEAR and subscriptionGraceDays = 15")
    public void TC_NPS008_ProcessVariableSubscriptionDCYear(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("DC")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("YEAR")
                .setSubscriptionGraceDays("15")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        execute_validateFetchPaymentOption(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), PayMethodType.DEBIT_CARD.toString(), false);
        execute_validateActiveBinDetail(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), DC_BIN, true);

        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(Constants.MerchantType.Subscription_PGOnly, initTxnDTO.getBody().getOrderId(), txnToken, PayMethodType.DEBIT_CARD, subsId)
                .setAUTH_MODE("otp")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnType("SALE")
                .validateGatewayName("HDFC")
                .validateRespCode("01")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateSubsid(subsId)
                .AssertAll();
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("subcription_start_date", subsId, orderDTO.getORDER_ID()))
                .as("subcription_start_date mismatch")
                .contains(CommonHelpers.getDate().toString());
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("acquirement_id", subsId, orderDTO.getORDER_ID()))
                .as("acquirement_id is null")
                .isNotNull();
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("status", subsId, orderDTO.getORDER_ID()))
                .as("subs status mismatch")
                .isEqualTo("ACTIVE");
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("saved_card_id", subsId, orderDTO.getORDER_ID()))
                .as("saved_card_id is null")
                .isNotNull();
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Process transaction with valid PPI, transaction token and SubscriptionId generated when " +
            "subscriptionAmountType=VARIABLE and subscriptionFrequencyUnit=YEAR and subscriptionGraceDays = 15")
    public void TC_NPS008_ProcessVariableSubscriptionPPIYear(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_ADDNPAY;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("3")
                .setSubscriptionPaymentMode("PPI")
                .setSubsPPIOnly("Y")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("YEAR")
                .setSubscriptionGraceDays("5")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        execute_validateFetchPaymentOption(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), PayMethodType.BALANCE.toString(), false);
        WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()));

        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(Constants.MerchantType.SUBSCRIPTION_ADDNPAY, initTxnDTO.getBody().getOrderId(), txnToken, PayMethodType.BALANCE, subsId)
                .setAUTH_MODE("USRPWD")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnType("SALE")
                .validateGatewayName("WALLET")
                .validateRespCode("01")
                .validateBankName("WALLET")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .validateSubsid(subsId)
                .AssertAll();
        WalletHelpers.validateBalance(user , 0.0);
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("subcription_start_date", subsId, orderDTO.getORDER_ID()))
                .as("subcription_start_date mismatch")
                .contains(CommonHelpers.getDate().toString());
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("acquirement_id", subsId, orderDTO.getORDER_ID()))
                .as("acquirement_id is null")
                .isNotNull();
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("status", subsId, orderDTO.getORDER_ID()))
                .as("subs status mismatch")
                .isEqualTo("ACTIVE");
    }
    //Deprecated CC/DC Flow after SI Hub Release
//    @Parameters({"isNativePlus"})
//    @Test(enabled = false, description = "Process transaction with valid CC, transaction token and SubscriptionId generated when " +
//            "subscriptionAmountType=FIX and subscriptionFrequencyUnit=MONTH and subscriptionGraceDays = 2")
    public void TC_NPS009_ProcessFixSubscriptionCCMonth(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("CC")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("2")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        execute_validateFetchPaymentOption(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), PayMethodType.CREDIT_CARD.toString(), false);
        execute_validateActiveBinDetail(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), CC_BIN, true);

        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(Constants.MerchantType.Subscription_PGOnly, initTxnDTO.getBody().getOrderId(), txnToken, PayMethodType.CREDIT_CARD, subsId)
                .setAUTH_MODE("3D")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnType("SALE")
                .validateGatewayName("HDFC")
                .validateRespCode("01")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateSubsid(subsId)
                .AssertAll();
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("subcription_start_date", subsId, orderDTO.getORDER_ID()))
                .as("subcription_start_date mismatch")
                .contains(CommonHelpers.getDate().toString());
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("acquirement_id", subsId, orderDTO.getORDER_ID()))
                .as("acquirement_id is null")
                .isNotNull();
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("status", subsId, orderDTO.getORDER_ID()))
                .as("subs status mismatch")
                .isEqualTo("ACTIVE");
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("saved_card_id", subsId, orderDTO.getORDER_ID()))
                .as("saved_card_id mismatch")
                .isNotNull();
    }
    //Deprecated CC/DC Flow after SI Hub Release
//    @Parameters({"isNativePlus"})
//    @Test(enabled = false, description = "Process transaction with valid DC, transaction token and SubscriptionId generated when " +
//            "subscriptionAmountType=FIX and subscriptionFrequencyUnit=MONTH and subscriptionGraceDays = 2")
    public void TC_NPS009_ProcessFixSubscriptionDCMonth(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("DC")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("2")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        execute_validateFetchPaymentOption(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), PayMethodType.DEBIT_CARD.toString(), false);
        execute_validateActiveBinDetail(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), DC_BIN, true);

        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(Constants.MerchantType.Subscription_PGOnly, initTxnDTO.getBody().getOrderId(), txnToken, PayMethodType.DEBIT_CARD, subsId)
                .setAUTH_MODE("otp")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnType("SALE")
                .validateGatewayName("HDFC")
                .validateRespCode("01")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateSubsid(subsId)
                .AssertAll();
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("subcription_start_date", subsId, orderDTO.getORDER_ID()))
                .as("subcription_start_date mismatch")
                .contains(CommonHelpers.getDate().toString());
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("acquirement_id", subsId, orderDTO.getORDER_ID()))
                .as("acquirement_id is null")
                .isNotNull();
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("status", subsId, orderDTO.getORDER_ID()))
                .as("subs status mismatch")
                .isEqualTo("ACTIVE");
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("saved_card_id", subsId, orderDTO.getORDER_ID()))
                .as("saved_card_id mismatch")
                .isNotNull();
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Process transaction with valid PPI, transaction token and SubscriptionId generated when " +
            "subscriptionAmountType=FIX and subscriptionFrequencyUnit=MONTH and subscriptionGraceDays = 2")
    public void TC_NPS009_ProcessFixSubscriptionPPIMonth(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_ADDNPAY;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("PPI")
                .setSubsPPIOnly("Y")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("2")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        execute_validateFetchPaymentOption(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), PayMethodType.BALANCE.toString(), false);
        WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()));

        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(Constants.MerchantType.SUBSCRIPTION_ADDNPAY, initTxnDTO.getBody().getOrderId(), txnToken, PayMethodType.BALANCE, subsId)
                .setAUTH_MODE("USRPWD")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnType("SALE")
                .validateGatewayName("WALLET")
                .validateRespCode("01")
                .validateBankName("WALLET")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .validateSubsid(subsId)
                .AssertAll();
        WalletHelpers.validateBalance(user, 0.0);
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("subcription_start_date", subsId, orderDTO.getORDER_ID()))
                .as("subcription_start_date mismatch")
                .contains(CommonHelpers.getDate().toString());
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("acquirement_id", subsId, orderDTO.getORDER_ID()))
                .as("acquirement_id is null")
                .isNotNull();
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("status", subsId, orderDTO.getORDER_ID()))
                .as("subs status mismatch")
                .isEqualTo("ACTIVE");
    }

    //Deprecated CC/DC Flow after SI Hub Release
//    @Parameters({"isNativePlus"})
//    @Test(enabled = false, description = "Process transaction with valid CC, transaction token and SubscriptionId generated when " +
//            "subscriptionAmountType=FIX and subscriptionFrequencyUnit=DAY and subscriptionGraceDays = 1")
    public void TC_NPS009_ProcessFixSubscriptionCCDay(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("CC")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("2")
                .setSubscriptionFrequencyUnit("DAY")
                .setSubscriptionGraceDays("1")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        execute_validateFetchPaymentOption(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), PayMethodType.CREDIT_CARD.toString(), false);
        execute_validateActiveBinDetail(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), CC_BIN, true);

        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(Constants.MerchantType.Subscription_PGOnly, initTxnDTO.getBody().getOrderId(), txnToken, PayMethodType.CREDIT_CARD, subsId)
                .setAUTH_MODE("3D")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnType("SALE")
                .validateGatewayName("HDFC")
                .validateRespCode("01")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateSubsid(subsId)
                .AssertAll();
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("subcription_start_date", subsId, orderDTO.getORDER_ID()))
                .as("subcription_start_date mismatch")
                .contains(CommonHelpers.getDate().toString());
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("acquirement_id", subsId, orderDTO.getORDER_ID()))
                .as("acquirement_id is null")
                .isNotNull();
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("status", subsId, orderDTO.getORDER_ID()))
                .as("subs status mismatch")
                .isEqualTo("ACTIVE");
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("saved_card_id", subsId, orderDTO.getORDER_ID()))
                .as("saved_card_id mismatch")
                .isNotNull();
    }
    //Deprecated CC/DC Flow after SI Hub Release
//    @Parameters({"isNativePlus"})
//    @Test(enabled = false, description = "Process transaction with valid DC, transaction token and SubscriptionId generated when " +
//            "subscriptionAmountType=FIX and subscriptionFrequencyUnit=DAY and subscriptionGraceDays = 1")
    public void TC_NPS009_ProcessFixSubscriptionDCDay(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("DC")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("2")
                .setSubscriptionFrequencyUnit("DAY")
                .setSubscriptionGraceDays("1")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        execute_validateFetchPaymentOption(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), PayMethodType.DEBIT_CARD.toString(), false);
        execute_validateActiveBinDetail(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), DC_BIN, true);

        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(Constants.MerchantType.Subscription_PGOnly, initTxnDTO.getBody().getOrderId(), txnToken, PayMethodType.DEBIT_CARD, subsId)
                .setAUTH_MODE("otp")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnType("SALE")
                .validateGatewayName("HDFC")
                .validateRespCode("01")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateSubsid(subsId)
                .AssertAll();
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("subcription_start_date", subsId, orderDTO.getORDER_ID()))
                .as("subcription_start_date mismatch")
                .contains(CommonHelpers.getDate().toString());
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("acquirement_id", subsId, orderDTO.getORDER_ID()))
                .as("acquirement_id is null")
                .isNotNull();
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("status", subsId, orderDTO.getORDER_ID()))
                .as("subs status mismatch")
                .isEqualTo("ACTIVE");
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("saved_card_id", subsId, orderDTO.getORDER_ID()))
                .as("saved_card_id mismatch")
                .isNotNull();
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Process transaction with valid PPI, transaction token and SubscriptionId generated when " +
            "subscriptionAmountType=FIX and subscriptionFrequencyUnit=DAY and subscriptionGraceDays = 1")
    public void TC_NPS009_ProcessFixSubscriptionPPIDay(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PPI;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("PPI")
                .setSubsPPIOnly("Y")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("2")
                .setSubscriptionFrequencyUnit("DAY")
                .setSubscriptionGraceDays("1")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        execute_validateFetchPaymentOption(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), PayMethodType.BALANCE.toString(), false);
        WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()));

        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(Constants.MerchantType.SUBSCRIPTION_PPI, initTxnDTO.getBody().getOrderId(), txnToken, PayMethodType.BALANCE, subsId)
                .setAUTH_MODE("USRPWD")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnType("SALE")
                .validateGatewayName("WALLET")
                .validateRespCode("01")
                .validateBankName("WALLET")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .validateSubsid(subsId)
                .AssertAll();
        WalletHelpers.validateBalance(user, 0.0);
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("subcription_start_date", subsId, orderDTO.getORDER_ID()))
                .as("subcription_start_date mismatch")
                .contains(CommonHelpers.getDate().toString());
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("acquirement_id", subsId, orderDTO.getORDER_ID()))
                .as("acquirement_id is null")
                .isNotNull();
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("status", subsId, orderDTO.getORDER_ID()))
                .as("subs status mismatch")
                .isEqualTo("ACTIVE");
    }
    //Deprecated CC/DC Flow after SI Hub Release
//    @Parameters({"isNativePlus"})
//    @Test(enabled = false, description = "Process transaction with valid CC, transaction token and SubscriptionId generated when " +
//            "subscriptionAmountType=FIX and subscriptionFrequencyUnit=YEAR and subscriptionGraceDays = 1")
    public void TC_NPS009_ProcessFixSubscriptionCCYear(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("CC")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("2")
                .setSubscriptionFrequencyUnit("YEAR")
                .setSubscriptionGraceDays("1")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        execute_validateFetchPaymentOption(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), PayMethodType.CREDIT_CARD.toString(), false);
        execute_validateActiveBinDetail(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), CC_BIN, true);

        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(Constants.MerchantType.Subscription_PGOnly, initTxnDTO.getBody().getOrderId(), txnToken, PayMethodType.CREDIT_CARD, subsId)
                .setAUTH_MODE("3D")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnType("SALE")
                .validateGatewayName("HDFC")
                .validateRespCode("01")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateSubsid(subsId)
                .AssertAll();
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("subcription_start_date", subsId, orderDTO.getORDER_ID()))
                .as("subcription_start_date mismatch")
                .contains(CommonHelpers.getDate().toString());
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("acquirement_id", subsId, orderDTO.getORDER_ID()))
                .as("acquirement_id is null")
                .isNotNull();
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("status", subsId, orderDTO.getORDER_ID()))
                .as("subs status mismatch")
                .isEqualTo("ACTIVE");
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("saved_card_id", subsId, orderDTO.getORDER_ID()))
                .as("saved_card_id mismatch")
                .isNotNull();
    }
    //Deprecated CC/DC Flow after SI Hub Release
//    @Parameters({"isNativePlus"})
//    @Test(enabled = false, description = "Process transaction with valid DC, transaction token and SubscriptionId generated when " +
//            "subscriptionAmountType=FIX and subscriptionFrequencyUnit=YEAR and subscriptionGraceDays = 1")
    public void TC_NPS009_ProcessFixSubscriptionDCYear(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("DC")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("2")
                .setSubscriptionFrequencyUnit("YEAR")
                .setSubscriptionGraceDays("1")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        execute_validateFetchPaymentOption(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), PayMethodType.DEBIT_CARD.toString(), false);
        execute_validateActiveBinDetail(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), DC_BIN, true);

        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(Constants.MerchantType.Subscription_PGOnly, initTxnDTO.getBody().getOrderId(), txnToken, PayMethodType.DEBIT_CARD, subsId)
                .setAUTH_MODE("otp")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnType("SALE")
                .validateGatewayName("HDFC")
                .validateRespCode("01")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateSubsid(subsId)
                .AssertAll();
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("subcription_start_date", subsId, orderDTO.getORDER_ID()))
                .as("subcription_start_date mismatch")
                .contains(CommonHelpers.getDate().toString());
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("acquirement_id", subsId, orderDTO.getORDER_ID()))
                .as("acquirement_id is null")
                .isNotNull();
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("status", subsId, orderDTO.getORDER_ID()))
                .as("subs status mismatch")
                .isEqualTo("ACTIVE");
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("saved_card_id", subsId, orderDTO.getORDER_ID()))
                .as("saved_card_id mismatch")
                .isNotNull();
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Process transaction with valid PPI, transaction token and SubscriptionId generated when " +
            "subscriptionAmountType=FIX and subscriptionFrequencyUnit=YEAR and subscriptionGraceDays = 1")
    public void TC_NPS009_ProcessFixSubscriptionPPIYear(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_ADDNPAY;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("PPI")
                .setSubsPPIOnly("Y")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("2")
                .setSubscriptionFrequencyUnit("YEAR")
                .setSubscriptionGraceDays("1")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        execute_validateFetchPaymentOption(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), PayMethodType.BALANCE.toString(), false);
        WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()));

        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(Constants.MerchantType.SUBSCRIPTION_ADDNPAY, initTxnDTO.getBody().getOrderId(), txnToken, PayMethodType.BALANCE, subsId)
                .setAUTH_MODE("USRPWD")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnType("SALE")
                .validateGatewayName("WALLET")
                .validateRespCode("01")
                .validateBankName("WALLET")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .validateSubsid(subsId)
                .AssertAll();
        WalletHelpers.validateBalance(user, 0.0);
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("subcription_start_date", subsId, orderDTO.getORDER_ID()))
                .as("subcription_start_date mismatch")
                .contains(CommonHelpers.getDate().toString());
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("acquirement_id", subsId, orderDTO.getORDER_ID()))
                .as("acquirement_id is null")
                .isNotNull();
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("status", subsId, orderDTO.getORDER_ID()))
                .as("subs status mismatch")
                .isEqualTo("ACTIVE");
    }
    //Deprecated CC/DC Flow after SI Hub Release
//    @Parameters({"isNativePlus"})
//    @Test(enabled = false, description = "Process transaction with valid CC, transaction token and SubscriptionId generated when " +
//            " subscriptionFrequency is passed 1 and SubscriptionStartDate is passed blank")
    public void TC_NPS010_ProcessSubscriptionWithoutSubscriptionStartDateCC(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        String SubscriptionStartDate = "";
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("5")
                .setSubscriptionPaymentMode("CC")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("YEAR")
                .setSubscriptionGraceDays(null)
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        execute_validateFetchPaymentOption(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), PayMethodType.CREDIT_CARD.toString(), false);
        execute_validateActiveBinDetail(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), CC_BIN, true);

        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(Constants.MerchantType.Subscription_PGOnly, initTxnDTO.getBody().getOrderId(),txnToken, PayMethodType.CREDIT_CARD, subsId)
                .setAUTH_MODE("3D")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnType("SALE")
                .validateGatewayName("HDFC")
                .validateRespCode("01")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateSubsid(subsId)
                .AssertAll();
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("subcription_start_date", subsId, orderDTO.getORDER_ID()))
                .as("subcription_start_date mismatch")
                .contains(CommonHelpers.getDate().plusYears(1L).toString());
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("acquirement_id", subsId, orderDTO.getORDER_ID()))
                .as("acquirement_id is null")
                .isNotNull();
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("status", subsId, orderDTO.getORDER_ID()))
                .as("subs status mismatch")
                .isEqualTo("ACTIVE");
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("saved_card_id", subsId, orderDTO.getORDER_ID()))
                .as("saved_card_id mismatch")
                .isNotNull();
    }
    //Deprecated CC/DC Flow after SI Hub Release
//    @Parameters({"isNativePlus"})
//    @Test(enabled = false, description = "Process transaction with valid DC, transaction token and SubscriptionId generated when " +
//            " subscriptionFrequency is passed 1 and SubscriptionStartDate is passed blank")
    public void TC_NPS010_ProcessSubscriptionWithoutSubscriptionStartDateDC(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        String SubscriptionStartDate = "";
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("5")
                .setSubscriptionPaymentMode("DC")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("YEAR")
                .setSubscriptionGraceDays(null)
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        execute_validateFetchPaymentOption(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), PayMethodType.DEBIT_CARD.toString(), false);
        execute_validateActiveBinDetail(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), DC_BIN, true);

        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(Constants.MerchantType.Subscription_PGOnly, initTxnDTO.getBody().getOrderId(),txnToken, PayMethodType.DEBIT_CARD, subsId)
                .setAUTH_MODE("otp")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnType("SALE")
                .validateGatewayName("HDFC")
                .validateRespCode("01")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateSubsid(subsId)
                .AssertAll();
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("subcription_start_date", subsId, orderDTO.getORDER_ID()))
                .as("subcription_start_date mismatch")
                .contains(CommonHelpers.getDate().plusYears(1L).toString());
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("acquirement_id", subsId, orderDTO.getORDER_ID()))
                .as("acquirement_id is null")
                .isNotNull();
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("status", subsId, orderDTO.getORDER_ID()))
                .as("subs status mismatch")
                .isEqualTo("ACTIVE");
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("saved_card_id", subsId, orderDTO.getORDER_ID()))
                .as("saved_card_id mismatch")
                .isNotNull();
    }

    //As disccused with Akshat Sharma test case is deprecated, refer JIRA: https://jira.mypaytm.com/browse/PGP-18868
/*    @Deprecated
    @Issue("PGP-14918")
    @Parameters({"isNativePlus"})
    @Test(description = "Process transaction with valid PPI, transaction token and SubscriptionId generated when " +
            " subscriptionFrequency is passed 0 and SubscriptionStartDate is passed blank", groups = Status.BUG, enabled = false) */
    public void TC_NPS010_ProcessSubscriptionWithoutSubscriptionStartDatePPI(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String SubscriptionStartDate = "";
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("5")
                .setSubscriptionPaymentMode("PPI")
                .setSubsPPIOnly("Y")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("0")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays(null)
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        execute_validateFetchPaymentOption(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), PayMethodType.BALANCE.toString(), false);
        WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()));

        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(Constants.MerchantType.Subscription_PGOnly, initTxnDTO.getBody().getOrderId(), txnToken, PayMethodType.BALANCE, subsId)
                .setAUTH_MODE("USRPWD")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnType("SALE")
                .validateGatewayName("WALLET")
                .validateRespCode("01")
                .validateBankName("WALLET")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .validateSubsid(subsId)
                .AssertAll();
        WalletHelpers.validateBalance(user, 0.0);
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("subcription_start_date", subsId, orderDTO.getORDER_ID()))
                .as("subcription_start_date mismatch")
                .contains(CommonHelpers.getDate().toString());
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("acquirement_id", subsId, orderDTO.getORDER_ID()))
                .as("acquirement_id is null")
                .isNotNull();
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("status", subsId, orderDTO.getORDER_ID()))
                .as("subs status mismatch")
                .isEqualTo("ACTIVE");
    }
    //Deprecated CC/DC Flow after SI Hub Release
  //  @Parameters({"isNativePlus"})
  //  @Test(enabled = false, description = "Process transaction with Saved Card CC, transaction token and SubscriptionId generated when " +
  //          "subscriptionAmountType=FIX and subscriptionFrequencyUnit=MONTH and subscriptionGraceDays = 2")
    public void TC_NPS013_ProcessFixSubscriptionUsingSavedCardCC(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        String savedCardId = SavedCardHelpers.getSavedCardId(user, 0);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("CC")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("2")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        execute_validateSavedFetchPaymentOption(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), savedCardId, false);
        execute_validateActiveBinDetail(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), CC_BIN, true);

        paymentDTO.setSavedCardId(savedCardId);
        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(Constants.MerchantType.Subscription_PGOnly, initTxnDTO.getBody().getOrderId(), txnToken, PayMethodType.CREDIT_CARD, subsId, paymentDTO)
                .setAUTH_MODE("3D")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnType("SALE")
                .validateGatewayName("HDFC")
                .validateRespCode("01")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateSubsid(subsId)
                .AssertAll();
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("subcription_start_date", subsId, orderDTO.getORDER_ID()))
                .as("subcription_start_date mismatch")
                .contains(CommonHelpers.getDate().toString());
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("acquirement_id", subsId, orderDTO.getORDER_ID()))
                .as("acquirement_id is null")
                .isNotNull();
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("status", subsId, orderDTO.getORDER_ID()))
                .as("subs status mismatch")
                .isEqualTo("ACTIVE");
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("saved_card_id", subsId, orderDTO.getORDER_ID()))
                .as("saved_card_id mismatch")
                .isNotNull();
    }
    //Deprecated CC/DC Flow after SI Hub Release
 //   @Parameters({"isNativePlus"})
 //   @Test(enabled = false, description = "Process transaction with Saved Card DC, transaction token and SubscriptionId generated when " +
 //           "subscriptionAmountType=FIX and subscriptionFrequencyUnit=MONTH and subscriptionGraceDays = 2")
    public void TC_NPS013_ProcessFixSubscriptionUsingSavedCardDC(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getDebitCardNumber());
        String savedCardId = SavedCardHelpers.getSavedCardId(user, 0);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("DC")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("2")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        execute_validateSavedFetchPaymentOption(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), savedCardId, false);
        execute_validateActiveBinDetail(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), DC_BIN, true);

        paymentDTO.setSavedCardId(savedCardId);
        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(Constants.MerchantType.Subscription_PGOnly, initTxnDTO.getBody().getOrderId(), txnToken, PayMethodType.DEBIT_CARD, subsId, paymentDTO)
                .setAUTH_MODE("otp")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnType("SALE")
                .validateGatewayName("HDFC")
                .validateRespCode("01")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateSubsid(subsId)
                .AssertAll();
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("subcription_start_date", subsId, orderDTO.getORDER_ID()))
                .as("subcription_start_date mismatch")
                .contains(CommonHelpers.getDate().toString());
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("acquirement_id", subsId, orderDTO.getORDER_ID()))
                .as("acquirement_id is null")
                .isNotNull();
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("status", subsId, orderDTO.getORDER_ID()))
                .as("subs status mismatch")
                .isEqualTo("ACTIVE");
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("saved_card_id", subsId, orderDTO.getORDER_ID()))
                .as("saved_card_id mismatch")
                .isNotNull();
    }
    //Deprecated CC/DC Flow after SI Hub Release
//    @Parameters({"isNativePlus"})
//    @Test(enabled = false, description = "Process transaction with Saved Card CC, transaction token and SubscriptionId generated when " +
//            "subscriptionAmountType=VARIABLE and subscriptionFrequencyUnit=YEAR and subscriptionGraceDays = 3")
    public void TC_NPS014_ProcessvariableSubscriptionUsingSavedCardCC(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        String savedCardId = SavedCardHelpers.getSavedCardId(user, 0);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("CC")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("YEAR")
                .setSubscriptionGraceDays("3")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        execute_validateSavedFetchPaymentOption(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), savedCardId, false);
        execute_validateActiveBinDetail(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), CC_BIN, true);

        paymentDTO.setSavedCardId(savedCardId);
        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(Constants.MerchantType.Subscription_PGOnly, initTxnDTO.getBody().getOrderId(), txnToken, PayMethodType.CREDIT_CARD, subsId, paymentDTO)
                .setAUTH_MODE("3D")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnType("SALE")
                .validateGatewayName("HDFC")
                .validateRespCode("01")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateSubsid(subsId)
                .AssertAll();
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("subcription_start_date", subsId, orderDTO.getORDER_ID()))
                .as("subcription_start_date mismatch")
                .contains(CommonHelpers.getDate().toString());
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("acquirement_id", subsId, orderDTO.getORDER_ID()))
                .as("acquirement_id is null")
                .isNotNull();
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("status", subsId, orderDTO.getORDER_ID()))
                .as("subs status mismatch")
                .isEqualTo("ACTIVE");
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("saved_card_id", subsId, orderDTO.getORDER_ID()))
                .as("saved_card_id mismatch")
                .isNotNull();
    }
    //Deprecated CC/DC Flow after SI Hub Release
//    @Parameters({"isNativePlus"})
//    @Test(enabled = false, description = "Process transaction with Saved Card DC, transaction token and SubscriptionId generated when " +
//            "subscriptionAmountType=VARIABLE and subscriptionFrequencyUnit=YEAR and subscriptionGraceDays = 3")
    public void TC_NPS014_ProcessvariableSubscriptionUsingSavedCardDC(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getDebitCardNumber());
        String savedCardId = SavedCardHelpers.getSavedCardId(user, 0);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("DC")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("YEAR")
                .setSubscriptionGraceDays("3")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        execute_validateSavedFetchPaymentOption(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), savedCardId, false);
        execute_validateActiveBinDetail(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), DC_BIN, true);

        paymentDTO.setSavedCardId(savedCardId);
        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(Constants.MerchantType.Subscription_PGOnly, initTxnDTO.getBody().getOrderId(), txnToken, PayMethodType.DEBIT_CARD, subsId, paymentDTO)
                .setAUTH_MODE("otp")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnType("SALE")
                .validateGatewayName("HDFC")
                .validateRespCode("01")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateSubsid(subsId)
                .AssertAll();
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("subcription_start_date", subsId, orderDTO.getORDER_ID()))
                .as("subcription_start_date mismatch")
                .contains(CommonHelpers.getDate().toString());
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("acquirement_id", subsId, orderDTO.getORDER_ID()))
                .as("acquirement_id is null")
                .isNotNull();
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("status", subsId, orderDTO.getORDER_ID()))
                .as("subs status mismatch")
                .isEqualTo("ACTIVE");
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("saved_card_id", subsId, orderDTO.getORDER_ID()))
                .as("savedCardId mismatch")
                .isNotNull();
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Process transaction with CC, transaction token and SubscriptionId generated with PPI for ADD N PAY when " +
            "subscriptionAmountType=VARIABLE and subscriptionFrequencyUnit=YEAR and subscriptionGraceDays = 3")
    public void TC_NPS015_ProcessAddnpayVariableSubscriptionCC(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_ADDNPAY;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("5")
                .setSubscriptionPaymentMode("PPI")
                .setSubsPPIOnly("Y")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("YEAR")
                .setSubscriptionGraceDays("3")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()) - 1);
        FetchPaymentOptResponseDTO fetchPaymentOptResponse = execute_validateFetchPaymentOption(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), PayMethodType.BALANCE.toString(), false);
        Assertions.assertThat(NativeHelpers.isAddMoneyFetchPaymentOptStatusMatched(fetchPaymentOptResponse, PayMethodType.CREDIT_CARD.toString(), false))
                .as("CREDIT_CARD payMethod is disabled for addMoney merchant")
                .isTrue();

        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(merchant, initTxnDTO.getBody().getOrderId(), txnToken, PayMethodType.CREDIT_CARD, subsId)
                .setPaymentFlow("ADDANDPAY")
                .setAUTH_MODE("3D")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnType("SALE")
                .validateGatewayName("WALLET")
                .validateRespCode("01")
                .validateBankName("WALLET")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .validateSubsid(subsId)
                .AssertAll();
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("subcription_start_date", subsId, orderDTO.getORDER_ID()))
                .as("subcription_start_date mismatch")
                .contains(CommonHelpers.getDate().toString());
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("acquirement_id", subsId, orderDTO.getORDER_ID()))
                .as("acquirement_id is null")
                .isNotNull();
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("status", subsId, orderDTO.getORDER_ID()))
                .as("subs status mismatch")
                .isEqualTo("ACTIVE");
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Process transaction with DC, transaction token and SubscriptionId generated with PPI for ADD N PAY when " +
            "subscriptionAmountType=VARIABLE and subscriptionFrequencyUnit=YEAR and subscriptionGraceDays = 3")
    public void TC_NPS015_ProcessAddnpayVariableSubscriptionDC(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_ADDNPAY;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("5")
                .setSubscriptionPaymentMode("PPI")
                .setSubsPPIOnly("Y")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("YEAR")
                .setSubscriptionGraceDays("3")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()) - 1);
        FetchPaymentOptResponseDTO fetchPaymentOptResponse = execute_validateFetchPaymentOption(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), PayMethodType.BALANCE.toString(), false);
        Assertions.assertThat(NativeHelpers.isAddMoneyFetchPaymentOptStatusMatched(fetchPaymentOptResponse, PayMethodType.DEBIT_CARD.toString(), false))
                .as("DEBIT_CARD payMethod is disabled for addMoney merchant")
                .isTrue();

        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(merchant, initTxnDTO.getBody().getOrderId(), txnToken, PayMethodType.DEBIT_CARD, subsId)
                .setPaymentFlow("ADDANDPAY")
                .setAUTH_MODE("otp")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnType("SALE")
                .validateGatewayName("WALLET")
                .validateRespCode("01")
                .validateBankName("WALLET")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .validateSubsid(subsId)
                .AssertAll();
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("subcription_start_date", subsId, orderDTO.getORDER_ID()))
                .as("subcription_start_date mismatch")
                .contains(CommonHelpers.getDate().toString());
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("acquirement_id", subsId, orderDTO.getORDER_ID()))
                .as("acquirement_id is null")
                .isNotNull();
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("status", subsId, orderDTO.getORDER_ID()))
                .as("subs status mismatch")
                .isEqualTo("ACTIVE");
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Process transaction with NB, transaction token and SubscriptionId generated with PPI for ADD N PAY when " +
            "subscriptionAmountType=VARIABLE and subscriptionFrequencyUnit=YEAR and subscriptionGraceDays = 3")
    public void TC_NPS015_ProcessAddnpayVariableSubscriptionNB(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PPI;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("5")
                .setSubscriptionPaymentMode("PPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("YEAR")
                .setSubscriptionGraceDays("3")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()) - 1);
        FetchPaymentOptResponseDTO fetchPaymentOptResponse = execute_validateFetchPaymentOption(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), PayMethodType.BALANCE.toString(), false);
        Assertions.assertThat(NativeHelpers.isAddMoneyFetchPaymentOptStatusMatched(fetchPaymentOptResponse, PayMethodType.NET_BANKING.toString(), false))
                .as("NET_BANKING payMethod is disabled for addMoney merchant")
                .isTrue();

        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(merchant, initTxnDTO.getBody().getOrderId(), txnToken, PayMethodType.NET_BANKING, subsId)
                .setPaymentFlow("ADDANDPAY")
                .setAUTH_MODE("USRPWD")
                .setChannelCode("ICICI")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnType("SALE")
                .validateGatewayName("WALLET")
                .validateRespCode("01")
                .validateBankName("WALLET")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .validateSubsid(subsId)
                .AssertAll();
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("subcription_start_date", subsId, orderDTO.getORDER_ID()))
                .as("subcription_start_date mismatch")
                .contains(CommonHelpers.getDate().toString());
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("acquirement_id", subsId, orderDTO.getORDER_ID()))
                .as("acquirement_id is null")
                .isNotNull();
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("status", subsId, orderDTO.getORDER_ID()))
                .as("subs status mismatch")
                .isEqualTo("ACTIVE");
    }

    @Issue("PGP-25588")
    @Parameters({"isNativePlus"})
    @Test(description = "Process transaction with UPI, transaction token and SubscriptionId generated with PPI for ADD N PAY when " +
            "subscriptionAmountType=VARIABLE and subscriptionFrequencyUnit=YEAR and subscriptionGraceDays = 3",groups = Status.BUG)
    public void TC_NPS015_ProcessAddnpayVariableSubscriptionUPI(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PPI;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("5")
                .setSubscriptionPaymentMode("PPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("YEAR")
                .setSubscriptionGraceDays("3")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()) - 1);
        FetchPaymentOptResponseDTO fetchPaymentOptResponse = execute_validateFetchPaymentOption(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), PayMethodType.BALANCE.toString(), false);
        Assertions.assertThat(NativeHelpers.isAddMoneyFetchPaymentOptStatusMatched(fetchPaymentOptResponse, PayMethodType.UPI.toString(), false))
                .as("UPI payMethod is disabled for addMoney merchant")
                .isTrue();

        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(merchant, initTxnDTO.getBody().getOrderId(), txnToken, PayMethodType.UPI, subsId)
                .setPaymentFlow("ADDANDPAY")
                .setAUTH_MODE("USRPWD")
                .setPayerAccount(new PaymentDTO().getVpa())
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnType("SALE")
                .validateGatewayName("WALLET")
                .validateRespCode("01")
                .validateBankName("WALLET")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .validateSubsid(subsId)
                .AssertAll();
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("subcription_start_date", subsId, orderDTO.getORDER_ID()))
                .as("subcription_start_date mismatch")
                .contains(CommonHelpers.getDate().toString());
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("acquirement_id", subsId, orderDTO.getORDER_ID()))
                .as("acquirement_id is null")
                .isNotNull();
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("status", subsId, orderDTO.getORDER_ID()))
                .as("subs status mismatch")
                .isEqualTo("ACTIVE");
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Process transaction with CC, transaction token and SubscriptionId generated with PPI for ADD N PAY when " +
            "subscriptionAmountType=FIX and subscriptionFrequencyUnit=YEAR and subscriptionGraceDays = 3")
    public void TC_NPS015_ProcessAddnpayFixSubscriptionCC(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_ADDNPAY;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("PPI")
                .setSubsPPIOnly("Y")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("YEAR")
                .setSubscriptionGraceDays("3")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()) - 1);
        FetchPaymentOptResponseDTO fetchPaymentOptResponse = execute_validateFetchPaymentOption(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), PayMethodType.BALANCE.toString(), false);
        Assertions.assertThat(NativeHelpers.isAddMoneyFetchPaymentOptStatusMatched(fetchPaymentOptResponse, PayMethodType.CREDIT_CARD.toString(), false))
                .as("CREDIT_CARD payMethod is disabled for addMoney merchant")
                .isTrue();

        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(merchant, initTxnDTO.getBody().getOrderId(), txnToken, PayMethodType.CREDIT_CARD, subsId)
                .setPaymentFlow("ADDANDPAY")
                .setAUTH_MODE("3D")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnType("SALE")
                .validateGatewayName("WALLET")
                .validateRespCode("01")
                .validateBankName("WALLET")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .validateSubsid(subsId)
                .AssertAll();
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("subcription_start_date", subsId, orderDTO.getORDER_ID()))
                .as("subcription_start_date mismatch")
                .contains(CommonHelpers.getDate().toString());
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("acquirement_id", subsId, orderDTO.getORDER_ID()))
                .as("acquirement_id is null")
                .isNotNull();
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("status", subsId, orderDTO.getORDER_ID()))
                .as("subs status mismatch")
                .isEqualTo("ACTIVE");
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Process transaction with DC, transaction token and SubscriptionId generated with PPI for ADD N PAY when " +
            "subscriptionAmountType=FIX and subscriptionFrequencyUnit=YEAR and subscriptionGraceDays = 3")
    public void TC_NPS015_ProcessAddnpayFixSubscriptionDC(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_ADDNPAY;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("PPI")
                .setSubsPPIOnly("Y")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("YEAR")
                .setSubscriptionGraceDays("3")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()) - 1);
        FetchPaymentOptResponseDTO fetchPaymentOptResponse = execute_validateFetchPaymentOption(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), PayMethodType.BALANCE.toString(), false);
        Assertions.assertThat(NativeHelpers.isAddMoneyFetchPaymentOptStatusMatched(fetchPaymentOptResponse, PayMethodType.DEBIT_CARD.toString(), false))
                .as("DEBIT_CARD payMethod is disabled for addMoney merchant")
                .isTrue();

        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(merchant, initTxnDTO.getBody().getOrderId(), txnToken, PayMethodType.DEBIT_CARD, subsId)
                .setPaymentFlow("ADDANDPAY")
                .setAUTH_MODE("otp")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnType("SALE")
                .validateGatewayName("WALLET")
                .validateRespCode("01")
                .validateBankName("WALLET")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .validateSubsid(subsId)
                .AssertAll();
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("subcription_start_date", subsId, orderDTO.getORDER_ID()))
                .as("subcription_start_date mismatch")
                .contains(CommonHelpers.getDate().toString());
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("acquirement_id", subsId, orderDTO.getORDER_ID()))
                .as("acquirement_id is null")
                .isNotNull();
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("status", subsId, orderDTO.getORDER_ID()))
                .as("subs status mismatch")
                .isEqualTo("ACTIVE");
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Process transaction with NB, transaction token and SubscriptionId generated with PPI for ADD N PAY when " +
            "subscriptionAmountType=FIX and subscriptionFrequencyUnit=YEAR and subscriptionGraceDays = 3")
    public void TC_NPS015_ProcessAddnpayFixSubscriptionNB(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PPI;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("PPI")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("YEAR")
                .setSubscriptionGraceDays("3")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()) - 1);
        FetchPaymentOptResponseDTO fetchPaymentOptResponse = execute_validateFetchPaymentOption(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), PayMethodType.BALANCE.toString(), false);
        Assertions.assertThat(NativeHelpers.isAddMoneyFetchPaymentOptStatusMatched(fetchPaymentOptResponse, PayMethodType.NET_BANKING.toString(), false))
                .as("NET_BANKING payMethod is disabled for addMoney merchant")
                .isTrue();

        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(merchant, initTxnDTO.getBody().getOrderId(), txnToken, PayMethodType.NET_BANKING, subsId)
                .setPaymentFlow("ADDANDPAY")
                .setAUTH_MODE("USRPWD")
                .setChannelCode("ICICI")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnType("SALE")
                .validateGatewayName("WALLET")
                .validateRespCode("01")
                .validateBankName("WALLET")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .validateSubsid(subsId)
                .AssertAll();
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("subcription_start_date", subsId, orderDTO.getORDER_ID()))
                .as("subcription_start_date mismatch")
                .contains(CommonHelpers.getDate().toString());
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("acquirement_id", subsId, orderDTO.getORDER_ID()))
                .as("acquirement_id is null")
                .isNotNull();
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("status", subsId, orderDTO.getORDER_ID()))
                .as("subs status mismatch")
                .isEqualTo("ACTIVE");
    }

    @Issue("PGP-25588")
    @Parameters({"isNativePlus"})
    @Test(description = "Process transaction with UPI, transaction token and SubscriptionId generated with PPI for ADD N PAY when " +
            "subscriptionAmountType=FIX and subscriptionFrequencyUnit=YEAR and subscriptionGraceDays = 3",groups = Status.BUG)
    public void TC_NPS015_ProcessAddnpayFIXSubscriptionUPI(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PPI;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("PPI")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("YEAR")
                .setSubscriptionGraceDays("3")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()) - 1);
        FetchPaymentOptResponseDTO fetchPaymentOptResponse = execute_validateFetchPaymentOption(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), PayMethodType.BALANCE.toString(), false);
        Assertions.assertThat(NativeHelpers.isAddMoneyFetchPaymentOptStatusMatched(fetchPaymentOptResponse, PayMethodType.UPI.toString(), false))
                .as("UPI payMethod is disabled for addMoney merchant")
                .isTrue();

        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(merchant, initTxnDTO.getBody().getOrderId(), txnToken, PayMethodType.UPI, subsId)
                .setPaymentFlow("ADDANDPAY")
                .setAUTH_MODE("USRPWD")
                .setPayerAccount(new PaymentDTO().getVpa())
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnType("SALE")
                .validateGatewayName("WALLET")
                .validateRespCode("01")
                .validateBankName("WALLET")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .validateSubsid(subsId)
                .AssertAll();
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("subcription_start_date", subsId, orderDTO.getORDER_ID()))
                .as("subscription_start_date mismatch")
                .contains(CommonHelpers.getDate().toString());
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("acquirement_id", subsId, orderDTO.getORDER_ID()))
                .as("acquirement_id is null")
                .isNotNull();
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("status", subsId, orderDTO.getORDER_ID()))
                .as("subs status mismatch")
                .isEqualTo("ACTIVE");
    }
    //Deprecated CC/DC Flow after SI Hub Release
//    @Parameters({"isNativePlus"})
//    @Test(enabled = false, description = "Process transaction with valid CC, transaction token and SubscriptionId generated when " +
//            "subscriptionAmountType=VARIABLE and subscriptionFrequencyUnit=WEEK and subscriptionGraceDays = 1")
    public void TC_NPS016_ProcessVariableSubscriptionCCWeek(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("CC")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("2")
                .setSubscriptionFrequencyUnit("WEEK")
                .setSubscriptionGraceDays("1")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        execute_validateFetchPaymentOption(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), PayMethodType.CREDIT_CARD.toString(), false);
        execute_validateActiveBinDetail(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), CC_BIN, true);

        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(merchant, initTxnDTO.getBody().getOrderId(), txnToken, PayMethodType.CREDIT_CARD, subsId)
                .setAUTH_MODE("3D")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnType("SALE")
                .validateGatewayName("HDFC")
                .validateRespCode("01")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateSubsid(subsId)
                .AssertAll();
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("subcription_start_date", subsId, orderDTO.getORDER_ID()))
                .as("subcription_start_date mismatch")
                .contains(CommonHelpers.getDate().toString());
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("acquirement_id", subsId, orderDTO.getORDER_ID()))
                .as("acquirement_id is null")
                .isNotNull();
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("status", subsId, orderDTO.getORDER_ID()))
                .as("subs status mismatch")
                .isEqualTo("ACTIVE");
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("saved_card_id", subsId, orderDTO.getORDER_ID()))
                .as("savedCardId mismatch")
                .isNotNull();
    }
    //Deprecated CC/DC Flow after SI Hub Release
//    @Parameters({"isNativePlus"})
//    @Test(enabled = false, description = "Process transaction with valid DC, transaction token and SubscriptionId generated when " +
//            "subscriptionAmountType=VARIABLE and subscriptionFrequencyUnit=WEEK and subscriptionGraceDays = 1")
    public void TC_NPS016_ProcessVariableSubscriptionDCWeek(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("DC")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("2")
                .setSubscriptionFrequencyUnit("WEEK")
                .setSubscriptionGraceDays("1")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        execute_validateFetchPaymentOption(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), PayMethodType.DEBIT_CARD.toString(), false);
        execute_validateActiveBinDetail(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), DC_BIN, true);

        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(merchant, initTxnDTO.getBody().getOrderId(), txnToken, PayMethodType.DEBIT_CARD, subsId)
                .setAUTH_MODE("otp")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnType("SALE")
                .validateGatewayName("HDFC")
                .validateRespCode("01")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateSubsid(subsId)
                .AssertAll();
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("subcription_start_date", subsId, orderDTO.getORDER_ID()))
                .as("subcription_start_date mismatch")
                .contains(CommonHelpers.getDate().toString());
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("acquirement_id", subsId, orderDTO.getORDER_ID()))
                .as("acquirement_id is null")
                .isNotNull();
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("status", subsId, orderDTO.getORDER_ID()))
                .as("subs status mismatch")
                .isEqualTo("ACTIVE");
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("saved_card_id", subsId, orderDTO.getORDER_ID()))
                .as("savedCardId mismatch")
                .isNotNull();
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Process transaction with valid PPI, transaction token and SubscriptionId generated when " +
            "subscriptionAmountType=VARIABLE and subscriptionFrequencyUnit=WEEK and subscriptionGraceDays = 1")
    public void TC_NPS016_ProcessVariableSubscriptionPPIWeek(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        WalletHelpers.modifyBalance(user, 10.0);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_ADDNPAY;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("PPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("2")
                .setSubscriptionFrequencyUnit("WEEK")
                .setSubscriptionGraceDays("1")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        execute_validateFetchPaymentOption(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), PayMethodType.BALANCE.toString(), false);

        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(merchant, initTxnDTO.getBody().getOrderId(), txnToken, PayMethodType.BALANCE, subsId)
                .setAUTH_MODE("USRPWD")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnType("SALE")
                .validateGatewayName("WALLET")
                .validateRespCode("01")
                .validateBankName("WALLET")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .validateSubsid(subsId)
                .AssertAll();
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("subcription_start_date", subsId, orderDTO.getORDER_ID()))
                .as("subcription_start_date mismatch")
                .contains(CommonHelpers.getDate().toString());
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("acquirement_id", subsId, orderDTO.getORDER_ID()))
                .as("acquirement_id is null")
                .isNotNull();
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("status", subsId, orderDTO.getORDER_ID()))
                .as("subs status mismatch")
                .isEqualTo("ACTIVE");
    }
    //Deprecated CC/DC Flow after SI Hub Release
//    @Parameters({"isNativePlus"})
//    @Test(enabled = false, description = "Process transaction with valid CC, transaction token and SubscriptionId generated when " +
//            "subscriptionAmountType=VARIABLE and subscriptionFrequencyUnit=QUARTER and subscriptionGraceDays = 1")
    public void TC_NPS017_ProcessVariableSubscriptionCCQuarter(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("CC")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("QUARTER")
                .setSubscriptionGraceDays("1")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        execute_validateFetchPaymentOption(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), PayMethodType.CREDIT_CARD.toString(), false);
        execute_validateActiveBinDetail(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), CC_BIN, true);

        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(merchant, initTxnDTO.getBody().getOrderId(), txnToken, PayMethodType.CREDIT_CARD, subsId)
                .setAUTH_MODE("3D")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnType("SALE")
                .validateGatewayName("HDFC")
                .validateRespCode("01")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateSubsid(subsId)
                .AssertAll();
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("subcription_start_date", subsId, orderDTO.getORDER_ID()))
                .as("subcription_start_date mismatch")
                .contains(CommonHelpers.getDate().toString());
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("acquirement_id", subsId, orderDTO.getORDER_ID()))
                .as("acquirement_id is null")
                .isNotNull();
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("status", subsId, orderDTO.getORDER_ID()))
                .as("subs status mismatch")
                .isEqualTo("ACTIVE");
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("saved_card_id", subsId, orderDTO.getORDER_ID()))
                .as("savedCardId mismatch")
                .isNotNull();
    }
    //Deprecated CC/DC Flow after SI Hub Release
//    @Parameters({"isNativePlus"})
//    @Test(enabled = false, description = "Process transaction with valid DC, transaction token and SubscriptionId generated when " +
//            "subscriptionAmountType=VARIABLE and subscriptionFrequencyUnit=QUARTER and subscriptionGraceDays = 1")
    public void TC_NPS017_ProcessVariableSubscriptionDCQuarter(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("DC")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("QUARTER")
                .setSubscriptionGraceDays("1")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        execute_validateFetchPaymentOption(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), PayMethodType.DEBIT_CARD.toString(), false);
        execute_validateActiveBinDetail(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), DC_BIN, true);

        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(merchant, initTxnDTO.getBody().getOrderId(), txnToken, PayMethodType.DEBIT_CARD, subsId)
                .setAUTH_MODE("otp")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnType("SALE")
                .validateGatewayName("HDFC")
                .validateRespCode("01")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateSubsid(subsId)
                .AssertAll();
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("subcription_start_date", subsId, orderDTO.getORDER_ID()))
                .as("subcription_start_date mismatch")
                .contains(CommonHelpers.getDate().toString());
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("acquirement_id", subsId, orderDTO.getORDER_ID()))
                .as("acquirement_id is null")
                .isNotNull();
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("status", subsId, orderDTO.getORDER_ID()))
                .as("subs status mismatch")
                .isEqualTo("ACTIVE");
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("saved_card_id", subsId, orderDTO.getORDER_ID()))
                .as("savedCardId mismatch")
                .isNotNull();
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Process transaction with valid PPI, transaction token and SubscriptionId generated when " +
            "subscriptionAmountType=VARIABLE and subscriptionFrequencyUnit=QUARTER and subscriptionGraceDays = 1")
    public void TC_NPS017_ProcessVariableSubscriptionPPIQuarter(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_ADDNPAY;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("PPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("QUARTER")
                .setSubscriptionGraceDays("1")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        execute_validateFetchPaymentOption(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), PayMethodType.BALANCE.toString(), false);
        WalletHelpers.modifyBalance(user, Double.valueOf(initTxnDTO.txnAmountFromBody()));

        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(merchant, initTxnDTO.getBody().getOrderId(), txnToken, PayMethodType.BALANCE, subsId)
                .setAUTH_MODE("USRPWD")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnType("SALE")
                .validateGatewayName("WALLET")
                .validateRespCode("01")
                .validateBankName("WALLET")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .validateSubsid(subsId)
                .AssertAll();
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("subcription_start_date", subsId, orderDTO.getORDER_ID()))
                .as("subcription_start_date mismatch")
                .contains(CommonHelpers.getDate().toString());
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("acquirement_id", subsId, orderDTO.getORDER_ID()))
                .as("acquirement_id is null")
                .isNotNull();
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("status", subsId, orderDTO.getORDER_ID()))
                .as("subs status mismatch")
                .isEqualTo("ACTIVE");
    }
    //Deprecated CC/DC Flow after SI Hub Release
//    @Parameters({"isNativePlus"})
//    @Test(enabled = false, description = "Process transaction with valid CC, transaction token and SubscriptionId generated when " +
//            "subscriptionAmountType=VARIABLE and subscriptionFrequencyUnit=BI_MONTHLY and subscriptionGraceDays = 1")
    public void TC_NPS018_ProcessVariableSubscriptionCCBimonthly(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("CC")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("BI_MONTHLY")
                .setSubscriptionGraceDays("1")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        execute_validateFetchPaymentOption(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), PayMethodType.CREDIT_CARD.toString(), false);
        execute_validateActiveBinDetail(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), CC_BIN, true);

        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(merchant, initTxnDTO.getBody().getOrderId(), txnToken, PayMethodType.CREDIT_CARD, subsId)
                .setAUTH_MODE("3D")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnType("SALE")
                .validateGatewayName("HDFC")
                .validateRespCode("01")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateSubsid(subsId)
                .AssertAll();
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("subcription_start_date", subsId, orderDTO.getORDER_ID()))
                .as("subcription_start_date mismatch")
                .contains(CommonHelpers.getDate().toString());
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("acquirement_id", subsId, orderDTO.getORDER_ID()))
                .as("acquirement_id is null")
                .isNotNull();
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("status", subsId, orderDTO.getORDER_ID()))
                .as("subs status mismatch")
                .isEqualTo("ACTIVE");
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("saved_card_id", subsId, orderDTO.getORDER_ID()))
                .as("savedCardId mismatch")
                .isNotNull();
    }
    //Deprecated CC/DC Flow after SI Hub Release
//    @Parameters({"isNativePlus"})
//    @Test(enabled = false, description = "Process transaction with valid DC, transaction token and SubscriptionId generated when " +
//            "subscriptionAmountType=VARIABLE and subscriptionFrequencyUnit=BI_MONTHLY and subscriptionGraceDays = 1")
    public void TC_NPS018_ProcessVariableSubscriptionDCBimonthly(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("DC")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("BI_MONTHLY")
                .setSubscriptionGraceDays("1")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        execute_validateFetchPaymentOption(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), PayMethodType.DEBIT_CARD.toString(), false);
        execute_validateActiveBinDetail(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), DC_BIN, true);

        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(merchant, initTxnDTO.getBody().getOrderId(), txnToken, PayMethodType.DEBIT_CARD, subsId)
                .setAUTH_MODE("otp")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnType("SALE")
                .validateGatewayName("HDFC")
                .validateRespCode("01")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateSubsid(subsId)
                .AssertAll();
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("subcription_start_date", subsId, orderDTO.getORDER_ID()))
                .as("subcription_start_date mismatch")
                .contains(CommonHelpers.getDate().toString());
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("acquirement_id", subsId, orderDTO.getORDER_ID()))
                .as("acquirement_id is null")
                .isNotNull();
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("status", subsId, orderDTO.getORDER_ID()))
                .as("subs status mismatch")
                .isEqualTo("ACTIVE");
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("saved_card_id", subsId, orderDTO.getORDER_ID()))
                .as("savedCardId mismatch")
                .isNotNull();
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Process transaction with valid PPI, transaction token and SubscriptionId generated when " +
            "subscriptionAmountType=VARIABLE and subscriptionFrequencyUnit=BI_MONTHLY and subscriptionGraceDays = 1")
    public void TC_NPS018_ProcessVariableSubscriptionPPIBimonthly(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_ADDNPAY;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("PPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("BI_MONTHLY")
                .setSubscriptionGraceDays("1")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        execute_validateFetchPaymentOption(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), PayMethodType.BALANCE.toString(), false);
        WalletHelpers.modifyBalance(user, Double.valueOf(initTxnDTO.txnAmountFromBody()));

        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(merchant, initTxnDTO.getBody().getOrderId(), txnToken, PayMethodType.BALANCE, subsId)
                .setAUTH_MODE("USRPWD")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnType("SALE")
                .validateGatewayName("WALLET")
                .validateRespCode("01")
                .validateBankName("WALLET")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .validateSubsid(subsId)
                .AssertAll();
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("subcription_start_date", subsId, orderDTO.getORDER_ID()))
                .as("subcription_start_date mismatch")
                .contains(CommonHelpers.getDate().toString());
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("acquirement_id", subsId, orderDTO.getORDER_ID()))
                .as("acquirement_id is null")
                .isNotNull();
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("status", subsId, orderDTO.getORDER_ID()))
                .as("subs status mismatch")
                .isEqualTo("ACTIVE");
    }
    //Deprecated CC/DC Flow after SI Hub Release
//    @Parameters({"isNativePlus"})
//    @Test(enabled = false, description = "Process transaction with valid CC, transaction token and SubscriptionId generated when " +
//            "subscriptionAmountType=VARIABLE and subscriptionFrequencyUnit=SEMI_ANNUALLY and subscriptionGraceDays = 1")
    public void TC_NPS019_ProcessVariableSubscriptionCCSemiAnnually(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("CC")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("SEMI_ANNUALLY")
                .setSubscriptionGraceDays("1")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        execute_validateFetchPaymentOption(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), PayMethodType.CREDIT_CARD.toString(), false);
        execute_validateActiveBinDetail(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), CC_BIN, true);

        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(merchant, initTxnDTO.getBody().getOrderId(), txnToken, PayMethodType.CREDIT_CARD, subsId)
                .setAUTH_MODE("3D")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnType("SALE")
                .validateGatewayName("HDFC")
                .validateRespCode("01")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateSubsid(subsId)
                .AssertAll();
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("subcription_start_date", subsId, orderDTO.getORDER_ID()))
                .as("subcription_start_date mismatch")
                .contains(CommonHelpers.getDate().toString());
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("acquirement_id", subsId, orderDTO.getORDER_ID()))
                .as("acquirement_id is null")
                .isNotNull();
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("status", subsId, orderDTO.getORDER_ID()))
                .as("subs status mismatch")
                .isEqualTo("ACTIVE");
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("saved_card_id", subsId, orderDTO.getORDER_ID()))
                .as("savedCardId mismatch")
                .isNotNull();
    }
    //Deprecated CC/DC Flow after SI Hub Release
//    @Parameters({"isNativePlus"})
//    @Test(enabled = false, description = "Process transaction with valid DC, transaction token and SubscriptionId generated when " +
//            "subscriptionAmountType=VARIABLE and subscriptionFrequencyUnit=SEMI_ANNUALLY and subscriptionGraceDays = 1")
    public void TC_NPS019_ProcessVariableSubscriptionDCSemiAnnually(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("DC")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("SEMI_ANNUALLY")
                .setSubscriptionGraceDays("1")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        execute_validateFetchPaymentOption(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), PayMethodType.DEBIT_CARD.toString(), false);
        execute_validateActiveBinDetail(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), DC_BIN, true);

        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(merchant, initTxnDTO.getBody().getOrderId(), txnToken, PayMethodType.DEBIT_CARD, subsId)
                .setAUTH_MODE("otp")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnType("SALE")
                .validateGatewayName("HDFC")
                .validateRespCode("01")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateSubsid(subsId)
                .AssertAll();
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("subcription_start_date", subsId, orderDTO.getORDER_ID()))
                .as("subcription_start_date mismatch")
                .contains(CommonHelpers.getDate().toString());
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("acquirement_id", subsId, orderDTO.getORDER_ID()))
                .as("acquirement_id is null")
                .isNotNull();
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("status", subsId, orderDTO.getORDER_ID()))
                .as("subs status mismatch")
                .isEqualTo("ACTIVE");
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("saved_card_id", subsId, orderDTO.getORDER_ID()))
                .as("savedCardId mismatch")
                .isNotNull();
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Process transaction with valid PPI, transaction token and SubscriptionId generated when " +
            "subscriptionAmountType=VARIABLE and subscriptionFrequencyUnit=SEMI_ANNUALLY and subscriptionGraceDays = 1")
    public void TC_NPS019_ProcessVariableSubscriptionPPISemiAnnually(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_ADDNPAY;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("PPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("SEMI_ANNUALLY")
                .setSubscriptionGraceDays("1")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        execute_validateFetchPaymentOption(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), PayMethodType.BALANCE.toString(), false);
        WalletHelpers.modifyBalance(user, Double.valueOf(initTxnDTO.txnAmountFromBody()));

        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(merchant, initTxnDTO.getBody().getOrderId(), txnToken, PayMethodType.BALANCE, subsId)
                .setAUTH_MODE("USRPWD")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnType("SALE")
                .validateGatewayName("WALLET")
                .validateRespCode("01")
                .validateBankName("WALLET")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .validateSubsid(subsId)
                .AssertAll();
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("subcription_start_date", subsId, orderDTO.getORDER_ID()))
                .as("subcription_start_date mismatch")
                .contains(CommonHelpers.getDate().toString());
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("acquirement_id", subsId, orderDTO.getORDER_ID()))
                .as("acquirement_id is null")
                .isNotNull();
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("status", subsId, orderDTO.getORDER_ID()))
                .as("subs status mismatch")
                .isEqualTo("ACTIVE");
    }
    //Deprecated CC/DC Flow after SI Hub Release
//    @Parameters({"isNativePlus"})
//    @Test(enabled = false, description = "Process transaction with CC saved card, transaction token and SubscriptionId generated when " +
//            "subscriptionAmountType=VARIABLE and subscriptionFrequencyUnit=MONTH and txnValue = 0 and subscriptionGraceDays = 1")
    public void TC_NPS020_ProcessVariableSubscriptionCCZeroAmount(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        String savedCardId = SavedCardHelpers.getSavedCardId(user, 0);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("0")
                .setSubscriptionPaymentMode("CC")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("2")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("1")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        execute_validateSavedFetchPaymentOption(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), savedCardId, false);
        execute_validateActiveBinDetail(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), CC_BIN, true);

        paymentDTO.setSavedCardId(savedCardId);
        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(Constants.MerchantType.Subscription_PGOnly, initTxnDTO.getBody().getOrderId(), txnToken, PayMethodType.CREDIT_CARD, subsId, paymentDTO)
                .setAUTH_MODE("3D")
                .setCardInfo(savedCardId+"|||")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnType("SALE")
                .validateGatewayName("HDFC")
                .validateRespCode("01")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateSubsid(subsId)
                .AssertAll();
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("subcription_start_date", subsId, orderDTO.getORDER_ID()))
                .as("subcription_start_date mismatch")
                .contains(CommonHelpers.getDate().toString());
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("acquirement_id", subsId, orderDTO.getORDER_ID()))
                .as("acquirement_id is null")
                .isNotNull();
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("status", subsId, orderDTO.getORDER_ID()))
                .as("subs status mismatch")
                .isEqualTo("ACTIVE");
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("saved_card_id", subsId, orderDTO.getORDER_ID()))
                .as("savedCardId mismatch")
                .isNotNull();
    }
    //Deprecated CC/DC Flow after SI Hub Release
//    @Parameters({"isNativePlus"})
//    @Test(enabled = false, description = "Process transaction with DC saved card, transaction token and SubscriptionId generated when " +
//            "subscriptionAmountType=VARIABLE and subscriptionFrequencyUnit=MONTH and txnValue = 0 and subscriptionGraceDays = 1")
    public void TC_NPS020_ProcessVariableSubscriptionDCZeroAmount(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getDebitCardNumber());
        String savedCardId = SavedCardHelpers.getSavedCardId(user, 0);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("0")
                .setSubscriptionPaymentMode("DC")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("2")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("1")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        execute_validateSavedFetchPaymentOption(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), savedCardId, false);
        execute_validateActiveBinDetail(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), DC_BIN, true);

        paymentDTO.setSavedCardId(savedCardId);
        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(Constants.MerchantType.Subscription_PGOnly, initTxnDTO.getBody().getOrderId(), txnToken, PayMethodType.DEBIT_CARD, subsId, paymentDTO)
                .setAUTH_MODE("otp")
                .setCardInfo(savedCardId+"|||")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnType("SALE")
                .validateGatewayName("HDFC")
                .validateRespCode("01")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateSubsid(subsId)
                .AssertAll();
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("subcription_start_date", subsId, orderDTO.getORDER_ID()))
                .as("subcription_start_date mismatch")
                .contains(CommonHelpers.getDate().toString());
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("acquirement_id", subsId, orderDTO.getORDER_ID()))
                .as("acquirement_id is null")
                .isNotNull();
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("status", subsId, orderDTO.getORDER_ID()))
                .as("subs status mismatch")
                .isEqualTo("ACTIVE");
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("saved_card_id", subsId, orderDTO.getORDER_ID()))
                .as("savedCardId mismatch")
                .isNotNull();
    }
    //Deprecated CC/DC Flow after SI Hub Release
 //   @Parameters({"isNativePlus"})
 //   @Test(enabled = false, description = "Verify fail of native subs when " +
 //           "PAYMENT_MODE=CC and AMOUNT_TYPE=VARIABLE and FREQ_UNIT=WEEK and GRACE_DAY=1 and FREQ=0 and START_DATE=''")
    public void TC_NPS021_ProcessSubs_VAR_CC_WEEK(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("CC")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("0")
                .setSubscriptionFrequencyUnit("WEEK")
                .setSubscriptionGraceDays("1")
                .setSubscriptionStartDate("")
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(responseDTO.getBody().getResultInfo().getResultStatus())
                .as("Result Status mismatch")
                .isEqualToIgnoringCase("F");
        softAssertions.assertThat(responseDTO.getBody().getResultInfo().getResultCode())
                .as("Result Code mismatch")
                .isEqualToIgnoringCase("4001");
        softAssertions.assertThat(responseDTO.getBody().getResultInfo().getResultMsg())
                .as("Result MSg mismatch")
                .isEqualToIgnoringCase("Invalid Subscription start date");
        softAssertions.assertAll();
    }
    //Deprecated CC/DC Flow after SI Hub Release
//    @Parameters({"isNativePlus"})
//    @Test(enabled = false, description = "Verify fail of native subs when " +
//            "PAYMENT_MODE=CC and AMOUNT_TYPE=VARIABLE and FREQ_UNIT=WEEK and GRACE_DAY=1 and FREQ=0 and START_DATE=less than today date")
    public void TC_NPS022_FailProcessSubs_VAR_CC_WEEK_startDateLesthanToday(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("CC")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("0")
                .setSubscriptionFrequencyUnit("WEEK")
                .setSubscriptionGraceDays("1")
                .setSubscriptionStartDate(CommonHelpers.getDate().minusDays(1).toString())
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(responseDTO.getBody().getResultInfo().getResultStatus())
                .as("Result Status mismatch")
                .isEqualToIgnoringCase("F");
        softAssertions.assertThat(responseDTO.getBody().getResultInfo().getResultCode())
                .as("Result Code mismatch")
                .isEqualToIgnoringCase("4001");
        softAssertions.assertThat(responseDTO.getBody().getResultInfo().getResultMsg())
                .as("Result MSg mismatch")
                .isEqualToIgnoringCase("Invalid Subscription start date");
        softAssertions.assertAll();
    }
    //Deprecated CC/DC Flow after SI Hub Release
//    @Parameters({"isNativePlus"})
//    @Test(enabled = false, description = "Process Guest Checkout transaction with valid CC, transaction token and SubscriptionId generated " +
//            "when subscriptionAmountType=VARIABLE and subscriptionFrequencyUnit=MONTH and subscriptionGraceDays = 5")
    public void TC_SGC01_GuestCheckout(@Optional("false") Boolean isNativePlus) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("CC")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("5")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        execute_validateFetchPaymentOption(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), PayMethodType.CREDIT_CARD.toString(), false);
        execute_validateActiveBinDetail(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), CC_BIN, true);
        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(merchant, initTxnDTO.getBody().getOrderId(),
                txnToken, PayMethodType.CREDIT_CARD, subsId)
                .setAUTH_MODE("3D")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnType("SALE")
                .validateGatewayName("HDFC")
                .validateRespCode("01")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateSubsid(subsId)
                .AssertAll();
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("subcription_start_date", subsId, orderDTO.getORDER_ID()))
                .as("subcription_start_date mismatch")
                .contains(CommonHelpers.getDate().toString());
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("acquirement_id", subsId, orderDTO.getORDER_ID()))
                .as("acquirement_id is null")
                .isNotNull();
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("status", subsId, orderDTO.getORDER_ID()))
                .as("subs status mismatch")
                .isEqualTo("ACTIVE");
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("saved_card_id", subsId, orderDTO.getORDER_ID()))
                .as("savedCardId mismatch")
                .isNotNull();
    }

    //Deprecated CC/DC Flow after SI Hub Release
//    @Parameters({"isNativePlus"})
//    @Test(enabled = false, description = "Verify success of native subs when " +
//            "PAYMENT_MODE=CC and AMOUNT_TYPE=VARIABLE and FREQ_UNIT=WEEK and GRACE_DAY=1 and FREQ=1 and START_DATE=greater than today date")
    public void TC_NPS023_ProcessSubs_VAR_CC_WEEK_startDateGreaterThanToday(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("CC")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("WEEK")
                .setSubscriptionGraceDays("1")
                .setSubscriptionStartDate(CommonHelpers.getDate().plusDays(1).toString())
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        execute_validateFetchPaymentOption(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), PayMethodType.CREDIT_CARD.toString(), false);
        execute_validateActiveBinDetail(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), CC_BIN, true);
        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(merchant, initTxnDTO.getBody().getOrderId(),
                txnToken, PayMethodType.CREDIT_CARD, subsId)
                .setAUTH_MODE("3D")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnType("SALE")
                .validateGatewayName("HDFC")
                .validateRespCode("01")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateSubsid(subsId)
                .AssertAll();
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("subcription_start_date", subsId, orderDTO.getORDER_ID()))
                .as("subcription_start_date mismatch")
                .contains(CommonHelpers.getDate().plusDays(1).toString());
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("acquirement_id", subsId, orderDTO.getORDER_ID()))
                .as("acquirement_id is null")
                .isNotNull();
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("status", subsId, orderDTO.getORDER_ID()))
                .as("subs status mismatch")
                .isEqualTo("ACTIVE");
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("saved_card_id", subsId, orderDTO.getORDER_ID()))
                .as("savedCardId mismatch")
                .isNotNull();
    }


//    @Parameters({"isNativePlus"})
//    @Test(description = "Cancel Subscription of subscriptionAmountType=VARIABLE and " +
//            "subscriptionFrequencyUnit=MONTH and subscriptionGraceDays = 5", enabled = false, groups = {Status.TO_BE_FIXED})
    public void TC_NPS013_CancelSubscription(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        String currentMethodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        String TxnValue = "1";
        String SubscriptionPaymentMode = "CC";
        String SubscriptionAmountType = "VARIABLE";
        String SubscriptionMaxAmount = "10";
        String SubscriptionFrequency = "1";
        String SubscriptionFrequencyUnit = "MONTH";
        String SubscriptionGraceDays = "5";
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Object[] fetchTrxDetails = validateInitiateSubscriptionTrxToken(user, new String[]{currentMethodName, TxnValue, SubscriptionPaymentMode, SubscriptionAmountType,
                SubscriptionMaxAmount, SubscriptionFrequency, SubscriptionFrequencyUnit, SubscriptionGraceDays, SubscriptionStartDate});
        InitTxnDTO initTxnDTO = (InitTxnDTO) fetchTrxDetails[2];
        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(Constants.MerchantType.Subscription_PGOnly
                , initTxnDTO.getBody().getOrderId(),
                fetchTrxDetails[0].toString().toString(), PayMethodType.CREDIT_CARD, fetchTrxDetails[1].toString().toString()).setAUTH_MODE("3D").build();
        validateFetchPaymentOptions(initTxnDTO, fetchTrxDetails[0].toString().toString(), PayMethodType.CREDIT_CARD);
        validate_BinDetail(fetchTrxDetails[0].toString(), initTxnDTO, orderDTO, CC_BIN);
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        CommonHelpers.validateTxnStatus(orderDTO, initTxnDTO, txnStatus, Constants.Gateway.HDFC.toString(),
                Constants.Gateway.HDFC.toString(), "CC");
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("subcription_start_date", fetchTrxDetails[1].toString(), orderDTO.getORDER_ID())).
                contains(CommonHelpers.getDate().toString());
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("acquirement_id", fetchTrxDetails[1].toString(),
                orderDTO.getORDER_ID())).isNotNull();
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("status", fetchTrxDetails[1].toString(),
                orderDTO.getORDER_ID())).isEqualTo("ACTIVE");
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("saved_card_id", fetchTrxDetails[1].toString(), orderDTO.getORDER_ID())).isNotNull();
        CancelSubscriptionDTO cancelSubscriptionDTO = new CancelSubscriptionDTO.Builder(Constants.
                MerchantType.Subscription_PGOnly.getId(), orderDTO.getSUBSCRIPTION_ID(), initTxnDTO.getBody().
                getPaytmSsoToken(), initTxnDTO.getHead().getSignature()).build();
        CancelSubscription cancelSubscription = new CancelSubscription(cancelSubscriptionDTO);
        JsonPath cancelSubJson = cancelSubscription.execute().jsonPath();
        CommonHelpers.assertCheck(cancelSubJson, new Object[]{
                "body.resultInfo.code", "400",
                "body.resultInfo.message", "The SSO Validation Failed.",
                "body.resultInfo.status", "FAILURE"
        });
    }
    //Deprecated CC/DC Flow after SI Hub Release
//    @Parameters({"isNativePlus"})
//    @Test(enabled = false, description = "Validate subs created with blank paymode")
    public void blankPaymode(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("")//blank paymode
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("5")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);        //Initiate create subs first request
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        execute_validateFetchPaymentOption(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), PayMethodType.CREDIT_CARD.toString(), false);
        execute_validateActiveBinDetail(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), CC_BIN, true);
        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(merchant, initTxnDTO.getBody().getOrderId(),
                txnToken, PayMethodType.CREDIT_CARD, subsId)
                .setAUTH_MODE("3D")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);             // payment through created subscription request
        PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnType("SALE")
                .validateGatewayName("HDFC")
                .validateRespCode("01")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateSubsid(subsId)
                .AssertAll();

        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("subcription_start_date", subsId, orderDTO.getORDER_ID()))
                .as("subcription_start_date not available")
                .contains(CommonHelpers.getDate().toString());
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("acquirement_id", subsId, orderDTO.getORDER_ID()))
                .as("acquirement_id is null")
                .isNotNull();
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("status", subsId, orderDTO.getORDER_ID()))
                .as("status mismatch")
                .isEqualTo("ACTIVE");
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("saved_card_id", subsId, orderDTO.getORDER_ID()))
                .as("savedCardId is null")
                .isNotNull();
    }

    //Deprecated CC/DC Flow after SI Hub Release
//    @Epic("PGP-24445")
//    @Owner("Tarun")
//    @Test(enabled = false, description = "To verify that subscription payment once cancelled with first saved card , should be processed with second saved card and 2nd saved card id should be stored in DB for renewal")
    public void savedCardId2ShouldBeStoredFF4jFlagOn() throws Exception {
        FF4JFlags.enable("theia.createOrderInNativeSubscriptionCreation");
        PaymentDTO paymentDTO = new PaymentDTO();
        PaymentDTO paymentDTO1 = new PaymentDTO().setCreditCardNumber(PaymentDTO.ICICI_CREDIT_CARD_NUMBER);

        Constants.MerchantType subscriptionMerchant = Constants.MerchantType.Subscription_PGOnly;
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.addCard(user,paymentDTO.getExpMonth(),paymentDTO.getExpYear(),paymentDTO.getCreditCardNumber());
        SavedCardHelpers.addCard(user,paymentDTO1.getExpMonth(),paymentDTO1.getExpYear(),paymentDTO1.getCreditCardNumber());
        String savedCardIdCC = SavedCardHelpers.getSavedCardId(user, 0);
        String savedCardIdCC2 = SavedCardHelpers.getSavedCardId(user, 1);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), subscriptionMerchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("CC")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("2")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(subscriptionMerchant.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("CREDIT_CARD")
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setSubsId(subsId)
                .setStoreInstrument("1")
                .setCardInfo(savedCardIdCC + "||" + paymentDTO.getCvvNumber() + "|")
                .build();
        QRHelper.executeProcessTransactionV1(processTxnV1Request);

        ProcessTxnV1Request processTxnV1Request1 = new ProcessTxnV1Request
                .Builder(subscriptionMerchant.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("CREDIT_CARD")
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setSubsId(subsId)
                .setStoreInstrument("1")
                .setCardInfo(savedCardIdCC2 + "||" + paymentDTO1.getCvvNumber() + "|")
                .build();

        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request1);
        ResponsePage responsePage = new ResponsePage();
        responsePage
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateOrderId(initTxnDTO.orderFromBody())
                .validateMid(subscriptionMerchant.getId())
                .assertAll();

        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("saved_card_id", subsId, initTxnDTO.orderFromBody()))
                .as("savedCardId mismatch")
                .isEqualTo(savedCardIdCC2);

    }
    //Deprecated CC/DC Flow after SI Hub Release
//    @Epic("PGP-24445")
//    @Owner("Tarun")
//    @Test(enabled = false, priority = 1,description = "To verify that subscription payment once cancelled with first saved card , should be processed with second saved card and 2nd saved card id should be stored in DB for renewal")
    public void savedCardId2ShouldBeStoredWithFF4jFlagOff() throws Exception {
        FF4JFlags.disable("theia.createOrderInNativeSubscriptionCreation");

        PaymentDTO paymentDTO = new PaymentDTO();
        PaymentDTO paymentDTO1 = new PaymentDTO().setCreditCardNumber(PaymentDTO.ICICI_CREDIT_CARD_NUMBER);

        Constants.MerchantType subscriptionMerchant = Constants.MerchantType.Subscription_PGOnly;
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.addCard(user,paymentDTO.getExpMonth(),paymentDTO.getExpYear(),paymentDTO.getCreditCardNumber());
        SavedCardHelpers.addCard(user,paymentDTO1.getExpMonth(),paymentDTO1.getExpYear(),paymentDTO1.getCreditCardNumber());
        String savedCardIdCC = SavedCardHelpers.getSavedCardId(user, 0);
        String savedCardIdCC2 = SavedCardHelpers.getSavedCardId(user, 1);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), subscriptionMerchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("CC")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("2")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(subscriptionMerchant.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("CREDIT_CARD")
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setSubsId(subsId)
                .setStoreInstrument("1")
                .setCardInfo(savedCardIdCC + "||" + paymentDTO.getCvvNumber() + "|")
                .build();
        QRHelper.executeProcessTransactionV1(processTxnV1Request);

        ProcessTxnV1Request processTxnV1Request1 = new ProcessTxnV1Request
                .Builder(subscriptionMerchant.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("CREDIT_CARD")
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setSubsId(subsId)
                .setStoreInstrument("1")
                .setCardInfo(savedCardIdCC2 + "||" + paymentDTO1.getCvvNumber() + "|")
                .build();

        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request1);
        ResponsePage responsePage = new ResponsePage();
        responsePage
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateOrderId(initTxnDTO.orderFromBody())
                .validateMid(subscriptionMerchant.getId())
                .assertAll();

        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("saved_card_id", subsId, initTxnDTO.orderFromBody()))
                .as("savedCardId mismatch")
                .isEqualTo(savedCardIdCC2);

        //Enabling it again
       FF4JFlags.enable("theia.createOrderInNativeSubscriptionCreation");

    }
    //Deprecated CC/DC Flow after SI Hub Release
//    @Test(enabled = false, description = "Verify Error Message When Subs Max Amount and Txn Amount both are zero and subscriptionAmountType is FIX")
    public void verifyErrorMessageWhenMaxAmtandTxnAmtisZeroTypeFIX() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("0")
                .setSubscriptionPaymentMode("CC")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("0")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("5")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultStatus())
                .as("resultStatus mismatch")
                .isEqualTo("U");
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultCode())
                .as("resultCode mismatch")
                .isEqualTo("00000900");
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultMsg())
                .as("resultMsg mismatch")
                .isEqualToIgnoringCase("SubMaxAmount cannot be zero set against the subscription");

    }
    //Deprecated CC/DC Flow after SI Hub Release
//    @Test(enabled = false, description = "Verify Error Message When Subs Max Amount and Txn Amount both are zero and subscriptionAmountType is VARIABLE")
    public void verifyErrorMessageWhenMaxAmtandTxnAmtisZeroTypeVARIABLE() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("0")
                .setSubscriptionPaymentMode("CC")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("0")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("5")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultStatus())
                .as("resultStatus mismatch")
                .isEqualTo("U");
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultCode())
                .as("resultCode mismatch")
                .isEqualTo("00000900");
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultMsg())
                .as("resultMsg mismatch")
                .isEqualToIgnoringCase("SubMaxAmount cannot be zero set against the subscription");

    }

    //Deprecated CC/DC Flow after SI Hub Release
    //PGP-21763 Verify Different Subscription Error Messages
//    @Test(enabled = false, description = "Verify HDFC DC Paymode Not Allowed when only HDFC CC Enabled in Subscription Flow.")
    public void verifyHDFCDebitCardNotAllowedwithOnlyHDFC_CCEnabledSubscription() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        String[] banks1 = {"HDFC"};
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("NORMAL")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("5")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setEnablePaymentMode(new EnablePaymentMode[]
                        {
                                new EnablePaymentMode().setMode("CREDIT_CARD").setBanks(banks1)
                        })
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        FetchBinDetailsRequest request = new FetchBinDetailsRequest.Builder(txnToken, "532571").build(); //HDFC DC Bin
        FetchBinDetail fetchBinDetail = new FetchBinDetail(request, initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody());
        JsonPath fetchBinsJson = fetchBinDetail.execute().jsonPath();
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultCode")).isEqualTo("2011");
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultMsg")).isEqualTo("MASTER Debit card is not allowed for NORMAL payment. Please try paying using other cards/options.");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail")).isNull();
    }
    //Deprecated CC/DC Flow after SI Hub Release
//    @Owner("Jai")
//    @Test(enabled = false, description = "Verify that Invalid Frequency Unit message is correctly displayed")
    public void PGP_21763_verifyInvalidFrequencyUnitMessage() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("CC")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("")
                .setSubscriptionGraceDays("5")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
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
                .isEqualToIgnoringCase("Invalid Frequency Unit");      //Initiate create subs first request
    }
    //Deprecated CC/DC Flow after SI Hub Release
//    @Owner("Jai")
//    @Test(enabled = false, description = "Verify that Invalid Subscription Amount Type message correctly displayed")
    public void PGP_21763_verifyInvalidSubsAmountTypeMessage() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("CC")
                .setSubscriptionAmountType("")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("5")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
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
                .isEqualToIgnoringCase("Invalid Subscription Amount Type");      //Initiate create subs first request
    }
    //Deprecated CC/DC Flow after SI Hub Release
//    @Test(enabled = false, description = "Verify that Invalid Max. Amount message is correctly displayed")
    public void PGP_21763_verifyInvalidMaxSubsAmountMessage() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("CC")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("5")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
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
                .isEqualToIgnoringCase("Invalid Max Amount");      //Initiate create subs first request
    }

    //Disabliing testcase as thees checks are now obsolete after discussion with Udit verma and Nikunj Kumar
//    @Owner("Jai")
//    @Test(description = "Verify that 'Transaction amount cannot be greater than the max amount set against the subscription' message is displayed ", enabled = false)
    public void PGP_21763_verifyTxnAmountCannotBeGreaterthanMaxSubsAmountMessage() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("11")
                .setSubscriptionPaymentMode("CC")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("5")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultStatus())
                .as("resultStatus mismatch")
                .isEqualTo("U");
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultCode())
                .as("resultCode mismatch")
                .isEqualTo("00000900");
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultMsg())
                .as("resultMsg mismatch")
                .isEqualToIgnoringCase("Transaction amount cannot be greater than the max amount set against the subscription");
    }
    //Deprecated CC/DC Flow after SI Hub Release
//    @Test(enabled = false, description = "Verify that Grace days cannot be greater than the frequency set against the subscription message is correctly displayed")
    public void PGP_21763_verifyGraceDaysCannotBeGreaterthanFrequencyMessage() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("CC")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("32")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
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
                .isEqualToIgnoringCase("Grace days cannot be greater than the frequency set against the subscription");      //Initiate create subs first request
    }

    @Test(description = "Verify that \"Paymode selected is not enabled for your merchant account. Please reach out to support teams to enable\n" +
            "\" message is displayed correctly")
    public void PGP_21763_verifyInvalidPaymodeSelectedMessage() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_WALLET_ONLY;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("NB")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("5")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
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

    @Test(description = "Verify that 'Max amount is greater than the permissible amount limit for available paymodes' message is displayed correctly")
    public void PGP_21763_verifyMaxAmtGreaterThanPermissibleAmtMessage() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_WALLET_LIMIT;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("PPI")
                .setSubsPPIOnly("Y")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("5100")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("5")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultStatus())
                .as("resultStatus mismatch")
                .isEqualTo("F");
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultCode())
                .as("resultCode mismatch")
                .isEqualTo("4004");
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultMsg())
                .as("resultMsg mismatch")
                .isEqualToIgnoringCase("Max amount is greater than the permissible amount limit for available paymodes");
    }
    //Deprecated CC/DC Flow after SI Hub Release
//    @Test(enabled = false, description = "Verify that Invalid AppInvokeDevice message is displayed")
    public void PGP_21763_verifyInvalidAppInvokeDeviceMessage() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("CC")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("5")
                .setAppInvokeDevice("abcde")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
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
                .isEqualToIgnoringCase("Invalid AppInvokeDevice");
    }
    //Deprecated CC/DC Flow after SI Hub Release
//    @Test(enabled = false, description = "Verify that Grace days value is mandatory")
    public void PGP_21763_verifyGraceDaysMandatoryMessage() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("CC")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionGraceDays("")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
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
    }

    //Disabliing testcase as thees checks are now obsolete after discussion with Udit verma and Nikunj Kumar
//    @Test(description = "Verify that Transaction amount is not equal to the max amount set against the subscription", enabled = false)
    public void PGP_21763_verifyTxnAmtNotEqualtoMaxAmtMessage() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionGraceDays("5")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
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
        execute_validateFetchPaymentOption(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), PayMethodType.BALANCE.toString(), false);

        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(merchant, initTxnDTO.getBody().getOrderId(), txnToken, PayMethodType.BALANCE, subsId)
                .setAUTH_MODE("USRPWD")
                .build();
        checkoutPage.createNativeOrder(orderDTO, false);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespMsg("Transaction amount is not equal to the max amount set against the subscription")
                .validateRespCode("810")
                .validateStatus("TXN_FAILURE")
                .assertAll();

    }

    @Owner(Constants.Owner.JAI)
    @Test(description = "Verify Successfull BANK_MANDATE Subscription Txn(Upfront Amt) with App Invoke Flow and verify status in Transaction Status v1 api.")
    public void PGP_23279_verifySuccessBankMandateNativeSubscriptionwithhAppInvokeFlowUpfrontAmt(@Optional("enhancedwap_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.BANK_MANDATE;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("BANK_MANDATE")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("1")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("")
                .setMandateAccountDetails(new MandateAccountDetails())
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        execute_validateFetchPaymentOption(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), "BANK_MANDATE", false);

        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(merchant, initTxnDTO.getBody().getOrderId(), txnToken)
                .setTXN_AMOUNT("10")
                .build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabEnach().click();
        cashierPage.proceedButton().click();
        BankMandatePage bankMandatePage = BankMandatePageFactory.getBankMandatePage(theme);
        bankMandatePage.payToSubscribe().click();
        //cashierPage.bankMandateConfirmPay().waitUntilVisible();
        //cashierPage.bankMandateConfirmPay().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespMsg("SUCCESS")
                .validateRespCode("3006")
                .validatePaymentMode("BANK_MANDATE")
                .assertAll();
        TransactionStatusV1DTO transactionStatusV1DTO = new TransactionStatusV1DTO();
        transactionStatusV1DTO.setBody(new Body().setIsCallbackUrlRequired(true).setIsFinalTxnStatusRequired(false).setMid(orderDTO.getMID()).setOrderId(orderDTO.getORDER_ID()))
                .setHead(new Head().setTokenType("TXN_TOKEN").setToken(txnToken));
        TransactionStatusV1API transactionStatusV1API = new TransactionStatusV1API(transactionStatusV1DTO);
        Response response = transactionStatusV1API.execute();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.jsonPath().getBoolean("body.isPollingRequired")).isEqualTo(false);
        softly.assertThat(response.jsonPath().getString("body.txnInfo.STATUS")).isEqualTo("TXN_SUCCESS");
        softly.assertThat(response.jsonPath().getString("body.txnInfo.PAYMENTMODE")).isEqualTo("BANK_MANDATE");
        softly.assertThat(response.jsonPath().getString("body.txnInfo.SUBS_ID")).isEqualTo(subsId);
        softly.assertThat(response.jsonPath().getString("body.txnInfo.MID")).isEqualTo(orderDTO.getMID());
        softly.assertThat(response.jsonPath().getString("body.txnInfo.ORDERID")).isEqualTo(orderDTO.getORDER_ID());
        softly.assertAll();
    }

    @Owner(Constants.Owner.JAI)
    @Test(description = "Verify Successfull BANK_MANDATE Native MF_SIP Subscription Txn(Upfront Amt) with App Invoke Flow and verify status in Transaction Status v1 api.")
    public void PGP_23279_verifySuccessBankMandateNativeMFSIPwithhAppInvokeFlowUpfrontAmt(@Optional("enhancedwap_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.MF_SIP_NEW_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("BANK_MANDATE")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("1")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_MF_SIP")
                .setMandateAccountDetails(new MandateAccountDetails())
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        execute_validateFetchPaymentOption(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), "BANK_MANDATE", false);

        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(merchant, initTxnDTO.getBody().getOrderId(), txnToken)
                .setTXN_AMOUNT("10")
                .build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabEnach().click();
        /*cashierPage.buttonPGPayNow().waitUntilVisible();
        cashierPage.buttonPGPayNow().click();*/
        //cashierPage.bankMandateConfirmPay().waitUntilVisible();
        //cashierPage.bankMandateConfirmPay().click();
        cashierPage.proceedButton().click();
        BankMandatePage bankMandatePage = BankMandatePageFactory.getBankMandatePage(theme);
        bankMandatePage.payToSubscribe().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespMsg("SUCCESS")
                .validateRespCode("3006")
                .validatePaymentMode("BANK_MANDATE")
                .assertAll();
        TransactionStatusV1DTO transactionStatusV1DTO = new TransactionStatusV1DTO();
        transactionStatusV1DTO.setBody(new Body().setIsCallbackUrlRequired(true).setIsFinalTxnStatusRequired(false).setMid(orderDTO.getMID()).setOrderId(orderDTO.getORDER_ID()))
                .setHead(new Head().setTokenType("TXN_TOKEN").setToken(txnToken));
        TransactionStatusV1API transactionStatusV1API = new TransactionStatusV1API(transactionStatusV1DTO);
        Response response = transactionStatusV1API.execute();
        SoftAssertions softly = new SoftAssertions();
        transactionStatusV1API.validateChecksum(response, orderDTO, merchant.getKey());
        softly.assertThat(response.jsonPath().getBoolean("body.isPollingRequired")).isEqualTo(false);
        softly.assertThat(response.jsonPath().getString("body.txnInfo.STATUS")).isEqualTo("TXN_SUCCESS");
        softly.assertThat(response.jsonPath().getString("body.txnInfo.PAYMENTMODE")).isEqualTo("BANK_MANDATE");
        softly.assertThat(response.jsonPath().getString("body.txnInfo.SUBS_ID")).isEqualTo(subsId);
        softly.assertThat(response.jsonPath().getString("body.txnInfo.MID")).isEqualTo(orderDTO.getMID());
        softly.assertThat(response.jsonPath().getString("body.txnInfo.ORDERID")).isEqualTo(orderDTO.getORDER_ID());
        softly.assertAll();
    }

    //Deprecated CC/DC Flow after SI Hub Release
//    @Owner(Constants.Owner.JAI)
//    @Test(enabled = false, description = "Verify Successfull CC Subscription Txn(Upfront Amt) with App Invoke Flow and verify status in Transaction Status v1 api.")
    public void PGP_23279_verifySuccessCCSubscriptionwithhAppInvokeFlowUpfrontAmt(@Optional("enhancedwap") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("CC")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("2")
                .setSubscriptionFrequencyUnit("WEEK")
                .setSubscriptionGraceDays("1")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        execute_validateFetchPaymentOption(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), PayMethodType.CREDIT_CARD.toString(), false);
        execute_validateActiveBinDetail(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), CC_BIN, true);

        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(merchant, initTxnDTO.getBody().getOrderId(), txnToken)
                .setTXN_AMOUNT("10")
                .build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateTxnAmount("10.00")
                .validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
        TransactionStatusV1DTO transactionStatusV1DTO = new TransactionStatusV1DTO();
        transactionStatusV1DTO.setBody(new Body().setIsCallbackUrlRequired(true).setIsFinalTxnStatusRequired(false).setMid(orderDTO.getMID()).setOrderId(orderDTO.getORDER_ID()))
                .setHead(new Head().setTokenType("TXN_TOKEN").setToken(txnToken));
        TransactionStatusV1API transactionStatusV1API = new TransactionStatusV1API(transactionStatusV1DTO);
        Response response = transactionStatusV1API.execute();
        SoftAssertions softly = new SoftAssertions();
        transactionStatusV1API.validateChecksum(response, orderDTO, merchant.getKey());
        softly.assertThat(response.jsonPath().getBoolean("body.isPollingRequired")).isEqualTo(false);
        softly.assertThat(response.jsonPath().getString("body.txnInfo.STATUS")).isEqualTo("TXN_SUCCESS");
        softly.assertThat(response.jsonPath().getString("body.txnInfo.PAYMENTMODE")).isEqualTo("CC");
        softly.assertThat(response.jsonPath().getString("body.txnInfo.SUBS_ID")).isEqualTo(subsId);
        softly.assertThat(response.jsonPath().getString("body.txnInfo.TXNAMOUNT")).isEqualTo("10.00");
        softly.assertThat(response.jsonPath().getString("body.txnInfo.MID")).isEqualTo(orderDTO.getMID());
        softly.assertThat(response.jsonPath().getString("body.txnInfo.ORDERID")).isEqualTo(orderDTO.getORDER_ID());
        softly.assertAll();
    }
    //Deprecated CC/DC Flow after SI Hub Release
//    @Owner(Constants.Owner.JAI)
//    @Test(enabled = false, description = "Verify Successfull CC Subscription Txn(Zero Amt) with App Invoke Flow and verify status in Transaction Status v1 api.")
    public void PGP_23279_verifySuccessCCSubscriptionwithhAppInvokeFlowZeroAmt(@Optional("enhancedwap") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("0")
                .setSubscriptionPaymentMode("CC")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("2")
                .setSubscriptionFrequencyUnit("WEEK")
                .setSubscriptionGraceDays("1")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        execute_validateFetchPaymentOption(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), PayMethodType.CREDIT_CARD.toString(), false);
        execute_validateActiveBinDetail(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), CC_BIN, true);

        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(merchant, initTxnDTO.getBody().getOrderId(), txnToken)
                .setTXN_AMOUNT("0")
                .build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateTxnAmount("1.00")
                .validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
        TransactionStatusV1DTO transactionStatusV1DTO = new TransactionStatusV1DTO();
        transactionStatusV1DTO.setBody(new Body().setIsCallbackUrlRequired(true).setIsFinalTxnStatusRequired(false).setMid(orderDTO.getMID()).setOrderId(orderDTO.getORDER_ID()))
                .setHead(new Head().setTokenType("TXN_TOKEN").setToken(txnToken));
        TransactionStatusV1API transactionStatusV1API = new TransactionStatusV1API(transactionStatusV1DTO);
        Response response = transactionStatusV1API.execute();
        SoftAssertions softly = new SoftAssertions();
        transactionStatusV1API.validateChecksum(response, orderDTO, merchant.getKey());
        softly.assertThat(response.jsonPath().getBoolean("body.isPollingRequired")).isEqualTo(false);
        softly.assertThat(response.jsonPath().getString("body.txnInfo.STATUS")).isEqualTo("TXN_SUCCESS");
        softly.assertThat(response.jsonPath().getString("body.txnInfo.PAYMENTMODE")).isEqualTo("CC");
        softly.assertThat(response.jsonPath().getString("body.txnInfo.SUBS_ID")).isEqualTo(subsId);
        softly.assertThat(response.jsonPath().getString("body.txnInfo.TXNAMOUNT")).isEqualTo("1.00");
        softly.assertThat(response.jsonPath().getString("body.txnInfo.MID")).isEqualTo(orderDTO.getMID());
        softly.assertThat(response.jsonPath().getString("body.txnInfo.ORDERID")).isEqualTo(orderDTO.getORDER_ID());
        softly.assertAll();
    }
    //Deprecated CC/DC Flow after SI Hub Release
//    @Owner(Constants.Owner.JAI)
//    @Test(enabled = false, description = "Verify Successfull DC Subscription Txn(Upfront Amt) with App Invoke Flow and verify status in Transaction Status v1 api.")
    public void PGP_23279_verifySuccessDCSubscriptionwithhAppInvokeFlowUpfrontAmt(@Optional("enhancedwap") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("DC")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("2")
                .setSubscriptionFrequencyUnit("WEEK")
                .setSubscriptionGraceDays("1")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        execute_validateFetchPaymentOption(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), PayMethodType.DEBIT_CARD.toString(), false);

        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(merchant, initTxnDTO.getBody().getOrderId(), txnToken)
                .setTXN_AMOUNT("10")
                .build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(Constants.PayMode.DC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateTxnAmount("10.00")
                .validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
        TransactionStatusV1DTO transactionStatusV1DTO = new TransactionStatusV1DTO();
        transactionStatusV1DTO.setBody(new Body().setIsCallbackUrlRequired(true).setIsFinalTxnStatusRequired(false).setMid(orderDTO.getMID()).setOrderId(orderDTO.getORDER_ID()))
                .setHead(new Head().setTokenType("TXN_TOKEN").setToken(txnToken));
        TransactionStatusV1API transactionStatusV1API = new TransactionStatusV1API(transactionStatusV1DTO);
        Response response = transactionStatusV1API.execute();
        SoftAssertions softly = new SoftAssertions();
        transactionStatusV1API.validateChecksum(response, orderDTO, merchant.getKey());
        softly.assertThat(response.jsonPath().getBoolean("body.isPollingRequired")).isEqualTo(false);
        softly.assertThat(response.jsonPath().getString("body.txnInfo.STATUS")).isEqualTo("TXN_SUCCESS");
        softly.assertThat(response.jsonPath().getString("body.txnInfo.PAYMENTMODE")).isEqualTo("DC");
        softly.assertThat(response.jsonPath().getString("body.txnInfo.SUBS_ID")).isEqualTo(subsId);
        softly.assertThat(response.jsonPath().getString("body.txnInfo.TXNAMOUNT")).isEqualTo("10.00");
        softly.assertThat(response.jsonPath().getString("body.txnInfo.MID")).isEqualTo(orderDTO.getMID());
        softly.assertThat(response.jsonPath().getString("body.txnInfo.ORDERID")).isEqualTo(orderDTO.getORDER_ID());
        softly.assertAll();
    }
    //Deprecated CC/DC Flow after SI Hub Release
//    @Owner(Constants.Owner.JAI)
//    @Test(enabled = false, description = "Verify Successfull DC Subscription Txn(Zero Amt) with App Invoke Flow and verify status in Transaction Status v1 api.")
    public void PGP_23279_verifySuccessDCSubscriptionwithhAppInvokeFlowZeroAmt(@Optional("enhancedwap") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("0")
                .setSubscriptionPaymentMode("DC")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("2")
                .setSubscriptionFrequencyUnit("WEEK")
                .setSubscriptionGraceDays("1")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        execute_validateFetchPaymentOption(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), PayMethodType.DEBIT_CARD.toString(), false);

        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(merchant, initTxnDTO.getBody().getOrderId(), txnToken)
                .setTXN_AMOUNT("10")
                .build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(Constants.PayMode.DC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateTxnAmount("1.00")
                .validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
        TransactionStatusV1DTO transactionStatusV1DTO = new TransactionStatusV1DTO();
        transactionStatusV1DTO.setBody(new Body().setIsCallbackUrlRequired(true).setIsFinalTxnStatusRequired(false).setMid(orderDTO.getMID()).setOrderId(orderDTO.getORDER_ID()))
                .setHead(new Head().setTokenType("TXN_TOKEN").setToken(txnToken));
        TransactionStatusV1API transactionStatusV1API = new TransactionStatusV1API(transactionStatusV1DTO);
        Response response = transactionStatusV1API.execute();
        SoftAssertions softly = new SoftAssertions();
        transactionStatusV1API.validateChecksum(response, orderDTO, merchant.getKey());
        softly.assertThat(response.jsonPath().getBoolean("body.isPollingRequired")).isEqualTo(false);
        softly.assertThat(response.jsonPath().getString("body.txnInfo.STATUS")).isEqualTo("TXN_SUCCESS");
        softly.assertThat(response.jsonPath().getString("body.txnInfo.PAYMENTMODE")).isEqualTo("DC");
        softly.assertThat(response.jsonPath().getString("body.txnInfo.SUBS_ID")).isEqualTo(subsId);
        softly.assertThat(response.jsonPath().getString("body.txnInfo.TXNAMOUNT")).isEqualTo("1.00");
        softly.assertThat(response.jsonPath().getString("body.txnInfo.MID")).isEqualTo(orderDTO.getMID());
        softly.assertThat(response.jsonPath().getString("body.txnInfo.ORDERID")).isEqualTo(orderDTO.getORDER_ID());
        softly.assertAll();
    }

    @Owner(Constants.Owner.JAI)
    @Test(description = "Verify Successfull PPI Subscription Txn(Upfront Amt) with App Invoke Flow and verify status in Transaction Status v1 api.")
    public void PGP_23279_verifySuccessPPISubscriptionwithhAppInvokeFlowUpfrontAmt(@Optional("enhancedwap_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PPI;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("PPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("2")
                .setSubscriptionFrequencyUnit("WEEK")
                .setSubscriptionGraceDays("1")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        execute_validateFetchPaymentOption(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), PayMethodType.BALANCE.toString(), false);

        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(merchant, initTxnDTO.getBody().getOrderId(), txnToken)
                .setTXN_AMOUNT("10")
                .build();
        WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()));
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.checkBoxPPI().check();
        cashierPage.payBy(Constants.PayMode.WALLET);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateTxnAmount("10.00")
                .validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
        TransactionStatusV1DTO transactionStatusV1DTO = new TransactionStatusV1DTO();
        transactionStatusV1DTO.setBody(new Body().setIsCallbackUrlRequired(true).setIsFinalTxnStatusRequired(false).setMid(orderDTO.getMID()).setOrderId(orderDTO.getORDER_ID()))
                .setHead(new Head().setTokenType("TXN_TOKEN").setToken(txnToken));
        TransactionStatusV1API transactionStatusV1API = new TransactionStatusV1API(transactionStatusV1DTO);
        Response response = transactionStatusV1API.execute();
        SoftAssertions softly = new SoftAssertions();
        transactionStatusV1API.validateChecksum(response, orderDTO, merchant.getKey());
        softly.assertThat(response.jsonPath().getBoolean("body.isPollingRequired")).isEqualTo(false);
        softly.assertThat(response.jsonPath().getString("body.txnInfo.STATUS")).isEqualTo("TXN_SUCCESS");
        softly.assertThat(response.jsonPath().getString("body.txnInfo.PAYMENTMODE")).isEqualTo("PPI");
        softly.assertThat(response.jsonPath().getString("body.txnInfo.SUBS_ID")).isEqualTo(subsId);
        softly.assertThat(response.jsonPath().getString("body.txnInfo.TXNAMOUNT")).isEqualTo("10.00");
        softly.assertThat(response.jsonPath().getString("body.txnInfo.MID")).isEqualTo(orderDTO.getMID());
        softly.assertThat(response.jsonPath().getString("body.txnInfo.ORDERID")).isEqualTo(orderDTO.getORDER_ID());
        softly.assertAll();
    }

    @Owner(Constants.Owner.JAI)
    @Test(description = "Verify Successfull PPI Subscription Txn(Zero Amt) with App Invoke Flow and verify status in Transaction Status v1 api.")
    public void PGP_23279_verifySuccessPPISubscriptionwithhAppInvokeFlowZeroAmt(@Optional("enhancedwap") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PPI;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("0")
                .setSubscriptionPaymentMode("PPI")
                .setSubsPPIOnly("Y")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("2")
                .setSubscriptionFrequencyUnit("WEEK")
                .setSubscriptionGraceDays("1")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        execute_validateFetchPaymentOption(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), PayMethodType.BALANCE.toString(), false);

        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(merchant, initTxnDTO.getBody().getOrderId(), txnToken)
                .setTXN_AMOUNT("0")
                .build();
        WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()) + 1);
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(Constants.PayMode.WALLET);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateTxnAmount("1.00")
                .validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
        TransactionStatusV1DTO transactionStatusV1DTO = new TransactionStatusV1DTO();
        transactionStatusV1DTO.setBody(new Body().setIsCallbackUrlRequired(true).setIsFinalTxnStatusRequired(false).setMid(orderDTO.getMID()).setOrderId(orderDTO.getORDER_ID()))
                .setHead(new Head().setTokenType("TXN_TOKEN").setToken(txnToken));
        TransactionStatusV1API transactionStatusV1API = new TransactionStatusV1API(transactionStatusV1DTO);
        Response response = transactionStatusV1API.execute();
        SoftAssertions softly = new SoftAssertions();
        transactionStatusV1API.validateChecksum(response, orderDTO, merchant.getKey());
        softly.assertThat(response.jsonPath().getBoolean("body.isPollingRequired")).isEqualTo(false);
        softly.assertThat(response.jsonPath().getString("body.txnInfo.STATUS")).isEqualTo("TXN_SUCCESS");
        softly.assertThat(response.jsonPath().getString("body.txnInfo.PAYMENTMODE")).isEqualTo("PPI");
        softly.assertThat(response.jsonPath().getString("body.txnInfo.SUBS_ID")).isEqualTo(subsId);
        softly.assertThat(response.jsonPath().getString("body.txnInfo.TXNAMOUNT")).isEqualTo("1.00");
        softly.assertThat(response.jsonPath().getString("body.txnInfo.MID")).isEqualTo(orderDTO.getMID());
        softly.assertThat(response.jsonPath().getString("body.txnInfo.ORDERID")).isEqualTo(orderDTO.getORDER_ID());
        softly.assertAll();
    }

    @Owner(Constants.Owner.JAI)
    @Test(description = "Verify Successfull PPBL Subscription Txn(Upfront Amt) with App Invoke Flow and verify status in Transaction Status v1 api.")
    public void PGP_23279_verifySuccessPPBLSubscriptionwithhAppInvokeFlowUpfrontAmt(@Optional("enhancedwap_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_Pg2_MID1;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("PPBL")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("2")
                .setSubscriptionFrequencyUnit("WEEK")
                .setSubscriptionGraceDays("1")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        execute_validateFetchPaymentOption(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), PayMethodType.PPBL.toString(), false);

        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(merchant, initTxnDTO.getBody().getOrderId(), txnToken)
                .setTXN_AMOUNT("10")
                .build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.textBoxPPBLPassCode().waitUntilEditable();
        cashierPage.textBoxPPBLPassCode().clearAndType(new PaymentDTO().getPasscode());
        cashierPage.buttonPpblSumbit().waitUntilClickable();
        cashierPage.buttonPpblSumbit().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateTxnAmount("10.00")
                .validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
        TransactionStatusV1DTO transactionStatusV1DTO = new TransactionStatusV1DTO();
        transactionStatusV1DTO.setBody(new Body().setIsCallbackUrlRequired(true).setIsFinalTxnStatusRequired(false).setMid(orderDTO.getMID()).setOrderId(orderDTO.getORDER_ID()))
                .setHead(new Head().setTokenType("TXN_TOKEN").setToken(txnToken));
        TransactionStatusV1API transactionStatusV1API = new TransactionStatusV1API(transactionStatusV1DTO);
        Response response = transactionStatusV1API.execute();
        SoftAssertions softly = new SoftAssertions();
        transactionStatusV1API.validateChecksum(response, orderDTO, merchant.getKey());
        softly.assertThat(response.jsonPath().getBoolean("body.isPollingRequired")).isEqualTo(false);
        softly.assertThat(response.jsonPath().getString("body.txnInfo.STATUS")).isEqualTo("TXN_SUCCESS");
        softly.assertThat(response.jsonPath().getString("body.txnInfo.PAYMENTMODE")).isEqualTo("NB");
        softly.assertThat(response.jsonPath().getString("body.txnInfo.BANKNAME")).isEqualTo("PPBL");
        softly.assertThat(response.jsonPath().getString("body.txnInfo.SUBS_ID")).isEqualTo(subsId);
        softly.assertThat(response.jsonPath().getString("body.txnInfo.TXNAMOUNT")).isEqualTo("10.00");
        softly.assertThat(response.jsonPath().getString("body.txnInfo.MID")).isEqualTo(orderDTO.getMID());
        softly.assertThat(response.jsonPath().getString("body.txnInfo.ORDERID")).isEqualTo(orderDTO.getORDER_ID());
        softly.assertAll();
    }

    @Owner(Constants.Owner.JAI)
    @Test(description = "Verify Successfull PPBL Subscription Txn(Zero Amt) with App Invoke Flow and verify status in Transaction Status v1 api.")
    public void PGP_23279_verifySuccessPPBLSubscriptionwithhAppInvokeFlowZeroAmt(@Optional("enhancedwap_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PPI;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("0")
                .setSubscriptionPaymentMode("PPBL")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("2")
                .setSubscriptionFrequencyUnit("WEEK")
                .setSubscriptionGraceDays("1")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        execute_validateFetchPaymentOption(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), PayMethodType.PPBL.toString(), false);

        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(merchant, initTxnDTO.getBody().getOrderId(), txnToken)
                .setTXN_AMOUNT("0")
                .build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.textBoxPPBLPassCode().waitUntilEditable();
        cashierPage.textBoxPPBLPassCode().clearAndType(new PaymentDTO().getPasscode());
        cashierPage.buttonPpblSumbit().waitUntilClickable();
        cashierPage.buttonPpblSumbit().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateTxnAmount("1.00")
                .validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
        TransactionStatusV1DTO transactionStatusV1DTO = new TransactionStatusV1DTO();
        transactionStatusV1DTO.setBody(new Body().setIsCallbackUrlRequired(true).setIsFinalTxnStatusRequired(false).setMid(orderDTO.getMID()).setOrderId(orderDTO.getORDER_ID()))
                .setHead(new Head().setTokenType("TXN_TOKEN").setToken(txnToken));
        TransactionStatusV1API transactionStatusV1API = new TransactionStatusV1API(transactionStatusV1DTO);
        Response response = transactionStatusV1API.execute();
        SoftAssertions softly = new SoftAssertions();
        transactionStatusV1API.validateChecksum(response, orderDTO, merchant.getKey());
        softly.assertThat(response.jsonPath().getBoolean("body.isPollingRequired")).isEqualTo(false);
        softly.assertThat(response.jsonPath().getString("body.txnInfo.STATUS")).isEqualTo("TXN_SUCCESS");
        softly.assertThat(response.jsonPath().getString("body.txnInfo.PAYMENTMODE")).isEqualTo("NB");
        softly.assertThat(response.jsonPath().getString("body.txnInfo.BANKNAME")).isEqualTo("PPBL");
        softly.assertThat(response.jsonPath().getString("body.txnInfo.SUBS_ID")).isEqualTo(subsId);
        softly.assertThat(response.jsonPath().getString("body.txnInfo.TXNAMOUNT")).isEqualTo("1.00");
        softly.assertThat(response.jsonPath().getString("body.txnInfo.MID")).isEqualTo(orderDTO.getMID());
        softly.assertThat(response.jsonPath().getString("body.txnInfo.ORDERID")).isEqualTo(orderDTO.getORDER_ID());
        softly.assertAll();
    }

    @Owner(Constants.Owner.JAI)
    @Test(description = "Verify Successfull UPI Subscription Txn(Upfront Amt) with App Invoke Flow and verify status in Transaction Status v1 api.")
    public void PGP_23279_verifySuccessUPISubscriptionwithhAppInvokeFlowUpfrontAmt(@Optional("enhancedwap") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
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
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        execute_validateFetchPaymentOption(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), PayMethodType.UPI.toString(), false);

        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(merchant, initTxnDTO.getBody().getOrderId(), txnToken)
                .setTXN_AMOUNT("1")
                .build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(Constants.PayMode.UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateTxnAmount("1.00")
                .validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
        TransactionStatusV1DTO transactionStatusV1DTO = new TransactionStatusV1DTO();
        transactionStatusV1DTO.setBody(new Body().setIsCallbackUrlRequired(true).setIsFinalTxnStatusRequired(false).setMid(orderDTO.getMID()).setOrderId(orderDTO.getORDER_ID()))
                .setHead(new Head().setTokenType("TXN_TOKEN").setToken(txnToken));
        TransactionStatusV1API transactionStatusV1API = new TransactionStatusV1API(transactionStatusV1DTO);
        Response response = transactionStatusV1API.execute();
        SoftAssertions softly = new SoftAssertions();
        transactionStatusV1API.validateChecksum(response, orderDTO, merchant.getKey());
        softly.assertThat(response.jsonPath().getBoolean("body.isPollingRequired")).isEqualTo(false);
        softly.assertThat(response.jsonPath().getString("body.txnInfo.STATUS")).isEqualTo("TXN_SUCCESS");
        softly.assertThat(response.jsonPath().getString("body.txnInfo.PAYMENTMODE")).isEqualTo("UPI");
        softly.assertThat(response.jsonPath().getString("body.txnInfo.SUBS_ID")).isEqualTo(subsId);
        softly.assertThat(response.jsonPath().getString("body.txnInfo.TXNAMOUNT")).isEqualTo("1.00");
        softly.assertThat(response.jsonPath().getString("body.txnInfo.MID")).isEqualTo(orderDTO.getMID());
        softly.assertThat(response.jsonPath().getString("body.txnInfo.ORDERID")).isEqualTo(orderDTO.getORDER_ID());
        softly.assertAll();
    }
    //Deprecated CC/DC Flow after SI Hub Release
 //   @Owner(Constants.Owner.JAI)
 //   @Test(enabled = false, description = "Verify Failed CC Subscription Txn(Upfront Amt) with App Invoke Flow and verify status in Transaction Status v1 api.")
    public void PGP_23279_verifyFailedCCSubscriptionwithhAppInvokeFlowUpfrontAmt(@Optional("enhancedwap") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("99.98")
                .setSubscriptionPaymentMode("CC")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("100")
                .setSubscriptionFrequency("2")
                .setSubscriptionFrequencyUnit("WEEK")
                .setSubscriptionGraceDays("1")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        execute_validateFetchPaymentOption(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), PayMethodType.CREDIT_CARD.toString(), false);
        execute_validateActiveBinDetail(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), CC_BIN, true);

        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(merchant, initTxnDTO.getBody().getOrderId(), txnToken)
                .setTXN_AMOUNT("99.98")
                .build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateTxnAmount("99.98")
                .validateRespCode("227")
                .validateStatus("TXN_FAILURE")
                .assertAll();
        TransactionStatusV1DTO transactionStatusV1DTO = new TransactionStatusV1DTO();
        transactionStatusV1DTO.setBody(new Body().setIsCallbackUrlRequired(true).setIsFinalTxnStatusRequired(false).setMid(orderDTO.getMID()).setOrderId(orderDTO.getORDER_ID()))
                .setHead(new Head().setTokenType("TXN_TOKEN").setToken(txnToken));
        TransactionStatusV1API transactionStatusV1API = new TransactionStatusV1API(transactionStatusV1DTO);
        Response response = transactionStatusV1API.execute();
        SoftAssertions softly = new SoftAssertions();
        transactionStatusV1API.validateChecksum(response, orderDTO, merchant.getKey());
        softly.assertThat(response.jsonPath().getBoolean("body.isPollingRequired")).isEqualTo(false);
        softly.assertThat(response.jsonPath().getString("body.txnInfo.STATUS")).isEqualTo("TXN_FAILURE");
        softly.assertThat(response.jsonPath().getString("body.txnInfo.PAYMENTMODE")).isEqualTo("CC");
        softly.assertThat(response.jsonPath().getString("body.txnInfo.SUBS_ID")).isEqualTo(subsId);
        softly.assertThat(response.jsonPath().getString("body.txnInfo.TXNAMOUNT")).isEqualTo("99.98");
        softly.assertThat(response.jsonPath().getString("body.txnInfo.MID")).isEqualTo(orderDTO.getMID());
        softly.assertThat(response.jsonPath().getString("body.txnInfo.ORDERID")).isEqualTo(orderDTO.getORDER_ID());
        softly.assertAll();
    }
    //Deprecated CC/DC Flow after SI Hub Release
 //   @Owner(Constants.Owner.JAI)
 //   @Test(enabled = false, description = "Verify Pending CC Subscription Txn(Upfront Amt) with App Invoke Flow and verify status in Transaction Status v1 api.")
    public void PGP_23279_verifyPendingCCSubscriptionwithhAppInvokeFlowUpfrontAmt(@Optional("enhancedwap") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("99.84")
                .setSubscriptionPaymentMode("CC")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("100")
                .setSubscriptionFrequency("2")
                .setSubscriptionFrequencyUnit("WEEK")
                .setSubscriptionGraceDays("1")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        execute_validateFetchPaymentOption(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), PayMethodType.CREDIT_CARD.toString(), false);
        execute_validateActiveBinDetail(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), CC_BIN, true);

        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(merchant, initTxnDTO.getBody().getOrderId(), txnToken)
                .setTXN_AMOUNT("99.84")
                .build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateTxnAmount("99.84")
                .validateRespCode("402")
                .validateStatus("PENDING")
                .assertAll();
        TransactionStatusV1DTO transactionStatusV1DTO = new TransactionStatusV1DTO();
        transactionStatusV1DTO.setBody(new Body().setIsCallbackUrlRequired(true).setIsFinalTxnStatusRequired(false).setMid(orderDTO.getMID()).setOrderId(orderDTO.getORDER_ID()))
                .setHead(new Head().setTokenType("TXN_TOKEN").setToken(txnToken));
        TransactionStatusV1API transactionStatusV1API = new TransactionStatusV1API(transactionStatusV1DTO);
        Response response = transactionStatusV1API.execute();
        SoftAssertions softly = new SoftAssertions();
        transactionStatusV1API.validateChecksum(response, orderDTO, merchant.getKey());
        softly.assertThat(response.jsonPath().getBoolean("body.isPollingRequired")).isEqualTo(false);
        softly.assertThat(response.jsonPath().getString("body.txnInfo.STATUS")).isEqualTo("PENDING");
        softly.assertThat(response.jsonPath().getString("body.txnInfo.PAYMENTMODE")).isEqualTo("CC");
        softly.assertThat(response.jsonPath().getString("body.txnInfo.SUBS_ID")).isEqualTo(subsId);
        softly.assertThat(response.jsonPath().getString("body.txnInfo.TXNAMOUNT")).isEqualTo("99.84");
        softly.assertThat(response.jsonPath().getString("body.txnInfo.MID")).isEqualTo(orderDTO.getMID());
        softly.assertThat(response.jsonPath().getString("body.txnInfo.ORDERID")).isEqualTo(orderDTO.getORDER_ID());
        softly.assertAll();
    }
    //Deprecated CC/DC Flow after SI Hub Release
 //   @Owner(Constants.Owner.JAI)
 //   @Parameters({"isNativePlus"})
 //   @Test(enabled = false, description = "To Verify that subscription is created when subscriptionEnableRetry = 0")
    public void PGP_27162_verifySubscriptionwhensubscriptionEnableRetryisZero(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("CC")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("5")
                .setSubscriptionEnableRetry("0")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);        //Initiate create subs first request
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        execute_validateFetchPaymentOption(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), PayMethodType.CREDIT_CARD.toString(), false);
        execute_validateActiveBinDetail(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), CC_BIN, true);
        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(merchant, initTxnDTO.getBody().getOrderId(),
                txnToken, PayMethodType.CREDIT_CARD, subsId)
                .setAUTH_MODE("3D")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);             // payment through created subscription request
        PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnType("SALE")
                .validateGatewayName("HDFC")
                .validateRespCode("01")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateSubsid(subsId)
                .AssertAll();
    }
    //Deprecated CC/DC Flow after SI Hub Release
 //   @Owner(Constants.Owner.JAI)
 //   @Parameters({"isNativePlus"})
 //   @Test(enabled = false, description = "To Verify that subscription is created when subscriptionEnableRetry = 1")
    public void PGP_27162_verifySubscriptionwhensubscriptionEnableRetryisOne(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("CC")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("5")
                .setSubscriptionEnableRetry("1")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);        //Initiate create subs first request
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        execute_validateFetchPaymentOption(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), PayMethodType.CREDIT_CARD.toString(), false);
        execute_validateActiveBinDetail(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), CC_BIN, true);
        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(merchant, initTxnDTO.getBody().getOrderId(),
                txnToken, PayMethodType.CREDIT_CARD, subsId)
                .setAUTH_MODE("3D")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);             // payment through created subscription request
        PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnType("SALE")
                .validateGatewayName("HDFC")
                .validateRespCode("01")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateSubsid(subsId)
                .AssertAll();
    }
    //Deprecated CC/DC Flow after SI Hub Release
 //   @Owner(Constants.Owner.JAI)
 //   @Parameters({"isNativePlus"})
 //   @Test(enabled = false, description = "To Verify that subscription is created when subscriptionEnableRetry > 1")
    public void PGP_27162_verifySubscriptionwhensubscriptionEnableRetryisGreaterThanOne(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("CC")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("5")
                .setSubscriptionEnableRetry("5")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);        //Initiate create subs first request
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        execute_validateFetchPaymentOption(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), PayMethodType.CREDIT_CARD.toString(), false);
        execute_validateActiveBinDetail(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), CC_BIN, true);
        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(merchant, initTxnDTO.getBody().getOrderId(),
                txnToken, PayMethodType.CREDIT_CARD, subsId)
                .setAUTH_MODE("3D")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);             // payment through created subscription request
        PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnType("SALE")
                .validateGatewayName("HDFC")
                .validateRespCode("01")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateSubsid(subsId)
                .AssertAll();
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify after login is successful, status is changed in contract_v2 table from INIT to ACTIVE after payment")
    public void PGP_18282_ValidateStatusAfterLoginNative(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        String subsStatus=null;
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_WALLET_ONLY;
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("" ,merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("PPI")
                .setSubsPPIOnly("Y")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("5")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()));
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);            //Initiate create subs first request
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        subsStatus = PGPHelpers.getSubsStatus(initTxnDTO.orderFromBody());
        Assertions.assertThat(subsStatus).isEqualTo("INIT");
        SendOTP sendotp = new SendOTP(txnToken,user.mobNo(),initTxnDTO.getBody().getMid(),initTxnDTO.getBody().getOrderId());
        Response SendOtpResponse = sendotp.execute();
        // Validating OTP by Mock now and not retrieving from logs any 6 digit otp will work
        //String otp = AuthUtil.getOtp(user.mobNo());
        String otp = "123456";
        ValidateOTP val = new ValidateOTP(txnToken,otp,initTxnDTO.getBody().getMid(),initTxnDTO.getBody().getOrderId());
        Response ValidateOTPResponse = val.execute();
        Assertions.assertThat(ValidateOTPResponse.jsonPath().getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("Success");
        Assertions.assertThat(ValidateOTPResponse.jsonPath().getInt("body.resultInfo.resultCode")).isEqualTo(01);
        String subsmetadata = PGPHelpers.getSubsMetadata(initTxnDTO.orderFromBody());
        Assertions.assertThat(subsmetadata).contains("isOtpAuthorized\":true");
        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(Constants.MerchantType.SUBSCRIPTION_WALLET_LIMIT, initTxnDTO.getBody().getOrderId(), txnToken, PayMethodType.BALANCE, subsId)
                .setAUTH_MODE("USRPWD")
                .setTXN_AMOUNT("0")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount("1.00")
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("WALLET")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName("WALLET")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateSubsid(Constants.ValidationType.NON_EMPTY)
                .AssertAll();

        //Verify ACTIVE status in contract_v2 table
        subsStatus = PGPHelpers.getSubsStatus(orderDTO.getORDER_ID());
        Assertions.assertThat(subsStatus).isEqualTo("ACTIVE");
    }

    @Owner(Constants.Owner.TARUN)
    @Feature("PGP-27600")
    @Test(description = "Verify that payChannelOption is not displayed as blank in v2/fpo for paymode - PPBL")
    public void payChannelNotBlankFPOv2() throws Exception {
        User user = userManager.getForRead(Label.PPBL);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_Pg2_MID1;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("PPBL")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("5")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);        //Initiate create subs first request
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();

        //FetchPayment Options V2

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken)
                .build();

        FetchPaymentOptionV2 fpoV2 = new FetchPaymentOptionV2(merchant.getId(), initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);

        Response fetchPaymentOptionsJsonv2 = fpoV2.execute();

        fetchPaymentOptionsJsonv2.then()
                .statusCode(200)
                .body("body.merchantPayOption.paymentModes.find { it.paymentMode == 'PPBL'}.payChannelOptions.iconUrl",
                        not(isEmptyOrNullString()));

    }

    @Owner(Constants.Owner.TARUN)
    @Feature("PGP-27600")
    @Test(description = "Verify that payChannelOption is not displayed as blank in v2/fpo for paymode - Blank")
    public void payChannelNotBlankFPOv2PayModeBlank() throws Exception {
        User user = userManager.getForRead(Label.PPBL);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_Pg2_MID1;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("5")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);        //Initiate create subs first request
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();

        //FetchPayment Options V2

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken)
                .build();

        FetchPaymentOptionV2 fpoV2 = new FetchPaymentOptionV2(merchant.getId(), initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);

        Response fetchPaymentOptionsJsonv2 = fpoV2.execute();

        fetchPaymentOptionsJsonv2.then()
                .statusCode(200)
                .body("body.merchantPayOption.paymentModes.find { it.paymentMode == 'PPBL'}.payChannelOptions.iconUrl",
                        not(isEmptyOrNullString()));

    }


    @Owner(Constants.Owner.TARUN)
    @Feature("PGP-27600")
    @Test(description = "Verify that payChannelOption is not displayed as blank in v1/fpo for paymode - PPBL")
    public void payChannelNotBlankFPOv1PPBL() throws Exception {
        User user = userManager.getForRead(Label.PPBL);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_Pg2_MID1;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("PPBL")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("5")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);        //Initiate create subs first request
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();

        //FetchPayment Options V1

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchant.getId(), initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        Response response = fetchPaymentOption.execute();
        Assertions.assertThat(response.jsonPath().getString("body.merchantPayOption.paymentModes.find { it.paymentMode == 'PPBL'}.payChannelOptions.iconUrl")).isNotEmpty();
    }


    @Owner(Constants.Owner.TARUN)
    @Feature("PGP-27600")
    @Test(description = "Verify that payChannelOption is not displayed as blank in v1/fpo for paymode - blank")
    public void payChannelNotBlankFPOv1PayModeBlank() throws Exception {
        User user = userManager.getForRead(Label.PPBL);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_Pg2_MID1;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("5")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);        //Initiate create subs first request
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();

        //FetchPayment Options V1

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchant.getId(), initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        Response response = fetchPaymentOption.execute();
        Assertions.assertThat(response.jsonPath().getString("body.merchantPayOption.paymentModes.find { it.paymentMode == 'PPBL'}.payChannelOptions.iconUrl")).isNotEmpty();
    }

    //Deprecated CC/DC Flow after SI Hub Release
 //   @Feature("PGP-28523")
 //   @Owner(GAGANDEEP)
 //   @Test(enabled = false, description = "Verify Error Message When Subs Max Amount and Txn Amount both are zero and subscriptionAmountType is FIX")
    public void verifyErrorMessageWhenReqTypeIsInvalid() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.UPI_MUTUAL_MF;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("CC")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("1")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("5")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION") //Invalid Request Type
                .build();
        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultStatus())
                .as("resultStatus mismatch")
                .isEqualTo("F");
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultCode())
                .as("resultCode mismatch")
                .isEqualTo("302");
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultMsg())
                .as("resultMsg mismatch")
                .isEqualToIgnoringCase("Invalid Request Type");

    }



    @Feature("PGP-28523")
    @Owner(GAGANDEEP)
    @Test(description = "Verify fail of native renew subs when " +
            "PAYMENT_MODE=CC and AMOUNT_TYPE=VARIABLE and FREQ_UNIT=WEEK and GRACE_DAY=0 and FREQ=0 and START_DATE=is greater than today date")
    public void TC_validateFailedDuringCreateSubs() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("0")
                .setSubscriptionFrequencyUnit("WEEK")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultStatus())
                .as("resultStatus mismatch")
                .isEqualTo("TXN_FAILURE");
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultCode())
                .as("resultCode mismatch")
                .isEqualTo("1104");
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultMsg())
                .as("resultMsg mismatch")
                .isEqualToIgnoringCase("Validation failed");

    }

    @DataProvider
    public Object[][] getFreqValue() {
        Object[][] freqvalue = new Object[8][3];
        freqvalue[0][0] = "1";
        freqvalue[0][1] = "MONTH";
        freqvalue[0][2] = "Every month";
        freqvalue[1][0] = "1";
        freqvalue[1][1] = "YEAR";
        freqvalue[1][2] = "Every year";
        freqvalue[2][0] = "2";
        freqvalue[2][1] = "DAY";
        freqvalue[2][2] = "Every 2 days";
        freqvalue[3][0] = "3";
        freqvalue[3][1] = "WEEK";
        freqvalue[3][2] = "Every 3 weeks";
        freqvalue[4][0] = "1";
        freqvalue[4][1] = "QUARTER";
        freqvalue[4][2] = "Every quarter";
        freqvalue[5][0] = "1";
        freqvalue[5][1] = "BI_MONTHLY";
        freqvalue[5][2] = "Every 2 months";
        freqvalue[6][0] = "1";
        freqvalue[6][1] = "SEMI_ANNUALLY";
        freqvalue[6][2] = "Every half-year";
        freqvalue[7][0] = "1";
        freqvalue[7][1] = "FORTNIGHT";
        freqvalue[7][2] = "Every fortnight";


        return freqvalue;
    }

    @Owner(SRISHTI)
    @Feature("PGP-26593")
    @Test(description = "Validation of Subs frequency details in fpo response", dataProvider = "getFreqValue")
    public void TC_ValidateSubsFrequencyInFpo(String frequency, String freqUnit, String result) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)     //Initiate subs request
                .setTxnValue("1")
                .setSubscriptionPaymentMode("")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency(frequency)
                .setSubscriptionFrequencyUnit(freqUnit)
                .setSubscriptionGraceDays("1")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);        //Initiate create subs first request
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();

        //FetchPayment Options V2

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken)
                .build();

        FetchPaymentOptionV2 fpoV2 = new FetchPaymentOptionV2(merchant.getId(), initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);

        Response fetchPaymentOptionsJsonv2 = fpoV2.execute();


        fetchPaymentOptionsJsonv2.then().assertThat().statusCode(200).
                body("body.subscriptionDetail.details.frequencyKey", equalTo(result));
        // assertEquals(frequencyKey, "");
    }




    @Feature("PGP-29610")
    @Epic(Constants.Sprint.SPRINT36_3)
    @Owner(ESHANI)
    @Test(description = "Verify Native txn fails for DAY subscription with GRACE PERIOD > FREQ * FREQ_UNIT")
    public void verifyNativeDAYsubsWithMoreGraceperiod() throws Exception {
        User user = userManager.getForRead(Label.BASIC);

        Random ran= new Random();
        int freq= ran.nextInt(10) +1;
        String frequency= String.valueOf(freq);
        String grace_days= String.valueOf(freq+1);

        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency(frequency)
                .setSubscriptionFrequencyUnit("DAY")
                .setSubscriptionGraceDays(grace_days)
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
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

    @Feature("PGP-29610")
    @Epic(Constants.Sprint.SPRINT36_3)
    @Parameters({"isNativePlus"})
    @Owner(ESHANI)
    @Test(description = "Verify Native txn success for WEEK subscription with GRACE PERIOD < FREQ * FREQ_UNIT")
    public void verifyNativeWEEKsubsWithLessGraceperiod(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForRead(Label.BASIC);

        Random ran= new Random();
        int freq= ran.nextInt(10) +1;
        String frequency= String.valueOf(freq);
        String grace_days= String.valueOf((freq*7)-1);

        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency(frequency)
                .setSubscriptionFrequencyUnit("WEEK")
                .setSubscriptionGraceDays(grace_days)
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);        //Initiate create subs first request
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        execute_validateFetchPaymentOption(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), PayMethodType.CREDIT_CARD.toString(), false);
        execute_validateActiveBinDetail(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), CC_BIN, true);
        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(merchant, initTxnDTO.getBody().getOrderId(),
                txnToken, PayMethodType.CREDIT_CARD, subsId)
                .setAUTH_MODE("3D")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);             // payment through created subscription request
        PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnType("SALE")
                .validateGatewayName("HDFC")
                .validateRespCode("01")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateSubsid(subsId)
                .AssertAll();

        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("subcription_start_date", subsId, orderDTO.getORDER_ID()))
                .as("subcription_start_date not available")
                .contains(CommonHelpers.getDate().toString());
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("acquirement_id", subsId, orderDTO.getORDER_ID()))
                .as("acquirement_id is null")
                .isNotNull();
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("status", subsId, orderDTO.getORDER_ID()))
                .as("status mismatch")
                .isEqualTo("ACTIVE");
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("saved_card_id", subsId, orderDTO.getORDER_ID()))
                .as("savedCardId is null")
                .isNotNull();
        NativeHelpers.assertRedisKeysNotPresent(txnToken);

    }



    @Feature("PGP-29610")
    @Epic(Constants.Sprint.SPRINT36_3)
    @Owner(ESHANI)
    @Test(description = "Verify Native txn fails for MONTH subscription with GRACE PERIOD > FREQ * FREQ_UNIT")
    public void verifyNativeMONTHsubsWithMoreGraceperiod() throws Exception {
        User user = userManager.getForRead(Label.BASIC);

        Random ran= new Random();
        int freq= ran.nextInt(10) +1;
        String frequency= String.valueOf(freq);
        String grace_days= String.valueOf((freq*30)+1);

        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency(frequency)
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays(grace_days)
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
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
    //Deprecated CC/DC Flow after SI Hub Release
/*    @Feature("PGP-29610")
    @Epic(Constants.Sprint.SPRINT36_3)
    @Parameters({"isNativePlus"})
    @Owner(ESHANI)
    @Test(enabled = false, description = "Verify Native txn success for YEAR subscription with GRACE PERIOD < FREQ * FREQ_UNIT") */
    public void verifyNativeYEARsubsWithLessGraceperiod(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForRead(Label.BASIC);

        Random ran= new Random();
        int freq= ran.nextInt(10) +1;
        String frequency= String.valueOf(freq);
        String grace_days= String.valueOf((freq*365)-1);

        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency(frequency)
                .setSubscriptionFrequencyUnit("YEAR")
                .setSubscriptionGraceDays(grace_days)
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);        //Initiate create subs first request
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        execute_validateFetchPaymentOption(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), PayMethodType.CREDIT_CARD.toString(), false);
        execute_validateActiveBinDetail(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), CC_BIN, true);
        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(merchant, initTxnDTO.getBody().getOrderId(),
                txnToken, PayMethodType.CREDIT_CARD, subsId)
                .setAUTH_MODE("3D")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);             // payment through created subscription request
        PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnType("SALE")
                .validateGatewayName("HDFC")
                .validateRespCode("01")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateSubsid(subsId)
                .AssertAll();

        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("subcription_start_date", subsId, orderDTO.getORDER_ID()))
                .as("subcription_start_date not available")
                .contains(CommonHelpers.getDate().toString());
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("acquirement_id", subsId, orderDTO.getORDER_ID()))
                .as("acquirement_id is null")
                .isNotNull();
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("status", subsId, orderDTO.getORDER_ID()))
                .as("status mismatch")
                .isEqualTo("ACTIVE");
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("saved_card_id", subsId, orderDTO.getORDER_ID()))
                .as("savedCardId is null")
                .isNotNull();
        NativeHelpers.assertRedisKeysNotPresent(txnToken);

    }

    @Owner(ABHAY)
    @Feature("PGP-27607")
    @Test(description = "Verify isAddNPay flag when create subscription transaction is done through UPI and payment flow is ADDANDPAY")
    public void VerifyisAddNPayFlagForCreateSubsUsingAddnPayThroughUPI() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PPI;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("1000")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("1")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = responseDTO.getBody().getTxnToken();
        String subsId = responseDTO.getBody().getSubscriptionId();
        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(merchant, initTxnDTO.getBody().getOrderId(), txnToken, PayMethodType.UPI, subsId)
                .setPaymentFlow("ADDANDPAY")
                .setAUTH_MODE("USRPWD")
                .setPayerAccount(new PaymentDTO().getVpa())
                .build();
        checkoutPage.createNativeOrder(orderDTO, false);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateMid(merchant.getId())
                .validateCurrency("INR")
                .validateRespCode("01")
                .validatePaymentMode("PPI")
                .validateBankName("WALLET")
                .validateStatus("TXN_SUCCESS")
                .validateRespMsg("Txn Success")
                .assertAll();
        String tsnId = responsePage.textTxnID().getText();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateStatus("TXN_SUCCESS")
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnType("SALE")
                .validateGatewayName("WALLET")
                .validateRespCode("01")
                .validateBankName("WALLET")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .validateSubsid(subsId)
                .AssertAll();
        String grepcmd = "grep \"" + tsnId + "\" /paytm/logs/instaproxy.log | " +
                 "grep \"extendInfo\"";
        String theiaFacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.INSTAPROXY, grepcmd);
        String [] ar = theiaFacadeLogs.split("passThroughExtendInfo");
        String [] ar2 = ar[1].split("\"");
        String decodedString = PGPHelpers.Base64Decode(ar2[2]);
        Assertions.assertThat(decodedString).contains("\"isAddNPay\":\"true\"");
    }

    @Owner(ABHAY)
    @Feature("PGP-29588")
    @Test(description = "Verify BANK MANDATE PaymentMode is not supported for FORTNIGHT frequency")
    public void VerifyBankMandateNotSupportedForFortnightFrequency() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.UPI_NATIVE_SUBS;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("BANK_MANDATE")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("FORTNIGHT")
                .setSubscriptionGraceDays("1")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();

        InitTxnResponseDTO initTxnResponseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        Assertions.assertThat(initTxnResponseDTO.getBody().getResultInfo().getResultStatus()).as("result status should be TXN_FAILURE").isEqualTo("TXN_FAILURE");
        Assertions.assertThat(initTxnResponseDTO.getBody().getResultInfo().getResultMsg()).as("result msg should be BANK MANDATE is not supported for FORTNIGHT frequency").isEqualTo("BANK MANDATE is not supported for FORTNIGHT frequency");
    }

    @Owner(ABHAY)
    @Feature("PGP-29588")
    @Test(description = "Verify Successful PPBL Subscription Txn when SubscriptionFrequencyUnit is FORTNIGHT.")
    public void VerifySuccessfulSubsPPBLTxnForFreqFortnight() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.BANK_MANDATE;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("PPBL")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("FORTNIGHT")
                .setSubscriptionGraceDays("1")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        PaymentDTO paymentDTO = new PaymentDTO();
        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(merchant, initTxnDTO.getBody().getOrderId(), txnToken, PayMethodType.PPBL, subsId)
                .setPaymentFlow("NONE")
                .setAUTH_MODE("3D")
                .setMpin(paymentDTO.getPasscode())
                .build();
        checkoutPage.createNativeOrder(orderDTO, false);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateMid(merchant.getId())
                .validateCurrency("INR")
                .validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .validateRespMsg("Txn Success")
                .validateGatewayName("PPBL")
                .validateBankName("PPBL")
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("PPBL")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("NB")
                .validateSubsid(subsId)
                .AssertAll();
    }

    @Owner(ABHAY)
    @Feature("PGP-29588")
    @Test(description = "Verify that subscription frequency is correctly displayed in FPO response when subscriptionFrequency ='FORTNIGHT' ")
    public void VerifySubsDetailsInFPOForSubsFrequencyFortnight() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.UPI_NATIVE_SUBS;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("FORTNIGHT")
                .setSubscriptionGraceDays("1")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchant.getId(), initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        Response response = fetchPaymentOption.execute();
        Assertions.assertThat(response.jsonPath().getString("body.subscriptionDetail.details.frequencyKey")).isEqualTo("Every fortnight");
    }


    @Owner(ABHAY)
    @Feature("PGP-29588")
    @Test(description = "Verify successfull transaction when" +
            "requestType=NATIVE_MF_SIP, Paymentmode=UPI, freq=FORTNIGHT")
    public void SuccessfulNativeMfSipWithFreqFORTNIGHT() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_MUTUAL_MF;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("FORTNIGHT")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_MF_SIP")
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
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateMid(merchant.getId())
                .validateCurrency("INR")
                .validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .validateRespMsg("Txn Success")
                .validateGatewayName("PPBLC")
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("PPBLC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("UPI")
                .validateSubsid(subsId)
                .AssertAll();
    }

    @Owner(ABHAY)
    @Feature("PGP-29589")
    @Test(description = "Verify Flexi:true flag in subscription_contract_v2 when subscription frequencyUnit is Month")
    public void VerifyFlexiTrueInSubsContractV2WhenFrequencyIsMonth() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.FLEXI_SUBSCRIPTION;
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
                .setRequestType("NATIVE_SUBSCRIPTION")
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
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateMid(merchant.getId())
                .validateCurrency("INR")
                .validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .validateRespMsg("Txn Success")
                .validateGatewayName("PPBLC")
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("PPBLC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("UPI")
                .validateSubsid(subsId)
                .AssertAll();
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("metadata", subsId, orderDTO.getORDER_ID()))
                .as("flexi flag is expected to be true")
                .contains("\"flexi\":true");
    }

    @Owner(ABHAY)
    @Feature("PGP-29589")
    @Test(description = "Verify Flexi:true flag in subscription_contract_v2 when subscription frequencyUnit is YEAR")
    public void VerifyFlexiTrueInSubsContractV2WhenFrequencyIsYear() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.FLEXI_SUBSCRIPTION;
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
                .setRequestType("NATIVE_SUBSCRIPTION")
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
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateMid(merchant.getId())
                .validateCurrency("INR")
                .validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .validateRespMsg("Txn Success")
                .validateGatewayName("PPBLC")
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("PPBLC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("UPI")
                .validateSubsid(subsId)
                .AssertAll();
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("metadata", subsId, orderDTO.getORDER_ID()))
                .as("flexi flag is expected to be true")
                .contains("\"flexi\":true");
    }

    @Owner(ABHAY)
    @Feature("PGP-29589")
    @Test(description = "Verify Flexi:true flag in subscription_contract_v2 when subscription frequencyUnit is DAY")
    public void VerifyFlexiTrueInSubsContractV2WhenFrequencyIsDay() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.FLEXI_SUBSCRIPTION;
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
                .setRequestType("NATIVE_SUBSCRIPTION")
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
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateMid(merchant.getId())
                .validateCurrency("INR")
                .validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .validateRespMsg("Txn Success")
                .validateGatewayName("PPBLC")
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("PPBLC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("UPI")
                .validateSubsid(subsId)
                .AssertAll();
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("metadata", subsId, orderDTO.getORDER_ID()))
                .as("flexi flag is expected to be true")
                .contains("\"flexi\":true");
    }

    @Owner(ABHAY)
    @Feature("PGP-29589")
    @Test(description = "Verify Flexi:true flag in subscription_contract_v2 when subscription frequencyUnit is WEEK")
    public void VerifyFlexiTrueInSubsContractV2WhenFrequencyIsWeek() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.FLEXI_SUBSCRIPTION;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("100")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("WEEK")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_SUBSCRIPTION")
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
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateMid(merchant.getId())
                .validateCurrency("INR")
                .validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .validateRespMsg("Txn Success")
                .validateGatewayName("PPBLC")
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("PPBLC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("UPI")
                .validateSubsid(subsId)
                .AssertAll();
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("metadata", subsId, orderDTO.getORDER_ID()))
                .as("flexi flag is expected to be true")
                .contains("\"flexi\":true");
    }

    @Owner(ABHAY)
    @Feature("PGP-29589")
    @Test(description = "Verify Flexi:true flag in subscription_contract_v2 when subscription frequencyUnit is BI_MonthLY")
    public void VerifyFlexiTrueInSubsContractV2WhenFrequencyIsBiMonthly() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.FLEXI_SUBSCRIPTION;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
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
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateMid(merchant.getId())
                .validateCurrency("INR")
                .validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .validateRespMsg("Txn Success")
                .validateGatewayName("PPBLC")
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("PPBLC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("UPI")
                .validateSubsid(subsId)
                .AssertAll();
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("metadata", subsId, orderDTO.getORDER_ID()))
                .as("flexi flag is expected to be true")
                .contains("\"flexi\":true");
    }

    @Owner(ABHAY)
    @Feature("PGP-29589")
    @Test(description = "Verify Flexi:true flag in subscription_contract_v2 when subscription frequencyUnit is QUARTER")
    public void VerifyFlexiTrueInSubsContractV2WhenFrequencyIsQuarter() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.FLEXI_SUBSCRIPTION;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
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
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateMid(merchant.getId())
                .validateCurrency("INR")
                .validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .validateRespMsg("Txn Success")
                .validateGatewayName("PPBLC")
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("PPBLC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("UPI")
                .validateSubsid(subsId)
                .AssertAll();
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("metadata", subsId, orderDTO.getORDER_ID()))
                .as("flexi flag is expected to be true")
                .contains("\"flexi\":true");
    }

    @Owner(ABHAY)
    @Feature("PGP-29589")
    @Test(description = "Verify Flexi:true flag in subscription_contract_v2 when subscription frequencyUnit is SEMI_ANNUALLY")
    public void VerifyFlexiTrueInSubsContractV2WhenFrequencyIsSemiAnually() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.FLEXI_SUBSCRIPTION;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("100")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("SEMI_ANNUALLY")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_SUBSCRIPTION")
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
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateMid(merchant.getId())
                .validateCurrency("INR")
                .validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .validateRespMsg("Txn Success")
                .validateGatewayName("PPBLC")
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("PPBLC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("UPI")
                .validateSubsid(subsId)
                .AssertAll();
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("metadata", subsId, orderDTO.getORDER_ID()))
                .as("flexi flag is expected to be true")
                .contains("\"flexi\":true");
    }

    @Owner(ABHAY)
    @Feature("PGP-29589")
    @Test(description = "Verify Flexi:true flag in subscription_contract_v2 when subscription frequencyUnit is FORTNIGHT")
    public void VerifyFlexiTrueInSubsContractV2WhenFrequencyIsFortnight() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.FLEXI_SUBSCRIPTION;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("100")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("FORTNIGHT")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_SUBSCRIPTION")
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
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateMid(merchant.getId())
                .validateCurrency("INR")
                .validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .validateRespMsg("Txn Success")
                .validateGatewayName("PPBLC")
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("PPBLC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("UPI")
                .validateSubsid(subsId)
                .AssertAll();
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("metadata", subsId, orderDTO.getORDER_ID()))
                .as("flexi flag is expected to be true")
                .contains("\"flexi\":true");
    }

    @Owner(PUSHKAL)
    @Feature("PGP-31174")
    @Test(description = "Verify isAutoRefund is true in FPO response of subscription created for merchant whose AutoRefund is enabled")
    public void verifyFpoResponseForAutoRefundMerchant() {
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_AUTO_REFUND;
        String mid = merchant.getId();
        String merchantKey = merchant.getKey();

        //calling create subscription api
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(mid, merchantKey, "")
                .setTxnValue("0")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("100")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("1")
                .setSubscriptionRetryCount("2")
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setValidateAccountNumber(null)
                .setAllowUnverifiedAccount(null)
                .setPayerAccount(null)
                .setIsNativeAddMoney(null)
                .setCallbackUrl(null)
                .build();
        InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = responseDTO.getBody().getTxnToken();

        //calling fetch payment options api
        FetchPaymentOptResponseDTO fetchPaymentOptResponseDTO = NativeHelpers.fetchPaymentOptionResponse(txnToken, merchant.getId(), initTxnDTO.orderFromBody());
        Boolean result = fetchPaymentOptResponseDTO.getBody().getSubscriptionDetail().getIsAutoRefund();
        Assert.assertTrue(result);
    }
    @Owner(AJEESH)
    @Feature("PGP-35618")
    @Test(description = "Verify that User is Navigated to Dynamic URL and DB value is also updated")
    public void PGP_35618_VerifythatUserisNavigatedtoDynamicURL() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        String callBackUrl = "www.google.com";
        Constants.MerchantType merchant = Constants.MerchantType.BANK_MANDATE;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("BANK_MANDATE")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("1")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setMandateAccountDetails(new MandateAccountDetails())
                .setCallbackUrl(callBackUrl)
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        execute_validateFetchPaymentOption(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), "BANK_MANDATE", false);

        OrderDTO orderDTO = new OrderFactory.BankMandate(merchant,txnToken,initTxnDTO.orderFromBody(),subsId)
                .build();
        checkoutPage.createNativeOrder(orderDTO, false);
        Assertions.assertThat((DriverManager.getCurrentWebDriver().getCurrentUrl()).contains(callBackUrl));
        String query = "SELECT callback_url  FROM PGPDB.bank_mandate_info bmi WHERE subscription_id ='" + subsId + "'";
        Assertions.assertThat((DatabaseUtil.getInstance().executeSelectQuery(LocalConfig.PGP_DB_CONNECTION_URL, query).toString()).contains(callBackUrl));

    }

    @Owner(ASHISH_JASWAL)
    @Feature("PGP-37658")
    @Parameters({"isNativePlus"})
    @Test(description = "Verify that User is Navigated to Dynamic URL for Bank Mandate Txn")
    public void PGP_37658_VerifythatUserisNavigatedtoDynamicURL(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForRead(Label.LOGIN);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        String callBackUrl = "www.google.com";
        Constants.MerchantType merchant = Constants.MerchantType.BANK_MANDATE_BM;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setChannelId("WAP")
                .setWebsiteName("retail")
                .setSubscriptionPaymentMode("BANK_MANDATE")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("1")
                .setAccountNumber("915445500424")
                .setAccountHolderName("Akshat Sharma")
                .setIfscCode("PYTM0000001")
                .setAccountType("Savings")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setCallbackUrl(callBackUrl)
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        execute_validateFetchPaymentOption(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), "BANK_MANDATE", false);

        OrderDTO orderDTO = new OrderFactory.BankMandate(merchant,txnToken,initTxnDTO.orderFromBody(),subsId)
                .build();
        checkoutPage.createNativeOrder(orderDTO, false);
        checkoutPage.waitUntilLoads();

        Assertions.assertThat((DriverManager.getCurrentWebDriver().getCurrentUrl()).contains(callBackUrl));

    }

    @Owner(ASHISH_JASWAL)
    @Feature("PGP-37658")
    @Parameters({"isNativePlus"})
    @Test(description = "Verify that User is Navigated to BM URL for Bank Mandate Txn")
    public void PGP_37658_VerifythatUserisNavigatedtoBMURL(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForRead(Label.LOGIN);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.BANK_MANDATE_BM;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("BANK_MANDATE")
                .setChannelId("WEB")
                .setWebsiteName("retail")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("0")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("1")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setAccountNumber("915445500424")
                .setAccountHolderName("Akshat Sharma")
                .setIfscCode("PYTM0000001")
                .setAccountType("Savings")
                .setCallbackUrl("")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        execute_validateFetchPaymentOption(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), "BANK_MANDATE", false);

        OrderDTO orderDTO = new OrderFactory.BankMandate(merchant,txnToken,initTxnDTO.orderFromBody(),subsId)
                .build();
        checkoutPage.createNativeOrder(orderDTO, false);
        checkoutPage.waitUntilLoads();

        Assertions.assertThat((DriverManager.getCurrentWebDriver().getCurrentUrl()).contains("https://www.spacex.com/"));
    }
    @Owner(Amanpreet)
    @Feature("PGP-39530")
    @Parameters({"isNativePlus"})
    @Test(description = "Verify that CC/DC as paymode option is displayed on cashier page for subscription transaction for grace days=0")
    public void PGP_39530_TC_01_Subs_GD_CC(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForRead(Label.LOGIN);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.SIHUB_Subs;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("")
                .setChannelId("WEB")
                .setWebsiteName("retail")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        FetchPaymentOptResponseDTO fetchPaymentOptResponseDTO = NativeHelpers.fetchPaymentOptionResponse(txnToken, merchant.getId(), initTxnDTO.orderFromBody());
        boolean paymodeCC = NativeHelpers.isFetchPaymentOptionStatusMatched(fetchPaymentOptResponseDTO, "CREDIT_CARD", false);
        boolean paymodeDC = NativeHelpers.isFetchPaymentOptionStatusMatched(fetchPaymentOptResponseDTO, "DEBIT_CARD", false);
        Assertions.assertThat(paymodeCC).isTrue();
        Assertions.assertThat(paymodeDC).isTrue();
    }
    @Owner(Amanpreet)
    @Feature("PGP-39530")
    @Parameters({"isNativePlus"})
    @Test(description = "Verify that CC/DC as paymode option is displayed on cashier page for subscription transaction for grace days=3")
    public void PGP_39530_TC_02_Subs_GD_CC(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForRead(Label.LOGIN);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.SIHUB_Subs;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("")
                .setChannelId("WEB")
                .setWebsiteName("retail")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("3")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        FetchPaymentOptResponseDTO fetchPaymentOptResponseDTO = NativeHelpers.fetchPaymentOptionResponse(txnToken, merchant.getId(), initTxnDTO.orderFromBody());
        boolean paymodeCC = NativeHelpers.isFetchPaymentOptionStatusMatched(fetchPaymentOptResponseDTO, "CREDIT_CARD", false);
        boolean paymodeDC = NativeHelpers.isFetchPaymentOptionStatusMatched(fetchPaymentOptResponseDTO, "DEBIT_CARD", false);
        Assertions.assertThat(paymodeCC).isTrue();
        Assertions.assertThat(paymodeDC).isTrue();

    }
    @Owner(Amanpreet)
    @Feature("PGP-39530")
    @Parameters({"isNativePlus"})
    @Test(description = "Verify that CC/DC as paymode option is displayed on cashier page for subscription transaction for grace days=4")
    public void PGP_39530_TC_03_Subs_GD_CC(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForRead(Label.LOGIN);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.SIHUB_Subs;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("")
                .setChannelId("WEB")
                .setWebsiteName("retail")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("4")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        FetchPaymentOptResponseDTO fetchPaymentOptResponseDTO = NativeHelpers.fetchPaymentOptionResponse(txnToken, merchant.getId(), initTxnDTO.orderFromBody());
        boolean paymodeCC = NativeHelpers.isFetchPaymentOptionStatusMatched(fetchPaymentOptResponseDTO, "CREDIT_CARD", false);
        boolean paymodeDC = NativeHelpers.isFetchPaymentOptionStatusMatched(fetchPaymentOptResponseDTO, "DEBIT_CARD", false);
        Assertions.assertThat(paymodeCC).isFalse();
        Assertions.assertThat(paymodeDC).isFalse();
    }

    @Feature("PGP-41004")
    @Owner(Constants.Owner.TAMANA_TATHAN)
    @Test(description = "Validate FPO response that bank account feature list is coming or not , when request type = SUBSCRIBE")
    public void TC01_ValidateBankAccountFeatureList() throws Exception {
        User user = userManager.getForRead(Label.ADVANCEDEPOSITLOWBALANCEUSER);
        Constants.MerchantType merchant = Constants.MerchantType.QR_Subscription_Merchant;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(merchant.getId(), merchant.getKey(), user.ssoToken())
                .setTxnValue("2")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("6")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("0")
                .setSubscriptionEnableRetry("0")
                .setRequestType("SUBSCRIBE")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String orderId = initTxnDTO.getBody().getOrderId();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(),
                orderId, fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        if(fetchPaymentOptionsJson.getString("body").contains("bankAccountFeatureList")){
            Assert.assertTrue(fetchPaymentOptionsJson.getString("body.groupedMerchantPayOption.upiProfile.respDetails.profileDetail.bankAccounts.bankAccountFeatureList").contains("OTM"), "Validating the FPO response , bank account feature list is coming or not ");
            Assert.assertTrue(fetchPaymentOptionsJson.getString("body.merchantPayOption.upiProfile.respDetails.profileDetail.bankAccounts.bankAccountFeatureList").contains("OTM"), "Validating the FPO response , bank account feature list is coming or not ");
         }
    }

    @Feature("PGP-41004")
    @Owner(Constants.Owner.TAMANA_TATHAN)
    @Test(description = "Validate FPO response that bank account feature list is coming or not , when request type = NATIVE_SUBSCRIPTION")
    public void TC02_ValidateBankAccountFeatureList_ReqType_NATIVE_SUBSCRIPTION() throws Exception {
        User user = userManager.getForRead(Label.ADVANCEDEPOSITLOWBALANCEUSER);
        Constants.MerchantType merchant = Constants.MerchantType.QR_Subscription_Merchant;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(merchant.getId(), merchant.getKey(), user.ssoToken())
                .setTxnValue("2")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("6")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("0")
                .setSubscriptionEnableRetry("0")
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String orderId = initTxnDTO.getBody().getOrderId();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(),
                orderId, fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        if (fetchPaymentOptionsJson.getString("body").contains("bankAccountFeatureList")) {
            Assert.assertTrue(fetchPaymentOptionsJson.getString("body.groupedMerchantPayOption.upiProfile.respDetails.profileDetail.bankAccounts.bankAccountFeatureList").contains("OTM"), "Validating the FPO response , bank account feature list is coming or not ");
            Assert.assertTrue(fetchPaymentOptionsJson.getString("body.merchantPayOption.upiProfile.respDetails.profileDetail.bankAccounts.bankAccountFeatureList").contains("OTM"), "Validating the FPO response , bank account feature list is coming or not ");
        }
    }

    @Feature("PGP-41004")
    @Owner(Constants.Owner.TAMANA_TATHAN)
    @Test(description = "Validate FPO response that bank account feature list is coming or not , when request type = NATIVE_SUBSCRIPTION_PAY")
    public void TC03_ValidateBankAccountFeatureList_ReqType_NATIVE_SUBSCRIPTION_PAY() throws Exception {
        User user = userManager.getForRead(Label.ADVANCEDEPOSITLOWBALANCEUSER);
        Constants.MerchantType merchant = Constants.MerchantType.QR_Subscription_Merchant;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(merchant.getId(), merchant.getKey(), user.ssoToken())
                .setTxnValue("2")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("6")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("0")
                .setSubscriptionEnableRetry("0")
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String orderId = initTxnDTO.getBody().getOrderId();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(),
                orderId, fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        String lpvLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia,orderId,"/ && /LITEPAYVIEW_CONSULT/ && /REQUEST/");

        if (fetchPaymentOptionsJson.getString("body").contains("bankAccountFeatureList")) {
            Assert.assertTrue(fetchPaymentOptionsJson.getString("body.groupedMerchantPayOption.upiProfile.respDetails.profileDetail.bankAccounts.bankAccountFeatureList").contains("OTM"), "Validating the FPO response , bank account feature list is coming or not ");
            Assert.assertTrue(fetchPaymentOptionsJson.getString("body.merchantPayOption.upiProfile.respDetails.profileDetail.bankAccounts.bankAccountFeatureList").contains("OTM"), "Validating the FPO response , bank account feature list is coming or not ");
        }
    }

    @Feature("PGP-41004")
    @Owner(Constants.Owner.TAMANA_TATHAN)
    @Test(description = "Validate FPO response that bank account feature list is coming or not , when request type = NATIVE_MF_SIP")
    public void TC04_ValidateBankAccountFeatureList_ReqType_NATIVE_MF_SIP() throws Exception {
        User user = userManager.getForRead(Label.ADVANCEDEPOSITLOWBALANCEUSER);
        Constants.MerchantType merchant = Constants.MerchantType.MF_SIP_NEW_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(merchant.getId(), merchant.getKey(), user.ssoToken())
                .setTxnValue("2")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("6")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("0")
                .setSubscriptionEnableRetry("0")
                .setRequestType("NATIVE_MF_SIP")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String orderId = initTxnDTO.getBody().getOrderId();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(),
                orderId, fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        if (fetchPaymentOptionsJson.getString("body").contains("bankAccountFeatureList")) {
            Assert.assertTrue(fetchPaymentOptionsJson.getString("body.groupedMerchantPayOption.upiProfile.respDetails.profileDetail.bankAccounts.bankAccountFeatureList").contains("OTM"), "Validating the FPO response , bank account feature list is coming or not ");
            Assert.assertTrue(fetchPaymentOptionsJson.getString("body.merchantPayOption.upiProfile.respDetails.profileDetail.bankAccounts.bankAccountFeatureList").contains("OTM"), "Validating the FPO response , bank account feature list is coming or not ");
        }
    }

    @Feature("PGP-41004")
    @Owner(Constants.Owner.TAMANA_TATHAN)
    @Test(description = "Validate FPO response that bank account feature list is coming or not , when request type = NATIVE_MF_SIP_PAY")
    public void TC05_ValidateBankAccountFeatureList_ReqType_NATIVE_MF_SIP_PAY() throws Exception {
        User user = userManager.getForRead(Label.ADVANCEDEPOSITLOWBALANCEUSER);
        Constants.MerchantType merchant = Constants.MerchantType.MF_SIP_NEW_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(merchant.getId(), merchant.getKey(), user.ssoToken())
                .setTxnValue("2")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("6")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("0")
                .setSubscriptionEnableRetry("0")
                .setRequestType("NATIVE_MF_SIP")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String orderId = initTxnDTO.getBody().getOrderId();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(),
                orderId, fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        String lpvLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia,orderId,"/ && /LITEPAYVIEW_CONSULT/ && /REQUEST/");

        if (fetchPaymentOptionsJson.getString("body").contains("bankAccountFeatureList")) {
            Assert.assertTrue(fetchPaymentOptionsJson.getString("body.groupedMerchantPayOption.upiProfile.respDetails.profileDetail.bankAccounts.bankAccountFeatureList").contains("OTM"), "Validating the FPO response , bank account feature list is coming or not ");
            Assert.assertTrue(fetchPaymentOptionsJson.getString("body.merchantPayOption.upiProfile.respDetails.profileDetail.bankAccounts.bankAccountFeatureList").contains("OTM"), "Validating the FPO response , bank account feature list is coming or not ");
        }
    }

    @Feature("PGP-41004")
    @Owner(Constants.Owner.TAMANA_TATHAN)
    @Test(description = "Validate the response when request type is null")
    public void TC06_ValidateBankAccountFeatureList_ReqType_NULL() throws Exception {
        User user = userManager.getForRead(Label.ADVANCEDEPOSITLOWBALANCEUSER);
        Constants.MerchantType merchant = Constants.MerchantType.MF_SIP_NEW_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(merchant.getId(), merchant.getKey(), user.ssoToken())
                .setTxnValue("2")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("6")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("0")
                .setSubscriptionEnableRetry("0")
                .setRequestType(" ")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .build();
        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        Assert.assertTrue(responseDTO.getBody().getResultInfo().getResultStatus().equals("U"), "Result Status validation");
        Assert.assertTrue(responseDTO.getBody().getResultInfo().getResultCode().equals("00000900"), "Result Code validation");
        Assert.assertTrue(responseDTO.getBody().getResultInfo().getResultMsg().equals("System error"), "Result Message validation");
    }

    @Feature("PGP-41004")
    @Owner(Constants.Owner.TAMANA_TATHAN)
    @Test(description = "Validate the response when request type is NATIVE_SUBSCRIPTION for NATIVE_MF_SIP flow")
    public void TC07_ValidateResponseWhenReuqestTypeisNATIVE_SUBSCRIPTION() throws Exception {
        User user = userManager.getForRead(Label.ADVANCEDEPOSITLOWBALANCEUSER);
        Constants.MerchantType merchant = Constants.MerchantType.PG2_SUBS_MFSIP;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(merchant.getId(), merchant.getKey(), user.ssoToken())
                .setTxnValue("2")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("6")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("0")
                .setSubscriptionEnableRetry("0")
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .build();
        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        Assert.assertTrue(responseDTO.getBody().getResultInfo().getResultStatus().equals("F"), "Result Status validation");
        Assert.assertTrue(responseDTO.getBody().getResultInfo().getResultCode().equals("302"), "Result Code validation");
        Assert.assertTrue(responseDTO.getBody().getResultInfo().getResultMsg().equals("Invalid Request Type"), "Result Message validation");
    }

    @Feature("PGP-41005")
    @Owner(Constants.Owner.TAMANA_TATHAN)
    @Test(description = "Validate FPO response when accountnumber is passed in create request then only that bank accounts should come in FPO response")
    public void TC01_CheckFPOResponse_AccountNoPassed() throws Exception {
        User user = userManager.getForRead(Label.ADVANCEDEPOSITLOWBALANCEUSER);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_MUTUAL_MF;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setAccountNumber("XXXXXXXX6101")
                .setTxnValue("1")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("5")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("0")
                .setSubscriptionEnableRetry("0")
                .setRequestType("NATIVE_MF_SIP")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String orderId = initTxnDTO.getBody().getOrderId();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(),
                orderId, fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        if(fetchPaymentOptionsJson.getString("body").contains("maskedAccountNumber")){
            Assert.assertTrue(fetchPaymentOptionsJson.getString("body.groupedMerchantPayOption.upiProfile.respDetails.profileDetail.bankAccounts").contains("maskedAccountNumber:XXXXXXXX6101"), "Validating the account number should be passed as was sent in the request");
            Assert.assertTrue(fetchPaymentOptionsJson.getString("body.merchantPayOption.upiProfile.respDetails.profileDetail.bankAccounts").contains("maskedAccountNumber:XXXXXXXX6101"), "Validating the account number should be passed as was sent in the request");
            Assert.assertFalse(fetchPaymentOptionsJson.getString("body.groupedMerchantPayOption.upiProfile.respDetails.profileDetail.bankAccounts").contains("maskedAccountNumber:XXXXXXXXXX4872"), "Validating that the other account number is not passed in FPO response");
            Assert.assertFalse(fetchPaymentOptionsJson.getString("body.merchantPayOption.upiProfile.respDetails.profileDetail.bankAccounts").contains("maskedAccountNumber:XXXXXXXXXX4872"), "Validating that the other account number is not passed in FPO response");
        }
    }
    @Feature("PGP-41005")
    @Owner(Constants.Owner.TAMANA_TATHAN)
    @Test(description = "Validate FPO response when accountnumber is NOT passed in create request , All the accounts will passed in FPO")
    public void TC02_CheckFPOResponse_AccountNotPassed() throws Exception {
        User user = userManager.getForRead(Label.ADVANCEDEPOSITLOWBALANCEUSER);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_MUTUAL_MF;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("5")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("0")
                .setSubscriptionEnableRetry("0")
                .setRequestType("NATIVE_MF_SIP")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String orderId = initTxnDTO.getBody().getOrderId();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(),
                orderId, fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        if(fetchPaymentOptionsJson.getString("body").contains("maskedAccountNumber")){
            Assert.assertTrue(fetchPaymentOptionsJson.getString("body.groupedMerchantPayOption.upiProfile.respDetails.profileDetail.bankAccounts").contains("maskedAccountNumber:XXXXXXXX6101"), "Validating the ALL Account Number is present in FPO, when no accNumber is sent in create Req");
            Assert.assertTrue(fetchPaymentOptionsJson.getString("body.groupedMerchantPayOption.upiProfile.respDetails.profileDetail.bankAccounts").contains("maskedAccountNumber:XXXXXXXXXX4872"), "Validating the ALL Account Number is present in FPO, when no accNumber is sent in create Req");
            Assert.assertTrue(fetchPaymentOptionsJson.getString("body.merchantPayOption.upiProfile.respDetails.profileDetail.bankAccounts").contains("maskedAccountNumber:XXXXXXXX6101"), "Validating the ALL Account Number is present in FPO, when no accNumber is sent in create Req");
            Assert.assertTrue(fetchPaymentOptionsJson.getString("body.merchantPayOption.upiProfile.respDetails.profileDetail.bankAccounts").contains("maskedAccountNumber:XXXXXXXXXX4872"), "Validating the ALL Account Number is present in FPO, when no accNumber is sent in create Req");
        }
    }
    @Feature("PGP-41005")
    @Owner(Constants.Owner.TAMANA_TATHAN)
    @Test(description = "Validate FPO response when accountnumber is incorrect in create request, No Account will passed in FPO response ")
    public void TC03_CheckFPOResponse_AccountNoIncorrect() throws Exception {
        User user = userManager.getForRead(Label.ADVANCEDEPOSITLOWBALANCEUSER);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_MUTUAL_MF;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setAccountNumber("XXXXXXXX6102")
                .setTxnValue("1")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("5")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("0")
                .setSubscriptionEnableRetry("0")
                .setRequestType("NATIVE_MF_SIP")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String orderId = initTxnDTO.getBody().getOrderId();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(),
                orderId, fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assert.assertFalse(fetchPaymentOptionsJson.getString("body").contains("maskedAccountNumber"), "Accounts will not get filtered");
    }
    @Feature("PGP-41005")
    @Owner(Constants.Owner.TAMANA_TATHAN)
    @Test(description = "Validate FPO response when accountnumber is passed in all numeric , Only that accounts should get filtered")
    public void TC04_CheckFPOResponse_AccountNoNumeric() throws Exception {
        User user = userManager.getForRead(Label.ADVANCEDEPOSITLOWBALANCEUSER);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_MUTUAL_MF;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setAccountNumber("123456784872")
                .setTxnValue("1")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("5")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("0")
                .setSubscriptionEnableRetry("0")
                .setRequestType("NATIVE_MF_SIP")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String orderId = initTxnDTO.getBody().getOrderId();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(),
                orderId, fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        if(fetchPaymentOptionsJson.getString("body").contains("maskedAccountNumber")){
            Assert.assertTrue(fetchPaymentOptionsJson.getString("body.groupedMerchantPayOption.upiProfile.respDetails.profileDetail.bankAccounts").contains("maskedAccountNumber:XXXXXXXXXX4872"), "Validating the account number which is passed in create Req");
            Assert.assertTrue(fetchPaymentOptionsJson.getString("body.merchantPayOption.upiProfile.respDetails.profileDetail.bankAccounts").contains("maskedAccountNumber:XXXXXXXXXX4872"), "Validating the account number , which is passed in create Req");
            Assert.assertFalse(fetchPaymentOptionsJson.getString("body.merchantPayOption.upiProfile.respDetails.profileDetail.bankAccounts").contains("maskedAccountNumber:XXXXXXXX6101"), "Validating that the other account number is not passed in FPO response");
            Assert.assertFalse(fetchPaymentOptionsJson.getString("body.groupedMerchantPayOption.upiProfile.respDetails.profileDetail.bankAccounts").contains("maskedAccountNumber:XXXXXXXX6101"), "Validating that the other account number is not passed in FPO response");
        }
    }
    @Feature("PGP-41005")
    @Owner(Constants.Owner.TAMANA_TATHAN)
    @Test(description = "Validate FPO response when accountnumber is passed as NULL in create request , All the accounts will passed in FPO")
    public void TC05_CheckFPOResponse_AccountNoPassedAsNULL() throws Exception {
        User user = userManager.getForRead(Label.ADVANCEDEPOSITLOWBALANCEUSER);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_MUTUAL_MF;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setAccountNumber(" ")
                .setTxnValue("1")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("5")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("0")
                .setSubscriptionEnableRetry("0")
                .setRequestType("NATIVE_MF_SIP")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String orderId = initTxnDTO.getBody().getOrderId();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(),
                orderId, fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        if(fetchPaymentOptionsJson.getString("body").contains("maskedAccountNumber")){
            Assert.assertTrue(fetchPaymentOptionsJson.getString("body.groupedMerchantPayOption.upiProfile.respDetails.profileDetail.bankAccounts").contains("maskedAccountNumber:XXXXXXXX6101"), "Validating the ALL Account Number is present in FPO, when no accNumber is sent in create Req");
            Assert.assertTrue(fetchPaymentOptionsJson.getString("body.groupedMerchantPayOption.upiProfile.respDetails.profileDetail.bankAccounts").contains("maskedAccountNumber:XXXXXXXXXX4872"), "Validating the ALL Account Number is present in FPO, when no accNumber is sent in create Req");
            Assert.assertTrue(fetchPaymentOptionsJson.getString("body.merchantPayOption.upiProfile.respDetails.profileDetail.bankAccounts").contains("maskedAccountNumber:XXXXXXXX6101"), "Validating the ALL Account Number is present in FPO, when no accNumber is sent in create Req");
            Assert.assertTrue(fetchPaymentOptionsJson.getString("body.merchantPayOption.upiProfile.respDetails.profileDetail.bankAccounts").contains("maskedAccountNumber:XXXXXXXXXX4872"), "Validating the ALL Account Number is present in FPO, when no accNumber is sent in create Req");
        }
    }
    @Feature("PGP-41005")
    @Owner(Constants.Owner.TAMANA_TATHAN)
    @Test(description = "Validate the response when request Type is null with TPV parameters")
    public void TC06_ValidateCreateSubscriptionResponse_RequestTypeNULL() throws Exception {
        User user = userManager.getForRead(Label.ADVANCEDEPOSITLOWBALANCEUSER);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_MUTUAL_MF;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setAccountNumber(" ")
                .setTxnValue("1")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("5")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("0")
                .setSubscriptionEnableRetry("0")
                .setRequestType("")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .build();
        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        Assert.assertTrue(responseDTO.getBody().getResultInfo().getResultStatus().equals("F"), "Result Status validation");
        Assert.assertTrue(responseDTO.getBody().getResultInfo().getResultCode().equals("501"), "Result Code validation");
        Assert.assertTrue(responseDTO.getBody().getResultInfo().getResultMsg().equals("System Error"), "Result Message validation");
    }

        @Parameters({"isNativePlus"})
        @Test(description = "Verify txnMessage in Subscription payload when passed in prenotify ")
        public void tc_01(@Optional("false") Boolean isNativePlus) throws Exception
        {
            User user = userManager.getForWrite(Label.BASIC);
            Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL;
            String SubscriptionStartDate = CommonHelpers.getDate().toString();
            InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                    .setTxnValue("1")
                    .setSubscriptionPaymentMode("PPI")
                    .setSubsPPIOnly("Y")
                    .setSubscriptionAmountType("VARIABLE")
                    .setSubscriptionMaxAmount("10")
                    .setSubscriptionFrequency("1")
                    .setSubscriptionFrequencyUnit("ONDEMAND")
                    .setSubscriptionGraceDays("0")
                    .setSubscriptionStartDate(SubscriptionStartDate)
                    .setRequestType("NATIVE_SUBSCRIPTION")
                    .build();
            InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
            String txnToken = initTxnResponseDTO.getBody().getTxnToken();
            String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
            OrderDTO orderDTO = new OrderFactory.SubscriptionNative(Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL, initTxnDTO.getBody().getOrderId(), txnToken, PayMethodType.BALANCE, subsId)
                    .setAUTH_MODE("USRPWD")
                    .setTXN_AMOUNT("1")
                    .build();
            checkoutPage.createNativeOrder(orderDTO, isNativePlus);

            /*Prenotify Subs*/
            String date = CommonHelpers.addDays(CommonUtils.getdate("dd-MM-yyyy"), "dd-MM-yyyy", 1);
            PreNotify preNotify = new PreNotify(merchant, orderDTO.getTXN_AMOUNT(), subsId,date);
            Response response = preNotify.execute();
            Assertions.assertThat(response.jsonPath().getString("body.resultInfo.status")).isEqualTo("SUCCESS");
            String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.subscription,orderDTO.getORDER_ID(),"PRE_PAYMENT_REMINDER");
            Assertions.assertThat(logs).contains("\"txnMessage\":\"subscription for postpaid mobile bill\"");
        }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify txnMessage in theia payload when passed in prenotify ")
    public void tc_02(@Optional("false") Boolean isNativePlus) throws Exception
    {
        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL;
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("PPI")
                .setSubsPPIOnly("Y")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL, initTxnDTO.getBody().getOrderId(), txnToken, PayMethodType.BALANCE, subsId)
                .setAUTH_MODE("USRPWD")
                .setTXN_AMOUNT("1")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);

        /*Prenotifys Subs*/
        String date = CommonUtils.getdate("dd-MM-yyyy");
        String referenceId = CommonHelpers.generateOrderId();
        String txnDate = CommonHelpers.addDays(date, "dd-MM-yyyy", 1) ;

        PreNotify preNotify = new PreNotify(Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL, "1", subsId ,txnDate,referenceId);
        Response response = preNotify.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.status"))
                .isEqualTo("SUCCESS");

        /*Renew Subs*/
        String paytmRefId = response.jsonPath().getString("body.paytmReferenceId");
        modifyNotifyDatesInDB(paytmRefId);
        executeRenewalAndFetchOrderId(merchant, subsId, "1", "");
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderDTO.getORDER_ID(),"ACQUIRING_CREATE_ORDER");
        Assertions.assertThat(logs).contains("\"txnMessage\":\"subscription for postpaid mobile bill\"");

    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify txnMessage in Subscription payload when ff4j flag is disabled")
    public void tc_03(@Optional("false") Boolean isNativePlus) throws Exception
    {
        FF4JFlags.disable("subs.prenotify.ppi.txnMessage.enabled");
        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBS_Prenotify_MID;
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("PPI")
                .setSubsPPIOnly("Y")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);            //Initiate create subs first request
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(Constants.MerchantType.SUBS_Prenotify_MID, initTxnDTO.getBody().getOrderId(), txnToken, PayMethodType.BALANCE, subsId)
                .setAUTH_MODE("USRPWD")
                .setTXN_AMOUNT("1")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);

        /*Prenotifys Subs*/
        String date = CommonUtils.getdate("dd-MM-yyyy");
        String referenceId = CommonHelpers.generateOrderId();
        String txnDate = CommonHelpers.addDays(date, "dd-MM-yyyy", 1) ;

        PreNotify preNotify = new PreNotify(Constants.MerchantType.SUBS_Prenotify_MID, "1", subsId ,txnDate,referenceId);
        Response response = preNotify.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.status"))
                .isEqualTo("SUCCESS");

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.subscription,orderDTO.getORDER_ID(),"PRE_PAYMENT_REMINDER");
        Assertions.assertThat(logs).doesNotContain("\"txnMessage\":\"subscription for postpaid mobile bill\"");
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify txnMessage in theia payload when ff4j flag is disabled")
    public void tc_04(@Optional("false") Boolean isNativePlus) throws Exception
    {
        FF4JFlags.disable("subs.prenotify.ppi.txnMessage.enabled");
        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBS_Prenotify_MID;
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("PPI")
                .setSubsPPIOnly("Y")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);            //Initiate create subs first request
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(Constants.MerchantType.SUBS_Prenotify_MID, initTxnDTO.getBody().getOrderId(), txnToken, PayMethodType.BALANCE, subsId)
                .setAUTH_MODE("USRPWD")
                .setTXN_AMOUNT("1")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);


        /*Prenotifys Subs*/
        String date = CommonUtils.getdate("dd-MM-yyyy");
        String referenceId = CommonHelpers.generateOrderId();
        String txnDate = CommonHelpers.addDays(date, "dd-MM-yyyy", 1) ;

        PreNotify preNotify = new PreNotify(Constants.MerchantType.SUBS_Prenotify_MID, "1", subsId ,txnDate,referenceId);
        Response response = preNotify.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.status"))
                .isEqualTo("SUCCESS");

        /*Renew Subs*/
        String paytmRefId = response.jsonPath().getString("body.paytmReferenceId");
        modifyNotifyDatesInDB(paytmRefId);
        executeRenewalAndFetchOrderId(merchant, subsId, "1", "");
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderDTO.getORDER_ID(),"ACQUIRING_CREATE_ORDER");
        Assertions.assertThat(logs).doesNotContain("\"txnMessage\":\"subscription for postpaid mobile bill\"");

    }

    @Feature("PGP-48382")
    @Owner(Constants.Owner.TAMANA_TATHAN)
    @Test(description = "Validate the account number is getting passed and istpv value is coming as true in subscription")
    public void TC01_PGP_48382() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.MF_SIP_NEW_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(merchant.getId(), merchant.getKey(), "")
                .setTxnValue("5")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("100")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setAccountNumber("83748239234872")
                .setSubscriptionRetryCount("0")
                .setSubscriptionEnableRetry("0")
                .setRequestType("NATIVE_MF_SIP")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .build();
        validateSuccessInitiateSubscription(initTxnDTO);

        LogsValidationHelper logsValidation = new LogsValidationHelper();
        String logs = logsValidation.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.subscription , initTxnDTO.getBody().getOrderId(), "Request received in request filter");
        Assertions.assertThat(logs).contains("\"accountNumber\":\"83748239234872\"");
        Assertions.assertThat(logs).contains("isTPVflow\":true");
    }

    @Feature("PGP-48382")
    @Owner(Constants.Owner.TAMANA_TATHAN)
    @Test(description = "Validate the account number is not getting passed and istpv value is coming as false , when mid is not in ff4j flag")
    public void TC02_PGP_48382() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.TPV_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(merchant.getId(), merchant.getKey(), "")
                .setTxnValue("5")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("100")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setAccountNumber("83748239234872")
                .setSubscriptionRetryCount("0")
                .setSubscriptionEnableRetry("0")
                .setRequestType("NATIVE_MF_SIP")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);

        LogsValidationHelper logsValidation = new LogsValidationHelper();
        String logs = logsValidation.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.subscription, initTxnDTO.getBody().getOrderId(), "Request received in request filter");
        Assertions.assertThat(logs).contains("accountNumber\":null");
        Assertions.assertThat(logs).contains("isTPVflow\":false");
    }


    @Feature("PGP-48382")
    @Owner(Constants.Owner.TAMANA_TATHAN)
    @Test(description = "Validate the account number is getting passed and istpv value is coming as true , when flag is off")
    public void TC03_PGP_48382() throws Exception {
        FF4JFlags.disable("theia.addUnmaskedAccountNoInSubsCreateRequest");
        Constants.MerchantType merchant = Constants.MerchantType.MF_SIP_NEW_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(merchant.getId(), merchant.getKey(), "")
                .setTxnValue("5")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("100")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setAccountNumber("83748239234872")
                .setSubscriptionRetryCount("0")
                .setSubscriptionEnableRetry("0")
                .setRequestType("NATIVE_MF_SIP")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .build();
        validateSuccessInitiateSubscription(initTxnDTO);

        LogsValidationHelper logsValidation = new LogsValidationHelper();
        String logs = logsValidation.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.subscription, initTxnDTO.getBody().getOrderId(), "Request received in request filter");
        Assert.assertFalse(logs.contains("accountNumber\":null"));
        Assert.assertFalse(logs.contains("isTPVflow\":false"));
        FF4JFlags.enable("theia.addUnmaskedAccountNoInSubsCreateRequest");
    }


    @Feature("PGP-48382")
    @Owner(Constants.Owner.TAMANA_TATHAN)
    @Test(description = "After success transaction , check data is updated in db")
    public void TC04_PGP_48382() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.MF_SIP_NEW_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(merchant.getId(), merchant.getKey(), "")
                .setTxnValue("5")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("100")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setAccountNumber("83748239234872")
                .setSubscriptionRetryCount("0")
                .setSubscriptionEnableRetry("0")
                .setRequestType("NATIVE_MF_SIP")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();

        OrderDTO orderDTO = new OrderFactory.UPIMandate(merchant, txnToken, initTxnDTO.orderFromBody(), subsId)
                .setPayerAccount("test@paytm")
                .build();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(orderDTO.getMID(),txnToken,orderDTO.getORDER_ID())
                .setPaymentMode("UPI")
                .setPayerAccount("test@paytm")
                .build();
        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
        Response ptcResponse = processTransactionV1.execute();
        Assertions.assertThat(ptcResponse.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        String query = "SELECT bank_details FROM SUBS_ACC.subscription_upi_details WHERE subscription_id ='" + subsId + "'";
        Assertions.assertThat((DatabaseUtil.getInstance().executeSelectQuery(LocalConfig.PGP_DB_CONNECTION_URL, query).toString()).contains("encryptedAccNo"));
    }


    @Feature("PGP-48382")
    @Owner(Constants.Owner.TAMANA_TATHAN)
    @Test(description = "After success creation , renew will get fail ")
    public void TC05_PGP_48382() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.MF_SIP_NEW_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(merchant.getId(), merchant.getKey(), "")
                .setTxnValue("5")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("100")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setAccountNumber("83748239234872")
                .setSubscriptionRetryCount("0")
                .setSubscriptionEnableRetry("0")
                .setRequestType("NATIVE_MF_SIP")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();

        OrderDTO orderDTO = new OrderFactory.UPIMandate(merchant, txnToken, initTxnDTO.orderFromBody(), subsId)
                .setPayerAccount("test@paytm")
                .build();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(orderDTO.getMID(), txnToken, orderDTO.getORDER_ID())
                .setPaymentMode("UPI")
                .setPayerAccount("test@paytm")
                .build();
        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
        Response ptcResponse = processTransactionV1.execute();
        Assertions.assertThat(ptcResponse.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        //prenotify
        String referenceId = CommonHelpers.generateOrderId();
        String date = CommonUtils.getdate("dd-MM-yyyy");
        String txnDate = CommonHelpers.addDays(date, "dd-MM-yyyy", 1);
        wait(1000);
        PreNotify preNotify = new PreNotify(Constants.MerchantType.MF_SIP_NEW_MID, "5", subsId ,txnDate,referenceId);
        Response response = preNotify.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.status"))
                .isEqualTo("SUCCESS");
        String paytmRefId = response.jsonPath().getString("body.paytmReferenceId");
        PostpaidRenewSubs.modifyNotifyDatesInDB(paytmRefId);

        //renew
        PostpaidRenewSubs.executeRenewalAndFetchOrderId(merchant, subsId, "5", "SUBS_RENEWAL_MF_SIP");

        String query = "SELECT status FROM SUBS_ACC.subscription_payment_details WHERE subscription_id ='" + subsId + "'";
        Assertions.assertThat((DatabaseUtil.getInstance().executeSelectQuery(LocalConfig.PGP_DB_CONNECTION_URL, query).toString()).contains("FAIL"));
    }


    @Feature("PGP-48382")
    @Owner(Constants.Owner.TAMANA_TATHAN)
    @Test(description = "If account number is not passed in req , check data in DB")
    public void TC06_PGP_48382() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.MF_SIP_NEW_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(merchant.getId(), merchant.getKey(), "")
                .setTxnValue("5")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("100")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("0")
                .setSubscriptionEnableRetry("0")
                .setRequestType("NATIVE_MF_SIP")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();

        OrderDTO orderDTO = new OrderFactory.UPIMandate(merchant, txnToken, initTxnDTO.orderFromBody(), subsId)
                .setPayerAccount("test@paytm")
                .build();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(orderDTO.getMID(),txnToken,orderDTO.getORDER_ID())
                .setPaymentMode("UPI")
                .setPayerAccount("test@paytm")
                .build();
        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
        Response ptcResponse = processTransactionV1.execute();
        Assertions.assertThat(ptcResponse.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        String query = "SELECT bank_details FROM SUBS_ACC.subscription_upi_details WHERE subscription_id ='" + subsId + "'";
        Assert.assertFalse((DatabaseUtil.getInstance().executeSelectQuery(LocalConfig.PGP_DB_CONNECTION_URL, query).toString()).contains("encryptedAccNo"));
    }

    @Feature("PGP-48382")
    @Owner(Constants.Owner.TAMANA_TATHAN)
    @Test(description = "If account number is  passed as null in req , check data in DB")
    public void TC07_PGP_48382() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.MF_SIP_NEW_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(merchant.getId(), merchant.getKey(), "")
                .setTxnValue("5")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("100")
                .setSubscriptionFrequency("1")
                .setAccountNumber("")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("0")
                .setSubscriptionEnableRetry("0")
                .setRequestType("NATIVE_MF_SIP")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();

        OrderDTO orderDTO = new OrderFactory.UPIMandate(merchant, txnToken, initTxnDTO.orderFromBody(), subsId)
                .setPayerAccount("test@paytm")
                .build();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(orderDTO.getMID(),txnToken,orderDTO.getORDER_ID())
                .setPaymentMode("UPI")
                .setPayerAccount("test@paytm")
                .build();
        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
        Response ptcResponse = processTransactionV1.execute();
        Assertions.assertThat(ptcResponse.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        String query = "SELECT bank_details FROM SUBS_ACC.subscription_upi_details WHERE subscription_id ='" + subsId + "'";
        Assert.assertTrue((DatabaseUtil.getInstance().executeSelectQuery(LocalConfig.PGP_DB_CONNECTION_URL, query).toString()).contains("encryptedAccNo"));
    }
        public static void modifyNotifyDatesInDB(String paytmRefId) {

        PGPHelpers.modifySubscriptionPreNotifyStatus(Long.valueOf(paytmRefId), "1");
        PGPHelpers.modifySubscriptionPreNotifyDate(Long.valueOf(paytmRefId), LocalDateTime.now().minusDays(2));
        PGPHelpers.modifySubscriptionPreNotifyTxnDate(Long.valueOf(paytmRefId), LocalDateTime.now().with(LocalTime.MIDNIGHT));

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

    @Feature("PGP-48530")
    @Owner(AKSHAT)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify that BankTransactionId is returned in the response of WalletSubsTransaction")
    public void nativeWalletSubs_BankTxnId_returned(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBS_Prenotify_MID;
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
    //    WalletHelpers.modifyBalance(user, 2.00);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("PPI")
                .setSubsPPIOnly("Y")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("5")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(Constants.MerchantType.SUBS_Prenotify_MID, initTxnDTO.getBody().getOrderId(), txnToken, PayMethodType.BALANCE, subsId)
                .setAUTH_MODE("USRPWD")
                .setTXN_AMOUNT("1")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("PPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("WALLET")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount("1.00")
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("WALLET")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName("WALLET")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateSubsid(Constants.ValidationType.NON_EMPTY)
                .AssertAll();
    }

    @Feature("PGP-49014")
    @Owner(AKSHAT)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify that disabled wallet is returned in v2 subs FPO response")
    public void InativeWallet_returned_in_v2FPO(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBS_Prenotify_MID;
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        //      WalletHelpers.modifyBalance(user, 2.00);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("PPI")
                .setSubsPPIOnly("Y")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("1")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        String orderId = initTxnDTO.getBody().getOrderId();

        /*FetchPaymentOptResponseDTO fetchPaymentOptResponseDTO = NativeHelpers.fetchPaymentOptionResponse(txnToken, merchant.getId(), initTxnDTO.orderFromBody());
        boolean paymodeStatus = NativeHelpers.isFetchPaymentOptionStatusMatched(fetchPaymentOptResponseDTO, "BALANCE", true);
        Assertions.assertThat(paymodeStatus).as("BALANCE paymode status mismatch").isTrue();*/

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v2").build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(), orderId, fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes[0].displayName")).isEqualTo("Paytm Balance");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes[0].paymentMode")).isEqualTo("BALANCE");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.isDisabled[0].status")).isEqualTo("true");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.isDisabled[0].msg")).isEqualTo("Please create account");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.isDisabled[0].merchantAccept")).isEqualTo("true");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.isDisabled[0].userAccountExist")).isEqualTo("false");

    }

    @Feature("PGP-49014")
    @Owner(AKSHAT)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify that disabled wallet is returned in v5 subs FPO response")
    public void InativeWallet_returned_in_v5FPO(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBS_Prenotify_MID;
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        //      WalletHelpers.modifyBalance(user, 2.00);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("PPI")
                .setSubsPPIOnly("Y")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("1")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        String orderId = initTxnDTO.getBody().getOrderId();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(), orderId, fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes[0].displayName")).isEqualTo("Paytm Balance");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes[0].paymentMode")).isEqualTo("BALANCE");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.isDisabled[0].merchantAccept")).isEqualTo("true");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.isDisabled[0].msg")).isEqualTo("Please create account");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.isDisabled[0].status")).isEqualTo("true");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.isDisabled[0].userAccountExist")).isEqualTo("false");

    }

    @Feature("PGP-49014")
    @Owner(AKSHAT)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify that enabled wallet is returned in v2 subs FPO response")
    public void ActiveWallet_returned_in_v2FPO(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBS_Prenotify_MID;
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
              WalletHelpers.modifyBalance(user, 2.00);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("PPI")
                .setSubsPPIOnly("Y")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("1")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        String orderId = initTxnDTO.getBody().getOrderId();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v2").build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(), orderId, fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes[0].displayName")).isEqualTo("Paytm Balance");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes[0].paymentMode")).isEqualTo("BALANCE");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.payChannelOptions[0].balanceInfo[0].payerAccountExists")).isEqualTo("true");

    }

    @Feature("PGP-49014")
    @Owner(AKSHAT)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify that enabled wallet is returned in v5 subs FPO response")
    public void ActiveWallet_returned_in_v5FPO(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBS_Prenotify_MID;
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
              WalletHelpers.modifyBalance(user, 2.00);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("PPI")
                .setSubsPPIOnly("Y")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("1")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        String orderId = initTxnDTO.getBody().getOrderId();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(), orderId, fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes[0].displayName")).isEqualTo("Paytm Balance");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes[0].paymentMode")).isEqualTo("BALANCE");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.payChannelOptions[0].balanceInfo[0].payerAccountExists")).isEqualTo("true");

    }

    @Feature("PGP-53048")
    @Owner(AKSHAT)
    @Test(description = "Verify that user is not able to create subscription with wallet as paymode")
    public void walletNotSupporting_subsCreation() throws Exception {

        // ff4j theia.disable.balance.forSubscriptionEligibility should be ON

        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBS_Prenotify_MID;
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("PPI")
                .setSubsPPIOnly("Y")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("1")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
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
                .isEqualToIgnoringCase("Invalid Subs payment mode");
    }

    @Feature("PGP-53048")
    @Owner(AKSHAT)
    @Test(description = "Verify that user is not able to create subscription with PPBL as paymode")
    public void ppblNotSupporting_subsCreation() throws Exception {

        // ff4j theia.disable.ppbl.forSubscriptionEligibility should be ON

        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBS_Prenotify_MID;
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", merchant)
                .setTxnValue("2")
                .setSubscriptionPaymentMode("PPBL")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("1")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
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
                .isEqualToIgnoringCase("Invalid Subs payment mode");
    }

    @Feature("PGP-54581")
    @Owner(AKSHAT)
    @Test(description = "Verify that wallet is returned in subs FPO response")
    public void walletNotReturned_inSubsFpo() throws Exception {

        // ff4j theia.disable.balance.forSubscriptionEligibility should be ON

        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBS_Prenotify_MID;
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("3")
                .setSubscriptionPaymentMode("")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("1")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        String orderId = initTxnDTO.getBody().getOrderId();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(), orderId, fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes[0].displayName")).doesNotContain("Paytm Balance");

    }

    @Feature("PGP-54581")
    @Owner(AKSHAT)
    @Test(description = "Verify that PPBL is returned in subs FPO response")
    public void ppblNotReturned_inSubsFpo() throws Exception {

        // ff4j theia.disable.balance.forSubscriptionEligibility should be ON

        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBS_Prenotify_MID;
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("4")
                .setSubscriptionPaymentMode("")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("1")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        String orderId = initTxnDTO.getBody().getOrderId();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(), orderId, fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes[0].displayName")).doesNotContain("Paytm Bank Account");

    }


    @Feature("PGP-58764")
    @Owner(AKSHAT)
    @Test(description = "Validate that payerAccountNumbers is returned in MF_SIP FPO response with multi TPV against it")
    public void TC01_multiTPV_MFSIP() throws Exception {

        Constants.MerchantType merchant = Constants.MerchantType.SIP_PCF_fix;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setAccountNumber("83748239234872|94638239236085|85658239234839|80488239232839|01208239232542|01398239231129")
                .setAllowUnverifiedAccount("true")
                .setValidateAccountNumber("true")
                .setTxnValue("5")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("50")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("0")
                .setSubscriptionEnableRetry("0")
                .setRequestType("NATIVE_MF_SIP")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String orderId = initTxnDTO.getBody().getOrderId();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(),
                orderId, fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assert.assertTrue(fetchPaymentOptionsJson.getString("body.payerAccountNumbers").contains("XXXXXXXXXX4872|XXXXXXXXXX6085|XXXXXXXXXX4839|XXXXXXXXXX2839|XXXXXXXXXX2542|XXXXXXXXXX1129"));
        Assert.assertTrue(fetchPaymentOptionsJson.getString("body.productCode").contains("51051000100000000043"));

    }

    @Feature("PGP-58764")
    @Owner(AKSHAT)
    @Test(description = "Validate that payerAccountNumbers is returned in NativeSubscription FPO response with multi TPV against it")
    public void TC02_multiTPV_NativeSubscription() throws Exception {

        Constants.MerchantType merchant = Constants.MerchantType.Subs_HDFC_CheckoutJS;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setAccountNumber("83748239234872|94638239236085|85658239234839|80488239232839|01208239232542|01398239231129")
                .setAllowUnverifiedAccount("true")
                .setValidateAccountNumber("true")
                .setTxnValue("5")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("50")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("0")
                .setSubscriptionEnableRetry("0")
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String orderId = initTxnDTO.getBody().getOrderId();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(),
                orderId, fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assert.assertTrue(fetchPaymentOptionsJson.getString("body.payerAccountNumbers").contains("XXXXXXXXXX4872|XXXXXXXXXX6085|XXXXXXXXXX4839|XXXXXXXXXX2839|XXXXXXXXXX2542|XXXXXXXXXX1129"));
        Assert.assertTrue(fetchPaymentOptionsJson.getString("body.productCode").contains("51051000100000000004"));

    }


    @Feature("PGP-59241")
    @Owner(AKSHAT)
    @Test(description = "Verify order is created on Product Code 51051000100000000052 for PCF & MF_SIP enabled MID")
    public void validatePennyDropSIP_51051000100000000052() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.MF_SIP_NEW_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("0")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("50")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("0")
                .setSubscriptionEnableRetry("0")
                .setRequestType("NATIVE_MF_SIP")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String orderId = initTxnDTO.getBody().getOrderId();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();

        OrderDTO orderDTO = new OrderFactory.UPIMandate(merchant, txnToken, initTxnDTO.orderFromBody(), subsId)
                .setPayerAccount("test@paytm")
                .build();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(orderDTO.getMID(),txnToken,orderDTO.getORDER_ID())
                .setPaymentMode("UPI")
                .setPayerAccount("test@paytm")
                .build();
        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
        Response ptcResponse = processTransactionV1.execute();

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderDTO.getORDER_ID());
        Assertions.assertThat(logs).contains("\"API_NAME\": \"ACQUIRING_PAY_ORDER\"");
        Assertions.assertThat(logs).contains("\"productCode\":\"51051000100000000052\"");
        Assertions.assertThat(logs).contains("{\"resultInfo\":{\"resultCode\":\"SUCCESS\",\"resultCodeId\":\"00000000\",\"resultStatus\":\"S\",\"resultMsg\":\"success\"}");


    }

    @Feature("PGP-59241")
    @Owner(AKSHAT)
    @Test(description = "Verify order is created on Product Code 51051000100000000052 for PCF & Subs enabled MID")
    public void validatePennyDropSubs_51051000100000000052() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_Pg2_MID2;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("0")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("50")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("0")
                .setSubscriptionEnableRetry("0")
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String orderId = initTxnDTO.getBody().getOrderId();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();

        OrderDTO orderDTO = new OrderFactory.UPIMandate(merchant, txnToken, initTxnDTO.orderFromBody(), subsId)
                .setPayerAccount("test@paytm")
                .build();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(orderDTO.getMID(),txnToken,orderDTO.getORDER_ID())
                .setPaymentMode("UPI")
                .setPayerAccount("test@paytm")
                .build();
        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
        Response ptcResponse = processTransactionV1.execute();

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderDTO.getORDER_ID());
        Assertions.assertThat(logs).contains("\"API_NAME\": \"ACQUIRING_PAY_ORDER\"");
        Assertions.assertThat(logs).contains("\"productCode\":\"51051000100000000052\"");
        Assertions.assertThat(logs).contains("{\"resultInfo\":{\"resultCode\":\"SUCCESS\",\"resultCodeId\":\"00000000\",\"resultStatus\":\"S\",\"resultMsg\":\"success\"}");

    }

    @Feature("PGP-59260")
    @Owner(VIDHI)
    @Test(description = "Verify the v1/ptc response when cardExpiry is different in v1/ptc request and create/subs request")
    public void verify_V1_PTC_cardExpiryDiff() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.ALLOWED_TPAP_MERCHANT_PPSL;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("11")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("50")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("0")
                .setSubscriptionEnableRetry("0")
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setEnablePaymentMode(new EnablePaymentMode[]
                        {
                                new EnablePaymentMode().setMode("CREDIT_CARD").setcardExpiry("122035")
                        })
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String orderId = initTxnDTO.getBody().getOrderId();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();

        OrderDTO orderDTO = new OrderFactory.UPIMandate(merchant, txnToken, initTxnDTO.orderFromBody(), subsId)
                .build();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(orderDTO.getMID(), txnToken, orderDTO.getORDER_ID())
                .setPaymentMode("CREDIT_CARD")
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setSubsId(subsId)
                .setCardInfo("|4718650100010336|111|122040")
                .build();

        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
        Response ptcResponse = processTransactionV1.execute();
        System.out.println(ptcResponse.jsonPath());
        Assert.assertEquals(ptcResponse.jsonPath().getString("body.resultInfo.resultMsg"),"Request parameters are not valid");
    }

    @Feature("PGP-59260")
    @Owner(VIDHI)
    @Test(description = "Verify the logs when cardExpiry is different in v1/ptc request and create/subs request")
    public void verify_Logs_cardExpiryDiff() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.ALLOWED_TPAP_MERCHANT_PPSL;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("11")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("50")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("0")
                .setSubscriptionEnableRetry("0")
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setEnablePaymentMode(new EnablePaymentMode[]
                        {
                                new EnablePaymentMode().setMode("CREDIT_CARD").setcardExpiry("122035")
                        })
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String orderId = initTxnDTO.getBody().getOrderId();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();

        OrderDTO orderDTO = new OrderFactory.UPIMandate(merchant, txnToken, initTxnDTO.orderFromBody(), subsId)
                .build();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(orderDTO.getMID(),txnToken,orderDTO.getORDER_ID())
                .setPaymentMode("CREDIT_CARD")
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setSubsId(subsId)
                .setCardInfo("|4718650100010336|111|122040")
                .build();

        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
        Response ptcResponse = processTransactionV1.execute();
        System.out.println(ptcResponse.jsonPath());
        String grepcmd = "com.paytm.pgplus.theia.utils.ProcessTransactionUtil.validateCardExpiry()";
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia, orderId, grepcmd);
        System.out.println(logs);
        Assertions.assertThat(logs).contains("Mismatch in expiry date provided in initiate & PTC request");
    }

    @Feature("PGP-59260")
    @Owner(VIDHI)
    @Test(description = "Verify the v1/ptc response when cardExpiry is same in v1/ptc request and create/subs request")
    public void verify_V1_PTC_cardExpirySame() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL_TXN;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("11")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("50")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("0")
                .setSubscriptionEnableRetry("0")
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setEnablePaymentMode(new EnablePaymentMode[]
                        {
                                new EnablePaymentMode().setMode("CREDIT_CARD").setcardExpiry("122035")
                        })
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String orderId = initTxnDTO.getBody().getOrderId();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();

        OrderDTO orderDTO = new OrderFactory.UPIMandate(merchant, txnToken, initTxnDTO.orderFromBody(), subsId)
                .build();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(orderDTO.getMID(), txnToken, orderDTO.getORDER_ID())
                .setPaymentMode("CREDIT_CARD")
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setSubsId(subsId)
                .setCardInfo("|4718650100010336|111|122035")
                .build();

        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
        Response ptcResponse = processTransactionV1.execute();
        System.out.println(ptcResponse.jsonPath());
        Assert.assertEquals(ptcResponse.jsonPath().getString("body.txnInfo.PAYMENTMODE"),"CC");
    }

    @Feature("PGP-61250")
    @Owner(AKSHAT)
    @Test(description = "Verify that following params are passed in SIP Cashier Pay subscriptionRequestType, isZeroRupeeTxn, actualMid")
    public void TC_001_Native_SIP_Cards_CashierPayParams() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.Subs_HDFC_CheckoutJS;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("0")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("50")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("0")
                .setSubscriptionEnableRetry("0")
                .setRequestType("NATIVE_MF_SIP")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String orderId = initTxnDTO.getBody().getOrderId();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();

        OrderDTO orderDTO = new OrderFactory.UPIMandate(merchant, txnToken, initTxnDTO.orderFromBody(), subsId)
                .build();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(orderDTO.getMID(), txnToken, orderDTO.getORDER_ID())
                .setPaymentMode("CREDIT_CARD")
                .setRequestType("NATIVE_MF_SIP")
                .setSubsId(subsId)
                .setCardInfo("|4444333322221111|111|122035")
                .build();

        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
        Response ptcResponse = processTransactionV1.execute();
        System.out.println(ptcResponse.jsonPath());

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderDTO.getORDER_ID(), "ACQUIRING_PAY_ORDER");
        Assertions.assertThat(logs).contains("\"subscriptionRequestType\":\"CREATE\"");
        Assertions.assertThat(logs).contains("\"isZeroRupeeTxn\":\"true\"");
        Assertions.assertThat(logs).contains("\"actualMid\":\"qa12mm54514776485488\"");


    }

    @Feature("PGP-61250")
    @Owner(AKSHAT)
    @Test(description = "Verify that following params are passed in Subs Cashier Pay subscriptionRequestType, isZeroRupeeTxn, actualMid")
    public void TC_002_Native_SIP_Cards_CashierPayParams() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.Subs_HDFC_CheckoutJS;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("0")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("50")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("0")
                .setSubscriptionEnableRetry("0")
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String orderId = initTxnDTO.getBody().getOrderId();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();

        OrderDTO orderDTO = new OrderFactory.UPIMandate(merchant, txnToken, initTxnDTO.orderFromBody(), subsId)
                .build();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(orderDTO.getMID(), txnToken, orderDTO.getORDER_ID())
                .setPaymentMode("DEBIT_CARD")
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setSubsId(subsId)
                .setCardInfo("|4444333322221111|111|122035")
                .build();

        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
        Response ptcResponse = processTransactionV1.execute();
        System.out.println(ptcResponse.jsonPath());

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderDTO.getORDER_ID(), "ACQUIRING_PAY_ORDER");
        Assertions.assertThat(logs).contains("\"subscriptionRequestType\":\"CREATE\"");
        Assertions.assertThat(logs).contains("\"isZeroRupeeTxn\":\"true\"");
        Assertions.assertThat(logs).contains("\"actualMid\":\"qa12mm54514776485488\"");


    }

    @Feature("PGP-61250")
    @Owner(AKSHAT)
    @Test(description = "Verify that SIP transaction is routed on the gateway of original transaction")
    public void TC_003_Native_SIP_Cards_RoutingGateway() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.Subs_HDFC_CheckoutJS;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("0")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("50")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("0")
                .setSubscriptionEnableRetry("0")
                .setRequestType("NATIVE_MF_SIP")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String orderId = initTxnDTO.getBody().getOrderId();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();

        OrderDTO orderDTO = new OrderFactory.UPIMandate(merchant, txnToken, initTxnDTO.orderFromBody(), subsId)
                .build();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(orderDTO.getMID(), txnToken, orderDTO.getORDER_ID())
                .setPaymentMode("CREDIT_CARD")
                .setRequestType("NATIVE_MF_SIP")
                .setSubsId(subsId)
                .setCardInfo("|4444333322221111|111|122035")
                .build();

        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
        Response ptcResponse = processTransactionV1.execute();
        System.out.println(ptcResponse.jsonPath());
        Assert.assertEquals(ptcResponse.jsonPath().getString("body.txnInfo.GATEWAYNAME"),"AXIA");


    }

    @Feature("PGP-61250")
    @Owner(AKSHAT)
    @Test(description = "Verify that subs transaction is routed on the gateway of original transaction")
    public void TC_004_Native_Subs_Cards_RoutingGateway() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.Subs_HDFC_CheckoutJS;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("0")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("50")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("0")
                .setSubscriptionEnableRetry("0")
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String orderId = initTxnDTO.getBody().getOrderId();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();

        OrderDTO orderDTO = new OrderFactory.UPIMandate(merchant, txnToken, initTxnDTO.orderFromBody(), subsId)
                .build();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(orderDTO.getMID(), txnToken, orderDTO.getORDER_ID())
                .setPaymentMode("DEBIT_CARD")
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setSubsId(subsId)
                .setCardInfo("|4444333322221111|111|122035")
                .build();

        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
        Response ptcResponse = processTransactionV1.execute();
        System.out.println(ptcResponse.jsonPath());
        Assert.assertEquals(ptcResponse.jsonPath().getString("body.txnInfo.GATEWAYNAME"),"AXIA");


    }


}

