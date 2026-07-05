package scripts;

import com.paytm.LocalConfig;
import com.paytm.ServerConfigProvider;
import com.paytm.api.*;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.*;
import com.paytm.base.test.Group;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.FastForwardApp.request.FastForwardAppRequest;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.response.FetchPaymentOptResponseDTO;
import com.paytm.dto.NativeDTO.InitTxn.DisablePaymentMode;
import com.paytm.dto.NativeDTO.InitTxn.EnablePaymentMode;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.dto.processTransactionV1.response.ProcessTxnV1Response;
import com.paytm.pages.DirectBankOTPPage;
import com.paytm.pages.NativePlusHoldpayPage;
import com.paytm.pages.ResponsePage;
import io.qameta.allure.*;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.testng.annotations.Test;
import org.awaitility.Awaitility;

import java.io.IOException;

import static com.paytm.base.test.Group.Status.BUG;

@Owner("Gagandeep")
public class DynamicQR extends PGPBaseTest {

    private static final String JSON_POST_URL = "/checkoutpage/nplus_page.jsp?ttype=hold&jsonresp=";
    private final NativePlusHoldpayPage nativePlusHoldpayPage = new NativePlusHoldpayPage();
    private static final String theme = "enhancedweb";
    private static final String DISABLED_PAYMENT_MODE_ERROR_MSG = "{paymentMode} is not allowed for this transaction, kindly use some other payment mode";
    private static final String postConvFlag = "";



    private OrderDTO validateDynamicQRAndCreateOrder(Constants.MerchantType merchantType, User user) {
        pre_requisite:
        {
            PGPHelpers.validate_MerchantPreference(merchantType.getId(), "DYNAMIC_QR_CASHIER", "Y");
            PGPHelpers.validate_MerchantPreference(merchantType.getId(), "DYNAMIC_QR_2FA", "N");
        }

        OrderDTO orderDTO = QRHelper.generateDynamicQROrder(merchantType, theme, user);
        return orderDTO;
    }

    private OrderDTO validateDynamicQRAndCreatePaymentServiceOrder(Constants.MerchantType merchantType, String txnAmount, User user) {
        pre_requisite:
        {
            PGPHelpers.validate_MerchantPreference(merchantType.getId(), "DYNAMIC_QR_CASHIER", "Y");
            PGPHelpers.validate_MerchantPreference(merchantType.getId(), "DYNAMIC_QR_2FA", "N");
        }

        OrderDTO orderDTO = QRHelper.generatePaymentServiceQROrder(merchantType, txnAmount ,theme, user);
        return orderDTO;
    }

    private FetchPaymentOptResponseDTO fetchPaymentOpt(User user, OrderDTO orderDTO, String paymentMode) throws IOException {
        FetchPaymentOptResponseDTO fetchPaymentOptResponse = QRHelper.fetchPaymentOptionResponse(user, orderDTO, false);
        boolean status = QRHelper.validatePaymentModeEnabled(fetchPaymentOptResponse, paymentMode, false);
        Assertions.assertThat(status).as(paymentMode + " is not enabled on fetchPaymentOption API").isTrue();
        return fetchPaymentOptResponse;
    }

    private FetchPaymentOptResponseDTO fetchPaymentOpt(User user, OrderDTO orderDTO, String paymentMode, String emiOption) throws IOException {
        FetchPaymentOptResponseDTO fetchPaymentOptResponse = QRHelper.fetchPaymentOptionResponse(user, orderDTO, false, emiOption);
        boolean status = QRHelper.validatePaymentModeEnabled(fetchPaymentOptResponse, paymentMode, false);
        Assertions.assertThat(status).as(paymentMode + " is not enabled on fetchPaymentOption API").isTrue();
        return fetchPaymentOptResponse;
    }

    private void submitProcessTxnResponseFromReq(ProcessTxnV1Request processTxnV1Request) {
        String jsonForm = QRHelper.executeProcessTransactionV1(processTxnV1Request);
        nativePlusHoldpayPage.
                launch(LocalConfig.MOCK_HOST + JSON_POST_URL).
                fillAndSubmitJsonForm(jsonForm);

    }


    //-------------------------------------Test Cases  Started------------------------------------------


    @Severity(SeverityLevel.CRITICAL)
    @Test(description = "Validate CC transaction using Dynamic QR merchant")
    public void validateCCTxn_UsingDynmQR() throws Exception {
        String paymentMode = "CREDIT_CARD";
        Constants.MerchantType merchantType = Constants.MerchantType.DYNAMICQR;
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = validateDynamicQRAndCreateOrder(merchantType, user);
        fetchPaymentOpt(user, orderDTO, paymentMode);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .build();

        submitProcessTxnResponseFromReq(processTxnV1Request);

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());         //validating txn status
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .AssertAll();
    }

    @Issue("PGP-20655")
    @Severity(SeverityLevel.CRITICAL)
    @Test(description = "test Dynamic QR order success by cc when non-matching website provided",groups = {BUG})
    public void testOrderSuccessByCCWhenNonMatchingWebsiteProvided() throws Exception {
        String paymentMode = "CREDIT_CARD";
        Constants.MerchantType merchantType = Constants.MerchantType.DYNAMICQR;
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = validateDynamicQRAndCreateOrder(merchantType, user);
        fetchPaymentOpt(user, orderDTO, paymentMode);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .setWebsite("nonmatchingwebsite")
                .build();

        submitProcessTxnResponseFromReq(processTxnV1Request);

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());         //validating txn status
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .AssertAll();
    }


    @Issue("SMP1-4603")
    @Severity(SeverityLevel.CRITICAL)
    @Test(description = "Validate DC transaction using Dynamic QR merchant", groups = {BUG})
    public void validateDCTxn_UsingDynmQR() throws Exception {
        String paymentMode = "DEBIT_CARD";
        Constants.MerchantType merchantType = Constants.MerchantType.DYNAMICQR;
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = validateDynamicQRAndCreateOrder(merchantType, user);
        fetchPaymentOpt(user, orderDTO, paymentMode);
        PaymentDTO paymentDTO = new PaymentDTO();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .setCardNum(paymentDTO.getDebitCardNumber())
                .build();

        submitProcessTxnResponseFromReq(processTxnV1Request);

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());         //validating txn status
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .AssertAll();
    }


    @Test(description = "Validate NB transaction using Dynamic QR merchant")
    public void validateNBTxn_UsingDynmQR() throws Exception {
        String paymentMode = "NET_BANKING";
        Constants.MerchantType merchantType = Constants.MerchantType.DYNAMICQR;
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = validateDynamicQRAndCreateOrder(merchantType, user);
        fetchPaymentOpt(user, orderDTO, paymentMode);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .setChannelCode("ICICI")
                .setCardInfo("")
                .build();

        submitProcessTxnResponseFromReq(processTxnV1Request);

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());     //Validating txn status
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("NB")
                .validateRefundAmnt("0.00")
                .AssertAll();
    }


    @Test(description = "Validate Card is saved using Dynamic QR merchant", dependsOnMethods = "validateCCTxn_UsingDynmQR")
    public void validateCardIsSaved_UsingDynmQR() throws Exception {
        String paymentMode = "CREDIT_CARD";
        Constants.MerchantType merchantType = Constants.MerchantType.DYNAMICQR;
        User user = userManager.getForWrite(Label.BASIC);
        PGPHelpers.validate_MerchantPreference(merchantType.getId(), "STORE CARD DETAILS", "YES");
        SavedCardHelpers.deleteSavedCard(user);
        OrderDTO orderDTO = validateDynamicQRAndCreateOrder(merchantType, user);
        fetchPaymentOpt(user, orderDTO, paymentMode);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .setStoreInstrument("1")
                .build();
        submitProcessTxnResponseFromReq(processTxnV1Request);

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());     //Validating txn status
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .AssertAll();

        SavedCardHelpers.validateSavedCardPresence(user);
    }

    @Issue("PGP-15322")
    @Severity(SeverityLevel.CRITICAL)
    @Test(description = "Validate Success transaction using saved card using Dynamic QR merchant", groups = BUG)
    public void validateSuccessTxnUsingSvdCard_UsingDynmQR() throws Exception {
        String paymentMode = "CREDIT_CARD";
        Constants.MerchantType merchantType = Constants.MerchantType.DYNAMICQR;
        User user = userManager.getForWrite(Label.LOGIN);
        PaymentDTO paymentDTO = new PaymentDTO();
        PGPHelpers.validate_MerchantPreference(merchantType.getId(), "STORE CARD DETAILS", "YES");
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        String cardId = SavedCardHelpers.getSavedCardId(user, 0);
        OrderDTO orderDTO = validateDynamicQRAndCreateOrder(merchantType, user);
        fetchPaymentOpt(user, orderDTO, paymentMode);
        String cardInfo = cardId + "||" + paymentDTO.getCvvNumber() + "|";
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .setCardInfo(cardInfo)
                .build();
        submitProcessTxnResponseFromReq(processTxnV1Request);

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());     //Validating txn status
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .AssertAll();
    }

    //TODO: Postpaid mock is not working

//    @Test(description = "Validate Success Postpaid Onboarding transaction using Dynamic QR merchant", enabled = false)
    public void validateSuces_PostpaidOnboarding_UsingDynmQR() throws Exception {
        String paymentMode = "PAYTM_DIGITAL_CREDIT";
        Constants.MerchantType merchantType = Constants.MerchantType.DYNAMICQR;
        User user = userManager.getForWrite(Label.POSTPAIDONBOARDING);
        WalletHelpers.setZeroBalance(user);
        PostpaidHelpers.updatePostpaidUserAttributes(user, PostpaidHelpers.WHITELISTED);
        PostpaidHelpers.updateBalance("1000");
        OrderDTO orderDTO = validateDynamicQRAndCreateOrder(merchantType, user);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(false))
                .setMid(orderDTO.getMID())
                .setToken(user.ssoToken())
                .setPostpaidOnboardingSupported(String.valueOf(true))
                .build();
        FetchPaymentOptResponseDTO fetchPaymentOptResponse = PGPHelpers.executeFetchPaymentOpt(orderDTO.getMID(), orderDTO.getORDER_ID(), fetchPaymentOptionsDTO, false);
        boolean status = QRHelper.validatePaymentModeEnabled(fetchPaymentOptResponse, paymentMode, false);
        Assertions.assertThat(status).as(paymentMode + " is not enabled on fetchPaymentOption API").isTrue();
        QRHelper.validatePostPaidOnboardingEnabled(fetchPaymentOptResponse, paymentMode);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .setCardInfo("")
                .build();
        submitProcessTxnResponseFromReq(processTxnV1Request);
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());     //Validating txn status
        txnStatus.executeUntilNotPending();
    }


    @Test(description = "Validate Success Peon of CC transaction using Dynamic QR merchant", dependsOnMethods = "validateCCTxn_UsingDynmQR")
    public void validateSucesPeon_UsingDynmQR() throws Exception {
        String paymentMode = "CREDIT_CARD";
        Constants.MerchantType merchantType = Constants.MerchantType.DYNAMICQR;
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = validateDynamicQRAndCreateOrder(merchantType, user);
        fetchPaymentOpt(user, orderDTO, paymentMode);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .build();

        submitProcessTxnResponseFromReq(processTxnV1Request);

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());         //validating txn status
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .AssertAll();
        Peon peon = new Peon(orderDTO.getORDER_ID());
        peon.executeUntilGetResponse();
    }


    @Test(description = "Validate Success PPBL transaction using Dynamic QR merchant")
    public void validateSucesPPBLTxn_UsingDynmQR() throws Exception {
        String paymentMode = "PPBL";
        Constants.MerchantType merchantType = Constants.MerchantType.DYNAMICQR;
        User user = userManager.getForRead(Label.PPBL);
        OrderDTO orderDTO = validateDynamicQRAndCreateOrder(merchantType, user);
        fetchPaymentOpt(user, orderDTO, paymentMode);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setCardInfo("")
                .setChannelCode("")
                .setPaymentMode(paymentMode)
                .setAuthMode("USRPWD")
                .setMpin("5335")
                .build();
        submitProcessTxnResponseFromReq(processTxnV1Request);
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());         //validating txn status
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("NB")
                .validateRefundAmnt("0.00")
                .AssertAll();
    }

    @Test(description = "Validate Success transaction using EMI using Dynamic QR merchant")
    public void validateSuccessTxnUsingEMI_UsingDynmQR() throws Exception {
        String paymentMode = "EMI";
        Constants.MerchantType merchantType = Constants.MerchantType.DYNAMICQR;
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = validateDynamicQRAndCreateOrder(merchantType, user);
        fetchPaymentOpt(user, orderDTO, paymentMode);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .setPlanId("HDFC|3")
                .setEmiType("CREDIT_CARD")
                .setCardNum("4718650100010336")
                .build();
        submitProcessTxnResponseFromReq(processTxnV1Request);

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());     //Validating txn status
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validatePaymentMode(paymentMode)
                .AssertAll();
    }

    @Test(description = "Validate Success transaction using Zero Cost EMI using Dynamic QR merchant")
    public void validateSuccessTxnUsingZeroCostEMI_UsingDynmQR() throws Exception {
        String paymentMode = "EMI";
        String emiOption = "0CostEMI:8565560_" + LocalConfig.ZERO_COST_EMI;
        Constants.MerchantType merchantType = Constants.MerchantType.DYNAMICQR;
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = validateDynamicQRAndCreateOrder(merchantType, user);
        fetchPaymentOpt(user, orderDTO, paymentMode, emiOption);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .setPlanId("HDFC|1")
                .setEmiType("CREDIT_CARD")
                .setCardNum("4718650100010336")
                .build();
        submitProcessTxnResponseFromReq(processTxnV1Request);
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());     //Validating txn status
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validatePaymentMode(paymentMode)
                .AssertAll();
    }

    @Test(description = "Validate Success transaction using UPI using Dynamic QR merchant")
    public void validateSuccessTxnUsingUPI_UsingDynmQR() throws Exception {
        String paymentMode = "UPI";
        Constants.MerchantType merchantType = Constants.MerchantType.DYNAMICQR;
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = validateDynamicQRAndCreateOrder(merchantType, user);
        fetchPaymentOpt(user, orderDTO, paymentMode);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .build();
        submitProcessTxnResponseFromReq(processTxnV1Request);

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());     //Validating txn status
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validatePaymentMode(paymentMode)
                .AssertAll();
    }
    @Epic(Constants.Sprint.SPRINT31_1)
    @Feature("PGP-19896")
    @Owner("Tarun")
    @Test(description = "Validate Success Balance transaction using Fast forward API using Dynamic QR merchant")
    public void validateSuccessTxnUsingFastForwardAPI_UsingDynmQR() throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.DYNAMICQR;
        User user = userManager.getForWrite(Label.LOGIN);
        WalletHelpers.modifyBalance(user,3.0);
        OrderDTO orderDTO = validateDynamicQRAndCreateOrder(merchantType, user);

        FastForwardAppRequest fastForwardAppRequest = new FastForwardAppRequest
                .Builder(merchantType.getId(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setPaymentMode("PPI")
                .setReqType("CLW_APP_PAY")
                .setToken(user.ssoToken())
                .setTokenType("SSO")
                .setCustomerId(user.custId())
                .setOrderAdditionalInfoMCC("mc1234")
                .setOrderAdditionalInfoMName("automation")
                .setOrderAdditionalInfoMLogo("logo")
                .build();

        JsonPath jsonPath = new FastForward(fastForwardAppRequest).execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(jsonPath.getString("body.resultInfo.resultStatus"))
                .isEqualToIgnoringCase("TXN_SUCCESS");
        softly.assertThat(jsonPath.getString("body.txnAmount"))
                .isEqualToIgnoringCase(orderDTO.getTXN_AMOUNT());
        softly.assertThat(jsonPath.getString("body.paymentMode"))
                .isEqualToIgnoringCase("PPI");
        softly.assertThat(jsonPath.getString("body.extendInfo.requestType"))
                .isEqualToIgnoringCase("DYNAMIC_QR");
        softly.assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());   //Validating txn status
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validatePaymentMode("PPI")
                .AssertAll();
    }

    @Test(description = "Verify the response when incorrect sso token is provided")
    public void incorrectSSOToken() throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.DYNAMICQR;
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = validateDynamicQRAndCreateOrder(merchantType, user);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(false))
                .setMid(orderDTO.getMID())
                .setToken("ABCD")
                .build();

        FetchPaymentOptResponseDTO fetchPaymentOptResponseDTO = PGPHelpers.executeFetchPaymentOpt(orderDTO.getMID(), orderDTO.getORDER_ID(), fetchPaymentOptionsDTO, false);
        Assertions.assertThat(fetchPaymentOptResponseDTO.getBody().getResultInfo().getResultMsg()).as("").isEqualTo("SSO Token is invalid");

    }

    //TODO: Need to fix.
    @Test(description = "Verify the response when empty txn token is provided")
    public void emptyTxnToken() throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.DYNAMICQR;
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = validateDynamicQRAndCreateOrder(merchantType, user);

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("TXN_TOKEN")
                .setGenerateOrderId(Boolean.toString(false))
                .setMid(orderDTO.getMID())
                .setToken("")
                .build();

        FetchPaymentOptResponseDTO fetchPaymentOptResponseDTO = PGPHelpers.executeFetchPaymentOpt(orderDTO.getMID(), orderDTO.getORDER_ID(), fetchPaymentOptionsDTO, false);
        Assertions.assertThat(fetchPaymentOptResponseDTO.getBody().getResultInfo().getResultMsg()).as("").isEqualTo("Your Session has expired.");

    }

    @Test(description = "Verify the response when incorrect mid is provided")
    public void incorrectMID() throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.DYNAMICQR;
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = validateDynamicQRAndCreateOrder(merchantType, user);

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(false))
                .setMid("123445")
                .setToken(user.ssoToken())
                .build();

        FetchPaymentOptResponseDTO fetchPaymentOptResponseDTO = PGPHelpers.executeFetchPaymentOpt("123445", orderDTO.getORDER_ID(), fetchPaymentOptionsDTO, false);
        Assertions.assertThat(fetchPaymentOptResponseDTO.getBody().getResultInfo().getResultMsg()).as("Incorrect response").isEqualTo("Mid is invalid");

    }

    @Issue("PGP-16460")
    @Test(groups = BUG, description = "Verify the response when incorrect emi option is provided")
    public void incorrectEMIOption() throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.DYNAMICQR;
        User user = userManager.getForRead(Label.BASIC);
        String emiOption = "abcd";
        OrderDTO orderDTO = validateDynamicQRAndCreateOrder(merchantType, user);
        FetchPaymentOptResponseDTO fetchPaymentOptResponse = QRHelper.fetchPaymentOptionResponse(user, orderDTO, false, emiOption);
        Assertions.assertThat(fetchPaymentOptResponse.getBody().getResultInfo().getResultMsg()).as("Incorrect response").isEqualTo("Request Parameters are not valid");

    }

    @Issue("PGP-16567")
    @Test(groups = BUG, description = "Verify the response when disable payment mode CREDIT_CARD is provided")
    public void validateDisablePaymentModeTxnCC_UsingDynamicQR() throws Exception {
        String paymentMode = "CREDIT_CARD";
        Constants.MerchantType merchantType = Constants.MerchantType.DYNAMICQR;
        User user = userManager.getForRead(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        OrderDTO orderDTO = validateDynamicQRAndCreateOrder(merchantType, user);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(false))
                .setMid(orderDTO.getMID())
                .setToken(user.ssoToken())
                .setDisablePaymentMode(new DisablePaymentMode[]{new DisablePaymentMode().setMode(paymentMode)})
                .build();

        FetchPaymentOptResponseDTO fetchPaymentOptResponse = PGPHelpers.executeFetchPaymentOpt(orderDTO.getMID(), orderDTO.getORDER_ID(), fetchPaymentOptionsDTO, false);
        String orderId = fetchPaymentOptResponse.getBody().getOrderId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(), orderId, orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .build();
        ProcessTxnV1Response processTxnV1Response = PGPHelpers.executeProcessTxn(processTxnV1Request);
        Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultMsg()).as("Disable Payment mode is not working").isEqualTo(DISABLED_PAYMENT_MODE_ERROR_MSG.replace("{paymentMode}", "Credit Card"));
        Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultStatus()).as("Result status is not as expected").isEqualTo("F");

    }

    @Issue("PGP-16569")
    @Test(groups = BUG, description = "Verify the response when enable payment mode CREDIT CARD is provided")
    public void validateEnablePaymentModeTxnDC_UsingDynamicQR() throws Exception {
        String paymentMode = "CREDIT_CARD";
        Constants.MerchantType merchantType = Constants.MerchantType.DYNAMICQR;
        User user = userManager.getForRead(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        OrderDTO orderDTO = validateDynamicQRAndCreateOrder(merchantType, user);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(false))
                .setMid(orderDTO.getMID())
                .setToken(user.ssoToken())
                .setEnablePaymentMode(new EnablePaymentMode[]{new EnablePaymentMode().setMode(paymentMode)})
                .build();

        FetchPaymentOptResponseDTO fetchPaymentOptResponse = PGPHelpers.executeFetchPaymentOpt(orderDTO.getMID(), orderDTO.getORDER_ID(), fetchPaymentOptionsDTO, false);
        String orderId = fetchPaymentOptResponse.getBody().getOrderId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(), orderId, orderDTO.getTXN_AMOUNT())
                .setPaymentMode("DEBIT_CARD")
                .setCardNum(PaymentDTO.MAESTRO_DEBIT_CARD_NUMBER)
                .build();
        ProcessTxnV1Response processTxnV1Response = PGPHelpers.executeProcessTxn(processTxnV1Request);
        Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultMsg()).as("Enable Payment mode is not working").isEqualTo(DISABLED_PAYMENT_MODE_ERROR_MSG.replace("{paymentMode}", "Debit Card"));
        Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultStatus()).as("Result status is not as expected").isEqualTo("F");

    }


    @Test(description = "Validate refund is happening in CC")
    public void validateRefundUsingCC() throws Exception {
        String paymentMode = "CREDIT_CARD";
        Constants.MerchantType merchantType = Constants.MerchantType.DYNAMICQR;
        PGPHelpers.validateRefundAllowedWithChecksum(merchantType.getId());
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        OrderDTO orderDTO = validateDynamicQRAndCreateOrder(merchantType, user);
        fetchPaymentOpt(user, orderDTO, paymentMode);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .build();

        submitProcessTxnResponseFromReq(processTxnV1Request);
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());         //validating txn status
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .AssertAll();

        Test:
        {
            PGPHelpers.initiateRefundRequest(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT(), txnStatus.getResponse().getTXNID(), postConvFlag);
            PGPHelpers.getRefundStatus(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), true)
                    .validateSuccessRefund()
                    .assertAll();
        }
    }

    @Issue("SMP1-4603")
    @Test(description = "Validate refund is happening in DC", groups = {BUG})
    public void validateRefundUsingDC() throws Exception {
        String paymentMode = "DEBIT_CARD";
        Constants.MerchantType merchantType = Constants.MerchantType.DYNAMICQR;
        PGPHelpers.validateRefundAllowedWithChecksum(merchantType.getId());
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        OrderDTO orderDTO = validateDynamicQRAndCreateOrder(merchantType, user);
        fetchPaymentOpt(user, orderDTO, paymentMode);
        PaymentDTO paymentDTO = new PaymentDTO();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .setCardNum(paymentDTO.getDebitCardNumber())
                .build();

        submitProcessTxnResponseFromReq(processTxnV1Request);
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());         //validating txn status
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .AssertAll();

        Test:
        {
            PGPHelpers.initiateRefundRequest(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT(), txnStatus.getResponse().getTXNID(), postConvFlag);
            PGPHelpers.getRefundStatus(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), true)
                    .validateSuccessRefund()
                    .assertAll();
        }

    }

//    @Test(description = "Validate refund is happening in NB", enabled = false)
    public void validateRefundUsingNB() throws Exception {//TODO need to create refund mock
        String paymentMode = "NET_BANKING";
        Constants.MerchantType merchantType = Constants.MerchantType.DYNAMICQR;
        PGPHelpers.validateRefundAllowedWithChecksum(merchantType.getId());
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        OrderDTO orderDTO = validateDynamicQRAndCreateOrder(merchantType, user);
        fetchPaymentOpt(user, orderDTO, paymentMode);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .setChannelCode("ICICI")
                .setCardInfo("")
                .build();

        submitProcessTxnResponseFromReq(processTxnV1Request);
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());         //validating txn status
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("NB")
                .validateRefundAmnt("0.00")
                .AssertAll();

        Test:
        {
            PGPHelpers.initiateRefundRequest(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT(), txnStatus.getResponse().getTXNID(), postConvFlag);
            PGPHelpers.getRefundStatus(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), true)
                    .validateSuccessRefund()
                    .assertAll();
        }

    }

    @Test(description = "Validate refund is happening in SAVED CARD")
    public void validateRefundUsingSavedCard() throws Exception {
        String paymentMode = "CREDIT_CARD";
        Constants.MerchantType merchantType = Constants.MerchantType.DYNAMICQR;
        PGPHelpers.validateRefundAllowedWithChecksum(merchantType.getId());
        User user = userManager.getForWrite(PGPBaseTest.Label.LOGIN);
        PaymentDTO paymentDTO = new PaymentDTO();
        PGPHelpers.validate_MerchantPreference(merchantType.getId(), "STORE CARD DETAILS", "YES");
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        String cardId = SavedCardHelpers.getSavedCardId(user, 0);
        OrderDTO orderDTO = validateDynamicQRAndCreateOrder(merchantType, user);
        fetchPaymentOpt(user, orderDTO, paymentMode);
        String cardInfo = cardId + "||" + paymentDTO.getCvvNumber() + "|";
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .setCardInfo(cardInfo)
                .build();
        submitProcessTxnResponseFromReq(processTxnV1Request);

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());     //Validating txn status
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .AssertAll();

        Test:
        {
            PGPHelpers.initiateRefundRequest(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT(), txnStatus.getResponse().getTXNID(), postConvFlag);
            PGPHelpers.getRefundStatus(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), true)
                    .validateSuccessRefund()
                    .assertAll();
        }

    }

    @Test(description = "Validate Refund via PPBL")
    public void validatePPBLRefund_UsingDynmQR() throws Exception {
        String paymentMode = "PPBL";
        Constants.MerchantType merchantType = Constants.MerchantType.DYNAMICQR;
        PGPHelpers.validateRefundAllowedWithChecksum(merchantType.getId());
        User user = userManager.getForRead(PGPBaseTest.Label.PPBL);
        OrderDTO orderDTO = validateDynamicQRAndCreateOrder(merchantType, user);
        fetchPaymentOpt(user, orderDTO, paymentMode);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setCardInfo("")
                .setChannelCode("")
                .setPaymentMode(paymentMode)
                .setAuthMode("USRPWD")
                .setMpin("5335")
                .build();
        submitProcessTxnResponseFromReq(processTxnV1Request);
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());         //validating txn status
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("NB")
                .validateRefundAmnt("0.00")
                .AssertAll();

        Test:
        {
            PGPHelpers.initiateRefundRequest(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT(), txnStatus.getResponse().getTXNID(), postConvFlag);
            PGPHelpers.getRefundStatus(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), true)
                    .validateSuccessRefund()
                    .assertAll();
        }
    }

//    @Test(description = "Validate refund via UPI", enabled = false)
    public void validateRefundViaUPI() throws Exception {//TODO need to create refund mock
        String paymentMode = "UPI";
        Constants.MerchantType merchantType = Constants.MerchantType.DYNAMICQR;
        PGPHelpers.validateRefundAllowedWithChecksum(merchantType.getId());
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = validateDynamicQRAndCreateOrder(merchantType, user);
        fetchPaymentOpt(user, orderDTO, paymentMode);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .build();
        submitProcessTxnResponseFromReq(processTxnV1Request);

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());     //Validating txn status
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validatePaymentMode(paymentMode)
                .AssertAll();

        Test:
        {
            PGPHelpers.initiateRefundRequest(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT(), txnStatus.getResponse().getTXNID(), postConvFlag);
            PGPHelpers.getRefundStatus(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), true)
                    .validateSuccessRefund()
                    .assertAll();
        }
    }


    @Epic(Constants.Sprint.SPRINT29_2)
    @Feature("PGP-17769")
    @Test(description = "Validate Success transaction using DIRECT BANK FOR HDFO using Dynamic QR merchant")
    public void validateHDFODirectPageOnDynamicFLow() throws Exception {
        String paymentMode = "CREDIT_CARD";
        Constants.MerchantType merchantType = Constants.MerchantType.DYNAMICQR_DIRECT_HDFO;
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = validateDynamicQRAndCreateOrder(merchantType, user);
        fetchPaymentOpt(user, orderDTO, paymentMode);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .build();
        submitProcessTxnResponseFromReq(processTxnV1Request);

        DirectBankOTPPage directBankOTPPage = new DirectBankOTPPage();
        directBankOTPPage.waitUntilLoads();
        directBankOTPPage.submitOtp(PaymentDTO.OTP);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());     //Validating txn status
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validatePaymentMode("CC")
                .AssertAll();
    }

    @Epic(Constants.Sprint.SPRINT29_2)
    @Feature("PGP-17769")
    @Issue("PGP-22045")
    @Test(description = "Validate Success transaction using DIRECT BANK FOR ICICI using Dynamic QR merchant", groups = Group.Status.BUG)
    public void validateICIOirectPageOnDynamicFLow() throws Exception {
        String paymentMode = "CREDIT_CARD";
        Constants.MerchantType merchantType = Constants.MerchantType.DYNAMICQR_DIRECT_ICIO;
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = validateDynamicQRAndCreateOrder(merchantType, user);
        fetchPaymentOpt(user, orderDTO, paymentMode);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .setCardNum(PaymentDTO.ICICI_CREDIT_CARD_NUMBER)
                .build();
        submitProcessTxnResponseFromReq(processTxnV1Request);

        DirectBankOTPPage directBankOTPPage = new DirectBankOTPPage();
        directBankOTPPage.waitUntilLoads();
        directBankOTPPage.submitOtp("111212");
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());     //Validating txn status
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validatePaymentMode("CC")
                .AssertAll();
    }

    @Epic(Constants.Sprint.SPRINT29_2)
    @Feature("PGP-17769")
    @Test(description = "Validate Success transaction using DIRECT BANK FOR HDFO using Dynamic QR merchant when Order Already Created Flag is true")
    public void validateHDFODirectPageOnDynamicFLowForOrderAlreadyCreatedFlag() throws Exception {
        String paymentMode = "CREDIT_CARD";
        Constants.MerchantType merchantType = Constants.MerchantType.DYNAMICQR_DIRECT_HDFO;
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = validateDynamicQRAndCreateOrder(merchantType, user);
        fetchPaymentOpt(user, orderDTO, paymentMode);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .setExtendInfoOrderAlreadyCreated(true)
                .build();
        submitProcessTxnResponseFromReq(processTxnV1Request);

        DirectBankOTPPage directBankOTPPage = new DirectBankOTPPage();
        directBankOTPPage.waitUntilLoads();
        directBankOTPPage.submitOtp(PaymentDTO.OTP);

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());     //Validating txn status
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validatePaymentMode("CC")
                .AssertAll();
    }

    @Epic(Constants.Sprint.SPRINT29_2)
    @Feature("PGP-17769")
    @Issue("PGP-22045")
    @Test(description = "Validate Success transaction using DIRECT BANK FOR ICICI using Dynamic QR merchant when Order Already Created Flag is true", groups = Group.Status.BUG)
    public void validateICIOirectPageOnDynamicFLowForOrderAlreadyCreatedFlagIsTrue() throws Exception {
        String paymentMode = "CREDIT_CARD";
        Constants.MerchantType merchantType = Constants.MerchantType.DYNAMICQR_DIRECT_ICIO;
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = validateDynamicQRAndCreateOrder(merchantType, user);
        fetchPaymentOpt(user, orderDTO, paymentMode);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .setExtendInfoOrderAlreadyCreated(true)
                .setCardNum(PaymentDTO.ICICI_CREDIT_CARD_NUMBER)
                .build();
        submitProcessTxnResponseFromReq(processTxnV1Request);

        DirectBankOTPPage directBankOTPPage = new DirectBankOTPPage();
        directBankOTPPage.waitUntilLoads();
        directBankOTPPage.submitOtp("111212");

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());     //Validating txn status
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validatePaymentMode("CC")
                .AssertAll();
    }


    @Epic(Constants.Sprint.SPRINT29_2)
    @Feature("PGP-17769")
    @Test(description = "Validate Success transaction using DIRECT BANK FOR ICICI using Dynamic QR merchant when Order is Created Through Payment Service", groups = Group.Status.BUG)
    public void validateHDFODirectPageWhenOrderIsCreatedWithPaymentService() throws Exception {
        String paymentMode = "CREDIT_CARD";
        String txnAmount = "2.00";
        Constants.MerchantType merchantType = Constants.MerchantType.DYNAMICQR_DIRECT_HDFO;
        User user = userManager.getForRead(Label.LOGIN);
        OrderDTO orderDTO = validateDynamicQRAndCreatePaymentServiceOrder(merchantType, txnAmount, user);
        fetchPaymentOpt(user, orderDTO, paymentMode);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .build();

        submitProcessTxnResponseFromReq(processTxnV1Request);

        DirectBankOTPPage directBankOTPPage = new DirectBankOTPPage();
        directBankOTPPage.waitUntilLoads();
        directBankOTPPage.submitOtp("111212");

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validatePaymentMode("CC")
                .AssertAll();
    }



    @Epic(Constants.Sprint.SPRINT29_2)
    @Feature("PGP-17769")
    @Issue("PGP-22045")
    @Test(description = "Validate Success transaction using DIRECT BANK FOR ICICI using Dynamic QR merchant when Order is Created through Payment Service", groups = Group.Status.BUG)
    public void validateICIODirectPageWhenOrderIsCreatedWithPaymentService() throws Exception {
        String paymentMode = "CREDIT_CARD";
        String txnAmount = "2.00";
        Constants.MerchantType merchantType = Constants.MerchantType.DYNAMICQR_DIRECT_ICIO;
        User user = userManager.getForRead(Label.LOGIN);
        OrderDTO orderDTO = validateDynamicQRAndCreatePaymentServiceOrder(merchantType, txnAmount, user);
        fetchPaymentOpt(user, orderDTO, paymentMode);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .setCardNum(PaymentDTO.ICICI_CREDIT_CARD_NUMBER)
                .build();

        submitProcessTxnResponseFromReq(processTxnV1Request);

        DirectBankOTPPage directBankOTPPage = new DirectBankOTPPage();
        directBankOTPPage.waitUntilLoads();
        directBankOTPPage.submitOtp("111212");

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validatePaymentMode("CC")
                .AssertAll();
    }
}

