/**
 * @desc This class is used to run test cases for close order V2 API
 */

package scripts.api.theia.colseOrderv2;

import com.paytm.api.CloseOrderV2Api;
import com.paytm.api.TxnStatus;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.CloseOrderV2.CloseOrderDTO;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.response.FetchPaymentOptResponseDTO;
import com.paytm.dto.NativeDTO.InitTxn.ExtendInfo;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.framework.reporting.Owners;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
import com.paytm.utils.ff4j.FF4JFlags;
import com.paytm.utils.merchant.util.PayMethodType;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Date;

@Owners(author = "Samar Aswal", qa = "Eshani")
@Feature("PGP-27186")
@Owner("Samar Aswal")
public class CloseOrder extends PGPBaseTest {

    /**
     * @desc This variable is used to store CC details
     */
    private static final String CCPaymentDetails = "|4718650100010336|882|052026";

    /**
     * @desc This variable is used to store additional info for offline flow
     */
    private String additionalInfo = "payeeType:MERCHANT|currencyCode:INR|category:Gas and Petrol|subCategory:BPCL Pump|service:P2M|mode:QR_CODE|offlinePostConvenience:false|merchantContactNo:9899267758|mappingId:GultiF44414842981731|pgEnabled:true|qrCodeId:{qrCode}|ORDER_ID:{ORDER_ID}|orderAlreadyCreated:|EXPIRY_DATE:|NAME:Gulti Filling Station|MERCHANT_NAME:Gulti Filling Station|MOBILE_NO:9373106192|TXN_AMOUNT|INDUSTRY_TYPE_ID:Retail|MERCHANT_GUID:5b28bcda-1a3c-4cac-91ff-9d82ddbc6558|MERCHANT_STATUS:ACTIVE|qr_code_id:{qrCode}|comment:|REQUEST_TYPE:QR_MERCHANT|";

    /**
     * @desc THis variable is used to store User
     */
    private ThreadLocal<User> user = new ThreadLocal<User>();

    /**
     * @desc This variable is used to store SSO token
     */
    private ThreadLocal<String> ssoToken = new ThreadLocal<String>();

    /**
     * @desc This variable is used to store transaction token
     */
    private ThreadLocal<String> txnToken = new ThreadLocal<String>();

    /**
     * @desc This variable is used to store orerId
     */
    private ThreadLocal<String> orderId = new ThreadLocal<String>();

    /**
     * @desc THis variable is used to store MID
     */
    private ThreadLocal<String> merchantId = new ThreadLocal<String>();

    /**
     * @desc Used to store validateRespMsg
     */
    private ThreadLocal<String> validateRespMsg = new ThreadLocal<String>();

    /**
     * @desc Used to store validateStatus
     */
    private ThreadLocal<String> validateStatus = new ThreadLocal<String>();

    /**
     * @desc This variable is uesd to store gateway name
     */
    private ThreadLocal<String> validateGatewayName = new ThreadLocal<String>();

    /**
     * @desc This function is used to store bank name
     */
    private ThreadLocal<String> validateBankName = new ThreadLocal<String>();

    /**
     * @desc This function is used to store txn type
     */
    private ThreadLocal<String> validateTxnType = new ThreadLocal<String>();

    /**
     * @desc This variable is used to store response code
     */
    private ThreadLocal<String> validateRespCode = new ThreadLocal<String>();

    /**
     * @desc This variable is used to store bank txn Id
     */
    private ThreadLocal<Constants.ValidationType> validateBankTxnId = new ThreadLocal<Constants.ValidationType>();

    /**
     * @desc This variable is used to store payment method
     */
    private ThreadLocal<String> paymenthod = new ThreadLocal<String>();


    /**
     * @param merchantType
     * @param body
     * @return
     * @throws Exception
     * @desc : This function is used to generate checksum
     */

    private String generateChecksum(Constants.MerchantType merchantType, Object body) throws Exception {
        return PGPHelpers.getNativeChecksum(merchantType.getKey(), body);
    }

    /**
     * @throws Exception
     * @desc This function is used to generate SSO Token
     */
    private void generateSsoToken() throws Exception {
        User user1 = userManager.getForRead(PGPBaseTest.Label.BASIC);
        ssoToken.set(user1.ssoToken());
    }


    /**
     * @param merchantType
     * @param amount
     * @throws Exception
     * @desc This function is used to hit initiate transaction API
     */
    private void initiateTxn(Constants.MerchantType merchantType, String amount) throws Exception {
        Reporter.log("SSO Token = " + ssoToken, true);
        ExtendInfo extendInfo = new ExtendInfo();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken.get(), merchantType)
                .setTxnValue(amount)
                .setExtendInfo(extendInfo)
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        txnToken.set(response.jsonPath().getString("body.txnToken"));
        Reporter.log("Transaction Token = " + txnToken, true);
        Assertions.assertThat(txnToken).as("TXN Token is null").isNotNull();
        merchantId.set(initTxnDTO.getBody().getMid());
        Reporter.log("MID = " + merchantId, true);
        orderId.set(initTxnDTO.getBody().getOrderId());
        Reporter.log("OrderID = " + orderId, true);
    }


    /**
     * @param isNativePlus
     * @param orderId
     * @param txnToken
     * @param transactionStatus
     * @param amount
     * @param merchantType
     * @param paymentMode
     * @param isTxnStatusValidate
     * @desc : This function is used to perform native and native plus transactions
     */
    private void nativeTransaction(Boolean isNativePlus, String orderId, String txnToken, String transactionStatus, String amount, Constants.MerchantType merchantType, String paymentMode, boolean isTxnStatusValidate) {
        System.out.println("Order ID = " + orderId);
        CheckoutPage checkoutPage = new CheckoutPage();
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, orderId, txnToken, PayMethodType.CREDIT_CARD).build();
        System.out.println(orderDTO);
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        if (isTxnStatusValidate) {
            String txnMode;
            setTransactionStatusValues(transactionStatus);
            if (transactionStatus.equalsIgnoreCase("pending")) {
                txnMode = "default";
            } else {
                txnMode = paymentMode;
            }
            getTransactionBankDetails(txnMode);
            validateTransactions(orderDTO.getMID(), orderDTO.getORDER_ID(), amount);
        }
    }

    /**
     * @param theme
     * @param amount
     * @param userSsoToken
     * @param requestType
     * @param merchantType
     * @param transactionStatus
     * @param paymentMode
     * @desc : This function is used to perform enhanced transctions
     */
    private void enhanceTransaction(String theme, String amount, String userSsoToken, String requestType, Constants.MerchantType merchantType, String transactionStatus, String paymentMode) {
        CheckoutPage checkoutPage = new CheckoutPage();
        OrderDTO orderDTO = new OrderFactory.PGOnly(merchantType, theme)
                .setTXN_AMOUNT(amount)
                .setSSO_TOKEN(userSsoToken)
                .setREQUEST_TYPE(requestType)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        orderId.set(orderDTO.getORDER_ID());
        System.out.println("Order Id = " + orderId);
        merchantId.set(orderDTO.getMID());
        System.out.println("MID = " + merchantId);
        setTransactionStatusValues(transactionStatus);
        String txnMode;
        if (transactionStatus.equalsIgnoreCase("pending")) {
            txnMode = "default";
        } else {
            txnMode = paymentMode;
        }
        getTransactionBankDetails(txnMode);
        validateTransactions(merchantId.get(), orderId.get(), amount);
    }


    /**
     * @param theme
     * @param amount
     * @param userSsoToken
     * @param transactionStatus
     * @param user
     * @throws Exception
     * @desc This function is used to add money transaction
     */
    private void addMoneyTranaction(String theme, String amount, String userSsoToken, String transactionStatus, User user) throws Exception {
        OrderDTO orderDTO = new OrderFactory.AddMoney(Constants.MerchantType.AddMoney, theme, user)
                .setTXN_AMOUNT(amount)
                .setSSO_TOKEN(userSsoToken)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.DC, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        orderId.set(orderDTO.getORDER_ID());
        System.out.println("orderId = " + orderId.get());
        merchantId.set(orderDTO.getMID());
        System.out.println("MID = " + merchantId.get());
        System.out.println("Amount = " + orderDTO.getTXN_AMOUNT());
        setTransactionStatusValues(transactionStatus);
        getTransactionBankDetails("DC");
        validateTransactions(merchantId.get(), orderId.get(), orderDTO.getTXN_AMOUNT());
    }

    /**
     * @param transactionStatus
     * @desc This function is set ransaction status values
     */
    private void setTransactionStatusValues(String transactionStatus) {
        switch (transactionStatus) {
            case "success":
                validateStatus.set("TXN_SUCCESS");
                validateRespMsg.set("Txn Success");
                validateTxnType.set("SALE");
                validateRespCode.set("01");
                validateBankTxnId.set(Constants.ValidationType.NON_EMPTY);
                break;

            case "failure":
                validateStatus.set("TXN_FAILURE");
                validateRespMsg.set("Your payment has been declined by your bank. Please contact your bank for any queries. If money has been deducted from your account, your bank will inform us within 48 hrs and we will refund the same");
                validateTxnType.set("SALE");
                validateRespCode.set("227");
                validateBankTxnId.set(Constants.ValidationType.NON_EMPTY);
                break;

            case "pending":
                validateStatus.set("PENDING");
                validateRespMsg.set("Looks like the payment is not complete. Please wait while we confirm the status with your bank.");
                validateTxnType.set("SALE");
                validateRespCode.set("402");
                validateBankTxnId.set(Constants.ValidationType.EMPTY);
                break;

            case "AddMoneySuccess":
                validateStatus.set("TXN_SUCCESS");
                validateRespMsg.set("Txn Success");
                validateTxnType.set("ADDMONEY");
                validateRespCode.set("01");
                validateBankTxnId.set(Constants.ValidationType.NON_EMPTY);
                break;

            case "AddMoneyFailure":
                validateStatus.set("TXN_FAILURE");
                validateRespMsg.set("Your payment has been declined by your bank. Please contact your bank for any queries. If money has been deducted from your account, your bank will inform us within 48 hrs and we will refund the same");
                validateTxnType.set("ADDMONEY");
                validateRespCode.set("227");
                validateBankTxnId.set(Constants.ValidationType.NON_EMPTY);
                break;

            case "AddMoneyPending":
                validateStatus.set("PENDING");
                validateRespMsg.set("Looks like the payment is not complete. Please wait while we confirm the status with your bank.");
                validateTxnType.set("ADDMONEY");
                validateRespCode.set("402");
                validateBankTxnId.set(Constants.ValidationType.EMPTY);
                break;

        }
    }


    /**
     * @param paymode
     * @desc this function is used to get bank details of transaction
     */
    private void getTransactionBankDetails(String paymode) {
        switch (paymode) {
            case "CC":
                if (validateStatus.get().equalsIgnoreCase("pending")) {
                    validateGatewayName.set(null);
                } else {
                    validateGatewayName.set(Constants.Gateway.HDFC.toString());
                }
                validateBankName.set(Constants.Bank.HDFC.toString());
                paymenthod.set("CC");
                break;

            case "DC":
                if (validateStatus.get().equalsIgnoreCase("pending")) {
                    validateGatewayName.set(null);
                } else {
                    validateGatewayName.set(Constants.Gateway.HDFC.toString());
                }
                validateBankName.set(Constants.Bank.HDFC.toString());
                paymenthod.set("DC");
                break;

            default:
                validateGatewayName.set(null);
                validateBankName.set(null);
                paymenthod.set(null);
                break;
        }
    }

    /**
     * @param mid
     * @param orderId
     * @param amount
     * @desc This funcion is used to validate success transaction
     */
    private void validateTransactions(String mid, String orderId, String amount) {
        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        if (validateStatus.get().equalsIgnoreCase("pending")) {
            txnStatus.executeUntilPending();
        } else {
            txnStatus.executeUntilNotPending();
        }
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(validateBankTxnId.get())
                .validateOrderid(orderId)
                .validateTxnAmount(amount)
                .validateStatus(validateStatus.get())
                .validateTxnType(validateTxnType.get())
                .validateGatewayName(validateGatewayName.get())
                .validateRespCode(validateRespCode.get())
                .validateRespMsg(validateRespMsg.get())
                .validateBankName(validateBankName.get())
                .validateMid(mid)
                .validatePaymentMode(paymenthod.get())
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    /**
     * @param generateOrderId
     * @param mid
     * @param userSsoToken
     * @return
     * @throws IOException
     * @desc This function is used to hit fetch payment option API
     */
    private FetchPaymentOptResponseDTO fetchPaymentOptionResponse(boolean generateOrderId, String mid, String userSsoToken) throws IOException {
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(generateOrderId))
                .setMid(mid)
                .setToken(userSsoToken)
                .build();
        return PGPHelpers.executeFetchPaymentOpt(mid, "", fetchPaymentOptionsDTO, generateOrderId);
    }

    /**
     * @param generateOrderId
     * @param mid
     * @param userSsoToken
     * @param amount
     * @throws IOException
     * @desc This function is used to perform offline transaction
     */
    private void ofllineTxn(boolean generateOrderId, String mid, String userSsoToken, String amount) throws IOException {
        FetchPaymentOptResponseDTO fetchPaymentOptResponse;
        fetchPaymentOptResponse = fetchPaymentOptionResponse(generateOrderId, mid, userSsoToken);
        orderId.set(fetchPaymentOptResponse.getBody().getOrderId());
        System.out.println("OrderID for offline FPO = " + orderId.get());
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid, "SSO", userSsoToken, orderId.get(), amount)
                .setAuthMode("otp")
                .setPayerAccount("")
                .setChannelCode("")
                .setCardInfo(CCPaymentDetails)
                .setExtendedInfoCloseOrderOffline(additionalInfo)
                .setCustId("")
                .build();
        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
    }

    /**
     * @param statusCode
     * @param apiResponse
     * @desc : This function is used to assert closeOrder API on status code
     */
    private void closeOrderV2ApiStatusCodeAssertion(int statusCode, String apiResponse) {
        switch (apiResponse) {
            case "success":
            case "invalidTxnToken":
            case "invalidOrderId":
            case "invalidChecksum":
            case "closeSuccesTxn":
            case "closePendingTxn":
            case "closeFailureTxn":
            case "closeSuccesOfflineTxn":
                Assert.assertEquals(statusCode, 200);
                break;

            case "invalidSso":
                Assert.assertEquals(statusCode, 401);
                break;
        }
    }

    /**
     * @param responseMessage
     * @param apiResponse
     * @desc : This function is used to assert closeOrder API on resultMsg
     */
    private void closeOrderV2ApiRespMsgAssertion(String responseMessage, String apiResponse) {
        switch (apiResponse) {
            case "success":
            case "closeFailureTxn":
                Assert.assertEquals(responseMessage, "SUCCESS");
                break;

            case "invalidTxnToken":
                Assert.assertEquals(responseMessage, "Your Session has expired.");
                break;

            case "invalidOrderId":
                Assert.assertEquals(responseMessage, "Invalid Request");
                break;

            case "invalidSso":
                Assert.assertEquals(responseMessage, "SSO Token is invalid");
                break;

            case "invalidChecksum":
                Assert.assertEquals(responseMessage, "Checksum provided is invalid");
                break;

            case "closeSuccesTxn":
            case "closeSuccesOfflineTxn":
                Assert.assertEquals(responseMessage, "Order status is invalid");
                break;

            case "closePendingTxn":
                Assert.assertEquals(responseMessage, "Internal Processing Error");
                break;

            case "closeAddMoneyFailureTxn":
                Assert.assertEquals(responseMessage, "Order status is closed");
                break;
        }
    }

    /**
     * @param response
     * @param apiResponse
     * @desc This function is used to appy assertion on close order API response
     */
    private void assertCloseOrderAPi(Response response, String apiResponse) {
        int statusCode = response.statusCode();
        Reporter.log("Actual status = " + statusCode, true);
        closeOrderV2ApiStatusCodeAssertion(statusCode, apiResponse);
        JsonPath closeOrderResponseJson = response.jsonPath();
        String responseMessage = closeOrderResponseJson.getString("body.resultInfo.resultMsg").toString();
        Reporter.log("Actual resultMsg = " + responseMessage, true);
        closeOrderV2ApiRespMsgAssertion(responseMessage, apiResponse);
    }


    @Test(description = "Txn initiated and is at cashier page only (INIT state) close order- fpo called only with valid SSO token")
    public void closeOrderSsoToken() throws Exception {
        try {
      //      FF4JFlags.enable("createOrderinIntTxn");
            generateSsoToken();
            initiateTxn(Constants.MerchantType.PGOnly, "2");
            FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken.get()).build();
            FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchantId.get(), orderId.get(), fetchPaymentOptionsDTO);
            JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
            CloseOrderV2Api closeOrderV2Api = new CloseOrderV2Api("SSO", merchantId.get(), orderId.get(), ssoToken.get());
            assertCloseOrderAPi(closeOrderV2Api.execute(),
                    "success");
        } finally {
        //    FF4JFlags.disable("createOrderinIntTxn");
        }

    }


    /**
     * @throws Exception
     * @desc =
     */
    @Test(description = "Txn initiated and is at cashier page only (INIT state) close order- fpo called only with valid txn token")
    public void closeOrderTxnToken() throws Exception {
        try {
       //     FF4JFlags.enable("createOrderinIntTxn");
            generateSsoToken();
            initiateTxn(Constants.MerchantType.PGOnly, "2");
            FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken.get()).build();
            FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchantId.get(), orderId.get(), fetchPaymentOptionsDTO);
            JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
            CloseOrderV2Api closeOrderV2Api = new CloseOrderV2Api("TXN_TOKEN", merchantId.get(), orderId.get(), txnToken.get());
            assertCloseOrderAPi(closeOrderV2Api.execute(),
                    "success");
        } finally {
      //      FF4JFlags.disable("createOrderinIntTxn");
        }
    }


    @Test(description = "Txn initiated and is at cashier page only (INIT state) close order- fpo called only with invalid SSO token")
    public void closeOrderInvalidSsoToken() throws Exception {
        generateSsoToken();
        initiateTxn(Constants.MerchantType.PGOnly, "2");
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken.get()).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchantId.get(), orderId.get(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        CloseOrderV2Api closeOrderV2Api = new CloseOrderV2Api("SSO", merchantId.get(), orderId.get(), "13456776");
        assertCloseOrderAPi(closeOrderV2Api.execute(),
                "invalidSso");
    }


    @Test(description = "Txn initiated and is at cashier page only (INIT state) close order- fpo called only with invalid txn token")
    public void closeOrderInvalidTxnToken() throws Exception {
        generateSsoToken();
        initiateTxn(Constants.MerchantType.PGOnly, "2");
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken.get()).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchantId.get(), orderId.get(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        CloseOrderV2Api closeOrderV2Api = new CloseOrderV2Api("TXN_TOKEN", merchantId.get(), orderId.get(), "45678987654356789");
        assertCloseOrderAPi(closeOrderV2Api.execute(),
                "invalidTxnToken");
    }


    @Test(description = "Invalid orderId with valid txn token")
    public void closeOrderInvalidOrderidVaildTxnToken() throws Exception {
        generateSsoToken();
        initiateTxn(Constants.MerchantType.PGOnly, "2");
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken.get()).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchantId.get(), orderId.get(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        CloseOrderV2Api closeOrderV2Api = new CloseOrderV2Api("TXN_TOKEN", merchantId.get(),
                "876578876", txnToken.get());
        assertCloseOrderAPi(closeOrderV2Api.execute(), "invalidOrderId");
    }


    @Test(description = "Invalid checksum")
    public void closeOrderInvalidChecksum() throws Exception {
        generateSsoToken();
        initiateTxn(Constants.MerchantType.PGOnly, "2");
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken.get()).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchantId.get(), orderId.get(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        CloseOrderV2Api closeOrderV2Api = new CloseOrderV2Api("CHECKSUM", merchantId.get(), orderId.get(), txnToken.get());
        assertCloseOrderAPi(closeOrderV2Api.execute(),
                "invalidChecksum");
    }


    @Test(description = "Txn initiated and is at cashier page only (INIT state) close order- fpo called only with valid checksum")
    public void closeOrderChecksumToken() throws Exception {
        try {
        //    FF4JFlags.enable("createOrderinIntTxn");
            generateSsoToken();
            initiateTxn(Constants.MerchantType.PGOnly, "2");
            CloseOrderDTO closeOrderDTO = new CloseOrderDTO.Builder()
                    .setMid(merchantId.get())
                    .setOrderId(orderId.get())
                    .build();
            Object checksumBody = closeOrderDTO.getBody();
            FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken.get()).build();
            FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchantId.get(), orderId.get(), fetchPaymentOptionsDTO);
            JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
            CloseOrderV2Api closeOrderV2Api = new CloseOrderV2Api("CHECKSUM", merchantId.get(), orderId.get(),
                    generateChecksum(Constants.MerchantType.PGOnly, checksumBody));
            assertCloseOrderAPi(closeOrderV2Api.execute(),
                    "success");
        } finally {
       //     FF4JFlags.disable("createOrderinIntTxn");
        }
    }


    @Test(description = "Hit close order API with SSO token for Native plus transaction")
    public void closeOrderSsoSuccessNativePlusTxn() throws Exception {
        generateSsoToken();
        initiateTxn(Constants.MerchantType.PGOnly, "24");
        nativeTransaction(true, orderId.get(), txnToken.get(), "success", "24", Constants.MerchantType.PGOnly, "CC", true);
        CloseOrderV2Api closeOrderV2Api = new CloseOrderV2Api("SSO", merchantId.get(), orderId.get(), ssoToken.get());
        assertCloseOrderAPi(closeOrderV2Api.execute(), "closeSuccesTxn");
    }


    @Test(description = "Hit close order API with Txn token for Native plus transaction")
    public void closeOrderTxnTokenSuccessNativePlusTxn() throws Exception {
        generateSsoToken();
        initiateTxn(Constants.MerchantType.PGOnly, "24");
        nativeTransaction(true, orderId.get(), txnToken.get(), "success", "24", Constants.MerchantType.PGOnly, "CC", true);
        CloseOrderV2Api closeOrderV2Api = new CloseOrderV2Api("TXN_TOKEN", merchantId.get(),
                orderId.get(), txnToken.get());
        assertCloseOrderAPi(closeOrderV2Api.execute(), "closeSuccesTxn");
    }


    @Test(description = "Hit close order API with checksum token for Native plus transaction")
    public void closeOrderChecksumSuccessNativePlusTxn() throws Exception {
        generateSsoToken();
        initiateTxn(Constants.MerchantType.PGOnly, "24");
        CloseOrderDTO closeOrderDTO = new CloseOrderDTO.Builder()
                .setMid(merchantId.get())
                .setOrderId(orderId.get())
                .build();
        Object checksumBody = closeOrderDTO.getBody();
        nativeTransaction(true, orderId.get(), txnToken.get(), "success", "24", Constants.MerchantType.PGOnly, "CC", true);
        CloseOrderV2Api closeOrderV2Api = new CloseOrderV2Api("CHECKSUM", merchantId.get(), orderId.get(),
                generateChecksum(Constants.MerchantType.PGOnly, checksumBody));
        assertCloseOrderAPi(closeOrderV2Api.execute(), "closeSuccesTxn");
    }


    @Test(description = "Hit close order API with SSO token for Native transaction")
    public void closeOrderSsoSuccessNativeTxn() throws Exception {
        generateSsoToken();
        initiateTxn(Constants.MerchantType.PGOnly, "24");
        nativeTransaction(false, orderId.get(), txnToken.get(), "success", "24", Constants.MerchantType.PGOnly, "CC", true);
        CloseOrderV2Api closeOrderV2Api = new CloseOrderV2Api("SSO", merchantId.get(), orderId.get(), ssoToken.get());
        assertCloseOrderAPi(closeOrderV2Api.execute(), "closeSuccesTxn");
    }


    @Test(description = "Hit close order API with Txn token for Native transaction")
    public void closeOrderTxnTokenSuccessNativeTxn() throws Exception {
        generateSsoToken();
        initiateTxn(Constants.MerchantType.PGOnly, "24");
        nativeTransaction(false, orderId.get(), txnToken.get(), "success", "24", Constants.MerchantType.PGOnly, "CC", true);
        CloseOrderV2Api closeOrderV2Api = new CloseOrderV2Api("TXN_TOKEN", merchantId.get(), orderId.get(), txnToken.get());
        assertCloseOrderAPi(closeOrderV2Api.execute(), "closeSuccesTxn");
    }


    @Test(description = "Hit close order API with checksum token for Native transaction")
    public void closeOrderChecksumSuccessNativeTxn() throws Exception {
        generateSsoToken();
        initiateTxn(Constants.MerchantType.PGOnly, "24");
        CloseOrderDTO closeOrderDTO = new CloseOrderDTO.Builder()
                .setMid(merchantId.get())
                .setOrderId(orderId.get())
                .build();
        Object checksumBody = closeOrderDTO.getBody();
        nativeTransaction(false, orderId.get(), txnToken.get(), "success", "24", Constants.MerchantType.PGOnly, "CC", true);
        CloseOrderV2Api closeOrderV2Api = new CloseOrderV2Api("CHECKSUM", merchantId.get(), orderId.get(),
                generateChecksum(Constants.MerchantType.PGOnly, checksumBody));
        assertCloseOrderAPi(closeOrderV2Api.execute(), "closeSuccesTxn");
    }


    @Test(description = "Hit close order API with Sso token for success enhanced transaction")
    @Parameters({"theme"})
    public void closeOrderSsoSuccessEnhanceTxn(@Optional("enhancedweb_revamp") String theme) throws Exception {
        generateSsoToken();
        enhanceTransaction(theme, "30", ssoToken.get(),
                "DEFAULT", Constants.MerchantType.PGOnly, "success", "CC");
        CloseOrderV2Api closeOrderV2Api = new CloseOrderV2Api("SSO", merchantId.get(), orderId.get(), ssoToken.get());
        assertCloseOrderAPi(closeOrderV2Api.execute(), "closeSuccesTxn");
    }


    @Test(description = "Hit close order API with checksum token for success enhanced transaction")
    @Parameters({"theme"})
    public void closeOrderChecksumSuccessEnhanceTxn(@Optional("enhancedweb_revamp") String theme) throws Exception {
        generateSsoToken();
        enhanceTransaction(theme, "24", ssoToken.get(),
                "DEFAULT", Constants.MerchantType.PGOnly, "success", "CC");
        CloseOrderDTO closeOrderDTO = new CloseOrderDTO.Builder()
                .setMid(merchantId.get())
                .setOrderId(orderId.get())
                .build();
        Object checksumBody = closeOrderDTO.getBody();
        CloseOrderV2Api closeOrderV2Api = new CloseOrderV2Api("CHECKSUM", merchantId.get(), orderId.get(),
                generateChecksum(Constants.MerchantType.PGOnly, checksumBody));
        assertCloseOrderAPi(closeOrderV2Api.execute(), "closeSuccesTxn");
    }


    @Test(description = "Hit close order API with SSO token for Native plus failure transaction")
    public void closeOrderSsoFailureNativePlusTxn() throws Exception {
        generateSsoToken();
        initiateTxn(Constants.MerchantType.PRIORITY_DEFAULT_MERCHANT, "99.98");
        nativeTransaction(true, orderId.get(), txnToken.get(), "failure", "99.98", Constants.MerchantType.PRIORITY_DEFAULT_MERCHANT, "CC", false);
        CloseOrderV2Api closeOrderV2Api = new CloseOrderV2Api("SSO", merchantId.get(), orderId.get(), ssoToken.get());
        assertCloseOrderAPi(closeOrderV2Api.execute(), "closeFailureTxn");
    }


    @Test(description = "Hit close order API with Txn token for Native plus failure transaction")
    public void closeOrderTxnTokenFailureNativePlusTxn() throws Exception {
        generateSsoToken();
        initiateTxn(Constants.MerchantType.PRIORITY_DEFAULT_MERCHANT, "99.98");
        nativeTransaction(true, orderId.get(), txnToken.get(), "failure", "99.98", Constants.MerchantType.PRIORITY_DEFAULT_MERCHANT, "CC", false);
        CloseOrderV2Api closeOrderV2Api = new CloseOrderV2Api("TXN_TOKEN", merchantId.get(), orderId.get(), txnToken.get());
        assertCloseOrderAPi(closeOrderV2Api.execute(), "closeFailureTxn");
    }

    @Test(description = "Hit close order API with checksum token for Native plus failure transaction")
    public void closeOrderChecksumFailureNativePlusTxn() throws Exception {
        generateSsoToken();
        initiateTxn(Constants.MerchantType.PRIORITY_DEFAULT_MERCHANT, "99.98");
        CloseOrderDTO closeOrderDTO = new CloseOrderDTO.Builder()
                .setMid(merchantId.get())
                .setOrderId(orderId.get())
                .build();
        Object checksumBody = closeOrderDTO.getBody();
        nativeTransaction(true, orderId.get(), txnToken.get(), "failure", "99.98", Constants.MerchantType.PRIORITY_DEFAULT_MERCHANT, "CC", false);
        CloseOrderV2Api closeOrderV2Api = new CloseOrderV2Api("CHECKSUM", merchantId.get(), orderId.get(),
                generateChecksum(Constants.MerchantType.PRIORITY_DEFAULT_MERCHANT, checksumBody));
        assertCloseOrderAPi(closeOrderV2Api.execute(), "closeFailureTxn");
    }


    @Test(description = "Hit close order API with SSO token for Native failure transaction")
    public void closeOrderSsoFailureNativeTxn() throws Exception {
        generateSsoToken();
        initiateTxn(Constants.MerchantType.PRIORITY_DEFAULT_MERCHANT, "99.98");
        nativeTransaction(false, orderId.get(), txnToken.get(), "failure", "99.98", Constants.MerchantType.PRIORITY_DEFAULT_MERCHANT, "CC", false);
        CloseOrderV2Api closeOrderV2Api = new CloseOrderV2Api("SSO", merchantId.get(), orderId.get(), ssoToken.get());
        assertCloseOrderAPi(closeOrderV2Api.execute(), "closeFailureTxn");
    }


    @Test(description = "Hit close order API with Txn token for Native failure transaction")
    public void closeOrderTxnTokenFailureNativeTxn() throws Exception {
        generateSsoToken();
        initiateTxn(Constants.MerchantType.PRIORITY_DEFAULT_MERCHANT, "99.98");
        nativeTransaction(false, orderId.get(), txnToken.get(), "failure", "99.98", Constants.MerchantType.PRIORITY_DEFAULT_MERCHANT, "CC", false);
        CloseOrderV2Api closeOrderV2Api = new CloseOrderV2Api("TXN_TOKEN", merchantId.get(), orderId.get(), txnToken.get());
        assertCloseOrderAPi(closeOrderV2Api.execute(), "closeFailureTxn");
    }


    @Test(description = "Hit close order API with checksum token for Native failure transaction")
    public void closeOrderChecksumFailureNativeTxn() throws Exception {
        generateSsoToken();
        initiateTxn(Constants.MerchantType.PRIORITY_DEFAULT_MERCHANT, "99.98");
        CloseOrderDTO closeOrderDTO = new CloseOrderDTO.Builder()
                .setMid(merchantId.get())
                .setOrderId(orderId.get())
                .build();
        Object checksumBody = closeOrderDTO.getBody();
        nativeTransaction(false, orderId.get(), txnToken.get(), "failure", "99.98", Constants.MerchantType.PRIORITY_DEFAULT_MERCHANT, "CC", false);
        CloseOrderV2Api closeOrderV2Api = new CloseOrderV2Api("CHECKSUM", merchantId.get(), orderId.get(),
                generateChecksum(Constants.MerchantType.PRIORITY_DEFAULT_MERCHANT, checksumBody));
        assertCloseOrderAPi(closeOrderV2Api.execute(), "closeFailureTxn");
    }


    @Test(description = "Hit close order API with SSO token for Native plus pending transaction")
    public void closeOrderSsoPendingNativePlusTxn() throws Exception {
        generateSsoToken();
        initiateTxn(Constants.MerchantType.PRIORITY_DEFAULT_MERCHANT, "99.84");
        nativeTransaction(true, orderId.get(), txnToken.get(), "pending", "99.84", Constants.MerchantType.PRIORITY_DEFAULT_MERCHANT, "CC", true);
        CloseOrderV2Api closeOrderV2Api = new CloseOrderV2Api("SSO", merchantId.get(), orderId.get(), ssoToken.get());
        assertCloseOrderAPi(closeOrderV2Api.execute(), "closePendingTxn");
    }


    @Test(description = "Hit close order API with Txn token for Native plus pending transaction")
    public void closeOrderTxnTokenPendingNativePlusTxn() throws Exception {
        generateSsoToken();
        initiateTxn(Constants.MerchantType.PRIORITY_DEFAULT_MERCHANT, "99.84");
        nativeTransaction(true, orderId.get(), txnToken.get(), "pending", "99.84", Constants.MerchantType.PRIORITY_DEFAULT_MERCHANT, "CC", true);
        CloseOrderV2Api closeOrderV2Api = new CloseOrderV2Api("TXN_TOKEN", merchantId.get(), orderId.get(), txnToken.get());
        assertCloseOrderAPi(closeOrderV2Api.execute(), "closePendingTxn");
    }


    @Test(description = "Hit close order API with checksum token for Native plus pending transaction")
    public void closeOrderChecksumPendingNativePlusTxn() throws Exception {
        generateSsoToken();
        initiateTxn(Constants.MerchantType.PRIORITY_DEFAULT_MERCHANT, "99.84");
        CloseOrderDTO closeOrderDTO = new CloseOrderDTO.Builder()
                .setMid(merchantId.get())
                .setOrderId(orderId.get())
                .build();
        Object checksumBody = closeOrderDTO.getBody();
        nativeTransaction(true, orderId.get(), txnToken.get(), "pending", "99.84", Constants.MerchantType.PRIORITY_DEFAULT_MERCHANT, "CC", true);
        CloseOrderV2Api closeOrderV2Api = new CloseOrderV2Api("CHECKSUM", merchantId.get(), orderId.get(),
                generateChecksum(Constants.MerchantType.PRIORITY_DEFAULT_MERCHANT, checksumBody));
        assertCloseOrderAPi(closeOrderV2Api.execute(), "closePendingTxn");
    }


    @Test(description = "Hit close order API with SSO token for Native pending transaction")
    public void closeOrderSsoPendingNativeTxn() throws Exception {
        generateSsoToken();
        initiateTxn(Constants.MerchantType.PRIORITY_DEFAULT_MERCHANT, "99.84");
        nativeTransaction(false, orderId.get(), txnToken.get(), "pending", "99.84", Constants.MerchantType.PRIORITY_DEFAULT_MERCHANT, "CC", true);
        CloseOrderV2Api closeOrderV2Api = new CloseOrderV2Api("SSO", merchantId.get(), orderId.get(), ssoToken.get());
        assertCloseOrderAPi(closeOrderV2Api.execute(), "closePendingTxn");
    }


    @Test(description = "Hit close order API with Txn token for Native pending transaction")
    public void closeOrderTxnTokenPendingNativeTxn() throws Exception {
        generateSsoToken();
        initiateTxn(Constants.MerchantType.PRIORITY_DEFAULT_MERCHANT, "99.84");
        nativeTransaction(false, orderId.get(), txnToken.get(), "pending", "99.84", Constants.MerchantType.PRIORITY_DEFAULT_MERCHANT, "CC", true);
        CloseOrderV2Api closeOrderV2Api = new CloseOrderV2Api("TXN_TOKEN", merchantId.get(), orderId.get(), txnToken.get());
        assertCloseOrderAPi(closeOrderV2Api.execute(), "closePendingTxn");
    }


    @Test(description = "Hit close order API with checksum token for Native pending transaction")
    public void closeOrderChecksumPendingNativeTxn() throws Exception {
        generateSsoToken();
        initiateTxn(Constants.MerchantType.PRIORITY_DEFAULT_MERCHANT, "99.84");
        CloseOrderDTO closeOrderDTO = new CloseOrderDTO.Builder()
                .setMid(merchantId.get())
                .setOrderId(orderId.get())
                .build();
        Object checksumBody = closeOrderDTO.getBody();
        nativeTransaction(false, orderId.get(), txnToken.get(), "pending", "99.84", Constants.MerchantType.PRIORITY_DEFAULT_MERCHANT, "CC", true);
        CloseOrderV2Api closeOrderV2Api = new CloseOrderV2Api("CHECKSUM", merchantId.get(), orderId.get(),
                generateChecksum(Constants.MerchantType.PRIORITY_DEFAULT_MERCHANT, checksumBody));
        assertCloseOrderAPi(closeOrderV2Api.execute(), "closePendingTxn");
    }


    @Test(description = "Hit close order API with Sso token for success enhanced pending transaction")
    @Parameters({"theme"})
    public void closeOrderSsoPendingEnhanceTxn(@Optional("enhancedweb_revamp") String theme) throws Exception {
        generateSsoToken();
        enhanceTransaction(theme, "99.84", ssoToken.get(),
                "DEFAULT", Constants.MerchantType.PRIORITY_DEFAULT_MERCHANT, "pending", "CC");
        CloseOrderV2Api closeOrderV2Api = new CloseOrderV2Api("SSO", merchantId.get(), orderId.get(), ssoToken.get());
        assertCloseOrderAPi(closeOrderV2Api.execute(), "closePendingTxn");
    }


    @Test(description = "Hit close order API with checksum token for success enhanced pending transaction")
    @Parameters({"theme"})
    public void closeOrderChecksumPendingEnhanceTxn(@Optional("enhancedweb_revamp") String theme) throws Exception {
        generateSsoToken();
        enhanceTransaction(theme, "99.84", ssoToken.get(),
                "DEFAULT", Constants.MerchantType.PRIORITY_DEFAULT_MERCHANT, "pending", "CC");
        CloseOrderDTO closeOrderDTO = new CloseOrderDTO.Builder()
                .setMid(merchantId.get())
                .setOrderId(orderId.get())
                .build();
        Object checksumBody = closeOrderDTO.getBody();
        CloseOrderV2Api closeOrderV2Api = new CloseOrderV2Api("CHECKSUM", merchantId.get(), orderId.get(),
                generateChecksum(Constants.MerchantType.PRIORITY_DEFAULT_MERCHANT, checksumBody));
        assertCloseOrderAPi(closeOrderV2Api.execute(), "closePendingTxn");
    }


    @Test(description = "Hit close order API with Sso token for success offline success transaction")
    public void closeOrderSsoSuccessOfflineTxn() throws Exception {
        generateSsoToken();
        ofllineTxn(true, Constants.MerchantType.PGOnly.getId(), ssoToken.get(), "2");
        CloseOrderV2Api closeOrderV2Api = new CloseOrderV2Api("SSO", Constants.MerchantType.PGOnly.getId(),
                orderId.get(), ssoToken.get());
        assertCloseOrderAPi(closeOrderV2Api.execute(), "closeSuccesOfflineTxn");
    }


    @Test(description = "Hit close order API with SSO token for success Add Money transaction")
    @Parameters({"theme"})
    public void closeOrderSSoAddMoneySuccess(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        addMoneyTranaction(theme, "24", user.ssoToken(), "AddMoneySuccess", user);
        CloseOrderV2Api closeOrderV2Api = new CloseOrderV2Api("SSO", merchantId.get(), orderId.get(), user.ssoToken());
        assertCloseOrderAPi(closeOrderV2Api.execute(), "closeSuccesTxn");
    }


    @Test(description = "Hit close order API with checksum token for success Add Money transaction")
    @Parameters({"theme"})
    public void closeOrderChecksumAddMoneySuccess(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        addMoneyTranaction(theme, "24", user.ssoToken(), "AddMoneySuccess", user);
        CloseOrderDTO closeOrderDTO = new CloseOrderDTO.Builder()
                .setMid(merchantId.get())
                .setOrderId(orderId.get())
                .build();
        Object checksumBody = closeOrderDTO.getBody();
        CloseOrderV2Api closeOrderV2Api = new CloseOrderV2Api("CHECKSUM", merchantId.get(), orderId.get(),
                generateChecksum(Constants.MerchantType.AddMoney, checksumBody));
        assertCloseOrderAPi(closeOrderV2Api.execute(), "closeSuccesTxn");
    }


    @Test(description = "Hit close order API with SSO token for failure Add Money transaction")
    @Parameters({"theme"})
    public void closeOrderSSoAddMoneyFailure(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        addMoneyTranaction(theme, "99.98", user.ssoToken(), "AddMoneyFailure", user);
        CloseOrderV2Api closeOrderV2Api = new CloseOrderV2Api("SSO", merchantId.get(), orderId.get(), user.ssoToken());
        assertCloseOrderAPi(closeOrderV2Api.execute(), "closeAddMoneyFailureTxn");
    }


    @Test(description = "Hit close order API with checksum for failure Add Money transaction")
    @Parameters({"theme"})
    public void closeOrderChecksumAddMoneyFailure(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        addMoneyTranaction(theme, "99.98", user.ssoToken(), "AddMoneyFailure", user);
        CloseOrderDTO closeOrderDTO = new CloseOrderDTO.Builder()
                .setMid(merchantId.get())
                .setOrderId(orderId.get())
                .build();
        Object checksumBody = closeOrderDTO.getBody();
        CloseOrderV2Api closeOrderV2Api = new CloseOrderV2Api("CHECKSUM", merchantId.get(), orderId.get(),
                generateChecksum(Constants.MerchantType.AddMoney, checksumBody));
        assertCloseOrderAPi(closeOrderV2Api.execute(), "closeAddMoneyFailureTxn");
    }


    @Test(description = "Hit close order API with SSO token for pending Add Money transaction")
    @Parameters({"theme"})
    public void closeOrderSSoAddMoneyPending(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        addMoneyTranaction(theme, "99.84", user.ssoToken(), "AddMoneyPending", user);
        CloseOrderV2Api closeOrderV2Api = new CloseOrderV2Api("SSO", merchantId.get(), orderId.get(), user.ssoToken());
        assertCloseOrderAPi(closeOrderV2Api.execute(), "success");
    }


    @Test(description = "Hit close order API with checksum token for pending Add Money transaction")
    @Parameters({"theme"})
    public void closeOrderChecksumAddMoneyPending(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        addMoneyTranaction(theme, "99.84", user.ssoToken(), "AddMoneyPending", user);
        CloseOrderDTO closeOrderDTO = new CloseOrderDTO.Builder()
                .setMid(merchantId.get())
                .setOrderId(orderId.get())
                .build();
        Object checksumBody = closeOrderDTO.getBody();
        CloseOrderV2Api closeOrderV2Api = new CloseOrderV2Api("CHECKSUM", merchantId.get(), orderId.get(),
                generateChecksum(Constants.MerchantType.AddMoney, checksumBody));
        assertCloseOrderAPi(closeOrderV2Api.execute(), "success");
    }

}
