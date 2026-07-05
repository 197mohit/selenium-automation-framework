package scripts;

import com.paytm.LocalConfig;
import com.paytm.api.FastForward;
import com.paytm.api.ProcessTransactionV1;
import com.paytm.api.TxnStatus;
import com.paytm.api.nativeAPI.FetchEMIDetail;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.*;
import com.paytm.base.test.Group;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.FastForwardApp.request.FastForwardAppRequest;
import com.paytm.dto.FastForwardRequestDTO;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.response.FetchPaymentOptResponseDTO;
import com.paytm.dto.NativeDTO.InitTxn.DisablePaymentMode;
import com.paytm.dto.NativeDTO.InitTxn.EnablePaymentMode;
import com.paytm.dto.NativeDTO.fetchEMIDetail.FetchEMIDetailRequest;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.dto.processTransactionV1.response.ProcessTxnV1Response;
import com.paytm.framework.utils.DatabaseUtil;
import com.paytm.framework.utils.RedisUtil;
import com.paytm.pages.NativePlusHoldpayPage;
import com.paytm.pages.ResponsePage;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.testng.Assert;
import org.testng.annotations.*;

import java.io.IOException;

@Deprecated
@Owner("Gagandeep")
public class StaticQR extends PGPBaseTest {

    boolean ff4jFlagEnabled;

    @Factory(dataProvider = "dataMethod")
    StaticQR(boolean ff4jFlagEnabled) {
                this.ff4jFlagEnabled = ff4jFlagEnabled;
    }

    @DataProvider(name = "dataMethod")
    static Object[][] dataMethod() {
        return new Object[][]{
                {true}, {false}
        };
    }

    @BeforeClass
   public void setFf4jFlag() {
        int isEnabled = this.ff4jFlagEnabled ? 1 : 0;
        String queryForExp = "UPDATE FF4J_FEATURES SET ENABLE = " + isEnabled + " WHERE FEAT_UID = 'paymentRetryInfo';";
        DatabaseUtil.getInstance().executeUpdateQuery(LocalConfig.PGP_DB_CONNECTION_URL, queryForExp);
        RedisUtil.getInstance().getConnection(LocalConfig.PG_REDIS_URI).del("FF4J_FEATURE_paymentRetryInfo");
        TRANSACTIONAL_REDIS_CLUSTER().del("FF4J_FEATURE_paymentRetryInfo");
    }

    private static final String JSON_POST_URL = "/checkoutpage/nplus_page.jsp?ttype=hold&jsonresp=";
    private static final String DISABLED_PAYMENT_MODE_ERROR_MSG = "{paymentMode} is not allowed for this transaction, kindly use some other payment mode";
    private static final String theme = "enhancedweb";
    private final NativePlusHoldpayPage nativePlusHoldpayPage = new NativePlusHoldpayPage();

    private OrderDTO validateOfflineQRAndCreateOrder(Constants.MerchantType merchantType, User user) {
        pre_requisite:
        {
            PGPHelpers.validate_MerchantPreference(merchantType.getId(), "DYNAMIC_QR_CASHIER", "N");
            PGPHelpers.validate_MerchantPreference(merchantType.getId(), "DYNAMIC_QR_2FA", "N");
        }

        OrderDTO orderDTO = new OrderFactory.Hybrid(merchantType, theme, user)
                .setTXN_AMOUNT("2.00")
                .setSSO_TOKEN("")
                .build();
        return orderDTO;
    }

    private FetchPaymentOptResponseDTO fetchPaymentOpt(User user, OrderDTO orderDTO, String paymentMode) throws IOException {
        FetchPaymentOptResponseDTO fetchPaymentOptResponse = QRHelper.fetchPaymentOptionResponse(user, orderDTO, true);
        boolean status = QRHelper.validatePaymentModeEnabled(fetchPaymentOptResponse, paymentMode, false);
        Assertions.assertThat(status).as(paymentMode + " is not enabled on fetchPaymentOption API").isTrue();
        return fetchPaymentOptResponse;
    }

    //Overloaded Method
    private FetchPaymentOptResponseDTO fetchPaymentOpt(User user, OrderDTO orderDTO, String paymentMode,String emiOption) throws IOException {
        FetchPaymentOptResponseDTO fetchPaymentOptResponse = QRHelper.fetchPaymentOptionResponse(user, orderDTO, true,emiOption);
        boolean status = QRHelper.validatePaymentModeEnabled(fetchPaymentOptResponse, paymentMode, false);
        Assertions.assertThat(status).as(paymentMode + " is not enabled on fetchPaymentOption API").isTrue();
        return fetchPaymentOptResponse;
    }

    private void submitProcessTxnResponseFromReq(ProcessTxnV1Request processTxnV1Request) {
        String jsonForm = QRHelper.executeProcessTransactionV1(processTxnV1Request);
        nativePlusHoldpayPage.
                launch(LocalConfig.MOCK_HOST +JSON_POST_URL).
                fillAndSubmitJsonForm(jsonForm);

    }

//-------------------------------------Test Cases  Started------------------------------------------

    @Test(description = "Validate Success CC transaction using static QR merchant")
    public void validateCCTxn_UsingStaticQR() throws Exception {
        String paymentMode = "CREDIT_CARD";

        Constants.MerchantType merchantType = Constants.MerchantType.Hybrid;
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = validateOfflineQRAndCreateOrder(merchantType,user);
        FetchPaymentOptResponseDTO fetchPaymentOptResponse = fetchPaymentOpt(user,orderDTO,paymentMode);
        String orderId = fetchPaymentOptResponse.getBody().getOrderId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(), orderId, orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .build();

        submitProcessTxnResponseFromReq(processTxnV1Request);

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderId);         //validating txn status
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
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

    @Test(description = "Validate Retry Info on Fast forward API response using Static QR merchant")
    public void validateRetryInfoUsingFastForwardAPI_UsingStaticQR() throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.Hybrid;
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = validateOfflineQRAndCreateOrder(merchantType, user);
        WalletHelpers.modifyBalance(user,Double.parseDouble(orderDTO.getTXN_AMOUNT()) - 1);

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

        if(ff4jFlagEnabled)
        softly.assertThat(jsonPath.getString("body.retryInfo"))
                .isNotNull();
        else {
            softly.assertThat(jsonPath.getString("body.retryInfo"))
                    .isNull();
        }
        softly.assertAll();
    }

    @Issue("PGP-18729")
    @Test(groups = {Group.Status.BUG,"smoke"}, description = "Validate txn should get succeeded using SCW merchant")
    public void validateAddMoneyTxnUsingSCW() throws Exception {
        String paymentMode = "CREDIT_CARD";

        Constants.MerchantType merchantType = Constants.MerchantType.AddMoneyMP;
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = validateOfflineQRAndCreateOrder(merchantType,user);
        FetchPaymentOptResponseDTO fetchPaymentOptResponse = fetchPaymentOpt(user,orderDTO,paymentMode);
        String orderId = fetchPaymentOptResponse.getBody().getOrderId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(), orderId, orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .build();

        submitProcessTxnResponseFromReq(processTxnV1Request);

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderId);         //validating txn status
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
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


    @Test(description = "Validate Success DC transaction using static QR merchant")
    public void validateDCTxn_UsingStaticQR() throws Exception {
        String paymentMode = "DEBIT_CARD";

        Constants.MerchantType merchantType = Constants.MerchantType.Hybrid;
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = validateOfflineQRAndCreateOrder(merchantType,user);
        FetchPaymentOptResponseDTO fetchPaymentOptResponse = fetchPaymentOpt(user,orderDTO,paymentMode);
        String orderId = fetchPaymentOptResponse.getBody().getOrderId();
        PaymentDTO paymentDTO=new PaymentDTO();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(), orderId, orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .setCardNum(paymentDTO.getDebitCardNumber())
                .build();
        submitProcessTxnResponseFromReq(processTxnV1Request);
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderId);         //validating txn status
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
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

    @Issue("PGP-20655")
    @Test(description = "test Static QR order success by dc when non-matching website provided", groups = Group.Status.BUG)
    public void testOrderSuccessByDCWhenNonMatchingWebsiteProvided() throws Exception {
        String paymentMode = "DEBIT_CARD";
        Constants.MerchantType merchantType = Constants.MerchantType.STATIC_PEON_DISABLED;
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = validateOfflineQRAndCreateOrder(merchantType,user);
        FetchPaymentOptResponseDTO fetchPaymentOptResponse = fetchPaymentOpt(user,orderDTO,paymentMode);
        String orderId = fetchPaymentOptResponse.getBody().getOrderId();
        PaymentDTO paymentDTO=new PaymentDTO();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(), orderId, orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .setCardNum(paymentDTO.getDebitCardNumber())
                .setWebsite("nonmatchingwebsite")
                .build();
        submitProcessTxnResponseFromReq(processTxnV1Request);
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderId);         //validating txn status
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
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

    @Test(description = "Validate NB transaction using Static QR merchant")
    public void validateNBTxn_UsingStaticQR() throws Exception {
        String paymentMode = "NET_BANKING";

        Constants.MerchantType merchantType = Constants.MerchantType.Hybrid;
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = validateOfflineQRAndCreateOrder(merchantType,user);
        FetchPaymentOptResponseDTO fetchPaymentOptResponse = fetchPaymentOpt(user,orderDTO,paymentMode);
        String orderId = fetchPaymentOptResponse.getBody().getOrderId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(), orderId, orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .setChannelCode("ICICI")
                .setCardInfo("")
                .build();
        submitProcessTxnResponseFromReq(processTxnV1Request);

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderId);     //Validating txn status
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
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

    @Test(description = "Validate PCF NB ICICI transaction using Static QR merchant")
    public void validatePCFNBTxn_UsingStaticQR() throws Exception {
        String paymentMode = "NET_BANKING";

        Constants.MerchantType merchantType = Constants.MerchantType.PPBL_NB_PCF;
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = validateOfflineQRAndCreateOrder(merchantType,user);
        FetchPaymentOptResponseDTO fetchPaymentOptResponse = fetchPaymentOpt(user,orderDTO,paymentMode);
        String orderId = fetchPaymentOptResponse.getBody().getOrderId();
        Double expectedChargeFeeAmt = convenienceFeeCalculator(Double.valueOf(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT())), 0, 4.00, "NB");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(), orderId, orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .setChannelCode("ICICI")
                .setCardInfo("")
                .build();
        submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
        responsePage.validateChargeAmount(expectedChargeFeeAmt.toString())
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderId);     //Validating txn status
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
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

    @Test(description = "Validate Success EMI transaction using static QR merchant")//BAJAJFN is not supported for Offline EMI Txns
    public void validateEMITxn_UsingStaticQR() throws Exception {
        String paymentMode = "EMI";
        Constants.MerchantType merchantType = Constants.MerchantType.Hybrid;
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = validateOfflineQRAndCreateOrder(merchantType,user);
        FetchPaymentOptResponseDTO fetchPaymentOptResponse = fetchPaymentOpt(user,orderDTO,paymentMode);
        String orderId = fetchPaymentOptResponse.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(), orderId, orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .setPlanId("HDFC|3")
                .setEmiType("CREDIT_CARD")
                .setCardNum("4718650100010336")
                .build();

        submitProcessTxnResponseFromReq(processTxnV1Request);

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderId);         //validating txn status
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .AssertAll();
    }



    @Test(description = "Validate Success Zero Cost EMI transaction using static QR merchant")//BAJAJFN is not supported for Offline EMI Txns
    public void validateZeroCostEMITxn_UsingStaticQR() throws Exception {
        String paymentMode = "EMI";
        String emiOption = "0CostEMI:8565560_" + LocalConfig.ZERO_COST_EMI;
        Constants.MerchantType merchantType = Constants.MerchantType.Hybrid;
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = validateOfflineQRAndCreateOrder(merchantType,user);
        FetchPaymentOptResponseDTO fetchPaymentOptResponse = fetchPaymentOpt(user,orderDTO,paymentMode,emiOption);
        String orderId = fetchPaymentOptResponse.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(), orderId, orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .setPlanId("HDFC|1")
                .setEmiType("CREDIT_CARD")
                .setCardNum("4718650100010336")
                .build();

        submitProcessTxnResponseFromReq(processTxnV1Request);

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderId);         //validating txn status
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .AssertAll();
    }


    @Test(description = "Validate Success UPI transaction using static QR merchant")
    public void validateUPITxn_UsingStaticQR() throws Exception {
        String paymentMode = "UPI";
        Constants.MerchantType merchantType = Constants.MerchantType.Hybrid;
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = validateOfflineQRAndCreateOrder(merchantType,user);
        FetchPaymentOptResponseDTO fetchPaymentOptResponse = fetchPaymentOpt(user,orderDTO,paymentMode);
        String orderId = fetchPaymentOptResponse.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(), orderId, orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .build();
        submitProcessTxnResponseFromReq(processTxnV1Request);

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderId);         //validating txn status
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("UPI")
                .validateRefundAmnt("0.00")
                .AssertAll();
    }

    @Test(description = "Validate Success Balance transaction using static QR merchant using Fast forward API")
    public void validateBalanceTxn_UsingStaticQR() throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.WalletOnly;
        String txnAmount = "2.00";
        User user = userManager.getForRead(Label.BASIC);
        WalletHelpers.modifyBalance(user,Double.valueOf(txnAmount));
        OrderDTO orderDTO = validateOfflineQRAndCreateOrder(merchantType,user);

        FastForwardRequestDTO fastForwardRequest = new FastForwardRequestDTO(merchantType, user)
                .addChecksumToRequest(merchantType.getKey())
                .setTokenType("SSO")
                .setSSOToken(user.ssoToken());

        QRHelper.executeFastForwardIVR(fastForwardRequest);

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), fastForwardRequest.getOrderId());         //validating txn status
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(fastForwardRequest.getOrderId())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .validateRefundAmnt("0.00")
                .AssertAll();
    }

    @Test(description = "Validate Success COD transaction using static QR merchant")
    public void validateCODTxn_UsingStaticQR() throws Exception {
        String paymentMode = "COD";
        Constants.MerchantType merchantType = Constants.MerchantType.Hybrid;
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = validateOfflineQRAndCreateOrder(merchantType,user);
        FetchPaymentOptResponseDTO fetchPaymentOptResponse = fetchPaymentOpt(user,orderDTO,paymentMode);
        String orderId = fetchPaymentOptResponse.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(), orderId, orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .setAuthMode("")
                .build();
        submitProcessTxnResponseFromReq(processTxnV1Request);

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderId);         //validating txn status
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("COD")
                .validateRefundAmnt("0.00")
                .AssertAll();
    }

    @Test(description = "Validate Success PPBL transaction using static QR merchant")
    public void validatePPBLTxn_UsingStaticQR() throws Exception {
        String paymentMode = "PPBL";
        Constants.MerchantType merchantType = Constants.MerchantType.Hybrid;
        User user = userManager.getForRead(Label.PPBL);
        OrderDTO orderDTO = validateOfflineQRAndCreateOrder(merchantType,user);
        FetchPaymentOptResponseDTO fetchPaymentOptResponse = fetchPaymentOpt(user,orderDTO,paymentMode);
        String orderId = fetchPaymentOptResponse.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(), orderId, orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .setMpin("1234")
                .build();

        submitProcessTxnResponseFromReq(processTxnV1Request);

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderId);         //validating txn status
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
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

    @Test(description = "Validate Success PPBL PCF transaction using static QR merchant")
    public void validatePPBLPCFTxn_UsingStaticQR() throws Exception {
        String paymentMode = "PPBL";
        Constants.MerchantType merchantType = Constants.MerchantType.PPBL_NB_PCF;
        User user = userManager.getForRead(Label.PPBL);
        OrderDTO orderDTO = validateOfflineQRAndCreateOrder(merchantType,user);
        FetchPaymentOptResponseDTO fetchPaymentOptResponse = fetchPaymentOpt(user,orderDTO,paymentMode);
        String orderId = fetchPaymentOptResponse.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(), orderId, orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .setMpin("1234")
                .build();
        Double expectedChargeFeeAmt = convenienceFeeCalculator(Double.valueOf(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT())), 0, 9.00, "PPBL");
        submitProcessTxnResponseFromReq(processTxnV1Request);

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateChargeAmount(expectedChargeFeeAmt.toString())
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderId);         //validating txn status
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
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


    @Test(description = "Validate Success transaction using Saved Card using static QR merchant")
    public void validateSuccessTxn_UsingSavedCardStaticQR() throws Exception {
        String paymentMode = "CREDIT_CARD";
        Constants.MerchantType merchantType = Constants.MerchantType.Hybrid;
        User user = userManager.getForRead(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        String cardId= SavedCardHelpers.getSavedCardId(user, 0);
        OrderDTO orderDTO = validateOfflineQRAndCreateOrder(merchantType,user);
        FetchPaymentOptResponseDTO fetchPaymentOptResponse = fetchPaymentOpt(user,orderDTO,paymentMode);
        String orderId = fetchPaymentOptResponse.getBody().getOrderId();
        String cardInfo = cardId+"||"+paymentDTO.getCvvNumber()+"|";
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(), orderId, orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .setCardInfo(cardInfo)
                .build();
        submitProcessTxnResponseFromReq(processTxnV1Request);

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderId);         //validating txn status
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
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

    @Test(description = "Validate Failure transaction when incorrect SSO token is provided using static QR merchant",groups = "Security")
    public void validateFailureTxn_InvalidSSOTokenUsingStaticQR() throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.Hybrid;
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = validateOfflineQRAndCreateOrder(merchantType,user);

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(orderDTO.getMID())
                .setToken("ABCD")
                .build();

        FetchPaymentOptResponseDTO fetchPaymentOptResponseDTO = PGPHelpers.executeFetchPaymentOpt(orderDTO.getMID(),orderDTO.getORDER_ID(),fetchPaymentOptionsDTO,true);
        Assertions.assertThat(fetchPaymentOptResponseDTO.getBody().getResultInfo().getResultMsg()).as("").isEqualTo("SSO Token is invalid");

    }

    @Test(description = "Validate Failure transaction when empty txn token is provided using static QR merchant")
    public void validateFailureTxnxnTokenUsingStaticQR() throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.Hybrid;
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = validateOfflineQRAndCreateOrder(merchantType,user);

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(orderDTO.getMID())
                .setToken("")
                .build();

        FetchPaymentOptResponseDTO fetchPaymentOptResponseDTO = PGPHelpers.executeFetchPaymentOpt(orderDTO.getMID(),orderDTO.getORDER_ID(),fetchPaymentOptionsDTO,true);
        Assertions.assertThat(fetchPaymentOptResponseDTO.getBody().getResultInfo().getResultMsg()).as("").isEqualTo("OrderId is mandatory in query parameter");

    }

    @Test(description = "Validate Failure transaction when incorrect mid is provided using static QR merchant")
    public void validateFailureTxn_WhenIncorrectMID_UsingStaticQR() throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.Hybrid;
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = validateOfflineQRAndCreateOrder(merchantType,user);

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid("123445")
                .setToken(user.ssoToken())
                .build();

        FetchPaymentOptResponseDTO fetchPaymentOptResponseDTO = PGPHelpers.executeFetchPaymentOpt("123445",orderDTO.getORDER_ID(),fetchPaymentOptionsDTO,true);
        Assertions.assertThat(fetchPaymentOptResponseDTO.getBody().getResultInfo().getResultMsg()).as("Incorrect response").isEqualTo("Mid is invalid");

    }

    @Issue("PGP-16460")
    @Test(groups = Group.Status.BUG,description = "Validate Failure transaction when incorrect emi option is provided using static QR merchant")
    public void validateFailureTxn_WhenIncorrectEMIOption_UsingStaticQR() throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.Hybrid;
        User user = userManager.getForRead(Label.BASIC);
        String emiOption = "abcd";
        OrderDTO orderDTO = validateOfflineQRAndCreateOrder(merchantType,user);
        FetchPaymentOptResponseDTO fetchPaymentOptResponse = QRHelper.fetchPaymentOptionResponse(user, orderDTO, true,emiOption);
        Assertions.assertThat(fetchPaymentOptResponse.getBody().getResultInfo().getResultMsg()).as("Incorrect response").isEqualTo("Request Parameters are not valid");

    }

    @Test(description = "Validate txn should fail if disablePaymentMode is passed as CC and txn is done by CC with static QR merchant")
    public void validateDisablePaymentModeTxnCC_UsingStaticQR() throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.Hybrid;
        User user = userManager.getForRead(Label.BASIC);
        String paymentMode = "CREDIT_CARD";
        OrderDTO orderDTO = validateOfflineQRAndCreateOrder(merchantType,user);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(orderDTO.getMID())
                .setToken(user.ssoToken())
                .setDisablePaymentMode(new DisablePaymentMode[]{new DisablePaymentMode().setMode(paymentMode)})
                .build();

        FetchPaymentOptResponseDTO fetchPaymentOptResponse = PGPHelpers.executeFetchPaymentOpt(orderDTO.getMID(),orderDTO.getORDER_ID(),fetchPaymentOptionsDTO,true);
        String orderId = fetchPaymentOptResponse.getBody().getOrderId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(),orderId, orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .build();
        ProcessTxnV1Response processTxnV1Response = PGPHelpers.executeProcessTxn(processTxnV1Request);
        Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultMsg()).as("Disable Payment mode is not working").isEqualTo(DISABLED_PAYMENT_MODE_ERROR_MSG.replace("{paymentMode}","Credit Card"));
        Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultStatus()).as("Result status is not as expected").isEqualTo("F");

    }

    @Test(description = "Validate txn should fail if disablePaymentMode is passed as DC and txn is done by DC with static QR merchant")
    public void validateDisablePaymentModeTxnDC_UsingStaticQR() throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.Hybrid;
        User user = userManager.getForRead(Label.BASIC);
        String paymentMode = "DEBIT_CARD";
        OrderDTO orderDTO = validateOfflineQRAndCreateOrder(merchantType,user);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(orderDTO.getMID())
                .setToken(user.ssoToken())
                .setDisablePaymentMode(new DisablePaymentMode[]{new DisablePaymentMode().setMode(paymentMode)})
                .build();

        FetchPaymentOptResponseDTO fetchPaymentOptResponse = PGPHelpers.executeFetchPaymentOpt(orderDTO.getMID(),orderDTO.getORDER_ID(),fetchPaymentOptionsDTO,true);
        String orderId = fetchPaymentOptResponse.getBody().getOrderId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(),orderId, orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .setCardNum(PaymentDTO.MAESTRO_DEBIT_CARD_NUMBER)
                .build();
        ProcessTxnV1Response processTxnV1Response = PGPHelpers.executeProcessTxn(processTxnV1Request);
        Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultMsg()).as("Disable Payment mode is not working").isEqualTo(DISABLED_PAYMENT_MODE_ERROR_MSG.replace("{paymentMode}","Debit Card"));
        Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultStatus()).as("Result status is not as expected").isEqualTo("F");

    }

    @Issue("PGP-13878")
    @Test(groups = Group.Status.BUG,description = "Validate txn should fail if disablePaymentMode is passed as CC with channel VISA and txn is done by CC with static QR merchant")
    public void validateDisablePaymentModeTxnCCWithChannelVisa_UsingStaticQR() throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.Hybrid;
        User user = userManager.getForRead(Label.BASIC);
        String paymentMode = "CREDIT_CARD";
        DisablePaymentMode disablePaymentMode = new DisablePaymentMode(new String[]{"VISA"}, paymentMode);
        OrderDTO orderDTO = validateOfflineQRAndCreateOrder(merchantType,user);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(orderDTO.getMID())
                .setToken(user.ssoToken())
                .setDisablePaymentMode(new DisablePaymentMode[]{disablePaymentMode})
                .build();

        FetchPaymentOptResponseDTO fetchPaymentOptResponse = PGPHelpers.executeFetchPaymentOpt(orderDTO.getMID(),orderDTO.getORDER_ID(),fetchPaymentOptionsDTO,true);
        String orderId = fetchPaymentOptResponse.getBody().getOrderId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(),orderId, orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .build();
        ProcessTxnV1Response processTxnV1Response = PGPHelpers.executeProcessTxn(processTxnV1Request);
        Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultMsg()).as("Disable Payment mode is not working").isEqualTo(DISABLED_PAYMENT_MODE_ERROR_MSG.replace("{paymentMode}","Credit Card"));
        Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultStatus()).as("Result status is not as expected").isEqualTo("F");

    }

    @Issue("PGP-13878")
    @Test(groups = Group.Status.BUG,description = "Validate txn should fail if disablePaymentMode is passed as DC and channel is passed as VISA and txn is done by DC with static QR merchant")
    public void validateDisablePaymentModeTxnDCWithChannelVISA_UsingStaticQR() throws Exception {
        String paymentMode = "DEBIT_CARD";
        Constants.MerchantType merchantType = Constants.MerchantType.Hybrid;
        DisablePaymentMode disablePaymentMode = new DisablePaymentMode(new String[]{"VISA"}, paymentMode);
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = validateOfflineQRAndCreateOrder(merchantType,user);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(orderDTO.getMID())
                .setToken(user.ssoToken())
                .setDisablePaymentMode(new DisablePaymentMode[]{disablePaymentMode})
                .build();

        FetchPaymentOptResponseDTO fetchPaymentOptResponse = PGPHelpers.executeFetchPaymentOpt(orderDTO.getMID(),orderDTO.getORDER_ID(),fetchPaymentOptionsDTO,true);
        String orderId = fetchPaymentOptResponse.getBody().getOrderId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(),orderId, orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .build();
        ProcessTxnV1Response processTxnV1Response = PGPHelpers.executeProcessTxn(processTxnV1Request);
        Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultMsg()).as("Disable Payment mode is not working").isEqualTo(DISABLED_PAYMENT_MODE_ERROR_MSG.replace("{paymentMode}","Debit Card"));
        Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultStatus()).as("Result status is not as expected").isEqualTo("F");

    }

    @Issue("PGP-13878")
    @Test(groups = Group.Status.BUG,description = "Validate txn should fail if disablePaymentMode is passed as CC and channel is passed as MASTER and txn is done by DC with static QR merchant")
    public void validateDisablePaymentModeTxnCCWithChannelMaster_UsingStaticQR() throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.Hybrid;
        String paymentMode = "CREDIT_CARD";
        DisablePaymentMode disablePaymentMode = new DisablePaymentMode(new String[]{"MASTER"}, paymentMode);
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = validateOfflineQRAndCreateOrder(merchantType,user);

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(orderDTO.getMID())
                .setToken(user.ssoToken())
                .setDisablePaymentMode(new DisablePaymentMode[]{disablePaymentMode})
                .build();

        FetchPaymentOptResponseDTO fetchPaymentOptResponse = PGPHelpers.executeFetchPaymentOpt(orderDTO.getMID(),orderDTO.getORDER_ID(),fetchPaymentOptionsDTO,true);
        String orderId = fetchPaymentOptResponse.getBody().getOrderId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(),orderId, orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .build();
        ProcessTxnV1Response processTxnV1Response = PGPHelpers.executeProcessTxn(processTxnV1Request);
        Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultMsg()).as("Disable Payment mode is not working").isEqualTo(DISABLED_PAYMENT_MODE_ERROR_MSG.replace("{paymentMode}","Credit Card"));
        Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultStatus()).as("Result status is not as expected").isEqualTo("F");

    }

    @Issue("PGP-13878")
    @Test(groups = Group.Status.BUG,description = "Validate txn should fail if disablePaymentMode is passed as DC and channel is passed as MASTER and txn is done by DC with static QR merchant")
    public void validateDisablePaymentModeTxnDCWithChannelMaster_UsingStaticQR() throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.Hybrid;
        String paymentMode = "DEBIT_CARD";
        DisablePaymentMode disablePaymentMode = new DisablePaymentMode(new String[]{"MASTER"}, paymentMode);
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = validateOfflineQRAndCreateOrder(merchantType,user);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(orderDTO.getMID())
                .setToken(user.ssoToken())
                .setDisablePaymentMode(new DisablePaymentMode[]{disablePaymentMode})
                .build();

        FetchPaymentOptResponseDTO fetchPaymentOptResponse = PGPHelpers.executeFetchPaymentOpt(orderDTO.getMID(),orderDTO.getORDER_ID(),fetchPaymentOptionsDTO,true);
        String orderId = fetchPaymentOptResponse.getBody().getOrderId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(),orderId, orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .build();
        ProcessTxnV1Response processTxnV1Response = PGPHelpers.executeProcessTxn(processTxnV1Request);
        Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultMsg()).as("Disable Payment mode is not working").isEqualTo(DISABLED_PAYMENT_MODE_ERROR_MSG.replace("{paymentMode}","Debit Card"));
        Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultStatus()).as("Result status is not as expected").isEqualTo("F");

    }



    @Test(description = "Validate txn should fail if disablePaymentMode is passed as NB and txn is done by NB with static QR merchant")
    public void validateDisablePaymentModeTxnNB_UsingStaticQR() throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.Hybrid;
        String paymentMode = "NET_BANKING";
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = validateOfflineQRAndCreateOrder(merchantType,user);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(orderDTO.getMID())
                .setToken(user.ssoToken())
                .setDisablePaymentMode(new DisablePaymentMode[]{new DisablePaymentMode().setMode(paymentMode)})
                .build();

        FetchPaymentOptResponseDTO fetchPaymentOptResponse = PGPHelpers.executeFetchPaymentOpt(orderDTO.getMID(),orderDTO.getORDER_ID(),fetchPaymentOptionsDTO,true);
        String orderId = fetchPaymentOptResponse.getBody().getOrderId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(),orderId, orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .build();
        ProcessTxnV1Response processTxnV1Response = PGPHelpers.executeProcessTxn(processTxnV1Request);
        Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultMsg()).as("Disable Payment mode is not working").isEqualTo(DISABLED_PAYMENT_MODE_ERROR_MSG.replace("{paymentMode}","Net Banking"));
        Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultStatus()).as("Result status is not as expected").isEqualTo("F");

    }



    @Test(description = "Validate txn should fail if disablePaymentMode is passed as UPI and txn is done by UPI with static QR merchant")
    public void validateDisablePaymentModeTxnUPI_UsingStaticQR() throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.Hybrid;
        String paymentMode = "UPI";
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = validateOfflineQRAndCreateOrder(merchantType,user);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(orderDTO.getMID())
                .setToken(user.ssoToken())
                .setDisablePaymentMode(new DisablePaymentMode[]{new DisablePaymentMode().setMode(paymentMode)})
                .build();

        FetchPaymentOptResponseDTO fetchPaymentOptResponse = PGPHelpers.executeFetchPaymentOpt(orderDTO.getMID(),orderDTO.getORDER_ID(),fetchPaymentOptionsDTO,true);
        String orderId = fetchPaymentOptResponse.getBody().getOrderId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(),orderId, orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .build();
        ProcessTxnV1Response processTxnV1Response = PGPHelpers.executeProcessTxn(processTxnV1Request);
        Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultMsg()).as("Disable Payment mode is not working").isEqualTo(DISABLED_PAYMENT_MODE_ERROR_MSG.replace("{paymentMode}","BHIM UPI"));
        Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultStatus()).as("Result status is not as expected").isEqualTo("F");

    }

    @Test(description = "Validate txn should fail if disablePaymentMode is passed as PPBL and txn is done by PPBL with static QR merchant")
    public void validateDisablePaymentModeTxnPPBL_UsingStaticQR() throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.Hybrid;
        String paymentMode = "PPBL";
        User user = userManager.getForRead(Label.PPBL);
        OrderDTO orderDTO = validateOfflineQRAndCreateOrder(merchantType,user);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(orderDTO.getMID())
                .setToken(user.ssoToken())
                .setDisablePaymentMode(new DisablePaymentMode[]{new DisablePaymentMode().setMode(paymentMode)})
                .build();

        FetchPaymentOptResponseDTO fetchPaymentOptResponse = PGPHelpers.executeFetchPaymentOpt(orderDTO.getMID(),orderDTO.getORDER_ID(),fetchPaymentOptionsDTO,true);
        String orderId = fetchPaymentOptResponse.getBody().getOrderId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(),orderId, orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .setMpin("5335")
                .build();
        ProcessTxnV1Response processTxnV1Response = PGPHelpers.executeProcessTxn(processTxnV1Request);
        Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultMsg()).as("Disable Payment mode is not working").isEqualTo(DISABLED_PAYMENT_MODE_ERROR_MSG.replace("{paymentMode}","Paytm Payments Bank"));
        Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultStatus()).as("Result status is not as expected").isEqualTo("F");

    }


    @Test(description = "Validate txn should fail if disablePaymentMode is passed as COD and txn is done by COD with static QR merchant")
    public void validateDisablePaymentModeTxnCOD_UsingStaticQR() throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.Hybrid;
        String paymentMode = "COD";
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = validateOfflineQRAndCreateOrder(merchantType,user);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(orderDTO.getMID())
                .setToken(user.ssoToken())
                .setDisablePaymentMode(new DisablePaymentMode[]{new DisablePaymentMode().setMode(paymentMode)})
                .build();

        FetchPaymentOptResponseDTO fetchPaymentOptResponse = PGPHelpers.executeFetchPaymentOpt(orderDTO.getMID(),orderDTO.getORDER_ID(),fetchPaymentOptionsDTO,true);
        String orderId = fetchPaymentOptResponse.getBody().getOrderId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(),orderId, orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .build();
        ProcessTxnV1Response processTxnV1Response = PGPHelpers.executeProcessTxn(processTxnV1Request);
        Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultMsg()).as("Disable Payment mode is not working").isEqualTo(DISABLED_PAYMENT_MODE_ERROR_MSG.replace("{paymentMode}","Cash on Delivery (COD)"));
        Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultStatus()).as("Result status is not as expected").isEqualTo("F");

    }

    @Issue("PGP-16466")
    @Test(groups = Group.Status.BUG,description = "Validate txn should fail if disablePaymentMode is passed as EMI and txn is done by EMI with static QR merchant")
    public void validateDisablePaymentModeTxnEMI_UsingStaticQR() throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.Hybrid;
        String paymentMode = "EMI";
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = validateOfflineQRAndCreateOrder(merchantType,user);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(orderDTO.getMID())
                .setToken(user.ssoToken())
                .setDisablePaymentMode(new DisablePaymentMode[]{new DisablePaymentMode().setMode(paymentMode)})
                .build();

        FetchPaymentOptResponseDTO fetchPaymentOptResponse = PGPHelpers.executeFetchPaymentOpt(orderDTO.getMID(),orderDTO.getORDER_ID(),fetchPaymentOptionsDTO,true);
        String orderId = fetchPaymentOptResponse.getBody().getOrderId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(),orderId, orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .setPlanId("HDFC|3")
                .setEmiType("CREDIT_CARD")
                .setCardNum("4718650100010336")
                .build();
        ProcessTxnV1Response processTxnV1Response = PGPHelpers.executeProcessTxn(processTxnV1Request);
        Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultMsg()).as("Disable Payment mode is not working").isEqualTo(DISABLED_PAYMENT_MODE_ERROR_MSG.replace("{paymentMode}","EMI"));
        Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultStatus()).as("Result status is not as expected").isEqualTo("F");

    }

    @Test(description = "Validate txn should fail if disablePaymentMode is passed as Balance and txn is done by Balance with static QR merchant")
    public void validateDisablePaymentModeTxnBalance_UsingStaticQR() throws Exception {
        String paymentMode = "BALANCE";
        Constants.MerchantType merchantType = Constants.MerchantType.Hybrid;
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = validateOfflineQRAndCreateOrder(merchantType,user);

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(orderDTO.getMID())
                .setToken(user.ssoToken())
                .setDisablePaymentMode(new DisablePaymentMode[]{new DisablePaymentMode().setMode(paymentMode)})
                .build();

        FetchPaymentOptResponseDTO fetchPaymentOptResponse = PGPHelpers.executeFetchPaymentOpt(orderDTO.getMID(),orderDTO.getORDER_ID(),fetchPaymentOptionsDTO,true);
        String orderId = fetchPaymentOptResponse.getBody().getOrderId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(),orderId, orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .build();
        ProcessTxnV1Response processTxnV1Response = PGPHelpers.executeProcessTxn(processTxnV1Request);
        Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultMsg()).as("Disable Payment mode is not working").isEqualTo(DISABLED_PAYMENT_MODE_ERROR_MSG.replace("{paymentMode}","Paytm Balance"));
        Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultStatus()).as("Result status is not as expected").isEqualTo("F");

    }


    @Test(description = "Validate if enablePaymentMode is given as CC, txn should pass with that mode only for static QR merchant")
    public void validateEnablePaymentModeTxnCC_UsingStaticQR() throws Exception {
        String paymentMode = "CREDIT_CARD";
        Constants.MerchantType merchantType = Constants.MerchantType.Hybrid;
        User user = userManager.getForRead(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        OrderDTO orderDTO = validateOfflineQRAndCreateOrder(merchantType,user);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(orderDTO.getMID())
                .setToken(user.ssoToken())
                .setEnablePaymentMode(new EnablePaymentMode[]{new EnablePaymentMode().setMode(paymentMode)})
                .build();

        FetchPaymentOptResponseDTO fetchPaymentOptResponse = PGPHelpers.executeFetchPaymentOpt(orderDTO.getMID(),orderDTO.getORDER_ID(),fetchPaymentOptionsDTO,true);
        String orderId = fetchPaymentOptResponse.getBody().getOrderId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(),orderId, orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .build();
        submitProcessTxnResponseFromReq(processTxnV1Request);

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderId);         //validating txn status
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .AssertAll();
        ProcessTxnV1Request processTxnV1Request1 = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(),orderId, orderDTO.getTXN_AMOUNT())
                .setPaymentMode("DEBIT_CARD")
                .setCardNum(PaymentDTO.MAESTRO_DEBIT_CARD_NUMBER)
                .build();

        ProcessTxnV1Response processTxnV1Response = PGPHelpers.executeProcessTxn(processTxnV1Request1);
        Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultMsg()).as("Enable Payment mode is not working").isEqualTo(DISABLED_PAYMENT_MODE_ERROR_MSG.replace("{paymentMode}","Debit Card"));
        Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultStatus()).as("Result status is not as expected").isEqualTo("F");

    }

    @Test(description = "Validate if enablePaymentMode is given as DC, txn should pass with that mode only for static QR merchant")
    public void validateEnablePaymentModeTxnDC_UsingStaticQR() throws Exception {
        String paymentMode = "DEBIT_CARD";
        Constants.MerchantType merchantType = Constants.MerchantType.Hybrid;
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = validateOfflineQRAndCreateOrder(merchantType,user);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(orderDTO.getMID())
                .setToken(user.ssoToken())
                .setEnablePaymentMode(new EnablePaymentMode[]{new EnablePaymentMode().setMode(paymentMode)})
                .build();

        FetchPaymentOptResponseDTO fetchPaymentOptResponse = PGPHelpers.executeFetchPaymentOpt(orderDTO.getMID(),orderDTO.getORDER_ID(),fetchPaymentOptionsDTO,true);
        String orderId = fetchPaymentOptResponse.getBody().getOrderId();

        PaymentDTO paymentDTO=new PaymentDTO();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(),orderId, orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .setCardNum(paymentDTO.getDebitCardNumber())
                .build();
        submitProcessTxnResponseFromReq(processTxnV1Request);

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderId);         //validating txn status
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .AssertAll();

        ProcessTxnV1Request processTxnV1Request1 = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(),orderId, orderDTO.getTXN_AMOUNT())
                .setPaymentMode("CREDIT_CARD")
                .build();

        ProcessTxnV1Response processTxnV1Response = PGPHelpers.executeProcessTxn(processTxnV1Request1);
        Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultMsg()).as("Enable Payment mode is not working").isEqualTo(DISABLED_PAYMENT_MODE_ERROR_MSG.replace("{paymentMode}","Credit Card"));
        Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultStatus()).as("Result status is not as expected").isEqualTo("F");

    }

    @Issue("PGP-13912")
    @Test(groups = Group.Status.BUG,description = "Validate if enablePaymentMode is given as DC with MAESTRO, txn should pass with that mode only for static QR merchant")
    public void validateEnablePaymentModeTxnDCWithVisa_UsingStaticQR() throws Exception {
        String paymentMode = "DEBIT_CARD";
        Constants.MerchantType merchantType = Constants.MerchantType.Hybrid;
        EnablePaymentMode enablePaymentMode = new EnablePaymentMode(new String[]{"MAESTRO"}, paymentMode);
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = validateOfflineQRAndCreateOrder(merchantType,user);

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(orderDTO.getMID())
                .setToken(user.ssoToken())
                .setEnablePaymentMode(new EnablePaymentMode[]{enablePaymentMode})
                .build();

        FetchPaymentOptResponseDTO fetchPaymentOptResponse = PGPHelpers.executeFetchPaymentOpt(orderDTO.getMID(),orderDTO.getORDER_ID(),fetchPaymentOptionsDTO,true);
        String orderId = fetchPaymentOptResponse.getBody().getOrderId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(),orderId, orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .setCardNum(PaymentDTO.MAESTRO_DEBIT_CARD_NUMBER)
                .build();
        submitProcessTxnResponseFromReq(processTxnV1Request);

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderId);         //validating txn status
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .AssertAll();

        ProcessTxnV1Request processTxnV1Request1 = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(),orderId, orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .setCardNum(PaymentDTO.MASTER_ICICI_DEBIT_CARD_NUMBER)
                .build();

        ProcessTxnV1Response processTxnV1Response = PGPHelpers.executeProcessTxn(processTxnV1Request1);
        Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultMsg()).as("Enable Payment mode is not working").isEqualTo(DISABLED_PAYMENT_MODE_ERROR_MSG.replace("{paymentMode}","Credit Card"));
        Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultStatus()).as("Result status is not as expected").isEqualTo("F");


    }

    @Issue("PGP-13912")
    @Test(groups = Group.Status.BUG,description = "Validate if enablePaymentMode is given as CC with channel VISA, txn should pass with that mode only for static QR merchant")
    public void validateEnablePaymentModeTxnCCWithMaster_UsingStaticQR() throws Exception {
        String paymentMode = "CREDIT_CARD";
        Constants.MerchantType merchantType = Constants.MerchantType.Hybrid;
        EnablePaymentMode enablePaymentMode = new EnablePaymentMode(new String[]{"VISA"}, paymentMode);
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = validateOfflineQRAndCreateOrder(merchantType,user);

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(orderDTO.getMID())
                .setToken(user.ssoToken())
                .setEnablePaymentMode(new EnablePaymentMode[]{enablePaymentMode})
                .build();

        FetchPaymentOptResponseDTO fetchPaymentOptResponse = PGPHelpers.executeFetchPaymentOpt(orderDTO.getMID(),orderDTO.getORDER_ID(),fetchPaymentOptionsDTO,true);
        String orderId = fetchPaymentOptResponse.getBody().getOrderId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(),orderId, orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .build();
        submitProcessTxnResponseFromReq(processTxnV1Request);

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderId);         //validating txn status
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .AssertAll();

        ProcessTxnV1Request processTxnV1Request1 = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(),orderId, orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .setCardNum(PaymentDTO.DINERS_CARD_NUMBER)
                .build();

        ProcessTxnV1Response processTxnV1Response = PGPHelpers.executeProcessTxn(processTxnV1Request1);
        Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultMsg()).as("Enable Payment mode is not working").isEqualTo(DISABLED_PAYMENT_MODE_ERROR_MSG.replace("{paymentMode}","Credit Card"));
        Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultStatus()).as("Result status is not as expected").isEqualTo("F");


    }

    @Test(description = "Validate if enablePaymentMode is given as NB, txn should pass with that mode only for static QR merchant")
    public void validateEnablePaymentModeTxnNB_UsingStaticQR() throws Exception {
        String paymentMode = "NET_BANKING";
        Constants.MerchantType merchantType = Constants.MerchantType.Hybrid;
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = validateOfflineQRAndCreateOrder(merchantType,user);

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(orderDTO.getMID())
                .setToken(user.ssoToken())
                .setEnablePaymentMode(new EnablePaymentMode[]{new EnablePaymentMode().setMode(paymentMode)})
                .build();

        FetchPaymentOptResponseDTO fetchPaymentOptResponse = PGPHelpers.executeFetchPaymentOpt(orderDTO.getMID(),orderDTO.getORDER_ID(),fetchPaymentOptionsDTO,true);
        String orderId = fetchPaymentOptResponse.getBody().getOrderId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(),orderId, orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .build();
        submitProcessTxnResponseFromReq(processTxnV1Request);

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderId);         //validating txn status
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("NB")
                .validateRefundAmnt("0.00")
                .AssertAll();

        ProcessTxnV1Request processTxnV1Request1 = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(),orderId, orderDTO.getTXN_AMOUNT())
                .setPaymentMode("CREDIT_CARD")
                .build();

        ProcessTxnV1Response processTxnV1Response = PGPHelpers.executeProcessTxn(processTxnV1Request1);
        Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultMsg()).as("Enable Payment mode is not working").isEqualTo(DISABLED_PAYMENT_MODE_ERROR_MSG.replace("{paymentMode}","Credit Card"));
        Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultStatus()).as("Result status is not as expected").isEqualTo("F");

    }

    @Test(description = "Validate if enablePaymentMode is given as UPI, txn should pass with that mode only for static QR merchant")
    public void validateEnablePaymentModeTxnUPI_UsingStaticQR() throws Exception {

        String paymentMode = "UPI";
        Constants.MerchantType merchantType = Constants.MerchantType.Hybrid;
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = validateOfflineQRAndCreateOrder(merchantType,user);

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(orderDTO.getMID())
                .setToken(user.ssoToken())
                .setEnablePaymentMode(new EnablePaymentMode[]{new EnablePaymentMode().setMode(paymentMode)})
                .build();

        FetchPaymentOptResponseDTO fetchPaymentOptResponse = PGPHelpers.executeFetchPaymentOpt(orderDTO.getMID(),orderDTO.getORDER_ID(),fetchPaymentOptionsDTO,true);
        String orderId = fetchPaymentOptResponse.getBody().getOrderId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(),orderId, orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .build();
        submitProcessTxnResponseFromReq(processTxnV1Request);

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderId);         //validating txn status
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("UPI")
                .validateRefundAmnt("0.00")
                .AssertAll();

        ProcessTxnV1Request processTxnV1Request1 = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(),orderId, orderDTO.getTXN_AMOUNT())
                .setPaymentMode("CREDIT_CARD")
                .build();

        ProcessTxnV1Response processTxnV1Response = PGPHelpers.executeProcessTxn(processTxnV1Request1);
        Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultMsg()).as("Enable Payment mode is not working").isEqualTo(DISABLED_PAYMENT_MODE_ERROR_MSG.replace("{paymentMode}","Credit Card"));
        Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultStatus()).as("Result status is not as expected").isEqualTo("F");

    }


    @Test(description = "Validate if enablePaymentMode is given as PPBL, txn should pass with that mode only for static QR merchant")
    public void validateEnablePaymentModeTxnPPBL_UsingStaticQR() throws Exception {
        String paymentMode = "PPBL";
        Constants.MerchantType merchantType = Constants.MerchantType.Hybrid;
        User user = userManager.getForRead(Label.PPBL);
        OrderDTO orderDTO = validateOfflineQRAndCreateOrder(merchantType,user);

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(orderDTO.getMID())
                .setToken(user.ssoToken())
                .setEnablePaymentMode(new EnablePaymentMode[]{new EnablePaymentMode().setMode(paymentMode)})
                .build();

        FetchPaymentOptResponseDTO fetchPaymentOptResponse = PGPHelpers.executeFetchPaymentOpt(orderDTO.getMID(),orderDTO.getORDER_ID(),fetchPaymentOptionsDTO,true);
        String orderId = fetchPaymentOptResponse.getBody().getOrderId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(),orderId, orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .setMpin("1234")
                .build();
        submitProcessTxnResponseFromReq(processTxnV1Request);

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderId);         //validating txn status
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("NB")
                .validateRefundAmnt("0.00")
                .AssertAll();

        ProcessTxnV1Request processTxnV1Request1 = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(),orderId, orderDTO.getTXN_AMOUNT())
                .setPaymentMode("NET_BANKING")
                .build();

        ProcessTxnV1Response processTxnV1Response = PGPHelpers.executeProcessTxn(processTxnV1Request1);
        Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultMsg()).as("Enable Payment mode is not working").isEqualTo(DISABLED_PAYMENT_MODE_ERROR_MSG.replace("{paymentMode}","Net Banking"));
        Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultStatus()).as("Result status is not as expected").isEqualTo("F");

    }


    @Test(description = "Validate if enablePaymentMode is given as COD, txn should pass with that mode only for static QR merchant")
    public void validateEnablePaymentModeTxnCOD_UsingStaticQR() throws Exception {
        String paymentMode = "COD";
        Constants.MerchantType merchantType = Constants.MerchantType.Hybrid;
        User user = userManager.getForRead(Label.POSTPAIDONBOARDING);
        WalletHelpers.setZeroBalance(user);
        PostpaidHelpers.updatePostpaidUserAttributes(user, PostpaidHelpers.WHITELISTED);
        PostpaidHelpers.updateBalance("1000");
        OrderDTO orderDTO = validateOfflineQRAndCreateOrder(merchantType,user);

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(orderDTO.getMID())
                .setToken(user.ssoToken())
                .setEnablePaymentMode(new EnablePaymentMode[]{new EnablePaymentMode().setMode(paymentMode)})
                .build();

        FetchPaymentOptResponseDTO fetchPaymentOptResponse = PGPHelpers.executeFetchPaymentOpt(orderDTO.getMID(),orderDTO.getORDER_ID(),fetchPaymentOptionsDTO,true);
        String orderId = fetchPaymentOptResponse.getBody().getOrderId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(),orderId, orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .build();
        submitProcessTxnResponseFromReq(processTxnV1Request);

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderId);         //validating txn status
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("COD")
                .validateRefundAmnt("0.00")
                .AssertAll();

        ProcessTxnV1Request processTxnV1Request1 = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(),orderId, orderDTO.getTXN_AMOUNT())
                .setPaymentMode("NET_BANKING")
                .build();

        ProcessTxnV1Response processTxnV1Response = PGPHelpers.executeProcessTxn(processTxnV1Request1);
        Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultMsg()).as("Enable Payment mode is not working").isEqualTo(DISABLED_PAYMENT_MODE_ERROR_MSG.replace("{paymentMode}","Net Banking"));
        Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultStatus()).as("Result status is not as expected").isEqualTo("F");

    }

    @Test(description = "Validate if enablePaymentMode is given as EMI, txn should pass with that mode only for static QR merchant")
    public void validateEnablePaymentModeTxnEMI_UsingStaticQR() throws Exception {
        String paymentMode = "EMI";
        Constants.MerchantType merchantType = Constants.MerchantType.Hybrid;
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = validateOfflineQRAndCreateOrder(merchantType,user);

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(orderDTO.getMID())
                .setToken(user.ssoToken())
                .setEnablePaymentMode(new EnablePaymentMode[]{new EnablePaymentMode().setMode(paymentMode)})
                .build();

        FetchPaymentOptResponseDTO fetchPaymentOptResponse = PGPHelpers.executeFetchPaymentOpt(orderDTO.getMID(),orderDTO.getORDER_ID(),fetchPaymentOptionsDTO,true);
        String orderId = fetchPaymentOptResponse.getBody().getOrderId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(),orderId, orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .setPlanId("HDFC|3")
                .setEmiType("CREDIT_CARD")
                .setCardNum("4718650100010336")
                .build();

        submitProcessTxnResponseFromReq(processTxnV1Request);

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderId);         //validating txn status
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .AssertAll();

        ProcessTxnV1Request processTxnV1Request1 = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(),orderId, orderDTO.getTXN_AMOUNT())
                .setPaymentMode("NET_BANKING")
                .build();

        ProcessTxnV1Response processTxnV1Response = PGPHelpers.executeProcessTxn(processTxnV1Request1);
        Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultMsg()).as("Enable Payment mode is not working").isEqualTo(DISABLED_PAYMENT_MODE_ERROR_MSG.replace("{paymentMode}","Net Banking"));
        Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultStatus()).as("Result status is not as expected").isEqualTo("F");

    }


    @Test(description = "Validate if mid in req url and body are different")
    public void validateIfMIDInReqAndURLAreDifferent_UsingStaticQR() throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.Hybrid;
        User user = userManager.getForRead(Label.BASIC);

        OrderDTO orderDTO = validateOfflineQRAndCreateOrder(merchantType,user);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(Constants.MerchantType.WalletOnly.getId())
                .setToken(user.ssoToken())
                .build();

        FetchPaymentOptResponseDTO fetchPaymentOptResponse = PGPHelpers.executeFetchPaymentOpt(orderDTO.getMID(),orderDTO.getORDER_ID(),fetchPaymentOptionsDTO,true);
        Assertions.assertThat(fetchPaymentOptResponse.getBody().getResultInfo().getResultMsg()).as("Result message is not as expected").isEqualTo("Mid in the query param doesn't match with the Mid send in the request");

    }

    //TODO: Need to Debug further why PPBL and Postpaid not coming
    @Test(description = "Verify with Pgonly merchant, all modes which are enabled on merchant should get displayed except wallet")
    public void validatePGOnlyMerchantPayModes() throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly;
        User user = userManager.getForRead(Label.BASIC);

        OrderDTO orderDTO = new OrderFactory.Hybrid(merchantType, theme, user)
                .setTXN_AMOUNT("2.00")
                .setSSO_TOKEN("")
                .build();
        pre_requisite:
        {
            PGPHelpers.validate_MerchantPreference(merchantType.getId(), "DYNAMIC_QR_CASHIER", "N");
        }


        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(Constants.MerchantType.PGOnly.getId())
                .setToken(user.ssoToken())
                .build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(orderDTO.getMID(),fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptResponse=fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptResponse.param("status", "false").getList(
                "body.merchantPayOption.paymentModes.findAll { paymentModes -> paymentModes.isDisabled.status == status }.paymentMode"))
                .as("All pay modes are not returned on merchant")
                .containsExactlyInAnyOrder("CREDIT_CARD","EMI","UPI","PPBL","COD","DEBIT_CARD","NET_BANKING","PAYTM_DIGITAL_CREDIT");
        Assertions.assertThat(fetchPaymentOptResponse.param("status", "false").getList(
                "body.merchantPayOption.paymentModes.findAll { paymentModes -> paymentModes.isDisabled.status == status }.paymentMode"))
                .as("All pay modes are not returned on merchant")
                .doesNotContain("BALANCE");
    }


    @Test(description = "Verify with Wallet Only merchant,only BALANCE is enabled on merchant should get displayed")
    public void validateWalletOnlyPayModes() throws Exception {

        Constants.MerchantType merchantType = Constants.MerchantType.WalletOnly;
        User user = userManager.getForRead(Label.BASIC);

        OrderDTO orderDTO = new OrderFactory.Hybrid(merchantType, theme, user)
                .setTXN_AMOUNT("2.00")
                .setSSO_TOKEN("")
                .build();
        pre_requisite:
        {
            PGPHelpers.validate_MerchantPreference(merchantType.getId(), "DYNAMIC_QR_CASHIER", "N");
        }

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(Constants.MerchantType.WalletOnly.getId())
                .setToken(user.ssoToken())
                .build();

        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(orderDTO.getMID(),fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptResponse=fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptResponse.param("status", "false").getList(
                "body.merchantPayOption.paymentModes.findAll { paymentModes -> paymentModes.isDisabled.status == status }.paymentMode"))
                .as("All pay modes are not returned on merchant")
                .doesNotContain("CREDIT_CARD","EMI","UPI","PPBL","COD","DEBIT_CARD","NET_BANKING","PAYTM_DIGITAL_CREDIT");
        Assertions.assertThat(fetchPaymentOptResponse.param("status", "false").getList(
                "body.merchantPayOption.paymentModes.findAll { paymentModes -> paymentModes.isDisabled.status == status }.paymentMode"))
                .as("All pay modes are not returned on merchant")
                .contains("BALANCE");

        //Assert.assertTrue(!QRHelper.extractAllPaymentModes(fetchPaymentOptResponse).containsAll(allPaymentModesDisabledOnMerchant));
        //Assert.assertTrue(QRHelper.extractAllPaymentModes(fetchPaymentOptResponse).contains("BALANCE"),"Balance should not be disabled");
    }


    @Test(description = "Verify the response for fetchEMI details")
    public void validateResponseWhenEMIIsAvailable() throws Exception {

        Constants.MerchantType merchantType = Constants.MerchantType.Hybrid;
        User user = userManager.getForRead(Label.BASIC);

        pre_requisite:
        {
            PGPHelpers.validate_MerchantPreference(merchantType.getId(), "DYNAMIC_QR_CASHIER", "N");
        }

        FetchEMIDetailRequest fetchEMIDetailRequest = new FetchEMIDetailRequest("SSO",user.ssoToken(),"HDFC",merchantType.getId());
        Response res = new FetchEMIDetail(fetchEMIDetailRequest,merchantType.getId()).execute();
        Assertions.assertThat("body.emiDetail.emiChannelInfos[0].planId").as("Plan Id is empty").isNotEmpty();
    }







    @Test(description = "validate Result Info For payment Retry Due To Insufficient Wallet Balance when Retry is present on merchant for offline Txn")
    public void validateRetryInfoWalletRetryMerchantOfflineTxn() throws Exception {
        String paymentMode = "BALANCE";
        Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_ADDNPAY;
        User user = userManager.getForRead(Label.BASIC);
        WalletHelpers.setZeroBalance(user);
        OrderDTO orderDTO = validateOfflineQRAndCreateOrder(merchantType,user);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(orderDTO.getMID())
                .setToken(user.ssoToken())
                .build();

        FetchPaymentOptResponseDTO fetchPaymentOptResponse = PGPHelpers.executeFetchPaymentOpt(orderDTO.getMID(),orderDTO.getORDER_ID(),fetchPaymentOptionsDTO,true);
        String orderId = fetchPaymentOptResponse.getBody().getOrderId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(),orderId, orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .build();

        ProcessTransactionV1 processTransaction = new ProcessTransactionV1(processTxnV1Request);

        Response response = processTransaction.execute();


        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultMsg"))
                .isEqualTo("Wallet balance Insufficient");

        Assertions.assertThat(response.jsonPath().getString("body.retryInfo.failureMessage"))
                .as("Incorrect failure Message")
                .isEqualTo("Balance Not Enough");

        Assertions.assertThat(response.jsonPath().getString("body.retryInfo.blockerMessage"))
                .as("Response Message is incorrect")
                .isEqualTo("Your payment has been failed due to wallet Balance Insufficient");

    }




    @Test(description = "validate Result Info For payment Retry Due To Insufficient Wallet Balance when Retry is NOT present on merchant for offline Txn")
    public void validateRetryInfoNotPresentForWalletWithoutRetryMerchantOfflineTxn() throws Exception {
        String paymentMode = "BALANCE";
        Constants.MerchantType merchantType = Constants.MerchantType.WalletOnly;
        User user = userManager.getForRead(Label.BASIC);
        WalletHelpers.setZeroBalance(user);
        OrderDTO orderDTO = validateOfflineQRAndCreateOrder(merchantType,user);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(orderDTO.getMID())
                .setToken(user.ssoToken())
                .build();

        FetchPaymentOptResponseDTO fetchPaymentOptResponse = PGPHelpers.executeFetchPaymentOpt(orderDTO.getMID(),orderDTO.getORDER_ID(),fetchPaymentOptionsDTO,true);
        String orderId = fetchPaymentOptResponse.getBody().getOrderId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(),orderId, orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .build();

        ProcessTransactionV1 processTransaction = new ProcessTransactionV1(processTxnV1Request);

        Response response = processTransaction.execute();

        Assert.assertFalse(response.getBody().asString().contains("retryInfo"));

    }

    @Epic(Constants.Sprint.SPRINT30_2)
    @Feature("PGP-9042")
    @Test(description = "Verify Successful PPI txn when wallet amount is 0 at fetch pay option and sufficient while doing PTC request for Offline")
    public void validateAmountZeroAtFetchPayAndSufficientWhilePTCForOffline() throws Exception {
        String paymentMode = "BALANCE";
        String txnAmount = "2.15";
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.setZeroBalance(user);
        Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_WALLET_ONLY;
        OrderDTO orderDTO = validateOfflineQRAndCreateOrder(merchantType,user);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(orderDTO.getMID())
                .setToken(user.ssoToken())
                .build();

        FetchPaymentOptResponseDTO fetchPaymentOptResponse = PGPHelpers.executeFetchPaymentOpt(orderDTO.getMID(),orderDTO.getORDER_ID(),fetchPaymentOptionsDTO,true);
        String orderId = fetchPaymentOptResponse.getBody().getOrderId();

//Balance added after fetch pay option
        WalletHelpers.modifyBalance(user, Double.valueOf(txnAmount));

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(),orderId, txnAmount)
                .setPaymentMode(paymentMode)
                .build();

        ProcessTransactionV1 processTransaction = new ProcessTransactionV1(processTxnV1Request);
        Response response = processTransaction.execute();
        Assert.assertEquals((response.jsonPath().get("body.resultInfo.resultMsg")),"Success");
        Assert.assertEquals((response.jsonPath().get("body.txnInfo.GATEWAYNAME")),"WALLET");
        Assert.assertEquals((response.jsonPath().get("body.txnInfo.TXNAMOUNT")),txnAmount);
    }

    @Epic(Constants.Sprint.SPRINT30_2)
    @Feature("PGP-9042")
    @Parameters({"isNativePlus"})
    @Test(description = "Verify add n pay success transaction with zero amount in wallet while fetchpayoption and sufficient while doing PTC txn. for offline txn")
    public void validateAmountZeroAtFetchPayAndSufficientWhilePTCForADDNPAYForOfflineTxn(@Optional("false") Boolean isNativePlus) throws Exception {
        String paymentMode = "BALANCE";
        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_ADDNPAY;
        String txnAmount = "2.00";
        WalletHelpers.setZeroBalance(user);
        OrderDTO orderDTO = validateOfflineQRAndCreateOrder(merchantType,user);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(orderDTO.getMID())
                .setToken(user.ssoToken())
                .build();

        FetchPaymentOptResponseDTO fetchPaymentOptResponse = PGPHelpers.executeFetchPaymentOpt(orderDTO.getMID(),orderDTO.getORDER_ID(),fetchPaymentOptionsDTO,true);
        String orderId = fetchPaymentOptResponse.getBody().getOrderId();

        WalletHelpers.modifyBalance(user, Double.valueOf(txnAmount));

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(),orderId, txnAmount)
                .setPaymentMode(paymentMode)
                .build();

        ProcessTransactionV1 processTransaction = new ProcessTransactionV1(processTxnV1Request);
        Response response = processTransaction.execute();
        Assert.assertEquals((response.jsonPath().get("body.resultInfo.resultMsg")),"Success");
        Assert.assertEquals((response.jsonPath().get("body.txnInfo.GATEWAYNAME")),"WALLET");
        Assert.assertEquals((response.jsonPath().get("body.txnInfo.TXNAMOUNT")),txnAmount);
    }

    @Epic(Constants.Sprint.SPRINT30_2)
    @Feature("PGP-9042")
    @Parameters({"isNativePlus"})
    @Test(description = "Verify PPI failure transaction with sufficient amount at fetch pay option and insufficient amount while doing PTC")
    public void validateSufficientAmountAtFetchPayAndInSufficientWhilePTCForPPIOffline(@Optional("false") Boolean isNativePlus) throws Exception {
        String paymentMode = "BALANCE";
        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_WALLET_ONLY;
        String txnAmount = "7.00";
        WalletHelpers.modifyBalance(user, 3.0);
        OrderDTO orderDTO = validateOfflineQRAndCreateOrder(merchantType,user);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(orderDTO.getMID())
                .setToken(user.ssoToken())
                .build();

        FetchPaymentOptResponseDTO fetchPaymentOptResponse = PGPHelpers.executeFetchPaymentOpt(orderDTO.getMID(),orderDTO.getORDER_ID(),fetchPaymentOptionsDTO,true);
        String orderId = fetchPaymentOptResponse.getBody().getOrderId();

        WalletHelpers.modifyBalance(user,5.0);

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(),orderId, txnAmount)
                .setPaymentMode(paymentMode)
                .build();

        ProcessTransactionV1 processTransaction = new ProcessTransactionV1(processTxnV1Request);
        Response response = processTransaction.execute();
        Assert.assertEquals((response.jsonPath().get("body.resultInfo.resultMsg")),"Wallet balance Insufficient");
        Assert.assertEquals((response.jsonPath().get("body.txnInfo.GATEWAYNAME")),"WALLET");
        Assert.assertEquals((response.jsonPath().get("body.txnInfo.TXNAMOUNT")),txnAmount);
    }

    @Epic(Constants.Sprint.SPRINT30_2)
    @Feature("PGP-9042")
    @Parameters({"isNativePlus"})
    @Test(description = "validate Successful Txn With Amount not equal to zero but less While Fetch Pay And Increased While PTC")
    public void validateSuccessfulTxnWithAmountlessWhileFetchPayAndIncreaseWhilePTCOffline(@Optional("false") Boolean isNativePlus) throws Exception {
        String paymentMode = "BALANCE";
        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_ADDNPAY;
        String txnAmount = "5.00";
        WalletHelpers.modifyBalance(user,3.0);
        OrderDTO orderDTO = validateOfflineQRAndCreateOrder(merchantType,user);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(orderDTO.getMID())
                .setToken(user.ssoToken())
                .build();

        FetchPaymentOptResponseDTO fetchPaymentOptResponse = PGPHelpers.executeFetchPaymentOpt(orderDTO.getMID(),orderDTO.getORDER_ID(),fetchPaymentOptionsDTO,true);
        String orderId = fetchPaymentOptResponse.getBody().getOrderId();

        WalletHelpers.modifyBalance(user, Double.valueOf(txnAmount));

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(),orderId, txnAmount)
                .setPaymentMode(paymentMode)
                .build();

        ProcessTransactionV1 processTransaction = new ProcessTransactionV1(processTxnV1Request);
        Response response = processTransaction.execute();
        Assert.assertEquals((response.jsonPath().get("body.resultInfo.resultMsg")),"Success");
        Assert.assertEquals((response.jsonPath().get("body.txnInfo.GATEWAYNAME")),"WALLET");
        Assert.assertEquals((response.jsonPath().get("body.txnInfo.TXNAMOUNT")),txnAmount);
    }


    @Epic(Constants.Sprint.SPRINT30_2)
    @Feature("PGP-9042")
    @Parameters({"isNativePlus"})
    @Test(description = "validate Successful Txn With Amount not equal to zero but less While Fetch Pay And Increased While PTC for Hybrid merchant for offline")
    public void validateSuccessfulTxnWithAmountlessWhileFetchPayAndIncreaseWhilePTCForHybridTxnOffline(@Optional("false") Boolean isNativePlus) throws Exception {
        String paymentMode = "BALANCE";
        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_HYBRID;
        String txnAmount = "5.00";
        WalletHelpers.modifyBalance(user, 3.00);

        OrderDTO orderDTO = validateOfflineQRAndCreateOrder(merchantType,user);

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(orderDTO.getMID())
                .setToken(user.ssoToken())
                .build();

        FetchPaymentOptResponseDTO fetchPaymentOptResponse = PGPHelpers.executeFetchPaymentOpt(orderDTO.getMID(),orderDTO.getORDER_ID(),fetchPaymentOptionsDTO,true);

        String orderId = fetchPaymentOptResponse.getBody().getOrderId();
        WalletHelpers.modifyBalance(user, 6.0);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(),orderId, txnAmount)
                .setPaymentMode(paymentMode)
                .build();

        ProcessTransactionV1 processTransaction = new ProcessTransactionV1(processTxnV1Request);
        Response response = processTransaction.execute();
        Assert.assertEquals((response.jsonPath().get("body.resultInfo.resultMsg")),"Success");
        Assert.assertEquals((response.jsonPath().get("body.txnInfo.GATEWAYNAME")),"WALLET");
        Assert.assertEquals((response.jsonPath().get("body.txnInfo.TXNAMOUNT")),txnAmount);
        WalletHelpers.validateBalance(user, 1.00);
    }

    @AfterClass
    public void setFf4jFlagEnabledifDisabled() {
        if(!ff4jFlagEnabled) {
            String queryForExp = "UPDATE FF4J_FEATURES SET ENABLE = 1 WHERE FEAT_UID = 'paymentRetryInfo';";
            DatabaseUtil.getInstance().executeUpdateQuery(LocalConfig.PGP_DB_CONNECTION_URL, queryForExp);
            RedisUtil.getInstance().getConnection(LocalConfig.PG_REDIS_URI).del("FF4J_FEATURE_paymentRetryInfo");
        }
    }
}



